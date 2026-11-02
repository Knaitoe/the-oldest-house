package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
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
    static final int POOL=63,ATTENDANT=64,CARRY=65,CHAIR=66;
    private static final NativeTestChunks CHUNKS=new NativeTestChunks();
    private static BlockPos pool,flat;private static boolean preloaded;
    private SceneReviewSocketProof(){}
    static boolean enabled(){return Boolean.getBoolean("the_oldest_house.sceneReviewSocketSmoke");}
    static BlockPos pool(){return pool;}
    static BlockPos flat(){return flat;}
    private static void require(boolean okay,String text){if(!okay)throw new IllegalStateException("LIVE SCENE REVIEW: "+text);}
    private static AABB poolBox(){return new AABB(Vec3.atLowerCornerOf(pool.offset(-16,-8,-47)),Vec3.atLowerCornerOf(pool.offset(17,12,3)));}
    private static AABB flatBox(){return new AABB(Vec3.atLowerCornerOf(flat.offset(-8,-8,-20)),Vec3.atLowerCornerOf(flat.offset(20,10,3)));}
    /** Both scenes' ground, requested on the native workers; true once it and its entity sections are in. */
    static boolean ready(MinecraftServer s,BlockPos origin){
        var l=s.getLevel(HouseDimensions.INTERIOR);if(l==null)return false;
        pool=LabyrinthPlaces.base(origin,LabyrinthPlace.LAST_SWIMMER);flat=LabyrinthPlaces.base(origin,LabyrinthPlace.VACANT_POSSESSION);
        CHUNKS.request(l,poolBox());CHUNKS.request(l,flatBox());return CHUNKS.ready();
    }
    private static void at(ServerPlayer p,BlockPos base,double x,double y,double z,float yaw){
        var l=p.server.getLevel(HouseDimensions.INTERIOR);p.stopRiding();p.setDeltaMovement(Vec3.ZERO);
        p.teleportTo(l,base.getX()+x,base.getY()+y,base.getZ()+z,yaw,0);p.hasChangedDimension();p.connection.resetPosition();
    }
    /** The pool and the flat built as they are built for any reader; the two readers set down in the pool, one at the strap. */
    static void begin(ServerPlayer a,ServerPlayer b){
        var l=a.server.getLevel(HouseDimensions.INTERIOR);var data=LabyrinthData.get(a.server);
        CHUNKS.hold(l,poolBox());CHUNKS.hold(l,flatBox());
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
    static boolean closed(){return LastSwimmer.closed(pool);}
    static boolean held(){return LastSwimmer.held(pool);}
    static boolean carried(){return VacantPossession.inVan(flat,VacantBlock.Kind.DRESSER);}
    static boolean gone(){return VacantPossession.seat(flat)==VacantPossession.GONE;}
    static void status(ServerPlayer a,ServerPlayer b,int phase){
        io.github.knaitoe.theoldesthouse.TheOldestHouse.LOGGER.info("LIVE SCENE REVIEW phase={} A={} B={} hauls={} closed={} held={} loaded={} seat={}",phase,a.position(),b.position(),LastSwimmer.hauls(pool),closed(),held(),VacantPossession.loaded(flat),VacantPossession.seat(flat));
    }
    static void finish(){
        try{Files.writeString(LiveExpeditionProof.folder().resolve("scene-review-passed.txt"),"Two actual clients in the production pool and flat, with their actors and the world's light: the cover hauled with real clicks from the strap and the attendant watched to the fire door from the strap and the west deck; the dresser carried down the real stair to the van; the tenant in the last chair, standing as the reader came close; the chair carried down and the window dark. Staging: both scenes built at their places, the readers set down in them, the first seven pieces loaded on the server.\n");}catch(Exception e){throw new IllegalStateException(e);}
        CHUNKS.close();
    }
}
