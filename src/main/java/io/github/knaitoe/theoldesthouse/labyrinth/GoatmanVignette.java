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
    public static boolean fresh(CompoundTag r){return r.getBoolean("Fresh0465");}
    public static int supperAt(CompoundTag r){return fresh(r)?SUPPER0465:SUPPER;}
    private static int gatherTicks(CompoundTag r){return fresh(r)?GATHER0465:GATHER_TICKS;}
    public static int vigilTicks(CompoundTag r){return fresh(r)?VIGIL0465:VIGIL_TICKS;}
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
    public static final String[] RUNNER_LINES={"Hey! Let me in!","Come on, stop playing! Open the door!","It's me! It's getting dark out here!"};
    public static final String[] LEGACY_MIMIC_LINES={"Let me. In.","in","stop playing","Let me in stop. playing","It's. me.","open the. Door."};
    public static final String[] MIMIC_LINES={"let me in","let. me. in","seriously i'm not playing","i'm not fucking playing let me in","i'm not fucking playing","let me in","let me in let me in let me in let me in"};
    private static final String[] CAMP_STORY={"So he turns on the porch light, and all the knocking stops.","You stole that off my brother.","He stole it off me.","What happened to the guy?","I don't know. I fell asleep.","That's your ending?","Fine. He got up and made toast."};
    /** Where the cousin who went for gas is, and where the one who doesn't belong is. */
    public static final int R_HOME=0,R_LEAVING=1,R_AWAY=2,R_RETURNING=3,R_KNOCKING=4,R_INSIDE=5,R_LOST=6;
    public static final int X_ACTIVITY=0,X_SUPPER=1,X_FIRE=2,X_APPROACH=3,X_DOOR=4,X_INSIDE=5,X_WINDOW=6,X_FLOOR=7;
    private static final ResourceLocation CHILD_SCALE=ResourceLocation.fromNamespaceAndPath(io.github.knaitoe.theoldesthouse.TheOldestHouse.MOD_ID,"trailer_child_scale");
    private static final double WALK=.075,RUN=.18,CREEP=.06;
    /** For the native tests: the cousins cover their routes at once, through every arrival, as though they had walked. */
    private static boolean hurried;
    public static int count(CompoundTag r){return r.contains("Cousins0464")?Math.max(5,Math.min(8,r.getInt("Cousins0464"))):5;}
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
    private static List<ServerPlayer> visitors(ServerLevel l,BlockPos b){return IndianLakeRooms.visitors(l,b,LabyrinthPlace.GOATMAN).stream().filter(p->p.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR).toList();}
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
        r.putInt("Cousins0464",8);r.putBoolean("PlayerServes0464",true);r.putBoolean("Fresh0465",true);int wrong=p.getRandom().nextInt(8),runner=(wrong+1+p.getRandom().nextInt(7))%8;r.putInt("Wrong",wrong);r.putInt("Runner",runner);
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
        if(!member){own.putBoolean("Active",true);own.putDouble("Progress",0);own.putInt("Vigil",0);own.putBoolean("Arrived",false);}
        own.putBoolean("Active",true);cohort.put(p.getUUID().toString(),own);r.put("Cohort",cohort);saveRun(d,r);
        if(!member)p.displayClientMessage(Component.literal("You're late. Your cousins are already at the trailer."),false);
        scale(p,true);IndianLakeRooms.keepLoaded(l,b,LabyrinthPlace.GOATMAN);
        if(!stage(l,b,r)){r.putBoolean("StagePending0469",true);saveRun(d,r);}
        GoatmanWoods.supplies(l,b,count(r)-1+cohort.getAllKeys().size());if(fresh(r))GoatmanWoods.supperChairs(l,b,count(r)-1+cohort.getAllKeys().size());return true;
    }
    /** A fresh evening: no actors, day, the door open, the bathroom window propped, the porch light on, the pan and the plates empty. */
    private static void reset(ServerLevel l,BlockPos b){
        removeActors(l,b);GoatmanWoods.atmosphere(l,b,false);GoatmanWoods.door(l,b,true);GoatmanWoods.setWindow(l,b,false);GoatmanWoods.bathroomDoor(l,b,false);GoatmanWoods.porchLight(l,b,true);
        GoatmanWoods.pan(l,b,0);for(int s=0;s<24;s++)GoatmanWoods.served(l,b,s,false);
        if(l.getBlockEntity(b.offset(GoatmanWoods.DOOR)) instanceof TrailerDoorBlockEntity door)door.resetStress();
    }
    private static void scale(ServerPlayer p,boolean active){
        var attribute=p.getAttribute(Attributes.SCALE);if(attribute==null)return;
        if(active){if(!attribute.hasModifier(CHILD_SCALE))attribute.addTransientModifier(new AttributeModifier(CHILD_SCALE,-.3,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
        else attribute.removeModifier(CHILD_SCALE);
    }
    public static boolean childScale(ServerPlayer p){return p.getAttribute(Attributes.SCALE)!=null&&p.getAttribute(Attributes.SCALE).hasModifier(CHILD_SCALE);}
    public static void depart(ServerPlayer p){
        scale(p,false);HousePackets.send(p,new GoatmanScenePayload(0,0,0,0));
        LabyrinthData d=LabyrinthData.get(p.server);CompoundTag r=run(d),cohort=r.getCompound("Cohort");String key=p.getUUID().toString();
        if(cohort.contains(key)){CompoundTag own=cohort.getCompound(key);own.putBoolean("Active",false);own.putBoolean("Interrupted",true);cohort.put(key,own);r.put("Cohort",cohort);saveRun(d,r);}
        BlockPos b=base(p.server);if(b!=null)for(var girl:p.serverLevel().getEntitiesOfClass(GoatmanChild.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),c->c.viewer().filter(p.getUUID()::equals).isPresent()&&!c.getTags().contains("TrailerWalk")))girl.discard();
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
        spot("kitchen",new Vec3(-4.5,1,-74.5),180,true,STAND,new Vec3(-3,1,-57.5),new Vec3(-3,1,-72.5));
        spot("cupboard",new Vec3(4,1,-70.5),270,true,STAND,new Vec3(4,1,-57.5),new Vec3(4,1,-70.5));
        spot("floor",new Vec3(.5,1.05,-58.5),0,true,LIE);
        spot("checkDoor",new Vec3(.5,1,-56.2),0,true,STAND);
        spot("fearDoor",new Vec3(.5,1,-56.6),0,true,STAND);
        spot("fearWindow",new Vec3(-3.75,1,-58.75),90,true,STAND,new Vec3(-3,1,-57.5));
        spot("fearPaceA",new Vec3(-3,1,-61.5),180,true,STAND,new Vec3(-3,1,-57.5));
        spot("fearPaceB",new Vec3(-3,1,-70.5),0,true,STAND,new Vec3(-3,1,-57.5));
        spot("fearComfort",new Vec3(4,1,-66.5),90,true,STAND,new Vec3(4,1,-57.5));
        spot("fearEars",new Vec3(4.7,1,-71.5),180,true,COWER,new Vec3(4,1,-57.5),new Vec3(4,1,-71.5));
        spot("fearStartle",new Vec3(4.5,1,-59.5),0,true,STAND,new Vec3(4,1,-57.5));
        for(int i=0;i<8;i++)spot("cower"+i,new Vec3(i%2==0?-3.8:4.8,1,-65.5-(i/2)*2),180,true,COWER,new Vec3(i%2==0?-3:4,1,-57.5),new Vec3(i%2==0?-3:4,1,-65.5-(i/2)*2));
        for(int s=0;s<10;s++){boolean west=s%2==0;double z=GoatmanWoods.CHAIR_Z[s/2]+.5;
            spot("seat"+s,new Vec3(west?-1.25:2.25,1.5,z),west?270:90,true,SIT,new Vec3(west?-3:4,1,-57.5),new Vec3(west?-3:4,1,z));}
        for(int k=0;k<10;k++){var foot=GoatmanWoods.bunk(k);if(foot.getY()!=1)continue;boolean west=k%2==0;double z=foot.getZ()+.5;
            SPOTS.put("bunk"+k,new Spot(new Vec3(foot.getX()+1.5,1.6875,z),west?270:270,true,LIE,new Vec3[]{new Vec3(west?-3:4,1,-57.5),new Vec3(west?-3:4,1,z),new Vec3(west?-3.6:4.4,1,z)},foot.east()));}
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
        if(hurried)speed=1000;
        ListTag route=c.getPersistentData().getList("Route",Tag.TAG_COMPOUND);
        if(route.isEmpty())return true;
        CompoundTag next=route.getCompound(0);Vec3 target=abs(new Vec3(next.getDouble("X"),next.getDouble("Y"),next.getDouble("Z")),b);
        Vec3 here=c.position(),delta=target.subtract(here);double length=delta.length();
        Vec3 to=length<=speed?target:here.add(delta.scale(speed/length));
        float yaw=Math.abs(delta.x)+Math.abs(delta.z)>.01?(float)(Math.atan2(delta.z,delta.x)*180/Math.PI)-90:c.getYRot();
        c.moveTo(to.x,to.y,to.z,yaw,0);c.setYHeadRot(yaw);c.yBodyRot=yaw;c.animate(Math.min(speed,length),false,false);
        if(!c.tell(SILENT)){double step=c.getPersistentData().getDouble("Step")+Math.min(speed,length);
            if(step>.7){l.playSound(null,c.blockPosition(),c.getY()>=b.getY()+.7?SoundEvents.WOOD_STEP:SoundEvents.GRASS_STEP,SoundSource.NEUTRAL,.3F,1.2F);step=0;}c.getPersistentData().putDouble("Step",step);}
        if(length<=speed){route.remove(0);c.getPersistentData().put("Route",route);}
        return route.isEmpty();
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
                case X_SUPPER,X_INSIDE->seatName(i,r);case X_FIRE->"byFire";case X_APPROACH,X_DOOR->"step";default->"floor";
            };
        }
        if(i==runner&&r.getInt("RunnerState")!=R_HOME&&r.getInt("RunnerState")!=R_INSIDE)return "step";
        if((phase==GATHERING||phase==VIGIL)&&GoatmanFear.stage(r)>=GoatmanFear.FEAR)return GoatmanFear.destination(i,r);
        if(phase>=VIGIL||clock>=supperAt(r))return seatName(i,r);
        return ACTIVITY[i];
    }
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
    private static GoatmanChild girl(ServerPlayer p,BlockPos b,CompoundTag r){
        if(!spawnReady(p.serverLevel(),b))return null;
        var girls=actors(p.serverLevel(),b).stream().filter(c->c.viewer().filter(p.getUUID()::equals).isPresent()&&!c.getTags().contains("TrailerWalk")).toList();
        for(int i=1;i<girls.size();i++)girls.get(i).discard();if(!girls.isEmpty())return girls.getFirst();
        GoatmanChild c=GoatmanRegistry.CHILD.get().create(p.serverLevel());if(c==null)return null;
        c.appearance(5,0,p.getUUID());c.addTag(ACTOR);c.getPersistentData().putUUID(ROUND,r.getUUID("Id"));c.getPersistentData().putInt(INDEX,-1);
        c.moveTo(abs(GoatmanWoods.path(7),b));p.serverLevel().addFreshEntity(c);return c;
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
            GoatmanChild girl=girl(p,b,r);if(girl!=null){Vec3 at=abs(GoatmanWoods.path(progress+7),b);double moved=girl.position().distanceTo(at);girl.moveTo(at);girl.animate(moved,true,false);face(girl,p.position());
                if(progress+7>=54&&(!p.hasLineOfSight(girl)||p.getLookAngle().dot(girl.position().subtract(p.position()).normalize())<.25)){own.putBoolean("GirlGone",true);girl.discard();}
            }
        }else for(var girl:actors(p.serverLevel(),b))if(girl.viewer().filter(p.getUUID()::equals).isPresent()&&!girl.getTags().contains("TrailerWalk"))girl.discard();
        hollows(p,b,own,progress);
        if(GoatmanWoods.clearing(rel(p,b))&&progress>=GoatmanWoods.CLEARING){
            own.putBoolean("Arrived",true);own.putBoolean("GirlGone",true);GoatmanHaunt.vanish(p,"hollow");
            if(!own.getBoolean("Greeted")){own.putBoolean("Greeted",true);p.displayClientMessage(Component.literal("A cousin: There you are. Everyone's here."),false);}
        }
    }
    /** Someone stands in each hollow with its back to the trail while the latecomer comes up on it, then it is gone. */
    private static void hollows(ServerPlayer p,BlockPos b,CompoundTag own,double progress){
        var l=p.serverLevel();
        for(int h=0;h<GoatmanWoods.HOLLOWS.length;h++){
            double at=GoatmanWoods.HOLLOWS[h];String purpose="hollow"+h;boolean want=!own.getBoolean("Arrived")&&progress>=at-14&&progress<=at+2&&!own.getBoolean("Hollow"+h);
            if(progress>at+2)own.putBoolean("Hollow"+h,true);
            var existing=l.getEntitiesOfClass(GoatmanFigure.class,p.getBoundingBox().inflate(40),f->f.viewer().filter(p.getUUID()::equals).isPresent()&&f.purpose.equals(purpose));
            if(!want){existing.forEach(Entity::discard);continue;}
            if(!existing.isEmpty())continue;
            var f=GoatmanRegistry.FIGURE.get().create(l);if(f==null)continue;f.viewer(p.getUUID());f.purpose=purpose;f.until=l.getGameTime()+2400;
            Vec3 pos=abs(GoatmanWoods.HOLLOW_STAND[h],b);f.moveTo(pos.x,pos.y,pos.z,GoatmanWoods.HOLLOW_YAW[h],0);f.face(GoatmanWoods.HOLLOW_YAW[h]);GoatmanHaunt.appear(l,f);
        }
    }

    // ------------------------------------------------------------------------------------------------ the cousins
    private static void cousins(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> present){
        int phase=r.getInt("Phase"),clock=r.getInt("Clock"),wrong=r.getInt("Wrong"),runner=r.getInt("Runner");
        boolean open=GoatmanWoods.doorOpen(l,b);
        for(var c:cousinsOf(l,b)){
            int i=c.getPersistentData().getInt(INDEX);if(i>=count(r))continue;
            if(fresh(r)&&phase==VIGIL&&i==wrong&&outside(r)){GoatmanStalking.step(l,b,r,c,clock);continue;}
            if(!outside(r)&&c.stalkingAppearance()&&GoatmanStalking.obscured(l,c))c.disguise(-1);
            if((phase==GATHERING||phase==VIGIL)&&i!=wrong)GoatmanFear.pose(c,r);else {c.fear(0);c.fearStage(0);}
            String want=place(i,r);double speed=WALK;
            if(i==runner){int rs=r.getInt("RunnerState");
                if(rs==R_LEAVING){if(!goal(c).equals("_trail")&&!at(c).equals("_trail")){List<Vec3> out=toYard(at(c));
                        for(double s:new double[]{66,63,58,53,48,43,41,37,33,30,27})out.add(GoatmanWoods.path(s));setRoute(c,out,"_trail");}
                    if(walk(c,b,WALK,l)){c.getPersistentData().putString("At","_trail");if(!watched(c,present)){c.discard();r.putInt("RunnerState",R_AWAY);}}continue;}
                if(rs==R_RETURNING){speed=RUN;if(walk(c,b,RUN,l)){c.getPersistentData().putString("At","step");r.putInt("RunnerState",open?R_INSIDE:R_KNOCKING);if(open){GoatmanFear.returned(r);setRoute(c,inFrom("step",i,r),seatName(i,r));}}continue;}
                if(rs==R_KNOCKING){face(c,abs(new Vec3(.5,1,-55.5),b));c.pose(false);if(open){r.putInt("RunnerState",R_INSIDE);GoatmanFear.returned(r);setRoute(c,inFrom("step",i,r),seatName(i,r));}continue;}
                if(rs==R_INSIDE&&c.getPersistentData().contains("Route"))speed=RUN;
            }
            if(i==wrong){int xs=r.getInt("ExtraState");
                if(xs==X_FIRE&&phase==GATHERING&&clock>=(fresh(r)?BACK0465:RUNNER_BACK)&&clock<(fresh(r)?SILENCE0465:SILENCE)){speed=RUN;
                    // Once it is out by the fire, it runs to the nearest window, scrapes at the glass where it stands, and goes back.
                    int run=r.getInt("WindowRun0464");
                    if(run==0&&at(c).equals("byFire")){r.putInt("WindowRun0464",run=1);GoatmanFear.departed(r);}
                    if(run==1&&at(c).equals("windowRun")&&c.getPersistentData().getList("Route",Tag.TAG_COMPOUND).isEmpty()){r.putInt("WindowRun0464",run=2);r.putInt("WindowAt0464",clock);}
                    if(run==2){int since=clock-r.getInt("WindowAt0464");
                        if(since%20==0)l.playSound(null,b.offset(-8,2,-59),GoatmanRegistry.CLAW.get(),SoundSource.BLOCKS,2.4F,1.35F);
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
                if(xs==X_WINDOW){if(walk(c,b,CREEP,l)){r.putInt("ExtraState",X_FLOOR);settle(c,b,"floor");}continue;}
                c.heave(xs==X_FIRE&&watched(c,present)&&clock%240<120);
            }
            if(!want.equals(goal(c))){
                if(at(c).isEmpty()){settle(c,b,want);continue;}
                setRoute(c,onward(c,want),want);
            }
            ListTag route=c.getPersistentData().getList("Route",Tag.TAG_COMPOUND);
            if(!route.isEmpty()){if(walk(c,b,speed,l))arrive(c,b,r,l,want);continue;}
            if(!at(c).equals(want)){arrive(c,b,r,l,want);continue;}
            if(phase==GATHERING&&clock>=supperAt(r)&&GoatmanSupper.eat(l,b,r,c,i,clock))say(present,"A cousin: Who had two? There was one for everybody.");
            idle(c,r,present,clock,i);
            if((phase==GATHERING||phase==VIGIL)&&i!=wrong){GoatmanFear.pose(c,r);if(c.fear()==GoatmanFear.WATCH&&c.fearStage()>=GoatmanFear.FEAR)face(c,abs(new Vec3(-8,2,-59),b));}
        }
    }
    /** From wherever a spot is, out to the yard. */
    private static List<Vec3> toYard(String from){
        Spot a=SPOTS.get(from);List<Vec3> out=new ArrayList<>();if(a==null){out.add(YARD);return out;}
        for(int i=a.access().length-1;i>=0;i--)out.add(a.access()[i]);
        if(a.inside()){out.add(ENTRY);for(int i=UP.length-1;i>=0;i--)out.add(UP[i]);}
        out.add(YARD);return out;
    }
    /** From the step through the door to a seat. */
    private static List<Vec3> inFrom(String spot,int i,CompoundTag r){var seat=SPOTS.get(seatName(i,r));List<Vec3> out=new ArrayList<>();out.add(UP[4]);out.add(ENTRY);out.addAll(List.of(seat.access()));out.add(seat.at());return out;}
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
    public static int expectedCount(CompoundTag r){return r.contains("Expected")?r.getInt("Expected"):count(r)-1+r.getCompound("Cohort").getAllKeys().size();}
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
        int expected=fresh(r)?count(r)-1+cohort.getAllKeys().size():count(r)-1+enrolled.size();
        if(phase<VIGIL&&!r.getBoolean("MealFrozen0465")&&r.getInt("Expected")!=expected){r.putInt("Expected",expected);GoatmanWoods.supplies(l,b,expected);if(fresh(r))GoatmanWoods.supperChairs(l,b,expected);}
        boolean arrived=true;int clock=r.getInt("Clock");
        for(ServerPlayer p:enrolled){
            String key=p.getUUID().toString();CompoundTag own=cohort.getCompound(key);scale(p,true);
            if(phase<VIGIL)path(p,b,r,own);
            arrived&=own.getBoolean("Arrived");
            if(phase==VIGIL){
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
            GoatmanSupper.tick(l,b,r);
            if(!fresh(r)||clock!=SUPPER0465-1||GoatmanSupper.ready(r)){clock++;evening(l,b,r,enrolled,clock);}
            if(clock>=gatherTicks(r)){nightfall(l,b,r,enrolled);phase=VIGIL;clock=0;}
        }
        else if(phase==VIGIL){clock++;night(l,b,r,enrolled,clock);if(dawnReady(r,clock)){r.putInt("Clock",clock);dawn(l,b,r,enrolled,d);phase=DAWN;}}
        r.putInt("Clock",clock);
        // Restore saved actors, never reroll a tell or duplicate a cousin after a restart.
        if(server.getTickCount()%20==0||r.getBoolean("StagePending0469"))r.putBoolean("StagePending0469",!stage(l,b,r));cousins(l,b,r,enrolled);
        if(phase==GATHERING||phase==VIGIL)GoatmanFear.tick(r,cousinsOf(l,b),clock);
        saveRun(d,r);
        if(server.getTickCount()%20==0||speaking(r)||clock==r.getInt("FearAt0472"))for(var p:enrolled)scene(p,r);
        ambience(l,b,r,enrolled,server.getTickCount());
    }
    private static void evening(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> enrolled,int clock){
        int supper=supperAt(r),leaves=fresh(r)?LEAVES0465:RUNNER_LEAVES,backAt=fresh(r)?BACK0465:RUNNER_BACK,silence=fresh(r)?SILENCE0465:SILENCE;
        if(fresh(r)){
            if(clock==220)say(enrolled,"A cousin: You should've heard his story. The scary part was how long it took.");
            if(clock==520)say(enrolled,"A cousin: Can somebody pass the plates? We brought enough for all of us.");
            if(clock==820)say(enrolled,"A cousin: You always burn them on one side. Turn them over this time.");
            if(clock==1340)say(enrolled,"A cousin: He knows where the gas is. He'll be back in a minute.");
        }
        // Late joiners before supper get a brat counted for them; nobody gets one counted after.
        if(!r.getBoolean("PlayerServes0464")&&clock<SUPPER&&r.getInt("PanFor")!=r.getInt("Expected")){r.putInt("Pan",r.getInt("Pan")+r.getInt("Expected")-r.getInt("PanFor"));r.putInt("PanFor",r.getInt("Expected"));GoatmanWoods.pan(l,b,r.getInt("Pan"));}
        boolean serves=r.getBoolean("PlayerServes0464");
        if(serves&&clock==SERVE_HINT)say(enrolled,"A cousin: Go grab 'em from the cooler by the kitchen counter. Four in each pack. Cook them on the griddle, then put one on everybody's plate.");
        if(serves&&clock==(fresh(r)?2460:WINDOW_HINT))say(enrolled,"A cousin: Is anyone else getting cold back there?");
        if(!serves&&clock==OLD_WINDOW_HINT)say(enrolled,"A cousin: Somebody shut the bathroom window. Bugs are getting in.");
        if(clock==leaves&&r.getInt("RunnerState")==R_HOME){r.putInt("RunnerState",R_LEAVING);say(enrolled,"A cousin: Generator's out of gas. I'll run get some from the truck. Back before dark.");}
        if(clock==supper){if(r.getInt("ExtraState")==X_ACTIVITY)r.putInt("ExtraState",X_SUPPER);GoatmanWoods.door(l,b,true);say(enrolled,"A cousin: Food's on the table. Come inside before it gets cold.");}
        if(!fresh(r)&&clock==GRUMBLE)say(enrolled,"A cousin: Who had two? There was one for everybody.");
        if(clock==(fresh(r)?2250:EXTRA_OUT)&&r.getInt("ExtraState")==X_SUPPER)r.putInt("ExtraState",X_FIRE);
        if(clock>=backAt&&clock<silence&&r.getInt("RunnerState")==R_AWAY){
            Vec3 at=abs(GoatmanWoods.path(27),b);GoatmanChild c=GoatmanRegistry.CHILD.get().create(l);
            if(c!=null){int i=r.getInt("Runner");c.addTag(ACTOR);c.getPersistentData().putUUID(ROUND,r.getUUID("Id"));c.getPersistentData().putInt(INDEX,i);c.appearance(r.getInt("Skin"+i),0,null);
                c.moveTo(at.x,at.y,at.z);l.addFreshEntity(c);List<Vec3> back=new ArrayList<>();for(double s:new double[]{30,33,37,41,43,48,53,58,63,66})back.add(GoatmanWoods.path(s));back.addAll(List.of(UP[0],UP[1],UP[2],UP[3]));
                setRoute(c,back,"step");r.putInt("RunnerState",R_RETURNING);say(enrolled,"From the trail: “Wait up! Don't lock it!”");}
        }
        if(r.getInt("RunnerState")==R_KNOCKING&&clock%60==0){knock(l,b,enrolled);int line=(clock/60)%RUNNER_LINES.length;r.putInt("Demand",-(line+1));r.putInt("DemandAt",clock);}
        if(clock==silence){
            GoatmanFear.quiet(r);
            r.putBoolean("Silence",true);GoatmanWoods.porchLight(l,b,false);
            for(var p:enrolled){p.connection.send(new ClientboundStopSoundPacket(GoatmanRegistry.CRICKETS.getId(),SoundSource.AMBIENT));p.connection.send(new ClientboundStopSoundPacket(GoatmanRegistry.WOODS.getId(),SoundSource.AMBIENT));}
            int rs=r.getInt("RunnerState");
            // The cousin who was still outside stops knocking in the middle of a word, and is not there.
            if(rs==R_LEAVING||rs==R_AWAY||rs==R_RETURNING||rs==R_KNOCKING){r.putInt("RunnerState",R_LOST);
                for(var c:cousinsOf(l,b))if(c.getPersistentData().getInt(INDEX)==r.getInt("Runner"))c.discard();
                if(rs==R_KNOCKING){r.putInt("Demand",-99);r.putInt("DemandAt",clock);}}
            if(r.getInt("ExtraState")==X_FIRE)r.putInt("ExtraState",X_APPROACH);
        }
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
        if(!fresh(r))return true;
        if(outside(r)&&(!GoatmanStalking.circuitDone(r)||r.getInt("StalkWindowLeg0465")<4))return false;
        // A delayed arrival retains the original occupied window-to-dawn interval.
        return !r.contains("NightWindowAt0472")||clock>=r.getInt("NightWindowAt0472")+VIGIL0465-WINDOW0465;
    }
    private static void night(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> enrolled,int clock){
        GoatmanWoods.door(l,b,false);
        if(outside(r)){
            if(fresh(r))GoatmanStalking.assault(l,b,r,enrolled,clock);
            else if(clock>=KNOCK_FROM&&clock<KNOCK_UNTIL){int beat=(clock-KNOCK_FROM)%80;if(beat==0||beat==7||beat==15){GoatmanFear.begin(r);hammer(l,b,enrolled,beat==0);}
                if(beat==0){r.putInt("Demand",1+((clock-KNOCK_FROM)/200)%LEGACY_MIMIC_LINES.length);r.putInt("DemandAt",clock);}}
            // The window: left open, it is how it gets in.
            // A timed admission cannot cancel a physically unfinished circuit.
            if(fresh(r)&&clock>=WINDOW0465&&GoatmanStalking.circuitDone(r)&&r.getInt("StalkWindowLeg0465")>=4&&!r.contains("NightWindowAt0472"))r.putInt("NightWindowAt0472",clock);
            int windowAt=WINDOW_TRY;
            if(fresh(r))windowAt=r.contains("NightWindowAt0472")?r.getInt("NightWindowAt0472"):-1;
            if(windowAt>=0&&clock>=windowAt&&clock<(fresh(r)?windowAt+WINDOW_FOR0465:KNOCK_UNTIL)){
                if(!GoatmanWoods.windowShut(l,b)){
                    r.putInt("ExtraState",X_WINDOW);GoatmanWoods.bathroomDoor(l,b,true);
                    for(var c:cousinsOf(l,b))if(c.getPersistentData().getInt(INDEX)==r.getInt("Wrong")){
                        // In under the sash, out through the bathroom door (its doorway is the cell at z -75), up the east aisle.
                        var through=new ArrayList<Vec3>();
                        if(fresh(r)){
                            through.add(new Vec3(10.25,0,-74.5));through.add(new Vec3(10.25,3.05,-74.5));through.add(new Vec3(8.5,3.05,-74.5));through.add(new Vec3(7.5,3.05,-74.5));through.add(new Vec3(6.5,1,-75.5));c.setPose(Pose.SWIMMING);
                        }else{Vec3 at=abs(new Vec3(6.5,1,-75.5),b);c.moveTo(at.x,at.y,at.z);}
                        c.getPersistentData().putString("At","bath");through.addAll(List.of(new Vec3(4.5,1,-74.5),new Vec3(3.5,1,-74.5),new Vec3(2.5,1,-74.5),new Vec3(2.5,1,-72.5),new Vec3(4,1,-71.5),new Vec3(4,1,-57.5),ENTRY,SPOTS.get("floor").at()));setRoute(c,through,"floor");}
                }else if(!fresh(r)&&clock==windowAt){
                    Vec3 w=Vec3.atCenterOf(b.offset(GoatmanWoods.WINDOW));
                    for(var p:enrolled)p.connection.send(new ClientboundSoundPacket(Holder.direct(GoatmanRegistry.CLAW.get()),SoundSource.BLOCKS,w.x,w.y,w.z,.9F,1.15F,p.getRandom().nextLong()));
                }
            }
            if(clock==KEEN)for(var p:enrolled)p.connection.send(new ClientboundSoundPacket(Holder.direct(GoatmanRegistry.KEEN.get()),SoundSource.AMBIENT,b.getX()+19.5,b.getY()+3,b.getZ()-70.5,1.2F,1,p.getRandom().nextLong()));
        }
        if(clock==CHECK&&GoatmanFear.stage(r)>0)GoatmanFear.queue(r,27);
        if(clock==BEDTIME&&r.getInt("ExtraState")==X_INSIDE)r.putInt("ExtraState",X_FLOOR);
        // Copper motes where the smell is strongest: the door, and the window it tries.
        if(outside(r)&&clock%10==0){Vec3 at=Vec3.atCenterOf(b.offset(GoatmanWoods.DOOR));l.sendParticles(GoatmanRegistry.COPPER.get(),at.x,at.y+.3,at.z-.4,3,.5,.6,.2,.002);}
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
                CompoundTag personal=personal(d,p.getUUID());personal.putBoolean("Finished",true);
                if(!personal.getBoolean("CounterGiven")){personal.putBoolean("CounterGiven",true);var counter=TallyCounterItem.forReader(p.getUUID());if(!p.getInventory().add(counter))p.drop(counter,false);}
                savePersonal(d,p.getUUID(),personal);
                WitnessAccount.resolve(p,WitnessAccount.Story.GOATMAN,"counted_right");own.putBoolean("Resolved",true);
                if(haunted){GoatmanHaunt.cure(p);GoatmanHaunt.walkOut(p,b,personal.getInt("HauntSkin"),personal.getInt("HauntTells"),true);}
                else GoatmanHaunt.treeline(p,b);
            }else if(!right){
                GoatmanHaunt.haunt(p,skin,tells);GoatmanHaunt.walkOut(p,b,skin,tells,false);own.putBoolean("TookItHome",true);
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
    public static boolean speaking(CompoundTag r){int demand=r.getInt("Demand"),since=r.getInt("Clock")-r.getInt("DemandAt");return demand!=0&&since>=0&&since<speechTicks(demand);}
    public static GoatmanScenePayload scenePayload(CompoundTag r){
        int clock=r.getInt("Clock"),demand=r.getInt("Demand"),since=clock-r.getInt("DemandAt");
        boolean voice=speaking(r);int child=r.getInt("FearLine0472"),remaining=GoatmanFear.remaining(r),phase=r.getInt("Phase");
        boolean childSpeaking=!voice&&(phase==GATHERING||phase==VIGIL)&&remaining>0;
        return new GoatmanScenePayload(phase,voice?demand:0,voice?speechTicks(demand)-since:0,r.getBoolean("Silence")?1:0,childSpeaking?child:0,childSpeaking?remaining:0);
    }
    private static void scene(ServerPlayer p,CompoundTag r){HousePackets.send(p,scenePayload(r));}
    private static void say(List<ServerPlayer> players,String line){for(var p:players)p.displayClientMessage(Component.literal(line),false);}
    /** A real knock: knuckles, an ordinary rhythm. */
    private static void knock(ServerLevel l,BlockPos b,List<ServerPlayer> players){
        Vec3 at=Vec3.atCenterOf(b.offset(GoatmanWoods.DOOR));
        for(var p:players)p.connection.send(new ClientboundSoundPacket(Holder.direct(LiteraryRegistry.CABIN_KNOCK.get()),SoundSource.BLOCKS,at.x,at.y,at.z,1.1F,1.08F,p.getRandom().nextLong()));
    }
    /** Half knocking, half clawing. */
    private static void hammer(ServerLevel l,BlockPos b,List<ServerPlayer> players,boolean claw){
        Vec3 at=Vec3.atCenterOf(b.offset(GoatmanWoods.DOOR));
        for(var p:players)p.connection.send(new ClientboundSoundPacket(Holder.direct((claw?GoatmanRegistry.CLAW:GoatmanRegistry.HAMMER).get()),SoundSource.BLOCKS,at.x,at.y,at.z,1.35F,.91F+p.getRandom().nextFloat()*.15F,p.getRandom().nextLong()));
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

    // ------------------------------------------------------------------------------------------------ what a child can touch
    /** Called only by a reachable, native trailer-door interaction. */
    public static boolean openDoor(ServerPlayer p,BlockPos at){
        if(!inside(p)||p.gameMode.getGameModeForPlayer()==GameType.SPECTATOR)return false;BlockPos b=base(p.server);
        if(!at.equals(b.offset(GoatmanWoods.DOOR))&&!at.equals(b.offset(GoatmanWoods.DOOR).above())||p.position().distanceToSqr(Vec3.atCenterOf(at))>25)return false;
        LabyrinthData d=LabyrinthData.get(p.server);CompoundTag r=run(d);
        if(r.getInt("Phase")==VIGIL){
            CompoundTag cohort=r.getCompound("Cohort"),own=cohort.getCompound(p.getUUID().toString());if(!own.getBoolean("Active")||own.getBoolean("Failed"))return true;
            // Whatever is out there at night is not who it says it is. The one who opens to it is taken, and it with them.
            own.putBoolean("Failed",true);own.putBoolean("Active",false);own.putBoolean("Opened",true);cohort.put(p.getUUID().toString(),own);r.put("Cohort",cohort);saveRun(d,r);
            boolean took=r.getInt("RunnerState")==R_LOST;int who=took?r.getInt("Runner"):r.getInt("Wrong");
            scale(p,false);HousePackets.send(p,new GoatmanScenePayload(0,0,0,0));GoatmanHaunt.taken(p,r.getInt("Skin"+who),took?0:r.getInt("Tells"));
            PlaytestLog.event(p,"goatman_opened");return true;
        }
        var state=p.serverLevel().getBlockState(b.offset(GoatmanWoods.DOOR));boolean open=state.getBlock() instanceof DoorBlock&&!state.getValue(DoorBlock.OPEN);GoatmanWoods.door(p.serverLevel(),b,open);return true;
    }
    public static void onBlock(PlayerInteractEvent.RightClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!inside(p))return;
        if(openDoor(p,e.getPos())){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);return;}
        BlockPos b=base(p.server);
        if(GoatmanSupper.interact(p,e))return;
        // The bathroom window is an ordinary awning window; anyone may prop it or shut it.
        if(e.getPos().equals(b.offset(GoatmanWoods.WINDOW)))return;
        if(e.getPos().equals(b.offset(GoatmanWoods.STOVE))||e.getPos().equals(b.offset(GoatmanWoods.PAN))){takeBrat(p,b);e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);return;}
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
