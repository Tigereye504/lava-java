package net.tigereye.lavajava.structure;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.PostPlacementProcessor;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.structure.StructurePiecesGenerator;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.tigereye.lavajava.LavaJava;
import net.tigereye.lavajava.register.LJEntities;
import org.apache.logging.log4j.Level;

import java.util.Optional;

public class LavaJavaCafeStructure extends StructureFeature<StructurePoolFeatureConfig>    {
    //when designing variants of this structure, use the following (with proper position and facing) to set up the loot barrels
    //setblock 30 -60 -37 minecraft:barrel[facing=east]{LootTable:"lavajava:chests/lava_java_cafe_barrel"} replace

    public LavaJavaCafeStructure(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, (context) -> {
                    // Check if the spot is valid for structure gen. If false, return nothing to signal to the game to skip this spawn attempt.
                    if (!LavaJavaCafeStructure.canGenerate(context)) {
                        return Optional.empty();
                    }
                    // Create the pieces layout of the structure and give it to
                    else {
                        return LavaJavaCafeStructure.createPiecesGenerator(context);
                    }
                },
                PostPlacementProcessor.EMPTY);
    }

    /**
     * These fields + NoiseChunkGeneratorMixin allows us to have mobs that spawn naturally over time in our structure.
     * No other mobs will spawn in the structure of the same entity classification.
     * The reason you want to match the classifications is so that your structure's mob
     * will contribute to that classification's cap. Otherwise, it may cause a runaway
     * spawning of the mob that will never stop.
     */
    public static final Pool<SpawnSettings.SpawnEntry> STRUCTURE_MONSTERS = Pool.of(
            new SpawnSettings.SpawnEntry(EntityType.ZOMBIFIED_PIGLIN, 90, 1, 3),
            new SpawnSettings.SpawnEntry(LJEntities.WITHER_BARISTA, 10,1,1),
            new SpawnSettings.SpawnEntry(EntityType.WITHER_SKELETON, 1,1,3)
    );

    public static final Pool<SpawnSettings.SpawnEntry> STRUCTURE_CREATURES = Pool.of(
            new SpawnSettings.SpawnEntry(EntityType.CAT, 5, 1, 1)
    );

    /*
     * This is where extra checks can be done to determine if the structure can spawn here.
     * This only needs to be overridden if you're adding additional spawn conditions.
     *
     * Fun fact, if you set your structure separation/spacing to be 0/1, you can use
     * canGenerate to return true only if certain chunk coordinates are passed in
     * which allows you to spawn structures only at certain coordinates in the world.
     *
     * Basically, this method is used for determining if the land is at a suitable height,
     * if certain other structures are too close or not, or some other restrictive condition.
     *
     * For example, Pillager Outposts added a check to make sure it cannot spawn within 10 chunk of a Village.
     * (Bedrock Edition seems to not have the same check)
     *
     *
     * Also, please for the love of god, do not do dimension checking here.
     * If you do and another mod's dimension is trying to spawn your structure,
     * the locate command will make minecraft hang forever and break the game.
     *
     * Instead, use the removeStructureSpawningFromSelectedDimension method in
     * StructureTutorialMain class. If you check for the dimension there and do not add your
     * structure's spacing into the chunk generator, the structure will not spawn in that dimension!
     */
    private static boolean canGenerate(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context) {
        BlockPos spawnXZPosition = context.chunkPos().getCenterAtY(0);


        // Grab height of land. Will stop at first non-air block.
        int landHeight = context.chunkGenerator().getHeightInGround(spawnXZPosition.getX(), spawnXZPosition.getZ(), Heightmap.Type.WORLD_SURFACE_WG, context.world());

        // Grabs column of blocks at given position. In overworld, this column will be made of stone, water, and air.
        // In nether, it will be netherrack, lava, and air. End will only be endstone and air. It depends on what block
        // the chunk generator will place for that dimension.
        VerticalBlockSample columnOfBlocks = context.chunkGenerator().getColumnSample(spawnXZPosition.getX(), spawnXZPosition.getZ(), context.world());
        //Cafes want to be as close to the lava lakes as they can be. Find the lowest platform that a Lava Java Cafe can be built upon
        BlockPos blockpos = context.chunkPos().getCenterAtY(31);
        for(int i = 31; i < context.world().getHeight();++i){
            if(columnOfBlocks.getState(i).isAir() && columnOfBlocks.getState(i-1).isOpaque()){
                blockpos = context.chunkPos().getCenterAtY(i-1);
                break;
            }
        }
        //Check that our chosen position is below the land height. If it is not, then we have violated the bedrock ceiling.
        if(blockpos.getY() >= landHeight) return false;


        // Combine the column of blocks with land height and you get the top block itself which you can test.
        // For the cafe, I'm instead grabbing the block at my chosen height
        BlockState chosenBlock = columnOfBlocks.getState(blockpos.getY());

        // Now we test to make sure our structure is not spawning on water or other fluids.
        // You can do height check instead too to make it spawn at high elevations.
        return chosenBlock.getFluidState().isEmpty() && columnOfBlocks.getState(blockpos.up().getY()).getFluidState().isEmpty(); //landHeight > 100;
    }

    public static Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> createPiecesGenerator(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context) {
        BlockPos blockpos = context.chunkPos().getCenterAtY(31);
        VerticalBlockSample blockView = context.chunkGenerator().getColumnSample(blockpos.getX(), blockpos.getZ(), context.world());

        //First, find the lowest platform that a Lava Java Cafe can be built upon
        for(int i = 1; i < context.world().getHeight();++i){
            if(blockView.getState(i).isAir() && blockView.getState(i-1).isOpaque()){
                blockpos = context.chunkPos().getCenterAtY(i-1);
                break;
            }
        }

        StructurePoolFeatureConfig newConfig = new StructurePoolFeatureConfig(
                () -> context.registryManager().get(Registry.STRUCTURE_POOL_KEY)
                        .get(new Identifier(LavaJava.MODID, "lava_java_cafe/start_pool")),
                // How many pieces outward from center can a recursive jigsaw structure spawn.
                // Our structure is only 1 piece outward and isn't recursive so any value of 1 or more doesn't change anything.
                10
        );

        // Create a new context with the new config that has our json pool. We will pass this into JigsawPlacement.addPieces
        StructureGeneratorFactory.Context<StructurePoolFeatureConfig> newContext = new StructureGeneratorFactory.Context<>(
                context.chunkGenerator(),
                context.biomeSource(),
                context.seed(),
                context.chunkPos(),
                newConfig,
                context.world(),
                context.validBiome(),
                context.structureManager(),
                context.registryManager()
        );

        Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> structurePiecesGenerator =
                StructurePoolBasedGenerator.generate(
                        newContext, // Used for StructurePoolBasedGenerator to get all the proper behaviors done.
                        PoolStructurePiece::new, // Needed in order to create a list of jigsaw pieces when making the structure's layout.
                        blockpos, // Position of the structure. Y value is ignored if last parameter is set to true.
                        false,  // Special boundary adjustments for villages. It's... hard to explain. Keep this false and make your pieces not be partially intersecting.
                        // Either not intersecting or fully contained will make children pieces spawn just fine. It's easier that way.
                        false // Place at heightmap (top land). Set this to false for structure to be place at the passed in blockpos's Y value instead.
                        // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
                );
        /*
         * Note, you are always free to make your own StructurePoolBasedGenerator class and implementation of how the structure
         * should generate. It is tricky but extremely powerful if you are doing something that vanilla's jigsaw system cannot do.
         * Such as for example, forcing 3 pieces to always spawn every time, limiting how often a piece spawns, or remove the intersection limitation of pieces.
         *
         * An example of a custom StructurePoolBasedGenerator.generate in action can be found here (warning, it is using Mojmap mappings):
         * https://github.com/TelepathicGrunt/RepurposedStructures-Fabric/blob/1.18/src/main/java/com/telepathicgrunt/repurposedstructures/world/structures/pieces/PieceLimitedJigsawManager.java
         */

        if(structurePiecesGenerator.isPresent()) {
            // I use to debug and quickly find out if the structure is spawning or not and where it is.
            // This is returning the coordinates of the center starting piece.
            LavaJava.LOGGER.log(Level.DEBUG, "Lava Java cafe at " + blockpos);
        }

        // Return the pieces generator that is now set up so that the game runs it when it needs to create the layout of structure pieces.
        return structurePiecesGenerator;
    }
}
