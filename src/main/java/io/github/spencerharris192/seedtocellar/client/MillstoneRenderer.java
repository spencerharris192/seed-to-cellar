package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.station.MillstoneBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.data.ModelData;

/** Draws the millstone's upper (runner) stone and handle, turning as it is cranked. */
public class MillstoneRenderer implements BlockEntityRenderer<MillstoneBlockEntity> {
    public static final ResourceLocation RUNNER = SeedToCellar.id("block/millstone_runner");

    public MillstoneRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MillstoneBlockEntity mill, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(RUNNER);
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(mill.stoneAngle(partialTick)));
        pose.translate(-0.5, 0, -0.5);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(pose.last(),
                buffers.getBuffer(RenderType.solid()), null, model, 1F, 1F, 1F, light, overlay, ModelData.EMPTY, RenderType.solid());
        pose.popPose();
    }
}
