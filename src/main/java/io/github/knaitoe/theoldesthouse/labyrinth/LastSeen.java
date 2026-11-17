package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.LastSeenFolderPayload;
import io.github.knaitoe.theoldesthouse.network.LastSeenTimePayload;
import java.text.Normalizer;
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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.667, Last Seen, the first part (docs/LAST_SEEN_0_4_667.md), from the owner's approval of docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md.
 * The place: the municipal office at the edge of a city park, with its front desk, its poster wall and its archive of folders, and the
 * park itself in the morning, with the avenue past its railing (LastSeenSite). Three people work in the office: Nora, who keeps the
 * archive, at her desk; Patricia and Luz behind the counter (stranger phases 253 to 255). Each has lines of their own, and every thing
 * answers a hand.
 * <p>0.4.668, the second part (docs/LAST_SEEN_0_4_668.md): the archive and the first one. The ten children's folders are things to carry
 * (LastSeenFolder), one of each, filed in the bays by the first letter of the name on their spines. Nora gives her rule and the chore: the
 * day's pile, four folders on the long table, to go back where their names say. When the pile is shelved (or the morning has gone on
 * long enough), the first of them is at the fountain, made where nobody is looking; Luz goes to the front door and says so, and Nora goes
 * as far as the edge of the path and no further. A reader who finds the girl's folder by her face and brings it to her has matched her:
 * she takes it, looks at it, looks at them, says one thing, and gives it back. The morning is one clock for everyone there (Day, held in
 * memory as the Corner's street is); what each reader shelved, read and matched is their own. A folder goes back on its shelf when its
 * reader leaves. The House deals the place from depth eight.
 * <p>0.4.669, the third part (docs/LAST_SEEN_0_4_669.md): more of them. The park runs one day at a time, and the day is the furthest any
 * reader there has reached: a reader who matched the first of them comes back to the second day, and anyone with them comes too. On the
 * second day eight of the ten stand apart in the park, each where their folder says they were last seen, each made where nobody is
 * looking; Yanina and Santino never come. Each is matched by the reader who brings their own folder, and says their own one thing. Food
 * held out is taken and held and never eaten. The families: Tomás's grandmother comes in by the gate with his coat and puts it round
 * him; Daiana's father takes her off by the wrist and brings her back to the gate; Joel's mother stands on the far pavement and never
 * crosses; Brian's family never comes. The city: a wire fence goes up round the playground, notices on the lamp posts, and Mr. Ocampo
 * walks in to tell Nora to close the files. She does not. At dusk the eight turn to face the avenue. Four matched on the second day and
 * the day counts.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LastSeen {
    private LastSeen(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LAST_SEEN;
    /** The ten who come back are on stranger phases 243 to 252; the office's three on 253 to 255; the city's man and the families on 256 to 259. */
    public static final int CHILDREN_FIRST=243,CHILDREN=10,FIRST=253,NORA=0,PATRICIA=1,LUZ=2,PEOPLE=3;
    public static final int OTHERS_FIRST=256,OCAMPO=0,GRAN=1,FATHER=2,MOTHER=3,OTHERS=4;
    private static final String[] CHILD_SKINS={"lastseen_rocio","lastseen_tomas","lastseen_micaela","lastseen_brian","lastseen_daiana","lastseen_lautaro","lastseen_abril","lastseen_joel","lastseen_yanina","lastseen_santino"};
    private static final String[] SKINS={"lastseen_nora","lastseen_patricia","lastseen_luz"};
    private static final String[] OTHER_SKINS={"lastseen_ocampo","lastseen_gran","lastseen_father","lastseen_mother"};
    private static final BlockPos[] SEATS={LastSeenSite.NORA_AT,LastSeenSite.PATRICIA_AT,LastSeenSite.LUZ_AT};
    public static boolean cast(int phase){return phase>=CHILDREN_FIRST&&phase<OTHERS_FIRST+OTHERS;}
    public static boolean office(int phase){return phase>=FIRST&&phase<FIRST+PEOPLE;}
    public static boolean other(int phase){return phase>=OTHERS_FIRST&&phase<OTHERS_FIRST+OTHERS;}
    /** One of the ten, at child scale. */
    public static boolean child(int phase){return phase>=CHILDREN_FIRST&&phase<CHILDREN_FIRST+CHILDREN;}
    public static String skin(int phase){return phase>=OTHERS_FIRST?OTHER_SKINS[phase-OTHERS_FIRST]:phase>=FIRST?SKINS[phase-FIRST]:CHILD_SKINS[phase-CHILDREN_FIRST];}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** The House deals the place from depth eight, now that there is something to do in it. */
    public static final int DEPTH=8;
    public static boolean dealable(LabyrinthData d,UUID reader){return d.returnDepth(reader)>=DEPTH;}

    // ------------------------------------------------------------------------------------------------ the morning, in memory
    /** The first of them, and the day's pile: four of the ten whose folders are out on the long table this morning. */
    public static final int ROCIO=0;
    public static final int[] PILE={1,2,3,4};
    /** 0.4.669: the others, by their folders. Yanina (8) and Santino (9) never come back. */
    public static final int TOMAS=1,MICAELA=2,BRIAN=3,DAIANA=4,LAUTARO=5,ABRIL=6,JOEL=7;
    public static final int[] EIGHT={0,1,2,3,4,5,6,7};
    /** The pile's place on the long table, and where she stands: at the fountain's south rim, facing the office. */
    public static final BlockPos PILE_AT=new BlockPos(-17,0,-7);
    public static final Vec3 RIM=new Vec3(-7.5,0,-45.5);
    /** 0.4.669: where each of the eight stands on the second day, near where their folder says they were last seen, and which way they face. */
    public static final Vec3[] SPOT={RIM,new Vec3(14.5,0,-83.5),new Vec3(-11.5,0,-83.5),new Vec3(-5.3,0,-56.5),new Vec3(-8.9,0,-28.5),
        new Vec3(13.5,0,-19.9),new Vec3(-27.5,1,-61.5),new Vec3(21.5,0,-83.5)};
    public static final float[] FACES={0,180,-113,90,-90,180,-90,180};
    /** Where Daiana's father leaves her when he brings her back: inside the gate, facing the avenue. */
    public static final Vec3 AT_GATE=new Vec3(-6.5,0,-83.6);
    /** Joel's mother, on the far pavement opposite him. */
    public static final Vec3 MOTHER_AT=new Vec3(21.5,0,-97.5);
    /** The morning's stages: the archive's work; Luz at the door and Nora on her way out; the girl at the fountain. On the second day: the
     * park, then dusk. */
    public static final int MORNING=0,CALLED=1,FOUND=2,PARK=3,DUSK=4;
    /** Ticks: the longest the morning waits for the pile before she comes anyway; how long Luz waits at the door before each thing she says
     * and before Nora gets up; how long the girl looks at her folder, and how long before she gives it back; an empty park's reset. */
    public static final int MORNING_MAX=3600,AT_DOOR=30,LOOK=40,GIVE=100,EMPTY_RESET=200;
    /** 0.4.669, the second day's clock: Joel's mother on the far pavement; the grandmother at the gate; Daiana's father; how long he keeps her
     * away; the city's fence and notices; Mr. Ocampo; dusk. */
    public static final int MOTHER_AT_TICK=200,GRAN_AT=400,FATHER_AT=1000,AWAY=1200,CITY_AT=2000,OCAMPO_AT=2600,DUSK_AT=4800;
    /** The hour in the park: nine in the morning, and on the second day the sun going down to dusk by DUSK_AT. */
    public static final long MORNING_TIME=3000,DUSK_TIME=12300;
    /** How many matched on the second day count it. */
    public static final int FOUR=4;
    /** The mark on Tomás's body once his grandmother has put his coat round him (LiteraryActor.marks). */
    public static final int COAT_MARK=1;
    /** Poses (LiteraryActor.gesture): a wrist held and drawn along; holding a child's wrist; both hands up on the wire; a coat carried. */
    public static final int LED=1,LEADING=2,MESH=3,CARRY=4;
    static final UUID PILE_HOLDER=new UUID(0L,0L);
    /** Luz's walk to the front door; Nora's from her desk, through the partition and out to the head of the path. */
    static final List<Vec3> LUZ_ROUTE=List.of(new Vec3(3.5,0,-10.5),new Vec3(1.5,0,-12.6)),
        NORA_ROUTE=List.of(new Vec3(-12.5,0,-6.5),new Vec3(-11.5,0,-10.5),new Vec3(-8.5,0,-11.5),new Vec3(-6.5,0,-11.5),new Vec3(0.5,0,-12.4),
            new Vec3(0.5,0,-15.5),new Vec3(-5.5,0,-17.5),new Vec3(-7.5,0,-18.5));
    /** 0.4.669: down the park's main path from the gate, round the fountain's west side (the first of them stands at its south rim). */
    static final List<Vec3> GATE_IN=List.of(new Vec3(-8.0,0,-86.6),new Vec3(-8.0,0,-83.5)),
        DOWN_PATH=List.of(new Vec3(-8.0,0,-58.5),new Vec3(-12.5,0,-54.5),new Vec3(-12.5,0,-45.5),new Vec3(-8.0,0,-41.5));
    static List<Vec3> route(List<Vec3> a,List<Vec3> b,Vec3... more){var out=new ArrayList<Vec3>(a);out.addAll(b);out.addAll(List.of(more));return out;}
    static List<Vec3> reversed(List<Vec3> r){var out=new ArrayList<Vec3>(r);Collections.reverse(out);return out;}
    static final Vec3 GRAN_FROM=new Vec3(-14.5,0,-87.5),FATHER_FROM=new Vec3(-2.5,0,-87.5),FATHER_AWAY=new Vec3(6.5,0,-87.5),OCAMPO_FROM=new Vec3(-3.5,0,-87.5);
    static final List<Vec3> GRAN_ROUTE=route(GATE_IN,List.of(),new Vec3(11.5,0,-82.8),new Vec3(13.4,0,-83.4)),
        FATHER_IN=route(GATE_IN,DOWN_PATH,new Vec3(-8.0,0,-31.0),new Vec3(-7.6,0,-29.4)),
        FATHER_OUT=route(reversed(route(GATE_IN,DOWN_PATH,new Vec3(-8.0,0,-31.0))),List.of(),FATHER_AWAY),
        FATHER_BACK=List.of(new Vec3(-8.0,0,-86.6),new Vec3(-8.0,0,-84.2)),
        FATHER_GONE=List.of(new Vec3(-8.0,0,-86.6),FATHER_AWAY),
        OCAMPO_IN=route(GATE_IN,DOWN_PATH,new Vec3(-8.0,0,-18.5),new Vec3(0.5,0,-15.5),new Vec3(0.5,0,-12.4),new Vec3(-6.5,0,-11.5),
            new Vec3(-8.5,0,-11.5),new Vec3(-9.6,0,-9.5),new Vec3(-9.4,0,-4.6));
    static final List<Vec3> OCAMPO_OUT=route(reversed(OCAMPO_IN.subList(0,OCAMPO_IN.size()-1)),List.of(),OCAMPO_FROM);

    static final class Day {
        final BlockPos base;final long id;int stage=MORNING,since,step,stepAt,empty,tick;boolean fresh=true;
        /** 0.4.669: which day this is, the first or the second. */
        int number=1;
        final ArrayDeque<Integer> pile=new ArrayDeque<>();
        /** Folders off their shelves and who has each (the pile's own are out with nobody); each folder's times out; each bay's own fill. */
        final Map<Integer,UUID> out=new HashMap<>();final int[] issue=new int[CHILDREN];final Map<Integer,Integer> fill=new HashMap<>();
        /** The ten in the park, by their folders; which of them hold their own folder, who brought it, and for how long. */
        final UUID[] kids=new UUID[CHILDREN];final Map<Integer,UUID> givers=new HashMap<>();final int[] held=new int[CHILDREN];
        /** Who of the office's three are up out of their chairs (a bit each). */
        int up;
        /** Walks under way, keyed: the office's three by their number, the others from 100, the children from 200. */
        final Map<Integer,List<Vec3>> routes=new HashMap<>();final Map<Integer,Integer> leg=new HashMap<>();
        final Set<UUID> sawRim=new HashSet<>();
        /** 0.4.669: the families and the city's man; what each is doing; Daiana away with her father, or back at the gate. */
        final UUID[] others=new UUID[OTHERS];final int[] beat=new int[OTHERS];final int[] beatAt=new int[OTHERS];
        int daiana;boolean coat,ocampoDone,dusk;final Set<Long> near=new HashSet<>();
        final ArrayDeque<BlockPos> fence=new ArrayDeque<>();final ArrayDeque<BlockPos> notices=new ArrayDeque<>();boolean meshed;
        /** What the fence went up over (a tuft of grass, a flower), to be put back when the day is put back. */
        final Map<BlockPos,net.minecraft.world.level.block.state.BlockState> under=new HashMap<>();
        Day(BlockPos b,long id){base=b;this.id=id;Arrays.fill(held,-1);}
    }
    static final Map<Long,Day> DAYS=new HashMap<>();
    @Nullable static Day day(@Nullable BlockPos b){return b==null?null:DAYS.get(b.asLong());}
    /** The morning's stage at a park, or -1 if nobody has been there since the server started. */
    public static int stage(BlockPos b){var d=day(b);return d==null?-1:d.stage;}
    /** 0.4.669: which day it is at a park (1 or 2), or 0 if nobody has been there. */
    public static int dayNumber(BlockPos b){var d=day(b);return d==null?0:d.number;}
    /** Whose a folder is out with: a reader's id, the pile's, or null if it is in its bay. */
    public static @Nullable UUID out(BlockPos b,int child){var d=day(b);return d==null?null:d.out.get(child);}
    public static boolean inPile(BlockPos b,int child){var d=day(b);return d!=null&&d.pile.contains(child);}
    public static @Nullable LiteraryActor rocio(ServerLevel l,BlockPos b){return kid(l,b,ROCIO);}
    /** 0.4.669: one of the ten in the park, if they are there. */
    public static @Nullable LiteraryActor kid(ServerLevel l,BlockPos b,int child){var d=day(b);return d==null?null:actor(l,d.kids[child]);}
    /** 0.4.669: the city's man or one of the families, if they are there. */
    public static @Nullable LiteraryActor family(ServerLevel l,BlockPos b,int who){var d=day(b);return d==null?null:actor(l,d.others[who]);}
    /** The child whose folder one of them is holding, or -1. */
    public static int holding(BlockPos b){var d=day(b);if(d==null)return -1;int c=-1;for(int k:d.givers.keySet())if(c<0||k<c)c=k;return c;}
    public static boolean holding(BlockPos b,int child){var d=day(b);return d!=null&&d.givers.containsKey(child);}
    /** 0.4.669: Daiana's whereabouts on the second day: 0 at the bench, 1 led away, 2 away, 3 brought back, 4 at the gate. */
    public static int daiana(BlockPos b){var d=day(b);return d==null?0:d.daiana;}
    public static boolean ocampoDone(BlockPos b){var d=day(b);return d!=null&&d.ocampoDone;}
    /** For checks: what of the fence is still to go up, and the first cell of it. */
    public static String fenceLeft(BlockPos b){var d=day(b);return d==null?"no day":d.fence.size()+" left, next "+d.fence.peekFirst()+", stage "+d.stage+", since "+d.since;}
    public static boolean fenced(BlockPos b){var d=day(b);return d!=null&&d.stage>=PARK&&d.since>=CITY_AT&&d.fence.isEmpty();}
    public static void clearAll(){DAYS.clear();}
    /** For a test or a look at the place: the morning has gone on long enough, and she comes as soon as she can come unseen. */
    public static void hurry(BlockPos b){var d=day(b);if(d!=null&&d.stage==MORNING)d.since=Math.max(d.since,MORNING_MAX);}
    /** 0.4.669, for a test or a look at the place: the second day's clock moved on to at least this tick. */
    /** The second day, past the morning; and dusk. */
    static boolean late(BlockPos b){var d=day(b);return d!=null&&d.number>=2;}
    static boolean dusk(BlockPos b){var d=day(b);return d!=null&&d.dusk;}
    public static int since(BlockPos b){var d=day(b);return d==null?0:d.since;}
    public static void at(BlockPos b,int since){var d=day(b);if(d!=null&&d.number==2)d.since=Math.max(d.since,since);}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    /** 0.4.669: the furthest day a reader has reached: the second, once they have matched the first of them. */
    static int reached(CompoundTag own){return own.getInt("Days")>=1?2:1;}

    /** A reader in: the morning is started if nobody is there, the office's three are put at their places, and the morning is heard. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();
        var d=DAYS.computeIfAbsent(b.asLong(),k->new Day(b,l.getGameTime()));
        own.putInt("Visits",own.getInt("Visits")+1);
        if(own.getLong("DayId")!=d.id){own.putLong("DayId",d.id);own.remove("Waits");own.remove("Heard2");}
        ensure(l,b);
        HousePackets.send(p,new LastSeenTimePayload(time(d)));
        l.playSound(null,b.offset(0,1,-14),LastSeenRegistry.TRAFFIC.get(),SoundSource.AMBIENT,.35F,1F);
    }
    /** The park's hour: the morning; on the second day, through the afternoon to dusk. */
    static long time(Day d){return d.number<2?MORNING_TIME:MORNING_TIME+(long)((DUSK_TIME-MORNING_TIME)*Math.min(1D,d.since/(double)DUSK_AT));}

    // ------------------------------------------------------------------------------------------------ the office's three
    static AABB area(BlockPos b){return new AABB(b.offset(LastSeenSite.WEST,-4,LastSeenSite.NORTH)).minmax(new AABB(b.offset(LastSeenSite.EAST,16,1)));}
    /** One of the office's three, wherever in the place they are. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,area(b),x->is(x)&&x.phase()==FIRST+who))if(a.isAlive())return a;
        return null;
    }
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    /** Each of the three is sat in their chair unless the morning has them up; nothing is made while the office's entities are unloaded. */
    public static void ensure(ServerLevel l,BlockPos b){
        var d=day(b);
        for(int who=0;who<PEOPLE;who++){
            var at=b.offset(SEATS[who]);
            if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))continue;
            var a=find(l,b,who);
            if(a==null){
                a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
                a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+who);
                sit(l,a,at);
                if(!l.addFreshEntity(a))continue;
                continue;
            }
            if(d!=null&&(d.up&1<<who)!=0)continue;
            sit(l,a,at);
        }
    }
    /** In the chair at the cell, turned the way the chair is (ActorSeat lowers the body onto its seat and keeps it there). */
    static void sit(ServerLevel l,LiteraryActor a,BlockPos at){
        var st=l.getBlockState(at);
        float yaw=LastSeenBlock.is(st,LastSeenBlock.Kind.CHAIR)?st.getValue(LastSeenBlock.FACING).toYRot():Direction.SOUTH.toYRot();
        a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.gesture(0);a.setDeltaMovement(Vec3.ZERO);
    }
    static void speak(ServerLevel l,BlockPos b,LiteraryActor a,@Nullable String name,String line){
        a.say(name,line);a.speaks(line);
        for(var r:readers(l,b))if(r.distanceToSqr(a)<24*24)r.displayClientMessage(Component.literal((name==null?"":name+": ")+"“"+line+"”").withStyle(ChatFormatting.GRAY),true);
    }
    static void turn(LivingEntity a,float yaw){a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    static float toward(Vec3 from,Vec3 to){var v=to.subtract(from);return (float)(Mth.atan2(v.z,v.x)*Mth.RAD_TO_DEG)-90F;}

    /** A word with one of the three, with the city's man or one of the families, or a hand on one of the ten. */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(child(a.phase())){child(p,a,own);return;}
        var d=day(LiteraryVignettes.base(p,PLACE));
        if(other(a.phase())){
            int who=a.phase()-OTHERS_FIRST,heard=own.getInt("Family"+who);
            String line=LastSeenTexts.family(who,heard);own.putInt("Family"+who,heard+1);
            a.say(LastSeenTexts.FAMILY_NAMES[who],line);a.speaks(line);return;
        }
        if(!office(a.phase()))return;
        int who=a.phase()-FIRST,heard=own.getInt("Heard"+who);
        String line;
        int c=child(p.getMainHandItem());
        if(who==NORA&&d!=null&&d.number>=2&&c>=EIGHT.length&&live(d,p.getMainHandItem()))line=LastSeenTexts.NORA_NOT_BACK;
        else if(who==NORA&&d!=null&&d.number>=2&&own.getBoolean("Rule")&&!own.getBoolean("Leaving")&&heard>=2&&left(p)){line=LastSeenTexts.LEAVING;own.putBoolean("Leaving",true);}
        else if(who==NORA&&d!=null&&d.number>=2&&own.getBoolean("Rule")){
            int second=own.getInt("Heard2");own.putInt("Heard2",second+1);
            line=d.ocampoDone?LastSeenTexts.NORA_CLOSED:second%2==0?LastSeenTexts.NORA_EIGHT:LastSeenTexts.NORA_EACH;
        }else if(who==NORA&&d!=null&&d.stage>=FOUND&&d.stage<PARK&&heard>=1){
            line=(own.getInt("Matched")&1<<ROCIO)!=0?LastSeenTexts.NORA_AFTER:own.getBoolean("Waits")?LastSeenTexts.NORA_FIND:LastSeenTexts.NORA_WAITS;
            own.putBoolean("Waits",true);
        }else if(who==NORA&&heard>=2&&!own.getBoolean("Leaving")&&left(p)){line=LastSeenTexts.LEAVING;own.putBoolean("Leaving",true);}
        else if(who==PATRICIA&&heard==0&&young(p))line=LastSeenTexts.PATRICIA_YOUNG;
        else if(who==PATRICIA&&d!=null&&d.number>=2&&!own.getBoolean("Phone")){line=LastSeenTexts.PATRICIA_PHONE;own.putBoolean("Phone",true);}
        else if(who==LUZ&&d!=null&&d.number>=2&&d.coat&&!own.getBoolean("CoatHeard")){line=LastSeenTexts.LUZ_COAT;own.putBoolean("CoatHeard",true);}
        else if(who==LUZ&&d!=null&&d.number>=2&&!own.getBoolean("Counted")){line=LastSeenTexts.LUZ_COUNTED;own.putBoolean("Counted",true);}
        else if(who==LUZ&&d!=null&&d.number>=2)line=LastSeenTexts.LUZ_DOOR;
        else line=LastSeenTexts.line(who,heard);
        own.putInt("Heard"+who,heard+1);
        if(who==NORA&&line.equals(LastSeenTexts.RULE))own.putBoolean("Rule",true);
        a.say(LastSeenTexts.NAMES[who],line);a.speaks(line);
    }
    /** The House's read: a reader it reads as young; a reader it reads as having left a home, or been in danger in one; one who ran. */
    static boolean young(ServerPlayer p){return HouseRead.category(p,HouseRead.Trait.AGE).map(c->c.value().equals("DIGITAL")).orElse(false);}
    static boolean left(ServerPlayer p){return HouseRead.category(p,HouseRead.Trait.HOME).map(c->c.value().equals("RUNAWAY")||c.value().equals("DANGER")).orElse(false);}
    static boolean ran(ServerPlayer p){return HouseRead.category(p,HouseRead.Trait.HOME).map(c->c.value().equals("RUNAWAY")).orElse(false);}

    // ------------------------------------------------------------------------------------------------ the folders
    static CompoundTag tag(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();}
    /** Whose folder a stack is, or -1. */
    public static int child(ItemStack s){if(s.isEmpty()||!s.is(LastSeenRegistry.FOLDER.get()))return -1;var t=tag(s);return t.contains("Child")?Mth.clamp(t.getInt("Child"),0,CHILDREN-1):-1;}
    static ItemStack folder(Day d,int child){
        var s=new ItemStack(LastSeenRegistry.FOLDER.get());
        var t=new CompoundTag();t.putInt("Child",child);t.putLong("Site",d.base.asLong());t.putLong("Day",d.id);t.putInt("Issue",d.issue[child]);
        CustomData.set(DataComponents.CUSTOM_DATA,s,t);s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(child+1));
        return s;
    }
    /** Whether a folder is the one out this morning: its park's day, and its latest time off the shelf. Any other is nobody's. */
    static boolean live(Day d,ItemStack s){int c=child(s);if(c<0)return false;var t=tag(s);return t.getLong("Site")==d.base.asLong()&&t.getLong("Day")==d.id&&t.getInt("Issue")==d.issue[c];}
    static boolean live(ItemStack s){var d=DAYS.get(tag(s).getLong("Site"));return d!=null&&live(d,s);}
    static boolean carries(ServerPlayer p,Day d,int child){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(child(s)==child&&live(d,s))return true;}return false;}
    static boolean carrying(ServerPlayer p,Day d){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(live(d,inv.getItem(i)))return true;return false;}
    static void strip(ServerPlayer p,Day d,int child){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(child(s)==child&&tag(s).getLong("Site")==d.base.asLong())inv.setItem(i,ItemStack.EMPTY);}}

    /** Held open: the child's photograph and the typed form, drawn on the reader's own screen. */
    public static void read(ServerPlayer p,ItemStack s){
        int c=child(s);if(c<0)return;
        show(p,c);
        if(LiteraryVignettes.current(p)==PLACE){var own=own(p);own.putInt("Read",own.getInt("Read")|1<<c);save(p,own);}
    }
    static void show(ServerPlayer p,int c){HousePackets.send(p,new LastSeenFolderPayload(c,LastSeenTexts.file(c),LastSeenTexts.CHILDREN[c],LastSeenTexts.WHERE[c],LastSeenTexts.DAY[c]));}

    /** A bay's letter: the alphabet laid along the thirty-five bays in walking order, so each letter has one bay or two. */
    public static char letter(int bay){return (char)('A'+bay*26/LastSeenSite.SHELVES.size());}
    /** The bay a cell is part of (its foot, and the three bays above it to the boxes), or -1. */
    public static int bay(BlockPos rel){
        for(int i=0;i<LastSeenSite.SHELVES.size();i++){var s=LastSeenSite.SHELVES.get(i);if(s.getX()==rel.getX()&&s.getZ()==rel.getZ()&&rel.getY()>=0&&rel.getY()<LastSeenSite.CEILING)return i;}
        return -1;
    }
    static char initial(int child){return Character.toUpperCase(Normalizer.normalize(LastSeenTexts.CHILDREN[child],Normalizer.Form.NFD).charAt(0));}
    /** The bay a child's folder is filed in: the first whose letter is their name's. */
    public static int bayFor(int child){char c=initial(child);for(int i=0;i<LastSeenSite.SHELVES.size();i++)if(letter(i)==c)return i;return 0;}
    /** The child whose folder is filed in a bay, or -1. */
    public static int filedIn(int bay){for(int c=0;c<CHILDREN;c++)if(bayFor(c)==bay)return c;return -1;}
    /** The bay's cell at eye height, where a folder out leaves its gap. */
    public static BlockPos gapCell(int child){return LastSeenSite.SHELVES.get(bayFor(child)).above(1);}
    static void gap(ServerLevel l,Day d,int child){
        var at=d.base.offset(gapCell(child));var st=l.getBlockState(at);if(!LastSeenBlock.is(st,LastSeenBlock.Kind.SHELF))return;
        int s=st.getValue(LastSeenBlock.STAGE);if(s!=LastSeenBlock.ONE_OUT)d.fill.put(child,s);
        l.setBlock(at,st.setValue(LastSeenBlock.STAGE,LastSeenBlock.ONE_OUT),3);
    }
    static void filled(ServerLevel l,Day d,int child){
        var at=d.base.offset(gapCell(child));var st=l.getBlockState(at);if(!LastSeenBlock.is(st,LastSeenBlock.Kind.SHELF))return;
        if(st.getValue(LastSeenBlock.STAGE)==LastSeenBlock.ONE_OUT)l.setBlock(at,st.setValue(LastSeenBlock.STAGE,d.fill.getOrDefault(child,child%3)),3);
    }
    /** Out to a reader: into their empty hand, or their pack. Every time out is a new issue; the folder's gap shows on its shelf. */
    static boolean take(ServerPlayer p,Day d,int child,CompoundTag own){
        d.issue[child]++;
        var stack=folder(d,child);
        if(p.getMainHandItem().isEmpty())p.setItemInHand(InteractionHand.MAIN_HAND,stack);else if(!p.getInventory().add(stack))return false;
        d.out.put(child,p.getUUID());gap(p.serverLevel(),d,child);
        play(p.serverLevel(),p.blockPosition(),LastSeenRegistry.PAPER.get(),.6F);
        own.putInt("Read",own.getInt("Read")|1<<child);show(p,child);
        return true;
    }
    /** Back in its bay: whatever copy is still about is nobody's now. */
    static void reshelve(ServerLevel l,Day d,int child){d.out.remove(child);d.issue[child]++;filled(l,d,child);}

    // ------------------------------------------------------------------------------------------------ a hand on things
    /** A hand on something of the office's or the park's. True if it was theirs to answer. The card is the first paper: the House's own reading answers it. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        // The houses across the avenue keep their doors locked; only the way on opens (OnwardPlacements sets that one).
        if(st.getBlock() instanceof DoorBlock&&rel.getZ()==LastSeenSite.HOUSE_FRONT&&rel.getX()!=LastSeenSite.ONWARD.getX()){cue(p,LastSeenTexts.HOUSE_DOOR);return true;}
        if(!LastSeenBlock.is(st))return false;
        // The park's gate is shut but not locked: a hand swings both leaves open, and they stay open for everyone.
        if(st.getValue(LastSeenBlock.KIND)==LastSeenBlock.Kind.GATE&&st.getValue(LastSeenBlock.STAGE)==LastSeenBlock.SHUT){
            LastSeenSite.gate(l,b,LastSeenBlock.OPEN);
            l.playSound(null,b.offset(LastSeenSite.GATE_W),net.minecraft.sounds.SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,.7F,.8F);
            cue(p,LastSeenTexts.GATE);return true;
        }
        int stage=st.getValue(LastSeenBlock.STAGE);
        if(st.getValue(LastSeenBlock.KIND)==LastSeenBlock.Kind.SHELF)return shelf(p,b,rel,stage,own);
        if(st.getValue(LastSeenBlock.KIND)==LastSeenBlock.Kind.TABLE&&rel.equals(PILE_AT)&&day(b)!=null){
            if(stage==1)return pile(p,b,own);
            if(day(b).pile.isEmpty()&&day(b).number==1){cue(p,LastSeenTexts.PILE_DONE);return true;}
        }
        String line=switch(st.getValue(LastSeenBlock.KIND)){
            case CARD->null;
            case COUNTER->stage==1?LastSeenTexts.FLAP:LastSeenTexts.COUNTER;
            case CHAIR->LastSeenTexts.CHAIR;
            case SHELF->null;
            case POSTER->LastSeenTexts.poster(stage);
            case POSTER_B->LastSeenTexts.poster(8+stage);
            case TABLE->stage==1?LastSeenTexts.PILE:stage==2?(late(b)?LastSeenTexts.LAMP_LATE:LastSeenTexts.LAMP):LastSeenTexts.TABLE;
            case DESK->stage==LastSeenBlock.DRAWN?LastSeenTexts.DESK_OPEN:LastSeenTexts.DESK;
            case LOUVRE->LastSeenTexts.LOUVRE;
            case TUBE->LastSeenTexts.TUBE;
            case RAILING->stage==1?LastSeenTexts.POST:LastSeenTexts.RAILING;
            case GATE->stage==LastSeenBlock.CHAINED?LastSeenTexts.GATE_CHAINED:LastSeenTexts.GATE_OPEN;
            case LAMP_POST->stage==LastSeenBlock.NOTICE?LastSeenTexts.NOTICE:stage==LastSeenBlock.DARK?LastSeenTexts.LAMP_DARK:dusk(b)?LastSeenTexts.LAMP_POST_DUSK:LastSeenTexts.LAMP_POST;
            case BENCH->LastSeenTexts.BENCH;
            case FOUNTAIN->stage>=2?LastSeenTexts.BOWL:LastSeenTexts.FOUNTAIN;
            case KIOSK->LastSeenTexts.KIOSK;
            case TRUNK->LastSeenTexts.TRUNK;
            case LEAVES->stage==1?LastSeenTexts.SILK:LastSeenTexts.PLANE;
            case PLAY->LastSeenTexts.play(stage);
            case BANDSTAND->LastSeenTexts.BANDSTAND;
            case SHELTER->stage==2?LastSeenTexts.STOP:LastSeenTexts.SHELTER;
            case SHUTTER->LastSeenTexts.SHUTTER;
            case FENCE->LastSeenTexts.FENCE;
        };
        if(line==null)return false;
        cue(p,line);
        return true;
    }
    /**
     * A bay of the archive. With a folder in hand: back where its name says, or told whose names these are. Empty-handed at a bay that
     * holds one of the ten: it comes down, and is held open. Otherwise the spines, and the letter they begin with.
     */
    static boolean shelf(ServerPlayer p,BlockPos b,BlockPos rel,int stage,CompoundTag own){
        int bay=bay(rel);var d=day(b);
        if(stage==LastSeenBlock.BOXES||bay<0){cue(p,LastSeenTexts.BOXES);return true;}
        char letter=letter(bay);
        var hand=p.getMainHandItem();int c=child(hand);
        if(c>=0){
            if(d==null||!live(d,hand)){hand.setCount(0);cue(p,LastSeenTexts.BACK_ON_SHELF);return true;}
            if(bayFor(c)!=bay){cue(p,LastSeenTexts.wrongBay(letter));return true;}
            hand.setCount(0);reshelve(p.serverLevel(),d,c);own.putInt("Shelved",own.getInt("Shelved")|1<<c);
            play(p.serverLevel(),b.offset(rel),LastSeenRegistry.PAPER.get(),.6F);
            cue(p,LastSeenTexts.shelved(letter));return true;
        }
        int here=filedIn(bay);
        if(d==null||here<0){cue(p,stage==LastSeenBlock.ONE_OUT?LastSeenTexts.SHELF_GAP:LastSeenTexts.spines(letter));return true;}
        if(d.out.containsKey(here)||d.givers.containsKey(here)){cue(p,LastSeenTexts.SHELF_GAP);return true;}
        if(carrying(p,d)){cue(p,LastSeenTexts.ONE_AT_A_TIME);return true;}
        if(!hand.isEmpty()){cue(p,LastSeenTexts.spines(letter));return true;}
        if(take(p,d,here,own))cue(p,LastSeenTexts.taken(here,letter));
        return true;
    }
    /** The day's pile on the long table: one folder at a time, empty-handed, to be shelved. The table is bare once it is done. */
    static boolean pile(ServerPlayer p,BlockPos b,CompoundTag own){
        var d=day(b);var l=p.serverLevel();
        if(!d.pile.isEmpty()&&carrying(p,d)){cue(p,LastSeenTexts.ONE_AT_A_TIME);return true;}
        if(d.pile.isEmpty()||!p.getMainHandItem().isEmpty()){cue(p,LastSeenTexts.PILE);return true;}
        int c=d.pile.peekFirst();
        if(!take(p,d,c,own))return true;
        d.pile.pollFirst();own.putInt("FromPile",own.getInt("FromPile")+1);
        cue(p,LastSeenTexts.fromPile(c));
        if(d.pile.isEmpty())table(l,d,0);
        return true;
    }
    static void table(ServerLevel l,Day d,int stage){var at=d.base.offset(PILE_AT);var st=l.getBlockState(at);if(LastSeenBlock.is(st,LastSeenBlock.Kind.TABLE))l.setBlock(at,st.setValue(LastSeenBlock.STAGE,stage),3);}

    // ------------------------------------------------------------------------------------------------ the ones who come back
    /**
     * A hand on one of the ten. Holding their own folder: they take it. Holding another's: they look at it, then away. Holding something to
     * eat: they take it and hold it, and do not eat. Otherwise: cold.
     */
    static void child(ServerPlayer p,LiteraryActor a,CompoundTag own){
        int who=a.phase()-CHILDREN_FIRST;var b=LiteraryVignettes.base(p,PLACE);var d=day(b);var l=p.serverLevel();
        var hand=p.getMainHandItem();int c=child(hand);
        if(d!=null&&d.givers.containsKey(who))return;
        if(d!=null&&c==who&&live(d,hand)&&(who!=DAIANA||d.daiana==0||d.daiana==4)){
            hand.setCount(0);d.givers.put(c,p.getUUID());d.held[c]=0;
            a.setItemInHand(InteractionHand.MAIN_HAND,folder(d,c));
            play(l,a.blockPosition(),LastSeenRegistry.PAPER.get(),.5F);
            own.putInt("Matched",own.getInt("Matched")|1<<c);
            if(d.number<2)own.putInt("Days",Math.max(1,own.getInt("Days")));
            else{
                int second=own.getInt("Second")|1<<c;own.putInt("Second",second);
                if(Integer.bitCount(second)>=FOUR)own.putInt("Days",Math.max(2,own.getInt("Days")));
            }
            for(var r:readers(l,b))if(r!=p){var o=own(r);o.putInt("SawMatched",o.getInt("SawMatched")|1<<c);save(r,o);}
            return;
        }
        if(c>=0){cue(p,LastSeenTexts.notTheirs(who));return;}
        if(hand.has(DataComponents.FOOD)){
            if(!a.getOffhandItem().isEmpty()){cue(p,LastSeenTexts.stillHolding(who));return;}
            a.setItemInHand(InteractionHand.OFF_HAND,hand.copyWithCount(1));hand.shrink(1);
            own.putInt("Fed",own.getInt("Fed")|1<<who);
            cue(p,LastSeenTexts.fed(who));return;
        }
        l.sendParticles(ParticleTypes.SNOWFLAKE,a.getX(),a.getY()+.6,a.getZ(),6,.15,.2,.15,0);
        cue(p,LastSeenTexts.cold(who,who==TOMAS&&(a.marks()&COAT_MARK)!=0));
    }
    /** Where one of the ten stands now, and which way they face: their own spot; Daiana at the gate once she is brought back; all of them
     * facing the avenue at dusk. */
    static Vec3 spot(Day d,int c){return c==DAIANA&&d.daiana==4?AT_GATE:SPOT[c];}
    static float facing(Day d,int c){return d.dusk?180F:c==DAIANA&&d.daiana==4?180F:FACES[c];}
    /** One of the ten made at their spot only where no reader can see it: if any can, they are not there yet. */
    static boolean makeKid(ServerLevel l,Day d,int c,List<ServerPlayer> here){
        if(actor(l,d.kids[c])!=null)return true;
        var at=world(d.base,spot(d,c));
        if(!l.isLoaded(BlockPos.containing(at))||!l.areEntitiesLoaded(ChunkPos.asLong(BlockPos.containing(at))))return false;
        if(Drawing.seen(here,at))return false;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return false;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,CHILDREN_FIRST+c);
        float yaw=facing(d,c);a.moveTo(at.x,at.y,at.z,yaw,0);turn(a,yaw);a.setDeltaMovement(Vec3.ZERO);
        if(c==TOMAS&&d.coat)a.marks(COAT_MARK);
        if(!l.addFreshEntity(a))return false;
        d.kids[c]=a.getUUID();return true;
    }
    /** She holds her folder and looks at it; looks at the one who brought it and says one thing; then gives it back. */
    static void holding(ServerLevel l,Day d){
        for(var e:List.copyOf(d.givers.entrySet())){
            int c=e.getKey();var a=actor(l,d.kids[c]);
            if(a==null){reshelve(l,d,c);d.givers.remove(c);d.held[c]=-1;continue;}
            int held=++d.held[c];
            var giver=l.getServer().getPlayerList().getPlayer(e.getValue());
            if(held<LOOK){a.setXRot(38F);continue;}
            if(held==LOOK){
                a.setXRot(0F);
                if(giver!=null)turn(a,toward(a.position(),giver.position()));
                speak(l,d.base,a,null,LastSeenTexts.oneThing(c,giver!=null&&ran(giver)));
                continue;
            }
            if(held<GIVE)continue;
            a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            turn(a,facing(d,c));
            if(giver!=null&&readers(l,d.base).contains(giver)&&giver.getInventory().add(folder(d,c))){d.out.put(c,giver.getUUID());cue(giver,LastSeenTexts.givesBack(c));}
            else reshelve(l,d,c);
            d.givers.remove(c);d.held[c]=-1;
        }
    }

    // ------------------------------------------------------------------------------------------------ the morning's clock
    static boolean loaded(ServerLevel l,Day d){
        for(var at:List.of(d.base.offset(0,0,-8),d.base.offset(-16,0,-8),BlockPos.containing(world(d.base,RIM))))if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))return false;
        return true;
    }
    /** The morning begins: the pile on the table and its gaps on the shelves, every other folder in its bay, the front door shut, the three in
     * their chairs. A second day begins with the table bare and the front door open, and the park's eight are made as soon as nobody sees. */
    static void start(ServerLevel l,Day d,List<ServerPlayer> here){
        for(int c=0;c<CHILDREN;c++)filled(l,d,c);
        d.pile.clear();d.out.clear();
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,area(d.base),x->x.role()==LiteraryActor.STRANGER&&(child(x.phase())||other(x.phase()))))a.discard();
        d.stage=MORNING;d.since=0;d.up=0;d.step=0;Arrays.fill(d.kids,null);d.givers.clear();Arrays.fill(d.held,-1);
        for(var p:here)d.number=Math.max(d.number,reached(own(p)));
        if(d.number>=2){
            table(l,d,0);door(l,d,true);d.stage=PARK;
            d.fence.addAll(fenceCells());d.notices.addAll(LastSeenSite.LAMPS);
        }else{
            for(int c:PILE){d.pile.add(c);d.out.put(c,PILE_HOLDER);gap(l,d,c);}
            table(l,d,1);door(l,d,false);
        }
        ensure(l,d.base);
        for(var p:here)HousePackets.send(p,new LastSeenTimePayload(time(d)));
    }
    /** Back as it was when nobody is there: the ten and the visitors gone, the three in their chairs, the pile on the table, every folder in its
     * bay, and the city's fence and notices taken down. */
    static void reset(ServerLevel l,Day d){
        for(var id:d.kids){var a=actor(l,id);if(a!=null)a.discard();}
        for(var id:d.others){var a=actor(l,id);if(a!=null)a.discard();}
        for(int c=0;c<CHILDREN;c++)filled(l,d,c);
        table(l,d,1);door(l,d,false);d.up=0;
        for(int who=0;who<PEOPLE;who++){var o=find(l,d.base,who);if(o!=null)sit(l,o,d.base.offset(SEATS[who]));}
        for(var cell:fenceCells())for(int y=0;y<=1;y++){var at=d.base.offset(cell).above(y);if(LastSeenBlock.is(l.getBlockState(at),LastSeenBlock.Kind.FENCE))l.setBlock(at,d.under.getOrDefault(at,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()),3);}
        d.under.clear();
        for(var lamp:LastSeenSite.LAMPS){var at=d.base.offset(lamp);var st=l.getBlockState(at);if(LastSeenBlock.is(st,LastSeenBlock.Kind.LAMP_POST)&&st.getValue(LastSeenBlock.STAGE)==LastSeenBlock.NOTICE)l.setBlock(at,LastSeenBlock.of(LastSeenBlock.Kind.LAMP_POST,Direction.NORTH,LastSeenBlock.SHAFT),3);}
    }
    static void door(ServerLevel l,Day d,boolean open){
        var at=d.base.offset(LastSeenSite.FRONT_DOOR);var st=l.getBlockState(at);
        if(st.getBlock() instanceof DoorBlock door&&st.getValue(DoorBlock.OPEN)!=open){door.setOpen(null,l,st,at,open);}
    }
    /** Whoever is walking: the office's three by number, the others from 100, the children from 200. */
    @Nullable static LiteraryActor walker(ServerLevel l,Day d,int key){return key<100?find(l,d.base,key):key<200?actor(l,d.others[key-100]):actor(l,d.kids[key-200]);}
    static void walk(ServerLevel l,Day d){
        for(var key:List.copyOf(d.routes.keySet())){
            var a=walker(l,d,key);if(a==null){d.routes.remove(key);d.leg.remove(key);continue;}
            var route=d.routes.get(key);int i=d.leg.get(key);
            if(i>=route.size()){d.routes.remove(key);d.leg.remove(key);continue;}
            // Spoken to on the way, they stop to answer, as the Drawing's villagers do, then go on.
            if(a.listening())continue;
            // The gate is opened by whoever comes to it shut.
            if(key>=100&&key<200)gateFor(l,d,a);
            var target=world(d.base,route.get(i));
            if(SceneFootpaths.walk(a,target,key<100?.11:.12)||a.position().distanceToSqr(target)<.09)d.leg.put(key,i+1);
        }
    }
    static void send(Day d,int key,List<Vec3> route){if(key<100)d.up|=1<<key;d.routes.put(key,new ArrayList<>(route));d.leg.put(key,0);}
    static boolean busy(Day d,int key){return d.routes.containsKey(key);}
    /** The pile shelved (none of its folders off the shelves), or the morning gone on long enough: she is at the fountain. */
    static void morning(ServerLevel l,Day d,List<ServerPlayer> here){
        switch(d.stage){
            case MORNING->{
                boolean shelved=d.pile.isEmpty()&&Arrays.stream(PILE).noneMatch(d.out::containsKey);
                if((shelved&&d.since>=40||d.since>=MORNING_MAX)&&makeKid(l,d,ROCIO,here)){d.stage=CALLED;d.since=0;d.step=0;send(d,LUZ,LUZ_ROUTE);}
            }
            case CALLED->called(l,d);
            default->{}
        }
    }
    /** Luz to the front door, which she opens; what she says; Nora up and out as far as the head of the path. */
    static void called(ServerLevel l,Day d){
        var luz=find(l,d.base,LUZ);
        switch(d.step){
            case 0->{if(!busy(d,LUZ)){door(l,d,true);if(luz!=null)turn(luz,180);d.step=1;d.stepAt=d.since;}}
            case 1->{if(d.since-d.stepAt>=AT_DOOR){if(luz!=null)speak(l,d.base,luz,LastSeenTexts.NAMES[LUZ],LastSeenTexts.LUZ_CALL);d.step=2;d.stepAt=d.since;}}
            case 2->{if(d.since-d.stepAt>=AT_DOOR*2){if(luz!=null){luz.setYHeadRot(90);speak(l,d.base,luz,LastSeenTexts.NAMES[LUZ],LastSeenTexts.LUZ_NORA);}send(d,NORA,NORA_ROUTE);d.step=3;d.stepAt=d.since;}}
            case 3->{if(!busy(d,NORA)){var nora=find(l,d.base,NORA);if(nora!=null)turn(nora,180);d.stage=FOUND;d.step=0;}}
            default->{}
        }
    }
    /** Each reader who comes near one of them is told what they are like, once a day; they saw the first one come back. */
    static void watchers(ServerLevel l,Day d,List<ServerPlayer> here){
        for(int c=0;c<CHILDREN;c++){
            var a=actor(l,d.kids[c]);if(a==null)continue;
            for(var r:here){
                if(r.distanceToSqr(a)>=8*8)continue;
                if(c==ROCIO&&d.sawRim.add(r.getUUID())){cue(r,LastSeenTexts.AT_RIM);var o=own(r);o.putBoolean("SawFirst",true);save(r,o);continue;}
                if(c!=ROCIO&&d.near.add(r.getUUID().getLeastSignificantBits()*31+c)){cue(r,LastSeenTexts.near(c));var o=own(r);o.putInt("Saw",o.getInt("Saw")|1<<c);save(r,o);}
            }
        }
    }
    /** A folder whose reader has left, logged off, died or let it go goes back on its shelf. */
    static void release(ServerLevel l,Day d,List<ServerPlayer> here){
        for(var e:List.copyOf(d.out.entrySet())){
            int c=e.getKey();var who=e.getValue();
            if(who.equals(PILE_HOLDER)||d.givers.containsKey(c))continue;
            var p=l.getServer().getPlayerList().getPlayer(who);
            boolean has=p!=null&&carries(p,d,c);
            if(p!=null&&here.contains(p)&&has)continue;
            if(p!=null)strip(p,d,c);
            reshelve(l,d,c);
            if(has)cue(p,LastSeenTexts.BACK_ON_SHELF);
        }
    }
    /** Any folder anywhere that is not the one out this morning is nobody's, and is gone. */
    static void sweep(net.minecraft.server.MinecraftServer server){
        for(var p:server.getPlayerList().getPlayers()){
            var inv=p.getInventory();
            for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(child(s)>=0&&!live(s))inv.setItem(i,ItemStack.EMPTY);}
        }
    }

    // ------------------------------------------------------------------------------------------------ 0.4.669, the second day
    /** Each of the eight is kept to their spot and turned the way they face, unless they are holding their folder or being led. */
    static void kids(ServerLevel l,Day d,List<ServerPlayer> here){
        for(int c:EIGHT){
            if(c==DAIANA&&(d.daiana==1||d.daiana==2||d.daiana==3))continue;
            if(!makeKid(l,d,c,here))continue;
            var a=actor(l,d.kids[c]);if(a==null||d.givers.containsKey(c))continue;
            var at=world(d.base,spot(d,c));
            if(a.position().distanceToSqr(at)>.16)SceneFootpaths.walk(a,at,.08);
            else{turn(a,facing(d,c));a.setDeltaMovement(Vec3.ZERO);}
            if(c==LAUTARO)a.gesture(d.meshed&&!d.dusk?MESH:0);
        }
    }
    /** Someone who comes to the park's gate finds it shut, and opens it. */
    static void gateFor(ServerLevel l,Day d,LiteraryActor a){
        var g=d.base.offset(LastSeenSite.GATE_W);var st=l.getBlockState(g);
        if(LastSeenBlock.is(st,LastSeenBlock.Kind.GATE)&&st.getValue(LastSeenBlock.STAGE)==LastSeenBlock.SHUT&&a.position().distanceToSqr(Vec3.atCenterOf(g))<3.5*3.5){
            LastSeenSite.gate(l,d.base,LastSeenBlock.OPEN);l.playSound(null,g,SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,.7F,.8F);
        }
    }
    /** One of the visitors, made at a point only where nobody sees it. */
    static @Nullable LiteraryActor makeOther(ServerLevel l,Day d,int who,Vec3 rel,float yaw,List<ServerPlayer> here){
        var have=actor(l,d.others[who]);if(have!=null)return have;
        var at=world(d.base,rel);
        if(!l.isLoaded(BlockPos.containing(at))||!l.areEntitiesLoaded(ChunkPos.asLong(BlockPos.containing(at))))return null;
        if(Drawing.seen(here,at))return null;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,OTHERS_FIRST+who);
        a.moveTo(at.x,at.y,at.z,yaw,0);turn(a,yaw);a.setDeltaMovement(Vec3.ZERO);
        if(!l.addFreshEntity(a))return null;
        d.others[who]=a.getUUID();return a;
    }
    /** A visitor gone again once they are out of everyone's sight. */
    static boolean gone(ServerLevel l,Day d,int who,List<ServerPlayer> here){
        var a=actor(l,d.others[who]);if(a==null)return true;
        if(Drawing.seen(here,a.position()))return false;
        a.discard();d.others[who]=null;return true;
    }
    static void everyoneNear(ServerLevel l,Day d,Vec3 at,String line){for(var r:readers(l,d.base))if(r.position().distanceToSqr(at)<20*20)cue(r,line);}
    static void secondDay(ServerLevel l,Day d,List<ServerPlayer> here){
        kids(l,d,here);
        mother(l,d,here);
        gran(l,d,here);
        father(l,d,here);
        city(l,d,here);
        ocampo(l,d,here);
        if(!d.dusk&&d.since>=DUSK_AT){
            d.dusk=true;d.stage=DUSK;
            for(var r:here)cue(r,LastSeenTexts.DUSK);
        }
    }
    /** Joel's mother, on the far pavement opposite him, facing the park. She never crosses. */
    static void mother(ServerLevel l,Day d,List<ServerPlayer> here){
        if(d.since<MOTHER_AT_TICK)return;
        var a=makeOther(l,d,MOTHER,MOTHER_AT,0F,here);
        if(a!=null&&!a.listening()&&!busy(d,100+MOTHER))turn(a,0F);
    }
    /** Tomás's grandmother, in by the gate with his coat over her arms; she puts it round him, and stays. */
    static void gran(ServerLevel l,Day d,List<ServerPlayer> here){
        if(d.since<GRAN_AT)return;
        switch(d.beat[GRAN]){
            case 0->{var a=makeOther(l,d,GRAN,GRAN_FROM,-90F,here);if(a!=null){a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(LastSeenRegistry.COAT.get()));a.gesture(CARRY);send(d,100+GRAN,GRAN_ROUTE);d.beat[GRAN]=1;}}
            case 1->{if(!busy(d,100+GRAN)){var a=actor(l,d.others[GRAN]);var t=actor(l,d.kids[TOMAS]);if(a!=null&&t!=null)turn(a,toward(a.position(),t.position()));d.beat[GRAN]=2;d.beatAt[GRAN]=d.since;}}
            case 2->{
                if(d.since-d.beatAt[GRAN]<40)return;
                var a=actor(l,d.others[GRAN]);var t=actor(l,d.kids[TOMAS]);
                if(t==null||d.givers.containsKey(TOMAS))return;
                d.coat=true;t.marks(t.marks()|COAT_MARK);
                if(a!=null){a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);a.gesture(0);}
                everyoneNear(l,d,t.position(),LastSeenTexts.COAT_ON);d.beat[GRAN]=3;
            }
            default->{var a=actor(l,d.others[GRAN]);var t=actor(l,d.kids[TOMAS]);if(a!=null&&t!=null&&!a.listening())turn(a,toward(a.position(),t.position()));}
        }
    }
    /** Daiana's father: down the path to her, by the wrist out of the gate and away; an hour later back to the gate, where he leaves her. */
    static void father(ServerLevel l,Day d,List<ServerPlayer> here){
        if(d.since<FATHER_AT)return;
        int key=100+FATHER;
        switch(d.beat[FATHER]){
            case 0->{var a=makeOther(l,d,FATHER,FATHER_FROM,90F,here);if(a!=null){send(d,key,FATHER_IN);d.beat[FATHER]=1;}}
            case 1->{if(!busy(d,key)){var a=actor(l,d.others[FATHER]);var k=actor(l,d.kids[DAIANA]);if(a!=null&&k!=null){turn(a,toward(a.position(),k.position()));speak(l,d.base,a,LastSeenTexts.FAMILY_NAMES[FATHER],LastSeenTexts.FATHER_COME);}d.beat[FATHER]=2;d.beatAt[FATHER]=d.since;}}
            case 2->{
                // Not while she has her folder in her hands.
                if(d.since-d.beatAt[FATHER]<40||d.givers.containsKey(DAIANA))return;
                var a=actor(l,d.others[FATHER]);var k=actor(l,d.kids[DAIANA]);
                if(a==null||k==null){d.beat[FATHER]=0;return;}
                a.gesture(LEADING);k.gesture(LED);d.daiana=1;send(d,key,FATHER_OUT);
                everyoneNear(l,d,k.position(),LastSeenTexts.FATHER_TAKES);d.beat[FATHER]=3;
            }
            case 3->{
                lead(l,d);
                if(busy(d,key))return;
                // The two go together, once neither is seen.
                var a=actor(l,d.others[FATHER]);var k=actor(l,d.kids[DAIANA]);
                if(a!=null&&Drawing.seen(here,a.position())||k!=null&&Drawing.seen(here,k.position()))return;
                if(a!=null)a.discard();if(k!=null)k.discard();d.others[FATHER]=null;d.kids[DAIANA]=null;d.daiana=2;d.beat[FATHER]=4;d.beatAt[FATHER]=d.since;
            }
            case 4->{
                if(d.since-d.beatAt[FATHER]<AWAY)return;
                var a=makeOther(l,d,FATHER,FATHER_AWAY,90F,here);if(a==null)return;
                // She is made beside him where nobody sees; if anyone does, he waits.
                var at=world(d.base,FATHER_AWAY.add(.8,0,.4));
                if(Drawing.seen(here,at))return;
                var k=LiteraryRegistry.ACTOR.get().create(l);if(k==null)return;
                k.bind(PLACE,null);k.appearance(LiteraryActor.STRANGER,CHILDREN_FIRST+DAIANA);k.moveTo(at.x,at.y,at.z,90F,0);turn(k,90F);
                if(!l.addFreshEntity(k))return;
                d.kids[DAIANA]=k.getUUID();k.gesture(LED);a.gesture(LEADING);d.daiana=3;send(d,key,FATHER_BACK);d.beat[FATHER]=5;
            }
            case 5->{
                lead(l,d);
                if(busy(d,key))return;
                var a=actor(l,d.others[FATHER]);var k=actor(l,d.kids[DAIANA]);
                if(k!=null){k.gesture(0);everyoneNear(l,d,k.position(),LastSeenTexts.FATHER_BACK);}
                if(a!=null)a.gesture(0);
                d.daiana=4;send(d,key,FATHER_GONE);d.beat[FATHER]=6;
            }
            case 6->{if(!busy(d,key)&&gone(l,d,FATHER,here))d.beat[FATHER]=7;}
            default->{}
        }
    }
    /** Daiana led by the wrist: a step behind her father and to his left, her arm out ahead to his hand. */
    static void lead(ServerLevel l,Day d){
        var a=actor(l,d.others[FATHER]);var k=actor(l,d.kids[DAIANA]);if(a==null||k==null)return;
        double yaw=Math.toRadians(a.getYRot());var forward=new Vec3(-Math.sin(yaw),0,Math.cos(yaw));var left=new Vec3(forward.z,0,-forward.x);
        var at=a.position().subtract(forward.scale(.75)).add(left.scale(.38));
        SceneFootpaths.step(k,at,.16);turn(k,a.getYRot());
    }
    /** The cells of the city's fence round the playground, its corners first, the old gap in it last. */
    static List<BlockPos> fenceCells(){
        var out=new ArrayList<BlockPos>();
        int x0=5,x1=21,z0=-39,z1=-21;
        for(int x=x0;x<=x1;x++)out.add(new BlockPos(x,0,z0));
        for(int z=z0+1;z<=z1;z++)out.add(new BlockPos(x1,0,z));
        for(int z=z0+1;z<=z1;z++)out.add(new BlockPos(x0,0,z));
        for(int x=x1-1;x>x0;x--)if(x<12||x>14)out.add(new BlockPos(x,0,z1));
        for(int x=12;x<=14;x++)out.add(new BlockPos(x,0,z1));
        return out;
    }
    /** One cell of the fence: a corner post with its half panels turned to run along both sides; otherwise a panel two blocks wide, its
     * frame's upright and foot at every other cell, at the joint with the panel before it. */
    static net.minecraft.world.level.block.state.BlockState fence(BlockPos cell,boolean top){
        int x=cell.getX(),z=cell.getZ();
        boolean west=x==5,east=x==21,north=z==-39,south=z==-21;
        if((west||east)&&(north||south)){
            var f=north?(west?Direction.NORTH:Direction.EAST):(east?Direction.SOUTH:Direction.WEST);
            return LastSeenBlock.of(LastSeenBlock.Kind.FENCE,f,top?LastSeenBlock.CORNER_TOP:LastSeenBlock.CORNER);
        }
        boolean column=west||east;int along=column?z+39:x-5;boolean joint=along%2==0;
        return LastSeenBlock.of(LastSeenBlock.Kind.FENCE,column?Direction.EAST:Direction.NORTH,joint?(top?LastSeenBlock.PANEL_TOP:LastSeenBlock.PANEL):(top?LastSeenBlock.PANEL_MID_TOP:LastSeenBlock.PANEL_MID));
    }
    /** A cell the fence may go up in: air, or something growing there that nothing stands on (grass, a flower). */
    static boolean free(ServerLevel l,BlockPos at,net.minecraft.world.level.block.state.BlockState st){
        return st.isAir()||st.canBeReplaced()||st.getCollisionShape(l,at).isEmpty()&&!st.hasBlockEntity()&&!(st.getBlock() instanceof LastSeenBlock);
    }
    static boolean ringed(Vec3 r){return r.x>5&&r.x<22&&r.z>-39&&r.z<-20;}
    /** The city: the fence goes up a panel at a time, and the notices onto the lamp posts, only where nobody sees, and never round a reader. */
    static void city(ServerLevel l,Day d,List<ServerPlayer> here){
        if(d.since<CITY_AT)return;
        if(d.tick%4==0&&!d.fence.isEmpty()&&here.stream().noneMatch(p->ringed(p.position().subtract(Vec3.atLowerCornerOf(d.base))))){
            for(int tries=Math.min(4,d.fence.size());tries>0;tries--){
                var cell=d.fence.pollFirst();var at=d.base.offset(cell);
                var low=l.getBlockState(at);var high=l.getBlockState(at.above());
                if(LastSeenBlock.is(low,LastSeenBlock.Kind.FENCE))continue;
                // Something built there (a post, a bench): the fence meets it and that cell is left out.
                if(!free(l,at,low)||!free(l,at.above(),high)){TheOldestHouse.LOGGER.debug("Last Seen: the fence leaves out {} ({})",cell,low);continue;}
                // Someone standing in it (not one standing at its edge, as Lautaro stands at the old gap), or a reader looking: later.
                if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(at).expandTowards(0,1,0).deflate(.3,0,.3)).isEmpty()||Drawing.seen(here,Vec3.atBottomCenterOf(at))){d.fence.addLast(cell);continue;}
                if(!low.isAir())d.under.put(at.immutable(),low);
                if(!high.isAir())d.under.put(at.above().immutable(),high);
                l.setBlock(at,fence(cell,false),3);l.setBlock(at.above(),fence(cell,true),3);
                if(l.getRandom().nextInt(3)==0)l.playSound(null,at,SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.5F,.7F);
                if(cell.getX()==13&&cell.getZ()==-21)d.meshed=true;
                break;
            }
        }
        if(d.tick%10==5&&!d.notices.isEmpty()){
            var lamp=d.notices.pollFirst();var at=d.base.offset(lamp);var st=l.getBlockState(at);
            if(!LastSeenBlock.is(st,LastSeenBlock.Kind.LAMP_POST))return;
            if(Drawing.seen(here,Vec3.atCenterOf(at))){d.notices.addLast(lamp);return;}
            l.setBlock(at,LastSeenBlock.of(LastSeenBlock.Kind.LAMP_POST,LastSeenSite.noticeFacing(lamp),LastSeenBlock.NOTICE),3);
        }
    }
    /** Mr. Ocampo, from the city: in by the gate, through the office to Nora's desk; what he tells her, and her answer; and out again. */
    static void ocampo(ServerLevel l,Day d,List<ServerPlayer> here){
        if(d.since<OCAMPO_AT)return;
        int key=100+OCAMPO;var a=actor(l,d.others[OCAMPO]);var nora=find(l,d.base,NORA);
        switch(d.beat[OCAMPO]){
            case 0->{if(makeOther(l,d,OCAMPO,OCAMPO_FROM,180F,here)!=null){send(d,key,OCAMPO_IN);d.beat[OCAMPO]=1;}}
            case 1->{if(!busy(d,key)){if(a!=null)turn(a,90F);if(nora!=null&&a!=null)nora.setYHeadRot(toward(nora.position(),a.position()));d.beat[OCAMPO]=2;d.beatAt[OCAMPO]=d.since;}}
            case 2->{if(d.since-d.beatAt[OCAMPO]>=20){if(a!=null)speak(l,d.base,a,LastSeenTexts.FAMILY_NAMES[OCAMPO],LastSeenTexts.OCAMPO_FILE);d.beat[OCAMPO]=3;d.beatAt[OCAMPO]=d.since;}}
            case 3->{if(d.since-d.beatAt[OCAMPO]>=60){if(a!=null)speak(l,d.base,a,LastSeenTexts.FAMILY_NAMES[OCAMPO],LastSeenTexts.OCAMPO_CLOSE);d.beat[OCAMPO]=4;d.beatAt[OCAMPO]=d.since;}}
            case 4->{if(d.since-d.beatAt[OCAMPO]>=50){if(nora!=null)speak(l,d.base,nora,LastSeenTexts.NAMES[NORA],LastSeenTexts.NORA_OPEN);d.ocampoDone=true;d.beat[OCAMPO]=5;d.beatAt[OCAMPO]=d.since;}}
            case 5->{if(d.since-d.beatAt[OCAMPO]>=40){send(d,key,OCAMPO_OUT);d.beat[OCAMPO]=6;}}
            case 6->{if(!busy(d,key)&&gone(l,d,OCAMPO,here))d.beat[OCAMPO]=7;}
            default->{}
        }
    }

    /** Everyone in the scene: inside its railing, in the office or on the avenue. */
    public static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players()){
            if(p.isSpectator()||!p.isAlive())continue;
            var r=p.position().subtract(Vec3.atLowerCornerOf(b));
            if(r.x>=LastSeenSite.WEST&&r.x<=LastSeenSite.EAST+1&&r.z>=LastSeenSite.NORTH&&r.z<=1&&r.y>=-4&&r.y<=16)out.add(p);
        }
        return out;
    }
    /**
     * Every tick while someone is there: the morning's clock, the walks, the ten and their folders, folders whose readers have gone; on the
     * second day the families and the city. Every few seconds, the morning: the avenue's traffic beyond the railing, pigeons somewhere on the
     * paths, and, while a reader is in the office, the typewriter at Nora's desk. Each is heard where it is. The park's hour goes to its
     * readers every two seconds.
     */
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();
        if(server.getTickCount()%40==0)sweep(server);
        if(DAYS.isEmpty())return;
        var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var d:List.copyOf(DAYS.values())){
            var here=readers(l,d.base);
            // Folders go back on their shelves when their readers go, even when nobody is left in the place.
            if(server.getTickCount()%10==0&&!d.fresh)release(l,d,here);
            if(here.isEmpty()){if(++d.empty>=EMPTY_RESET&&loaded(l,d)){reset(l,d);DAYS.remove(d.base.asLong());}continue;}
            d.empty=0;
            if(d.fresh){if(!loaded(l,d))continue;d.fresh=false;start(l,d,here);}
            d.tick++;d.since++;
            walk(l,d);
            if(d.number<2)morning(l,d,here);else secondDay(l,d,here);
            holding(l,d);
            if(d.tick%5==0)watchers(l,d,here);
            if(d.tick%40==0)for(var r:here)HousePackets.send(r,new LastSeenTimePayload(time(d)));
            var rnd=l.getRandom();
            if(d.tick%240==60)play(l,d.base.offset(-8+rnd.nextInt(34)-17,1,LastSeenSite.ROAD_S-3),LastSeenRegistry.TRAFFIC.get(),.6F);
            if(d.tick%300==150)play(l,d.base.offset(LastSeenSite.PATH_X+rnd.nextInt(9)-4,0,LastSeenSite.PATH_Z+rnd.nextInt(30)-15),LastSeenRegistry.PIGEONS.get(),.5F);
            if(d.tick%180==90&&here.stream().anyMatch(p->LastSeenSite.inOffice(p.position().subtract(Vec3.atLowerCornerOf(d.base)))))
                play(l,d.base.offset(LastSeenSite.DESK).above(),LastSeenRegistry.TYPEWRITER.get(),.45F);
        }
    }
    static void play(ServerLevel l,BlockPos at,SoundEvent e,float volume){l.playSound(null,at,e,SoundSource.AMBIENT,volume,.95F+l.getRandom().nextFloat()*.1F);}
    /** 0.4.669: Tomás in his grandmother's coat once she has put it round him; everyone else as they are. */
    public static @Nullable String dressed(int phase,int marks){return phase==CHILDREN_FIRST+TOMAS&&(marks&COAT_MARK)!=0?"lastseen_tomas_coat":null;}
}
