package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.PortraitProfile;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The photograph stores the sitter's native skin profile on its own flat print. */
public final class HotelPhotoBlockEntity extends BlockEntity {
    private String sitter="";
    public HotelPhotoBlockEntity(BlockPos pos,BlockState state){super(HotelRegistry.PHOTO_ENTITY.get(),pos,state);}
    public PortraitProfile sitter(){return PortraitProfile.decode(sitter);}
    public void sitter(com.mojang.authlib.GameProfile profile){sitter=PortraitProfile.of(profile).encode();setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);String value=tag.getString("Sitter");sitter=PortraitProfile.decode(value)==null?"":value;}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);if(!sitter.isEmpty())tag.putString("Sitter",sitter);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
