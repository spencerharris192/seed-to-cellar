package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlock;
import io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Drinks set down on a surface: each stands as the same 3D bottle, mug or flask the shelves show, about half size (a
 * wine bottle stands half a block tall), on its own spot and turned a little its own way, so a table of them looks set
 * down by hand rather than lined up.
 */
public class PlacedDrinksRenderer implements BlockEntityRenderer<PlacedDrinksBlockEntity> {
    private static final float SCALE = 0.5F;

    public PlacedDrinksRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PlacedDrinksBlockEntity spot, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        List<ItemStack> drinks = spot.drinks();
        if (drinks.isEmpty()) return;
        float[][] spots = PlacedDrinksBlock.SPOTS[drinks.size() - 1];
        float facing = spot.getBlockState().getValue(PlacedDrinksBlock.FACING).toYRot();
        for (int i = 0; i < drinks.size(); i++) {
            ResourceLocation model = WineryRenderers.Rack.drinkModel(drinks.get(i));
            if (model == null) continue;
            pose.pushPose();
            pose.translate(0.5, 0, 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(-facing));               // the group faces whoever set it down
            pose.translate(spots[i][0] / 16F - 0.5, 0, spots[i][1] / 16F - 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(PlacedDrinksBlock.TURN[i]));
            pose.scale(SCALE, SCALE, SCALE);
            pose.translate(-0.5, 0, -0.5);                                 // the models stand around x = z = 8
            WineryRenderers.Rack.renderTinted(model, drinks.get(i), pose, buffers, light, overlay);
            pose.popPose();
        }
    }
}
