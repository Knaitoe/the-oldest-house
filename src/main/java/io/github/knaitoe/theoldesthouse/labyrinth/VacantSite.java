package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.VacantBlock.Kind.*;

/**
 * 0.4.228, Vacant Possession (docs/VACANT_POSSESSION_0_4_228.md): a small flat on the first floor of a terrace, built whole. The
 * reader comes in at the front (the base) onto the upper landing (x -5 to 2, z -1 to -3); the flat's door is straight ahead in its
 * double wall (z -4 and -5), the stair down on the left (x -5 to -4, treads z -1 to -5). The flat (x -2 to 7, z -6 to -17) is a
 * living room and, through the partition at z -12, a bedroom; its windows look east onto the street. Downstairs the hall (x -5 to 7,
 * z -6 to -8, floor at y -6) runs east to the front door at x 8, which stands open onto the pavement. The van is backed onto the kerb
 * with its back to the door (x 10 to 15, z -5 to -9): its load bay three by three behind a bulkhead, and its cab, lower, at the road
 * end (0.4.260, owner: "the van needs to be flipped around"; its back doors had windows and read as a cab at the house end, and the
 * load bay ran up to the windscreen). Every position is relative to the base.
 */
public final class VacantSite {
    private VacantSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.VACANT_POSSESSION;
    /** The ground floor's standing level (its floor is the row below). */
    public static final int GROUND=-5;
    /** A piece of the tenant's furniture, where it stands in the flat and which way it faces. */
    public record Piece(VacantBlock.Kind kind,BlockPos at,Direction facing,String name){
        /** The cells the piece fills where it stands: the bed is two long, the wardrobe two high. */
        public List<BlockPos> cells(){return switch(kind){case BED->List.of(at,at.relative(facing));case WARDROBE->List.of(at,at.above());default->List.of(at);};}
    }
    /** Everything on the list, in the order it is written. */
    public static final List<Piece> PIECES=List.of(
        new Piece(DRESSER,new BlockPos(-2,0,-9),Direction.EAST,"the dresser"),
        new Piece(RUG,new BlockPos(2,0,-8),Direction.NORTH,"the rug"),
        new Piece(BED,new BlockPos(2,0,-17),Direction.SOUTH,"the bed"),
        new Piece(WARDROBE,new BlockPos(6,0,-17),Direction.SOUTH,"the wardrobe"),
        new Piece(TABLE,new BlockPos(3,0,-11),Direction.SOUTH,"the table"),
        new Piece(ARMCHAIR,new BlockPos(5,0,-7),Direction.WEST,"the armchair"),
        new Piece(BOXES,new BlockPos(-2,0,-6),Direction.EAST,"the boxes"),
        new Piece(LAMP,new BlockPos(7,0,-11),Direction.WEST,"the lamp"));
    /** The last chair, by the window, facing it. */
    public static final Piece CHAIR_PIECE=new Piece(CHAIR,new BlockPos(6,0,-9),Direction.EAST,"the chair");
    @Nullable public static Piece piece(VacantBlock.Kind kind){if(kind==CHAIR)return CHAIR_PIECE;for(var p:PIECES)if(p.kind()==kind)return p;return null;}
    /** The piece whose cells include this one, where it stands in the flat. */
    @Nullable public static Piece pieceAt(BlockPos rel){for(var p:PIECES)if(p.cells().contains(rel))return p;return CHAIR_PIECE.cells().contains(rel)?CHAIR_PIECE:null;}
    /** The tenant's list, taped by the door inside the flat (the source); the removal docket on the van's floor (the account). */
    public static final BlockPos LIST=new BlockPos(1,1,-6),DOCKET=new BlockPos(10,GROUND,-6);
    /** Where the tenant waits, by the window; where they stand when they get up from the last chair. */
    public static final BlockPos TENANT_START=new BlockPos(6,0,-7),STAND=new BlockPos(6,0,-10);
    /** The bulbs: the landing, the living room, the bedroom, the downstairs hall. The flat's two go dark when it is empty. */
    public static final BlockPos LANDING_BULB=new BlockPos(0,3,-2),LIVING_BULB=new BlockPos(2,3,-8),BEDROOM_BULB=new BlockPos(2,3,-15),HALL_BULB=new BlockPos(1,GROUND+3,-7);
    /** The way on: the back door at the west end of the downstairs hall. */
    public static final int BACK_X=-6,BACK_Z=-7;
    /** The van's load bay, front (against the bulkhead) to back: where each piece goes, in the order it is set down. */
    public static final List<BlockPos> SLOTS=List.of(
        new BlockPos(13,GROUND,-8),new BlockPos(13,GROUND,-7),new BlockPos(13,GROUND,-6),
        new BlockPos(12,GROUND,-8),new BlockPos(12,GROUND,-7),new BlockPos(12,GROUND,-6),
        new BlockPos(11,GROUND,-8),new BlockPos(11,GROUND,-7),new BlockPos(11,GROUND,-6));
    /** The van's rear doors, swung right round against its sides so the pavement stays clear; its wheels, on the outside of its sides. */
    public static final List<BlockPos> VAN_DOORS=List.of(new BlockPos(10,GROUND,-4),new BlockPos(10,GROUND,-10));
    public static final List<BlockPos> WHEELS=List.of(new BlockPos(11,GROUND,-4),new BlockPos(15,GROUND,-4),new BlockPos(11,GROUND,-10),new BlockPos(15,GROUND,-10));
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    public static boolean inFlat(Vec3 r){return r.x>=-2&&r.x<8&&r.z>=-17&&r.z<-5&&r.y>=-.5&&r.y<4;}
    public static boolean onStreet(Vec3 r){return r.x>=9&&r.x<17&&r.z>=-18&&r.z<0&&r.y<GROUND+3;}
    public static boolean inside(Vec3 r){return r.x>=-6&&r.x<18&&r.z>=-18&&r.z<1&&r.y>=GROUND-1&&r.y<8;}
    /** The van: its panels, floor, roof, cab, wheels and the rear doors swung wide. */
    public static boolean van(BlockPos rel){
        int x=rel.getX(),y=rel.getY(),z=rel.getZ();
        for(var d:VAN_DOORS)if(x==d.getX()&&z==d.getZ()&&y>=GROUND&&y<=GROUND+1)return true;
        if(WHEELS.contains(rel))return true;
        return x>=10&&x<=15&&z>=-9&&z<=-5&&y>=GROUND-1&&y<=GROUND+3;
    }

