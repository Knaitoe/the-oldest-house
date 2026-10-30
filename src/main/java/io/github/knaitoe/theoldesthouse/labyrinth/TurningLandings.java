package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/**
 * 0.4.199, the turning landings: the Turning Landing (the third puzzle room) and the turning stair (its deep room).
 *
 * The owner: "A square stair landing turns with a heavy wall handle, connecting the entrance stairs to an otherwise
 * inaccessible door. Scraped floorboards show its movement." And, deep: "Several landings connect across a tall stairwell.
 * Turning one changes the available route through the others. Broad platforms and railings keep this a spatial puzzle,
 * with room to stop and think."
 *
 * A landing is five boards square on a post, railed on every side but its gaps; it turns a quarter at a time on its post,
 * and anyone standing on it turns with it. Around it, fixed ledges: the top of a stair, a bridge to a door, or a railed nook
 * to stand in and think. The ledges' boards are scraped in arcs where its corners swing past. A gap facing a nook only lets
 * you into the nook; railings everywhere else, and a cistern below the whole stairwell, so a mistake is never a fall onto
 * stone. Every arrangement can be turned back.
 *
 * The Turning Landing: one landing, three gaps, the stair to its south and the door's bridge to its north; the handle by the
 * stair top turns it a quarter. The turning stair: two landings of two gaps each, geared together. The handle by the lower
 * landing's stair turns it one way and the upper landing the other; a second handle, in the lower landing's west nook, turns
 * the upper landing alone. The way through needs the lower landing open south and east and the upper open east and north
 * at once, and the upper landing cannot be reached to be set from above.
 */
