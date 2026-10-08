package io.github.spencerharris192.seedtocellar.brewing;

import io.github.spencerharris192.seedtocellar.farming.FertileFarmlandBlock;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A bucket of a brewing liquid. It pours into vessels (they handle the click) but never
 * into the world, so a batch can't be lost by misclicking on the ground. Unlike a vanilla
 * bucket it keeps the liquid's data (malt bill, quality, age): the bucket carries the same {@link BrewData}.
 */
public class VesselBucketItem extends BucketItem {
    public VesselBucketItem(Fluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public boolean emptyContents(@Nullable LivingEntity user, Level level, BlockPos pos, @Nullable BlockHitResult hit, @Nullable ItemStack container) {
        return false;
    }

    @Override
    public boolean emptyContents(@Nullable LivingEntity user, Level level, BlockPos pos, @Nullable BlockHitResult hit) {
        return false;
    }

    @Override
    protected void playEmptySound(@Nullable LivingEntity user, LevelAccessor level, BlockPos pos) {
    }

    /** A fertilizer (stillage) poured on farmland, or a crop on it, adds a point of fertility to the 3x3 of farmland there. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!content.is(ModTags.Fluids.FERTILIZERS)) return super.useOn(context);
        Level level = context.getLevel();
        BlockPos center = context.getClickedPos();
        if (level.getBlockState(center).getBlock() instanceof VegetationBlock) center = center.below();
        boolean fed = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
            fed |= FertileFarmlandBlock.addFertility(level, pos.immutable(), 1);
        }
        if (!fed) return InteractionResult.PASS;
        Player player = context.getPlayer();
        if (!level.isClientSide()) {
            level.playSound(null, center, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1F, 0.8F);
            if (player != null && !player.getAbilities().instabuild) player.setItemInHand(context.getHand(), new ItemStack(Items.BUCKET));
        }
        return InteractionResult.SUCCESS;
    }

    /** A bucket of a spirit goes by its age too ("New Make Bucket"). */
    @Override
    public Component getName(ItemStack stack) {
        String key = Drinks.nameKey(content, BrewData.orNull(stack));
        return key != null ? Component.translatable("item.seedtocellar.named_bucket", Component.translatable(key)) : super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        CompoundTag data = BrewData.orNull(stack);
        if (data == null) return;
        if (data.contains(WortData.TAG)) {
            tooltip.accept(Component.translatable("gui.seedtocellar.kettle.strength",
                    WortData.load(data.getCompoundOrEmpty(WortData.TAG)).strength().displayName()).withStyle(ChatFormatting.GRAY));
        }
        if (data.contains(BrewQuality.TAG)) {
            List<Component> lines = new ArrayList<>();
            DrinkItem.addQualityLines(BrewQuality.load(data.getCompoundOrEmpty(BrewQuality.TAG)), content, data, lines);
            lines.forEach(tooltip);
        }
    }

    /** Our filled buckets as liquid containers (an empty bucket fills through NeoForge's own handler, which asks our
     * liquid's type for the bucket: {@link io.github.spencerharris192.seedtocellar.registry.ModFluids.BrewFluidType}). */
    public static class Handler extends ItemAccessResourceHandler<FluidResource> {
        public Handler(ItemAccess access) {
            super(access, 1);
        }

        @Override
        protected FluidResource getResourceFrom(ItemResource held, int index) {
            if (!(held.getItem() instanceof VesselBucketItem bucket)) return FluidResource.EMPTY;
            FluidStack inside = new FluidStack(bucket.content, FluidType.BUCKET_VOLUME);
            BrewData.copy(held.getComponents(), inside);
            return FluidResource.of(inside);
        }

        @Override
        protected int getAmountFrom(ItemResource held, int index) {
            return getResourceFrom(held, index).isEmpty() ? 0 : FluidType.BUCKET_VOLUME;
        }

        @Override
        protected int getCapacity(int index, FluidResource resource) {
            return FluidType.BUCKET_VOLUME;
        }

        @Override
        protected ItemResource update(ItemResource held, int index, FluidResource resource, int amount) {
            if (amount == 0) return ItemResource.of(Items.BUCKET);
            if (amount != FluidType.BUCKET_VOLUME) return ItemResource.EMPTY;
            FluidStack stack = resource.toStack(amount);
            return ItemResource.of(stack.getFluidType().getBucket(stack));
        }
    }
}
