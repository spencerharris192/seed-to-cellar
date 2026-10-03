package io.github.spencerharris192.seedtocellar.client;

import org.jetbrains.annotations.Nullable;
import io.github.spencerharris192.seedtocellar.brewing.BottleLook;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.RandomSource;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.winery.CrushingTubBlock;
import io.github.spencerharris192.seedtocellar.winery.CrushingTubBlockEntity;
import io.github.spencerharris192.seedtocellar.winery.FruitPressBlockEntity;
import io.github.spencerharris192.seedtocellar.winery.WineRackBlock;
import io.github.spencerharris192.seedtocellar.winery.WineRackBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelData;

/** What you see inside the winery stations: the fruit and juice in the tub, the press's moving screw, the racked bottles. */
public final class WineryRenderers {
    /** The press's screw with its plate, and the handle on top: drawn moving, not part of the block model. */
    public static final ResourceLocation PRESS_SCREW = SeedToCellar.id("block/fruit_press_screw");
    public static final ResourceLocation PRESS_HANDLE = SeedToCellar.id("block/fruit_press_handle");
    /** A wine bottle lying in a rack hole, neck out; its foil (tint 0) takes the wine's color. */
    public static final ResourceLocation RACK_BOTTLE = SeedToCellar.id("block/wine_rack_bottle");
    /** The 3D drinks on the Bottle Shelf and Wine Display: upright, on y = 0 around x = z = 8; tint 0 is the drink's color. */
    public static final ResourceLocation DRINK_BOTTLE = SeedToCellar.id("block/drink_wine_bottle");
    public static final ResourceLocation DRINK_MUG = SeedToCellar.id("block/drink_mug");
    public static final ResourceLocation DRINK_FLASK = SeedToCellar.id("block/drink_flask");
    /** An empty mug, for the Mug Rack's pegs. */
    public static final ResourceLocation MUG_EMPTY = SeedToCellar.id("block/drink_mug_empty");
    /** The Mug Rack's pegs, left to right as you face it (x, in pixels, facing north), and how far out its mugs hang. */
    private static final float[] PEG_X = {14, 10, 6, 2};
    private static final float MUG_Z = 10, MUG_BOTTOM = 9, MUG_SCALE = 0.6F;
    /** How far the plate travels down into the cage over one pressing, in pixels. */
    private static final float PLATE_TRAVEL = 5F;
    /** Centers of the rack's holes in the north-facing model, in pixels: columns left to right as you face it, rows top then bottom. */
    private static final float[] RACK_X = {13, 8, 3};
    private static final float[] RACK_Y = {11, 5};
    /** The Bottle Shelf (north-facing model, half deep at z 8-16): columns, the tops of its two boards, depth, scale. */
    private static final float[] SHELF_X = {12, 8, 4};
    private static final float SHELF_UPPER_BOARD = 8, SHELF_LOWER_BOARD = 1, SHELF_Z = 11.5F, SHELF_SCALE = 0.42F, SHELF_MODEL_SCALE = 0.4F;
    /** The Wine Display: the tops of its three shelves (top first), depth, scale. */
    private static final float[] DISPLAY_SHELVES = {11, 6, 1};
    private static final float DISPLAY_Z = 6, DISPLAY_SCALE = 0.62F, DISPLAY_MODEL_SCALE = 0.58F;
    /** How high a wine bottle's middle rests above a Wine Display shelf: its body is 5 pixels thick. */
    private static final float WINE_BOTTLE_RADIUS = 2.65F;
    /** The room between a Wine Display shelf and the one above it, in pixels (a hair under the 4 there are). */
    private static final float DISPLAY_ROOM = 3.9F;

    /** A spirit's own bottle on the shelves (datagen builds one from each drink's BottleLook). */
    public static ResourceLocation spiritModel(Drinks.Drink drink) {
        return SeedToCellar.id("block/display/" + drink.name());
    }

    /** The crowned (six-star) Apple Crown Whiskey's bottle. */
    public static ResourceLocation crownedModel(Drinks.Drink drink) {
        return SeedToCellar.id("block/display/" + drink.name() + "_crowned");
    }

    /** Only the Apple Crown's bottle has a crowned look. */
    public static boolean canBeCrowned(BottleLook look) {
        return look.shape() == BottleLook.Shape.APPLE;
    }

    public static class Tub implements BlockEntityRenderer<CrushingTubBlockEntity> {
        public Tub(BlockEntityRendererProvider.Context context) {
        }

