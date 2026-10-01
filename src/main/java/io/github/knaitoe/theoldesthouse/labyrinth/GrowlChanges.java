package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.DoorBlock;

/** One roll per utterance. Successful rolls wait, saved, for one genuinely unwitnessed change. */
public final class GrowlChanges {
    public static final String STATE="growl_changes_0414";
    public static final int CHANCE=30, DELAY=40;
    private static final String MANOR="manor";
    private static final List<HouseShifts.Shift> MANOR_CHANGES=List.of(HouseShifts.Shift.PAINTINGS,
            HouseShifts.Shift.DOORS,HouseShifts.Shift.CANDLES,HouseShifts.Shift.CHAIRS,HouseShifts.Shift.GUEST_BED);
    private GrowlChanges(){}

    public static boolean selected(int roll){return roll>=0&&roll<CHANCE;}
    public static CompoundTag record(LabyrinthData data,UUID player){return data.state(STATE).getCompound(player.toString()).copy();}
    private static void save(LabyrinthData data,UUID player,CompoundTag record){
        CompoundTag world=data.state(STATE);world.put(player.toString(),record);data.setState(STATE,world);
    }
    public static int pending(LabyrinthData data,UUID player){
        CompoundTag events=record(data,player).getCompound("Pending");int n=0;
        for(String key:events.getAllKeys())n+=events.getCompound(key).getInt("Count");return n;
    }
    private static @Nullable String placeFor(ServerPlayer player){
        var house=HouseSavedData.get(player.server);BlockPos origin=house.houseOrigin();
        if(origin==null||!house.isImpossibleDoorRevealed()||player.isSpectator()
                ||!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR))return null;
        var phase=FinaleProgress.phase(player.server,player.getUUID());
        if(FinaleProgress.committed(phase)||FinaleProgress.terminal(phase))return null;
        LabyrinthPlace here=LabyrinthPlaces.placeAt(origin,player.blockPosition());
        if(here!=null)return here.isGray()&&!LabyrinthPacing.quiet(here)?here.id():null;
        return HouseDimensionMirror.isDomesticPosition(origin,player.blockPosition())?MANOR:null;
    }
    private static void enqueue(CompoundTag record,String place,long now,RandomSource random){
        CompoundTag pending=record.getCompound("Pending"),event=pending.getCompound(place);
        int count=event.getInt("Count");
        if(count==0){event.putLong("Due",now+DELAY);event.putInt("Kind",random.nextInt(3));}
        event.putInt("Count",count==Integer.MAX_VALUE?count:count+1);
        pending.put(place,event);record.put("Pending",pending);
    }
    /** Called by the common Growl playback path, including the cellar and manual sound fixtures. */
    public static boolean onGrowl(ServerPlayer player,RandomSource random){
        String place=placeFor(player);if(place==null)return false;
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag record=record(data,player.getUUID());
        record.putInt("Heard",record.getInt("Heard")+1);
        boolean change=selected(random.nextInt(100));
        if(change){record.putInt("Selected",record.getInt("Selected")+1);enqueue(record,place,player.serverLevel().getGameTime(),random);}
        save(data,player.getUUID(),record);return change;
    }
    /** Explicit operator fixture; it requests a change without altering the natural roll statistics. */
    public static boolean request(ServerPlayer player){
        String place=placeFor(player);if(place==null)return false;
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag record=record(data,player.getUUID());
        record.putInt("Requested",record.getInt("Requested")+1);
        enqueue(record,place,player.serverLevel().getGameTime(),player.getRandom());save(data,player.getUUID(),record);return true;
    }
    public static void tick(MinecraftServer server){
        ServerLevel level=server.getLevel(HouseDimensions.INTERIOR);
        if(level==null||HouseSavedData.get(server).houseOrigin()==null)return;
        for(ServerPlayer player:List.copyOf(level.players()))tickPlayer(player);
    }
    public static boolean tickPlayer(ServerPlayer player){
        if(placeFor(player)==null)return false;
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag record=record(data,player.getUUID());
        CompoundTag pending=record.getCompound("Pending");long now=player.serverLevel().getGameTime();
        List<String> places=new ArrayList<>(pending.getAllKeys());
        places.sort(Comparator.comparingLong(id->pending.getCompound(id).getLong("Due")));
        for(String id:places){
            CompoundTag event=pending.getCompound(id);if(event.getInt("Count")<=0||now<event.getLong("Due"))continue;
            String changed=apply(player,id,event.getInt("Kind"));if(changed==null)continue;
            int left=event.getInt("Count")-1;
            if(left==0)pending.remove(id);
            else{event.putInt("Count",left);event.putLong("Due",now+DELAY);event.putInt("Kind",player.getRandom().nextInt(3));pending.put(id,event);}
            record.put("Pending",pending);record.putInt("Applied",record.getInt("Applied")+1);
            record.putString("Last",changed);save(data,player.getUUID(),record);return true;
        }return false;
    }
    private static @Nullable String apply(ServerPlayer player,String id,int preferred){
        var house=HouseSavedData.get(player.server);BlockPos origin=house.houseOrigin();ServerLevel level=player.serverLevel();
        if(origin==null)return null;
        if(MANOR.equals(id)){
            if(!level.hasChunkAt(origin))return null;
            for(int i=0;i<MANOR_CHANGES.size();i++){
                String changed=HouseShifts.trigger(player.server,house,MANOR_CHANGES.get(Math.floorMod(preferred+i,MANOR_CHANGES.size())));
                if(changed!=null)return changed;
            }return null;
        }
        LabyrinthPlace place=LabyrinthPlace.byId(id);
        if(place==null||!place.isGray()||LabyrinthPacing.quiet(place))return null;
        BlockPos base=LabyrinthPlaces.base(origin,place);if(base==null||!level.hasChunkAt(base))return null;
        for(int i=0;i<3;i++){
            switch(Math.floorMod(preferred+i,3)){
                case 0->{String changed=changeRoute(player,origin,place);if(changed!=null)return changed;}
                case 1->{if(LabyrinthLighting.rearrangeOne(level,origin,place,player))return "A light moved in "+place.id()+".";}
                case 2->{if(NavigationAids.alterOne(player,place))return "A navigation mark changed in "+place.id()+".";}
            }
        }return null;
    }
    /** Entry/return, vignette, quiet-room, anomaly, finale and operator doors are never replaced. */
    public static @Nullable String changeRoute(ServerPlayer player,BlockPos origin,LabyrinthPlace place){
        if(!place.isGray()||LabyrinthPacing.quiet(place))return null;
        LabyrinthData data=LabyrinthData.get(player.server);ServerLevel level=player.serverLevel();
        List<LabyrinthData.Door> choices=new ArrayList<>();
        for(var spec:place.doors()){
            var door=data.door(place.doorId(spec));
            if(door!=null&&!door.command&&LabyrinthData.DEALT.equals(door.destination)
                    &&door.dimension.equals(level.dimension()))choices.add(door);
        }
        Collections.shuffle(choices,new Random(player.getRandom().nextLong()));
        for(var door:choices){
            var old=data.deal(player.getUUID(),door);LabyrinthPlace oldPlace=old==null?null:LabyrinthPlace.byId(old.place());
            if(old==null||old.bark()||oldPlace==null||!LabyrinthPacing.ordinary(oldPlace)||!mayChangeDoor(level,door))continue;
            var alternatives=LabyrinthDealer.grayAvailable(data,player.getUUID()).stream()
                    .filter(p->LabyrinthPacing.ordinary(p)&&p!=place&&p!=oldPlace).toList();
            if(alternatives.isEmpty())continue;
            var next=alternatives.get(player.getRandom().nextInt(alternatives.size()));
            data.deal(player.getUUID(),door,next.id(),old.leak(),false);
            return "Door "+door.id+" now leads to "+next.id()+" (was "+old.place()+").";
        }return null;
    }
    public static boolean mayChangeDoor(ServerLevel level,LabyrinthData.Door door){
        if(!level.hasChunkAt(door.lower))return false;
        var lower=level.getBlockState(door.lower);var upper=level.getBlockState(door.lower.above());
        if(!(lower.getBlock() instanceof DoorBlock)||!upper.is(lower.getBlock())
                ||lower.getValue(DoorBlock.OPEN)||upper.getValue(DoorBlock.OPEN))return false;
        if(HouseWatchers.isWatched(level,door.lower)||HouseWatchers.isWatched(level,door.lower.above()))return false;
        for(var watcher:level.players())if(!watcher.isSpectator()&&watcher.distanceToSqr(door.lower.getCenter())<16)return false;
        return true;
    }
    public static List<String> describe(MinecraftServer server,@Nullable ServerPlayer viewer){
        if(viewer==null)return List.of("Growl changes: "+CHANCE+"% per utterance; saved until unwitnessed.");
        LabyrinthData data=LabyrinthData.get(server);CompoundTag record=record(data,viewer.getUUID());
        return List.of("Growl changes: "+CHANCE+"% per utterance; "+record.getInt("Selected")+" selected from "
                +record.getInt("Heard")+" eligible growls; "+record.getInt("Applied")+" applied; "+pending(data,viewer.getUUID())
                +" pending."+(record.contains("Last")?" Last: "+record.getString("Last"):""));
    }
}