    static BlockState mat(String name){return VacantRegistry.material(name).defaultBlockState();}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);}
    private static void door(ServerLevel l,BlockPos b,int x,int y,int z,Block door,Direction facing,boolean open){
        var s=door.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.OPEN,open).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT);
        set(l,b,x,y,z,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,x,y+1,z,s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var plaster=mat("plaster");var paper=mat("wallpaper");var brick=mat("brick");
        // The shell, and the linings of each room: plaster round the hall, the stair and the landing, wallpaper round the flat.
        box(l,b,-7,GROUND-2,-19,18,8,0,brick);
        box(l,b,-6,GROUND-1,-9,8,-1,-5,plaster);
        box(l,b,-6,GROUND-1,-7,-3,4,0,plaster);
        box(l,b,-6,-1,-4,3,4,0,plaster);
        box(l,b,-3,-1,-18,8,4,-5,paper);
        // Floors and ceilings: the hall's lino under its plaster ceiling, the landing's runner, the flat's boards (new under the rug).
        box(l,b,-5,GROUND-1,-8,7,GROUND-1,-6,mat("lino"));box(l,b,-5,-1,-8,7,-1,-6,plaster);
        box(l,b,-5,-1,-3,2,-1,-1,mat("stair_carpet"));box(l,b,-5,4,-3,2,4,-1,plaster);
        box(l,b,-2,-1,-17,7,-1,-6,mat("boards"));box(l,b,1,-1,-9,3,-1,-7,mat("boards_new"));box(l,b,-2,4,-17,7,4,-6,plaster);
        // The rooms.
        box(l,b,-5,GROUND,-8,7,GROUND+3,-6,air);
        box(l,b,-5,0,-3,2,3,-1,air);
        box(l,b,-2,0,-17,7,3,-6,air);
        // The stair well, and its treads going down north from the landing, solid beneath. Five treads for the five blocks between the
        // landing and the hall (0.4.240: there were four, and the top one sat a whole block under the landing, so the stair could be
        // walked down but not back up).
        box(l,b,-5,GROUND,-6,-4,3,-1,air);
        for(int i=0;i<5;i++){int z=-1-i,y=-1-i;
            for(int x=-5;x<=-4;x++){set(l,b,x,y,z,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));for(int under=GROUND;under<y;under++)set(l,b,x,under,z,plaster);}}
        // A rail along the landing's edge over the well.
        for(int z=-3;z<=-2;z++)set(l,b,-3,0,z,Blocks.SPRUCE_FENCE.defaultBlockState());
        // The partition between the living room and the bedroom, with its doorway.
        box(l,b,-2,0,-12,7,3,-12,paper);set(l,b,-1,0,-12,air);set(l,b,-1,1,-12,air);
        // The flat's door in its double wall, standing open; the reveal behind it.
        door(l,b,0,0,-4,Blocks.OAK_DOOR,Direction.NORTH,true);set(l,b,0,0,-5,air);set(l,b,0,1,-5,air);
        // The building's front, brick on the street, the front door open onto the pavement, the flat's windows above.
        box(l,b,8,GROUND-1,-18,8,7,0,brick);
        door(l,b,8,GROUND,-7,Blocks.OAK_DOOR,Direction.EAST,true);
        // 0.4.240 (the scene looked at in play): the living room's first window is straight over the van, seen from the pavement only
        // by looking up and from across the street not at all; a second window past the van's end shows the room lit, then dark.
        for(int y=1;y<=2;y++)for(int z:new int[]{-8,-9,-11,-14,-15})set(l,b,8,y,z,Blocks.GLASS_PANE.defaultBlockState());
        // The ground floor's other windows, onto rooms nobody is in.
        for(int y=GROUND+1;y<=GROUND+2;y++)for(int z:new int[]{-12,-15}){set(l,b,8,y,z,Blocks.GLASS_PANE.defaultBlockState());set(l,b,7,y,z,Blocks.BLACK_CONCRETE.defaultBlockState());}
        // The street: the pavement, the kerb, the road, the far pavement and the houses opposite, under the evening.
        box(l,b,9,GROUND,-17,16,7,-1,air);
        box(l,b,9,GROUND-1,-17,9,GROUND-1,-1,mat("pavement"));box(l,b,10,GROUND-1,-17,10,GROUND-1,-1,mat("kerb"));
        box(l,b,11,GROUND-1,-17,15,GROUND-1,-1,mat("road"));box(l,b,16,GROUND-1,-17,16,GROUND-1,-1,mat("pavement"));
        box(l,b,17,GROUND-1,-18,17,7,0,brick);box(l,b,9,8,-18,17,8,0,Blocks.BLACK_CONCRETE.defaultBlockState());
        for(int z:new int[]{-3,-4,-9,-10,-14,-15})for(int y:new int[]{GROUND+1,GROUND+2,1,2}){set(l,b,17,y,z,Blocks.GLASS_PANE.defaultBlockState());set(l,b,18,y,z,Blocks.BLACK_CONCRETE.defaultBlockState());}
        // Two street lamps on the near pavement.
        for(int z:new int[]{-3,-14}){for(int y=GROUND;y<=GROUND+2;y++)set(l,b,9,y,z,VacantBlock.of(STREETLAMP,Direction.EAST,0));set(l,b,9,GROUND+3,z,VacantBlock.of(STREETLAMP,Direction.EAST,1));}
        // The van, backed onto the kerb with its back to the front door. The box: floor, sides, roof, and the bulkhead behind the cab,
        // so what goes in stands in the load bay and nowhere else. The cab at the road end, a block lower: its bumper, its glass
        // round the front and both sides, its roof. The wheels outside the sides, the rear doors swung right round against them.
        var panel=mat("van_panel");
        box(l,b,10,GROUND-1,-9,15,GROUND-1,-5,mat("van_floor"));
        for(int z:new int[]{-5,-9})box(l,b,10,GROUND,z,14,GROUND+2,z,panel);
        box(l,b,10,GROUND+3,-9,14,GROUND+3,-5,panel);box(l,b,14,GROUND,-8,14,GROUND+2,-6,panel);
        box(l,b,15,GROUND,-9,15,GROUND,-5,panel);for(int z=-9;z<=-5;z++)set(l,b,15,GROUND+1,z,Blocks.GLASS_PANE.defaultBlockState());box(l,b,15,GROUND+2,-9,15,GROUND+2,-5,panel);
        for(var w:WHEELS)set(l,b,w,VacantBlock.of(VAN_WHEEL,w.getZ()>-7?Direction.SOUTH:Direction.NORTH));
        for(int y=0;y<=1;y++){set(l,b,10,GROUND+y,-4,VacantBlock.of(VAN_DOOR,Direction.SOUTH,y));set(l,b,10,GROUND+y,-10,VacantBlock.of(VAN_DOOR,Direction.NORTH,y));}
        set(l,b,DOCKET,VacantBlock.of(VacantBlock.Kind.DOCKET,Direction.WEST));
        // The downstairs hall: the mailboxes, the bulb.
        set(l,b,3,GROUND+1,-8,VacantBlock.of(MAILBOXES,Direction.SOUTH));
        // The flat: the list by the door and the coat on its hook, the radiator under the window, the bulbs.
        set(l,b,LIST,VacantBlock.of(VacantBlock.Kind.LIST,Direction.NORTH));set(l,b,2,1,-6,VacantBlock.of(HOOKS,Direction.NORTH));
        set(l,b,7,0,-8,VacantBlock.of(RADIATOR,Direction.WEST));
        for(var at:List.of(LANDING_BULB,LIVING_BULB,BEDROOM_BULB,HALL_BULB))set(l,b,at,VacantBlock.of(BULB,Direction.NORTH));
        // Everything on the list, and the chair.
        reset(l,b,false);
        BuildBlocks.after(l,()->{for(int y=1;y<=2;y++)for(int z:new int[]{-8,-9,-14,-15})join(l,b.offset(8,y,z));for(int z=-9;z<=-5;z++)join(l,b.offset(15,GROUND+1,z));
            for(int z=-3;z<=-2;z++){var at=b.offset(-3,0,z);var s=l.getBlockState(at);for(var d:Direction.Plane.HORIZONTAL)s=s.updateShape(d,l.getBlockState(at.relative(d)),l,at,at.relative(d));l.setBlock(at,s,F);}});
    }
    private static void join(ServerLevel l,BlockPos at){var s=l.getBlockState(at);if(!(s.getBlock() instanceof IronBarsBlock))return;for(var d:Direction.Plane.HORIZONTAL)s=s.updateShape(d,l.getBlockState(at.relative(d)),l,at,at.relative(d));l.setBlock(at,s,F);}

    /** Every piece back where it stands, what they hid covered again, the van empty and the lights on. */
    public static void reset(ServerLevel l,BlockPos b,boolean live){
        for(var p:PIECES)place(l,b,p,live);place(l,b,CHAIR_PIECE,live);
        for(var at:SLOTS){put(l,b,at,Blocks.AIR.defaultBlockState(),live);put(l,b,at.above(),Blocks.AIR.defaultBlockState(),live);}
        lights(l,b,true,live);
    }
    private static void put(ServerLevel l,BlockPos b,BlockPos at,BlockState s,boolean live){if(live){if(!l.getBlockState(b.offset(at)).equals(s))l.setBlock(b.offset(at),s,F);}else set(l,b,at,s);}
    /** A piece set down where it stands. The dresser's marks and the wardrobe's square go back behind it. */
    public static void place(ServerLevel l,BlockPos b,Piece p,boolean live){
        var c=p.cells();
        switch(p.kind()){
            case BED->{put(l,b,c.get(0),VacantBlock.of(BED,p.facing(),0),live);put(l,b,c.get(1),VacantBlock.of(BED,p.facing(),1),live);}
            case WARDROBE->{put(l,b,c.get(0),VacantBlock.of(WARDROBE,p.facing(),0),live);put(l,b,c.get(1),VacantBlock.of(WARDROBE,p.facing(),1),live);}
            case DRESSER->{put(l,b,p.at(),VacantBlock.of(DRESSER,p.facing()),live);put(l,b,p.at().above(),Blocks.AIR.defaultBlockState(),live);}
            default->put(l,b,p.at(),VacantBlock.of(p.kind(),p.facing()),live);
        }
    }
    /** A piece lifted: its cells empty, and what it was hiding there to see. */
    public static void lift(ServerLevel l,BlockPos b,Piece p){
        var c=p.cells();
        switch(p.kind()){
            case DRESSER->{put(l,b,p.at(),VacantBlock.of(MARKS,p.facing(),0),true);put(l,b,p.at().above(),VacantBlock.of(MARKS,p.facing(),1),true);}
            case BED->{for(var at:c)put(l,b,at,VacantBlock.of(SCRATCHES,p.facing()),true);}
            case WARDROBE->{put(l,b,c.get(0),VacantBlock.of(SQUARE,p.facing(),0),true);put(l,b,c.get(1),VacantBlock.of(SQUARE,p.facing(),1),true);}
            default->{for(var at:c)put(l,b,at,Blocks.AIR.defaultBlockState(),true);}
        }
    }
    /** A piece in the van, at its slot: the bed stood on end, the rug rolled, the lamp unplugged. */
    public static void stow(ServerLevel l,BlockPos b,VacantBlock.Kind kind,int slot){
        var at=SLOTS.get(slot);var facing=Direction.WEST;
        switch(kind){
            case BED->put(l,b,at,VacantBlock.of(BED,facing,2),true);
            case RUG->put(l,b,at,VacantBlock.of(RUG,facing,1),true);
            case LAMP->put(l,b,at,VacantBlock.of(LAMP,facing,1),true);
            case WARDROBE->{put(l,b,at,VacantBlock.of(WARDROBE,facing,0),true);put(l,b,at.above(),VacantBlock.of(WARDROBE,facing,1),true);}
            default->put(l,b,at,VacantBlock.of(kind,facing),true);
        }
    }
    /** The flat's two bulbs, on or off; the landing and the hall stay lit. */
    public static void lights(ServerLevel l,BlockPos b,boolean on,boolean live){
        for(var at:List.of(LIVING_BULB,BEDROOM_BULB)){var s=VacantBlock.of(BULB,Direction.NORTH,on?0:1);put(l,b,at,s,live);}
    }
}
