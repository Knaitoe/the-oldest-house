package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;

/**
 * 0.4.310, A Deer in the Road (docs/DEER_ROAD_0_4_310.md), after the owner's design "A Deer in the Road: An Understanding of the
 * Bent-Neck Woman". One stretch of county road at night, one wreck, one doe and two bodies for the whole party: the state is the
 * road's (DeerRoadScene), what each reader saw is their own record.
 *
 * <p>The reader comes through the door behind the wheel of a car already moving. A doe steps out; one beat to brake or turn; the
 * strike happens either way, and how the car lands and which door springs open follow from the beat. The hazards tick. The woman from
 * the tunnel comes out of the doe, rises, makes for the driver's door and dies against it, or where she is struck down. The body
 * stays. After a silence the rear belt ratchets tight, the bench gives at the seam, and she comes out of the back seat with the first
 * body still lying where it fell. The second one hunts with the tunnel's stride, and has to come round the car and its doors. Off the
 * road: the service door at the bend (quicker, and safe while the next vehicle, slowed by a triangle and flagged from the hatched
 * shoulder, stands alongside with its lights on her), or the ditch to the drainage gate. Caught: a blackout and the failed return.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class DeerRoad {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.DEER_ROAD;
    /** The woman's appearances on the road (LiteraryActor.ROAD_WOMAN). */
    public static final int HIDDEN=0,EMERGE=1,STAND=2,STRIDE=3,FALL=4,DEAD_DOOR=5,DEAD_ROAD=6,SEAT=7,HELD=8,DEAD_DOOR_OPEN=9;
    public static final int HUSH_TICKS=120,BIRTH1_TICKS=200,FALL_TICKS=30,DEAD1_TICKS=500,REEL_TICKS=160,BIRTH2_TICKS=160,HOLD_TICKS=200;
    public static final double DRIVE_SPEED=.7;
    public static final int HITS=4;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final Set<UUID> ARRIVING=new HashSet<>();
    private static final List<BlockPos> VAN_LIGHTS=new ArrayList<>();
    private DeerRoad(){}
    public static void clearAll(){ARRIVING.clear();VAN_LIGHTS.clear();}

    // ------------------------------------------------------------------ finding the road
    public static @Nullable BlockPos base(MinecraftServer s){var o=HouseSavedData.get(s).houseOrigin();return o==null?null:LabyrinthPlaces.base(o,PLACE);}
    public static @Nullable DeerRoadScene scene(MinecraftServer s){
        var b=base(s);var l=s.getLevel(NovelRooms.dimension(PLACE));if(b==null||l==null||!l.isLoaded(b.offset(DeerRoadSite.ANCHOR)))return null;
        return l.getBlockEntity(b.offset(DeerRoadSite.ANCHOR)) instanceof DeerRoadScene sc?sc:null;
    }
    static boolean onRoad(ServerPlayer p){return LiteraryVignettes.current(p)==PLACE;}
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    /** Dealt only to a reader who has come through the tunnel (the owner's "place this after a completed Tunnel Periphery encounter"). */
    public static boolean dealable(LabyrinthData d,UUID p){return WitnessAccount.has(d,p,WitnessAccount.Story.TUNNEL_PERIPHERY)&&!LiteraryVignettes.personal(d,p,PLACE).getBoolean("Caught");}
    static List<ServerPlayer> readers(DeerRoadScene s){var out=new ArrayList<ServerPlayer>();if(!(s.getLevel() instanceof ServerLevel l))return out;for(var p:l.players())if(onRoad(p))out.add(p);return out;}
    static void say(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(net.minecraft.ChatFormatting.GRAY),true);}
    static void sound(DeerRoadScene s,Vec3 at,SoundEvent e,float volume,float pitch){if(s.getLevel()!=null)s.getLevel().playSound(null,at.x,at.y,at.z,e,SoundSource.AMBIENT,volume,pitch);}
    static Vec3 at(DeerRoadScene s,double x,double y,double z){return Vec3.atLowerCornerOf(s.base()).add(x,y,z);}
    static @Nullable LiteraryActor woman(DeerRoadScene s,@Nullable UUID id){return id!=null&&s.getLevel() instanceof ServerLevel l&&l.getEntity(id) instanceof LiteraryActor a?a:null;}
    static @Nullable DeerRoadSeat seat(DeerRoadScene s,@Nullable UUID id){return id!=null&&s.getLevel() instanceof ServerLevel l&&l.getEntity(id) instanceof DeerRoadSeat seat?seat:null;}

    // ------------------------------------------------------------------ arriving
    /** A reader through the door: the wheel of a fresh car, the passenger's seat of a moving one, or the same headlight spill. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        ARRIVING.add(p.getUUID());own.putBoolean("DeerHere",true);
        if(!own.contains("DeerVisit"))own.putInt("DeerVisit",0);own.putInt("DeerVisit",own.getInt("DeerVisit")+1);
    }
    private static void seatArrivals(DeerRoadScene s,ServerLevel l){
        if(ARRIVING.isEmpty())return;
        for(var id:List.copyOf(ARRIVING)){
            var p=l.getServer().getPlayerList().getPlayer(id);if(p==null||p.level()!=l){ARRIVING.remove(id);continue;}
            if(!onRoad(p)||LabyrinthDoors.isBusy(p))continue;
            ARRIVING.remove(id);prune(s,l);
            if(s.car!=DeerRoadScene.FRESH&&s.party.isEmpty()&&readers(s).stream().allMatch(r->r==p))reset(s,l);
            s.party.add(id);s.partySeen.put(id,l.getGameTime());
            if(s.car==DeerRoadScene.FRESH&&seat(s,s.driverSeat)==null){if(board(s,l,p,0)){s.car=DeerRoadScene.DRIVE;s.speed=DRIVE_SPEED;s.carClock=0;own(p).putBoolean("DeerDriver",true);var o=own(p);o.putBoolean("DeerDriver",true);save(p,o);}}
            else if(s.car==DeerRoadScene.DRIVE&&seat(s,s.passengerSeat)==null)board(s,l,p,1);
            else{
                // Others arrive within the same headlight spill: on the shoulder where the car will stop, or beside it.
                var spot=s.stopped()?s.carCentre().add(2.6,0,1.5):at(s,DeerRoadSite.SHOULDER+.5,0,DeerRoadSite.DOE_Z+9.5);
                p.teleportTo(l,spot.x,spot.y,spot.z,150,5);p.setDeltaMovement(Vec3.ZERO);
                if(s.car==DeerRoadScene.FRESH)say(p,"A car is idling at the bend, its lights on and nobody at the wheel.");
            }
            s.sync();
        }
    }
    private static boolean board(DeerRoadScene s,ServerLevel l,ServerPlayer p,int slot){
        var seat=DeerRoadRegistry.SEAT.get().create(l);if(seat==null)return false;var c=s.carCentre();
        seat.bind(s.getBlockPos(),slot);seat.moveTo(c.x,c.y,c.z,180,0);if(!l.addFreshEntity(seat))return false;
        if(!p.startRiding(seat,true)){seat.discard();return false;}
        if(slot==0)s.driverSeat=seat.getUUID();else s.passengerSeat=seat.getUUID();
        p.setYRot(180);p.setYHeadRot(180);p.setXRot(4);p.connection.resetPosition();return true;
    }
    /** Nobody steps out of a moving car. */
    @SubscribeEvent public static void dismount(EntityMountEvent e){
        if(!e.isDismounting()||!(e.getEntityBeingMounted() instanceof DeerRoadSeat seat)||!(e.getEntityMounting() instanceof ServerPlayer))return;
        var s=seat.scene();if(s!=null&&(s.car==DeerRoadScene.DRIVE||s.car==DeerRoadScene.SKID))e.setCanceled(true);
    }

    // ------------------------------------------------------------------ the road's clock
    public static void tick(DeerRoadScene s){
        if(!(s.getLevel() instanceof ServerLevel l))return;long now=l.getGameTime();
        seatArrivals(s,l);
        if(now%20==0){prune(s,l);if(s.car!=DeerRoadScene.FRESH&&s.party.isEmpty()&&readers(s).isEmpty()){if(++s.emptyTicks>=5)reset(s,l);}else s.emptyTicks=0;}
        if(readers(s).isEmpty()&&s.car!=DeerRoadScene.DRIVE&&s.car!=DeerRoadScene.SKID)return;
        boolean moving=false;
        switch(s.car){
            case DeerRoadScene.FRESH->{if(now%40==0)sound(s,s.carCentre(),PuzzleRegistry.YARD_IDLE.get(),.6F,1.1F);}
            case DeerRoadScene.DRIVE->{drive(s,l);moving=true;}
            case DeerRoadScene.SKID->{skid(s,l);moving=true;}
            default->story(s,l,now);
        }
        doe(s,l,now);
        if(van(s,l,now))moving=true;
        if(s.hazardFrom>=0&&(now-s.hazardFrom)%10==0)sound(s,s.carCentre().add(0,.8,0),SoundEvents.COMPARATOR_CLICK,.35F,(now-s.hazardFrom)%20==0?1.1F:.85F);
        if(moving)s.sync();else s.setChanged();
    }
    private static void prune(DeerRoadScene s,ServerLevel l){
        long now=l.getGameTime();
        for(var id:List.copyOf(s.party)){
            var p=l.getServer().getPlayerList().getPlayer(id);
            if(p!=null&&onRoad(p)){s.partySeen.put(id,now);continue;}
            long seen=s.partySeen.getOrDefault(id,now);
            if(p!=null&&now-seen>200||p==null&&now-seen>24000){s.party.remove(id);s.partySeen.remove(id);}
        }
    }

    // ------------------------------------------------------------------ the drive, the doe, the strike
    private static void drive(DeerRoadScene s,ServerLevel l){
        var driverSeat=seat(s,s.driverSeat);var driver=driverSeat==null||driverSeat.getPassengers().isEmpty()?null:driverSeat.getPassengers().getFirst() instanceof ServerPlayer p?p:null;
        s.carClock++;
        if(s.carClock%40==1)sound(s,s.carCentre(),PuzzleRegistry.YARD_IDLE.get(),.9F,1.35F);
        double ahead=s.carZ-2.5-DeerRoadSite.DOE_Z;
        if(s.doe==DeerRoadScene.DOE_HIDDEN&&ahead<=14){s.doe=DeerRoadScene.DOE_STEP;s.doeX=DeerRoadSite.DITCH+2.5;s.doeZ=DeerRoadSite.DOE_Z+.5;s.doeY=0;s.doeYaw=90;s.doeAt=l.getGameTime();}
        if(s.doe==DeerRoadScene.DOE_STEP&&s.response==DeerRoadScene.NONE&&driver!=null){
            if(driver.zza<-.1F)s.response=DeerRoadScene.BRAKE;else if(driver.xxa>.1F)s.response=DeerRoadScene.LEFT;else if(driver.xxa<-.1F)s.response=DeerRoadScene.RIGHT;
            if(s.response!=DeerRoadScene.NONE){var o=own(driver);o.putInt("DeerBeat",s.response);save(driver,o);}
        }
        if(s.response==DeerRoadScene.BRAKE){s.speed=Math.max(.25,s.speed-.05);s.carYaw=(float)Math.sin(s.carClock*.9)*.6F;}
        if(s.response==DeerRoadScene.LEFT||s.response==DeerRoadScene.RIGHT){double side=s.response==DeerRoadScene.LEFT?-1:1;s.carX+=side*.06;s.carYaw=(float)(side*-Math.min(9,(s.carYaw==0?1:Math.abs(s.carYaw))+1.2));}
        s.carZ-=s.speed;moveSeats(s);
        if(s.carZ-2.5<=DeerRoadSite.DOE_Z+.6)strike(s,l);
    }
    private static void strike(DeerRoadScene s,ServerLevel l){
        s.car=DeerRoadScene.SKID;s.carClock=0;
        s.shift=switch(s.response){case DeerRoadScene.LEFT->-2;case DeerRoadScene.RIGHT->2;default->0;};
        s.finalZ=switch(s.response){case DeerRoadScene.BRAKE->-50;case DeerRoadScene.NONE->-53;default->-51;};
        double[] rest=switch(s.response){case DeerRoadScene.BRAKE->new double[]{-12.6,-56.3};case DeerRoadScene.NONE->new double[]{-13.8,-61.2};case DeerRoadScene.LEFT->new double[]{-7.4,-57.6};default->new double[]{-14.2,-57.4};};
        s.doeRestX=rest[0];s.doeRestZ=rest[1];s.doe=DeerRoadScene.DOE_THROWN;s.doeAt=l.getGameTime();s.pDoeX=s.doeX;s.pDoeZ=s.doeZ;
        var c=s.carCentre();sound(s,c,BackseatRegistry.IMPACT.get(),s.response==DeerRoadScene.BRAKE?1.1F:1.8F,.8F);sound(s,c,SoundEvents.SHIELD_BLOCK,1F,.5F);
        for(var id:new UUID[]{s.driverSeat,s.passengerSeat}){var seat=seat(s,id);if(seat!=null)for(var r:seat.getPassengers())if(r instanceof ServerPlayer p)HousePackets.send(p,new HouseFadePayload(1,2,7));}
        for(var p:readers(s))if(seen(p,at(s,s.doeX,.6,s.doeZ),.5)){var o=own(p);o.putBoolean("DeerSawDoe",true);save(p,o);}
    }
    private static void skid(DeerRoadScene s,ServerLevel l){
        s.carClock++;double tx=DeerRoadSite.LANE+s.shift+.5,tz=s.finalZ+.5;
        double q=Math.min(1,s.carClock/14.0);s.carX+=(tx-s.carX)*Math.min(1,.18+q*.3);s.carZ+=(tz-s.carZ)*Math.min(1,.2+q*.3);
        s.carYaw*=.78F;if(s.carClock==1)sound(s,s.carCentre(),SoundEvents.GRINDSTONE_USE,.9F,1.7F);
        moveSeats(s);
        if(s.carClock>=16){s.carX=tx;s.carZ=tz;s.carYaw=0;stop(s,l);}
    }
    private static void moveSeats(DeerRoadScene s){
        var c=s.carCentre();
        for(var id:new UUID[]{s.driverSeat,s.passengerSeat}){var seat=seat(s,id);if(seat==null)continue;seat.setPos(c.x,c.y,c.z);seat.setYRot(180+s.carYaw);
            for(var r:seat.getPassengers()){seat.positionRider(r);if(r instanceof ServerPlayer p)p.connection.resetPosition();}}
    }
    private static void stop(DeerRoadScene s,ServerLevel l){
        s.car=DeerRoadScene.STOPPED;s.story=DeerRoadScene.HUSH;s.clock=0;s.storyAt=l.getGameTime();s.hazardFrom=l.getGameTime();
        // The impact springs a door: the driver's on a straight strike, the passenger's when the car was turned.
        int sprung=s.response==DeerRoadScene.LEFT||s.response==DeerRoadScene.RIGHT?DeerCarMesh.PASSENGER:DeerCarMesh.DRIVER;
        s.doors|=1<<sprung;s.doorAt[sprung]=l.getGameTime();
        placeCells(s,l);
        sound(s,s.carCentre(),SoundEvents.BEACON_DEACTIVATE,.4F,.6F);
        // The low beams stay on down the road ahead.
        var root=s.base().offset(s.finalRoot());
        for(int d=3;d<=14;d++)for(int w=-(1+d/4);w<=1+d/4;w++){var at=root.offset(w,1,-d);if(l.getBlockState(at).isAir()&&(d+w)%2==0)l.setBlock(at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,Math.max(5,14-d/2-Math.abs(w))),F);}
        s.vehicleNext=l.getGameTime()+1100;
        for(var p:readers(s)){var o=own(p);o.putBoolean("DeerStruck",true);save(p,o);}
    }
    static void placeCells(DeerRoadScene s,ServerLevel l){
        var root=s.base().offset(s.finalRoot());
        for(int i=0;i<DeerCarCells.COUNT;i++){var at=root.offset(DeerCarCells.offset(i));var st=l.getBlockState(at);if(st.isAir()||st.canBeReplaced()||st.is(DeerRoadRegistry.CAR.get())||st.is(Blocks.LIGHT))l.setBlock(at,DeerRoadRegistry.CAR.get().defaultBlockState().setValue(DeerCarBlock.CELL,i).setValue(DeerCarBlock.MASK,s.doors&7),F);}
    }
    static void removeCells(DeerRoadScene s,ServerLevel l){
        var root=s.base().offset(s.finalRoot());
        for(int i=0;i<DeerCarCells.COUNT;i++){var at=root.offset(DeerCarCells.offset(i));if(l.getBlockState(at).is(DeerRoadRegistry.CAR.get()))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);}
    }
    private static void doe(DeerRoadScene s,ServerLevel l,long now){
        if(s.doe==DeerRoadScene.DOE_STEP){s.doeX-=.13;s.doeYaw=90;}
        else if(s.doe==DeerRoadScene.DOE_THROWN){
            double t=Math.min(1,(now-s.doeAt)/14.0);s.doeX+=(s.doeRestX-s.doeX)*.25;s.doeZ+=(s.doeRestZ-s.doeZ)*.25;s.doeY=Math.sin(Math.PI*t)*1.4;s.doeYaw+=31;
            if(t>=1){s.doe=DeerRoadScene.DOE_LYING;s.doeX=s.doeRestX;s.doeZ=s.doeRestZ;s.doeY=0;s.doeYaw=(float)Math.toDegrees(Math.atan2(-(s.carX-s.doeX),s.carZ-s.doeZ))+90;
                sound(s,at(s,s.doeX,.3,s.doeZ),SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,.5F,.5F);
                var at=s.base().offset(BlockPos.containing(s.doeX,0,s.doeZ));for(var c:new BlockPos[]{at,at.relative(Direction.fromYRot(s.doeYaw+90))})if(l.getBlockState(c).isAir())l.setBlock(c,DeerRoadBlock.of(DeerRoadBlock.Kind.CARCASS,Direction.NORTH),F);}
        }
    }

    // ------------------------------------------------------------------ the story, once the car has stopped
    private static void story(DeerRoadScene s,ServerLevel l,long now){
        s.clock++;
        switch(s.story){
            case DeerRoadScene.HUSH->{
                if(s.clock==30)sound(s,at(s,DeerRoadSite.DITCH+.5,-1.5,s.finalZ),SoundEvents.WATER_AMBIENT,.8F,.8F);
                if(s.clock>60&&s.clock%17==0)sound(s,at(s,s.doeX,.2,s.doeZ),SoundEvents.SLIME_BLOCK_STEP,.4F,.6F);
                if(s.clock>=HUSH_TICKS)next(s,DeerRoadScene.BIRTH1,now);
            }
            case DeerRoadScene.BIRTH1->{
                var w=woman(s,s.first);
                if(w==null&&s.clock<5){w=spawn(s,l,at(s,s.doeX,0,s.doeZ).add(towardCar(s).scale(.7)),EMERGE);if(w!=null){s.first=w.getUUID();face(w,s.carCentre());}}
                if(s.clock%23==0&&s.clock<120)sound(s,at(s,s.doeX,.2,s.doeZ),SoundEvents.SLIME_BLOCK_STEP,.5F,.5F);
                if(s.clock==150&&w!=null)sound(s,w.position(),SoundEvents.WARDEN_STEP,.8F,.7F);
                if(s.clock>=BIRTH1_TICKS){if(w!=null)w.appearance(LiteraryActor.ROAD_WOMAN,STRIDE);next(s,DeerRoadScene.LIFE1,now);}
            }
            case DeerRoadScene.LIFE1->{
                var w=woman(s,s.first);if(w==null){next(s,DeerRoadScene.DEAD1,now);return;}
                if(w.phase()==FALL){if(s.clock>=w.getPersistentData().getInt("DeerFellAt")+FALL_TICKS)die(s,l,w,true,now);return;}
                if(staggered(w,now))return;
                boolean open=s.open(DeerCarMesh.DRIVER);var target=s.carCentre().add(open?DeerCarMesh.AGAINST_OPEN:DeerCarMesh.AGAINST_SHUT);
                stride(s,w,target,now);
                catchNear(s,l,w,1);
                if(w.position().subtract(target).horizontalDistance()<.6||s.clock>600){w.moveTo(target.x,target.y,target.z,open?0:90,0);w.getPersistentData().putBoolean("DeerAtDoor",true);fall(s,w);}
            }
            case DeerRoadScene.DEAD1->{if(s.clock>=DEAD1_TICKS)next(s,DeerRoadScene.REEL,now);}
            case DeerRoadScene.REEL->{
                var bench=s.carCentre().add(DeerCarMesh.BENCH);
                if(s.clock%20==10&&s.beltTeeth<7){s.beltTeeth++;sound(s,bench,SoundEvents.LEVER_CLICK,.5F,1.7F);s.sync();}
                if(s.clock==60){s.bench=1;s.sync();sound(s,bench,SoundEvents.WOOL_PLACE,.6F,.5F);}
                if(s.clock==140){s.bench=2;s.sync();sound(s,bench,SoundEvents.HONEY_BLOCK_BREAK,.8F,.6F);}
                if(s.clock>=REEL_TICKS)next(s,DeerRoadScene.BIRTH2,now);
            }
            case DeerRoadScene.BIRTH2->{
                var w=woman(s,s.second);var bench=s.carCentre().add(DeerCarMesh.BENCH);
                if(w==null&&s.clock<5){w=spawn(s,l,s.carCentre().add(DeerCarMesh.BENCH.x,.3,DeerCarMesh.BENCH.z),SEAT);if(w!=null){s.second=w.getUUID();w.setYRot(90);w.setYBodyRot(90);w.setYHeadRot(90);}}
                if(s.clock==90&&!s.open(DeerCarMesh.REAR))door(s,l,DeerCarMesh.REAR,true,null);
                if(s.clock%30==15)sound(s,bench,SoundEvents.WOOL_BREAK,.5F,.6F);
                if(s.clock>=BIRTH2_TICKS){if(w!=null){var out=s.carCentre().add(DeerCarMesh.REAR_DOORWAY);w.moveTo(out.x,out.y,out.z,90,0);w.appearance(LiteraryActor.ROAD_WOMAN,STAND);sound(s,out,SoundEvents.WARDEN_STEP,1F,.7F);}next(s,DeerRoadScene.LIFE2,now);}
            }
            case DeerRoadScene.LIFE2->{
                var w=woman(s,s.second);if(w==null){next(s,DeerRoadScene.DEAD2,now);return;}
                if(w.phase()==FALL){if(s.clock>=w.getPersistentData().getInt("DeerFellAt")+FALL_TICKS)die(s,l,w,false,now);return;}
                if(held(s,w)){if(w.phase()!=HELD){w.appearance(LiteraryActor.ROAD_WOMAN,HELD);}face(w,at(s,s.vanX,1,s.vanZ));return;}
                if(staggered(w,now))return;
                var prey=prey(s,w);
                if(prey==null){if(w.phase()!=STAND)w.appearance(LiteraryActor.ROAD_WOMAN,STAND);w.gesture(0);return;}
                if(w.phase()!=STRIDE)w.appearance(LiteraryActor.ROAD_WOMAN,STRIDE);
                stride(s,w,prey.position(),now);
                catchNear(s,l,w,2);
            }
            case DeerRoadScene.DEAD2->culvert(s,l,now);
            default->{}
        }
    }
    private static void next(DeerRoadScene s,int story,long now){s.story=story;s.clock=0;s.storyAt=now;s.sync();}
    private static Vec3 towardCar(DeerRoadScene s){var d=new Vec3(s.carX-s.doeX,0,s.carZ-s.doeZ);return d.lengthSqr()<1e-4?new Vec3(0,0,1):d.normalize();}
    private static void face(LiteraryActor a,Vec3 to){float yaw=(float)Math.toDegrees(Math.atan2(-(to.x-a.getX()),to.z-a.getZ()));a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    private static @Nullable LiteraryActor spawn(DeerRoadScene s,ServerLevel l,Vec3 at,int phase){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;a.bind(PLACE,null);a.appearance(LiteraryActor.ROAD_WOMAN,phase);a.moveTo(at.x,at.y,at.z,0,0);a.setNoGravity(true);a.setDeltaMovement(Vec3.ZERO);
        return l.addFreshEntity(a)?a:null;
    }
    /** The tunnel's stride: long, uneven and heavy from far off; shorter, slower than a walk, close in. */
    private static void stride(DeerRoadScene s,LiteraryActor w,Vec3 goal,long now){
        double d=w.position().subtract(goal).horizontalDistance();boolean far=d>9;
        w.gesture(far?1:0);double step=far?.34+.12*(.5+.5*Math.sin(w.tickCount*.13)):.13+.05*(.5+.5*Math.sin(w.tickCount*.17));
        var before=w.position();w.setNoGravity(false);SceneFootpaths.walk(w,goal,step);
        double moved=w.position().subtract(before).horizontalDistance();var tag=w.getPersistentData();
        double travelled=tag.getDouble("DeerStride")+moved;
        if(travelled>=(far?4.2:2.4)){travelled=0;sound(s,w.position(),SoundEvents.WARDEN_STEP,far?1.1F:.6F,.7F+w.getRandom().nextFloat()*.1F);}
        tag.putDouble("DeerStride",travelled);
    }
    private static boolean staggered(LiteraryActor w,long now){return w.getPersistentData().getLong("DeerStagger")>now;}
    private static void fall(DeerRoadScene s,LiteraryActor w){w.appearance(LiteraryActor.ROAD_WOMAN,FALL);w.getPersistentData().putInt("DeerFellAt",s.clock);w.gesture(w.getPersistentData().getBoolean("DeerAtDoor")?2:3);sound(s,w.position(),SoundEvents.WARDEN_STEP,.5F,.5F);s.sync();}
    private static void die(DeerRoadScene s,ServerLevel l,LiteraryActor w,boolean first,long now){
        boolean door=w.getPersistentData().getBoolean("DeerAtDoor");
        w.appearance(LiteraryActor.ROAD_WOMAN,door?(s.open(DeerCarMesh.DRIVER)?DEAD_DOOR_OPEN:DEAD_DOOR):DEAD_ROAD);w.setNoGravity(true);w.setDeltaMovement(Vec3.ZERO);
        if(door)s.jam=DeerCarMesh.DRIVER;
        if(first)next(s,DeerRoadScene.DEAD1,now);else next(s,DeerRoadScene.DEAD2,now);
        for(var p:readers(s)){var o=own(p);if(seen(p,w.position().add(0,.8,0),.42)){o.putBoolean(first?"DeerSawDeath1":"DeerSawDeath2",true);}if(first)o.putBoolean("DeerFirstAtDoor",door);save(p,o);}
    }
    private static void catchNear(DeerRoadScene s,ServerLevel l,LiteraryActor w,int life){
        for(var p:readers(s)){
            if(!LiteraryVignettes.participant(p)||LabyrinthDoors.isBusy(p))continue;
            var d=p.position().subtract(w.position());if(d.horizontalDistance()>1.15||Math.abs(d.y)>2.2)continue;
            caught(s,p,life);
        }
    }
    static void caught(DeerRoadScene s,ServerPlayer p,int life){
        var o=own(p);if(o.getBoolean("Caught")||o.getBoolean("Completed"))return;
        o.putBoolean("Caught",true);o.putInt("DeerCaughtLife",life);save(p,o);s.party.remove(p.getUUID());
        p.stopRiding();say(p,"Her hand closes on the back of your neck.");
        HousePackets.send(p,new HouseFadePayload(2,30,20));
        LabyrinthDoors.sendBack(p,2,30,20);
    }
    /** The nearest reader she can see. Down in the ditch and crouched, a reader is hidden from her beyond a few steps. */
    private static @Nullable ServerPlayer prey(DeerRoadScene s,LiteraryActor w){
        ServerPlayer best=null;double bestD=1e9;var eye=w.getEyePosition();
        for(var p:readers(s)){
            if(!LiteraryVignettes.participant(p))continue;double d=p.distanceToSqr(w);var r=p.position().subtract(Vec3.atLowerCornerOf(s.base()));
            if(DeerRoadSite.inDitch(r)&&p.isCrouching()&&d>16)continue;
            var hit=w.level().clip(new ClipContext(eye,p.getEyePosition(),ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,w));
            if(hit.getType()!=HitResult.Type.MISS&&d>4&&!p.isPassenger())continue;
            if(d<bestD){bestD=d;best=p;}
        }
        return best;
    }
    /** In the next vehicle's lights she stands and looks into them. */
    private static boolean held(DeerRoadScene s,LiteraryActor w){
        if(s.van==DeerRoadScene.VAN_STOPPED)return true;
        return s.vanSlow&&(s.van==DeerRoadScene.VAN_COMING||s.van==DeerRoadScene.VAN_GOING)&&Math.abs(s.vanZ-(w.getZ()-s.base().getZ()))<14;
    }
    private static void culvert(DeerRoadScene s,ServerLevel l,long now){
        var grate=s.base().offset(DeerRoadSite.GRATE);var g=at(s,DeerRoadSite.GRATE.getX()+.5,DeerRoadSite.GRATE.getY()+.5,DeerRoadSite.GRATE.getZ()+.5);
        if(!s.culvertPlayed){
            for(var p:readers(s))if(p.position().distanceTo(g)<4.5){
                s.culvertPlayed=true;s.clock=0;sound(s,g,SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,.35F,.6F);s.sync();break;}
            return;
        }
        if(s.clock==12)sound(s,g,SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,.3F,.55F);
        if(s.clock==20&&DeerRoadBlock.is(l.getBlockState(grate),DeerRoadBlock.Kind.GRATE)){l.setBlock(grate,l.getBlockState(grate).setValue(DeerRoadBlock.STAGE,1),F);
            for(var p:readers(s)){var o=own(p);if(p.position().distanceTo(g)<16)o.putBoolean("DeerHeardScrape",true);if(seen(p,g,.6))o.putBoolean("DeerSawThird",true);save(p,o);}}
        if(s.clock==100&&DeerRoadBlock.is(l.getBlockState(grate),DeerRoadBlock.Kind.GRATE))l.setBlock(grate,l.getBlockState(grate).setValue(DeerRoadBlock.STAGE,0),F);
    }

    // ------------------------------------------------------------------ the next vehicle
    /** Comes up out of the bend. A triangle slows it; a reader on the hatched shoulder flags it, and it stands alongside, lights on. */
    private static boolean van(DeerRoadScene s,ServerLevel l,long now){
        if(s.van==DeerRoadScene.VAN_NONE){
            if(s.vehicleNext<0||now<s.vehicleNext||s.car!=DeerRoadScene.STOPPED)return false;
            s.van=DeerRoadScene.VAN_COMING;s.vanX=DeerRoadSite.LANE+.5;s.vanZ=DeerRoadSite.START_Z+2;s.vanSlow=s.triangleSet;s.vanClock=0;
            sound(s,at(s,s.vanX,1,s.vanZ),PuzzleRegistry.YARD_TRAFFIC.get(),s.vanSlow?.9F:1.6F,s.vanSlow?.8F:1.05F);
            for(var p:readers(s)){var o=own(p);o.putBoolean("DeerSawVehicle",true);save(p,o);}
        }
        s.vanClock++;double carRear=s.carZ+2.5;
        switch(s.van){
            case DeerRoadScene.VAN_COMING->{
                double speed=s.vanSlow?(s.vanZ>carRear+10?.5:.22):.9;
                s.vanZ-=speed;
                // It pulls out round the wreck into the other lane.
                if(s.vanZ<carRear+12)s.vanX+=((DeerRoadSite.CENTRE-2.5)-s.vanX)*.15;
                if(s.vanSlow&&s.vanZ<=s.carZ+.5&&flagged(s)){s.van=DeerRoadScene.VAN_STOPPED;s.vanClock=0;
                    for(var p:readers(s)){var o=own(p);var r=p.position().subtract(Vec3.atLowerCornerOf(s.base()));if(DeerRoadSite.onHatch(r))o.putBoolean("DeerFlagged",true);o.putBoolean("DeerVehicleStopped",true);save(p,o);
                        say(p,"The car stops alongside, engine running. Its lights are on the road, on her, and on the body by the door.");}}
                else if(s.vanZ<=s.carZ-1)s.van=DeerRoadScene.VAN_GOING;
            }
            case DeerRoadScene.VAN_STOPPED->{if(s.vanClock%40==0)sound(s,at(s,s.vanX,1,s.vanZ),PuzzleRegistry.YARD_IDLE.get(),.8F,.95F);if(s.vanClock>=HOLD_TICKS)s.van=DeerRoadScene.VAN_GOING;}
            case DeerRoadScene.VAN_GOING->{s.vanZ-=s.vanSlow?.4:.9;if(s.vanZ<DeerRoadSite.FAR+4){s.van=DeerRoadScene.VAN_NONE;s.vehicleNext=now+900;}}
            default->{}
        }
        vanLights(s,l);return true;
    }
    private static boolean flagged(DeerRoadScene s){for(var p:readers(s)){var r=p.position().subtract(Vec3.atLowerCornerOf(s.base()));if(DeerRoadSite.onHatch(r))return true;}return false;}
    private static void vanLights(DeerRoadScene s,ServerLevel l){
        for(var at:VAN_LIGHTS)if(l.getBlockState(at).is(Blocks.LIGHT))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);VAN_LIGHTS.clear();
        if(s.van==DeerRoadScene.VAN_NONE)return;
        var front=s.base().offset(BlockPos.containing(s.vanX,1,s.vanZ-3));
        for(int d=0;d<=8;d+=4)for(int w=-1;w<=1;w+=2){var at=front.offset(w,0,-d);if(l.getBlockState(at).isAir()){l.setBlock(at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,14-d),F);VAN_LIGHTS.add(at.immutable());}}
    }

    // ------------------------------------------------------------------ hands on the car, the triangle, blows
    /** A hand on the stopped car: a door, the boot, or the triangle out of it. */
    public static boolean carUse(ServerPlayer p,Vec3 hit){
        var s=scene(p.server);if(s==null||!s.stopped()||!onRoad(p)||p.distanceToSqr(hit)>36)return false;
        var rel=hit.subtract(s.carCentre());double x=rel.x*16,z=rel.z*16;var l=(ServerLevel)s.getLevel();
        if(z>29){
            if(!s.boot){s.boot=true;s.bootAt=l.getGameTime();sound(s,hit,SoundEvents.IRON_TRAPDOOR_OPEN,.7F,.8F);
                if(!s.triangleTaken)say(p,"In the boot: the warning triangle in its case, a jack, a blanket.");s.sync();return true;}
            if(!s.triangleTaken){s.triangleTaken=true;LiteraryVignettes.give(p,new ItemStack(DeerRoadRegistry.TRIANGLE.get()));say(p,"The triangle folds out. Its feet want the road behind the car.");var o=own(p);o.putBoolean("DeerTookTriangle",true);save(p,o);s.sync();return true;}
            s.boot=false;s.bootAt=l.getGameTime();sound(s,hit,SoundEvents.IRON_TRAPDOOR_CLOSE,.7F,.8F);s.sync();return true;
        }
        int door=x<0?(z<10?DeerCarMesh.DRIVER:DeerCarMesh.REAR):(z<10?DeerCarMesh.PASSENGER:-1);
        if(door<0){say(p,"That door is buckled into its frame. It won't open.");return true;}
        if(door==s.jam){say(p,s.open(door)?"Her arm is across the sill. The door won't swing past her.":"Her weight is against the door. It won't open.");return true;}
        door(s,l,door,!s.open(door),p);return true;
    }
    static void door(DeerRoadScene s,ServerLevel l,int door,boolean open,@Nullable ServerPlayer by){
        if(s.open(door)==open)return;
        if(open)s.doors|=1<<door;else s.doors&=~(1<<door);s.doorAt[door]=l.getGameTime();
        var root=s.base().offset(s.finalRoot());
        for(int i=0;i<DeerCarCells.COUNT;i++){var at=root.offset(DeerCarCells.offset(i));var st=l.getBlockState(at);if(st.is(DeerRoadRegistry.CAR.get()))l.setBlock(at,st.setValue(DeerCarBlock.MASK,s.doors&7),F);}
        sound(s,s.carCentre(),open?SoundEvents.IRON_DOOR_OPEN:BackseatRegistry.LATCH.get(),.7F,open?1.2F:1F);
        if(by!=null&&!open&&s.story==DeerRoadScene.LIFE2){var w=woman(s,s.second);if(w!=null&&w.distanceToSqr(by)<25){var o=own(by);o.putBoolean("DeerHeldOff",true);save(by,o);}}
        s.sync();
    }
    public static boolean setTriangle(ServerPlayer p,BlockPos clicked,ItemStack stack){
        var s=scene(p.server);if(s==null||!onRoad(p))return false;var l=(ServerLevel)s.getLevel();
        var at=clicked.above();var r=Vec3.atBottomCenterOf(at).subtract(Vec3.atLowerCornerOf(s.base()));
        boolean road=r.x>DeerRoadSite.ROAD_W-1&&r.x<DeerRoadSite.SHOULDER+2&&r.z>s.carZ+3&&r.z<s.carZ+16&&Math.abs(r.y)<.6;
        if(!road||!l.getBlockState(at).isAir()){say(p,"It wants the road behind the car, where traffic will come up on it.");return true;}
        l.setBlock(at,DeerRoadBlock.of(DeerRoadBlock.Kind.TRIANGLE,Direction.SOUTH),F);stack.shrink(1);s.triangleSet=true;
        var o=own(p);o.putBoolean("DeerSetTriangle",true);save(p,o);say(p,"The triangle stands on the road behind the car, its face to the bend.");s.sync();return true;
    }
    /** A blow, from a reader: it counts while she is moving; enough of them and she goes down where she is. */
    public static void struck(ServerPlayer p,LiteraryActor w,float amount){
        var s=scene(p.server);if(s==null||!onRoad(p)||amount<1)return;long now=p.serverLevel().getGameTime();
        boolean first=w.getUUID().equals(s.first),second=w.getUUID().equals(s.second);
        boolean live=first&&(s.story==DeerRoadScene.LIFE1||s.story==DeerRoadScene.BIRTH1&&s.clock>170)||second&&s.story==DeerRoadScene.LIFE2;
        if(!live||w.phase()==FALL)return;var tag=w.getPersistentData();if(now-tag.getLong("DeerHitAt")<8)return;
        tag.putLong("DeerHitAt",now);tag.putLong("DeerStagger",now+12);int hits=tag.getInt("DeerHits")+1;tag.putInt("DeerHits",hits);
        var o=own(p);o.putBoolean(first?"DeerStruck1":"DeerStruck2",true);o.putBoolean("DeerHeldOff",true);
        sound(s,w.position().add(0,1.5,0),SoundEvents.PLAYER_ATTACK_STRONG,.8F,.6F);
        if(hits>=HITS){o.putBoolean(first?"DeerKilled1":"DeerKilled2",true);if(first&&s.story==DeerRoadScene.BIRTH1)next(s,DeerRoadScene.LIFE1,now);fall(s,w);}
        save(p,o);
    }

    // ------------------------------------------------------------------ what each reader sees
    static boolean seen(ServerPlayer p,Vec3 point,double cos){
        if(p.isSpectator())return false;var eye=p.getEyePosition();var to=point.subtract(eye);double d=to.length();if(d>40)return false;if(d<1.2)return true;
        if(p.getLookAngle().dot(to.scale(1/d))<cos)return false;
        var hit=p.serverLevel().clip(new ClipContext(eye,point,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));
        return hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceTo(point)<.9;
    }
    /** Every five ticks, for each reader on the road: what they could see from where they stood. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var s=scene(p.server);if(s==null||own.getBoolean("Completed")||own.getBoolean("Caught"))return;
        if(!s.party.contains(p.getUUID())&&!ARRIVING.contains(p.getUUID())){s.party.add(p.getUUID());s.partySeen.put(p.getUUID(),p.serverLevel().getGameTime());}
        var first=woman(s,s.first);var second=woman(s,s.second);var r=p.position().subtract(Vec3.atLowerCornerOf(b));
        if(s.doe>=DeerRoadScene.DOE_STEP&&seen(p,at(s,s.doeX,.6,s.doeZ),.45))own.putBoolean("DeerSawDoe",true);
        if(s.story==DeerRoadScene.BIRTH1&&s.clock>=30&&s.clock<140&&seen(p,at(s,s.doeX,.3,s.doeZ),.5))own.putBoolean("DeerSawHand",true);
        if(first!=null&&(s.story==DeerRoadScene.BIRTH1&&s.clock>=140||s.story==DeerRoadScene.LIFE1)&&seen(p,first.position().add(0,2,0),.45))own.putBoolean("DeerSawRise",true);
        if(own.getBoolean("DeerSawHand")||own.getBoolean("DeerSawRise"))own.putBoolean("DeerWitnessed",true);
        if(s.story==DeerRoadScene.REEL&&p.position().distanceTo(s.carCentre())<20)own.putBoolean("DeerHeardReel",true);
        if(s.story==DeerRoadScene.BIRTH2){
            if(second!=null&&seen(p,s.carCentre().add(DeerCarMesh.BENCH).add(0,.4,0),.6))own.putBoolean("DeerSawBirth2",true);
            else if(p.position().distanceTo(s.carCentre())<24)own.putBoolean("DeerHeardBirth2",true);
            if(DeerRoadSite.inDitch(r))own.putBoolean("DeerFromDitch",true);
            if(first!=null&&seen(p,first.position().add(0,.6,0),.55)&&!own.getBoolean("DeerSawBirth2"))own.putBoolean("DeerWatchedFirst",true);
        }
        if(second!=null&&first!=null&&s.story>=DeerRoadScene.BIRTH2&&seen(p,second.position().add(0,s.story==DeerRoadScene.BIRTH2?.6:1.8,0),.6)&&seen(p,first.position().add(0,.6,0),.6))own.putBoolean("DeerBothAtOnce",true);
        if(second!=null&&s.story==DeerRoadScene.LIFE2&&seen(p,second.position().add(0,2,0),.45))own.putBoolean("DeerSawSecond",true);
        if(s.story>=DeerRoadScene.LIFE2&&own.getBoolean("DeerWitnessed")&&!own.getBoolean("DeerSecondLine")&&p.position().distanceTo(s.carCentre())<30){own.putBoolean("DeerSecondLine",true);}
        if(DeerRoadSite.inDitch(r))own.putBoolean("DeerInDitch",true);
        if(r.distanceTo(Vec3.atCenterOf(DeerRoadSite.NOTICE))<3&&!own.getBoolean("DeerNoticeHint")&&!own.getBoolean("Read_Source")&&s.stopped()){own.putBoolean("DeerNoticeHint",true);say(p,"A county notice on the post, under the crossing sign. It is wet through but readable.");}
    }

    // ------------------------------------------------------------------ off the road
    /** Through the service door at the bend (the entry). */
    public static void returned(ServerPlayer p,LabyrinthData.Door door){if(SceneInstances.plain(door.id).equals(PLACE.entryDoorId()))leave(p,"service");}
    /** Through the drainage gate at the head of the ditch (the way on). */
    public static void onward(ServerPlayer p,LabyrinthData.Door door){if(door.id.equals(OnwardDoors.id(PLACE)))leave(p,"drain");}
    public static void leave(ServerPlayer p,String how){
        var d=LabyrinthData.get(p.server);var o=LiteraryVignettes.personal(d,p.getUUID(),PLACE);var s=scene(p.server);if(s!=null){s.party.remove(p.getUUID());s.setChanged();}
        if(o.getBoolean("Completed")||o.getBoolean("Caught"))return;
        o.putString("DeerExit",how);
        if(!o.getBoolean("DeerWitnessed")){LiteraryVignettes.save(d,p.getUUID(),PLACE,o);say(p,"You left the road before anything got up out of the doe. There was nothing yet to leave behind you.");return;}
        String outcome=how.equals("drain")?"took_the_ditch_to_the_drainage_gate":o.getBoolean("DeerFlagged")?"flagged_the_next_vehicle_and_used_the_service_door":"walked_back_along_the_shoulder_to_the_service_door";
        o.putBoolean("Ready",true);o.putString("Outcome",outcome);o.putBoolean("Completed",true);o.putInt("Beat",o.getInt("Visit"));
        LiteraryVignettes.save(d,p.getUUID(),PLACE,o);
        WitnessAccount.resolve(p,WitnessAccount.Story.DEER_ROAD,outcome);d.setCompleted(PLACE.id(),true);
        // The account comes out with them, folded in a pocket.
        LiteraryVignettes.reward(p,PLACE,o,"Ending",DeerRoadTexts.account(p.getGameProfile().getName(),o));
    }

    // ------------------------------------------------------------------ the road set back
    /** Nobody left on the road and nobody with a visit open on it: the next reader finds it as it was before the strike. */
    public static void reset(DeerRoadScene s,ServerLevel l){
        for(var id:new UUID[]{s.first,s.second}){var w=woman(s,id);if(w!=null)w.discard();}
        for(var id:new UUID[]{s.driverSeat,s.passengerSeat}){var seat=seat(s,id);if(seat!=null){seat.ejectPassengers();seat.discard();}}
        if(s.car==DeerRoadScene.STOPPED)removeCells(s,l);
        var b=s.base();
        for(int x=DeerRoadSite.ROAD_W-4;x<=0;x++)for(int z=DeerRoadSite.FAR;z<=0;z++)for(int y=0;y<=1;y++){
            var at=b.offset(x,y,z);var st=l.getBlockState(at);
            if(st.is(Blocks.LIGHT)||DeerRoadBlock.is(st,DeerRoadBlock.Kind.TRIANGLE)||DeerRoadBlock.is(st,DeerRoadBlock.Kind.CARCASS)||st.is(DeerRoadRegistry.CAR.get()))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
        }
        var grate=b.offset(DeerRoadSite.GRATE);if(DeerRoadBlock.is(l.getBlockState(grate),DeerRoadBlock.Kind.GRATE))l.setBlock(grate,l.getBlockState(grate).setValue(DeerRoadBlock.STAGE,0),F);
        VAN_LIGHTS.clear();
        s.car=DeerRoadScene.FRESH;s.story=DeerRoadScene.HUSH;s.clock=0;s.carClock=0;s.response=DeerRoadScene.NONE;s.shift=0;s.finalZ=0;
        s.carX=DeerRoadSite.LANE+.5;s.carZ=DeerRoadSite.START_Z+.5;s.speed=0;s.carYaw=0;s.doors=0;s.bench=0;s.beltTeeth=0;s.boot=false;s.triangleTaken=false;s.triangleSet=false;s.culvertPlayed=false;s.jam=-1;
        s.hazardFrom=-1;s.doe=DeerRoadScene.DOE_HIDDEN;s.van=DeerRoadScene.VAN_NONE;s.vehicleNext=-1;s.first=null;s.second=null;s.driverSeat=null;s.passengerSeat=null;s.party.clear();s.partySeen.clear();s.emptyTicks=0;
        s.sync();
    }
    /** The hinge side the drainage gate's door uses when the scene places it. */
    static final DoorHingeSide GATE_HINGE=DoorHingeSide.LEFT;
    static boolean player(Player p){return p instanceof ServerPlayer;}
}
