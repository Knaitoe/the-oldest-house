package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Camera facts, finite health fuel, and actual nearby bodies; private actors never leak to peers. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class UncannySenses {
    public static final String STATE="uncanny_senses_04113",LENS="UncannyLens04113";
    private UncannySenses(){}
    public static boolean finite(double... values){for(double d:values)if(!Double.isFinite(d))return false;return true;}
    public static void camera(ServerPlayer p,UncannyCameraPayload sample){
        var place=LiteraryVignettes.current(p);
        if(!UncannyRooms.isUncanny(place)||p.isSpectator()||!p.isAlive()||!finite(sample.x(),sample.y(),sample.z(),sample.dx(),sample.dy(),sample.dz(),sample.fov(),sample.aspect()))return;
        var at=new Vec3(sample.x(),sample.y(),sample.z());var look=new Vec3(sample.dx(),sample.dy(),sample.dz());
        if(at.distanceToSqr(p.getEyePosition())>49||Math.abs(look.lengthSqr()-1)>.03||sample.fov()<5||sample.fov()>170||sample.aspect()<.5||sample.aspect()>8)return;
        var previous=p.getPersistentData().getCompound(LENS);long now=p.serverLevel().getGameTime();if(previous.getLong("At")==now)return;
        var lens=new CompoundTag();lens.putDouble("X",at.x);lens.putDouble("Y",at.y);lens.putDouble("Z",at.z);lens.putDouble("DX",look.x);lens.putDouble("DY",look.y);lens.putDouble("DZ",look.z);lens.putFloat("Fov",sample.fov());lens.putFloat("Aspect",sample.aspect());lens.putLong("At",now);
        if(place==LabyrinthPlace.MISSING_SLEEP&&sample.monitor()>=0&&sample.monitor()<4&&p.distanceToSqr(LiteraryVignettes.base(p,place).offset(UncannyRooms.MONITOR_AT).getCenter())<25)lens.putInt("Monitor",sample.monitor());else lens.putInt("Monitor",-1);
        p.getPersistentData().put(LENS,lens);
    }
    public static boolean fresh(ServerPlayer p){var t=p.getPersistentData().getCompound(LENS);long age=p.serverLevel().getGameTime()-t.getLong("At");return t.contains("At")&&age>=0&&age<=20;}
    public static Vec3 eye(ServerPlayer p){var t=p.getPersistentData().getCompound(LENS);return fresh(p)?new Vec3(t.getDouble("X"),t.getDouble("Y"),t.getDouble("Z")):p.getEyePosition();}
    public static Vec3 look(ServerPlayer p){var t=p.getPersistentData().getCompound(LENS);return fresh(p)?new Vec3(t.getDouble("DX"),t.getDouble("DY"),t.getDouble("DZ")):p.getLookAngle();}
    public static double halfFov(ServerPlayer p){var t=p.getPersistentData().getCompound(LENS);return fresh(p)?Math.atan(Math.tan(Math.toRadians(t.getFloat("Fov")*.5))*t.getFloat("Aspect")):Math.toRadians(55);}
    public static double angle(ServerPlayer p,Vec3 point){var v=point.subtract(eye(p));var f=look(p);double horizontal=Math.hypot(v.x,v.z)*Math.hypot(f.x,f.z);if(horizontal<.0001)return 0;return Math.acos(Math.max(-1,Math.min(1,(v.x*f.x+v.z*f.z)/horizontal)));}
    public static boolean clear(ServerPlayer p,Vec3 point){
        var hit=p.serverLevel().clip(new ClipContext(eye(p),point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        return hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceToSqr(point)<.1;
    }
    public static boolean ring(ServerPlayer p){return p.getOffhandItem().is(UncannyRegistry.BONE_RING.get());}
    public static boolean heldMirror(ServerPlayer p){return p.getMainHandItem().is(UncannyRegistry.SMOKY_MIRROR.get())||p.getOffhandItem().is(UncannyRegistry.SMOKY_MIRROR.get());}
    public static boolean mirror(ServerPlayer p){return heldMirror(p)&&LabyrinthData.get(p.server).stateEntry(STATE,p.getUUID().toString()).getInt("MirrorTicks")>0;}
    public static boolean wakeMirror(ServerPlayer p){
        if(!LiteraryVignettes.participant(p)||!heldMirror(p)||p.getHealth()<=2)return false;
        var d=LabyrinthData.get(p.server);var own=d.stateEntry(STATE,p.getUUID().toString());
        if(own.getInt("MirrorTicks")>0)return true;
        float before=p.getHealth();boolean hurt=p.hurt(p.damageSources().magic(),2);
        if(!hurt||!p.isAlive()||p.getHealth()>=before)return false;
        own.putInt("MirrorTicks",240);d.setStateEntry(STATE,p.getUUID().toString(),own);return true;
    }
    private static boolean human(LivingEntity e){
        if(e instanceof LiteraryActor a){if(a.role()==LiteraryActor.HARRIGAN&&a.phase()>=1)return false;return true;}
        String id=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();
        return e instanceof net.minecraft.world.entity.player.Player||e instanceof AbstractVillager||e instanceof net.minecraft.world.entity.monster.Zombie||e instanceof net.minecraft.world.entity.monster.AbstractSkeleton||e instanceof net.minecraft.world.entity.monster.AbstractIllager||e instanceof net.minecraft.world.entity.monster.Witch||id.contains("human")||id.contains("actor")||id.equals("goatman_child");
    }
    private static String intent(LivingEntity e){
        if(e instanceof LiteraryActor a&&UncannyStories.role(a.role()))return switch(a.role()){
            case LiteraryActor.UNCANNY_PROPPED->"Waiting";case LiteraryActor.UNCANNY_FALSE_WAITRESS->a.phase()>=2?"Hungry":"Mimicking";case LiteraryActor.UNCANNY_GIANT->a.phase()==1?"Waiting":"Following";case LiteraryActor.UNCANNY_MARIONETTE->"Mimicking";case LiteraryActor.UNCANNY_PEELER->a.phase()>=2?"Afraid":"Holding";default->"Afraid";};
        if(e instanceof GoatmanChild child)return child.stalkingAppearance()?"Mimicking":child.cowering()?"Afraid":"Waiting";
        if(e instanceof net.minecraft.world.entity.player.Player)return "Unknown";
        if(e instanceof Mob m&&m.getTarget()!=null)return "Hunting";
        return e.getLastHurtByMob()!=null?"Afraid":"Waiting";
    }
    public static CompoundTag view(ServerPlayer p){
        var view=new CompoundTag();var place=LiteraryVignettes.current(p);
        if(UncannyRooms.isUncanny(place)){
            var b=LiteraryVignettes.base(p,place);var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),place);
            view.putString("Room",place.id());view.putLong("Base",b.asLong());view.putInt("Age",own.getInt("UncannyAge"));view.putInt("Stage",own.getInt("UncannyStage"));view.putInt("Sleeps",own.getInt("Sleeps"));view.putInt("Night",own.getInt("NightClock"));
            view.putBoolean("Green",UncannyStories.green(own));view.putBoolean("Dark",p.serverLevel().getBlockState(b.offset(UncannyRooms.FLOOD)).is(UncannyRegistry.PROP.get())&&p.serverLevel().getBlockState(b.offset(UncannyRooms.FLOOD)).getValue(UncannyPropBlock.STAGE)>0);
            view.putInt("Edge",own.getInt("Edge"));view.putInt("Growth",own.getInt("GrowthCue"));view.putBoolean("Ready",own.getBoolean("Ready"));view.putBoolean("Failed",own.getBoolean("UncannyFailed"));
            view.putInt("Engulfment",own.getInt("Engulfment"));
            view.putLong("RumbleUntil130",own.getLong("CellarRumbleUntil130"));
            view.putString("Sign",UncannyStories.sign(p,b,own));view.putInt("Doll",own.contains("CastDoll")&&own.hasUUID("CastDoll")&&p.serverLevel().getEntity(own.getUUID("CastDoll")) instanceof LiteraryActor a?a.getId():-1);
        }
        view.putBoolean("Mirror",mirror(p));view.putBoolean("Ring",ring(p));
        if(ring(p)||mirror(p)){
            var list=new ListTag();
            for(var e:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(14),e->e!=p&&e.isAlive()&&human(e))){
                if(e instanceof LiteraryActor a&&a.owner().isPresent()&&!a.owner().get().equals(p.getUUID()))continue;
                if(e instanceof GoatmanChild a&&a.viewer().isPresent()&&!a.viewer().get().equals(p.getUUID()))continue;
                if(list.size()>=24)break;var row=new CompoundTag();row.putInt("Id",e.getId());row.putString("Intent",intent(e));list.add(row);
            }view.put("Sensed",list);
        }
        return view;
    }
    public static void send(ServerPlayer p,boolean open){var v=view(p);v.putBoolean("OpenMonitor",open);HousePackets.send(p,new UncannyViewPayload(v));}
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        if(event.getServer().getTickCount()%5!=0)return;
        for(var p:event.getServer().getPlayerList().getPlayers()){
            if(event.getServer().getTickCount()%20==0)UncannyCellar.migrateKeys(p);
            if(LiteraryVignettes.current(p)!=LabyrinthPlace.LAYERED_MAN&&p.hasEffect(UncannyRegistry.ENGULFED))p.removeEffect(UncannyRegistry.ENGULFED);
            var d=LabyrinthData.get(p.server);var own=d.stateEntry(STATE,p.getUUID().toString());int fuel=own.getInt("MirrorTicks");if(!p.isAlive())fuel=0;if(fuel>0){own.putInt("MirrorTicks",Math.max(0,fuel-5));d.setStateEntry(STATE,p.getUUID().toString(),own);}
            boolean active=p.isAlive()&&!p.isSpectator()&&(UncannyRooms.isUncanny(LiteraryVignettes.current(p))||ring(p)||heldMirror(p));
            if(active)send(p,false);else if(p.getPersistentData().getBoolean("UncannyViewActive"))HousePackets.send(p,new UncannyViewPayload(new CompoundTag()));
            p.getPersistentData().putBoolean("UncannyViewActive",active);
        }
    }
}
