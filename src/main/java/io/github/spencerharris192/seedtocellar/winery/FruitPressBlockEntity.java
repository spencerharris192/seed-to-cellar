package io.github.spencerharris192.seedtocellar.winery;

import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.brewing.station.SyncedBlockEntity;
import io.github.spencerharris192.seedtocellar.recipe.PressingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
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
import net.minecraftforge.items.wrapper.RangedWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fruit Press logic: up to 16 of one fruit in the cage, 4 buckets in the tray, and the pomace left behind.
 * Each crank turns the screw down a little; after a recipe's cranks (`seedtocellar:pressing`) the press
 * squeezes one batch (4 fruit, usually) into the tray and leaves the byproduct. Cranking with too little
 * fruit, a full tray, a different liquid in the tray or no room for pomace turns the screw but presses nothing.
 * Automation: hoppers put fruit in from the top and sides and take pomace out from below; pipes take the liquid.
 */
public class FruitPressBlockEntity extends SyncedBlockEntity implements HydrometerReadable {
    public static final int CAPACITY = 4000;
    public static final int INPUT = 0, BYPRODUCT = 1;
    public static final int SLOT_LIMIT = 16;
    public static final int EVENT_CRANK = 1;
    /** Ticks per crank: also how long the handle takes to swing round. */
    public static final int CRANK_TICKS = 8;

    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };

    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == INPUT ? SLOT_LIMIT : 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT && recipeFor(stack).isPresent();
        }
    };

    private int turns;
    private long lastCrank = -CRANK_TICKS;

    // client-side handle animation (not saved)
    private int clientCranks;
    private long clientAnimStart = Long.MIN_VALUE;

    private final LazyOptional<IItemHandler> inputCap = LazyOptional.of(() -> new InsertOnlyItems(new RangedWrapper(items, INPUT, INPUT + 1)));
    private final LazyOptional<IItemHandler> outputCap = LazyOptional.of(() -> new RangedWrapper(items, BYPRODUCT, BYPRODUCT + 1) {
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }
    });
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> new DrainOnlyTank(tank));

    public FruitPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FRUIT_PRESS.get(), pos, state);
    }

    public FluidTank tank() {
        return tank;
    }

    public ItemStack input() {
        return items.getStackInSlot(INPUT);
    }

    public ItemStack byproduct() {
        return items.getStackInSlot(BYPRODUCT);
    }

    public Optional<PressingRecipe> recipeFor(ItemStack stack) {
        if (level == null || stack.isEmpty()) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(ModRecipes.PRESSING.get(), new SimpleContainer(stack), level);
    }

    /** How far down the screw has come for the current batch, 0 (up) to 1 (pressing). */
    public float screwDepth() {
        Optional<PressingRecipe> recipe = recipeFor(input());
        return recipe.map(r -> Math.min(1F, turns / (float) r.cranks())).orElse(0F);
    }

    public InteractionResult onUse(Player player, InteractionHand hand) {
        boolean client = level.isClientSide;
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() && player.isShiftKeyDown()) {
            if (!client) {
                if (!byproduct().isEmpty()) ItemHandlerHelper.giveItemToPlayer(player, items.extractItem(BYPRODUCT, 64, false));
                else if (!input().isEmpty()) ItemHandlerHelper.giveItemToPlayer(player, items.extractItem(INPUT, SLOT_LIMIT, false));
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (held.isEmpty()) {
            if (!client) crank();
            return InteractionResult.sidedSuccess(client);
        }
        if (FluidUtil.getFluidHandler(held).isPresent()) {
            if (!client) FluidUtil.interactWithFluidHandler(player, hand, new DrainOnlyTank(tank));
            return InteractionResult.sidedSuccess(client);
        }
        if (recipeFor(held).isPresent()) {
            if (!client) {
                ItemStack remainder = items.insertItem(INPUT, held.copy(), false);
                if (remainder.getCount() == held.getCount()) {
                    player.displayClientMessage(Component.translatable("message.seedtocellar.press.one_fruit").withStyle(ChatFormatting.YELLOW), true);
                } else if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, remainder);
                }
            }
            return InteractionResult.sidedSuccess(client);
        }
        return InteractionResult.PASS;
    }

    /** One turn of the screw. Returns false while the previous turn is still swinging round. */
    public boolean crank() {
        if (level == null || level.isClientSide) return false;
        long now = level.getGameTime();
        if (now - lastCrank < CRANK_TICKS) return false;
        lastCrank = now;
        level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_CRANK, 0);
        level.playSound(null, worldPosition, SoundEvents.CROSSBOW_LOADING_MIDDLE, SoundSource.BLOCKS, 0.6F, 0.6F + level.random.nextFloat() * 0.1F);

        Optional<PressingRecipe> found = recipeFor(input());
        if (found.isEmpty() || input().getCount() < found.get().count()) return true;
        PressingRecipe recipe = found.get();
        FluidStack out = recipe.result();
        ItemStack left = recipe.byproduct();
        boolean trayOk = (tank.isEmpty() || tank.getFluid().isFluidEqual(out)) && tank.getSpace() >= out.getAmount();
        boolean roomForPomace = left.isEmpty() || byproduct().isEmpty()
                || (ItemStack.isSameItemSameTags(byproduct(), left) && byproduct().getCount() + left.getCount() <= byproduct().getMaxStackSize());
        if (!trayOk || !roomForPomace) return true;
        if (++turns >= recipe.cranks()) {
            turns = 0;
            items.extractItem(INPUT, recipe.count(), false);
            tank.fill(out, IFluidHandler.FluidAction.EXECUTE);
            if (!left.isEmpty()) {
                ItemStack pomace = byproduct().isEmpty() ? left : byproduct().copyWithCount(byproduct().getCount() + left.getCount());
                items.setStackInSlot(BYPRODUCT, pomace);
            }
            level.playSound(null, worldPosition, SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, 1.0F, 0.8F);
        }
        sync();
        return true;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == EVENT_CRANK) {
            if (level != null && level.isClientSide) {
                clientCranks++;
                clientAnimStart = level.getGameTime();
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    /** Degrees the handle has turned, smoothly animated (client only). */
    public float handleAngle(float partialTick) {
        if (level == null || clientCranks == 0) return 0;
        float t = Math.min(1F, (level.getGameTime() - clientAnimStart + partialTick) / CRANK_TICKS);
        float eased = t * t * (3 - 2 * t);
        return 90F * (clientCranks - 1 + eased);
    }

    @Override
    public List<Component> hydrometerLines() {
        List<Component> lines = new ArrayList<>();
        String h = "hydrometer.seedtocellar.";
        if (!input().isEmpty()) lines.add(Component.translatable(h + "press.fruit", input().getCount(), input().getHoverName()));
        recipeFor(input()).ifPresent(r -> lines.add(Component.translatable(h + "press.progress", turns, r.cranks(), r.count())
                .withStyle(ChatFormatting.GRAY)));
        if (!tank.isEmpty()) lines.add(Component.translatable(h + "liquid", tank.getFluidAmount(), tank.getFluid().getDisplayName()));
        if (!byproduct().isEmpty()) lines.add(Component.translatable(h + "press.byproduct", byproduct().getCount(), byproduct().getHoverName()));
        if (lines.isEmpty()) lines.add(Component.translatable(h + "empty"));
        return lines;
    }

    public void dropContents() {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), items.getStackInSlot(i));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        tag.putInt("Turns", turns);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Items"));
        tank.readFromNBT(tag.getCompound("Tank"));
        turns = tag.getInt("Turns");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return side == Direction.DOWN ? outputCap.cast() : inputCap.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputCap.invalidate();
        outputCap.invalidate();
        fluidCap.invalidate();
    }
}
