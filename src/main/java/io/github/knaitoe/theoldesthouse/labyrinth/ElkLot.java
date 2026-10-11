package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.labyrinth.RoadhouseBlock.Kind;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The Last Stop (0.4.78): the elk lot rebuilt whole as a roadside bar at night. A two-lane highway crosses the scene in
 * front of the reader's door; across it a gravel lot, the bar (porch, booths, a pool table, a jukebox, the counter and back
 * bar, the noticeboard), three pickups with their headlights left on, the field fence and its broken gap, a ditch, and the
 * field. The field repeats itself every {@value #PERIOD} blocks from z {@value #FIELD_START} north, so a reader carried back
 * one period at {@value #LOOP_AT} stands in a place identical to the one they left; the fog hides the rest. Nothing generic
 * is laid over it: polish, composition and review passes skip the lot. Saved lots are re-carved once ({@value #STATE}).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ElkLot {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.ELK_LOT;
    public static final String STATE="elk_lot_0478";
    /** The till receipt by the register (the source) and the clipping on the noticeboard (the ending). */
    public static final BlockPos REGISTER=new BlockPos(-20,1,-25),BOARD=new BlockPos(-20,1,-15);
    /** Where the old lot kept its source and ending, read until a saved lot is re-carved. */
    public static final BlockPos OLD_SOURCE=new BlockPos(-18,1,-12),OLD_ENDING=new BlockPos(0,0,-5);
    public static final BlockPos JUKEBOX=new BlockPos(-14,0,-17),DARTS=new BlockPos(-14,2,-21),POOL_L=new BlockPos(-26,0,-19),POOL_R=new BlockPos(-25,0,-19);
    public static final BlockPos FRONT_DOOR=new BlockPos(-23,0,-14),SIDE_DOOR=new BlockPos(-13,0,-25),PORCH=new BlockPos(-23,0,-12);
    /** Where the field begins, how long its repeat is, and where a reader is carried one repeat back. */
    public static final int FIELD_START=-45,PERIOD=34,LOOP_AT=-96,FENCE=-40,FIELD=-44;
    /** The gap the elk breaks through, and the broken post and dropped cap that repeat in the field. */
    public static final int GAP_X0=0,GAP_X1=2,POSTS_X=-2,BROKEN_POST=14;
    /** Each pickup: the driver's left front corner (all point north toward the field), and its paint. */
    public static final int[][] PICKUPS={{-6,-22,0},{-1,-23,1},{4,-21,2}};
    public static final int STRUCK=1;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private ElkLot(){}

    public static @Nullable BlockPos base(net.minecraft.server.MinecraftServer s){var origin=HouseSavedData.get(s).houseOrigin();return origin==null?null:LabyrinthPlaces.base(origin,PLACE);}
    /** The struck pickup's dented door, on the side the elk comes in from. */
    public static BlockPos dent(){var p=PICKUPS[STRUCK];return new BlockPos(p[0],0,p[1]+2);}
    /** The field's position within its repeat, 0 at its south edge; -1 south of the field. */
    public static int phase(int z){return z>FIELD_START?-1:Math.floorMod(FIELD_START-z,PERIOD);}
    private static int hash(int x,int z){int h=x*73856093^z*19349663;h^=h>>>13;h*=0x5bd1e995;return (h^(h>>>15))&0x7fffffff;}

    // ------------------------------------------------------------------------------------------------ building
    private static final class Site {
        final ServerLevel l;final BlockPos b;final List<BlockPos> connect=new ArrayList<>();
        Site(ServerLevel l,BlockPos b){this.l=l;this.b=b;}
        void set(int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);if(s.getBlock() instanceof FenceBlock||s.getBlock() instanceof IronBarsBlock||s.getBlock() instanceof WallBlock)connect.add(b.offset(x,y,z));}
        void set(int x,int y,int z,Block s){set(x,y,z,s.defaultBlockState());}
        void set(BlockPos r,BlockState s){set(r.getX(),r.getY(),r.getZ(),s);}
        void fill(int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(x,y,z,s);}
        void fill(int x0,int y0,int z0,int x1,int y1,int z1,Block s){fill(x0,y0,z0,x1,y1,z1,s.defaultBlockState());}
        void r(int x,int y,int z,Kind k,Direction f){set(x,y,z,RoadhouseBlock.of(k,f));}
        void r(int x,int y,int z,Kind k,Direction f,int stage){set(x,y,z,RoadhouseBlock.of(k,f,stage));}
        BlockState at(int x,int y,int z){return BuildBlocks.state(l,b.offset(x,y,z));}
        void door(int x,int z,Direction facing){var lower=Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.OPEN,false);set(x,0,z,lower);set(x,1,z,lower.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));}
        void lantern(int x,int y,int z){set(x,y,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));}
        void leaves(int x,int y,int z,Block leaf){var r=b.offset(x,y,z).subtract(b);if(Math.abs(r.getX())<=35&&r.getZ()<=0&&r.getZ()>=-113&&at(x,y,z).isAir())set(x,y,z,leaf.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));}
        void spruce(int x,int z,int h){for(int y=0;y<h;y++)set(x,y,z,Blocks.SPRUCE_LOG);for(int y=2;y<=h;y++){int r=y>=h-1?(y==h?0:1):y%2==0?2:1;for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)if((dx!=0||dz!=0||y>=h)&&Math.abs(dx)+Math.abs(dz)<=r+1)leaves(x+dx,y,z+dz,Blocks.SPRUCE_LEAVES);}}
        void oak(int x,int z,int h){for(int y=0;y<h;y++)set(x,y,z,Blocks.OAK_LOG);for(int y=h-2;y<=h+1;y++){int r=y>=h+1?1:2;for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)if((dx!=0||dz!=0||y>=h)&&!(Math.abs(dx)==2&&Math.abs(dz)==2))leaves(x+dx,y,z+dz,Blocks.OAK_LEAVES);}}
    }

    public static void build(ServerLevel l,BlockPos b){
        var s=new Site(l,b);
        ground(s);highway(s);lot(s);bar(s);for(int i=0;i<PICKUPS.length;i++)pickup(s,PICKUPS[i][0],PICKUPS[i][1],Direction.NORTH,PICKUPS[i][2],false);
        fenceAndDitch(s);field(s);edges(s);
        // Fences, rails and panes take their joins from what was actually built around them.
        BuildBlocks.after(l,()->{for(var at:s.connect){var st=l.getBlockState(at);var joined=Block.updateFromNeighbourShapes(st,l,at);if(!joined.equals(st))l.setBlock(at,joined,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}});
        markBuilt(LabyrinthData.get(l.getServer()),b);
    }

    private static void ground(Site s){
        s.fill(-35,-3,-113,35,-2,0,Blocks.DIRT);
        for(int x=-35;x<=35;x++)for(int z=-113;z<=0;z++){
            int h=hash(x,z);BlockState top=switch(h%11){case 0->Blocks.COARSE_DIRT.defaultBlockState();case 1->Blocks.PODZOL.defaultBlockState();default->Blocks.GRASS_BLOCK.defaultBlockState();};
            s.set(x,-1,z,top);
        }
    }
    private static void highway(Site s){
        for(int x=-35;x<=35;x++)for(int z=-10;z<=-2;z++){
            var k=z==-2||z==-10?Kind.GRAVEL:z==-3||z==-9?Kind.ASPHALT_EDGE:z==-6?Kind.ASPHALT_CENTRE:Kind.ASPHALT;
            s.r(x,-1,z,k,Direction.NORTH,k==Kind.GRAVEL?3:0);
            // White posts with amber reflectors mark the shoulders.
            if((z==-2||z==-10)&&Math.floorMod(x,9)==4&&Math.abs(x)>4)s.r(x,0,z,Kind.REFLECTOR,z==-2?Direction.SOUTH:Direction.NORTH);
        }
        // The worn apron the reader steps out onto, and the right-of-way fence either side of it.
        s.fill(-3,-1,-1,3,-1,0,RoadhouseBlock.of(Kind.GRAVEL,Direction.NORTH,3));
        for(int x=-35;x<=35;x++)if(Math.abs(x)>=4)s.set(x,0,-1,Blocks.SPRUCE_FENCE);
        // The pole sign by the road: LAST STOP over COCKTAILS · BEER, facing traffic both ways.
        for(int y=0;y<=4;y++)s.r(-10,y,-11,Kind.POLE,Direction.NORTH);
        Kind[][] sign={{Kind.SIGN_TL,Kind.SIGN_TM,Kind.SIGN_TR},{Kind.SIGN_BL,Kind.SIGN_BM,Kind.SIGN_BR}};
        for(int row=0;row<2;row++)for(int col=0;col<3;col++)s.r(-11+col,6-row,-11,sign[row][col],Direction.SOUTH);
    }
    private static void lot(Site s){
        for(int x=-35;x<=26;x++)for(int z=-38;z<=-11;z++){int h=hash(x,z)%23;s.r(x,-1,z,Kind.GRAVEL,Direction.from2DDataValue(hash(z,x)%4),h<2?1:h<4?2:h<6?3:0);}
        // A sodium lamp by the side door lights the lot; past the trucks it is dark.
        for(var e:ProofrockTown.lampStates(s.b,-10,-29,Direction.EAST).entrySet())BuildBlocks.set(s.l,e.getKey(),e.getValue(),F);
        // A dumpster against the bar's back corner.
        for(int x=-12;x<=-11;x++){s.set(x,0,-31,Blocks.GREEN_TERRACOTTA);s.set(x,1,-31,Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.BOTTOM));}
        // Headlights left on: each pickup throws light on the gravel ahead of it.
        for(var p:PICKUPS)headlights(s.l,s.b,p,true);
    }
    /** The light a pickup's headlights throw ahead of it, on or off, only into open air. */
    public static void headlights(ServerLevel l,BlockPos b,int[] p,boolean on){
        for(int i=1;i<=3;i++)for(int j=0;j<=2;j+=2){var at=b.offset(p[0]+j,0,p[1]-i);var st=BuildBlocks.state(l,at);
            if(on&&st.isAir())BuildBlocks.set(l,at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,i==1?12:i==2?10:7),F);
            else if(!on&&st.is(Blocks.LIGHT))BuildBlocks.set(l,at,Blocks.AIR.defaultBlockState(),F);}
    }

    private static void bar(Site s){
        // Foundation, floor and the board-and-batten shell.
        s.fill(-33,-1,-30,-13,-1,-14,Blocks.COBBLESTONE);s.fill(-32,-1,-29,-14,-1,-15,Blocks.DARK_OAK_PLANKS);
        var boards=RoadhouseBlock.of(Kind.BOARDS,Direction.NORTH);
        for(int y=0;y<=4;y++){for(int x=-33;x<=-13;x++){s.set(x,y,-14,boards);s.set(x,y,-30,boards);}for(int z=-30;z<=-14;z++){s.set(-33,y,z,boards);s.set(-13,y,z,boards);}}
        for(int y=0;y<=5;y++)for(int[] c:new int[][]{{-33,-14},{-13,-14},{-33,-30},{-13,-30}})s.set(c[0],y,c[1],Blocks.STRIPPED_SPRUCE_LOG);
        s.fill(-33,5,-30,-13,5,-14,Blocks.SPRUCE_PLANKS);
        // A false front over the porch, and a dark rim round the flat roof.
        for(int x=-33;x<=-13;x++)s.set(x,6,-14,boards);for(int x=-28;x<=-18;x++)s.set(x,7,-14,boards);
        var rim=Blocks.DARK_OAK_SLAB.defaultBlockState();for(int x=-33;x<=-13;x++)s.set(x,6,-30,rim);for(int z=-29;z<=-15;z++){s.set(-33,6,z,rim);s.set(-13,6,z,rim);}
        // Windows: two front pairs (one with the neon), one over each booth, two on the lot side.
        for(int x:new int[]{-30,-29,-28,-18,-17,-16})for(int y=1;y<=2;y++)s.set(x,y,-14,Blocks.GLASS_PANE);
        s.r(-29,2,-14,Kind.NEON_BEER,Direction.SOUTH);
        for(int z:new int[]{-17,-21})s.set(-33,2,z,Blocks.GLASS_PANE);for(int z:new int[]{-19,-20})for(int y=1;y<=2;y++)s.set(-13,y,z,Blocks.GLASS_PANE);
        s.door(FRONT_DOOR.getX(),FRONT_DOOR.getZ(),Direction.SOUTH);s.door(SIDE_DOOR.getX(),SIDE_DOOR.getZ(),Direction.EAST);
        // The porch: a plank deck under a slab roof on posts, a lantern by the door and a bench under the window.
        s.fill(-31,-1,-13,-15,-1,-11,Blocks.SPRUCE_PLANKS);s.fill(-31,4,-13,-15,4,-11,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
        for(int x:new int[]{-31,-26,-20,-15})for(int y=0;y<=3;y++)s.set(x,y,-11,Blocks.SPRUCE_FENCE);
        s.lantern(-23,3,-12);
        for(int x=-30;x<=-28;x++)s.set(x,0,-13,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        // The counter, the bartender's aisle and the back bar: cabinets, two shelves of bottles and the mirror between them.
        for(int x=-31;x<=-19;x++){s.r(x,0,-25,Kind.COUNTER,Direction.SOUTH);s.r(x,0,-29,Kind.BACK_BAR,Direction.SOUTH);s.r(x,1,-29,Kind.SHELF,Direction.SOUTH);s.r(x,2,-29,x>=-27&&x<=-23?Kind.MIRROR:Kind.SHELF,Direction.SOUTH);}
        s.r(-26,1,-25,Kind.TAPS,Direction.SOUTH);s.r(-29,1,-25,Kind.GLASSES,Direction.SOUTH);s.r(-23,1,-25,Kind.GLASSES,Direction.SOUTH);s.r(REGISTER.getX(),REGISTER.getY(),REGISTER.getZ(),Kind.REGISTER,Direction.NORTH);
        for(int x=-30;x<=-20;x+=2)s.r(x,0,-24,Kind.STOOL,Direction.NORTH);
        // Two booths along the west wall, each bench facing its table.
        for(int[] booth:new int[][]{{-16,-17,-18},{-20,-21,-22}})for(int x=-32;x<=-31;x++){s.r(x,0,booth[0],Kind.BOOTH,Direction.NORTH);s.r(x,0,booth[1],Kind.BOOTH_TABLE,Direction.NORTH);s.r(x,0,booth[2],Kind.BOOTH,Direction.SOUTH);}
        // The pool table under its lamp, the dartboard and the jukebox on the lot-side wall, the noticeboard by the door.
        s.r(POOL_L.getX(),0,POOL_L.getZ(),Kind.POOL_L,Direction.NORTH);s.r(POOL_R.getX(),0,POOL_R.getZ(),Kind.POOL_R,Direction.NORTH);
        s.r(-26,4,-19,Kind.POOL_LAMP,Direction.NORTH);s.r(-25,4,-19,Kind.POOL_LAMP,Direction.NORTH);
        s.r(DARTS.getX(),DARTS.getY(),DARTS.getZ(),Kind.DARTBOARD,Direction.WEST);
        s.set(JUKEBOX.getX(),0,JUKEBOX.getZ(),Blocks.JUKEBOX);
        s.r(BOARD.getX(),BOARD.getY(),BOARD.getZ(),Kind.CORKBOARD,Direction.NORTH);
        for(int[] at:new int[][]{{-28,-21},{-20,-17},{-27,-27},{-22,-27}})s.lantern(at[0],4,at[1]);
    }

    /**
     * A pickup three wide and six long, pointing {@code heading}, with {@code (x,z)} its driver's-side front corner. The
     * parts are named as seen: the front's left is the driver's right. {@code dented} dents the driver's door.
     */
    static java.util.Map<BlockPos,BlockState> pickupStates(BlockPos b,int x,int z,Direction heading,int paint,boolean dented){
        var out=new java.util.LinkedHashMap<BlockPos,BlockState>();var right=heading.getClockWise();
        for(int row=0;row<6;row++)for(int col=0;col<3;col++){
            var at=b.offset(x,0,z).relative(heading,-row).relative(right,col);var side=col==0?heading.getCounterClockWise():right;
            Kind k;Direction f=heading;
            switch(row){
                case 0->{k=col==0?Kind.PU_FRONT_R:col==1?Kind.PU_FRONT_M:Kind.PU_FRONT_L;}
                case 1->{if(col==1)k=Kind.PU_HOOD;else{k=Kind.PU_WHEEL;f=side;}}
                case 2->{if(col==1)k=Kind.PU_BENCH;else{k=dented&&col==0?Kind.PU_DOOR_DENT:Kind.PU_DOOR;f=side;}out.put(at.above(),RoadhouseBlock.of(Kind.PU_CAB,heading,paint));}
                case 3->{if(col==1)k=Kind.PU_BED;else{k=Kind.PU_BED_SIDE;f=side;}}
                case 4->{if(col==1)k=Kind.PU_BED;else{k=Kind.PU_BED_WHEEL;f=side;}}
                default->{k=col==0?Kind.PU_TAIL_L:col==1?Kind.PU_TAIL_M:Kind.PU_TAIL_R;f=heading.getOpposite();}
            }
            out.put(at,RoadhouseBlock.of(k,f,paint));
        }
        return out;
    }
    private static void pickup(Site s,int x,int z,Direction heading,int paint,boolean dented){for(var e:pickupStates(s.b,x,z,heading,paint,dented).entrySet())BuildBlocks.set(s.l,e.getKey(),e.getValue(),F);}

    private static void fenceAndDitch(Site s){
        // Post and rail along the field, broken through where the elk goes.
        for(int x=-35;x<=35;x++){if(x>GAP_X0&&x<=GAP_X1)continue;s.set(x,0,FENCE,Blocks.SPRUCE_FENCE);}
        s.r(2,0,FENCE+1,Kind.FALLEN_RAIL,Direction.EAST);s.r(1,0,FENCE-1,Kind.FALLEN_RAIL,Direction.NORTH);
        // A dry ditch with a soft bottom, crossed by planks at the gap.
        for(int x=-35;x<=35;x++)for(int z=-43;z<=-42;z++){
            if(x>=GAP_X0&&x<=GAP_X1){s.set(x,-1,z,Blocks.SPRUCE_PLANKS);continue;}
            s.set(x,-1,z,Blocks.AIR);s.set(x,-2,z,hash(x,z)%3==0?Blocks.MUD:Blocks.COARSE_DIRT);
        }
        for(int x=-35;x<=35;x++)for(int z:new int[]{-41,-44})if(hash(x,z)%4==0&&s.at(x,0,z).isAir())s.set(x,0,z,Blocks.SHORT_GRASS);
    }

    /** One repeat of the field, laid twice; everything in it depends only on x and the repeat's phase. */
    private static void field(Site s){
        for(int z=FIELD_START;z>=-112;z--){
            int k=phase(z);
            for(int x=-30;x<=30;x++){
                int h=hash(x,k);
                s.set(x,-1,z,h%7==0?Blocks.ROOTED_DIRT:h%5==0?Blocks.DIRT:Blocks.COARSE_DIRT);
                boolean lane=Math.abs(x-lane(k))<=1;
                if(x==POSTS_X&&k%4==2){s.set(x,0,z,Blocks.SPRUCE_FENCE);if(k!=BROKEN_POST)s.set(x,1,z,Blocks.SPRUCE_FENCE);continue;}
                if(x==POSTS_X+1&&k==BROKEN_POST){s.r(x,0,z,Kind.FALLEN_RAIL,Direction.EAST);continue;}
                if(x==9&&k==27){for(int y=0;y<6;y++)s.set(x,y,z,Blocks.STRIPPED_OAK_LOG);s.set(x+1,4,z,Blocks.OAK_FENCE);s.set(x-1,3,z,Blocks.OAK_FENCE);continue;}
                if(lane){if(h%5==0)s.r(x,0,z,Kind.STUBBLE,Direction.from2DDataValue(h%4));continue;}
                if(h%9==0)s.r(x,0,z,Kind.STUBBLE,Direction.from2DDataValue(h%4));
                else if(h%13!=0){s.r(x,0,z,Kind.STALKS,Direction.from2DDataValue(h%4));if(h%3!=0)s.r(x,1,z,Kind.STALKS_TOP,Direction.from2DDataValue(h%4));}
            }
            // Treelines both sides, planted only well inside the repeat so no crown crosses its seam.
            for(int side:new int[]{-1,1})for(int x=31;x<=35;x++){int xx=side*x;s.set(xx,-1,z,Blocks.PODZOL);}
            if(k>=3&&k<=30)for(int side:new int[]{-1,1}){int h=hash(side,k);if(h%3==0){int xx=side*(32+h%3);if(h%2==0)s.spruce(xx,z,6+h%3);else s.oak(xx,z,4+h%2);}}
        }
    }
    /** The trodden lane the elk left, wandering a little as it goes north. */
    public static int lane(int k){return 3+(k%17<6?0:k%17<11?1:k%17<14?2:1);}

    private static void edges(Site s){
        // Woods close the scene on both sides of the lot and road; the field ends in a hedge nobody ever reaches.
        for(int z=-1;z>=-44;z-=3)for(int side:new int[]{-1,1}){int h=hash(side*7,z);int x=side*(33+h%2);if(side<0&&z<=-12&&z>=-31)x=-35;if(h%2==0)s.spruce(x,z,6+h%4);else s.oak(x,z,4+h%3);}
        // Hedgerows: nothing walks out past the woods. The road runs on into them at both ends.
        for(int z=0;z>=-44;z--)for(int side:new int[]{-1,1})for(int y=0;y<=2;y++)s.leaves(side*35,y,z,Blocks.SPRUCE_LEAVES);
        for(int z=FIELD_START;z>=-113;z--)for(int side:new int[]{-1,1})for(int y=0;y<=1;y++)s.leaves(side*31,y,z,Blocks.OAK_LEAVES);
        for(int x=-35;x<=35;x++)for(int y=0;y<=2;y++)s.leaves(x,y,-113,Blocks.SPRUCE_LEAVES);
        // Scrub east of the lot.
        for(int x=27;x<=31;x++)for(int z=-38;z<=-11;z++)if(hash(x,z)%5==0&&s.at(x,0,z).isAir())s.set(x,0,z,hash(z,x)%3==0?Blocks.FERN:Blocks.SHORT_GRASS);
        // A few young spruce along the road's far side, either side of the reader's door.
        for(int x:new int[]{-30,-22,-14,12,17,31})s.spruce(x,0,4+hash(x,0)%2);
    }

    // ------------------------------------------------------------------------------------------------ saved worlds
    static void markBuilt(LabyrinthData d,BlockPos b){var t=d.state(STATE);t.putBoolean(Long.toString(b.asLong()),true);d.setState(STATE,t);}
    public static boolean built(LabyrinthData d,BlockPos b){return d.state(STATE).getBoolean(Long.toString(b.asLong()));}
    private static AABB area(BlockPos b){var r=PLACE.room();return new AABB(Vec3.atLowerCornerOf(b.offset(r.minX()-1,r.minY()-1,r.minZ()-1)),Vec3.atLowerCornerOf(b.offset(r.maxX()+2,r.maxY()+2,r.maxZ()+2)));}
    public static boolean loaded(ServerLevel l,BlockPos b){var area=area(b);
        for(int x=(int)Math.floor(area.minX)>>4;x<=(int)Math.floor(area.maxX)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=(int)Math.floor(area.maxZ)>>4;z++)
            if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        return true;
    }
    /**
     * Re-carves an older lot once ({@value #STATE}): only when its chunks and entity sections are loaded, no camera is
     * within sixteen blocks and nothing living is in it but the elk and the scene's own cast. The elk keeps its body, its
     * wound and its UUID; the old accusers and herd eyes go, and the bar's people are met fresh. Every reader's record is
     * kept as it was: a reader who saw the elk saw it, and a finished reader stays finished.
     */
    public static boolean rebuild(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());if(built(d,b))return true;var area=area(b);if(!loaded(l,b))return false;
        if(l.players().stream().anyMatch(p->area.inflate(16).intersects(p.getCamera().getBoundingBox())))return false;
        if(!l.getEntitiesOfClass(LivingEntity.class,area,e->e.isAlive()&&!e.isSpectator()&&!(e instanceof LiteraryActor)&&!(e instanceof LiteraryElk)).isEmpty())return false;
        // The old source lectern would throw its book onto the floor as it goes, and a disc would leave the jukebox.
        for(var at:BlockPos.betweenClosed(BlockPos.containing(area.minX,area.minY,area.minZ),BlockPos.containing(area.maxX-1,area.maxY-1,area.maxZ-1))){
            var be=l.getBlockEntity(at);
            if(be instanceof LecternBlockEntity lectern){lectern.clearContent();var st=l.getBlockState(at);if(st.hasProperty(LecternBlock.HAS_BOOK))l.setBlock(at,st.setValue(LecternBlock.HAS_BOOK,false),Block.UPDATE_CLIENTS);}
            else if(be instanceof JukeboxBlockEntity box)box.setTheItem(net.minecraft.world.item.ItemStack.EMPTY);
        }
        for(var actor:l.getEntitiesOfClass(LiteraryActor.class,area))actor.discard();
        var world=LiteraryVignettes.shared(d,PLACE);var kept=new net.minecraft.nbt.CompoundTag();if(world.hasUUID("Elk"))kept.putUUID("Elk",world.getUUID("Elk"));LiteraryVignettes.shared(d,PLACE,kept);
        LiteraryRooms.build(l,b,PLACE);
        ElkLotNight.restElk(l,b);
        markBuilt(d,b);
        return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=13||LabyrinthBuilder.isCarving())return;
        var data=LabyrinthData.get(s);var b=base(s);if(b==null||data.door(PLACE.entryDoorId())==null||!LabyrinthBuilder.isPlaceReady(data,PLACE)||built(data,b))return;
        var l=s.getLevel(NovelRooms.dimension(PLACE));if(l!=null)rebuild(l,b);
    }
}
