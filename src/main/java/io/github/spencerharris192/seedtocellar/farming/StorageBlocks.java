package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HayBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Storage blocks (GDD section 17.1): bales of grain (nine), sacks of goods and crates of fruit (eight round a string or a
 * wooden slab), crafted back into what they hold. Tagged {@code forge:storage_blocks/<tag>} so other mods' storage
 * and compacting recipes see them. All burn: bales like hay (and soften falls like it), sacks like
 * wool, crates like planks.
 */
public final class StorageBlocks {
    /**
     * One storage block: what it holds (packed from that item only, as other mods pack theirs, so the two never fight over
     * a crafting grid), its forge:storage_blocks/ tag name and its display name. Datagen reads these.
     */
    public record Storage(RegistryObject<Block> block, Supplier<? extends ItemLike> contents, String tag, String name) {
        /**
         * How many it holds: a bale nine, like hay; a sack eight round the string that ties it, a crate eight round the slab
         * it's built on (nine apples or nine sugar would be Quark's apple crate or Supplementaries' sugar cube).
         */
        public int count() {
            return block.get() instanceof Bale ? 9 : 8;
        }
    }

    public static class Bale extends HayBlock {
        public Bale(Properties properties) {
            super(properties);
        }

        @Override
        public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return 20;
        }

        @Override
        public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return 60;
        }
    }

    public static class Sack extends Block {
        public Sack(Properties properties) {
            super(properties);
        }

        @Override
        public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return 60;
        }

        @Override
        public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return 30;
        }
    }

    /** A slatted wooden crate, the fruit heaped on top. */
    public static class Crate extends Block {
        public Crate(Properties properties) {
            super(properties);
        }

        @Override
        public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return 20;
        }

        @Override
        public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return 5;
        }
    }

    private StorageBlocks() {}
}
