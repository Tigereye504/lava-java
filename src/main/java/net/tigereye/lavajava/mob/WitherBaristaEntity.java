package net.tigereye.lavajava.mob;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.*;
import net.minecraft.world.World;
import net.tigereye.lavajava.LavaJava;
import net.tigereye.lavajava.flavor.*;
import net.tigereye.lavajava.item.LavaJavaItem;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

//TODO: prevent the wither baristas from despawning from distance/time or peaceful mode

public class WitherBaristaEntity extends WitherSkeletonEntity implements Merchant {
    public static final float HEIGHT =2.4f;
    public static final float WIDTH = 0.7f;
    private static final int[] LEVEL_BASE_EXPERIENCE = new int[]{0, 25, 70, 150, 250};
    private static final int MAX_LEVEL = 5;
    private static final int SHOP_REFRESH_PERIOD = 3200;

    @Nullable
    private PlayerEntity customer;
    @Nullable
    protected TradeOfferList offers;

    private int experience = 0;
    private int level = 1;
    private long lastShopRefresh = 0;

    public WitherBaristaEntity(EntityType<? extends WitherSkeletonEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createWitherBaristaAttributes() {
        return HostileEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3499999940395355D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new AvoidSunlightGoal(this));
        this.goalSelector.add(3, new EscapeSunlightGoal(this, 1.0D));
        this.goalSelector.add(3, new FleeEntityGoal(this, WolfEntity.class, 6.0F, 1.0D, 1.2D));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
    }

    @Override
    public void setCurrentCustomer(@Nullable PlayerEntity customer) {
        this.customer = customer;
    }

    @Nullable
    @Override
    public PlayerEntity getCurrentCustomer() {
        return this.customer;
    }

    public boolean hasCustomer() {
        return customer != null;
    }

    //TODO: make custom factory for wither barista
    protected void fillRecipes() {
        TradeOffers.Factory[] factorys = WITHER_BARISTA_TRADES.get(1);
        int i = 1;
        if (this.offers == null) {
            this.offers = new TradeOfferList();
        }
        offers.clear();
        if(level < 1){level = 1;}
        while(i <= level && factorys != null){
            TradeOffer tradeOffer = factorys[0].create(this, this.random);
            if (tradeOffer != null) {offers.add(tradeOffer);}
            tradeOffer = factorys[1].create(this, this.random);
            if (tradeOffer != null) {offers.add(tradeOffer);}
            i++;
            factorys = WITHER_BARISTA_TRADES.get(i);
        }
        this.lastShopRefresh = world.getTime();
    }

    protected void fillRecipesFromPool(TradeOfferList recipeList, TradeOffers.Factory[] pool, int count) {
        Set<Integer> set = Sets.newHashSet();
        if (pool.length > count) {
            while(set.size() < count) {
                set.add(this.random.nextInt(pool.length));
            }
        } else {
            for(int i = 0; i < pool.length; ++i) {
                set.add(i);
            }
        }

        Iterator var9 = set.iterator();

        while(var9.hasNext()) {
            Integer integer = (Integer)var9.next();
            TradeOffers.Factory factory = pool[integer];
            TradeOffer tradeOffer = factory.create(this, this.random);
            if (tradeOffer != null) {
                recipeList.add(tradeOffer);
            }
        }

    }

    @Override
    public TradeOfferList getOffers() {
        if (this.offers == null) {
            this.offers = new TradeOfferList();
            this.fillRecipes();
        }

        return this.offers;
    }

    @Override
    public void setOffersFromServer(TradeOfferList offers) {
        this.offers = offers;
    }

    public void trade(TradeOffer offer) {
        offer.use();
        this.ambientSoundChance = -this.getMinAmbientSoundDelay();
        this.afterUsing(offer);
        //if (this.customer instanceof ServerPlayerEntity) {
        //    Criteria.VILLAGER_TRADE.trigger((ServerPlayerEntity)this.customer, this, offer.getSellItem());
        //}
    }