        @Override
        public void render(CrushingTubBlockEntity tub, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            float floor = CrushingTubBlock.FLOOR / 16F;
            float fill = tub.tank().getFluidAmount() / (float) CrushingTubBlockEntity.CAPACITY;
            float surface = floor + fill * (CrushingTubBlock.RIM - CrushingTubBlock.FLOOR - 1) / 16F;
            if (fill > 0) FluidRendering.surface(pose, buffers, tub.tank().getFluid(), 2 / 16F, 2 / 16F, 14 / 16F, 14 / 16F, surface, light);
            // the fruit waiting to be stomped floats on the juice: up to six, lying about
            ItemStack fruit = tub.fruit();
            int shown = Math.min(6, fruit.getCount());
            for (int i = 0; i < shown; i++) {
                double angle = i * Math.PI * 2 / 6 + 0.4;
                double radius = i == 5 ? 0 : 0.2;
                layFlat(tub, fruit, pose, buffers, light, 0.5 + Math.cos(angle) * radius, surface + 0.02 + i * 0.004,
                        0.5 + Math.sin(angle) * radius, i * 67, i);
            }
        }
    }

    public static class Press implements BlockEntityRenderer<FruitPressBlockEntity> {
        public Press(BlockEntityRendererProvider.Context context) {
        }

        @Override
        public void render(FruitPressBlockEntity press, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            // juice in the tray, around the cage
            float fill = press.tank().getFluidAmount() / (float) FruitPressBlockEntity.CAPACITY;
            if (fill > 0) FluidRendering.surface(pose, buffers, press.tank().getFluid(), 1 / 16F, 1 / 16F, 15 / 16F, 15 / 16F, (1 + 1.8F * fill) / 16F, light);
            // fruit in the cage, under the plate
            ItemStack fruit = press.input();
            int shown = Math.min(4, (fruit.getCount() + 3) / 4);
            for (int i = 0; i < shown; i++) {
                layFlat(press, fruit, pose, buffers, light, 0.4 + (i % 2) * 0.2, 3.3 / 16 + i / 2 * 0.06, 0.4 + (i / 2) * 0.2, i * 80, i);
            }
            // the screw and plate come down as the batch is cranked; the handle swings round on top
            float drop = press.screwDepth() * PLATE_TRAVEL / 16F;
            pose.pushPose();
            pose.translate(0, -drop, 0);
            renderPart(PRESS_SCREW, pose, buffers, light, overlay);
            pose.translate(0.5, 0, 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(press.handleAngle(partialTick)));
            pose.translate(-0.5, 0, -0.5);
            renderPart(PRESS_HANDLE, pose, buffers, light, overlay);
            pose.popPose();
        }
    }

    /**
     * The racks' bottles, by layout: the Wine Rack's lie in their cubbies neck out (our bottle model, foil tinted with
     * the wine); the Bottle Shelf's stand on its boards and the Wine Display's lie along its shelves as little 3D
     * bottles, mugs and flasks in each drink's color, every spirit in its own bottle with its own label.
     */
    public static class Rack implements BlockEntityRenderer<WineRackBlockEntity> {
        public Rack(BlockEntityRendererProvider.Context context) {
        }

        @Override
        public void render(WineRackBlockEntity rack, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            if (rack.getLevel() == null) return;
            Direction facing = rack.getBlockState().getValue(WineRackBlock.FACING);
            pose.pushPose();
            pose.translate(0.5, 0, 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(-((int) facing.toYRot() + 180) % 360));
            pose.translate(-0.5, 0, -0.5);
            switch (rack.layout()) {
                case WINE_RACK -> cubbies(rack, pose, buffers, facing, overlay);
                case BOTTLE_SHELF -> shelf(rack, pose, buffers, light, overlay);
                case WINE_DISPLAY -> display(rack, pose, buffers, light, overlay);
                case MUG_RACK -> mugs(rack, pose, buffers, light, overlay);
            }
            pose.popPose();
        }

