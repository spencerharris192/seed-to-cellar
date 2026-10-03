package io.github.spencerharris192.seedtocellar.winery;

import net.minecraft.server.level.ServerPlayer;
import io.github.spencerharris192.seedtocellar.registry.ModTriggers;
import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.brewing.station.SyncedBlockEntity;
import io.github.spencerharris192.seedtocellar.recipe.CrushingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Crushing Tub logic: up to 16 of one fruit and 4 buckets of what it gives. Each stomp counts toward
 * the fruit on top; when a fruit has had its stomps (`seedtocellar:crushing` recipes) it's gone and its
 * liquid is in the tub. Only one liquid at a time: empty the tub before crushing a different fruit.
 * Automation: hoppers and pipes put fruit in from any side and take liquid out; the fruit can't be
 * pulled back out by machines.
 */
public class CrushingTubBlockEntity extends SyncedBlockEntity implements HydrometerReadable {
    public static final int CAPACITY = 4000;
    public static final int SLOT_LIMIT = 16;

    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };

    private final ItemStackHandler fruit = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return SLOT_LIMIT;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return recipeFor(stack).isPresent();
        }
    };

    private int stomps;

    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> new InsertOnlyItems(fruit));
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> new DrainOnlyTank(tank));

    public CrushingTubBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUSHING_TUB.get(), pos, state);
    }

    public FluidTank tank() {
        return tank;
    }

    public ItemStack fruit() {
        return fruit.getStackInSlot(0);
    }

    public Optional<CrushingRecipe> recipeFor(ItemStack stack) {
        if (level == null || stack.isEmpty()) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(ModRecipes.CRUSHING.get(), new SimpleContainer(stack), level);
    }

    public InteractionResult onUse(Player player, InteractionHand hand) {
        boolean client = level.isClientSide;
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            if (player.isShiftKeyDown() && !fruit().isEmpty()) {
                if (!client) ItemHandlerHelper.giveItemToPlayer(player, fruit.extractItem(0, SLOT_LIMIT, false));
                return InteractionResult.sidedSuccess(client);
            }
            if (!client) player.displayClientMessage(Component.translatable(fruit().isEmpty()
                    ? "message.seedtocellar.tub.add_fruit" : "message.seedtocellar.tub.jump_in"), true);
            return InteractionResult.sidedSuccess(client);
        }
        if (FluidUtil.getFluidHandler(held).isPresent()) {
            if (!client) FluidUtil.interactWithFluidHandler(player, hand, new DrainOnlyTank(tank));
            return InteractionResult.sidedSuccess(client);
        }
        if (recipeFor(held).isPresent()) {
            if (!client) {
                ItemStack remainder = fruit.insertItem(0, held.copy(), false);
                if (remainder.getCount() == held.getCount()) {
                    player.displayClientMessage(Component.translatable("message.seedtocellar.tub.one_fruit").withStyle(ChatFormatting.YELLOW), true);
                } else if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, remainder);
                }
            }
            return InteractionResult.sidedSuccess(client);
        }
        return InteractionResult.PASS;
    }

    /** Someone landed in the tub: one stomp toward the fruit on top. */
    public void stomp(Entity entity) {
        if (level == null || level.isClientSide) return;
        Optional<CrushingRecipe> recipe = recipeFor(fruit());
        if (recipe.isEmpty()) return;
        FluidStack out = recipe.get().result();
        if (!tank.isEmpty() && !tank.getFluid().isFluidEqual(out)) {
            if (entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("message.seedtocellar.tub.other_liquid", tank.getFluid().getDisplayName())
                        .withStyle(ChatFormatting.YELLOW), true);
            }
            return;
        }
        if (tank.getSpace() < out.getAmount()) {
            if (entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("message.seedtocellar.tub.full").withStyle(ChatFormatting.YELLOW), true);
            }
            return;
        }
        ItemStack squashed = fruit().copyWithCount(1);
        if (entity instanceof ServerPlayer player) ModTriggers.STOMP.trigger(player);
        level.playSound(null, worldPosition, SoundEvents.SLIME_BLOCK_FALL, SoundSource.BLOCKS, 0.8F, 0.8F + level.random.nextFloat() * 0.3F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, squashed), worldPosition.getX() + 0.5,
                    worldPosition.getY() + CrushingTubBlock.FLOOR / 16.0 + 0.1, worldPosition.getZ() + 0.5, 8, 0.25, 0.05, 0.25, 0.05);
        }
        if (++stomps >= recipe.get().stomps()) {
            stomps = 0;
            fruit.extractItem(0, 1, false);
            tank.fill(out, IFluidHandler.FluidAction.EXECUTE);
        }
        sync();
    }

    @Override
    public List<Component> hydrometerLines() {
        List<Component> lines = new ArrayList<>();
        String h = "hydrometer.seedtocellar.";
        if (!fruit().isEmpty()) lines.add(Component.translatable(h + "tub.fruit", fruit().getCount(), fruit().getHoverName()));
        if (!tank.isEmpty()) lines.add(Component.translatable(h + "liquid", tank.getFluidAmount(), tank.getFluid().getDisplayName()));
        if (!fruit().isEmpty()) lines.add(Component.translatable(h + "tub.stomp").withStyle(ChatFormatting.GRAY));
        if (lines.isEmpty()) lines.add(Component.translatable(h + "empty"));
        return lines;
    }

    public void dropContents() {
        if (level != null) Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), fruit());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Fruit", fruit.serializeNBT());
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        tag.putInt("Stomps", stomps);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fruit.deserializeNBT(tag.getCompound("Fruit"));
        tank.readFromNBT(tag.getCompound("Tank"));
        stomps = tag.getInt("Stomps");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCap.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
        fluidCap.invalidate();
    }
}
