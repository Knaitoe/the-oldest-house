package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
/** One original, usually submerged. Brief native emergence/pursuit/contact; never a talking hotel guest. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HotelBathHaunt {
    public static final String STATE="hotel_bath_04113";
    public static final int DORMANT=0,RISING=1,PURSUIT=2,FADING=3,RISE_TICKS=60,FADE_TICKS=24,REST_TICKS=3600;
    private HotelBathHaunt(){}
    public static Vec3 submerged(BlockPos b){return new Vec3(b.getX()-26.5,b.getY()+4.5,b.getZ()-24.25);}
    /** High stage bit changes only the actual tub's contents; facing, half, collision and player edits remain native. */
    private static void contents(ServerLevel l,BlockPos b,boolean present){
        for(int half=0;half<2;half++){
            var at=b.offset(-27,6,-25+half);if(!l.hasChunkAt(at))continue;
            var old=l.getBlockState(at);if(!StanleyBlock.is(old,StanleyBlock.Kind.TUB))continue;
            int stage=(old.getValue(StanleyBlock.STAGE)&1)|(present?2:0);
            if(old.getValue(StanleyBlock.STAGE)!=stage)l.setBlock(at,old.setValue(StanleyBlock.STAGE,stage),2);
        }
    }
    public static boolean inBathroom(ServerPlayer p,BlockPos b){var v=p.position().subtract(Vec3.atLowerCornerOf(b));return HotelVignette.inside(p,LabyrinthPlace.HOTEL)&&v.y>=6&&v.y<9&&v.x>=-27.7&&v.x< -22&&v.z>=-26.7&&v.z< -23;}
    public static CompoundTag state(ServerLevel l){return LabyrinthData.get(l.getServer()).state(STATE);}
    private static void save(ServerLevel l,CompoundTag state){LabyrinthData.get(l.getServer()).setState(STATE,state);}
    public static HotelActor original(ServerLevel l){var all=HotelVignette.world(LabyrinthData.get(l.getServer()));return all.hasUUID("BathGhost")&&l.getEntity(all.getUUID("BathGhost")) instanceof HotelActor a?a:null;}
    public static boolean begin(ServerPlayer p,boolean operator){
        var l=p.serverLevel();var b=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);var ghost=original(l);var state=state(l);
        if(b==null||ghost==null||ghost.role()!=6||state.getInt("Phase")!=0||!HotelVignette.inside(p,LabyrinthPlace.HOTEL)||!p.isAlive()||p.isSpectator()||!operator&&!inBathroom(p,b))return false;
        if(!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(BlockPos.containing(submerged(b)))))return false;
        ghost.moveTo(submerged(b));ghost.setNoGravity(true);ghost.noPhysics=true;ghost.bathState(RISING,0);ghost.setDeltaMovement(Vec3.ZERO);contents(l,b,true);
        state.putInt("Phase",RISING);state.putInt("Age",0);state.putInt("Hunt",0);state.putInt("HallTicks",0);state.putUUID("Target",p.getUUID());state.putUUID("Original",ghost.getUUID());state.putString("LastEnd","");state.putBoolean("Operator",operator);save(l,state);
        var own=HotelVignette.personal(LabyrinthData.get(p.server),p.getUUID());own.putInt("BathRest04113",REST_TICKS);HotelVignette.save(LabyrinthData.get(p.server),p.getUUID(),own);
        l.playSound(null,b.offset(-27,6,-25),SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,SoundSource.BLOCKS,.4F,.55F);return true;
    }
    private static void fade(ServerLevel l,CompoundTag state,String reason){if(state.getInt("Phase")==FADING||state.getInt("Phase")==0)return;state.putInt("Phase",FADING);state.putInt("Age",0);state.putString("LastEnd",reason);save(l,state);}
    public static void dismiss(ServerPlayer p){var state=state(p.serverLevel());if(state.hasUUID("Target")&&state.getUUID("Target").equals(p.getUUID()))fade(p.serverLevel(),state,"operator_dismissed");}
    private static void touch(ServerPlayer p,ServerLevel l,CompoundTag state){
        p.setTicksFrozen(Math.max(p.getTicksFrozen(),p.getTicksRequiredToFreeze()));
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,1,false,true,true));
        p.displayClientMessage(Component.literal("Cold fingers close around your wrist."),true);
        var own=HotelVignette.personal(LabyrinthData.get(p.server),p.getUUID());own.putInt("BathTouches04113",own.getInt("BathTouches04113")+1);HotelVignette.save(LabyrinthData.get(p.server),p.getUUID(),own);
        fade(l,state,"touched_the_reader");
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        var s=event.getServer();var l=s.getLevel(HouseDimensions.INTERIOR);var b=IndianLakeRooms.base(s,LabyrinthPlace.HOTEL);if(l==null||b==null)return;
        var ghost=original(l);if(ghost==null)return;var state=state(l);int phase=state.getInt("Phase");
        if(phase==0){
            ghost.bathState(0,0);ghost.setDeltaMovement(Vec3.ZERO);contents(l,b,false);
            for(var p:l.players()){
                if(!HotelVignette.inside(p,LabyrinthPlace.HOTEL)||!p.isAlive()||p.isSpectator())continue;
                var d=LabyrinthData.get(s);var own=HotelVignette.personal(d,p.getUUID());int rest=Math.max(0,own.getInt("BathRest04113")-1);own.putInt("BathRest04113",rest);
                boolean inside=inBathroom(p,b),enter=inside&&!own.getBoolean("BathInside04113");own.putBoolean("BathInside04113",inside);
                if(enter&&rest==0&&p.containerMenu==p.inventoryMenu){
                    own.putInt("BathRolls04113",own.getInt("BathRolls04113")+1);own.putInt("BathRest04113",REST_TICKS);HotelVignette.save(d,p.getUUID(),own);
                    if(p.getRandom().nextInt(3)==0&&begin(p,false))return;
                }else HotelVignette.save(d,p.getUUID(),own);
            }return;
        }
        if(!state.hasUUID("Original")||!state.getUUID("Original").equals(ghost.getUUID()))return;
        contents(l,b,true);
        var target=state.hasUUID("Target")?s.getPlayerList().getPlayer(state.getUUID("Target")):null;
        if(phase!=FADING&&(target==null||!target.isAlive()||target.isSpectator()||!HotelVignette.inside(target,LabyrinthPlace.HOTEL))){fade(l,state,"reader_left");phase=FADING;}
        if(phase!=FADING&&target.containerMenu!=target.inventoryMenu){ghost.setDeltaMovement(Vec3.ZERO);return;}
        int age=state.getInt("Age")+1;state.putInt("Age",age);
        if(phase==RISING){
            ghost.noPhysics=true;var start=submerged(b);double y=start.y+(b.getY()+6.625-start.y)*Math.min(1,age/(double)RISE_TICKS);ghost.move(MoverType.SELF,new Vec3(0,y-ghost.getY(),0));ghost.bathState(RISING,age);
            if(age%8==0)l.sendParticles(ParticleTypes.SPLASH,b.getX()-26.5,b.getY()+6.64,b.getZ()-24.25,3,.22,.01,.3,.01);
            if(age>=RISE_TICKS){state.putInt("Phase",PURSUIT);state.putInt("Age",0);ghost.noPhysics=false;ghost.bathState(PURSUIT,0);}
        }else if(phase==PURSUIT){
            ghost.noPhysics=false;ghost.bathState(PURSUIT,age);state.putInt("Hunt",state.getInt("Hunt")+1);
            var delta=target.position().subtract(ghost.position());float yaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z));ghost.setYRot(yaw);ghost.yBodyRot=yaw;ghost.setYHeadRot(yaw);
            if(ghost.getBoundingBox().inflate(.32,.12,.32).intersects(target.getBoundingBox())&&ghost.hasLineOfSight(target)){touch(target,l,state);return;}
            if(ghost.getX()>b.getX()-15.4)state.putInt("HallTicks",state.getInt("HallTicks")+1);
            if(state.getInt("HallTicks")>=40||state.getInt("Hunt")>=800||target.getY()<b.getY()+4){fade(l,state,state.getInt("HallTicks")>=40?"followed_into_the_hall":"the_bath_went_quiet");return;}
            SceneFootpaths.step(ghost,target.position(),.065);
        }else if(phase==FADING){
            ghost.setDeltaMovement(Vec3.ZERO);ghost.bathState(FADING,age);
            if(age>=FADE_TICKS){ghost.bathState(0,0);ghost.noPhysics=true;ghost.moveTo(submerged(b));state.putInt("Phase",0);state.putInt("Age",0);state.remove("Target");contents(l,b,false);}
        }save(l,state);
    }
}
