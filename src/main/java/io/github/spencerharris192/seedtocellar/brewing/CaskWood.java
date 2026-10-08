package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import java.util.Optional;

/**
 * The twelve vanilla woods a cask can be made from (GDD section 13). Each drink that ages lists its ideal
 * woods ({@link DrinkProfile#woods()}): there the aging star comes on time, in any other wood it takes
 * twice as long. Crimson and warped casks age twice as fast but never give the aging star.
 */
public enum CaskWood {
    OAK("oak", MapColor.WOOD, SoundType.WOOD, false),
    SPRUCE("spruce", MapColor.PODZOL, SoundType.WOOD, false),
    BIRCH("birch", MapColor.SAND, SoundType.WOOD, false),
    JUNGLE("jungle", MapColor.DIRT, SoundType.WOOD, false),
    ACACIA("acacia", MapColor.COLOR_ORANGE, SoundType.WOOD, false),
    DARK_OAK("dark_oak", MapColor.COLOR_BROWN, SoundType.WOOD, false),
    MANGROVE("mangrove", MapColor.COLOR_RED, SoundType.WOOD, false),
    CHERRY("cherry", MapColor.TERRACOTTA_WHITE, SoundType.CHERRY_WOOD, false),
    PALE_OAK("pale_oak", MapColor.QUARTZ, SoundType.WOOD, false),
    POPLAR("poplar", MapColor.COLOR_LIGHT_GRAY, SoundType.WOOD, false),
    CRIMSON("crimson", MapColor.CRIMSON_STEM, SoundType.NETHER_WOOD, true),
    WARPED("warped", MapColor.WARPED_STEM, SoundType.NETHER_WOOD, true);

    private final String id;
    private final MapColor mapColor;
    private final SoundType sound;
    private final boolean nether;

    CaskWood(String id, MapColor mapColor, SoundType sound, boolean nether) {
        this.id = id;
        this.mapColor = mapColor;
        this.sound = sound;
        this.nether = nether;
    }

    public String id() {
        return id;
    }

    public MapColor mapColor() {
        return mapColor;
    }

    public SoundType sound() {
        return sound;
    }

    /** Nether wood: doesn't burn, ages twice as fast, never gives the aging star. */
    public boolean nether() {
        return nether;
    }

    /** Years that pass per in-game day. */
    public int speed() {
        return nether ? 2 : 1;
    }

    public boolean givesStar() {
        return !nether;
    }

    public Component displayName() {
        return Component.translatable("wood.seedtocellar." + id);
    }

    public static Optional<CaskWood> byId(String id) {
        for (CaskWood wood : values()) if (wood.id.equals(id)) return Optional.of(wood);
        return Optional.empty();
    }
}
