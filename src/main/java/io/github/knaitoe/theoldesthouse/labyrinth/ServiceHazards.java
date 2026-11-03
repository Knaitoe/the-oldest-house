package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

final class ServiceHazards {
    private static final int[] RETURNS = {-9, -17, -25};
    private static final int[] PANELS = {-9, -18, -27};
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private ServiceHazards() {}

    static boolean is(LabyrinthPlace place) {
        return place == LabyrinthPlace.RETURN_AIR || place == LabyrinthPlace.SETTLING_CELLAR;
    }

    static void build(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        if (place == LabyrinthPlace.RETURN_AIR) buildReturnAir(level, base);
        else buildSettlingCellar(level, base);
    }

    static void tick(ServerLevel level, BlockPos base, LabyrinthPlace place, CompoundTag room,
                     long clock, List<ServerPlayer> readers) {
        if (place == LabyrinthPlace.RETURN_AIR) returnAir(level, base, clock, readers);
        else settling(level, base, room, readers);
    }

    private static void buildReturnAir(ServerLevel level, BlockPos base) {
        var wall = Blocks.POLISHED_ANDESITE.defaultBlockState();
        LabyrinthBuilder.room(level, base, -5, 5, 3, -32, -1, wall, Blocks.SMOOTH_STONE.defaultBlockState(), wall);
        var bars = Blocks.IRON_BARS.defaultBlockState()
                .setValue(IronBarsBlock.NORTH, true).setValue(IronBarsBlock.SOUTH, true);
        for (int i = 0; i < RETURNS.length; i++) {
            int z = RETURNS[i], side = i % 2 == 0 ? 1 : -1;
            fill(level, base, side * 5, 0, z - 1, side * 5, 2, z + 1, bars);
            fill(level, base, side * 6, 0, z - 1, side * 6, 2, z + 1, Blocks.BLACK_CONCRETE.defaultBlockState());
            fill(level, base, -side * 3, 0, z - 2, -side * 3, 2, z + 2, wall);
            LabyrinthBuilder.hangLantern(level, base.offset(-side * 4, 3, z + 2), false);
        }
        for (int z : new int[]{-3, -30}) LabyrinthBuilder.hangLantern(level, base.offset(0, 3, z), false);
    }

    private static void returnAir(ServerLevel level, BlockPos base, long clock, List<ServerPlayer> readers) {
        for (int i = 0; i < RETURNS.length; i++) {
            int z = RETURNS[i], side = i % 2 == 0 ? 1 : -1;
            int phase = (int) Math.floorMod(clock + 42L * i, 156);
            var vent = base.offset(side * 5, 1, z);
            if (phase == 88 || phase == 100)
                level.playSound(null, vent, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, .7F, .63F);
            if (phase >= 88 && phase < 112 && phase % 4 == 0)
                level.sendParticles(ParticleTypes.ASH, vent.getX() + .5, vent.getY(), vent.getZ() + .5, 4, .18, .3, 1, .01);
            if (phase == 112)
                level.playSound(null, vent, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, .75F, .55F);
            if (phase < 112 || phase >= 144 || phase % 4 != 0) continue;
            int left = side == 1 ? -2 : -5, right = side == 1 ? 6 : 3;
            var draft = volume(base, left, 0, z - 1, right, 3, z + 2);
            level.sendParticles(ParticleTypes.ASH, base.getX() + side, vent.getY(), vent.getZ() + .5, 18, 2, .6, 1, .02);
            for (var reader : readers) {
                if (reader.isShiftKeyDown() || !reader.getBoundingBox().intersects(draft)) continue;
                double speed = reader.getDeltaMovement().x * side;
                if (speed >= .45) continue;
                reader.push(side * Math.min(.27, .45 - speed), 0, 0);
                reader.hurtMarked = true;
            }
        }
    }

    private static void buildSettlingCellar(ServerLevel level, BlockPos base) {
        var wall = HazardBlock.of(HazardBlock.Kind.PIT_STONE);
        var timber = Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState();
        var boards = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        LabyrinthBuilder.room(level, base, -5, 5, 3, -35, -1, wall, boards, wall);
        for (int i = 0; i < PANELS.length; i++) {
            int z = PANELS[i], side = i % 2 == 0 ? 1 : -1;
            fill(level, base, -3, -5, z - 1, 3, -5, z + 1, wall);
            fill(level, base, -3, -4, z - 1, 3, -2, z + 1, Blocks.AIR.defaultBlockState());
            for (int end : new int[]{z - 2, z + 2})
                fill(level, base, -4, -4, end, 4, -2, end, wall);
            for (int bank : new int[]{-1, 1}) {
                fill(level, base, bank * 4, -4, z - 1, bank * 4, -2, z + 1, wall);
                // Steps stay along the back edge so the floor in front clears a falling reader.
                for (int step = 1; step <= 3; step++)
                    fill(level, base, bank * step, -4, z - 1, bank * step, -5 + step, z - 1, wall);
                set(level, base, bank, -1, z + 1, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
            }
            fill(level, base, 0, -4, z - 1, 0, -1, z + 1, timber);
            fill(level, base, 0, 0, z + 3, side * 5, 2, z + 3, wall);
            LabyrinthBuilder.hangLantern(level, base.offset(-side * 2, 3, z + 3), false);
        }
        LabyrinthBuilder.hangLantern(level, base.offset(0, 3, -33), false);
    }

    private static void settling(ServerLevel level, BlockPos base, CompoundTag room, List<ServerPlayer> readers) {
        for (int z : PANELS) for (int side : new int[]{-1, 1}) {
            var seam = base.offset(side, -1, z + 1);
            if (!level.getBlockState(seam).is(Blocks.CRACKED_STONE_BRICKS)) continue;
            int left = side < 0 ? -3 : 1, right = side < 0 ? 0 : 4;
            var panel = volume(base, left, 0, z - 1, right, 1, z + 2);
            int load = 0;
            for (var reader : readers) {
                if (reader.getY() - base.getY() < .08 && reader.getY() >= base.getY()
                        && reader.getBoundingBox().intersects(panel)) load += reader.isSprinting() ? 3 : 1;
            }
            if (load == 0) continue;
            String key = "Cellar" + z + ":" + side;
            int wear = room.getInt(key), next = Math.min(48, wear + load);
            room.putInt(key, next);
            if (wear < 12 && next >= 12 || wear < 32 && next >= 32) {
                level.playSound(null, seam, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, .8F, .62F);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_PLANKS.defaultBlockState()),
                        seam.getX() + .5, seam.getY() + 1, seam.getZ() + .5, 9, .45, .12, .6, .04);
            }
            if (next < 48) continue;
            // The catch bay and its steps already exist before the shared floor gives way.
            fill(level, base, left, -1, z - 1, right - 1, -1, z + 1, Blocks.AIR.defaultBlockState());
            level.playSound(null, seam, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1F, .58F);
        }
    }

    private static AABB volume(BlockPos b, int x0, int y0, int z0, int x1, int y1, int z1) {
        return new AABB(b.getX() + x0, b.getY() + y0, b.getZ() + z0, b.getX() + x1, b.getY() + y1, b.getZ() + z1);
    }

    private static void set(ServerLevel level, BlockPos base, int x, int y, int z, BlockState state) {
        BuildBlocks.set(level, base.offset(x, y, z), state, FLAGS);
    }

    private static void fill(ServerLevel level, BlockPos base, int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        BuildBlocks.box(level, base.offset(x0, y0, z0), base.offset(x1, y1, z1), state, FLAGS);
    }
}
