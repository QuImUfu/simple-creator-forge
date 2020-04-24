package quimufu.simple_creator;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.Level;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static quimufu.simple_creator.SimpleCreatorMod.log;

public class ItemResourceLoader extends GenericManualResourceLoader<Item> {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final String dataType = "items";
    public static Map<ResourceLocation, Item> items = Maps.newHashMap();

    ItemResourceLoader() {
        super(GSON, dataType);
    }

    @Override
    protected void register(ResourceLocation id, Item thing) {
        //no-op on forge :(
    }

    public Item deserialize(Pair<ResourceLocation, JsonObject> e) {
        JsonObject jo = e.getSecond();
        String group = JSONUtils.getString(jo, "group", "misc");
        ItemGroup g = findGroup(group);
        int durability = JSONUtils.getInt(jo, "durability", 0);
        byte stackSize = JSONUtils.func_219795_a(jo, "stackSize", (byte) 1);
        boolean isFood = JSONUtils.hasField(jo, "food");
        String rarity = JSONUtils.getString(jo, "rarity", "common");


        Item.Properties settings = new Item.Properties();
        settings.group(g);
        if (isFood) {
            if (durability != 0) {
                log(Level.WARN, "durability does not work with food");
                log(Level.WARN, "ignoring");
            }
            settings.maxStackSize(stackSize);
            JsonObject jsonFoodObject = JSONUtils.getJsonObject(jo, "food");
            settings.food(deserializeFoodComponent(jsonFoodObject));
        } else if (durability != 0 && stackSize != 1) {
            log(Level.WARN, "durability and stackSize do not work together");
            log(Level.WARN, "ignoring stackSize");
            settings.maxDamage(durability);
        } else {
            if (durability != 0) {
                settings.maxDamage(durability);
            } else {
                settings.maxStackSize(stackSize);
            }
        }
        settings.rarity(findRarity(rarity));
        return new Item(settings);
    }

    @Override
    protected void save(ResourceLocation id, Item item) {
        items.put(id, item);
    }

    private static Food deserializeFoodComponent(JsonObject jo) {
        Food fc;
        Food.Builder fcb = new Food.Builder();
        fcb.hunger(JSONUtils.getInt(jo, "hunger", 4));
        fcb.saturation(JSONUtils.getFloat(jo, "saturationModifier", 0.3F));
        if (JSONUtils.getBoolean(jo, "isAlwaysEdible", false))
            fcb.setAlwaysEdible();
        if (JSONUtils.getBoolean(jo, "isWolfFood", false))
            fcb.meat();
        if (JSONUtils.getBoolean(jo, "isFast", false))
            fcb.fastToEat();
        if (JSONUtils.isJsonArray(jo, "effects")) {
            JsonArray jsonEffectsArray = JSONUtils.getJsonArray(jo, "effects");
            deserializeEffects(fcb, jsonEffectsArray);
        }
        fc = fcb.build();
        return fc;
    }

    private static void deserializeEffects(Food.Builder fcb, JsonArray ja) {
        for (JsonElement e : ja) {
            Effect type;
            int duration = 0;
            int amplifier = 0;
            boolean ambient = false;
            boolean visible = true;
            float chance = 1.F;
            JsonObject jo = JSONUtils.getJsonObject(e, "effects");
            String effect = JSONUtils.getString(jo, "effect");
            ResourceLocation ei = ResourceLocation.tryCreate(effect);
            if (ei != null) {
                Effect se = ForgeRegistries.POTIONS.getValue(ei);
                if (se != null) {
                    type = se;
                } else {
                    log(Level.WARN, "Effect " + ei + " not found, skipping");
                    continue;
                }
            } else {
                log(Level.WARN, "Effect id " + effect + " invalid, skipping");
                continue;
            }
            duration = JSONUtils.getInt(jo, "duration", duration);
            amplifier = JSONUtils.getInt(jo, "amplifier", amplifier);
            ambient = JSONUtils.getBoolean(jo, "ambient", ambient);
            visible = JSONUtils.getBoolean(jo, "visible", visible);
            chance = JSONUtils.getFloat(jo, "chance", chance);
            fcb.effect(new EffectInstance(type, duration, amplifier, ambient, visible), chance);
        }
    }

    private static Rarity findRarity(String filter) {
        for (Rarity r : Rarity.values()) {
            if (r.name().toLowerCase().equals(filter.toLowerCase()))
                return r;
        }
        log(Level.WARN, "Rarity " + filter + " not found, using common");
        log(Level.INFO, "Valid groups:" + Arrays.stream(Rarity.values()).map(Rarity::name).map(String::toLowerCase).map(s -> s + "\n").collect(Collectors.joining()));
        return Rarity.COMMON;
    }

    public static ItemGroup findGroup(String filter) {
        for (ItemGroup g : ItemGroup.GROUPS) {
            if (g.getPath().toLowerCase().equals(filter.toLowerCase())) {
                return g;
            }
        }
        log(Level.WARN, "Item Group " + filter + " not found, using misc");
        log(Level.INFO, "Valid groups:" + Arrays.stream(ItemGroup.GROUPS).map(ItemGroup::getPath).map(s -> s + "\n").collect(Collectors.joining()));
        return ItemGroup.MISC;
    }


    public void forgeRegisterItems(RegistryEvent.Register<Item> itemRegistryEvent) {
        IForgeRegistry<Item> reg = itemRegistryEvent.getRegistry();
        for (Map.Entry<ResourceLocation, Item> i : items.entrySet()) {
            i.getValue().setRegistryName(i.getKey());
            reg.register(i.getValue());
        }
    }
}
