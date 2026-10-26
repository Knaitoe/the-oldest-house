package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.UncannyPropBlock.Kind.*;

/** Six complete authored spaces. Every placement participates in the bounded native construction plan. */
final class UncannyRooms0129 {
    public static final List<LabyrinthPlace> PLACES=List.of(LabyrinthPlace.PAINTED_INTERIOR,LabyrinthPlace.SECOND_WAITRESS,LabyrinthPlace.TUNNEL_PERIPHERY,LabyrinthPlace.MISSING_SLEEP,LabyrinthPlace.LAYERED_MAN,LabyrinthPlace.MOTION_LIGHT);
    public static final BlockPos PARLOR_BOARD=new BlockPos(0,1,-28),PARLOR_DOOR=new BlockPos(8,0,-23);
    public static final BlockPos DINER_TABLE=new BlockPos(5,1,-19),MONITOR_AT=new BlockPos(-5,6,-22),BED_AT=new BlockPos(-6,5,-26),PARENTS_DOOR=new BlockPos(4,5,-15);
    public static final BlockPos CELLAR_DOOR=new BlockPos(0,0,-5),MASS=new BlockPos(0,-5,-20),TOOL=new BlockPos(5,-4,-13),KEY_CAVITY=new BlockPos(0,-4,-22);
    public static final BlockPos INTERCOM=new BlockPos(3,1,-10),FLOOD=new BlockPos(0,4,-20),HINGED=new BlockPos(4,0,-5);
    public static final List<BlockPos> FALSE_DOORS=List.of(new BlockPos(-4,0,-4),new BlockPos(-4,0,-8),new BlockPos(0,0,-2));
    public static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private UncannyRooms0129(){}
    public static boolean isUncanny(LabyrinthPlace p){return p!=null&&PLACES.contains(p);}
    public static BlockPos source(LabyrinthPlace p){return switch(p){
        case PAINTED_INTERIOR->new BlockPos(3,1,-11);case SECOND_WAITRESS->new BlockPos(-6,1,-8);case TUNNEL_PERIPHERY->new BlockPos(-3,1,-5);
        case MISSING_SLEEP->new BlockPos(-6,6,-22);case LAYERED_MAN->new BlockPos(3,1,-5);case MOTION_LIGHT->new BlockPos(3,1,-9);default->throw new IllegalArgumentException(p.id());};}
    public static BlockPos account(LabyrinthPlace p){return switch(p){
        case PAINTED_INTERIOR->new BlockPos(10,1,-10);case SECOND_WAITRESS->new BlockPos(-6,1,-5);case TUNNEL_PERIPHERY->new BlockPos(3,1,-69);
        case MISSING_SLEEP->new BlockPos(3,1,-4);case LAYERED_MAN->new BlockPos(-3,1,-5);case MOTION_LIGHT->new BlockPos(6,1,-3);default->throw new IllegalArgumentException(p.id());};}
    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace p){
        var r=p.room();box(l,b,r.minX(),r.minY(),r.minZ(),r.maxX(),r.maxY(),r.maxZ(),Blocks.AIR);
        switch(p){case PAINTED_INTERIOR->boarded(l,b);case SECOND_WAITRESS->diner(l,b);case TUNNEL_PERIPHERY->tunnel(l,b);case MISSING_SLEEP->bedroom(l,b);case LAYERED_MAN->cellar(l,b);case MOTION_LIGHT->foyer(l,b);default->throw new IllegalArgumentException(p.id());}
        // The return vestibule opens at z=0; several authored front walls stand at z=-1.
        // Join both cells without deleting the diner's own door farther inside the forecourt.
        box(l,b,0,0,-1,0,1,0,Blocks.AIR);
        Block threshold=switch(p){case PAINTED_INTERIOR->Blocks.COARSE_DIRT;case SECOND_WAITRESS->Blocks.GRAVEL;case TUNNEL_PERIPHERY->Blocks.SMOOTH_STONE;case MISSING_SLEEP->Blocks.SPRUCE_PLANKS;default->Blocks.DARK_OAK_PLANKS;};
        for(int z=-1;z<=0;z++)if(BuildBlocks.state(l,b.offset(0,-1,z)).isAir())at(l,b,0,-1,z,threshold);
        fit(l,b,account(p),LEDGER,Direction.SOUTH);
    }
    private static void at(ServerLevel l,BlockPos b,int x,int y,int z,Block block){BuildBlocks.set(l,b.offset(x,y,z),block.defaultBlockState(),FLAGS);}
    private static void state(ServerLevel l,BlockPos b,BlockPos rel,BlockState s){BuildBlocks.set(l,b.offset(rel),s,FLAGS);}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,Block block){NovelRooms.box(l,b,x,y,z,X,Y,Z,block.defaultBlockState());}
    public static void fit(ServerLevel l,BlockPos b,BlockPos rel,UncannyPropBlock.Kind k,Direction face){state(l,b,rel,UncannyPropBlock.of(k,face));}
    private static void room(ServerLevel l,BlockPos b,int x,int X,int z,int Z,int y,int height,Block wall,Block floor){NovelRooms.room(l,b,x,X,z,Z,y,height,wall,floor);}
    private static void table(ServerLevel l,BlockPos b,int x,int y,int z){NovelRooms.furniture(l,b.offset(x,y,z),HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);}
    private static void chair(ServerLevel l,BlockPos b,int x,int y,int z,Direction f){NovelRooms.furniture(l,b.offset(x,y,z),HouseholdFurnitureBlock.Kind.CANE_CHAIR,f);}
    private static void door(ServerLevel l,BlockPos b,BlockPos rel,Direction f,boolean open){box(l,b,rel.getX(),rel.getY(),rel.getZ(),rel.getX(),rel.getY()+1,rel.getZ(),Blocks.AIR);NovelRooms.door(l,b.offset(rel),f,Blocks.DARK_OAK_DOOR,open);}
    private static void lamp(ServerLevel l,BlockPos b,int x,int y,int z){state(l,b,new BlockPos(x,y,z),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));at(l,b,x,y+1,z,Blocks.CHAIN);}
    private static void note(ServerLevel l,BlockPos b,LabyrinthPlace p,UncannyPropBlock.Kind kind){fit(l,b,source(p),kind,Direction.SOUTH);}
    private static void boarded(ServerLevel l,BlockPos b){
        room(l,b,-14,14,-38,0,0,9,Blocks.DIRT,Blocks.COARSE_DIRT);
        for(int z=-2;z>=-35;z-=5)for(int x:new int[]{-13,13}){box(l,b,x,0,z,x,2,z,Blocks.ROOTED_DIRT);at(l,b,x,3,z,Blocks.HANGING_ROOTS);}
        room(l,b,-8,8,-28,-7,0,5,material("parlor_paper"),Blocks.DARK_OAK_PLANKS);
        box(l,b,-8,0,-15,8,4,-15,material("parlor_paper"));
        for(int z=-28;z<=-7;z++)for(int x:new int[]{-8,8})at(l,b,x,0,z,material("dark_wood"));
        for(int z:new int[]{-28,-15,-7})box(l,b,-8,0,z,8,0,z,material("dark_wood"));
        door(l,b,new BlockPos(0,0,-15),Direction.SOUTH,true);door(l,b,new BlockPos(0,0,-7),Direction.SOUTH,true);
        for(int z:new int[]{-11,-19,-24})for(int x:new int[]{-8,8}){
            for(int y=1;y<=2;y++)fit(l,b,new BlockPos(x,y,z),PAINTED_BOARD,x<0?Direction.EAST:Direction.WEST);
            at(l,b,x,3,z,material("dark_wood"));box(l,b,x<0?-9:9,0,z,x<0?-9:9,3,z,Blocks.DIRT);
        }
        for(int x=-2;x<=2;x++)for(int y=1;y<=2;y++)fit(l,b,new BlockPos(x,y,-28),PAINTED_BOARD,Direction.SOUTH);
        door(l,b,PARLOR_DOOR,Direction.WEST,false);
        furnishing(l,b,3,0,-11,UncannyFurnitureBlock.Kind.DESK,Direction.SOUTH);chair(l,b,3,0,-9,Direction.NORTH);note(l,b,LabyrinthPlace.PAINTED_INTERIOR,PAPER);
        for(int x=-3;x<=2;x++)for(int z=-24;z<=-18;z++)at(l,b,x,0,z,(x==-3||x==2||z==-24||z==-18)?Blocks.BROWN_CARPET:UncannyRegistry.PARLOR_RUG.get());
        sofa(l,b,-6,0,-22,Direction.EAST);
        furnishing(l,b,4,0,-20,UncannyFurnitureBlock.Kind.ARMCHAIR,Direction.WEST);
        furnishing(l,b,4,0,-23,UncannyFurnitureBlock.Kind.ARMCHAIR,Direction.WEST);
        for(int z=-21;z>=-22;z--)furnishing(l,b,0,0,z,UncannyFurnitureBlock.Kind.COFFEE_TABLE,Direction.EAST);
        for(int x=-6;x<=-4;x++)for(int y=0;y<=1;y++)furnishing(l,b,x,y,-27,UncannyFurnitureBlock.Kind.BOOKCASE,Direction.SOUTH);
        furnishing(l,b,6,0,-16,UncannyFurnitureBlock.Kind.SIDEBOARD,Direction.WEST);
        furnishing(l,b,6,1,-16,UncannyFurnitureBlock.Kind.DESK_LAMP);
        furnishing(l,b,-7,0,-10,UncannyFurnitureBlock.Kind.RADIATOR,Direction.EAST);
        furnishing(l,b,-7,2,-13,UncannyFurnitureBlock.Kind.PICTURE,Direction.EAST);
        for(int z:new int[]{-10,-18,-25})furnishing(l,b,-7,3,z,UncannyFurnitureBlock.Kind.SCONCE,Direction.EAST);
        furnishing(l,b,10,0,-10,UncannyFurnitureBlock.Kind.WORKBENCH,Direction.SOUTH);fit(l,b,new BlockPos(11,0,-12),BOX,Direction.SOUTH);
        lamp(l,b,10,4,-10);
        // Behind the removable painted rear opening there is only earth.
        box(l,b,-3,0,-30,3,4,-30,Blocks.DIRT);
    }
    private static Block material(String name){return UncannyRegistry.material(name);}
    private static void furnishing(ServerLevel l,BlockPos b,int x,int y,int z,UncannyFurnitureBlock.Kind kind,Direction facing){state(l,b,new BlockPos(x,y,z),UncannyFurnitureBlock.of(kind,facing));}
    private static void furnishing(ServerLevel l,BlockPos b,int x,int y,int z,UncannyFurnitureBlock.Kind kind){furnishing(l,b,x,y,z,kind,Direction.NORTH);}
    private static void sofa(ServerLevel l,BlockPos b,int x,int y,int z,Direction facing){
        for(int i=0;i<3;i++){var at=new BlockPos(x,y,z).relative(facing.getClockWise(),i);furnishing(l,b,at.getX(),at.getY(),at.getZ(),i==0?UncannyFurnitureBlock.Kind.SOFA_LEFT:i==2?UncannyFurnitureBlock.Kind.SOFA_RIGHT:UncannyFurnitureBlock.Kind.SOFA,facing);}
    }
    private static void diner(ServerLevel l,BlockPos b){
        // A roadside site: a narrow road, marked parking and paving, with rough ground beyond.
        box(l,b,-27,-3,-56,27,-2,0,Blocks.STONE);
        box(l,b,-27,-1,-56,27,-1,0,Blocks.GRASS_BLOCK);
        box(l,b,19,-1,-56,25,-1,0,material("asphalt"));
        box(l,b,17,-1,-56,18,-1,0,material("pavement"));
        box(l,b,-12,-1,-34,16,-1,-1,material("pavement"));
        box(l,b,-12,-1,-41,16,-1,-35,material("asphalt"));
        for(int z=-4;z>=-54;z-=7)box(l,b,22,0,z-2,22,0,z,Blocks.YELLOW_CARPET);
        for(int x=-11;x<=13;x+=4)box(l,b,x,0,-41,x,0,-36,Blocks.WHITE_CARPET);
        for(int z=-4;z>=-31;z-=3){at(l,b,-14,-1,z,Blocks.COARSE_DIRT);if(z%2==0)at(l,b,-15,0,z,Blocks.SHORT_GRASS);}
        room(l,b,-10,10,-32,-3,0,5,material("diner_wall"),material("diner_tile"));
        for(int y=0;y<=1;y++){
            box(l,b,-10,y,-32,10,y,-32,material("diner_dado"));box(l,b,-10,y,-3,10,y,-3,material("diner_dado"));
            box(l,b,-10,y,-31,-10,y,-4,material("diner_dado"));box(l,b,10,y,-31,10,y,-4,material("diner_dado"));
        }
        // Glazing occupies the actual wall cells; the frames have mullions and a shallow sill.
        for(int z=-5;z>=-21;z--)if(z%4!=-2)for(int y=2;y<=3;y++)furnishing(l,b,10,y,z,UncannyFurnitureBlock.Kind.WINDOW_FRAME,Direction.WEST);
        for(int x=-7;x<=7;x++)if(Math.abs(x)>1)for(int y=2;y<=3;y++)furnishing(l,b,x,y,-3,UncannyFurnitureBlock.Kind.WINDOW_FRAME,Direction.NORTH);
        box(l,b,-11,5,-33,11,5,-2,material("roof"));
        box(l,b,0,0,-3,0,1,-3,Blocks.AIR);NovelRooms.door(l,b.offset(0,0,-3),Direction.SOUTH,UncannyRegistry.DINER_DOOR.get(),true);
        for(int z=-7;z>=-23;z--)furnishing(l,b,-6,0,z,UncannyFurnitureBlock.Kind.COUNTER,Direction.EAST);
        // The original till and pie case retain their actual source and custody positions.
        fit(l,b,source(LabyrinthPlace.SECOND_WAITRESS),REGISTER,Direction.EAST);
        fit(l,b,new BlockPos(-6,1,-22),PIE_CASE,Direction.EAST);
        furnishing(l,b,-6,0,-5,UncannyFurnitureBlock.Kind.COUNTER,Direction.EAST);
        for(int z:new int[]{-10,-14,-18})furnishing(l,b,-4,0,z,UncannyFurnitureBlock.Kind.STOOL,Direction.WEST);
        for(int z:new int[]{-7,-11,-15,-19}){
            for(int x=5;x<=7;x++){
                furnishing(l,b,x,0,z,UncannyFurnitureBlock.Kind.TABLE);
                furnishing(l,b,x,0,z-1,UncannyFurnitureBlock.Kind.BOOTH,Direction.SOUTH);
                furnishing(l,b,x,0,z+1,UncannyFurnitureBlock.Kind.BOOTH,Direction.NORTH);
            }
            furnishing(l,b,7,1,z,UncannyFurnitureBlock.Kind.NAPKIN_CADDY);
            if(z!=-19)furnishing(l,b,5,1,z,UncannyFurnitureBlock.Kind.CUP);
        }
        for(int z:new int[]{-8,-14})for(int x=1;x<=2;x++){
            furnishing(l,b,x,0,z,UncannyFurnitureBlock.Kind.TABLE);
            furnishing(l,b,x,0,z-1,UncannyFurnitureBlock.Kind.DINER_CHAIR,Direction.SOUTH);
            furnishing(l,b,x,0,z+1,UncannyFurnitureBlock.Kind.DINER_CHAIR,Direction.NORTH);
        }
        furnishing(l,b,2,1,-8,UncannyFurnitureBlock.Kind.NAPKIN_CADDY);
        furnishing(l,b,1,1,-14,UncannyFurnitureBlock.Kind.CUP);
        // Booth four's cold caddy stands where the authored draft and black frost begin.
        furnishing(l,b,8,0,-19,UncannyFurnitureBlock.Kind.TABLE);
        furnishing(l,b,8,1,-19,UncannyFurnitureBlock.Kind.NAPKIN_CADDY);
        for(int z=-7;z>=-22;z--){
            furnishing(l,b,-9,0,z,z==-18?UncannyFurnitureBlock.Kind.SINK:UncannyFurnitureBlock.Kind.CABINET,Direction.EAST);
        }
        furnishing(l,b,-9,1,-11,UncannyFurnitureBlock.Kind.COFFEE_MAKER,Direction.EAST);
        furnishing(l,b,-9,2,-15,UncannyFurnitureBlock.Kind.SHELF,Direction.EAST);
        box(l,b,-9,0,-25,9,4,-25,material("diner_wall"));
        box(l,b,-6,1,-25,-3,2,-25,Blocks.AIR);
        for(int x=-6;x<=-3;x++)furnishing(l,b,x,0,-25,UncannyFurnitureBlock.Kind.COUNTER,Direction.SOUTH);
        door(l,b,new BlockPos(-8,0,-25),Direction.SOUTH,true);
        for(int z=-27;z>=-30;z--)furnishing(l,b,-9,0,z,z==-28?UncannyFurnitureBlock.Kind.GRIDDLE:UncannyFurnitureBlock.Kind.CABINET,Direction.EAST);
        furnishing(l,b,-4,0,-31,UncannyFurnitureBlock.Kind.FRIDGE,Direction.SOUTH);
        furnishing(l,b,-2,0,-31,UncannyFurnitureBlock.Kind.FRIDGE,Direction.SOUTH);
        for(int x=4;x<=8;x++)furnishing(l,b,x,0,-31,x==5?UncannyFurnitureBlock.Kind.SINK:UncannyFurnitureBlock.Kind.WORKBENCH,Direction.SOUTH);
        for(int z:new int[]{-27,-29,-31}){furnishing(l,b,9,0,z,UncannyFurnitureBlock.Kind.SHELF,Direction.WEST);furnishing(l,b,9,2,z,UncannyFurnitureBlock.Kind.SHELF,Direction.WEST);}
        furnishing(l,b,7,0,-27,UncannyFurnitureBlock.Kind.BIN);
        furnishing(l,b,-9,3,-28,UncannyFurnitureBlock.Kind.VENT,Direction.EAST);
        for(int z:new int[]{-7,-13,-19,-28})for(int x:new int[]{-6,-5,3,4})furnishing(l,b,x,4,z,UncannyFurnitureBlock.Kind.FLUORESCENT);
        for(int x:new int[]{-8,8})furnishing(l,b,x,3,-2,UncannyFurnitureBlock.Kind.EXTERIOR_LAMP,Direction.SOUTH);
        furnishing(l,b,-1,6,-3,UncannyFurnitureBlock.Kind.SIGN_LEFT,Direction.SOUTH);
        furnishing(l,b,0,6,-3,UncannyFurnitureBlock.Kind.SIGN_CENTER,Direction.SOUTH);
        furnishing(l,b,1,6,-3,UncannyFurnitureBlock.Kind.SIGN_RIGHT,Direction.SOUTH);
        for(int x:new int[]{-1,1})at(l,b,x,5,-3,material("roof"));
        furnishing(l,b,11,0,-30,UncannyFurnitureBlock.Kind.BIN,Direction.EAST);
        // A second actual exterior exit beside the till, not merely a door into the kitchen.
        door(l,b,new BlockPos(-10,0,-6),Direction.WEST,false);
        box(l,b,-12,-1,-8,-11,-1,-2,material("pavement"));
        box(l,b,-12,0,-8,-11,2,-2,Blocks.AIR);
    }
    private static void tunnel(ServerLevel l,BlockPos b){
        room(l,b,-3,3,-73,-1,0,3,material("workshop_wall"),Blocks.SMOOTH_STONE);
        for(int z=-3;z>=-70;z--){
            furnishing(l,b,-2,2,z,UncannyFurnitureBlock.Kind.PIPE_HORIZONTAL,Direction.EAST);
            if(z%8==0)furnishing(l,b,2,0,z,UncannyFurnitureBlock.Kind.DRAIN,Direction.WEST);
        }
        for(int z:new int[]{-12,-28,-44,-60}){
            fit(l,b,new BlockPos(-3,2,z),SIGNAL,Direction.EAST);fit(l,b,new BlockPos(3,1,z-2),SIGN,Direction.WEST);
            at(l,b,-1,-1,z,Blocks.YELLOW_CONCRETE);at(l,b,1,-1,z,Blocks.YELLOW_CONCRETE);
            furnishing(l,b,2,2,z-4,UncannyFurnitureBlock.Kind.VENT,Direction.WEST);
        }
        furnishing(l,b,-2,1,-7,UncannyFurnitureBlock.Kind.SWITCHBOX,Direction.EAST);
        for(int z:new int[]{-6,-20,-36,-52})furnishing(l,b,0,2,z,UncannyFurnitureBlock.Kind.FLUORESCENT);
        at(l,b,-3,0,-5,material("workshop_wall"));note(l,b,LabyrinthPlace.TUNNEL_PERIPHERY,CLIPBOARD);
        at(l,b,3,0,-69,material("workshop_wall"));box(l,b,-3,3,-73,3,6,-65,Blocks.AIR);box(l,b,-3,6,-73,3,6,-65,material("workshop_wall"));
        furnishing(l,b,0,5,-69,UncannyFurnitureBlock.Kind.FLUORESCENT);
        furnishing(l,b,2,0,-71,UncannyFurnitureBlock.Kind.BIN,Direction.WEST);
    }
    private static void bedroom(ServerLevel l,BlockPos b){
        room(l,b,-9,9,-29,-1,0,10,material("bedroom_paper"),Blocks.SPRUCE_PLANKS);
        box(l,b,-9,4,-29,9,4,-1,Blocks.SPRUCE_PLANKS);
        for(int z=-11;z>=-28;z--)for(int x:new int[]{-3,4}){
            at(l,b,x,5,z,material("dark_wood"));for(int y=6;y<=9;y++)at(l,b,x,y,z,material("bedroom_paper"));
        }
        for(int i=0;i<5;i++){int z=-4-i;box(l,b,-8,0,z,-6,i-1,z,Blocks.SPRUCE_PLANKS);for(int x=-8;x<=-6;x++)state(l,b,new BlockPos(x,i,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));box(l,b,-8,i+1,z,-6,5,z,Blocks.AIR);}
        box(l,b,-8,4,-11,-4,4,-9,Blocks.SPRUCE_PLANKS);
        door(l,b,new BlockPos(-3,5,-16),Direction.WEST,true);door(l,b,PARENTS_DOOR,Direction.EAST,true);
        for(int z=-4;z>=-9;z--)state(l,b,new BlockPos(-5,5,z),Blocks.SPRUCE_FENCE.defaultBlockState().setValue(FenceBlock.NORTH,z>-9).setValue(FenceBlock.SOUTH,z<-4));
        for(int x=-8;x<=-5;x++)state(l,b,new BlockPos(x,5,-3),Blocks.SPRUCE_FENCE.defaultBlockState().setValue(FenceBlock.EAST,x<-5).setValue(FenceBlock.WEST,x>-8));
        for(int x=2;x<=4;x++)furnishing(l,b,x,5,-2,UncannyFurnitureBlock.Kind.SIDEBOARD);
        furnishing(l,b,4,6,-2,UncannyFurnitureBlock.Kind.DESK_LAMP);
        furnishing(l,b,3,7,-2,UncannyFurnitureBlock.Kind.PICTURE);
        for(int z=-13;z>=-26;z--)for(int x=0;x<=1;x++)at(l,b,x,5,z,UncannyRegistry.PARLOR_RUG.get());
        // A furnished home below the five real stair treads, rather than an empty plinth.
        box(l,b,-8,0,-12,8,3,-12,material("bedroom_paper"));door(l,b,new BlockPos(1,0,-12),Direction.SOUTH,true);
        sofa(l,b,6,0,-18,Direction.WEST);
        for(int x=-2;x<=2;x++)for(int z=-23;z<=-17;z++)at(l,b,x,0,z,Blocks.LIGHT_GRAY_CARPET);
        furnishing(l,b,0,0,-20,UncannyFurnitureBlock.Kind.COFFEE_TABLE);
        furnishing(l,b,1,0,-20,UncannyFurnitureBlock.Kind.COFFEE_TABLE);
        furnishing(l,b,-5,0,-20,UncannyFurnitureBlock.Kind.DESK,Direction.EAST);
        furnishing(l,b,-5,1,-20,UncannyFurnitureBlock.Kind.DESK_LAMP);
        furnishing(l,b,-4,0,-18,UncannyFurnitureBlock.Kind.ARMCHAIR,Direction.NORTH);
        for(int x=-5;x<=-3;x++)for(int y=0;y<=1;y++)furnishing(l,b,x,y,-28,UncannyFurnitureBlock.Kind.BOOKCASE,Direction.SOUTH);
        for(int z=-5;z>=-9;z--)furnishing(l,b,8,0,z,z==-8?UncannyFurnitureBlock.Kind.SINK:UncannyFurnitureBlock.Kind.CABINET,Direction.WEST);
        furnishing(l,b,6,0,-10,UncannyFurnitureBlock.Kind.FRIDGE,Direction.SOUTH);
        furnishing(l,b,3,0,-4,UncannyFurnitureBlock.Kind.SIDEBOARD,Direction.SOUTH);
        furnishing(l,b,8,2,-11,UncannyFurnitureBlock.Kind.PICTURE,Direction.WEST);
        NovelRooms.bed(l,b.offset(BED_AT),Blocks.GRAY_BED,Direction.NORTH);NovelRooms.bed(l,b.offset(6,5,-25),Blocks.BLUE_BED,Direction.NORTH);
        furnishing(l,b,-6,5,-28,UncannyFurnitureBlock.Kind.HEADBOARD,Direction.SOUTH);
        furnishing(l,b,6,5,-27,UncannyFurnitureBlock.Kind.HEADBOARD,Direction.SOUTH);
        for(int x:new int[]{-7,7}){furnishing(l,b,x,5,-27,UncannyFurnitureBlock.Kind.BEDSIDE,Direction.SOUTH);furnishing(l,b,x,6,-27,UncannyFurnitureBlock.Kind.DESK_LAMP);}
        furnishing(l,b,-8,5,-17,UncannyFurnitureBlock.Kind.WARDROBE,Direction.EAST);
        furnishing(l,b,-8,5,-25,UncannyFurnitureBlock.Kind.HAMPER);
        furnishing(l,b,8,5,-19,UncannyFurnitureBlock.Kind.WARDROBE,Direction.WEST);
        furnishing(l,b,8,7,-23,UncannyFurnitureBlock.Kind.PICTURE,Direction.WEST);
        for(int x=-6;x<=-5;x++)furnishing(l,b,x,5,-22,UncannyFurnitureBlock.Kind.DESK,Direction.SOUTH);
        furnishing(l,b,-6,5,-23,UncannyFurnitureBlock.Kind.DESK,Direction.WEST);
        chair(l,b,-7,5,-23,Direction.EAST);
        fit(l,b,MONITOR_AT,MONITOR,Direction.SOUTH);fit(l,b,source(LabyrinthPlace.MISSING_SLEEP),PAPER,Direction.SOUTH);
        for(var camera:List.of(new BlockPos(-8,8,-24),new BlockPos(0,8,-28),new BlockPos(8,8,-12),new BlockPos(0,3,-4)))fit(l,b,camera,CAMERA,Direction.SOUTH);
        for(int z:new int[]{-8,-18,-27}){furnishing(l,b,0,9,z,UncannyFurnitureBlock.Kind.FLUORESCENT);furnishing(l,b,0,3,z,UncannyFurnitureBlock.Kind.FLUORESCENT);}
        for(int x:new int[]{-7,-6,6,7})for(int y=6;y<=7;y++)furnishing(l,b,x,y,-29,UncannyFurnitureBlock.Kind.WINDOW_FRAME,Direction.SOUTH);
        furnishing(l,b,2,5,-28,UncannyFurnitureBlock.Kind.RADIATOR,Direction.SOUTH);
    }
    private static void cellar(ServerLevel l,BlockPos b){
        // A complete house over a brick cellar. Solid backing continues under the stair well.
        box(l,b,-8,-7,-30,8,-1,-1,Blocks.STONE);
        room(l,b,-8,8,-30,-1,0,4,material("cellar_plaster"),Blocks.SPRUCE_PLANKS);
        box(l,b,-8,4,-30,8,4,-1,Blocks.DARK_OAK_PLANKS);
        for(int z:new int[]{-10,-20})box(l,b,-7,3,z,7,3,z,Blocks.STRIPPED_DARK_OAK_LOG);
        door(l,b,new BlockPos(0,0,-1),Direction.SOUTH,true);
        // Sitting room, kitchen and the rear bedroom read as an ordinary occupied home.
        furnishing(l,b,-6,0,-5,UncannyFurnitureBlock.Kind.SOFA_LEFT,Direction.EAST);
        furnishing(l,b,-6,0,-6,UncannyFurnitureBlock.Kind.SOFA_RIGHT,Direction.EAST);
        furnishing(l,b,-4,0,-6,UncannyFurnitureBlock.Kind.COFFEE_TABLE);
        furnishing(l,b,-6,0,-11,UncannyFurnitureBlock.Kind.BOOKCASE,Direction.EAST);
        furnishing(l,b,-7,2,-7,UncannyFurnitureBlock.Kind.PICTURE,Direction.EAST);
        for(int z=-3;z>=-10;z--)furnishing(l,b,6,0,z,z==-7?UncannyFurnitureBlock.Kind.SINK:UncannyFurnitureBlock.Kind.CABINET,Direction.WEST);
        furnishing(l,b,5,0,-11,UncannyFurnitureBlock.Kind.FRIDGE,Direction.SOUTH);
        furnishing(l,b,3,0,-5,UncannyFurnitureBlock.Kind.COUNTER,Direction.SOUTH);
        note(l,b,LabyrinthPlace.LAYERED_MAN,LEDGER);
        furnishing(l,b,-3,0,-5,UncannyFurnitureBlock.Kind.SIDEBOARD,Direction.SOUTH);
        box(l,b,-7,0,-15,7,3,-15,material("cellar_plaster"));door(l,b,new BlockPos(4,0,-15),Direction.NORTH,false);
        box(l,b,0,0,-29,0,3,-16,material("cellar_plaster"));door(l,b,new BlockPos(0,0,-20),Direction.EAST,false);
        state(l,b,new BlockPos(-4,0,-24),Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT));
        state(l,b,new BlockPos(-4,0,-25),Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD));
        furnishing(l,b,-6,0,-25,UncannyFurnitureBlock.Kind.BEDSIDE);
        furnishing(l,b,-6,0,-18,UncannyFurnitureBlock.Kind.WARDROBE,Direction.EAST);
        furnishing(l,b,4,0,-22,UncannyFurnitureBlock.Kind.TABLE);
        chair(l,b,3,0,-22,Direction.EAST);chair(l,b,5,0,-22,Direction.WEST);
        furnishing(l,b,6,0,-27,UncannyFurnitureBlock.Kind.SIDEBOARD,Direction.WEST);
        // The cellar is excavated inside the foundation, never into an unbounded gap.
        room(l,b,-7,7,-29,-9,-5,4,material("cellar_brick"),material("cellar_floor"));
        box(l,b,-6,-5,-28,6,-2,-10,Blocks.AIR);
        box(l,b,-1,-5,-12,1,2,-6,Blocks.AIR);
        for(int i=0;i<5;i++){int z=-6-i,y=-i-1;box(l,b,-1,-6,z,1,y-1,z,Blocks.STONE_BRICKS);for(int x=-1;x<=1;x++)state(l,b,new BlockPos(x,y,z),Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));}
        for(int x:new int[]{-2,2}){box(l,b,x,-5,-12,x,0,-7,material("cellar_brick"));box(l,b,x,1,-12,x,1,-7,Blocks.OAK_FENCE);}
        for(int x:new int[]{-1,1})box(l,b,x,0,-5,x,2,-5,material("cellar_plaster"));
        box(l,b,-1,2,-5,1,2,-5,material("cellar_plaster"));
        door(l,b,CELLAR_DOOR,Direction.SOUTH,true);
        // A backed ladder reaches a real upper landing; its one-cell shaft has no side pit.
        box(l,b,-7,-5,-10,-7,1,-10,material("cellar_brick"));box(l,b,-6,-5,-10,-6,1,-10,Blocks.AIR);
        for(int y=-5;y<=0;y++)state(l,b,new BlockPos(-6,y,-10),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
        for(int z=-12;z>=-27;z--)furnishing(l,b,-6,-2,z,UncannyFurnitureBlock.Kind.PIPE_HORIZONTAL,Direction.EAST);
        for(int z:new int[]{-14,-25,-27})furnishing(l,b,6,-5,z,UncannyFurnitureBlock.Kind.SHELF,Direction.WEST);
        furnishing(l,b,-6,-5,-26,UncannyFurnitureBlock.Kind.CABINET,Direction.EAST);
        furnishing(l,b,-5,-5,-26,UncannyFurnitureBlock.Kind.DRAIN);
        furnishing(l,b,6,-3,-17,UncannyFurnitureBlock.Kind.TOOL_RACK,Direction.WEST);
        furnishing(l,b,5,-5,-13,UncannyFurnitureBlock.Kind.WORKBENCH,Direction.WEST);fit(l,b,TOOL,CLEAVER,Direction.SOUTH);
        at(l,b,-5,-5,-23,Blocks.CAULDRON);
        for(int x=-3;x<=3;x++)for(int y=-5;y<=-2;y++)for(int z=-25;z<=-17;z++)if(UncannyCellar.authored(x,y,z))state(l,b,new BlockPos(x,y,z),UncannyFleshBlock.at(new BlockPos(x,y,z)));
        at(l,b,KEY_CAVITY.getX(),KEY_CAVITY.getY(),KEY_CAVITY.getZ(),Blocks.AIR);
        for(int x:new int[]{-3,3})furnishing(l,b,x,-2,-14,UncannyFurnitureBlock.Kind.FLUORESCENT);
        furnishing(l,b,3,-2,-25,UncannyFurnitureBlock.Kind.FLUORESCENT);
        for(int z:new int[]{-5,-22})furnishing(l,b,0,3,z,UncannyFurnitureBlock.Kind.FLUORESCENT);
    }
    private static void foyer(ServerLevel l,BlockPos b){
        room(l,b,-4,4,-14,-1,0,5,material("lobby_wall"),Blocks.DARK_OAK_PLANKS);
        box(l,b,-7,-2,-25,7,-2,-15,Blocks.STONE);
        room(l,b,-7,7,-25,-15,0,7,material("courtyard_wall"),Blocks.GRAVEL);
        // An open service yard. Its one working light remains the reverse-motion floodlight.
        box(l,b,-6,7,-24,6,7,-16,Blocks.AIR);
        for(int x=-6;x<=6;x++)furnishing(l,b,x,4,-20,UncannyFurnitureBlock.Kind.PIPE_HORIZONTAL,Direction.NORTH);
        // Join the yard through its front wall; glass alone left solid masonry immediately behind it.
        box(l,b,-3,0,-15,3,3,-15,Blocks.AIR);
        box(l,b,-3,0,-14,3,3,-14,Blocks.GLASS);door(l,b,new BlockPos(0,0,-14),Direction.SOUTH,false);
        fit(l,b,INTERCOM,BUZZER,Direction.WEST);fit(l,b,source(LabyrinthPlace.MOTION_LIGHT),SCRAWL,Direction.WEST);fit(l,b,FLOOD,FLOODLIGHT,Direction.SOUTH);
        for(int z=-3;z>=-9;z--)box(l,b,5,0,z,7,3,z,Blocks.AIR);
        box(l,b,5,-1,-10,7,-1,-1,Blocks.DARK_OAK_PLANKS);box(l,b,5,4,-10,7,4,-1,material("lobby_wall"));
        box(l,b,8,0,-10,8,4,-1,material("lobby_wall"));box(l,b,5,0,-10,7,4,-10,material("lobby_wall"));
        door(l,b,HINGED,Direction.WEST,false);box(l,b,4,0,-2,7,3,0,Blocks.AIR);
        furnishing(l,b,6,0,-3,UncannyFurnitureBlock.Kind.SIDEBOARD,Direction.SOUTH);
        for(int z=-6;z>=-8;z--)furnishing(l,b,-3,0,z,UncannyFurnitureBlock.Kind.LOBBY_BENCH,Direction.EAST);
        furnishing(l,b,-3,1,-11,UncannyFurnitureBlock.Kind.MAILBOX,Direction.EAST);
        furnishing(l,b,-3,2,-11,UncannyFurnitureBlock.Kind.MAILBOX,Direction.EAST);
        furnishing(l,b,3,0,-12,UncannyFurnitureBlock.Kind.RADIATOR,Direction.WEST);
        furnishing(l,b,0,4,-5,UncannyFurnitureBlock.Kind.FLUORESCENT);
        // Warm spill at the glass keeps a silhouette readable after the yard floodlight dies.
        furnishing(l,b,0,4,-12,UncannyFurnitureBlock.Kind.FLUORESCENT);
        furnishing(l,b,-3,3,-12,UncannyFurnitureBlock.Kind.SCONCE,Direction.EAST);
        furnishing(l,b,3,3,-12,UncannyFurnitureBlock.Kind.SCONCE,Direction.WEST);
        for(int z=-18;z>=-24;z--)furnishing(l,b,-6,0,z,UncannyFurnitureBlock.Kind.DRAIN);
        for(int y=0;y<=5;y++)furnishing(l,b,-6,y,-24,UncannyFurnitureBlock.Kind.PIPE,Direction.EAST);
        furnishing(l,b,6,0,-23,UncannyFurnitureBlock.Kind.BIN,Direction.WEST);
        furnishing(l,b,6,0,-21,UncannyFurnitureBlock.Kind.BIN,Direction.WEST);
        furnishing(l,b,6,2,-24,UncannyFurnitureBlock.Kind.VENT,Direction.WEST);
    }
}
