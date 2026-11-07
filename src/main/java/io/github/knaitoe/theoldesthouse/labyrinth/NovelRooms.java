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
    public static final BlockPos ARCHIVE_DOOR=new BlockPos(0,0,-19),ARCHIVE_DESK=new BlockPos(-6,0,-35),MAT=new BlockPos(0,0,-16),
        WELL=new BlockPos(0,0,-23),CARVING=new BlockPos(0,-10,-22),OLD_CARVING=new BlockPos(0,-12,-24),RIBBON=new BlockPos(2,0,-25),
        APOLOGY=new BlockPos(-4,0,-7),FIGURE=new BlockPos(0,0,-93),BUTTON=new BlockPos(5,1,-14),WARD_NOTE=new BlockPos(-4,0,-15),
        BED=new BlockPos(7,0,-16),PROJECTOR=new BlockPos(0,1,-6),KAREN_BOOK=new BlockPos(6,1,-17);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private NovelRooms(){}
    public static boolean outside(LabyrinthPlace p){return TruantYard.is(p)||YardStock.is(p)||YardWind.is(p)||YardStorm.is(p)||YardConnectors.any(p)||YardCrossings.is(p)||p==LabyrinthPlace.YARD_REUNION||p==LabyrinthPlace.YARD_SURVEY||p==LabyrinthPlace.YARD_FLOOD||p==LabyrinthPlace.WAY_OUT||p==LabyrinthPlace.SILO||p==LabyrinthPlace.BACKSEAT||p==LabyrinthPlace.LAKE_HOUSE_EMPTY||p==LabyrinthPlace.ZAMPANO_COURTYARD||p==LabyrinthPlace.BARN_WELL||p==LabyrinthPlace.PLAIN||LakeLandscape.isLake(p)||p==LabyrinthPlace.HOTEL_GROUNDS||p==LabyrinthPlace.SPHINX_FIELD||LiteraryRooms.outside(p);}
    public static net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension(LabyrinthPlace p){return outside(p)?HouseDimensions.OUTSIDE:HouseDimensions.INTERIOR;}
    public static void build(MinecraftServer server,ServerLevel l,BlockPos b,LabyrinthPlace p){
        var r=p.room();box(l,b,r.minX(),r.minY(),r.minZ(),r.maxX(),r.maxY(),r.maxZ(),Blocks.AIR.defaultBlockState());
        switch(p){case ZAMPANO_COURTYARD->courtyard(l,b);case WHALE->WhaleInstitute.build(l,b);case BARN_WELL->well(l,b);case PLAIN->plain(l,b);case HOSPITAL->hospital(l,b);case KAREN_ROOM->karen(l,b);default->throw new IllegalArgumentException(p.id());}
        for(var spec:p.doors()){if(OnwardDoors.isOnward(spec))continue;var at=b.offset(spec.rel());door(l,at,spec.facing(),Blocks.DARK_OAK_DOOR,false);BuildBlocks.set(l,at.below(),Blocks.SMOOTH_STONE.defaultBlockState(),F);}
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
    /** 0.4.201: the apartment court is built whole by ZampanoApartment, and none of the later scene passes dress it. */
    private static void courtyard(ServerLevel l,BlockPos b){ZampanoApartment.build(l,b);}
    /** 0.4.164: the farm is built whole by BarnWell, and none of the later scene passes dress it. */
    private static void well(ServerLevel l,BlockPos b){BarnWell.build(l,b);Farmstead.composed(l,b);}
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
        PlaytestScenes0488.fresh(l,b,LabyrinthPlace.PLAIN);
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
    /** 0.4.199: the ward is built whole by NightWard, as a clinical ward with curtained bays. */
    private static void hospital(ServerLevel l,BlockPos b){NightWard.build(l,b);}
    /** 0.4.201: Karen's room is built whole by KarenRoom, and none of the later scene passes dress it. */
    private static void karen(ServerLevel l,BlockPos b){KarenRoom.build(l,b);}
    public static void bed(ServerLevel l,BlockPos pos,Block block,Direction facing){BuildBlocks.set(l,pos,block.defaultBlockState().setValue(BedBlock.FACING,facing).setValue(BedBlock.PART,BedPart.FOOT),F);BuildBlocks.set(l,pos.relative(facing),block.defaultBlockState().setValue(BedBlock.FACING,facing).setValue(BedBlock.PART,BedPart.HEAD),F);}
    public static void door(ServerLevel l,BlockPos at,Direction facing,Block block,boolean open){
        BuildBlocks.set(l,at,block.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.OPEN,open),F);
        BuildBlocks.set(l,at.above(),block.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.OPEN,open).setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),F);
    }
    public static BlockState prop(NovelPropBlock.Kind kind,Direction facing){return NovelRegistry.PROP.get().defaultBlockState().setValue(NovelPropBlock.KIND,kind).setValue(NovelPropBlock.FACING,facing);}
    public static void furniture(ServerLevel l,BlockPos at,HouseholdFurnitureBlock.Kind kind,Direction facing){BuildBlocks.set(l,at,HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,kind).setValue(HouseholdFurnitureBlock.FACING,facing),F);}
    static void lectern(ServerLevel l,BlockPos at,ItemStack book){BuildBlocks.set(l,at,Blocks.LECTERN.defaultBlockState(),F);BuildBlocks.after(l,()->{if(l.getBlockEntity(at) instanceof LecternBlockEntity d){d.setBook(book);d.setChanged();}});}
    public static void sign(ServerLevel l,BlockPos at,Direction facing,String[] lines){BuildBlocks.set(l,at,Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING,facing),F);BuildBlocks.after(l,()->{if(l.getBlockEntity(at) instanceof SignBlockEntity s){var text=s.getFrontText();for(int i=0;i<4;i++)text=text.setMessage(i,net.minecraft.network.chat.Component.literal(lines[i]));s.setText(text,true);}});}
}
