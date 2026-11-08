package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.351, The Last Crossing: the flood station's fittings, drawn facing north by tools/draw_last_crossing_art.py (a wall fitting hangs on
 * the wall south of its cell when it faces north, so it is set facing away from its wall).
 * <ul>
 * <li>TABLE: the porch table. CLIPBOARD: Vera's roll on its clipboard, blank (0) and signed (1). SLATE: the work slate, the story's source.</li>
 * <li>PLATE: the handprint plate in its brass bracket on a wall, and PLATE_SET: standing on its foot on a floor, each clear (0), fogged on
 * the near side by a clean palm (1), with a second print on the far side (2), and both (3). BRACKET: an empty bracket.</li>
 * <li>STOVE: the office stove with the kettle. GAUGE: the pump room's pressure gauge, its needle in the first, second or third band (0 to
 * 2) or settled (3). BRAKE: the brake lever, set (0) or held open (1).</li>
 * <li>VALVE: the numbered valves one to three (0 to 2) and the red bypass wheel (3). RUN: a length of the intake pipe. BLEED: the bleed
 * screw, shut (0) and open (1). GRATE: the service grate set as the floor, whole (0) and with its broken corner (1). PUMP: idle (0) and
 * running (1). PIPE: a speaking pipe's mouth.</li>
 * <li>ROLLER: a cable roller, seated (0) and unseated (1). FAIRLEAD: jammed (0) and clear (1). DRUM: the winch drum. HATCH: the
 * inspection hatch, shut (0) and open (1).</li>
 * <li>WHEEL: the ramp sluice's wheel, free (0), held (1) and pinned (2). PIN: the locking pin, up (0) and seated (1). COIL: the mooring
 * line, loose on the ramp (0), coiled on the porch (1) and staged at the ramp (2). HOOK: the lamp hook, empty (0) and with the lamp (1).
 * DEPTH: the depth post. NAMEPLATE: the ferry's brass nameplate, its left (0) and right (1) halves.</li>
 * <li>0.4.352, Night One: GRATE (2) with a hand come up through its broken corner; COIL (3), the line snapped at the first rise; BAR: the
 * cable house's door bar, leaning by the door (0) and dropped across the doorway (1), where it stops anything coming through; ISOLATOR:
 * the gauge room's isolation valve for the lower floor, open (0) and shut (1).</li>
 * <li>0.4.353, Day Two: BYPASS: a bypass wheel on its riser out of the floor, shut (0) and open (1), flooding its service branch. BRANCH: a
 * branch's grate set in a wall, and FLOOR_GRATE: set in a floor, dry (0), wet with the branch flooded behind it (1) and damp after (2).
 * WITNESS: the witness plate on the cable bed, greased over (0) and scraped clean (1). CATCH: the winch's parking catch beside the brake,
 * off (0) and set in the drum's teeth (1). STAIN: the water stain on the boards under the fairlead, fresh (0) and drying (1). TOOLS: the
 * cable tools, rolled in canvas, on the office desk.</li>
 * </ul>
 */
