package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.winery.CrushingTubBlock;
import io.github.spencerharris192.seedtocellar.winery.CrushingTubBlockEntity;
import io.github.spencerharris192.seedtocellar.winery.FruitPressBlockEntity;
import io.github.spencerharris192.seedtocellar.winery.RackLayout;
import io.github.spencerharris192.seedtocellar.winery.WineRackBlock;
import io.github.spencerharris192.seedtocellar.winery.WineRackBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jspecify.annotations.Nullable;

/** What you see inside the winery stations: the fruit and juice in the tub, the press's moving screw, the racked bottles. */
public final class WineryRenderers {
    /** The press's screw with its plate, and the handle on top: drawn moving, not part of the block model. */
    public static final Identifier PRESS_SCREW = SeedToCellar.id("block/fruit_press_screw");
    public static final Identifier PRESS_HANDLE = SeedToCellar.id("block/fruit_press_handle");
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
    /** The room between a Wine Display shelf and the one above it, in pixels (a hair under the 4 there are). */
    private static final float DISPLAY_ROOM = 3.9F;

    public static void registerModels(ModelEvent.RegisterStandalone event) {
        ClientModels.register(event, PRESS_SCREW);
        ClientModels.register(event, PRESS_HANDLE);
        DrinkModels.register(event);
    }

    /** A liquid, how full its vessel is, and the fruit lying in it (drawn several times over). */
    public static class FruitState extends VesselRenderers.State {
        public final ItemStackRenderState fruit = new ItemStackRenderState();
        public int shown;
        public float screwDepth, handleAngle;
    }

    public static class Tub implements BlockEntityRenderer<CrushingTubBlockEntity, FruitState> {
        private final ItemModelResolver items;

        public Tub(BlockEntityRendererProvider.Context context) {
            this.items = context.itemModelResolver();
        }

        @Override
        public FruitState createRenderState() {
            return new FruitState();
        }

        @Override
        public void extractRenderState(CrushingTubBlockEntity tub, FruitState state, float partialTick, Vec3 camera,
                                       ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
            BlockEntityRenderer.super.extractRenderState(tub, state, partialTick, camera, breaking);
            state.fluid = tub.tank().getFluid().copy();
            state.fill = tub.tank().getFluidAmount() / (float) CrushingTubBlockEntity.CAPACITY;
            ItemStack fruit = tub.fruit();
            state.shown = Math.min(6, fruit.getCount());
            items.updateForTopItem(state.fruit, fruit, ItemDisplayContext.FIXED, tub.getLevel(), null, (int) tub.getBlockPos().asLong());
        }

        @Override
        public void submit(FruitState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            float floor = CrushingTubBlock.FLOOR / 16F;
            float surface = floor + state.fill * (CrushingTubBlock.RIM - CrushingTubBlock.FLOOR - 1) / 16F;
            if (state.fill > 0) FluidRendering.surface(pose, collector, state.fluid, 2 / 16F, 2 / 16F, 14 / 16F, 14 / 16F, surface, state.lightCoords);
            // the fruit waiting to be stomped floats on the juice: up to six, lying about
            for (int i = 0; i < state.shown; i++) {
                double angle = i * Math.PI * 2 / 6 + 0.4;
                double radius = i == 5 ? 0 : 0.2;
                layFlat(state.fruit, pose, collector, state.lightCoords, 0.5 + Math.cos(angle) * radius, surface + 0.02 + i * 0.004,
                        0.5 + Math.sin(angle) * radius, i * 67);
            }
        }
    }

    public static class Press implements BlockEntityRenderer<FruitPressBlockEntity, FruitState> {
        private final ItemModelResolver items;

        public Press(BlockEntityRendererProvider.Context context) {
            this.items = context.itemModelResolver();
        }

        @Override
        public FruitState createRenderState() {
            return new FruitState();
        }

        @Override
        public void extractRenderState(FruitPressBlockEntity press, FruitState state, float partialTick, Vec3 camera,
                                       ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
            BlockEntityRenderer.super.extractRenderState(press, state, partialTick, camera, breaking);
            state.fluid = press.tank().getFluid().copy();
            state.fill = press.tank().getFluidAmount() / (float) FruitPressBlockEntity.CAPACITY;
            ItemStack fruit = press.input();
            state.shown = Math.min(4, (fruit.getCount() + 3) / 4);
            items.updateForTopItem(state.fruit, fruit, ItemDisplayContext.FIXED, press.getLevel(), null, (int) press.getBlockPos().asLong());
            state.screwDepth = press.screwDepth();
            state.handleAngle = press.handleAngle(partialTick);
        }

