package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/**
 * Proofrock (0.4.67): the living town on the bank of Indian Lake, with the old town still beneath the water.
 *
 * Every street, yard and floor she can cross is one level, so a hunt can follow a reader anywhere on land. Lamps keep the
 * main routes lit and leave the alleys, yards and back rooms dark; corners, cars, dumpsters, sheds and board fences break
 * sight lines; a few low gaps (the flatbed, the picnic tables, the gym bleachers) are hers alone. Living grass stays where
 * it always was a refuge: the fenced lawns, the green and the school courtyard. The lake still hides a reader who stays under.
 *
 * Plan, relative to the entry: Main Street runs north from the forest road to the beach. West of it are shops, an alley and
 * Pine Lane's houses, then School Street and Indian Lake High. East are shops, an alley, the garage and motel, then Lake
 * Street, the cinema row, the green and two houses, and Beach Road. The lake fills the north and an eastern bay.
 */
public final class ProofrockTown {
    public static final int HALF=64,NORTH=-128;
    public static final BlockPos FURNACE=new BlockPos(6,0,-90),SUPPLIES=new BlockPos(7,0,-90),LECTERN=new BlockPos(5,0,-4),SIGN=new BlockPos(-5,0,-4);
    public static final BlockPos CHURCH_DOOR=new BlockPos(30,-11,-103),ROOF_HATCH=new BlockPos(30,-4,-107),HYMN=new BlockPos(30,-8,-112),PREACHER=new BlockPos(30,-11,-120);
    public static final BlockPos CANOE=new BlockPos(3,0,-99),SHORE_BODY=new BlockPos(-8,0,-90);
    /** Dark places she can always reach, for her first appearance and when no better cover exists. */
    public static final List<BlockPos> LURKS=List.of(new BlockPos(19,0,-72),new BlockPos(-19,0,-31),new BlockPos(-60,0,-34),new BlockPos(40,0,-24),new BlockPos(-58,0,-13));
    private static final int F=LabyrinthBuilder.flags();
    private static final int[] SLOPE={-2,-3,-4,-6,-8,-10};
    private static int[][] distance;
    private final ServerLevel l;private final BlockPos b;
    private ProofrockTown(ServerLevel l,BlockPos b){this.l=l;this.b=b;}

    public static void build(ServerLevel l,BlockPos b){new ProofrockTown(l,b).all();}

    // ------------------------------------------------------------------------------------------------ the land and the lake
    /** The lake: the whole north shore and an eastern bay. */
    public static boolean water(int x,int z){return Math.abs(x)<HALF&&z>NORTH&&z<0&&(z<=-92||x>=42&&z<=-34);}
    /** How many cells out from the nearest shore a water cell lies. */
    static synchronized int fromShore(int x,int z){
        if(distance==null){
            int w=2*HALF+1,d=-NORTH+1;distance=new int[w][d];var open=new ArrayDeque<int[]>();
            for(int i=0;i<w;i++)for(int j=0;j<d;j++){boolean wet=water(i-HALF,-j);distance[i][j]=wet?Integer.MAX_VALUE:0;if(!wet)open.add(new int[]{i,j});}
            while(!open.isEmpty()){var at=open.removeFirst();for(int[] step:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int i=at[0]+step[0],j=at[1]+step[1];
                if(i<0||j<0||i>=w||j>=d||distance[i][j]<=distance[at[0]][at[1]]+1)continue;distance[i][j]=distance[at[0]][at[1]]+1;open.add(new int[]{i,j});}}
        }
        return distance[x+HALF][-z]-1;
    }
    /** The top of the solid ground in a column: -1 on land, the shelving bed under water. */
    public static int bed(int x,int z){if(!water(x,z))return -1;int d=fromShore(x,z);return d<SLOPE.length?SLOPE[d]:-12;}

    private void all(){
        // Above ground everything is cleared; below it everything is made solid before any water is let in, so nothing flows.
        clear(-HALF,0,NORTH,HALF,8,0);
        fill(-HALF,-13,NORTH,HALF,-13,0,Blocks.DEEPSLATE);
        fill(-HALF,-12,NORTH,HALF,-5,0,Blocks.STONE);
        fill(-HALF,-4,NORTH,HALF,-2,0,Blocks.DIRT);
        ground();
        for(int x=-HALF+1;x<HALF;x++)for(int z=NORTH+1;z<0;z++)if(water(x,z)){int top=bed(x,z);
            set(x,top,z,top<=-10?(hash(x,z)%5==0?Blocks.CLAY:Blocks.GRAVEL):Blocks.SAND);
            if(top<-1)fill(x,top+1,z,x,-1,z,Blocks.WATER);}
        roads();forest();
        shopsWest();shopsEast();houses();garage();motel();green();beach();
        IndianLakeHigh.build(l,b);
        drowned();
        // Shop awnings and the lane's lawn are authored after the roads. Install
        // the complete fitted lamps last so neither replaces a section of a post.
        for(var e:streetlights(b).entrySet())BuildBlocks.set(l,e.getKey(),e.getValue(),F);
        street();
        LabyrinthBuilder.entrance(l,b,Blocks.BLACK_CONCRETE.defaultBlockState(),Blocks.COARSE_DIRT.defaultBlockState(),Blocks.BLACK_CONCRETE.defaultBlockState());
        LabyrinthBuilder.doors(l,b,LabyrinthPlace.DROWNED_TOWN);
        NovelRooms.safeApproach(l,b);
    }
    /** Top-layer materials on land. Grass only where a reader can stand safely, and only where it is meant to be. */
    private void ground(){
        for(int x=-HALF;x<=HALF;x++)for(int z=NORTH;z<=0;z++){if(water(x,z))continue;int h=hash(x,z);
            set(x,-1,z,h%7==0?Blocks.COARSE_DIRT:h%3==0?Blocks.PODZOL:Blocks.ROOTED_DIRT);}
        // The beaches: the north shore and the bay's western shore.
        fill(-HALF+1,-1,-91,41,-1,-89,Blocks.SAND);fill(38,-1,-91,41,-1,-34,Blocks.SAND);
        fill(-HALF+1,-2,-91,41,-2,-89,Blocks.SANDSTONE);fill(38,-2,-91,41,-2,-34,Blocks.SANDSTONE);
    }

