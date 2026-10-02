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
            laid.setTarget(player.getUUID());laid.setUnlimitedLifetime();laid.setPickUpDelay(32767);laid.setInvulnerable(true);
            record.putUUID("LaidDown",laid.getUUID());record.putLong("LaidAt",laid.blockPosition().asLong());
        }
        if(player.getRespawnPosition()!=null)record.putLong("Base",player.getRespawnPosition().asLong());
        record.putString("BaseDimension",player.getRespawnDimension().location().toString());
        FinaleProgress.save(player.server,player.getUUID(),record);
        keepSceneLoaded(player,origin);
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
    /** The owner remains in the chamber while the actor climbs beyond short simulation distances. */
    public static Set<net.minecraft.world.level.ChunkPos> releaseChunks(BlockPos origin){
        List<BlockPos> route=releaseRoute(origin);int minX=Integer.MAX_VALUE,minZ=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,maxZ=Integer.MIN_VALUE;
        for(BlockPos at:route){minX=Math.min(minX,at.getX());maxX=Math.max(maxX,at.getX());minZ=Math.min(minZ,at.getZ());maxZ=Math.max(maxZ,at.getZ());}
        Set<net.minecraft.world.level.ChunkPos> chunks=new HashSet<>();
        for(int x=Math.floorDiv(minX-2,16);x<=Math.floorDiv(maxX+2,16);x++)for(int z=Math.floorDiv(minZ-2,16);z<=Math.floorDiv(maxZ+2,16);z++)
            chunks.add(new net.minecraft.world.level.ChunkPos(x,z));return chunks;
    }
    public static void keepSceneLoaded(ServerPlayer player,BlockPos origin){
        for(var chunk:releaseChunks(origin)){
            player.serverLevel().getChunkAt(chunk.getWorldPosition());
            player.serverLevel().getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.PORTAL,chunk,3,chunk.getWorldPosition());
        }
    }
    private static void unloadScene(ServerPlayer player,BlockPos origin){
        for(var chunk:releaseChunks(origin)){
            player.serverLevel().getChunkSource().removeRegionTicket(net.minecraft.server.level.TicketType.PORTAL,chunk,3,chunk.getWorldPosition());
        }
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
        if(step>=5){
            BlockPos b=FinaleArchitecture.base(origin);int x=at.getX()-b.getX(),z=at.getZ()-b.getZ();
            // The broad body must clear the higher treads on the inside of a spiral turn.
            target=target.add(Math.abs(x)==FinaleArchitecture.STAIR_RADIUS?Math.signum(x)*.6:0,0,Math.abs(z)==FinaleArchitecture.STAIR_RADIUS?Math.signum(z)*.6:0);
        }
        Vec3 delta=target.subtract(creature.position());
        if(delta.multiply(1,0,1).lengthSqr()<(step>=5?.0225:.65)&&Math.abs(delta.y)<1.2){
            if(step==3)FinaleArchitecture.seal(player.serverLevel(),origin,false);
            record.putInt("ReleaseStep",step+1);FinaleProgress.save(player.server,player.getUUID(),record);return;
        }
        // A broad actor's pathfinder can accept a partial path at the narrow cell opening.
        // Follow the authored route with native collision/step handling; never warp through a wall.
        creature.getNavigation().stop();
        Vec3 stride=delta.multiply(1,0,1);double distance=stride.length();
        creature.setDeltaMovement(creature.getDeltaMovement().multiply(0,1,0));
        // Downward contact keeps vanilla's on-ground stair stepping active for an authored stride.
        if(distance>.001)creature.move(MoverType.SELF,stride.scale(Math.min(.14,distance)/distance)
                .add(0,creature.getDeltaMovement().y>0?0:-.08,0));
        // Stair corners can present a full riser to a broad body; use the native jump controller there.
        if(creature.horizontalCollision&&delta.y>.1)creature.getJumpControl().jump();
        float facing=(float)(Math.atan2(delta.z,delta.x)*180/Math.PI)-90;creature.setYRot(facing);creature.yBodyRot=facing;
        creature.getLookControl().setLookAt(target.x,target.y+2,target.z,15,15);
        if(player.tickCount%24==0)player.serverLevel().playSound(null,creature.blockPosition(),SoundEvents.RAVAGER_STEP,SoundSource.HOSTILE,.45F,.65F);
    }
    private static void depart(ServerPlayer player,MinotaurEntity creature,CompoundTag record,BlockPos origin){
        creature.discard();record.remove("Creature");record.putString("Phase",FinaleProgress.Phase.HOMEWARD.name());
        unloadScene(player,origin);
        FinaleArchitecture.seal(player.serverLevel(),origin,false);FinaleProgress.save(player.server,player.getUUID(),record);
        FinaleController.releaseClaim(player.server,player.getUUID());
        var world=FinaleProgress.world(player.server);world.putBoolean("CellReleased",true);LabyrinthData.get(player.server).setState(FinaleProgress.STATE,world);
        FinaleController.words(player,FinaleArchitecture.base(origin).offset(0,FinaleArchitecture.ARENA+2,31),"The stairs lead back. The cell stays open.");
    }
    public static boolean returnWeapon(ServerPlayer player,CompoundTag record){
        if(!record.hasUUID("LaidDown"))return true;
        player.serverLevel().getChunkAt(BlockPos.of(record.getLong("LaidAt")));
        Entity entity=player.serverLevel().getEntity(record.getUUID("LaidDown"));
        if(!(entity instanceof ItemEntity item)){
            boolean alreadyRecovered=false;
            if(record.hasUUID("Weapon")){
                UUID original=record.getUUID("Weapon");
                for(int i=0;i<player.getInventory().getContainerSize();i++)alreadyRecovered|=WeaponHistory.wounds(player.getInventory().getItem(i),original);
                alreadyRecovered|=WeaponHistory.wounds(player.containerMenu.getCarried(),original);
            }
            if(alreadyRecovered){record.remove("LaidDown");record.remove("LaidAt");}return alreadyRecovered;
        }
        if(player.getInventory().getFreeSlot()<0){
            if(!record.getBoolean("WeaponNeedsSpace")){
                record.putBoolean("WeaponNeedsSpace",true);FinaleProgress.save(player.server,player.getUUID(),record);
                FinaleController.words(player,player.blockPosition().above(2),"Make room for the weapon you laid down.");
            }return false;
        }
        ItemStack stack=item.getItem();player.getInventory().add(stack);if(!stack.isEmpty())return false;
        item.discard();record.remove("LaidDown");record.remove("LaidAt");record.remove("WeaponNeedsSpace");return true;
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
        Vec3 outside=HouseProxyEntityEvacuation.frontDoorExit(player.server.overworld(),origin);
        if(outside==null)return; // Do not seal the player's ending before a safe exterior landing exists.
        if(!returnWeapon(player,record))return;
        List<TamableAnimal> companions=new ArrayList<>();
        for(var level:player.server.getAllLevels())if(HouseDimensions.isHouseDimension(level.dimension()))
            for(Entity entity:level.getAllEntities())if(entity instanceof TamableAnimal pet&&pet.isAlive()
                    &&!pet.getTags().contains(MotherOfStrays.PET)&&player.getUUID().equals(CompanionOrders.owner(pet)))companions.add(pet);
        record.putString("Phase",FinaleProgress.Phase.WITNESSED.name());record.putLong("EpilogueDue",player.server.overworld().getGameTime()+24000);
        FinaleProgress.save(player.server,player.getUUID(),record);
        FinaleController.releaseClaim(player.server,player.getUUID());
        CompoundTag world=FinaleProgress.world(player.server);world.putBoolean("CellReleased",true);LabyrinthData.get(player.server).setState(FinaleProgress.STATE,world);
        LabyrinthData.get(player.server).clearReturns(player.getUUID());HouseTransitionEvents.cancelPending(player,"the account is complete");
        player.server.overworld().getChunkAt(BlockPos.containing(outside));player.stopRiding();
        player.teleportTo(player.server.overworld(),outside.x,outside.y,outside.z,180,0);player.resetFallDistance();
        for(var pet:companions)CompanionOrders.followAcross(pet,player);
        FinaleController.words(player,player.blockPosition().above(2),"Your belongings. Your companions. The door closes quietly.");
    }
    public static ItemStack epilogue(ServerPlayer player){
        return WitnessAccount.book(LabyrinthData.get(player.server),player.getUUID(),player.getGameProfile().getName(),true);
    }
}
