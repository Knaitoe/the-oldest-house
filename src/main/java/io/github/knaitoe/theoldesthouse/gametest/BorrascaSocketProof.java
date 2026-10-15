package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

/** Actual socket entry, native reading, music suppression, climbing and crouching. Final-visit setup is explicit staging. */
final class BorrascaSocketProof {
    private static final NativeTestChunks CHUNKS=new NativeTestChunks();
    private static final Map<UUID,Integer> DEPTH=new HashMap<>(),RISES=new HashMap<>();
    private static final Map<UUID,Double> HEIGHT=new HashMap<>();
    private static final Map<UUID,UUID> KYLES=new HashMap<>();
    private static final Set<UUID> CROUCHED=new HashSet<>();
    private static BlockPos source;
    private BorrascaSocketProof(){}
    private static void require(boolean okay,String text){if(!okay)throw new IllegalStateException("LIVE BORRASCA: "+text);}
    static BlockPos source(){return source;}
    static BlockPos base(ServerPlayer p){return BorrascaSites.base(p.server,p.getUUID());}
    private static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.BORRASCA);}
    static void begin(ServerPlayer a,ServerPlayer b,BlockPos origin){
        source=origin.offset(80,0,80);var level=a.server.overworld();var data=LabyrinthData.get(a.server);CHUNKS.hold(level,new AABB(source).inflate(6));
        for(var at:BlockPos.betweenClosed(source.offset(-3,-1,-4),source.offset(3,-1,5)))level.setBlock(at,Blocks.STONE.defaultBlockState(),3);
        NovelRooms.door(level,source,Direction.SOUTH,Blocks.DARK_OAK_DOOR,false);data.putDoor(new LabyrinthData.Door("proof/borrasca/source",level.dimension(),source,Direction.SOUTH,LabyrinthData.toPlace(LabyrinthPlace.BORRASCA),true));
        int i=0;for(var p:List.of(a,b)){p.stopUsingItem();p.stopRiding();p.setGameMode(GameType.SURVIVAL);p.setNoGravity(false);p.setHealth(p.getMaxHealth());p.getFoodData().setFoodLevel(20);DEPTH.put(p.getUUID(),data.returnDepth(p.getUUID()));p.teleportTo(level,source.getX()+.5+(i++==0?-.4:.4),source.getY(),source.getZ()+3.5,180,0);p.hasChangedDimension();p.connection.resetPosition();}
    }
    static boolean entered(ServerPlayer a,ServerPlayer b){for(var p:List.of(a,b)){var site=base(p);if(!BorrascaSites.inside(p)||site==null||p.getZ()>site.getZ()-2)return false;require(own(p).getInt("Visit")==1&&LabyrinthData.get(p.server).returnDepth(p.getUUID())==DEPTH.get(p.getUUID())+1,"actual source door enters visit one and pushes exactly one return");}return true;}
    static boolean read(ServerPlayer a,ServerPlayer b){for(var p:List.of(a,b)){var own=own(p);if(!own.getBoolean("Read_Source")||!own.hasUUID("CastKyle"))return false;KYLES.put(p.getUUID(),own.getUUID("CastKyle"));require(!WitnessAccount.has(LabyrinthData.get(p.server),p.getUUID(),WitnessAccount.Story.BORRASCA),"native source reading grants no ending");}return true;}
    static void observe(ServerPlayer a,ServerPlayer b,int phase){for(var p:List.of(a,b)){if(!BorrascaSites.inside(p))continue;var b0=base(p);if(phase==56){Double old=HEIGHT.put(p.getUUID(),p.getY());if(old!=null&&p.getY()>old+.025)RISES.merge(p.getUUID(),1,Integer::sum);}if(phase==58&&p.isCrouching()&&p.getX()>b0.getX()-22&&p.getX()<b0.getX()-16&&p.getZ()<b0.getZ()-207&&p.getZ()>b0.getZ()-211)CROUCHED.add(p.getUUID());}}
    static boolean climbed(ServerPlayer a,ServerPlayer b){for(var p:List.of(a,b)){var own=own(p);if(!own.getBoolean("TreeReached"))return false;require(RISES.getOrDefault(p.getUUID(),0)>=15&&p.getY()>=base(p).getY()+5.9,"the actual client body climbed the production ladder over successive packets");}return true;}
    private static void at(ServerPlayer p,BlockPos at){p.stopUsingItem();p.setDeltaMovement(Vec3.ZERO);p.teleportTo(p.server.getLevel(HouseDimensions.OUTSIDE),at.getX()+.5,at.getY(),at.getZ()+.5,180,0);p.hasChangedDimension();p.connection.resetPosition();}
    static void records(ServerPlayer a,ServerPlayer b){for(var p:List.of(a,b)){var level=p.server.getLevel(HouseDimensions.OUTSIDE);var base=base(p);CHUNKS.hold(level,new AABB(Vec3.atLowerCornerOf(base.offset(-31,10,-214)),Vec3.atLowerCornerOf(base.offset(17,22,-162))));var own=own(p);own.putInt("Visit",4);own.putInt("Beat",3);own.putBoolean("Consent",true);own.putBoolean("TestimonyRead",true);own.putBoolean("CastRecalled",false);own.putBoolean("Ready",false);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.BORRASCA,own);BorrascaStory.openDoor(level,base.offset(BorrascaRooms.FACTORY_DOOR),true);at(p,base.offset(-24,12,-193));}}
    static boolean took(ServerPlayer a,ServerPlayer b){for(var p:List.of(a,b)){var own=own(p);if(!own.getBoolean("EvidenceRead")||!own.getBoolean("Taken_Evidence")||!BorrascaStory.carriesEvidence(p,own))return false;require(KYLES.get(p.getUUID()).equals(own.getUUID("CastKyle")),"the original saved Kyle survives cold return staging without replacement");}return true;}
    static void pipe(ServerPlayer a,ServerPlayer b){for(var p:List.of(a,b))at(p,base(p).offset(-15,12,-204));}
    static boolean crawled(ServerPlayer a,ServerPlayer b){for(var p:List.of(a,b)){if(!CROUCHED.contains(p.getUUID())||p.getX()>base(p).getX()-24)return false;require(own(p).getBoolean("ReleaseOpened")&&BorrascaStory.carriesEvidence(p,own(p)),"the native fire-release input and actual low body carry the same original out");}return true;}
    static void porch(ServerPlayer a,ServerPlayer b){for(var p:List.of(a,b))at(p,base(p).offset(0,0,-3));}
    static boolean returned(ServerPlayer a,ServerPlayer b){for(var p:List.of(a,b)){if(p.level().dimension()!=p.server.overworld().dimension()||LabyrinthDoors.isBusy(p))return false;var d=LabyrinthData.get(p.server);require(d.returnDepth(p.getUUID())==DEPTH.get(p.getUUID())&&p.position().distanceToSqr(Vec3.atBottomCenterOf(source))<25,"the actual doorway consumes only its original source return");require(own(p).getBoolean("BorrascaDeparted")&&BorrascaStory.carriesEvidence(p,own(p))&&!WitnessAccount.has(d,p.getUUID(),WitnessAccount.Story.BORRASCA),"targeted late-visit staging, native documents and a partial escape grant no Witness");}return true;}
    static void finish(){try{Files.writeString(LiveExpeditionProof.folder().resolve("borrasca-socket-passed.txt"),"Two actual clients entered separate production-built Drisking sites through one real source door, read native source pages, verified vanilla music cancellation and the same positional grinder source, tracked private original cast, walked to and climbed the real ladder, and used native menus to take distinct original records. Explicit final-visit fixture staging then selected the already-read investigation; each client operated the real fire release, crouched under the actual low pipe and returned through its original saved source doorway. Original UUIDs and inventory records survived. Neither partial/staged route received a Witness. This targeted socket proof is not an ordinary four-visit traversal.\n");CHUNKS.close();}catch(Exception e){throw new IllegalStateException(e);}}
}
