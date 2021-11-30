package net.tigereye.lavajava;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.tigereye.lavajava.mob.WitherBaristaEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LavaJava implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("lavajava");

	public static final EntityType<WitherBaristaEntity> WITHER_BARISTA = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier("lavajava", "wither_barista"),
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WitherBaristaEntity::new).dimensions(EntityDimensions.fixed(WitherBaristaEntity.WIDTH, WitherBaristaEntity.HEIGHT)).build()
	);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Coffee!");
		FabricDefaultAttributeRegistry.register(WITHER_BARISTA, WitherBaristaEntity.createWitherBaristaAttributes());

	}
}
