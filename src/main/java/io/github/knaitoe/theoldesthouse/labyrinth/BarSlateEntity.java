package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 0.4.450: what is chalked in one square of the league slate ({@link BarSlateBlock}): three short lines, in the game's own letters. */
public final class BarSlateEntity extends BlockEntity {
    public static final int LINES=3,LENGTH=40;
    private final String[] lines={"","",""};
    public BarSlateEntity(BlockPos pos,BlockState state){super(BarRegistry.SLATE_ENTITY.get(),pos,state);}
    public String line(int i){return i>=0&&i<LINES?lines[i]:"";}
    public boolean blank(){for(var line:lines)if(!line.isEmpty())return false;return true;}
    void chalk(ServerLevel l,String first,String second,String third){
        var next=new String[]{first,second,third};boolean changed=false;
        for(int i=0;i<LINES;i++){var text=next[i]==null?"":next[i].length()>LENGTH?next[i].substring(0,LENGTH):next[i];if(!text.equals(lines[i])){lines[i]=text;changed=true;}}
        if(!changed)return;
        setChanged();l.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);
    }
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);for(int i=0;i<LINES;i++)lines[i]=t.getString("Line"+i);}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);for(int i=0;i<LINES;i++)t.putString("Line"+i,lines[i]);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
    @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
