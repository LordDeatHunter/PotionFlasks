package wraith.potion_flasks.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
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
import org.lwjgl.system.MathUtil;
import wraith.potion_flasks.registry.ItemRegistry;

import static net.minecraft.block.CauldronBlock.LEVEL;

public class FlaskItem extends GlassBottleItem {

    public FlaskItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.MISS) {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = hitResult.getBlockPos();
                if (!world.canPlayerModifyAt(user, blockPos)) {
                    return TypedActionResult.pass(itemStack);
                }

                if (world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
                    world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                    ItemStack potionStack = PotionUtil.setPotion(new ItemStack(ItemRegistry.getPotionFlask(this)), Potions.WATER);
                    NbtCompound tag = potionStack.getOrCreateTag();
                    tag.putInt("filled_amount", 1);
                    return TypedActionResult.success(this.fill(itemStack, user, potionStack), world.isClient());
                }
            }

        }
        return TypedActionResult.pass(itemStack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        ItemStack itemStack = context.getStack();
        BlockPos pos = context.getBlockPos();
        Hand hand = context.getHand();
        FlaskItem item = (FlaskItem) itemStack.getItem();
        if (player == null) {
            return ActionResult.PASS;
        }
        BlockState state = world.getBlockState(context.getBlockPos());
        if (!state.isOf(Blocks.CAULDRON)) {
            return super.useOnBlock(context);
        }
        CauldronBlock block = (CauldronBlock) state.getBlock();
        int cauldronFilledAmount = state.get(LEVEL);
        if (cauldronFilledAmount > 0 && !world.isClient) {
            if (!player.abilities.creativeMode) {
                ItemStack potionStack = PotionUtil.setPotion(new ItemStack(ItemRegistry.getPotionFlask(item)), Potions.WATER);
                player.incrementStat(Stats.USE_CAULDRON);
                itemStack.decrement(1);
                NbtCompound tag = potionStack.getOrCreateTag();
                tag.putInt("filled_amount", 1);
                if (itemStack.isEmpty()) {
                    player.setStackInHand(hand, potionStack);
                } else if (!player.inventory.insertStack(potionStack)) {
                    player.dropItem(potionStack, false);
                } else if (player instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) player).refreshScreenHandler(player.playerScreenHandler);
                }
            }

            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            block.setLevel(world, pos, state, cauldronFilledAmount - 1);
        }
        return ActionResult.success(world.isClient);
    }

}
