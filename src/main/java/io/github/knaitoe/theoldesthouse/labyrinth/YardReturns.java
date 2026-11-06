package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;

/** A saved meeting on the apron after private quiet rooms; every original continuation and return remains personal. */
public final class YardReturns {
    private YardReturns(){}
    public static final String STATE="yard_returns_0314";
    public static final LabyrinthPlace PLACE=LabyrinthPlace.YARD_REUNION;
    private static String source(LabyrinthData.Door d){return d.dimension.location()+"|"+d.id+"|"+d.lower.asLong();}
    private static CompoundTag own(LabyrinthData d,UUID id){return d.stateEntry(STATE,id.toString());}
    private static CompoundTag group(LabyrinthData d,String id){return d.stateEntry(STATE,"group/"+id);}
    /** Called only once a private room is ready to admit this reader, while their friends are still by the source door. */
    public static void reserve(ServerPlayer p,LabyrinthData.Door from){
        if(from.command||p.isSpectator())return;var d=LabyrinthData.get(p.server);var key=source(from);var at=Vec3.atBottomCenterOf(from.lower);
        var nearby=new LinkedHashMap<UUID,String>();
        for(var q:p.serverLevel().players())if(q.isAlive()&&!q.isSpectator()&&io.github.knaitoe.theoldesthouse.opening.OpeningSequence.state(q).enteredHouse()&&q.position().distanceTo(at)<=6)nearby.put(q.getUUID(),q.getGameProfile().getName());
        var saved=own(d,p.getUUID());String id=saved.getString("Group");var g=group(d,id);
        boolean joined=!id.isEmpty()&&g.getString("Source").equals(key)&&g.getCompound("Candidates").contains(p.getUUID().toString())
                &&(!saved.getBoolean("Entered")||saved.getBoolean("Returned"));
        if(!joined){
            if(nearby.size()<2)return;
            id=UUID.randomUUID().toString();g=new CompoundTag();g.putString("Source",key);g.putLong("Created",p.server.overworld().getGameTime());
            var candidates=new CompoundTag();nearby.forEach((u,n)->candidates.putString(u.toString(),n));g.put("Candidates",candidates);
            for(var u:nearby.keySet()){
                var member=own(d,u);if(member.getBoolean("Entered")&&!member.getBoolean("Returned"))continue;
                member=new CompoundTag();member.putString("Group",id);d.setStateEntry(STATE,u.toString(),member);
            }
        }
        saved=own(d,p.getUUID());saved.putString("Group",id);saved.putBoolean("Entered",true);saved.putBoolean("Returned",false);saved.remove("Arrived");saved.remove("ArrivalDoor");
        d.setStateEntry(STATE,p.getUUID().toString(),saved);d.setStateEntry(STATE,"group/"+id,g);
    }
    /** Bind a new apron node before the ordinary arrival machinery reads it. No discovered node is overwritten. */
    public static boolean departQuiet(ServerPlayer p,LabyrinthData.Door from,LabyrinthPlace next){
        var quiet=HouseReadSites.inside(p);if(quiet==null||!HouseReadStory.ready(p,quiet))return false;
        var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());var g=group(d,o.getString("Group"));
        if(!o.getBoolean("Entered")||o.getBoolean("Returned")||!source(from).equals(g.getString("Source"))||g.getCompound("Candidates").getAllKeys().size()<2)return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null||!LabyrinthBuilder.isPlaceReady(p.server,PLACE))return false;
        var members=new ArrayList<>(g.getCompound("Candidates").getAllKeys());Collections.sort(members);
        String side=members.indexOf(p.getUUID().toString())%2==0?"arrival_left":"arrival_right";var entry=d.door(PLACE.id()+"/"+side);
        var l=p.server.getLevel(HouseDimensions.OUTSIDE);
        if(entry==null||l==null||!(l.getBlockState(entry.lower).getBlock() instanceof DoorBlock))return false;
        var point=new LabyrinthData.Waypoint(from.dimension,Vec3.atBottomCenterOf(from.lower),from.facing.toYRot(),true);
        long key=d.arrivalKey(p.getUUID(),PLACE,point);String exit=PLACE.id()+"/exit";var remembered=d.node(p.getUUID(),key);
        if(remembered!=null&&(!remembered.containsKey(exit)||!remembered.get(exit).place().equals(next.id())))return false;
        if(remembered==null)d.rememberNode(p.getUUID(),key,Map.of(exit,new LabyrinthData.Deal(next.id(),false,false)));
        var route=new CompoundTag();route.putString("Door",entry.id);d.setStateEntry(STATE,"door/"+p.getUUID()+"/"+key,route);
        o.putString("ArrivalDoor",entry.id);o.putString("Next",next.id());d.setStateEntry(STATE,p.getUUID().toString(),o);
        LabyrinthDoors.continueFromRead(p,from,PLACE);return true;
    }
    public static @Nullable LabyrinthData.Door entry(ServerPlayer p,LabyrinthPlace place){
        if(place!=PLACE)return null;var d=LabyrinthData.get(p.server);
        String door=HouseReadSites.inside(p)!=null?own(d,p.getUUID()).getString("ArrivalDoor"):d.stateEntry(STATE,"door/"+p.getUUID()+"/"+d.nodeKey(p.getUUID(),place)).getString("Door");
        return d.door(door);
    }
    public static boolean fixedEntrance(LabyrinthData.Door d){return d.id.startsWith(PLACE.id()+"/");}
    public static boolean mayReturn(ServerPlayer p,LabyrinthData.Door door){
        if(!fixedEntrance(door)||!LabyrinthData.RETURN.equals(door.destination))return true;
        var mine=entry(p,PLACE);return mine==null?door.id.equals(PLACE.entryDoorId()):mine.id.equals(door.id);
    }
    public static void arrived(ServerPlayer p,LabyrinthPlace place){
        var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());
        if(place==PLACE&&o.contains("ArrivalDoor")){o.putBoolean("Arrived",true);d.setStateEntry(STATE,p.getUUID().toString(),o);}
        else if(o.getBoolean("Arrived")){o.putBoolean("Returned",true);o.remove("Arrived");o.remove("ArrivalDoor");d.setStateEntry(STATE,p.getUUID().toString(),o);}
    }
}
