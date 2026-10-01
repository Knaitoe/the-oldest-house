package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseExteriorEntityMirror;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.CompanionMenuPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Real cats and dogs keep their native owner and health while obeying a companion order. */
public final class CompanionOrders {
    public enum Order { FOLLOW, STAY, DEEPER, EXIT }
    public static final int PET_ACTION=4;
    private static final String KEY="HouseCompanionOrder";
    private static final Set<TamableAnimal> INSTALLED=java.util.Collections.newSetFromMap(new WeakHashMap<>());
    private CompanionOrders() {}
    @Nullable public static UUID owner(TamableAnimal pet) {
        HillaryTag hillary=Hillary.tagOf(pet);
        if(hillary!=null) return hillary.acknowledged() ? hillary.recipient() : null;
        return pet.isTame() ? pet.getOwnerUUID() : null;
    }
    public static boolean supported(Entity entity) {return entity instanceof TamableAnimal;}
    public static boolean managed(TamableAnimal pet) {return pet.getPersistentData().contains(KEY);}
    public static Order order(TamableAnimal pet) {
        int value=pet.getPersistentData().getInt(KEY);
        return value>=0&&value<Order.values().length ? Order.values()[value] : Order.FOLLOW;
    }
    public static boolean canCommand(ServerPlayer player, TamableAnimal pet) {
        return supported(pet)&&pet.isAlive()&&!HouseExteriorEntityMirror.isProjection(pet)
                &&!pet.getTags().contains(MotherOfStrays.PET)
                &&player.getUUID().equals(owner(pet))&&pet.level()==player.level()
                &&pet.distanceToSqr(player)<=36&&!player.isSpectator();
    }
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if(!(event.getTarget() instanceof TamableAnimal pet)||!supported(pet)
                ||event.getHand()!=InteractionHand.MAIN_HAND) return;
        if(!(event.getEntity() instanceof ServerPlayer player)||!canCommand(player,pet)) return;
        boolean compass=event.getItemStack().is(net.minecraft.world.item.Items.COMPASS);
        if(!compass&&!event.getItemStack().isEmpty())return;
        event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
        if(Hillary.introductionActive(pet)&&!Hillary.introductionConfirmed(pet)&&!compass) {
            pet(pet,player);return;
        }
        if(compass)issue(pet,player,Order.EXIT);else HousePackets.send(player,new CompanionMenuPayload(pet.getId()));
    }
    /** Used by the serverbound packet; entity ids never confer ownership or unlimited reach. */
    public static boolean command(ServerPlayer player,int entityId,int value) {
        if(value<0||value>PET_ACTION)return false;
        Entity entity=player.serverLevel().getEntity(entityId);
        if(!(entity instanceof TamableAnimal pet)||!canCommand(player,pet))return false;
        return value==PET_ACTION ? pet(pet,player) : issue(pet,player,Order.values()[value]);
    }
    /** A pat never replaces the stored movement order. Repeated packets cannot spam hearts. */
    public static boolean pet(TamableAnimal pet,ServerPlayer player) {
        if(!canCommand(player,pet))return false;
        long now=player.serverLevel().getGameTime();
        if(now<pet.getPersistentData().getLong("CompanionPatAfter"))return false;
        pet.getPersistentData().putLong("CompanionPatAfter",now+20);
        pet.getPersistentData().remove("CompanionFearUntil");
        pet.getLookControl().setLookAt(player,30,30);
        if(pet instanceof Wolf wolf) {wolf.setIsInterested(false);Hillary.confirmIntroduction(wolf,player);}
        player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
                pet.getX(),pet.getY()+pet.getBbHeight()+.2,pet.getZ(),3,.2,.1,.2,0);
        pet.playSound(pet instanceof Cat?SoundEvents.CAT_PURR:SoundEvents.WOLF_AMBIENT,.45F,1.25F);
        return true;
    }
    /** Resume the chosen wheel command after Hillary has finished the introduction. */
    public static void resume(TamableAnimal pet) {
        UUID id=owner(pet);if(id!=null&&pet.isTame())pet.setOwnerUUID(id);
        boolean sit=managed(pet)&&order(pet)==Order.STAY;
        pet.setOrderedToSit(sit);pet.setInSittingPose(sit);pet.getNavigation().stop();
    }
    public static boolean issue(TamableAnimal pet,ServerPlayer player,Order order) {
        if(!player.getUUID().equals(owner(pet)))return false;
        if((order==Order.EXIT||order==Order.DEEPER)&&!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)) {
            reassureSound(pet,false);return false;
        }
        CompoundTag data=pet.getPersistentData();
        data.putInt(KEY,order.ordinal());data.remove("CompanionFearUntil");data.remove("CompanionLastRoom");
        data.remove("HillaryFindExit");
        if(order==Order.EXIT)data.putBoolean("HillaryFindExit",true);
        if(pet.isTame())pet.setOwnerUUID(player.getUUID());
        boolean sit=order==Order.STAY&&!Hillary.introductionActive(pet);
        pet.setOrderedToSit(sit);pet.setInSittingPose(sit);
        pet.clearRestriction();pet.getNavigation().stop();
        install(pet);reassureSound(pet,true);
        return true;
    }
    public static void clear(TamableAnimal pet) {
        pet.getPersistentData().remove(KEY);pet.getPersistentData().remove("HillaryFindExit");
        UUID owner=owner(pet);if(owner!=null&&pet.isTame())pet.setOwnerUUID(owner);
    }
    private static void reassureSound(TamableAnimal pet,boolean accepted) {
        pet.playSound(pet instanceof Cat ? (accepted ? SoundEvents.CAT_AMBIENT : SoundEvents.CAT_HISS)
                : accepted ? SoundEvents.WOLF_AMBIENT : SoundEvents.WOLF_WHINE,.5F,accepted?1.15F:.9F);
    }
    private static void install(TamableAnimal pet) {
        if(INSTALLED.add(pet))pet.goalSelector.addGoal(0,new GuideGoal(pet));
    }
    public static void onEntityTick(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof TamableAnimal pet)||!supported(pet)
                ||!(pet.level() instanceof ServerLevel level)||HouseExteriorEntityMirror.isProjection(pet))return;
        if(pet.getPersistentData().getBoolean(FinaleController.GUIDE)
                ||pet.getTags().contains(MotherOfStrays.PET)||Hillary.introductionActive(pet))return;
        if((pet.getPersistentData().getBoolean("HouseStray")||pet.getTags().contains(MotherOfStrays.RELEASED))&&pet.isTame()&&!managed(pet))
            pet.getPersistentData().putInt(KEY,Order.FOLLOW.ordinal());
        if(!managed(pet))return;
        install(pet);
        if(pet instanceof Wolf wolf&&level.getGameTime()>=pet.getPersistentData().getLong("CompanionFearUntil"))
            wolf.setIsInterested(false);
        UUID id=owner(pet);ServerPlayer player=id==null?null:level.getServer().getPlayerList().getPlayer(id);
        if(player==null||player.level()!=level)return;
        if (order(pet)==Order.FOLLOW && level.dimension().equals(HouseDimensions.INTERIOR)) noteRoom(pet, player);
        if(order(pet)==Order.FOLLOW&&!pet.isTame()&&!pet.isOrderedToSit()
                &&level.getGameTime()>=pet.getPersistentData().getLong("CompanionFearUntil")
                &&pet.distanceToSqr(player)>9&&level.getGameTime()%10==0)pet.getNavigation().moveTo(player,1.1);
        if(order(pet)==Order.STAY&&!pet.isOrderedToSit()) {
            pet.setOrderedToSit(true);pet.setInSittingPose(true);pet.getNavigation().stop();
        }
    }
    private static void noteRoom(TamableAnimal pet, ServerPlayer player) {
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();
        if(origin==null)return;
        LabyrinthPlace place=LabyrinthPlaces.placeAt(origin,player.blockPosition());
        String room=place==null?"manor":place.id();
        CompoundTag data=pet.getPersistentData();
        if(room.equals(data.getString("CompanionLastRoom")))return;
        data.putString("CompanionLastRoom",room);
        int depth=LabyrinthData.get(player.server).returnDepth(player.getUUID());
        int pause=place==null||LabyrinthPacing.quiet(place)?0:hesitationTicks(depth);
        if(pause>0) {
            data.putLong("CompanionFearUntil",player.serverLevel().getGameTime()+pause);
            if(pet instanceof Wolf wolf)wolf.setIsInterested(true);
            reassureSound(pet,false);
        }
    }
    /** Fear costs a few seconds at a new deep place, never an indefinite refusal. */
    public static int hesitationTicks(int depth) {return depth<3?0:depth<6?20:depth<10?30:40;}
    public static void guide(TamableAnimal pet,ServerPlayer player) {
        ServerLevel level=player.serverLevel();BlockPos origin=HouseSavedData.get(player.server).houseOrigin();
        if(origin==null||!level.dimension().equals(HouseDimensions.INTERIOR))return;
        LabyrinthPlace place=LabyrinthPlaces.placeAt(origin,player.blockPosition());
        BlockPos base=place==null?null:LabyrinthPlaces.base(origin,place);
        noteRoom(pet, player);
        CompoundTag data=pet.getPersistentData();long now=level.getGameTime();
        if(now<data.getLong("CompanionFearUntil")) {
            pet.getNavigation().stop();pet.getLookControl().setLookAt(player,30,30);return;
        }
        if (FinaleArchitecture.contains(origin, player.blockPosition())) {
            BlockPos next = FinaleController.companionTarget(player, order(pet) == Order.EXIT);
            if (next != null) { pet.getNavigation().moveTo(next.getX()+.5,next.getY(),next.getZ()+.5,.85); pet.getLookControl().setLookAt(player); }
            return;
        }
        BlockPos goal;
        if(order(pet)==Order.EXIT) {
            goal=base!=null ? base.offset(0,0,-1)
                    : player.getZ()-origin.getZ()>HouseLayout.THRESHOLD_Z
                    ? origin.offset(HouseLayout.AXIS_X,1,HouseLayout.THRESHOLD_Z-1)
                    : origin.offset(HouseLayout.AXIS_X,1,HouseLayout.FRONT_DOOR_Z+1);
        } else {
            goal=base==null ? origin.offset(HouseLayout.AXIS_X,1,HouseLayout.THRESHOLD_Z+4)
                    : onward(player,place,base);
            if(goal==null) {pet.getNavigation().stop();pet.getLookControl().setLookAt(player,30,30);return;}
        }
        HillaryPaths.lead(pet,player,goal,place,base);
    }
    @Nullable private static BlockPos onward(ServerPlayer player,LabyrinthPlace place,BlockPos base) {
        LabyrinthData data=LabyrinthData.get(player.server);
        LabyrinthData.Door chosen=null;
        for(var spec:place.doors()) {
            LabyrinthData.Door door=data.door(place.doorId(spec));
            if(door==null||!LabyrinthData.DEALT.equals(door.destination))continue;
            var deal=data.deal(player.getUUID(),door);
            if(deal==null)continue;
            if(deal.bark())return door.lower.relative(door.facing,2);
            if(chosen==null||door.lower.distSqr(base)>chosen.lower.distSqr(base))chosen=door;
        }
        return chosen==null?null:chosen.lower.relative(chosen.facing,2);
    }
    /** Nearby standing companions are captured before the player leaves their world. */
    public static List<TamableAnimal> followingAll(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(TamableAnimal.class,player.getBoundingBox().inflate(12),
                pet->supported(pet)&&pet.isAlive()&&player.getUUID().equals(owner(pet))&&!pet.isOrderedToSit()
                        &&!pet.isLeashed()&&!pet.isPassenger()&&!pet.getTags().contains(MotherOfStrays.PET)
                        &&!HouseExteriorEntityMirror.isProjection(pet)
                        &&pet.distanceToSqr(player)<=144);
    }
    @Nullable public static TamableAnimal followAcross(TamableAnimal pet,ServerPlayer player) {
        if(pet.isRemoved())return null;
        Vec3 point=HillaryPaths.safeBeside(pet,player);
        player.serverLevel().getChunkAt(BlockPos.containing(point));
        if(pet.level()!=player.level()) {
            Entity moved=pet.changeDimension(new DimensionTransition(player.serverLevel(),point,Vec3.ZERO,
                    player.getYRot(),0,DimensionTransition.PLACE_PORTAL_TICKET));
            if(!(moved instanceof TamableAnimal arriving))return null;
            pet=arriving;
        } else pet.teleportTo(point.x,point.y,point.z);
        UUID owner=owner(pet);if(owner!=null&&pet.isTame())pet.setOwnerUUID(owner);
        pet.clearRestriction();pet.getNavigation().stop();pet.setDeltaMovement(Vec3.ZERO);pet.resetFallDistance();
        return pet;
    }
    public static void clearAll(){INSTALLED.clear();}
    private static final class GuideGoal extends Goal {
        private final TamableAnimal pet;
        GuideGoal(TamableAnimal pet){this.pet=pet;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse() {
            UUID id=owner(pet);
            var level=pet.level();
            if(Hillary.introductionActive(pet)||pet.getTags().contains(MotherOfStrays.PET))return false;
            if(!(level instanceof ServerLevel server)||!managed(pet)||pet.isOrderedToSit()||pet.isLeashed()||pet.isPassenger())return false;
            Order order=order(pet);
            if(order==Order.FOLLOW)return server.getGameTime()<pet.getPersistentData().getLong("CompanionFearUntil");
            if(order!=Order.EXIT&&order!=Order.DEEPER)return false;
            ServerPlayer player=id==null?null:server.getServer().getPlayerList().getPlayer(id);
            return player!=null&&player.level()==level&&server.dimension().equals(HouseDimensions.INTERIOR);
        }
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick() {
            UUID id=owner(pet);
            ServerPlayer player=id==null?null:((ServerLevel)pet.level()).getServer().getPlayerList().getPlayer(id);
            if(player!=null) {
                if(order(pet)==Order.FOLLOW) {pet.getNavigation().stop();pet.getLookControl().setLookAt(player,30,30);}
                else guide(pet,player);
            }
        }
        @Override public void stop(){pet.getNavigation().stop();}
    }
}

