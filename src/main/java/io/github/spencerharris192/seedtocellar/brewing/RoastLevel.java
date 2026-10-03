package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

/** The kiln's roast setting. Darker takes longer. Malt: pale / amber / black; hops dry on Light. */
public enum RoastLevel implements StringRepresentable {
    LIGHT("light"),
    MEDIUM("medium"),
    DARK("dark");

    public static final com.mojang.serialization.Codec<RoastLevel> CODEC = StringRepresentable.fromEnum(RoastLevel::values);

    private final String name;

    RoastLevel(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public Component displayName() {
        return Component.translatable("roast.seedtocellar." + name);
    }

    public RoastLevel next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static RoastLevel byName(String name) {
        for (RoastLevel level : values()) if (level.name.equals(name)) return level;
        return LIGHT;
    }
}
