package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/** Six authored places. Outdoor sites really have sky in the outside dimension. */
public final class NovelRooms {
    public static final BlockPos ARCHIVE_DOOR=new BlockPos(0,0,-19),ARCHIVE_DESK=new BlockPos(-5,0,-30),MAT=new BlockPos(0,0,-16),
        MAIL=new BlockPos(5,0,-8),ATTIC_DOOR=new BlockPos(-5,8,-20),ATTIC_DESK=new BlockPos(-9,8,-25),
        WELL=new BlockPos(0,0,-23),CARVING=new BlockPos(0,-10,-22),OLD_CARVING=new BlockPos(0,-12,-24),RIBBON=new BlockPos(2,0,-25),
        APOLOGY=new BlockPos(-4,0,-7),FIGURE=new BlockPos(0,0,-93),BUTTON=new BlockPos(5,1,-14),WARD_NOTE=new BlockPos(-4,0,-15),
        BED=new BlockPos(4,0,-10),PROJECTOR=new BlockPos(0,1,-6);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private NovelRooms(){}
    public static boolean outside(LabyrinthPlace p){return p==LabyrinthPlace.ZAMPANO_COURTYARD||p==LabyrinthPlace.BARN_WELL||p==LabyrinthPlace.PLAIN||LakeLandscape.isLake(p)||p==LabyrinthPlace.HOTEL_GROUNDS||LiteraryRooms.outside(p);}
    public static net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension(LabyrinthPlace p){return outside(p)?HouseDimensions.OUTSIDE:HouseDimensions.INTERIOR;}
    public static void build(MinecraftServer server,ServerLevel l,BlockPos b,LabyrinthPlace p){
        var r=p.room();box(l,b,r.minX(),r.minY(),r.minZ(),r.maxX(),r.maxY(),r.maxZ(),Blocks.AIR.defaultBlockState());
        switch(p){case ZAMPANO_COURTYARD->courtyard(l,b);case WHALE->whale(l,b);case BARN_WELL->well(l,b);case PLAIN->plain(l,b);case HOSPITAL->hospital(l,b);case KAREN_ROOM->karen(l,b);default->throw new IllegalArgumentException(p.id());}
        for(var spec:p.doors()){var at=b.offset(spec.rel());door(l,at,spec.facing(),Blocks.DARK_OAK_DOOR,false);BuildBlocks.set(l,at.below(),Blocks.SMOOTH_STONE.defaultBlockState(),F);}
        for(int x=-3;x<=3;x++)for(int z=1;z<=4;z++)BuildBlocks.set(l,b.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState(),F);
    }
    public static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState state){
        BuildBlocks.box(l,b.offset(x0,y0,z0),b.offset(x1,y1,z1),state,F);
    }
    private static void at(ServerLevel l,BlockPos b,int x,int y,int z,Block block){BuildBlocks.set(l,b.offset(x,y,z),block.defaultBlockState(),F);}
    public static void room(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,int y,int height,Block wall,Block floor){
        box(l,b,x0,y-1,z0,x1,y-1,z1,floor.defaultBlockState());box(l,b,x0,y+height,z0,x1,y+height,z1,Blocks.BIRCH_PLANKS.defaultBlockState());
        box(l,b,x0,y,z0,x0,y+height,z1,wall.defaultBlockState());box(l,b,x1,y,z0,x1,y+height,z1,wall.defaultBlockState());
        box(l,b,x0,y,z0,x1,y+height,z0,wall.defaultBlockState());box(l,b,x0,y,z1,x1,y+height,z1,wall.defaultBlockState());
    }
    private static void outdoor(ServerLevel l,BlockPos b,int width,int depth,Block ground){
        box(l,b,-width,-1,-depth,width,-1,0,ground.defaultBlockState());
        box(l,b,-width,0,-depth,-width,16,0,Blocks.BARRIER.defaultBlockState());box(l,b,width,0,-depth,width,16,0,Blocks.BARRIER.defaultBlockState());
        box(l,b,-width,0,-depth,width,16,-depth,Blocks.BARRIER.defaultBlockState());box(l,b,-width,0,0,width,12,0,Blocks.BARRIER.defaultBlockState());
        box(l,b,-1,0,0,1,2,0,Blocks.AIR.defaultBlockState());
    }
    private static void courtyard(ServerLevel l,BlockPos b){
        outdoor(l,b,15,41,Blocks.MOSSY_STONE_BRICKS);room(l,b,-12,12,-39,-19,0,7,NovelRegistry.PLASTER.get(),Blocks.DARK_OAK_PLANKS);
        for(int x=-11;x<=11;x++)for(int y=2;y<=4;y++)if(PlaytestSceneReview.paperPatch(x,y,-39))at(l,b,x,y,-39,NovelRegistry.PAPER.get());
        door(l,b.offset(ARCHIVE_DOOR),Direction.SOUTH,Blocks.IRON_DOOR,false);
        for(int x:new int[]{-12,12})for(int z:new int[]{-23,-29,-35})for(int y=1;y<=3;y++)at(l,b,x,y,z,NovelRegistry.SEALED_WINDOW.get());
        for(int z=-3;z>=-15;z-=3)for(int x:new int[]{-10,10}){at(l,b,x,0,z,Blocks.MOSS_BLOCK);at(l,b,x,1,z,Blocks.AZALEA);}
        at(l,b,0,0,-16,Blocks.BROWN_CARPET);at(l,b,5,0,-34,Blocks.CHEST);at(l,b,5,1,-33,NovelRegistry.GOUGES.get());
        lectern(l,b.offset(ARCHIVE_DESK),NovelTexts.archive());
        if(l.getBlockEntity(b.offset(5,0,-34)) instanceof ChestBlockEntity chest&&chest.isEmpty()){
            chest.setItem(0,HouseWriting.book("Readers' appointments","Zampano",HouseWriting.WritingStyle.ZAMPANO,List.of("Seven names. Seven visits.\n\nThe margins remember the voices more clearly than the survey remembers the walls.")));chest.setChanged();}
        for(int i=0;i<7;i++)sign(l,b.offset(10,2,-22-i*2),Direction.WEST,new String[]{"reader "+(i+1),new String[]{"Beatrice","Leonie","Pauline","Ruth","Anne","Esther","Helen"}[i],"", ""});
        at(l,b,-7,5,-25,Blocks.LANTERN);
    }
    private static void whale(ServerLevel l,BlockPos b){
        room(l,b,-13,13,-32,0,0,5,NovelRegistry.INSTITUTE.get(),Blocks.SMOOTH_STONE);
        door(l,b,Direction.SOUTH,Blocks.BIRCH_DOOR,true);bed(l,b.offset(6,0,-23),Blocks.WHITE_BED,Direction.NORTH);
        furniture(l,b.offset(-7,0,-25),HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);lectern(l,b.offset(-7,1,-25),NovelTexts.whaleOpening());
        BuildBlocks.set(l,b.offset(MAIL),prop(NovelPropBlock.Kind.MAIL_SLOT,Direction.WEST),F);
        MailPlaqueBlock.place(l,b);
        // The ladder joins three separate, real attics; only one opens to the knock.
        for(int y=0;y<=11;y++){at(l,b,0,y,-19,Blocks.SMOOTH_STONE);BuildBlocks.set(l,b.offset(0,y,-18),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),F);}
        for(int y:new int[]{5,8,11}){room(l,b,-12,-5,-29,-19,y,2,NovelRegistry.INSTITUTE.get(),Blocks.DARK_OAK_PLANKS);
            box(l,b,-4,y,-21,-1,y-1,-17,Blocks.SMOOTH_STONE.defaultBlockState());door(l,b.offset(-5,y,-20),Direction.EAST,Blocks.IRON_DOOR,false);}
        // Leave a supported landing beside each door, with two blocks of headroom.
        for(int y:new int[]{5,8,11})box(l,b,-4,y,-21,-1,y+2,-17,Blocks.AIR.defaultBlockState());
        // The landings stop beside the ladder; laid across it they would cut the climb.
        for(int y:new int[]{5,8,11})box(l,b,-4,y-1,-20,-1,y-1,-17,Blocks.SMOOTH_STONE.defaultBlockState());
        for(int y=0;y<=13;y++){at(l,b,0,y,-19,Blocks.SMOOTH_STONE);BuildBlocks.set(l,b.offset(0,y,-18),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),F);}
        lectern(l,b.offset(ATTIC_DESK),NovelTexts.whaleLast());
        for(int z:new int[]{-5,-15,-26})at(l,b,8,4,z,Blocks.LANTERN);
    }
    private static void well(ServerLevel l,BlockPos b){
        outdoor(l,b,17,39,Blocks.GRASS_BLOCK);room(l,b,5,15,-34,-17,0,7,Blocks.SPRUCE_PLANKS,Blocks.COARSE_DIRT);
        box(l,b,8,0,-17,10,3,-17,Blocks.AIR.defaultBlockState());
        for(int y=-12;y<=1;y++)for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
            if(dx==0&&dz==0)at(l,b,dx,y,-23+dz,Blocks.AIR);else at(l,b,dx,y,-23+dz,Blocks.MOSSY_COBBLESTONE);}
        for(int y=-12;y<=1;y++)BuildBlocks.set(l,b.offset(0,y,-23),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),F);
        at(l,b,0,-13,-23,Blocks.MOSSY_COBBLESTONE);BuildBlocks.set(l,b.offset(CARVING),NovelRegistry.CARVINGS.get().defaultBlockState(),F);
        cover(l,b,false);lectern(l,b.offset(-4,0,-17),NovelTexts.well());
        at(l,b,2,0,-25,Blocks.BARREL);
        for(int z=-4;z>=-33;z-=6)for(int x:new int[]{-13,16}){box(l,b,x,0,z,x,5,z,Blocks.SPRUCE_LOG.defaultBlockState());box(l,b,x-1,4,z-1,x+1,7,z+1,Blocks.SPRUCE_LEAVES.defaultBlockState());}
        BarnFarm.dress(l,b);
        Farmstead.layout(l,b);
        Farmstead.built(l,b);
    }
    /** A copied return vestibule must never open onto unsupported outside air. */
    public static void safeApproach(ServerLevel l,BlockPos b){
        for(int x=-7;x<=7;x++)for(int z=1;z<=9;z++){
            if(BuildBlocks.state(l,b.offset(x,-1,z)).isAir())at(l,b,x,-1,z,Blocks.SMOOTH_STONE);
            if(Math.abs(x)==7||z==9)for(int y=0;y<=5;y++)if(BuildBlocks.state(l,b.offset(x,y,z)).isAir())at(l,b,x,y,z,Blocks.DARK_OAK_PLANKS);
            if(BuildBlocks.state(l,b.offset(x,5,z)).isAir())at(l,b,x,5,z,Blocks.DARK_OAK_PLANKS);
        }
    }
    public static void cover(ServerLevel l,BlockPos b,boolean closed){
        var at=b.offset(WELL);var state=BuildBlocks.state(l,at);
        if(state.is(NovelRegistry.WELL_COVER.get()))return; // the occupied controller owns an installed lid
        if(state.is(Blocks.SPRUCE_TRAPDOOR)||state.is(Blocks.LADDER)||state.isAir())
            BuildBlocks.set(l,at,NovelRegistry.WELL_COVER.get().defaultBlockState().setValue(WellCoverBlock.OPEN,!closed),F);
    }
    private static void plain(ServerLevel l,BlockPos b){
        outdoor(l,b,29,65,Blocks.SANDSTONE);
        // A one-block sand sheet over outside air cascades into thousands of falling entities.
        repairPlainGround(l,b);box(l,b,-28,-1,-64,28,-1,-1,Blocks.SAND.defaultBlockState());
        for(int x:new int[]{-28,28})for(int z=-2;z>=-64;z--)box(l,b,x-1,0,z,x+1,1+Math.floorMod(z,3),z,Blocks.SANDSTONE.defaultBlockState());
        // The shape remains beyond the traversable dunes: it cannot be approached.
        box(l,b,-10,-1,-104,10,-1,-78,Blocks.SANDSTONE.defaultBlockState());
        // The distant boy is a reader-owned native silhouette, restored by the scene tick.
        lectern(l,b.offset(APOLOGY),NovelTexts.apology());at(l,b,3,0,-7,Blocks.BARREL);
    }
    /** Back the authored walkable core first, then restore only its missing floor tiles. */
    public static void repairPlainGround(ServerLevel l,BlockPos b){
        for(int x=-28;x<=28;x++)for(int z=-64;z<=0;z++)for(int y=-3;y<=-2;y++){
            var at=b.offset(x,y,z);var s=BuildBlocks.state(l,at);
            if(s.isAir()||s.is(Blocks.SAND))BuildBlocks.set(l,at,Blocks.SANDSTONE.defaultBlockState(),F);
        }
        for(int x=-28;x<=28;x++)for(int z=-64;z<=0;z++){
            var at=b.offset(x,-1,z);if(BuildBlocks.state(l,at).isAir())BuildBlocks.set(l,at,Blocks.SAND.defaultBlockState(),F);
        }
        var volume=new net.minecraft.world.phys.AABB(b.getX()-28,l.getMinBuildHeight(),b.getZ()-64,b.getX()+29,b.getY()+1,b.getZ()+1);
        for(var debris:l.getEntitiesOfClass(net.minecraft.world.entity.item.FallingBlockEntity.class,volume,e->e.getBlockState().is(Blocks.SAND)))debris.discard();
    }
    private static void hospital(ServerLevel l,BlockPos b){
        room(l,b,-9,9,-23,0,0,5,NovelRegistry.INSTITUTE.get(),Blocks.WHITE_CONCRETE);door(l,b,Direction.SOUTH,Blocks.BIRCH_DOOR,true);
        BuildBlocks.set(l,b.offset(0,0,-14),prop(NovelPropBlock.Kind.INCUBATOR,Direction.SOUTH),F);
        furniture(l,b.offset(3,0,-12),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        BuildBlocks.set(l,b.offset(BUTTON),Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING,Direction.WEST).setValue(ButtonBlock.FACE,AttachFace.WALL),F);
        at(l,b,6,1,-14,NovelRegistry.INSTITUTE.get());lectern(l,b.offset(WARD_NOTE),NovelTexts.hospitalOpening());
        at(l,b,0,4,-10,Blocks.SEA_LANTERN);at(l,b,0,4,-19,Blocks.SEA_LANTERN);
    }
    private static void karen(ServerLevel l,BlockPos b){
        room(l,b,-9,9,-18,0,0,5,Blocks.BIRCH_PLANKS,Blocks.OAK_PLANKS);door(l,b,Direction.SOUTH,Blocks.BIRCH_DOOR,true);
        bed(l,b.offset(BED),Blocks.RED_BED,Direction.NORTH);furniture(l,b.offset(6,0,-11),HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH);
        furniture(l,b.offset(-4,0,-8),HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.NORTH);
        at(l,b,0,0,-6,Blocks.OAK_PLANKS);BuildBlocks.set(l,b.offset(PROJECTOR),prop(NovelPropBlock.Kind.PROJECTOR,Direction.NORTH),F);
        box(l,b,-3,1,-15,3,3,-15,Blocks.WHITE_CONCRETE.defaultBlockState());lectern(l,b.offset(6,1,-11),NovelTexts.karen());
        for(int x:new int[]{-6,6})at(l,b,x,4,-8,Blocks.LANTERN);
    }
    public static void bed(ServerLevel l,BlockPos pos,Block block,Direction facing){BuildBlocks.set(l,pos,block.defaultBlockState().setValue(BedBlock.FACING,facing).setValue(BedBlock.PART,BedPart.FOOT),F);BuildBlocks.set(l,pos.relative(facing),block.defaultBlockState().setValue(BedBlock.FACING,facing).setValue(BedBlock.PART,BedPart.HEAD),F);}
    public static void door(ServerLevel l,BlockPos at,Direction facing,Block block,boolean open){
        BuildBlocks.set(l,at,block.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.OPEN,open),F);
        BuildBlocks.set(l,at.above(),block.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.OPEN,open).setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),F);
    }
    public static BlockState prop(NovelPropBlock.Kind kind,Direction facing){return NovelRegistry.PROP.get().defaultBlockState().setValue(NovelPropBlock.KIND,kind).setValue(NovelPropBlock.FACING,facing);}
    public static void furniture(ServerLevel l,BlockPos at,HouseholdFurnitureBlock.Kind kind,Direction facing){BuildBlocks.set(l,at,HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,kind).setValue(HouseholdFurnitureBlock.FACING,facing),F);}
    private static void lectern(ServerLevel l,BlockPos at,ItemStack book){BuildBlocks.set(l,at,Blocks.LECTERN.defaultBlockState(),F);BuildBlocks.after(l,()->{if(l.getBlockEntity(at) instanceof LecternBlockEntity d){d.setBook(book);d.setChanged();}});}
    public static void sign(ServerLevel l,BlockPos at,Direction facing,String[] lines){BuildBlocks.set(l,at,Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING,facing),F);BuildBlocks.after(l,()->{if(l.getBlockEntity(at) instanceof SignBlockEntity s){var text=s.getFrontText();for(int i=0;i<4;i++)text=text.setMessage(i,net.minecraft.network.chat.Component.literal(lines[i]));s.setText(text,true);}});}
}
