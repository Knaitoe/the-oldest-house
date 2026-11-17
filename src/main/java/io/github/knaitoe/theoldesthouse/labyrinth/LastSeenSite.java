package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.667, Last Seen (docs/LAST_SEEN_0_4_667.md): a city park in the morning, under the outside sky, and the low municipal office at its
 * edge where the archive of missing children is kept. Every position is relative to the base, the floor just inside the House's door; north
 * is -z; people stand at y 0 on ground at y -1.
 *
 * <p>The House's door is in the office's south wall, so a reader comes in at the front desk: a counter with a typed card on it, two chairs
 * behind it, benches for whoever comes to ask, and on the partition to the left the poster wall, a face and two lines of type to each
 * poster. Behind the counter a doorway in the partition leads into the archive: steel shelves of folders to the ceiling on three walls, a
 * long table under its lamp, and Nora's desk with the typewriter and its one drawer. A door from each side opens north onto the park: a
 * paved front, paths in a cross, a dry fountain where they meet, a fenced playground, a bandstand, a shut kiosk, benches and globe lamps,
 * plane trees and the swollen, spined trunks of silk-floss trees, and an iron railing all round. Past the north gates are the avenue and a
 * bus stop, and across it a row of houses facing the park. A door in one of them is the way on.
 */
public final class LastSeenSite {
    private LastSeenSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LAST_SEEN;
    public static final int WEST=-42,EAST=26,NORTH=-110;
    /** The office: its four walls (the south one is the House's, with the door at x 0), the partition between the front desk and the
     * archive, the counter's line, its roof. */
    public static final int OFFICE_W=-24,OFFICE_E=8,OFFICE_S=0,OFFICE_N=-14,PARTITION=-8,COUNTER_Z=-6,CEILING=5;
    /** The park inside its railing, the paved front along the office, the cross of paths, and the fountain where they meet. */
    public static final int PARK_W=-40,PARK_E=24,PARK_N=-85,FRONT_N=-17,PATH_X=-8,PATH_Z=-50;
    public static final BlockPos FOUNTAIN=new BlockPos(-8,0,-50);
    /** The avenue: its near pavement, curbs and road, its far pavement, and the houses' fronts. */
    public static final int NEAR_PAVE=-86,NEAR_CURB=-88,ROAD_S=-89,ROAD_N=-95,FAR_CURB=-96,FAR_PAVE=-98,HOUSE_FRONT=-99,HOUSE_BACK=-108;
    /** The office's two doors onto the park: the front desk's and the archive's. The park's gate leaves in the north railing. */
    public static final BlockPos FRONT_DOOR=new BlockPos(0,0,OFFICE_N),BACK_DOOR=new BlockPos(-16,0,OFFICE_N),
        GATE_W=new BlockPos(-9,0,PARK_N),GATE_E=new BlockPos(-8,0,PARK_N);
    /** The typed card on the counter (the first paper); where each reader's account will wait, Nora's desk. */
    public static final BlockPos CARD=new BlockPos(0,1,COUNTER_Z),DESK=new BlockPos(-11,0,-4);
    /** The way on: a door in the houses across the avenue, facing the park. */
    public static final BlockPos ONWARD=new BlockPos(-16,0,HOUSE_FRONT);
    /** Where the three stand or sit: Nora behind her desk, Patricia and Luz in the chairs behind the counter. */
    public static final BlockPos NORA_AT=new BlockPos(-12,0,-4),PATRICIA_AT=new BlockPos(-3,0,-8),LUZ_AT=new BlockPos(3,0,-8);
    /** The shelf bays, floor to ceiling: the archive's west wall, its south wall and its north wall beside the back door. */
    public static final List<BlockPos> SHELVES;
    static{
        var out=new ArrayList<BlockPos>();
        for(int z=-1;z>=-12;z--)out.add(new BlockPos(OFFICE_W+1,0,z));
        for(int x=OFFICE_W+2;x<=PARTITION-2;x++)out.add(new BlockPos(x,0,OFFICE_S-1));
        for(int x=OFFICE_W+2;x<=PARTITION-2;x++)if(Math.abs(x-BACK_DOOR.getX())>1)out.add(new BlockPos(x,0,OFFICE_N+1));
        SHELVES=List.copyOf(out);
    }
    /** The poster wall: two rows on the partition facing the desk, then a third row above the first three. Sixteen posters. */
    public static final List<BlockPos> POSTERS;
    static{
        var out=new ArrayList<BlockPos>();
        for(int y=2;y>=1;y--)for(int z=-1;z>=-5;z--)out.add(new BlockPos(PARTITION+1,y,z));
        for(int z=-1;z>=-4;z--)out.add(new BlockPos(PARTITION+1,3,z));
        for(int z=-9;z>=-10;z--)out.add(new BlockPos(PARTITION+1,2,z));
        POSTERS=List.copyOf(out);
    }
    /** The plane trees along the paths, and the three silk-floss trees. */
    static final int[][] PLANES={{-12,-24},{-4,-24},{-12,-36},{-4,-36},{-12,-64},{-4,-64},{-12,-76},{-4,-76},{-30,-46},{-30,-54},{14,-46},{14,-54},{-34,-22},{20,-80}};
    static final int[][] SILKS={{-24,-30},{12,-68},{-32,-76}};
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ where things are
    public static boolean inOffice(Vec3 r){return r.x>OFFICE_W+.5&&r.x<OFFICE_E+.5&&r.z<OFFICE_S-.3&&r.z>OFFICE_N+.5&&r.y>-.5&&r.y<CEILING;}
    public static boolean inArchive(Vec3 r){return inOffice(r)&&r.x<PARTITION+.5;}
    public static boolean atDesk(Vec3 r){return inOffice(r)&&r.x>PARTITION+.5;}
    public static boolean inPark(Vec3 r){return r.x>PARK_W+.5&&r.x<PARK_E+.5&&r.z<OFFICE_N+.5&&r.z>PARK_N+.5&&r.y>-1&&r.y<12;}
    public static boolean onAvenue(Vec3 r){return r.z<PARK_N+.5&&r.z>HOUSE_FRONT+.5&&r.x>WEST+.5&&r.x<EAST+.5;}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    static BlockState mat(String name){return LastSeenRegistry.material(name).defaultBlockState();}
    static BlockState fit(LastSeenBlock.Kind kind,Direction facing,int stage){return LastSeenBlock.of(kind,facing,stage);}
    private static boolean air(ServerLevel l,BlockPos b,int x,int y,int z){return l.getBlockState(b.offset(x,y,z)).isAir();}

