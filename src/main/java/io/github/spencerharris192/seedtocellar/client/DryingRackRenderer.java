package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.farming.DryingRackBlock;
import io.github.spencerharris192.seedtocellar.farming.DryingRackBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws what hangs on a Drying Rack: two items under the top rail, two under the lower one,
 * flat and facing the front like items in a frame (visible from both sides).
 */
public class DryingRackRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {
    // Spot centers in block pixels (x across the rails, y height), for a rack facing north.
    private static final float[][] SPOTS = {{5.5F, 10.5F}, {10.5F, 10.5F}, {5.5F, 4.0F}, {10.5F, 4.0F}};
    private static final float SCALE = 0.375F;

    public DryingRackRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DryingRackBlockEntity rack, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        float facing = rack.getBlockState().getValue(DryingRackBlock.FACING).toYRot();
        for (int slot = 0; slot < DryingRackBlockEntity.SLOTS; slot++) {
            ItemStack stack = rack.stack(slot);
            if (stack.isEmpty()) continue;
            pose.pushPose();
            pose.translate(0.5, 0, 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(-facing));
            pose.translate(-0.5 + SPOTS[slot][0] / 16.0, SPOTS[slot][1] / 16.0, 0);
            pose.scale(SCALE, SCALE, SCALE);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light,
                    OverlayTexture.NO_OVERLAY, pose, buffers, rack.getLevel(), (int) rack.getBlockPos().asLong() + slot);
            pose.popPose();
        }
    }
}
