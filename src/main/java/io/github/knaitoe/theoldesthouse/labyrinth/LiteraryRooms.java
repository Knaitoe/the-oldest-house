package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.properties.*;
/** Twenty-four append-only structures. Furnishings and originals are constructed once. */
public final class LiteraryRooms {
    public static final BlockPos SOURCE=new BlockPos(3,0,-4),HILL_KNOCK=new BlockPos(-10,-4,-36),HILL_WALL=new BlockPos(-14,2,-17),
        TABLE=new BlockPos(0,0,-18),CLOCK=new BlockPos(0,0,-75),COFFIN=new BlockPos(0,-3,-29),FAN=new BlockPos(0,7,-17),FAN_REACH=new BlockPos(0,4,-17),
        PILE=new BlockPos(0,-5,-71),JOURNAL=new BlockPos(-8,1,-25),CAMERA=new BlockPos(0,0,-28),BOOTH=new BlockPos(15,0,-37),TV=new BlockPos(7,1,-24);
    /** The cabin (0.4.52): its front door, the two beds a reader wakes in (heads), and the shed door that is the only way out. */
    public static final BlockPos CABIN_DOOR=new BlockPos(0,0,-13),SHED_DOOR=new BlockPos(0,0,-4);
    public static final List<BlockPos> CABIN_BEDS=List.of(new BlockPos(-10,0,-34),new BlockPos(-6,0,-34));
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private LiteraryRooms(){}
    public static boolean isLiterary(LabyrinthPlace p){return p!=null&&p.slot()>=45&&p.slot()<=68;}
    public static boolean outside(LabyrinthPlace p){return switch(p){case ELK_LOT,MAPPING_INTERIOR,HOLY_RABBIT,ELK_CARCASSES,COSTUME_NIGHT,MOVIE_NIGHT,WINTER_LAKE,CAMP_BLOOD,END_WORLD_CABIN,FAMILY_COPY,OLD_CABIN->true;default->false;};}
    public static BlockPos source(LabyrinthPlace p){return switch(p){
        case HILL_NURSERY->new BlockPos(11,1,-21);case MINIATURES->new BlockPos(-12,1,-25);case MASQUE->new BlockPos(7,1,-4);case USHER->new BlockPos(-12,1,-34);case WINCHESTER->new BlockPos(-26,1,-9);case CHILD_ROOM->new BlockPos(6,1,-20);case CRIMSON_HALL->new BlockPos(10,1,-12);case BLY_ROUTE->new BlockPos(8,1,-36);case ELK_LOT->new BlockPos(-18,1,-12);case ELK_FAN->new BlockPos(-7,1,-13);case MAPPING_INTERIOR->new BlockPos(-9,1,-29);case HOLY_RABBIT->new BlockPos(2,0,-22);case CONFESSION->JOURNAL;case ELK_CARCASSES->ElkCarcassMap.SOURCE;case COSTUME_NIGHT,MOVIE_NIGHT,WINTER_LAKE->new BlockPos(4,1,-6);case CAMP_BLOOD->new BlockPos(-20,1,-23);case DEVILS_ROCK->new BlockPos(-8,1,-21);case WHEEL->new BlockPos(-14,1,-43);case GHOSTS_SET->new BlockPos(17,1,-9);case END_WORLD_CABIN->new BlockPos(-8,1,-31);default->SOURCE;};}
    public static BlockPos ending(LabyrinthPlace p){return switch(p){case HILL_NURSERY->new BlockPos(8,-3,-36);case MINIATURES->new BlockPos(0,1,-25);case MASQUE->new BlockPos(5,0,-73);case USHER->new BlockPos(12,1,-34);case WINCHESTER->new BlockPos(26,9,-70);case CHILD_ROOM->new BlockPos(0,-2,-23);case CRIMSON_HALL->new BlockPos(0,1,-39);case BLY_ROUTE->new BlockPos(8,1,-38);case ELK_LOT->new BlockPos(0,0,-5);case ELK_FAN->new BlockPos(0,0,-17);case MAPPING_INTERIOR->new BlockPos(6,0,-20);case HOLY_RABBIT->new BlockPos(0,0,-120);case CONFESSION->new BlockPos(10,0,-28);case ELK_CARCASSES->ElkCarcassMap.ENDING;case COSTUME_NIGHT->new BlockPos(0,0,-67);case MOVIE_NIGHT->new BlockPos(0,0,-85);case WINTER_LAKE->new BlockPos(0,0,-78);case CAMP_BLOOD->new BlockPos(0,0,-97);case DEVILS_ROCK->new BlockPos(9,0,-36);case WHEEL->new BlockPos(0,0,-4);case GHOSTS_SET->new BlockPos(15,0,-39);case END_WORLD_CABIN->new BlockPos(2,1,-30);case FAMILY_COPY->new BlockPos(0,0,-5);case OLD_CABIN->new BlockPos(0,0,-5);default->throw new IllegalArgumentException(p.id());};}
    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace p){
        var r=p.room();box(l,b,r.minX(),r.minY(),r.minZ(),r.maxX(),r.maxY(),r.maxZ(),Blocks.AIR);
        switch(p){case HILL_NURSERY->hill(l,b);case MINIATURES->miniatures(l,b);case MASQUE->masque(l,b);case USHER->usher(l,b);case WINCHESTER->winchester(l,b);case CHILD_ROOM->child(l,b);case CRIMSON_HALL->crimson(l,b);case BLY_ROUTE->bly(l,b);case ELK_LOT->lot(l,b);case ELK_FAN->fan(l,b);case MAPPING_INTERIOR->mapping(l,b);case HOLY_RABBIT->rabbit(l,b);case CONFESSION->confession(l,b);case ELK_CARCASSES->carcasses(l,b);case COSTUME_NIGHT,MOVIE_NIGHT,WINTER_LAKE->lake(l,b,p);case CAMP_BLOOD->camp(l,b);case DEVILS_ROCK->diary(l,b);case WHEEL->wheel(l,b);case GHOSTS_SET->set(l,b);case END_WORLD_CABIN->endCabin(l,b);case FAMILY_COPY,OLD_CABIN->copyLanding(l,b,p);default->throw new IllegalArgumentException(p.id());}
        // Papers are original scene-specific texts; reading surfaces generate immutable reader originals.
        var note=source(p);if(note.getY()>0&&BuildBlocks.state(l,b.offset(note.below())).isAir())furniture(l,b,note.getX(),note.getY()-1,note.getZ(),HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH);paper(l,b,note,LiteraryTexts.source(p));
        if(p!=LabyrinthPlace.OLD_CABIN&&p!=LabyrinthPlace.FAMILY_COPY){var end=ending(p);if(end.getY()>0&&BuildBlocks.state(l,b.offset(end.below())).isAir())furniture(l,b,end.getX(),end.getY()-1,end.getZ(),HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH);prop(l,b,end,LiteraryPropBlock.Kind.LEDGER,Direction.SOUTH);}
        NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,wall(p).defaultBlockState(),floor(p).defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState());LabyrinthBuilder.doors(l,b,p);
        // The elk arrival is aboard a yacht: its hull is the arrival's support, never earth heaped in the lake.
        if(outside(p)&&p!=LabyrinthPlace.FAMILY_COPY&&p!=LabyrinthPlace.OLD_CABIN&&p!=LabyrinthPlace.ELK_CARCASSES)ScenePlaytestRepairs.arrivalGround(l,b);
        StaceyCover.fresh(l,b,p);
    }
    public static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,Block block){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,block instanceof LeavesBlock?block.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true):block.defaultBlockState());}
    public static void at(ServerLevel l,BlockPos b,int x,int y,int z,Block block){BuildBlocks.set(l,b.offset(x,y,z),block.defaultBlockState(),F);}
    public static void prop(ServerLevel l,BlockPos b,BlockPos rel,LiteraryPropBlock.Kind kind,Direction facing){BuildBlocks.set(l,b.offset(rel),LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND,kind).setValue(LiteraryPropBlock.FACING,facing),F);}
    public static void paper(ServerLevel l,BlockPos b,BlockPos rel,ItemStack book){BuildBlocks.set(l,b.offset(rel),Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.HAS_BOOK,true),F);BuildBlocks.after(l,()->{if(l.getBlockEntity(b.offset(rel)) instanceof LecternBlockEntity d){d.setBook(book);d.setChanged();}});}
    private static Block wall(LabyrinthPlace p){return switch(p){case USHER->Blocks.DEEPSLATE_BRICKS;case MAPPING_INTERIOR->LiteraryRegistry.SIDING.get();case CRIMSON_HALL->Blocks.POLISHED_BLACKSTONE_BRICKS;case MINIATURES,DEVILS_ROCK,GHOSTS_SET,WHEEL->Blocks.CALCITE;default->LiteraryRegistry.DARK_PANEL.get();};}
    private static Block floor(LabyrinthPlace p){return switch(p){case USHER->Blocks.POLISHED_DEEPSLATE;case CRIMSON_HALL->LiteraryRegistry.RED_FLOOR.get();case MAPPING_INTERIOR->Blocks.BIRCH_PLANKS;default->Blocks.DARK_OAK_PLANKS;};}
    private static void room(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,int y,int h,Block walls,Block flooring){
        NovelRooms.room(l,b,x0,x1,z0,z1,y,h,walls,flooring);
        // Timber bays, sills and a recessed coffer construction rather than featureless boxes.
        for(int z=z0+2;z<z1;z+=5)for(int x:new int[]{x0,x1}){box(l,b,x,y,z,x,y+h,z,Blocks.STRIPPED_DARK_OAK_LOG);at(l,b,x,y+h-1,z,Blocks.DARK_OAK_SLAB);}
        for(int x=x0+2;x<x1;x+=5)for(int z:new int[]{z0,z1})box(l,b,x,y,z,x,y+h,z,Blocks.STRIPPED_DARK_OAK_LOG);
        for(int x=x0+1;x<x1;x++)for(int z=z0+1;z<z1;z++)if((x-x0)%5==0||(z-z0)%5==0)at(l,b,x,y+h,z,Blocks.DARK_OAK_PLANKS);
        for(int z=z0+1;z<z1;z++)for(int x:new int[]{x0,x1})at(l,b,x,y,z,Blocks.DARK_OAK_PLANKS);
    }
    private static void door(ServerLevel l,BlockPos b,int x,int y,int z,Direction f,boolean open){box(l,b,x,y,z,x,y+1,z,Blocks.AIR);at(l,b,x,y+2,z,Blocks.STRIPPED_DARK_OAK_LOG);NovelRooms.door(l,b.offset(x,y,z),f,Blocks.DARK_OAK_DOOR,open);var across=f.getAxis()==Direction.Axis.X?Direction.NORTH:Direction.EAST;for(int a:new int[]{-1,1})for(int dy=0;dy<3;dy++)BuildBlocks.set(l,b.offset(x,y+dy,z).relative(across,a),Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),F);}
    private static void furniture(ServerLevel l,BlockPos b,int x,int y,int z,HouseholdFurnitureBlock.Kind k,Direction f){NovelRooms.furniture(l,b.offset(x,y,z),k,f);}
    private static void desk(ServerLevel l,BlockPos b,int x,int y,int z){furniture(l,b,x,y,z,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);furniture(l,b,x,y,z+2,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);}
    private static void light(ServerLevel l,BlockPos b,int x,int y,int z){at(l,b,x,y+1,z,Blocks.CHAIN);BuildBlocks.set(l,b.offset(x,y,z),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);}
    private static void steps(ServerLevel l,BlockPos b,int x,int z,int y,int count,boolean down){for(int i=0;i<count;i++){int yy=y+(down?-i:i);box(l,b,x-1,yy-2,z-i,x+1,yy-1,z-i,Blocks.STONE_BRICKS);box(l,b,x-1,yy,z-i,x+1,yy+2,z-i,Blocks.AIR);for(int xx=x-1;xx<=x+1;xx++)BuildBlocks.set(l,b.offset(xx,yy-1,z-i),Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,down?Direction.SOUTH:Direction.NORTH),F);}}
    private static void hill(ServerLevel l,BlockPos b){
        room(l,b,-3,3,-43,0,0,5,Blocks.CALCITE,Blocks.DARK_OAK_PLANKS);
        for(int side:new int[]{-1,1}){room(l,b,side<0?-14:4,side<0?-4:14,-24,-10,0,6,Blocks.CALCITE,Blocks.DARK_OAK_PLANKS);door(l,b,side*4,0,-16,side<0?Direction.EAST:Direction.WEST,true);NovelRooms.bed(l,b.offset(side*10,0,-22),Blocks.WHITE_BED,Direction.NORTH);furniture(l,b,side*11,0,-12,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.SOUTH);box(l,b,side*14,2,-22,side*14,3,-19,Blocks.BLACK_STAINED_GLASS);}
        box(l,b,-4,-1,-16,-4,2,-16,Blocks.POWDER_SNOW);door(l,b,-4,0,-16,Direction.EAST,true);
        // The corridor's own wall stands behind each nursery door: open it, or the nurseries are sealed.
        for(int x:new int[]{-3,3})box(l,b,x,0,-16,x,1,-16,Blocks.AIR);
        room(l,b,-13,13,-42,-30,-4,3,Blocks.MOSSY_STONE_BRICKS,Blocks.STONE_BRICKS);steps(l,b,0,-25,0,5,true);box(l,b,-1,-4,-33,1,-2,-30,Blocks.AIR);box(l,b,-1,-5,-33,1,-5,-30,Blocks.STONE_BRICKS);
        prop(l,b,HILL_WALL,LiteraryPropBlock.Kind.PHOTO,Direction.EAST);BuildBlocks.set(l,b.offset(HILL_WALL),BuildBlocks.state(l,b.offset(HILL_WALL)).setValue(LiteraryPropBlock.STAGE,3),F);at(l,b,-10,-4,-36,Blocks.CHISELED_STONE_BRICKS);desk(l,b,8,-4,-36);light(l,b,0,3,-8);light(l,b,0,3,-36);
    }
    private static void miniatures(ServerLevel l,BlockPos b){
        room(l,b,-14,14,-30,0,0,7,Blocks.CALCITE,Blocks.BIRCH_PLANKS);
        for(int x:new int[]{-8,0,8}){furniture(l,b,x,0,-18,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);prop(l,b,new BlockPos(x,1,-18),LiteraryPropBlock.Kind.MINIATURE,Direction.SOUTH);BuildBlocks.set(l,b.offset(x,1,-18),BuildBlocks.state(l,b.offset(x,1,-18)).setValue(LiteraryPropBlock.STAGE,x<0?0:x==0?1:2),F);furniture(l,b,x,0,-15,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.NORTH);light(l,b,x,4,-18);}
        for(int z=-26;z<=-7;z+=4){at(l,b,13,0,z,Blocks.BARREL);at(l,b,13,1,z,Blocks.BOOKSHELF);furniture(l,b,-12,0,z,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.EAST);}
        desk(l,b,0,0,-25);box(l,b,-8,2,-30,8,4,-30,Blocks.GLASS);box(l,b,-8,2,-31,8,4,-31,Blocks.LIGHT_GRAY_CONCRETE);
    }
    private static void masque(ServerLevel l,BlockPos b){
        Block[] colors={Blocks.BLUE_STAINED_GLASS,Blocks.PURPLE_STAINED_GLASS,Blocks.GREEN_STAINED_GLASS,Blocks.ORANGE_STAINED_GLASS,Blocks.WHITE_STAINED_GLASS,Blocks.PURPLE_STAINED_GLASS,Blocks.BLACK_STAINED_GLASS};
        for(int i=0;i<7;i++){int z=-11*(i+1);room(l,b,-10,10,z,z+10,0,8,colors[i],i==6?Blocks.BLACK_CONCRETE:Blocks.DARK_OAK_PLANKS);
            box(l,b,-1,0,z,1,3,z,Blocks.AIR);box(l,b,-1,0,z+10,1,3,z+10,Blocks.AIR);
            for(int zz=z+2;zz<z+9;zz+=3)for(int side:new int[]{-1,1}){at(l,b,side*11,2,zz,Blocks.SEA_LANTERN);box(l,b,side*12,0,zz-1,side*12,6,zz+1,Blocks.BLACK_CONCRETE);}
            furniture(l,b,-7,0,z+3,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);furniture(l,b,7,0,z+7,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.WEST);
        }
        prop(l,b,CLOCK,LiteraryPropBlock.Kind.CLOCK,Direction.SOUTH);at(l,b,0,1,-77,Blocks.RED_STAINED_GLASS);light(l,b,0,5,-6);
    }
    private static void usher(ServerLevel l,BlockPos b){
        room(l,b,-17,17,-41,0,0,10,Blocks.DEEPSLATE_BRICKS,Blocks.POLISHED_DEEPSLATE);room(l,b,-8,8,-35,-20,-3,3,Blocks.DEEPSLATE_BRICKS,Blocks.POLISHED_DEEPSLATE);
        steps(l,b,0,-14,0,4,true);box(l,b,-1,-3,-23,1,-1,-18,Blocks.AIR);box(l,b,-1,-4,-23,1,-4,-18,Blocks.POLISHED_DEEPSLATE);
        for(int z:new int[]{-8,-20,-36})for(int x:new int[]{-14,14})box(l,b,x,0,z,x,8,z,Blocks.POLISHED_BASALT);
        prop(l,b,COFFIN,LiteraryPropBlock.Kind.COFFIN,Direction.NORTH);prop(l,b,COFFIN.north(),LiteraryPropBlock.Kind.COFFIN,Direction.NORTH);light(l,b,0,5,-8);light(l,b,0,2,-25);desk(l,b,12,0,-34);
        box(l,b,11,2,-41,14,5,-41,Blocks.BLACK_STAINED_GLASS);at(l,b,-13,0,-33,Blocks.CANDLE);
    }
    private static void winchester(ServerLevel l,BlockPos b){
        room(l,b,-30,30,-77,0,0,15,Blocks.RED_TERRACOTTA,Blocks.DARK_OAK_PLANKS);
        for(int z:new int[]{-16,-32,-48,-64}){box(l,b,-29,0,z,29,7,z,Blocks.OAK_PLANKS);for(int x:new int[]{-25,0,25})door(l,b,x,0,z,Direction.SOUTH,x==0);}
        // Long opposing flights gain half a block at each turn, not a full floor per flight.
        for(int flight=0;flight<16;flight++){int y=flight/2,z=-8-flight*4;var type=flight%2==0?SlabType.BOTTOM:SlabType.TOP;int turn=flight%2==0?23:-23;
            box(l,b,-24,y-1,z,24,y-1,z,Blocks.OAK_PLANKS);box(l,b,-24,y,z-1,24,y+2,z+1,Blocks.AIR);
            for(int x=-23;x<=23;x++)BuildBlocks.set(l,b.offset(x,y,z),Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,type),F);
            if(flight<15){for(int zz=z-1;zz>=z-3;zz--)for(int x=turn-1;x<=turn+1;x++){box(l,b,x,y,zz,x,y+3,zz,Blocks.AIR);BuildBlocks.set(l,b.offset(x,y,zz),Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,type),F);}}
            for(int x=-21;x<=21;x++)at(l,b,x,y+1,z-1,Blocks.DARK_OAK_FENCE);
        }
        // The following flight's clearance cuts reach the preceding turn: lay every turn last.
        for(int flight=0;flight<15;flight++){int y=flight/2,z=-8-flight*4,turn=flight%2==0?23:-23;var type=flight%2==0?SlabType.BOTTOM:SlabType.TOP;for(int zz=z-1;zz>=z-3;zz--)for(int x=turn-1;x<=turn+1;x++){box(l,b,x,y,zz,x,y+3,zz,Blocks.AIR);BuildBlocks.set(l,b.offset(x,y,zz),Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,type),F);}}
        box(l,b,-25,-1,-8,-22,-1,-3,Blocks.OAK_PLANKS);box(l,b,-25,0,-7,-22,3,-3,Blocks.AIR);
        box(l,b,-24,7,-73,28,7,-70,Blocks.OAK_PLANKS);box(l,b,-24,8,-73,28,10,-70,Blocks.AIR);
        box(l,b,-24,7,-70,-22,10,-69,Blocks.AIR);box(l,b,-24,7,-70,-22,7,-69,Blocks.OAK_PLANKS);
        steps(l,b,-28,-5,0,6,false);box(l,b,-29,5,-10,-27,7,-10,Blocks.OAK_PLANKS);
        box(l,b,24,7,-74,28,7,-63,Blocks.DARK_OAK_PLANKS);door(l,b,25,8,-63,Direction.SOUTH,true);box(l,b,24,0,-62,28,8,-59,Blocks.AIR);box(l,b,24,-1,-62,28,-1,-59,Blocks.HAY_BLOCK);
        desk(l,b,26,8,-70);for(int z:new int[]{-8,-26,-44,-60})light(l,b,0,11,z);NovelRooms.sign(l,b.offset(-28,5,-9),Direction.SOUTH,new String[]{"ATTIC","Continue upward","",""});
    }
    private static void child(ServerLevel l,BlockPos b){
        room(l,b,-11,11,-24,0,0,6,Blocks.CALCITE,Blocks.BIRCH_PLANKS);door(l,b,-11,0,-11,Direction.EAST,false);door(l,b,11,0,-17,Direction.WEST,false);box(l,b,-4,2,-24,4,3,-24,Blocks.GLASS);
        NovelRooms.bed(l,b.offset(0,0,-16),Blocks.WHITE_BED,Direction.NORTH);furniture(l,b,6,0,-20,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST);
        for(int x:new int[]{-6,5})for(int z:new int[]{-8,-20}){at(l,b,x,5,z,Blocks.WHITE_WOOL);at(l,b,x,4,z,Blocks.CHAIN);}
        // The only surviving route is a genuine one-block crawl below the bed.
        box(l,b,-1,-3,-23,1,-3,-15,Blocks.BIRCH_PLANKS);box(l,b,-1,-2,-23,1,-2,-15,Blocks.AIR);box(l,b,-1,-1,-23,1,-1,-15,Blocks.BIRCH_PLANKS);box(l,b,0,-2,-14,0,0,-14,Blocks.AIR);at(l,b,0,-1,-14,Blocks.LADDER);light(l,b,7,3,-6);
    }
    private static void crimson(ServerLevel l,BlockPos b){
        room(l,b,-18,18,-43,0,0,13,Blocks.POLISHED_BLACKSTONE_BRICKS,LiteraryRegistry.RED_FLOOR.get());box(l,b,-3,13,-27,3,15,-21,Blocks.AIR);
        for(int x:new int[]{-14,14})for(int z=-38;z<-6;z+=8)box(l,b,x,0,z,x,11,z,Blocks.POLISHED_BASALT);
        box(l,b,-17,6,-41,17,6,-35,Blocks.DARK_OAK_PLANKS);box(l,b,-17,6,-35,-14,6,-6,Blocks.DARK_OAK_PLANKS);box(l,b,14,6,-35,17,6,-6,Blocks.DARK_OAK_PLANKS);
        for(int z=-34;z<-6;z++){at(l,b,-14,7,z,Blocks.DARK_OAK_FENCE);at(l,b,14,7,z,Blocks.DARK_OAK_FENCE);}steps(l,b,-11,-7,0,7,false);box(l,b,-13,6,-17,-10,6,-13,Blocks.DARK_OAK_PLANKS);
        at(l,b,0,0,-30,Blocks.JUKEBOX);for(var q:List.of(new BlockPos(-12,0,-35),new BlockPos(12,0,-35),new BlockPos(0,7,-39)))prop(l,b,q,LiteraryPropBlock.Kind.CYLINDER,Direction.SOUTH);
        desk(l,b,0,0,-39);furniture(l,b,9,0,-12,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.WEST);light(l,b,0,8,-10);
    }
    private static void bly(ServerLevel l,BlockPos b){
        room(l,b,-12,12,-53,0,0,7,Blocks.CALCITE,Blocks.DARK_OAK_PLANKS);box(l,b,-1,-5,-50,1,-1,-41,Blocks.WATER);box(l,b,-2,-5,-51,2,-5,-40,Blocks.STONE_BRICKS);
        for(int z:new int[]{-9,-20,-31})for(int x:new int[]{-6,6}){room(l,b,x-2,x+2,z-3,z+2,0,3,Blocks.DARK_OAK_PLANKS,Blocks.DARK_OAK_PLANKS);door(l,b,x+(x<0?2:-2),0,z,Direction.EAST,false);furniture(l,b,x,0,z-2,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);}
        for(int z:new int[]{-6,-17,-28})light(l,b,0,4,z);desk(l,b,8,0,-38);box(l,b,8,2,-53,10,4,-53,Blocks.BLACK_STAINED_GLASS);
    }
    private static void terrain(ServerLevel l,BlockPos b,LabyrinthPlace p,Block ground){var r=p.room();if(ground==Blocks.SAND)box(l,b,r.minX(),-4,r.minZ(),r.maxX(),-4,0,Blocks.SANDSTONE);box(l,b,r.minX(),-3,r.minZ(),r.maxX(),-1,0,ground);for(int x=r.minX()+1;x<r.maxX();x+=6)for(int z=r.minZ()+3;z<-10;z+=9){if(Math.abs(x)<9)continue;int ox=Math.floorMod(x*31+z*17,5)-2,oz=Math.floorMod(x*13-z*7,5)-2;if(buildingClearing(p,x+ox,z+oz))continue;tree(l,b,x+ox,0,z+oz,4+(Math.abs(x+z)%3));}}
    /** Reserve the whole canopy around roofs and walls before planting the forest. */
    static boolean buildingClearing(LabyrinthPlace p,int x,int z){return switch(p){
        case CAMP_BLOOD->{boolean cabin=false;for(int cx:new int[]{-20,20})for(int cz:new int[]{-26,-57,-83})if(Math.abs(x-cx)<=12&&Math.abs(z-cz)<=11)cabin=true;yield cabin;}
        case MAPPING_INTERIOR->Math.abs(x)<=17&&z>=-41&&z<=-3;
        case END_WORLD_CABIN->Math.abs(x)<=17&&z>=-40&&z<=-8;
        case ELK_LOT->(x>=-27&&x<=-3&&z>=-28&&z<=0)||((Math.abs(x-9)<=6||Math.abs(x-21)<=6)&&z>=-33&&z<=-16);
        default->false;
    };}
    private static void tree(ServerLevel l,BlockPos b,int x,int y,int z,int height){box(l,b,x,y,z,x,y+height,z,Blocks.SPRUCE_LOG);for(int h=2;h<=height+2;h++){int w=Math.max(1,(height+3-h)/2);box(l,b,x-w,y+h,z-w,x+w,y+h,z+w,Blocks.SPRUCE_LEAVES);}}
    private static void roof(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,int eaves){
        // Foundations skirt the building. They must not overwrite its floor or cellar ladder.
        for(int x=x0-1;x<=x1+1;x++)for(int z=z0-1;z<=z1+1;z++)if(x<x0||x>x1||z<z0||z>z1)
            box(l,b,x,-2,z,x,-1,z,Blocks.STONE_BRICKS);
        int mid=(x0+x1)/2;for(int x=x0-1;x<=x1+1;x++){int y=eaves+Math.min(x-x0+1,x1+1-x)/2;for(int z=z0-1;z<=z1+1;z++)BuildBlocks.set(l,b.offset(x,y,z),Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,x<=mid?Direction.EAST:Direction.WEST),F);if(x>=x0&&x<=x1)for(int zz:new int[]{z0,z1})box(l,b,x,eaves,zz,x,y-1,zz,Blocks.SPRUCE_PLANKS);}
        for(int z=z0-1;z<=z1+1;z++)at(l,b,mid,eaves+(x1-x0)/4+1,z,Blocks.DARK_OAK_SLAB);
    }
    private static void window(ServerLevel l,BlockPos b,int x,int z,boolean east){
        for(int w=-2;w<=2;w++)for(int y=1;y<=4;y++){var at=east?b.offset(x,y,z+w):b.offset(x+w,y,z);BuildBlocks.set(l,at,(Math.abs(w)==2||y==1||y==4?Blocks.STRIPPED_SPRUCE_LOG:Blocks.GLASS_PANE).defaultBlockState(),F);}
    }
    private static void truck(ServerLevel l,BlockPos b,int x,int z){box(l,b,x-1,0,z-3,x+1,0,z+3,Blocks.GRAY_CONCRETE);box(l,b,x-1,1,z-1,x+1,2,z+1,Blocks.RED_TERRACOTTA);box(l,b,x-1,2,z-1,x+1,2,z-1,Blocks.BLACK_STAINED_GLASS);for(int xx:new int[]{x-2,x+2})for(int zz:new int[]{z-2,z+2})at(l,b,xx,0,zz,Blocks.BLACK_CONCRETE);box(l,b,x-1,1,z+2,x+1,1,z+3,Blocks.IRON_BARS);}
    private static void lot(ServerLevel l,BlockPos b){
        terrain(l,b,LabyrinthPlace.ELK_LOT,Blocks.GRASS_BLOCK);box(l,b,-28,-1,-36,28,-1,-3,Blocks.GRAY_CONCRETE);room(l,b,-23,-7,-24,-5,0,6,LiteraryRegistry.SIDING.get(),Blocks.DARK_OAK_PLANKS);door(l,b,-7,0,-15,Direction.EAST,true);desk(l,b,-18,0,-12);light(l,b,-12,3,-15);
        roof(l,b,-23,-7,-24,-5,6);window(l,b,-23,-14,true);window(l,b,-16,-5,false);for(int x:new int[]{-1,9,21})truck(l,b,x,-24);for(int z=-46;z>-105;z-=7)for(int x=-22;x<=22;x+=4)at(l,b,x,0,z,Blocks.TALL_GRASS);NovelRooms.sign(l,b.offset(-7,2,-8),Direction.EAST,new String[]{"LAST STOP","Open late","",""});
    }
    private static void fan(ServerLevel l,BlockPos b){
        room(l,b,-11,11,-28,0,0,9,LiteraryRegistry.SIDING.get(),Blocks.BIRCH_PLANKS);prop(l,b,FAN,LiteraryPropBlock.Kind.FAN,Direction.SOUTH);for(int y=0;y<4;y++){at(l,b,0,y,-18,Blocks.DARK_OAK_PLANKS);BuildBlocks.set(l,b.offset(0,y,-17),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),F);}
        box(l,b,-4,-1,-21,4,-1,-12,Blocks.GRAY_WOOL);box(l,b,6,0,-23,9,2,-23,Blocks.STONE_BRICKS);at(l,b,7,0,-22,Blocks.CAMPFIRE);furniture(l,b,-7,0,-21,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);furniture(l,b,-7,0,-13,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.EAST);box(l,b,6,2,-28,9,4,-28,Blocks.BLACK_STAINED_GLASS);
    }
    private static void mapping(ServerLevel l,BlockPos b){
        terrain(l,b,LabyrinthPlace.MAPPING_INTERIOR,Blocks.GRASS_BLOCK);room(l,b,-13,13,-36,-8,0,5,LiteraryRegistry.SIDING.get(),Blocks.BIRCH_PLANKS);door(l,b,0,0,-8,Direction.SOUTH,true);box(l,b,0,-1,-8,0,-1,0,Blocks.GRAVEL);
        box(l,b,-12,0,-24,12,4,-24,Blocks.CALCITE);door(l,b,0,0,-24,Direction.SOUTH,true);door(l,b,7,0,-24,Direction.SOUTH,true);NovelRooms.bed(l,b.offset(6,0,-32),Blocks.BLUE_BED,Direction.NORTH);furniture(l,b,-8,0,-19,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);furniture(l,b,-9,0,-29,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.EAST);at(l,b,-11,0,-33,Blocks.FURNACE);
        box(l,b,-1,-6,-76,1,-6,-18,Blocks.DIRT);box(l,b,-1,-5,-76,1,-5,-18,Blocks.AIR);box(l,b,-1,-4,-76,1,-2,-18,Blocks.DIRT);box(l,b,0,-5,-17,0,0,-17,Blocks.AIR);for(int y=-5;y<=-1;y++)BuildBlocks.set(l,b.offset(0,y,-17),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),F);prop(l,b,PILE,LiteraryPropBlock.Kind.BELONGINGS,Direction.SOUTH);light(l,b,0,3,-12);light(l,b,0,-4,-41);roof(l,b,-13,13,-36,-8,5);window(l,b,-13,-18,true);window(l,b,13,-30,true);for(int x:new int[]{-9,9})window(l,b,x,-8,false);box(l,b,-4,-1,-7,4,-1,-4,Blocks.SPRUCE_PLANKS);for(int x:new int[]{-4,4})box(l,b,x,0,-4,x,4,-4,Blocks.STRIPPED_SPRUCE_LOG);
        ScenePlaytestRepairs.cabin(l,b);
    }
    private static void rabbit(ServerLevel l,BlockPos b){
        terrain(l,b,LabyrinthPlace.HOLY_RABBIT,Blocks.SNOW_BLOCK);tree(l,b,0,0,-25,19);box(l,b,-5,0,-29,5,4,-20,Blocks.SPRUCE_LEAVES);box(l,b,-3,0,-28,3,0,-21,Blocks.AIR);box(l,b,-3,-1,-28,3,-1,-21,Blocks.PODZOL);box(l,b,-2,0,-20,2,2,-18,Blocks.AIR);
        at(l,b,0,2,-25,LiteraryRegistry.CARVED_TRUNK.get());prop(l,b,new BlockPos(0,3,-25),LiteraryPropBlock.Kind.TRUNK_MARK,Direction.SOUTH);
        // The drag trench is physical: three blocks deep, with no side route over the drifts.
        box(l,b,-3,0,-118,3,3,-33,Blocks.SNOW_BLOCK);box(l,b,-1,0,-118,1,2,-30,Blocks.AIR);box(l,b,-1,-1,-118,1,-1,-30,LiteraryRegistry.DRAG_SNOW.get());for(int z=-119;z<=-34;z++)for(int x:new int[]{-2,2})box(l,b,x,0,z,x,3,z,Blocks.SNOW_BLOCK);
        at(l,b,0,0,-120,Blocks.BARREL);box(l,b,-3,-1,-123,3,-1,-119,Blocks.SNOW_BLOCK);
    }
    private static void confession(ServerLevel l,BlockPos b){
        room(l,b,-12,12,-33,0,0,9,LiteraryRegistry.DARK_PANEL.get(),Blocks.DARK_OAK_PLANKS);for(int x:new int[]{-11,11})for(int z=-29;z<-8;z++)at(l,b,x,0,z,Blocks.BOOKSHELF);
        desk(l,b,-8,0,-25);paper(l,b,JOURNAL,LiteraryTexts.source(LabyrinthPlace.CONFESSION));furniture(l,b,1,0,-22,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.SOUTH);furniture(l,b,1,0,-17,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.NORTH);box(l,b,-3,2,-33,4,5,-33,Blocks.BLACK_STAINED_GLASS);light(l,b,0,4,-21);box(l,b,9,0,-29,11,3,-29,Blocks.DARK_OAK_PLANKS);
    }
    private static void carcasses(ServerLevel l,BlockPos b){ElkCarcassMap.build(l,b);}
    private static void lake(ServerLevel l,BlockPos b,LabyrinthPlace p){
        terrain(l,b,p,Blocks.SAND);var r=p.room();box(l,b,-28,-11,-72,28,-1,-13,Blocks.WATER);box(l,b,-29,-12,-73,29,-12,-12,Blocks.CLAY);
        // The far bank is reached by real rowing/swimming rather than an arrival trigger.
        box(l,b,-28,-1,r.minZ()+4,28,-1,-73,Blocks.GRASS_BLOCK);for(int x=-26;x<=26;x+=5){tree(l,b,x,0,r.minZ()+5,5);}
        if(p==LabyrinthPlace.COSTUME_NIGHT){box(l,b,-28,-1,-48,28,-1,-13,Blocks.SAND);for(int i=0;i<9;i++){var stand=new ArmorStand(l,b.getX()+(i%3-1)*12+.5,b.getY(),b.getZ()-18-(i/3)*11+.5);stand.setNoGravity(true);stand.setInvulnerable(true);stand.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN));stand.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.LEATHER_CHESTPLATE));stand.addTag("HouseCostume");BuildBlocks.after(l,()->l.addFreshEntity(stand));}box(l,b,-28,-1,-74,28,-1,-55,Blocks.GRASS_BLOCK);}
        if(p==LabyrinthPlace.WINTER_LAKE){box(l,b,-28,-1,-72,28,-1,-13,Blocks.ICE);box(l,b,-28,0,-12,28,0,-1,Blocks.SNOW);box(l,b,-28,0,-83,28,0,-73,Blocks.SNOW);for(int z=-70;z<-15;z+=7)for(int x=-24;x<=24;x+=7)at(l,b,x,0,z,Blocks.SNOW);}
        if(p==LabyrinthPlace.MOVIE_NIGHT){box(l,b,-4,-1,-87,4,-1,-73,Blocks.OAK_PLANKS);box(l,b,-1,-1,-14,1,-1,-3,Blocks.OAK_PLANKS);}
        desk(l,b,4,0,-6);light(l,b,7,4,-5);
    }
    private static void camp(ServerLevel l,BlockPos b){
        terrain(l,b,LabyrinthPlace.CAMP_BLOOD,Blocks.PODZOL);for(int x:new int[]{-20,20})for(int z:new int[]{-26,-57,-83}){room(l,b,x-8,x+8,z-7,z+7,0,5,Blocks.SPRUCE_PLANKS,Blocks.SPRUCE_PLANKS);door(l,b,x+(x<0?8:-8),0,z,Direction.EAST,true);for(int dx:new int[]{-5,5}){NovelRooms.bed(l,b.offset(x+dx,0,z-4),Blocks.RED_BED,Direction.NORTH);furniture(l,b,x+dx,0,z+3,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.NORTH);}for(int zz=z-8;zz<=z+8;zz++)box(l,b,x-9,5,zz,x+9,5,zz,Blocks.SPRUCE_SLAB);roof(l,b,x-8,x+8,z-7,z+7,5);window(l,b,x,z-7,false);light(l,b,x,3,z);}
        for(int z=-8;z>=-98;z--)box(l,b,-2,-1,z,2,-1,z,Blocks.GRAVEL);at(l,b,6,0,-45,Blocks.CAMPFIRE);NovelRooms.sign(l,b.offset(4,1,-97),Direction.SOUTH,new String[]{"SERVICE ROAD","No camp traffic","",""});
    }
    private static void diary(ServerLevel l,BlockPos b){
        room(l,b,-16,16,-42,0,0,8,Blocks.CALCITE,Blocks.OAK_PLANKS);room(l,b,-14,-3,-38,-20,0,4,Blocks.CALCITE,Blocks.OAK_PLANKS);door(l,b,-3,0,-27,Direction.EAST,true);NovelRooms.bed(l,b.offset(-10,0,-33),Blocks.BLUE_BED,Direction.NORTH);desk(l,b,-8,0,-21);
        prop(l,b,CAMERA,LiteraryPropBlock.Kind.CAMERA,Direction.SOUTH);for(int x:new int[]{-5,5})at(l,b,x,0,-14,Blocks.STONE_PRESSURE_PLATE);at(l,b,-1,-1,-20,Blocks.SCULK_SENSOR);at(l,b,-1,0,-20,Blocks.BLUE_CARPET);for(int x=-13;x<=13;x++)at(l,b,x,0,-18,Blocks.TRIPWIRE);light(l,b,0,4,-8);light(l,b,8,4,-32);box(l,b,3,2,-42,10,4,-42,Blocks.BLACK_STAINED_GLASS);
    }
    private static void wheel(ServerLevel l,BlockPos b){
        room(l,b,-26,26,-64,0,0,9,Blocks.CALCITE,Blocks.OAK_PLANKS);for(int z:new int[]{-17,-34,-51}){box(l,b,-25,0,z,25,5,z,Blocks.CALCITE);for(int x:new int[]{-14,14}){door(l,b,x,0,z,Direction.SOUTH,false);NovelRooms.sign(l,b.offset(x+2,2,z),Direction.SOUTH,new String[]{""+(Math.abs(z)/17*2+(x>0?1:0)),"","",""});}}
        for(int x:new int[]{-14,14}){NovelRooms.bed(l,b.offset(x,0,-60),Blocks.WHITE_BED,Direction.NORTH);furniture(l,b,x,0,-25,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.SOUTH);desk(l,b,x,0,-43);}light(l,b,0,4,-9);light(l,b,0,4,-29);light(l,b,0,4,-48);
        door(l,b,0,0,-7,Direction.SOUTH,false);prop(l,b,new BlockPos(14,0,-58),LiteraryPropBlock.Kind.PHOTO,Direction.SOUTH);
    }
    private static void set(ServerLevel l,BlockPos b){
        room(l,b,-20,20,-46,0,0,10,Blocks.CALCITE,Blocks.OAK_PLANKS);room(l,b,-16,9,-39,-14,0,5,Blocks.CALCITE,Blocks.OAK_PLANKS);door(l,b,0,0,-14,Direction.SOUTH,true);room(l,b,12,18,-42,-31,0,4,Blocks.DARK_OAK_PLANKS,Blocks.DARK_OAK_PLANKS);door(l,b,12,0,-36,Direction.EAST,true);
        prop(l,b,CAMERA,LiteraryPropBlock.Kind.TRIPOD,Direction.SOUTH);for(int x:new int[]{-17,17})for(int z:new int[]{-9,-25})prop(l,b,new BlockPos(x,0,z),LiteraryPropBlock.Kind.LIGHT_STAND,Direction.SOUTH);
        for(int x:new int[]{-5,0,5})prop(l,b,new BlockPos(x,0,-20),LiteraryPropBlock.Kind.TAPE_X,Direction.SOUTH);furniture(l,b,-8,0,-30,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);furniture(l,b,2,0,-35,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH);NovelRooms.bed(l,b.offset(5,0,-34),Blocks.WHITE_BED,Direction.NORTH);prop(l,b,BOOTH,LiteraryPropBlock.Kind.RECEIVER,Direction.SOUTH);light(l,b,0,5,-8);
    }
    /**
     * A lake cabin at dusk (0.4.52). A front door onto a deep porch; a bedroom with two beds against the lake; a kitchen;
     * a living room with the television, the hearth and the dining table where the account will lie; windows on every
     * side, so the storm is always in view. A small shed stands at the edge of the yard: its door is the only way out.
     */
    private static void endCabin(ServerLevel l,BlockPos b){
        terrain(l,b,LabyrinthPlace.END_WORLD_CABIN,Blocks.GRASS_BLOCK);box(l,b,-27,-8,-76,27,-1,-42,Blocks.WATER);box(l,b,-28,-9,-77,28,-9,-41,Blocks.GRAVEL);
        room(l,b,-12,12,-35,-13,0,6,Blocks.SPRUCE_PLANKS,Blocks.SPRUCE_PLANKS);door(l,b,0,0,-13,Direction.SOUTH,false);
        // Bedroom (north-west): two beds under the lake window, a lamp between them, the visitors' request on its stand.
        box(l,b,-4,0,-34,-4,5,-25,Blocks.SPRUCE_PLANKS);box(l,b,-11,0,-25,-4,5,-25,Blocks.SPRUCE_PLANKS);for(int y=0;y<=5;y++){at(l,b,-4,y,-25,Blocks.STRIPPED_SPRUCE_LOG);at(l,b,-4,y,-34,Blocks.STRIPPED_SPRUCE_LOG);}
        box(l,b,-4,0,-29,-4,1,-29,Blocks.AIR);NovelRooms.door(l,b.offset(-4,0,-29),Direction.EAST,Blocks.SPRUCE_DOOR,false);at(l,b,-4,2,-29,Blocks.STRIPPED_SPRUCE_LOG);
        NovelRooms.bed(l,b.offset(-10,0,-33),Blocks.WHITE_BED,Direction.NORTH);NovelRooms.bed(l,b.offset(-6,0,-33),Blocks.LIGHT_GRAY_BED,Direction.NORTH);
        furniture(l,b,-8,0,-34,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH);
        furniture(l,b,-11,0,-27,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.EAST);light(l,b,-8,4,-29);
        // Kitchen (south-west), open to the living room.
        BuildBlocks.set(l,b.offset(-11,0,-23),Blocks.SMOKER.defaultBlockState().setValue(AbstractFurnaceBlock.FACING,Direction.EAST),F);at(l,b,-11,0,-22,Blocks.CAULDRON);
        for(int z=-21;z<=-15;z++){at(l,b,-11,0,z,Blocks.SPRUCE_PLANKS);BuildBlocks.set(l,b.offset(-11,3,z),Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP),F);}
        for(int z:new int[]{-20,-16})at(l,b,-11,1,z,Blocks.FLOWER_POT);
        furniture(l,b,-8,0,-18,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH);furniture(l,b,-8,0,-17,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.NORTH);furniture(l,b,-8,0,-19,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.SOUTH);light(l,b,-8,4,-19);
        // Living room: the television on its cabinet, seats facing it, the hearth, and the dining table.
        furniture(l,b,7,0,-24,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);prop(l,b,TV,LiteraryPropBlock.Kind.TELEVISION,Direction.SOUTH);
        furniture(l,b,5,0,-20,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.NORTH);furniture(l,b,7,0,-20,HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.NORTH);furniture(l,b,9,0,-20,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.NORTH);furniture(l,b,7,0,-22,HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.NORTH);
        furniture(l,b,2,0,-30,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH);furniture(l,b,2,0,-29,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);furniture(l,b,2,0,-31,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);
        furniture(l,b,1,0,-30,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);furniture(l,b,3,0,-30,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        box(l,b,10,0,-34,11,11,-33,Blocks.BRICKS);box(l,b,9,0,-32,11,2,-32,Blocks.BRICKS);at(l,b,10,0,-32,Blocks.CAMPFIRE);at(l,b,10,1,-32,Blocks.AIR);
        for(int x=9;x<=11;x++)BuildBlocks.set(l,b.offset(x,3,-32),Blocks.DARK_OAK_SLAB.defaultBlockState(),F);
        light(l,b,2,4,-30);light(l,b,7,4,-21);light(l,b,0,4,-17);
        // Windows on every side: the lake behind, the porch in front, the woods either side.
        glaze(l,b,true,-35,-9,-7);glaze(l,b,true,-35,1,7);glaze(l,b,false,-12,-31,-28);glaze(l,b,false,-12,-21,-18);
        glaze(l,b,false,12,-29,-26);glaze(l,b,false,12,-21,-17);glaze(l,b,true,-13,-8,-6);glaze(l,b,true,-13,4,8);
        // The porch, deep and roofed, with the jar of grasshoppers still on it.
        box(l,b,-12,-1,-12,12,-1,-8,Blocks.SPRUCE_PLANKS);for(int x:new int[]{-12,12})box(l,b,x,0,-8,x,5,-8,Blocks.SPRUCE_LOG);box(l,b,-12,6,-12,12,6,-8,Blocks.SPRUCE_SLAB);prop(l,b,new BlockPos(-7,0,-9),LiteraryPropBlock.Kind.JAR,Direction.SOUTH);
        roof(l,b,-12,12,-35,-13,6);
        // A trodden path from the porch to the shed.
        for(int z=-7;z<=-5;z++)for(int x=-1;x<=1;x++)at(l,b,x,-1,z,Blocks.DIRT_PATH);
        shed(l,b);
    }
    /** Glass panes from {@code from} to {@code to} along a wall, three high, in a stripped spruce frame. */
    private static void glaze(ServerLevel l,BlockPos b,boolean alongX,int wall,int from,int to){
        for(int a=from-1;a<=to+1;a++)for(int y=0;y<=4;y++){boolean frame=a<from||a>to||y==0||y==4;var at=alongX?b.offset(a,y,wall):b.offset(wall,y,a);
            if(frame){if(y>0)BuildBlocks.set(l,at,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(),F);}else BuildBlocks.set(l,at,Blocks.GLASS_PANE.defaultBlockState(),F);}
    }
    /** The way out: a small shed at the edge of the yard, built against the House's own door at its back. */
    private static void shed(ServerLevel l,BlockPos b){
        box(l,b,-3,-1,-4,3,-1,0,Blocks.SPRUCE_PLANKS);box(l,b,-3,0,-4,3,3,0,Blocks.AIR);
        for(int z=-4;z<=0;z++)for(int x:new int[]{-3,3})box(l,b,x,0,z,x,3,z,Blocks.SPRUCE_PLANKS);box(l,b,-3,0,-4,3,3,-4,Blocks.SPRUCE_PLANKS);
        for(int x:new int[]{-3,3})box(l,b,x,0,-4,x,3,-4,Blocks.STRIPPED_SPRUCE_LOG);box(l,b,-2,4,-4,2,4,-4,Blocks.SPRUCE_PLANKS);box(l,b,-1,5,-4,1,5,-4,Blocks.SPRUCE_PLANKS);
        box(l,b,0,0,-4,0,1,-4,Blocks.AIR);NovelRooms.door(l,b.offset(0,0,-4),Direction.NORTH,Blocks.SPRUCE_DOOR,false);
        for(int z=-5;z<=0;z++){
            for(int x:new int[]{-4,-3})BuildBlocks.set(l,b.offset(x,x==-4?3:4,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST),F);
            for(int x:new int[]{3,4})BuildBlocks.set(l,b.offset(x,x==4?3:4,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST),F);
            for(int x:new int[]{-2,-1})BuildBlocks.set(l,b.offset(x,5,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST),F);
            for(int x:new int[]{1,2})BuildBlocks.set(l,b.offset(x,5,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST),F);
            BuildBlocks.set(l,b.offset(0,6,z),Blocks.DARK_OAK_SLAB.defaultBlockState(),F);
        }
        for(int x:new int[]{-3,3})BuildBlocks.set(l,b.offset(x,1,-2),Blocks.GLASS_PANE.defaultBlockState(),F);
        at(l,b,-2,0,-1,Blocks.CRAFTING_TABLE);at(l,b,2,0,-1,Blocks.OAK_LOG);at(l,b,2,1,-1,Blocks.OAK_LOG);at(l,b,-2,0,-3,Blocks.COMPOSTER);
        at(l,b,0,5,-2,Blocks.CHAIN);at(l,b,0,4,-2,Blocks.CHAIN);BuildBlocks.set(l,b.offset(0,3,-2),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);
    }
    private static void copyLanding(ServerLevel l,BlockPos b,LabyrinthPlace p){box(l,b,-5,-1,-8,5,-1,0,Blocks.SMOOTH_STONE);room(l,b,-5,5,-8,0,0,5,Blocks.GRAY_TERRACOTTA,Blocks.SMOOTH_STONE);}
}
