package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Anansi's Goatman (0.4.53). The latecomer walks the trail as a child, past two shapes standing with their backs to
 * it, to a camp of five cousins. One of the five is not a cousin. Nothing says which, or that any is wrong: there is
 * a pan of brats for everyone who should be there, one more child than that at the table, and a cousin who wants to
 * know who had two. One real cousin goes off for gas at dusk and comes back calling, before the woods go quiet. The
 * one who doesn't belong goes and stands by the fire with its back to the trailer, and when the woods go quiet it comes
 * for the door, moving only while nobody watches. The bathroom window has been left open.
 *
 * <p>The night is right if the cousin who went for gas was let in before the quiet, the thing was kept out (by the
 * door and the window), and nobody opened the door to it once it was dark. Then every child who sat out the whole
 * night resolves the story and takes home the count. If it got in, or the cousin was left outside, it walks home with
 * everyone there; whoever opens the door to it at night is taken from the trailer, and it with them. None of that is a
 * death or a loss of anything carried: it is the haunting in {@link GoatmanHaunt}, and it ends with a night done right.
 */
public final class GoatmanVignette {
    public static final String ID="goatman",ACTOR="TrailerChild",ROUND="TrailerRound",INDEX="TrailerIndex";
    public static final int PATH=1,GATHERING=2,VIGIL=3,DAWN=4,MAX_PLAYERS=16,VERSION=453;
    public static final int GATHER_TICKS=1800,VIGIL_TICKS=1600;
    public static final int SUPPER0465=1500,LEAVES0465=1200,BACK0465=1900,SILENCE0465=2700,GATHER0465=3300,VIGIL0465=2500;
    private static final int WINDOW0465=2100,WINDOW_FOR0465=200;
    /**
     * 0.4.123, the owner's pacing for new rounds (October 25: the sequence still drags). The others come in with the first
     * plate and the cousin goes for gas with the last, as before; once supper is on the table there is only a little talk
     * while everyone eats. Then he knocks at the shut door asking to be let in, and the reader decides. A few seconds after
     * he is in (or his voice stops) the woods go quiet and night falls, and the thing outside starts at once: each of its
     * words comes with its own run of knocks on the door while the same body circles the windows. Older saved rounds keep
     * the evening they began.
     */
    public static final int BRISK_SUPPER=600,BRISK_COOK_TALK=420;
    /** After supper is on the table, by ticks since it began: talk while they eat, the door pulled to, his call from the trail. */
    public static final int TABLE_MUSTARD=30,TABLE_COLD=80,TABLE_SHUT=130,TABLE_PLACE=170,TABLE_BACK=210,TABLE_LATEST=1400;
    /** His three lines at the door (one every RUNNER_KNOCK_EVERY) before his voice stops; the quiet and the dark follow his coming in by these. */
    public static final int KNOCK_FOR=300,QUIET_AFTER=40,DARK_AFTER=80;
    /** The night: the shortest whole vigil, and how soon morning follows a bathroom window found shut. */
    public static final int BRISK_VIGIL=900,BRISK_REFUSED=60;
    /** Where on the trail he goes out of sight for gas, and where he is heard calling on the way back. */
    private static final double BRISK_GONE=48,BRISK_CALL=45;
    private static final double JOG=.12;
    public static boolean fresh(CompoundTag r){return r.getBoolean("Fresh0465");}
    public static boolean brisk(CompoundTag r){return fresh(r)&&r.getBoolean("Brisk04123");}
    public static int supperAt(CompoundTag r){return brisk(r)?BRISK_SUPPER:fresh(r)?SUPPER0465:SUPPER;}
    private static int gatherTicks(CompoundTag r){return brisk(r)?(r.contains("NightAt04123")?r.getInt("NightAt04123"):Integer.MAX_VALUE):fresh(r)?GATHER0465:GATHER_TICKS;}
    public static int vigilTicks(CompoundTag r){return brisk(r)?BRISK_VIGIL:fresh(r)?VIGIL0465:VIGIL_TICKS;}
    /** The earliest night tick at which the bathroom window is tried, once every circuit and approach leg is done. */
    public static int windowFrom(CompoundTag r){return brisk(r)?0:WINDOW0465;}
    /** How long after the window is tried morning may come: briefly if it was found shut, else the whole attempt. */
    public static int dawnAfterWindow(CompoundTag r){return !brisk(r)?VIGIL0465-WINDOW0465:r.getBoolean("WindowRefused04123")?BRISK_REFUSED:WINDOW_FOR0465;}
    /** The evening, by the gathering clock. */
    public static final int WINDOW_HINT=880,RUNNER_LEAVES=360,SUPPER=600,GRUMBLE=1000,EXTRA_OUT=1100,RUNNER_BACK=1300,SILENCE=1500;
    /** Rounds saved before 0.4.64 keep the evening they began: the cousins serve themselves, and the window is mentioned early. */
    static final int OLD_WINDOW_HINT=200,SERVE_HINT=120;
    /** How long it stays at the window, scraping, before it goes back to its place by the fire. */
    static final int WINDOW_STAY=40;
    /** The night, by the vigil clock. */
    public static final int KNOCK_FROM=60,KNOCK_UNTIL=1150,WINDOW_TRY=700,CHECK=720,BEDTIME=1000,KEEN=1250;
    public static final int FACE=1,SILENT=2,LATE_LAUGH=4,STILL=8,FIRELIGHT=16,HEAD=32,PET=64;
    /** The cousin who went for gas, in an ordinary voice; and the same words from the thing that heard him. */
    public static final int RUNNER_KNOCK_EVERY=100;
    public static final String[] RUNNER_LINES={"Hey! Let me in!","Come on, stop playing! Open the door!","It's me! It's getting dark out here!"};
    public static final String[] LEGACY_MIMIC_LINES={"Let me. In.","in","stop playing","Let me in stop. playing","It's. me.","open the. Door."};
    public static final String[] MIMIC_LINES={"let me in","let. me. in","seriously i'm not playing","i'm not fucking playing let me in","i'm not fucking playing","let me in","let me in let me in let me in let me in"};
    private static final String[] CAMP_STORY={"So he turns on the porch light, and all the knocking stops.","You stole that off my brother.","He stole it off me.","What happened to the guy?","I don't know. I fell asleep.","That's your ending?","Fine. He got up and made toast."};
    /** Where the cousin who went for gas is, and where the one who doesn't belong is. */
    public static final int R_HOME=0,R_LEAVING=1,R_AWAY=2,R_RETURNING=3,R_KNOCKING=4,R_INSIDE=5,R_LOST=6;
    public static final int X_ACTIVITY=0,X_SUPPER=1,X_FIRE=2,X_APPROACH=3,X_DOOR=4,X_INSIDE=5,X_WINDOW=6,X_FLOOR=7;
    private static final ResourceLocation CHILD_SCALE=ResourceLocation.fromNamespaceAndPath(io.github.knaitoe.theoldesthouse.TheOldestHouse.MOD_ID,"trailer_child_scale");
    private static final double WALK=.075,RUN=.18,CREEP=.06;
    public static final int MEAL_BREATH=220;
    public static final double IMPOSTOR_RUN=.52;
    private static double impostorRun(CompoundTag r){return brisk(r)?IMPOSTOR_RUN:RUN;}
    /** Inner frame of the three-pixel-thick south-facing native door. */
    private static final double EXCHANGE_PLANE=GoatmanWoods.DOOR.getZ()+3/32D;
    /** For the native tests: the cousins cover their routes at once, through every arrival, as though they had walked. */
    private static boolean hurried;
    public static int count(CompoundTag r){return r.contains("Cousins0464")?Math.max(5,Math.min(8,r.getInt("Cousins0464"))):5;}
    /**
     * 0.4.150, the owner's campers: a second reader and every one after takes the place of a cousin, so the camp has as many
     * children as it would for one reader alone. The cousin whose place is taken is never staged; the reader eats from that
     * cousin's plate, sits in that cousin's chair and sleeps in that cousin's bunk, and the count treats them as that cousin.
     * Never the thing, the one who goes for gas, or the two who carry the jars. Saved in the round, so it lasts the evening.
     */
    public static final String PLAYS="Plays0150",PLAYED="Played0150";
    /**
     * The owner's note: the second reader takes the place of the cousin who goes for gas and fills the generator themselves
     * (brisk rounds). Their return to the camper still sends the thing out past them.
     */
    public static final String GAS="GasReader0150",GAS_NAME="GasName0150";
    /**
     * How the cousins speak of the gas reader (owner): "he" or "she" only when the coatroom has settled how they present,
     * otherwise their name and "they". Saved when they are sent out.
     */
    public static final String GAS_FORM="GasForm0150";
    public static String gasForm(CompoundTag r){String form=r.getString(GAS_FORM);return form.equals("he")||form.equals("she")?form:"they";}
    /** The gas job is a reader's: a brisk round with its gas reader. A round made old-style by a fixture has none. */
    public static boolean byReader(CompoundTag r){return brisk(r)&&r.hasUUID(GAS);}
    /** A place taken, and still one a reader may take: never the thing or a carrier, and the one who goes for gas only as the gas reader. */
    public static boolean playedBy(CompoundTag r,int i){return fresh(r)&&i>=0&&i<count(r)&&(r.getInt(PLAYED)&(1<<i))!=0&&i!=r.getInt("Wrong")&&(i!=r.getInt("Runner")||byReader(r))&&!GoatmanMeal.carriers(r).contains(i);}
    public static int played(CompoundTag r){if(r.getInt(PLAYED)==0)return 0;int n=0;for(int i=0;i<count(r);i++)if(playedBy(r,i))n++;return n;}
    /** The children who should be at the trailer: the real cousins still staged, and every reader. */
    public static int expected(CompoundTag r,int readers){return count(r)-1+readers-played(r);}
    /** Whose place goes first: the cousins whose fear keeps them in their seats or pacing; the one who covers their ears, last. */
    private static final List<Integer> STAND_IN_ORDER=List.of(GoatmanFear.TREMBLE,GoatmanFear.PACE,GoatmanFear.COMFORT,GoatmanFear.STARTLE,GoatmanFear.WATCH,GoatmanFear.BRACE,GoatmanFear.EARS);
    /** A cousin whose place a reader may take, or -1 when every such place is taken. */
    static int standIn(CompoundTag r){
        int runner=r.getInt("Runner");if(brisk(r)&&!r.hasUUID(GAS)&&(r.getInt(PLAYED)&(1<<runner))==0)return runner;
        var carriers=GoatmanMeal.carriers(r);int best=-1,rank=Integer.MAX_VALUE;
        for(int i=0;i<count(r);i++){
            if(i==r.getInt("Wrong")||i==r.getInt("Runner")||carriers.contains(i)||(r.getInt(PLAYED)&(1<<i))!=0)continue;
            int at=STAND_IN_ORDER.indexOf(GoatmanFear.role(i,r));if(at<rank){rank=at;best=i;}
        }
        return best;
    }
    private GoatmanVignette(){}

