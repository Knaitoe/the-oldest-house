package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.524, the Pell house (docs/PELL_HOUSE_PEOPLE_0_4_524.md): its people, from the proposal's "The people", standing where the
 * first visit puts them (the proposal's "1. The placement": "Gail drops them off. June shows them round, and Callum watches from the end
 * of the hall").
 *
 * <ul>
 * <li><b>June Pell</b>, at the foot of her front step, beside the mat and just short of the line (she never stands on it): a
 * cardigan with its sleeves pushed up, her reading glasses on their chain, paint at the quick of two nails.</li>
 * <li><b>Gail Mercer</b>, the caseworker, on the path, her folder held to her chest and the lanyard round her neck, turned to June.</li>
 * <li><b>Tilly</b>, beside Gail on the path, her cane in her hand, her sunhat on and her backpack on her back. In this slice she is
 * always one of the house's people; a reader who takes the cane comes later.</li>
 * <li><b>Callum</b>, at the far end of the hall, barefoot, watching the front door.</li>
 * </ul>
 *
 * <p>They turn their heads to a reader near them, every tick, a little at a time; Callum watches one anywhere in the house or on
 * the lot, from further off, and turns his whole body to keep them in sight. A hand on
 * June, Gail or Tilly gets the next of their lines, as a caption over them (never chat). Callum does not speak: a hand on him gets
 * what he does instead. Nothing else of the visits is here yet: no dinner, no night, no copying, no line-walking.
 *
 * <p>They are the house's for everyone at it: every reader at the same house sees the same four. Each is made where they stand on
 * the first visit if they are not there; one already there (after a reload, say) is taken up again, never made twice.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PellPeople {
    private PellPeople(){}
    /** Stranger phases, from this one up: June, Tilly, Callum, Gail. */
    public static final int FIRST=195,JUNE=0,TILLY=1,CALLUM=2,GAIL=3,COUNT=4;
    private static final String[] SKINS={"june","tilly","callum","gail"};
    public static boolean cast(int phase){return phase>=FIRST&&phase<FIRST+COUNT;}
    public static String skin(int phase){return "pell_"+SKINS[phase-FIRST];}
    /** How big each is: the two grown women at full size, the two children smaller, Callum the smaller of them. */
    public static double size(int phase){return switch(phase-FIRST){case TILLY->.62;case CALLUM->.58;default->1;};}
    /** Tilly has her cane in her hand, and Gail her folder. */
    public static boolean carries(int phase){return phase==FIRST+TILLY||phase==FIRST+GAIL;}
    public static boolean is(@Nullable Entity e){return e instanceof LiteraryActor a&&a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    public static int who(LiteraryActor a){return a.phase()-FIRST;}
    /** What each is called over what they say. */
    public static String name(int who){return switch(who){case JUNE->"June";case TILLY->"Tilly";case CALLUM->"Callum";default->"Gail";};}
    /** Their gestures, for the renderer: Tilly's cane held out ahead of her, Gail's folder held to her chest; from 0.4.527, asleep (lying
     *  along a bed), June brushing Tilly's hair, and Tilly having it brushed. */
    public static final int CANE=1,FOLDER=2,ASLEEP=3,BRUSH=4,BRUSHED=5;

    /** Where each stands on the first visit, and which way they face (degrees; 0 is south, toward the street). */
    public record Spot(double x,double z,float look){}
    public static final Spot[] FIRST_VISIT={
        new Spot(1.5,-55.5,0),      // June: at the foot of her front step, beside the mat and short of the line, looking down the path
        new Spot(.5,-53,180),       // Tilly: on the path, turned to the house
        new Spot(-12.5,-65.5,-90),  // Callum: the far end of the hall, looking along it to the front door
        new Spot(1.5,-53.5,180)};   // Gail: on the path beside Tilly, turned to June
    /** How near a reader is for June, Gail and Tilly to turn their heads to them, and for Callum to watch them. */
    public static final double NEAR=8,WATCH=18;
    /** How far a head turns from where the body faces, in degrees, before the body comes round too (Callum's does). */
    public static final float HEAD_TURN=70;

    /** The four at one house: who they are by body; and, from 0.4.525, where the visit is (PellVisit). */
    static final class Household {
        final UUID[] ids=new UUID[COUNT];long lastTick=-1;final int[] said=new int[COUNT];
        /** Which way each faces when at rest, and who walked this tick (their heads are the walk's). */
        final float[] rest=new float[COUNT];final boolean[] moving=new boolean[COUNT];
        /** The visit: begun again from the start when fresh; how far it has got; when the next line may come. */
        boolean fresh=true,gailGone,tillyHome;int step,waypoint,gailStep,tillyStep,stuck,empty;long next;UUID car;
        /** Where June has walked, newest last, for Tilly to follow. */
        final java.util.ArrayDeque<Vec3> trail=new java.util.ArrayDeque<>();
        /** 0.4.526: the house's own hour, and whether it is going on; who has been sent it. */
        long time=PellVisit.AFTERNOON;int rate;final java.util.Set<UUID> told=new java.util.HashSet<>();
        /** 0.4.526: who is sitting, and in which of the table's places (PellVisit.TABLE); where each is walking to, and what they do
         *  when they get there (sit at a place, or stand facing a way). */
        final boolean[] seated=new boolean[COUNT];final int[] place={-1,-1,-1,-1};
        @SuppressWarnings("unchecked") final java.util.ArrayDeque<Vec3>[] way=new java.util.ArrayDeque[COUNT];
        final int[] sitAt={-1,-1,-1,-1};final float[] endLook=new float[COUNT];final int[] stuckFor=new int[COUNT];
        /** 0.4.526, dinner: each place's plate (PellVisit.NONE to SCRAPED), its body on the table, who eats there (a person, a reader,
         *  or nobody), the order June serves in, and when it was called, served and cleared. */
        final int[] plate=new int[4];final UUID[] plates=new UUID[4];final int[] diner={-1,-1,-1,-1};
        final java.util.List<Integer> order=new java.util.ArrayList<>();int serving;long calledAt,servedAt,clearedAt;
        /** 0.4.527, bedtime: who sits where away from the table (June's chair, the edge of Nessa's bed) and where a walker sits or lies
         *  down at the end of their way; who is asleep; which of the outside doors June has locked; whether she has asked for her chair
         *  back, how far the brushing and the locking up have got, and what she has said while the tape plays; when the tape's sound
         *  last began, the brushing began, and she last asked a reader to go out. */
        final PellVisit.Perch[] perch=new PellVisit.Perch[COUNT],perchTo=new PellVisit.Perch[COUNT];
        final Vec3[] sleepAt=new Vec3[COUNT];final boolean[] asleep=new boolean[COUNT];
        final java.util.Set<net.minecraft.core.BlockPos> locked=new java.util.HashSet<>();
        boolean chairSaid;int brushSaid,lockIndex,watchSaid;long tapeAt,brushedAt,nudgedAt;
        Household(){for(int i=0;i<COUNT;i++){rest[i]=FIRST_VISIT[i].look();way[i]=new java.util.ArrayDeque<>();}}
    }
    private static final Map<BlockPos,Household> HOUSES=new HashMap<>();
    public static void clearAll(){HOUSES.clear();}
    /** The house as if everybody had gone: its visit begins again from the start when a reader is next at it. */
    public static void vacate(BlockPos b){household(b).fresh=true;}
    static Household household(BlockPos b){return HOUSES.computeIfAbsent(b.immutable(),k->new Household());}

    public static AABB area(BlockPos b){return new AABB(b.getX()+PellSite.WEST,b.getY()-4,b.getZ()+PellSite.NORTH,b.getX()+PellSite.EAST+1,b.getY()+PellSite.ROOF+2,b.getZ()+2);}
    private static boolean live(ServerLevel l,Vec3 at){var cell=BlockPos.containing(at);return l.isLoaded(cell)&&l.areEntitiesLoaded(ChunkPos.asLong(cell));}
    static Vec3 world(BlockPos b,Spot s){return new Vec3(b.getX()+s.x(),b.getY(),b.getZ()+s.z());}

    /** One of them at this house, if their body is here. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){
        var h=household(b);
        if(h.ids[who]!=null&&l.getEntity(h.ids[who]) instanceof LiteraryActor a&&a.isAlive()&&is(a)&&who(a)==who)return a;
        for(var old:l.getEntitiesOfClass(LiteraryActor.class,area(b),PellPeople::is))if(who(old)==who&&old.isAlive()){h.ids[who]=old.getUUID();return old;}
        return null;
    }
    /**
     * Each of the four at the house, made where the first visit puts them if they are not there. Nothing is made where the ground's
     * entities are not loaded, so nobody is made twice over one who has yet to load.
     */
    public static void ensure(ServerLevel l,BlockPos b){
        var h=household(b);
        for(int who=0;who<COUNT;who++){
            // 0.4.525: Gail, once she has gone, is not made again until the visit begins again.
            if(who==GAIL&&h.gailGone&&!h.fresh)continue;
            if(find(l,b,who)!=null)continue;
            var at=world(b,FIRST_VISIT[who]);if(!live(l,at))continue;
            var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
            a.bind(PellHouse.PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+who);
            float look=FIRST_VISIT[who].look();
            a.moveTo(at.x,at.y,at.z,look,0);a.setYHeadRot(look);a.setYBodyRot(look);
            if(who==TILLY){a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.CANE.get()));a.gesture(CANE);}
            if(who==GAIL){a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.FOLDER.get()));a.gesture(FOLDER);}
            if(!l.addFreshEntity(a))continue;
            h.ids[who]=a.getUUID();
        }
    }
    /** None of the four is at the house: for a house built again from nothing, and for tests. */
    public static void reset(ServerLevel l,BlockPos b){
        for(var old:l.getEntitiesOfClass(LiteraryActor.class,area(b),PellPeople::is))old.discard();
        PellVisit.clearPlates(l,b);
        HOUSES.remove(b);
    }

    /**
     * Every game tick, each house with a reader at it. The scene's own reader tick comes a quarter second apart, and heads turned on
     * it would turn in steps.
     */
    @SubscribeEvent public static void serverTick(ServerTickEvent.Post e){
        if(HOUSES.isEmpty())return;var l=e.getServer().getLevel(NovelRooms.dimension(PellHouse.PLACE));if(l==null)return;
        for(var entry:List.copyOf(HOUSES.entrySet())){
            var b=entry.getKey();var h=entry.getValue();var here=PellHouse.readers(l,b);
            // 0.4.525: a house left empty long enough begins its visit again when somebody next comes.
            if(here.isEmpty()){if(++h.empty>=PellVisit.EMPTY)h.fresh=true;continue;}
            h.empty=0;tick(l,b,here);
        }
    }
    /** Once a tick for the house, however many readers are at it: each of them looks at the nearest reader near enough to look at. */
    public static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var h=household(b);long now=l.getGameTime();if(h.lastTick==now)return;h.lastTick=now;
        // 0.4.525: the visit moves them first; whoever walked this tick has their head where the walk put it.
        PellVisit.tick(l,b,h,here);
        for(int who=0;who<COUNT;who++){
            // Anybody asleep is left as they lie (0.4.527).
            if(h.moving[who]||h.asleep[who])continue;
            var a=find(l,b,who);if(a==null)continue;
            double reach=who==CALLUM?WATCH:NEAR;ServerPlayer near=null;double best=reach*reach;
            for(var p:here){if(p.isSpectator()||!p.isAlive())continue;double d=a.distanceToSqr(p);if(d<best){best=d;near=p;}}
            float rest=h.rest[who];
            if(near==null){turnHead(a,rest,rest);continue;}
            float toward=yawTo(a,near.getEyePosition());
            // Callum turns to follow whoever he is watching; the others turn only their heads, as far as a head turns, and their
            // bodies settle the way they stand. Anybody sitting at the table stays turned to it.
            float body=who==CALLUM&&!h.seated[who]?toward:rest;
            turnHead(a,body,toward);
        }
    }
    static float yawTo(Entity a,Vec3 at){var d=at.subtract(a.position());return (float)(Mth.atan2(d.z,d.x)*Mth.RAD_TO_DEG)-90;}
    /** Body to this yaw, and the head toward the other, no more than HEAD_TURN off the body, a little at a time. */
    private static void turnHead(LiteraryActor a,float body,float head){
        float b=Mth.approachDegrees(a.getYRot(),body,6);a.setYRot(b);a.setYBodyRot(b);
        float want=b+Mth.clamp(Mth.wrapDegrees(head-b),-HEAD_TURN,HEAD_TURN);
        a.setYHeadRot(Mth.approachDegrees(a.getYHeadRot(),want,12));
    }

    /** A hand on one of them: June, Gail and Tilly say the next of their lines; Callum does something instead. */
    public static void talk(ServerPlayer p,LiteraryActor a,net.minecraft.nbt.CompoundTag own){
        if(!is(a))return;var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null)return;
        int who=who(a);var h=household(b);
        // 0.4.527: asleep, they are only looked at.
        if(h.asleep[who]){PellHouse.cue(p,who==CALLUM?PellTexts.CALLUM_ASLEEP:PellTexts.TILLY_ASLEEP);return;}
        if(who==CALLUM){var lines=h.seated[who]?PellTexts.CALLUM_TABLE:PellTexts.CALLUM_HAND;PellHouse.cue(p,lines[h.said[who]++%lines.length]);return;}
        if(a.saying())return;
        var lines=switch(who){case JUNE->PellTexts.JUNE_TALK;case TILLY->PellTexts.TILLY_TALK;default->PellTexts.GAIL_TALK;};
        String line;
        // 0.4.527: in her chair with the tape on, June talks about the girl on it first.
        if(who==JUNE&&PellVisit.watching(b)&&h.watchSaid<PellTexts.JUNE_WATCHING.length)line=PellTexts.JUNE_WATCHING[h.watchSaid++];
        else line=lines[h.said[who]++%lines.length];
        a.say(name(who),line);a.speaks(line);
        // 0.4.525: June's rules count however they are heard: from a hand on her too. The reader whose hand it is keeps it in the
        // record the talk saves after this (LiteraryVignettes.talk), so it is written there and not saved under it.
        PellVisit.heard(a,PellTexts.rule(line),PellHouse.readers(p.serverLevel(),b),p,own);
    }
}
