package io.github.spencerharris192.seedtocellar.client;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

/** Client-only look of our fluids: one shared grey animated texture, tinted per fluid. */
public final class BrewFluidClient {
    public static final ResourceLocation STILL = SeedToCellar.id("block/liquid_still");
    public static final ResourceLocation FLOW = SeedToCellar.id("block/liquid_flow");

    /**
     * Reads the tint from the type on every call. This is created while the type is still being
     * constructed, before its tint field is set, so copying the tint here would capture 0 (black).
     */
    public static IClientFluidTypeExtensions extensions(ModFluids.BrewFluidType type) {
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOW;
            }

            @Override
            public int getTintColor() {
                return type.tint;
            }

            @Override
            public int getTintColor(FluidStack stack) {
                return io.github.spencerharris192.seedtocellar.brewing.Drinks.tint(stack.getFluid(), stack.getTag(), type.tint);   // spirits deepen with age
            }
        };
    }

    private BrewFluidClient() {}
}
