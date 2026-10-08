package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.FermentingVatBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.PreservingJarBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

/** Liquid levels you can see in the kettle, the open vat, and the glass jar. */
public final class VesselRenderers {
    /** What a vessel shows: its liquid and how full it is (0-1), and for the jar the items resting inside. */
    public static class State extends BlockEntityRenderState {
        public FluidStack fluid = FluidStack.EMPTY;
        public float fill;
        public final ItemStackRenderState[] items = new ItemStackRenderState[PreservingJarBlockEntity.SLOTS];
    }

    public static class Kettle implements BlockEntityRenderer<BrewKettleBlockEntity, State> {
        public Kettle(BlockEntityRendererProvider.Context context) {
        }

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(BrewKettleBlockEntity kettle, State state, float partialTick, Vec3 camera,
                                       ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
            BlockEntityRenderer.super.extractRenderState(kettle, state, partialTick, camera, breaking);
            state.fluid = kettle.tank().getFluid().copy();
            state.fill = kettle.tank().getFluidAmount() / (float) BrewKettleBlockEntity.CAPACITY;
        }

        @Override
        public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            if (state.fill <= 0) return;
            FluidRendering.surface(pose, collector, state.fluid, 2 / 16F, 2 / 16F, 14 / 16F, 14 / 16F, (2 + 9 * state.fill) / 16F, state.lightCoords);
        }
    }

    public static class Vat implements BlockEntityRenderer<FermentingVatBlockEntity, State> {
        public Vat(BlockEntityRendererProvider.Context context) {
        }

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(FermentingVatBlockEntity vat, State state, float partialTick, Vec3 camera,
                                       ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
            BlockEntityRenderer.super.extractRenderState(vat, state, partialTick, camera, breaking);
            state.fluid = vat.isOpen() ? vat.tank().getFluid().copy() : FluidStack.EMPTY;   // the lid hides it
            state.fill = vat.isOpen() ? vat.tank().getFluidAmount() / (float) FermentingVatBlockEntity.CAPACITY : 0;
        }

        @Override
        public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            if (state.fill <= 0) return;
            FluidRendering.surface(pose, collector, state.fluid, 2 / 16F, 2 / 16F, 14 / 16F, 14 / 16F, (1 + 13 * state.fill) / 16F, state.lightCoords);
        }
    }

    public static class Jar implements BlockEntityRenderer<PreservingJarBlockEntity, State> {
        private final ItemModelResolver items;

        public Jar(BlockEntityRendererProvider.Context context) {
            this.items = context.itemModelResolver();
        }

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(PreservingJarBlockEntity jar, State state, float partialTick, Vec3 camera,
                                       ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
            BlockEntityRenderer.super.extractRenderState(jar, state, partialTick, camera, breaking);
            state.fluid = jar.tank().getFluid().copy();
            state.fill = jar.tank().getFluidAmount() / (float) PreservingJarBlockEntity.CAPACITY;
            for (int i = 0; i < PreservingJarBlockEntity.SLOTS; i++) {
                ItemStack stack = jar.items().getStackInSlot(i);
                if (stack.isEmpty()) {
                    state.items[i] = null;
                    continue;
                }
                if (state.items[i] == null) state.items[i] = new ItemStackRenderState();
                items.updateForTopItem(state.items[i], stack, ItemDisplayContext.FIXED, jar.getLevel(), null, (int) jar.getBlockPos().asLong() + i);
            }
        }

        @Override
        public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            // The liquid fills the glass body (4-12 across, 0-9 high) as a solid block of color,
            // just inside the glass so the two never flicker against each other.
            if (state.fill > 0) {
                float in = 4.25F / 16, out = 11.75F / 16;
                FluidRendering.box(pose, collector, state.fluid, in, 0.25F / 16, in, out, (0.25F + 8.5F * state.fill) / 16, out, state.lightCoords);
            }
            // items resting inside the jar, small and slightly tilted
            for (int i = 0; i < state.items.length; i++) {
                if (state.items[i] == null || state.items[i].isEmpty()) continue;
                pose.pushPose();
                pose.translate(0.5 + (i % 2 == 0 ? -0.07 : 0.07), 0.12 + i * 0.1, 0.5 + (i < 2 ? -0.06 : 0.06));
                pose.rotateDegrees(Axis.YP, i * 50);
                pose.rotateDegrees(Axis.XP, 70);
                pose.scale(0.25F, 0.25F, 0.25F);
                state.items[i].submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                pose.popPose();
            }
        }
    }

    private VesselRenderers() {}
}
