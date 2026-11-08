package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/**
 * 0.4.391, the long bedroom hall (OrdinaryHalls, docs/FOUR_HALLS_0_4_391.md): the two bedroom doors that open, drawn by
 * tools/draw_ordinary_hall_art.py from the same design as the eighteen painted onto the walls. An ordinary door in every other way.
 */
public final class HallDoorBlock extends DoorBlock {
    public HallDoorBlock(Properties p){super(BlockSetType.SPRUCE,p);}
}
