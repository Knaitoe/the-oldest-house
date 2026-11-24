package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.754, the cold room (docs/COLD_ROOM_0_4_754.md): a rented house on a steep street in winter, built whole in its interior slot.
 * North from the House's door: the front hall with the table and the coats, the stair up its east side, the kitchen through the hall's
 * west wall; up the stair, the landing, and at its end the back bedroom: the iron bed, the chest of drawers with the book on it and the
 * cross over it, the bedside tables, the radiator under the window. Through the window, the long flight of steps outside, falling
 * north down the hill between two walls to the street, and the lamp at the bottom.
 *
 * <p>All positions are relative to the place's base, the floor under the entry door; north is negative z.
 */
public final class ColdSite {
    private ColdSite(){}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The whole place: the house and, north of it, the steps. */
    public static final int WEST=-10,EAST=6,NORTH=-45,TOP=11,BOTTOM=-12;
    /** The house's own shell, brick; everything else in the bounds is the night outside. */
    public static final int HOUSE_WEST=-10,HOUSE_EAST=4,HOUSE_NORTH=-22,HOUSE_TOP=9;
    /** An area of the house: its air from (x0,y0,z0) to (x1,y1,z1), standing on y0. */
    public record Area(String name,int x0,int y0,int z0,int x1,int y1,int z1){
        public boolean contains(Vec3 r){return r.x>=x0&&r.x<x1+1&&r.z>=z0&&r.z<z1+1&&r.y>y0-1&&r.y<y1+1;}
        boolean holds(int x,int y,int z){return x>=x0&&x<=x1&&y>=y0&&y<=y1&&z>=z0&&z<=z1;}
    }
    /** The hall in front of the door, the passage beside the stair to the kitchen, the stair's well, the kitchen. */
    public static final Area HALL=new Area("hall",-2,0,-3,1,2,-1),PASSAGE=new Area("hall",-2,0,-9,-1,2,-3),
        WELL=new Area("stair",0,0,-8,1,7,-4),KITCHEN=new Area("kitchen",-8,0,-9,-4,2,-3);
    /** Up the stair: the landing, then the back bedroom. */
    public static final Area LANDING=new Area("landing",-2,5,-12,1,7,-9),BEDROOM=new Area("bedroom",-4,5,-21,3,7,-14);
    public static final List<Area> AREAS=List.of(HALL,PASSAGE,WELL,KITCHEN,LANDING,BEDROOM);
    public static boolean inside(Vec3 r){for(var a:AREAS)if(a.contains(r))return true;return false;}
    public static boolean inBedroom(Vec3 r){return BEDROOM.contains(r);}
    /** The openings between areas, a block thick: the kitchen door off the passage, the bedroom door off the landing. */
    static final int[][] DOORWAYS={{-3,0,-6,-3,1,-6},{-1,5,-13,-1,6,-13}};
    /** The window over the radiator: two panes wide, two high, in the bedroom's north wall. */
    static final int[] WINDOW={-1,6,-22,0,7,-22};
    /** How many treads the stair has; the n-th (from nought) stands at y n, z -4-n, over the well's two columns. */
    public static final int TREADS=5;

