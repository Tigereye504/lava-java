package net.tigereye.lavajava.register;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.tigereye.lavajava.mob.WitherBaristaEntity;

public class LJEntities {
    public static final EntityType<WitherBaristaEntity> WITHER_BARISTA = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("lavajava", "wither_barista"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WitherBaristaEntity::new).fireImmune().dimensions(EntityDimensions.fixed(WitherBaristaEntity.WIDTH, WitherBaristaEntity.HEIGHT)).build()
    );

    public static void register(){
        FabricDefaultAttributeRegistry.register(WITHER_BARISTA, WitherBaristaEntity.createWitherBaristaAttributes());
    }
}
