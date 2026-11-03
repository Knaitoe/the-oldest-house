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
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** A dry lakeside town and real outdoor memories. Migrations copy native contents, never stock rewards. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LakeLandscape {
    public static final List<LabyrinthPlace> SITES=List.of(LabyrinthPlace.DROWNED_TOWN,LabyrinthPlace.SHALLOWS,LabyrinthPlace.PHONE_CANOE);
    private static final int F=LabyrinthBuilder.flags();
    private LakeLandscape(){}
    public static boolean isLake(LabyrinthPlace p){return SITES.contains(p);}
    private static void at(ServerLevel l,BlockPos b,int x,int y,int z,Block block){l.setBlock(b.offset(x,y,z),block.defaultBlockState(),F);}
    public static void dress(ServerLevel l,BlockPos b,LabyrinthPlace site){
        var r=site.room();
        // Remove the old stage-box ceiling, moon blocks and flat walls, without removing player construction.
        for(int x=r.minX();x<=r.maxX();x++)for(int z=r.minZ();z<=0;z++)for(int y=0;y<=r.maxY();y++){
            var at=b.offset(x,y,z);var state=l.getBlockState(at);
            boolean edge=x==r.minX()||x==r.maxX()||z==r.minZ()||z==0||y==r.maxY();
            if(edge&&(state.is(Blocks.BLACK_CONCRETE)||state.is(Blocks.WHITE_CONCRETE)||state.is(Blocks.LIGHT)))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
        }
        if(site!=LabyrinthPlace.DROWNED_TOWN){
            // 0.4.280: the Shallows and the phone's lake are Indian Lake, in Idaho, and get the banks of a mountain lake.
            idaho(l,b,site);NovelRooms.safeApproach(l,b);return;
        }
        town(l,b);
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
        LakeSettlement.decorateOnce(l,b,site);
    }
    private static void at(ServerLevel l,BlockPos b,int x,int y,int z,BlockState state){l.setBlock(b.offset(x,y,z),state,F);}
    /** Where a memory lake's near bank (the beach the story is told from) gives way to the water. */
    private static int shore(LabyrinthPlace site){return site==LabyrinthPlace.PHONE_CANOE?-11:-14;}
    /**
     * 0.4.280: how far a column of a memory lake lies beyond the water's edge (positive is bank, zero or less is lake or the level
     * beach). The open water is kept narrower than the room, with rounded far corners, and its edge wanders a block or two. For the
     * Shallows it leaves the girl's whole flight and the drowned steeple in the water; for the phone, the steeple, the canoe's lane
     * and most of the lake.
     */
    static double reach(LabyrinthPlace site,int x,int z){
        boolean phone=site==LabyrinthPlace.PHONE_CANOE;int half=phone?20:13,far=phone?-53:-33;
        double dx=Math.abs(x)-half,dz=far-z;double d=dx>0&&dz>0?Math.hypot(dx,dz):Math.max(dx,dz);
        d+=Math.sin(z*.23+x*.13+(x>0?1.9:0))+.5*Math.sin(x*.31-z*.17);
        // The two ways on stand in level clearings at the foot of their banks.
        if(phone?Math.abs(x+23)<=2&&z>=-3:x>=14&&x<=18&&z>=-12&&z<=-8)return Math.min(d,0);
        return d;
    }
    /** The height of a memory lake's bank column: none for the lake and the level beach, zero for the shore at the water. */
    static int bank(LabyrinthPlace site,int x,int z){
        var r=site.room();int shore=shore(site);double d=reach(site,x,z);if(d<=0)return Integer.MIN_VALUE;
        double h=Math.min(6,d*.8+d*d*.08);
        // Past the room the slope steepens into the woods; along the beach the side banks stand lower.
        int out=Math.max(Math.max(r.minX()-x,x-r.maxX()),r.minZ()-z);if(out>0)h=Math.min(9,h+out*1.5);
        if(z>shore){h*=.35+.65*Math.min(1,z/(double)shore);if(Math.round(h)<1)return Integer.MIN_VALUE;}
        return (int)Math.round(h);
    }
    /** Sand where reeds stand, gravel and sand elsewhere along the water. */
    private static BlockState shoreTop(int x,int z,boolean reeds){return reeds?Blocks.SAND.defaultBlockState():IdahoShore.shore(x,z);}
    /** Whether a memory lake's story needs a cell left as it is: the Shallows' flight, landing and run north into the woods; the phone's dock and canoe lane, and the steeple. */
    private static boolean kept(LabyrinthPlace site,int x,int z){
        if(site==LabyrinthPlace.SHALLOWS)return x>=-8&&x<=13&&z<=-12&&z>=-41;
        return Math.abs(x)<=4&&z<=-1||Math.abs(x)<=5&&z<=-31&&z>=-43;
    }
    /**
     * 0.4.280: the banks of the Shallows and of the phone's lake as an Idaho mountain lake's: a level beach of gravel, sand and needles
     * where the story is told; side banks and a far bank of needles, duff and granite climbing steeply into lodgepole pine, Douglas
     * and subalpine fir and aspen; a gravel shore with reeds, granite boulders at the water and a pine fallen into the lake. The open
     * water, the beach's spots, the steeple and both ways on are where they were.
     */
    private static void idaho(ServerLevel l,BlockPos b,LabyrinthPlace site){
        var r=site.room();int shore=shore(site);boolean phone=site==LabyrinthPlace.PHONE_CANOE;
        // The old shade trees (a trunk and a bar of leaves) and the reeds standing on open water go.
        for(int x=r.minX();x<=r.maxX();x++)for(int z=r.minZ();z<=-1;z++)for(int y=0;y<=6;y++){
            var s=l.getBlockState(b.offset(x,y,z));if(s.is(Blocks.DARK_OAK_LOG)||s.is(Blocks.DARK_OAK_LEAVES)||s.is(Blocks.SUGAR_CANE))at(l,b,x,y,z,Blocks.AIR);
        }
        // The beach: gravel and sand at the water, needles and grass behind; the path to the door is left as it is.
        for(int x=r.minX();x<=r.maxX();x++)for(int z=shore+1;z<=-1;z++){
            if(Math.abs(x)<=2)continue;at(l,b,x,-1,z,z<shore+4?IdahoShore.shore(x,z):IdahoShore.floor(x,z));
        }
        // The row before the doorway was the old stage box's black floor: the path (or the jetty) runs on to the threshold.
        for(int x=-3;x<=3;x++)if(l.getBlockState(b.offset(x,-1,0)).is(Blocks.BLACK_CONCRETE))
            at(l,b,x,-1,0,Math.abs(x)==3?IdahoShore.floor(x,0):(phone&&Math.abs(x)<=1?Blocks.DARK_OAK_PLANKS:Blocks.COARSE_DIRT).defaultBlockState());
        // The banks, from the water's edge up into the woods, and on past the room so no edge shows.
        for(int x=r.minX()-4;x<=r.maxX()+4;x++)for(int z=r.minZ()-4;z<=-1;z++){
            int h=bank(site,x,z);if(h==Integer.MIN_VALUE||Math.abs(x)<=2&&z>shore)continue;
            boolean steep=false;
            for(int[] n:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int o=bank(site,x+n[0],z+n[1]);if(Math.abs((o==Integer.MIN_VALUE?0:o)-h)>=2)steep=true;}
            boolean reeds=h==0&&!kept(site,x,z)&&IdahoShore.hash(x,13,z)%4==0;
            for(int y=-4;y<h-1;y++)at(l,b,x,y,z,y<h-3||steep?IdahoShore.rock(x,y,z):Blocks.DIRT.defaultBlockState());
            at(l,b,x,h-1,z,h==0?shoreTop(x,z,reeds):steep?IdahoShore.rock(x,h,z):IdahoShore.floor(x,z));
            for(int y=h;y<=10;y++){var s=l.getBlockState(b.offset(x,y,z));if(!s.isAir()&&(s.getFluidState().isEmpty()?s.is(Blocks.BLACK_CONCRETE)||s.is(Blocks.WHITE_CONCRETE)||s.is(Blocks.DEEPSLATE):true))at(l,b,x,y,z,Blocks.AIR);}
            if(reeds){int tall=1+IdahoShore.hash(x,14,z)%2;for(int y=0;y<tall;y++)at(l,b,x,y,z,Blocks.SUGAR_CANE);}
        }
        // Behind the beach, on either side of the doorway, the ground rises into the same woods; the phone's way on is a root cellar
        // dug into that rise.
        for(int x=r.minX()-4;x<=r.maxX()+4;x++)for(int z=0;z<=3;z++){
            if(Math.abs(x)<4)continue;int side=bank(site,x,-1);int h=Math.min(6,Math.max(side==Integer.MIN_VALUE?0:side,z+1));
            if(phone&&Math.abs(x+23)<=2)h=Math.max(h,3);
            for(int y=-3;y<h-1;y++)at(l,b,x,y,z,y<h-4?IdahoShore.rock(x,y,z):Blocks.DIRT.defaultBlockState());at(l,b,x,h-1,z,IdahoShore.floor(x,z));
        }
        // Granite at the water: boulders along the shore and a few breaking the surface just off it.
        for(int x=r.minX()-1;x<=r.maxX()+1;x++)for(int z=r.minZ()-1;z<=shore;z++){
            if(kept(site,x,z))continue;int h=bank(site,x,z);int k=IdahoShore.hash(x,15,z)%23;double d=reach(site,x,z);
            if(h!=Integer.MIN_VALUE&&h<=2&&k<2&&l.getBlockState(b.offset(x,h,z)).isAir())IdahoShore.boulder(l,b,x,h,z,1+k+(h==0?1:0));
            else if(h==Integer.MIN_VALUE&&d>-1.6&&k==3){at(l,b,x,-1,z,IdahoShore.rock(x,-1,z));at(l,b,x,-2,z,IdahoShore.rock(x,-2,z));if(IdahoShore.hash(x,16,z)%2==0)at(l,b,x,0,z,IdahoShore.rock(x,0,z));}
        }
        // A pine long fallen from the bank, its top in the water.
        int fz=phone?-30:-24,from=phone?24:-17,to=phone?17:-10;
        for(int x=Math.min(from,to);x<=Math.max(from,to);x++){int h=bank(site,x,fz);int y=h==Integer.MIN_VALUE||h<1?-1:h;
            if(y<=1)at(l,b,x,y,fz,Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));}
        // The woods: lodgepole, Douglas fir and subalpine fir up the slopes, aspen down by the water. Nothing grows on the beach's
        // spots, across the girl's way into the woods, by the doors or within reach of the water.
        for(int x=r.minX()-4;x<=r.maxX()+4;x+=3)for(int z=r.minZ()-4;z<=-1;z+=3){
            int hh=IdahoShore.hash(x,17,z);int tx=x+hh%3-1,tz=Math.min(-1,z+(hh/3)%3-1);
            int h=bank(site,tx,tz);double d=reach(site,tx,tz);
            if(h==Integer.MIN_VALUE||h<1||d<1.6||kept(site,tx,tz)||hh%7==0)continue;
            if(phone?Math.abs(tx+23)<=3&&tz>=-5:tx>=13&&tz>=-13&&tz<=-7)continue;
            if(!l.getBlockState(b.offset(tx,h,tz)).isAir())continue;
            IdahoShore.tree(l,b,tx,h,tz,16,d<3.5);
        }
        // Two pines on the beach, for shade over the boys' spots and the jetty.
        if(phone){IdahoShore.tree(l,b,-17,0,-5,16,true);IdahoShore.tree(l,b,16,0,-7,16,true);}
        else{IdahoShore.tree(l,b,-10,0,-2,16,true);IdahoShore.tree(l,b,9,0,-3,16,true);}
        // Grass, fern and moss under the trees.
        for(int x=r.minX()-4;x<=r.maxX()+4;x++)for(int z=r.minZ()-4;z<=-1;z++){
            int h=bank(site,x,z);if(h==Integer.MIN_VALUE||h<1||!l.getBlockState(b.offset(x,h,z)).isAir())continue;int k=IdahoShore.hash(x,18,z)%9;
            var top=l.getBlockState(b.offset(x,h-1,z));
            if(top.is(Blocks.MOSS_BLOCK)&&k<4)at(l,b,x,h,z,Blocks.MOSS_CARPET);
            else if((top.is(Blocks.GRASS_BLOCK)||top.is(Blocks.PODZOL))&&k<2)at(l,b,x,h,z,k==0?Blocks.FERN:Blocks.SHORT_GRASS);
        }
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
    @SubscribeEvent public static void night(ServerTickEvent.Post event){
        if(event.getServer().getTickCount()%20!=0)return;
        var origin=HouseSavedData.get(event.getServer()).houseOrigin();var outside=event.getServer().getLevel(HouseDimensions.OUTSIDE);
        // A lake's woods are settled as it is built; this finishes one whose entity sections or cameras made the build's own pass wait.
        if(origin!=null&&outside!=null)for(var site:SITES)LakeSettlement.decorateOnce(outside,LabyrinthPlaces.base(origin,site),site);
        for(ServerPlayer p:event.getServer().getPlayerList().getPlayers())for(var site:SITES)if(IndianLakeRooms.inside(p,site)){
            io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new io.github.knaitoe.theoldesthouse.network.NovelScenePayload(7,0,"",0,0));break;
        }
    }
}
