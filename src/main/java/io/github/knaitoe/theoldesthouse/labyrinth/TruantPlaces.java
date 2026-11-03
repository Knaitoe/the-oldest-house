package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.264, Johnny Truant's places (docs/TRUANT_0_4_264.md; the owner's draft is docs/TRUANT_RESIDENT_DESIGN_DRAFT.md): three ordinary
 * halls of his own, built whole, dark and cramped, dealt among the halls like any other. Who is in them, and what he does there, is
 * {@link Truant}.
 * <ul>
 * <li>The held door: a low service passage of brick and pipes, a heavy door on a closer halfway along, a recess with his lamp past it.</li>
 * <li>The crossing: a shaft with a one-block ledge along its wall and no rail; the drop is to a lower landing, with a ladder back up.</li>
 * <li>The lamp: a low corridor, two high, round a corner to a shallow alcove with his lamp, where his face is seen half round the jamb.</li>
 * </ul>
 */
public final class TruantPlaces {
    private TruantPlaces(){}
    public static final LabyrinthPlace DOOR=LabyrinthPlace.TRUANT_DOOR,LEDGE=LabyrinthPlace.TRUANT_LEDGE,LAMP=LabyrinthPlace.TRUANT_LAMP;
    public static final List<LabyrinthPlace> PLACES=List.of(DOOR,LEDGE,LAMP);
    public static boolean is(LabyrinthPlace place){return place!=null&&PLACES.contains(place);}
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------ the plan
    /**
     * The held door: the heavy door in the middle of its brick partition, the cells either side of it (south, the way readers come;
     * north, through), where he stands holding it (beside the doorway on its far side, a hand on the door), and his lamp in the recess.
     */
    public static final BlockPos HEAVY_DOOR=new BlockPos(0,0,-11);
    public static final Vec3 DOOR_SPOT=new Vec3(1.45,0,-12.3);
    public static final BlockPos DOOR_LAMP=new BlockPos(2,1,-14);
    /** 0.4.282: where he stands in the doorway itself, a hand up on the frame, facing the way readers come (the hand in the doorway). */
    public static final Vec3 IN_DOORWAY=new Vec3(.5,0,-10.5);
    /** The crossing: the ledge's column and its run over the drop, the lower landing's floor, where he waits at the near end. */
    public static final int LEDGE_X=4,LEDGE_NEAR=-4,LEDGE_FAR=-12,LOWER=-7;
    public static final Vec3 LEDGE_SPOT=new Vec3(3.5,0,-2.5),FAR_SPOT=new Vec3(3.4,0,-13.6);
    /** 0.4.282: the dark west end of the near landing, where he waits before he comes up behind someone on the ledge (the shove). */
    public static final Vec3 SHOVE_WAIT=new Vec3(-3.4,0,-2.6);
    /**
     * The lamp: where he stands to look round the inner corner of the turn (behind it from the north leg, all but the edge of his
     * face), where he waits back to the wall round the corner, and the alcove where he stands at his lamp.
     */
    public static final Vec3 PEEK_SPOT=new Vec3(-1.25,0,-6.68),WALL_SPOT=new Vec3(-3.5,0,-6.72),LAMP_SPOT=new Vec3(-4.5,0,-9.55);
    public static final BlockPos LAMP_LIGHT=new BlockPos(-6,1,-10);
    /** 0.4.282: the far leg past the alcove, where he goes to look into the dark once his lamp has been put out for him. */
    public static final Vec3 DARK_SPOT=new Vec3(-10,0,-13.5);

    /**
     * How often each of his places is dealt among the halls: from the crossing where stories begin (the opening stays domestic), and
     * none of them within six rooms of the last.
     */
    public static final int WEIGHT=3,FROM_DEPTH=LabyrinthPacing.STORY_DEPTH,REPEAT=6;
    public static int weight(LabyrinthData data,UUID player,int depth){
        if(depth<FROM_DEPTH)return 0;
        for(var place:PLACES){int age=data.recentVisit(player,place);if(age>=0&&age<REPEAT)return 0;}
        return WEIGHT;
    }

