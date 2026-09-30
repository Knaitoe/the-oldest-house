package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;

/** The old man is a quiet human witness, never a merchant or a combat mob. */
public final class FinaleWitness extends PathfinderMob {
    public FinaleWitness(EntityType<? extends FinaleWitness> type,Level level){super(type,level);setPersistenceRequired();setInvulnerable(true);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,0);}
    @Override protected void registerGoals(){goalSelector.addGoal(1,new LookAtPlayerGoal(this,Player.class,12));}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override protected InteractionResult mobInteract(Player player,InteractionHand hand){
        if(player instanceof ServerPlayer server&&hand==InteractionHand.MAIN_HAND)FinaleController.inspectWeapon(server,this);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }
}
