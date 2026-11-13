package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.SecondPersonPayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.SecondPersonSite.*;

/**
 * 0.4.274, Second Person, the first slice (docs/SECOND_PERSON_0_4_274.md; the proposal is docs/SECOND_PERSON_PROPOSAL.md). A flat above
 * a shut launderette; somebody moves in who cannot see the reader: the House's guess at them, in their own House body, with the
 * routine object, the coat and the drawing the quiet rooms kept. Late summer, autumn, then a last morning with the door on the latch.
 * <p>
 * The year is each reader's own (the street is copied for them, SceneInstances) and is kept in their record step by step: its clock
 * counts only while they are there, so leaving pauses it. Nobody can see, answer or step aside for the reader. What they can do is
 * what people in houses like this have always done: move a thing, open or shut a door, turn a lamp on or off, make the air go cold,
 * knock, lift a ringing phone and listen, write a word in breath on cold glass, stand where glass or a camera might catch them. The
 * book on the shelf keeps up; the airing cupboard turns the season. Nothing here can hurt, and there is no failing.
 * <p>
 * 0.4.369 adds winter (docs/SECOND_PERSON_0_4_369.md) between autumn and the last morning: Iris dies and leaves the resident her sewing
 * tin, "for you, and for your lodger"; three mornings of grief at the basin; a train ticket in the unopened post; a night they get up
 * asleep and do, in the flat, what the reader did in the quiet rooms; once, in the bathroom mirror or the dark television, they see the
 * reader; and when the reader goes into the cupboard again, the House carries on in the reader's name.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SecondPerson {
    private SecondPerson(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SECOND_PERSON;
    /** The cast on stranger phases: Iris Dunne across the landing, and Sam from work. */
    public static final int IRIS=75,SAM=76;
    public static boolean cast(int phase){return phase==IRIS||phase==SAM;}
    public static final int SUMMER=0,AUTUMN=1,WINTER=2,MORNING=3;
    public static final int ARRIVE=0,UNPACK=1,GLASS_HUNG=2,BATH=3,NIGHT=4,SUMMER_DONE=5;
    public static final int IRIS_VISIT=0,ROUTINE=1,EVENING=2,PHOTO=3,CALL=4,STAY=5,AUTUMN_DONE=6;
    public static final int W_FUNERAL=0,W_MORNING=1,W_DAY=2,W_EVENING=3,W_NIGHT=4,WINTER_DONE=5;
    public static final int LATCH=0,OUT=1,ON_BENCH=2;
    /** The four things that are theirs, in the order they unpack them. */
    public static final List<String> THINGS=List.of("photo","drawing","coat","routine");
    /** Everything of theirs a reader can pick up from where it stands: the four, and in winter the train ticket. */
    static final List<String> CARRYABLE=List.of("photo","drawing","coat","routine","ticket");
    static final String TAG="SecondPerson274";
    public static final int EMPTY_RESET=200,WAIT=400,NIGHT_WAIT=1200,RING=200,EVENING_LENGTH=1200;
    /** Winter: how long a morning waits at the basin, a day, an evening in the armchair, a day away, and how long being seen lasts. */
    public static final int CARE_WAIT=400,DAY_LENGTH=360,WINTER_EVENING=600,AWAY=400,SEEN_OPEN=200,SEEN_STILL=80;
    static final int G_SIT=1,G_PHONE=3,G_CAMERA=4,G_PERCH=5,G_CARRY=6,G_COLD=7;
    /** 0.4.370: looking down at their phone, a bag in one hand, knitting, walking asleep, a hand out to the latch (SecondPersonRenderers). */
    static final int G_LOOK=8,G_BAG=9,G_KNIT=10,G_DRIFT=11,G_REACH=12;

    /** One flat in memory: its people and the counters between beats. Every fact lives in the reader's record. */
    static final class Flat {
        final BlockPos base;int empty;boolean cleared;
        @Nullable UUID resident,iris,sam;
        int ring=-1,doorway,armchair,window,shut,near,knockBack=-1,unseen,wordWipe=-1,flash,seenRun,seenAway,still;@Nullable Vec3 stillAt;
        boolean bedDoor,drift;long sent=Long.MIN_VALUE;
        /** 0.4.565: when they last stopped for a reader in a doorway and said nothing to them. */
        long stalled=Long.MIN_VALUE/2;
        final Map<UUID,Long> said=new HashMap<>();final Set<String> once=new HashSet<>();
        Flat(BlockPos b){base=b;}
    }
    private static final Map<Long,Flat> FLATS=new HashMap<>();
    public static void clearAll(){FLATS.clear();}
    @Nullable static Flat flat(BlockPos b){return FLATS.get(b.asLong());}

    // ------------------------------------------------------------------------------------------------ records
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 abs(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static Vec3 at(double x,double y,double z){return new Vec3(x,y,z);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void sound(ServerPlayer p,SoundEvent s,BlockPos b,BlockPos at,float volume,float pitch){LiteraryVignettes.heard(p,s,b.offset(at),volume,pitch);}
    public static int seasonOf(CompoundTag own){return own.getInt("Season");}
    public static int step(CompoundTag own){return own.getInt("Step");}
    static int t(CompoundTag own){return own.getInt("T");}
    static int sub(CompoundTag own){return own.getInt("Sub");}
    static int dt(CompoundTag own){return own.getInt("T")-own.getInt("SubT");}
    static void next(CompoundTag own,int sub){own.putInt("Sub",sub);own.putInt("SubT",own.getInt("T"));}
    static void go(CompoundTag own,int step){own.putInt("Step",step);own.putInt("T",0);own.putInt("Sub",0);own.putInt("SubT",0);}
    static CompoundTag thing(CompoundTag own,String thing){return own.getCompound("Thing_"+thing);}
    static String state(CompoundTag own,String thing){var t=thing(own,thing);return t.getString("State").isEmpty()?"box":t.getString("State");}
    static void state(CompoundTag own,String thing,String state){var t=thing(own,thing);t.putString("State",state);own.put("Thing_"+thing,t);}
    @Nullable static BlockPos placedAt(CompoundTag own,String thing){var t=thing(own,thing);return t.contains("At")?BlockPos.of(t.getLong("At")):null;}
    static void placedAt(CompoundTag own,String thing,@Nullable BlockPos at){var t=thing(own,thing);if(at==null)t.remove("At");else t.putLong("At",at.asLong());own.put("Thing_"+thing,t);}
    /** Is it dark in the flat? (Words on the street window, and the window behind the resident's chair.) */
    static boolean dark(CompoundTag own){int s=seasonOf(own),st=step(own);return s==SUMMER&&(st==BATH||st==NIGHT)||s==AUTUMN&&st>=EVENING||s==WINTER&&st!=W_DAY&&st!=WINTER_DONE;}
    /** The flat's hour, from the year so far. */
    static long hour(CompoundTag own){
        int s=seasonOf(own),st=step(own);
        if(s==SUMMER)return switch(st){case ARRIVE,UNPACK->8200;case GLASS_HUNG->10800;case BATH->13200;case NIGHT->18000;default->23400;};
        if(s==AUTUMN)return switch(st){case IRIS_VISIT->11400;case ROUTINE->12100;case EVENING->13600;case PHOTO->14200;case CALL->15000;case STAY->16000;default->17500;};
        if(s==WINTER)return switch(st){case W_FUNERAL->sub(own)>=2?18000:12700;case W_MORNING->23500;case W_DAY->4500;case W_EVENING->13400;case W_NIGHT->18500;default->2000;};
        return 900;
    }

    // ------------------------------------------------------------------------------------------------ the snapshot of the House's read
    /** The House's read of this reader, taken once on their first arrival; the vignette never changes it. */
    static CompoundTag snapshot(ServerPlayer p){
        var t=new CompoundTag();var d=LabyrinthData.get(p.server);var id=p.getUUID();
        boolean read=HouseRead.enabled(HouseRead.Trait.HOME);t.putBoolean("Read",read);
        if(!read){t.putInt("Routine",-1);t.putString("Coat","");return t;}
        var seen=HouseRead.seen(d,id);var bath=seen.getCompound("bathroom");
        t.putInt("Routine",bath.contains("Used")?bath.getInt("Used"):bath.contains("Item")?bath.getInt("Item"):-1);
        t.putString("Coat",HouseReadWardrobe.coatOf(seen.getCompound("coatroom")));
        var kitchen=seen.getCompound("kitchen");
        if(kitchen.getBoolean("Hung")){var dr=new CompoundTag();for(var key:List.of("Face","Hands","Hair","Clothes"))dr.putInt(key,kitchen.contains(key)?kitchen.getInt(key):-1);dr.putInt("Body",kitchen.getInt("Body"));dr.putInt("Mood",kitchen.getInt("Mood"));t.put("Drawing",dr);}
        t.putString("Home",HouseRead.value(d,id,HouseRead.Trait.HOME,true));
        t.putBoolean("Unseen",HouseRead.value(d,id,HouseRead.Trait.SELF,false).equals("UNSEEN"));
        // 0.4.369: the quiet rooms this reader went through, in the order the House showed them; the winter sleepwalk replays them.
        var walk=new net.minecraft.nbt.ListTag();for(var room:HouseReadSites.CHAIN)if(!seen.getCompound(room.key()).isEmpty())walk.add(net.minecraft.nbt.StringTag.valueOf(room.key()));t.put("Walk",walk);
        return t;
    }

    // ------------------------------------------------------------------------------------------------ arrival
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!own.contains("Snap")){own.put("Snap",snapshot(p));own.putInt("Season",SUMMER);go(own,ARRIVE);}
        var f=FLATS.computeIfAbsent(b.asLong(),k->new Flat(b));f.sent=Long.MIN_VALUE;f.empty=0;
        sky(p,f,own);
    }
    static void sky(ServerPlayer p,Flat f,CompoundTag own){long h=hour(own);if(h!=f.sent){f.sent=h;HousePackets.send(p,new SecondPersonPayload(h));}}

    // ------------------------------------------------------------------------------------------------ the cast
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    @Nullable public static LiteraryActor resident(ServerLevel l,BlockPos b){var f=flat(b);return f==null?null:actor(l,f.resident);}
    @Nullable public static LiteraryActor iris(ServerLevel l,BlockPos b){var f=flat(b);return f==null?null:actor(l,f.iris);}
    @Nullable public static LiteraryActor sam(ServerLevel l,BlockPos b){var f=flat(b);return f==null?null:actor(l,f.sam);}
    @Nullable static LiteraryActor spawn(ServerLevel l,BlockPos b,UUID reader,int role,int phase,Vec3 r){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,reader);a.appearance(role,phase);a.moveTo(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z,90,0);a.addTag(TAG);
        return l.addFreshEntity(a)?a:null;
    }
    static AABB bounds(BlockPos b){return new AABB(b.offset(-17,-3,-31).getCenter(),b.offset(17,12,0).getCenter()).inflate(1);}
    static void clearActors(ServerLevel l,BlockPos b){for(var a:l.getEntitiesOfClass(LiteraryActor.class,bounds(b),a->a.getTags().contains(TAG)))a.discard();}
    static boolean loaded(ServerLevel l,BlockPos b){for(int x=-17;x<=17;x+=16)for(int z=-31;z<=0;z+=15)if(!l.areEntitiesLoaded(ChunkPos.asLong(b.offset(x,0,z))))return false;return l.areEntitiesLoaded(ChunkPos.asLong(b.offset(17,0,-31)));}
    /** A line said above someone's head, for a while. Speech is never chat. */
    static void say(Flat f,@Nullable LiteraryActor a,String line){if(a==null||line.isEmpty())return;a.say(line);f.said.put(a.getUUID(),a.level().getGameTime());}

    /** Where someone should be, which way they face and how they hold themselves; absent marks have them gone. */
    record Mark(Vec3 at,float yaw,int gesture,boolean asleep,boolean present){
        static Mark gone(){return new Mark(Vec3.ZERO,0,0,false,false);}
        static Mark stand(double x,double z,float yaw){return new Mark(SecondPerson.at(x,UP,z),yaw,0,false,true);}
        static Mark stand(double x,double y,double z,float yaw){return new Mark(SecondPerson.at(x,y,z),yaw,0,false,true);}
        Mark pose(int g){return new Mark(at,yaw,g,asleep,present);}
    }
    static final float SOUTH=0,WEST=90,NORTH=180,EAST=-90;
    /** A chair: the cane chairs' seats stand a little higher than the armchair's and the sofa's, so the body sits up onto them. */
    static Mark seat(BlockPos chair,float yaw){boolean cane=chair.equals(RESIDENT_CHAIR)||chair.equals(GUEST_CHAIR)||chair.equals(HALL_CHAIR);return new Mark(at(chair.getX()+.5,chair.getY()+(cane?.1:0),chair.getZ()+.5),yaw,G_SIT,false,true);}
    /** The edge of the bed at its foot, facing the bedroom door; the mattress is higher than a chair's seat. */
    static Mark bedEdge(){return new Mark(at(BED_FOOT.getX()+.5,UP+.12,BED_FOOT.getZ()+.2),NORTH,G_SIT,false,true);}
    static Mark bed(){return new Mark(at(BED_HEAD.getX()+.5,UP,BED_HEAD.getZ()+.5),EAST,0,true,true);}
    /** Somewhere to stand beside a fitting, from fixed places in the flat. */
    static Mark beside(String what){return switch(what){
        case "photo_box"->Mark.stand(-.5,-17.5,NORTH);case "coat_box"->Mark.stand(.5,-18.5,NORTH);case "drawing_box"->Mark.stand(-1.5,-21.5,NORTH);case "last_box"->Mark.stand(1.5,-16.5,EAST);
        case "photo"->Mark.stand(.5,-15.5,NORTH);case "drawing"->Mark.stand(-1.5,-21.5,SOUTH);case "coat"->Mark.stand(1.5,-25.5,EAST);case "routine"->Mark.stand(-.5,-25.5,NORTH);
        case "glass"->Mark.stand(1.5,-22.5,EAST);case "phone"->Mark.stand(-5.5,-18.5,WEST);case "sink"->Mark.stand(-2.5,-21.5,NORTH);case "bathdoor"->Mark.stand(1.5,-25.5,WEST);
        case "door"->Mark.stand(1.5,-26.5,EAST);case "window"->Mark.stand(-3.5,-14.5,SOUTH);case "photo_spot"->Mark.stand(1.5,-16.5,WEST);case "sofa"->new Mark(at(-4.5,UP,-18.5),SOUTH,G_SIT,false,true);
        case "hall_bag"->Mark.stand(1.5,-25.5,SOUTH);case "tub"->new Mark(at(-2.5,UP,-25.5),SOUTH,G_SIT,false,true);
        // 0.4.369, winter.
        case "basin"->Mark.stand(-1.5,-25.9,NORTH);case "hall_table"->Mark.stand(1.5,-21.6,EAST);case "lamp"->Mark.stand(-2.5,-17.8,NORTH);case "knock"->Mark.stand(-2.5,-29.3,NORTH);
        default->Mark.stand(-2.5,-17.5,WEST);};}
    static String boxOf(String thing){return switch(thing){case "photo"->"photo_box";case "coat"->"coat_box";case "drawing"->"drawing_box";default->"last_box";};}
    static BlockPos boxAt(String thing){return switch(thing){case "photo"->PHOTO_BOX;case "coat"->COAT_BOX;case "drawing"->DRAWING_BOX;default->LAST_BOX;};}
    @Nullable static Mark nearThing(CompoundTag own,String thing){var at=placedAt(own,thing);if(at==null)return null;return Mark.stand(at.getX()+.5,at.getZ()+1.5,NORTH);}

    /** Where the resident is, from what the record says has happened. */
    static Mark residentMark(CompoundTag own){
        int s=seasonOf(own),st=step(own),sub=sub(own);
        if(s==SUMMER)switch(st){
            case ARRIVE->{return Mark.gone();}
            case UNPACK->{
                if(sub==0)return beside("last_box").pose(G_CARRY);
                if(sub==1)return new Mark(at(LAST_BOX.getX()+.5,UP,LAST_BOX.getZ()+.5),WEST,G_PERCH,false,true);
                int i=(sub-2)/2;if(i>=THINGS.size())return beside("glass");String th=THINGS.get(i);boolean toBox=(sub-2)%2==0;
                if(toBox)return beside(boxOf(th));
                String state=state(own,th);
                if(state.equals("placed")){var m=nearThing(own,th);if(m!=null)return m;}
                if(state.equals("home")||state.equals("resident"))return beside(th).pose(state.equals("resident")?0:0);
                return beside("default");
            }
            case GLASS_HUNG->{return sub==0?beside("glass"):seat(ARMCHAIR,WEST);}
            case BATH->{
                if(sub==1)return beside("tub");
                if(sub==2)return own.getString("Call1").equals("left")?beside("phone").pose(G_PHONE):own.getString("Call1").equals("put_down")?beside("bathdoor"):beside("tub");
                return beside("tub");
            }
            case NIGHT->{
                if(sub==2&&own.getString("Night").equals("tap"))return beside("sink");
                if(sub==2&&own.getString("Night").equals("doorway"))return bedEdge();
                return bed();
            }
            default->{return bed();}
        }
        if(s==AUTUMN)switch(st){
            case IRIS_VISIT->{return sub>=1&&sub<=2?beside("door"):seat(RESIDENT_CHAIR,EAST);}
            case ROUTINE->{
                String state=state(own,"routine");
                if(state.equals("placed")){var m=nearThing(own,"routine");if(m!=null)return m;}
                return beside("routine");
            }
            case EVENING->{return sub==0?beside("door"):seat(RESIDENT_CHAIR,EAST);}
            case PHOTO->{return sub<=1?beside("photo_spot").pose(G_CAMERA):beside("photo_spot").pose(G_LOOK);}
            case CALL->{return own.getString("Call2").equals("left")?beside("phone").pose(G_PHONE):seat(RESIDENT_CHAIR,EAST);}
            case STAY->{return own.getBoolean("SamStayed")?beside("sofa"):beside("door");}
            default->{return own.getBoolean("SamStayed")?beside("sofa"):seat(ARMCHAIR,WEST);}
        }
        if(s==WINTER)switch(st){
            case W_FUNERAL->{return sub==0?beside("sink"):sub==1?seat(RESIDENT_CHAIR,EAST):bed();}
            case W_MORNING->{return sub==0&&dt(own)<40?bed():beside("basin");}
            case W_DAY->{
                if(own.getInt("Care")==0)return sub==0?beside("hall_table"):seat(ARMCHAIR,WEST);
                String ticket=own.getString("Ticket");
                if(ticket.equals("mat"))return switch(sub){case 0->beside("door");case 1->beside("hall_bag").pose(G_BAG);case 2->Mark.gone();default->seat(ARMCHAIR,WEST);};
                if(ticket.equals("left")&&sub==0){var m=state(own,"ticket").equals("placed")?nearThing(own,"ticket"):null;return m!=null?m:beside("hall_table");}
                return seat(ARMCHAIR,WEST);
            }
            case W_EVENING->{var m=seat(ARMCHAIR,WEST);return own.getBoolean("SleeveFound")?m.pose(G_KNIT):m;}
            case W_NIGHT->{return sub>=2&&sub<100?station(own,(sub-2)/2):bed();}
            default->{return seat(ARMCHAIR,WEST);}
        }
        // The last morning.
        boolean stays=own.getString("Decision").equals("stay");
        if(st==LATCH)return stays?seat(RESIDENT_CHAIR,EAST):beside("hall_bag").pose(G_BAG);
        if(stays)return beside("window");
        return sub==0?Mark.stand(5.5,0,-11.0,SOUTH).pose(G_BAG):sub==1?Mark.stand(8.5,0,-11.0,EAST).pose(G_BAG):sub==2?Mark.stand(15.5,0,-11.0,EAST).pose(G_BAG):Mark.gone();
    }
    /** The resident first comes up the stairs with the last box; afterwards they are wherever the year left them. */
    static @Nullable Vec3 residentFrom(CompoundTag own){
        int s=seasonOf(own),st=step(own),sub=sub(own);
        boolean landing=s==SUMMER&&st==UNPACK&&sub==0||s==WINTER&&(st==W_FUNERAL&&sub==0||st==W_DAY&&sub==3);
        return landing?at(5.5,UP,-24.5):null;
    }
    /** Where the sleepwalk takes them, station by station: what the reader did in the quiet rooms, in that order, then the latch. */
    static Mark station(CompoundTag own,int i){
        var walk=SecondPersonStory.list(own,"WalkList");if(i<0||i>=walk.size())return bed();
        return switch(walk.get(i)){case "kitchen"->seat(RESIDENT_CHAIR,EAST);case "waiting"->seat(HALL_CHAIR,WEST);case "bathroom"->beside("basin").pose(G_DRIFT);case "coatroom"->beside("coat").pose(G_DRIFT);
            case "shelter"->beside("window").pose(G_DRIFT);case "lamp"->beside("lamp").pose(G_DRIFT);case "tap"->beside("sink").pose(G_DRIFT);case "glass"->beside("glass").pose(G_DRIFT);case "knock"->beside("knock").pose(G_DRIFT);default->beside("door").pose(G_REACH);};
    }
    static Mark irisMark(CompoundTag own){
        int s=seasonOf(own),st=step(own),sub=sub(own);
        if(s==AUTUMN&&st==IRIS_VISIT&&sub>=1&&sub<=2)return Mark.stand(4.5,UP,-27.5,WEST);
        if(s==AUTUMN&&st==AUTUMN_DONE&&own.getBoolean("IrisKnocked")&&!own.getBoolean("IrisTold")&&sub==1)return Mark.stand(4.5,UP,-27.5,WEST);
        return Mark.gone();
    }
    static Mark samMark(CompoundTag own){
        int s=seasonOf(own),st=step(own),sub=sub(own);if(s!=AUTUMN)return Mark.gone();
        // Going home: out to the landing, then gone.
        if(own.getBoolean("SamLeft"))return own.getInt("Clock")-own.getInt("SamLeftAt")<160?Mark.stand(4.5,UP,-27.5,EAST):Mark.gone();
        boolean left=own.getString("Evening").equals("seen")&&st>EVENING||own.getBoolean("SamLeft");
        if(st==EVENING)return sub==0?Mark.stand(4.5,UP,-27.5,WEST):own.getBoolean("SamLeft")?Mark.gone():seat(GUEST_CHAIR,WEST);
        if(left)return Mark.gone();
        if(st==PHOTO||st==CALL)return beside("sofa");
        if(st==STAY)return own.getBoolean("SamStayed")?new Mark(at(-3.5,UP,-18.5),SOUTH,G_SIT,false,true):Mark.stand(2.5,-26.5,EAST);
        if(st==AUTUMN_DONE)return own.getBoolean("SamStayed")?new Mark(at(-3.5,UP,-18.5),SOUTH,G_SIT,false,true):Mark.gone();
        return Mark.gone();
    }

    /** Puts someone at their mark: straight there when the reader cannot see them or it is far, otherwise on foot; never through the reader. */
    static @Nullable LiteraryActor stage(ServerLevel l,Flat f,ServerPlayer p,@Nullable UUID id,Mark mark,@Nullable Vec3 from,int role,int phase,java.util.function.Consumer<UUID> keep){
        var b=f.base;var a=actor(l,id);
        if(!mark.present()){if(a!=null){if(a.isSleeping())a.stopSleeping();a.discard();}return null;}
        var to=abs(b,mark.at());
        if(a==null){
            // 0.4.560 (the standard cast checks): nobody comes into being where the reader can see; they come in when nobody is looking.
            var at=abs(b,from==null?mark.at():from);
            if(io.github.knaitoe.theoldesthouse.house.HouseWatchers.sees(p,at.add(0,1.6,0))||io.github.knaitoe.theoldesthouse.house.HouseWatchers.sees(p,at.add(0,.3,0)))return null;
            a=spawn(l,b,p.getUUID(),role,phase,from==null?mark.at():from);if(a==null)return null;keep.accept(a.getUUID());
        }
        double dist=a.position().distanceToSqr(to);
        boolean seated=mark.gesture()==G_SIT||mark.gesture()==G_PERCH||mark.gesture()==G_KNIT||mark.asleep();
        boolean resident=id!=null&&id.equals(f.resident);int stride=resident&&f.drift?G_DRIFT:0;
        if(mark.asleep()&&a.isSleeping()){a.gesture(0);return a;}
        if(a.isSleeping())a.stopSleeping();
        // A seat or the bed: walk up to it, then sit or lie down in it. 0.4.560 (the standard cast checks): the last of the way, past
        // the seat's edge, is a short slide over a few ticks, not one step of up to two and a half blocks.
        if(seated&&dist<6.25&&dist>.04){
            var way=to.subtract(a.position());double length=Math.sqrt(dist);var step=way.scale(Math.min(.2,length)/length);
            a.noPhysics=true;a.setNoGravity(true);a.gesture(stride);a.moveTo(a.getX()+step.x,a.getY()+step.y,a.getZ()+step.z,a.getYRot(),0);a.setDeltaMovement(Vec3.ZERO);
            SceneFootpaths.face(a,to,20);return a;
        }
        if(seated&&dist<6.25){
            if(mark.asleep()){a.noPhysics=false;a.setNoGravity(false);a.gesture(0);a.moveTo(to.x,to.y,to.z,mark.yaw(),0);a.startSleeping(b.offset(BED_HEAD));return a;}
            a.gesture(mark.gesture());a.noPhysics=true;a.setNoGravity(true);a.moveTo(to.x,to.y,to.z,mark.yaw(),0);a.setYRot(mark.yaw());a.setYBodyRot(mark.yaw());a.setYHeadRot(mark.yaw());return a;
        }
        a.gesture(seated?stride:mark.gesture());
        if(dist<.09){a.noPhysics=false;a.setNoGravity(false);a.setYRot(mark.yaw());a.setYBodyRot(mark.yaw());a.setYHeadRot(mark.yaw());return a;}
        a.noPhysics=false;a.setNoGravity(false);
        // Put straight there only while the reader sees neither them nor the place (0.4.560: a long way off is no longer enough).
        if(!io.github.knaitoe.theoldesthouse.house.HouseWatchers.sees(p,a.getEyePosition())&&!io.github.knaitoe.theoldesthouse.house.HouseWatchers.sees(p,a.position().add(0,.3,0))
                &&!io.github.knaitoe.theoldesthouse.house.HouseWatchers.sees(p,to.add(0,1.5,0))&&p.position().distanceToSqr(to)>16){a.moveTo(to.x,to.y,to.z,mark.yaw(),0);a.setYHeadRot(mark.yaw());a.setYBodyRot(mark.yaw());return a;}
        // They stop in a doorway the reader is standing in, rather than walk through them.
        var ahead=to.subtract(a.position()).normalize();var toReader=p.position().subtract(a.position());
        // 0.4.565 (the two-reader scene checks found it): at most once in twenty seconds, not once in each twenty-second window, which let
        // two stops either side of a window's edge, three seconds apart, say it twice.
        if(toReader.horizontalDistance()<1.1&&ahead.dot(toReader.normalize())>.4){if(l.getGameTime()-f.stalled>=400){f.stalled=l.getGameTime();say(f,a,"...");}return a;}
        doorsFor(l,b,rel(a.position(),b),mark.at());openOnTheWay(l,f,b,a,to);
        SceneFootpaths.walk(a,to,a.gesture()==G_DRIFT?.075:.12);
        return a;
    }
    /**
     * 0.4.370: what the resident has in their hand follows what they are doing: the landline's receiver, off its cradle, while they are on
     * it; their phone to take the picture and look at it; a bag on their way out; Iris's sleeve while they knit, taken up off the chair's
     * arm and laid back on it after.
     */
    static void props(ServerLevel l,Flat f,CompoundTag own,@Nullable LiteraryActor a){
        if(a==null)return;var b=f.base;int g=a.gesture();
        ItemStack want=switch(g){
            case G_PHONE->prop(SecondPersonRegistry.RECEIVER_MODEL,"Receiver");
            case G_CAMERA,G_LOOK->new ItemStack(HouseReadRegistry.PHONE.get());
            case G_BAG->new ItemStack(Items.BUNDLE);
            case G_KNIT->prop(SecondPersonRegistry.SLEEVE_MODEL,"Iris's sleeve");
            default->ItemStack.EMPTY;};
        if(!ItemStack.isSameItemSameComponents(a.getMainHandItem(),want))a.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,want);
        boolean onLine=g==G_PHONE&&a.distanceToSqr(Vec3.atCenterOf(b.offset(PHONE)))<6;int stage=SecondPersonBlock.stage(l.getBlockState(b.offset(PHONE)));
        if(onLine&&stage==0)set(l,b,PHONE,SecondPersonBlock.of(SecondPersonBlock.Kind.PHONE,Direction.EAST,2));else if(!onLine&&stage==2)phone(l,b,false);
        boolean onArm=state(own,"sleeve").equals("arm")&&!own.getBoolean("SleeveFinished");
        if(g==G_KNIT&&sleeve(l,b)==0)sleeve(l,b,-1);else if(g!=G_KNIT&&onArm&&sleeve(l,b)<0)sleeve(l,b,0);
    }
    static ItemStack prop(int model,String name){var s=new ItemStack(SecondPersonRegistry.CARRIED.get());s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(model));s.set(DataComponents.CUSTOM_NAME,Component.literal(name));return s;}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,3);}
    /** A hand put to something: taking it out of a box, setting it down, turning the note over. */
    static void handle(@Nullable LiteraryActor a){if(a!=null)a.swing(net.minecraft.world.InteractionHand.MAIN_HAND);}
    static boolean arrived(@Nullable LiteraryActor a,BlockPos b,Mark m){return a!=null&&(m.asleep()?a.isSleeping():a.position().distanceToSqr(abs(b,m.at()))<.2);}
    /** A door between them and where they are going is opened as they come to it; a door the reader shut on them is noticed once. */
    static void openOnTheWay(ServerLevel l,Flat f,BlockPos b,LiteraryActor a,Vec3 to){
        for(var door:List.of(FLAT_DOOR,KITCHEN_DOOR,BATH_DOOR,BED_DOOR)){
            var at=b.offset(door);if(a.position().distanceToSqr(Vec3.atBottomCenterOf(at))>6.5)continue;
            var s=l.getBlockState(at);if(!(s.getBlock() instanceof DoorBlock)||s.getValue(DoorBlock.OPEN))continue;
            ((DoorBlock)s.getBlock()).setOpen(a,l,s,at,true);
            if(f.once.add("shut_on_them_"+door.asLong()))say(f,a,"Did I shut that?");
        }
    }
    /** The door into or out of the room they are going to, opened ahead of them when they are near it. */
    static void doorsFor(ServerLevel l,BlockPos b,Vec3 from,Vec3 to){
        boolean fromFlat=inFlat(from),toFlat=inFlat(to);
        if(fromFlat!=toFlat&&near(from,FLAT_DOOR))open(l,b,FLAT_DOOR,true);
        if((inBathroom(from)!=inBathroom(to))&&near(from,BATH_DOOR))open(l,b,BATH_DOOR,true);
        if((inKitchen(from)!=inKitchen(to))&&near(from,KITCHEN_DOOR))open(l,b,KITCHEN_DOOR,true);
        if((inBedroom(from)!=inBedroom(to))&&near(from,BED_DOOR))open(l,b,BED_DOOR,true);
    }
    static boolean near(Vec3 r,BlockPos door){return r.distanceToSqr(Vec3.atBottomCenterOf(door))<30;}
    static void open(ServerLevel l,BlockPos b,BlockPos door,boolean open){var at=b.offset(door);var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock d&&s.getValue(DoorBlock.OPEN)!=open)d.setOpen(null,l,s,at,open);}
    public static boolean isOpen(ServerLevel l,BlockPos b,BlockPos door){var s=l.getBlockState(b.offset(door));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}

    // ------------------------------------------------------------------------------------------------ the tick
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        var server=event.getServer();var seen=new HashSet<Long>();
        for(var p:server.getPlayerList().getPlayers()){
            if(p.isSpectator()||!p.isAlive()||LiteraryVignettes.current(p)!=PLACE)continue;
            var b=LiteraryVignettes.base(p,PLACE);if(b==null||!inside(rel(p.position(),b)))continue;
            var l=p.serverLevel();if(!loaded(l,b))continue;
            var f=FLATS.computeIfAbsent(b.asLong(),k->new Flat(b));seen.add(b.asLong());f.empty=0;
            var own=own(p);if(!own.contains("Snap"))continue;
            if(!f.cleared){clearActors(l,b);f.cleared=true;}
            own.putInt("T",t(own)+1);own.putInt("Clock",own.getInt("Clock")+1);
            story(l,f,p,own);
            save(p,own);
            sky(p,f,own);
            var gone=new ArrayList<UUID>();for(var e:f.said.entrySet()){var a=actor(l,e.getKey());if(a!=null&&a.saying())continue;if(a!=null){a.setCustomName(null);a.setCustomNameVisible(false);}gone.add(e.getKey());}gone.forEach(f.said::remove);
        }
        for(var it=FLATS.values().iterator();it.hasNext();){var f=it.next();if(seen.contains(f.base.asLong()))continue;
            if(++f.empty>=EMPTY_RESET){var l=server.getLevel(NovelRooms.dimension(PLACE));if(l!=null&&l.isLoaded(f.base))clearActors(l,f.base);it.remove();}}
        // Something of theirs carried out of the flat goes back to being lost.
        if(server.getTickCount()%20==0)for(var p:server.getPlayerList().getPlayers()){
            var b=LiteraryVignettes.current(p)==PLACE?LiteraryVignettes.base(p,PLACE):null;
            boolean in=b!=null&&p.isAlive()&&inside(rel(p.position(),b));
            if(!in&&!held(p).isEmpty()){var thing=held(p);take(p);var own=own(p);if(state(own,thing).equals("held")){state(own,thing,"missing");save(p,own);}}
        }
    }

    /** The year, one tick at a time, for one reader in their flat. */
    static void story(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own){
        var b=f.base;var r=rel(p.position(),b);
        // Everyone at their marks.
        f.drift=own.getBoolean("Walking");
        var resident=stage(l,f,p,f.resident,residentMark(own),residentFrom(own),LiteraryActor.SP_RESIDENT,0,id->f.resident=id);
        props(l,f,own,resident);
        var iris=stage(l,f,p,f.iris,irisMark(own),at(5.5,UP,-30.5),LiteraryActor.STRANGER,IRIS,id->f.iris=id);
        var sam=stage(l,f,p,f.sam,samMark(own),at(5.5,UP,-24.5),LiteraryActor.STRANGER,SAM,id->f.sam=id);
        cold(l,f,p,own,resident,iris,sam);
        words(l,f,p,own,resident);
        glassGlimpse(l,f,p,own,resident);
        if(f.knockBack>0&&--f.knockBack==0){sound(p,DoorSounds.KNOCK_FAR.get(),b,new BlockPos(-2,UP+1,-31),.9F,1.05F);cue(p,"Somebody knocks back, from the other side of the wall.");}
        ringing(l,f,p,own,resident);
        cupboard(l,f,p,own,r);
        int s=seasonOf(own);
        if(s==SUMMER)summer(l,f,p,own,r,resident);else if(s==AUTUMN)autumn(l,f,p,own,r,resident,iris,sam);else if(s==WINTER)winter(l,f,p,own,resident);else morning(l,f,p,own,r,resident);
    }

    static void summer(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,Vec3 r,@Nullable LiteraryActor a){
        var b=f.base;int st=step(own),sub=sub(own),dt=dt(own);
        switch(st){
            case ARRIVE->{
                if(t(own)==20){cue(p,"Someone is coming up the stairs.");sound(p,SoundEvents.WOOD_STEP,b,new BlockPos(5,2,-18),.8F,.9F);}
                if(t(own)>=WAIT||own.getBoolean("Read_Source")&&t(own)>=60){go(own,UNPACK);open(l,b,FLAT_DOOR,true);sound(p,SoundEvents.WOODEN_DOOR_OPEN,b,FLAT_DOOR,1,1);}
            }
            case UNPACK->{
                var mark=residentMark(own);
                if(sub==0){if(arrived(a,b,mark)){box(l,b,LAST_BOX,0);sound(p,SoundEvents.WOOL_PLACE,b,LAST_BOX,1,.8F);say(f,a,"Right.");next(own,1);}}
                else if(sub==1){if(dt==70)say(f,a,"Right. That's everything.");if(dt>=170)next(own,2);}
                else{int i=(sub-2)/2;
                    if(i>=THINGS.size()){go(own,GLASS_HUNG);for(var at:List.of(PHOTO_BOX,COAT_BOX,DRAWING_BOX,LAST_BOX))box(l,b,at,3);return;}
                    String th=THINGS.get(i);boolean toBox=(sub-2)%2==0;
                    if(toBox){if(!arrived(a,b,mark))return;
                        String state=state(own,th);
                        if(state.equals("box")){box(l,b,boxAt(th),2);state(own,th,"resident");handle(a);sound(p,SoundEvents.BUNDLE_REMOVE_ONE,b,boxAt(th),1,1);if(th.equals("drawing"))say(f,a,"Kettle. Where's the kettle.");next(own,sub+1);}
                        else if(state.equals("home")){found(own,th,"put_away");next(own,sub+1);}
                        else if(state.equals("placed")){found(own,th,"moved");next(own,sub+1);}
                        else{if(!f.once.contains("hunt_"+th)){f.once.add("hunt_"+th);say(f,a,"It was here. It was in this box.");found(own,th,"hidden");next(own,sub);return;}
                            if(dt>=110){say(f,a,"It'll turn up.");f.once.remove("hunt_"+th);next(own,sub+2);}}
                    }else{if(!arrived(a,b,mark))return;
                        String state=state(own,th);
                        if(state.equals("resident")){home(l,b,own,th);state(own,th,"homed");handle(a);sound(p,SoundEvents.ITEM_FRAME_ADD_ITEM,b,homeAt(th),1,1);if(th.equals("photo"))say(f,a,"There.");if(th.equals("routine"))own.putString("RoutineWhere","on the bathroom shelf");next(own,sub+1);}
                        else if(state.equals("home")){if(!f.once.contains("found_"+th)){f.once.add("found_"+th);say(f,a,"I didn't put that there.");next(own,sub);return;}
                            if(dt==60)say(f,a,"I must have.");if(dt>=100){f.once.remove("found_"+th);next(own,sub+1);}}
                        else if(state.equals("placed")){if(!f.once.contains("found_"+th)){f.once.add("found_"+th);say(f,a,"That's not where I'd have put it.");next(own,sub);return;}
                            if(dt==60)say(f,a,"Fine. There, then.");if(dt>=90){f.once.remove("found_"+th);next(own,sub+1);}}
                        else next(own,sub+1);
                    }
                }
            }
            case GLASS_HUNG->{
                if(sub==0){if(arrived(a,b,residentMark(own))){boolean unseen=own.getCompound("Snap").getBoolean("Unseen");glass(l,b,unseen?1:0);handle(a);own.putBoolean("GlassHungTurned",unseen);sound(p,SoundEvents.ITEM_FRAME_PLACE,b,GLASS,1,1);if(unseen)say(f,a,"Not yet.");next(own,1);}}
                else{
                    if(dt>=200&&dt%50==0&&glass(l,b)==1&&!own.getBoolean("GlassHungTurned")&&!own.getBoolean("GlassTurned")){own.putBoolean("GlassTurned",true);say(f,a,"...Fine.");}
                    if(dt==120)say(f,a,"It's quiet.");
                    if(dt>=WAIT)go(own,BATH);
                }
            }
            case BATH->bath(l,f,p,own,a);
            case NIGHT->night(l,f,p,own,r,a);
            default->{}
        }
    }
    static void bath(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a){
        var b=f.base;int sub=sub(own),dt=dt(own);
        if(sub==0){open(l,b,BATH_DOOR,true);if(arrived(a,b,residentMark(own))){next(own,1);}}
        else if(sub==1){
            if(dt%20==0&&dt<=100)sound(p,HouseReadRegistry.TAP.get(),b,TUB,.7F,1);
            if(dt==100){mirror(l,b,true);own.putInt("SteamUntil",t(own)+1600);}
            if(dt==160){f.ring=RING;own.putInt("Ringing",1);}
            unanswered(p,own,1);
            if(!own.getString("Call1").isEmpty()&&f.ring<0)next(own,2);
        }
        else if(sub==2){
            if(dt==1){switch(own.getString("Call1")){case "left"->say(f,a,SecondPersonStory.caught(own,1));case "put_down"->say(f,a,"...");default->{}}}
            if(dt==80&&own.getString("Call1").equals("put_down"))say(f,a,"Nobody, then.");
            if(dt>=200){go(own,NIGHT);open(l,b,BED_DOOR,true);f.bedDoor=true;}
        }
    }
    static void night(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,Vec3 r,@Nullable LiteraryActor a){
        var b=f.base;int sub=sub(own),dt=dt(own);
        if(sub==0){if(a!=null&&a.isSleeping()){next(own,1);f.bedDoor=isOpen(l,b,BED_DOOR);}return;}
        if(sub==1){
            if(dt%240==60)say(f,a,dt<300?"Come on.":dt<700?"Can't.":"...");
            String choice="";
            if(inBedroomDoorway(r)){if(++f.doorway>=60)choice="doorway";}else f.doorway=0;
            if(p.getVehicle()!=null&&p.blockPosition().distManhattan(b.offset(ARMCHAIR))<=1){if(++f.armchair>=60)choice="armchair";}else f.armchair=0;
            boolean doorOpen=isOpen(l,b,BED_DOOR);if(f.bedDoor&&!doorOpen)choice="shut";f.bedDoor=doorOpen;
            if(own.getBoolean("TapRan"))choice="tap";
            if(choice.isEmpty()&&dt>=NIGHT_WAIT)choice="alone";
            if(!choice.isEmpty()){own.putString("Night",choice);next(own,2);
                switch(choice){case "doorway"->{say(f,a,"...hello?");SecondPersonStory.turned(own,"You stood in their doorway.");}case "tap"->SecondPersonStory.turned(own,"You ran the kitchen tap in the night.");
                    case "armchair"->SecondPersonStory.turned(own,"You sat in the armchair in the dark.");case "shut"->{say(f,a,"...Oh.");SecondPersonStory.turned(own,"You shut their door on them.");}
                    default->cue(p,"They sleep at last, near dawn.");}}
            return;
        }
        if(sub==2){
            if(own.getString("Night").equals("tap")){if(dt==40){own.remove("TapRan");say(f,a,"...");}if(dt==120)say(f,a,"Who's running the tap.");}
            if(own.getString("Night").equals("doorway")&&dt==90)say(f,a,"Nobody. Go to sleep.");
            if(dt>=200){go(own,SUMMER_DONE);cue(p,"Morning comes in thin through the street window. The airing cupboard at the end of the hall is warm.");}
        }
    }

    static void autumn(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,Vec3 r,@Nullable LiteraryActor a,@Nullable LiteraryActor iris,@Nullable LiteraryActor sam){
        var b=f.base;int st=step(own),sub=sub(own),dt=dt(own);
        switch(st){
            case IRIS_VISIT->{
                if(sub==0){if(dt>=200){sound(p,DoorSounds.KNOCK.get(),b,FLAT_DOOR,1,1);say(f,a,"Coming.");next(own,1);}}
                else if(sub==1){if(arrived(a,b,residentMark(own))){handle(a);open(l,b,FLAT_DOOR,true);open(l,b,IRIS_DOOR,true);next(own,2);}}
                else if(sub==2){
                    if(dt==20)say(f,iris,"I made too much again. It's only stew.");
                    if(dt==90)say(f,a,"You didn't have to. Thank you.");
                    if(dt==160)say(f,iris,"That was Ted's chair by your window. He liked to watch the buses.");
                    if(dt==240){say(f,iris,"This flat's never been empty in my time. You're all right. Just don't frighten them.");cue(p,"She says it past them, into the room.");SecondPersonStory.turned(own,"Iris spoke to you over their shoulder.");}
                    if(dt>=330){open(l,b,FLAT_DOOR,false);open(l,b,IRIS_DOOR,false);next(own,3);}
                }
                else if(dt>=200)go(own,ROUTINE);
            }
            case ROUTINE->{
                String state=state(own,"routine");
                if(sub==0){if(dt==10)say(f,a,"Sam's coming. Right.");if(arrived(a,b,residentMark(own))){next(own,state.equals("homed")||state.equals("home")||state.equals("placed")?1:2);}}
                else if(sub==1){if(dt==60){say(f,a,"There.");}if(dt>=140){next(own,4);}}
                else if(sub==2){if(dt==0||dt==1)say(f,a,"I had it. I had it this morning.");
                    if(state.equals("placed")||state.equals("home")){own.putBoolean("RoutineSet",true);next(own,3);SecondPersonStory.turned(own,"You set their "+SecondPersonStory.routine(own)+" out where they would see it.");}
                    else if(dt>=WAIT){say(f,a,"Never mind. Never mind.");next(own,4);}}
                else if(sub==3){if(arrived(a,b,residentMark(own))&&!f.once.contains("thanks")){f.once.add("thanks");say(f,a,"Thanks.");}if(dt>=120){f.once.remove("thanks");next(own,4);}}
                else if(dt>=60){go(own,EVENING);cloth(l,b,0);own.putBoolean("EveLamp",lampOn(l,b));own.remove("EveDoorBanged");}
            }
            case EVENING->evening(l,f,p,own,r,a,sam);
            case PHOTO->{
                if(sub==0){if(arrived(a,b,residentMark(own)))next(own,1);if(dt>300)next(own,1);}
                else if(sub==1){if(dt==30){l.sendParticles(p,ParticleTypes.FLASH,true,a==null?b.getX():a.getX(),a==null?b.getY():a.getEyeY(),a==null?b.getZ():a.getZ(),1,0,0,0,0);sound(p,LookoutRegistry.SHUTTER.get(),b,new BlockPos(1,UP+1,-17),1,1);
                        boolean in=inSitting(r)&&a!=null&&inFrame(a,p);own.putString("PhotoIn",in?"in":"out");if(in)SecondPersonStory.turned(own,"You stood in the room while they took its picture.");}
                    if(dt>=60)next(own,2);}
                else{boolean in=own.getString("PhotoIn").equals("in");
                    if(dt==40&&in)say(f,a,"...");
                    if(dt==140){if(in){boolean good=!own.getString("Evening").equals("seen")&&!own.getString("Evening").equals("interfered");own.putString("Photo",good?"in_kept":"in_deleted");say(f,a,good?"I'll keep that one.":"No.");}else{own.putString("Photo","out");say(f,a,"That'll do.");}}
                    if(dt>=200)go(own,CALL);}
            }
            case CALL->{
                if(sub==0){if(dt==60){f.ring=RING;own.putInt("Ringing",2);}unanswered(p,own,2);if(!own.getString("Call2").isEmpty()&&f.ring<0)next(own,1);}
                else{if(dt==1&&own.getString("Call2").equals("left")){if(sam!=null)say(f,a,"Sorry. I have to get this.");}if(dt==40&&own.getString("Call2").equals("left"))say(f,a,SecondPersonStory.caught(own,2));if(dt>=200)go(own,STAY);}
            }
            case STAY->{
                if(own.getString("Evening").equals("seen")||sam==null&&own.getBoolean("SamLeft")){go(own,AUTUMN_DONE);return;}
                if(sub==0){if(dt==40)say(f,sam,"Can I stay? I mean. It's late.");if(dt==41)own.putBoolean("StayLamp",lampOn(l,b));
                    String choice="";if(own.getBoolean("ColdAtStay"))choice="cold";else if(dt>41&&!own.getBoolean("StayLamp")&&lampOn(l,b))choice="warm";
                    if(choice.isEmpty()&&dt>=300)choice="none";
                    if(!choice.isEmpty()&&dt>40){own.putString("StayChoice",choice);
                        String ev=own.getString("Evening");boolean stays=choice.equals("warm")&&!ev.equals("interfered")||!choice.equals("cold")&&(ev.equals("helped")||ev.equals("alone")||ev.equals("none")||ev.isEmpty());
                        own.putBoolean("SamStayed",stays);if(!stays){own.putBoolean("SamLeft",true);own.putInt("SamLeftAt",own.getInt("Clock"));open(l,b,FLAT_DOOR,true);}
                        say(f,a,stays?"...Stay.":"I've got an early start.");if(choice.equals("cold"))SecondPersonStory.turned(own,"You made the flat cold.");if(choice.equals("warm"))SecondPersonStory.turned(own,"You made the flat warm.");next(own,1);}}
                else if(dt>=160){go(own,AUTUMN_DONE);}
            }
            default->{
                if(own.getBoolean("IrisKnocked")&&!own.getBoolean("IrisTold")){
                    if(sub==0&&dt>=100){sound(p,DoorSounds.KNOCK.get(),b,FLAT_DOOR,1,1);open(l,b,FLAT_DOOR,true);open(l,b,IRIS_DOOR,true);next(own,1);}
                    else if(sub==1){if(dt==30)say(f,iris,"You've got company. I don't mind it. I like a bit of company.");if(dt>=130){own.putBoolean("IrisTold",true);open(l,b,FLAT_DOOR,false);open(l,b,IRIS_DOOR,false);next(own,2);}}
                }
                if(dt==20&&sub==0&&!f.once.contains("autumn_done")){f.once.add("autumn_done");cue(p,"The flat goes quiet.");}
            }
        }
    }
    static void evening(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,Vec3 r,@Nullable LiteraryActor a,@Nullable LiteraryActor sam){
        var b=f.base;int sub=sub(own),dt=dt(own);
        if(sub==0){if(dt==20){sound(p,DoorSounds.KNOCK.get(),b,FLAT_DOOR,1,1);open(l,b,FLAT_DOOR,true);}if(dt==60)say(f,sam,"I brought wine. It's not good wine.");if(dt>=120)next(own,1);return;}
        if(sub==1){
            if(dt==140)open(l,b,FLAT_DOOR,false);
            String[] talk={"Sam:This is nice. It's a nice flat.","They:It was cheap. It's quieter than you'd think.","Sam:How long have you been here now?","They:Since the end of the summer. It feels longer.",
                    "Sam:Do you get on with the neighbours?","They:Iris. She feeds me.","Sam:I'd live on stew if someone brought it.","They:You would not."};
            if(dt%150==30&&dt/150<talk.length){var line=talk[dt/150].split(":",2);say(f,line[0].equals("Sam")?sam:a,line[1]);}
            if(own.getString("Evening").isEmpty()){
                String choice="";
                if(!own.getBoolean("EveLamp")&&lampOn(l,b)||cloth(l,b)>=1)choice="helped";
                // 0.4.561 (run 1595): the kitchen door counts when the reader's hand moved it. It used to count whenever it stood otherwise
                // than at the start of the evening, and since 0.4.560 Sam walks in to the table through it rather than appearing in his chair.
                if(own.getBoolean("ColdBySam")||own.getBoolean("KnockedInEvening")||own.getBoolean("EveDoorBanged"))choice="interfered";
                if(r.x<-6.2&&Math.abs(r.z+21.5)<1.6&&r.y>=UP-.5&&r.y<UP+2.5){if(++f.window>=30)choice="seen";}else f.window=0;
                if(inBedroom(r)&&!isOpen(l,b,BED_DOOR)){if(++f.shut>=100)choice="alone";}else f.shut=0;
                if(!choice.isEmpty()){own.putString("Evening",choice);
                    switch(choice){
                        case "helped"->{say(f,sam,"You've made it nice. You didn't have to make it nice.");SecondPersonStory.turned(own,"You helped with the evening.");}
                        case "interfered"->{say(f,sam,"Your flat's haunted.");SecondPersonStory.turned(own,"You made yourself felt in the evening.");}
                        case "seen"->{say(f,sam,"There's someone. In the window. Behind you.");SecondPersonStory.turned(own,"You stood in the window, and Sam saw someone standing there.");}
                        default->SecondPersonStory.turned(own,"You left them alone with each other.");
                    }
                    if(choice.equals("seen")){next(own,2);return;}
                }
            }
            if(dt>=EVENING_LENGTH){if(own.getString("Evening").isEmpty())own.putString("Evening","none");go(own,PHOTO);}
            return;
        }
        // The evening ends at the window.
        if(dt==60)say(f,a,"There's nobody. Sam. There's nobody there.");
        if(dt==140)say(f,sam,"I should go. I'm sorry. I should go.");
        if(dt==150){open(l,b,FLAT_DOOR,true);own.putBoolean("SamLeft",true);own.putInt("SamLeftAt",own.getInt("Clock"));}
        if(dt>=260){open(l,b,FLAT_DOOR,false);go(own,PHOTO);}
    }
    static boolean inFrame(LiteraryActor a,ServerPlayer p){var d=p.getEyePosition().subtract(a.getEyePosition());if(d.lengthSqr()>100)return false;var look=Vec3.directionFromRotation(0,a.getYRot());return look.dot(d.normalize())>Math.cos(Math.toRadians(38));}

    // ------------------------------------------------------------------------------------------------ 0.4.369, winter
    static void winter(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a){
        var b=f.base;int st=step(own),sub=sub(own),dt=dt(own);
        seen(l,f,p,own,a);
        switch(st){
            case W_FUNERAL->{
                // They come back from the funeral with her tin, and read her note at the kitchen table.
                if(sub==0){
                    if(dt==20){open(l,b,FLAT_DOOR,true);sound(p,SoundEvents.WOODEN_DOOR_OPEN,b,FLAT_DOOR,1,.9F);}
                    if(dt>20&&arrived(a,b,residentMark(own))||dt>400){tin(l,b,0);handle(a);sound(p,SoundEvents.ITEM_FRAME_ADD_ITEM,b,TIN_AT,.8F,.8F);say(f,a,"She left me this.");open(l,b,FLAT_DOOR,false);next(own,1);}
                }
                else if(sub==1){
                    if(dt==90)say(f,a,"\"For you, and for your lodger.\"");
                    if(dt==170){handle(a);cue(p,"They turn the note over, as if the rest of it might be on the back.");}
                    if(dt==200)say(f,a,"What lodger?");
                    if(dt>=330)next(own,2);
                }
                else if(sub==2){if(a!=null&&a.isSleeping()||dt>600)next(own,3);}
                else if(dt>=200)morningStart(l,f,own,0);
            }
            case W_MORNING->care(l,f,p,own,a);
            case W_DAY->day(l,f,p,own,a);
            case W_EVENING->{
                if(sub==0){if(arrived(a,b,residentMark(own))||dt>300)next(own,1);return;}
                // Iris's sleeve on the arm of the chair, found.
                if(dt==20&&sleeve(l,b)==0&&state(own,"sleeve").equals("arm")&&!own.getBoolean("SleeveFound")){handle(a);say(f,a,"...Iris's.");f.once.add("found_sleeve");}
                if(dt==120&&f.once.contains("found_sleeve")&&!own.getBoolean("SleeveFound")){say(f,a,"She never finished anything. Look at this.");own.putBoolean("SleeveFound",true);own.putInt("SleeveEvening",own.getInt("Care"));}
                if(dt==200&&own.getBoolean("SleeveFound")&&own.getInt("SleeveEvening")!=own.getInt("Care"))say(f,a,"I've dropped one. Where's it gone.");
                if(dt==320&&own.getInt("Care")==0)say(f,a,"She had the radio on every night. You could hear it through the wall.");
                if(dt>=WINTER_EVENING&&!own.getBoolean("SeenOpen")){
                    if(own.getInt("Care")==0)buildWalk(own);
                    go(own,W_NIGHT);
                }
            }
            case W_NIGHT->winterNight(l,f,p,own,a);
            default->{
                // The last of the winter: the sleeve finished, and, if they never went, the phone they will not answer.
                if(dt==1&&own.getBoolean("SleeveFound")&&!own.getBoolean("SleeveFinished")){own.putBoolean("SleeveFinished",true);sleeve(l,b,1);state(own,"sleeve","finished");}
                if(dt==120&&!own.getBoolean("Went")&&own.getString("Call3").isEmpty()){f.ring=RING;own.putInt("Ringing",3);}
                unanswered(p,own,3);
                if(own.getString("Call3").equals("unanswered")&&f.once.add("watched"))cue(p,"They watch it ring. They do not answer it.");
                if(dt>=360&&f.ring<0&&f.once.add("winter_done"))cue(p,"The flat goes quiet.");
            }
        }
    }
    /** A winter morning begins: in the worst of it, their routine goes into the drawer under the basin at night. */
    static void morningStart(ServerLevel l,Flat f,CompoundTag own,int care){
        var b=f.base;go(own,W_MORNING);own.putInt("Care",care);own.remove("LaidToday");
        String routine=state(own,"routine");
        if(routine.equals("home")||routine.equals("homed")){var at=b.offset(ROUTINE_HOME);if("routine".equals(thingAt(l,at)))l.setBlock(at,Blocks.AIR.defaultBlockState(),3);state(own,"routine","drawer");}
        if(own.getBoolean("LeftDoor")){open(l,b,FLAT_DOOR,false);own.remove("LeftDoor");}
    }
    /** The care scene: three mornings at the basin. Laid out, they go from leaving it, to using it and crying, to "All right." */
    static void care(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a){
        var b=f.base;int sub=sub(own),dt=dt(own),care=own.getInt("Care");String routine=SecondPersonStory.routine(own);
        if(sub==0){
            if(dt==60&&care==1&&own.getString("Sleepwalk").equals("left"))say(f,a,"I woke up at the door. I was standing at the door.");
            if(dt==90&&care==1&&own.getBoolean("DrewAtNight"))cue(p,"On the kitchen table, a face drawn in pencil in the dark. The one you drew.");
            if(dt>40&&arrived(a,b,residentMark(own))||dt>500){mirror(l,b,true);own.putInt("SteamClock",own.getInt("Clock")+700);sound(p,HouseReadRegistry.TAP.get(),b,BASIN,.6F,1.1F);next(own,1);}
            return;
        }
        if(sub==1){
            if(own.getBoolean("SeenOpen"))return;
            boolean laid=own.getBoolean("LaidToday");
            if(!laid&&dt<CARE_WAIT)return;
            if(laid){int n=own.getInt("LaidCount")+1;own.putInt("LaidCount",n);
                switch(n){
                    case 1->{cue(p,"They look at it, and leave it where it is.");SecondPersonStory.turned(own,"You laid their "+routine+" out on a winter morning, and they left it where it was.");}
                    case 2->{sound(p,HouseReadRegistry.TAP.get(),b,BASIN,.8F,1);cue(p,"They use it. Halfway through they are crying, and they keep going.");SecondPersonStory.turned(own,"You laid their "+routine+" out again, and they used it, and cried.");}
                    default->{say(f,a,"All right. All right.");own.putBoolean("AllRight",true);SecondPersonStory.turned(own,"You laid their "+routine+" out a third morning, and they said \"All right.\"");}
                }}
            else cue(p,"They stand at the basin, do nothing, and go back to bed for an hour.");
            own.putString("Care"+care,laid?"laid":"not");next(own,2);
            return;
        }
        // Used, it is used with both hands at the basin for a while.
        if(own.getString("Care"+care).equals("laid")&&own.getInt("LaidCount")>=2&&dt%20==5&&dt<130)handle(a);
        if(own.getBoolean("SeenOpen")||dt<200)return;
        if(care>=2){go(own,WINTER_DONE);return;}
        go(own,W_DAY);
    }
    /** Where the ticket is when the second day begins: by the door, where they cannot miss it; still on the table or out somewhere; or hidden. */
    static String ticketWhere(CompoundTag own){
        String state=state(own,"ticket");var at=placedAt(own,"ticket");
        if(state.equals("placed")&&at!=null&&byTheDoor(at))return "mat";
        if(state.equals("home")||state.equals("homed")||state.equals("placed"))return "left";
        return "hidden";
    }
    static void clearTicket(ServerLevel l,BlockPos b,CompoundTag own){
        for(var at:new BlockPos[]{placedAt(own,"ticket"),TICKET_HOME})if(at!=null&&"ticket".equals(thingAt(l,b.offset(at))))l.setBlock(b.offset(at),Blocks.AIR.defaultBlockState(),3);
        placedAt(own,"ticket",null);
    }
    /** The two winter days: the post nobody opens, then whatever the ticket's place decides. */
    static void day(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a){
        var b=f.base;int sub=sub(own),dt=dt(own);
        if(own.getInt("Care")==0){
            if(sub==0){
                if(dt==20){sound(p,SoundEvents.BOOK_PAGE_TURN,b,MAT,1,.7F);cue(p,"The post comes through the door.");}
                if(dt>30&&arrived(a,b,residentMark(own))||dt>400){home(l,b,own,"ticket");state(own,"ticket","homed");handle(a);sound(p,SoundEvents.BOOK_PUT,b,TICKET_HOME,.8F,1);say(f,a,"Later.");next(own,1);}
            }
            else if(dt>=DAY_LENGTH)go(own,W_EVENING);
            return;
        }
        if(sub==0&&dt==1){own.putString("Ticket",ticketWhere(own));return;}
        switch(own.getString("Ticket")){
            case "mat"->{
                if(sub==0){
                    if(!f.once.contains("found_ticket")&&(arrived(a,b,residentMark(own))||dt>400)){f.once.add("found_ticket");clearTicket(l,b,own);handle(a);state(own,"ticket","taken");say(f,a,"...Oh.");own.putInt("FoundAt",dt);}
                    if(f.once.contains("found_ticket")&&dt-own.getInt("FoundAt")==80)say(f,a,SecondPersonStory.ticketLine(own));
                    if(f.once.contains("found_ticket")&&dt-own.getInt("FoundAt")>=180)next(own,1);
                }
                else if(sub==1){if(dt>20&&arrived(a,b,residentMark(own))||dt>400){open(l,b,FLAT_DOOR,true);next(own,2);}}
                else if(sub==2){if(dt==60){open(l,b,FLAT_DOOR,false);cue(p,"The door shuts behind them. The flat is empty but for you.");}if(dt>=AWAY){open(l,b,FLAT_DOOR,true);sound(p,SoundEvents.WOODEN_DOOR_OPEN,b,FLAT_DOOR,1,1);next(own,3);}}
                else{if(dt==80){open(l,b,FLAT_DOOR,false);say(f,a,SecondPersonStory.backLine(own));own.putBoolean("Went",true);}if(dt>=DAY_LENGTH/2)go(own,W_EVENING);}
            }
            case "left"->{
                if(sub==0){if(dt>1&&arrived(a,b,residentMark(own))||dt>400){clearTicket(l,b,own);state(own,"ticket","pocket");handle(a);say(f,a,"Next week.");cue(p,"They put the ticket in a pocket.");next(own,1);}}
                else if(dt>=DAY_LENGTH)go(own,W_EVENING);
            }
            default->{if(dt>=DAY_LENGTH)go(own,W_EVENING);}
        }
    }
    /** The quiet rooms this reader went through, then their own habit in the flat if they had one, then the latch. */
    static void buildWalk(CompoundTag own){
        var out=new net.minecraft.nbt.ListTag();var snap=own.getCompound("Snap");
        for(var key:SecondPersonStory.list(snap,"Walk")){if(key.equals("porch"))continue;if(key.equals("kitchen")&&!snap.contains("Drawing"))continue;out.add(net.minecraft.nbt.StringTag.valueOf(key));}
        String habit="";int best=1;for(var h:List.of("lamp","tap","glass","knock")){int n=own.getInt("Habit_"+h);if(n>best){best=n;habit=h;}}
        if(!habit.isEmpty())out.add(net.minecraft.nbt.StringTag.valueOf(habit));
        out.add(net.minecraft.nbt.StringTag.valueOf("latch"));own.put("WalkList",out);
    }
    static int holdFor(String station){return switch(station){case "kitchen","shelter"->140;case "waiting","bathroom"->120;case "coatroom"->80;case "latch"->240;default->60;};}
    /** The winter nights: the first, they get up asleep and walk; the second, they sleep. */
    static void winterNight(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a){
        var b=f.base;int sub=sub(own),dt=dt(own),care=own.getInt("Care");
        if(sub==0){if(a!=null&&a.isSleeping()||dt>600)next(own,1);return;}
        if(care!=0){if(dt>=300)morningStart(l,f,own,care+1);return;}
        if(sub==1){if(dt>=120){own.putBoolean("Walking",true);next(own,2);cue(p,"They get up. Their eyes are open, and they are asleep.");}return;}
        if(sub>=100){if(a!=null&&a.isSleeping()&&dt>=200||dt>900)morningStart(l,f,own,1);return;}
        var walk=SecondPersonStory.list(own,"WalkList");int i=(sub-2)/2;String station=i<walk.size()?walk.get(i):"";
        // Brought back: the lamp, a knock, a hand.
        String by=own.getString("BroughtBy");
        if(!by.isEmpty()){own.putString("Sleepwalk","brought");own.putString("BroughtAt",station);own.remove("Walking");say(f,a,"...How did I get here?");
            SecondPersonStory.turned(own,switch(by){case "lamp"->"You turned the lamp on while they walked asleep, and they woke.";case "knock"->"You knocked while they walked asleep, and they woke.";default->"You put a hand on them while they walked asleep, and they woke.";});
            endWalk(l,b,own);next(own,100);return;}
        if(i>=walk.size()){own.remove("Walking");endWalk(l,b,own);next(own,100);return;}
        if(sub%2==0){if(arrived(a,b,residentMark(own))||dt>300)next(own,sub+1);return;}
        switch(station){
            case "kitchen"->{if(dt%30==10)sound(p,SoundEvents.BOOK_PAGE_TURN,b,TABLE,.4F,1.6F);}
            case "waiting"->{if(dt==40)say(f,a,"Is it me next?");}
            case "bathroom"->{if(dt==10||dt==70)sound(p,HouseReadRegistry.TAP.get(),b,BASIN,.4F,1);if(dt==10){String r=state(own,"routine");if(!(r.equals("home")||r.equals("homed")))own.putBoolean("WalkEmptyHands",true);}}
            case "coatroom"->{if(dt==10&&coatHung(l,b)){handle(a);coat(l,b,"",false);own.putBoolean("WoreCoat",true);}}
            case "shelter"->{if(dt==40)say(f,a,"It'll stop in a minute.");}
            case "lamp"->{if(dt==10){handle(a);lamp(l,b,true);sound(p,SoundEvents.LEVER_CLICK,b,LAMP,.6F,1.2F);}}
            case "tap"->{if(dt==10)sound(p,HouseReadRegistry.TAP.get(),b,TAP,1,1);}
            case "glass"->{if(dt==10&&glass(l,b)>=0){handle(a);glass(l,b,glass(l,b)==0?1:0);sound(p,SoundEvents.ITEM_FRAME_ROTATE_ITEM,b,GLASS,1,1);}}
            case "knock"->{if(dt==10||dt==25||dt==40){handle(a);}if(dt==10||dt==25||dt==40)sound(p,DoorSounds.KNOCK.get(),b,new BlockPos(-2,UP+1,-30),.8F,1);}
            default->{
                if(dt==60){handle(a);open(l,b,FLAT_DOOR,true);sound(p,SoundEvents.WOODEN_DOOR_OPEN,b,FLAT_DOOR,.8F,1);say(f,a,SecondPersonStory.haven(own)?"Is that you? I've left it open for you.":"...");}
            }
        }
        if(dt<holdFor(station))return;
        // The station done: what it leaves behind, and one more of the reader's acts walked through.
        if(station.equals("kitchen")){SecondPersonSite.thing(l,b,CLOTH,"drawing",0);own.putBoolean("DrewAtNight",true);}
        if(station.equals("latch")){own.putString("Sleepwalk","left");own.putBoolean("LeftDoor",true);say(f,a,"...What—");cue(p,"They wake in the open doorway, cold, and do not know how they got there.");SecondPersonStory.turned(own,"You let them walk asleep, all the way to the front door.");}
        own.putInt("WalkDone",i+1);next(own,sub+1);
    }
    static void endWalk(ServerLevel l,BlockPos b,CompoundTag own){if(own.getBoolean("WoreCoat")&&!coatHung(l,b))coat(l,b,own.getCompound("Snap").getString("Coat"),true);}

    // ------------------------------------------------------------------------------------------------ 0.4.369, being seen
    /**
     * Once this winter they see the reader: in the bathroom mirror, which faces the room, on a morning at the basin; failing that, in the
     * dark television from the armchair of an evening. The reader is seen when they stand behind them where the glass would show them.
     */
    static void seen(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a){
        if(a==null||a.isSleeping()||own.getBoolean("Walking"))return;
        int st=step(own);var b=f.base;
        if(!own.getBoolean("SeenOpen")){
            String surface=st==W_MORNING&&sub(own)>=1?"mirror":st==W_EVENING&&sub(own)>=1?"tv":"";
            if(own.contains("Seen")||surface.isEmpty()){f.seenRun=0;return;}
            if(!reflected(b,surface,a,p)){f.seenRun=0;return;}
            if(++f.seenRun<20)return;
            own.putBoolean("SeenOpen",true);own.putString("SeenIn",surface);own.putInt("SeenClock",own.getInt("Clock"));own.remove("SeenHow");
            say(f,a,"...That's my face.");f.stillAt=p.position();f.still=0;f.seenAway=0;
            return;
        }
        String how=own.getString("SeenHow"),surface=own.getString("SeenIn");
        if(how.isEmpty()){
            boolean in=reflected(b,surface,a,p);
            if(!in){if(++f.seenAway>=20)how="gone";}else f.seenAway=0;
            if(f.stillAt!=null&&p.position().distanceToSqr(f.stillAt)<.04){if(++f.still>=SEEN_STILL&&how.isEmpty())how="still";}else{f.stillAt=p.position();f.still=0;}
            if(how.isEmpty()&&own.getInt("Clock")-own.getInt("SeenClock")>=SEEN_OPEN)how=in?"still":"gone";
        }
        if(!how.isEmpty())resolveSeen(f,p,own,a,how);
    }
    /** The reader's eye behind theirs, where the glass in front of them would show it. */
    static boolean reflected(BlockPos b,String surface,LiteraryActor a,ServerPlayer p){
        boolean mirror=surface.equals("mirror");
        var s=mirror?abs(b,at(MIRROR.getX()+.5,MIRROR.getY()+.5,MIRROR.getZ()+.25)):abs(b,at(TV.getX()+.75,TV.getY()+.55,TV.getZ()+.5));
        var n=mirror?new Vec3(0,0,1):new Vec3(1,0,0);
        // Only from inside the same room as the glass; a seated eye is lower than a standing one.
        var pr=rel(p.position(),b);if(mirror?!inBathroom(pr):!inSitting(pr))return false;
        var r=a.gesture()==G_SIT?a.getEyePosition().subtract(0,.6,0):a.getEyePosition();var e=p.getEyePosition();
        double dr=r.subtract(s).dot(n),de=e.subtract(s).dot(n);
        if(dr<.2||de<dr+.3||de>7)return false;
        if(Vec3.directionFromRotation(0,a.getYRot()).dot(n)>-.3)return false;
        double k=dr/(dr+de);var latR=r.subtract(s).subtract(n.scale(dr));var latE=e.subtract(s).subtract(n.scale(de));
        var hit=latR.add(latE.subtract(latR).scale(k));
        return Math.abs(hit.y)<.9&&Math.sqrt(hit.x*hit.x+hit.z*hit.z)<.7;
    }
    static void resolveSeen(Flat f,ServerPlayer p,CompoundTag own,LiteraryActor a,String how){
        own.remove("SeenOpen");own.remove("SeenHow");
        String glass=own.getString("SeenIn").equals("mirror")?"the bathroom mirror":"the dark television";
        switch(how.split(":",2)[0]){
            case "still"->{say(f,a,"Are you me?");own.putString("Seen","still");own.putInt("AskedClock",own.getInt("Clock"));SecondPersonStory.turned(own,"You stayed still in "+glass+" while they looked at you, and they asked if you were them.");}
            case "gone"->{say(f,a,"...Gone.");cue(p,"In the glass there is only the room.");own.putString("Seen","gone");SecondPersonStory.turned(own,"You stepped out of "+glass+" while they looked at you.");}
            case "touch"->{
                boolean kind=SecondPersonStory.gentle(own)>=SecondPersonStory.fright(own);own.putString("Seen",kind?"touch_kind":"touch_afraid");
                if(kind){say(f,a,"It's you. It's been you.");SecondPersonStory.turned(own,"You touched them while they looked at you in "+glass+", and they said it had been you all along.");}
                else{say(f,a,"Don't. Don't touch me.");cue(p,"They back away from the glass until the wall stops them.");SecondPersonStory.turned(own,"You touched them while they looked at you in "+glass+", and they backed away to the wall.");}
            }
            default->{String w=how.length()>5?how.substring(5):"";own.putString("Seen","word");own.putString("SeenWord",w);say(f,a,SecondPersonStory.seenWord(w));SecondPersonStory.turned(own,"While they looked at you in "+glass+", you wrote "+w+" on the glass.");}
        }
    }

    static void morning(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,Vec3 r,@Nullable LiteraryActor a){
        var b=f.base;int st=step(own),sub=sub(own),dt=dt(own);
        boolean stays=own.getString("Decision").equals("stay");
        if(st==LATCH){
            if(t(own)==120)cue(p,"The front door is on the latch.");
            if(t(own)==80&&!SecondPersonStory.list(own,"HousePages").isEmpty())say(f,a,SecondPersonStory.houseSays(own));
            if(onStreet(r)){go(own,OUT);if(stays)lamp(l,b,true);}
        }
        else if(st==OUT){
            if(stays){if(dt==80)cue(p,"There is a light in the window, and they are standing at it.");if(dt>=200)go(own,ON_BENCH);}
            else{
                if(sub==0&&dt==1)open(l,b,STREET_DOOR,true);
                if(sub<3&&arrived(a,b,residentMark(own))&&dt>20){if(sub==1&&dt<80)return;next(own,sub+1);}
                if(sub==3||dt>600){go(own,ON_BENCH);}
            }
        }
        else if(st==ON_BENCH){
            if(t(own)==1){benchBook(l,b,true);LiteraryVignettes.ready(p,PLACE,own,"the_door_on_the_latch");cue(p,"On the bench at the bus stop, where nobody waits, there is a book.");}
        }
    }

    // ------------------------------------------------------------------------------------------------ the cupboard
    /** Going into the airing cupboard and shutting the door lets the season go by, once the season has done what it does. */
    static void cupboard(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,Vec3 r){
        if(!inCupboard(r)||isOpen(l,f.base,CUPBOARD_DOOR)){f.unseen=0;return;}
        if(++f.unseen==20)sound(p,SoundEvents.COPPER_BULB_TURN_ON,f.base,TANK,.4F,.6F);
        if(f.unseen<60)return;
        int s=seasonOf(own),st=step(own);
        boolean done=s==SUMMER&&st==SUMMER_DONE||s==AUTUMN&&st==AUTUMN_DONE&&(!own.getBoolean("IrisKnocked")||own.getBoolean("IrisTold"))
            ||s==WINTER&&st==WINTER_DONE&&f.ring<0&&(own.getBoolean("Went")||!own.getString("Call3").isEmpty());
        if(!done){if(f.unseen==60)cue(p,"It is warm in here, and the tank ticks. Not yet.");return;}
        turn(l,f,p,own);f.unseen=0;
    }
    /** The season goes by. */
    public static void turn(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own){
        var b=f.base;int s=seasonOf(own);
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,80,0,false,false));
        clearWords(l,f);mirror(l,b,false);
        if(s==SUMMER){own.putInt("Season",AUTUMN);go(own,IRIS_VISIT);season(l,b,1);
            // What was never found stays lost; what the reader put where it lives stays where they put it.
            cue(p,SecondPersonStory.first(SecondPersonStory.whileAway(own,1)));SecondPersonStory.turned(own,"You let the summer go by in the cupboard.");}
        else if(s==AUTUMN){own.putInt("Season",WINTER);go(own,W_FUNERAL);season(l,b,2);lamp(l,b,false);cloth(l,b,-1);
            // They went to the funeral in a borrowed coat if theirs was lost; looking for their good shoes afterwards, they found what was under the bed.
            String coat=state(own,"coat");own.putBoolean("BorrowedCoat",!(coat.equals("homed")||coat.equals("home")||coat.equals("placed")));
            for(var th:THINGS)if(state(own,th).equals("hidden")&&thing(own,th).getString("Where").equals("under the bed")){state(own,th,"homed");home(l,b,own,th);SecondPersonStory.add(own,"Returned",th);}
            cue(p,SecondPersonStory.first(SecondPersonStory.whileAway(own,2)));SecondPersonStory.turned(own,"You let the autumn go by in the cupboard.");}
        else{own.putInt("Season",MORNING);go(own,LATCH);season(l,b,3);lamp(l,b,false);
            // While the reader was in the cupboard, the House carried on in their name.
            SecondPersonStory.housePages(own);
            own.putString("Decision",SecondPersonStory.stays(own)?"stay":"go");open(l,b,FLAT_DOOR,true);
            cue(p,SecondPersonStory.first(SecondPersonStory.whileAway(own,3)));SecondPersonStory.turned(own,"You let the rest of the winter go by in the cupboard.");}
        f.once.clear();f.ring=-1;own.remove("Ringing");own.remove("Walking");own.remove("SeenOpen");
        for(var id:new UUID[]{f.resident,f.iris,f.sam}){var a=actor(l,id);if(a!=null){if(a.isSleeping())a.stopSleeping();a.discard();}}
        f.resident=null;f.iris=null;f.sam=null;
    }

    // ------------------------------------------------------------------------------------------------ the phone
    static void ringing(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a){
        if(f.ring<0)return;int call=own.getInt("Ringing");if(call==0){f.ring=-1;return;}
        if(f.ring%40==0)sound(p,WorkPrintRegistry.RING.get(),f.base,PHONE,1,1);
        if(--f.ring<0){
            // Nobody lifted it: they catch it themselves.
            String key="Call"+call;if(own.getString(key).isEmpty()){own.putString(key,call==3?"unanswered":"left");SecondPersonStory.turned(own,call==1?"You let the phone ring in the evening.":call==2?"You let the phone ring again.":"You let the phone ring at the end of the winter, and so did they.");}
            own.remove("Ringing");
        }
    }
    /** The reader lifts the receiver. The voice goes on in the conversation panel; the reader can only put it down, leave it off, or listen. */
    static void lift(ServerPlayer p,BlockPos b,CompoundTag own){
        var f=flat(b);if(f==null)return;int call=own.getInt("Ringing");if(call==0)return;f.ring=-1;own.remove("Ringing");own.putInt("LiftedAt",t(own));own.putInt("LiftedCall",call);
        sound(p,PhoneSounds.PICKUP.get(),b,PHONE,1,1);
        Talk.begin(p,"sp_call_"+call,SecondPersonStory.caller(own,call));
        for(var line:SecondPersonStory.call(own,call))Talk.line(p,SecondPersonStory.caller(own,call),line);
        Talk.ask(p,Talk.act("Put the receiver down.",q->answer(q,b,call,"put_down")),Talk.act("Leave it off the hook.",q->answer(q,b,call,"off")),Talk.act("Listen.",q->answer(q,b,call,"lifted")));
    }
    /** A receiver lifted and the panel stepped away from: after a while it counts as having listened, and is put back. */
    static void unanswered(ServerPlayer p,CompoundTag own,int call){
        if(own.getInt("LiftedCall")!=call||!own.getString("Call"+call).isEmpty()||t(own)-own.getInt("LiftedAt")<600||Talk.waiting(p,"sp_call_"+call))return;
        own.putString("Call"+call,"lifted");SecondPersonStory.turned(own,"You lifted the phone and listened, and said nothing.");
    }
    static void answer(ServerPlayer p,BlockPos b,int call,String how){
        var own=own(p);String key="Call"+call;if(!own.getString(key).isEmpty()){Talk.close(p);return;}own.putString(key,how);
        var l=p.serverLevel();
        switch(how){
            case "put_down"->{phone(l,b,false);Talk.close(p);SecondPersonStory.turned(own,"You lifted the phone and put it down again.");}
            case "off"->{phone(l,b,true);Talk.close(p);SecondPersonStory.turned(own,"You left the phone off the hook.");}
            default->{for(var line:SecondPersonStory.listened(own,call))Talk.line(p,SecondPersonStory.caller(own,call),line);Talk.line(p,"","The line clicks, and purrs.");Talk.end(p);phone(l,b,false);SecondPersonStory.turned(own,"You lifted the phone and listened, and said nothing.");}
        }
        save(p,own);
    }

    // ------------------------------------------------------------------------------------------------ cold, and touch
    /** Standing close enough to someone makes the air go cold round them. */
    static void cold(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a,@Nullable LiteraryActor iris,@Nullable LiteraryActor sam){
        LiteraryActor close=null;for(var x:new LiteraryActor[]{sam,a,iris})if(x!=null&&!x.isSleeping()&&x.distanceToSqr(p)<1.8){close=x;break;}
        if(close==null){f.near=0;return;}
        if(++f.near%10==0)l.sendParticles(p,ParticleTypes.SNOWFLAKE,true,close.getX(),close.getY()+1.2,close.getZ(),3,.25,.4,.25,0);
        if(f.near==60){
            boolean isSam=close==sam,isIris=close==iris;
            say(f,close,isSam?"Is it cold in here, or is it me?":isIris?"There you are.":"Somebody walked over my grave.");
            if(isSam&&seasonOf(own)==AUTUMN&&step(own)==EVENING)own.putBoolean("ColdBySam",true);
            if(seasonOf(own)==AUTUMN&&step(own)==STAY)own.putBoolean("ColdAtStay",true);
            SecondPersonStory.add(own,"Cold",isSam?"Sam":isIris?"Iris":"them");
        }
    }
    /** Hitting or touching someone in the flat: a flinch, never harm. */
    public static void touched(ServerPlayer p,LiteraryActor a){
        if(LiteraryVignettes.current(p)!=PLACE)return;var b=LiteraryVignettes.base(p,PLACE);var f=b==null?null:flat(b);if(f==null)return;var own=own(p);
        if(a.role()==LiteraryActor.SP_RESIDENT){
            // 0.4.369: a hand while they look at the reader in the glass, or while they walk asleep, is its own answer.
            if(own.getBoolean("SeenOpen")){own.putString("SeenHow","touch");save(p,own);return;}
            if(own.getBoolean("Walking")){if(own.getString("BroughtBy").isEmpty())own.putString("BroughtBy","hand");save(p,own);return;}
            say(f,a,a.isSleeping()?"Mm.":own.contains("Seen")?"...Is that you?":"...Who's there?");SecondPersonStory.add(own,"Touched","them");}
        else if(a.phase()==IRIS){say(f,a,"Hello, you.");own.putBoolean("IrisMet",true);}
        else if(a.phase()==SAM){say(f,a,"Did you feel that?");if(seasonOf(own)==AUTUMN&&step(own)==EVENING)own.putBoolean("ColdBySam",true);}
        p.serverLevel().sendParticles(p,ParticleTypes.SNOWFLAKE,true,a.getX(),a.getY()+1.2,a.getZ(),6,.25,.4,.25,0);
        save(p,own);
    }
    public static void talk(ServerPlayer p,LiteraryActor a){touched(p,a);}

    // ------------------------------------------------------------------------------------------------ glass
    /** Standing where the hall glass faces them, when they pass it. */
    static void glassGlimpse(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a){
        if(a==null||own.getBoolean("GlassSeen")||glass(l,f.base)!=0)return;var r=rel(p.position(),f.base);var ar=rel(a.position(),f.base);
        boolean readerIn=r.x>=1&&r.x<3&&r.z>=-24.6&&r.z<-21.4&&r.y>=UP-.5&&r.y<UP+2.5,residentPassing=ar.x>=1&&ar.x<3&&Math.abs(ar.z+22.5)<1.3;
        if(readerIn&&residentPassing){own.putBoolean("GlassSeen",true);say(f,a,"...");SecondPersonStory.turned(own,"You stood where the hall glass faced you, and they stopped and looked into it.");f.once.add("glassline");}
    }
    static final List<Integer> WINDOW_YS=List.of(UP+1,UP+2);
    static boolean streetPane(BlockPos rel){return rel.getZ()==-12&&rel.getX()>=WINDOW_X0&&rel.getX()<=WINDOW_X1&&WINDOW_YS.contains(rel.getY());}
    static boolean mirrorAt(BlockPos rel){return rel.equals(MIRROR);}
    /** Breath on cold or steamed glass holds one word. */
    static boolean write(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();boolean mirror=mirrorAt(rel);
        boolean ready=mirror?steamed(l,b):dark(own)||seasonOf(own)>=WINTER;
        if(!ready){cue(p,mirror?"The glass is dry. Nothing would hold.":"The glass is too warm to hold a breath.");return true;}
        String surface=mirror?"mirror":"window";if(own.contains("Word_"+surface)){cue(p,"There is a word on it already.");return true;}
        Talk.begin(p,"sp_word","Breath on the glass");Talk.line(p,"","You lean close and breathe, and the glass clouds. One word.");
        var replies=new ArrayList<Talk.Reply>();for(var w:SecondPersonStory.words(own))replies.add(Talk.act(w,q->word(q,b,surface,w)));replies.add(Talk.act("Nothing.",Talk::close));
        Talk.ask(p,replies);return true;
    }
    static void word(ServerPlayer p,BlockPos b,String surface,String word){
        var own=own(p);var f=flat(b);if(f==null||own.contains("Word_"+surface)){Talk.close(p);return;}Talk.close(p);var l=p.serverLevel();
        own.putString("Word_"+surface,word);SecondPersonStory.add(own,"Words",word);SecondPersonStory.turned(own,"You wrote "+word+" on the "+(surface.equals("mirror")?"bathroom mirror":"street window")+".");
        if(seasonOf(own)==MORNING&&own.getString("LastAct").isEmpty())own.putString("LastAct","word");
        // 0.4.369: a word written while they look at the reader in the glass, or as an answer to "Are you me?", is read at once.
        var res=actor(l,f.resident);
        if(own.getBoolean("SeenOpen")&&own.getString("SeenHow").isEmpty()){own.putString("SeenHow","word:"+word);own.putBoolean("WordSeen_"+surface,true);own.putInt("Wipe_"+surface,own.getInt("Clock")+200);}
        else if(own.getString("Seen").equals("still")&&own.getString("SeenAnswer").isEmpty()&&(word.equals("YES")||word.equals("NO"))&&own.getInt("Clock")-own.getInt("AskedClock")<400){
            own.putString("SeenAnswer",word);own.putBoolean("WordSeen_"+surface,true);own.putInt("Wipe_"+surface,own.getInt("Clock")+200);
            say(f,res,word.equals("YES")?"...Yes.":"Then who are you?");SecondPersonStory.turned(own,"They asked if you were them. You wrote "+word+".");}
        var display=EntityType.TEXT_DISPLAY.create(l);
        if(display!=null){var tag=new CompoundTag();display.saveWithoutId(tag);tag.putString("text",Component.Serializer.toJson(Component.literal(word),p.registryAccess()));tag.putString("billboard","fixed");tag.putInt("background",0);tag.putInt("text_opacity",120);
            display.load(tag);boolean mirror=surface.equals("mirror");
            if(mirror)display.moveTo(b.getX()+MIRROR.getX()+.5,b.getY()+MIRROR.getY()+.35,b.getZ()+MIRROR.getZ()+.27,0,0);
            else display.moveTo(b.getX()+(WINDOW_X0+WINDOW_X1)/2.0+.5,b.getY()+UP+1.4,b.getZ()-12+.44,180,0);
            display.addTag(TAG);display.addTag("SpWord_"+surface);l.addFreshEntity(display);}
        save(p,own);
    }
    /** When they see a word, they say so in their own way, and wipe it. */
    static void words(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a){
        if(a==null||a.isSleeping())return;var b=f.base;
        for(var surface:List.of("mirror","window")){
            if(!own.contains("Word_"+surface)||own.getBoolean("WordSeen_"+surface)||own.getBoolean("Walking"))continue;
            var target=surface.equals("mirror")?Vec3.atCenterOf(b.offset(MIRROR)):abs(b,at(-3,UP+1.5,-12.5));
            if(a.distanceToSqr(target)>25||!a.hasLineOfSight(p)&&a.getEyePosition().distanceToSqr(target)>9)continue;
            String w=own.getString("Word_"+surface);own.putBoolean("WordSeen_"+surface,true);SecondPersonStory.add(own,"WordsSeen",w);
            say(f,a,SecondPersonStory.sawWord(w));own.putInt("Wipe_"+surface,own.getInt("Clock")+120);
        }
        for(var surface:List.of("mirror","window")){int at=own.getInt("Wipe_"+surface);if(at>0&&own.getInt("Clock")>=at){own.remove("Wipe_"+surface);own.remove("Word_"+surface);own.remove("WordSeen_"+surface);
            for(var d:l.getEntitiesOfClass(Display.TextDisplay.class,bounds(b),d->d.getTags().contains("SpWord_"+surface)))d.discard();if(surface.equals("mirror"))mirror(l,b,false);}}
        if(own.getInt("SteamUntil")>0&&t(own)>=own.getInt("SteamUntil")&&seasonOf(own)==SUMMER){own.remove("SteamUntil");if(!own.contains("Word_mirror"))mirror(l,b,false);}
        if(own.getInt("SteamClock")>0&&own.getInt("Clock")>=own.getInt("SteamClock")&&!own.getBoolean("SeenOpen")){own.remove("SteamClock");if(!own.contains("Word_mirror"))mirror(l,b,false);}
    }
    static void clearWords(ServerLevel l,Flat f){for(var d:l.getEntitiesOfClass(Display.TextDisplay.class,bounds(f.base),d->d.getTags().contains(TAG)))d.discard();}

    // ------------------------------------------------------------------------------------------------ the things
    static void found(CompoundTag own,String thing,String how){SecondPersonStory.add(own,"Found",thing+":"+how);}
    static BlockPos homeAt(String thing){return switch(thing){case "photo"->PHOTO_HOME;case "drawing"->FRIDGE.above();case "coat"->HOOK;case "ticket"->TICKET_HOME;case "sleeve"->ARM;default->ROUTINE_HOME;};}
    /** The resident puts a thing where it lives. */
    static void home(ServerLevel l,BlockPos b,CompoundTag own,String thing){
        var snap=own.getCompound("Snap");
        switch(thing){case "drawing"->picture(l,b,snap.getCompound("Drawing"),true);case "coat"->coat(l,b,snap.getString("Coat"),true);
            default->SecondPersonSite.thing(l,b,homeAt(thing),thing,snap.getInt("Routine"));}
    }
    static int model(CompoundTag own,String thing){return switch(thing){case "coat"->SecondPersonRegistry.COAT_MODEL;case "drawing"->SecondPersonRegistry.SHEET_MODEL;case "photo"->SecondPersonRegistry.PHOTO_MODEL;case "ticket"->SecondPersonRegistry.TICKET_MODEL;case "sleeve"->SecondPersonRegistry.SLEEVE_MODEL;default->1+Math.floorMod(own.getCompound("Snap").getInt("Routine")<0?11:own.getCompound("Snap").getInt("Routine"),16);};}
    static String name(CompoundTag own,String thing){var s=SecondPersonStory.thing(own,thing);if(s.startsWith("the "))s=s.substring(4);return Character.toUpperCase(s.charAt(0))+s.substring(1);}
    static ItemStack carried(ServerPlayer p,CompoundTag own,String thing){
        var s=new ItemStack(SecondPersonRegistry.CARRIED.get());s.set(DataComponents.CUSTOM_NAME,Component.literal(name(own,thing)));s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(model(own,thing)));
        var tag=new CompoundTag();tag.putString(TAG,thing);tag.putUUID("Owner",p.getUUID());s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return s;
    }
    /** What of theirs the reader is carrying, if anything. */
    public static String held(ServerPlayer p){for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);var c=s.get(DataComponents.CUSTOM_DATA);if(s.is(SecondPersonRegistry.CARRIED.get())&&c!=null&&c.copyTag().contains(TAG))return c.copyTag().getString(TAG);}return "";}
    static void take(ServerPlayer p){for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);var c=s.get(DataComponents.CUSTOM_DATA);if(s.is(SecondPersonRegistry.CARRIED.get())&&c!=null&&c.copyTag().contains(TAG))p.getInventory().setItem(i,ItemStack.EMPTY);}}
    static boolean pickUp(ServerPlayer p,CompoundTag own,String thing){
        if(!held(p).isEmpty()){cue(p,"Your hands are full.");return true;}
        var stack=carried(p,own,thing);if(!p.getInventory().add(stack)){cue(p,"You cannot carry it like this.");return true;}
        state(own,thing,"held");placedAt(own,thing,null);return true;
    }
    /** The reader sets a thing down: put away where it lives, somewhere else, or hidden where they will not look. */
    static void setDown(ServerPlayer p,BlockPos b,CompoundTag own,String thing,String how,@Nullable BlockPos at){
        take(p);var l=p.serverLevel();
        switch(how){
            case "arm"->{state(own,thing,"arm");placedAt(own,thing,ARM);sleeve(l,b,0);own.putBoolean("SleeveOut",true);SecondPersonStory.turned(own,"You laid Iris's half-finished sleeve on the arm of the armchair.");}
            case "tin"->{state(own,thing,"tin");placedAt(own,thing,null);tin(l,b,1);SecondPersonStory.turned(own,"You put Iris's sleeve back in her tin.");}
            case "home"->{state(own,thing,"home");placedAt(own,thing,null);home(l,b,own,thing);SecondPersonStory.add(own,"PutAway",thing);
                SecondPersonStory.turned(own,thing.equals("ticket")?"You put the train ticket back on the hall table.":"You put "+SecondPersonStory.thing(own,thing)+" where it belongs.");
                if(thing.equals("routine"))own.putString("RoutineWhere","on the bathroom shelf, where you put it");}
            case "bed"->{state(own,thing,"hidden");thing(own,thing).putString("Where","under the bed");SecondPersonStory.add(own,"Hidden",thing);SecondPersonStory.turned(own,"You pushed "+SecondPersonStory.thing(own,thing)+" under the bed.");}
            case "cupboard"->{state(own,thing,"hidden");placedAt(own,thing,at);SecondPersonSite.thing(l,b,at,thing,own.getCompound("Snap").getInt("Routine"));SecondPersonStory.add(own,"Hidden",thing);SecondPersonStory.turned(own,"You hid "+SecondPersonStory.thing(own,thing)+" in the airing cupboard.");}
            default->{state(own,thing,"placed");placedAt(own,thing,at);SecondPersonSite.thing(l,b,at,thing,own.getCompound("Snap").getInt("Routine"));SecondPersonStory.add(own,"PlacedThings",thing);
                SecondPersonStory.turned(own,thing.equals("ticket")&&at!=null&&byTheDoor(at)?"You put the train ticket on the mat by the door, where it could not be missed.":"You put "+SecondPersonStory.thing(own,thing)+" somewhere else.");
                if(thing.equals("routine"))own.putString("RoutineWhere",where(at));}
        }
        // 0.4.369: their routine set out in the bathroom on a winter morning, before they give up on it.
        if(seasonOf(own)==WINTER&&step(own)==W_MORNING&&sub(own)<=1&&thing.equals("routine")&&(how.equals("home")||how.equals("placed")&&at!=null&&inBathroom(Vec3.atCenterOf(at))))own.putBoolean("LaidToday",true);
        sound(p,SoundEvents.ITEM_FRAME_ADD_ITEM,b,at==null?homeAt(thing):at,.7F,1);
        if(seasonOf(own)==MORNING&&thing.equals("routine")&&own.getString("LastAct").isEmpty()&&!how.equals("bed")&&!how.equals("cupboard"))own.putString("LastAct","routine");
    }
    static String where(BlockPos at){var v=Vec3.atCenterOf(at);return inKitchen(v)?"in the kitchen, where you put it":inSitting(v)?"in the sitting room, where you put it":inBedroom(v)?"in the bedroom, where you put it":inBathroom(v)?"in the bathroom, where you put it":"in the hall, where you put it";}

    // ------------------------------------------------------------------------------------------------ what the reader does
    /** A right-click in the flat (the book on the shelf and the bench book are LiteraryVignettes'). */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own,@Nullable Direction face){
        var l=p.serverLevel();var s=l.getBlockState(b.offset(rel));var v=Vec3.atCenterOf(rel);var f=flat(b);if(f==null)return false;
        String holding=held(p);
        // Carrying something of theirs: set it down.
        if(!holding.isEmpty()&&p.getMainHandItem().is(SecondPersonRegistry.CARRIED.get())){
            if(holding.equals("sleeve")&&rel.equals(ARMCHAIR)){setDown(p,b,own,holding,"arm",null);return true;}
            if(holding.equals("sleeve")&&rel.equals(TIN_AT)&&tin(l,b)==2){setDown(p,b,own,holding,"tin",null);return true;}
            if(s.getBlock() instanceof BedBlock){setDown(p,b,own,holding,"bed",null);return true;}
            if(holding.equals("drawing")&&(rel.equals(FRIDGE)||rel.equals(FRIDGE.above()))){setDown(p,b,own,holding,"home",null);return true;}
            if(holding.equals("coat")&&rel.equals(HOOK)){setDown(p,b,own,holding,"home",null);return true;}
            if(face==Direction.UP){var cell=rel.above();var cv=Vec3.atCenterOf(cell);
                if((inFlat(cv)||inCupboard(cv))&&l.getBlockState(b.offset(cell)).isAir()){
                    boolean homeCell=cell.equals(homeAt(holding))&&!holding.equals("drawing")&&!holding.equals("coat");
                    setDown(p,b,own,holding,homeCell?"home":inCupboard(cv)?"cupboard":"placed",cell);return true;}
            }
            cue(p,"Not there.");return true;
        }
        // 0.4.561: the reader's own hand on the kitchen door during the evening bangs it; the door then opens or shuts as any door does.
        if(s.getBlock() instanceof DoorBlock&&(rel.equals(KITCHEN_DOOR)||rel.equals(KITCHEN_DOOR.above()))&&seasonOf(own)==AUTUMN&&step(own)==EVENING)own.putBoolean("EveDoorBanged",true);
        // A box with something of theirs still in it.
        for(var th:THINGS)if(rel.equals(boxAt(th))&&SecondPersonBlock.is(s,SecondPersonBlock.Kind.BOX)){
            int stage=s.getValue(SecondPersonBlock.STAGE);
            if(stage==0){box(l,b,rel,1);sound(p,SoundEvents.WOOL_BREAK,b,rel,1,1.4F);return true;}
            if(stage==1&&state(own,th).equals("box")&&(seasonOf(own)==SUMMER&&step(own)<=UNPACK)){box(l,b,rel,2);pickUp(p,own,th);return true;}
            cue(p,"Empty.");return true;
        }
        // One of their things where it was set down (by the reader, or by them).
        var standing=thingAt(l,b.offset(rel));
        if(standing!=null){
            for(var th:CARRYABLE)if(th.equals(standing)&&(rel.equals(placedAt(own,th))||rel.equals(homeAt(th))&&(state(own,th).equals("homed")||state(own,th).equals("home")))){
                if(!holding.isEmpty()){cue(p,"Your hands are full.");return true;}
                l.setBlock(b.offset(rel),Blocks.AIR.defaultBlockState(),3);pickUp(p,own,th);return true;}
        }
        if(rel.equals(HOOK)&&coatHung(l,b)&&(state(own,"coat").equals("homed")||state(own,"coat").equals("home"))){if(!holding.isEmpty()){cue(p,"Your hands are full.");return true;}coat(l,b,"",false);pickUp(p,own,"coat");return true;}
        if((rel.equals(FRIDGE.above())||rel.equals(FRIDGE))&&pictureUp(l,b)&&(state(own,"drawing").equals("homed")||state(own,"drawing").equals("home"))){if(!holding.isEmpty()){cue(p,"Your hands are full.");return true;}picture(l,b,own.getCompound("Snap").getCompound("Drawing"),false);pickUp(p,own,"drawing");return true;}
        // 0.4.369: Iris's tin, the sleeve on the armchair's arm, and the drawer their routine went into.
        if(rel.equals(TIN_AT)&&tin(l,b)>=0){int stage=tin(l,b);
            if(stage==0){tin(l,b,1);sound(p,SoundEvents.CHAIN_PLACE,b,TIN_AT,.6F,1.4F);cue(p,"Needles, buttons, a card of thread, and a sleeve half-finished on its needles.");if(!own.getBoolean("TinOpened")){own.putBoolean("TinOpened",true);SecondPersonStory.turned(own,"You opened Iris's tin.");}return true;}
            if(stage==1&&holding.isEmpty()){tin(l,b,2);pickUp(p,own,"sleeve");return true;}
            cue(p,stage==1?"Your hands are full.":"Needles and buttons.");return true;}
        if(rel.equals(ARMCHAIR)&&sleeve(l,b)==0&&state(own,"sleeve").equals("arm")&&!own.getBoolean("SleeveFound")){if(!holding.isEmpty()){cue(p,"Your hands are full.");return true;}sleeve(l,b,-1);own.remove("SleeveOut");pickUp(p,own,"sleeve");return true;}
        if(rel.equals(VANITY)&&state(own,"routine").equals("drawer")){if(!holding.isEmpty()){cue(p,"Your hands are full.");return true;}sound(p,SoundEvents.CHISELED_BOOKSHELF_PICKUP,b,VANITY,.7F,1);pickUp(p,own,"routine");return true;}
        // The hall glass turns to the wall and back.
        if(rel.equals(GLASS)&&glass(l,b)>=0){int now=glass(l,b)==0?1:0;glass(l,b,now);sound(p,SoundEvents.ITEM_FRAME_ROTATE_ITEM,b,GLASS,1,1);habit(own,"glass");
            if(now==1)SecondPersonStory.turned(own,"You turned the hall glass to the wall.");else SecondPersonStory.turned(own,"You turned the hall glass round again.");
            if(seasonOf(own)==MORNING&&own.getString("LastAct").isEmpty())own.putString("LastAct","glass");return true;}
        // The lamp.
        if(rel.equals(LAMP)){boolean on=!lampOn(l,b);lamp(l,b,on);sound(p,SoundEvents.LEVER_CLICK,b,LAMP,.6F,on?1.2F:.9F);
            if(on){habit(own,"lamp");if(own.getBoolean("Walking")&&own.getString("BroughtBy").isEmpty())own.putString("BroughtBy","lamp");}return true;}
        // The tap.
        if(rel.equals(TAP)){sound(p,HouseReadRegistry.TAP.get(),b,TAP,1,1);l.sendParticles(p,ParticleTypes.SPLASH,true,b.getX()+TAP.getX()+.5,b.getY()+TAP.getY()+.3,b.getZ()+TAP.getZ()+.5,10,.1,.05,.1,0);
            if(seasonOf(own)==SUMMER&&step(own)==NIGHT&&sub(own)==1)own.putBoolean("TapRan",true);habit(own,"tap");return true;}
        // The phone.
        if(rel.equals(PHONE)){if(own.getInt("Ringing")>0){lift(p,b,own);return true;}if(phoneOff(l,b)){phone(l,b,false);sound(p,PhoneSounds.PICKUP.get(),b,PHONE,.6F,.8F);cue(p,"You put the receiver back.");return true;}cue(p,"The line purrs.");return true;}
        // Glass that holds a word.
        if(mirrorAt(rel)||streetPane(rel))return write(p,b,rel,own);
        // The tablecloth.
        if(rel.equals(CLOTH)&&SecondPersonBlock.is(s,SecondPersonBlock.Kind.CLOTH)){int stage=cloth(l,b);if(stage==0){cloth(l,b,1);sound(p,SoundEvents.WOOL_PLACE,b,CLOTH,1,1);}return true;}
        // The bed is theirs.
        if(s.getBlock() instanceof BedBlock){cue(p,"Their bed.");return true;}
        // The armchair takes the reader's weight, for all the good it does.
        if(rel.equals(ARMCHAIR)){io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,b.offset(ARMCHAIR));return true;}
        // Walls: a knock. The wall at the end of the passage is Iris's.
        if(inFlat(v)&&(s.is(SecondPersonRegistry.material("paper"))||s.is(SecondPersonRegistry.material("kitchen"))||s.is(SecondPersonRegistry.material("tile"))||s.is(SecondPersonRegistry.material("brick")))){
            sound(p,DoorSounds.KNOCK.get(),b,rel,.9F,1.1F);habit(own,"knock");
            if(own.getBoolean("Walking")){if(own.getString("BroughtBy").isEmpty())own.putString("BroughtBy","knock");}
            else if(irisWall(rel)&&seasonOf(own)==WINTER){cue(p,"Nobody knocks back.");if(!own.getBoolean("KnockedWinter")){own.putBoolean("KnockedWinter",true);SecondPersonStory.turned(own,"You knocked on Iris's wall in the winter, and nobody knocked back.");}}
            else if(irisWall(rel)&&seasonOf(own)==AUTUMN){if(!own.getBoolean("IrisKnocked")){own.putBoolean("IrisKnocked",true);SecondPersonStory.turned(own,"You knocked on Iris's wall, and she knocked back.");}f.knockBack=30;}
            else if(seasonOf(own)==AUTUMN&&step(own)==EVENING)own.putBoolean("KnockedInEvening",true);
            else{var a=actor(l,f.resident);if(a!=null&&!a.isSleeping()&&f.once.add("knock"+(t(own)/600)))say(f,a,"Hello?");}
            return true;
        }
        return false;
    }
    /** What the reader does again and again in the flat: the sleepwalk takes up the most worn habit. */
    static void habit(CompoundTag own,String what){if(seasonOf(own)<WINTER||!own.getBoolean("Walking"))own.putInt("Habit_"+what,own.getInt("Habit_"+what)+1);}
    /** The book on the shelf, after its opening: always its latest page. */
    public static boolean book(ServerPlayer p,BlockPos b,CompoundTag own){
        var pages=SecondPersonTexts.printed(SecondPersonStory.latest(own));
        var stack=io.github.knaitoe.theoldesthouse.house.HouseWriting.book(SecondPersonTexts.TITLE,"",pages);var at=b.offset(BOOK);
        p.openMenu(new SimpleMenuProvider((id,inv,who)->new LiteraryVignettes.Pages(id,p,PLACE,"Latest",stack,false,true,at),stack.getHoverName()));
        return true;
    }
}
