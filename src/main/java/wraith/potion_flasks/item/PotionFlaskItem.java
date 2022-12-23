package wraith.potion_flasks.item;


import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import wraith.potion_flasks.registry.ItemRegistry;

import java.util.List;

import static net.minecraft.block.CauldronBlock.LEVEL;

public class PotionFlaskItem extends PotionItem {

    private final int capacity;

    public PotionFlaskItem(Item.Settings settings, int capacity) {
        super(settings);
        this.capacity = MathHelper.clamp(capacity, 1, 6);
    }

    public static Potion getPotionForCrafting(Inventory craftingInventory) {
        int potions = 0;
        Potion potionType = null;
        boolean foundFlask = false;
        for (int i = 0; i < craftingInventory.size(); ++i) {
            ItemStack craftingStack = craftingInventory.getStack(i);
            if (craftingStack.isEmpty()) {
                continue;
            }
            Item item = craftingStack.getItem();
            if (!(item instanceof PotionItem)) {
                if (item instanceof FlaskItem && !foundFlask) {
                    foundFlask = true;
                    continue;
                }
                break;
            }
            Potion potion = PotionUtil.getPotion(craftingStack);
            if (potionType == null) {
                potionType = potion;
            } else if (potionType != potion) {
                potions = 0;
                break;
            }

            ++potions;
        }
        return potions == 6 && foundFlask ? potionType : null;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        ItemStack itemStack = context.getStack();
        BlockPos pos = context.getBlockPos();
        Hand hand = context.getHand();
        PotionFlaskItem item = (PotionFlaskItem) itemStack.getItem();
        if (player == null) {
            return ActionResult.PASS;
        }
        BlockState state = world.getBlockState(context.getBlockPos());
        if (!state.isOf(Blocks.CAULDRON)) {
            return super.useOnBlock(context);
        }
        CauldronBlock block = (CauldronBlock) state.getBlock();

        if (PotionUtil.getPotion(context.getStack()) != Potions.WATER) {
            return super.useOnBlock(context);
        }
        int cauldronFilledAmount = state.get(LEVEL);
        if (cauldronFilledAmount < 3 && (getFillAmount(itemStack, capacity) == capacity || cauldronFilledAmount == 0 || player.isSneaking() || player.isCreative())  && !world.isClient) {
            if (!player.abilities.creativeMode) {
                player.incrementStat(Stats.USE_CAULDRON);

                item.tryDecreaseOrReplace(player, hand, itemStack);
            }

            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
            block.setLevel(world, pos, state, cauldronFilledAmount + 1);
            return ActionResult.SUCCESS;
        }
        if (cauldronFilledAmount > 0 && getFillAmount(itemStack, capacity) < item.getCapacity() && !world.isClient) {
            block.setLevel(world, pos, state, cauldronFilledAmount - 1);
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            tryIncreaseFilledAmount(itemStack);
            return ActionResult.SUCCESS;
        }
        return ActionResult.success(world.isClient);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        stack.getOrCreateTag().putInt("filled_amount", capacity);
        return stack;
    }

    public int getFillAmount(ItemStack stack, int fallback) {
        NbtCompound nbt = stack.getOrCreateTag();
        return nbt != null && nbt.contains("filled_amount") ? nbt.getInt("filled_amount") : fallback;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity) user : null;
        if (playerEntity instanceof ServerPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity) playerEntity, stack);
        }

        if (!world.isClient) {
            List<StatusEffectInstance> list = PotionUtil.getPotionEffects(stack);
            for (StatusEffectInstance statusEffectInstance : list) {
                if (statusEffectInstance.getEffectType().isInstant()) {
                    statusEffectInstance.getEffectType().applyInstantEffect(playerEntity, playerEntity, user, statusEffectInstance.getAmplifier(), 1.0D);
                } else {
                    user.addStatusEffect(new StatusEffectInstance(statusEffectInstance));
                }
            }
        }

        if (playerEntity != null) {
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!playerEntity.abilities.creativeMode) {
                decrementFilledAmount(stack);
            }
        }

        if (stack.isEmpty() && (playerEntity == null || !playerEntity.abilities.creativeMode)) {
            return new ItemStack(ItemRegistry.getFlask(this));
        }

        return stack;
    }

    public boolean decrementFilledAmount(ItemStack stack) {
        NbtCompound tag = stack.getOrCreateTag();
        int filledAmount = getFillAmount(stack, capacity) - 1;
        if (filledAmount <= 0) {
            stack.decrement(1);
            return false;
        } else {
            tag.putInt("filled_amount", filledAmount);
            return true;
        }
    }
    public boolean tryIncreaseFilledAmount(ItemStack stack) {
        return tryIncreaseFilledAmount(stack, 1);
    }

    public boolean tryIncreaseFilledAmount(ItemStack stack, int amount) {
        NbtCompound tag = stack.getOrCreateTag();
        int filledAmount = getFillAmount(stack, capacity);
        if (filledAmount >= capacity) {
            return false;
        } else if (filledAmount + amount > capacity) {
            return false;
        } else {
            tag.putInt("filled_amount", Math.min(filledAmount + amount, capacity));
            return true;
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (PotionUtil.getPotion(itemStack) != Potions.WATER) {
            return ItemUsage.consumeHeldItem(world, user, hand);
        }

        NbtCompound tag = itemStack.getOrCreateTag();
        int amount = getFillAmount(itemStack, 1);

        BlockHitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (amount >= capacity || hitResult.getType() == HitResult.Type.MISS || hitResult.getType() != HitResult.Type.BLOCK) {
            return amount >= 1 ? ItemUsage.consumeHeldItem(world, user, hand) : TypedActionResult.pass(itemStack);
        }
        BlockPos blockPos = hitResult.getBlockPos();
        if (!world.canPlayerModifyAt(user, blockPos)) {
            return ItemUsage.consumeHeldItem(world, user, hand);
        }

        if (world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
            world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            tag.putInt("filled_amount", amount + 1);
            return TypedActionResult.success(itemStack, world.isClient());
        }
        return ItemUsage.consumeHeldItem(world, user, hand);
    }


    public void tryDecreaseOrReplace(PlayerEntity player, Hand hand, ItemStack itemStack) {
        if (!decrementFilledAmount(itemStack)) {
            ItemStack flaskStack = new ItemStack(ItemRegistry.getFlask(this));
            player.setStackInHand(hand, flaskStack);
        }
        if (player instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) player).refreshScreenHandler(player.playerScreenHandler);
        }
    }
}