        @Override
        public void submit(FruitState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            int light = state.lightCoords;
            // juice in the tray, around the cage
            if (state.fill > 0) FluidRendering.surface(pose, collector, state.fluid, 1 / 16F, 1 / 16F, 15 / 16F, 15 / 16F, (1 + 1.8F * state.fill) / 16F, light);
            // fruit in the cage, under the plate
            for (int i = 0; i < state.shown; i++) {
                layFlat(state.fruit, pose, collector, light, 0.4 + (i % 2) * 0.2, 3.3 / 16 + i / 2 * 0.06, 0.4 + (i / 2) * 0.2, i * 80);
            }
            // the screw and plate come down as the batch is cranked; the handle swings round on top
            float drop = state.screwDepth * PLATE_TRAVEL / 16F;
            pose.pushPose();
            pose.translate(0, -drop, 0);
            ClientModels.submit(PRESS_SCREW, pose, collector, light);
            pose.translate(0.5, 0, 0.5);
            pose.rotateDegrees(Axis.YP, state.handleAngle);
            pose.translate(-0.5, 0, -0.5);
            ClientModels.submit(PRESS_HANDLE, pose, collector, light);
            pose.popPose();
        }
    }

    /** The racks' contents: each slot's drink as the shelves show it, or (another mod's drink) as its item. */
    public static class RackState extends BlockEntityRenderState {
        public RackLayout layout = RackLayout.WINE_RACK;
        public Direction facing = Direction.NORTH;
        /** For the Wine Rack: the light in front of it (the rack is a solid block, so its own light is 0). */
        public int frontLight;
        public DrinkModels.@Nullable Shown[] shown = new DrinkModels.Shown[0];
        public ItemStackRenderState[] items = new ItemStackRenderState[0];
    }

    /**
     * The racks' bottles, by layout: the Wine Rack's lie in their cubbies neck out (our bottle model, foil tinted with
     * the wine); the Bottle Shelf's stand on its boards and the Wine Display's lie along its shelves as little 3D
     * bottles, mugs and flasks in each drink's color, every spirit in its own bottle with its own label.
     */
    public static class Rack implements BlockEntityRenderer<WineRackBlockEntity, RackState> {
        private final ItemModelResolver itemModels;

        public Rack(BlockEntityRendererProvider.Context context) {
            this.itemModels = context.itemModelResolver();
        }

        @Override
        public RackState createRenderState() {
            return new RackState();
        }

        @Override
        public void extractRenderState(WineRackBlockEntity rack, RackState state, float partialTick, Vec3 camera,
                                       ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
            BlockEntityRenderer.super.extractRenderState(rack, state, partialTick, camera, breaking);
            state.layout = rack.layout();
            state.facing = rack.getBlockState().getValue(WineRackBlock.FACING);
            state.frontLight = rack.getLevel() == null ? state.lightCoords
                    : LightCoordsUtil.getLightCoords(rack.getLevel(), rack.getBlockPos().relative(state.facing));
            int slots = rack.bottles().getSlots();
            if (state.shown.length != slots) {
                state.shown = new DrinkModels.Shown[slots];
                state.items = new ItemStackRenderState[slots];
                for (int i = 0; i < slots; i++) state.items[i] = new ItemStackRenderState();
            }
            for (int slot = 0; slot < slots; slot++) {
                ItemStack bottle = rack.bottle(slot);
                Identifier model = switch (state.layout) {
                    case WINE_RACK -> DrinkModels.RACK_BOTTLE;
                    case MUG_RACK -> DrinkModels.MUG_EMPTY;   // cut out: clear glass
                    default -> null;
                };
                state.shown[slot] = bottle.isEmpty() ? null : DrinkModels.shown(bottle, model);
                boolean asItem = state.shown[slot] != null && state.shown[slot].model() == null;
                itemModels.updateForTopItem(state.items[slot], asItem ? bottle : ItemStack.EMPTY, ItemDisplayContext.NONE, rack.getLevel(), null,
                        (int) rack.getBlockPos().asLong() + slot);
            }
        }

        @Override
        public void submit(RackState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            pose.pushPose();
            pose.translate(0.5, 0, 0.5);
            pose.rotateDegrees(Axis.YP, -((int) state.facing.toYRot() + 180) % 360);
            pose.translate(-0.5, 0, -0.5);
            switch (state.layout) {
                case WINE_RACK -> cubbies(state, pose, collector);
                case BOTTLE_SHELF -> shelf(state, pose, collector);
                case WINE_DISPLAY -> display(state, pose, collector);
                case MUG_RACK -> mugs(state, pose, collector);
            }
            pose.popPose();
        }

