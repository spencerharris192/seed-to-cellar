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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SeedToCellar.MOD_ID);

    public static final RegistryObject<BlockEntityType<MaltingTubBlockEntity>> MALTING_TUB = register("malting_tub", MaltingTubBlockEntity::new, ModBlocks.MALTING_TUB);
    public static final RegistryObject<BlockEntityType<KilnBlockEntity>> KILN = register("kiln", KilnBlockEntity::new, ModBlocks.KILN);
    public static final RegistryObject<BlockEntityType<MillstoneBlockEntity>> MILLSTONE = register("millstone", MillstoneBlockEntity::new, ModBlocks.MILLSTONE);
    public static final RegistryObject<BlockEntityType<BrewKettleBlockEntity>> BREW_KETTLE = register("brew_kettle", BrewKettleBlockEntity::new, ModBlocks.BREW_KETTLE);
    public static final RegistryObject<BlockEntityType<FermentingVatBlockEntity>> FERMENTING_VAT = register("fermenting_vat", FermentingVatBlockEntity::new, ModBlocks.FERMENTING_VAT);
    public static final RegistryObject<BlockEntityType<PreservingJarBlockEntity>> PRESERVING_JAR = register("preserving_jar", PreservingJarBlockEntity::new, ModBlocks.PRESERVING_JAR);
    public static final RegistryObject<BlockEntityType<CompostBinBlockEntity>> COMPOST_BIN = register("compost_bin", CompostBinBlockEntity::new, ModBlocks.COMPOST_BIN);
    public static final RegistryObject<BlockEntityType<DryingRackBlockEntity>> DRYING_RACK = register("drying_rack", DryingRackBlockEntity::new, ModBlocks.DRYING_RACK);
    public static final RegistryObject<BlockEntityType<CrushingTubBlockEntity>> CRUSHING_TUB = register("crushing_tub", CrushingTubBlockEntity::new, ModBlocks.CRUSHING_TUB);
    public static final RegistryObject<BlockEntityType<FruitPressBlockEntity>> FRUIT_PRESS = register("fruit_press", FruitPressBlockEntity::new, ModBlocks.FRUIT_PRESS);
    public static final RegistryObject<BlockEntityType<io.github.spencerharris192.seedtocellar.distillery.PotStillBlockEntity>> POT_STILL =
            register("pot_still", io.github.spencerharris192.seedtocellar.distillery.PotStillBlockEntity::new, ModBlocks.POT_STILL);
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<CaskBlockEntity>> CASK = TYPES.register("cask",
            () -> BlockEntityType.Builder.of(CaskBlockEntity::new, Stream.concat(ModBlocks.CASKS.values().stream(),
                    Stream.of(ModBlocks.KEG)).map(RegistryObject::get).toArray(Block[]::new)).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlockEntity>> PLACED_DRINKS =
            TYPES.register("placed_drinks", () -> BlockEntityType.Builder.of(io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlockEntity::new,
                    ModBlocks.PLACED_DRINKS.get()).build(null));
    public static final RegistryObject<BlockEntityType<WineRackBlockEntity>> WINE_RACK = TYPES.register("wine_rack",
            () -> BlockEntityType.Builder.of(WineRackBlockEntity::new, ModBlocks.WINE_RACK.get(), ModBlocks.BOTTLE_SHELF.get(),
                    ModBlocks.WINE_DISPLAY.get(), ModBlocks.MUG_RACK.get()).build(null));

    @SuppressWarnings("DataFlowIssue") // a null data fixer is standard for mods
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> factory,
                                                                                     Supplier<Block> block) {
        return TYPES.register(name, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }

    private ModBlockEntities() {}
}
