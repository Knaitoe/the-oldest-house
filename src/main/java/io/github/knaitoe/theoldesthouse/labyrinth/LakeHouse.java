package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The house at the bottom of the lake: an adaptation of the premise of Josh Malerman's novella, in original words.
 * <p>A boathouse on a clear lake lends a canoe. A low culvert under a service drive leads into a murky third lake, and a
 * two-storey brick house stands on its bottom, the roof a few blocks under the surface, a raft already tied to the chimney.
 * Readers dive to it on several visits, each with better gear, under one rule: don't ask how. See
 * docs/HOUSE_AT_THE_BOTTOM_OF_A_LAKE_DESIGN.md.
 * <p>The scene is built whole here; the polish, composition, review and playtest passes leave it alone. Its base stands at
 * a fixed height ({@link #BASE_Y}) rather than its slot's layer, so the deep lake and the basement fit in the world.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LakeHouse {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LAKE_HOUSE;
    /** The layout version that first carves this scene in saved worlds. */
    public static final int LAYOUT=44;
    /** Outdoor scenes stand 4,096 blocks apart; this one keeps one height whatever its slot's layer. */
    public static final int BASE_Y=40;
    /** The scene's extent, relative to its base. The deepest cell is the basement under the house. */
    public static final int MIN_X=-56,MAX_X=56,MIN_Y=-44,MAX_Y=24,MIN_Z=-230;

    // ------------------------------------------------------------------------------------------------ the map
    /** The boathouse: the rental log on its lectern, the account on its shelf. */
    public static final BlockPos SOURCE=new BlockPos(-4,0,-5),ACCOUNT=new BlockPos(4,0,-5);
    /** Where each reader's canoe is waiting, beside the end of the dock. */
    public static final BlockPos DOCK_END=new BlockPos(0,-1,-22),CANOE=new BlockPos(3,-1,-19);
    /** The culvert under the service drive: its two mouths. Its channel runs from the clear lake to the third. */
    public static final BlockPos CULVERT_SOUTH=new BlockPos(0,-1,-103),CULVERT_NORTH=new BlockPos(0,-1,-121);
    public static final int CHANNEL_SOUTH=-100,CHANNEL_NORTH=-127,COVER_SOUTH=-104,COVER_NORTH=-120;
    /** The house: its centre on the lake bed, the half front door, and the raft tied over the chimney. */
    public static final BlockPos HOUSE=new BlockPos(0,-26,-170),FRONT_DOOR=new BlockPos(0,-25,-161),RAFT=new BlockPos(6,-1,-173),CHIMNEY=new BlockPos(6,-3,-173);
    /** The compressor at the raft's west end, feeding the helmet's hose ({@link LakeHouseDive}). */
    public static final BlockPos COMPRESSOR=new BlockPos(4,0,-173);
    /** The house's walls: x -9..9, z -161..-179; ground floor y -25..-21, upstairs y -19..-15, roof ridge y -5. */
    public static final int HX=9,HZ_FRONT=-161,HZ_BACK=-179,GROUND=-26,UPPER=-20,EAVES=-14;

    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private LakeHouse(){}

    // ------------------------------------------------------------------------------------------------ shape of the land
    static boolean clearLake(int x,int z){double a=x/36.0,c=(z+58)/44.0;return a*a+c*c<=1;}
    static double clearDepth(int x,int z){double a=x/36.0,c=(z+58)/44.0;return 1-(a*a+c*c);}
    static boolean thirdLake(int x,int z){double a=x/42.0,c=(z+172)/46.0;return a*a+c*c<=1;}
    static double thirdDepth(int x,int z){double a=x/42.0,c=(z+172)/46.0;return 1-(a*a+c*c);}
    static boolean underHouse(int x,int z){return Math.abs(x)<=14&&z<=-156&&z>=-184;}
    /** The culvert's water: two wide, under the service drive. */
    static boolean channel(int x,int z){return (x==0||x==1)&&z<=CHANNEL_SOUTH&&z>=CHANNEL_NORTH;}
    /** The embankment the service drive runs along. */
    static boolean embankment(int x,int z){return Math.abs(x)<=26&&z<=-104&&z>=-120;}
    static boolean road(int x,int z){return Math.abs(x)<=26&&z<=-110&&z>=-114;}
    static double noise(int x,int z){long h=x*341873128712L+z*132897987541L;h=(h^(h>>>13))*0x5DEECE66DL;h^=h>>>17;return (h&0xFFFF)/65535.0;}
    /** The lake bed under the clear lake: shelving, clean sand and gravel. */
    static int clearBed(int x,int z){return -2-(int)Math.round(7*Math.sqrt(Math.max(0,clearDepth(x,z))));}
    /** The third lake's bed: deep and flat where the house stands. */
    static int thirdBed(int x,int z){if(underHouse(x,z))return GROUND-1;return Math.max(GROUND-1,-2-(int)Math.round(27*Math.pow(Math.max(0,thirdDepth(x,z)),.55)));}
    /** Land: low and level near the water, rising into wooded banks at the edges so no edge of the world shows. */
    static int surface(int x,int z){
        if(embankment(x,z))return road(x,z)?2:Math.min(2,1+Math.min(Math.abs(z+104),Math.abs(z+120))/2);
        // The south edge rises too, except where the boathouse stands against it.
        int south=-z+Math.max(0,(14-Math.abs(x))*3/2);
        int edge=Math.min(Math.min(x-MIN_X,MAX_X-x),Math.min(z-MIN_Z,south));
        int rise=Math.max(0,12-edge);
        return -1+(rise*rise)/14+(noise(x/3,z/3)<.3&&rise>0?1:0);
    }

    // ------------------------------------------------------------------------------------------------ building
    private static final class Site {
        final ServerLevel l;final BlockPos b;final List<BlockPos> connect=new ArrayList<>();
        Site(ServerLevel l,BlockPos b){this.l=l;this.b=b;}
        boolean vestibule(int x,int z){return z>=1;}
        void set(int x,int y,int z,BlockState s){if(vestibule(x,z))return;BuildBlocks.set(l,b.offset(x,y,z),s,F);
            if(s.getBlock() instanceof FenceBlock||s.getBlock() instanceof IronBarsBlock||s.getBlock() instanceof WallBlock)connect.add(b.offset(x,y,z));}
        void set(int x,int y,int z,Block s){set(x,y,z,s instanceof LeavesBlock?s.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true):s.defaultBlockState());}
        void fill(int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(x,y,z,s);}
        void fill(int x0,int y0,int z0,int x1,int y1,int z1,Block s){fill(x0,y0,z0,x1,y1,z1,s.defaultBlockState());}
        BlockState at(int x,int y,int z){return BuildBlocks.state(l,b.offset(x,y,z));}
        /** A block that keeps the water round it: anything that can hold water is built holding it below the surface. */
        void wet(int x,int y,int z,BlockState s){set(x,y,z,y<=-1&&s.hasProperty(BlockStateProperties.WATERLOGGED)?s.setValue(BlockStateProperties.WATERLOGGED,true):s);}
        void wet(int x,int y,int z,Block s){wet(x,y,z,s.defaultBlockState());}
    }

    public static void build(ServerLevel l,BlockPos b){
        var s=new Site(l,b);
        land(s);culvert(s);boathouse(s);dock(s);house(s);raft(s);woods(s);LakeHouseInterior.build(l,b);
        // Fences, walls and panes take their joins from what was actually built around them.
        BuildBlocks.after(l,()->{for(var at:s.connect){var st=l.getBlockState(at);var joined=Block.updateFromNeighbourShapes(st,l,at);if(!joined.equals(st))l.setBlock(at,joined,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}});
    }

    public static void buildEmpty(ServerLevel l,BlockPos b){var r=PLACE.room();for(int x=r.minX();x<=r.maxX();x++)for(int z=r.minZ();z<=r.maxZ();z++)for(int y=r.minY();y<=r.maxY();y++)BuildBlocks.set(l,b.offset(x,y,z),Blocks.AIR.defaultBlockState(),F);var s=new Site(l,b);land(s);culvert(s);boathouse(s);dock(s);woods(s);LiteraryRooms.paper(l,b,SOURCE,LiteraryTexts.source(PLACE));LiteraryRooms.prop(l,b,ACCOUNT,LiteraryPropBlock.Kind.LEDGER,Direction.SOUTH);LabyrinthBuilder.entrance(l,b,Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState());LabyrinthBuilder.doors(l,b,LabyrinthPlace.LAKE_HOUSE_EMPTY);NovelRooms.safeApproach(l,b);}

    /** The lowest water in a column, or none: the culvert's channel, then either lake. */
    static int lowestWater(int x,int z){
        if(channel(x,z))return -1;
        if(embankment(x,z))return Integer.MAX_VALUE;
        if(clearLake(x,z))return clearBed(x,z)+1;
        if(thirdLake(x,z))return thirdBed(x,z)+1;
        return Integer.MAX_VALUE;
    }
    /** How far down a column must be solid so that no neighbour's water stands against open space under it. */
    static int footing(int x,int z,int own){
        int low=Math.min(Math.min(lowestWater(x+1,z),lowestWater(x-1,z)),Math.min(lowestWater(x,z+1),lowestWater(x,z-1)));
        return Math.min(own,low==Integer.MAX_VALUE?own:low-1);
    }

    /** Ground, the two lakes and their beds. Water is only ever laid where solid ground holds it in, sides and all. */
    private static void land(Site s){
        for(int x=MIN_X;x<=MAX_X;x++)for(int z=MIN_Z;z<=0;z++){
            if(channel(x,z))continue;
            if(clearLake(x,z)&&!embankment(x,z)){
                int bed=clearBed(x,z);
                s.fill(x,footing(x,z,bed-2),z,x,bed-1,z,Blocks.STONE);
                s.set(x,bed,z,noise(x,z)<.25?Blocks.GRAVEL:Blocks.SAND);
                s.fill(x,bed+1,z,x,-1,z,Blocks.WATER);
                if(bed<-3&&noise(x+7,z)<.07)s.set(x,bed+1,z,Blocks.SEAGRASS);
                if(clearDepth(x,z)<.08&&noise(x,z+3)<.12&&!near(x,z,0,-16,4))s.set(x,0,z,Blocks.LILY_PAD);
                continue;
            }
            if(thirdLake(x,z)&&!embankment(x,z)){
                int bed=thirdBed(x,z);
                s.fill(x,footing(x,z,bed-2),z,x,bed-1,z,Blocks.STONE);
                s.set(x,bed,z,noise(x,z)<.4?Blocks.MUD:noise(x,z)<.7?Blocks.CLAY:Blocks.DIRT);
                s.fill(x,bed+1,z,x,-1,z,Blocks.WATER);
                continue;
            }
            int top=surface(x,z);
            int foot=footing(x,z,top-3);
            if(foot<top-3)s.fill(x,foot,z,x,top-4,z,Blocks.STONE);
            s.fill(x,top-3,z,x,top-1,z,Blocks.DIRT);
            s.set(x,top,z,road(x,z)?Blocks.GRAY_CONCRETE.defaultBlockState():Blocks.GRASS_BLOCK.defaultBlockState());
            // Reeds and pale sand where the clear lake meets the land.
            boolean shore=clearLake(x+1,z)||clearLake(x-1,z)||clearLake(x,z+1)||clearLake(x,z-1);
            if(shore&&top==-1){s.set(x,top,z,Blocks.SAND);if(noise(x,z)<.18&&!near(x,z,0,-12,6))s.set(x,0,z,Blocks.SUGAR_CANE);}
        }
    }
    private static boolean near(int x,int z,int cx,int cz,int r){return Math.abs(x-cx)<=r&&Math.abs(z-cz)<=r;}

    /**
     * The service drive and its culvert: two wide, dark, and so low under the drive that a reader in a canoe has to duck.
     * One block of air over one of water; a canoe fits, and so does a reader swimming or wading.
     */
    private static void culvert(Site s){
        for(int z=CHANNEL_NORTH;z<=CHANNEL_SOUTH;z++){
            boolean covered=z<=COVER_SOUTH&&z>=COVER_NORTH;
            // Water floor and channel, footed deep enough that the lakes at either mouth stand against stone.
            for(int x=0;x<=1;x++){int foot=footing(x,z,-4);s.fill(x,foot,z,x,-4,z,Blocks.STONE);}
            s.fill(-1,-3,z,2,-2,z,Blocks.SMOOTH_STONE);
            s.set(0,-1,z,Blocks.WATER);s.set(1,-1,z,Blocks.WATER);
            s.fill(0,0,z,1,2,z,Blocks.AIR);
            // Concrete walls, stained low down where the water stands.
            for(int y=-1;y<=0;y++){s.set(-1,y,z,y==-1?Blocks.CYAN_TERRACOTTA:Blocks.LIGHT_GRAY_CONCRETE);s.set(2,y,z,y==-1?Blocks.CYAN_TERRACOTTA:Blocks.LIGHT_GRAY_CONCRETE);}
            if(covered){
                // The ceiling, and the drive's own surface over it.
                for(int x=0;x<=1;x++){s.set(x,1,z,Blocks.LIGHT_GRAY_CONCRETE);if(surface(x,z)>=2)s.set(x,2,z,road(x,z)?Blocks.GRAY_CONCRETE:Blocks.GRASS_BLOCK);}
            }
        }
        // The drive's kerbs.
        for(int x=-26;x<=26;x++){s.wet(x,3,-110,Blocks.STONE_BRICK_WALL);s.wet(x,3,-114,Blocks.STONE_BRICK_WALL);}
        // Brush on the banks half hides both mouths; the channel and its walls are left clear.
        for(int z=CHANNEL_NORTH+1;z<=CHANNEL_SOUTH-1;z++){
            if(z<=COVER_SOUTH&&z>=COVER_NORTH&&z!=COVER_SOUTH&&z!=COVER_NORTH)continue;
            for(int x=-5;x<=6;x++){
                if(x>=-1&&x<=2||lowestWater(x,z)!=Integer.MAX_VALUE||road(x,z)||noise(x*3,z)>=.7)continue;
                int top=surface(x,z),h=noise(x,z*5)<.45?2:1;
                for(int y=top+1;y<=top+h;y++)s.set(x,y,z,noise(x,z+y)<.5?Blocks.AZALEA_LEAVES:Blocks.OAK_LEAVES);
            }
        }
    }

    /** The boathouse the reader comes into from the House: the rental log, the account shelf, and the lake through the door. */
    private static void boathouse(Site s){
        s.fill(-6,-1,0,6,-1,-9,Blocks.SPRUCE_PLANKS);
        s.fill(-5,0,-1,5,4,-8,Blocks.AIR);
        for(int y=0;y<=3;y++){
            for(int z=0;z>=-9;z--){s.set(-6,y,z,Blocks.SPRUCE_PLANKS);s.set(6,y,z,Blocks.SPRUCE_PLANKS);}
            for(int x=-6;x<=6;x++){s.set(x,y,0,Blocks.SPRUCE_PLANKS);s.set(x,y,-9,Blocks.SPRUCE_PLANKS);}
        }
        for(int y=0;y<=3;y++)for(int[] c:new int[][]{{-6,0},{6,0},{-6,-9},{6,-9}})s.set(c[0],y,c[1],Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Y));
        // The wide lake door and two windows.
        s.fill(-1,0,-9,1,2,-9,Blocks.AIR);
        for(int z:new int[]{-3,-6}){s.set(-6,1,z,Blocks.GLASS_PANE);s.set(6,1,z,Blocks.GLASS_PANE);}
        // A pitched roof along the room, seated on the walls' top course, ridge over the middle.
        for(int i=0;i<=6;i++)for(int z=1;z>=-10;z--){
            int y=3+i;
            s.set(-7+i,y,z,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
            s.set(7-i,y,z,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST));
            if(i>0)for(int x=-7+i+1;x<=7-i-1;x++){if(z==0||z==-9)s.set(x,y,z,Blocks.SPRUCE_PLANKS);}
        }
        s.fill(0,9,1,0,9,-10,Blocks.SPRUCE_SLAB);
        // Inside: the rental counter with the log, the shelf with the accounts, a rack of paddles, one lamp.
        s.set(-4,0,-4,Blocks.SPRUCE_PLANKS);s.set(-4,0,-6,Blocks.SPRUCE_PLANKS);s.set(-5,0,-5,Blocks.SPRUCE_PLANKS);
        s.set(4,0,-4,Blocks.SPRUCE_PLANKS);s.set(4,0,-6,Blocks.SPRUCE_PLANKS);s.set(5,0,-5,Blocks.SPRUCE_PLANKS);
        for(int z=-2;z>=-7;z-=5)s.set(5,2,z,Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.WEST).setValue(TrapDoorBlock.OPEN,true));
        s.fill(0,3,-4,0,8,-4,Blocks.CHAIN);s.set(0,2,-4,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
    }

    /** The dock runs out over the clear water to where the canoes are tied. */
    private static void dock(Site s){
        for(int z=-10;z>=DOCK_END.getZ();z--){
            for(int x=-1;x<=1;x++)s.set(x,-1,z,Blocks.SPRUCE_PLANKS);
            if(z%4==-2)for(int x:new int[]{-2,2}){for(int y=-2;y>=clearBed(x,Math.min(z,-14))+1;y--)s.wet(x,y,z,Blocks.SPRUCE_FENCE);s.set(x,-1,z,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Y));s.set(x,0,z,Blocks.SPRUCE_FENCE);}
        }
    }

    /** The house on the bottom of the third lake: brick, two storeys, a gabled roof a few blocks under the surface. */
    private static void house(Site s){
        int x0=-HX,x1=HX,zf=HZ_FRONT,zb=HZ_BACK;
        // Foundations and floors.
        s.fill(x0-1,GROUND-1,zf+1,x1+1,GROUND-1,zb-1,Blocks.STONE_BRICKS);
        s.fill(x0,GROUND,zf,x1,GROUND,zb,Blocks.OAK_PLANKS);
        s.fill(x0,UPPER,zf,x1,UPPER,zb,Blocks.SPRUCE_PLANKS);
        s.fill(x0,EAVES,zf,x1,EAVES,zb,Blocks.SPRUCE_PLANKS);
        // Walls: brick, with windows on both storeys.
        for(int y=GROUND+1;y<EAVES;y++){
            if(y==UPPER)continue;
            for(int x=x0;x<=x1;x++){s.set(x,y,zf,Blocks.BRICKS);s.set(x,y,zb,Blocks.BRICKS);}
            for(int z=zb;z<=zf;z++){s.set(x0,y,z,Blocks.BRICKS);s.set(x1,y,z,Blocks.BRICKS);}
        }
        for(int storey:new int[]{GROUND+2,UPPER+2})for(int y=storey;y<=storey+1;y++){
            for(int x:new int[]{-6,-5,5,6}){s.wet(x,y,zf,Blocks.GLASS_PANE);s.wet(x,y,zb,Blocks.GLASS_PANE);}
            for(int z:new int[]{-165,-166,-174,-175}){s.wet(x0,y,z,Blocks.GLASS_PANE);s.wet(x1,y,z,Blocks.GLASS_PANE);}
        }
        // The front doorway: one block wide; its half door is hung in a later pass.
        s.set(FRONT_DOOR.getX(),FRONT_DOOR.getY(),zf,Blocks.WATER);s.set(FRONT_DOOR.getX(),FRONT_DOOR.getY()+1,zf,Blocks.WATER);
        // The stone step and its handrails.
        s.wet(0,GROUND,zf+1,Blocks.STONE_BRICK_SLAB);
        for(int x:new int[]{-1,1})s.wet(x,GROUND+1,zf+1,Blocks.IRON_BARS);
        // A gabled roof, ridge along the house, slates stepping down to the eaves on both sides.
        int half=(zf-zb)/2+1;
        for(int i=0;i<=half;i++){
            int y=EAVES+i;
            for(int x=x0-1;x<=x1+1;x++){
                s.wet(x,y,zf+1-i,Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
                s.wet(x,y,zb-1+i,Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));
            }
            // The gable ends are brick.
            for(int z=zb+i;z<=zf-i;z++){s.set(x0,y,z,Blocks.BRICKS);s.set(x1,y,z,Blocks.BRICKS);}
        }
        for(int x=x0-1;x<=x1+1;x++)s.wet(x,EAVES+half+1,(zf+zb)/2,Blocks.DEEPSLATE_TILE_SLAB);
        // The chimney comes up through the back slope to just under the surface.
        for(int y=EAVES;y<=CHIMNEY.getY();y++)for(int dx=0;dx<=1;dx++)s.set(CHIMNEY.getX()+dx,y,CHIMNEY.getZ(),Blocks.BRICKS);
        // A garden under the front windows: kelp and seagrass, a glow of sea pickles, a low wall.
        for(int x=-8;x<=8;x++){
            if(Math.abs(x)<=1)continue;
            s.wet(x,GROUND,zf+3,Blocks.STONE_BRICK_WALL);
            for(int z=zf+1;z<=zf+2;z++){
                if(noise(x,z)<.45){int h=1+(int)(noise(z,x)*4);for(int y=GROUND;y<GROUND+h;y++)s.set(x,y,z,y==GROUND+h-1?Blocks.KELP:Blocks.KELP_PLANT);}
                else if(noise(x+1,z)<.7)s.set(x,GROUND,z,Blocks.SEAGRASS);
                else s.set(x,GROUND,z,Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES,1+(int)(noise(x,z+1)*3)).setValue(SeaPickleBlock.WATERLOGGED,true));
            }
        }
    }

    /** Somebody's raft, already tied to the chimney: logs and rope, the compressor, and a bedroll to lie down on. */
    private static void raft(Site s){
        for(int x=RAFT.getX()-3;x<=RAFT.getX()+3;x++)for(int z=RAFT.getZ()-2;z<=RAFT.getZ()+2;z++)
            s.set(x,-1,z,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        s.wet(CHIMNEY.getX(),CHIMNEY.getY()+1,CHIMNEY.getZ(),Blocks.CHAIN);
        s.set(COMPRESSOR.getX(),COMPRESSOR.getY(),COMPRESSOR.getZ(),LakeHouseRegistry.COMPRESSOR.get().defaultBlockState().setValue(LakeHouseCompressorBlock.FACING,Direction.EAST));
        for(int x=RAFT.getX()+2;x<=RAFT.getX()+3;x++)s.set(x,0,RAFT.getZ()-2,Blocks.RED_CARPET);
    }

    /** Woods round both lakes: birch and oak by the clear water, spruce close round the third. */
    private static void woods(Site s){
        for(int x=MIN_X+1;x<=MAX_X-1;x+=1)for(int z=MIN_Z+1;z<=-11;z+=1){
            if(clearLake(x,z)||thirdLake(x,z)||embankment(x,z)||channel(x,z)||near(x,z,0,-6,9)||near(x,z,0,-103,7)||near(x,z,0,-121,7))continue;
            boolean edgeOfLake=clearLake(x+3,z)||clearLake(x-3,z)||clearLake(x,z+3)||clearLake(x,z-3)||thirdLake(x+3,z)||thirdLake(x-3,z)||thirdLake(x,z+3)||thirdLake(x,z-3);
            if(edgeOfLake)continue;
            double n=noise(x*7,z*5);
            int edge=Math.min(Math.min(x-MIN_X,MAX_X-x),z-MIN_Z);
            double density=edge<14?.16:.05;
            if(n>density)continue;
            int top=surface(x,z);
            boolean north=z<-110;
            tree(s,x,top+1,z,north?(n<density*.7?Blocks.SPRUCE_LOG:Blocks.DARK_OAK_LOG):(n<density*.5?Blocks.BIRCH_LOG:Blocks.OAK_LOG),
                north?Blocks.SPRUCE_LEAVES:(n<density*.5?Blocks.BIRCH_LEAVES:Blocks.OAK_LEAVES),5+(int)(noise(z,x)*4));
        }
    }
    private static void tree(Site s,int x,int y,int z,Block log,Block leaves,int height){
        for(int i=0;i<height;i++)s.set(x,y+i,z,log.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Y));
        boolean conifer=leaves==Blocks.SPRUCE_LEAVES;
        for(int dy=conifer?2:height-3;dy<=height;dy++){
            int r=conifer?Math.max(0,(height-dy)/2):(dy==height?1:2);
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
                if(dx==0&&dz==0&&dy<height)continue;
                if(Math.abs(dx)==r&&Math.abs(dz)==r&&r>1)continue;
                if(s.at(x+dx,y+dy,z+dz).isAir())s.set(x+dx,y+dy,z+dz,leaves);
            }
        }
        s.set(x,y+height,z,leaves);
    }

    // ------------------------------------------------------------------------------------------------ helpers
    public static @Nullable BlockPos base(MinecraftServer s){var origin=HouseSavedData.get(s).houseOrigin();return origin==null?null:LabyrinthPlaces.base(origin,PLACE);}
    public static @Nullable BlockPos emptyBase(MinecraftServer s){var o=HouseSavedData.get(s).houseOrigin();return o==null?null:LabyrinthPlaces.base(o,LabyrinthPlace.LAKE_HOUSE_EMPTY);}
    public static boolean empty(ServerPlayer p){var b=emptyBase(p.server);return b!=null&&p.level().dimension()==NovelRooms.dimension(PLACE)&&IndianLakeRooms.bounds(b,PLACE).contains(p.position());}
    /** The reader's lake: the empty lake once their story has ended there, else the copy they stand in or keep (0.4.156: the lake house has copies, two readers to each). */
    public static @Nullable BlockPos readerBase(ServerPlayer p){return empty(p)?emptyBase(p.server):SceneInstances.base(p,PLACE);}
    public static LabyrinthData.Door destinationEntry(ServerPlayer p,LabyrinthData.Door supplied,LabyrinthPlace place){
        if(place!=PLACE)return supplied;var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);
        if(!SAID_HELLO.equals(own.getString("Outcome")))return supplied;
        var end=LabyrinthData.get(p.server).door(LabyrinthPlace.LAKE_HOUSE_EMPTY.entryDoorId());return end==null?supplied:end;
    }
    /** Whether a reader is in the lake house's grounds at all. */
    public static boolean inside(ServerPlayer p){
        if(p.level().dimension()!=NovelRooms.dimension(PLACE))return false;var b=readerBase(p);return b!=null&&IndianLakeRooms.bounds(b,PLACE).contains(p.position());
    }
    /** Whether the reader stands on the raft. Relative to the base. */
    static boolean onRaft(Vec3 r){return Math.abs(r.x-RAFT.getX()-.5)<=3.6&&Math.abs(r.z-RAFT.getZ()-.5)<=2.6&&r.y>=-.3&&r.y<=1.5;}
    /** Whether the reader's flashlight has gone out (the lounge, on the helmet visit). */
    static boolean flashlightOut(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE).getBoolean("FlashlightOut")&&!LakeHouseStory.lit(p);}
    static int stage(CompoundTag own){return own.getInt("Stage");}

    // ------------------------------------------------------------------------------------------------ the reader's part
    /** Each visit starts on the reader's own clock. */
    public static void arrive(ServerPlayer p,CompoundTag own){
        if(own.getBoolean("LakeFreshVisit"))own.putInt("T",0);
        // A reader in Tess's place (0.4.156) has no partner of their own and rides in their diver's canoe.
        if(LakeHousePairs.isPartner(p)){LakeHouseStory.arrive(p,own);return;}
        LakeHousePartner.choose(p,own);LakeHouseStory.arrive(p,own);
        LakeHouseCanoe.ensure(p);
    }
    /** Every five ticks while the reader is in the scene. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        own.putInt("T",own.getInt("T")+5);
        if(LakeHousePairs.partner(p,b,own))return;
        if(LakeHouseStory.reader(p,b,own))return;
        // A reader who arrived before their level did, or whose canoe was put away, finds one waiting again.
        if(own.getInt("T")%100==5)LakeHouseCanoe.ensure(p);
        var r=p.position().subtract(Vec3.atLowerCornerOf(b));int visit=Math.max(1,own.getInt("Visit"));
        // The flashlight hangs by the boathouse door. The visit's diving gear waits on the raft: the helmet on the second
        // visit, the tanks from the third.
        if(r.z>-10&&LakeHouseGear.give(p,LakeHouseRegistry.FLASHLIGHT.get()))p.displayClientMessage(Component.literal("A flashlight hangs on a nail by the door. You take it."),true);
        if(onRaft(r)){
            if(visit==2&&LakeHouseGear.give(p,LakeHouseRegistry.HELMET.get()))p.displayClientMessage(Component.literal("A gold diving helmet sits on the raft, its hose coupled to the compressor."),true);
            else if(visit>=3&&LakeHouseGear.give(p,LakeHouseRegistry.TANKS.get()))p.displayClientMessage(Component.literal("A pair of air tanks lies on the raft, filled."),true);
        }
        LakeHouseDive.hose(p,b,own);
        if(r.z<CHANNEL_NORTH)own.putBoolean("ReachedThird",true);
        // The visit's beat: how far into the house the reader got, and back up into the air.
        if(LakeHouseDive.inHouse(r)){
            if(r.z>HZ_FRONT-5)own.putBoolean("SawFoyer",true);
            if(visit>=2&&r.z<HZ_FRONT-9)own.putBoolean("SawBack",true);
        }
        boolean surfaced=r.z<CHANNEL_NORTH&&r.y>-1.5&&!p.isEyeInFluid(net.minecraft.tags.FluidTags.WATER);
        if(surfaced&&own.getInt("Beat")<visit){
            if(visit==1&&own.getBoolean("SawFoyer")){own.putInt("Beat",visit);p.displayClientMessage(Component.literal("There's furniture down there."),false);}
            else if(visit==2&&own.getBoolean("SawBack")){own.putInt("Beat",visit);p.displayClientMessage(Component.literal("There's a staircase at the back, with a red runner on it."),false);}
        }
    }
    /** True when a click in the scene is used up. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){return LakeHouseStory.click(p,b,rel,own);}
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){LakeHouseStory.talk(p,a,own);}

    // ------------------------------------------------------------------------------------------------ the account
    public static final String SAID_HELLO="said_hello",LEFT_THE_LIGHTS_ON="left_the_lights_on",STAYED="stayed_a_little_longer";
    public static String account(CompoundTag own){
        if(own.getBoolean(LakeHousePairs.SIDE))return LakeHousePartner.partnerAccount(own);
        String ending=switch(own.getString("Outcome")){
            case LEFT_THE_LIGHTS_ON->"THE LIGHTS\n\nEvery light in the house came on at once. I didn't go up. I swam out through the half door and didn't look back until I was on the raft. From up there you can see it, lit, all the way down. It was still lit when I left.";
            case STAYED->"THE PLACE AT THE TABLE\n\nOne of the chairs at the table had air in it, so I sat down. The lights went down one at a time and I stayed where I was. I woke on the raft. They tell me it was days. My place is still set down there.";
            default->"THE EMPTY LAKE\n\nI went up the stairs with the lights on and put my hand on its shoulder. I said my name. Then it was dark, and when I came up there was nothing under me but water. I've been back. The roof isn't there. I still have the flashlight.";
        };return LakeHousePartner.accountOpening(own)+ending+(own.getBoolean("RuleBroken")?"\n\nI tested the rule twice. Nothing came loose, but someone walked upstairs afterward. I had asked how.":"");
    }

    // ------------------------------------------------------------------------------------------------ the shared clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();if(s.getTickCount()%10!=1)return;var l=s.getLevel(NovelRooms.dimension(PLACE));if(l==null||!LabyrinthBuilder.isPlaceReady(LabyrinthData.get(s),PLACE))return;
        if(s.getTickCount()%40==11)LakeHousePairs.refresh(s);
        // The original and every copy that stands has its own compressor, canoes and lent gear.
        for(var b:SceneInstances.bases(s,PLACE)){
            if(!l.hasChunkAt(b.offset(COMPRESSOR)))continue;
            LakeHouseDive.compressor(l,b,s.getTickCount());
            if(s.getTickCount()%40!=11)continue;
            if(l.hasChunkAt(b.offset(CANOE)))LakeHouseCanoe.tidy(l,b);
            LakeHouseGear.tidy(l,box(b));
        }
    }
    /** The scene's box, absolute. */
    public static AABB box(BlockPos b){return new AABB(b.getX()+MIN_X,b.getY()+MIN_Y,b.getZ()+MIN_Z,b.getX()+MAX_X+1,b.getY()+MAX_Y+1,b.getZ()+1);}
}
