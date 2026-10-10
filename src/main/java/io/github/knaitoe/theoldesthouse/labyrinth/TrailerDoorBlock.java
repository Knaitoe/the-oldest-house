package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/** Spruce-door collision and latch rules with a separate, bounded visual recoil. */
public final class TrailerDoorBlock extends DoorBlock implements EntityBlock {
    public static final MapCodec<TrailerDoorBlock> CODEC=simpleCodec(TrailerDoorBlock::new);
    public TrailerDoorBlock(BlockBehaviour.Properties p){super(BlockSetType.SPRUCE,p);}
    @Override public MapCodec<? extends DoorBlock> codec(){return CODEC;}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new TrailerDoorBlockEntity(p,s);}
}