    /** The narrow table by the door, with the letter on it (the source); the coats on their hooks opposite. */
    public static final BlockPos HALL_TABLE=new BlockPos(-2,0,-2),HOOKS=new BlockPos(1,1,-1);
    /** The kitchen table, the mother's chair at its west end, the other chair, the stove and the sink. */
    public static final BlockPos KITCHEN_TABLE=new BlockPos(-6,0,-6),MOTHER_CHAIR=new BlockPos(-7,0,-6),OTHER_CHAIR=new BlockPos(-5,0,-6),
        STOVE=new BlockPos(-8,0,-4),SINK=new BlockPos(-8,0,-8);
    /** The bed against the east wall, its head (the pillow) and foot; the head's facing points to the foot. */
    public static final BlockPos BED_HEAD=new BlockPos(3,5,-17),BED_FOOT=new BlockPos(2,5,-17);
    public static final Direction BED_FACING=Direction.WEST;
    /** The bedside tables either side of the pillow: the lamp's, south, and the flask's, north. */
    public static final BlockPos LAMP_TABLE=new BlockPos(3,5,-15),FLASK_TABLE=new BlockPos(3,5,-19);
    /** The cross on the east wall over the pillow. */
    public static final BlockPos CRUCIFIX=new BlockPos(3,7,-17);
    /** The chest of drawers against the west wall, and the book on it (the rite, part 3). */
    public static final BlockPos DRESSER=new BlockPos(-4,5,-17),BOOK=new BlockPos(-4,6,-17);
    /** The radiator's two lengths under the window; the chair by the bed. */
    public static final BlockPos[] RADIATOR={new BlockPos(-1,5,-21),new BlockPos(0,5,-21)};
    public static final BlockPos CHAIR=new BlockPos(0,5,-15);
    /** The bulbs on their flexes: the hall's, the kitchen's and the landing's. */
    public static final BlockPos[] BULBS={new BlockPos(-1,2,-2),new BlockPos(-6,2,-6),new BlockPos(-1,7,-11)};
    /** Where the three keep: the mother at the kitchen table, the girl in the bed (her head on the pillow), the old priest by the window. */
    public static final BlockPos MOTHER=MOTHER_CHAIR,GIRL=BED_HEAD,PRIEST=new BlockPos(-2,5,-20);
    /** The account (part 4) waits on the hall table beside the letter: the letter's own cell holds both. */
    public static final BlockPos SOURCE=HALL_TABLE;
    /** The way on: a door in the kitchen's north wall, the back way out. */
    public static final BlockPos ONWARD=new BlockPos(-6,0,-10);

    // ------------------------------------------------------------------------------------------------ outside
    /** The steps: the first tread under the window's wall, at y nought, one down a block north, to the last at y -9 (0.4.757: ten, so the whole place, sky to the steps' footing, keeps within its slot's 24 rows). */
    public static final int STEP_X0=-2,STEP_X1=1,STEP_TOP_Z=-24,STEPS=10;
    /** The landing at the foot of the steps, its floor's top at y -11 (standing at -10), and the lamp beside it. */
    public static final int FOOT_Z0=-39,FOOT_Z1=-43,FOOT_Y=-10;
    public static final BlockPos STREETLAMP=new BlockPos(3,FOOT_Y,-40);
    /** Where a reader comes to at the foot of the steps (part 4). */
    public static final BlockPos FOOT=new BlockPos(0,FOOT_Y,-41);
    /** The standing height of the steps at a z: the top of the tread there, or of the landing below them. */
    public static int ground(int z){
        int k=STEP_TOP_Z-z;
        if(k<0)return 0;
        if(k<STEPS)return -k;
        return FOOT_Y;
    }

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    public static BlockState mat(String name){return ColdRegistry.material(name).defaultBlockState();}
    static BlockState fit(ColdBlock.Kind kind,Direction facing){return ColdBlock.of(kind,facing,0);}
    static BlockState fit(ColdBlock.Kind kind,Direction facing,int stage){return ColdBlock.of(kind,facing,stage);}
    private static final BlockState AIR=Blocks.AIR.defaultBlockState(),NIGHT=Blocks.BLACK_CONCRETE.defaultBlockState();
    private static final Direction N=Direction.NORTH,S=Direction.SOUTH,E=Direction.EAST,W=Direction.WEST;

