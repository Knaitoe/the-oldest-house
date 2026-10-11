package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** Worn camp boards and purpose-built furnishings. Bunks have a real twelve-pixel crawl void underneath. */
public final class CampDecorBlock extends Block {
    public static final MapCodec<CampDecorBlock> CODEC=simpleCodec(CampDecorBlock::new);
    public enum Kind implements StringRepresentable {WALL,FLOOR,BED_HEAD,BED_FOOT,SIGN,NOTICE,FIRST_AID,OAR,RING,TRUNK,BOOTS,LANTERN;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}public boolean bed(){return this==BED_HEAD||this==BED_FOOT;}}
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,5);
    public CampDecorBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.WALL).setValue(FACING,Direction.NORTH).setValue(STAGE,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,STAGE);}
    public static BlockState of(Kind k,Direction facing,int stage){return LiteraryRegistry.CAMP_DECOR.get().defaultBlockState().setValue(KIND,k).setValue(FACING,facing).setValue(STAGE,Math.floorMod(stage,6));}
    public static boolean bed(BlockState s){return s.is(LiteraryRegistry.CAMP_DECOR.get())&&s.getValue(KIND).bed();}
    public static int light(BlockState s){return s.getValue(KIND)==Kind.LANTERN?11:0;}
    public static double[][] boxes(Kind k){return switch(k){
        case WALL,FLOOR->new double[][]{{0,0,0,16,16,16}};
        case BED_HEAD->new double[][]{{0,12,0,16,16,16},{0,0,0,2,21,2},{14,0,0,16,21,2},{2,17,0,14,21,2}};
        case BED_FOOT->new double[][]{{0,12,0,16,16,16},{0,0,14,2,17,16},{14,0,14,16,17,16},{2,15,14,14,17,16}};
        case SIGN->new double[][]{{1,3,14,15,11,16}};
        case NOTICE->new double[][]{{1,1,14,15,15,16}};
        case FIRST_AID->new double[][]{{3,3,11,13,12,16}};
        case OAR->new double[][]{{7,0,14,9,16,16},{5,10,13,11,16,15}};
        case RING->new double[][]{{3,3,13,6,13,16},{10,3,13,13,13,16},{6,3,13,10,6,16},{6,10,13,10,13,16}};
        case TRUNK->new double[][]{{1,0,2,15,7,14},{1,7,2,15,9,14}};
        case BOOTS->new double[][]{{3,0,4,6,3,11},{3,3,7,6,7,11},{10,0,4,13,3,11},{10,3,7,13,7,11}};
        case LANTERN->new double[][]{{4,1,4,12,11,12},{3,0,3,13,2,13},{3,11,3,13,13,13},{7,13,7,9,16,9}};
    };}
    public static VoxelShape shape(BlockState s){var out=new VoxelShape[]{Shapes.empty()};int turns=s.getValue(FACING)==Direction.EAST?1:s.getValue(FACING)==Direction.SOUTH?2:s.getValue(FACING)==Direction.WEST?3:0;
        for(var box:boxes(s.getValue(KIND))){double x0=box[0],z0=box[2],x1=box[3],z1=box[5];for(int i=0;i<turns;i++){double a=16-z1,b=x0,c=16-z0,d=x1;x0=a;z0=b;x1=c;z1=d;}
            out[0]=Shapes.or(out[0],Block.box(x0,box[1],z0,x1,box[4],z1));}return out[0];}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(KIND)){case SIGN,NOTICE,FIRST_AID,OAR,RING->Shapes.empty();default->shape(s);};}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND)==Kind.WALL||s.getValue(KIND)==Kind.FLOOR?Shapes.block():Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){if(!s.getValue(KIND).bed())return InteractionResult.PASS;
        if(p instanceof ServerPlayer reader)CampBedCrawl.begin(reader,pos);return InteractionResult.sidedSuccess(l.isClientSide());}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
