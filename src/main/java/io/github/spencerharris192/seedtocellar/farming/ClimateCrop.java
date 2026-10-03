package io.github.spencerharris192.seedtocellar.farming;

/** A plant with a preferred climate (GDD section 6.5), shown by the Hydrometer and Jade. */
public interface ClimateCrop {
    Climate climate();
}
