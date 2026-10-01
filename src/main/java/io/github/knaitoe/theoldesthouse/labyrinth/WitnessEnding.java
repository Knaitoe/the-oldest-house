package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** A deliberate release, a physical return, and a personal ending in a House that remains. */
public final class WitnessEnding {
    private WitnessEnding(){}
    public static boolean qualified(LabyrinthData data,UUID player){return WitnessAccount.ready(data,player)&&WitnessAccount.readPlay(data,player);}
    private static @Nullable ItemEntity laidDown(ServerPlayer player,BlockPos cell,UUID weapon){
        return player.serverLevel().getEntitiesOfClass(ItemEntity.class,new AABB(cell).inflate(6),
                item->item.isAlive()&&WeaponHistory.wounds(item.getItem(),weapon)).stream().findFirst().orElse(null);
    }
    public static boolean begin(ServerPlayer player){
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();
        if(origin==null||player.isSpectator()||!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)
                ||!FinaleArchitecture.ready(player.server)||FinaleController.lockedOut(player)
                ||FinaleProgress.committed(FinaleProgress.phase(player.server,player.getUUID())))return false;
        BlockPos cell=FinaleArchitecture.cell(origin);if(cell.distToCenterSqr(player.position())>49)return false;
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag world=FinaleProgress.world(player.server);
        if(world.getBoolean("Ended")||world.hasUUID("Owner")&&!world.getUUID("Owner").equals(player.getUUID()))return false;
        if(!qualified(data,player.getUUID())){
            FinaleController.words(player,cell.above(),"The passage outside the cell is still unread.");return false;
        }
        if(!player.isShiftKeyDown()||!player.getMainHandItem().isEmpty()||!player.getOffhandItem().isEmpty()){
            FinaleController.words(player,cell.above(),"Both hands empty. Bow your head. Leave the door open.");return false;
        }
        UUID weapon=WeaponHistory.favorite(player);ItemEntity laid=weapon==null?null:laidDown(player,cell,weapon);
        if(weapon!=null&&laid==null){
            FinaleController.words(player,cell.above(),"Lay the original weapon beside the cell. An empty hand is only part of it.");return false;
        }
        world.putUUID("Owner",player.getUUID());data.setState(FinaleProgress.STATE,world);
        CompoundTag record=FinaleProgress.player(player.server,player.getUUID());
        record.putString("Phase",FinaleProgress.Phase.RELEASE.name());record.putInt("ReleaseStep",0);record.putInt("ReleaseWait",60);
        record.putInt("PassSide",player.getX()>cell.getX()+.5?-1:1);
        if(weapon!=null)record.putUUID("Weapon",weapon);
        if(laid!=null){
            laid.setTarget(player.getUUID());laid.setUnlimitedLifetime();laid.setPickUpDelay(32767);
            record.putUUID("LaidDown",laid.getUUID());record.putLong("LaidAt",laid.blockPosition().asLong());
        }
        if(player.getRespawnPosition()!=null)record.putLong("Base",player.getRespawnPosition().asLong());
        record.putString("BaseDimension",player.getRespawnDimension().location().toString());
        FinaleProgress.save(player.server,player.getUUID(),record);
        FinaleArchitecture.seal(player.serverLevel(),origin,true);FinaleArchitecture.openCell(player.serverLevel(),origin);
        FinaleController.words(player,cell.above(),"There is someone inside.");return true;
    }
    public static List<BlockPos> releaseRoute(BlockPos origin){
        BlockPos b=FinaleArchitecture.base(origin),cell=FinaleArchitecture.cell(origin);
        List<BlockPos> stairs=FinaleArchitecture.staircaseRoute(origin),route=new ArrayList<>();
        route.add(cell.south(5));route.add(cell.north(2));route.add(cell.north(6));route.add(b.offset(0,FinaleArchitecture.ARENA,31));
        BlockPos last=stairs.get(stairs.size()-1);
        route.add(new BlockPos(b.getX(),FinaleArchitecture.ARENA,last.getZ()));route.add(last);
        for(int i=stairs.size()-2;i>=stairs.size()-26;i--)route.add(stairs.get(i));return List.copyOf(route);
    }
    /** The actor pauses while its owner is away, and resumes its saved physical route. */
    public static void tickCreature(MinotaurEntity creature,ServerPlayer player){
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();if(origin==null)return;
        CompoundTag record=FinaleProgress.player(player.server,player.getUUID());
        if(FinaleProgress.phase(record)!=FinaleProgress.Phase.RELEASE)return;
        int wait=record.getInt("ReleaseWait"),step=record.getInt("ReleaseStep");
        if(wait>0){creature.getNavigation().stop();creature.getLookControl().setLookAt(player,15,15);record.putInt("ReleaseWait",wait-1);
            FinaleProgress.save(player.server,player.getUUID(),record);return;}
        List<BlockPos> route=releaseRoute(origin);
        if(step>=route.size()){depart(player,creature,record,origin);return;}
        BlockPos at=route.get(step);player.serverLevel().getChunkAt(at);
        Vec3 target=Vec3.atBottomCenterOf(at);
        if(step==2)target=target.add(record.getInt("PassSide")*1.25,0,0);
        Vec3 delta=target.subtract(creature.position());
        if(delta.multiply(1,0,1).lengthSqr()<.65&&Math.abs(delta.y)<1.2){
            record.putInt("ReleaseStep",step+1);FinaleProgress.save(player.server,player.getUUID(),record);return;
        }
        if(player.tickCount%5==0)creature.getNavigation().moveTo(target.x,target.y,target.z,.7);
        creature.getLookControl().setLookAt(target.x,target.y+2,target.z,15,15);
        if(player.tickCount%24==0)player.serverLevel().playSound(null,creature.blockPosition(),SoundEvents.RAVAGER_STEP,SoundSource.HOSTILE,.45F,.65F);
    }
    private static void depart(ServerPlayer player,MinotaurEntity creature,CompoundTag record,BlockPos origin){
        creature.discard();record.remove("Creature");record.putString("Phase",FinaleProgress.Phase.HOMEWARD.name());
        FinaleArchitecture.seal(player.serverLevel(),origin,false);FinaleProgress.save(player.server,player.getUUID(),record);
        FinaleController.words(player,FinaleArchitecture.base(origin).offset(0,FinaleArchitecture.ARENA+2,31),"The stairs lead back. The cell stays open.");
    }
    public static boolean returnWeapon(ServerPlayer player,CompoundTag record){
        if(!record.hasUUID("LaidDown"))return true;
        player.serverLevel().getChunkAt(BlockPos.of(record.getLong("LaidAt")));
        Entity entity=player.serverLevel().getEntity(record.getUUID("LaidDown"));
        if(!(entity instanceof ItemEntity item))return false;
        ItemStack stack=item.getItem();player.getInventory().add(stack);
        if(stack.isEmpty())item.discard();else{item.setNoPickUpDelay();item.setPos(player.getX(),player.getY(),player.getZ());}
        record.remove("LaidDown");record.remove("LaidAt");return true;
    }
    public static void keepWeaponOnDeath(ServerPlayer player,CompoundTag record){
        if(!record.hasUUID("LaidDown"))return;
        player.serverLevel().getChunkAt(BlockPos.of(record.getLong("LaidAt")));
        if(player.serverLevel().getEntity(record.getUUID("LaidDown")) instanceof ItemEntity item){
            MotherCollection.get(player.server).keepFinaleItem(item.getItem(),player.registryAccess(),player.getUUID(),player.serverLevel().getGameTime());item.discard();
        }
        record.remove("LaidDown");record.remove("LaidAt");
    }
    public static void finish(ServerPlayer player,BlockPos origin,CompoundTag record){
        if(FinaleProgress.phase(record)!=FinaleProgress.Phase.HOMEWARD)return;
        if(!returnWeapon(player,record))return;
        List<TamableAnimal> companions=new ArrayList<>();
        for(var level:player.server.getAllLevels())if(HouseDimensions.isHouseDimension(level.dimension()))
            for(Entity entity:level.getAllEntities())if(entity instanceof TamableAnimal pet&&pet.isAlive()
                    &&!pet.getTags().contains(MotherOfStrays.PET)&&player.getUUID().equals(CompanionOrders.owner(pet)))companions.add(pet);
        record.putString("Phase",FinaleProgress.Phase.WITNESSED.name());record.putLong("EpilogueDue",player.server.overworld().getGameTime()+24000);
        FinaleProgress.save(player.server,player.getUUID(),record);
        CompoundTag world=FinaleProgress.world(player.server);world.remove("Owner");LabyrinthData.get(player.server).setState(FinaleProgress.STATE,world);
        LabyrinthData.get(player.server).clearReturns(player.getUUID());HouseTransitionEvents.cancelPending(player,"the account is complete");
        Vec3 outside=HouseProxyEntityEvacuation.frontDoorExit(player.server.overworld(),origin);
        player.server.overworld().getChunkAt(BlockPos.containing(outside));player.stopRiding();
        player.teleportTo(player.server.overworld(),outside.x,outside.y,outside.z,180,0);player.resetFallDistance();
        for(var pet:companions)CompanionOrders.followAcross(pet,player);
        FinaleController.words(player,player.blockPosition().above(2),"The same evening. Everything you carried. The door closes quietly.");
    }
    public static ItemStack epilogue(ServerPlayer player){
        return WitnessAccount.book(LabyrinthData.get(player.server),player.getUUID(),player.getGameProfile().getName(),true);
    }
}
