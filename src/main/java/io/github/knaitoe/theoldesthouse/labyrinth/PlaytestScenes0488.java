package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Small, once-only scenery repairs; original books, actors and finite supplies are never restaged. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PlaytestScenes0488 {
    public static final String STATE="scene_finishing_0488",ORIGINALS="plain_notes_originals_0488";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private static final List<LabyrinthPlace> SITES=List.of(LabyrinthPlace.HOLY_RABBIT,LabyrinthPlace.WHALE,LabyrinthPlace.ZAMPANO_COURTYARD,LabyrinthPlace.PLAIN);
    private PlaytestScenes0488(){}
    public static boolean applies(LabyrinthPlace p){return SITES.contains(p);}
    private static String key(BlockPos b,LabyrinthPlace p){return b.asLong()+":"+p.id();}
    public static boolean done(ServerLevel l,BlockPos b,LabyrinthPlace p){return LabyrinthData.get(l.getServer()).state(STATE).getBoolean(key(b,p));}
    public static void forget(net.minecraft.server.MinecraftServer s,BlockPos origin,LabyrinthPlace p){var d=LabyrinthData.get(s);var t=d.state(STATE);t.remove(key(LabyrinthPlaces.base(origin,p),p));d.setState(STATE,t);}

    /** A weatherproof field case retains the exact book that was on the old native lectern. */
    public static ItemStack plainOriginal(ServerLevel l,BlockPos b){
        var saved=LabyrinthData.get(l.getServer()).state(ORIGINALS);String k=Long.toString(b.asLong());
        if(saved.contains(k))return ItemStack.parseOptional(l.registryAccess(),saved.getCompound(k));
        if(l.getBlockEntity(b.offset(NovelRooms.APOLOGY)) instanceof LecternBlockEntity lectern)return lectern.getBook().copy();
        return NovelTexts.apology();
    }
    private static void plain(ServerLevel l,BlockPos b,Map<BlockPos,BlockState> edits){
        var at=b.offset(NovelRooms.APOLOGY);var old=l.getBlockState(at);
        if(old.is(Blocks.LECTERN))edits.put(at,VignetteDetailBlock.state(VignetteDetailBlock.Kind.PLAIN_NOTES,Direction.SOUTH));
    }

    /** The original handwriting fills the north wall, laid against actual plaster instead of generic notice papers. */
    private static void archive(ServerLevel l,BlockPos b,Map<BlockPos,BlockState> edits){
        for(int x=-11;x<=11;x++)for(int y=0;y<=6;y++){
            var wall=b.offset(x,y,-39);if(l.getBlockState(wall).is(NovelRegistry.PAPER.get()))edits.put(wall,NovelRegistry.PLASTER.get().defaultBlockState());
            if(Math.abs(x)>10||y<1||y>5)continue;
            var at=wall.south();var old=l.getBlockState(at);var backing=l.getBlockState(wall);
            if(!backing.is(NovelRegistry.PLASTER.get())&&!backing.is(NovelRegistry.PAPER.get()))continue;
            if(old.isAir()||old.is(HouseBlocks.VIGNETTE_DETAIL.get())&&old.getValue(VignetteDetailBlock.KIND).name().startsWith("SCRIBBLE_"))
                edits.put(at,VignetteDetailBlock.state(VignetteDetailBlock.Kind.valueOf("SCRIBBLE_"+(8+Math.floorMod(x*3+y*5,8))),Direction.SOUTH));
        }
        var bowl=b.offset(CourtyardFountain.BOWL);
        if(l.getBlockState(bowl).is(Blocks.WATER_CAULDRON))edits.put(bowl,VignetteDetailBlock.state(VignetteDetailBlock.Kind.FOUNTAIN_BOWL,Direction.NORTH));
    }

    /** All authored illumination comes from the yellow fixtures attached to the ceiling. */
    private static void institute(ServerLevel l,BlockPos b,Map<BlockPos,BlockState> edits){
        for(int x=-14;x<=14;x++)for(int z=-33;z<=0;z++)for(int y=0;y<=10;y++){
            var at=b.offset(x,y,z);var old=l.getBlockState(at);
            boolean fixture=old.is(NovelRegistry.WARD_FIXTURE.get())&&old.getValue(InstituteFixtureBlock.KIND)==InstituteFixtureBlock.Kind.LIGHT;
            boolean lamp=old.is(HouseBlocks.SCENE_DETAIL.get())&&old.getValue(SceneDetailBlock.KIND)==SceneDetailBlock.Kind.TABLE_LAMP;
            if(lamp||old.is(Blocks.LIGHT)){edits.put(at,Blocks.AIR.defaultBlockState());continue;}
            if(old.is(Blocks.LANTERN)||old.is(Blocks.SOUL_LANTERN)||old.is(Blocks.SEA_LANTERN)||fixture&&y<4){
                int top=y>=6?9:4;var ceiling=b.offset(x,top,z);
                if(l.getBlockState(ceiling).isAir()||ceiling.equals(at))edits.put(ceiling,InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.LIGHT));
                if(!ceiling.equals(at))edits.put(at,Blocks.AIR.defaultBlockState());
            }
        }
    }

    /** A two-bed camp under the existing tree; the father's real walking route and carved trunk remain clear. */
    private static void camp(ServerLevel l,BlockPos b,Map<BlockPos,BlockState> edits){
        for(int x=-3;x<=3;x++)for(int z=-28;z<=-21;z++)for(int y=0;y<=2;y++){
            var at=b.offset(x,y,z);if(l.getBlockState(at).is(Blocks.SPRUCE_LEAVES))edits.put(at,Blocks.AIR.defaultBlockState());
        }
        for(var rel:List.of(new BlockPos(-2,0,-27),new BlockPos(1,0,-27),new BlockPos(-2,0,-26),new BlockPos(1,0,-26))){
            var at=b.offset(rel);if(l.getBlockState(at).isAir()||l.getBlockState(at).is(Blocks.SPRUCE_LEAVES))edits.put(at,SceneDetailBlock.state(SceneDetailBlock.Kind.BLANKET,Direction.SOUTH));
        }
        for(var entry:Map.of(new BlockPos(-3,0,-23),SceneDetailBlock.Kind.SATCHEL,new BlockPos(1,0,-23),SceneDetailBlock.Kind.CROCK).entrySet()){
            var at=b.offset(entry.getKey());if(l.getBlockState(at).isAir()||l.getBlockState(at).is(Blocks.SPRUCE_LEAVES))edits.put(at,SceneDetailBlock.state(entry.getValue(),Direction.SOUTH));
        }
    }
    private static AABB area(BlockPos b,LabyrinthPlace p){
        if(p==LabyrinthPlace.HOLY_RABBIT)return new AABB(Vec3.atLowerCornerOf(b.offset(-5,-1,-29)),Vec3.atLowerCornerOf(b.offset(6,5,-17)));
        if(p==LabyrinthPlace.PLAIN)return new AABB(Vec3.atLowerCornerOf(b.offset(-5,-1,-8)),Vec3.atLowerCornerOf(b.offset(-2,3,-5)));
        return SceneReview.area(b,p);
    }
    private static boolean loaded(ServerLevel l,AABB a){
        for(int x=(int)Math.floor(a.minX)>>4;x<=(int)Math.floor(a.maxX)>>4;x++)for(int z=(int)Math.floor(a.minZ)>>4;z<=(int)Math.floor(a.maxZ)>>4;z++)
            if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;return true;
    }
    public static boolean repair(ServerLevel l,BlockPos b,LabyrinthPlace p,boolean guarded){
        if(!SITES.contains(p)||done(l,b,p))return true;var area=area(b,p);
        if(guarded&&(!loaded(l,area)||l.players().stream().anyMatch(reader->area.inflate(48).intersects(reader.getCamera().getBoundingBox()))))return false;
        var edits=new LinkedHashMap<BlockPos,BlockState>();
        switch(p){case HOLY_RABBIT->camp(l,b,edits);case WHALE->institute(l,b,edits);case ZAMPANO_COURTYARD->archive(l,b,edits);case PLAIN->plain(l,b,edits);default->{}}
        for(var entry:edits.entrySet()){
            var at=entry.getKey();var old=l.getBlockState(at);var next=entry.getValue();
            if(l.getBlockEntity(at)!=null&&!(p==LabyrinthPlace.PLAIN&&old.is(Blocks.LECTERN)&&l.getBlockEntity(at) instanceof LecternBlockEntity))return false;
            if(guarded){var change=Shapes.joinUnoptimized(old.getCollisionShape(l,at),next.getCollisionShape(l,at),BooleanOp.NOT_SAME);
                for(var box:change.toAabbs())if(!l.getEntitiesOfClass(LivingEntity.class,box.move(at).inflate(.02),LivingEntity::isAlive).isEmpty())return false;}
        }
        var d=LabyrinthData.get(l.getServer());
        if(p==LabyrinthPlace.PLAIN&&edits.containsKey(b.offset(NovelRooms.APOLOGY))){
            var originals=d.state(ORIGINALS);String k=Long.toString(b.asLong());
            if(!originals.contains(k))originals.put(k,plainOriginal(l,b).save(l.registryAccess()));d.setState(ORIGINALS,originals);
            SceneHuntReview.emptyLectern(l,b.offset(NovelRooms.APOLOGY));
        }
        edits.forEach((at,next)->l.setBlock(at,next,F));
        var state=d.state(STATE);state.putBoolean(key(b,p),true);d.setState(STATE,state);return true;
    }
    public static void fresh(ServerLevel l,BlockPos b,LabyrinthPlace p){if(applies(p))BuildBlocks.after(l,()->{var d=LabyrinthData.get(l.getServer());var t=d.state(STATE);t.remove(key(b,p));d.setState(STATE,t);repair(l,b,p,false);});}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=19||LabyrinthBuilder.isCarving())return;
        var origin=HouseSavedData.get(s).houseOrigin();if(origin==null)return;
        for(var p:SITES){if(!LabyrinthBuilder.isPlaceReady(s,p))continue;var l=s.getLevel(NovelRooms.dimension(p));if(l==null)continue;var b=LabyrinthPlaces.base(origin,p);
            if(p==LabyrinthPlace.ZAMPANO_COURTYARD&&!CourtyardFountain.done(LabyrinthData.get(s),b))continue;
            if(!done(l,b,p)&&repair(l,b,p,true))break;}
    }
}