        private static void cubbies(RackState state, PoseStack pose, SubmitNodeCollector collector) {
            int columns = state.layout.columns();
            for (int slot = 0; slot < state.shown.length; slot++) {
                if (state.shown[slot] == null) continue;
                pose.pushPose();
                // the bottle model is centered on (8, 8): move it to its hole
                pose.translate((RACK_X[slot % columns] - 8) / 16F, (RACK_Y[slot / columns] - 8) / 16F, 0);
                state.shown[slot].submit(pose, collector, state.frontLight);
                pose.popPose();
            }
        }

        /**
         * Two boards of three: each drink stands on its board as a 3D bottle, mug or flask in its own color. Another mod's
         * drink (no model of ours) is drawn as its item, the sprite's lowest pixels 7/16 below its middle.
         */
        private static void shelf(RackState state, PoseStack pose, SubmitNodeCollector collector) {
            for (int slot = 0; slot < state.shown.length; slot++) {
                DrinkModels.Shown drink = state.shown[slot];
                if (drink == null) continue;
                float board = slot / 3 == 0 ? SHELF_UPPER_BOARD : SHELF_LOWER_BOARD;
                pose.pushPose();
                if (drink.model() != null) {
                    pose.translate(SHELF_X[slot % 3] / 16F, board / 16F, SHELF_Z / 16F);
                    pose.scale(SHELF_MODEL_SCALE, SHELF_MODEL_SCALE, SHELF_MODEL_SCALE);
                    pose.translate(-0.5, 0, -0.5);
                    drink.submit(pose, collector, state.lightCoords);
                } else {
                    pose.translate(SHELF_X[slot % 3] / 16F, board / 16F + 7F / 16F * SHELF_SCALE, SHELF_Z / 16F);
                    pose.rotateDegrees(Axis.YP, 180);          // the drink's face toward the room
                    pose.scale(SHELF_SCALE, SHELF_SCALE, SHELF_SCALE);
                    state.items[slot].submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                }
                pose.popPose();
            }
        }

        /** Four pegs: an empty mug hangs from each by its handle, the peg through the handle, the handle toward the wall. */
        private static void mugs(RackState state, PoseStack pose, SubmitNodeCollector collector) {
            for (int slot = 0; slot < state.shown.length; slot++) {
                if (state.shown[slot] == null) continue;
                pose.pushPose();
                pose.translate(PEG_X[slot] / 16F, MUG_BOTTOM / 16F, MUG_Z / 16F);
                pose.rotateDegrees(Axis.YP, -90);                  // its handle toward the wall
                pose.scale(MUG_SCALE, MUG_SCALE, MUG_SCALE);
                pose.translate(-0.5, 0, -0.5);
                state.shown[slot].submit(pose, collector, state.lightCoords);
                pose.popPose();
            }
        }

        /** Three shelves: a 3D bottle lies along each, neck to the right, resting on its body (5 pixels thick). */
        private static void display(RackState state, PoseStack pose, SubmitNodeCollector collector) {
            for (int slot = 0; slot < state.shown.length; slot++) {
                DrinkModels.Shown bottle = state.shown[slot];
                if (bottle == null) continue;
                pose.pushPose();
                if (bottle.model() != null) {
                    float scale = bottle.displayScale(DISPLAY_MODEL_SCALE, DISPLAY_ROOM);
                    pose.translate(0.5, DISPLAY_SHELVES[slot] / 16F + bottle.restingRadius() / 16F * scale, DISPLAY_Z / 16F);
                    pose.rotateDegrees(Axis.ZP, 90);           // the neck toward the viewer's right
                    pose.scale(scale, scale, scale);
                    pose.translate(-0.5, -7.5 / 16, -0.5);               // turn about the bottle's middle
                    bottle.submit(pose, collector, state.lightCoords);
                } else {
                    pose.translate(0.5, DISPLAY_SHELVES[slot] / 16F + 3F / 16F * DISPLAY_SCALE, DISPLAY_Z / 16F);
                    pose.rotateDegrees(Axis.YP, 180);
                    pose.rotateDegrees(Axis.ZP, -90);
                    pose.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
                    state.items[slot].submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                }
                pose.popPose();
            }
        }
    }

    /** One item lying flat at (x, y, z) in block units, turned `yaw` degrees. */
    private static void layFlat(ItemStackRenderState item, PoseStack pose, SubmitNodeCollector collector, int light,
                                double x, double y, double z, float yaw) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.rotateDegrees(Axis.YP, yaw);
        pose.rotateDegrees(Axis.XP, 90);
        pose.scale(0.3F, 0.3F, 0.3F);
        item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }

    private WineryRenderers() {}
}
