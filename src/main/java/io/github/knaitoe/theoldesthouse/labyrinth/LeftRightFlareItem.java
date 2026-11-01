package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;

/**
 * 0.4.205, the Left-Right Game: a road flare. Struck against a floor, it stands there burning red for twelve seconds and goes
 * out by itself. On the divider at a fork it holds the salt off while the reader walks the wrong way. Apollo's last flare,
 * kept, is only its owner's, and lights again after three minutes; the crate's are spent once each.
 */
public final class LeftRightFlareItem extends Item {
    public static final int COOLDOWN=3600;
    public LeftRightFlareItem(Properties p){super(p);}
    @Override public InteractionResult useOn(UseOnContext c){
        var level=c.getLevel();var at=c.getClickedPos().relative(c.getClickedFace());
        if(c.getClickedFace()!=Direction.UP||!level.getBlockState(at).canBeReplaced()||!level.getFluidState(at).isEmpty())return InteractionResult.PASS;
        if(level.isClientSide)return InteractionResult.SUCCESS;
        var stack=c.getItemInHand();if(!(c.getPlayer() instanceof ServerPlayer p))return InteractionResult.PASS;
        var data=stack.get(DataComponents.CUSTOM_DATA);boolean kept=data!=null&&data.copyTag().hasUUID(LiteraryVignettes.OWNER);
        if(kept&&!data.copyTag().getUUID(LiteraryVignettes.OWNER).equals(p.getUUID())){p.displayClientMessage(net.minecraft.network.chat.Component.literal("It will not strike for you."),true);return InteractionResult.FAIL;}
        level.setBlock(at,LeftRightBlock.of(LeftRightBlock.Kind.FLARE,p.getDirection()),net.minecraft.world.level.block.Block.UPDATE_ALL);
        level.playSound(null,at,LeftRightRegistry.FLARE_SOUND.get(),SoundSource.PLAYERS,1F,1F);
        if(kept)p.getCooldowns().addCooldown(this,COOLDOWN);else stack.shrink(1);
        LeftRightGame.flareLit(p,at);
        return InteractionResult.CONSUME;
    }
}
