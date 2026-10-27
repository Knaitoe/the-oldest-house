package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** The extended rooms inherit the actual fragment's floor; changes belong at room partitions. */
public final class DomesticPalette {
    private DomesticPalette(){}
    public static BlockState entry(String home,int x,int z){return LabyrinthDomestic.fragmentFloor(switch(home){case "family","grandmother"->LabyrinthDomestic.Room.KITCHEN;case "students"->LabyrinthDomestic.Room.DINING;default->LabyrinthDomestic.Room.BEDROOM;},x,z);}
    public static BlockState floor(String home,int x,int z){
        return switch(home){
            case "family"->x>=-13?entry(home,x,z):Blocks.BIRCH_PLANKS.defaultBlockState();
            case "students"->x>=15?Blocks.BIRCH_PLANKS.defaultBlockState():z<=-16&&x<=12?entry("family",x,z):entry(home,x,z);
            case "drinker"->x>=-1&&z<=-28?entry("family",x,z):entry(home,x,z);
            case "grandmother"->x>=-13?entry(home,x,z):Blocks.DARK_OAK_PLANKS.defaultBlockState();
            default->throw new IllegalArgumentException(home);
        };
    }
}
