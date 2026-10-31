package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_interior_review") @PrefixGameTestTemplate(false)
public final class ArchiveReaderFilesTests {
    private static NovelTests.Fixture fixture;
    @AfterBatch(batch="archive_reader_files131") public static void close(ServerLevel ignored){if(fixture!=null){for(var p:fixture.players)NativeTestPlayers.remove(p);fixture.players.clear();fixture.close();fixture=null;}}
    @GameTest(template="empty",batch="archive_reader_files131",timeoutTicks=500)
    public static void theSevenNamedAppointmentsAreFittedFilesUnderMountedLabels(GameTestHelper h){
        fixture=new NovelTests.Fixture(h,57800,LabyrinthPlace.ZAMPANO_COURTYARD);var f=fixture;io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(f.l.getServer()).markInteriorInitialized();var l=f.l;var b=f.b;f.chunks.hold(l,IndianLakeRooms.bounds(b,LabyrinthPlace.ZAMPANO_COURTYARD));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the actual archive and native entity sections are loaded")).thenExecute(()->{
            for(int i=0;i<7;i++){
                int z=-22-i*2;var at=b.offset(10,2,z);h.assertTrue(l.getBlockState(at).getBlock() instanceof WallSignBlock&&l.getBlockEntity(at) instanceof SignBlockEntity sign&&sign.getFrontText().getMessage(1,false).getString().equals(ArchiveReaderFiles.NAMES[i]),"the archive names all seven actual readers on mounted file labels");
                for(int y=0;y<3;y++){var state=l.getBlockState(b.offset(11,y,z));h.assertTrue(state.is(LiteraryRegistry.DETAIL.get())&&state.getValue(ServiceFittingBlock.KIND)==ServiceFittingBlock.Kind.DRAWER&&l.getBlockEntity(b.offset(11,y,z)) instanceof ServiceFittingEntity,"the names belong to physical fitted archive shelves");}
            }
        }).thenSucceed();
    }
}
