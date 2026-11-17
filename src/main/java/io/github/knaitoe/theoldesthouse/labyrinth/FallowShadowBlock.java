package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.680, Fallow's light (docs/FALLOW_0_4_680.md): a reader's shadow on the station's front wall, where the light threw it past them. The
 * block stands in the air before the wall and holds nothing a body meets; its {@link Shadow} keeps whose shadow it is, the House's body
 * they wore and how they stood when the light came, and the client draws that body flat on the wall in that stance (FallowShadowRenderer).
 * {@link #FACING} is the way the wall's face looks, toward the light.
 */
public final class FallowShadowBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<FallowShadowBlock> CODEC=simpleCodec(FallowShadowBlock::new);
    public FallowShadowBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    public static boolean is(BlockState s){return s.is(FallowRegistry.SHADOW.get());}
    public static BlockState facing(Direction d){return FallowRegistry.SHADOW.get().defaultBlockState().setValue(FACING,d);}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos,BlockState s){return new Shadow(pos,s);}

    /**
     * Whose shadow, and how they stood. {@code body} is the House's body they wore, null when the House's bodies are off (their own skin is
     * drawn then); {@code yaw} the way their body faced, in the world's degrees; {@code stride} how far into a step they were (0 standing,
     * up to 1 at a run) and {@code phase} where in it; {@code crouched} whether they were down low.
     */
    public static final class Shadow extends BlockEntity {
        public @Nullable UUID reader;public String name="";public @Nullable HouseBodies.Body body;
        public float yaw,stride,phase;public boolean crouched;
        public Shadow(BlockPos pos,BlockState s){super(FallowRegistry.SHADOW_ENTITY.get(),pos,s);}
        void cast(UUID reader,String name,@Nullable HouseBodies.Body body,float yaw,float stride,float phase,boolean crouched){
            this.reader=reader;this.name=name;this.body=body;this.yaw=yaw;this.stride=stride;this.phase=phase;this.crouched=crouched;
            setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);
        }
        @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){
            super.saveAdditional(t,r);if(reader==null)return;
            t.putUUID("Reader",reader);t.putString("Name",name);t.putFloat("Yaw",yaw);t.putFloat("Stride",stride);t.putFloat("Phase",phase);t.putBoolean("Crouched",crouched);
            if(body!=null)t.putIntArray("Body",new int[]{body.age(),body.presentation(),body.cut(),body.tone(),body.hair(),body.face()});
        }
        @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){
            super.loadAdditional(t,r);reader=t.hasUUID("Reader")?t.getUUID("Reader"):null;name=t.getString("Name");
            yaw=t.getFloat("Yaw");stride=t.getFloat("Stride");phase=t.getFloat("Phase");crouched=t.getBoolean("Crouched");
            var b=t.getIntArray("Body");body=b.length==6?new HouseBodies.Body(b[0],b[1],b[2],b[3],b[4],b[5]):null;
        }
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
        @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    }
}
