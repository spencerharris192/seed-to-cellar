package io.github.spencerharris192.seedtocellar.client;

import net.minecraft.client.Minecraft;

/** Small client-only queries for shared code, called only behind a client check (never loaded on servers). */
public final class ClientHooks {
    public static boolean shiftDown() {
        return Minecraft.getInstance().hasShiftDown();
    }

    private ClientHooks() {}
}
