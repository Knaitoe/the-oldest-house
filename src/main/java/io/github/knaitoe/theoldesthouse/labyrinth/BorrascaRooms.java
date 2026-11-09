package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** New builds: one rising pine trail read as childhood, loss, denial and ordinary industry; BorrascaTown builds its buildings. */
public final class BorrascaRooms {
    public static final BlockPos SOURCE=new BlockPos(3,1,-5),ACCOUNT=new BlockPos(-3,1,-5),CARVING=new BlockPos(0,0,-98);
    public static final BlockPos MARKER_ONE=new BlockPos(5,0,-76),MARKER_TWO=new BlockPos(7,4,-123),MARKER_THREE=new BlockPos(5,9,-148),MARKER_FOUR=new BlockPos(5,12,-165);
    public static final BlockPos SHERIFF=new BlockPos(2,4,-122),TAPE=new BlockPos(2,5,-125),TAKING=new BlockPos(-2,9,-148),DROP=new BlockPos(1,10,-151),HIDE=new BlockPos(-12,9,-151);
    static final BlockPos ELI_POSTER=new BlockPos(-15,1,-24),WREN_POSTER=new BlockPos(-15,1,-22);
    static final BlockPos YARD_PATROL_START=new BlockPos(2,12,-174);
    public static final BlockPos SHELTER_ONE=new BlockPos(3,2,-114),SHELTER_TWO=new BlockPos(-2,6,-131);
    public static final BlockPos GATE=new BlockPos(0,12,-169),LOCK=new BlockPos(0,13,-167),GATE_LOG=new BlockPos(5,13,-170),PACK=new BlockPos(-24,13,-195);
    public static final BlockPos EVIDENCE=new BlockPos(-26,13,-195),POWER=new BlockPos(6,12,-183),RELEASE=new BlockPos(-15,12,-205),MACHINE=new BlockPos(0,12,-189),FACTORY_DOOR=new BlockPos(-17,12,-169),LAMP=new BlockPos(0,17,-184);
    public static final BlockPos[] GULLY={new BlockPos(-9,0,-97),new BlockPos(-15,0,-107),new BlockPos(-16,2,-117),new BlockPos(-13,4,-128),new BlockPos(-8,6,-137),new BlockPos(-2,8,-143)};
    /**
     * The way down the far side from the service gate, outside the wire all the way (0.4.235: it used to run along the west fence and
     * cut it open, so a child could walk into the yard on the first visit).
     */
    public static final BlockPos[] RETURN_ROUTE={new BlockPos(-31,11,-209),new BlockPos(-33,11,-205),new BlockPos(-33,8,-189),new BlockPos(-33,4,-165),new BlockPos(-27,0,-131),new BlockPos(-18,0,-106),new BlockPos(-12,0,-73)};
    /** The depot's iron back door and the gate in the west fence the service tunnel ends at; the release opens both on the last night. */
    public static final BlockPos SERVICE_DOOR=new BlockPos(-17,12,-206),SERVICE_GATE=new BlockPos(-30,12,-209);
    /** 0.4.121: the trail map in the station, the noticeboard on the store's porch and the friend's notes in the fort. */
    public static final BlockPos TRAIL_MAP=new BlockPos(6,1,-7),NOTES=new BlockPos(-1,7,-99);
    public static final BlockPos[] BOARD={new BlockPos(-15,1,-21),new BlockPos(-15,1,-20),new BlockPos(-15,2,-21),new BlockPos(-15,2,-20)};
    /** 0.4.262: the men's truck stands in the turning bay at the top of the ridge road, nose to the road, out of sight behind the shoulder. */
    public static final BlockPos TRUCK=new BlockPos(35,12,-199);
    /**
     * 0.4.237: the rooms under the depot. A hatch in the back room's floor and a ladder down; a corridor with six rooms either side,
     * every one bolted from the corridor (a lever by each door); at its far end a desk with the night book and a letter that was never
     * posted. The east rooms are numbered 1 to 6 from the ladder, the west 7 to 12. Nell's is 6, open and empty, her name scratched beside
     * the reader's; Mara's is 7, by the ladder, bolted.
     */
    public static final BlockPos HATCH=new BlockPos(-19,11,-202),NIGHT_BOOK=new BlockPos(-19,7,-172),LETTER=new BlockPos(-18,7,-172),NELL_WALL=new BlockPos(-15,7,-181);
    public static final int[] ROOM_Z={-199,-195,-191,-187,-183,-179};
    public static final int NELL_ROOM=6,MARA_ROOM=7;
    /** The numbers in the ground behind the shed: the tags before Nell's. */
    public static final String[] GRAVE_TAGS={"211-A","214-A","219-A","222-A","226-A","230-A","234-A","238-A","241-A","245-A","247-A","251-A","255-A","258-A","260-A"};
    public static BlockPos grave(int i){return new BlockPos(-6+3*(i%5),12,-202-3*(i/5));}
    /** A room's door, by number: east rooms open onto x -17, west rooms onto x -21. */
    public static BlockPos roomDoor(int room){return room<=6?new BlockPos(-17,6,ROOM_Z[room-1]):new BlockPos(-21,6,ROOM_Z[room-7]);}
    public static BlockPos roomLever(int room){return room<=6?new BlockPos(-18,7,ROOM_Z[room-1]+1):new BlockPos(-20,7,ROOM_Z[room-7]+1);}
    /** Inside a room: the three-by-three floor behind its door. */
    public static boolean inRoom(int room,double x,double y,double z){int x0=room<=6?-16:-24,zc=room<=6?ROOM_Z[room-1]:ROOM_Z[room-7];return y<9&&x>=x0&&x<x0+3&&z>=zc-1&&z<zc+2;}
    public static boolean inLowerRooms(double x,double y,double z){return y<9.5&&x>=-25&&x<-12&&z>=-203&&z<-171;}
    // ---- 0.4.420: the longer hill ------------------------------------------------------------------------------------
    /*
     * The owner, November 9: "move the triple tree farther up the hill - expand the hill totally, so it's a walk to the tree and a
     * farther walk to the mill. The walk gives Nell and the player and other potential npcs ... time to talk and bond."
     *
     * Every place on the hill keeps the coordinates it was authored at (the constants here, the builders, the story). The world puts
     * them farther up: the town is where it was, and runs on north past its houses to TOWN_END, where the woods begin; the first marker,
     * the Triple Tree and the fort are 160 blocks farther north and 6 higher; everything from the second marker on (the blockade, the
     * gully's head, the clearing, the lane, the wire, the depot and all under and behind it) 252 farther and 16 higher. Between them the
     * slope is drawn out: a walk through the woods from the trailhead to the Tree, and a long climb from the Tree to the second marker,
     * the trail swinging from side to side. The clearing, the lane and the wire keep their own distances, so the light on the ridge
     * still finds the clearing. world() and authored() turn one into the other; tree() and ridge() are the bases those regions are built
     * from.
     */
    private static final int[] FROM={0,-74,-75,-110,-118},TO={0,-74,-235,-270,-370},LIFT={0,0,6,6,16};
    /** 0.4.420: the town runs on north past its houses to here before the woods begin (the shops of 0.4.421 stand along it). */
    public static final int TOWN_END=-134;
    /** How far the trail swings from side to side on each drawn-out slope (they alternate with the authored regions). */
    private static final double[] MEANDER={0,7,0,5,0};
    /** The world's far end of the hill, relative to the base (the authored -220). */
    public static final int FAR=-472,TOP=44;
    /** Which part of the map a row is in. A region keeps both its end rows (its own ground, steps and all); a slope has only the rows between. */
    private static int segment(double z){if(z>=FROM[1])return 0;for(int i=1;i<FROM.length-1;i++)if(i%2==1?z>FROM[i+1]:z>=FROM[i+1])return i;return FROM.length-1;}
    private static int worldSegment(double w){if(w>=TO[1])return 0;for(int i=1;i<TO.length-1;i++)if(i%2==1?w>TO[i+1]:w>=TO[i+1])return i;return TO.length-1;}
    /** Whether an authored z lies on one of the drawn-out slopes (otherwise its region is only moved). */
    public static boolean drawnOut(double z){return segment(z)%2==1;}
    public static boolean drawnOutWorld(double w){return worldSegment(w)%2==1;}
    public static double worldZ(double z){int i=segment(z);if(i==FROM.length-1||i==0)return z+(TO[i]-FROM[i]);return TO[i]+(z-FROM[i])*(TO[i+1]-TO[i])/(double)(FROM[i+1]-FROM[i]);}
    public static double authoredZ(double w){int i=worldSegment(w);if(i==TO.length-1||i==0)return w-(TO[i]-FROM[i]);return FROM[i]+(w-TO[i])*(FROM[i+1]-FROM[i])/(double)(TO[i+1]-TO[i]);}
    /** For a world z on a drawn-out slope: its authored ends, their world rows and their lifts. */
    public static int[] slope(double w){int i=worldSegment(w);return new int[]{FROM[i],FROM[i+1],TO[i],TO[i+1],LIFT[i],LIFT[i+1]};}
    /** How far along a drawn-out slope a world z is, 0 at its town end and 1 at its far end. */
    public static double along(double w){int i=worldSegment(w);double from=i==1?Math.min(TO[1],TOWN_END):TO[i];if(w>from)return 0;return (w-from)/(double)(TO[i+1]-from);}
    /** The trail's world height and line on the hill, for any world z. */
    public static double trailYw(double w){int i=worldSegment(w);if(i%2==0){double z=authoredZ(w);return trailY((int)Math.round(z))+LIFT[i];}double t=along(w);double a=trailY(FROM[i])+LIFT[i],c=trailY(FROM[i+1])+LIFT[i+1];return a+(c-a)*t;}
    public static double trailXw(double w){int i=worldSegment(w);if(i%2==0)return trailX((int)Math.round(authoredZ(w)));double t=along(w);return trailX(FROM[i])+(trailX(FROM[i+1])-trailX(FROM[i]))*t+MEANDER[i]*Math.sin(2*Math.PI*t);}
    public static BlockPos trailWorld(int w){return new BlockPos((int)Math.round(trailXw(w)),(int)Math.round(trailYw(w)),w);}
    /** How much higher the world stands than the authored height at an authored z. */
    public static double lift(double z){int i=segment(z);if(i%2==0)return LIFT[i];double w=worldZ(z);return trailYw(w)-trailY((int)Math.round(z));}
    /** How far the drawn-out trail has swung from its authored line at an authored z (nothing, in an authored region). Whatever lies beside it swings with it. */
    public static double sway(double z){int i=segment(z);if(i%2==0)return 0;return trailXw(worldZ(z))-trailX((int)Math.round(z));}
    public static BlockPos world(BlockPos a){return new BlockPos(a.getX()+(int)Math.round(sway(a.getZ())),a.getY()+(int)Math.round(lift(a.getZ())),(int)Math.round(worldZ(a.getZ())));}
    public static Vec3 world(Vec3 a){return new Vec3(a.x+sway(a.z),a.y+lift(a.z),worldZ(a.z));}
    /** Whether the world between two world z crosses a drawn-out slope. */
    static boolean crosses(double w0,double w1){double lo=Math.min(w0,w1),hi=Math.max(w0,w1);for(int i=1;i<TO.length-1;i+=2)if(TO[i+1]<hi&&TO[i]>lo)return true;return false;}
    /**
     * Where to head next on the way from authored a to authored c, from where one stands now (relative to the base, in the world):
     * c itself, unless the step crosses a drawn-out slope; then a point a few blocks ahead along the step as the hill draws it out, so
     * whoever walks it keeps to the trail (or beside it) instead of cutting across the woods.
     */
    public static Vec3 toward(Vec3 now,BlockPos a,BlockPos c){
        var wc=Vec3.atBottomCenterOf(world(c));double wa=worldZ(a.getZ()),wz=worldZ(c.getZ());if(!crosses(wa,wz))return wc;
        int n=Math.max(2,(int)Math.ceil(Math.abs(wz-wa)/3));int best=0;double bd=Double.MAX_VALUE;var pts=new Vec3[n+1];
        for(int k=0;k<=n;k++){double w=wa+(wz-wa)*k/n,z=authoredZ(w),t=c.getZ()==a.getZ()?1:(z-a.getZ())/(c.getZ()-a.getZ());
            pts[k]=world(new Vec3(a.getX()+.5+(c.getX()-a.getX())*t,a.getY()+(c.getY()-a.getY())*t,z));double d=pts[k].subtract(now).horizontalDistanceSqr();if(d<bd){bd=d;best=k;}}
        return best+2>=n?wc:pts[best+2];
    }
    public static BlockPos authored(BlockPos w){double z=authoredZ(w.getZ());return new BlockPos((int)Math.round(w.getX()-sway(z)),(int)Math.round(w.getY()-lift(z)),(int)Math.round(z));}
    public static Vec3 authored(Vec3 w){double z=authoredZ(w.z);return new Vec3(w.x-sway(z),w.y-lift(z),z);}
    /** An authored place on this reader's hill, in the world. */
    public static BlockPos at(BlockPos b,BlockPos authored){return b.offset(world(authored));}
    public static BlockPos at(BlockPos b,int x,int y,int z){return at(b,new BlockPos(x,y,z));}
    /** Where a reader (or anything) stands on the hill, in authored coordinates. */
    public static Vec3 rel(Entity e,BlockPos b){return authored(e.position().subtract(Vec3.atLowerCornerOf(b)));}
    /** The bases the moved regions are built from: authored coordinates written from these land where world() puts them. */
    public static BlockPos tree(BlockPos b){return b.offset(0,LIFT[2],TO[2]-FROM[2]);}
    public static BlockPos ridge(BlockPos b){return b.offset(0,LIFT[4],TO[4]-FROM[4]);}
    public static BlockPos depot(BlockPos b){return ridge(b);}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private BorrascaRooms(){}
    static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,Block block){LiteraryRooms.box(l,b,x,y,z,X,Y,Z,block);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,Block block){BuildBlocks.set(l,b.offset(x,y,z),block.defaultBlockState(),F);}
    static void fit(ServerLevel l,BlockPos b,BlockPos at,BorrascaFittingBlock.Kind k,Direction d){BuildBlocks.set(l,b.offset(at),BorrascaFittingBlock.of(k,d),F);}
    private static void furniture(ServerLevel l,BlockPos b,BlockPos at,HouseholdFurnitureBlock.Kind k){NovelRooms.furniture(l,b.offset(at),k,Direction.SOUTH);}
    private static void sign(ServerLevel l,BlockPos b,BlockPos at,String... words){NovelRooms.sign(l,b.offset(at),Direction.SOUTH,words);}
    private static int mix(int x,int z){int h=x*73856093^z*83492791^0x2b1;h^=h>>>13;h*=0x5bd1e995;return (h^h>>>15)&0x7fffffff;}
    /**
     * 0.4.276: a walked surface: packed earth where feet go (out to one off the line). 0.4.277: at its edge the Ozark woods' floor comes
     * back, leaves and grass, not taiga needles.
     */
    static net.minecraft.world.level.block.state.BlockState walked(int x,int z,int off){
        int k=mix(x,z)%10;
        if(off<=1)return Blocks.DIRT_PATH.defaultBlockState();
        return (k<4?Blocks.COARSE_DIRT:k<8?Blocks.GRASS_BLOCK:Blocks.DIRT_PATH).defaultBlockState();
    }
    /** A timber riser where a path climbs a block. */
    static net.minecraft.world.level.block.state.BlockState riser(Direction up){return Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,up);}
    public static int trailX(int z){return z>=-100?0:(int)Math.round(4*Math.sin((-z-100)*.08));}
    public static int trailY(int z){return Math.min(12,Math.max(0,(-z-100)/5));}
    public static BlockPos trail(int z){return new BlockPos(trailX(z),trailY(z),z);}
    public static void build(ServerLevel l,BlockPos b){
        var tree=tree(b);var ridge=ridge(b);var depot=depot(b);
        box(l,b,-44,-5,FAR,44,TOP,2,Blocks.AIR);box(l,b,-44,-5,FAR,44,-2,2,Blocks.STONE);box(l,b,-44,-1,FAR,44,-1,2,Blocks.GRASS_BLOCK);
        // 0.4.262: the valley, not a staircase of full-width terraces with walls down both sides (BorrascaLand).
        BorrascaLand.terrain(l,b);
        // 0.4.276: a walked path, not a painted strip: packed earth down the middle, its edges giving way to the needles, and a timber
        // riser at each rise. 0.4.420: walked in the world, from the trailhead to the lane below the wire, the long way.
        int top=(int)Math.round(worldZ(-166)),last=0;
        for(int z=-8;z>=top;z--){var p=trailWorld(z);box(l,b,p.getX()-2,p.getY(),z,p.getX()+2,p.getY()+3,z,Blocks.AIR);for(int dx=-2;dx<=2;dx++)BuildBlocks.set(l,b.offset(p.offset(dx,-1,0)),walked(p.getX()+dx,z,Math.abs(dx)),F);if(z<-8&&p.getY()>last){int band=drawnOutWorld(z)?5:2;for(int dx=-band;dx<=band;dx++)BuildBlocks.set(l,b.offset(p.offset(dx,-1,0)),riser(Direction.NORTH),F);}last=p.getY();}
        BorrascaTown.fort(l,tree);BorrascaDressing.tree(l,tree);gully(l,ridge);compound(l,depot);BorrascaDressing.yard(l,depot);serviceDescent(l,depot);lowerRooms(l,depot);graves(l,depot);
        // The sealed store under the far bank, where whoever is not about is kept out of the way: it stays at the town end.
        box(l,b,34,-5,-61,41,-1,-53,Blocks.STONE_BRICKS);box(l,b,35,-4,-60,40,-2,-54,Blocks.AIR);
        marker(l,tree,MARKER_ONE,BorrascaFittingBlock.Kind.MARKER_ONE);marker(l,ridge,MARKER_TWO,BorrascaFittingBlock.Kind.MARKER_TWO);marker(l,ridge,MARKER_THREE,BorrascaFittingBlock.Kind.MARKER_THREE);marker(l,depot,MARKER_FOUR,BorrascaFittingBlock.Kind.MARKER_FOUR);
        path(l,ridge,trail(-125),SHELTER_TWO);path(l,ridge,SHELTER_TWO,new BlockPos(1,6,-131));path(l,ridge,new BlockPos(1,6,-131),trail(-141));
        // The shelter branch starts on this rise; its floor must not replace the main trail's half-step.
        for(int dx=-2;dx<=2;dx++)BuildBlocks.set(l,ridge.offset(trail(-125).offset(dx,-1,0)),riser(Direction.NORTH),F);
        // The first shelter lies on the drawn-out climb between the Tree and the second marker: a log beside the trail, wherever the trail is.
        {BlockPos a=world(new BlockPos(1,2,-115)),c=world(new BlockPos(5,2,-115));box(l,b,a.getX(),a.getY(),a.getZ(),c.getX(),a.getY(),a.getZ(),Blocks.SPRUCE_LOG);}
        box(l,ridge,-5,6,-132,-1,6,-132,Blocks.SPRUCE_LOG);
        BorrascaLand.clearing(l,ridge);
        for(int x=-3;x<=1;x++)BuildBlocks.set(l,ridge.offset(x,8,-144),riser(Direction.NORTH),F);
        // The lane up from the clearing to the wire, the way the men come down and go back up: a step at each rise, so it can be walked
        // both ways. 0.4.420: it lies past the second marker, raised and moved up the hill with everything there.
        for(int z=-151;z>=-163;z--){int y=trailY(z);for(int x=0;x<=4;x++)BuildBlocks.set(l,ridge.offset(x,y-1,z),walked(x,z,Math.abs(x-2)),F);box(l,ridge,0,y,z,4,y+3,z,Blocks.AIR);}
        for(int z:new int[]{-150,-155,-160})for(int x=0;x<=4;x++)BuildBlocks.set(l,ridge.offset(x,trailY(z)-1,z),riser(Direction.NORTH),F);
        BorrascaLand.road(l,depot);
        BorrascaTown.town(l,b);BorrascaDressing.town(l,b);BorrascaShops.build(l,b);
        BorrascaLand.forest(l,b);
        for(var tree0:new BlockPos[]{BorrascaPlay.EAST_TREE,BorrascaPlay.WEST_TREE})
            for(int height=0;height<3;height++)BuildBlocks.set(l,b.offset(tree0.above(height)),Blocks.OAK_LOG.defaultBlockState(),F);
        furniture(l,b,SOURCE.below(),HouseholdFurnitureBlock.Kind.WALNUT_DESK);furniture(l,b,ACCOUNT.below(),HouseholdFurnitureBlock.Kind.WALNUT_DESK);
        LiteraryRooms.paper(l,b,SOURCE,BorrascaTexts.source(),LiteraryRooms.holder(LabyrinthPlace.BORRASCA));LiteraryRooms.prop(l,b,ACCOUNT,LiteraryPropBlock.Kind.LEDGER,Direction.SOUTH);
        NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.STONE_BRICKS.defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState());NovelRooms.door(l,b.offset(0,0,1),Direction.SOUTH,Blocks.DARK_OAK_DOOR,false);
    }
    private static void gully(ServerLevel l,BlockPos b){
        // 0.4.276: the gully's bed, banks and steps come with the ground (BorrascaLand.terrain), as one channel the water cut.
        for(int x=-17;x<=-15;x++)for(int z=-120;z<=-118;z++)BuildBlocks.set(l,b.offset(x,4,z),Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP),F);
        BorrascaLand.rock(l,b,-6.5,7.4,-122,2.2,2.1,2.6,null,null);box(l,b,-15,4,-130,-13,4,-129,Blocks.SPRUCE_LOG);
    }
    private static void compound(ServerLevel l,BlockPos b){
        box(l,b,-30,11,-214,27,11,-165,Blocks.GRAVEL);box(l,b,-30,12,-214,27,24,-166,Blocks.AIR);
        for(int x=-30;x<=27;x++)for(int y=12;y<=15;y++){fit(l,b,new BlockPos(x,y,-167),BorrascaFittingBlock.Kind.FENCE,Direction.SOUTH);fit(l,b,new BlockPos(x,y,-214),BorrascaFittingBlock.Kind.FENCE,Direction.SOUTH);}
        for(int z=-213;z<=-168;z++)for(int y=12;y<=15;y++)for(int x:new int[]{-30,27})fit(l,b,new BlockPos(x,y,z),BorrascaFittingBlock.Kind.FENCE,Direction.EAST);
        for(int x:new int[]{-2,2})box(l,b,x,12,-167,x,16,-167,Blocks.IRON_BLOCK);for(int x=-1;x<=1;x++)for(int y=12;y<=14;y++)fit(l,b,new BlockPos(x,y,-167),BorrascaFittingBlock.Kind.FENCE,Direction.SOUTH);fit(l,b,LOCK,BorrascaFittingBlock.Kind.LOCKED_FENCE,Direction.SOUTH);
        BorrascaTown.depot(l,b);
        for(int z=-178;z>=-200;z--)fit(l,b,new BlockPos(0,12,z),BorrascaFittingBlock.Kind.BELT,Direction.NORTH);
        for(int y=13;y<=16;y++)for(int x=-2;x<=2;x++)for(int z=-191;z<=-187;z++)if(Math.abs(x)==2||z==-191||z==-187)fit(l,b,new BlockPos(x,y,z),BorrascaFittingBlock.Kind.POLISHED,Direction.NORTH);
        BuildBlocks.set(l,b.offset(MACHINE),BorrascaRegistry.MACHINE.get().defaultBlockState(),F);box(l,b,3,12,-184,3,17,-184,Blocks.IRON_BARS);box(l,b,1,17,-184,3,17,-184,Blocks.IRON_BARS);join(l,b,1,12,-184,3,17,-184);fit(l,b,LAMP,BorrascaFittingBlock.Kind.LAMP,Direction.SOUTH);
        BuildBlocks.set(l,b.offset(POWER),Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE,AttachFace.FLOOR).setValue(LeverBlock.POWERED,true),F);fit(l,b,POWER.below(),BorrascaFittingBlock.Kind.BREAKER,Direction.SOUTH);
        for(var e:ElkLot.pickupStates(b.offset(0,12,0),12,-180,Direction.NORTH,2,false).entrySet())BuildBlocks.set(l,e.getKey(),e.getValue(),F);
        for(var e:ElkLot.pickupStates(b.offset(0,12,0),TRUCK.getX(),TRUCK.getZ(),Direction.SOUTH,3,false).entrySet())BuildBlocks.set(l,e.getKey(),e.getValue(),F);
        for(int z=-180;z>=-201;z--)box(l,b,-11,12,z,-11,14,z,Blocks.IRON_BARS);join(l,b,-11,12,-201,-11,14,-180);box(l,b,-11,12,-179,-11,13,-179,Blocks.AIR);
    }
    /**
     * The service exit: the depot's iron back door opens into a low tunnel walled off from the yard, which runs west under the wire to
     * an iron gate in the fence; outside it the way goes down the far side, outside the wire all the way. Until the release is pulled
     * both doors stay shut and the fence is whole, so the yard cannot be reached from the woods (0.4.235).
     */
    private static void serviceDescent(ServerLevel l,BlockPos b){
        box(l,b,-17,12,-204,-17,13,-204,Blocks.AIR);
        box(l,b,-18,12,-207,-14,15,-206,Blocks.STONE_BRICKS);NovelRooms.door(l,b.offset(SERVICE_DOOR),Direction.NORTH,Blocks.IRON_DOOR,false);
        // The tunnel: two blocks wide, roofed low enough that an adult must crouch, walled on both sides.
        box(l,b,-30,11,-210,-16,14,-207,Blocks.STONE_BRICKS);
        box(l,b,-29,12,-209,-17,12,-208,Blocks.AIR);
        for(int x=-29;x<=-17;x++)for(int z=-209;z<=-208;z++)BuildBlocks.set(l,b.offset(x,13,z),Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP),F);
        set(l,b,-17,12,-207,Blocks.AIR);BuildBlocks.set(l,b.offset(-17,13,-207),Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP),F);
        NovelRooms.door(l,b.offset(SERVICE_GATE),Direction.WEST,Blocks.IRON_DOOR,false);
        // The west fence, whole again wherever the way down touched it.
        for(int z=-213;z<=-168;z++){if(z>=-210&&z<=-207)continue;for(int y=12;y<=15;y++)fit(l,b,new BlockPos(-30,y,z),BorrascaFittingBlock.Kind.FENCE,Direction.EAST);}
        BuildBlocks.set(l,b.offset(RELEASE),Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE,AttachFace.FLOOR),F);sign(l,b,RELEASE.above(2),"SERVICE EXIT","Mechanical release","Keep low","Return to trail");
    }
    /** 0.4.237: the rooms under the depot (see HATCH). Nothing in them is shown happening; what they were for is in the book. */
    private static void lowerRooms(ServerLevel l,BlockPos b){
        box(l,b,-25,5,-203,-13,9,-171,Blocks.LIGHT_GRAY_CONCRETE);
        // The hatch in the back room's floor, and a ladder down through the rock.
        BuildBlocks.set(l,b.offset(HATCH),Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP).setValue(TrapDoorBlock.FACING,Direction.NORTH).setValue(TrapDoorBlock.OPEN,false),F);
        box(l,b,-19,6,-202,-19,10,-202,Blocks.AIR);
        for(int y=6;y<=10;y++)BuildBlocks.set(l,b.offset(-19,y,-202),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),F);
        // The corridor, and its far end.
        box(l,b,-20,6,-201,-18,8,-172,Blocks.AIR);box(l,b,-18,6,-202,-18,8,-202,Blocks.AIR);
        for(int z:new int[]{-196,-186,-176})BuildBlocks.set(l,b.offset(-19,8,z),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);
        NovelRooms.furniture(l,b.offset(NIGHT_BOOK.below()),HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);NovelRooms.furniture(l,b.offset(LETTER.below()),HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);
        NovelRooms.furniture(l,b.offset(-20,6,-173),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        fit(l,b,NIGHT_BOOK,BorrascaFittingBlock.Kind.PAPER,Direction.SOUTH);fit(l,b,LETTER,BorrascaFittingBlock.Kind.PAPER,Direction.SOUTH);
        // Twelve rooms, three by three, each bolted from the corridor: a cot, a bucket, and nothing else.
        for(int room=1;room<=12;room++){
            boolean east=room<=6;int zc=ROOM_Z[(room-1)%6],x0=east?-16:-24,cot=east?-14:-24;
            box(l,b,x0,6,zc-1,x0+2,8,zc+1,Blocks.AIR);
            for(int z=zc-1;z<=zc;z++)BuildBlocks.set(l,b.offset(cot,6,z),Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP),F);
            set(l,b,east?-14:-24,6,zc+1,Blocks.CAULDRON);
            boolean open=room==NELL_ROOM;
            NovelRooms.door(l,b.offset(roomDoor(room)),east?Direction.WEST:Direction.EAST,Blocks.IRON_DOOR,open);
            BuildBlocks.set(l,b.offset(roomLever(room)),Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE,AttachFace.WALL).setValue(LeverBlock.FACING,east?Direction.WEST:Direction.EAST).setValue(LeverBlock.POWERED,open),F);
            var tag=room==NELL_ROOM?new String[]{"RM 6","263-A","TRANSFER","D-417"}:room==MARA_ROOM?new String[]{"RM 7","264-A","",""}:new String[]{"RM "+room,"","",""};
            NovelRooms.sign(l,b.offset(east?-18:-20,8,zc),east?Direction.WEST:Direction.EAST,tag);
        }
        // Her room: the blanket gone, her name scratched into the wall beside the reader's (the names are set for each reader).
        BuildBlocks.set(l,b.offset(NELL_WALL),BorrascaRegistry.CARVING.get().defaultBlockState().setValue(BorrascaCarvingBlock.STAGE,2).setValue(BorrascaCarvingBlock.WALL,true),F);
        // Mara's: a blanket folded on the cot.
        set(l,b,-24,7,ROOM_Z[0],Blocks.WHITE_CARPET);set(l,b,-24,7,ROOM_Z[0]-1,Blocks.WHITE_CARPET);
        // Older names, scratched low in other rooms, from other years.
        String[] older={"DANA","RUTH ANN","LISA P","JO","KATIE"};int[] rooms={2,4,9,10,12};
        for(int i=0;i<older.length;i++){int room=rooms[i];int zc=ROOM_Z[(room-1)%6];var at=new BlockPos(room<=6?-15:-23,6,zc-2);BuildBlocks.set(l,b.offset(at),BorrascaRegistry.CARVING.get().defaultBlockState().setValue(BorrascaCarvingBlock.STAGE,1).setValue(BorrascaCarvingBlock.WALL,true),F);final String name=older[i];BuildBlocks.after(l,()->BorrascaCarvingEntity.names(l,b.offset(at),name,""));}
    }
    /** 0.4.237: behind the shed, low mounds in rows, a number on a stake at the head of each. No names. */
    private static void graves(ServerLevel l,BlockPos b){
        for(int i=0;i<GRAVE_TAGS.length;i++){
            var head=grave(i);set(l,b,head.getX(),11,head.getZ()-1,Blocks.COARSE_DIRT);set(l,b,head.getX(),11,head.getZ()-2,Blocks.COARSE_DIRT);
            var at=b.offset(head);final String tag=GRAVE_TAGS[i];
            BuildBlocks.set(l,at,Blocks.SPRUCE_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION,8),F);
            BuildBlocks.after(l,()->{if(l.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign){var text=sign.getFrontText().setMessage(1,net.minecraft.network.chat.Component.literal(tag));sign.setText(text,true);sign.setWaxed(true);}});
        }
    }
    private static void marker(ServerLevel l,BlockPos b,BlockPos at,BorrascaFittingBlock.Kind k){box(l,b,at.getX(),at.getY(),at.getZ()-1,at.getX(),at.getY()+1,at.getZ()-1,Blocks.SPRUCE_LOG);fit(l,b,at.above(),k,Direction.SOUTH);}
    private static void path(ServerLevel l,BlockPos b,BlockPos a,BlockPos c){int n=Math.max(Math.abs(a.getX()-c.getX()),Math.abs(a.getZ()-c.getZ()));int previous=a.getY();for(int i=0;i<=n;i++){double t=n==0?0:(double)i/n;int x=(int)Math.round(a.getX()+(c.getX()-a.getX())*t),y=(int)Math.round(a.getY()+(c.getY()-a.getY())*t),z=(int)Math.round(a.getZ()+(c.getZ()-a.getZ())*t);box(l,b,x-1,y-2,z,x+1,y-1,z,Blocks.STONE);box(l,b,x-1,y,z,x+1,y+2,z,Blocks.AIR);for(int dx=-1;dx<=1;dx++){BuildBlocks.set(l,b.offset(x+dx,y-1,z),walked(x+dx,z,Math.abs(dx)),F);if(y!=previous){Direction facing=Math.abs(c.getZ()-a.getZ())>=Math.abs(c.getX()-a.getX())?(c.getZ()<a.getZ()?Direction.NORTH:Direction.SOUTH):(c.getX()<a.getX()?Direction.WEST:Direction.EAST);if(y<previous)facing=facing.getOpposite();BuildBlocks.set(l,b.offset(x+dx,y>previous?y-1:y,z),riser(facing),F);}}previous=y;}}
    static void join(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z){for(var rel:BlockPos.betweenClosed(x,y,z,X,Y,Z)){var at=b.offset(rel).immutable();BuildBlocks.after(l,()->{var s=l.getBlockState(at);if(!(s.getBlock() instanceof FenceBlock||s.getBlock() instanceof IronBarsBlock))return;for(var d:Direction.Plane.HORIZONTAL)s=s.updateShape(d,l.getBlockState(at.relative(d)),l,at,at.relative(d));l.setBlock(at,s,F);});}}
}
