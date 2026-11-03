package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseExperience;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.264, Johnny Truant, first slice (docs/TRUANT_0_4_264.md; the owner's draft, word for word, is
 * docs/TRUANT_RESIDENT_DESIGN_DRAFT.md). A useful stranger who lives in three halls of his own ({@link TruantPlaces}): he holds the
 * heavy door, steadies the crossing and catches a slip, and makes room at his lamp, where his face is first seen half round the jamb.
 * He helps first and asks afterwards, in the conversation panel ({@link TruantTexts}), and remembers what each reader actually told
 * him and actually promised. Nothing in this slice is hostile; struck, he flinches and keeps his distance for the rest of that meeting.
 * <p>
 * There is one of him. He stands in at most one of his places, and moves to another only while nobody is in the one he leaves.
 * Shared: where he is and his one body. Each reader's own: their meetings, the help he gave them, the places they told him about, the
 * promise as they made it and whether they kept it, how often they saw the half face, and how deep they had been at each meeting.
 * What he is doing in a place (holding the door, a hand on someone's back, drawing back from the corner) is not saved: a fresh
 * arrival finds him starting over.
 * <p>
 * 0.4.282, the second slice (docs/TRUANT_0_4_282.md): the claimant and the collector. Each meeting he is one thing ({@link Turn}),
 * chosen as the place is come into: the useful stranger as before, or, for a reader past twenty passes who has met him since, a
 * claimant who invents what they owe him, the hand in the doorway, the covered light, or (rarely) the shove from the crossing's ledge.
 * His claims are kept apart from what the reader actually said and what he actually did. Nothing hostile happens while any reader
 * under twenty passes is in the place with him. After he has harmed someone he can still help them, and ask nothing.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Truant {
    private Truant(){}
    public static final String STATE="truant_0264",KEY="truant";
    /** His two faces, stranger phases: the ordinary tired one, and the leer seen round the corner. */
    public static final int FACE=73,LEER=74;
    /** His gestures: holding a door, a hand forward between someone's shoulders, lifting his lamp, the lean round a corner, flinching. */
    public static final int HOLD=10,STEADY=11,LAMP=12,PEEK=13,FLINCH=14;
    /** Each kind of help, as bits of a reader's record. */
    public static final int HELP_DOOR=1,HELP_STEADY=2,HELP_CATCH=4,HELP_LAMP=8;
    /** "Twenty passes": the deepest a reader has been, which backtracking does not lower. In this slice the record only notes it. */
    public static final int TWENTY=20;
    /** The heavy door shuts itself this long after it is opened, unless he is holding it. */
    public static final int CLOSER=14;
    /** He lets the door go once nobody near it has needed it for this long. */
    public static final int LET_GO=30;
    /** A reader seen with him again after this long away is at a new meeting. */
    public static final int APART=600;
    /** How long a reader must look at the half face before he draws back from it, and how near they can come first. */
    public static final int LOOK=20;
    public static final double CLOSE=2.5;
    /** How long a struck man keeps his distance. */
    public static final int WITHDRAW=200;
    /**
     * 0.4.282, his further gestures: both hands forward, the push; bent over an edge, looking down; reaching across his lamp, a hand out
     * for a wrist; bent over a page; a hand up on a door frame.
     */
    public static final int PUSH=15,OVER=16,REACH=17,PAGE=18,FRAME=19;
    /** 0.4.282: what he is at a meeting. */
    public enum Turn{HELP,CLAIM,DOORWAY,COVER,SHOVE}
    /** The shove: how long he must have been coming before his hands can land, how long they rest there first, how long the reader must look back at him to stop him, how long he looks over. */
    public static final int APPROACH=24,HANDS=6,TURN_BACK=4,LOOK_OVER=30;
    /** The covered light: how long the lamp is out at most, and how long he holds the wrist. */
    public static final int COVER_DARK=80,WRIST_HOLD=40;
    /** A lamp put out for him comes back after this long, or as soon as the place is empty. */
    public static final int FAVOUR_DARK=1200;

    public static boolean phase(int phase){return phase==FACE||phase==LEER;}
    public static boolean isTruant(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&phase(a.phase());}

    // ------------------------------------------------------------------ records
    static CompoundTag state(MinecraftServer s){return LabyrinthData.get(s).state(STATE);}
    static void save(MinecraftServer s,CompoundTag t){LabyrinthData.get(s).setState(STATE,t);}
    /** A reader's own record of him (a copy). */
    public static CompoundTag record(MinecraftServer s,UUID id){return state(s).getCompound("Readers").getCompound(id.toString()).copy();}
    static void record(MinecraftServer s,UUID id,CompoundTag own){var t=state(s);var r=t.getCompound("Readers");r.put(id.toString(),own);t.put("Readers",r);save(s,t);}
    /** Where he is: one of his places, or null before anyone has met him. */
    public static @Nullable LabyrinthPlace at(MinecraftServer s){var place=LabyrinthPlace.byId(state(s).getString("At"));return TruantPlaces.is(place)?place:null;}
    /** The deepest the reader has ever been: the House's saved maximum, or how deep they are now if that is deeper. */
    public static int deepest(MinecraftServer s,UUID id){var d=LabyrinthData.get(s);return Math.max(HouseExperience.record(d,id).getInt("Deepest"),d.returnDepth(id));}
    /** The last story the reader was in that the House has a name for, or empty. */
    public static String lastStory(MinecraftServer s,UUID id){
        for(var placeId:LabyrinthData.get(s).recent(id)){var place=LabyrinthPlace.byId(placeId);if(place!=null&&place.isVignette()&&StaircaseProse.named(placeId))return placeId;}
        return "";
    }

    // ------------------------------------------------------------------ what is going on in his place now (not saved)
    static final class Scene {
        final LabyrinthPlace place;
        Scene(LabyrinthPlace place){this.place=place;}
        long quietAt=-1,flinchUntil=-1;
        // The held door.
        long openedAt=-1;boolean holding,asked;int idle;final Set<UUID> held=new HashSet<>();
        // The crossing: whom his hand is on, the last place on the ledge they stood, and where he is in the crossing.
        @Nullable UUID steadied;@Nullable Vec3 safe;int crossing,leg;
        // The lamp: 0 round the corner, 1 drawing back, 2 against the wall, 3 at his lamp, 4 leaning out (the half smile); each reader's looking.
        int lamp;int waited;final Map<UUID,Integer> looked=new HashMap<>();boolean greeted;
        // 0.4.282: what he is this meeting, and to whom.
        @Nullable Turn turn;@Nullable UUID target;
        // The hand in the doorway: stepped aside; how often they have pushed into the doorway; whether they are in it now.
        boolean aside,inDoor;int struggles;
        // The covered light: 0 not yet, 1 reaching across the lamp, 2 across their path, 3 the wrist, 4 done.
        int cover,coverTicks;
        // The shove: 0 waiting, 1 coming up behind, 2 hands on, 3 pushed and looking over, 4 stopped, 5 going back, 6 back in the dark.
        int shove,shoveTicks,turned;boolean breathed,careful;double pushZ;
        // The half smile: how long he has leaned out.
        int lean;
        // Favours taken on this meeting, by reader; the side of the ledge they took it on; the lamp put out for him.
        final Map<UUID,String> favour=new HashMap<>();final Map<UUID,Integer> favourSide=new HashMap<>();boolean dark,saidOut;
        boolean handsOff;
    }
    static @Nullable Scene scene;
    /** Each reader's last time with him (for meetings), the readers he has helped this meeting, those he keeps his distance from, and their time close to him (for the panel). */
    static final Map<UUID,Long> WITH=new HashMap<>();
    static final Set<UUID> HELPED=new HashSet<>(),WITHDRAWN=new HashSet<>(),ASKED=new HashSet<>();
    static final Map<UUID,Integer> NEAR=new HashMap<>();
    /** Which side of the heavy door each reader was last on, wholly: +1 the near side, -1 through. */
    static final Map<UUID,Integer> SIDE=new HashMap<>();
    static final Set<LabyrinthPlace> OCCUPIED=EnumSet.noneOf(LabyrinthPlace.class);
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){scene=null;WITH.clear();HELPED.clear();WITHDRAWN.clear();ASKED.clear();NEAR.clear();SIDE.clear();OCCUPIED.clear();FORCED=null;}
    /** A lamp he put out is never lost to a stop. */
    @SubscribeEvent public static void stopping(ServerStoppingEvent e){var l=e.getServer().getLevel(HouseDimensions.INTERIOR);if(l!=null)relight(l,null,true);}

    // ------------------------------------------------------------------ the clock
    static @Nullable LabyrinthPlace placeOf(ServerPlayer p,BlockPos origin){
        if(!p.level().dimension().equals(HouseDimensions.INTERIOR))return null;
        var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());return TruantPlaces.is(place)?place:null;
    }
    static boolean reader(ServerPlayer p){return p.isAlive()&&!p.isSpectator();}

    @SubscribeEvent public static void serverTick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null)return;
        Map<LabyrinthPlace,List<ServerPlayer>> here=new EnumMap<>(LabyrinthPlace.class);
        for(var p:l.players()){var place=placeOf(p,origin);if(place!=null)here.computeIfAbsent(place,k->new ArrayList<>()).add(p);}
        if(l.getGameTime()%10==0)relight(l,here,false);
        if(here.isEmpty()&&OCCUPIED.isEmpty())return;
        tick(l,origin,here);
    }

    static void tick(ServerLevel l,BlockPos origin,Map<LabyrinthPlace,List<ServerPlayer>> here){
        var server=l.getServer();long now=l.getGameTime();
        var s=state(server);var at=LabyrinthPlace.byId(s.getString("At"));if(!TruantPlaces.is(at))at=null;
        // He moves only while nobody (not even someone only watching) is in the place he leaves.
        if(at==null||!here.containsKey(at)){
            LabyrinthPlace next=null;
            for(var place:TruantPlaces.PLACES){var ps=here.get(place);if(ps!=null&&ps.stream().anyMatch(Truant::reader)){next=place;break;}}
            if(next!=null&&next!=at){at=next;s.putString("At",at.id());save(server,s);scene=null;}
        }
        // A place newly come into finds him starting over.
        for(var place:TruantPlaces.PLACES){
            boolean now_=here.containsKey(place);
            if(now_&&!OCCUPIED.contains(place)&&place==at)scene=null;
            if(now_)OCCUPIED.add(place);else OCCUPIED.remove(place);
        }
        for(var entry:here.entrySet()){
            var place=entry.getKey();var b=LabyrinthPlaces.base(origin,place);if(b==null||!l.isLoaded(b))continue;
            var box=RoomActors.box(b,place);if(!RoomActors.ready(l,box))continue;
            var readers=entry.getValue().stream().filter(Truant::reader).toList();
            if(place==TruantPlaces.DOOR)closer(l,b,readers,place==at);
            if(place!=at){RoomActors.clear(l,box,Truant::isTruant);continue;}
            if(scene==null||scene.place!=place)scene=new Scene(place);
            s=state(server);var before=s.hasUUID("Body")?s.getUUID("Body"):null;var a=RoomActors.one(l,box,s,"Body",Truant::isTruant);
            if(a!=null&&!box.contains(a.position())){a.bind(place,null);place(a,b,home(place),0);}
            if(a==null)a=spawn(l,b,place,s);
            if(!Objects.equals(before,s.hasUUID("Body")?s.getUUID("Body"):null))save(server,s);
            if(a==null)continue;
            for(var p:readers)met(p,a,now);
            if(scene.turn==null&&!readers.isEmpty())choose(scene,readers,l.random);
            if(now>scene.quietAt&&scene.quietAt>=0&&a.hasCustomName()){a.setCustomName(null);a.setCustomNameVisible(false);scene.quietAt=-1;}
            if(now<scene.flinchUntil){if(a.gesture()!=FLINCH)a.gesture(FLINCH);continue;}
            if(a.gesture()==FLINCH)a.gesture(0);
            switch(place){
                case TRUANT_DOOR->{if(scene.turn==Turn.DOORWAY&&!scene.aside)doorway(l,b,a,readers);else door(l,b,a,readers,now);}
                case TRUANT_LEDGE->{if(scene.turn==Turn.SHOVE)shove(l,b,a,readers);else ledge(l,b,a,readers);}
                case TRUANT_LAMP->lamp(l,b,a,readers);
                default->{}
            }
            favours(l,b,a,readers);
            panel(a,readers);
        }
    }
    static Vec3 home(LabyrinthPlace place){return switch(place){case TRUANT_DOOR->TruantPlaces.DOOR_SPOT;case TRUANT_LEDGE->TruantPlaces.LEDGE_SPOT;default->TruantPlaces.PEEK_SPOT;};}
    static @Nullable LiteraryActor spawn(ServerLevel l,BlockPos b,LabyrinthPlace place,CompoundTag s){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(place,null);a.appearance(LiteraryActor.STRANGER,place==TruantPlaces.LAMP?LEER:FACE);a.setNoGravity(true);
        var at=home(place);a.moveTo(b.getX()+at.x,b.getY()+at.y,b.getZ()+at.z,0,0);
        if(!l.addFreshEntity(a))return null;
        s.putUUID("Body",a.getUUID());return a;
    }
    static void place(LiteraryActor a,BlockPos b,Vec3 rel,float yaw){a.moveTo(b.getX()+rel.x,b.getY()+rel.y,b.getZ()+rel.z,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    static Vec3 rel(BlockPos b,Vec3 world){return world.subtract(b.getX(),b.getY(),b.getZ());}
    static void face(LiteraryActor a,Vec3 at){var d=at.subtract(a.position());if(d.horizontalDistanceSqr()<1e-4)return;float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    static void say(LiteraryActor a,String line){a.say(line);if(scene!=null)scene.quietAt=a.level().getGameTime()+90;}
    static void gesture(LiteraryActor a,int gesture){
        if(a.gesture()!=gesture)a.gesture(gesture);
        var hand=gesture==LAMP?new ItemStack(Items.LANTERN):gesture==PAGE?new ItemStack(Items.PAPER):ItemStack.EMPTY;
        if(!ItemStack.isSameItem(a.getItemBySlot(EquipmentSlot.MAINHAND),hand))a.setItemSlot(EquipmentSlot.MAINHAND,hand);
    }
    static void walk(LiteraryActor a,Vec3 to,double speed){
        var d=to.subtract(a.position());double len=d.length();if(len<1e-3)return;
        var step=len<=speed?d:d.scale(speed/len);var before=a.position();
        a.moveTo(before.x+step.x,before.y+step.y,before.z+step.z,a.getYRot(),0);
        a.walkAnimation.update((float)step.horizontalDistance()*4,.4F);
    }
    static @Nullable ServerPlayer nearest(LiteraryActor a,List<ServerPlayer> readers){ServerPlayer best=null;double d=Double.MAX_VALUE;for(var p:readers){double dd=p.distanceToSqr(a);if(dd<d){d=dd;best=p;}}return best;}

    // ------------------------------------------------------------------ meetings
    /** A reader with him: a new meeting if they have been apart long enough. */
    static void met(ServerPlayer p,LiteraryActor a,long now){
        var last=WITH.put(p.getUUID(),now);if(last!=null&&now-last<=APART)return;
        var id=p.getUUID();HELPED.remove(id);WITHDRAWN.remove(id);ASKED.remove(id);NEAR.remove(id);
        var own=record(p.server,id);int meeting=own.getInt("Meetings")+1;own.putInt("Meetings",meeting);
        int deepest=deepest(p.server,id);var depths=own.getList("Depths",Tag.TAG_INT);depths.add(IntTag.valueOf(deepest));while(depths.size()>32)depths.remove(0);own.put("Depths",depths);
        if(deepest>=TWENTY&&!own.getBoolean("Twenty")){own.putBoolean("Twenty",true);own.putInt("TwentyAt",meeting);}
        own.putString("MetIn",a.scene());record(p.server,id,own);
    }
    static int meeting(CompoundTag own){return own.getInt("Meetings");}
    static void helped(ServerPlayer p,int bit){
        var own=record(p.server,p.getUUID());own.putInt("Help",own.getInt("Help")|bit);record(p.server,p.getUUID(),own);HELPED.add(p.getUUID());
    }
    static boolean withdrawn(ServerPlayer p){return WITHDRAWN.contains(p.getUUID());}

    // ------------------------------------------------------------------ the held door
    static boolean doorOpen(ServerLevel l,BlockPos b){var st=l.getBlockState(b.offset(TruantPlaces.HEAVY_DOOR));return st.getBlock() instanceof DoorBlock&&st.getValue(DoorBlock.OPEN);}
    static void swing(ServerLevel l,BlockPos b,boolean open){
        var pos=b.offset(TruantPlaces.HEAVY_DOOR);var st=l.getBlockState(pos);if(!(st.getBlock() instanceof DoorBlock door)||st.getValue(DoorBlock.OPEN)==open)return;
        door.setOpen(null,l,st,pos,open);
    }
    /** Which side of the door a reader is on, by the door's own cell: +1 the near side, -1 through, 0 in the doorway. */
    static int side(BlockPos b,ServerPlayer p){double z=p.getZ()-b.getZ();double door=TruantPlaces.HEAVY_DOOR.getZ();if(z-.3>door+1)return 1;if(z+.3<door)return -1;return 0;}
    static boolean inDoorway(BlockPos b,ServerPlayer p){double x=p.getX()-b.getX();return side(b,p)==0&&x>-.3&&x<1.3&&p.getY()-b.getY()<2;}
    /**
     * The door's closer: it shuts the door a moment after anyone opens it, whether or not he is here, unless he is holding it; a reader
     * caught in the doorway is pushed back the side they came from.
     */
    static void closer(ServerLevel l,BlockPos b,List<ServerPlayer> readers,boolean present){
        long now=l.getGameTime();
        for(var p:readers){int side=side(b,p);if(side!=0)SIDE.put(p.getUUID(),side);}
        boolean open=doorOpen(l,b);var sc=present&&scene!=null&&scene.place==TruantPlaces.DOOR?scene:null;
        if(!open){if(sc!=null)sc.openedAt=-1;else OPENED_ALONE=-1;return;}
        if(sc!=null&&sc.holding)return;
        long opened=sc!=null?sc.openedAt:OPENED_ALONE;
        if(opened<0){if(sc!=null)sc.openedAt=now;else OPENED_ALONE=now;return;}
        if(now-opened<CLOSER)return;
        swing(l,b,false);if(sc!=null)sc.openedAt=-1;else OPENED_ALONE=-1;
        for(var p:readers)if(inDoorway(b,p)){
            int back=SIDE.getOrDefault(p.getUUID(),1);double z=b.getZ()+TruantPlaces.HEAVY_DOOR.getZ()+(back>0?1.42:-.42);
            p.teleportTo(l,Math.max(b.getX()-.2,Math.min(b.getX()+1.2,p.getX())),p.getY(),z,p.getYRot(),p.getXRot());
            p.setDeltaMovement(0,.1,back*.3);p.hurtMarked=true;
            l.playSound(null,p.blockPosition(),SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.9F,.7F);
            PuzzleRooms.say(p,"The door swings shut on you and shoves you back.");
        }
    }
    /** When he is not here to keep the count, the door's own. */
    static long OPENED_ALONE=-1;
    /** He holds the door while anyone near it on the near side, or in the doorway, has yet to go through; then he lets it shut and asks. */
    static void door(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers,long now){
        var sc=scene;var spot=TruantPlaces.DOOR_SPOT;if(a.phase()!=FACE)a.appearance(LiteraryActor.STRANGER,FACE);
        if(a.position().distanceToSqr(Vec3.atLowerCornerOf(b).add(spot))>.01)place(a,b,spot,a.getYRot());
        double doorZ=b.getZ()+TruantPlaces.HEAVY_DOOR.getZ();
        List<ServerPlayer> waiting=new ArrayList<>();
        for(var p:readers){if(withdrawn(p)||sc.favour.containsKey(p.getUUID()))continue;int side=side(b,p);if(side==0&&inDoorway(b,p)||side>0&&p.getZ()<doorZ+6.5)waiting.add(p);}
        if(!waiting.isEmpty()){
            if(!sc.holding){sc.holding=true;swing(l,b,true);if(!sc.asked)say(a,TruantTexts.HOLDING);}
            sc.idle=0;gesture(a,HOLD);face(a,Vec3.atLowerCornerOf(b).add(.5,0,TruantPlaces.HEAVY_DOOR.getZ()+2));
            for(var p:waiting)sc.held.add(p.getUUID());
        }else if(sc.holding){
            if(!doorOpen(l,b))swing(l,b,true);
            if(++sc.idle>=LET_GO){
                sc.holding=false;swing(l,b,false);gesture(a,0);
                boolean through=false;for(var p:readers)if(sc.held.contains(p.getUUID())&&side(b,p)<0){helped(p,HELP_DOOR);through=true;}
                if(through&&!sc.asked){sc.asked=true;say(a,TruantTexts.ASK_DOOR);for(var p:readers)if(sc.held.contains(p.getUUID()))ASKED.add(p.getUUID());}
                sc.held.clear();
            }
        }else{var r=nearest(a,readers);if(r!=null)face(a,r.position());}
        // Anyone he held it for who is through counts at once, so the help is theirs even if they walk on before he lets go.
        if(sc.holding)for(var p:readers)if(sc.held.contains(p.getUUID())&&side(b,p)<0&&!HELPED.contains(p.getUUID()))helped(p,HELP_DOOR);
    }

    // ------------------------------------------------------------------ the crossing
    static boolean onLedge(BlockPos b,ServerPlayer p){
        var r=rel(b,p.position());
        return r.x>=TruantPlaces.LEDGE_X-.3&&r.x<TruantPlaces.LEDGE_X+1&&r.z<TruantPlaces.LEDGE_NEAR+1&&r.z>TruantPlaces.LEDGE_FAR&&r.y>-.3&&r.y<1;
    }
    static boolean inShaft(BlockPos b,ServerPlayer p){var r=rel(b,p.position());return r.z<TruantPlaces.LEDGE_NEAR+1&&r.z>TruantPlaces.LEDGE_FAR&&r.y<-.6&&r.y>TruantPlaces.LOWER-.5;}
    static boolean across(BlockPos b,ServerPlayer p){var r=rel(b,p.position());return r.z<TruantPlaces.LEDGE_FAR-.6&&r.y>-.3;}
    static boolean nearSide(BlockPos b,ServerPlayer p){var r=rel(b,p.position());return r.z>TruantPlaces.LEDGE_NEAR+1&&r.y>-.3;}
    /**
     * He waits at the near end. As a reader steps onto the ledge he puts a hand between their shoulders and walks behind them; while his
     * hand is there a slip off the edge is caught. At the far end he stays by them a while, then goes back over when someone is waiting.
     */
    static void ledge(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers){
        var sc=scene;double x=b.getX()+TruantPlaces.LEDGE_X+.5,y=b.getY();
        ServerPlayer held=null;if(sc.steadied!=null)for(var p:readers)if(p.getUUID().equals(sc.steadied))held=p;
        if(sc.steadied!=null&&held==null){sc.steadied=null;sc.crossing=3;sc.leg=1;}
        if(a.phase()!=FACE)a.appearance(LiteraryActor.STRANGER,FACE);
        switch(sc.crossing){
            case 0->{
                place(a,b,TruantPlaces.LEDGE_SPOT,a.getYRot());var r=nearest(a,readers);if(r!=null)face(a,r.position());gesture(a,0);
                for(var p:readers)if(onLedge(b,p)&&!withdrawn(p)&&!sc.favour.containsKey(p.getUUID())){
                    sc.steadied=p.getUUID();sc.crossing=1;sc.safe=p.position();
                    // 0.4.282: after the shove he keeps his hands to himself, a step further back, and still catches a slip.
                    var own=record(p.server,p.getUUID());sc.handsOff=harmed(own)&&own.getString("Harm").equals("shove");
                    say(a,sc.handsOff?TruantTexts.HANDS_OFF:TruantTexts.STEADY);break;}
            }
            case 1->{
                if(held==null)break;
                if(onLedge(b,held)&&held.onGround())sc.safe=held.position();
                if(inShaft(b,held)&&sc.safe!=null){
                    held.teleportTo(l,sc.safe.x,sc.safe.y,sc.safe.z,held.getYRot(),held.getXRot());held.setDeltaMovement(Vec3.ZERO);held.fallDistance=0;held.hurtMarked=true;
                    l.playSound(null,held.blockPosition(),SoundEvents.ARMOR_EQUIP_LEATHER.value(),SoundSource.PLAYERS,.8F,.8F);
                    say(a,TruantTexts.CAUGHT);helped(held,HELP_CATCH);
                }
                // Behind them, a hand forward: a block back along the ledge, never past its near end.
                double z=Math.min(b.getZ()+TruantPlaces.LEDGE_NEAR+1.6,Math.max(b.getZ()+TruantPlaces.LEDGE_FAR+.5,held.getZ()+(sc.handsOff?2:1)));
                var before=a.position();a.moveTo(x,y,z,180,0);a.setYBodyRot(180);a.setYHeadRot(180);
                a.walkAnimation.update((float)a.position().subtract(before).horizontalDistance()*4,.4F);gesture(a,sc.handsOff?0:STEADY);
                if(across(b,held)){helped(held,HELP_STEADY);say(a,TruantTexts.THERE);ASKED.add(held.getUUID());sc.steadied=null;sc.crossing=2;sc.leg=0;}
                else if(nearSide(b,held)){sc.steadied=null;sc.crossing=0;}
            }
            case 2->{
                // Off the end of the ledge onto the far landing, and stand by them.
                var off=new Vec3(x,y,b.getZ()+TruantPlaces.LEDGE_FAR-1.2);
                if(sc.leg==0){walk(a,off,.12);if(a.position().distanceToSqr(off)<.01)sc.leg=1;}else walk(a,Vec3.atLowerCornerOf(b).add(TruantPlaces.FAR_SPOT),.08);
                gesture(a,0);var r=nearest(a,readers);if(r!=null)face(a,r.position());
                boolean waiting=readers.stream().anyMatch(p->nearSide(b,p)),company=readers.stream().anyMatch(p->p.distanceToSqr(a)<25);
                if(waiting&&!company){sc.crossing=3;sc.leg=0;}
            }
            case 3->{
                // Back over to the near end along the ledge: to its far end, along it, and back to his place.
                gesture(a,0);
                Vec3[] legs={new Vec3(x,y,b.getZ()+TruantPlaces.LEDGE_FAR-1.2),new Vec3(x,y,b.getZ()+TruantPlaces.LEDGE_NEAR+1.5),Vec3.atLowerCornerOf(b).add(TruantPlaces.LEDGE_SPOT)};
                var to=legs[Math.min(sc.leg,legs.length-1)];walk(a,to,sc.leg==1?.14:.1);face(a,to);
                if(a.position().distanceToSqr(to)<.01&&++sc.leg>=legs.length){sc.crossing=0;sc.leg=0;}
            }
            default->{}
        }
    }

    // ------------------------------------------------------------------ the lamp
    /** The edge of his face that shows round the corner, where a reader's eye has to rest to have seen it. */
    static Vec3 sliver(BlockPos b){return Vec3.atLowerCornerOf(b).add(TruantPlaces.PEEK_SPOT.x+.33,1.55,TruantPlaces.PEEK_SPOT.z);}
    static boolean looking(ServerLevel l,ServerPlayer p,Vec3 at){
        var eye=p.getEyePosition();var d=at.subtract(eye);double len=d.length();if(len>18||len<1e-3)return false;
        if(p.getViewVector(1).dot(d.scale(1/len))<.97)return false;
        var hit=l.clip(new ClipContext(eye,at,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));
        return hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceToSqr(eye)>=len*len-.01;
    }
    /**
     * The first sight of him is the edge of a face round the inner corner: one eye, part of the nose, half a mouth, the leer. It stays
     * until someone has looked at it (or comes close), then slowly draws back. Round the corner he waits back to the wall, then goes
     * to his lamp, where he has his ordinary face and makes room: "Stay a minute. Listen before you go."
     */
    static void lamp(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers){
        var sc=scene;var base=Vec3.atLowerCornerOf(b);
        switch(sc.lamp){
            case 0->{
                if(a.phase()!=LEER)a.appearance(LiteraryActor.STRANGER,LEER);
                if(a.position().distanceToSqr(base.add(TruantPlaces.PEEK_SPOT))>.01)place(a,b,TruantPlaces.PEEK_SPOT,a.getYRot());
                gesture(a,PEEK);var r=nearest(a,readers);if(r!=null)face(a,r.getEyePosition());
                boolean seen=false,close=false;var at=sliver(b);
                for(var p:readers){
                    if(p.distanceToSqr(a)<CLOSE*CLOSE)close=true;
                    if(looking(l,p,at)){int n=sc.looked.merge(p.getUUID(),1,Integer::sum);if(n==LOOK){seen=true;var own=record(p.server,p.getUUID());own.putInt("Peeked",own.getInt("Peeked")+1);record(p.server,p.getUUID(),own);}}
                }
                // 0.4.282, the half smile: a claimant leans further out into the passage before he draws back.
                if(seen||close)sc.lamp=sc.turn==Turn.CLAIM&&target(readers)!=null?4:1;
            }
            case 4->{
                var t=target(readers);gesture(a,PEEK);
                if(t!=null){face(a,t.getEyePosition());var d=t.position().subtract(base.add(TruantPlaces.PEEK_SPOT));d=new Vec3(d.x,0,d.z);
                    if(d.lengthSqr()>1e-3)walk(a,base.add(TruantPlaces.PEEK_SPOT).add(d.normalize().scale(Math.min(1.1,Math.max(0,d.length()-1.4)))),.04);}
                if(++sc.lean==30)say(a,TruantTexts.LEAN);
                if(sc.lean>=70){sc.lamp=1;gesture(a,PEEK);}
            }
            case 1->{
                // Slowly back behind the corner, still looking.
                var r=nearest(a,readers);if(r!=null)face(a,r.getEyePosition());
                var to=base.add(TruantPlaces.PEEK_SPOT.x-1.35,0,TruantPlaces.PEEK_SPOT.z);walk(a,to,.03);
                if(a.position().distanceToSqr(to)<.01){gesture(a,0);sc.lamp=2;sc.waited=0;}
            }
            case 2->{
                var spot=base.add(TruantPlaces.WALL_SPOT);walk(a,spot,.06);
                if(a.position().distanceToSqr(spot)<.01){
                    // Back to the wall, sideways to whoever comes round.
                    a.setYRot(180);a.setYBodyRot(180);a.setYHeadRot(180);if(a.phase()!=FACE)a.appearance(LiteraryActor.STRANGER,FACE);
                    boolean coming=readers.stream().anyMatch(p->p.distanceToSqr(a)<16);
                    if(++sc.waited>=(coming?20:50))sc.lamp=3;
                }
            }
            case 3->{
                var t=target(readers);
                if(sc.turn==Turn.COVER&&t!=null&&sc.cover>0&&sc.cover<4){cover(l,b,a,t,readers);break;}
                if(sc.dark){
                    // The lamp put out for him: he goes past the alcove and looks into the dark.
                    var spot=base.add(TruantPlaces.DARK_SPOT);walk(a,spot,.07);face(a,spot.add(0,0,-2));gesture(a,0);
                    if(a.position().distanceToSqr(spot)<.01&&!sc.saidOut){sc.saidOut=true;say(a,TruantTexts.OUT);}
                    break;
                }
                var spot=base.add(TruantPlaces.LAMP_SPOT);walk(a,spot,.07);
                if(sc.turn==Turn.COVER&&t!=null&&sc.cover==0&&a.position().distanceToSqr(spot)<.01&&t.distanceToSqr(a)<2.6*2.6&&!gated(readers)&&!withdrawn(t)){sc.cover=1;sc.coverTicks=0;cover(l,b,a,t,readers);break;}
                if(a.position().distanceToSqr(spot)<.01){
                    var r=nearest(a,readers);if(r!=null)face(a,r.position());else{a.setYRot(0);a.setYBodyRot(0);a.setYHeadRot(0);}
                    boolean any=false;for(var p:readers)if(!withdrawn(p)&&p.distanceToSqr(a)<3.5*3.5){any=true;if(!HELPED.contains(p.getUUID())){helped(p,HELP_LAMP);ASKED.add(p.getUUID());}}
                    if(any&&!sc.greeted){sc.greeted=true;say(a,TruantTexts.LAMP);}
                    // A claimant is bent over a page by the light, while the one he wants it from is by him.
                    gesture(a,sc.turn==Turn.CLAIM&&t!=null&&t.distanceToSqr(a)<3.5*3.5?PAGE:any?LAMP:0);
                }
            }
            default->{}
        }
    }

    // ------------------------------------------------------------------ the panel
    /** After he has helped someone and asked, the panel opens for them once they stand by him a moment. */
    static void panel(LiteraryActor a,List<ServerPlayer> readers){
        for(var p:readers){
            var id=p.getUUID();
            if(!ASKED.contains(id)||withdrawn(p)||Talk.waiting(p)||p.distanceToSqr(a)>9){NEAR.remove(id);continue;}
            if(NEAR.merge(id,1,Integer::sum)>=20){NEAR.remove(id);ASKED.remove(id);var own=record(p.server,id);if(own.getInt("Talked")<meeting(own))converse(p,a);}
        }
    }
    /** Right-clicking him. */
    public static void talk(ServerPlayer p,LiteraryActor a){
        if(!reader(p)||!a.isAlive()||p.distanceToSqr(a)>25)return;
        if(withdrawn(p)||scene!=null&&a.level().getGameTime()<scene.flinchUntil)return;
        // 0.4.282: waiting at the crossing to come up behind someone, he will not be drawn into talk.
        if(scene!=null&&scene.turn==Turn.SHOVE){say(a,TruantTexts.BEHIND);return;}
        if(Talk.resume(p,KEY))return;
        if(WITH.get(p.getUUID())==null)met(p,a,a.level().getGameTime());
        var own=record(p.server,p.getUUID());
        if(own.getInt("Talked")>=meeting(own)){say(a,TruantTexts.again(own));return;}
        ASKED.remove(p.getUUID());converse(p,a);
    }
    static void line(ServerPlayer p,LiteraryActor a,String who,String text){Talk.line(p,who,text);say(a,text);}
    static String who(CompoundTag own){return own.getBoolean("Named")?"Johnny":"The man";}
    /** The exchange: what he remembers, his question, and the replies. */
    static void open(ServerPlayer p,LiteraryActor a){
        var server=p.server;var id=p.getUUID();var own=record(server,id);int meeting=meeting(own);
        if(harmed(own)){afterHarm(p,a);return;}
        boolean first=own.getInt("Talked")==0;own.putInt("Talked",meeting);record(server,id,own);
        String who=who(own);Talk.begin(p,KEY,who);
        var place=LabyrinthPlace.byId(a.scene());
        for(var said:TruantTexts.remembers(own,first,place))line(p,a,who,said);
        // 0.4.282: past twenty, he is wearing: he repeats a question already answered, tells their story as his, misremembers.
        if(!first&&collecting(own,deepest(server,id))){var worn=TruantTexts.worn(own,worn(own));if(worn!=null)line(p,a,who,worn);}
        // A kept promise is thanked for once.
        if(own.getBoolean("Kept")&&!own.getBoolean("Thanked")){own=record(server,id);own.putBoolean("Thanked",true);record(server,id,own);}
        line(p,a,who,place==TruantPlaces.DOOR?TruantTexts.ASK_DOOR:TruantTexts.ASK);
        ask(p,a);
    }
    static void ask(ServerPlayer p,LiteraryActor a){
        var own=record(p.server,p.getUUID());String last=lastStory(p.server,p.getUUID());
        List<Talk.Reply> replies=new ArrayList<>();
        if(!last.isEmpty())replies.add(Talk.reply("Tell him about "+StaircaseProse.place(last)+".",q->tell(q,a,last)));
        replies.add(Talk.reply(last.isEmpty()?"Nothing I can put into words.":"I'd rather not.",q->decline(q,a)));
        if(!own.getBoolean("Named"))replies.add(Talk.reply("Who are you?",q->name(q,a)));
        Talk.ask(p,replies);
    }
    static void tell(ServerPlayer p,LiteraryActor a,String placeId){tell(p,a,placeId,q->promise(q,a));}
    static void tell(ServerPlayer p,LiteraryActor a,String placeId,java.util.function.Consumer<ServerPlayer> after){
        var server=p.server;var id=p.getUUID();var own=record(server,id);String who=who(own);
        var told=own.getList("Told",Tag.TAG_STRING);told.add(StringTag.valueOf(placeId));while(told.size()>16)told.remove(0);own.put("Told",told);
        String promise=own.getString("Promise");
        if((promise.equals("agreed")||promise.equals("maybe"))&&!own.getBoolean("Kept")&&meeting(own)>own.getInt("PromisedAt")){own.putBoolean("Kept",true);own.putString("KeptPlace",placeId);own.putInt("KeptAt",meeting(own));}
        record(server,id,own);
        var follow=TruantTexts.follow(placeId);
        line(p,a,who,TruantTexts.heard(placeId));line(p,a,who,follow.question());
        List<Talk.Reply> replies=new ArrayList<>();
        for(int i=0;i<follow.answers().length;i++){int k=i;replies.add(Talk.reply(follow.answers()[i],q->{answered(q,placeId,k);line(q,a,who,follow.replies()[k]);after.accept(q);}));}
        Talk.ask(p,replies);
    }
    static void decline(ServerPlayer p,LiteraryActor a){
        var own=record(p.server,p.getUUID());own.putInt("Declined",own.getInt("Declined")+1);record(p.server,p.getUUID(),own);
        line(p,a,who(own),TruantTexts.DECLINED);promise(p,a);
    }
    static void name(ServerPlayer p,LiteraryActor a){
        var own=record(p.server,p.getUUID());String who=who(own);own.putBoolean("Named",true);record(p.server,p.getUUID(),own);
        for(var said:TruantTexts.NAME)line(p,a,who,said);
        ask(p,a);
    }
    /** At the end he may ask for a promise; what the reader says is kept as said. Once made, it is not asked for again. */
    static void promise(ServerPlayer p,LiteraryActor a){
        var own=record(p.server,p.getUUID());String who=who(own);
        if(!own.getString("Promise").isEmpty()){line(p,a,who,TruantTexts.GOODBYE);Talk.end(p);return;}
        line(p,a,who,TruantTexts.PROMISE);
        Talk.ask(p,
            Talk.reply("I will.",q->promised(q,a,"agreed")),
            Talk.reply("Maybe.",q->promised(q,a,"maybe")),
            Talk.reply("No.",q->promised(q,a,"refused")));
    }
    static void promised(ServerPlayer p,LiteraryActor a,String said){
        var own=record(p.server,p.getUUID());own.putString("Promise",said);own.putInt("PromisedAt",meeting(own));record(p.server,p.getUUID(),own);
        line(p,a,who(own),TruantTexts.promised(said));Talk.end(p);
    }

    // ------------------------------------------------------------------ struck
    /** Struck, he flinches and keeps his distance from that reader for the rest of the meeting. He cannot die. */
    public static void struck(ServerPlayer p,LiteraryActor a){
        if(!reader(p))return;
        WITHDRAWN.add(p.getUUID());ASKED.remove(p.getUUID());Talk.close(p);
        var own=record(p.server,p.getUUID());own.putInt("Struck",own.getInt("Struck")+1);record(p.server,p.getUUID(),own);
        if(scene!=null){scene.flinchUntil=a.level().getGameTime()+WITHDRAW;if(p.getUUID().equals(scene.steadied)){scene.steadied=null;scene.crossing=3;scene.leg=1;}
            // 0.4.282: struck, he gives up whatever he was doing to them: steps out of the doorway, lets go of the wrist, stops coming.
            var sc=scene;
            if(sc.turn==Turn.DOORWAY&&!sc.aside){sc.aside=true;sc.holding=false;}
            if(sc.turn==Turn.COVER&&sc.cover>0&&sc.cover<4&&a.level() instanceof ServerLevel l){var b=base(l,TruantPlaces.LAMP);if(b!=null)release(l,b,a,sc.target==null?null:l.getServer().getPlayerList().getPlayer(sc.target));}
            if(sc.turn==Turn.SHOVE&&sc.shove>=1&&sc.shove<=2){sc.shove=4;sc.shoveTicks=0;}}
        a.gesture(FLINCH);say(a,TruantTexts.STRUCK);
        a.level().playSound(null,a.blockPosition(),SoundEvents.PLAYER_HURT,SoundSource.NEUTRAL,.6F,.9F);
    }


    // ------------------------------------------------------------------ 0.4.282: what he is at a meeting
    static @Nullable Turn FORCED;
    /** Past twenty passes now (the House's saved maximum, which backtracking does not lower). */
    static boolean veteran(ServerPlayer p){return deepest(p.server,p.getUUID())>=TWENTY;}
    /** Twenty passes noted at an earlier meeting than this one: collection is possible for them now, never on the meeting it was reached. */
    static boolean collecting(CompoundTag own,int deepest){return deepest>=TWENTY&&own.getBoolean("Twenty")&&meeting(own)>own.getInt("TwentyAt");}
    /** How many meetings since twenty passes were noted: how worn he is with them. */
    static int worn(CompoundTag own){return own.getBoolean("Twenty")?Math.max(0,meeting(own)-own.getInt("TwentyAt")):0;}
    /** The multiplayer gate: nothing hostile while anyone under twenty passes is in the place with him. */
    static boolean gated(List<ServerPlayer> readers){for(var p:readers)if(!veteran(p))return true;return false;}
    /** He has harmed them since he last forbore with them. */
    static boolean harmed(CompoundTag own){return own.getInt("HarmAt")>own.getInt("ForboreAt");}
    /** The shove is rare: only someone he once steadied on the crossing, at a meeting after collection began, and never within five meetings of the last. */
    static boolean shoveDue(CompoundTag own){return (own.getInt("Help")&HELP_STEADY)!=0&&own.getInt("Revealed")>0&&meeting(own)>own.getInt("Revealed")&&(own.getInt("Shoved")==0||meeting(own)-own.getInt("ShovedAt")>=5);}
    static @Nullable ServerPlayer target(List<ServerPlayer> readers){var sc=scene;if(sc==null||sc.target==null)return null;for(var p:readers)if(p.getUUID().equals(sc.target))return p;return null;}
    static @Nullable BlockPos base(ServerLevel l,LabyrinthPlace place){var origin=HouseSavedData.get(l.getServer()).houseOrigin();return origin==null?null:LabyrinthPlaces.base(origin,place);}
    static boolean has(ListTag list,String value){for(int i=0;i<list.size();i++)if(list.getString(i).equals(value))return true;return false;}

    /**
     * What he is this meeting, chosen as the place is come into. The one he turns to is the reader past twenty with the longest history
     * with him, if any; the first such meeting shows it (the door, or a claim elsewhere); afterwards he is often as he was, sometimes a
     * claimant, sometimes the place's own hostile form, and after harm he mostly helps. With anyone under twenty there, nothing hostile.
     */
    static void choose(Scene sc,List<ServerPlayer> readers,RandomSource r){
        sc.turn=Turn.HELP;sc.target=null;
        ServerPlayer target=null;CompoundTag tOwn=null;
        for(var p:readers){if(withdrawn(p))continue;var own=record(p.server,p.getUUID());if(!collecting(own,deepest(p.server,p.getUUID())))continue;if(tOwn==null||meeting(own)>meeting(tOwn)){target=p;tOwn=own;}}
        if(target==null)return;
        var place=sc.place;
        Turn hostile=place==TruantPlaces.DOOR?Turn.DOORWAY:place==TruantPlaces.LAMP?Turn.COVER:Turn.SHOVE;
        Turn want;
        if(FORCED!=null)want=FORCED;
        else if(tOwn.getInt("Revealed")==0)want=place==TruantPlaces.DOOR?Turn.DOORWAY:Turn.CLAIM;
        else if(harmed(tOwn))want=r.nextInt(10)<7?Turn.HELP:Turn.CLAIM;
        else{int k=r.nextInt(10);want=k<4?Turn.HELP:k<7?Turn.CLAIM:hostile;}
        if(want==Turn.SHOVE&&FORCED==null&&!shoveDue(tOwn))want=Turn.CLAIM;
        if(want!=Turn.HELP&&want!=Turn.CLAIM&&want!=hostile)want=Turn.CLAIM;
        if(want==hostile&&gated(readers))want=Turn.CLAIM;
        sc.turn=want;if(want==Turn.HELP)return;
        sc.target=target.getUUID();
        var own=record(target.server,target.getUUID());if(own.getInt("Revealed")==0)own.putInt("Revealed",meeting(own));
        var turns=own.getList("Turns",Tag.TAG_STRING);turns.add(StringTag.valueOf(want.name().toLowerCase(Locale.ROOT)+"@"+meeting(own)));while(turns.size()>16)turns.remove(0);own.put("Turns",turns);
        record(target.server,target.getUUID(),own);
    }
    /** Something he did to them: counted, and the meeting and kind kept, so a later meeting can answer for it. */
    static void harm(ServerPlayer p,String kind,String count){var own=record(p.server,p.getUUID());own.putInt(count,own.getInt(count)+1);own.putInt("HarmAt",meeting(own));own.putString("Harm",kind);record(p.server,p.getUUID(),own);}

    // ------------------------------------------------------------------ the hand in the doorway
    /**
     * He once held this door. Now he stands in its opening with a hand up on the frame, the leer on him, and asks before he lets the one
     * he wants through. Others pass him. Told something, he steps aside. Pushed past, the first time he shoves them back out of the
     * doorway; the second time he gives way. Struck, or with anyone under twenty there, he steps aside.
     */
    static void doorway(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers){
        var sc=scene;var t=target(readers);
        if(t==null||gated(readers)||withdrawn(t)){aside(l,b,a);return;}
        if(a.phase()!=LEER)a.appearance(LiteraryActor.STRANGER,LEER);
        if(a.position().distanceToSqr(Vec3.atLowerCornerOf(b).add(TruantPlaces.IN_DOORWAY))>.01)place(a,b,TruantPlaces.IN_DOORWAY,0);
        if(sc.shoveTicks>0)sc.shoveTicks--;
        gesture(a,sc.shoveTicks>0?PUSH:FRAME);face(a,t.position());
        // The door open behind him: the closer cannot shut it while he stands in it.
        sc.holding=true;if(!doorOpen(l,b))swing(l,b,true);
        var own=record(t.server,t.getUUID());
        if(own.getInt("Talked")<meeting(own)&&!Talk.waiting(t)&&side(b,t)>0&&t.distanceToSqr(a)<3.5*3.5)claim(t,a);
        boolean in=inDoorway(b,t);
        if(in&&!sc.inDoor){
            if(++sc.struggles==1){
                double z=b.getZ()+TruantPlaces.HEAVY_DOOR.getZ()+1.42;
                t.teleportTo(l,Math.max(b.getX()-.2,Math.min(b.getX()+1.2,t.getX())),t.getY(),z,t.getYRot(),t.getXRot());
                t.setDeltaMovement(0,.1,.35);t.hurtMarked=true;
                l.playSound(null,t.blockPosition(),SoundEvents.PLAYER_ATTACK_KNOCKBACK,SoundSource.PLAYERS,.8F,.9F);
                sc.shoveTicks=8;gesture(a,PUSH);say(a,TruantTexts.NOT_YET);harm(t,"doorway","Struggled");in=false;
            }else{say(a,TruantTexts.GIVE_WAY);aside(l,b,a);}
        }
        sc.inDoor=in;
    }
    /** Out of the doorway to his place beside it, where he holds the door as at any other meeting. */
    static void aside(ServerLevel l,BlockPos b,LiteraryActor a){
        var sc=scene;sc.aside=true;sc.holding=false;sc.idle=0;
        if(a.phase()!=FACE)a.appearance(LiteraryActor.STRANGER,FACE);
        place(a,b,TruantPlaces.DOOR_SPOT,a.getYRot());gesture(a,0);
    }

    // ------------------------------------------------------------------ the shove
    static boolean lookingAt(ServerPlayer p,LiteraryActor a){var d=a.getEyePosition().subtract(p.getEyePosition());double len=d.length();return len>1e-3&&len<12&&p.getViewVector(1).dot(d.scale(1/len))>.8;}
    /**
     * He waits in the dark end of the near landing. When the one he wants is a few steps out along the ledge he comes up behind them:
     * a scrape of a shoe as he starts, footsteps, a breath close behind. Turning to look at him, stepping off the ledge, a friend coming
     * near him, or a blow stops him, hand still up. Otherwise his hand comes to rest where it rested the first time, between their
     * shoulders, and then both hands push. The fall is the crossing's own. He looks over, says one word, and goes back the way he came.
     */
    static void shove(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers){
        var sc=scene;var base=Vec3.atLowerCornerOf(b);var t=target(readers);
        if(a.phase()!=FACE)a.appearance(LiteraryActor.STRANGER,FACE);
        double x=b.getX()+TruantPlaces.LEDGE_X+.5,y=b.getY();var end=new Vec3(x,y,b.getZ()+TruantPlaces.LEDGE_NEAR+1.4);
        sc.shoveTicks++;
        switch(sc.shove){
            case 0->{
                if(a.position().distanceToSqr(base.add(TruantPlaces.SHOVE_WAIT))>.01)place(a,b,TruantPlaces.SHOVE_WAIT,-90);
                gesture(a,0);
                if(t!=null&&!gated(readers)&&!withdrawn(t)&&onLedge(b,t)&&rel(b,t.position()).z<TruantPlaces.LEDGE_NEAR-1.5){
                    sc.shove=1;sc.shoveTicks=0;sc.leg=0;sc.turned=0;
                    l.playSound(null,a.blockPosition(),SoundEvents.STONE_STEP,SoundSource.NEUTRAL,.6F,.7F);
                }
            }
            case 1,2->{
                if(t==null||!onLedge(b,t)){stop(a,t,null,"aside");break;}
                if(gated(readers)){stop(a,t,null,"gated");break;}
                ServerPlayer friend=null;for(var p:readers)if(p!=t&&p.distanceToSqr(a)<1.6*1.6)friend=p;
                if(friend!=null){stop(a,t,friend,"interrupted");break;}
                sc.turned=lookingAt(t,a)?sc.turned+1:0;
                if(sc.turned>=TURN_BACK){stop(a,t,null,"turned");break;}
                var behind=new Vec3(x,y,Math.min(end.z,t.getZ()+1.05));
                if(sc.shove==1){
                    if(sc.leg==0){walk(a,end,.2);face(a,end);if(a.position().distanceToSqr(end)<.01)sc.leg=1;}
                    else{walk(a,behind,.17);face(a,t.position());}
                    gesture(a,0);
                    if(sc.shoveTicks%12==6)l.playSound(null,a.blockPosition(),SoundEvents.STONE_STEP,SoundSource.NEUTRAL,.35F,.75F);
                    if(!sc.breathed&&a.distanceToSqr(t)<9){sc.breathed=true;l.playSound(null,t.blockPosition(),HorrorRegistry.STORE_BREATH.get(),SoundSource.NEUTRAL,.6F,1.05F);}
                    if(sc.leg==1&&sc.shoveTicks>=APPROACH&&a.position().distanceToSqr(behind)<.04){sc.shove=2;sc.shoveTicks=0;}
                }else{
                    a.moveTo(behind.x,behind.y,behind.z,180,0);face(a,t.position());gesture(a,STEADY);
                    if(sc.shoveTicks>=HANDS)push(l,b,a,t,readers);
                }
            }
            case 3->{
                var edge=new Vec3(x-.2,y,sc.pushZ+.2);walk(a,edge,.08);a.setYRot(90);a.setYBodyRot(90);a.setYHeadRot(90);gesture(a,OVER);
                boolean down=t==null||!t.isAlive()||t.onGround()&&rel(b,t.position()).y<-3;
                if(!sc.careful&&(down||sc.shoveTicks>=LOOK_OVER)){sc.careful=true;sc.shoveTicks=LOOK_OVER;say(a,TruantTexts.CAREFUL);}
                if(sc.careful&&sc.shoveTicks>=LOOK_OVER+50){sc.shove=5;sc.shoveTicks=0;sc.leg=0;gesture(a,0);}
            }
            case 4->{
                gesture(a,STEADY);if(t!=null)face(a,t.position());
                if(sc.shoveTicks>=20){sc.shove=5;sc.shoveTicks=0;sc.leg=a.getZ()<end.z-.3?0:1;gesture(a,0);}
            }
            case 5->{
                Vec3[] legs={end,base.add(TruantPlaces.SHOVE_WAIT)};var to=legs[Math.min(sc.leg,1)];walk(a,to,.1);face(a,to);gesture(a,0);
                if(a.position().distanceToSqr(to)<.01&&++sc.leg>=2)sc.shove=6;
            }
            default->{if(a.position().distanceToSqr(base.add(TruantPlaces.SHOVE_WAIT))>.01)place(a,b,TruantPlaces.SHOVE_WAIT,-90);gesture(a,0);}
        }
    }
    /** He stops before his hands land: why, kept for the one he was coming for, and for a friend who came between. */
    static void stop(LiteraryActor a,@Nullable ServerPlayer t,@Nullable ServerPlayer by,String why){
        var sc=scene;sc.shove=4;sc.shoveTicks=0;
        if(t!=null&&!why.equals("gated")){var own=record(t.server,t.getUUID());own.putInt("Stopped",own.getInt("Stopped")+1);own.putString("StoppedBy",why);record(t.server,t.getUUID(),own);}
        if(by!=null){var own=record(by.server,by.getUUID());own.putInt("Interrupted",own.getInt("Interrupted")+1);record(by.server,by.getUUID(),own);}
        if(why.equals("turned"))say(a,TruantTexts.STEADY);
    }
    static void push(ServerLevel l,BlockPos b,LiteraryActor a,ServerPlayer t,List<ServerPlayer> readers){
        var sc=scene;sc.shove=3;sc.shoveTicks=0;sc.pushZ=t.getZ();gesture(a,PUSH);
        // West, off the ledge over the drop, and up off their feet so a crouch cannot hold the edge. The fall is the place's own.
        t.setDeltaMovement(-.8,.32,0);t.hurtMarked=true;t.setOnGround(false);
        l.playSound(null,t.blockPosition(),SoundEvents.PLAYER_ATTACK_KNOCKBACK,SoundSource.PLAYERS,1F,.8F);
        harm(t,"shove","Shoved");var own=record(t.server,t.getUUID());own.putInt("ShovedAt",meeting(own));record(t.server,t.getUUID(),own);
        for(var p:readers)if(p!=t){var w=record(p.server,p.getUUID());w.putInt("Witnessed",w.getInt("Witnessed")+1);w.putInt("WitnessedAt",meeting(w));record(p.server,p.getUUID(),w);}
    }

    // ------------------------------------------------------------------ the covered light
    /**
     * At his lamp, close to the one he wants, he reaches across it and closes his hand round it: the alcove goes dark, and they see
     * almost nothing. He steps across their path, between them and the way back to the turn, and takes their wrist; he asks; he lets go,
     * and the lamp is lit again. A blow ends it at once. Nothing of this with anyone under twenty there.
     */
    static void cover(ServerLevel l,BlockPos b,LiteraryActor a,ServerPlayer t,List<ServerPlayer> readers){
        var sc=scene;sc.coverTicks++;
        if(gated(readers)||withdrawn(t)){release(l,b,a,t);return;}
        var lamp=b.offset(TruantPlaces.LAMP_LIGHT);
        switch(sc.cover){
            case 1->{
                gesture(a,REACH);face(a,Vec3.atCenterOf(lamp));
                if(sc.coverTicks==6){darken(l,lamp,TruantPlaces.LAMP,l.getGameTime()+COVER_DARK);t.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,COVER_DARK,0,false,false,false));l.playSound(null,lamp,SoundEvents.LANTERN_HIT,SoundSource.BLOCKS,.7F,.6F);}
                if(sc.coverTicks>=10){sc.cover=2;sc.coverTicks=0;}
            }
            case 2->{
                var across=new Vec3(Math.min(t.getX()+1.1,b.getX()-2.4),b.getY(),Math.max(b.getZ()-8.7,Math.min(b.getZ()-7.3,t.getZ())));
                walk(a,across,.22);face(a,t.position());gesture(a,REACH);
                if(a.position().distanceToSqr(across)<.01||sc.coverTicks>=12){
                    sc.cover=3;sc.coverTicks=0;
                    t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,WRIST_HOLD,6,false,false,false));
                    say(a,TruantTexts.WRIST);harm(t,"cover","Covered");
                }
            }
            case 3->{face(a,t.position());gesture(a,REACH);if(sc.coverTicks>=WRIST_HOLD)release(l,b,a,t);}
            default->{}
        }
    }
    /** He lets go: the lamp lit again, his effects gone, and the one he held asked what he asks. */
    static void release(ServerLevel l,BlockPos b,LiteraryActor a,@Nullable ServerPlayer t){
        var sc=scene;if(sc!=null)sc.cover=4;relightAt(l,b.offset(TruantPlaces.LAMP_LIGHT));gesture(a,0);
        if(t==null)return;
        var slow=t.getEffect(MobEffects.MOVEMENT_SLOWDOWN);if(slow!=null&&slow.getAmplifier()==6&&slow.getDuration()<=WRIST_HOLD)t.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        var blind=t.getEffect(MobEffects.BLINDNESS);if(blind!=null&&blind.getDuration()<=COVER_DARK)t.removeEffect(MobEffects.BLINDNESS);
        ASKED.add(t.getUUID());
    }

    // ------------------------------------------------------------------ a lamp put out
    /** Puts out one of his lamps until a time, or until the place is empty; saved, so no lamp is ever lost to a stop or a crash. */
    static void darken(ServerLevel l,BlockPos pos,LabyrinthPlace place,long until){
        if(!l.getBlockState(pos).is(Blocks.LANTERN))return;
        var s=state(l.getServer());var list=s.getList("Dark",Tag.TAG_COMPOUND);var e=new CompoundTag();e.putLong("Pos",pos.asLong());e.putString("Place",place.id());e.putLong("Until",until);list.add(e);s.put("Dark",list);save(l.getServer(),s);
        l.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
    }
    static void relightAt(ServerLevel l,BlockPos pos){
        var s=state(l.getServer());var list=s.getList("Dark",Tag.TAG_COMPOUND);boolean had=false;
        for(int i=list.size()-1;i>=0;i--)if(list.getCompound(i).getLong("Pos")==pos.asLong()){list.remove(i);had=true;}
        if(!had)return;s.put("Dark",list);save(l.getServer(),s);
        if(l.isLoaded(pos)&&l.getBlockState(pos).isAir())l.setBlock(pos,Blocks.LANTERN.defaultBlockState(),3);
    }
    /** Lights again every lamp that is due: its time is up, its place is empty, or (all) the server is stopping. A cell someone has filled is left as it is. */
    static void relight(ServerLevel l,@Nullable Map<LabyrinthPlace,List<ServerPlayer>> here,boolean all){
        var s=state(l.getServer());var list=s.getList("Dark",Tag.TAG_COMPOUND);if(list.isEmpty())return;long now=l.getGameTime();boolean changed=false;
        for(int i=list.size()-1;i>=0;i--){
            var e=list.getCompound(i);var pos=BlockPos.of(e.getLong("Pos"));var place=LabyrinthPlace.byId(e.getString("Place"));
            boolean due=all||now>=e.getLong("Until")||place==null||here==null||!here.containsKey(place);
            if(!due||!l.isLoaded(pos))continue;
            if(l.getBlockState(pos).isAir())l.setBlock(pos,Blocks.LANTERN.defaultBlockState(),3);
            list.remove(i);changed=true;
        }
        if(changed){s.put("Dark",list);save(l.getServer(),s);}
    }

    // ------------------------------------------------------------------ favours
    static int ledgeSide(BlockPos b,ServerPlayer p){return nearSide(b,p)?1:across(b,p)?-1:0;}
    /** Favours taken on this meeting, done when the reader has done them. */
    static void favours(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers){
        var sc=scene;if(sc.favour.isEmpty())return;
        for(var p:readers){
            var id=p.getUUID();var what=sc.favour.get(id);if(what==null)continue;
            switch(what){
                case "go_back"->{if(sc.place==TruantPlaces.DOOR&&side(b,p)>0){sc.favour.put(id,"went_back");did(p,"go_back");say(a,TruantTexts.WELL);}}
                case "cross_alone"->{
                    if(sc.place!=TruantPlaces.LEDGE)break;int side=ledgeSide(b,p);var from=sc.favourSide.get(id);
                    if(from==null){if(side!=0)sc.favourSide.put(id,side);}
                    else if(side!=0&&side!=from){sc.favour.put(id,"crossed");did(p,"cross_alone");say(a,TruantTexts.WATCHING);}
                }
                default->{}
            }
        }
    }
    static void did(ServerPlayer p,String what){
        var own=record(p.server,p.getUUID());var list=own.getList("Favours",Tag.TAG_COMPOUND);
        for(int i=list.size()-1;i>=0;i--){var f=list.getCompound(i);if(f.getString("What").equals(what)&&f.getInt("At")==meeting(own)){f.putBoolean("Did",true);break;}}
        own.put("Favours",list);record(p.server,p.getUUID(),own);
    }

    // ------------------------------------------------------------------ the claimant's panel
    /** The one he has turned to this meeting gets the claimant; everyone else, the man they know. */
    static void converse(ServerPlayer p,LiteraryActor a){
        var sc=scene;
        if(sc!=null&&sc.turn!=null&&sc.turn!=Turn.HELP&&sc.turn!=Turn.SHOVE&&p.getUUID().equals(sc.target))claim(p,a);else open(p,a);
    }
    static String lastTold(CompoundTag own){var told=own.getList("Told",Tag.TAG_STRING);return told.isEmpty()?"":told.getString(told.size()-1);}
    static void answered(ServerPlayer p,String placeId,int k){var own=record(p.server,p.getUUID());var ans=own.getCompound("Answers");ans.putInt(placeId,k);own.put("Answers",ans);record(p.server,p.getUUID(),own);}
    /**
     * The claimant: his papers first, if he has written up something they told him (at his lamp, or once he is worn); then what he
     * claims, which is his and is kept apart from what they said ("Promise") and what he did ("Help"); then the collector's question
     * about a place they already told him of. They can correct him, tell him something new, tell it again, say they already have, or
     * set a limit.
     */
    static void claim(ServerPlayer p,LiteraryActor a){
        var server=p.server;var id=p.getUUID();var own=record(server,id);int meeting=meeting(own);
        own.putInt("Talked",meeting);String who=who(own);Talk.begin(p,KEY,who);
        var sc=scene;var place=LabyrinthPlace.byId(a.scene());
        if(sc!=null&&sc.turn==Turn.DOORWAY&&!sc.aside)line(p,a,who,TruantTexts.NOT_YET);
        var page=TruantTexts.page(own);boolean read=page!=null&&(place==TruantPlaces.LAMP||worn(own)>=2);
        if(read){
            Talk.line(p,"",TruantTexts.PAGE_NOTE);for(var said:page.lines())line(p,a,who,said);
            var papers=own.getList("Papers",Tag.TAG_STRING);if(!has(papers,page.place()))papers.add(StringTag.valueOf(page.place()));own.put("Papers",papers);
        }
        var claim=TruantTexts.claim(own);for(var said:claim.lines())line(p,a,who,said);
        var claims=own.getList("Claims",Tag.TAG_COMPOUND);var c=new CompoundTag();c.putString("Claim",claim.id());c.putInt("At",meeting);claims.add(c);while(claims.size()>16)claims.remove(0);own.put("Claims",claims);
        record(server,id,own);
        String known=lastTold(own);
        line(p,a,who,known.isEmpty()?TruantTexts.ASK:TruantTexts.collect(known,meeting));
        List<Talk.Reply> replies=new ArrayList<>();
        if(read)replies.add(Talk.reply("That happened to me.",q->answer(q,a,"Corrected",TruantTexts.CORRECTED)));
        String last=lastStory(server,id);
        if(!last.isEmpty()&&!has(own.getList("Told",Tag.TAG_STRING),last))replies.add(Talk.reply("Tell him about "+StaircaseProse.place(last)+".",q->tell(q,a,last,r->then(r,a))));
        if(!known.isEmpty()){
            replies.add(Talk.reply("Tell him about "+StaircaseProse.place(known)+" again.",q->answer(q,a,"Retold",TruantTexts.RETOLD)));
            replies.add(Talk.reply("I already told you that.",q->challenged(q,a)));
        }
        replies.add(Talk.reply("That's all you get.",q->limited(q,a)));
        Talk.ask(p,replies);
    }
    static void answer(ServerPlayer p,LiteraryActor a,String count,List<String> lines){
        var own=record(p.server,p.getUUID());own.putInt(count,own.getInt(count)+1);record(p.server,p.getUUID(),own);
        for(var said:lines)line(p,a,who(own),said);
        then(p,a);
    }
    /** Told he has had it already: sometimes he knows it, and asks again anyway; more often he finds a part they left out. */
    static void challenged(ServerPlayer p,LiteraryActor a){
        var own=record(p.server,p.getUUID());int n=own.getInt("Challenged")+1;own.putInt("Challenged",n);record(p.server,p.getUUID(),own);
        for(var said:worn(own)>=3||n>=2?TruantTexts.CHALLENGED_LUCID:TruantTexts.CHALLENGED)line(p,a,who(own),said);
        then(p,a);
    }
    static void limited(ServerPlayer p,LiteraryActor a){
        var own=record(p.server,p.getUUID());own.putInt("Limited",own.getInt("Limited")+1);record(p.server,p.getUUID(),own);String who=who(own);
        line(p,a,who,TruantTexts.STILL_MINE);
        if(scene!=null&&scene.turn==Turn.DOORWAY&&!scene.aside)line(p,a,who,TruantTexts.WAIT);
        Talk.end(p);
    }
    /** After an answer: in the doorway, he steps aside; after the covered light, that is all; otherwise, a favour. */
    static void then(ServerPlayer p,LiteraryActor a){
        var sc=scene;var own=record(p.server,p.getUUID());String who=who(own);
        if(sc!=null&&sc.turn==Turn.DOORWAY&&!sc.aside){
            own.putInt("Paid",own.getInt("Paid")+1);record(p.server,p.getUUID(),own);line(p,a,who,TruantTexts.STEP_ASIDE);Talk.end(p);
            if(a.level() instanceof ServerLevel l){var b=base(l,TruantPlaces.DOOR);if(b!=null)aside(l,b,a);}
            return;
        }
        if(sc!=null&&sc.turn==Turn.COVER){line(p,a,who,TruantTexts.FOR_NOW);Talk.end(p);return;}
        favour(p,a);
    }
    /** The greater price: something dangerous done for him, fitted to the place. */
    static void favour(ServerPlayer p,LiteraryActor a){
        var own=record(p.server,p.getUUID());String who=who(own);var place=LabyrinthPlace.byId(a.scene());
        String what=place==TruantPlaces.DOOR?"go_back":place==TruantPlaces.LEDGE?"cross_alone":"lamp_out";
        for(var said:TruantTexts.favour(what))line(p,a,who,said);
        Talk.ask(p,Talk.reply("All right.",q->favoured(q,a,what,true)),Talk.reply("No.",q->favoured(q,a,what,false)));
    }
    static void favoured(ServerPlayer p,LiteraryActor a,String what,boolean agreed){
        var own=record(p.server,p.getUUID());String who=who(own);
        var list=own.getList("Favours",Tag.TAG_COMPOUND);var f=new CompoundTag();f.putString("What",what);f.putInt("At",meeting(own));f.putString("Answer",agreed?"agreed":"refused");list.add(f);while(list.size()>16)list.remove(0);own.put("Favours",list);record(p.server,p.getUUID(),own);
        if(!agreed){line(p,a,who,TruantTexts.STILL_MINE);Talk.end(p);return;}
        var sc=scene;if(sc!=null)sc.favour.put(p.getUUID(),what);
        if(what.equals("lamp_out")){
            Talk.line(p,"",TruantTexts.LAMP_OUT_NOTE);Talk.end(p);
            if(a.level() instanceof ServerLevel l){var b=base(l,TruantPlaces.LAMP);if(b!=null)darken(l,b.offset(TruantPlaces.LAMP_LIGHT),TruantPlaces.LAMP,l.getGameTime()+FAVOUR_DARK);}
            if(sc!=null)sc.dark=true;did(p,what);return;
        }
        line(p,a,who,TruantTexts.agreed(what));Talk.end(p);
    }
    /** Help after harm: he says what he did, in his words, and asks for nothing. That is his promise, and it is kept as his. */
    static void afterHarm(ServerPlayer p,LiteraryActor a){
        var server=p.server;var id=p.getUUID();var own=record(server,id);int meeting=meeting(own);
        own.putInt("Talked",meeting);own.putInt("ForboreAt",meeting);own.putInt("AfterHarm",own.getInt("AfterHarm")+1);own.putInt("HisPromise",meeting);record(server,id,own);
        String who=who(own);Talk.begin(p,KEY,who);
        for(var said:TruantTexts.afterHarm(own))line(p,a,who,said);
        Talk.end(p);
    }

    // ------------------------------------------------------------------ for tests
    /** His body, if it is loaded. */
    public static @Nullable LiteraryActor body(ServerLevel l){var s=state(l.getServer());return s.hasUUID("Body")&&l.getEntity(s.getUUID("Body")) instanceof LiteraryActor a&&a.isAlive()?a:null;}
    /** What is going on in his place, for tests' messages. */
    public static String debug(MinecraftServer server){var sc=scene;return state(server).toString()+(sc==null?" scene=none":" scene="+sc.place.id()+" turn="+sc.turn+" target="+sc.target+" holding="+sc.holding+" crossing="+sc.crossing+" lamp="+sc.lamp+" looked="+sc.looked+" aside="+sc.aside+" struggles="+sc.struggles+" cover="+sc.cover+" shove="+sc.shove+"/"+sc.shoveTicks+" leg="+sc.leg+" turned="+sc.turned+" favour="+sc.favour);}
    /** 0.4.282: makes his next chosen meeting this turn (the multiplayer gate still applies); null to choose as in play. */
    public static void force(@Nullable Turn turn){FORCED=turn;}
    /** What he is at the present meeting, or null. */
    public static @Nullable Turn turn(){return scene==null?null:scene.turn;}
    public static int shoveStage(){return scene!=null&&scene.place==TruantPlaces.LEDGE?scene.shove:-1;}
    public static int coverStage(){return scene!=null&&scene.place==TruantPlaces.LAMP?scene.cover:-1;}
    public static boolean steppedAside(){return scene!=null&&scene.aside;}
    /** Sets a reader's record, for tests that start partway into a history with him. */
    public static void remember(MinecraftServer s,UUID id,CompoundTag own){record(s,id,own);}
    /** The stage of the lamp's scene: 0 round the corner, 1 drawing back, 2 against the wall, 3 at the lamp; -1 when it is not the lamp. */
    public static int lampStage(){return scene!=null&&scene.place==TruantPlaces.LAMP?scene.lamp:-1;}
    /** Ends every reader's present meeting, as if they had been apart a long while. */
    public static void apart(){WITH.clear();HELPED.clear();WITHDRAWN.clear();ASKED.clear();NEAR.clear();}
    /** Forgets what he was doing, as a fresh arrival would find him. */
    public static void fresh(){scene=null;OPENED_ALONE=-1;OCCUPIED.clear();}
}
