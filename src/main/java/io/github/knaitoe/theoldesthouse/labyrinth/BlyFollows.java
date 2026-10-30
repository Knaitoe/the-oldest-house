package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Bly's Lady follows the one she held (0.4.189), as she came back for Dani: the room's own hold and carry, smaller, in the
 * reader's world. Once a night, near water in the dark, the dark closes in; they are held a moment and walked a few steps
 * they did not take, and set down at the water's edge, never in it, with wet footprints from the water to where they stood.
 * It never wounds them and never happens in the House. It ends when they wait out a later night at Bly in a shut cupboard.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BlyFollows {
    private BlyFollows(){}
    static final LabyrinthPlace PLACE=LabyrinthPlace.BLY_ROUTE;
    public static final String FOLLOWS="LadyFollows0189",NIGHT="LadyNight0189";
    /** How near water must be, how long the hold lasts before the carry, and how long the carry takes. */
    public static final int NEAR=6,HOLD=20,CARRY=20;
    private record Carry(Vec3 from,Vec3 to,long start){}
    private static final Map<UUID,Carry> CARRIES=new HashMap<>();

    private static CompoundTagAccess own(ServerPlayer p){return new CompoundTagAccess(p);}
    /** A small holder so the reader's Bly record is read and written in one place. */
    private record CompoundTagAccess(ServerPlayer p){
        net.minecraft.nbt.CompoundTag get(){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
        void put(net.minecraft.nbt.CompoundTag t){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,t);}
    }
    public static boolean follows(ServerPlayer p){return own(p).get().getBoolean(FOLLOWS);}
    public static boolean carrying(ServerPlayer p){return CARRIES.containsKey(p.getUUID());}

    /** The nearest dry, standable place at the water's edge, or null if there is no water near. */
    static Vec3 edge(ServerLevel l,ServerPlayer p){
        BlockPos at=p.blockPosition();Vec3 best=null;double bestD=Double.MAX_VALUE;
        for(var c:BlockPos.betweenClosed(at.offset(-NEAR,-2,-NEAR),at.offset(NEAR,1,NEAR))){
            if(!l.getFluidState(c).is(FluidTags.WATER))continue;
            // The bank beside the water: level with it, or a block up where the water lies in the ground.
            for(var side:net.minecraft.core.Direction.Plane.HORIZONTAL)for(int up=0;up<=1;up++){var stand=c.relative(side).above(up);
                if(l.getFluidState(stand).is(FluidTags.WATER)||!l.getBlockState(stand).getCollisionShape(l,stand).isEmpty()||l.getBlockState(stand.below()).getCollisionShape(l,stand.below()).isEmpty()||!l.getBlockState(stand.above()).getCollisionShape(l,stand.above()).isEmpty())continue;
                var v=Vec3.atBottomCenterOf(stand);double d=v.distanceToSqr(p.position());if(d<bestD){bestD=d;best=v;}}
        }
        return best;
    }
    /** The hold and the carry: held a moment in the dark, then walked to the water's edge. True if it began. */
    public static boolean carry(ServerPlayer p){
        var l=p.serverLevel();if(carrying(p)||HouseDimensions.isHouseDimension(l.dimension())||p.isInWater())return false;
        var to=edge(l,p);if(to==null||to.distanceToSqr(p.position())<1)return false;
        CARRIES.put(p.getUUID(),new Carry(p.position(),to,l.getGameTime()));
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,HOLD+CARRY+40,0,false,false));
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,HOLD,9,false,false));
        LiteraryVignettes.heard(p,SoundEvents.PLAYER_SPLASH,BlockPos.containing(to),.5F,.6F);
        p.displayClientMessage(Component.literal("Something wet takes your arm.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
        return true;
    }
    static void step(ServerPlayer p,Carry c){
        var l=p.serverLevel();long t=l.getGameTime()-c.start();
        if(t<HOLD){p.setDeltaMovement(Vec3.ZERO);p.hurtMarked=true;return;}
        double f=Math.min(1,(t-HOLD)/(double)CARRY);var at=c.from().lerp(c.to(),f);
        p.teleportTo(l,at.x,Math.max(at.y,c.to().y),at.z,p.getYRot(),p.getXRot());p.fallDistance=0;
        if(f>=1){
            CARRIES.remove(p.getUUID());
            var dir=c.from().subtract(c.to());double yaw=Math.atan2(dir.z,dir.x);
            for(int i=1;i<=4;i++){var foot=c.to().add(dir.normalize().scale(i*.9));l.sendParticles(p,LakeHouseRegistry.WET_FOOTPRINT.get(),true,foot.x,c.to().y+.02,foot.z,0,yaw,0,0,1);}
            p.displayClientMessage(Component.literal("You are standing at the water's edge. Wet footprints lead from the water to where you were.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);
        }
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();
        for(var p:server.getPlayerList().getPlayers()){var c=CARRIES.get(p.getUUID());if(c!=null){if(!p.isAlive())CARRIES.remove(p.getUUID());else step(p,c);}}
        if(server.getTickCount()%20!=0)return;
        long time=server.overworld().getDayTime();int night=(int)(time/24000L);
        for(var p:server.getPlayerList().getPlayers()){
            if(!p.isAlive()||p.isSpectator()||carrying(p)||!p.serverLevel().isNight())continue;
            var own=own(p).get();if(!own.getBoolean(FOLLOWS)||own.getInt(NIGHT)==night+1)continue;
            if(carry(p)){own.putInt(NIGHT,night+1);own(p).put(own);}
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){CARRIES.remove(e.getEntity().getUUID());}
}
