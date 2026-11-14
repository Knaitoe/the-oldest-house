package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.GrowingBlock.Kind;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.350, Growing Things (docs/GROWING_THINGS_0_4_350.md), from the owner's draft "The House Has a Yard": "A back fence opens onto a
 * family yard. Familiar chores come first. Bring in washing and help move the kitchen table when the window frame bows. The action moves
 * inside with Merry and Marjorie. Their father goes out to clear the back steps. By supper, the plants have lifted the kitchen floor enough
 * that dinner has to be carried upstairs. Merry asks whether to bring his plate. Let the player answer by what they carry."
 *
 * <p>The Barretts, years before the filming: Merry, Marjorie and their father John, on a Saturday their mother is out. The chores come
 * first: the washing off the line, and the kitchen table moved away from the window, which takes a pair of hands on each end. Then the
 * afternoon runs on its own clock while anyone is there. John goes out with a spade to clear the back step and does not come back in; his
 * spade is left standing in the shoots. The kitchen floor lifts. By supper it cannot be eaten on, so supper is carried upstairs to the
 * girls' room, and Merry asks whether to bring Dad's plate. Whatever reaches the table upstairs is the answer. A reader who goes back out
 * through the gate before supper is up has their own ending. One day for the party: one washing line, one table, one supper. Each reader's
 * account is built from their own record. Nothing here hurts.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class GrowingThings {
    private GrowingThings(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.GROWING_THINGS;
    public static final String TALK="growing_things";
    /** 0.4.555: how many readers one Saturday at the Barretts' holds, a pair of hands at each end of the kitchen table and two more; the fifth
     *  is given a Saturday of their own (SceneInstances). */
    public static final int PLACES=4;
    public static final String CARRIED="carried_his_plate_upstairs",LEFT="left_his_plate_downstairs",WENT="went_back_through_the_fence";
    /** The day: the chores; the afternoon; supper being carried up; over. */
    public static final int CHORES=0,AFTERNOON=1,SUPPER=2,OVER=3;
    /** Ticks: the most the chores wait for help before the family does them; how long a hand on the table holds; how often Merry takes a
     * piece of washing herself; when John goes out, the frame splits, John may be gone, John walks off if still watched; the floor's three
     * stages; the beat after supper is up; the longest supper waits to be carried; an empty yard's reset. */
    public static final int CHORE_LIMIT=1800,HOLD=200,MERRY_TAKES=260,JOHN_OUT=200,FRAME_SPLITS=400,JOHN_GONE=600,JOHN_WALKS=900,
        FLOOR_1=400,FLOOR_2=800,FLOOR_3=1200,BEAT=100,SUPPER_LIMIT=1800,EMPTY_RESET=200;
    /**
     * 0.4.571 (run 1601, the two-reader conduct check): how near a reader hears one of the family; ticks before Merry or John asks for a
     * hand again, of a reader who was not near enough to hear the first asking; ticks the lawn's shoots are left to be read before
     * Merry speaks over them.
     */
    public static final double HEARING=14;
    public static final int ASK_AGAIN=400,READ_FIRST=60;
    static final String WASHING_ASK="Can you help me get the washing in? Mom says before it rains.",TABLE_ASK="That window's going. Give me a hand with the table? Grab an end.";
    static final int MERRY=0,MARJORIE=1,JOHN=2;
    static final String[] NAMES={"Merry","Marjorie","John"};
    static final int[] ROLES={LiteraryActor.GHOST_MERRY,LiteraryActor.GHOST_MARJORIE,LiteraryActor.GHOST_JOHN};
    static final String SUPPER_TAG="GrowingSupper";

    static final class Day {
        final BlockPos base;long tick;int empty,phase=CHORES,since,choresTicks;boolean fresh=true;
        final UUID[] cast=new UUID[3];
        final Set<Integer> washingDown=new HashSet<>();boolean washingDone,tableMoved,tableAlone;String tableBy="";
        /** When Merry last asked for help with the washing and John with the table, and when each reader saw the shoots, by the day's tick. */
        long washingAsked=-ASK_AGAIN,tableAsked=-ASK_AGAIN;final Map<UUID,Long> shootsAt=new HashMap<>();
        /** Hands on the table's ends: who, which end (0 west, 1 east), until when. */
        final Map<UUID,int[]> holds=new HashMap<>();int johnEnd=-1;long merryNext=MERRY_TAKES;
        boolean johnOut,johnGone,johnWalking,spadeLeft,frameSplit;int floor;
        /** Supper: what is still on the counter, what has reached the table upstairs, who set his plate, who carried anything. */
        final Set<String> counter=new LinkedHashSet<>(),upstairs=new LinkedHashSet<>();final Set<String> carriers=new LinkedHashSet<>();
        String dadBy="",outcome="";long upAt=-1;boolean asked,sistersUp;
        final Map<Integer,List<Vec3>> routes=new HashMap<>();final Map<Integer,Integer> leg=new HashMap<>(),legTicks=new HashMap<>();
        final List<UUID> displays=new ArrayList<>();final Map<UUID,Long> chatter=new HashMap<>();
        Day(BlockPos b){base=b;}
    }
    private static final Map<Long,Day> DAYS=new HashMap<>();
    public static void clearAll(){DAYS.clear();SEEDS.clear();LOOKS.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}

    // ------------------------------------------------------------------------------------------------ for tests
    @Nullable static Day day(BlockPos b){return DAYS.get(b.asLong());}
    public static int phase(BlockPos b){var d=day(b);return d==null?-1:d.phase;}
    public static int floor(BlockPos b){var d=day(b);return d==null?-1:d.floor;}
    public static boolean flag(BlockPos b,String name){var d=day(b);if(d==null)return false;return switch(name){case "washing"->d.washingDone;case "table"->d.tableMoved;case "out"->d.johnOut;case "gone"->d.johnGone;case "spade"->d.spadeLeft;case "frame"->d.frameSplit;case "asked"->d.asked;case "up"->d.sistersUp;default->false;};}
    public static String outcome(BlockPos b){var d=day(b);return d==null?"":d.outcome;}
    public static Set<String> upstairs(BlockPos b){var d=day(b);return d==null?Set.of():Set.copyOf(d.upstairs);}
    @Nullable public static LiteraryActor actor(ServerLevel l,BlockPos b,int who){var d=day(b);return d==null?null:actor(l,d.cast[who]);}
    /** For tests: the current stage of the day moved on by this many ticks. */
    public static void skip(BlockPos b,int ticks){var d=day(b);if(d!=null){if(d.phase==CHORES)d.choresTicks+=ticks;else d.since+=ticks;}}
    public static void resetNow(ServerLevel l,BlockPos b){var d=day(b);if(d!=null){reset(l,d);DAYS.remove(b.asLong());}}

    // ------------------------------------------------------------------------------------------------ records and speech
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void refresh(CompoundTag held,CompoundTag fresh){for(var key:List.copyOf(held.getAllKeys()))held.remove(key);held.merge(fresh);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void sound(ServerLevel l,BlockPos at,SoundEvent e,float volume,float pitch){l.playSound(null,at,e,SoundSource.BLOCKS,volume,pitch);}
    /** Someone at the Barretts' says something: over their head, and in the action bar for readers near enough to hear it. */
    static void speak(ServerLevel l,Day d,int who,String line){
        var a=actor(l,d.cast[who]);if(a==null)return;a.say(line);a.speaks(line);
        for(var r:readers(l,d.base))if(r.distanceToSqr(a)<HEARING*HEARING)r.displayClientMessage(Component.literal(NAMES[who]+": “"+line+"”").withStyle(ChatFormatting.GRAY),true);
    }
    /**
     * One of the family asks a reader for a hand: said once, for every reader near enough to hear it, and all of them are marked as
     * asked. 0.4.571 (run 1601): each reader was asked on their own first step into the yard or the house, so two readers coming in
     * a moment apart were told the whole sentence twice. A reader who comes into hearing while it is still being said has it as the
     * others did. True when it is said now; false while the person is not there, is saying something else, asked less than
     * {@link #ASK_AGAIN} ago, or the reader is out of hearing: they are asked when they come near.
     */
    static boolean ask(ServerPlayer p,Day d,int who,String line,String key,CompoundTag own,long last){
        var l=p.serverLevel();var a=actor(l,d.cast[who]);if(a==null||p.distanceToSqr(a)>=HEARING*HEARING)return false;
        if(a.saying()&&a.getCustomName()!=null&&a.getCustomName().getString().equals(line)){own.putBoolean(key,true);return false;}
        if(a.saying()||d.tick-last<ASK_AGAIN)return false;
        speak(l,d,who,line);own.putBoolean(key,true);
        for(var r:readers(l,d.base)){if(r==p||r.distanceToSqr(a)>=HEARING*HEARING)continue;var theirs=own(r);if(theirs.getBoolean(key)||theirs.getBoolean("Completed"))continue;theirs.putBoolean(key,true);save(r,theirs);}
        return true;
    }
    /** A fact marked in the record of every reader past the gate, who was there for it. */
    static void witnessed(ServerLevel l,Day d,String key){for(var r:readers(l,d.base)){if(!GrowingThingsSite.past(rel(r.position(),d.base)))continue;var own=own(r);if(own.getBoolean("Completed"))continue;own.putBoolean(key,true);save(r,own);}}

    // ------------------------------------------------------------------------------------------------ who is where
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }
    /** Whether a reader could see a point: in reach, before their eyes, and nothing solid or leafy in the way. */
    static boolean sees(ServerPlayer p,Vec3 point,double reach,double cone){
        var eye=p.getEyePosition();var to=point.subtract(eye);double d=to.length();if(d>reach)return false;if(d<.8)return true;
        if(p.getLookAngle().dot(to.scale(1/d))<cone)return false;
        var l=p.level();int steps=(int)(d/.2);
        for(int i=1;i<steps;i++){var at=eye.add(to.scale(i/(double)steps));var st=l.getBlockState(BlockPos.containing(at));
            if(st.isAir()||st.is(GrowingRegistry.FITTING.get()))continue;
            if(st.getBlock() instanceof LeavesBlock||st.canOcclude())return false;}
        return true;
    }
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}

    // ------------------------------------------------------------------------------------------------ arriving
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;var d=DAYS.computeIfAbsent(b.asLong(),k->new Day(b));
        if((d.floor>=1||d.phase>=SUPPER)&&!own.getBoolean("Completed"))own.putBoolean("Partway",true);
        var home=HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.HOME,true);if(!home.isEmpty())own.putString("GrowingHome",home);
        cue(p,"An alley behind a row of houses. Across it, a board fence, and a gate with its latch tied shut with twine.");
    }

    // ------------------------------------------------------------------------------------------------ the day's clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();
        if(!LOOKS.isEmpty())looks(server);
        if(DAYS.isEmpty())return;var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var d:List.copyOf(DAYS.values())){
            d.tick++;var here=readers(l,d.base);
            if(here.isEmpty()){if(++d.empty>=EMPTY_RESET&&l.isLoaded(d.base.offset(GrowingThingsSite.NOTE))){reset(l,d);DAYS.remove(d.base.asLong());}continue;}
            d.empty=0;
            if(!l.isLoaded(d.base.offset(GrowingThingsSite.COUNTER))||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(d.base.offset(GrowingThingsSite.COUNTER))))continue;
            if(d.fresh){d.fresh=false;reset(l,d);}
            ensureCast(l,d);
            walk(l,d);
            switch(d.phase){
                case CHORES->chores(l,d,here);
                case AFTERNOON->afternoon(l,d,here);
                case SUPPER->supper(l,d,here);
                default->{}
            }
        }
    }
    /** Everyone in the family who should be at the house is there; nobody comes back who has gone. */
    static void ensureCast(ServerLevel l,Day d){
        for(int i=0;i<3;i++){
            if(actor(l,d.cast[i])!=null)continue;if(i==JOHN&&d.johnGone)continue;
            var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
            a.bind(PLACE,null);a.appearance(ROLES[i],GhostsSet.STAND);
            var at=switch(i){case MERRY->new Vec3(-5.5,0,-14.5);case MARJORIE->new Vec3(-5.5,1,-32.4);default->new Vec3(-3.5,1,-34.5);};
            float yaw=i==MARJORIE?0:180;place(a,d.base,at,yaw);
            if(l.addFreshEntity(a))d.cast[i]=a.getUUID();
        }
    }
    static void place(LiteraryActor a,BlockPos b,Vec3 r,float yaw){a.moveTo(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.setDeltaMovement(Vec3.ZERO);}
    /** Sends someone along points (local). */
    static void route(Day d,int who,Vec3... points){d.routes.put(who,new ArrayList<>(List.of(points)));d.leg.put(who,0);d.legTicks.put(who,0);}
    static boolean walking(Day d,int who){var r=d.routes.get(who);return r!=null&&d.leg.getOrDefault(who,0)<r.size();}
    static void walk(ServerLevel l,Day d){
        for(var e:List.copyOf(d.routes.entrySet())){int who=e.getKey();var pts=e.getValue();int leg=d.leg.getOrDefault(who,0);if(leg>=pts.size())continue;
            var a=actor(l,d.cast[who]);if(a==null)continue;var target=world(d.base,pts.get(leg));
            int ticks=d.legTicks.merge(who,1,Integer::sum);
            // A leg the footpaths cannot finish (a body caught on a step or a door frame) is finished with a step, so nobody stands stuck.
            if(SceneFootpaths.walk(a,target,.11)||a.position().distanceToSqr(target)<.09||ticks>160){if(ticks>160)a.moveTo(target.x,target.y,target.z,a.getYRot(),0);d.leg.put(who,leg+1);d.legTicks.put(who,0);}
        }
    }

    // ------------------------------------------------------------------------------------------------ the chores
    static void chores(ServerLevel l,Day d,List<ServerPlayer> here){
        boolean anyonePast=false;for(var p:here)if(GrowingThingsSite.past(rel(p.position(),d.base)))anyonePast=true;
        if(!anyonePast)return;
        d.choresTicks++;
        // Merry takes the washing in herself, a piece at a time; quicker with help.
        var merry=actor(l,d.cast[MERRY]);
        if(!d.washingDone&&merry!=null&&d.choresTicks>=d.merryNext&&!walking(d,MERRY)){
            for(int x:GrowingThingsSite.GARMENTS)if(!d.washingDown.contains(x)){takeDown(l,d,x,null);route(d,MERRY,new Vec3(x+.5,0,GrowingThingsSite.LINE_Z+1.5));break;}
            d.merryNext=d.choresTicks+MERRY_TAKES;
        }
        // The table: a pair of hands on each end moves it; a reader alone gets John on the other end.
        if(!d.tableMoved){
            d.holds.entrySet().removeIf(h->{var p=l.getServer().getPlayerList().getPlayer(h.getKey());return p==null||d.tick>h.getValue()[1]||!nearEnd(p,d,h.getValue()[0]);});
            var ends=new HashMap<Integer,UUID>();for(var h:d.holds.entrySet())ends.putIfAbsent(h.getValue()[0],h.getKey());
            var john=actor(l,d.cast[JOHN]);
            if(ends.size()==2)moveTable(l,d,new ArrayList<>(ends.values()),false);
            else if(ends.size()==1&&john!=null){
                int other=1-ends.keySet().iterator().next();
                if(d.johnEnd!=other){d.johnEnd=other;route(d,JOHN,endSpot(other));speak(l,d,JOHN,"Got it. On three.");}
                else if(!walking(d,JOHN))moveTable(l,d,new ArrayList<>(ends.values()),true);
            }else if(d.choresTicks>=CHORE_LIMIT){d.tableAlone=true;moveTable(l,d,List.of(),true);}
        }
        if(!d.washingDone&&d.choresTicks>=CHORE_LIMIT)for(int x:GrowingThingsSite.GARMENTS)if(!d.washingDown.contains(x))takeDown(l,d,x,null);
        if(d.washingDone&&d.tableMoved){d.phase=AFTERNOON;d.since=0;
            var m=actor(l,d.cast[MERRY]);if(m!=null)route(d,MERRY,new Vec3(0.5,0,-27.5),new Vec3(.5,1,-31.5),new Vec3(-1.5,1,-34.5));}
    }
    static Vec3 endSpot(int end){var t=GrowingThingsSite.TABLE_AT;return new Vec3(end==0?t.getX()-.5:t.getX()+2.5,1,t.getZ()+.5);}
    static boolean nearEnd(ServerPlayer p,Day d,int end){var t=GrowingThingsSite.TABLE_AT.offset(end,0,0);return rel(p.position(),d.base).distanceToSqr(Vec3.atBottomCenterOf(t))<2.6*2.6;}
    /** A piece of washing off the line: by a reader, or (no reader) by Merry. */
    static void takeDown(ServerLevel l,Day d,int x,@Nullable ServerPlayer by){
        if(!d.washingDown.add(x))return;
        l.setBlock(d.base.offset(x,1,GrowingThingsSite.LINE_Z),GrowingBlock.of(Kind.LINE,Direction.NORTH),Block.UPDATE_CLIENTS);
        sound(l,d.base.offset(x,1,GrowingThingsSite.LINE_Z),SoundEvents.WOOL_BREAK,.6F,1.2F);
        if(d.washingDown.size()>=GrowingThingsSite.GARMENTS.size()){d.washingDone=true;
            l.setBlock(d.base.offset(GrowingThingsSite.BASKET),GrowingBlock.of(Kind.BASKET,Direction.NORTH,1),Block.UPDATE_CLIENTS);
            speak(l,d,MERRY,"That's all of it. Mom'll be happy.");}
    }
    /** The table lifted off the floor at both ends and set down away from the window. */
    static void moveTable(ServerLevel l,Day d,List<UUID> hands,boolean john){
        for(var c:GrowingThingsSite.table(GrowingThingsSite.TABLE_AT))l.setBlock(d.base.offset(c),Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        var to=GrowingThingsSite.table(GrowingThingsSite.TABLE_MOVED);
        l.setBlock(d.base.offset(to.get(0)),GrowingBlock.of(Kind.TABLE,Direction.NORTH,0),Block.UPDATE_CLIENTS);l.setBlock(d.base.offset(to.get(1)),GrowingBlock.of(Kind.TABLE,Direction.NORTH,1),Block.UPDATE_CLIENTS);
        sound(l,d.base.offset(to.get(0)),SoundEvents.WOOD_PLACE,1F,.7F);
        d.tableMoved=true;d.holds.clear();d.johnEnd=-1;
        var names=new ArrayList<String>();for(var id:hands){var p=l.getServer().getPlayerList().getPlayer(id);if(p!=null)names.add(p.getGameProfile().getName());}
        d.tableBy=String.join(" and ",names);
        for(var id:hands){var p=l.getServer().getPlayerList().getPlayer(id);if(p==null)continue;var own=own(p);
            var others=new ArrayList<String>();for(var n:names)if(!n.equals(p.getGameProfile().getName()))others.add(n);if(john)others.add("John");
            own.putString("TableWith",String.join(" and ",others));own.putBoolean("Committed",true);save(p,own);cue(p,"You lift together and walk it away from the window.");}
        if(hands.isEmpty()){speak(l,d,JOHN,"I'll do it myself, then.");witnessedIn(l,d,"TableAlone");}
        else speak(l,d,JOHN,"That'll do. Thanks.");
        var john0=actor(l,d.cast[JOHN]);if(john0!=null)route(d,JOHN,new Vec3(-2.5,1,-36.5));
    }
    static void witnessedIn(ServerLevel l,Day d,String key){for(var r:readers(l,d.base)){if(!GrowingThingsSite.inHouse(rel(r.position(),d.base)))continue;var own=own(r);if(own.getBoolean("Completed"))continue;own.putBoolean(key,true);save(r,own);}}

    // ------------------------------------------------------------------------------------------------ the afternoon
    static void afternoon(ServerLevel l,Day d,List<ServerPlayer> here){
        d.since++;int t=d.since;
        var john=actor(l,d.cast[JOHN]);
        if(!d.johnOut&&t>=JOHN_OUT){d.johnOut=true;
            speak(l,d,JOHN,"I'm going to clear those back steps before your mom gets home.");
            if(john!=null){john.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SHOVEL));
                route(d,JOHN,new Vec3(.5,1,-31.5),new Vec3(.5,0,-27.5));}
            openBack(l,d,true);
            route(d,MARJORIE,new Vec3(-4.5,1,-32.4));route(d,MERRY,new Vec3(-6.5,1,-32.4));
            witnessed(l,d,"SawJohnGo");
        }
        // John at the step: digging, the shoots round it coming up faster than he can turn them.
        if(d.johnOut&&!d.johnGone&&john!=null&&!d.johnWalking&&!walking(d,JOHN)){
            SceneFootpaths.face(john,world(d.base,new Vec3(.5,.5,GrowingThingsSite.STEP_Z+.5)),30);
            if(t%30==0){john.swing(InteractionHand.MAIN_HAND);sound(l,d.base.offset(0,0,GrowingThingsSite.STEP_Z),GrowingRegistry.SPADE.get(),.9F,.9F+l.random.nextFloat()*.2F);}
            if(t%120==0)GrowingThingsSite.grow(l,d.base,4);
        }
        // He is gone the first moment nobody is looking; watched long enough, he leaves the spade standing and walks round the side of the house.
        if(d.johnOut&&!d.johnGone&&john!=null&&t>=JOHN_GONE&&!watched(l,d,john)){johnGone(l,d,john);}
        else if(d.johnOut&&!d.johnGone&&john!=null&&t>=JOHN_WALKS&&!d.johnWalking){d.johnWalking=true;leaveSpade(l,d);john.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            route(d,JOHN,new Vec3(-4.5,0,-27.5),new Vec3(-12.5,0,-27.5),new Vec3(-12.5,0,-36.5));witnessed(l,d,"SawJohnWalk");}
        if(!d.frameSplit&&t>=FRAME_SPLITS){d.frameSplit=true;
            for(var w:GrowingThingsSite.WINDOW)l.setBlock(d.base.offset(w),GrowingBlock.of(Kind.FRAME,Direction.SOUTH,2),Block.UPDATE_CLIENTS);
            sound(l,d.base.offset(GrowingThingsSite.WINDOW.get(0)),GrowingRegistry.FRAME.get(),1F,1F);
            for(var p:here){var r=rel(p.position(),d.base);if(!GrowingThingsSite.inKitchen(r))continue;var own=own(p);if(own.getBoolean("Completed"))continue;
                own.putBoolean("SawFrame",true);
                if(own.getString("GrowingHome").equals("DANGER")){own.putBoolean("Shielded",true);cue(p,"The window frame splits. Marjorie steps between you and it without a word.");}
                save(p,own);}
        }
        if(d.floor<1&&t>=FLOOR_1)lift(l,d,1);
        if(d.floor<2&&t>=FLOOR_2)lift(l,d,2);
        if(d.floor<3&&t>=FLOOR_3){lift(l,d,3);beginSupper(l,d,here);}
        chatter(l,d,here);
    }
    static boolean watched(ServerLevel l,Day d,LiteraryActor a){for(var p:readers(l,d.base))if(!p.isSpectator()&&(sees(p,a.getEyePosition(),40,.3)||sees(p,a.position().add(0,.6,0),40,.3)))return true;return false;}
    static void openBack(ServerLevel l,Day d,boolean open){
        for(int y=0;y<=1;y++){var at=d.base.offset(GrowingThingsSite.BACK_DOOR.getX(),1+y,GrowingThingsSite.HOUSE_S);var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock)l.setBlock(at,s.setValue(DoorBlock.OPEN,open),Block.UPDATE_CLIENTS);}
    }
    static void leaveSpade(ServerLevel l,Day d){
        if(d.spadeLeft)return;d.spadeLeft=true;
        l.setBlock(d.base.offset(GrowingThingsSite.SPADE),GrowingBlock.of(Kind.SPADE,Direction.NORTH),Block.UPDATE_CLIENTS);
    }
    static void johnGone(ServerLevel l,Day d,LiteraryActor john){
        d.johnGone=true;john.discard();d.routes.remove(JOHN);leaveSpade(l,d);
        for(int x=-1;x<=1;x++)l.setBlock(d.base.offset(x,0,GrowingThingsSite.STEP_Z),GrowingBlock.of(Kind.STEPS,Direction.NORTH,2),Block.UPDATE_CLIENTS);
        GrowingThingsSite.grow(l,d.base,6);GrowingThingsSite.grow(l,d.base,3);
        sound(l,d.base.offset(0,0,GrowingThingsSite.STEP_Z),GrowingRegistry.SHOOTS.get(),.8F,1F);
        witnessed(l,d,"JohnGone");
        speak(l,d,MARJORIE,"He's out with them.");
    }
    /** The kitchen floor's boards a stage higher, everywhere but under the fittings and the table; nobody is left standing inside them. */
    static void lift(ServerLevel l,Day d,int stage){
        d.floor=stage;var table=d.tableMoved?GrowingThingsSite.table(GrowingThingsSite.TABLE_MOVED):GrowingThingsSite.table(GrowingThingsSite.TABLE_AT);
        double h=new int[]{1,4,9,14}[stage]/16.0;
        for(var c:GrowingThingsSite.floorCells()){if(table.contains(c))continue;var at=d.base.offset(c);
            var box=new AABB(at);boolean actor=!l.getEntitiesOfClass(LiteraryActor.class,box).isEmpty();if(actor)continue;
            var s=l.getBlockState(at);if(!s.isAir()&&!GrowingBlock.is(s,Kind.FLOOR))continue;
            l.setBlock(at,GrowingBlock.of(Kind.FLOOR,Direction.from2DDataValue(Math.floorMod(c.getX()+c.getZ(),2)*2),stage),Block.UPDATE_CLIENTS);
            for(var p:l.getEntitiesOfClass(ServerPlayer.class,box))if(p.getY()<at.getY()+h)p.teleportTo(p.getX(),at.getY()+h+.01,p.getZ());
        }
        sound(l,d.base.offset(-4,1,-35),GrowingRegistry.BOARDS.get(),1F,.9F+stage*.05F);
        for(var p:readers(l,d.base)){var r=rel(p.position(),d.base);if(!GrowingThingsSite.inHouse(r)||r.y>4.5)continue;var own=own(p);if(own.getBoolean("Completed"))continue;own.putInt("SawFloor",Math.max(own.getInt("SawFloor"),stage));save(p,own);
            cue(p,switch(stage){case 1->"Under your feet the kitchen floor creaks, and a seam in the boards stands up.";case 2->"The kitchen floor is tenting. Shoots stand in the gaps between the boards.";default->"The boards come up with a crack. The kitchen floor is a slope of broken planks and shoots.";});}
    }
    /** Merry talks; Marjorie, at the window, mostly doesn't. */
    static final String[] MERRY_LINES={"Marjorie says they come up in the night. I've never seen them do it.","Mom keeps asking Dad to do the steps. He keeps saying Saturday.",
        "Do you have a yard? Ours used to be just grass.","Marjorie won't pull them. She says they don't like it.","If you put your ear on the floor you can hear them."};
    static void chatter(ServerLevel l,Day d,List<ServerPlayer> here){
        if(d.since%300!=150)return;var merry=actor(l,d.cast[MERRY]);if(merry==null)return;
        boolean near=false;for(var p:here)if(p.distanceToSqr(merry)<10*10)near=true;if(!near)return;
        speak(l,d,MERRY,d.johnGone?"Dad's still out there.":MERRY_LINES[(d.since/300)%MERRY_LINES.length]);
    }

    // ------------------------------------------------------------------------------------------------ supper
    static void beginSupper(ServerLevel l,Day d,List<ServerPlayer> here){
        d.phase=SUPPER;d.since=0;d.asked=true;
        d.counter.clear();d.counter.addAll(List.of("pot","loaf","dad"));showCounter(l,d);
        speak(l,d,MERRY,"We can't eat down here. Mom said if we had to, we could eat upstairs.");
        for(var p:here){var r=rel(p.position(),d.base);if(!GrowingThingsSite.past(r))continue;var own=own(p);if(own.getBoolean("Completed"))continue;own.putBoolean("HeardAsk",true);save(p,own);
            p.displayClientMessage(Component.literal("Merry: “Should we bring Dad's plate?”").withStyle(ChatFormatting.GRAY),false);}
        // The girls take their own plates up; the pot, the loaf and their father's plate wait on the counter by the hall door.
        for(int i:new int[]{MERRY,MARJORIE}){var a=actor(l,d.cast[i]);if(a!=null)a.setItemInHand(InteractionHand.MAIN_HAND,plate(NAMES[i]));
            route(d,i,new Vec3(1.5,1,-35.5),new Vec3(4.5,1,-34.5),new Vec3(8.5,1,-32.4),new Vec3(8.5,6,-38.5),new Vec3(5.5,6,-38.5),new Vec3(3.5,6,-35.5),new Vec3(.5,6,-35.5),
                i==MERRY?new Vec3(-5.6,6,-32.5):new Vec3(-4.0,6,-33.7));}
    }
    static void supper(ServerLevel l,Day d,List<ServerPlayer> here){
        d.since++;
        if(!d.sistersUp&&!walking(d,MERRY)&&!walking(d,MARJORIE)){d.sistersUp=true;
            for(int i:new int[]{MERRY,MARJORIE}){var a=actor(l,d.cast[i]);if(a==null)continue;a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);d.upstairs.add(NAMES[i].toLowerCase(Locale.ROOT));
                a.appearance(ROLES[i],GhostsSet.SIT);SceneFootpaths.face(a,world(d.base,new Vec3(-3.5,6.7,-32.5)),180);}
            showUpstairs(l,d);
            // The House's read: Merry keeps the place beside her for a reader who came from somewhere safe.
            for(var p:here){var own=own(p);if(own.getBoolean("Completed")||!own.getString("GrowingHome").equals("HAVEN")||!GrowingThingsSite.upstairs(rel(p.position(),d.base)))continue;
                own.putBoolean("Kept",true);save(p,own);cue(p,"Merry pats the place beside her. “You can sit here.”");break;}
        }
        if(d.upAt<0&&d.upstairs.contains("pot")&&d.upstairs.contains("loaf")){d.upAt=d.tick;speak(l,d,MERRY,"Sit down, sit down.");}
        boolean plateCarried=false;for(var p:here)if(carrying(p).contains("dad"))plateCarried=true;
        if(d.upAt>=0&&(d.upstairs.contains("dad")||d.tick-d.upAt>=(plateCarried?BEAT*3:BEAT)))resolveAll(l,d,here);
        else if(d.since>=SUPPER_LIMIT){
            // Nobody carried it: Merry goes down for the pot and the loaf herself, and leaves his plate.
            for(var w:List.of("pot","loaf"))if(d.counter.remove(w))d.upstairs.add(w);showCounter(l,d);showUpstairs(l,d);resolveAll(l,d,here);
        }
    }
    static ItemStack plate(String whose){
        var s=new ItemStack(GrowingRegistry.PLATE.get());s.set(DataComponents.CUSTOM_NAME,Component.literal(whose.equals("John")?"Dad's plate":whose+"'s plate"));
        var tag=new CompoundTag();tag.putString(SUPPER_TAG,whose.equals("John")?"dad":whose.toLowerCase(Locale.ROOT));s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return s;
    }
    static ItemStack supperItem(String what){
        if(what.equals("dad"))return plate("John");
        var s=new ItemStack(what.equals("pot")?GrowingRegistry.POT.get():GrowingRegistry.LOAF.get());var tag=new CompoundTag();tag.putString(SUPPER_TAG,what);s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return s;
    }
    static String supperOf(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null?"":c.copyTag().getString(SUPPER_TAG);}
    static Set<String> carrying(ServerPlayer p){var out=new LinkedHashSet<String>();for(var st:p.getInventory().items){var w=supperOf(st);if(!w.isEmpty())out.add(w);}for(var st:p.getInventory().offhand){var w=supperOf(st);if(!w.isEmpty())out.add(w);}return out;}
    static void dropSupper(ServerPlayer p){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(!supperOf(inv.getItem(i)).isEmpty())inv.setItem(i,ItemStack.EMPTY);}
    /** What lies on the counter and on the table upstairs, as things lying there. */
    static void showCounter(ServerLevel l,Day d){clearDisplays(l,d,"growing_counter");int i=0;var c=GrowingThingsSite.COUNTER;
        for(var w:d.counter){var at=world(d.base,new Vec3(c.getX()+.3+i*.2,c.getY()+1.01,c.getZ()+.3+i*.2));var e=KitchenFittings.lay(l,at,supperItem(w),i*40,"growing_counter",.4F);if(e!=null)d.displays.add(e.getUUID());i++;}}
    static final Map<String,Vec3> SPOTS=Map.of("dad",new Vec3(-3.5,6.76,-32.3),"merry",new Vec3(-4.7,6.76,-32.5),"marjorie",new Vec3(-4.0,6.76,-32.8),"pot",new Vec3(-4.3,6.76,-32.45),"loaf",new Vec3(-3.35,6.76,-32.75));
    static void showUpstairs(ServerLevel l,Day d){clearDisplays(l,d,"growing_table");
        for(var w:d.upstairs){var spot=SPOTS.get(w);if(spot==null)continue;var item=w.equals("merry")?plate("Merry"):w.equals("marjorie")?plate("Marjorie"):supperItem(w);
            var e=KitchenFittings.lay(l,world(d.base,spot),item,w.hashCode()%90,"growing_table",.45F);if(e!=null)d.displays.add(e.getUUID());}}
    static void clearDisplays(ServerLevel l,Day d,String tag){for(var it=d.displays.iterator();it.hasNext();){var e=l.getEntity(it.next());if(e==null){it.remove();continue;}if(e.getTags().contains(tag)){e.discard();it.remove();}}}

    // ------------------------------------------------------------------------------------------------ endings
    /** Supper is up: his plate there or not. Everyone past the gate has their ending; whoever set his plate down, or carried the rest, is the actor. */
    static void resolveAll(ServerLevel l,Day d,List<ServerPlayer> here){
        if(d.phase==OVER)return;d.phase=OVER;boolean his=d.upstairs.contains("dad");d.outcome=his?CARRIED:LEFT;
        if(his){speak(l,d,MARJORIE,"He'll come up for it.");}
        else{var m=actor(l,d.cast[MERRY]);if(m!=null)SceneFootpaths.face(m,world(d.base,new Vec3(3.5,6,-35.5)),180);
            for(var p:here)if(GrowingThingsSite.inHouse(rel(p.position(),d.base)))cue(p,"Merry looks back down the stairs toward the kitchen, and doesn't say anything else.");}
        String actor=his?d.dadBy:String.join(" and ",d.carriers);
        for(var p:here){var own=own(p);if(own.getBoolean("Completed"))continue;var r=rel(p.position(),d.base);
            if(!own.getBoolean("Committed")&&!GrowingThingsSite.past(r))continue;
            String name=p.getGameProfile().getName();
            VignetteRole role=own.getBoolean("Partway")?VignetteRole.PARTWAY:(his?name.equals(d.dadBy):d.carriers.isEmpty()||d.carriers.contains(name))?VignetteRole.ACTOR:VignetteRole.WITNESS;
            own.putString("DadBy",d.dadBy);own.putString("Carriers",String.join(" and ",d.carriers));
            resolve(p,own,d.outcome,role,actor);
            cue(p,"Marjorie puts something in your hand: a seed head, dry, off the kitchen sill.");
        }
    }
    static void resolve(ServerPlayer p,CompoundTag own,String outcome,VignetteRole role,String actor){
        if(own.getBoolean("Completed"))return;var d=LabyrinthData.get(p.server);
        own.putString("Outcome",outcome);own.putBoolean("Ready",true);own.putString("GrowingRole",role.id());own.putBoolean("Completed",true);own.putBoolean("Taken_Ending",true);own.putInt("Beat",own.getInt("Visit"));
        save(p,own);
        if(role==VignetteRole.ACTOR)WitnessAccount.resolve(p,WitnessAccount.Story.GROWING_THINGS,outcome);else WitnessAccount.resolveAs(p,WitnessAccount.Story.GROWING_THINGS,outcome,role,actor);
        d.setCompleted(PLACE.id(),true);
        dropSupper(p);
        LiteraryVignettes.reward(p,PLACE,own,"Ending",GrowingThingsTexts.account(p.getGameProfile().getName(),own));
        var seed=new ItemStack(GrowingRegistry.SEED_HEAD.get());if(!p.getInventory().add(seed))p.drop(seed,false);
        var fresh=own(p);refresh(own,fresh);
    }

    // ------------------------------------------------------------------------------------------------ each reader
    /** Every five ticks, for each reader in the alley, the yard or the house. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag held){
        save(p,held);var own=own(p);var d=day(b);
        if(d==null){refresh(held,own);return;}
        var r=rel(p.position(),b);
        if(!own.getBoolean("Completed")){
            if(GrowingThingsSite.inYard(r)&&!own.getBoolean("SawShoots")){own.putBoolean("SawShoots",true);d.shootsAt.put(p.getUUID(),d.tick);cue(p,"Shoots in the lawn where nothing was planted, more of them toward the house.");}
            // 0.4.571 (run 1601): Merry and John each ask once for everybody in hearing (ask), and Merry not over the shoots' own line.
            if(d.phase==CHORES&&!d.washingDone&&!own.getBoolean("Asked")&&GrowingThingsSite.inYard(r)&&d.tick-d.shootsAt.getOrDefault(p.getUUID(),-(long)READ_FIRST)>=READ_FIRST
                &&ask(p,d,MERRY,WASHING_ASK,"Asked",own,d.washingAsked))d.washingAsked=d.tick;
            if(GrowingThingsSite.inHouse(r)&&!own.getBoolean("Inside")){own.putBoolean("Inside",true);own.putBoolean("Committed",true);}
            if(d.phase==CHORES&&!d.tableMoved&&!own.getBoolean("AskedTable")&&GrowingThingsSite.inHouse(r)&&ask(p,d,JOHN,TABLE_ASK,"AskedTable",own,d.tableAsked))d.tableAsked=d.tick;
            if(d.spadeLeft&&!own.getBoolean("SawSpade")&&sees(p,Vec3.atCenterOf(b.offset(GrowingThingsSite.SPADE)),24,.7)){own.putBoolean("SawSpade",true);
                cue(p,d.johnGone?"His spade is standing in the shoots where the back step was.":"His spade is standing in the shoots by the back step.");}
            // Back out through the gate before supper is up: their own ending.
            if(own.getBoolean("Committed")&&d.phase<OVER&&GrowingThingsSite.inAlley(r)){
                own.putBoolean("WentBack",true);resolve(p,own,WENT,VignetteRole.ACTOR,"");
                cue(p,"You're back in the alley. Over the fence: the washing line, the back step, the kitchen window. Nobody calls after you.");
            }
        }
        save(p,own);refresh(held,own);
    }

    // ------------------------------------------------------------------------------------------------ hands on things
    static void begin(ServerPlayer p,String header){Talk.begin(p,TALK,header);}
    /** Clicks on the Barretts' fittings: the washing, the table, the counter, the card table upstairs. */
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(LiteraryVignettes.current(p)!=PLACE)return false;var b=LiteraryVignettes.base(p,PLACE);if(b==null)return false;var d=day(b);if(d==null)return false;
        var rel=pos.subtract(b);var own=own(p);var l=p.serverLevel();
        if(GrowingBlock.is(s,Kind.WASHING)){
            if(own.getBoolean("Completed"))return false;takeDown(l,d,rel.getX(),p);own.putInt("Washing",own.getInt("Washing")+1);own.putBoolean("Committed",true);save(p,own);
            cue(p,"You take it off the line and fold it over your arm. It's nearly dry.");return true;}
        if(GrowingBlock.is(s,Kind.TABLE)&&d.phase==CHORES&&!d.tableMoved&&!own.getBoolean("Completed")){
            var t=GrowingThingsSite.TABLE_AT;int end=rel.getX()==t.getX()?0:1;
            d.holds.put(p.getUUID(),new int[]{end,(int)d.tick+HOLD});own.putBoolean("Committed",true);own.putBoolean("HeldTable",true);save(p,own);
            cue(p,"You take hold of the "+(end==0?"west":"east")+" end.");return true;}
        if(GrowingBlock.is(s,Kind.COUNTER)&&d.phase==SUPPER){
            begin(p,"The counter");
            if(d.counter.isEmpty()){Talk.line(p,"","Nothing left on the counter but the rings the pot left.");Talk.ask(p,Talk.act("Leave it.",Talk::close));return true;}
            Talk.line(p,"",d.counter.contains("dad")?"The pot, the loaf, and Dad's plate, his name scratched into the rim like the others'.":"What's left of supper.");
            var replies=new ArrayList<Talk.Reply>();
            if(d.counter.contains("pot"))replies.add(Talk.act("Take the pot.",q->take(q,"pot")));
            if(d.counter.contains("loaf"))replies.add(Talk.act("Take the loaf.",q->take(q,"loaf")));
            if(d.counter.contains("dad"))replies.add(Talk.act("Take Dad's plate.",q->take(q,"dad")));
            replies.add(Talk.act("Leave them.",Talk::close));Talk.ask(p,replies);return true;}
        if(GrowingBlock.is(s,Kind.UP_TABLE)&&d.phase==SUPPER){
            var carried=carrying(p);if(carried.isEmpty()){cue(p,d.sistersUp?"The girls are at the table, waiting.":"The card table, set by the window.");return true;}
            for(var w:carried)setDown(l,d,p,w);dropSupper(p);showUpstairs(l,d);return true;}
        if(GrowingBlock.is(s,Kind.BASKET)&&d.washingDone){cue(p,"The washing, folded into the basket.");return true;}
        return false;
    }
    static void take(ServerPlayer p,String what){
        Talk.close(p);var b=LiteraryVignettes.base(p,PLACE);var d=b==null?null:day(b);if(d==null||d.phase!=SUPPER||!d.counter.remove(what))return;
        var st=supperItem(what);if(!p.getInventory().add(st))p.drop(st,false);showCounter(p.serverLevel(),d);
        var own=own(p);own.putBoolean("Committed",true);if(what.equals("dad"))own.putBoolean("TookPlate",true);save(p,own);
        cue(p,what.equals("dad")?"His plate. His name is scratched into the rim, the way the others' are.":what.equals("pot")?"The pot, still warm through the cloth.":"The loaf, wrapped in a tea towel.");
    }
    static void setDown(ServerLevel l,Day d,ServerPlayer p,String what){
        if(!d.upstairs.add(what))return;String name=p.getGameProfile().getName();var own=own(p);
        var list=new ArrayList<>(Arrays.asList(own.getString("Carried").isEmpty()?new String[0]:own.getString("Carried").split(",")));if(!list.contains(what))list.add(what);own.putString("Carried",String.join(",",list));
        if(what.equals("dad")){d.dadBy=name;cue(p,"You set his plate at the place by the window.");}
        else{d.carriers.add(name);cue(p,"You set it down on the card table.");}
        save(p,own);
    }
    /** Right-clicking one of the family. */
    public static void talk(ServerPlayer p,LiteraryActor a){
        var b=LiteraryVignettes.base(p,PLACE);var d=b==null?null:day(b);if(d==null)return;
        String line=switch(a.role()){
            case LiteraryActor.GHOST_MERRY->d.phase==CHORES?(d.washingDone?"Dad's moving the table. The window's doing the thing again.":"Mom says it all has to come in before it rains."):d.phase==AFTERNOON?(d.johnGone?"Dad's still out there.":"He's been saying he'd do the steps since forever."):d.phase==SUPPER?"Can you carry something up? I've got mine.":d.outcome.equals(CARRIED)?"He'll come up when he's done.":"";
            case LiteraryActor.GHOST_MARJORIE->d.johnGone?"He's out with them.":d.phase==OVER?"":"Don't pull them. They don't like it.";
            case LiteraryActor.GHOST_JOHN->d.phase==CHORES&&!d.tableMoved?"Grab an end, would you?":"I'll get to those steps.";
            default->"";
        };
        if(line.isEmpty()){cue(p,a.role()==LiteraryActor.GHOST_MERRY?"Merry doesn't answer. She's looking at the stairs.":"Marjorie is looking at the window.");return;}
        int who=a.role()==LiteraryActor.GHOST_MERRY?MERRY:a.role()==LiteraryActor.GHOST_MARJORIE?MARJORIE:JOHN;speak(p.serverLevel(),d,who,line);
    }
    /** The front door is locked; their mother has the key. */
    @SubscribeEvent public static void frontDoor(PlayerInteractEvent.RightClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||LiteraryVignettes.current(p)!=PLACE)return;var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        var rel=e.getPos().subtract(b);var door=new BlockPos(GrowingThingsSite.FRONT_DOOR.getX(),1,GrowingThingsSite.HOUSE_N);
        if(rel.equals(door)||rel.equals(door.above())){e.setCanceled(true);cue(p,"The front door is locked. Their mother has the key.");}
    }

    // ------------------------------------------------------------------------------------------------ the seed head, carried
    private static final Set<UUID> SEEDS=new HashSet<>();
    public static boolean carriesSeed(UUID p){return SEEDS.contains(p);}
    @SubscribeEvent public static void seeds(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%20!=0)return;
        for(var p:server.getPlayerList().getPlayers()){boolean has=false;for(var st:p.getInventory().items)if(st.is(GrowingRegistry.SEED_HEAD.get()))has=true;if(has)SEEDS.add(p.getUUID());else SEEDS.remove(p.getUUID());}
    }
    /** The House's places where things grow, that a carried seed head leans the dealing toward. */
    static final Set<LabyrinthPlace> OVERGROWN=EnumSet.of(LabyrinthPlace.YARD_STORM_FIELD,LabyrinthPlace.YARD_WINDBREAK,LabyrinthPlace.MARSH_SUPPER,LabyrinthPlace.TALL_GRASS,LabyrinthPlace.SPHINX_FIELD);
    public static boolean overgrown(LabyrinthPlace place){return OVERGROWN.contains(place);}

    // ------------------------------------------------------------------------------------------------ the Ghosts Set, years later
    /** Where the drawing is held to the Barretts' fridge in the Ghosts Set (BarrettHome's fridge at x -10, z -25, facing south). */
    public static final BlockPos DRAWING_AT=new BlockPos(-10,0,-24);
    /** A reader who has been to the Barretts' on that Saturday. */
    public static boolean knows(LabyrinthData d,UUID p){var own=LiteraryVignettes.personal(d,p,PLACE);return own.getBoolean("Completed")&&!own.getString("Outcome").isEmpty();}
    /** Merry, in the Ghosts Set, looking at a reader she remembers: her own copy, for that reader's eyes only. */
    private static final Map<UUID,Object[]> LOOKS=new HashMap<>();
    /** Called each time the Ghosts Set looks after one of its readers. Returns whether the drawing was shown. */
    public static boolean ghosts(ServerPlayer p,BlockPos b,CompoundTag ghostsOwn){
        var d=LabyrinthData.get(p.server);if(!knows(d,p.getUUID()))return false;var l=p.server.getLevel(NovelRooms.dimension(LabyrinthPlace.GHOSTS_SET));if(l==null)return false;
        boolean shown=false;var at=b.offset(DRAWING_AT);
        if(l.isLoaded(at)&&l.getBlockState(at).isAir()&&(p.tickCount%100<5||!ghostsOwn.getBoolean("Drawing0350"))){p.connection.send(new ClientboundBlockUpdatePacket(at,GrowingBlock.of(Kind.DRAWING,Direction.SOUTH)));shown=true;
            if(!ghostsOwn.getBoolean("Drawing0350"))ghostsOwn.putBoolean("Drawing0350",true);}
        if(!ghostsOwn.getBoolean("MerryKnew0350")){
            var world=LiteraryVignettes.shared(d,LabyrinthPlace.GHOSTS_SET);String key=LiteraryVignettes.actorId(p,LabyrinthPlace.GHOSTS_SET,"FilmMerry",true);
            if(world.hasUUID(key)&&l.getEntity(world.getUUID(key)) instanceof LiteraryActor merry&&merry.distanceToSqr(p)<7*7&&sees(p,merry.getEyePosition(),8,.6)){
                ghostsOwn.putBoolean("MerryKnew0350",true);LOOKS.put(merry.getUUID(),new Object[]{p.getUUID(),l.getGameTime()+50});
                cue(p,"Merry looks at you for a moment, as if she remembers you from somewhere.");}
        }
        return shown;
    }
    static void looks(net.minecraft.server.MinecraftServer server){
        var l=server.getLevel(NovelRooms.dimension(LabyrinthPlace.GHOSTS_SET));if(l==null){LOOKS.clear();return;}
        for(var it=LOOKS.entrySet().iterator();it.hasNext();){var e=it.next();var v=e.getValue();
            if(l.getGameTime()>(long)v[1]||!(l.getEntity(e.getKey()) instanceof LiteraryActor merry)){it.remove();continue;}
            var p=server.getPlayerList().getPlayer((UUID)v[0]);if(p==null){it.remove();continue;}
            var to=p.getEyePosition().subtract(merry.getEyePosition());float yaw=(float)(Mth.atan2(to.z,to.x)*180/Math.PI)-90;merry.setYHeadRot(yaw);
        }
    }

    // ------------------------------------------------------------------------------------------------ dealing and the reset
    /** Dealt once each, and only to a reader who has crossed the House's yard. */
    public static boolean dealable(LabyrinthData d,UUID p){
        if(LiteraryVignettes.personal(d,p,PLACE).getBoolean("Completed"))return false;
        var seen=d.visited(p);for(var y:YardCrossings.ORDINARY)if(seen.contains(y.id()))return true;return false;
    }
    /** Nobody at the Barretts': the yard and house as they were that morning, and the family home again. */
    static void reset(ServerLevel l,Day d){
        var b=d.base;
        var area=new AABB(b.offset(GrowingThingsSite.WEST,-3,GrowingThingsSite.NORTH)).minmax(new AABB(b.offset(GrowingThingsSite.EAST,14,2)));
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,area,a->a.role()==LiteraryActor.GHOST_MERRY||a.role()==LiteraryActor.GHOST_MARJORIE||a.role()==LiteraryActor.GHOST_JOHN))if(a.owner().isEmpty())a.discard();
        for(var e:l.getEntitiesOfClass(Display.ItemDisplay.class,area,e->e.getTags().contains("growing_counter")||e.getTags().contains("growing_table")))e.discard();
        GrowingThingsSite.living(l,b);openBack(l,d,false);
        d.phase=CHORES;d.since=0;d.choresTicks=0;Arrays.fill(d.cast,null);d.washingDown.clear();d.washingDone=false;d.tableMoved=false;d.tableAlone=false;d.tableBy="";d.washingAsked=-ASK_AGAIN;d.tableAsked=-ASK_AGAIN;d.shootsAt.clear();
        d.holds.clear();d.johnEnd=-1;d.merryNext=MERRY_TAKES;d.johnOut=false;d.johnGone=false;d.johnWalking=false;d.spadeLeft=false;d.frameSplit=false;d.floor=0;
        d.counter.clear();d.upstairs.clear();d.carriers.clear();d.dadBy="";d.outcome="";d.upAt=-1;d.asked=false;d.sistersUp=false;d.routes.clear();d.leg.clear();d.legTicks.clear();d.displays.clear();
    }
}
