package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlock;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

/**
 * The spirit you've made, standing in the still's glass spirit safe (the box on the front, 5-11 across, 0-6 high, 0-3 deep
 * when facing north): it rises as the receiver fills.
 */
public class PotStillRenderer implements BlockEntityRenderer<PotStillBlockEntity> {
    public PotStillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PotStillBlockEntity still, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        float fill = still.receiver().getFluidAmount() / (float) PotStillBlockEntity.CAPACITY;
        if (fill <= 0) return;
        Direction facing = still.getBlockState().getValue(PotStillBlock.FACING);
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-((facing.toYRot() + 180) % 360)));   // as the block model turns
        pose.translate(-0.5, 0, -0.5);
        float in = 0.25F / 16;
        FluidRendering.box(pose, buffers, still.receiver().getFluid(), 5 / 16F + in, 1 / 16F, in, 11 / 16F - in,
                (1 + 4.5F * fill) / 16F, 3 / 16F - in, light);
        pose.popPose();
    }
}
