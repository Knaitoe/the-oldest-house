package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SequenceRepairTests {
    @GameTest(template="empty") public static void customEssayUsesReadableNativeBookWithoutDuplicatingItsOriginal(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();var essay=new ItemStack(DrownedTownRegistry.DRIED_ESSAY_ONE.get());var identity=new CompoundTag();identity.putUUID("OriginalEssay",UUID.randomUUID());essay.set(DataComponents.CUSTOM_DATA,CustomData.of(identity));p.setItemInHand(InteractionHand.MAIN_HAND,essay);
        try{
            essay.getItem().use(p.level(),p,InteractionHand.MAIN_HAND);
            h.assertTrue(p.containerMenu instanceof LecternMenu,"using the custom dried item opens an actual native lectern reader");
            var menu=(LecternMenu)p.containerMenu;var display=menu.getSlot(0).getItem();
            h.assertTrue(display.is(Items.WRITTEN_BOOK)&&display.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().get(1).raw().getString().contains("water's surface"),"the native reader receives a vanilla written book with the actual refuge guidance");
            h.assertTrue(menu.clickMenuButton(p,102)&&menu.getPage()==2&&!menu.clickMenuButton(p,999),"native page controls work and reject forged pages");
            h.assertTrue(!menu.clickMenuButton(p,3)&&p.getInventory().countItem(Items.WRITTEN_BOOK)==0&&p.getItemInHand(InteractionHand.MAIN_HAND)==essay&&essay.get(DataComponents.CUSTOM_DATA).copyTag().equals(identity),"the reader cannot take a second book or change original custody and identity");h.succeed();
        }finally{p.closeContainer();p.server.getPlayerList().remove(p);}
    }
    @GameTest(template="empty") public static void visitLockAndDormantSourceRemainPersonalAcrossReload(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);var s=p.server;var previous=LabyrinthData.get(s);var d=new LabyrinthData();s.overworld().getDataStorage().set("the_oldest_house_labyrinth",d);
        try{
            var at=h.absolutePos(new BlockPos(0,0,0));var source=new LabyrinthData.Door("repair.source",p.level().dimension(),at,Direction.SOUTH,LabyrinthData.DEALT,false);d.putDoor(source);
            LabyrinthBuilder.registerDoors(d,LabyrinthPlace.DROWNED_TOWN,at.offset(500,0,0));var exit=d.door(LabyrinthPlace.DROWNED_TOWN.entryDoorId());var town=new CompoundTag();town.putInt("Visit",1);d.setState(DrownedTown.ID,town);
            VignetteGate.begin(p,LabyrinthPlace.DROWNED_TOWN);h.assertTrue(!VignetteGate.exitLocked(p,exit),"the vestibule remains reversible before physically entering");VignetteGate.stepped(p,LabyrinthPlace.DROWNED_TOWN);
            h.assertTrue(!VignetteGate.exitLocked(p,exit),"an unfinished lake visit can be voluntarily abandoned without false completion");
            h.assertTrue(!WitnessAccount.has(d,p.getUUID(),WitnessAccount.Story.DROWNED_TOWN)&&d.state(DrownedTown.ID).getInt("DryMask")==0,"retreat neither awards evidence nor erases the unfinished task");
            VignetteGate.departed(p,new LabyrinthData.Waypoint(p.level().dimension(),Vec3.atBottomCenterOf(at),0,true),exit);UUID peer=UUID.randomUUID();
            h.assertTrue(VignetteGate.dormant(d,p.getUUID(),source)&&!VignetteGate.dormant(d,peer,source),"leaving makes only this explorer's source dormant");
            var loaded=LabyrinthData.FACTORY.deserializer().apply(d.save(new CompoundTag(),p.registryAccess()),p.registryAccess());s.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
            h.assertTrue(VignetteGate.dormant(loaded,p.getUUID(),loaded.door(source.id)),"dormancy survives native world-data reload");loaded.deal(p.getUUID(),loaded.door(source.id),LabyrinthPlace.DROWNED_TOWN.id(),false);
            h.assertTrue(!VignetteGate.dormant(loaded,p.getUUID(),loaded.door(source.id)),"an actual personal rediscovery unlocks the source again");h.succeed();
        }finally{s.overworld().getDataStorage().set("the_oldest_house_labyrinth",previous);s.getPlayerList().remove(p);}
    }
    @GameTest(template="empty") public static void cagedBoyReloadsAndAwakensAsTheSameLivingCreature(GameTestHelper h){
        var boy=FinaleRegistry.MINOTAUR.get().create(h.getLevel());boy.caged();var save=new CompoundTag();boy.saveWithoutId(save);var restored=FinaleRegistry.MINOTAUR.get().create(h.getLevel());restored.load(save);
        try{
            h.assertTrue(restored.motion()==MinotaurEntity.CAGED&&restored.getBbWidth()<.6&&restored.getBbHeight()<1.4&&restored.getUUID().equals(boy.getUUID()),"native save retains the visible child's pose, size and entity identity");UUID owner=UUID.randomUUID();restored.awaken(owner);
            h.assertTrue(owner.equals(restored.owner())&&restored.motion()==MinotaurEntity.WATCHING&&restored.getBbHeight()>3&&restored.getUUID().equals(boy.getUUID())&&restored.isAlive(),"opening the cell changes the same living actor into the encounter");h.succeed();
        }finally{boy.discard();restored.discard();}
    }
}
