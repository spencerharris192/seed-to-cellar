package io.github.spencerharris192.seedtocellar.food;

import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * A Kitchen food (GDD section 15) that may come in a container and may cure a Hangover:
 * <ul>
 *   <li>With a container (a bowl for soups and stews, a glass bottle for jam), eating it gives the
 *       container back, and so does using it in a crafting recipe (jam on toast, rice balls): the item's properties say
 *       which (ModItems).</li>
 *   <li>Curing foods (sourdough bread, porridge) end a Hangover: a proper breakfast.</li>
 * </ul>
 */
public class DishItem extends Item {
    private final boolean curesHangover;

    public DishItem(Properties properties, boolean curesHangover) {
        super(properties);
        this.curesHangover = curesHangover;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (curesHangover && !level.isClientSide()) entity.removeEffect(ModEffects.HANGOVER);
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (curesHangover) tooltip.accept(Component.translatable("tooltip.seedtocellar.cures_hangover").withStyle(ChatFormatting.BLUE));
    }
}
