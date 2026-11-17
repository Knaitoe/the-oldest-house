package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.663, Not Yet (docs/NOT_YET_0_4_663.md): a ward deeper in the same machine as the Kept, built whole in its interior slot. North
 * from the House's door: a short passage in the machine's casing, its floor over a shaft; the ward, two rows of cots under lamps on
 * jointed arms, with curtains on rails between them, a screen at every cot, a long window down the east wall painted with the machine's
 * copy of outdoors, and the counter over the arch at its far end; the mending room off its west side, a table under a ring of arms; the
 * long room past the arch, where the ones it has mended too often lie; and the far door at the end of it, shut.
 *
 * <p>All positions are relative to the place's base, the floor under the entry door; north is negative z.
 */
public final class NotYetSite {
    private NotYetSite(){}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The shell: everything inside it is the machine. */
    public static final int WEST=-20,EAST=12,NORTH=-67,TOP=8,BOTTOM=-12;
    /** The passage from the House's door: its half-width, its far end, its height. */
    public static final int PASS_HALF=2,PASS_N=-8,PASS_HIGH=3;
    /** The plates over the shaft, three by three in the passage floor; the shaft under them, and the floor at its foot. */
    public static final int HATCH_W=-1,HATCH_E=1,HATCH_S=-4,HATCH_N=-6,SHAFT_FLOOR=-12;
    /** The ward between its walls: its half-width, its near and far ends, its height. */
    public static final int WARD_HALF=9,WARD_S=-10,WARD_N=-44,WARD_HIGH=6;
    /** The cots down either side, by the z of each head; the west side leaves room for the doorway to the mending room. */
    public static final int[] EAST_COTS={-12,-16,-20,-24,-28,-32,-36,-40},WEST_COTS={-12,-16,-20,-24,-32,-36,-40};
    /** The mending room off the ward's west side, and the doorway into it. */
    public static final int MEND_W=-19,MEND_E=-12,MEND_S=-23,MEND_N=-33,MEND_HIGH=2,MEND_DOOR_S=-27,MEND_DOOR_N=-29;
    /** The mending table, its head and its foot, and the ring of arms over it. */
    public static final BlockPos TABLE=new BlockPos(-15,0,-27),TABLE_FOOT=new BlockPos(-15,0,-28);
    public static final BlockPos[] ARMS={new BlockPos(-16,2,-27),new BlockPos(-14,2,-27),new BlockPos(-16,2,-28),new BlockPos(-14,2,-28),new BlockPos(-15,2,-26),new BlockPos(-15,2,-29)};
    /** The long room past the arch: its near and far ends and its height; its cots, the three that are taken, by side and z. */
    public static final int LONG_S=-46,LONG_N=-62,LONG_HIGH=5;
    public static final int[] LONG_COTS={-49,-54,-59};
    /** The ones it has mended too often: west at -49, east at -54, west at -59. */
    public static final BlockPos[] PEOPLE_COTS={new BlockPos(-9,0,-49),new BlockPos(9,0,-54),new BlockPos(-9,0,-59)};
    /** The far door (lower half) in the long room's end wall; the small room past it, and the way on in that room's north wall. */
    public static final BlockPos FAR_DOOR=new BlockPos(0,0,-63),ONWARD=new BlockPos(0,0,-66);
    /** The counter over the arch: seven windows, the first (most significant) at the west end. */
    public static final BlockPos COUNTER_FIRST=new BlockPos(-3,4,-44);
    public static final int COUNTER_DIGITS=7;
    /** The ward's chart, on the wall by the door (the source). */
    public static final BlockPos CHART=new BlockPos(3,1,-10);
    /** Where the account will be: the drawer under bed one, the first of the ward's cots, at its foot (part 4). */
    public static final BlockPos ENDING=new BlockPos(8,0,-12);
    /** The long window down the east wall: rows and ends; the sun's four quarters and the three copies of the one cloud. */
    public static final int WINDOW_X=10,WINDOW_LOW=3,WINDOW_HIGH=5,WINDOW_S=-14,WINDOW_N=-38;

