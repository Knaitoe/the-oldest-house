package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseFurnishings;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.240, the pool and the flat looked at in play (the owner, 2 November: "Review the scenes with actors and normal lighting present.
 * For the pool, watch from the strap while the attendant crosses to the exit. For Vacant Possession, carry furniture down the stairs and
 * watch the final chair decision. Those views will expose obstructed movement, missed gestures and awkward sightlines that the cutaways
 * conceal."). Two real clients on a dedicated server, the production builders, actors and renderers, and the world's own light.
 *
 * <p>Fixture staging only: both scenes are built at their places and the two readers are set down in them rather than walking in through
 * a hall; the flat's first seven pieces go down to the van on the server, so the run starts with the last of them. Everything after
 * that is the scene as written: the clients haul the cover with real clicks, carry the dresser down the real stair with real movement,
 * walk back up to the tenant and take the chair down.
 */
final class SceneReviewSocketProof {
    static final int POOL=63,ATTENDANT=64,CARRY=65,CHAIR=66,FEED_WALK=67,FEED_SHOWN=68;
    /** 0.4.662, the feed: the hall the reader walks while it is watched, and the hall they are set down in, where the monitor hangs. */
    static final LabyrinthPlace WATCHED=LabyrinthPlace.ALCOVE_HALL,NEXT=LabyrinthPlace.OFFSET_HALL;
    private static BlockPos origin,watched,next;
    static BlockPos watchedHall(){return watched;}
    static BlockPos nextHall(){return next;}
    private static final NativeTestChunks CHUNKS=new NativeTestChunks();
    private static BlockPos pool,flat;private static boolean preloaded;
    private SceneReviewSocketProof(){}
    static boolean enabled(){return Boolean.getBoolean("the_oldest_house.sceneReviewSocketSmoke");}
    static BlockPos pool(){return pool;}
    static BlockPos flat(){return flat;}
    private static void require(boolean okay,String text){if(!okay)throw new IllegalStateException("LIVE SCENE REVIEW: "+text);}
    private static AABB poolBox(){return new AABB(Vec3.atLowerCornerOf(pool.offset(-16,-8,-47)),Vec3.atLowerCornerOf(pool.offset(17,12,3)));}
    private static AABB flatBox(){return new AABB(Vec3.atLowerCornerOf(flat.offset(-8,-8,-20)),Vec3.atLowerCornerOf(flat.offset(20,10,3)));}
    private static AABB hallBox(BlockPos origin,LabyrinthPlace place){return AABB.of(LabyrinthPlaces.placeBounds(origin,place)).inflate(2);}
    /** Both scenes' ground, requested on the native workers; true once it and its entity sections are in. */
    static boolean ready(MinecraftServer s,BlockPos origin){
        var l=s.getLevel(HouseDimensions.INTERIOR);if(l==null)return false;
        pool=LabyrinthPlaces.base(origin,LabyrinthPlace.LAST_SWIMMER);flat=LabyrinthPlaces.base(origin,LabyrinthPlace.VACANT_POSSESSION);
        SceneReviewSocketProof.origin=origin;watched=LabyrinthPlaces.base(origin,WATCHED);next=LabyrinthPlaces.base(origin,NEXT);
        CHUNKS.request(l,poolBox());CHUNKS.request(l,flatBox());CHUNKS.request(l,hallBox(origin,WATCHED));CHUNKS.request(l,hallBox(origin,NEXT));return CHUNKS.ready();
    }
    private static void at(ServerPlayer p,BlockPos base,double x,double y,double z,float yaw){
        var l=p.server.getLevel(HouseDimensions.INTERIOR);p.stopRiding();p.setDeltaMovement(Vec3.ZERO);
        p.teleportTo(l,base.getX()+x,base.getY()+y,base.getZ()+z,yaw,0);p.hasChangedDimension();p.connection.resetPosition();
    }
    /** The pool and the flat built as they are built for any reader; the two readers set down in the pool, one at the strap. */
    static void begin(ServerPlayer a,ServerPlayer b){
        var l=a.server.getLevel(HouseDimensions.INTERIOR);var data=LabyrinthData.get(a.server);
        CHUNKS.hold(l,poolBox());CHUNKS.hold(l,flatBox());
        // 0.4.662: two ordinary halls for the feed, built as the House builds them.
        for(var hall:List.of(WATCHED,NEXT)){var base=LabyrinthPlaces.base(origin,hall);CHUNKS.hold(l,hallBox(origin,hall));
            LabyrinthHalls.build(l,base,hall);HouseFurnishings.decorate(l,base,hall);LabyrinthBuilder.registerDoors(data,hall,base);}
        LiteraryRooms.build(l,pool,LabyrinthPlace.LAST_SWIMMER);LabyrinthBuilder.registerDoors(data,LabyrinthPlace.LAST_SWIMMER,pool);
        LiteraryRooms.build(l,flat,LabyrinthPlace.VACANT_POSSESSION);LabyrinthBuilder.registerDoors(data,LabyrinthPlace.VACANT_POSSESSION,flat);
        for(var p:List.of(a,b)){p.setGameMode(GameType.SURVIVAL);p.setNoGravity(false);p.setHealth(p.getMaxHealth());p.getFoodData().setFoodLevel(20);p.getInventory().clearContent();}
        // At the strap on the east deck, facing the crank; and on the west deck, beside the way the attendant will come down.
        at(a,pool,9.5,0,-11.5,-90);at(b,pool,-10.5,0,-26.5,90);
        for(var p:List.of(a,b))LiteraryVignettes.onArrive(p,LabyrinthPlace.LAST_SWIMMER);
    }
    static void attendant(){LastSwimmer.hurry(pool,LastSwimmer.STEPS_AFTER);}
    /** Into the flat: one reader in the living room with the dresser, one in the hall downstairs to watch the stair. */
    static void flat(ServerPlayer a,ServerPlayer b){
        preloaded=false;at(a,flat,-.5,0,-8.5,90);at(b,flat,-1.5,VacantSite.GROUND,-7.5,90);
        for(var p:List.of(a,b))LiteraryVignettes.onArrive(p,LabyrinthPlace.VACANT_POSSESSION);
    }
    /** Fixture: the first seven pieces on the list go down to the van on the server, so the clients carry only the last of them. */
    static void preload(ServerPlayer a){
        if(preloaded||VacantPossession.tenantActor(a.serverLevel(),flat)==null)return;
        var data=LabyrinthData.get(a.server);var own=LiteraryVignettes.personal(data,a.getUUID(),LabyrinthPlace.VACANT_POSSESSION);
        var van=new BlockPos(13,VacantSite.GROUND,-5);
        for(var piece:VacantSite.PIECES){if(piece.kind()==VacantBlock.Kind.DRESSER)continue;
            require(VacantPossession.click(a,flat,piece.at(),own)&&VacantPossession.click(a,flat,van,own),"fixture loading "+piece.name());}
        LiteraryVignettes.save(data,a.getUUID(),LabyrinthPlace.VACANT_POSSESSION,own);preloaded=true;
    }
    static boolean preloaded(){return preloaded;}

    /**
     * 0.4.662, the feed. Fixture: six crossings behind the reader (as a reader six in has), the next hall they come into watched
     * (HallFeed.force, as the case uses), and A set down alone at the alcove hall's near end facing up it. The server's own tick then
     * watches the hall; the client walks up it with real movement, its own game taking the pictures. B stays in the flat.
     */
    static void feedWalk(ServerPlayer a){
        var data=LabyrinthData.get(a.server);
        for(int i=0;i<HallFeed.DEPTH;i++)data.pushReturn(a.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.atBottomCenterOf(watched),180F,true,false));
        require(HallFeed.ready(a,a.serverLevel().getGameTime()),"the reader is six in and rested");
        HallFeed.force=true;at(a,watched,.5,0,-3.5,180);
    }
    static boolean watching(ServerPlayer a){return HallFeed.watching(a.getUUID())!=null;}
    /**
     * The reader set down in the offset hall, alone, where a monitor can hang straight ahead of them: the first floor cell, walking up
     * the hall from its door, from which the feed's own choice of wall is the one they face. The server's own tick shows the feed.
     */
    static void feedShow(ServerPlayer a){
        var l=a.serverLevel();var box=LabyrinthPlaces.placeBounds(origin,NEXT);
        for(int z=next.getZ()-3;z>=box.minZ()+2;z--)for(int x=box.minX()+1;x<=box.maxX()-1;x++){
            var feet=new BlockPos(x,next.getY(),z);
            if(!l.getBlockState(feet).isAir()||!l.getBlockState(feet.above()).isAir()||l.getBlockState(feet.below()).getCollisionShape(l,feet.below()).isEmpty())continue;
            for(float yaw:new float[]{180,0,90,-90}){
                var spot=HallFeed.spot(l,Vec3.atBottomCenterOf(feet),yaw);
                if(spot==null||spot.facing()!=Direction.fromYRot(yaw).getOpposite())continue;
                TheOldestHouse.LOGGER.info("LIVE SCENE REVIEW feed: set down at {} facing {}, the monitor at {} facing {}",feet,yaw,spot.cell(),spot.facing());
                a.stopRiding();a.setDeltaMovement(Vec3.ZERO);a.teleportTo(l,feet.getX()+.5,feet.getY(),feet.getZ()+.5,yaw,0);a.connection.resetPosition();return;
            }
        }
        require(false,"a stand in the offset hall with a wall for the monitor straight ahead");
    }
    static boolean shown(ServerPlayer a){return HallFeed.showing(a.getUUID())==NEXT;}
    static boolean closed(){return LastSwimmer.closed(pool);}
    static boolean held(){return LastSwimmer.held(pool);}
    static boolean carried(){return VacantPossession.inVan(flat,VacantBlock.Kind.DRESSER);}
    static boolean gone(){return VacantPossession.seat(flat)==VacantPossession.GONE;}
    static void status(ServerPlayer a,ServerPlayer b,int phase){
        TheOldestHouse.LOGGER.info("LIVE SCENE REVIEW phase={} A={} B={} hauls={} closed={} held={} loaded={} seat={}",phase,a.position(),b.position(),LastSwimmer.hauls(pool),closed(),held(),VacantPossession.loaded(flat),VacantPossession.seat(flat));
        // 0.4.581: the hall's lamps, as the server has them: how many are off, and the light at the strap (under the last bank) and on the cover.
        // 0.4.581: and the hall's light at eight cells, which the runner sets beside what each client has for them.
        if(phase==FEED_WALK||phase==FEED_SHOWN)TheOldestHouse.LOGGER.info("LIVE SCENE REVIEW feed watching={} showing={} place={}",HallFeed.watching(a.getUUID()),HallFeed.showing(a.getUUID()),LabyrinthPlaces.placeAt(origin,a.blockPosition()));
        if(phase==POOL||phase==ATTENDANT)TheOldestHouse.LOGGER.info("LIVE SCENE REVIEW pool lamps off={} light=[{}] resent={}",LastSwimmer.lightsOut(pool),SwimmerSite.lightReading(a.serverLevel(),pool),io.github.knaitoe.theoldesthouse.house.LightSync.resent());
    }
    static void finish(){
        try{Files.writeString(LiveExpeditionProof.folder().resolve("scene-review-passed.txt"),"Two actual clients in the production pool and flat, with their actors and the world's light: the cover hauled with real clicks from the strap and the attendant watched to the fire door from the strap and the west deck, and the steps under the cover looked at from both with the hall's lamps off; the dresser carried down the real stair to the van; the tenant in the last chair, standing as the reader came close; the chair carried down and the window dark. And the feed (0.4.662): one client walked up an ordinary hall the server watched for it, its own game taking pictures of the hall from under the ceiling, then was in the next hall alone with the monitor on the wall it faced, the hall it left on the screen, footprints coming into it along its way and the picture breaking up. Staging: both scenes built at their places, the readers set down in them, the first seven pieces loaded on the server; for the feed, two ordinary halls built as the House builds them, six crossings pushed behind the reader, the next hall watched for certain, and the reader set down in the second hall rather than walking through a door.\n");}catch(Exception e){throw new IllegalStateException(e);}
        CHUNKS.close();
    }
}
