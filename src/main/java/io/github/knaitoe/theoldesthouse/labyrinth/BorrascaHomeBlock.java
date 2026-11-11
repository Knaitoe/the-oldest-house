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
 * 0.4.456, the owner, note 12 of the playtest of 10 November: "You could add inflections of personality into the citizens homes.
 * Remember that this town is sick, that many know what the town does and why and just accept it. Some don't, but they have to
 * bite their tongue." The things that tell Drisking's four households apart (BorrascaHomes), drawn at sixteen pixels by
 * tools/draw_borrasca_homes.py.
 * <ul>
 * <li>A crib, a television in its cabinet and a suitcase stand on the floor and are solid.</li>
 * <li>A portrait, a cross and a row of hooks hang on the wall behind them (the model's south).</li>
 * <li>A Bible, an atlas and a picture frame on its face lie on a table or a chest of drawers, in the cell above it, with their
 * tops to the model's north: a reader reads them standing at the other side.</li>
 * </ul>
 * A thing's stage is its year: 0 on the childhood visits, 1 to a reader who comes back grown.
 */
public final class BorrascaHomeBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BorrascaHomeBlock> CODEC=simpleCodec(BorrascaHomeBlock::new);
    public enum Kind implements StringRepresentable {
        CRIB,PORTRAIT,CROSS,BIBLE,TV,JACKET,SUITCASE,ATLAS,FRAME_DOWN;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** On a wall, behind the model's south face. */
        public boolean wall(){return this==PORTRAIT||this==CROSS||this==JACKET;}
        /** Lying on a table or a chest of drawers, in the cell above it. */
        public boolean lies(){return this==BIBLE||this==ATLAS||this==FRAME_DOWN;}
        /** Whether it is different years on. */
        public boolean ages(){return this!=CROSS&&this!=FRAME_DOWN;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,1);
    public BorrascaHomeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CRIB).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing,int stage){return BorrascaRegistry.HOME.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(1,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(BorrascaRegistry.HOME.get())&&s.getValue(KIND)==kind;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case CRIB->MarshBlock.turned(f,1,0,3,14,13,13);
            case PORTRAIT->MarshBlock.turned(f,3,3,15,13,14,16);
            case CROSS->MarshBlock.turned(f,4,2,15,12,14,16);
            case BIBLE->MarshBlock.turned(f,2,0,3,14,2,13);
            case TV->s.getValue(STAGE)==0?MarshBlock.turned(f,1,0,5,15,14,14):Shapes.or(MarshBlock.turned(f,1,0,5,15,13,14),MarshBlock.turned(f,0,13,9,16,24,11));
            case JACKET->MarshBlock.turned(f,0,2,13,16,14,16);
            case SUITCASE->MarshBlock.turned(f,3,0,6,13,10.5,10);
            case ATLAS->MarshBlock.turned(f,1,0,2,15,2.5,14);
            case FRAME_DOWN->MarshBlock.turned(f,5,0,4,11,1,12);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case CRIB,SUITCASE->getShape(s,l,p,c);case TV->MarshBlock.turned(s.getValue(FACING),1,0,5,15,13,14);default->Shapes.empty();};
    }
}
