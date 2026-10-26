package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_interior_review") @PrefixGameTestTemplate(false)
public final class ArchiveReaderFilesTests {
    private static NovelTests.Fixture fixture;
    @AfterBatch(batch="archive_reader_files131") public static void close(ServerLevel ignored){if(fixture!=null){for(var p:fixture.players)NativeTestPlayers.remove(p);fixture.players.clear();fixture.close();fixture=null;}}
    @GameTest(template="empty",batch="archive_reader_files131",timeoutTicks=500)
    public static void theSevenNamedAppointmentsBecomeFittedFilesAndTheOldPostsRepairWithoutReplacingOriginalBooksOrEditedCards(GameTestHelper h){
        fixture=new NovelTests.Fixture(h,57800,LabyrinthPlace.ZAMPANO_COURTYARD);var f=fixture;var l=f.l;var b=f.b;var p=NativeTestPlayers.survival(h,"archive_files");f.players.add(p);p.teleportTo(l,b.getX()+10.5,b.getY(),b.getZ()-21.5,180,0);p.hasChangedDimension();f.chunks.hold(l,IndianLakeRooms.bounds(b,LabyrinthPlace.ZAMPANO_COURTYARD));
        var signs=new ArrayList<SignBlockEntity>();
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the actual archive and native entity sections are loaded")).thenExecute(()->{
            var source=l.getBlockEntity(b.offset(NovelRooms.ARCHIVE_DESK));var chest=(ChestBlockEntity)l.getBlockEntity(b.offset(5,0,-34));var book=chest.getItem(0).copy();
            for(int i=0;i<7;i++){
                int z=-22-i*2;var at=b.offset(10,2,z);h.assertTrue(l.getBlockState(at).getBlock() instanceof WallSignBlock&&l.getBlockEntity(at) instanceof SignBlockEntity sign&&sign.getFrontText().getMessage(1,false).getString().equals(ArchiveReaderFiles.NAMES[i]),"the fresh archive names all seven actual readers on mounted file labels");
                for(int y=0;y<3;y++){var state=l.getBlockState(b.offset(11,y,z));h.assertTrue(state.is(NovelRegistry.PROP.get())&&state.getValue(NovelPropBlock.KIND)==NovelPropBlock.Kind.ARCHIVE_FILES,"the names belong to physical fitted archive shelves");l.setBlock(b.offset(11,y,z),Blocks.AIR.defaultBlockState(),18);}
                l.setBlock(at,Blocks.DARK_OAK_SIGN.defaultBlockState(),18);var sign=(SignBlockEntity)l.getBlockEntity(at);sign.setText(sign.getFrontText().setMessage(0,Component.literal("reader "+(i+1))).setMessage(1,Component.literal(i==6?"Helen's edited card":ArchiveReaderFiles.NAMES[i])),true);signs.add(sign);
                for(int y=0;y<2;y++)l.setBlock(b.offset(10,y,z),Blocks.SPRUCE_FENCE.defaultBlockState(),18);
            }
            var state=f.data().state(ArchiveReaderFiles.STATE);state.putInt(Long.toString(b.asLong()),0);f.data().setState(ArchiveReaderFiles.STATE,state);
            h.assertTrue(!ArchiveReaderFiles.repair(l,b)&&l.getBlockEntity(b.offset(10,2,-22))==signs.get(0),"a real nearby reader defers the old-post refit");
            p.teleportTo(l,b.getX()+50,b.getY(),b.getZ()+4,0,0);p.hasChangedDimension();
            var body=EntityType.ARMOR_STAND.create(l);body.setNoGravity(true);body.moveTo(b.getX()+11.5,b.getY(),b.getZ()-23.5);l.addFreshEntity(body);f.extra.add(body);
            ArchiveReaderFiles.repair(l,b);h.assertTrue(f.data().state(ArchiveReaderFiles.STATE).getInt(Long.toString(b.asLong()))==1&&!ArchiveReaderFiles.repair(l,b),"one column is bounded and the next defers its actual living occupant");body.discard();
            for(int i=0;i<8;i++)ArchiveReaderFiles.repair(l,b);
            for(int i=0;i<6;i++){int z=-22-i*2;h.assertTrue(l.getBlockEntity(b.offset(10,2,z))==signs.get(i)&&l.getBlockState(b.offset(10,2,z)).getBlock() instanceof WallSignBlock&&l.getBlockState(b.offset(10,0,z)).isAir()&&signs.get(i).getFrontText().getMessage(1,false).getString().equals(ArchiveReaderFiles.NAMES[i]),"recognized names keep their original sign BE while the freestanding poles disappear");}
            h.assertTrue(l.getBlockEntity(b.offset(10,2,-34))==signs.get(6)&&l.getBlockState(b.offset(10,0,-34)).is(Blocks.SPRUCE_FENCE)&&l.isEmptyBlock(b.offset(11,0,-34)),"an edited card and its old setting remain intact");
            h.assertTrue(l.getBlockEntity(b.offset(NovelRooms.ARCHIVE_DESK))==source&&l.getBlockEntity(b.offset(5,0,-34))==chest&&ItemStack.matches(book,chest.getItem(0)),"the actual survey, original chest and appointments book retain custody and components");f.reload();h.assertTrue(ArchiveReaderFiles.repair(l,b)&&f.data().state(ArchiveReaderFiles.STATE).getInt(Long.toString(b.asLong()))==7,"native SavedData reload keeps the completed bounded repair");
        }).thenSucceed();
    }
}
