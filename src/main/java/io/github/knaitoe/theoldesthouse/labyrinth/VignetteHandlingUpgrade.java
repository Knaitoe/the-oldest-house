package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

/** A tiny once-only upgrade of the earlier incidental fittings, never the original source/model/recording objects. */
public final class VignetteHandlingUpgrade {
 public static final String STATE="vignette_handling_0126";
 private VignetteHandlingUpgrade(){}
 public static boolean step(ServerLevel l,BlockPos origin,LabyrinthPlace p){
  var d=LabyrinthData.get(l.getServer());String key=origin.asLong()+":"+p.id();if(d.state(STATE).getBoolean(key))return true;
  var base=LabyrinthPlaces.base(origin,p);var a=SceneReview.area(base,p);
  for(int x=(int)Math.floor(a.minX)>>4;x<=((int)Math.ceil(a.maxX)-1)>>4;x++)for(int z=(int)Math.floor(a.minZ)>>4;z<=((int)Math.ceil(a.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
  if(l.players().stream().anyMatch(r->a.inflate(24).intersects(r.getCamera().getBoundingBox())))return false;
  for(var edit:VignetteInteriors.plan(p)){
   if(!edit.next().is(LiteraryRegistry.HANDLING.get()))continue;var at=base.offset(edit.relative());var old=l.getBlockState(at);
   if(l.getBlockEntity(at)!=null||!old.is(LiteraryRegistry.INTERIOR.get())||VignetteHandlingBlock.replacement(old.getValue(VignetteInteriorBlock.KIND))!=edit.next().getValue(VignetteHandlingBlock.KIND))continue;
   if(!l.getEntities((Entity)null,new AABB(at).inflate(.02),Entity::isAlive).isEmpty())return false;
   boolean active=old.getValue(VignetteInteriorBlock.ACTIVE);l.setBlock(at,edit.next(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
   if(active&&l.getBlockEntity(at) instanceof VignetteHandlingBlockEntity be)be.restoreOrdinaryState();
  }
  var tag=d.state(STATE);tag.putBoolean(key,true);d.setState(STATE,tag);return true;
 }
 public static void tick(MinecraftServer s){
  if(s instanceof net.minecraft.gametest.framework.GameTestServer||LabyrinthBuilder.isCarving()||s.getTickCount()%40!=0)return;
  var o=HouseSavedData.get(s).houseOrigin();if(o==null||!o.equals(LabyrinthData.get(s).builtOrigin()))return;
  for(var p:VignetteInteriors.PLACES){if(LabyrinthData.get(s).door(p.entryDoorId())==null)continue;var l=s.getLevel(NovelRooms.dimension(p));if(l!=null)step(l,o,p);}
 }
}
