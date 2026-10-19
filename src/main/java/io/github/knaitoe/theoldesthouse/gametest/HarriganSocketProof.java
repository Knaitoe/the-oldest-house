package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/** Explicit visit prerequisites; both real clients must render the same saved original after each native reload. */
final class HarriganSocketProof {
    private static final NativeTestChunks CHUNKS=new NativeTestChunks();
    private static BlockPos site;
    private static UUID original;
    private HarriganSocketProof(){}
    static BlockPos base(){return site;}
    private static void require(boolean yes,String text){if(!yes)throw new IllegalStateException("LIVE HARRIGAN: "+text);}
    static void begin(ServerPlayer a,ServerPlayer b,BlockPos origin){
        var level=a.server.getLevel(HouseDimensions.INTERIOR);site=LabyrinthPlaces.base(origin,LabyrinthPlace.HARRIGAN);
        CHUNKS.hold(level,HarriganWake.bounds(site).inflate(2));
        var data=LabyrinthData.get(a.server);var state=data.state(HarriganVignette.ID);
        state.putInt("Visit",1);state.putBoolean("BeatDone",false);data.setState(HarriganVignette.ID,state);
        HarriganVignette.build(a.server,level,site);LabyrinthBuilder.registerDoors(data,LabyrinthPlace.HARRIGAN,site);
        VignetteArchitecture.decorateOnce(level,origin,LabyrinthPlace.HARRIGAN);ScenePolish.polishOnce(level,origin,LabyrinthPlace.HARRIGAN);
        view(a,b,1);
    }
    private static LiteraryActor body(ServerPlayer p){
        var state=LabyrinthData.get(p.server).state(HarriganVignette.ID);
        return state.hasUUID("Harrigan0505")&&p.server.getLevel(HouseDimensions.INTERIOR).getEntity(state.getUUID("Harrigan0505")) instanceof LiteraryActor actor?actor:null;
    }
    static boolean renderedState(ServerPlayer a,ServerPlayer b,int visit){
        if(!CHUNKS.ready())return false;var actor=body(a);if(actor==null)return false;
        if(original==null)original=actor.getUUID();
        require(actor.getUUID().equals(original)&&actor.phase()==visit-1,"the original UUID is in visit "+visit+" on the server");
        require(a.serverLevel()==actor.level()&&b.serverLevel()==actor.level(),"both native readers share the actor's level");
        require(a.serverLevel().getEntitiesOfClass(LiteraryActor.class,HarriganWake.bounds(site),e->e.role()==LiteraryActor.HARRIGAN).size()==1,"one original body, never a double");
        if(visit==3){var c=site.offset(HarriganVignette.CASKET);
            require(actor.position().distanceToSqr(new Vec3(c.getX()+.5,c.getY()+.9,c.getZ()+.5))<.0001,"the original is physically inside the casket");
            require(a.serverLevel().getEntitiesOfClass(LiteraryActor.class,HarriganWake.bounds(site),e->e.role()==LiteraryActor.HARRIGAN_MOURNER).size()==3,"all three original mourners stand at the funeral");
        }
        return true;
    }
    static void advance(ServerPlayer a,ServerPlayer b,int visit){
        var actor=body(a);require(actor!=null,"the preceding visit's body is loaded");
        var older=new CompoundTag();actor.saveWithoutId(older);
        var level=a.serverLevel();
        // End occupancy before the real arrival callback, as the shared visit gate requires.
        for(var p:List.of(a,b)){p.stopRiding();p.teleportTo(level,site.getX()+.5,site.getY(),site.getZ()+5,0,0);p.hasChangedDimension();p.connection.resetPosition();}
        var data=LabyrinthData.get(a.server);var state=data.state(HarriganVignette.ID);
        state.putBoolean("BeatDone",true);data.setState(HarriganVignette.ID,state);
        HarriganVignette.onArrive(a,LabyrinthPlace.HARRIGAN);
        require(data.state(HarriganVignette.ID).getInt("Visit")==visit,"the real arrival advances only the requested visit");
        // Reproduce an entity section loading its older NBT after the newer story/room setup.
        actor.load(older);view(a,b,visit);
    }
    private static void view(ServerPlayer a,ServerPlayer b,int visit){
        for(var p:List.of(a,b)){
            p.setGameMode(GameType.SURVIVAL);p.setInvulnerable(true);p.setNoGravity(false);p.stopUsingItem();p.closeContainer();
            double x=visit==3?(p==a?3.1:-2.1):(p==a?-.15:1.15);
            double z=visit==3?-20:-11.8;
            p.teleportTo(p.server.getLevel(HouseDimensions.INTERIOR),site.getX()+x,site.getY(),site.getZ()+z,0,0);p.hasChangedDimension();p.connection.resetPosition();
        }
    }
    static void finish(){
        try{Files.writeString(LiveExpeditionProof.folder().resolve("harrigan-states-passed.txt"),"Two connected native clients rendered and interacted with the same original Harrigan in all three visits. Actual older entity NBT was restored after the second and third arrivals; normal production ticks reconciled the dead chair skin/pose and casket placement. Both clients saved the actual renderer's living/dead/casket views, with motionless corpses and original UUID. Explicit visit prerequisites, not an uninterrupted manual three-visit playthrough.\n");}catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
        CHUNKS.close();
    }
}
