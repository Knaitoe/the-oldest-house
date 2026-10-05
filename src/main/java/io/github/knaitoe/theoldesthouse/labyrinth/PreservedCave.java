package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Verb: moving quietly. Native player vibrations build a saved, eight-voice congregation. */
public final class PreservedCave {
    public static final String ID="preserved_cave", BODY="HouseCaveBody", CANOE_TAG="HouseCaveCanoe";
    public static final int BODY_COUNT=24, FULL_HYMN=8;
    public static final BlockPos CANOE=new BlockPos(0,0,-42);
    private static long nextHymn;
    private PreservedCave() {}
    public static void build(MinecraftServer server,ServerLevel level,BlockPos base){
        IndianLakeArchitecture.cave(level,base);
        LabyrinthData data=LabyrinthData.get(server);CompoundTag state=data.state(ID);state.putBoolean("Built",true);data.setState(ID,state);
        stage(level,base,data);
    }
    public static void onArrive(ServerPlayer player,LabyrinthPlace place){
        if(place!=LabyrinthPlace.PRESERVED_CAVE)return;
        BlockPos base=IndianLakeRooms.base(player.server,place);ServerLevel level=player.serverLevel();if(base==null)return;
        IndianLakeRooms.keepLoaded(level,base,place);
        if(IndianLakeRooms.visitors(level,base,place).stream().anyMatch(p->!p.getUUID().equals(player.getUUID())))return;
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=data.state(ID);
        state.putInt("Visit",state.getInt("Visit")+1);state.putDouble("Voices",0);state.putBoolean("Cold",false);
        state.put("Reached",new CompoundTag());data.setState(ID,state);
        // This is the next actual, unoccupied arrival: neither building nor opening the roof empties an occupied cave.
        IndianLakeProgress.arriveAtCave(data);stage(level,base,data);nextHymn=0;
        for(int z=-12;z>=-38;z-=6)for(int side:new int[]{-1,1})IndianLakeArchitecture.light(level,base.offset(side*9,4,z),4);
        player.displayClientMessage(Component.literal(IndianLakeProgress.deadOnShore(data)
                ?"The pews are empty. The bank is crowded."
                :"The waterline runs through the mouth of a cave. Every face inside is turned toward the lake."),false);
    }
    public static double noiseWeight(Holder<GameEvent> event){
        if(event.equals(GameEvent.STEP))return .35;
        if(event.equals(GameEvent.HIT_GROUND)||event.equals(GameEvent.PROJECTILE_LAND))return 1.4;
        if(event.equals(GameEvent.SWIM))return .6;
        if(event.equals(GameEvent.EAT)||event.equals(GameEvent.DRINK))return .8;
        return .7;
    }
    /** Only player-caused sound counts. The choir and the bodies cannot feed their own hymn. */
    public static void onGameEvent(VanillaGameEvent event){
        if(!(event.getLevel() instanceof ServerLevel level)||!level.dimension().equals(HouseDimensions.INTERIOR))return;
        BlockPos base=IndianLakeRooms.base(level.getServer(),LabyrinthPlace.PRESERVED_CAVE);if(base==null)return;
        Vec3 at=event.getEventPosition();
        if(!IndianLakeRooms.bounds(base,LabyrinthPlace.PRESERVED_CAVE).contains(at)||at.z>base.getZ()-11)return;
        Entity source=event.getContext().sourceEntity();if(source instanceof Projectile p&&p.getOwner()!=null)source=p.getOwner();
        if(!(source instanceof ServerPlayer player)||player.isSpectator()||TellTaleFloorboards.isSilent(player,event.getVanillaEvent()))return;
        LabyrinthData data=LabyrinthData.get(level.getServer());
        if(!data.state(ID).getBoolean("Built")||IndianLakeProgress.deadOnShore(data))return;
        int before=(int)Math.ceil(data.state(ID).getDouble("Voices"));
        addNoise(data,noiseWeight(event.getVanillaEvent()));stageBodies(level,base,data);
        int voices=(int)Math.ceil(data.state(ID).getDouble("Voices"));
        if(voices>before)level.playSound(null,base.offset(0,0,-10),DrownedTownRegistry.caveVoice(Math.max(0,voices-1)),SoundSource.AMBIENT,.35F,1);
    }
    public static void addNoise(LabyrinthData data,double weight){
        if(weight<=0||IndianLakeProgress.deadOnShore(data))return;
        CompoundTag state=data.state(ID);state.putDouble("Voices",Math.min(FULL_HYMN,state.getDouble("Voices")+weight));data.setState(ID,state);
    }
    public static boolean standing(int index,double voices){
        // Front rows rise first. Reaching the full hymn leaves no one seated.
        return voices>=4+2*(index/8);
    }
    public static void stage(ServerLevel level,BlockPos base,LabyrinthData data){
        stageBodies(level,base,data);
        List<LakeCanoeEntity> canoes=level.getEntitiesOfClass(LakeCanoeEntity.class,IndianLakeRooms.bounds(base,LabyrinthPlace.PRESERVED_CAVE));
        if(canoes.isEmpty()){
            LakeCanoeEntity canoe=DrownedTownRegistry.CAVE_CANOE.get().create(level);
            if(canoe!=null){canoe.addTag(CANOE_TAG);canoe.moveTo(Vec3.atBottomCenterOf(base.offset(CANOE)));canoe.setYRot(90);level.addFreshEntity(canoe);}
        }else for(int i=1;i<canoes.size();i++)canoes.get(i).discard();
    }
    public static void stageBodies(ServerLevel level,BlockPos base,LabyrinthData data){
        var bodies=level.getEntitiesOfClass(LakeCongregantEntity.class,IndianLakeRooms.bounds(base,LabyrinthPlace.PRESERVED_CAVE),b->b.getTags().contains(BODY));
        boolean shore=IndianLakeProgress.deadOnShore(data);double voices=data.state(ID).getDouble("Voices");
        for(int index=0;index<BODY_COUNT;index++){
            String tag=BODY+index;var matching=bodies.stream().filter(b->b.getTags().contains(tag)).toList();
            LakeCongregantEntity body=matching.isEmpty()?DrownedTownRegistry.CONGREGANT.get().create(level):matching.getFirst();if(body==null)continue;
            for(int i=1;i<matching.size();i++)matching.get(i).discard();
            boolean up=shore||standing(index,voices);int row=index/4,col=index%4;
            BlockPos at=shore?base.offset(col<2?-10-col:10+(col-2),0,-2-row)
                    :base.offset(new int[]{-5,-3,3,5}[col],0,-15-row*4);
            body.pose(false,!up);body.setNoGravity(true);body.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,0,0);
            if(matching.isEmpty()){body.addTag(BODY);body.addTag(tag);body.preservedEra(index%4);level.addFreshEntity(body);}
        }
    }
    public static void examine(ServerPlayer player,LakeCanoeEntity canoe){
        if(player.isSpectator()||!IndianLakeRooms.inside(player,LabyrinthPlace.PRESERVED_CAVE)
                ||!canoe.getTags().contains(CANOE_TAG)||player.distanceToSqr(canoe)>16)return;
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=data.state(ID);
        if(state.getBoolean("Cold"))return;
        CompoundTag reached=state.getCompound("Reached");reached.putBoolean(player.getUUID().toString(),true);state.put("Reached",reached);data.setState(ID,state);
        recoverPhone(player);
        if(data.isCompleted(ID))WitnessAccount.resolve(player,WitnessAccount.Story.PRESERVED_CAVE,"aftermath");
        player.displayClientMessage(Component.literal(IndianLakeProgress.deadOnShore(data)
                ?"The canoe is still here. Every seat behind it is empty."
                :"Water has gathered in the canoe. It has not touched the faces behind you."),false);
    }
    public static void onDepart(ServerPlayer player){
        departure(player).accept(player);
    }
    /** Keep this explorer's observed aftermath while their physical return is pending. */
    public static java.util.function.Consumer<ServerPlayer> departure(ServerPlayer player){
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=data.state(ID);
        boolean earned=!state.getBoolean("Cold")&&state.getCompound("Reached").getBoolean(player.getUUID().toString());
        boolean empty=IndianLakeProgress.deadOnShore(data);
        return p->{if(earned&&p.isAlive()&&p.gameMode.getGameModeForPlayer()!=net.minecraft.world.level.GameType.SPECTATOR){
            var current=LabyrinthData.get(p.server);WitnessAccount.resolve(p,WitnessAccount.Story.PRESERVED_CAVE,empty?"empty_pews":"crossed_congregation");
            if(empty)current.setCompleted(ID,true);
        }};
    }
    /** The filming encounter stores the original stack here. Its owner alone can retrieve it. */
    public static void rememberDroppedPhone(ServerPlayer owner,ItemStack phone){
        if(phone.isEmpty())return;LabyrinthData data=LabyrinthData.get(owner.server);CompoundTag state=data.state("indian_lake"),phones=state.getCompound("DroppedPhones");
        String id=owner.getUUID().toString();if(phones.contains(id))return;
        CompoundTag record=new CompoundTag();record.put("Item",phone.copyWithCount(1).save(owner.registryAccess()));
        record.putBoolean("Recovered",false);phones.put(id,record);state.put("DroppedPhones",phones);data.setState("indian_lake",state);
    }
    public static boolean phoneWaiting(LabyrinthData data,UUID player){
        CompoundTag record=data.state("indian_lake").getCompound("DroppedPhones").getCompound(player.toString());
        return !record.isEmpty()&&!record.getBoolean("Recovered");
    }
    /** Continue the same recording after it falls. Identity and recovery status prevent replacing another artifact. */
    public static boolean updateDroppedPhone(ServerPlayer owner,ItemStack phone){
        LabyrinthData data=LabyrinthData.get(owner.server);CompoundTag state=data.state("indian_lake"),phones=state.getCompound("DroppedPhones"),record=phones.getCompound(owner.getUUID().toString());
        if(record.isEmpty()||record.getBoolean("Recovered"))return false;
        ItemStack original=ItemStack.parseOptional(owner.registryAccess(),record.getCompound("Item"));
        var old=original.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);var next=phone.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if(old==null||next==null||!old.copyTag().hasUUID(PhoneCanoe.SCENE)||!next.copyTag().hasUUID(PhoneCanoe.SCENE)
                ||!old.copyTag().getUUID(PhoneCanoe.SCENE).equals(next.copyTag().getUUID(PhoneCanoe.SCENE)))return false;
        record.put("Item",phone.copyWithCount(1).save(owner.registryAccess()));phones.put(owner.getUUID().toString(),record);state.put("DroppedPhones",phones);data.setState("indian_lake",state);return true;
    }
    public static boolean recoverPhone(ServerPlayer player){
        if(!IndianLakeRooms.inside(player,LabyrinthPlace.PRESERVED_CAVE))return false;
        BlockPos base=IndianLakeRooms.base(player.server,LabyrinthPlace.PRESERVED_CAVE);
        if(player.position().distanceToSqr(Vec3.atBottomCenterOf(base.offset(CANOE)))>16)return false;
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=data.state("indian_lake"),phones=state.getCompound("DroppedPhones"),record=phones.getCompound(player.getUUID().toString());
        if(record.isEmpty()||record.getBoolean("Recovered"))return false;
        ItemStack phone=ItemStack.parseOptional(player.registryAccess(),record.getCompound("Item"));if(phone.isEmpty())return false;
        // Full inventories leave the phone in the canoe. It can never become a duplicate world drop.
        if(!player.getInventory().add(phone))return false;
        record.putBoolean("Recovered",true);phones.put(player.getUUID().toString(),record);state.put("DroppedPhones",phones);data.setState("indian_lake",state);
        player.inventoryMenu.broadcastChanges();return true;
    }
    public static void onAttack(AttackEntityEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)||!IndianLakeRooms.inside(player,LabyrinthPlace.PRESERVED_CAVE)
                ||!event.getTarget().getTags().contains(BODY))return;
        event.setCanceled(true);LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=data.state(ID);
        state.putBoolean("Cold",true);state.putDouble("Voices",FULL_HYMN);data.setState(ID,state);
        BlockPos base=IndianLakeRooms.base(player.server,LabyrinthPlace.PRESERVED_CAVE);stageBodies(player.serverLevel(),base,data);
        for(int z=-12;z>=-38;z-=6)for(int side:new int[]{-1,1})IndianLakeArchitecture.light(player.serverLevel(),base.offset(side*9,4,z),0);
    }
    public static void onServerTick(ServerTickEvent.Post event){
        MinecraftServer server=event.getServer();if(server.getTickCount()%20!=0)return;
        ServerLevel level=server.getLevel(HouseDimensions.INTERIOR);BlockPos base=IndianLakeRooms.base(server,LabyrinthPlace.PRESERVED_CAVE);
        if(level==null||base==null)return;LabyrinthData data=LabyrinthData.get(server);
        if(!data.state(ID).getBoolean("Built")||IndianLakeRooms.visitors(level,base,LabyrinthPlace.PRESERVED_CAVE).isEmpty())return;
        IndianLakeRooms.keepLoaded(level,base,LabyrinthPlace.PRESERVED_CAVE);stage(level,base,data);
        if(level.getGameTime()>=nextHymn){
            nextHymn=level.getGameTime()+120;int voices=IndianLakeProgress.deadOnShore(data)?FULL_HYMN:(int)Math.ceil(data.state(ID).getDouble("Voices"));
            for(int i=0;i<voices;i++)level.playSound(null,base.offset((i%2==0?-1:1)*6,0,-9),DrownedTownRegistry.caveVoice(i),SoundSource.AMBIENT,.3F,1);
        }
    }
    public static List<String> describe(MinecraftServer server){
        LabyrinthData data=LabyrinthData.get(server);CompoundTag state=data.state(ID);
        return List.of("Preserved cave: visit "+state.getInt("Visit")+"; hymn "+String.format(Locale.ROOT,"%.1f",state.getDouble("Voices"))+"/8; dead "+(IndianLakeProgress.deadOnShore(data)?"on shore":"in pews")+"; finished "+data.isCompleted(ID)+".");
    }
    public static boolean reset(MinecraftServer server){
        BlockPos base=IndianLakeRooms.base(server,LabyrinthPlace.PRESERVED_CAVE);ServerLevel level=server.getLevel(HouseDimensions.INTERIOR);if(base==null||level==null)return false;
        for(Entity e:level.getEntitiesOfClass(Entity.class,IndianLakeRooms.bounds(base,LabyrinthPlace.PRESERVED_CAVE),e->e.getTags().contains(BODY)||e.getTags().contains(CANOE_TAG)))e.discard();
        LabyrinthData data=LabyrinthData.get(server);data.setState(ID,new CompoundTag());data.setCompleted(ID,false);build(server,level,base);clearAll();return true;
    }
    public static void clearAll(){nextHymn=0;}
}
