package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleMenu;
import io.github.spencerharris192.seedtocellar.brewing.station.FermentingVatMenu;
import io.github.spencerharris192.seedtocellar.brewing.station.KilnMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, SeedToCellar.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<KilnMenu>> KILN = MENUS.register("kiln", () -> IMenuTypeExtension.create(KilnMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<BrewKettleMenu>> BREW_KETTLE = MENUS.register("brew_kettle", () -> IMenuTypeExtension.create(BrewKettleMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<io.github.spencerharris192.seedtocellar.distillery.PotStillMenu>> POT_STILL =
            MENUS.register("pot_still", () -> IMenuTypeExtension.create(io.github.spencerharris192.seedtocellar.distillery.PotStillMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<FermentingVatMenu>> FERMENTING_VAT = MENUS.register("fermenting_vat", () -> IMenuTypeExtension.create(FermentingVatMenu::new));

    private ModMenus() {}
}
