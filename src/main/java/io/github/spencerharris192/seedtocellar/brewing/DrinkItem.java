package io.github.spencerharris192.seedtocellar.brewing;

import io.github.spencerharris192.seedtocellar.client.ClientHooks;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.effect.Intoxication;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Supplier;

/**
 * A served drink: a mug of beer or cider, a bottle of wine, a glass bottle of juice. Carries the same data
 * as its liquid: quality checks ("Brew") and, for drinks that age, "Age" in years and the cask "Wood". Drinking gives its
 * effect (longer at higher quality), adds to tipsiness if it's alcoholic, and gives the empty vessel back.
 */
public class DrinkItem extends Item {
    public static final String AGE = "Age";
    public static final String WOOD = "Wood";
    /** The cask that gave the drink its years was charred. */
    public static final String CHARRED = "Charred";
    public static final int SERVING = 250;

    private final Supplier<Fluid> fluid;
    private final DrinkProfile profile;
    private final Vessel vessel;

    public DrinkItem(Supplier<Fluid> fluid, DrinkProfile profile, Vessel vessel, Properties properties) {
        super(properties);
        this.fluid = fluid;
        this.profile = profile;
        this.vessel = vessel;
    }

    public Vessel vessel() {
        return vessel;
    }

    public Fluid fluid() {
        return fluid.get();
    }

    public DrinkProfile profile() {
        return profile;
    }

    // --- data --------------------------------------------------------------------------------

