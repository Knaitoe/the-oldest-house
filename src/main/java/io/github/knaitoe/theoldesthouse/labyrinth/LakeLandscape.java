package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** A dry lakeside town and real outdoor memories. Migrations copy native contents, never stock rewards. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LakeLandscape {
    public static final List<LabyrinthPlace> SITES=List.of(LabyrinthPlace.DROWNED_TOWN,LabyrinthPlace.SHALLOWS,LabyrinthPlace.PHONE_CANOE);
    private static final int F=LabyrinthBuilder.flags();
    private LakeLandscape(){}
    public static boolean isLake(LabyrinthPlace p){return SITES.contains(p);}
    private static void at(ServerLevel l,BlockPos b,int x,int y,int z,Block block){l.setBlock(b.offset(x,y,z),block.defaultBlockState(),F);}
    private record Cell(BlockPos position,BlockState state,CompoundTag data){}
    private static List<Cell> capture(ServerLevel l,BlockPos min,BlockPos max){
        List<Cell> cells=new ArrayList<>();for(BlockPos pos:BlockPos.betweenClosed(min,max)){
            var be=l.getBlockEntity(pos);cells.add(new Cell(pos.immutable(),l.getBlockState(pos),be==null?null:be.saveWithFullMetadata(l.registryAccess())));
        }return cells;
    }
    private static void paste(ServerLevel l,List<Cell> cells,BlockPos shift,boolean dry){
        for(Cell cell:cells){var at=cell.position().offset(shift);BlockState state=cell.state();
            if(dry){if(state.is(Blocks.WATER)||state.is(Blocks.BUBBLE_COLUMN))state=Blocks.AIR.defaultBlockState();if(state.hasProperty(BlockStateProperties.WATERLOGGED))state=state.setValue(BlockStateProperties.WATERLOGGED,false);}
            l.setBlock(at,state,F);
            var be=l.getBlockEntity(at);if(cell.data()!=null&&be!=null){var nbt=cell.data().copy();nbt.putInt("x",at.getX());nbt.putInt("y",at.getY());nbt.putInt("z",at.getZ());be.loadWithComponents(nbt,l.registryAccess());be.setChanged();}
        }
    }
    public static void liftSchool(ServerLevel l,BlockPos b){
        var cells=capture(l,b.offset(-26,-12,-35),b.offset(-6,-5,-22));paste(l,cells,new BlockPos(0,11,0),true);
        var houses=capture(l,b.offset(-25,-12,-60),b.offset(-14,-7,-45));paste(l,houses,new BlockPos(0,11,0),true);
    }
    public static void dress(ServerLevel l,BlockPos b,LabyrinthPlace site){
        var r=site.room();
        // Remove the old stage-box ceiling, moon blocks and flat walls, without removing player construction.
        for(int x=r.minX();x<=r.maxX();x++)for(int z=r.minZ();z<=0;z++)for(int y=0;y<=r.maxY();y++){
            var at=b.offset(x,y,z);var state=l.getBlockState(at);
            boolean edge=x==r.minX()||x==r.maxX()||z==r.minZ()||z==0||y==r.maxY();
            if(edge&&(state.is(Blocks.BLACK_CONCRETE)||state.is(Blocks.WHITE_CONCRETE)||state.is(Blocks.LIGHT)))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
        }
        if(site==LabyrinthPlace.DROWNED_TOWN)town(l,b);
        else{
            int shore=site==LabyrinthPlace.SHALLOWS?-14:-11;
            for(int x=r.minX()+1;x<r.maxX();x++)for(int z=shore+1;z<=-1;z++){
                if(Math.abs(x)<=2)continue;at(l,b,x,-1,z,z<shore+4?Blocks.SAND:Math.abs(x)>r.maxX()-7?Blocks.PODZOL:Blocks.COARSE_DIRT);
            }
            for(int x:new int[]{r.minX()+4,r.maxX()-4})for(int z=-5;z>shore;z-=4)tree(l,b,x,z);
        }
        // An uneven, supported forest bank hides the far and side boundaries.
        for(int x=r.minX()-4;x<=r.maxX()+4;x++)for(int z=r.minZ()-4;z<=3;z++){
            int edge=Math.max(Math.max(r.minX()+2-x,x-r.maxX()+2),Math.max(r.minZ()+2-z,z+1));
            if(edge<=0||Math.abs(x)<4&&z>=-1)continue;
            int h=Math.min(5,Math.max(0,edge/2))+Math.floorMod(x*7+z,2);
            for(int y=-3;y<=h;y++)at(l,b,x,y,z,y==h?Blocks.PODZOL:Blocks.DIRT);
            if(edge>=4)for(int y=h+1;y<=8;y++)l.setBlock(b.offset(x,y,z),Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);
        }
        for(int x:new int[]{r.minX()+1,r.maxX()-1})for(int z=-6;z>r.minZ();z-=7)tree(l,b,x,z);
        for(int x=r.minX()+4;x<r.maxX()-3;x+=7)tree(l,b,x,r.minZ()+1);
        NovelRooms.safeApproach(l,b);
    }
    private static void town(ServerLevel l,BlockPos b){
        // School and shops are dry; the older church alone remains below the reservoir.
        for(int x=-28;x<=3;x++)for(int z=-63;z<=-12;z++){
            for(int y=-12;y<=-1;y++)at(l,b,x,y,z,y==-1?(Math.abs(x)<=2?Blocks.COBBLESTONE:Blocks.COARSE_DIRT):Blocks.DIRT);
            if(x>=-3)for(int y=0;y<=5;y++)if(l.getBlockState(b.offset(x,y,z)).is(Blocks.WATER))at(l,b,x,y,z,Blocks.AIR);
        }
        // Cobbled street, shop fronts, pier, pale sloping beach and a forest to the west.
        for(int z=-12;z>=-58;z--)for(int x=-2;x<=2;x++)at(l,b,x,-1,z,Blocks.COBBLESTONE);
        for(int z=-12;z>=-59;z--)for(int x=4;x<=7;x++){
            int floor=x==4?-1:x==5?-2:-3;
            for(int y=-12;y<=floor;y++)at(l,b,x,y,z,y==floor?Blocks.SAND:Blocks.SANDSTONE);
        }
        NovelRooms.room(l,b,-11,-4,-49,-41,0,4,Blocks.OAK_PLANKS,Blocks.SPRUCE_PLANKS);
        NovelRooms.door(l,b.offset(-7,0,-41),Direction.SOUTH,Blocks.OAK_DOOR,false);
        for(int x:new int[]{-10,-9,-5})for(int y=1;y<=2;y++)at(l,b,x,y,-41,Blocks.GLASS);
        for(int z=-41;z>=-49;z--)for(int x=-12;x<=-3;x++)l.setBlock(b.offset(x,5,z),LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,x<-7?Direction.EAST:Direction.WEST),F);
        NovelRooms.furniture(l,b.offset(-9,0,-43),HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);
        for(int x=5;x<=17;x++)for(int dz=-1;dz<=1;dz++)at(l,b,x,-1,-38+dz,Blocks.SPRUCE_PLANKS);
        for(int x:new int[]{7,11,15}){for(int y=-6;y<=-1;y++)at(l,b,x,y,-37,Blocks.SPRUCE_LOG);at(l,b,x,0,-37,Blocks.SPRUCE_FENCE);}
        at(l,b,7,1,-37,Blocks.LANTERN);
        for(int z:new int[]{-17,-36,-56}){for(int y=0;y<=3;y++)at(l,b,3,y,z,Blocks.SPRUCE_FENCE);at(l,b,3,4,z,Blocks.LANTERN);}
        for(int z=-15;z>=-58;z-=7)tree(l,b,-27,z);
        // Correct the old water-refuge instructions on this single scenery lectern.
        if(l.getBlockEntity(b.offset(-3,0,-5)) instanceof LecternBlockEntity desk){desk.setBook(HouseWriting.book("At the waterline","An explorer",HouseWriting.WritingStyle.PLAIN,List.of(
            "Proofrock is on the bank. The older town is beneath Indian Lake.\n\nThe school is left along the street. Three damp essays remain on its desks.",
            "Dry the three pages in the furnace at the beach. Fuel is beside it.\n\nWhen all three are dry, leave and return. The school's key opens the church across the water.",
            "She runs across water. It is not a barrier to her. She stops at the living grass.\n\nInside the drowned church, a breath held beneath the roof can lift its hatch.")));desk.setChanged();}
    }
    private static void tree(ServerLevel l,BlockPos b,int x,int z){
        for(int y=0;y<=5;y++)at(l,b,x,y,z,Blocks.SPRUCE_LOG);
        for(int y=3;y<=8;y++)for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)if(Math.abs(dx)+Math.abs(dz)<=8-y)
            l.setBlock(b.offset(x+dx,y,z+dz),Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);
    }
    public static void upgradeWorld(MinecraftServer server,BlockPos origin){
        var data=LabyrinthData.get(server);if(data.builtVersion()<14||!origin.equals(data.builtOrigin()))return;
        var from=server.getLevel(HouseDimensions.INTERIOR);var to=server.getLevel(HouseDimensions.OUTSIDE);if(from==null||to==null)return;
        var progress=data.state("lake_landscape_0426");
        for(var site:SITES){
            if(progress.getBoolean(site.id()))continue;
            var dest=LabyrinthPlaces.base(origin,site);var old=dest.offset(0,0,4096+site.slot()*192);var r=site.room();var delta=dest.subtract(old);
            var cells=capture(from,old.offset(r.minX(),r.minY(),r.minZ()),old.offset(r.maxX(),r.maxY(),18));
            paste(to,cells,delta,false);
            if(site==LabyrinthPlace.DROWNED_TOWN)liftSchool(to,dest);
            dress(to,dest,site);LabyrinthBuilder.registerDoors(data,site,dest);
            data.remapReturns(w->w.dimension().equals(HouseDimensions.INTERIOR)&&IndianLakeRooms.bounds(old,site).contains(w.pos())?new LabyrinthData.Waypoint(HouseDimensions.OUTSIDE,w.pos().add(delta.getX(),delta.getY(),delta.getZ()),w.yaw(),w.door()):w);
            List<Entity> roots=new ArrayList<>();for(Entity e:from.getAllEntities())if(!e.isPassenger()&&IndianLakeRooms.bounds(old,site).contains(e.position()))roots.add(e);
            for(Entity e:roots){
                if(e instanceof LakeWitchEntity witch)witch.relocateLandscape(delta);
                e.changeDimension(new DimensionTransition(to,e.position().add(delta.getX(),delta.getY(),delta.getZ()),e.getDeltaMovement(),e.getYRot(),e.getXRot(),DimensionTransition.DO_NOTHING));
            }
            // Clear source only after preserving native inventories and actors in the destination.
            for(Cell cell:cells)from.setBlock(cell.position(),Blocks.AIR.defaultBlockState(),F);
            progress.putBoolean(site.id(),true);data.setState("lake_landscape_0426",progress);
        }
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){
        if(!(event.getEntity() instanceof ServerPlayer p)||!p.level().dimension().equals(HouseDimensions.INTERIOR))return;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return;var state=LabyrinthData.get(p.server).state("lake_landscape_0426");
        for(var site:SITES){var b=LabyrinthPlaces.base(origin,site);var old=b.offset(0,0,4096+site.slot()*192);if(state.getBoolean(site.id())&&IndianLakeRooms.bounds(old,site).contains(p.position())){
            var dest=p.server.getLevel(HouseDimensions.OUTSIDE);if(dest!=null)p.changeDimension(new DimensionTransition(dest,p.position().add(0,0,b.getZ()-old.getZ()),p.getDeltaMovement(),p.getYRot(),p.getXRot(),DimensionTransition.DO_NOTHING));return;
        }}
    }
}
