package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlock;
import io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Drinks set down on a surface: each stands as the same 3D bottle, mug or flask the shelves show, about half size (a
 * wine bottle stands half a block tall), on its own spot and turned a little its own way, so a table of them looks set
 * down by hand rather than lined up.
 */
public class PlacedDrinksRenderer implements BlockEntityRenderer<PlacedDrinksBlockEntity, PlacedDrinksRenderer.State> {
    private static final float SCALE = 0.5F;

    public static class State extends BlockEntityRenderState {
        public final List<DrinkModels.Shown> drinks = new ArrayList<>();
        public float facing;
    }

    public PlacedDrinksRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PlacedDrinksBlockEntity spot, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(spot, state, partialTick, camera, breaking);
        state.drinks.clear();
        for (ItemStack drink : spot.drinks()) state.drinks.add(DrinkModels.shown(drink, null));
        state.facing = spot.getBlockState().getValue(PlacedDrinksBlock.FACING).toYRot();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.drinks.isEmpty()) return;
        float[][] spots = PlacedDrinksBlock.SPOTS[state.drinks.size() - 1];
        for (int i = 0; i < state.drinks.size(); i++) {
            DrinkModels.Shown drink = state.drinks.get(i);
            if (drink.model() == null) continue;
            pose.pushPose();
            pose.translate(0.5, 0, 0.5);
            pose.rotateDegrees(Axis.YP, -state.facing);          // the group faces whoever set it down
            pose.translate(spots[i][0] / 16F - 0.5, 0, spots[i][1] / 16F - 0.5);
            pose.rotateDegrees(Axis.YP, PlacedDrinksBlock.TURN[i]);
            pose.scale(SCALE, SCALE, SCALE);
            pose.translate(-0.5, 0, -0.5);                                 // the models stand around x = z = 8
            drink.submit(pose, collector, state.lightCoords);
            pose.popPose();
        }
    }
}
