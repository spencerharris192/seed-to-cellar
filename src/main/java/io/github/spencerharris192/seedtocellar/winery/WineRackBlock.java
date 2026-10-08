package io.github.spencerharris192.seedtocellar.winery;

import java.util.Map;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A bottle rack (GDD section 16), laid out by its {@link RackLayout}: the Wine Rack's six cubbies, the Bottle Shelf's two
 * boards of three, or the Wine Display's three shelves. Right-click a place with a bottle to put it there, or a full
 * place with an empty hand to take its bottle; hoppers fill and empty it, and a comparator reads how full it is.
 * Faces the player who placed it; bottles drop when it's broken.
 */
public class WineRackBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    /** The Bottle Shelf is half a block deep, against the wall behind it. */
    private static final Map<Direction, VoxelShape> SHELF_SHAPES = Map.of(
            Direction.NORTH, box(0, 0, 8, 16, 16, 16), Direction.SOUTH, box(0, 0, 0, 16, 16, 8),
            Direction.WEST, box(8, 0, 0, 16, 16, 16), Direction.EAST, box(0, 0, 0, 8, 16, 16));

    private final RackLayout layout;

    public WineRackBlock(RackLayout layout, Properties properties) {
        super(properties);
        this.layout = layout;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public RackLayout layout() {
        return layout;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (layout) {
            case BOTTLE_SHELF -> SHELF_SHAPES.get(state.getValue(FACING));
            case MUG_RACK -> MUG_RACK_SHAPES.get(state.getValue(FACING));
            default -> Shapes.block();
        };
    }

    /** The Mug Rack: a board on the wall, and its mugs hanging in front of it. */
    private static final Map<Direction, VoxelShape> MUG_RACK_SHAPES = Map.of(
            Direction.NORTH, box(0, 7, 8, 16, 16, 16), Direction.SOUTH, box(0, 7, 0, 16, 16, 8),
            Direction.WEST, box(8, 7, 0, 16, 16, 16), Direction.EAST, box(0, 7, 0, 8, 16, 16));

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WineRackBlockEntity(pos, state);
    }

    /**
     * The place under a point on the front face, counting along the top row first, left to right as you face the rack
     * (the Wine Rack: 0-2 on top, 3-5 below); -1 if the point isn't on the front.
     */
    public static int slotAt(BlockState state, BlockPos pos, Vec3 hit, Direction face) {
        if (!(state.getBlock() instanceof WineRackBlock rack)) return -1;
        RackLayout layout = rack.layout();
        Direction front = state.getValue(FACING);
        if (face != front) return -1;
        Vec3 local = hit.subtract(Vec3.atLowerCornerOf(pos));
        double across = switch (front) {   // 0 at the viewer's left, 1 at their right
            case NORTH -> 1 - local.x;
            case SOUTH -> local.x;
            case WEST -> local.z;
            default -> 1 - local.z;         // EAST
        };
        int column = Math.min(layout.columns() - 1, Math.max(0, (int) (across * layout.columns())));
        int row = Math.min(layout.rows() - 1, Math.max(0, (int) ((1 - local.y) * layout.rows())));
        return row * layout.columns() + column;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof WineRackBlockEntity rack)) return InteractionResult.PASS;
        int slot = slotAt(state, pos, hit.getLocation(), hit.getDirection());
        if (slot < 0) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        ItemStack racked = rack.bottle(slot);
        boolean bottleInHand = rack.bottles().isItemValid(slot, held);
        if (!racked.isEmpty() && (held.isEmpty() || bottleInHand)) {   // other items (a Hydrometer) do their own thing
            if (!level.isClientSide()) {
                io.github.spencerharris192.seedtocellar.brewing.station.SyncedBlockEntity.give(player, rack.bottles().extractItem(slot, 1, false));
                level.playSound(null, pos, SoundEvents.CHISELED_BOOKSHELF_PICKUP, SoundSource.BLOCKS, 1F, 1.2F);
            }
            return InteractionResult.SUCCESS;
        }
        if (racked.isEmpty() && bottleInHand) {
            if (!level.isClientSide()) {
                ItemStack left = rack.bottles().insertItem(slot, held.copyWithCount(1), false);
                if (left.isEmpty() && !player.getAbilities().instabuild) held.shrink(1);
                level.playSound(null, pos, SoundEvents.CHISELED_BOOKSHELF_INSERT, SoundSource.BLOCKS, 1F, 1.2F);
                if (rack.layout() == RackLayout.BOTTLE_SHELF && rack.differentDrinks() == rack.bottles().getSlots()
                        && player instanceof net.minecraft.server.level.ServerPlayer server) {
                    io.github.spencerharris192.seedtocellar.registry.ModTriggers.TAVERN_KEEPER.get().trigger(server);   // a full shelf, all different
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /** Comparators read how many bottles are racked (0-15). */
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof WineRackBlockEntity rack
                ? net.neoforged.neoforge.transfer.ResourceHandlerUtil.getRedstoneSignalFromResourceHandler(rack.bottles()) : 0;
    }

}
