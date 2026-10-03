package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.VesselBucketItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Brewing liquids: real Forge fluids, so pipes from Create, Mekanism, Immersive Engineering
 * and others can move them. They have buckets but no world block: they live in vessels only.
 * Details like malt bill or quality ride along as data on the fluid stack.
 */
public final class ModFluids {
    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, SeedToCellar.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, SeedToCellar.MOD_ID);

    private static final List<Entry> ALL = new ArrayList<>();

    // Wort (0xAARRGGBB tints over a shared grey liquid texture)
    public static final Entry SWEET_WORT = register("sweet_wort", 0xF0A0621E, 330);
    public static final Entry HOPPED_WORT = register("hopped_wort", 0xF0906826, 330);
    // Beers
    public static final Entry PALE_ALE = register("pale_ale", 0xF0DDA137, 300);
    public static final Entry AMBER_ALE = register("amber_ale", 0xF0BA6A20, 300);
    public static final Entry STOUT = register("stout", 0xFA2C1911, 300);
    public static final Entry OLD_ALE = register("old_ale", 0xF4823A16, 300);
    public static final Entry PLAIN_ALE = register("plain_ale", 0xE8C99A55, 300);
    public static final Entry TABLE_BEER = register("table_beer", 0xE0E3C57A, 300);
    // Kitchen
    public static final Entry VINEGAR = register("vinegar", 0xE89C6A2E, 300);
    // Winery: must and juices (from the Crushing Tub and Fruit Press), oil and syrups' juices
    public static final Entry RED_GRAPE_MUST = register("red_grape_must", 0xF4481030, 300);
    public static final Entry RED_GRAPE_JUICE = register("red_grape_juice", 0xECA0304C, 300);
    public static final Entry WHITE_GRAPE_JUICE = register("white_grape_juice", 0xE4D8D070, 300);
    public static final Entry APPLE_JUICE = register("apple_juice", 0xE8E0A040, 300);
    public static final Entry PEAR_JUICE = register("pear_juice", 0xE4E4DC90, 300);
    public static final Entry CHERRY_JUICE = register("cherry_juice", 0xF08E1828, 300);
    public static final Entry PLUM_JUICE = register("plum_juice", 0xF0642454, 300);
    public static final Entry PEACH_JUICE = register("peach_juice", 0xECF0A460, 300);
    public static final Entry ORANGE_JUICE = register("orange_juice", 0xF0F09424, 300);
    public static final Entry LEMON_JUICE = register("lemon_juice", 0xE4F0E464, 300);
    public static final Entry BLUEBERRY_JUICE = register("blueberry_juice", 0xF0402A6C, 300);
    public static final Entry BLACKBERRY_JUICE = register("blackberry_juice", 0xF038183C, 300);
    public static final Entry ELDERBERRY_JUICE = register("elderberry_juice", 0xF2301028, 300);
    public static final Entry CRANBERRY_JUICE = register("cranberry_juice", 0xF0B02434, 300);
    public static final Entry SWEET_BERRY_JUICE = register("sweet_berry_juice", 0xF0C42C3C, 300);
    public static final Entry GLOW_BERRY_JUICE = register("glow_berry_juice", 0xECF4B034, 300);
    public static final Entry MELON_JUICE = register("melon_juice", 0xE8F06464, 300);
    public static final Entry OLIVE_OIL = register("olive_oil", 0xECB4A420, 300);
    public static final Entry SORGHUM_JUICE = register("sorghum_juice", 0xE4B48444, 300);
    // Winery: honey water for mead, then the wines, ciders and meads (GDD section 10.2)
    public static final Entry HONEY_WATER = register("honey_water", 0xE0E8C060, 300);
    public static final Entry RED_WINE = register("red_wine", 0xF45A0C1E, 300);
    public static final Entry WHITE_WINE = register("white_wine", 0xE0E8DC8C, 300);
    public static final Entry ROSE = register("rose", 0xE8F08C96, 300);
    public static final Entry CIDER = register("cider", 0xE8E8B44C, 300);
    public static final Entry PERRY = register("perry", 0xE4ECE0A0, 300);
    public static final Entry MEAD = register("mead", 0xECE8A834, 300);
    public static final Entry CHERRY_WINE = register("cherry_wine", 0xF27A1024, 300);
    public static final Entry PLUM_WINE = register("plum_wine", 0xF058204C, 300);
    public static final Entry PEACH_WINE = register("peach_wine", 0xE8F0B470, 300);
    public static final Entry BLUEBERRY_WINE = register("blueberry_wine", 0xF038245C, 300);
    public static final Entry BLACKBERRY_WINE = register("blackberry_wine", 0xF2301430, 300);
    public static final Entry ELDERBERRY_WINE = register("elderberry_wine", 0xF4280C20, 300);
    public static final Entry CRANBERRY_WINE = register("cranberry_wine", 0xF09C1C2C, 300);
    public static final Entry SWEET_BERRY_WINE = register("sweet_berry_wine", 0xF0AC2034, 300);
    public static final Entry GLOW_BERRY_WINE = register("glow_berry_wine", 0xECF0A42C, 300);
    public static final Entry MELON_WINE = register("melon_wine", 0xE8EC7478, 300);
    public static final Entry MULLED_WINE = register("mulled_wine", 0xF4701418, 330);
    // Kitchen soft drinks (GDD section 10.5)
    public static final Entry LEMONADE = register("lemonade", 0xE4F8F0A8, 300);
    public static final Entry ELDERFLOWER_CORDIAL = register("elderflower_cordial", 0xE4ECE49C, 300);
    // Distillery (GDD sections 9.4, 10.3): washes to distil, the still's leftovers, and the spirits
    public static final Entry CORN_WASH = register("corn_wash", 0xE8D8B060, 300);
    public static final Entry POTATO_WASH = register("potato_wash", 0xE4CCC090, 300);
    public static final Entry STILLAGE = register("stillage", 0xEC7A6440, 320);
    public static final Entry MALT_WHISKEY = register("malt_whiskey", 0xC0F0E2B8, 300);
    public static final Entry BOURBON = register("bourbon", 0xC0F4E4BC, 300);
    public static final Entry VODKA = register("vodka", 0x9CF4F8FA, 300);
    public static final Entry POMACE_MASH = register("pomace_mash", 0xE8784060, 300);
    public static final Entry POMACE_WASH = register("pomace_wash", 0xE4A0705C, 300);
    public static final Entry BRANDY = register("brandy", 0xB8F4ECD0, 300);
    public static final Entry APPLE_BRANDY = register("apple_brandy", 0xB8F4F0D8, 300);
    public static final Entry PEAR_BRANDY = register("pear_brandy", 0xB0F4F4E4, 300);
    public static final Entry KIRSCH = register("kirsch", 0xA8F4F4F4, 300);
    public static final Entry SLIVOVITZ = register("slivovitz", 0xB0F4F0E0, 300);
    public static final Entry PEACH_BRANDY = register("peach_brandy", 0xB0F8F0DC, 300);
    public static final Entry GRAPPA = register("grappa", 0xA8F4F4EC, 300);
    public static final Entry CANE_JUICE = register("cane_juice", 0xE4C8D488, 300);
    public static final Entry MOLASSES = register("molasses", 0xFA2E160C, 320);
    public static final Entry RUM_WASH = register("rum_wash", 0xEC7A4A24, 300);
    public static final Entry RUM = register("rum", 0xB0F4F0E4, 300);
    public static final Entry AGAVE_JUICE = register("agave_juice", 0xE8D0A458, 300);
    public static final Entry AGAVE_WASH = register("agave_wash", 0xE4C0A068, 300);
    public static final Entry TEQUILA = register("tequila", 0xA8F0F4F4, 300);
    public static final Entry GIN = register("gin", 0x9CF0F8F4, 300);
    // Liqueurs and bitters, steeped in the Preserving Jar (GDD section 10.4)
    public static final Entry LIMONCELLO = register("limoncello", 0xF4F4E048, 300);
    public static final Entry ORANGE_LIQUEUR = register("orange_liqueur", 0xECF0A030, 300);
    public static final Entry UMESHU = register("umeshu", 0xE8D8A048, 300);
    public static final Entry CHERRY_LIQUEUR = register("cherry_liqueur", 0xF0A01830, 300);
    public static final Entry CREME_DE_MURE = register("creme_de_mure", 0xF4401838, 300);
    public static final Entry ELDERFLOWER_LIQUEUR = register("elderflower_liqueur", 0xE0ECE8B0, 300);
    public static final Entry HERBAL_LIQUEUR = register("herbal_liqueur", 0xF06A7A28, 300);
    public static final Entry SPICED_RUM = register("spiced_rum", 0xF0A85A1C, 300);
    public static final Entry AROMATIC_BITTERS = register("aromatic_bitters", 0xF8501410, 300);
    public static final Entry COFFEE_LIQUEUR = register("coffee_liqueur", 0xFA2A140C, 300);
    /** Apple Crown Whiskey: malt whiskey steeped with apples and honey, a deep apple-gold. */
    public static final Entry APPLE_CROWN_WHISKEY = register("apple_crown_whiskey", 0xF2D0862A, 300);
    // Sake, lager, wheat beer, ginger beer and coffee (GDD sections 9.3, 10.1, 10.2, 10.5)
    public static final Entry SAKE_MASH = register("sake_mash", 0xE4ECE8D8, 300);
    public static final Entry SAKE = register("sake", 0xC8F4F2E4, 300);
    public static final Entry LAGER = register("lager", 0xE8E8C458, 300);
    public static final Entry WHEAT_BEER = register("wheat_beer", 0xE8F0D27A, 300);
    public static final Entry GINGER_BEER = register("ginger_beer", 0xE0E4C878, 300);
    public static final Entry COFFEE = register("coffee", 0xFA2E1A10, 330);

    public static List<Entry> all() {
        return Collections.unmodifiableList(ALL);
    }

    /** One fluid: its type, still + flowing forms, and bucket. */
    public static final class Entry {
        public final String name;
        public final int tint;
        public final RegistryObject<FluidType> type;
        public final RegistryObject<ForgeFlowingFluid.Source> still;
        public final RegistryObject<ForgeFlowingFluid.Flowing> flowing;
        public final RegistryObject<Item> bucket;

        private Entry(String name, int tint, int temperature) {
            this.name = name;
            this.tint = tint;
            this.type = TYPES.register(name, () -> new BrewFluidType(FluidType.Properties.create()
                    .descriptionId("fluid." + SeedToCellar.MOD_ID + "." + name)
                    .temperature(temperature)
                    .canSwim(false).canDrown(false).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY), tint));
            ForgeFlowingFluid.Properties[] props = new ForgeFlowingFluid.Properties[1];
            this.still = FLUIDS.register(name, () -> new ForgeFlowingFluid.Source(props[0]));
            this.flowing = FLUIDS.register(name + "_flowing", () -> new ForgeFlowingFluid.Flowing(props[0]));
            this.bucket = ModItems.ITEMS.register(name + "_bucket", () -> new VesselBucketItem(still,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
            props[0] = new ForgeFlowingFluid.Properties(type, still, flowing).bucket(bucket);
        }

        public Fluid get() {
            return still.get();
        }
    }

    private static Entry register(String name, int tint, int temperature) {
        Entry entry = new Entry(name, tint, temperature);
        ALL.add(entry);
        return entry;
    }

    /**
     * Fluid type whose client look (shared texture + tint) is set up in client code.
     * FluidType's constructor calls initializeClient before our fields are assigned,
     * so the client side must read {@link #tint} when drawing, not when set up.
     */
    public static class BrewFluidType extends FluidType {
        public final int tint;

        BrewFluidType(Properties properties, int tint) {
            super(properties);
            this.tint = tint;
        }

        @Override
        public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
            consumer.accept(io.github.spencerharris192.seedtocellar.client.BrewFluidClient.extensions(this));
        }

        /** Spirits go by their age ("New Make", "White Dog") in tanks, JEI and read-outs too. */
        @Override
        public String getDescriptionId(net.minecraftforge.fluids.FluidStack stack) {
            String key = io.github.spencerharris192.seedtocellar.brewing.Drinks.nameKey(stack.getFluid(), stack.getTag());
            return key != null ? key : super.getDescriptionId(stack);
        }

        /** Filled buckets keep the liquid's data (quality, malt bill) so nothing is lost in a bucket. */
        @Override
        public net.minecraft.world.item.ItemStack getBucket(net.minecraftforge.fluids.FluidStack stack) {
            net.minecraft.world.item.ItemStack bucket = super.getBucket(stack);
            if (stack.hasTag()) bucket.getOrCreateTag().put(VesselBucketItem.FLUID_TAG, stack.getTag().copy());
            return bucket;
        }
    }

    private ModFluids() {}
}
