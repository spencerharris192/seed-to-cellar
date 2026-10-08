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
import io.github.spencerharris192.seedtocellar.recipe.Recipes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

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

    private final StationTank tank = new StationTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };

    private final StationItems fruit = new StationItems(1) {
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

    public CrushingTubBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUSHING_TUB.get(), pos, state);
    }

    public StationTank tank() {
        return tank;
    }

    public ItemStack fruit() {
        return fruit.getStackInSlot(0);
    }

    public Optional<CrushingRecipe> recipeFor(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return Recipes.find(level, ModRecipes.CRUSHING.get(), new SingleRecipeInput(stack)).map(RecipeHolder::value);
    }

    public InteractionResult onUse(Player player, InteractionHand hand) {
        boolean client = level.isClientSide();
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            if (player.isShiftKeyDown() && !fruit().isEmpty()) {
                if (!client) give(player, fruit.extractItem(0, SLOT_LIMIT, false));
                return InteractionResult.SUCCESS;
            }
            if (!client) player.sendOverlayMessage(Component.translatable(fruit().isEmpty()
                    ? "message.seedtocellar.tub.add_fruit" : "message.seedtocellar.tub.jump_in"));
            return InteractionResult.SUCCESS;
        }
        if (holdsLiquidContainer(player, hand)) {
            if (!client) pourWith(player, hand, drainOnly);
            return InteractionResult.SUCCESS;
        }
        if (recipeFor(held).isPresent()) {
            if (!client) {
                ItemStack remainder = fruit.insertItem(0, held.copy(), false);
                if (remainder.getCount() == held.getCount()) {
                    player.sendOverlayMessage(Component.translatable("message.seedtocellar.tub.one_fruit").withStyle(ChatFormatting.YELLOW));
                } else if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, remainder);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** Someone landed in the tub: one stomp toward the fruit on top. */
    public void stomp(Entity entity) {
        if (level == null || level.isClientSide()) return;
        Optional<CrushingRecipe> recipe = recipeFor(fruit());
        if (recipe.isEmpty()) return;
        FluidStack out = recipe.get().result();
        if (!tank.isEmpty() && !FluidStack.isSameFluidSameComponents(tank.getFluid(), out)) {
            if (entity instanceof Player player) {
                player.sendOverlayMessage(Component.translatable("message.seedtocellar.tub.other_liquid", tank.getFluid().getHoverName())
                        .withStyle(ChatFormatting.YELLOW));
            }
            return;
        }
        if (tank.getSpace() < out.getAmount()) {
            if (entity instanceof Player player) {
                player.sendOverlayMessage(Component.translatable("message.seedtocellar.tub.full").withStyle(ChatFormatting.YELLOW));
            }
            return;
        }
        ItemStack squashed = fruit().copyWithCount(1);
        if (entity instanceof ServerPlayer player) ModTriggers.STOMP.get().trigger(player);
        level.playSound(null, worldPosition, SoundEvents.SLIME_BLOCK_FALL, SoundSource.BLOCKS, 0.8F, 0.8F + level.getRandom().nextFloat() * 0.3F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, squashed.getItem()), worldPosition.getX() + 0.5,
                    worldPosition.getY() + CrushingTubBlock.FLOOR / 16.0 + 0.1, worldPosition.getZ() + 0.5, 8, 0.25, 0.05, 0.25, 0.05);
        }
        if (++stomps >= recipe.get().stomps()) {
            stomps = 0;
            fruit.extractItem(0, 1, false);
            tank.fill(out, StationTank.Action.EXECUTE);
        }
        sync();
    }

    @Override
    public List<Component> hydrometerLines() {
        List<Component> lines = new ArrayList<>();
        String h = "hydrometer.seedtocellar.";
        if (!fruit().isEmpty()) lines.add(Component.translatable(h + "tub.fruit", fruit().getCount(), fruit().getHoverName()));
        if (!tank.isEmpty()) lines.add(Component.translatable(h + "liquid", tank.getFluidAmount(), tank.getFluid().getHoverName()));
        if (!fruit().isEmpty()) lines.add(Component.translatable(h + "tub.stomp").withStyle(ChatFormatting.GRAY));
        if (lines.isEmpty()) lines.add(Component.translatable(h + "empty"));
        return lines;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), fruit());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        fruit.serialize(output.child("Fruit"));
        tank.serialize(output.child("Tank"));
        output.putInt("Stomps", stomps);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fruit.deserialize(input.childOrEmpty("Fruit"));
        tank.deserialize(input.childOrEmpty("Tank"));
        stomps = input.getIntOr("Stomps", 0);
    }

    private final ResourceHandler<ItemResource> insertOnly = new InsertOnlyItems(fruit);
    private final ResourceHandler<FluidResource> drainOnly = new DrainOnlyTank(tank);

    /** Hoppers put fruit in from any side (it can't be taken back out). */
    public ResourceHandler<ItemResource> itemHandler(@Nullable Direction side) {
        return insertOnly;
    }

    /** Pipes and buckets take the liquid out (nothing can be poured in). */
    public ResourceHandler<FluidResource> fluidHandler(@Nullable Direction side) {
        return drainOnly;
    }
}
