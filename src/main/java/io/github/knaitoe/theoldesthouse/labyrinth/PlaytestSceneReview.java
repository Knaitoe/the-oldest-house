package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.Set;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** One bounded 0.4.64 repair after prior composition. Never rebuild a scene or its supplies. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PlaytestSceneReview {
    public static final String STATE="scene_playtest_0464";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private record Work(ServerLevel level,BlockPos origin,LabyrinthPlace place,BuildBlocks.Plan plan){}
    private static Work work;
    private static String key(BlockPos o,LabyrinthPlace p){return o.asLong()+":"+p.id();}
    /** The 0.4.55 guards (block entities, living bodies, costume stands and the lake witch), only on loaded native sections. */
    private static boolean safe(ServerLevel l,BlockPos at,BlockState next,boolean lectern){
        return l.hasChunkAt(at)&&l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4))&&SceneHuntReview.safe(l,at,next,lectern);
    }
    private static boolean set(ServerLevel l,BlockPos at,BlockState next){return BuildBlocks.state(l,at).equals(next)||safe(l,at,next,false)&&BuildBlocks.guardedSet(l,at,next,F,()->safe(l,at,next,false));}
    public static boolean courtyard(ServerLevel l,BlockPos b){boolean ready=true;
        for(int x:new int[]{-8,8}){
            var at=b.offset(x,0,-12);var old=BuildBlocks.state(l,at);
            if(!old.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())||!Set.of(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,HouseholdFurnitureBlock.Kind.WALNUT_DESK,HouseholdFurnitureBlock.Kind.READING_DESK,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,HouseholdFurnitureBlock.Kind.CHESS_TABLE).contains(old.getValue(HouseholdFurnitureBlock.KIND)))continue;
            ready&=set(l,at,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CHESS_TABLE,Direction.SOUTH));
            var top=at.above();old=BuildBlocks.state(l,top);
            if(old.is(HouseBlocks.SCENE_DETAIL.get())&&Set.of(SceneDetailBlock.Kind.VASE,SceneDetailBlock.Kind.TABLE_LAMP).contains(old.getValue(SceneDetailBlock.KIND)))
                ready&=set(l,top,VignetteDetailBlock.state(VignetteDetailBlock.Kind.CHESS,Direction.NORTH));
            else if(old.isAir())ready&=set(l,top,VignetteDetailBlock.state(VignetteDetailBlock.Kind.CHESS,Direction.NORTH));
            for(int z:new int[]{-10,-14}){var seat=b.offset(x,0,z);old=BuildBlocks.state(l,seat);
                if(old.isAir()||old.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())&&old.getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.CANE_CHAIR)
                    ready&=set(l,seat,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CANE_CHAIR,z==-10?Direction.NORTH:Direction.SOUTH));}
        }
        // Retain the papers as groups on a plaster wall, rather than wallpaper on every face.
        for(int y=0;y<=7;y++)for(int z=-39;z<=-19;z++)for(int x=-12;x<=12;x++){
            if(x!=-12&&x!=12&&z!=-39&&z!=-19)continue;var at=b.offset(x,y,z);var old=BuildBlocks.state(l,at);
            if(old.is(NovelRegistry.PAPER.get())&&!paperPatch(x,y,z))ready&=set(l,at,NovelRegistry.PLASTER.get().defaultBlockState());
        }
        return ready;
    }
    public static boolean paperPatch(int x,int y,int z){return z==-39&&y>=2&&y<=4&&(x>=-8&&x<=-5||x>=-1&&x<=2||x>=6&&x<=8);}
    private static boolean diaries(BlockState s){return s.is(HouseBlocks.VIGNETTE_DETAIL.get())&&s.getValue(VignetteDetailBlock.KIND)==VignetteDetailBlock.Kind.DIARY_STACK;}
    public static boolean cabin(ServerLevel l,BlockPos origin,BlockPos b){
        var low=b.offset(-8,0,-31);var high=low.above();var old=BuildBlocks.state(l,low);var top=BuildBlocks.state(l,high);
        var table=HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH);var notes=VignetteDetailBlock.state(VignetteDetailBlock.Kind.DIARY_STACK,Direction.SOUTH);
        if(!diaries(old)&&!old.is(Blocks.LECTERN))return true;
        // The 0.4.55 pass, run after the address moved up, lays the notes on top of the old lectern itself.
        if(!top.isAir()&&!diaries(top)||!safe(l,low,table,true)||!safe(l,high,notes,false))return false;
        if(l.getBlockEntity(low) instanceof LecternBlockEntity lectern){var book=lectern.getBook().copy();if(!book.isEmpty())BuildBlocks.after(l,()->{
            var d=LabyrinthData.get(l.getServer());var originals=d.state("scene_source_originals_0455");String k=key(origin,LabyrinthPlace.END_WORLD_CABIN);
            if(!originals.contains(k))originals.put(k,book.save(l.registryAccess()));d.setState("scene_source_originals_0455",originals);});}
        boolean done=BuildBlocks.guardedSet(l,low,table,F,()->{if(!safe(l,low,table,true))return false;SceneHuntReview.emptyLectern(l,low);return true;});
        return done&&set(l,high,notes);
    }
    public static boolean apply(ServerLevel l,BlockPos origin,LabyrinthPlace p){var b=LabyrinthPlaces.base(origin,p);return p==LabyrinthPlace.ZAMPANO_COURTYARD?courtyard(l,b):p!=LabyrinthPlace.END_WORLD_CABIN||cabin(l,origin,b);}
    private static boolean available(ServerLevel l,BlockPos o,LabyrinthPlace p){var a=SceneReview.area(LabyrinthPlaces.base(o,p),p);
        for(int x=(int)Math.floor(a.minX)>>4;x<=(int)Math.floor(a.maxX)>>4;x++)for(int z=(int)Math.floor(a.minZ)>>4;z<=(int)Math.floor(a.maxZ)>>4;z++)
            if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;
        return l.players().stream().noneMatch(player->a.inflate(48).intersects(player.getCamera().getBoundingBox()));
    }
    public static void forget(net.minecraft.server.MinecraftServer s,BlockPos o,LabyrinthPlace p){var d=LabyrinthData.get(s);var t=d.state(STATE);t.remove(key(o,p));d.setState(STATE,t);if(work!=null&&work.origin.equals(o)&&work.place==p)work=null;}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||LabyrinthBuilder.isCarving())return;
        if(work!=null){var w=work;if(!w.origin.equals(HouseSavedData.get(s).houseOrigin())||!available(w.level,w.origin,w.place)){work=null;return;}
            if(w.plan.tick()){var d=LabyrinthData.get(s);var t=d.state(STATE);t.putBoolean(key(w.origin,w.place),true);d.setState(STATE,t);work=null;}return;}
        if(s.getTickCount()%40!=31)return;var o=HouseSavedData.get(s).houseOrigin();if(o==null)return;var d=LabyrinthData.get(s);
        for(var p:new LabyrinthPlace[]{LabyrinthPlace.ZAMPANO_COURTYARD,LabyrinthPlace.END_WORLD_CABIN}){var l=s.getLevel(NovelRooms.dimension(p));
            if(l==null||d.state(STATE).getBoolean(key(o,p))||!d.state(SceneHuntReview.STATE).getBoolean(key(o,p))||!available(l,o,p))continue;
            boolean[] ready={true};var plan=BuildBlocks.record(l,()->ready[0]=apply(l,o,p));if(ready[0])work=new Work(l,o,p,plan);break;}
    }
}
