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
 * 0.4.408, The House on the Corner (docs/THE_HOUSE_ON_THE_CORNER_0_4_408.md): a city street at siesta, under the outside sky. Every
 * position is relative to the base, the ground just inside the door the reader comes through; north is -z; people stand at y 0 on
 * ground at y -1.
 *
 * <p>Out of the House's door, in the side wall of a house, a street of low rendered houses runs north between pavements. The houses have
 * balconies and their shutters are down for the afternoon. There are jacarandas on the pavements, their petals in the gutters, a kiosk,
 * and a dog asleep. At the end it meets a cross street. On the far corner to the right, behind a tall iron fence and a garden nobody
 * keeps, stands the empty house. Its front room is clean and lit. On the wall to the left are boards with glass jars. A door at the back
 * of the room opens onto a corridor longer than the house could hold; behind the house a blank apartment block stands over it, and the
 * corridor runs on inside it to a last door. Opposite the main street, across the cross street, a door in the row of houses there is the
 * way on.
 */
public final class CornerSite {
    private CornerSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.CORNER_HOUSE;
    public static final int WEST=-36,EAST=44,NORTH=-122;
    /** The main street: road between kerbs, pavements, the two rows of houses' fronts. */
    public static final int ROAD=3,KERB=4,PAVE=8,FRONT=9,ROW_END=-55;
    /** The cross street: its south pavement, kerb, road, north kerb and pavement, and the row of houses beyond it. */
    public static final int CROSS_PAVE_S=-56,CROSS_KERB_S=-58,CROSS_ROAD_S=-59,CROSS_ROAD_N=-65,CROSS_KERB_N=-66,CROSS_PAVE_N=-68,NORTH_FRONT=-69;
    /** The house on the corner: its lot, its garden, its walls, the front room inside the lining, and the corridor. */
    public static final int LOT_W=9,LOT_E=43,GARDEN_S=-70,GARDEN_N=-76,HOUSE_W=11,HOUSE_E=25,HOUSE_S=-77,HOUSE_N=-91;
    public static final int ROOM_W=13,ROOM_E=23,ROOM_S=-79,ROOM_N=-84,HALL_W=17,HALL_E=19,HALL_S=-86,HALL_N=-119;
    /** A post in the lot's fence at every third cell. */
    public static final int FENCE_POSTS=3;
    /** The gate's two cells, the front door, the back door into the corridor, and the last door at its end. */
    public static final BlockPos GATE_W=new BlockPos(18,0,-69),GATE_E=new BlockPos(19,0,-69),FRONT_DOOR=new BlockPos(18,0,-77),BACK_DOOR=new BlockPos(18,0,-85),
        LAST_DOOR=new BlockPos(18,0,-120),NICHE=new BlockPos(18,0,-121);
    /** The way on: a door in the row across the cross street, facing up the main street. */
    public static final BlockPos ONWARD=new BlockPos(0,0,-69);
    /** On the cross street's kerb, facing the gate: the exercise book (the first paper), and where each reader's account waits. */
    public static final BlockPos NOTEBOOK=new BlockPos(16,0,-57),NOTE=new BlockPos(18,0,-56);
    /** Where the children stand on the kerb: the brother, the girl, their friend. */
    public static final Vec3 KERB_NACHO=new Vec3(17.5,0,-56.5),KERB_ROMINA=new Vec3(18.5,0,-56.5),KERB_LULI=new Vec3(19.5,0,-56.5);
    /** The jar boards on the front room's west wall: twelve places, nearest the door first. */
    public static final List<BlockPos> JARS;
    static{var out=new ArrayList<BlockPos>();for(int z=ROOM_S;z>=ROOM_N;z--)out.add(new BlockPos(ROOM_W,1,z));for(int z=ROOM_S;z>=ROOM_N;z--)out.add(new BlockPos(ROOM_W,2,z));JARS=List.copyOf(out);}
    /** The jars there are before anyone has come back: teeth, nails, and the one nobody can make out. */
    public static final int FIRST_JARS=3;
    /** The house's lamps (front room and corridor), and its front windows. */
    public static final List<BlockPos> ROOM_LAMPS=List.of(new BlockPos(15,2,-84),new BlockPos(21,2,-84));
    public static final List<BlockPos> HALL_LAMPS=List.of(new BlockPos(17,2,-94),new BlockPos(19,2,-102),new BlockPos(17,2,-110));
    public static final List<BlockPos> WINDOWS;
    static{var out=new ArrayList<BlockPos>();for(int x:new int[]{13,14,22,23})for(int y:new int[]{1,2})out.add(new BlockPos(x,y,HOUSE_S));for(int x:new int[]{13,14,18,22,23})for(int y:new int[]{5,6})out.add(new BlockPos(x,y,HOUSE_S));WINDOWS=List.copyOf(out);}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ where things are
    /** On the main street or the cross street, outside the lot. */
    public static boolean inStreet(Vec3 r){return r.y>-.5&&r.y<4&&(Math.abs(r.x-.5)<PAVE+.5&&r.z<1.2&&r.z>CROSS_PAVE_N-.5||r.z<CROSS_PAVE_S+1&&r.z>CROSS_PAVE_N-.5&&r.x>WEST+.5&&r.x<EAST+.5);}
    /** Inside the lot's fence: the garden, or the house. */
    public static boolean inLot(Vec3 r){return r.x>LOT_W&&r.x<LOT_E+1&&r.z<NORTH_FRONT&&r.z>NORTH;}
    /** In the house: the front room or the corridor. */
    public static boolean inHouse(Vec3 r){return inRoom(r)||inHall(r);}
    public static boolean inRoom(Vec3 r){return r.x>ROOM_W-.2&&r.x<ROOM_E+1.2&&r.z<HOUSE_S-.3&&r.z>ROOM_N-1.2&&r.y>-.5&&r.y<4;}
    public static boolean inHall(Vec3 r){return r.x>HALL_W-.2&&r.x<HALL_E+1.2&&r.z<BACK_DOOR.getZ()+.2&&r.z>NICHE.getZ()-.2&&r.y>-.5&&r.y<3;}
    /** Past the house's back wall, where the house had already ended outside. */
    public static boolean deepInHall(Vec3 r){return inHall(r)&&r.z<HOUSE_N;}
    /** Within reach of the last door. */
    public static boolean atLastDoor(Vec3 r){return inHall(r)&&r.z<LAST_DOOR.getZ()+4;}
    /** Out of the house: in the street, or the garden. */
    public static boolean outside(Vec3 r){return inStreet(r)||inLot(r)&&!inHouse(r)&&r.z>HOUSE_S;}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    static BlockState mat(String name){return CornerRegistry.material(name).defaultBlockState();}
    static BlockState fit(CornerBlock.Kind kind,Direction facing,int stage){return CornerBlock.of(kind,facing,stage);}

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var rnd=new Random(94080);
        box(l,b,WEST,-4,NORTH,EAST,16,0,air);
        box(l,b,WEST,-4,NORTH,EAST,-2,0,Blocks.DIRT.defaultBlockState());
        box(l,b,WEST,-1,NORTH,EAST,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        mainStreet(l,b,rnd);
        crossStreet(l,b);
        rows(l,b);
        northRow(l,b);
        trees(l,b,rnd);
        kiosk(l,b);
        lot(l,b,rnd);
        house(l,b);
        block(l,b);
        corridor(l,b);
        set(l,b,NOTEBOOK,fit(CornerBlock.Kind.NOTEBOOK,Direction.NORTH,0));
        set(l,b,NOTE,fit(CornerBlock.Kind.NOTE,Direction.NORTH,0));
        set(l,b,6,0,-34,fit(CornerBlock.Kind.DOG,Direction.NORTH,0));
        gate(l,b,false);path(l,b,false);jars(l,b,FIRST_JARS);lamps(l,b,true);windows(l,b,false);
    }

