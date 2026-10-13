package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Three earned keepsakes. Their effects are outside the House, and work only for their original reader. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LakeHouseArtifactItem extends Item implements Equipable {
    public static final int FLASHLIGHT=0,LIGHT=1,HELMET=2;
    private final int kind;
    private record Beam(ServerLevel level,BlockPos at,BlockState original){}
    private static final Map<UUID,Beam> BEAMS=new HashMap<>();
    public LakeHouseArtifactItem(Properties p,int kind){super(p);this.kind=kind;}
    public int kind(){return kind;}
    @Override public EquipmentSlot getEquipmentSlot(){return kind==HELMET?EquipmentSlot.HEAD:EquipmentSlot.MAINHAND;}
    public static boolean owned(ItemStack s,Player p){var data=s.get(DataComponents.CUSTOM_DATA);return data!=null&&data.copyTag().hasUUID(LiteraryVignettes.OWNER)&&p.getUUID().equals(data.copyTag().getUUID(LiteraryVignettes.OWNER));}
    public static boolean helmet(Player p){var s=p.getItemBySlot(EquipmentSlot.HEAD);return s.is(LakeHouseRegistry.GOLD_HELMET.get())&&owned(s,p);}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){
        if(kind==HELMET&&owned(p.getItemInHand(hand),p))return swapWithEquipmentSlot(this,l,p,hand);
        if(p instanceof ServerPlayer r)r.displayClientMessage(Component.literal(owned(p.getItemInHand(hand),p)?kind==LIGHT?"Hang it where you want to come home.":"The beam doesn't falter, even under water.":"It was left for somebody else."),true);
        return InteractionResultHolder.sidedSuccess(p.getItemInHand(hand),l.isClientSide);
    }
    @Override public InteractionResult useOn(UseOnContext c){
        if(kind!=LIGHT)return super.useOn(c);var p=c.getPlayer();if(p==null||!owned(c.getItemInHand(),p))return InteractionResult.FAIL;
        if(c.getLevel().dimension()!=Level.OVERWORLD){if(p instanceof ServerPlayer r)r.displayClientMessage(Component.literal("This light belongs in the world you came from."),true);return InteractionResult.FAIL;}
        var at=c.getClickedPos().relative(c.getClickedFace());var l=c.getLevel();var existing=l.getBlockState(at);
        if(!existing.canBeReplaced()||!l.mayInteract(p,at)||!p.mayUseItemAt(at,c.getClickedFace(),c.getItemInHand()))return InteractionResult.FAIL;
        if(!l.isClientSide){var original=c.getItemInHand().copyWithCount(1);
            l.setBlock(at,LakeHouseRegistry.HOME_LIGHT.get().defaultBlockState().setValue(LakeHouseLightBlock.WATERLOGGED,existing.getFluidState().is(net.minecraft.tags.FluidTags.WATER)),Block.UPDATE_ALL);
            if(l.getBlockEntity(at) instanceof LakeHouseLightBlock.Lamp lamp){lamp.original(original);c.getItemInHand().shrink(1);if(p instanceof ServerPlayer r){var own=LiteraryVignettes.personal(LabyrinthData.get(r.server),r.getUUID(),LakeHouse.PLACE);own.putLong("HomeLight",at.asLong());LiteraryVignettes.save(LabyrinthData.get(r.server),r.getUUID(),LakeHouse.PLACE,own);r.displayClientMessage(Component.literal("You hang the light. This is where it will bring you home."),false);}}
        }return InteractionResult.sidedSuccess(l.isClientSide);
    }
    private static void restore(UUID id){var old=BEAMS.remove(id);if(old==null||!old.level().hasChunkAt(old.at())||!old.level().getBlockState(old.at()).is(Blocks.LIGHT))return;
        if(BEAMS.values().stream().anyMatch(b->b.level()==old.level()&&b.at().equals(old.at())))return;
        old.level().setBlock(old.at(),old.original(),Block.UPDATE_CLIENTS);
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var id=p.getUUID();
        if(!p.isAlive()||p.isSpectator()||HouseDimensions.isHouseDimension(p.level().dimension())){restore(id);return;}
        if(helmet(p)){p.setAirSupply(p.getMaxAirSupply());if(p.tickCount%1800==0)ThreeFingers.hear(p,net.minecraft.sounds.SoundEvents.WOOD_STEP,p.blockPosition().above(3),.18F,.65F);}
        boolean holding=List.of(p.getMainHandItem(),p.getOffhandItem()).stream().anyMatch(s->s.is(LakeHouseRegistry.FOUND_FLASHLIGHT.get())&&owned(s,p));
        if(!holding){restore(id);return;}if(p.tickCount%4!=0)return;
        var l=p.serverLevel();var eye=p.getEyePosition();var end=eye.add(p.getLookAngle().scale(12));var hit=l.clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        var at=hit.getType()==HitResult.Type.BLOCK?hit.getBlockPos().relative(hit.getDirection()):BlockPos.containing(end);if(!l.hasChunkAt(at))return;
        var previous=BEAMS.get(id);if(previous!=null&&previous.level()==l&&previous.at().equals(at))return;
        var there=l.getBlockState(at);boolean water=there.is(Blocks.WATER)&&there.getFluidState().isSource();
        if(!there.isAir()&&!water&&!there.is(Blocks.LIGHT))return;
        if(there.is(Blocks.LIGHT)){var same=BEAMS.values().stream().filter(b->b.level()==l&&b.at().equals(at)).findFirst().orElse(null);if(same==null)return;there=same.original();}
        restore(id);l.setBlock(at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,14).setValue(LightBlock.WATERLOGGED,water||there.is(Blocks.WATER)),Block.UPDATE_CLIENTS);BEAMS.put(id,new Beam(l,at,there));
        if(p.tickCount%1200==0){var edge=p.getEyePosition().add(p.getLookAngle().scale(10)).add(2,0,0);l.sendParticles(p,net.minecraft.core.particles.ParticleTypes.SMOKE,true,edge.x,edge.y,edge.z,2,.1,.3,.1,0);}
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){restore(e.getEntity().getUUID());}
    @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var id:new ArrayList<>(BEAMS.keySet()))restore(id);}
}
