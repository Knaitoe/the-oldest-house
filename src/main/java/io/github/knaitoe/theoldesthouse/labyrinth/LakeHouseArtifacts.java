package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
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
 * What a reader brings back from the lake house, one for each ending, each with a use outside the House only (for now).
 * <ul>
 * <li><b>The flashlight that found you</b> (said hello): a light that works anywhere, under water too, and never runs out.
 * Once in a while its beam catches something at the edge of the dark.</li>
 * <li><b>The house light</b> (left the lights on): see {@link LakeHouseLightBlock}.</li>
 * <li><b>The gold helmet</b> (stayed a little longer): unlimited breath under water, but no armour, muffled hearing and a
 * narrower view (drawn by the client). Sometimes the house creaks somewhere above.</li>
 * </ul>
 * Each is marked with its reader when it is given, and can be offered to the Mother like any artifact.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LakeHouseArtifacts {
    private record Beam(ServerLevel level,BlockPos at,boolean water){}
    private static final Map<UUID,Beam> BEAMS=new HashMap<>();
    private static final DustParticleOptions EYES=new DustParticleOptions(new Vector3f(.85F,.9F,.8F),.5F);
    private LakeHouseArtifacts(){}

    /** The gold helmet: worn on the head, like the diving helmet it was. */
    public static final class Helmet extends Item implements Equipable {
        public Helmet(Properties properties){super(properties);}
        @Override public EquipmentSlot getEquipmentSlot(){return EquipmentSlot.HEAD;}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){return swapWithEquipmentSlot(this,level,player,hand);}
    }
    public static boolean wearsGoldHelmet(Player p){return p.getItemBySlot(EquipmentSlot.HEAD).is(LakeHouseRegistry.GOLD_HELMET.get());}
    static boolean holds(Player p,Item item){return p.getMainHandItem().is(item)||p.getOffhandItem().is(item);}

    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        boolean outside=!HouseDimensions.isHouseDimension(p.level().dimension())&&!p.isSpectator();
        // The flashlight that found you.
        if(outside&&holds(p,LakeHouseRegistry.FOUND_FLASHLIGHT.get()))beam(p);else end(p.getUUID());
        // The gold helmet.
        if(outside&&wearsGoldHelmet(p)){
            var breath=p.getEffect(MobEffects.WATER_BREATHING);
            if(breath==null||breath.getDuration()<220)p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,300,0,true,false,true));
            if(p.tickCount%2400==1200&&p.getRandom().nextInt(3)==0)LakeHouseRules.hear(p,SoundEvents.WOOD_STEP,p.blockPosition().above(6),.5F,.6F);
        }
    }
    /** A light where the beam lands, moved as it swings; it never runs out. Sometimes something at the edge of it looks back. */
    private static void beam(ServerPlayer p){
        var id=p.getUUID();var old=BEAMS.get(id);if(old!=null&&old.level()!=p.serverLevel()){end(id);old=null;}
        if(old!=null&&p.tickCount%2!=0)return;
        var l=p.serverLevel();var eye=p.getEyePosition();var end=eye.add(p.getLookAngle().scale(14));
        var hit=l.clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        var cell=hit.getType()==HitResult.Type.BLOCK?hit.getBlockPos().relative(hit.getDirection()):BlockPos.containing(end);
        if(p.tickCount%3600==1800&&hit.getLocation().distanceTo(eye)>9&&l.getMaxLocalRawBrightness(cell)<4&&p.getRandom().nextInt(2)==0){
            var at=hit.getLocation().add(p.getLookAngle().scale(-.4));var side=p.getLookAngle().cross(new Vec3(0,1,0)).normalize().scale(.12);
            for(int i=0;i<4;i++){l.sendParticles(p,EYES,true,at.x+side.x,at.y,at.z+side.z,1,0,0,0,0);l.sendParticles(p,EYES,true,at.x-side.x,at.y,at.z-side.z,1,0,0,0,0);}
        }
        if(old!=null&&old.at().equals(cell))return;
        var there=l.getBlockState(cell);boolean water=there.is(Blocks.WATER)&&there.getFluidState().isSource();
        if(!water&&!there.isAir())return;
        if(old!=null)restore(old);
        l.setBlock(cell,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,13).setValue(LightBlock.WATERLOGGED,water),Block.UPDATE_CLIENTS);
        BEAMS.put(id,new Beam(l,cell,water));
    }
    private static void end(UUID id){var old=BEAMS.remove(id);if(old!=null)restore(old);}
    private static void restore(Beam beam){
        var l=beam.level();if(!l.hasChunkAt(beam.at())||!l.getBlockState(beam.at()).is(Blocks.LIGHT))return;
        for(var other:BEAMS.values())if(other!=beam&&other.level()==l&&other.at().equals(beam.at()))return;
        l.setBlock(beam.at(),beam.water()?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
    }
    /** For tests: where the found flashlight's beam has put its light. */
    public static BlockPos beamAt(UUID id){var b=BEAMS.get(id);return b==null?null:b.at();}
    @SubscribeEvent public static void left(PlayerEvent.PlayerLoggedOutEvent e){end(e.getEntity().getUUID());}
    @SubscribeEvent public static void changed(PlayerEvent.PlayerChangedDimensionEvent e){end(e.getEntity().getUUID());}
    @SubscribeEvent public static void stopping(ServerStoppingEvent e){for(var b:new java.util.ArrayList<>(BEAMS.values()))restore(b);BEAMS.clear();}
}
