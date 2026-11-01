package io.github.knaitoe.theoldesthouse.labyrinth;

import static io.github.knaitoe.theoldesthouse.labyrinth.PittBlock.Kind.*;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.207, the God of the Pitt (docs/GOD_OF_THE_PITT_0_4_207.md): the deeper passages under Ted the Caver's cave, built whole,
 * standing at their own height ({@link #BASE_Y}) in the outside dimension so the void under the ledge has somewhere to go.
 *
 * <p>From the reader's door: Brad's staging ledge, a domed sinkhole floor with his rope hanging down its throat from the moor;
 * a two-high stoopway into Floyd's Tomb, an eighteen-block crawl under pinch-ceiling slabs with four seams across it and a
 * fissure along its east side into an unlit parallel crawl; the fist-sized hole at its end, broken open after the first visit
 * into the Wind Tunnel, a vault a crouch high, and steps down into the Round Stone Room, where the round stone stands in its
 * copper grooves against the west wall. Two chimneys go down from that room: the one the reader is shown first, in its north-east
 * corner, which pinches out; and from the third visit, with the stone pushed out of its place, the one behind it, three drops on
 * Brad's ropes joined by squeezes. At the bottom the Pre-Echo Ledge, a shelf over a void, bridges nobody can see, and a rusted
 * ladder up the far wall to a hatch in the moor. Every position is relative to the base.
 */
public final class PittCave {
    private PittCave(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.PITT;
    /** The cave's own floor height in the outside dimension: the void goes forty blocks down from here. */
    public static final int BASE_Y=200;
    /** The staging ledge: Brad's note on its piton, his gear crate, Brad and his belay post, his rope down the sinkhole's throat. */
    public static final BlockPos NOTE=new BlockPos(-6,1,-6),CRATE=new BlockPos(-3,0,-11),BRAD=new BlockPos(2,0,-13),BELAY=new BlockPos(1,0,-14),ROPE_X=new BlockPos(0,0,-8);
    /** Floyd's Tomb: its crawl runs along x 0 from the stoopway to the fist-sized hole. */
    public static final int TOMB_Z0=-17,TOMB_Z1=-34;
    public static final int[] SEAMS={-20,-24,-28,-32};
    public static final BlockPos HOLE=new BlockPos(0,0,-35);
    /** The bolts the orange line is clipped to, in order from the ledge: four in the Tomb, one in the Wind Tunnel, one in the room. */
    public static final BlockPos[] ANCHORS={new BlockPos(-1,0,-18),new BlockPos(-1,0,-23),new BlockPos(-1,0,-28),new BlockPos(-1,0,-33),new BlockPos(-2,0,-37),new BlockPos(-5,-1,-42)};
    /** The Round Stone Room's floor (two below the Tomb), the stone's place in the west wall and where it stands pushed out, its grooves. */
    public static final int ROOM_Y=-2,WHEEL_X=-5,WHEEL_OUT_X=-3,WHEEL_Z=-44;
    public static final BlockPos HELMET=new BlockPos(-4,-2,-49);
    /** The first chimney's mouth in the room's north-east corner, and the second's in the alcove behind the stone. */
    public static final BlockPos MOUTH_A=new BlockPos(4,-3,-50),MOUTH_B=new BlockPos(-7,-3,-46);
    /** Where Brad's line goes into a crack in the second chimney's middle squeeze. */
    public static final BlockPos CUT=new BlockPos(1,-15,-47),LINE_AT=new BlockPos(-6,-2,-46);
    /** The Pre-Echo Ledge's landing (its floor), and the void it looks out over. */
    public static final int LEDGE_Y=-24,VOID_Y0=-38,VOID_Y1=-14;
    public static final BlockPos LANDING=new BlockPos(2,-24,-52);
    /** The bridges nobody can see, from the shelf's lip to the foot of the ladder, in the order they are walked. */
    public static final List<BlockPos> PATH=List.of(new BlockPos(9,-25,-55),new BlockPos(9,-25,-56),new BlockPos(9,-25,-57),new BlockPos(8,-25,-57),new BlockPos(7,-25,-57),new BlockPos(7,-25,-58),
        new BlockPos(7,-25,-59),new BlockPos(8,-25,-59),new BlockPos(9,-25,-59),new BlockPos(10,-25,-59),new BlockPos(10,-25,-60),new BlockPos(10,-25,-61),new BlockPos(10,-25,-62));
    /** The rusted ladder up the void's far wall, its hatch in the moor, and the field journal on its stone (the account). */
    public static final BlockPos LADDER=new BlockPos(10,-24,-62),HATCH=new BlockPos(10,8,-62),JOURNAL=new BlockPos(12,9,-59);
    /** The moor's grass is at this height, so it is walked a block above. */
    public static final int MOOR_Y=8;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();

    private static BlockState mat(String name){return PittRegistry.material(name).defaultBlockState();}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    static BlockState fit(PittBlock.Kind k,Direction f){return PittBlock.of(k,f,0);}
    static BlockState fit(PittBlock.Kind k,Direction f,int stage){return PittBlock.of(k,f,stage);}
    static BlockState rope(){return PittClimbBlock.of(PittClimbBlock.Kind.ROPE,Direction.NORTH);}
    public static Vec3 at(BlockPos b,double x,double y,double z){return new Vec3(b.getX()+x,b.getY()+y,b.getZ()+z);}

    public static void build(ServerLevel l,BlockPos b){
        var rnd=new Random(207);
        // The rock: slate limestone with ochre bands, clay underfoot.
        for(int x=-24;x<=24;x++)for(int z=-64;z<=0;z++)for(int y=-40;y<=MOOR_Y-1;y++){
            int band=Math.floorMod(y*3+(int)Math.round(Math.sin(x*.31+z*.17)*2),11);
            set(l,b,x,y,z,band<2?mat("ochre"):mat("limestone"));
        }
        box(l,b,-24,MOOR_Y,-64,24,16,0,AIR);
        moor(l,b,rnd);staging(l,b);tomb(l,b);windTunnel(l,b);room(l,b);chimneyA(l,b);chimneyB(l,b);ledge(l,b);
        // Brad's rope, tied off at the lip, hangs down the throat to the ledge's floor; hung last, after everything is carved.
        for(int y=0;y<=MOOR_Y;y++)set(l,b,ROPE_X.getX(),y,ROPE_X.getZ(),rope());
        visit(l,b,1);
    }
    /** The moor over the cave: grass, a rocky rim, spruce, the sinkhole's throat with Brad's rope tied off at its lip, and the hatch. */
    private static void moor(ServerLevel l,BlockPos b,Random rnd){
        for(int x=-24;x<=24;x++)for(int z=-64;z<=0;z++){
            boolean rim=Math.abs(x)>=23||z<=-63||z>=-1;
            set(l,b,x,MOOR_Y,z,Blocks.GRASS_BLOCK.defaultBlockState());
            if(rim){int h=2+Math.floorMod(x*7+z*13,3);for(int y=1;y<=h;y++)set(l,b,x,MOOR_Y+y,z,(x+z+y)%3==0?Blocks.ANDESITE.defaultBlockState():Blocks.STONE.defaultBlockState());}
            else if(rnd.nextInt(5)==0)set(l,b,x,MOOR_Y+1,z,rnd.nextInt(3)==0?Blocks.FERN.defaultBlockState():Blocks.SHORT_GRASS.defaultBlockState());
        }
        for(var t:List.of(new BlockPos(-18,0,-12),new BlockPos(18,0,-20),new BlockPos(-15,0,-40),new BlockPos(17,0,-47),new BlockPos(-19,0,-56),new BlockPos(6,0,-31),new BlockPos(-9,0,-26),new BlockPos(19,0,-6)))spruce(l,b,t.getX(),t.getZ(),5+Math.floorMod(t.getX()*3+t.getZ(),3));
        for(var s:List.of(new BlockPos(-6,0,-18),new BlockPos(9,0,-12),new BlockPos(-12,0,-50),new BlockPos(14,0,-36)))set(l,b,s.getX(),MOOR_Y+1,s.getZ(),Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        // The sinkhole's throat, a ring of mossy stones round it, and the post Brad's rope is tied to.
        for(int y=3;y<=MOOR_Y;y++)set(l,b,0,y,-8,AIR);
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(dx!=0||dz!=0)set(l,b,dx,MOOR_Y+1,-8+dz,Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState());
        set(l,b,0,MOOR_Y+1,-10,Blocks.SPRUCE_FENCE.defaultBlockState());
        // The hatch at the top of the ladder, and the field journal on its stone a few steps from it.
        set(l,b,HATCH,Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.SOUTH).setValue(TrapDoorBlock.HALF,Half.TOP).setValue(TrapDoorBlock.OPEN,false));
        set(l,b,JOURNAL,fit(PittBlock.Kind.JOURNAL,Direction.SOUTH));set(l,b,JOURNAL.getX()+1,MOOR_Y+1,JOURNAL.getZ(),Blocks.COBBLESTONE_WALL.defaultBlockState());
    }
    private static void spruce(ServerLevel l,BlockPos b,int x,int z,int h){
        var leaves=Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        for(int y=1;y<=h;y++)set(l,b,x,MOOR_Y+y,z,Blocks.SPRUCE_LOG.defaultBlockState());
        for(int y=3;y<=h+1;y++){int r=y>=h?1:2;for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(dx==0&&dz==0&&y<=h)continue;if(Math.abs(dx)==r&&Math.abs(dz)==r&&r==2)continue;
            if(BuildBlocks.state(l,b.offset(x+dx,MOOR_Y+y,z+dz)).isAir())set(l,b,x+dx,MOOR_Y+y,z+dz,leaves);}}
    }
    /** Brad's staging ledge: the domed sinkhole floor, lanterns, the crate, the note, Brad at the belay, rope coils and tins. */
    private static void staging(ServerLevel l,BlockPos b){
        box(l,b,-1,0,0,1,2,-1,AIR);
        box(l,b,-6,0,-1,6,2,-14,AIR);
        for(int y=3;y<=6;y++){double k=1-(y-2)/5.0;double rx=6.5*Math.sqrt(k),rz=7*Math.sqrt(k);
            for(int x=-6;x<=6;x++)for(int z=-1;z>=-14;z--)if(x*x/(rx*rx)+(z+7.5)*(z+7.5)/(rz*rz)<=1)set(l,b,x,y,z,AIR);}
        for(int x=-6;x<=6;x++)for(int z=-1;z>=-14;z--)set(l,b,x,-1,z,(x*5+z*3)%7==0?mat("ochre"):mat("clay"));
        box(l,b,0,0,-15,0,1,-16,AIR);
        set(l,b,NOTE,fit(PittBlock.Kind.NOTE,Direction.EAST));
        set(l,b,CRATE,fit(PittBlock.Kind.CRATE,Direction.EAST));
        set(l,b,-5,0,-3,fit(LANTERN,Direction.NORTH));set(l,b,5,0,-10,fit(LANTERN,Direction.NORTH));
        set(l,b,-4,0,-12,fit(CANISTER,Direction.NORTH));set(l,b,-5,0,-12,fit(CANISTER,Direction.EAST));
        set(l,b,4,0,-5,fit(TINS,Direction.WEST));set(l,b,5,0,-4,fit(COIL,Direction.NORTH));set(l,b,-5,0,-9,fit(COIL,Direction.EAST));
        set(l,b,-4,0,-11,fit(PittBlock.Kind.CRATE,Direction.EAST));
        for(var c:List.of(new BlockPos(-6,0,-2),new BlockPos(6,0,-14),new BlockPos(-6,0,-14)))set(l,b,c,Blocks.POINTED_DRIPSTONE.defaultBlockState());
        for(var c:List.of(new BlockPos(3,4,-5),new BlockPos(-3,4,-10),new BlockPos(2,4,-11)))if(BuildBlocks.state(l,b.offset(c)).isAir()&&!BuildBlocks.state(l,b.offset(c.above())).isAir())
            set(l,b,c,Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION,Direction.DOWN));
    }
    /** Floyd's Tomb: the crawl, its clay floor, the pinch-ceiling slabs over it, the bolts on the west wall and the fissure on the east. */
    private static void tomb(ServerLevel l,BlockPos b){
        for(int z=TOMB_Z0;z>=TOMB_Z1;z--){
            set(l,b,0,0,z,AIR);set(l,b,0,-1,z,mat("clay"));set(l,b,0,1,z,fit(SLAB,Direction.NORTH,0));
            set(l,b,1,0,z,fit(FISSURE,Direction.WEST));set(l,b,2,0,z,AIR);set(l,b,2,-1,z,mat("clay"));
        }
        for(int i=0;i<4;i++)set(l,b,ANCHORS[i],fit(ANCHOR,Direction.EAST));
    }
    /** The Wind Tunnel past the hole: a vault a crouch high, a bolt, and steps down into the round room. */
    private static void windTunnel(ServerLevel l,BlockPos b){
        box(l,b,-1,0,-36,1,0,-38,AIR);for(int x=-1;x<=1;x++)for(int z=-36;z>=-38;z--)set(l,b,x,1,z,fit(CEILING,Direction.NORTH));
        box(l,b,-1,-1,-36,1,-1,-38,mat("clay"));
        set(l,b,ANCHORS[4],fit(ANCHOR,Direction.EAST));
        var stair=Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH);
        for(int x=-1;x<=1;x++){set(l,b,x,-1,-39,stair);box(l,b,x,0,-39,x,1,-39,AIR);set(l,b,x,-2,-40,stair);box(l,b,x,-1,-40,x,1,-40,AIR);}
    }
    /** The Round Stone Room: calcite flowstone on the walls, the copper grooves, the stone in the west wall and the alcove behind it. */
    private static void room(ServerLevel l,BlockPos b){
        box(l,b,-4,ROOM_Y,-41,5,ROOM_Y+4,-50,AIR);box(l,b,-4,ROOM_Y-1,-41,5,ROOM_Y-1,-50,mat("clay"));
        for(int z=-41;z>=-50;z--)for(int y=ROOM_Y;y<=ROOM_Y+4;y++){if(Math.floorMod(z+y,3)==0)set(l,b,6,y,z,mat("calcite"));}
        for(int x=-4;x<=5;x++)set(l,b,x,ROOM_Y+5,-50,mat("calcite"));
        for(int x=WHEEL_X+1;x<=-1;x++)for(int z=WHEEL_Z-1;z>=WHEEL_Z-2;z--)set(l,b,x,ROOM_Y-1,z,fit(GROOVE,Direction.EAST));
        box(l,b,-7,ROOM_Y,WHEEL_Z,-6,ROOM_Y+3,WHEEL_Z-3,AIR);
        set(l,b,ANCHORS[5],fit(ANCHOR,Direction.EAST));
        for(var c:List.of(new BlockPos(5,ROOM_Y,-41),new BlockPos(-4,ROOM_Y,-50)))set(l,b,c,Blocks.POINTED_DRIPSTONE.defaultBlockState());
    }
    /** The first chimney: one drop on a rope from the room's north-east corner to a landing, and a squeeze west that pinches out into a fissure. */
    private static void chimneyA(ServerLevel l,BlockPos b){
        for(int y=-8;y<=-3;y++)set(l,b,MOUTH_A.getX(),y,MOUTH_A.getZ(),rope());
        box(l,b,3,-9,-49,5,-8,-51,AIR);box(l,b,3,-10,-49,5,-10,-51,mat("clay"));
        for(int x=2;x>=0;x--){set(l,b,x,-9,-50,AIR);set(l,b,x,-10,-50,mat("clay"));}
        set(l,b,-1,-9,-50,fit(FISSURE,Direction.EAST));
    }
    /** The second chimney: three drops on Brad's ropes and three squeezes, fissures beside them, his line down it and into a crack. */
    private static void chimneyB(ServerLevel l,BlockPos b){
        for(int y=-8;y<=-3;y++)set(l,b,MOUTH_B.getX(),y,MOUTH_B.getZ(),rope());
        box(l,b,-8,-9,-45,-6,-8,-47,AIR);box(l,b,-8,-10,-45,-6,-10,-47,mat("clay"));
        for(int x=-5;x<=-3;x++){set(l,b,x,-9,-46,AIR);set(l,b,x,-10,-46,mat("clay"));set(l,b,x,-9,-45,fit(FISSURE,Direction.NORTH));}
        for(int y=-14;y<=-9;y++)set(l,b,-2,y,-46,rope());set(l,b,-2,-9,-46,AIR);
        box(l,b,-3,-15,-45,-1,-14,-47,AIR);box(l,b,-3,-16,-45,-1,-16,-47,mat("clay"));
        for(int y=-14;y<=-10;y++)set(l,b,-2,y,-46,rope());
        for(int x=0;x<=2;x++){set(l,b,x,-15,-46,AIR);set(l,b,x,-16,-46,mat("clay"));set(l,b,x,-15,-45,fit(FISSURE,Direction.NORTH));}
        set(l,b,CUT,fit(PittBlock.Kind.CUT,Direction.SOUTH));
        for(int y=-20;y<=-15;y++)set(l,b,3,y,-46,rope());
        box(l,b,2,-21,-45,4,-20,-47,AIR);box(l,b,2,-22,-45,4,-22,-47,mat("clay"));
        for(int z=-48;z>=-50;z--){set(l,b,3,-21,z,AIR);set(l,b,3,-22,z,mat("clay"));set(l,b,4,-21,z,fit(FISSURE,Direction.WEST));}
        set(l,b,3,-21,-51,AIR);set(l,b,3,-22,-51,AIR);set(l,b,3,-23,-51,AIR);
    }
    /** The Pre-Echo Ledge: the landing, the shelf along the void's near edge, the void, the bridges, the ladder and its shaft. */
    private static void ledge(ServerLevel l,BlockPos b){
        box(l,b,-18,VOID_Y0,-54,18,VOID_Y1,-62,AIR);
        box(l,b,0,LEDGE_Y,-51,4,LEDGE_Y+2,-53,AIR);box(l,b,0,LEDGE_Y-1,-51,4,LEDGE_Y-1,-53,mat("clay"));
        for(int x=0;x<=9;x++){set(l,b,x,LEDGE_Y-1,-54,mat("limestone"));set(l,b,x,LEDGE_Y-2,-54,mat("limestone"));}
        for(int x=5;x<=9;x++)set(l,b,x,LEDGE_Y-1,-55,Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        for(int i=1;i<PATH.size()-1;i++)set(l,b,PATH.get(i),fit(BRIDGE,Direction.NORTH));
        set(l,b,PATH.get(PATH.size()-1),mat("limestone"));
        for(int y=VOID_Y1+1;y<MOOR_Y;y++)set(l,b,LADDER.getX(),y,LADDER.getZ(),AIR);
        for(int y=LEDGE_Y;y<MOOR_Y;y++)set(l,b,LADDER.getX(),y,LADDER.getZ(),PittClimbBlock.of(PittClimbBlock.Kind.LADDER,Direction.SOUTH));
        for(int x=-4;x<=4;x+=2)set(l,b,x*3,VOID_Y1,-58+x/2,Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION,Direction.DOWN));
    }

    // ------------------------------------------------------------------------------------------------ the visits
    /**
     * The passages made ready for a visit. The first: four seams shut, the hole a hole, Brad at the belay, the stone in its place
     * and the first chimney open. The second: the seams cut, the hole broken through. The third and fourth: Brad gone and his
     * belay hanging slack, the stone pushed out along its grooves from inside, the first chimney choked with rubble, the second
     * open behind the stone, his helmet on the floor. Slabs go back up and the fissure goes empty every time.
     */
    public static void visit(ServerLevel l,BlockPos b,int visit){
        for(int i=0;i<SEAMS.length;i++)set(l,b,0,0,SEAMS[i],fit(SEAM,Direction.NORTH,visit>=2?4:0));
        set(l,b,HOLE,visit>=2?AIR:fit(PittBlock.Kind.HOLE,Direction.SOUTH));
        for(int z=TOMB_Z0;z>=TOMB_Z1;z--){set(l,b,0,1,z,fit(SLAB,Direction.NORTH,0));set(l,b,1,0,z,fit(FISSURE,Direction.WEST,0));}
        for(var a:ANCHORS)set(l,b,a,fit(ANCHOR,Direction.EAST,0));
        brad(l,b,visit<=2);
        set(l,b,BELAY,fit(PittBlock.Kind.BELAY,Direction.NORTH,visit<=2?0:1));
        wheel(l,b,visit>=3?WHEEL_OUT_X:WHEEL_X);
        // The alcove is walled behind the stone until it is pushed out.
        for(int y=ROOM_Y;y<=ROOM_Y+3;y++)for(int z=WHEEL_Z;z>=WHEEL_Z-3;z--){set(l,b,-6,y,z,visit>=3?AIR:mat("limestone"));set(l,b,-7,y,z,visit>=3?AIR:mat("limestone"));}
        set(l,b,MOUTH_B,visit>=3?AIR:mat("limestone"));
        // Brad's line, across the alcove's floor from where the stone stood to the hole, and on down.
        if(visit>=3)set(l,b,LINE_AT,fit(LINE,Direction.WEST));
        set(l,b,MOUTH_A,visit>=3?fit(RUBBLE,Direction.NORTH):AIR);
        for(int y=-8;y<=-4;y++)set(l,b,MOUTH_A.getX(),y,MOUTH_A.getZ(),visit>=3?fit(RUBBLE,Direction.NORTH):rope());
        set(l,b,HELMET,visit>=3?fit(PittBlock.Kind.HELMET,Direction.WEST,1):AIR);
        rubble(l,b,false);
        var hatch=BuildBlocks.state(l,b.offset(HATCH));if(hatch.is(Blocks.IRON_TRAPDOOR)&&hatch.getValue(TrapDoorBlock.OPEN))set(l,b,HATCH,hatch.setValue(TrapDoorBlock.OPEN,false));
    }
    /**
     * Brad's kit box by the stoopway; gone from the third visit. 0.4.220: Brad himself is a painted actor who sits on it
     * ({@link PittGame#bradActor}), there while the box is; the cell over it is left for his lamp's light.
     */
    public static void brad(ServerLevel l,BlockPos b,boolean there){
        set(l,b,BRAD,there?fit(PittBlock.Kind.BRAD,Direction.NORTH):AIR);set(l,b,BRAD.above(),AIR);
    }
    /** The round stone, sixteen faces in a four-by-four disc standing on edge, its face to the east, at x. */
    public static void wheel(ServerLevel l,BlockPos b,int x){
        for(int u=0;u<4;u++)for(int v=0;v<4;v++){
            for(int other:new int[]{WHEEL_X,WHEEL_OUT_X}){var at=b.offset(other,ROOM_Y+v,WHEEL_Z-u);var s=BuildBlocks.state(l,at);if(PittBlock.is(s,WHEEL)||PittBlock.is(s,WHEEL_LOW))BuildBlocks.set(l,at,AIR,F);}
        }
        for(int u=0;u<4;u++)for(int v=0;v<4;v++)set(l,b,x,ROOM_Y+v,WHEEL_Z-u,fit(v>=2?WHEEL:WHEEL_LOW,Direction.EAST,u+4*(v%2)));
    }
    /** The collapse's rubble, dropped along the round room's sides (never its middle, never on anyone), or cleared. */
    public static final List<BlockPos> RUBBLE_CELLS=List.of(new BlockPos(5,ROOM_Y,-43),new BlockPos(4,ROOM_Y,-45),new BlockPos(5,ROOM_Y,-47),new BlockPos(-3,ROOM_Y,-49),new BlockPos(4,ROOM_Y,-49),
        new BlockPos(5,ROOM_Y+1,-43),new BlockPos(5,ROOM_Y,-49),new BlockPos(3,ROOM_Y,-50),new BlockPos(-4,ROOM_Y,-41),new BlockPos(5,ROOM_Y,-44));
    public static void rubble(ServerLevel l,BlockPos b,boolean fallen){for(var c:RUBBLE_CELLS){var s=BuildBlocks.state(l,b.offset(c));if(fallen?s.isAir():PittBlock.is(s,RUBBLE))set(l,b,c,fallen?fit(RUBBLE,Direction.NORTH):AIR);}}

    // ------------------------------------------------------------------------------------------------ where the reader is
    /** Every cell crawled on hands and knees: the Tomb, and the three squeezes of each chimney. */
    public static final Set<BlockPos> CRAWL=new HashSet<>();
    static{
        for(int z=TOMB_Z0;z>=TOMB_Z1;z--)CRAWL.add(new BlockPos(0,0,z));CRAWL.add(HOLE);
        for(int x=2;x>=0;x--)CRAWL.add(new BlockPos(x,-9,-50));
        for(int x=-5;x<=-3;x++)CRAWL.add(new BlockPos(x,-9,-46));
        for(int x=0;x<=2;x++)CRAWL.add(new BlockPos(x,-15,-46));
        for(int z=-48;z>=-51;z--)CRAWL.add(new BlockPos(3,-21,z));
    }
    /** The cracks beside the second chimney's three squeezes, where the eyes are on the third visit. */
    public static final List<BlockPos> CHIMNEY_FISSURES=new ArrayList<>();
    static{for(int x=-5;x<=-3;x++)CHIMNEY_FISSURES.add(new BlockPos(x,-9,-45));for(int x=0;x<=2;x++)CHIMNEY_FISSURES.add(new BlockPos(x,-15,-45));for(int z=-48;z>=-50;z--)CHIMNEY_FISSURES.add(new BlockPos(4,-21,z));}
    /** The cell in front of each crawl's mouth, and the way into the crawl from it. */
    public static final Map<BlockPos,Direction> APPROACH=Map.of(new BlockPos(0,0,-16),Direction.NORTH,new BlockPos(3,-9,-50),Direction.WEST,new BlockPos(-6,-9,-46),Direction.EAST,
        new BlockPos(-1,-15,-46),Direction.EAST,new BlockPos(3,-21,-47),Direction.NORTH);
    public static boolean inTomb(Vec3 r){return r.x>-.3&&r.x<1.3&&r.z<=TOMB_Z0+1.3&&r.z>=TOMB_Z1-1.2&&r.y>-.6&&r.y<1;}
    public static boolean inWindTunnel(Vec3 r){return r.x>-1.5&&r.x<2.5&&r.z<-35&&r.z>=-40&&r.y>-2.2&&r.y<1.5;}
    public static boolean onStaging(Vec3 r){return r.z>-15.2&&r.z<1&&r.y>-.5&&r.y<7;}
    public static boolean inRoom(Vec3 r){return r.x>-8&&r.x<6.5&&r.z<=-40.5&&r.z>=-51&&r.y>=ROOM_Y-.5&&r.y<ROOM_Y+5;}
    public static boolean onLanding(Vec3 r){return r.x>-.2&&r.x<5.2&&r.z<=-50.8&&r.z>=-54.2&&r.y>LEDGE_Y-.6&&r.y<LEDGE_Y+3;}
    public static boolean onLedge(Vec3 r){return r.z<=-50.8&&r.y>LEDGE_Y-3&&r.y<LEDGE_Y+3;}
    public static boolean onMoor(Vec3 r){return r.y>=MOOR_Y+.5;}
    public static BlockPos cell(Vec3 r){return BlockPos.containing(r.x,r.y+.2,r.z);}
}
