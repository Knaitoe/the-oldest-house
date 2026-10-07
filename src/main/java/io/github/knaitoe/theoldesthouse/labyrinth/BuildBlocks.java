package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

/** Ordered construction commands, executed only on the server thread in bounded slices. */
public final class BuildBlocks {
    public static final int MAX_VISITS = 4096;
    private static final long BUDGET_NANOS = 6_000_000L;
    private static Plan recording;
    private BuildBlocks() {}

    private static BlockState nativeShape(ServerLevel level,BlockPos at,BlockState state){
        if(state.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock&&!state.is(net.minecraft.world.level.block.Blocks.IRON_BARS))
            for(var side:net.minecraft.core.Direction.Plane.HORIZONTAL){var next=at.relative(side);state=state.updateShape(side,level.getBlockState(next),level,at,next);}
        return state;
    }

    private interface Command {
        boolean step(ServerLevel level);
        default BlockState stateAt(BlockPos pos) { return null; }
    }

    private static final class Fill implements Command {
        private final BlockPos low, high;
        private final BlockState state;
        private final int flags, width, depth;
        private final long size;
        private long cursor;
        Fill(BlockPos a, BlockPos b, BlockState state, int flags) {
            low = new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
            high = new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
            this.state = state; this.flags = flags;
            width = high.getX() - low.getX() + 1; depth = high.getZ() - low.getZ() + 1;
            size = (long) width * depth * (high.getY() - low.getY() + 1);
        }
        public boolean step(ServerLevel level) {
            long column = cursor / width;
            BlockPos pos = low.offset((int)(cursor % width), (int)(column / depth), (int)(column % depth));
            var shaped=nativeShape(level,pos,state);
            if (!level.getBlockState(pos).equals(shaped)) level.setBlock(pos, shaped, flags);
            return ++cursor == size;
        }
        public BlockState stateAt(BlockPos pos) {
            return pos.getX() >= low.getX() && pos.getX() <= high.getX()
                    && pos.getY() >= low.getY() && pos.getY() <= high.getY()
                    && pos.getZ() >= low.getZ() && pos.getZ() <= high.getZ() ? state : null;
        }
    }

    public static final class Plan {
        private final ServerLevel level;
        private final List<Command> commands = new ArrayList<>();
        /**
         * Recording happens synchronously, so later authored reads must not walk the
         * entire construction plan. Keep only block-affecting commands indexed by
         * chunk; command order inside each bucket is still the original authoring order.
         */
        private final Map<Long, List<Command>> stateIndex = new HashMap<>();
        private int cursor;
        private int lastVisits;
        private Plan(ServerLevel level) { this.level = level; }

        private void add(Command command, BlockPos a, BlockPos b) {
            commands.add(command);
            int minChunkX = Math.min(a.getX(), b.getX()) >> 4;
            int maxChunkX = Math.max(a.getX(), b.getX()) >> 4;
            int minChunkZ = Math.min(a.getZ(), b.getZ()) >> 4;
            int maxChunkZ = Math.max(a.getZ(), b.getZ()) >> 4;
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    stateIndex.computeIfAbsent(ChunkPos.asLong(chunkX, chunkZ), ignored -> new ArrayList<>()).add(command);
                }
            }
        }

        private void add(Command command) {
            commands.add(command);
        }

        private BlockState authoredStateAt(BlockPos pos) {
            List<Command> local = stateIndex.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
            if (local == null) return null;
            for (int i = local.size() - 1; i >= 0; i--) {
                BlockState state = local.get(i).stateAt(pos);
                if (state != null) return state;
            }
            return null;
        }

        /** A hard position limit also bounds slices on faster machines. */
        public boolean tick() {
            lastVisits = 0;
            long deadline = System.nanoTime() + BUDGET_NANOS;
            while (cursor < commands.size() && lastVisits < MAX_VISITS) {
                if (commands.get(cursor).step(level)) cursor++;
                lastVisits++;
                if ((lastVisits & 31) == 0 && System.nanoTime() >= deadline) break;
            }
            return cursor == commands.size();
        }
        public int lastVisits() { return lastVisits; }
    }

    public static Plan record(ServerLevel level, Runnable author) {
        if (recording != null) throw new IllegalStateException("Nested construction plan");
        Plan plan = new Plan(level);
        recording = plan;
        try { author.run(); } finally { recording = null; }
        return plan;
    }

    public static void box(ServerLevel level, BlockPos a, BlockPos b, BlockState state, int flags) {
        // Panes need native neighbour shapes; known-shape placement leaves separate glass posts.
        if(state.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock&&!state.is(net.minecraft.world.level.block.Blocks.IRON_BARS))
            flags &= ~net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE;
        if (recording != null && recording.level == level) {
            Fill fill = new Fill(a, b, state, flags);
            recording.add(fill, fill.low, fill.high);
        } else {
            for (BlockPos pos : BlockPos.betweenClosed(a, b)) level.setBlock(pos, nativeShape(level,pos,state), flags);
        }
    }

    public static void set(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        box(level, pos, pos, state, flags);
    }

    /** Recheck real bodies at execution, since a companion can enter a recorded edit between slices. */
    public static boolean guardedSet(ServerLevel level,BlockPos pos,BlockState state,int flags,java.util.function.BooleanSupplier guard){
        if(recording!=null&&recording.level==level){
            var before=state(level,pos);
            recording.add(new Command(){
                public boolean step(ServerLevel ignored){var actual=level.getBlockState(pos);if(!actual.equals(before)&&!actual.equals(state))return true;if(!guard.getAsBoolean())return false;if(!actual.equals(state))level.setBlock(pos,state,flags);return true;}
                public BlockState stateAt(BlockPos at){return pos.equals(at)?state:null;}
            },pos,pos);return true;
        }
        if(!guard.getAsBoolean())return false;if(!level.getBlockState(pos).equals(state))level.setBlock(pos,state,flags);return true;
    }

    /** Read earlier authored commands when a later prop depends on them. */
    public static BlockState state(ServerLevel level, BlockPos pos) {
        if (recording != null && recording.level == level) {
            BlockState state = recording.authoredStateAt(pos);
            if (state != null) return state;
        }
        return level.getBlockState(pos);
    }

    /** Native books, sign text and actors are installed once, after their supporting blocks. */
    public static void after(ServerLevel level, Runnable action) {
        if (recording != null && recording.level == level) {
            recording.add(new Command() {
                public boolean step(ServerLevel ignored) { action.run(); return true; }
            });
        } else action.run();
    }
}
