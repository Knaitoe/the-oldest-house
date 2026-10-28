package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
/** Native bathroom fittings, kitchen controls and finite pull-out archive drawers. */
public final class ServiceFittingBlock extends BaseEntityBlock {
 public static final MapCodec<ServiceFittingBlock> CODEC=simpleCodec(ServiceFittingBlock::new);
 public enum Kind implements StringRepresentable {DRAWER,TOILET,BASIN,MIRROR,DRYER,SOAP,MICROWAVE,TOASTER,KETTLE,ROACH;public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
 public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
 public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
 public static final BooleanProperty ACTIVE=BlockStateProperties.POWERED;
 // Chunk meshing workers query collision as well as the native game thread.
 private static final java.util.Map<BlockState,VoxelShape> SHAPES=new java.util.concurrent.ConcurrentHashMap<>();
 public ServiceFittingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.DRAWER).setValue(FACING,Direction.NORTH).setValue(ACTIVE,false));}
 @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,ACTIVE);}
 public static BlockState of(Kind k,Direction d){return LiteraryRegistry.DETAIL.get().defaultBlockState().setValue(KIND,k).setValue(FACING,d);}
 @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
 private static VoxelShape shape(BlockState state){return SHAPES.computeIfAbsent(state,s->{
  if(s.getValue(KIND)==Kind.ROACH)return box(1,0,1,15,.6,15);var out=Shapes.empty();
  for(var b:ServiceFittingGeometry.parts(s.getValue(KIND),s.getValue(ACTIVE),0)){
   double x0=b.x0(),z0=b.z0(),x1=b.x1(),z1=b.z1();for(int i=0;i<VignetteHandlingBlock.turns(s.getValue(FACING));i++){double a=16-z1,c=16-z0;z0=x0;z1=x1;x0=a;x1=c;}out=Shapes.or(out,box(x0,b.y0(),z0,x1,b.y1(),z1));
  }return out.optimize();});}
 @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s);}
 @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(KIND)==Kind.ROACH?Shapes.empty():shape(s);}
 @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
 @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){return interact(l,at,p);}
 public static InteractionResult interact(Level l,BlockPos at,Player p){
  var s=l.getBlockState(at);if(!s.is(LiteraryRegistry.DETAIL.get())||p.isSpectator()||!p.isAlive()||p.distanceToSqr(at.getCenter())>36)return InteractionResult.PASS;
  var k=s.getValue(KIND);if(k==Kind.ROACH||k==Kind.MIRROR)return InteractionResult.PASS;if(l.isClientSide)return InteractionResult.SUCCESS;
  var next=s.cycle(ACTIVE);var added=Shapes.joinUnoptimized(shape(next),shape(s),BooleanOp.ONLY_FIRST);
  for(var a:added.toAabbs())if(!l.getEntities((net.minecraft.world.entity.Entity)null,a.move(at).inflate(.001),e->e.isAlive()&&!e.isSpectator()).isEmpty())return InteractionResult.CONSUME;
  if(k==Kind.DRAWER){if(l.getBlockEntity(at) instanceof ServiceFittingEntity d)p.openMenu(d);return InteractionResult.CONSUME;}
  l.setBlock(at,next,3);boolean on=next.getValue(ACTIVE);var sound=switch(k){case TOILET,BASIN->on?SoundEvents.BUCKET_EMPTY:SoundEvents.BUCKET_FILL;case DRYER->SoundEvents.FIRE_EXTINGUISH;case TOASTER->on?SoundEvents.LEVER_CLICK:SoundEvents.NOTE_BLOCK_HAT.value();default->SoundEvents.LEVER_CLICK;};
  l.playSound(null,at,sound,SoundSource.BLOCKS,.35F,k==Kind.TOILET?.65F:1.1F);if(on)l.scheduleTick(at,s.getBlock(),k==Kind.SOAP?8:k==Kind.TOILET?40:k==Kind.TOASTER?80:120);return InteractionResult.CONSUME;
 }
 @Override protected void tick(BlockState s,ServerLevel l,BlockPos at,RandomSource r){if(s.getValue(KIND)==Kind.DRAWER){if(l.getBlockEntity(at) instanceof ServiceFittingEntity d)d.recheck();return;}if(s.getValue(ACTIVE)){l.setBlock(at,s.setValue(ACTIVE,false),3);if(s.getValue(KIND)==Kind.TOASTER)l.playSound(null,at,SoundEvents.NOTE_BLOCK_HAT.value(),SoundSource.BLOCKS,.25F,1.5F);}}
 @Override public void animateTick(BlockState s,Level l,BlockPos at,RandomSource r){if(s.getValue(ACTIVE)&&(s.getValue(KIND)==Kind.KETTLE||s.getValue(KIND)==Kind.BASIN||s.getValue(KIND)==Kind.TOILET))l.addParticle(s.getValue(KIND)==Kind.KETTLE?ParticleTypes.CLOUD:ParticleTypes.SPLASH,at.getX()+.5,at.getY()+.8,at.getZ()+.5,0,.025,0);}
 @Override public BlockEntity newBlockEntity(BlockPos at,BlockState s){return new ServiceFittingEntity(at,s);}
 @Override protected void onRemove(BlockState s,Level l,BlockPos at,BlockState next,boolean moved){if((!next.is(this)||s.getValue(KIND)!=next.getValue(KIND))&&l.getBlockEntity(at) instanceof ServiceFittingEntity d&&!l.isClientSide)Containers.dropContents(l,at,d);super.onRemove(s,l,at,next,moved);}
 @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
