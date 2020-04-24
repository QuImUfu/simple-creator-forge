package quimufu.simple_creator;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Field;
import java.util.Map;

import static quimufu.simple_creator.SimpleCreatorMod.log;


public class BlockResourceLoader extends GenericManualResourceLoader<Pair<Block, Item>> {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final String dataType = "blocks";
    public static Map<ResourceLocation, Block> blocks = Maps.newHashMap();
    public static Map<ResourceLocation, Item> blockItems = Maps.newHashMap();

    BlockResourceLoader() {
        super(GSON, dataType);
    }

    @Override
    protected void register(ResourceLocation id, Pair<Block, Item> thing) {
        //no-op on forge :(
    }

    @Override
    protected void save(ResourceLocation id, Pair<Block, Item> thing) {
        blocks.put(id, thing.getFirst());
        blockItems.put(id, thing.getSecond());
    }

    @Override
    protected Pair<Block, Item> deserialize(Pair<ResourceLocation, JsonObject> e) {
        JsonObject jo = e.getSecond();
        Material material;
        if (JSONUtils.isString(jo, "material")) {
            String materialString = JSONUtils.getString(jo, "material");
            material = getMaterial(materialString);
        } else if (JSONUtils.getJsonObject(jo, "material", null) != null) {
            // get material information
            JsonObject jmo = JSONUtils.getJsonObject(jo, "material");
            MaterialSettingsPojo mspj = GSON.fromJson(jmo, MaterialSettingsPojo.class);
            //build material
            material = getSettings(mspj);
        } else {
            material = Material.EARTH;
        }

        // get block information
        BlockSettingsPojo bspj = GSON.fromJson(jo, BlockSettingsPojo.class);

        // move block information in Block.Properties (!!hacky!!)
        Block.Properties bs = getSettings(material, bspj);

        // parse item group
        String group = JSONUtils.getString(jo, "itemGroup", "misc");
        ItemGroup g = ItemResourceLoader.findGroup(group);
        //create block and corresponding item
        Block resB = new Block(bs);
        Item resI = new BlockItem(resB, new Item.Properties().group(g));

        FireBlock fireBlock = (FireBlock) Blocks.FIRE;

        int burnChance = JSONUtils.getInt(jo, "burnChance", -1);
        int spreadChance = JSONUtils.getInt(jo, "spreadChance", -1);
        if (burnChance != -1 && spreadChance != -1) {
            //spreadChance and burnChance are the wrong way around in yarn
            fireBlock.setFireInfo(resB, spreadChance, burnChance);
        }


        return new Pair<>(resB, resI);
    }

    private Material getSettings(MaterialSettingsPojo mspj) {
        return new Material(
                MaterialColor.PINK,
                mspj.liquid,
                mspj.solid,
                mspj.blocksMovement,
                mspj.blocksLight,
                mspj.breakByHand,
                mspj.burnable,
                mspj.replaceable,
                getPistonBehavior(mspj.pistonBehavior));

    }

    private PushReaction getPistonBehavior(String pistonBehavior) {
        switch (pistonBehavior.toUpperCase()) {
            case "NORMAL":
                return PushReaction.NORMAL;
            case "DESTROY":
                return PushReaction.DESTROY;
            case "BLOCK":
                return PushReaction.BLOCK;
            case "IGNORE":
                return PushReaction.IGNORE;
            case "PUSH_ONLY":
                return PushReaction.PUSH_ONLY;
            default:
                log(Level.WARN, "Piston Behavior " + pistonBehavior + " not found, using normal");
                return PushReaction.NORMAL;

        }
    }