    public static void build(ServerLevel l,BlockPos b){
        var rnd=new Random(66793);
        box(l,b,WEST,-4,NORTH,EAST,16,0,Blocks.AIR.defaultBlockState());
        box(l,b,WEST,-4,NORTH,EAST,-2,0,Blocks.DIRT.defaultBlockState());
        box(l,b,WEST,-1,NORTH,EAST,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        office(l,b);
        archive(l,b,rnd);
        desk(l,b);
        park(l,b,rnd);
        fountain(l,b);
        playground(l,b);
        bandstand(l,b);
        kiosk(l,b);
        trees(l,b,rnd);
        benchesAndLamps(l,b);
        railing(l,b);
        avenue(l,b);
        houses(l,b,rnd);
    }

    /** The office's shell: render over a darker plinth, a flat roof behind a parapet, louvred windows, a floor of worn tiles, tube lights. */
    static void office(ServerLevel l,BlockPos b){
        box(l,b,OFFICE_W,-1,OFFICE_N,OFFICE_E,-1,OFFICE_S,mat("floor"));
        for(int x=OFFICE_W;x<=OFFICE_E;x++)for(int z=OFFICE_N;z<=OFFICE_S;z++){
            boolean wall=x==OFFICE_W||x==OFFICE_E||z==OFFICE_N||z==OFFICE_S;
            if(wall){set(l,b,x,0,z,mat("plinth"));for(int y=1;y<CEILING;y++)set(l,b,x,y,z,mat("render"));}
            else for(int y=0;y<CEILING;y++)set(l,b,x,y,z,Blocks.AIR.defaultBlockState());
            set(l,b,x,CEILING,z,mat("ceiling"));
            if(wall)set(l,b,x,CEILING+1,z,mat("render"));
        }
        // The partition between the front desk and the archive, with its doorway behind the counter.
        for(int z=OFFICE_N+1;z<OFFICE_S;z++)for(int y=0;y<CEILING;y++)set(l,b,PARTITION,y,z,mat("render"));
        for(int y=0;y<=1;y++)set(l,b,PARTITION,y,-11,Blocks.AIR.defaultBlockState());
        // The two doors north onto the park.
        door(l,b,FRONT_DOOR,Direction.NORTH);door(l,b,BACK_DOOR,Direction.NORTH);
        // Louvred windows: north onto the park, east onto the side, and in the archive's north wall either side of its door.
        for(int x:new int[]{-21,-20,-12,-11,3,4,5})for(int y=1;y<=2;y++)set(l,b,x,y,OFFICE_N,fit(LastSeenBlock.Kind.LOUVRE,Direction.NORTH,0));
        for(int z:new int[]{-3,-4,-9,-10})for(int y=1;y<=2;y++)set(l,b,OFFICE_E,y,z,fit(LastSeenBlock.Kind.LOUVRE,Direction.EAST,0));
        // Tube lights on the ceiling of each side.
        for(int[] at:new int[][]{{-2,-3},{4,-3},{-2,-10},{4,-10},{-19,-4},{-14,-4},{-19,-10},{-14,-10}})set(l,b,at[0],CEILING-1,at[1],fit(LastSeenBlock.Kind.TUBE,Direction.NORTH,0));
        // The front desk: the counter across the room with its flap at the east end, the card on it, the chairs behind it, benches in front.
        for(int x=-7;x<=5;x++)set(l,b,x,0,COUNTER_Z,fit(LastSeenBlock.Kind.COUNTER,Direction.SOUTH,0));
        set(l,b,6,0,COUNTER_Z,fit(LastSeenBlock.Kind.COUNTER,Direction.SOUTH,1));
        set(l,b,CARD,fit(LastSeenBlock.Kind.CARD,Direction.SOUTH,0));
        set(l,b,PATRICIA_AT,fit(LastSeenBlock.Kind.CHAIR,Direction.SOUTH,0));set(l,b,LUZ_AT,fit(LastSeenBlock.Kind.CHAIR,Direction.SOUTH,0));
        for(int x=-6;x<=-4;x++)set(l,b,x,0,-1,fit(LastSeenBlock.Kind.BENCH,Direction.NORTH,x==-6?0:x==-5?1:2));
        for(int x=3;x<=5;x++)set(l,b,x,0,-1,fit(LastSeenBlock.Kind.BENCH,Direction.NORTH,x==3?0:x==4?1:2));
        // The poster wall: the first ten are the ten who come back, the rest are faces nobody brings.
        for(int i=0;i<POSTERS.size();i++)set(l,b,POSTERS.get(i),fit(i<8?LastSeenBlock.Kind.POSTER:LastSeenBlock.Kind.POSTER_B,Direction.EAST,i%8));
    }
    /** A plain wooden door, its lower half at the cell and its upper above, set into the wall it faces out of. */
    static void door(ServerLevel l,BlockPos b,BlockPos at,Direction out){
        var d=Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,out).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,false);
        set(l,b,at,d.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,at.above(),d.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    /** Shelf bays on three walls, the top bay of each with archive boxes; the long table down the middle with its lamp and the day's pile. */
    static void archive(ServerLevel l,BlockPos b,Random rnd){
        for(var at:SHELVES){
            var facing=at.getX()==OFFICE_W+1?Direction.EAST:at.getZ()==OFFICE_S-1?Direction.NORTH:Direction.SOUTH;
            for(int y=0;y<CEILING-1;y++)set(l,b,at.getX(),y,at.getZ(),fit(LastSeenBlock.Kind.SHELF,facing,rnd.nextInt(3)));
            set(l,b,at.getX(),CEILING-1,at.getZ(),fit(LastSeenBlock.Kind.SHELF,facing,LastSeenBlock.BOXES));
        }
        for(int x=-19;x<=-14;x++)set(l,b,x,0,-7,fit(LastSeenBlock.Kind.TABLE,Direction.NORTH,x==-19?2:x==-17?1:0));
        for(int x:new int[]{-18,-16,-15})set(l,b,x,0,-8,fit(LastSeenBlock.Kind.CHAIR,Direction.NORTH,0));
    }
    /** Nora's desk, turned to her chair, the typewriter on it and the one drawer; her chair behind it. */
    static void desk(ServerLevel l,BlockPos b){
        set(l,b,DESK,fit(LastSeenBlock.Kind.DESK,Direction.WEST,LastSeenBlock.LOCKED));
        set(l,b,NORA_AT,fit(LastSeenBlock.Kind.CHAIR,Direction.EAST,0));
    }

    /** The park's ground: the paved front along the office, the cross of paths and the plaza round the fountain, the beds under the trees. */
    static void park(ServerLevel l,BlockPos b,Random rnd){
        box(l,b,PARK_W+1,-1,FRONT_N,PARK_E-1,-1,OFFICE_N-1,mat("paving"));
        box(l,b,PATH_X-1,-1,PARK_N+1,PATH_X+1,-1,FRONT_N,mat("paving"));
        box(l,b,PARK_W+1,-1,PATH_Z-1,PARK_E-1,-1,PATH_Z+1,mat("paving"));
        for(int dx=-8;dx<=8;dx++)for(int dz=-8;dz<=8;dz++){double r=Math.sqrt(dx*dx+dz*dz);if(r<=7.5)set(l,b,FOUNTAIN.getX()+dx,-1,FOUNTAIN.getZ()+dz,mat("paving"));else if(r<=8.4)set(l,b,FOUNTAIN.getX()+dx,-1,FOUNTAIN.getZ()+dz,mat("curb"));}
        // Curbs along the paths' edges where they meet the grass.
        for(int z=PARK_N+1;z<=FRONT_N;z++)for(int x:new int[]{PATH_X-2,PATH_X+2})if(l.getBlockState(b.offset(x,-1,z)).is(Blocks.GRASS_BLOCK))set(l,b,x,-1,z,mat("curb"));
        for(int x=PARK_W+1;x<=PARK_E-1;x++)for(int z:new int[]{PATH_Z-2,PATH_Z+2})if(l.getBlockState(b.offset(x,-1,z)).is(Blocks.GRASS_BLOCK))set(l,b,x,-1,z,mat("curb"));
        // A few worn patches in the grass where people cut across.
        for(int i=0;i<40;i++){int x=PARK_W+2+rnd.nextInt(PARK_E-PARK_W-3),z=PARK_N+2+rnd.nextInt(FRONT_N-PARK_N-3);if(l.getBlockState(b.offset(x,-1,z)).is(Blocks.GRASS_BLOCK))set(l,b,x,-1,z,mat("earth"));}
    }
    /** The dry fountain: a ring of rim round a dry basin, and a plinth at the centre with its bowl on top. */
    static void fountain(ServerLevel l,BlockPos b){
        int cx=FOUNTAIN.getX(),cz=FOUNTAIN.getZ();
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){
            double r=Math.sqrt(dx*dx+dz*dz);
            if(r>2.5&&r<=3.5){
                boolean corner=Math.abs(dx)==Math.abs(dz)||Math.abs(dx)==2&&Math.abs(dz)==3||Math.abs(dx)==3&&Math.abs(dz)==2;
                Direction out=Math.abs(dx)>=Math.abs(dz)?(dx>0?Direction.EAST:Direction.WEST):(dz>0?Direction.SOUTH:Direction.NORTH);
                set(l,b,cx+dx,0,cz+dz,fit(LastSeenBlock.Kind.FOUNTAIN,out,corner?1:0));
            }else if(r<=2.5)set(l,b,cx+dx,-1,cz+dz,mat("stone"));
        }
        set(l,b,cx,0,cz,fit(LastSeenBlock.Kind.FOUNTAIN,Direction.NORTH,2));set(l,b,cx,1,cz,fit(LastSeenBlock.Kind.FOUNTAIN,Direction.NORTH,3));
    }
    /** The playground on sand in the north-east, inside a low fence: the swings on their frame, the slide, the see-saw. */
    static void playground(ServerLevel l,BlockPos b){
        int x0=6,x1=20,z0=-38,z1=-22;
        box(l,b,x0,-1,z0,x1,-1,z1,Blocks.SAND.defaultBlockState());
        for(int x=x0;x<=x1;x++){set(l,b,x,0,z0,fit(LastSeenBlock.Kind.PLAY,Direction.NORTH,5));if(x<12||x>14)set(l,b,x,0,z1,fit(LastSeenBlock.Kind.PLAY,Direction.NORTH,5));}
        for(int z=z0+1;z<z1;z++){set(l,b,x0,0,z,fit(LastSeenBlock.Kind.PLAY,Direction.EAST,5));set(l,b,x1,0,z,fit(LastSeenBlock.Kind.PLAY,Direction.EAST,5));}
        // The swing frame: two posts and the bar between them, two swings on their chains.
        for(int y=0;y<=2;y++){set(l,b,9,y,-27,fit(LastSeenBlock.Kind.PLAY,Direction.EAST,0));set(l,b,15,y,-27,fit(LastSeenBlock.Kind.PLAY,Direction.EAST,0));}
        for(int x=9;x<=15;x++)set(l,b,x,3,-27,fit(LastSeenBlock.Kind.PLAY,Direction.EAST,6));
        for(int x:new int[]{11,13}){set(l,b,x,2,-27,fit(LastSeenBlock.Kind.PLAY,Direction.EAST,7));set(l,b,x,1,-27,fit(LastSeenBlock.Kind.PLAY,Direction.EAST,1));}
        // The slide: its ladder at the top end, the chute coming down to the south.
        for(int y=0;y<=2;y++)set(l,b,17,y,-34,fit(LastSeenBlock.Kind.PLAY,Direction.SOUTH,2));
        set(l,b,17,2,-33,fit(LastSeenBlock.Kind.PLAY,Direction.SOUTH,3));set(l,b,17,1,-32,fit(LastSeenBlock.Kind.PLAY,Direction.SOUTH,3));set(l,b,17,0,-31,fit(LastSeenBlock.Kind.PLAY,Direction.SOUTH,3));
        for(int x=8;x<=10;x++)set(l,b,x,0,-34,fit(LastSeenBlock.Kind.PLAY,Direction.EAST,4));
    }
    /** The bandstand to the west: a raised octagon of stone, posts and a balustrade, steps up from the east, and a tiled roof. */
    static void bandstand(ServerLevel l,BlockPos b){
        int cx=-28,cz=-62;
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++){if(Math.abs(dx)+Math.abs(dz)>6)continue;set(l,b,cx+dx,-1,cz+dz,mat("stone"));set(l,b,cx+dx,0,cz+dz,mat("stone"));}
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++){
            int m=Math.abs(dx)+Math.abs(dz);boolean edge=(Math.abs(dx)==4||Math.abs(dz)==4)&&m<=6||m==6;
            if(!edge)continue;
            boolean post=Math.abs(dx)==4&&Math.abs(dz)==2||Math.abs(dz)==4&&Math.abs(dx)==2;
            if(post){for(int y=1;y<=3;y++)set(l,b,cx+dx,y,cz+dz,fit(LastSeenBlock.Kind.BANDSTAND,Direction.NORTH,0));}
            else if(!(dx==4&&Math.abs(dz)<=1))set(l,b,cx+dx,1,cz+dz,fit(LastSeenBlock.Kind.BANDSTAND,Math.abs(dx)>=Math.abs(dz)?Direction.EAST:Direction.NORTH,1));
        }
        for(int dz=-1;dz<=1;dz++)set(l,b,cx+5,0,cz+dz,fit(LastSeenBlock.Kind.BANDSTAND,Direction.EAST,3));
        // The roof: an eave on the posts, its iron valance hung on its outer face (on both at the corners), then rising courses of tile.
        for(int dx=-5;dx<=5;dx++)for(int dz=-5;dz<=5;dz++){
            int m=Math.abs(dx)+Math.abs(dz);
            if(Math.max(Math.abs(dx),Math.abs(dz))>4||m>7||!(Math.abs(dx)==4||Math.abs(dz)==4||m==7))continue;
            boolean corner=Math.abs(dx)>=3&&Math.abs(dz)>=3;
            Direction out=corner?(dx<0?(dz<0?Direction.NORTH:Direction.WEST):(dz<0?Direction.EAST:Direction.SOUTH))
                :Math.abs(dx)>Math.abs(dz)?(dx>0?Direction.EAST:Direction.WEST):(dz>0?Direction.SOUTH:Direction.NORTH);
            set(l,b,cx+dx,4,cz+dz,fit(LastSeenBlock.Kind.BANDSTAND,out,corner?4:2));
        }
        // Courses of tile rising to a point: each an octagon one smaller than the one below.
        int[][] tiers={{4,6},{3,4},{2,3},{1,1},{0,0}};
        for(int i=0;i<tiers.length;i++)for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++){
            int mx=Math.max(Math.abs(dx),Math.abs(dz)),m=Math.abs(dx)+Math.abs(dz);
            boolean in=mx<=tiers[i][0]&&m<=tiers[i][1],inner=i+1<tiers.length&&mx<=tiers[i+1][0]&&m<=tiers[i+1][1];
            if(in&&!inner)set(l,b,cx+dx,5+i,cz+dz,Blocks.DEEPSLATE_TILES.defaultBlockState());
        }
    }
    /** The kiosk by the cross, its shutters down: the front onto the path, its sides papered with old bills, a flat roof. */
    static void kiosk(ServerLevel l,BlockPos b){
        int x0=-4,z0=-58;
        for(int dx=0;dx<=2;dx++)for(int dz=0;dz<=2;dz++){
            if(dx==1&&dz==1){for(int y=0;y<=2;y++)set(l,b,x0+dx,y,z0+dz,Blocks.AIR.defaultBlockState());continue;}
            Direction out=dx==0?Direction.WEST:dx==2?Direction.EAST:dz==0?Direction.NORTH:Direction.SOUTH;
            for(int y=0;y<=2;y++)set(l,b,x0+dx,y,z0+dz,fit(LastSeenBlock.Kind.KIOSK,out,out==Direction.WEST?0:1));
        }
        box(l,b,x0-1,3,z0-1,x0+3,3,z0+3,Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
    }
    /** Plane trees in their beds along the paths, and three silk-floss trees with their swollen, spined trunks. */
    static void trees(ServerLevel l,BlockPos b,Random rnd){
        for(int[] t:PLANES){
            int h=4+rnd.nextInt(2);
            set(l,b,t[0],-1,t[1],mat("earth"));
            for(int y=0;y<=h;y++)set(l,b,t[0],y,t[1],mat("bark"));
            canopy(l,b,t[0],h,t[1],3,0,rnd);
        }
        for(int[] t:SILKS){
            set(l,b,t[0],-1,t[1],mat("earth"));
            for(int y=0;y<=3;y++)set(l,b,t[0],y,t[1],fit(LastSeenBlock.Kind.TRUNK,Direction.from2DDataValue(rnd.nextInt(4)),y));
            canopy(l,b,t[0],4,t[1],2,1,rnd);
        }
    }
    /** A crown of leaves: a smaller layer under it round the trunk so it rounds off below, its widest in the middle, a smaller layer on top,
     * and some of the outermost leaves of each layer left out so the edge is ragged. */
    static void canopy(ServerLevel l,BlockPos b,int cx,int y0,int cz,int radius,int kind,Random rnd){
        for(int dy=-1;dy<=2;dy++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
            double r=Math.sqrt(dx*dx+dz*dz)+(dy==2?1.2:dy==0?.4:dy==-1?1.5:0);
            if(r>radius+.3)continue;
            if(dx==0&&dz==0&&dy<=0)continue;
            if(r>radius-.5&&rnd.nextInt(3)==0)continue;
            int x=cx+dx,y=y0+dy,z=cz+dz;
            if(x<=PARK_W||x>=PARK_E||z>=OFFICE_N||z<=PARK_N)continue;
            if(!air(l,b,x,y,z))continue;
            set(l,b,x,y,z,fit(LastSeenBlock.Kind.LEAVES,Direction.from2DDataValue(rnd.nextInt(4)),kind==1?1:r>radius-.8?2:0));
        }
    }
    /** Benches along the paths and round the plaza, and globe lamps on posts. */
    /** The globe lamps along the paths and the paved front: the cell of each post's shaft, where the city's notices go (0.4.669). */
    static final int[][] LAMP_POSTS={{PATH_X-2,-22},{PATH_X+2,-40},{PATH_X-2,-60},{PATH_X+2,-78},{-24,PATH_Z+2},{8,PATH_Z-2},{-36,PATH_Z-2},{20,PATH_Z+2},{-20,FRONT_N},{4,FRONT_N}};
    public static final List<BlockPos> LAMPS;
    static{var out=new ArrayList<BlockPos>();for(int[] at:LAMP_POSTS)out.add(new BlockPos(at[0],1,at[1]));LAMPS=List.copyOf(out);}
    /** A notice on a post faces the path it stands by, so it is read from the path. */
    public static Direction noticeFacing(BlockPos lamp){
        if(Math.abs(lamp.getX()-PATH_X)==2)return lamp.getX()<PATH_X?Direction.EAST:Direction.WEST;
        if(Math.abs(lamp.getZ()-PATH_Z)==2)return lamp.getZ()<PATH_Z?Direction.SOUTH:Direction.NORTH;
        return Direction.NORTH;
    }
    static void benchesAndLamps(ServerLevel l,BlockPos b){
        for(int z:new int[]{-29,-43,-59,-71})bench(l,b,PATH_X-2,z,Direction.EAST);
        for(int z:new int[]{-31,-45,-61,-73})bench(l,b,PATH_X+2,z,Direction.WEST);
        for(int x:new int[]{-30,-22,4,14})bench(l,b,x,PATH_Z-2,Direction.SOUTH);
        for(int[] at:LAMP_POSTS)lamp(l,b,at[0],at[1],true);
    }
    static void bench(ServerLevel l,BlockPos b,int x,int z,Direction faces){
        var along=faces.getClockWise();
        for(int i=-1;i<=1;i++){var at=new BlockPos(x,0,z).relative(along,i);set(l,b,at,fit(LastSeenBlock.Kind.BENCH,faces,i==-1?2:i==0?1:0));}
    }
    static void lamp(ServerLevel l,BlockPos b,int x,int z,boolean lit){
        set(l,b,x,0,z,fit(LastSeenBlock.Kind.LAMP_POST,Direction.NORTH,LastSeenBlock.FOOT));
        for(int y=1;y<=2;y++)set(l,b,x,y,z,fit(LastSeenBlock.Kind.LAMP_POST,Direction.NORTH,LastSeenBlock.SHAFT));
        set(l,b,x,3,z,fit(LastSeenBlock.Kind.LAMP_POST,Direction.NORTH,lit?LastSeenBlock.LIT:LastSeenBlock.DARK));
    }
    /** The iron railing all round, posts every fourth bar, joining the office's corners; the north gates, shut. */
    static void railing(ServerLevel l,BlockPos b){
        for(int x=PARK_W;x<=PARK_E;x++){
            run(l,b,x,PARK_N,Direction.NORTH,(x-PARK_W)%4==0);
            if(x<OFFICE_W||x>OFFICE_E)run(l,b,x,OFFICE_N,Direction.NORTH,(x-PARK_W)%4==0);
        }
        for(int z=PARK_N+1;z<OFFICE_N;z++){run(l,b,PARK_W,z,Direction.EAST,(z-PARK_N)%4==0);run(l,b,PARK_E,z,Direction.EAST,(z-PARK_N)%4==0);}
        gate(l,b,LastSeenBlock.SHUT);
    }
    static void run(ServerLevel l,BlockPos b,int x,int z,Direction along,boolean post){set(l,b,x,0,z,fit(LastSeenBlock.Kind.RAILING,along,post?1:0));set(l,b,x,1,z,fit(LastSeenBlock.Kind.RAILING,along,post?1:0));}
    /** The north gates, two leaves between their posts: shut, shut and chained, or open. */
    public static void gate(ServerLevel l,BlockPos b,int stage){
        for(var at:List.of(GATE_W,GATE_E))for(int y=0;y<=1;y++)set(l,b,at.getX(),y,at.getZ(),fit(LastSeenBlock.Kind.GATE,at==GATE_W?Direction.NORTH:Direction.SOUTH,stage));
        for(int x:new int[]{GATE_W.getX()-1,GATE_E.getX()+1})for(int y=0;y<=2;y++)set(l,b,x,y,PARK_N,fit(LastSeenBlock.Kind.RAILING,Direction.NORTH,1));
    }
    /** A tall window of a house: its low half and its top half, the blind down. */
    static void window(ServerLevel l,BlockPos b,int x,int y,int z,Direction faces){
        set(l,b,x,y,z,fit(LastSeenBlock.Kind.SHUTTER,faces,2));set(l,b,x,y+1,z,fit(LastSeenBlock.Kind.SHUTTER,faces,3));
    }
    /** A house's front, from x0 to x1 in the plane z: its own render, stone at its two corners, a darker plinth along its foot and a band of
     * stone between its storeys. */
    static void front(ServerLevel l,BlockPos b,int x0,int x1,int z,int top,String render){
        for(int x=x0;x<=x1;x++)for(int y=0;y<=top;y++)set(l,b,x,y,z,mat(x==x0||x==x1?"stone":y==0?"plinth":y==4?"stone":render));
    }
    /** A run of wall along z at x, dressed as the sides and backs of the houses that close the scene: their renders by turns, stone where
     * one house ends and the next begins, a plinth, a band between the storeys, and windows with their blinds down. */
    static void backs(ServerLevel l,BlockPos b,int x,int z0,int z1,Direction faces){
        String[] renders={"house","house_b","house_c"};
        for(int z=z0;z<=z1;z++){
            int k=Math.floorMod(z-z0,12);String render=renders[Math.floorMod((z-z0)/12,renders.length)];
            for(int y=0;y<=8;y++)set(l,b,x,y,z,mat(k==0?"stone":y==0?"plinth":y==4?"stone":render));
        }
        for(int z=z0+3;z<=z1-2;z+=4){
            int k=Math.floorMod(z-z0,12);
            if(k<2||k>10)continue;
            window(l,b,x,5,z,faces);
            if(Math.floorMod((z-z0)/4,2)==1)window(l,b,x,1,z,faces);
        }
    }
    /** The avenue past the railing: pavement, curb, asphalt with its centre line and a crossing at the gate, curb and pavement; the bus
     * stop on the near side. */
    static void avenue(ServerLevel l,BlockPos b){
        box(l,b,WEST,-1,NEAR_CURB+1,EAST,-1,PARK_N-1,mat("pavement"));
        box(l,b,WEST,-1,NEAR_CURB,EAST,-1,NEAR_CURB,mat("curb"));
        box(l,b,WEST,-1,ROAD_N,EAST,-1,ROAD_S,mat("asphalt"));
        box(l,b,WEST,-1,FAR_CURB,EAST,-1,FAR_CURB,mat("curb"));
        box(l,b,WEST,-1,FAR_PAVE,EAST,-1,FAR_CURB-1,mat("pavement"));
        // The centre line, dashed, broken for the crossing from the park's gate to the far pavement.
        int cross0=GATE_W.getX()-2,cross1=GATE_E.getX()+2,centre=(ROAD_S+ROAD_N)/2;
        for(int x=WEST;x<=EAST;x++)if((x<cross0-1||x>cross1+1)&&Math.floorMod(x,4)<2)set(l,b,x,-1,centre,mat("road_line"));
        for(int z=ROAD_N;z<=ROAD_S;z++)if(Math.floorMod(z-ROAD_N,2)==1)for(int x=cross0;x<=cross1;x++)set(l,b,x,-1,z,mat("crossing"));
        // The shelter: glass at each end, a roof over the bench, the stop's sign at the curb.
        for(int y=0;y<=2;y++){set(l,b,12,y,NEAR_PAVE,fit(LastSeenBlock.Kind.SHELTER,Direction.EAST,0));set(l,b,16,y,NEAR_PAVE,fit(LastSeenBlock.Kind.SHELTER,Direction.EAST,0));}
        for(int x=12;x<=16;x++)set(l,b,x,3,NEAR_PAVE,fit(LastSeenBlock.Kind.SHELTER,Direction.NORTH,1));
        for(int x=13;x<=15;x++)set(l,b,x,0,NEAR_PAVE,fit(LastSeenBlock.Kind.SHELTER,Direction.SOUTH,3));
        for(int y=0;y<=2;y++)set(l,b,18,y,NEAR_PAVE-1,fit(LastSeenBlock.Kind.SHELTER,Direction.NORTH,2));
        // The scene's ends: a house's side across each end of the avenue, so it turns out of sight.
        for(int x:new int[]{WEST,EAST})box(l,b,x,-1,HOUSE_FRONT,x,8,PARK_N-1,mat("house"));
        // West and east of the park, beyond the railing: a strip of pavement, then the sides and backs of houses, which run on along the
        // avenue's ends; their windows look in on the park and the road.
        for(int x:new int[]{WEST,WEST+1,EAST-1,EAST}){box(l,b,x,-1,PARK_N,x,-1,OFFICE_N,mat("pavement"));}
        box(l,b,WEST,0,PARK_N,WEST,8,OFFICE_S,mat("house"));box(l,b,EAST,0,PARK_N,EAST,8,OFFICE_S,mat("house"));
        backs(l,b,WEST,HOUSE_FRONT+1,OFFICE_S-1,Direction.EAST);backs(l,b,EAST,HOUSE_FRONT+1,OFFICE_S-1,Direction.WEST);
        // The office stands in a row: two houses either side of it face the park over their own strips of garden, blinds down.
        int[][] row={{WEST,-34},{-33,OFFICE_W-1},{OFFICE_E+1,17},{18,EAST}};
        String[] renders={"house_c","house","house_b","house_c"};
        for(int i=0;i<row.length;i++){
            front(l,b,row[i][0],row[i][1],OFFICE_S,8,renders[i]);
            for(int x=row[i][0]+2;x<=row[i][1]-2;x+=3){window(l,b,x,1,OFFICE_S,Direction.NORTH);window(l,b,x,5,OFFICE_S,Direction.NORTH);}
        }
        for(int x=WEST;x<=EAST;x++)if(x<OFFICE_W||x>OFFICE_E)set(l,b,x,8,OFFICE_S-1,Blocks.STONE_BRICK_SLAB.defaultBlockState());
    }
    /** The houses across the avenue: four fronts of two storeys, each its own render with stone at its corners, a plinth and a band between
     * its storeys, a door in a stone surround and tall windows with their blinds down, and a parapet over them. */
    static void houses(ServerLevel l,BlockPos b,Random rnd){
        box(l,b,WEST,-1,HOUSE_BACK,EAST,9,HOUSE_FRONT,mat("house"));
        box(l,b,WEST,10,HOUSE_BACK,EAST,10,HOUSE_FRONT,Blocks.AIR.defaultBlockState());
        int[][] fronts={{-40,-25},{-24,-9},{-8,8},{9,24}};
        String[] renders={"house_b","house","house_c","house_b"};
        for(int i=0;i<fronts.length;i++){
            int[] h=fronts[i];
            front(l,b,h[0],h[1],HOUSE_FRONT,9,renders[i]);
            int door=h[0]+3+rnd.nextInt(3);if(h[0]==-24)door=ONWARD.getX();
            for(int y=0;y<=2;y++){set(l,b,door-1,y,HOUSE_FRONT,mat("stone"));set(l,b,door+1,y,HOUSE_FRONT,mat("stone"));}
            set(l,b,door,2,HOUSE_FRONT,mat("stone"));
            if(door!=ONWARD.getX())door(l,b,new BlockPos(door,0,HOUSE_FRONT),Direction.SOUTH);
            for(int x=h[0]+2;x<=h[1]-2;x+=3){
                if(Math.abs(x-door)>1)window(l,b,x,1,HOUSE_FRONT,Direction.SOUTH);
                window(l,b,x,5,HOUSE_FRONT,Direction.SOUTH);
            }
            for(int x=h[0];x<=h[1];x++)set(l,b,x,8,HOUSE_FRONT+1,Blocks.STONE_BRICK_SLAB.defaultBlockState());
        }
    }
}
