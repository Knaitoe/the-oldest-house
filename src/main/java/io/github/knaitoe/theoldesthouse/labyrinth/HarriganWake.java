package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Quiet, persistent wake guests; talking never confers phone custody or Witness progress. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HarriganWake {
    public static final String TAG=TheOldestHouse.MOD_ID+"_harrigan_mourner0488";
    private static final BlockPos[] SPOTS={new BlockPos(-4,0,-17),new BlockPos(4,0,-17),new BlockPos(4,0,-23)};
    private static final String[] WORDS={"\"You read to him, didn't you?\"","\"I keep thinking he'll ask us to be quiet.\"","\"I don't know what to do with my hands.\""};
    private HarriganWake(){}
    public static void ensureCast(ServerLevel level,BlockPos base){
        var data=LabyrinthData.get(level.getServer());var state=data.state(HarriganVignette.ID);
        if(state.getInt("Visit")!=2)return;
        for(int role=0;role<SPOTS.length;role++){
            String key="Mourner0488_"+role;if(state.hasUUID(key))continue;
            var pos=base.offset(SPOTS[role]);var chunk=new ChunkPos(pos);
            if(!level.hasChunkAt(pos)||!level.areEntitiesLoaded(chunk.toLong())||HouseWatchers.isWatched(level,Vec3.atCenterOf(pos).add(0,1,0)))continue;
            // Migrate any original loaded guest before considering a fresh actor.
            int variant=4+role;var originals=level.getEntitiesOfClass(ArmorStand.class,HarriganWake.bounds(base),a->a.getTags().contains(TAG)&&HarriganAppearance.variant(a)==variant);
            if(!originals.isEmpty()){state.putUUID(key,originals.getFirst().getUUID());continue;}
            var guest=new ArmorStand(level,pos.getX()+.5,pos.getY(),pos.getZ()+.5);guest.setInvisible(true);guest.setNoGravity(true);guest.setNoBasePlate(true);guest.setShowArms(true);guest.setInvulnerable(true);guest.addTag(TAG);
            guest.setYRot(role==0?-35:role==1?35:180);guest.setHeadPose(new Rotations(10,0,0));
            guest.setLeftArmPose(new Rotations(-18,0,-9));guest.setRightArmPose(new Rotations(-18,0,9));
            ItemStack head=new ItemStack(Items.PLAYER_HEAD);CustomData.update(DataComponents.CUSTOM_DATA,head,tag->tag.putInt(HarriganAppearance.KEY,variant));guest.setItemSlot(EquipmentSlot.HEAD,head);
            if(level.addFreshEntity(guest))state.putUUID(key,guest.getUUID());
        }
        data.setState(HarriganVignette.ID,state);
    }
    public static AABB bounds(BlockPos base){return new AABB(base.getX()-8,base.getY()-2,base.getZ()-25,base.getX()+9,base.getY()+7,base.getZ()+3);}
    @SubscribeEvent(priority=EventPriority.HIGH) public static void talk(PlayerInteractEvent.EntityInteract event){if(event.isCanceled()||!mourner(event,event.getTarget()))return;event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);talk(event,event.getTarget());}
    @SubscribeEvent(priority=EventPriority.HIGH) public static void talkAt(PlayerInteractEvent.EntityInteractSpecific event){if(event.isCanceled()||!mourner(event,event.getTarget()))return;event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);talk(event,event.getTarget());}
    private static boolean mourner(PlayerInteractEvent event,Entity target){return event.getHand()==InteractionHand.MAIN_HAND&&target instanceof ArmorStand guest&&guest.getTags().contains(TAG);}
    private static void talk(PlayerInteractEvent event,Entity target){
        if(!(event.getEntity() instanceof ServerPlayer player)||!(target instanceof ArmorStand guest))return;
        var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(player.server).houseOrigin();
        if(origin==null||!player.isAlive()||player.isSpectator()||!player.level().dimension().equals(io.github.knaitoe.theoldesthouse.house.HouseDimensions.INTERIOR)||!bounds(LabyrinthPlaces.base(origin,LabyrinthPlace.HARRIGAN)).contains(player.position())||player.distanceToSqr(guest)>16)return;
        int role=HarriganAppearance.variant(guest)-4;if(role<0||role>=WORDS.length)return;
        long now=player.level().getGameTime();String key="WakeReply0488";
        if(!player.getPersistentData().contains(key)||now-player.getPersistentData().getLong(key)>=60){player.getPersistentData().putLong(key,now);player.displayClientMessage(Component.literal(WORDS[role]),false);}
    }
}
