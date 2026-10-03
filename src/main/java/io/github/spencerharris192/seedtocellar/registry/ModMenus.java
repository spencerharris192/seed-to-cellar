package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleMenu;
import io.github.spencerharris192.seedtocellar.brewing.station.FermentingVatMenu;
import io.github.spencerharris192.seedtocellar.brewing.station.KilnMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SeedToCellar.MOD_ID);

    public static final RegistryObject<MenuType<KilnMenu>> KILN = MENUS.register("kiln", () -> IForgeMenuType.create(KilnMenu::new));
    public static final RegistryObject<MenuType<BrewKettleMenu>> BREW_KETTLE = MENUS.register("brew_kettle", () -> IForgeMenuType.create(BrewKettleMenu::new));
    public static final RegistryObject<MenuType<io.github.spencerharris192.seedtocellar.distillery.PotStillMenu>> POT_STILL =
            MENUS.register("pot_still", () -> IForgeMenuType.create(io.github.spencerharris192.seedtocellar.distillery.PotStillMenu::new));
    public static final RegistryObject<MenuType<FermentingVatMenu>> FERMENTING_VAT = MENUS.register("fermenting_vat", () -> IForgeMenuType.create(FermentingVatMenu::new));

    private ModMenus() {}
}
