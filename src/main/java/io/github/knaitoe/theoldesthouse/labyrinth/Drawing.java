package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.PolaroidFlashPayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.407, The Drawing (docs/THE_DRAWING_0_4_407.md): a village square on a bright summer morning. Everyone comes. A man with a list reads
 * the households, and the head of each walks up and draws a folded slip from a black box on a stool. At the end of the list he reads each
 * reader's own name, as a new household, and they draw too, or he draws for them. When everyone is told to look, every reader's slip is
 * blank. It is one household; its members draw again; one of them draws the slip with the mark on it and holds it out, and nobody takes
 * it. The crowd turns and steps back from them. A child presses a smooth stone into each reader's hand. Each reader throws it, drops it,
 * walks away with it, or stands; the day cuts to white, and each is set at the head of the lane with the square empty.
 *
 * <p>One morning for everyone in the square, on one clock; each reader's slip, stone and choice their own. The year is the most anyone
 * there has stood in the square, plus one: each year one villager fewer, a house shuttered, the box shabbier, and the one marked is the
 * one who came late, in the place where the woman with the jug stood the first year. A real player is never the one marked. Nothing
 * here says which choice was right, and every choice is told in the account.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Drawing {
    private Drawing(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.THE_DRAWING;
    /** The villagers, on stranger phases one hundred and twenty-three to one hundred and thirty. */
    public static final int ALBRIGHT=123,RUDD=124,NELL=125,DALE=126,BESS=127,PIKE=128,DUNN=129,RICKY=130;
    static final int COUNT=8;
    static final String[] NAMES={"Mr. Albright","Mr. Rudd","Nell Garrow","Dale Garrow","Bess Garrow","Old Mr. Pike","Mrs. Dunn","Ricky"};
    static final String[] SAYS={"Mr. Albright","Mr. Rudd","Nell","Dale","Bess","Mr. Pike","Mrs. Dunn","Ricky"};
    static final String[] SKINS={"albright","rudd","nell","dale","bess","pike","dunn","ricky"};
    static final String[] HOUSEHOLD={"Albright","Rudd","Garrow","Garrow","Garrow","Pike","Dunn","Dunn"};
    public static boolean cast(int phase){return phase>=ALBRIGHT&&phase<=RICKY;}
    public static String skin(int phase){return "drawing_"+SKINS[phase-ALBRIGHT];}
    /** Bess and Ricky are children. */
    public static boolean child(int phase){return phase==BESS||phase==RICKY;}
    static int idx(int phase){return phase-ALBRIGHT;}
    /** What a reader did with the stone. */
    public static final String THREW="threw",DROPPED="dropped",WALKED="walked",STOOD="stood";
    /** The morning's stages. */
    public static final int MORNING=0,CALL=1,OPENING=2,HOUSEHOLD_DRAW=3,STONES=4,CHOICE=5,OVER=6;
    /** Ticks: the morning; between households; how long the list waits for a reader to come up; the opening; between the household's
     * draws; the stones; how long the crowd waits before it closes in; how long after the late arrival the call begins; an empty
     * square's reset. */
    public static final int MORNING_LENGTH=1200,CALL_EVERY=100,CALL_WAIT=300,OPENING_LENGTH=220,HOUSE_EVERY=80,STONES_LENGTH=160,STAND_LIMIT=600,LATE=900,EMPTY_RESET=200;
    /** The late arrival in the morning, who pours the lemonade and is the one marked: by year, then Mr. Pike every year after. */
    static final int[] MARKED={idx(NELL),idx(DUNN),idx(DALE),idx(RICKY),idx(BESS)};
    /** Poses: reading the list, holding the box steady, holding a slip out, a stone in hand. */
    public static final int READ=6,STEADY=7,HOLD_OUT=8,HOLDING=9;
    public static final String TALK="the_drawing";
    static final String TAKEN="DrawingTaken0407";

    /** Who is marked in a given year (one-based): Nell, Mrs. Dunn, Dale, Ricky, Bess, then Mr. Pike. */
    public static int markedIn(int year){return year>=1&&year<=MARKED.length?MARKED[year-1]:idx(PIKE);}
    /** The villagers still there in a given year: one fewer each year, the marked of the years before gone. */
    public static List<Integer> present(int year){var out=new ArrayList<Integer>();for(int i=0;i<COUNT;i++)out.add(i);for(int y=1;y<year&&y<=MARKED.length;y++)out.remove(Integer.valueOf(MARKED[y-1]));return out;}
    /** The households read in a year, each with whoever draws for it. */
    static List<int[]> households(int year){
        var here=present(year);var out=new ArrayList<int[]>();
        for(var house:new int[][]{{idx(ALBRIGHT)},{idx(DUNN),idx(RICKY)},{idx(DALE),idx(NELL),idx(BESS)},{idx(PIKE)},{idx(RUDD)}})
            for(int who:house)if(here.contains(who)){out.add(new int[]{who});break;}
        return out;
    }
    /** The marked household's members there that year, the marked one last. */
    static List<Integer> members(int year){
        int m=markedIn(year);var out=new ArrayList<Integer>();for(int i:present(year))if(i!=m&&HOUSEHOLD[i].equals(HOUSEHOLD[m]))out.add(i);out.add(m);return out;
    }

    // ------------------------------------------------------------------------------------------------ places in the square (local)
    static final Vec3[] SPOT={new Vec3(.5,0,-36.2),new Vec3(1.9,0,-35.1),new Vec3(3.5,0,-29.5),new Vec3(-1.5,0,-29.0),new Vec3(-.4,0,-28.4),new Vec3(-4.5,0,-30.5),new Vec3(-3.2,0,-28.2),new Vec3(5.2,0,-28.6)};
    /** Where each comes out of in the morning: the post office, the bank, the houses, the grocery. */
    static final Vec3[] FROM={new Vec3(-12.5,0,-20.5),new Vec3(-12.5,0,-21.5),new Vec3(12.5,0,-19.5),new Vec3(13.5,0,-46.5),new Vec3(13.5,0,-47.5),new Vec3(-12.5,0,-33.5),new Vec3(-12.5,0,-46.5),new Vec3(-12.5,0,-47.5)};
    static final int[] ARRIVES={20,40,LATE,420,440,160,300,0};
    static final Vec3 BOX_FRONT=new Vec3(.5,0,-32.4),BY_WALL=new Vec3(13.2,0,-34.5),GATHER=new Vec3(2.5,0,-13.5),PILE=new Vec3(13.2,0,-31.5);
    /** Around the one left by the wall, the others step back to these. */
    static final Vec3[] RING={new Vec3(5.5,0,-29.5),new Vec3(4.5,0,-32.5),new Vec3(4.2,0,-35.5),new Vec3(5.0,0,-38.5),new Vec3(6.5,0,-41.0),new Vec3(7.5,0,-27.5),new Vec3(3.5,0,-30.8),new Vec3(3.6,0,-37.5)};

    static final class Day {
        final BlockPos base;int stage=MORNING,since,empty,year=1;long tick,id;
        final UUID[] cast=new UUID[COUNT];
        boolean fresh=true,started,lateIn,poured,markedOut,crowdBack,stonesUp;
        int pile,ferry;
        final List<Integer> here=new ArrayList<>();
        /** The list: villager calls (index) then readers (uuid), in order; where it is, and since when. */
        final List<Object> calls=new ArrayList<>();int call=-1,callAt;boolean callDone;
        final List<UUID> order=new ArrayList<>();
        final Set<UUID> drew=new HashSet<>(),cut=new HashSet<>();
        List<Integer> household=List.of();int draw=-1;
        final Map<Integer,List<Vec3>> routes=new HashMap<>();final Map<Integer,Integer> leg=new HashMap<>(),dwell=new HashMap<>(),paused=new HashMap<>(),held=new HashMap<>();
        final Map<UUID,Long> said=new HashMap<>();
        Day(BlockPos b){base=b;}
        int marked(){return markedIn(year);}
    }
    private static final Map<Long,Day> DAYS=new HashMap<>();
    /** Readers whose day has been cut: the square is empty for them. */
    private static final Set<UUID> CUT_OUT=new HashSet<>();
    /** Readers being set at the head of the lane once the white has come up. */
    private static final Map<UUID,Integer> SETTING=new HashMap<>();
    public static void clearAll(){DAYS.clear();CUT_OUT.clear();SETTING.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}
    /** Whether a reader's day in the square is over: the villagers are no longer there for them. */
    public static boolean cutOut(UUID p){return CUT_OUT.contains(p);}

    // ------------------------------------------------------------------------------------------------ for tests
    @Nullable static Day day(BlockPos b){return DAYS.get(b.asLong());}
    public static int stage(BlockPos b){var d=day(b);return d==null?-1:d.stage;}
    public static int year(BlockPos b){var d=day(b);return d==null?0:d.year;}
    @Nullable public static LiteraryActor actor(ServerLevel l,BlockPos b,int phase){var d=day(b);return d==null?null:actor(l,d.cast[idx(phase)]);}
    public static void skip(BlockPos b,int since){var d=day(b);if(d!=null)d.since=Math.max(d.since,since);}
    /** For tests: whether a reader is the one being read just now. */
    public static boolean calling(BlockPos b,UUID p){var d=day(b);return d!=null&&d.stage==CALL&&d.call>=0&&d.call<d.calls.size()&&p.equals(d.calls.get(d.call));}

    // ------------------------------------------------------------------------------------------------ records and speech
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void refresh(CompoundTag held,CompoundTag fresh){for(var key:List.copyOf(held.getAllKeys()))held.remove(key);held.merge(fresh);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void sound(ServerLevel l,BlockPos at,SoundEvent e,float volume,float pitch){l.playSound(null,at,e,SoundSource.NEUTRAL,volume,pitch);}
    /** Someone in the square says something: over their head, and in the action bar for readers near enough to hear it. */
    static void speak(ServerLevel l,Day d,int who,String line){
        var a=actor(l,d.cast[who]);if(a==null)return;a.say(line);a.speaks(line);d.said.put(a.getUUID(),d.tick);
        for(var r:readers(l,d.base))if(!d.cut.contains(r.getUUID())&&r.distanceToSqr(a)<20*20)r.displayClientMessage(Component.literal(SAYS[who]+": “"+line+"”").withStyle(ChatFormatting.GRAY),true);
    }
    static void everyone(ServerLevel l,Day d,String line){for(var r:readers(l,d.base))if(!d.cut.contains(r.getUUID()))cue(r,line);}
    /** Marks a fact in the record of every reader in the square whose day is not over. */
    static void witnessed(ServerLevel l,Day d,java.util.function.Consumer<CompoundTag> fact){for(var r:readers(l,d.base)){if(d.cut.contains(r.getUUID()))continue;var own=own(r);fact.accept(own);save(r,own);}}

    // ------------------------------------------------------------------------------------------------ who is here
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }

    // ------------------------------------------------------------------------------------------------ the villagers
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    static Vec3 spot(Day d,int who){return who==d.marked()&&d.year>1?SPOT[idx(NELL)]:SPOT[who];}
    @Nullable static LiteraryActor spawn(ServerLevel l,Day d,int who,Vec3 at){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,ALBRIGHT+who);place(a,d.base,at,180);
        if(who==idx(ALBRIGHT)){a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.PAPER));}
        if(who==d.marked()&&d.stage==MORNING)a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(DrawingRegistry.JUG.get()));
        return l.addFreshEntity(a)?a:null;
    }
    static void place(LiteraryActor a,BlockPos b,Vec3 r,float yaw){a.moveTo(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.setDeltaMovement(Vec3.ZERO);}
    static AABB area(BlockPos b){return new AABB(b.offset(DrawingSite.WEST,-4,DrawingSite.NORTH)).minmax(new AABB(b.offset(DrawingSite.EAST,16,1)));}
    static void clearActors(ServerLevel l,BlockPos b){for(var a:l.getEntitiesOfClass(LiteraryActor.class,area(b),a->a.role()==LiteraryActor.STRANGER&&cast(a.phase())))a.discard();}
    static boolean loaded(ServerLevel l,Day d){var at=d.base.offset(DrawingSite.BOX);return l.isLoaded(at)&&l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at));}
    /**
     * Everyone who has come into the square is there; nobody comes back once the day is over. 0.4.560 (the standard cast checks):
     * nobody comes into being where a reader can see. A villager comes out of the end of their lane when nobody is looking at it,
     * and walks in from there; one who belongs in the square already is put in their place when nobody is looking at that.
     */
    static void ensureCast(ServerLevel l,Day d){
        if(d.stage==OVER||!loaded(l,d))return;
        List<ServerPlayer> watching=null;
        for(int who:d.here){
            if(!arrived(d,who)||actor(l,d.cast[who])!=null)continue;
            boolean early=d.stage==MORNING&&d.since<arrival(d,who)+200;var at=early?FROM[who]:spot(d,who);
            if(watching==null)watching=readers(l,d.base);
            if(seen(watching,world(d.base,at)))continue;
            var a=spawn(l,d,who,at);if(a==null)continue;d.cast[who]=a.getUUID();
            if(early&&!busy(d,who))send(d,who,0,spot(d,who));
        }
    }
    /** Whether any of these readers can see a person standing at this point. */
    static boolean seen(List<ServerPlayer> readers,Vec3 at){
        for(var r:readers)if(io.github.knaitoe.theoldesthouse.house.HouseWatchers.sees(r,at.add(0,1.6,0))||io.github.knaitoe.theoldesthouse.house.HouseWatchers.sees(r,at.add(0,.3,0)))return true;
        return false;
    }
    static int arrival(Day d,int who){return who==d.marked()?LATE:ARRIVES[who];}
    static boolean arrived(Day d,int who){return d.stage>MORNING||d.since>=arrival(d,who);}
    static void send(Day d,int who,int wait,Vec3... points){d.routes.put(who,new ArrayList<>(List.of(points)));d.leg.put(who,0);d.dwell.put(who,wait);d.paused.remove(who);}
    static boolean busy(Day d,int who){return d.routes.containsKey(who);}
    static Vec3 pause(int ticks){return new Vec3(ticks,-99,0);}
    static void walk(ServerLevel l,Day d){
        for(var who:List.copyOf(d.routes.keySet())){
            var a=actor(l,d.cast[who]);if(a==null){d.routes.remove(who);continue;}
            var route=d.routes.get(who);int i=d.leg.get(who);
            if(i<route.size()&&route.get(i).y<-50){int left=d.paused.getOrDefault(who,(int)route.get(i).x);if(left>0){d.paused.put(who,left-1);continue;}d.paused.remove(who);d.leg.put(who,i+1);continue;}
            // 0.4.561 (the standard cast checks: the late villager answered a reader over his shoulder and walked on): spoken to on
            // the way, they stop to answer, for up to three seconds a leg, then go on.
            if(i<route.size()&&a.listening()&&d.held.merge(who,1,Integer::sum)<=60)continue;
            if(i<route.size()){var target=world(d.base,route.get(i));if(SceneFootpaths.walk(a,target,.12)||a.position().distanceToSqr(target)<.09){d.leg.put(who,i+1);d.held.remove(who);}continue;}
            int wait=d.dwell.getOrDefault(who,0);if(wait>0){d.dwell.put(who,wait-1);continue;}
            d.routes.remove(who);d.leg.remove(who);d.dwell.remove(who);d.held.remove(who);
        }
    }
    /** Heads turn: to the box while the list is read, to the one by the wall after, otherwise to whoever is nearest. */
    static void look(ServerLevel l,Day d){
        var here=readers(l,d.base);var m=actor(l,d.cast[d.marked()]);
        for(int who:d.here){var a=actor(l,d.cast[who]);if(a==null||busy(d,who))continue;
            if(d.stage>=STONES&&m!=null&&who!=d.marked()){SceneFootpaths.face(a,m.getEyePosition(),90);continue;}
            if(d.stage>=CALL&&d.stage<STONES&&who!=idx(ALBRIGHT)&&who!=idx(RUDD)){SceneFootpaths.face(a,world(d.base,new Vec3(.5,1.5,-33.5)),70);continue;}
            ServerPlayer near=null;double best=64;for(var r:here){if(d.cut.contains(r.getUUID()))continue;double dd=r.distanceToSqr(a);if(dd<best){best=dd;near=r;}}
            if(near!=null)SceneFootpaths.face(a,near.getEyePosition(),70);
            var t=d.said.get(a.getUUID());if(t!=null&&!a.saying()){a.setCustomNameVisible(false);a.silent();d.said.remove(a.getUUID());}
        }
    }

    // ------------------------------------------------------------------------------------------------ arriving
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;var d=DAYS.computeIfAbsent(b.asLong(),k->{var n=new Day(b);n.id=p.serverLevel().getGameTime();return n;});
        if(own.getLong("DayId")!=d.id){
            for(var key:List.of("Lemonade","PouredBy","Talked","Met","Called","Drew","Opened","Marked","MarkedName","MarkedCount","InPlaceOf","InPlaceTalked","Stone","StoneFrom","Choice","Late","Ready","Taken_Ending","Read_Ending","Yield_Ending","Whole"))own.remove(key);
            own.putLong("DayId",d.id);
        }
        CUT_OUT.remove(p.getUUID());d.cut.remove(p.getUUID());
        if(!d.order.contains(p.getUUID()))d.order.add(p.getUUID());
        if(d.stage>=OPENING&&d.stage<OVER)own.putBoolean("Late",true);
        cue(p,d.stage>=OVER?"The square is empty. The box is on its stool.":d.stage>=OPENING?"A village square in the sun, full of people, all of them looking at one place.":"A lane between hedges, and at the end of it a village square in the sun. People are coming into it from every side.");
    }

    // ------------------------------------------------------------------------------------------------ the morning's clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();
        if(!SETTING.isEmpty())for(var id:List.copyOf(SETTING.keySet())){int left=SETTING.get(id)-1;if(left>0){SETTING.put(id,left);continue;}SETTING.remove(id);var p=server.getPlayerList().getPlayer(id);if(p!=null&&LiteraryVignettes.current(p)==PLACE){var b=LiteraryVignettes.base(p,PLACE);if(b!=null){var at=world(b,DrawingSite.LANE_HEAD);p.teleportTo(p.serverLevel(),at.x,at.y,at.z,180,0);}}}
        if(DAYS.isEmpty())return;var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var d:List.copyOf(DAYS.values())){
            d.tick++;var here=readers(l,d.base);
            if(here.isEmpty()){if(++d.empty>=EMPTY_RESET&&loaded(l,d)){reset(l,d);DAYS.remove(d.base.asLong());}continue;}
            d.empty=0;
            if(d.fresh){if(!loaded(l,d))continue;d.fresh=false;start(l,d,here);}
            ensureCast(l,d);
            walk(l,d);
            if(d.tick%2==0)look(l,d);
            stage(l,d,here);
            d.since++;
        }
        if(server.getTickCount()%20==0)slips(server);
    }
    /** The day begins: its year is the most anyone there has stood in the square, plus one. */
    static void start(ServerLevel l,Day d,List<ServerPlayer> here){
        int most=0;for(var r:here)most=Math.max(most,own(r).getInt("Years"));d.year=most+1;d.started=true;
        clearActors(l,d.base);d.here.clear();d.here.addAll(present(d.year));
        DrawingSite.boxAndStool(l,d.base,Math.min(3,d.year-1));
        DrawingSite.shutters(l,d.base,Math.min(DrawingSite.HOUSES,d.year-1));
        d.pile=ferrier(d)>=0?0:3;DrawingSite.piles(l,d.base,d.pile);
        for(var item:l.getEntitiesOfClass(ItemEntity.class,area(d.base),i->i.getItem().is(DrawingRegistry.STONE.get())))item.discard();
    }
    /** A new stage: its clock reads 0 on the next tick it runs, so each stage's first tick is never skipped. */
    static void next(ServerLevel l,Day d,int stage){d.stage=stage;d.since=-1;}

    static void stage(ServerLevel l,Day d,List<ServerPlayer> here){
        int t=d.since;
        switch(d.stage){
            case MORNING->morning(l,d,here,t);
            case CALL->call(l,d,here,t);
            case OPENING->opening(l,d,t);
            case HOUSEHOLD_DRAW->household(l,d,t);
            case STONES->stones(l,d,here,t);
            case CHOICE->choice(l,d,here,t);
            default->{}
        }
    }

    /** The morning: they come in in ones and twos, the children gather stones, and last of all someone comes in late with a jug. */
    static void morning(ServerLevel l,Day d,List<ServerPlayer> here,int t){
        int ferrier=ferrier(d);
        for(int who:d.here)if(who!=d.marked()&&who!=ferrier&&t==ARRIVES[who])send(d,who,0,FROM[who],spot(d,who));
        // A child ferries stones from the lane's verges to the wall.
        int child=ferrier;
        if(child>=0&&actor(l,d.cast[child])==null)child=-1;
        if(child>=0&&!busy(d,child)&&t<LATE-100&&d.pile<3){if(d.ferry++>0){d.pile++;DrawingSite.piles(l,d.base,d.pile);sound(l,d.base.offset(DrawingSite.PILE_SOUTH),DrawingRegistry.STONES.get(),.4F,1.2F);}if(d.pile<3)send(d,child,10,GATHER,pause(30),PILE);}
        if(child>=0&&!busy(d,child)&&(d.pile>=3||t>=LATE-100)&&actor(l,d.cast[child])!=null&&actor(l,d.cast[child]).position().distanceToSqr(world(d.base,spot(d,child)))>1)send(d,child,0,spot(d,child));
        talk(l,d,t,200,idx(DUNN),"They're saying rain by Thursday. I'll believe it when it's on the roof.");
        talk(l,d,t,360,idx(PIKE),"I was coming to this before the stool. They had the box on a crate then.");
        talk(l,d,t,520,idx(ALBRIGHT),"Morning. Morning. Nearly time.");
        talk(l,d,t,650,idx(DALE),"Bess. Stay where I can see you.");
        talk(l,d,t,780,idx(RUDD),"Somebody ought to give that box a coat of paint.");
        int m=d.marked();
        if(t==LATE&&d.here.contains(m)){send(d,m,0,FROM[m],spot(d,m));d.lateIn=true;}
        if(t==LATE+40)speak(l,d,m,switch(d.year){case 1->"Would you believe I nearly forgot what day it was? I had the kettle on.";case 2->"Don't start without me. I'm here, I'm here.";case 3->"I couldn't find my other boot. Of all the mornings.";case 4->"Gran said I had to bring the jug.";case 5->"I brought it. Dad said to.";default->"Every year, and I'm still the last one.";});
        if(d.lateIn&&!d.poured&&!busy(d,m)){d.poured=true;pour(l,d,here);}
        if(t>=MORNING_LENGTH&&(d.poured||t>=MORNING_LENGTH+400)){var a=actor(l,d.cast[m]);if(a!=null)a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);d.poured=true;next(l,d,CALL);}
    }
    /** The child who gathers stones from the lane and piles them by the wall: Ricky, or Bess once he is gone; nobody once both are. */
    static int ferrier(Day d){return d.here.contains(idx(RICKY))&&d.marked()!=idx(RICKY)?idx(RICKY):d.here.contains(idx(BESS))&&d.marked()!=idx(BESS)?idx(BESS):-1;}
    static void talk(ServerLevel l,Day d,int t,int at,int who,String line){if(t==at&&d.here.contains(who)&&who!=d.marked())speak(l,d,who,line);}
    /** A glass of lemonade from the jug for each reader there. */
    static void pour(ServerLevel l,Day d,List<ServerPlayer> here){
        int m=d.marked();var a=actor(l,d.cast[m]);
        for(var r:here){if(d.cut.contains(r.getUUID()))continue;var own=own(r);if(own.getBoolean("Lemonade"))continue;own.putBoolean("Lemonade",true);own.putString("PouredBy",NAMES[m]);save(r,own);
            LiteraryVignettes.give(r,new ItemStack(DrawingRegistry.LEMONADE.get()));
            if(a!=null)sound(l,a.blockPosition(),DrawingRegistry.POUR.get(),.6F,1F);
            cue(r,SAYS[m]+" pours you a glass of lemonade from the jug, laughing, and goes to stand with the others.");}
    }

    /** The list: each household in turn, then the new households, one name each. */
    static void call(ServerLevel l,Day d,List<ServerPlayer> here,int t){
        if(t==0){d.calls.clear();for(var h:households(d.year))d.calls.add(h[0]);for(var id:d.order)if(online(l,d,id))d.calls.add(id);d.call=-1;
            var a=actor(l,d.cast[idx(ALBRIGHT)]);if(a!=null){a.gesture(READ);place(a,d.base,SPOT[idx(ALBRIGHT)],0);}
            var r=actor(l,d.cast[idx(RUDD)]);if(r!=null){r.gesture(STEADY);place(r,d.base,SPOT[idx(RUDD)],20);}
            speak(l,d,idx(ALBRIGHT),"Well. Let's get started, so everybody can get back to work.");d.callAt=t;d.callDone=true;return;}
        // Late readers are read at the end of the list.
        for(var r:here)if(!d.cut.contains(r.getUUID())&&!d.calls.contains(r.getUUID())){d.calls.add(r.getUUID());if(!d.order.contains(r.getUUID()))d.order.add(r.getUUID());}
        if(d.callDone&&t-d.callAt>=40){
            d.call++;d.callAt=t;d.callDone=false;
            if(d.call>=d.calls.size()){speak(l,d,idx(ALBRIGHT),"That's everybody? Everyone has one? All right.");next(l,d,OPENING);return;}
            var who=d.calls.get(d.call);
            if(who instanceof Integer v){speak(l,d,idx(ALBRIGHT),HOUSEHOLD[v]+".");if(v!=idx(ALBRIGHT)&&v!=idx(RUDD))send(d,v,0,BOX_FRONT,pause(20),spot(d,v));}
            else{var p=l.getServer().getPlayerList().getPlayer((UUID)who);
                if(p==null||d.cut.contains(p.getUUID())||readers(l,d.base).stream().noneMatch(r->r==p)){d.callDone=true;return;}
                speak(l,d,idx(ALBRIGHT),p.getGameProfile().getName()+". A new household this year.");
                var own=own(p);own.putBoolean("Called",true);save(p,own);cue(p,"He has read your name. Go up and put your hand in the box.");}
            return;
        }
        if(d.callDone||d.call<0||d.call>=d.calls.size())return;
        int k=t-d.callAt;var who=d.calls.get(d.call);
        if(who instanceof Integer v){
            if(k==30)sound(l,d.base.offset(DrawingSite.BOX),DrawingRegistry.SLIP_SOUND.get(),.6F,1F);
            if(k>=CALL_EVERY){if(v!=idx(ALBRIGHT)&&v!=idx(RUDD)){var a=actor(l,d.cast[v]);if(a!=null&&!busy(d,v))place(a,d.base,spot(d,v),180);}d.callDone=true;}
            return;
        }
        var id=(UUID)who;
        if(d.drew.contains(id)){d.callDone=true;return;}
        var p=l.getServer().getPlayerList().getPlayer(id);
        if(p==null||readers(l,d.base).stream().noneMatch(r->r==p)){d.callDone=true;return;}
        if(k>=CALL_WAIT){speak(l,d,idx(ALBRIGHT),"I'll draw for "+p.getGameProfile().getName()+".");drawFor(l,d,p,"for");}
    }
    static boolean online(ServerLevel l,Day d,UUID id){var p=l.getServer().getPlayerList().getPlayer(id);return p!=null&&readers(l,d.base).contains(p)&&!d.cut.contains(id);}
    /** A slip for a reader: drawn by their own hand, or for them. */
    static void drawFor(ServerLevel l,Day d,ServerPlayer p,String how){
        if(!d.drew.add(p.getUUID()))return;
        var slip=new ItemStack(DrawingRegistry.SLIP.get());slip.set(DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(slipTag(p)));
        LiteraryVignettes.give(p,slip);sound(l,d.base.offset(DrawingSite.BOX),DrawingRegistry.SLIP_SOUND.get(),.8F,1F);
        var own=own(p);own.putString("Drew",how);save(p,own);
        cue(p,how.equals("self")?"You put your hand in the box and take a slip, folded twice. Don't open it yet.":"Someone puts a folded slip in your hand. Don't open it yet.");
        d.callDone=true;
    }
    static CompoundTag slipTag(ServerPlayer p){var t=new CompoundTag();t.putBoolean(TAKEN,true);t.putUUID("Reader",p.getUUID());return t;}

    /** Everyone may look: every reader's slip is blank, and a murmur goes round; it's one household. */
    static void opening(ServerLevel l,Day d,int t){
        int m=d.marked();String house=HOUSEHOLD[m];var members=members(d.year);int head=members.get(0);
        if(t==0){speak(l,d,idx(ALBRIGHT),"All right. You can all look now.");everyone(l,d,"You can open your slip now.");}
        if(t==25)sound(l,d.base.offset(DrawingSite.BOX),DrawingRegistry.MURMUR.get(),1F,1F);
        if(t==70){int sayer=firstOf(d,m,idx(DUNN),idx(PIKE),idx(RUDD),idx(ALBRIGHT));if(sayer>=0)speak(l,d,sayer,members.size()>1?"It's the "+house+"s.":"It's "+(house.equals("Pike")?"Pike":SAYS[m])+".");}
        if(t==100){var a=actor(l,d.cast[head]);if(a!=null)a.gesture(HOLD_OUT);}
        if(t==140)speak(l,d,m,switch(d.year){case 1->"He was rushed. You all saw him rushed, he never had the time to choose.";case 2->"Somebody look at that list again. Somebody check it.";case 3->"Give it a minute. Just give it one minute.";case 4->"I didn't even want to come.";case 5->"Dad?";default->"Every year. There's always been one.";});
        if(t>=OPENING_LENGTH){var a=actor(l,d.cast[head]);if(a!=null)a.gesture(0);next(l,d,HOUSEHOLD_DRAW);d.household=members;d.draw=-1;}
    }
    static int firstOf(Day d,int not,int... who){for(int w:who)if(d.here.contains(w)&&w!=not&&!HOUSEHOLD[w].equals(HOUSEHOLD[not]))return w;return -1;}

    /** The household draws again, one at a time; the last of them draws the marked slip and holds it out. */
    static void household(ServerLevel l,Day d,int t){
        int m=d.marked();
        if(t==0){speak(l,d,idx(ALBRIGHT),d.household.size()>1?"The "+HOUSEHOLD[m]+"s, then. One each.":"Just the one, then.");return;}
        int k=t-20;if(k<0)return;
        int i=k/HOUSE_EVERY,at=k%HOUSE_EVERY;
        if(i<d.household.size()){int who=d.household.get(i);
            if(at==0)send(d,who,0,BOX_FRONT,pause(25));
            if(at==45)sound(l,d.base.offset(DrawingSite.BOX),DrawingRegistry.SLIP_SOUND.get(),.7F,1F);
            if(at==HOUSE_EVERY-1&&who!=m)send(d,who,0,spot(d,who));
            return;}
        if(at==0&&i==d.household.size()){
            var a=actor(l,d.cast[m]);if(a!=null)a.gesture(HOLD_OUT);
            var name=NAMES[m];int count=d.household.size();
            witnessed(l,d,own->{own.putString("Marked",HOUSEHOLD[m]);own.putString("MarkedName",name);own.putInt("MarkedCount",count);
                if(contains(own.getList("SawLast",8),name)){own.putString("InPlaceOf",name);if(contains(own.getList("MetLast",8),name))own.putBoolean("InPlaceTalked",true);}});
            everyone(l,d,name+" holds out a slip with a black mark on it. Nobody takes it.");
        }
        if(i==d.household.size()&&at==60)next(l,d,STONES);
    }
    static boolean contains(ListTag list,String s){for(int i=0;i<list.size();i++)if(list.getString(i).equals(s))return true;return false;}

    /** The crowd steps back from the one marked; a child puts a stone in each reader's hand; the villagers pick up stones. */
    static void stones(ServerLevel l,Day d,List<ServerPlayer> here,int t){
        int m=d.marked();
        if(t==0){var a=actor(l,d.cast[m]);if(a!=null){a.gesture(0);send(d,m,0,BY_WALL);}
            int ring=0;for(int who:d.here)if(who!=m){if(who==idx(RUDD)||who==idx(ALBRIGHT)){var v=actor(l,d.cast[who]);if(v!=null)v.gesture(0);}send(d,who,0,RING[ring++%RING.length]);}
            d.crowdBack=true;everyone(l,d,"The crowd turns and steps back. "+NAMES[m]+" is left standing by the wall.");}
        if(t==40)for(var r:here)give(l,d,r);
        if(t==70){for(int who:d.here)if(who!=m&&!child(ALBRIGHT+who)){var v=actor(l,d.cast[who]);if(v!=null){v.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(DrawingRegistry.STONE.get()));v.gesture(HOLDING);}}
            sound(l,d.base.offset(DrawingSite.PILE_SOUTH),DrawingRegistry.STONES.get(),1F,1F);d.stonesUp=true;}
        if(t>=STONES_LENGTH)next(l,d,CHOICE);
    }
    static void give(ServerLevel l,Day d,ServerPlayer r){
        if(d.cut.contains(r.getUUID()))return;var own=own(r);if(own.getBoolean("Stone"))return;
        int giver=ferrier(d);String from=giver>=0?"child":d.here.contains(idx(RUDD))?NAMES[idx(RUDD)]:NAMES[idx(ALBRIGHT)];
        own.putBoolean("Stone",true);own.putString("StoneFrom",from);save(r,own);LiteraryVignettes.give(r,new ItemStack(DrawingRegistry.STONE.get()));
        cue(r,from.equals("child")?"A child presses a stone into your hand. It is smooth, and warm from a pocket.":from+" puts a stone in your hand. It is smooth, and cold.");
    }

    /** Each reader for themselves; after a while the crowd closes in, and the day cuts to white for everyone still there. */
    static void choice(ServerLevel l,Day d,List<ServerPlayer> here,int t){
        if(t==STAND_LIMIT-160)speak(l,d,firstOf(d,d.marked(),idx(ALBRIGHT),idx(RUDD),idx(PIKE)),"Come on, now. Everyone.");
        if(t>=STAND_LIMIT){
            for(var r:List.copyOf(here))if(!d.cut.contains(r.getUUID())){var own=own(r);cut(l,d,r,own.getString("Choice").isEmpty()?STOOD:own.getString("Choice"));}
            over(l,d);
        }
    }
    static void over(ServerLevel l,Day d){next(l,d,OVER);clearActors(l,d.base);d.routes.clear();}

    // ------------------------------------------------------------------------------------------------ each reader
    /** Every five ticks, for each reader in the square. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag held){
        save(p,held);var own=own(p);var d=day(b);var l=p.serverLevel();
        if(d==null||d.cut.contains(p.getUUID())){refresh(held,own);return;}
        var r=rel(p.position(),b);
        if(d.stage>=STONES&&d.stage<OVER&&!own.getBoolean("Stone")){save(p,own);give(l,d,p);own=own(p);}
        // Walking back down the lane once the stones are out: with the stone in hand, or after dropping it.
        if(d.stage==CHOICE&&DrawingSite.inLane(r)){
            String choice=own.getString("Choice");
            if(choice.isEmpty()&&holdsStone(p)){cue(p,"The crowd stands aside to let you through. Nobody says anything. Nobody stops you.");cut(l,d,p,WALKED);own=own(p);}
            else if(choice.equals(DROPPED)){cut(l,d,p,DROPPED);own=own(p);}
        }
        refresh(held,own);
    }
    static boolean holdsStone(ServerPlayer p){for(var st:p.getInventory().items)if(st.is(DrawingRegistry.STONE.get()))return true;for(var st:p.getInventory().offhand)if(st.is(DrawingRegistry.STONE.get()))return true;return false;}
    /** A reader's day cut to white: their record finished for the year, set at the head of the lane, the square empty for them. */
    static void cut(ServerLevel l,Day d,ServerPlayer p,String choice){
        if(!d.cut.add(p.getUUID()))return;
        var own=own(p);
        if(own.getString("Choice").isEmpty())own.putString("Choice",choice);
        boolean whole=own.getBoolean("Called")&&!own.getString("Drew").isEmpty();
        if(whole){own.putInt("Years",own.getInt("Years")+1);own.putBoolean("Whole",true);}
        if(whole){own.put("MetLast",own.getList("Met",8).copy());var saw=new ListTag();for(int who:d.here)saw.add(StringTag.valueOf(NAMES[who]));own.put("SawLast",saw);}
        for(var st:p.getInventory().items)if(st.is(DrawingRegistry.SLIP.get()))st.setCount(0);for(var st:p.getInventory().offhand)if(st.is(DrawingRegistry.SLIP.get()))st.setCount(0);
        if(whole)LiteraryVignettes.ready(p,PLACE,own,outcome(own.getString("Choice")));
        else cue(p,"Your name was never read. The square is empty.");
        save(p,own);
        HousePackets.send(p,new PolaroidFlashPayload(80,3.6F));
        p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(DrawingRegistry.WHITE,SoundSource.MASTER,p.getX(),p.getY(),p.getZ(),1F,1F,p.getRandom().nextLong()));
        CUT_OUT.add(p.getUUID());SETTING.put(p.getUUID(),10);
        if(whole)account(p,d);
        String name=p.getGameProfile().getName();
        String seen=switch(own.getString("Choice")){case THREW->name+" throws.";case DROPPED->name+" lets the stone drop.";case WALKED->name+" walks back down the lane. The crowd lets them through.";default->null;};
        if(seen!=null)for(var r:readers(l,d.base))if(r!=p&&!d.cut.contains(r.getUUID())&&r.distanceToSqr(p)<32*32)cue(r,seen);
        boolean anyone=false;for(var r:readers(l,d.base))if(!d.cut.contains(r.getUUID()))anyone=true;
        if(!anyone&&d.stage>=CHOICE&&d.stage<OVER)over(l,d);
    }
    static String outcome(String choice){return switch(choice){case THREW->"threw_the_stone";case DROPPED->"dropped_the_stone";case WALKED->"walked_away_with_the_stone";default->"stood_and_held_the_stone";};}
    /** The account pinned on the board beside the notice, for this reader alone. */
    static void account(ServerPlayer p,Day d){var at=d.base.offset(DrawingSite.ACCOUNT);var st=p.serverLevel().getBlockState(at);if(DrawingBlock.is(st,DrawingBlock.Kind.ACCOUNT))p.connection.send(new ClientboundBlockUpdatePacket(at,st.setValue(DrawingBlock.STAGE,1)));}

    // ------------------------------------------------------------------------------------------------ talk
    /** Right-clicking someone in the square. They are friendly and ordinary, and nobody says what the drawing is for. */
    public static void talk(ServerPlayer p,LiteraryActor actor){
        var b=LiteraryVignettes.base(p,PLACE);var d=b==null?null:day(b);if(d==null||!cast(actor.phase()))return;int who=idx(actor.phase());
        if(d.cut.contains(p.getUUID())||d.stage>=OVER)return;
        if(d.stage>=HOUSEHOLD_DRAW){cue(p,who==d.marked()?NAMES[who]+" doesn't look at you.":NAMES[who]+" doesn't look away from "+(d.stage>=STONES?"the wall.":"the box."));return;}
        var own=own(p);var met=own.getList("Met",8);if(!contains(met,NAMES[who])){met.add(StringTag.valueOf(NAMES[who]));own.put("Met",met);own.putInt("Talked",own.getInt("Talked")+1);save(p,own);}
        String line=switch(actor.phase()){
            case ALBRIGHT->d.stage==MORNING?"Morning. Good turnout, considering. We'll start at ten, same as always.":"Not long now. Stand with the others.";
            case RUDD->"Every year I mean to make a new box. Every year it's this one.";
            case NELL->"Don't look at me, I know. I'm all over the place this morning.";
            case DALE->"Wet spring. The low field's still soft. Bess, come and say hello.";
            case BESS->"Ricky's got more stones than me. He always has more.";
            case PIKE->"There's always been a drawing. Before the bank, before the road was paved. Always.";
            case DUNN->"We had an inch on Tuesday. Feed's gone up again. My son's got a new tractor, a red one, he won't stop about it.";
            default->"These are the good ones. The smooth ones.";
        };
        var name=SAYS[who];Talk.begin(p,TALK,name);Talk.line(p,name,line);actor.say(line);actor.speaks(line);d.said.put(actor.getUUID(),d.tick);
        Talk.ask(p,Talk.reply("Ask what the drawing is for.",q->{Talk.line(q,name,switch(actor.phase()){case PIKE->"For? It's the drawing.";case DUNN->"Oh, you know. Same as every year.";case RICKY,BESS->"You draw. Then you see.";case ALBRIGHT->"It's on the notice, if you want to read it.";default->"It's the drawing. You'll see.";});Talk.end(q);}),
            Talk.act("Leave it there.",Talk::close));
    }

    // ------------------------------------------------------------------------------------------------ the things in the square
    /** Clicks in the square: the box, the shut doors, the piles. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag held){
        save(p,held);boolean handled=clicked(p,b,rel,own(p));refresh(held,own(p));return handled;
    }
    static boolean clicked(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var d=day(b);var st=l.getBlockState(b.offset(rel));
        if(DrawingBlock.is(st,DrawingBlock.Kind.BOX)||DrawingBlock.is(st,DrawingBlock.Kind.STOOL)){
            if(d!=null&&d.stage==CALL&&!d.cut.contains(p.getUUID())){
                if(d.drew.contains(p.getUUID())){cue(p,"You have your slip.");return true;}
                if(calling(b,p.getUUID())){drawFor(l,d,p,"self");return true;}
                cue(p,"Wait until your name is read.");return true;}
            int stage=DrawingBlock.is(st,DrawingBlock.Kind.BOX)?st.getValue(DrawingBlock.STAGE):Math.min(3,d==null?0:d.year-1);
            cue(p,switch(stage){case 0->"A black box, the paint chipped. One corner is split and bound round with string.";case 1->"A black box, the paint chipped. The split corner is bound with wire now.";case 2->"A black box, more grey than black. The split corner is tied with a bootlace.";default->"A box that was black once. The split has opened along one side, and a bootlace holds it.";});
            return true;
        }
        if(st.getBlock() instanceof DoorBlock&&!rel.equals(DrawingSite.HALL_DOOR)&&!rel.equals(DrawingSite.HALL_DOOR.above())){
            boolean office=rel.distManhattan(DrawingSite.POST_DOOR)<=1||rel.distManhattan(DrawingSite.BANK_DOOR)<=1;
            cue(p,office?"Locked. Closed for the morning.":"Locked. Whoever lives here is out in the square.");return true;}
        if(DrawingBlock.is(st,DrawingBlock.Kind.STONES)){cue(p,"Smooth stones from the lane, piled against the wall.");return true;}
        if(DrawingBlock.is(st,DrawingBlock.Kind.SHUTTER)&&st.getValue(DrawingBlock.STAGE)==1){cue(p,"The shutters are nailed across.");return true;}
        return false;
    }

    // ------------------------------------------------------------------------------------------------ the slip and the stone in hand
    @SubscribeEvent public static void use(PlayerInteractEvent.RightClickItem e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var s=p.getItemInHand(e.getHand());
        boolean slip=s.is(DrawingRegistry.SLIP.get()),stone=s.is(DrawingRegistry.STONE.get());if(!slip&&!stone)return;
        var b=LiteraryVignettes.current(p)==PLACE?LiteraryVignettes.base(p,PLACE):null;var d=b==null?null:day(b);
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(slip){
            if(d==null||d.stage<OPENING&&d.stage!=OVER){cue(p,"Not until everyone has drawn.");return;}
            if(s.has(DataComponents.CUSTOM_MODEL_DATA)){cue(p,"Blank.");return;}
            s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(1));sound(p.serverLevel(),p.blockPosition(),DrawingRegistry.SLIP_SOUND.get(),.6F,1.1F);
            var own=own(p);own.putBoolean("Opened",true);save(p,own);cue(p,"You unfold it. It's blank.");return;
        }
        if(d==null||d.stage!=CHOICE||d.cut.contains(p.getUUID())||!own(p).getString("Choice").isEmpty()){cue(p,"You turn the stone over in your hand. It is smooth, and warm.");return;}
        s.shrink(1);cut(p.serverLevel(),d,p,THREW);
    }
    @SubscribeEvent public static void toss(ItemTossEvent e){
        if(!(e.getPlayer() instanceof ServerPlayer p)||!e.getEntity().getItem().is(DrawingRegistry.STONE.get()))return;
        var b=LiteraryVignettes.current(p)==PLACE?LiteraryVignettes.base(p,PLACE):null;var d=b==null?null:day(b);
        if(d==null||d.stage!=CHOICE||d.cut.contains(p.getUUID()))return;
        var own=own(p);if(!own.getString("Choice").isEmpty())return;
        own.putString("Choice",DROPPED);save(p,own);e.getEntity().setNeverPickUp();
        cue(p,"You let the stone drop. It rolls a little way on the flagstones and stops.");
        for(var r:readers(p.serverLevel(),d.base))if(r!=p&&!d.cut.contains(r.getUUID())&&r.distanceToSqr(p)<32*32)cue(r,p.getGameProfile().getName()+" lets the stone drop.");
    }
    /** Slips never leave the square. */
    static void slips(net.minecraft.server.MinecraftServer server){
        for(var p:server.getPlayerList().getPlayers()){if(LiteraryVignettes.current(p)==PLACE)continue;
            for(var st:p.getInventory().items)if(st.is(DrawingRegistry.SLIP.get()))st.setCount(0);for(var st:p.getInventory().offhand)if(st.is(DrawingRegistry.SLIP.get()))st.setCount(0);}
    }

    // ------------------------------------------------------------------------------------------------ dealing and the reset
    /** The drawing is held on every visit, and each visit is a year. */
    public static boolean dealable(LabyrinthData d,UUID p){return true;}
    /** Nobody in the square: the next morning starts over, the square set back, the box on its stool. */
    static void reset(ServerLevel l,Day d){
        clearActors(l,d.base);for(var id:d.cut)CUT_OUT.remove(id);
        for(var item:l.getEntitiesOfClass(ItemEntity.class,area(d.base),i->i.getItem().is(DrawingRegistry.STONE.get())))item.discard();
    }
    /** For tests: the square set back as an empty square would be. */
    public static void resetNow(ServerLevel l,BlockPos b){var d=day(b);if(d!=null){reset(l,d);DAYS.remove(b.asLong());}}
}
