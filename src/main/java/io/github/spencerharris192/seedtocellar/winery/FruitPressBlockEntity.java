package io.github.spencerharris192.seedtocellar.winery;

import io.github.spencerharris192.seedtocellar.brewing.station.StationTank;
import io.github.spencerharris192.seedtocellar.brewing.station.StationItems;
import org.jspecify.annotations.Nullable;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
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
import io.github.spencerharris192.seedtocellar.recipe.Recipes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

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

    private final StationTank tank = new StationTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };

    private final StationItems items = new StationItems(2) {
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

    public FruitPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FRUIT_PRESS.get(), pos, state);
    }

    public StationTank tank() {
        return tank;
    }

    public ItemStack input() {
        return items.getStackInSlot(INPUT);
    }

    public ItemStack byproduct() {
        return items.getStackInSlot(BYPRODUCT);
    }

    public Optional<PressingRecipe> recipeFor(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return Recipes.find(level, ModRecipes.PRESSING.get(), new SingleRecipeInput(stack)).map(RecipeHolder::value);
    }

    /** How far down the screw has come for the current batch, 0 (up) to 1 (pressing). */
    public float screwDepth() {
        Optional<PressingRecipe> recipe = recipeFor(input());
        return recipe.map(r -> Math.min(1F, turns / (float) r.cranks())).orElse(0F);
    }

    public InteractionResult onUse(Player player, InteractionHand hand) {
        boolean client = level.isClientSide();
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() && player.isShiftKeyDown()) {
            if (!client) {
                if (!byproduct().isEmpty()) give(player, items.extractItem(BYPRODUCT, 64, false));
                else if (!input().isEmpty()) give(player, items.extractItem(INPUT, SLOT_LIMIT, false));
            }
            return InteractionResult.SUCCESS;
        }
        if (held.isEmpty()) {
            if (!client) crank();
            return InteractionResult.SUCCESS;
        }
        if (holdsLiquidContainer(player, hand)) {
            if (!client) pourWith(player, hand, drainOnly);
            return InteractionResult.SUCCESS;
        }
        if (recipeFor(held).isPresent()) {
            if (!client) {
                ItemStack remainder = items.insertItem(INPUT, held.copy(), false);
                if (remainder.getCount() == held.getCount()) {
                    player.sendOverlayMessage(Component.translatable("message.seedtocellar.press.one_fruit").withStyle(ChatFormatting.YELLOW));
                } else if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, remainder);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** One turn of the screw. Returns false while the previous turn is still swinging round. */
    public boolean crank() {
        if (level == null || level.isClientSide()) return false;
        long now = level.getGameTime();
        if (now - lastCrank < CRANK_TICKS) return false;
        lastCrank = now;
        level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_CRANK, 0);
        level.playSound(null, worldPosition, SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.BLOCKS, 0.6F, 0.6F + level.getRandom().nextFloat() * 0.1F);

        Optional<PressingRecipe> found = recipeFor(input());
        if (found.isEmpty() || input().getCount() < found.get().count()) return true;
        PressingRecipe recipe = found.get();
        FluidStack out = recipe.result();
        ItemStack left = recipe.byproduct();
        boolean trayOk = (tank.isEmpty() || FluidStack.isSameFluidSameComponents(tank.getFluid(), out)) && tank.getSpace() >= out.getAmount();
        boolean roomForPomace = left.isEmpty() || byproduct().isEmpty()
                || (ItemStack.isSameItemSameComponents(byproduct(), left) && byproduct().getCount() + left.getCount() <= byproduct().getMaxStackSize());
        if (!trayOk || !roomForPomace) return true;
        if (++turns >= recipe.cranks()) {
            turns = 0;
            items.extractItem(INPUT, recipe.count(), false);
            tank.fill(out, StationTank.Action.EXECUTE);
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
            if (level != null && level.isClientSide()) {
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
        if (!tank.isEmpty()) lines.add(Component.translatable(h + "liquid", tank.getFluidAmount(), tank.getFluid().getHoverName()));
        if (!byproduct().isEmpty()) lines.add(Component.translatable(h + "press.byproduct", byproduct().getCount(), byproduct().getHoverName()));
        if (lines.isEmpty()) lines.add(Component.translatable(h + "empty"));
        return lines;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), items.getStackInSlot(i));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        items.serialize(output.child("Items"));
        tank.serialize(output.child("Tank"));
        output.putInt("Turns", turns);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.deserialize(input.childOrEmpty("Items"));
        tank.deserialize(input.childOrEmpty("Tank"));
        turns = input.getIntOr("Turns", 0);
    }

    private final ResourceHandler<ItemResource> inputSide = new InsertOnlyItems(RangedResourceHandler.ofSingleIndex(items, INPUT));
    /** The pomace left in the press comes out from below; nothing goes in that way. */
    private final ResourceHandler<ItemResource> outputSide = new DelegatingResourceHandler<>(RangedResourceHandler.ofSingleIndex(items, BYPRODUCT)) {
        @Override
        public boolean isValid(int index, ItemResource resource) {
            return false;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    };
    private final ResourceHandler<FluidResource> drainOnly = new DrainOnlyTank(tank);

    /** Hoppers: fruit in from the top and sides, pomace out from below. */
    public ResourceHandler<ItemResource> itemHandler(@Nullable Direction side) {
        return side == Direction.DOWN ? outputSide : inputSide;
    }

    /** Pipes and buckets take the juice out (nothing can be poured in). */
    public ResourceHandler<FluidResource> fluidHandler(@Nullable Direction side) {
        return drainOnly;
    }
}
