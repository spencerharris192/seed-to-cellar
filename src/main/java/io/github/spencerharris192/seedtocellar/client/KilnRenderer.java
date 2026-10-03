package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.brewing.station.KilnBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Shows what is on the kiln's top grate: the batch being roasted, or the finished result.
 * Up to four copies in a 2x2 grid, each in its own square with a gap between, turned only in
 * quarter turns (like the campfire) so no two ever overlap.
 */
public class KilnRenderer implements BlockEntityRenderer<KilnBlockEntity> {
    private static final float[][] SPOTS = {{0.29F, 0.29F}, {0.71F, 0.29F}, {0.29F, 0.71F}, {0.71F, 0.71F}};
    private static final float SCALE = 0.375F;

    public KilnRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(KilnBlockEntity kiln, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack shown = kiln.items().getStackInSlot(KilnBlockEntity.INPUT);
        if (shown.isEmpty()) shown = kiln.items().getStackInSlot(KilnBlockEntity.OUTPUT);
        if (shown.isEmpty()) return;
        // Light the items with the air above the grate. The light passed in is from inside the
        // kiln, a solid block, which is pitch dark whenever the fire is out.
        if (kiln.getLevel() != null) light = LevelRenderer.getLightColor(kiln.getLevel(), kiln.getBlockPos().above());
        int copies = Math.min(SPOTS.length, (shown.getCount() + 3) / 4);
        for (int i = 0; i < copies; i++) {
            pose.pushPose();
            pose.translate(SPOTS[i][0], 1.02, SPOTS[i][1]);
            pose.mulPose(Axis.XP.rotationDegrees(90));
            pose.mulPose(Axis.ZP.rotationDegrees(i * 90));
            pose.scale(SCALE, SCALE, SCALE);
            Minecraft.getInstance().getItemRenderer().renderStatic(shown, ItemDisplayContext.FIXED, light,
                    OverlayTexture.NO_OVERLAY, pose, buffers, kiln.getLevel(), (int) kiln.getBlockPos().asLong() + i);
            pose.popPose();
        }
    }
}
