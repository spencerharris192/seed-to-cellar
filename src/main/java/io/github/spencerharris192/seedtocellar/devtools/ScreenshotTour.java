package io.github.spencerharris192.seedtocellar.devtools;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The photo tour for the mod's store page (development only: the devtools package never ships in the jar). With the
 * system property {@code seedtocellar.tour} naming a tour file (tools/make_showcase.py writes it), the dev client, once
 * in a world, follows its steps one at a time:
 * <ul>
 *   <li>{@code setup}: finds plains with dry land all round for the origin, freezes time, weather and growth, hides the HUD;</li>
 *   <li>{@code clear}: a flat plot (dirt below {@code ground}, grass at it, air above), relative to the origin, with
 *       {@code scatter} some wild grass and flowers;</li>
 *   <li>{@code place}: a structure template with its corner at {@code at};</li>
 *   <li>{@code replace}: turns one block into another in a box (a village building's street jigsaw into its path);</li>
 *   <li>{@code reshape}: lets every block in a box take the shape its neighbours give it (bar counters, fences);</li>
 *   <li>{@code shot}: moves the camera to {@code eye} (yaw, pitch), sets the time, waits for the chunks to draw, and
 *       saves screenshots/{@code name}.png;</li>
 *   <li>{@code done}: closes the game.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = SeedToCellar.MOD_ID, value = Dist.CLIENT)
public final class ScreenshotTour {
    private static final String FILE = System.getProperty("seedtocellar.tour");
    private static final double EYE = 1.62;

