package io.github.spencerharris192.seedtocellar.client;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewData;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;

/** Client-only look of our fluids: one shared grey animated texture, tinted per fluid. */
public final class BrewFluidClient {
    public static final Identifier STILL = SeedToCellar.id("block/liquid_still");
    public static final Identifier FLOW = SeedToCellar.id("block/liquid_flow");

    /**
     * A fluid's color: its own tint (with the see-through of its alpha) in the world; spirits deepen as they age. Held in a
     * bucket, bottle or gauge it's drawn solid.
     */
    public record BrewTint(int base) implements FluidTintSource {
        @Override
        public int color(FluidState state) {
            return base;
        }

        @Override
        public int colorAsStack(FluidStack stack) {
            return ARGB.opaque(withAlpha(stack));
        }

        /** The liquid's color at its age, alpha included. */
        public int withAlpha(FluidStack stack) {
            return Drinks.tint(stack.getFluid(), BrewData.orNull(stack), base);
        }
    }

    public static void register(RegisterFluidModelsEvent event) {
        for (ModFluids.Entry fluid : ModFluids.all()) {
            // see-through whatever the texture's own alpha, like water
            event.register(new FluidModel.Unbaked(new Material(STILL, true), new Material(FLOW, true), null, new BrewTint(fluid.tint)),
                    fluid.still, fluid.flowing);
        }
    }

    private BrewFluidClient() {}
}
