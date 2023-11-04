package net.tigereye.lavajava;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;
import net.tigereye.lavajava.item.LavaJavaItem;
import net.tigereye.lavajava.model.WitherBaristaModel;
import net.tigereye.lavajava.register.LJEntities;
import net.tigereye.lavajava.register.LJItems;
import net.tigereye.lavajava.render.WitherBaristaRenderer;

@Environment(EnvType.CLIENT)
public class LavaJavaClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(LJEntities.WITHER_BARISTA, WitherBaristaRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(WitherBaristaModel.WITHER_BARISTA_MODEL_LAYER, WitherBaristaModel::getTexturedModelData);

        ModelPredicateProviderRegistry.register(LJItems.LAVA_JAVA, new Identifier("temperature"), (itemStack, clientWorld, livingEntity, something) -> {
            if (livingEntity == null || clientWorld == null){
                return .9F;
            }
            return LavaJavaItem.calculateTemperature(itemStack,clientWorld.getTime());
        });
    }
}
