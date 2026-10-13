package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** A placed original keeps its owner and every item component. Native death/respawn remains authoritative. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LakeHouseLightBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<LakeHouseLightBlock> CODEC=simpleCodec(LakeHouseLightBlock::new);
    public static final BooleanProperty LIT=BlockStateProperties.LIT,WATERLOGGED=BlockStateProperties.WATERLOGGED;
    public LakeHouseLightBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(LIT,true).setValue(WATERLOGGED,false));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LIT,WATERLOGGED);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return box(4,0,4,12,14,12);}
    @Override protected FluidState getFluidState(BlockState s){return s.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(s);}
    @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,LevelAccessor l,BlockPos at,BlockPos next){if(s.getValue(WATERLOGGED))l.scheduleTick(at,Fluids.WATER,Fluids.WATER.getTickDelay(l));return super.updateShape(s,d,n,l,at,next);}
    @Override public BlockEntity newBlockEntity(BlockPos at,BlockState s){return new Lamp(at,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return l.isClientSide?null:createTickerHelper(type,LakeHouseRegistry.HOME_LIGHT_ENTITY.get(),Lamp::tick);}
    @Override protected void onRemove(BlockState s,Level l,BlockPos at,BlockState next,boolean moving){
        if(!s.is(next.getBlock())&&!l.isClientSide&&l.getBlockEntity(at) instanceof Lamp lamp&&!lamp.original.isEmpty()){
            var item=new ItemEntity(l,at.getX()+.5,at.getY()+.3,at.getZ()+.5,lamp.original.copy());var d=lamp.original.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if(d!=null&&d.copyTag().hasUUID(LiteraryVignettes.OWNER))item.setTarget(d.copyTag().getUUID(LiteraryVignettes.OWNER));lamp.original=ItemStack.EMPTY;l.addFreshEntity(item);
        }super.onRemove(s,l,at,next,moving);
    }
    public static final class Lamp extends BlockEntity {
        private ItemStack original=ItemStack.EMPTY;
        public Lamp(BlockPos at,BlockState s){super(LakeHouseRegistry.HOME_LIGHT_ENTITY.get(),at,s);}
        public void original(ItemStack s){original=s.copyWithCount(1);setChanged();}
        public ItemStack original(){return original.copy();}
        @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);if(!original.isEmpty())t.put("Original",original.save(r));}
        @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);original=ItemStack.parseOptional(r,t.getCompound("Original"));}
        static void tick(Level l,BlockPos at,BlockState s,Lamp lamp){
            if(l.getGameTime()%20!=0)return;var d=lamp.original.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);if(d==null||!d.copyTag().hasUUID(LiteraryVignettes.OWNER))return;
            var p=l.getPlayerByUUID(d.copyTag().getUUID(LiteraryVignettes.OWNER));boolean on=!l.isDay()&&p!=null&&p.distanceToSqr(at.getCenter())<64*64;
            if(on!=s.getValue(LIT))l.setBlock(at,s.setValue(LIT,on),Block.UPDATE_ALL);
            if(on&&p instanceof ServerPlayer r&&l.getGameTime()%2400==0)ThreeFingers.hear(r,net.minecraft.sounds.SoundEvents.WET_GRASS_STEP,at.below(),.22F,.75F);
        }
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
    public static void death(LivingDeathEvent e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||HouseDimensions.isHouseDimension(p.level().dimension()))return;var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LakeHouse.PLACE);if(own.contains("HomeLight")){own.putBoolean("LightRespawn",true);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),LakeHouse.PLACE,own);}}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.isEndConquered())return;var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LakeHouse.PLACE);if(!own.getBoolean("LightRespawn"))return;
        own.putBoolean("LightRespawn",false);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),LakeHouse.PLACE,own);var at=BlockPos.of(own.getLong("HomeLight"));var l=p.server.overworld();
        // Respawning is allowed to load its actual home chunk, just as a vanilla bed respawn does.
        l.getChunkAt(at);if(!(l.getBlockEntity(at) instanceof Lamp lamp)||!LakeHouseArtifactItem.owned(lamp.original,p))return;
        for(var feet:new BlockPos[]{at.below(2),at.below(),at.above(),at.north(),at.south(),at.east(),at.west()}){
            var box=p.getDimensions(net.minecraft.world.entity.Pose.STANDING).makeBoundingBox(feet.getX()+.5,feet.getY(),feet.getZ()+.5);if(!l.noCollision(p,box))continue;
            p.teleportTo(l,feet.getX()+.5,feet.getY(),feet.getZ()+.5,p.getYRot(),0);p.resetFallDistance();return;
        }
    }
}
