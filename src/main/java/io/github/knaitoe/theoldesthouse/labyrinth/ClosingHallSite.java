package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.209, the closing hall (docs/CLOSING_HALL_0_4_209.md): a long dark hall in the dark halls' own ash, the two children at its
 * far end, a squeeze in its west wall near the way in, and past the squeeze a small room, the way on, and a passage north into
 * the dark.
 *
 * <p>Relative to the base: the way in at z 1; the hall seven wide (x -3 to 3), six high (y 0 to 5), from z -1 to z -46. The
 * squeeze runs west along z -3 at floor level, one block high, from the hall through its wall to the room past it (x -16 to -11,
 * z -6 to -1, four high). The children's passage goes north from that room, two wide, to a blank wall at z -41.
 *
 * <p>How far the hall has closed is a stage from 0 (open) to 9 (shut). At each stage the hall's open section is a half width
 * ({@link #halfWidth}) and a height ({@link #height}); everything else between its walls is ash. The squeeze's cells inside the
 * hall stay open until it shuts, so the closer the walls come the longer the squeeze is.
 */
public final class ClosingHallSite {
    private ClosingHallSite(){}
    public static final int HALF=3,HIGH=6,NEAR_Z=-1,FAR_Z=-46,SQUEEZE_Z=-3;
    /** The squeeze through the west wall, from the shell to the room past it. */
    public static final int TUNNEL_IN=-4,TUNNEL_OUT=-10;
    /** The room past the squeeze. */
    public static final int ROOM_X0=-16,ROOM_X1=-11,ROOM_Z0=-6,ROOM_Z1=-1,ROOM_H=4;
    /** The children's passage north out of that room. */
    public static final int RUN_X0=-15,RUN_X1=-14,RUN_Z_END=-40;
    public static final BlockPos CHAD=new BlockPos(-1,0,-44),DAISY=new BlockPos(1,0,-44);
    /** Daisy's drawing on the floor between them (the source). */
    public static final BlockPos DRAWING=new BlockPos(0,0,-43);
    /** Karen's interview in the room past the squeeze (the account), and its copy by the way in. */
    public static final BlockPos TRANSCRIPT=new BlockPos(-12,0,-5),TRANSCRIPT_DOOR=new BlockPos(-1,0,0);
    public static final BlockPos CRAYONS=new BlockPos(0,0,-45);
    /** The way on, in the west wall of the room past the squeeze. */
    public static final BlockPos ONWARD=new BlockPos(-17,0,-3);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    public static int halfWidth(int stage){return stage<1?3:stage<4?2:stage<7?1:0;}
    public static int height(int stage){return switch(stage){case 0,1->6;case 2->5;case 3,4->4;case 5->3;case 6,7->2;case 8->1;default->0;};}
    static BlockState ash(){return DarkHallRegistry.material("ash").defaultBlockState();}
    static BlockState floor(){return DarkHallRegistry.material("floor").defaultBlockState();}
    static BlockState ceiling(){return DarkHallRegistry.material("ceiling").defaultBlockState();}

    /** Inside the hall's walls (open or closed), at any height up to its ceiling. */
    public static boolean inHall(Vec3 r){return r.x>=-HALF&&r.x<HALF+1&&r.z>=FAR_Z&&r.z<NEAR_Z+1&&r.y>=-.5&&r.y<HIGH;}
    public static boolean inHallCell(int x,int y,int z){return x>=-HALF&&x<=HALF&&y>=0&&y<HIGH&&z>=FAR_Z&&z<=NEAR_Z;}
    /** The squeeze's cells inside the hall, from its middle out to its wall: open until the hall is shut. */
    public static boolean channel(int x,int y,int z){return z==SQUEEZE_Z&&y==0&&x<0&&x>=-HALF;}
    /** In the squeeze, past the hall's open section at this stage (where a child has to go first). */
    public static boolean inSqueeze(Vec3 r,int stage){int x=(int)Math.floor(r.x),z=(int)Math.floor(r.z);return z==SQUEEZE_Z&&r.y<1&&x<-halfWidth(stage)&&x>=TUNNEL_OUT;}
    public static boolean inTunnel(Vec3 r){int x=(int)Math.floor(r.x),z=(int)Math.floor(r.z);return z==SQUEEZE_Z&&r.y<1&&x<=TUNNEL_IN&&x>=TUNNEL_OUT;}
    public static boolean inRoom(Vec3 r){return r.x>=ROOM_X0&&r.x<ROOM_X1+1&&r.z>=ROOM_Z0&&r.z<ROOM_Z1+1;}
    public static boolean inRun(Vec3 r){return r.x>=RUN_X0&&r.x<RUN_X1+1&&r.z<ROOM_Z0&&r.z>=RUN_Z_END;}
    public static boolean past(Vec3 r){return inRoom(r)||inRun(r);}
    /** The open cell at the squeeze's mouth at this stage: the first one past the hall's open section. */
    public static BlockPos mouth(int stage){return new BlockPos(-halfWidth(stage)-1,0,SQUEEZE_Z);}
    /** Whether a hall cell is ash at this stage. */
    public static boolean closed(int stage,int x,int y,int z){
        if(!inHallCell(x,y,z))return false;
        if(stage>=9)return true;
        if(stage>=1&&z==NEAR_Z)return true;
        if(channel(x,y,z))return false;
        return Math.abs(x)>halfWidth(stage)||y>=height(stage);
    }

    /** Builds the hall open, the children's things laid out, the squeeze's mouth shut. */
    public static void build(ServerLevel l,BlockPos b){
        // Ash all through, then the spaces carved out of it.
        BuildBlocks.box(l,b.offset(-19,-1,-48),b.offset(5,7,0),ash(),F);
        for(int z=FAR_Z;z<=NEAR_Z;z++)for(int x=-HALF;x<=HALF;x++){
            BuildBlocks.set(l,b.offset(x,-1,z),floor(),F);BuildBlocks.set(l,b.offset(x,HIGH,z),ceiling(),F);
            for(int y=0;y<HIGH;y++)BuildBlocks.set(l,b.offset(x,y,z),Blocks.AIR.defaultBlockState(),F);
        }
        // The way in: a step inside the door, three wide and three high.
        for(int x=-1;x<=1;x++){BuildBlocks.set(l,b.offset(x,-1,0),floor(),F);for(int y=0;y<3;y++)BuildBlocks.set(l,b.offset(x,y,0),Blocks.AIR.defaultBlockState(),F);BuildBlocks.set(l,b.offset(x,3,0),ceiling(),F);}
        // The squeeze through the wall, one high, its mouth shut until the hall begins to close.
        for(int x=TUNNEL_OUT;x<=TUNNEL_IN;x++){BuildBlocks.set(l,b.offset(x,-1,SQUEEZE_Z),floor(),F);BuildBlocks.set(l,b.offset(x,0,SQUEEZE_Z),Blocks.AIR.defaultBlockState(),F);BuildBlocks.set(l,b.offset(x,1,SQUEEZE_Z),ceiling(),F);}
        BuildBlocks.set(l,b.offset(TUNNEL_IN,0,SQUEEZE_Z),ash(),F);
        // The room past it, and the passage north.
        for(int x=ROOM_X0;x<=ROOM_X1;x++)for(int z=ROOM_Z0;z<=ROOM_Z1;z++){BuildBlocks.set(l,b.offset(x,-1,z),floor(),F);BuildBlocks.set(l,b.offset(x,ROOM_H,z),ceiling(),F);for(int y=0;y<ROOM_H;y++)BuildBlocks.set(l,b.offset(x,y,z),Blocks.AIR.defaultBlockState(),F);}
        for(int x=RUN_X0;x<=RUN_X1;x++)for(int z=RUN_Z_END;z<ROOM_Z0;z++){BuildBlocks.set(l,b.offset(x,-1,z),floor(),F);BuildBlocks.set(l,b.offset(x,3,z),ceiling(),F);for(int y=0;y<3;y++)BuildBlocks.set(l,b.offset(x,y,z),Blocks.AIR.defaultBlockState(),F);}
        // The children's things, and the account in both places.
        BuildBlocks.set(l,b.offset(DRAWING),HorrorBlock.of(HorrorBlock.Kind.DRAWING,Direction.SOUTH),F);
        BuildBlocks.set(l,b.offset(CRAYONS),HorrorBlock.of(HorrorBlock.Kind.CRAYONS,Direction.SOUTH),F);
        BuildBlocks.set(l,b.offset(TRANSCRIPT),HorrorBlock.of(HorrorBlock.Kind.TRANSCRIPT,Direction.EAST),F);
        BuildBlocks.set(l,b.offset(TRANSCRIPT_DOOR),HorrorBlock.of(HorrorBlock.Kind.TRANSCRIPT,Direction.EAST),F);
    }

    /**
     * Brings the hall to a stage: every cell that should be ash and is air becomes ash, unless {@code spared} says not to (a child
     * is in it). Returns how many cells were closed.
     */
    public static int close(ServerLevel l,BlockPos b,int stage,java.util.function.Predicate<BlockPos> spared){
        int closed=0;
        for(int z=FAR_Z;z<=NEAR_Z;z++)for(int x=-HALF;x<=HALF;x++)for(int y=0;y<HIGH;y++){
            if(!closed(stage,x,y,z))continue;var at=b.offset(x,y,z);
            if(!l.getBlockState(at).isAir()||spared.test(at))continue;
            l.setBlock(at,ash(),F);closed++;
        }
        if(stage>=1&&stage<9&&l.getBlockState(b.offset(TUNNEL_IN,0,SQUEEZE_Z)).is(ash().getBlock()))l.setBlock(b.offset(TUNNEL_IN,0,SQUEEZE_Z),Blocks.AIR.defaultBlockState(),F);
        return closed;
    }
    /** The squeeze filling behind the hall, one cell at a time from the hall side. */
    public static void fillTunnel(ServerLevel l,BlockPos b,int x){l.setBlock(b.offset(x,0,SQUEEZE_Z),ash(),F);}
}
