package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;

/** 0.4.310: the road's anchor, a stone under the asphalt holding the scene's one shared state (DeerRoadScene). */
public final class DeerRoadAnchorBlock extends BaseEntityBlock {
    public static final MapCodec<DeerRoadAnchorBlock> CODEC=simpleCodec(DeerRoadAnchorBlock::new);
    public DeerRoadAnchorBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos at,BlockState s){return new DeerRoadScene(at,s);}
    @Override public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return l.isClientSide?null:createTickerHelper(type,DeerRoadRegistry.SCENE.get(),(w,at,st,scene)->scene.tick());}
}
