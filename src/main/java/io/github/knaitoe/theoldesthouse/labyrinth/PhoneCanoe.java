package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Verb: filming. Each explorer loses one original recording; the cave keeps that exact item. */
public final class PhoneCanoe {
    public static final String ID="phone_canoe",CANOE="HouseFilmingCanoe",CAMERA="HouseDroppedPhone",SCENE="LakePhoneScene",OWNER="LakePhoneOwner";
    public static final int FILM_TICKS=180,SKY_TICKS=140,FADE_TICKS=32,END=FILM_TICKS+SKY_TICKS+FADE_TICKS;
    public static final BlockPos DOCK=new BlockPos(0,0,-10),BOAT=new BlockPos(0,0,-13),STEEPLE=new BlockPos(0,-1,-37);
    private PhoneCanoe(){}
    public static CompoundTag personal(LabyrinthData data,UUID player){return data.state(ID).getCompound(player.toString()).copy();}
    private static void save(LabyrinthData data,UUID player,CompoundTag state){CompoundTag all=data.state(ID);all.put(player.toString(),state);data.setState(ID,all);}
    public static boolean canDeal(LabyrinthData data,UUID player){return !personal(data,player).getBoolean("Finished")&&!WitnessAccount.has(data,player,WitnessAccount.Story.PHONE_CANOE);}
    public static boolean bound(ServerPlayer player){return personal(LabyrinthData.get(player.server),player.getUUID()).getInt("Phase")>=3&&IndianLakeRooms.inside(player,LabyrinthPlace.PHONE_CANOE);}
    public static void build(net.minecraft.server.MinecraftServer server,ServerLevel level,BlockPos base){IndianLakeArchitecture.phone(level,base);stage(level,base);}
    public static LakeCanoeEntity stage(ServerLevel level,BlockPos base){
        var boats=level.getEntitiesOfClass(LakeCanoeEntity.class,IndianLakeRooms.bounds(base,LabyrinthPlace.PHONE_CANOE),b->b.getTags().contains(CANOE));
        if(!boats.isEmpty()){for(int i=1;i<boats.size();i++)boats.get(i).discard();boats.getFirst().mobile(true);return boats.getFirst();}
        LakeCanoeEntity boat=DrownedTownRegistry.CAVE_CANOE.get().create(level);if(boat==null)return null;
        boat.addTag(CANOE);boat.mobile(true);boat.moveTo(Vec3.atBottomCenterOf(base.offset(BOAT)));boat.setYRot(180);level.addFreshEntity(boat);return boat;
    }
    public static void onArrive(ServerPlayer player,LabyrinthPlace place){
        if(place!=LabyrinthPlace.PHONE_CANOE)return;BlockPos base=IndianLakeRooms.base(player.server,place);if(base==null)return;
        IndianLakeRooms.keepLoaded(player.serverLevel(),base,place);stage(player.serverLevel(),base);
        player.displayClientMessage(Component.literal("A canoe waits beside the jetty. Board with an empty hand, then use the phone to film."),false);
    }
    public static boolean board(ServerPlayer player,LakeCanoeEntity canoe){
        if(!IndianLakeRooms.inside(player,LabyrinthPlace.PHONE_CANOE)||!canoe.getTags().contains(CANOE)||player.distanceToSqr(canoe)>25)return false;
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=personal(data,player.getUUID());
        if(state.getBoolean("Finished")){player.displayClientMessage(Component.literal("The seat is wet. Your phone is somewhere else now."),true);return false;}
        if(!canoe.getPassengers().isEmpty()&&!canoe.hasPassenger(player)){player.displayClientMessage(Component.literal("Someone else is in the canoe."),true);return false;}
        if(state.getBoolean("Dropped")){
            if(!player.startRiding(canoe,true))return false;state.putInt("Phase",3);state.putInt("Tick",Math.max(FILM_TICKS,state.getInt("Tick")));save(data,player.getUUID(),state);return true;
        }
        if(!player.getMainHandItem().isEmpty()||BlindfoldItem.isWorn(player)){player.displayClientMessage(Component.literal("You need an empty hand to take the phone."),true);return false;}
        if(!player.startRiding(canoe,true))return false;
        UUID scene=UUID.randomUUID();ItemStack phone=new ItemStack(DrownedTownRegistry.LAKE_PHONE.get());
        phone.set(DataComponents.CUSTOM_NAME,Component.literal("Your phone · Indian Lake"));
        CustomData.update(DataComponents.CUSTOM_DATA,phone,t->{t.putUUID(OWNER,player.getUUID());t.putUUID(SCENE,scene);t.putString("Name",player.getGameProfile().getName());t.put("Footage",new ListTag());});
        player.setItemInHand(InteractionHand.MAIN_HAND,phone);state.putUUID(SCENE,scene);state.putInt("Phase",1);state.put("Item",phone.save(player.registryAccess()));save(data,player.getUUID(),state);
        canoe.fixedView(false);player.inventoryMenu.broadcastChanges();player.displayClientMessage(Component.literal("Use the phone, then row toward the drowned steeple. Crouch to stop filming and leave."),true);return true;
    }
    private static boolean original(ItemStack stack,UUID player,CompoundTag state){
        CustomData c=stack.get(DataComponents.CUSTOM_DATA);if(c==null||!state.hasUUID(SCENE))return false;CompoundTag t=c.copyTag();
        return stack.is(DrownedTownRegistry.LAKE_PHONE.get())&&t.hasUUID(OWNER)&&player.equals(t.getUUID(OWNER))&&t.hasUUID(SCENE)&&state.getUUID(SCENE).equals(t.getUUID(SCENE));
    }
    public static boolean beginFilm(ServerPlayer player,ItemStack phone){
        if(!IndianLakeRooms.inside(player,LabyrinthPlace.PHONE_CANOE)||!(player.getVehicle() instanceof LakeCanoeEntity canoe)||!canoe.getTags().contains(CANOE))return false;
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=personal(data,player.getUUID());
        if(state.getInt("Phase")!=1||!original(phone,player.getUUID(),state))return false;
        state.putInt("Phase",2);state.putInt("Tick",0);state.put("Item",phone.copyWithCount(1).save(player.registryAccess()));save(data,player.getUUID(),state);send(player,2,-1,0);
        sound(player,DrownedTownRegistry.PHONE_RECORD.get(),player.position(),.65F);player.displayClientMessage(Component.literal("Recording. Row over the steeple in the dark water ahead."),true);return true;
    }
    private static ItemStack scenePhone(ServerPlayer player,CompoundTag state){
        for(int i=0;i<player.getInventory().getContainerSize();i++){ItemStack item=player.getInventory().getItem(i);if(original(item,player.getUUID(),state))return item;}
        ItemStack carried=player.containerMenu.getCarried();return original(carried,player.getUUID(),state)?carried:ItemStack.EMPTY;
    }
    private static String image(ServerPlayer player,BlockPos base){
        Vec3 look=player.getLookAngle();if(look.y>.45)return "Night sky above the lake.";
        Vec3 steeple=Vec3.atCenterOf(base.offset(STEEPLE)).subtract(player.getEyePosition()).normalize();
        if(look.dot(steeple)>.7)return "The drowned steeple below the water.";
        return look.y<-.5?"The canoe's wet boards and the water beside them.":"Dark water. The bank at the edge of the image.";
    }
    private static void removePhone(ServerPlayer player,CompoundTag state){
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(original(player.getInventory().getItem(i),player.getUUID(),state))player.getInventory().setItem(i,ItemStack.EMPTY);
        if(original(player.containerMenu.getCarried(),player.getUUID(),state))player.containerMenu.setCarried(ItemStack.EMPTY);player.inventoryMenu.broadcastChanges();
    }
    private static void frame(ServerPlayer player,ItemStack phone,int tick,String image,Vec3 at,float yaw,float pitch){
        CustomData.update(DataComponents.CUSTOM_DATA,phone,t->{ListTag frames=t.getList("Footage",Tag.TAG_COMPOUND);CompoundTag f=new CompoundTag();f.putInt("Tick",tick);f.putLong("WorldTime",player.server.overworld().getGameTime());
            f.putString("Image",image);f.putDouble("X",at.x);f.putDouble("Y",at.y);f.putDouble("Z",at.z);f.putFloat("Yaw",yaw);f.putFloat("Pitch",pitch);frames.add(f);t.put("Footage",frames);});
    }
    private static LakePhoneCamera camera(ServerPlayer player,BlockPos base){
        String owner=CAMERA+player.getUUID();var cameras=player.serverLevel().getEntitiesOfClass(LakePhoneCamera.class,IndianLakeRooms.bounds(base,LabyrinthPlace.PHONE_CANOE),c->c.getTags().contains(owner));
        LakePhoneCamera camera=cameras.isEmpty()?DrownedTownRegistry.PHONE_CAMERA.get().create(player.serverLevel()):cameras.getFirst();if(camera==null)return null;
        var state=personal(LabyrinthData.get(player.server),player.getUUID());
        for(int i=1;i<cameras.size();i++)cameras.get(i).discard();camera.moveTo(dropPosition(state,base),0,-88);
        if(cameras.isEmpty()){camera.addTag(CAMERA);camera.addTag(owner);player.serverLevel().addFreshEntity(camera);}return camera;
    }
    private static Vec3 dropPosition(CompoundTag state,BlockPos base){return state.contains("DropX")?new Vec3(state.getDouble("DropX"),state.getDouble("DropY"),state.getDouble("DropZ")):new Vec3(base.getX()+1.2,base.getY()+.15,base.getZ()-32.5);}
    public static void tick(ServerPlayer player){
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=personal(data,player.getUUID());int phase=state.getInt("Phase");if(phase==0)return;
        if(!IndianLakeRooms.inside(player,LabyrinthPlace.PHONE_CANOE)){interrupt(player);return;}
        BlockPos base=IndianLakeRooms.base(player.server,LabyrinthPlace.PHONE_CANOE);IndianLakeRooms.keepLoaded(player.serverLevel(),base,LabyrinthPlace.PHONE_CANOE);
        LakeCanoeEntity canoe=stage(player.serverLevel(),base);if(canoe==null)return;
        if(phase==1){if(player.getVehicle()!=canoe)interrupt(player);return;}
        if(phase==2&&player.getVehicle()!=canoe){interrupt(player);return;}
        if(phase>=3){
            if(player.getVehicle()!=canoe){if(!canoe.getPassengers().isEmpty()||!player.startRiding(canoe,true))return;}
            player.setDeltaMovement(Vec3.ZERO);player.setAirSupply(player.getMaxAirSupply());player.fallDistance=0;
        }
        canoe.fixedView(phase>=3);int tick=state.getInt("Tick");
        if(phase==2&&tick>=FILM_TICKS&&canoe.position().multiply(1,0,1).distanceToSqr(Vec3.atBottomCenterOf(base.offset(STEEPLE)).multiply(1,0,1))>64){
            if(player.serverLevel().getGameTime()%20==0)send(player,2,-1,tick);
            if(player.serverLevel().getGameTime()%100==0)player.displayClientMessage(Component.literal("Keep filming. Row closer to the submerged steeple."),true);
            return;
        }
        if(tick<FILM_TICKS){
            ItemStack phone=scenePhone(player,state);if(phone.isEmpty()){interrupt(player);return;}
            if(tick%40==0)frame(player,phone,tick,image(player,base),player.getEyePosition(),player.getYRot(),player.getXRot());
            state.put("Item",phone.copyWithCount(1).save(player.registryAccess()));
            if(tick==120)sound(player,DrownedTownRegistry.PHONE_WATER.get(),Vec3.atCenterOf(base.offset(STEEPLE)),.7F);
        }else if(!state.getBoolean("Dropped")){
            ItemStack phone=scenePhone(player,state);if(phone.isEmpty()){interrupt(player);return;}
            state.putDouble("DropX",canoe.getX()+.7);state.putDouble("DropY",base.getY()-.1);state.putDouble("DropZ",canoe.getZ()+.25);
            frame(player,phone,tick,"The image turns. Water crosses the lens. Above it: sky.",dropPosition(state,base),0,-88);
            CustomData.update(DataComponents.CUSTOM_DATA,phone,t->t.putBoolean("Dropped",true));
            state.put("Item",phone.copyWithCount(1).save(player.registryAccess()));
            PreservedCave.rememberDroppedPhone(player,phone);removePhone(player,state);state.putBoolean("Dropped",true);state.putInt("Phase",3);canoe.fixedView(true);save(data,player.getUUID(),state);
            sound(player,DrownedTownRegistry.PHONE_WATER.get(),player.position(),1F);
        }
        if(tick>=FILM_TICKS){LakePhoneCamera camera=camera(player,base);if(camera!=null&&(tick%20==0||tick==FILM_TICKS))send(player,3,camera.getId(),tick);}
        if(tick==FILM_TICKS+SKY_TICKS){
            ItemStack phone=ItemStack.parseOptional(player.registryAccess(),state.getCompound("Item"));
            frame(player,phone,tick,"The same sky. The camera has not turned back.",dropPosition(state,base),0,-88);
            state.put("Item",phone.save(player.registryAccess()));PreservedCave.updateDroppedPhone(player,phone);
            HousePackets.send(player,new HouseFadePayload(12,20,20));
        }
        if(tick>=END){finish(player,state);return;}
        if(tick<FILM_TICKS&&tick%20==0)send(player,2,-1,tick);state.putInt("Tick",tick+1);save(data,player.getUUID(),state);
    }
    private static void finish(ServerPlayer player,CompoundTag state){
        state.putInt("Phase",0);state.putBoolean("Finished",true);save(LabyrinthData.get(player.server),player.getUUID(),state);
        player.stopRiding();BlockPos base=IndianLakeRooms.base(player.server,LabyrinthPlace.PHONE_CANOE);Vec3 at=Vec3.atBottomCenterOf(base.offset(DOCK));
        player.connection.teleport(at.x,at.y,at.z,180,0);player.setDeltaMovement(Vec3.ZERO);clearCamera(player,base);send(player,0,-1,0);
        LakeCanoeEntity canoe=stage(player.serverLevel(),base);if(canoe!=null){canoe.fixedView(false);canoe.setDeltaMovement(Vec3.ZERO);canoe.moveTo(Vec3.atBottomCenterOf(base.offset(BOAT)));}
        WitnessAccount.resolve(player,WitnessAccount.Story.PHONE_CANOE,"lost_recording");player.displayClientMessage(Component.literal("Your hands are empty. Something wooden scrapes against stone, far away."),false);
    }
    private static void clearCamera(ServerPlayer player,BlockPos base){
        if(base==null)return;ServerLevel level=player.server.getLevel(io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE);if(level==null)return;
        for(var camera:level.getEntitiesOfClass(LakePhoneCamera.class,IndianLakeRooms.bounds(base,LabyrinthPlace.PHONE_CANOE),c->c.getTags().contains(CAMERA+player.getUUID())))camera.discard();
    }
    public static void interrupt(ServerPlayer player){
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=personal(data,player.getUUID());
        if(!state.getBoolean("Dropped"))removePhone(player,state);state.putInt("Phase",0);save(data,player.getUUID(),state);
        clearCamera(player,IndianLakeRooms.base(player.server,LabyrinthPlace.PHONE_CANOE));send(player,0,-1,0);
        if(player.getVehicle() instanceof LakeCanoeEntity canoe&&canoe.getTags().contains(CANOE)){canoe.fixedView(false);player.stopRiding();}
    }
    private static void send(ServerPlayer player,int phase,int entity,int elapsed){HousePackets.send(player,new LakePhonePayload(phase,entity,elapsed));}
    private static void sound(ServerPlayer player,SoundEvent sound,Vec3 at,float volume){player.connection.send(new ClientboundSoundPacket(Holder.direct(sound),SoundSource.AMBIENT,at.x,at.y,at.z,volume,1,player.getRandom().nextLong()));}
    public static void onServerTick(ServerTickEvent.Post event){
        var server=event.getServer();for(ServerLevel world:server.getAllLevels())for(ServerPlayer player:List.copyOf(world.players()))tick(player);
        if(server.getTickCount()%20!=0)return;BlockPos base=IndianLakeRooms.base(server,LabyrinthPlace.PHONE_CANOE);ServerLevel level=server.getLevel(io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE);
        if(base==null||level==null||IndianLakeRooms.visitors(level,base,LabyrinthPlace.PHONE_CANOE).isEmpty())return;
        IndianLakeRooms.keepLoaded(level,base,LabyrinthPlace.PHONE_CANOE);LakeCanoeEntity canoe=stage(level,base);
    }
    public static void onDamage(LivingIncomingDamageEvent event){if(event.getEntity() instanceof ServerPlayer player&&bound(player))event.setCanceled(true);}
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer player)clearCamera(player,IndianLakeRooms.base(player.server,LabyrinthPlace.PHONE_CANOE));}
    public static void onToss(ItemTossEvent event){
        if(!(event.getPlayer() instanceof ServerPlayer player))return;CompoundTag state=personal(LabyrinthData.get(player.server),player.getUUID());
        if(bound(player)){event.setCanceled(true);player.getInventory().add(event.getEntity().getItem());return;}
        if(!original(event.getEntity().getItem(),player.getUUID(),state))return;
        event.setCanceled(true);interrupt(player);
    }
    public static void onAttack(AttackEntityEvent event){if(event.getEntity() instanceof ServerPlayer player&&bound(player))event.setCanceled(true);}
    public static void onBlock(PlayerInteractEvent.RightClickBlock event){if(event.getEntity() instanceof ServerPlayer player&&bound(player))event.setCanceled(true);}
    public static String describe(ServerPlayer player){var state=personal(LabyrinthData.get(player.server),player.getUUID());return "Phone in the canoe: phase "+state.getInt("Phase")+"; tick "+state.getInt("Tick")+"; dropped "+state.getBoolean("Dropped")+"; finished "+state.getBoolean("Finished")+"; waiting in cave "+PreservedCave.phoneWaiting(LabyrinthData.get(player.server),player.getUUID())+".";}
}
