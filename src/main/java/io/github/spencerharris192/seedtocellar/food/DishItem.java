package io.github.spencerharris192.seedtocellar.food;

import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * A Kitchen food (GDD section 15) that may come in a container and may cure a Hangover:
 * <ul>
 *   <li>With a container (a bowl for soups and stews, a glass bottle for jam), eating it gives the
 *       container back, and so does using it in a crafting recipe (jam on toast, rice balls).</li>
 *   <li>Curing foods (sourdough bread, porridge) end a Hangover: a proper breakfast.</li>
 * </ul>
 */
public class DishItem extends Item {
    @Nullable private final Supplier<Item> container;
    private final boolean curesHangover;

    public DishItem(Properties properties, @Nullable Supplier<Item> container, boolean curesHangover) {
        super(properties);
        this.container = container;
        this.curesHangover = curesHangover;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (curesHangover && !level.isClientSide) entity.removeEffect(ModEffects.HANGOVER.get());
        ItemStack left = super.finishUsingItem(stack, level, entity);
        if (container == null || entity instanceof Player player && player.getAbilities().instabuild) return left;
        ItemStack empty = new ItemStack(container.get());
        if (left.isEmpty()) return empty;
        if (entity instanceof Player player && !player.getInventory().add(empty)) player.drop(empty, false);
        return left;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return container != null;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return container == null ? ItemStack.EMPTY : new ItemStack(container.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        if (curesHangover) lines.add(Component.translatable("tooltip.seedtocellar.cures_hangover").withStyle(ChatFormatting.BLUE));
    }
}
