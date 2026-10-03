package io.github.knaitoe.theoldesthouse.house;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
/** A textured board mounted on a real post at the outgoing-mail desk. */
public final class MailPlaqueBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<MailPlaqueBlock> CODEC=simpleCodec(MailPlaqueBlock::new);
    public MailPlaqueBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.SOUTH));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
    public static void place(ServerLevel level,BlockPos base){
        level.setBlock(base.offset(4,0,-8),Blocks.OAK_FENCE.defaultBlockState(),LabyrinthBuilder.flags());
        level.setBlock(base.offset(4,1,-8),HouseBlocks.MAIL_PLAQUE.get().defaultBlockState(),LabyrinthBuilder.flags());
    }
    public static void repair(ServerLevel level,BlockPos base){
        var data=LabyrinthData.get(level.getServer());var saved=data.state("mail_plaque_0430");String key=Long.toString(base.asLong());
        if(saved.getBoolean(key)||level.players().stream().anyMatch(p->IndianLakeRooms.bounds(base,LabyrinthPlace.WHALE).contains(p.position())))return;
        var at=base.offset(4,1,-8);
        if(level.getBlockEntity(at) instanceof SignBlockEntity sign&&sign.getFrontText().getMessage(0,false).getString().equals("Outgoing mail")&&level.getBlockState(at.below()).isAir())place(level,base);
        saved.putBoolean(key,true);data.setState("mail_plaque_0430",saved);
    }
}
