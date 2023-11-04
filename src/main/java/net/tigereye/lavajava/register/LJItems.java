package net.tigereye.lavajava.register;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.tigereye.lavajava.LavaJava;
import net.tigereye.lavajava.item.LavaJavaItem;

public class LJItems {

    public static final FoodComponent LAVA_JAVA_FOOD_COMPONENT = new FoodComponent.Builder().hunger(4).saturationModifier(.2f).build();
    public static final LavaJavaItem LAVA_JAVA = new LavaJavaItem(new Item.Settings().maxCount(1).food(LAVA_JAVA_FOOD_COMPONENT));

    public static void register(){
        registerItem("lava_java", LAVA_JAVA);
        registerItemGroups();
    }

    private static void registerItem(String name, Item item) {
        Registry.register(Registries.ITEM, LavaJava.MODID + ":" + name, item);
    }

    private static void registerItemGroups(){
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(LAVA_JAVA);
        });
    }
}
