package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.*;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HomeExperienceTests {
    @GameTest(template="empty") public static void homeLettersRememberNativeSeatsAndKeepFiniteOriginalRepliesAcrossSave(GameTestHelper h){
        var server=h.getLevel().getServer();var level=HouseTestLevel.get(server,HouseDimensions.INTERIOR);var oldHouse=HouseSavedData.get(server);var oldData=LabyrinthData.get(server);var house=new HouseSavedData();var d=new LabyrinthData();var origin=new BlockPos(95600,70,95600);house.markSpawned(origin);
        server.overworld().getDataStorage().set("the_oldest_house",house);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",d);
        var p=h.makeMockServerPlayerInLevel();p.teleportTo(level,origin.getX()+6.5,origin.getY()+1,origin.getZ()+23.5,0,0);p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);var desk=HomeLetters.desk(origin);var chair=origin.offset(6,1,22);var drawer=origin.offset(8,1,22);
        try{
            level.setBlock(chair.below(),Blocks.OAK_PLANKS.defaultBlockState(),3);level.setBlock(chair,HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState(),3);
            // Use an ordinary native stair seat so the actual ride event must succeed.
            level.setBlock(chair,Blocks.OAK_STAIRS.defaultBlockState(),3);p.moveTo(chair.getX()+.5,chair.getY(),chair.getZ()+1.5);
            h.assertTrue(HouseSitting.sit(p,chair)&&p.isPassenger()&&HouseExperience.record(d,p.getUUID()).getLong("Seat")==chair.asLong(),"only a successful native sitting action binds the remembered furniture");p.stopRiding();
            level.setBlock(desk.below(),Blocks.OAK_PLANKS.defaultBlockState(),3);level.setBlock(desk,Blocks.LECTERN.defaultBlockState(),3);level.setBlock(drawer,Blocks.BARREL.defaultBlockState(),3);var cache=(BarrelBlockEntity)level.getBlockEntity(drawer);var kept=new ItemStack(Items.DIAMOND,2);cache.setItem(0,kept);p.moveTo(desk.getX()+.5,desk.getY(),desk.getZ()+1.5);
            p.openMenu(new SimpleMenuProvider((id,inv,reader)->new HomeLetters.LetterMenu(id,p,desk),net.minecraft.network.chat.Component.literal("Letters")));var menu=(LecternMenu)p.containerMenu;
            h.assertTrue(menu.clickMenuButton(p,101)&&menu.clickMenuButton(p,3)&&!menu.clickMenuButton(p,3),"the actual native letter reader advances on the last page and yields one original copy");
            var delivered=p.getInventory().items.stream().filter(i->i.has(DataComponents.WRITTEN_BOOK_CONTENT)).findFirst().orElseThrow();h.assertTrue(delivered.get(DataComponents.CUSTOM_DATA).copyTag().getUUID("LetterTo").equals(p.getUUID()),"letter custody remains bound to the actual reader");p.closeContainer();
            var reply=new ItemStack(Items.WRITABLE_BOOK);reply.set(DataComponents.WRITABLE_BOOK_CONTENT,new WritableBookContent(java.util.List.of(Filterable.passThrough("I came back. I have left the kettle alone."))));UUID original=UUID.randomUUID();CustomData.update(DataComponents.CUSTOM_DATA,reply,t->t.putUUID("OriginalReply",original));p.setItemInHand(InteractionHand.MAIN_HAND,reply);
            h.assertTrue(HomeLetters.reply(p,origin)&&p.getMainHandItem().isEmpty()&&cache.getItem(1).get(DataComponents.CUSTOM_DATA).copyTag().getUUID("OriginalReply").equals(original)&&cache.getItem(0)==kept,"reply submission moves the original native book into a vacant drawer slot and retains existing supplies");
            var loaded=LabyrinthData.FACTORY.deserializer().apply(d.save(new CompoundTag(),p.registryAccess()),p.registryAccess());server.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
            var own=HouseExperience.record(loaded,p.getUUID());h.assertTrue(own.getCompound("LetterTaken").getBoolean("0")&&own.getCompound("ReplySent").getBoolean("0")&&own.getLong("Seat")==chair.asLong()&&HouseExperience.record(loaded,UUID.randomUUID()).isEmpty(),"native reload retains finite personal correspondence without granting it to peers");
            h.assertTrue(WitnessAccount.count(loaded,p.getUUID())==0,"domestic attachment and letter collection confer no false story resolution");h.succeed();
        }finally{p.closeContainer();p.stopRiding();server.getPlayerList().remove(p);level.getEntitiesOfClass(SeatEntity.class,new net.minecraft.world.phys.AABB(chair).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);}
    }
    @GameTest(template="empty") public static void retreatChangesOnlyItsReadersFutureWritingAndPreservesCompletion(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();var server=p.server;var prior=LabyrinthData.get(server);var d=new LabyrinthData();server.overworld().getDataStorage().set("the_oldest_house_labyrinth",d);
        try{HouseExperience.arrived(p,LabyrinthPlace.DROWNED_TOWN);HouseExperience.returned(p);var own=HouseExperience.record(d,p.getUUID());
            h.assertTrue(own.getCompound("Retreats").getInt(LabyrinthPlace.DROWNED_TOWN.id())==1&&own.getInt("Returns")==1&&WitnessAccount.count(d,p.getUUID())==0,"an actual unfinished retreat creates a personal trace without awarding completion");
            h.assertTrue(HouseExperience.weight(d,p.getUUID(),LabyrinthPlace.DROWNED_TOWN,12)>12&&HouseExperience.weight(d,UUID.randomUUID(),LabyrinthPlace.DROWNED_TOWN,12)==12,"only the explorer's later destination odds remember the unfinished return");
            d.visit(p.getUUID(),LabyrinthPlace.DROWNED_TOWN);h.assertTrue(HouseExperience.weight(d,p.getUUID(),LabyrinthPlace.DROWNED_TOWN,12)==12,"personal affinity cannot defeat native recent-visit spacing");h.succeed();
        }finally{server.overworld().getDataStorage().set("the_oldest_house_labyrinth",prior);server.getPlayerList().remove(p);}
    }
}
