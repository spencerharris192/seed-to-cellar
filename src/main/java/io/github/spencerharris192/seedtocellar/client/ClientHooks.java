package io.github.spencerharris192.seedtocellar.client;

import net.minecraft.client.gui.screens.Screen;

/** Small client-only queries used by shared code through DistExecutor (never loaded on servers). */
public final class ClientHooks {
    public static boolean shiftDown() {
        return Screen.hasShiftDown();
    }

    private ClientHooks() {}
}
