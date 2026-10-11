package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.LinkedHashMap;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.77, once per saved town ({@value #STATE}): the cyan car that stood across School Street is parked along its school-side kerb,
 * and the lamp on the lane's lawn comes down, its lawn mended under it. Only cells still holding their authored parts change
 * (old or fitted lamp parts, the car's own fixtures); anything a player removed stays removed and nothing is restocked. It
 * waits for both places' chunks and entity sections, for no camera within 48 blocks, and for nothing at all (a body, an
 * item) in a cell it would change; and for the 0.4.68 car fixtures, so the old painted car is never left half replaced.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ProofrockRepairs {
    public static final String STATE="proofrock_0477";
    private static final int F=Block.UPDATE_ALL|Block.UPDATE_SUPPRESS_DROPS;
    /** What the 0.4.67 painted car was made of, before the 0.4.68 fixtures replaced it. */
    private static final Set<Block> PAINTED=Set.of(Blocks.POLISHED_BLACKSTONE,Blocks.CYAN_TERRACOTTA,Blocks.BLACK_STAINED_GLASS,Blocks.SMOOTH_STONE_SLAB,Blocks.RED_NETHER_BRICK_SLAB,Blocks.WARPED_SLAB,Blocks.SMOOTH_QUARTZ_SLAB,Blocks.CUT_RED_SANDSTONE_SLAB);
    private ProofrockRepairs(){}
    static void markDone(LabyrinthData d,BlockPos b){var s=d.state(STATE);s.putBoolean(Long.toString(b.asLong()),true);d.setState(STATE,s);}
    public static boolean done(LabyrinthData d,BlockPos b){return d.state(STATE).getBoolean(Long.toString(b.asLong()));}
    private static boolean lampPart(BlockPos b,BlockPos at,BlockState old,BlockState fitted){
        if(old.equals(fitted))return true;
        var kind=fitted.getValue(TownStreetlightBlock.KIND);
        if(kind==TownStreetlightBlock.Kind.HEAD)return old.is(Blocks.LANTERN);
        if(kind==TownStreetlightBlock.Kind.BASE)return old.is(Blocks.SHORT_GRASS);
        return old.is(Blocks.DARK_OAK_FENCE)&&!old.getValue(FenceBlock.WATERLOGGED);
    }
    public static boolean apply(ServerLevel l,BlockPos b){
        var data=LabyrinthData.get(l.getServer());if(done(data,b))return true;
        var across=ProofrockTown.CYAN_ACROSS.apply(b);var along=ProofrockTown.CYAN_ALONG.apply(b);var lamp=ProofrockTown.lawnLamp(b);
        var cells=new java.util.ArrayList<BlockPos>();cells.addAll(across.keySet());cells.addAll(along.keySet());cells.addAll(lamp.keySet());
        for(var at:cells)if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return false;
        // The 0.4.68 fixtures have not replaced the painted car yet: wait for them.
        for(var at:across.keySet())if(PAINTED.contains(l.getBlockState(at).getBlock()))return false;
        var edits=new LinkedHashMap<BlockPos,BlockState>();
        boolean whole=across.entrySet().stream().allMatch(e->l.getBlockState(e.getKey()).equals(e.getValue()));
        boolean clear=along.keySet().stream().allMatch(at->across.containsKey(at)||l.getBlockState(at).isAir());
        if(whole&&clear){for(var at:across.keySet())edits.put(at,Blocks.AIR.defaultBlockState());edits.putAll(along);}
        for(var e:lamp.entrySet())if(lampPart(b,e.getKey(),l.getBlockState(e.getKey()),e.getValue()))edits.put(e.getKey(),Blocks.AIR.defaultBlockState());
        var under=b.offset(-36,-1,-24);if(edits.containsKey(b.offset(-36,0,-24))&&l.getBlockState(under).isAir())edits.put(under,Blocks.GRASS_BLOCK.defaultBlockState());
        for(var at:edits.keySet()){
            if(l.getBlockEntity(at)!=null||!l.getEntitiesOfClass(Entity.class,new AABB(at).inflate(.02)).isEmpty())return false;
            if(l.players().stream().anyMatch(p->new AABB(at).inflate(48).intersects(p.getCamera().getBoundingBox())))return false;
        }
        for(var e:edits.entrySet())l.setBlock(e.getKey(),e.getValue(),F);
        markDone(data,b);return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=29||LabyrinthBuilder.isCarving()||!LabyrinthBuilder.isPlaceReady(s,LabyrinthPlace.DROWNED_TOWN))return;
        var origin=HouseSavedData.get(s).houseOrigin();var l=s.getLevel(NovelRooms.dimension(LabyrinthPlace.DROWNED_TOWN));var b=origin==null?null:LabyrinthPlaces.base(origin,LabyrinthPlace.DROWNED_TOWN);
        if(l!=null&&b!=null&&!l.getBlockState(b.offset(0,-1,-3)).isAir())apply(l,b);
    }
}
