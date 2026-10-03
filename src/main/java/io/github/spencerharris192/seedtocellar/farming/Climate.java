package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.SeedToCellar;

import java.util.List;
import java.util.Locale;

/** The climate a crop prefers (GDD section 6.5). Outside it, the crop grows at half speed. */
public enum Climate {
    COLD, TEMPERATE, WARM, HOT;

    /**
     * The Serene Seasons seasons a crop of this climate is fertile in (GDD section 6.5): cold crops skip
     * summer, warm ones skip autumn and winter, hot ones grow only in summer.
     */
    public List<String> seasons() {
        return switch (this) {
            case COLD -> List.of("spring", "autumn", "winter");
            case TEMPERATE -> List.of("spring", "summer", "autumn");
            case WARM -> List.of("spring", "summer");
            case HOT -> List.of("summer");
        };
    }

    public String translationKey() {
        return "climate." + SeedToCellar.MOD_ID + "." + name().toLowerCase(Locale.ROOT);
    }
}
