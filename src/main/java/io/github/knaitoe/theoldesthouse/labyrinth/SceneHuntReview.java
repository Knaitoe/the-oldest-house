package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.house.VignetteDetailBlock.Kind.*;

/** 0.4.55: preserve scene addresses and originals while repairing the authored scenery. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SceneHuntReview {
    public static final String STATE="scene_hunt_review_0455";
    public static final BlockPos TOOLS=new BlockPos(-5,1,-15);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private static Work work;private static int cursor;
    private record Work(ServerLevel level,BlockPos origin,LabyrinthPlace place,BuildBlocks.Plan plan){}
    private SceneHuntReview(){}
    private static String key(BlockPos origin,LabyrinthPlace place){return origin.asLong()+":"+place.id();}
    public static boolean applies(LabyrinthPlace p){return p!=LabyrinthPlace.ELK_CARCASSES&&SceneReview.applies(p);}
    private static boolean loaded(ServerLevel l,AABB a){
        for(int x=(int)Math.floor(a.minX)>>4;x<=((int)Math.ceil(a.maxX)-1)>>4;x++)
            for(int z=(int)Math.floor(a.minZ)>>4;z<=((int)Math.ceil(a.maxZ)-1)>>4;z++)
                if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;return true;
    }
    private static boolean vacant(ServerLevel l,AABB a){return l.players().stream().noneMatch(p->a.inflate(24).intersects(p.getCamera().getBoundingBox()));}
    private static boolean safe(ServerLevel l,BlockPos at,BlockState next,boolean source){
        if(l.getBlockEntity(at)!=null&&!(source&&l.getBlockEntity(at) instanceof LecternBlockEntity))return false;
        var old=l.getBlockState(at).getCollisionShape(l,at);var fresh=next.getCollisionShape(l,at);
        var changed=Shapes.joinUnoptimized(old,fresh,BooleanOp.NOT_SAME);
        for(var box:changed.toAabbs())if(!l.getEntitiesOfClass(LivingEntity.class,box.move(at).inflate(.02,.09,.02),e->e.isAlive()&&!e.isSpectator()&&!(fresh.isEmpty()&&e.isNoGravity()&&(e instanceof net.minecraft.world.entity.decoration.ArmorStand&&e.getTags().contains("HouseCostume")||e instanceof LakeWitchEntity))).isEmpty())return false;
        return true;
    }
    private static boolean set(ServerLevel l,BlockPos at,BlockState next){
        if(BuildBlocks.state(l,at).equals(next))return true;if(!safe(l,at,next,false))return false;
        return BuildBlocks.guardedSet(l,at,next,F,()->safe(l,at,next,false));
    }
    private static boolean add(ServerLevel l,BlockPos at,BlockState next){return !BuildBlocks.state(l,at).isAir()||set(l,at,next);}
    private static boolean detail(ServerLevel l,BlockPos b,BlockPos rel,VignetteDetailBlock.Kind kind,Direction face){
        var at=b.offset(rel);boolean wall=Set.of(REPAIR_DIAGRAM,FAN_WALLPAPER,CURTAIN,FAMILY_BOARD,CHILD_FABRIC).contains(kind);
        var support=wall?at.relative(face.getOpposite()):at.below();if(BuildBlocks.state(l,support).getShape(l,support).isEmpty())return true;
        return add(l,at,VignetteDetailBlock.state(kind,face));
    }
    private static boolean surface(ServerLevel l,BlockPos b,int x,int y,int z,VignetteDetailBlock.Kind kind,Direction face){return detail(l,b,new BlockPos(x,y,z),kind,face);}
    /** The exact former native source is archived once, before its reading surface changes. */
    private static boolean source(ServerLevel l,BlockPos origin,BlockPos b,LabyrinthPlace p){
        boolean archive=p==LabyrinthPlace.ZAMPANO_COURTYARD;
        if(!archive&&(!LiteraryRooms.isLiterary(p)||p==LabyrinthPlace.FAMILY_COPY||p==LabyrinthPlace.OLD_CABIN))return true;
        var rel=archive?NovelRooms.ARCHIVE_DESK:LiteraryRooms.source(p);var at=b.offset(rel);var old=BuildBlocks.state(l,at);
        var kind=SceneReview.readingKind(p);if(rel.getY()>0&&kind==BOOK_TRAY)kind=FIELD_NOTEBOOK;
        var next=archive?HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.READING_DESK,Direction.SOUTH):VignetteDetailBlock.state(kind,Direction.SOUTH);
        if(old.is(Blocks.LECTERN)){
            if(!safe(l,at,next,true))return false;
            if(l.getBlockEntity(at) instanceof LecternBlockEntity lectern){
                var book=lectern.getBook().copy();if(!book.isEmpty())BuildBlocks.after(l,()->{
                    var d=LabyrinthData.get(l.getServer());var originals=d.state("scene_source_originals_0455");
                    if(!originals.contains(key(origin,p)))originals.put(key(origin,p),book.save(l.registryAccess()));d.setState("scene_source_originals_0455",originals);});
            }
            BuildBlocks.guardedSet(l,at,next,F,()->safe(l,at,next,true));
        }else if(old.isAir()){
            if(archive){var floor=at.below();if(!BuildBlocks.state(l,floor).getShape(l,floor).isEmpty())set(l,at,next);}
            else detail(l,b,rel,kind,Direction.SOUTH);
        }
        if(archive)return detail(l,b,rel.above(),SCRIBBLE_7,Direction.SOUTH);
        // Remove only the redundant alternate object authored by 0.4.54.
        var alias=b.offset(SceneReview.readingSurface(p));var s=BuildBlocks.state(l,alias);
        if(!alias.equals(at)&&s.is(HouseBlocks.VIGNETTE_DETAIL.get())&&s.getValue(VignetteDetailBlock.KIND)==SceneReview.readingKind(p))
            return set(l,alias,Blocks.AIR.defaultBlockState());return true;
    }
    public static ItemStack sourceBook(ServerLevel l,BlockPos b,LabyrinthPlace p){
        var origin=HouseSavedData.get(l.getServer()).houseOrigin();var d=LabyrinthData.get(l.getServer());
        if(origin!=null){var all=d.state("scene_source_originals_0455");if(all.contains(key(origin,p)))return ItemStack.parseOptional(l.registryAccess(),all.getCompound(key(origin,p)));}
        if(l.getBlockEntity(b.offset(p==LabyrinthPlace.ZAMPANO_COURTYARD?NovelRooms.ARCHIVE_DESK:LiteraryRooms.source(p))) instanceof LecternBlockEntity lectern&&!lectern.getBook().isEmpty())return lectern.getBook().copy();
        if(p==LabyrinthPlace.END_WORLD_CABIN&&l.getBlockEntity(b.offset(-8,0,-31)) instanceof LecternBlockEntity old&&!old.getBook().isEmpty())return old.getBook().copy();
        return p==LabyrinthPlace.ZAMPANO_COURTYARD?NovelTexts.archive():LiteraryTexts.source(p);
    }
    private static boolean fan(ServerLevel l,BlockPos b){boolean ready=true;
        // A smaller drawing room and three-wide connecting hall retain every interaction address.
        for(int y=0;y<=8;y++)for(int z=-25;z<=-7;z++)for(int x:new int[]{-9,9}){
            var at=b.offset(x,y,z);var s=BuildBlocks.state(l,at);
            if(s.isAir()||s.is(HouseBlocks.SCENE_DETAIL.get())||s.is(Blocks.DARK_OAK_SLAB))ready&=set(l,at,LiteraryRegistry.SIDING.get().defaultBlockState());
        }
        for(int x=-9;x<=9;x++)for(int y=0;y<=8;y++)for(int z:new int[]{-25,-7}){
            if(z==-7&&Math.abs(x)<=1&&y<3)continue;var at=b.offset(x,y,z);var s=BuildBlocks.state(l,at);
            if(s.isAir()||s.is(HouseBlocks.SCENE_DETAIL.get()))ready&=set(l,at,LiteraryRegistry.SIDING.get().defaultBlockState());
        }
        for(int x=-8;x<=8;x++)for(int z=-24;z<=-8;z++){
            var at=b.offset(x,8,z);if(BuildBlocks.state(l,at).isAir())ready&=set(l,at,Blocks.BIRCH_PLANKS.defaultBlockState());
        }
        for(int z:new int[]{-10,-14,-21})for(int y=1;y<=3;y++){
            ready&=add(l,b.offset(-8,y,z),VignetteDetailBlock.state(FAN_WALLPAPER,Direction.EAST));
            ready&=add(l,b.offset(8,y,z),VignetteDetailBlock.state(FAN_WALLPAPER,Direction.WEST));
        }
        for(int x=-5;x<=-2;x++)for(int y=2;y<=4;y++)ready&=add(l,b.offset(x,y,-24),VignetteDetailBlock.state(CURTAIN,Direction.NORTH));
        ready&=add(l,b.offset(TOOLS.below()),HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH));
        ready&=detail(l,b,TOOLS,TOOL_TRAY,Direction.SOUTH);
        ready&=surface(l,b,-8,2,-16,REPAIR_DIAGRAM,Direction.EAST);
        for(int z:new int[]{-10,-11}){
            ready&=add(l,b.offset(6,0,z),HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.WEST));
            ready&=surface(l,b,6,1,z,CUSHION,Direction.WEST);
        }
        ready&=add(l,b.offset(-4,0,-22),HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH));
        ready&=surface(l,b,-4,1,-22,RECORDER,Direction.SOUTH);
        return ready;
    }
    private static boolean cover(ServerLevel l,BlockPos b,int x,int z){boolean ready=true;
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
            var at=b.offset(x+dx,1,z+dz);if(BuildBlocks.state(l,at).isAir())ready&=set(l,at,HouseBlocks.FOREST_COVER.get().defaultBlockState());
        }return ready;
    }
    private static boolean forest(ServerLevel l,BlockPos b){boolean ready=true;
        // Only new ground beyond the saved footprint is filled; the original pit remains open.
        for(int x=-55;x<=55;x++)for(int z=-131;z<=-2;z++)if(Math.abs(x)>30||z<-77){
            for(int y=-3;y<=-1;y++){var at=b.offset(x,y,z);if(BuildBlocks.state(l,at).isAir())
                ready&=set(l,at,(y==-1?(Math.floorMod(x*7+z*11,17)<3?Blocks.COARSE_DIRT:Blocks.PODZOL):Blocks.DIRT).defaultBlockState());}
        }
        // Low cover at arrival hides the visitor from the first clearing without obstructing the door.
        for(int side:new int[]{-2,2})for(int z=-9;z<=-4;z++)for(int y=0;y<=2;y++)
            ready&=add(l,b.offset(side,y,z),Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
        for(int[] node:new int[][]{{-4,-8},{4,-8},{-18,-46},{-8,-24},{10,-29},{-20,-65},{27,-81},{-33,-103},{16,-111}})ready&=cover(l,b,node[0],node[1]);
        // ELK_HIDE owns a native block entity: its new low hide mesh changes presentation, retaining that original.
        for(int x=-1;x<=1;x++)for(int z=-34;z<=-33;z++){
            var at=b.offset(x,1,z);if(BuildBlocks.state(l,at).is(Blocks.SPRUCE_TRAPDOOR))ready&=set(l,at,HouseBlocks.FOREST_COVER.get().defaultBlockState());
        }
        for(int[] node:new int[][]{{-4,-35},{14,-66},{-27,-104}})ready&=surface(l,b,node[0],0,node[1],ELK_SKULL,Direction.SOUTH);
        // Distinct rooted groves and connected three-wide trails in the expanded ground.
        for(int[] node:new int[][]{{-42,-30},{43,-39},{-40,-63},{42,-77},{-45,-97},{37,-110},{-22,-123},{17,-126}}){
            int x=node[0],z=node[1];var root=b.offset(x,-1,z);if(BuildBlocks.state(l,root).getCollisionShape(l,root).isEmpty())continue;
            for(int y=0;y<=5;y++)ready&=add(l,b.offset(x,y,z),Blocks.BIRCH_LOG.defaultBlockState());
            for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)for(int y=4;y<=7;y++)
                if(Math.abs(dx)+Math.abs(dz)<=4-(y-4)/2)ready&=add(l,b.offset(x+dx,y,z+dz),Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
        }
        for(int z=-126;z<=-30;z++){int x=32+(int)Math.round(Math.sin(z*.065)*5);
            for(int dx=-1;dx<=1;dx++){var at=b.offset(x+dx,-1,z);var s=BuildBlocks.state(l,at);
                if(s.is(Blocks.PODZOL)||s.is(Blocks.COARSE_DIRT))ready&=set(l,at,Blocks.ROOTED_DIRT.defaultBlockState());}}
        return ready;
    }
    private static boolean costumeLake(ServerLevel l,BlockPos b){boolean ready=true;
        // An eroded peninsula, three islands and deep coves replace the featureless beach rectangle.
        for(int z=-47;z<=-15;z++)for(int x=-26;x<=26;x++){
            int width=z>-24?9:z>-34?4:2;int center=(int)Math.round(Math.sin(z*.12)*2);
            boolean island=(x-12)*(x-12)+(z+29)*(z+29)<10||(x+12)*(x+12)+(z+40)*(z+40)<8;
            if(Math.abs(x-center)<=width||island)continue;
            for(int y=-1;y>=-5;y--){var at=b.offset(x,y,z);var s=BuildBlocks.state(l,at);
                if(s.is(Blocks.SAND)||s.is(Blocks.SANDSTONE))ready&=set(l,at,Blocks.WATER.defaultBlockState());}
        }
        for(int z=-10;z>=-24;z--)for(int x=-1;x<=1;x++){
            var at=b.offset(x,-1,z);var old=BuildBlocks.state(l,at);
            if(old.is(Blocks.SAND)||old.is(Blocks.WATER))ready&=set(l,at,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        }
        for(int side:new int[]{-1,1})for(int z:new int[]{-22,-31,-43,-57}){
            var root=b.offset(side*(z==-57?24:Math.abs(z)%9+6),-1,z);if(BuildBlocks.state(l,root).is(Blocks.SAND)||BuildBlocks.state(l,root).is(Blocks.GRASS_BLOCK))
                ready&=add(l,root.above(),Blocks.SHORT_GRASS.defaultBlockState());
        }
        BuildBlocks.after(l,()->SceneReview.dressCostume(l,b));return ready;
    }
    public static boolean apply(ServerLevel l,BlockPos origin,LabyrinthPlace p){if(p==LabyrinthPlace.ELK_CARCASSES)return true;var b=LabyrinthPlaces.base(origin,p);boolean ready=true;
        if(p==LabyrinthPlace.ELK_FAN)ready&=fan(l,b);
        if(p==LabyrinthPlace.CAMP_BLOOD)for(int[] node:new int[][]{{-4,-8},{4,-8},{10,-29},{27,-81},{-27,-77}})ready&=cover(l,b,node[0],node[1]);
        if(p==LabyrinthPlace.ELK_CARCASSES)ready&=forest(l,b);
        if(p==LabyrinthPlace.COSTUME_NIGHT)ready&=costumeLake(l,b);
        if(p==LabyrinthPlace.ZAMPANO_COURTYARD){
            for(int i=0;i<SceneReview.DRAFTS.size();i++){var at=b.offset(SceneReview.DRAFTS.get(i));var old=BuildBlocks.state(l,at);
                if(old.is(HouseBlocks.SCENE_DETAIL.get())||old.is(HouseBlocks.VIGNETTE_DETAIL.get()))ready&=set(l,at,VignetteDetailBlock.state(VignetteDetailBlock.Kind.valueOf("SCRIBBLE_"+i),Direction.SOUTH));
                else ready&=detail(l,b,SceneReview.DRAFTS.get(i),VignetteDetailBlock.Kind.valueOf("SCRIBBLE_"+i),Direction.SOUTH);
            }
            for(int i=8;i<16;i++){int z=-22-(i-8)*2;var at=b.offset(-11,4,z);var backing=at.west();
                if(!BuildBlocks.state(l,backing).getShape(l,backing).isEmpty())ready&=add(l,at,VignetteDetailBlock.state(VignetteDetailBlock.Kind.valueOf("SCRIBBLE_"+i),Direction.EAST));
            }
        }
        if(p==LabyrinthPlace.DEVILS_ROCK){
            ready&=surface(l,b,15,2,-32,FAMILY_BOARD,Direction.WEST);
            ready&=add(l,b.offset(14,0,-6),HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.WEST));
            ready&=surface(l,b,14,1,-6,RECORDER,Direction.WEST);
            ready&=surface(l,b,-13,2,-32,CHILD_FABRIC,Direction.EAST);
        }
        ready&=source(l,origin,b,p);
        return ready;
    }
    public static void fresh(ServerLevel l,BlockPos origin,LabyrinthPlace p){if(!applies(p))return;
        var d=LabyrinthData.get(l.getServer());if(d.state(STATE).getBoolean(key(origin,p)))return;var area=SceneReview.area(LabyrinthPlaces.base(origin,p),p);
        if(!d.state(SceneReview.STATE).getBoolean(key(origin,p))||!loaded(l,area)||!vacant(l,area))return;
        if(apply(l,origin,p))BuildBlocks.after(l,()->{var done=d.state(STATE);done.putBoolean(key(origin,p),true);d.setState(STATE,done);});
    }
    /** A new scene's final dressing uses the builder's bounded slices before its arrival becomes ready. */
    public static BuildBlocks.Plan prepareFresh(ServerLevel l,BlockPos origin,LabyrinthPlace p){
        if(!applies(p))return null;var d=LabyrinthData.get(l.getServer());var tag=d.state(STATE);if(tag.getBoolean(key(origin,p)))return null;
        var area=SceneReview.area(LabyrinthPlaces.base(origin,p),p);if(!loaded(l,area)||!vacant(l,area)||!d.state(SceneReview.STATE).getBoolean(key(origin,p)))return null;
        boolean[] ready={true};var plan=BuildBlocks.record(l,()->{ready[0]=apply(l,origin,p);if(ready[0])BuildBlocks.after(l,()->{
            var done=d.state(STATE);done.putBoolean(key(origin,p),true);d.setState(STATE,done);});});return ready[0]?plan:null;
    }
    public static void forget(net.minecraft.server.MinecraftServer s,BlockPos o,LabyrinthPlace p){var d=LabyrinthData.get(s);var tag=d.state(STATE);tag.remove(key(o,p));d.setState(STATE,tag);
        var sources=d.state("scene_source_originals_0455");sources.remove(key(o,p));d.setState("scene_source_originals_0455",sources);if(work!=null&&work.origin.equals(o)&&work.place==p)work=null;}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||LabyrinthBuilder.isCarving())return;
        if(work!=null){var w=work;var a=SceneReview.area(LabyrinthPlaces.base(w.origin,w.place),w.place);
            if(!w.origin.equals(HouseSavedData.get(s).houseOrigin())||!loaded(w.level,a)||!vacant(w.level,a)){work=null;return;}
            if(w.plan.tick()){var d=LabyrinthData.get(s);var done=d.state(STATE);done.putBoolean(key(w.origin,w.place),true);d.setState(STATE,done);work=null;}return;}
        if(s.getTickCount()%40!=31)return;var origin=HouseSavedData.get(s).houseOrigin();if(origin==null)return;var d=LabyrinthData.get(s);var places=LabyrinthPlace.values();
        for(int i=0;i<places.length;i++){var p=places[Math.floorMod(cursor++,places.length)];if(!applies(p)||d.state(STATE).getBoolean(key(origin,p))||!d.state(SceneReview.STATE).getBoolean(key(origin,p))||!LabyrinthBuilder.isPlaceReady(d,p))continue;
            var l=s.getLevel(NovelRooms.dimension(p));var a=SceneReview.area(LabyrinthPlaces.base(origin,p),p);if(l==null||!loaded(l,a)||!vacant(l,a))continue;
            boolean[] ready={true};var plan=BuildBlocks.record(l,()->ready[0]=apply(l,origin,p));if(ready[0])work=new Work(l,origin,p,plan);break;}
    }
}
