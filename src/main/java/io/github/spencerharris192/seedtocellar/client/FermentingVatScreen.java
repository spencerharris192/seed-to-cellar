package io.github.spencerharris192.seedtocellar.client;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.YeastType;
import io.github.spencerharris192.seedtocellar.brewing.station.FermentingVatBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.FermentingVatMenu;
import io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe;
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

import java.util.List;
import java.util.Optional;

/**
 * Vat screen: liquid gauge, yeast and lees slots, a lid button, a progress bar, and four lines
 * that explain the batch: what it will become, temperature (with the ideal), yeast, and status.
 */
public class FermentingVatScreen extends AbstractContainerScreen<FermentingVatMenu> {
    private static final Identifier TEXTURE = SeedToCellar.id("textures/gui/fermenting_vat.png");
    private static final int GAUGE_X = 9, GAUGE_Y = 17, GAUGE_W = 24, GAUGE_H = 52;
    private static final int LID_X = 64, LID_Y = 16;
    /** Text column: right of the lid button, up to the panel's inner edge. */
    private static final int TEXT_X = 86, TEXT_W = 85;

    public FermentingVatScreen(FermentingVatMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        FermentingVatBlockEntity vat = menu.vat();
        if (vat != null && isHovering(GAUGE_X, GAUGE_Y, GAUGE_W, GAUGE_H, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(font, FluidRendering.tooltip(vat.tank().getFluid(), FermentingVatBlockEntity.CAPACITY), mouseX, mouseY);
        }
        if (isHovering(LID_X, LID_Y, 18, 18, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(font, List.of(Component.translatable(menu.open()
                    ? "gui.seedtocellar.vat.close_lid" : "gui.seedtocellar.vat.open_lid")), mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos, y = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        FermentingVatBlockEntity vat = menu.vat();
        if (vat != null) {
            FluidRendering.gauge(graphics, vat.tank().getFluid(), FermentingVatBlockEntity.CAPACITY, x + GAUGE_X, y + GAUGE_Y, GAUGE_W, GAUGE_H);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + GAUGE_X, y + GAUGE_Y, 176, 0, GAUGE_W, GAUGE_H, 256, 256);
        }
        // lid button: open lid icon (200,0) or closed (218,0); hover frame (200,18)
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + LID_X, y + LID_Y, menu.open() ? 200 : 218, 0, 18, 18, 256, 256);
        if (isHovering(LID_X, LID_Y, 18, 18, mouseX, mouseY)) graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + LID_X, y + LID_Y, 200, 18, 18, 18, 256, 256);
        // progress bar (texture 176,52 80x5)
        if (menu.fermenting()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + TEXT_X, y + 64, 176, 52, Math.round(80 * menu.progress()), 5, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0xFF404040, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF404040, false);
        FermentingVatBlockEntity vat = menu.vat();
        if (vat == null) return;
        FluidStack fluid = vat.tank().getFluid();
        Optional<FermentingRecipe> recipe = vat.expectedRecipe();
        int line = 18;
        if (fluid.isEmpty()) {
            GuiText.draw(graphics, font, Component.translatable("gui.seedtocellar.vat.add_wort"), TEXT_X, line, TEXT_W, 0xFF404040);
            return;
        }
        Component first;
        if (recipe.isPresent() && !menu.fermenting()) {
            first = Component.translatable("gui.seedtocellar.vat.makes", new FluidStack(recipe.get().result(), 1).getHoverName());
        } else if (menu.fermenting()) {
            first = Component.translatable("gui.seedtocellar.vat.fermenting", Math.round(menu.progress() * 100));
        } else {
            first = fluid.getHoverName();
        }
        // A long drink name wraps; the lines below move down with it.
        int y = line + GuiText.draw(graphics, font, first, TEXT_X, line, TEXT_W, 0xFF404040) * GuiText.LINE_HEIGHT + 3;
        recipe.ifPresent(r -> {
            Temperature now = menu.temperature();
            boolean ok = menu.fermenting() ? menu.temperatureOk() && r.suits(now) : r.suits(now);
            graphics.text(font, Component.translatable("gui.seedtocellar.vat.temperature", now.displayName())
                    .withStyle(ok ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED), TEXT_X, y, 0xFF404040, false);
            graphics.text(font, Component.translatable("gui.seedtocellar.vat.ideal", r.idealName()),
                    TEXT_X, y + 10, 0xFF707070, false);
            if (!menu.fermenting()) {
                YeastType yeast = YeastType.of(vat.items().getStackInSlot(FermentingVatBlockEntity.YEAST));
                Component yeastLine = yeast == YeastType.WILD
                        ? Component.translatable("gui.seedtocellar.vat.yeast_wild").withStyle(ChatFormatting.GOLD)
                        : Component.translatable("gui.seedtocellar.vat.yeast_ok").withStyle(ChatFormatting.DARK_GREEN);
                graphics.text(font, yeastLine, TEXT_X, y + 20, 0xFF404040, false);
                graphics.text(font, Component.translatable(menu.open() ? "gui.seedtocellar.vat.hint_close" : "gui.seedtocellar.vat.hint_open"),
                        TEXT_X, y + 32, 0xFF707070, false);
            }
        });
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x(), mouseY = event.y();
        if (isHovering(LID_X, LID_Y, 18, 18, mouseX, mouseY) && minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, FermentingVatMenu.BUTTON_LID);
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
