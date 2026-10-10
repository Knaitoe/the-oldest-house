package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Finite, in-place fronts and interiors. Never stocks containers or replaces actors or story furniture. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SceneExteriors {
    public static final String STATE="scene_exteriors_0429";
    private static final List<LabyrinthPlace> SITES=List.of(LabyrinthPlace.GOATMAN,LabyrinthPlace.HOLLOWAY_CAMP,LabyrinthPlace.ZAMPANO_COURTYARD);
    private final ServerLevel l;private final BlockPos b;private final LabyrinthPlace site;
    private SceneExteriors(ServerLevel level,BlockPos base,LabyrinthPlace place){l=level;b=base;site=place;}
    public static void decorateOnce(ServerLevel l,BlockPos origin,LabyrinthPlace site){
        if(!SITES.contains(site))return;var d=LabyrinthData.get(l.getServer());String key=origin.asLong()+":"+site.id();var done=d.state(STATE);
        if(done.getBoolean(key))return;var b=LabyrinthPlaces.base(origin,site);if(b==null||l.getBlockState(b.offset(0,-1,-3)).isAir())return;
        var r=site.room();var box=new AABB(Vec3.atLowerCornerOf(b.offset(r.minX()-2,r.minY()-2,r.minZ()-2)),Vec3.atLowerCornerOf(b.offset(r.maxX()+3,r.maxY()+6,18)));
        if(l.players().stream().anyMatch(p->box.intersects(p.getBoundingBox())))return;
        var work=new SceneExteriors(l,b,site);switch(site){case DROWNED_TOWN->work.town();case GOATMAN->work.trailer();case HOLLOWAY_CAMP->work.hut();case ZAMPANO_COURTYARD->work.archive();default->{}}
        done.putBoolean(key,true);d.setState(STATE,done);
    }
    static void forget(ServerLevel l,BlockPos origin,LabyrinthPlace site){var d=LabyrinthData.get(l.getServer());var s=d.state(STATE);s.remove(origin.asLong()+":"+site.id());d.setState(STATE,s);}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(e.getServer().getTickCount()%20!=0||LabyrinthBuilder.isCarving())return;var origin=HouseSavedData.get(e.getServer()).houseOrigin();if(origin==null)return;
        var layout=LabyrinthData.get(e.getServer());if(layout.builtVersion()!=LabyrinthBuilder.VERSION||!origin.equals(layout.builtOrigin()))return;
        for(var site:SITES){var level=e.getServer().getLevel(NovelRooms.dimension(site));if(level!=null)decorateOnce(level,origin,site);}
    }
    private BlockPos p(int x,int y,int z){return b.offset(x,y,z);}
    private boolean reserved(BlockPos at){
        var v=LabyrinthPlaces.localVestibule();var rel=at.subtract(b);if(v.isInside(rel))return true;
        for(var door:site.doors())if(Math.abs(rel.getX()-door.rel().getX())<=1&&Math.abs(rel.getZ()-door.rel().getZ())<=1&&rel.getY()>=door.rel().getY()-1&&rel.getY()<=door.rel().getY()+2)return true;
        if(site==LabyrinthPlace.DROWNED_TOWN){
            for(var a:List.of(DrownedTown.FURNACE,DrownedTown.SUPPLIES,DrownedTown.KEY_DESK,DrownedTown.SCHOOL_DOOR,DrownedTown.CHURCH_DOOR,DrownedTown.ROOF_HATCH))
                if(rel.getX()==a.getX()&&rel.getZ()==a.getZ()&&Math.abs(rel.getY()-a.getY())<=2)return true;
            for(var a:DrownedTown.PAPERS)if(rel.getX()==a.getX()&&rel.getZ()==a.getZ()&&Math.abs(rel.getY()-a.getY())<=1)return true;
        }
        return false;
    }
    private boolean safe(BlockPos at,BlockState target){
        var old=l.getBlockState(at);if(reserved(at)||l.getBlockEntity(at)!=null||old.getBlock() instanceof DoorBlock||old.getBlock() instanceof TrapDoorBlock||old.getBlock() instanceof BedBlock)return false;
        if(!old.isAir()&&!net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(old.getBlock()).getNamespace().equals("minecraft")
                &&!old.is(NovelRegistry.PAPER.get())&&!(site==LabyrinthPlace.ZAMPANO_COURTYARD&&old.is(NovelRegistry.PLASTER.get()))&&!old.is(LabyrinthBuilder.SOLID.getBlock()))return false;
        if(!l.getEntitiesOfClass(Entity.class,new AABB(at)).isEmpty())return false;
        // Preserve attached original props, signs and frames, including their real backing blocks.
        for(Direction side:Direction.values()){
            var near=at.relative(side);var s=l.getBlockState(near);
            if(l.getBlockEntity(near)!=null)return false;
            if(s.is(HouseBlocks.SCENE_DETAIL.get())){
                var kind=s.getValue(SceneDetailBlock.KIND);var support=kind.wall()?near.relative(s.getValue(SceneDetailBlock.FACING).getOpposite()):near.below();
                if(support.equals(at))return false;
            }
        }
        for(var h:l.getEntitiesOfClass(HangingEntity.class,new AABB(at).inflate(2)))if(h.getPos().relative(h.getDirection().getOpposite()).equals(at))return false;
        return true;
    }
    private void set(int x,int y,int z,Block block){set(x,y,z,block.defaultBlockState());}
    private void set(int x,int y,int z,BlockState st){var at=p(x,y,z);if(safe(at,st))l.setBlock(at,st,LabyrinthBuilder.flags());}
    private void add(int x,int y,int z,Block block){add(x,y,z,block.defaultBlockState());}
    private void add(int x,int y,int z,BlockState st){if(l.getBlockState(p(x,y,z)).isAir())set(x,y,z,st);}
    private void prop(int x,int y,int z,SceneDetailBlock.Kind kind,Direction facing){var s=SceneDetailBlock.state(kind,facing);if(SceneDetailBlock.supported(l,p(x,y,z),s))add(x,y,z,s);}
    private void furniture(int x,int y,int z,HouseholdFurnitureBlock.Kind kind,Direction face){add(x,y,z,HouseholdFurnitureBlock.state(kind,face));}
    private void desk(int x,int y,int z,SceneDetailBlock.Kind kind){furniture(x,y,z,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);prop(x,y+1,z,kind,Direction.SOUTH);}
    private void cabinet(int x,int y,int z,SceneDetailBlock.Kind kind){furniture(x,y,z,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);prop(x,y+1,z,kind,Direction.SOUTH);}
    private void beamX(int x0,int x1,int y,int z,Block block){for(int x=x0;x<=x1;x++)set(x,y,z,block.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));}
    private static BlockState pane(Block block,Direction.Axis along){var st=block.defaultBlockState();return along==Direction.Axis.X
        ?st.setValue(BlockStateProperties.EAST,true).setValue(BlockStateProperties.WEST,true)
        :st.setValue(BlockStateProperties.NORTH,true).setValue(BlockStateProperties.SOUTH,true);}
    private void windowSouth(int x0,int x1,int y,int z,Block frame){
        for(int x=x0;x<=x1;x++){set(x,y-1,z,frame);for(int yy=y;yy<y+2;yy++)set(x,yy,z,pane(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,Direction.Axis.X));set(x,y+2,z,frame);}
        for(int xx:new int[]{x0-1,x1+1})for(int yy=y-1;yy<=y+2;yy++)set(xx,yy,z,frame);
        for(int x=x0;x<=x1;x++)add(x,y-1,z+1,Blocks.STONE_BRICK_SLAB);
    }
    private void town(){
        // The school reads as a civic building, with grouped tall windows and a deep entrance.
        for(int x:new int[]{-24,-21,-10,-7})windowSouth(x,x,1,-22,Blocks.POLISHED_ANDESITE);
        for(int x=-26;x<=-6;x++){set(x,4,-22,Blocks.BRICKS);set(x,5,-21,Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));}
        for(int x:new int[]{-23,-19,-11,-7})for(int z:new int[]{-26,-30}){
            // Original native desks occupy some of these positions; additions use vacant cells only.
            desk(x,0,z,SceneDetailBlock.Kind.INK_PAPERS);furniture(x,0,z+1,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        }
        cabinet(-24,0,-32,SceneDetailBlock.Kind.BOOKS);cabinet(-8,0,-32,SceneDetailBlock.Kind.FILE_TRAY);
        set(-14,2,-34,Blocks.GREEN_TERRACOTTA);set(-15,2,-34,Blocks.GREEN_TERRACOTTA);set(-13,2,-34,Blocks.GREEN_TERRACOTTA);
        // Cottage: framed sash windows, hinged shutters, a sitting room and a separate sleeping nook.
        for(int x:new int[]{-23,-17}){
            windowSouth(x,x+1,1,-45,Blocks.STRIPPED_DARK_OAK_WOOD);
            for(int edge:new int[]{x-1,x+2})for(int y=1;y<=2;y++)add(edge,y,-44,Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.OPEN,true).setValue(TrapDoorBlock.FACING,edge<x?Direction.EAST:Direction.WEST));
        }
        beamX(-25,-14,4,-45,Blocks.STRIPPED_DARK_OAK_LOG);
        for(int x=-24;x<=-15;x++)for(int y=0;y<=3;y++)if(x!=-19&&x!=-18)add(x,y,-53,y==3?Blocks.STRIPPED_SPRUCE_WOOD:Blocks.WHITE_TERRACOTTA);
        furniture(-23,0,-50,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.EAST);furniture(-16,0,-50,HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.WEST);
        desk(-21,0,-50,SceneDetailBlock.Kind.TEA_SET);cabinet(-24,0,-57,SceneDetailBlock.Kind.BLANKET);
        cabinet(-16,0,-58,SceneDetailBlock.Kind.CROCK);bed(-21,0,-58,Blocks.BROWN_BED,Direction.NORTH);
        prop(-24,2,-50,SceneDetailBlock.Kind.FRAME,Direction.EAST);prop(-15,2,-49,SceneDetailBlock.Kind.CLOCK,Direction.WEST);
        // Grocery: shop windows, a framed signboard and a counter with a clear route to its back room.
        windowSouth(-10,-9,1,-41,Blocks.STRIPPED_OAK_WOOD);windowSouth(-5,-5,1,-41,Blocks.STRIPPED_OAK_WOOD);
        beamX(-10,-5,4,-41,Blocks.STRIPPED_OAK_LOG);
        for(int z=-48;z<=-45;z++)cabinet(-10,0,z,z%2==0?SceneDetailBlock.Kind.CROCK:SceneDetailBlock.Kind.BOTTLES);
        for(int x=-9;x<=-6;x++)desk(x,0,-46,x%2==0?SceneDetailBlock.Kind.TEA_SET:SceneDetailBlock.Kind.INK_PAPERS);
        furniture(-5,0,-48,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.WEST);prop(-9,0,-48,SceneDetailBlock.Kind.FEED_SACK,Direction.NORTH);
        // Brick store: projecting sill bands, bays on both floors, and a lived-in room above the shop.
        for(int z=-60;z<=-53;z++){set(-4,3,z,Blocks.POLISHED_ANDESITE);set(-4,7,z,Blocks.STONE_BRICKS);}
        for(int z:new int[]{-59,-53})for(int y:new int[]{1,5}){
            set(-4,y-1,z,Blocks.STONE_BRICKS);set(-4,y,z,pane(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,Direction.Axis.Z));set(-4,y+1,z,pane(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,Direction.Axis.Z));set(-4,y+2,z,Blocks.STONE_BRICKS);
            add(-3,y-1,z,Blocks.STONE_BRICK_SLAB);
        }
        for(int x:new int[]{-9,-6})windowSouth(x,x,5,-52,Blocks.STONE_BRICKS);
        for(int x=-11;x<=-4;x++)set(x,8,-51,Blocks.STONE_BRICK_SLAB);
        cabinet(-5,0,-60,SceneDetailBlock.Kind.BOTTLES);cabinet(-5,0,-54,SceneDetailBlock.Kind.FILE_TRAY);
        bed(-8,4,-58,Blocks.GRAY_BED,Direction.NORTH);cabinet(-5,4,-54,SceneDetailBlock.Kind.TEA_SET);
        furniture(-7,4,-54,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.SOUTH);prop(-10,5,-60,SceneDetailBlock.Kind.COAT,Direction.EAST);
        // Low weatherboard cabin and the boat shed have deliberately different frames and fittings.
        for(int z=-18;z<=-14;z++){set(-5,0,z,Blocks.STONE_BRICKS);set(-5,3,z,Blocks.STRIPPED_SPRUCE_WOOD);}
        for(int z:new int[]{-18,-14}){set(-5,1,z,pane(Blocks.GLASS_PANE,Direction.Axis.Z));set(-5,2,z,pane(Blocks.GLASS_PANE,Direction.Axis.Z));add(-4,0,z,Blocks.SPRUCE_SLAB);}
        cabinet(-11,0,-18,SceneDetailBlock.Kind.DISH_RACK);furniture(-6,0,-18,HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.WEST);
        prop(-11,2,-15,SceneDetailBlock.Kind.CLOCK,Direction.EAST);prop(-8,0,-14,SceneDetailBlock.Kind.BLANKET,Direction.NORTH);
        for(int x=-26;x<=-20;x++)set(x,3,-13,Blocks.STRIPPED_SPRUCE_WOOD);
        for(int x:new int[]{-26,-20})for(int y=0;y<=3;y++)set(x,y,-13,Blocks.STRIPPED_SPRUCE_LOG);
        for(int z=-18;z<=-14;z++){add(-25,1,z,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));prop(-25,2,z,z%2==0?SceneDetailBlock.Kind.TOOLS:SceneDetailBlock.Kind.ROPE_COIL,Direction.SOUTH);}
        prop(-21,2,-17,SceneDetailBlock.Kind.COAT,Direction.WEST);
        // The older church remains drowned; submerged ribs and real sills sharpen its silhouette.
        for(int x:new int[]{7,11,19,23})for(int y=-11;y<=-5;y++)set(x,y,-41,Blocks.STONE_BRICKS);
        for(int x=7;x<=23;x++)set(x,-5,-40,Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.WATERLOGGED,true));
    }
    private void bed(int x,int y,int z,Block block,Direction facing){
        var foot=p(x,y,z);var head=foot.relative(facing);if(!l.getBlockState(foot).isAir()||!l.getBlockState(head).isAir()||!safe(foot,block.defaultBlockState())||!safe(head,block.defaultBlockState()))return;
        LabyrinthBuilder.bed(l,foot,facing,block);
    }
    private void trailer(){
        // Riveted, lightly weathered siding and a rounded stepped metal roof, rather than a white cube.
        for(int x=-8;x<=8;x++)for(int z=-77;z<=-55;z++)if(Math.abs(x)==8||z==-77||z==-55){
            for(int y=1;y<=5;y++){
                var old=l.getBlockState(p(x,y,z));if(old.is(Blocks.WHITE_TERRACOTTA)||old.is(Blocks.LIGHT_GRAY_TERRACOTTA))set(x,y,z,y==1?Blocks.CYAN_TERRACOTTA:y==4?Blocks.LIGHT_GRAY_CONCRETE:Blocks.WHITE_TERRACOTTA);
            }
            if(Math.abs(x)==8){set(x,5,z,Blocks.IRON_BLOCK);set(x,6,z,Blocks.SMOOTH_STONE_SLAB);}
        }
        for(int x=-7;x<=7;x++)for(int z=-77;z<=-55;z++)set(x,Math.abs(x)>5?6:7,z,Blocks.SMOOTH_STONE_SLAB);
        for(int x:new int[]{-8,8})for(int z:new int[]{-77,-55})for(int y=1;y<=5;y++)set(x,y,z,Blocks.IRON_BLOCK);
        for(int side:new int[]{-1,1})for(int z:new int[]{-59,-67,-73}){
            // 0.4.53: the east wall's last window became the bathroom's small awning window, framed by a sill alone.
            if(side>0&&z==-73)continue;
            for(int zz=z-1;zz<=z+1;zz++){add(side*9,1,zz,Blocks.SMOOTH_STONE_SLAB);add(side*9,4,zz,Blocks.SMOOTH_STONE_SLAB);}
            for(int zz:new int[]{z-1,z+1})for(int y=2;y<=3;y++)add(side*9,y,zz,Blocks.IRON_BARS.defaultBlockState().setValue(BlockStateProperties.NORTH,true).setValue(BlockStateProperties.SOUTH,true));
        }
        add(9,2,-75,Blocks.SMOOTH_STONE_SLAB);
        for(int z:new int[]{-59,-73}){
            for(int x=-9;x<=9;x++)set(x,-1,z,Blocks.POLISHED_BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
            for(int x:new int[]{-9,9}){set(x,0,z,Blocks.BLACK_CONCRETE);set(x,0,z-1,Blocks.BLACK_CONCRETE);add(x,1,z,Blocks.POLISHED_BLACKSTONE_SLAB);}
        }
        for(int x:new int[]{-6,6})for(int z:new int[]{-57,-75}){set(x,0,z,Blocks.POLISHED_ANDESITE);set(x,-1,z,Blocks.STONE_BRICKS);}
        for(int x=-3;x<=3;x++)for(int z=-54;z<=-52;z++)if(x!=0){set(x,0,z,Blocks.SPRUCE_SLAB);set(x,-1,z,Blocks.SPRUCE_LOG);}
        for(int x:new int[]{-3,3}){for(int y=1;y<=3;y++)add(x,y,-53,Blocks.SPRUCE_FENCE);}
        for(int x=-3;x<=3;x++)for(int z=-55;z<=-52;z++)add(x,4,z,Blocks.SPRUCE_SLAB);
        // The porch light, which goes out when the woods go quiet.
        add(0,3,-53,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        for(int z=-54;z<=-51;z++)add(5,0,z,Blocks.IRON_BARS.defaultBlockState().setValue(BlockStateProperties.NORTH,true).setValue(BlockStateProperties.SOUTH,true));
        add(5,-1,-51,Blocks.STONE_BRICK_WALL);prop(-4,0,-52,SceneDetailBlock.Kind.SHOES,Direction.NORTH);
        prop(6,0,-53,SceneDetailBlock.Kind.CRATE,Direction.NORTH);
    }
    private void hut(){
        // A dirt-roofed dugout with a braced entrance and a stone toe, retaining Holloway's rough shelter.
        for(int x:new int[]{-7,7})for(int z=-12;z<=0;z++){
            set(x,-1,z,Blocks.MOSSY_COBBLESTONE);set(x,0,z,Blocks.COBBLESTONE);
            for(int y=1;y<=4;y++)if(Math.floorMod(z,4)==0)set(x,y,z,Blocks.STRIPPED_SPRUCE_LOG);
        }
        for(int x:new int[]{-3,3})for(int y=0;y<=4;y++)add(x,y,0,Blocks.STRIPPED_SPRUCE_LOG);
        beamX(-7,7,5,0,Blocks.STRIPPED_SPRUCE_LOG);
        for(int z=-12;z<=0;z++)for(int x=-7;x<=7;x++){
            int height=6+Math.max(0,2-Math.abs(x)/2);set(x,height,z,Math.floorMod(x*13+z*7,5)==0?Blocks.MOSS_BLOCK:Blocks.COARSE_DIRT);
            if(Math.abs(x)==7)set(x,5,z,Blocks.SPRUCE_SLAB);
        }
        for(int x:new int[]{-5,5}){add(x,0,-1,Blocks.SPRUCE_LOG);add(x,1,-1,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));}
        // Bundle and tools stay beside the entrance, clear of his real patrol and supply barrel.
        prop(5,2,-1,SceneDetailBlock.Kind.ROPE_COIL,Direction.NORTH);prop(-5,2,-1,SceneDetailBlock.Kind.TOOLS,Direction.NORTH);
    }
    private void archive(){
        // A narrow brick/stucco archive with paired pilasters, a recessed entrance and a lead roof.
        for(int x=-12;x<=12;x++)for(int y=0;y<=6;y++){
            var old=l.getBlockState(p(x,y,-19));if(old.is(NovelRegistry.PAPER.get())||old.isCollisionShapeFullBlock(l,p(x,y,-19)))set(x,y,-19,y==0?Blocks.STONE_BRICKS:y==2?Blocks.POLISHED_ANDESITE:Blocks.LIGHT_GRAY_TERRACOTTA);
        }
        for(int x:new int[]{-12,-7,-3,3,7,12})for(int y=0;y<=6;y++){
            set(x,y,-18,y==0?Blocks.CHISELED_STONE_BRICKS:Blocks.STONE_BRICKS);set(x,7,-18,Blocks.STONE_BRICK_SLAB);
        }
        for(int x:new int[]{-10,-5,5,10})windowSouth(x,x+1,3,-19,Blocks.BRICKS);
        for(int x=-2;x<=2;x++){set(x,4,-18,Blocks.CHISELED_STONE_BRICKS);set(x,5,-18,Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));}
        for(int x=-12;x<=12;x++){set(x,7,-18,Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));set(x,8,-19,Blocks.STONE_BRICK_SLAB);}
        for(int side:new int[]{-12,12})for(int z=-38;z<=-20;z++)for(int y=0;y<=6;y++){
            var old=l.getBlockState(p(side,y,z));if(old.is(NovelRegistry.PAPER.get())||old.is(NovelRegistry.PLASTER.get()))set(side,y,z,y==0||y==6?Blocks.STONE_BRICKS:Math.floorMod(z,5)==0?Blocks.BRICKS:Blocks.LIGHT_GRAY_TERRACOTTA);
        }
        for(int x=-11;x<=11;x++)for(int z=-38;z<=-20;z++)set(x,8,z,Blocks.DEEPSLATE_TILE_SLAB);
        // Small glazed rooflights have a physical upstand; no ambient world-time change is involved.
        for(int z:new int[]{-25,-32})for(int x=-3;x<=3;x++)for(int zz=z-1;zz<=z+1;zz++)set(x,9,zz,Math.abs(x)==3||Math.abs(zz-z)==1?Blocks.POLISHED_ANDESITE:Blocks.LIGHT_GRAY_STAINED_GLASS);
        for(int x:new int[]{-8,8})add(x,0,-18,Blocks.POTTED_FERN);
    }
}