    protected void afterUsing(TradeOffer offer) {
        if (offer.shouldRewardPlayerExperience()) {
            int i = 3 + this.random.nextInt(4);
            this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.getX(), this.getY() + 0.5D, this.getZ(), i));
        }

    }

    @Override
    public void onSellingItem(ItemStack stack) {
        if (!this.world.isClient && this.ambientSoundChance > -this.getMinAmbientSoundDelay() + 20) {
            this.ambientSoundChance = -this.getMinAmbientSoundDelay();
            this.playSound(this.getTradingSound(!stack.isEmpty()), this.getSoundVolume(), this.getSoundPitch());
        }

    }

    public int getLevel() { return this.level; }

    public int getExperience() {
        return this.experience;
    }

    public void setExperienceFromServer(int experience) {
        this.experience = experience;
    }

    @Override
    public boolean isLeveledMerchant() {
        return true;
    }

    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (!itemStack.isOf(Items.WITHER_SKELETON_SPAWN_EGG) && this.isAlive() && !this.hasCustomer() && !this.isBaby()) {
            if (hand == Hand.MAIN_HAND) {
                player.incrementStat(Stats.TALKED_TO_VILLAGER);
            }

            if (this.getOffers().isEmpty()) {
                return ActionResult.success(this.world.isClient);
            } else {
                if (!this.world.isClient) {
                    this.setCurrentCustomer(player);
                    this.sendOffers(player, this.getDisplayName(), this.getLevel());
                }

                return ActionResult.success(this.world.isClient);
            }
        } else {
            return super.interactMob(player, hand);
        }
    }

    @Override
    protected void mobTick() {
        if (!this.hasCustomer() && world.getTime() - this.lastShopRefresh > SHOP_REFRESH_PERIOD) {
            if(level <= MAX_LEVEL && experience > LEVEL_BASE_EXPERIENCE[level]){
                experience = 0;
                level++;
            }
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 0));
            fillRecipes();
        }


        super.mobTick();
    }

    protected SoundEvent getTradingSound(boolean sold) {
        return sold ? SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT : SoundEvents.ENTITY_WITHER_SKELETON_HURT;
    }

    @Override
    public SoundEvent getYesSound() {
        return SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT;
    }

    @Override
    public boolean isClient() {
        return this.world.isClient();
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        TradeOfferList tradeOfferList = this.getOffers();
        if (!tradeOfferList.isEmpty()) {
            nbt.put("Offers", tradeOfferList.toNbt());
        }
        nbt.putInt("tradeXP",this.experience);
        nbt.putInt("tradeLevel",this.level);
        nbt.putInt("lastShopRefresh",this.level);
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Offers", 10)) {
            this.offers = new TradeOfferList(nbt.getCompound("Offers"));
        }
        this.experience = nbt.getInt("tradeXP");
        this.level = nbt.getInt("tradeLevel");
    }



    public static final Int2ObjectMap<TradeOffers.Factory[]> WITHER_BARISTA_TRADES;
    private static Int2ObjectMap<TradeOffers.Factory[]> copyToFastUtilMap(ImmutableMap<Integer, TradeOffers.Factory[]> map) {
        return new Int2ObjectOpenHashMap(map);
    }
    static {
        WITHER_BARISTA_TRADES = copyToFastUtilMap(ImmutableMap.of(
                1, new TradeOffers.Factory[]{
                        new WitherBaristaEntity.SellItemFactory(Items.COOKIE, 2, 1, 32, 2),
                        new WitherBaristaEntity.SellLavaJavaFactory(0)},
                2, new TradeOffers.Factory[]{
                        new WitherBaristaEntity.SellLavaJavaFactory(1),
                        new WitherBaristaEntity.SellLavaJavaFactory(1)},
                3, new TradeOffers.Factory[]{
                        new WitherBaristaEntity.SellItemFactory(Items.PUMPKIN_PIE, 8, 1, 8, 10),
                        new WitherBaristaEntity.SellLavaJavaFactory(2)},
                4, new TradeOffers.Factory[]{
                        new WitherBaristaEntity.SellItemFactory(Blocks.CAKE, 16, 1, 2, 15),
                        new WitherBaristaEntity.SellLavaJavaFactory(3)},
                5, new TradeOffers.Factory[]{
                        new WitherBaristaEntity.SellLavaJavaFactory(4),
                        new WitherBaristaEntity.SellLavaJavaFactory(4)}
        ));

    }

    static class SellItemFactory implements TradeOffers.Factory {
        private final ItemStack sell;
        private final int price;
        private final int count;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public SellItemFactory(Block block, int price, int count, int maxUses, int experience) {
            this(new ItemStack(block), price, count, maxUses, experience);
        }

        public SellItemFactory(Item item, int price, int count, int experience) {
            this((ItemStack)(new ItemStack(item)), price, count, 12, experience);
        }

        public SellItemFactory(Item item, int price, int count, int maxUses, int experience) {
            this(new ItemStack(item), price, count, maxUses, experience);
        }

        public SellItemFactory(ItemStack stack, int price, int count, int maxUses, int experience) {
            this(stack, price, count, maxUses, experience, 0.05F);
        }

        public SellItemFactory(ItemStack stack, int price, int count, int maxUses, int experience, float multiplier) {
            this.sell = stack;
            this.price = price;
            this.count = count;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(new ItemStack(Items.GOLD_NUGGET, this.price), new ItemStack(this.sell.getItem(), this.count), this.maxUses, this.experience, this.multiplier);
        }
    }

    private static class SellLavaJavaFactory implements TradeOffers.Factory {

        private static float[] SECOND_FLAVOR_CHANCE = new float[] {0,.5f,.8f,1,1};
        private static float[] THIRD_FLAVOR_CHANCE = new float[] {0,0,.2f,.6f,1};
        int tier;

        public SellLavaJavaFactory(int tier){this.tier = tier;}

        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack = LavaJava.LAVA_JAVA.getDefaultStack().copy();
            int rolls = 1;
            if(random.nextFloat() < SECOND_FLAVOR_CHANCE[tier]) rolls++;
            if(random.nextFloat() < THIRD_FLAVOR_CHANCE[tier]) rolls++;
            Map<Identifier, FlavorData> flavors = new HashMap<>();
            for (int i = 0; i < rolls; i++) {
                Pair<Identifier, FlavorData> flavor = FlavorManager.getWeightedRandomFlavor(random);
                if(flavor != null){
                    flavors.put(flavor.getLeft(),flavor.getRight());
                }
            }

            int basePrice = 5;
            int positiveCost = 0;
            int positiveCount = 0;
            int negativeDiscount = 0;
            int negativeCount = 0;
            for(Map.Entry<Identifier,FlavorData> flavor : flavors.entrySet()){
                if(flavor.getValue().isDrawback){
                    negativeDiscount += flavor.getValue().value;
                    negativeCount++;
                }
                else {
                    positiveCost += flavor.getValue().value;
                    positiveCount++;
                }
                LavaJavaItem.addFlavor(itemStack, flavor.getKey());
            }
            int nuggets = (int)(basePrice + (positiveCost*(1+positiveCount)/2f) - (negativeDiscount*negativeCount));
            int ingots = 0;
            if(nuggets > 64){
                ingots = Math.min(nuggets / 9, 64);
                nuggets = nuggets % 9;
            }

            return new TradeOffer(new ItemStack(Items.GOLD_NUGGET, nuggets), new ItemStack(Items.GOLD_INGOT, ingots), itemStack, 2, 1+(rolls*rolls*2), 0.2F);
        }
    }
}
