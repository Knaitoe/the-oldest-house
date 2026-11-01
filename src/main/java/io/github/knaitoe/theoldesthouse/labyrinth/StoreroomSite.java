package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.210, the storeroom (docs/STOREROOM_0_4_210.md): a dark hall in the dark halls' ash, twenty long, ending in a door on a
 * spring, and past it a narrow storeroom twelve deep: steel shelving down both sides and across the end, one bulb on a pull chain
 * halfway, boxes. The list hangs beside the door, on its inside.
 *
 * <p>Relative to the base: the hall x -1 to 1, z 0 to -19; the door at z -20; the storeroom z -21 to -32 between shelves at
 * x -3 and 3 (five wide), four high, the end shelf at z -33. Each time someone looks back at the door the side shelves step a
 * block nearer ({@link #side}); at the third, they meet.
 */
public final class StoreroomSite {
    private StoreroomSite(){}
    public static final int HALL_END=-19,DOOR_Z=-20,NEAR_Z=-21,FAR_Z=-32,END_Z=-33,SIDE=3,HIGH=4;
    public static final BlockPos DOOR=new BlockPos(0,0,DOOR_Z);
    /** The list, pinned beside the door on its inside (the source). */
    public static final BlockPos LIST=new BlockPos(1,1,NEAR_Z);
    public static final BlockPos BULB=new BlockPos(0,3,-26);
    /** The three things: the ink on the left, the needles on the right, the reader's own thing on the end shelf. */
    public static final int INK_Z=-24,NEEDLES_Z=-29,ITEM_Y=1;
    public static final BlockPos OWN=new BlockPos(0,ITEM_Y,END_Z);
    /** Johnny's footnote, on the hall floor near the way in (the account). */
    public static final BlockPos FOOTNOTE=new BlockPos(-1,0,-3);
    public static final BlockPos ONWARD=new BlockPos(2,0,-8);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    static BlockState ash(){return DarkHallRegistry.material("ash").defaultBlockState();}
    static BlockState floor(){return DarkHallRegistry.material("floor").defaultBlockState();}
    static BlockState ceiling(){return DarkHallRegistry.material("ceiling").defaultBlockState();}
    /** Where the side shelves stand after this many looks: x = ±(3 - looks). */
    public static int side(int looks){return SIDE-Math.max(0,Math.min(SIDE,looks));}
    public static boolean inStore(Vec3 r){return r.x>=-SIDE&&r.x<SIDE+1&&r.z>=FAR_Z&&r.z<NEAR_Z+1&&r.y>=-.5&&r.y<HIGH;}
    public static boolean inHall(Vec3 r){return r.x>=-1&&r.x<2&&r.z>=HALL_END&&r.z<1;}
    public static Vec3 doorCentre(BlockPos b){return Vec3.atCenterOf(b.offset(DOOR)).add(0,.5,0);}

    public static void build(ServerLevel l,BlockPos b){
        BuildBlocks.box(l,b.offset(-4,-1,-35),b.offset(4,5,0),ash(),F);
        for(int z=HALL_END;z<=0;z++)for(int x=-1;x<=1;x++){BuildBlocks.set(l,b.offset(x,-1,z),floor(),F);BuildBlocks.set(l,b.offset(x,3,z),ceiling(),F);for(int y=0;y<3;y++)BuildBlocks.set(l,b.offset(x,y,z),Blocks.AIR.defaultBlockState(),F);}
        for(int z=FAR_Z;z<=NEAR_Z;z++)for(int x=-SIDE+1;x<=SIDE-1;x++){BuildBlocks.set(l,b.offset(x,-1,z),floor(),F);BuildBlocks.set(l,b.offset(x,HIGH,z),ceiling(),F);for(int y=0;y<HIGH;y++)BuildBlocks.set(l,b.offset(x,y,z),Blocks.AIR.defaultBlockState(),F);}
        shelves(l,b,0);
        for(int x=-SIDE+1;x<=SIDE-1;x++)for(int y=0;y<HIGH;y++)BuildBlocks.set(l,b.offset(x,y,END_Z),HorrorBlock.of(x==0&&y==ITEM_Y?HorrorBlock.Kind.SHELF_OWN:HorrorBlock.Kind.SHELF,Direction.SOUTH),F);
        door(l,b,false);
        BuildBlocks.set(l,b.offset(LIST),HorrorBlock.of(HorrorBlock.Kind.LIST,Direction.NORTH),F);
        BuildBlocks.set(l,b.offset(BULB),HorrorBlock.of(HorrorBlock.Kind.BULB,Direction.NORTH,1),F);
        BuildBlocks.set(l,b.offset(2,0,FAR_Z),HorrorBlock.of(HorrorBlock.Kind.BOXES,Direction.WEST),F);
        BuildBlocks.set(l,b.offset(-2,0,NEAR_Z),HorrorBlock.of(HorrorBlock.Kind.BOXES,Direction.EAST),F);
        BuildBlocks.set(l,b.offset(FOOTNOTE),HorrorBlock.of(HorrorBlock.Kind.FOOTNOTE,Direction.SOUTH),F);
    }
    /**
     * Puts the side shelves where this many looks leave them: everything between the old shelves and the new is shelving, with
     * the ink and the needles on the innermost face. Returns the cells newly filled.
     */
    public static java.util.List<BlockPos> shelves(ServerLevel l,BlockPos b,int looks){
        var filled=new java.util.ArrayList<BlockPos>();int in=side(looks);
        for(int z=FAR_Z;z<=NEAR_Z;z++)for(int y=0;y<HIGH;y++)for(int sign:new int[]{-1,1})for(int x=in;x<=SIDE;x++){
            if(x==0&&sign==1)continue;
            var at=b.offset(sign*x,y,z);boolean face=x==in;
            HorrorBlock.Kind kind=face&&y==ITEM_Y&&z==INK_Z&&sign<0?HorrorBlock.Kind.SHELF_INK:face&&y==ITEM_Y&&z==NEEDLES_Z&&sign>0?HorrorBlock.Kind.SHELF_NEEDLES:HorrorBlock.Kind.SHELF;
            var was=l.getBlockState(at);var want=HorrorBlock.of(kind,sign<0?Direction.EAST:Direction.WEST);
            if(!HorrorBlock.shelf(was))filled.add(at.immutable());
            if(!was.equals(want))BuildBlocks.set(l,at,want,F);
        }
        return filled;
    }
    public static void door(ServerLevel l,BlockPos b,boolean open){
        var lower=Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.OPEN,open);
        BuildBlocks.set(l,b.offset(DOOR),lower.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),F);
        BuildBlocks.set(l,b.offset(DOOR).above(),lower.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),F);
    }
    public static boolean open(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(DOOR));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    public static void bulb(ServerLevel l,BlockPos b,int stage){var at=b.offset(BULB);var s=l.getBlockState(at);if(HorrorBlock.is(s,HorrorBlock.Kind.BULB)&&s.getValue(HorrorBlock.STAGE)!=stage)l.setBlock(at,s.setValue(HorrorBlock.STAGE,stage),F);}
}
