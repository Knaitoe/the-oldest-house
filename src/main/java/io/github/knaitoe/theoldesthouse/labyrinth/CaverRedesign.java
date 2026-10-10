package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Brings a saved cave to its 0.4.71 shape once, in place (checkpoint {@value #STATE}). The bowl is closed behind the stone,
 * the passage behind it and the low chamber get their lower ceilings, and an unopened crack becomes the packed run with its
 * saved work kept. Nothing is rebuilt or restocked: the notebook, barrel, ladder, line and every reader's record stay as
 * they are. A torch standing where rock returns goes back to the camp barrel. The whole change waits while anyone can see
 * the deep cave or would be touched by new rock, and applies all at once.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class CaverRedesign {
    public static final String STATE="caver_0471",MARKS="caver_marks_0472",CRAWLS="caver_crawls_0473";
    /** Work at which an older cave's single crack was open. */
    static final int OLD_STROKES=24;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private CaverRedesign(){}

    public static boolean done(LabyrinthData d,BlockPos b){return d.state(STATE).getBoolean(Long.toString(b.asLong()));}
    public static boolean marked(LabyrinthData d,BlockPos b){return d.state(MARKS).getBoolean(Long.toString(b.asLong()));}
    public static boolean crawled(LabyrinthData d,BlockPos b){return d.state(CRAWLS).getBoolean(Long.toString(b.asLong()));}
    static void markBuilt(LabyrinthData d,BlockPos b){check(d,STATE,b);check(d,MARKS,b);check(d,CRAWLS,b);}
    private static void check(LabyrinthData d,String id,BlockPos b){var s=d.state(id);s.putBoolean(Long.toString(b.asLong()),true);d.setState(id,s);}
    /** No camera anywhere in the deep cave, and its chunks and entity sections loaded. */
    private static boolean ready(ServerLevel l,BlockPos b){
        if(l.players().stream().anyMatch(p->deep(b).intersects(p.getCamera().getBoundingBox())))return false;
        for(int x=(b.getX()-10)>>4;x<=(b.getX()+11)>>4;x++)for(int z=(b.getZ()-61)>>4;z<=(b.getZ()-15)>>4;z++)
            if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        return true;
    }
    /**
     * 0.4.72: the dressed chiseled block older caves used for the mark becomes the marked rock, once ({@value #MARKS}). Same
     * collision; it waits only for the cave to be loaded and unwatched.
     */
    public static boolean marks(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());if(marked(d,b))return crawls(l,b);if(!ready(l,b))return false;
        var at=b.offset(CaverCave.MARK);if(l.getBlockState(at).is(Blocks.CHISELED_DEEPSLATE))l.setBlock(at,LabyrinthRegistry.CAVE_MARKS.get().defaultBlockState(),F);
        check(d,MARKS,b);return crawls(l,b);
    }
    /**
     * 0.4.73: the straight squeeze and the open passage become the two winding crawls with their air bells, once
     * ({@value #CRAWLS}). Rock comes back only where nothing living stands; a torch in the way goes back to the barrel. All
     * of it, or none, and only while the cave is loaded and unwatched.
     */
    public static boolean crawls(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());if(crawled(d,b))return true;if(!ready(l,b))return false;
        var bodies=l.getEntitiesOfClass(LivingEntity.class,deep(b),e->e.isAlive()&&!e.isSpectator());
        var plan=new ArrayList<Change>();
        for(int x=-6;x<=6;x++)for(int y=-3;y<=-1;y++)for(int z=-53;z<=-28;z++){
            var open=CaverCave.crawlTemplate(x,y,z);if(open==null)continue;
            var at=b.offset(x,y,z);var old=l.getBlockState(at);
            if(open){if(rockLike(old)&&l.getBlockEntity(at)==null)plan.add(new Change(at,Blocks.AIR.defaultBlockState(),false));}
            else rock(l,b,x,y,z,plan);
        }
        for(var c:plan)if(!safe(l,c.at(),c.next(),bodies))return false;
        for(var c:plan){if(c.refund()){refund(l,b,l.getBlockState(c.at()));CaverVignette.forgetTorch(d,c.at());}l.setBlock(c.at(),c.next(),F);}
        check(d,CRAWLS,b);return true;
    }
    /** The cave's own rock, which a new crawl may be cut through; never a placed or authored block. */
    private static boolean rockLike(BlockState s){return s.is(Blocks.STONE)||s.is(Blocks.ANDESITE)||s.is(Blocks.TUFF)||s.is(Blocks.MOSSY_COBBLESTONE)||s.is(Blocks.DEEPSLATE)||s.is(Blocks.COBBLESTONE);}
    /** The deep cave beyond the corridor, where every change is made and where no camera may be. */
    public static AABB deep(BlockPos b){return new AABB(Vec3.atLowerCornerOf(b.offset(-10,-5,-61)),Vec3.atLowerCornerOf(b.offset(11,9,-15)));}

    private record Change(BlockPos at,BlockState next,boolean refund){}

    /** Returns true once the cave has its new shape; false while it must wait. */
    public static boolean repair(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());if(done(d,b))return marks(l,b);if(!ready(l,b))return false;
        var bodies=l.getEntitiesOfClass(LivingEntity.class,deep(b),e->e.isAlive()&&!e.isSpectator());
        var plan=new ArrayList<Change>();
        // The bowl's back right becomes the stone's wall; the passage behind it loses its third block of height.
        for(int x=3;x<=6;x++)for(int y=-3;y<=5;y++)for(int z=-49;z<=-43;z++){
            if(z==-43&&x>=4&&x<=6&&y<=-2&&(x<=5||y==-3))continue; // the stone itself, and where it rolls to
            if(CaverCave.passage(x,y,z))continue;
            rock(l,b,x,y,z,plan);
        }
        // The low chamber: one block of air under a slab ceiling, sealed above, its only way in through the passage.
        for(int x=2;x<=7;x++)for(int z=-57;z<=-50;z++)for(int y=-3;y<=1;y++){
            if(CaverCave.passage(x,y,z)||CaverCave.chamber(x,y,z))continue;
            if(CaverCave.chamberCeiling(x,y,z)){change(l,b.offset(x,y,z),CaverCave.ceiling(),plan);continue;}
            rock(l,b,x,y,z,plan);
        }
        var light=b.offset(5,-3,-54);if(l.getBlockState(light).isAir())plan.add(new Change(light,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,2),false));
        // An unopened crack becomes the packed run; saved strokes keep the blocks they had already earned.
        var state=d.state(CaverVignette.ID);int work=state.getInt("Work");boolean open=work>=OLD_STROKES&&CaverCave.front(l,b)<0;
        if(!open){int removed=Math.max(0,work)/CaverVignette.PER_BLOCK;
            for(int i=0;i<CaverCave.RUBBLE;i++){var at=b.offset(CaverCave.rubbleCell(i));var old=l.getBlockState(at);
                if(!old.isAir()&&!CaverCave.isRubble(old))continue;
                change(l,at,(i<removed?Blocks.AIR:LabyrinthRegistry.CAVE_RUBBLE.get()).defaultBlockState(),plan);}}
        for(var c:plan)if(!safe(l,c.at(),c.next(),bodies))return false;
        // Everything can change: refund what stood in the new rock, then make it.
        for(var c:plan){if(c.refund()){refund(l,b,l.getBlockState(c.at()));CaverVignette.forgetTorch(d,c.at());}l.setBlock(c.at(),c.next(),F);}
        if(open&&work!=CaverVignette.STROKES){state.putInt("Work",CaverVignette.STROKES);d.setState(CaverVignette.ID,state);}
        check(d,STATE,b);
        return marks(l,b);
    }
    private static boolean torch(BlockState s){return s.is(Blocks.TORCH)||s.is(Blocks.WALL_TORCH)||s.is(Blocks.SOUL_TORCH)||s.is(Blocks.SOUL_WALL_TORCH);}
    private static void rock(ServerLevel l,BlockPos b,int x,int y,int z,List<Change> plan){
        var at=b.offset(x,y,z);var old=l.getBlockState(at);
        if(torch(old)){plan.add(new Change(at,CaverCave.rock(x,y,z),true));return;}
        if(old.isAir()||old.is(Blocks.LIGHT)||old.is(Blocks.POINTED_DRIPSTONE))plan.add(new Change(at,CaverCave.rock(x,y,z),false));
    }
    private static void change(ServerLevel l,BlockPos at,BlockState next,List<Change> plan){
        var old=l.getBlockState(at);if(old.equals(next)||l.getBlockEntity(at)!=null)return;
        plan.add(new Change(at,next,torch(old)));
    }
    /** New collision never closes through a living body. */
    private static boolean safe(ServerLevel l,BlockPos at,BlockState next,List<LivingEntity> bodies){
        var added=Shapes.joinUnoptimized(next.getCollisionShape(l,at),l.getBlockState(at).getCollisionShape(l,at),BooleanOp.ONLY_FIRST);
        for(var box:added.toAabbs())for(var body:bodies)if(box.move(at).intersects(body.getBoundingBox()))return false;
        return true;
    }
    /** A placed torch is a finite supply: it goes back into the camp barrel, or beside it if the barrel is full. */
    private static void refund(ServerLevel l,BlockPos b,BlockState old){
        var stack=new ItemStack(old.is(Blocks.SOUL_TORCH)||old.is(Blocks.SOUL_WALL_TORCH)?net.minecraft.world.item.Items.SOUL_TORCH:net.minecraft.world.item.Items.TORCH);
        var cache=b.offset(CaverCave.CACHE);
        if(l.getBlockEntity(cache) instanceof Container box){
            for(int i=0;i<box.getContainerSize()&&!stack.isEmpty();i++){var held=box.getItem(i);
                if(held.isEmpty()){box.setItem(i,stack);stack=ItemStack.EMPTY;}
                else if(ItemStack.isSameItemSameComponents(held,stack)&&held.getCount()<held.getMaxStackSize()){held.grow(1);stack=ItemStack.EMPTY;}}
            box.setChanged();
        }
        if(!stack.isEmpty())Containers.dropItemStack(l,cache.getX()+.5,cache.getY()+1,cache.getZ()-.5,stack);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        var s=event.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=7||LabyrinthBuilder.isCarving())return;
        var origin=HouseSavedData.get(s).houseOrigin();if(origin==null)return;var data=LabyrinthData.get(s);var p=LabyrinthPlace.TED_CAVER;
        if(data.door(p.entryDoorId())==null||!LabyrinthBuilder.isPlaceReady(data,p))return;
        var b=LabyrinthPlaces.base(origin,p);var l=s.getLevel(NovelRooms.dimension(p));
        if(b!=null&&l!=null&&!(done(data,b)&&marked(data,b)&&crawled(data,b)))repair(l,b);
    }
}
