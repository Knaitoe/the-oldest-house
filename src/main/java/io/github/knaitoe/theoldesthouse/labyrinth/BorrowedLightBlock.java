package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.199, the first puzzle room: Borrowed Light. One block for each of its working parts, each drawn facing north and turned
 * by {@link #FACING}: the lamp, its shutter with the brass catch, the dressing mirror on its scraped track, the plate over the
 * far door, the track's boards and the room's two dim sconces. {@link #TURNED}: the mirror's glass a further eighth of a turn
 * clockwise. {@link #OPEN}: the shutter latched open, the plate lit, a track board with the deep scrape of a stop.
 * Drawn by tools/draw_borrowed_light.py.
 */
public final class BorrowedLightBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<BorrowedLightBlock> CODEC=simpleCodec(BorrowedLightBlock::new);
    public enum Kind implements StringRepresentable {
        LAMP,SHUTTER,MIRROR,PLATE,TRACK,SCONCE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final BooleanProperty TURNED=BooleanProperty.create("turned");
    public static final BooleanProperty OPEN=BlockStateProperties.OPEN;
    public BorrowedLightBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.TRACK).setValue(TURNED,false).setValue(OPEN,false));}
    public static BlockState of(Kind kind,Direction facing){return PuzzleRegistry.BORROWED_LIGHT.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.BORROWED_LIGHT.get())&&s.getValue(KIND)==kind;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,TURNED,OPEN);}
    /** The lamp glows round its shutter; the sconces are dim; the plate, once lit, holds the light. */
    public static int light(BlockState s){return switch(s.getValue(KIND)){case LAMP->7;case SCONCE->6;case PLATE->s.getValue(OPEN)?11:0;default->0;};}
    /** The way the mirror's glass faces, one of eight: its facing, and an eighth of a turn clockwise when turned. */
    public static int[] normal(BlockState s){
        var f=s.getValue(FACING);int x=f.getStepX(),z=f.getStepZ();
        if(!s.getValue(TURNED))return new int[]{x,z};
        var r=f.getClockWise();return new int[]{x+r.getStepX(),z+r.getStepZ()};
    }
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return DareFittingBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case LAMP->box(f,2,2,4,14,14,16);
            case SHUTTER->s.getValue(OPEN)?Shapes.or(box(f,0,0,6,2,16,10),box(f,14,0,6,16,16,10)):box(f,0,0,6,16,16,10);
            case MIRROR->Block.box(2,0,2,14,24,14);
            case PLATE->box(f,3,3,14,13,13,16);
            case TRACK->Shapes.block();
            case SCONCE->box(f,5,4,12,11,12,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case PLATE,SCONCE->Shapes.empty();default->getShape(s,l,p,c);};
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return BorrowedLight.use(p,pos,s,hit.getLocation().y-pos.getY())?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState s){return s.getValue(KIND)==Kind.LAMP?new Beam(pos,s):null;}

    /** The lamp's beam: where it turns and where it ends, in the room's blocks, for the renderer. */
    public static final class Beam extends BlockEntity {
        private final List<BlockPos> points=new ArrayList<>();private final List<BlockPos> lit=new ArrayList<>();
        public Beam(BlockPos pos,BlockState s){super(PuzzleRegistry.BEAM.get(),pos,s);}
        public List<BlockPos> points(){return points;}
        List<BlockPos> lit(){return lit;}
        void set(List<BlockPos> path,List<BlockPos> glow){points.clear();points.addAll(path);lit.clear();lit.addAll(glow);setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}
        @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putLongArray("Path",points.stream().mapToLong(BlockPos::asLong).toArray());t.putLongArray("Lit",lit.stream().mapToLong(BlockPos::asLong).toArray());}
        @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);points.clear();for(long at:t.getLongArray("Path"))points.add(BlockPos.of(at));lit.clear();for(long at:t.getLongArray("Lit"))lit.add(BlockPos.of(at));}
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();t.putLongArray("Path",points.stream().mapToLong(BlockPos::asLong).toArray());return t;}
        @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    }
}
