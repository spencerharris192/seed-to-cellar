package io.github.spencerharris192.seedtocellar.client;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlockEntity;
import io.github.spencerharris192.seedtocellar.distillery.PotStillMenu;
import io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Still screen: the pot gauge on the left, the Gin Basket's four slots and the charcoal filter beside it, an arrow over to
 * the receiver (the spirit) and the stillage gauges, a "Run again" button, and two plain-language lines under it all: what's
 * happening (or what's missing) and what the pot will make.
 */
public class PotStillScreen extends AbstractContainerScreen<PotStillMenu> {
    private static final Identifier TEXTURE = SeedToCellar.id("textures/gui/pot_still.png");
    private static final int POT_X = 9, RECEIVER_X = 99, STILLAGE_X = 121, GAUGE_Y = 17, GAUGE_W = 16, GAUGE_H = 52, STILLAGE_W = 10;
    private static final int ARROW_X = 70, ARROW_Y = 35, FLAME_X = 9, FLAME_Y = 75, BUTTON_X = 150, BUTTON_Y = 16;
    private static final int TEXT_X = 28, TEXT_Y = 75, TEXT_W = 140;

    public PotStillScreen(PotStillMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 186);
        inventoryLabelY = PotStillMenu.INVENTORY_Y - 11;
    }

    private boolean canRunAgain() {
        PotStillBlockEntity still = menu.still();
        return still != null && !menu.running() && still.pot().isEmpty() && !still.receiver().isEmpty();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        PotStillBlockEntity still = menu.still();
        if (still == null) return;
        if (isHovering(POT_X, GAUGE_Y, GAUGE_W, GAUGE_H, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(font, tankTooltip("pot", still.pot().getFluid()), mouseX, mouseY);
        } else if (isHovering(RECEIVER_X, GAUGE_Y, GAUGE_W, GAUGE_H, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(font, tankTooltip("receiver", still.receiver().getFluid()), mouseX, mouseY);
        } else if (isHovering(STILLAGE_X, GAUGE_Y, STILLAGE_W, GAUGE_H, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(font, tankTooltip("stillage", still.stillage().getFluid()), mouseX, mouseY);
        } else if (isHovering(BUTTON_X, BUTTON_Y, 18, 18, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(font, List.of(Component.translatable("gui.seedtocellar.still.run_again"),
                    Component.translatable("gui.seedtocellar.still.run_again_hint").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
        } else if (!menu.hasBasket() && isHovering(29, 16, 37, 37, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(font, List.of(Component.translatable("gui.seedtocellar.still.basket_missing")), mouseX, mouseY);
        } else if (still.items().getStackInSlot(PotStillBlockEntity.FILTER).isEmpty()
                && isHovering(PotStillMenu.FILTER_X, PotStillMenu.FILTER_Y, 16, 16, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(font, List.of(Component.translatable("gui.seedtocellar.still.filter"),
                    Component.translatable("gui.seedtocellar.still.filter_hint").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
        }
    }

    /** Tank name, its liquid and amount, and (for a spirit) its stars. */
    private static List<Component> tankTooltip(String tank, FluidStack fluid) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gui.seedtocellar.still.tank_" + tank).withStyle(ChatFormatting.GOLD));
        lines.addAll(FluidRendering.tooltip(fluid, PotStillBlockEntity.CAPACITY));
        if (BrewQuality.has(fluid)) lines.add(DrinkItem.stars(BrewQuality.of(fluid).stars()));
        return lines;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos, y = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        PotStillBlockEntity still = menu.still();
        if (still != null) {
            FluidRendering.gauge(graphics, still.pot().getFluid(), PotStillBlockEntity.CAPACITY, x + POT_X, y + GAUGE_Y, GAUGE_W, GAUGE_H);
            FluidRendering.gauge(graphics, still.receiver().getFluid(), PotStillBlockEntity.CAPACITY, x + RECEIVER_X, y + GAUGE_Y, GAUGE_W, GAUGE_H);
            FluidRendering.gauge(graphics, still.stillage().getFluid(), PotStillBlockEntity.CAPACITY, x + STILLAGE_X, y + GAUGE_Y, STILLAGE_W, GAUGE_H);
            // measuring marks over the liquids
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + POT_X, y + GAUGE_Y, 176, 31, GAUGE_W, GAUGE_H, 256, 256);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + RECEIVER_X, y + GAUGE_Y, 176, 31, GAUGE_W, GAUGE_H, 256, 256);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + STILLAGE_X, y + GAUGE_Y, 192, 31, STILLAGE_W, GAUGE_H, 256, 256);
        }
        // The Gin Basket's slots: shaded over until a basket is fitted.
        if (!menu.hasBasket()) graphics.fill(x + 29, y + 16, x + 65, y + 52, 0xB0C6C6C6);
        if (menu.heated()) graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + FLAME_X, y + FLAME_Y, 176, 0, 14, 14, 256, 256);
        if (menu.total() > 0 && menu.progress() > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + ARROW_X, y + ARROW_Y, 176, 14, menu.progress() * 24 / menu.total(), 17, 256, 256);
        }
        // run again: normal (202,31), hovered (202,49), unavailable (202,67)
        int v = !canRunAgain() ? 67 : isHovering(BUTTON_X, BUTTON_Y, 18, 18, mouseX, mouseY) ? 49 : 31;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + BUTTON_X, y + BUTTON_Y, 202, v, 18, 18, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        PotStillBlockEntity still = menu.still();
        if (still == null) return;
        Optional<DistillingRecipe> recipe = still.expectedRecipe();
        Component status = status(still, recipe);
        int lines = GuiText.draw(graphics, font, status, TEXT_X, TEXT_Y, TEXT_W, 0xFF404040);
        if (lines < 2 && recipe.isPresent()) GuiText.draw(graphics, font, still.makesLine(recipe.get()), TEXT_X, TEXT_Y + GuiText.LINE_HEIGHT, TEXT_W, 0xFF707070);
    }

    private Component status(PotStillBlockEntity still, Optional<DistillingRecipe> recipe) {
        String k = "gui.seedtocellar.still.";
        if (menu.running()) {
            return menu.heated() ? Component.translatable(k + "distilling", menu.total() == 0 ? 0 : menu.progress() * 100 / menu.total())
                    : Component.translatable(k + "no_heat").withStyle(ChatFormatting.DARK_RED);
        }
        if (still.pot().isEmpty()) {
            return Component.translatable(canRunAgain() ? k + "hint_run_again" : k + "add_wash");
        }
        if (recipe.isEmpty()) return Component.translatable(k + "wont_distil").withStyle(ChatFormatting.DARK_RED);
        Component blocked = still.blocked(recipe.get());
        if (blocked != null) return blocked.copy().withStyle(ChatFormatting.DARK_RED);
        if (!menu.heated()) return Component.translatable(k + "no_heat").withStyle(ChatFormatting.DARK_RED);
        return Component.translatable(k + "starting");
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x(), mouseY = event.y();
        if (isHovering(BUTTON_X, BUTTON_Y, 18, 18, mouseX, mouseY) && canRunAgain() && minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, PotStillMenu.BUTTON_RUN_AGAIN);
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
