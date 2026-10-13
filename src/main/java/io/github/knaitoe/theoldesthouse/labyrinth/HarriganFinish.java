package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.HarriganFinishBlock.Kind.*;
/** One vacant, bounded finish; later edits and original actors remain intact. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HarriganFinish {
    private static final String STATE="harrigan_finish0490";
    private static final Map<ServerLevel,BuildBlocks.Plan> PLANS=new WeakHashMap<>();
    private HarriganFinish(){}
    public static boolean safe(ServerLevel l,BlockPos b){var box=new AABB(Vec3.atLowerCornerOf(b.offset(-8,-1,-25)),Vec3.atLowerCornerOf(b.offset(9,6,-12)));for(int x=(int)box.minX>>4;x<=(int)box.maxX>>4;x++)for(int z=(int)box.minZ>>4;z<=(int)box.maxZ>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;return l.players().stream().noneMatch(p->box.inflate(32).intersects(p.getCamera().getBoundingBox()));}
    private static void set(ServerLevel l,BlockPos b,BlockPos relative,BlockState wanted,boolean fresh){var at=b.offset(relative);if(fresh){BuildBlocks.set(l,at,wanted,18);return;}if(l.getBlockEntity(at)!=null)return;var old=l.getBlockState(at);boolean expected=old.is(Blocks.DARK_OAK_PLANKS)||old.is(Blocks.SPRUCE_PLANKS)||old.is(Blocks.DARK_OAK_SLAB)||old.is(Blocks.DARK_OAK_TRAPDOOR)||old.is(Blocks.WHITE_CARPET);if(!expected)return;var shape=wanted.getCollisionShape(l,at);if(shape.toAabbs().stream().anyMatch(box->!l.getEntitiesOfClass(LivingEntity.class,box.move(at),LivingEntity::isAlive).isEmpty()))return;BuildBlocks.protectedSet(l,at,wanted,18,()->safe(l,b)&&l.getBlockEntity(at)==null&&l.getBlockState(at).equals(old)&&shape.toAabbs().stream().allMatch(box->l.getEntitiesOfClass(LivingEntity.class,box.move(at),LivingEntity::isAlive).isEmpty()),Set.of());}
    private static void finish(ServerLevel l,BlockPos b,boolean fresh){for(int x=-7;x<=7;x++)for(int z=-24;z<=-13;z++){if(x>-7&&x<7&&z>-24&&z<-13)set(l,b,new BlockPos(x,-1,z),HarriganFinishBlock.of(CARPET),fresh);if(Math.abs(x)==7||z==-24||z==-13)for(int y=0;y<=4;y++){if(z==-13&&x==0&&y<2)continue;set(l,b,new BlockPos(x,y,z),HarriganFinishBlock.of(y<2?PANEL:WALLPAPER),fresh);}}for(int x=-2;x<=2;x++)for(int z=-22;z<=-19;z++){if(!fresh)set(l,b,new BlockPos(x,0,z),Blocks.AIR.defaultBlockState(),false);if(Math.abs(x)==2)set(l,b,new BlockPos(x,1,z),Blocks.AIR.defaultBlockState(),fresh);}for(int z=-22;z<=-21;z++)set(l,b,new BlockPos(0,1,z),Blocks.AIR.defaultBlockState(),fresh);set(l,b,HarriganVignette.CASKET,HarriganFinishBlock.of(CASKET),fresh);}
    private static void mark(ServerLevel l,BlockPos b){var d=LabyrinthData.get(l.getServer());var t=d.state(STATE);t.putBoolean(Long.toString(b.asLong()),true);d.setState(STATE,t);}
    public static void build(ServerLevel l,BlockPos b){finish(l,b,true);BuildBlocks.after(l,()->mark(l,b));}
    public static boolean repair(ServerLevel l,BlockPos b){if(LabyrinthData.get(l.getServer()).state(STATE).getBoolean(Long.toString(b.asLong())))return true;if(!safe(l,b))return false;var plan=PLANS.computeIfAbsent(l,world->BuildBlocks.record(world,()->finish(world,b,false)));if(plan.tick()){PLANS.remove(l);mark(l,b);return true;}return false;}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%5!=3||LabyrinthBuilder.isCarving()||!LabyrinthBuilder.isPlaceReady(s,LabyrinthPlace.HARRIGAN))return;var o=HouseSavedData.get(s).houseOrigin();var l=s.getLevel(HouseDimensions.INTERIOR);if(o!=null&&l!=null)repair(l,LabyrinthPlaces.base(o,LabyrinthPlace.HARRIGAN));}
    @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent ignored){PLANS.clear();}
}
