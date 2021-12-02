package net.tigereye.lavajava;

import net.fabricmc.api.ModInitializer;
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
import net.minecraft.util.registry.Registry;
import net.tigereye.lavajava.flavor.FlavorManager;
import net.tigereye.lavajava.item.LavaJavaItem;
import net.tigereye.lavajava.mob.WitherBaristaEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LavaJava implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "lavajava";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static final EntityType<WitherBaristaEntity> WITHER_BARISTA = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier("lavajava", "wither_barista"),
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WitherBaristaEntity::new).dimensions(EntityDimensions.fixed(WitherBaristaEntity.WIDTH, WitherBaristaEntity.HEIGHT)).build()
	);
	public static final FoodComponent LAVA_JAVA_FOOD_COMPONENT = new FoodComponent.Builder().hunger(0).saturationModifier(0).alwaysEdible().build();
	public static final LavaJavaItem LAVA_JAVA = new LavaJavaItem(new Item.Settings().maxCount(1).group(ItemGroup.FOOD).food(LAVA_JAVA_FOOD_COMPONENT));

	@Override
	public void onInitialize() {
		LOGGER.info("Brewing Lava Java.");
		FabricDefaultAttributeRegistry.register(WITHER_BARISTA, WitherBaristaEntity.createWitherBaristaAttributes());
		registerItem("lava_java", LAVA_JAVA);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new FlavorManager());

	}

	private static void registerItem(String name, Item item) {
		Registry.register(Registry.ITEM, LavaJava.MODID + ":" + name, item);
	}
}
