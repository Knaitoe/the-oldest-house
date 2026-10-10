package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.neoforge.gametest.*;

/** Actual block-entity custody, native menu taking, disk reload and one-time saved-world replacement. */
@GameTestHolder("the_oldest_house")
@PrefixGameTestTemplate(false)
public final class NoticePostTests {
    private static NativeTestChunks chunks;private static LabyrinthData previous;private static ServerPlayer reader;
    @AfterBatch(batch="notice_post0468") public static void cleanup(ServerLevel l){if(chunks!=null){chunks.close();chunks=null;}if(reader!=null){reader.discard();reader=null;}if(previous!=null){l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",previous);previous=null;}}
    @GameTest(template="empty",batch="notice_post0468",timeoutTicks=2400)
    public static void theNoticeKeepsItsExactOriginalAcrossReplacementReloadAndTaking(GameTestHelper h){
        var s=h.getLevel().getServer();var l=HouseTestLevel.get(s,HouseDimensions.OUTSIDE);var b=LabyrinthPlaces.base(new BlockPos(569000,80,569000),LabyrinthPlace.DROWNED_TOWN);var at=b.offset(ProofrockTown.LECTERN);
        previous=LabyrinthData.get(s);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",new LabyrinthData());chunks=new NativeTestChunks();chunks.hold(l,IndianLakeRooms.bounds(b,LabyrinthPlace.DROWNED_TOWN));
        reader=h.makeMockServerPlayerInLevel();reader.teleportTo(l,100,80,100,0,0);reader.hasChangedDimension();
        h.startSequence().thenWaitUntil(()->h.assertTrue(chunks.ready(),"the saved town's native entity sections are ready")).thenExecute(()->{
            l.setBlock(at.below(),Blocks.GRASS_BLOCK.defaultBlockState(),2);l.setBlock(at,Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.HAS_BOOK,true),2);
            var original=HouseWriting.book("A marked original","An explorer",HouseWriting.WritingStyle.PLAIN,java.util.List.of("The actual first page.","The actual second page."));CustomData.update(DataComponents.CUSTOM_DATA,original,t->t.putString("Original0468","Keep this exact note"));((LecternBlockEntity)l.getBlockEntity(at)).setBook(original.copy());
            reader.moveTo(at.getX()+.5,at.getY(),at.getZ()+2.5);h.assertTrue(!PlaytestFixes.apply(l,b,LabyrinthPlace.DROWNED_TOWN)&&l.getBlockEntity(at) instanceof LecternBlockEntity,"a reader looking at the old note postpones its replacement");reader.moveTo(100,80,100);
            h.assertTrue(PlaytestFixes.apply(l,b,LabyrinthPlace.DROWNED_TOWN)&&l.getBlockEntity(at) instanceof NoticePostBlockEntity,"the unseen loaded lectern becomes an actual notice post");var post=(NoticePostBlockEntity)l.getBlockEntity(at);
            h.assertTrue(ItemStack.isSameItemSameComponents(original,post.book())&&l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(at).inflate(2)).isEmpty(),"replacement transfers the exact marked original without dropping another copy");
            var restored=new NoticePostBlockEntity(at,post.getBlockState());restored.loadWithComponents(post.saveWithoutMetadata(l.registryAccess()),l.registryAccess());h.assertTrue(ItemStack.isSameItemSameComponents(original,restored.book()),"the native block entity keeps the exact note through disk serialization");
            reader.moveTo(at.getX()+.5,at.getY(),at.getZ()+2.5);post.open(reader);var menu=reader.containerMenu;h.assertTrue(menu.clickMenuButton(reader,3)&&post.book().isEmpty()&&reader.getInventory().countItem(Items.WRITTEN_BOOK)==1&&!menu.clickMenuButton(reader,3),"the native reading menu hands over the original once and rejects repeated takes");
            l.setBlock(at,Blocks.AIR.defaultBlockState(),2);PlaytestFixes.apply(l,b,LabyrinthPlace.DROWNED_TOWN);h.assertTrue(l.getBlockState(at).isAir()&&reader.getInventory().countItem(Items.WRITTEN_BOOK)==1,"later removal neither rebuilds the note nor restocks its pages");h.succeed();
        });
    }
}
