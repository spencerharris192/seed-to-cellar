package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;

import java.util.function.Supplier;

/**
 * A perennial plant you pick again and again (GDD growth styles E, F and G), in the pattern of
 * vanilla's sweet berry bush.
 * <ul>
 *   <li>Ages: 0 planted, 1 young (and where it goes back to after picking), 2 flowering,
 *       3 ripe. Grows on random ticks (and bone meal, and Forge's crop events).</li>
 *   <li>Right-click when ripe to pick 2-3 of its harvest; it drops back to age 1 and regrows.
 *       Herbs are cut back the same way (right-click or shears).</li>
 *   <li>Options: thorny (slows and scratches, like sweet berries: blackberry), needs water
 *       beside it (cranberry), and flowers you can pick instead of waiting for fruit
 *       (elderberry: picking the flowers costs this cycle's berries).</li>
 * </ul>
 * Grows on grass, dirt or farmland. Found in the wild as itself, already ripe.
 */
public class BushCropBlock extends BushBlock implements BonemealableBlock, ClimateCrop {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final int MAX_AGE = 3;
    /** Where a plant goes back to after picking. */
    public static final int PICKED_AGE = 1;
    private static final VoxelShape[] SHAPES = {
            box(4, 0, 4, 12, 7, 12), box(2, 0, 2, 14, 11, 14), box(1, 0, 1, 15, 15, 15), box(1, 0, 1, 15, 15, 15)};

    /** What makes this bush different. */
    public record Traits(boolean thorny, boolean needsWater, Supplier<? extends Item> flowers) {
        public static final Traits PLAIN = new Traits(false, false, null);
    }

    private final Supplier<? extends Item> planting;
    private final Supplier<? extends Item> harvest;
    private final Traits traits;
    private final Climate climate;

    public BushCropBlock(Properties properties, Supplier<? extends Item> planting, Supplier<? extends Item> harvest,
                         Traits traits, Climate climate) {
        super(properties);
        this.planting = planting;
        this.harvest = harvest;
        this.traits = traits;
        this.climate = climate;
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public BlockState ripe() {
        return defaultBlockState().setValue(AGE, MAX_AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(planting.get());
    }

    // --- where it grows ------------------------------------------------------------------------

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    /** Checked directly: soil below, and for water-edge plants, water right beside the soil or the plant. */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        if (!mayPlaceOn(level.getBlockState(below), level, below)) return false;
        return !traits.needsWater() || waterBeside(level, below) || waterBeside(level, pos);
    }

    private static boolean waterBeside(LevelReader level, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getFluidState(pos.relative(side)).is(FluidTags.WATER)) return true;
        }
        return false;
    }

    // --- growing -------------------------------------------------------------------------------

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public Climate climate() {
        return climate;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int attempts = ClimateRules.growthAttempts(climate, level, pos, random);
        for (int i = 0; i < attempts && level.getBlockState(pos).is(this); i++) growTick(level.getBlockState(pos), level, pos, random);
    }

    private void growTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE && level.getRawBrightness(pos.above(), 0) >= 9
                && ForgeHooks.onCropsGrowPre(level, pos, state, random.nextInt(5) == 0)) {
            BlockState grown = state.setValue(AGE, age + 1);
            level.setBlock(pos, grown, Block.UPDATE_CLIENTS);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(grown));
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, Math.min(MAX_AGE, state.getValue(AGE) + 1)), Block.UPDATE_CLIENTS);
    }

    // --- picking -------------------------------------------------------------------------------

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int age = state.getValue(AGE);
        if (age != MAX_AGE && !(age == 2 && traits.flowers() != null)) return InteractionResult.PASS;
        if (!level.isClientSide) pick(level, pos, state, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Picks a ripe bush (2-3 of its harvest) or a flowering one's flowers (1-2), then cuts it back
     * to young. Fortune on the held tool adds up to its level. False if there was nothing to pick.
     */
    public boolean pick(Level level, BlockPos pos, BlockState state, Player player) {
        int age = state.getValue(AGE);
        Item picked = age == MAX_AGE ? harvest.get()
                : age == 2 && traits.flowers() != null ? traits.flowers().get() : null;
        if (picked == null) return false;
        int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, player.getMainHandItem());
        int count = (age == MAX_AGE ? 2 + level.random.nextInt(2) : 1 + level.random.nextInt(2)) + level.random.nextInt(fortune + 1);
        popResource(level, pos, new ItemStack(picked, count));
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
        BlockState cutBack = state.setValue(AGE, PICKED_AGE);
        level.setBlock(pos, cutBack, Block.UPDATE_CLIENTS);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, cutBack));
        return true;
    }

    // --- thorns --------------------------------------------------------------------------------

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!traits.thorny() || !(entity instanceof LivingEntity) || entity.getType() == EntityType.FOX || entity.getType() == EntityType.BEE) return;
        entity.makeStuckInBlock(state, new Vec3(0.8F, 0.75D, 0.8F));
        if (!level.isClientSide && state.getValue(AGE) > 0 && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
            double dx = Math.abs(entity.getX() - entity.xOld), dz = Math.abs(entity.getZ() - entity.zOld);
            if (dx >= 0.003 || dz >= 0.003) entity.hurt(level.damageSources().sweetBerryBush(), 1.0F);
        }
    }
}
