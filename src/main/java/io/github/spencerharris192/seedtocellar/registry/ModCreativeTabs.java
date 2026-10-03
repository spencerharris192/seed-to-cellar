package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SeedToCellar.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + SeedToCellar.MOD_ID))
            .icon(() -> new ItemStack(Crops.BARLEY.produce()))
            .displayItems((params, output) -> {
                // Crops first (seeds, harvest, wild plant for each), fruit trees, then everything else in registration order.
                Set<Item> shown = new LinkedHashSet<>();
                Crops.all().forEach(crop -> shown.addAll(crop.items()));
                FruitTrees.all().forEach(tree -> shown.addAll(tree.items()));
                ModItems.ITEMS.getEntries().forEach(item -> shown.add(item.get()));
                shown.forEach(output::accept);
            })
            .build());

    private ModCreativeTabs() {}
}
