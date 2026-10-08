package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.FermentingVatBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.KilnBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.MaltingTubBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.MillstoneBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.PreservingJarBlockEntity;
import io.github.spencerharris192.seedtocellar.farming.CompostBinBlockEntity;
import io.github.spencerharris192.seedtocellar.farming.DryingRackBlockEntity;
import io.github.spencerharris192.seedtocellar.winery.CrushingTubBlockEntity;
import io.github.spencerharris192.seedtocellar.winery.FruitPressBlockEntity;
import io.github.spencerharris192.seedtocellar.winery.WineRackBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SeedToCellar.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaltingTubBlockEntity>> MALTING_TUB = register("malting_tub", MaltingTubBlockEntity::new, ModBlocks.MALTING_TUB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KilnBlockEntity>> KILN = register("kiln", KilnBlockEntity::new, ModBlocks.KILN);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MillstoneBlockEntity>> MILLSTONE = register("millstone", MillstoneBlockEntity::new, ModBlocks.MILLSTONE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrewKettleBlockEntity>> BREW_KETTLE = register("brew_kettle", BrewKettleBlockEntity::new, ModBlocks.BREW_KETTLE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FermentingVatBlockEntity>> FERMENTING_VAT = register("fermenting_vat", FermentingVatBlockEntity::new, ModBlocks.FERMENTING_VAT);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PreservingJarBlockEntity>> PRESERVING_JAR = register("preserving_jar", PreservingJarBlockEntity::new, ModBlocks.PRESERVING_JAR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CompostBinBlockEntity>> COMPOST_BIN = register("compost_bin", CompostBinBlockEntity::new, ModBlocks.COMPOST_BIN);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> DRYING_RACK = register("drying_rack", DryingRackBlockEntity::new, ModBlocks.DRYING_RACK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrushingTubBlockEntity>> CRUSHING_TUB = register("crushing_tub", CrushingTubBlockEntity::new, ModBlocks.CRUSHING_TUB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FruitPressBlockEntity>> FRUIT_PRESS = register("fruit_press", FruitPressBlockEntity::new, ModBlocks.FRUIT_PRESS);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<io.github.spencerharris192.seedtocellar.distillery.PotStillBlockEntity>> POT_STILL =
            register("pot_still", io.github.spencerharris192.seedtocellar.distillery.PotStillBlockEntity::new, ModBlocks.POT_STILL);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CaskBlockEntity>> CASK = TYPES.register("cask",
            () -> new BlockEntityType<>(CaskBlockEntity::new, Stream.concat(ModBlocks.CASKS.values().stream(),
                    Stream.of(ModBlocks.KEG)).map(DeferredBlock::get).toArray(Block[]::new)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlockEntity>> PLACED_DRINKS =
            TYPES.register("placed_drinks", () -> new BlockEntityType<>(io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlockEntity::new,
                    ModBlocks.PLACED_DRINKS.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WineRackBlockEntity>> WINE_RACK = TYPES.register("wine_rack",
            () -> new BlockEntityType<>(WineRackBlockEntity::new, ModBlocks.WINE_RACK.get(), ModBlocks.BOTTLE_SHELF.get(),
                    ModBlocks.WINE_DISPLAY.get(), ModBlocks.MUG_RACK.get()));

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> factory,
                                                                                     Supplier<? extends Block> block) {
        return TYPES.register(name, () -> new BlockEntityType<>(factory, block.get()));
    }

    private ModBlockEntities() {}
}