    /** The road in old setts between kerbs, pavements in slabs, the House's door in the side wall of the first house. */
    static void mainStreet(ServerLevel l,BlockPos b,Random rnd){
        box(l,b,-ROAD,-1,ROW_END,ROAD,-1,0,mat("setts"));
        box(l,b,-KERB,-1,ROW_END,-KERB,-1,0,Blocks.POLISHED_ANDESITE.defaultBlockState());box(l,b,KERB,-1,ROW_END,KERB,-1,0,Blocks.POLISHED_ANDESITE.defaultBlockState());
        box(l,b,-PAVE,-1,ROW_END,-KERB-1,-1,0,mat("paving"));box(l,b,KERB+1,-1,ROW_END,PAVE,-1,0,mat("paving"));
        // The wall the House's door is in: a house's blank side, rendered, under a tiled coping.
        box(l,b,-PAVE,0,1,PAVE,5,1,mat("stucco"));box(l,b,-PAVE,6,1,PAVE,6,1,Blocks.BRICK_SLAB.defaultBlockState());
        // Street lamps on the kerbs, for whoever is here after dark.
        for(int z:new int[]{-14,-38})for(int x:new int[]{-KERB,KERB}){for(int y=0;y<=2;y++)set(l,b,x,y,z+(x<0?0:-8),Blocks.DARK_OAK_FENCE.defaultBlockState());set(l,b,x,3,z+(x<0?0:-8),Blocks.LANTERN.defaultBlockState());}
    }
    static void crossStreet(ServerLevel l,BlockPos b){
        box(l,b,WEST+1,-1,CROSS_ROAD_N,EAST-1,-1,CROSS_ROAD_S,mat("setts"));
        box(l,b,WEST+1,-1,CROSS_KERB_S,EAST-1,-1,CROSS_KERB_S,Blocks.POLISHED_ANDESITE.defaultBlockState());box(l,b,WEST+1,-1,CROSS_KERB_N,EAST-1,-1,CROSS_KERB_N,Blocks.POLISHED_ANDESITE.defaultBlockState());
        box(l,b,-ROAD,-1,CROSS_KERB_S,ROAD,-1,CROSS_KERB_S,mat("setts"));
        box(l,b,WEST+1,-1,CROSS_KERB_S+1,EAST-1,-1,CROSS_PAVE_S,mat("paving"));box(l,b,-ROAD,-1,CROSS_KERB_S+1,ROAD,-1,CROSS_PAVE_S,mat("setts"));
        box(l,b,WEST+1,-1,CROSS_PAVE_N,EAST-1,-1,CROSS_KERB_N+1,mat("paving"));
        // The street's two ends: a house front across each, so the cross street turns out of sight.
        for(int x:new int[]{WEST,EAST}){box(l,b,x,-1,CROSS_PAVE_N,x,8,CROSS_PAVE_S,mat("stucco"));box(l,b,x,9,CROSS_PAVE_N,x,9,CROSS_PAVE_S,Blocks.STONE_BRICK_SLAB.defaultBlockState());
            for(int z=CROSS_PAVE_S-1;z>=CROSS_PAVE_N+1;z-=4)for(int y:new int[]{2,6})set(l,b,x,y,z,fit(CornerBlock.Kind.WINDOW,x<0?Direction.EAST:Direction.WEST,0));}
        for(int x:new int[]{-14,10,30})for(int z:new int[]{CROSS_KERB_S,CROSS_KERB_N}){if(z==CROSS_KERB_N&&x==10)continue;for(int y=0;y<=2;y++)set(l,b,x,y,z,Blocks.DARK_OAK_FENCE.defaultBlockState());set(l,b,x,3,z,Blocks.LANTERN.defaultBlockState());}
    }
    /** One low house: rendered walls, a door kept shut, shuttered windows, a balcony over the door, a parapet on the flat roof. */
    static void house(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,Direction faces,BlockState wall){
        int lx=Math.min(x0,x1),hx=Math.max(x0,x1),lz=Math.min(z0,z1),hz=Math.max(z0,z1);
        box(l,b,lx,-1,lz,hx,8,hz,wall);
        boolean alongZ=faces.getAxis()==Direction.Axis.X;
        int front=switch(faces){case EAST->hx;case WEST->lx;case SOUTH->hz;default->lz;};
        // The parapet round the roof.
        for(int x=lx;x<=hx;x++)for(int z=lz;z<=hz;z++)if(x==lx||x==hx||z==lz||z==hz)set(l,b,x,9,z,Blocks.STONE_BRICK_WALL.defaultBlockState());
        int mid=alongZ?(lz+hz)/2:(lx+hx)/2;int span=alongZ?hz-lz:hx-lx;
        BlockPos door=alongZ?new BlockPos(front,0,mid):new BlockPos(mid,0,front);
        NovelRooms.door(l,b.offset(door),faces.getOpposite(),Blocks.DARK_OAK_DOOR,false);
        set(l,b,door.relative(faces).below(),Blocks.SMOOTH_STONE.defaultBlockState());
        for(int d=-span/2+1;d<=span/2-1;d++){if(Math.abs(d)<2)continue;if((d&1)!=0)continue;
            for(int y:new int[]{1,2,5,6}){var at=alongZ?door.offset(0,y,d):door.offset(d,y,0);set(l,b,at,fit(CornerBlock.Kind.WINDOW,faces,0));}}
        // The balcony over the door: a ledge three wide with an iron rail round it, and the balcony window behind it. 0.4.578: the
        // ledge was one slab, with the game's bars as three loose posts over it, two of them standing on nothing.
        var ledge=door.relative(faces).above(4);
        for(var along:new Direction[]{null,faces.getCounterClockWise(),faces.getClockWise()}){
            var at=along==null?ledge:ledge.relative(along);
            set(l,b,at,Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
            set(l,b,at.above(),fit(CornerBlock.Kind.RAILING,faces,along==null?0:along==faces.getCounterClockWise()?1:2));
        }
        set(l,b,ledge.above().relative(faces.getOpposite()),fit(CornerBlock.Kind.WINDOW,faces,0));set(l,b,ledge.above(2).relative(faces.getOpposite()),fit(CornerBlock.Kind.WINDOW,faces,0));
    }
    static BlockState tint(int i){return switch(i%3){case 0->mat("stucco");case 1->Blocks.WHITE_TERRACOTTA.defaultBlockState();default->Blocks.SMOOTH_SANDSTONE.defaultBlockState();};}
    /** The two rows down the main street, and the backs of the blocks along the cross street's south side. */
    static void rows(ServerLevel l,BlockPos b){
        int[][] spans={{0,-12},{-13,-23},{-24,-34},{-35,-45},{-46,ROW_END}};
        for(int i=0;i<spans.length;i++){
            house(l,b,-FRONT-11,-FRONT,spans[i][0],spans[i][1],Direction.EAST,tint(i));
            house(l,b,FRONT,FRONT+11,spans[i][0],spans[i][1],Direction.WEST,tint(i+1));
        }
        for(int[] s:new int[][]{{WEST,-FRONT-12},{FRONT+12,EAST}})for(int x=s[0];x<=s[1];x+=12)house(l,b,x,Math.min(s[1],x+11),ROW_END+11,ROW_END,Direction.NORTH,tint(x&7));
    }
    /** The row across the cross street; the door facing up the main street is left open for the way on. */
    static void northRow(ServerLevel l,BlockPos b){
        int[][] spans={{WEST+1,-21},{-20,-9},{-8,8}};
        for(int i=0;i<spans.length;i++)house(l,b,spans[i][0],spans[i][1],NORTH_FRONT,NORTH_FRONT-13,Direction.SOUTH,tint(i+2));
        set(l,b,ONWARD,Blocks.AIR.defaultBlockState());set(l,b,ONWARD.above(),Blocks.AIR.defaultBlockState());
    }
    /** Jacarandas on the pavements, in flower, their petals fallen round them and along the gutters. */
    static void trees(ServerLevel l,BlockPos b,Random rnd){
        var log=Blocks.DARK_OAK_LOG.defaultBlockState();var blossom=fit(CornerBlock.Kind.BLOSSOM,Direction.NORTH,0);var petals=fit(CornerBlock.Kind.PETALS,Direction.NORTH,0);
        int[][] at={{-6,-8},{6,-18},{-6,-30},{6,-44},{-6,-50},{26,-57},{-24,-67}};
        for(int[] t:at){
            for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)for(int y=4;y<=6;y++){if(Math.abs(dx)+Math.abs(dz)>(y==6?2:3))continue;int x=t[0]+dx,z=t[1]+dz;if(Math.abs(x)>=FRONT&&z>ROW_END||z<=NORTH_FRONT||z>=1)continue;if(l.getBlockState(b.offset(x,y,z)).isAir())set(l,b,x,y,z,blossom);}
            for(int y=0;y<=4;y++)set(l,b,t[0],y,t[1],log);
            for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){if(dx==0&&dz==0||rnd.nextInt(3)==0)continue;int x=t[0]+dx,z=t[1]+dz;if(l.getBlockState(b.offset(x,0,z)).isAir()&&!l.getBlockState(b.offset(x,-1,z)).isAir())set(l,b,x,0,z,petals);}
        }
        for(int z=-3;z>ROW_END;z-=3)for(int x:new int[]{-ROAD,ROAD})if(rnd.nextInt(2)==0&&l.getBlockState(b.offset(x,0,z)).isAir())set(l,b,x,0,z,petals);
    }
    /** The kiosk on the west pavement: a green box with its shutter half down and the papers in their rack. */
    static void kiosk(ServerLevel l,BlockPos b){
        box(l,b,-PAVE,0,-23,-PAVE+1,2,-21,Blocks.GREEN_TERRACOTTA.defaultBlockState());
        box(l,b,-PAVE,3,-24,-PAVE+2,3,-20,Blocks.DARK_OAK_SLAB.defaultBlockState());
        set(l,b,-PAVE+2,0,-22,fit(CornerBlock.Kind.KIOSK,Direction.EAST,0));
    }
    /** The corner lot: the tall fence and its gate, the garden nobody keeps, the path to the door. */
    static void lot(ServerLevel l,BlockPos b,Random rnd){
        box(l,b,LOT_W,-1,GARDEN_N,LOT_E,-1,GARDEN_S,Blocks.GRASS_BLOCK.defaultBlockState());
        box(l,b,LOT_W,-1,HOUSE_N,LOT_E,-1,HOUSE_S,Blocks.GRASS_BLOCK.defaultBlockState());
        // 0.4.578 (the owner: loose bars, no top rail, the bars one iron and the gate another): the fence is the lot's own, in the
        // gate's iron, three cells high: a rail on a low plinth, a pair of rails at the gate's head and points above them. A post
        // stands each side of the gate, at the wall the fence starts from, at every third cell between and at the corner; over the
        // gate the fence's top cell goes on from post to post.
        int gw=GATE_W.getX(),ge=GATE_E.getX();
        for(int x=LOT_W;x<=LOT_E;x++){
            set(l,b,x,-1,NORTH_FRONT,Blocks.STONE_BRICKS.defaultBlockState());
            if(x==gw||x==ge){set(l,b,x,2,NORTH_FRONT,fit(CornerBlock.Kind.FENCE,Direction.NORTH,2));continue;}
            boolean jamb=x==gw-1,post=jamb||(x<gw?(x-LOT_W)%FENCE_POSTS==0:(x-ge-1)%FENCE_POSTS==0);
            for(int y=0;y<=2;y++)set(l,b,x,y,NORTH_FRONT,fit(post?CornerBlock.Kind.FENCE_POST:CornerBlock.Kind.FENCE,jamb?Direction.SOUTH:Direction.NORTH,y));
        }
        set(l,b,LOT_E+1,-1,NORTH_FRONT,Blocks.STONE_BRICKS.defaultBlockState());
        for(int y=0;y<=2;y++)set(l,b,LOT_E+1,y,NORTH_FRONT,fit(CornerBlock.Kind.FENCE_CORNER,Direction.NORTH,y));
        // The lot's east side, from the corner to the block behind the house; and its west side, past the end of the row.
        side(l,b,LOT_E+1,NORTH_FRONT-1,FENCE_POSTS-1);side(l,b,LOT_W-1,NORTH_FRONT-14,0);
        // The garden: grass gone to seed, a rose run wild, ferns, a lemon tree that has died standing.
        Block[] weeds={Blocks.TALL_GRASS,Blocks.SHORT_GRASS,Blocks.FERN,Blocks.SHORT_GRASS,Blocks.ROSE_BUSH,Blocks.LARGE_FERN,Blocks.SHORT_GRASS,Blocks.LILAC};
        for(int x=LOT_W;x<=LOT_E;x++)for(int z=GARDEN_S;z>=HOUSE_N;z--){
            if(x>=HOUSE_W&&x<=HOUSE_E&&z<=HOUSE_S)continue;if(x>=GATE_W.getX()&&x<=GATE_E.getX()&&z>HOUSE_S)continue;if(rnd.nextInt(3)!=0)continue;
            var w=weeds[rnd.nextInt(weeds.length)];
            if(w==Blocks.TALL_GRASS||w==Blocks.ROSE_BUSH||w==Blocks.LARGE_FERN||w==Blocks.LILAC){set(l,b,x,0,z,w.defaultBlockState().setValue(DoublePlantBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,x,1,z,w.defaultBlockState().setValue(DoublePlantBlock.HALF,DoubleBlockHalf.UPPER));}
            else set(l,b,x,0,z,w.defaultBlockState());
        }
        for(int y=0;y<=3;y++)set(l,b,34,y,-73,Blocks.STRIPPED_OAK_LOG.defaultBlockState());set(l,b,33,3,-73,Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.EAST,true));set(l,b,35,3,-73,Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WEST,true));
    }
    /** One side of the lot's fence, from z south back to the block behind the house; a post at the south end of every third cell,
     * the first of them the cell that many cells in (0 where the run starts at a wall, 2 where it starts at the corner's post). */
    private static void side(ServerLevel l,BlockPos b,int x,int south,int first){
        for(int z=south;z>=HOUSE_N;z--)for(int y=0;y<=2;y++)set(l,b,x,y,z,fit((south-z)%FENCE_POSTS==first?CornerBlock.Kind.FENCE_POST:CornerBlock.Kind.FENCE,Direction.WEST,y));
    }
    /** The house: rendered outside, papered inside; the front room clean and lit; everything behind it shut but the corridor. */
    static void house(ServerLevel l,BlockPos b){
        var stucco=mat("stucco");var paper=mat("wallpaper");var tiles=mat("tiles");var air=Blocks.AIR.defaultBlockState();
        box(l,b,HOUSE_W,-1,HOUSE_N,HOUSE_E,8,HOUSE_S,stucco);
        for(int x=HOUSE_W;x<=HOUSE_E;x++)for(int z=HOUSE_N;z<=HOUSE_S;z++)if(x==HOUSE_W||x==HOUSE_E||z==HOUSE_N||z==HOUSE_S)set(l,b,x,9,z,Blocks.STONE_BRICK_WALL.defaultBlockState());
        // The front room: papered, tiled, ceilinged, with the lining one block in from the render.
        box(l,b,ROOM_W-1,0,ROOM_N-1,ROOM_E+1,3,ROOM_S+1,paper);
        box(l,b,ROOM_W,-1,ROOM_N,ROOM_E,-1,ROOM_S,tiles);
        box(l,b,ROOM_W,0,ROOM_N,ROOM_E,3,ROOM_S,air);
        box(l,b,ROOM_W-1,4,ROOM_N-1,ROOM_E+1,4,ROOM_S+1,Blocks.SMOOTH_QUARTZ.defaultBlockState());
        // The front door through both walls; the windows' recesses inside, behind their shutters.
        set(l,b,FRONT_DOOR.north(),air);set(l,b,FRONT_DOOR.north().above(),air);set(l,b,FRONT_DOOR.north().below(),tiles);
        frontDoor(l,b,false);
        set(l,b,FRONT_DOOR.south().below(),Blocks.SMOOTH_STONE.defaultBlockState());
        for(var w:WINDOWS)if(w.getY()<4)set(l,b,w.north(),air);
        // A front room someone keeps: a rug, two armchairs, a low table, a sideboard.
        box(l,b,16,0,-83,20,0,-80,Blocks.LIGHT_GRAY_CARPET.defaultBlockState());
        set(l,b,ROOM_E,0,-81,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));set(l,b,ROOM_E,0,-82,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
        set(l,b,21,0,-81,Blocks.DARK_OAK_FENCE.defaultBlockState());set(l,b,21,1,-81,Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());
        set(l,b,ROOM_E,0,-79,Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP).setValue(TrapDoorBlock.FACING,Direction.WEST));
        for(var at:ROOM_LAMPS)set(l,b,at,fit(CornerBlock.Kind.LAMP,Direction.SOUTH,1));
        // The back door, through both walls, into the corridor.
        set(l,b,BACK_DOOR.north(),air);set(l,b,BACK_DOOR.north().above(),air);
        backDoor(l,b,false);
    }
    /** The corridor: three wide, papered, tiled, low-ceilinged, lit at long intervals, on and on to the last door. */
    static void corridor(ServerLevel l,BlockPos b){
        var paper=mat("wallpaper");var tiles=mat("tiles");var air=Blocks.AIR.defaultBlockState();
        box(l,b,HALL_W-1,-1,NICHE.getZ()-1,HALL_E+1,3,BACK_DOOR.getZ()-1,paper);
        box(l,b,HALL_W,-1,HALL_N,HALL_E,-1,BACK_DOOR.getZ()-1,tiles);
        box(l,b,HALL_W,0,HALL_N,HALL_E,2,BACK_DOOR.getZ()-1,air);
        box(l,b,HALL_W-1,3,NICHE.getZ()-1,HALL_E+1,3,BACK_DOOR.getZ()-1,Blocks.SMOOTH_QUARTZ.defaultBlockState());
        for(var at:HALL_LAMPS)set(l,b,at,fit(CornerBlock.Kind.LAMP,at.getX()<18?Direction.EAST:Direction.WEST,1));
        // The last door, and behind it a room smaller than the door.
        set(l,b,NICHE.below(),tiles);set(l,b,NICHE,air);set(l,b,NICHE.above(),Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        lastDoor(l,b,false);
    }
    /** The blank apartment block behind the house, over the corridor: render and shutters, nothing to get into. */
    static void block(ServerLevel l,BlockPos b){
        var wall=Blocks.WHITE_TERRACOTTA.defaultBlockState();
        for(int x=LOT_W-1;x<=EAST;x++)for(int y=0;y<=15;y++){set(l,b,x,y,HOUSE_N-1,wall);set(l,b,x,y,NORTH,wall);}
        for(int z=NORTH;z<=HOUSE_N-1;z++)for(int y=0;y<=15;y++){set(l,b,LOT_W-1,y,z,wall);set(l,b,EAST,y,z,wall);}
        box(l,b,LOT_W-1,15,NORTH,EAST,15,HOUSE_N-1,Blocks.SMOOTH_STONE.defaultBlockState());
        for(int x=LOT_W+3;x<=EAST-3;x+=4)for(int y:new int[]{11,12})set(l,b,x,y,HOUSE_N-1,fit(CornerBlock.Kind.WINDOW,Direction.SOUTH,0));
    }

    // ------------------------------------------------------------------------------------------------ what changes
    /** The gate's two leaves, each hung on its own post: the west one faces north and the east one south, so that open, each stands
     * back against its post. (0.4.578: both faced north, and open, the east leaf stood in the middle of the gateway.) */
    public static void gate(ServerLevel l,BlockPos b,boolean open){
        for(var at:List.of(GATE_W,GATE_E)){var f=at==GATE_W?Direction.NORTH:Direction.SOUTH;
            set(l,b,at,fit(CornerBlock.Kind.GATE,f,open?1:0));set(l,b,at.above(),fit(CornerBlock.Kind.GATE,f,open?3:2));}
    }
    /** The garden path, or the garden grown over it. */
    public static void path(ServerLevel l,BlockPos b,boolean grown){
        for(int x=GATE_W.getX();x<=GATE_E.getX();x++)for(int z=GARDEN_S;z>HOUSE_S;z--){
            set(l,b,x,-1,z,grown?Blocks.GRASS_BLOCK.defaultBlockState():mat("tiles"));
            set(l,b,x,0,z,grown?((x+z)%3==0?Blocks.FERN:Blocks.SHORT_GRASS).defaultBlockState():Blocks.AIR.defaultBlockState());
        }
    }
    /** As many jars on the boards as there are, the first three teeth, nails and the pale one, then the same again. */
    public static void jars(ServerLevel l,BlockPos b,int count){for(int i=0;i<JARS.size();i++)set(l,b,JARS.get(i),fit(CornerBlock.Kind.JAR,Direction.EAST,i<count?1+i%3:0));}
    public static void lamps(ServerLevel l,BlockPos b,boolean lit){for(var at:ROOM_LAMPS)set(l,b,at,fit(CornerBlock.Kind.LAMP,Direction.SOUTH,lit?1:0));for(var at:HALL_LAMPS)set(l,b,at,fit(CornerBlock.Kind.LAMP,at.getX()<18?Direction.EAST:Direction.WEST,lit?1:0));}
    public static void windows(ServerLevel l,BlockPos b,boolean lit){for(var at:WINDOWS)set(l,b,at,fit(CornerBlock.Kind.WINDOW,Direction.SOUTH,lit?1:0));}
    public static void frontDoor(ServerLevel l,BlockPos b,boolean open){NovelRooms.door(l,b.offset(FRONT_DOOR),Direction.NORTH,Blocks.DARK_OAK_DOOR,open);}
    public static void backDoor(ServerLevel l,BlockPos b,boolean open){NovelRooms.door(l,b.offset(BACK_DOOR),Direction.NORTH,Blocks.DARK_OAK_DOOR,open);}
    public static void lastDoor(ServerLevel l,BlockPos b,boolean open){NovelRooms.door(l,b.offset(LAST_DOOR),Direction.NORTH,Blocks.DARK_OAK_DOOR,open);}
    public static boolean doorOpen(ServerLevel l,BlockPos b,BlockPos door){var s=l.getBlockState(b.offset(door));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
}
