package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.CornerPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.408, The House on the Corner (docs/THE_HOUSE_ON_THE_CORNER_0_4_408.md): a city street at siesta, an empty house on the corner, and
 * three children on the kerb across from it. The girl has one arm, and a different story about the other every time she is asked. She
 * loves the house.
 *
 * <p>One day at a time for everyone on the street, on one clock. The first day is the dare: the children take the reader through the gate
 * into the front room, clean and lit though the power has been off for years, with jars on the boards along the wall; the door at the
 * back will not open, and they go home. On the second day it opens onto a corridor longer than the house; she holds out her hand and
 * walks to the last door, and says not today. On the third her brother will not go in. She walks to the last door again, lets go, goes
 * in, and the door shuts. A reader who opens it finds the room empty and smaller than its door; a reader who waits outside sees the front
 * door open onto the street with nobody in it. Either is the end of it, and the garden has grown over the path. After that the street is
 * dark whenever that reader comes back, the house's windows are lit, her brother is older on the kerb, and there is one jar more.
 *
 * <p>The day the street has is the furthest any unfinished reader there has reached, so friends go on together; a reader's day counts
 * only if they were there for it. She walks with one reader at a time, the one holding her hand. Each reader's last room is their own.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Corner {
    private Corner(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.CORNER_HOUSE;
    /** The girl, her brother, their friend, and her brother older; stranger phases one hundred and thirty-one to one hundred and thirty-four. */
    public static final int ROMINA=131,NACHO=132,LULI=133,NACHO_GROWN=134;
    static final String[] NAMES={"Romina","Nacho","Luli","Nacho"};
    static final String[] SKINS={"romina","nacho","luli","nacho_grown"};
    public static boolean cast(int phase){return phase>=ROMINA&&phase<=NACHO_GROWN;}
    public static String skin(int phase){return "corner_"+SKINS[phase-ROMINA];}
    public static boolean child(int phase){return phase>=ROMINA&&phase<=LULI;}
    /** She has one arm. */
    public static boolean oneArm(int phase){return phase==ROMINA;}
    /** The days: the dare, the corridor, the last room, and every visit after. */
    public static final int DARE=1,CORRIDOR=2,LAST=3,AFTER=4;
    /** A day's stages. */
    public static final int WAITING=0,GOING=1,INSIDE=2,WALKING=3,AT_DOOR=4,GONE=5,HOME=6,OVER=7,NIGHT=8;
    /** Ticks: how long they wait on the kerb before going over anyway; the front room's beats; how long she holds her hand out alone; how
     * long a reader waits outside before the front door opens; an empty street's reset. */
    public static final int KERB_WAIT=600,ROOM_BEAT=200,HAND_WAIT=300,WAIT=400,EMPTY_RESET=200;
    /** Poses: her hand held out; her hand in someone's; her hand on the last door. */
    public static final int HAND_OUT=6,HOLDING=7,TOUCH=8;
    public static final String TALK="the_house_on_the_corner";
    static final Vec3 ROMINA_ROOM=new Vec3(14.5,0,-81.5),NACHO_ROOM=new Vec3(17.5,0,-79.5),LULI_ROOM=new Vec3(20.5,0,-80.5),
        HALL_MOUTH=new Vec3(18.5,0,-86.5),LAST_FRONT=new Vec3(18.5,0,-118.5),IN_DOOR=new Vec3(18.5,0,-119.5),
        BY_GATE=new Vec3(18.5,0,-67.5),IN_GARDEN=new Vec3(18.5,0,-72.5),AT_FRONT=new Vec3(18.5,0,-76.5),ENTER=new Vec3(18.5,0,-79.5),BY_BACK=new Vec3(18.5,0,-83.5),
        NACHO_GATE=new Vec3(16.5,0,-67.5),LULI_GATE=new Vec3(21.5,0,-67.5),AWAY=new Vec3(-30.5,0,-62.5);

    static final class Day {
        final BlockPos base;int day=DARE,stage=WAITING,since,empty,after;long tick,id;boolean fresh=true,backOpen,handOut;
        final UUID[] cast=new UUID[4];
        @Nullable UUID holder;
        final Map<Integer,List<Vec3>> routes=new HashMap<>();final Map<Integer,Integer> leg=new HashMap<>();
        final Map<UUID,Long> said=new HashMap<>();
        Day(BlockPos b){base=b;}
    }
    private static final Map<Long,Day> DAYS=new HashMap<>();
    /** The hour last sent to each reader. */
    private static final Map<UUID,Long> SENT=new HashMap<>();
    public static void clearAll(){DAYS.clear();SENT.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){SENT.remove(e.getEntity().getUUID());}

    // ------------------------------------------------------------------------------------------------ for tests
    @Nullable static Day day(BlockPos b){return DAYS.get(b.asLong());}
    public static int stage(BlockPos b){var d=day(b);return d==null?-1:d.stage;}
    public static int dayOf(BlockPos b){var d=day(b);return d==null?0:d.day;}
    @Nullable public static UUID holder(BlockPos b){var d=day(b);return d==null?null:d.holder;}
    @Nullable public static LiteraryActor actor(ServerLevel l,BlockPos b,int phase){var d=day(b);return d==null?null:actor(l,d.cast[phase-ROMINA]);}
    public static void skip(BlockPos b,int since){var d=day(b);if(d!=null)d.since=Math.max(d.since,since);}

    // ------------------------------------------------------------------------------------------------ records and speech
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void refresh(CompoundTag held,CompoundTag fresh){for(var key:List.copyOf(held.getAllKeys()))held.remove(key);held.merge(fresh);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void sound(ServerLevel l,BlockPos at,SoundEvent e,float volume,float pitch){l.playSound(null,at,e,SoundSource.NEUTRAL,volume,pitch);}
    /** One of them says something: over their head, and in the action bar for readers near enough to hear it. */
    static void speak(ServerLevel l,Day d,int who,String line){
        var a=actor(l,d.cast[who]);if(a==null)return;a.say(line);a.speaks(line);d.said.put(a.getUUID(),d.tick);
        for(var r:readers(l,d.base))if(r.distanceToSqr(a)<20*20)r.displayClientMessage(Component.literal(NAMES[who]+": “"+line+"”").withStyle(ChatFormatting.GRAY),true);
    }
    static void everyone(ServerLevel l,Day d,String line){for(var r:readers(l,d.base))cue(r,line);}
    static void witnessed(ServerLevel l,Day d,java.util.function.BiConsumer<ServerPlayer,CompoundTag> fact){for(var r:readers(l,d.base)){var own=own(r);fact.accept(r,own);save(r,own);}}
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }

    // ------------------------------------------------------------------------------------------------ the children
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    @Nullable static LiteraryActor spawn(ServerLevel l,Day d,int who,Vec3 at,float yaw){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,ROMINA+who);place(a,d.base,at,yaw);
        if(!l.addFreshEntity(a))return null;d.cast[who]=a.getUUID();return a;
    }
    static void place(LiteraryActor a,BlockPos b,Vec3 r,float yaw){a.moveTo(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.setDeltaMovement(Vec3.ZERO);}
    static AABB area(BlockPos b){return new AABB(b.offset(CornerSite.WEST,-4,CornerSite.NORTH)).minmax(new AABB(b.offset(CornerSite.EAST,16,2)));}
    static void clearActors(ServerLevel l,BlockPos b){for(var a:l.getEntitiesOfClass(LiteraryActor.class,area(b),a->a.role()==LiteraryActor.STRANGER&&cast(a.phase())))a.discard();}
    static boolean loaded(ServerLevel l,Day d){var at=d.base.offset(CornerSite.GATE_W);return l.isLoaded(at)&&l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at));}
    static void send(Day d,int who,Vec3... points){d.routes.put(who,new ArrayList<>(List.of(points)));d.leg.put(who,0);}
    static boolean busy(Day d,int who){return d.routes.containsKey(who);}
    static void walk(ServerLevel l,Day d){
        for(var who:List.copyOf(d.routes.keySet())){
            var a=actor(l,d.cast[who]);if(a==null){d.routes.remove(who);continue;}
            var route=d.routes.get(who);int i=d.leg.get(who);
            if(i>=route.size()){d.routes.remove(who);d.leg.remove(who);continue;}
            var target=world(d.base,route.get(i));
            if(SceneFootpaths.walk(a,target,who==0&&d.stage==WALKING?pace(l,d):.11)||a.position().distanceToSqr(target)<.09)d.leg.put(who,i+1);
        }
    }
    /** Down the corridor she walks slowly, waits for whoever holds her hand, and slower still alone. */
    static double pace(ServerLevel l,Day d){var h=holder(l,d);var a=actor(l,d.cast[0]);if(h!=null&&a!=null&&h.distanceTo(a)>4)return 0;return h!=null?.065:.05;}
    @Nullable static ServerPlayer holder(ServerLevel l,Day d){if(d.holder==null)return null;var p=l.getServer().getPlayerList().getPlayer(d.holder);return p!=null&&readers(l,d.base).contains(p)?p:null;}
    /** Heads turn to whoever is nearest, and to the house while they wait for it. */
    static void look(ServerLevel l,Day d){
        var here=readers(l,d.base);
        for(int who=0;who<4;who++){var a=actor(l,d.cast[who]);if(a==null||busy(d,who))continue;
            if(who==0&&d.stage==WAITING){SceneFootpaths.face(a,world(d.base,new Vec3(18.5,2,-80)),60);continue;}
            ServerPlayer near=null;double best=64;for(var r:here){double dd=r.distanceToSqr(a);if(dd<best){best=dd;near=r;}}
            if(near!=null)SceneFootpaths.face(a,near.getEyePosition(),70);
            var t=d.said.get(a.getUUID());if(t!=null&&d.tick-t>90){a.setCustomNameVisible(false);a.silent();d.said.remove(a.getUUID());}
        }
    }

    // ------------------------------------------------------------------------------------------------ arriving
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;var d=DAYS.computeIfAbsent(b.asLong(),k->{var n=new Day(b);n.id=p.serverLevel().getGameTime();return n;});
        if(own.getLong("DayId")!=d.id){
            // A finished reader keeps what happened on the last day for the account, however late it is read.
            for(var key:own.getString("Done").isEmpty()?List.of("Held","HeldToDoor","Let","SawGo","Opened","Outside","Whole","AfterTalked"):List.of("Whole","AfterTalked"))own.remove(key);
            own.putLong("DayId",d.id);
            if(!d.fresh&&d.day==AFTER){own.putInt("After",own.getInt("After")+1);own.putBoolean("Whole",true);}
        }
        cue(p,!d.fresh&&d.day==AFTER?"The street at night. The shutters are down, and the house on the corner has its windows lit.":"A street of low houses with their shutters down, asleep in the afternoon heat. Petals in the gutters. At the end of it, on a corner, a house behind a tall fence.");
    }

    // ------------------------------------------------------------------------------------------------ the street's clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(DAYS.isEmpty())return;var server=e.getServer();var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var d:List.copyOf(DAYS.values())){
            d.tick++;var here=readers(l,d.base);
            if(here.isEmpty()){if(++d.empty>=EMPTY_RESET&&loaded(l,d)){reset(l,d);DAYS.remove(d.base.asLong());}continue;}
            d.empty=0;
            if(d.fresh){if(!loaded(l,d))continue;d.fresh=false;start(l,d,here);}
            walk(l,d);
            leash(l,d);
            if(d.tick%2==0)look(l,d);
            stage(l,d,here);
            d.since++;
        }
    }
    /** The day: the furthest any unfinished reader there has reached, or, once every reader there is done, a night after. */
    static void start(ServerLevel l,Day d,List<ServerPlayer> here){
        int most=0,after=0;boolean open=false;
        for(var r:here){var own=own(r);if(own.getString("Done").isEmpty()){open=true;most=Math.max(most,Math.min(LAST,own.getInt("Days")+1));}else after=Math.max(after,own.getInt("After"));}
        d.day=open?Math.max(DARE,most):AFTER;d.after=after+1;
        clearActors(l,d.base);d.holder=null;d.backOpen=false;d.handOut=false;
        CornerSite.gate(l,d.base,d.day==AFTER);CornerSite.path(l,d.base,d.day==AFTER);CornerSite.frontDoor(l,d.base,d.day==AFTER);CornerSite.backDoor(l,d.base,false);CornerSite.lastDoor(l,d.base,false);
        CornerSite.jars(l,d.base,Math.min(CornerSite.JARS.size(),CornerSite.FIRST_JARS+(d.day==AFTER?d.after:0)));
        CornerSite.windows(l,d.base,d.day==AFTER);CornerSite.lamps(l,d.base,true);
        if(d.day==AFTER){
            spawn(l,d,3,CornerSite.KERB_ROMINA,0);d.stage=NIGHT;
            for(var r:here){var own=own(r);own.putInt("After",own.getInt("After")+1);own.putBoolean("Whole",true);save(r,own);}
            return;
        }
        spawn(l,d,0,CornerSite.KERB_ROMINA,0);spawn(l,d,1,CornerSite.KERB_NACHO,0);spawn(l,d,2,CornerSite.KERB_LULI,0);
        d.stage=WAITING;
    }
    static void next(Day d,int stage){d.stage=stage;d.since=-1;}

    static void stage(ServerLevel l,Day d,List<ServerPlayer> here){
        int t=d.since;
        switch(d.stage){
            case WAITING->waiting(l,d,here,t);
            case GOING->going(l,d,t);
            case INSIDE->inside(l,d,here,t);
            case WALKING->{if(t%120==60&&d.day==CORRIDOR)speak(l,d,t%240==60?1:2,t%240==60?"Romi, come back.":"I want to go home.");var a=actor(l,d.cast[0]);if(a!=null&&!busy(d,0)){next(d,AT_DOOR);}}
            case AT_DOOR->atDoor(l,d,t);
            case GONE->gone(l,d,here,t);
            case HOME->{if(t==0){for(int who=0;who<3;who++){var a=actor(l,d.cast[who]);if(a!=null)home(d,who,rel(a.position(),d.base));}}
                for(int who=0;who<3;who++){var a=actor(l,d.cast[who]);if(a!=null&&!busy(d,who))a.discard();}
                if(t>=400)next(d,OVER);}
            default->{}
        }
    }
    /** On the kerb, until a reader comes over, or long enough that they go anyway. */
    static void waiting(ServerLevel l,Day d,List<ServerPlayer> here,int t){
        var a=actor(l,d.cast[0]);if(a==null)return;
        boolean near=false;for(var r:here)if(r.distanceTo(a)<7)near=true;
        if(t==40)speak(l,d,0,switch(d.day){case DARE->"You're new. Do you want to see it? Nobody lives there. Nobody ever has.";case CORRIDOR->"It's open today. I know it is. Come on.";default->"Today. Come on.";});
        if(near&&t>=60||t>=KERB_WAIT){
            next(d,GOING);
            CornerSite.gate(l,d.base,true);CornerSite.frontDoor(l,d.base,true);sound(l,d.base.offset(CornerSite.GATE_W),CornerRegistry.GATE.get(),.8F,1F);
            send(d,0,new Vec3(18.5,0,-62.5),BY_GATE,IN_GARDEN,AT_FRONT,ENTER,ROMINA_ROOM);
            if(d.day==LAST){send(d,1,new Vec3(17.5,0,-62.5),NACHO_GATE);send(d,2,new Vec3(19.5,0,-62.5),LULI_GATE);speak(l,d,1,"I'm not going in. I'll wait here. Somebody has to.");}
            else{send(d,1,new Vec3(17.5,0,-62.5),BY_GATE,IN_GARDEN,AT_FRONT,ENTER,NACHO_ROOM);send(d,2,new Vec3(19.5,0,-62.5),BY_GATE,IN_GARDEN,AT_FRONT,ENTER,LULI_ROOM);}
        }
    }
    /** Home from wherever they are: out of the room and the garden by the path, then off down the cross street. */
    static void home(Day d,int who,Vec3 at){
        var road=new Vec3(who==1?17.5:who==2?19.5:18.5,0,-62.5);
        if(at.z<CornerSite.HOUSE_S)send(d,who,ENTER,AT_FRONT,IN_GARDEN,BY_GATE,road,AWAY);
        else if(at.z<CornerSite.NORTH_FRONT+1)send(d,who,BY_GATE,road,AWAY);
        else send(d,who,road,AWAY);
    }
    static void going(ServerLevel l,Day d,int t){if(!busy(d,0)||t>=600){next(d,INSIDE);}}
    /** The front room: the same beats each day, then home, or the door at the back. */
    static void inside(ServerLevel l,Day d,List<ServerPlayer> here,int t){
        if(t==20&&d.day!=LAST)speak(l,d,1,"The electric's been cut off since before we were born. Look at it. Every light on.");
        if(t==80&&d.day!=LAST)speak(l,d,2,"Don't touch anything. I mean it.");
        if(t==140)speak(l,d,0,d.day==DARE?"They're always here. The same ones, every time.":"Look. Somebody keeps it clean.");
        if(d.day==DARE){
            if(t==ROOM_BEAT){speak(l,d,0,"It won't open. It always opens.");sound(l,d.base.offset(CornerSite.BACK_DOOR),SoundEvents.WOODEN_DOOR_CLOSE,.6F,.7F);}
            if(t==ROOM_BEAT+100){speak(l,d,1,"Mum'll be up from her nap. Come on, Romi.");
                witnessed(l,d,(r,own)->{if(CornerSite.inRoom(rel(r.position(),d.base))){own.putBoolean("Whole",true);own.putInt("Days",Math.max(own.getInt("Days"),DARE));}});
                next(d,HOME);}
            return;
        }
        if(t==ROOM_BEAT){CornerSite.backDoor(l,d.base,true);d.backOpen=true;sound(l,d.base.offset(CornerSite.BACK_DOOR),SoundEvents.WOODEN_DOOR_OPEN,.8F,.8F);send(d,0,BY_BACK,HALL_MOUTH);}
        if(t>ROOM_BEAT&&!busy(d,0)&&!d.handOut){d.handOut=true;var a=actor(l,d.cast[0]);if(a!=null)a.gesture(HAND_OUT);speak(l,d,0,"Come on. It goes further than you think.");everyone(l,d,"Romina holds out her hand.");}
        if(d.handOut&&(d.holder!=null||t>=ROOM_BEAT+HAND_WAIT)){next(d,WALKING);send(d,0,LAST_FRONT);var a=actor(l,d.cast[0]);if(a!=null&&d.holder==null)a.gesture(0);}
    }
    /** Whoever holds her hand is drawn gently after her; letting go is always possible, and walking away lets go. */
    static void leash(ServerLevel l,Day d){
        if(d.holder==null)return;var p=holder(l,d);var a=actor(l,d.cast[0]);
        if(p==null||a==null||p.isShiftKeyDown()||p.distanceTo(a)>8||!CornerSite.inHouse(rel(p.position(),d.base))){letGo(l,d,p);return;}
        double dist=p.distanceTo(a);
        if(dist>2.2){var v=a.position().subtract(p.position());var h=new Vec3(v.x,0,v.z).normalize().scale(Math.min(.09,.03*(dist-2.2)));p.setDeltaMovement(p.getDeltaMovement().add(h));p.hurtMarked=true;}
    }
    static void letGo(ServerLevel l,Day d,@Nullable ServerPlayer p){
        if(d.holder==null)return;d.holder=null;var a=actor(l,d.cast[0]);if(a!=null&&d.stage<AT_DOOR)a.gesture(d.stage==INSIDE?HAND_OUT:0);
        if(p!=null){var own=own(p);own.putBoolean("Let",true);save(p,own);cue(p,"You let go of her hand.");}
    }
    static void atDoor(ServerLevel l,Day d,int t){
        var a=actor(l,d.cast[0]);
        if(t==0){if(a!=null)a.gesture(TOUCH);witnessed(l,d,(r,own)->{if(CornerSite.deepInHall(rel(r.position(),d.base)))own.putBoolean("Hall",true);});}
        if(d.day==CORRIDOR){
            if(t==40){speak(l,d,0,"Not today.");var h=holder(l,d);if(h!=null){d.holder=null;cue(h,"She lets go of your hand.");}if(a!=null)a.gesture(0);
                witnessed(l,d,(r,own)->{if(CornerSite.deepInHall(rel(r.position(),d.base))){own.putBoolean("Whole",true);own.putInt("Days",Math.max(own.getInt("Days"),CORRIDOR));}});
                send(d,0,HALL_MOUTH,BY_BACK,ROMINA_ROOM);}
            if(t>40&&!busy(d,0)||t>=600){speak(l,d,1,"Come on. Mum'll be up.");next(d,HOME);}
            return;
        }
        if(t==10){var h=holder(l,d);if(h!=null){var own=own(h);own.putBoolean("HeldToDoor",true);save(h,own);d.holder=null;}speak(l,d,0,"Wait here.");}
        if(t==40){CornerSite.lastDoor(l,d.base,true);sound(l,d.base.offset(CornerSite.LAST_DOOR),SoundEvents.WOODEN_DOOR_OPEN,.8F,.9F);send(d,0,IN_DOOR);}
        if(t==60){
            CornerSite.lastDoor(l,d.base,false);sound(l,d.base.offset(CornerSite.LAST_DOOR),CornerRegistry.SHUT.get(),1F,1F);
            if(a!=null)a.discard();d.cast[0]=null;d.routes.remove(0);
            witnessed(l,d,(r,own)->own.putBoolean("SawGo",true));
            CornerSite.path(l,d.base,true);
            for(var r:readers(l,d.base))if(!CornerSite.inHall(rel(r.position(),d.base)))cue(r,"Somewhere deep in the house, a door shuts.");
            next(d,GONE);
        }
    }
    /** She has gone in. Each reader opens the last door, or waits outside until the front door opens; then her brother goes home. */
    static void gone(ServerLevel l,Day d,List<ServerPlayer> here,int t){
        if(t==200)speak(l,d,1,"She always comes out. She always does.");
        if(t==500)speak(l,d,2,"Nacho. Can we go home?");
        boolean all=true;for(var r:here)if(own(r).getString("Done").isEmpty())all=false;
        if(all&&t>=100||t>=1800){speak(l,d,1,"I'll tell Mum she's at Luli's.");next(d,HOME);}
    }

    // ------------------------------------------------------------------------------------------------ each reader
    /** Every five ticks, for each reader on the street. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag held){
        save(p,held);var own=own(p);var d=day(b);var l=p.serverLevel();
        long hour=d!=null&&d.day==AFTER&&!d.fresh?18000L:-1L;var sent=SENT.get(p.getUUID());
        if(sent==null||sent!=hour||l.getGameTime()%100==0){SENT.put(p.getUUID(),hour);HousePackets.send(p,new CornerPayload(hour));}
        if(d==null){refresh(held,own);return;}
        var r=rel(p.position(),b);
        if(CornerSite.inRoom(r)||CornerSite.inHall(r))own.putBoolean("InHouse",true);
        // Cicadas out on the street at siesta; in the front room, the hum of lights that have no power to them.
        long beat=l.getGameTime()/5+p.getId();
        if(!d.fresh&&d.day!=AFTER&&CornerSite.inStreet(r)&&beat%40==0)p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(CornerRegistry.CICADAS,SoundSource.AMBIENT,p.getX()+3,p.getY()+4,p.getZ()-2,.45F,1F,p.getRandom().nextLong()));
        if(CornerSite.inRoom(r)&&beat%12==0){var at=world(b,new Vec3(18.5,2,-81.5));p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(CornerRegistry.HUM,SoundSource.BLOCKS,at.x,at.y,at.z,.35F,1F,p.getRandom().nextLong()));}
        // Only a reader who was on the street when she went in can wait for the door; one who came after has nothing to wait for.
        if(d.stage==GONE&&own.getString("Done").isEmpty()&&own.getBoolean("SawGo")){
            if(CornerSite.outside(r)&&!CornerSite.inHouse(r)){own.putInt("Outside",own.getInt("Outside")+5);
                if(own.getInt("Outside")>=WAIT){
                    if(!CornerSite.doorOpen(l,b,CornerSite.FRONT_DOOR)){CornerSite.frontDoor(l,b,true);sound(l,b.offset(CornerSite.FRONT_DOOR),SoundEvents.WOODEN_DOOR_OPEN,1F,.8F);everyone(l,d,"The front door of the house opens onto the street. Nobody comes out.");}
                    else cue(p,"The front door stands open onto the street. Nobody has come out.");
                    resolve(p,own,"waited");
                }
            }else if(CornerSite.inHouse(r))own.putInt("Outside",0);
        }
        if(own.getBoolean("Ready")&&!own.getBoolean("Completed")&&l.getGameTime()%100==0)account(p,b);
        refresh(held,own);
    }
    /** The end of it for this reader: the account is ready, under the pebble on the kerb. */
    static void resolve(ServerPlayer p,CompoundTag own,String how){
        if(!own.getString("Done").isEmpty())return;
        own.putString("Done",how);own.putBoolean("Whole",true);own.putInt("Days",LAST);
        LiteraryVignettes.ready(p,PLACE,own,how.equals("opened")?"opened_the_last_door":"waited_outside_for_the_door");
        save(p,own);var b=LiteraryVignettes.base(p,PLACE);if(b!=null)account(p,b);
    }
    static void account(ServerPlayer p,BlockPos b){var at=b.offset(CornerSite.NOTE);var st=p.serverLevel().getBlockState(at);if(CornerBlock.is(st,CornerBlock.Kind.NOTE))p.connection.send(new ClientboundBlockUpdatePacket(at,st.setValue(CornerBlock.STAGE,1)));}

    // ------------------------------------------------------------------------------------------------ talk, and her hand
    /** Right-clicking one of them. Her hand, when she holds it out; otherwise her stories, and the others' ordinary talk. */
    public static void talk(ServerPlayer p,LiteraryActor actor){
        var b=LiteraryVignettes.base(p,PLACE);var d=b==null?null:day(b);if(d==null||!cast(actor.phase()))return;var l=p.serverLevel();
        int who=actor.phase()-ROMINA;
        if(who==0&&(d.handOut&&d.stage==INSIDE||d.stage==WALKING)){
            if(p.getUUID().equals(d.holder)){letGo(l,d,p);return;}
            if(d.holder==null){d.holder=p.getUUID();actor.gesture(HOLDING);var own=own(p);own.putBoolean("Held",true);own.putBoolean("HeldEver",true);save(p,own);cue(p,"You take her hand. It is small, and dry, and she holds on.");
                for(var r:readers(l,d.base))if(r!=p&&r.distanceToSqr(p)<24*24)cue(r,p.getGameProfile().getName()+" takes Romina's hand.");return;}
            cue(p,"Someone else has her hand.");return;
        }
        var own=own(p);String line;var name=NAMES[who];
        if(who==0){
            var told=own.getList("Stories",3);
            if(io.github.knaitoe.theoldesthouse.labyrinth.BodyLoss.armTaken(p.server,p.getUUID())!=null&&!own.getBoolean("TrueStory")){own.putBoolean("TrueStory",true);line=CornerTexts.TRUE_STORY;}
            else if(told.size()<CornerTexts.STORIES.length){line=CornerTexts.STORIES[told.size()];told.add(IntTag.valueOf(told.size()));own.put("Stories",told);}
            else line="I've told you all of them. You pick one.";
            save(p,own);
            Talk.begin(p,TALK,name);Talk.line(p,name,line);actor.say(line);actor.speaks(line);d.said.put(actor.getUUID(),d.tick);
            Talk.ask(p,Talk.reply("Ask what really happened to it.",q->{Talk.line(q,name,"That's what really happened.");Talk.end(q);}),Talk.act("Leave it there.",Talk::close));
            return;
        }
        line=switch(actor.phase()){
            case NACHO->d.stage==GONE?"She'll come out. She always comes out. You can go, if you want.":d.day==LAST?"I'm not going in. Somebody has to stay out here.":d.day==CORRIDOR?"We went in yesterday and nothing happened. Nothing ever happens.":"Don't believe her about her arm. She changes it. I don't remember either, and I was there.";
            case LULI->d.stage==GONE?"Can we go home now? I want to go home.":d.day==DARE?"My nan says nobody has ever lived there. Not ever. It was built like that, empty.":"Don't touch the jars. I mean it.";
            default->switch(Math.min(5,Math.max(1,own.getInt("After")))){
                case 1->"I know you. You were here that day. I hear her, you know. Not in there. In our house, in the wall behind my bed, telling one of her stories about her arm.";
                case 2->"My mother still sets four places at the table. Nobody says anything.";
                case 3->"The lights in there came on at three in the morning last week. The whole street came out in their slippers to look. Nobody went to the gate.";
                case 4->"I went up to the gate once, at night. I could hear something being set down on a shelf in there. Then another.";
                default->"She'd be grown by now. I keep thinking I'll see her on a bus, and she'll tell me something different.";
            };
        };
        if(actor.phase()==NACHO_GROWN){own.putBoolean("AfterTalked",true);save(p,own);}
        Talk.begin(p,TALK,name);Talk.line(p,name,line);actor.say(line);actor.speaks(line);d.said.put(actor.getUUID(),d.tick);
        Talk.ask(p,Talk.act("Leave it there.",Talk::close));
    }

    // ------------------------------------------------------------------------------------------------ the things on the street and in the house
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag held){
        save(p,held);boolean handled=clicked(p,b,rel,own(p));refresh(held,own(p));return handled;
    }
    static boolean clicked(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var d=day(b);var st=l.getBlockState(b.offset(rel));
        if(CornerBlock.is(st,CornerBlock.Kind.JAR)){
            int stage=st.getValue(CornerBlock.STAGE);if(stage==0){cue(p,"A bare board.");return true;}
            int i=CornerSite.JARS.indexOf(rel);sound(l,b.offset(rel),CornerRegistry.JAR.get(),.5F,1F);
            own.putBoolean("Jars",true);save(p,own);
            cue(p,switch(stage){case 1->"A jar of small teeth. Milk teeth, a lot of them, packed to the lid.";case 2->"A jar of clipped fingernails, filled to the brim.";default->"Something pale in the jar, folded over on itself. You can't tell what it is.";}+(i>=CornerSite.FIRST_JARS?" You don't remember this one.":""));
            return true;
        }
        if(CornerBlock.is(st,CornerBlock.Kind.KIOSK)){cue(p,"Yesterday's papers in the rack, and a card in the window: back at five.");return true;}
        if(CornerBlock.is(st,CornerBlock.Kind.DOG)){cue(p,"The dog is asleep in the shade. It doesn't wake.");return true;}
        if(CornerBlock.is(st,CornerBlock.Kind.GATE)){if(!CornerBlock.open(st))cue(p,"The gate is chained on the inside.");return true;}
        if(st.getBlock() instanceof DoorBlock){
            var lower=st.getValue(DoorBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER?rel:rel.below();
            if(lower.equals(CornerSite.ONWARD))return false;
            if(lower.equals(CornerSite.LAST_DOOR)){
                if(d!=null&&d.stage>=GONE&&d.stage<NIGHT&&d.day==LAST&&own.getBoolean("SawGo")){
                    // Each reader's last room is their own: the door opens for them alone.
                    var at=b.offset(CornerSite.LAST_DOOR);var s=l.getBlockState(at);
                    if(s.getBlock() instanceof DoorBlock){p.connection.send(new ClientboundBlockUpdatePacket(at,s.setValue(DoorBlock.OPEN,true)));p.connection.send(new ClientboundBlockUpdatePacket(at.above(),l.getBlockState(at.above()).setValue(DoorBlock.OPEN,true)));}
                    p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.Holder.direct(SoundEvents.WOODEN_DOOR_OPEN),SoundSource.BLOCKS,at.getX()+.5,at.getY()+.5,at.getZ()+.5,1F,.9F,p.getRandom().nextLong()));
                    cue(p,"The room behind the door is empty, and smaller than the door is. There is nowhere in it to go.");
                    own.putBoolean("Opened",true);
                    if(own.getString("Done").isEmpty())resolve(p,own,"opened");else save(p,own);
                    return true;
                }
                cue(p,"It won't open.");return true;
            }
            if(lower.equals(CornerSite.BACK_DOOR)){if(d==null||!d.backOpen)cue(p,"It won't open.");return d==null||!d.backOpen;}
            if(lower.equals(CornerSite.FRONT_DOOR)){if(!CornerSite.doorOpen(l,b,CornerSite.FRONT_DOOR))cue(p,"Locked.");return true;}
            cue(p,"Shut. Everyone in the street is asleep, or pretending to be.");return true;
        }
        if(CornerBlock.is(st,CornerBlock.Kind.WINDOW)){cue(p,st.getValue(CornerBlock.STAGE)==1?"Light through the slats of the shutter.":"The shutter is down.");return true;}
        return false;
    }

    // ------------------------------------------------------------------------------------------------ dealing and the reset
    /** The house is there on every visit, before its story is had and after. */
    public static boolean dealable(LabyrinthData d,UUID p){return true;}
    /** Nobody on the street: the next day starts over, the house set back as it was. */
    static void reset(ServerLevel l,Day d){
        clearActors(l,d.base);d.holder=null;
        CornerSite.gate(l,d.base,false);CornerSite.path(l,d.base,false);CornerSite.frontDoor(l,d.base,false);CornerSite.backDoor(l,d.base,false);CornerSite.lastDoor(l,d.base,false);
        CornerSite.jars(l,d.base,CornerSite.FIRST_JARS);CornerSite.windows(l,d.base,false);CornerSite.lamps(l,d.base,true);
    }
    /** For tests: the street set back as an empty street would be. */
    public static void resetNow(ServerLevel l,BlockPos b){var d=day(b);if(d!=null){reset(l,d);DAYS.remove(b.asLong());}}
}