public final class TurningLandings {
    public static final String STATE="turning_landings_0199";
    /** Clockwise from north: the landing's sides, as bits of its gap mask. */
    static final Direction[] SIDES={Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
    public static final int N=1,E=2,S=4,W=8;
    /** A landing: its centre, the height of its boards, and its gaps before it has turned. */
    public record Landing(String id,int cx,int y,int cz,int base){}
    public static final Landing EARLY=new Landing("landing",0,3,-10,E|S|W);
    public static final Landing LOWER=new Landing("lower",-4,3,-9,N|E),UPPER=new Landing("upper",3,9,-17,N|E);
    /** The early room's handle by the stair top; the deep room's two. */
    public static final BlockPos EARLY_HANDLE=new BlockPos(2,4,-7),GEARED_HANDLE=new BlockPos(-2,4,-6),UPPER_HANDLE=new BlockPos(-9,4,-9);
    private static final int FLAGS=PuzzleRooms.FLAGS;
    private TurningLandings(){}

    /** A gap mask turned clockwise by quarters. */
    public static int gaps(int base,int rot){int out=0,r=Math.floorMod(rot,4);for(int i=0;i<4;i++)if((base&1<<i)!=0)out|=1<<((i+r)%4);return out;}
    public static int gaps(ServerLevel l,LabyrinthPlace place,Landing land){return gaps(land.base(),rot(l,place,land));}
    /** How many quarters a landing stands turned from how it was built, as it was left. */
    public static int rot(ServerLevel l,LabyrinthPlace place,Landing land){var s=LabyrinthData.get(l.getServer()).state(STATE);String key=place.id()+"/"+land.id();return s.contains(key)?s.getInt(key):start(l.getSeed(),place,land);}
    static void setRot(ServerLevel l,LabyrinthPlace place,Landing land,int rot){var d=LabyrinthData.get(l.getServer());var s=d.state(STATE);s.putInt(place.id()+"/"+land.id(),Math.floorMod(rot,4));d.setState(STATE,s);}
    /** How the world first sets them: never already through, and for the turning stair never so the lower landing's handle alone will do. */
    static int start(long seed,LabyrinthPlace place,Landing land){
        int k=Math.floorMod(seed^0x61c88647L,4);
        if(place==LabyrinthPlace.TURNING_LANDING)return k%2==0?0:2;
        int[][] pairs={{3,1},{3,3},{0,2},{0,3}};return pairs[k][land==LOWER?0:1];
    }
    /** Whether a landing's gaps reach the ways it must: the early room's stair and door; the turning stair's two at once. */
    public static boolean through(ServerLevel l,LabyrinthPlace place){
        if(place==LabyrinthPlace.TURNING_LANDING){int g=gaps(l,place,EARLY);return (g&N)!=0&&(g&S)!=0;}
        return gaps(l,place,LOWER)==(E|S)&&gaps(l,place,UPPER)==(N|E);
    }

    // ------------------------------------------------------------------ the landings
    /** Lays a landing's boards and rails as it stands turned: rails on the corners always, and on every side that is not a gap. */
    static void lay(ServerLevel l,BlockPos b,Landing land,int rot){
        int g=gaps(land.base(),rot);var rails=new ArrayList<BlockPos>();
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
            var at=new BlockPos(land.cx()+dx,land.y(),land.cz()+dz);
            Direction side=dz==-2&&Math.abs(dx)<2?Direction.NORTH:dz==2&&Math.abs(dx)<2?Direction.SOUTH:dx==2&&Math.abs(dz)<2?Direction.EAST:dx==-2&&Math.abs(dz)<2?Direction.WEST:null;
            boolean edge=Math.abs(dx)==2||Math.abs(dz)==2;
            boolean gap=side!=null&&(g&bit(side))!=0;
            // Where feet cross, the boards are worn pale; elsewhere they are plain.
            l.setBlock(b.offset(at),gap&&(dx==0||dz==0)?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.WORN,side.getOpposite()):Blocks.DARK_OAK_PLANKS.defaultBlockState(),FLAGS);
            if(edge&&!gap)rails.add(at.above());else l.setBlock(b.offset(at.above()),Blocks.AIR.defaultBlockState(),FLAGS);
        }
        fences(l,b,rails);
    }
    static int bit(Direction d){for(int i=0;i<4;i++)if(SIDES[i]==d)return 1<<i;return 0;}
    /** Rails: dark oak fences, joined to each other along a run, each standing on a board where nothing else holds it up. */
    static void fences(ServerLevel l,BlockPos b,Collection<BlockPos> cells){
        var set=new HashSet<>(cells);
        for(var c:cells){var under=b.offset(c.below());var s=l.getBlockState(under);if(s.isAir()||s.is(Blocks.WATER))l.setBlock(under,Blocks.DARK_OAK_PLANKS.defaultBlockState(),FLAGS);}
        for(var c:cells){var f=Blocks.DARK_OAK_FENCE.defaultBlockState();
            f=f.setValue(FenceBlock.NORTH,set.contains(c.north())).setValue(FenceBlock.SOUTH,set.contains(c.south())).setValue(FenceBlock.EAST,set.contains(c.east())).setValue(FenceBlock.WEST,set.contains(c.west()));
            l.setBlock(b.offset(c),f,FLAGS);}
    }
    /** Turns a landing a quarter (by +1 clockwise, -1 the other way), carrying whoever stands on it round with it. */
    static void turn(ServerLevel l,BlockPos b,LabyrinthPlace place,Landing land,int by){
        int rot=rot(l,place,land)+by;
        double cx=b.getX()+land.cx()+.5,cz=b.getZ()+land.cz()+.5,top=b.getY()+land.y()+1;
        var riders=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(!p.isSpectator()&&Math.abs(p.getX()-cx)<=2.5&&Math.abs(p.getZ()-cz)<=2.5&&p.getY()>=top-.3&&p.getY()<=top+1.2)riders.add(p);
        setRot(l,place,land,rot);lay(l,b,land,rot);
        for(var p:riders){
            double dx=p.getX()-cx,dz=p.getZ()-cz,nx=by>0?-dz:dz,nz=by>0?dx:-dx;
            // Anyone carried into a rail is set a step in from it.
            if(Math.abs(nx)>1.5)nx=Math.signum(nx)*1.4;if(Math.abs(nz)>1.5)nz=Math.signum(nz)*1.4;
            p.teleportTo(l,cx+nx,p.getY(),cz+nz,p.getYRot()+90*by,p.getXRot());
        }
        l.playSound(null,b.offset(land.cx(),land.y(),land.cz()),SoundEvents.GRINDSTONE_USE,SoundSource.BLOCKS,.9F,.5F);
        l.playSound(null,b.offset(land.cx(),land.y(),land.cz()),SoundEvents.WOOD_FALL,SoundSource.BLOCKS,1F,.6F);
    }
    static void handle(ServerLevel l,BlockPos b,BlockPos at,int stage,Direction facing){l.setBlock(b.offset(at),PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.HANDLE,facing,stage),FLAGS);}

    // ------------------------------------------------------------------ hands on
    static boolean use(ServerPlayer p,ServerLevel l,BlockPos b,LabyrinthPlace place,BlockPos pos,BlockState s){
        if(!PuzzleFittingBlock.is(s,PuzzleFittingBlock.Kind.HANDLE)){
            switch(s.getValue(PuzzleFittingBlock.KIND)){
                case SCRAPED->{PuzzleRooms.say(p,"The boards are scraped in an arc, pale, where the corner of something heavy has swung across them.");return true;}
                case WORN->{PuzzleRooms.say(p,"The boards are worn pale here, where feet cross.");return true;}
                default->{return false;}
            }
        }
        var rel=pos.subtract(b);boolean was=through(l,place);
        if(place==LabyrinthPlace.TURNING_LANDING){
            turn(l,b,place,EARLY,1);handle(l,b,EARLY_HANDLE,rot(l,place,EARLY),Direction.WEST);
            PuzzleRooms.say(p,"The handle is stiff. The whole landing comes round with it, a quarter turn, scraping.");
        }else if(rel.equals(GEARED_HANDLE)){
            turn(l,b,place,LOWER,1);turn(l,b,place,UPPER,-1);handle(l,b,GEARED_HANDLE,rot(l,place,LOWER),Direction.WEST);
            PuzzleRooms.say(p,"The landing below comes round a quarter. Up above, with a groan of gears, the other landing turns the other way.");
        }else if(rel.equals(UPPER_HANDLE)){
            turn(l,b,place,UPPER,1);handle(l,b,UPPER_HANDLE,rot(l,place,UPPER),Direction.EAST);
            PuzzleRooms.say(p,"A chain runs up the wall from this handle. Up above, the other landing comes round a quarter on its own.");
        }else return false;
        if(!was&&through(l,place))PuzzleRooms.solve(l,place,place==LabyrinthPlace.TURNING_LANDING?"The landing settles into line with the far door with a knock.":"Both landings knock into line. There is a way up through them now.");
        return true;
    }

    // ------------------------------------------------------------------ the early room
    public static void buildEarly(ServerLevel l,BlockPos b){
        var place=LabyrinthPlace.TURNING_LANDING;var box=place.room();
        var wall=Blocks.STONE_BRICKS.defaultBlockState();var floor=Blocks.MOSSY_STONE_BRICKS.defaultBlockState();var ceiling=Blocks.DARK_OAK_PLANKS.defaultBlockState();
        PuzzleRooms.shell(l,b,box,wall,floor,ceiling);
        var water=Blocks.WATER.defaultBlockState();var planks=Blocks.DARK_OAK_PLANKS.defaultBlockState();var log=Blocks.DARK_OAK_LOG.defaultBlockState();
        // The floor by the door, and the cistern under the whole stairwell beyond it.
        PuzzleRooms.fill(l,b,-4,-3,-3,4,-2,0,wall);PuzzleRooms.fill(l,b,-4,-1,-3,4,-1,0,planks);
        PuzzleRooms.fill(l,b,-4,-3,-17,4,-2,-4,water);
        // Plaster above the stone, from the landing's height up.
        PuzzleRooms.fill(l,b,-5,6,-18,-5,11,0,Blocks.CALCITE.defaultBlockState());PuzzleRooms.fill(l,b,5,6,-18,5,11,0,Blocks.CALCITE.defaultBlockState());PuzzleRooms.fill(l,b,-4,6,-18,4,11,-18,Blocks.CALCITE.defaultBlockState());
        // The stair up from the door, on stone; the ledge at its top, scraped where the landing swings past.
        for(int i=0;i<3;i++)for(int x=-1;x<=1;x++){PuzzleRooms.fill(l,b,x,-3,-4-i,x,i-1,-4-i,wall);l.setBlock(b.offset(x,i,-4-i),Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH),FLAGS);}
        for(int x=-1;x<=1;x++){PuzzleRooms.fill(l,b,x,-3,-7,x,2,-7,wall);l.setBlock(b.offset(x,3,-7),x==0?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.SCRAPED,Direction.NORTH):planks,FLAGS);}
        // The bridge to the door on its north side, and a nook either side to stand in.
        for(int z=-13;z>=-17;z--)for(int x=-1;x<=1;x++)l.setBlock(b.offset(x,3,z),z==-13&&x==0?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.SCRAPED,Direction.SOUTH):planks,FLAGS);
        for(int side:new int[]{-1,1})for(int x=3;x<=4;x++)for(int z=-9;z>=-11;z--)l.setBlock(b.offset(side*x,3,z),x==3&&z==-10?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.SCRAPED,side>0?Direction.WEST:Direction.EAST):planks,FLAGS);
        // The post the landing turns on, and the handle's post by the stair top.
        PuzzleRooms.fill(l,b,EARLY.cx(),-3,EARLY.cz(),EARLY.cx(),EARLY.y()-1,EARLY.cz(),log);
        PuzzleRooms.fill(l,b,EARLY_HANDLE.getX(),-3,EARLY_HANDLE.getZ(),EARLY_HANDLE.getX(),EARLY_HANDLE.getY()-1,EARLY_HANDLE.getZ(),log);
        var rails=new ArrayList<BlockPos>();
        for(int i=0;i<3;i++){rails.add(new BlockPos(-2,i+1,-4-i));rails.add(new BlockPos(2,i+1,-4-i));}
        rails.add(new BlockPos(-2,4,-7));
        for(int z=-13;z>=-17;z--){rails.add(new BlockPos(-2,4,z));rails.add(new BlockPos(2,4,z));}
        for(int side:new int[]{-1,1})for(int x=3;x<=4;x++){rails.add(new BlockPos(side*x,4,-8));rails.add(new BlockPos(side*x,4,-12));}
        for(int x=-4;x<=4;x++)if(Math.abs(x)>=2&&x!=3)rails.add(new BlockPos(x,0,-3));
        fences(l,b,rails);
        // A ladder out of the cistern, and somewhere to sit in the east nook.
        for(int y=-3;y<=-1;y++)l.setBlock(b.offset(3,y,-4),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH),FLAGS);
        l.setBlock(b.offset(4,4,-10),Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST),FLAGS);
        l.setBlock(b.offset(4,4,-9),Blocks.POTTED_FERN.defaultBlockState(),FLAGS);
        // Light: a lantern on a long chain over the landing, and one in each nook.
        PuzzleRooms.fill(l,b,0,9,-10,0,11,-10,Blocks.CHAIN.defaultBlockState());l.setBlock(b.offset(0,8,-10),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),FLAGS);
        for(int side:new int[]{-1,1})l.setBlock(b.offset(side*4,5,-11),Blocks.LANTERN.defaultBlockState(),FLAGS);
        l.setBlock(b.offset(-4,4,-11),Blocks.DARK_OAK_FENCE.defaultBlockState(),FLAGS);l.setBlock(b.offset(4,4,-11),Blocks.DARK_OAK_FENCE.defaultBlockState(),FLAGS);
        l.setBlock(b.offset(-4,5,-11),Blocks.LANTERN.defaultBlockState(),FLAGS);l.setBlock(b.offset(4,5,-11),Blocks.LANTERN.defaultBlockState(),FLAGS);
        LabyrinthBuilder.entrance(l,b,wall,planks,ceiling);
        LabyrinthBuilder.doors(l,b,place);
        lay(l,b,EARLY,rot(l,place,EARLY));handle(l,b,EARLY_HANDLE,rot(l,place,EARLY),Direction.WEST);
    }

    // ------------------------------------------------------------------ the deep room
    public static void buildDeep(ServerLevel l,BlockPos b){
        var place=LabyrinthPlace.TURNING_STAIR;var box=place.room();
        var wall=Blocks.STONE_BRICKS.defaultBlockState();var floor=Blocks.MOSSY_STONE_BRICKS.defaultBlockState();var ceiling=Blocks.DARK_OAK_PLANKS.defaultBlockState();
        PuzzleRooms.shell(l,b,box,wall,floor,ceiling);
        var water=Blocks.WATER.defaultBlockState();var planks=Blocks.DARK_OAK_PLANKS.defaultBlockState();var log=Blocks.DARK_OAK_LOG.defaultBlockState();
        PuzzleRooms.fill(l,b,-9,-3,-2,9,-2,0,wall);PuzzleRooms.fill(l,b,-9,-1,-2,9,-1,0,planks);
        PuzzleRooms.fill(l,b,-9,-3,-21,9,-2,-3,water);
        PuzzleRooms.fill(l,b,-10,6,-22,-10,14,0,Blocks.CALCITE.defaultBlockState());PuzzleRooms.fill(l,b,10,6,-22,10,14,0,Blocks.CALCITE.defaultBlockState());PuzzleRooms.fill(l,b,-9,6,-22,9,14,-22,Blocks.CALCITE.defaultBlockState());
        var rails=new ArrayList<BlockPos>();
        // The stair up from the door to the lower landing's south ledge.
        for(int i=0;i<3;i++)for(int x=-5;x<=-3;x++){PuzzleRooms.fill(l,b,x,-3,-3-i,x,i-1,-3-i,wall);l.setBlock(b.offset(x,i,-3-i),Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH),FLAGS);
            rails.add(new BlockPos(-6,i+1,-3-i));rails.add(new BlockPos(-2,i+1,-3-i));}
        for(int x=-5;x<=-3;x++){PuzzleRooms.fill(l,b,x,-3,-6,x,2,-6,wall);l.setBlock(b.offset(x,3,-6),x==-4?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.SCRAPED,Direction.NORTH):planks,FLAGS);}
        rails.add(new BlockPos(-6,4,-6));PuzzleRooms.fill(l,b,GEARED_HANDLE.getX(),-3,GEARED_HANDLE.getZ(),GEARED_HANDLE.getX(),GEARED_HANDLE.getY()-1,GEARED_HANDLE.getZ(),log);
        // The lower landing's west nook, with the second handle; its north nook.
        for(int x=-9;x<=-7;x++)for(int z=-8;z>=-10;z--)l.setBlock(b.offset(x,3,z),x==-7&&z==-9?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.SCRAPED,Direction.EAST):planks,FLAGS);
        for(int x=-9;x<=-7;x++){rails.add(new BlockPos(x,4,-7));rails.add(new BlockPos(x,4,-11));}
        for(int x=-5;x<=-3;x++){l.setBlock(b.offset(x,3,-12),x==-4?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.SCRAPED,Direction.SOUTH):planks,FLAGS);rails.add(new BlockPos(x,4,-13));}
        rails.add(new BlockPos(-6,4,-12));rails.add(new BlockPos(-2,4,-12));
        // East from the lower landing: a ledge, a walkway, and the foot of the second stair.
        for(int x=-1;x<=9;x++)for(int z=-8;z>=-10;z--)l.setBlock(b.offset(x,3,z),x==-1&&z==-9?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.SCRAPED,Direction.WEST):planks,FLAGS);
        for(int x=-1;x<=9;x++){rails.add(new BlockPos(x,4,-7));if(x<=6)rails.add(new BlockPos(x,4,-11));}
        for(int x:new int[]{1,5,8})PuzzleRooms.fill(l,b,x,-3,-9,x,2,-9,log);
        // The second stair, north up the east wall, to a landing beside the upper landing's east side.
        for(int i=0;i<5;i++)for(int x=7;x<=9;x++){PuzzleRooms.fill(l,b,x,-3,-11-i,x,3+i,-11-i,wall);l.setBlock(b.offset(x,4+i,-11-i),Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH),FLAGS);
            rails.add(new BlockPos(6,5+i,-11-i));}
        for(int x=6;x<=9;x++)for(int z=-16;z>=-18;z--)l.setBlock(b.offset(x,9,z),x==6&&z==-17?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.SCRAPED,Direction.WEST):planks,FLAGS);
        for(int x=6;x<=9;x++)rails.add(new BlockPos(x,10,-19));rails.add(new BlockPos(6,10,-15));
        for(int x:new int[]{7,9})PuzzleRooms.fill(l,b,x,-3,-18,x,8,-18,log);
        // The upper landing's north ledge and the bridge to the door; its south and west nooks.
        for(int x=2;x<=4;x++)for(int z=-20;z>=-21;z--)l.setBlock(b.offset(x,9,z),x==3&&z==-20?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.SCRAPED,Direction.SOUTH):planks,FLAGS);
        for(int z=-20;z>=-21;z--){rails.add(new BlockPos(1,10,z));rails.add(new BlockPos(5,10,z));}
        for(int x=2;x<=4;x++){l.setBlock(b.offset(x,9,-14),x==3?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.SCRAPED,Direction.NORTH):planks,FLAGS);rails.add(new BlockPos(x,10,-13));}
        rails.add(new BlockPos(1,10,-14));rails.add(new BlockPos(5,10,-14));
        for(int x=-1;x<=0;x++)for(int z=-16;z>=-18;z--)l.setBlock(b.offset(x,9,z),x==0&&z==-17?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.SCRAPED,Direction.EAST):planks,FLAGS);
        for(int x=-1;x<=0;x++){rails.add(new BlockPos(x,10,-15));rails.add(new BlockPos(x,10,-19));}
        for(int z=-16;z>=-18;z--)rails.add(new BlockPos(-2,10,z));
        fences(l,b,rails);
        // The posts the landings turn on.
        PuzzleRooms.fill(l,b,LOWER.cx(),-3,LOWER.cz(),LOWER.cx(),LOWER.y()-1,LOWER.cz(),log);
        PuzzleRooms.fill(l,b,UPPER.cx(),-3,UPPER.cz(),UPPER.cx(),UPPER.y()-1,UPPER.cz(),log);
        // A ladder out of the cistern; lanterns on chains over each landing.
        for(int y=-3;y<=-1;y++)l.setBlock(b.offset(2,y,-3),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH),FLAGS);
        PuzzleRooms.fill(l,b,LOWER.cx(),8,LOWER.cz(),LOWER.cx(),14,LOWER.cz(),Blocks.CHAIN.defaultBlockState());l.setBlock(b.offset(LOWER.cx(),7,LOWER.cz()),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),FLAGS);
        l.setBlock(b.offset(UPPER.cx(),14,UPPER.cz()),Blocks.CHAIN.defaultBlockState(),FLAGS);l.setBlock(b.offset(UPPER.cx(),13,UPPER.cz()),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),FLAGS);
        for(var at:new BlockPos[]{new BlockPos(-8,4,-10),new BlockPos(9,10,-18),new BlockPos(-1,10,-18)})l.setBlock(b.offset(at),Blocks.LANTERN.defaultBlockState(),FLAGS);
        LabyrinthBuilder.entrance(l,b,wall,planks,ceiling);
        LabyrinthBuilder.doors(l,b,place);
        lay(l,b,LOWER,rot(l,place,LOWER));lay(l,b,UPPER,rot(l,place,UPPER));
        handle(l,b,GEARED_HANDLE,rot(l,place,LOWER),Direction.WEST);handle(l,b,UPPER_HANDLE,rot(l,place,UPPER),Direction.EAST);
    }
}
