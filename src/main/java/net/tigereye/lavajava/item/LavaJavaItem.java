package net.tigereye.lavajava.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.tigereye.lavajava.LavaJava;
import net.tigereye.lavajava.flavor.*;
import net.tigereye.lavajava.util.LavaJavaUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LavaJavaItem extends Item {
    public static final float BOILING_TEMPERATURE = .85f;
    public static final float HOT_TEMPERATURE = .65f;
    public static final float WARM_TEMPERATURE = .35f;
    public static final float TEPID_TEMPERATURE = 0f;
    public LavaJavaItem(Item.Settings settings) {
        super(settings);
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity)user : null;
        if (playerEntity instanceof ServerPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity)playerEntity, stack);
        }

        if (!world.isClient) {
            List<FlavorData> flavors = new ArrayList<>();
            NbtCompound nbtCompound = stack.getOrCreateNbt();
            NbtCompound flavorNbt = nbtCompound.getCompound("Lava_Java_Flavors");
            float temperature = calculateDurationFactor(stack,world.getTime());
            for(String id : flavorNbt.getKeys()){
                FlavorData flavor = FlavorManager.getFlavor(new Identifier(id));
                if(flavor != null){
                    flavors.add(flavor);
                }
            }
            for (FlavorData flavor:
                 flavors) {

                StatusEffectInstance effect =  LavaJavaUtil.convertFlavorToStatusEffect(flavor,temperature);
                if(effect != null) {
                    user.addStatusEffect(effect);
                }
            }
        }

        if (playerEntity != null) {
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!playerEntity.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        world.emitGameEvent(user, GameEvent.DRINKING_FINISH, user.getCameraBlockPos());
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        if(!nbtCompound.contains("Lava_Java_Brew_Time")){
            nbtCompound.putLong("Lava_Java_Brew_Time",world.getTime());
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    public int getMaxUseTime(ItemStack stack) {
        return 32;
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    public String getTranslationKey(ItemStack stack) {
        //TODO: learn how to assemble a proper name
        return super.getTranslationKey();
    }

    public Text getName(ItemStack stack) {
        //TODO: read each flavor in the lava java and append it to the name, such as 'Filling Icy Minty Lava Java'
        //TODO: sort flavors by priority in name


        return new TranslatableText(this.getTranslationKey(stack));
    }

    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        //TODO: for now, flavors will be in the tooltip. This will change once I master TranslatableText nonsense.

        NbtCompound nbtCompound = stack.getOrCreateNbt();
        NbtCompound flavorNbt = nbtCompound.getCompound("Lava_Java_Flavors");
        for(String id : flavorNbt.getKeys()){
            String[] splitid = id.split(":");
            TranslatableText text = new TranslatableText("flavor." + splitid[0] + "." + splitid[1]);
            tooltip.add(text);
        }
        if(nbtCompound.contains("Lava_Java_Brew_Time")) {
            float temperature = calculateTemperature(stack, world.getTime());
            String temperatureText;
            if (temperature >= BOILING_TEMPERATURE) temperatureText = "***Boiling***";
            else if (temperature >= HOT_TEMPERATURE) temperatureText = "**Hot**";
            else if (temperature >= WARM_TEMPERATURE) temperatureText = "*Warm*";
            else if (temperature >= TEPID_TEMPERATURE) temperatureText = "Tepid";
            else temperatureText = "...stale";
            tooltip.add(new LiteralText(temperatureText));
        }
    }

    public boolean hasGlint(ItemStack stack) {
        return false;
    }

    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        //TODO: understand what this is doing
        if (this.isIn(group)) {
            Iterator var3 = Registry.POTION.iterator();

            while(var3.hasNext()) {
                Potion potion = (Potion)var3.next();
                if (potion != Potions.EMPTY) {
                    stacks.add(PotionUtil.setPotion(new ItemStack(this), potion));
                }
            }
        }

    }

    public static void addFlavor(ItemStack item, Identifier flavor){
        NbtCompound nbtCompound = item.getOrCreateNbt();
        NbtCompound flavorNbt = nbtCompound.getCompound("Lava_Java_Flavors");
        flavorNbt.putInt(flavor.toString(), 1);
        nbtCompound.put("Lava_Java_Flavors",flavorNbt);
    }

    public static float calculateTemperature(ItemStack item, long time){
        NbtCompound nbtCompound = item.getOrCreateNbt();
        long timeBrewed = nbtCompound.getLong("Lava_Java_Brew_Time");
        return 1 - (((float) (time - timeBrewed)) / LavaJava.config.TIME_UNTIL_STALE);
    }

    public static float calculateDurationFactor(ItemStack item, long time){
        float temperature = calculateTemperature(item, time);
        return calculateDurationFactor(temperature);
    }

    public static float calculateDurationFactor(float temperature){
        if(temperature > BOILING_TEMPERATURE){
            return 1;
        }
        else if(temperature > HOT_TEMPERATURE){
            return 1 - LavaJava.config.DURATION_LOST_PER_STAGE;
        }
        else if(temperature > WARM_TEMPERATURE){
            return 1 - LavaJava.config.DURATION_LOST_PER_STAGE * 2;
        }
        else if(temperature > TEPID_TEMPERATURE){
            return 1 - LavaJava.config.DURATION_LOST_PER_STAGE * 3;
        }
        else{
            return 1 - LavaJava.config.DURATION_LOST_PER_STAGE * 4;
        }
    }
}
