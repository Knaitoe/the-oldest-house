package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.204, the Polaroid Trail: four places joined end to end, as the four visits walk them. The reader comes out of the back
 * door of a house onto its porch (the entry, at the base): the yard runs north to a shallow creek and a half-built raft, and
 * beyond it an embankment with a corrugated culvert going into it. The culvert's far end is a side grate; past it a scramble
 * of earth climbs to the pried lattice vent of the crawlspace under the house next door (Mrs Maggie's), one block high, brick
 * piers, drains, pink insulation, a cat bowl, a heap of stolen clothes and a hatch in the clay floor. The hatch drops into a
 * brick tunnel that comes up through the floor of the neighbourhood cinema's store room: a concession lobby, eleven rows of
 * velvet seats from K at the back to A at the front, a torn screen, the projection booth's open port over the doors, and a
 * fire exit beside the screen that lets out into the pines: two trenches dug side by side in a clearing, a deer blind on its
 * legs, and the trees. Every position is relative to the base; the plateau the house next door stands on is three blocks up.
 */
public final class PolaroidSite {
    private PolaroidSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.POLAROID_TRAIL;
    /** Standing height on the plateau north of the embankment. */
    public static final int PLATEAU=3;
    /** The first balloon tag, tied to the porch rail (the source); the photo album in the deer blind (the account). */
    public static final BlockPos TAG1=new BlockPos(3,0,-4),ALBUM=new BlockPos(16,9,-112);
    public static final BlockPos TAG2=new BlockPos(-14,3,-46),TAG3=new BlockPos(6,4,-63),TAG4=new BlockPos(0,4,-117);
    /** The creek's water, the raft and the stump beside it. */
    public static final int CREEK_Z0=-17,CREEK_Z1=-15;
    public static final BlockPos STUMP=new BlockPos(8,0,-12);
    /** The culvert: its interior x -15..-13, y 0..1, from its mouth at z -24 to the side grate at z -38. */
    public static final int CULVERT_X0=-15,CULVERT_X1=-13,CULVERT_Z0=-38,CULVERT_MOUTH=-24,GRATE_Z=-38;
    /** The crawlspace: x -22..-4, z -43..-58, one block high on the plateau; its lattice vent; the cat bowl, the clothes, the hatch. */
    public static final int CRAWL_X0=-22,CRAWL_X1=-4,CRAWL_Z0=-58,CRAWL_Z1=-43;
    public static final BlockPos VENT=new BlockPos(-14,3,-42),BOWL=new BlockPos(-20,3,-51),HATCH=new BlockPos(-12,2,-57);
    public static final List<BlockPos> CLOTHES=List.of(new BlockPos(-6,3,-56),new BlockPos(-5,3,-56),new BlockPos(-6,3,-57),new BlockPos(-5,3,-57));
    /** The cinema: x -14..14, z -63..-101; the auditorium from z -72; rows K (z -74) to A (z -94); the stage front; the fire exit. */
    public static final int CINEMA_Z0=-101,CINEMA_Z1=-63,HALL_Z=-72,FRONT_Z=-95;
    public static final BlockPos PROJECTOR=new BlockPos(0,9,-70),PORT=new BlockPos(0,9,-71),FIRE_EXIT=new BlockPos(15,3,-97),EXIT_SIGN=new BlockPos(14,5,-97);
    /** The seat the figure sits in, at x -2: its row by index from K (0) to A (10). */
    public static final int SITTER_X=-2;
    public static int rowZ(int index){return -74-2*index;}
    public static final String ROWS="KJIHGFEDCBA";
    /** The pines: from z -104; the two trenches (x -6..-5 and 4..5, z -126..-121, two blocks deep); the deer blind. */
    public static final int PINES_Z=-104,TRENCH_Z0=-126,TRENCH_Z1=-121,TRENCH_A_X0=-6,TRENCH_A_X1=-5,TRENCH_B_X0=4,TRENCH_B_X1=5;
    /** The way on: the door of a garden shed at the far end of the wood. */
    public static final int EXIT_Z=-151;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    private static BlockState mat(String name){return PolaroidRegistry.material(name).defaultBlockState();}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static BlockState fit(PolaroidBlock.Kind k,Direction f,int stage){return PolaroidBlock.of(k,f,stage);}
    private static BlockState fit(PolaroidBlock.Kind k,Direction f){return PolaroidBlock.of(k,f,0);}
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();
    /** The ground's top block at z: the yard, the three steps of the embankment, the plateau. */
    static int groundTop(int z){return z>=-23?-1:z==-24?0:z==-25?1:2;}

    public static void build(ServerLevel l,BlockPos b){
        // Clear the whole site, then lay the ground under it.
        box(l,b,-26,-5,-152,26,18,0,AIR);
        for(int x=-26;x<=26;x++)for(int z=-152;z<=0;z++){int top=groundTop(z);
            for(int y=-5;y<top;y++)set(l,b,x,y,z,Blocks.DIRT.defaultBlockState());
            set(l,b,x,top,z,(z<=PINES_Z?Blocks.PODZOL:Blocks.GRASS_BLOCK).defaultBlockState());}
        yard(l,b);culvert(l,b);crawlspace(l,b);tunnel(l,b);cinema(l,b);pines(l,b);edges(l,b);
    }
    /** Beyond the fences the ground ends in a hedge of spruce: trunks every few blocks and dense boughs between, too high to climb. */
    private static void edges(ServerLevel l,BlockPos b){
        var leaves=Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        for(int z=-24;z>=-152;z--)for(int x:new int[]{-26,26})hedge(l,b,x,z,leaves,Math.floorMod(z,4)==0);
        for(int x=-25;x<=25;x++)if(Math.abs(x)>3)hedge(l,b,x,-152,leaves,Math.floorMod(x,4)==0);
    }
    private static void hedge(ServerLevel l,BlockPos b,int x,int z,BlockState leaves,boolean trunk){
        int y0=groundTop(z)+1;
        for(int y=y0;y<=y0+4;y++){var at=b.offset(x,y,z);if(BuildBlocks.state(l,at).isAir())BuildBlocks.set(l,at,trunk&&y<y0+4?Blocks.SPRUCE_LOG.defaultBlockState():leaves,F);}
    }

    // ------------------------------------------------------------------------------------------------ the yard
    private static void yard(ServerLevel l,BlockPos b){
        var siding=mat("siding");
        // The back of the house the reader comes out of: siding, two windows, the door in the middle (the entry).
        box(l,b,-10,0,0,10,5,0,siding);box(l,b,-10,6,0,10,6,0,Blocks.SPRUCE_SLAB.defaultBlockState());
        for(int x:new int[]{-6,-5,5,6})for(int y=1;y<=2;y++)set(l,b,x,y,0,Blocks.GLASS_PANE.defaultBlockState());
        // The porch: boards, a rail with a gap for the steps, a post at each corner.
        box(l,b,-4,-1,-4,4,-1,-1,Blocks.SPRUCE_PLANKS.defaultBlockState());
        var fence=Blocks.SPRUCE_FENCE.defaultBlockState();
        for(int x=-4;x<=4;x++)if(Math.abs(x)>=2)set(l,b,x,0,-5,fence);
        for(int z=-1;z>=-5;z--){set(l,b,-5,0,z,fence);set(l,b,5,0,z,fence);}
        for(int x:new int[]{-5,5})for(int y=1;y<=2;y++)set(l,b,x,y,-5,fence);
        box(l,b,-5,3,-5,5,3,-1,Blocks.SPRUCE_SLAB.defaultBlockState());
        set(l,b,TAG1,fit(PolaroidBlock.Kind.TAG,Direction.SOUTH));
        set(l,b,-3,0,-2,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));
        // The gravel drive along the east side, and the chain-link fences.
        box(l,b,7,-1,-10,11,-1,-1,Blocks.GRAVEL.defaultBlockState());
        for(int z=-1;z>=-23;z--)for(int y=0;y<=1;y++){set(l,b,-22,y,z,Blocks.IRON_BARS.defaultBlockState());set(l,b,22,y,z,Blocks.IRON_BARS.defaultBlockState());}
        // The creek: one block of water over gravel, running the width of the yard.
        for(int x=-21;x<=21;x++)for(int z=CREEK_Z0;z<=CREEK_Z1;z++){set(l,b,x,-2,z,Blocks.GRAVEL.defaultBlockState());set(l,b,x,-1,z,Blocks.WATER.defaultBlockState());}
        // The half-built raft on the near bank, the stump beside it, a loose pallet and a coil of rope.
        set(l,b,4,0,-13,fit(PolaroidBlock.Kind.RAFT,Direction.NORTH));set(l,b,5,0,-13,fit(PolaroidBlock.Kind.RAFT,Direction.NORTH));
        set(l,b,STUMP,Blocks.STRIPPED_OAK_LOG.defaultBlockState());set(l,b,3,0,-12,Blocks.OAK_SLAB.defaultBlockState());
        // A few trees in the yard and along the creek.
        tree(l,b,-17,0,-8,5);tree(l,b,15,0,-19,6);tree(l,b,-6,0,-20,5);
    }
    // ------------------------------------------------------------------------------------------------ the culvert
    private static void culvert(ServerLevel l,BlockPos b){
        var steel=mat("culvert");
        for(int z=CULVERT_MOUTH;z>=CULVERT_Z0;z--){
            box(l,b,CULVERT_X0-1,-1,z,CULVERT_X1+1,2,z,steel);
            box(l,b,CULVERT_X0,0,z,CULVERT_X1,1,z,AIR);
        }
        // The mouth's lip stands out of the slope; the ground in front of it is dug down to the yard.
        for(int x=CULVERT_X0-1;x<=CULVERT_X1+1;x++)for(int y=0;y<=2;y++)if(x<CULVERT_X0||x>CULVERT_X1||y==2)set(l,b,x,y,CULVERT_MOUTH+1,AIR);
        for(int x=CULVERT_X0;x<=CULVERT_X1;x++)set(l,b,x,-1,CULVERT_MOUTH+1,Blocks.GRAVEL.defaultBlockState());
        // The side grate at the far end, shut.
        for(int x=CULVERT_X0;x<=CULVERT_X1;x++)for(int y=0;y<=1;y++)set(l,b,x,y,GRATE_Z,fit(PolaroidBlock.Kind.GRATE,Direction.SOUTH,0));
        // Past it, a scramble of earth up to the crawlspace vent, in a burrow heaped against the house's foundation: three steps,
        // two blocks high, then one block high at the vent itself, where the reader has to be down on hands and knees.
        for(int i=0;i<3;i++){int z=GRATE_Z-1-i,floor=i+1,top=i==2?floor:floor+1;
            box(l,b,CULVERT_X0-1,-1,z,CULVERT_X1+1,4,z,Blocks.DIRT.defaultBlockState());
            box(l,b,CULVERT_X0,-1,z,CULVERT_X1,floor-1,z,Blocks.COARSE_DIRT.defaultBlockState());
            box(l,b,CULVERT_X0,floor,z,CULVERT_X1,top,z,AIR);
        }
        // The embankment is fenced off from the yard but for the culvert's mouth: chain-link, too high to climb.
        for(int x=-26;x<=26;x++)if(x<CULVERT_X0-1||x>CULVERT_X1+1)for(int y=0;y<=3;y++)set(l,b,x,y,CULVERT_MOUTH+1,Blocks.IRON_BARS.defaultBlockState());
    }

    // ------------------------------------------------------------------------------------------------ the crawlspace
    private static void crawlspace(ServerLevel l,BlockPos b){
        var brick=mat("pier_brick");var clay=mat("clay");var insulation=mat("insulation");var siding=mat("siding");
        // Foundation walls round the crawlspace, the clay floor, the insulated joists overhead.
        box(l,b,CRAWL_X0-1,2,CRAWL_Z0-1,CRAWL_X1+1,4,CRAWL_Z1+1,brick);
        box(l,b,CRAWL_X0,2,CRAWL_Z0,CRAWL_X1,2,CRAWL_Z1,clay);
        box(l,b,CRAWL_X0,3,CRAWL_Z0,CRAWL_X1,3,CRAWL_Z1,AIR);
        box(l,b,CRAWL_X0,4,CRAWL_Z0,CRAWL_X1,4,CRAWL_Z1,insulation);
        // Mrs Maggie's house above it: the floor, siding walls with windows, a pitched roof. Nobody is home.
        box(l,b,CRAWL_X0-1,5,CRAWL_Z0-1,CRAWL_X1+1,5,CRAWL_Z1+1,Blocks.OAK_PLANKS.defaultBlockState());
        box(l,b,CRAWL_X0-1,6,CRAWL_Z0-1,CRAWL_X1+1,9,CRAWL_Z1+1,siding);
        box(l,b,CRAWL_X0,6,CRAWL_Z0,CRAWL_X1,9,CRAWL_Z1,AIR);
        for(int x=CRAWL_X0+2;x<=CRAWL_X1-2;x+=4)for(int y=7;y<=8;y++){set(l,b,x,y,CRAWL_Z1+1,Blocks.GLASS_PANE.defaultBlockState());set(l,b,x,y,CRAWL_Z0-1,Blocks.GLASS_PANE.defaultBlockState());}
        for(int i=0;i<=5;i++)box(l,b,CRAWL_X0-1+i,10+i/2,CRAWL_Z0-1,CRAWL_X1+1-i,10+i/2,CRAWL_Z1+1,Blocks.DARK_OAK_PLANKS.defaultBlockState());
        // The pried vent in the south wall, where the scramble comes up.
        set(l,b,VENT,fit(PolaroidBlock.Kind.VENT,Direction.SOUTH,1));
        // Piers, drains, hanging insulation.
        for(int x:new int[]{-19,-14,-9})for(int z:new int[]{-47,-52,-55})if(!(x==-9&&z==-55))set(l,b,x,3,z,brick);
        set(l,b,-5,3,-46,fit(PolaroidBlock.Kind.DRAIN,Direction.NORTH));set(l,b,-21,3,-55,fit(PolaroidBlock.Kind.DRAIN,Direction.NORTH));
        for(var at:List.of(new BlockPos(-17,3,-45),new BlockPos(-11,3,-50),new BlockPos(-7,3,-53),new BlockPos(-20,3,-48),new BlockPos(-16,3,-56)))set(l,b,at,fit(PolaroidBlock.Kind.BATT,Direction.NORTH));
        set(l,b,TAG2,fit(PolaroidBlock.Kind.TAG,Direction.SOUTH));
        // The cat bowl of stale water by the west wall; the bed of stolen clothes in the north-east corner; the hatch.
        set(l,b,BOWL,fit(PolaroidBlock.Kind.BOWL,Direction.NORTH));
        for(var at:CLOTHES)set(l,b,at,fit(PolaroidBlock.Kind.CLOTHES,Direction.NORTH));
        set(l,b,HATCH,fit(PolaroidBlock.Kind.HATCH,Direction.NORTH,0));
    }
    // ------------------------------------------------------------------------------------------------ the tunnel
    private static void tunnel(ServerLevel l,BlockPos b){
        var brick=mat("pier_brick");
        // Under the hatch, a ladder down the shaft; a brick tunnel north; a ladder up into the cinema's store room.
        box(l,b,-13,-4,-65,-10,1,-56,brick);
        for(int y=-3;y<=1;y++)set(l,b,HATCH.getX(),y,HATCH.getZ(),AIR);
        box(l,b,-12,-3,-64,-11,-2,-57,AIR);
        for(int y=-3;y<=1;y++)set(l,b,HATCH.getX(),y,HATCH.getZ(),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        set(l,b,HATCH.getX(),-4,HATCH.getZ(),brick);
        for(int y=-3;y<=2;y++)set(l,b,-12,y,-65,brick);
        for(int y=-3;y<=1;y++)set(l,b,-12,y,-64,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        set(l,b,-11,-1,-60,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
    }
    // ------------------------------------------------------------------------------------------------ the cinema
    private static void cinema(ServerLevel l,BlockPos b){
        var wall=mat("cinema_wall");var carpet=mat("cinema_carpet");var velvet=mat("velvet");
        box(l,b,-15,2,CINEMA_Z0-1,15,13,CINEMA_Z1+1,wall);
        box(l,b,-14,2,CINEMA_Z0,14,2,CINEMA_Z1,carpet);
        box(l,b,-14,3,CINEMA_Z0,14,12,CINEMA_Z1,AIR);
        // The store room the ladder comes up into, and its door into the lobby.
        box(l,b,-8,3,-66,-8,6,-63,wall);box(l,b,-14,3,-67,-9,6,-67,wall);door(l,b,-8,-65,Direction.EAST,Blocks.OAK_DOOR);
        set(l,b,-12,2,-64,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        box(l,b,-14,7,-66,-9,7,-63,wall);
        // The lobby: a low ceiling under the booth, the concession counter, posters.
        box(l,b,-7,7,-71,14,7,-63,wall);
        for(int x=2;x<=8;x++)set(l,b,x,3,-67,fit(PolaroidBlock.Kind.COUNTER,Direction.SOUTH));
        set(l,b,TAG3,fit(PolaroidBlock.Kind.TAG,Direction.NORTH));
        for(int x:new int[]{-5,10,13})set(l,b,x,4,-63,fit(PolaroidBlock.Kind.POSTER,Direction.NORTH));
        set(l,b,-3,6,-66,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));set(l,b,8,6,-66,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        // The wall between the lobby and the auditorium, with its doorway; the booth above with its open port.
        box(l,b,-14,3,-71,14,12,-71,wall);box(l,b,-1,3,-71,1,5,-71,AIR);
        box(l,b,-3,8,-70,3,11,-68,AIR);box(l,b,-4,8,-71,4,12,-67,wall);box(l,b,-3,8,-70,3,11,-68,AIR);
        set(l,b,PORT,AIR);set(l,b,PROJECTOR,fit(PolaroidBlock.Kind.PROJECTOR,Direction.NORTH,0));
        // The rows, K at the back to A at the front, with an aisle down the middle and a light at each row's end.
        for(int i=0;i<ROWS.length();i++){int z=rowZ(i);
            for(int x=-9;x<=9;x++)if(Math.abs(x)>=2)set(l,b,x,3,z,fit(PolaroidBlock.Kind.SEAT,Direction.NORTH,0));
            set(l,b,-1,3,z,fit(PolaroidBlock.Kind.AISLE_LIGHT,Direction.EAST,0));set(l,b,1,3,z,fit(PolaroidBlock.Kind.AISLE_LIGHT,Direction.WEST,0));}
        // Velvet down the side walls; the screen on the north wall, torn at one corner.
        for(int z=HALL_Z;z>=CINEMA_Z0;z--)for(int y=3;y<=12;y++){set(l,b,-14,y,z,velvet);set(l,b,14,y,z,velvet);}
        box(l,b,-13,3,CINEMA_Z0,13,3,CINEMA_Z0,velvet);
        for(int x=-9;x<=9;x++)for(int y=5;y<=11;y++)set(l,b,x,y,CINEMA_Z0,fit(PolaroidBlock.Kind.SCREEN,Direction.SOUTH,0));
        // The fire exit beside the screen, its sign over it; locked until the power goes.
        set(l,b,14,3,FIRE_EXIT.getZ(),AIR);set(l,b,14,4,FIRE_EXIT.getZ(),AIR);
        door(l,b,FIRE_EXIT.getX(),FIRE_EXIT.getZ(),Direction.EAST,Blocks.IRON_DOOR);
        set(l,b,EXIT_SIGN,fit(PolaroidBlock.Kind.EXIT_SIGN,Direction.WEST,0));
        // The alley out of the fire exit, north between brick walls to the trees.
        box(l,b,16,2,-103,20,2,-96,Blocks.STONE.defaultBlockState());box(l,b,16,3,-103,20,6,-96,AIR);
        box(l,b,21,3,-103,21,6,-96,mat("pier_brick"));box(l,b,16,3,-95,21,6,-95,mat("pier_brick"));
    }
    private static void door(ServerLevel l,BlockPos b,int x,int z,Direction facing,Block door){
        int y=PLATEAU;var lower=door.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER);
        set(l,b,x,y,z,lower);set(l,b,x,y+1,z,lower.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    // ------------------------------------------------------------------------------------------------ the pines
    private static void pines(ServerLevel l,BlockPos b){
        var rnd=new Random(204);
        // The two trenches, two blocks deep, their spoil heaped beside them.
        for(int x0:new int[]{TRENCH_A_X0,TRENCH_B_X0})for(int x=x0;x<=x0+1;x++)for(int z=TRENCH_Z0;z<=TRENCH_Z1;z++){set(l,b,x,2,z,AIR);set(l,b,x,1,z,AIR);set(l,b,x,0,z,Blocks.COARSE_DIRT.defaultBlockState());}
        for(int z=TRENCH_Z0;z<=TRENCH_Z1;z++){set(l,b,-8,3,z,Blocks.COARSE_DIRT.defaultBlockState());set(l,b,7,3,z,Blocks.COARSE_DIRT.defaultBlockState());}
        set(l,b,-8,4,-124,Blocks.COARSE_DIRT.defaultBlockState());
        // Trees: dense round the edges, sparse in the middle, none over the clearing but the one whose branch reaches over trench A.
        for(int i=0;i<70;i++){int x=rnd.nextInt(49)-24,z=PINES_Z-2-rnd.nextInt(44);
            boolean clearing=Math.abs(x)<11&&z<-110&&z>-140;if(clearing||Math.abs(x-16)<4&&Math.abs(z+112)<4)continue;
            if(z>-108&&x>13)continue;
            tree(l,b,x,PLATEAU,z,8+rnd.nextInt(5));}
        set(l,b,TAG4.getX(),PLATEAU,TAG4.getZ()-1,Blocks.SPRUCE_LOG.defaultBlockState());for(int y=PLATEAU;y<=7;y++)set(l,b,TAG4.getX(),y,TAG4.getZ()-1,Blocks.SPRUCE_LOG.defaultBlockState());
        set(l,b,TAG4,fit(PolaroidBlock.Kind.TAG,Direction.SOUTH));
        // The tall pine west of trench A, with its branch over the trench: the watcher's last place.
        for(int y=PLATEAU;y<=18;y++)set(l,b,-10,y,-123,Blocks.SPRUCE_LOG.defaultBlockState());
        for(int x=-10;x<=-3;x++)set(l,b,x,18,-123,Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        for(int x=-11;x<=-2;x++)for(int z=-125;z<=-121;z++)for(int y=17;y<=18;y++){var at=b.offset(x,y,z);if(BuildBlocks.state(l,at).isAir()&&!(y==17&&Math.abs(x+5)<=1&&Math.abs(z+123)<=1))BuildBlocks.set(l,at,Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);}
        // The deer blind on its legs, a ladder up its south side, the album on its boards.
        for(int x:new int[]{15,17})for(int z:new int[]{-113,-111})for(int y=PLATEAU;y<=7;y++)set(l,b,x,y,z,Blocks.SPRUCE_LOG.defaultBlockState());
        box(l,b,15,8,-113,17,8,-111,Blocks.SPRUCE_PLANKS.defaultBlockState());
        for(int x=15;x<=17;x++){set(l,b,x,9,-113,Blocks.SPRUCE_FENCE.defaultBlockState());if(x!=16)set(l,b,x,9,-111,Blocks.SPRUCE_FENCE.defaultBlockState());}
        set(l,b,15,9,-112,Blocks.SPRUCE_FENCE.defaultBlockState());set(l,b,17,9,-112,Blocks.SPRUCE_FENCE.defaultBlockState());
        box(l,b,14,11,-114,18,11,-110,Blocks.SPRUCE_SLAB.defaultBlockState());
        for(int x:new int[]{15,17})for(int z:new int[]{-113,-111})set(l,b,x,10,z,Blocks.SPRUCE_FENCE.defaultBlockState());
        for(int y=PLATEAU;y<=7;y++)set(l,b,16,y,-111,Blocks.SPRUCE_LOG.defaultBlockState());
        for(int y=PLATEAU;y<=8;y++)set(l,b,16,y,-110,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        set(l,b,ALBUM,fit(PolaroidBlock.Kind.ALBUM,Direction.SOUTH));
        // Where he stands in the trees for the first three photographs: a hollow in the boughs, and a gap in them toward the clearing.
        var clearing=new Vec3(.5,4.5,-123.5);
        for(var node:List.of(new Vec3(-12.5,14.5,-114.5),new Vec3(14.5,15.5,-130.5),new Vec3(-14.5,14.5,-138.5))){
            for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++)bough(l,b,BlockPos.containing(node.x+dx,node.y+dy,node.z+dz));
            var d=clearing.subtract(node);double len=d.length();d=d.scale(1/len);
            for(double t=0;t<len;t+=.4)bough(l,b,BlockPos.containing(node.x+d.x*t,node.y+d.y*t,node.z+d.z*t));
        }
        set(l,b,17,9,-112,fit(PolaroidBlock.Kind.LUNCHBOX,Direction.WEST));
        // The garden shed at the far end, whose door is the way on.
        // Only its front stands inside the scene: the clapboard face, a window either side of the door, and the eave over it.
        box(l,b,-3,PLATEAU,EXIT_Z-1,3,PLATEAU+3,EXIT_Z,mat("siding"));
        for(int x:new int[]{-2,2}){set(l,b,x,PLATEAU+1,EXIT_Z,Blocks.GLASS_PANE.defaultBlockState());set(l,b,x,PLATEAU+1,EXIT_Z-1,Blocks.BLACK_WOOL.defaultBlockState());}
        box(l,b,-4,PLATEAU+4,EXIT_Z-1,4,PLATEAU+4,EXIT_Z+1,Blocks.SPRUCE_SLAB.defaultBlockState());
    }
    private static void bough(ServerLevel l,BlockPos b,BlockPos rel){var at=b.offset(rel);if(BuildBlocks.state(l,at).is(Blocks.SPRUCE_LEAVES))BuildBlocks.set(l,at,AIR,F);}
    /** A spruce: a trunk and tiers of leaves narrowing upward. */
    static void tree(ServerLevel l,BlockPos b,int x,int y0,int z,int height){
        for(int y=0;y<height;y++)set(l,b,x,y0+y,z,Blocks.SPRUCE_LOG.defaultBlockState());
        for(int y=2;y<=height;y++){int r=Math.max(0,(height-y)/3+ (y%2==0?1:0));if(y==height)r=0;
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.abs(dx)+Math.abs(dz)>r+1||dx==0&&dz==0&&y<height)continue;var at=b.offset(x+dx,y0+y,z+dz);
                if(BuildBlocks.state(l,at).isAir())BuildBlocks.set(l,at,Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);}}
        var top=b.offset(x,y0+height,z);if(BuildBlocks.state(l,top).isAir())BuildBlocks.set(l,top,Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);
    }
    /** The fifty balloons that light in the canopy at the end: where each one hangs, among the trees round the clearing. */
    public static List<BlockPos> canopyBalloons(){
        var out=new ArrayList<BlockPos>();var rnd=new Random(2041);
        while(out.size()<50){int x=rnd.nextInt(41)-20,z=PINES_Z-4-rnd.nextInt(40),y=10+rnd.nextInt(7);var at=new BlockPos(x,y,z);if(!out.contains(at))out.add(at);}
        return out;
    }
    public static Vec3 at(BlockPos b,double x,double y,double z){return new Vec3(b.getX()+x,b.getY()+y,b.getZ()+z);}
}
