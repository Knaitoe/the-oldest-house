package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;

/** Recognized-cell furnishing repairs; no source, reward, actor or container is constructed here. */
public final class PlaytestScenery130 {
    public static final String STATE="playtest_furnishings_0130";
    public static final List<LabyrinthPlace> PLACES=List.of(LabyrinthPlace.QUIET_ROOM,LabyrinthPlace.END_WORLD_CABIN,LabyrinthPlace.ELK_LOT);
    public record Edit(BlockPos relative,BlockState expected,BlockState next){}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private static Work work;
    private static final class Work {
        final ServerLevel level;final BlockPos origin,base;final LabyrinthPlace place;final List<Edit> edits;int cursor;
        Work(ServerLevel l,BlockPos o,LabyrinthPlace p,int n){level=l;origin=o.immutable();base=LabyrinthPlaces.base(o,p);place=p;edits=plan(p);cursor=n;}
    }
    private PlaytestScenery130(){}
    private static String key(BlockPos o,LabyrinthPlace p){return o.asLong()+":"+p.id();}
    private static BlockState furniture(HouseholdFurnitureBlock.Kind k,Direction f){return HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,k).setValue(HouseholdFurnitureBlock.FACING,f);}
    private static void add(List<Edit> edits,int x,int y,int z,BlockState old,BlockState next){edits.add(new Edit(new BlockPos(x,y,z),old,next));}
    private static void fit(List<Edit> edits,int x,int y,int z,VignetteInteriorBlock.Kind k,Direction f){add(edits,x,y,z,Blocks.AIR.defaultBlockState(),VignetteInteriorBlock.of(k,f));}
    private static void replace(List<Edit> edits,int x,int z,HouseholdFurnitureBlock.Kind old,Direction f,VignetteInteriorBlock.Kind next){add(edits,x,0,z,furniture(old,f),VignetteInteriorBlock.of(next,f));}
    public static List<Edit> plan(LabyrinthPlace p){
        var e=new ArrayList<Edit>();
        if(p==LabyrinthPlace.END_WORLD_CABIN){
            replace(e,-8,-18,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH,VignetteInteriorBlock.Kind.CABIN_TABLE);
            replace(e,-8,-17,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.NORTH,VignetteInteriorBlock.Kind.CABIN_STOOL);
            replace(e,-8,-19,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.SOUTH,VignetteInteriorBlock.Kind.CABIN_STOOL);
            replace(e,2,-30,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH,VignetteInteriorBlock.Kind.CABIN_TABLE);
            replace(e,2,-29,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH,VignetteInteriorBlock.Kind.CABIN_CHAIR);
            replace(e,2,-31,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH,VignetteInteriorBlock.Kind.CABIN_CHAIR);
            replace(e,1,-30,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST,VignetteInteriorBlock.Kind.CABIN_CHAIR);
            replace(e,3,-30,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST,VignetteInteriorBlock.Kind.CABIN_CHAIR);
            for(int z:new int[]{-20,-16})add(e,-11,1,z,Blocks.FLOWER_POT.defaultBlockState(),VignetteInteriorBlock.of(VignetteInteriorBlock.Kind.CABIN_FERN,Direction.EAST));
            for(int[] at:new int[][]{{-11,-30},{11,-28},{11,-19}}){
                add(e,at[0],0,at[1],Blocks.AIR.defaultBlockState(),Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
                fit(e,at[0],1,at[1],VignetteInteriorBlock.Kind.CABIN_FERN,Direction.NORTH);
            }
            // The fire's back is masonry across the whole opening, including its left reveal.
            for(int x=9;x<=11;x++)for(int y=0;y<=3;y++)add(e,x,y,-33,Blocks.AIR.defaultBlockState(),Blocks.BRICKS.defaultBlockState());
            for(int x=9;x<=11;x++)for(int z=-32;z<=-31;z++)add(e,x,-1,z,Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.SMOOTH_STONE.defaultBlockState());
        }
        if(p==LabyrinthPlace.QUIET_ROOM){
            var air=Blocks.AIR.defaultBlockState();
            for(int z=-8;z>=-11;z--)add(e,-4,0,z,LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS,Direction.WEST),air);
            add(e,-3,1,-12,Blocks.OAK_PRESSURE_PLATE.defaultBlockState(),air);add(e,-3,0,-12,Blocks.DARK_OAK_FENCE.defaultBlockState(),air);
            add(e,-4,0,-9,air,UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.ARMCHAIR,Direction.EAST));
            add(e,-4,0,-11,air,UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.ARMCHAIR,Direction.EAST));
            add(e,-4,0,-10,air,UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.COFFEE_TABLE,Direction.EAST));
            add(e,-5,0,-13,air,UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.BOOKCASE,Direction.EAST));
            add(e,-5,1,-13,air,UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.BOOKCASE,Direction.EAST));
            // Counter and shelves unite the original water basin and finite cache; both originals stay accessible.
            for(int z:new int[]{-8,-9,-10})add(e,4,0,z,air,UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.COUNTER,Direction.WEST));
            for(int z:new int[]{-8,-10})add(e,4,2,z,air,UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.SHELF,Direction.WEST));
            add(e,4,1,-9,air,UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.CUP,Direction.WEST));
            add(e,2,0,-13,air,UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.ARMCHAIR,Direction.EAST));
        }
        if(p==LabyrinthPlace.ELK_LOT)for(int x=-12;x<=-11;x++){
            add(e,x,1,-31,Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.BOTTOM),Blocks.AIR.defaultBlockState());
            add(e,x,0,-31,Blocks.GREEN_TERRACOTTA.defaultBlockState(),VignetteInteriorBlock.of(x==-12?VignetteInteriorBlock.Kind.DUMPSTER_LEFT:VignetteInteriorBlock.Kind.DUMPSTER_RIGHT,Direction.NORTH));
        }
        return List.copyOf(e);
    }
    private static boolean safe(ServerLevel l,BlockPos at,Edit e,boolean recorded){
        var actual=recorded?BuildBlocks.state(l,at):l.getBlockState(at);
        if(!actual.equals(e.expected())||l.getBlockEntity(at)!=null)return false;
        if(!e.next().isAir()&&e.expected().isAir()){
            var below=recorded?BuildBlocks.state(l,at.below()):l.getBlockState(at.below());
            if(below.getCollisionShape(l,at.below()).isEmpty())return false;
        }
        return l.getEntities((Entity)null,new AABB(at).inflate(.01),a->a.isAlive()&&!a.isSpectator()).isEmpty();
    }
    public static void fresh(ServerLevel l,BlockPos origin,LabyrinthPlace p){
        if(!PLACES.contains(p))return;var base=LabyrinthPlaces.base(origin,p);
        for(var e:plan(p)){var at=base.offset(e.relative());if(safe(l,at,e,true))BuildBlocks.guardedSet(l,at,e.next(),F,()->safe(l,at,e,false)||l.getBlockState(at).equals(e.next()));}
        BuildBlocks.after(l,()->{var d=LabyrinthData.get(l.getServer());var state=d.state(STATE);state.putBoolean(key(origin,p),true);d.setState(STATE,state);});
    }
    public static boolean step(ServerLevel l,BlockPos origin,LabyrinthPlace p){
        if(!PLACES.contains(p))return true;var d=LabyrinthData.get(l.getServer());var state=d.state(STATE);var key=key(origin,p);if(state.getBoolean(key))return true;
        var area=SceneReview.area(LabyrinthPlaces.base(origin,p),p);
        if(l.players().stream().anyMatch(a->area.inflate(24).intersects(a.getCamera().getBoundingBox())))return false;
        var edits=plan(p);var base=LabyrinthPlaces.base(origin,p);
        for(var e:edits){var at=base.offset(e.relative());if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return false;}
        if(work==null||work.level.getServer()!=l.getServer()||!work.origin.equals(origin))work=new Work(l,origin,p,state.getInt(key+":cursor"));
        if(work.level!=l||work.place!=p)return false;
        int visited=0;long deadline=System.nanoTime()+3_000_000;
        while(work.cursor<work.edits.size()&&visited++<256&&(visited==1||System.nanoTime()<deadline)){
            var e=work.edits.get(work.cursor++);var at=work.base.offset(e.relative());if(safe(l,at,e,false))l.setBlock(at,e.next(),F);
        }
        state.putInt(key+":cursor",work.cursor);boolean done=work.cursor>=work.edits.size();if(done){state.putBoolean(key,true);work=null;}d.setState(STATE,state);return done;
    }
    public static void tick(MinecraftServer s){
        if(s instanceof net.minecraft.gametest.framework.GameTestServer||LabyrinthBuilder.isCarving())return;
        var origin=HouseSavedData.get(s).houseOrigin();if(origin==null){work=null;return;}var d=LabyrinthData.get(s);if(!origin.equals(d.builtOrigin()))return;
        if(work!=null){if(work.level.getServer()!=s||!work.origin.equals(origin)){work=null;return;}step(work.level,origin,work.place);return;}
        if(s.getTickCount()%20!=0)return;for(var p:PLACES)if(d.door(p.entryDoorId())!=null&&!d.state(STATE).getBoolean(key(origin,p))){var l=s.getLevel(NovelRooms.dimension(p));if(l!=null&&!step(l,origin,p))return;}
    }
}
