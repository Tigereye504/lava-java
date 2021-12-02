package net.tigereye.lavajava.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.tigereye.lavajava.mob.WitherBaristaEntity;
import net.tigereye.lavajava.model.WitherBaristaModel;

public class WitherBaristaRenderer  extends MobEntityRenderer<WitherBaristaEntity, WitherBaristaModel<WitherBaristaEntity>> {

    public WitherBaristaRenderer(EntityRendererFactory.Context context) {
        super(context, new WitherBaristaModel(context.getPart(WitherBaristaModel.WITHER_BARISTA_MODEL_LAYER)), .5f);
    }

    @Override
    public Identifier getTexture(WitherBaristaEntity entity) {
        return new Identifier("lavajava", "textures/entity/wither_barista.png");
    }
}