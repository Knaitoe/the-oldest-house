package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** Ordinary household fabric and handled objects; no loot, quest or Witness source. */
public final class DomesticRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<DomesticFittingBlock>> TYPE=CODECS.register("domestic_fitting",()->DomesticFittingBlock.CODEC);
    public static final DeferredBlock<DomesticFittingBlock> FITTING=BLOCKS.registerBlock("domestic_fitting",DomesticFittingBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(.8F).noOcclusion().noLootTable().lightLevel(DomesticFittingBlock::light));
    private static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BES=DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<DomesticRecordBlock>> RECORD_TYPE=CODECS.register("domestic_record",()->DomesticRecordBlock.CODEC);
    public static final DeferredBlock<DomesticRecordBlock> RECORD=BLOCKS.registerBlock("domestic_record",DomesticRecordBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(.5F).noOcclusion().noLootTable());
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<DomesticRecordBlockEntity>> RECORD_ENTITY=BES.register("domestic_record",()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(DomesticRecordBlockEntity::new,RECORD.get()).build(null));
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    static {
        for(String name:new String[]{"family_paper","student_paper","smoked_paper","rose_paper","parquet","linoleum","parquet_worn"})
            MATERIALS.put(name,BLOCKS.registerSimpleBlock("domestic_"+name,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(1).noLootTable()));
    }
    private DomesticRegistry(){}
    public static Block material(String name){var b=MATERIALS.get(name);if(b==null)throw new IllegalArgumentException(name);return b.get();}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BES.register(bus);}
}
