package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.622, A Little Longer, the second slice (docs/A_LITTLE_LONGER_0_4_622.md): Rowan, in the shop. The plan's line: "Painted (two
 * adult casts), standing, walking real paths, humming at the bench, the conversation panel."
 *
 * <ul>
 * <li><b>The cast</b> is fixed at the first meeting from the House's settled read of who the reader is drawn to (HouseRead.drawnTo):
 * men, the man; women, the woman; both, nobody or not yet read, one of the two by the reader's own id. It is kept in the reader's
 * record and never changed. Rowan is the same person in either body: the same work, voice and habits.</li>
 * <li><b>At the bench</b>: sat in the mended chair, hands in the back of the radio, humming the same five notes now and then; the
 * radio is tuned through its static between times. Both carry through the back door into the corridor, so the reader hears them
 * before seeing anybody.</li>
 * <li><b>Errands</b>: now and then Rowan gets up and walks, on their own feet, to the shelf of waiting sets and looks along the tags,
 * then to the counter and looks out at the street through the door's glass, then back to the chair. Nobody is sent on an errand while
 * a reader has them in the panel.</li>
 * <li><b>A hand on Rowan</b> opens the panel. In this slice they say only where they have got to and go back to it; the evening's
 * conversation starts with the next slice (the workbench). Rowan turns their head to a reader near them who they can see.</li>
 * </ul>
 *
 * <p>Each reader's copy of the place has its own Rowan, made once the copy's entities are loaded, taken up again after a reload and
 * never made twice.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LongerRowan {
    private LongerRowan(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LITTLE_LONGER;
    /** Stranger phases: Rowan as a woman, Rowan as a man. */
    /** 0.4.628: the same two in their coats, on the next two phases (LongerTomorrow). */
    public static final int FIRST=210,WOMAN=0,MAN=1,COAT=2,COUNT=4;
    public static boolean cast(int phase){return phase>=FIRST&&phase<FIRST+COUNT;}
    public static String skin(int phase){int k=phase-FIRST;return ((k%2)==MAN?"longer_rowan_man":"longer_rowan_woman")+(k>=COAT?"_coat":"");}
    public static boolean is(@Nullable Entity e){return e instanceof LiteraryActor a&&a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    /** Their gestures, for the renderer (client.LiteraryRenderers.CastModel.longer): walking (nothing), at the radio, at the shelf, at the counter. */
    public static final int WALK=0,WORK=1,SHELF=2,COUNTER=3;
    /** The reader's record: which Rowan they met (FIRST+WOMAN or FIRST+MAN), once they have. */
    public static final String CAST="RowanCast0622",MET="RowanMet0622";
    public static final String TALK="longer_rowan";

    /** Which Rowan this reader meets: fixed the first time and kept. */
    public static int castFor(ServerPlayer p,CompoundTag own){
        if(own.contains(CAST))return own.getInt(CAST);
        int cast=switch(HouseRead.drawnTo(p)){case "MEN"->FIRST+MAN;case "WOMEN"->FIRST+WOMAN;default->FIRST+(Math.floorMod(p.getUUID().hashCode(),2)==0?WOMAN:MAN);};
        own.putInt(CAST,cast);return cast;
    }

    // ------------------------------------------------------------------------------------------------ where Rowan is
    /** Where they stand or sit, from the place's base, which way they face (degrees; 0 is south), and what they do there. */
    public record Spot(double x,double y,double z,float look,int gesture){public Vec3 at(BlockPos b){return new Vec3(b.getX()+x,b.getY()+y,b.getZ()+z);}}
    /** In the mended chair, facing the bench (west). */
    public static final Spot BENCH=new Spot(LongerSite.CHAIR.getX()+.5,0,LongerSite.CHAIR.getZ()+.5,90,WORK);
    /** In the chair with the tool put down (LongerBench, once the workbench chapter is over). */
    public static final Spot REST_AT_BENCH=new Spot(BENCH.x(),0,BENCH.z(),90,LongerBench.REST);
    /** In front of the shelf of waiting sets, facing it (east). */
    public static final Spot SHELF_SPOT=new Spot(5.68,0,-12.4,-90,SHELF);
    /** Behind the counter, facing the shop door (north). */
    public static final Spot COUNTER_SPOT=new Spot(4.5,0,-14.68,180,COUNTER);
    /** The way from the chair to the shelf: out of the chair to the aisle, then along it. The counter is straight on from the shelf. */
    public static final List<Vec3> TO_SHELF=List.of(new Vec3(-4.5,0,-12.4),new Vec3(-3.5,0,-12.4));
    public static final List<Vec3> TO_COUNTER=List.of(new Vec3(4.5,0,-13.4));
    public static final List<Vec3> TO_BENCH=List.of(new Vec3(4.5,0,-12.4),new Vec3(-3.5,0,-12.4),new Vec3(-4.5,0,-12.4));
    /** The errand: each leg's waypoints, then where it ends. */
    record Leg(List<Vec3> via,Spot to,int hold){}
    static final Leg[] ERRAND={new Leg(TO_SHELF,SHELF_SPOT,110),new Leg(TO_COUNTER,COUNTER_SPOT,90),new Leg(TO_BENCH,BENCH,0)};
    /** How far in a tick they walk; how far a body turns in a tick; how far a head turns from the body; how near a reader turns it. */
    public static final double PACE=.085,NEAR=8;
    public static final float TURN=18,HEAD_TURN=70;
    /** Between errands, in ticks at the bench (the least, and how much more it may be); between hums; between tunings. */
    public static final int SIT_LEAST=900,SIT_MORE=900,HUM_EVERY=160,TUNE_EVERY=560;
    /** After a hand on them, how long they stay put before an errand. */
    public static final int SPOKEN_QUIET=400;

    /** One copy's Rowan: who they are by body, where they are in their errand, and their clocks. */
    static final class Shop {UUID id;int phase=-1;int leg=-1,via=0,hold=0;long nextErrand=-1,nextHum=-1,nextTune=-1,lastTick=-1,spokenAt=-100000;}
    private static final Map<BlockPos,Shop> SHOPS=new HashMap<>();
    static Shop shop(BlockPos b){return SHOPS.computeIfAbsent(b.immutable(),k->new Shop());}
    public static void clearAll(){SHOPS.clear();LongerBench.clearAll();LongerKettle.clearAll();LongerRadio.clearAll();LongerRoof.clearAll();LongerReturn.clearAll();LongerTomorrow.clearAll();LongerCut.clearAll();}

    public static AABB area(BlockPos b){return new AABB(b.getX()-7,b.getY()-1,b.getZ()-19,b.getX()+8,b.getY()+LongerSite.ROOF+4,b.getZ()+1);}
    private static boolean live(ServerLevel l,Vec3 at){var cell=BlockPos.containing(at);return l.isLoaded(cell)&&l.areEntitiesLoaded(ChunkPos.asLong(cell));}

    /** A reader arrives in their copy: the copy is kept ticking, and their Rowan is decided now if not already. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;var s=shop(b);s.phase=LongerCut.gone(own)?-1:castFor(p,own);ensure(p.serverLevel(),b);LongerBench.arrive(p,b,own);LongerKettle.arrive(p,b,own);LongerRadio.arrive(p,b,own);LongerRoof.arrive(p,b,own);LongerReturn.arrive(p,b,own);LongerTomorrow.arrive(p,b,own);LongerCut.arrive(p,b,own);if(LongerCut.gone(own))s.phase=-1;
    }
    /** Rowan at this copy, if their body is here. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b){
        var s=shop(b);
        if(s.id!=null&&l.getEntity(s.id) instanceof LiteraryActor a&&a.isAlive()&&is(a))return a;
        for(var old:l.getEntitiesOfClass(LiteraryActor.class,area(b),LongerRowan::is))if(old.isAlive()){s.id=old.getUUID();return old;}
        return null;
    }
    /** Rowan made in the chair if they are not here; nothing is made while the shop has its entities unloaded. */
    public static void ensure(ServerLevel l,BlockPos b){
        var s=shop(b);if(s.phase<0||find(l,b)!=null||!RoomActors.ready(l,area(b)))return;
        var at=BENCH.at(b);if(!live(l,at))return;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,s.phase);a.gesture(WORK);
        a.moveTo(at.x,at.y,at.z,BENCH.look(),0);a.setYHeadRot(BENCH.look());a.setYBodyRot(BENCH.look());
        if(!l.addFreshEntity(a))return;
        s.id=a.getUUID();s.leg=-1;
    }
    /** Nobody at this copy: for a shop built again from nothing, and for tests. */
    public static void reset(ServerLevel l,BlockPos b){for(var old:l.getEntitiesOfClass(LiteraryActor.class,area(b),LongerRowan::is))old.discard();SHOPS.remove(b);LongerBench.forget(b);LongerKettle.forget(b);LongerRadio.forget(b);LongerRoof.forget(b);LongerReturn.forget(b);LongerTomorrow.forget(b);LongerCut.forget(b);}

    /** Readers in this copy of the place. */
    public static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(!p.isSpectator()&&p.isAlive()&&LongerSite.inside(p.position().subtract(b.getX(),b.getY(),b.getZ())))out.add(p);
        return out;
    }
    @SubscribeEvent public static void serverTick(ServerTickEvent.Post e){
        if(SHOPS.isEmpty())return;var l=e.getServer().getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var b:List.copyOf(SHOPS.keySet())){var here=readers(l,b);if(!here.isEmpty())tick(l,b,here);}
    }
    /** Once a tick for the copy, however many readers are in it. */
    public static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var s=shop(b);long now=l.getGameTime();if(s.lastTick==now)return;s.lastTick=now;
        ensure(l,b);var a=find(l,b);
        // 0.4.625: the kitchen radio's tune while it is on (LongerRadio), whoever switched it.
        LongerRadio.tune(l,b,a);
        if(a==null)return;
        if(s.nextErrand<0){s.nextErrand=now+SIT_LEAST+l.random.nextInt(SIT_MORE);s.nextHum=now+40;s.nextTune=now+100;}
        if(s.leg<0){
            // At the bench, while they work at it: humming now and then, and, while its back is off, the radio tuned through its static
            // between. Once the work is put down (LongerBench) neither: the set is off and nobody is concentrating.
            boolean working=a.gesture()==WORK||a.gesture()==LongerBench.HURRY;
            if(working&&now>=s.nextHum){l.playSound(null,a.getX(),a.getY()+1,a.getZ(),LongerRegistry.HUM.get(),SoundSource.NEUTRAL,.55F,1);s.nextHum=now+HUM_EVERY+l.random.nextInt(HUM_EVERY);}
            var radio=l.getBlockState(b.offset(LongerSite.RADIO));
            if(now>=s.nextTune&&LongerBlock.is(radio,LongerBlock.Kind.RADIO)&&radio.getValue(LongerBlock.STAGE)==2){var r=LongerSite.RADIO;l.playSound(null,b.getX()+r.getX()+.5,b.getY()+r.getY()+.5,b.getZ()+r.getZ()+.5,LongerRegistry.TUNING.get(),SoundSource.BLOCKS,.5F,1);s.nextTune=now+TUNE_EVERY+l.random.nextInt(TUNE_EVERY);}
        }
        // The workbench chapter has Rowan while it runs: no errands in the middle of it (LongerBench).
        if(LongerBench.tick(l,b,a,here,s.leg<0))return;
        // 0.4.624: then the kettle chapter, which keeps Rowan from the offer on, and upstairs once the tea is made (LongerKettle).
        if(LongerKettle.tick(l,b,a,here,s.leg<0))return;
        if(s.leg<0){
            settle(a,LongerBench.rested(l,b)?REST_AT_BENCH:BENCH);
            if(now>=s.nextErrand&&now-s.spokenAt>SPOKEN_QUIET&&!inPanel(here)){s.leg=0;s.via=0;s.hold=0;a.gesture(WALK);}
            else{turnHead(a,BENCH.look(),here);return;}
        }
        var leg=ERRAND[s.leg];
        if(s.hold>0){
            // Stopped where the leg ends, looking at what they came to look at; a reader near turns their head.
            settle(a,leg.to());turnHead(a,leg.to().look(),here);
            // Spoken to while they stand there: they stay a while longer before going on.
            if(now-s.spokenAt<SPOKEN_QUIET/2)return;
            if(--s.hold<=0)next(l,s,a,now);
            return;
        }
        // Walking: each waypoint in turn, then the spot itself.
        var target=s.via<leg.via().size()?leg.via().get(s.via):new Vec3(leg.to().x(),leg.to().y(),leg.to().z());
        if(step(a,target.add(b.getX(),b.getY(),b.getZ()))){
            if(s.via<leg.via().size())s.via++;
            else{settle(a,leg.to());if(leg.hold()>0)s.hold=leg.hold();else next(l,s,a,now);}
        }
    }
    private static void next(ServerLevel l,Shop s,LiteraryActor a,long now){
        s.leg++;s.via=0;s.hold=0;
        if(s.leg>=ERRAND.length){s.leg=-1;s.nextErrand=now+SIT_LEAST+l.random.nextInt(SIT_MORE);settle(a,BENCH);}
        else a.gesture(WALK);
    }
    private static boolean inPanel(List<ServerPlayer> here){for(var p:here)if(Talk.waiting(p,TALK))return true;return false;}
    /** Where they are now: in the chair, at the shelf, at the counter, or on the way. For cases. */
    public static int leg(BlockPos b){return shop(b).leg;}
    /** For cases: how long they will stay where the errand has stopped them. */
    public static int holding(BlockPos b){return shop(b).hold;}
    /** For cases: the errand starts now. */
    public static void sendOnErrand(BlockPos b){var s=shop(b);s.nextErrand=0;s.spokenAt=-100000;}

    /** Arrived: turned the way the spot faces, doing what is done there. */
    private static void settle(LiteraryActor a,Spot to){
        if(a.gesture()!=to.gesture())a.gesture(to.gesture());
        var at=new Vec3(a.getX(),a.getY(),a.getZ());
        if(to.x()==BENCH.x()&&to.z()==BENCH.z()&&at.distanceToSqr(new Vec3(Math.floor(at.x)+.5,at.y,Math.floor(at.z)+.5))>.0001)a.setPos(Math.floor(at.x)+.5,at.y,Math.floor(at.z)+.5);
        float body=Mth.approachDegrees(a.getYRot(),to.look(),TURN);a.setYRot(body);a.setYBodyRot(body);
    }
    /**
     * A step toward this point, on the floor, the body turning into the way they walk rather than snapping round. The ways are laid
     * clear of everything in the shop (LongerTests checks every cell of them), so a step is never into anything.
     */
    static boolean step(LiteraryActor a,Vec3 target){
        var at=a.position();var d=new Vec3(target.x-at.x,0,target.z-at.z);double len=d.length();
        if(len<.04){a.setPos(target.x,target.y,target.z);return true;}
        float want=(float)(Mth.atan2(d.z,d.x)*Mth.RAD_TO_DEG)-90;
        float body=Mth.approachDegrees(a.getYRot(),want,TURN);a.setYRot(body);a.setYBodyRot(body);a.setYHeadRot(Mth.approachDegrees(a.getYHeadRot(),body,TURN));
        // Turning on the spot first when the way bends sharply, as a body does; then walking.
        if(Math.abs(Mth.wrapDegrees(want-body))>45){a.walkAnimation.update(0,.4F);return false;}
        double stepLen=Math.min(PACE,len);
        // 0.4.624: the height eased along the way with the distance walked, so the back stair is climbed tread by tread, not stepped onto.
        a.setPos(at.x+d.x/len*stepLen,at.y+(target.y-at.y)*stepLen/len,at.z+d.z/len*stepLen);
        a.walkAnimation.update((float)stepLen*4,.4F);a.setDeltaMovement(Vec3.ZERO);
        return false;
    }
    /** Kept in the chair, doing this, the head toward a reader near: for the workbench chapter (LongerBench). */
    static void hold(LiteraryActor a,int gesture,List<ServerPlayer> here){hold(a,gesture,here,BENCH.look());}
    /** The same, turned in the chair to this yaw. */
    static void hold(LiteraryActor a,int gesture,List<ServerPlayer> here,float look){settle(a,new Spot(BENCH.x(),0,BENCH.z(),look,gesture));turnHead(a,look,here);}
    /** The head toward a reader near, the body as it is: for the kettle chapter (LongerKettle). */
    static void lookAt(LiteraryActor a,List<ServerPlayer> here){turnHead(a,a.getYRot(),here);}
    /** The body kept to this yaw; the head toward a reader near who can be seen, no more than HEAD_TURN off it, a little at a time. */
    private static void turnHead(LiteraryActor a,float rest,List<ServerPlayer> here){
        ServerPlayer near=null;double best=NEAR*NEAR;
        for(var p:here){double d=a.distanceToSqr(p);if(d<best&&a.hasLineOfSight(p)){best=d;near=p;}}
        float body=a.getYRot();float head=body;
        if(near!=null){var v=near.getEyePosition().subtract(a.position());head=(float)(Mth.atan2(v.z,v.x)*Mth.RAD_TO_DEG)-90;}
        float want=body+Mth.clamp(Mth.wrapDegrees(head-body),-HEAD_TURN,HEAD_TURN);
        a.setYHeadRot(Mth.approachDegrees(a.getYHeadRot(),want,10));
    }

    // ------------------------------------------------------------------------------------------------ a hand on Rowan
    /** A hand on Rowan: the panel, with where they have got to. Nothing more is asked of anybody in this slice. */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!is(a))return;var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        if(a.saying()&&Talk.key(p).equals(TALK))return;
        var s=shop(b);s.spokenAt=p.serverLevel().getGameTime();
        // 0.4.623: at the bench the evening's first chapter answers (LongerBench); it says what it says in the panel itself.
        if(LongerBench.talk(p,a,own,s.leg<0))return;
        if(LongerKettle.talk(p,a,own,s.leg<0))return;
        boolean first=!own.getBoolean(MET);
        if(first){own.putBoolean(MET,true);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
        Talk.begin(p,TALK,LongerTexts.ROWAN);
        int where=s.leg<0?-1:s.hold>0?s.leg:-2;
        String line=switch(where){
            case -1->LongerTexts.ROWAN_AFTER;case 0->LongerTexts.ROWAN_SHELF;case 1->LongerTexts.ROWAN_COUNTER;default->LongerTexts.ROWAN_WALKING;};
        Talk.line(p,LongerTexts.ROWAN,line);a.say(LongerTexts.ROWAN,line);a.speaks(line);
        Talk.end(p);
    }
}
