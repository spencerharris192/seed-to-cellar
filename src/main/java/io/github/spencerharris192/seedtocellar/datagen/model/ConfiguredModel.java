package io.github.spencerharris192.seedtocellar.datagen.model;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/** One model a blockstate shows, turned and weighted. */
public record ConfiguredModel(ModelFile model, int rotationX, int rotationY, boolean uvLock, int weight) {
    public ConfiguredModel(ModelFile model) {
        this(model, 0, 0, false, 1);
    }

    JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("model", model.getLocation().toString());
        if (rotationX != 0) json.addProperty("x", rotationX);
        if (rotationY != 0) json.addProperty("y", rotationY);
        if (uvLock) json.addProperty("uvlock", true);
        if (weight != 1) json.addProperty("weight", weight);
        return json;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<ConfiguredModel> done = new ArrayList<>();
        private ModelFile model;
        private int x, y, weight = 1;
        private boolean uvLock;

        public Builder modelFile(ModelFile model) {
            this.model = model;
            return this;
        }

        public Builder rotationX(int x) {
            this.x = x;
            return this;
        }

        public Builder rotationY(int y) {
            this.y = y;
            return this;
        }

        public Builder uvLock(boolean uvLock) {
            this.uvLock = uvLock;
            return this;
        }

        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        /** Another model for the same state (one is picked at random, by weight). */
        public Builder nextModel() {
            done.add(current());
            model = null;
            x = y = 0;
            weight = 1;
            uvLock = false;
            return this;
        }

        private ConfiguredModel current() {
            if (model == null) throw new IllegalStateException("No model set");
            return new ConfiguredModel(model, x, y, uvLock, weight);
        }

        public ConfiguredModel[] build() {
            List<ConfiguredModel> all = new ArrayList<>(done);
            all.add(current());
            return all.toArray(ConfiguredModel[]::new);
        }
    }
}
