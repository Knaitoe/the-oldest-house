package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/**
 * Indian Lake High (0.4.67): one storey, so a hunt can follow a reader through all of it. A corridor of lockers runs round a
 * grass courtyard; classrooms open onto it by front and back doors, and most rooms have a second way out, so a chase can
 * loop. The gym's folded bleachers leave a gap only she fits under; the library's stacks make long blind aisles. The three
 * damp essays are in three teachers' desks on different sides of the ring, and the church key waits in the principal's.
 *
 * Bounds, relative to the town's entry: x -6 (on Main Street) to -62, z -45 (on School Street) to -88 (on the beach).
 */
public final class IndianLakeHigh {
    public static final BlockPos ENTRANCE=new BlockPos(-6,0,-61);
    /** Film studies, the library carrel and the north classroom. */
    public static final BlockPos[] ESSAY_DESKS={new BlockPos(-29,0,-50),new BlockPos(-59,0,-86),new BlockPos(-13,0,-86)};
    public static final BlockPos KEY_DESK=new BlockPos(-12,0,-76);
    public static final int X0=-62,X1=-6,Z0=-88,Z1=-45;
    private static final int F=LabyrinthBuilder.flags();
    private final ServerLevel l;private final BlockPos b;
    private IndianLakeHigh(ServerLevel l,BlockPos b){this.l=l;this.b=b;}
    public static void build(ServerLevel l,BlockPos b){new IndianLakeHigh(l,b).all();}

    /** The corridor that rings the courtyard. */
    public static boolean ring(int x,int z){
        boolean east=x>=-23&&x<=-21,west=x>=-40&&x<=-38,south=z>=-57&&z<=-55,north=z>=-80&&z<=-78;
        return x>=-40&&x<=-21&&z>=-80&&z<=-55&&(east||west||south||north);
    }
    public static boolean courtyard(int x,int z){return x>=-36&&x<=-25&&z>=-76&&z<=-59;}
    public static boolean gym(int x,int z){return x>=-61&&x<=-42&&z>=-68&&z<=-46;}

