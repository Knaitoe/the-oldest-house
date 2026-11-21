package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.TheListNightPayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 0.4.706, The list, part 3a (docs/THE_LIST_0_4_706.md): night one, as far as coming to. The town's night runs from ten on its own clock,
 * one to a town (a copy's own once the town has copies), only while someone is in it.
 * <ol>
 * <li><b>The party</b> ({@link #PARTY}): the five on the list at the stock tank, a pickup with its lights on and a sedan pulled up, a radio,
 * an ice chest, a lantern, a blanket. The radio is heard across town. Deek asks the one he has picked (the first reader through the
 * door) to help him with a door at the gin; talked to again, he takes them.</li>
 * <li><b>The ride</b> ({@link #RIDE}): the view goes black for everyone at the tank and comes back at the gin's yard, the five and the
 * pickup with them.</li>
 * <li><b>The seed house</b> ({@link #SEED}): Deek rolls its door open and sends the one he picked in. The House's door is open to them
 * until here.</li>
 * <li><b>Shut</b> ({@link #SHUT}): three blocks in, the door rolls shut behind them and is bolted; the lamp and the bulb go out; the auger
 * starts in its trench and the seed goes, pulling them toward it; outside the five laugh, then do not. Then black. The House's door
 * is shut to them from the door rolling shut.</li>
 * <li><b>After</b> ({@link #AFTER}): three in the morning. The one who went in comes to under the water tower, face down, with the sack
 * over their head and the body theirs (TheListBody.comeTo). Anybody else is where they stood, alone; the five and the trucks are gone.</li>
 * <li><b>First light</b> ({@link #DONE}, 0.4.707, docs/THE_LIST_0_4_707.md): the one who went in walks home past Amber's window, where
 * she holds pages up to the glass for each reader who passes, and night one ends when they lie down on their own bed or when the clock
 * comes round to first light, whichever is first. Everyone in town has had night one from then.</li>
 * </ol>
 * From 0.4.707 the House deals the town, and night one begins round the first reader through its door.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TheListNight {
    private TheListNight(){}
    /** The night's hours in the game's own day time: ten, two, three, and the first light, where its clock holds for now. */
    public static final long TEN=16000,TWO=20000,THREE=21000,LIGHT=23300;
    /** How much of the night passes in a tick while someone is in town: twenty real minutes from ten to the first light. */
    public static final double RATE=1/3.0;
    public static final int NONE=0,PARTY=1,RIDE=2,SEED=3,SHUT=4,AFTER=5,DONE=6;
    /** The phases' names, for an operator. */
    public static final String[] NAMES={"none","party","ride","seed house","shut","after","first light","night two, home","night two, the den","night two, to the field","night two, the field","night two, ran","night two, the hunt","night two, first light","night three","night three, first light","night four","the end"};
    /** 0.4.720, night two (TheListDen): home at ten, Amber's den, her walk to the field, the field, and the first rule written. */
    public static final int HOME2=7,DEN=8,TO_FIELD=9,FIELD=10,RAN=11;
    /** The hour on a clock, from the game's own day time: "2:00 am". */
    public static String hour(long time){
        if(time<0)return "no night";
        long t=Math.floorMod(time+6000,24000);int h=(int)(t/1000),m=(int)((t%1000)*60/1000);
        return (h%12==0?12:h%12)+":"+(m<10?"0":"")+m+(h<12?" am":" pm");
    }
    /** The ride: the black lands, and everyone at the tank is set down at the gin. */
    public static final int RIDE_BLACK=24;
    /** At the seed house: the door rolls open from OPEN, a column every three ticks; Deek sends them in at TOLD. */
    public static final int OPEN=30,TOLD=44;
    /** Shut: the bolt, the lights out, the auger, the fade and the cut; the lines outside at SHUT_AT. */
    public static final int BOLT=10,DARK=40,AUGER=60,FADE=150,CUT=172;
    public static final int[] SHUT_AT={14,30,50,66,84,104,124};
    /** After the cut: the one who went in lies face down this long (the black lifting over the first part of it); told where they are at WAKE. */
    public static final int LIE=150,WAKE=90;
    /** How near Deek asks the one he picked, how near the tank a reader rides along, how far past the door the one who goes in has gone when it shuts. */
    public static final double ASK_REACH=6,RIDE_REACH=24,INSIDE=3;
    /** 0.4.707: how near Amber's window a reader in the street must be for her to hold a page up, and the time between her two pages. */
    public static final double PAGE_REACH=14;public static final int PAGE_GAP=60;
    /** 0.4.707: after first light comes, how long until each reader is told of it (past the black, for a reader who went to bed). */
    public static final int LIGHT_CUE=70;
    /** 0.4.707: Amber's place from the cut until first light: on her feet beside her desk, facing the street, the hand that holds the
     * page beside the window (her front wall is two blocks thick, the window cut through both). */
    public static final BlockPos AT_WINDOW=new BlockPos(TheListSite.AMBER_DESK.getX()+1,0,TheListSite.AMBER_DESK.getZ());
    /** Her window, on the front of her house over Seventh. */
    public static final BlockPos WINDOW=new BlockPos(TheListSite.AMBER_DESK.getX(),1,TheListSite.AMBER_S);

    // ------------------------------------------------------------------------------------------------ where
    /** The five, by their number in TheListPeople: Deek, Wes, Mel, Lesley, Shannon. */
    public static final int[] FIVE={TheListPeople.DEEK,TheListPeople.WES,TheListPeople.MEL,TheListPeople.LESLEY,TheListPeople.SHANNON};
    /** Where the five stand at the tank, north of it, and which way: Deek by the pickup's nose and the lantern, Wes by the radio, Mel
     * across from them with the camera, Lesley by the lawn chair, Shannon on the blanket. */
    public static final BlockPos[] AT_TANK={new BlockPos(-28,0,77),new BlockPos(-25,0,78),new BlockPos(-31,0,80),new BlockPos(-34,0,79),new BlockPos(-32,0,76)};
    static final Direction[] FACE_TANK={Direction.WEST,Direction.WEST,Direction.NORTH,Direction.EAST,Direction.EAST};
    /** Where they stand in the gin's yard: Deek by the seed house's door, the rest back toward the pickup. */
    public static final BlockPos[] AT_GIN={new BlockPos(77,0,-73),new BlockPos(84,0,-71),new BlockPos(85,0,-69),new BlockPos(83,0,-70),new BlockPos(86,0,-72)};
    static final Direction[] FACE_GIN={Direction.EAST,Direction.WEST,Direction.WEST,Direction.WEST,Direction.WEST};
    /** Where the party's things are set out, which, and facing: the radio, the ice chest, cans, the lantern, the blanket, the case, a lawn chair. */
    public static final BlockPos[] PROPS={new BlockPos(-25,0,77),new BlockPos(-33,0,78),new BlockPos(-30,0,79),new BlockPos(-26,0,80),new BlockPos(-29,0,78),
        new BlockPos(-32,0,76),new BlockPos(-24,0,80),new BlockPos(-35,0,77)};
    static final int[] PROP_STAGE={0,1,2,2,3,4,5,-1};
    static final Direction[] PROP_FACE={Direction.WEST,Direction.SOUTH,Direction.NORTH,Direction.EAST,Direction.NORTH,Direction.NORTH,Direction.NORTH,Direction.EAST};
    /** Where the pickup's lights fall: a light there, not a block anyone sees. */
    static final BlockPos BEAM=new BlockPos(-27,1,77);
    public static final Vec3 PICKUP=new Vec3(-27.5,0,72.5),SEDAN=new Vec3(-34.5,0,73),PICKUP_GIN=new Vec3(91,0,-70.5);
    static final float PICKUP_YAW=0,SEDAN_YAW=-20,GIN_YAW=90;
    /** Where those who rode are set down at the gin: the one Deek picked first, before the seed house's door. */
    static final Vec3[] RIDERS={new Vec3(79.5,0,-72.5),new Vec3(82.5,0,-72.5),new Vec3(80.5,0,-71.5),new Vec3(81.5,0,-70.5),new Vec3(87.5,0,-73.5),new Vec3(88.5,0,-72.5)};
    /** Where the one who went in comes to: under the water tower, between its legs. */
    public static final Vec3 UNDER=new Vec3(TheListSite.TOWER_X-.5,0,TheListSite.TOWER_Z+1.5);
    /** The seed house's doorway, the cells beside it the door rolls into, the lamp over it and the bulb inside. */
    public static final int DOOR_X=(TheListSite.SEED_W+TheListSite.SEED_E)/2;
    public static final BlockPos LAMP=new BlockPos(DOOR_X,3,TheListSite.SEED_S+1),BULB=new BlockPos(DOOR_X+2,TheListSite.BULB_Y,(TheListSite.SEED_N+TheListSite.SEED_S)/2);
    /** The auger's trench, along which the seed draws them. */
    public static final double TRENCH_X=DOOR_X+1.5;

    // ------------------------------------------------------------------------------------------------ the night's record
    static final class Night{
        final BlockPos base;@Nullable UUID slasher;int phase,since;double time=TEN;boolean asked,bed;
        /** 0.4.721: which of the five are dead from an earlier night, a bit for each; the town does not make them again. */
        int dead;
        /** 0.4.729: which of the five have left town on an earlier night, warned off; the town does not make them again either. */
        int left;
        final Set<UUID> riders=new LinkedHashSet<>();@Nullable UUID pickup,sedan;int sent;
        Night(BlockPos b){base=b;}
    }
    static final Map<Long,Night> NIGHTS=new HashMap<>();
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){NIGHTS.clear();}
    public static void clearAll(){NIGHTS.clear();}
    static int copy(MinecraftServer s,BlockPos b){return SceneInstances.copyAt(s,TheList.PLACE,b);}
    /** The night of the town at this base, from its record; null while it has none. */
    static @Nullable Night night(MinecraftServer s,BlockPos b){
        var n=NIGHTS.get(b.asLong());if(n!=null)return n;
        var t=LiteraryVignettes.shared(LabyrinthData.get(s),TheList.PLACE,copy(s,b)).getCompound("Night");
        if(!t.contains("Phase"))return null;
        n=new Night(b);n.slasher=t.hasUUID("Slasher")?t.getUUID("Slasher"):null;n.phase=t.getInt("Phase");n.since=t.getInt("Since");n.time=t.getDouble("Time");n.asked=t.getBoolean("Asked");n.bed=t.getBoolean("Bed");n.dead=t.getInt("Dead");n.left=t.getInt("Left");
        for(var r:t.getList("Riders",Tag.TAG_INT_ARRAY))n.riders.add(NbtUtils.loadUUID(r));
        NIGHTS.put(b.asLong(),n);return n;
    }
    static void save(MinecraftServer s,Night n){
        var d=LabyrinthData.get(s);int copy=copy(s,n.base);var w=LiteraryVignettes.shared(d,TheList.PLACE,copy);
        var t=new CompoundTag();if(n.slasher!=null)t.putUUID("Slasher",n.slasher);
        t.putInt("Phase",n.phase);t.putInt("Since",n.since);t.putDouble("Time",n.time);t.putBoolean("Asked",n.asked);t.putBoolean("Bed",n.bed);t.putInt("Dead",n.dead);t.putInt("Left",n.left);
        var r=new ListTag();for(var id:n.riders)r.add(NbtUtils.createUUID(id));t.put("Riders",r);
        w.put("Night",t);LiteraryVignettes.shared(d,TheList.PLACE,copy,w);
    }
    /** Where the town's night is: NONE until one begins. */
    public static int phase(MinecraftServer s,BlockPos b){var n=night(s,b);return n==null?NONE:n.phase;}
    /** The night's hour, or -1 with no night. */
    public static long time(MinecraftServer s,BlockPos b){var n=night(s,b);return n==null?-1:(long)n.time;}
    /** Whose night it is: the one Deek picks, who comes to with the body. */
    public static @Nullable UUID slasher(MinecraftServer s,BlockPos b){var n=night(s,b);return n==null?null:n.slasher;}
    /** 0.4.721: whether one of the five is dead from an earlier night. */
    public static boolean gone(MinecraftServer s,BlockPos b,int who){var n=night(s,b);return n!=null&&(TheListNights.dead(n,who)||(n.left&1<<who)!=0);}
    public static int since(MinecraftServer s,BlockPos b){var n=night(s,b);return n==null?0:n.since;}

    /**
     * Night one begins round this reader, who is the one Deek picks: the five go out to the tank, the trucks pull up, the party is set out.
     * False if the town has a night already, or the reader cannot be the one (a spectator, or not in this town).
     */
    public static boolean begin(ServerPlayer p,BlockPos b){
        if(!LiteraryVignettes.participant(p)||!TheListSite.inScene(p.position().subtract(Vec3.atLowerCornerOf(b))))return false;
        var n=night(p.server,b);if(n!=null&&n.phase!=NONE)return false;
        n=new Night(b);n.slasher=p.getUUID();n.phase=PARTY;n.time=TEN;NIGHTS.put(b.asLong(),n);save(p.server,n);quiet(p.server,b,false);
        TheList.ACTIVE.computeIfAbsent(b.asLong(),k->new TheList.Site(b));
        var l=p.serverLevel();props(l,b,true);TheListPeople.ensure(l,b);
        for(var q:TheList.readers(l,b))TheList.cue(q,TheListTexts.PARTY_HEARD);
        send(l,n,true);
        return true;
    }
    /**
     * A reader come into town. 0.4.707: into a town with no night, night one begins round them if they have not had it and the town is
     * not held quiet; into a town whose night one is over and nobody else is in, it is put back and begins again round them. Into a night
     * that is on, they hear the party if it is on, and the night's hour is theirs at once.
     */
    static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var n=night(p.server,b);
        boolean owed=LiteraryVignettes.participant(p)&&own.getInt("Night")<1;
        if(n==null||n.phase==NONE){if(owed&&!quiet(p.server,b))begin(p,b);return;}
        // 0.4.720: the one who went in, back in a town whose night one is over, and night two begins round them.
        if(TheListDen.owed(p,n,own)){TheListDen.begin(p,n);return;}
        // 0.4.721: and nights three and four, the same way.
        if(TheListNights.owed(p,n,own)){TheListNights.begin(p,n);return;}
        // 0.4.721: a town whose four nights are over, come into by a reader who has not had one and nobody else in, begins again round them.
        if(n.phase==TheListNights.OVER&&owed&&TheList.readers(p.serverLevel(),b).stream().allMatch(q->q==p)){end(p.serverLevel(),b);begin(p,b);return;}
        if(n.phase>=HOME2){TheListDen.hour(p,n);return;}
        if(n.phase==DONE&&owed&&TheList.readers(p.serverLevel(),b).stream().allMatch(q->q==p)){end(p.serverLevel(),b);begin(p,b);return;}
        if(n.phase==PARTY)TheList.cue(p,TheListTexts.PARTY_HEARD);
        boolean running=n.phase==PARTY||n.phase==RIDE||n.phase==AFTER;
        PacketDistributor.sendToPlayer(p,new TheListNightPayload((long)n.time,(float)(running?RATE:0),0));
    }
    /** 0.4.707: whether the town is held without a night: no night begins round a reader coming in until one is begun by hand. */
    public static boolean quiet(MinecraftServer s,BlockPos b){return LiteraryVignettes.shared(LabyrinthData.get(s),TheList.PLACE,copy(s,b)).getBoolean("Quiet");}
    /** For an operator or a check: the town held without a night, or let go. */
    public static void quiet(MinecraftServer s,BlockPos b,boolean held){
        var d=LabyrinthData.get(s);int copy=copy(s,b);var w=LiteraryVignettes.shared(d,TheList.PLACE,copy);
        if(held==w.getBoolean("Quiet"))return;
        if(held)w.putBoolean("Quiet",true);else w.remove("Quiet");
        LiteraryVignettes.shared(d,TheList.PLACE,copy,w);
    }
    /** For an operator: the town's night ended and put back as it was before, the seed house's door, lamp and bulb with it. */
    public static void end(ServerLevel l,BlockPos b){
        var n=night(l.getServer(),b);
        if(n!=null){cars(l,n,false);}
        props(l,b,false);seedHouse(l,b,true,true);TheListDen.reset(l,b);TheListHunt.end(l,b);TheListFlyers.down(l,b);TheListDeputy.reset(l,b);TheListStace.reset(l,b);
        NIGHTS.remove(b.asLong());
        var d=LabyrinthData.get(l.getServer());int copy=copy(l.getServer(),b);var w=LiteraryVignettes.shared(d,TheList.PLACE,copy);w.remove("Night");LiteraryVignettes.shared(d,TheList.PLACE,copy,w);
        TheListPeople.ensure(l,b);
        for(var q:inTown(l,b))PacketDistributor.sendToPlayer(q,new TheListNightPayload(-1,0,0));
    }
    /** For an operator or a check: the night moved straight to a phase, as if it had got there. */
    public static void jump(ServerLevel l,BlockPos b,int phase){
        var n=night(l.getServer(),b);if(n==null||phase<=NONE)return;
        if(phase>=SEED&&n.phase<SEED){props(l,b,false);n.time=Math.max(n.time,TWO);}
        if(phase>=AFTER){n.time=Math.max(n.time,THREE);}
        if(phase>=DONE){dawn(l,n,false);return;}
        n.phase=phase;n.since=0;save(l.getServer(),n);TheListPeople.ensure(l,b);send(l,n,true);
    }

    /** Whether this reader is shut in: the one who went in, from the door rolling shut until the cut. The House's door is not theirs. */
    public static boolean locked(ServerPlayer p){
        var b=TheListBody.town(p);if(b==null)return false;
        // 0.4.726: on night four, from the moment Stace's plan begins.
        var n=night(p.server,b);return n!=null&&(n.phase==SHUT||n.phase==TheListNights.HUNT4&&TheListStace.on(b))&&p.getUUID().equals(n.slasher);
    }

    // ------------------------------------------------------------------------------------------------ each tick
    /** One tick of the town's night, from TheList's tick while someone is in town. */
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var n=night(l.getServer(),b);if(n==null||n.phase==NONE)return;
        n.since++;
        boolean changed=false;
        switch(n.phase){
            case PARTY->{n.time=Math.min(TWO,n.time+RATE);changed=party(l,n,here);}
            case RIDE->{n.time=Math.min(TWO,n.time+RATE);changed=ride(l,n,here);}
            case SEED->{n.time=Math.min(TWO,n.time+RATE/4);changed=seed(l,n,here);}
            case SHUT->changed=shut(l,n,here);
            case AFTER->{n.time=Math.min(LIGHT,n.time+RATE);changed=after(l,n,here);}
            case DONE->changed=light(l,n,here);
            default->{
                if(n.phase>=TheListNights.HUNT2)changed=TheListNights.tick(l,n,here);
                else if(n.phase>=HOME2){changed=TheListDen.tick(l,n,here);if(n.phase==RAN&&TheListNights.ran(l,n))changed=true;}
            }
        }
        if(changed){save(l.getServer(),n);TheListPeople.ensure(l,b);send(l,n,true);}
        else{if(n.since%100==0)save(l.getServer(),n);send(l,n,false);}
    }
    /** The clock (and the shake) to everyone in town, spectators too: on a change, and once a second. */
    static void send(ServerLevel l,Night n,boolean now){
        if(!now&&++n.sent%20!=0)return;
        boolean running=n.phase==PARTY||n.phase==RIDE||n.phase==AFTER||n.phase>=HOME2;
        float rate=(float)(running&&n.time<(n.phase==AFTER||n.phase>=HOME2?LIGHT:TWO)?RATE:0);
        boolean shaking=n.phase==SHUT&&n.since>=AUGER&&n.since<CUT;
        var house=Vec3.atLowerCornerOf(n.base).add(DOOR_X+.5,0,(TheListSite.SEED_N+TheListSite.SEED_S)/2.0);
        for(var q:inTown(l,n.base)){
            int shake=shaking&&q.position().distanceTo(house)<16?(q.getUUID().equals(n.slasher)?30:12):0;
            PacketDistributor.sendToPlayer(q,new TheListNightPayload((long)n.time,rate,shake));
        }
    }
    /** Everyone in town, spectators among them (for the sky). */
    static List<ServerPlayer> inTown(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var q:l.players())if(TheListSite.inScene(q.position().subtract(Vec3.atLowerCornerOf(b))))out.add(q);
        return out;
    }
    static @Nullable ServerPlayer slasher(ServerLevel l,Night n){
        if(n.slasher==null)return null;var p=l.getServer().getPlayerList().getPlayer(n.slasher);
        return p!=null&&p.level()==l&&p.isAlive()&&!p.isSpectator()&&TheListSite.inScene(p.position().subtract(Vec3.atLowerCornerOf(n.base)))?p:null;
    }
    static Vec3 at(BlockPos b,Vec3 rel){return Vec3.atLowerCornerOf(b).add(rel);}
    static Vec3 at(BlockPos b,BlockPos rel){return Vec3.atBottomCenterOf(b.offset(rel));}

    // ------------------------------------------------------------------------------------------------ the party
    static boolean party(ServerLevel l,Night n,List<ServerPlayer> here){
        cars(l,n,true);
        var b=n.base;var radio=b.offset(PROPS[0]);
        if(n.since%160==1){
            l.playSound(null,radio,TheListRegistry.RADIO.get(),SoundSource.RECORDS,1F,1F);
            // Across town it is heard from the way the tank is, faintly: a little of it sent to each reader from that way.
            for(var q:here){
                var to=Vec3.atCenterOf(radio).subtract(q.position());double far=to.length();
                if(far<40||far>170)continue;
                var from=q.position().add(to.normalize().scale(16)).add(0,2,0);
                q.connection.send(new ClientboundSoundPacket(TheListRegistry.RADIO,SoundSource.RECORDS,from.x,from.y,from.z,(float)Math.max(.12,.4-far/600),1F,q.getRandom().nextLong()));
            }
        }
        if(n.since%60==0)props(l,b,true);
        // Deek asks the one he picked once they come near.
        var p=slasher(l,n);
        if(p!=null&&!n.asked&&p.position().distanceTo(at(b,AT_TANK[0]))<ASK_REACH){
            var deek=TheListPeople.find(l,b,TheListPeople.DEEK);
            if(deek!=null){deek.sayTo(p,TheListTexts.NAMES[TheListPeople.DEEK],TheListTexts.ASK);deek.speaks(TheListTexts.ASK);n.asked=true;return true;}
        }
        return false;
    }
    /** A word with one of the five at the party; with Deek, the one he picked says yes. True if the night took it. */
    static boolean talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=TheListBody.town(p);if(b==null)return false;
        var n=night(p.server,b);if(n==null)return false;
        // 0.4.720: on night two, Amber is the den's.
        if(TheListDen.talk(p,a,n,own))return true;
        int who=TheListPeople.who(a);
        // 0.4.707: Amber, at her window from the cut until first light, has one thing to say through the glass.
        if(who==TheListPeople.AMBER&&n.phase==AFTER){a.sayTo(p,TheListTexts.NAMES[who],TheListTexts.AMBER_AFTER);a.speaks(TheListTexts.AMBER_AFTER);return true;}
        if(n.phase!=PARTY||!five(who))return false;
        var name=TheListTexts.NAMES[who];
        if(who==TheListPeople.DEEK&&p.getUUID().equals(n.slasher)){
            if(!n.asked){a.sayTo(p,name,TheListTexts.ASK);a.speaks(TheListTexts.ASK);n.asked=true;save(p.server,n);return true;}
            a.sayTo(p,name,TheListTexts.RIDE);a.speaks(TheListTexts.RIDE);ride(p.serverLevel(),n);return true;
        }
        int heard=own.getInt("Party"+who);own.putInt("Party"+who,heard+1);
        var line=TheListTexts.party(who,heard);a.sayTo(p,name,line);a.speaks(line);
        return true;
    }
    public static boolean five(int who){for(int w:FIVE)if(w==who)return true;return false;}
    /** The party's things set out (or taken up): only into air, or over what is theirs; the lawn chair is a yard's. */
    static void props(ServerLevel l,BlockPos b,boolean out){
        if(!l.isLoaded(b.offset(PROPS[0])))return;
        for(int i=0;i<PROPS.length;i++){
            var at=b.offset(PROPS[i]);var st=l.getBlockState(at);
            var want=PROP_STAGE[i]<0?TheListBlock.of(TheListBlock.Kind.LOT,PROP_FACE[i],4):TheListBlock.of(TheListBlock.Kind.PARTY,PROP_FACE[i],PROP_STAGE[i]);
            if(out){if(!st.equals(want)&&(st.isAir()||TheListBlock.is(st,TheListBlock.Kind.WEED)))l.setBlock(at,want,3);}
            else if(st.equals(want))l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
        }
        var beam=b.offset(BEAM);var st=l.getBlockState(beam);
        if(out&&st.isAir())l.setBlock(beam,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,11),3);
        else if(!out&&st.is(Blocks.LIGHT))l.setBlock(beam,Blocks.AIR.defaultBlockState(),3);
    }
    /** The pickup and the sedan, told where they stand each tick (they are gone two seconds after they are not): at the tank for the
     * party, the pickup alone at the gin from the ride until the cut. With {@code on} false, both are let go. */
    static void cars(ServerLevel l,Night n,boolean on){
        var b=n.base;
        if(!on){for(var id:new UUID[]{n.pickup,n.sedan})if(id!=null&&l.getEntity(id) instanceof TownCar c)c.discard();n.pickup=n.sedan=null;return;}
        boolean gin=n.phase>=RIDE&&!(n.phase==RIDE&&n.since<RIDE_BLACK);
        n.pickup=car(l,n.pickup,TownCar.WHITE,at(b,gin?PICKUP_GIN:PICKUP),gin?GIN_YAW:PICKUP_YAW,true);
        if(!gin)n.sedan=car(l,n.sedan,TownCar.BLUE,at(b,SEDAN),SEDAN_YAW,false);
        else if(n.sedan!=null){if(l.getEntity(n.sedan) instanceof TownCar c)c.discard();n.sedan=null;}
    }
    static @Nullable UUID car(ServerLevel l,@Nullable UUID id,int kind,Vec3 at,float yaw,boolean lit){
        if(id!=null&&l.getEntity(id) instanceof TownCar c&&c.isAlive()){c.place(at,yaw);c.lit(lit);return id;}
        var body=CalderTraffic.make(l,kind,at,yaw);if(body==null)return null;
        body.lit(lit);return body.getUUID();
    }

    // ------------------------------------------------------------------------------------------------ the ride
    /** Deek takes them: everyone at the tank rides, the view going black. */
    static void ride(ServerLevel l,Night n){
        var tank=at(n.base,TheListSite.TANK_AT);
        n.riders.clear();if(n.slasher!=null)n.riders.add(n.slasher);
        for(var q:TheList.readers(l,n.base))if(q.position().distanceTo(tank)<RIDE_REACH)n.riders.add(q.getUUID());
        for(var id:n.riders){var q=l.getServer().getPlayerList().getPlayer(id);if(q!=null)HousePackets.send(q,new HouseFadePayload(20,30,30));}
        n.phase=RIDE;n.since=0;save(l.getServer(),n);
    }
    static boolean ride(ServerLevel l,Night n,List<ServerPlayer> here){
        cars(l,n,true);
        if(n.since<RIDE_BLACK)return false;
        var b=n.base;props(l,b,false);
        int i=1;
        for(var id:n.riders){
            var q=l.getServer().getPlayerList().getPlayer(id);if(q==null||q.level()!=l||!q.isAlive())continue;
            var to=at(b,id.equals(n.slasher)?RIDERS[0]:RIDERS[Math.min(RIDERS.length-1,i++)]);
            q.teleportTo(l,to.x,to.y,to.z,180,0);q.setDeltaMovement(Vec3.ZERO);
        }
        n.time=Math.max(n.time,TWO);n.phase=SEED;n.since=0;cars(l,n,true);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the seed house
    static boolean seed(ServerLevel l,Night n,List<ServerPlayer> here){
        cars(l,n,true);
        var b=n.base;
        if(n.since==OPEN)l.playSound(null,b.offset(DOOR_X,1,TheListSite.SEED_S),TheListRegistry.SLIDE.get(),SoundSource.BLOCKS,1F,1.05F);
        if(n.since>=OPEN&&n.since<=OPEN+6&&(n.since-OPEN)%3==0)door(l,b,(n.since-OPEN)/3+1);
        var deek=TheListPeople.find(l,b,TheListPeople.DEEK);
        var p=slasher(l,n);
        if(n.since==TOLD&&deek!=null&&p!=null){deek.sayTo(p,TheListTexts.NAMES[TheListPeople.DEEK],TheListTexts.GO_IN);deek.speaks(TheListTexts.GO_IN);}
        if(n.since<TOLD||p==null)return false;
        var r=p.position().subtract(Vec3.atLowerCornerOf(b));
        if(!inSeedHouse(r)||r.z>TheListSite.SEED_S-INSIDE)return false;
        // Only the one he picked: anybody else inside is told to come out first.
        for(var q:here)if(q!=p&&inSeedHouse(q.position().subtract(Vec3.atLowerCornerOf(b)))){
            if(deek!=null&&n.since%60==0){deek.sayTo(q,TheListTexts.NAMES[TheListPeople.DEEK],TheListTexts.ONE_AT_A_TIME);deek.speaks(TheListTexts.ONE_AT_A_TIME);}
            return false;
        }
        n.phase=SHUT;n.since=0;
        l.playSound(null,b.offset(DOOR_X,1,TheListSite.SEED_S),TheListRegistry.SLIDE.get(),SoundSource.BLOCKS,1.2F,.95F);
        return true;
    }
    public static boolean inSeedHouse(Vec3 r){
        return r.x>TheListSite.SEED_W+1&&r.x<TheListSite.SEED_E&&r.z<TheListSite.SEED_S&&r.z>TheListSite.SEED_N+1&&r.y>-.5&&r.y<TheListSite.SEED_TOP;
    }
    /**
     * The seed house's door this far open, in its three columns (0 shut, 3 open). It rolls east on its rail, so the doorway clears from
     * its west column and the door's panel stands outside against the wall east of the doorway, a column of it for each column open.
     */
    static void door(ServerLevel l,BlockPos b,int open){
        int z=TheListSite.SEED_S,west=DOOR_X-1;
        for(int i=0;i<3;i++)for(int y=0;y<=2;y++){
            var panel=TheListBlock.of(TheListBlock.Kind.SEED,Direction.SOUTH,y==2?5:4);
            var cell=b.offset(west+i,y,z);var want=i<open?Blocks.AIR.defaultBlockState():panel;
            if(!l.getBlockState(cell).equals(want))l.setBlock(cell,want,Block.UPDATE_ALL);
            var rail=b.offset(DOOR_X+2+i,y,z+1);var st=l.getBlockState(rail);
            if(i<open&&st.isAir())l.setBlock(rail,panel,Block.UPDATE_ALL);
            else if(i>=open&&st.equals(panel))l.setBlock(rail,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);
        }
    }
    /** The seed house put to rights or left dark: the door shut or not, the lamp over it and the bulb inside lit or out. */
    static void seedHouse(ServerLevel l,BlockPos b,boolean shut,boolean lit){
        if(!l.isLoaded(b.offset(DOOR_X,0,TheListSite.SEED_S)))return;
        if(shut)door(l,b,0);
        var lamp=b.offset(LAMP);var bulb=b.offset(BULB);
        l.setBlock(lamp,lit?TheListBlock.of(TheListBlock.Kind.SEED,Direction.SOUTH,6):TheListBlock.of(TheListBlock.Kind.PARTY,Direction.SOUTH,6),Block.UPDATE_ALL);
        l.setBlock(bulb,lit?TheListBlock.of(TheListBlock.Kind.FIXTURE,Direction.NORTH,3):TheListBlock.of(TheListBlock.Kind.PARTY,Direction.NORTH,7),Block.UPDATE_ALL);
    }

    // ------------------------------------------------------------------------------------------------ shut
    static boolean shut(ServerLevel l,Night n,List<ServerPlayer> here){
        cars(l,n,true);
        var b=n.base;int t=n.since;
        if(t>=1&&t<=5&&t%2==1)door(l,b,(5-t)/2);
        if(t==BOLT)l.playSound(null,b.offset(DOOR_X,1,TheListSite.SEED_S+1),TheListRegistry.BOLT.get(),SoundSource.BLOCKS,1F,1F);
        for(int i=0;i<SHUT_AT.length;i++)if(t==SHUT_AT[i]){
            int who=TheListTexts.SHUT_SAYS[i];var a=TheListPeople.find(l,b,who);
            if(a!=null){a.say(TheListTexts.NAMES[who],TheListTexts.SHUT_LINES[i]);a.speaks(TheListTexts.SHUT_LINES[i]);}
        }
        if(t==DARK){seedHouse(l,b,false,false);l.playSound(null,b.offset(LAMP),SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.8F,.7F);}
        var p=slasher(l,n);
        if(t>=AUGER&&t<CUT){
            var trench=b.offset(DOOR_X+1,0,(TheListSite.SEED_N+TheListSite.SEED_S)/2);
            if((t-AUGER)%80==0)l.playSound(null,trench,TheListRegistry.AUGER.get(),SoundSource.BLOCKS,1.3F,1F);
            if((t-AUGER)%40==10)l.playSound(null,trench,TheListRegistry.SEED.get(),SoundSource.BLOCKS,1F,.9F+l.getRandom().nextFloat()*.2F);
            if(t%2==0){
                var seed=new BlockParticleOption(ParticleTypes.FALLING_DUST,TheListBlock.of(TheListBlock.Kind.SEED,Direction.NORTH,1));
                for(int z=TheListSite.SEED_N+1;z<TheListSite.SEED_S-1;z+=2)
                    l.sendParticles(seed,b.getX()+TRENCH_X,b.getY()+.6,b.getZ()+z+.5,2,.4,.1,.4,.02);
            }
            // The seed goes toward the trench and takes whoever is on it, a little more each second.
            if(p!=null&&inSeedHouse(p.position().subtract(Vec3.atLowerCornerOf(b)))){
                double k=Math.min(1,(t-AUGER)/60.0)*.045;
                var r=p.position().subtract(Vec3.atLowerCornerOf(b));
                double tz=Math.max(TheListSite.SEED_N+1.5,Math.min(TheListSite.SEED_S-1.5,r.z));
                var to=new Vec3(TRENCH_X-r.x,0,tz-r.z);
                if(to.lengthSqr()>.01){to=to.normalize().scale(k);p.push(to.x,0,to.z);p.hurtMarked=true;}
            }
        }
        if(t==FADE)for(var q:here){
            var r=q.position().subtract(Vec3.atLowerCornerOf(b));
            if(q.getUUID().equals(n.slasher)||n.riders.contains(q.getUUID())||Math.abs(r.x-DOOR_X)<40&&Math.abs(r.z-TheListSite.SEED_S)<40)
                HousePackets.send(q,new HouseFadePayload(20,80,40));
        }
        if(t<CUT)return t==DARK;
        cut(l,n,here);
        return true;
    }
    /** The cut: three in the morning. The one who went in comes to under the tower; the five and the trucks are gone. */
    static void cut(ServerLevel l,Night n,List<ServerPlayer> here){
        var b=n.base;
        cars(l,n,false);
        n.time=Math.max(n.time,THREE);n.phase=AFTER;n.since=0;
        var d=LabyrinthData.get(l.getServer());
        var p=slasher(l,n);
        if(p!=null){
            var to=at(b,UNDER);
            p.teleportTo(l,to.x,to.y,to.z,180,0);p.setDeltaMovement(Vec3.ZERO);
            TheListBody.comeTo(p,LIE);
            var own=LiteraryVignettes.personal(d,p.getUUID(),TheList.PLACE);own.putBoolean("CameTo1",true);LiteraryVignettes.save(d,p.getUUID(),TheList.PLACE,own);
        }
        for(var q:here)if(!q.getUUID().equals(n.slasher)&&n.riders.contains(q.getUUID())){
            var own=LiteraryVignettes.personal(d,q.getUUID(),TheList.PLACE);own.putBoolean("SawShut1",true);LiteraryVignettes.save(d,q.getUUID(),TheList.PLACE,own);
        }
    }

    // ------------------------------------------------------------------------------------------------ after
    static boolean after(ServerLevel l,Night n,List<ServerPlayer> here){
        if(n.since==WAKE)for(var q:here){
            if(q.getUUID().equals(n.slasher))TheList.cue(q,TheListTexts.CAME_TO);
            else if(n.riders.contains(q.getUUID()))TheList.cue(q,TheListTexts.ALONE);
        }
        // One who went in and was not in town at the cut comes to under the tower the next time they are.
        var p=slasher(l,n);
        if(p!=null&&!TheListBody.has(p)){
            var to=at(n.base,UNDER);p.teleportTo(l,to.x,to.y,to.z,180,0);HousePackets.send(p,new HouseFadePayload(0,40,40));TheListBody.comeTo(p,LIE);
        }
        // 0.4.707: Amber at her window, a page for each reader who passes in the street; and first light, if it comes before bed.
        if(n.since>WAKE)for(var q:here)pages(l,n,q);
        if(n.time>=LIGHT)dawn(l,n,false);
        return false;
    }

    // ------------------------------------------------------------------------------------------------ the walk home (0.4.707)
    /**
     * Amber at her window: a reader out in the street in front of her house, near enough to read a page, sees her hold one up to the
     * glass, and a little later a second. Each reader's pages are their own, once a night: the one who went in is told about the sack (or,
     * with it off, about their face); anyone else is asked what happened. The second page is the same for everyone: tomorrow, the back
     * door, after ten.
     */
    static void pages(ServerLevel l,Night n,ServerPlayer q){
        if(!LiteraryVignettes.participant(q))return;
        var d=LabyrinthData.get(l.getServer());var own=LiteraryVignettes.personal(d,q.getUUID(),TheList.PLACE);
        int stage=own.getInt("Pages1");if(stage>=2)return;
        long now=l.getGameTime();
        if(stage==0){
            var r=q.position().subtract(Vec3.atLowerCornerOf(n.base));
            if(TheListSite.indoors(r)||r.z<=TheListSite.AMBER_S||Vec3.atCenterOf(WINDOW).distanceTo(r)>PAGE_REACH)return;
            var amber=TheListPeople.find(l,n.base,TheListPeople.AMBER);
            if(amber==null||amber.position().distanceTo(at(n.base,AT_WINDOW))>1.5)return;
            boolean him=q.getUUID().equals(n.slasher);
            TheList.cue(q,!him?TheListTexts.PAGE_WHAT:TheListSack.worn(q)?TheListTexts.PAGE_SACK:TheListTexts.PAGE_FACE);
            own.putInt("Pages1",1);own.putLong("PageAt1",now);LiteraryVignettes.save(d,q.getUUID(),TheList.PLACE,own);
            return;
        }
        if(now-own.getLong("PageAt1")<PAGE_GAP)return;
        TheList.cue(q,TheListTexts.PAGE_TOMORROW);
        own.putInt("Pages1",2);LiteraryVignettes.save(d,q.getUUID(),TheList.PLACE,own);
    }
    /** A hand on the reader's own bed: for the one who went in, after the cut, it is lying down, and night one ends. True if it did. The
     * reader's record in hand (saved by the hand's caller after this) keeps it too. */
    public static boolean bed(ServerPlayer p,BlockPos b,CompoundTag own){
        var n=night(p.server,b);
        if(n!=null&&TheListNights.hunting(n.phase))return TheListNights.bed(p,n,own);
        if(n==null||n.phase!=AFTER||n.since<=WAKE||!p.getUUID().equals(n.slasher)||!LiteraryVignettes.participant(p))return false;
        dawn(p.serverLevel(),n,true);
        own.putInt("Night",Math.max(1,own.getInt("Night")));if(!own.contains("Light1"))own.putString("Light1","bed");
        return true;
    }
    /**
     * First light: night one is over. Gone to bed, it comes behind a black that falls on everyone in town; otherwise it comes up on its own
     * clock. Each reader in town, and the one who went in wherever they are, keeps that they have had night one and where they were when it
     * got light.
     */
    static void dawn(ServerLevel l,Night n,boolean bed){
        n.time=LIGHT;n.phase=DONE;n.since=0;n.bed=bed;
        var d=LabyrinthData.get(l.getServer());
        if(bed)for(var q:inTown(l,n.base))HousePackets.send(q,new HouseFadePayload(20,60,40));
        for(var q:TheList.readers(l,n.base))if(LiteraryVignettes.participant(q))light(d,q.getUUID(),where(n,q));
        if(n.slasher!=null&&slasher(l,n)==null)light(d,n.slasher,"away");
        save(l.getServer(),n);TheListPeople.ensure(l,n.base);send(l,n,true);
    }
    /** Where a reader was when it got light: in bed (the one who went in, gone to bed), in the house, or out in the town. */
    static String where(Night n,ServerPlayer q){
        if(n.bed&&q.getUUID().equals(n.slasher))return "bed";
        return TheListSite.inHouse(q.position().subtract(Vec3.atLowerCornerOf(n.base)))?"house":"town";
    }
    static void light(LabyrinthData d,UUID id,String where){
        var own=LiteraryVignettes.personal(d,id,TheList.PLACE);
        own.putInt("Night",Math.max(1,own.getInt("Night")));if(!own.contains("Light1"))own.putString("Light1",where);
        LiteraryVignettes.save(d,id,TheList.PLACE,own);
    }
    /** After first light: each reader in town is told, past the black for a reader who went to bed. */
    static boolean light(ServerLevel l,Night n,List<ServerPlayer> here){
        if(n.since!=LIGHT_CUE)return false;
        for(var q:here){
            if(!LiteraryVignettes.participant(q))continue;
            if(!q.getUUID().equals(n.slasher)){TheList.cue(q,TheListTexts.LIGHT_OTHER);continue;}
            var w=where(n,q);
            TheList.cue(q,w.equals("bed")?TheListTexts.LIGHT_BED:w.equals("house")?TheListTexts.LIGHT_HOUSE:TheListTexts.LIGHT_OUT);
        }
        return false;
    }

    /** Nobody near enough to hear: the box round the tank the five stand in, for a check. */
    public static AABB tank(BlockPos b){return new AABB(at(b,TheListSite.TANK_AT),at(b,TheListSite.TANK_AT)).inflate(12,4,12);}
}