    private static JsonArray steps;
    private static int index;
    private static int wait = 100;          // let the world settle after joining
    private static BlockPos origin;
    private static JsonObject pendingShot;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (FILE == null || FMLLoader.isProduction() || event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        MinecraftServer server = mc.getSingleplayerServer();
        if (mc.player == null || server == null) return;
        try {
            if (steps == null) steps = JsonParser.parseString(Files.readString(Path.of(FILE))).getAsJsonObject().getAsJsonArray("steps");
            if (wait > 0) {
                wait--;
                return;
            }
            if (pendingShot != null) {          // the camera has been in place long enough: take it
                mc.getToasts().clear();
                String name = pendingShot.get("shot").getAsString() + ".png";
                Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), message -> {});
                SeedToCellar.LOGGER.info("[tour] shot {}", name);
                pendingShot = null;
                wait = 5;
                return;
            }
            if (index >= steps.size()) return;
            run(steps.get(index++).getAsJsonObject(), mc, server);
        } catch (Exception e) {
            SeedToCellar.LOGGER.error("[tour] step {} failed", index, e);
            index = Integer.MAX_VALUE;
        }
    }

    private static void run(JsonObject step, Minecraft mc, MinecraftServer server) {
        ServerLevel level = server.overworld();
        String player = mc.player.getGameProfile().getName();
        if (step.has("setup")) {
            server.submit(() -> {
                BlockPos at = dryPlains(level);
                level.getChunk(at);
                origin = new BlockPos(at.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, at.getX(), at.getZ()) - 1, at.getZ());
                for (String command : new String[]{"gamerule randomTickSpeed 0", "gamerule doDaylightCycle false",
                        "gamerule doWeatherCycle false", "gamerule doMobSpawning false", "weather clear", "difficulty peaceful",
                        "gamemode spectator " + player}) {
                    command(server, command);
                }
            }).join();
            SeedToCellar.LOGGER.info("[tour] origin {}", origin);
            mc.options.hideGui = true;
            wait = 20;
        } else if (step.has("clear")) {
            int[] b = ints(step.getAsJsonArray("clear"));
            int ground = step.get("ground").getAsInt();
            boolean scatter = step.has("scatter");
            int[] free = step.has("free") ? ints(step.getAsJsonArray("free")) : new int[]{1, 1, 0, 0};   // no plants inside the scene
            BlockState dirt = Blocks.DIRT.defaultBlockState(), grass = Blocks.GRASS_BLOCK.defaultBlockState(), air = Blocks.AIR.defaultBlockState();
            server.submit(() -> {
                BlockPos from = origin.offset(b[0], 0, b[2]), to = origin.offset(b[3], 0, b[5]);   // keep it loaded to build in
                command(server, String.format("forceload add %d %d %d %d", from.getX(), from.getZ(), to.getX(), to.getZ()));
                for (int x = b[0]; x <= b[3]; x++) {
                    for (int z = b[2]; z <= b[5]; z++) {
                        for (int y = b[1]; y <= b[4]; y++) {
                            level.setBlock(origin.offset(x, y, z), y < ground ? dirt : y == ground ? grass : air,
                                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                        }
                        boolean inside = x >= free[0] - 1 && x <= free[2] + 1 && z >= free[1] - 1 && z <= free[3] + 1;
                        if (scatter && !inside) {       // wild grass and a few flowers, so the plot isn't a lawn
                            int roll = level.random.nextInt(100);
                            BlockState plant = roll < 14 ? Blocks.GRASS.defaultBlockState() : roll == 14 ? Blocks.DANDELION.defaultBlockState()
                                    : roll == 15 ? Blocks.POPPY.defaultBlockState() : roll == 16 ? Blocks.OXEYE_DAISY.defaultBlockState()
                                    : roll == 17 ? Blocks.CORNFLOWER.defaultBlockState() : null;
                            if (plant != null) level.setBlock(origin.offset(x, ground + 1, z), plant, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                        }
                    }
                }
                BlockPos lo = origin.offset(b[0], b[1], b[2]);
                command(server, String.format("kill @e[type=!player,x=%d,y=%d,z=%d,dx=%d,dy=%d,dz=%d]", lo.getX(), lo.getY(), lo.getZ(),
                        b[3] - b[0], b[4] - b[1], b[5] - b[2]));
            }).join();
            wait = 10;
        } else if (step.has("place")) {
            int[] a = ints(step.getAsJsonArray("at"));
            BlockPos at = origin.offset(a[0], a[1], a[2]);
            server.submit(() -> command(server, String.format("place template %s %d %d %d", step.get("place").getAsString(),
                    at.getX(), at.getY(), at.getZ()))).join();
            wait = 10;
        } else if (step.has("replace")) {
            int[] b = ints(step.getAsJsonArray("replace"));
            Block from = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(SeedToCellar.parse(step.get("from").getAsString()));
            BlockState to = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(SeedToCellar.parse(step.get("to").getAsString())).defaultBlockState();
            server.submit(() -> {
                for (BlockPos pos : BlockPos.betweenClosed(origin.offset(b[0], b[1], b[2]), origin.offset(b[3], b[4], b[5]))) {
                    if (level.getBlockState(pos).is(from)) level.setBlock(pos, to, Block.UPDATE_CLIENTS);
                }
            }).join();
        } else if (step.has("reshape")) {
            int[] b = ints(step.getAsJsonArray("reshape"));
            server.submit(() -> {
                for (int pass = 0; pass < 2; pass++) {
                    for (BlockPos pos : BlockPos.betweenClosed(origin.offset(b[0], b[1], b[2]), origin.offset(b[3], b[4], b[5]))) {
                        BlockState state = level.getBlockState(pos);
                        BlockState shaped = Block.updateFromNeighbourShapes(state, level, pos);
                        if (shaped != state) level.setBlock(pos, shaped, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    }
                }
            }).join();
        } else if (step.has("shot")) {
            double[] eye = doubles(step.getAsJsonArray("eye"));
            server.submit(() -> {
                command(server, "time set " + step.get("time").getAsInt());
                command(server, String.format(java.util.Locale.ROOT, "tp %s %.2f %.2f %.2f %.1f %.1f", player, origin.getX() + eye[0],
                        origin.getY() + eye[1] - EYE, origin.getZ() + eye[2], step.get("yaw").getAsFloat(), step.get("pitch").getAsFloat()));
            }).join();
            pendingShot = step;
            wait = 80;          // the chunks around the camera draw
        } else if (step.has("done")) {
            SeedToCellar.LOGGER.info("[tour] done");
            mc.stop();
        }
    }

    /**
     * Plains with dry, flat land all round: the sites cover about 300 by 200 blocks east and south of the origin, so every
     * point sampled on a 32-block grid over that and the views beyond it must be land (not ocean, river, beach or
     * mountain), its heights within 6 blocks of each other. Searches out from spawn in rings until it finds such a place.
     */
    private static BlockPos dryPlains(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        for (int ring = 0; ring < 8; ring++) {
            for (int dir = 0; dir < (ring == 0 ? 1 : 8); dir++) {
                double angle = dir * Math.PI / 4;
                BlockPos start = spawn.offset((int) (Math.cos(angle) * ring * 1500), 0, (int) (Math.sin(angle) * ring * 1500));
                var found = level.findClosestBiome3d(biome -> biome.is(Biomes.PLAINS), start, 3200, 32, 64);
                if (found == null) continue;
                BlockPos at = found.getFirst();
                var generator = level.getChunkSource().getGenerator();
                var noise = level.getChunkSource().randomState();
                boolean dry = true;
                int low = Integer.MAX_VALUE, high = Integer.MIN_VALUE;
                for (int dx = -120; dx <= 380 && dry; dx += 32) {          // the sites, and the views beyond them
                    for (int dz = -160; dz <= 300 && dry; dz += 32) {
                        var biome = level.getBiome(new BlockPos(at.getX() + dx, 64, at.getZ() + dz));
                        dry = !biome.is(net.minecraft.tags.BiomeTags.IS_OCEAN) && !biome.is(net.minecraft.tags.BiomeTags.IS_RIVER)
                                && !biome.is(net.minecraft.tags.BiomeTags.IS_BEACH) && !biome.is(net.minecraft.tags.BiomeTags.IS_MOUNTAIN);
                        int height = generator.getBaseHeight(at.getX() + dx, at.getZ() + dz, Heightmap.Types.WORLD_SURFACE_WG, level, noise);
                        low = Math.min(low, height);
                        high = Math.max(high, height);
                    }
                }
                if (dry && high - low <= 6) return at;   // and flat, so a cleared plot leaves no cliffs
            }
        }
        return spawn;
    }

    private static void command(MinecraftServer server, String command) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
    }

    private static int[] ints(JsonArray array) {
        int[] out = new int[array.size()];
        for (int i = 0; i < out.length; i++) out[i] = array.get(i).getAsInt();
        return out;
    }

    private static double[] doubles(JsonArray array) {
        double[] out = new double[array.size()];
        for (int i = 0; i < out.length; i++) out[i] = array.get(i).getAsDouble();
        return out;
    }

    private ScreenshotTour() {}
}
