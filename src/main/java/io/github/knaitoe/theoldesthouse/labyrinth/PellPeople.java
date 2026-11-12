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
    /** Their gestures, for the renderer: Tilly's cane held out ahead of her, Gail's folder held to her chest. */
    public static final int CANE=1,FOLDER=2;

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

    /** The four at one house: who they are by body. */
    static final class Household {
        final UUID[] ids=new UUID[COUNT];long lastTick=-1;final int[] said=new int[COUNT];
    }
    private static final Map<BlockPos,Household> HOUSES=new HashMap<>();
    public static void clearAll(){HOUSES.clear();}
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
        HOUSES.remove(b);
    }

    /**
     * Every game tick, each house with a reader at it. The scene's own reader tick comes a quarter second apart, and heads turned on
     * it would turn in steps.
     */
    @SubscribeEvent public static void serverTick(ServerTickEvent.Post e){
        if(HOUSES.isEmpty())return;var l=e.getServer().getLevel(NovelRooms.dimension(PellHouse.PLACE));if(l==null)return;
        for(var b:List.copyOf(HOUSES.keySet())){var here=PellHouse.readers(l,b);if(!here.isEmpty())tick(l,b,here);}
    }
    /** Once a tick for the house, however many readers are at it: each of them looks at the nearest reader near enough to look at. */
    public static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var h=household(b);long now=l.getGameTime();if(h.lastTick==now)return;h.lastTick=now;
        for(int who=0;who<COUNT;who++){
            var a=find(l,b,who);if(a==null)continue;
            double reach=who==CALLUM?WATCH:NEAR;ServerPlayer near=null;double best=reach*reach;
            for(var p:here){if(p.isSpectator()||!p.isAlive())continue;double d=a.distanceToSqr(p);if(d<best){best=d;near=p;}}
            float rest=FIRST_VISIT[who].look();
            if(near==null){turnHead(a,rest,rest);continue;}
            float toward=yawTo(a,near.getEyePosition());
            // Callum turns to follow whoever he is watching; the others turn only their heads, as far as a head turns.
            float body=who==CALLUM?toward:a.getYRot();
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
    public static void talk(ServerPlayer p,LiteraryActor a){
        if(!is(a))return;var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null)return;
        int who=who(a);var h=household(b);
        if(who==CALLUM){var lines=PellTexts.CALLUM_HAND;PellHouse.cue(p,lines[h.said[who]++%lines.length]);return;}
        if(a.saying())return;
        var lines=switch(who){case JUNE->PellTexts.JUNE_TALK;case TILLY->PellTexts.TILLY_TALK;default->PellTexts.GAIL_TALK;};
        String line=lines[h.said[who]++%lines.length];
        a.say(name(who),line);a.speaks(line);
    }
}
