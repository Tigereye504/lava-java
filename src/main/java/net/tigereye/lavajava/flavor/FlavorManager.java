package net.tigereye.lavajava.flavor;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.Random;
import net.tigereye.lavajava.LavaJava;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FlavorManager implements SimpleSynchronousResourceReloadListener {

    private static final String RESOURCE_LOCATION = "lava_java_flavor";
    private final FlavorSerializer SERIALIZER = new FlavorSerializer();
    private static Map<Identifier, FlavorData> flavorReferenceMap = new HashMap<>();
    private static int totalWeight = 0;
    private static Map<Identifier, FlavorData> positiveFlavorReferenceMap = new HashMap<>();
    private static int positiveWeight = 0;
    private static Map<Identifier, FlavorData> negativeFlavorReferenceMap = new HashMap<>();
    private static int negativeWeight = 0;

    @Override
    public Identifier getFabricId() {
        return new Identifier(LavaJava.MODID, RESOURCE_LOCATION);
    }

    @Override
    public void reload(ResourceManager manager) {
        flavorReferenceMap.clear();
        totalWeight = 0;
        positiveFlavorReferenceMap.clear();
        positiveWeight = 0;
        negativeFlavorReferenceMap.clear();
        negativeWeight = 0;
        LavaJava.LOGGER.info("Loading Lava Java Flavors.");
        manager.findResources(RESOURCE_LOCATION, path -> path.getPath().endsWith(".json")).forEach((id,resource) -> {
            try(InputStream stream = resource.getInputStream()) {
                Reader reader = new InputStreamReader(stream);
                Pair<Identifier,FlavorData> flavorDataPair = SERIALIZER.read(id,new Gson().fromJson(reader,FlavorJsonFormat.class));
                if(flavorReferenceMap.containsKey(flavorDataPair.getLeft())){
                    LavaJava.LOGGER.error("Duplicate flavor " +flavorDataPair.getLeft()+ " found.");
                }
                else {
                    flavorReferenceMap.put(flavorDataPair.getLeft(), flavorDataPair.getRight());
                    totalWeight += flavorDataPair.getRight().weight;
                    if (flavorDataPair.getRight().isDrawback) {
                        negativeFlavorReferenceMap.put(flavorDataPair.getLeft(), flavorDataPair.getRight());
                        negativeWeight += flavorDataPair.getRight().weight;
                    } else {
                        positiveFlavorReferenceMap.put(flavorDataPair.getLeft(), flavorDataPair.getRight());
                        positiveWeight += flavorDataPair.getRight().weight;
                    }
                }
            } catch(Exception e) {
                LavaJava.LOGGER.error("Error occurred while loading resource json " + id.toString(), e);
            }
        });
        LavaJava.LOGGER.info("Loaded "+ flavorReferenceMap.size()+" Lava Java Flavors.");
    }

    @Nullable
    public static FlavorData getFlavor(Identifier flavor){
        return flavorReferenceMap.get(flavor);
    }

    @Nullable
    public static Pair<Identifier,FlavorData> getWeightedRandomFlavor (Random random) {
        int roll = random.nextInt(totalWeight);
        int remainingRoll = roll;
        Set<Identifier> flavors = flavorReferenceMap.keySet();
        for(Map.Entry<Identifier,FlavorData> flavor : flavorReferenceMap.entrySet()){
            remainingRoll -= flavor.getValue().weight;
            if(remainingRoll <= 0){
                return new Pair<>(flavor.getKey(),flavor.getValue());
            }
        }
        LavaJava.LOGGER.error("End of flavor reference map reached.");
        LavaJava.LOGGER.error("Total weight of flavors: " + totalWeight);
        LavaJava.LOGGER.error("Flavor entry rolled: " + roll);
        return null;
    }
}
