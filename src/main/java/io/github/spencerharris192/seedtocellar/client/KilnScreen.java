package io.github.spencerharris192.seedtocellar.client;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.RoastLevel;
import io.github.spencerharris192.seedtocellar.brewing.station.KilnMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Kiln screen: vanilla furnace layout plus three roast swatches (pale, amber, black) on the right.
 * Clicking a swatch sends a standard menu button click; the server changes the setting.
 */
public class KilnScreen extends AbstractContainerScreen<KilnMenu> {
    private static final ResourceLocation TEXTURE = SeedToCellar.id("textures/gui/kiln.png");
    private static final int SWATCH_X = 144;
    private static final int SWATCH_Y = 17;
    private static final int SWATCH_STEP = 18;
    private static final int SWATCH_SIZE = 16;

    public KilnScreen(KilnMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        int hovered = swatchAt(mouseX, mouseY);
        if (hovered >= 0) {
            RoastLevel level = RoastLevel.values()[hovered];
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable("gui.seedtocellar.kiln.roast", level.displayName()),
                    Component.translatable("gui.seedtocellar.kiln.roast." + level.getSerializedName() + ".hint")), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        // flame: burn time remaining (texture at 176,0, 14x14, drawn from the bottom up)
        if (menu.burnTime() > 0 && menu.burnDuration() > 0) {
            int h = Math.max(1, menu.burnTime() * 14 / menu.burnDuration());
            graphics.blit(TEXTURE, x + 57, y + 37 + 14 - h, 176, 14 - h, 14, h);
        }
        // arrow: batch progress (texture at 176,14, 24x17)
        if (menu.totalTime() > 0 && menu.progress() > 0) {
            int w = menu.progress() * 24 / menu.totalTime();
            graphics.blit(TEXTURE, x + 79, y + 34, 176, 14, w, 17);
        }
        // roast swatches (texture at 176,31: three 16x16 swatches) + selection frame (176,47, 18x18)
        int selected = menu.roast().ordinal();
        for (int i = 0; i < 3; i++) {
            int sy = y + SWATCH_Y + i * SWATCH_STEP;
            graphics.blit(TEXTURE, x + SWATCH_X, sy, 176 + i * 16, 31, SWATCH_SIZE, SWATCH_SIZE);
            if (i == selected) graphics.blit(TEXTURE, x + SWATCH_X - 1, sy - 1, 176, 47, 18, 18);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int swatch = swatchAt(mouseX, mouseY);
        if (swatch >= 0 && minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, swatch);
            minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
