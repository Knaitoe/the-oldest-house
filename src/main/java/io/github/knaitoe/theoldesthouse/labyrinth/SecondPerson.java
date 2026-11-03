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
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SecondPerson {
    private SecondPerson(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SECOND_PERSON;
    /** The cast on stranger phases: Iris Dunne across the landing, and Sam from work. */
    public static final int IRIS=75,SAM=76;
    public static boolean cast(int phase){return phase==IRIS||phase==SAM;}
    public static final int SUMMER=0,AUTUMN=1,MORNING=2;
    public static final int ARRIVE=0,UNPACK=1,GLASS_HUNG=2,BATH=3,NIGHT=4,SUMMER_DONE=5;
    public static final int IRIS_VISIT=0,ROUTINE=1,EVENING=2,PHOTO=3,CALL=4,STAY=5,AUTUMN_DONE=6;
    public static final int LATCH=0,OUT=1,ON_BENCH=2;
    /** The four things that are theirs, in the order they unpack them. */
    public static final List<String> THINGS=List.of("photo","drawing","coat","routine");
    static final String TAG="SecondPerson274";
    public static final int EMPTY_RESET=200,WAIT=400,NIGHT_WAIT=1200,RING=200,EVENING_LENGTH=1200;
    static final int G_SIT=1,G_PHONE=3,G_CAMERA=4,G_PERCH=5,G_CARRY=6,G_COLD=7;

    /** One flat in memory: its people and the counters between beats. Every fact lives in the reader's record. */
    static final class Flat {
        final BlockPos base;int empty;boolean cleared;
        @Nullable UUID resident,iris,sam;
        int ring=-1,doorway,armchair,window,shut,near,knockBack=-1,unseen,wordWipe=-1,flash;
        boolean bedDoor;long sent=Long.MIN_VALUE;
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
    static boolean dark(CompoundTag own){int s=seasonOf(own),st=step(own);return s==SUMMER&&(st==BATH||st==NIGHT)||s==AUTUMN&&st>=EVENING;}
    /** The flat's hour, from the year so far. */
    static long hour(CompoundTag own){
        int s=seasonOf(own),st=step(own);
        if(s==SUMMER)return switch(st){case ARRIVE,UNPACK->8200;case GLASS_HUNG->10800;case BATH->13200;case NIGHT->18000;default->23400;};
        if(s==AUTUMN)return switch(st){case IRIS_VISIT->11400;case ROUTINE->12100;case EVENING->13600;case PHOTO->14200;case CALL->15000;case STAY->16000;default->17500;};
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
    static Mark seat(BlockPos chair,float yaw){return new Mark(at(chair.getX()+.5,chair.getY(),chair.getZ()+.5),yaw,G_SIT,false,true);}
    static Mark bed(){return new Mark(at(BED_HEAD.getX()+.5,UP,BED_HEAD.getZ()+.5),EAST,0,true,true);}
    /** Somewhere to stand beside a fitting, from fixed places in the flat. */
    static Mark beside(String what){return switch(what){
        case "photo_box"->Mark.stand(-.5,-17.5,NORTH);case "coat_box"->Mark.stand(.5,-18.5,NORTH);case "drawing_box"->Mark.stand(-1.5,-21.5,NORTH);case "last_box"->Mark.stand(1.5,-16.5,EAST);
        case "photo"->Mark.stand(.5,-15.5,NORTH);case "drawing"->Mark.stand(-1.5,-21.5,SOUTH);case "coat"->Mark.stand(1.5,-25.5,EAST);case "routine"->Mark.stand(-.5,-25.5,NORTH);
        case "glass"->Mark.stand(1.5,-22.5,EAST);case "phone"->Mark.stand(-5.5,-18.5,WEST);case "sink"->Mark.stand(-2.5,-21.5,NORTH);case "bathdoor"->Mark.stand(1.5,-25.5,WEST);
        case "door"->Mark.stand(1.5,-26.5,EAST);case "window"->Mark.stand(-3.5,-14.5,SOUTH);case "photo_spot"->Mark.stand(1.5,-16.5,WEST);case "sofa"->new Mark(at(-4.5,UP,-18.5),SOUTH,G_SIT,false,true);
        case "hall_bag"->Mark.stand(1.5,-25.5,SOUTH);case "tub"->new Mark(at(-2.5,UP,-25.5),SOUTH,G_SIT,false,true);
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
                if(sub==2&&own.getString("Night").equals("doorway"))return new Mark(at(-5.5,UP,-26.5),EAST,G_PERCH,false,true);
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
            case PHOTO->{return sub<=1?beside("photo_spot").pose(G_CAMERA):beside("photo_spot").pose(G_PHONE);}
            case CALL->{return own.getString("Call2").equals("left")?beside("phone").pose(G_PHONE):seat(RESIDENT_CHAIR,EAST);}
            case STAY->{return own.getBoolean("SamStayed")?beside("sofa"):beside("door");}
            default->{return own.getBoolean("SamStayed")?beside("sofa"):seat(ARMCHAIR,WEST);}
        }
        // The last morning.
        boolean stays=own.getString("Decision").equals("stay");
        if(st==LATCH)return stays?seat(RESIDENT_CHAIR,EAST):beside("hall_bag").pose(G_CARRY);
        if(stays)return beside("window");
        return sub==0?Mark.stand(5.5,0,-11.0,SOUTH).pose(G_CARRY):sub==1?Mark.stand(8.5,0,-11.0,EAST).pose(G_CARRY):sub==2?Mark.stand(15.5,0,-11.0,EAST).pose(G_CARRY):Mark.gone();
    }
    /** The resident first comes up the stairs with the last box; afterwards they are wherever the year left them. */
    static @Nullable Vec3 residentFrom(CompoundTag own){return seasonOf(own)==SUMMER&&step(own)==UNPACK&&sub(own)==0?at(5.5,UP,-24.5):null;}
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
        if(a==null){a=spawn(l,b,p.getUUID(),role,phase,from==null?mark.at():from);if(a==null)return null;keep.accept(a.getUUID());}
        double dist=a.position().distanceToSqr(to);
        boolean seated=mark.gesture()==G_SIT||mark.gesture()==G_PERCH||mark.asleep();
        if(mark.asleep()&&a.isSleeping()){a.gesture(0);return a;}
        if(a.isSleeping())a.stopSleeping();
        // A seat or the bed: walk up to it, then sit or lie down in it.
        if(seated&&dist<6.25){
            if(mark.asleep()){a.noPhysics=false;a.setNoGravity(false);a.gesture(0);a.moveTo(to.x,to.y,to.z,mark.yaw(),0);a.startSleeping(b.offset(BED_HEAD));return a;}
            a.gesture(mark.gesture());a.noPhysics=true;a.setNoGravity(true);a.moveTo(to.x,to.y,to.z,mark.yaw(),0);a.setYRot(mark.yaw());a.setYBodyRot(mark.yaw());a.setYHeadRot(mark.yaw());return a;
        }
        a.gesture(seated?0:mark.gesture());
        if(dist<.09){a.noPhysics=false;a.setNoGravity(false);a.setYRot(mark.yaw());a.setYBodyRot(mark.yaw());a.setYHeadRot(mark.yaw());return a;}
        a.noPhysics=false;a.setNoGravity(false);
        if(!p.hasLineOfSight(a)||dist>144){a.moveTo(to.x,to.y,to.z,mark.yaw(),0);a.setYHeadRot(mark.yaw());a.setYBodyRot(mark.yaw());return a;}
        // They stop in a doorway the reader is standing in, rather than walk through them.
        var ahead=to.subtract(a.position()).normalize();var toReader=p.position().subtract(a.position());
        if(toReader.horizontalDistance()<1.1&&ahead.dot(toReader.normalize())>.4){if(f.once.add("stalled"+(l.getGameTime()/400)))say(f,a,"...");return a;}
        doorsFor(l,b,rel(a.position(),b),mark.at());openOnTheWay(l,f,b,a,to);
        SceneFootpaths.walk(a,to,.12);
        return a;
    }
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
    static boolean isOpen(ServerLevel l,BlockPos b,BlockPos door){var s=l.getBlockState(b.offset(door));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}

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
            var gone=new ArrayList<UUID>();for(var e:f.said.entrySet())if(l.getGameTime()-e.getValue()>70){var a=actor(l,e.getKey());if(a!=null){a.setCustomName(null);a.setCustomNameVisible(false);}gone.add(e.getKey());}gone.forEach(f.said::remove);
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
        var resident=stage(l,f,p,f.resident,residentMark(own),residentFrom(own),LiteraryActor.SP_RESIDENT,0,id->f.resident=id);
        var iris=stage(l,f,p,f.iris,irisMark(own),at(5.5,UP,-30.5),LiteraryActor.STRANGER,IRIS,id->f.iris=id);
        var sam=stage(l,f,p,f.sam,samMark(own),at(5.5,UP,-24.5),LiteraryActor.STRANGER,SAM,id->f.sam=id);
        cold(l,f,p,own,resident,iris,sam);
        words(l,f,p,own,resident);
        glassGlimpse(l,f,p,own,resident);
        if(f.knockBack>0&&--f.knockBack==0){sound(p,DoorSounds.KNOCK_FAR.get(),b,new BlockPos(-2,UP+1,-31),.9F,1.05F);cue(p,"Somebody knocks back, from the other side of the wall.");}
        ringing(l,f,p,own,resident);
        cupboard(l,f,p,own,r);
        int s=seasonOf(own);
        if(s==SUMMER)summer(l,f,p,own,r,resident);else if(s==AUTUMN)autumn(l,f,p,own,r,resident,iris,sam);else morning(l,f,p,own,r,resident);
    }

    static void summer(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,Vec3 r,@Nullable LiteraryActor a){
        var b=f.base;int st=step(own),sub=sub(own),dt=dt(own);
        switch(st){
            case ARRIVE->{
                if(t(own)==20)cue(p,"Somebody's boxes. Somebody is coming up the stairs.");
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
                        if(state.equals("box")){box(l,b,boxAt(th),2);state(own,th,"resident");sound(p,SoundEvents.BUNDLE_REMOVE_ONE,b,boxAt(th),1,1);if(th.equals("drawing"))say(f,a,"Kettle. Where's the kettle.");next(own,sub+1);}
                        else if(state.equals("home")){found(own,th,"put_away");next(own,sub+1);}
                        else if(state.equals("placed")){found(own,th,"moved");next(own,sub+1);}
                        else{if(!f.once.contains("hunt_"+th)){f.once.add("hunt_"+th);say(f,a,"It was here. It was in this box.");found(own,th,"hidden");next(own,sub);return;}
                            if(dt>=110){say(f,a,"It'll turn up.");f.once.remove("hunt_"+th);next(own,sub+2);}}
                    }else{if(!arrived(a,b,mark))return;
                        String state=state(own,th);
                        if(state.equals("resident")){home(l,b,own,th);state(own,th,"homed");sound(p,SoundEvents.ITEM_FRAME_ADD_ITEM,b,homeAt(th),1,1);if(th.equals("photo"))say(f,a,"There.");if(th.equals("routine"))own.putString("RoutineWhere","on the bathroom shelf");next(own,sub+1);}
                        else if(state.equals("home")){if(!f.once.contains("found_"+th)){f.once.add("found_"+th);say(f,a,"I didn't put that there.");next(own,sub);return;}
                            if(dt==60)say(f,a,"I must have.");if(dt>=100){f.once.remove("found_"+th);next(own,sub+1);}}
                        else if(state.equals("placed")){if(!f.once.contains("found_"+th)){f.once.add("found_"+th);say(f,a,"That's not where I'd have put it.");next(own,sub);return;}
                            if(dt==60)say(f,a,"Fine. There, then.");if(dt>=90){f.once.remove("found_"+th);next(own,sub+1);}}
                        else next(own,sub+1);
                    }
                }
            }
            case GLASS_HUNG->{
                if(sub==0){if(arrived(a,b,residentMark(own))){boolean unseen=own.getCompound("Snap").getBoolean("Unseen");glass(l,b,unseen?1:0);own.putBoolean("GlassHungTurned",unseen);sound(p,SoundEvents.ITEM_FRAME_PLACE,b,GLASS,1,1);if(unseen)say(f,a,"Not yet.");next(own,1);}}
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
                    case "armchair"->{cue(p,"Nothing happens. That is its own answer.");SecondPersonStory.turned(own,"You sat in the armchair in the dark.");}case "shut"->{say(f,a,"...Oh.");SecondPersonStory.turned(own,"You shut their door on them.");}
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
                if(sub==0){if(dt==60)cue(p,"The radio is on low. Somebody lives here now.");if(dt>=200){sound(p,DoorSounds.KNOCK.get(),b,FLAT_DOOR,1,1);say(f,a,"Coming.");next(own,1);}}
                else if(sub==1){if(arrived(a,b,residentMark(own))){open(l,b,FLAT_DOOR,true);open(l,b,IRIS_DOOR,true);next(own,2);}}
                else if(sub==2){
                    if(dt==20)say(f,iris,"I made too much again. It's only stew.");
                    if(dt==90)say(f,a,"You didn't have to. Thank you.");
                    if(dt==160)say(f,iris,"That was Ted's chair by your window. He liked to watch the buses.");
                    if(dt==240){say(f,iris,"This flat's never been empty in my time. You're all right. Just don't frighten them.");cue(p,"She says it past them, into the room. To you.");SecondPersonStory.turned(own,"Iris spoke to you over their shoulder.");}
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
                else if(sub==3){if(arrived(a,b,residentMark(own))&&!f.once.contains("thanks")){f.once.add("thanks");say(f,a,"Thanks.");cue(p,"To nobody.");}if(dt>=120){f.once.remove("thanks");next(own,4);}}
                else if(dt>=60){go(own,EVENING);cloth(l,b,0);own.putBoolean("EveLamp",lampOn(l,b));own.putBoolean("EveKitchenDoor",isOpen(l,b,KITCHEN_DOOR));}
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
                if(dt==20&&sub==0&&!f.once.contains("autumn_done")){f.once.add("autumn_done");cue(p,"The flat goes quiet. The airing cupboard at the end of the hall is warm.");}
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
                if(own.getBoolean("ColdBySam")||own.getBoolean("KnockedInEvening")||own.getBoolean("EveKitchenDoor")!=isOpen(l,b,KITCHEN_DOOR))choice="interfered";
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

    static void morning(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,Vec3 r,@Nullable LiteraryActor a){
        var b=f.base;int st=step(own),sub=sub(own),dt=dt(own);
        boolean stays=own.getString("Decision").equals("stay");
        if(st==LATCH){
            if(t(own)==40)cue(p,"The front door is on the latch.");
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
        boolean done=s==SUMMER&&st==SUMMER_DONE||s==AUTUMN&&st==AUTUMN_DONE&&(!own.getBoolean("IrisKnocked")||own.getBoolean("IrisTold"));
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
            cue(p,SecondPersonStory.whileAway(own,1));SecondPersonStory.turned(own,"You let the summer go by in the cupboard.");}
        else{own.putInt("Season",MORNING);go(own,LATCH);season(l,b,2);lamp(l,b,false);
            own.putString("Decision",SecondPersonStory.stays(own)?"stay":"go");open(l,b,FLAT_DOOR,true);
            cue(p,SecondPersonStory.whileAway(own,2));SecondPersonStory.turned(own,"You let the rest of the year go by in the cupboard.");}
        f.once.clear();f.ring=-1;own.remove("Ringing");
        for(var id:new UUID[]{f.resident,f.iris,f.sam}){var a=actor(l,id);if(a!=null){if(a.isSleeping())a.stopSleeping();a.discard();}}
        f.resident=null;f.iris=null;f.sam=null;
    }

    // ------------------------------------------------------------------------------------------------ the phone
    static void ringing(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a){
        if(f.ring<0)return;int call=own.getInt("Ringing");if(call==0){f.ring=-1;return;}
        if(f.ring%40==0)sound(p,WorkPrintRegistry.RING.get(),f.base,PHONE,1,1);
        if(--f.ring<0){
            // Nobody lifted it: they catch it themselves.
            String key="Call"+call;if(own.getString(key).isEmpty()){own.putString(key,"left");SecondPersonStory.turned(own,call==1?"You let the phone ring in the evening.":"You let the phone ring again.");}
            own.remove("Ringing");
        }
    }
    /** The reader lifts the receiver. The voice goes on in the conversation panel; the reader can only put it down, leave it off, or listen. */
    static void lift(ServerPlayer p,BlockPos b,CompoundTag own){
        var f=flat(b);if(f==null)return;int call=own.getInt("Ringing");if(call==0)return;f.ring=-1;own.remove("Ringing");own.putInt("LiftedAt",t(own));own.putInt("LiftedCall",call);
        sound(p,PhoneSounds.PICKUP.get(),b,PHONE,1,1);
        Talk.begin(p,"sp_call_"+call,SecondPersonStory.caller(own));
        for(var line:SecondPersonStory.call(own,call))Talk.line(p,SecondPersonStory.caller(own),line);
        Talk.ask(p,Talk.reply("Put the receiver down.",q->answer(q,b,call,"put_down")),Talk.reply("Leave it off the hook.",q->answer(q,b,call,"off")),Talk.reply("Listen.",q->answer(q,b,call,"lifted")));
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
            default->{for(var line:SecondPersonStory.listened(own,call))Talk.line(p,SecondPersonStory.caller(own),line);Talk.line(p,"","The line clicks, and purrs.");Talk.end(p);phone(l,b,false);SecondPersonStory.turned(own,"You lifted the phone and listened, and said nothing.");}
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
        if(a.role()==LiteraryActor.SP_RESIDENT){say(f,a,a.isSleeping()?"Mm.":"...Who's there?");SecondPersonStory.add(own,"Touched","them");}
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
        if(readerIn&&residentPassing){own.putBoolean("GlassSeen",true);say(f,a,"...");SecondPersonStory.turned(own,"You stood where the hall glass faced you, and they looked into it a long time.");f.once.add("glassline");}
    }
    static final List<Integer> WINDOW_YS=List.of(UP+1,UP+2);
    static boolean streetPane(BlockPos rel){return rel.getZ()==-12&&rel.getX()>=WINDOW_X0&&rel.getX()<=WINDOW_X1&&WINDOW_YS.contains(rel.getY());}
    static boolean mirrorAt(BlockPos rel){return rel.equals(MIRROR);}
    /** Breath on cold or steamed glass holds one word. */
    static boolean write(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();boolean mirror=mirrorAt(rel);
        boolean ready=mirror?steamed(l,b):dark(own)||seasonOf(own)==MORNING;
        if(!ready){cue(p,mirror?"The glass is dry. Nothing would hold.":"The glass is too warm to hold a breath.");return true;}
        String surface=mirror?"mirror":"window";if(own.contains("Word_"+surface)){cue(p,"There is a word on it already.");return true;}
        Talk.begin(p,"sp_word","Breath on the glass");Talk.line(p,"","You lean close and breathe, and the glass clouds. One word.");
        var replies=new ArrayList<Talk.Reply>();for(var w:SecondPersonStory.WORDS)replies.add(Talk.reply(w,q->word(q,b,surface,w)));replies.add(Talk.reply("Nothing.",Talk::close));
        Talk.ask(p,replies);return true;
    }
    static void word(ServerPlayer p,BlockPos b,String surface,String word){
        var own=own(p);var f=flat(b);if(f==null||own.contains("Word_"+surface)){Talk.close(p);return;}Talk.close(p);var l=p.serverLevel();
        own.putString("Word_"+surface,word);SecondPersonStory.add(own,"Words",word);SecondPersonStory.turned(own,"You wrote "+word+" on the "+(surface.equals("mirror")?"bathroom mirror":"street window")+".");
        if(seasonOf(own)==MORNING&&own.getString("LastAct").isEmpty())own.putString("LastAct","word");
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
            if(!own.contains("Word_"+surface)||own.getBoolean("WordSeen_"+surface))continue;
            var target=surface.equals("mirror")?Vec3.atCenterOf(b.offset(MIRROR)):abs(b,at(-3,UP+1.5,-12.5));
            if(a.distanceToSqr(target)>25||!a.hasLineOfSight(p)&&a.getEyePosition().distanceToSqr(target)>9)continue;
            String w=own.getString("Word_"+surface);own.putBoolean("WordSeen_"+surface,true);SecondPersonStory.add(own,"WordsSeen",w);
            say(f,a,SecondPersonStory.sawWord(w));own.putInt("Wipe_"+surface,t(own)+120);
        }
        for(var surface:List.of("mirror","window")){int at=own.getInt("Wipe_"+surface);if(at>0&&t(own)>=at){own.remove("Wipe_"+surface);own.remove("Word_"+surface);own.remove("WordSeen_"+surface);
            for(var d:l.getEntitiesOfClass(Display.TextDisplay.class,bounds(b),d->d.getTags().contains("SpWord_"+surface)))d.discard();if(surface.equals("mirror"))mirror(l,b,false);}}
        if(own.getInt("SteamUntil")>0&&t(own)>=own.getInt("SteamUntil")&&seasonOf(own)==SUMMER){own.remove("SteamUntil");if(!own.contains("Word_mirror"))mirror(l,b,false);}
    }
    static void clearWords(ServerLevel l,Flat f){for(var d:l.getEntitiesOfClass(Display.TextDisplay.class,bounds(f.base),d->d.getTags().contains(TAG)))d.discard();}

    // ------------------------------------------------------------------------------------------------ the things
    static void found(CompoundTag own,String thing,String how){SecondPersonStory.add(own,"Found",thing+":"+how);}
    static BlockPos homeAt(String thing){return switch(thing){case "photo"->PHOTO_HOME;case "drawing"->FRIDGE.above();case "coat"->HOOK;default->ROUTINE_HOME;};}
    /** The resident puts a thing where it lives. */
    static void home(ServerLevel l,BlockPos b,CompoundTag own,String thing){
        var snap=own.getCompound("Snap");
        switch(thing){case "drawing"->picture(l,b,snap.getCompound("Drawing"),true);case "coat"->coat(l,b,snap.getString("Coat"),true);
            default->SecondPersonSite.thing(l,b,homeAt(thing),thing,snap.getInt("Routine"));}
    }
    static int model(CompoundTag own,String thing){return switch(thing){case "coat"->SecondPersonRegistry.COAT_MODEL;case "drawing"->SecondPersonRegistry.SHEET_MODEL;case "photo"->SecondPersonRegistry.PHOTO_MODEL;default->1+Math.floorMod(own.getCompound("Snap").getInt("Routine")<0?11:own.getCompound("Snap").getInt("Routine"),16);};}
    static String name(CompoundTag own,String thing){var s=SecondPersonStory.thing(own,thing);return Character.toUpperCase(s.charAt(4))+s.substring(5);}
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
            case "home"->{state(own,thing,"home");placedAt(own,thing,null);home(l,b,own,thing);SecondPersonStory.add(own,"PutAway",thing);SecondPersonStory.turned(own,"You put "+SecondPersonStory.thing(own,thing)+" where it belongs.");
                if(thing.equals("routine"))own.putString("RoutineWhere","on the bathroom shelf, where you put it");}
            case "bed"->{state(own,thing,"hidden");thing(own,thing).putString("Where","under the bed");SecondPersonStory.add(own,"Hidden",thing);SecondPersonStory.turned(own,"You pushed "+SecondPersonStory.thing(own,thing)+" under the bed.");}
            case "cupboard"->{state(own,thing,"hidden");placedAt(own,thing,at);SecondPersonSite.thing(l,b,at,thing,own.getCompound("Snap").getInt("Routine"));SecondPersonStory.add(own,"Hidden",thing);SecondPersonStory.turned(own,"You hid "+SecondPersonStory.thing(own,thing)+" in the airing cupboard.");}
            default->{state(own,thing,"placed");placedAt(own,thing,at);SecondPersonSite.thing(l,b,at,thing,own.getCompound("Snap").getInt("Routine"));SecondPersonStory.add(own,"PlacedThings",thing);SecondPersonStory.turned(own,"You put "+SecondPersonStory.thing(own,thing)+" somewhere else.");
                if(thing.equals("routine"))own.putString("RoutineWhere",where(at));}
        }
        sound(p,SoundEvents.ITEM_FRAME_ADD_ITEM,b,at==null?b.subtract(b):at,.7F,1);
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
            for(var th:THINGS)if(th.equals(standing)&&(rel.equals(placedAt(own,th))||rel.equals(homeAt(th))&&(state(own,th).equals("homed")||state(own,th).equals("home")))){
                if(!holding.isEmpty()){cue(p,"Your hands are full.");return true;}
                l.setBlock(b.offset(rel),Blocks.AIR.defaultBlockState(),3);pickUp(p,own,th);return true;}
        }
        if(rel.equals(HOOK)&&coatHung(l,b)&&(state(own,"coat").equals("homed")||state(own,"coat").equals("home"))){if(!holding.isEmpty()){cue(p,"Your hands are full.");return true;}coat(l,b,"",false);pickUp(p,own,"coat");return true;}
        if((rel.equals(FRIDGE.above())||rel.equals(FRIDGE))&&pictureUp(l,b)&&(state(own,"drawing").equals("homed")||state(own,"drawing").equals("home"))){if(!holding.isEmpty()){cue(p,"Your hands are full.");return true;}picture(l,b,own.getCompound("Snap").getCompound("Drawing"),false);pickUp(p,own,"drawing");return true;}
        // The hall glass turns to the wall and back.
        if(rel.equals(GLASS)&&glass(l,b)>=0){int now=glass(l,b)==0?1:0;glass(l,b,now);sound(p,SoundEvents.ITEM_FRAME_ROTATE_ITEM,b,GLASS,1,1);
            if(now==1)SecondPersonStory.turned(own,"You turned the hall glass to the wall.");else SecondPersonStory.turned(own,"You turned the hall glass round again.");
            if(seasonOf(own)==MORNING&&own.getString("LastAct").isEmpty())own.putString("LastAct","glass");return true;}
        // The lamp.
        if(rel.equals(LAMP)){boolean on=!lampOn(l,b);lamp(l,b,on);sound(p,SoundEvents.LEVER_CLICK,b,LAMP,.6F,on?1.2F:.9F);return true;}
        // The tap.
        if(rel.equals(TAP)){sound(p,HouseReadRegistry.TAP.get(),b,TAP,1,1);l.sendParticles(p,ParticleTypes.SPLASH,true,b.getX()+TAP.getX()+.5,b.getY()+TAP.getY()+.3,b.getZ()+TAP.getZ()+.5,10,.1,.05,.1,0);
            if(seasonOf(own)==SUMMER&&step(own)==NIGHT&&sub(own)==1)own.putBoolean("TapRan",true);return true;}
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
            sound(p,DoorSounds.KNOCK.get(),b,rel,.9F,1.1F);
            if(irisWall(rel)&&seasonOf(own)==AUTUMN){if(!own.getBoolean("IrisKnocked")){own.putBoolean("IrisKnocked",true);SecondPersonStory.turned(own,"You knocked on Iris's wall, and she knocked back.");}f.knockBack=30;}
            else if(seasonOf(own)==AUTUMN&&step(own)==EVENING)own.putBoolean("KnockedInEvening",true);
            else{var a=actor(l,f.resident);if(a!=null&&!a.isSleeping()&&f.once.add("knock"+(t(own)/600)))say(f,a,"Hello?");}
            return true;
        }
        return false;
    }
    /** The book on the shelf, after its opening: always its latest page. */
    public static boolean book(ServerPlayer p,BlockPos b,CompoundTag own){
        var pages=SecondPersonTexts.printed(SecondPersonStory.latest(own));
        var stack=io.github.knaitoe.theoldesthouse.house.HouseWriting.book(SecondPersonTexts.TITLE,"",pages);var at=b.offset(BOOK);
        p.openMenu(new SimpleMenuProvider((id,inv,who)->new LiteraryVignettes.Pages(id,p,PLACE,"Latest",stack,false,true,at),stack.getHoverName()));
        return true;
    }
}
