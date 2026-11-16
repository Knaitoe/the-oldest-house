package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.LongerBlock.Kind.*;

/**
 * 0.4.621, A Little Longer (docs/A_LITTLE_LONGER_PLAN.md, docs/A_LITTLE_LONGER_PROPOSAL.md): Rowan's shop, the flat above it, the roof
 * landing and a short stretch of the House's own corridor, built whole, at night, in the rain. Every position is relative to the base;
 * north is into the scene.
 * <p>
 * The House's door opens on its own corridor (x -1 to 1, z -1 to -6), papered and carpeted the House's way, which ends at the shop's
 * back door (0,0,-7). The shop is x -6 to 6, z -8 to -18, its floor at 0 and its ceiling at 4: the workbench against the west wall
 * with the parts trays, the radio and the repaired lamp on it, the unfinished work order on its spike above, and the chair whose leg
 * was fixed twice; parts shelves along the east wall; the counter across the front, and the shopfront with its locked door onto a
 * street that is only ever on the other side of the glass. A stair climbs the back wall eastward (x 1 to 6, z -8 and -9) to the flat
 * (floor 6, ceiling 10): the coat hook at the stairhead, the kitchen worktop along the east wall with the kettle, the toaster and the
 * sink; the table under the front window, the comfortable chair on its west side and the kitchen chair on its east, the two cups
 * between them; the bathroom in the west back corner; a second stair up the west wall (x -6 and -5, z -17 to -13) into a little stair
 * house on the roof, whose door opens east onto the felt (surface 12). The roof is walled to shoulder height; the bench under its
 * awning in the north-east corner looks up at the one patch of sky the leaning buildings round the yard leave open.
 */
public final class LongerSite {
    private LongerSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LITTLE_LONGER;
    public static final int UP=6,ROOF=12;
    // ------------------------------------------------------------------------------------------------ the corridor and the shop
    public static final BlockPos BACK_DOOR=new BlockPos(0,0,-7),FRONT_DOOR=new BlockPos(0,0,-19);
    /** The workbench (three long against the west wall) and what stands on it. */
    public static final List<BlockPos> BENCH_PARTS=List.of(new BlockPos(-6,0,-10),new BlockPos(-6,0,-11),new BlockPos(-6,0,-12));
    public static final BlockPos LAMP=new BlockPos(-6,1,-10),RADIO=new BlockPos(-6,1,-11),TRAYS=new BlockPos(-6,1,-12),ORDER=new BlockPos(-6,2,-11),
        CHAIR=new BlockPos(-5,0,-11);
    public static final List<BlockPos> SHELVES=List.of(new BlockPos(6,0,-11),new BlockPos(6,0,-12),new BlockPos(6,0,-13),new BlockPos(6,1,-11),new BlockPos(6,1,-12),new BlockPos(6,1,-13));
    public static final List<BlockPos> COUNTER=List.of(new BlockPos(3,0,-16),new BlockPos(4,0,-16),new BlockPos(5,0,-16),new BlockPos(6,0,-16));
    /** The work order: the first paper, on its spike over the bench. */
    public static final BlockPos SOURCE=ORDER;
    // ------------------------------------------------------------------------------------------------ the flat
    public static final BlockPos HOOK=new BlockPos(6,UP+1,-10),KETTLE=new BlockPos(6,UP+1,-12),TOASTER=new BlockPos(6,UP+1,-13),SINK=new BlockPos(6,UP,-14),
        FLAT_RADIO=new BlockPos(6,UP+1,-11),TABLE=new BlockPos(1,UP,-17),CUPS=new BlockPos(1,UP+1,-17),ARMCHAIR=new BlockPos(0,UP,-17),KITCHEN_CHAIR=new BlockPos(2,UP,-17),
        BATH_DOOR=new BlockPos(-2,UP,-10),BASIN=new BlockPos(-6,UP,-9),LAVATORY=new BlockPos(-6,UP,-11),ROOF_DOOR=new BlockPos(-4,ROOF,-11);
    public static final List<BlockPos> WORKTOP=List.of(new BlockPos(6,UP,-11),new BlockPos(6,UP,-12),new BlockPos(6,UP,-13),SINK);
    /** Where the account will wait: the kitchen table. */
    public static final BlockPos ENDING=TABLE;
    // ------------------------------------------------------------------------------------------------ the roof
    public static final BlockPos BENCH=new BlockPos(4,ROOF,-9),TANK=new BlockPos(-2,ROOF,-17);
    /** Where the reader's way on is: a second door in the House's corridor. */
    public static final BlockPos ONWARD=new BlockPos(2,0,-3);

