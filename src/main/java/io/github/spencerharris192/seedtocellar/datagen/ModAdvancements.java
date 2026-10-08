package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.registry.ModComponents;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import io.github.spencerharris192.seedtocellar.registry.ModTriggers;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.MobEffectsPredicate;
import net.minecraft.advancements.triggers.ConsumeItemTrigger;
import net.minecraft.advancements.triggers.EffectsChangedTrigger;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Optional;

/**
 * Advancements that teach each chain step by step (GDD section 18.5): farming, brewing (From Seed to Cellar > Malted >
 * Brew Day > First Pint > Four Stars > Aged to Perfection, with Culture Club off First Pint) and winemaking
 * (Stomped > Vintner > Good Year, with Mother off Vintner). A drink's quality checks are components of their own, so
 * "four stars" asks for the three checks it needs and takes the drink whether or not it's also aged.
 */
public class ModAdvancements extends AdvancementSubProvider {
    private final HolderGetter<Item> items;
    private final HolderGetter<Block> blocks;

    public ModAdvancements(BootstrapContext<Advancement> output) {
        super(output);
        this.items = output.lookup(Registries.ITEM);
        this.blocks = output.lookup(Registries.BLOCK);
    }

    @Override
    public void generate() {
        AdvancementHolder root = Advancement.Builder.advancement()
                .rootDisplay(Crops.BARLEY.produce(), title("root"), desc("root"),
                        SeedToCellar.rl("minecraft", "block/spruce_planks"), AdvancementType.TASK, false, false, false)
                .addCriterion("seeds", InventoryChangeTrigger.TriggerInstance.hasItems(Crops.BARLEY.seeds()))
                .addCriterion("rhizome", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.HOP_RHIZOME.get()))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(output, id("root"));

