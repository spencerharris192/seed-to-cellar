package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.station.MillstoneBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Draws the millstone's upper (runner) stone and handle, turning as it is cranked. */
public class MillstoneRenderer implements BlockEntityRenderer<MillstoneBlockEntity, MillstoneRenderer.State> {
    public static final Identifier RUNNER = SeedToCellar.id("block/millstone_runner");

    public static class State extends BlockEntityRenderState {
        public float angle;
    }

    public MillstoneRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MillstoneBlockEntity mill, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(mill, state, partialTick, camera, breaking);
        state.angle = mill.stoneAngle(partialTick);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        pose.rotateDegrees(Axis.YP, state.angle);
        pose.translate(-0.5, 0, -0.5);
        ClientModels.submit(RUNNER, pose, collector, state.lightCoords);
        pose.popPose();
    }
}
