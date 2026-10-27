package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Extend the existing passage behind its old end wall before opening the join. */
public final class TunnelPassage {
    public static final String STATE="tunnel_passage134";
    public static final int WALL=-145,PASS=-138;
    public static final BlockPos ACCOUNT=new BlockPos(3,1,-141),OLD_ACCOUNT=new BlockPos(3,1,-69);
    private record Edit(BlockPos rel,BlockState before,BlockState after){}
    private static Work work;
    private static final class Work {
        final ServerLevel l;final BlockPos b;final List<Edit> edits=new ArrayList<>(),join=new ArrayList<>();
        Work(ServerLevel l,BlockPos b){this.l=l;this.b=b.immutable();
            var before=BuildBlocks.record(l,()->UncannyRooms0132.build(l,b,LabyrinthPlace.TUNNEL_PERIPHERY));
            var after=BuildBlocks.record(l,()->UncannyRooms.build(l,b,LabyrinthPlace.TUNNEL_PERIPHERY));
            var r=LabyrinthPlace.TUNNEL_PERIPHERY.room();
            for(int z=r.minZ();z<=r.maxZ();z++)for(int y=r.minY();y<=r.maxY();y++)for(int x=r.minX();x<=r.maxX();x++){
                var rel=new BlockPos(x,y,z);var at=b.offset(rel);var old=before.plannedState(at);var next=after.plannedState(at);if(next==null||next.equals(old))continue;
                var e=new Edit(rel,old,next);
                if(z==-73&&Math.abs(x)<=2&&y>=0&&y<=2||rel.equals(OLD_ACCOUNT)||rel.equals(ACCOUNT)||rel.equals(new BlockPos(0,1,-72)))join.add(e);else edits.add(e);
            }
        }
    }
    private TunnelPassage(){}
    public static boolean extended(ServerLevel l,BlockPos b){return LabyrinthData.get(l.getServer()).stateEntry(STATE,Long.toString(b.asLong())).getBoolean("Done");}
    public static void fresh(ServerLevel l,BlockPos b){BuildBlocks.after(l,()->{var d=LabyrinthData.get(l.getServer());var t=d.stateEntry(STATE,Long.toString(b.asLong()));t.putBoolean("Done",true);d.setStateEntry(STATE,Long.toString(b.asLong()),t);});}
    public static int pass(ServerLevel l,BlockPos b){return extended(l,b)?PASS:-66;}
    public static double exit(ServerLevel l,BlockPos b){return extended(l,b)?-142.5:-70.5;}
    public static BlockPos account(ServerLevel l,BlockPos b){
        if(!extended(l,b)||!l.hasChunkAt(b.offset(ACCOUNT)))return OLD_ACCOUNT;
        var s=l.getBlockState(b.offset(ACCOUNT));return s.is(UncannyRegistry.PROP.get())&&s.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.LEDGER?ACCOUNT:OLD_ACCOUNT;
    }
    private static boolean safe(ServerLevel l,BlockPos b){
        var r=LabyrinthPlace.TUNNEL_PERIPHERY.room();var area=new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(b.offset(r.minX(),r.minY(),r.minZ())),net.minecraft.world.phys.Vec3.atLowerCornerOf(b.offset(r.maxX()+1,r.maxY()+1,r.maxZ()+1)));
        for(int x=(int)Math.floor(area.minX)>>4;x<=((int)Math.ceil(area.maxX)-1)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=((int)Math.ceil(area.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        if(l.players().stream().anyMatch(p->area.inflate(12).intersects(p.getCamera().getBoundingBox())))return false;
        return l.getEntitiesOfClass(LivingEntity.class,area,e->e.isAlive()&&!(e instanceof LiteraryActor a&&a.scene().equals(LabyrinthPlace.TUNNEL_PERIPHERY.id()))).isEmpty();
    }
    private static void cell(Work w,Edit e){
        var at=w.b.offset(e.rel);var actual=w.l.getBlockState(at);var be=w.l.getBlockEntity(at);
        boolean oldSignal=e.rel.equals(new BlockPos(0,1,-72))&&actual.is(UncannyRegistry.PROP.get())&&actual.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.STOPLIGHT&&be instanceof UncannyPropBlock.Fitting f&&f.meal.isEmpty();
        if(actual.equals(e.after)||be!=null&&!oldSignal)return;
        boolean virgin=e.rel.getZ()< -75&&(actual.isAir()||actual.is(Blocks.STONE));
        if(!virgin&&!actual.equals(e.before))return;
        if(!w.l.getEntitiesOfClass(LivingEntity.class,new AABB(at).inflate(.01),LivingEntity::isAlive).isEmpty())return;
        w.l.setBlock(at,e.after,UncannyRooms.FLAGS);
    }
    /** Bounded and restartable. Never executes construction callbacks or replaces existing actors. */
    public static boolean repair(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());String key=Long.toString(b.asLong());var state=d.stateEntry(STATE,key);if(state.getBoolean("Done"))return true;if(!safe(l,b))return false;
        if(work==null||work.l!=l||!work.b.equals(b))work=new Work(l,b);
        int cursor=state.getInt("Cursor"),visits=0;long until=System.nanoTime()+3_000_000;
        while(cursor<work.edits.size()&&visits++<256&&(visits==1||System.nanoTime()<until))cell(work,work.edits.get(cursor++));
        state.putInt("Cursor",cursor);d.setStateEntry(STATE,key,state);if(cursor<work.edits.size())return false;
        // Native paper originals live in each reader's SavedData. Carry the actual
        // fitting metadata too, and open the old wall only after the far half is complete.
        var oldAt=b.offset(OLD_ACCOUNT);var old=l.getBlockState(oldAt);var be=l.getBlockEntity(oldAt);net.minecraft.nbt.CompoundTag paper=null;
        boolean move=old.is(UncannyRegistry.PROP.get())&&old.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.LEDGER&&be instanceof UncannyPropBlock.Fitting f&&f.meal.isEmpty();
        if(move)paper=be.saveWithFullMetadata(l.registryAccess());
        for(var e:work.join)if(!e.rel.equals(OLD_ACCOUNT)&&!e.rel.equals(ACCOUNT))cell(work,e);
        if(move){
            var target=b.offset(ACCOUNT);var current=l.getBlockState(target);if(l.getBlockEntity(target)==null&&(current.isAir()||current.is(Blocks.STONE)||current.is(UncannyRegistry.material("workshop_wall")))){
                l.setBlock(target,old,UncannyRooms.FLAGS);if(l.getBlockEntity(target) instanceof UncannyPropBlock.Fitting next){next.loadWithComponents(paper,l.registryAccess());next.setChanged();l.setBlock(oldAt,UncannyRegistry.material("workshop_wall").defaultBlockState(),UncannyRooms.FLAGS);}
            }
        }
        state.putBoolean("Done",true);d.setStateEntry(STATE,key,state);work=null;return true;
    }
    public static void tick(MinecraftServer s){
        if(s instanceof net.minecraft.gametest.framework.GameTestServer||LabyrinthBuilder.isCarving()||s.getTickCount()%2!=0)return;var o=HouseSavedData.get(s).houseOrigin();if(o==null)return;
        var d=LabyrinthData.get(s);if(!o.equals(d.builtOrigin())||!LabyrinthBuilder.isPlaceReady(d,LabyrinthPlace.TUNNEL_PERIPHERY))return;
        var l=s.getLevel(NovelRooms.dimension(LabyrinthPlace.TUNNEL_PERIPHERY));if(l!=null)repair(l,LabyrinthPlaces.base(o,LabyrinthPlace.TUNNEL_PERIPHERY));
    }
}
