package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** No wall clock or catch-up: the server calls advance once per occupied well tick. */
public final class WellCoverBlockEntity extends BlockEntity {
    public static final int CLOSE_TICKS=160;
    private int progress,direction;
    private long receivedAt;
    public WellCoverBlockEntity(BlockPos p,BlockState s){super(NovelRegistry.WELL_COVER_ENTITY.get(),p,s);}
    public int progress(){return progress;}
    public float closure(float partial){
        float dt=level!=null&&level.isClientSide?Mth.clamp(level.getGameTime()-receivedAt+partial,0,5):0;
        float t=Mth.clamp((progress+direction*dt)/CLOSE_TICKS,0,1);return t*t*(3-2*t);
    }
    public void advance(boolean occupied,boolean close){
        if(level==null||level.isClientSide)return;
        int old=progress,step=occupied?(close?1:-4):0;
        // The actual horizontal plate never closes through an explorer, spectator camera body or pet.
        if(step>0&&progress>=CLOSE_TICKS-1&&!level.getEntitiesOfClass(LivingEntity.class,
                new AABB(worldPosition.getX()+.01,worldPosition.getY()+.81,worldPosition.getZ()+.01,worldPosition.getX()+.99,worldPosition.getY()+1.01,worldPosition.getZ()+.99),e->e.isAlive()).isEmpty())step=0;
        progress=Mth.clamp(progress+step,0,CLOSE_TICKS);
        boolean open=progress<CLOSE_TICKS;
        if(getBlockState().getValue(WellCoverBlock.OPEN)!=open)level.setBlock(worldPosition,getBlockState().setValue(WellCoverBlock.OPEN,open),3);
        boolean changed=direction!=step;direction=step;
        if(old!=progress||changed){setChanged();if(changed||progress%5==0||progress==0||progress==CLOSE_TICKS)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    }
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putInt("OccupiedClosure",progress);}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);progress=Mth.clamp(t.getInt("OccupiedClosure"),0,CLOSE_TICKS);direction=level!=null&&level.isClientSide?Mth.clamp(t.getInt("Direction"),-4,1):0;receivedAt=level==null?0:level.getGameTime();}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveAdditional(t,r);t.putInt("Direction",direction);return t;}
    @Override public void handleUpdateTag(CompoundTag t,HolderLookup.Provider r){loadAdditional(t,r);direction=Mth.clamp(t.getInt("Direction"),-4,1);receivedAt=level==null?0:level.getGameTime();}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public AABB getRenderBoundingBox(){return new AABB(worldPosition).inflate(1,3,1);}
}
