package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseExperience;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.410, the Kept (docs/THE_KEPT_0_4_410.md): the inside of a machine, and four people it has kept longer than a lifetime. It has
 * promised them food at the end of a long walk. They walk with the reader, bickering, through a corridor of cabinets, a cave of ice it has
 * made out of its own casing, and a thicket of wire, to a hall with a hundred chairs that turn to face whoever is nearest. At the head of
 * the hall is a crate of tins, and nothing to open them with. Then, one at a time, they ask to be let go: each stands at a door in the
 * wall and asks the reader to shut their eyes and open it, and walks out into glare, and is gone.
 *
 * <p>As the reader walks, the machine reads them their own record from the screens: how many times they have died, the companions the
 * House saw them lose, the stories they walked out of before the end. It only ever says what is true.
 *
 * <p>The four are shared by everyone in the machine, and each release counts for every reader who had their eyes shut by that door when
 * it opened. A reader who has let all four go, in one visit or across several, is noticed: every screen in the machine wakes, and the
 * machine takes their mouth. It is permanent (see {@link FaceLoss}); their chat is left alone. The machine puts the four back whenever it
 * is empty, so later visits can let them go again, and they remember having been out.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Kept {
    private Kept(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.KEPT;
    /** The four; stranger phases one hundred and thirty-five to one hundred and thirty-eight. */
    public static final int SILAS=135,PETRA=136,WENDELL=137,TAMSIN=138;
    static final String[] SKINS={"silas","petra","wendell","tamsin"};
    public static boolean cast(int phase){return phase>=SILAS&&phase<=TAMSIN;}
    public static String skin(int phase){return "kept_"+SKINS[phase-SILAS];}
    /** Where the walk has got to, for everyone in the machine. */
    public static final int SITTING=0,WALKING=1,AT_TABLE=2,BROKEN=3,ASKING=4,DONE=5;
    /** Poses: sat on the floor, knees up; walking hunched; a hand flat on a cell's door; hands to the face; reaching for the crate. */
    public static final int SIT=1,HUNCH=2,HAND=3,WEEP=4,REACH=5;
    /** Ticks: eyes shut before the hand finds the door; how long an empty machine waits before it puts them back; how long before it notices. */
    public static final int CLOSE_EYES=60,EMPTY_RESET=200,NOTICE=60,TAKE=160;
    public static final String TALK="the_kept";
    public static final String OUTCOME="let_them_all_go";

    static final class Scene {
        final BlockPos base;int stage=SITTING,since,empty,bicker,pair,asking=-1,releasing=-1,releaseT,awake;long tick;boolean fresh=true,sent,cold,wire,toHall;
        final UUID[] cast=new UUID[4];final boolean[] gone=new boolean[4];
        final Map<Integer,List<Vec3>> routes=new HashMap<>();final Map<Integer,Integer> leg=new HashMap<>();
        final Map<UUID,Long> said=new HashMap<>();
        Scene(BlockPos b){base=b;}
    }
    private static final Map<Long,Scene> SCENES=new HashMap<>();
    /** How long each reader has had their eyes shut by a cell, and how far each noticed reader has got. */
    private static final Map<UUID,Integer> CLOSED=new HashMap<>(),NOTICED=new HashMap<>();
    public static void clearAll(){SCENES.clear();CLOSED.clear();NOTICED.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){CLOSED.remove(e.getEntity().getUUID());}

    // ------------------------------------------------------------------------------------------------ for tests
    @Nullable static Scene scene(BlockPos b){return SCENES.get(b.asLong());}
    public static int stage(BlockPos b){var d=scene(b);return d==null?-1:d.stage;}
    public static int asking(BlockPos b){var d=scene(b);return d==null?-1:d.asking;}
    public static boolean gone(BlockPos b,int who){var d=scene(b);return d!=null&&d.gone[who];}
    @Nullable public static LiteraryActor actor(ServerLevel l,BlockPos b,int who){var d=scene(b);return d==null?null:actor(l,d.cast[who]);}
    public static int closed(ServerPlayer p){return CLOSED.getOrDefault(p.getUUID(),0);}
    public static void closeEyes(ServerPlayer p,int ticks){CLOSED.put(p.getUUID(),ticks);}
    public static void skip(BlockPos b,int since){var d=scene(b);if(d!=null)d.since=Math.max(d.since,since);}
    /** For tests: the crate tried, so the four understand it now. */
    public static void tryCrate(ServerLevel l,BlockPos b){var d=scene(b);if(d!=null&&d.stage<=AT_TABLE){d.stage=BROKEN;d.since=0;}}

    // ------------------------------------------------------------------------------------------------ records, speech, the machine's voice
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void refresh(CompoundTag held,CompoundTag fresh){for(var key:List.copyOf(held.getAllKeys()))held.remove(key);held.merge(fresh);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void sound(ServerLevel l,BlockPos at,SoundEvent e,float volume,float pitch){l.playSound(null,at,e,SoundSource.NEUTRAL,volume,pitch);}
    /** The machine, in its own hand: across the middle of the reader's view, under nothing. */
    static void machine(ServerPlayer p,String line){
        p.connection.send(new ClientboundSetTitlesAnimationPacket(6,70,20));p.connection.send(new ClientboundSetTitleTextPacket(Component.empty()));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(line).withStyle(ChatFormatting.WHITE)));
        p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(KeptRegistry.WAKE,SoundSource.BLOCKS,p.getX(),p.getY()+1,p.getZ(),.5F,1F,p.getRandom().nextLong()));
    }
    /** One of them says something: over their head, and in the action bar for readers near enough to hear it. */
    static void speak(ServerLevel l,Scene d,int who,String line){
        var a=actor(l,d.cast[who]);if(a==null)return;a.say(line);a.speaks(line);d.said.put(a.getUUID(),d.tick);
        for(var r:readers(l,d.base))if(r.distanceToSqr(a)<20*20)r.displayClientMessage(Component.literal(KeptTexts.NAMES[who]+": “"+line+"”").withStyle(ChatFormatting.GRAY),true);
    }
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }
    static List<Vec3> where(List<ServerPlayer> here,BlockPos b){var out=new ArrayList<Vec3>();for(var r:here)out.add(rel(r.position(),b));return out;}

    // ------------------------------------------------------------------------------------------------ the four
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    @Nullable static LiteraryActor spawn(ServerLevel l,Scene d,int who,Vec3 at,float yaw){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,SILAS+who);place(a,d.base,at,yaw);
        if(!l.addFreshEntity(a))return null;d.cast[who]=a.getUUID();return a;
    }
    static void place(LiteraryActor a,BlockPos b,Vec3 r,float yaw){a.moveTo(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.setDeltaMovement(Vec3.ZERO);}
    static AABB area(BlockPos b){return new AABB(b.offset(KeptSite.WEST,-3,KeptSite.NORTH)).minmax(new AABB(b.offset(KeptSite.EAST,KeptSite.TOP,2)));}
    static void clearActors(ServerLevel l,BlockPos b){for(var a:l.getEntitiesOfClass(LiteraryActor.class,area(b),a->a.role()==LiteraryActor.STRANGER&&cast(a.phase())))a.discard();}
    static boolean loaded(ServerLevel l,Scene d){var at=d.base.offset(0,0,-12);return l.isLoaded(at)&&l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at));}
    static void send(Scene d,int who,Vec3... points){d.routes.put(who,new ArrayList<>(List.of(points)));d.leg.put(who,0);}
    static boolean busy(Scene d,int who){return d.routes.containsKey(who);}
    static void walk(ServerLevel l,Scene d){
        for(var who:List.copyOf(d.routes.keySet())){
            var a=actor(l,d.cast[who]);if(a==null){d.routes.remove(who);continue;}
            var route=d.routes.get(who);int i=d.leg.get(who);
            if(i>=route.size()){d.routes.remove(who);d.leg.remove(who);continue;}
            var target=world(d.base,route.get(i));
            if(SceneFootpaths.walk(a,target,.1)||a.position().distanceToSqr(target)<.09)d.leg.put(who,i+1);
        }
    }
    /** Heads turn to whoever is nearest; sitting, they look across the corridor. */
    static void look(ServerLevel l,Scene d,List<ServerPlayer> here){
        for(int who=0;who<4;who++){var a=actor(l,d.cast[who]);if(a==null||busy(d,who))continue;
            ServerPlayer near=null;double best=d.stage==SITTING?36:144;for(var r:here){double dd=r.distanceToSqr(a);if(dd<best){best=dd;near=r;}}
            if(near!=null)SceneFootpaths.face(a,near.getEyePosition(),60);
            var t=d.said.get(a.getUUID());if(t!=null&&d.tick-t>100){a.setCustomNameVisible(false);a.silent();d.said.remove(a.getUUID());}
        }
    }

    // ------------------------------------------------------------------------------------------------ arriving
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;var d=SCENES.computeIfAbsent(b.asLong(),k->new Scene(b));
        own.putInt("Visits",own.getInt("Visits")+1);own.putInt("Said",0);own.putInt("ReleasedBefore",own.getInt("Released"));
        cue(p,"Cabinets on both sides, humming, as far as the light goes. The air smells of hot dust.");
    }

    // ------------------------------------------------------------------------------------------------ the machine's clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(SCENES.isEmpty())return;var server=e.getServer();var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var d:List.copyOf(SCENES.values())){
            d.tick++;var here=readers(l,d.base);
            if(here.isEmpty()){if(++d.empty>=EMPTY_RESET&&loaded(l,d)){reset(l,d);SCENES.remove(d.base.asLong());}continue;}
            d.empty=0;
            if(d.fresh){if(!loaded(l,d))continue;d.fresh=false;start(l,d);}
            walk(l,d);
            if(d.tick%2==0)look(l,d,here);
            bodies(l,d,here);
            if(d.tick%10==0){var at=where(here,d.base);KeptSite.chairs(l,d.base,at.stream().filter(KeptSite::inHall).toList());if(d.awake<=0&&KeptSite.screens(l,d.base,at,7)>0&&d.tick%40==0)sound(l,here.get(0).blockPosition(),KeptRegistry.WAKE.get(),.3F,1.2F);}
            if(d.awake>0&&--d.awake==0)KeptSite.allScreens(l,d.base,0);
            stage(l,d,here);
            d.since++;
        }
        noticed(server);
    }
    /** The four put where they start: sat on the floor against the cabinets. */
    static void start(ServerLevel l,Scene d){
        clearActors(l,d.base);KeptSite.reset(l,d.base);
        for(int who=0;who<4;who++){var a=spawn(l,d,who,KeptSite.SEATS[who],KeptSite.SEAT_YAW[who]);if(a!=null)a.gesture(SIT);}
        d.stage=SITTING;d.since=0;
    }
    static void next(Scene d,int stage){d.stage=stage;d.since=-1;}

    /** What each reader's own body is doing: cold in the cave, eyes shut by a cell's door. */
    static void bodies(ServerLevel l,Scene d,List<ServerPlayer> here){
        for(var r:here){
            var at=rel(r.position(),d.base);
            if(KeptSite.inCave(at))r.setTicksFrozen(Math.min(130,r.getTicksFrozen()+3));
            int cell=d.stage==ASKING&&d.asking>=0&&d.releasing<0?d.asking:-1;
            if(cell>=0&&r.isShiftKeyDown()&&KeptSite.atCell(at,cell,3.5)){
                int t=CLOSED.merge(r.getUUID(),1,Integer::sum);
                if(t==1)cue(r,"You shut your eyes.");
                if(t%10==1)r.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,30,0,false,false,false));
                if(t==CLOSE_EYES)cue(r,"Your eyes are shut. Your hand finds the door.");
            }else if(CLOSED.containsKey(r.getUUID())&&d.releasing<0){CLOSED.remove(r.getUUID());r.removeEffect(MobEffects.BLINDNESS);}
        }
    }

    static void stage(ServerLevel l,Scene d,List<ServerPlayer> here){
        int t=d.since;
        switch(d.stage){
            case SITTING->sitting(l,d,here,t);
            case WALKING->walking(l,d,here,t);
            case AT_TABLE->atTable(l,d,here,t);
            case BROKEN->broken(l,d,here,t);
            case ASKING->asking(l,d,here,t);
            default->{}
        }
    }
    /** Sat on the floor until a reader comes near enough, or long enough that they get up anyway. */
    static void sitting(ServerLevel l,Scene d,List<ServerPlayer> here,int t){
        boolean near=false;for(int who=0;who<4;who++){var a=actor(l,d.cast[who]);if(a!=null)for(var r:here)if(r.distanceTo(a)<6)near=true;}
        if(t==60)speak(l,d,1,"Look. Somebody new.");
        if(near&&t>=40||t>=600){
            for(int who=0;who<4;who++){var a=actor(l,d.cast[who]);if(a!=null)a.gesture(HUNCH);}
            speak(l,d,0,"It's started, then. Come on. It said there's food at the end.");
            next(d,WALKING);
        }
    }
    /** Behind whoever has gone furthest, in single file down the middle, as far as the wire. */
    static void walking(ServerLevel l,Scene d,List<ServerPlayer> here,int t){
        double lead=0;for(var r:here)lead=Math.min(lead,rel(r.position(),d.base).z);
        boolean inHall=false;for(var r:here)if(KeptSite.inHall(rel(r.position(),d.base)))inHall=true;
        if(lead<KeptSite.THICKET_S-3||inHall){
            // They go round the wire some way of their own, and are there at the mouth of the hall before anyone comes through.
            for(int who=0;who<4;who++){var a=actor(l,d.cast[who]);if(a!=null){d.routes.remove(who);place(a,d.base,KeptSite.HALL_MOUTH[who],180);}}
            next(d,AT_TABLE);return;
        }
        for(int who=0;who<4;who++){
            var a=actor(l,d.cast[who]);if(a==null)continue;
            double z=Math.min(-6-who*1.5,lead+2.5+who*1.6);
            Vec3 target=z>KeptSite.JUNGLE_S+.5?KeptSite.walk(z):new Vec3(.5+(who-1.5)*1.6,0,Math.max(z,KeptSite.THICKET_S+1.4));
            boolean crowding=false;for(var r:here)if(r.distanceToSqr(a)<1.6)crowding=true;
            var goal=world(d.base,target);
            if(!crowding&&a.position().distanceToSqr(goal)>.5)SceneFootpaths.walk(a,goal,.09);
        }
        if(!d.cold&&lead<=KeptSite.CAVE_S){d.cold=true;speak(l,d,2,KeptTexts.COLD[0]);}
        if(d.cold&&!d.wire&&lead<=KeptSite.CAVE_S-8&&t%200==100)speak(l,d,1,KeptTexts.COLD[1]);
        if(!d.wire&&lead<=KeptSite.JUNGLE_S){d.wire=true;d.bicker=-100;speak(l,d,0,KeptTexts.WIRE_ROUND);}
        if(d.wire&&d.bicker==-40){speak(l,d,3,KeptTexts.WIRE_UNDER);}
        // Between the rooms they bicker, a pair at a time.
        d.bicker++;
        if(d.bicker>=260){d.bicker=0;d.pair++;}
        var pair=KeptTexts.WALKING[Math.floorMod(d.pair,KeptTexts.WALKING.length)];
        if(d.pair>0&&d.bicker==0)speak(l,d,Integer.parseInt(pair[0]),pair[1]);
        if(d.pair>0&&d.bicker==70&&pair.length>2)speak(l,d,Integer.parseInt(pair[2]),pair[3]);
        if(d.pair>0&&d.bicker==140&&pair.length>4)speak(l,d,Integer.parseInt(pair[4]),pair[5]);
    }
    /** Waiting at the mouth of the hall; up the aisle to the crate once a reader is in the hall; the crate tried, or long enough. */
    static void atTable(ServerLevel l,Scene d,List<ServerPlayer> here,int t){
        boolean inHall=false;for(var r:here)if(KeptSite.inHall(rel(r.position(),d.base)))inHall=true;
        if(!d.toHall&&inHall){
            d.toHall=true;d.since=0;
            for(int who=0;who<4;who++)send(d,who,KeptSite.AISLE_S,KeptSite.AISLE_N,KeptSite.AT_CRATE[who]);
            speak(l,d,2,KeptTexts.HALL[0]);
            for(var r:here){var own=own(r);int said=own.getInt("Said");if((said&8)==0&&KeptSite.inHall(rel(r.position(),d.base))){own.putInt("Said",said|8);save(r,own);machine(r,KeptTexts.chairs(4+here.size()));}}
            return;
        }
        if(!d.toHall)return;
        if(t==90)speak(l,d,1,KeptTexts.HALL[1]);
        boolean there=true;for(int who=0;who<4;who++)if(busy(d,who))there=false;
        if(there&&!d.sent){d.sent=true;d.since=0;for(int who=0;who<4;who++){var a=actor(l,d.cast[who]);if(a!=null)a.gesture(REACH);}speak(l,d,2,KeptTexts.CRATE[0]);}
        if(d.sent&&t>=400)next(d,BROKEN);
    }
    /** The tins, and nothing to open them with. */
    static void broken(ServerLevel l,Scene d,List<ServerPlayer> here,int t){
        if(t==0)speak(l,d,2,KeptTexts.CRATE[1]);
        if(t==40)sound(l,d.base.offset(KeptSite.CRATE),KeptRegistry.TINS.get(),1F,1F);
        if(t==90){speak(l,d,1,KeptTexts.CRATE[2]);for(int who=0;who<4;who++){var a=actor(l,d.cast[who]);if(a!=null)a.gesture(0);}}
        if(t==150)speak(l,d,0,KeptTexts.CRATE[3]);
        if(t==200){speak(l,d,3,KeptTexts.CRATE[4]);var a=actor(l,d.cast[3]);if(a!=null){a.gesture(WEEP);sound(l,a.blockPosition(),KeptRegistry.WEEP.get(),.8F,1F);}}
        if(t==260)for(var r:here)if(KeptSite.inHall(rel(r.position(),d.base)))machine(r,KeptTexts.FOOD);
        if(t>=340){d.asking=-1;askNext(l,d);}
    }
    /** The next of them still here goes to their door and asks. */
    static void askNext(ServerLevel l,Scene d){
        int next=-1;for(int who=0;who<4;who++)if(!d.gone[who]){next=who;break;}
        d.releasing=-1;
        if(next<0){next(d,DONE);d.asking=-1;return;}
        d.asking=next;next(d,ASKING);
        var a=actor(l,d.cast[next]);if(a==null)return;a.gesture(HUNCH);
        var front=KeptSite.cellFront(next);double side=front.x<0?-1:1;
        var at=rel(a.position(),d.base);
        if(at.z>-92.5)send(d,next,new Vec3(side*5.5,0,-93.5),new Vec3(side*13.0,0,-93.5),front);
        else send(d,next,new Vec3(side*13.0,0,at.z),front);
    }
    static void asking(ServerLevel l,Scene d,List<ServerPlayer> here,int t){
        int who=d.asking;if(who<0)return;var a=actor(l,d.cast[who]);
        if(d.releasing==who){
            d.releaseT++;
            if(d.releaseT==30)speak(l,d,who,KeptTexts.THANKS[who]);
            if(a!=null&&(d.releaseT>=120||!busy(d,who))&&d.releaseT>=40){a.discard();d.cast[who]=null;d.routes.remove(who);}
            if(d.releaseT==140){KeptSite.door(l,d.base,who,false);sound(l,d.base.offset(KeptSite.CELL_DOORS[who]),SoundEvents.IRON_DOOR_CLOSE,1F,.8F);}
            if(d.releaseT>=160){d.gone[who]=true;for(var r:here)if(CLOSED.remove(r.getUUID())!=null)r.removeEffect(MobEffects.BLINDNESS);askNext(l,d);}
            return;
        }
        if(a==null||busy(d,who))return;
        if(a.gesture()!=HAND){a.gesture(HAND);d.since=0;t=0;SceneFootpaths.face(a,world(d.base,new Vec3(KeptSite.CELL_DOORS[who].getX()+.5,1.5,KeptSite.CELL_DOORS[who].getZ()+.5)),180);}
        if(t==20)speak(l,d,who,KeptTexts.ASK[who][0]);
        if(t==140||t>140&&(t-140)%400==0)speak(l,d,who,KeptTexts.ASK[who][1]);
    }
    /** The door opened with eyes shut: the one asking walks into the glare, and everyone with their eyes shut by the door was there for it. */
    static void release(ServerLevel l,Scene d,ServerPlayer opener){
        int who=d.asking;var a=actor(l,d.cast[who]);
        KeptSite.door(l,d.base,who,true);sound(l,d.base.offset(KeptSite.CELL_DOORS[who]),KeptRegistry.GLARE.get(),1F,1F);
        d.releasing=who;d.releaseT=0;
        if(a!=null){a.gesture(0);send(d,who,KeptSite.cellEnd(who));}
        for(var r:readers(l,d.base)){
            if(r!=opener&&(closed(r)<CLOSE_EYES||!KeptSite.atCell(rel(r.position(),d.base),who,5)))continue;
            var own=own(r);int before=own.getInt("Released");int now=before|1<<who;own.putInt("Released",now);
            own.putInt("Releases",own.getInt("Releases")+1);
            if(now==15&&before!=15&&!own.getBoolean("Taken"))NOTICED.put(r.getUUID(),0);
            save(r,own);
        }
    }
    /** A reader who has let all four go: every screen wakes, the machine asks what they would say to it, and takes their mouth. */
    static void noticed(net.minecraft.server.MinecraftServer server){
        if(NOTICED.isEmpty())return;
        for(var id:List.copyOf(NOTICED.keySet())){
            var p=server.getPlayerList().getPlayer(id);if(p==null||LiteraryVignettes.current(p)!=PLACE){NOTICED.remove(id);continue;}
            int t=NOTICED.merge(id,1,Integer::sum);var b=LiteraryVignettes.base(p,PLACE);
            if(t==NOTICE&&b!=null){var d=scene(b);KeptSite.allScreens(p.serverLevel(),b,1);if(d!=null)d.awake=TAKE+60;
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(KeptRegistry.NOTICE,SoundSource.HOSTILE,p.getX(),p.getY()+1,p.getZ(),1F,1F,p.getRandom().nextLong()));machine(p,KeptTexts.NOTICED);}
            if(t==NOTICE+50)machine(p,KeptTexts.ASKED);
            if(t==TAKE){
                FaceLoss.take(p);
                var own=own(p);own.putBoolean("Taken",true);
                LiteraryVignettes.ready(p,PLACE,own,OUTCOME);save(p,own);
                cue(p,"Your hand goes to your face. There is nothing there to open.");
                if(b!=null)tray(p,b);
            }
            if(t==TAKE+60){machine(p,KeptTexts.AFTER);NOTICED.remove(id);}
        }
    }
    static void tray(ServerPlayer p,BlockPos b){var at=b.offset(KeptSite.TRAY);var st=p.serverLevel().getBlockState(at);if(KeptBlock.is(st,KeptBlock.Kind.TRAY))p.connection.send(new ClientboundBlockUpdatePacket(at,st.setValue(KeptBlock.STAGE,1)));}

    // ------------------------------------------------------------------------------------------------ each reader
    /** Every five ticks, for each reader in the machine: what the screens read them, and the hum. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag held){
        save(p,held);var own=own(p);var d=scene(b);var l=p.serverLevel();
        if(d==null){refresh(held,own);return;}
        var r=rel(p.position(),b);int said=own.getInt("Said");
        long beat=l.getGameTime()/5+p.getId();
        if(beat%24==0)p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(KeptRegistry.HUM,SoundSource.AMBIENT,p.getX()+2,p.getY()+2,p.getZ()-3,.4F,1F,p.getRandom().nextLong()));
        if((said&16)==0&&own.getInt("Visits")>=2&&r.z<-2){said|=16;machine(p,KeptTexts.returned(own.getInt("Visits")));}
        else if((said&1)==0&&r.z<-8){said|=1;int deaths=p.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));own.putInt("ToldDeaths",deaths);machine(p,KeptTexts.deaths(deaths));}
        else if((said&2)==0&&KeptSite.inCave(r)&&r.z<KeptSite.CAVE_S-3){said|=2;var names=lost(p);var list=new ListTag();for(var n:names)list.add(StringTag.valueOf(n));own.put("ToldLost",list);machine(p,KeptTexts.lost(names));}
        else if((said&4)==0&&KeptSite.inJungle(r)){said|=4;var places=left(p);var list=new ListTag();for(var n:places.subList(0,Math.min(3,places.size())))list.add(StringTag.valueOf(n));own.put("ToldLeft",list);machine(p,KeptTexts.left(places));}
        own.putInt("Said",said);
        // A reader who has let all four go is noticed, whenever they are next in the machine, until it has been done.
        if((own.getInt("Released")&15)==15&&!own.getBoolean("Taken"))NOTICED.putIfAbsent(p.getUUID(),0);
        if(own.getBoolean("Ready")&&!own.getBoolean("Completed")&&l.getGameTime()%100==0)tray(p,b);
        refresh(held,own);
    }
    /** The companions the House saw this reader lose, by name, oldest first. */
    static List<String> lost(ServerPlayer p){
        var out=new ArrayList<String>();var list=HotelVignette.personal(LabyrinthData.get(p.server),p.getUUID()).getList("LostPets",net.minecraft.nbt.Tag.TAG_COMPOUND);
        for(int i=0;i<list.size();i++){var n=list.getCompound(i).getString("Name");if(!n.isBlank()&&!out.contains(n))out.add(n);}
        return out;
    }
    /** The stories this reader walked out of before the end, by the House's prose names for them, most walked out of first. */
    static List<String> left(ServerPlayer p){
        var retreats=HouseExperience.record(LabyrinthData.get(p.server),p.getUUID()).getCompound("Retreats");
        var ids=new ArrayList<>(retreats.getAllKeys());ids.removeIf(id->id.equals(PLACE.id()));
        ids.sort(Comparator.comparingInt((String id)->-retreats.getInt(id)).thenComparing(id->id));
        var out=new ArrayList<String>();for(var id:ids){var name=StaircaseProse.place(id);if(!out.contains(name))out.add(name);}
        return out;
    }

    // ------------------------------------------------------------------------------------------------ talk
    public static void talk(ServerPlayer p,LiteraryActor actor){
        var b=LiteraryVignettes.base(p,PLACE);var d=b==null?null:scene(b);if(d==null||!cast(actor.phase()))return;
        int who=actor.phase()-SILAS;var own=own(p);var name=KeptTexts.NAMES[who];
        boolean before=(own.getInt("ReleasedBefore")&1<<who)!=0;
        String line=KeptTexts.talk(who,d.stage,before,before);
        Talk.begin(p,TALK,name);Talk.line(p,name,line);actor.say(line);actor.speaks(line);d.said.put(actor.getUUID(),d.tick);
        Talk.ask(p,Talk.act("Leave it there.",Talk::close));
    }

    // ------------------------------------------------------------------------------------------------ the machine's things
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag held){
        save(p,held);boolean handled=clicked(p,b,rel,own(p));refresh(held,own(p));return handled;
    }
    static boolean clicked(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var d=scene(b);var st=l.getBlockState(b.offset(rel));
        if(st.getBlock() instanceof DoorBlock){
            var lower=st.getValue(DoorBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER?rel:rel.below();
            if(lower.equals(KeptSite.ONWARD))return false;
            int cell=KeptSite.cellOf(lower);if(cell<0)return false;
            if(d==null||d.stage!=ASKING||d.asking!=cell||d.releasing>=0||busy(d,cell)){cue(p,KeptSite.doorOpen(l,b,cell)?"Glare, from wall to wall. Nothing in it.":"An iron door. Light comes round the edges of it, too much light.");return true;}
            if(closed(p)<CLOSE_EYES){
                speak(l,d,cell,cell==1?"Not with your eyes open. Shut them.":"Shut your eyes first.");
                if(!own.getBoolean("Hinted")){own.putBoolean("Hinted",true);save(p,own);p.sendSystemMessage(Component.literal("Crouch by the door to shut your eyes.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));}
                return true;
            }
            release(l,d,p);return true;
        }
        if(KeptBlock.is(st,KeptBlock.Kind.CRATE)){
            sound(l,b.offset(rel),KeptRegistry.TINS.get(),.6F,1.1F);own.putBoolean("Crate",true);save(p,own);
            cue(p,"Tins. Dozens of them, packed in straw, with no labels. There is nothing here to open them with, and nothing in the machine would do it.");
            if(d!=null&&d.stage==AT_TABLE&&d.sent){next(d,BROKEN);}
            return true;
        }
        if(KeptBlock.is(st,KeptBlock.Kind.CHAIR)){cue(p,"An empty chair, turned toward you.");return true;}
        if(KeptBlock.is(st,KeptBlock.Kind.TABLE)){cue(p,"A place laid: a plate, a knife, a fork and a glass. Nothing on any of it. Dust in the glass.");return true;}
        if(KeptBlock.is(st,KeptBlock.Kind.CABINET)){cue(p,st.getValue(KeptBlock.STAGE)==1?"A cabinet sealed in rime. The small lamps on it are still lit under the ice.":"Warm. The reels behind the glass turn a little, stop, and turn.");return true;}
        if(KeptBlock.is(st,KeptBlock.Kind.SCREEN)){
            if(st.getValue(KeptBlock.STAGE)==1){int deaths=p.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));machine(p,KeptTexts.deaths(deaths));}
            else cue(p,"A dark screen. Your face in it, small and curved.");
            return true;
        }
        if(KeptBlock.is(st,KeptBlock.Kind.WIRE)){cue(p,"Wire, packed too thick to push through. It's warm.");return true;}
        if(KeptBlock.is(st,KeptBlock.Kind.ICICLE)){cue(p,"Ice. It doesn't drip.");return true;}
        if(KeptBlock.is(st,KeptBlock.Kind.TRAY)){if(!own.getBoolean("Ready"))cue(p,"The tray is empty.");return !own.getBoolean("Ready");}
        if(KeptBlock.is(st,KeptBlock.Kind.CABLE)){cue(p,"Cable, as thick as an arm, running on into the dark.");return true;}
        return false;
    }

    // ------------------------------------------------------------------------------------------------ dealing and the reset
    /** The machine is there on every visit, before its story is had and after. */
    public static boolean dealable(LabyrinthData d,UUID p){return true;}
    /** Nobody in the machine: it puts them back as they were. */
    static void reset(ServerLevel l,Scene d){clearActors(l,d.base);KeptSite.reset(l,d.base);}
    /** For tests: the machine set back as an empty machine would be. */
    public static void resetNow(ServerLevel l,BlockPos b){var d=scene(b);if(d!=null){reset(l,d);SCENES.remove(b.asLong());}}
}
