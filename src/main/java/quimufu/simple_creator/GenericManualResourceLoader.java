package quimufu.simple_creator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.*;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.packs.ModFileResourcePack;
import net.minecraftforge.fml.packs.ResourcePackLoader;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static quimufu.simple_creator.SimpleCreatorMod.log;

public abstract class GenericManualResourceLoader<T> {
    private Gson GSON;
    private String dataType;
    private SimpleCreatorConfig config;

    GenericManualResourceLoader(Gson gson, String dt) {
        GSON = gson;
        dataType = dt;
    }

    protected void loadItems(ArrayList<Pair<ResourceLocation, JsonObject>> itemJsonList) {
        log(Level.INFO, "Start loading " + dataType);
        for (Pair<ResourceLocation, JsonObject> e : itemJsonList) {
            ResourceLocation id = e.getFirst();
            log(Level.INFO, "Loading " + dataType.substring(0, dataType.length() - 1) + " " + id);
            T thing = deserialize(e);
            save(id, thing);
            log(Level.INFO, "Registering " + dataType.substring(0, dataType.length() - 1) + " " + id);
            register(id, thing);
        }
        log(Level.INFO, "Finished loading " + dataType);
    }

    protected abstract void register(ResourceLocation id, T thing);

    protected abstract T deserialize(Pair<ResourceLocation, JsonObject> e);

    protected abstract void save(ResourceLocation id, T item);

    public void load() {
        config = new SimpleCreatorConfig();
        ResourcePackList<ResourcePackInfo> resourcePackManager = new ResourcePackList<>(ResourcePackInfo::new);
        resourcePackManager.addPackFinder(new ServerPackFinder());
        resourcePackManager.addPackFinder(new FolderPackFinder(new File("./datapacks")));
        
        resourcePackManager.reloadPacksFromFinders();
        List<ResourcePackInfo> ep = Lists.newArrayList(resourcePackManager.getEnabledPacks());
        for (ResourcePackInfo rpp : resourcePackManager.getAllPacks()) {
            if (!ep.contains(rpp)) {
                rpp.getPriority().func_198993_a(ep, rpp, resourcePackProfile -> resourcePackProfile, false);
            }
        }

        
        ArrayList<Pair<ResourceLocation, JsonObject>> itemJsonList = new ArrayList<>();
        HashMap<ResourceLocation, JsonObject> itemJsonMap = Maps.newHashMap();
        resourcePackManager.setEnabledPacks(ep);
        if (config.enableTestThings){
            Optional<ModFileResourcePack> orp = ResourcePackLoader.getResourcePackFor(SimpleCreatorMod.MOD_ID);
            if(orp.isPresent()){
                ModFileResourcePack rp = orp.get();
            Collection<ResourceLocation> resources = rp.func_225637_a_(ResourcePackType.SERVER_DATA, SimpleCreatorMod.MOD_ID, dataType, 5, s -> s.endsWith(".json"));
                for (ResourceLocation id : resources) {
                    if (config.extendedLogging)
                        log(Level.INFO, "found: " + id.toString() + " in Pack: " + rp.getName());
                    ResourceLocation idNice = new ResourceLocation(id.getNamespace(), getName(id));
                    try {
                        InputStream is = rp.getResourceStream(ResourcePackType.SERVER_DATA, id);
                        Reader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                        JsonObject jo = JSONUtils.fromJson(GSON, r, JsonObject.class);
                        if (jo != null)
                            if (jo.entrySet().isEmpty()) {
                                itemJsonMap.remove(idNice);
                                if (config.extendedLogging)
                                    log(Level.INFO, "deleting " + idNice + " because of an empty override in " + rp.getName());
                            } else {
                                itemJsonMap.put(idNice, jo);
                                if (config.extendedLogging)
                                    log(Level.INFO, "adding " + idNice + " from " + rp.getName());
                            }
                    } catch (IOException e) {
                        log(Level.ERROR, "error loading " + id + " " + e.getMessage());
                    } catch (JsonParseException e) {
                        log(Level.ERROR, "error parsing json for " + id + " " + e.getMessage());
                    }
                }}
        }
        for (ResourcePackInfo rpp : resourcePackManager.getEnabledPacks()) {
            IResourcePack rp = rpp.getResourcePack();
            log(Level.INFO, "Loading ResourcePack " + rp.getName());
            for (String ns : rp.getResourceNamespaces(ResourcePackType.SERVER_DATA)) {
                log(Level.INFO, "Loading namespace " + ns);
                Collection<ResourceLocation> resources = rp.func_225637_a_(ResourcePackType.SERVER_DATA, ns, dataType, 5, s -> s.endsWith(".json"));
                for (ResourceLocation id : resources) {
                    if (config.extendedLogging)
                        log(Level.INFO, "found: " + id.toString() + " in Pack: " + rp.getName());
                    ResourceLocation idNice = new ResourceLocation(id.getNamespace(), getName(id));
                    try {
                        InputStream is = rp.getResourceStream(ResourcePackType.SERVER_DATA, id);
                        Reader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                        JsonObject jo = JSONUtils.fromJson(GSON, r, JsonObject.class);
                        if (jo != null)
                            if (jo.entrySet().isEmpty()) {
                                itemJsonMap.remove(idNice);
                                if (config.extendedLogging)
                                    log(Level.INFO, "deleting " + idNice + " because of an empty override in " + rp.getName());
                            } else {
                                itemJsonMap.put(idNice, jo);
                                if (config.extendedLogging)
                                    log(Level.INFO, "adding " + idNice + " from " + rp.getName());
                            }
                    } catch (IOException e) {
                        log(Level.ERROR, "error loading " + id + " " + e.getMessage());
                    } catch (JsonParseException e) {
                        log(Level.ERROR, "error parsing json for " + id + " " + e.getMessage());
                    }
                }
            }
        }
        for (Map.Entry<ResourceLocation, JsonObject> e : itemJsonMap.entrySet()) {
            itemJsonList.add(new Pair<>(e.getKey(), e.getValue()));
        }
        loadItems(itemJsonList);
    }

    private String getName(ResourceLocation id) {
        String path = id.getPath();
        int startLength = dataType.length() + 1;
        int endLength = ".json".length();
        return path.substring(startLength, path.length() - endLength);
    }
}
