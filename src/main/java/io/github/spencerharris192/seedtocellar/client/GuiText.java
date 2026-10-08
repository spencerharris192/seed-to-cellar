package io.github.spencerharris192.seedtocellar.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/** Status text that stays inside its box: long lines wrap, and two-line text is split evenly. */
final class GuiText {
    static final int LINE_HEIGHT = 9;

    /** Draws {@code text} wrapped to {@code width} and returns how many lines it took. */
    static int draw(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int width, int color) {
        List<FormattedCharSequence> lines = wrap(font, text, width);
        for (int i = 0; i < lines.size(); i++) {
            graphics.text(font, lines.get(i), x, y + i * LINE_HEIGHT, color, false);
        }
        return lines.size();
    }

    static List<FormattedCharSequence> wrap(Font font, Component text, int width) {
        List<FormattedCharSequence> lines = font.split(text, width);
        if (lines.size() != 2) return lines;
        // Two lines: pick the break that makes them closest in length, so "Add grist or / ingredients"
        // reads as a pair instead of a long line and one stray word.
        String s = text.getString();
        Style style = text.getStyle();
        int best = -1, bestWidth = Integer.MAX_VALUE;
        for (int i = s.indexOf(' '); i >= 0; i = s.indexOf(' ', i + 1)) {
            int wider = Math.max(font.width(s.substring(0, i)), font.width(s.substring(i + 1)));
            if (wider <= width && wider < bestWidth) {
                best = i;
                bestWidth = wider;
            }
        }
        if (best < 0) return lines;
        return List.of(Component.literal(s.substring(0, best)).withStyle(style).getVisualOrderText(),
                Component.literal(s.substring(best + 1)).withStyle(style).getVisualOrderText());
    }

    private GuiText() {}
}
