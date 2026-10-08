package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class HallChangeRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    public static final DeferredBlock<HallChangeBlock> PROP=BLOCKS.register("hall_change",()->new HallChangeBlock(BlockBehaviour.Properties.of().noOcclusion().noLootTable().strength(.3F)
            .lightLevel(s->s.getValue(HallChangeBlock.KIND)==HallChangeBlock.Kind.LAMP?9:0)));
    private HallChangeRegistry(){}
    public static void register(IEventBus bus){BLOCKS.register(bus);}
}
