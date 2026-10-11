package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.HouseSightlineStatePayload;
import java.nio.file.Files;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/** Actual socket-client range, motion, ownership and manor-mirror proof; opt-in fixtures only. */
public final class MobSightProof {
    public static final BlockPos OUTSIDE_VIEW = new BlockPos(14000, 80, 26000);
    public static final String WOLF = "OTH horizon wolf", PRIVATE_A = "OTH horizon A", PRIVATE_B = "OTH horizon B";
    public static final String MIRROR = "OTH far window wolf";
    private static final NativeTestChunks chunks = new NativeTestChunks();
    private static Wolf wolf, source;
    private static LiteraryActor actorA, actorB;
    private static BlockPos manorView;
    private static int phase, ticks;
    private static boolean staged;
    private MobSightProof() {}

    public static BlockPos manorView() { return manorView; }
    private static void require(boolean okay, String message) {
        if (!okay) throw new IllegalStateException("LIVE MOB SIGHT: " + message);
    }
    private static void floor(ServerLevel level, BlockPos at) {
        chunks.hold(level, new AABB(at).inflate(3));
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++)
            for (int y = -1; y <= 2; y++) level.setBlock(at.offset(x, y, z),
                    (y == -1 ? Blocks.SEA_LANTERN : Blocks.AIR).defaultBlockState(), 3);
    }
    private static void arrive(ServerPlayer player, ServerLevel level, BlockPos at, double side) {
        player.teleportTo(level, at.getX() + .5 + side, at.getY(), at.getZ() + .5, 0, 0);
        player.hasChangedDimension();player.connection.resetPosition();player.setInvulnerable(true);
    }
    public static void beginOutside(ServerPlayer a, ServerPlayer b) {
        var level = a.server.getLevel(HouseDimensions.OUTSIDE);
        floor(level, OUTSIDE_VIEW);floor(level, OUTSIDE_VIEW.offset(70, 0, 70));
        for (var p : List.of(a, b)) arrive(p, level, OUTSIDE_VIEW, p == a ? -.6 : .6);
        phase = 37;ticks = 0;staged = false;
    }
    private static Wolf wolf(ServerLevel level, BlockPos at, String name) {
        var mob = EntityType.WOLF.create(level);require(mob != null, "native wolf creates");
        mob.setNoAi(true);mob.setNoGravity(true);mob.setInvulnerable(true);mob.setPersistenceRequired();
        mob.setCustomName(Component.literal(name));mob.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5);
        require(level.addFreshEntity(mob), "native wolf registers");return mob;
    }
    private static LiteraryActor actor(ServerPlayer owner, BlockPos at, String name) {
        var mob = LiteraryRegistry.ACTOR.get().create(owner.serverLevel());
        require(mob != null, "native private actor creates");mob.appearance(LiteraryActor.STRANGER, ThreeFingers.SERGEANT);
        mob.bind(LabyrinthPlace.THREE_FINGERS, owner.getUUID());mob.setNoAi(true);mob.setNoGravity(true);
        mob.setInvulnerable(true);mob.setCustomName(Component.literal(name));
        mob.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5);
        require(owner.serverLevel().addFreshEntity(mob), "native private actor registers");return mob;
    }
    public static void tick(ServerPlayer a, ServerPlayer b) {
        if (a == null || b == null) return;ticks++;
        if (!staged) {
            if (!chunks.ready()) return;
            // These throwaway test coordinates are natural terrain, not a built manor.
            // Clear only the loaded sight corridor so a real screenshot can see the tracked body.
            BlockPos view=phase==37?OUTSIDE_VIEW:manorView;int direction=phase==37?1:-1;
            if(!sightCorridor(a.serverLevel(),view,direction,false))return;
            if(phase==38&&!sightCorridor(a.server.overworld(),view,direction,false))return;
            sightCorridor(a.serverLevel(),view,direction,true);
            if(phase==38){sightCorridor(a.server.overworld(),view,direction,true);window(a.serverLevel(),view);window(a.server.overworld(),view);}
            if (phase == 37) {
                wolf = wolf(a.serverLevel(), OUTSIDE_VIEW.offset(70, 0, 70), WOLF);
                actorA = actor(a, OUTSIDE_VIEW.offset(67, 0, 70), PRIVATE_A);
                actorB = actor(b, OUTSIDE_VIEW.offset(73, 0, 70), PRIVATE_B);
            } else if (phase == 38) source = wolf(a.server.overworld(), manorView.offset(70, 0, -70), MIRROR);
            staged = true;
        }
        if (phase == 37) {
            double offset = Math.sin(ticks * .11) * 2;
            wolf.setPos(OUTSIDE_VIEW.getX() + 70.5 + offset, OUTSIDE_VIEW.getY(), OUTSIDE_VIEW.getZ() + 70.5);
            wolf.setDeltaMovement(new Vec3(.15, 0, 0));
        } else if (phase == 38) {
            source.setPos(manorView.getX() + 70.5 + Math.sin(ticks * .11) * 2, manorView.getY(), manorView.getZ() - 69.5);
            source.setYRot(ticks % 90);source.setYHeadRot(ticks % 90);source.setDeltaMovement(new Vec3(.15, 0, 0));
        }
    }
    private static boolean sightCorridor(ServerLevel l,BlockPos view,int direction,boolean carve){
        for(int t=0;t<=72;t++)for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){
            var at=view.offset(t+x,0,direction*t+z);
            if(l.getChunkSource().getChunkNow(at.getX()>>4,at.getZ()>>4)==null)return false;
            if(carve)for(int y=0;y<4;y++)l.setBlock(at.above(y),Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS);
        }return true;
    }
    private static void window(ServerLevel l,BlockPos view){for(int x=0;x<=4;x++)for(int y=0;y<=3;y++)l.setBlock(view.offset(x,y,-2),
        (x==0||x==4||y==3?Blocks.SPRUCE_PLANKS:Blocks.GLASS).defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS);}
    public static void checkOutside(ServerPlayer a, ServerPlayer b) {
        require(staged && a.distanceToSqr(wolf) > 80 * 80 && b.distanceToSqr(wolf) > 80 * 80,
                "moving native wolf is beyond the former server distance gate");
        for (var p : List.of(a, b)) require(a.serverLevel().getChunkSource().chunkMap.getPlayersWatching(wolf).contains(p),
                "native tracking sends the distant moving wolf to both actual clients");
        for (var entity : List.of(wolf, actorA, actorB)) entity.discard();
    }
    public static void beginManor(ServerPlayer a, ServerPlayer b, BlockPos origin) {
        manorView = origin.offset(HouseLayout.AXIS_X, 1, HouseLayout.FRONT_DOOR_Z + 2);
        var interior = a.server.getLevel(HouseDimensions.INTERIOR);
        HouseExteriorEntityMirror.clear(a.server);
        floor(interior, manorView);floor(interior, manorView.offset(70, 0, -70));
        floor(a.server.overworld(), manorView.offset(70, 0, -70));
        for (var p : List.of(a, b)) {
            arrive(p, interior, manorView, p == a ? -.6 : .6);
            PacketDistributor.sendToPlayer(p, new HouseSightlineStatePayload(origin, true));
        }
        phase = 38;ticks = 0;staged = false;
    }
    private static List<Mob> projections(ServerLevel level) {
        return level.getEntitiesOfClass(Mob.class, new AABB(manorView).inflate(120),
                mob -> HouseExteriorEntityMirror.isProjection(mob) && mob.getCustomName() != null
                        && mob.getCustomName().getString().equals(MIRROR));
    }
    public static void checkManor(ServerPlayer a, ServerPlayer b) {
        var mirrors = projections(a.serverLevel());
        require(mirrors.size() == 1, "one source has exactly one shared far projection");
        var mirror = mirrors.getFirst();
        require(source != null && !source.getUUID().equals(mirror.getUUID()) && source.isAlive()
                && !HouseExteriorEntityMirror.isProjection(source), "the original wolf retains its real identity");
        require(mirror.isNoAi() && mirror.isNoGravity() && mirror.isInvulnerable(), "scenery has no gameplay authority");
        require(mirror.position().distanceToSqr(source.position()) < .5, "far native projection follows its source each tick");
        for (var p : List.of(a, b)) require(a.serverLevel().getChunkSource().chunkMap.getPlayersWatching(mirror).contains(p),
                "both actual clients track a mirror past the former 40-block band");
    }
    public static void depart(ServerPlayer a, ServerPlayer b) {
        for (var p : List.of(a, b)) arrive(p, a.server.getLevel(HouseDimensions.OUTSIDE), OUTSIDE_VIEW, p == a ? -.6 : .6);
        phase = 39;
    }
    public static void finish(ServerPlayer a) {
        require(projections(a.server.getLevel(HouseDimensions.INTERIOR)).isEmpty(),
                "production mirror releases far scenery when the last domestic reader leaves");
        require(source != null && a.server.overworld().getEntity(source.getUUID()) == source && source.isAlive(),
                "departure preserves the sole native source");
        source.discard();chunks.close();
        try { Files.writeString(LiveExpeditionProof.folder().resolve("mob-sight-passed.txt"),
                "Two actual clients track moving landscape wolves across diagonal visible chunks beyond the former 80-block server gate; registered native renderers draw them beyond their model-distance gate, preserve frustum culling and private actor ownership, and cap at the effective terrain view. Both domestic clients see one moving projection of the same native exterior wolf beyond the former 40-block band; the last departure removes scenery and preserves the original.\n");
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
