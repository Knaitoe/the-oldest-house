package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;

/** New builds: one rising pine trail read as childhood, loss, denial and ordinary industry. */
public final class BorrascaRooms {
    public static final BlockPos SOURCE=new BlockPos(3,1,-5),ACCOUNT=new BlockPos(-3,1,-5),CARVING=new BlockPos(0,1,-98);
    public static final BlockPos MARKER_ONE=new BlockPos(5,0,-76),MARKER_TWO=new BlockPos(7,4,-123),MARKER_THREE=new BlockPos(5,9,-148),MARKER_FOUR=new BlockPos(5,12,-165);
    public static final BlockPos SHERIFF=new BlockPos(2,4,-122),TAPE=new BlockPos(2,5,-125),TAKING=new BlockPos(-2,9,-148),DROP=new BlockPos(1,10,-151),HIDE=new BlockPos(-12,9,-151);
    public static final BlockPos SHELTER_ONE=new BlockPos(3,2,-114),SHELTER_TWO=new BlockPos(-2,6,-131);
    public static final BlockPos GATE=new BlockPos(0,12,-169),LOCK=new BlockPos(0,13,-168),PACK=new BlockPos(-24,13,-195),PATCH=PACK;
    public static final BlockPos EVIDENCE=new BlockPos(-26,13,-195),POWER=new BlockPos(6,12,-183),RELEASE=new BlockPos(-15,12,-205),MACHINE=new BlockPos(0,12,-189),FACTORY_DOOR=new BlockPos(-17,12,-169),LAMP=new BlockPos(0,16,-184);
    public static final BlockPos[] GULLY={new BlockPos(-9,0,-97),new BlockPos(-15,0,-107),new BlockPos(-16,2,-117),new BlockPos(-13,4,-128),new BlockPos(-8,6,-137),new BlockPos(-2,8,-143)};
    public static final BlockPos[] RETURN_ROUTE={new BlockPos(-17,12,-208),new BlockPos(-27,11,-209),new BlockPos(-32,8,-189),new BlockPos(-32,4,-165),new BlockPos(-27,0,-131),new BlockPos(-18,0,-106),new BlockPos(-12,0,-73)};
    public static final BlockPos BEDROOM=PACK,REGISTER=EVIDENCE,MAP=MARKER_TWO,NOTICE=SOURCE,SIGN=MARKER_FOUR,TRUCK=new BlockPos(12,12,-163),SERVICE=EVIDENCE,TESTIMONY=EVIDENCE;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private BorrascaRooms(){}
    static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,Block block){LiteraryRooms.box(l,b,x,y,z,X,Y,Z,block);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,Block block){BuildBlocks.set(l,b.offset(x,y,z),block.defaultBlockState(),F);}
    static void fit(ServerLevel l,BlockPos b,BlockPos at,BorrascaFittingBlock.Kind k,Direction d){BuildBlocks.set(l,b.offset(at),BorrascaFittingBlock.of(k,d),F);}
    private static void furniture(ServerLevel l,BlockPos b,BlockPos at,HouseholdFurnitureBlock.Kind k){NovelRooms.furniture(l,b.offset(at),k,Direction.SOUTH);}
    private static void sign(ServerLevel l,BlockPos b,BlockPos at,String... words){NovelRooms.sign(l,b.offset(at),Direction.SOUTH,words);}
    public static int trailX(int z){return z>=-100?0:(int)Math.round(4*Math.sin((-z-100)*.08));}
    public static int trailY(int z){return Math.min(12,Math.max(0,(-z-100)/5));}
    public static BlockPos trail(int z){return new BlockPos(trailX(z),trailY(z),z);}
    public static void build(ServerLevel l,BlockPos b){
        box(l,b,-44,-5,-220,44,26,2,Blocks.AIR);box(l,b,-44,-5,-220,44,-2,2,Blocks.STONE);box(l,b,-44,-1,-220,44,-1,2,Blocks.GRASS_BLOCK);
        for(int z=-101;z>=-216;z--){int y=trailY(z);if(y>0){if(y>1)box(l,b,-30,0,z,30,y-2,z,Blocks.STONE);box(l,b,-30,y-1,z,30,y-1,z,Blocks.PODZOL);}}
        for(int z=-8;z>=-166;z--){var p=trail(z);box(l,b,p.getX()-2,p.getY(),z,p.getX()+2,p.getY()+3,z,Blocks.AIR);for(int dx=-2;dx<=2;dx++)fit(l,b,p.offset(dx,-1,0),BorrascaFittingBlock.Kind.TRAIL,Direction.NORTH);if(z<-101&&(-z-100)%5==0)for(int dx=-2;dx<=2;dx++)BuildBlocks.set(l,b.offset(p.offset(dx,-1,0)),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH),F);}
        for(int z=-18;z>=-211;z-=11)for(int side:new int[]{-1,1}){int x=side*(12+Math.floorMod(z*7,23));int y=trailY(z);tree(l,b,x,z,y,9+Math.floorMod(z,5));if(z>-142&&z<-104)box(l,b,side<0?-7:8,y,z,side<0?-5:11,y+3,z+3,Blocks.MOSSY_COBBLESTONE);}
        for(int z=-211;z<=-9;z+=9)for(int x:new int[]{-41,40}){tree(l,b,x,z,trailY(z),10);box(l,b,x,0,z,x+1,trailY(z)+3,z+8,Blocks.STONE);}
        box(l,b,-44,0,-220,44,23,-217,Blocks.STONE);
        treehouse(l,b);gully(l,b);compound(l,b);serviceDescent(l,b);
        marker(l,b,MARKER_ONE,BorrascaFittingBlock.Kind.MARKER_ONE);marker(l,b,MARKER_TWO,BorrascaFittingBlock.Kind.MARKER_TWO);marker(l,b,MARKER_THREE,BorrascaFittingBlock.Kind.MARKER_THREE);marker(l,b,MARKER_FOUR,BorrascaFittingBlock.Kind.MARKER_FOUR);
        path(l,b,trail(-125),SHELTER_TWO);path(l,b,SHELTER_TWO,new BlockPos(1,6,-131));path(l,b,new BlockPos(1,6,-131),trail(-141));
        for(var at:new BlockPos[]{new BlockPos(1,2,-115),new BlockPos(-5,6,-132)})box(l,b,at.getX(),at.getY(),at.getZ(),at.getX()+4,at.getY(),at.getZ(),Blocks.SPRUCE_LOG);
        box(l,b,-14,8,-154,7,8,-144,Blocks.RED_TERRACOTTA);box(l,b,-14,9,-154,7,13,-144,Blocks.AIR);box(l,b,-9,9,-154,-7,14,-150,Blocks.SPRUCE_LOG);box(l,b,-10,9,-152,-8,12,-149,Blocks.MOSSY_COBBLESTONE);
        box(l,b,-15,8,-157,-13,14,-146,Blocks.STONE);box(l,b,-16,8,-157,-12,8,-148,Blocks.RED_TERRACOTTA);
        for(int z=-151;z>=-163;z--){int y=trailY(z);for(int x=0;x<=4;x++)fit(l,b,new BlockPos(x,y-1,z),BorrascaFittingBlock.Kind.TRAIL,Direction.NORTH);}
        for(int x=0;x<=4;x++)BuildBlocks.set(l,b.offset(x,9,-150),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH),F);
        box(l,b,5,-1,-26,15,-1,-14,Blocks.GRAVEL);vehicle(l,b,new BlockPos(8,0,-18),true);vehicle(l,b,new BlockPos(12,0,-25),false);
        sign(l,b,new BlockPos(5,1,-12),"OZARK PINE TRAIL","TRIPLE TREE","Stay this side","of marker TWO");
        furniture(l,b,SOURCE.below(),HouseholdFurnitureBlock.Kind.WALNUT_DESK);furniture(l,b,ACCOUNT.below(),HouseholdFurnitureBlock.Kind.WALNUT_DESK);
        LiteraryRooms.paper(l,b,SOURCE,BorrascaTexts.source());LiteraryRooms.prop(l,b,ACCOUNT,LiteraryPropBlock.Kind.LEDGER,Direction.SOUTH);
        NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.STONE_BRICKS.defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState());NovelRooms.door(l,b.offset(0,0,1),Direction.SOUTH,Blocks.DARK_OAK_DOOR,false);
    }
    private static void treehouse(ServerLevel l,BlockPos b){
        box(l,b,-8,-1,-105,7,-1,-82,Blocks.PODZOL);box(l,b,-8,0,-105,7,16,-82,Blocks.AIR);for(int x:new int[]{-3,0,4})tree(l,b,x,-100+Math.floorMod(x,3),0,17);
        box(l,b,-5,5,-100,5,5,-88,Blocks.SPRUCE_PLANKS);for(int x:new int[]{-5,5})for(int z:new int[]{-100,-89})box(l,b,x,0,z,x,5,z,Blocks.SPRUCE_LOG);
        for(int x:new int[]{-5,5})box(l,b,x,6,-100,x,8,-89,Blocks.SPRUCE_FENCE);box(l,b,-5,6,-100,5,8,-100,Blocks.SPRUCE_PLANKS);box(l,b,-5,10,-101,5,10,-88,Blocks.SPRUCE_SLAB);
        box(l,b,3,-1,-88,3,-1,-86,Blocks.SPRUCE_PLANKS);box(l,b,3,0,-89,3,7,-89,Blocks.SPRUCE_LOG);
        for(int y=0;y<=6;y++)BuildBlocks.set(l,b.offset(3,y,-88),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),F);
        BuildBlocks.set(l,b.offset(3,6,-88),Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.SOUTH).setValue(TrapDoorBlock.OPEN,true),F);box(l,b,3,7,-88,3,9,-88,Blocks.AIR);
        furniture(l,b,new BlockPos(-3,6,-95),HouseholdFurnitureBlock.Kind.CANE_CHAIR);set(l,b,-3,6,-99,Blocks.BARREL);
        box(l,b,-1,0,-99,1,4,-98,Blocks.SPRUCE_LOG);BuildBlocks.set(l,b.offset(CARVING),BorrascaRegistry.CARVING.get().defaultBlockState(),F);join(l,b,-5,6,-100,5,8,-89);
    }
    private static void gully(ServerLevel l,BlockPos b){
        for(int i=0;i<GULLY.length-1;i++)path(l,b,GULLY[i],GULLY[i+1]);for(var at:GULLY){set(l,b,at.getX()-2,at.getY(),at.getZ(),Blocks.FERN);set(l,b,at.getX()+2,at.getY(),at.getZ(),Blocks.FERN);}
        for(int x=-17;x<=-15;x++)for(int z=-120;z<=-118;z++)BuildBlocks.set(l,b.offset(x,4,z),Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP),F);
        box(l,b,-8,6,-124,-5,9,-120,Blocks.MOSSY_COBBLESTONE);box(l,b,-15,4,-130,-13,4,-129,Blocks.SPRUCE_LOG);
    }
    private static void compound(ServerLevel l,BlockPos b){
        box(l,b,-30,11,-214,27,11,-165,Blocks.GRAVEL);box(l,b,-30,12,-214,27,24,-166,Blocks.AIR);
        for(int x=-30;x<=27;x++)for(int y=12;y<=15;y++){fit(l,b,new BlockPos(x,y,-167),BorrascaFittingBlock.Kind.FENCE,Direction.SOUTH);fit(l,b,new BlockPos(x,y,-214),BorrascaFittingBlock.Kind.FENCE,Direction.SOUTH);}
        for(int z=-213;z<=-168;z++)for(int y=12;y<=15;y++)for(int x:new int[]{-30,27})fit(l,b,new BlockPos(x,y,z),BorrascaFittingBlock.Kind.FENCE,Direction.EAST);
        for(int x:new int[]{-2,2})box(l,b,x,12,-167,x,16,-167,Blocks.IRON_BLOCK);for(int x=-1;x<=1;x++)for(int y=12;y<=14;y++)fit(l,b,new BlockPos(x,y,-167),BorrascaFittingBlock.Kind.FENCE,Direction.SOUTH);fit(l,b,LOCK,BorrascaFittingBlock.Kind.LOCK,Direction.SOUTH);
        NovelRooms.room(l,b,-29,-14,-204,-169,12,5,Blocks.STONE_BRICKS,Blocks.POLISHED_ANDESITE);NovelRooms.door(l,b.offset(FACTORY_DOOR),Direction.SOUTH,Blocks.IRON_DOOR,false);NovelRooms.door(l,b.offset(-14,12,-179),Direction.EAST,Blocks.SPRUCE_DOOR,false);box(l,b,-11,12,-179,-11,13,-179,Blocks.AIR);
        for(int z=-175;z>=-201;z-=5)furniture(l,b,new BlockPos(-27,12,z),HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS);for(var at:new BlockPos[]{EVIDENCE,PACK})furniture(l,b,at.below(),HouseholdFurnitureBlock.Kind.WALNUT_DESK);fit(l,b,EVIDENCE,BorrascaFittingBlock.Kind.PAPER,Direction.SOUTH);fit(l,b,PACK,BorrascaFittingBlock.Kind.PACK,Direction.SOUTH);
        for(int z=-178;z>=-200;z--)fit(l,b,new BlockPos(0,12,z),BorrascaFittingBlock.Kind.BELT,Direction.NORTH);
        for(int y=13;y<=16;y++)for(int x=-2;x<=2;x++)for(int z=-191;z<=-187;z++)if(Math.abs(x)==2||z==-191||z==-187)fit(l,b,new BlockPos(x,y,z),BorrascaFittingBlock.Kind.POLISHED,Direction.NORTH);
        BuildBlocks.set(l,b.offset(MACHINE),BorrascaRegistry.MACHINE.get().defaultBlockState(),F);box(l,b,3,12,-184,3,16,-184,Blocks.IRON_BARS);box(l,b,1,16,-184,3,16,-184,Blocks.IRON_BARS);join(l,b,1,12,-184,3,16,-184);fit(l,b,LAMP,BorrascaFittingBlock.Kind.LAMP,Direction.SOUTH);
        BuildBlocks.set(l,b.offset(POWER),Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE,AttachFace.FLOOR).setValue(LeverBlock.POWERED,true),F);fit(l,b,POWER.below(),BorrascaFittingBlock.Kind.BREAKER,Direction.SOUTH);
        vehicle(l,b,new BlockPos(12,12,-179),true);vehicle(l,b,TRUCK,false);for(int z:new int[]{-175,-185,-201}){set(l,b,-22,16,z,Blocks.CHAIN);set(l,b,-22,15,z,Blocks.LANTERN);}set(l,b,5,14,-183,Blocks.REDSTONE_TORCH);
        for(int z=-180;z>=-201;z--)box(l,b,-11,12,z,-11,14,z,Blocks.IRON_BARS);join(l,b,-11,12,-201,-11,14,-180);
    }
    private static void vehicle(ServerLevel l,BlockPos b,BlockPos at,boolean county){
        for(int x=0;x<=2;x++)for(int z=0;z<=4;z++)fit(l,b,at.offset(x,0,z),county?BorrascaFittingBlock.Kind.COUNTY:BorrascaFittingBlock.Kind.TRUCK,Direction.NORTH);box(l,b,at.getX(),at.getY()+1,at.getZ()+1,at.getX()+2,at.getY()+1,at.getZ()+2,Blocks.BLACK_STAINED_GLASS);
        sign(l,b,at.offset(1,1,5),county?"COUNTY 17":"PRESCOTT","PROPERTY SERVICE",county?"SHERIFF'S OFFICE":"D-417","");
    }
    private static void serviceDescent(ServerLevel l,BlockPos b){
        box(l,b,-18,12,-207,-14,15,-206,Blocks.STONE_BRICKS);NovelRooms.door(l,b.offset(-17,12,-206),Direction.NORTH,Blocks.IRON_DOOR,false);box(l,b,-17,12,-207,-17,13,-207,Blocks.AIR);for(int i=0;i<RETURN_ROUTE.length-1;i++)path(l,b,RETURN_ROUTE[i],RETURN_ROUTE[i+1]);
        for(int x=-21;x<=-15;x++)for(int z=-210;z<=-208;z++)BuildBlocks.set(l,b.offset(x,13,z),Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP),F);BuildBlocks.set(l,b.offset(RELEASE),Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE,AttachFace.FLOOR),F);sign(l,b,RELEASE.above(2),"SERVICE EXIT","Mechanical release","Keep low","Return to trail");
        box(l,b,34,-5,-61,41,-1,-53,Blocks.STONE_BRICKS);box(l,b,35,-4,-60,40,-2,-54,Blocks.AIR);
    }
    private static void marker(ServerLevel l,BlockPos b,BlockPos at,BorrascaFittingBlock.Kind k){box(l,b,at.getX(),at.getY(),at.getZ()-1,at.getX(),at.getY()+1,at.getZ()-1,Blocks.SPRUCE_LOG);fit(l,b,at.above(),k,Direction.SOUTH);}
    private static void path(ServerLevel l,BlockPos b,BlockPos a,BlockPos c){int n=Math.max(Math.abs(a.getX()-c.getX()),Math.abs(a.getZ()-c.getZ()));int previous=a.getY();for(int i=0;i<=n;i++){double t=n==0?0:(double)i/n;int x=(int)Math.round(a.getX()+(c.getX()-a.getX())*t),y=(int)Math.round(a.getY()+(c.getY()-a.getY())*t),z=(int)Math.round(a.getZ()+(c.getZ()-a.getZ())*t);box(l,b,x-1,y-2,z,x+1,y-1,z,Blocks.STONE);box(l,b,x-1,y,z,x+1,y+2,z,Blocks.AIR);for(int dx=-1;dx<=1;dx++){fit(l,b,new BlockPos(x+dx,y-1,z),BorrascaFittingBlock.Kind.TRAIL,Direction.NORTH);if(y>previous){Direction facing=Math.abs(c.getZ()-a.getZ())>=Math.abs(c.getX()-a.getX())?(c.getZ()<a.getZ()?Direction.NORTH:Direction.SOUTH):(c.getX()<a.getX()?Direction.WEST:Direction.EAST);BuildBlocks.set(l,b.offset(x+dx,y-1,z),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,facing),F);}}previous=y;}}
    private static void tree(ServerLevel l,BlockPos b,int x,int z,int y,int h){box(l,b,x,y,z,x,y+h,z,Blocks.SPRUCE_LOG);for(int k=0;k<5;k++){int w=3-k/2;for(int dx=-w;dx<=w;dx++)for(int dz=-w;dz<=w;dz++)BuildBlocks.set(l,b.offset(x+dx,y+h-3+k,z+dz),Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);}}
    private static void join(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z){for(var rel:BlockPos.betweenClosed(x,y,z,X,Y,Z)){var at=b.offset(rel).immutable();BuildBlocks.after(l,()->{var s=l.getBlockState(at);if(!(s.getBlock() instanceof FenceBlock||s.getBlock() instanceof IronBarsBlock))return;for(var d:Direction.Plane.HORIZONTAL)s=s.updateShape(d,l.getBlockState(at.relative(d)),l,at,at.relative(d));l.setBlock(at,s,F);});}}
}