    private MaterialColor getMaterialColor(String color) {
        switch (color.toUpperCase()) {
            case "AIR":
                return MaterialColor.AIR;
            case "GRASS":
                return MaterialColor.GRASS;
            case "SAND":
                return MaterialColor.SAND;
            case "WEB":
                return MaterialColor.WOOL;
            case "LAVA":
                return MaterialColor.TNT;
            case "ICE":
                return MaterialColor.ICE;
            case "IRON":
                return MaterialColor.IRON;
            case "FOLIAGE":
                return MaterialColor.FOLIAGE;
            case "WHITE":
                return MaterialColor.SNOW;
            case "CLAY":
                return MaterialColor.CLAY;
            case "DIRT":
                return MaterialColor.DIRT;
            case "STONE":
                return MaterialColor.STONE;
            case "WATER":
                return MaterialColor.WATER;
            case "WOOD":
                return MaterialColor.WOOD;
            case "QUARTZ":
                return MaterialColor.QUARTZ;
            case "ORANGE":
                return MaterialColor.ADOBE;
            case "MAGENTA":
                return MaterialColor.MAGENTA;
            case "LIGHT_BLUE":
                return MaterialColor.LIGHT_BLUE;
            case "YELLOW":
                return MaterialColor.YELLOW;
            case "LIME":
                return MaterialColor.LIME;
            case "PINK":
                return MaterialColor.PINK;
            case "GRAY":
                return MaterialColor.GRAY;
            case "LIGHT_GRAY":
                return MaterialColor.LIGHT_GRAY;
            case "CYAN":
                return MaterialColor.CYAN;
            case "PURPLE":
                return MaterialColor.PURPLE;
            case "BLUE":
                return MaterialColor.BLUE;
            case "BROWN":
                return MaterialColor.BROWN;
            case "GREEN":
                return MaterialColor.GREEN;
            case "RED":
                return MaterialColor.RED;
            case "BLACK":
                return MaterialColor.BLACK;
            case "GOLD":
                return MaterialColor.GOLD;
            case "DIAMOND":
                return MaterialColor.DIAMOND;
            case "LAPIS":
                return MaterialColor.LAPIS;
            case "EMERALD":
                return MaterialColor.EMERALD;
            case "SPRUCE":
                return MaterialColor.OBSIDIAN;
            case "NETHER":
                return MaterialColor.NETHERRACK;
            case "WHITE_TERRACOTTA":
                return MaterialColor.WHITE_TERRACOTTA;
            case "ORANGE_TERRACOTTA":
                return MaterialColor.ORANGE_TERRACOTTA;
            case "MAGENTA_TERRACOTTA":
                return MaterialColor.MAGENTA_TERRACOTTA;
            case "LIGHT_BLUE_TERRACOTTA":
                return MaterialColor.LIGHT_BLUE_TERRACOTTA;
            case "YELLOW_TERRACOTTA":
                return MaterialColor.YELLOW_TERRACOTTA;
            case "LIME_TERRACOTTA":
                return MaterialColor.LIME_TERRACOTTA;
            case "PINK_TERRACOTTA":
                return MaterialColor.PINK_TERRACOTTA;
            case "GRAY_TERRACOTTA":
                return MaterialColor.GRAY_TERRACOTTA;
            case "LIGHT_GRAY_TERRACOTTA":
                return MaterialColor.LIGHT_GRAY_TERRACOTTA;
            case "CYAN_TERRACOTTA":
                return MaterialColor.CYAN_TERRACOTTA;
            case "PURPLE_TERRACOTTA":
                return MaterialColor.PURPLE_TERRACOTTA;
            case "BLUE_TERRACOTTA":
                return MaterialColor.BLUE_TERRACOTTA;
            case "BROWN_TERRACOTTA":
                return MaterialColor.BROWN_TERRACOTTA;
            case "GREEN_TERRACOTTA":
                return MaterialColor.GREEN_TERRACOTTA;
            case "RED_TERRACOTTA":
                return MaterialColor.RED_TERRACOTTA;
            case "BLACK_TERRACOTTA":
                return MaterialColor.BLACK_TERRACOTTA;
            default:
                log(Level.WARN, "MapColor " + color + " not found, using pink");
                return MaterialColor.PINK;
        }
    }

