package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Saved, finite, ordinary navigation tools. The House tampers only where nobody can see. */
public final class NavigationAids {
    private static final String STATE="navigation_marks",ACTIVE="HouseTrailActive",LAST="HouseTrailLast";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final UUID EXPLORER=new UUID(0,0);
    private NavigationAids(){}
    public static boolean allowed(ServerPlayer player) {
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();
        return !player.isSpectator()&&origin!=null&&player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)
                &&(LabyrinthPlaces.placeAt(origin,player.blockPosition())!=null||FinaleArchitecture.contains(origin,player.blockPosition()));
    }
    public static boolean placeChalk(ServerLevel level,BlockPos pos,Direction face,Direction arrow) {
        BlockState old=level.getBlockState(pos);
        if(!old.isAir()&&!old.is(HouseBlocks.CHALK_MARK.get()))return false;
        BlockPos support=pos.relative(face.getOpposite());
        if(level.getBlockState(support).getCollisionShape(level,support).isEmpty())return false;
        return level.setBlock(pos,HouseBlocks.CHALK_MARK.get().defaultBlockState()
                .setValue(ChalkMarkBlock.FACE,face).setValue(ChalkMarkBlock.ARROW,arrow),FLAGS);
    }
    public static boolean placeLine(ServerLevel level,BlockPos pos) {
        BlockState old=level.getBlockState(pos);
        if(old.is(HouseBlocks.TRAIL_LINE.get()))return false;
        if(!old.isAir()||!level.getBlockState(pos.below()).isCollisionShapeFullBlock(level,pos.below()))return false;
        level.setBlock(pos,HouseBlocks.TRAIL_LINE.get().defaultBlockState(),FLAGS);
        connections(level,pos);
        for(Direction direction:Direction.Plane.HORIZONTAL)connections(level,pos.relative(direction));
        return true;
    }
    private static void connections(ServerLevel level,BlockPos pos) {
        BlockState state=level.getBlockState(pos);
        if(!state.is(HouseBlocks.TRAIL_LINE.get()))return;
        state=state.setValue(BlockStateProperties.NORTH,level.getBlockState(pos.north()).is(HouseBlocks.TRAIL_LINE.get()))
                .setValue(BlockStateProperties.SOUTH,level.getBlockState(pos.south()).is(HouseBlocks.TRAIL_LINE.get()))
                .setValue(BlockStateProperties.EAST,level.getBlockState(pos.east()).is(HouseBlocks.TRAIL_LINE.get()))
                .setValue(BlockStateProperties.WEST,level.getBlockState(pos.west()).is(HouseBlocks.TRAIL_LINE.get()));
        level.setBlock(pos,state,FLAGS);
    }
    public static void remember(ServerLevel level,BlockPos pos,UUID owner,boolean chalk) {
        LabyrinthData data=LabyrinthData.get(level.getServer());CompoundTag state=data.state(STATE);
        ListTag marks=state.getList("Marks",Tag.TAG_COMPOUND);
        for(int i=marks.size()-1;i>=0;i--)if(marks.getCompound(i).getLong("Pos")==pos.asLong())marks.remove(i);
        CompoundTag mark=new CompoundTag();mark.putLong("Pos",pos.asLong());mark.putUUID("Owner",owner);
        mark.putBoolean("Chalk",chalk);marks.add(mark);
        while(marks.size()>2048)marks.remove(0);
        state.put("Marks",marks);data.setState(STATE,state);
    }
    public static boolean erase(ServerPlayer player,BlockPos pos) {
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=data.state(STATE);
        ListTag marks=state.getList("Marks",Tag.TAG_COMPOUND);
        for(int i=0;i<marks.size();i++) {
            CompoundTag mark=marks.getCompound(i);
            if(mark.getLong("Pos")==pos.asLong()&&mark.hasUUID("Owner")&&player.getUUID().equals(mark.getUUID("Owner"))) {
                if(player.serverLevel().getBlockState(pos).is(HouseBlocks.CHALK_MARK.get()))
                    player.serverLevel().setBlock(pos,Blocks.AIR.defaultBlockState(),FLAGS);
                marks.remove(i);state.put("Marks",marks);data.setState(STATE,state);return true;
            }
        }
        return false;
    }
    public static void toggle(ServerPlayer player) {
        if(!allowed(player))return;
        boolean active=!player.getPersistentData().getBoolean(ACTIVE);
        player.getPersistentData().putBoolean(ACTIVE,active);player.getPersistentData().remove(LAST);
        player.displayClientMessage(Component.literal(active?"You knot the string.":"You wind the loose end in."),true);
    }
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if(!(event.getEntity() instanceof ServerPlayer player)||!allowed(player)||event.getHand()!=InteractionHand.MAIN_HAND)return;
        ItemStack held=event.getItemStack();
        if(!held.is(LabyrinthRegistry.CHALK.get())&&!held.is(LabyrinthRegistry.TRAIL_SPOOL.get()))return;
        if(!(player.serverLevel().getBlockState(event.getPos()).getBlock() instanceof DoorBlock))return;
        // Marking a handle must not also send the player through it.
        event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
        if(held.is(LabyrinthRegistry.TRAIL_SPOOL.get())){toggle(player);return;}
        BlockPos floor=player.blockPosition();
        if(placeChalk(player.serverLevel(),floor,Direction.UP,player.getDirection())) {
            remember(player.serverLevel(),floor,player.getUUID(),true);
            held.hurtAndBreak(1,player,EquipmentSlot.MAINHAND);
        }
    }
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player)||player.serverLevel().getGameTime()%5!=0)return;
        if(!allowed(player)) {
            player.getPersistentData().remove(ACTIVE);player.getPersistentData().remove(LAST);return;
        }
        if(player.getPersistentData().getBoolean(ACTIVE)&&player.onGround())layTrail(player);
        if(player.serverLevel().getGameTime()%400==0)maybeAlter(player);
    }
    private static void layTrail(ServerPlayer player) {
        ItemStack spool=ItemStack.EMPTY;
        for(int i=0;i<player.getInventory().getContainerSize();i++) {
            ItemStack item=player.getInventory().getItem(i);
            if(item.is(LabyrinthRegistry.TRAIL_SPOOL.get())){spool=item;break;}
        }
        if(spool.isEmpty()){player.getPersistentData().remove(ACTIVE);return;}
        BlockPos at=player.blockPosition();CompoundTag tag=player.getPersistentData();
        BlockPos from=tag.contains(LAST)?BlockPos.of(tag.getLong(LAST)):at;
        // Steps, doors and folds end a segment. Never draw an impossible line through solid space.
        if(from.getY()!=at.getY()||from.distManhattan(at)>4)from=at;
        BlockPos step=from;
        List<BlockPos> pieces=new ArrayList<>();pieces.add(step);
        while(step.getX()!=at.getX()) {step=step.offset(Integer.compare(at.getX(),step.getX()),0,0);pieces.add(step);}
        while(step.getZ()!=at.getZ()) {step=step.offset(0,0,Integer.compare(at.getZ(),step.getZ()));pieces.add(step);}
        for(BlockPos piece:pieces) {
            if(spool.isEmpty())break;
            if(placeLine(player.serverLevel(),piece)) {
                remember(player.serverLevel(),piece,player.getUUID(),false);
                spool.hurtAndBreak(1,player,EquipmentSlot.MAINHAND);
            }
        }
        tag.putLong(LAST,at.asLong());
    }
    public static boolean mayAlter(ServerLevel level,BlockPos pos,ServerPlayer player) {
        if(player.distanceToSqr(pos.getCenter())<144)return false;
        return !HouseWatchers.isWatched(level,pos.getCenter())
                &&!HouseWatchers.isWatched(level,pos.getCenter().add(0,-.4,0));
    }
    private static void maybeAlter(ServerPlayer player) {
        if(LabyrinthData.get(player.server).returnDepth(player.getUUID())<6)return;
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=data.state(STATE);
        long now=player.serverLevel().getGameTime();
        if(now-state.getLong("LastAltered")<2400||player.getRandom().nextInt(100)>=4)return;
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();
        LabyrinthPlace here=origin==null?null:LabyrinthPlaces.placeAt(origin,player.blockPosition());
        alterOne(player,here);
    }
    /** One existing mark, still subject to the original depth, distance, observation and cooldown rules. */
    public static boolean alterOne(ServerPlayer player,LabyrinthPlace here) {
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=data.state(STATE);
        long now=player.serverLevel().getGameTime();BlockPos origin=HouseSavedData.get(player.server).houseOrigin();
        if(origin==null||here==null||!here.isGray()||LabyrinthPacing.quiet(here)
                ||data.returnDepth(player.getUUID())<6
                ||(state.contains("LastAltered")&&now-state.getLong("LastAltered")<2400))return false;
        ListTag marks=state.getList("Marks",Tag.TAG_COMPOUND);
        for(int i=marks.size()-1;i>=0;i--) {
            CompoundTag mark=marks.getCompound(i);
            if(!mark.hasUUID("Owner"))continue;
            UUID owner=mark.getUUID("Owner");
            if(!owner.equals(player.getUUID())&&!owner.equals(EXPLORER))continue;
            BlockPos pos=BlockPos.of(mark.getLong("Pos"));
            if(!player.serverLevel().hasChunkAt(pos)||!mayAlter(player.serverLevel(),pos,player))continue;
            if(LabyrinthPlaces.placeAt(origin,pos)!=here)continue;
            BlockState old=player.serverLevel().getBlockState(pos);
            if(old.is(HouseBlocks.CHALK_MARK.get())) {
                if(old.getValue(ChalkMarkBlock.FACE)!=Direction.UP)continue;
                player.serverLevel().setBlock(pos,old.setValue(ChalkMarkBlock.ARROW,old.getValue(ChalkMarkBlock.ARROW).getClockWise()),FLAGS);
            } else if(old.is(HouseBlocks.TRAIL_LINE.get())) {
                player.serverLevel().setBlock(pos,Blocks.AIR.defaultBlockState(),FLAGS);
                for(Direction direction:Direction.Plane.HORIZONTAL)connections(player.serverLevel(),pos.relative(direction));
                marks.remove(i);state.put("Marks",marks);
            } else continue;
            state.putLong("LastAltered",now);data.setState(STATE,state);return true;
        }
        return false;
    }
    public static void explorerMark(ServerLevel level,BlockPos pos,Direction arrow) {
        if(placeChalk(level,pos,Direction.UP,arrow))remember(level,pos,EXPLORER,true);
    }
}
