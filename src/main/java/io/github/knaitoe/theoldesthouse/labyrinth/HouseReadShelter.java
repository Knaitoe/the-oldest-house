package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/**
 * Two original, private, quiet strangers, mirror images on the west and east rails (0.4.159). They are the reader's age when the
 * waiting room read it: young adults, around forty, or older. Fixed meeting points never chase a retreating reader.
 */
public final class HouseReadShelter {
    private HouseReadShelter(){}
    public static boolean role(int role){return role==LiteraryActor.READ_MAN||role==LiteraryActor.READ_WOMAN;}
    /** The appearance phase: two per age (quiet, then the half-smile), young adults first. */
    public static int phase(int age,boolean smile){return age*2+(smile?1:0);}
    public static int age(ServerPlayer p){return switch(HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.AGE,false)){case "ANALOG"->2;case "TRANSITIONAL"->1;default->0;};}
    public static void ensure(ServerPlayer p,BlockPos b,CompoundTag t){
        if(!t.contains("CastAge"))t.putInt("CastAge",age(p));
        for(int i=0;i<2;i++){String key=i==0?"Man":"Woman";if(t.hasUUID(key))continue;double x=b.getX()+.5+(i==0?-HouseReadRemainingRooms.FIGURE_X:HouseReadRemainingRooms.FIGURE_X),z=b.getZ()+HouseReadRemainingRooms.FIGURE_Z;if(!p.serverLevel().areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(BlockPos.containing(x,b.getY(),z))))continue;var actor=LiteraryRegistry.ACTOR.get().create(p.serverLevel());if(actor==null)continue;
            // Each leans back on their own rail, facing across the floor.
            float yaw=i==0?-90:90;actor.appearance(i==0?LiteraryActor.READ_MAN:LiteraryActor.READ_WOMAN,phase(t.getInt("CastAge"),false));actor.bind(LabyrinthPlace.READ_SHELTER,p.getUUID());actor.setNoGravity(true);actor.moveTo(x,b.getY(),z,yaw,0);actor.setYHeadRot(yaw);actor.yBodyRot=yaw;actor.setCustomNameVisible(false);if(p.serverLevel().addFreshEntity(actor))t.putUUID(key,actor.getUUID());}
    }
    public static void tick(LiteraryActor a){
        if(!role(a.role())||a.owner().isEmpty())return;var p=a.level().getServer().getPlayerList().getPlayer(a.owner().get());if(p==null||HouseReadSites.inside(p)!=HouseReadSites.Room.SHELTER){a.setDeltaMovement(Vec3.ZERO);return;}
        var own=HouseReadSites.state(p.server,p.getUUID());var t=own.getCompound(HouseReadSites.Room.SHELTER.key());if(!t.getBoolean("SongStarted")||t.getInt("SongAge")>=900)return;
        var b=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.SHELTER);boolean man=a.role()==LiteraryActor.READ_MAN;String key=man?"ManNear":"WomanNear";int age=t.getInt("CastAge");
        // A step away does not undo standing beside them; walking off does.
        boolean near=p.distanceToSqr(a)<9;int linger=near?t.getInt(key)+1:Math.max(0,t.getInt(key)-3);t.putInt(key,linger);
        if(linger>=60&&near){a.appearance(a.role(),phase(age,true));a.setYHeadRot((float)Math.toDegrees(Math.atan2(-(p.getX()-a.getX()),p.getZ()-a.getZ())));a.setXRot(-3);if(!t.getBoolean(key+"Noted")){t.putBoolean(key+"Noted",true);t.putBoolean("NoteArmed",true);boolean older=age>0;HouseReadMoments.literal(t,man?(older?"stood beside the man by the rail":"stood beside the boy by the rail"):(older?"stood beside the woman with the bottle":"stood beside the girl with the bottle"));HouseRead.first(p,HouseReadSites.Room.SHELTER,"Stood",man?"man":"woman");}}
        // Making room on the rail: a small shift along it, toward the lake, the same for both.
        if(t.getInt(key+"Shift")<20&&(linger>=60||t.getInt(key+"Shift")>0)){var before=a.position();a.move(MoverType.SELF,new Vec3(0,0,-.015));a.walkAnimation.update((float)a.position().distanceTo(before)*8,.4f);t.putInt(key+"Shift",t.getInt(key+"Shift")+1);}
        if(HouseReadRemainingRooms.middle(b,p.position())){var target=new Vec3(b.getX()+.5+(man?-3.6:3.6),b.getY(),b.getZ()+HouseReadRemainingRooms.FIGURE_Z);var step=target.subtract(a.position()).multiply(1,0,1);if(step.lengthSqr()>.0004&&p.distanceToSqr(a)>10.1){step=step.normalize().scale(Math.min(.035,step.length()));var before=a.position();a.move(MoverType.SELF,step);a.walkAnimation.update((float)a.position().distanceTo(before)*8,.4f);float yaw=(float)Math.toDegrees(Math.atan2(-step.x,step.z));a.setYRot(yaw);a.yBodyRot=yaw;if(!near)a.setYHeadRot(yaw);}}
        a.setDeltaMovement(Vec3.ZERO);own.put(HouseReadSites.Room.SHELTER.key(),t);HouseReadSites.save(p.server,p.getUUID(),own);
    }
}
