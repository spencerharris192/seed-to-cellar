package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.FermentingVatBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.PreservingJarBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Liquid levels you can see in the kettle, the open vat, and the glass jar. */
public final class VesselRenderers {
    public static class Kettle implements BlockEntityRenderer<BrewKettleBlockEntity> {
        public Kettle(BlockEntityRendererProvider.Context context) {
        }

        @Override
        public void render(BrewKettleBlockEntity kettle, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            float fill = kettle.tank().getFluidAmount() / (float) BrewKettleBlockEntity.CAPACITY;
            if (fill <= 0) return;
            FluidRendering.surface(pose, buffers, kettle.tank().getFluid(), 2 / 16F, 2 / 16F, 14 / 16F, 14 / 16F, (2 + 9 * fill) / 16F, light);
        }
    }

    public static class Vat implements BlockEntityRenderer<FermentingVatBlockEntity> {
        public Vat(BlockEntityRendererProvider.Context context) {
        }

        @Override
        public void render(FermentingVatBlockEntity vat, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            if (!vat.isOpen()) return;
            float fill = vat.tank().getFluidAmount() / (float) FermentingVatBlockEntity.CAPACITY;
            if (fill <= 0) return;
            FluidRendering.surface(pose, buffers, vat.tank().getFluid(), 2 / 16F, 2 / 16F, 14 / 16F, 14 / 16F, (1 + 13 * fill) / 16F, light);
        }
    }

    public static class Jar implements BlockEntityRenderer<PreservingJarBlockEntity> {
        public Jar(BlockEntityRendererProvider.Context context) {
        }

        @Override
        public void render(PreservingJarBlockEntity jar, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            // The liquid fills the glass body (4-12 across, 0-9 high) as a solid block of color,
            // just inside the glass so the two never flicker against each other.
            float fill = jar.tank().getFluidAmount() / (float) PreservingJarBlockEntity.CAPACITY;
            if (fill > 0) {
                float in = 4.25F / 16, out = 11.75F / 16;
                FluidRendering.box(pose, buffers, jar.tank().getFluid(), in, 0.25F / 16, in, out, (0.25F + 8.5F * fill) / 16, out, light);
            }
            // items resting inside the jar, small and slightly tilted
            for (int i = 0; i < PreservingJarBlockEntity.SLOTS; i++) {
                ItemStack stack = jar.items().getStackInSlot(i);
                if (stack.isEmpty()) continue;
                pose.pushPose();
                pose.translate(0.5 + (i % 2 == 0 ? -0.07 : 0.07), 0.12 + i * 0.1, 0.5 + (i < 2 ? -0.06 : 0.06));
                pose.mulPose(Axis.YP.rotationDegrees(i * 50));
                pose.mulPose(Axis.XP.rotationDegrees(70));
                pose.scale(0.25F, 0.25F, 0.25F);
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                        pose, buffers, jar.getLevel(), (int) jar.getBlockPos().asLong() + i);
                pose.popPose();
            }
        }
    }

    private VesselRenderers() {}
}