public final class CrossingBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<CrossingBlock> CODEC=simpleCodec(CrossingBlock::new);
    public enum Kind implements StringRepresentable {
        TABLE,CLIPBOARD,SLATE,PLATE,PLATE_SET,BRACKET,STOVE,GAUGE,BRAKE,VALVE,RUN,BLEED,GRATE,PUMP,PIPE,ROLLER,FAIRLEAD,DRUM,HATCH,WHEEL,PIN,COIL,HOOK,DEPTH,NAMEPLATE,BAR,ISOLATOR,
        BYPASS,BRANCH,FLOOR_GRATE,WITNESS,CATCH,STAIN,TOOLS;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public CrossingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.TABLE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return CrossingRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(CrossingRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static boolean plate(BlockState s){return is(s,Kind.PLATE)||is(s,Kind.PLATE_SET);}
    /** The lamp hung on its hook gives light; the pump's lamp a little while it runs. Read from the state's own properties: this is asked
     * while the block's states are being made, before the block is registered. */
    public static int light(BlockState s){if(!s.hasProperty(KIND))return 0;var k=s.getValue(KIND);int st=s.getValue(STAGE);return k==Kind.HOOK&&st==1?13:k==Kind.PUMP&&st==1?4:0;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    private static VoxelShape t(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return MarshBlock.turned(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case TABLE->Block.box(0,0,0,16,14,16);
            case CLIPBOARD->Block.box(3,0,2,13,1.5,14);
            case SLATE->t(f,0,2,13.8,16,14,16);
            case PLATE,BRACKET->t(f,1,1,14,15,15,16);
            case PLATE_SET->t(f,2,0,6,14,14,10);
            case STOVE->Block.box(2,0,3,14,16,13);
            case GAUGE->t(f,3,0,14.4,13,13,16);
            case BRAKE->Block.box(5,0,5,11,16,11);
            case VALVE->t(f,2,6,2,14,14,14);
            case RUN->t(f,0,5,5,16,11,11);
            case BLEED->t(f,0,4,5,16,15.5,11);
            case GRATE->Block.box(0,15,0,16,16,16);
            case PUMP->Block.box(1,0,1,15,16,15);
            case PIPE->t(f,5,0,10.6,11,16,16);
            case ROLLER->t(f,1,0,4,15,9,12);
            case FAIRLEAD->t(f,2,4,0,14,12,16);
            case DRUM->t(f,0,0,1,16,16,15);
            case HATCH->s.getValue(STAGE)==0?Block.box(0,14,0,16,16.5,16):t(f,0,14,0,16,16,16);
            case WHEEL->Block.box(0,0,0,16,11,16);
            case PIN->Block.box(4,0,5,12,16,11);
            case COIL->Block.box(1,0,0,15,5,14);
            case HOOK->t(f,5,3,6,11,15,16);
            case DEPTH->Block.box(6,0,6,10,16,10);
            case NAMEPLATE->t(f,0,5,15,16,11,16);
            case BAR->s.getValue(STAGE)==0?t(f,5,0,13,11,16,16):t(f,0,3,5,16,16,11);
            case ISOLATOR->t(f,4,4,11,12,16,16);
            case BYPASS->Block.box(2,0,2,14,14,14);
            case BRANCH,FLOOR_GRATE->Shapes.block();
            case WITNESS->Block.box(2,0,2,14,2,14);
            case CATCH->t(f,5,0,0,11,13,11);
            case STAIN->Block.box(0,0,0,16,.2,16);
            case TOOLS->t(f,2,0,5,16,3,11);
        };
    }
    /** The grate and the shut hatch are walked on; wall things, the clipboard and the coil are walked through; the rest stand. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case CLIPBOARD,GAUGE,PIPE,HOOK,NAMEPLATE,SLATE,COIL,ISOLATOR,WITNESS,STAIN,TOOLS->Shapes.empty();
            case BRANCH,FLOOR_GRATE->Shapes.block();
            case BYPASS->Block.box(6,0,6,10,13,10);
            case CATCH->Block.box(6,0,6,10,11,10);
            // The bar across the doorway stands higher than a jump, so nothing comes over it.
            case BAR->s.getValue(STAGE)==0?t(f,5,0,13,11,16,16):t(f,0,0,5,16,24,11);
            case TABLE->Shapes.or(Block.box(0,12,0,16,14,16),Block.box(1,0,1,15,12,15));
            case GRATE->Shapes.block();
            case HATCH->s.getValue(STAGE)==0?Shapes.block():Shapes.empty();
            case BRAKE->Block.box(5,0,5,11,3,11);
            case VALVE,RUN,BLEED->t(f,0,5,5,16,11,11);
            case WHEEL->Block.box(6,0,6,10,11,10);
            case PIN->Block.box(5,0,5,11,8,11);
            default->getShape(s,l,p,c);
        };
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player player,BlockHitResult hit){
        if(!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        if(LastCrossing.use(p,pos,s))return InteractionResult.CONSUME;
        p.displayClientMessage(net.minecraft.network.chat.Component.literal(LastCrossingTexts.look(s.getValue(KIND),s.getValue(STAGE))).withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);
        return InteractionResult.CONSUME;
    }
}
