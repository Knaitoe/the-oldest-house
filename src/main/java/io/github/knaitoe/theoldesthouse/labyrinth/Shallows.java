package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Verb: throwing. The memory is personal; crossing its threshold alone never completes it. */
public final class Shallows {
    public static final String ID="shallows", BOY="HouseShallowsBoy", WORDS="HouseLakeWords";
    private static final String OWNER="ShallowsOwner", ACTOR="ShallowsActor";
    private Shallows() {}
    public static void build(MinecraftServer server,ServerLevel level,BlockPos base){
        IndianLakeArchitecture.shallows(level,base);LabyrinthData data=LabyrinthData.get(server);CompoundTag state=data.state(ID);
        state.putBoolean("Built",true);data.setState(ID,state);boys(level,base);
    }
    private static void boys(ServerLevel level,BlockPos base){
        var all=level.getEntitiesOfClass(LakeCongregantEntity.class,IndianLakeRooms.bounds(base,LabyrinthPlace.SHALLOWS),e->e.getTags().contains(BOY));
        for(int i=0;i<3;i++){
            String tag=BOY+i;if(all.stream().anyMatch(b->b.getTags().contains(tag)))continue;
            var boy=DrownedTownRegistry.CONGREGANT.get().create(level);if(boy==null)continue;
            boy.pose(false,false);boy.memoryBoy(true);boy.addTag(BOY);boy.addTag(tag);
            boy.moveTo(base.getX()+new int[]{-4,5,-3}[i]+.5,base.getY(),base.getZ()+new int[]{-10,-11,-5}[i]+.5,180,0);level.addFreshEntity(boy);
        }
    }
    public static CompoundTag personal(LabyrinthData data,UUID owner){return data.state(ID).getCompound("Players").getCompound(owner.toString()).copy();}
    private static void save(LabyrinthData data,UUID owner,CompoundTag record){
        CompoundTag state=data.state(ID),players=state.getCompound("Players");players.put(owner.toString(),record);state.put("Players",players);data.setState(ID,state);
    }
    public static @Nullable LakeWitchEntity actor(ServerPlayer player){
        CompoundTag record=personal(LabyrinthData.get(player.server),player.getUUID());
        if(!record.hasUUID("Actor"))return null;
        ServerLevel level=player.server.getLevel(HouseDimensions.INTERIOR);if(level==null)return null;
        Entity actor=level.getEntity(record.getUUID("Actor"));
        return actor instanceof LakeWitchEntity witch&&player.getUUID().equals(witch.memoryOwner())?witch:null;
    }
    public static void onArrive(ServerPlayer player,LabyrinthPlace place){
        if(place!=LabyrinthPlace.SHALLOWS)return;LabyrinthData data=LabyrinthData.get(player.server);
        if(!IndianLakeProgress.canDealShallows(data,player.getUUID()))return;
        BlockPos base=IndianLakeRooms.base(player.server,place);if(base==null)return;
        IndianLakeRooms.keepLoaded(player.serverLevel(),base,place);boys(player.serverLevel(),base);
        CompoundTag record=personal(data,player.getUUID());record.putBoolean("Cold",false);record.putBoolean("Active",true);
        // Reconnects reuse their saved actor; a new visit cannot duplicate a still-loaded recollection.
        LakeWitchEntity girl=actor(player);
        if(girl==null){
            girl=DrownedTownRegistry.LAKE_WITCH.get().create(player.serverLevel());if(girl==null)return;
            girl.recollection(player.getUUID(),base);girl.moveTo(Vec3.atBottomCenterOf(base.offset(2,0,-8)));
            player.serverLevel().addFreshEntity(girl);record.putUUID("Actor",girl.getUUID());
        }
        save(data,player.getUUID(),record);words(player,base.offset(-4,2,-10),"Take her out further. Both hands.");
        IndianLakeArchitecture.light(player.serverLevel(),base.offset(2,3,-8),8);IndianLakeArchitecture.light(player.serverLevel(),base.offset(0,2,-13),6);
    }
    public static boolean lift(ServerPlayer player,LakeWitchEntity girl){
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag record=personal(data,player.getUUID());
        if(!IndianLakeProgress.canDealShallows(data,player.getUUID())||!IndianLakeRooms.inside(player,LabyrinthPlace.SHALLOWS)
                ||!player.getUUID().equals(girl.memoryOwner())||player.distanceToSqr(girl)>9||girl.memoryPhase()>1
                ||record.getBoolean("Cold")||!record.getBoolean("Active"))return false;
        if(!player.getMainHandItem().isEmpty()||!player.getOffhandItem().isEmpty()){
            words(player,girl.blockPosition().above(2),"Both hands. Put what you're holding away.");return false;
        }
        ItemStack burden=new ItemStack(DrownedTownRegistry.SHALLOWS_BURDEN.get());
        CustomData.update(DataComponents.CUSTOM_DATA,burden,t->{t.putUUID(OWNER,player.getUUID());t.putUUID(ACTOR,girl.getUUID());});
        player.setItemInHand(InteractionHand.MAIN_HAND,burden);girl.memoryPhase(1);girl.setNoGravity(true);
        player.inventoryMenu.broadcastChanges();words(player,girl.blockPosition().above(2),"The water. Carry her to its edge. Hold use, then let go.");return true;
    }
    public static boolean validBurden(ServerPlayer player,ItemStack stack){
        if(!stack.is(DrownedTownRegistry.SHALLOWS_BURDEN.get()))return false;
        CustomData custom=stack.get(DataComponents.CUSTOM_DATA);if(custom==null)return false;CompoundTag t=custom.copyTag();
        LakeWitchEntity girl=actor(player);
        return t.hasUUID(OWNER)&&player.getUUID().equals(t.getUUID(OWNER))&&t.hasUUID(ACTOR)&&girl!=null
                &&girl.getUUID().equals(t.getUUID(ACTOR))&&girl.memoryPhase()==1
                &&IndianLakeRooms.inside(player,LabyrinthPlace.SHALLOWS)
                &&!personal(LabyrinthData.get(player.server),player.getUUID()).getBoolean("Cold");
    }
    public static boolean throwGirl(ServerPlayer player,ItemStack burden){
        if(!validBurden(player,burden))return false;
        BlockPos base=IndianLakeRooms.base(player.server,LabyrinthPlace.SHALLOWS);Vec3 toward=player.getLookAngle().multiply(1,0,1).normalize();
        if(player.getZ()>base.getZ()-11||player.getZ()<base.getZ()-14||Math.abs(player.getX()-base.getX())>12
                ||toward.z>-.7||player.isInWaterOrBubble()){
            words(player,player.blockPosition().above(2),"At the bank. Face the open water.");return false;
        }
        LakeWitchEntity girl=actor(player);girl.memoryPhase(2);girl.setNoGravity(false);
        girl.setDeltaMovement(toward.scale(.34).add(0,.22,0));girl.hurtMarked=true;removeBurdens(player);
        words(player,base.offset(5,2,-11),"Let go.");return true;
    }
    /** Called after the actor's ordinary native physics tick, not a timer that assumes a splash. */
    public static void tickActor(LakeWitchEntity girl){
        if(!(girl.level() instanceof ServerLevel level)||girl.memoryOwner()==null)return;
        ServerPlayer owner=level.getServer().getPlayerList().getPlayer(girl.memoryOwner());
        // Native fake players used by server tests live in the level rather than the connection list.
        if(owner==null)owner=level.players().stream().filter(p->p.getUUID().equals(girl.memoryOwner())).findFirst().orElse(null);
        if(owner==null||!IndianLakeRooms.inside(owner,LabyrinthPlace.SHALLOWS)){
            // Interrupted recollections are retryable, and never count a throw after their owner leaves.
            girl.memoryPhase(0);girl.setNoGravity(true);girl.setDeltaMovement(Vec3.ZERO);girl.moveTo(Vec3.atBottomCenterOf(girl.memoryBase().offset(2,0,-8)));return;
        }
        if(girl.memoryPhase()==1){
            if(!hasBurden(owner)){girl.memoryPhase(0);girl.setNoGravity(false);girl.moveTo(Vec3.atBottomCenterOf(girl.memoryBase().offset(2,0,-8)));return;}
            Vec3 front=owner.getLookAngle().multiply(1,0,1).normalize();Vec3 at=owner.position().add(front.scale(.9)).add(0,.65,0);
            girl.moveTo(at.x,at.y,at.z,owner.getYRot()+90,0);girl.setDeltaMovement(Vec3.ZERO);
        }else if(girl.memoryPhase()==2&&girl.getZ()<=girl.memoryBase().getZ()-14&&girl.getY()<girl.memoryBase().getY()+.65
                &&level.getFluidState(girl.blockPosition()).is(FluidTags.WATER)){
            IndianLakeProgress.completedShallows(LabyrinthData.get(level.getServer()),owner.getUUID(),owner.getGameProfile().getName());
            WitnessAccount.resolve(owner,WitnessAccount.Story.SHALLOWS,"threw_her");
            girl.memoryPhase(3);girl.setNoGravity(true);girl.setDeltaMovement(0,-.045,0);
            level.playSound(null,girl.blockPosition(),SoundEvents.GENERIC_SPLASH,SoundSource.PLAYERS,1.2F,.7F);
            updateEssays(owner);words(owner,girl.memoryBase().offset(-4,2,-10),"That was all of us.");
        }else if(girl.memoryPhase()==3){
            girl.move(MoverType.SELF,new Vec3(0,-.045,0));if(girl.getY()<girl.memoryBase().getY()-2.5)girl.discard();
        }
    }
    private static boolean hasBurden(ServerPlayer player){
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(validBurden(player,player.getInventory().getItem(i)))return true;
        return false;
    }
    public static void removeBurdens(ServerPlayer player){
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(DrownedTownRegistry.SHALLOWS_BURDEN.get()))player.getInventory().setItem(i,ItemStack.EMPTY);
        if(player.containerMenu.getCarried().is(DrownedTownRegistry.SHALLOWS_BURDEN.get()))player.containerMenu.setCarried(ItemStack.EMPTY);
        player.inventoryMenu.broadcastChanges();
    }
    private static void updateEssays(ServerPlayer player){
        for(int i=0;i<player.getInventory().getContainerSize();i++){
            ItemStack stack=player.getInventory().getItem(i);if(stack.getItem() instanceof DrownedEssayItem essay)essay.refresh(player,stack);
        }
        player.inventoryMenu.broadcastChanges();
    }
    public static void onDepart(ServerPlayer player){
        LakeWitchEntity girl=actor(player);if(girl!=null)girl.discard();removeBurdens(player);
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag record=personal(data,player.getUUID());record.putBoolean("Active",false);record.remove("Actor");save(data,player.getUUID(),record);
    }
    public static void onToss(ItemTossEvent event){
        if(!(event.getPlayer() instanceof ServerPlayer player)||!event.getEntity().getItem().is(DrownedTownRegistry.SHALLOWS_BURDEN.get()))return;
        event.setCanceled(true);event.getEntity().discard();LakeWitchEntity girl=actor(player);
        if(girl!=null){girl.memoryPhase(0);girl.setNoGravity(false);girl.moveTo(Vec3.atBottomCenterOf(girl.memoryBase().offset(2,0,-8)));}
        removeBurdens(player);
    }
    public static void onAttack(AttackEntityEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)||!IndianLakeRooms.inside(player,LabyrinthPlace.SHALLOWS))return;
        if(!(event.getTarget() instanceof LakeWitchEntity w&&w.memory())&&!event.getTarget().getTags().contains(BOY))return;
        event.setCanceled(true);LabyrinthData data=LabyrinthData.get(player.server);CompoundTag record=personal(data,player.getUUID());record.putBoolean("Cold",true);save(data,player.getUUID(),record);
        LakeWitchEntity girl=actor(player);if(girl!=null)girl.discard();removeBurdens(player);
        BlockPos base=IndianLakeRooms.base(player.server,LabyrinthPlace.SHALLOWS);IndianLakeArchitecture.light(player.serverLevel(),base.offset(2,3,-8),0);IndianLakeArchitecture.light(player.serverLevel(),base.offset(0,2,-13),0);
    }
    public static void words(ServerPlayer player,BlockPos at,String text){
        var display=EntityType.TEXT_DISPLAY.create(player.serverLevel());if(display==null)return;
        CompoundTag tag=new CompoundTag();display.saveWithoutId(tag);tag.putString("text",Component.Serializer.toJson(Component.literal(text),player.registryAccess()));
        tag.putString("billboard","center");tag.putInt("line_width",210);tag.putInt("background",0x44000000);tag.putBoolean("see_through",false);display.load(tag);
        display.moveTo(Vec3.atCenterOf(at));display.addTag(WORDS);display.getPersistentData().putLong("Until",player.serverLevel().getGameTime()+120);player.serverLevel().addFreshEntity(display);
    }
    public static void onServerTick(ServerTickEvent.Post event){
        if(event.getServer().getTickCount()%20!=0)return;ServerLevel level=event.getServer().getLevel(HouseDimensions.INTERIOR);if(level==null)return;
        BlockPos base=IndianLakeRooms.base(event.getServer(),LabyrinthPlace.SHALLOWS);
        if(base!=null){
            var visitors=IndianLakeRooms.visitors(level,base,LabyrinthPlace.SHALLOWS);
            if(!visitors.isEmpty())IndianLakeRooms.keepLoaded(level,base,LabyrinthPlace.SHALLOWS);
            Set<ServerPlayer> players=new HashSet<>(event.getServer().getPlayerList().getPlayers());players.addAll(level.players());
            for(ServerPlayer player:players){
                if(!IndianLakeRooms.inside(player,LabyrinthPlace.SHALLOWS)&&personal(LabyrinthData.get(player.server),player.getUUID()).getBoolean("Active"))onDepart(player);
                for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(DrownedTownRegistry.SHALLOWS_BURDEN.get())&&!validBurden(player,player.getInventory().getItem(i)))player.getInventory().setItem(i,ItemStack.EMPTY);
            }
        }
        for(Entity e:level.getAllEntities())if(e.getTags().contains(WORDS)&&e.getPersistentData().getLong("Until")<=level.getGameTime())e.discard();
    }
    public static boolean reset(ServerPlayer player){onDepart(player);LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=data.state("indian_lake"),boys=state.getCompound("Boys");boys.remove(player.getUUID().toString());state.put("Boys",boys);data.setState("indian_lake",state);save(data,player.getUUID(),new CompoundTag());return true;}
}
