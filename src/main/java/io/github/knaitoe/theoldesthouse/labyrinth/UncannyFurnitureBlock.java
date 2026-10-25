package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.core.*;
import net.minecraft.sounds.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** Fitted, native-scale furniture. Opening a door never creates food or changes an account. */
public final class UncannyFurnitureBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<UncannyFurnitureBlock> CODEC=simpleCodec(UncannyFurnitureBlock::new);
    public enum Kind implements StringRepresentable {
        BOOTH,TABLE,STOOL,COUNTER,CABINET,FRIDGE,COFFEE_MAKER,GRIDDLE,SINK,SHELF,FLUORESCENT,
        WINDOW_FRAME,SIGN_LEFT,SIGN_CENTER,SIGN_RIGHT,EXTERIOR_LAMP,SCONCE,ARMCHAIR,SOFA,DESK,
        SIDEBOARD,BOOKCASE,PICTURE,WARDROBE,BEDSIDE,HEADBOARD,HAMPER,RADIATOR,PIPE,PIPE_HORIZONTAL,
        VENT,SWITCHBOX,WORKBENCH,TOOL_RACK,MAILBOX,LOBBY_BENCH,DRAIN,BIN,DESK_LAMP,NAPKIN_CADDY,CUP,
        SOFA_LEFT,SOFA_RIGHT,COFFEE_TABLE,DINER_CHAIR;
        @Override public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
        public boolean seat(){return this==BOOTH||this==STOOL||this==ARMCHAIR||this==SOFA||this==SOFA_LEFT||this==SOFA_RIGHT||this==DINER_CHAIR||this==LOBBY_BENCH;}
        public double seatHeight(){return this==STOOL?13:9;}
        boolean opens(){return this==CABINET||this==FRIDGE||this==SIDEBOARD||this==WARDROBE||this==MAILBOX;}
        boolean switches(){return this==DESK_LAMP;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final BooleanProperty OPEN=BlockStateProperties.OPEN;
    public UncannyFurnitureBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.TABLE).setValue(FACING,Direction.NORTH).setValue(OPEN,false));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,OPEN);}
    public static BlockState of(Kind kind,Direction facing){return UncannyRegistry.FURNITURE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean isSeat(BlockState s){return s.is(UncannyRegistry.FURNITURE.get())&&s.getValue(KIND).seat();}
    public static int light(BlockState s){return switch(s.getValue(KIND)){
        case FLUORESCENT->s.getValue(OPEN)?0:12;case EXTERIOR_LAMP->9;case SCONCE->8;case DESK_LAMP->s.getValue(OPEN)?0:8;
        case SIGN_LEFT,SIGN_CENTER,SIGN_RIGHT->7;default->0;
    };}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var result=Shapes.empty();int turns=switch(s.getValue(FACING)){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
        for(var b:UncannyFurnitureShapes.boxes(s.getValue(KIND),s.getValue(OPEN))){
            double x0=b[0],z0=b[2],x1=b[3],z1=b[5];
            for(int i=0;i<turns;i++){double a=16-z1,e=16-z0;z0=x0;z1=x1;x0=a;x1=e;}
            result=Shapes.or(result,Block.box(x0,b[1],z0,x1,b[4],z1));
        }
        return result;
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos p){return 0;}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player user,BlockHitResult hit){
        var kind=s.getValue(KIND);
        if(!kind.opens()&&!kind.switches()||user.isSpectator()||!user.isAlive()||user.distanceToSqr(at.getCenter())>16)return InteractionResult.PASS;
        if(!l.isClientSide){
            l.setBlock(at,s.cycle(OPEN),3);
            l.playSound(null,at,kind.switches()?SoundEvents.LEVER_CLICK:s.getValue(OPEN)?SoundEvents.IRON_DOOR_CLOSE:SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,.22F,kind.switches()?(s.getValue(OPEN)?.6F:.5F):1.1F);
        }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
