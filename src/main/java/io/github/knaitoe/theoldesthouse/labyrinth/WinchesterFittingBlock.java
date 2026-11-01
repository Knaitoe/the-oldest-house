package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Winchester house's fittings (0.4.121): the séance table with its hand bell, a blue velvet side chair, the hall clock
 * (two blocks, its dial with thirteen hours), and what the unseen carpenters leave out: a sawhorse with a board half cut,
 * new lumber, a keg of nails with the hammer on it, the brass 13 beside a door, and (0.4.201) a spider-web window. tools/draw_winchester.py draws the
 * models, authored facing north.
 */
public final class WinchesterFittingBlock extends Block implements net.minecraft.world.level.block.EntityBlock {
    public static final MapCodec<WinchesterFittingBlock> CODEC=simpleCodec(WinchesterFittingBlock::new);
    public enum Kind implements StringRepresentable {
        SEANCE_TABLE,SEANCE_CHAIR,CLOCK,CLOCK_TOP,SAWHORSE,LUMBER,NAIL_KEG,PLAQUE,SPIDER_WINDOW,
        /** 0.4.202 (owner: "i don't know if this weirdly placed clutter is intentional"): her stockpile and the carpenters' work. */
        SPARE_DOOR,SPARE_WINDOW,STUDS,WORKBENCH,SAWDUST,
        /** 0.4.220 (owner: "custom chair textures", "review the furniture placement"): the house's own furniture. */
        DINING_CHAIR,TABLE_END,TABLE_MID,TABLE_PEDESTAL,CANDELABRA,BUFFET_L,BUFFET_R,SETTEE_L,SETTEE_R,WING_CHAIR,LAMP_TABLE,PIANO_L,PIANO_R,
        FERN_STAND,HALL_STAND,HALL_STAND_TOP,SEWING_MACHINE,DRESS_FORM,SCONCE,RUG_PERSIAN,RUNNER_EDGE,RUNNER_MID,RUNNER_NARROW,
        /** The hall console the construction ledger stands on, against the hall's west wall. */
        CONSOLE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public WinchesterFittingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.SEANCE_TABLE).setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    public static BlockState of(Kind kind,Direction facing){return LiteraryRegistry.WINCHESTER_FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean is(BlockState s,Kind kind){return s.is(LiteraryRegistry.WINCHESTER_FITTING.get())&&s.getValue(KIND)==kind;}

    private static VoxelShape base(Kind k){return switch(k){
        case SEANCE_TABLE->Shapes.or(Block.box(1,11,1,15,13,15),Block.box(5.5,0,5.5,10.5,11,10.5));
        case SEANCE_CHAIR->Shapes.or(Block.box(2.5,0,2.5,13.5,9.5,13.5),Block.box(3,9.5,12,13,16,13.5));
        case CLOCK->Shapes.or(Block.box(2,0,4,14,3,15),Block.box(3,3,5,13,16,14));
        case CLOCK_TOP->Shapes.or(Block.box(2,0,4,14,12,15),Block.box(1,12,3,15,14,16));
        case SAWHORSE->Block.box(0,0,4,16,13,12);
        case LUMBER->Block.box(0,0,1,16,6,15.5);
        case NAIL_KEG->Block.box(4,0,4,12,12,12);
        case PLAQUE->Block.box(4,5,15,12,12,16);
        // 0.4.201: the spider-web window, a leaded pane set flat against the wall behind it.
        case SPIDER_WINDOW->Block.box(0,0,15,16,16,16);
        case SPARE_DOOR->Block.box(2,0,8,14,16,16);
        case SPARE_WINDOW->Block.box(1,0,8,15,15,16);
        case STUDS->Block.box(0,0,6.5,16,16,9.5);
        case WORKBENCH->Block.box(0,0,1,16,15,15);
        case SAWDUST->Block.box(0,0,0,16,.5,16);
        case DINING_CHAIR->Shapes.or(Block.box(3,0,3,13,10.5,13),Block.box(3.5,10.5,11.5,12.5,16,13));
        case TABLE_END,TABLE_MID,TABLE_PEDESTAL->Block.box(0,12,0,16,15.5,16);
        case CANDELABRA->Block.box(3,0,6,13,13,10);
        case BUFFET_L,BUFFET_R->Block.box(0,0,3,16,15,16);
        case SETTEE_L,SETTEE_R->Shapes.or(Block.box(0,0,2,16,9.5,15),Block.box(0,9.5,12,16,16,15));
        case WING_CHAIR->Shapes.or(Block.box(1,0,2,15,9.5,14.5),Block.box(1,9.5,12,15,16,14.5));
        case LAMP_TABLE->Block.box(3,0,3,13,12,13);
        case PIANO_L,PIANO_R->Shapes.or(Block.box(0,0,7,16,16,16),Block.box(0,0,3,16,11,7));
        case FERN_STAND->Block.box(4.5,0,4.5,11.5,15,11.5);
        case HALL_STAND->Block.box(0,0,8,16,16,16);case HALL_STAND_TOP->Block.box(0,0,9.5,16,16,16);
        case SEWING_MACHINE->Block.box(0,0,2,16,12.5,14);case DRESS_FORM->Block.box(4,0,4,12,16,12);
        case SCONCE->Block.box(5.5,3,9,10.5,12,16);
        case CONSOLE->Block.box(0,0,5,16,16,16);
        case RUG_PERSIAN,RUNNER_EDGE,RUNNER_MID,RUNNER_NARROW->Block.box(0,0,0,16,.5,16);
    };}
    private static VoxelShape rotated(VoxelShape shape,Direction facing){
        if(facing==Direction.NORTH)return shape;var out=new VoxelShape[]{Shapes.empty()};int turns=facing.get2DDataValue()==0?2:facing.get2DDataValue()==1?3:facing.get2DDataValue()==3?1:0;
        shape.forAllBoxes((x0,y0,z0,x1,y1,z1)->{double a0=x0,b0=z0,a1=x1,b1=z1;for(int i=0;i<turns;i++){double na0=1-b1,nb0=a0,na1=1-b0,nb1=a1;a0=na0;b0=nb0;a1=na1;b1=nb1;}
            out[0]=Shapes.or(out[0],Shapes.box(a0,y0,b0,a1,y1,b1));});
        return out[0];
    }
    private static VoxelShape shape(BlockState s){
        var all=SHAPES.computeIfAbsent(s.getValue(KIND),x->new VoxelShape[4]);int i=s.getValue(FACING).get2DDataValue();
        if(all[i]==null)all[i]=rotated(base(s.getValue(KIND)),s.getValue(FACING));
        return all[i];
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(KIND)){case PLAQUE,SPIDER_WINDOW,SAWDUST,SCONCE,RUG_PERSIAN,RUNNER_EDGE,RUNNER_MID,RUNNER_NARROW,CANDELABRA->Shapes.empty();default->shape(s);};}
    /** 0.4.220: the clock's top has an entity, for its moving hands; 0.4.223: and the hall stand's top, for its mirror. */
    @Override public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos at,BlockState s){return s.getValue(KIND)==Kind.CLOCK_TOP||s.getValue(KIND)==Kind.HALL_STAND_TOP?new WinchesterClockEntity(at,s):null;}
    /** 0.4.223 (owner: "the chair should be sittable"): the house's chairs and its settee seat a reader, facing the way they face. */
    public static boolean seat(BlockState s){return s.is(LiteraryRegistry.WINCHESTER_FITTING.get())&&switch(s.getValue(KIND)){case SEANCE_CHAIR,DINING_CHAIR,WING_CHAIR,SETTEE_L,SETTEE_R->true;default->false;};}
    /** Where the seat is, in pixels: on the cushion, a little under its top. */
    public static double seatHeight(BlockState s){return switch(s.getValue(KIND)){case DINING_CHAIR->9.5;case SEANCE_CHAIR->9;default->8.5;}/16.0;}
    /**
     * 0.4.223 (owner: "the piano should be playable"): each key sounds where the hand falls on the keyboard, two octaves across the
     * piano's two halves, the low notes to the left of whoever stands at it, as on any piano. A note rises off the keys.
     */
    @Override protected net.minecraft.world.InteractionResult useWithoutItem(BlockState s,net.minecraft.world.level.Level l,BlockPos at,net.minecraft.world.entity.player.Player p,net.minecraft.world.phys.BlockHitResult hit){
        if(s.getValue(KIND)!=Kind.PIANO_L&&s.getValue(KIND)!=Kind.PIANO_R)return net.minecraft.world.InteractionResult.PASS;
        int note=note(l,at,s,hit.getLocation());
        if(!l.isClientSide){
            float pitch=(float)Math.pow(2,(note-12)/12.0);
            l.playSound(null,at,net.minecraft.sounds.SoundEvents.NOTE_BLOCK_HARP.value(),net.minecraft.sounds.SoundSource.RECORDS,.8f,pitch);
            if(l instanceof net.minecraft.server.level.ServerLevel server){var keys=hit.getLocation();server.sendParticles(net.minecraft.core.particles.ParticleTypes.NOTE,keys.x,at.getY()+1.15,keys.z,0,note/24.0,0,0,1);}
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(l.isClientSide);
    }
    /** The key under a point on the piano: 0 at the left of whoever faces it, 24 at the right. */
    public static int note(BlockGetter l,BlockPos at,BlockState s,net.minecraft.world.phys.Vec3 point){
        Direction right=s.getValue(FACING).getCounterClockWise();
        var other=l.getBlockState(at.relative(right));boolean left=other.is(s.getBlock())&&other.getValue(KIND)!=s.getValue(KIND)&&(other.getValue(KIND)==Kind.PIANO_L||other.getValue(KIND)==Kind.PIANO_R);
        BlockPos start=left?at:at.relative(right.getOpposite());
        var corner=net.minecraft.world.phys.Vec3.atCenterOf(start).subtract(right.getStepX()*.5,0,right.getStepZ()*.5);
        double across=(point.x-corner.x)*right.getStepX()+(point.z-corner.z)*right.getStepZ();
        return Math.max(0,Math.min(24,(int)Math.floor(across/2*25)));
    }
    /** The lamps and sconces give light. */
    public static int light(BlockState s){return switch(s.getValue(KIND)){case LAMP_TABLE->12;case SCONCE->11;case CANDELABRA->8;default->0;};}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
}
