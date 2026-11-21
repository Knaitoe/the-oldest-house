package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.737, The list, stand-ins 3 (docs/THE_LIST_0_4_737.md): the town out with flashlights. On night three and night four, while the
 * hunt is on, three of the town's people come out from beside the substation, each with a flashlight lit in their hand, and walk
 * streets of their own: one Main Street's far lane, one Avenue K and South Ninth, one South Seventh. Each flashlight's beam is a real
 * light a few blocks ahead of them.
 * <p>
 * If one sees the one who went in (near enough, facing them, nothing in the way) they stop, keep the light on them and shout, and the
 * deputy leaves his round and goes there. The hunt reads the one who went in as standing in light while a searcher has them. Then that
 * searcher walks on and does not shout for them again for fifteen seconds. Nobody else is looked for, and a spectator never.
 * <p>
 * On night three, if the House read the one who went in as DANGER (HOME), all three walk to the street in front of that reader's house
 * first and stand there with their lights on it for half a minute, and that reader alone is told so.
 * <p>
 * They are never saved: made each night three and night four when the hunt begins, put away at first light and when the night ends.
 */
public final class TheListSearch{
    private TheListSearch(){}
    /** Their phases: a painted figure each (tools/paint_character_skins.py, literary_list_searcher_*). */
    public static final int FIRST=281,COUNT=3;
    private static final String[] SKINS={"list_searcher_a","list_searcher_b","list_searcher_c"};
    public static boolean cast(int phase){return phase>=FIRST&&phase<FIRST+COUNT;}
    public static String skin(int phase){return SKINS[phase-FIRST];}
    /** Where they come out: the sidewalk beside the substation, a step apart. */
    static final Vec3[] OUT={new Vec3(34.5,0,-110.5),new Vec3(33.5,0,-111.5),new Vec3(32.5,0,-110.5)};
    /** Each one's streets, walked out and back: Main Street's far lane; Avenue K down to Ninth and along it; Seventh. */
    static final Vec3[][] ROUTES={
        {new Vec3(36.5,0,-119.5),new Vec3(-40.5,0,-119.5)},
        {new Vec3(21.5,0,-108.5),new Vec3(21.5,0,-28.5),new Vec3(-55.5,0,-28.5),new Vec3(21.5,0,-28.5)},
        {new Vec3(21.5,0,-108.5),new Vec3(21.5,0,-71.5),new Vec3(-85.5,0,-71.5),new Vec3(21.5,0,-71.5)}};
    /** In front of the reader's house, in Ninth's near lane: where they stand when the town comes there first. */
    public static final Vec3[] HOUSE={new Vec3(1.5,0,-26.5),new Vec3(5.5,0,-26.5),new Vec3(9.5,0,-26.5)};
    public static final double WALK=.5,SEES=16,FACING=50,BEAM=4;
    /** Ticks a searcher keeps the light on them once they are out of sight; ticks before shouting for them again; ticks at the house. */
    public static final int LOST=40,AGAIN=300,AT_HOUSE=600,STUCK=100,RADIO=400;

    static final class One{@Nullable UUID id;int step;int still;@Nullable Vec3 last;@Nullable BlockPos beam;boolean seeing;int unseen;long shoutedAt=Long.MIN_VALUE/2;}
    static final class Search{final One[] ones={new One(),new One(),new One()};boolean house;long houseUntil=-1;boolean houseTold;@Nullable UUID on;}
    static final Map<Long,Search> SEARCHES=new HashMap<>();
    public static void clearAll(){SEARCHES.clear();}

