package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/** Drawing liquids: a surface inside a vessel (world) and a tank gauge (screens). */
public final class FluidRendering {
    /**
     * The entity translucent layer, which the game draws <em>before</em> translucent blocks: a glass vessel (the jar, the
     * still's spirit safe) is drawn afterwards, over the liquid, and tints it. With the blocks' translucent layer the liquid
     * would come last and the glass in front would hide it.
     */
    private static final RenderType LIQUID = RenderTypes.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);

    private static FluidModel model(FluidStack fluid) {
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.getFluid().defaultFluidState());
    }

    private static TextureAtlasSprite sprite(FluidStack fluid) {
        return model(fluid).stillMaterial().sprite();
    }

    /** The liquid's color, alpha included (ours are a little see-through); fully opaque if it has none. */
    private static int tint(FluidStack fluid) {
        FluidTintSource source = model(fluid).fluidTintSource();
        int color = source instanceof BrewFluidClient.BrewTint brew ? brew.withAlpha(fluid) : source != null ? source.colorAsStack(fluid) : -1;
        return ARGB.alpha(color) == 0 ? ARGB.opaque(color) : color;
    }

    /** A flat liquid surface at height y (block units 0-1) over the rectangle x0..x1, z0..z1. */
    public static void surface(PoseStack pose, SubmitNodeCollector collector, FluidStack fluid,
                               float x0, float z0, float x1, float z1, float y, int light) {
        if (fluid.isEmpty()) return;
        TextureAtlasSprite sprite = sprite(fluid);
        int color = tint(fluid);
        collector.submitCustomGeometry(pose, LIQUID, (last, vc) ->
                quad(vc, last, color, light, 0, 1, 0, sprite.getU(x0), sprite.getU(x1), sprite.getV(z0), sprite.getV(z1),
                        x0, y, z0, x0, y, z1, x1, y, z1, x1, y, z0));
    }

    /**
     * A block of liquid (four sides and the top), for see-through vessels like the glass jar.
     * Coordinates are in block units (0-1).
     */
    public static void box(PoseStack pose, SubmitNodeCollector collector, FluidStack fluid,
                           float x0, float y0, float z0, float x1, float y1, float z1, int light) {
        if (fluid.isEmpty() || y1 <= y0) return;
        TextureAtlasSprite sprite = sprite(fluid);
        int color = tint(fluid);
        // Texture coordinates follow the face's size, so the liquid texture isn't stretched.
        float vTop = sprite.getV(1 - y1), vBottom = sprite.getV(1 - y0);
        collector.submitCustomGeometry(pose, LIQUID, (last, vc) -> {
            // Each face's corners run counter-clockwise seen from outside, so back faces are culled.
            quad(vc, last, color, light, 0, 0, -1, sprite.getU(x1), sprite.getU(x0), vTop, vBottom,
                    x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0); // north
            quad(vc, last, color, light, 0, 0, 1, sprite.getU(x0), sprite.getU(x1), vTop, vBottom,
                    x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1); // south
            quad(vc, last, color, light, -1, 0, 0, sprite.getU(z0), sprite.getU(z1), vTop, vBottom,
                    x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1); // west
            quad(vc, last, color, light, 1, 0, 0, sprite.getU(z1), sprite.getU(z0), vTop, vBottom,
                    x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0); // east
            quad(vc, last, color, light, 0, 1, 0, sprite.getU(x0), sprite.getU(x1), sprite.getV(z0), sprite.getV(z1),
                    x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0); // top
        });
    }

    /** One quad; corners 1-2 share u0 and run top to bottom, corners 3-4 share u1 bottom to top. */
    private static void quad(VertexConsumer vc, PoseStack.Pose pose, int color, int light,
                             float nx, float ny, float nz, float u0, float u1, float v0, float v1,
                             float xa, float ya, float za, float xb, float yb, float zb,
                             float xc, float yc, float zc, float xd, float yd, float zd) {
        Matrix4f m = pose.pose();
        vc.addVertex(m, xa, ya, za).setColor(color).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        vc.addVertex(m, xb, yb, zb).setColor(color).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        vc.addVertex(m, xc, yc, zc).setColor(color).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        vc.addVertex(m, xd, yd, zd).setColor(color).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    /** Fills a gauge rectangle from the bottom, tiling the liquid texture in its color. */
    public static void gauge(GuiGraphicsExtractor graphics, FluidStack fluid, int capacity, int x, int y, int w, int h) {
        if (fluid.isEmpty() || capacity <= 0) return;
        int filled = Math.max(1, Math.min(h, Math.round(h * fluid.getAmount() / (float) capacity)));
        TextureAtlasSprite sprite = sprite(fluid);
        int color = ARGB.opaque(tint(fluid));
        int top = y + h - filled;
        for (int dy = 0; dy < filled; dy += 16) {
            int rowH = Math.min(16, filled - dy);
            for (int dx = 0; dx < w; dx += 16) {
                int colW = Math.min(16, w - dx);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + dx, top + filled - dy - rowH, colW, rowH, color);
            }
        }
    }

    /** Tooltip lines for a tank: liquid name and amount. */
    public static List<Component> tooltip(FluidStack fluid, int capacity) {
        List<Component> lines = new ArrayList<>();
        if (fluid.isEmpty()) {
            lines.add(Component.translatable("gui.seedtocellar.tank.empty"));
        } else {
            lines.add(fluid.getHoverName());
        }
        lines.add(Component.translatable("gui.seedtocellar.tank.amount", fluid.getAmount(), capacity));
        return lines;
    }

    private FluidRendering() {}
}
