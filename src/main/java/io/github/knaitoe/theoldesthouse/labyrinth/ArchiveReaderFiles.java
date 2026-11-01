package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.*;

/** The seven historical reader appointments, filed beside the sealed windows. */
public final class ArchiveReaderFiles {
    public static final String[] NAMES={"Beatrice","Leonie","Pauline","Ruth","Anne","Esther","Helen"};
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private ArchiveReaderFiles(){}
    private static net.minecraft.world.level.block.state.BlockState files(){return ServiceFittingBlock.of(ServiceFittingBlock.Kind.DRAWER,Direction.WEST);}
    private static net.minecraft.world.level.block.state.BlockState label(){return Blocks.DARK_OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING,Direction.WEST);}
    /** 0.4.201 (owner: "the signposts should be fixed too"): one name to a label, hung on the wall over its stack, instead of
     *  three cramped lines on a sign stuck to the top drawer's face. */
    public static BlockPos label(int reader){return stack(reader).above(3);}
    /** 0.4.201: the seven stacks stand side by side along the front room's east wall, where the readers sat. */
    public static BlockPos stack(int reader){return new BlockPos(11,0,-21-reader);}
    private static void words(SignBlockEntity sign,int reader){var text=sign.getFrontText().setMessage(1,Component.literal(NAMES[reader]));sign.setText(text,true);sign.setWaxed(true);sign.setChanged();}
    public static void build(ServerLevel l,BlockPos b){
        for(int i=0;i<NAMES.length;i++){int z=stack(i).getZ(),index=i;for(int y=0;y<3;y++){var cell=b.offset(11,y,z);int drawer=y;BuildBlocks.set(l,cell,files(),F);BuildBlocks.after(l,()->{if(l.getBlockEntity(cell) instanceof ServiceFittingEntity file)file.file(index,drawer);});}var at=b.offset(label(i));BuildBlocks.set(l,at,label(),F);BuildBlocks.after(l,()->{if(l.getBlockEntity(at) instanceof SignBlockEntity sign)words(sign,index);});if(i%3==0)BuildBlocks.set(l,b.offset(10,6,z),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);}
    }
}
