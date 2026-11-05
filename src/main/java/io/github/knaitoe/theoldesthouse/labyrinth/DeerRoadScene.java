package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.310: the road's one shared state, kept on the anchor under the asphalt and sent to every client on the road: where the car is
 * and how it stands, its doors, boot, bench and belt; the doe; the next vehicle; the two women by id; who is on the road. The story
 * itself is DeerRoad's. Positions are relative to the scene's base.
 */
public final class DeerRoadScene extends BlockEntity {
    /** The car: parked at the bend with its engine running, driving, sliding after the strike, stopped. */
    public static final int FRESH=0,DRIVE=1,SKID=2,STOPPED=3;
    /** What the driver did in the one beat they had. */
    public static final int NONE=0,BRAKE=1,LEFT=2,RIGHT=3;
    /** The story once the car has stopped (DeerRoad). */
    public static final int HUSH=0,BIRTH1=1,LIFE1=2,DEAD1=3,REEL=4,BIRTH2=5,LIFE2=6,DEAD2=7;
    /** The doe: not yet out, stepping across, thrown, lying, opened. */
    public static final int DOE_HIDDEN=0,DOE_STEP=1,DOE_THROWN=2,DOE_LYING=3,DOE_OPEN=4;
    /** The next vehicle: none, coming up from the bend, stopped alongside, going on. */
    public static final int VAN_NONE=0,VAN_COMING=1,VAN_STOPPED=2,VAN_GOING=3;
    public int car=FRESH,story=HUSH,clock,carClock,response=NONE,shift,finalZ;
    public double carX=DeerRoadSite.LANE+.5,carZ=DeerRoadSite.START_Z+.5,speed;public float carYaw;
    public int doors,bench,beltTeeth;public boolean boot,triangleTaken,triangleSet,culvertPlayed;public int jam=-1;
    public long[] doorAt=new long[3];public long bootAt,hazardFrom=-1,storyAt;
    public int doe=DOE_HIDDEN;public double doeX,doeZ,doeY;public float doeYaw;public long doeAt;public double doeRestX,doeRestZ;
    public int van=VAN_NONE;public double vanX,vanZ;public boolean vanSlow;public int vanClock;public long vehicleNext=-1;
    public @Nullable UUID first,second,driverSeat,passengerSeat;
    public final Set<UUID> party=new HashSet<>();
    public final Map<UUID,Long> partySeen=new HashMap<>();
    public int emptyTicks;
    /** Client only: where the moving things were at the previous update, for smooth drawing between updates. */
    public double pCarX,pCarZ,pDoeX,pDoeZ,pDoeY,pVanX,pVanZ;public float pCarYaw,pDoeYaw;public long syncedAt;
    public DeerRoadScene(BlockPos at,BlockState state){super(DeerRoadRegistry.SCENE.get(),at,state);}
    public BlockPos base(){return worldPosition.subtract(DeerRoadSite.ANCHOR);}
    /** The stopped car's root cell, relative to the base. */
    public BlockPos finalRoot(){return new BlockPos(DeerRoadSite.LANE+shift,0,finalZ);}
    public Vec3 carCentre(){return Vec3.atLowerCornerOf(base()).add(carX,0,carZ);}
    public boolean open(int door){return (doors&(1<<door))!=0;}
    public boolean stopped(){return car==STOPPED;}
    /** Where a rider steps out: by the driver's door if it is open, else out of the passenger's side. */
    public Vec3 dismount(int slot){
        var c=carCentre();
        boolean west=slot==0?jam!=DeerCarMesh.DRIVER&&(open(DeerCarMesh.DRIVER)||!open(DeerCarMesh.PASSENGER)):!open(DeerCarMesh.PASSENGER)&&open(DeerCarMesh.DRIVER)&&jam!=DeerCarMesh.DRIVER;
        return c.add(west?-2.3:2.3,0,west?.6:.6);
    }
    public void sync(){setChanged();if(level!=null&&!level.isClientSide){level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}}
    public void tick(){DeerRoad.tick(this);}
    private static void uuid(CompoundTag t,String k,@Nullable UUID id){if(id!=null)t.putUUID(k,id);}
    private static @Nullable UUID uuid(CompoundTag t,String k){return t.hasUUID(k)?t.getUUID(k):null;}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){
        super.saveAdditional(t,r);
        t.putInt("Car",car);t.putInt("Story",story);t.putInt("Clock",clock);t.putInt("CarClock",carClock);t.putInt("Response",response);t.putInt("Shift",shift);t.putInt("FinalZ",finalZ);
        t.putDouble("CarX",carX);t.putDouble("CarZ",carZ);t.putDouble("Speed",speed);t.putFloat("CarYaw",carYaw);
        t.putInt("Doors",doors);t.putInt("Bench",bench);t.putInt("Belt",beltTeeth);t.putBoolean("Boot",boot);t.putBoolean("TriangleTaken",triangleTaken);t.putBoolean("TriangleSet",triangleSet);t.putBoolean("Culvert",culvertPlayed);t.putInt("Jam",jam);
        t.putLongArray("DoorAt",doorAt);t.putLong("BootAt",bootAt);t.putLong("Hazard",hazardFrom);t.putLong("StoryAt",storyAt);
        t.putInt("Doe",doe);t.putDouble("DoeX",doeX);t.putDouble("DoeZ",doeZ);t.putDouble("DoeY",doeY);t.putFloat("DoeYaw",doeYaw);t.putLong("DoeAt",doeAt);t.putDouble("DoeRestX",doeRestX);t.putDouble("DoeRestZ",doeRestZ);
        t.putInt("Van",van);t.putDouble("VanX",vanX);t.putDouble("VanZ",vanZ);t.putBoolean("VanSlow",vanSlow);t.putInt("VanClock",vanClock);t.putLong("VehicleNext",vehicleNext);
        uuid(t,"First",first);uuid(t,"Second",second);uuid(t,"DriverSeat",driverSeat);uuid(t,"PassengerSeat",passengerSeat);
        var list=new ListTag();for(var id:party){var e=new CompoundTag();e.putUUID("Id",id);e.putLong("Seen",partySeen.getOrDefault(id,0L));list.add(e);}t.put("Party",list);
        t.putInt("Empty",emptyTicks);
    }
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){
        super.loadAdditional(t,r);
        if(level!=null&&level.isClientSide){pCarX=carX;pCarZ=carZ;pCarYaw=carYaw;pDoeX=doeX;pDoeZ=doeZ;pDoeY=doeY;pDoeYaw=doeYaw;pVanX=vanX;pVanZ=vanZ;syncedAt=level.getGameTime();}
        car=t.getInt("Car");story=t.getInt("Story");clock=t.getInt("Clock");carClock=t.getInt("CarClock");response=t.getInt("Response");shift=t.getInt("Shift");finalZ=t.getInt("FinalZ");
        if(t.contains("CarX")){carX=t.getDouble("CarX");carZ=t.getDouble("CarZ");}speed=t.getDouble("Speed");carYaw=t.getFloat("CarYaw");
        doors=t.getInt("Doors");bench=t.getInt("Bench");beltTeeth=t.getInt("Belt");boot=t.getBoolean("Boot");triangleTaken=t.getBoolean("TriangleTaken");triangleSet=t.getBoolean("TriangleSet");culvertPlayed=t.getBoolean("Culvert");jam=t.contains("Jam")?t.getInt("Jam"):-1;
        var at=t.getLongArray("DoorAt");doorAt=at.length==3?at:new long[3];bootAt=t.getLong("BootAt");hazardFrom=t.contains("Hazard")?t.getLong("Hazard"):-1;storyAt=t.getLong("StoryAt");
        doe=t.getInt("Doe");doeX=t.getDouble("DoeX");doeZ=t.getDouble("DoeZ");doeY=t.getDouble("DoeY");doeYaw=t.getFloat("DoeYaw");doeAt=t.getLong("DoeAt");doeRestX=t.getDouble("DoeRestX");doeRestZ=t.getDouble("DoeRestZ");
        van=t.getInt("Van");vanX=t.getDouble("VanX");vanZ=t.getDouble("VanZ");vanSlow=t.getBoolean("VanSlow");vanClock=t.getInt("VanClock");vehicleNext=t.contains("VehicleNext")?t.getLong("VehicleNext"):-1;
        first=uuid(t,"First");second=uuid(t,"Second");driverSeat=uuid(t,"DriverSeat");passengerSeat=uuid(t,"PassengerSeat");
        party.clear();partySeen.clear();for(var e:t.getList("Party",Tag.TAG_COMPOUND)){var c=(CompoundTag)e;if(c.hasUUID("Id")){party.add(c.getUUID("Id"));partySeen.put(c.getUUID("Id"),c.getLong("Seen"));}}
        emptyTicks=t.getInt("Empty");
        if(level!=null&&level.isClientSide&&car!=DRIVE&&car!=SKID){pCarX=carX;pCarZ=carZ;pCarYaw=carYaw;}
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveAdditional(t,r);t.remove("Party");return t;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
