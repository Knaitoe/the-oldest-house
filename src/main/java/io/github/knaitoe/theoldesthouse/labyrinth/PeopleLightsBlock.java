package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.253, After the People Lights Have Gone Off (docs/PEOPLE_LIGHTS_0_4_253.md): the house's own fittings, drawn by
 * tools/draw_people_lights_art.py. Models face north as drawn and turn with {@link #FACING}; a fitting against a wall has its back to
 * it. The wheelchair faces the way its sitter would. Stages: the bathroom mirror hung where Stan put it (0) or lowered where Kelly can
 * see herself (1); the hall runner laid (0) or rolled up at the wall (1); the cards on the table put away (0) or dealt (1).
 */
public final class PeopleLightsBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PeopleLightsBlock> CODEC=simpleCodec(PeopleLightsBlock::new);
    public enum Kind implements StringRepresentable {
        CHAIR,NEWEL,MIRROR,CASE,PHOTO,RAILING,MATTRESS,OFFER,PHONE,CARDS,GRAB_RAIL,RUNNER;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,1);
    public PeopleLightsBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CHAIR).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PeopleLightsRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(PeopleLightsRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(PeopleLightsRegistry.FITTING.get())?s.getValue(KIND):null;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case CHAIR->Block.box(2,0,1,14,15,15);
            case NEWEL->Block.box(4,0,4,12,16,12);
            case MIRROR->s.getValue(STAGE)==0?box(f,3,6,15,13,16,16):box(f,3,0,15,13,10,16);
            case CASE->Block.box(3,0,5,13,7,11);
            case PHOTO->box(f,3,3,15,13,13,16);
            case RAILING->box(f,0,0,12,16,16,16);
            case MATTRESS->Block.box(0,0,0,16,3,16);
            case OFFER,CARDS->Block.box(3,0,3,13,1,13);
            case PHONE->box(f,5,2,13,11,13,16);
            case GRAB_RAIL->box(f,1,5,13,15,9,16);
            case RUNNER->s.getValue(STAGE)==0?Block.box(0,0,0,16,1,16):box(f,1,0,10,15,5,15);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case MIRROR,PHOTO,OFFER,CARDS,PHONE,GRAB_RAIL,RUNNER,MATTRESS->Shapes.empty();
            // The loft's rail stands a block and a half to the hand, so nobody climbs over it.
            case RAILING->box(s.getValue(FACING),0,0,12,16,24,16);
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
