package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/** Five native objects, with actual per-key, tray, piece and transport handling. */
public final class VignetteHandlingBlock extends HorizontalDirectionalBlock implements EntityBlock {
 public static final MapCodec<VignetteHandlingBlock> CODEC=simpleCodec(VignetteHandlingBlock::new);
 public enum Kind implements StringRepresentable {TOY_PIANO,SPINET,SLIDES,MODEL_KIT,TAPE;public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
 public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
 public static final BooleanProperty ACTIVE=BooleanProperty.create("active");
 public VignetteHandlingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.TOY_PIANO).setValue(FACING,Direction.NORTH).setValue(ACTIVE,false));}
 @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,ACTIVE);}
 @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
 public static Kind replacement(VignetteInteriorBlock.Kind k){return switch(k){case TOY_PIANO->Kind.TOY_PIANO;case USHER_SPINET->Kind.SPINET;case KAREN_SLIDE_SORTER->Kind.SLIDES;case MODEL_TOOLS->Kind.MODEL_KIT;case WATCHROOM_TAPE->Kind.TAPE;default->null;};}
 public static BlockState of(Kind k,Direction d){return LiteraryRegistry.HANDLING.get().defaultBlockState().setValue(KIND,k).setValue(FACING,d);}
 public static int turns(Direction d){return switch(d){case EAST->1;case SOUTH->2;case WEST->3;default->0;};}
 public static Vec3 local(BlockPos at,Direction d,Vec3 point){double x=point.x-at.getX(),z=point.z-at.getZ();for(int i=0;i<turns(d);i++){double t=x;x=z;z=1-t;}return new Vec3(x,point.y-at.getY(),z);}
 public static VoxelShape shape(BlockState s,VignetteHandlingGeometry.View view){
  VoxelShape out=Shapes.empty();for(var b:VignetteHandlingGeometry.parts(s.getValue(KIND),view,0)){
   double x0=b.x0(),z0=b.z0(),x1=b.x1(),z1=b.z1();
   // Moving reel spokes remain within their solid reel guard, so the guard is their collision envelope.
   if(b.yaw()!=0){double cx=(x0+x1)/2,cz=(z0+z1)/2,r=Math.hypot(x1-x0,z1-z0)/2;x0=cx-r;x1=cx+r;z0=cz-r;z1=cz+r;}
   for(int t=0;t<turns(s.getValue(FACING));t++){double a=16-z1,c=16-z0;z0=x0;z1=x1;x0=a;x1=c;}
   out=Shapes.or(out,Block.box(x0,b.y0(),z0,x1,b.y1(),z1));
  }return out.optimize();
 }
 @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s,l.getBlockEntity(p) instanceof VignetteHandlingBlockEntity b?b.view():VignetteHandlingGeometry.View.initial());}
 @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return getShape(s,l,p,c);}
 @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
 @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){if(p.isSpectator())return InteractionResult.PASS;if(l.isClientSide)return InteractionResult.SUCCESS;return l.getBlockEntity(at) instanceof VignetteHandlingBlockEntity b?b.use(p,local(at,s.getValue(FACING),hit.getLocation())):InteractionResult.PASS;}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new VignetteHandlingBlockEntity(p,s);}
 @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return t==LiteraryRegistry.HANDLING_ENTITY.get()?(world,pos,state,be)->((VignetteHandlingBlockEntity)be).tick():null;}
 @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
