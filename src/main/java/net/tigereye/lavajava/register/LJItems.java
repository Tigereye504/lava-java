package net.tigereye.lavajava.register;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;
import net.tigereye.lavajava.LavaJava;
import net.tigereye.lavajava.item.LavaJavaItem;

public class LJItems {

    public static final FoodComponent LAVA_JAVA_FOOD_COMPONENT = new FoodComponent.Builder().hunger(4).saturationModifier(.2f).build();
    public static final LavaJavaItem LAVA_JAVA = new LavaJavaItem(new Item.Settings().maxCount(1).group(ItemGroup.FOOD).food(LAVA_JAVA_FOOD_COMPONENT));

    public static void register(){
        registerItem("lava_java", LAVA_JAVA);
    }

    private static void registerItem(String name, Item item) {
        Registry.register(Registry.ITEM, LavaJava.MODID + ":" + name, item);
    }
}
