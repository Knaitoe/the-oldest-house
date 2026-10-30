package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.Files;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;

/** Explicit day-six fixture; both socket clients use native walking/filming while each original performs the production leap. */
final class GhostsSocketProof {
    private static final NativeTestChunks CHUNKS=new NativeTestChunks();
    private static final Map<UUID,UUID> ORIGINALS=new HashMap<>();
    private static final Map<UUID,Vec3> PREVIOUS=new HashMap<>(),HELD=new HashMap<>(),RETURNS=new HashMap<>();
    private static final Map<UUID,Integer> MOVED=new HashMap<>(),DEPTH=new HashMap<>(),POSE=new HashMap<>();
    private static BlockPos site;
    private GhostsSocketProof(){}
    private static void require(boolean okay,String words){if(!okay)throw new IllegalStateException("LIVE GHOSTS: "+words);}
    static BlockPos base(){return site;}
    static void begin(ServerPlayer a,ServerPlayer b,BlockPos origin){
        var level=a.server.getLevel(HouseDimensions.INTERIOR);var data=LabyrinthData.get(a.server);site=LabyrinthPlaces.base(origin,LabyrinthPlace.GHOSTS_SET);
        CHUNKS.hold(level,new AABB(Vec3.atLowerCornerOf(site.offset(-22,-5,-48)),Vec3.atLowerCornerOf(site.offset(23,13,2))));
        LiteraryRooms.build(level,site,LabyrinthPlace.GHOSTS_SET);LabyrinthBuilder.registerDoors(data,LabyrinthPlace.GHOSTS_SET,site);
        for(var p:List.of(a,b)){
            p.stopRiding();p.setGameMode(GameType.SURVIVAL);p.setNoGravity(false);p.setInvulnerable(false);p.setHealth(p.getMaxHealth());p.getFoodData().setFoodLevel(20);p.setAirSupply(p.getMaxAirSupply());
            var back=Vec3.atBottomCenterOf(LabyrinthPlaces.base(origin,LabyrinthPlace.OFFSET_HALL).offset(0,0,-6));
            DEPTH.put(p.getUUID(),data.returnDepth(p.getUUID()));data.pushReturn(p.getUUID(),new LabyrinthData.Waypoint(level.dimension(),back,0,true));RETURNS.put(p.getUUID(),back.add(0,0,1));
            p.teleportTo(level,site.getX()-4.5,site.getY()+5,site.getZ()-19.5-(p==b?1.2:0),0,0);p.hasChangedDimension();p.connection.resetPosition();
            LiteraryVignettes.onArrive(p,LabyrinthPlace.GHOSTS_SET);var own=LiteraryVignettes.personal(data,p.getUUID(),LabyrinthPlace.GHOSTS_SET);
            // Fixture selects the already-read day; production movement, camera lease and forced return remain untouched.
            own.putBoolean("Read_Source",true);own.putInt("FilmDay",6);own.putInt("ShotClock",590);own.putBoolean("CastPlaced",false);own.putBoolean("DayDone",false);own.putBoolean("Rolling",true);LiteraryVignettes.save(data,p.getUUID(),LabyrinthPlace.GHOSTS_SET,own);
            var camera=p.getInventory().items.stream().filter(s->s.is(LiteraryRegistry.CAMCORDER.get())).findFirst().orElseThrow();int slot=p.getInventory().items.indexOf(camera);if(slot!=0){var kept=p.getInventory().getItem(0);p.getInventory().setItem(0,camera);p.getInventory().setItem(slot,kept);}p.getInventory().selected=0;p.inventoryMenu.broadcastChanges();p.stopUsingItem();
            MOVED.put(p.getUUID(),0);
        }
    }
    static void observe(ServerPlayer a,ServerPlayer b){
        if(!CHUNKS.ready())return;
        for(var p:List.of(a,b)){
            var world=LiteraryVignettes.shared(LabyrinthData.get(p.server),LabyrinthPlace.GHOSTS_SET);String key="FilmMarjorie_"+p.getUUID();if(!world.hasUUID(key))continue;
            require(p.server.getLevel(HouseDimensions.INTERIOR).getEntity(world.getUUID(key)) instanceof LiteraryActor,"the reader's original is loaded");var m=(LiteraryActor)p.server.getLevel(HouseDimensions.INTERIOR).getEntity(world.getUUID(key));
            ORIGINALS.putIfAbsent(p.getUUID(),m.getUUID());require(ORIGINALS.get(p.getUUID()).equals(m.getUUID()),"approach, jump and return never replace the original UUID");
            var previous=PREVIOUS.put(p.getUUID(),m.position());Integer oldPose=POSE.put(p.getUUID(),m.phase());if(previous!=null&&oldPose!=null&&oldPose==GhostsSet.STAND&&m.phase()==GhostsSet.STAND){double distance=m.position().distanceTo(previous);require(distance<.151,"native approach has no scripted five-tick displacement: "+distance);if(distance>.005)MOVED.merge(p.getUUID(),1,Integer::sum);}
            var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.GHOSTS_SET);
            if(own.getBoolean("JumpFrozen0488")){HELD.putIfAbsent(p.getUUID(),m.position());require(m.position().distanceToSqr(HELD.get(p.getUUID()))<.000001&&m.getY()>site.getY()+6&&m.getZ()>site.getZ()-9,"the physical leap clears the native banister and holds the same apex");}
        }
    }
    static boolean finished(ServerPlayer a,ServerPlayer b){
        var data=LabyrinthData.get(a.server);
        for(var p:List.of(a,b)){
            if(LabyrinthDoors.isBusy(p)||p.level().dimension()!=HouseDimensions.INTERIOR||p.position().distanceToSqr(RETURNS.get(p.getUUID()))>.03)return false;
            var own=LiteraryVignettes.personal(data,p.getUUID(),LabyrinthPlace.GHOSTS_SET);
            require(own.getBoolean("DayDone")&&own.getBoolean("FilmDoorExit")&&own.getInt("FilmDay")==6&&data.returnDepth(p.getUUID())==DEPTH.get(p.getUUID()),"only the original saved route is consumed; day seven waits for later arrival");
            require(MOVED.get(p.getUUID())>100&&HELD.containsKey(p.getUUID())&&p.serverLevel().getEntity(ORIGINALS.get(p.getUUID()))!=null,"continuous native approach and held original survive the actual cut");
            require(own.getCompound("FilmLog6").getInt("Frames_FrozenLeap")>=20&&!WitnessAccount.has(data,p.getUUID(),WitnessAccount.Story.GHOSTS_SET),"the real owned camera records the frozen shot without premature ending credit");
        }
        return true;
    }
    static void finish(){try{Files.writeString(LiveExpeditionProof.folder().resolve("ghosts-native-jump-passed.txt"),"Explicit day-six fixture, two actual socket readers: original private Marjories walked continuously from the real beds through the bedroom doors, physically cleared the balcony guards, held their native apex while the leased lenses froze and the heads turned, then returned through their own saved routes. Filming used native item input; actual frozen-shot frames, six-day state, UUIDs and route depths remained personal. This is a targeted native fixture, not an ordinary seven-day manual playthrough.\n");CHUNKS.close();}catch(Exception e){throw new IllegalStateException(e);}}
}
