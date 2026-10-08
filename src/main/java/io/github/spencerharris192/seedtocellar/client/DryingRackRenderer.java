package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.farming.DryingRackBlock;
import io.github.spencerharris192.seedtocellar.farming.DryingRackBlockEntity;
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

/**
 * Draws what hangs on a Drying Rack: two items under the top rail, two under the lower one,
 * flat and facing the front like items in a frame (visible from both sides).
 */
public class DryingRackRenderer implements BlockEntityRenderer<DryingRackBlockEntity, DryingRackRenderer.State> {
    // Spot centers in block pixels (x across the rails, y height), for a rack facing north.
    private static final float[][] SPOTS = {{5.5F, 10.5F}, {10.5F, 10.5F}, {5.5F, 4.0F}, {10.5F, 4.0F}};
    private static final float SCALE = 0.375F;
    private final ItemModelResolver items;

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState[] items = new ItemStackRenderState[DryingRackBlockEntity.SLOTS];
        public float facing;
    }

    public DryingRackRenderer(BlockEntityRendererProvider.Context context) {
        this.items = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        State state = new State();
        for (int i = 0; i < state.items.length; i++) state.items[i] = new ItemStackRenderState();
        return state;
    }

    @Override
    public void extractRenderState(DryingRackBlockEntity rack, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(rack, state, partialTick, camera, breaking);
        state.facing = rack.getBlockState().getValue(DryingRackBlock.FACING).toYRot();
        for (int slot = 0; slot < DryingRackBlockEntity.SLOTS; slot++) {
            items.updateForTopItem(state.items[slot], rack.stack(slot), ItemDisplayContext.FIXED, rack.getLevel(), null,
                    (int) rack.getBlockPos().asLong() + slot);
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int slot = 0; slot < DryingRackBlockEntity.SLOTS; slot++) {
            if (state.items[slot].isEmpty()) continue;
            pose.pushPose();
            pose.translate(0.5, 0, 0.5);
            pose.rotateDegrees(Axis.YP, -state.facing);
            pose.translate(-0.5 + SPOTS[slot][0] / 16.0, SPOTS[slot][1] / 16.0, 0);
            pose.scale(SCALE, SCALE, SCALE);
            state.items[slot].submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
