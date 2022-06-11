package net.tigereye.lavajava.mob;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.*;
import net.tigereye.lavajava.LavaJava;
import net.tigereye.lavajava.flavor.*;
import net.tigereye.lavajava.item.LavaJavaItem;
import net.tigereye.lavajava.register.LJItems;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WitherBaristaEntity extends WitherSkeletonEntity implements Merchant {
    public static final float HEIGHT =2.4f;
    public static final float WIDTH = 0.7f;
    private static final int[] LEVEL_BASE_EXPERIENCE = new int[]{0, 10, 70, 150, 250};
    //private static final int[] LEVEL_BASE_EXPERIENCE = new int[]{0, 10, 60, 80, 100};
    private static final int MAX_LEVEL = 5;

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

    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        EntityData entityData2 = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
        this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(4.0D);
        this.updateAttackType();
        setPersistent();
        return entityData2;
    }

    public static DefaultAttributeContainer.Builder createWitherBaristaAttributes() {
        return HostileEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3499999940395355D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new AvoidSunlightGoal(this));
        this.goalSelector.add(3, new EscapeSunlightGoal(this, 1.0D));
        this.goalSelector.add(3, new FleeEntityGoal<>(this, WolfEntity.class, 6.0F, 1.0D, 1.2D));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty difficulty) {
    }

    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {

        Box exclusionZone = new Box(this.getX() - 32, this.getY() - 32, this.getZ() - 32, this.getX() + 32, this.getY() + 32, this.getZ() + 32);
        List<WitherBaristaEntity> entities = world.getEntitiesByClass(WitherBaristaEntity.class, exclusionZone, witherBaristaEntity -> true);
        return entities.size() < LavaJava.config.MAX_BARISTAS_PER_CAFE;
    }

    @Override
    public void setCustomer(@Nullable PlayerEntity customer) {
        this.customer = customer;
    }

    @Nullable
    @Override
    public PlayerEntity getCustomer() {
        return this.customer;
    }

    public boolean hasCustomer() {
        return customer != null;
    }


    protected void fillRecipes() {
        if(world.isClient()){
            return;
        }
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
            factorys = WITHER_BARISTA_TRADES.get(Math.min(i,MAX_LEVEL));
        }
        this.lastShopRefresh = world.getTime()/ LavaJava.config.CAFE_REFRESH_PERIOD;
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
                    this.setCustomer(player);
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
        if (!this.hasCustomer()) {
            int expToLevel = 0;
            if(level < MAX_LEVEL){
                expToLevel = LEVEL_BASE_EXPERIENCE[level];
            }
            else{
                expToLevel = LEVEL_BASE_EXPERIENCE[MAX_LEVEL-1] * (level-MAX_LEVEL+2);
            }
            if(level <= MAX_LEVEL && experience >= expToLevel){
                //experience = 0;
                level++;
                fillRecipes();
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 2));
            }
            if(world.getTime()/LavaJava.config.CAFE_REFRESH_PERIOD != this.lastShopRefresh) {
                fillRecipes();
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 2));
            }
        }


        super.mobTick();
    }

    @Override
    protected void updateDespawnCounter() {
        this.despawnCounter = 0;
    }

    @Override
    protected boolean isDisallowedInPeaceful() {
        return false;
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
        nbt.putLong("lastShopRefresh",this.lastShopRefresh);
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Offers", 10)) {
            this.offers = new TradeOfferList(nbt.getCompound("Offers"));
        }
        this.experience = nbt.getInt("tradeXP");
        this.level = nbt.getInt("tradeLevel");
        this.lastShopRefresh = nbt.getLong("lastShopRefresh");
    }



    public static final Int2ObjectMap<TradeOffers.Factory[]> WITHER_BARISTA_TRADES;
    private static Int2ObjectMap<TradeOffers.Factory[]> copyToFastUtilMap(ImmutableMap<Integer, TradeOffers.Factory[]> map) {
        return new Int2ObjectOpenHashMap(map);
    }
    static {
        WITHER_BARISTA_TRADES = copyToFastUtilMap(ImmutableMap.of(
                1, new TradeOffers.Factory[]{
                        new SellItemFactory(Items.COOKIE, 2, 1, 8, 1),
                        new SellLavaJavaFactory(0)},
                2, new TradeOffers.Factory[]{
                        new SellLavaJavaFactory(1),
                        new SellLavaJavaFactory(1)},
                3, new TradeOffers.Factory[]{
                        new SellItemFactory(Items.PUMPKIN_PIE, 8, 1, 3, 4),
                        new SellLavaJavaFactory(2)},
                4, new TradeOffers.Factory[]{
                        new SellItemFactory(Blocks.CAKE, 16, 1, 1, 8),
                        new SellLavaJavaFactory(3)},
                5, new TradeOffers.Factory[]{
                        new SellLavaJavaFactory(4),
                        new SellLavaJavaFactory(4)}
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

        private static final float[] SECOND_FLAVOR_CHANCE = new float[] {0,.5f,.8f,1,1};
        private static final float[] THIRD_FLAVOR_CHANCE = new float[] {0,0,.2f,.6f,1};
        int tier;

        public SellLavaJavaFactory(int tier){this.tier = tier;}

        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack = LJItems.LAVA_JAVA.getDefaultStack().copy();
            int rolls = 1;
            if(random.nextFloat() < SECOND_FLAVOR_CHANCE[tier]) rolls++;
            if(random.nextFloat() < THIRD_FLAVOR_CHANCE[tier]) rolls++;
            Map<Identifier, FlavorData> flavors = new HashMap<>();
            for (int i = 0; i < rolls; i++) {
                Pair<Identifier, FlavorData> flavor = FlavorManager.getWeightedRandomFlavor(random);
                if(flavor != null){
                    boolean badResult = flavors.containsKey(flavor.getLeft());
                    if(!badResult) {
                        //if any existing flavors are excluded by this flavor, it is no good
                        for (Identifier excludedFlavor :
                                flavor.getRight().exclusions) {
                            if (flavors.containsKey(excludedFlavor)) {
                                badResult = true;
                                break;
                            }
                        }
                    }
                    if(!badResult){
                        //if any existing flavors exclude this flavor, it is no good
                        for(Map.Entry<Identifier,FlavorData> existingFlavor : flavors.entrySet()){
                            if(existingFlavor.getValue().exclusions.contains(flavor.getLeft())){
                                badResult = true;
                            }
                        }
                    }
                    if(badResult) {
                        --i;
                    }
                    else{
                        flavors.put(flavor.getLeft(), flavor.getRight());
                    }
                }
            }

            int basePrice = 2;
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
            int nuggets = Math.max(1,basePrice + (int)(((positiveCost*(1+positiveCount)/2f) - (negativeDiscount*negativeCount)) / 4));
            int ingots = 0;
            if(nuggets > 64){
                ingots = Math.min(nuggets / 9, 64);
                nuggets = nuggets % 9;
            }

            return new TradeOffer(new ItemStack(Items.GOLD_NUGGET, nuggets), new ItemStack(Items.GOLD_INGOT, ingots), itemStack, 2, 1+(rolls*rolls*4), 0.2F);
        }
    }
}
