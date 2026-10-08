package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Block models drawn by our renderers rather than placed as blocks: the millstone's runner, the press's screw, the 3D
 * drinks on the shelves. Each is loaded by itself (a standalone model) and drawn like an item, so its tinted parts take
 * the colors given and a crowned bottle can shimmer.
 */
public final class ClientModels {
    private static final Map<Identifier, StandaloneModelKey<ItemQuads>> KEYS = new HashMap<>();

    /** Loads the model {@code id} (a block model path, like {@code seedtocellar:block/millstone_runner}). */
    public static void register(ModelEvent.RegisterStandalone event, Identifier id) {
        StandaloneModelKey<ItemQuads> key = KEYS.computeIfAbsent(id, i -> new StandaloneModelKey<>(i::toString));
        event.register(key, new SimpleUnbakedStandaloneModel<>(id, (model, baker, name) ->
                ItemQuads.split(model.bakeTopGeometry(model.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll())));
    }

    /**
     * Draws the model {@code id}, its tint index i in {@code tints[i]} (ARGB), lit with {@code light}; {@code foil} adds an
     * enchanted shimmer.
     */
    public static void submit(Identifier id, PoseStack pose, SubmitNodeCollector collector, int[] tints, int light, boolean foil) {
        StandaloneModelKey<ItemQuads> key = KEYS.get(id);
        if (key == null) return;
        ItemQuads quads = Minecraft.getInstance().getModelManager().getStandaloneModel(key);
        if (quads == null || quads.isEmpty()) return;
        collector.submitItem(pose, ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, 0, tints, quads,
                foil ? ItemStackRenderState.FoilType.STANDARD : ItemStackRenderState.FoilType.NONE);
    }

    /** Untinted. */
    public static void submit(Identifier id, PoseStack pose, SubmitNodeCollector collector, int light) {
        submit(id, pose, collector, new int[0], light, false);
    }

    private ClientModels() {}
}
