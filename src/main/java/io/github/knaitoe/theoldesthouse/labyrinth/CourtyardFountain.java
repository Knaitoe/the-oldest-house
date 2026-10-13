package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.SceneDetailBlock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.77: Zampano's courtyard gets courtyard things. Stone planters stand where four bedside tables with a lamp and a vase
 * used to, and a small fountain sits in the open ground between the gate and the mat: a sunk basin, a low kerb, a pedestal
 * and a bowl that drips. One fish swims in the basin. A reader may take it with a bare hand, once; it is an ordinary raw cod
 * and the cat on the mat will take it as it takes any meat. If the fish is gone and the cat is still waiting, another is in
 * the basin when a reader who has not had one arrives; once the cat has come off the mat, no more come. Nothing else is
 * restocked. Saved courtyards are changed once ({@value #STATE}), only where cells still hold what the House put there, and
 * only when loaded, unwatched and clear.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class CourtyardFountain {
    public static final String STATE="courtyard_0477",FISH="CourtyardFish0477";
    static final LabyrinthPlace PLACE=LabyrinthPlace.ZAMPANO_COURTYARD;
    /** The basin's middle and the bowl on its pedestal. */
    public static final BlockPos CENTRE=new BlockPos(0,-1,-7),BOWL=new BlockPos(0,1,-7);
    private static final int[][] PLANTERS={{-13,-6},{13,-6},{-13,-14},{13,-14}};
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private CourtyardFountain(){}

    // ------------------------------------------------------------------------------------------------ the pieces
    public static Map<BlockPos,BlockState> fountain(BlockPos b){
        var out=new LinkedHashMap<BlockPos,BlockState>();var c=b.offset(CENTRE);
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){out.put(c.offset(dx,-1,dz),Blocks.MOSSY_STONE_BRICKS.defaultBlockState());out.put(c.offset(dx,0,dz),Blocks.WATER.defaultBlockState());}
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)if(Math.max(Math.abs(dx),Math.abs(dz))==2)
            out.put(c.offset(dx,1,dz),Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
        out.put(c,Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState().setValue(WallBlock.WATERLOGGED,true));
        out.put(c.above(),Blocks.STONE_BRICK_WALL.defaultBlockState());
        out.put(b.offset(BOWL),Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,3));
        return out;
    }
    public static Map<BlockPos,BlockState> planters(BlockPos b){
        var out=new LinkedHashMap<BlockPos,BlockState>();
        for(int i=0;i<PLANTERS.length;i++){var at=b.offset(PLANTERS[i][0],0,PLANTERS[i][1]);
            out.put(at,Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState());out.put(at.above(),(i<2?Blocks.POTTED_AZALEA:Blocks.POTTED_FERN).defaultBlockState());}
        return out;
    }
    /** New courtyards: built with them, and the checkpoint marked. */
    public static void build(ServerLevel l,BlockPos b){
        for(var e:fountain(b).entrySet())BuildBlocks.set(l,e.getKey(),e.getValue(),F);
        for(var e:planters(b).entrySet())BuildBlocks.set(l,e.getKey(),e.getValue(),F);
        markDone(LabyrinthData.get(l.getServer()),b);
    }
    static void markDone(LabyrinthData d,BlockPos b){var s=d.state(STATE);s.putBoolean(Long.toString(b.asLong()),true);d.setState(STATE,s);}
    public static boolean done(LabyrinthData d,BlockPos b){return d.state(STATE).getBoolean(Long.toString(b.asLong()));}
    public static boolean bowl(BlockState s){return s.is(Blocks.WATER_CAULDRON)||s.is(HouseBlocks.VIGNETTE_DETAIL.get())&&s.getValue(io.github.knaitoe.theoldesthouse.house.VignetteDetailBlock.KIND)==io.github.knaitoe.theoldesthouse.house.VignetteDetailBlock.Kind.FOUNTAIN_BOWL;}

    // ------------------------------------------------------------------------------------------------ saved courtyards
    private static final Set<HouseholdFurnitureBlock.Kind> TABLES=Set.of(HouseholdFurnitureBlock.Kind.WALNUT_DESK,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,HouseholdFurnitureBlock.Kind.READING_DESK,HouseholdFurnitureBlock.Kind.FORMICA_TABLE);
    private static boolean table(BlockState s){return s.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())&&TABLES.contains(s.getValue(HouseholdFurnitureBlock.KIND));}
    private static boolean top(BlockState s){return s.isAir()||s.is(HouseBlocks.SCENE_DETAIL.get())&&Set.of(SceneDetailBlock.Kind.TABLE_LAMP,SceneDetailBlock.Kind.VASE).contains(s.getValue(SceneDetailBlock.KIND));}
    public static boolean repair(ServerLevel l,BlockPos b){
        var data=LabyrinthData.get(l.getServer());if(done(data,b))return true;
        var fountain=fountain(b);var planters=planters(b);
        var cells=new java.util.ArrayList<BlockPos>(fountain.keySet());cells.addAll(planters.keySet());
        for(var at:cells)if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return false;
        var area=new AABB(Vec3.atLowerCornerOf(b.offset(-14,-2,-15)),Vec3.atLowerCornerOf(b.offset(15,3,-4)));
        if(l.players().stream().anyMatch(p->area.inflate(16).intersects(p.getCamera().getBoundingBox())))return false;
        var edits=new LinkedHashMap<BlockPos,BlockState>();
        for(var e:planters.entrySet())if(e.getValue().getBlock() instanceof WallBlock&&table(l.getBlockState(e.getKey()))&&top(l.getBlockState(e.getKey().above()))){edits.put(e.getKey(),e.getValue());edits.put(e.getKey().above(),planters.get(e.getKey().above()));}
        // The fountain goes in only where the ground and the air above it are still as the House left them.
        var c=b.offset(CENTRE);boolean open=true;
        for(var at:fountain.keySet()){var s=l.getBlockState(at);int y=at.getY()-c.getY();open&=y<=0?s.isSolidRender(l,at)||s.isAir()&&y<0:s.isAir();}
        if(open)edits.putAll(fountain);
        for(var at:edits.keySet())if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(at).inflate(.3),e->e.isAlive()).isEmpty()||l.getBlockEntity(at)!=null&&!l.getBlockState(at).is(HouseBlocks.HOUSEHOLD_FURNITURE.get()))return false;
        for(var e:edits.entrySet())l.setBlock(e.getKey(),e.getValue(),F);
        markDone(data,b);return true;
    }
    @SubscribeEvent public static void repairTick(ServerTickEvent.Post e){
        var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=31||LabyrinthBuilder.isCarving()||!LabyrinthBuilder.isPlaceReady(s,PLACE))return;
        var origin=HouseSavedData.get(s).houseOrigin();var l=s.getLevel(NovelRooms.dimension(PLACE));var b=origin==null?null:LabyrinthPlaces.base(origin,PLACE);
        if(l!=null&&b!=null&&!done(LabyrinthData.get(s),b))repair(l,b);
    }

    // ------------------------------------------------------------------------------------------------ the fish
    /** On a reader's arrival: one fish in the basin, unless the cat is off the mat or this reader has already had one. */
    public static void ensureFish(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();var data=LabyrinthData.get(p.server);var all=data.state(NovelVignettes.STATE);
        if(!l.getBlockState(b.offset(CENTRE).north()).is(Blocks.WATER))return;
        if(all.hasUUID(FISH)&&l.getEntity(all.getUUID(FISH)) instanceof Cod alive&&alive.isAlive())return;
        if(all.getBoolean("CatCoaxed")||NovelVignettes.personal(data,p.getUUID()).getBoolean(FISH))return;
        var fish=EntityType.COD.create(l);if(fish==null)return;
        Vec3 at=Vec3.atBottomCenterOf(b.offset(CENTRE).offset(1,0,1)).add(0,.2,0);fish.moveTo(at.x,at.y,at.z,0,0);
        fish.setPersistenceRequired();fish.setInvulnerable(true);fish.addTag(FISH);l.addFreshEntity(fish);
        all.putUUID(FISH,fish.getUUID());data.setState(NovelVignettes.STATE,all);
    }
    /** A bare hand takes the fish; a bucket does not. */
    @SubscribeEvent(priority=EventPriority.HIGH) public static void grab(PlayerInteractEvent.EntityInteract e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getTarget() instanceof Cod fish)||!fish.getTags().contains(FISH))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(e.getHand()!=InteractionHand.MAIN_HAND||p.isSpectator()||!fish.isAlive()||fish.distanceToSqr(p)>16)return;
        if(!p.getMainHandItem().isEmpty()){p.displayClientMessage(Component.literal(p.getMainHandItem().is(Items.WATER_BUCKET)?"It slips away from the bucket.":"Your hands are full."),true);return;}
        var data=LabyrinthData.get(p.server);var own=NovelVignettes.personal(data,p.getUUID());
        if(own.getBoolean(FISH)){p.displayClientMessage(Component.literal("You have had your fish."),true);return;}
        fish.discard();var l=p.serverLevel();
        l.playSound(null,fish.getX(),fish.getY(),fish.getZ(),SoundEvents.FISH_SWIM,SoundSource.NEUTRAL,.8F,1.1F);
        l.sendParticles(ParticleTypes.SPLASH,fish.getX(),fish.getY()+.6,fish.getZ(),12,.3,.1,.3,.1);
        p.setItemInHand(InteractionHand.MAIN_HAND,VignetteYields.mark(new ItemStack(Items.COD),PLACE.id()));
        own.putBoolean(FISH,true);NovelVignettes.save(data,p.getUUID(),own);
        p.displayClientMessage(Component.literal("You close your hand on the fish. It is cold, and it does not fight."),true);
    }

    // ------------------------------------------------------------------------------------------------ the sound of it
    @SubscribeEvent public static void ambience(ServerTickEvent.Post e){
        var s=e.getServer();if(s.getTickCount()%10!=0)return;var origin=HouseSavedData.get(s).houseOrigin();if(origin==null)return;
        var l=s.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;var b=LabyrinthPlaces.base(origin,PLACE);var bowl=b.offset(BOWL);
        if(!l.hasChunkAt(bowl)||!bowl(l.getBlockState(bowl))||l.players().stream().noneMatch(p->p.distanceToSqr(Vec3.atCenterOf(bowl))<400))return;
        // The continuous animated jet is part of the native bowl mesh; moving droplets follow the nozzle and overflow.
        l.sendParticles(ParticleTypes.SPLASH,bowl.getX()+.5,bowl.getY()+.95,bowl.getZ()+.5,3,.035,.10,.035,.025);
        for(int i=0;i<2;i++){double a=l.random.nextDouble()*Math.PI*2;l.sendParticles(ParticleTypes.DRIPPING_WATER,bowl.getX()+.5+Math.cos(a)*.48,bowl.getY()+.56,bowl.getZ()+.5+Math.sin(a)*.48,1,0,0,0,0);}
        if(s.getTickCount()%60==0)l.playSound(null,bowl,SoundEvents.WATER_AMBIENT,SoundSource.BLOCKS,.35F,1.3F);
    }
}