    // ------------------------------------------------------------------------------------------------ the cots
    /** Every cot in the ward and the long room, as its head cell; the order the machine gives them out in, nearest the door first. */
    public static final List<BlockPos> COTS;
    /** The cots a reader can be given: the ward's, nearest the door first, east and west in turn. */
    public static final List<BlockPos> WARD_COTS;
    static{
        var ward=new ArrayList<BlockPos>();
        for(int i=0;i<EAST_COTS.length;i++){ward.add(new BlockPos(9,0,EAST_COTS[i]));if(i<WEST_COTS.length)ward.add(new BlockPos(-9,0,WEST_COTS[i]));}
        WARD_COTS=List.copyOf(ward);
        var all=new ArrayList<>(ward);for(int z:LONG_COTS){all.add(new BlockPos(9,0,z));all.add(new BlockPos(-9,0,z));}
        COTS=List.copyOf(all);
    }
    /** The way a cot faces: from its head at the wall toward the aisle. */
    public static Direction cotFacing(BlockPos head){return head.getX()<0?Direction.EAST:Direction.WEST;}
    public static BlockPos cotFoot(BlockPos head){return head.relative(cotFacing(head));}
    /** The screen and the lamp over a cot's head, on the wall. */
    public static BlockPos screen(BlockPos head){return head.above();}
    public static BlockPos lamp(BlockPos head){return head.above(2);}
    /** Where a body stands beside a cot to lie on it: in the aisle, at its foot. */
    public static Vec3 bedside(BlockPos head){var f=cotFacing(head);var foot=cotFoot(head).relative(f);return new Vec3(foot.getX()+.5,0,foot.getZ()+.5);}
    public static boolean isCot(BlockPos rel){return COTS.contains(rel);}

    // ------------------------------------------------------------------------------------------------ where a body is
    public static boolean inPassage(Vec3 r){return r.z<=1&&r.z>PASS_N-1&&Math.abs(r.x-.5)<=PASS_HALF+1&&r.y>-1.5;}
    public static boolean inShaft(Vec3 r){return r.x>=HATCH_W&&r.x<HATCH_E+1&&r.z<=HATCH_S+1&&r.z>HATCH_N&&r.y<-.5;}
    public static boolean overHatch(Vec3 r){return r.x>=HATCH_W&&r.x<HATCH_E+1&&r.z<=HATCH_S+1&&r.z>HATCH_N&&r.y>=-.5&&r.y<1;}
    public static boolean inWard(Vec3 r){return r.z<=WARD_S+1&&r.z>WARD_N&&Math.abs(r.x-.5)<=WARD_HALF+1;}
    public static boolean inMending(Vec3 r){return r.x>=MEND_W&&r.x<MEND_E+1&&r.z<=MEND_S+1&&r.z>MEND_N;}
    public static boolean inLong(Vec3 r){return r.z<=WARD_N&&r.z>LONG_N&&Math.abs(r.x-.5)<=WARD_HALF+1;}
    public static boolean pastFarDoor(Vec3 r){return r.z<=FAR_DOOR.getZ();}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    static BlockState kept(String name){return KeptRegistry.material(name).defaultBlockState();}
    static BlockState mat(String name){return NotYetRegistry.material(name).defaultBlockState();}
    static BlockState fit(NotYetBlock.Kind kind,Direction facing){return NotYetBlock.of(kind,facing,0);}
    static BlockState fit(NotYetBlock.Kind kind,Direction facing,int stage){return NotYetBlock.of(kind,facing,stage);}
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();

    public static void build(ServerLevel l,BlockPos b){
        box(l,b,WEST,BOTTOM,NORTH,EAST,TOP,0,kept("casing"));
        passage(l,b);shaft(l,b);ward(l,b);window(l,b);mending(l,b);longRoom(l,b);far(l,b);
    }