    public static void build(ServerLevel l,BlockPos b){
        ColdNight.forget(b);
        box(l,b,WEST,BOTTOM,NORTH,EAST,TOP,0,NIGHT);
        box(l,b,HOUSE_WEST,-1,HOUSE_NORTH,HOUSE_EAST,HOUSE_TOP,0,mat("brick"));
        for(var a:AREAS)box(l,b,a.x0(),a.y0(),a.z0(),a.x1(),a.y1(),a.z1(),AIR);
        for(var d:DOORWAYS)box(l,b,d[0],d[1],d[2],d[3],d[4],d[5],AIR);
        var painted=new HashSet<BlockPos>();
        for(var a:AREAS)face(l,b,a,painted);
        stair(l,b);hall(l,b);kitchen(l,b);landing(l,b);bedroom(l,b);outside(l,b);
    }
    /** Whether a cell is open by the plan; the build is worked out from the plan, never read back from the world. */
    static boolean open(int x,int y,int z){
        for(var a:AREAS)if(a.holds(x,y,z))return true;
        for(var d:DOORWAYS)if(x>=d[0]&&x<=d[3]&&y>=d[1]&&y<=d[4]&&z>=d[2]&&z<=d[5])return true;
        return false;
    }
    /**
     * Each area's own surfaces where its air meets the brick: faded paper on every wall, plaster overhead; the hall's and passage's
     * boards, the kitchen's linoleum, the runner on the landing and in the bedroom the same boards as the hall.
     */
    static void face(ServerLevel l,BlockPos b,Area a,Set<BlockPos> painted){
        var floor=switch(a.name()){case "kitchen"->mat("lino");case "landing"->mat("runner");default->mat("boards");};
        for(int x=a.x0()-1;x<=a.x1()+1;x++)for(int z=a.z0()-1;z<=a.z1()+1;z++){
            boolean in=x>=a.x0()&&x<=a.x1()&&z>=a.z0()&&z<=a.z1();
            if(in){paint(l,b,painted,x,a.y0()-1,z,floor);paint(l,b,painted,x,a.y1()+1,z,mat("plaster"));continue;}
            for(int y=a.y0();y<=a.y1();y++)paint(l,b,painted,x,y,z,mat("paper"));
        }
    }
    private static void paint(ServerLevel l,BlockPos b,Set<BlockPos> painted,int x,int y,int z,BlockState s){if(!open(x,y,z)&&painted.add(new BlockPos(x,y,z)))set(l,b,x,y,z,s);}

