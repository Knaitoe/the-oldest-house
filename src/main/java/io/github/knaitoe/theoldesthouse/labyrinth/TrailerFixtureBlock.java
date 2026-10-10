package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** Small native porcelain fixtures and a glazed awning sash. */
public final class TrailerFixtureBlock extends Block {
    public enum Kind { TOILET,SINK,WINDOW,COOLER,WHEEL }
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN=BlockStateProperties.OPEN,FILLED=BooleanProperty.create("filled");
    private final Kind kind;
    public TrailerFixtureBlock(Kind kind,BlockBehaviour.Properties properties){
        super(properties);this.kind=kind;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(OPEN,false).setValue(FILLED,false));
    }
    @Override protected MapCodec<? extends Block> codec(){return simpleCodec(p->new TrailerFixtureBlock(kind,p));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,OPEN,FILLED);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        if(kind==Kind.COOLER)return box(1,0,1,15,14,15);
        if(kind==Kind.WHEEL)return s.getValue(FACING).getAxis()==Direction.Axis.X?Shapes.or(box(5,0,4,11,16,12),box(5,4,0,11,12,16)):Shapes.or(box(4,0,5,12,16,11),box(0,4,5,16,12,11));
        if(kind==Kind.WINDOW){
            if(s.getValue(OPEN))return s.getValue(FACING).getAxis()==Direction.Axis.X?
                Shapes.or(box(7,0,0,9,16,1),box(7,0,15,9,16,16),box(7,0,1,9,1,15),box(7,15,1,9,16,15)):
                Shapes.or(box(0,0,7,1,16,9),box(15,0,7,16,16,9),box(1,0,7,15,1,9),box(1,15,7,15,16,9));
            return s.getValue(FACING).getAxis()==Direction.Axis.X?box(7,0,0,9,16,16):box(0,0,7,16,16,9);
        }
        return kind==Kind.TOILET?box(2,0,2,14,16,15):box(2,8,2,14,16,15);
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        if(kind==Kind.WINDOW&&s.getValue(OPEN))return Shapes.empty();return getShape(s,l,at,c);
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){
        if(kind==Kind.WHEEL)return InteractionResult.PASS;
        if(kind==Kind.COOLER)return InteractionResult.sidedSuccess(l.isClientSide);
        if(!l.isClientSide){
            if(kind==Kind.WINDOW)l.setBlock(at,s.cycle(OPEN),Block.UPDATE_ALL);
            else if(kind==Kind.SINK)l.setBlock(at,s.cycle(FILLED),Block.UPDATE_ALL);
            l.playSound(null,at,kind==Kind.WINDOW?SoundEvents.WOODEN_TRAPDOOR_CLOSE:SoundEvents.BUCKET_EMPTY,SoundSource.BLOCKS,.45F,kind==Kind.TOILET?.75F:1F);
        }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