    public static BrewQuality quality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(BrewQuality.TAG) ? BrewQuality.load(tag.getCompound(BrewQuality.TAG)) : BrewQuality.NONE;
    }

    public static int ageYears(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(AGE);
    }

    /** The serving of liquid this drink holds (same data travels with it). */
    public FluidStack asFluid(ItemStack stack) {
        FluidStack out = new FluidStack(fluid(), SERVING);
        if (stack.getTag() != null) out.setTag(stack.getTag().copy());
        return out;
    }

    /** A drink item holding this serving of liquid, or empty if the liquid isn't drinkable from a mug. */
    public static ItemStack fromFluid(FluidStack serving) {
        return Drinks.byFluid(serving.getFluid()).map(d -> {
            ItemStack stack = new ItemStack(d.item().get());
            if (serving.getTag() != null && !serving.getTag().isEmpty()) stack.setTag(serving.getTag().copy());
            return stack;
        }).orElse(ItemStack.EMPTY);
    }

    /** Spirits go by their age: New Make, White Dog, Eau-de-vie... */
    @Override
    public Component getName(ItemStack stack) {
        String key = Drinks.nameKey(fluid(), stack.getTag());
        return key != null ? Component.translatable(key) : super.getName(stack);
    }

    // --- drinking ----------------------------------------------------------------------------

    /** Sneak and right-click the top of a block to set the drink down there (or beside drinks already set down). */
    @Override
    public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        if (!context.isSecondaryUseActive() || context.getPlayer() == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        net.minecraft.core.BlockPos clicked = context.getClickedPos();
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(clicked);
        ItemStack held = context.getItemInHand();
        boolean placed;
        if (state.getBlock() instanceof io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlock) {
            placed = io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlock.add(level, clicked, state, held);
        } else {
            placed = context.getClickedFace() == net.minecraft.core.Direction.UP && io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlock
                    .place(new net.minecraft.world.item.context.BlockPlaceContext(context), held);
        }
        if (!placed) return InteractionResult.PASS;
        if (!level.isClientSide && !context.getPlayer().getAbilities().instabuild) held.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            int stars = quality(stack).stars();
            if (ModConfigs.SERVER.drinkEffects.get()) {
                player.addEffect(new MobEffectInstance(profile.effect().get(), profile.effectTicks(stars), 0, false, true, true));
                if (quality(stack).crowned()) {   // a crowned whiskey drinks like the golden apple steeped in it
                    player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 2400, 0));
                    player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 1));
                }
            }
            Intoxication.drink(player, profile.units(), stars);
            Drinks.byItem(this).ifPresent(d -> Drinks.cures(d).forEach(effect -> player.removeEffect(effect.get())));
        }
        boolean creative = entity instanceof Player p && p.getAbilities().instabuild;
        ItemStack remaining = super.finishUsingItem(stack, level, entity); // food + shrink
        if (creative) return remaining;
        ItemStack empty = new ItemStack(vessel.empty());
        if (remaining.isEmpty()) return empty;
        if (entity instanceof Player player && !player.getInventory().add(empty)) player.drop(empty, false);
        return remaining;
    }

    // --- tooltip -----------------------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        List<Component> quality = new java.util.ArrayList<>();
        if (profile.graded()) addQualityLines(quality(stack), fluid(), stack.getTag(), quality);
        if (!quality.isEmpty()) tooltip.add(quality.remove(0));   // the stars first, then what it does, then the rest
        tooltip.addAll(effectLines(stack));
        boolean shift = Boolean.TRUE.equals(DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> ClientHooks::shiftDown));
        if (shift) tooltip.add(Component.translatable("tooltip.seedtocellar.set_down").withStyle(ChatFormatting.DARK_GRAY));
        if (profile.alcoholic()) {
            tooltip.add((profile.units() == 1 ? Component.translatable("tooltip.seedtocellar.units_one")
                    : Component.translatable("tooltip.seedtocellar.units", unitsText(profile.units()))).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.addAll(quality);
    }

    /**
     * What drinking it does, as potions show it: its effect for as long as its stars give (a crowned whiskey adds a golden
     * apple's), and what it cures.
     */
    public List<Component> effectLines(ItemStack stack) {
        List<Component> lines = new java.util.ArrayList<>();
        BrewQuality quality = quality(stack);
        lines.add(effectLine(new MobEffectInstance(profile.effect().get(), profile.effectTicks(quality.stars()))));
        if (quality.crowned()) {
            lines.add(effectLine(new MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 2400, 0)));
            lines.add(effectLine(new MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 1)));
        }
        Drinks.byItem(this).map(Drinks::cures).filter(cures -> !cures.isEmpty()).ifPresent(cures -> {
            MutableComponent names = Component.empty();
            for (int i = 0; i < cures.size(); i++) {
                if (i > 0) names.append(i == cures.size() - 1 ? Component.translatable("tooltip.seedtocellar.list_and") : Component.literal(", "));
                names.append(cures.get(i).get().getDisplayName());
            }
            lines.add(Component.translatable("tooltip.seedtocellar.cures", names).withStyle(ChatFormatting.BLUE));
        });
        return lines;
    }

    private static Component effectLine(MobEffectInstance effect) {
        MutableComponent name = Component.translatable(effect.getDescriptionId());
        if (effect.getAmplifier() > 0) {
            name = Component.translatable("potion.withAmplifier", name, Component.translatable("potion.potency." + effect.getAmplifier()));
        }
        return Component.translatable("potion.withDuration", name, net.minecraft.world.effect.MobEffectUtil.formatDuration(effect, 1F))
                .withStyle(effect.getEffect().getCategory().getTooltipFormatting());
    }

    /** "1", "1.5", "3". */
    private static String unitsText(float units) {
        return units == Math.round(units) ? Integer.toString(Math.round(units)) : Float.toString(units);
    }

    /**
     * Star line, age (and the cask wood that gave it), and (holding Shift) the quality checklist and the woods the
     * drink ages best in. {@code data} holds the drink's Age and Wood. Shared with buckets and casks.
     */
    public static void addQualityLines(BrewQuality quality, Fluid fluid, @Nullable CompoundTag data, List<Component> tooltip) {
        tooltip.add(stars(quality.stars()));
        int years = data == null ? 0 : data.getInt(AGE);
        if (years > 0) {
            tooltip.add(CaskWood.byId(data.getString(WOOD))
                    .map(wood -> Component.translatable("tooltip.seedtocellar.aged_years_in", years, woodName(wood, data.getBoolean(CHARRED))))
                    .orElseGet(() -> Component.translatable("tooltip.seedtocellar.aged_years", years)).withStyle(ChatFormatting.GRAY));
        }
        DrinkProfile profile = Drinks.byFluid(fluid).map(Drinks.Drink::profile).orElse(null);
        boolean ageable = profile != null && profile.ageable();
        int runs = data == null ? 0 : data.getInt(CraftStep.RUNS);
        if (runs > 0) {
            tooltip.add(Component.translatable(runs == 1 ? "tooltip.seedtocellar.distilled_once" : "tooltip.seedtocellar.distilled_times", runs)
                    .withStyle(ChatFormatting.GRAY));
        }
        boolean shift = Boolean.TRUE.equals(DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> ClientHooks::shiftDown));
        if (shift) {
            tooltip.add(check(quality.yeast(), "tooltip.seedtocellar.check.yeast"));
            tooltip.add(check(quality.temperature(), "tooltip.seedtocellar.check.temperature"));
            tooltip.add(check(quality.craft(), (profile == null ? CraftStep.CONDITIONED : profile.craft()).checkKey()));
            if (ageable) {
                tooltip.add(check(quality.aged(), "tooltip.seedtocellar.check.aged"));
                tooltip.add(Component.translatable("tooltip.seedtocellar.ideal_woods", idealCasks(profile), profile.agingYears())
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
            if (quality.crowned()) {   // shown only once earned: the secret stays one
                tooltip.add(Component.literal("♔ ").withStyle(ChatFormatting.YELLOW)
                        .append(Component.translatable("tooltip.seedtocellar.check.crowned").withStyle(ChatFormatting.GOLD)));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.seedtocellar.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    /** "Oak", or "Charred Oak". */
    public static Component woodName(CaskWood wood, boolean charred) {
        return charred ? Component.translatable("wood.seedtocellar.charred", wood.displayName()) : wood.displayName();
    }

    /** "Oak and Birch", "Oak and Dark Oak, or any charred cask", "Charred Oak". */
    public static Component idealCasks(DrinkProfile profile) {
        if (profile.charring() == DrinkProfile.Charring.REQUIRED) {
            MutableComponent line = Component.empty();
            List<CaskWood> woods = profile.woods();
            for (int i = 0; i < woods.size(); i++) {
                if (i > 0) line.append(i == woods.size() - 1 ? Component.translatable("tooltip.seedtocellar.list_and") : Component.literal(", "));
                line.append(woodName(woods.get(i), true));
            }
            return line;
        }
        Component list = woodList(profile.woods());
        return profile.charring() == DrinkProfile.Charring.IDEAL ? Component.translatable("tooltip.seedtocellar.or_charred", list) : list;
    }

    /** "Oak, Birch and Acacia". */
    public static Component woodList(List<CaskWood> woods) {
        MutableComponent line = Component.empty();
        for (int i = 0; i < woods.size(); i++) {
            if (i > 0) line.append(i == woods.size() - 1 ? Component.translatable("tooltip.seedtocellar.list_and") : Component.literal(", "));
            line.append(woods.get(i).displayName());
        }
        return line;
    }

    public static MutableComponent stars(int count) {
        int gold = Math.min(count, BrewQuality.PERFECT);
        MutableComponent line = Component.literal("★".repeat(gold)).withStyle(ChatFormatting.GOLD);
        line.append(Component.literal("☆".repeat(BrewQuality.PERFECT - gold)).withStyle(ChatFormatting.DARK_GRAY));
        if (count > BrewQuality.PERFECT) line.append(Component.literal("★").withStyle(ChatFormatting.YELLOW));   // the crown's star
        return line;
    }

    /** A crowned bottle shimmers like an enchanted golden apple, and its name shows it. */
    @Override
    public boolean isFoil(ItemStack stack) {
        return quality(stack).crowned() || super.isFoil(stack);
    }

    @Override
    public net.minecraft.world.item.Rarity getRarity(ItemStack stack) {
        return quality(stack).crowned() ? net.minecraft.world.item.Rarity.EPIC : super.getRarity(stack);
    }

    private static Component check(boolean ok, String key) {
        return Component.literal(ok ? "✔ " : "✘ ").withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED)
                .append(Component.translatable(key).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new VesselFluidHandler(stack);
    }
}