        private static void cubbies(WineRackBlockEntity rack, PoseStack pose, MultiBufferSource buffers, Direction facing, int overlay) {
            // The rack is a solid block, so its own light is 0: the bottles take the light in front of it.
            int front = LevelRenderer.getLightColor(rack.getLevel(), rack.getBlockPos().relative(facing));
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(RACK_BOTTLE);
            int columns = rack.layout().columns();
            for (int slot = 0; slot < rack.bottles().getSlots(); slot++) {
                ItemStack bottle = rack.bottle(slot);
                if (bottle.isEmpty()) continue;
                int color = Minecraft.getInstance().getItemColors().getColor(bottle, 1);
                pose.pushPose();
                // the bottle model is centered on (8, 8): move it to its hole
                pose.translate((RACK_X[slot % columns] - 8) / 16F, (RACK_Y[slot / columns] - 8) / 16F, 0);
                Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(pose.last(),
                        buffers.getBuffer(RenderType.solid()), null, model,
                        (color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F,
                        front, overlay, ModelData.EMPTY, RenderType.solid());
                pose.popPose();
            }
        }

        /**
         * Two boards of three: each drink stands on its board as a 3D bottle, mug or flask in its own color. Another mod's
         * drink (no model of ours) is drawn as its item, the sprite's lowest pixels 7/16 below its middle.
         */
        private static void shelf(WineRackBlockEntity rack, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            for (int slot = 0; slot < rack.bottles().getSlots(); slot++) {
                ItemStack drink = rack.bottle(slot);
                if (drink.isEmpty()) continue;
                float board = slot / 3 == 0 ? SHELF_UPPER_BOARD : SHELF_LOWER_BOARD;
                ResourceLocation model = drinkModel(drink);
                pose.pushPose();
                if (model != null) {
                    pose.translate(SHELF_X[slot % 3] / 16F, board / 16F, SHELF_Z / 16F);
                    pose.scale(SHELF_MODEL_SCALE, SHELF_MODEL_SCALE, SHELF_MODEL_SCALE);
                    pose.translate(-0.5, 0, -0.5);
                    renderTinted(model, drink, pose, buffers, light, overlay);
                } else {
                    pose.translate(SHELF_X[slot % 3] / 16F, board / 16F + 7F / 16F * SHELF_SCALE, SHELF_Z / 16F);
                    pose.mulPose(Axis.YP.rotationDegrees(180));          // the drink's face toward the room
                    pose.scale(SHELF_SCALE, SHELF_SCALE, SHELF_SCALE);
                    renderItem(rack, drink, slot, pose, buffers, light);
                }
                pose.popPose();
            }
        }

        /** Four pegs: an empty mug hangs from each by its handle, the peg through the handle, the handle toward the wall. */
        private static void mugs(WineRackBlockEntity rack, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            for (int slot = 0; slot < rack.bottles().getSlots(); slot++) {
                if (rack.bottle(slot).isEmpty()) continue;
                pose.pushPose();
                pose.translate(PEG_X[slot] / 16F, MUG_BOTTOM / 16F, MUG_Z / 16F);
                pose.mulPose(Axis.YP.rotationDegrees(-90));                  // its handle toward the wall
                pose.scale(MUG_SCALE, MUG_SCALE, MUG_SCALE);
                pose.translate(-0.5, 0, -0.5);
                renderTinted(MUG_EMPTY, rack.bottle(slot), pose, buffers, light, overlay);   // cut out: clear glass
                pose.popPose();
            }
        }

        /** Three shelves: a 3D bottle lies along each, neck to the right, resting on its body (5 pixels thick). */
        private static void display(WineRackBlockEntity rack, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            for (int slot = 0; slot < rack.bottles().getSlots(); slot++) {
                ItemStack bottle = rack.bottle(slot);
                if (bottle.isEmpty()) continue;
                ResourceLocation model = drinkModel(bottle);
                pose.pushPose();
                if (model != null) {
                    float scale = displayScale(bottle);
                    pose.translate(0.5, DISPLAY_SHELVES[slot] / 16F + restingRadius(bottle) / 16F * scale, DISPLAY_Z / 16F);
                    pose.mulPose(Axis.ZP.rotationDegrees(90));           // the neck toward the viewer's right
                    pose.scale(scale, scale, scale);
                    pose.translate(-0.5, -7.5 / 16, -0.5);               // turn about the bottle's middle
                    renderTinted(model, bottle, pose, buffers, light, overlay);
                } else {
                    pose.translate(0.5, DISPLAY_SHELVES[slot] / 16F + 3F / 16F * DISPLAY_SCALE, DISPLAY_Z / 16F);
                    pose.mulPose(Axis.YP.rotationDegrees(180));
                    pose.mulPose(Axis.ZP.rotationDegrees(-90));
                    pose.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
                    renderItem(rack, bottle, slot, pose, buffers, light);
                }
                pose.popPose();
            }
        }

        /** Our 3D model for a drink, by what it's served in (each spirit its own); null for anything else. */
        @Nullable
        static ResourceLocation drinkModel(ItemStack stack) {
            if (!(stack.getItem() instanceof DrinkItem drink)) return null;
            return switch (drink.vessel()) {
                case WINE_BOTTLE -> DRINK_BOTTLE;
                case MUG -> DRINK_MUG;
                case GLASS_BOTTLE -> DRINK_FLASK;
                case SPIRIT_BOTTLE -> look(stack) == null ? null : Drinks.byFluid(drink.fluid())
                        .map(d -> crowned(stack) ? crownedModel(d) : spiritModel(d)).orElse(null);
            };
        }

        @Nullable
        private static BottleLook look(ItemStack stack) {
            return stack.getItem() instanceof DrinkItem drink ? Drinks.byFluid(drink.fluid()).map(BottleLook::of).orElse(null) : null;
        }

        /** A crowned Apple Crown Whiskey: its own look, and a shimmer. */
        private static boolean crowned(ItemStack stack) {
            BottleLook look = look(stack);
            return look != null && canBeCrowned(look) && DrinkItem.quality(stack).crowned();
        }

        /** How high the bottle's middle rests above a Wine Display shelf when laid down. */
        private static float restingRadius(ItemStack stack) {
            BottleLook look = look(stack);
            return look == null ? WINE_BOTTLE_RADIUS : look.shape().radius + 0.15F;
        }

        /** The Wine Display's usual scale, smaller for a bottle too stout to lie under the shelf above (the decanter, the Apple Crown). */
        private static float displayScale(ItemStack stack) {
            BottleLook look = look(stack);
            if (look == null) return DISPLAY_MODEL_SCALE;
            return Math.min(DISPLAY_MODEL_SCALE, DISPLAY_ROOM / (restingRadius(stack) + look.shape().reach));
        }

        /**
         * A drink model, each tinted part in its color: tint 0 is the drink's (the color its item shows); a spirit's bottle
         * adds its label's paper (1) and accent (2) and its glass (3), the spirit seen through tinted glass taking its tint.
         * A crowned bottle swaps its label's colors and shimmers like an enchanted item.
         */
        static void renderTinted(ResourceLocation id, ItemStack stack, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            int drink = Minecraft.getInstance().getItemColors().getColor(stack, 1);
            BottleLook look = look(stack);
            boolean crowned = crowned(stack);
            int[] tints = look == null ? new int[]{drink}
                    : crowned ? new int[]{multiply(drink, look.glass()), look.accent(), look.paper(), look.glass()}
                    : new int[]{multiply(drink, look.glass()), look.paper(), look.accent(), look.glass()};
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(id);
            VertexConsumer buffer = net.minecraft.client.renderer.entity.ItemRenderer.getFoilBufferDirect(buffers, RenderType.cutout(), true, crowned);
            for (Direction side : SIDES) {
                QUAD_RANDOM.setSeed(42L);
                for (BakedQuad quad : model.getQuads(null, side, QUAD_RANDOM, ModelData.EMPTY, RenderType.cutout())) {
                    int color = quad.isTinted() && quad.getTintIndex() < tints.length ? tints[quad.getTintIndex()] : 0xFFFFFF;
                    buffer.putBulkData(pose.last(), quad, (color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F,
                            light, overlay);
                }
            }
        }

        /** One random source for picking model quads, reused on the render thread (not a new one per bottle per frame). */
        private static final RandomSource QUAD_RANDOM = RandomSource.create();

        /** Every face list of a baked model: the unculled faces, then each side's. */
        private static final Direction[] SIDES = {null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

        private static int multiply(int a, int b) {
            int out = 0;
            for (int shift = 0; shift <= 16; shift += 8) {
                out |= ((a >> shift & 255) * (b >> shift & 255) / 255) << shift;
            }
            return out;
        }

        private static void renderItem(WineRackBlockEntity rack, ItemStack stack, int slot, PoseStack pose, MultiBufferSource buffers, int light) {
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY,
                    pose, buffers, rack.getLevel(), (int) rack.getBlockPos().asLong() + slot);
        }
    }

    private static void renderPart(ResourceLocation id, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(id);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(pose.last(),
                buffers.getBuffer(RenderType.solid()), null, model, 1F, 1F, 1F, light, overlay, ModelData.EMPTY, RenderType.solid());
    }

    /** One item lying flat at (x, y, z) in block units, turned `yaw` degrees. */
    private static void layFlat(BlockEntity be, ItemStack stack, PoseStack pose, MultiBufferSource buffers, int light,
                                double x, double y, double z, float yaw, int seed) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.XP.rotationDegrees(90));
        pose.scale(0.3F, 0.3F, 0.3F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                pose, buffers, be.getLevel(), (int) be.getBlockPos().asLong() + seed);
        pose.popPose();
    }

    private WineryRenderers() {}
}
