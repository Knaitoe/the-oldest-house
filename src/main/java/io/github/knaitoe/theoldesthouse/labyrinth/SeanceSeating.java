package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.*;

/** Native seat anchors and a checked floor step precede the original family's flight. */
public final class SeanceSeating {
    public static final List<BlockPos> CHAIRS=List.of(new BlockPos(0,0,-13),new BlockPos(-3,0,-15),new BlockPos(2,0,-15),new BlockPos(0,0,-18));
    public static final List<Vec3> STANDS=List.of(new Vec3(.5,0,-13.5),new Vec3(-1.5,0,-14.5),new Vec3(1.5,0,-14.5),new Vec3(.5,0,-16.5));
    private SeanceSeating(){}
    public static boolean seat(SeanceActor a,BlockPos b){
        var l=(ServerLevel)a.level();var at=b.offset(CHAIRS.get(a.role()));var s=l.getBlockState(at);
        if(!HouseSitting.isSeat(s)||!l.getEntitiesOfClass(SeatEntity.class,new AABB(at),e->e.isVehicle()&&e!=a.getVehicle()).isEmpty())return false;
        if(a.getVehicle() instanceof SeatEntity){a.appearance(a.role(),true,a.afraid());return true;}
        var anchor=HouseSitting.SEAT.get().create(l);if(anchor==null)return false;anchor.support(at);
        double height=s.getBlock() instanceof HouseholdFurnitureBlock?s.getValue(HouseholdFurnitureBlock.KIND).seatHeight/16D:.5;
        anchor.moveTo(at.getX()+.5,at.getY()+height,at.getZ()+.5);
        if(!l.addFreshEntity(anchor)||!a.startRiding(anchor,true)){anchor.discard();return false;}
        float yaw=s.getValue(HouseholdFurnitureBlock.FACING).toYRot();a.setYRot(yaw);a.setYHeadRot(yaw);a.yBodyRot=yaw;a.setDeltaMovement(Vec3.ZERO);a.appearance(a.role(),true,false);return true;
    }
    public static void scare(SeanceActor a){
        if(!a.getPersistentData().getBoolean("SeanceRaised131")&&!a.getPersistentData().contains("SeanceRiseAt131"))a.getPersistentData().putLong("SeanceRiseAt131",a.level().getGameTime()+6);
        a.appearance(a.role(),a.seated(),true);
    }
    public static void tick(ServerLevel l,BlockPos b,List<SeanceActor> cast){
        for(var a:cast){var data=a.getPersistentData();if(!data.contains("SeanceRiseAt131")||l.getGameTime()<data.getLong("SeanceRiseAt131"))continue;
            var at=STANDS.get(a.role()).add(b.getX(),b.getY(),b.getZ());var feet=BlockPos.containing(at);
            var box=a.getBoundingBox().move(at.subtract(a.position()));
            if(!l.getBlockState(feet.below()).isFaceSturdy(l,feet.below(),Direction.UP)||!l.noCollision(a,box))continue;
            var anchor=a.getVehicle();a.stopRiding();a.moveTo(at);a.setDeltaMovement(Vec3.ZERO);a.setNoGravity(false);a.appearance(a.role(),false,true);
            if(anchor instanceof SeatEntity&&anchor.getPassengers().isEmpty())anchor.discard();
            data.remove("SeanceRiseAt131");data.putBoolean("SeanceRaised131",true);data.putLong("SeanceFlightAt131",l.getGameTime()+12+a.role()*3);
        }
    }
}
