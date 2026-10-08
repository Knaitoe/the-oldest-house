package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.PlaytestLog;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

/** Personal completion earns one calm fresh deal, never a reroll of a known route. */
public final class ExpeditionRhythm {
    public static final String STATE="expedition_rhythm_0457";
    private ExpeditionRhythm(){}
    public static void request(LabyrinthData data,UUID player){
        var own=data.stateEntry(STATE,player.toString());own.putBoolean("Breather",true);data.setStateEntry(STATE,player.toString(),own);
    }
    public static boolean pending(LabyrinthData data,UUID player){return data.stateEntry(STATE,player.toString()).getBoolean("Breather");}
    public static void consumed(LabyrinthData data,UUID player){
        var own=data.stateEntry(STATE,player.toString());own.remove("Breather");data.setStateEntry(STATE,player.toString(),own);
    }
    /** Called only after the far door's native movement succeeds, never for a retreat. */
    public static void crossedHazard(ServerPlayer player,LabyrinthData.Door from){
        if(!player.isAlive()||player.isSpectator()||!from.dimension.equals(HouseDimensions.INTERIOR))return;
        for(var place:LabyrinthPlace.values())if(LabyrinthPacing.physicalTrial(place))for(var spec:place.doors())
            if(LabyrinthData.DEALT.equals(spec.destination())&&place.doorId(spec).equals(from.id)){
                request(LabyrinthData.get(player.server),player.getUUID());
                PlaytestLog.event(player,"hazard_resolve","place",place.id());return;
            }
    }
    /** All choices are navigable and calm; unavailable rooms never enter the map. */
    public static boolean deal(LabyrinthData data,UUID player,List<LabyrinthData.Door> doors,LabyrinthPlace here,RandomSource random){
        var calm=LabyrinthDealer.grayAvailable(data,player).stream()
                .filter(p->p!=here&&(LabyrinthPacing.ordinary(p)||LabyrinthPacing.quiet(p)))
                .filter(LabyrinthDealer::onward).filter(p->LabyrinthDealer.grayWeight(p,data.returnDepth(player))>0).toList();
        if(calm.isEmpty())return false;
        var quiet=calm.stream().filter(LabyrinthPacing::quiet).toList();
        var ordinary=calm.stream().filter(LabyrinthPacing::ordinary).toList();
        var shortHalls=ordinary.stream().filter(p->p.room().maxZ()-p.room().minZ()<=30).toList();
        if(!shortHalls.isEmpty())ordinary=shortHalls;
        for(int i=0;i<doors.size();i++){
            var pool=i==0&&!quiet.isEmpty()?quiet:ordinary.isEmpty()?calm:ordinary;
            var next=pool.get(random.nextInt(pool.size()));data.deal(player,doors.get(i),next.id(),false,false);
        }
        return true;
    }
    /** The first truthful retreat explanation is saved, and cannot appear during a commitment. */
    public static boolean offerRetreat(ServerPlayer p,LabyrinthPlace place,LabyrinthData.Door entry){
        if(!p.isAlive()||p.isSpectator()||WitnessAccount.Story.of(place.id())==null||place==LabyrinthPlace.GOATMAN
                ||entry==null||!entry.dimension.equals(p.level().dimension())||p.distanceToSqr(entry.lower.getCenter())>100
                ||HideAndClap.isLocked(p)||NovelVignettes.exitLocked(p,entry)||VignetteGate.exitLocked(p,entry)
                ||LiteraryVignettes.retreatLocked(p))return false;
        var data=LabyrinthData.get(p.server);var own=data.stateEntry(STATE,p.getUUID().toString());
        if(own.getBoolean("RetreatExplained"))return false;
        own.putBoolean("RetreatExplained",true);data.setStateEntry(STATE,p.getUUID().toString(),own);
        p.displayClientMessage(Component.literal("You can still leave through the door behind you."),true);
        PlaytestLog.event(p,"retreat_guidance","place",place.id());return true;
    }
    public static void refuse(ServerPlayer p,BlockPos at,String gate,String words){
        p.displayClientMessage(Component.literal(words),true);PlaytestLog.refused(p,gate,at);
    }
}
