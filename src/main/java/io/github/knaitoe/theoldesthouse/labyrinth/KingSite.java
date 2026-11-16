package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.641, A sort of thing, part 1 (docs/SLEEPING_KING_PLAN.md, docs/SLEEPING_KING_0_4_641.md): the place, built whole. Every position
 * is relative to the base; north is into the scene.
 * <p>
 * The House's door opens into the drawing-room (x -6 to 6, z -1 to -12, floor 0, ceiling 5): the chimney-piece against the north wall
 * with the fire in its grate and the looking-glass over it, three wide and two high; the hearthrug before it, the chessmen fallen in
 * the hearth, the little table with the chess problem on it printed backwards, the armchair, a tall window in each side wall. The
 * room's north wall is the glass (z -13.5): behind it the same room the other way round (z -15 to -26), its things reflected across
 * that plane, and in its far wall, where the House's door is in the first, a doorway onto the board.
 * <p>
 * The board is a strip of three files and eight ranks of sixteen-block squares, light and dark turf by parity, ruled from the house
 * northward: rank r begins at z {@link #rankNear(int)}, and two rows of brook lie between each rank and the next. Hedges divide the
 * files (x -9 and 8) with a gap in each rank; a gravel road runs up the middle file over footbridges. Around the strip the country is
 * ruled to x ±64. The fourth rank is the wood, and the King's tree stands in its east file.
 * <p>
 * The land is drawn as the proposal's deep sleep has it: the third to fifth ranks filled, the first, seventh and eighth ruled, and the
 * second and sixth the band between, a block at a time.
 */
public final class KingSite {
    private KingSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SLEEPING_KING;
    public static final int CEIL=5,RANKS=8,SQUARE=16,PITCH=18,NEAR=-30,WEST=-25,EAST=24,COUNTRY=64;
    /** The glass's plane, between the drawing-room and the room behind it. */
    public static final double MIRROR_Z=-13.5;
    // ------------------------------------------------------------------------------------------------ the drawing-room
    public static final BlockPos FIRE=new BlockPos(0,0,-12),RUG=new BlockPos(0,0,-9),CHESSMEN=new BlockPos(1,0,-11),TABLE=new BlockPos(-4,0,-7),
        PROBLEM=new BlockPos(-4,1,-7),ARMCHAIR=new BlockPos(-4,0,-9);
    /** Where the reader's way on is: a door in the drawing-room's east wall, between the window and the fire. */
    public static final BlockPos ONWARD=new BlockPos(7,0,-9);
    /** The chess problem is the first paper. */
    public static final BlockPos SOURCE=PROBLEM;
    // ------------------------------------------------------------------------------------------------ the board
    /** The doorway out of the room behind the glass. */
    public static final BlockPos GARDEN_DOOR=new BlockPos(0,0,-27);
    /** The King's tree, on the fourth rank's east file, and where he lies under it. */
    public static final BlockPos TREE=new BlockPos(16,0,-91),KING=new BlockPos(16,0,-86);
    /** Where the account will wait (part 3 and after): the foot of his tree. */
    public static final BlockPos ENDING=new BlockPos(13,0,-88);

    /** The z the rank (1 to 8) begins at, nearest the house, and the last z it covers. */
    public static int rankNear(int r){return NEAR-(r-1)*PITCH;}
    public static int rankFar(int r){return rankNear(r)-SQUARE+1;}
    /** The rank a z lies in, 0 if in a brook or off the board. */
    public static int rank(int z){for(int r=1;r<=RANKS;r++)if(z<=rankNear(r)&&z>=rankFar(r))return r;return 0;}
    /** The file an x lies in (1 west, 2 middle, 3 east), 0 for a hedge or off the strip. */
    public static int file(int x){return x>=WEST&&x<=-10?1:x>=-8&&x<=7?2:x>=9&&x<=EAST?3:0;}
    /** Whether the square at (rank, file) is the light one. */
    public static boolean light(int rank,int file){return (rank+file)%2==0;}

    /** The proposal's deep sleep: how ruled the land is at z, from 0 (filled) to 1 (ruled). */
    public static double ruledness(int z){
        double third=rankNear(3)+.5,sixth=rankNear(6),seventh=rankNear(7)+.5,second=rankNear(2);
        if(z>third)return z>second?1:Mth.clamp((z-third)/(second-third+.5),0,1);
        if(z<sixth+.5){if(z<seventh)return 1;return Mth.clamp((sixth+.5-z)/(sixth+.5-seventh),0,1);}
        return 0;
    }
    /** Whether the block at (x,z) is drawn ruled: certain outside the band, by a fixed hash within it. */
    public static boolean ruled(int x,int z){return ruled(x,z,0);}
    /**
     * 0.4.642: how ruled the land at (x,z) is with the fill's edge moved {@code shift} blocks out (his in-breath moves it one, KingBreath):
     * toward the house on the near side of the wood and toward the far ranks beyond it, so the filled land grows both ways.
     */
    public static double ruledness(int x,int z,int shift){
        double r=shift==0?ruledness(z):z>rankFar(4)?ruledness(z-shift):ruledness(z+shift);
        if(Math.abs(x)>EAST+1)r=Math.max(r,Mth.clamp((Math.abs(x)-EAST-1)/8.0,0,1));
        return r;
    }
    public static boolean ruled(int x,int z,int shift){
        double r=ruledness(x,z,shift);
        // Within the band the land goes over in clumps of four blocks, not one block at a time.
        if(r<=0)return false;if(r>=1)return true;return hash(x>>1,z>>1)<r;
    }
    private static long[] breathing;
    /** Every column (x, z, as BlockPos longs at y 0) whose drawing changes between the out-breath and the in-breath. */
    public static synchronized long[] breathing(){
        if(breathing==null){
            var cells=new java.util.ArrayList<Long>();
            for(int x=-COUNTRY;x<=COUNTRY;x++)for(int z=rankFar(RANKS)-8;z<=0;z++)if(ruled(x,z,0)!=ruled(x,z,1))cells.add(BlockPos.asLong(x,0,z));
            breathing=cells.stream().mapToLong(Long::longValue).toArray();
        }
        return breathing;
    }
    static double hash(int x,int z){long h=x*341873128712L+z*132897987541L;h^=h>>>29;h*=0xBF58476D1CE4E5B9L;h^=h>>>32;return (h&0xFFFFFF)/(double)0x1000000;}

    public static boolean inside(Vec3 r){return r.x>=-COUNTRY&&r.x<COUNTRY+1&&r.z>=rankFar(RANKS)-8&&r.z<1&&r.y>=-4&&r.y<32;}
    public static boolean inRoom(Vec3 r){return r.x>=-6&&r.x<7&&r.z>=-12.9&&r.z<0&&r.y>=-.5&&r.y<CEIL;}
    public static boolean behindGlass(Vec3 r){return r.x>=-6&&r.x<7&&r.z>=-26.9&&r.z<-13.6&&r.y>=-.5&&r.y<CEIL;}
    public static boolean onBoard(Vec3 r){return r.z<-28&&r.z>=rankFar(RANKS)-2&&r.x>=-COUNTRY&&r.x<COUNTRY+1;}

    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static BlockState mat(String name){return KingRegistry.material(name).defaultBlockState();}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);
    }
    static BlockState fit(KingBlock.Kind kind,Direction facing){return KingBlock.of(kind,facing);}
    static BlockState fit(KingBlock.Kind kind,Direction facing,int stage){return KingBlock.of(kind,facing,stage);}
    /** The mirror image, across the glass, of a cell of the drawing-room. */
    public static BlockPos reflect(BlockPos p){return new BlockPos(p.getX(),p.getY(),-27-p.getZ());}
    static Direction reflect(Direction d){return d.getAxis()==Direction.Axis.Z?d.getOpposite():d;}
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();

    public static void build(ServerLevel l,BlockPos b){
        country(l,b);
        house(l,b);
        room(l,b,false);
        room(l,b,true);
        board(l,b);
        wood(l,b);
    }

    // ------------------------------------------------------------------------------------------------ the country round the strip
    static void country(ServerLevel l,BlockPos b){
        int far=rankFar(RANKS)-8;
        for(int x=-COUNTRY;x<=COUNTRY;x++)for(int z=far;z<=0;z++){
            boolean r=ruled(x,z);
            set(l,b,x,-3,z,KingRegistry.country("soil",r));set(l,b,x,-2,z,KingRegistry.country("soil",r));
            set(l,b,x,-1,z,KingRegistry.country(((x>>3)+(z>>3)&1)==0?"turf_light":"turf_dark",r));
            for(int y=0;y<=8;y++)set(l,b,x,y,z,AIR);
        }
        // The land beyond the eighth rank is a low ruled hedge, as far as the country goes: the end of the board.
        for(int x=-COUNTRY;x<=COUNTRY;x++)for(int y=0;y<=1;y++)set(l,b,x,y,far,KingRegistry.country("hedge",true));
        for(int z=far;z<=0;z++)for(int y=0;y<=1;y++){set(l,b,-COUNTRY,y,z,KingRegistry.country("hedge",true));set(l,b,COUNTRY,y,z,KingRegistry.country("hedge",true));}
    }

    // ------------------------------------------------------------------------------------------------ the house: two rooms back to back
    static void house(ServerLevel l,BlockPos b){
        var brick=mat("chimney");
        // The outer skin, chimney brick, x -8 to 8 and back to z -28; the inner walls the rooms' own (the drawing-room's south wall at z 0
        // is the vestibule's back, the other room's north wall at -27 its mirror).
        box(l,b,-8,-1,-28,8,CEIL+1,0,brick);
        box(l,b,-7,0,-27,7,CEIL-1,0,mat("panel"));
        box(l,b,-6,0,-26,6,CEIL-1,-1,AIR);
        // The back of the chimney-pieces: one thick wall of brick between the rooms.
        box(l,b,-7,0,-14,7,CEIL-1,-13,brick);
        // Floors and ceilings.
        box(l,b,-6,-1,-26,6,-1,-1,mat("floorboards"));box(l,b,-6,CEIL,-26,6,CEIL,-1,mat("ceiling"));
        box(l,b,-6,-1,-14,6,-1,-13,brick);
        // A low roof of brick, stepped, so the house is a thing in the country and not a box.
        for(int y=CEIL+2,i=0;i<3;y++,i++)box(l,b,-8+i*2,y,-27,8-i*2,y,0,brick);
        // The House's door at z 0 in the drawing-room's south wall; the doorway out of the other room at z -27, through the brick.
        set(l,b,0,0,0,AIR);set(l,b,0,1,0,AIR);
        set(l,b,GARDEN_DOOR,AIR);set(l,b,GARDEN_DOOR.above(),AIR);set(l,b,GARDEN_DOOR.north(),AIR);set(l,b,GARDEN_DOOR.north().above(),AIR);
        set(l,b,GARDEN_DOOR.below(),mat("floorboards"));set(l,b,GARDEN_DOOR.north().below(),KingRegistry.country("path",true));
        // The windows go through the brick: each is open to the country behind it.
        for(int x=-8;x<=8;x+=16)for(int z:new int[]{-4,-6,-21,-23})for(int y=1;y<=2;y++)set(l,b,x,y,z,AIR);
        // A step of path from the doorway to the board's first rank.
        for(int z=-29;z>=NEAR;z--){set(l,b,-1,-1,z,KingRegistry.country("path",ruled(-1,z)));set(l,b,0,-1,z,KingRegistry.country("path",ruled(0,z)));}
    }

    /** One of the two rooms; the second is the first reflected across the glass. */
    static void room(ServerLevel l,BlockPos b,boolean behind){
        java.util.function.UnaryOperator<BlockPos> at=p->behind?reflect(p):p;
        java.util.function.UnaryOperator<Direction> to=d->behind?reflect(d):d;
        // Walls: panel to the dado, paper above it.
        for(int y=1;y<CEIL;y++){
            for(int x=-7;x<=7;x+=14)for(int z=-1;z>=-12;z--)set(l,b,at.apply(new BlockPos(x,y,z)),mat("wallpaper"));
            for(int x=-6;x<=6;x++)set(l,b,at.apply(new BlockPos(x,y,0)),mat("wallpaper"));
        }
        for(int x=-6;x<=6;x++)set(l,b,at.apply(new BlockPos(x,0,0)),mat("panel"));
        // The chimney wall either side of the breast is the room's own: panel to the dado, paper above.
        for(int x=-6;x<=6;x++)if(Math.abs(x)>2)for(int y=0;y<CEIL;y++)set(l,b,at.apply(new BlockPos(x,y,-13)),mat(y==0?"panel":"wallpaper"));
        if(!behind){set(l,b,0,0,0,AIR);set(l,b,0,1,0,AIR);}
        else{set(l,b,GARDEN_DOOR,AIR);set(l,b,GARDEN_DOOR.above(),AIR);}
        // The chimney-piece: two jambs and the fire, the hearth before it, the glass above, three across and two high.
        var into=to.apply(Direction.SOUTH);
        for(int i=-1;i<=1;i++){
            set(l,b,at.apply(new BlockPos(i,0,-12)),fit(KingBlock.Kind.MANTEL,into,i+1));
            set(l,b,at.apply(new BlockPos(i,-1,-11)),mat("hearth"));set(l,b,at.apply(new BlockPos(i,-1,-12)),mat("hearth"));
            set(l,b,at.apply(new BlockPos(i,1,-12)),fit(KingBlock.Kind.GLASS,into,i+1));set(l,b,at.apply(new BlockPos(i,2,-12)),fit(KingBlock.Kind.GLASS,into,i+4));
        }
        // The chimney breast either side of the glass and above it, in brick.
        for(int y=1;y<CEIL;y++){set(l,b,at.apply(new BlockPos(-2,y,-12)),mat("chimney"));set(l,b,at.apply(new BlockPos(2,y,-12)),mat("chimney"));}
        for(int x=-1;x<=1;x++)for(int y=3;y<CEIL;y++)set(l,b,at.apply(new BlockPos(x,y,-12)),mat("chimney"));
        set(l,b,at.apply(new BlockPos(-2,0,-12)),mat("chimney"));set(l,b,at.apply(new BlockPos(2,0,-12)),mat("chimney"));
        // The hearthrug, the chessmen in the hearth, the table and its paper, the armchair turned to the fire.
        for(int x=-1;x<=1;x++)for(int z=-9;z>=-10;z--)set(l,b,at.apply(new BlockPos(x,0,z)),fit(KingBlock.Kind.RUG,into));
        set(l,b,at.apply(CHESSMEN),fit(KingBlock.Kind.CHESSMEN,into));
        set(l,b,at.apply(TABLE),fit(KingBlock.Kind.TABLE,into));
        set(l,b,at.apply(PROBLEM),fit(KingBlock.Kind.PROBLEM,into,behind?1:0));
        set(l,b,at.apply(ARMCHAIR),fit(KingBlock.Kind.ARMCHAIR,to.apply(Direction.NORTH)));
        // A tall window in each side wall, looking in.
        for(int x=-7;x<=7;x+=14){var in=x<0?Direction.EAST:Direction.WEST;
            for(int z=-4;z>=-6;z-=2){set(l,b,at.apply(new BlockPos(x,1,z)),fit(KingBlock.Kind.WINDOW,in,0));set(l,b,at.apply(new BlockPos(x,2,z)),fit(KingBlock.Kind.WINDOW,in,1));}}
    }

    // ------------------------------------------------------------------------------------------------ the board
    static void board(ServerLevel l,BlockPos b){
        for(int r=1;r<=RANKS;r++){
            int near=rankNear(r),far=rankFar(r);
            for(int x=WEST;x<=EAST;x++)for(int z=far;z<=near;z++){
                int f=file(x);boolean ruled=ruled(x,z);
                if(f==0){
                    // The hedge between the files, with a gap at the middle of each rank.
                    boolean gap=z==near-7||z==near-8;
                    set(l,b,x,-1,z,KingRegistry.country("turf_dark",ruled));
                    if(!gap){set(l,b,x,0,z,KingRegistry.country("hedge",ruled));set(l,b,x,1,z,KingRegistry.country("hedge",ruled));}
                }else set(l,b,x,-1,z,KingRegistry.country(light(r,f)?"turf_light":"turf_dark",ruled));
                if(f==2&&(x==-1||x==0))set(l,b,x,-1,z,KingRegistry.country("path",ruled));
            }
            // The brook beyond this rank, two rows sunk one block; a footbridge carries the road over.
            if(r<RANKS)for(int x=WEST-1;x<=EAST+1;x++)for(int z=far-2;z<=far-1;z++){
                boolean ruled=ruled(x,z);
                set(l,b,x,-2,z,KingRegistry.country("brook",ruled));set(l,b,x,-1,z,AIR);
                if(x==-1||x==0)set(l,b,x,-1,z,KingRegistry.country("bridge",ruled));
                // 0.4.642: the verge over the brook, never seen: a wall to a thing, wading to a sort of thing, nothing to a line.
                else set(l,b,x,0,z,KingRegistry.VERGE.get().defaultBlockState());
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ the wood on the fourth rank
    static void wood(ServerLevel l,BlockPos b){
        int near=rankNear(4),far=rankFar(4);
        // Small trees over the whole rank, off the road and the hedge gaps, at fixed places.
        for(int x=WEST+2;x<=EAST-2;x+=5)for(int z=near-2;z>=far+2;z-=5){
            int dx=(int)(hash(x,z)*3)-1,dz=(int)(hash(z,x)*3)-1,tx=x+dx,tz=z+dz;
            if(file(tx)==0||Math.abs(tx)<=3||Math.abs(tx-TREE.getX())<=5&&Math.abs(tz-TREE.getZ())<=6)continue;
            tree(l,b,tx,tz,4+(int)(hash(tx,tz)*2),2);
        }
        tree(l,b,TREE.getX(),TREE.getZ(),7,4);
        // The trunk is two thick at his tree.
        for(int y=0;y<5;y++){set(l,b,TREE.getX()+1,y,TREE.getZ(),KingRegistry.country("bark",false));}
    }
    static void tree(ServerLevel l,BlockPos b,int x,int z,int height,int spread){
        boolean ruled=ruled(x,z);var bark=KingRegistry.country("bark",ruled);var bough=KingRegistry.country("bough",ruled);
        for(int y=0;y<height;y++)set(l,b,x,y,z,bark);
        int top=height-1;
        for(int dx=-spread;dx<=spread;dx++)for(int dy=-1;dy<=spread;dy++)for(int dz=-spread;dz<=spread;dz++){
            double d=dx*dx+dz*dz+dy*dy*1.6;if(d>spread*spread+1)continue;if(dx==0&&dz==0&&dy<1)continue;
            if(hash(x+dx*7,z+dz*13+dy)<.08)continue;
            set(l,b,x+dx,top+dy,z+dz,bough);
        }
    }
}
