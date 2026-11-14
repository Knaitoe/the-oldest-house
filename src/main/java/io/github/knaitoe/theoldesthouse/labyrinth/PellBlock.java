package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.518, the Pell house: the street's and the house's own things, drawn facing north by tools/draw_pell_house.py and turned by
 * FACING.
 * <ul>
 * <li>FENCE: a paling fence across its cell; stage 1 a corner (an arm west and an arm north). Too tall to get over.</li>
 * <li>POOL_FENCE: the pool's black bars, the same two shapes. POOL_GATE: its child gate; stage 0 shut, 1 shut and padlocked, 2 open.</li>
 * <li>GATE: the side gate, a finger's gap planed under it; stage 1 open.</li>
 * <li>DOOR, GARAGE_DOOR, SHED_DOOR, SCREEN: a door's leaf. Stage bit 0 open, bit 1 the upper cell. The garage's has its bolt on its
 * north face (the yard's side) and none on the other. SCREEN is the front security door.</li>
 * <li>ROLLER: the garage's roller door, which is never opened.</li>
 * <li>WINDOW: a window in the wall's thickness. Stage 0 clear, 1 blinds down, 2 painted over from inside.</li>
 * <li>SLIDER: the glass sliding door. Stage bit 0 open, bit 1 the upper cell.</li>
 * <li>TARP: the pool's cover lying on the water; where it sags it holds one pond: stage 1 the pond's inside, 2 its edge with the dry
 * cloth on the side the cell faces, 3 a rounded corner of it, dry to the facing and to the left of it as drawn facing north.</li>
 * <li>LETTERBOX: a bent tin box on a post, the house's number in stick-on figures with one gone (the first paper is in it).
 * DOORMAT: the mat at the front step (where each reader's account waits), on the line; stage 1 its near edge lifted and folded
 * back, so the line shows running on under it.</li>
 * <li>WALL_CORNER: a corner of the house's outside wall, brick veneer on its north and west faces as drawn (the two it turns to the
 * weather). Stage 0 the veneer, 1 its lowest course. 0.4.573: the rest of the house's walls are PellWall, a finish to each face.</li>
 * <li>EAVE: the eaves: the upper half of its cell, a painted board on its edges, the soffit under it and tiles on top.</li>
 * </ul>
 */
public final class PellBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PellBlock> CODEC=simpleCodec(PellBlock::new);
    public enum Kind implements StringRepresentable {
        FENCE,POOL_FENCE,POOL_GATE,GATE,DOOR,GARAGE_DOOR,SHED_DOOR,SCREEN,ROLLER,WINDOW,SLIDER,TARP,LETTERBOX,DOORMAT,WALL_CORNER,EAVE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** A door of two cells whose stage's bit 0 is open and bit 1 the upper cell. */
        public boolean leaf(){return this==DOOR||this==GARAGE_DOOR||this==SHED_DOOR||this==SCREEN||this==SLIDER;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public PellBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.FENCE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PellRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PellRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    /** Whether a body can go through this cell: an open leaf, an open gate. */
    public static boolean open(BlockState s){
        if(!s.is(PellRegistry.FITTING.get()))return false;
        Kind k=s.getValue(KIND);int stage=s.getValue(STAGE);
        return k.leaf()?(stage&1)==1:k==Kind.GATE?stage==1:k==Kind.POOL_GATE&&stage==2;
    }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    private static VoxelShape plane(Direction f,double lo,double hi,double top){return MarshBlock.turned(f,0,0,lo,16,top,hi);}
    private static VoxelShape corner(Direction f,double lo,double hi,double top){return Shapes.or(MarshBlock.turned(f,0,0,lo,hi,top,hi),MarshBlock.turned(f,lo,0,0,hi,top,lo));}
    /** Swung back against the west jamb (as the model is drawn, facing north). */
    private static VoxelShape swung(Direction f,double thick,double top){return MarshBlock.turned(f,0,0,0,thick,top,16);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case FENCE->stage==1?corner(f,7,9,16):plane(f,7,9,16);
            case POOL_FENCE->stage==1?corner(f,7,9,16):plane(f,7,9,16);
            case POOL_GATE->stage==2?swung(f,1,16):plane(f,7,9,16);
            case GATE->stage==1?swung(f,2,16):plane(f,7,9,16);
            case DOOR,GARAGE_DOOR,SHED_DOOR,SCREEN->(stage&1)==1?swung(f,2,16):plane(f,7,9,16);
            case ROLLER->plane(f,6,10,16);
            case WINDOW->plane(f,6,10,16);
            case SLIDER->(stage&1)==1?MarshBlock.turned(f,0,(stage&2)==2?15:0,7,16,(stage&2)==2?16:.5,9):plane(f,7,9,16);
            case TARP->Block.box(0,12,0,16,13.5,16);
            case LETTERBOX->Shapes.or(Block.box(7,0,7,9,10,9),MarshBlock.turned(f,4,10,5,12,15,11));
            case DOORMAT->MarshBlock.turned(f,1,0,3,15,1,13);
            case WALL_CORNER->Shapes.block();
            case EAVE->Block.box(0,8,0,16,16,16);
        };
    }
    /**
     * What stops a body. A fence is too tall to get over (a cell and a half, as a fence is); a shut leaf or gate stops, an open one
     * does not; a window stops; the pool's cover holds nobody up.
     */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case FENCE,POOL_FENCE->stage==1?corner(f,7,9,24):plane(f,7,9,24);
            case POOL_GATE->stage==2?Shapes.empty():plane(f,7,9,24);
            case GATE->stage==1?Shapes.empty():plane(f,7,9,24);
            case DOOR,GARAGE_DOOR,SHED_DOOR,SCREEN,SLIDER->(stage&1)==1?Shapes.empty():plane(f,7,9,16);
            case ROLLER,WINDOW->plane(f,6,10,16);
            case TARP,DOORMAT->Shapes.empty();
            case LETTERBOX->Shapes.or(Block.box(7,0,7,9,10,9),MarshBlock.turned(f,4,10,5,12,15,11));
            case WALL_CORNER->Shapes.block();
            case EAVE->Block.box(0,8,0,16,16,16);
        };
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){var k=s.getValue(KIND);return k!=Kind.WALL_CORNER&&k!=Kind.EAVE;}
}
