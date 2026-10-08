package io.github.spencerharris192.seedtocellar.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;

/**
 * Shared layout for our JEI pages: a fixed-size panel with JEI's own arrow and flame
 * graphics, and helper methods to write a few plain-language lines under the slots.
 */
public abstract class SimpleCategory<T> implements IRecipeCategory<T> {
    protected static final int WIDTH = 160;
    private final RecipeType<T> type;
    private final Component title;
    private final IDrawable icon;
    private final int height;
    protected final IDrawableStatic arrow;
    protected final IDrawableStatic flame;

    protected SimpleCategory(IGuiHelper gui, RecipeType<T> type, String titleKey, ItemLike icon, int height) {
        this.type = type;
        this.title = Component.translatable(titleKey);
        this.icon = gui.createDrawableItemStack(new ItemStack(icon));
        this.height = height;
        this.arrow = gui.getRecipeArrow();
        this.flame = gui.getRecipeFlameFilled();
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return height;
    }

    /** Writes lines of grey text starting at (x, y), 10 px apart. */
    protected static void lines(GuiGraphicsExtractor graphics, int x, int y, List<Component> text) {
        for (int i = 0; i < text.size(); i++) {
            graphics.text(Minecraft.getInstance().font, text.get(i), x, y + i * 10, 0xFF555555, false);
        }
    }

    protected static Component seconds(int ticks) {
        int s = ticks / 20;
        return s >= 60 && s % 60 == 0 ? Component.translatable("jei.seedtocellar.minutes", s / 60) : Component.translatable("jei.seedtocellar.seconds", s);
    }

    /** In-game days, for fermenting and jars (one day = 20 real minutes). */
    protected static Component days(int ticks) {
        float d = ticks / 24000F;
        return Component.translatable("jei.seedtocellar.days", d == Math.floor(d) ? String.valueOf((int) d) : String.valueOf(d));
    }
}
