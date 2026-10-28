package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class HouseReadPropBlock extends BaseEntityBlock {
    public enum Part implements StringRepresentable {
        WOOL,TRENCH,BARN,WOOL_BOOTS,TRENCH_BOOTS,BARN_BOOTS,TOWEL,MIRROR,MIRROR_TOP,UMBRELLA,RADIATOR,CURTAIN,RAIL,SWING,MAILBOX,PLATE,COVERED_MIRROR;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final MapCodec<HouseReadPropBlock> CODEC=simpleCodec(HouseReadPropBlock::new);
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    public static final BooleanProperty TURNED=BooleanProperty.create("turned"),WRITING=BooleanProperty.create("writing");
    public HouseReadPropBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(PART,Part.WOOL).setValue(HorizontalDirectionalBlock.FACING,net.minecraft.core.Direction.SOUTH).setValue(TURNED,false).setValue(WRITING,false));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PART,HorizontalDirectionalBlock.FACING,TURNED,WRITING);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Fitting(p,s);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return HouseReadGeometry.shape(s);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(PART)){
        case CURTAIN,PLATE,TOWEL->Shapes.empty();default->getShape(s,l,p,c);};}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player player,BlockHitResult hit){
        if(player.isSpectator()||!player.isAlive()||player.distanceToSqr(at.getCenter())>36)return InteractionResult.PASS;
        if(l.isClientSide)return InteractionResult.SUCCESS;
        if(player instanceof ServerPlayer p)HouseReadSites.use(p,at,s.getValue(PART));return InteractionResult.CONSUME;
    }
    public static final class Fitting extends BlockEntity {public Fitting(BlockPos at,BlockState s){super(HouseReadRegistry.FITTING_ENTITY.get(),at,s);}}
}
