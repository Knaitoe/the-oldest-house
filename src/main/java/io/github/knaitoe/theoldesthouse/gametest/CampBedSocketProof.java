package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.*;
import java.nio.file.Files;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
/** Throwaway fixture after the original expedition: real native use/movement packets and two private paw views. */
public final class CampBedSocketProof {
    private static UUID hunter;private static BlockPos site;
    private CampBedSocketProof(){}
    private static void require(boolean okay,String text){if(!okay)throw new IllegalStateException("LIVE CAMP BED: "+text);}
    public static BlockPos bed(ServerPlayer p){return site.offset(p.getGameProfile().getName().equals("OTHProofA")?-1:1,0,-35);}
    public static void begin(ServerPlayer a,ServerPlayer b,BlockPos origin){var l=a.server.getLevel(HouseDimensions.OUTSIDE);site=LabyrinthPlaces.base(origin,LabyrinthPlace.CAMP_BLOOD);
        for(int x=-3;x<=3;x++)for(int z=-39;z<=-30;z++){l.setBlock(site.offset(x,-1,z),Blocks.SPRUCE_PLANKS.defaultBlockState(),2);for(int y=0;y<=3;y++)l.setBlock(site.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);}
        // The earlier forest fixture discarded its actor when it moved to the private yacht proof.
        // Stage one new shared native body for this separate fixture and keep that identity throughout.
        var killer=LiteraryRegistry.ACTOR.get().create(l);require(killer!=null,"native shared slasher creates");killer.bind(LabyrinthPlace.CAMP_BLOOD,null);killer.appearance(LiteraryActor.KILLER,0);killer.setNoGravity(true);killer.moveTo(site.getX()+.5,site.getY(),site.getZ()-38.5);require(l.addFreshEntity(killer),"shared slasher registers");hunter=killer.getUUID();
        var shared=LiteraryVignettes.shared(LabyrinthData.get(a.server),LabyrinthPlace.CAMP_BLOOD);shared.putUUID("Killer",hunter);LiteraryVignettes.shared(LabyrinthData.get(a.server),LabyrinthPlace.CAMP_BLOOD,shared);
        for(var p:List.of(a,b)){var at=bed(p);l.setBlock(at,CampDecorBlock.of(CampDecorBlock.Kind.BED_FOOT,Direction.NORTH,p==a?0:1),2);
            l.setBlock(at.north(),CampDecorBlock.of(CampDecorBlock.Kind.BED_HEAD,Direction.NORTH,p==a?0:1),2);
            p.setGameMode(GameType.SURVIVAL);p.teleportTo(l,at.getX()+.5,at.getY(),at.getZ()+2.5,180,0);p.hasChangedDimension();p.connection.resetPosition();
            var d=LabyrinthData.get(a.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.CAMP_BLOOD);own.putBoolean("Here",true);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.CAMP_BLOOD,own);
        }
    }
    public static void hidden(ServerPlayer a,ServerPlayer b){var l=a.serverLevel();require(l.getEntity(hunter) instanceof LiteraryActor,"the original shared slasher remains");var killer=(LiteraryActor)l.getEntity(hunter);
        for(var p:List.of(a,b))require(CampBedCrawl.under(p)&&p.getForcedPose()==Pose.SWIMMING&&p.getBbHeight()<.7&&l.noCollision(p,p.getBoundingBox())&&!CarcassHunt.sees(killer,p),"both native clients actually fit beneath the beds and are concealed");
    }
    public static void left(ServerPlayer a,ServerPlayer b,BlockPos origin){for(var p:List.of(a,b)){require(!CampBedCrawl.under(p)&&p.getForcedPose()==null&&p.getBbHeight()>1&&p.serverLevel().noCollision(p,p.getBoundingBox()),"walking out restores native body and collision");
            CampBedCrawl.clear(p);var l=a.server.getLevel(HouseDimensions.INTERIOR);var at=origin.offset(HouseLayout.AXIS_X,1,HouseLayout.FRONT_DOOR_Z+2);
            p.teleportTo(l,at.getX()+.5+(p==a?-.6:.6),at.getY(),at.getZ()+.5,0,0);p.hasChangedDimension();p.connection.resetPosition();HousePackets.send(p,new PawClutchPayload(p==a?2:1));}
    }
    public static void finish(ServerPlayer a){try{Files.writeString(LiveExpeditionProof.folder().resolve("camp-bed-passed.txt"),"Two actual clients used the raised bunk through native interaction packets, crawled under real mattress/leg collision, remained concealed from the original shared slasher, and walked out into restored full bodies. Both then rendered their separate received authoritative finger counts in the articulated close-focus animation.\n");}catch(Exception e){throw new IllegalStateException(e);}}
}