    private void all(){
        // Floors, then the whole building cleared to the rafters.
        for(int x=X0;x<=X1;x++)for(int z=Z0;z<=Z1;z++)set(x,-1,z,floor(x,z));
        fill(X0+1,0,Z0+1,X1-1,7,Z1-1,Blocks.AIR);
        // Outer walls in brick, a pale band under the roof; the gym stands taller.
        for(int x=X0;x<=X1;x++)for(int z=Z0;z<=Z1;z++){if(x!=X0&&x!=X1&&z!=Z0&&z!=Z1)continue;int top=z==Z1&&x<=-41||x==X0&&z>=-69?7:4;
            for(int y=0;y<=top;y++)set(x,y,z,y==top?Blocks.LIGHT_GRAY_CONCRETE:Blocks.BRICKS);set(x,top+1,z,ProofrockTown.slab(Blocks.BRICKS));}
        // Inner walls.
        wallZ(-20,-87,-46);wallZ(-41,-87,-46);wallX(-54,-40,-21);wallX(-81,-40,-21);wallZ(-31,-53,-46);
        for(int z:new int[]{-56,-67,-73,-79})wallX(z,-19,-7);
        wallX(-69,-61,-42);
        // The gym's own walls rise to its higher ceiling.
        for(int z=-69;z<=-46;z++)for(int y=4;y<=7;y++)set(-41,y,z,y==7?Blocks.LIGHT_GRAY_CONCRETE:Blocks.BRICKS);
        for(int x=-61;x<=-42;x++)for(int y=4;y<=7;y++)set(x,y,-69,y==7?Blocks.LIGHT_GRAY_CONCRETE:Blocks.BRICKS);
        // The courtyard: open to the sky, glazed on all four sides, grass underfoot.
        for(int x=-37;x<=-24;x++)for(int z=-77;z<=-58;z++){boolean wall=x==-37||x==-24||z==-77||z==-58;if(!wall)continue;
            for(int y=0;y<=4;y++)set(x,y,z,y>=1&&y<=2&&!corner(x,z)&&Math.floorMod(x+z,3)!=0?Blocks.GLASS_PANE.defaultBlockState():(y==4?Blocks.LIGHT_GRAY_CONCRETE:Blocks.BRICKS).defaultBlockState());}
        // Ceilings: four blocks of headroom everywhere, seven in the gym, none over the courtyard.
        for(int x=X0+1;x<=X1-1;x++)for(int z=Z0+1;z<=Z1-1;z++){if(courtyard(x,z))continue;if(gym(x,z))set(x,7,z,Blocks.SMOOTH_STONE);else if(!(x>=-37&&x<=-24&&z>=-77&&z<=-58))set(x,4,z,Blocks.SMOOTH_STONE);}
        doors();windows();lockers();lights();
        classrooms();offices();cafeteria();gymnasium();library();court();front();
    }
    private static boolean corner(int x,int z){return (x==-37||x==-24)&&(z==-77||z==-58);}
    private BlockState floor(int x,int z){
        if(courtyard(x,z))return Blocks.GRASS_BLOCK.defaultBlockState();
        if(ring(x,z))return ((x+z)&1)==0?Blocks.WHITE_CONCRETE.defaultBlockState():Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        if(gym(x,z))return z==-57||x==-52&&z>=-60&&z<=-54?Blocks.WHITE_CONCRETE.defaultBlockState():Blocks.BIRCH_PLANKS.defaultBlockState();
        if(x>=-61&&x<=-42&&z>=-87&&z<=-70)return Blocks.DARK_OAK_PLANKS.defaultBlockState();
        if(x>=-40&&x<=-21&&z>=-87&&z<=-82)return ((x+z)&1)==0?Blocks.RED_CONCRETE.defaultBlockState():Blocks.WHITE_CONCRETE.defaultBlockState();
        if(x>=-19&&x<=-7&&z>=-66&&z<=-57)return ((x+z)&1)==0?Blocks.POLISHED_ANDESITE.defaultBlockState():Blocks.POLISHED_DIORITE.defaultBlockState();
        if(x>=-19&&x<=-7&&z>=-78&&z<=-68)return Blocks.DARK_OAK_PLANKS.defaultBlockState();
        return Blocks.OAK_PLANKS.defaultBlockState();
    }
    private void wallZ(int x,int z0,int z1){for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)for(int y=0;y<=3;y++)set(x,y,z,Blocks.WHITE_TERRACOTTA);}
    private void wallX(int z,int x0,int x1){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=0;y<=3;y++)set(x,y,z,Blocks.WHITE_TERRACOTTA);}

    // ------------------------------------------------------------------------------------------------ ways through
    private void doors(){
        // Main doors from Main Street; an open arch from the lobby into the ring.
        door(-6,-61,Direction.WEST);door(-6,-62,Direction.WEST);
        for(int z:new int[]{-61,-62})for(int y=0;y<=2;y++)set(-20,y,z,Blocks.AIR);
        // Classrooms, each with two ways out.
        door(-12,-56,Direction.NORTH);door(-20,-55,Direction.WEST);
        door(-23,-54,Direction.NORTH);door(-28,-54,Direction.NORTH);door(-25,-45,Direction.NORTH);door(-31,-47,Direction.WEST);
        door(-34,-54,Direction.NORTH);door(-39,-54,Direction.NORTH);door(-41,-50,Direction.WEST);
        door(-20,-80,Direction.WEST);door(-20,-85,Direction.WEST);
        // The office, the principal's room, the cafeteria and its loading door onto the beach.
        door(-12,-67,Direction.NORTH);door(-20,-70,Direction.WEST);door(-15,-73,Direction.NORTH);
        door(-26,-81,Direction.NORTH);door(-35,-81,Direction.NORTH);door(-30,-88,Direction.NORTH);
        // The gym's doors to the ring and to School Street; the library's to the ring, the cafeteria, the gym and the woods.
        door(-41,-60,Direction.WEST);door(-41,-61,Direction.WEST);door(-51,-45,Direction.NORTH);door(-52,-45,Direction.NORTH);
        door(-41,-74,Direction.WEST);door(-41,-84,Direction.WEST);door(-50,-69,Direction.NORTH);door(-62,-79,Direction.WEST);
        // Four doors into the courtyard.
        door(-24,-67,Direction.WEST);door(-37,-67,Direction.EAST);door(-30,-58,Direction.NORTH);door(-30,-77,Direction.SOUTH);
    }
    private void door(int x,int z,Direction into){NovelRooms.door(l,b.offset(x,0,z),into,Blocks.OAK_DOOR,false);}
    private void windows(){
        for(int z=-47;z>=-87;z-=3)if(!(z<=-57&&z>=-66))for(int y=1;y<=2;y++)set(X1,y,z,Blocks.GLASS_PANE);
        for(int x=-8;x>=-61;x-=3)if(x>-41||x<-48){for(int y=1;y<=2;y++)set(x,y,Z1,Blocks.GLASS_PANE);}
        for(int x=-8;x>=-40;x-=3)for(int y=1;y<=2;y++)if(x!=-29&&x!=-32&&(x>-11||x<-15))set(x,y,Z0,Blocks.GLASS_PANE);
        for(int z=-72;z>=-86;z-=3)if(z!=-78&&z!=-81)for(int y=1;y<=2;y++)set(X0,y,z,Blocks.GLASS_PANE);
        // High windows along the gym.
        for(int x=-44;x>=-60;x-=3)for(int y=4;y<=5;y++)set(x,y,Z1,Blocks.GLASS_PANE);
        for(int z=-48;z>=-66;z-=3)for(int y=4;y<=5;y++)set(X0,y,z,Blocks.GLASS_PANE);
    }
    /** Lockers line the corridor's outer walls between the doors, two high. */
    private void lockers(){
        Set<Long> doorways=new HashSet<>();
        for(int[] d:new int[][]{{-20,-55},{-20,-61},{-20,-62},{-20,-70},{-20,-80},{-41,-60},{-41,-61},{-41,-74},{-23,-54},{-28,-54},{-34,-54},{-39,-54},{-26,-81},{-35,-81}})
            for(int s=-1;s<=1;s++){doorways.add(BlockPos.asLong(d[0],0,d[1]+s));doorways.add(BlockPos.asLong(d[0]+s,0,d[1]));}
        for(int z=-56;z>=-79;z--){locker(-20,z,Direction.WEST,doorways);locker(-41,z,Direction.EAST,doorways);}
        for(int x=-22;x>=-39;x--){locker(x,-54,Direction.NORTH,doorways);locker(x,-81,Direction.SOUTH,doorways);}
    }
    private void locker(int x,int z,Direction facing,Set<Long> doorways){
        if(doorways.contains(BlockPos.asLong(x,0,z)))return;
        set(x,0,z,TownFixtureBlock.of(TownFixtureBlock.Kind.LOCKER,facing));set(x,1,z,TownFixtureBlock.of(TownFixtureBlock.Kind.LOCKER_TOP,facing));
    }
    /** The east and south corridors are lit; the north one and most classrooms are not. */
    private void lights(){
        for(int z=-58;z>=-76;z-=6){light(-22,3,z,10);set(-22,4,z,Blocks.SEA_LANTERN);}
        for(int x=-26;x>=-38;x-=6){light(x,3,-56,10);set(x,4,-56,Blocks.SEA_LANTERN);}
        for(int z=-60;z>=-76;z-=8)light(-39,3,z,5);
        light(-13,3,-61,11);set(-13,4,-61,Blocks.SEA_LANTERN);
        light(-26,3,-50,9);light(-30,3,-85,5);light(-52,6,-52,7);light(-52,6,-63,7);light(-12,3,-70,6);
    }

    // ------------------------------------------------------------------------------------------------ rooms
    private void classrooms(){
        // Film studies: the chalkboard, the projector, posters and the first essay's desk.
        chalkboard(-31,-49,-1,Direction.EAST);desk(ESSAY_DESKS[0],Direction.EAST);
        for(int x:new int[]{-27,-25,-23})for(int z:new int[]{-47,-49,-51})student(x,z,Direction.WEST);
        NovelRooms.furniture(l,b.offset(-22,0,-53),HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.WEST);
        set(-22,1,-53,NovelRooms.prop(NovelPropBlock.Kind.PROJECTOR,Direction.WEST));
        for(int z:new int[]{-47,-52})poster(-21,1,z,Direction.WEST);
        // The second south classroom, its board on the outer wall.
        chalkboard(-35,-45,-1,Direction.NORTH);desk(new BlockPos(-36,0,-47),Direction.NORTH);
        for(int x:new int[]{-34,-36,-38})for(int z:new int[]{-49,-51})student(x,z,Direction.SOUTH);
        // The east classroom off the lobby.
        chalkboard(-20,-49,-1,Direction.EAST);desk(new BlockPos(-18,0,-50),Direction.EAST);
        for(int x:new int[]{-10,-12,-14,-16})for(int z:new int[]{-48,-50,-52})student(x,z,Direction.WEST);
        // The north classroom: the third essay's desk under its board.
        chalkboard(-14,-88,1,Direction.SOUTH);desk(ESSAY_DESKS[2],Direction.SOUTH);
        for(int x:new int[]{-9,-11,-15,-17})for(int z:new int[]{-82,-84})student(x,z,Direction.NORTH);
    }
    private void offices(){
        // Lobby: trophy cases and benches.
        for(int x:new int[]{-8,-9,-15,-16})for(int y=0;y<=1;y++)set(x,y,-66,TownFixtureBlock.of(TownFixtureBlock.Kind.TROPHY,Direction.SOUTH));
        for(int x:new int[]{-9,-10,-14,-15})set(x,0,-58,LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,Direction.NORTH));
        poster(-19,1,-64,Direction.EAST);poster(-7,1,-58,Direction.WEST);
        // The front office counter, with a way round it.
        for(int x=-8;x>=-16;x--){set(x,0,-70,Blocks.SPRUCE_PLANKS);set(x,1,-70,ProofrockTown.slab(Blocks.SPRUCE_PLANKS));}
        set(-10,1,-70,SceneDetailBlock.state(SceneDetailBlock.Kind.FILE_TRAY,Direction.SOUTH));
        NovelRooms.furniture(l,b.offset(-9,0,-72),HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);
        // The principal: the desk the key is put in, a shelf, a chair behind.
        desk(KEY_DESK,Direction.SOUTH);NovelRooms.furniture(l,b.offset(-12,0,-77),HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.SOUTH);
        for(int x:new int[]{-8,-9})for(int y=0;y<=2;y++)set(x,y,-78,Blocks.BOOKSHELF);
        set(-18,0,-74,Blocks.POTTED_FERN);
    }
    private void cafeteria(){
        for(int x=-22;x>=-27;x--){set(x,0,-87,Blocks.WHITE_CONCRETE);set(x,1,-87,ProofrockTown.slab(Blocks.WHITE_CONCRETE));}
        for(int x=-24;x>=-38;x-=3)for(int z:new int[]{-83,-85}){NovelRooms.furniture(l,b.offset(x,0,z),HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH);
            NovelRooms.furniture(l,b.offset(x-1,0,z),HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.EAST);}
        set(-39,0,-86,Blocks.BARREL);set(-39,1,-86,Blocks.BARREL);set(-38,0,-87,Blocks.CAULDRON);
    }
    private void gymnasium(){
        // Folded bleachers along both sides: three blocks of seating above a gap one body high, broken for the doors.
        for(int x=-44;x>=-58;x--){
            if(x>-50||x<-53)for(int z:new int[]{-46,-47})bleacher(x,z,Direction.NORTH);
            if(x>-49||x<-51)for(int z:new int[]{-67,-68})bleacher(x,z,Direction.SOUTH);
        }
        for(int x:new int[]{-42,-61}){for(int y=4;y<=5;y++)set(x,y,-57,Blocks.WHITE_CONCRETE);set(x+(x==-42?-1:1),4,-57,Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP));}
        set(-60,0,-49,Blocks.BARREL);set(-43,0,-65,Blocks.HAY_BLOCK);
    }
    private void bleacher(int x,int z,Direction facing){for(int y=1;y<=3;y++)set(x,y,z,TownFixtureBlock.of(TownFixtureBlock.Kind.BLEACHER,facing));}
    private void library(){
        // Five stacks with a cross aisle: long, blind, dark.
        for(int x:new int[]{-45,-48,-51,-54,-57})for(int z=-72;z>=-84;z--)if(z!=-78)for(int y=0;y<=2;y++)set(x,y,z,Blocks.BOOKSHELF);
        NovelRooms.furniture(l,b.offset(-43,0,-72),HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.EAST);
        set(-43,1,-72,SceneDetailBlock.state(SceneDetailBlock.Kind.BOOKS,Direction.EAST));
        for(int z:new int[]{-73,-77,-81}){NovelRooms.furniture(l,b.offset(-60,0,z),HouseholdFurnitureBlock.Kind.READING_DESK,Direction.WEST);
            NovelRooms.furniture(l,b.offset(-59,0,z),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);}
        set(-60,1,-77,SceneDetailBlock.state(SceneDetailBlock.Kind.TABLE_LAMP,Direction.WEST));
        desk(ESSAY_DESKS[1],Direction.EAST);
    }
    private void court(){
        // One oak in the middle of the lawn; benches you have to step onto grass to reach.
        for(int y=0;y<=4;y++)set(-30,y,-67,Blocks.OAK_LOG);
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)for(int y=4;y<=7;y++)if(Math.abs(dx)+Math.abs(dz)+(y-5)*2<=3&&(dx!=0||dz!=0||y>4))
            set(-30+dx,y,-67+dz,Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
        for(int z:new int[]{-62,-72}){set(-27,0,z,LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,Direction.WEST));set(-34,0,z,LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,Direction.EAST));}
    }
    private void front(){
        // The school's name over the doors, the crest beside it, a canopy and a flagpole.
        for(int i=0;i<3;i++)set(X1,5,-60-i,TownFixtureBlock.of(TownFixtureBlock.Kind.sign("school",i),Direction.EAST));
        set(X1,3,-64,TownFixtureBlock.of(TownFixtureBlock.Kind.CREST,Direction.EAST));
        for(int z=-59;z>=-64;z--)set(X1+1,3,z,Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        for(int y=0;y<=7;y++)set(-5,y,-57,ProofrockTown.fence(Blocks.SPRUCE_FENCE,null));
        set(-5,7,-56,Blocks.BLUE_WOOL);set(-5,6,-56,Blocks.YELLOW_WOOL);
    }

    // ------------------------------------------------------------------------------------------------ fittings
    private void desk(BlockPos at,Direction facing){set(at.getX(),at.getY(),at.getZ(),SchoolDeskBlock.facing(facing));}
    private void student(int x,int z,Direction facing){set(x,0,z,TownFixtureBlock.of(TownFixtureBlock.Kind.STUDENT_DESK,facing));}
    private void poster(int x,int y,int z,Direction facing){set(x,y,z,TownFixtureBlock.of(TownFixtureBlock.Kind.POSTER,facing));}
    /**
     * Three panels at eye height, set into the wall from {@code (x, z)}, the reader's left, stepping {@code step} along the wall
     * towards their right: +1 along x facing south, -1 along x facing north, -1 along z facing east, +1 along z facing west.
     */
    private void chalkboard(int x,int z,int step,Direction facing){
        boolean alongX=facing==Direction.NORTH||facing==Direction.SOUTH;
        TownFixtureBlock.Kind[] panels={TownFixtureBlock.Kind.CHALK_L,TownFixtureBlock.Kind.CHALK_M,TownFixtureBlock.Kind.CHALK_R};
        for(int i=0;i<3;i++)set(alongX?x+i*step:x,1,alongX?z:z+i*step,TownFixtureBlock.of(panels[i],facing));
    }
    private void light(int x,int y,int z,int level){set(x,y,z,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,level));}
    private void set(int x,int y,int z,Block block){set(x,y,z,block.defaultBlockState());}
    private void set(int x,int y,int z,BlockState state){BuildBlocks.set(l,b.offset(x,y,z),state,F);}
    private void fill(int x0,int y0,int z0,int x1,int y1,int z1,Block block){BuildBlocks.box(l,b.offset(x0,y0,z0),b.offset(x1,y1,z1),block.defaultBlockState(),F);}
}
