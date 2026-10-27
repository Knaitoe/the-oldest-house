package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/** A finite household paper, drawing, photograph or inscription. No personal quest state. */
public final class DomesticRecordBlock extends BaseEntityBlock {
    public static final MapCodec<DomesticRecordBlock> CODEC=simpleCodec(DomesticRecordBlock::new);
    public enum Kind implements StringRepresentable { NOTE,DRAWING,PHOTO,AWARD,RECIPE,FAMILY_DRAWING,COUPLE;
        public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}}
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty PRESENT=BooleanProperty.create("present");
    public DomesticRecordBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.NOTE).setValue(FACING,Direction.NORTH).setValue(PRESENT,true));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,PRESENT);}
    public static BlockState of(Kind k,Direction f){return DomesticRegistry.RECORD.get().defaultBlockState().setValue(KIND,k).setValue(FACING,f);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos at,BlockState s){return new DomesticRecordBlockEntity(at,s);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        var k=s.getValue(KIND);if(k==Kind.NOTE||k==Kind.RECIPE)return Block.box(2,0,2,14,1,14);
        if(k==Kind.AWARD)return Block.box(3,0,3,13,14,13);
        return switch(s.getValue(FACING)){case EAST->Block.box(0,2,2,2,14,14);case SOUTH->Block.box(2,2,0,14,14,2);case WEST->Block.box(14,2,2,16,14,14);default->Block.box(2,2,14,14,14,16);};
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return Shapes.empty();}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos at){return Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){
        if(p.isSpectator()||!p.isAlive()||p.distanceToSqr(at.getCenter())>25)return InteractionResult.PASS;
        if(l.isClientSide)return InteractionResult.SUCCESS;
        if(p instanceof ServerPlayer reader&&l.getBlockEntity(at) instanceof DomesticRecordBlockEntity paper)paper.open(reader);
        return InteractionResult.CONSUME;
    }
}
