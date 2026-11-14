package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.labyrinth.DoorBleed;
import io.github.knaitoe.theoldesthouse.network.DoorLeaksPayload;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * What a shut door lets out into the hall, drawn for this viewer alone: something of the room on the hall's floor, the light it
 * has on, what is in its air. Deals remain private even when players share a door.
 * <p>
 * 0.4.576 (docs/DOOR_BLEEDS_0_4_576.md): every room has its own {@link DoorBleed}. The four strips that were there before are
 * drawn as they were. Every light that moves is scaled by the reader's screen-effects setting, with no floor: at nothing, each
 * is a steady light ({@link #cell}). At full, none of them loses more than a sixth of its brightness and gets it back more
 * than three times in a second: the quick ones (a projector's flutter, a neon tube's hum) are slighter than that.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class DoorLeakRenderer {
    private DoorLeakRenderer() {}
    private static final DoorBleed[] BLEEDS = DoorBleed.values();
    /** How far along the hall's floor the light reaches, in strips out from the door, and how many it is cut into across. */
    private static final int ROWS = 4, COLUMNS = 8;
    /** The Masque's seven rooms, in their order, the last the one with the clock. */
    private static final int[] SUITE = {0x3A5AC8, 0x7A3AC8, 0x3AC86A, 0xE8842A, 0xF2F2F2, 0x9A5AE8, 0xB01818};
    /** A test card's bars, left to right. */
    private static final int[] CARD = {0xC0C0C0, 0xC0C000, 0x00C0C0, 0x00C000, 0xC000C0, 0xC00000, 0x0000C0, 0x181818};
    /** The string of bulbs over the beach, after the first, which is the bleed's own colour. */
    private static final int[] STRING = {0, 0xE8504A, 0x4AC86A, 0x4A8AE8};
    /** The cabin's lamp, a unit to an entry: HOME, and a breath before it begins again. */
    private static final boolean[] LAMP = keyed("HOME");
    /** Ticks to one unit of the lamp. */
    private static final int LAMP_UNIT = 6;
    /** How near somebody has to come for a light that waits for them, and how long it stays on after they have gone. */
    private static final double COME_NEAR = 4.5;
    private static final int STAY_ON = 70;
    private static final Long2LongOpenHashMap WOKEN = new Long2LongOpenHashMap();
    static { WOKEN.defaultReturnValue(Long.MIN_VALUE / 2); }
    /** One cell of the light: red, green, blue and how bright. Filled by {@link #cell}; the render thread's alone. */
    private static final float[] CELL = new float[4];

    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        var buffers = mc.renderBuffers().bufferSource();
        RenderType type = RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS);
        VertexConsumer out = buffers.getBuffer(type);
        pose.pushPose(); pose.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = pose.last().pose();
        double time = mc.level.getGameTime() + mc.getTimer().getGameTimeDeltaPartialTick(false);
        float effects = Comfort.effects();
        for (DoorLeaksPayload.Leak leak : DoorLeakClientState.leaks()) {
            BlockState door = mc.level.getBlockState(leak.door());
            if (!(door.getBlock() instanceof DoorBlock) || door.getValue(DoorBlock.OPEN)
                    || camera.distanceToSqr(Vec3.atCenterOf(leak.door())) > 144 || leak.kind() < 0 || leak.kind() >= BLEEDS.length) continue;
            Direction facing = Direction.from3DDataValue(leak.facing());
            DoorBleed bleed = BLEEDS[leak.kind()];
            draw(out, matrix, leak.door(), facing, bleed, time, effects,
                    bleed.rhythm() == DoorBleed.Rhythm.CLICK ? woken(mc.level, leak.door()) : 1F,
                    LevelRenderer.getLightColor(mc.level, leak.door().relative(facing)));
        }
        pose.popPose(); buffers.endBatch(type);
    }

    /**
     * Everything one door lets out onto the hall's floor: its water, its light, and over them its strip or its picture.
     * {@code light} is the hall's own light at the door, which the floor's things are seen by; the room's light is its own.
     */
    static void draw(VertexConsumer out, Matrix4f matrix, BlockPos door, Direction facing, DoorBleed bleed, double time,
                     float effects, float woken, int light) {
        Minecraft mc = Minecraft.getInstance();
        if (bleed.floor() == DoorBleed.Floor.WATER) {
            // Animated vanilla water atlas, in overlapping shallow patches rather than a square block.
            TextureAtlasSprite water = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ResourceLocation.withDefaultNamespace("block/water_still"));
            int c = bleed.water();
            for (int i = 0; i < 4; i++) quad(out, matrix, door, facing, -.38F + .05F * i,
                    .36F - .03F * i, .18F + i * .27F, .53F + i * .27F, .009F + i * .001F,
                    water, c >> 16 & 255, c >> 8 & 255, c & 255, 175 - i * 28, light);
        }
        if (bleed.light() != 0) light(out, matrix, door, facing, bleed, time, effects, woken);
        if (bleed.floor() == DoorBleed.Floor.STRIP) {
            TextureAtlasSprite sprite = strip(bleed.strip());
            float length = bleed.strip() == DoorBleed.Strip.CARPET ? 1.65F : bleed.strip() == DoorBleed.Strip.BOARDS ? .85F : 1.2F;
            quad(out, matrix, door, facing, -.4F, .4F, .18F, length, .017F, sprite, 235, 235, 235, 230, light);
            // An irregular frayed edge makes fabric read as a creep through the door rather than a floor replacement.
            if (bleed.strip() != DoorBleed.Strip.BOARDS) for (int i = 0; i < 5; i++) {
                float x = -.37F + i * .15F;
                quad(out, matrix, door, facing, x, x + .07F, length, length + .12F + (i % 2) * .16F,
                        .018F, sprite, 235, 235, 235, 195, light);
            }
        } else if (bleed.floor() == DoorBleed.Floor.DRAWN) {
            // The room's own picture, its first row against the door: a block wide, and as deep as it was drawn. Its left
            // is the left of somebody standing in the hall facing the door, so what is written on it reads from there.
            TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(
                    ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, bleed.texture()));
            quad(out, matrix, door, facing, .5F, -.5F, .18F, .18F + bleed.depth(), .017F, sprite, 255, 255, 255, 255, light);
        }
    }

    /** The light under the door: four strips out along the floor, each a little wider and fainter than the one before. */
    private static void light(VertexConsumer out, Matrix4f matrix, BlockPos door, Direction facing,
                              DoorBleed bleed, double time, float effects, float woken) {
        TextureAtlasSprite white = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                .getBlockModel(Blocks.WHITE_CONCRETE.defaultBlockState()).getParticleIcon();
        int columns = across(bleed.rhythm()) ? COLUMNS : 1;
        for (int row = 0; row < ROWS; row++) {
            float half = .44F + row * .035F, v0 = .18F + row * .28F;
            for (int column = 0; column < columns; column++) {
                cell(bleed, time, door, column, columns, row, effects, woken);
                int alpha = Mth.clamp(Math.round((92 - 20 * row) * bleed.glow() * CELL[3]), 0, 210);
                if (alpha < 2) continue;
                // The first column is at the left of somebody in the hall facing the door.
                quad(out, matrix, door, facing, half - 2 * half * column / columns, half - 2 * half * (column + 1) / columns,
                        v0, v0 + .28F, .014F, white, Math.round(CELL[0] * 255), Math.round(CELL[1] * 255), Math.round(CELL[2] * 255),
                        alpha, LightTexture.FULL_BRIGHT);
            }
        }
    }
    /** The rhythms that differ from one side of the door to the other. The rest are one piece across. */
    private static boolean across(DoorBleed.Rhythm rhythm) {
        return switch (rhythm) {
            case SWEEP, SWAY, FAN, SEQUENCE, GRILLE, CAUSTIC, BARS, NEON, BULBS -> true;
            default -> false;
        };
    }
    /** A light that waits for somebody: on while anybody is near its door and for a few seconds after, and all but out otherwise. */
    private static float woken(ClientLevel level, BlockPos door) {
        long now = level.getGameTime(), key = door.asLong();
        Vec3 at = Vec3.atBottomCenterOf(door);
        for (var player : level.players()) if (!player.isSpectator() && player.position().distanceToSqr(at) <= COME_NEAR * COME_NEAR) {
            if (WOKEN.size() > 256) WOKEN.clear();
            WOKEN.put(key, now); break;
        }
        long since = now - WOKEN.get(key);
        return since >= 0 && since < STAY_ON ? 1F : .04F;
    }

    /**
     * One cell of a bleed's light at this moment, into {@link #CELL}. Each rhythm gives what it is doing now and what it is when
     * it holds still; the reader's screen-effects setting says how far from still it is allowed to go, with no floor. At nothing,
     * every light is its still self: one colour, one brightness, and for the lights with a pattern across them, the pattern at
     * rest.
     */
    static void cell(DoorBleed bleed, double time, BlockPos door, int column, int columns, int row, float effects, float woken) {
        int seed = Long.hashCode(door.asLong() * 0x9E3779B97F4A7C15L);
        double s = time / 20 * bleed.rate() + (seed & 1023) * .37;       // seconds, and no two doors in step
        float phase = (seed >>> 10 & 255) / 255F * Mth.TWO_PI;
        float x = (column + .5F) / columns - .5F;                         // across the door from the hall, left to right: -.5 to .5
        int first = bleed.light(), second = bleed.light2() != 0 ? bleed.light2() : first;
        int now = first, still = first;                                   // the colour moving, and at rest
        float level = 1F, rest = 1F;                                      // how bright moving, and at rest
        switch (bleed.rhythm()) {
            case STEADY -> {}
            case BREATHE -> { rest = .62F; level = rest + .38F * sin(s * Mth.TWO_PI / 6 + phase); }
            case FIRE -> { rest = .72F; level = rest + .16F * sin(s * 7.1 + phase) + .08F * sin(s * 13.7 + phase * 2) + .04F * sin(s * 23.3); }
            case CANDLE -> { rest = .8F; level = rest + .1F * sin(s * 5.3 + phase) + .06F * sin(s * 11.9 + phase * 2) + .04F * sin(s * 19.1); }
            case TUBE -> {
                // A tube that is nearly sound: it holds, and every seven seconds or so it stutters: three fifths of a second
                // in fifths, so it is never down and up more than twice in one.
                rest = .95F; double u = s % 7;
                level = u < .6 && chance((long) (u * 5), seed + (long) (s / 7)) < .5F ? .35F : 1F;
            }
            case TV -> {
                // A screen in another room: a brightness to a shot, and a quick change between them.
                rest = .78F; double u = s * 2.5; long shot = (long) u; float f = (float) (u - shot);
                float a = chance(shot, seed), b = chance(shot + 1, seed);
                level = .55F + .45F * (f < .85F ? a : Mth.lerp((f - .85F) / .15F, a, b));
            }
            case FLUTTER -> {
                // A projector: the gate's flutter, and the picture's colour changing with the shot.
                rest = .93F; level = .86F + .14F * chance((long) (time / 2), seed);
                double u = s / 3.2; long shot = (long) u; float f = (float) (u - shot);
                float a = chance(shot, seed ^ 77) < .5F ? 0 : 1, b = chance(shot + 1, seed ^ 77) < .5F ? 0 : 1;
                now = mix(first, second, f < .9F ? a : Mth.lerp((f - .9F) / .1F, a, b)); still = mix(first, second, .5F);
            }
            case BEAT -> {
                rest = .51F; double beat = s * 2; long count = (long) beat;
                level = .35F + .65F * (float) Math.exp(-4 * (beat - count));
                now = (count & 1) == 0 ? first : second; still = mix(first, second, .5F);
            }
            case BLINK -> {
                // On and off, a second to the pair; with two colours, one and then the other.
                float f = (float) (s - Math.floor(s));
                float on = f < .44F ? 1 : f < .5F ? 1 - (f - .44F) / .06F : f < .94F ? 0 : (f - .94F) / .06F;
                if (second != first) { now = mix(second, first, on); still = mix(first, second, .5F); }
                else { rest = .56F; level = .12F + .88F * on; }
            }
            case FLASH -> { rest = .1F; level = .08F + 1.6F * (float) Math.exp(-9 * (s % 7)); }
            case SWEEP -> {
                // Headlights going by: a band of light across the floor, and the dark again.
                rest = .2F; double u = s % 6;
                level = u < 1.2 ? .15F + .85F * bell((x - (-.9F + (float) (u / 1.2) * 1.8F)) / .25F) : .15F;
            }
            case SWAY -> { rest = .45F + .55F * bell(x / .3F); level = .45F + .55F * bell((x - .28F * sin(s * Mth.TWO_PI / 3.2 + phase)) / .3F); }
            case FAN -> { rest = .9F; float blade = Math.max(0, (float) Math.cos(Mth.TWO_PI * (x * 1.2 + row * .18 - s * 1.1))); level = 1 - .45F * blade * blade * blade; }
            case CYCLE -> {
                // The suite's rooms, one after another, four seconds to a room and the last second of each going over.
                double u = s / 4; long room = (long) u; float f = (float) (u - room);
                int a = SUITE[(int) (room % SUITE.length)], b = SUITE[(int) ((room + 1) % SUITE.length)];
                now = f < .75F ? a : mix(a, b, (f - .75F) / .25F);
            }
            case SEQUENCE -> {
                // A house's windows going dark one at a time, and all of them lit again.
                rest = .75F; double u = s % 16, start = 4 + 2.5 * (column * 4 / columns);
                float lit = u < start ? 1 : u < start + .4 ? 1 - (float) ((u - start) / .4) : u < 14 ? 0 : u < 14.4 ? (float) ((u - 14) / .4) : 1;
                level = .15F + .85F * lit;
            }
            case MORSE -> { rest = .45F; level = LAMP[(int) ((long) (time * bleed.rate() / LAMP_UNIT) % LAMP.length)] ? 1F : .1F; }
            case GRILLE -> {
                float lattice = (column + row & 1) == 0 ? 1F : .3F;
                rest = .8F * lattice; level = (.8F + .1F * sin(s * 5.3 + phase) + .06F * sin(s * 11.9 + phase * 2)) * lattice;
            }
            case CAUSTIC -> { rest = .6F; level = rest + .4F * sin(x * 9 + s * 1.3 + Math.sin(row * 1.7 + s * .9) * 1.5); }
            case BARS -> now = still = CARD[column * CARD.length / columns];
            case CLICK -> { rest = .5F; level = woken; }
            case NEON -> {
                // A sign in two colours, side by side. It hums, and the second tube drops out now and then.
                rest = .92F; now = still = column * 2 < columns ? first : second; level = rest + .08F * sin(s * 37);
                if (column * 2 >= columns && s % 5.3 < .12) level = .2F;
            }
            case BULBS -> {
                int bulb = column * STRING.length / columns;
                now = still = bulb == 0 ? first : STRING[bulb]; rest = .85F; level = rest + .15F * sin(s * 1.3 + bulb * 1.9);
            }
        }
        int colour = effects >= 1 ? now : mix(still, now, effects);
        CELL[0] = (colour >> 16 & 255) / 255F; CELL[1] = (colour >> 8 & 255) / 255F; CELL[2] = (colour & 255) / 255F;
        CELL[3] = Math.max(0, rest + (level - rest) * effects);
    }
    private static float sin(double a) { return (float) Math.sin(a); }
    /** One at the middle, falling away on both sides. */
    private static float bell(float d) { return (float) Math.exp(-d * d); }
    /** The same number from the same two, between nothing and one: what a rhythm does next without anything being kept. */
    private static float chance(long a, long b) {
        long h = a * 0x9E3779B97F4A7C15L ^ b * 0xC2B2AE3D27D4EB4FL;
        h ^= h >>> 29; h *= 0xBF58476D1CE4E5B9L; h ^= h >>> 32;
        return (h & 0xFFFFFF) / (float) 0x1000000;
    }
    private static int mix(int a, int b, float t) {
        t = Mth.clamp(t, 0, 1);
        int r = Math.round(Mth.lerp(t, a >> 16 & 255, b >> 16 & 255)), g = Math.round(Mth.lerp(t, a >> 8 & 255, b >> 8 & 255)),
                bl = Math.round(Mth.lerp(t, a & 255, b & 255));
        return r << 16 | g << 8 | bl;
    }
    /** A word as a lamp keys it: a unit on for a dot and three for a dash, a unit between, three between letters, seven after. */
    private static boolean[] keyed(String word) {
        String[] code = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--", "-.", "---", ".--.",
                "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--.."};
        var units = new StringBuilder();
        for (char c : word.toLowerCase(java.util.Locale.ROOT).toCharArray()) if (c >= 'a' && c <= 'z') {
            for (char pulse : code[c - 'a'].toCharArray()) units.append(pulse == '.' ? "x " : "xxx ");
            units.append("  ");
        }
        units.append("    ");
        boolean[] on = new boolean[units.length()];
        for (int i = 0; i < on.length; i++) on[i] = units.charAt(i) == 'x';
        return on;
    }

    @SubscribeEvent public static void particles(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) return;
        long tick = mc.level.getGameTime();
        for (DoorLeaksPayload.Leak leak : DoorLeakClientState.leaks()) {
            if (leak.kind() < 0 || leak.kind() >= BLEEDS.length) continue;
            DoorBleed bleed = BLEEDS[leak.kind()];
            if (bleed.air() == DoorBleed.Air.NONE || tick % every(bleed.air()) != 0
                    || mc.player.position().distanceToSqr(Vec3.atCenterOf(leak.door())) > 64) continue;
            BlockState door = mc.level.getBlockState(leak.door());
            if (!(door.getBlock() instanceof DoorBlock) || door.getValue(DoorBlock.OPEN)) continue;
            mote(mc.level, bleed, leak.door(), Direction.from3DDataValue(leak.facing()));
        }
    }
    /** Ticks between one mote and the next. */
    private static int every(DoorBleed.Air air) {
        return switch (air) {
            case NONE -> Integer.MAX_VALUE;
            case DRIP -> 8;
            case SNOW -> 5;
            case ASH, SAND -> 6;
            case DUST -> 9;
            case STEAM -> 7;
            case FLIES -> 3;
        };
    }
    /** One mote of what is in the room's air, at the foot of its door on the hall's side. */
    private static void mote(ClientLevel level, DoorBleed bleed, BlockPos door, Direction facing) {
        RandomSource r = level.random;
        if (bleed.air() == DoorBleed.Air.DRIP) {
            Vec3 at = Vec3.atBottomCenterOf(door).add(facing.getStepX() * .35, .14, facing.getStepZ() * .35);
            level.addParticle(ParticleTypes.DRIPPING_WATER, at.x, at.y, at.z, 0, 0, 0);
            return;
        }
        double side = (r.nextDouble() - .5) * .8, away, up, drift = 0, rise = 0;
        ParticleOptions what;
        switch (bleed.air()) {
            case SNOW -> { what = ParticleTypes.SNOWFLAKE; away = .3 + r.nextDouble() * .8; up = .04 + r.nextDouble() * .35; drift = .012; rise = -.002; }
            case ASH -> { what = ParticleTypes.ASH; away = .3 + r.nextDouble() * .6; up = .1 + r.nextDouble() * .5; }
            case DUST -> { what = dust(bleed.airColour(), .5F); away = .3 + r.nextDouble() * .7; up = .05 + r.nextDouble() * .6; drift = .02; }
            case STEAM -> { what = ParticleTypes.WHITE_SMOKE; away = .3 + r.nextDouble() * .2; side *= .85; up = .03; drift = .006; rise = .006 + r.nextDouble() * .006; }
            case SAND -> { what = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()); away = .3 + r.nextDouble() * .5; up = .08 + r.nextDouble() * .18; }
            default -> { what = dust(0x12100D, .3F); away = .2 + r.nextDouble() * .7; side *= 1.1; up = .15 + r.nextDouble() * .8; drift = (r.nextDouble() - .5) * .5; rise = (r.nextDouble() - .5) * .4; }
        }
        Vec3 at = Vec3.atBottomCenterOf(door).add(facing.getStepX() * away - facing.getStepZ() * side, up, facing.getStepZ() * away + facing.getStepX() * side);
        level.addParticle(what, at.x, at.y, at.z, facing.getStepX() * drift, rise, facing.getStepZ() * drift);
    }
    private static DustParticleOptions dust(int colour, float size) {
        return new DustParticleOptions(new Vector3f((colour >> 16 & 255) / 255F, (colour >> 8 & 255) / 255F, (colour & 255) / 255F), size);
    }

    private static TextureAtlasSprite strip(DoorBleed.Strip strip) {
        BlockState state = switch (strip) {
            case CARPET -> HouseBlocks.HOTEL_CARPET.get().defaultBlockState();
            case CLOTH -> Blocks.WHITE_WOOL.defaultBlockState();
            case FUR -> Blocks.GRAY_WOOL.defaultBlockState();
            case BOARDS -> Blocks.SPRUCE_PLANKS.defaultBlockState();
        };
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state).getParticleIcon();
    }
    private static void quad(VertexConsumer out, Matrix4f matrix, BlockPos door, Direction facing,
                             float u0, float u1, float v0, float v1, float height, TextureAtlasSprite sprite,
                             int r, int g, int b, int a, int light) {
        vertex(out, matrix, door, facing, u0, v0, height, sprite.getU0(), sprite.getV0(), r, g, b, a, light);
        vertex(out, matrix, door, facing, u1, v0, height, sprite.getU1(), sprite.getV0(), r, g, b, a, light);
        vertex(out, matrix, door, facing, u1, v1, height, sprite.getU1(), sprite.getV1(), r, g, b, a, light);
        vertex(out, matrix, door, facing, u0, v1, height, sprite.getU0(), sprite.getV1(), r, g, b, a, light);
    }
    private static void vertex(VertexConsumer out, Matrix4f matrix, BlockPos door, Direction facing, float u, float v,
                               float y, float s, float t, int r, int g, int b, int a, int light) {
        float x = door.getX() + .5F - facing.getStepZ() * u + facing.getStepX() * v;
        float z = door.getZ() + .5F + facing.getStepX() * u + facing.getStepZ() * v;
        out.addVertex(matrix, x, door.getY() + y, z).setColor(r, g, b, a).setUv(s, t)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
    }
}
