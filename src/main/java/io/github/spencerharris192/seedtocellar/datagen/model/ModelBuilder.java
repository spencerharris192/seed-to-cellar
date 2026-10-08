package io.github.spencerharris192.seedtocellar.datagen.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A block or item model being written: its parent, textures and elements, saved as the game's model JSON. (Whether a face
 * is cut out or see-through now follows its texture's own transparency, so there's no render type to set.)
 */
public class ModelBuilder<T extends ModelBuilder<T>> extends ModelFile {
    private @Nullable Identifier parent;
    private final Map<String, String> textures = new LinkedHashMap<>();
    private final List<ElementBuilder> elements = new ArrayList<>();
    private @Nullable Boolean ambientOcclusion;

    public ModelBuilder(Identifier location) {
        super(location);
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    public T parent(ModelFile parent) {
        this.parent = parent.getLocation();
        return self();
    }

    public T texture(String key, Identifier texture) {
        textures.put(key, texture.toString());
        return self();
    }

    /** A texture by id, or "#key" for another of this model's textures. */
    public T texture(String key, String texture) {
        textures.put(key, texture);
        return self();
    }

    /** Kept so older code reads the same: the texture's own transparency decides now. */
    public T renderType(String renderType) {
        return self();
    }

    public T ao(boolean ambientOcclusion) {
        this.ambientOcclusion = ambientOcclusion;
        return self();
    }

    public ElementBuilder element() {
        ElementBuilder element = new ElementBuilder();
        elements.add(element);
        return element;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (parent != null) json.addProperty("parent", parent.toString());
        if (ambientOcclusion != null) json.addProperty("ambientocclusion", ambientOcclusion);
        if (!textures.isEmpty()) {
            JsonObject tex = new JsonObject();
            textures.forEach(tex::addProperty);
            json.add("textures", tex);
        }
        if (!elements.isEmpty()) {
            JsonArray array = new JsonArray();
            for (ElementBuilder element : elements) array.add(element.toJson());
            json.add("elements", array);
        }
        return json;
    }

    public enum FaceRotation {
        ZERO(0), CLOCKWISE_90(90), UPSIDE_DOWN(180), COUNTERCLOCKWISE_90(270);

        /** The old name for a quarter turn. */
        public static final FaceRotation CLOCKWISE = CLOCKWISE_90;

        final int degrees;

        FaceRotation(int degrees) {
            this.degrees = degrees;
        }
    }

    public class ElementBuilder {
        private final float[] from = {0, 0, 0}, to = {16, 16, 16};
        private final Map<Direction, FaceBuilder> faces = new EnumMap<>(Direction.class);
        private @Nullable RotationBuilder rotation;
        private boolean shade = true;

        public ElementBuilder from(float x, float y, float z) {
            from[0] = x; from[1] = y; from[2] = z;
            return this;
        }

        public ElementBuilder to(float x, float y, float z) {
            to[0] = x; to[1] = y; to[2] = z;
            return this;
        }

        public FaceBuilder face(Direction direction) {
            return faces.computeIfAbsent(direction, d -> new FaceBuilder(this));
        }

        public ElementBuilder allFaces(BiConsumer<Direction, FaceBuilder> each) {
            for (Direction direction : Direction.values()) each.accept(direction, face(direction));
            return this;
        }

        public RotationBuilder rotation() {
            if (rotation == null) rotation = new RotationBuilder(this);
            return rotation;
        }

        public ElementBuilder shade(boolean shade) {
            this.shade = shade;
            return this;
        }

        public T end() {
            return self();
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.add("from", vec(from));
            json.add("to", vec(to));
            if (rotation != null) json.add("rotation", rotation.toJson());
            if (!shade) json.addProperty("shade", false);
            JsonObject faceJson = new JsonObject();
            faces.forEach((direction, face) -> faceJson.add(direction.getSerializedName(), face.toJson()));
            json.add("faces", faceJson);
            return json;
        }
    }

    public class FaceBuilder {
        private final ElementBuilder element;
        private @Nullable float[] uvs;
        private String texture = "#missing";
        private @Nullable Direction cullface;
        private int tintIndex = -1;
        private FaceRotation rotation = FaceRotation.ZERO;

        FaceBuilder(ElementBuilder element) {
            this.element = element;
        }

        public FaceBuilder uvs(float u0, float v0, float u1, float v1) {
            this.uvs = new float[]{u0, v0, u1, v1};
            return this;
        }

        public FaceBuilder texture(String texture) {
            this.texture = texture;
            return this;
        }

        public FaceBuilder cullface(@Nullable Direction cullface) {
            this.cullface = cullface;
            return this;
        }

        public FaceBuilder tintindex(int tintIndex) {
            this.tintIndex = tintIndex;
            return this;
        }

        public FaceBuilder rotation(FaceRotation rotation) {
            this.rotation = rotation;
            return this;
        }

        public ElementBuilder end() {
            return element;
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            if (uvs != null) json.add("uv", vec(uvs));
            json.addProperty("texture", texture);
            if (cullface != null) json.addProperty("cullface", cullface.getSerializedName());
            if (rotation != FaceRotation.ZERO) json.addProperty("rotation", rotation.degrees);
            if (tintIndex >= 0) json.addProperty("tintindex", tintIndex);
            return json;
        }
    }

    public class RotationBuilder {
        private final ElementBuilder element;
        private final float[] origin = {8, 8, 8};
        private Direction.Axis axis = Direction.Axis.Y;
        private float angle;
        private boolean rescale;

        RotationBuilder(ElementBuilder element) {
            this.element = element;
        }

        public RotationBuilder origin(float x, float y, float z) {
            origin[0] = x; origin[1] = y; origin[2] = z;
            return this;
        }

        public RotationBuilder axis(Direction.Axis axis) {
            this.axis = axis;
            return this;
        }

        public RotationBuilder angle(float angle) {
            this.angle = angle;
            return this;
        }

        public RotationBuilder rescale(boolean rescale) {
            this.rescale = rescale;
            return this;
        }

        public ElementBuilder end() {
            return element;
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.add("origin", vec(origin));
            json.addProperty("axis", axis.getName().toLowerCase(Locale.ROOT));
            json.addProperty("angle", angle);
            if (rescale) json.addProperty("rescale", true);
            return json;
        }
    }

    private static JsonArray vec(float[] values) {
        JsonArray array = new JsonArray();
        for (float v : values) {
            if (v == Math.rint(v)) array.add((int) v);
            else array.add(v);
        }
        return array;
    }
}
