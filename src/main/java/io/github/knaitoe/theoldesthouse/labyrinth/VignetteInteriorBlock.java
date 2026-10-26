package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.util.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** Ordinary usable furnishings. Native saved states; no extra narration, loot or personal story gates. */
public final class VignetteInteriorBlock extends HorizontalDirectionalBlock {
 public static final MapCodec<VignetteInteriorBlock> CODEC=simpleCodec(VignetteInteriorBlock::new);
 public enum Kind implements StringRepresentable {
  SICKROOM_PLASTER,SICKROOM_BOARDS,CLAP_STARS,CLAP_BOARDS,HILL_ROSES,HILL_DADO,HILL_FLOOR,WORKSHOP_PLASTER,
  USHER_LINEN,USHER_BOARDS,VAULT_STONE,CRIMSON_SILK,CRIMSON_WALNUT,TRAILER_PAPER,TRAILER_VINYL,CONFESSION_PAPER,
  CONFESSION_OAK,WATCHROOM_FLOWERS,WATCHROOM_BOARDS,HOLIDAY_PINE,HOLIDAY_TILE,KAREN_PAPER,KAREN_CORK,
  MEDICINE_PRESS,TOY_CUPBOARD,LINEN_PRESS,MODEL_DRAWERS,USHER_PRESS,DICTATION_FILES,WATCHROOM_DRAWER,TRAILER_CUPBOARD,HOLIDAY_LARDER,SLIDE_CABINET,
  SICKROOM_WASHSTAND,TRAILER_SINK,HOLIDAY_SINK,BEDSIDE_LAMP,TASK_LAMP,KAREN_LAMP,HILL_LAMP,TOY_PIANO,ROCKING_HORSE,SEWING_BASKET,
  CRAFT_BENCH,DRAFTING_BOARD,MODEL_TOOLS,USHER_SPINET,CYLINDER_CABINET,CYLINDER_HORN,FOLDING_MUSIC_STAND,DICTATION_DESK,INK_SET,DESK_BELL,
  WATCHROOM_TRIPOD_CASE,WATCHROOM_TAPE,HOLIDAY_CROCKERY,KAREN_SLIDE_SORTER,CAMP_KIT,MODEL_PLAN,USHER_PORTRAIT,HILL_HEIGHT_MARKS,KAREN_NOTICEBOARD,CRIMSON_COAT_HOOK,
  CAMP_WORKTABLE,SEANCE_SIDEBOARD,TEA_SERVICE,PARLOUR_MUSIC_BOX,WATCHROOM_CUPBOARD,KITCHEN_WORKTABLE,CABIN_TABLE,CABIN_CHAIR,CABIN_STOOL,CABIN_FERN,DUMPSTER_LEFT,DUMPSTER_RIGHT;
  public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
  public boolean seat(){return this==CABIN_CHAIR||this==CABIN_STOOL;}
  public boolean surface(){return ordinal()<=KAREN_CORK.ordinal();}
  public boolean usable(){return switch(this){
   case MEDICINE_PRESS,TOY_CUPBOARD,LINEN_PRESS,MODEL_DRAWERS,USHER_PRESS,DICTATION_FILES,WATCHROOM_DRAWER,TRAILER_CUPBOARD,HOLIDAY_LARDER,SLIDE_CABINET,
    SICKROOM_WASHSTAND,TRAILER_SINK,HOLIDAY_SINK,BEDSIDE_LAMP,TASK_LAMP,KAREN_LAMP,HILL_LAMP,TOY_PIANO,ROCKING_HORSE,SEWING_BASKET,MODEL_TOOLS,USHER_SPINET,DESK_BELL,WATCHROOM_TRIPOD_CASE,WATCHROOM_TAPE,KAREN_SLIDE_SORTER,CAMP_KIT,PARLOUR_MUSIC_BOX,WATCHROOM_CUPBOARD,DUMPSTER_LEFT,DUMPSTER_RIGHT -> true;
   default -> false;};}
  public boolean lamp(){return this==BEDSIDE_LAMP||this==TASK_LAMP||this==KAREN_LAMP||this==HILL_LAMP;}
  public boolean wall(){return this==MODEL_PLAN||this==USHER_PORTRAIT||this==HILL_HEIGHT_MARKS||this==KAREN_NOTICEBOARD||this==CRIMSON_COAT_HOOK;}
 }
 public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
 public static final BooleanProperty ACTIVE=BooleanProperty.create("active");
 private static final Map<BlockState,VoxelShape> SHAPES=new HashMap<>();
 public VignetteInteriorBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.SICKROOM_PLASTER).setValue(ACTIVE,false));}
 @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,ACTIVE);}
 public static BlockState of(Kind k,Direction f){return LiteraryRegistry.INTERIOR.get().defaultBlockState().setValue(KIND,k).setValue(FACING,f);}
 public static boolean is(BlockState s,Kind k){return s.is(LiteraryRegistry.INTERIOR.get())&&s.getValue(KIND)==k;}
 public static int light(BlockState s){return s.getValue(KIND).lamp()&&s.getValue(ACTIVE)?11:s.getValue(KIND)==Kind.KAREN_SLIDE_SORTER&&s.getValue(ACTIVE)?7:0;}
 private static VoxelShape shape(BlockState state){return SHAPES.computeIfAbsent(state,s->{
  VoxelShape out=Shapes.empty();int turns=switch(s.getValue(FACING)){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
  for(double[] b:VignetteInteriorGeometry.boxes(s.getValue(KIND),s.getValue(ACTIVE))){double x0=b[0],z0=b[2],x1=b[3],z1=b[5];
   for(int i=0;i<turns;i++){double a=16-z1,c=16-z0;z0=x0;z1=x1;x0=a;x1=c;}out=Shapes.or(out,Block.box(x0,b[1],z0,x1,b[4],z1));
  }return out.optimize();});}
 @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s);}
 @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s);}
 @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND).surface()?Shapes.block():Shapes.empty();}
 @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return !s.getValue(KIND).surface();}
 @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){return interact(l,at,p);}
 /** Ordinary production use, also exercised directly by the native test. */
 public static InteractionResult interact(Level l,BlockPos at,Player p){
  var before=l.getBlockState(at);if(!before.is(LiteraryRegistry.INTERIOR.get())||!before.getValue(KIND).usable()||p.isSpectator())return InteractionResult.PASS;
  if(l.isClientSide)return InteractionResult.SUCCESS;
  var kind=before.getValue(KIND);boolean active=!before.getValue(ACTIVE);var next=before.setValue(ACTIVE,active);
  var added=Shapes.joinUnoptimized(next.getCollisionShape(l,at),before.getCollisionShape(l,at),BooleanOp.ONLY_FIRST);
  for(var box:added.toAabbs())if(!l.getEntities((Entity)null,box.move(at).inflate(.001),e->e.isAlive()&&!e.isSpectator()).isEmpty())return InteractionResult.CONSUME;
  l.setBlock(at,next,Block.UPDATE_ALL);
  var sound=kind.lamp()||kind==Kind.KAREN_SLIDE_SORTER?SoundEvents.LEVER_CLICK:
   kind==Kind.SICKROOM_WASHSTAND||kind==Kind.TRAILER_SINK||kind==Kind.HOLIDAY_SINK?(active?SoundEvents.BUCKET_EMPTY:SoundEvents.BUCKET_FILL):
   kind==Kind.DESK_BELL?SoundEvents.NOTE_BLOCK_BELL.value():
   active&&(kind==Kind.TOY_PIANO||kind==Kind.USHER_SPINET||kind==Kind.PARLOUR_MUSIC_BOX)?SoundEvents.NOTE_BLOCK_HARP.value():
   active?SoundEvents.BARREL_OPEN:SoundEvents.BARREL_CLOSE;
  l.playSound(null,at,sound,SoundSource.BLOCKS,.28F,kind==Kind.USHER_SPINET?.65F:1F);l.gameEvent(p,GameEvent.BLOCK_CHANGE,at);
  if(kind==Kind.DESK_BELL||kind==Kind.TOY_PIANO||kind==Kind.USHER_SPINET)l.scheduleTick(at,before.getBlock(),8);
  return InteractionResult.CONSUME;
 }
 @Override protected void tick(BlockState s,ServerLevel l,BlockPos at,RandomSource r){var k=s.getValue(KIND);if(k==Kind.DESK_BELL||k==Kind.TOY_PIANO||k==Kind.USHER_SPINET)l.setBlock(at,s.setValue(ACTIVE,false),Block.UPDATE_ALL);}
}
