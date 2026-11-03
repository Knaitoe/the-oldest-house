package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/**
 * 0.4.283, Second Person's street as a British terrace (docs/SECOND_PERSON_TERRACE_0_4_283.md). The street was a box: the terrace the
 * reader comes out of one flat brick wall, both ends of the road blank brick, bare stone either side of the launderette, the street tree
 * in a plain square of earth. Now:
 * <ul>
 * <li>the terrace behind the reader is a row of houses: a slate eave along its top, chimney stacks with their pots, a stone head over
 * each window and brick between the floors;</li>
 * <li>across the road the launderette's parade stands in its own row, a two-storey house each side of it with a slate gable roof and
 * chimneys, and back yards behind them (walled, a bin in each) under the flat's west windows;</li>
 * <li>both ends of the road meet the front of a house across a T-junction, a kerb before it;</li>
 * <li>the launderette's flat roof keeps its parapet, with two chimney stacks above it;</li>
 * <li>the street tree has an iron grille round its foot.</li>
 * </ul>
 * Everything stays inside the scene's room (y up to 12), so the near terrace and the end houses, whose roofs would slope away outside
 * it, show their eaves and chimneys. No story position, door, window or walking surface of the street or the flat changes.
 */
final class SecondPersonStreet {
    private SecondPersonStreet(){}

    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){SecondPersonSite.set(l,b,x,y,z,s);}
    static BlockState stair(Direction facing){return Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,facing);}
    static BlockState tiles(){return Blocks.DEEPSLATE_TILES.defaultBlockState();}
    static BlockState brick(){return SecondPersonSite.mat("brick");}
    static BlockState stone(){return Blocks.SMOOTH_STONE.defaultBlockState();}
    static BlockState glass(){return Blocks.BLACK_STAINED_GLASS.defaultBlockState();}
    static BlockState pot(){return Blocks.FLOWER_POT.defaultBlockState();}
    static void door(ServerLevel l,BlockPos b,int x,int z,Direction facing){
        var s=Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,false);
        set(l,b,x,0,z,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,x,1,z,s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }

    static void dress(ServerLevel l,BlockPos b){
        nearTerrace(l,b);
        neighbour(l,b,-16,-10,-13,new int[]{-15,-11},new int[]{-15,-14,-12,-11});
        neighbour(l,b,9,16,12,new int[]{10,14,15},new int[]{10,11,14,15});
        yard(l,b,-16,-10,-13);yard(l,b,9,16,12);
        launderetteChimneys(l,b);
        roadEnd(l,b,-17,Direction.EAST);roadEnd(l,b,17,Direction.WEST);
        backs(l,b,-17,Direction.EAST);backs(l,b,17,Direction.WEST);
        treeGrille(l,b);
    }

    /**
     * The terrace the reader comes out of (z 0, facing north): a stone head over every window and door,
     * a slate eave along the top in place of the parapet, and a chimney stack at each party wall with two pots.
     */
    static void nearTerrace(ServerLevel l,BlockPos b){
        // A stone head over each window, and brick between the floors: the first-floor windows are two panes high.
        int[] windows={-15,-14,-9,-8,-3,-2,2,3,8,9,14,15};
        for(int x:windows){set(l,b,x,3,0,stone());set(l,b,x,5,0,brick());set(l,b,x,8,0,stone());}
        for(int x=-16;x<=16;x+=4)set(l,b,x,4,0,brick());
        for(int x:new int[]{-12,6,12})set(l,b,x,3,0,stone());
        for(int x=-17;x<=17;x++)set(l,b,x,10,0,stair(Direction.SOUTH));
        for(int x:new int[]{-17,-11,-6,0,5,11,16})chimney(l,b,x,10,0);
    }
    /** A chimney stack two wide at a party wall: brick for two courses and a pot on each flue. */
    static void chimney(ServerLevel l,BlockPos b,int x,int y,int z){
        for(int dx=0;dx<=1;dx++){if(x+dx>17)continue;set(l,b,x+dx,y,z,brick());set(l,b,x+dx,y+1,z,brick());if(y+2<=12)set(l,b,x+dx,y+2,z,pot());}
    }

    /**
     * A two-storey house in the row across the road, from x0 to x1, seven deep (z -12 to -18): stock brick, its door and dark windows
     * with stone heads, a slate gable roof with the ridge along the street (eaves at 8, ridge at 10),
     * and a chimney stack at its outer end.
     */
    static void neighbour(ServerLevel l,BlockPos b,int x0,int x1,int doorX,int[] groundWindows,int[] upperWindows){
        for(int x=x0;x<=x1;x++)for(int z=-18;z<=-12;z++)for(int y=-1;y<=7;y++)set(l,b,x,y,z,brick());
        door(l,b,doorX,-12,Direction.SOUTH);set(l,b,doorX,2,-12,glass());set(l,b,doorX,3,-12,stone());
        for(int x:groundWindows){set(l,b,x,1,-12,glass());set(l,b,x,2,-12,glass());set(l,b,x,3,-12,stone());}
        for(int x:upperWindows){set(l,b,x,5,-12,glass());set(l,b,x,6,-12,glass());set(l,b,x,7,-12,stone());}
        for(int x=x0;x<=x1;x++){
            set(l,b,x,8,-12,stair(Direction.NORTH));set(l,b,x,9,-13,stair(Direction.NORTH));
            for(int z=-16;z<=-14;z++)set(l,b,x,10,z,tiles());
            set(l,b,x,9,-17,stair(Direction.SOUTH));set(l,b,x,8,-18,stair(Direction.SOUTH));
            for(int z=-17;z<=-13;z++)set(l,b,x,8,z,brick());for(int z=-16;z<=-14;z++)set(l,b,x,9,z,brick());
        }
        int cx=x0<0?x0:x1-1;
        for(int dx=0;dx<=1;dx++){set(l,b,cx+dx,11,-15,brick());set(l,b,cx+dx,12,-15,pot());}
    }
    /** The house's back yard, under the flat's west windows: flags, a brick wall down the middle and along the back, a bin. */
    static void yard(ServerLevel l,BlockPos b,int x0,int x1,int split){
        for(int x=x0;x<=x1;x++)for(int z=-31;z<=-19;z++)set(l,b,x,-1,z,Blocks.SMOOTH_STONE.defaultBlockState());
        var wall=Blocks.BRICK_WALL.defaultBlockState();
        for(int z=-31;z<=-19;z++){set(l,b,split,0,z,wall);set(l,b,split,1,z,wall);}
        for(int x=x0;x<=x1;x++){set(l,b,x,0,-31,wall);set(l,b,x,1,-31,wall);}
        set(l,b,split<0?x0+1:x1-1,0,-20,Blocks.CAULDRON.defaultBlockState());set(l,b,split<0?x1-1:x0+1,0,-21,Blocks.CAULDRON.defaultBlockState());
    }
    /** The launderette's parade keeps its flat roof and parapet; two chimney stacks stand above it with their pots. */
    static void launderetteChimneys(ServerLevel l,BlockPos b){
        for(int x:new int[]{-9,7})for(int dx=0;dx<=1;dx++){set(l,b,x+dx,10,-21,brick());set(l,b,x+dx,11,-21,brick());set(l,b,x+dx,12,-21,pot());}
    }
    /**
     * Each end of the road meets the front of a house across a T-junction (the old blank wall at x ±17): a kerb across the road's end, the
     * house's door and fanlight, its windows with stone heads, a slate eave and a chimney stack.
     */
    static void roadEnd(ServerLevel l,BlockPos b,int x,Direction facing){
        int in=facing==Direction.EAST?1:-1;
        for(int z=-7;z<=-4;z++)set(l,b,x+in,-1,z,SecondPersonSite.mat("kerb"));
        door(l,b,x,-6,facing);set(l,b,x,2,-6,glass());set(l,b,x,3,-6,stone());
        for(int z:new int[]{-2,-3,-9,-10}){set(l,b,x,1,z,glass());set(l,b,x,2,z,glass());set(l,b,x,3,z,stone());}
        for(int z:new int[]{-2,-3,-6,-9,-10}){set(l,b,x,6,z,glass());set(l,b,x,7,z,glass());set(l,b,x,8,z,stone());}
        for(int z=-11;z<=-1;z++)set(l,b,x,10,z,stair(facing.getOpposite()));
        for(int dz=0;dz<=1;dz++){set(l,b,x,10,-5-dz,brick());set(l,b,x,11,-5-dz,brick());set(l,b,x,12,-5-dz,pot());}
    }
    /**
     * Beyond the yards (x ±17, z -31 to -12), the backs of the next street's houses: dark back windows with stone heads over the
     * yards, the walls' slate eave, and a chimney stack. The flat's kitchen and bedroom windows look out over the west yard to these.
     */
    static void backs(ServerLevel l,BlockPos b,int x,Direction facing){
        for(int z:new int[]{-21,-24,-27,-30}){
            set(l,b,x,1,z,glass());set(l,b,x,2,z,glass());set(l,b,x,3,z,stone());
            set(l,b,x,6,z,glass());set(l,b,x,7,z,glass());set(l,b,x,8,z,stone());
        }
        for(int z=-31;z<=-12;z++)set(l,b,x,10,z,stair(facing.getOpposite()));
        for(int dz=0;dz<=1;dz++){set(l,b,x,10,-25-dz,brick());set(l,b,x,11,-25-dz,brick());set(l,b,x,12,-25-dz,pot());}
    }
    /** The street tree's iron grille, flat round the foot of its trunk; the season's fallen leaves lie beyond it. */
    static void treeGrille(ServerLevel l,BlockPos b){
        var tree=SecondPersonSite.TREE;var grille=Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.BOTTOM).setValue(TrapDoorBlock.OPEN,false);
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(dx!=0||dz!=0)set(l,b,tree.getX()+dx,0,tree.getZ()+dz,grille);
    }
}
