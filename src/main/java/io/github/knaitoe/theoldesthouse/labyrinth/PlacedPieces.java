package io.github.knaitoe.theoldesthouse.labyrinth;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** 0.4.758: where readers have set story pieces down in a level ({@link StoryPieces}); only these break like ordinary blocks. */
public final class PlacedPieces extends SavedData {
    private final LongOpenHashSet at=new LongOpenHashSet();
    private static PlacedPieces get(ServerLevel l){
        return l.getDataStorage().computeIfAbsent(new SavedData.Factory<>(PlacedPieces::new,PlacedPieces::load,null),"the_oldest_house_placed_pieces");
    }
    private static PlacedPieces load(CompoundTag tag,HolderLookup.Provider lookup){var p=new PlacedPieces();for(long v:tag.getLongArray("At"))p.at.add(v);return p;}
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider lookup){tag.putLongArray("At",at.toLongArray());return tag;}
    public static boolean has(ServerLevel l,BlockPos pos){return get(l).at.contains(pos.asLong());}
    public static void add(ServerLevel l,BlockPos pos){var p=get(l);if(p.at.add(pos.asLong()))p.setDirty();}
    public static void remove(ServerLevel l,BlockPos pos){var p=get(l);if(p.at.remove(pos.asLong()))p.setDirty();}
}
