package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** Original surfaces and the one continuously positioned industrial recording. No victim items. */
public final class BorrascaRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<BorrascaFittingBlock>> FITTING_TYPE=CODECS.register("borrasca_fitting",()->BorrascaFittingBlock.CODEC);
    public static final DeferredBlock<BorrascaFittingBlock> FITTING=BLOCKS.registerBlock("borrasca_fitting",BorrascaFittingBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(-1,3600000).noOcclusion().noLootTable());
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<BorrascaMachineBlock>> MACHINE_TYPE=CODECS.register("borrasca_machine",()->BorrascaMachineBlock.CODEC);
    public static final DeferredBlock<BorrascaMachineBlock> MACHINE=BLOCKS.registerBlock("borrasca_machine",BorrascaMachineBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(-1,3600000).noOcclusion().noLootTable());
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<BorrascaMachineBlock.Machine>> MACHINE_ENTITY=BES.register("borrasca_machine",()->BlockEntityType.Builder.of(BorrascaMachineBlock.Machine::new,MACHINE.get()).build(null));
    public static final DeferredHolder<SoundEvent,SoundEvent> GRINDER=sound("grinder"),GRINDER_MUFFLED=sound("grinder_muffled"),MOTOR_STOP=sound("motor_stop"),TREE_CREAK=sound("tree_creak"),TOWN_WIND=sound("town_wind");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register("borrasca."+id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca."+id)));}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BES.register(bus);SOUNDS.register(bus);}
    private BorrascaRegistry(){}
}
