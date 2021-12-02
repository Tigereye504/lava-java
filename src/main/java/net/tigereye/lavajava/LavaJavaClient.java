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
        EntityRendererRegistry.INSTANCE.register(LavaJava.WITHER_BARISTA, WitherBaristaRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(WitherBaristaModel.WITHER_BARISTA_MODEL_LAYER, WitherBaristaModel::getTexturedModelData);
    }
}
