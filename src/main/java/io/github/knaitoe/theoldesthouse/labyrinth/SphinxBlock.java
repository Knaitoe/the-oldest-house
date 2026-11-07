package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.306, the Sphinx (docs/SPHINX_0_4_306.md): the fittings of her first crossing, drawn facing north (front to the room, back to its
 * wall) and turned by {@link #FACING}; drawn by tools/draw_sphinx_fittings.py in vanilla's own manner.
 * <ul>
 * <li>The umbrella stand: a brass-bound pot with two umbrellas and a stick.</li>
 * <li>A letter flap set low in a wall: shut (0), or open (1), a real hole to look through.</li>
 * <li>A door latch on the wall: down (0), lifted (1).</li>
 * <li>The stop pin by a door's frame: hidden in the stop (0), exposed (1), pulled out (2).</li>
 * <li>A grille high in a wall, the gap her voice comes through.</li>
 * <li>Her paw hooked over a door frame: heavy toes and dark hooked claws.</li>
 * <li>Her foreleg coming down a wall out of the dark (her eye is {@link SphinxEyeBlock}).</li>
 * <li>0.4.335, the stair: a stair mouth's iron gate, shut (0), released and swung back (1), or bowed out by her hand (2); the tray of
 * loose chips; a chip; the landing's iron grating; her foreleg laid up a flight.</li>
 * <li>0.4.338, the shelf: a stone shelf, settled (0) or lifted as she breathes (1); an iron survey pin in the far wall; the short ladder
 * stowed (0), lowered (1) or dropped toward a false shelf (2); the ladder lying across the gap.</li>
 * <li>0.4.334: a survey socket on the wall beside a choice, empty (0), with one reader's pin in it (1), or with two or more (2).</li>
 * </ul>
 */
public final class SphinxBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SphinxBlock> CODEC=simpleCodec(SphinxBlock::new);
    public enum Kind implements StringRepresentable {
        STAND,FLAP,LATCH,PIN,GRILLE,FINGERS,REACH,SURVEY,RAIL,TRAY,CHIP,GRATE,LIMB,SHELF,PEG,LADDER,SPAN;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,2);
    public SphinxBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.STAND).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.SPHINX.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(2,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.SPHINX.get())&&s.getValue(KIND)==kind;}
    public static int stage(BlockState s){return s.is(PuzzleRegistry.SPHINX.get())?s.getValue(STAGE):-1;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case STAND->Block.box(4,0,4,12,14,12);
            // Open, the flap is a frame round a real hole: the shape leaves the middle clear so the room beyond can be seen and pointed at.
            case FLAP->s.getValue(STAGE)==1?Shapes.or(box(f,0,0,0,16,4,16),box(f,0,12,0,16,16,16),box(f,0,0,0,3,16,16),box(f,13,0,0,16,16,16)):Shapes.block();
            case LATCH->box(f,5,3,13,11,13,16);
            case PIN->box(f,3,3,10,13,13,16); // the whole stop plate answers a hand, not just the pin's head
            case GRILLE->box(f,1,1,14,15,15,16);
            case FINGERS->box(f,0,4,9,16,16,16);
            case REACH->box(f,0,0,10,16,16,16);
            case SURVEY->box(f,4,3,11,12,13,16);
            case RAIL->switch(s.getValue(STAGE)){case 1->box(f,0,0,0,2,16,16);case 2->box(f,0,0,11,16,16,13);default->box(f,0,0,7,16,16,9);};
            case TRAY->Block.box(2,0,3,14,11,13);
            case CHIP->Block.box(6,0,6,10,2,10);
            case GRATE->Block.box(0,13,0,16,16,16);
            case LIMB->Block.box(2,0,0,14,13,16);
            case SHELF->s.getValue(STAGE)==1?Block.box(0,4,0,16,20,16):Shapes.block();
            case PEG->box(f,5,0,3,11,3,16);
            case LADDER->s.getValue(STAGE)==0?box(f,2,0,11,14,16,13):box(f,1,0,10,15,2,16);
            case SPAN->Block.box(0,13,0,16,16,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case STAND->getShape(s,l,p,c);
            case FLAP->getShape(s,l,p,c);
            // Shut, a stair's gate is as tall as a fence to a body: nobody steps over it.
            case RAIL->s.getValue(STAGE)==0?Shapes.or(getShape(s,l,p,c),box(s.getValue(FACING),0,16,7,16,24,9)):Shapes.empty();
            case TRAY,GRATE,SHELF,SPAN->getShape(s,l,p,c);
            default->Shapes.empty();
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return Sphinx.use(p,pos,s)||SphinxStair.use(p,pos,s)||SphinxShelf.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    /** A caught latch drops back. */
    @Override protected void tick(BlockState s,net.minecraft.server.level.ServerLevel l,BlockPos p,net.minecraft.util.RandomSource r){Sphinx.latchDown(l,p,s);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
