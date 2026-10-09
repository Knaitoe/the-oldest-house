package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
/** Native collision and bounded model data, without display-entity swarms. */
public final class LiteraryPropBlock extends BaseEntityBlock {
    public static final MapCodec<LiteraryPropBlock> CODEC=simpleCodec(LiteraryPropBlock::new);
    public enum Kind implements StringRepresentable {CLOCK,COFFIN,FAN,MINIATURE,CAMERA,TRIPOD,LIGHT_STAND,TAPE_X,JAR,BELONGINGS,CYLINDER,PHOTO,TRUNK_MARK,ELK_HIDE,TELEVISION,RECEIVER,PAGE,LEDGER,
        // 0.4.50: the elk vignette's dead, drawn by the block entity renderer, and its small evidence.
        CARCASS,GUEST_BODY,CREW_BODY,STAIN,LIFE_RING,HARD_HAT;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public LiteraryPropBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CLOCK).setValue(STAGE,0));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected RenderShape getRenderShape(BlockState s){return s.getValue(KIND)==Kind.LEDGER?RenderShape.ENTITYBLOCK_ANIMATED:RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new LiteraryModelBlockEntity(p,s);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(KIND)){
        case CLOCK->Block.box(2,0,2,14,30,14);case COFFIN,ELK_HIDE->Block.box(1,0,0,15,9,16);
        case FAN->Block.box(6,11,6,10,16,10);case MINIATURE->Block.box(0,0,0,16,7,16);
        case CAMERA,TRIPOD,LIGHT_STAND->Block.box(5,0,5,11,23,11);
        case TAPE_X,PAGE->Block.box(0,0,0,16,1,16);case LEDGER->Block.box(2,0,3,14,2,13);case STAIN,LIFE_RING->Shapes.empty();case CARCASS->Block.box(0,0,0,16,13,16);case GUEST_BODY,CREW_BODY->s.getValue(STAGE)==2?Block.box(2,0,2,14,15,14):Block.box(0,0,0,16,5,16);case HARD_HAT->Block.box(3,0,3,13,5,13);case PHOTO,TRUNK_MARK->Block.box(1,0,13,15,16,16);
        case CYLINDER,JAR->Block.box(5,0,5,11,9,11);case BELONGINGS->Block.box(1,0,1,15,12,15);
        default->Block.box(1,0,1,15,16,15);};}
}
