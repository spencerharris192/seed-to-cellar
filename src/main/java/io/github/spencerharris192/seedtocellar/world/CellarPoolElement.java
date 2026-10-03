package io.github.spencerharris192.seedtocellar.world;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;

/**
 * A village building with a cellar under it (the Brewhouse). A village fits each house inside the space of the street
 * it joins, which starts at street level, so a building whose template reaches underground is never placed. This one
 * plans like any street-level building, and when it's built it also builds its cellar template below, turned and
 * clipped the same way: the cellar's x and z match the building's, and its top layer sits right under the floor.
 */
public class CellarPoolElement extends SinglePoolElement {
    public static final Codec<CellarPoolElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            templateCodec(), processorsCodec(), projectionCodec(),
            ResourceLocation.CODEC.fieldOf("cellar").forGetter(element -> element.cellar),
            Codec.INT.fieldOf("cellar_depth").forGetter(element -> element.depth)
    ).apply(instance, CellarPoolElement::new));

    private final ResourceLocation cellar;
    private final int depth;

    protected CellarPoolElement(Either<ResourceLocation, StructureTemplate> template, Holder<StructureProcessorList> processors,
                                StructureTemplatePool.Projection projection, ResourceLocation cellar, int depth) {
        super(template, processors, projection);
        this.cellar = cellar;
        this.depth = depth;
    }

    /** The building {@code template}, with {@code cellar} (a template {@code depth} blocks tall) under its floor. */
    public static CellarPoolElement of(ResourceLocation template, ResourceLocation cellar, int depth) {
        return new CellarPoolElement(Either.left(template), Holder.direct(new StructureProcessorList(List.of())),
                StructureTemplatePool.Projection.RIGID, cellar, depth);
    }

    @Override
    public boolean place(StructureTemplateManager templates, WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
                         BlockPos offset, BlockPos pos, Rotation rotation, BoundingBox box, RandomSource random, boolean keepJigsaws) {
        if (!super.place(templates, level, structures, generator, offset, pos, rotation, box, random, keepJigsaws)) return false;
        // the turn is about the template's origin and leaves height alone, so the same turn lines the cellar up below
        templates.getOrCreate(cellar).placeInWorld(level, offset.below(depth), pos, getSettings(rotation, box, keepJigsaws), random, 18);
        return true;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return ModStructures.CELLAR_BUILDING.get();
    }

    @Override
    public String toString() {
        return "Cellar[" + template + " over " + cellar + "]";
    }
}
