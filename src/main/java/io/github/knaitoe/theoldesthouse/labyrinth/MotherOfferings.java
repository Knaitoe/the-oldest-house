package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseExteriorEntityMirror;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.level.block.DoorBlock;

/** A bargain requires a real, reachable life; a closed door by itself proves nothing. */
public final class MotherOfferings {
    private static final int MAX_ROOM_CELLS=256;
    private static final int ROOM_RADIUS=8;

    private MotherOfferings() {}

    public static boolean animalEligible(ServerPlayer trader, MotherEntity mother, LivingEntity offered) {
        if(!(offered instanceof Animal || offered instanceof AbstractVillager) || !offered.isAlive()
                || offered.isRemoved() || offered.isInvulnerable() || offered.level()!=mother.level()
                || offered.getTags().contains(MotherOfStrays.PET) || HouseExteriorEntityMirror.isProjection(offered)
                || mother.distanceToSqr(offered)>16 || !mother.hasLineOfSight(offered))return false;
        if(offered instanceof TamableAnimal tame && tame.isTame() && !trader.getUUID().equals(tame.getOwnerUUID()))return false;
        if(offered instanceof AbstractHorse horse && horse.isTamed() && !trader.getUUID().equals(horse.getOwnerUUID()))return false;
        return !(offered instanceof Mob mob) || !mob.isLeashed() || mob.getLeashHolder()==trader;
    }

    @Nullable public static LivingEntity find(ServerPlayer trader, MotherEntity mother, MotherCollection collection) {
        var animals=trader.serverLevel().getEntitiesOfClass(LivingEntity.class,mother.getBoundingBox().inflate(4),
                e->animalEligible(trader,mother,e) && collection.canKeepOfferedLife(e.getUUID()));
        animals.sort(Comparator.comparingDouble((LivingEntity e)->e instanceof Mob m && m.isLeashed() ? -1 : mother.distanceToSqr(e))
                .thenComparing(e->e.getUUID().toString()));
        if(!animals.isEmpty())return animals.getFirst();
        return trader.serverLevel().players().stream().filter(p->playerEligible(trader,mother,p))
                .min(Comparator.comparingDouble((ServerPlayer p)->mother.distanceToSqr(p))).orElse(null);
    }

    public static boolean playerEligible(ServerPlayer trader, MotherEntity mother, ServerPlayer offered) {
        var mode=offered.gameMode.getGameModeForPlayer();
        return offered!=trader && offered.isAlive() && !offered.isRemoved()
                && (mode==net.minecraft.world.level.GameType.SURVIVAL || mode==net.minecraft.world.level.GameType.ADVENTURE)
                && offered.level()==mother.level() && mother.distanceToSqr(offered)<=16
                && mother.hasLineOfSight(offered) && enclosedTogether(trader.serverLevel(),mother.blockPosition(),offered.blockPosition());
    }

    /** Flood player-sized empty space. A doorway, missing ceiling or large room invalidates the trap. */
    public static boolean enclosedTogether(ServerLevel level, BlockPos mother, BlockPos offered) {
        if(!walkable(level,mother))return false;
        Set<BlockPos> seen=new HashSet<>();var queue=new ArrayDeque<BlockPos>();
        seen.add(mother.immutable());queue.add(mother.immutable());
        while(!queue.isEmpty()) {
            BlockPos current=queue.removeFirst();
            for(Direction direction:Direction.values()) {
                BlockPos next=current.relative(direction);
                if(seen.contains(next))continue;
                if(!level.hasChunkAt(next))return false;
                if(!walkable(level,next))continue;
                if(Math.abs(next.getX()-mother.getX())>ROOM_RADIUS || Math.abs(next.getY()-mother.getY())>ROOM_RADIUS
                        || Math.abs(next.getZ()-mother.getZ())>ROOM_RADIUS || seen.size()>=MAX_ROOM_CELLS)return false;
                seen.add(next.immutable());queue.addLast(next.immutable());
            }
        }
        return seen.contains(offered);
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        return passable(level,feet) && passable(level,feet.above());
    }

    private static boolean passable(ServerLevel level, BlockPos pos) {
        var state=level.getBlockState(pos);
        return state.getBlock() instanceof DoorBlock ? state.getValue(DoorBlock.OPEN)
                : state.getCollisionShape(level,pos).isEmpty();
    }
}
