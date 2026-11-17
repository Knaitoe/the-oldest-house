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

/** 0.4.678: the words on one lettered plate ({@link LetterPlates}): up to six lines, set by the scene's builder and never by a reader. */
public final class LetterPlateEntity extends BlockEntity {
    public static final int LINES=6,LENGTH=40;
    private final String[] lines={"","","","","",""};private String look="";
    public LetterPlateEntity(BlockPos pos,BlockState state){super(BoardRegistry.PLATE_ENTITY.get(),pos,state);}
    public String line(int i){return i>=0&&i<LINES?lines[i]:"";}
    /** Every line, in order, the empty ones left out. */
    public java.util.List<String> words(){var out=new java.util.ArrayList<String>();for(var line:lines)if(!line.isEmpty())out.add(line);return out;}
    /** What a hand on it reads: the words, and what they are written on. */
    public String look(){return look;}
    void letter(ServerLevel l,String look,String... next){
        boolean changed=!look.equals(this.look);this.look=look;
        for(int i=0;i<LINES;i++){var text=i<next.length&&next[i]!=null?next[i]:"";if(text.length()>LENGTH)text=text.substring(0,LENGTH);if(!text.equals(lines[i])){lines[i]=text;changed=true;}}
        if(!changed)return;
        setChanged();l.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);
    }
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);for(int i=0;i<LINES;i++)lines[i]=t.getString("Line"+i);look=t.getString("Look");}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);for(int i=0;i<LINES;i++)t.putString("Line"+i,lines[i]);t.putString("Look",look);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
    @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
