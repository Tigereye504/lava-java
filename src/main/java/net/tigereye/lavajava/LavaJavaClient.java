package net.tigereye.lavajava;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.tigereye.lavajava.mob.WitherBaristaEntity;
import net.tigereye.lavajava.model.WitherBaristaModel;
import net.tigereye.lavajava.render.WitherBaristaRenderer;

import java.rmi.registry.Registry;

@Environment(EnvType.CLIENT)
public class LavaJavaClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        /*
         * Registers our Cube Entity's renderer, which provides a model and texture for the entity.
         *
         * Entity Renderers can also manipulate the model before it renders based on entity context (EndermanEntityRenderer#render).
         */
        EntityRendererRegistry.INSTANCE.register(LavaJava.WITHER_BARISTA, WitherBaristaRenderer::new);
        // In 1.17, use EntityRendererRegistry.register (seen below) instead of EntityRendererRegistry.INSTANCE.register (seen above)
        //EntityRendererRegistry.register(WitherBaristaEntity.WITHER_BARISTA, (context) -> {
        //    return new WitherBaristaRenderer(context);
        //});

        EntityModelLayerRegistry.registerModelLayer(WitherBaristaModel.WITHER_BARISTA_MODEL_LAYER, WitherBaristaModel::getTexturedModelData);
    }
}
