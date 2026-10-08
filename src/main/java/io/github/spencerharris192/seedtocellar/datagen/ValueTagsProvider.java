package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

import java.util.concurrent.CompletableFuture;

/**
 * A tags provider whose tags take the blocks, items or fluids themselves (the game's take only their registry keys), and
 * an optional entry or tag by its id.
 */
public abstract class ValueTagsProvider<T> extends TagsProvider<T> {
    private final Registry<T> registry;

    protected ValueTagsProvider(PackOutput output, Registry<T> registry, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, registry.key(), lookup, SeedToCellar.MOD_ID);
        this.registry = registry;
    }

    @Override
    protected Appender<T> tag(TagKey<T> tag) {
        return new Appender<>(super.tag(tag), registry);
    }

    public static final class Appender<T> implements TagAppender<T> {
        private final TagAppender<T> inner;
        private final Registry<T> registry;

        Appender(TagAppender<T> inner, Registry<T> registry) {
            this.inner = inner;
            this.registry = registry;
        }

        @SafeVarargs
        public final Appender<T> add(T... values) {
            for (T value : values) inner.add(registry.getResourceKey(value).orElseThrow(() -> new IllegalStateException("Unregistered: " + value)));
            return this;
        }

        /** An entry from another mod, there only if that mod is. */
        public Appender<T> addOptional(Identifier id) {
            inner.addOptional(ResourceKey.create(registry.key(), id));
            return this;
        }

        /** A tag that may not exist (another mod's). */
        public Appender<T> addOptionalTag(Identifier id) {
            inner.addOptionalTag(TagKey.create(registry.key(), id));
            return this;
        }

        @SafeVarargs
        @Override
        public final Appender<T> addTags(TagKey<T>... tags) {
            for (TagKey<T> tag : tags) inner.addTag(tag);
            return this;
        }

        @Override
        public Appender<T> add(ResourceKey<T> element) {
            inner.add(element);
            return this;
        }

        @Override
        public Appender<T> addOptional(ResourceKey<T> element) {
            inner.addOptional(element);
            return this;
        }

        @Override
        public Appender<T> addTag(TagKey<T> tag) {
            inner.addTag(tag);
            return this;
        }

        @Override
        public Appender<T> addOptionalTag(TagKey<T> tag) {
            inner.addOptionalTag(tag);
            return this;
        }

        @Override
        public Appender<T> add(TagEntry entry) {
            inner.add(entry);
            return this;
        }

        @Override
        public Appender<T> replace(boolean value) {
            inner.replace(value);
            return this;
        }

        @Override
        public Appender<T> remove(ResourceKey<T> element) {
            inner.remove(element);
            return this;
        }

        @Override
        public Appender<T> remove(TagKey<T> tag) {
            inner.remove(tag);
            return this;
        }
    }
}
