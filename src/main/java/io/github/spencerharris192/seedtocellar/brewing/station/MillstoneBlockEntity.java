package io.github.spencerharris192.seedtocellar.brewing.station;

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
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

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

    private final ItemStackHandler items = new ItemStackHandler(SLOTS) {
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

    private final LazyOptional<IItemHandler> inputCap = LazyOptional.of(() -> new RangedWrapper(items, INPUT, INPUT + 1));
    private final LazyOptional<IItemHandler> outputCap = LazyOptional.of(() -> new RangedWrapper(items, OUTPUT, BYPRODUCT + 1));

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
                || ItemStack.isSameItemSameTags(in, stack) && in.getCount() + stack.getCount() <= in.getMaxStackSize();
    }

    private Optional<MillingRecipe> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(ModRecipes.MILLING.get(), new SimpleContainer(stack), level);
    }

    public InteractionResult onUse(Player player, InteractionHand hand) {
        boolean client = level.isClientSide;
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() && player.isShiftKeyDown()) {
            if (!client) {
                if (!output().isEmpty() || !byproduct().isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, items.extractItem(OUTPUT, 64, false));
                    ItemHandlerHelper.giveItemToPlayer(player, items.extractItem(BYPRODUCT, 64, false));
                } else if (!input().isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, items.extractItem(INPUT, 64, false));
                }
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (!held.isEmpty() && findRecipe(held).isPresent()) {
            if (!client) {
                ItemStack remainder = items.insertItem(INPUT, held.copy(), false);
                if (!player.getAbilities().instabuild) player.setItemInHand(hand, remainder);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (held.isEmpty()) {
            if (!client) crank();
            return InteractionResult.sidedSuccess(client);
        }
        return InteractionResult.PASS;
    }

    /** One turn of the stone. Returns false while the previous turn is still animating. */
    public boolean crank() {
        if (level == null || level.isClientSide) return false;
        long now = level.getGameTime();
        if (now - lastCrank < CRANK_TICKS) return false;
        lastCrank = now;
        level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_CRANK, 0);
        level.playSound(null, worldPosition, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.5F, 0.8F + level.random.nextFloat() * 0.2F);

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
            if (level != null && level.isClientSide) {
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
        tag.putInt("Turns", turns);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Items"));
        if (items.getSlots() < SLOTS) {   // millstones from before the byproduct slot: keep their input and output
            ItemStack in = items.getStackInSlot(INPUT), out = items.getStackInSlot(OUTPUT);
            items.setSize(SLOTS);
            items.setStackInSlot(INPUT, in);
            items.setStackInSlot(OUTPUT, out);
        }
        turns = tag.getInt("Turns");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return side == Direction.DOWN ? outputCap.cast() : inputCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputCap.invalidate();
        outputCap.invalidate();
    }
}
