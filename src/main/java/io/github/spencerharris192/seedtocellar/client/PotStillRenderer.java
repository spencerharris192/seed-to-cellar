package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlock;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The spirit you've made, standing in the still's glass spirit safe (the box on the front, 5-11 across, 0-6 high, 0-3 deep
 * when facing north): it rises as the receiver fills.
 */
public class PotStillRenderer implements BlockEntityRenderer<PotStillBlockEntity, PotStillRenderer.State> {
    public static class State extends VesselRenderers.State {
        public Direction facing = Direction.NORTH;
    }

    public PotStillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PotStillBlockEntity still, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(still, state, partialTick, camera, breaking);
        state.fluid = still.receiver().getFluid().copy();
        state.fill = still.receiver().getFluidAmount() / (float) PotStillBlockEntity.CAPACITY;
        state.facing = still.getBlockState().getValue(PotStillBlock.FACING);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.fill <= 0) return;
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        pose.rotateDegrees(Axis.YP, -((state.facing.toYRot() + 180) % 360));   // as the block model turns
        pose.translate(-0.5, 0, -0.5);
        float in = 0.25F / 16;
        FluidRendering.box(pose, collector, state.fluid, 5 / 16F + in, 1 / 16F, in, 11 / 16F - in,
                (1 + 4.5F * state.fill) / 16F, 3 / 16F - in, state.lightCoords);
        pose.popPose();
    }
}