    private Block.Properties getSettings(Material material, BlockSettingsPojo bspj) {
        Block.Properties bs = Block.Properties.create(material, material.getColor());
        Field[] fields = Block.Properties.class.getDeclaredFields();
        try {
            fields[0].setAccessible(true);
            fields[0].set(bs, material);
            fields[1].setAccessible(true);
            fields[1].set(bs, getMaterialColor(bspj.mapColor));
            fields[2].setAccessible(true);
            fields[2].setBoolean(bs, bspj.collidable);
            fields[3].setAccessible(true);
            fields[3].set(bs, getSoundGroup(bspj.soundGroup));
            fields[4].setAccessible(true);
            fields[4].setInt(bs, bspj.lightLevel);
            fields[5].setAccessible(true);
            fields[5].setFloat(bs, bspj.explosionResistance);
            fields[6].setAccessible(true);
            fields[6].setFloat(bs, bspj.hardness);
            fields[8].setAccessible(true);
            fields[8].setFloat(bs, bspj.slipperiness);
            fields[9].setAccessible(true);
            fields[9].setFloat(bs, bspj.slowDownMultiplier);
            fields[10].setAccessible(true);
            fields[10].setFloat(bs, bspj.jumpVelocityMultiplier);
            fields[11].setAccessible(true);
            fields[11].set(bs, getDropTableId(bspj.dropTableId));
            fields[12].setAccessible(true);
            fields[12].setBoolean(bs, bspj.opaque);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return bs;
    }

    private ResourceLocation getDropTableId(String s) {
        if (s == null)
            return null;
        ResourceLocation i = ResourceLocation.tryCreate(s);
        if (i == null) {
            log(Level.WARN, "Drop table invalid " + s + ", using default");
            i = null;
        }
        return i;
    }

    private SoundType getSoundGroup(String s) {
        switch (s.toUpperCase()) {
            case "WOOD":
                return SoundType.WOOD;
            case "GRAVEL":
                return SoundType.GROUND;
            case "GRASS":
                return SoundType.PLANT;
            case "STONE":
                return SoundType.STONE;
            case "METAL":
                return SoundType.METAL;
            case "GLASS":
                return SoundType.GLASS;
            case "WOOL":
                return SoundType.CLOTH;
            case "SAND":
                return SoundType.SAND;
            case "SNOW":
                return SoundType.SNOW;
            case "LADDER":
                return SoundType.LADDER;
            case "ANVIL":
                return SoundType.ANVIL;
            case "SLIME":
                return SoundType.SLIME;
            case "HONEY":
                return SoundType.field_226947_m_;
            case "WET_GRASS":
                return SoundType.WET_GRASS;
            case "CORAL":
                return SoundType.CORAL;
            case "BAMBOO":
                return SoundType.BAMBOO;
            case "BAMBOO_SAPLING":
                return SoundType.BAMBOO_SAPLING;
            case "SCAFFOLDING":
                return SoundType.SCAFFOLDING;
            case "SWEET_BERRY_BUSH":
                return SoundType.SWEET_BERRY_BUSH;
            case "CROP":
                return SoundType.CROP;
            case "STEM":
                return SoundType.STEM;
            case "NETHER_WART":
                return SoundType.NETHER_WART;
            case "LANTERN":
                return SoundType.LANTERN;
            default:
                log(Level.WARN, "Sound group " + s + " not found, using stone");
                return SoundType.STONE;
        }
    }

    private Material getMaterial(String s) {
        switch (s.toUpperCase()) {
            case "AIR":
                return Material.AIR;
            case "STRUCTURE_VOID":
                return Material.STRUCTURE_VOID;
            case "PORTAL":
                return Material.PORTAL;
            case "CARPET":
                return Material.CARPET;
            case "PLANT":
                return Material.PLANTS;
            case "UNDERWATER_PLANT":
                return Material.OCEAN_PLANT;
            case "REPLACEABLE_PLANT":
                return Material.TALL_PLANTS;
            case "SEAGRASS":
                return Material.SEA_GRASS;
            case "WATER":
                return Material.WATER;
            case "BUBBLE_COLUMN":
                return Material.BUBBLE_COLUMN;
            case "LAVA":
                return Material.LAVA;
            case "SNOW":
                return Material.SNOW;
            case "FIRE":
                return Material.FIRE;
            case "PART":
                return Material.MISCELLANEOUS;
            case "COBWEB":
                return Material.WEB;
            case "REDSTONE_LAMP":
                return Material.REDSTONE_LIGHT;
            case "CLAY":
                return Material.CLAY;
            case "EARTH":
                return Material.EARTH;
            case "ORGANIC":
                return Material.ORGANIC;
            case "PACKED_ICE":
                return Material.PACKED_ICE;
            case "SAND":
                return Material.SAND;
            case "SPONGE":
                return Material.SPONGE;
            case "SHULKER_BOX":
                return Material.SHULKER;
            case "WOOD":
                return Material.WOOD;
            case "BAMBOO_SAPLING":
                return Material.BAMBOO_SAPLING;
            case "BAMBOO":
                return Material.BAMBOO;
            case "WOOL":
                return Material.WOOL;
            case "TNT":
                return Material.TNT;
            case "LEAVES":
                return Material.LEAVES;
            case "GLASS":
                return Material.GLASS;
            case "ICE":
                return Material.ICE;
            case "CACTUS":
                return Material.CACTUS;
            case "STONE":
                return Material.ROCK;
            case "METAL":
                return Material.IRON;
            case "SNOW_BLOCK":
                return Material.SNOW_BLOCK;
            case "ANVIL":
                return Material.ANVIL;
            case "BARRIER":
                return Material.BARRIER;
            case "PISTON":
                return Material.PISTON;
            case "UNUSED_PLANT":
                return Material.CORAL;
            case "PUMPKIN":
                return Material.GOURD;
            case "EGG":
                return Material.DRAGON_EGG;
            case "CAKE":
                return Material.CAKE;
            default:
                log(Level.WARN, "Material " + s + " not found, using stone");
                return Material.ROCK;

        }
    }

    public void forgeRegisterItems(RegistryEvent.Register<Item> itemRegistryEvent) {
        IForgeRegistry<Item> reg = itemRegistryEvent.getRegistry();
        for (Map.Entry<ResourceLocation, Item> i : blockItems.entrySet()) {
            i.getValue().setRegistryName(i.getKey());
            reg.register(i.getValue());
        }
    }

    public void forgeRegisterBlocks(RegistryEvent.Register<Block> itemRegistryEvent) {
        IForgeRegistry<Block> reg = itemRegistryEvent.getRegistry();
        for (Map.Entry<ResourceLocation, Block> i : blocks.entrySet()) {
            i.getValue().setRegistryName(i.getKey());
            reg.register(i.getValue());
        }
    }
}
