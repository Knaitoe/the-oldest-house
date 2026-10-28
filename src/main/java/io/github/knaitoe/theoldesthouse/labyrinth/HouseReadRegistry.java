package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** The quiet rooms have no story source, reward or inferred reader categories. */
public final class HouseReadRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties wood(){return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(-1,3600000).noLootTable();}
    public static final DeferredBlock<Block> WALLPAPER=BLOCKS.registerBlock("read_wallpaper",Block::new,wood());
    public static final DeferredBlock<Block> CLAPBOARD=BLOCKS.registerBlock("read_clapboard",Block::new,wood());
    public static final DeferredBlock<Block> LINOLEUM=BLOCKS.registerBlock("read_linoleum",Block::new,wood());
    public static final DeferredBlock<HouseReadPropBlock> FITTING=BLOCKS.registerBlock("read_fitting",HouseReadPropBlock::new,wood().noOcclusion().dynamicShape());
    public static final DeferredBlock<DoorBlock> SCREEN=BLOCKS.register("read_screen_door",()->new DoorBlock(BlockSetType.OAK,wood().noOcclusion()){});
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<HouseReadPropBlock>> FITTING_TYPE=CODECS.register("read_fitting",()->HouseReadPropBlock.CODEC);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<HouseReadPropBlock.Fitting>> FITTING_ENTITY=BES.register("read_fitting",()->BlockEntityType.Builder.of(HouseReadPropBlock.Fitting::new,FITTING.get()).build(null));
    public static final DeferredItem<Garment> WOOL=garment("wool",EquipmentSlot.CHEST),TRENCH=garment("trench",EquipmentSlot.CHEST),BARN=garment("barn",EquipmentSlot.CHEST),WOOL_BOOTS=garment("wool_boots",EquipmentSlot.FEET),TRENCH_BOOTS=garment("trench_boots",EquipmentSlot.FEET),BARN_BOOTS=garment("barn_boots",EquipmentSlot.FEET);
    public static final DeferredHolder<SoundEvent,SoundEvent> RAIN=sound("rain"),RADIATOR=sound("radiator"),WHISTLE=sound("whistle"),SUPPER=sound("supper"),NEWS=sound("news"),DISHES=sound("dishes"),ICE=sound("ice"),STEP=sound("step");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register("house_read."+id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"house_read."+id)));}
    private static DeferredItem<Garment> garment(String id,EquipmentSlot slot){return ITEMS.register("read_"+id,()->new Garment(new Item.Properties().stacksTo(1),slot,id));}
    public static final class Garment extends Item implements Equipable {
        private final EquipmentSlot slot;public final String cloth;
        Garment(Properties p,EquipmentSlot slot,String cloth){super(p);this.slot=slot;this.cloth=cloth;}
        @Override public EquipmentSlot getEquipmentSlot(){return slot;}
    }
    private HouseReadRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BES.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}
}