    // ------------------------------------------------------------------ building
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);
    }
    static BlockState air(){return Blocks.AIR.defaultBlockState();}
    static BlockState pipe(Direction along){return Blocks.LIGHTNING_ROD.defaultBlockState().setValue(LightningRodBlock.FACING,along);}
    static void door(ServerLevel l,BlockPos b,BlockPos at,Block block,Direction facing,boolean open){
        var s=block.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,open);
        l.setBlock(b.offset(at),s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),FLAGS);l.setBlock(b.offset(at).above(),s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),FLAGS);
    }

    /**
     * The held door. A service passage three wide and three high, brick, with pipes run along the top of both walls; halfway, a brick
     * partition with a heavy door in it (it shuts itself, {@link Truant}); past it, on the right, a recess with a crate, his lamp on it,
     * and a stool. Nothing else is lit.
     */
    public static void buildDoor(ServerLevel l,BlockPos b){
        var brick=Blocks.BRICKS.defaultBlockState();var floor=Blocks.SMOOTH_STONE.defaultBlockState();var ceiling=Blocks.SMOOTH_STONE.defaultBlockState();
        box(l,b,-3,-1,-24,3,4,0,brick);
        box(l,b,-1,-1,-23,1,-1,-1,floor);box(l,b,-1,3,-23,1,3,-1,ceiling);box(l,b,-1,0,-23,1,2,-1,air());
        for(int z=-23;z<=-1;z++)if(z!=HEAVY_DOOR.getZ())for(int x:new int[]{-1,1})set(l,b,x,2,z,pipe(Direction.NORTH));
        // The partition and the heavy door, shut.
        for(int x:new int[]{-1,1})box(l,b,x,0,HEAVY_DOOR.getZ(),x,2,HEAVY_DOOR.getZ(),brick);set(l,b,0,2,HEAVY_DOOR.getZ(),brick);
        door(l,b,HEAVY_DOOR,Blocks.DARK_OAK_DOOR,Direction.SOUTH,false);
        // The recess past it, on the right: a crate with his lamp on it, and a stool.
        box(l,b,2,0,-15,2,1,-14,air());box(l,b,2,-1,-15,2,-1,-14,floor);
        set(l,b,2,0,-14,Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,Direction.UP));set(l,b,DOOR_LAMP.getX(),DOOR_LAMP.getY(),DOOR_LAMP.getZ(),Blocks.LANTERN.defaultBlockState());
        set(l,b,2,0,-15,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.WEST));
        LabyrinthBuilder.entrance(l,b,brick,floor,ceiling);
        LabyrinthBuilder.doors(l,b,DOOR);
    }

    /**
     * The crossing. A stone room with a landing at each end and a shaft between them; the only way over is the ledge along the east wall,
     * a block wide, with no rail. The drop is to a lower landing seven blocks down, with a lantern by the ladder back up to the near
     * landing. A lantern on the far landing; the shaft is dark.
     */
    public static void buildLedge(ServerLevel l,BlockPos b){
        var stone=Blocks.STONE_BRICKS.defaultBlockState();var mossy=Blocks.MOSSY_STONE_BRICKS.defaultBlockState();var floor=Blocks.POLISHED_ANDESITE.defaultBlockState();
        box(l,b,-5,-9,-16,5,4,0,stone);
        // The two landings.
        box(l,b,-4,-1,-3,4,-1,-1,floor);box(l,b,-4,0,-3,4,3,-1,air());
        box(l,b,-4,-1,-15,4,-1,-13,floor);box(l,b,-4,0,-15,4,3,-13,air());
        // The shaft between them, down to the lower landing; the ledge left standing along the east wall.
        box(l,b,-4,LOWER,LEDGE_FAR,4,3,LEDGE_NEAR,air());
        for(int z=LEDGE_FAR;z<=LEDGE_NEAR;z++){set(l,b,LEDGE_X,-1,z,floor);set(l,b,LEDGE_X,-2,z,stone);}
        box(l,b,-4,LOWER-1,LEDGE_FAR,4,LOWER-1,LEDGE_NEAR,floor);
        for(int z=LEDGE_FAR;z<=LEDGE_NEAR;z+=3)set(l,b,-5,LOWER+2,z,mossy);
        // The ladder from the lower landing back up to the near one, and a lantern by its foot.
        for(int y=LOWER;y<=-1;y++)set(l,b,0,y,LEDGE_NEAR,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        set(l,b,1,LOWER,LEDGE_NEAR,Blocks.LANTERN.defaultBlockState());
        set(l,b,-4,0,-15,Blocks.LANTERN.defaultBlockState());
        LabyrinthBuilder.entrance(l,b,stone,floor,stone);
        LabyrinthBuilder.doors(l,b,LEDGE);
    }

    /**
     * The lamp. A plastered corridor two high: north from the door, west at the turn, north again to the far door. In the west leg's
     * north wall, a shallow alcove with a crate and his lamp on it; that lamp is the only light. His face is seen round the inner corner
     * of the turn ({@link #PEEK_SPOT}).
     */
    public static void buildLamp(ServerLevel l,BlockPos b){
        var plaster=TraceRooms.mat("plaster");var panel=TraceRooms.mat("panel");var boards=TraceRooms.mat("boards");
        box(l,b,-12,-1,-16,2,3,0,plaster);
        box(l,b,-12,0,-16,2,0,0,panel);
        box(l,b,-11,-1,-15,1,-1,-1,boards);
        box(l,b,-1,0,-9,1,1,-1,air());
        box(l,b,-11,0,-9,-2,1,-7,air());
        box(l,b,-11,0,-15,-9,1,-10,air());
        // The alcove: a crate with his lamp on it, and room beside it for him to stand.
        box(l,b,-6,0,-10,-5,1,-10,air());
        set(l,b,-6,0,-10,Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,Direction.UP));set(l,b,LAMP_LIGHT.getX(),LAMP_LIGHT.getY(),LAMP_LIGHT.getZ(),Blocks.LANTERN.defaultBlockState());
        LabyrinthBuilder.entrance(l,b,plaster,boards,plaster);
        LabyrinthBuilder.doors(l,b,LAMP);
    }

    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace place){
        switch(place){
            case TRUANT_DOOR->buildDoor(l,b);
            case TRUANT_LEDGE->buildLedge(l,b);
            case TRUANT_LAMP->buildLamp(l,b);
            default->{}
        }
    }
}
