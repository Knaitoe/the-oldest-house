package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HouseSitting {
    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, TheOldestHouse.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<SeatEntity>> SEAT = TYPES.register("seat", () ->
            EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC).sized(0.05F, 0.05F)
                    .clientTrackingRange(8).updateInterval(20).build("seat"));
    private HouseSitting() {}
    public static void register(IEventBus bus) { TYPES.register(bus); }
    public static boolean isSeat(BlockState state) {
        return state.getBlock() instanceof StairBlock && state.getValue(StairBlock.HALF) == Half.BOTTOM
                || state.getBlock() instanceof CarpetBlock
                || state.getBlock() instanceof HouseholdFurnitureBlock && state.getValue(HouseholdFurnitureBlock.KIND).seat;
    }
    public static boolean sit(ServerPlayer player, BlockPos pos) {
        BlockState state = player.level().getBlockState(pos);
        if (!isSeat(state) || player.isPassenger() || player.isSpectator()
                || player.distanceToSqr(pos.getCenter()) > 16
                || io.github.knaitoe.theoldesthouse.labyrinth.HarriganVignette.occupiesChair(player.serverLevel(), pos)
                || !player.level().getEntitiesOfClass(SeatEntity.class, new AABB(pos)).isEmpty()) return false;
        SeatEntity seat = SEAT.get().create(player.serverLevel());
        if (seat == null) return false;
        seat.support(pos);
        double height=state.getBlock() instanceof CarpetBlock ? .07 : state.getBlock() instanceof HouseholdFurnitureBlock
                ? state.getValue(HouseholdFurnitureBlock.KIND).seatHeight/16.0 : .45;
        seat.moveTo(pos.getX() + 0.5, pos.getY() + height, pos.getZ() + 0.5);
        if (!player.serverLevel().addFreshEntity(seat) || !player.startRiding(seat)) { seat.discard(); return false; }
        if (state.getBlock() instanceof StairBlock) {
            float yaw = state.getValue(StairBlock.FACING).getOpposite().toYRot();
            player.setYRot(yaw); player.setYHeadRot(yaw);
        }
        if (state.getBlock() instanceof HouseholdFurnitureBlock) {
            float yaw=state.getValue(HouseholdFurnitureBlock.FACING).toYRot(); player.setYRot(yaw); player.setYHeadRot(yaw);
        }
        return true;
    }
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND
                || !event.getItemStack().isEmpty() || !(event.getEntity() instanceof ServerPlayer player)
                || player.isShiftKeyDown()) return;
        if (sit(player, event.getPos())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
