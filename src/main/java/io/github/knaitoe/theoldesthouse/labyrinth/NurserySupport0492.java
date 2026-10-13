package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Two expected toy mounts, corrected once without rebuilding the room or returning a removed toy. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class NurserySupport0492 {
    public static final String STATE="nursery_support0492";
    private NurserySupport0492(){}
    public static void fresh(ServerLevel l,BlockPos b){BuildBlocks.after(l,()->{apply(l,b);mark(l,b);});}
    private static void mark(ServerLevel l,BlockPos b){var d=LabyrinthData.get(l.getServer());var t=d.state(STATE);t.putBoolean(Long.toString(b.asLong()),true);d.setState(STATE,t);}
    private static void apply(ServerLevel l,BlockPos b){
        var chest=b.offset(ChildRoom.TOY_HOMES.get(1));var s=l.getBlockState(chest);
        if(NurseryBlock.is(s,NurseryBlock.Kind.DOLL)&&s.getValue(NurseryBlock.STAGE)==0&&NurseryBlock.is(l.getBlockState(chest.below()),NurseryBlock.Kind.TOY_CHEST))l.setBlock(chest,s.setValue(NurseryBlock.STAGE,2),18);
        var shelf=b.offset(-10,1,-16);s=l.getBlockState(shelf);
        if(NurseryBlock.is(s,NurseryBlock.Kind.DOLL)&&s.getValue(NurseryBlock.STAGE)==0&&NurseryBlock.is(l.getBlockState(shelf.below()),NurseryBlock.Kind.SHELF))l.setBlock(shelf,s.setValue(NurseryBlock.STAGE,1),18);
    }
    public static boolean repair(ServerLevel l,BlockPos b){
        if(LabyrinthData.get(l.getServer()).state(STATE).getBoolean(Long.toString(b.asLong())))return true;
        var area=new AABB(Vec3.atLowerCornerOf(b.offset(-11,0,-23)),Vec3.atLowerCornerOf(b.offset(1,7,-14)));
        for(int x=(int)area.minX>>4;x<=(int)area.maxX>>4;x++)for(int z=(int)area.minZ>>4;z<=(int)area.maxZ>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        if(l.players().stream().anyMatch(p->area.inflate(32).intersects(p.getCamera().getBoundingBox())))return false;
        for(var rel:new BlockPos[]{ChildRoom.TOY_HOMES.get(1),new BlockPos(-10,1,-16)})if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(b.offset(rel)).inflate(.5),LivingEntity::isAlive).isEmpty())return false;
        apply(l,b);mark(l,b);return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=29||LabyrinthBuilder.isCarving()||!LabyrinthBuilder.isPlaceReady(s,ChildRoom.PLACE))return;var o=HouseSavedData.get(s).houseOrigin();var l=s.getLevel(NovelRooms.dimension(ChildRoom.PLACE));if(o!=null&&l!=null)repair(l,LabyrinthPlaces.base(o,ChildRoom.PLACE));}
}
