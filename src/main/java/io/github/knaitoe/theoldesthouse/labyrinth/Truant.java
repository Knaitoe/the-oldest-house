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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
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
        // The lamp: 0 round the corner, 1 drawing back, 2 against the wall, 3 at his lamp; each reader's looking.
        int lamp;int waited;final Map<UUID,Integer> looked=new HashMap<>();boolean greeted;
    }
    static @Nullable Scene scene;
    /** Each reader's last time with him (for meetings), the readers he has helped this meeting, those he keeps his distance from, and their time close to him (for the panel). */
    static final Map<UUID,Long> WITH=new HashMap<>();
    static final Set<UUID> HELPED=new HashSet<>(),WITHDRAWN=new HashSet<>(),ASKED=new HashSet<>();
    static final Map<UUID,Integer> NEAR=new HashMap<>();
    /** Which side of the heavy door each reader was last on, wholly: +1 the near side, -1 through. */
    static final Map<UUID,Integer> SIDE=new HashMap<>();
    static final Set<LabyrinthPlace> OCCUPIED=EnumSet.noneOf(LabyrinthPlace.class);
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){scene=null;WITH.clear();HELPED.clear();WITHDRAWN.clear();ASKED.clear();NEAR.clear();SIDE.clear();OCCUPIED.clear();}

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
            if(now>scene.quietAt&&scene.quietAt>=0&&a.hasCustomName()){a.setCustomName(null);a.setCustomNameVisible(false);scene.quietAt=-1;}
            if(now<scene.flinchUntil){if(a.gesture()!=FLINCH)a.gesture(FLINCH);continue;}
            if(a.gesture()==FLINCH)a.gesture(0);
            switch(place){
                case TRUANT_DOOR->door(l,b,a,readers,now);
                case TRUANT_LEDGE->ledge(l,b,a,readers);
                case TRUANT_LAMP->lamp(l,b,a,readers);
                default->{}
            }
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
        var hand=gesture==LAMP?new ItemStack(Items.LANTERN):ItemStack.EMPTY;
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
        for(var p:readers){if(withdrawn(p))continue;int side=side(b,p);if(side==0&&inDoorway(b,p)||side>0&&p.getZ()<doorZ+6.5)waiting.add(p);}
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
                for(var p:readers)if(onLedge(b,p)&&!withdrawn(p)){sc.steadied=p.getUUID();sc.crossing=1;sc.safe=p.position();say(a,TruantTexts.STEADY);break;}
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
                double z=Math.min(b.getZ()+TruantPlaces.LEDGE_NEAR+1.6,Math.max(b.getZ()+TruantPlaces.LEDGE_FAR+.5,held.getZ()+1));
                var before=a.position();a.moveTo(x,y,z,180,0);a.setYBodyRot(180);a.setYHeadRot(180);
                a.walkAnimation.update((float)a.position().subtract(before).horizontalDistance()*4,.4F);gesture(a,STEADY);
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
                if(seen||close)sc.lamp=1;
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
                var spot=base.add(TruantPlaces.LAMP_SPOT);walk(a,spot,.07);
                if(a.position().distanceToSqr(spot)<.01){
                    var r=nearest(a,readers);if(r!=null)face(a,r.position());else{a.setYRot(0);a.setYBodyRot(0);a.setYHeadRot(0);}
                    boolean any=false;for(var p:readers)if(!withdrawn(p)&&p.distanceToSqr(a)<3.5*3.5){any=true;if(!HELPED.contains(p.getUUID())){helped(p,HELP_LAMP);ASKED.add(p.getUUID());}}
                    if(any&&!sc.greeted){sc.greeted=true;say(a,TruantTexts.LAMP);}
                    gesture(a,any?LAMP:0);
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
            if(NEAR.merge(id,1,Integer::sum)>=20){NEAR.remove(id);ASKED.remove(id);var own=record(p.server,id);if(own.getInt("Talked")<meeting(own))open(p,a);}
        }
    }
    /** Right-clicking him. */
    public static void talk(ServerPlayer p,LiteraryActor a){
        if(!reader(p)||!a.isAlive()||p.distanceToSqr(a)>25)return;
        if(withdrawn(p)||scene!=null&&a.level().getGameTime()<scene.flinchUntil)return;
        if(Talk.resume(p,KEY))return;
        if(WITH.get(p.getUUID())==null)met(p,a,a.level().getGameTime());
        var own=record(p.server,p.getUUID());
        if(own.getInt("Talked")>=meeting(own)){say(a,TruantTexts.again(own));return;}
        ASKED.remove(p.getUUID());open(p,a);
    }
    static void line(ServerPlayer p,LiteraryActor a,String who,String text){Talk.line(p,who,text);say(a,text);}
    static String who(CompoundTag own){return own.getBoolean("Named")?"Johnny":"The man";}
    /** The exchange: what he remembers, his question, and the replies. */
    static void open(ServerPlayer p,LiteraryActor a){
        var server=p.server;var id=p.getUUID();var own=record(server,id);int meeting=meeting(own);
        boolean first=own.getInt("Talked")==0;own.putInt("Talked",meeting);record(server,id,own);
        String who=who(own);Talk.begin(p,KEY,who);
        var place=LabyrinthPlace.byId(a.scene());
        for(var said:TruantTexts.remembers(own,first,place))line(p,a,who,said);
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
    static void tell(ServerPlayer p,LiteraryActor a,String placeId){
        var server=p.server;var id=p.getUUID();var own=record(server,id);String who=who(own);
        var told=own.getList("Told",Tag.TAG_STRING);told.add(StringTag.valueOf(placeId));while(told.size()>16)told.remove(0);own.put("Told",told);
        String promise=own.getString("Promise");
        if((promise.equals("agreed")||promise.equals("maybe"))&&!own.getBoolean("Kept")&&meeting(own)>own.getInt("PromisedAt")){own.putBoolean("Kept",true);own.putString("KeptPlace",placeId);own.putInt("KeptAt",meeting(own));}
        record(server,id,own);
        var follow=TruantTexts.follow(placeId);
        line(p,a,who,TruantTexts.heard(placeId));line(p,a,who,follow.question());
        List<Talk.Reply> replies=new ArrayList<>();
        for(int i=0;i<follow.answers().length;i++){int k=i;replies.add(Talk.reply(follow.answers()[i],q->{line(q,a,who,follow.replies()[k]);promise(q,a);}));}
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
        if(scene!=null){scene.flinchUntil=a.level().getGameTime()+WITHDRAW;if(p.getUUID().equals(scene.steadied)){scene.steadied=null;scene.crossing=3;scene.leg=1;}}
        a.gesture(FLINCH);say(a,TruantTexts.STRUCK);
        a.level().playSound(null,a.blockPosition(),SoundEvents.PLAYER_HURT,SoundSource.NEUTRAL,.6F,.9F);
    }

    // ------------------------------------------------------------------ for tests
    /** His body, if it is loaded. */
    public static @Nullable LiteraryActor body(ServerLevel l){var s=state(l.getServer());return s.hasUUID("Body")&&l.getEntity(s.getUUID("Body")) instanceof LiteraryActor a&&a.isAlive()?a:null;}
    /** What is going on in his place, for tests' messages. */
    public static String debug(MinecraftServer server){var sc=scene;return state(server).toString()+(sc==null?" scene=none":" scene="+sc.place.id()+" holding="+sc.holding+" crossing="+sc.crossing+" lamp="+sc.lamp+" looked="+sc.looked);}
    /** The stage of the lamp's scene: 0 round the corner, 1 drawing back, 2 against the wall, 3 at the lamp; -1 when it is not the lamp. */
    public static int lampStage(){return scene!=null&&scene.place==TruantPlaces.LAMP?scene.lamp:-1;}
    /** Ends every reader's present meeting, as if they had been apart a long while. */
    public static void apart(){WITH.clear();HELPED.clear();WITHDRAWN.clear();ASKED.clear();NEAR.clear();}
    /** Forgets what he was doing, as a fresh arrival would find him. */
    public static void fresh(){scene=null;OPENED_ALONE=-1;OCCUPIED.clear();}
}
