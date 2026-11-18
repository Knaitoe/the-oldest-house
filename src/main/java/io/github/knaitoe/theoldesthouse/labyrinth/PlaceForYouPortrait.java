package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.691, A Place for You (docs/A_PLACE_FOR_YOU_0_4_691.md): the reader's picture copied onto the landing wall among the household's
 * older photographs. The block entity keeps the drawing (the same tag as the fridge picture: face, hands, hair, clothes, body, the face
 * the pencil drew); the client draws the page in a dark frame against the wall, or only the nail once it has been taken down.
 */
public final class PlaceForYouPortrait extends BaseEntityBlock {
    public static final MapCodec<PlaceForYouPortrait> CODEC=simpleCodec(PlaceForYouPortrait::new);
    public static final BooleanProperty HUNG=BooleanProperty.create("hung");
    public PlaceForYouPortrait(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING,Direction.EAST).setValue(HUNG,true));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(HorizontalDirectionalBlock.FACING,HUNG);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Portrait(p,s);}
    /** The frame against the wall behind it, or the nail alone; nothing to walk into either way. */
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        boolean hung=s.getValue(HUNG);double lo=hung?1.5:7,hi=hung?14.5:9,y0=hung?1.5:14.5,y1=hung?14.5:16,d=hung?2.4:1;
        return switch(s.getValue(HorizontalDirectionalBlock.FACING)){
            case SOUTH->Block.box(lo,y0,0,hi,y1,d);
            case NORTH->Block.box(16-hi,y0,16-d,16-lo,y1,16);
            case EAST->Block.box(0,y0,16-hi,d,y1,16-lo);
            default->Block.box(16-d,y0,lo,16,y1,hi);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    // A hand on it is the house's own click (PlaceForYou.click by position), like every fitting in it.

    public static final class Portrait extends BlockEntity {
        private CompoundTag read=new CompoundTag();
        public Portrait(BlockPos at,BlockState s){super(PlaceForYouRegistry.PORTRAIT_ENTITY.get(),at,s);}
        public CompoundTag read(){return read.copy();}
        public void setRead(CompoundTag tag){read=tag.copy();setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}
        @Override protected void saveAdditional(CompoundTag t,net.minecraft.core.HolderLookup.Provider r){super.saveAdditional(t,r);if(!read.isEmpty())t.put("Read",read);}
        @Override protected void loadAdditional(CompoundTag t,net.minecraft.core.HolderLookup.Provider r){super.loadAdditional(t,r);read=t.getCompound("Read");}
        @Override public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider r){return saveWithoutMetadata(r);}
        @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
    }
}
