package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** The fitted doors of the scenes' ways on, drawn by tools/draw_onward_doors.py. Like every House door, unbreakable. */
public final class OnwardRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties wood(){return BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_DOOR).strength(-1,3600000).noOcclusion().noLootTable();}
    private static BlockBehaviour.Properties metal(){return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR).strength(-1,3600000).noOcclusion().noLootTable();}
    public static final DeferredBlock<DoorBlock> WEATHERED=BLOCKS.register("onward_weathered_door",()->new DoorBlock(BlockSetType.SPRUCE,wood()));
    public static final DeferredBlock<DoorBlock> PANEL=BLOCKS.register("onward_panel_door",()->new DoorBlock(BlockSetType.OAK,wood()));
    public static final DeferredBlock<DoorBlock> CABIN=BLOCKS.register("onward_cabin_door",()->new DoorBlock(BlockSetType.DARK_OAK,wood()));
    // Opened by hand like the House's other service doors, though it sounds like steel.
    public static final DeferredBlock<DoorBlock> SERVICE=BLOCKS.register("onward_service_door",()->new DoorBlock(BlockSetType.COPPER,metal()));
    public static final DeferredBlock<DoorBlock> CELLAR=BLOCKS.register("onward_cellar_door",()->new DoorBlock(BlockSetType.SPRUCE,wood()));
    private OnwardRegistry(){}
    public static void register(IEventBus bus){BLOCKS.register(bus);}
}
