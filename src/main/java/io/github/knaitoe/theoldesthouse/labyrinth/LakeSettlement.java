package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import java.util.*;

/** Authored lakeside streets and an irregular forest. Original puzzle blocks and inventories stay in place. */
public final class LakeSettlement {
    public static final String STATE="lakeside_architecture_0427";
    private static final int F=LabyrinthBuilder.flags();
    private final ServerLevel l;private final BlockPos b;private final LabyrinthPlace site;
    private LakeSettlement(ServerLevel l,BlockPos b,LabyrinthPlace site){this.l=l;this.b=b;this.site=site;}
    public static void decorateOnce(ServerLevel l,BlockPos b,LabyrinthPlace site){
        // 0.4.67: Proofrock authors its own streets and woods.
        if(!LakeLandscape.isLake(site)||site==LabyrinthPlace.DROWNED_TOWN||l.getBlockState(b.offset(0,-1,-3)).isAir())return;
        if(site==LabyrinthPlace.DROWNED_TOWN&&IndianLakeRooms.visitors(l,b,site).isEmpty())townSign(l,b);
        var data=LabyrinthData.get(l.getServer());var state=data.state(STATE);String key=b.asLong()+":"+site.id();
        if(state.getBoolean(key))return;
        if(!IndianLakeRooms.visitors(l,b,site).isEmpty())return;
        var scene=new LakeSettlement(l,b,site);scene.removeRegularTrees();
        if(site==LabyrinthPlace.DROWNED_TOWN)scene.town();scene.forest();
        state.putBoolean(key,true);data.setState(STATE,state);
    }
    private static void townSign(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());var state=d.state(STATE);String key="Sign:"+b.asLong();if(state.getBoolean(key))return;
        BlockPos at=b.offset(-3,0,-10);var old=l.getBlockState(at);if(old.getBlock() instanceof SignBlock||old.isAir())l.setBlock(at,HouseBlocks.TOWN_SIGN.get().defaultBlockState(),F);
        state.putBoolean(key,true);d.setState(STATE,state);
    }
    public static void forget(ServerLevel l,BlockPos b,LabyrinthPlace site){var d=LabyrinthData.get(l.getServer());var s=d.state(STATE);s.remove(b.asLong()+":"+site.id());d.setState(STATE,s);}
    private BlockPos p(int x,int y,int z){return b.offset(x,y,z);}
    private boolean protectedAt(int x,int y,int z){
        if(l.getBlockEntity(p(x,y,z))!=null)return true;
        if(site!=LabyrinthPlace.DROWNED_TOWN)return false;
        for(var anchor:List.of(DrownedTown.FURNACE,DrownedTown.SUPPLIES,DrownedTown.KEY_DESK,DrownedTown.SCHOOL_DOOR,DrownedTown.CHURCH_DOOR,DrownedTown.ROOF_HATCH))
            if(x==anchor.getX()&&z==anchor.getZ()&&y>=anchor.getY()-1&&y<=anchor.getY()+2)return true;
        for(var paper:DrownedTown.PAPERS)if(x==paper.getX()&&z==paper.getZ()&&Math.abs(y-paper.getY())<=1)return true;
        return (y==-1&&DrownedTownArchitecture.isGrassPatch(x,z))||(x==0&&z>=-3&&y>=-1&&y<=2);
    }
    private void put(int x,int y,int z,Block block){put(x,y,z,block.defaultBlockState());}
    private void put(int x,int y,int z,BlockState state){
        BlockPos at=p(x,y,z);if(protectedAt(x,y,z))return;
        var old=l.getBlockState(at);
        // Edits are limited to native authored shell/scenery. Player tools, custom blocks and original doors survive.
        if(old.getBlock() instanceof DoorBlock||old.getBlock() instanceof TrapDoorBlock||old.is(HouseBlocks.SCENE_DETAIL.get())||old.is(HouseBlocks.HOUSEHOLD_FURNITURE.get()))return;
        if(!old.isAir()&&!net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(old.getBlock()).getNamespace().equals("minecraft"))return;
        if(!state.getCollisionShape(l,at).isEmpty()&&!l.getEntitiesOfClass(LivingEntity.class,new AABB(at)).isEmpty())return;
        l.setBlock(at,state,F);
    }
    private void add(int x,int y,int z,Block block){if(l.getBlockState(p(x,y,z)).isAir())put(x,y,z,block);}
    private void prop(int x,int y,int z,SceneDetailBlock.Kind kind){
        var s=SceneDetailBlock.state(kind,Direction.SOUTH);if(l.getBlockState(p(x,y,z)).isAir()&&SceneDetailBlock.supported(l,p(x,y,z),s))l.setBlock(p(x,y,z),s,F);
    }
    private void table(int x,int y,int z,SceneDetailBlock.Kind kind){
        if(l.getBlockState(p(x,y,z)).isAir()){l.setBlock(p(x,y,z),HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH),F);prop(x,y+1,z,kind);}
    }
    private void removeRegularTrees(){
        var r=site.room();List<BlockPos> roots=new ArrayList<>();
        for(int x:new int[]{r.minX()+1,r.maxX()-1})for(int z=-6;z>r.minZ();z-=7)roots.add(new BlockPos(x,0,z));
        for(int x=r.minX()+4;x<r.maxX()-3;x+=7)roots.add(new BlockPos(x,0,r.minZ()+1));
        if(site==LabyrinthPlace.DROWNED_TOWN){
            for(int z=-15;z>=-58;z-=7)roots.add(new BlockPos(-27,0,z));
            for(int x=-27;x<=27;x+=6)if(Math.abs(x)>=4)roots.add(new BlockPos(x,0,-1));
        }else{
            int shore=site==LabyrinthPlace.SHALLOWS?-14:-11;
            for(int x:new int[]{r.minX()+4,r.maxX()-4})for(int z=-5;z>shore;z-=4)roots.add(new BlockPos(x,0,z));
            if(site==LabyrinthPlace.SHALLOWS)for(int x:new int[]{-15,-10,-5,5,10,15})roots.add(new BlockPos(x,0,-2));
        }
        for(var root:roots)for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)for(int y=0;y<=9;y++){
            var at=p(root.getX()+dx,y,root.getZ()+dz);var s=l.getBlockState(at);
            if(s.getBlock() instanceof LeavesBlock||s.is(Blocks.SPRUCE_LOG)||s.is(Blocks.DARK_OAK_LOG))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
        }
        // The previous perimeter was a solid leaf curtain. Keep its earth bank, replace the curtain with crowns.
        for(int x=r.minX()-4;x<=r.maxX()+4;x++)for(int z=r.minZ()-4;z<=3;z++){
            int edge=Math.max(Math.max(r.minX()+2-x,x-r.maxX()+2),Math.max(r.minZ()+2-z,z+1));
            if(edge<4)continue;
            for(int y=0;y<=9;y++)if(l.getBlockState(p(x,y,z)).getBlock() instanceof LeavesBlock)l.setBlock(p(x,y,z),Blocks.AIR.defaultBlockState(),F);
        }
    }
    private void town(){
        streets();school();cottage();market();
        // A taller brick corner store, a low weatherboard cabin and a copper-roofed boat shed.
        building(-11,-4,-61,-52,7,Blocks.BRICKS,Blocks.LIGHT_GRAY_TERRACOTTA,Blocks.DEEPSLATE_TILE_STAIRS,Direction.EAST);
        building(-12,-5,-19,-13,3,Blocks.STRIPPED_SPRUCE_WOOD,Blocks.WHITE_TERRACOTTA,Blocks.DARK_OAK_STAIRS,Direction.EAST);
        building(-26,-20,-19,-13,3,Blocks.COBBLESTONE,Blocks.SPRUCE_PLANKS,Blocks.OXIDIZED_CUT_COPPER_STAIRS,Direction.SOUTH);
        table(-9,0,-58,SceneDetailBlock.Kind.FILE_TRAY);table(-7,0,-57,SceneDetailBlock.Kind.BOTTLES);
        table(-10,0,-17,SceneDetailBlock.Kind.TEA_SET);prop(-8,0,-17,SceneDetailBlock.Kind.SHOES);
        table(-24,0,-17,SceneDetailBlock.Kind.TOOLS);prop(-23,0,-15,SceneDetailBlock.Kind.ROPE_COIL);prop(-25,0,-16,SceneDetailBlock.Kind.CRATE);
        // Real stairs reach the corner store's upstairs room; the opening clears its ceiling.
        for(int i=0;i<4;i++){
            put(-10,i,-54-i,LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,Direction.NORTH));
            for(int y=i+1;y<=5;y++)put(-10,y,-54-i,Blocks.AIR);
        }
        table(-6,4,-59,SceneDetailBlock.Kind.BOOKS);prop(-8,4,-60,SceneDetailBlock.Kind.BLANKET);
        beach();waterfront();
    }
    private void streets(){
        for(int z=-12;z>=-62;z--)for(int x=-3;x<=3;x++){
            Block floor=Math.abs(x)==3?Blocks.STONE_BRICKS:Math.floorMod(x*11+z*7,13)==0?Blocks.MOSSY_COBBLESTONE:Blocks.COBBLESTONE;
            put(x,-1,z,floor);
        }
        for(int z:new int[]{-38,-50,-21})for(int x=-27;x<=3;x++)for(int dz=-1;dz<=1;dz++){
            if(z==-21&&x>-18&&x<-12)continue;
            put(x,-1,z+dz,Math.abs(dz)==1?Blocks.STONE_BRICKS:Blocks.GRAVEL);
        }
        for(int z:new int[]{-14,-31,-51,-61}){
            for(int y=0;y<=3;y++)put(3,y,z,Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH,true).setValue(IronBarsBlock.SOUTH,true));
            put(3,4,z,Blocks.LANTERN);put(2,4,z,Blocks.SPRUCE_FENCE);
        }
        for(int z:new int[]{-28,-45}){put(-3,0,z,Blocks.FLOWER_POT);prop(-4,0,z+1,SceneDetailBlock.Kind.CRATE);}
    }
    private void school(){
        // Existing classrooms, the three original desks, the key and the actual school door remain fixed.
        for(int x=-26;x<=-6;x++){
            put(x,-1,-22,Blocks.STONE_BRICKS);put(x,5,-22,Blocks.POLISHED_ANDESITE);put(x,6,-22,Blocks.STONE_BRICK_SLAB);
        }
        for(int x:new int[]{-26,-22,-18,-12,-8,-6})for(int y=0;y<=4;y++)put(x,y,-22,y==0?Blocks.STONE_BRICKS:Blocks.POLISHED_ANDESITE);
        for(int z=-34;z<=-23;z++){put(-26,-1,z,Blocks.STONE_BRICKS);put(-6,-1,z,Blocks.STONE_BRICKS);}
        for(int z:new int[]{-26,-31})for(int y=2;y<=3;y++){put(-6,y,z,Blocks.GLASS_PANE);put(-26,y,z,Blocks.GLASS_PANE);}
        // A broad hipped slate roof with a smaller front gable and a columned entrance.
        for(int ring=0;ring<=4;ring++){
            int x0=-27+ring,x1=-5-ring,z0=-36+ring,z1=-21-ring,y=7+ring;
            for(int z=z0;z<=z1;z++){put(x0,y,z,LabyrinthBuilder.stairs(Blocks.DEEPSLATE_TILE_STAIRS,Direction.EAST));put(x1,y,z,LabyrinthBuilder.stairs(Blocks.DEEPSLATE_TILE_STAIRS,Direction.WEST));}
            for(int x=x0+1;x<x1;x++){put(x,y,z0,LabyrinthBuilder.stairs(Blocks.DEEPSLATE_TILE_STAIRS,Direction.SOUTH));put(x,y,z1,LabyrinthBuilder.stairs(Blocks.DEEPSLATE_TILE_STAIRS,Direction.NORTH));}
        }
        for(int x=-22;x<=-10;x++)for(int z=-31;z<=-26;z++)put(x,11,z,Blocks.DEEPSLATE_TILE_SLAB);
        for(int x=-18;x<=-12;x++)for(int z=-21;z<=-20;z++){
            put(x,-1,z,Blocks.STONE_BRICKS);put(x,4,z,Blocks.STONE_BRICK_SLAB);
        }
        for(int x:new int[]{-18,-12})for(int y=0;y<=3;y++)put(x,y,-20,Blocks.STONE_BRICK_WALL);
        for(int i=0;i<4;i++)for(int z=-23;z<=-20;z++){
            put(-19+i,7+i,z,LabyrinthBuilder.stairs(Blocks.DEEPSLATE_TILE_STAIRS,Direction.EAST));
            put(-11-i,7+i,z,LabyrinthBuilder.stairs(Blocks.DEEPSLATE_TILE_STAIRS,Direction.WEST));
        }
        put(-15,7,-21,Blocks.CHISELED_STONE_BRICKS);
    }
    private void cottage(){
        for(int x=-25;x<=-14;x++){
            put(x,0,-45,Blocks.MOSSY_COBBLESTONE);for(int y=1;y<=3;y++)put(x,y,-45,Blocks.WHITE_TERRACOTTA);
        }
        for(int x:new int[]{-25,-20,-14})for(int y=0;y<=4;y++)put(x,y,-45,Blocks.STRIPPED_DARK_OAK_WOOD);
        for(int x:new int[]{-23,-17})for(int y=1;y<=2;y++){put(x,y,-45,Blocks.GLASS_PANE);put(x+1,y,-45,Blocks.GLASS_PANE);}
        pitchedRoof(-26,-13,-61,-44,5,Blocks.SPRUCE_STAIRS);
        for(int x=-24;x<=-17;x++)for(int z=-44;z<=-42;z++){put(x,-1,z,Blocks.SPRUCE_PLANKS);put(x,3,z,Blocks.SPRUCE_SLAB);}
        for(int x:new int[]{-24,-17})for(int y=0;y<3;y++)put(x,y,-42,Blocks.SPRUCE_FENCE);
        for(int y=5;y<=10;y++)put(-23,y,-56,Blocks.BRICKS);put(-23,11,-56,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false));
        table(-23,0,-48,SceneDetailBlock.Kind.DISH_RACK);table(-16,0,-55,SceneDetailBlock.Kind.TEA_SET);prop(-22,0,-43,SceneDetailBlock.Kind.SHOES);
        put(-20,3,-43,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
    }
    private void market(){
        for(int x=-11;x<=-4;x++)for(int y=0;y<=3;y++){
            if(x==-7&&y<2)continue;put(x,y,-41,y==0?Blocks.BRICKS:Blocks.STRIPPED_OAK_WOOD);
        }
        for(int x:new int[]{-10,-9,-5})for(int y=1;y<=2;y++)put(x,y,-41,Blocks.GLASS_PANE);
        pitchedRoof(-12,-3,-50,-40,5,Blocks.DARK_PRISMARINE_STAIRS);
        for(int x=-11;x<=-4;x++)put(x,3,-40,x%3==0?Blocks.BLUE_WOOL:Blocks.WHITE_WOOL);
        for(int x:new int[]{-11,-4})for(int y=0;y<3;y++)put(x,y,-40,Blocks.SPRUCE_FENCE);
        prop(-10,0,-39,SceneDetailBlock.Kind.CRATE);prop(-9,0,-39,SceneDetailBlock.Kind.FEED_SACK);table(-5,0,-44,SceneDetailBlock.Kind.BOTTLES);
    }
    private void building(int x0,int x1,int z0,int z1,int top,Block foot,Block wall,Block roof,Direction facing){
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            put(x,-1,z,Blocks.SPRUCE_PLANKS);
            for(int y=0;y<=top;y++)if(x==x0||x==x1||z==z0||z==z1)put(x,y,z,y==0?foot:wall);
            put(x,top+1,z,Blocks.SPRUCE_PLANKS);if(top>4)put(x,3,z,Blocks.SPRUCE_PLANKS);
        }
        for(int x:new int[]{x0,x1})for(int z:new int[]{z0,z1})for(int y=0;y<=top;y++)put(x,y,z,Blocks.STRIPPED_SPRUCE_WOOD);
        int doorX=facing==Direction.EAST?x1:(x0+x1)/2,doorZ=facing==Direction.EAST?(z0+z1)/2:z1;
        put(doorX,0,doorZ,Blocks.AIR);put(doorX,1,doorZ,Blocks.AIR);
        if(l.getBlockState(p(doorX,0,doorZ)).isAir())NovelRooms.door(l,p(doorX,0,doorZ),facing,Blocks.SPRUCE_DOOR,false);
        for(int z=z0+2;z<z1;z+=4)for(int y:new int[]{1,2,5,6})if(y<top&&(z!=doorZ||y>2))put(x1,y,z,Blocks.GLASS_PANE);
        for(int x=x0+2;x<x1;x+=3)for(int y=1;y<=2;y++)if(x!=doorX)put(x,y,z1,Blocks.GLASS_PANE);
        pitchedRoof(x0-1,x1+1,z0-1,z1+1,top+2,roof);
        put(x0+2,top+2,z0+2,Blocks.BRICKS);put(x0+2,top+3,z0+2,Blocks.BRICKS);
    }
    private void pitchedRoof(int x0,int x1,int z0,int z1,int bottom,Block stairs){
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            int rise=Math.min(x-x0,x1-x)/2;Direction facing=x<(x0+x1)/2.?Direction.EAST:Direction.WEST;
            put(x,bottom+rise,z,LabyrinthBuilder.stairs(stairs,facing));
            if(rise>0&&(z==z0+1||z==z1-1))for(int y=bottom;y<bottom+rise;y++)put(x,y,z,Blocks.SPRUCE_PLANKS);
        }
    }
    public static int shoreline(int z){return z<=-40?4:5+(int)Math.round(Math.sin(z*.22)*1.4+Math.sin(z*.57)*.7);}
    private void beach(){
        for(int z=-12;z>=-60;z--){int coast=Math.max(4,shoreline(z)),width=z<=-40?3:4+Math.floorMod(z,3);
            for(int x=4;x<=coast+width;x++){
                int depth=x<=coast?-1:-1-(x-coast);depth=Math.max(-6,depth);
                for(int y=-12;y<=-1;y++){
                    if(x>=7&&z<=-40&&y<=-3)continue;
                    put(x,y,z,y<=depth?(y==depth?(Math.floorMod(x*17+z*11,9)==0?Blocks.GRAVEL:Blocks.SAND):Blocks.SANDSTONE):(y<=-1?Blocks.WATER:Blocks.AIR));
                }
            }
        }
        for(int x=5;x<=27;x++)for(int z=-10;z<=-6;z++)if(!DrownedTownArchitecture.isGrassPatch(x,z))put(x,-1,z,Math.floorMod(x*7+z*13,8)==0?Blocks.GRAVEL:Blocks.SAND);
        for(int z:new int[]{-16,-24,-32,-47,-57}){
            int x=Math.max(4,shoreline(z));add(x,0,z,Blocks.DEAD_BUSH);prop(x+1,0,z+1,SceneDetailBlock.Kind.ROPE_COIL);
        }
        // Driftwood and stones gather above the waterline, rather than in a repeating grid.
        for(int[] at:new int[][]{{7,-17},{5,-27},{9,-11},{25,-8}}){
            put(at[0],0,at[1],Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
            add(at[0]+1,0,at[1],Blocks.MOSSY_COBBLESTONE_SLAB);
        }
    }
    private void waterfront(){
        // An L-shaped landing, working pilings and a covered fishing bench.
        for(int x=6;x<=17;x++)for(int z=-39;z<=-37;z++)put(x,-1,z,Blocks.SPRUCE_PLANKS);
        for(int x=15;x<=17;x++)for(int z=-36;z<=-32;z++)put(x,-1,z,Blocks.SPRUCE_PLANKS);
        for(int[] at:new int[][]{{8,-39},{13,-39},{17,-39},{17,-33}})for(int y=-6;y<=0;y++)put(at[0],y,at[1],Blocks.SPRUCE_LOG);
        prop(16,0,-36,SceneDetailBlock.Kind.ROPE_COIL);prop(15,0,-38,SceneDetailBlock.Kind.CRATE);
        table(6,0,-34,SceneDetailBlock.Kind.TOOLS);
        for(int x:new int[]{5,7})for(int y=0;y<=2;y++)put(x,y,-34,Blocks.SPRUCE_FENCE);
        for(int x=5;x<=7;x++)put(x,3,-34,Blocks.SPRUCE_SLAB);
        // A grounded shore shelter gives the furnace and supplies a readable destination.
        for(int x=12;x<=15;x++)for(int z=-9;z<=-7;z++)put(x,3,z,Blocks.SPRUCE_SLAB);
        for(int[] at:new int[][]{{12,-9},{15,-9}})for(int y=0;y<=2;y++)put(at[0],y,at[1],Blocks.SPRUCE_FENCE);
        prop(11,0,-8,SceneDetailBlock.Kind.CRATE);prop(16,0,-7,SceneDetailBlock.Kind.SATCHEL);
    }
    private void forest(){
        var r=site.room();
        for(int side:new int[]{-1,1}){
            int z=-5,index=0;
            while(z>r.minZ()-2){int seed=Math.floorMod(z*73+side*181,997);int x=side<0?r.minX()-1+seed%3:r.maxX()-1+seed%4;
                if(Math.abs(x)>5)tree(x,z,seed%4,4+seed%6,(seed%3)-1,((seed/3)%3)-1);
                z-=5+seed%6;index++;
            }
        }
        for(int x=r.minX()+1;x<=r.maxX();){int seed=Math.floorMod(x*67+331,991);tree(x,r.minZ()-1-seed%3,seed%4,5+seed%5,(seed%3)-1,0);x+=6+seed%5;}
        if(site==LabyrinthPlace.DROWNED_TOWN){tree(-28,-14,1,6,1,0);tree(-28,-39,0,9,0,-1);tree(27,-16,2,5,-1,0);tree(24,-4,3,5,1,0);}
        else{tree(r.minX()+5,-4,2,6,1,0);tree(r.maxX()-6,-6,1,5,-1,1);}
    }
    private void tree(int x,int z,int type,int height,int leanX,int leanZ){
        int ground=-1;for(int y=8;y>=-2;y--){var s=l.getBlockState(p(x,y,z));if(s.is(Blocks.PODZOL)||s.is(Blocks.DIRT)||s.is(Blocks.COARSE_DIRT)||s.is(Blocks.GRASS_BLOCK)||s.is(Blocks.SAND)){ground=y;break;}}
        Block log=type==2?Blocks.BIRCH_LOG:type==1?Blocks.OAK_LOG:Blocks.SPRUCE_LOG;
        Block leaves=type==2?Blocks.BIRCH_LEAVES:type==1?Blocks.OAK_LEAVES:Blocks.SPRUCE_LEAVES;
        for(int y=1;y<=height;y++){
            int dx=y>height/2?leanX:0,dz=y>height/2?leanZ:0;
            add(x+dx,ground+y,z+dz,log);
        }
        if(leanX!=0)add(x+leanX,ground+height/2,z,log);if(leanZ!=0)add(x,ground+height/2,z+leanZ,log);
        if(type==3){add(x+1,ground+height-1,z,Blocks.SPRUCE_FENCE);add(x-1,ground+height-2,z,Blocks.SPRUCE_FENCE);return;}
        int low=type==0?height/2:height-3;
        for(int y=low;y<=height+1;y++){
            int radius=type==0?Math.max(0,(height+1-y)/2):y==height+1?1:2+(type==1&&y==height-1?1:0);
            for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
                if(dx*dx+dz*dz>radius*radius+1||Math.floorMod(dx*31+dz*17+y*7+x,11)==0)continue;
                int px=x+leanX+dx,pz=z+leanZ+dz,py=ground+y;
                if(l.getBlockState(p(px,py,pz)).isAir())put(px,py,pz,leaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
            }
        }
    }
}
