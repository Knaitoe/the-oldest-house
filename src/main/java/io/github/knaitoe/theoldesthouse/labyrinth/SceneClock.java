package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** A presentation clock belongs to a real scene, never an expiring caption packet. */
public final class SceneClock {
    private SceneClock(){}
    public static int at(BlockPos origin,BlockPos position,ResourceKey<Level> dimension){
        if(origin==null||position==null||!HouseDimensions.isHouseDimension(dimension))return 0;
        var place=LabyrinthPlaces.placeAt(origin,position);
        // Keep the plain's presentation across its complete authored arrival volume.
        if(place==null&&dimension.equals(HouseDimensions.OUTSIDE)){
            var plain=LabyrinthPlaces.base(origin,LabyrinthPlace.PLAIN);
            if(plain!=null&&IndianLakeRooms.bounds(plain,LabyrinthPlace.PLAIN).contains(net.minecraft.world.phys.Vec3.atCenterOf(position)))place=LabyrinthPlace.PLAIN;
        }
        // 0.4.342: the Sphinx's dream is in the morning.
        if(place==null&&dimension.equals(HouseDimensions.OUTSIDE)&&SphinxDream.dreamSite(origin,position))return 11;
        if(place==null||!dimension.equals(NovelRooms.dimension(place)))return 0;
        return switch(place){
            case ZAMPANO_COURTYARD->1;
            case BARN_WELL->3;
            case PLAIN->4;
            case HOSPITAL->5;
            case DROWNED_TOWN,SHALLOWS,PHONE_CANOE->7;
            case HOTEL_GROUNDS->8;
            case HOLY_RABBIT->11;
            case SECOND_WAITRESS,ELK_LOT,ELK_CARCASSES,COSTUME_NIGHT,MOVIE_NIGHT,WINTER_LAKE,CAMP_BLOOD,BLY_ROUTE->9;
            case END_WORLD_CABIN->10;
            case LAKE_HOUSE,LAKE_HOUSE_EMPTY->4;
            case WAY_OUT->12; // 0.4.199: a fine morning, and the sun never moves
            case MAPPING_INTERIOR->13; // 0.4.201: 2:49 in the morning, every night
            case DEVILS_ROCK->9;
            case PITT->14; // 0.4.207: first light over the moor, for whoever climbs out
            case DEAD_AIR->3; // 0.4.206: an overcast evening over the marsh
            case LEFT_RIGHT->9; // 0.4.205: two in the morning at the toll
            case REPLACEMENT->9; // 0.4.218: midnight on the street
            case PLACE_FOR_YOU->9; // 0.4.257: one evening in Mara's house
            case SPHINX_FIELD->3; // 0.4.339: dusk over the corn
            case STATE_ROUTE->9; // 0.4.333: midnight and rain on the state route
            case MARSH_SUPPER->3; // 0.4.348: dusk over the corn at the Marsh farm
            case BLACK_CITY->20; // 0.4.409: a low sun over the ice that never quite sets; dawn for a reader who outran what came up the tunnel
            case CORNER_HOUSE->19; // 0.4.408: siesta on the street, the shutters down; night for a reader back after the last room
            case THE_DRAWING->18; // 0.4.407: ten in the morning in the village square, bright, all day
            case TALL_GRASS->16; // 0.4.349: late afternoon on the county road, the sun low over the grass
            case GROWING_THINGS->16; // 0.4.350: a Saturday afternoon at the Barretts', going on to supper
            case PINEHOOK->3; // 0.4.364: dusk over the lake at Pinehook
            case LAST_CROSSING->17; // 0.4.351: the flood station's own day, morning to dusk, sent by the station (CrossingPayload)
            case DEER_ROAD->9; // 0.4.310: midnight and rain on the county road
            case SECOND_PERSON->15; // 0.4.274: the flat's own year and hour, from the reader's record (SecondPersonClient)
            case PEOPLE_LIGHTS->9; // 0.4.253: the evening and the night, the people lights going off
            case VACANT_POSSESSION->3; // 0.4.228: moving day, the light going over the street
            case LAST_SWIMMER->9; // 0.4.227: closing time at the pool, dark over the skylight
            case POLAROID_TRAIL->3; // 0.4.204: late autumn twilight over the yard and the pines
            case WHISTLERS->9; // 0.4.202: night at the lookout, stars over the forest of towers // 0.4.201: midnight at the Sandersons' and in the park
            default->0;
        };
    }
    public static long time(int mode,int elapsed,long nativeTime){return switch(mode){
        case 1->21000;case 3,10->12500;case 7,8,9->18000;case 4->6000;
        case 11->1000;case 18->4000;case 19->8000;case 20->11400;case 15->7000;case 17->1000;case 16->10800;case 12->3000;case 13->20820;case 14->23200;case 5->18000+Math.min(6000,Math.max(0,elapsed)*6000L/3600);default->nativeTime;
    };}
}
