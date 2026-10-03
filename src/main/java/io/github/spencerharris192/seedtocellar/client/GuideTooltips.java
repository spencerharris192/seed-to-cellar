package io.github.spencerharris192.seedtocellar.client;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.Climate;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * "What is this / what's next" lines under the name of our items (GDD section 19), so nobody
 * needs a wiki. The text lives in the language file:
 * tooltip.seedtocellar.&lt;item&gt;.desc and tooltip.seedtocellar.&lt;item&gt;.next (either is optional).
 */
@Mod.EventBusSubscriber(modid = SeedToCellar.MOD_ID, value = Dist.CLIENT)
public final class GuideTooltips {
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        if (id == null || !id.getNamespace().equals(SeedToCellar.MOD_ID)) return;
        String base = "tooltip." + SeedToCellar.MOD_ID + "." + id.getPath();
        List<Component> lines = event.getToolTip();
        int at = Math.min(1, lines.size());
        if (I18n.exists(base + ".desc")) {
            lines.add(at++, Component.translatable(base + ".desc").withStyle(ChatFormatting.GRAY));
        }
        Climate climate = climates().get(event.getItemStack().getItem());
        if (climate != null) {
            lines.add(at++, Component.translatable("tooltip." + SeedToCellar.MOD_ID + ".climate",
                    Component.translatable(climate.translationKey())).withStyle(ChatFormatting.DARK_GREEN));
        }
        if (I18n.exists(base + ".next")) {
            lines.add(at, Component.translatable("tooltip." + SeedToCellar.MOD_ID + ".next",
                    Component.translatable(base + ".next").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    private static Map<Item, Climate> climates;

    /** What you plant (seeds, the crop itself for garlic and the like, saplings) mapped to its favorite climate. */
    private static Map<Item, Climate> climates() {
        if (climates == null) {
            Map<Item, Climate> map = new HashMap<>();
            for (Crop crop : Crops.all()) map.put(crop.seeds(), crop.climate);
            for (FruitTree tree : FruitTrees.all()) map.put(tree.saplingItem(), tree.climate);
            climates = map;
        }
        return climates;
    }

    private GuideTooltips() {}
}
