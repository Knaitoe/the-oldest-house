package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

/**
 * Diving at the lake house: breath, the lent gear and the blackout.
 * <ul>
 * <li><b>Breath is the vanilla air bar</b>, but the scene keeps it: under water a reader's air runs down a point a tick and
 * comes back four a tick in the air, whatever potion, conduit, turtle shell or Respiration they brought. Only the
 * vignette's own gear helps.</li>
 * <li><b>The helmet</b> breathes while its hose is fed: the compressor on the raft is running (somebody minds it, or Tess
 * does for a reader alone), the hose is not caught, and the diver is within its reach.</li>
 * <li><b>The tanks</b> breathe for a minute a fill, their durability bar the gauge; the compressor fills them again.</li>
 * <li><b>The flashlight</b> is a real light where its beam lands, moved as it swings; everyone sees it.</li>
 * <li><b>Out of air</b> the reader chokes for four seconds; air back in time ends it in a gasp. Otherwise they black out and
 * come to on the raft (or the dock, if they never got through the culvert) with half a breath, everything else kept.</li>
 * </ul>
 * Only the beats and the hose's snag are saved; air, choking and lights are the server's for the moment.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LakeHouseDive {
    public static final int CHOKE=80,BLACKOUT=40,WAKE=24;
    /** A fill of the tanks, in ticks under water. */
    public static final int TANK_AIR=1200;
    /** How far the hose reaches from the compressor: the ground floor, not the far rooms upstairs. */
    public static final double HOSE=32;
    /** The step's handrails, where the hose catches the first time the diver goes in. */
    public static final BlockPos[] RAILS={new BlockPos(-1,LakeHouse.GROUND+1,LakeHouse.HZ_FRONT+1),new BlockPos(1,LakeHouse.GROUND+1,LakeHouse.HZ_FRONT+1)};
    private static final DustParticleOptions HOSE_DUST=new DustParticleOptions(new Vector3f(.32F,.13F,.1F),.75F);

    enum Source{AIR,HOSE,TANKS,NONE}
    private record Light(ServerLevel level,BlockPos at,boolean water){}
    private static final Map<UUID,Integer> AIR=new HashMap<>(),CHOKING=new HashMap<>(),OUT=new HashMap<>();
    private static final Map<UUID,Light> LIGHTS=new HashMap<>();
    /** Divers whose hose is caught or stretched to its end: the helmet gives them nothing. */
    private static final Set<UUID> HOSE_BLOCKED=new HashSet<>();
    private LakeHouseDive(){}

    // ------------------------------------------------------------------------------------------------ every tick
    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        if(p.level().dimension()!=HouseDimensions.OUTSIDE){if(LIGHTS.containsKey(p.getUUID())||AIR.containsKey(p.getUUID()))forget(p.getUUID());return;}
        var b=LakeHouse.base(p.server);
        if(b==null||p.isSpectator()||!LakeHouse.box(b).contains(p.position())){forget(p.getUUID());return;}
        breathe(p,b);light(p);
    }
    public static void breathe(ServerPlayer p,BlockPos b){
        var id=p.getUUID();int max=p.getMaxAirSupply();
        int out=OUT.getOrDefault(id,0);
        if(out>0){out--;if(out==0)OUT.remove(id);else OUT.put(id,out);if(out==WAKE)wake(p,b);p.setAirSupply(AIR.getOrDefault(id,0));return;}
        if(p.getAbilities().invulnerable){AIR.remove(id);CHOKING.remove(id);return;}
        int air=AIR.getOrDefault(id,p.getAirSupply());
        var source=source(p,b);
        air=switch(source){case AIR->Math.min(max,air+4);case HOSE,TANKS->Math.min(max,air+2);case NONE->Math.max(0,air-1);};
        AIR.put(id,air);p.setAirSupply(air);
        if(air>0){recover(p);return;}
        choke(p,b);
    }
    /** What the reader is breathing this tick. Tanks spend a tick of their fill when they are what keeps the reader going. */
    static Source source(ServerPlayer p,BlockPos b){
        var eye=BlockPos.containing(p.getX(),p.getEyeY(),p.getZ());
        if(!p.isEyeInFluid(FluidTags.WATER)||p.level().getBlockState(eye).is(Blocks.BUBBLE_COLUMN))return Source.AIR;
        if(LakeHouseGear.mine(p.getItemBySlot(EquipmentSlot.HEAD),p,LakeHouseRegistry.HELMET.get())&&running(p.serverLevel(),b)&&!HOSE_BLOCKED.contains(p.getUUID()))return Source.HOSE;
        var tanks=p.getItemBySlot(EquipmentSlot.CHEST);
        if(LakeHouseGear.mine(tanks,p,LakeHouseRegistry.TANKS.get())&&tanks.getDamageValue()<tanks.getMaxDamage()-1){
            tanks.setDamageValue(tanks.getDamageValue()+1);
            if(tanks.getMaxDamage()-tanks.getDamageValue()==TANK_AIR/4)p.displayClientMessage(Component.literal("The needle on the tanks is in the red."),true);
            if(tanks.getDamageValue()>=tanks.getMaxDamage()-1)p.displayClientMessage(Component.literal("The tanks are empty."),true);
            return Source.TANKS;
        }
        return Source.NONE;
    }
    /** No air left: a few seconds of the chest locking, heard by anyone near, then the blackout. */
    private static void choke(ServerPlayer p,BlockPos b){
        var id=p.getUUID();int choke=CHOKING.getOrDefault(id,0)+1;CHOKING.put(id,choke);
        if(choke==1)p.displayClientMessage(Component.literal("Your chest locks. You need air, now."),true);
        if(choke%30==1)p.serverLevel().playSound(null,p.getX(),p.getY(),p.getZ(),LabyrinthRegistry.CAVER_CHOKE.value(),SoundSource.PLAYERS,.7F,.9F+p.getRandom().nextFloat()*.1F);
        if(choke%6==0)p.serverLevel().sendParticles(ParticleTypes.BUBBLE,p.getX(),p.getEyeY(),p.getZ(),3,.15,.1,.15,.02);
        if(choke<CHOKE)return;
        CHOKING.remove(id);OUT.put(id,BLACKOUT);
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,BLACKOUT+30,0,false,false,false));
        p.displayClientMessage(Component.literal("The lake goes black."),true);
    }
    /** Air again before the blackout: the choking ends in a gasp. */
    private static void recover(ServerPlayer p){
        if(CHOKING.remove(p.getUUID())==null)return;
        p.serverLevel().playSound(null,p.getX(),p.getY(),p.getZ(),LabyrinthRegistry.CAVER_GASP.value(),SoundSource.PLAYERS,.75F,1.1F);
        p.displayClientMessage(Component.literal("Air. You drag it in."),true);
    }
    /** Coming to on the raft, or on the dock for a reader who never got through the culvert, with nothing lost but time. */
    private static void wake(ServerPlayer p,BlockPos b){
        boolean third=p.getZ()<b.getZ()+LakeHouse.CHANNEL_NORTH;
        var at=Vec3.atBottomCenterOf(b.offset(third?LakeHouse.RAFT.offset(1,1,0):LakeHouse.DOCK_END.offset(0,1,2)));
        p.stopRiding();p.teleportTo(p.serverLevel(),at.x,at.y,at.z,p.getYRot(),20);p.setDeltaMovement(Vec3.ZERO);p.resetFallDistance();
        AIR.put(p.getUUID(),p.getMaxAirSupply()/2);p.setAirSupply(p.getMaxAirSupply()/2);
        HOSE_BLOCKED.remove(p.getUUID());
        p.serverLevel().playSound(null,p.getX(),p.getY(),p.getZ(),LabyrinthRegistry.CAVER_GASP.value(),SoundSource.PLAYERS,.9F,1F);
        p.displayClientMessage(Component.literal(third?"You come to on the raft, coughing up lake water.":"You come to on the dock, coughing up lake water."),false);
    }
    /** For tests and operators: the reader's kept air in the scene, if any. */
    public static @Nullable Integer air(UUID id){return AIR.get(id);}
    public static void setAir(ServerPlayer p,int air){AIR.put(p.getUUID(),air);p.setAirSupply(air);}
    public static boolean choking(UUID id){return CHOKING.containsKey(id);}
    public static boolean blackedOut(UUID id){return OUT.containsKey(id);}

    // ------------------------------------------------------------------------------------------------ the compressor and hose
    /** The compressor on the raft runs while it is minded. */
    public static boolean running(ServerLevel l,BlockPos b){
        var s=l.getBlockState(b.offset(LakeHouse.COMPRESSOR));return s.is(LakeHouseRegistry.COMPRESSOR.get())&&s.getValue(LakeHouseCompressorBlock.RUNNING);
    }
    /**
     * Whether anyone minds the compressor: a reader near it with their head out of the water, or, for a reader diving alone,
     * Tess. With two or more readers and nobody at the compressor, it stops.
     */
    static boolean minded(ServerLevel l,BlockPos b){
        var at=b.offset(LakeHouse.COMPRESSOR).getCenter();int readers=0;boolean diver=false;
        for(var p:l.getEntitiesOfClass(ServerPlayer.class,LakeHouse.box(b),q->!q.isSpectator()&&q.isAlive())){
            readers++;
            if(p.distanceToSqr(at)<4.5*4.5&&!p.isEyeInFluid(FluidTags.WATER))return true;
            if(LakeHouseGear.mine(p.getItemBySlot(EquipmentSlot.HEAD),p,LakeHouseRegistry.HELMET.get()))diver=true;
        }
        return readers==1&&diver;
    }
    /** Every ten ticks: start or stop the compressor, and its engine's sound and exhaust while it runs. */
    public static void compressor(ServerLevel l,BlockPos b,long now){
        var at=b.offset(LakeHouse.COMPRESSOR);var s=l.getBlockState(at);if(!s.is(LakeHouseRegistry.COMPRESSOR.get()))return;
        boolean run=minded(l,b);
        if(run!=s.getValue(LakeHouseCompressorBlock.RUNNING))l.setBlock(at,s.setValue(LakeHouseCompressorBlock.RUNNING,run),Block.UPDATE_CLIENTS);
        if(!run)return;
        if(now%20<10)l.playSound(null,at,LakeHouseRegistry.COMPRESSOR_SOUND.get(),SoundSource.BLOCKS,.6F,1F);
        l.sendParticles(ParticleTypes.SMOKE,at.getX()+.5,at.getY()+.9,at.getZ()+.5,2,.05,.05,.05,.01);
    }
    /**
     * The diver's hose, every five ticks while they wear the helmet: drawn from the compressor down to them, pulled up short at
     * its reach, and caught once on the step's rail the first time they go in on the helmet visit, until they swim back and
     * free it.
     */
    static void hose(ServerPlayer p,BlockPos b,CompoundTag own){
        var id=p.getUUID();var l=p.serverLevel();
        if(!LakeHouseGear.mine(p.getItemBySlot(EquipmentSlot.HEAD),p,LakeHouseRegistry.HELMET.get())){HOSE_BLOCKED.remove(id);return;}
        var top=Vec3.atCenterOf(b.offset(LakeHouse.COMPRESSOR)).add(0,.3,0);var edge=Vec3.atCenterOf(b.offset(LakeHouse.COMPRESSOR.offset(-1,-1,0)));
        var chest=p.position().add(0,p.getBbHeight()*.7,0);
        boolean inside=inHouse(p.position().subtract(Vec3.atLowerCornerOf(b)));
        int snag=own.getInt("HoseSnag");
        if(snag==0&&inside&&own.getInt("Visit")==2){snag=1;own.putInt("HoseSnag",1);p.displayClientMessage(Component.literal("The hose pulls tight behind you. It has caught on something outside."),true);}
        Vec3 rail=null;
        if(snag==1){
            for(var r:RAILS){var at=Vec3.atCenterOf(b.offset(r));if(rail==null||at.distanceToSqr(chest)<rail.distanceToSqr(chest))rail=at;}
            if(rail.distanceToSqr(chest)<1.8*1.8){snag=2;own.putInt("HoseSnag",2);rail=null;p.displayClientMessage(Component.literal("You work the hose free of the rail."),true);}
        }
        double reach=chest.distanceTo(top);
        boolean blocked=snag==1;
        if(reach>HOSE){
            blocked=true;
            var pull=top.subtract(chest).normalize().scale(Math.min(.35,(reach-HOSE)*.15+.08));
            p.setDeltaMovement(p.getDeltaMovement().add(pull));p.hurtMarked=true;
            if(p.tickCount%40<5)p.displayClientMessage(Component.literal("The hose won't give any more."),true);
        }
        if(blocked)HOSE_BLOCKED.add(id);else HOSE_BLOCKED.remove(id);
        // The hose itself, seen by everyone nearby: compressor, over the raft's edge, down to the diver (by way of the rail if caught).
        line(l,top,edge);if(rail!=null){line(l,edge,rail);line(l,rail,chest);}else line(l,edge,chest);
    }
    private static void line(ServerLevel l,Vec3 from,Vec3 to){
        double length=from.distanceTo(to);int points=Math.max(2,Math.min(40,(int)(length*1.2)));
        for(int i=0;i<=points;i++){double t=i/(double)points;var at=from.lerp(to,t).add(0,-Math.sin(t*Math.PI)*Math.min(2.5,length*.08),0);
            l.sendParticles(HOSE_DUST,at.x,at.y,at.z,1,0,0,0,0);}
    }
    /** Inside the house's walls, either storey. Relative to the scene's base. */
    public static boolean inHouse(Vec3 r){
        return Math.abs(r.x-.5)<LakeHouse.HX&&r.z>LakeHouse.HZ_BACK+1&&r.z<LakeHouse.HZ_FRONT&&r.y>LakeHouse.GROUND&&r.y<LakeHouse.EAVES;
    }
    /** The compressor fills a reader's lent tanks, worn or carried. */
    static void refill(ServerPlayer p,BlockPos at){
        var b=LakeHouse.base(p.server);if(b==null||!b.offset(LakeHouse.COMPRESSOR).equals(at))return;
        boolean filled=false;var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(LakeHouseGear.mine(s,p,LakeHouseRegistry.TANKS.get())&&s.getDamageValue()>0){s.setDamageValue(0);filled=true;}}
        if(filled){p.serverLevel().playSound(null,at,LakeHouseRegistry.HISS.get(),SoundSource.BLOCKS,.8F,1F);p.displayClientMessage(Component.literal("Air hisses into the tanks until the needle stops."),true);}
        else p.displayClientMessage(Component.literal(running(p.serverLevel(),b)?"The compressor chugs. It runs while somebody minds it.":"The compressor runs while somebody minds it."),true);
    }

    // ------------------------------------------------------------------------------------------------ the flashlight
    /** The lent flashlight: a light where the beam lands, moved as it swings. */
    public static void light(ServerPlayer p){
        var id=p.getUUID();var old=LIGHTS.get(id);
        boolean holding=LakeHouseGear.mine(p.getMainHandItem(),p,LakeHouseRegistry.FLASHLIGHT.get())||LakeHouseGear.mine(p.getOffhandItem(),p,LakeHouseRegistry.FLASHLIGHT.get());
        if(!holding||OUT.containsKey(id)||LakeHouse.flashlightOut(p)){if(old!=null){restore(old);LIGHTS.remove(id);}return;}
        if(old!=null&&p.tickCount%2!=0)return;
        var l=p.serverLevel();var eye=p.getEyePosition();var end=eye.add(p.getLookAngle().scale(10));
        var hit=l.clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        var cell=hit.getType()==HitResult.Type.BLOCK?hit.getBlockPos().relative(hit.getDirection()):BlockPos.containing(end);
        if(old!=null&&old.at().equals(cell))return;
        var there=l.getBlockState(cell);boolean water=there.is(Blocks.WATER)&&there.getFluidState().isSource();
        if(!water&&!there.isAir())return;
        if(old!=null)restore(old);
        l.setBlock(cell,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,13).setValue(LightBlock.WATERLOGGED,water),Block.UPDATE_CLIENTS);
        LIGHTS.put(id,new Light(l,cell,water));
    }
    private static void restore(Light light){
        var l=light.level();if(!l.hasChunkAt(light.at())||!l.getBlockState(light.at()).is(Blocks.LIGHT))return;
        for(var other:LIGHTS.values())if(other!=light&&other.level()==l&&other.at().equals(light.at()))return;
        l.setBlock(light.at(),light.water()?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
    }
    /** For tests: where the reader's beam has put its light. */
    public static @Nullable BlockPos lightAt(UUID id){var light=LIGHTS.get(id);return light==null?null:light.at();}

    // ------------------------------------------------------------------------------------------------ leaving
    static void forget(UUID id){
        AIR.remove(id);CHOKING.remove(id);OUT.remove(id);HOSE_BLOCKED.remove(id);
        var light=LIGHTS.remove(id);if(light!=null)restore(light);
    }
    @SubscribeEvent public static void left(PlayerEvent.PlayerLoggedOutEvent e){forget(e.getEntity().getUUID());}
    @SubscribeEvent public static void stopping(ServerStoppingEvent e){
        for(var light:new java.util.ArrayList<>(LIGHTS.values()))restore(light);
        LIGHTS.clear();AIR.clear();CHOKING.clear();OUT.clear();HOSE_BLOCKED.clear();
    }
}
