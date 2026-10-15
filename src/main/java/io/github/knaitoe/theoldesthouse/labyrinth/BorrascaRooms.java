package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/** A continuous native town, remembered treehouse, switchback and inhabited industrial site. */
public final class BorrascaRooms {
    public static final BlockPos SOURCE=new BlockPos(3,1,-5),ACCOUNT=new BlockPos(-3,1,-5),CARVING=new BlockPos(0,7,-98),BEDROOM=new BlockPos(27,6,-56),REGISTER=new BlockPos(-25,1,-31),MAP=new BlockPos(-20,1,-37),NOTICE=new BlockPos(9,1,-47),SIGN=new BlockPos(7,10,-148),TRUCK=new BlockPos(12,12,-163),SERVICE=new BlockPos(-21,13,-171),EVIDENCE=new BlockPos(-26,13,-195),TESTIMONY=new BlockPos(-22,13,-202),POWER=new BlockPos(6,12,-183),RELEASE=new BlockPos(-15,12,-205),MACHINE=new BlockPos(0,12,-189),FACTORY_DOOR=new BlockPos(-17,12,-169);
    public static final BlockPos[] RETURN_ROUTE={new BlockPos(-17,12,-208),new BlockPos(-27,11,-209),new BlockPos(-32,8,-189),new BlockPos(-32,4,-165),new BlockPos(-27,0,-131),new BlockPos(-18,0,-106),new BlockPos(-12,0,-73)};
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private BorrascaRooms(){}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,Block block){LiteraryRooms.box(l,b,x0,y0,z0,x1,y1,z1,block);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,Block block){BuildBlocks.set(l,b.offset(x,y,z),block.defaultBlockState(),F);}
    static void fit(ServerLevel l,BlockPos b,BlockPos at,BorrascaFittingBlock.Kind k,Direction face){BuildBlocks.set(l,b.offset(at),BorrascaFittingBlock.of(k,face),F);}
    private static void furnished(ServerLevel l,BlockPos b,int x,int y,int z,HouseholdFurnitureBlock.Kind k,Direction face){NovelRooms.furniture(l,b.offset(x,y,z),k,face);}
    private static void door(ServerLevel l,BlockPos b,int x,int y,int z,Direction facing,Block block){box(l,b,x,y,z,x,y+1,z,Blocks.AIR);NovelRooms.door(l,b.offset(x,y,z),facing,block,false);}
    private static void lamp(ServerLevel l,BlockPos b,int x,int y,int z){set(l,b,x,y+1,z,Blocks.CHAIN);BuildBlocks.set(l,b.offset(x,y,z),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);}
    private static void sign(ServerLevel l,BlockPos b,int x,int y,int z,String a,String c,String d,String e){NovelRooms.sign(l,b.offset(x,y,z),Direction.SOUTH,new String[]{a,c,d,e});}
    /** Each connection is its own bounded construction command, after its neighbours exist. */
    private static void join(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1){for(var rel:BlockPos.betweenClosed(x0,y0,z0,x1,y1,z1)){var at=b.offset(rel).immutable();BuildBlocks.after(l,()->{var state=l.getBlockState(at);if(!(state.getBlock() instanceof FenceBlock||state.getBlock() instanceof IronBarsBlock))return;for(var side:Direction.Plane.HORIZONTAL){var next=at.relative(side);state=state.updateShape(side,l.getBlockState(next),l,at,next);}l.setBlock(at,state,F);});}}
    private static void house(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,int y,Block wall,Block floor){
        NovelRooms.room(l,b,x0,x1,z0,z1,y,5,wall,floor);
        for(int x:new int[]{x0,x1})for(int z:new int[]{z0,z1})box(l,b,x,y,z,x,y+5,z,Blocks.STRIPPED_SPRUCE_LOG);
        for(int x=x0-1;x<=x1+1;x++)for(int z=z0-1;z<=z1+1;z++){int rise=Math.min(Math.min(x-x0+1,x1+1-x),3);set(l,b,x,y+6+Math.max(0,rise),z,Blocks.DARK_OAK_PLANKS);}
        for(int x=x0+3;x<x1-1;x+=5)for(int z:new int[]{z0,z1}){box(l,b,x,y+2,z,x+1,y+3,z,Blocks.GLASS_PANE);join(l,b,x,y+2,z,x+1,y+3,z);}
        int dx=(x0+x1)/2;door(l,b,dx,y,z1,Direction.SOUTH,Blocks.SPRUCE_DOOR);box(l,b,dx-1,y-1,z1+1,dx+1,y-1,z1+2,Blocks.STONE_BRICKS);lamp(l,b,dx,y+3,z1-2);
    }
    public static void build(ServerLevel l,BlockPos b){
        // Fresh sites only. A built reader's landscape and every player edit remain untouched.
        box(l,b,-44,-5,-220,44,26,0,Blocks.AIR);box(l,b,-44,-5,-220,44,-2,0,Blocks.STONE);box(l,b,-44,-1,-220,44,-1,0,Blocks.GRASS_BLOCK);
        box(l,b,-5,-1,-71,5,-1,-9,Blocks.GRAY_CONCRETE);for(int z=-13;z>-69;z-=7)box(l,b,0,-1,z,0,-1,z-2,Blocks.YELLOW_TERRACOTTA);
        for(int x:new int[]{-8,8})box(l,b,x,-1,-70,x+1,-1,-10,Blocks.SMOOTH_STONE);
        // Bounded edges are wooded embankments and rock, not invisible interior walls.
        for(int z=-218;z<=-9;z+=8)for(int x:new int[]{-42,42})tree(l,b,x,z,0,9+Math.floorMod(z,4));
        box(l,b,-44,0,-220,44,22,-217,Blocks.STONE);for(int x:new int[]{-44,43})box(l,b,x,0,-218,x+1,5,-8,Blocks.MOSSY_COBBLESTONE);
        house(l,b,-32,-12,-42,-22,0,Blocks.BRICKS,Blocks.SPRUCE_PLANKS);sign(l,b,-22,2,-21,"DRISKING","COUNTY RECORDS","Survey office","Public entrance");
        for(int z=-38;z<=-27;z+=3){set(l,b,-30,0,z,Blocks.BOOKSHELF);set(l,b,-30,1,z,Blocks.BOOKSHELF);fit(l,b,new BlockPos(-25,0,z),BorrascaFittingBlock.Kind.DRAWERS,Direction.SOUTH);}
        furnished(l,b,-20,0,-37,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);furnished(l,b,-20,0,-34,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);fit(l,b,REGISTER.below(),BorrascaFittingBlock.Kind.DRAWERS,Direction.SOUTH);fit(l,b,REGISTER,BorrascaFittingBlock.Kind.PAPER,Direction.SOUTH);fit(l,b,MAP,BorrascaFittingBlock.Kind.PAPER,Direction.SOUTH);
        house(l,b,12,32,-37,-18,0,Blocks.WHITE_TERRACOTTA,Blocks.BIRCH_PLANKS);sign(l,b,22,2,-17,"DRISKING","DINER","Coffee / pie","Open daily");
        for(int z=-32;z<=-22;z+=5){furnished(l,b,17,0,z,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH);furnished(l,b,17,0,z+2,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);furnished(l,b,17,0,z-2,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);}
        for(int x=22;x<=29;x++)furnished(l,b,x,0,-34,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);
        // A complete two-storey home; the bedroom is reached by its native stair flight.
        house(l,b,13,34,-65,-44,0,LiteraryRegistry.SIDING.get(),Blocks.OAK_PLANKS);box(l,b,14,5,-64,33,5,-45,Blocks.OAK_PLANKS);box(l,b,13,6,-65,34,10,-44,LiteraryRegistry.SIDING.get());box(l,b,14,6,-64,33,10,-45,Blocks.AIR);box(l,b,13,11,-65,34,11,-44,Blocks.DARK_OAK_PLANKS);
        for(int i=0;i<6;i++){box(l,b,15,1+i,-48-i,17,2+i,-48-i,Blocks.AIR);for(int x=15;x<=17;x++)BuildBlocks.set(l,b.offset(x,i,-48-i),Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH),F);}
        box(l,b,15,5,-52,17,5,-48,Blocks.AIR);box(l,b,25,8,-44,28,9,-44,Blocks.GLASS_PANE);NovelRooms.bed(l,b.offset(30,6,-61),Blocks.YELLOW_BED,Direction.NORTH);furnished(l,b,27,6,-56,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);fit(l,b,BEDROOM.above(),BorrascaFittingBlock.Kind.PAPER,Direction.SOUTH);furnished(l,b,32,6,-55,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST);lamp(l,b,24,9,-57);
        for(int x:new int[]{-8,8})for(int z:new int[]{-15,-44,-68}){box(l,b,x,0,z,x,3,z,Blocks.DARK_OAK_FENCE);set(l,b,x,4,z,Blocks.LANTERN);}
        fit(l,b,NOTICE,BorrascaFittingBlock.Kind.NOTICE,Direction.WEST);sign(l,b,9,2,-47,"COMMUNITY","MISSING","Please ask at","the records office");
        // Three trunks, a braced platform, open trapdoor, a real seven-rung ladder and rain shelter.
        box(l,b,-3,-1,-105,3,-1,-72,Blocks.DIRT_PATH);for(int x:new int[]{-2,0,2})tree(l,b,x,-96+x,0,14);
        box(l,b,-5,5,-99,5,5,-88,Blocks.SPRUCE_PLANKS);for(int x:new int[]{-5,5})for(int z:new int[]{-99,-88})box(l,b,x,0,z,x,5,z,Blocks.SPRUCE_LOG);
        box(l,b,-5,6,-99,5,9,-99,Blocks.SPRUCE_PLANKS);for(int x:new int[]{-5,5})box(l,b,x,6,-98,x,8,-90,Blocks.SPRUCE_FENCE);box(l,b,-5,10,-100,5,10,-88,Blocks.SPRUCE_SLAB);
        // Dirt paths are 1/16 lower than a rung's block. A full-height timber apron lets ordinary walking engage the bottom rung.
        box(l,b,3,-1,-88,3,-1,-86,Blocks.SPRUCE_PLANKS);
        box(l,b,3,0,-89,3,7,-89,Blocks.SPRUCE_LOG);for(int y=0;y<=6;y++)BuildBlocks.set(l,b.offset(3,y,-88),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),F);box(l,b,3,6,-88,3,6,-88,Blocks.AIR);BuildBlocks.set(l,b.offset(3,6,-88),Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.SOUTH).setValue(TrapDoorBlock.OPEN,true),F);
        set(l,b,-1,7,-100,Blocks.SPRUCE_PLANKS);fit(l,b,CARVING,BorrascaFittingBlock.Kind.CARVING,Direction.SOUTH);sign(l,b,-1,7,-99,"KYLE / KIMBER","Keep a place","for whoever","comes back");furnished(l,b,-3,6,-96,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);
        // Gentle one-block rises carry the trail all the way to the twelve-block ridge.
        for(int z=-105;z>=-169;z--){int y=Math.min(12,(-z-105)/5);int x=(int)Math.round(8*Math.sin((-z-105)*.065));for(int dx=-2;dx<=2;dx++){if(y>0)box(l,b,x+dx,0,z,x+dx,y-1,z,Blocks.STONE);set(l,b,x+dx,y-1,z,Blocks.DIRT_PATH);if(z<-105&&(-z-105)%5==0)BuildBlocks.set(l,b.offset(x+dx,y-1,z),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH),F);}}
        box(l,b,-31,0,-214,27,10,-165,Blocks.STONE);box(l,b,-31,11,-214,27,11,-165,Blocks.GRAVEL);box(l,b,10,0,-165,14,11,-159,Blocks.STONE);box(l,b,-30,12,-213,26,24,-166,Blocks.AIR);
        for(int x=-8;x<=-4;x++)BuildBlocks.set(l,b.offset(x,11,-165),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH),F);
        house(l,b,-29,-14,-204,-169,12,Blocks.STONE_BRICKS,Blocks.POLISHED_ANDESITE);box(l,b,-28,17,-203,-15,17,-170,Blocks.SMOOTH_STONE);door(l,b,-17,12,-169,Direction.SOUTH,Blocks.IRON_DOOR);
        door(l,b,-14,12,-179,Direction.EAST,Blocks.SPRUCE_DOOR);box(l,b,-11,12,-179,-11,13,-179,Blocks.AIR);box(l,b,-17,12,-204,-17,13,-204,Blocks.AIR);
        // A visible lever opens the office. No invisible per-player wall is used.
        BuildBlocks.set(l,b.offset(-15,13,-168),Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE,AttachFace.WALL).setValue(LeverBlock.FACING,Direction.SOUTH),F);
        for(int z=-175;z>=-201;z-=5){furnished(l,b,-27,12,z,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.EAST);furnished(l,b,-24,12,z,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);}for(var paper:new BlockPos[]{SERVICE,EVIDENCE,TESTIMONY})furnished(l,b,paper.getX(),paper.getY()-1,paper.getZ(),HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);fit(l,b,SERVICE,BorrascaFittingBlock.Kind.PAPER,Direction.SOUTH);fit(l,b,EVIDENCE,BorrascaFittingBlock.Kind.PAPER,Direction.SOUTH);fit(l,b,TESTIMONY,BorrascaFittingBlock.Kind.PAPER,Direction.SOUTH);
        box(l,b,-11,11,-204,8,11,-176,Blocks.POLISHED_ANDESITE);for(int x:new int[]{-11,8})box(l,b,x,12,-204,x,18,-176,Blocks.IRON_BARS);box(l,b,-11,19,-204,8,19,-176,Blocks.DARK_OAK_PLANKS);for(int z=-177;z>=-199;z-=7)lamp(l,b,-8,17,z);
        for(int z=-178;z>=-199;z--){fit(l,b,new BlockPos(0,12,z),BorrascaFittingBlock.Kind.BELT,Direction.NORTH);for(int x:new int[]{-2,2})fit(l,b,new BlockPos(x,12,z),BorrascaFittingBlock.Kind.RUST,Direction.NORTH);}BuildBlocks.set(l,b.offset(MACHINE),BorrascaRegistry.MACHINE.get().defaultBlockState(),F);
        for(int y=13;y<=16;y++)for(int x=-2;x<=2;x++)for(int z=-191;z<=-187;z++)if(Math.abs(x)==2||z==-191||z==-187)fit(l,b,new BlockPos(x,y,z),BorrascaFittingBlock.Kind.STEEL,Direction.NORTH);
        BuildBlocks.set(l,b.offset(POWER),Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE,AttachFace.FLOOR).setValue(LeverBlock.POWERED,true),F);BuildBlocks.set(l,b.offset(RELEASE),Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE,AttachFace.FLOOR),F);
        for(int x:new int[]{11,13})for(int z=-165;z<=-160;z++)fit(l,b,new BlockPos(x,12,z),BorrascaFittingBlock.Kind.TRUCK,Direction.NORTH);box(l,b,11,13,-163,13,13,-161,Blocks.GREEN_TERRACOTTA);box(l,b,11,14,-163,13,14,-162,Blocks.BLACK_STAINED_GLASS);sign(l,b,12,13,-160,"PRESCOTT","ORE / SERVICE","D-417","Drisking");
        box(l,b,5,0,-149,5,12,-149,Blocks.SPRUCE_FENCE);box(l,b,9,0,-149,9,12,-149,Blocks.SPRUCE_FENCE);box(l,b,5,9,-150,9,12,-149,Blocks.SPRUCE_PLANKS);fit(l,b,SIGN,BorrascaFittingBlock.Kind.NOTICE,Direction.SOUTH);sign(l,b,7,11,-148,"   SKIN ","  ND"," MIN ","West rim");
        // A separately graded service descent. Its door is opened by the native release lever.
        box(l,b,-18,12,-207,-14,15,-206,Blocks.STONE_BRICKS);door(l,b,-17,12,-206,Direction.NORTH,Blocks.IRON_DOOR);
        for(int i=0;i<RETURN_ROUTE.length-1;i++)path(l,b,RETURN_ROUTE[i],RETURN_ROUTE[i+1]);
        sign(l,b,-15,14,-205,"FIRE EXIT","Release lever","Keep low under","the service pipe");for(int x=-21;x<=-15;x++)for(int z=-210;z<=-208;z++)BuildBlocks.set(l,b.offset(x,13,z),Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP),F);
        box(l,b,-23,12,-207,-18,15,-207,Blocks.STONE_BRICKS);box(l,b,-23,12,-210,-16,15,-210,Blocks.STONE_BRICKS);box(l,b,-16,12,-209,-16,15,-207,Blocks.STONE_BRICKS);
        box(l,b,34,-5,-61,41,-1,-53,Blocks.STONE_BRICKS);box(l,b,35,-4,-60,40,-2,-54,Blocks.AIR);
        for(int z:new int[]{-181,-191,-201})lamp(l,b,-22,15,z);
        for(int x:new int[]{-5,5})join(l,b,x,6,-98,x,8,-90);for(int x:new int[]{-11,8})join(l,b,x,12,-204,x,18,-176);for(int x:new int[]{5,9})join(l,b,x,9,-149,x,12,-149);join(l,b,25,8,-44,28,9,-44);
        // Reading porch and normal return, away from the committed evidence room.
        box(l,b,-5,-1,-9,5,-1,2,Blocks.STONE_BRICKS);for(int x:new int[]{-5,5})box(l,b,x,0,-8,x,4,0,Blocks.BRICKS);box(l,b,-5,5,-9,5,5,0,Blocks.DARK_OAK_PLANKS);furnished(l,b,3,0,-5,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);furnished(l,b,-3,0,-5,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);LiteraryRooms.paper(l,b,SOURCE,BorrascaTexts.source());LiteraryRooms.prop(l,b,ACCOUNT,LiteraryPropBlock.Kind.LEDGER,Direction.SOUTH);lamp(l,b,0,3,-6);
        NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,Blocks.BRICKS.defaultBlockState(),Blocks.STONE_BRICKS.defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState());NovelRooms.door(l,b.offset(0,0,1),Direction.SOUTH,Blocks.DARK_OAK_DOOR,false);
    }
    private static void path(ServerLevel l,BlockPos b,BlockPos a,BlockPos c){int n=Math.max(Math.abs(a.getX()-c.getX()),Math.abs(a.getZ()-c.getZ()));for(int i=0;i<=n;i++){double t=n==0?0:(double)i/n;int x=(int)Math.round(a.getX()+(c.getX()-a.getX())*t),y=(int)Math.round(a.getY()+(c.getY()-a.getY())*t),z=(int)Math.round(a.getZ()+(c.getZ()-a.getZ())*t);box(l,b,x-1,y-2,z,x+1,y-1,z,Blocks.STONE);box(l,b,x-1,y,z,x+1,y+2,z,Blocks.AIR);for(int dx=-1;dx<=1;dx++)set(l,b,x+dx,y-1,z,Blocks.DIRT_PATH);}}
    private static void tree(ServerLevel l,BlockPos b,int x,int z,int y,int h){box(l,b,x,y,z,x,y+h,z,Blocks.SPRUCE_LOG);for(int k=0;k<5;k++){int half=3-k/2;box(l,b,x-half,y+h-3+k,z-half,x+half,y+h-3+k,z+half,Blocks.SPRUCE_LEAVES);}}
}
