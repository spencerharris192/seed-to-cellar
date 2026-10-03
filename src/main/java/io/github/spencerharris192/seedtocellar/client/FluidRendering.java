package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/** Drawing liquids: a surface inside a vessel (world) and a tank gauge (screens). */
public final class FluidRendering {
    private static TextureAtlasSprite sprite(FluidStack fluid) {
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ext.getStillTexture(fluid));
    }

    private static int tint(FluidStack fluid) {
        return IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
    }

    /** A flat liquid surface at height y (block units 0-1) over the rectangle x0..x1, z0..z1. */
    public static void surface(PoseStack pose, MultiBufferSource buffers, FluidStack fluid,
                               float x0, float z0, float x1, float z1, float y, int light) {
        if (fluid.isEmpty()) return;
        TextureAtlasSprite sprite = sprite(fluid);
        int color = tint(fluid);
        float a = (color >>> 24 & 255) / 255F;
        float r = (color >> 16 & 255) / 255F;
        float g = (color >> 8 & 255) / 255F;
        float b = (color & 255) / 255F;
        if (a == 0) a = 1;
        float u0 = sprite.getU(x0 * 16), u1 = sprite.getU(x1 * 16);
        float v0 = sprite.getV(z0 * 16), v1 = sprite.getV(z1 * 16);
        VertexConsumer vc = buffers.getBuffer(RenderType.translucent());
        Matrix4f m = pose.last().pose();
        vc.vertex(m, x0, y, z0).color(r, g, b, a).uv(u0, v0).uv2(light).normal(0, 1, 0).endVertex();
        vc.vertex(m, x0, y, z1).color(r, g, b, a).uv(u0, v1).uv2(light).normal(0, 1, 0).endVertex();
        vc.vertex(m, x1, y, z1).color(r, g, b, a).uv(u1, v1).uv2(light).normal(0, 1, 0).endVertex();
        vc.vertex(m, x1, y, z0).color(r, g, b, a).uv(u1, v0).uv2(light).normal(0, 1, 0).endVertex();
    }

    /**
     * A block of liquid (four sides and the top), for see-through vessels like the glass jar.
     * Coordinates are in block units (0-1).
     * <p>It uses the entity translucent layer, which the game draws <em>before</em> translucent
     * blocks. So the jar's glass is drawn afterwards, over the liquid, and tints it. With the
     * normal translucent layer the liquid would come last and the glass in front would hide it.
     */
    public static void box(PoseStack pose, MultiBufferSource buffers, FluidStack fluid,
                           float x0, float y0, float z0, float x1, float y1, float z1, int light) {
        if (fluid.isEmpty() || y1 <= y0) return;
        TextureAtlasSprite sprite = sprite(fluid);
        int color = tint(fluid);
        int a = color >>> 24 & 255, r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
        if (a == 0) a = 255;
        VertexConsumer vc = buffers.getBuffer(Sheets.translucentCullBlockSheet());
        PoseStack.Pose last = pose.last();
        // Texture coordinates follow the face's size, so the liquid texture isn't stretched.
        float vTop = sprite.getV(16 - y1 * 16), vBottom = sprite.getV(16 - y0 * 16);
        // Each face's corners run counter-clockwise seen from outside, so back faces are culled.
        quad(vc, last, r, g, b, a, light, 0, 0, -1, sprite.getU(x1 * 16), sprite.getU(x0 * 16), vTop, vBottom,
                x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0); // north
        quad(vc, last, r, g, b, a, light, 0, 0, 1, sprite.getU(x0 * 16), sprite.getU(x1 * 16), vTop, vBottom,
                x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1); // south
        quad(vc, last, r, g, b, a, light, -1, 0, 0, sprite.getU(z0 * 16), sprite.getU(z1 * 16), vTop, vBottom,
                x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1); // west
        quad(vc, last, r, g, b, a, light, 1, 0, 0, sprite.getU(z1 * 16), sprite.getU(z0 * 16), vTop, vBottom,
                x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0); // east
        quad(vc, last, r, g, b, a, light, 0, 1, 0, sprite.getU(x0 * 16), sprite.getU(x1 * 16), sprite.getV(z0 * 16), sprite.getV(z1 * 16),
                x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0); // top
    }

    /** One quad; corners 1-2 share u0 and run top to bottom, corners 3-4 share u1 bottom to top. */
    private static void quad(VertexConsumer vc, PoseStack.Pose pose, int r, int g, int b, int a, int light,
                             float nx, float ny, float nz, float u0, float u1, float v0, float v1,
                             float xa, float ya, float za, float xb, float yb, float zb,
                             float xc, float yc, float zc, float xd, float yd, float zd) {
        Matrix4f m = pose.pose();
        vc.vertex(m, xa, ya, za).color(r, g, b, a).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), nx, ny, nz).endVertex();
        vc.vertex(m, xb, yb, zb).color(r, g, b, a).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), nx, ny, nz).endVertex();
        vc.vertex(m, xc, yc, zc).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), nx, ny, nz).endVertex();
        vc.vertex(m, xd, yd, zd).color(r, g, b, a).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), nx, ny, nz).endVertex();
    }

    /** Fills a gauge rectangle from the bottom, tiling the liquid texture in its color. */
    public static void gauge(GuiGraphics graphics, FluidStack fluid, int capacity, int x, int y, int w, int h) {
        if (fluid.isEmpty() || capacity <= 0) return;
        int filled = Math.max(1, Math.min(h, Math.round(h * fluid.getAmount() / (float) capacity)));
        TextureAtlasSprite sprite = sprite(fluid);
        int color = tint(fluid);
        RenderSystem.setShaderColor((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 1F);
        int top = y + h - filled;
        for (int dy = 0; dy < filled; dy += 16) {
            int rowH = Math.min(16, filled - dy);
            for (int dx = 0; dx < w; dx += 16) {
                int colW = Math.min(16, w - dx);
                graphics.blit(x + dx, top + filled - dy - rowH, 0, colW, rowH, sprite);
            }
        }
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    /** Tooltip lines for a tank: liquid name and amount. */
    public static List<Component> tooltip(FluidStack fluid, int capacity) {
        List<Component> lines = new ArrayList<>();
        if (fluid.isEmpty()) {
            lines.add(Component.translatable("gui.seedtocellar.tank.empty"));
        } else {
            lines.add(fluid.getDisplayName());
        }
        lines.add(Component.translatable("gui.seedtocellar.tank.amount", fluid.getAmount(), capacity));
        return lines;
    }

    private FluidRendering() {}
}