    /** One tick of night three's or night four's hunt. */
    static void tick(ServerLevel l,TheListNight.Night n,List<ServerPlayer> here){
        var b=n.base;var slasher=TheListNight.slasher(l,n);
        var s=SEARCHES.computeIfAbsent(b.asLong(),k->{var t=new Search();t.house=n.phase==TheListNights.HUNT3&&slasher!=null&&danger(slasher);return t;});
        long now=l.getGameTime();
        s.on=slasher==null?null:slasher.getUUID();
        boolean allThere=true;
        for(int i=0;i<COUNT;i++){
            var o=s.ones[i];var a=actor(l,o);
            if(a==null){a=make(l,b,i);if(a==null){allThere=false;continue;}o.id=a.getUUID();}
            if(!a.getMainHandItem().is(BorrascaRegistry.FLASHLIGHT.get()))a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(BorrascaRegistry.FLASHLIGHT.get()));
            beam(l,a,o);
            boolean sees=slasher!=null&&sees(a,slasher);
            if(sees){
                o.unseen=0;
                if(!o.seeing){
                    o.seeing=true;BorrascaCast.stop(a);a.setNoGravity(true);
                    if(now-o.shoutedAt>=AGAIN){
                        o.shoutedAt=now;
                        a.say(TheListTexts.SEARCHER_NAMES[i],TheListTexts.SEARCH_SHOUT);a.speaks(TheListTexts.SEARCH_SHOUT);
                        TheList.cue(slasher,TheListTexts.SEARCH_SEEN);
                        TheListDeputy.radio(l,b,slasher.position(),RADIO);
                    }
                }
                TheListDeputy.face(a,slasher.getEyePosition());
                allThere=false;continue;
            }
            if(o.seeing){if(++o.unseen<LOST){allThere=false;continue;}o.seeing=false;o.unseen=0;o.last=null;o.still=0;}
            if(s.house&&(s.houseUntil<0||now<s.houseUntil)){
                var goal=Vec3.atLowerCornerOf(b).add(HOUSE[i]);
                if(a.position().distanceToSqr(goal)>.5){go(a,o,goal);allThere=false;}
                else{BorrascaCast.stop(a);a.setNoGravity(true);TheListDeputy.face(a,Vec3.atCenterOf(b.offset(TheListSite.FRONT_DOOR)));}
                continue;
            }
            walk(a,b,i,o);
        }
        if(s.house&&s.houseUntil<0&&allThere){
            s.houseUntil=now+AT_HOUSE;
            if(slasher!=null&&!s.houseTold){s.houseTold=true;TheList.cue(slasher,TheListTexts.SEARCH_HOUSE);}
        }
        if(s.house&&s.houseUntil>=0&&now>=s.houseUntil){s.house=false;for(var o:s.ones){o.step=0;o.last=null;o.still=0;}}
    }
    /** Whether the House read this reader's home as a danger to them (HOME DANGER). */
    static boolean danger(ServerPlayer p){
        return "DANGER".equals(HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.HOME,false));
    }
    static @Nullable LiteraryActor actor(ServerLevel l,One o){return o.id!=null&&l.getEntity(o.id) instanceof LiteraryActor a&&a.isAlive()?a:null;}
    static @Nullable LiteraryActor make(ServerLevel l,BlockPos b,int i){
        var at=Vec3.atLowerCornerOf(b).add(OUT[i]);var cell=BlockPos.containing(at);
        if(!l.isLoaded(cell)||!l.areEntitiesLoaded(ChunkPos.asLong(cell)))return null;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(TheList.PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+i);
        a.moveTo(at.x,at.y,at.z,180,0);a.setYBodyRot(180);a.setYHeadRot(180);
        a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(BorrascaRegistry.FLASHLIGHT.get()));
        return l.addFreshEntity(a)?a:null;
    }
    static void walk(LiteraryActor a,BlockPos b,int i,One o){
        var route=ROUTES[i];
        var goal=Vec3.atLowerCornerOf(b).add(route[o.step]);
        if(a.position().distanceToSqr(goal)>.5){go(a,o,goal);return;}
        o.step=(o.step+1)%route.length;o.still=0;
        go(a,o,Vec3.atLowerCornerOf(b).add(route[o.step]));
    }
    static void go(LiteraryActor a,One o,Vec3 goal){
        if(o.last!=null&&a.position().distanceToSqr(o.last)<.0004){if(++o.still>=STUCK){a.moveTo(goal.x,goal.y,goal.z,a.getYRot(),0);o.still=0;}}else o.still=0;
        o.last=a.position();
        BorrascaCast.walk(a,goal,WALK);
    }
    /** Whether this searcher has the one who went in in sight: near enough, turned toward them, nothing in the way. */
    static boolean sees(LiteraryActor a,ServerPlayer p){
        if(!p.isAlive()||p.isSpectator()||a.distanceTo(p)>SEES)return false;
        var d=p.getEyePosition().subtract(a.getEyePosition());
        double yaw=Math.toDegrees(Math.atan2(-d.x,d.z));
        return Math.abs(Mth.wrapDegrees(yaw-a.getYHeadRot()))<=FACING&&a.hasLineOfSight(p);
    }
    /** The flashlight's beam: a light a few blocks ahead at chest height, moved as they move. */
    static void beam(ServerLevel l,LiteraryActor a,One o){
        var look=Vec3.directionFromRotation(0,a.getYHeadRot());
        var cell=BlockPos.containing(a.getX()+look.x*BEAM,a.getY()+1,a.getZ()+look.z*BEAM);
        if(cell.equals(o.beam))return;
        clear(l,o);
        if(l.isLoaded(cell)&&l.getBlockState(cell).isAir()){l.setBlock(cell,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,11),Block.UPDATE_CLIENTS);o.beam=cell;}
    }
    static void clear(ServerLevel l,One o){
        if(o.beam!=null&&l.isLoaded(o.beam)&&l.getBlockState(o.beam).is(Blocks.LIGHT))l.setBlock(o.beam,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        o.beam=null;
    }
    /** The night over: everyone home, their lights out. */
    static void reset(ServerLevel l,BlockPos b){
        var s=SEARCHES.remove(b.asLong());if(s==null)return;
        for(var o:s.ones){clear(l,o);var a=actor(l,o);if(a!=null)a.discard();}
    }
    /** Whether a searcher has their light on this reader now: the hunt reads it as standing in light. */
    public static boolean spotting(ServerPlayer q){for(var s:SEARCHES.values())if(q.getUUID().equals(s.on))for(var o:s.ones)if(o.seeing&&o.unseen==0)return true;return false;}
    /** For checks. */
    public static @Nullable LiteraryActor searcher(ServerLevel l,BlockPos b,int i){var s=SEARCHES.get(b.asLong());return s==null?null:actor(l,s.ones[i]);}
    public static boolean seeing(BlockPos b,int i){var s=SEARCHES.get(b.asLong());return s!=null&&s.ones[i].seeing;}
    public static @Nullable BlockPos beamAt(BlockPos b,int i){var s=SEARCHES.get(b.asLong());return s==null?null:s.ones[i].beam;}
    public static boolean atHouse(BlockPos b){var s=SEARCHES.get(b.asLong());return s!=null&&s.house&&s.houseUntil>=0;}
    public static boolean goingToHouse(BlockPos b){var s=SEARCHES.get(b.asLong());return s!=null&&s.house;}
    /** For a check: end the wait at the house now. */
    public static void leaveHouse(BlockPos b){var s=SEARCHES.get(b.asLong());if(s!=null&&s.house)s.houseUntil=0;}
}
