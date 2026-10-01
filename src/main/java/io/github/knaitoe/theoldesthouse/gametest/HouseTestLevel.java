package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;

/** The vanilla GameTest server omits data-pack dimensions; install a native test House level. */
final class HouseTestLevel {
    private HouseTestLevel(){}
    static ServerLevel get(MinecraftServer server){get(server,HouseDimensions.OUTSIDE);return get(server,HouseDimensions.INTERIOR);}
    @SuppressWarnings("deprecation") static ServerLevel get(MinecraftServer server,net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension){
        ServerLevel existing=server.getLevel(dimension);if(existing!=null)return existing;
        try{
            Field field=MinecraftServer.class.getDeclaredField("storageSource");field.setAccessible(true);
            var storage=(LevelStorageSource.LevelStorageAccess)field.get(server);var overworld=server.overworld();
            // Use the runner's flat generator so arbitrary biome terrain cannot mask an authored-geometry test.
            LevelStem stem=new LevelStem(overworld.dimensionTypeRegistration(),overworld.getChunkSource().getGenerator());
            ChunkProgressListener progress=new ChunkProgressListener(){
                public void updateSpawnPos(ChunkPos pos){}public void onStatusChange(ChunkPos pos,ChunkStatus status){}
                public void start(){}public void stop(){}
            };
            ServerLevel level=new ServerLevel(server,Util.backgroundExecutor(),storage,
                    new DerivedLevelData(server.getWorldData(),(ServerLevelData)overworld.getLevelData()),dimension,
                    stem,progress,false,0,List.of(),false,overworld.getRandomSequences());
            server.forgeGetWorldMap().put(dimension,level);server.markWorldsDirty();return level;
        }catch(ReflectiveOperationException exception){throw new IllegalStateException("Cannot initialize the native test House level",exception);}
    }
}