        // Farming branch (GDD section 18.5): First Harvest > Fertile Ground > Botanist.
        Advancement.Builder harvestBuilder = step(root, Crops.RYE.produce(), "first_harvest", AdvancementType.TASK)
                .requirements(AdvancementRequirements.Strategy.OR);
        for (Crop crop : Crops.all()) {
            harvestBuilder.addCriterion(crop.name, InventoryChangeTrigger.TriggerInstance.hasItems(crop.produce()));
        }
        AdvancementHolder firstHarvest = harvestBuilder.save(output, farming("first_harvest"));
        AdvancementHolder fertileGround = step(firstHarvest, ModItems.COMPOST.get(), "fertile_ground", AdvancementType.TASK)
                .addCriterion("compost", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                        LocationPredicate.Builder.location(), ItemPredicate.Builder.item().of(items, ModItems.COMPOST.get())))
                .save(output, farming("fertile_ground"));
        Advancement.Builder orchardistBuilder = step(fertileGround, FruitTrees.CHERRY.saplingItem(), "orchardist", AdvancementType.GOAL);
        for (FruitTree tree : FruitTrees.all()) {
            orchardistBuilder.addCriterion(tree.name, ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(blocks, tree.sapling()));
        }
        AdvancementHolder orchardist = orchardistBuilder.save(output, farming("orchardist"));
        Advancement.Builder botanistBuilder = step(orchardist, Crops.LAVENDER.produce(), "botanist", AdvancementType.GOAL);
        for (Crop crop : Crops.all()) {
            if (crop.kind == Crop.Kind.HERB) botanistBuilder.addCriterion(crop.name, InventoryChangeTrigger.TriggerInstance.hasItems(crop.produce()));
        }
        botanistBuilder.save(output, farming("botanist"));

        winemaking(root);
        distilling(root);
        justForFun(root);

        AdvancementHolder malted = step(root, ModItems.GREEN_BARLEY_MALT.get(), "malted", AdvancementType.TASK)
                .addCriterion("green_malt", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.GREEN_BARLEY_MALT.get()))
                .save(output, id("malted"));

        AdvancementHolder brewDay = step(malted, ModItems.BREW_KETTLE.get(), "brew_day", AdvancementType.TASK)
                .addCriterion("spent_grain", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SPENT_GRAIN.get()))
                .save(output, id("brew_day"));

        Advancement.Builder firstPintBuilder = step(brewDay, Drinks.PALE_ALE.item().get(), "first_pint", AdvancementType.TASK)
                .requirements(AdvancementRequirements.Strategy.OR);
        for (Drinks.Drink drink : Drinks.all()) {
            firstPintBuilder.addCriterion(drink.name(), ConsumeItemTrigger.TriggerInstance.usedItem(items, drink.item().get()));
        }
        AdvancementHolder firstPint = firstPintBuilder.save(output, id("first_pint"));

        AdvancementHolder cultureClub = step(firstPint, ModItems.ALE_YEAST.get(), "culture_club", AdvancementType.TASK)
                .addCriterion("yeast", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ALE_YEAST.get()))
                .save(output, id("culture_club"));
        // Cold Comfort: a lager, brewed with yeast grown in the cold.
        step(cultureClub, ModItems.LAGER_YEAST.get(), "cold_comfort", AdvancementType.TASK)
                .addCriterion("lager", InventoryChangeTrigger.TriggerInstance.hasItems(Drinks.LAGER.item().get()))
                .save(output, id("cold_comfort"));

        // Four stars: cultured yeast + right temperature + conditioned (aged or not).
        AdvancementHolder fourStarAdv = step(firstPint, Drinks.AMBER_ALE.item().get(), "four_stars", AdvancementType.GOAL)
                .addCriterion("drink", InventoryChangeTrigger.TriggerInstance.hasItems(withChecks(ItemPredicate.Builder.item().of(items, ModTags.Items.DRINKS),
                        ModComponents.QUALITY_YEAST, ModComponents.QUALITY_TEMPERATURE, ModComponents.QUALITY_CRAFT)))
                .save(output, id("four_stars"));

        // Every Style: pour every beer there is.
        Advancement.Builder everyStyle = step(fourStarAdv, Drinks.STOUT.item().get(), "every_style", AdvancementType.CHALLENGE);
        for (Drinks.Drink beer : Drinks.beers()) {
            everyStyle.addCriterion(beer.name(), InventoryChangeTrigger.TriggerInstance.hasItems(beer.item().get()));
        }
        everyStyle.save(output, id("every_style"));

        step(fourStarAdv, ModItems.OAK_CASK.get(), "aged_to_perfection", AdvancementType.CHALLENGE)
                .addCriterion("old_ale", InventoryChangeTrigger.TriggerInstance.hasItems(withChecks(
                        ItemPredicate.Builder.item().of(items, Drinks.OLD_ALE.item().get()), ModComponents.QUALITY_AGED)))
                .save(output, id("aged_to_perfection"));
    }

    /** Winemaking (GDD section 18.5): Stomped > Vintner > Good Year; Mother (vinegar) branches off Vintner. */
    private void winemaking(AdvancementHolder root) {
        AdvancementHolder stomped = step(root, ModItems.CRUSHING_TUB.get(), "stomped", AdvancementType.TASK)
                .addCriterion("stomp", player(ModTriggers.STOMP))
                .save(output, winery("stomped"));
        AdvancementHolder vintner = step(stomped, Drinks.RED_WINE.item().get(), "vintner", AdvancementType.GOAL)
                .addCriterion("red_wine", InventoryChangeTrigger.TriggerInstance.hasItems(Drinks.RED_WINE.item().get()))
                .addCriterion("white_wine", InventoryChangeTrigger.TriggerInstance.hasItems(Drinks.WHITE_WINE.item().get()))
                .addCriterion("rose", InventoryChangeTrigger.TriggerInstance.hasItems(Drinks.ROSE.item().get()))
                .save(output, winery("vintner"));
        step(vintner, Drinks.WHITE_WINE.item().get(), "good_year", AdvancementType.CHALLENGE)
                .addCriterion("wine", InventoryChangeTrigger.TriggerInstance.hasItems(fiveStars(ModTags.Items.WINES)))
                .save(output, winery("good_year"));
        step(vintner, ModItems.MOTHER_OF_VINEGAR.get(), "mother", AdvancementType.TASK)
                .addCriterion("mother", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MOTHER_OF_VINEGAR.get()))
                .save(output, winery("mother"));
    }

    /** Distilling (GDD section 18.5): First Run > Double Distilled > The Angel's Share > Top Shelf. */
    private void distilling(AdvancementHolder root) {
        AdvancementHolder firstRun = step(root, ModItems.POT_STILL.get(), "first_run", AdvancementType.TASK)
                .addCriterion("spirit", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items, ModTags.Items.SPIRITS)))
                .save(output, distillery("first_run"));
        AdvancementHolder doubleDistilled = step(firstRun, Drinks.MALT_WHISKEY.item().get(), "double_distilled", AdvancementType.TASK)
                .addCriterion("spirit", InventoryChangeTrigger.TriggerInstance.hasItems(withChecks(
                        ItemPredicate.Builder.item().of(items, ModTags.Items.SPIRITS), ModComponents.QUALITY_CRAFT)))
                .save(output, distillery("double_distilled"));
        AdvancementHolder angelsShare = step(doubleDistilled, ModItems.CASKS.get(CaskWood.DARK_OAK).get(), "angels_share", AdvancementType.GOAL)
                .addCriterion("pour", player(ModTriggers.ANGELS_SHARE))
                .save(output, distillery("angels_share"));
        AdvancementHolder topShelf = step(angelsShare, Drinks.BRANDY.item().get(), "top_shelf", AdvancementType.CHALLENGE)
                .addCriterion("spirit", InventoryChangeTrigger.TriggerInstance.hasItems(fiveStars(ModTags.Items.SPIRITS)))
                .save(output, distillery("top_shelf"));
        // Long Live the King: the secret six-star Apple Crown Whiskey. Hidden until earned.
        Advancement.Builder.advancement().parent(topShelf)
                .display(Drinks.APPLE_CROWN_WHISKEY.item().get(), title("long_live_the_king"), desc("long_live_the_king"),
                        AdvancementType.CHALLENGE, true, true, true)
                .addCriterion("crowned", InventoryChangeTrigger.TriggerInstance.hasItems(withChecks(
                        ItemPredicate.Builder.item().of(items, Drinks.APPLE_CROWN_WHISKEY.item().get()), ModComponents.QUALITY_CROWNED)))
                .save(output, distillery("long_live_the_king"));
    }

    /** Just for fun (GDD section 18.5): Merry! > Morning After > Tavern Keeper. */
    private void justForFun(AdvancementHolder root) {
        AdvancementHolder merry = step(root, Drinks.PALE_ALE.item().get(), "merry", AdvancementType.TASK)
                .addCriterion("tipsy", EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.Builder.effects().and(ModEffects.TIPSY)))
                .save(output, fun("merry"));
        AdvancementHolder morningAfter = step(merry, Drinks.AROMATIC_BITTERS.item().get(), "morning_after", AdvancementType.TASK)
                .addCriterion("cured", player(ModTriggers.MORNING_AFTER))
                .save(output, fun("morning_after"));
        step(morningAfter, ModItems.BOTTLE_SHELF.get(), "tavern_keeper", AdvancementType.GOAL)
                .addCriterion("shelf", player(ModTriggers.TAVERN_KEEPER))
                .save(output, fun("tavern_keeper"));
    }

    /** Our own trigger, fired for a player (no further conditions). */
    private static Criterion<PlayerTrigger.TriggerInstance> player(DeferredHolder<net.minecraft.advancements.triggers.CriterionTrigger<?>, PlayerTrigger> trigger) {
        return trigger.get().createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
    }

    /** A drink with at least these quality checks (each a marker component: see BrewData). */
    @SafeVarargs
    private static ItemPredicate.Builder withChecks(ItemPredicate.Builder item, DeferredHolder<DataComponentType<?>, DataComponentType<Unit>>... checks) {
        DataComponentExactPredicate.Builder exact = DataComponentExactPredicate.builder();
        for (var check : checks) exact.expect(check.get(), Unit.INSTANCE);
        return item.withComponents(DataComponentMatchers.Builder.components().exact(exact.build()).build());
    }

    /** Any of the tag's drinks with every check passed. */
    private ItemPredicate.Builder fiveStars(TagKey<Item> drinks) {
        return withChecks(ItemPredicate.Builder.item().of(items, drinks), ModComponents.QUALITY_YEAST, ModComponents.QUALITY_TEMPERATURE,
                ModComponents.QUALITY_CRAFT, ModComponents.QUALITY_AGED);
    }

    private static Advancement.Builder step(AdvancementHolder parent, ItemLike icon, String key, AdvancementType frame) {
        return Advancement.Builder.advancement().parent(parent)
                .display(icon.asItem(), title(key), desc(key), frame, true, true, false);
    }

    private static Component title(String key) {
        return Component.translatable("advancements." + SeedToCellar.MOD_ID + "." + key + ".title");
    }

    private static Component desc(String key) {
        return Component.translatable("advancements." + SeedToCellar.MOD_ID + "." + key + ".description");
    }

    private static String fun(String key) {
        return SeedToCellar.id("fun/" + key).toString();
    }

    private static String distillery(String key) {
        return SeedToCellar.id("distilling/" + key).toString();
    }

    private static String winery(String key) {
        return SeedToCellar.id("winemaking/" + key).toString();
    }

    private static String farming(String key) {
        return SeedToCellar.id("farming/" + key).toString();
    }

    private static String id(String key) {
        return SeedToCellar.id("brewing/" + key).toString();
    }
}
