package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** A presentation clock belongs to a real scene, never an expiring caption packet. */
public final class SceneClock {
    private SceneClock(){}
    public static int at(BlockPos origin,BlockPos position,ResourceKey<Level> dimension){
        if(!HouseDimensions.isHouseDimension(dimension))return 0;
        var place=LabyrinthPlaces.placeAt(origin,position);
        if(place==null||!dimension.equals(NovelRooms.dimension(place)))return 0;
        return switch(place){
            case ZAMPANO_COURTYARD->1;
            case BARN_WELL->3;
            case PLAIN->4;
            case HOSPITAL->5;
            case DROWNED_TOWN,SHALLOWS,PHONE_CANOE->7;
            default->0;
        };
    }
    public static long time(int mode,int elapsed,long nativeTime){return switch(mode){
        case 1->21000;case 3,7->18000;case 4->6000;
        case 5->18000+Math.min(6000,Math.max(0,elapsed)*6000L/3600);default->nativeTime;
    };}
}
