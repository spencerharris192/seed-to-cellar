package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import java.util.List;
import java.util.function.Supplier;

/**
 * A bucket of a brewing liquid. It pours into vessels (they handle the click) but never
 * into the world, so a batch can't be lost by misclicking on the ground. Unlike a vanilla
 * bucket it keeps the liquid's data (malt bill, quality, age) under "Fluid".
 */
public class VesselBucketItem extends BucketItem {
    public static final String FLUID_TAG = "Fluid";

    public VesselBucketItem(Supplier<? extends Fluid> fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public boolean emptyContents(Player player, Level level, BlockPos pos, BlockHitResult hit, ItemStack container) {
        return false;
    }

    @Override
    public boolean emptyContents(Player player, Level level, BlockPos pos, BlockHitResult hit) {
        return false;
    }

    @Override
    protected void playEmptySound(Player player, LevelAccessor level, BlockPos pos) {
    }

    /** A fertilizer (stillage) poured on farmland, or a crop on it, adds a point of fertility to the 3x3 of farmland there. */
    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        if (!getFluid().is(io.github.spencerharris192.seedtocellar.registry.ModTags.Fluids.FERTILIZERS)) return super.useOn(context);
        Level level = context.getLevel();
        BlockPos center = context.getClickedPos();
        if (level.getBlockState(center).getBlock() instanceof net.minecraft.world.level.block.BushBlock) center = center.below();
        boolean fed = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
            fed |= io.github.spencerharris192.seedtocellar.farming.FertileFarmlandBlock.addFertility(level, pos.immutable(), 1);
        }
        if (!fed) return net.minecraft.world.InteractionResult.PASS;
        Player player = context.getPlayer();
        if (!level.isClientSide) {
            level.playSound(null, center, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, net.minecraft.sounds.SoundSource.BLOCKS, 1F, 0.8F);
            if (player != null && !player.getAbilities().instabuild) player.setItemInHand(context.getHand(), new ItemStack(net.minecraft.world.item.Items.BUCKET));
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new FluidBucketWrapper(stack) {
            @Override
            public FluidStack getFluid() {
                FluidStack fluid = super.getFluid();
                CompoundTag tag = container.getTag();
                if (!fluid.isEmpty() && tag != null && tag.contains(FLUID_TAG)) fluid.setTag(tag.getCompound(FLUID_TAG).copy());
                return fluid;
            }
        };
    }

    /** A bucket of a spirit goes by its age too ("New Make Bucket"). */
    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        String key = tag == null ? null : Drinks.nameKey(getFluid(), tag.getCompound(FLUID_TAG));
        return key != null ? Component.translatable("item.seedtocellar.named_bucket", Component.translatable(key)) : super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(FLUID_TAG)) return;
        CompoundTag fluid = tag.getCompound(FLUID_TAG);
        if (fluid.contains(WortData.TAG)) {
            tooltip.add(Component.translatable("gui.seedtocellar.kettle.strength",
                    WortData.load(fluid.getCompound(WortData.TAG)).strength().displayName()).withStyle(ChatFormatting.GRAY));
        }
        if (fluid.contains(BrewQuality.TAG)) {
            DrinkItem.addQualityLines(BrewQuality.load(fluid.getCompound(BrewQuality.TAG)), getFluid(), fluid, tooltip);
        }
    }
}
