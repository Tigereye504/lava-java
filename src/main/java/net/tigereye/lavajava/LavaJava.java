package net.tigereye.lavajava;

//import io.github.fablabsmc.fablabs.api.bannerpattern.v1.LoomPattern;
//import io.github.fablabsmc.fablabs.api.bannerpattern.v1.LoomPatterns;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.tigereye.lavajava.config.LJConfig;
import net.tigereye.lavajava.flavor.*;
import net.tigereye.lavajava.register.LJEntities;
import net.tigereye.lavajava.register.LJItems;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LavaJava implements ModInitializer {
	public static final String MODID = "lavajava";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static LJConfig config;



	@Override
	public void onInitialize() {
		LOGGER.info("Brewing Lava Java.");
		AutoConfig.register(LJConfig.class, GsonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(LJConfig.class).getConfig();

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new FlavorManager());
		LJItems.register();
		LJEntities.register();
		//addStructureSpawningToDimensionsAndBiomes();
		//TODO: once Banners++ updates to 1.18, unlock glorious banners
		//Registry.register(LoomPatterns.REGISTRY, new Identifier("lavajava", "lava_java_banner"), new LoomPattern(false));
	}

	/*
	private void addStructureSpawningToDimensionsAndBiomes(){
		BiomeModifications.addStructure(
				BiomeSelectors.categories(Biome.Category.NETHER),
				RegistryKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY,
						BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.getId(LJConfiguredStructures.CONFIGURED_LAVA_JAVA_CAFE))
		);

	}
	*/
}
