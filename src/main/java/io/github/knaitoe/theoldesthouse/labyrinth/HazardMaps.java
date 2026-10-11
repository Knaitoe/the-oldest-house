package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import static io.github.knaitoe.theoldesthouse.labyrinth.HazardBlock.Kind.*;

/** Sixteen complete maps, recorded and carved by BuildBlocks in the normal bounded construction queue. */
public final class HazardMaps {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final BlockState AIR=Blocks.AIR.defaultBlockState(),FLOOR=Blocks.SMOOTH_STONE.defaultBlockState(),WALL=Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    private HazardMaps(){}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){BuildBlocks.box(l,b.offset(x0,y0,z0),b.offset(x1,y1,z1),s,F);}
    private static void room(ServerLevel l,BlockPos b,LabyrinthPlace p,BlockState skin){var r=p.room();box(l,b,r.minX(),r.minY(),r.minZ(),r.maxX(),r.maxY(),0,skin);box(l,b,r.minX()+1,0,r.minZ()+1,r.maxX()-1,r.maxY()-1,-1,AIR);box(l,b,r.minX()+1,-1,r.minZ()+1,r.maxX()-1,-1,-1,FLOOR);}
    private static void lamp(ServerLevel l,BlockPos b,int x,int y,int z){set(l,b,x,y,z,HazardBlock.of(HEAT_LAMP));}
    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace p){
        if(b==null||!HazardVariety.added(p))throw new IllegalArgumentException("Not an appended hazard: "+p);
        switch(p){
            case COLD_STORE->cold(l,b,p);case LISTING_HALL->listing(l,b,p);case QUIET_HALL->quiet(l,b,p);case UNWATCHED_MAZE->maze(l,b,p);
            case MIRROR_HALL->mirror(l,b,p);case HEAVY_AND_LIGHT->weight(l,b,p);case NESTED_ROOMS->nested(l,b,p);case COUNTING_STAIR->counting(l,b,p);
            case BENT_CISTERN->cistern(l,b,p);case SPIRAL_WALKWAY->spiral(l,b,p);case CORNER_PRESSURE->pressure(l,b,p);case RECEDING_HALL->receding(l,b,p);
            case LAST_SCONCE->sconce(l,b,p);case WATCHED_THRESHOLD->threshold(l,b,p);case DUPLICATE_ROOMS->duplicates(l,b,p);case TURNING_GRAVITY->gravity(l,b,p);default->throw new IllegalArgumentException(p.id());}
        LabyrinthBuilder.entrance(l,b,WALL,FLOOR,Blocks.STONE.defaultBlockState());
        if(p==LabyrinthPlace.WATCHED_THRESHOLD){LabyrinthBuilder.placeDoor(l,b.offset(0,0,1),Direction.SOUTH);var d=p.doors().get(1);LabyrinthBuilder.placeDoor(l,b.offset(d.rel()),d.facing());}
        else LabyrinthBuilder.doors(l,b,p);
        BuildBlocks.after(l,()->HazardVariety.fresh(l,b,p));
    }
    private static void cold(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,HazardBlock.of(LOCKER_WALL));
        for(int z=-5;z>=-37;z-=8){for(int x:new int[]{-3,3}){set(l,b,x,0,z,HazardBlock.of(FROSTED_CRATE));set(l,b,x,1,z,HazardBlock.of(FROSTED_CRATE));set(l,b,x,4,z-2,HazardBlock.of(HOOK));}lamp(l,b,0,4,z);}
        for(int x:new int[]{-2,2})set(l,b,x,1,-41,HazardBlock.of(COMPRESSOR));
    }
    private static void listing(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,HazardBlock.of(SHIP_PLATE));
        for(int z=-3;z>=-41;z--){for(int x:new int[]{-4,4}){set(l,b,x,-1,z,Blocks.WATER.defaultBlockState());set(l,b,x,-2,z,HazardBlock.of(SHIP_PLATE));}for(int x:new int[]{-2,2})if(z%3==0)set(l,b,x,0,z,HazardBlock.of(RAIL));}
        for(int z=-6;z>=-38;z-=8)lamp(l,b,0,3,z);
    }
    public static List<BlockPos> sensors(BlockPos b){var out=new ArrayList<BlockPos>();for(int z=-5;z>=-37;z-=4)out.add(b.offset(0,-2,z));return List.copyOf(out);}
    private static void quiet(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,Blocks.DARK_OAK_PLANKS.defaultBlockState());box(l,b,-3,-1,-41,3,-1,-2,HazardBlock.of(SCULK_BOARDS));
        for(var at:sensors(b)){BuildBlocks.set(l,at,Blocks.SCULK_SENSOR.defaultBlockState(),F);BuildBlocks.set(l,at.above(),HazardBlock.of(SCULK_BOARDS),F);}
        for(int z=-5;z>=-37;z-=6)lamp(l,b,0,4,z);
    }
    private static void maze(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,HazardBlock.of(MAZE_PANEL));box(l,b,-12,0,-27,12,3,-3,HazardBlock.of(MAZE_PANEL));
        for(int n=0;n<36;n++){var c=HazardMaze.cell(n);box(l,b,c.getX()-1,0,c.getZ()-1,c.getX()+1,2,c.getZ()+1,AIR);if(n%7==0)lamp(l,b,c.getX(),3,c.getZ());}
        long mask=HazardMaze.initialMask(b.asLong());for(int i=0;i<HazardMaze.EDGES.size();i++)if((mask&(1L<<i))!=0)for(var at:HazardMaze.EDGES.get(i).cells())BuildBlocks.set(l,b.offset(at),AIR,F);
        box(l,b,-1,0,-3,3,2,-1,AIR);box(l,b,1,0,-34,3,2,-27,AIR);
    }
    private static void mirror(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,Blocks.GRAY_CONCRETE.defaultBlockState());box(l,b,-7,0,-40,-7,3,-2,HazardBlock.of(MIRROR_GLASS));
        for(int x:new int[]{-10,-14,-18})LabyrinthBuilder.placeDoor(l,b.offset(x,0,-42),Direction.SOUTH);
        for(int z=-7;z>=-37;z-=10){lamp(l,b,1,4,z);lamp(l,b,-18,4,z);}
        for(int z=-10;z>=-30;z-=10){set(l,b,6,0,z,Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));set(l,b,-20,0,z,Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST));}
    }
    private static void weight(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,HazardBlock.of(PIT_STONE));box(l,b,-6,0,-22,6,4,-22,WALL);box(l,b,-1,0,-22,1,2,-22,AIR);
        for(int z=-5;z>=-19;z-=4)for(int x=-1;x<=1;x++)set(l,b,x,0,z,Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
        box(l,b,-5,-1,-34,5,-1,-27,AIR);box(l,b,-5,-2,-34,5,-2,-27,AIR);box(l,b,-5,-3,-34,5,-3,-27,HazardBlock.of(NET));
        // A permanent low route climbs out beside either wall after the net catches a jump.
        for(int x:new int[]{-5,5})for(int y=-2;y<=0;y++){set(l,b,x+(x<0?-1:1),y,-34,HazardBlock.of(PIT_STONE));set(l,b,x,y,-34,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,x<0?Direction.EAST:Direction.WEST));}
        lamp(l,b,0,5,-10);lamp(l,b,0,5,-38);
    }
    private static void nested(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,HazardBlock.of(PARLOUR_WALL));
        for(int r=0;r<4;r++){int z=-5-r*9;set(l,b,-4,0,z,Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST));set(l,b,4,0,z,Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));set(l,b,3,0,z-2,Blocks.DARK_OAK_FENCE.defaultBlockState());set(l,b,3,1,z-2,Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());lamp(l,b,0,5,z);if(r<3){int wall=z-4;box(l,b,-6,0,wall,6,5,wall,HazardBlock.of(PARLOUR_WALL));if(r==0)box(l,b,0,0,wall,0,1,wall,AIR);else set(l,b,0,0,wall,HazardBlock.of(NARROW_ARCH));}}
        box(l,b,-6,0,-39,6,5,-39,HazardBlock.of(PARLOUR_WALL));set(l,b,0,0,-39,HazardBlock.of(SKIRT_GAP));
    }
    public static BlockPos landing(int i){return new BlockPos(0,i*2,-3-i*6);}
    private static void counting(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,HazardBlock.of(PARLOUR_WALL));
        for(int i=0;i<=7;i++){var at=landing(i);box(l,b,-2,i*2-1,at.getZ()-2,5,i*2-1,at.getZ()+1,FLOOR);if(i>0)set(l,b,5,i*2+1,at.getZ()-1,HazardBlock.of(NUMBER_FOUR));
            if(i<7)for(int step=1;step<=4;step++)for(int x=-1;x<=1;x++){int y=i*2+(step-1)/2;set(l,b,x,y,-3-i*6-1-step,Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));}}
        for(int i:new int[]{1,3,5,7})lamp(l,b,-3,i*2+2,-3-i*6);
    }
    public static List<BlockPos> cisternRoute(){return line(new int[][]{{0,-3},{0,-10},{-8,-10},{-8,-22},{8,-22},{8,-34},{0,-34},{0,-40}});}
    public static List<BlockPos> airPockets(){return List.of(new BlockPos(-8,0,-11),new BlockPos(0,0,-22),new BlockPos(8,0,-32));}
    private static void cistern(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,HazardBlock.of(CISTERN_TILE));box(l,b,-10,-3,-40,10,5,-2,HazardBlock.of(CISTERN_TILE));
        for(var at:cisternRoute()){int z=at.getZ();int depth=z>-7||z<-36?0:z>-11||z<-32?-1:-3;
            for(int x=0;x<2;x++){set(l,b,at.getX()+x,depth-1,z,HazardBlock.of(CISTERN_TILE));for(int y=depth;y<=2;y++)set(l,b,at.getX()+x,y,z,z>-5||z<-37?AIR:Blocks.WATER.defaultBlockState());}}
        for(var at:airPockets()){box(l,b,at.getX()-1,2,at.getZ()-1,at.getX()+2,5,at.getZ()+1,AIR);for(int x=-1;x<=2;x++)set(l,b,at.getX()+x,6,at.getZ(),Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,x<1?Direction.EAST:Direction.WEST));lamp(l,b,at.getX(),5,at.getZ());}
        box(l,b,-1,0,-4,2,2,-1,AIR);box(l,b,-1,0,-40,2,2,-37,AIR);box(l,b,-1,-1,-40,2,-1,-37,FLOOR);
    }
    public static List<BlockPos> spiralRoute(){return line(new int[][]{{0,-3},{9,-3},{9,-25},{-9,-25},{-9,-3},{0,-3},{0,-29}});}
    private static void spiral(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,HazardBlock.of(PIT_STONE));box(l,b,-11,-3,-29,11,15,-2,AIR);box(l,b,-11,-4,-29,11,-4,-2,HazardBlock.of(PIT_STONE));var path=spiralRoute();
        for(int i=0;i<path.size();i++){var at=path.get(i);int y=Math.min(10,i/8);boolean vertical=i+1<path.size()?path.get(i+1).getZ()!=at.getZ():true;
            for(int across=-1;across<=1;across++)set(l,b,at.getX()+(vertical?across:0),y-1,at.getZ()+(vertical?0:across),Blocks.STONE_BRICKS.defaultBlockState());}
        // Cut the gaps last so overlapping corners cannot silently fill them back in.
        for(int i:new int[]{12,13,43,44,70,71}){var at=path.get(i);int y=Math.min(10,i/8);boolean vertical=path.get(i+1).getZ()!=at.getZ();for(int across=-1;across<=1;across++)set(l,b,at.getX()+(vertical?across:0),y-1,at.getZ()+(vertical?0:across),AIR);}
        for(int z:new int[]{-9,-19,-27})for(int x:new int[]{-11,11})for(int y=-3;y<=10;y++)set(l,b,x,y,z,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,x<0?Direction.EAST:Direction.WEST));
        box(l,b,-1,9,-29,1,9,-24,FLOOR);box(l,b,-1,-1,-3,1,-1,-1,FLOOR);lamp(l,b,0,15,-14);
    }
    public static List<BlockPos> pressureRoute(){return line(new int[][]{{0,-2},{0,-14},{-18,-14}});}
    private static void pressure(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,HazardBlock.of(PRESSURE_PANEL));box(l,b,-18,0,-17,3,4,-2,HazardBlock.of(PRESSURE_PANEL));
        for(var at:pressureRoute())box(l,b,at.getX()-1,0,at.getZ()-1,at.getX()+1,3,at.getZ()+1,AIR);lamp(l,b,0,3,-4);lamp(l,b,-16,3,-14);}
    private static void receding(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,HazardBlock.of(PRESSURE_PANEL));for(int z=-6;z>=-62;z-=8)lamp(l,b,0,4,z);box(l,b,-2,0,-34,2,3,-34,WALL);}
    private static void sconce(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,Blocks.GRAY_TERRACOTTA.defaultBlockState());box(l,b,-1,0,-25,1,3,-12,Blocks.DEEPSLATE_BRICKS.defaultBlockState());set(l,b,-8,2,-7,HazardBlock.of(SCONCE));}
    private static void threshold(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,WALL);lamp(l,b,0,4,-11);}
    public static int missingRoom(BlockPos b){return Math.floorMod(Long.hashCode(b.asLong()^0x534556454eL),3);}
    private static void duplicates(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,HazardBlock.of(PARLOUR_WALL));int missing=missingRoom(b);
        for(int i=0;i<3;i++){int z=-9-i*14;for(int x:new int[]{-4,4})set(l,b,x,0,z,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,x<0?Direction.WEST:Direction.EAST));set(l,b,-5,0,z-3,Blocks.POTTED_FERN.defaultBlockState());if(i!=missing)set(l,b,5,0,z-4,LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND,LiteraryPropBlock.Kind.CLOCK));lamp(l,b,0,4,z);
            if(i<2){box(l,b,-6,0,z-7,6,4,z-7,HazardBlock.of(PARLOUR_WALL));box(l,b,-1,0,z-7,1,2,z-7,AIR);}}
    }
    private static void gravity(ServerLevel l,BlockPos b,LabyrinthPlace p){room(l,b,p,HazardBlock.of(DRIFT_TILE));box(l,b,-11,-1,-26,11,-1,-3,AIR);box(l,b,-11,-2,-26,11,-2,-3,AIR);box(l,b,-11,-3,-26,11,-3,-3,FLOOR);
        box(l,b,-1,-1,-28,1,-1,-1,FLOOR);box(l,b,-11,-1,-15,11,-1,-13,FLOOR);
        for(int x:new int[]{-11,11})for(int z:new int[]{-6,-22}){set(l,b,x,0,z,HazardBlock.of(RAIL));for(int y=-2;y<=0;y++){set(l,b,x+(x<0?-1:1),y,z-1,HazardBlock.of(DRIFT_TILE));set(l,b,x,y,z-1,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,x<0?Direction.EAST:Direction.WEST));}}
        lamp(l,b,0,4,-14);
    }
    private static List<BlockPos> line(int[][] corners){var out=new ArrayList<BlockPos>();for(int i=0;i<corners.length-1;i++){int x=corners[i][0],z=corners[i][1];while(x!=corners[i+1][0]||z!=corners[i+1][1]){out.add(new BlockPos(x,0,z));x+=Integer.signum(corners[i+1][0]-x);z+=Integer.signum(corners[i+1][1]-z);}}var last=corners[corners.length-1];out.add(new BlockPos(last[0],0,last[1]));return List.copyOf(out);}
}
