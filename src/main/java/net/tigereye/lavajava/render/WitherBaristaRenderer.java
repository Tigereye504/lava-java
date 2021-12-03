package net.tigereye.lavajava.render;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.WitherSkeletonEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.util.Identifier;
import net.tigereye.lavajava.mob.WitherBaristaEntity;
import net.tigereye.lavajava.model.WitherBaristaModel;

public class WitherBaristaRenderer<T extends LivingEntity> extends BipedEntityRenderer<AbstractSkeletonEntity, WitherBaristaModel<WitherBaristaEntity>> {

    public WitherBaristaRenderer(EntityRendererFactory.Context context) {
        this(context, WitherBaristaModel.WITHER_BARISTA_MODEL_LAYER, EntityModelLayers.WITHER_SKELETON_INNER_ARMOR, EntityModelLayers.WITHER_SKELETON_OUTER_ARMOR);
    }

    public WitherBaristaRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer, EntityModelLayer legArmorLayer, EntityModelLayer bodyArmorLayer) {
        super(ctx, new WitherBaristaModel(ctx.getPart(layer)), 0.5F);
        this.addFeature(new ArmorFeatureRenderer(this, new SkeletonEntityModel(ctx.getPart(legArmorLayer)), new SkeletonEntityModel(ctx.getPart(bodyArmorLayer))));
    }

    @Override
    public Identifier getTexture(AbstractSkeletonEntity entity) {
        return new Identifier("lavajava", "textures/entity/wither_barista.png");
    }

    @Override
    protected void scale(AbstractSkeletonEntity entity , MatrixStack matrices, float amount) {
        matrices.scale(1.2F, 1.2F, 1.2F);
    }
}