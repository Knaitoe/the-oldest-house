package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.413: a home cabinet's own high-score tables, one for each of its three games, ten places each, kept in its lower half and
 * saved with the world. Whoever plays it goes on them, not only the reader who made it.
 */
public final class HomeCabinetBlockEntity extends BlockEntity {
    private final List<List<PolybiusScores.Entry>> tables=new ArrayList<>();
    public HomeCabinetBlockEntity(BlockPos pos,BlockState state){
        super(PolybiusRegistry.HOME_CABINET_ENTITY.get(),pos,state);
        for(int g=0;g<PolybiusSite.BLACK;g++)tables.add(new ArrayList<>());
    }
    public List<PolybiusScores.Entry> table(int game){return game<0||game>=tables.size()?List.of():tables.get(game);}
    /** A finished game on this cabinet: the reader's best stands; their place is returned (1 for the top), or 0. */
    public int record(int game,ServerPlayer p,int score){
        if(game<0||game>=tables.size())return 0;
        int rank=PolybiusScores.record(tables.get(game),p,score);setChanged();return rank;
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.saveAdditional(tag,registries);
        for(int g=0;g<tables.size();g++)tag.put("Game"+g,PolybiusScores.write(tables.get(g)));
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.loadAdditional(tag,registries);
        for(int g=0;g<tables.size();g++){tables.get(g).clear();tables.get(g).addAll(PolybiusScores.read(tag.getList("Game"+g,Tag.TAG_COMPOUND)));}
    }
}