    // ------------------------------------------------------------------------------------------------ streets
    private void roads(){
        // Main Street: asphalt with a broken centre line, stone sidewalks and kerbs.
        for(int z=-2;z>=-88;z--){for(int x=-3;x<=3;x++)set(x,-1,z,x==0&&Math.floorMod(z,4)<2?Blocks.YELLOW_CONCRETE:hash(x,z)%11==0?Blocks.ANDESITE:Blocks.GRAY_CONCRETE);
            if(z<=-6)for(int x:new int[]{-5,-4,4,5})set(x,-1,z,Math.abs(x)==4?Blocks.POLISHED_ANDESITE:hash(x,z)%6==0?Blocks.CRACKED_STONE_BRICKS:Blocks.STONE_BRICKS);}
        // School Street to the west, Lake Street to the east, each with a sidewalk either side.
        for(int x=-6;x>=-63;x--){for(int z=-39;z>=-43;z--)set(x,-1,z,z==-41&&Math.floorMod(x,4)<2?Blocks.YELLOW_CONCRETE:hash(x,z)%11==0?Blocks.ANDESITE:Blocks.GRAY_CONCRETE);
            set(x,-1,-38,Blocks.STONE_BRICKS);set(x,-1,-44,Blocks.STONE_BRICKS);}
        for(int x=6;x<=41;x++){for(int z=-39;z>=-43;z--)set(x,-1,z,z==-41&&Math.floorMod(x,4)<2?Blocks.YELLOW_CONCRETE:hash(x,z)%11==0?Blocks.ANDESITE:Blocks.GRAY_CONCRETE);
            if(x<=37){set(x,-1,-38,Blocks.STONE_BRICKS);set(x,-1,-44,Blocks.STONE_BRICKS);}}
        // Beach Road behind the east blocks, and the gravel lane and alleys.
        for(int x=6;x<=37;x++)for(int z=-85;z>=-88;z--)set(x,-1,z,hash(x,z)%5==0?Blocks.ANDESITE:Blocks.GRAY_CONCRETE);
        for(int z=-2;z>=-38;z--)for(int x=-40;x<=-37;x++)set(x,-1,z,hash(x,z)%3==0?Blocks.COARSE_DIRT:Blocks.GRAVEL);
        for(int z=-6;z>=-37;z--){alley(-19,z);alley(-18,z);alley(18,z);alley(19,z);}
        for(int z=-45;z>=-84;z--){alley(18,z);alley(19,z);}
        // The narrow gaps between shops, one body wide.
        for(int z:new int[]{-16,-27})for(int x=6;x<=17;x++){alley(-x,z);alley(x,z);}
        for(int z:new int[]{-59,-71})for(int x=6;x<=17;x++)alley(x,z);
        // Main Street's lamps; side streets have fewer.
        for(int z:new int[]{-9,-23,-47,-68,-79}){lamp(-5,z,Direction.EAST);lamp(5,z,Direction.WEST);}
        for(int x:new int[]{-14,-30,-46,-60})lamp(x,-44,Direction.SOUTH);
        for(int x:new int[]{14,30})lamp(x,-38,Direction.NORTH);
        lamp(-36,-24,Direction.WEST);
        // Cars at the kerb: solid enough to hide behind.
        car(-3,-18,Blocks.RED_CONCRETE);car(2,-30,Blocks.BLUE_CONCRETE);car(-3,-52,Blocks.GREEN_TERRACOTTA);car(2,-67,Blocks.WHITE_CONCRETE);
        car(23,-26,Blocks.LIGHT_BLUE_TERRACOTTA);car(26,-32,Blocks.BROWN_TERRACOTTA);car(-34,-41,Blocks.CYAN_TERRACOTTA);
        // Dumpsters against the far side of the alleys, clear of every back door: a body's width of passage stays beside each.
        for(int[] d:new int[][]{{19,-19},{19,-31},{19,-48},{19,-75},{-19,-9},{-19,-21}})dumpster(d[0],d[1]);
    }
    /** Two long, one wide, a lid on top: low enough that a reader standing on it is still within her reach. */
    private void dumpster(int x,int z){for(int i=0;i<2;i++){set(x,0,z-i,Blocks.GREEN_TERRACOTTA);set(x,1,z-i,Blocks.IRON_TRAPDOOR);}}
    private void alley(int x,int z){set(x,-1,z,hash(x,z)%4==0?Blocks.COBBLESTONE:hash(x,z)%3==0?Blocks.COARSE_DIRT:Blocks.GRAVEL);}
    /** A pole with an arm, the lantern hung over the street on the side the arm reaches. */
    private void lamp(int x,int z,Direction arm){
        for(var e:lampStates(b,x,z,arm).entrySet())BuildBlocks.set(l,e.getKey(),e.getValue(),F);
    }
    private static java.util.Map<BlockPos,BlockState> lampStates(BlockPos b,int x,int z,Direction arm){
        var out=new java.util.LinkedHashMap<BlockPos,BlockState>();
        for(int y=0;y<=4;y++)out.put(b.offset(x,y,z),TownStreetlightBlock.of(y==0?TownStreetlightBlock.Kind.BASE:y==4?TownStreetlightBlock.Kind.TOP:TownStreetlightBlock.Kind.POLE,arm,false));
        out.put(b.offset(x+arm.getStepX(),4,z+arm.getStepZ()),TownStreetlightBlock.of(TownStreetlightBlock.Kind.ARM,arm,false));
        out.put(b.offset(x+arm.getStepX(),3,z+arm.getStepZ()),TownStreetlightBlock.of(TownStreetlightBlock.Kind.HEAD,arm,false));return out;
    }
    /** Exact authored locations also used for the once-only saved streetlight repair. */
    public static java.util.Map<BlockPos,BlockState> streetlights(BlockPos b){
        var out=new java.util.LinkedHashMap<BlockPos,BlockState>();
        for(int z:new int[]{-9,-23,-47,-68,-79}){out.putAll(lampStates(b,-5,z,Direction.EAST));out.putAll(lampStates(b,5,z,Direction.WEST));}
        for(int x:new int[]{-14,-30,-46,-60})out.putAll(lampStates(b,x,-44,Direction.SOUTH));
        for(int x:new int[]{14,30})out.putAll(lampStates(b,x,-38,Direction.NORTH));
        out.putAll(lampStates(b,-36,-24,Direction.WEST));out.putAll(lampStates(b,20,-46,Direction.SOUTH));out.putAll(lampStates(b,-5,-88,Direction.EAST));out.putAll(lampStates(b,37,-60,Direction.WEST));
        for(int y=0;y<=2;y++)out.put(b.offset(1,y,-110),TownStreetlightBlock.of(y==0?TownStreetlightBlock.Kind.BASE:TownStreetlightBlock.Kind.POLE,Direction.NORTH,false));out.put(b.offset(1,3,-110),TownStreetlightBlock.of(TownStreetlightBlock.Kind.HEAD,Direction.NORTH,false));
        for(int x=4;x<=28;x+=8){for(int y=-11;y<=-8;y++)out.put(b.offset(x,y,-99),TownStreetlightBlock.of(y==-11?TownStreetlightBlock.Kind.BASE:TownStreetlightBlock.Kind.POLE,Direction.NORTH,true));out.put(b.offset(x,-7,-99),TownStreetlightBlock.of(TownStreetlightBlock.Kind.HEAD,Direction.NORTH,true));}
        return out;
    }
    /** Four long, two wide, two high: a body of paint over dark wheels, glass above. */
    private void car(int x,int z,Block paint){
        for(var e:carStates(b,x,z,paint).entrySet())BuildBlocks.set(l,e.getKey(),e.getValue(),F);
    }
    private static Map<BlockPos,BlockState> carStates(BlockPos b,int x,int z,Block paint){
        var states=new LinkedHashMap<BlockPos,BlockState>();
        String colour=paint==Blocks.RED_CONCRETE?"RED":paint==Blocks.BLUE_CONCRETE?"BLUE":paint==Blocks.GREEN_TERRACOTTA?"GREEN":paint==Blocks.WHITE_CONCRETE?"WHITE":paint==Blocks.LIGHT_BLUE_TERRACOTTA?"LIGHT_BLUE":paint==Blocks.BROWN_TERRACOTTA?"BROWN":paint==Blocks.CYAN_TERRACOTTA?"CYAN":"ORANGE";
        for(int i=0;i<4;i++)for(int j=0;j<2;j++){int cx=x+j,cz=z-i;boolean wheel=i==0||i==3;
            states.put(b.offset(cx,0,cz),TownFixtureBlock.of(wheel?TownFixtureBlock.Kind.CAR_WHEEL:TownFixtureBlock.Kind.valueOf("CAR_"+colour),j==0?Direction.WEST:Direction.EAST));
            states.put(b.offset(cx,1,cz),TownFixtureBlock.of(i==1||i==2?TownFixtureBlock.Kind.CAR_CABIN:TownFixtureBlock.Kind.valueOf("HOOD_"+colour),i==0?Direction.SOUTH:Direction.NORTH));}
        return states;
    }
    /** The same finite cars, exposed for a guarded saved-world material upgrade. */
    public static Map<BlockPos,BlockState> cars(BlockPos b){var out=new LinkedHashMap<BlockPos,BlockState>();int[][] spots={{-3,-18},{2,-30},{-3,-52},{2,-67},{23,-26},{26,-32},{-34,-41},{31,-10}};Block[] paint={Blocks.RED_CONCRETE,Blocks.BLUE_CONCRETE,Blocks.GREEN_TERRACOTTA,Blocks.WHITE_CONCRETE,Blocks.LIGHT_BLUE_TERRACOTTA,Blocks.BROWN_TERRACOTTA,Blocks.CYAN_TERRACOTTA,Blocks.ORANGE_TERRACOTTA};for(int i=0;i<spots.length;i++)out.putAll(carStates(b,spots[i][0],spots[i][1],paint[i]));return out;}

