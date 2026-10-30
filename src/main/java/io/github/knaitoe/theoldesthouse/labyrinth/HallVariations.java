package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/** Small changes of width, alignment and ceiling rhythm; authored exits stay at fixed coordinates. */
public final class HallVariations {
    private HallVariations() {}
    public static boolean domestic(LabyrinthPlace p) {
        return p==LabyrinthPlace.ALCOVE_HALL||p==LabyrinthPlace.OFFSET_HALL||p==LabyrinthPlace.SERVICE_LANDING||plain(p);
    }
    /** 0.4.197: the four halls that are just hallways; 0.4.199: and the vaulted and crumbled halls. */
    public static boolean plain(LabyrinthPlace p) {
        return p==LabyrinthPlace.JOGGED_HALL||p==LabyrinthPlace.FORKED_HALL||p==LabyrinthPlace.PILLAR_HALL||p==LabyrinthPlace.ZIGZAG_HALL
                ||p==LabyrinthPlace.VAULTED_HALL||p==LabyrinthPlace.CRUMBLED_HALL;
    }
    /** 0.4.199: the vaulted hall's ceiling, fifteen blocks over the floor, and the chandeliers hung down the middle of it. */
    public static final int VAULT=15;
    static final int[] CHANDELIERS={-9,-17,-25,-33};
    /** The vaulted hall's pilasters, up both long walls. */
    static final int[] PILASTERS={-6,-12,-18,-24,-30,-36};
    /** The ceiling's height over the floor (the last air block). */
    public static int roof(LabyrinthPlace p) {
        if(p==LabyrinthPlace.VAULTED_HALL)return VAULT;
        return domestic(p)?4:p==LabyrinthPlace.STONE_BEND?5:6;
    }
    /**
     * 0.4.199: the crumbled hall. Its floor has fallen three blocks into a cellar between {@link #PIT_NEAR} and {@link #PIT_FAR}
     * (never deep enough to hurt), and what is left of the boards crosses it. The pit's one ladder climbs back to the door
     * they came in by, so a missed jump costs a climb and another try; nothing in the pit reaches the far side.
     */
    public static final int PIT_NEAR=-6,PIT_FAR=-30,PIT_FLOOR=-4;
    /** The crossing, in order: each piece of floor left standing, x0..x1 by z0..z1, with the height a reader stands at. */
    public record Piece(int x0,int x1,int z0,int z1,int y) {}
    public static final java.util.List<Piece> CROSSING=java.util.List.of(
            new Piece(-2,2,-5,0,0),         // the near landing, where the boards still hold
            new Piece(-2,-2,-6,-6,0),       // a stub of board at the broken edge
            new Piece(-1,0,-8,-8,0),
            new Piece(1,2,-11,-10,0),
            new Piece(2,2,-16,-13,0),       // a joist, walked along its length
            new Piece(-1,0,-19,-18,0),
            new Piece(-2,-2,-21,-21,1),     // a beam end tipped up onto the rubble: one step up
            new Piece(0,1,-23,-23,0),
            new Piece(-1,1,-27,-26,0),      // the long jump: two blocks across
            new Piece(1,1,-29,-29,0),
            new Piece(-2,2,-38,-31,0));     // the far landing
    /** The pillar hall's columns, down the middle of its floor. */
    static final int[] PILLARS={-9,-15,-21,-27,-33,-39};
    public static boolean added(LabyrinthPlace p) { return domestic(p)||p==LabyrinthPlace.STONE_ARCADE||p==LabyrinthPlace.STONE_BEND||p==LabyrinthPlace.STONE_LANDING; }
    public static Set<BlockPos> floor(LabyrinthPlace p) {
        Set<BlockPos> out=new HashSet<>();
        switch(p) {
            case ALCOVE_HALL -> {
                LabyrinthHalls.rectangle(out,-1,1,-41,0); LabyrinthHalls.rectangle(out,-4,4,-21,-13);
                LabyrinthHalls.rectangle(out,-7,-3,-18,-16); LabyrinthHalls.rectangle(out,-3,3,-33,-29);
            }
            case OFFSET_HALL -> {
                LabyrinthHalls.rectangle(out,-1,1,-17,0); LabyrinthHalls.rectangle(out,-1,5,-18,-14);
                LabyrinthHalls.rectangle(out,3,5,-39,-16); LabyrinthHalls.rectangle(out,3,8,-28,-23);
            }
            case SERVICE_LANDING -> {
                LabyrinthHalls.rectangle(out,-1,1,-30,0); LabyrinthHalls.rectangle(out,-8,8,-22,-10);
                LabyrinthHalls.rectangle(out,-10,-7,-17,-15);
            }
            case JOGGED_HALL -> {
                LabyrinthHalls.rectangle(out,-1,1,-27,0); LabyrinthHalls.rectangle(out,-2,-2,-15,-13);
                LabyrinthHalls.rectangle(out,-1,3,-31,-27); LabyrinthHalls.rectangle(out,1,3,-55,-28);
            }
            case FORKED_HALL -> {
                LabyrinthHalls.rectangle(out,-1,1,-16,0); LabyrinthHalls.rectangle(out,-11,11,-19,-17);
                LabyrinthHalls.rectangle(out,-11,-9,-30,-17); LabyrinthHalls.rectangle(out,9,11,-30,-17);
            }
            case PILLAR_HALL -> LabyrinthHalls.rectangle(out,-2,2,-45,0);
            case ZIGZAG_HALL -> {
                LabyrinthHalls.rectangle(out,-1,1,-13,0); LabyrinthHalls.rectangle(out,-1,9,-15,-13);
                LabyrinthHalls.rectangle(out,7,9,-28,-13); LabyrinthHalls.rectangle(out,-1,9,-30,-28);
                LabyrinthHalls.rectangle(out,-1,1,-43,-28);
            }
            case VAULTED_HALL -> {
                LabyrinthHalls.rectangle(out,-1,1,-3,0); LabyrinthHalls.rectangle(out,-3,3,-40,-4);
                LabyrinthHalls.rectangle(out,-1,1,-41,-41);
            }
            // Only where a reader can stand at the floor's height: the landings and the boards left over the pit.
            case CRUMBLED_HALL -> { for(var piece:CROSSING)if(piece.y()==0)LabyrinthHalls.rectangle(out,piece.x0(),piece.x1(),piece.z0(),piece.z1()); }
            case STONE_ARCADE -> {
                LabyrinthHalls.rectangle(out,-2,2,-39,0);
                for(int z=-10;z>=-34;z-=8)LabyrinthHalls.rectangle(out,-4,4,z-2,z+2);
            }
            case STONE_BEND -> {
                LabyrinthHalls.rectangle(out,-2,2,-20,0); LabyrinthHalls.rectangle(out,-11,2,-21,-17);
                LabyrinthHalls.rectangle(out,-11,-7,-35,-18);
            }
            case STONE_LANDING -> {
                LabyrinthHalls.rectangle(out,-2,2,-27,0); LabyrinthHalls.rectangle(out,-10,10,-23,-8);
                LabyrinthHalls.rectangle(out,9,11,-18,-14);
            }
            default -> throw new IllegalArgumentException(p.id());
        }
        return Set.copyOf(out);
    }
    /** The hall's whole footprint between its walls: the floor, and for the crumbled hall the pit as well. */
    public static Set<BlockPos> shell(LabyrinthPlace p) {
        if(p!=LabyrinthPlace.CRUMBLED_HALL)return floor(p);
        Set<BlockPos> out=new HashSet<>();LabyrinthHalls.rectangle(out,-2,2,-38,0);return Set.copyOf(out);
    }
    /** Whether a cell is open pit in the crumbled hall: between the landings and not a board left standing. */
    public static boolean pit(int x,int z) {
        if(x<-2||x>2||z>PIT_NEAR||z<PIT_FAR)return false;
        for(var piece:CROSSING)if(x>=piece.x0()&&x<=piece.x1()&&z>=piece.z0()&&z<=piece.z1())return false;
        return true;
    }
    private static void set(ServerLevel l,BlockPos at,BlockState state) { BuildBlocks.set(l,at,state,LabyrinthBuilder.flags()); }
    private static BlockState slab(boolean stone) {
        return (stone?Blocks.STONE_BRICK_SLAB:Blocks.SPRUCE_SLAB).defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP);
    }
    public static void buildDomestic(ServerLevel l,BlockPos b,LabyrinthPlace p) {
        var open=shell(p);var box=p.room();int roof=roof(p);
        // 0.4.199: the vaulted hall's ceiling is dark coffered wood, lost in the dark above its lamps.
        BlockState ceiling=(p==LabyrinthPlace.VAULTED_HALL?Blocks.DARK_OAK_PLANKS:Blocks.WHITE_CONCRETE).defaultBlockState();
        for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=-1;y<=roof+1;y++) {
            boolean inside=open.contains(new BlockPos(x,0,z));
            BlockState s=!inside?(y==0?Blocks.SPRUCE_PLANKS.defaultBlockState():LabyrinthDomestic.WALL)
                    :y==-1?Blocks.SPRUCE_PLANKS.defaultBlockState():y==roof+1?ceiling:Blocks.AIR.defaultBlockState();
            set(l,b.offset(x,y,z),s);
        }
        if(p==LabyrinthPlace.CRUMBLED_HALL)crumble(l,b);
        finish(l,b,p);
        if(p==LabyrinthPlace.VAULTED_HALL)vault(l,b);
        LabyrinthBuilder.entrance(l,b,LabyrinthDomestic.WALL,LabyrinthDomestic.FLOOR,LabyrinthDomestic.CEILING);
        LabyrinthBuilder.doors(l,b,p);
    }
    public static void buildStone(ServerLevel l,BlockPos b,LabyrinthPlace p) {
        var open=floor(p);var box=p.room();int roof=p==LabyrinthPlace.STONE_BEND?5:6;
        for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=-1;y<=roof+1;y++) {
            boolean inside=open.contains(new BlockPos(x,0,z));
            BlockState s=!inside||y==roof+1?Blocks.STONE_BRICKS.defaultBlockState()
                    :y==-1?(Math.floorMod(x+z,7)==0?Blocks.POLISHED_DIORITE:Blocks.POLISHED_ANDESITE).defaultBlockState():Blocks.AIR.defaultBlockState();
            set(l,b.offset(x,y,z),s);
        }
        finish(l,b,p);
    }
    public static void finish(ServerLevel l,BlockPos b,LabyrinthPlace p) {
        boolean stone=!domestic(p);int roof=roof(p);boolean vaulted=p==LabyrinthPlace.VAULTED_HALL;
        var open=shell(p);
        // Ceiling ribs and a few recessed bays vary the silhouette without reducing head clearance.
        for(BlockPos cell:open) {
            if(nearDoor(p,cell))continue;
            if(!vaulted&&Math.floorMod(cell.getZ(),8)==3) {
                var at=b.offset(cell).above(roof);
                if(BuildBlocks.state(l,at).isAir())set(l,at,slab(stone));
            }
            for(Direction side:Direction.Plane.HORIZONTAL) {
                BlockPos wall=cell.relative(side);
                if(open.contains(wall)||nearDoor(p,wall))continue;
                var at=b.offset(wall);
                if(l.getBlockEntity(at)!=null)continue;
                set(l,at,(stone?Blocks.CHISELED_STONE_BRICKS:Blocks.STRIPPED_SPRUCE_WOOD).defaultBlockState());
                if(!stone&&Math.floorMod(wall.getZ(),11)==0) {
                    // Glass has a real, dark back, rather than opening onto uncarved space.
                    set(l,at.above(),Blocks.GRAY_STAINED_GLASS.defaultBlockState());
                    set(l,at.above(2),Blocks.GRAY_STAINED_GLASS.defaultBlockState());
                    set(l,at.relative(side).above(),Blocks.DEEPSLATE.defaultBlockState());
                    set(l,at.relative(side).above(2),Blocks.DEEPSLATE.defaultBlockState());
                }
            }
        }
        for(int z=-6;!vaulted&&z>p.room().minZ()+4;z-=10) {
            int x=p==LabyrinthPlace.OFFSET_HALL&&z<-18?4:p==LabyrinthPlace.STONE_BEND&&z<-21?-9:p==LabyrinthPlace.JOGGED_HALL&&z<-28?2:p==LabyrinthPlace.ZIGZAG_HALL&&z<-15&&z>-28?8:0;
            if(open.contains(new BlockPos(x,0,z)))LabyrinthBuilder.hangLantern(l,b.offset(x,roof,z),false);
        }
        // 0.4.197: a lamp down each arm of the fork, so neither way is the dark one.
        if(p==LabyrinthPlace.FORKED_HALL)for(int x:new int[]{-10,10})LabyrinthBuilder.hangLantern(l,b.offset(x,roof,-24),false);
        // 0.4.197: the pillar hall's columns, each a plain post from floor to ceiling, an aisle either side.
        if(p==LabyrinthPlace.PILLAR_HALL)for(int z:PILLARS)for(int y=0;y<=roof;y++)set(l,b.offset(0,y,z),Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        if(p==LabyrinthPlace.SERVICE_LANDING||p==LabyrinthPlace.STONE_LANDING) {
            // Seating is supported at the side of a broad landing, with no new supplies.
            // Each bench's back is to its own wall, so it seats people facing into the landing.
            int x=p==LabyrinthPlace.SERVICE_LANDING?7:-9;Direction back=x>0?Direction.EAST:Direction.WEST;
            for(int z=-12;z>=-15;z--)set(l,b.offset(x,0,z),LabyrinthBuilder.stairs(stone?Blocks.STONE_BRICK_STAIRS:Blocks.SPRUCE_STAIRS,back));
        }
    }
    /**
     * 0.4.199: the vaulted hall. A low neck at each end opens into the nave, whose plaster rises fifteen blocks between spruce
     * pilasters to a dark coffered ceiling crossed by beams; a picture rail at the height of an ordinary ceiling, a clerestory of
     * dark glass high up, and four lamps hung on long chains, so the light stays low and the ceiling stays in shadow.
     */
    static void vault(ServerLevel l,BlockPos b) {
        var wall=LabyrinthDomestic.WALL;var log=Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();var rail=Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState();
        // The necks keep an ordinary ceiling: the room is only seen once through it.
        for(int x=-1;x<=1;x++)for(int z:new int[]{0,-1,-2,-3,-41})for(int y=5;y<=VAULT;y++)set(l,b.offset(x,y,z),wall);
        for(int side:new int[]{-4,4}) {
            for(int z=-4;z>=-40;z--) {
                if(z==-20||z==-21||z==-22)continue;
                set(l,b.offset(side,3,z),rail);set(l,b.offset(side,VAULT,z),rail);
                // The clerestory between the pilasters, with a real dark back.
                if(Math.floorMod(z,6)!=0)for(int y=10;y<=12;y++){set(l,b.offset(side,y,z),Blocks.GRAY_STAINED_GLASS.defaultBlockState());set(l,b.offset(side+Integer.signum(side),y,z),Blocks.DEEPSLATE.defaultBlockState());}
            }
            for(int z:PILASTERS)for(int y=0;y<=VAULT;y++)set(l,b.offset(side,y,z),log);
        }
        // Beams across the nave from pilaster to pilaster, under the coffers.
        var beam=Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,Direction.Axis.X);
        for(int z:PILASTERS)for(int x=-3;x<=3;x++)set(l,b.offset(x,VAULT,z),beam);
        // The lamps on their chains, ten blocks of chain each, so the lamp hangs at the height of an ordinary hall's.
        for(int z:CHANDELIERS){for(int y=6;y<=VAULT;y++)set(l,b.offset(0,y,z),Blocks.CHAIN.defaultBlockState());LabyrinthBuilder.hangLantern(l,b.offset(0,5,z),false);}
    }
    /**
     * 0.4.199: the crumbled hall's fallen floor. The pit is three blocks deep, so a fall never hurts; its walls are a cellar's,
     * with the old joists' ends in them, and its floor is rubble and fallen boards. What is left of the floor stands on posts.
     */
    static void crumble(ServerLevel l,BlockPos b) {
        var post=Blocks.SPRUCE_FENCE.defaultBlockState();var board=Blocks.SPRUCE_PLANKS.defaultBlockState();
        // The cellar's enclosure: rubble floor, foundation walls, solid under both landings.
        for(int x=-3;x<=3;x++)for(int z=PIT_NEAR+1;z>=PIT_FAR-1;z--)for(int y=PIT_FLOOR-1;y<=-1;y++) {
            boolean open=pit(x,z);
            if(x==-3||x==3||z==PIT_NEAR+1||z==PIT_FAR-1||y<=PIT_FLOOR) {
                if(y==-1&&!(x==-3||x==3))continue; // the landings' own boards
                BlockState s=y<=PIT_FLOOR?(Math.floorMod(x*7+z*3,5)==0?Blocks.COARSE_DIRT:Math.floorMod(x+z,3)==0?Blocks.MOSSY_COBBLESTONE:Blocks.COBBLESTONE).defaultBlockState()
                        :(y==-1&&Math.floorMod(z,3)==0)?Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,Direction.Axis.X)
                        :Blocks.STONE_BRICKS.defaultBlockState();
                set(l,b.offset(x,y,z),s);
                continue;
            }
            set(l,b.offset(x,y,z),open?Blocks.AIR.defaultBlockState():y==-1?board:post);
        }
        // The joist walked along its length, and the beam end tipped up onto a heap of rubble.
        var joist=Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,Direction.Axis.Z);
        for(int z=-13;z>=-16;z--)set(l,b.offset(2,-1,z),joist);
        for(int z=-14;z>=-15;z--)for(int y=-3;y<=-2;y++)set(l,b.offset(2,y,z),Blocks.AIR.defaultBlockState());
        for(int y=-3;y<=-1;y++)set(l,b.offset(-2,y,-21),(y==-1?Blocks.MOSSY_COBBLESTONE:Blocks.COBBLESTONE).defaultBlockState());
        set(l,b.offset(-2,0,-21),Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,Direction.Axis.X));
        // Fallen boards on the cellar floor, never against a post or a wall, so nothing in the pit is a step up and out.
        for(int[] c:new int[][]{{0,-12},{-1,-15},{1,-20},{-1,-24},{0,-29}})
            if(pit(c[0],c[1]))set(l,b.offset(c[0],PIT_FLOOR+1,c[1]),Blocks.SPRUCE_SLAB.defaultBlockState());
        // Torches low on the cellar walls, so the pit is seen to be shallow and no lamp has to hang down into the jumps.
        for(int z:new int[]{-9,-15,-27})set(l,b.offset(-2,-2,z),Blocks.WALL_TORCH.defaultBlockState().setValue(net.minecraft.world.level.block.WallTorchBlock.FACING,Direction.EAST));
        for(int z:new int[]{-12,-19,-24})set(l,b.offset(2,-2,z),Blocks.WALL_TORCH.defaultBlockState().setValue(net.minecraft.world.level.block.WallTorchBlock.FACING,Direction.WEST));
        // A lamp over the far landing too, so the door they jumped for is seen from the near one.
        LabyrinthBuilder.hangLantern(l,b.offset(0,4,-34),false);
        // The way back up after a missed jump: a ladder on the near wall, to the door they came in by.
        for(int y=PIT_FLOOR+1;y<=-1;y++)set(l,b.offset(0,y,PIT_NEAR),Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING,Direction.NORTH));
    }
    private static boolean nearDoor(LabyrinthPlace p,BlockPos cell) {
        for(var door:p.doors())if(Math.abs(cell.getX()-door.rel().getX())+Math.abs(cell.getZ()-door.rel().getZ())<4)return true;
        return false;
    }
}
