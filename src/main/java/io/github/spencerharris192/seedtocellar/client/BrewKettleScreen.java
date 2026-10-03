package io.github.spencerharris192.seedtocellar.client;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.WortData;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleMenu;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Kettle screen: liquid gauge on the left, four ingredient slots over the heat flame, progress
 * arrow, a bowl/bottle slot above the output, and plain-language status lines explaining what's
 * next. When the ingredients match a dish, the output slot shows it faded, before it's cooked.
 */
public class BrewKettleScreen extends AbstractContainerScreen<BrewKettleMenu> {
    private static final ResourceLocation TEXTURE = SeedToCellar.id("textures/gui/brew_kettle.png");
    private static final int GAUGE_X = 9, GAUGE_Y = 17, GAUGE_W = 16, GAUGE_H = 52;
    private static final int FLAME_X = 44, FLAME_Y = 54, ARROW_X = 84, ARROW_Y = 34;
    /** Status text box: right of the flame, up to the panel's inner edge. */
    private static final int TEXT_X = 76, TEXT_W = 95;

    public BrewKettleScreen(BrewKettleMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        BrewKettleBlockEntity kettle = menu.kettle();
        if (kettle != null && isHovering(GAUGE_X, GAUGE_Y, GAUGE_W, GAUGE_H, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, FluidRendering.tooltip(kettle.tank().getFluid(), BrewKettleBlockEntity.CAPACITY), mouseX, mouseY);
        }
        // The faded dish in an empty output slot: hovering it says what it is.
        if (kettle != null && kettle.items().getStackInSlot(BrewKettleBlockEntity.OUTPUT).isEmpty()
                && isHovering(BrewKettleMenu.OUTPUT_X, BrewKettleMenu.OUTPUT_Y, 16, 16, mouseX, mouseY)) {
            kettle.matchingRecipe().ifPresent(r -> graphics.renderTooltip(font,
                    Component.translatable("gui.seedtocellar.kettle.makes", r.result().getHoverName()), mouseX, mouseY));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        BrewKettleBlockEntity kettle = menu.kettle();
        if (kettle != null) {
            FluidRendering.gauge(graphics, kettle.tank().getFluid(), BrewKettleBlockEntity.CAPACITY, x + GAUGE_X, y + GAUGE_Y, GAUGE_W, GAUGE_H);
            graphics.blit(TEXTURE, x + GAUGE_X, y + GAUGE_Y, 176, 31, GAUGE_W, GAUGE_H); // measuring marks over the liquid
            if (kettle.items().getStackInSlot(BrewKettleBlockEntity.OUTPUT).isEmpty()) {
                kettle.matchingRecipe().ifPresent(r -> {
                    ItemStack ghost = r.result();
                    int gx = x + BrewKettleMenu.OUTPUT_X, gy = y + BrewKettleMenu.OUTPUT_Y;
                    graphics.renderFakeItem(ghost, gx, gy);
                    graphics.fill(gx, gy, gx + 16, gy + 16, 0x998B8B8B);   // slot-colored veil: "not cooked yet"
                });
            }
        }
        if (menu.heated()) graphics.blit(TEXTURE, x + FLAME_X, y + FLAME_Y, 176, 0, 14, 14);
        if (menu.total() > 0 && menu.progress() > 0) {
            graphics.blit(TEXTURE, x + ARROW_X, y + ARROW_Y, 176, 14, menu.progress() * 24 / menu.total(), 17);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        BrewKettleBlockEntity kettle = menu.kettle();
        if (kettle == null) return;
        Component status = status(kettle);
        Component strength = strength(kettle);
        int statusLines = GuiText.wrap(font, status, TEXT_W).size();
        // One short line sits level with the flame; longer text starts higher so it stays above the inventory.
        int y = statusLines + (strength == null ? 0 : 1) == 1 ? 58 : 54;
        y += GuiText.draw(graphics, font, status, TEXT_X, y, TEXT_W, 0x404040) * GuiText.LINE_HEIGHT + 1;
        if (strength != null) GuiText.draw(graphics, font, strength, TEXT_X, y, TEXT_W, 0x404040);
    }

    /** The wort's strength while mashing or once it's wort; null when there's nothing to judge. */
    private static Component strength(BrewKettleBlockEntity kettle) {
        FluidStack fluid = kettle.tank().getFluid();
        String key = "gui.seedtocellar.kettle.strength";
        if (fluid.getFluid() == ModFluids.SWEET_WORT.get() || fluid.getFluid() == ModFluids.HOPPED_WORT.get()) {
            return Component.translatable(key, WortData.of(fluid).strength().displayName());
        }
        WortData.Strength preview = kettle.previewStrength();
        if (!fluid.isEmpty() && preview != null && !kettle.mashWeights().isEmpty()) {
            return Component.translatable(key, preview.displayName());
        }
        return null;
    }

    private Component status(BrewKettleBlockEntity kettle) {
        FluidStack fluid = kettle.tank().getFluid();
        String k = "gui.seedtocellar.kettle.";
        if (!menu.heated()) return Component.translatable(k + "no_heat").withStyle(ChatFormatting.DARK_RED);
        switch (menu.stage()) {
            case MASHING -> { return Component.translatable(k + "mashing"); }
            case BOILING -> { return Component.translatable(k + "boiling"); }
            case COOKING -> { return Component.translatable(k + "cooking"); }
            case MIXING -> { return Component.translatable(k + "mixing"); }
            default -> { }
        }
        if (fluid.getFluid() == ModFluids.SWEET_WORT.get()) return Component.translatable(k + "add_hops", kettle.hopsNeeded());
        if (fluid.getFluid() == ModFluids.HOPPED_WORT.get()) return Component.translatable(k + "ready");
        if (kettle.needsMalt() && fluid.getFluid().is(net.minecraft.tags.FluidTags.WATER)) {
            return Component.translatable(k + "needs_malt").withStyle(ChatFormatting.DARK_RED);
        }
        Component mixing = kettle.missingForMixing();
        if (mixing != null) return mixing.copy().withStyle(ChatFormatting.DARK_RED);
        Component missing = kettle.missingForCooking();
        if (missing != null) return missing.copy().withStyle(ChatFormatting.DARK_RED);
        if (fluid.isEmpty()) return Component.translatable(k + "add_water");
        return Component.translatable(k + "add_grist");
    }
}
