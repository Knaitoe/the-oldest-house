package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.227, the last swimmer (docs/LAST_SWIMMER_0_4_227.md). A small municipal pool at closing time. The attendant asks the reader
 * to help pull the cover across; someone is still swimming in what is left uncovered, heard more than seen, every length the same
 * length of time on the pace clock, moving a lane over each time the cover reaches theirs. When the cover closes the splashing stops,
 * the lights go off bank by bank, and something walks across the cover toward the reader from underneath, pressing it up with each
 * step. The attendant holds the fire door open. Walking out is the ending.
 * <p>
 * The pool's state belongs to the pool (its base), so readers who come in together close it together; a reader on their own has their
 * own copy (SceneInstances). It starts again from the beginning when nobody has been in it for five seconds. Nothing in it can hurt.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LastSwimmer {
    private LastSwimmer(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LAST_SWIMMER;
    public static final int ATTENDANT=51,SWIMMER=52;
    /** One length of the pool, in ticks: fifteen seconds, so every turn comes round on a quarter of the pace clock. */
    public static final int LENGTH_TICKS=300;
    /** Between hauls; after the cover closes, the quiet, then the lights, then the steps. */
    public static final int HAUL_GAP=12,QUIET=60,LIGHTS_EVERY=20,STEPS_AFTER=150,STEP_EVERY=14,EMPTY_RESET=100;
    /** 0.4.581: how long a foot stays down after the next has landed, and how long the last one down stays when no more come. */
    public static final int FOOT_LIFT=3,FOOT_STAYS=STEP_EVERY+6;
    /** The lamps left on near the fire door. */
    private static final int KEEP_LIT=2;
    /**
     * 0.4.233: the radio's clock. Ticks after the radio starts at which each part of "Falling Out the Sky" begins (Armand Hammer and
     * The Alchemist, 2021, 3:40): the first verse, the first spoken interlude, the second verse, the second spoken interlude, the
     * third verse, and the end. Estimated from the verses' lengths, not timed against the recording: tune these to the track.
     */
    public static final int[] SONG={0,940,1280,2620,2960,4400};
    public static final int SECOND_TALK=3,LAST_VERSE=4;
    /** How long the splashing stops when the last verse begins. */
    public static final int HELD_BREATH=100;
    /**
     * 0.4.238: the cover is pulled across in seventeen hauls, which a reader doing as asked can finish in ten seconds, long before the
     * radio reaches its second spoken part. So each beat comes with the song or with the cover, whichever is first: the lights waver
     * once a third of the pool is covered, and the splashing stops once two thirds are, while a lane is still open.
     */
    public static final int WAVER_HAULS=6,BREATH_HAULS=12,WAVER_TICKS=340;
    /** The stars the skylight lays on the water, as pool cells (x, z) under it: a handle of three and a bowl of four. */
    public static final int[][] STARS={{3,-21},{2,-22},{0,-23},{-1,-24},{-1,-26},{-3,-26},{-3,-24}};
    /** Deep water: from here north the floor is at its lowest. */
    public static final int DEEP_FROM=-29;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** One pool's closing. */
    static final class Pool {
        final BlockPos base;long tick;int hauls,lane=3,lastHaul=-100,closedAt=-1,lightsOut,empty;
        @Nullable UUID attendant,swimmer;@Nullable BlockPos pressed,stepAt;int pressedUntil,lastStep,radio=-1;boolean held,cuedLane;
        /** 0.4.581: the foot before the one now down, and when it lifts; which foot comes next; the way it walks. */
        @Nullable BlockPos lifted;int liftAt;boolean right;Direction way=Direction.SOUTH;
        /** 0.4.233: the radio's part now playing, the lamp that wavered and when it comes back, and when the held breath ends. */
        int part=-1,flicker=-1,flickerUntil,breathUntil=-1;
        /** 0.4.238: whether each beat has come, and until when the lights waver. */
        boolean waved,breathed;int waverUntil=-1;
        int song(){return radio<0?-1:(int)(tick-radio);}
        Pool(BlockPos b){base=b;}
        boolean closed(){return closedAt>=0;}
        int since(){return closed()?(int)(tick-closedAt):-1;}
        boolean stepping(){return closed()&&since()>=STEPS_AFTER;}
    }
    private static final Map<Long,Pool> POOLS=new HashMap<>();
    /** 0.4.244: pools with only finished readers in them, and how long they have been; dressed as their aftermath at {@link #AFTER_WAIT}. */
    private static final Map<Long,Integer> AFTER=new HashMap<>();
    public static final int AFTER_WAIT=20;
    public static void clearAll(){POOLS.clear();AFTER.clear();}
    public static boolean swimmer(int phase){return phase==SWIMMER;}

    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 at(BlockPos b,double x,double y,double z){return new Vec3(b.getX()+x,b.getY()+y,b.getZ()+z);}

    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean("Completed"))return;
        cue(p,"Chlorine, and a radio on somewhere. Past the lockers, the slap of someone swimming lengths.");
    }
    @Nullable static Pool pool(BlockPos b){return POOLS.get(b.asLong());}
    /** For tests: this pool's closing, from the base. */
    public static int hauls(BlockPos b){var p=pool(b);return p==null?0:p.hauls;}
    public static int lane(BlockPos b){var p=pool(b);return p==null?-1:p.lane;}
    public static boolean closed(BlockPos b){var p=pool(b);return p!=null&&p.closed();}
    public static boolean stepping(BlockPos b){var p=pool(b);return p!=null&&p.stepping();}
    public static boolean held(BlockPos b){var p=pool(b);return p!=null&&p.held;}
    public static long tick(BlockPos b){var p=pool(b);return p==null?-1:p.tick;}
    @Nullable public static BlockPos pressed(BlockPos b){var p=pool(b);return p==null?null:p.pressed;}
    /** For tests: the foot before the one now down, while it is still down; and how many lamps are off. */
    @Nullable public static BlockPos lifted(BlockPos b){var p=pool(b);return p==null?null:p.lifted;}
    public static int lightsOut(BlockPos b){var p=pool(b);return p==null?0:p.lightsOut;}
    /** For tests: the part of the song the radio is in (0 to 4, 5 once it has ended), and whether the swimmer is holding a breath. */
    public static int part(BlockPos b){var p=pool(b);return p==null?-1:p.part;}
    public static boolean holding(BlockPos b){var p=pool(b);return p!=null&&p.breathUntil>p.tick;}
    /** For tests: whether the lights have wavered, and whether they are wavering now. */
    public static boolean waved(BlockPos b){var p=pool(b);return p!=null&&p.waved;}
    public static boolean wavering(BlockPos b){var p=pool(b);return p!=null&&p.waverUntil>p.tick;}
    /** 0.4.239: the pool through its door: someone swimming lengths until the cover is on, then nothing moving. */
    @Nullable public static DoorSounds.Through heard(@Nullable BlockPos b){
        var p=b==null?null:pool(b);
        return DoorSounds.Through.of(p!=null&&p.closed()?DoorSounds.Carry.EMPTY:DoorSounds.Carry.POOL);
    }
    /** 0.4.244: and for a reader who closed it, with no closing under way, what they would find: the cover on, nothing moving. */
    @Nullable public static DoorSounds.Through heard(@Nullable BlockPos b,LabyrinthData data,UUID listener){
        if((b==null||pool(b)==null)&&Aftermath.returning(data,listener,PLACE))return DoorSounds.Through.of(DoorSounds.Carry.EMPTY);
        return heard(b);
    }
    /** For tests: whether this pool is standing as its aftermath. */
    public static boolean after(BlockPos b){Integer w=AFTER.get(b.asLong());return w!=null&&w>=AFTER_WAIT;}
    /** The stars still on the water: those the cover has not reached. */
    public static int stars(int hauls){int n=0;for(var s:STARS)if(!covered(hauls,s[0]))n++;return n;}
    static boolean covered(int hauls,int x){return x>SwimmerSite.POOL_X1-hauls;}
    /** Which part of the song a moment after the radio starts falls in. */
    public static int part(int song){if(song<0)return -1;for(int i=SONG.length-1;i>=0;i--)if(song>=SONG[i])return i;return 0;}
    /** For tests: runs the closing on to the steps without waiting. */
    public static void hurry(BlockPos b,int ticks){var p=pool(b);if(p!=null)p.tick+=ticks;}

    // ------------------------------------------------------------------------------------------------ hauling
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var s=l.getBlockState(b.offset(rel));var kind=SwimmerBlock.kind(s);
        if(kind==null)return false;
        var pool=pool(b);
        // 0.4.244: the pool as the reader left it, with no closing under way.
        if(pool==null&&own.getBoolean("Completed")){
            String line=switch(kind){
                case COVER->SwimmerTexts.AFTER_COVER;
                case PACE_CLOCK->SwimmerTexts.AFTER_CLOCK;
                case RADIO->SwimmerTexts.AFTER_RADIO;
                case LOCKERS->s.getValue(SwimmerBlock.STAGE)==1?SwimmerTexts.AFTER_LOCKER:null;
                default->null;
            };
            if(line!=null){cue(p,line);return true;}
        }
        switch(kind){
            case REEL_END,REEL->{if(p.isShiftKeyDown()){cue(p,SwimmerTexts.NO_REVERSE);return true;}if(rel.getZ()>=SwimmerSite.POOL_SOUTH)return haul(p,b,own,pool);cue(p,"The crank at this end turns with the other. Pull from the shallow end.");return true;}
            case COVER->{if(s.getValue(SwimmerBlock.STAGE)==2&&rel.getZ()>=SwimmerSite.POOL_SOUTH-2)return haul(p,b,own,pool);cue(p,pool!=null&&pool.closed()?"Taut. It doesn't give.":"Heavy, wet fabric. The strap is at the shallow end.");return true;}
            case PACE_CLOCK->{own.putBoolean("Clock227",true);cue(p,pool!=null&&!pool.closed()?"The sweep hand goes round. Every turn at the wall lands on a quarter.":"The sweep hand goes round.");return true;}
            case RADIO->{own.putBoolean("Radio227",true);cue(p,pool==null||pool.part<SONG.length-1?SwimmerTexts.RADIO:SwimmerTexts.RADIO_ENDED);return true;}
            case LOCKERS->{cue(p,s.getValue(SwimmerBlock.STAGE)==1?"One locker is open. Inside: a towel folded square, and a pair of shoes, dry.":"Locked.");return true;}
            case START->{cue(p,"Lane "+(s.getValue(SwimmerBlock.STAGE)+1)+".");return true;}
            case SIGN->{cue(p,s.getValue(SwimmerBlock.STAGE)==0?"NO RUNNING.":"LAST SWIM half an hour before closing.");return true;}
            case DEPTH->{cue(p,s.getValue(SwimmerBlock.STAGE)==0?"1.0 m.":"2.0 m.");return true;}
            case ROPE->{cue(p,"Floats on a line, from wall to wall.");return true;}
            case EXIT_SIGN->{cue(p,"EXIT.");return true;}
            default->{return false;}
        }
    }
    static boolean haul(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable Pool pool){
        if(pool==null||pool.closed()){cue(p,"The cover is on.");return true;}
        if(!SwimmerSite.southDeck(rel(p.position(),b))){cue(p,"Pull from the shallow end. The attendant has the other end.");return true;}
        if(pool.tick-pool.lastHaul<HAUL_GAP)return true;
        pool.lastHaul=(int)pool.tick;pool.hauls++;own.putInt("Hauled227",own.getInt("Hauled227")+1);
        var l=p.serverLevel();SwimmerSite.cover(l,b,pool.hauls);
        l.playSound(null,b.offset(SwimmerSite.REEL_SOUTH),SwimmerRegistry.HAUL.get(),SoundSource.BLOCKS,1F,.9F+l.random.nextFloat()*.2F);
        String line=SwimmerTexts.haul(pool.hauls);
        if(pool.hauls>=SwimmerSite.COLUMNS){close(l,pool,p,own);return true;}
        // 0.4.233: the cover takes the sky off the water, star by star; the last one gone is said.
        if(stars(pool.hauls)==0&&stars(pool.hauls-1)>0){for(var r:readers(l,b))cue(r,SwimmerTexts.STARS_OUT);return true;}
        if(line!=null)for(var r:readers(l,b))cue(r,line);
        return true;
    }
    /** The cover is on. The hauler's record is the one their click is holding and will save; everyone else's is saved here. */
    static void close(ServerLevel l,Pool pool,ServerPlayer hauler,CompoundTag hauled){
        pool.closedAt=(int)pool.tick;
        var s=swimmerActor(l,pool);if(s!=null)s.discard();pool.swimmer=null;
        hauled.putBoolean("Covered227",true);
        for(var r:readers(l,pool.base)){cue(r,"The cover's on. The splashing stops.");if(r==hauler)continue;var own=own(r);own.putBoolean("Covered227",true);save(r,own);}
    }

    // ------------------------------------------------------------------------------------------------ the pool's clock
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(!p.isSpectator()&&p.isAlive()&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE))&&SwimmerSite.inside(rel(p.position(),b)))out.add(p);
        return out;
    }
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    @Nullable static LiteraryActor swimmerActor(ServerLevel l,Pool pool){return actor(l,pool.swimmer);}
    @Nullable static LiteraryActor attendantActor(ServerLevel l,Pool pool){return actor(l,pool.attendant);}
    static LiteraryActor spawn(ServerLevel l,int phase,Vec3 at,float yaw){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,phase);a.moveTo(at.x,at.y,at.z,yaw,0);
        return l.addFreshEntity(a)?a:null;
    }
    /** Every actor of this scene inside the pool's box: the strays from an earlier closing are put away before a new one starts. */
    static void clearActors(ServerLevel l,BlockPos b){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(-16,-8,-46)).minmax(new AABB(b.offset(16,11,2))),a->a.role()==LiteraryActor.STRANGER&&(a.phase()==ATTENDANT||a.phase()==SWIMMER)))a.discard();
    }
    /** The hall's one swimmer: the pool's own if it is found, else the one already there; every other swimmer in the hall is put away. */
    @Nullable static LiteraryActor one(ServerLevel l,BlockPos b,Pool pool,@Nullable LiteraryActor own){
        var keep=own;
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(-16,-8,-46)).minmax(new AABB(b.offset(16,11,2))),a->a.isAlive()&&a.role()==LiteraryActor.STRANGER&&a.phase()==SWIMMER)){
            if(keep==null){keep=a;pool.swimmer=a.getUUID();continue;}
            if(a!=keep)a.discard();
        }
        return keep;
    }
    static boolean peopleLoaded(ServerLevel l,BlockPos b){
        for(int x=(b.getX()-16)>>4;x<=(b.getX()+16)>>4;x++)for(int z=(b.getZ()-46)>>4;z<=(b.getZ()+2)>>4;z++)if(!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        return true;
    }
    static void start(ServerLevel l,Pool pool){
        var b=pool.base;clearActors(l,b);SwimmerSite.cover(l,b,0);lights(l,b,0);SwimmerSite.poolLights(l,b,true);door(l,b,false);
        var a=spawn(l,ATTENDANT,Vec3.atBottomCenterOf(b.offset(SwimmerSite.ATTENDANT_START)),180);pool.attendant=a==null?null:a.getUUID();
        var s=spawn(l,SWIMMER,at(b,SwimmerSite.laneMiddle(pool.lane),SwimmerSite.SURFACE-.55,SwimmerSite.POOL_SOUTH+.5),180);pool.swimmer=s==null?null:s.getUUID();
        if(s!=null){s.setInvisible(true);s.setNoGravity(true);}
    }
    /** 0.4.244: the pool as the reader left it: the cover on from wall to wall, every lamp off but the two by the fire door, nobody in it. */
    static void aftermath(ServerLevel l,BlockPos b){clearActors(l,b);SwimmerSite.cover(l,b,SwimmerSite.COLUMNS);lights(l,b,SwimmerSite.LAMPS.size()-KEEP_LIT);
        // 0.4.581: the pool's own lamps are off too. What a reader is told here is that the hall is dark but for the two lamps by the fire door.
        SwimmerSite.poolLights(l,b,false);door(l,b,false);}
    static void lights(ServerLevel l,BlockPos b,int out){
        var lamps=SwimmerSite.LAMPS;
        for(int i=0;i<lamps.size();i++){var at=b.offset(lamps.get(i));var s=l.getBlockState(at);if(!SwimmerBlock.is(s,SwimmerBlock.Kind.LAMP))continue;int stage=i<out?1:0;if(s.getValue(SwimmerBlock.STAGE)!=stage)l.setBlock(at,s.setValue(SwimmerBlock.STAGE,stage),F);
            // 0.4.581: the light a lamp throws down onto the deck and the water goes out with it, and comes back with it.
            var under=b.offset(SwimmerSite.thrown(lamps.get(i)));var us=l.getBlockState(under);
            if(stage==0&&us.isAir())l.setBlock(under,Blocks.LIGHT.defaultBlockState(),F);else if(stage==1&&us.is(Blocks.LIGHT))l.setBlock(under,Blocks.AIR.defaultBlockState(),F);}
    }
    static void door(ServerLevel l,BlockPos b,boolean open){
        for(int y=0;y<=1;y++){var at=b.offset(SwimmerSite.EXIT_X,y,SwimmerSite.EXIT_Z);var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN)!=open)l.setBlock(at,s.setValue(DoorBlock.OPEN,open),F);}
    }
    /** The westmost uncovered columns' middle: the swimmer's lane, or what is left of it. */
    static double swimX(Pool pool){
        int open=SwimmerSite.COLUMNS-pool.hauls;if(open<=0)return SwimmerSite.POOL_X0+.5;
        int lane=Math.min(pool.lane,(open-2)/3);int x0=SwimmerSite.laneX(Math.max(0,lane));
        return open>=2?x0+1.0:SwimmerSite.POOL_X0+.5;
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();
        // Find the pools that have a reader in them, and every reader's pool.
        var seen=new HashSet<Long>();var finished=new HashMap<Long,List<ServerPlayer>>();
        for(var p:server.getPlayerList().getPlayers()){
            if(p.isSpectator()||!p.isAlive()||LiteraryVignettes.current(p)!=PLACE)continue;
            var b=LiteraryVignettes.base(p,PLACE);if(b==null||!SwimmerSite.inside(rel(p.position(),b)))continue;
            var own=own(p);if(own.getBoolean("Completed")){finished.computeIfAbsent(b.asLong(),k->new ArrayList<>()).add(p);continue;}
            var pool=POOLS.computeIfAbsent(b.asLong(),k->new Pool(b));
            if(seen.add(b.asLong()))step(p.serverLevel(),pool);
            reader(p,b,own,pool);
        }
        // 0.4.244: a pool with only finished readers in it, and no closing under way, stands as they left it. It waits a second, so
        // that a reader who has not finished it, coming in with them, starts the closing instead; a closing always starts from new.
        AFTER.keySet().removeIf(k->!finished.containsKey(k)||POOLS.containsKey(k));
        for(var entry:finished.entrySet()){
            if(POOLS.containsKey(entry.getKey()))continue;
            var first=entry.getValue().get(0);var l=first.serverLevel();var b=LiteraryVignettes.base(first,PLACE);if(b==null)continue;
            int waited=AFTER.merge(entry.getKey(),1,Integer::sum);
            if(waited==AFTER_WAIT){if(!peopleLoaded(l,b)){AFTER.put(entry.getKey(),AFTER_WAIT-1);continue;}aftermath(l,b);}
            if(waited>=AFTER_WAIT)for(var r:entry.getValue())if(Aftermath.counted(r,PLACE))cue(r,SwimmerTexts.AFTERMATH);
        }
        // A pool is put back only when nobody is in it at all: a reader who has finished and lingers by the door still holds it (0.4.238).
        for(var it=POOLS.values().iterator();it.hasNext();){var pool=it.next();if(seen.contains(pool.base.asLong())||present(server,pool.base)){pool.empty=0;continue;}
            if(++pool.empty>=EMPTY_RESET){var l=server.getLevel(NovelRooms.dimension(PLACE));if(l!=null&&l.isLoaded(pool.base)){clearActors(l,pool.base);SwimmerSite.cover(l,pool.base,0);lights(l,pool.base,0);SwimmerSite.poolLights(l,pool.base,true);door(l,pool.base,false);}it.remove();}}
    }
    /** Whether anyone at all is in the hall: every living player there who is not a spectator, finished or not. */
    static boolean present(net.minecraft.server.MinecraftServer server,BlockPos b){
        var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return false;
        for(var p:l.players())if(!p.isSpectator()&&p.isAlive()&&SwimmerSite.inside(rel(p.position(),b)))return true;
        return false;
    }
    static void step(ServerLevel l,Pool pool){
        var b=pool.base;
        // The pool starts only once its people can be seen: an actor added to a section still loading is not found again, and would be added twice.
        if(pool.tick==0){if(!peopleLoaded(l,b))return;start(l,pool);}
        pool.tick++;
        // The pace clock's sweep hand: a mark every five seconds.
        var pace=b.offset(SwimmerSite.PACE);var ps=l.getBlockState(pace);int stage=(int)(pool.tick/100%12);
        if(SwimmerBlock.is(ps,SwimmerBlock.Kind.PACE_CLOCK)&&ps.getValue(SwimmerBlock.STAGE)!=stage)l.setBlock(pace,ps.setValue(SwimmerBlock.STAGE,stage),F);
        if(pool.radio<0){pool.radio=(int)pool.tick;l.playSound(null,b.offset(SwimmerSite.RADIO),SwimmerRegistry.RADIO.get(),SoundSource.RECORDS,2F,1F);}
        song(l,pool);
        if(!pool.closed())swim(l,pool);else after(l,pool);
        attend(l,pool);
        // A foot lifts once the next has landed, and the last one down settles back when no more come.
        if(pool.lifted!=null&&pool.tick>=pool.liftAt){settle(l,pool,pool.lifted);pool.lifted=null;}
        if(pool.pressed!=null&&pool.tick>=pool.pressedUntil){settle(l,pool,pool.pressed);pool.pressed=null;}
    }
    /**
     * 0.4.233: the closing played against the song on the radio. In the second spoken part (a daydream that takes over and comes apart)
     * the hall's lights waver one after another and steady; when the last verse begins (learning to swim where someone went under, and
     * voices in the deep end), the splashing stops and nothing comes up for a breath, then goes on as if it had not. Only while the cover
     * is still being pulled; the stars on the water go out as the cover reaches them.
     */
    static void song(ServerLevel l,Pool pool){
        var b=pool.base;int part=part(pool.song());pool.part=part;
        // Each beat once, with the song or with the cover, whichever comes first, and only while the cover is still being pulled.
        if(!pool.closed()&&!pool.waved&&(part>=SECOND_TALK||pool.hauls>=WAVER_HAULS)){pool.waved=true;pool.waverUntil=(int)pool.tick+WAVER_TICKS;for(var r:readers(l,b))cue(r,SwimmerTexts.WAVER);}
        if(!pool.closed()&&!pool.breathed&&(part>=LAST_VERSE||pool.hauls>=BREATH_HAULS)){
            pool.breathed=true;pool.waverUntil=Math.min(pool.waverUntil,(int)pool.tick);pool.breathUntil=(int)pool.tick+HELD_BREATH;
            for(var r:readers(l,b)){cue(r,SwimmerTexts.BREATH);var own=own(r);own.putBoolean("Breath227",true);save(r,own);}
        }
        if(pool.flicker>=0&&pool.tick>=pool.flickerUntil){lamp(l,b,pool.flicker,pool.closed()&&pool.flicker<pool.lightsOut?1:0);pool.flicker=-1;}
        if(!pool.closed()&&pool.waverUntil>pool.tick&&pool.flicker<0&&pool.tick%9==0){
            int i=l.random.nextInt(SwimmerSite.LAMPS.size());pool.flicker=i;pool.flickerUntil=(int)pool.tick+3+l.random.nextInt(4);lamp(l,b,i,1);
            l.playSound(null,b.offset(SwimmerSite.LAMPS.get(i)),SoundEvents.REDSTONE_TORCH_BURNOUT,SoundSource.BLOCKS,.15F,1.6F);
        }
        if(!pool.closed()&&pool.tick%5==0){
            for(var star:STARS){if(covered(pool.hauls,star[0]))continue;var at=b.offset(star[0],SwimmerSite.SURFACE,star[1]);
                l.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.92F,.93F,1F),.55F),at.getX()+.5,at.getY()+1.02,at.getZ()+.5,1,.04,0,.04,0);}
        }
    }
    static void lamp(ServerLevel l,BlockPos b,int i,int stage){var at=b.offset(SwimmerSite.LAMPS.get(i));var s=l.getBlockState(at);if(SwimmerBlock.is(s,SwimmerBlock.Kind.LAMP)&&s.getValue(SwimmerBlock.STAGE)!=stage)l.setBlock(at,s.setValue(SwimmerBlock.STAGE,stage),F);}
    static void swim(ServerLevel l,Pool pool){
        var b=pool.base;var s=swimmerActor(l,pool);boolean breath=pool.breathUntil>pool.tick;
        int t=(int)(pool.tick%LENGTH_TICKS);int length=(int)(pool.tick/LENGTH_TICKS);boolean north=length%2==0;
        if(t==0){
            // At each turn: if the cover has reached their lane, they come back a lane over. Every length the same length.
            int open=SwimmerSite.COLUMNS-pool.hauls;int free=Math.max(0,(open-2)/3);
            if(pool.lane>free){pool.lane=free;for(var r:readers(l,b))cue(r,"The splashing is a lane further over now.");}
            var wall=at(b,swimX(pool),SwimmerSite.SURFACE+.5,north?SwimmerSite.POOL_SOUTH+.5:SwimmerSite.POOL_NORTH+.5);
            l.playSound(null,wall.x,wall.y,wall.z,SwimmerRegistry.TURN.get(),SoundSource.AMBIENT,1.3F,.95F+l.random.nextFloat()*.1F);
        }
        double frac=(t+1)/(double)LENGTH_TICKS;double z=north?SwimmerSite.POOL_SOUTH+.5-(SwimmerSite.POOL_SOUTH-SwimmerSite.POOL_NORTH)*frac:SwimmerSite.POOL_NORTH+.5+(SwimmerSite.POOL_SOUTH-SwimmerSite.POOL_NORTH)*frac;
        var pos=at(b,swimX(pool),SwimmerSite.SURFACE-.55,z);
        // Run 1417: one swimmer, always. A lookup that misses (the body's section not visible for a moment) adopts the body already in the
        // hall instead of adding a second one; any other swimmer found there is put away, here and every two seconds.
        if(s==null||pool.tick%40==0)s=one(l,b,pool,s);
        if(s==null&&l.areEntitiesLoaded(ChunkPos.asLong(BlockPos.containing(pos)))){s=spawn(l,SWIMMER,pos,north?180:0);pool.swimmer=s==null?null:s.getUUID();if(s!=null)s.setNoGravity(true);}
        if(s!=null){
            s.moveTo(pos.x,pos.y,pos.z,north?180:0,0);s.setYHeadRot(north?180:0);s.setDeltaMovement(Vec3.ZERO);
            // Seen only in glimpses: pushing off each wall, and now and then for a moment in the middle of a length.
            boolean seen=!breath&&(t<12||(pool.tick/80)%4==1&&pool.tick%80<9);s.setInvisible(!seen);
        }
        // Holding a breath: no splash, no sound. Their lengths go on all the same.
        if(breath)return;
        if(pool.tick%3==0)l.sendParticles(ParticleTypes.SPLASH,pos.x,b.getY()+SwimmerSite.SURFACE+.95,pos.z+(north?-.6:.6),4,.35,0,.35,0);
        if(pool.tick%16==0)l.playSound(null,pos.x,pos.y+.5,pos.z,SoundEvents.PLAYER_SWIM,SoundSource.AMBIENT,.45F,.8F+l.random.nextFloat()*.3F);
        if(pool.tick%40==0)l.sendParticles(ParticleTypes.BUBBLE,pos.x,pos.y+.3,pos.z,3,.2,.1,.2,.02);
    }
    static void after(ServerLevel l,Pool pool){
        var b=pool.base;int since=pool.since();
        if(since==QUIET/2)for(var r:readers(l,b))cue(r,"\"There.\"");
        // The lights go off bank by bank, from the far end, leaving the two by the fire door. 0.4.581: a bank is a row of four, and
        // how many are off follows the time since the cover closed, so a closing that is hurried on is as dark as it should be.
        int lamps=SwimmerSite.LAMPS.size()-KEEP_LIT;
        int due=since<QUIET?0:Math.min(lamps,SwimmerSite.BANK*((since-QUIET)/LIGHTS_EVERY+1));
        if(due>pool.lightsOut){boolean first=pool.lightsOut==0;pool.lightsOut=due;lights(l,b,due);l.playSound(null,b.offset(SwimmerSite.LAMPS.get(due-1)),SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.8F,.6F);
            if(first)for(var r:readers(l,b))cue(r,"Switches click along the wall, one bank of lights at a time.");}
        if(since<STEPS_AFTER)return;
        if(pool.stepAt==null){
            // 0.4.581: never in the column under the cover's bar and strap: a foot comes up under the cover, beside its edge.
            pool.stepAt=new BlockPos(Math.max(SwimmerSite.POOL_X0+1,SwimmerSite.POOL_X0+(pool.lane>0?1:0)+3*Math.max(0,pool.lane)),SwimmerSite.SURFACE,SwimmerSite.POOL_NORTH+2);
            for(var r:readers(l,b)){cue(r,"Something crosses the cover. You can see where: the cover rises under each step.");var own=own(r);if(!own.getBoolean("Ready")){own.putBoolean("Ready",true);own.putString("Outcome","held_the_door");own.putInt("Beat",own.getInt("Visit"));}save(r,own);}
        }
        if((since-STEPS_AFTER)%STEP_EVERY!=0||pool.stepAt==null)return;
        // Toward whoever is nearest, a cell at a time, and up under the cover at each.
        var target=nearest(l,pool);if(target==null)return;
        var now=pool.stepAt;int dx=Integer.signum(target.getX()-now.getX()),dz=Integer.signum(target.getZ()-now.getZ());
        boolean across=Math.abs(target.getX()-now.getX())>=Math.abs(target.getZ()-now.getZ());
        BlockPos next=now;
        if(dx!=0||dz!=0){next=across?now.offset(dx,0,0):now.offset(0,0,dz);}
        else if((since-STEPS_AFTER)%(STEP_EVERY*3)!=0)return;
        // 0.4.581: its feet point the way it steps; where it has stopped and shifts its weight, the way it came.
        if(!next.equals(now))pool.way=across?(dx>0?Direction.EAST:Direction.WEST):(dz>0?Direction.SOUTH:Direction.NORTH);
        pool.stepAt=next;press(l,pool,next);
    }
    /** The cover cell nearest the nearest reader: where the steps are going. */
    @Nullable static BlockPos nearest(ServerLevel l,Pool pool){
        ServerPlayer best=null;double d=Double.MAX_VALUE;var b=pool.base;
        for(var r:readers(l,b)){var at=Vec3.atBottomCenterOf(b.offset(pool.stepAt));double dd=r.position().distanceToSqr(at);if(dd<d){d=dd;best=r;}}
        if(best==null)return null;var r=rel(best.position(),b);
        int x=Math.max(SwimmerSite.POOL_X0+1,Math.min(SwimmerSite.POOL_X1,(int)Math.floor(r.x))),z=Math.max(SwimmerSite.POOL_NORTH,Math.min(SwimmerSite.POOL_SOUTH,(int)Math.floor(r.z)));
        return new BlockPos(x,SwimmerSite.SURFACE,z);
    }
    static void press(ServerLevel l,Pool pool,BlockPos rel){
        var b=pool.base;var at=b.offset(rel);var s=l.getBlockState(at);
        // 0.4.581: a foot, the left and the right by turns, pointing the way it walks. The one before stays down until this one has
        // landed, as a walker's does. It was one shapeless rise that went down as the next came up.
        if(pool.pressed!=null&&!pool.pressed.equals(rel)){settle(l,pool,pool.lifted);pool.lifted=pool.pressed;pool.liftAt=(int)pool.tick+FOOT_LIFT;}
        if(SwimmerBlock.is(s,SwimmerBlock.Kind.COVER))l.setBlock(at,s.setValue(SwimmerBlock.STAGE,SwimmerBlock.foot(pool.way,pool.right)),F);
        pool.right=!pool.right;
        pool.pressed=rel;pool.pressedUntil=(int)pool.tick+FOOT_STAYS;pool.lastStep=(int)pool.tick;
        l.playSound(null,at,SwimmerRegistry.STEP.get(),SoundSource.HOSTILE,1.2F,.9F+l.random.nextFloat()*.15F);
        l.sendParticles(ParticleTypes.FALLING_WATER,at.getX()+.5,at.getY()+1.1,at.getZ()+.5,8,.4,.05,.4,0);
        for(var r:readers(l,b)){
            var cell=BlockPos.containing(r.getX(),r.getY()-.2,r.getZ());
            if(cell.equals(at)||cell.equals(at.above())){r.setDeltaMovement(r.getDeltaMovement().add(0,.32,0));r.hurtMarked=true;var own=own(r);if(!own.getBoolean("Pressed227")){own.putBoolean("Pressed227",true);cue(r,"It presses up under your feet.");}save(r,own);}
        }
    }
    /** A cell of the cover lies flat again, or as the leading edge it was. */
    static void settle(ServerLevel l,Pool pool,@Nullable BlockPos rel){
        if(rel==null)return;var at=pool.base.offset(rel);var s=l.getBlockState(at);
        if(SwimmerBlock.pressed(s))l.setBlock(at,s.setValue(SwimmerBlock.STAGE,rel.getX()==SwimmerSite.POOL_X0?2:0),F);
    }
    static void attend(ServerLevel l,Pool pool){
        var a=attendantActor(l,pool);var b=pool.base;if(a==null)return;
        Vec3 goal;
        if(!pool.closed()){
            int x=pool.hauls==0?SwimmerSite.ATTENDANT_START.getX():Math.max(SwimmerSite.POOL_X0,SwimmerSite.coverColumn(pool.hauls-1));
            goal=Vec3.atBottomCenterOf(b.offset(x,0,SwimmerSite.POOL_NORTH-2));
            if(!SceneFootpaths.walk(a,goal,.12)){}else{var r=closest(l,b,a.position());if(r!=null)SceneFootpaths.face(a,r.position(),12);}
            return;
        }
        // Along the north deck, down the west deck, to the fire door, switching the lights off as they go; then holding it.
        var corner=Vec3.atBottomCenterOf(b.offset(-12,0,SwimmerSite.POOL_NORTH-2));var hold=Vec3.atBottomCenterOf(b.offset(SwimmerSite.HOLD));
        boolean turned=a.position().x<b.getX()-11;
        if(!turned&&pool.since()>=QUIET/2){SceneFootpaths.walk(a,corner,.13);return;}
        if(turned&&!pool.held){if(SceneFootpaths.walk(a,hold,.13)){pool.held=true;door(l,b,true);for(var r:readers(l,b))cue(r,"The attendant is at the fire door, holding it open. They don't say anything.");}return;}
        if(pool.held){door(l,b,true);var r=closest(l,b,a.position());if(r!=null)SceneFootpaths.face(a,r.position(),8);}
    }
    /**
     * 0.4.403, the owner's playtest: the attendant had nothing to say to a reader who turned to them. What they say follows the
     * closing: the ask before the first haul, a word while the cover goes across (each time the next one), the lights once it is on,
     * and nothing at the held door, only a nod at it.
     */
    public static void talk(ServerPlayer p,LiteraryActor actor,CompoundTag own){
        if(actor.phase()!=ATTENDANT)return;
        var b=LiteraryVignettes.base(p,PLACE);var pool=b==null?null:pool(b);
        if(pool==null)return;
        if(pool.held){cue(p,SwimmerTexts.ATTENDANT_HELD);return;}
        if(pool.closed()){cue(p,SwimmerTexts.ATTENDANT_CLOSED);return;}
        if(pool.hauls==0){cue(p,SwimmerTexts.ATTENDANT_ASK);return;}
        int k=own.getInt("Talk227");own.putInt("Talk227",k+1);
        cue(p,SwimmerTexts.attendant(k));
    }
    @Nullable static ServerPlayer closest(ServerLevel l,BlockPos b,Vec3 from){ServerPlayer best=null;double d=Double.MAX_VALUE;for(var r:readers(l,b)){double dd=r.position().distanceToSqr(from);if(dd<d){d=dd;best=r;}}return best;}

    // ------------------------------------------------------------------------------------------------ each reader
    static void reader(ServerPlayer p,BlockPos b,CompoundTag own,Pool pool){
        var r=rel(p.position(),b);boolean changed=false;
        if(SwimmerSite.inHall(r)&&!own.getBoolean("Hall227")){own.putBoolean("Hall227",true);changed=true;cue(p,"\"Closing up. Take the strap at the shallow end, would you? We pull it across together.\"");}
        var s=swimmerActor(p.serverLevel(),pool);
        if(s!=null&&!s.isInvisible()&&p.hasLineOfSight(s)&&!own.getBoolean("Seen227")){own.putBoolean("Seen227",true);changed=true;cue(p,"Under the water, for a moment, something pale. Then only the splashing.");}
        // 0.4.233: under the water at the deep end, while the pool is still open, voices nobody can make out, for this reader alone.
        if(!pool.closed()&&p.level().getFluidState(BlockPos.containing(p.getEyePosition())).is(net.minecraft.tags.FluidTags.WATER)&&SwimmerSite.inPool(BlockPos.containing(r))&&r.z<DEEP_FROM+1&&pool.tick%60==0){
            p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(DoorSounds.Carry.VOICES.sound,SoundSource.AMBIENT,p.getX(),p.getEyeY(),p.getZ(),.9F,.7F,p.getRandom().nextLong()));
            if(!own.getBoolean("Voices227")){own.putBoolean("Voices227",true);changed=true;cue(p,SwimmerTexts.VOICES);}
        }
        // Walking out through the held door is the ending.
        if(own.getBoolean("Ready")&&pool.held&&!own.getBoolean("Completed")&&p.position().distanceToSqr(at(b,SwimmerSite.EXIT_X+.5,0,SwimmerSite.EXIT_Z+.5))<4.0){complete(p,own);changed=true;}
        if(changed)save(p,own);
    }
    /** The ending: the closing log in the reader's hand as they go, and their Witness line. */
    public static void complete(ServerPlayer p,CompoundTag own){
        if(own.getBoolean("Completed")||!own.getBoolean("Ready"))return;
        var book=LiteraryTexts.ending(p.getGameProfile().getName(),PLACE,own);
        if(!p.getInventory().add(book))p.drop(book,false);
        var story=WitnessAccount.Story.of(PLACE.id());if(story!=null)WitnessAccount.resolve(p,story,own.getString("Outcome"));
        own.putBoolean("Completed",true);LabyrinthData.get(p.server).setCompleted(PLACE.id(),true);
        cue(p,"The attendant puts the closing log in your hand as you pass, and lets the door go behind you.");
    }
}
