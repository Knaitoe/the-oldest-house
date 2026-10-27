package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** Native car components. No creative item and no story deal until the scene is complete. */
public final class BackseatRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> IDLE=sound("idle"),RADIO=sound("radio"),WIPER=sound("wiper"),LATCH=sound("latch"),LOCK=sound("lock"),RISE=sound("rise"),GLASS=sound("glass"),IMPACT=sound("impact"),WIND=sound("wind");
    private static DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> sound(String name){return SOUNDS.register("backseat."+name,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"backseat."+name)));}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<BackseatCarBlock>> CAR_TYPE=CODECS.register("backseat_car",()->BackseatCarBlock.CODEC);
    public static final DeferredBlock<BackseatCarBlock> CAR=BLOCKS.registerBlock("backseat_car",BackseatCarBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(-1,3600000).noOcclusion().dynamicShape().noLootTable().lightLevel(s->{if(s.getValue(BackseatCarBlock.RUNNING))return 0;int cell=s.getValue(BackseatCarBlock.CELL);var at=BackseatCarCells.offset(cell);return at.getZ()==-3&&at.getY()==0&&Math.abs(at.getX())==1?13:cell==BackseatCarCells.ROOT?(s.getValue(BackseatCarBlock.OPEN)?9:1):0;}));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<BackseatCarBlock.Car>> CAR_ENTITY=BES.register("backseat_car",()->BlockEntityType.Builder.of(BackseatCarBlock.Car::new,CAR.get()).build(null));
    public static final DeferredHolder<EntityType<?>,EntityType<BackseatSeat>> SEAT=ENTITIES.register("backseat_seat",()->EntityType.Builder.<BackseatSeat>of(BackseatSeat::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(10).updateInterval(1).build("backseat_seat"));
    private BackseatRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BES.register(bus);ENTITIES.register(bus);SOUNDS.register(bus);}
}
