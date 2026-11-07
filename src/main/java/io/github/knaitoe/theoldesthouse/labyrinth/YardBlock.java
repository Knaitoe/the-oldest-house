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
 * 0.4.309, the House's yard (docs/YARD_TURNAROUND_0_4_309.md): the porch's bare bulb and the station wagon idling on the apron, block by
 * block, drawn facing north (the wagon's nose) by tools/draw_yard_fittings.py in vanilla's manner.
 * <ul>
 * <li>The porch bulb, bare, lit.</li>
 * <li>The wagon's nose: grille and two headlights, lit, the hood over them.</li>
 * <li>A length of the wagon's body: paint over wood-grain panelling, a wheel (stage 1) or none (0).</li>
 * <li>A length of the cabin: windows all round, the roof rack.</li>
 * <li>The tail: the tailgate and its lamps.</li>
 * <li>0.4.322: soft mud (stage 0), and the same mud with a reader's footprints pressed into it (stage 1), toes the way they walked.</li>
 * <li>0.4.345: a stockyard swing gate's leaf, three blocks high (stage 0, 1, 2 from the ground), a galvanised tube gate on oak with its
 * top rail painted, across the arm it faces along (YardStock).</li>
 * <li>0.4.345: a sheet of corrugated barn roofing, rust at its laps: on a roof (stage 0), its east edge lifted by the wind (1), and fallen
 * flat on the ground (2) (YardWind).</li>
 * </ul>
 */
public final class YardBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<YardBlock> CODEC=simpleCodec(YardBlock::new);
    public enum Kind implements StringRepresentable {
        BULB,NOSE,BODY,CABIN,TAIL,SHEET,PIPE_LIP,SURVEY_SLOT,GATE_LATCH,FLOOD_GRATE,PRINT,STOCK_LEAF,ROOF_SHEET;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public YardBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.BODY).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.YARD.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.YARD.get())&&s.getValue(KIND)==kind;}
    public static int light(BlockState s){return switch(s.getValue(KIND)){case BULB->14;case NOSE->12;case TAIL->4;default->0;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case BULB->Block.box(6,6,6,10,16,10);
            case CABIN->Block.box(0,0,0,16,12,16);
            case SHEET->s.getValue(STAGE)==0?Block.box(0,0,7,16,16,9):Block.box(0,0,7,5,16,9);
            case PIPE_LIP->Block.box(0,0,0,16,16,16);
            case SURVEY_SLOT->Shapes.or(Block.box(0,0,7,5,16,9),Block.box(11,0,7,16,16,9),Block.box(5,0,7,11,2,9),Block.box(5,14,7,11,16,9));
            case GATE_LATCH->WorkPrintBlock.box(s.getValue(FACING),3,5,12,13,11,16);
            case FLOOD_GRATE->WorkPrintBlock.box(s.getValue(FACING),0,0,14,16,16,16);
            case STOCK_LEAF->WorkPrintBlock.box(s.getValue(FACING),0,0,7,16,16,9);
            case ROOF_SHEET->Block.box(0,0,0,16,s.getValue(STAGE)==2?1:2,16);
            default->Block.box(0,0,0,16,14,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(KIND)==Kind.BULB||s.getValue(KIND)==Kind.SHEET?Shapes.empty():getShape(s,l,p,c);}
    @Override protected void entityInside(BlockState s,Level l,BlockPos at,net.minecraft.world.entity.Entity e){if(e instanceof ServerPlayer p&&s.getValue(KIND)==Kind.SHEET)YardConnectors.brush(p,at);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        if(YardFields.use(p,pos,s))return InteractionResult.CONSUME;
        if(YardStock.use(p,pos,s))return InteractionResult.CONSUME;
        if(s.getValue(KIND)==Kind.SHEET){YardConnectors.brush(p,pos);return InteractionResult.CONSUME;}
        if(s.getValue(KIND)==Kind.PIPE_LIP){if(YardCrawl.start(p))PuzzleRooms.say(p,"You get down onto the dry concrete lip.");return InteractionResult.CONSUME;}
        PuzzleRooms.say(p,switch(s.getValue(KIND)){
            case BULB->"A bare bulb over the back step. Someone leaves it on.";
            case PRINT->s.getValue(STAGE)==0?"Soft mud. Nothing has been through it.":"Footprints in the mud, the size of your own.";
            case ROOF_SHEET->s.getValue(STAGE)==2?"A sheet of barn roofing, bent where it hit the wire.":"Corrugated roofing, rusted along the laps. The nails have worked loose.";
            case STOCK_LEAF->"A swing gate on its post.";
            case NOSE->"The engine ticks over. Warm air comes off the grille. Nobody is at the wheel.";
            case CABIN->"Through the glass: a blanket on the back seat, a map folded wrong, the keys in the ignition.";
            case TAIL->"The tailgate is up. The driver's door, round the side, stands ajar.";
            default->"A station wagon, wood-panelled. Mud up the doors from the drive.";
        });
        return InteractionResult.CONSUME;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