    /** The stair: five treads up the well, the boards filled in beneath each, the runner up the middle of the treads. */
    static void stair(ServerLevel l,BlockPos b){
        for(int i=0;i<TREADS;i++){
            int z=-4-i;
            for(int x=WELL.x0();x<=WELL.x1();x++){
                if(i>0)box(l,b,x,0,z,x,i-1,z,mat("boards"));
                set(l,b,x,i,z,ColdRegistry.TREADS.get().defaultBlockState().setValue(StairBlock.FACING,N));
            }
        }
        // Over the top tread the landing's floor carries on; the well is open above the treads.
        for(int x=WELL.x0();x<=WELL.x1();x++)set(l,b,x,4,-9,mat("runner"));
    }
    /** The narrow table by the door with the letter on it; the coats on their hooks on the east wall; the hall's bulb. */
    static void hall(ServerLevel l,BlockPos b){
        set(l,b,HALL_TABLE,fit(ColdBlock.Kind.HALL_TABLE,E,1));
        set(l,b,HOOKS,fit(ColdBlock.Kind.HOOKS,W));
        set(l,b,BULBS[0],fit(ColdBlock.Kind.BULB,N));
    }
    /** The table, two chairs either end of it, the stove and the sink along the west wall, the bulb. */
    static void kitchen(ServerLevel l,BlockPos b){
        set(l,b,KITCHEN_TABLE,fit(ColdBlock.Kind.KITCHEN_TABLE,N));
        set(l,b,MOTHER_CHAIR,fit(ColdBlock.Kind.CHAIR,E));set(l,b,OTHER_CHAIR,fit(ColdBlock.Kind.CHAIR,W));
        set(l,b,STOVE,fit(ColdBlock.Kind.STOVE,E));set(l,b,SINK,fit(ColdBlock.Kind.SINK,E));
        set(l,b,BULBS[1],fit(ColdBlock.Kind.BULB,N));
    }
    /** Nothing on the landing but its runner and its bulb. */
    static void landing(ServerLevel l,BlockPos b){set(l,b,BULBS[2],fit(ColdBlock.Kind.BULB,N));}
    /**
     * The back bedroom: the bed against the east wall, a bedside table either side of the pillow (the lamp lit), the cross over the
     * pillow, the chest of drawers on the west wall with the book shut on it, the chair, the radiator and the window.
     */
    static void bedroom(ServerLevel l,BlockPos b){
        set(l,b,BED_HEAD,fit(ColdBlock.Kind.BED,BED_FACING,0));set(l,b,BED_FOOT,fit(ColdBlock.Kind.BED,BED_FACING,1));
        set(l,b,LAMP_TABLE,fit(ColdBlock.Kind.NIGHTSTAND,W,0));set(l,b,FLASK_TABLE,fit(ColdBlock.Kind.NIGHTSTAND,W,1));
        set(l,b,CRUCIFIX,fit(ColdBlock.Kind.CRUCIFIX,W,0));
        set(l,b,DRESSER,fit(ColdBlock.Kind.DRESSER,E,0));set(l,b,BOOK,fit(ColdBlock.Kind.BOOK,E,0));
        set(l,b,CHAIR,fit(ColdBlock.Kind.CHAIR,E));
        for(var at:RADIATOR)set(l,b,at,fit(ColdBlock.Kind.RADIATOR,S));
        box(l,b,WINDOW[0],WINDOW[1],WINDOW[2],WINDOW[3],WINDOW[4],WINDOW[5],Blocks.GLASS_PANE.defaultBlockState()
            .setValue(net.minecraft.world.level.block.CrossCollisionBlock.EAST,true).setValue(net.minecraft.world.level.block.CrossCollisionBlock.WEST,true));
    }
    /**
     * Outside, the night: the steps falling north from under the window between two rendered walls that rise two above them, the
     * banks behind the walls filled in, the landing at the foot and the street lamp lit beside it. Nothing out here is reached on foot
     * from the house.
     */
    static void outside(ServerLevel l,BlockPos b){
        box(l,b,WEST+1,BOTTOM+1,NORTH+1,EAST-1,TOP-1,HOUSE_NORTH-1,AIR);
        for(int z=HOUSE_NORTH-1;z>=NORTH+1;z--){
            int g=ground(z);boolean step=z<=STEP_TOP_Z&&z>STEP_TOP_Z-STEPS;
            // Under the way down: the flags' bed; under the walls and the banks, the earth they hold.
            box(l,b,STEP_X0,BOTTOM+1,z,STEP_X1,g-1,z,mat("render"));
            if(step)for(int x=STEP_X0;x<=STEP_X1;x++)set(l,b,x,g-1,z,ColdRegistry.STEPS.get().defaultBlockState().setValue(StairBlock.FACING,S));
            else box(l,b,STEP_X0,g-1,z,STEP_X1,g-1,z,mat("flags"));
            box(l,b,WEST+1,BOTTOM+1,z,STEP_X0-1,g+1,z,mat("render"));box(l,b,STEP_X1+1,BOTTOM+1,z,EAST-1,g+1,z,mat("render"));
        }
        // At the foot the walls open onto the street: the landing runs the width between them.
        box(l,b,WEST+1,FOOT_Y-1,FOOT_Z1,EAST-1,FOOT_Y-1,FOOT_Z0,mat("flags"));
        box(l,b,WEST+1,FOOT_Y,FOOT_Z1,EAST-1,TOP-1,FOOT_Z0,AIR);
        set(l,b,STREETLAMP,fit(ColdBlock.Kind.STREETLAMP,S,0));set(l,b,STREETLAMP.above(),fit(ColdBlock.Kind.STREETLAMP,S,0));set(l,b,STREETLAMP.above(2),fit(ColdBlock.Kind.STREETLAMP,S,1));
    }
}
