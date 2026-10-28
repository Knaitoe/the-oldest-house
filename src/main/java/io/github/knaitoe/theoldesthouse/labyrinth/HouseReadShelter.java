package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/** Two original, private, quiet strangers. Fixed meeting points never chase a retreating reader. */
public final class HouseReadShelter {
    private HouseReadShelter(){}
    public static boolean role(int role){return role==LiteraryActor.READ_MAN||role==LiteraryActor.READ_WOMAN;}
    public static void ensure(ServerPlayer p,BlockPos b,CompoundTag t){
        for(int i=0;i<2;i++){String key=i==0?"Man":"Woman";if(t.hasUUID(key))continue;if(!p.serverLevel().areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(b.offset(i==0?-6:6,0,-10))))continue;var actor=LiteraryRegistry.ACTOR.get().create(p.serverLevel());if(actor==null)continue;actor.appearance(i==0?LiteraryActor.READ_MAN:LiteraryActor.READ_WOMAN,0);actor.bind(LabyrinthPlace.READ_SHELTER,p.getUUID());actor.setNoGravity(true);actor.moveTo(b.getX()+.5+(i==0?-6:6),b.getY(),b.getZ()-9.5,180,0);actor.setYHeadRot(180);actor.yBodyRot=180;actor.setCustomNameVisible(false);if(p.serverLevel().addFreshEntity(actor))t.putUUID(key,actor.getUUID());}
    }
    public static void tick(LiteraryActor a){
        if(!role(a.role())||a.owner().isEmpty())return;var p=a.level().getServer().getPlayerList().getPlayer(a.owner().get());if(p==null||HouseReadSites.inside(p)!=HouseReadSites.Room.SHELTER){a.setDeltaMovement(Vec3.ZERO);return;}
        var own=HouseReadSites.state(p.server,p.getUUID());var t=own.getCompound(HouseReadSites.Room.SHELTER.key());if(!t.getBoolean("SongStarted")||t.getInt("SongAge")>=900)return;
        var b=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.SHELTER);boolean man=a.role()==LiteraryActor.READ_MAN;String key=man?"ManNear":"WomanNear";
        boolean near=p.distanceToSqr(a)<9;int linger=near?t.getInt(key)+1:0;t.putInt(key,linger);
        if(linger>=60){a.appearance(a.role(),1);a.setYHeadRot((float)Math.toDegrees(Math.atan2(-(p.getX()-a.getX()),p.getZ()-a.getZ())));a.setXRot(-3);if(!t.getBoolean(key+"Noted")){t.putBoolean(key+"Noted",true);t.putBoolean("NoteArmed",true);HouseReadMoments.literal(t,man?"stood beside the boy by the rail":"stood beside the girl with the bottle");}}
        if(t.getInt(key+"Shift")<20&&(linger>=60||t.getInt(key+"Shift")>0)){var before=a.position();a.move(MoverType.SELF,new Vec3(man?-.015:.015,0,0));a.walkAnimation.update((float)a.position().distanceTo(before)*8,.4f);t.putInt(key+"Shift",t.getInt(key+"Shift")+1);}
        boolean middle=Math.abs(p.getX()-b.getX()-.5)<1.5&&p.getZ()<b.getZ()-6&&p.getZ()>b.getZ()-12;
        if(middle){var target=new Vec3(b.getX()+.5+(man?-3.6:3.6),b.getY(),b.getZ()-9.5);var step=target.subtract(a.position()).multiply(1,0,1);if(step.lengthSqr()>.0004&&p.distanceToSqr(a)>10.1){step=step.normalize().scale(Math.min(.035,step.length()));var before=a.position();a.move(MoverType.SELF,step);a.walkAnimation.update((float)a.position().distanceTo(before)*8,.4f);float yaw=(float)Math.toDegrees(Math.atan2(-step.x,step.z));a.setYRot(yaw);a.yBodyRot=yaw;if(!near)a.setYHeadRot(yaw);}}
        a.setDeltaMovement(Vec3.ZERO);own.put(HouseReadSites.Room.SHELTER.key(),t);HouseReadSites.save(p.server,p.getUUID(),own);
    }
}
