package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TrailerDoorBlockEntity extends BlockEntity {
    private long impactAt=Long.MIN_VALUE;
    private int strength,stress;
    public TrailerDoorBlockEntity(BlockPos at,BlockState s){super(GoatmanRegistry.DOOR_ENTITY.get(),at,s);}
    public void impact(int force){
        if(level==null||level.isClientSide)return;impactAt=level.getGameTime();strength=Mth.clamp(force,1,3);stress=Math.min(20,stress+strength);setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
    }
    public float recoil(float partial){
        if(level==null||impactAt==Long.MIN_VALUE)return 0;float t=level.getGameTime()-impactAt+partial;
        return t>=0&&t<12?Mth.sin(t*1.1F)*(1-t/12)*strength*.7F:0;
    }
    public int stress(){return stress;}
    public void resetStress(){impactAt=Long.MIN_VALUE;stress=0;strength=0;setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putInt("Stress",stress);}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);stress=Mth.clamp(t.getInt("Stress"),0,20);impactAt=Long.MIN_VALUE;strength=0;}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveAdditional(t,r);t.putLong("ImpactAt",impactAt);t.putInt("Strength",strength);return t;}
    @Override public void handleUpdateTag(CompoundTag t,HolderLookup.Provider r){loadAdditional(t,r);impactAt=t.getLong("ImpactAt");strength=Mth.clamp(t.getInt("Strength"),0,3);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
