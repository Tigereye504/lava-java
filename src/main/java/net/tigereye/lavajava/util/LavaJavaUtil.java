package net.tigereye.lavajava.util;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.registry.Registry;
import net.tigereye.lavajava.LavaJava;
import net.tigereye.lavajava.flavor.FlavorData;
import net.tigereye.lavajava.item.LavaJavaItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LavaJavaUtil {

    public static StatusEffectInstance convertFlavorToStatusEffect(FlavorData flavor){
        return convertFlavorToStatusEffect(flavor,1);
    }

    public static StatusEffectInstance convertFlavorToStatusEffect(FlavorData flavor, float durationFactor){

        Optional<StatusEffect> optional = Registry.STATUS_EFFECT.getOrEmpty(flavor.statusID);
        if(optional.isEmpty()){
            LavaJava.LOGGER.error("Lava Java flavor had invalid status effect "+flavor.statusID+"!");
            return null;
        }
        StatusEffect effect = optional.get();
        return new StatusEffectInstance(effect, (int)(Math.max(flavor.duration*durationFactor,1)), flavor.magnitude);
    }


}
