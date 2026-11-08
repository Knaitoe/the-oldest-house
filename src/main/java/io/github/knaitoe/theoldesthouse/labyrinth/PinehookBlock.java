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
 * 0.4.364, Last Weekend at Pinehook (Pinehook, docs/PINEHOOK_0_4_364.md): the cabin's fittings, drawn by tools/draw_pinehook_art.py facing
 * toward the room and turned by {@link #FACING}.
 * <ul>
 * <li>The stereo, off (0) or playing (1); the television and its tape deck, off (0), Casey's film (1), the rented film (2).</li>
 * <li>The breaker box, on (0) or tripped (1); a ceiling lamp, dark (0) or lit (1).</li>
 * <li>The stove, cold (0), cooking (1), done (2).</li>
 * <li>Jude's duffel, zipped (0), open (1), and open with the costume gone (2); the bag behind the shed, with the strap in it (0) or
 * without (1).</li>
 * <li>The cooler set down; the shelf of rented tapes; the first-aid kit on the bathroom wall, full (0) or taken (1).</li>
 * <li>0.4.365, the night: the rowboat's bow and stern (the stern empty (0), a tarp over a bundle (1), the tarp pulled back (2), empty
 * again (3)); wet footprints and dark spots on the ground or the planks; a coverall's tie caught on a post; Casey's bag in the bedroom,
 * shut with his plan in it (0) or open and empty (1); Len's safe, shut (0) or open (1); the portable player set down, stopped (0) or
 * recording (1).</li>
 * <li>0.4.366: the junction box on the cabin's side, its switch off (0), to the dock (1), to the back (2), or mended and feeding both (3);
 * a floodlight, dark (0) or lit (1); the brace bar by the boathouse door, leaning (0) or across the door (1); the slip gate in the
 * boathouse's lake wall, shut (0); the release rope's cleat by the dock, made fast (0) or pulled (1); the boathouse's wall rack, with oars
 * and hook (0), the oars gone (1), the hook gone (2), both gone (3); the paddle in the woodshed. The safe also opens empty (2), and the
 * rowboat's bow takes oars (1) or the paddle (2).</li>
 * </ul>
 */
public final class PinehookBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PinehookBlock> CODEC=simpleCodec(PinehookBlock::new);
    public enum Kind implements StringRepresentable {
        STEREO,TV,BREAKER,LAMP,STOVE,DUFFEL,BAG,COOLER,TAPES,KIT,BOAT_BOW,BOAT_STERN,PRINTS,BLOOD,TIE,SUITCASE,SAFE,RECORDER,JUNCTION,FLOOD,BRACE,GATE,RELEASE,RACK,PADDLE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public PinehookBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.LAMP).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing,int stage){return PinehookRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PinehookRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static int stage(BlockState s){return s.is(PinehookRegistry.FITTING.get())?s.getValue(STAGE):-1;}
    /** A lit lamp lights the room; a playing television glows. */
    public static int light(BlockState s){return switch(s.getValue(KIND)){case LAMP->s.getValue(STAGE)==1?13:0;case TV->s.getValue(STAGE)>=1?5:0;case STOVE->s.getValue(STAGE)==1?4:0;case FLOOD->s.getValue(STAGE)==1?15:0;default->0;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case STEREO->box(f,1,0,3,15,10,13);
            case TV->box(f,1,0,2,15,14,14);
            case BREAKER->box(f,4,2,13,12,14,16);
            case LAMP->Block.box(5,9,5,11,16,11);
            case STOVE->Block.box(0,0,0,16,15,16);
            case DUFFEL->box(f,2,0,4,14,6,12);
            case BAG->Block.box(3,0,3,13,7,13);
            case COOLER->box(f,1,0,3,15,11,13);
            case TAPES->box(f,0,0,9,16,16,16);
            case KIT->box(f,3,3,13,13,13,16);
            case BOAT_BOW,BOAT_STERN->Block.box(0,6,1,16,16,15);
            case PRINTS,BLOOD->Block.box(1,0,1,15,1,15);
            case TIE->Block.box(3,0,3,13,2,13);
            case SUITCASE->box(f,1,0,4,15,8,12);
            case SAFE->Block.box(1,0,1,15,16,15);
            case RECORDER->box(f,4,0,5,12,4,11);
            case JUNCTION->box(f,3,2,12,13,14,16);
            case FLOOD->box(f,4,4,8,12,12,16);
            case BRACE->s.getValue(STAGE)==1?box(f,0,6,0,16,10,4):box(f,6,0,12,10,16,16);
            case GATE->Block.box(0,0,6,16,16,10);
            case RELEASE->Block.box(5,0,5,11,12,11);
            case RACK->box(f,0,2,13,16,14,16);
            case PADDLE->box(f,6,0,12,10,16,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case STEREO,TV,STOVE,COOLER,TAPES,SUITCASE,SAFE,GATE->getShape(s,l,p,c);
            case BRACE->s.getValue(STAGE)==1?Block.box(0,0,0,16,16,4):Shapes.empty();
            default->Shapes.empty();
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return Pinehook.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