    public static boolean inside(Vec3 r){return r.x>=-16&&r.x<17&&r.z>=-30&&r.z<1&&r.y>=-2&&r.y<31;}
    public static boolean inCorridor(Vec3 r){return r.x>=-1.5&&r.x<2.5&&r.z>=-6.9&&r.z<.5&&r.y<4;}
    public static boolean inShop(Vec3 r){return r.x>=-6&&r.x<7&&r.z>=-18.9&&r.z<-7&&r.y>=-.5&&r.y<4;}
    public static boolean inFlat(Vec3 r){return r.x>=-6&&r.x<7&&r.z>=-18.9&&r.z<-7&&r.y>=UP-.5&&r.y<UP+4;}
    public static boolean inBathroom(Vec3 r){return inFlat(r)&&r.x<-2&&r.z>=-11.9;}
    public static boolean onRoof(Vec3 r){return r.x>=-7&&r.x<8&&r.z>=-19.9&&r.z<-6&&r.y>=ROOF-.5&&r.y<ROOF+5;}

    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static BlockState mat(String name){return LongerRegistry.material(name).defaultBlockState();}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);
    }
    static void door(ServerLevel l,BlockPos b,BlockPos at,Block door,Direction facing,DoorHingeSide hinge){
        var s=door.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.OPEN,false).setValue(DoorBlock.HINGE,hinge);
        set(l,b,at,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,at.above(),s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    static BlockState fit(LongerBlock.Kind kind,Direction facing){return LongerBlock.of(kind,facing);}
    static BlockState fit(LongerBlock.Kind kind,Direction facing,int stage){return LongerBlock.of(kind,facing,stage);}
    private static BlockState stair(Block s,Direction facing){return s.defaultBlockState().setValue(StairBlock.FACING,facing);}
    private static BlockState light(int level){return Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,level);}
    /** A two-high rain window in a wall, facing the way its inside looks. */
    static void window(ServerLevel l,BlockPos b,int x,int y,int z,Direction in){set(l,b,x,y,z,fit(WINDOW,in,0));set(l,b,x,y+1,z,fit(WINDOW,in,1));}

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        box(l,b,-16,-2,-30,16,30,0,air);
        box(l,b,-16,-2,-30,16,-2,0,mat("facade"));
        yard(l,b);corridor(l,b);shop(l,b);flat(l,b);roof(l,b);windows(l,b);
        RoomActors.settlePanes(l,b,PLACE);
    }

    /** The buildings round the yard and across the street: they lean in at the top, as no building should. */
    private static void yard(ServerLevel l,BlockPos b){
        var facade=mat("facade");
        // The street outside the shopfront: pavement, a curb of coping each side, the road, the facade across it, and an end wall
        // either way where the street bends out of sight.
        box(l,b,-16,-1,-30,16,-1,-20,mat("pavement"));box(l,b,-16,-1,-25,16,-1,-22,mat("road"));
        for(int x=-16;x<=16;x++){set(l,b,x,-1,-21,mat("coping"));set(l,b,x,-1,-26,mat("coping"));}
        box(l,b,-16,0,-30,16,25,-28,facade);
        box(l,b,-16,0,-27,-16,25,-20,facade);box(l,b,16,0,-27,16,25,-20,facade);
        // Leaning out over the street from the twentieth row.
        box(l,b,-16,20,-27,16,25,-21,facade);
        // Either side of the shop, tall, and leaning in over the roof from the eighteenth row.
        box(l,b,-16,-1,-19,-8,29,-1,facade);box(l,b,8,-1,-19,16,29,-1,facade);
        box(l,b,-16,18,-27,-3,29,-1,facade);box(l,b,3,18,-27,16,29,-1,facade);
        // Behind: over the House's corridor, and out over the roof from the eighteenth row.
        box(l,b,-16,-1,-6,16,29,-2,facade);box(l,b,-16,18,-10,16,29,-6,facade);
        // A lamp post on the near and the far pavement.
        for(int x:new int[]{-9,9}){box(l,b,x,0,-22+(x<0?0:-5),x,3,-22+(x<0?0:-5),Blocks.DARK_OAK_FENCE.defaultBlockState());set(l,b,x,4,-22+(x<0?0:-5),Blocks.LANTERN.defaultBlockState());}
    }
    /** Every face of the buildings round the yard and across the street that looks out on air gets its windows, one in four lit. */
    private static void windows(ServerLevel l,BlockPos b){
        var facade=LongerRegistry.material("facade");var rnd=new java.util.Random(6210);
        for(int y=1;y<=29;y++){if(y%3!=2)continue;
            for(int x=-16;x<=16;x++)for(int z=-30;z<=-1;z++){
                var at=b.offset(x,y,z);if(!l.getBlockState(at).is(facade))continue;
                boolean ns=l.getBlockState(at.north()).isAir()||l.getBlockState(at.south()).isAir(),ew=l.getBlockState(at.east()).isAir()||l.getBlockState(at.west()).isAir();
                if(!(ns&&Math.floorMod(x,3)==0||ew&&Math.floorMod(z,3)==0))continue;
                set(l,b,x,y,z,mat(rnd.nextInt(4)==0?"facade_lit":"facade_dark"));
            }}
    }

    /** The House's own corridor, three wide and six long, papered and carpeted the House's way; a light at its far end. */
    private static void corridor(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        box(l,b,-2,-1,-6,2,4,0,mat("corridor"));box(l,b,-1,0,-6,1,3,-1,air);
        box(l,b,-1,-1,-6,1,-1,-1,mat("runner"));box(l,b,-1,4,-6,1,4,-1,mat("ceiling"));
        set(l,b,0,3,-4,light(7));
    }

    private static void shop(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var brick=mat("brick");var plaster=mat("plaster");
        // The shell: brick outside, plaster within; the shopfront's painted timber across the front.
        box(l,b,-7,-1,-19,7,ROOF-1,-7,brick);box(l,b,-6,0,-18,6,3,-8,air);
        box(l,b,-6,0,-7,6,3,-7,plaster);box(l,b,-6,0,-19,6,3,-19,mat("shopfront"));
        for(int z=-18;z<=-8;z++)for(int y=0;y<=3;y++){set(l,b,-7,y,z,plaster);set(l,b,7,y,z,plaster);}
        box(l,b,-6,-1,-18,6,-1,-8,mat("boards"));box(l,b,-6,4,-18,6,4,-8,mat("ceiling"));
        box(l,b,-7,0,-19,-7,3,-19,brick);box(l,b,7,0,-19,7,3,-19,brick);
        // The shopfront: rain windows either side of the door, a fascia over them.
        for(int x=-5;x<=-2;x++)window(l,b,x,0,-19,Direction.SOUTH);for(int x=2;x<=5;x++)window(l,b,x,0,-19,Direction.SOUTH);
        door(l,b,FRONT_DOOR,Blocks.SPRUCE_DOOR,Direction.SOUTH,DoorHingeSide.LEFT);
        door(l,b,BACK_DOOR,Blocks.SPRUCE_DOOR,Direction.NORTH,DoorHingeSide.LEFT);
        // The workbench, and what is on it and over it.
        for(int i=0;i<3;i++)set(l,b,BENCH_PARTS.get(i),fit(WORKBENCH,Direction.EAST,i));
        set(l,b,LAMP,fit(LongerBlock.Kind.LAMP,Direction.EAST,1));set(l,b,RADIO,fit(LongerBlock.Kind.RADIO,Direction.EAST,2));set(l,b,TRAYS,fit(LongerBlock.Kind.TRAYS,Direction.EAST));
        set(l,b,ORDER,fit(LongerBlock.Kind.ORDER,Direction.EAST));set(l,b,CHAIR,fit(LongerBlock.Kind.CHAIR,Direction.WEST));
        for(int i=0;i<SHELVES.size();i++)set(l,b,SHELVES.get(i),fit(SHELF,Direction.WEST,i%3));
        set(l,b,6,2,-12,fit(SHELF,Direction.WEST,1));
        for(var at:COUNTER)set(l,b,at,fit(LongerBlock.Kind.COUNTER,Direction.NORTH));
        // The stair up the back wall, rising east, its underside boarded and a rail on its open side.
        for(int i=0;i<6;i++){int x=1+i;for(int z=-9;z<=-8;z++){set(l,b,x,i,z,stair(Blocks.DARK_OAK_STAIRS,Direction.EAST));for(int u=0;u<i;u++)set(l,b,x,u,z,plaster);}
            if(i<3)set(l,b,x,i+1,-10,Blocks.DARK_OAK_FENCE.defaultBlockState());}
        // The well through the shop's ceiling over the treads; the top tread is level with the flat's floor.
        box(l,b,1,4,-9,4,4,-8,air);
        set(l,b,0,3,-13,light(9));set(l,b,-3,3,-16,light(6));
    }

    private static void flat(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var paper=mat("paper");
        box(l,b,-6,UP,-18,6,UP+3,-8,air);
        for(int z=-19;z<=-7;z++)for(int y=UP;y<=UP+3;y++){set(l,b,-7,y,z,paper);set(l,b,7,y,z,paper);}
        box(l,b,-6,UP,-7,6,UP+3,-7,paper);box(l,b,-6,UP,-19,6,UP+3,-19,paper);
        box(l,b,-6,UP-1,-18,6,UP-1,-8,mat("flatboards"));box(l,b,-6,UP+4,-18,6,UP+4,-8,mat("ceiling"));
        box(l,b,1,UP-1,-9,5,UP-1,-8,air);
        // The top tread is in the floor's own plane; the floor laid over it puts it back.
        for(int z=-9;z<=-8;z++)set(l,b,6,UP-1,z,stair(Blocks.DARK_OAK_STAIRS,Direction.EAST));
        // The stairhead's rail round the well, open at its east end, where the top tread meets the floor between the rail and the worktop.
        for(int x=0;x<=4;x++)set(l,b,x,UP,-10,Blocks.DARK_OAK_FENCE.defaultBlockState());for(int z=-9;z<=-8;z++)set(l,b,0,UP,z,Blocks.DARK_OAK_FENCE.defaultBlockState());
        // The front window over the table, rain on it.
        for(int x=-2;x<=3;x++)window(l,b,x,UP+1,-19,Direction.SOUTH);
        // The kitchen.
        set(l,b,HOOK,fit(LongerBlock.Kind.HOOK,Direction.WEST,0));
        for(var at:WORKTOP)set(l,b,at,fit(LongerBlock.Kind.WORKTOP,Direction.WEST,at.equals(SINK)?1:0));
        set(l,b,KETTLE,fit(LongerBlock.Kind.KETTLE,Direction.WEST));set(l,b,TOASTER,fit(LongerBlock.Kind.TOASTER,Direction.WEST));set(l,b,FLAT_RADIO,fit(LongerBlock.Kind.RADIO,Direction.WEST));
        set(l,b,TABLE,fit(LongerBlock.Kind.TABLE,Direction.NORTH));set(l,b,CUPS,fit(LongerBlock.Kind.CUPS,Direction.NORTH));
        set(l,b,ARMCHAIR,fit(LongerBlock.Kind.ARMCHAIR,Direction.EAST));set(l,b,KITCHEN_CHAIR,fit(LongerBlock.Kind.KITCHEN_CHAIR,Direction.WEST));
        // The bathroom: tiled within, the partition papered on the flat's side.
        var tile=mat("tile");
        box(l,b,-6,UP-1,-11,-3,UP-1,-8,tile);
        for(int y=UP;y<=UP+3;y++){for(int z=-12;z<=-8;z++)set(l,b,-2,y,z,paper);for(int x=-6;x<=-3;x++)set(l,b,x,y,-12,paper);}
        door(l,b,BATH_DOOR,Blocks.BIRCH_DOOR,Direction.EAST,DoorHingeSide.RIGHT);
        set(l,b,BASIN,fit(LongerBlock.Kind.BASIN,Direction.EAST));set(l,b,LAVATORY,fit(LongerBlock.Kind.LAVATORY,Direction.EAST));
        set(l,b,-4,UP+3,-10,light(7));
        // The stair to the roof up the west wall, rising toward the back through the ceiling and the roof's felt, boxed in on its open side by a
        // papered partition, open at its foot.
        for(int i=0;i<6;i++){int z=-17+i;for(int x=-6;x<=-5;x++){set(l,b,x,UP+i,z,stair(Blocks.DARK_OAK_STAIRS,Direction.SOUTH));for(int u=0;u<i;u++)set(l,b,x,UP+u,z,paper);}}
        box(l,b,-4,UP,-16,-4,UP+3,-12,paper);
        box(l,b,-6,UP+4,-17,-5,UP+4,-14,air);
        set(l,b,3,UP+3,-14,light(9));
    }

    private static void roof(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var brick=mat("brick");var coping=mat("coping");
        box(l,b,-7,ROOF-1,-19,7,ROOF-1,-7,mat("felt"));
        box(l,b,-6,ROOF,-18,6,ROOF+6,-8,air);
        // The parapet: two courses of brick and the coping on top, too high to step over.
        for(int x=-7;x<=7;x++){box(l,b,x,ROOF,-19,x,ROOF+1,-19,brick);set(l,b,x,ROOF+2,-19,coping);box(l,b,x,ROOF,-7,x,ROOF+1,-7,brick);set(l,b,x,ROOF+2,-7,coping);}
        for(int z=-19;z<=-7;z++){box(l,b,-7,ROOF,z,-7,ROOF+1,z,brick);set(l,b,-7,ROOF+2,z,coping);box(l,b,7,ROOF,z,7,ROOF+1,z,brick);set(l,b,7,ROOF+2,z,coping);}
        // The stair house over the roof stair: its walls, its own little roof, its door east onto the felt; the opening through the
        // roof over the lower treads.
        box(l,b,-7,ROOF,-18,-4,ROOF+3,-10,brick);box(l,b,-6,ROOF,-17,-5,ROOF+2,-11,air);
        box(l,b,-7,ROOF+3,-18,-4,ROOF+3,-10,mat("felt"));
        box(l,b,-6,ROOF-1,-17,-5,ROOF-1,-13,air);box(l,b,-6,ROOF-1,-11,-5,ROOF-1,-11,mat("flatboards"));
        // The roof stair's top tread is in the felt's plane; put it back over the felt.
        for(int x=-6;x<=-5;x++)set(l,b,x,ROOF-1,-12,stair(Blocks.DARK_OAK_STAIRS,Direction.SOUTH));
        door(l,b,ROOF_DOOR,Blocks.SPRUCE_DOOR,Direction.EAST,DoorHingeSide.LEFT);
        set(l,b,-5,ROOF+2,-14,light(5));
        // The awning in the north-east corner over the benches, resting on the coping behind and two posts in front.
        for(int x=2;x<=6;x++)for(int z=-11;z<=-8;z++)set(l,b,x,ROOF+3,z,fit(AWNING,Direction.NORTH));
        for(int y=ROOF;y<=ROOF+2;y++){set(l,b,2,y,-11,Blocks.DARK_OAK_FENCE.defaultBlockState());set(l,b,6,y,-11,Blocks.DARK_OAK_FENCE.defaultBlockState());}
        set(l,b,BENCH,fit(LongerBlock.Kind.BENCH,Direction.SOUTH));set(l,b,5,ROOF,-9,fit(LongerBlock.Kind.BENCH,Direction.SOUTH));
        // A water tank, a chimney stack.
        set(l,b,TANK,fit(LongerBlock.Kind.TANK,Direction.NORTH));
        box(l,b,5,ROOF,-17,6,ROOF+3,-16,brick);
    }

    // ------------------------------------------------------------------------------------------------ for the cases
    /** The rain windows' cells, each pane's lower half. */
    public static List<BlockPos> windows(){
        var out=new java.util.ArrayList<BlockPos>();
        for(int x=-5;x<=-2;x++)out.add(new BlockPos(x,0,-19));for(int x=2;x<=5;x++)out.add(new BlockPos(x,0,-19));
        for(int x=-2;x<=3;x++)out.add(new BlockPos(x,UP+1,-19));
        return out;
    }
}
