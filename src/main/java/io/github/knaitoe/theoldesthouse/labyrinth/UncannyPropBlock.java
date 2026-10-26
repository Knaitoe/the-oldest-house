package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
/** Collision belongs to the fitting's actual native shape; lighting never depends on a HUD effect. */
public final class UncannyPropBlock extends BaseEntityBlock {
    public static final MapCodec<UncannyPropBlock> CODEC=simpleCodec(UncannyPropBlock::new);
    public enum Kind implements StringRepresentable {
        PAINTED_BOARD,PAPER,RECEIPT,CLIPBOARD,LEDGER,SCRAWL,MONITOR,CAMERA,SIGNAL,SIGN,FLOODLIGHT,BUZZER,FALSE_DOOR,FLESH,REGISTER,PIE_CASE,PLATE,CLEAVER,BOX,STOPLIGHT,PULL_CHAIN,COURTYARD_PENDULUM,SUN_BOARD;
        @Override public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public UncannyPropBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.PAPER).setValue(FACING,Direction.SOUTH).setValue(STAGE,0));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,STAGE);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override public SoundType getSoundType(BlockState s,LevelReader l,BlockPos at,net.minecraft.world.entity.Entity entity){return s.getValue(KIND)==Kind.FLESH?UncannyFleshBlock.SOUNDS:super.getSoundType(s,l,at,entity);}
    public static int light(BlockState s){return switch(s.getValue(KIND)){case FLOODLIGHT->s.getValue(STAGE)==0?15:0;case SIGNAL,STOPLIGHT->10;case MONITOR->3;default->0;};}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        return switch(s.getValue(KIND)){
            case FLESH->Shapes.block();
            // The opened leaf reveals a solid black recess, not a route into the
            // unbuilt space outside the room. This also protects existing saves.
            case FALSE_DOOR->s.getValue(FACING).getAxis()==Direction.Axis.Z?box(0,0,6,16,16,10):box(6,0,0,10,16,16);
            case PAINTED_BOARD,SUN_BOARD->s.getValue(FACING).getAxis()==Direction.Axis.Z?box(0,0,6,16,16,10):box(6,0,0,10,16,16);
            case REGISTER,MONITOR->box(1,0,2,15,13,14);
            case PIE_CASE->box(0,0,0,16,15,16);
            case BOX->box(2,0,2,14,9,14);
            case PLATE->box(2,0,2,14,4,14);
            case PULL_CHAIN->box(5,1,6,11,16,10);
            case COURTYARD_PENDULUM->box(0,0,6,16,16,10);
            case PAPER,RECEIPT,CLIPBOARD,LEDGER,CLEAVER->box(1,0,2,15,1,14);
            default->box(2,3,6,14,15,10);
        };
    }
    @Override public BlockEntity newBlockEntity(BlockPos at,BlockState s){return new Fitting(at,s);}
    @Override protected void onRemove(BlockState old,Level l,BlockPos at,BlockState next,boolean moved){
        if(old.getValue(KIND)==Kind.PLATE&&(!next.is(this)||next.getValue(KIND)!=Kind.PLATE)&&l.getBlockEntity(at) instanceof Fitting f&&!f.meal.isEmpty()&&!l.isClientSide){var meal=f.meal;f.meal=net.minecraft.world.item.ItemStack.EMPTY;Block.popResource(l,at,meal);}
        super.onRemove(old,l,at,next,moved);
    }
    public static BlockState of(Kind kind,Direction face){return UncannyRegistry.PROP.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,face);}
    public static final class Fitting extends BlockEntity {
        public final FoyerMotion.Swing swing=new FoyerMotion.Swing();
        public net.minecraft.world.item.ItemStack meal=net.minecraft.world.item.ItemStack.EMPTY;
        public Fitting(BlockPos at,BlockState s){super(UncannyRegistry.FITTING.get(),at,s);}
        @Override protected void saveAdditional(net.minecraft.nbt.CompoundTag t,net.minecraft.core.HolderLookup.Provider r){super.saveAdditional(t,r);if(getBlockState().getValue(KIND)==Kind.COURTYARD_PENDULUM)t.put("CourtyardMotion131",swing.save());if(!meal.isEmpty())t.put("ServedMeal131",meal.save(r));}
        @Override protected void loadAdditional(net.minecraft.nbt.CompoundTag t,net.minecraft.core.HolderLookup.Provider r){super.loadAdditional(t,r);if(t.contains("CourtyardMotion131"))swing.load(t.getCompound("CourtyardMotion131"));meal=net.minecraft.world.item.ItemStack.parseOptional(r,t.getCompound("ServedMeal131"));}
        @Override public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider r){return saveWithoutMetadata(r);}
        @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
        public void syncMotion(){setChanged();if(level instanceof ServerLevel)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}
    }
}
