package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Settle the original native record onto the actual support, retaining its block entity and book. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class DomesticRecordSurfaces {
    private DomesticRecordSurfaces(){}
    public static boolean standing(BlockState s){return s.is(DomesticRegistry.RECORD.get())&&switch(s.getValue(DomesticRecordBlock.KIND)){case NOTE,RECIPE,AWARD->true;default->false;};}
    public static boolean settle(ServerLevel l,BlockPos at){
        var s=l.getBlockState(at);if(!standing(s))return false;
        double top=Double.NEGATIVE_INFINITY;
        for(int n=1;n<=2;n++){
            var below=at.below(n);if(l.getChunkSource().getChunkNow(below.getX()>>4,below.getZ()>>4)==null)return false;
            var support=l.getBlockState(below).getShape(l,below);if(support.isEmpty())continue;
            for(var box:support.toAabbs())if(box.minX<.8&&box.maxX>.2&&box.minZ<.8&&box.maxZ>.2){
                double y=below.getY()+box.maxY;if(y<=at.getY()+.0001)top=Math.max(top,y);
            }
        }
        if(top<at.getY()-1||top>at.getY())return false;
        int down=(int)Math.round((at.getY()-top)*16);if(s.getValue(DomesticRecordBlock.DROP)==down)return false;
        l.setBlock(at,s.setValue(DomesticRecordBlock.DROP,down),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);return true;
    }
    /** At a low oblique angle vanilla may pick the table cell before its lowered paper. */
    @SubscribeEvent(priority=EventPriority.HIGH) public static void use(PlayerInteractEvent.RightClickBlock e){
        if(e.getHand()!=InteractionHand.MAIN_HAND||e.getEntity().isSpectator()||!e.getEntity().isAlive())return;
        var l=e.getLevel();var hit=e.getHitVec().getLocation();
        for(int n=1;n<=2;n++){
            var at=e.getPos().above(n);var s=l.getBlockState(at);
            if(!standing(s)||!s.getValue(DomesticRecordBlock.PRESENT)||s.getValue(DomesticRecordBlock.DROP)==0)continue;
            double y=at.getY()-s.getValue(DomesticRecordBlock.DROP)/16.;
            if(Math.abs(hit.y-y)>.04||hit.x<at.getX()+.125||hit.x>at.getX()+.875||hit.z<at.getZ()+.125||hit.z>at.getZ()+.875||e.getEntity().distanceToSqr(at.getCenter())>25)continue;
            if(e.getEntity() instanceof ServerPlayer p&&l.getBlockEntity(at) instanceof DomesticRecordBlockEntity paper)paper.open(p);
            e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);return;
        }
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%20!=0||LabyrinthBuilder.isCarving())return;
        var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);if(origin==null||l==null)return;
        for(var home:DomesticLives.HOMES){var b=LabyrinthPlaces.base(origin,home.hall());if(b==null||LabyrinthData.get(server).door(home.hall().entryDoorId())==null)continue;
            for(var piece:DomesticStories.pieces(home.id())){var at=b.offset(piece.at());if(l.getChunkSource().getChunkNow(at.getX()>>4,at.getZ()>>4)==null||!l.areEntitiesLoaded(ChunkPos.asLong(at))||HazardVariety.watched(l,at))continue;settle(l,at);DomesticStories.revise(l,at,home.id(),piece.id());}
        }
    }
}
