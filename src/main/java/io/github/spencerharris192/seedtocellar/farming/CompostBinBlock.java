package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Compost Bin (GDD section 7): a wooden bin you fill with plant leftovers. You can see it
 * working: the pile rises as you fill it, then turns dark and crumbly when the compost is
 * ready. Right-click with a leftover to add it (sneak to add the whole stack); right-click the
 * finished bin to take 4 Compost.
 */
public class CompostBinBlock extends BaseEntityBlock {
    public static final int MAX_LEVEL = 4;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, MAX_LEVEL);
    public static final BooleanProperty READY = BooleanProperty.create("ready");
    // Walls and floor only, so you can reach (and see) into it.
    private static final VoxelShape SHAPE = Shapes.join(box(1, 0, 1, 15, 14, 15), box(2, 1, 2, 14, 14, 14),
            (a, b) -> a && !b);

    public CompostBinBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LEVEL, 0).setValue(READY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, READY);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CompostBinBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CompostBinBlockEntity bin)) return InteractionResult.PASS;
        if (bin.isReady()) {
            if (!level.isClientSide()) {
                ItemStack compost = bin.takeCompost();
                if (!player.getInventory().add(compost)) popResource(level, pos.above(), compost);
                level.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!CompostBinBlockEntity.accepts(held)) return InteractionResult.PASS;
        int wanted = player.isSecondaryUseActive() ? held.getCount() : 1;
        if (bin.add(wanted, true) <= 0) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            int taken = bin.add(wanted, false);
            if (!player.getAbilities().instabuild) held.shrink(taken);
            level.playSound(null, pos, SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof CompostBinBlockEntity bin) bin.checkFinished();
    }


    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /** Comparators read how full it is (and 15 when the compost is ready), like the vanilla composter. */
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return state.getValue(READY) ? 15 : state.getValue(LEVEL) * 3;
    }
}
