package io.github.spencerharris192.seedtocellar.brewing.station;

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
import io.github.spencerharris192.seedtocellar.recipe.MillingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Millstone logic. Each crank grinds toward the next item. The turning top stone is drawn by
 * the client renderer; a block event tells nearby clients to animate a crank.
 * Automation: top/sides = input, bottom = output (and any byproduct, like rice's bran).
 */
public class MillstoneBlockEntity extends SyncedBlockEntity implements HydrometerReadable {
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int BYPRODUCT = 2;
    public static final int SLOTS = 3;
    public static final int EVENT_CRANK = 1;
    /** Ticks per crank: also the length of the turning animation. */
    public static final int CRANK_TICKS = 8;

    private final StationItems items = new StationItems(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT && findRecipe(stack).isPresent();
        }
    };

    private int turns;
    private long lastCrank = -CRANK_TICKS;

    // Client-side animation state (not saved)
    private int clientCranks;
    private long clientAnimStart = Long.MIN_VALUE;

    public MillstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MILLSTONE.get(), pos, state);
    }

    public ItemStack input() {
        return items.getStackInSlot(INPUT);
    }

    public ItemStack output() {
        return items.getStackInSlot(OUTPUT);
    }

    public ItemStack byproduct() {
        return items.getStackInSlot(BYPRODUCT);
    }

    /** Whether `stack` fits on top of what's in `slot`. */
    private boolean fits(int slot, ItemStack stack) {
        ItemStack in = items.getStackInSlot(slot);
        return stack.isEmpty() || in.isEmpty()
                || ItemStack.isSameItemSameComponents(in, stack) && in.getCount() + stack.getCount() <= in.getMaxStackSize();
    }

    private Optional<MillingRecipe> findRecipe(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return Recipes.find(level, ModRecipes.MILLING.get(), new SingleRecipeInput(stack)).map(RecipeHolder::value);
    }

    public InteractionResult onUse(Player player, InteractionHand hand) {
        boolean client = level.isClientSide();
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() && player.isShiftKeyDown()) {
            if (!client) {
                if (!output().isEmpty() || !byproduct().isEmpty()) {
                    give(player, items.extractItem(OUTPUT, 64, false));
                    give(player, items.extractItem(BYPRODUCT, 64, false));
                } else if (!input().isEmpty()) {
                    give(player, items.extractItem(INPUT, 64, false));
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (!held.isEmpty() && findRecipe(held).isPresent()) {
            if (!client) {
                ItemStack remainder = items.insertItem(INPUT, held.copy(), false);
                if (!player.getAbilities().instabuild) player.setItemInHand(hand, remainder);
            }
            return InteractionResult.SUCCESS;
        }
        if (held.isEmpty()) {
            if (!client) crank();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** One turn of the stone. Returns false while the previous turn is still animating. */
    public boolean crank() {
        if (level == null || level.isClientSide()) return false;
        long now = level.getGameTime();
        if (now - lastCrank < CRANK_TICKS) return false;
        lastCrank = now;
        level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_CRANK, 0);
        level.playSound(null, worldPosition, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.5F, 0.8F + level.getRandom().nextFloat() * 0.2F);

        Optional<MillingRecipe> recipe = findRecipe(input());
        if (recipe.isEmpty()) return true;
        ItemStack result = recipe.get().result();
        ItemStack bran = recipe.get().byproduct();
        if (!fits(OUTPUT, result) || !fits(BYPRODUCT, bran)) {
            return true; // output full: the stone turns but grinds nothing
        }
        if (++turns >= recipe.get().cranks()) {
            turns = 0;
            items.extractItem(INPUT, 1, false);
            items.setStackInSlot(OUTPUT, output().isEmpty() ? result.copy() : output().copyWithCount(output().getCount() + result.getCount()));
            if (!bran.isEmpty()) items.setStackInSlot(BYPRODUCT, byproduct().isEmpty() ? bran.copy() : byproduct().copyWithCount(byproduct().getCount() + bran.getCount()));
        }
        setChanged();
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

    /** Degrees the top stone has turned, smoothly animated (client only). */
    public float stoneAngle(float partialTick) {
        if (level == null || clientCranks == 0) return 0;
        float t = Math.min(1F, (level.getGameTime() - clientAnimStart + partialTick) / CRANK_TICKS);
        float eased = t * t * (3 - 2 * t);
        return 90F * (clientCranks - 1 + eased);
    }

    @Override
    public List<Component> hydrometerLines() {
        List<Component> lines = new ArrayList<>();
        String h = "hydrometer.seedtocellar.";
        if (!input().isEmpty()) lines.add(Component.translatable(h + "mill.input", input().getCount(), input().getHoverName()));
        if (!output().isEmpty()) lines.add(Component.translatable(h + "mill.output", output().getCount(), output().getHoverName()));
        if (!byproduct().isEmpty()) lines.add(Component.translatable(h + "mill.output", byproduct().getCount(), byproduct().getHoverName()));
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
        output.putInt("Turns", turns);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.deserialize(input.childOrEmpty("Items"));
        turns = input.getIntOr("Turns", 0);
    }

    private final ResourceHandler<ItemResource> inputSide = RangedResourceHandler.ofSingleIndex(items, INPUT);
    private final ResourceHandler<ItemResource> outputSide = RangedResourceHandler.of(items, OUTPUT, BYPRODUCT + 1);

    /** Hoppers: grain in from the top and sides, flour (and any byproduct) out from below. */
    public ResourceHandler<ItemResource> itemHandler(@Nullable Direction side) {
        return side == Direction.DOWN ? outputSide : inputSide;
    }

}
