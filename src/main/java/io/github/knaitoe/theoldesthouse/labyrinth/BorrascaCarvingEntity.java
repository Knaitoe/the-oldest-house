package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Names use the native font; there is no generated texture or replacement actor per username. */
public final class BorrascaCarvingEntity extends BlockEntity {
    private String reader="",friend=BorrascaStory.FRIEND;
    public BorrascaCarvingEntity(BlockPos p,BlockState s){super(BorrascaRegistry.CARVING_ENTITY.get(),p,s);}
    public String reader(){return reader;}
    public String friend(){return friend;}
    public static void names(ServerLevel l,BlockPos at,String reader){
        if(l.getBlockEntity(at) instanceof BorrascaCarvingEntity e&&!e.reader.equals(reader)){
            e.reader=reader.substring(0,Math.min(40,reader.length()));e.friend=BorrascaStory.FRIEND;e.setChanged();
            l.sendBlockUpdated(at,e.getBlockState(),e.getBlockState(),Block.UPDATE_CLIENTS);
        }
    }
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);reader=t.getString("ReaderName");friend=t.contains("FriendName")?t.getString("FriendName"):BorrascaStory.FRIEND;}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putString("ReaderName",reader);t.putString("FriendName",friend);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
    @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
