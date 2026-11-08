package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.390, the new ordinary halls of docs/HALLS_TRIALS_AND_TIME_DESIGN.md ("New hallway variations (ordinary, no hazard)"): halls
 * with their own look and one small thing to them, built whole, dealt among the ordinary halls, never a trial or a story.
 *
 * <p>The laundry corridor (docs/LAUNDRY_CORRIDOR_0_4_390.md): washing hung on lines across a long hall, so it is crossed through
 * the sheets, which swing behind whoever parts them (HallSheetBlock). Two of its three ways on are behind the washing: one past a
 * full line, and one behind sheets hung along the wall.
 */
public final class OrdinaryHalls {
    private OrdinaryHalls(){}
    private static final int FLAGS=LabyrinthBuilder.flags();
    public static boolean is(LabyrinthPlace p){return p==LabyrinthPlace.LAUNDRY_HALL;}
    /** The halls dealt only once the halls have turned to stone (none yet). */
    public static boolean stone(LabyrinthPlace p){return false;}
    /** The dealer's weight: domestic halls until the halls turn to stone, as often as the vaulted hall. */
    public static int weight(LabyrinthPlace p,int depth){
        if(p==LabyrinthPlace.LAUNDRY_HALL)return depth>=LabyrinthPacing.STONE_DEPTH?0:8;
        return 0;
    }
    public static void build(ServerLevel level,BlockPos base,LabyrinthPlace place){
        if(place==LabyrinthPlace.LAUNDRY_HALL)buildLaundry(level,base);
        LabyrinthBuilder.doors(level,base,place);
    }

    // ------------------------------------------------------------------
    // The laundry corridor

    /** Half the corridor's width, inside its walls, and its far wall. */
    public static final int LAUNDRY_HALF=2,LAUNDRY_FAR=-42;
    /** The lines strung across the corridor, from wall to wall. */
    public static final int[] LINES={-6,-12,-18,-24,-30,-36};
    /** What hangs on each line across, west to east. */
    private static final HallSheetBlock.Kind L=HallSheetBlock.Kind.LINEN,T=HallSheetBlock.Kind.TICKING,W=HallSheetBlock.Kind.TOWEL,B=HallSheetBlock.Kind.BARE;
    public static final List<List<HallSheetBlock.Kind>> WASHING=List.of(
            List.of(L,W,B,T,L),
            List.of(L,L,T,W,L),
            List.of(T,L,W,L,T),
            List.of(L,T,L,T,L),     // a full line: the east door is behind it
            List.of(W,L,B,L,T),
            List.of(L,T,L,L,T));    // a full line before the far door
    /** The line along the west wall, between the second and third lines across, hiding the west door. */
    public static final int WALL_LINE_X=-2;
    public static final List<HallSheetBlock.Kind> WALL_WASHING=List.of(B,L,L,T,B);
    public static final int WALL_LINE_NEAR=-13;
    /** The doors behind the washing, and the lamps, one between each pair of lines. */
    public static final BlockPos WEST_DOOR=new BlockPos(-3,0,-15),EAST_DOOR=new BlockPos(3,0,-28);
    public static final int[] LAMPS={-3,-9,-15,-21,-27,-33,-39};
    public static final List<BlockPos> BASKETS=List.of(new BlockPos(2,0,-3),new BlockPos(-2,0,-21),new BlockPos(2,0,-39));
    /** The stool kept to reach the lines, against the wall under the last of them. */
    public static final BlockPos STOOL=new BlockPos(-2,0,-34);

    private static BlockState mat(String name){return OrdinaryHallRegistry.material(name).defaultBlockState();}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,FLAGS);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){BuildBlocks.box(l,b.offset(x0,y0,z0),b.offset(x1,y1,z1),s,FLAGS);}

    private static void buildLaundry(ServerLevel l,BlockPos b){
        var wall=mat("laundry_wall");var low=mat("laundry_wall_low");var floor=mat("laundry_floor");var ceiling=mat("laundry_ceiling");
        box(l,b,-4,-1,LAUNDRY_FAR,4,4,0,wall);
        box(l,b,-LAUNDRY_HALF,0,LAUNDRY_FAR+1,LAUNDRY_HALF,3,-1,Blocks.AIR.defaultBlockState());
        box(l,b,-LAUNDRY_HALF,-1,LAUNDRY_FAR+1,LAUNDRY_HALF,-1,-1,floor);
        box(l,b,-LAUNDRY_HALF,4,LAUNDRY_FAR+1,LAUNDRY_HALF,4,-1,ceiling);
        // The tarred course at the foot of every wall, where the wet gets in.
        box(l,b,-3,0,LAUNDRY_FAR,-3,0,0,low);box(l,b,3,0,LAUNDRY_FAR,3,0,0,low);box(l,b,-3,0,LAUNDRY_FAR,3,0,LAUNDRY_FAR,low);
        for(int i=0;i<LINES.length;i++)for(int x=-LAUNDRY_HALF;x<=LAUNDRY_HALF;x++)hang(l,b,x,LINES[i],WASHING.get(i).get(x+LAUNDRY_HALF),Direction.Axis.X);
        for(int i=0;i<WALL_WASHING.size();i++)hang(l,b,WALL_LINE_X,WALL_LINE_NEAR-i,WALL_WASHING.get(i),Direction.Axis.Z);
        for(int z:LAMPS)set(l,b,0,3,z,TrialBlock.of(TrialBlock.Kind.STILL_LAMP,1));
        for(var at:BASKETS)set(l,b,at.getX(),at.getY(),at.getZ(),OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.BASKET,at.getX()>0?Direction.WEST:Direction.EAST));
        set(l,b,STOOL.getX(),STOOL.getY(),STOOL.getZ(),OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.STOOL,Direction.EAST));
        LabyrinthBuilder.entrance(l,b,wall,floor,ceiling);
    }
    private static void hang(ServerLevel l,BlockPos b,int x,int z,HallSheetBlock.Kind kind,Direction.Axis axis){
        set(l,b,x,0,z,HallSheetBlock.of(kind,axis,false));set(l,b,x,1,z,HallSheetBlock.of(kind,axis,true));
    }
}
