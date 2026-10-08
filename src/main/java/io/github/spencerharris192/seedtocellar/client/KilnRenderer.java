package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.brewing.station.KilnBlockEntity;
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
import org.jspecify.annotations.Nullable;
import net.minecraft.util.LightCoordsUtil;

/**
 * Shows what is on the kiln's top grate: the batch being roasted, or the finished result.
 * Up to four copies in a 2x2 grid, each in its own square with a gap between, turned only in
 * quarter turns (like the campfire) so no two ever overlap.
 */
public class KilnRenderer implements BlockEntityRenderer<KilnBlockEntity, KilnRenderer.State> {
    private static final float[][] SPOTS = {{0.29F, 0.29F}, {0.71F, 0.29F}, {0.29F, 0.71F}, {0.71F, 0.71F}};
    private static final float SCALE = 0.375F;
    private final ItemModelResolver items;

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public int copies;
    }

    public KilnRenderer(BlockEntityRendererProvider.Context context) {
        this.items = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(KilnBlockEntity kiln, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(kiln, state, partialTick, camera, breaking);
        ItemStack shown = kiln.items().getStackInSlot(KilnBlockEntity.INPUT);
        if (shown.isEmpty()) shown = kiln.items().getStackInSlot(KilnBlockEntity.OUTPUT);
        state.copies = shown.isEmpty() ? 0 : Math.min(SPOTS.length, (shown.getCount() + 3) / 4);
        if (state.copies == 0) return;
        items.updateForTopItem(state.item, shown, ItemDisplayContext.FIXED, kiln.getLevel(), null, (int) kiln.getBlockPos().asLong());
        // Light the items with the air above the grate. The light of the kiln itself, a solid block, is pitch dark
        // whenever the fire is out.
        if (kiln.getLevel() != null) state.lightCoords = LightCoordsUtil.getLightCoords(kiln.getLevel(), kiln.getBlockPos().above());
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int i = 0; i < state.copies; i++) {
            pose.pushPose();
            pose.translate(SPOTS[i][0], 1.02, SPOTS[i][1]);
            pose.rotateDegrees(Axis.XP, 90);
            pose.rotateDegrees(Axis.ZP, i * 90);
            pose.scale(SCALE, SCALE, SCALE);
            state.item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
