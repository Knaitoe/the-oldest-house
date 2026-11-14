package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Environment pieces only: no new rewards, inventory sources or Witness stories. */
public final class HazardRegistry {
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<HazardBlock>> TYPE=CODECS.register("hazard_piece",()->HazardBlock.CODEC);
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    public static final DeferredBlock<HazardBlock> PIECE=BLOCKS.register("hazard_piece",()->new HazardBlock(BlockBehaviour.Properties.of().strength(-1,3600000).sound(SoundType.STONE).noOcclusion().noLootTable().lightLevel(HazardBlock::light)));
    /** 0.4.592: the mirror hall's wall, a true plane mirror hung from one of its blocks (HazardBlock.mirrorAnchor). */
    private static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> ENTITIES=DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<HazardBlock.MirrorWall>> MIRROR_WALL=
        ENTITIES.register("mirror_wall",()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(HazardBlock.MirrorWall::new,PIECE.get()).build(null));
    private HazardRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ENTITIES.register(bus);}
}
