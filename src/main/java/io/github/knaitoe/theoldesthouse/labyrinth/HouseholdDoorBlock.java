package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 0.4.214: the household's door in the great staircase's wall, a painted house door with a brass knob (StaircaseHousehold). It
 * never swings open in the shaft: its reader goes through it into the household's room, and anyone else finds it shut.
 */
public final class HouseholdDoorBlock extends DoorBlock {
    public HouseholdDoorBlock(Properties p){super(BlockSetType.OAK,p);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player player,BlockHitResult hit){
        if(l.isClientSide)return InteractionResult.SUCCESS;
        if(player instanceof ServerPlayer p)StaircaseHousehold.useDoor(p,at);
        return InteractionResult.CONSUME;
    }
}