    // ------------------------------------------------------------------------------------------------ state
    public static CompoundTag run(LabyrinthData d){return d.state(ID).getCompound("Run").copy();}
    public static CompoundTag personal(LabyrinthData d,UUID id){return d.state(ID).getCompound("Players").getCompound(id.toString()).copy();}
    static void saveRun(LabyrinthData d,CompoundTag r){CompoundTag all=d.state(ID);all.put("Run",r);d.setState(ID,all);}
    static void savePersonal(LabyrinthData d,UUID id,CompoundTag p){CompoundTag all=d.state(ID),players=all.getCompound("Players");players.put(id.toString(),p);all.put("Players",players);d.setState(ID,all);}
    /** A trailer carved again starts its next evening from nothing; every child's own record stays. */
    public static void forgetRun(MinecraftServer s){LabyrinthData d=LabyrinthData.get(s);CompoundTag all=d.state(ID);if(!all.contains("Run"))return;all.remove("Run");d.setState(ID,all);}
    public static boolean canDeal(LabyrinthData d,UUID id){return !WitnessAccount.has(d,id,WitnessAccount.Story.GOATMAN)&&!personal(d,id).getBoolean("Respawn");}
    public static @Nullable BlockPos base(MinecraftServer s){return IndianLakeRooms.base(s,LabyrinthPlace.GOATMAN);}
    private static boolean inBounds(ServerPlayer p){BlockPos b=base(p.server);return b!=null&&p.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR&&p.level().dimension().equals(HouseDimensions.INTERIOR)&&IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN).contains(p.position());}
    public static boolean inside(ServerPlayer p){return p.isAlive()&&inBounds(p);}
    private static Vec3 rel(Entity e,BlockPos b){return e.position().subtract(b.getX(),b.getY(),b.getZ());}
    private static Vec3 abs(Vec3 rel,BlockPos b){return rel.add(b.getX(),b.getY(),b.getZ());}
    static List<ServerPlayer> visitors(ServerLevel l,BlockPos b){return IndianLakeRooms.visitors(l,b,LabyrinthPlace.GOATMAN).stream().filter(p->p.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR).toList();}
    public static void build(MinecraftServer s,ServerLevel l,BlockPos b){GoatmanWoods.build(l,b);}
    public static boolean canEnter(ServerPlayer p){
        if(!canDeal(LabyrinthData.get(p.server),p.getUUID()))return false;
        CompoundTag r=run(LabyrinthData.get(p.server));BlockPos b=base(p.server);ServerLevel l=p.server.getLevel(HouseDimensions.INTERIOR);
        boolean empty=b==null||l==null||visitors(l,b).isEmpty();
        if(!empty&&!r.getCompound("Cohort").contains(p.getUUID().toString())&&r.getCompound("Cohort").getAllKeys().size()>=MAX_PLAYERS)return false;
        CompoundTag own=r.getCompound("Cohort").getCompound(p.getUUID().toString());
        // A latecomer waits out a supper already under way, but an evening everyone has left is begun again on entry.
        if(!empty&&own.isEmpty()&&r.getInt("Phase")==GATHERING&&(r.getInt("Clock")>=supperAt(r)||r.getBoolean("MealFrozen0465")))return false;
        if(r.getInt("Phase")==DAWN)return empty;
        return r.getInt("Phase")!=VIGIL||empty||own.getBoolean("Active")&&!own.getBoolean("Failed");
    }
    private static CompoundTag newRun(ServerPlayer p){
        CompoundTag r=new CompoundTag();r.putUUID("Id",UUID.randomUUID());r.putInt("Phase",PATH);r.putInt("Version",VERSION);
        r.putInt("Cousins0464",8);r.putBoolean("PlayerServes0464",true);r.putBoolean("Fresh0465",true);r.putBoolean("Brisk04123",true);int wrong=p.getRandom().nextInt(8),runner=(wrong+1+p.getRandom().nextInt(7))%8;r.putInt("Wrong",wrong);r.putInt("Runner",runner);
        List<Integer> tells=new ArrayList<>(List.of(FACE,SILENT,LATE_LAUGH,STILL,FIRELIGHT,HEAD,PET));
        Collections.shuffle(tells,new Random(p.getRandom().nextLong()));int mask=0,n=2+p.getRandom().nextInt(2);for(int tell:tells){if(tell==HEAD&&(mask&FACE)!=0||tell==FACE&&(mask&HEAD)!=0)continue;mask|=tell;if(Integer.bitCount(mask)==n)break;}r.putInt("Tells",mask);
        List<Integer> skins=new ArrayList<>(List.of(0,1,2,3,4,6,7,8));Collections.shuffle(skins,new Random(p.getRandom().nextLong()));for(int i=0;i<count(r);i++)r.putInt("Skin"+i,skins.get(i));
        var disguises=new ArrayList<>(List.of(0,1,2,3,4,5,6,7,8));Collections.shuffle(disguises,new Random(p.getRandom().nextLong()));r.putIntArray("StalkDeck0465",disguises);
        return r;
    }
    public static void onArrive(ServerPlayer p,LabyrinthPlace place){if(place==LabyrinthPlace.GOATMAN)enter(p);}
    /** Direct arrivals and reconnects use the same saved enrollment. No credit is awarded here. */
    public static boolean enter(ServerPlayer p){
        if(!inside(p)||!canDeal(LabyrinthData.get(p.server),p.getUUID()))return false;
        LabyrinthData d=LabyrinthData.get(p.server);BlockPos b=base(p.server);ServerLevel l=p.serverLevel();CompoundTag r=run(d),cohort=r.getCompound("Cohort");
        boolean member=cohort.contains(p.getUUID().toString());
        final CompoundTag initialCohort=cohort;
        boolean other=visitors(l,b).stream().anyMatch(peer->peer!=p&&initialCohort.contains(peer.getUUID().toString())&&!initialCohort.getCompound(peer.getUUID().toString()).getBoolean("Failed"));
        if(r.getInt("Phase")==0||r.getInt("Version")!=VERSION&&!other||r.getInt("Phase")==DAWN&&!other||!other&&(!member||cohort.getCompound(p.getUUID().toString()).getBoolean("Failed")||!cohort.getCompound(p.getUUID().toString()).getBoolean("Active"))){
            r=newRun(p);cohort=new CompoundTag();member=false;reset(l,b);
        }
        if(!member&&(r.getInt("Phase")==VIGIL||r.getInt("Phase")==DAWN||r.getInt("Phase")==GATHERING&&(r.getInt("Clock")>=supperAt(r)||r.getBoolean("MealFrozen0465"))||cohort.getAllKeys().size()>=MAX_PLAYERS))return false;
        CompoundTag own=cohort.getCompound(p.getUUID().toString());
        if(!member){own.putBoolean("Active",true);own.putDouble("Progress",0);own.putInt("Vigil",0);own.putBoolean("Arrived",false);
            // 0.4.150: the first in is the latecomer, whose bed the thing comes to first.
            if(cohort.isEmpty())own.putBoolean("Latecomer0150",true);
            // 0.4.150: arriving with the first reader, before anyone reaches the camp, a reader takes a cousin's place.
            if(fresh(r)&&r.getInt("Phase")==PATH&&!cohort.isEmpty()){int role=standIn(r);if(role>=0){own.putInt(PLAYS,role);r.putInt(PLAYED,r.getInt(PLAYED)|1<<role);
                if(role==r.getInt("Runner")){r.putUUID(GAS,p.getUUID());r.putString(GAS_NAME,p.getGameProfile().getName());}}}}
        own.putBoolean("Active",true);cohort.put(p.getUUID().toString(),own);r.put("Cohort",cohort);saveRun(d,r);
        if(!member)p.displayClientMessage(Component.literal(own.contains(PLAYS)?"You're late too. The others are already at the trailer, and they've kept your place.":"You're late. Your cousins are already at the trailer."),false);
        scale(p,true);IndianLakeRooms.keepLoaded(l,b,LabyrinthPlace.GOATMAN);GoatmanBedtime.restore(p,r);
        if(!stage(l,b,r)){r.putBoolean("StagePending0469",true);saveRun(d,r);}
        GoatmanWoods.supplies(l,b,expected(r,cohort.getAllKeys().size()));if(fresh(r))GoatmanWoods.supperChairs(l,b,expected(r,cohort.getAllKeys().size()));return true;
    }
    /** A fresh evening: no actors, day, the door open, the bathroom window propped, the porch light on, the pan and the plates empty. */
    private static void reset(ServerLevel l,BlockPos b){
        removeActors(l,b);GoatmanWoods.atmosphere(l,b,false);GoatmanWoods.door(l,b,true);GoatmanWoods.setWindow(l,b,false);GoatmanWoods.bathroomDoor(l,b,false);GoatmanWoods.porchLight(l,b,true);
        GoatmanMeal.reset(l,b);GoatmanWoods.pan(l,b,0);for(int s=0;s<24;s++)GoatmanWoods.served(l,b,s,false);
        if(l.getBlockEntity(b.offset(GoatmanWoods.DOOR)) instanceof TrailerDoorBlockEntity door)door.resetStress();
    }
    private static void scale(ServerPlayer p,boolean active){
        var attribute=p.getAttribute(Attributes.SCALE);if(attribute==null)return;
        if(active){if(!attribute.hasModifier(CHILD_SCALE))attribute.addTransientModifier(new AttributeModifier(CHILD_SCALE,-.3,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
        else attribute.removeModifier(CHILD_SCALE);
        // The body and eye take the new size now rather than on the player's next tick, so aim and reach agree with it at once.
        p.refreshDimensions();
    }
    public static boolean childScale(ServerPlayer p){return p.getAttribute(Attributes.SCALE)!=null&&p.getAttribute(Attributes.SCALE).hasModifier(CHILD_SCALE);}
    public static void depart(ServerPlayer p){
        scale(p,false);HousePackets.send(p,new GoatmanScenePayload(0,0,0,0));
        LabyrinthData d=LabyrinthData.get(p.server);CompoundTag r=run(d),cohort=r.getCompound("Cohort");String key=p.getUUID().toString();
        if(cohort.contains(key)){CompoundTag own=cohort.getCompound(key);own.putBoolean("Active",false);own.putBoolean("Interrupted",true);cohort.put(key,own);r.put("Cohort",cohort);saveRun(d,r);}
        BlockPos b=base(p.server);if(b!=null)for(var girl:p.serverLevel().getEntitiesOfClass(GoatmanChild.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),c->c.viewer().filter(p.getUUID()::equals).isPresent()&&!c.getTags().contains("TrailerWalk")))girl.discard();
        // Departure is reported before the leaving player is teleported. Exclude
        // that player explicitly; a living enrolled peer keeps the shared supper.
        if(b!=null&&cohort.contains(key)&&p.level().dimension().equals(HouseDimensions.INTERIOR)){
            final CompoundTag enrolled=cohort;
            boolean peer=visitors(p.serverLevel(),b).stream().anyMatch(other->other!=p&&other.isAlive()&&enrolled.contains(other.getUUID().toString())&&enrolled.getCompound(other.getUUID().toString()).getBoolean("Active"));
            if(!peer){for(int seat=0;seat<24;seat++)GoatmanWoods.served(p.serverLevel(),b,seat,false);r.putInt("Plated0464",0);r.putInt("EmptyPlates0465",0);r.putInt("BurntPlates0476",0);r.putBoolean("MealReady0465",false);saveRun(d,r);}
        }
        GoatmanHaunt.vanish(p,"hollow");
    }
    private static List<GoatmanChild> actors(ServerLevel l,BlockPos b){return l.getEntitiesOfClass(GoatmanChild.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),c->c.getTags().contains(ACTOR));}
    private static void removeActors(ServerLevel l,BlockPos b){for(var child:actors(l,b))if(!child.getTags().contains("TrailerWalk"))child.discard();}
    private static List<GoatmanChild> cousinsOf(ServerLevel l,BlockPos b){return actors(l,b).stream().filter(c->!c.girl()&&c.getPersistentData().getInt(INDEX)>=0).toList();}

    // ------------------------------------------------------------------------------------------------ where they go
    private record Spot(Vec3 at,float yaw,boolean inside,int pose,Vec3[] access,@Nullable BlockPos bed){}
    static final int STAND=0,SIT=1,LIE=2,COWER=3;
    static final Vec3 YARD=new Vec3(-1.5,0,-49.5),ENTRY=new Vec3(.5,1,-57);
    /** From the yard up the porch, the step and through the door. */
    static final Vec3[] UP={new Vec3(.5,0,-50.5),new Vec3(.5,0,-51.5),new Vec3(.5,.5,-52.6),new Vec3(.5,1,-53.8),new Vec3(.5,1,-54.5)};
    /** From the fire, clear of its seats, round the trailer's north-west corner to the window and up onto the sill. */
    static final Vec3[] WINDOW_PATH={new Vec3(-7.5,0,-52.5),new Vec3(-10.5,0,-54.5),new Vec3(-10.5,0,-58.5),new Vec3(-10,1.5,-58.5)};
    private static final Vec3[] WINDOW_ENTRY={new Vec3(10.25,0,-74.5),new Vec3(10.25,1.1,-74.5),new Vec3(10.25,2.55,-74.5),new Vec3(8.5,3.05,-74.5),new Vec3(7.5,3.05,-74.5),new Vec3(6.5,1,-75.5)};
    private static final float[] ENTRY_BEATS={.12F,.25F,.42F,.62F,.78F,1F};
    static final String[] ACTIVITY={"fireW","fireE","fireN","kitchen","shed","fireS","firelight","cupboard"};
    private static final Map<String,Spot> SPOTS=new HashMap<>();
    private static void spot(String key,Vec3 at,float yaw,boolean inside,int pose,Vec3... access){SPOTS.put(key,new Spot(at,yaw,inside,pose,access,null));}
    static{
        spot("fireW",new Vec3(-8.25,.5,-47.5),270,false,SIT,new Vec3(-4.5,0,-49.5),new Vec3(-8.5,0,-49.5));
        spot("fireE",new Vec3(-2.75,.5,-47.5),90,false,SIT);
        spot("fireN",new Vec3(-5.5,.5,-50.25),0,false,SIT,new Vec3(-5.5,0,-49.5));
        spot("fireS",new Vec3(-5.5,.5,-44.75),180,false,SIT,new Vec3(-3.5,0,-46));
        spot("byFire",new Vec3(-6,0,-49.6),0,false,STAND,new Vec3(-4.5,0,-49.6));
        spot("firelight",new Vec3(7.5,0,-48.5),90,false,STAND,new Vec3(3.5,0,-48.5));
        spot("shed",new Vec3(10.5,0,-62.5),270,false,STAND,new Vec3(3.5,0,-48.5),new Vec3(10.5,0,-48.5));
        // Up on the sill of the west window nearest the fire, facing in through the glass.
        spot("windowRun",new Vec3(-8.5,1.5,-58.5),270,false,STAND,WINDOW_PATH);
        spot("step",new Vec3(.5,1,-53.8),180,false,STAND,UP[0],UP[1],UP[2]);
        spot("condiments0",new Vec3(-1.45,1,-61.5),270,true,STAND,new Vec3(-3,1,-61.5));
        spot("condiments1",new Vec3(-1.45,1,-63.5),270,true,STAND,new Vec3(-3,1,-63.5));
        spot("kitchen",new Vec3(-4.5,1,-74.5),180,true,STAND,new Vec3(-3,1,-57.5),new Vec3(-3,1,-72.5));
        spot("cupboard",new Vec3(4,1,-70.5),270,true,STAND,new Vec3(4,1,-57.5),new Vec3(4,1,-70.5));
        spot("floor",new Vec3(.5,1.05,-58.5),0,true,LIE);
        spot("admitted131",new Vec3(4,1,-69.5),0,true,STAND);
        spot("checkDoor",new Vec3(.5,1,-56.2),0,true,STAND);
        spot("fearDoor",new Vec3(.5,1,-56.6),0,true,STAND);
        spot("fearWindow",new Vec3(-3.75,1,-58.75),90,true,STAND,new Vec3(-3,1,-57.5));
        spot("fearPaceA",new Vec3(-3,1,-61.5),180,true,STAND,new Vec3(-3,1,-57.5));
        spot("fearPaceB",new Vec3(-3,1,-70.5),0,true,STAND,new Vec3(-3,1,-57.5));
        spot("fearComfort",new Vec3(4,1,-66.5),90,true,STAND,new Vec3(4,1,-57.5));
        spot("fearEars",new Vec3(4.7,1,-71.5),180,true,COWER,new Vec3(4,1,-57.5),new Vec3(4,1,-71.5));
        spot("fearStartle",new Vec3(4.5,1,-59.5),0,true,STAND,new Vec3(4,1,-57.5));
        // 0.4.121: where the cousins draw back to, up by the door, facing the back, while something comes in by the bathroom.
        for(int i=0;i<8;i++)spot("shy"+i,new Vec3(i%2==0?-3.3:4.3,1,-57.6-(i/2)*1.2),180,true,STAND,new Vec3(i%2==0?-3:4,1,-57.5));
        for(int i=0;i<8;i++)spot("cower"+i,new Vec3(i%2==0?-3.8:4.8,1,-65.5-(i/2)*2),180,true,COWER,new Vec3(i%2==0?-3:4,1,-57.5),new Vec3(i%2==0?-3:4,1,-65.5-(i/2)*2));
        for(int s=0;s<10;s++){boolean west=s%2==0;double z=GoatmanWoods.CHAIR_Z[s/2]+.5;
            spot("seat"+s,new Vec3(west?-1.25:2.25,1.5,z),west?270:90,true,SIT,new Vec3(west?-3:4,1,-57.5),new Vec3(west?-3:4,1,z));}
        for(int k=0;k<24;k++){var head=GoatmanWoods.bunkHead(k);boolean west=k%2==0;double z=head.getZ()+.5;
            SPOTS.put("bunk"+k,new Spot(new Vec3(head.getX()+.5,head.getY()+.6875,z),west?270:90,true,LIE,new Vec3[]{new Vec3(west?-3:4,1,-57.5),new Vec3(west?-3:4,1,z),new Vec3(west?-3.6:4.4,1,z)},head));}
    }
    /** Points to walk from one spot to another, through the yard or the door as needed. */
    private static List<Vec3> route(String from,String to){
        Spot a=SPOTS.get(from),z=SPOTS.get(to);List<Vec3> out=new ArrayList<>();if(z==null)return out;
        // The fire and the window are next to each other: no need to go by the yard.
        if(from.equals("byFire")&&to.equals("windowRun")){out.addAll(List.of(WINDOW_PATH));out.add(z.at());return out;}
        if(from.equals("windowRun")&&to.equals("byFire")){for(int i=WINDOW_PATH.length-1;i>=0;i--)out.add(WINDOW_PATH[i]);out.add(z.at());return out;}
        if(a!=null){for(int i=a.access().length-1;i>=0;i--)out.add(a.access()[i]);
            if(a.inside()!=z.inside()){
                if(a.inside()){out.add(ENTRY);for(int i=UP.length-1;i>=0;i--)out.add(UP[i]);out.add(YARD);}
                else{out.add(YARD);out.addAll(List.of(UP));out.add(ENTRY);}
            }else out.add(a.inside()?ENTRY:YARD);
        }
        out.addAll(List.of(z.access()));out.add(z.at());return out;
    }
    /**
     * A goal can change partway along a walk, after a cousin has already gone out or come in. Planning again from the spot it
     * set out from would walk it back through the trailer wall, so it finishes the leg it is on and goes on from there; partway
     * out to the window, it goes back the way it came.
     */
    private static List<Vec3> onward(GoatmanChild c,String to){
        ListTag left=c.getPersistentData().getList("Route",Tag.TAG_COMPOUND);String going=goal(c);
        if(left.isEmpty()||going.equals(at(c))||!SPOTS.containsKey(going))return route(at(c),to);
        List<Vec3> out=new ArrayList<>();
        if(going.equals("windowRun")&&at(c).equals("byFire")){
            int passed=Math.min(WINDOW_PATH.length,WINDOW_PATH.length+1-left.size());for(int k=passed-1;k>=0;k--)out.add(WINDOW_PATH[k]);
            out.add(SPOTS.get("byFire").at());if(!to.equals("byFire"))out.addAll(route("byFire",to));return out;
        }
        for(int k=0;k<left.size();k++){CompoundTag t=left.getCompound(k);out.add(new Vec3(t.getDouble("X"),t.getDouble("Y"),t.getDouble("Z")));}
        out.addAll(route(going,to));return out;
    }
    private static void setRoute(GoatmanChild c,List<Vec3> points,String goal){
        ListTag list=new ListTag();for(Vec3 v:points){CompoundTag t=new CompoundTag();t.putDouble("X",v.x);t.putDouble("Y",v.y);t.putDouble("Z",v.z);list.add(t);}
        c.getPersistentData().put("Route",list);c.getPersistentData().putString("Goal",goal);
        if(c.isSleeping())c.stopSleeping();if(c.hasPose(Pose.SLEEPING))c.setPose(Pose.STANDING);c.pose(false);c.cower(false);
    }
    private static String at(GoatmanChild c){return c.getPersistentData().getString("At");}
    private static String goal(GoatmanChild c){return c.getPersistentData().getString("Goal");}
    /** Places a cousin exactly at a spot, in that spot's pose. */
    private static void settle(GoatmanChild c,BlockPos b,String key){
        Spot s=SPOTS.get(key);c.getPersistentData().putString("At",key);c.getPersistentData().putString("Goal",key);c.getPersistentData().remove("Route");if(s==null)return;
        Vec3 at=abs(s.at(),b);
        if(s.pose()==LIE&&s.bed()!=null){c.startSleeping(b.offset(s.bed()));}
        else{c.moveTo(at.x,at.y,at.z,s.yaw(),0);c.setYHeadRot(s.yaw());c.yBodyRot=s.yaw();if(s.pose()==LIE)c.setPose(Pose.SLEEPING);}
        c.pose(s.pose()==SIT);c.cower(s.pose()==COWER);
    }
    /** Every cousin walks the rest of its way now (native tests only); watching still stops the one that only moves unwatched. */
    public static void hurry(ServerLevel l,BlockPos b){
        LabyrinthData d=LabyrinthData.get(l.getServer());CompoundTag r=run(d);var present=visitors(l,b);
        hurried=true;try{for(int i=0;i<40;i++)cousins(l,b,r,present);}finally{hurried=false;}saveRun(d,r);
    }
    /** One tick along its route; true once it has arrived. */
    private static boolean walk(GoatmanChild c,BlockPos b,double speed,ServerLevel l){
        return walk(c,b,speed,l,false);
    }
    private static boolean walk(GoatmanChild c,BlockPos b,double speed,ServerLevel l,boolean physical){
        if(hurried)speed=1000;
        ListTag route=c.getPersistentData().getList("Route",Tag.TAG_COMPOUND);
        if(route.isEmpty())return true;
        CompoundTag next=route.getCompound(0);Vec3 target=abs(GoatmanWindowCircuit.porchLanding(l,b,new Vec3(next.getDouble("X"),next.getDouble("Y"),next.getDouble("Z"))),b);
        Vec3 here=c.position(),delta=target.subtract(here);double length=delta.length();
        Vec3 to=length<=speed?target:here.add(delta.scale(speed/length));
        float yaw=Math.abs(delta.x)+Math.abs(delta.z)>.01?(float)(Math.atan2(delta.z,delta.x)*180/Math.PI)-90:c.getYRot();
        // Walking cousins file in rather than stepping through one another: wait while another walking cousin is just
        // ahead. Head-on, the higher index gives way; nobody waits more than a second and a half.
        if(!hurried&&!physical&&length>1e-4&&waitBehind(c,b,l,here,to,delta)){int wait=c.getPersistentData().getInt("Yield0118")+1;c.getPersistentData().putInt("Yield0118",wait);
            if(wait<30){c.setYRot(yaw);c.setYHeadRot(yaw);c.yBodyRot=yaw;c.animate(0,false,false);return false;}}
        c.getPersistentData().remove("Yield0118");
        if(physical&&!hurried){c.setOnGround(true);c.move(MoverType.SELF,to.subtract(here));c.setDeltaMovement(Vec3.ZERO);c.setYRot(yaw);}
        else c.moveTo(to.x,to.y,to.z,yaw,0);
        double moved=c.position().distanceTo(here);c.setYHeadRot(yaw);c.yBodyRot=yaw;c.animate(moved,false,false);
        if(!c.tell(SILENT)){double step=c.getPersistentData().getDouble("Step")+moved;
            if(step>.7){l.playSound(null,c.blockPosition(),c.getY()>=b.getY()+.7?SoundEvents.WOOD_STEP:SoundEvents.GRASS_STEP,SoundSource.NEUTRAL,.3F,1.2F);step=0;}c.getPersistentData().putDouble("Step",step);}
        if(c.position().distanceToSqr(target)<1e-6){route.remove(0);c.getPersistentData().put("Route",route);}
        return route.isEmpty();
    }
    private static boolean waitBehind(GoatmanChild c,BlockPos b,ServerLevel l,Vec3 here,Vec3 to,Vec3 delta){
        // Only the supper walk in files through the door; the runner's exchange keeps its own timing.
        if(!goal(c).startsWith("seat"))return false;int mine=c.getPersistentData().getInt(INDEX);
        for(var o:l.getEntitiesOfClass(GoatmanChild.class,new AABB(to,to).inflate(.8),e->e!=c&&e.isAlive()&&!e.isSleeping()&&e.getTags().contains(ACTOR)&&e.getPersistentData().contains("Route")&&goal(e).startsWith("seat"))){
            Vec3 at=o.position();if(at.distanceToSqr(to)>.49||at.subtract(here).dot(delta)<=0)continue;
            var route=o.getPersistentData().getList("Route",Tag.TAG_COMPOUND);if(route.isEmpty())continue;
            var next=route.getCompound(0);Vec3 heading=abs(new Vec3(next.getDouble("X"),next.getDouble("Y"),next.getDouble("Z")),b);
            boolean headOn=heading.subtract(at).dot(here.subtract(at))>0;
            if(!headOn||mine>o.getPersistentData().getInt(INDEX))return true;
        }
        return false;
    }
    static void script(GoatmanChild c,List<Vec3> points,String name){setRoute(c,points,name);}
    static void supplyRoute(GoatmanChild c,BlockPos b,boolean toCooler,int carrier){
        var here=rel(c,b);var route=new ArrayList<Vec3>();
        if(!GoatmanWoods.indoors(here)){route.addAll(toYard(at(c)));route.addAll(List.of(UP));route.add(ENTRY);route.add(new Vec3(-3,1,-57.5));}
        else if(here.x>.5){route.add(new Vec3(4,1,here.z));route.add(new Vec3(4,1,-59.5));route.add(new Vec3(-3,1,-59.5));}
        else route.add(new Vec3(-3,1,here.z));
        route.add(new Vec3(-3,1,toCooler?-72.5:-61.5-2*carrier));
        route.add(new Vec3(toCooler?-5.5:-1.45,1,toCooler?-73.5:-61.5-2*carrier));
        setRoute(c,route,toCooler?"cooler137":"condiments137");
    }
    static boolean advance(GoatmanChild c,BlockPos b,double speed,ServerLevel l,boolean physical){return walk(c,b,speed,l,physical);}
    static boolean bunk(GoatmanChild c,BlockPos b,int index,ServerLevel l){
        String name="bunk"+index;var spot=SPOTS.get(name);if(spot==null||!(l.getBlockState(b.offset(spot.bed())).getBlock() instanceof BedBlock))return false;
        if(c.isSleeping())return c.getSleepingPos().filter(b.offset(spot.bed())::equals).isPresent();
        if(!goal(c).equals(name)){
            double x=index%2==0?-3:4,z=spot.at().z;var here=rel(c,b);var path=new ArrayList<Vec3>();
            if(!GoatmanWoods.indoors(here)){path.addAll(List.of(UP));path.add(ENTRY);}
            if((here.x<.5)!=(x<.5)){double end=here.z<-65?-71.5:-59.5;path.add(new Vec3(here.x<.5?-3:4,1,end));path.add(new Vec3(x,1,end));}
            path.add(new Vec3(x,1,z));path.add(new Vec3(index%2==0?-3.6:4.4,1,z));path.add(spot.at());setRoute(c,path,name);
        }
        if(walk(c,b,.11,l)){settle(c,b,name);c.heave(false);c.cower(false);c.fear(0);c.fearStage(0);return true;}return false;
    }
    /** Admission owns its route until the same body is indoors; morning cannot interrupt it. */
    private static void windowEntry(GoatmanChild c,BlockPos b,CompoundTag r,ServerLevel l){
        var state=c.getPersistentData();c.heave(false);c.cower(false);c.fear(0);c.fearStage(0);
        int count=state.getInt("WindowRoute04114"),left=state.getList("Route",Tag.TAG_COMPOUND).size(),leg=count-left;
        if(fresh(r)&&count>0&&leg<WINDOW_ENTRY.length){
            int reach=state.getInt("WindowReach04114");
            c.setCrawling(false);c.setPose(Pose.SWIMMING);
            if(reach<12&&!hurried){state.putInt("WindowReach04114",reach+1);c.setWindowEntry(.12F*(reach+1)/12);c.pose(false);face(c,abs(new Vec3(7.5,c.getY()-b.getY(),-74.5),b));return;}
            if(walk(c,b,CREEP,l,true)){finishWindowEntry(c,b,r);return;}
            left=state.getList("Route",Tag.TAG_COMPOUND).size();leg=count-left;
            if(leg<WINDOW_ENTRY.length){
                Vec3 start=WINDOW_ENTRY[Math.max(0,leg-1)],end=WINDOW_ENTRY[leg];
                float part=start.distanceToSqr(end)<1e-8?1:(float)Mth.clamp(rel(c,b).distanceTo(start)/start.distanceTo(end),0,1);
                float before=leg==0?.12F:ENTRY_BEATS[leg-1];c.setWindowEntry(Mth.lerp(part,before,ENTRY_BEATS[leg]));
                return;
            }
        }
        // 0.4.121: he crawls only through the sash. Once down inside he is a cousin again: he gets up in the bathroom and walks
        // out among them, never crawling on the ground outside or up the aisle.
        c.setWindowEntry(-1);c.setCrawling(false);if(!c.hasPose(Pose.STANDING))c.setPose(Pose.STANDING);
        boolean arrived=walk(c,b,WALK,l,true);if(c.getX()<b.getX()+3.1)c.indoors(true);
        if(arrived)finishWindowEntry(c,b,r);
    }
    private static void finishWindowEntry(GoatmanChild c,BlockPos b,CompoundTag r){
        c.setWindowEntry(-1);c.setCrawling(false);c.setPose(Pose.STANDING);c.indoors(true);c.getPersistentData().remove("WindowRoute04114");c.getPersistentData().remove("WindowReach04114");r.putInt("ExtraState",X_FLOOR);r.putInt("FloorAt0121",r.getInt("Clock"));settle(c,b,fresh(r)?"admitted131":"floor");
    }
    private static float facing(Vec3 from,Vec3 at){Vec3 v=at.subtract(from);return (float)(Math.atan2(v.z,v.x)*180/Math.PI)-90;}
    private static void face(Entity c,Vec3 at){float yaw=facing(c.position(),at);c.setYRot(yaw);c.setYHeadRot(yaw);if(c instanceof LivingEntity e)e.yBodyRot=yaw;}
    /** Whether any present child is looking at it. */
    private static boolean watched(Entity e,List<ServerPlayer> present){
        for(var p:present){Vec3 to=e.getEyePosition().subtract(p.getEyePosition());double d=to.length();
            if(d<48&&d>.1&&p.getLookAngle().dot(to.scale(1/d))>.5&&p.hasLineOfSight(e))return true;}
        return false;
    }
    /** Where cousin i belongs right now (a spot), given the run. */
    private static String place(int i,CompoundTag r){
        int phase=r.getInt("Phase"),clock=r.getInt("Clock"),wrong=r.getInt("Wrong"),runner=r.getInt("Runner");
        if(i==wrong){
            return switch(r.getInt("ExtraState")){
                case X_ACTIVITY->(r.getInt("Tells")&FIRELIGHT)!=0?"firelight":ACTIVITY[i];
                case X_SUPPER,X_INSIDE->seatName(i,r);case X_FIRE->"byFire";case X_APPROACH,X_DOOR->"step";default->fresh(r)&&r.contains("AdmissionSkin04114")?"admitted131":"floor";
            };
        }
        if(i==runner&&r.getInt("RunnerState")!=R_HOME&&r.getInt("RunnerState")!=R_INSIDE)return "step";
        if((phase==GATHERING||phase==VIGIL)&&GoatmanFear.stage(r)>=GoatmanFear.FEAR)return drawBack(i,r,GoatmanFear.destination(i,r));
        if(phase==VIGIL)return drawBack(i,r,seatName(i,r));
        if(phase>=VIGIL||clock>=supperAt(r)||r.getBoolean("CalledIn04123"))return seatName(i,r);
        return ACTIVITY[i];
    }
    /**
     * 0.4.121: while something comes in through the bathroom window, and for fifteen seconds after it has lain down, the
     * cousins in the back half of the trailer draw away from it, up toward the door.
     */
    static String drawBack(int i,CompoundTag r,String usual){
        if(GoatmanBedtime.enabled(r))return usual; // Bedside admission replaces the old table retreat.
        int xs=r.getInt("ExtraState");
        boolean coming=xs==X_WINDOW||xs==X_FLOOR&&r.contains("FloorAt0121")&&r.getInt("Clock")-r.getInt("FloorAt0121")<DRAW_BACK;
        return coming&&backSpot(usual)?"shy"+Math.floorMod(i,8):usual;
    }
    /** A place in the back half of the trailer, nearer the bathroom than the door. */
    public static boolean backSpot(String key){var spot=SPOTS.get(key);return spot!=null&&spot.inside()&&spot.at().z<-63;}
    static final int DRAW_BACK=300;
    static String seatName(int i,CompoundTag r){
        if(!fresh(r))return "seat"+i;int real=i==r.getInt("Wrong")?r.getInt("Runner"):i;
        return "seat"+(real<r.getInt("Wrong")?real:real-1);
    }
    /** The cousin who wants to check the door: the first who is neither the runner nor the thing. */
    static int checker(CompoundTag r){for(int i=0;i<count(r);i++)if(i!=r.getInt("Wrong")&&i!=r.getInt("Runner"))return i;return 0;}

    private static boolean spawnReady(ServerLevel l,BlockPos b){
        var area=IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN);
        for(int x=(int)Math.floor(area.minX)>>4;x<=(int)Math.floor(area.maxX)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=(int)Math.floor(area.maxZ)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;
        return true;
    }
    public static boolean stage(ServerLevel l,BlockPos b,CompoundTag r){
        // Block chunks can arrive before their saved entity sections. Wait for the latter
        // before deciding that a cousin is missing or registering a fresh body.
        if(!r.hasUUID("Id")||!spawnReady(l,b))return false;List<GoatmanChild> children=actors(l,b);
        for(var c:children)if(!c.getTags().contains("TrailerWalk")&&(!c.getPersistentData().hasUUID(ROUND)||!c.getPersistentData().getUUID(ROUND).equals(r.getUUID("Id"))))c.discard();
        for(int i=0;i<count(r);i++){
            boolean away=i==r.getInt("Runner")&&(r.getInt("RunnerState")==R_AWAY||r.getInt("RunnerState")==R_LOST)||i==r.getInt("Wrong")&&r.getInt("Phase")==DAWN;
            final int index=i;var matches=children.stream().filter(c->!c.isRemoved()&&!c.girl()&&c.getPersistentData().getInt(INDEX)==index).toList();
            if(away){matches.forEach(Entity::discard);continue;}
            // A cousin whose place a reader has taken is not there; one already standing goes only while nobody sees it go.
            if(playedBy(r,i)){for(var c:matches)if(unlooked(l,c.position()))c.discard();continue;}
            for(int n=1;n<matches.size();n++)matches.get(n).discard();
            if(!matches.isEmpty())continue;
            GoatmanChild c=GoatmanRegistry.CHILD.get().create(l);if(c==null)continue;
            c.addTag(ACTOR);c.getPersistentData().putUUID(ROUND,r.getUUID("Id"));c.getPersistentData().putInt(INDEX,i);
            c.appearance(r.getInt("Skin"+i),i==r.getInt("Wrong")?r.getInt("Tells"):0,null);
            // Native spawn callbacks and the first tracking packet must see the campsite,
            // rather than the new entity's default position in chunk zero.
            settle(c,b,place(i,r));
            if(i!=r.getInt("Wrong")&&(r.getInt("Phase")==GATHERING||r.getInt("Phase")==VIGIL))GoatmanFear.pose(c,r);
            if(!l.addFreshEntity(c))io.github.knaitoe.theoldesthouse.TheOldestHouse.LOGGER.warn("Could not stage trailer cousin {} for round {} at {}; retrying",i,r.getUUID("Id"),c.position());
        }
        return true;
    }
    /** How far behind the reader she keeps on the trail, and how far her head is bowed. */
    public static final double FOLLOW_GAP=2.8;public static final float HEAD_TUCKED=38;
    private static GoatmanChild girl(ServerPlayer p,BlockPos b,CompoundTag r){
        if(!spawnReady(p.serverLevel(),b))return null;
        var girls=actors(p.serverLevel(),b).stream().filter(c->c.viewer().filter(p.getUUID()::equals).isPresent()&&!c.getTags().contains("TrailerWalk")).toList();
        for(int i=1;i<girls.size();i++)girls.get(i).discard();if(!girls.isEmpty())return girls.getFirst();
        GoatmanChild c=GoatmanRegistry.CHILD.get().create(p.serverLevel());if(c==null)return null;
        c.appearance(5,0,p.getUUID());c.addTag(ACTOR);c.getPersistentData().putUUID(ROUND,r.getUUID("Id"));c.getPersistentData().putInt(INDEX,-1);
        c.moveTo(abs(GoatmanWoods.path(0),b));p.serverLevel().addFreshEntity(c);return c;
    }

    // ------------------------------------------------------------------------------------------------ the trail
    private static void path(ServerPlayer p,BlockPos b,CompoundTag r,CompoundTag own){
        Vec3 relative=rel(p,b);var projected=GoatmanWoods.project(relative);double progress=own.getDouble("Progress");
        if(fresh(r)&&progress>=38&&own.getInt("CampTalk0465")<CAMP_STORY.length*60){
            int talk=own.getInt("CampTalk0465");if(talk%60==0)p.displayClientMessage(Component.literal("By the fire: “"+CAMP_STORY[talk/60]+"”"),false);own.putInt("CampTalk0465",talk+1);
        }
        if(!own.getBoolean("Arrived")&&relative.z<-.1){
            // Dense edges cannot be flanked with jumping or a pearl. Normal walking remains free.
            if((!GoatmanWoods.clearing(relative)&&projected.distance()>1.65)||relative.y>2.2||relative.y<-.5||projected.progress()>progress+2){
                Vec3 at=abs(GoatmanWoods.path(progress),b);p.connection.teleport(at.x,at.y,at.z,p.getYRot(),p.getXRot());p.fallDistance=0;
            }else {progress=projected.progress();own.putDouble("Progress",progress);}
        }
        if(!own.getBoolean("GirlGone")&&!own.getBoolean("Arrived")){
            GoatmanChild girl=girl(p,b,r);if(girl!=null){
                follow(girl,b,progress);
                if(progress>=54&&(!p.hasLineOfSight(girl)||p.getLookAngle().dot(girl.position().subtract(p.position()).normalize())<.25)){own.putBoolean("GirlGone",true);girl.discard();}
            }
        }else for(var girl:actors(p.serverLevel(),b))if(girl.viewer().filter(p.getUUID()::equals).isPresent()&&!girl.getTags().contains("TrailerWalk"))girl.discard();
        hollows(p,b,own,progress);
        if(GoatmanWoods.clearing(rel(p,b))&&progress>=GoatmanWoods.CLEARING){
            own.putBoolean("Arrived",true);own.putBoolean("GirlGone",true);GoatmanHaunt.vanish(p,"hollow");
            if(!own.getBoolean("Greeted")){own.putBoolean("Greeted",true);p.displayClientMessage(Component.literal("A cousin: There you are. Everyone's here."),false);}
        }
    }
    /**
     * 0.4.145 (owner): she keeps a few steps behind on the trail with her head down, easing into and out of her walk.
     * Turn back towards her and she never turns to walk away: she is simply further off, slid back along the trail
     * without a step, still facing the way the reader was going.
     */
    private static void follow(GoatmanChild girl,BlockPos b,double progress){
        girl.getPersistentData().remove("Route");
        double here=GoatmanWoods.project(rel(girl,b)).progress(),want=Math.max(0,progress-FOLLOW_GAP),gap=want-here;
        boolean walking=gap>0;double next=walking?here+Math.min(gap,Math.min(.17,.03+gap*.09)):here+Math.max(gap,-.4);
        Vec3 at=abs(GoatmanWoods.path(next),b),ahead=abs(GoatmanWoods.path(next+1.5),b),from=girl.position();float yaw=facing(at,ahead);
        girl.setOnGround(true);girl.move(MoverType.SELF,at.subtract(from));girl.setDeltaMovement(Vec3.ZERO);
        girl.setYRot(yaw);girl.setXRot(HEAD_TUCKED);girl.setYHeadRot(yaw);girl.yBodyRot=yaw;
        girl.animate(walking?girl.position().distanceTo(from):0,false,false);
    }
    /** Any camera, a spectator's included, turned straight at the figure with nothing solid between. */
    static boolean lookedAt(ServerLevel l,Entity figure){
        for(var player:l.players()){
            Entity camera=player.getCamera();if(camera.level()!=l)continue;var eye=camera.getEyePosition();
            for(double y:new double[]{.9,1.6}){var point=figure.position().add(0,y,0);var to=point.subtract(eye);double d=to.length();
                if(d>.1&&d<64&&camera.getViewVector(1).dot(to.scale(1/d))>.96&&GoatmanStalking.visible(l,camera,eye,point))return true;}
        }
        return false;
    }
    /** Someone stands in each hollow with its back to the trail while the latecomer comes up on it, then it is gone. */
    private static void hollows(ServerPlayer p,BlockPos b,CompoundTag own,double progress){
        var l=p.serverLevel();
        for(int h=0;h<GoatmanWoods.HOLLOWS.length;h++){
            double at=GoatmanWoods.HOLLOWS[h];String purpose="hollow"+h;boolean want=!own.getBoolean("Arrived")&&progress>=at-14&&progress<=at+2&&!own.getBoolean("Hollow"+h);
            if(progress>at+2)own.putBoolean("Hollow"+h,true);
            var existing=l.getEntitiesOfClass(GoatmanFigure.class,p.getBoundingBox().inflate(40),f->f.viewer().filter(p.getUUID()::equals).isPresent()&&f.purpose.equals(purpose));
            if(!want){existing.forEach(Entity::discard);continue;}
            // 0.4.145 (owner): it is gone the moment anyone looks straight at it.
            if(!existing.isEmpty()){if(existing.stream().anyMatch(f->lookedAt(l,f))){existing.forEach(Entity::discard);own.putBoolean("Hollow"+h,true);}continue;}
            var f=GoatmanRegistry.FIGURE.get().create(l);if(f==null)continue;f.viewer(p.getUUID());f.purpose=purpose;f.until=l.getGameTime()+2400;
            Vec3 pos=abs(GoatmanWoods.HOLLOW_STAND[h],b);f.moveTo(pos.x,pos.y,pos.z,GoatmanWoods.HOLLOW_YAW[h],0);f.face(GoatmanWoods.HOLLOW_YAW[h]);GoatmanHaunt.appear(l,f);
        }
    }

    // ------------------------------------------------------------------------------------------------ the cousins
    /** Count actual scripted movement ticks to the door plane, including the one tick used by a zero-length waypoint. */
    private static int doorTicks(GoatmanChild c,BlockPos b,List<Vec3> points,boolean incoming,double speed){
        Vec3 here=rel(c,b);int ticks=0;double plane=EXCHANGE_PLANE;
        if(incoming?here.z<=plane:here.z>=plane)return -1;
        for(var next:points){double length=here.distanceTo(next);
            if(incoming?next.z<=plane:next.z>=plane){double fraction=(plane-here.z)/(next.z-here.z);return ticks+Math.max(1,(int)Math.ceil(length*fraction/speed));}
            ticks+=Math.max(1,(int)Math.ceil(length/speed));here=next;
        }
        return Integer.MAX_VALUE;
    }
    private static List<Vec3> remaining(GoatmanChild c){
        var out=new ArrayList<Vec3>();var left=c.getPersistentData().getList("Route",Tag.TAG_COMPOUND);
        for(int k=0;k<left.size();k++){var t=left.getCompound(k);out.add(new Vec3(t.getDouble("X"),t.getDouble("Y"),t.getDouble("Z")));}return out;
    }
    /** Two child-width lanes fit through the native one-block door without the bodies passing through one another. */
    private static List<Vec3> exchangeLane(List<Vec3> points,boolean incoming){return points.stream().map(v->v.z>=-57&&v.z<=-54.5?new Vec3(incoming?.75:.25,v.y,v.z):v).toList();}
    /** The borrowed-seat child leaves in the real child's doorway beat, never on a later wall-clock timer. */
    private static void exchange(ServerLevel l,BlockPos b,CompoundTag r,boolean open,List<GoatmanChild> cast){
        if(!fresh(r)||r.getInt("Phase")!=GATHERING||r.getBoolean("Silence"))return;
        var extra=cast.stream().filter(c->c.getPersistentData().getInt(INDEX)==r.getInt("Wrong")).findFirst().orElse(null);
        var real=cast.stream().filter(c->c.getPersistentData().getInt(INDEX)==r.getInt("Runner")).findFirst().orElse(null);if(extra==null||real==null)return;
        int rs=r.getInt("RunnerState");var incoming=remaining(real);incoming.addAll(inFrom("step",r.getInt("Runner"),r));incoming=exchangeLane(incoming,true);
        if(open&&r.getInt("ExtraState")==X_SUPPER&&(rs==R_RETURNING||rs==R_KNOCKING||rs==R_INSIDE)){
            var outgoing=exchangeLane(onward(extra,"byFire"),false);int out=doorTicks(extra,b,outgoing,false,impostorRun(r)),in=doorTicks(real,b,incoming,true,RUN);
            if(rs!=R_RETURNING||in<=out){r.putBoolean("Exchange0497",true);r.putInt("ExtraState",X_FIRE);setRoute(extra,outgoing,"byFire");}
        }
        // If the real child was kept at a shut door, let the outgoing body cover its aisle first.
        // Both then run across the same threshold together, in either actor iteration order.
        if(r.getBoolean("Exchange0497"))r.putBoolean("ExchangeAdmit0497",open&&doorTicks(extra,b,remaining(extra),false,impostorRun(r))<=doorTicks(real,b,incoming,true,RUN));
    }
    private static boolean admit(CompoundTag r){return !r.getBoolean("Exchange0497")||r.getBoolean("ExchangeAdmit0497");}
    private static void exchanged(BlockPos b,CompoundTag r,List<GoatmanChild> cast){
        if(!r.getBoolean("Exchange0497"))return;
        for(var c:cast){int i=c.getPersistentData().getInt(INDEX);
            if(i==r.getInt("Wrong")&&rel(c,b).z>=EXCHANGE_PLANE&&!r.contains("ExchangeOut0497")){r.putInt("ExchangeOut0497",r.getInt("Clock"));GoatmanFear.departed(r);}
            if(i==r.getInt("Runner")&&rel(c,b).z<=EXCHANGE_PLANE&&!r.contains("ExchangeIn0497"))r.putInt("ExchangeIn0497",r.getInt("Clock"));
        }
        if(r.contains("ExchangeOut0497")&&r.contains("ExchangeIn0497"))GoatmanFear.returned(r);
    }
    /**
     * 0.4.123: indoors when the night begins, the same body leaves its place only while no camera, spectators included, is
     * turned toward it or toward the porch step it goes to. From the step it begins its circuit. A seat left empty is noticed.
     */
    private static boolean slipOut(ServerLevel l,BlockPos b,CompoundTag r,GoatmanChild c){
        Vec3 step=abs(GoatmanWindowCircuit.porchLanding(l,b,SPOTS.get("step").at()),b);
        if(!unlooked(l,c.position())||!unlooked(l,step))return false;
        boolean seated=r.getInt("ExtraState")==X_SUPPER;settle(c,b,"step");c.moveTo(step.x,step.y,step.z,180,0);c.setYHeadRot(180);c.yBodyRot=180;
        c.pose(false);c.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);c.getPersistentData().remove("EatingUntil0465");r.putInt("ExtraState",X_DOOR);
        if(seated)GoatmanFear.queue(r,27);
        return true;
    }
    /** No camera, spectators included, is turned toward a body standing here with a clear line (native glazing is clear). */
    private static boolean unlooked(ServerLevel l,Vec3 feet){
        for(var p:l.players()){Entity camera=p.getCamera();Vec3 eye=camera.getEyePosition(),look=camera.getLookAngle();
            for(double y:new double[]{.3,1,1.7}){Vec3 point=feet.add(0,y,0),to=point.subtract(eye);double d=to.length();
                if(d>.01&&d<72&&look.dot(to.scale(1/d))>.35&&GoatmanStalking.visible(l,camera,eye,point))return false;}}
        return true;
    }
    private static void cousins(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> present){
        int phase=r.getInt("Phase"),clock=r.getInt("Clock"),wrong=r.getInt("Wrong"),runner=r.getInt("Runner");
        boolean open=GoatmanWoods.doorOpen(l,b);var cast=cousinsOf(l,b);exchange(l,b,r,open,cast);
        for(var c:cast){
            int i=c.getPersistentData().getInt(INDEX);if(i>=count(r))continue;
            if(fresh(r)&&phase==GATHERING&&r.getBoolean("Exchange0497")&&!open
                    &&(!r.contains("ExchangeOut0497")||!r.contains("ExchangeIn0497"))&&(i==wrong||i==runner)){c.pose(false);continue;}
            // 0.4.123: still indoors when night falls (he was never let in, or the door shut on it), it leaves unseen first.
            if(brisk(r)&&phase==VIGIL&&i==wrong&&(r.getInt("ExtraState")==X_SUPPER||outside(r))&&GoatmanWoods.indoors(rel(c,b))){
                if(slipOut(l,b,r,c))continue;
                if(outside(r)){c.pose(false);continue;}
            }
            if(fresh(r)&&phase==VIGIL&&i==wrong&&outside(r)){GoatmanStalking.step(l,b,r,c,clock);continue;}
            // Admission changes ExtraState to X_WINDOW while the clock remains in VIGIL.
            // Do not let the ordinary staging branch reset the route before the window crawl completes.
            if(i==wrong&&r.getInt("ExtraState")==X_WINDOW){
                windowEntry(c,b,r,l);
                continue;
            }
            if(i==wrong&&(r.getInt("ExtraState")==X_INSIDE||r.getInt("ExtraState")==X_FLOOR)&&GoatmanWoods.indoors(rel(c,b)))c.indoors(true);
            if(GoatmanBedtime.cousin(l,b,r,c,present))continue;
            if(GoatmanMeal.cousin(l,b,r,c,present))continue;
            if(!outside(r)&&!r.contains("AdmissionSkin04114")&&c.stalkingAppearance()&&GoatmanStalking.obscured(l,c))c.disguise(-1);
            if((phase==GATHERING||phase==VIGIL)&&i!=wrong)GoatmanFear.pose(c,r);else {c.fear(0);c.fearStage(0);}
            String want=place(i,r);double speed=brisk(r)&&phase==GATHERING&&want.startsWith("seat")?JOG:WALK;
            if(i==runner){int rs=r.getInt("RunnerState");
                // 0.4.123: on a brisk evening he runs for the truck and is out of sight at the bend.
                if(rs==R_LEAVING){if(!goal(c).equals("_trail")&&!at(c).equals("_trail")){List<Vec3> out=toYard(at(c));
                        for(double s:new double[]{66,63,58,53,48,43,41,37,33,30,27})if(!brisk(r)||s>=BRISK_GONE)out.add(GoatmanWoods.path(s));setRoute(c,out,"_trail");}
                    if(walk(c,b,brisk(r)?RUN:WALK,l)){c.getPersistentData().putString("At","_trail");if(!watched(c,present)){c.discard();r.putInt("RunnerState",R_AWAY);}}continue;}
                if(rs==R_RETURNING){speed=RUN;if(walk(c,b,RUN,l)){c.getPersistentData().putString("At","step");boolean enter=open&&admit(r);r.putInt("RunnerState",enter?R_INSIDE:R_KNOCKING);if(enter){GoatmanFear.returned(r);setRoute(c,inFrom("step",i,r),seatName(i,r));}}continue;}
                if(rs==R_KNOCKING){face(c,abs(new Vec3(.5,1,-55.5),b));c.pose(false);if(open&&admit(r)){r.putInt("RunnerState",R_INSIDE);GoatmanFear.returned(r);setRoute(c,inFrom("step",i,r),seatName(i,r));walk(c,b,RUN,l);}continue;}
                if(rs==R_INSIDE&&c.getPersistentData().contains("Route"))speed=RUN;
            }
            if(i==wrong){int xs=r.getInt("ExtraState");
                // 0.4.123: out at a run as he comes in, to wait by the fire for the dark. The circuit is the night's, with its words.
                if(brisk(r)&&xs==X_FIRE&&phase==GATHERING)speed=impostorRun(r);
                else if(fresh(r)&&xs==X_FIRE&&phase==GATHERING&&clock>=BACK0465
                        &&GoatmanStalking.evening(l,b,r,c,clock,at(c).equals("byFire")&&c.getPersistentData().getList("Route",Tag.TAG_COMPOUND).isEmpty()))continue;
                else if(xs==X_FIRE&&phase==GATHERING&&clock>=(fresh(r)?BACK0465:RUNNER_BACK)&&clock<(fresh(r)?SILENCE0465:SILENCE)){speed=RUN;
                    // Once it is out by the fire, it runs to the nearest window, scrapes at the glass where it stands, and goes back.
                    int run=r.getInt("WindowRun0464");
                    if(!fresh(r)&&run==0&&at(c).equals("byFire")){r.putInt("WindowRun0464",run=1);GoatmanFear.departed(r);}
                    if(run==1&&at(c).equals("windowRun")&&c.getPersistentData().getList("Route",Tag.TAG_COMPOUND).isEmpty()){r.putInt("WindowRun0464",run=2);r.putInt("WindowAt0464",clock);}
                    if(run==2){int since=clock-r.getInt("WindowAt0464");
                        if(since%20==0)l.playSound(null,b.offset(-8,2,-59),GoatmanRegistry.CLAW.get(),SoundSource.BLOCKS,3.2F,1.35F);
                        if(since>=WINDOW_STAY)r.putInt("WindowRun0464",run=3);}
                    if(run==1||run==2)want="windowRun";
                }
                if(xs==X_APPROACH){
                    // It comes only while nobody is looking at it.
                    if(!goal(c).equals("step"))setRoute(c,onward(c,"step"),"step");
                    if(!watched(c,present)&&walk(c,b,CREEP,l)){c.getPersistentData().putString("At","step");if(open){r.putInt("ExtraState",X_INSIDE);setRoute(c,inFrom("step",i,r),seatName(i,r));}else r.putInt("ExtraState",X_DOOR);}
                    else if(watched(c,present)){c.pose(false);c.heave(true);}
                    continue;
                }
                if(xs==X_DOOR&&phase==GATHERING&&open){r.putInt("ExtraState",X_INSIDE);setRoute(c,inFrom("step",i,r),seatName(i,r));}
                c.heave(xs==X_FIRE&&watched(c,present)&&clock%240<120);
            }
            if(!want.equals(goal(c))){
                if(at(c).isEmpty()){settle(c,b,want);continue;}
                setRoute(c,onward(c,want),want);
            }
            ListTag route=c.getPersistentData().getList("Route",Tag.TAG_COMPOUND);
            if(!route.isEmpty()){if(walk(c,b,speed,l))arrive(c,b,r,l,want);continue;}
            if(!at(c).equals(want)){arrive(c,b,r,l,want);continue;}
            if(phase==GATHERING&&clock>=supperAt(r)){int ate=GoatmanSupper.eat(l,b,r,c,i,clock);
                if(ate==1)say(present,"A cousin: Who had two? There was one for everybody.");
                else if(ate==2){String line=GoatmanSupper.burntLine(r);if(line!=null)say(present,line);}}
            // Something burnt on the griddle: every real cousin looks round at it for a moment.
            if(i!=wrong&&GoatmanSupper.smelled(r,l.getGameTime()))face(c,Vec3.atCenterOf(b.offset(GoatmanWoods.PAN)));
            else idle(c,r,present,clock,i);
            if((phase==GATHERING||phase==VIGIL)&&i!=wrong){GoatmanFear.pose(c,r);if(c.fear()==GoatmanFear.WATCH&&c.fearStage()>=GoatmanFear.FEAR)face(c,abs(new Vec3(-8,2,-59),b));}
        }
        exchanged(b,r,cast);
    }
    /** From wherever a spot is, out to the yard. */
    private static List<Vec3> toYard(String from){
        Spot a=SPOTS.get(from);List<Vec3> out=new ArrayList<>();if(a==null){out.add(YARD);return out;}
        for(int i=a.access().length-1;i>=0;i--)out.add(a.access()[i]);
        if(a.inside()){out.add(ENTRY);for(int i=UP.length-1;i>=0;i--)out.add(UP[i]);}
        out.add(YARD);return out;
    }
    /** From the step through the door to a seat. */
    private static List<Vec3> inFrom(String spot,int i,CompoundTag r){var seat=SPOTS.get(seatName(i,r));List<Vec3> out=new ArrayList<>();out.add(UP[4]);out.add(ENTRY);out.addAll(List.of(seat.access()));out.add(seat.at());return r.getBoolean("Exchange0497")?exchangeLane(out,true):out;}
    private static void arrive(GoatmanChild c,BlockPos b,CompoundTag r,ServerLevel l,String key){
        settle(c,b,key);int i=c.getPersistentData().getInt(INDEX);
        if(i!=r.getInt("Wrong")&&(r.getInt("Phase")==GATHERING||r.getInt("Phase")==VIGIL))GoatmanFear.pose(c,r);
        // Each cousin who sits down to supper takes a brat from the pan, the one who doesn't belong included.
        if(!r.getBoolean("PlayerServes0464")&&key.startsWith("seat")&&r.getInt("Phase")==GATHERING&&(r.getInt("Served")&(1<<i))==0){
            r.putInt("Served",r.getInt("Served")|(1<<i));
            if(r.getInt("Pan")>0){r.putInt("Pan",r.getInt("Pan")-1);GoatmanWoods.pan(l,b,r.getInt("Pan"));GoatmanWoods.served(l,b,i,true);}
            else if(i==r.getInt("Runner"))say(visitors(l,b),"A cousin: Y'all ate mine?");
        }
    }
    /** Small ordinary movements, never a drift: a turn of the head toward someone, now and then. */
    private static void idle(GoatmanChild c,CompoundTag r,List<ServerPlayer> present,int clock,int i){
        Spot s=SPOTS.get(at(c));if(s==null||s.pose()==LIE)return;
        ServerPlayer nearest=present.stream().min(Comparator.comparingDouble(c::distanceToSqr)).orElse(null);
        if(c.tell(FACE)&&nearest!=null){face(c,nearest.position());return;}
        c.setYRot(s.yaw());c.yBodyRot=s.yaw();
        if(c.tell(STILL)){c.setYHeadRot(s.yaw());return;}
        int beat=Math.floorMod(clock+i*53,220);
        if(beat<45&&nearest!=null&&nearest.distanceToSqr(c)<100){Vec3 v=nearest.position().subtract(c.position());float yaw=(float)(Math.atan2(v.z,v.x)*180/Math.PI)-90;
            float turn=Mth.wrapDegrees(yaw-s.yaw());c.setYHeadRot(s.yaw()+Math.max(-70,Math.min(70,turn)));}
        else c.setYHeadRot(s.yaw()+(beat<90?12:0));
        if(r.getInt("Phase")==GATHERING&&clock>=80&&clock<SUPPER){int laugh=clock%240;if(laugh==(c.tell(LATE_LAUGH)?112:100))c.level().playSound(null,c.blockPosition(),GoatmanRegistry.LAUGH.get(),SoundSource.NEUTRAL,.28F,.95F+i*.025F);}
    }

    // ------------------------------------------------------------------------------------------------ the evening and the night
    /** Includes every enrolled real child, rather than treating multiplayer visitors as spectators. */
    public static int expectedCount(CompoundTag r){return r.contains("Expected")?r.getInt("Expected"):expected(r,r.getCompound("Cohort").getAllKeys().size());}
    public static int presentCount(ServerLevel l,BlockPos b){return cousinsOf(l,b).size()+(int)visitors(l,b).stream().filter(p->GoatmanWoods.clearing(rel(p,b))).count();}
    public static void onServerTick(ServerTickEvent.Post event){tick(event.getServer());}
    public static void tick(MinecraftServer server){
        // A transient modifier is also removed after commands, death, cloning or dimension changes.
        for(ServerLevel world:server.getAllLevels())for(ServerPlayer p:List.copyOf(world.players()))if(childScale(p)&&!inside(p))depart(p);
        ServerLevel l=server.getLevel(HouseDimensions.INTERIOR);BlockPos b=base(server);if(l==null||b==null)return;
        List<ServerPlayer> present=visitors(l,b);if(present.isEmpty())return;IndianLakeRooms.keepLoaded(l,b,LabyrinthPlace.GOATMAN);
        LabyrinthData d=LabyrinthData.get(server);CompoundTag r=run(d);
        for(ServerPlayer p:present)if(!r.getCompound("Cohort").contains(p.getUUID().toString())&&canDeal(d,p.getUUID())){enter(p);r=run(d);}
        if(r.getInt("Phase")==0||r.getInt("Version")!=VERSION)return;CompoundTag cohort=r.getCompound("Cohort");int phase=r.getInt("Phase");
        List<ServerPlayer> enrolled=present.stream().filter(p->cohort.contains(p.getUUID().toString())&&!cohort.getCompound(p.getUUID().toString()).getBoolean("Failed")).toList();
        if(enrolled.isEmpty())return;
        int expected=expected(r,fresh(r)?cohort.getAllKeys().size():enrolled.size());
        if(phase<VIGIL&&!r.getBoolean("MealFrozen0465")&&r.getInt("Expected")!=expected){r.putInt("Expected",expected);GoatmanWoods.supplies(l,b,expected);if(fresh(r))GoatmanWoods.supperChairs(l,b,expected);}
        boolean arrived=true;int clock=r.getInt("Clock");
        for(ServerPlayer p:enrolled){
            String key=p.getUUID().toString();CompoundTag own=cohort.getCompound(key);scale(p,true);
            if(phase<VIGIL)path(p,b,r,own);
            // The private lap filled long waits behind the door; a brisk night's own body never stops circling.
            if(fresh(r)&&!brisk(r)&&outside(r)&&(phase==VIGIL||phase==GATHERING))GoatmanStalking.dash(l,b,r,p,own);
            arrived&=own.getBoolean("Arrived");
            if(phase==VIGIL&&!own.getBoolean("BedsideEnded131")){
                if(!GoatmanWoods.indoors(rel(p,b))){
                    // A closed wall or a teleport cannot substitute for opening the real door.
                    Vec3 at=abs(new Vec3(.5,1,-58.5),b);p.connection.teleport(at.x,at.y,at.z,p.getYRot(),p.getXRot());
                }else own.putInt("Vigil",own.getInt("Vigil")+1);
            }
            cohort.put(key,own);
        }
        r.put("Cohort",cohort);
        if(phase==PATH&&arrived){phase=GATHERING;clock=0;r.putInt("Phase",phase);r.putInt("Pan",r.getBoolean("PlayerServes0464")?0:count(r)-1+enrolled.size());r.putInt("PanFor",count(r)-1+enrolled.size());GoatmanWoods.pan(l,b,r.getInt("Pan"));}
        if(phase==PATH)clock++;
        else if(phase==GATHERING){
            GoatmanSupper.tick(l,b,r,enrolled);GoatmanMeal.tick(l,b,r,enrolled);cookingChatter(r,enrolled);gas(l,b,r,enrolled);
            boolean waitingFirst=brisk(r)&&!r.getBoolean("MealStarted137")&&!r.contains("SupperAt04123")&&(r.getInt("Plated0464")&~r.getInt("EmptyPlates0465"))==0&&clock>=BRISK_SUPPER-1;
            if(!waitingFirst&&(brisk(r)||!fresh(r)||clock!=supperAt(r)-1||GoatmanSupper.ready(r))){clock++;evening(l,b,r,enrolled,clock);}
            if(clock>=gatherTicks(r)&&!GoatmanStalking.eveningActive(r)){nightfall(l,b,r,enrolled);phase=VIGIL;clock=0;}
        }
        else if(phase==VIGIL){clock++;night(l,b,r,enrolled,clock);if(dawnReady(r,clock)&&GoatmanBedtime.canDawn(r,enrolled)){r.putInt("Clock",clock);dawn(l,b,r,enrolled,d);phase=DAWN;}}
        r.putInt("Clock",clock);
        // Restore saved actors, never reroll a tell or duplicate a cousin after a restart.
        if(server.getTickCount()%20==0||r.getBoolean("StagePending0469"))r.putBoolean("StagePending0469",!stage(l,b,r));cousins(l,b,r,enrolled);
        if(phase==GATHERING||phase==VIGIL){GoatmanFear.tick(r,cousinsOf(l,b),clock);GoatmanBedtime.tick(l,b,r,enrolled,cousinsOf(l,b));}
        saveRun(d,r);
        if(server.getTickCount()%20==0||speaking(r)||clock==r.getInt("FearAt0472"))for(var p:enrolled)scene(p,r);
        ambience(l,b,r,enrolled,server.getTickCount());
    }
    static final String[] COOKING_LINES={"A cousin: Leave mine another minute. I like the edges dark.","A cousin: Who packed three bottles of mustard and no forks?","A cousin: They're in the drawer. Try the other one.","A cousin: My dad used to cook these on a stick. Half of them fell in the fire.","A cousin: Turn that one. It's catching underneath.","A cousin: We can wait. There's still light outside.","A cousin: Save one for him when he gets back.","A cousin: That's plenty. We can split the last ones."};
    /** Whether a cooking line fits the moment: the griddle lines need franks on it; the saved one needs someone away. */
    static boolean cookingLineTrue(int k,CompoundTag r){
        return switch(k){case 0,4->GoatmanSupper.onGriddle(r);case 6->{int rs=r.getInt("RunnerState");yield rs==R_LEAVING||rs==R_AWAY||rs==R_RETURNING||rs==R_KNOCKING;}default->true;};
    }
    private static void cookingChatter(CompoundTag r,List<ServerPlayer> readers){
        if(!fresh(r)||GoatmanSupper.ready(r)||r.getInt("Clock")<(brisk(r)?BRISK_COOK_TALK:850))return;
        int ticks=r.getInt("CookingChatTicks0490")+1;r.putInt("CookingChatTicks0490",ticks);if(ticks%220!=0)return;
        String[] lines=COOKING_LINES;int next=r.getInt("CookingChatLine0490");
        // 0.4.121: a line is said only if it is true now: nobody asks for theirs to stay on a griddle that is empty.
        for(int tries=0;tries<lines.length;tries++,next++){int k=Math.floorMod(next,lines.length);
            if(cookingLineTrue(k,r)){say(readers,forRunner(r,lines[k]));r.putInt("CookingChatLine0490",next+1);return;}}
        r.putInt("CookingChatLine0490",next);
    }
    private static void evening(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> enrolled,int clock){
        boolean brisk=brisk(r);
        int supper=supperAt(r),leaves=fresh(r)?LEAVES0465:RUNNER_LEAVES,backAt=fresh(r)?BACK0465:RUNNER_BACK,silence=fresh(r)?SILENCE0465:SILENCE;
        if(brisk){
            if(clock==220)say(enrolled,"A cousin: You should've heard his story. The scary part was how long it took.");
        }else if(fresh(r)){
            if(clock==220)say(enrolled,"A cousin: You should've heard his story. The scary part was how long it took.");
            if(clock==520)say(enrolled,"A cousin: Can somebody pass the plates? We brought enough for all of us.");
            if(clock==820&&GoatmanSupper.onGriddle(r))say(enrolled,"A cousin: You always burn them on one side. Turn them over this time.");
            if(clock==1340)say(enrolled,"A cousin: He knows where the gas is. He'll be back in a minute.");
        }
        // Late joiners before supper get a brat counted for them; nobody gets one counted after.
        if(!r.getBoolean("PlayerServes0464")&&clock<SUPPER&&r.getInt("PanFor")!=r.getInt("Expected")){r.putInt("Pan",r.getInt("Pan")+r.getInt("Expected")-r.getInt("PanFor"));r.putInt("PanFor",r.getInt("Expected"));GoatmanWoods.pan(l,b,r.getInt("Pan"));}
        boolean serves=r.getBoolean("PlayerServes0464");
        if(serves&&clock==SERVE_HINT)say(enrolled,"A cousin: Go grab 'em from the cooler by the kitchen counter. Four in each pack. Cook them on the griddle, then put one on everybody's plate.");
        if(serves&&!brisk&&clock==(fresh(r)?2460:WINDOW_HINT))say(enrolled,"A cousin: Is anyone else getting cold back there?");
        if(!serves&&clock==OLD_WINDOW_HINT)say(enrolled,"A cousin: Somebody shut the bathroom window. Bugs are getting in.");
        // Older saved evenings retain their clock; current evenings wait for an actual seated meal below.
        boolean served=fresh(r)&&serves&&r.getBoolean("MealReady0465")&&clock<leaves;
        if(!brisk&&(clock==leaves||served)&&r.getInt("RunnerState")==R_HOME){r.putInt("RunnerState",R_LEAVING);say(enrolled,"A cousin: Generator's out of gas. I'll run get some from the truck. Back before dark.");}
        if(brisk){table(l,b,r,enrolled,clock);return;}
        if(clock==supper){if(r.getInt("ExtraState")==X_ACTIVITY)r.putInt("ExtraState",X_SUPPER);GoatmanWoods.door(l,b,true);say(enrolled,"A cousin: Food's on the table. Come inside before it gets cold.");}
        if(!fresh(r)&&clock==GRUMBLE)say(enrolled,"A cousin: Who had two? There was one for everybody.");
        if(!fresh(r)&&clock==EXTRA_OUT&&r.getInt("ExtraState")==X_SUPPER)r.putInt("ExtraState",X_FIRE);
        if(clock>=backAt&&clock<silence&&r.getInt("RunnerState")==R_AWAY){
            Vec3 at=abs(GoatmanWoods.path(27),b);GoatmanChild c=GoatmanRegistry.CHILD.get().create(l);
            if(c!=null){int i=r.getInt("Runner");c.addTag(ACTOR);c.getPersistentData().putUUID(ROUND,r.getUUID("Id"));c.getPersistentData().putInt(INDEX,i);c.appearance(r.getInt("Skin"+i),0,null);
                c.moveTo(at.x,at.y,at.z);l.addFreshEntity(c);List<Vec3> back=new ArrayList<>();for(double s:new double[]{30,33,37,41,43,48,53,58,63,66})back.add(GoatmanWoods.path(s));back.addAll(List.of(UP[0],UP[1],UP[2],UP[3]));
                setRoute(c,back,"step");r.putInt("RunnerState",R_RETURNING);say(enrolled,"From the trail: “Wait up! Don't lock it!”");}
        }
        if(r.getInt("RunnerState")==R_KNOCKING&&!GoatmanWoods.doorOpen(l,b)){
            // 0.4.121: the words first, the moment he reaches the door, then two knocks, every five seconds rather than three every three.
            if(!r.contains("RunnerKnockFrom"))r.putInt("RunnerKnockFrom",clock);
            int since=clock-r.getInt("RunnerKnockFrom"),beat=Math.floorMod(since,RUNNER_KNOCK_EVERY);
            if(beat==20||beat==26){knock(l,b,enrolled);r.putBoolean(GoatmanFear.DOOR_KNOCKED,true);GoatmanFear.heard(r);}
            if(beat==0){int line=Math.floorMod(since/RUNNER_KNOCK_EVERY,RUNNER_LINES.length);r.putInt("Demand",-(line+1));r.putInt("DemandAt",clock);}
        }else r.remove("RunnerKnockFrom");
        if(clock==silence)quiet(l,b,r,enrolled,clock);
    }
    /** The woods go quiet: the porch light goes out, and a cousin still outside stops in the middle of a word and is not there. */
    private static void quiet(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> enrolled,int clock){
        GoatmanFear.quiet(r);
        r.putBoolean("Silence",true);GoatmanWoods.porchLight(l,b,false);
        for(var p:enrolled){p.connection.send(new ClientboundStopSoundPacket(GoatmanRegistry.CRICKETS.getId(),SoundSource.AMBIENT));p.connection.send(new ClientboundStopSoundPacket(GoatmanRegistry.WOODS.getId(),SoundSource.AMBIENT));}
        int rs=r.getInt("RunnerState");
        if(rs==R_LEAVING||rs==R_AWAY||rs==R_RETURNING||rs==R_KNOCKING){r.putInt("RunnerState",R_LOST);
            for(var c:cousinsOf(l,b))if(c.getPersistentData().getInt(INDEX)==r.getInt("Runner"))c.discard();
            // A gas reader still outside when the woods go quiet is not there any more, as the cousin would not be.
            var gas=gasReader(l.getServer(),r);if(gas!=null&&!GoatmanWoods.indoors(rel(gas,b)))lose(gas,r);
            if(rs==R_KNOCKING){r.putInt("Demand",-99);r.putInt("DemandAt",clock);}}
        // A brisk evening's thing waits where it is for the dark, a few seconds off; the night takes it from there.
        if(brisk(r))return;
        if(fresh(r)&&r.getInt("ExtraState")==X_SUPPER)r.putInt("ExtraState",X_FIRE);
        if(r.getInt("ExtraState")==X_FIRE&&!GoatmanStalking.eveningActive(r))r.putInt("ExtraState",X_APPROACH);
    }
    /**
     * 0.4.123: the brisk evening. The others come in as the first frank is plated; the cousin goes for gas with the last, and
     * the one who doesn't belong comes in to take his chair once he is out past the porch. With supper on the table there is
     * a little talk while they eat, the door is pulled to, and he is heard on the trail. At the shut door he says his line
     * and knocks, and the reader decides. Let in (the thing slipping out past him as he comes), or left until his voice stops
     * mid-word, the woods go quiet and the night follows in seconds.
     */
    private static void table(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> enrolled,int clock){
        if(!r.getBoolean("CalledIn04123")&&(Integer.bitCount(r.getInt("Plated0464")&~r.getInt("EmptyPlates0465"))>0||clock>=BRISK_SUPPER)){
            r.putBoolean("CalledIn04123",true);GoatmanWoods.door(l,b,true);say(enrolled,"A cousin: Food's coming. Everybody inside.");
        }
        GoatmanMeal.firstPlate(l,b,r,enrolled);
        int rs=r.getInt("RunnerState");
        if(r.getInt("ExtraState")==X_ACTIVITY&&r.getBoolean("CalledIn04123")&&rs!=R_HOME&&runnerOut(l,b,r))r.putInt("ExtraState",X_SUPPER);
        if(!r.contains("SupperAt04123")&&GoatmanSupper.ready(r)&&GoatmanMeal.supplied(r)){
            r.putInt("SupperAt04123",clock);say(enrolled,"A cousin: Food's on the table. Sit down before it gets cold.");
        }
        if(r.contains("SupperAt04123")){
            GoatmanMeal.seated(l,b,r,enrolled,clock);
            int since=clock-r.getInt("SupperAt04123");
            // An already-started old supper keeps its saved clock and originals; do not restage supplies.
            boolean mealRunning=r.contains("MealSeatedAt137")||!r.getBoolean("MealStarted137");
            int away=r.contains("MealSeatedAt137")?clock-r.getInt("MealSeatedAt137"):mealRunning?since:0;
            // Pulled to before he is heard on the trail, never once he is coming or across him.
            if(away>=TABLE_SHUT&&!r.getBoolean("Shut04123")&&(rs==R_AWAY||rs==R_LEAVING)&&shutDoor(l,b,r,enrolled))r.putBoolean("Shut04123",true);
            if(rs!=R_HOME&&away==TABLE_PLACE)say(enrolled,forRunner(r,"A cousin: Save his place. He'll be back."));
            if(!byReader(r)&&mealRunning&&away>=GoatmanMeal.MEAL_TICKS&&rs==R_AWAY&&!r.contains("QuietAt04123")){
                Vec3 at=abs(GoatmanWoods.path(BRISK_CALL),b);GoatmanChild c=GoatmanRegistry.CHILD.get().create(l);
                if(c!=null){int i=r.getInt("Runner");c.addTag(ACTOR);c.getPersistentData().putUUID(ROUND,r.getUUID("Id"));c.getPersistentData().putInt(INDEX,i);c.appearance(r.getInt("Skin"+i),0,null);
                    c.moveTo(at.x,at.y,at.z);l.addFreshEntity(c);List<Vec3> back=new ArrayList<>();for(double s:new double[]{48,53,58,63,66})back.add(GoatmanWoods.path(s));back.addAll(List.of(UP[0],UP[1],UP[2],UP[3]));
                    setRoute(c,back,"step");r.putInt("RunnerState",rs=R_RETURNING);say(enrolled,"From the trail: “Wait up! Don't lock it!”");}
            }
            // Something went wrong upstream (he never got away to come back): the dark still comes.
            if(mealRunning&&away>=TABLE_LATEST&&!r.contains("QuietAt04123"))schedule(r,clock,0);
        }
        // His line the moment he reaches the shut door, then two knocks, every five seconds (0.4.121). A gas reader knocks for themselves.
        if(rs==R_KNOCKING&&!byReader(r)&&!GoatmanWoods.doorOpen(l,b)&&!r.getBoolean("Silence")){
            if(!r.contains("RunnerKnockFrom"))r.putInt("RunnerKnockFrom",clock);
            if(!r.contains("KnockAt04123"))r.putInt("KnockAt04123",clock);
            int knocked=clock-r.getInt("RunnerKnockFrom"),beat=Math.floorMod(knocked,RUNNER_KNOCK_EVERY);
            if(beat==20||beat==26){knock(l,b,enrolled);r.putBoolean(GoatmanFear.DOOR_KNOCKED,true);GoatmanFear.heard(r);}
            if(beat==0){r.putInt("Demand",-(Math.floorMod(knocked/RUNNER_KNOCK_EVERY,RUNNER_LINES.length)+1));r.putInt("DemandAt",clock);}
        }else r.remove("RunnerKnockFrom");
        if(!r.contains("QuietAt04123")){
            boolean in=rs==R_INSIDE&&(!r.getBoolean("Exchange0497")||r.contains("ExchangeOut0497")&&r.contains("ExchangeIn0497"));
            if(in)schedule(r,clock,QUIET_AFTER);
            // Once the door has opened for him he is not cut off, even if the thing is still crossing the other way.
            else if(rs==R_KNOCKING&&!r.getBoolean("Exchange0497")&&r.contains("KnockAt04123")&&clock-r.getInt("KnockAt04123")>=KNOCK_FOR)schedule(r,clock,0);
        }
        if(!r.getBoolean("Silence")&&r.contains("QuietAt04123")&&clock>=r.getInt("QuietAt04123"))quiet(l,b,r,enrolled,clock);
    }
    private static void schedule(CompoundTag r,int clock,int quiet){r.putInt("QuietAt04123",clock+quiet);r.putInt("NightAt04123",clock+quiet+DARK_AFTER-QUIET_AFTER);}
    /** He is gone, or out past the porch on his way: the one who doesn't belong can come in to his chair without passing him. */
    private static boolean runnerOut(ServerLevel l,BlockPos b,CompoundTag r){
        int rs=r.getInt("RunnerState");
        // The gas reader is out of the way once they are outdoors and clear of the door, until they are back.
        if(byReader(r)){if(rs==R_HOME||rs==R_INSIDE)return false;var p=gasReader(l.getServer(),r);return p==null||!GoatmanWoods.indoors(rel(p,b))&&p.position().distanceTo(Vec3.atCenterOf(b.offset(GoatmanWoods.DOOR)))>4;}
        if(rs!=R_LEAVING)return rs!=R_HOME;
        return cousinsOf(l,b).stream().filter(c->c.getPersistentData().getInt(INDEX)==r.getInt("Runner")).allMatch(c->rel(c,b).z>-52);
    }
    /** A cousin pulls the door to once everyone at the table is in, never through somebody standing in it. */
    private static boolean shutDoor(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> enrolled){
        if(!GoatmanWoods.doorOpen(l,b))return true;
        for(var c:cousinsOf(l,b))if(c.getPersistentData().getInt(INDEX)!=r.getInt("Runner")&&(!GoatmanWoods.indoors(rel(c,b))||!c.getPersistentData().getList("Route",Tag.TAG_COMPOUND).isEmpty()))return false;
        var doorway=new AABB(b.offset(GoatmanWoods.DOOR)).expandTowards(0,1,0).inflate(.1);
        if(!l.getEntitiesOfClass(LivingEntity.class,doorway,LivingEntity::isAlive).isEmpty())return false;
        GoatmanWoods.door(l,b,false);say(enrolled,"A cousin: Can somebody get the door? The bugs are getting in.");return true;
    }
    /** Night falls: a cousin locks the door on whatever is outside, and anyone outside finds themselves in. */
    private static void nightfall(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> enrolled){
        r.putInt("Phase",VIGIL);
        int xs=r.getInt("ExtraState");if(xs==X_APPROACH||xs==X_FIRE)r.putInt("ExtraState",X_DOOR);
        if(GoatmanWoods.doorOpen(l,b))say(enrolled,"A cousin: Lock it.");
        for(var p:enrolled)HousePackets.send(p,new HouseFadePayload(8,8,14));
        GoatmanWoods.atmosphere(l,b,true);GoatmanWoods.door(l,b,false);GoatmanWoods.porchLight(l,b,false);
        for(var c:cousinsOf(l,b))if(!fresh(r)&&c.getPersistentData().getInt(INDEX)==r.getInt("Wrong")&&r.getInt("ExtraState")==X_DOOR)settle(c,b,"step");
    }
    private static boolean outside(CompoundTag r){int xs=r.getInt("ExtraState");return xs==X_FIRE||xs==X_APPROACH||xs==X_DOOR;}
    private static boolean dawnReady(CompoundTag r,int clock){
        if(clock<vigilTicks(r))return false;
        if(r.getInt("ExtraState")==X_WINDOW)return false;
        if(!fresh(r))return true;
        if(outside(r)&&(!GoatmanStalking.circuitDone(r)||!GoatmanStalking.windowApproachDone(r)))return false;
        // A delayed arrival retains the original occupied window-to-dawn interval.
        return !r.contains("NightWindowAt0472")||clock>=r.getInt("NightWindowAt0472")+dawnAfterWindow(r);
    }
    private static void night(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> enrolled,int clock){
        GoatmanWoods.door(l,b,false);
        if(r.getBoolean("WindowRefused04123"))GoatmanBedtime.call(l,b,r,enrolled);
        if(outside(r)){
            if(fresh(r)){if(!r.contains("BedQuietAt131"))GoatmanStalking.assault(l,b,r,enrolled,clock);}
            else if(clock>=KNOCK_FROM&&clock<KNOCK_UNTIL){int beat=(clock-KNOCK_FROM)%80;if(beat==0||beat==7||beat==15){GoatmanFear.begin(r);r.putBoolean(GoatmanFear.DOOR_KNOCKED,true);GoatmanFear.heard(r);hammer(l,b,enrolled,beat==0);}
                if(beat==0){r.putInt("Demand",1+((clock-KNOCK_FROM)/200)%LEGACY_MIMIC_LINES.length);r.putInt("DemandAt",clock);}}
            // The window: left open, it is how it gets in.
            // A timed admission cannot cancel a physically unfinished circuit.
            if(fresh(r)&&clock>=windowFrom(r)&&GoatmanStalking.circuitDone(r)&&GoatmanStalking.windowApproachDone(r)&&!r.contains("NightWindowAt0472"))r.putInt("NightWindowAt0472",clock);
            int windowAt=WINDOW_TRY;
            if(fresh(r))windowAt=r.contains("NightWindowAt0472")?r.getInt("NightWindowAt0472"):-1;
            if(windowAt>=0&&clock>=windowAt&&clock<(fresh(r)?windowAt+WINDOW_FOR0465:KNOCK_UNTIL)){
                if(!GoatmanWoods.windowShut(l,b)){
                    var entrant=cousinsOf(l,b).stream().filter(c->c.getPersistentData().getInt(INDEX)==r.getInt("Wrong")).findFirst().orElse(null);
                    if(entrant==null||!GoatmanStalking.admissionDisguise(l,r,entrant))return;
                    r.putInt("ExtraState",X_WINDOW);GoatmanWoods.bathroomDoor(l,b,true);GoatmanBedtime.call(l,b,r,enrolled);
                    for(var c:cousinsOf(l,b))if(c.getPersistentData().getInt(INDEX)==r.getInt("Wrong")){
                        // In under the sash, out through the bathroom door (its doorway is the cell at z -75), up the east aisle.
                        var through=new ArrayList<Vec3>();
                        if(fresh(r)){
                            through.addAll(List.of(WINDOW_ENTRY));c.setCrawling(false);c.setPose(Pose.SWIMMING);c.setWindowEntry(0);c.getPersistentData().putInt("WindowReach04114",0);
                        }else{Vec3 at=abs(new Vec3(6.5,1,-75.5),b);c.moveTo(at.x,at.y,at.z);}
                        c.indoors(false);c.getPersistentData().putString("At","bath");through.addAll(List.of(new Vec3(4.5,1,-75.5),new Vec3(4.5,1,-74.5),new Vec3(3.5,1,-74.5),new Vec3(2.5,1,-74.5),new Vec3(2.5,1,-72.5),new Vec3(4,1,-71.5),new Vec3(4,1,-69.5)));setRoute(c,through,"admitted131");c.getPersistentData().putInt("WindowRoute04114",fresh(r)?through.size():0);}
                }else if(brisk(r)&&clock==windowAt){
                    // Found shut when it is tried: morning comes soon after, rather than after the whole attempt.
                    r.putBoolean("WindowRefused04123",true);
                    GoatmanBedtime.call(l,b,r,enrolled);
                }else if(!fresh(r)&&clock==windowAt){
                    Vec3 w=Vec3.atCenterOf(b.offset(GoatmanWoods.WINDOW));
                    for(var p:enrolled)p.connection.send(new ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(GoatmanRegistry.CLAW.get()),SoundSource.BLOCKS,w.x,w.y,w.z,3.2F,1.15F,p.getRandom().nextLong()));
                }
            }
            if(clock==KEEN&&!r.contains("BedQuietAt131"))for(var p:enrolled)p.connection.send(new ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(GoatmanRegistry.KEEN.get()),SoundSource.AMBIENT,b.getX()+19.5,b.getY()+3,b.getZ()-70.5,1.2F,1,p.getRandom().nextLong()));
        }
        if(clock==CHECK&&GoatmanFear.stage(r)>0&&!GoatmanBedtime.quiet(r))GoatmanFear.queue(r,27);
        if(clock==BEDTIME&&r.getInt("ExtraState")==X_INSIDE)r.putInt("ExtraState",X_FLOOR);
        if(clock>=BEDTIME&&(r.getInt("ExtraState")==X_INSIDE||r.getInt("ExtraState")==X_FLOOR))GoatmanBedtime.call(l,b,r,enrolled);
        if(brisk(r)&&clock>=60&&r.getInt("ExtraState")==X_INSIDE)GoatmanBedtime.call(l,b,r,enrolled);
        // Copper motes where the smell is strongest: the door, and the window it tries.
        if(outside(r)&&clock%10==0){Vec3 at=Vec3.atCenterOf(b.offset(GoatmanWoods.DOOR));l.sendParticles(GoatmanRegistry.COPPER.get(),at.x,at.y+.3,at.z-.4,3,.5,.6,.2,.002);}
    }
    /** 0.4.150: the trailer's line in the Witness account; {@code alone} is the line for a reader who sat the night out alone. */
    public static String witness(CompoundTag personal,String alone){
        var with=new ArrayList<String>();for(var t:personal.getList("With0150",Tag.TAG_STRING))with.add(t.getAsString());
        if(!personal.getBoolean("Drafted0150")||with.isEmpty())return alone;
        String names=with.size()==1?with.getFirst():String.join(", ",with.subList(0,with.size()-1))+" and "+with.getLast();
        return "I came up the trail late with "+names+(personal.getBoolean("Gas0150")?". I was the one who went out across the yard to fill the generator while the others ate.":personal.getBoolean("StoodIn0150")?", and the cousins had kept my place at the table.":".")
                +" There were more of us than there were places at the table. Someone knocked at the door and used a voice we knew. We kept it closed until the knocking stopped. I still could not say who had been outside.";
    }
    /** Morning: what the night was, for every child who sat all of it out. */
    private static void dawn(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> enrolled,LabyrinthData d){
        r.putInt("Phase",DAWN);GoatmanWoods.atmosphere(l,b,false);GoatmanWoods.door(l,b,true);GoatmanWoods.porchLight(l,b,true);r.putBoolean("Silence",false);
        boolean right=outside(r)&&r.getInt("RunnerState")!=R_LOST;r.putBoolean("Right",right);
        int wrong=r.getInt("Wrong"),runner=r.getInt("Runner");boolean took=r.getInt("RunnerState")==R_LOST;
        int skin=r.getInt("Skin"+(took?runner:wrong)),tells=took?0:r.getInt("Tells");
        CompoundTag cohort=r.getCompound("Cohort");
        for(var p:enrolled){
            String key=p.getUUID().toString();CompoundTag own=cohort.getCompound(key);if(!own.getBoolean("Active")||own.getBoolean("Failed"))continue;
            boolean whole=own.getInt("Vigil")>=vigilTicks(r)&&!own.getBoolean("Interrupted");boolean haunted=GoatmanHaunt.haunted(d,p.getUUID());
            if(right&&whole){
                if(p.isSleeping())GoatmanBedtime.wake(p);
                CompoundTag personal=personal(d,p.getUUID());personal.putBoolean("Finished",true);
                // 0.4.150: who else sat the night out, and whether this reader had taken a cousin's place, for the account.
                var with=new ListTag();for(var other:enrolled){var theirs=cohort.getCompound(other.getUUID().toString());if(other!=p&&theirs.getBoolean("Active")&&!theirs.getBoolean("Failed"))with.add(StringTag.valueOf(other.getGameProfile().getName()));}
                personal.put("With0150",with);personal.putBoolean("StoodIn0150",own.contains(PLAYS));personal.putBoolean("Gas0150",byReader(r)&&r.getUUID(GAS).equals(p.getUUID()));personal.putBoolean("Drafted0150",true);
                if(!personal.getBoolean("CounterGiven")){personal.putBoolean("CounterGiven",true);var counter=TallyCounterItem.forReader(p.getUUID());if(!p.getInventory().add(counter))p.drop(counter,false);}
                savePersonal(d,p.getUUID(),personal);
                WitnessAccount.resolve(p,WitnessAccount.Story.GOATMAN,"counted_right");own.putBoolean("Resolved",true);
                if(haunted){GoatmanHaunt.cure(p);GoatmanHaunt.walkOut(p,b,personal.getInt("HauntSkin"),personal.getInt("HauntTells"),true);}
                else GoatmanHaunt.treeline(p,b);
            }else if(!right&&!own.getBoolean("BedsideEnded131")){
                GoatmanHaunt.haunt(p,skin,tells);GoatmanHaunt.walkOut(p,b,skin,tells,false);own.putBoolean("TookItHome",true);
            }
            if(own.getBoolean("BedsideEnded131")){
                var personal=personal(d,p.getUUID());personal.putBoolean("EncounterEnded131",true);savePersonal(d,p.getUUID(),personal);
                GoatmanBedtime.wake(p);
            }
            cohort.put(key,own);
        }
        r.put("Cohort",cohort);
        // The thing is not among them in the morning; whatever walks out with each of them is their own to see.
        for(var c:cousinsOf(l,b))if(c.getPersistentData().getInt(INDEX)==wrong)c.discard();
        say(enrolled,right?"A cousin: I think it stopped.":"A cousin: I think it stopped. Everybody here?");
    }
    private static void ambience(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> enrolled,int tick){
        int phase=r.getInt("Phase");
        if(phase<VIGIL&&!r.getBoolean("Silence")){
            if(tick%180==0)l.playSound(null,b.offset(5,1,-40),GoatmanRegistry.WOODS.get(),SoundSource.AMBIENT,.35F,1);
            if(phase==GATHERING&&tick%400==0){var at=new BlockPos[]{b.offset(-14,1,-60),b.offset(14,1,-50),b.offset(-10,1,-76)}[(tick/400)%3];l.playSound(null,at,GoatmanRegistry.CRICKETS.get(),SoundSource.AMBIENT,.35F,1);}
        }
        // The fire gutters and the camp fills with copper motes once the woods go quiet.
        if(phase==GATHERING&&r.getBoolean("Silence")&&tick%5==0){Vec3 f=Vec3.atCenterOf(b.offset(GoatmanWoods.FIRE));l.sendParticles(GoatmanRegistry.COPPER.get(),f.x,f.y+.6,f.z,4,1.6,.8,1.6,.003);
            l.sendParticles(ParticleTypes.SMOKE,f.x,f.y+.4,f.z,2,.15,.1,.15,.01);}
    }
    /** What a child's client shows: the phase, a line at the door, and whether the woods have gone quiet. */
    public static int speechTicks(int demand){return demand>=107?150:demand>=101?100:60;}
    /** A brisk night's words come closer together, so each holds the middle of the screen a little less long. */
    public static int speechTicks(CompoundTag r,int demand){return brisk(r)&&demand>=101?(demand>=107?110:70):speechTicks(demand);}
    public static boolean speaking(CompoundTag r){if(GoatmanBedtime.quiet(r))return false;int demand=r.getInt("Demand"),since=r.getInt("Clock")-r.getInt("DemandAt");return demand!=0&&since>=0&&since<speechTicks(r,demand);}
    public static GoatmanScenePayload scenePayload(CompoundTag r){
        int clock=r.getInt("Clock"),demand=r.getInt("Demand"),since=clock-r.getInt("DemandAt");
        boolean voice=speaking(r);int child=r.getInt("FearLine0472"),remaining=GoatmanFear.remaining(r),phase=r.getInt("Phase");
        boolean childSpeaking=!GoatmanBedtime.quiet(r)&&!voice&&(phase==GATHERING||phase==VIGIL)&&remaining>0;
        return new GoatmanScenePayload(phase,voice?demand:0,voice?speechTicks(r,demand)-since:0,r.getBoolean("Silence")?1:0,childSpeaking?child:0,childSpeaking?remaining:0);
    }
    static void scene(ServerPlayer p,CompoundTag r){var state=scenePayload(r);int flags=state.flags();if(GoatmanBedtime.called(r)&&p.isSleeping()&&!outside(r))flags|=GoatmanScenePayload.BED_WATCH;if(GoatmanBedtime.risen(r)&&p.isSleeping())flags|=GoatmanScenePayload.BED_FADE;HousePackets.send(p,new GoatmanScenePayload(state.phase(),state.demand(),state.remaining(),flags,state.cousinLine(),state.cousinRemaining()));}
    /** 0.4.145: the bedside visit ends that reader's night. They wake out of the trailer, at home, and it went with them. */
    static void carriedOff(ServerPlayer p){
        var d=LabyrinthData.get(p.server);var personal=personal(d,p.getUUID());personal.putBoolean("EncounterEnded131",true);savePersonal(d,p.getUUID(),personal);
        GoatmanBedtime.wake(p);scale(p,false);HousePackets.send(p,new GoatmanScenePayload(0,0,0,0));GoatmanHaunt.home(p,"It was standing at the foot of the bed. Then you were somewhere else.");
        PlaytestLog.event(p,"goatman_carried_off");
    }
    static void say(List<ServerPlayer> players,String line){for(var p:players)p.displayClientMessage(Component.literal(line),false);}
    /** A real knock: knuckles, an ordinary rhythm. */
    private static void knock(ServerLevel l,BlockPos b,List<ServerPlayer> players){
        Vec3 at=Vec3.atCenterOf(b.offset(GoatmanWoods.DOOR));
        for(var p:players)p.connection.send(new ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(GoatmanRegistry.KNOCK.get()),SoundSource.BLOCKS,at.x,at.y,at.z,3.2F,1.08F,p.getRandom().nextLong()));
    }
    /** Half knocking, half clawing. */
    private static void hammer(ServerLevel l,BlockPos b,List<ServerPlayer> players,boolean claw){
        Vec3 at=Vec3.atCenterOf(b.offset(GoatmanWoods.DOOR));
        for(var p:players)p.connection.send(new ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder((claw?GoatmanRegistry.CLAW:GoatmanRegistry.HAMMER).get()),SoundSource.BLOCKS,at.x,at.y,at.z,3.4F,.91F+p.getRandom().nextFloat()*.15F,p.getRandom().nextLong()));
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.OAK_PLANKS.defaultBlockState()),at.x,at.y+.4,at.z+.05,10,.38,.55,.08,.012);
    }

    // ------------------------------------------------------------------------------------------------ pets
    public static boolean companionScene(ServerPlayer p){
        if(!inside(p))return false;CompoundTag r=run(LabyrinthData.get(p.server));int phase=r.getInt("Phase");
        return phase>0&&(phase<VIGIL&&!r.getCompound("Cohort").getCompound(p.getUUID().toString()).getBoolean("Arrived")||phase==VIGIL||phase==GATHERING&&(r.getInt("Tells")&PET)!=0);
    }
    /** A refusal is bodily: even a DEEPER order cannot send a pet in front on the path. */
    public static boolean companion(TamableAnimal pet,ServerPlayer p){
        if(!inside(p)||pet.level()!=p.level()||pet.isLeashed()||pet.isPassenger()||pet.isOrderedToSit()||!p.getUUID().equals(CompanionOrders.owner(pet)))return false;
        BlockPos b=base(p.server);CompoundTag r=run(LabyrinthData.get(p.server)),own=r.getCompound("Cohort").getCompound(p.getUUID().toString());int phase=r.getInt("Phase");
        if(phase<VIGIL&&!own.getBoolean("Arrived")){
            double playerProgress=GoatmanWoods.project(rel(p,b)).progress(),petProgress=GoatmanWoods.project(rel(pet,b)).progress();
            pet.getNavigation().stop();Vec3 behind=abs(GoatmanWoods.path(Math.max(0,playerProgress-1.8)),b);
            if(petProgress>playerProgress-.4||pet.distanceToSqr(p)>36){pet.teleportTo(behind.x,behind.y,behind.z);pet.setDeltaMovement(Vec3.ZERO);}
            else if(pet.distanceToSqr(p)>4)pet.getNavigation().moveTo(behind.x,behind.y,behind.z,.85);
            pet.getLookControl().setLookAt(p,30,30);return true;
        }
        if(phase==VIGIL||(r.getInt("Tells")&PET)!=0&&phase==GATHERING){
            Entity target=phase==VIGIL?null:cousinsOf(p.serverLevel(),b).stream().filter(c->c.getPersistentData().getInt(INDEX)==r.getInt("Wrong")).findFirst().orElse(null);
            Vec3 at=target==null?Vec3.atCenterOf(b.offset(GoatmanWoods.DOOR)):target.position();
            Vec3 between=p.position().add(at.subtract(p.position()).normalize().scale(1.25));pet.getNavigation().moveTo(between.x,between.y,between.z,.7);pet.getLookControl().setLookAt(at.x,at.y+.6,at.z,60,45);
            if(phase==VIGIL&&p.serverLevel().getGameTime()%160==0)pet.playSound(pet instanceof Cat?SoundEvents.CAT_HISS:SoundEvents.WOLF_GROWL,.6F,.8F);
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------ the one who goes for gas
    /** The generator in the shed across the yard, and how many pulls of its cord it takes to catch. */
    public static final BlockPos GENERATOR=new BlockPos(13,0,-62);
    static final int GAS_PULLS=3,GAS_PULL_GAP=12,GAS_WAIT=200,GAS_REMIND=240;
    /** The reader who goes for gas, if they are here and still part of the evening. */
    static @Nullable ServerPlayer gasReader(MinecraftServer s,CompoundTag r){
        if(!byReader(r))return null;var p=s.getPlayerList().getPlayer(r.getUUID(GAS));if(p==null||!inside(p))return null;
        var own=r.getCompound("Cohort").getCompound(p.getUUID().toString());return own.getBoolean("Active")&&!own.getBoolean("Failed")?p:null;
    }
    /** A line about the cousin who went for gas, said of the reader who went instead: by name, never he or she. */
    public static String forRunner(CompoundTag r,String line){
        if(!byReader(r))return line;String name=r.getString(GAS_NAME),form=gasForm(r);
        if(form.equals("he"))return line;
        if(form.equals("she"))return line.replace("Save his place. He'll be back.","Save her place. She'll be back.").replace("Save one for him when he gets back.","Save one for her when she gets back.");
        return line.replace("Save his place. He'll be back.","Save "+name+"'s place. They'll be back.").replace("Save one for him when he gets back.","Save one for "+name+" for when they get back.");
    }
    /**
     * The meal has begun: the gas reader is sent out to the generator. If they are not here, the cousin whose place they took
     * goes after all, and the reader is one more child when they come back.
     */
    static boolean sendForGas(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> readers){
        if(!byReader(r))return false;var p=gasReader(l.getServer(),r);
        if(p==null||!readers.contains(p)){r.putInt(PLAYED,r.getInt(PLAYED)&~(1<<r.getInt("Runner")));r.remove(GAS);r.remove(GAS_NAME);r.putBoolean("StagePending0469",true);return false;}
        r.putInt("RunnerState",R_LEAVING);r.putInt("RunnerLeftAt134",r.getInt("Clock"));r.putInt("GasRemind0150",r.getInt("Clock"));
        r.putString(GAS_FORM,io.github.knaitoe.theoldesthouse.labyrinth.HouseRead.address(p,"they","he","she","they"));
        say(readers,"A cousin: Generator's out of gas. "+r.getString(GAS_NAME)+", the can's in the shed across the yard. Fill it and pull the cord. We'll save yours.");
        p.displayClientMessage(Component.literal("The generator is in the shed across the yard."),true);return true;
    }
    /** The gas reader's evening, from the doorway out to the shed and back to the camper. */
    private static void gas(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> readers){
        if(!byReader(r)||r.getBoolean("Silence"))return;int rs=r.getInt("RunnerState");if(rs==R_HOME||rs==R_INSIDE||rs==R_LOST)return;
        var p=gasReader(l.getServer(),r);if(p==null||!readers.contains(p))return;
        Vec3 at=rel(p,b);boolean in=GoatmanWoods.indoors(at);int clock=r.getInt("Clock");
        if(rs==R_LEAVING&&!in){r.putInt("RunnerState",rs=R_AWAY);r.putInt("GasAwayAt0150",clock);}
        if((rs==R_LEAVING||rs==R_AWAY)&&clock-r.getInt("GasRemind0150")>=GAS_REMIND){r.putInt("GasRemind0150",clock);p.displayClientMessage(Component.literal("The generator in the shed still needs filling."),true);}
        if(rs!=R_RETURNING&&rs!=R_KNOCKING)return;
        boolean open=GoatmanWoods.doorOpen(l,b),near=!in&&p.position().distanceTo(Vec3.atCenterOf(b.offset(GoatmanWoods.DOOR)))<6;
        if(near&&rs==R_RETURNING&&!open){r.putInt("RunnerState",rs=R_KNOCKING);r.putInt("KnockAt04123",clock);}
        // As they come back in, the one in their chair runs out past them, as it did past the cousin.
        if(open&&(near||in))gasExchange(l,b,r);
        if(in){r.putInt("RunnerState",R_INSIDE);if(!r.contains("ExchangeIn0497"))r.putInt("ExchangeIn0497",clock);gasExchange(l,b,r);GoatmanFear.returned(r);}
    }
    private static void gasExchange(ServerLevel l,BlockPos b,CompoundTag r){
        if(r.getBoolean("Exchange0497"))return;int xs=r.getInt("ExtraState");if(xs!=X_SUPPER&&xs!=X_ACTIVITY)return;
        var extra=cousinsOf(l,b).stream().filter(c->c.getPersistentData().getInt(INDEX)==r.getInt("Wrong")).findFirst().orElse(null);if(extra==null)return;
        r.putBoolean("Exchange0497",true);r.putInt("ExtraState",X_FIRE);setRoute(extra,exchangeLane(onward(extra,"byFire"),false),"byFire");
    }
    /** A pull of the generator's cord: it catches on the third, once the one in their chair has sat down or a while has passed. */
    private static boolean generator(ServerPlayer p){
        var d=LabyrinthData.get(p.server);var r=run(d);if(!byReader(r)||!r.getUUID(GAS).equals(p.getUUID())||r.getInt("Phase")!=GATHERING)return false;
        int rs=r.getInt("RunnerState");BlockPos b=base(p.server);var at=b.offset(GENERATOR);
        if(rs!=R_LEAVING&&rs!=R_AWAY){p.displayClientMessage(Component.literal(rs==R_HOME?"It's still running. For now.":"It's running."),true);return true;}
        long now=p.serverLevel().getGameTime();if(now-r.getLong("GasPullAt0150")<GAS_PULL_GAP)return true;r.putLong("GasPullAt0150",now);
        int pulls=r.getInt("GasPulls0150")+1;r.putInt("GasPulls0150",pulls);
        p.serverLevel().playSound(null,at,SoundEvents.FISHING_BOBBER_RETRIEVE,SoundSource.BLOCKS,.9F,.6F);
        boolean seated=r.getInt("ExtraState")!=X_ACTIVITY,waited=r.getInt("Clock")-r.getInt("GasAwayAt0150")>=GAS_WAIT;
        if(pulls<GAS_PULLS||!seated&&!waited){p.serverLevel().playSound(null,at,SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.5F,.7F);
            p.displayClientMessage(Component.literal(pulls==1?"You tip the can in and pull the cord. It coughs and dies.":"It coughs and dies."),true);saveRun(d,r);return true;}
        p.serverLevel().playSound(null,at,SoundEvents.BLASTFURNACE_FIRE_CRACKLE,SoundSource.BLOCKS,1.2F,.5F);
        p.displayClientMessage(Component.literal("The generator catches and settles into a rattle. Back to the camper."),true);
        r.putInt("RunnerState",R_RETURNING);saveRun(d,r);return true;
    }
    /** The gas reader at the shut door, from outside: they knock, and somebody inside has to open it. */
    private static boolean knockToComeIn(ServerPlayer p,BlockPos b,CompoundTag r){
        if(!byReader(r)||!r.getUUID(GAS).equals(p.getUUID())||r.getInt("Phase")!=GATHERING||GoatmanWoods.indoors(rel(p,b))||GoatmanWoods.doorOpen(p.serverLevel(),b))return false;
        int rs=r.getInt("RunnerState");if(rs==R_HOME||rs==R_INSIDE||rs==R_LOST)return false;
        long now=p.serverLevel().getGameTime();if(now-p.getPersistentData().getLong("GasKnock0150")<10)return true;p.getPersistentData().putLong("GasKnock0150",now);
        var d=LabyrinthData.get(p.server);if(rs==R_RETURNING){r.putInt("RunnerState",R_KNOCKING);r.putInt("KnockAt04123",r.getInt("Clock"));}
        knock(p.serverLevel(),b,visitors(p.serverLevel(),b));r.putBoolean(GoatmanFear.DOOR_KNOCKED,true);GoatmanFear.heard(r);saveRun(d,r);
        p.displayClientMessage(Component.literal("It's shut. Somebody inside has to open it."),true);return true;
    }
    /** Still outside when the woods go quiet: gone, with the face of the cousin whose place they had. */
    private static void lose(ServerPlayer p,CompoundTag r){
        var cohort=r.getCompound("Cohort");var own=cohort.getCompound(p.getUUID().toString());own.putBoolean("Failed",true);own.putBoolean("Active",false);own.putBoolean("LostOutside0150",true);cohort.put(p.getUUID().toString(),own);r.put("Cohort",cohort);
        scale(p,false);HousePackets.send(p,new GoatmanScenePayload(0,0,0,0));GoatmanHaunt.taken(p,r.getInt("Skin"+r.getInt("Runner")),0);PlaytestLog.event(p,"goatman_lost_outside");
    }

    // ------------------------------------------------------------------------------------------------ what a child can touch
    /** Called only by a reachable, native trailer-door interaction. */
    public static boolean openDoor(ServerPlayer p,BlockPos at){
        if(!inside(p)||p.gameMode.getGameModeForPlayer()==GameType.SPECTATOR)return false;BlockPos b=base(p.server);
        if(!at.equals(b.offset(GoatmanWoods.DOOR))&&!at.equals(b.offset(GoatmanWoods.DOOR).above())||p.position().distanceToSqr(Vec3.atCenterOf(at))>25)return false;
        LabyrinthData d=LabyrinthData.get(p.server);CompoundTag r=run(d);
        if(r.getInt("Phase")==VIGIL){
            CompoundTag cohort=r.getCompound("Cohort"),own=cohort.getCompound(p.getUUID().toString());if(!own.getBoolean("Active")||own.getBoolean("Failed"))return true;
            if(!GoatmanBedtime.enabled(r)&&own.getBoolean("BedsideEnded131")){GoatmanWoods.door(p.serverLevel(),b,!GoatmanWoods.doorOpen(p.serverLevel(),b));return true;}
            // Whatever is out there at night is not who it says it is. The one who opens to it is taken, and it with them.
            own.putBoolean("Failed",true);own.putBoolean("Active",false);own.putBoolean("Opened",true);cohort.put(p.getUUID().toString(),own);r.put("Cohort",cohort);saveRun(d,r);
            boolean took=r.getInt("RunnerState")==R_LOST;int who=took?r.getInt("Runner"):r.getInt("Wrong");
            scale(p,false);HousePackets.send(p,new GoatmanScenePayload(0,0,0,0));GoatmanHaunt.taken(p,r.getInt("Skin"+who),took?0:r.getInt("Tells"));
            PlaytestLog.event(p,"goatman_opened");return true;
        }
        if(knockToComeIn(p,b,r))return true;
        var state=p.serverLevel().getBlockState(b.offset(GoatmanWoods.DOOR));boolean open=state.getBlock() instanceof DoorBlock&&!state.getValue(DoorBlock.OPEN);GoatmanWoods.door(p.serverLevel(),b,open);return true;
    }
    public static void onBlock(PlayerInteractEvent.RightClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!inside(p))return;
        if(openDoor(p,e.getPos())){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);return;}
        BlockPos b=base(p.server);
        if(GoatmanSupper.interact(p,e))return;
        if(e.getLevel().getBlockState(e.getPos()).getBlock() instanceof BedBlock){GoatmanBedtime.use(p,b,e.getPos());e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);return;}
        // The bathroom window is an ordinary awning window; anyone may prop it or shut it.
        if(e.getPos().equals(b.offset(GoatmanWoods.WINDOW)))return;
        if(e.getPos().equals(b.offset(GoatmanWoods.STOVE))||e.getPos().equals(b.offset(GoatmanWoods.PAN))){takeBrat(p,b);e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);return;}
        if(e.getPos().equals(b.offset(GENERATOR))&&generator(p)){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);return;}
        // Sleeping would skip the night; containers and props provide no alternative resolution.
        var state=e.getLevel().getBlockState(e.getPos());if(state.getBlock() instanceof BedBlock||state.is(Blocks.CAMPFIRE)||state.is(Blocks.SMOKER)||state.is(Blocks.BARREL)||state.getBlock() instanceof TrapDoorBlock||state.is(Blocks.BLAST_FURNACE)){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    }
    /** One brat each, from the pan, once supper is on. The pan holds what it holds. */
    private static void takeBrat(ServerPlayer p,BlockPos b){
        LabyrinthData d=LabyrinthData.get(p.server);CompoundTag r=run(d);String key=p.getUUID().toString();
        if(r.getInt("Phase")!=GATHERING||r.getInt("Clock")<SUPPER){p.displayClientMessage(Component.literal("They're not done yet."),true);return;}
        CompoundTag taken=r.getCompound("Taken");if(taken.getBoolean(key)){p.displayClientMessage(Component.literal("You've had yours."),true);return;}
        if(r.getInt("Pan")<=0){p.displayClientMessage(Component.literal("The pan is empty."),true);return;}
        r.putInt("Pan",r.getInt("Pan")-1);taken.putBoolean(key,true);r.put("Taken",taken);saveRun(d,r);GoatmanWoods.pan(p.serverLevel(),b,r.getInt("Pan"));
        var brat=new ItemStack(GoatmanRegistry.BRAT.get());if(!p.getInventory().add(brat))p.drop(brat,false);
    }
    public static void onAttack(AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p&&(e.getTarget() instanceof GoatmanChild||e.getTarget() instanceof GoatmanFigure)&&inside(p))e.setCanceled(true);}
    /** A death here (a fall, a command) only ends that child's evening; nothing is taken and nothing follows them for it. */
    public static void onDeath(LivingDeathEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!inBounds(p))return;
        LabyrinthData d=LabyrinthData.get(p.server);CompoundTag r=run(d),cohort=r.getCompound("Cohort"),own=cohort.getCompound(p.getUUID().toString());if(!own.getBoolean("Active"))return;
        own.putBoolean("Failed",true);own.putBoolean("Active",false);cohort.put(p.getUUID().toString(),own);r.put("Cohort",cohort);saveRun(d,r);
        CompoundTag personal=personal(d,p.getUUID());personal.putInt("Failures",personal.getInt("Failures")+1);savePersonal(d,p.getUUID(),personal);
        scale(p,false);HousePackets.send(p,new GoatmanScenePayload(0,0,0,0));
    }
    /** Failures before 0.4.53 still wake once in the manor, as they were promised. */
    public static void onRespawnPosition(PlayerRespawnPositionEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.isFromEndFight()||!personal(LabyrinthData.get(p.server),p.getUUID()).getBoolean("Respawn"))return;
        BlockPos origin=HouseSavedData.get(p.server).houseOrigin();ServerLevel l=p.server.getLevel(HouseDimensions.INTERIOR);if(origin==null||l==null)return;
        Vec3 at=HideAndClap.manorRespawn(origin);l.getChunkAt(BlockPos.containing(at));e.setDimensionTransition(new DimensionTransition(l,at,Vec3.ZERO,0,0,DimensionTransition.DO_NOTHING));
    }
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;scale(p,false);LabyrinthData d=LabyrinthData.get(p.server);CompoundTag own=personal(d,p.getUUID());
        if(own.getBoolean("Respawn")){own.putBoolean("Respawn",false);savePersonal(d,p.getUUID(),own);p.displayClientMessage(Component.literal("You wake in the manor. Your hands are empty."),false);}
    }
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){scale(p,false);if(inside(p))enter(p);}}
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){scale(p,false);HousePackets.send(p,new GoatmanScenePayload(0,0,0,0));}}
}
