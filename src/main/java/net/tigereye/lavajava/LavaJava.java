package net.tigereye.lavajava;

//import io.github.fablabsmc.fablabs.api.bannerpattern.v1.LoomPattern;
//import io.github.fablabsmc.fablabs.api.bannerpattern.v1.LoomPatterns;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.tigereye.lavajava.flavor.*;
import net.tigereye.lavajava.item.LavaJavaItem;
import net.tigereye.lavajava.mob.WitherBaristaEntity;
import net.tigereye.lavajava.register.LJConfiguredStructures;
import net.tigereye.lavajava.register.LJEntities;
import net.tigereye.lavajava.register.LJItems;
import net.tigereye.lavajava.register.LJStructures;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LavaJava implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "lavajava";
	public static final Logger LOGGER = LogManager.getLogger(MODID);



	@Override
	public void onInitialize() {
		LOGGER.info("Brewing Lava Java.");
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new FlavorManager());
		LJItems.register();
		LJEntities.register();
		LJStructures.setupAndRegisterStructureFeatures();
		LJConfiguredStructures.registerConfiguredStructures();
		addStructureSpawningToDimensionsAndBiomes();
		//TODO: once Banners++ updates to 1.18, unlock glorious banners
		//Registry.register(LoomPatterns.REGISTRY, new Identifier("lavajava", "lava_java_banner"), new LoomPattern(false));
	}


	private void addStructureSpawningToDimensionsAndBiomes(){
		/*
		 * This is the API you will use to add anything to any biome.
		 * This includes spawns, changing the biome's looks, messing with its temperature,
		 * adding carvers, spawning new features... etc
		 */
		BiomeModifications.addStructure(
				BiomeSelectors.categories(Biome.Category.NETHER),
				RegistryKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY,
						BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.getId(LJConfiguredStructures.CONFIGURED_LAVA_JAVA_CAFE))

		);

	}

}
