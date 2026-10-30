package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 0.4.173: the girls' bathroom's heavy wooden door. Shut all the way it latches and lets no light from the hall through.
 * After the third count its handle is held from the hall ({@link DareInTheDark#handle}), and the client, told so, does not
 * show it opening under the reader's hand.
 */
public final class DareDoorBlock extends DoorBlock {
    /** Set on the client by the dare's state: the handle is held from the other side. */
    public static volatile boolean clientHeld;
    public DareDoorBlock(Properties p){super(BlockSetType.OAK,p);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player player,BlockHitResult hit){
        if(l.isClientSide)return clientHeld&&!s.getValue(OPEN)?InteractionResult.SUCCESS:super.useWithoutItem(s,l,at,player,hit);
        if(player instanceof ServerPlayer p&&DareInTheDark.handle(p,at,s))return InteractionResult.CONSUME;
        var result=super.useWithoutItem(s,l,at,player,hit);
        if(player instanceof ServerPlayer p&&l.getBlockState(at).getBlock()==this)DareInTheDark.moved(p,l.getBlockState(at).getValue(OPEN));
        return result;
    }
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos p){return s.getValue(OPEN)?0:15;}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return s.getValue(OPEN);}
}
