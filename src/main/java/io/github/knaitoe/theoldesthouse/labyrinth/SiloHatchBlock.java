package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;

/** An ordinary hand-operated tin hatch. Its opening reveals a second, painted metal ceiling. */
public final class SiloHatchBlock extends TrapDoorBlock {
    public SiloHatchBlock(Properties p){super(BlockSetType.COPPER,p);}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level l,BlockPos at,Player p,BlockHitResult hit){
        var result=super.useWithoutItem(state,l,at,p,hit);
        if(!l.isClientSide&&p instanceof ServerPlayer reader&&l.getBlockState(at).getValue(OPEN))SiloSites.openedHatch(reader,at);
        return result;
    }
}
