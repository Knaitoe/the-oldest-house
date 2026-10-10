package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * What an older Drowned Town held, kept through its rebuild as Proofrock (0.4.67). Before the carve, the furnace, the
 * supply barrel, the three essay desks and the key desk are emptied into saved custody, so no block removal drops them;
 * any air door or soul sand a reader left in the lake is returned with the supplies. After the carve their exact contents
 * go into their new counterparts, nothing is restocked, and the canoe, the shore body and the hunter move to their new places.
 */
public final class TownCarry {
    static final String KEY="Carry0467";
    /** Where the old town kept them, in the order of Proofrock's new containers. */
    private static final BlockPos OLD_FURNACE=new BlockPos(13,0,-8),OLD_SUPPLIES=new BlockPos(14,0,-8),OLD_KEY=new BlockPos(-14,1,-32);
    private static final BlockPos[] OLD_PAPERS={new BlockPos(-23,1,-33),new BlockPos(-8,1,-28),new BlockPos(-19,1,-24)};
    private TownCarry(){}

    public static void capture(ServerLevel l,BlockPos b){
        var data=LabyrinthData.get(l.getServer());var state=data.state(DrownedTown.ID);
        // A carve interrupted by a restart has already taken custody; never take it twice.
        if(state.contains(KEY))return;
        var carry=new CompoundTag();
        take(l,b.offset(OLD_FURNACE),carry,"Furnace");take(l,b.offset(OLD_SUPPLIES),carry,"Supplies");take(l,b.offset(OLD_KEY),carry,"Key");
        for(int i=0;i<OLD_PAPERS.length;i++)take(l,b.offset(OLD_PAPERS[i]),carry,"Paper"+i);
        // Air tools a reader placed in the old lake come back with the supplies.
        var supplies=carry.getList("Supplies",Tag.TAG_COMPOUND);var placed=state.getCompound("PlacedAirTools");
        for(String key:placed.getAllKeys()){var at=BlockPos.of(Long.parseLong(key));var block=l.getBlockState(at);
            boolean door=placed.getString(key).equals("door")&&block.getBlock() instanceof DoorBlock&&block.getValue(DoorBlock.HALF)==DoubleBlockHalf.LOWER;
            boolean sand=placed.getString(key).equals("sand")&&block.is(Blocks.SOUL_SAND);
            if(door||sand){var tool=new CompoundTag();tool.putInt("Slot",-1);tool.put("Item",new ItemStack(block.getBlock().asItem()).save(l.registryAccess()));supplies.add(tool);}}
        carry.put("Supplies",supplies);state.remove("PlacedAirTools");
        state.put(KEY,carry);data.setState(DrownedTown.ID,state);
    }
    private static void take(ServerLevel l,BlockPos at,CompoundTag carry,String name){
        var list=new ListTag();
        if(l.getBlockEntity(at) instanceof Container box){
            for(int i=0;i<box.getContainerSize();i++){var stack=box.getItem(i);if(stack.isEmpty())continue;
                var entry=new CompoundTag();entry.putInt("Slot",i);entry.put("Item",stack.save(l.registryAccess()));list.add(entry);}
            box.clearContent();box.setChanged();
        }
        carry.put(name,list);
    }

    static void restore(ServerLevel l,BlockPos b,LabyrinthData data){
        var state=data.state(DrownedTown.ID);if(!state.contains(KEY))return;var carry=state.getCompound(KEY);
        give(l,b.offset(DrownedTown.FURNACE),carry.getList("Furnace",Tag.TAG_COMPOUND));
        give(l,b.offset(DrownedTown.SUPPLIES),carry.getList("Supplies",Tag.TAG_COMPOUND));
        give(l,b.offset(DrownedTown.KEY_DESK),carry.getList("Key",Tag.TAG_COMPOUND));
        for(int i=0;i<DrownedTown.PAPERS.length;i++)give(l,b.offset(DrownedTown.PAPERS[i]),carry.getList("Paper"+i,Tag.TAG_COMPOUND));
        // The shared actors keep their identities and are set down where the new town keeps them.
        if(state.hasUUID("TownCanoeUUID")&&l.getEntity(state.getUUID("TownCanoeUUID")) instanceof Entity canoe)
            canoe.moveTo(b.getX()+ProofrockTown.CANOE.getX()+.5,b.getY()-.25,b.getZ()+ProofrockTown.CANOE.getZ()+.5,90,0);
        if(state.hasUUID("ShoreBodyUUID")&&l.getEntity(state.getUUID("ShoreBodyUUID")) instanceof Entity body)
            body.moveTo(b.getX()+ProofrockTown.SHORE_BODY.getX()+.5,b.getY()+.03,b.getZ()+ProofrockTown.SHORE_BODY.getZ()+.5,32,0);
        for(var witch:l.getEntitiesOfClass(LakeWitchEntity.class,IndianLakeRooms.bounds(b,LabyrinthPlace.DROWNED_TOWN))){
            var at=DrownedTown.witchSpawn(l,b);witch.relocateLandscape(BlockPos.ZERO);witch.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5);witch.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);}
        state=data.state(DrownedTown.ID);state.remove(KEY);data.setState(DrownedTown.ID,state);
    }
    /** Exact slots where the new container has them; anything without room is set down beside it, not lost. */
    private static void give(ServerLevel l,BlockPos at,ListTag items){
        if(items.isEmpty())return;var box=l.getBlockEntity(at) instanceof Container c?c:null;
        for(int i=0;i<items.size();i++){var entry=items.getCompound(i);var stack=ItemStack.parseOptional(l.registryAccess(),entry.getCompound("Item"));if(stack.isEmpty())continue;
            int slot=entry.getInt("Slot");
            if(box!=null&&slot>=0&&slot<box.getContainerSize()&&box.getItem(slot).isEmpty()){box.setItem(slot,stack);continue;}
            if(box!=null){for(int s=0;s<box.getContainerSize()&&!stack.isEmpty();s++)if(box.getItem(s).isEmpty()){box.setItem(s,stack);stack=ItemStack.EMPTY;}}
            if(!stack.isEmpty())net.minecraft.world.Containers.dropItemStack(l,at.getX()+.5,at.getY()+1,at.getZ()+.5,stack);
        }
        if(box!=null)box.setChanged();
    }
}
