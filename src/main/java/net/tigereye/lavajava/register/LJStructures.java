package net.tigereye.lavajava.register;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.tigereye.lavajava.LavaJava;
import net.tigereye.lavajava.structure.LavaJavaCafeStructure;

public class LJStructures {

    /**
     /**
     * Registers the structure itself and sets what its path is. In this case, the
     * structure will have the Identifier of structure_tutorial:run_down_house.
     *
     * It is always a good idea to register your Structures so that other mods and datapacks can
     * use them too directly from the registries. It great for mod/datapacks compatibility.
     */
    public static StructureFeature<StructurePoolFeatureConfig> LAVA_JAVA_CAFE = new LavaJavaCafeStructure(StructurePoolFeatureConfig.CODEC);

    /**
     * This is where we use Fabric API's structure API to setup the StructureFeature
     * See the comments in below for more details.
     */
    public static void setupAndRegisterStructureFeatures() {

        // This is Fabric API's builder for structures.
        // It has many options to make sure your structure will spawn and work properly.
        // Give it your structure and the identifier you want for it.
        FabricStructureBuilder.create(new Identifier(LavaJava.MODID, "lava_java_cafe"), LAVA_JAVA_CAFE)
                .step(GenerationStep.Feature.SURFACE_STRUCTURES)
                .defaultConfig(new StructureConfig(
                        LavaJava.config.CAFE_AVERAGE_DISTANCE_CHUNKS, /* average distance apart in chunks between spawn attempts */
                        LavaJava.config.CAFE_MINIMUM_DISTANCE_CHUNKS, /* minimum distance apart in chunks between spawn attempts. MUST BE LESS THAN ABOVE VALUE */
                        225170915 /* this modifies the seed of the structure so no two structures always spawn over each-other. Make this large and unique. */))
                .adjustsSurface()
                .register();



        // Add more structures here and so on
    }


}