    /** Machine casing, grating underfoot, cable overhead, two strip lamps; three by three of its floor plates are over the shaft. */
    static void passage(ServerLevel l,BlockPos b){
        int w=PASS_HALF;
        box(l,b,-w,-1,PASS_N,w,-1,0,kept("grating"));
        box(l,b,-w,0,PASS_N,w,PASS_HIGH,0,AIR);
        box(l,b,-w,PASS_HIGH+1,PASS_N,w,PASS_HIGH+1,0,kept("ceiling"));
        for(int z=PASS_N;z<=0;z++)for(int side:new int[]{-1,1})if(z!=-1&&z!=PASS_N&&!(z<=HATCH_S&&z>=HATCH_N))set(l,b,side*w,0,z,KeptBlock.of(KeptBlock.Kind.CABLE,Direction.NORTH));
        for(int z:new int[]{-1,PASS_N+1})set(l,b,0,PASS_HIGH,z,KeptBlock.of(KeptBlock.Kind.TUBE,Direction.NORTH));
        for(int x=HATCH_W;x<=HATCH_E;x++)for(int z=HATCH_N;z<=HATCH_S;z++)set(l,b,x,-1,z,hatch(x,z,false));
        // The doorway into the ward, three wide.
        box(l,b,-1,0,WARD_S+1,1,2,WARD_S+1,AIR);
    }
    /** A plate over the shaft, shut or open, its hinge on the side of the shaft it hangs against when open. */
    static BlockState hatch(int x,int z,boolean open){return fit(NotYetBlock.Kind.HATCH,x<0?Direction.WEST:x>0?Direction.EAST:z==HATCH_S?Direction.SOUTH:Direction.NORTH,open?1:0);}
    public static void hatches(ServerLevel l,BlockPos b,boolean open){for(int x=HATCH_W;x<=HATCH_E;x++)for(int z=HATCH_N;z<=HATCH_S;z++){var at=b.offset(x,-1,z);var want=hatch(x,z,open);if(!l.getBlockState(at).equals(want))l.setBlock(at,want,F);}}
    public static boolean hatchOpen(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(0,-1,(HATCH_S+HATCH_N)/2));return NotYetBlock.is(s,NotYetBlock.Kind.HATCH)&&s.getValue(NotYetBlock.STAGE)==1;}

    /** Straight down, in the casing, ten blocks, to grating; nothing at the foot of it but the light from the top. */
    static void shaft(ServerLevel l,BlockPos b){
        box(l,b,HATCH_W,SHAFT_FLOOR+1,HATCH_N,HATCH_E,-2,HATCH_S,AIR);
        box(l,b,HATCH_W,SHAFT_FLOOR,HATCH_N,HATCH_E,SHAFT_FLOOR,HATCH_S,kept("grating"));
        for(int y=SHAFT_FLOOR+1;y<=-2;y+=3)set(l,b,HATCH_E+1,y,HATCH_S+1,kept("rime"));
    }

    /** The ward: tiled floor, a dado to waist height, painted wall above it, cable ceiling, two rows of cots. */
    static void ward(ServerLevel l,BlockPos b){
        int w=WARD_HALF;
        box(l,b,-w,-1,WARD_N,w,-1,WARD_S,mat("tile"));
        box(l,b,-w,0,WARD_N,w,WARD_HIGH,WARD_S,AIR);
        box(l,b,-w,WARD_HIGH+1,WARD_N,w,WARD_HIGH+1,WARD_S,kept("ceiling"));
        for(int z=WARD_N-1;z<=WARD_S+1;z++)for(int y=0;y<=WARD_HIGH;y++)for(int side:new int[]{-1,1})set(l,b,side*(w+1),y,z,y==0?mat("dado"):mat("wall"));
        for(int x=-w;x<=w;x++)for(int y=0;y<=WARD_HIGH;y++){set(l,b,x,y,WARD_N-1,y==0?mat("dado"):mat("wall"));if(!(x>=-1&&x<=1&&y<=2))set(l,b,x,y,WARD_S+1,y==0?mat("dado"):mat("wall"));}
        // The arch to the long room, five wide and four high, with the counter over it.
        box(l,b,-2,0,WARD_N-1,2,3,WARD_N-1,AIR);
        for(int i=0;i<COUNTER_DIGITS;i++)set(l,b,COUNTER_FIRST.offset(i,0,0),fit(NotYetBlock.Kind.COUNTER,Direction.SOUTH));
        for(var head:WARD_COTS)cot(l,b,head,true);
        // Curtains on rails between the cots, bunched at the wall.
        for(int i=0;i+1<EAST_COTS.length;i++)curtain(l,b,1,(EAST_COTS[i]+EAST_COTS[i+1])/2,false);
        for(int i=0;i+1<WEST_COTS.length;i++)if(WEST_COTS[i]-WEST_COTS[i+1]==4)curtain(l,b,-1,(WEST_COTS[i]+WEST_COTS[i+1])/2,false);
        // Strip lamps down the middle; a vent low in each long wall.
        for(int z=WARD_S-3;z>=WARD_N+1;z-=6)set(l,b,0,WARD_HIGH,z,KeptBlock.of(KeptBlock.Kind.TUBE,Direction.NORTH));
        for(int z:new int[]{-14,-34})set(l,b,-w,0,z,fit(NotYetBlock.Kind.VENT,Direction.EAST));
        set(l,b,w,0,-26,fit(NotYetBlock.Kind.VENT,Direction.WEST));
        set(l,b,CHART,fit(NotYetBlock.Kind.CHART,Direction.NORTH));
    }
    /** A cot at its place against the wall, its screen and its lamp over it. */
    static void cot(ServerLevel l,BlockPos b,BlockPos head,boolean screen){
        var f=cotFacing(head);
        set(l,b,head,fit(NotYetBlock.Kind.COT,f));set(l,b,cotFoot(head),fit(NotYetBlock.Kind.COT_FOOT,f));
        if(screen)set(l,b,screen(head),fit(NotYetBlock.Kind.SCREEN,f));
        set(l,b,lamp(head),fit(NotYetBlock.Kind.LAMP,f));
    }
    /** A rail three cells out from the wall on one side, the curtain bunched at the wall end (or drawn across, the whole way). */
    static void curtain(ServerLevel l,BlockPos b,int side,int z,boolean drawn){
        var facing=side>0?Direction.SOUTH:Direction.NORTH;
        for(int k=0;k<3;k++){
            int x=side*(WARD_HALF-k);
            if(drawn){set(l,b,x,1,z,fit(NotYetBlock.Kind.CURTAIN,facing,3));set(l,b,x,0,z,fit(NotYetBlock.Kind.CURTAIN,facing,4));}
            else{set(l,b,x,1,z,fit(NotYetBlock.Kind.CURTAIN,facing,k==0?1:0));if(k==0)set(l,b,x,0,z,fit(NotYetBlock.Kind.CURTAIN,facing,2));}
        }
    }

    /**
     * The long window down the east wall, three rows high: the machine's copy of outdoors, a sky in flat bands, the one cloud it knows
     * three times over, and a sun that does not move. Seen from the ward, west of it, its left is north.
     */
    static void window(ServerLevel l,BlockPos b){
        for(int z=WINDOW_N;z<=WINDOW_S;z++)for(int y=WINDOW_LOW;y<=WINDOW_HIGH;y++)set(l,b,WINDOW_X,y,z,fit(NotYetBlock.Kind.WINDOW,Direction.WEST,0));
        // The sun, two by two: its top-left quarter at the north end of the top row.
        int sz=-21;set(l,b,WINDOW_X,5,sz,fit(NotYetBlock.Kind.WINDOW,Direction.WEST,4));set(l,b,WINDOW_X,5,sz+1,fit(NotYetBlock.Kind.WINDOW,Direction.WEST,5));
        set(l,b,WINDOW_X,4,sz,fit(NotYetBlock.Kind.WINDOW,Direction.WEST,6));set(l,b,WINDOW_X,4,sz+1,fit(NotYetBlock.Kind.WINDOW,Direction.WEST,7));
        for(int[] c:new int[][]{{5,-17},{5,-30},{4,-37}})for(int k=0;k<3;k++)set(l,b,WINDOW_X,c[0],c[1]+k,fit(NotYetBlock.Kind.WINDOW,Direction.WEST,1+k));
    }

    /** The mending room: the table under its ring of arms, a strip lamp over each end; a doorway three wide from the ward. */
    static void mending(ServerLevel l,BlockPos b){
        box(l,b,MEND_W,-1,MEND_N,MEND_E,-1,MEND_S,mat("tile"));
        box(l,b,MEND_W,0,MEND_N,MEND_E,MEND_HIGH,MEND_S,AIR);
        box(l,b,MEND_W,MEND_HIGH+1,MEND_N,MEND_E,MEND_HIGH+1,MEND_S,kept("ceiling"));
        for(int z=MEND_N-1;z<=MEND_S+1;z++)for(int y=0;y<=MEND_HIGH;y++)set(l,b,MEND_W-1,y,z,y==0?mat("dado"):mat("wall"));
        for(int x=MEND_W;x<=MEND_E;x++)for(int y=0;y<=MEND_HIGH;y++){set(l,b,x,y,MEND_N-1,y==0?mat("dado"):mat("wall"));set(l,b,x,y,MEND_S+1,y==0?mat("dado"):mat("wall"));}
        box(l,b,-WARD_HALF-1,0,MEND_DOOR_N,MEND_E+1,2,MEND_DOOR_S,AIR);box(l,b,-WARD_HALF-1,-1,MEND_DOOR_N,MEND_E+1,-1,MEND_DOOR_S,mat("tile"));
        set(l,b,TABLE,fit(NotYetBlock.Kind.TABLE,Direction.NORTH));set(l,b,TABLE_FOOT,fit(NotYetBlock.Kind.TABLE_FOOT,Direction.NORTH));
        for(var arm:ARMS)set(l,b,arm,fit(NotYetBlock.Kind.ARM,armFacing(arm)));
        set(l,b,-15,MEND_HIGH,-24,KeptBlock.of(KeptBlock.Kind.TUBE,Direction.EAST));set(l,b,-15,MEND_HIGH,-31,KeptBlock.of(KeptBlock.Kind.TUBE,Direction.EAST));
        for(int z:new int[]{-24,-31})set(l,b,MEND_W,1,z,KeptBlock.of(KeptBlock.Kind.CABINET,Direction.EAST));
        set(l,b,MEND_W,0,-24,KeptBlock.of(KeptBlock.Kind.CABINET,Direction.EAST));set(l,b,MEND_W,0,-31,KeptBlock.of(KeptBlock.Kind.CABINET,Direction.EAST));
        for(int z=MEND_N+1;z<=MEND_S-1;z++)if(z!=-24&&z!=-31&&(z&1)==0)set(l,b,MEND_W,0,z,KeptBlock.of(KeptBlock.Kind.CABLE,Direction.NORTH));
    }
    /** An arm reaches toward the table from where it hangs. */
    static Direction armFacing(BlockPos arm){
        int dx=TABLE.getX()-arm.getX();int z=arm.getZ();
        if(dx>0)return Direction.EAST;if(dx<0)return Direction.WEST;
        return z>TABLE.getZ()?Direction.NORTH:Direction.SOUTH;
    }
    /** The arms folded up (0), let down (1) or working (2). */
    public static void arms(ServerLevel l,BlockPos b,int stage){
        for(var arm:ARMS){var at=b.offset(arm);var st=l.getBlockState(at);if(NotYetBlock.is(st,NotYetBlock.Kind.ARM)&&st.getValue(NotYetBlock.STAGE)!=stage)l.setBlock(at,st.setValue(NotYetBlock.STAGE,stage),F);}
    }

    /** The long room: the same tile and walls, lower, three cots to a side, curtains drawn round the three that are taken. */
    static void longRoom(ServerLevel l,BlockPos b){
        int w=WARD_HALF;
        box(l,b,-w,-1,LONG_N,w,-1,LONG_S,mat("tile"));
        box(l,b,-w,0,LONG_N,w,LONG_HIGH,LONG_S,AIR);
        box(l,b,-w,LONG_HIGH+1,LONG_N,w,WARD_HIGH+1,LONG_S,kept("ceiling"));
        for(int z=LONG_N-1;z<=LONG_S;z++)for(int y=0;y<=LONG_HIGH;y++)for(int side:new int[]{-1,1})set(l,b,side*(w+1),y,z,y==0?mat("dado"):mat("wall"));
        for(int x=-w;x<=w;x++)for(int y=0;y<=LONG_HIGH;y++)set(l,b,x,y,LONG_N-1,y==0?mat("dado"):mat("wall"));
        // The wall between ward and long room, a block thick with the arch through it, is the ward's.
        box(l,b,-2,0,WARD_N-1,2,3,LONG_S,AIR);
        for(int z:LONG_COTS)for(int side:new int[]{-1,1}){
            var head=new BlockPos(side*w,0,z);cot(l,b,head,true);
            boolean taken=false;for(var p:PEOPLE_COTS)if(p.equals(head))taken=true;
            var screenAt=screen(head);set(l,b,screenAt,fit(NotYetBlock.Kind.SCREEN,cotFacing(head),taken?NotYetBlock.NOISE:NotYetBlock.DARK));
            if(taken){set(l,b,lamp(head),fit(NotYetBlock.Kind.LAMP,cotFacing(head),1));curtain(l,b,side,z-2,true);}
        }
        for(int z:new int[]{-52,-57})for(int side:new int[]{-1,1})set(l,b,side*w,0,z,fit(NotYetBlock.Kind.VENT,side<0?Direction.EAST:Direction.WEST,1));
    }

    /** The far door in the long room's end wall, shut; past it a small room in the casing and the way on. */
    static void far(ServerLevel l,BlockPos b){
        box(l,b,-2,-1,ONWARD.getZ(),2,-1,FAR_DOOR.getZ()-1,kept("grating"));
        box(l,b,-2,0,ONWARD.getZ()+1,2,3,FAR_DOOR.getZ()-1,AIR);
        box(l,b,-2,4,ONWARD.getZ()+1,2,4,FAR_DOOR.getZ()-1,kept("ceiling"));
        set(l,b,0,3,ONWARD.getZ()+1,KeptBlock.of(KeptBlock.Kind.TUBE,Direction.EAST));
        door(l,b,false);
    }
    /** The far door, shut or slid open. */
    public static void door(ServerLevel l,BlockPos b,boolean open){
        var lower=b.offset(FAR_DOOR);
        var a=fit(NotYetBlock.Kind.DOOR,Direction.SOUTH,open?2:0);var c=fit(NotYetBlock.Kind.DOOR,Direction.SOUTH,open?3:1);
        if(!l.getBlockState(lower).equals(a))l.setBlock(lower,a,F);if(!l.getBlockState(lower.above()).equals(c))l.setBlock(lower.above(),c,F);
    }
    public static boolean doorOpen(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(FAR_DOOR));return NotYetBlock.is(s,NotYetBlock.Kind.DOOR)&&s.getValue(NotYetBlock.STAGE)>=2;}

    /** The ward as it was: the plates shut, the arms folded, the far door shut, every reader's cot as it stands. */
    public static void reset(ServerLevel l,BlockPos b){hatches(l,b,false);arms(l,b,0);door(l,b,false);}
}
