package io.github.spencerharris192.seedtocellar.client;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.RoastLevel;
import io.github.spencerharris192.seedtocellar.brewing.station.KilnMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Kiln screen: vanilla furnace layout plus three roast swatches (pale, amber, black) on the right.
 * Clicking a swatch sends a standard menu button click; the server changes the setting.
 */
public class KilnScreen extends AbstractContainerScreen<KilnMenu> {
    private static final Identifier TEXTURE = SeedToCellar.id("textures/gui/kiln.png");
    private static final int SWATCH_X = 144;
    private static final int SWATCH_Y = 17;
    private static final int SWATCH_STEP = 18;
    private static final int SWATCH_SIZE = 16;

    public KilnScreen(KilnMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int hovered = swatchAt(mouseX, mouseY);
        if (hovered >= 0) {
            RoastLevel level = RoastLevel.values()[hovered];
            graphics.setComponentTooltipForNextFrame(font, List.of(
                    Component.translatable("gui.seedtocellar.kiln.roast", level.displayName()),
                    Component.translatable("gui.seedtocellar.kiln.roast." + level.getSerializedName() + ".hint")), mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        // flame: burn time remaining (texture at 176,0, 14x14, drawn from the bottom up)
        if (menu.burnTime() > 0 && menu.burnDuration() > 0) {
            int h = Math.max(1, menu.burnTime() * 14 / menu.burnDuration());
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 57, y + 37 + 14 - h, 176, 14 - h, 14, h, 256, 256);
        }
        // arrow: batch progress (texture at 176,14, 24x17)
        if (menu.totalTime() > 0 && menu.progress() > 0) {
            int w = menu.progress() * 24 / menu.totalTime();
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 79, y + 34, 176, 14, w, 17, 256, 256);
        }
        // roast swatches (texture at 176,31: three 16x16 swatches) + selection frame (176,47, 18x18)
        int selected = menu.roast().ordinal();
        for (int i = 0; i < 3; i++) {
            int sy = y + SWATCH_Y + i * SWATCH_STEP;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + SWATCH_X, sy, 176 + i * 16, 31, SWATCH_SIZE, SWATCH_SIZE, 256, 256);
            if (i == selected) graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + SWATCH_X - 1, sy - 1, 176, 47, 18, 18, 256, 256);
        }
    }

    private int swatchAt(double mouseX, double mouseY) {
        for (int i = 0; i < 3; i++) {
            int sx = leftPos + SWATCH_X;
            int sy = topPos + SWATCH_Y + i * SWATCH_STEP;
            if (mouseX >= sx && mouseX < sx + SWATCH_SIZE && mouseY >= sy && mouseY < sy + SWATCH_SIZE) return i;
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x(), mouseY = event.y();
        int swatch = swatchAt(mouseX, mouseY);
        if (swatch >= 0 && minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, swatch);
            minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
