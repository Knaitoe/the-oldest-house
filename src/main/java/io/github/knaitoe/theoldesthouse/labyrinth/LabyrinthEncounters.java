package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseExteriorEntityMirror;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import io.github.knaitoe.theoldesthouse.opening.Hillary;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Very rare quiet encounters. Rescued animals remain ordinary, vulnerable, tamable pets. */
public final class LabyrinthEncounters {
    public static final String STRAY="HouseStray",AMBIENT_UNTIL="HouseAmbientUntil";
    private static final String STATE="exploration_encounters",HOME="HouseAmbientHome";
    private static final Set<Item> STRAY_MEAT=Set.of(Items.BEEF,Items.COOKED_BEEF,Items.PORKCHOP,Items.COOKED_PORKCHOP,
            Items.CHICKEN,Items.COOKED_CHICKEN,Items.MUTTON,Items.COOKED_MUTTON,Items.RABBIT,Items.COOKED_RABBIT,
            Items.ROTTEN_FLESH,Items.COD,Items.COOKED_COD,Items.SALMON,Items.COOKED_SALMON,Items.TROPICAL_FISH,Items.PUFFERFISH);
    private LabyrinthEncounters(){}
    public static boolean eligible(LabyrinthPlace place) {
        return LabyrinthPacing.ordinary(place)||LabyrinthPacing.quiet(place)
                ||place==LabyrinthPlace.FOLDED_MAZE||place==LabyrinthPlace.DEEP_MAZE||place==LabyrinthPlace.ABYSS_MAZE;
    }
    public static int motherDenominator(LabyrinthPlace place){return LabyrinthPacing.quiet(place)?100:300;}
    public static int strayDenominator(LabyrinthPlace place){return LabyrinthPacing.quiet(place)?120:240;}
    public static boolean ambient(MotherEntity mother){return mother.getPersistentData().contains(AMBIENT_UNTIL);}
    public static void onArrive(ServerPlayer player,LabyrinthPlace place) {
        if(player.isSpectator()||!eligible(place)||!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR))return;
        LabyrinthData data=LabyrinthData.get(player.server);int depth=data.returnDepth(player.getUUID());
        if(depth<4)return;
        CompoundTag state=data.state(STATE);long now=player.serverLevel().getGameTime();
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();
        BlockPos base=origin==null?null:LabyrinthPlaces.base(origin,place);
        if(base==null)return;
        if(!state.getBoolean("Stray_"+place.id())
                &&(!state.contains("LastStray")||now-state.getLong("LastStray")>=9600)
                &&player.getRandom().nextInt(strayDenominator(place))==0) {
            long waiting=0;
            for(Entity entity:player.serverLevel().getAllEntities())
                if(entity instanceof TamableAnimal pet&&pet.getPersistentData().getBoolean(STRAY)&&!pet.isTame())waiting++;
            Vec3 spot=waiting>=3?null:hiddenSpot(player,place,base,2);
            if(spot!=null&&spawnStray(player.serverLevel(),spot,player.getRandom().nextBoolean())!=null) {
                state.putBoolean("Stray_"+place.id(),true);state.putLong("LastStray",now);data.setState(STATE,state);
            }
        }
        MotherCollection collection=MotherCollection.get(player.server);
        if(depth<LabyrinthPacing.STRANGE_DEPTH||collection.banished()||collection.salved()||collection.dogThreatOwner()!=null
                ||collection.anyDebt()
                ||(state.contains("LastMother")&&now-state.getLong("LastMother")<24000)
                ||player.getRandom().nextInt(motherDenominator(place))!=0)return;
        if(player.serverLevel().players().stream().anyMatch(p->LabyrinthPlaces.placeAt(origin,p.blockPosition())==LabyrinthPlace.MOTHER_DEN))return;
        MotherEntity keeper=null;
        for(Entity entity:player.serverLevel().getAllEntities())if(entity instanceof MotherEntity mother) {
            if(ambient(mother))return;
            if(mother.following()==null)keeper=mother;
        }
        Vec3 spot=keeper==null?null:hiddenSpot(player,place,base,3);
        if(spot==null||HouseWatchers.isWatched(player.serverLevel(),keeper.getEyePosition())
                ||HouseWatchers.isWatched(player.serverLevel(),keeper.position().add(0,.7,0)))return;
        keeper.getPersistentData().putLong(HOME,keeper.blockPosition().asLong());
        keeper.getPersistentData().putLong(AMBIENT_UNTIL,now+900);
        keeper.getNavigation().stop();keeper.teleportTo(spot.x,spot.y,spot.z);
        keeper.setCorruption(collection.corruption());
        state.putLong("LastMother",now);data.setState(STATE,state);
    }
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if(event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof TamableAnimal pet
                && feedStray(player,pet,event.getHand())) {
            event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
    /** A scarce piece of meat is enough to adopt a real lost animal in the House. */
    public static boolean feedStray(ServerPlayer player,TamableAnimal pet,InteractionHand hand) {
        ItemStack food=player.getItemInHand(hand);
        if(player.isSpectator()||!player.isAlive()||pet.level()!=player.level()
                ||!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)
                ||!pet.isAlive()||pet.isTame()||pet.getOwnerUUID()!=null||pet.distanceToSqr(player)>16
                ||!pet.getPersistentData().getBoolean(STRAY)||pet.getTags().contains(MotherOfStrays.PET)
                ||HouseExteriorEntityMirror.isProjection(pet)||Hillary.tagOf(pet)!=null||!STRAY_MEAT.contains(food.getItem())) return false;
        if(!player.getAbilities().instabuild)food.shrink(1);
        pet.tame(player);pet.setPersistenceRequired();pet.setTarget(null);
        if(pet instanceof Wolf wolf){wolf.setPersistentAngerTarget(null);wolf.setRemainingPersistentAngerTime(0);wolf.setIsInterested(false);}
        pet.setHealth(pet.getMaxHealth());
        CompanionOrders.issue(pet,player,CompanionOrders.Order.FOLLOW);
        player.serverLevel().broadcastEntityEvent(pet,(byte)7);
        return true;
    }
    /** Native bone/fish taming still works too. No Mother-kept-pet tag. */
    public static TamableAnimal spawnStray(ServerLevel level,Vec3 spot,boolean cat) {
        TamableAnimal pet=cat?EntityType.CAT.create(level):EntityType.WOLF.create(level);
        if(pet==null)return null;
        pet.moveTo(spot.x,spot.y,spot.z,level.getRandom().nextFloat()*360,0);
        pet.setPersistenceRequired();pet.getPersistentData().putBoolean(STRAY,true);
        return level.addFreshEntity(pet)?pet:null;
    }
    private static Vec3 hiddenSpot(ServerPlayer player,LabyrinthPlace place,BlockPos base,int headroom) {
        ServerLevel level=player.serverLevel();var box=place.room();
        Vec3 best=null;double distance=-1;
        for(int x=box.minX()+1;x<box.maxX();x++)for(int z=box.minZ()+1;z<box.maxZ();z++) {
            BlockPos feet=base.offset(x,0,z);
            if(!level.getBlockState(feet.below()).isCollisionShapeFullBlock(level,feet.below()))continue;
            boolean clear=true;
            for(int y=0;y<headroom;y++)if(!level.getBlockState(feet.above(y)).getCollisionShape(level,feet.above(y)).isEmpty()){clear=false;break;}
            Vec3 point=Vec3.atBottomCenterOf(feet);
            double apart=player.distanceToSqr(point);
            if(!clear||apart<64||HouseWatchers.isWatched(level,point.add(0,1,0))
                    ||HouseWatchers.isWatched(level,point.add(0,2.5,0)))continue;
            if(apart>distance){best=point;distance=apart;}
        }
        return best;
    }
    public static void onServerTick(ServerTickEvent.Post event) {
        if(event.getServer().getTickCount()%20!=0)return;
        ServerLevel level=event.getServer().getLevel(HouseDimensions.INTERIOR);
        if(level==null)return;
        BlockPos origin=HouseSavedData.get(event.getServer()).houseOrigin();
        List<MotherEntity> visitors=new ArrayList<>();
        for(Entity entity:level.getAllEntities())if(entity instanceof MotherEntity mother&&ambient(mother))visitors.add(mother);
        for(MotherEntity mother:visitors) {
            boolean denEntered=origin!=null&&level.players().stream().anyMatch(p->LabyrinthPlaces.placeAt(origin,p.blockPosition())==LabyrinthPlace.MOTHER_DEN);
            if(level.getGameTime()<mother.getPersistentData().getLong(AMBIENT_UNTIL)&&!denEntered)continue;
            if(HouseWatchers.isWatched(level,mother.getEyePosition())
                    ||HouseWatchers.isWatched(level,mother.position().add(0,.7,0)))continue;
            BlockPos home=BlockPos.of(mother.getPersistentData().getLong(HOME));level.getChunkAt(home);
            mother.getNavigation().stop();mother.teleportTo(home.getX()+.5,home.getY(),home.getZ()+.5);
            mother.getPersistentData().remove(AMBIENT_UNTIL);mother.getPersistentData().remove(HOME);
        }
    }
}
