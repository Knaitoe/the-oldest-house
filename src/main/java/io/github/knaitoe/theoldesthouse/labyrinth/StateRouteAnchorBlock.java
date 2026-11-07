package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;

/** 0.4.333: the highway's anchor, a stone under the median holding the road's one shared state (StateRouteScene). */
public final class StateRouteAnchorBlock extends BaseEntityBlock {
    public static final MapCodec<StateRouteAnchorBlock> CODEC=simpleCodec(StateRouteAnchorBlock::new);
    public StateRouteAnchorBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos at,BlockState s){return new StateRouteScene(at,s);}
    @Override public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return l.isClientSide?null:createTickerHelper(type,StateRouteRegistry.SCENE.get(),(w,at,st,scene)->scene.tick());}
}
