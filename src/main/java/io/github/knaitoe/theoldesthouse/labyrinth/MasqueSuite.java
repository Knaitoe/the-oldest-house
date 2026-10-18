package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.MasqueBlock.Kind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Masque, rebuilt (0.4.105): Prince Prospero's seven chambers as Poe has them. They wind: each doorway is off to the other
 * side of the last, so a reader sees little more than one room at a time. Each is hung in its own colour, and a tall Gothic
 * window in the middle of each side wall looks out onto a closed corridor where a brazier on a tripod stands opposite it. There
 * is no lamp or candle in the suite; the braziers light the rooms through the glass. The seventh is black velvet from ceiling
 * to carpet, its panes scarlet, and the gigantic ebony clock stands against its far wall, facing the way in.
 * <p>Every room keeps the eleven-block band along -z the story counts by: room {@code i}'s near wall at {@code -11i-1}, its far
 * wall at {@code -11i-11}, the dancers at {@code (±5, -11i-6)}. Polish, composition and review passes do not touch it.
 */
public final class MasqueSuite {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    public static final int ROOMS=7;
    /** The clock's face, which a reader puts their hand on; the rest of the clock stands above and below it. */
    public static final BlockPos FACE=new BlockPos(0,2,-76);
    public static final BlockPos INVITATION=new BlockPos(7,1,-4),ACCOUNT=new BlockPos(5,1,-73);
    private MasqueSuite(){}

    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void fill(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)set(l,b,x,y,z,s);}
    private static BlockState of(Kind k){return MasqueBlock.of(k,Direction.NORTH);}
    private static BlockState of(Kind k,Direction f,int stage){return MasqueBlock.of(k,f,stage);}
    /** Where the doorway out of room {@code i} (into {@code i+1}) is centred: off to alternate sides. */
    public static int door(int i){return i%2==0?6:-6;}
    static int near(int i){return -11*i-1;}
    static int far(int i){return -11*i-11;}
    static int centre(int i){return -11*i-6;}

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        // The abbey's stone first; the chambers and the corridor are cut out of it.
        fill(l,b,-12,-1,-78,12,11,0,of(Kind.TRACERY));
        for(int i=0;i<ROOMS;i++)chamber(l,b,i);
        // Doorways last, so no later chamber's hangings close one.
        for(int i=0;i<ROOMS-1;i++)doorway(l,b,i);
        // The closed corridor that follows the suite on both sides, with a brazier opposite every window.
        for(int side:new int[]{-11,11}){fill(l,b,side,0,-77,side,7,-1,air);
            for(int i=0;i<ROOMS;i++){set(l,b,side,0,centre(i),of(Kind.TRIPOD));set(l,b,side,1,centre(i),of(Kind.BRAZIER));}}
        // Into the first chamber from the House: a narrow ebony-framed opening through both walls.
        for(int z:new int[]{0,-1}){fill(l,b,-1,0,z,1,2,z,of(Kind.EBONY));set(l,b,0,0,z,air);set(l,b,0,1,z,air);}
        // The steward's last invitation on its stand by the door.
        set(l,b,7,0,-4,of(Kind.STAND));set(l,b,INVITATION.getX(),INVITATION.getY(),INVITATION.getZ(),of(Kind.INVITATION,Direction.SOUTH,0));
        // The musicians' dais in the fourth chamber, tucked in the corner away from both doorways; nobody is playing.
        fill(l,b,-9,0,-37,-6,0,-35,of(Kind.PARQUET));
        for(int x=-8;x<=-7;x++){set(l,b,x,1,-36,of(Kind.MUSIC_STAND,Direction.SOUTH,0));NovelRooms.furniture(l,b.offset(x,1,-35),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);}
        set(l,b,-9,1,-36,of(Kind.MUSIC_STAND,Direction.SOUTH,0));
        // The clock: an ebony cabinet three wide against the black room's far wall, the clock itself in the middle.
        int cx=FACE.getX(),cz=FACE.getZ();
        for(int dx:new int[]{-1,1}){fill(l,b,cx+dx,0,cz,cx+dx,2,cz,of(Kind.EBONY));set(l,b,cx+dx,3,cz,of(Kind.CORNICE));}
        set(l,b,cx,0,cz,of(Kind.CLOCK_BASE,Direction.SOUTH,0));set(l,b,cx,1,cz,of(Kind.CLOCK_CASE,Direction.SOUTH,0));set(l,b,cx,2,cz,of(Kind.CLOCK_FACE,Direction.SOUTH,0));
        set(l,b,cx,3,cz,of(Kind.CORNICE));set(l,b,cx,4,cz,of(Kind.CLOCK_CROWN,Direction.SOUTH,0));
        // The account's stand; the ledger itself is set on it with every other room's.
        set(l,b,ACCOUNT.getX(),0,ACCOUNT.getZ(),of(Kind.STAND));
    }

    private static void chamber(ServerLevel l,BlockPos b,int i){
        var air=Blocks.AIR.defaultBlockState();int c=i;boolean black=i==ROOMS-1;
        var hang=of(Kind.VELVET,Direction.NORTH,c);
        int n=near(i),f=far(i),mid=centre(i);
        // Hung walls on all four sides (the walls between rooms are two hangings thick, one in each room's colour).
        fill(l,b,-10,0,f,10,7,n,hang);
        fill(l,b,-9,0,f+1,9,7,n-1,air);
        fill(l,b,-9,-1,f+1,9,-1,n-1,black?hang:of(Kind.PARQUET));
        fill(l,b,-10,8,f,10,8,n,black?hang:of(Kind.COFFER));
        if(!black){
            // A gilt cornice under the ceiling, and ebony pilasters either side of each window and at the corners.
            for(int z=f;z<=n;z++){set(l,b,-10,7,z,of(Kind.CORNICE));set(l,b,10,7,z,of(Kind.CORNICE));}
            for(int x=-10;x<=10;x++){set(l,b,x,7,f,of(Kind.CORNICE));set(l,b,x,7,n,of(Kind.CORNICE));}
            for(int x:new int[]{-10,10})for(int z:new int[]{mid-3,mid+3,f+1,n-1})fill(l,b,x,0,z,x,6,z,of(Kind.EBONY));
        }
        // The window in the middle of each side wall: three lancets, the middle one tallest, on a stone sill.
        for(int x:new int[]{-10,10}){var face=x<0?Direction.EAST:Direction.WEST;
            for(int dz=-1;dz<=1;dz++){int top=dz==0?4:3;set(l,b,x,0,mid+dz,of(Kind.TRACERY));
                for(int y=1;y<=top;y++)set(l,b,x,y,mid+dz,of(Kind.LANCET,face,c));
                set(l,b,x,top+1,mid+dz,of(Kind.LANCET_ARCH,face,c));}}
        // Divans against the side walls beyond each window.
        for(int x:new int[]{-9,9})for(int z:new int[]{mid-2,mid-3})set(l,b,x,0,z,of(Kind.DIVAN,x<0?Direction.EAST:Direction.WEST,c));
        // Two masks on the near wall, looking in.
        if(!black)for(int x:new int[]{-3,3})set(l,b,x,3,n-1,of(Kind.MASK,Direction.NORTH,0));
    }

    /** The doorway on from room {@code i}, off to one side, pointed and framed in ebony through both hangings. */
    private static void doorway(ServerLevel l,BlockPos b,int i){
        var air=Blocks.AIR.defaultBlockState();int d=door(i),f=far(i);
        for(int z:new int[]{f,f-1}){
            fill(l,b,d-2,0,z,d-2,4,z,of(Kind.EBONY));fill(l,b,d+2,0,z,d+2,4,z,of(Kind.EBONY));fill(l,b,d-1,5,z,d+1,5,z,of(Kind.EBONY));
            fill(l,b,d-1,0,z,d+1,3,z,air);set(l,b,d,4,z,air);set(l,b,d-1,4,z,of(Kind.EBONY));set(l,b,d+1,4,z,of(Kind.EBONY));}
    }

    /** Any part of the clock a reader can put their hand on. */
    public static boolean clock(BlockPos rel){return rel.getX()==FACE.getX()&&rel.getZ()==FACE.getZ()&&rel.getY()>=0&&rel.getY()<=FACE.getY();}

    /** What a reader who stopped the clock sees: its pendulum still and its hands at twelve. The world's clock keeps going. */
    public static void stoppedView(ServerPlayer p,BlockPos b){
        for(var part:new BlockPos[]{FACE.below(),FACE}){var at=b.offset(part);var s=p.serverLevel().getBlockState(at);
            if(s.is(LiteraryRegistry.MASQUE.get()))p.connection.send(new ClientboundBlockUpdatePacket(at,s.setValue(MasqueBlock.STAGE,1)));}
    }

    static boolean built(ServerLevel l,BlockPos b){return MasqueBlock.is(l.getBlockState(b.offset(FACE)),Kind.CLOCK_FACE);}
}