    // ------------------------------------------------------------------------------------------------ the woods round the edge
    private void forest(){
        for(int x=-62;x<=62;x+=5)for(int z:new int[]{-2,-4}){if(Math.abs(x)<=6||x>=-41&&x<=-36)continue;tree(x+Math.floorMod(hash(x,z),3)-1,z);}
        for(int z=-8;z>=-36;z-=5){tree(-62,z);tree(-61+Math.floorMod(hash(-61,z),2),z-2);}
        for(int x=40;x<=62;x+=4)for(int z=-6;z>=-31;z-=5)if(hash(x,z)%4!=0)tree(x+Math.floorMod(hash(x,z),2),z-Math.floorMod(hash(z,x),2));
        // A cold campfire ring in the eastern woods.
        set(48,0,-18,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false));
        for(var log:new int[][]{{46,-18},{50,-18},{48,-16}})set(log[0],0,log[1],Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,log[1]==-16?Direction.Axis.X:Direction.Axis.Z));
    }
    private void tree(int x,int z){
        if(water(x,z))return;int h=5+Math.floorMod(hash(x,z),3);
        for(int y=0;y<h;y++)set(x,y,z,Blocks.SPRUCE_LOG);
        for(int y=3;y<=Math.min(8,h+2);y++){int r=y>=h?0:y>=h-2?1:2;
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)if((dx!=0||dz!=0||y>=h)&&Math.abs(dx)+Math.abs(dz)<=r+1&&Math.abs(x+dx)<HALF&&z+dz>NORTH&&z+dz<0)
                setIfAir(x+dx,y,z+dz,Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));}
    }

    // ------------------------------------------------------------------------------------------------ Main Street's shops
    private void shopsWest(){
        shop(-6,-6,-15,Direction.EAST,"general",Blocks.WHITE_TERRACOTTA,Blocks.STRIPPED_SPRUCE_LOG,true,true);
        shop(-6,-17,-26,Direction.EAST,"post",Blocks.BRICKS,Blocks.STONE_BRICKS,true,true);
        shop(-6,-28,-37,Direction.EAST,"laundry",Blocks.LIGHT_BLUE_TERRACOTTA,Blocks.STRIPPED_BIRCH_LOG,false,true);
        // Inside: shelves and a till; pigeonholes behind the post counter; the machines along the wall.
        for(int z=-8;z>=-13;z-=2)for(int y=0;y<=1;y++)set(-11,y,z,Blocks.BOOKSHELF);
        counter(-8,-13,-15,Direction.EAST);detail(-8,1,-14,SceneDetailBlock.Kind.FILE_TRAY,Direction.EAST);
        for(int z=-19;z>=-24;z--){set(-9,0,z,Blocks.SPRUCE_PLANKS);set(-9,1,z,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));}
        for(int z=-19;z>=-24;z--)for(int y=1;y<=2;y++)set(-13,y,z,NovelRegistry.PIGEONHOLE.get().defaultBlockState().setValue(PigeonholeBlock.FACING,Direction.EAST).setValue(PigeonholeBlock.NUMBER,1+Math.floorMod(z*3+y,12)));
        for(int z=-30;z>=-35;z--)furniture(-13,0,z,HouseholdFurnitureBlock.Kind.WASHING_MACHINE,Direction.EAST);
        for(int z=-30;z>=-34;z-=2)furniture(-9,0,z,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        poster(-6,1,-16,Direction.NORTH);poster(-18,1,-21,Direction.WEST);
    }
    private void shopsEast(){
        shop(6,-6,-15,Direction.WEST,"hardware",Blocks.SPRUCE_PLANKS,Blocks.STRIPPED_DARK_OAK_LOG,true,true);
        shop(6,-17,-26,Direction.WEST,"sheriff",Blocks.STONE_BRICKS,Blocks.POLISHED_ANDESITE,true,true);
        shop(6,-28,-37,Direction.WEST,"diner",Blocks.RED_TERRACOTTA,Blocks.WHITE_TERRACOTTA,false,true);
        shop(6,-45,-58,Direction.WEST,"cinema",Blocks.DARK_OAK_PLANKS,Blocks.BLACKSTONE,false,false);
        shop(6,-60,-70,Direction.WEST,"bait",Blocks.MOSSY_COBBLESTONE,Blocks.STRIPPED_SPRUCE_LOG,true,true);
        shop(6,-72,-83,Direction.WEST,"hall",Blocks.LIGHT_GRAY_TERRACOTTA,Blocks.STONE_BRICKS,true,true);
        // Hardware shelves; the sheriff's desk, notices and a barred cell; the diner's counter and stools.
        for(int z=-8;z>=-13;z-=2)for(int y=0;y<=1;y++)set(11,y,z,Blocks.BARREL);
        counter(8,-13,-15,Direction.WEST);
        furniture(10,0,-20,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.WEST);furniture(11,0,-20,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        for(int z=-23;z>=-25;z--)for(int y=0;y<=2;y++)set(15,y,z,Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH,true).setValue(IronBarsBlock.SOUTH,true));
        for(int z:new int[]{-18,-19,-24})poster(13,1,z,Direction.WEST);
        for(int z=-30;z>=-35;z--){set(12,0,z,Blocks.WHITE_CONCRETE);set(12,1,z,Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));}
        for(int z=-30;z>=-35;z-=2)furniture(11,0,z,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.EAST);
        for(int z=-29;z>=-36;z-=3){furniture(8,0,z,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.WEST);detail(8,1,z,SceneDetailBlock.Kind.TEA_SET,Direction.WEST);}
        // The cinema: a box office in the lobby, then rows of seats facing a dead screen.
        for(int y=0;y<=3;y++)for(int z=-46;z>=-57;z--)set(16,y,z,Blocks.WHITE_CONCRETE);
        for(int x=9;x<=14;x+=2)for(int z=-47;z>=-56;z--)if(z!=-51&&z!=-52)set(x,0,z,LabyrinthBuilder.stairs(Blocks.RED_NETHER_BRICK_STAIRS,Direction.WEST));
        for(int x=8;x<=12;x++)for(int z=-62;z>=-68;z-=3)if(hash(x,z)%3==0)set(x,0,z,Blocks.BARREL);
        for(int z=-74;z>=-81;z-=2){set(12,0,z,LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,Direction.EAST));set(13,0,z,LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,Direction.EAST));}
        set(8,0,-77,Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING,Direction.EAST));
        poster(6,1,-59,Direction.SOUTH);poster(17,1,-64,Direction.EAST);
    }
    /**
     * A single-storey shop: a false front with its signboard, display windows either side of the door, an awning over the
     * sidewalk and a back door onto the alley. {@code front} is the facade's x; {@code faces} is the street it faces.
     */
    private void shop(int front,int z0,int z1,Direction faces,String sign,Block wall,Block trim,boolean lit,boolean backRoom){
        Direction into=faces.getOpposite();
        int dir=into.getStepX(),back=front+11*dir,zMax=Math.max(z0,z1),zMin=Math.min(z0,z1),door=Math.floorDiv(zMax+zMin,2);
        int x0=Math.min(front,back),x1=Math.max(front,back);
        fill(x0,-1,zMin,x1,-1,zMax,hash(front,z0)%2==0?Blocks.DARK_OAK_PLANKS:Blocks.SPRUCE_PLANKS);
        fill(x0,0,zMin,x1,3,zMax,Blocks.AIR);
        for(int x=x0;x<=x1;x++)for(int z=zMin;z<=zMax;z++)for(int y=0;y<=3;y++){boolean edge=x==x0||x==x1||z==zMin||z==zMax;if(edge)set(x,y,z,wall);}
        fill(x0,4,zMin,x1,4,zMax,Blocks.SMOOTH_STONE);
        // The false front stands above the roof, with corner posts and a cornice.
        for(int z=zMin;z<=zMax;z++){for(int y=4;y<=6;y++)set(front,y,z,z==zMin||z==zMax?trim:wall);set(front,7,z,slab(Blocks.STONE_BRICKS));}
        for(int y=0;y<=3;y++){set(front,y,zMin,trim);set(front,y,zMax,trim);}
        // Read from the street: the left panel is on the reader's left.
        for(int i=0;i<3;i++)set(front,5,faces==Direction.EAST?door+1-i:door-1+i,TownFixtureBlock.of(TownFixtureBlock.Kind.sign(sign,i),faces));
        // Display windows, the door, and the awning over the sidewalk.
        for(int z=zMin+1;z<zMax;z++)if(Math.abs(z-door)>=1)for(int y=0;y<=2;y++)set(front,y,z,y==0?wall:Blocks.GLASS_PANE);
        NovelRooms.door(l,b.offset(front,0,door),into,faces==Direction.EAST?Blocks.SPRUCE_DOOR:Blocks.OAK_DOOR,false);
        for(int z=zMin;z<=zMax;z++)set(front-dir,3,z,Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP));
        // A back room behind a partition, and the back door onto the alley.
        if(backRoom){int partition=back-3*dir;for(int z=zMin;z<=zMax;z++)for(int y=0;y<=3;y++)set(partition,y,z,wall);
            NovelRooms.door(l,b.offset(partition,0,door),into,Blocks.SPRUCE_DOOR,false);}
        NovelRooms.door(l,b.offset(back,0,door-2),into,Blocks.SPRUCE_DOOR,false);
        set(back-dir,0,zMin+1,Blocks.BARREL);set(back-dir,1,zMin+1,Blocks.BARREL);
        if(lit)light(front+4*dir,3,door,9);
        // A side window in the shop's long wall, onto the gap or the street.
        set(front+6*dir,1,zMin,Blocks.GLASS_PANE);set(front+6*dir,1,zMax,Blocks.GLASS_PANE);
    }
    private void counter(int x,int z0,int z1,Direction facing){
        for(int z=Math.max(z0,z1);z>=Math.min(z0,z1);z--){set(x,0,z,Blocks.SPRUCE_PLANKS);set(x,1,z,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));}
    }

    // ------------------------------------------------------------------------------------------------ Pine Lane and the green's houses
    private void houses(){
        // The west alley's board fence keeps the back yards out of sight of the shops.
        for(int z=-6;z>=-37;z--)if(z!=-16&&z!=-27)for(int y=0;y<=1;y++)set(-20,y,z,Blocks.SPRUCE_PLANKS);
        int[][] plots={{-6,-15},{-17,-26},{-28,-37}};
        for(var p:plots){
            // East of the lane, facing west; back yards onto the alley fence.
            house(-31,p[0]-2,Direction.WEST,hash(p[0],1)%2==0?Blocks.BIRCH_PLANKS:Blocks.WHITE_TERRACOTTA,hash(p[0],2)%3!=0);
            lawn(-36,-33,p[0]-1,p[1]+1);walk(-36,-33,p[0]-5);
            // West of the lane, facing east; sheds and woodpiles out back.
            house(-46,p[0]-2,Direction.EAST,hash(p[0],3)%2==0?Blocks.OAK_PLANKS:Blocks.LIGHT_GRAY_TERRACOTTA,hash(p[0],4)%3!=0);
            lawn(-44,-41,p[0]-1,p[1]+1);walk(-44,-41,p[0]-5);
            shed(-59,p[0]-3);
            for(int z=p[1]+2;z<=p[1]+4;z++)for(int y=0;y<=1;y++)set(-56,y,z,Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
            set(-21,0,p[1]+1,Blocks.CAULDRON);
        }
        set(-55,0,-9,Blocks.SPRUCE_PLANKS);set(-55,1,-9,LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,Direction.EAST));
        // Two houses north of the green, facing it.
        house2(21,-75);house2(29,-75);
        lawn(21,35,-70,-73);
        for(int x=21;x<=35;x++)set(x,-1,-69,Blocks.GRAVEL);
        for(int x=20;x<=36;x++)for(int y=0;y<=1;y++)if(x!=28)set(x,y,-84,Blocks.SPRUCE_PLANKS);
    }
    /** Nine by seven, gabled, with a porch at ground level and a door in the middle of the front that {@code faces} the lane. */
    private void house(int front,int zTop,Direction faces,Block wall,boolean lit){
        Direction into=faces.getOpposite();int z0=zTop-6,z1=zTop;
        int x0=faces==Direction.WEST?front:front-8,x1=faces==Direction.WEST?front+8:front;
        fill(x0,-1,z0,x1,-1,z1,Blocks.OAK_PLANKS);fill(x0,0,z0,x1,3,z1,Blocks.AIR);
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)for(int y=0;y<=3;y++)if(x==x0||x==x1||z==z0||z==z1)set(x,y,z,(x==x0||x==x1)&&(z==z0||z==z1)?Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState():wall.defaultBlockState());
        fill(x0+1,4,z0+1,x1-1,4,z1-1,Blocks.SPRUCE_PLANKS);
        // Gable roof, ridge running north to south.
        for(int k=0;k<=4;k++){int y=4+k;
            for(int z=z0-1;z<=z1+1;z++){if(k<4){set(x0+k,y,z,LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS,Direction.EAST));set(x1-k,y,z,LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS,Direction.WEST));}
                else set(x0+4,y,z,Blocks.DARK_OAK_PLANKS);}
            for(int x=x0+k+1;x<=x1-k-1;x++){set(x,y,z0,wall);set(x,y,z1,wall);}}
        for(int y=0;y<=8;y++)set(x0+2,y,z0,Blocks.BRICKS);
        int door=z0+3;
        NovelRooms.door(l,b.offset(front,0,door),into,Blocks.OAK_DOOR,false);
        for(int z:new int[]{z0+1,z1-1})set(front,1,z,Blocks.GLASS_PANE);
        for(int x=x0+3;x<=x1-2;x+=3){set(x,1,z0,Blocks.GLASS_PANE);set(x,1,z1,Blocks.GLASS_PANE);}
        int porch=front+faces.getStepX();
        for(int z:new int[]{z0+1,z1-1})for(int y=0;y<=2;y++)set(porch,y,z,fence(Blocks.DARK_OAK_FENCE,null));
        for(int z=z0+1;z<=z1-1;z++)set(porch,3,z,slab(Blocks.SPRUCE_PLANKS));
        for(int z=z0+1;z<=z1-1;z++)set(porch,-1,z,Blocks.SPRUCE_PLANKS);
        // A bed, a table and two chairs, a sofa; some houses have a lamp on.
        int inner=faces==Direction.WEST?x0+2:x1-2,step=faces==Direction.WEST?1:-1;
        NovelRooms.bed(l,b.offset(inner,0,z0+1),Blocks.RED_BED,Direction.SOUTH);
        furniture(inner+2*step,0,z1-2,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH);
        furniture(inner+3*step,0,z1-2,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.WEST);
        furniture(inner,0,z1-1,HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.NORTH);
        if(lit)light((x0+x1)/2,3,(z0+z1)/2,9);
    }
    /** A house facing south across the green. */
    private void house2(int x0,int zFront){
        int x1=x0+6,z0=zFront-6,z1=zFront;
        fill(x0,-1,z0,x1,-1,z1,Blocks.OAK_PLANKS);fill(x0,0,z0,x1,3,z1,Blocks.AIR);
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)for(int y=0;y<=3;y++)if(x==x0||x==x1||z==z0||z==z1)set(x,y,z,(x==x0||x==x1)&&(z==z0||z==z1)?Blocks.STRIPPED_SPRUCE_LOG:Blocks.BIRCH_PLANKS);
        for(int k=0;k<=3;k++){int y=4+k;for(int x=x0-1;x<=x1+1;x++){if(k<3){set(x,y,z0+k,LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,Direction.SOUTH));set(x,y,z1-k,LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,Direction.NORTH));}else set(x,y,z0+3,Blocks.SPRUCE_PLANKS);}
            for(int z=z0+k+1;z<=z1-k-1;z++){set(x0,y,z,Blocks.BIRCH_PLANKS);set(x1,y,z,Blocks.BIRCH_PLANKS);}}
        NovelRooms.door(l,b.offset(x0+3,0,z1),Direction.NORTH,Blocks.OAK_DOOR,false);
        set(x0+1,1,z1,Blocks.GLASS_PANE);set(x1-1,1,z1,Blocks.GLASS_PANE);
        NovelRooms.bed(l,b.offset(x0+1,0,z0+1),Blocks.WHITE_BED,Direction.EAST);
        furniture(x1-1,0,z0+1,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);furniture(x1-1,0,z1-1,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.WEST);
        set(x0,0,z0-1,Blocks.CAULDRON);
    }
    /** Grass behind a white picket fence and gate: a refuge with one way in. */
    private void lawn(int x0,int x1,int z0,int z1){
        int lx=Math.min(x0,x1),hx=Math.max(x0,x1),lz=Math.min(z0,z1),hz=Math.max(z0,z1);
        fill(lx,-1,lz,hx,-1,hz,Blocks.GRASS_BLOCK);
        for(int x=lx;x<=hx;x++)for(int z=lz;z<=hz;z++)if(hash(x,z)%9==0)set(x,0,z,Blocks.SHORT_GRASS);
    }
    /** A stone front walk from the lane to the porch, across the lawn: her way to the door, too. */
    private void walk(int x0,int x1,int z){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)set(x,-1,z,Blocks.COBBLESTONE);}
    private void shed(int x,int z){
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)for(int y=0;y<=2;y++){boolean wall=Math.abs(dx)==1||Math.abs(dz)==1;set(x+dx,y,z+dz,wall?Blocks.SPRUCE_PLANKS.defaultBlockState():Blocks.AIR.defaultBlockState());}
        fill(x-1,3,z-1,x+1,3,z+1,Blocks.SPRUCE_SLAB);
        set(x+1,0,z,Blocks.AIR);set(x+1,1,z,Blocks.AIR);set(x,0,z,Blocks.AIR);set(x-1,0,z+1,Blocks.SPRUCE_PLANKS);
    }

    // ------------------------------------------------------------------------------------------------ the garage, the motel, the green
    private void garage(){
        // Forecourt, pumps and an abandoned flatbed she can lie under.
        fill(21,-1,-6,27,-1,-19,Blocks.GRAY_CONCRETE);
        for(int z:new int[]{-9,-13}){set(24,0,z,Blocks.RED_CONCRETE);set(24,1,z,Blocks.WHITE_CONCRETE);set(24,2,z,Blocks.RED_CONCRETE_POWDER);}
        for(int x=21;x<=26;x++)for(int z=-16;z>=-18;z--)set(x,1,z,Blocks.SPRUCE_PLANKS);
        for(int[] wheel:new int[][]{{21,-16},{21,-18},{26,-16},{26,-18}})set(wheel[0],0,wheel[1],Blocks.POLISHED_BLACKSTONE_WALL);
        // The garage itself, its bay door open onto the forecourt.
        int x0=28,x1=36,z0=-18,z1=-7;fill(x0,-1,z0,x1,-1,z1,Blocks.GRAY_CONCRETE);fill(x0,0,z0,x1,3,z1,Blocks.AIR);
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)for(int y=0;y<=3;y++)if(x==x0||x==x1||z==z0||z==z1)set(x,y,z,Blocks.LIGHT_GRAY_CONCRETE);
        fill(x0,4,z0,x1,4,z1,Blocks.SMOOTH_STONE);
        fill(x0,0,-14,x0,2,-10,Blocks.AIR);
        for(int z=-14;z<=-10;z++)set(x0,3,z,Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP).setValue(TrapDoorBlock.OPEN,false));
        for(int i=0;i<3;i++)set(x0,4,-13+i,TownFixtureBlock.of(TownFixtureBlock.Kind.sign("garage",i),Direction.WEST));
        car(31,-10,Blocks.ORANGE_TERRACOTTA);set(35,0,-16,Blocks.CRAFTING_TABLE);set(35,0,-17,Blocks.SMITHING_TABLE);
        for(int z:new int[]{-8,-9})set(35,0,z,Blocks.CAULDRON);light(32,3,-12,8);
    }
    private void motel(){
        // Six rooms in a row facing the lot, the office at the north end.
        fill(21,-1,-21,30,-1,-37,Blocks.GRAY_CONCRETE);
        int x0=31,x1=37;fill(x0,-1,-21,x1,-1,-37,Blocks.SPRUCE_PLANKS);fill(x0,0,-21,x1,3,-37,Blocks.AIR);
        for(int z=-21;z>=-37;z--)for(int y=0;y<=3;y++){set(x0,y,z,Blocks.WHITE_TERRACOTTA);set(x1,y,z,Blocks.WHITE_TERRACOTTA);}
        for(int x=x0;x<=x1;x++)for(int y=0;y<=3;y++)for(int z:new int[]{-21,-24,-27,-30,-33,-37})set(x,y,z,Blocks.WHITE_TERRACOTTA);
        fill(x0,4,-21,x1,4,-37,Blocks.SMOOTH_STONE);
        for(int x=x0-1;x<=x1;x++)set(x,4,-20,slab(Blocks.BRICKS));
        int[][] rooms={{-22,-23},{-25,-26},{-28,-29},{-31,-32},{-34,-36}};
        for(var r:rooms){NovelRooms.door(l,b.offset(x0,0,r[0]),Direction.EAST,Blocks.BIRCH_DOOR,false);set(x0,1,r[1],Blocks.GLASS_PANE);
            NovelRooms.bed(l,b.offset(x1-2,0,r[0]),Blocks.LIGHT_BLUE_BED,Direction.EAST);}
        for(int z=-21;z>=-37;z--)set(x0-1,3,z,Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP));
        for(int i=0;i<3;i++)set(x0,4,-30+i,TownFixtureBlock.of(TownFixtureBlock.Kind.sign("motel",i),Direction.WEST));
        light(34,3,-35,7);
    }
    private void green(){
        // The town green: grass, a bandstand on a plank floor, benches and a memorial. Exposed, and safe.
        lawn(21,35,-46,-68);
        for(int x=36;x<=37;x++)for(int z=-45;z>=-84;z--)set(x,-1,z,Blocks.GRAVEL);
        int cx=28,cz=-57;
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)set(cx+dx,-1,cz+dz,Blocks.SPRUCE_PLANKS);
        for(int[] post:new int[][]{{-2,-2},{-2,2},{2,-2},{2,2}})for(int y=0;y<=3;y++)set(cx+post[0],y,cz+post[1],fence(Blocks.SPRUCE_FENCE,null));
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){int ring=Math.max(Math.abs(dx),Math.abs(dz));set(cx+dx,ring>=2?4:5,cz+dz,slab(Blocks.DARK_OAK_PLANKS));}
        set(cx,5,cz,Blocks.DARK_OAK_PLANKS);set(cx,4,cz,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        for(int y=0;y<=3;y++)set(23,y,-64,y==3?Blocks.CHISELED_STONE_BRICKS:Blocks.STONE_BRICKS);
        for(int z:new int[]{-49,-53,-61})set(33,0,z,LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,Direction.EAST));
        for(var t:new int[][]{{23,-48},{34,-66},{24,-58}})oak(t[0],t[1]);
        lamp(20,-46,Direction.SOUTH);
    }
    private void oak(int x,int z){
        for(int y=0;y<=4;y++)set(x,y,z,Blocks.OAK_LOG);
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)for(int y=3;y<=6;y++)if(Math.abs(dx)+Math.abs(dz)+Math.max(0,y-5)*2<=3&&(dx!=0||dz!=0||y>4))
            setIfAir(x+dx,y,z+dz,Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
    }

    // ------------------------------------------------------------------------------------------------ the beach, the pier and the boathouse
    private void beach(){
        // The furnace and its fuel, on bare sand some steps from any grass.
        set(FURNACE.getX(),0,FURNACE.getZ(),Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING,Direction.SOUTH));
        set(SUPPLIES.getX(),0,SUPPLIES.getZ(),LabyrinthBuilder.barrel(Direction.UP));
        // The pier: planks over posts, a lamp at its end.
        for(int z=-89;z>=-110;z--)for(int x=-1;x<=1;x++){set(x,-1,z,Blocks.SPRUCE_PLANKS);if(Math.abs(x)==1&&Math.floorMod(z,4)==0)for(int y=bed(x,z)+1;y<=-2;y++)set(x,y,z,Blocks.SPRUCE_LOG);}
        for(int y=0;y<=2;y++)set(1,y,-110,TownStreetlightBlock.of(y==0?TownStreetlightBlock.Kind.BASE:TownStreetlightBlock.Kind.POLE,Direction.NORTH,false));set(1,3,-110,TownStreetlightBlock.of(TownStreetlightBlock.Kind.HEAD,Direction.NORTH,false));
        set(-1,0,-104,LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,Direction.WEST));
        // The boathouse, open to the water on its north side.
        int x0=10,x1=17,z0=-96,z1=-89;
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){set(x,-1,z,Blocks.SPRUCE_PLANKS);if((x==x0||x==x1)&&(z==z0||z==z1))for(int y=bed(x,z)+1;y<=-2;y++)set(x,y,z,Blocks.SPRUCE_LOG);}
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)for(int y=0;y<=3;y++)if((x==x0||x==x1||z==z1)&&!(z==z1&&(x==13||x==14)&&y<=1))set(x,y,z,Blocks.SPRUCE_PLANKS);
        fill(x0,4,z0,x1,4,z1,Blocks.SPRUCE_SLAB);
        set(16,0,-91,Blocks.BARREL);set(16,0,-92,Blocks.BARREL);set(11,3,-93,Blocks.CHAIN);set(11,2,-93,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        // Picnic tables she can lie under, and a cold fire pit.
        // No benches: a table top two blocks up is out of a reader's jump, so it is never somewhere to stand above her.
        for(int x:new int[]{-20,-14})for(int dx=0;dx<=2;dx++){set(x+dx,1,-90,slab(Blocks.SPRUCE_PLANKS).setValue(SlabBlock.TYPE,SlabType.TOP));
            if(dx!=1)set(x+dx,0,-90,fence(Blocks.SPRUCE_FENCE,null));}
        set(-32,0,-90,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false));
        lamp(-5,-88,Direction.EAST);lamp(37,-60,Direction.WEST);
    }

    // ------------------------------------------------------------------------------------------------ the old town under the lake
    private void drowned(){
        // The old street, its lamps still standing, runs from under the pier to the church door.
        for(int x=-1;x<=31;x++)for(int z=-100;z>=-102;z--)if(water(x,z)&&bed(x,z)==-12)set(x,-12,z,hash(x,z)%3==0?Blocks.MOSSY_COBBLESTONE:Blocks.COBBLESTONE);
        for(int x=4;x<=28;x+=8){for(int y=-11;y<=-8;y++)set(x,y,-99,TownStreetlightBlock.of(y==-11?TownStreetlightBlock.Kind.BASE:TownStreetlightBlock.Kind.POLE,Direction.NORTH,true));set(x,-7,-99,TownStreetlightBlock.of(TownStreetlightBlock.Kind.HEAD,Direction.NORTH,true));}
        // Two wells still breathe: soul sand under a bubbling column.
        for(var well:new int[][]{{4,-106},{24,-100}}){int x=well[0],z=well[1];
            for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(dx!=0||dz!=0)set(x+dx,-11,z+dz,Blocks.MOSSY_STONE_BRICKS);
            set(x,-12,z,Blocks.SOUL_SAND);for(int y=-11;y<=-1;y++)set(x,y,z,Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(BubbleColumnBlock.DRAG_DOWN,false));}
        ruin(8,-108);ruin(-24,-106);ruin(48,-64);
        church();
    }
    private void ruin(int x0,int z0){
        for(int x=x0;x<=x0+6;x++)for(int z=z0;z>=z0-6;z--){boolean wall=x==x0||x==x0+6||z==z0||z==z0-6;if(!wall)continue;int h=-11+Math.floorMod(hash(x,z),3);
            for(int y=-11;y<=h;y++)if(hash(x,z+y)%5!=0)set(x,y,z,Blocks.MOSSY_COBBLESTONE);}
        set(x0+3,-11,z0-3,Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WATERLOGGED,true));
    }
    private void church(){
        // The nave, full of the lake, its door shut, its roof hatch under the water.
        int x0=22,x1=38,z0=-123,z1=-103;
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)for(int y=-12;y<=-4;y++){boolean shell=x==x0||x==x1||z==z0||z==z1||y==-12||y==-4;
            set(x,y,z,shell?(y==-12?Blocks.POLISHED_DEEPSLATE:y==-4?Blocks.DEEPSLATE_TILES:hash(x,y+z)%4==0?Blocks.MOSSY_STONE_BRICKS:Blocks.STONE_BRICKS).defaultBlockState():Blocks.WATER.defaultBlockState());}
        for(int z=-107;z>=-115;z-=2)for(int x:new int[]{25,26,27,33,34,35})set(x,-11,z,LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS,Direction.SOUTH).setValue(StairBlock.WATERLOGGED,true));
        set(30,-11,-117,Blocks.POLISHED_BLACKSTONE);set(30,-10,-117,Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState().setValue(SlabBlock.WATERLOGGED,true));
        BlockState gate=Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH);
        set(CHURCH_DOOR.getX(),CHURCH_DOOR.getY(),CHURCH_DOOR.getZ(),gate.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));
        set(CHURCH_DOOR.getX(),CHURCH_DOOR.getY()+1,CHURCH_DOOR.getZ(),gate.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        set(ROOF_HATCH.getX(),ROOF_HATCH.getY(),ROOF_HATCH.getZ(),Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.NORTH)
                .setValue(TrapDoorBlock.HALF,Half.TOP).setValue(TrapDoorBlock.WATERLOGGED,true));
        for(int z:new int[]{-108,-113,-118})for(int y=-9;y<=-7;y++){set(x0,y,z,Blocks.BLUE_STAINED_GLASS);set(x1,y,z,Blocks.BLUE_STAINED_GLASS);}
        lightWet(30,-6,-112,7);lightWet(30,-6,-106,6);
        // The steeple over the pulpit rises out of the lake; above the waterline inside it is air.
        for(int x=27;x<=33;x++)for(int z=-123;z<=-117;z++)for(int y=-4;y<=4;y++){boolean wall=x==27||x==33||z==-123||z==-117;
            set(x,y,z,wall?Blocks.DEEPSLATE_BRICKS.defaultBlockState():y<=-1?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState());}
        for(int[] side:new int[][]{{30,-117},{30,-123},{27,-120},{33,-120}})for(int y=2;y<=3;y++)set(side[0],y,side[1],Blocks.IRON_BARS);
        fill(27,5,-123,33,5,-117,Blocks.DEEPSLATE_TILES);
        for(int y=6;y<=7;y++)set(30,y,-120,Blocks.DEEPSLATE_BRICK_WALL);
        set(30,4,-120,Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT,BellAttachType.CEILING));
    }

    // ------------------------------------------------------------------------------------------------ the welcome and the note
    private void street(){
        set(SIGN.getX(),0,SIGN.getZ(),HouseBlocks.TOWN_SIGN.get().defaultBlockState().setValue(TownSignBlock.FACING,Direction.SOUTH));
        set(LECTERN.getX(),0,LECTERN.getZ(),DrownedTownRegistry.NOTICE.get().defaultBlockState().setValue(NoticePostBlock.FACING,Direction.SOUTH));
        BuildBlocks.after(l,()->{if(l.getBlockEntity(b.offset(LECTERN)) instanceof NoticePostBlockEntity post)post.book(note());});
    }
    static net.minecraft.world.item.ItemStack note(){return HouseWriting.book("At the waterline","An explorer",HouseWriting.WritingStyle.PLAIN,List.of(
        "Proofrock is on the bank. The older town is under Indian Lake.\n\nThe high school is at the top of Main Street, on the left, by the water.",
        "Three damp essays were left in the teachers' desks. Dry them in the furnace on the beach; the fuel is beside it.\n\nWhen all three are dry, leave and come back.",
        "She does not like the grass. She is not stopped by the water, only by whoever stays under it.\n\nShe waits where it is dark."));}

    // ------------------------------------------------------------------------------------------------ small helpers
    private void set(int x,int y,int z,Block block){set(x,y,z,block.defaultBlockState());}
    private void set(int x,int y,int z,BlockState state){BuildBlocks.set(l,b.offset(x,y,z),state,F);}
    private void setIfAir(int x,int y,int z,BlockState state){if(BuildBlocks.state(l,b.offset(x,y,z)).isAir())set(x,y,z,state);}
    private void fill(int x0,int y0,int z0,int x1,int y1,int z1,Block block){BuildBlocks.box(l,b.offset(x0,y0,z0),b.offset(x1,y1,z1),block.defaultBlockState(),F);}
    private void clear(int x0,int y0,int z0,int x1,int y1,int z1){BuildBlocks.box(l,b.offset(x0,y0,z0),b.offset(x1,y1,z1),Blocks.AIR.defaultBlockState(),F);}
    private void light(int x,int y,int z,int level){set(x,y,z,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,level));}
    private void lightWet(int x,int y,int z,int level){set(x,y,z,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,level).setValue(LightBlock.WATERLOGGED,true));}
    private void furniture(int x,int y,int z,HouseholdFurnitureBlock.Kind kind,Direction facing){NovelRooms.furniture(l,b.offset(x,y,z),kind,facing);}
    private void detail(int x,int y,int z,SceneDetailBlock.Kind kind,Direction facing){set(x,y,z,SceneDetailBlock.state(kind,facing));}
    private void poster(int x,int y,int z,Direction facing){set(x,y,z,TownFixtureBlock.of(TownFixtureBlock.Kind.POSTER,facing));}
    static BlockState slab(Block block){
        Block slab=block==Blocks.STONE_BRICKS?Blocks.STONE_BRICK_SLAB:block==Blocks.SPRUCE_PLANKS?Blocks.SPRUCE_SLAB:block==Blocks.DARK_OAK_PLANKS?Blocks.DARK_OAK_SLAB
            :block==Blocks.BRICKS?Blocks.BRICK_SLAB:block==Blocks.WHITE_CONCRETE?Blocks.SMOOTH_QUARTZ_SLAB:block==Blocks.RED_CONCRETE?Blocks.RED_NETHER_BRICK_SLAB
            :block==Blocks.BLUE_CONCRETE?Blocks.WARPED_SLAB:block==Blocks.ORANGE_TERRACOTTA?Blocks.CUT_RED_SANDSTONE_SLAB:Blocks.SMOOTH_STONE_SLAB;
        return slab.defaultBlockState();
    }
    /** A fence post, joined on one side when something hangs from it, so it reads as connected. */
    public static BlockState fence(Block fence,Direction joined){
        var state=fence.defaultBlockState();if(joined==null)return state;
        return state.setValue(switch(joined){case NORTH->CrossCollisionBlock.NORTH;case SOUTH->CrossCollisionBlock.SOUTH;case EAST->CrossCollisionBlock.EAST;default->CrossCollisionBlock.WEST;},true);
    }
    static int hash(int x,int z){int h=x*734287+z*912931;h^=h>>>13;h*=0x5bd1e995;h^=h>>>15;return Math.floorMod(h,1000);}
}
