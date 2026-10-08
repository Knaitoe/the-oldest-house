package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Personal facts acquired through native play. No inventory snapshots or invented biography. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HouseExperience {
    public static final String ID="house_experience_0427";
    private HouseExperience(){}
    public static CompoundTag record(LabyrinthData d,UUID id){return d.state(ID).getCompound(id.toString()).copy();}
    public static void save(LabyrinthData d,UUID id,CompoundTag own){var all=d.state(ID);all.put(id.toString(),own);d.setState(ID,all);}
    public static boolean inManor(ServerPlayer p,BlockPos pos){
        var origin=HouseSavedData.get(p.server).houseOrigin();
        return origin!=null&&p.level().dimension().equals(HouseDimensions.INTERIOR)&&HouseDimensionMirror.isDomesticPosition(origin,pos)&&!HouseImpossibleHallway.isInteriorOnlyPosition(origin,pos);
    }
    public static void sat(ServerPlayer p,BlockPos pos,BlockState state){
        if(!inManor(p,pos))return;var d=LabyrinthData.get(p.server);var own=record(d,p.getUUID());
        own.putLong("Seat",pos.asLong());own.put("SeatState",NbtUtils.writeBlockState(state));
        own.put("SeatFloor",NbtUtils.writeBlockState(p.level().getBlockState(pos.below())));save(d,p.getUUID(),own);
    }
    public static void slept(ServerPlayer p){
        if(!inManor(p,p.blockPosition()))return;var d=LabyrinthData.get(p.server);var own=record(d,p.getUUID());
        own.putLong("Bed",p.getRespawnPosition()==null?p.blockPosition().asLong():p.getRespawnPosition().asLong());own.putInt("Sleeps",own.getInt("Sleeps")+1);save(d,p.getUUID(),own);
    }
    public static void cared(ServerPlayer p,TamableAnimal pet){
        if(!p.getUUID().equals(CompanionOrders.owner(pet))||!pet.isAlive())return;
        var d=LabyrinthData.get(p.server);var own=record(d,p.getUUID());own.putUUID("CaredPet",pet.getUUID());own.putString("CaredName",pet.getName().getString());own.putBoolean("CaredNamed",pet.hasCustomName());own.putInt("Care",own.getInt("Care")+1);save(d,p.getUUID(),own);
    }
    public static void ordered(ServerPlayer p,TamableAnimal pet,CompanionOrders.Order order){
        if(order!=CompanionOrders.Order.STAY||!HouseDimensions.isHouseDimension(p.level().dimension()))return;
        var d=LabyrinthData.get(p.server);var own=record(d,p.getUUID());own.putUUID("WaitingPet",pet.getUUID());own.putString("WaitingName",pet.getName().getString());own.putLong("WaitingAt",pet.blockPosition().asLong());own.putString("WaitingDimension",pet.level().dimension().location().toString());save(d,p.getUUID(),own);
    }
    @SubscribeEvent public static void opened(PlayerContainerEvent.Open e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getContainer() instanceof ChestMenu menu)||!(menu.getContainer() instanceof BlockEntity be)||!inManor(p,be.getBlockPos()))return;
        var d=LabyrinthData.get(p.server);var own=record(d,p.getUUID());own.putLong("Preparation",be.getBlockPos().asLong());save(d,p.getUUID(),own);
    }
    public static void arrived(ServerPlayer p,LabyrinthPlace place){
        HouseCorrespondence.crossed(p);
        LabyrinthLoops.arrive(p,place);
        var d=LabyrinthData.get(p.server);var own=record(d,p.getUUID());own.putString("CurrentPlace",place.id());own.putInt("Deepest",Math.max(own.getInt("Deepest"),d.returnDepth(p.getUUID())));
        own.remove("EchoAt");
        if(place==LabyrinthPlace.QUIET_ROOM&&d.returnDepth(p.getUUID())>=8&&own.contains("SeatState")){
            var origin=HouseSavedData.get(p.server).houseOrigin();var base=origin==null?null:LabyrinthPlaces.base(origin,place);
            if(base!=null)for(int[] offset:new int[][]{{-4,-11},{4,-11},{-4,-6},{4,-6}}){
                BlockPos at=base.offset(offset[0],0,offset[1]);
                if(p.level().getBlockState(at).isAir()&&p.level().getBlockState(at.above()).isAir()&&p.level().getBlockEntity(at.below())==null&&p.level().getBlockState(at.below()).isFaceSturdy(p.level(),at.below(),Direction.UP)){
                    own.putLong("EchoAt",at.asLong());own.putString("EchoDimension",p.level().dimension().location().toString());break;
                }
            }
        }save(d,p.getUUID(),own);
    }
    public static void returned(ServerPlayer p){
        var d=LabyrinthData.get(p.server);var own=record(d,p.getUUID());String id=own.getString("CurrentPlace");if(id.isEmpty())return;
        HouseCorrespondence.crossed(p);
        var place=Arrays.stream(LabyrinthPlace.values()).filter(v->v.id().equals(id)).findFirst().orElse(null);
        own.putInt("Returns",own.getInt("Returns")+1);own.putString("LastReturn",id);
        if(place!=null&&place.isVignette()){
            var story=WitnessAccount.Story.of(id);boolean finished=story!=null&&WitnessAccount.has(d,p.getUUID(),story);
            if(!finished){var retreats=own.getCompound("Retreats");retreats.putInt(id,Math.min(16,retreats.getInt(id)+1));own.put("Retreats",retreats);own.putString("LastRetreat",id);}
        }
        own.remove("CurrentPlace");own.remove("EchoAt");save(d,p.getUUID(),own);
    }
    public static int weight(LabyrinthData d,UUID player,LabyrinthPlace place,int weight){
        var own=record(d,player);if(d.recentVisit(player,place)>=0&&d.recentVisit(player,place)<3)return weight;
        var story=WitnessAccount.Story.of(place.id());boolean finished=story!=null&&WitnessAccount.has(d,player,story);
        if(own.getCompound("Retreats").getInt(place.id())>0&&!finished)return weight+Math.max(1,weight/2);
        if(place==LabyrinthPlace.KAREN_ROOM&&own.getInt("Returns")>=2)return weight+Math.max(1,weight/3);
        return weight;
    }
    public static List<String> traces(ServerPlayer p){
        var d=LabyrinthData.get(p.server);var own=record(d,p.getUUID());var text=new ArrayList<String>();
        if(own.contains("Seat"))text.add("You once sat in that chair. Someone has measured the space beneath it.");
        if(own.getInt("Sleeps")>0)text.add("You have slept in the manor. A crease in the pillow would be evidence enough, if you could find the bed again.");
        if(!own.getString("LastRetreat").isEmpty())text.add("You turned back from "+own.getString("LastRetreat").replace('_',' ')+". There was still something left to do. The page leaves a space for it.");
        if(!own.getString("WaitingName").isEmpty())text.add("You asked "+own.getString("WaitingName")+" to wait. The ink keeps the name. It does not promise the animal is still there.");
        if(!own.getString("CaredName").isEmpty())text.add("You put your hand on "+own.getString("CaredName")+". For a moment the movement in the dark had a name.");
        var camp=HollowayVignette.personal(d,p.getUUID());
        if(camp.getBoolean("Looted"))text.add("You took something from Holloway's supplies. Someone has counted the empty places twice.");
        if(WitnessAccount.has(d,p.getUUID(),WitnessAccount.Story.MOTHER))text.add("You have returned from the Mother with a bargain recorded in your account. A return is also a thing that happened here.");
        var novel=NovelVignettes.personal(d,p.getUUID());
        if(novel.contains("Photo"))text.add(novel.getBoolean("PhotoGivenAway")?"You gave away the original photograph. The page preserves the blank where its frame would fit.":"You have held the distant frame. A picture can remember a direction without returning you there.");
        return text;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        for(var p:e.getServer().getPlayerList().getPlayers())if(p.tickCount%10==0)tickPlayer(p);
    }
    public static void tickPlayer(ServerPlayer p){
        if(!p.isAlive()||p.isSpectator())return;var d=LabyrinthData.get(p.server);var own=record(d,p.getUUID());var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return;
        boolean inManor=inManor(p,p.blockPosition());
        if(inManor&&!own.getBoolean("AtManor")){
            if(own.getBoolean("ManorSeen"))own.putInt("ManorReturns",own.getInt("ManorReturns")+1);
            own.putBoolean("ManorSeen",true);
        }
        own.putBoolean("AtManor",inManor);
        int hush=0;BlockPos threshold=origin.offset(HouseLayout.AXIS_X,1,HouseLayout.THRESHOLD_Z);
        if(HouseSavedData.get(p.server).isImpossibleDoorRevealed()&&p.level().dimension().equals(HouseDimensions.INTERIOR)){
            Vec3 to=threshold.getCenter().subtract(p.getEyePosition());double distance=to.length();
            if(distance<9&&Math.abs(p.getY()-threshold.getY())<3&&p.getLookAngle().dot(to.normalize())>.15)hush=(int)Math.min(100,Math.max(0,(9-distance)*12));
            long now=p.level().getGameTime();
            if(hush>25&&own.getLong("HesitationAfter")<now){
                Vec3 previous=new Vec3(own.getDouble("ApproachX"),own.getDouble("ApproachY"),own.getDouble("ApproachZ"));
                if(own.getBoolean("Approached")&&previous.distanceToSqr(p.position())<.01){
                    Vec3 behind=p.position().subtract(p.getLookAngle().multiply(1,0,1).normalize().scale(4));
                    p.connection.send(new ClientboundSoundPacket(Holder.direct(SoundEvents.DEEPSLATE_STEP),SoundSource.BLOCKS,behind.x,behind.y,behind.z,.2F,.8F,p.getRandom().nextLong()));own.putLong("HesitationAfter",now+180);
                }
            }
            own.putBoolean("Approached",hush>0);own.putDouble("ApproachX",p.getX());own.putDouble("ApproachY",p.getY());own.putDouble("ApproachZ",p.getZ());
        }
        int chair=-1,floor=-1;BlockPos echo=BlockPos.ZERO;
        if(own.contains("EchoAt")&&p.level().dimension().location().toString().equals(own.getString("EchoDimension"))){
            echo=BlockPos.of(own.getLong("EchoAt"));
            if(p.level().getBlockState(echo).isAir()&&p.level().getBlockState(echo.above()).isAir()){
                var blocks=p.registryAccess().lookupOrThrow(Registries.BLOCK);chair=Block.getId(NbtUtils.readBlockState(blocks,own.getCompound("SeatState")));floor=Block.getId(NbtUtils.readBlockState(blocks,own.getCompound("SeatFloor")));
            }
        }
        HousePackets.send(p,new HomeEchoPayload(echo,chair,floor,hush));
        if(own.hasUUID("WaitingPet")&&p.level().dimension().location().toString().equals(own.getString("WaitingDimension"))&&p.level().getGameTime()>own.getLong("ReturnVoiceAfter")){
            var pet=p.serverLevel().getEntity(own.getUUID("WaitingPet"));
            if(pet instanceof TamableAnimal animal&&animal.isAlive()&&p.getUUID().equals(CompanionOrders.owner(animal))&&CompanionOrders.order(animal)==CompanionOrders.Order.STAY&&p.distanceToSqr(animal)>36&&p.distanceToSqr(animal)<576){
                p.connection.send(new ClientboundSoundPacket(Holder.direct(animal instanceof Cat?SoundEvents.CAT_AMBIENT:SoundEvents.WOLF_WHINE),SoundSource.NEUTRAL,animal.getX(),animal.getY(),animal.getZ(),.7F,1,p.getRandom().nextLong()));own.putLong("ReturnVoiceAfter",p.level().getGameTime()+200);
            }
        }
        save(d,p.getUUID(),own);
    }
}
