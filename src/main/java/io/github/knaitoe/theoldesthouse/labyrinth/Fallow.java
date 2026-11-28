package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.672, Fallow, the first part (docs/FALLOW_0_4_672.md), from the owner's approval of docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md and
 * the plan in docs/FALLOW_PLAN.md. The place alone: a gas station and grocery on a two-lane road in the wheat, on a late summer
 * afternoon (FallowSite). Five people are there: Harlan, who keeps the station, on his stool behind the register; Faye at the coffee
 * machine, whose car is up on the lift; her daughter Opal out front; Faye's man Boyd in the garage, watching the car; and Otis, a long-haul
 * driver, by his rig at the diesel pump (stranger phases 263 to 267; the stranger's skin, on 268, is painted now and he comes in part 2).
 * Each has two lines, counted per reader, and every thing answers a hand. The cellar's doors open and shut for anyone.
 *
 * <p>0.4.673, the second part (docs/FALLOW_0_4_673.md): the afternoon, and the House deals it from ten crossings down. One clock runs for
 * everyone at the station while anyone is there, and starts again after the place has stood empty five seconds. Harlan gives each reader
 * a chore, by the House's read of their age: one read as young keeps Opal company, anyone else minds the till; a second reader gets
 * whichever the first did not. He gets down off his stool and goes round to the car and under it. Opal goes to him for her bean, which
 * is in the car, and sets it down out front in the sun, and talks to whoever stays by her. Faye buys milk, Otis coffee and diesel: a
 * reader behind the counter rings them up at the register, or they leave the money. Then a man nobody knows comes walking along the
 * road, buys a bottle of pop, asks Opal her name (she does not tell him), asks a reader the time, and walks off up the track into the
 * wheat toward the far pad, and is gone the first moment nobody sees him. Each reader keeps their own chore, what they rang up, how long
 * they kept Opal company and what they told the man. Nothing here is a Witness source yet.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Fallow {
    private Fallow(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.FALLOW;
    /** The five at the station on 263 to 267, and the stranger on 268. */
    public static final int FIRST=263,KEEPER=0,WOMAN=1,GIRL=2,MAN=3,DRIVER=4,PEOPLE=5,STRANGER=5,CAST=6;
    private static final String[] SKINS={"fallow_harlan","fallow_faye","fallow_opal","fallow_boyd","fallow_otis","fallow_stranger"};
    private static final BlockPos[] AT={FallowSite.KEEPER_AT,FallowSite.WOMAN_AT,FallowSite.GIRL_AT,FallowSite.MAN_AT,FallowSite.DRIVER_AT};
    /** Which way each faces: the keeper the counter, Faye the coffee machine, Opal the road, Boyd the car, Otis his pump. */
    private static final Direction[] FACING={Direction.NORTH,Direction.EAST,Direction.NORTH,Direction.SOUTH,Direction.EAST};
    public static boolean cast(int phase){return phase>=FIRST&&phase<FIRST+CAST;}
    public static boolean child(int phase){return phase==FIRST+GIRL;}
    public static String skin(int phase){return SKINS[phase-FIRST];}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    /** 0.4.683: the man on the road in a face that is not his (FallowRoad): the keeper's, the big man's or the driver's skin; null when
     * he has his own, or the viewer's (drawn on the player's model, LookoutRenderers.Double). */
    public static @Nullable String dressed(int phase,int marks){
        return phase==FIRST+STRANGER&&marks>=FallowRoad.KEEPER_FACE&&marks<FallowRoad.VIEWER?skin(FIRST+FallowRoad.faceOf(marks)):null;
    }
    /** 0.4.683: the man on the road with the face of whoever looks at him. */
    public static boolean viewersFace(LiteraryActor a){return is(a)&&a.phase()==FIRST+STRANGER&&a.marks()==FallowRoad.VIEWER;}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** 0.4.673: dealt once a reader has come this many crossings down, after Last Seen's eight and before Not Yet's twelve. */
    public static final int DEPTH=10;
    public static boolean dealable(LabyrinthData d,UUID reader){
        if(d.returnDepth(reader)<DEPTH)return false;
        // 0.4.681: a station the light has had is dealt only to readers who were there when it came; 0.4.683: when this afternoon's came.
        for(var s:ACTIVE.values())if(s.strike.toneAt>=0&&s.strike.since(s.tick)>=FallowStrike.LIGHT&&LiteraryVignettes.personal(d,reader,PLACE).getInt("Afternoon")!=s.id)return false;
        return true;
    }
    /** How the afternoon's people hold themselves (LiteraryRenderers.fallow): working overhead under the car, reaching up or out, a cup held
     * in both hands, kneeling by the bean, pouring at the machine. */
    public static final int WORK=1,REACH=2,CUP=3,KNEEL=4,POUR=5;
    /** 0.4.674: Otis with Opal in his arms (FallowStrike). */
    public static final int CARRY=6;
    /** 0.4.681, below in the buried days (FallowBuried): sat on the cellar's floor, the hands on the knees; and dozing there at night,
     * the head down and the arms folded. */
    public static final int SIT=7,DOZE=8;

    /** A reader in: the five are put at their places if they are not there, the car is up on the lift, and the afternoon is heard. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();
        var s=ACTIVE.get(b.asLong());
        // A fresh afternoon: the bean is still in the car, the man not yet on the road.
        if(s==null){s=new Site(b);ACTIVE.put(b.asLong(),s);afresh(l,b);}
        ensure(l,b,s);car(l,b);
        own.putInt("Visits",own.getInt("Visits")+1);
        l.playSound(null,b.offset(0,1,FallowSite.STORE_N-2),FallowRegistry.WIND.get(),SoundSource.AMBIENT,.4F,1F);
    }

    /** The scene's whole ground, where any of them may have walked. */
    static AABB scene(BlockPos b){return new AABB(b.getX()+FallowSite.WEST,b.getY()-6,b.getZ()+FallowSite.NORTH,b.getX()+FallowSite.EAST+1,b.getY()+16,b.getZ()+1);}
    /** One of the six, wherever they are in the scene (0.4.673: they walk about now); a second of anyone is let go. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){
        LiteraryActor found=null;
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,scene(b),x->is(x)&&x.phase()==FIRST+who&&x.isAlive())){if(found==null)found=a;else a.discard();}
        return found;
    }
    /** Each of the five is put at their place if they are not there; nothing is made while their entities are unloaded. */
    public static void ensure(ServerLevel l,BlockPos b){ensure(l,b,ACTIVE.get(b.asLong()));}
    /** As above, leaving be whoever the afternoon has sent somewhere. */
    static void ensure(ServerLevel l,BlockPos b,@Nullable Site s){
        for(int who=0;who<PEOPLE;who++){
            if(s!=null&&s.away.contains(who))continue;
            var at=b.offset(AT[who]);
            if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))continue;
            var a=find(l,b,who);
            if(a==null){
                a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
                a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+who);
                stand(a,at,FACING[who]);
                if(!l.addFreshEntity(a))continue;
            }
            stand(a,at,FACING[who]);
        }
    }
    /** At the cell, turned the way given; the keeper on his stool (ActorSeat lowers him onto it and keeps him there). */
    static void stand(LiteraryActor a,BlockPos at,Direction facing){
        float yaw=facing.toYRot();
        // 0.4.674: nobody is left carried, or carrying, from a strike that is over.
        if(a.isPassenger())a.stopRiding();a.ejectPassengers();
        a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.gesture(0);a.setDeltaMovement(Vec3.ZERO);
        a.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);
    }

    /** The car up on the lift, a little above a reader's head, facing out of the bay; a car that nobody keeps telling where it is goes. */
    public static @Nullable TownCar car(ServerLevel l,BlockPos b){
        var at=Vec3.atLowerCornerOf(b).add(FallowSite.CAR_AT);var cell=BlockPos.containing(at);
        if(!l.isLoaded(cell)||!l.areEntitiesLoaded(ChunkPos.asLong(cell)))return null;
        TownCar car=null;
        for(var c:l.getEntitiesOfClass(TownCar.class,new AABB(cell).inflate(3),c->c.isAlive()))car=c;
        if(car==null){
            car=BikeNightRegistry.TOWN_CAR.get().create(l);if(car==null)return null;
            car.kind(TownCar.BLUE);car.place(at,180F);
            if(!l.addFreshEntity(car))return null;
        }
        car.place(at,180F);
        return car;
    }

    /**
     * A word with one of them. Harlan gives a reader without a chore theirs, and under the car says something to do with it; Opal by her bean
     * says what she says to anyone who keeps her company; the man asking the time asks this reader too; otherwise each says their own first
     * line, then a second, then the second again.
     */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!is(a))return;
        var s=site(p,a);int who=a.phase()-FIRST;
        if(FallowStrike.talk(p,a,s,who,own))return;
        if(who==STRANGER){
            if(s!=null&&s.stranger==ASKING&&p.distanceToSqr(a)<ASK_RANGE*ASK_RANGE){askTime(p,s,a,own);return;}
            if(s!=null&&s.stranger<ASKING){a.say(null,FallowTexts.STRANGER_GREETS);a.speaks(FallowTexts.STRANGER_GREETS);}
            return;
        }
        if(who==KEEPER&&s!=null&&s.tick>=CHORES&&!s.chores.containsKey(p.getUUID())){chore(p,s,a,own);return;}
        if(who==KEEPER&&s!=null&&s.keeper==UNDER&&own.contains("Chore")){
            int heard=own.getInt("HeardWork");own.putInt("HeardWork",heard+1);
            String line=FallowTexts.working(own.getString("Chore").equals(TILL),heard);a.say("Harlan",line);a.speaks(line);return;
        }
        if(who==GIRL&&s!=null&&s.bean==SET){
            int heard=Math.min(FallowTexts.BEAN_TALK.length-1,own.getInt("HeardBean"));own.putInt("HeardBean",heard+1);
            String line=FallowTexts.BEAN_TALK[heard];a.say("Opal",line);a.speaks(line);return;
        }
        int heard=own.getInt("Heard"+who);
        String line=FallowTexts.line(who,heard);
        own.putInt("Heard"+who,heard+1);
        a.say(FallowTexts.NAMES[who],line);a.speaks(line);
    }
    /** The afternoon a reader is in, if they are in one. */
    static @Nullable Site site(ServerPlayer p,LiteraryActor a){
        for(var s:ACTIVE.values())if(scene(s.base).contains(a.position()))return s;
        return null;
    }

    /** A hand on something of the station's. True if it was theirs to answer. The note is the first paper: the House's own reading answers it. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        // 0.4.681: below in the buried days, the lantern, the roof, the crock, the shelves and the doors over the steps have their own say.
        var buried=ACTIVE.get(b.asLong());
        if(buried!=null&&FallowBuried.click(p,l,buried,rel,st,own))return true;
        // 0.4.683: a hand on the store's front where a shadow lies.
        if(buried!=null&&FallowRoad.touched(p,buried,rel,own))return true;
        if(FallowBuried.roof(rel,st)){cue(p,FallowTexts.CEILING_BEFORE);return true;}
        if(st.getBlock() instanceof DoorBlock){
            var lower=st.getValue(DoorBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER?rel.below():rel;
            if(lower.equals(FallowSite.TRAILER_DOOR)){cue(p,FallowTexts.TRAILER_DOOR);return true;}
            return false;
        }
        if(!FallowBlock.is(st))return false;
        int stage=st.getValue(FallowBlock.STAGE);var kind=st.getValue(FallowBlock.KIND);
        // The cellar's doors swing back on their hinges for anyone, and shut again; they stay as they are left.
        if(kind==FallowBlock.Kind.CELLAR||kind==FallowBlock.Kind.CELLAR_OPEN){
            // 0.4.674: while Harlan holds them in the four minutes, a hand on them near him takes them from him.
            var site=ACTIVE.get(b.asLong());
            if(site!=null&&FallowStrike.takeDoors(p,l,site,kind==FallowBlock.Kind.CELLAR_OPEN,own))return true;
            boolean open=kind==FallowBlock.Kind.CELLAR;
            cellarDoors(l,b,open);
            l.playSound(null,b.offset(FallowSite.WELL_W,0,FallowSite.WELL_S-1),open?net.minecraft.sounds.SoundEvents.WOODEN_TRAPDOOR_OPEN:net.minecraft.sounds.SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.8F,.7F);
            cue(p,open?FallowTexts.CELLAR_OPENED:FallowTexts.CELLAR_SHUT);
            return true;
        }
        if(kind==FallowBlock.Kind.PUMP&&(stage==0||stage==2))l.playSound(null,b.offset(rel),FallowRegistry.BELL.get(),SoundSource.BLOCKS,.6F,1F);
        // 0.4.673: a reader behind the counter rings up whoever is waiting to pay, or opens the drawer on nobody.
        if(kind==FallowBlock.Kind.COUNTER&&stage==1&&FallowSite.behindCounter(p.position().subtract(Vec3.atLowerCornerOf(b)))){ring(p,l,b,own);return true;}
        String line=FallowStrike.thing(ACTIVE.get(b.asLong()),kind,stage);
        if(line==null)line=FallowTexts.thing(kind,stage);
        if(line==null)return false;
        cue(p,line);
        return true;
    }
    /** The cellar's eight lengths of door, all shut or all thrown back. */
    public static void cellarDoors(ServerLevel l,BlockPos b,boolean open){
        for(int z=FallowSite.WELL_S;z>=FallowSite.WELL_N;z--)for(int x=FallowSite.WELL_W;x<=FallowSite.WELL_E;x++){
            var at=b.offset(x,0,z);var st=l.getBlockState(at);
            if(!FallowBlock.is(st,FallowBlock.Kind.CELLAR)&&!FallowBlock.is(st,FallowBlock.Kind.CELLAR_OPEN))continue;
            l.setBlock(at,FallowBlock.of(open?FallowBlock.Kind.CELLAR_OPEN:FallowBlock.Kind.CELLAR,st.getValue(FallowBlock.FACING),st.getValue(FallowBlock.STAGE)),3);
        }
    }
    public static boolean cellarOpen(ServerLevel l,BlockPos b){return FallowBlock.is(l.getBlockState(b.offset(FallowSite.WELL_W,0,FallowSite.WELL_S)),FallowBlock.Kind.CELLAR_OPEN);}

    // ------------------------------------------------------------------------------------------------ the afternoon
    /** The sites a reader is in or has just left: each one's afternoon, on its own clock. */
    static final Map<Long,Site> ACTIVE=new HashMap<>();
    /** 0.4.767: whether this place has anything going on in memory; a reader who logs back in where it has not arrives again (LiteraryVignettes.returned). */
    static boolean running(BlockPos b){return ACTIVE.containsKey(b.asLong());}
    /** When things happen, in ticks of the afternoon's clock, which only runs while somebody is there. */
    public static final int CHORES=100,KEEPER_GOES=220,BEAN_TIME=480,FAYE_TIME=760,OTIS_TIME=1080,STRANGER_TIME=1400;
    /** How long someone waits at the register before leaving the money; how long the man waits for the time; how near he asks. */
    public static final int WAIT=200,ASK_FOR=160;public static final double ASK_RANGE=10;
    /** Where Harlan works under the car and Opal stands to ask for her bean; where her bean is set down, out front in the sun. */
    public static final Vec3 UNDER_CAR=new Vec3(13.5,0,-4.5),AT_THE_CAR=new Vec3(12.5,0,-3.5),COUNTER_SPOT=new Vec3(-.5,0,-4.5);
    public static final BlockPos BEAN_AT=new BlockPos(4,0,-13);
    /** Where the man comes from, along the road out of the haze, and where he goes, up the far pad's track into the wheat. */
    public static final Vec3 STRANGER_FROM=new Vec3(-50.5,0,-27.5),STRANGER_TO=new Vec3(20.5,0,-99.5);
    /** Harlan's afternoon: on his stool, walking round, under the car; 0.4.674, the four minutes (FallowStrike has him). */
    public static final int STOOL=0,WALKING=1,UNDER=2,STRIKE=3;
    /** The bean's: in the car, Opal going for it, asking, carrying it out, set down; 0.4.674, taken up again in the four minutes. */
    public static final int IN_CAR=0,FETCHING=1,ASKING_BEAN=2,CARRYING=3,SET=4,TAKEN=5;
    /** The man's: not come yet, on his way in, at the cooler, at the register, with Opal, asking the time, going, gone. */
    public static final int NOT_YET=0,COMING=1,CHOOSING=2,PAYING=3,AT_OPAL=4,ASKING=5,GOING=6,GONE=7;
    /** A customer's: walking to what they want, taking it, walking to the register, waiting, done and going back. */
    static final int TO_PICK=0,PICKING=1,TO_PAY=2,WAITING=3,LEAVING=4;
    /** A reader's chore. */
    public static final String TILL="till",GIRL_CHORE="girl";
    static final class Site{
        final BlockPos base;int tick,empty;
        /** 0.4.683: this afternoon's own mark, kept by every reader its light finds (FallowStrike.light). */
        final int id=1+java.util.concurrent.ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE-1);
        /** Whoever the afternoon has sent somewhere, so that nobody puts them back at their place. */
        final Set<Integer> away=new HashSet<>();
        final Map<Integer,List<Vec3>> routes=new HashMap<>();final Map<Integer,Integer> leg=new HashMap<>(),stall=new HashMap<>();
        /** Each reader's chore this afternoon, and when Harlan last gave one. */
        final Map<UUID,String> chores=new HashMap<>();int toldAt=-1000;
        int keeper=STOOL,carTalk,carTalkAt,bean=IN_CAR,beanAt,opalTalk,opalAt=-1000,stranger=NOT_YET,strangerAt;
        /** Who is buying (Faye, Otis, the man), how far along, since when; who has been through. */
        int customer=-1,buying,buyingAt,served;
        /** 0.4.674: how fast each walks, when not their afternoon's pace; the four minutes. */
        final Map<Integer,Double> speed=new HashMap<>();
        final FallowStrike.State strike=new FallowStrike.State();
        Site(BlockPos b){base=b;}
        Vec3 world(Vec3 rel){return Vec3.atLowerCornerOf(base).add(rel);}
    }
    public static final int EMPTY_FORGET=100;
    /** Everyone in the scene, store, yard, cellar, road and wheat alike. */
    public static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players()){
            if(p.isSpectator()||!p.isAlive())continue;
            if(FallowSite.inScene(p.position().subtract(Vec3.atLowerCornerOf(b))))out.add(p);
        }
        return out;
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){ACTIVE.clear();}
    public static void clearAll(){ACTIVE.clear();}
    /** For tests: the afternoon at this base, if one is running. */
    public static @Nullable Site afternoon(BlockPos b){return ACTIVE.get(b.asLong());}
    public static int clock(BlockPos b){var s=ACTIVE.get(b.asLong());return s==null?-1:s.tick;}
    /** 0.4.683: the afternoon's own mark (Site.id), 0 when there is none. */
    public static int mark(BlockPos b){var s=ACTIVE.get(b.asLong());return s==null?0:s.id;}
    /** For tests: set the afternoon's clock on, as if that much of it had gone by. */
    public static void skipTo(BlockPos b,int tick){var s=ACTIVE.get(b.asLong());if(s!=null)s.tick=Math.max(s.tick,tick);}
    public static int keeper(BlockPos b){var s=ACTIVE.get(b.asLong());return s==null?-1:s.keeper;}
    public static int bean(BlockPos b){var s=ACTIVE.get(b.asLong());return s==null?-1:s.bean;}
    public static int stranger(BlockPos b){var s=ACTIVE.get(b.asLong());return s==null?-1:s.stranger;}
    public static int customer(BlockPos b){var s=ACTIVE.get(b.asLong());return s==null||s.buying!=WAITING?-1:s.customer;}
    public static int served(BlockPos b){var s=ACTIVE.get(b.asLong());return s==null?0:s.served;}
    /** For tests: the afternoon at this base forgotten, as when the place has stood empty. */
    public static void forget(BlockPos b){ACTIVE.remove(b.asLong());}
    /** For tests: Faye and Otis taken as having paid already, so that the man comes. */
    public static void servedAlready(BlockPos b){var s=ACTIVE.get(b.asLong());if(s!=null)s.served|=3;}
    /** For tests: Faye taken as having paid already, so that Otis is next. */
    public static void fayeServed(BlockPos b){var s=ACTIVE.get(b.asLong());if(s!=null)s.served|=1;}
    /** For tests: what a reader's chore is this afternoon, or null. */
    public static @Nullable String chore(BlockPos b,UUID reader){var s=ACTIVE.get(b.asLong());return s==null?null:s.chores.get(reader);}

    /** A new afternoon: the bean back in the car, the man not yet come. */
    static void afresh(ServerLevel l,BlockPos b){
        var at=b.offset(BEAN_AT);
        if(l.isLoaded(at)&&FallowBlock.is(l.getBlockState(at),FallowBlock.Kind.BEAN))l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
        var man=find(l,b,STRANGER);if(man!=null)man.discard();
        FallowStrike.afresh(l,b);
    }

    /**
     * Every tick while someone is there, the afternoon goes on: Harlan gives chores and goes under the car, Opal fetches her bean, Faye, Otis
     * and the man come to the register in turn, the man asks Opal her name and a reader the time and walks off into the wheat. Under it, as
     * before, the wind, the insects and the radio. When the place has stood empty five seconds, the afternoon is over and begins again.
     */
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(ACTIVE.isEmpty())return;
        var l=e.getServer().getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var it=ACTIVE.values().iterator();it.hasNext();){
            var s=it.next();var here=readers(l,s.base);
            if(here.isEmpty()){
                // 0.4.681: a station buried with readers' days still running is kept a while, for a reader below who has stepped out of the game.
                // 0.4.683: and a station held for whoever the light found out in it and has not come back (FallowRoad.RETURN_HOLD).
                int keep=EMPTY_FORGET;if(FallowBuried.holding(s))keep=Math.max(keep,FallowBuried.GRACE);if(FallowRoad.waiting(s))keep=Math.max(keep,FallowRoad.RETURN_HOLD);
                if(++s.empty>=keep){var c=car(l,s.base);if(c!=null)c.discard();afresh(l,s.base);it.remove();}
                continue;
            }
            s.empty=0;s.tick++;
            if(s.tick%20==0){FallowRuin.leftover(l,s,here);if(s.strike.toneAt<0)FallowRoad.afresh(l,s.base,here);FallowAsh.leftover(l,s,l.players().stream().filter(p->FallowSite.inScene(FallowStrike.rel(s,p))).toList());car(l,s.base);ensure(l,s.base,s);}
            afternoon(l,s,here);
            FallowStrike.tick(l,s,here);
            var rnd=l.getRandom();var one=here.get(rnd.nextInt(here.size()));
            // 0.4.682: after the light, nothing in the field to rustle and nothing alive in it to hum: only the wind over the ash, heard
            // by whoever is up in it and not by anyone below.
            boolean after=s.strike.toneAt>=0&&s.strike.since(s.tick)>=FallowStrike.LIGHT;
            if(s.tick%220==40){
                if(!after)play(l,one.blockPosition().offset(rnd.nextInt(17)-8,1,rnd.nextInt(17)-8),FallowRegistry.WIND.get(),.55F);
                else if(!FallowSite.inCellar(one.position().subtract(Vec3.atLowerCornerOf(s.base))))play(l,one.blockPosition().offset(rnd.nextInt(17)-8,1,rnd.nextInt(17)-8),FallowRegistry.ASH_WIND.get(),.5F);
            }
            if(s.tick%260==130&&!after){
                var r=one.position().subtract(Vec3.atLowerCornerOf(s.base));
                if(!FallowSite.inStore(r)&&!FallowSite.inCellar(r))play(l,one.blockPosition().offset(rnd.nextInt(9)-4,0,rnd.nextInt(9)-4),FallowRegistry.INSECTS.get(),.5F);
            }
            if(s.tick%180==60&&s.strike.toneAt<0&&here.stream().anyMatch(p->FallowSite.inStore(p.position().subtract(Vec3.atLowerCornerOf(s.base)))))
                play(l,s.base.offset(FallowSite.RADIO),FallowRegistry.RADIO.get(),.4F);
        }
    }
    static void play(ServerLevel l,BlockPos at,SoundEvent e,float volume){l.playSound(null,at,e,SoundSource.AMBIENT,volume,.95F+l.getRandom().nextFloat()*.1F);}

    static void afternoon(ServerLevel l,Site s,List<ServerPlayer> here){
        walk(l,s,here);
        if(s.tick%20==0)chores(l,s,here);
        keeper(l,s,here);
        bean(l,s,here);
        // 0.4.674: nobody comes in to buy anything once the tone is on.
        if(s.strike.toneAt<0)customers(l,s,here);
        stranger(l,s,here);
    }

    // ------------------------------------------------------------------------------------------------ the chores
    /** Harlan gives a chore to each reader near enough to hear who has none, one at a time, from wherever he is. */
    static void chores(ServerLevel l,Site s,List<ServerPlayer> here){
        if(s.tick<CHORES||s.tick-s.toldAt<70||s.keeper==WALKING||s.keeper==STRIKE)return;
        var a=find(l,s.base,KEEPER);if(a==null)return;
        for(var p:here){
            if(s.chores.containsKey(p.getUUID())||p.distanceToSqr(a)>ASK_RANGE*ASK_RANGE||!a.hasLineOfSight(p))continue;
            chore(p,s,a,own(p));return;
        }
    }
    /**
     * A reader's chore, by the House's read of their age: one read as young keeps Opal company, anyone else (unread too) minds the till. A
     * second reader gets whichever the first has not got, so that both are seen to; past two, it is the read again.
     */
    static void chore(ServerPlayer p,Site s,LiteraryActor a,CompoundTag own){
        String wanted=HouseRead.category(p,HouseRead.Trait.AGE).map(c->c.value().equals("DIGITAL")).orElse(false)?GIRL_CHORE:TILL;
        String other=wanted.equals(TILL)?GIRL_CHORE:TILL;
        boolean wantedTaken=false,otherTaken=false;
        for(var e:s.chores.entrySet()){if(e.getValue().equals(wanted))wantedTaken=true;else otherTaken=true;}
        String chore=wantedTaken&&!otherTaken?other:wanted;
        s.chores.put(p.getUUID(),chore);s.toldAt=s.tick;
        own.putString("Chore",chore);own.putInt("Chores",own.getInt("Chores")+1);save(p,own);
        String[] lines=chore.equals(TILL)?FallowTexts.TILL:FallowTexts.COMPANY;
        String line=HouseRead.address(p,lines[0],lines[1],lines[2],lines[3]);
        a.sayTo(p,"Harlan",line);a.speaks(line);
    }

    // ------------------------------------------------------------------------------------------------ Harlan under the car
    static final List<Vec3> KEEPER_ROUTE=List.of(new Vec3(-.5,0,-2.5),new Vec3(6.5,0,-2.5),new Vec3(7.5,0,-3.5),new Vec3(7.5,0,-4.5),new Vec3(8.5,0,-4.5),
        new Vec3(9.5,0,-4.5),UNDER_CAR);
    static void keeper(ServerLevel l,Site s,List<ServerPlayer> here){
        var a=find(l,s.base,KEEPER);if(a==null)return;
        switch(s.keeper){
            case STOOL->{
                // Once he has given out the chores (or given up waiting for anyone to come near enough to hear one), and his last word
                // is said, he gets down off his stool and goes round to the car.
                boolean told=here.stream().allMatch(p->s.chores.containsKey(p.getUUID()));
                if(s.tick>=KEEPER_GOES&&(told||s.tick>=KEEPER_GOES+240)&&s.tick-s.toldAt>=60){s.keeper=WALKING;s.away.add(KEEPER);send(s,KEEPER,KEEPER_ROUTE);}
            }
            case WALKING->{if(!busy(s,KEEPER)){s.keeper=UNDER;s.carTalk=0;s.carTalkAt=s.tick+40;work(a);}}
            case UNDER->{
                // Under the car, the wrench going now and then, and he and Boyd have a word.
                if(s.bean!=ASKING_BEAN&&s.tick%90==0)l.playSound(null,BlockPos.containing(s.world(UNDER_CAR)).above(2),FallowRegistry.WRENCH.get(),SoundSource.NEUTRAL,.55F,.95F+l.getRandom().nextFloat()*.1F);
                if(s.carTalk<FallowTexts.AT_THE_CAR.length&&s.tick>=s.carTalkAt){
                    var said=FallowTexts.AT_THE_CAR[s.carTalk++];var who=said[0].equals("Harlan")?a:find(l,s.base,MAN);
                    if(who!=null){who.say(said[0],said[1]);who.speaks(said[1]);}
                    s.carTalkAt=s.tick+90;
                }
            }
            default->{}
        }
    }
    /** At the car, his arms up into it. */
    static void work(LiteraryActor a){face(a,Direction.NORTH.toYRot());a.gesture(WORK);a.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(CrossingRegistry.WRENCH.get()));}

    // ------------------------------------------------------------------------------------------------ the bean
    static final List<Vec3> TO_THE_CAR=List.of(new Vec3(12.5,0,-12.5),new Vec3(12.5,0,-8.5),AT_THE_CAR),
        FROM_THE_CAR=List.of(new Vec3(12.5,0,-8.5),new Vec3(12.5,0,-12.5),new Vec3(3.5,0,-12.5));
    static void bean(ServerLevel l,Site s,List<ServerPlayer> here){
        var girl=find(l,s.base,GIRL);var keeper=find(l,s.base,KEEPER);if(girl==null||keeper==null)return;
        switch(s.bean){
            case IN_CAR->{if(s.tick>=BEAN_TIME&&s.keeper==UNDER&&s.carTalk>=FallowTexts.AT_THE_CAR.length){s.bean=FETCHING;s.away.add(GIRL);girl.gesture(0);send(s,GIRL,TO_THE_CAR);}}
            case FETCHING->{if(!busy(s,GIRL)){s.bean=ASKING_BEAN;s.beanAt=s.tick;faceTo(girl,keeper.position());girl.say("Opal",FallowTexts.ASK_BEAN);girl.speaks(FallowTexts.ASK_BEAN);}}
            case ASKING_BEAN->{
                int t=s.tick-s.beanAt;
                // He reaches up into the car for it, turns, and hands it down to her; she takes it in both hands.
                if(t==40){keeper.gesture(REACH);keeper.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(FallowRegistry.BEAN_CUP.get()));}
                if(t==60){faceTo(keeper,girl.position());keeper.say("Harlan",FallowTexts.HAND_BEAN);keeper.speaks(FallowTexts.HAND_BEAN);}
                if(t==80){keeper.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);girl.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(FallowRegistry.BEAN_CUP.get()));girl.gesture(CUP);}
                if(t==100)work(keeper);
                if(t>=110){s.bean=CARRYING;send(s,GIRL,FROM_THE_CAR);}
            }
            case CARRYING->{
                if(busy(s,GIRL))return;
                // Out front in the sun, she kneels and sets it down beside her.
                face(girl,Direction.EAST.toYRot());girl.gesture(KNEEL);girl.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);
                var at=s.base.offset(BEAN_AT);if(l.getBlockState(at).isAir())l.setBlock(at,FallowBlock.of(FallowBlock.Kind.BEAN,Direction.WEST,0),3);
                s.bean=SET;s.opalAt=s.tick;
            }
            case SET->company(l,s,girl,here);
            default->{}
        }
    }
    /** Whoever stays by Opal keeps her company: she talks to them, a line every seven seconds, and each reader near her has the time kept. */
    static void company(ServerLevel l,Site s,LiteraryActor girl,List<ServerPlayer> here){
        if(s.tick%20!=0)return;
        boolean near=false;
        for(var p:here){
            if(p.distanceToSqr(girl)>4.5*4.5)continue;
            near=true;var own=own(p);own.putInt("Kept",own.getInt("Kept")+20);save(p,own);
        }
        if(near&&s.opalTalk<FallowTexts.BEAN_TALK.length&&s.tick-s.opalAt>=140&&s.stranger!=AT_OPAL){
            String line=FallowTexts.BEAN_TALK[s.opalTalk++];girl.say("Opal",line);girl.speaks(line);s.opalAt=s.tick;
        }
    }

    // ------------------------------------------------------------------------------------------------ at the register
    /** Each customer's way to what they buy, how they face it, how they take it, what they carry, and the way back to their place. */
    static List<Vec3> toPick(int who){
        return switch(who){
            case WOMAN->List.of(new Vec3(.5,0,-4.5),new Vec3(-4.5,0,-4.5),new Vec3(-5.5,0,-6.5));
            case DRIVER->List.of(new Vec3(7.5,0,-14.5),new Vec3(.5,0,-12.5),new Vec3(.5,0,-10.5),new Vec3(6.5,0,-10.5));
            default->List.of(new Vec3(-8.5,0,-27.5),new Vec3(-4.5,0,-23.5),new Vec3(-4.5,0,-14.5),new Vec3(.5,0,-12.5),new Vec3(.5,0,-10.5),
                new Vec3(-1.5,0,-10.5),new Vec3(-4.5,0,-9.5),new Vec3(-5.5,0,-7.5));
        };
    }
    static List<Vec3> toPay(int who){
        return switch(who){
            case WOMAN->List.of(new Vec3(-4.5,0,-4.5),COUNTER_SPOT);
            case DRIVER->List.of(new Vec3(2.5,0,-6.5),COUNTER_SPOT);
            default->List.of(new Vec3(-4.5,0,-4.5),COUNTER_SPOT);
        };
    }
    static List<Vec3> home(int who){
        return switch(who){
            case WOMAN->List.of(new Vec3(5.5,0,-8.5));
            case DRIVER->List.of(new Vec3(.5,0,-10.5),new Vec3(.5,0,-12.5),new Vec3(7.5,0,-14.5),new Vec3(12.5,0,-19.5));
            default->List.of(new Vec3(.5,0,-10.5),new Vec3(.5,0,-12.5),new Vec3(3.5,0,-14.5));
        };
    }
    /** The cooler for milk and pop, the machine for coffee. */
    static Vec3 pickFrom(int who){return who==DRIVER?new Vec3(7.5,0,-8.5):new Vec3(-6.5,0,who==WOMAN?-6.5:-7.5);}
    static net.minecraft.world.item.Item carries(int who){return who==WOMAN?FallowRegistry.MILK.get():who==DRIVER?FallowRegistry.COFFEE.get():FallowRegistry.SODA.get();}
    static int bit(int who){return who==WOMAN?1:who==DRIVER?2:4;}
    static void customers(ServerLevel l,Site s,List<ServerPlayer> here){
        if(s.customer<0){
            if((s.served&1)==0&&s.tick>=FAYE_TIME)begin(s,WOMAN);
            else if((s.served&2)==0&&s.tick>=OTIS_TIME)begin(s,DRIVER);
            return;
        }
        var a=find(l,s.base,s.customer);if(a==null)return;
        int who=s.customer;
        // 0.4.693 (run 1718): a customer a reader is talking to finishes answering before going on with the purchase. Turned to the machine,
        // the counter or his place while his answer was still up, he looked away from the reader he was answering.
        if((a.addressee()!=null||a.saying())&&s.strike.toneAt<0)return;
        switch(s.buying){
            case TO_PICK->{if(!busy(s,who)){faceTo(a,s.world(pickFrom(who)));a.gesture(who==DRIVER?POUR:REACH);s.buying=PICKING;s.buyingAt=s.tick;}}
            case PICKING->{
                if(s.tick-s.buyingAt==20)a.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(carries(who)));
                if(s.tick-s.buyingAt>=(who==DRIVER?50:30)){a.gesture(0);s.buying=TO_PAY;send(s,who,toPay(who));}
            }
            case TO_PAY->{
                if(busy(s,who))return;
                face(a,Direction.SOUTH.toYRot());s.buying=WAITING;s.buyingAt=s.tick;
                String line=FallowTexts.CUSTOMER[index(who)][0];a.say(name(who),line);a.speaks(line);
            }
            case WAITING->{
                if(s.tick-s.buyingAt<WAIT)return;
                // Nobody behind the counter to ring it up: the money is left by the register.
                String line=FallowTexts.CUSTOMER[index(who)][2];a.say(name(who),line);a.speaks(line);
                l.playSound(null,s.base.offset(FallowSite.ENDING).above(),FallowRegistry.COINS.get(),SoundSource.NEUTRAL,.7F,1F);
                for(var p:here)if(FallowSite.inStore(p.position().subtract(Vec3.atLowerCornerOf(s.base))))cue(p,FallowTexts.COINS_LEFT);
                paid(s,who);
            }
            case LEAVING->{
                if(busy(s,who))return;
                if(who!=STRANGER){face(a,FACING[who].toYRot());s.away.remove(who);}
                s.customer=-1;
            }
            default->{}
        }
    }
    static void begin(Site s,int who){s.customer=who;s.buying=TO_PICK;s.away.add(who);send(s,who,toPick(who));}
    static void paid(Site s,int who){s.served|=bit(who);s.buying=LEAVING;if(who==STRANGER){s.stranger=AT_OPAL;s.strangerAt=-1;}send(s,who,home(who));}
    static int index(int who){return who==WOMAN?0:who==DRIVER?1:2;}
    static @Nullable String name(int who){return who==STRANGER?null:FallowTexts.NAMES[who];}
    /** A reader behind the counter at the register: whoever is waiting is rung up and thanks them; otherwise the drawer opens on nobody. */
    static void ring(ServerPlayer p,ServerLevel l,BlockPos b,CompoundTag own){
        l.playSound(null,b.offset(FallowSite.ENDING).above(),FallowRegistry.REGISTER.get(),SoundSource.BLOCKS,.8F,1F);
        var s=ACTIVE.get(b.asLong());
        if(s==null||s.buying!=WAITING||s.customer<0){cue(p,FallowTexts.RING_NOTHING);return;}
        var a=find(l,b,s.customer);
        if(a!=null){String line=FallowTexts.CUSTOMER[index(s.customer)][1];a.sayTo(p,name(s.customer),line);a.speaks(line);}
        cue(p,FallowTexts.RING);
        own.putInt("Rang",own.getInt("Rang")+1);own.putInt("Rang"+index(s.customer),1);
        paid(s,s.customer);
    }

    // ------------------------------------------------------------------------------------------------ the man on the road
    static final List<Vec3> INTO_THE_WHEAT=List.of(new Vec3(7.5,0,-14.5),new Vec3(7.5,0,-26.5),new Vec3(20.5,0,-37.5),new Vec3(20.5,0,-60.5),STRANGER_TO);
    static void stranger(ServerLevel l,Site s,List<ServerPlayer> here){
        switch(s.stranger){
            case NOT_YET->{
                // He comes after the others have paid, along the road out of the haze, made only where nobody sees him come to be.
                if(s.tick<STRANGER_TIME||s.customer>=0||(s.served&3)!=3)return;
                var at=s.world(STRANGER_FROM);var cell=BlockPos.containing(at);
                if(!l.isLoaded(cell)||!l.areEntitiesLoaded(ChunkPos.asLong(cell))||Drawing.seen(here,at))return;
                var old=find(l,s.base,STRANGER);if(old!=null)old.discard();
                var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
                a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+STRANGER);
                a.moveTo(at.x,at.y,at.z,-90,0);
                if(!l.addFreshEntity(a))return;
                s.stranger=COMING;s.customer=STRANGER;s.buying=TO_PICK;send(s,STRANGER,toPick(STRANGER));
            }
            case AT_OPAL->{
                var a=find(l,s.base,STRANGER);var girl=find(l,s.base,GIRL);if(a==null||girl==null)return;
                if(s.strangerAt<0){if(busy(s,STRANGER))return;s.strangerAt=s.tick;faceTo(a,girl.position());a.say(null,FallowTexts.ASKS_NAME);a.speaks(FallowTexts.ASKS_NAME);return;}
                // She keeps her eyes on her bean and does not tell him.
                if(s.tick-s.strangerAt==80){a.say(null,FallowTexts.NAMES_KEEP);a.speaks(FallowTexts.NAMES_KEEP);}
                if(s.tick-s.strangerAt>=150){s.stranger=ASKING;s.strangerAt=s.tick;askAll(l,s,a,here);}
            }
            case ASKING->{
                var a=find(l,s.base,STRANGER);if(a==null)return;
                if(s.tick-s.strangerAt>=ASK_FOR){
                    a.say(null,FallowTexts.UNANSWERED);a.speaks(FallowTexts.UNANSWERED);
                    for(var p:l.players())if(Talk.waiting(p,TIME_TALK))Talk.close(p);
                    going(s);
                }
            }
            case GOING->{
                var a=find(l,s.base,STRANGER);if(a==null){s.stranger=GONE;return;}
                // Out at the end of the track, the first moment nobody sees him, he is gone.
                if(!busy(s,STRANGER)&&!Drawing.seen(here,a.position())){a.discard();s.stranger=GONE;}
            }
            default->{}
        }
    }
    static final String TIME_TALK="fallow_time";
    /** He turns to the nearest reader who can see him and asks the time; every reader near enough may answer. */
    static void askAll(ServerLevel l,Site s,LiteraryActor a,List<ServerPlayer> here){
        ServerPlayer nearest=null;double best=ASK_RANGE*ASK_RANGE;
        for(var p:here){
            double d=p.distanceToSqr(a);if(d>ASK_RANGE*ASK_RANGE||!a.hasLineOfSight(p))continue;
            if(d<best){best=d;nearest=p;}
            var own=own(p);askTime(p,s,a,own);save(p,own);
        }
        if(nearest!=null)a.sayTo(nearest,null,FallowTexts.ASKS_TIME);else a.say(null,FallowTexts.ASKS_TIME);
        a.speaks(FallowTexts.ASKS_TIME);
    }
    static void askTime(ServerPlayer p,Site s,LiteraryActor a,CompoundTag own){
        own.putBoolean("Asked",true);
        Talk.begin(p,TIME_TALK,FallowTexts.STRANGER_LABEL);Talk.line(p,FallowTexts.STRANGER_LABEL,FallowTexts.ASKS_TIME);
        Talk.ask(p,Talk.reply(FallowTexts.TIME_ANSWER,q->answered(q,s,a)),Talk.act(FallowTexts.SAY_NOTHING,q->{var o=own(q);o.putBoolean("Silent",true);save(q,o);Talk.close(q);}));
    }
    /** A reader tells him the time: he thanks nobody, says what he says, and goes. */
    static void answered(ServerPlayer p,Site s,LiteraryActor a){
        var own=own(p);own.putBoolean("Answered",true);save(p,own);
        if(s.stranger!=ASKING)return;
        Talk.line(p,FallowTexts.STRANGER_LABEL,FallowTexts.ANSWERED);Talk.end(p);
        a.sayTo(p,null,FallowTexts.ANSWERED);a.speaks(FallowTexts.ANSWERED);
        for(var q:p.serverLevel().players())if(q!=p&&Talk.waiting(q,TIME_TALK))Talk.close(q);
        going(s);
    }
    static void going(Site s){s.stranger=GOING;send(s,STRANGER,INTO_THE_WHEAT);}

    // ------------------------------------------------------------------------------------------------ walking
    static final int STALL=60;
    static void send(Site s,int who,List<Vec3> route){s.routes.put(who,new ArrayList<>(route));s.leg.put(who,0);s.stall.remove(who);}
    static boolean busy(Site s,int who){return s.routes.containsKey(who);}
    /** Everyone walking goes on toward their next point, stopping to answer a reader who speaks to them, opening a shut door on the way. */
    static void walk(ServerLevel l,Site s,List<ServerPlayer> here){
        for(var who:List.copyOf(s.routes.keySet())){
            var a=find(l,s.base,who);if(a==null){s.routes.remove(who);continue;}
            var route=s.routes.get(who);int i=s.leg.get(who);
            if(i>=route.size()){s.routes.remove(who);s.leg.remove(who);s.stall.remove(who);continue;}
            if(a.listening()&&who!=STRANGER&&s.strike.toneAt<0)continue;
            var target=s.world(route.get(i));var before=a.position();
            var down=FallowStrike.steps(l,s,a,target,s.speed.getOrDefault(who,who==GIRL?.09:.11));
            if(down!=null){if(down){s.leg.put(who,i+1);s.stall.remove(who);}continue;}
            doors(l,s,a);
            if(SceneFootpaths.walk(a,target,s.speed.getOrDefault(who,who==GIRL?.09:.11))||a.position().distanceToSqr(target)<.09){s.leg.put(who,i+1);s.stall.remove(who);continue;}
            // Caught on something (or a reader in the way) three seconds: where nobody sees, they are simply at their next point.
            int still=a.position().distanceToSqr(before)<1.0E-4?s.stall.getOrDefault(who,0)+1:0;s.stall.put(who,still);
            if(still>=STALL&&!Drawing.seen(here,a.position())&&!Drawing.seen(here,target)){a.moveTo(target.x,target.y,target.z,a.getYRot(),0);s.leg.put(who,i+1);s.stall.remove(who);}
        }
    }
    /** The store's front door and the garage's are opened by whoever comes to them shut, and left open in the heat. */
    static void doors(ServerLevel l,Site s,LiteraryActor a){
        for(var rel:new BlockPos[]{FallowSite.FRONT_DOOR,FallowSite.GARAGE_DOOR}){
            var at=s.base.offset(rel);if(a.position().distanceToSqr(Vec3.atBottomCenterOf(at))>2.6*2.6)continue;
            var st=l.getBlockState(at);
            if(st.getBlock() instanceof DoorBlock door&&!st.getValue(DoorBlock.OPEN))door.setOpen(null,l,st,at,true);
        }
    }
    static void face(LiteraryActor a,float yaw){a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    static void faceTo(LiteraryActor a,Vec3 at){var d=at.subtract(a.position());face(a,(float)Math.toDegrees(Math.atan2(-d.x,d.z)));}
    /** 0.4.674: a reader at a station whose lids have opened leaves only by the light (LiteraryVignettes.retreatLocked). */
    public static boolean locked(ServerPlayer p){
        if(!LiteraryVignettes.inside(p,PLACE))return false;
        var b=LiteraryVignettes.base(p,PLACE);return b!=null&&FallowStrike.locked(ACTIVE.get(b.asLong()),p);
    }
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
}
