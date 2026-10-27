package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.*;

/** The actual occupied timber groans, sags and opens beneath the actual feet. Nothing heals on reload. */
public final class SiloTimberBlock extends BaseEntityBlock {
    public static final MapCodec<SiloTimberBlock> CODEC=simpleCodec(SiloTimberBlock::new);
    public enum Kind implements StringRepresentable {DOOR_LOW,DOOR_HIGH,TABLE,JOIST,SWING;public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}}
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STRAIN=IntegerProperty.create("strain",0,4);
    private static final Map<BlockState,VoxelShape> SHAPES=new java.util.concurrent.ConcurrentHashMap<>();
    public SiloTimberBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.DOOR_LOW).setValue(FACING,Direction.NORTH).setValue(STRAIN,0));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,STRAIN);}
    public static BlockState of(Kind k,Direction facing){return SiloRegistry.TIMBER.get().defaultBlockState().setValue(KIND,k).setValue(FACING,facing);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos at,BlockState s){return new Timber(at,s);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return SHAPES.computeIfAbsent(s,SiloTimberBlock::shape);}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos at){return Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    private static VoxelShape shape(BlockState s){
        VoxelShape out=Shapes.empty();int turns=s.getValue(FACING)==Direction.EAST?1:s.getValue(FACING)==Direction.SOUTH?2:s.getValue(FACING)==Direction.WEST?3:0;
        for(var original:SiloTimberShapes.boxes(s.getValue(KIND).ordinal(),s.getValue(STRAIN))){var q=original.clone();
            for(int t=0;t<turns;t++){double x=q[0],X=q[3];q[0]=16-q[5];q[3]=16-q[2];q[2]=x;q[5]=X;}
            out=Shapes.or(out,Block.box(q[0],q[1],q[2],q[3],q[4],q[5]));
        }return out.optimize();
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return l.isClientSide?null:createTickerHelper(type,SiloRegistry.TIMBER_ENTITY.get(),(world,at,state,wood)->wood.tick());}
    public static final class Timber extends BlockEntity {
        public double fatigue;public int rot;private boolean complained;
        public Timber(BlockPos at,BlockState s){super(SiloRegistry.TIMBER_ENTITY.get(),at,s);}
        public void sentenceFinished(){if(rot<8){rot++;setChanged();}}
        private void tick(){
            if(!(level instanceof ServerLevel l)||getBlockState().getValue(STRAIN)==4)return;
            var parts=getBlockState().getCollisionShape(l,worldPosition).toAabbs();double load=0;
            for(var body:l.getEntitiesOfClass(LivingEntity.class,new AABB(worldPosition).inflate(.4,.08,.4),e->e.isAlive()&&e.onGround()&&!e.isSpectator())){
                var feet=body.getBoundingBox();boolean touches=parts.stream().map(box->box.move(worldPosition)).anyMatch(box->Math.abs(feet.minY-box.maxY)<.045&&feet.maxX>box.minX+1e-5&&feet.minX<box.maxX-1e-5&&feet.maxZ>box.minZ+1e-5&&feet.minZ<box.maxZ-1e-5);
                if(touches)load+=Math.max(.1,Math.min(6,body.getBbWidth()*body.getBbHeight()/1.08));
            }
            if(load==0)return;fatigue=Math.min(100,fatigue+.08*load*(1+rot*.22));setChanged();
            if(!complained&&fatigue>=8){complained=true;l.playSound(null,worldPosition,SiloRegistry.GROAN.get(),SoundSource.BLOCKS,.6F,.85F);}
            int next=fatigue>=100?4:fatigue>=74?3:fatigue>=48?2:fatigue>=24?1:0;
            if(next>getBlockState().getValue(STRAIN)){
                l.setBlock(worldPosition,getBlockState().setValue(STRAIN,next),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
                l.playSound(null,worldPosition,next==4?SiloRegistry.SPLINTER.get():SiloRegistry.GROAN.get(),SoundSource.BLOCKS,next==4?.85F:.55F,.85F-next*.06F);
            }
        }
        @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);fatigue=t.getDouble("Fatigue");rot=t.getInt("Rot");complained=t.getBoolean("Complained");}
        @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putDouble("Fatigue",fatigue);t.putInt("Rot",rot);t.putBoolean("Complained",complained);}
    }
}
