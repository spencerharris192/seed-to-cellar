package io.github.spencerharris192.seedtocellar.datagen;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import io.github.spencerharris192.seedtocellar.registry.ModTriggers;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;

import java.util.function.Consumer;

/**
 * Advancements that teach each chain step by step (GDD section 18.5): farming, brewing (From Seed to Cellar > Malted >
 * Brew Day > First Pint > Four Stars > Aged to Perfection, with Culture Club off First Pint) and winemaking
 * (Stomped > Vintner > Good Year, with Mother off Vintner).
 */
public class ModAdvancements implements ForgeAdvancementProvider.AdvancementGenerator {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<Advancement> saver, ExistingFileHelper files) {
        Advancement root = Advancement.Builder.advancement()
                .display(Crops.BARLEY.produce(), title("root"), desc("root"),
                        SeedToCellar.rl("minecraft", "textures/block/spruce_planks.png"), FrameType.TASK, false, false, false)
                .addCriterion("seeds", InventoryChangeTrigger.TriggerInstance.hasItems(Crops.BARLEY.seeds()))
                .addCriterion("rhizome", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.HOP_RHIZOME.get()))
                .requirements(RequirementsStrategy.OR)
                .save(saver, id("root"), files);

        // Farming branch (GDD section 18.5): First Harvest > Fertile Ground > Botanist.
        Advancement.Builder harvestBuilder = step(root, Crops.RYE.produce(), "first_harvest", FrameType.TASK)
                .requirements(RequirementsStrategy.OR);
        for (Crop crop : Crops.all()) {
            harvestBuilder.addCriterion(crop.name, InventoryChangeTrigger.TriggerInstance.hasItems(crop.produce()));
        }
        Advancement firstHarvest = harvestBuilder.save(saver, farming("first_harvest"), files);
        Advancement fertileGround = step(firstHarvest, ModItems.COMPOST.get(), "fertile_ground", FrameType.TASK)
                .addCriterion("compost", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                        LocationPredicate.Builder.location(), ItemPredicate.Builder.item().of(ModItems.COMPOST.get())))
                .save(saver, farming("fertile_ground"), files);
        Advancement.Builder orchardistBuilder = step(fertileGround, FruitTrees.CHERRY.saplingItem(), "orchardist", FrameType.GOAL);
        for (FruitTree tree : FruitTrees.all()) {
            orchardistBuilder.addCriterion(tree.name, ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(tree.sapling()));
        }
        Advancement orchardist = orchardistBuilder.save(saver, farming("orchardist"), files);
        Advancement.Builder botanistBuilder = step(orchardist, Crops.LAVENDER.produce(), "botanist", FrameType.GOAL);
        for (Crop crop : Crops.all()) {
            if (crop.kind == Crop.Kind.HERB) botanistBuilder.addCriterion(crop.name, InventoryChangeTrigger.TriggerInstance.hasItems(crop.produce()));
        }
        botanistBuilder.save(saver, farming("botanist"), files);

        winemaking(root, saver, files);
        distilling(root, saver, files);
        justForFun(root, saver, files);

        Advancement malted = step(root, ModItems.GREEN_BARLEY_MALT.get(), "malted", FrameType.TASK)
                .addCriterion("green_malt", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.GREEN_BARLEY_MALT.get()))
                .save(saver, id("malted"), files);

        Advancement brewDay = step(malted, ModItems.BREW_KETTLE.get(), "brew_day", FrameType.TASK)
                .addCriterion("spent_grain", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SPENT_GRAIN.get()))
                .save(saver, id("brew_day"), files);

        Advancement.Builder firstPintBuilder = step(brewDay, Drinks.PALE_ALE.item().get(), "first_pint", FrameType.TASK)
                .requirements(RequirementsStrategy.OR);
        for (Drinks.Drink drink : Drinks.all()) {
            firstPintBuilder.addCriterion(drink.name(), ConsumeItemTrigger.TriggerInstance.usedItem(drink.item().get()));
        }
        Advancement firstPint = firstPintBuilder.save(saver, id("first_pint"), files);

        Advancement cultureClub = step(firstPint, ModItems.ALE_YEAST.get(), "culture_club", FrameType.TASK)
                .addCriterion("yeast", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ALE_YEAST.get()))
                .save(saver, id("culture_club"), files);
        // Cold Comfort: a lager, brewed with yeast grown in the cold.
        step(cultureClub, ModItems.LAGER_YEAST.get(), "cold_comfort", FrameType.TASK)
                .addCriterion("lager", InventoryChangeTrigger.TriggerInstance.hasItems(Drinks.LAGER.item().get()))
                .save(saver, id("cold_comfort"), files);

        // Four stars: cultured yeast + right temperature + conditioned.
        CompoundTag fourStars = new CompoundTag();
        fourStars.put(BrewQuality.TAG, new BrewQuality(true, true, true, false).save());
        fourStars.getCompound(BrewQuality.TAG).remove("Aged"); // match whether or not it's also aged
        Advancement fourStarAdv = step(firstPint, Drinks.AMBER_ALE.item().get(), "four_stars", FrameType.GOAL)
                .addCriterion("drink", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(ModTags.Items.DRINKS).hasNbt(fourStars).build()))
                .save(saver, id("four_stars"), files);

        // Every Style: pour every beer there is.
        Advancement.Builder everyStyle = step(fourStarAdv, Drinks.STOUT.item().get(), "every_style", FrameType.CHALLENGE);
        for (Drinks.Drink beer : Drinks.beers()) {
            everyStyle.addCriterion(beer.name(), InventoryChangeTrigger.TriggerInstance.hasItems(beer.item().get()));
        }
        everyStyle.save(saver, id("every_style"), files);

        CompoundTag aged = new CompoundTag();
        CompoundTag agedQuality = new CompoundTag();
        agedQuality.putBoolean("Aged", true);
        aged.put(BrewQuality.TAG, agedQuality);
        step(fourStarAdv, ModItems.OAK_CASK.get(), "aged_to_perfection", FrameType.CHALLENGE)
                .addCriterion("old_ale", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(Drinks.OLD_ALE.item().get()).hasNbt(aged).build()))
                .save(saver, id("aged_to_perfection"), files);
    }

    /** Winemaking (GDD section 18.5): Stomped > Vintner > Good Year; Mother (vinegar) branches off Vintner. */
    private static void winemaking(Advancement root, Consumer<Advancement> saver, ExistingFileHelper files) {
        Advancement stomped = step(root, ModItems.CRUSHING_TUB.get(), "stomped", FrameType.TASK)
                .addCriterion("stomp", new PlayerTrigger.TriggerInstance(ModTriggers.STOMP.getId(), ContextAwarePredicate.ANY))
                .save(saver, winery("stomped"), files);
        Advancement vintner = step(stomped, Drinks.RED_WINE.item().get(), "vintner", FrameType.GOAL)
                .addCriterion("red_wine", InventoryChangeTrigger.TriggerInstance.hasItems(Drinks.RED_WINE.item().get()))
                .addCriterion("white_wine", InventoryChangeTrigger.TriggerInstance.hasItems(Drinks.WHITE_WINE.item().get()))
                .addCriterion("rose", InventoryChangeTrigger.TriggerInstance.hasItems(Drinks.ROSE.item().get()))
                .save(saver, winery("vintner"), files);
        CompoundTag fiveStars = new CompoundTag();
        fiveStars.put(BrewQuality.TAG, new BrewQuality(true, true, true, true).save());
        step(vintner, Drinks.WHITE_WINE.item().get(), "good_year", FrameType.CHALLENGE)
                .addCriterion("wine", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(ModTags.Items.WINES).hasNbt(fiveStars).build()))
                .save(saver, winery("good_year"), files);
        step(vintner, ModItems.MOTHER_OF_VINEGAR.get(), "mother", FrameType.TASK)
                .addCriterion("mother", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MOTHER_OF_VINEGAR.get()))
                .save(saver, winery("mother"), files);
    }

    /** Distilling (GDD section 18.5): First Run > Double Distilled > The Angel's Share > Top Shelf. */
    private static void distilling(Advancement root, Consumer<Advancement> saver, ExistingFileHelper files) {
        Advancement firstRun = step(root, ModItems.POT_STILL.get(), "first_run", FrameType.TASK)
                .addCriterion("spirit", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ModTags.Items.SPIRITS).build()))
                .save(saver, distillery("first_run"), files);
        CompoundTag crafted = new CompoundTag();
        CompoundTag craft = new CompoundTag();
        craft.putBoolean("Craft", true);
        crafted.put(BrewQuality.TAG, craft);
        Advancement doubleDistilled = step(firstRun, Drinks.MALT_WHISKEY.item().get(), "double_distilled", FrameType.TASK)
                .addCriterion("spirit", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(ModTags.Items.SPIRITS).hasNbt(crafted).build()))
                .save(saver, distillery("double_distilled"), files);
        Advancement angelsShare = step(doubleDistilled, ModItems.CASKS.get(io.github.spencerharris192.seedtocellar.brewing.CaskWood.DARK_OAK).get(),
                "angels_share", FrameType.GOAL)
                .addCriterion("pour", new PlayerTrigger.TriggerInstance(ModTriggers.ANGELS_SHARE.getId(), ContextAwarePredicate.ANY))
                .save(saver, distillery("angels_share"), files);
        CompoundTag fiveStars = new CompoundTag();
        fiveStars.put(BrewQuality.TAG, new BrewQuality(true, true, true, true).save());
        Advancement topShelf = step(angelsShare, Drinks.BRANDY.item().get(), "top_shelf", FrameType.CHALLENGE)
                .addCriterion("spirit", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(ModTags.Items.SPIRITS).hasNbt(fiveStars).build()))
                .save(saver, distillery("top_shelf"), files);
        // Long Live the King: the secret six-star Apple Crown Whiskey. Hidden until earned.
        CompoundTag crowned = new CompoundTag();
        CompoundTag crown = new CompoundTag();
        crown.putBoolean("Crowned", true);
        crowned.put(BrewQuality.TAG, crown);
        Advancement.Builder.advancement().parent(topShelf)
                .display(Drinks.APPLE_CROWN_WHISKEY.item().get(), title("long_live_the_king"), desc("long_live_the_king"), null,
                        FrameType.CHALLENGE, true, true, true)
                .addCriterion("crowned", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(Drinks.APPLE_CROWN_WHISKEY.item().get()).hasNbt(crowned).build()))
                .save(saver, distillery("long_live_the_king"), files);
    }

    /** Just for fun (GDD section 18.5): Merry! > Morning After > Tavern Keeper. */
    private static void justForFun(Advancement root, Consumer<Advancement> saver, ExistingFileHelper files) {
        Advancement merry = step(root, Drinks.PALE_ALE.item().get(), "merry", FrameType.TASK)
                .addCriterion("tipsy", net.minecraft.advancements.critereon.EffectsChangedTrigger.TriggerInstance.hasEffects(
                        net.minecraft.advancements.critereon.MobEffectsPredicate.effects().and(
                                io.github.spencerharris192.seedtocellar.effect.ModEffects.TIPSY.get())))
                .save(saver, fun("merry"), files);
        Advancement morningAfter = step(merry, Drinks.AROMATIC_BITTERS.item().get(), "morning_after", FrameType.TASK)
                .addCriterion("cured", new PlayerTrigger.TriggerInstance(ModTriggers.MORNING_AFTER.getId(), ContextAwarePredicate.ANY))
                .save(saver, fun("morning_after"), files);
        step(morningAfter, ModItems.BOTTLE_SHELF.get(), "tavern_keeper", FrameType.GOAL)
                .addCriterion("shelf", new PlayerTrigger.TriggerInstance(ModTriggers.TAVERN_KEEPER.getId(), ContextAwarePredicate.ANY))
                .save(saver, fun("tavern_keeper"), files);
    }

    private static ResourceLocation fun(String key) {
        return SeedToCellar.id("fun/" + key);
    }

    private static Advancement.Builder step(Advancement parent, ItemLike icon, String key, FrameType frame) {
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, title(key), desc(key), null, frame, true, true, false);
    }

    private static Component title(String key) {
        return Component.translatable("advancements." + SeedToCellar.MOD_ID + "." + key + ".title");
    }

    private static Component desc(String key) {
        return Component.translatable("advancements." + SeedToCellar.MOD_ID + "." + key + ".description");
    }

    private static ResourceLocation distillery(String key) {
        return SeedToCellar.id("distilling/" + key);
    }

    private static ResourceLocation winery(String key) {
        return SeedToCellar.id("winemaking/" + key);
    }

    private static ResourceLocation farming(String key) {
        return SeedToCellar.id("farming/" + key);
    }

    private static ResourceLocation id(String key) {
        return SeedToCellar.id("brewing/" + key);
    }
}
