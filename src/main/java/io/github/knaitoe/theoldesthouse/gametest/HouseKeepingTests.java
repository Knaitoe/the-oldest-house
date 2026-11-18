package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseKeeping;
import io.github.knaitoe.theoldesthouse.house.HouseKeeping.Where;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.255, the hall drawers (docs/HALL_DRAWERS_0_4_255.md), with a real reader's inventory: what a reader carries in from outside is
 * put away unless the House made it, it is eaten or drunk, or it is a light; their own things can be taken out of the drawers in the
 * manor and go back to them past the hall; what the House gave them comes back in with them from outside; the lists survive a save.
 * 0.4.704 (docs/FAVORITE_ITEM_0_4_704.md): nothing is taken at the front door, only past the end of the hall; the hall stand keeps one
 * thing for each reader and hands it over at the cell door.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class HouseKeepingTests {
    private HouseKeepingTests(){}
    private static int count(net.minecraft.server.level.ServerPlayer p,Item item){int n=0;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(item))n+=p.getInventory().getItem(i).getCount();return n;}
    private static int count(List<ItemStack> list,Item item){int n=0;for(var s:list)if(s.is(item))n+=s.getCount();return n;}

    @GameTest(template="empty",batch="hall_drawers",timeoutTicks=200)
    public static void theHouseKeepsWhatAReaderCarriesInFromOutsideInTheHallDrawers(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"drawers_reader");var server=h.getLevel().getServer();var data=HouseKeeping.get(server);data.forget(p.getUUID());
        h.startSequence().thenExecute(()->{
            var inv=p.getInventory();inv.clearContent();
            HouseKeeping.update(p,Where.MANOR);
            h.assertTrue(HouseKeeping.of(p).where==Where.MANOR,"a reader the House has not seen before is taken to be where they stand");
            HouseKeeping.update(p,Where.OUTSIDE);
            var stick=new ItemStack(Items.STICK);var mark=new CompoundTag();mark.putBoolean("HouseMade",true);stick.set(DataComponents.CUSTOM_DATA,CustomData.of(mark));
            inv.setItem(0,new ItemStack(Items.COBBLESTONE,32));inv.setItem(1,new ItemStack(Items.IRON_SWORD));inv.setItem(2,new ItemStack(Items.BREAD,4));
            inv.setItem(3,new ItemStack(Items.TORCH,8));inv.setItem(4,new ItemStack(Items.WATER_BUCKET));inv.setItem(5,new ItemStack(Items.ENDER_PEARL,2));
            inv.setItem(6,new ItemStack(Items.CHORUS_FRUIT));inv.setItem(7,new ItemStack(HouseBlocks.FURNITURE_ITEM.get()));inv.setItem(8,stick);
            inv.setItem(9,new ItemStack(Items.CANDLE,3));inv.setItem(10,new ItemStack(Items.LANTERN));inv.setItem(11,new ItemStack(Items.POTION));
            inv.armor.set(2,new ItemStack(Items.IRON_CHESTPLATE));inv.offhand.set(0,new ItemStack(Items.SHIELD));
            p.inventoryMenu.getCraftSlots().setItem(0,new ItemStack(Items.OAK_PLANKS,4));
            // In through the front door (0.4.704): nothing is taken; the reader's own things are loose in the manor.
            HouseKeeping.update(p,Where.MANOR);
            var keep=HouseKeeping.of(p);
            for(var held:List.of(Items.COBBLESTONE,Items.IRON_SWORD,Items.WATER_BUCKET,Items.ENDER_PEARL,Items.CHORUS_FRUIT,Items.IRON_CHESTPLATE,Items.SHIELD))
                h.assertTrue(count(p,held)>0&&count(keep.loose,held)>0&&count(keep.drawer,held)==0,"kept in hand in the manor, the reader's own: "+held);
            h.assertTrue(keep.drawer.isEmpty()&&count(keep.loose,Items.BREAD)==0&&count(keep.loose,Items.TORCH)==0,"nothing in the drawer at the front door, and what the House allows is nobody's to take");
            // Past the end of the hall, the House takes what it did not provide.
            HouseKeeping.update(p,Where.BEYOND);
            for(var kept:List.of(Items.BREAD,Items.TORCH,HouseBlocks.FURNITURE_ITEM.get(),Items.STICK,Items.CANDLE,Items.LANTERN,Items.POTION))
                h.assertTrue(count(p,kept)>0,"allowed in: "+kept);
            for(var taken:List.of(Items.COBBLESTONE,Items.IRON_SWORD,Items.WATER_BUCKET,Items.ENDER_PEARL,Items.CHORUS_FRUIT,Items.IRON_CHESTPLATE,Items.SHIELD))
                h.assertTrue(count(p,taken)==0&&count(keep.drawer,taken)>0,"put away in the drawers past the hall: "+taken);
            h.assertTrue(p.inventoryMenu.getCraftSlots().getItem(0).isEmpty()&&count(keep.drawer,Items.OAK_PLANKS)==4,"nothing slips in through the crafting grid");
            h.assertTrue(count(keep.drawer,Items.COBBLESTONE)==32&&keep.loose.isEmpty(),"exactly what was taken, all of it: "+keep.drawer);
            HouseKeeping.update(p,Where.MANOR);
            // The drawers in the hall: the sword taken out, the stone taken out and put back.
            HouseKeeping.open(p,p.blockPosition());
            h.assertTrue(p.containerMenu instanceof ChestMenu,"the drawers open as six rows");var menu=(ChestMenu)p.containerMenu;
            int sword=-1,stone=-1;for(int i=0;i<HouseKeeping.SHOWN;i++){var s=menu.getSlot(i).getItem();if(s.is(Items.IRON_SWORD))sword=i;if(s.is(Items.COBBLESTONE))stone=i;}
            h.assertTrue(sword>=0&&stone>=0,"the reader's own things are in their drawer");
            menu.quickMoveStack(p,sword);
            h.assertTrue(count(p,Items.IRON_SWORD)==1&&count(keep.loose,Items.IRON_SWORD)==1&&count(keep.drawer,Items.IRON_SWORD)==0,"taken out of the drawer, the sword is the reader's to hold in the manor");
            menu.quickMoveStack(p,stone);
            h.assertTrue(count(p,Items.COBBLESTONE)==32&&count(keep.loose,Items.COBBLESTONE)==32,"and the stone");
            int back=-1;for(int i=HouseKeeping.SHOWN;i<menu.slots.size();i++)if(menu.getSlot(i).getItem().is(Items.COBBLESTONE))back=i;
            menu.quickMoveStack(p,back);
            h.assertTrue(count(p,Items.COBBLESTONE)==0&&count(keep.loose,Items.COBBLESTONE)==0&&count(keep.drawer,Items.COBBLESTONE)==32,"put back, it is struck off the loose list");
            p.closeContainer();
            // Past the hall, the sword goes back.
            HouseKeeping.update(p,Where.BEYOND);
            h.assertTrue(count(p,Items.IRON_SWORD)==0&&count(keep.drawer,Items.IRON_SWORD)==1&&keep.loose.isEmpty(),"past the hall the reader's own sword goes back to the drawer");
            // Something the House gives past the hall, a plain iron pickaxe, goes out into the world and comes back in.
            inv.setItem(12,new ItemStack(Items.IRON_PICKAXE));
            HouseKeeping.update(p,Where.MANOR);HouseKeeping.update(p,Where.OUTSIDE);
            h.assertTrue(count(keep.carried,Items.IRON_PICKAXE)==1,"the House notes its own pickaxe going out");
            inv.setItem(13,new ItemStack(Items.DIRT,5));
            HouseKeeping.update(p,Where.MANOR);
            h.assertTrue(count(p,Items.IRON_PICKAXE)==1&&count(keep.carried,Items.IRON_PICKAXE)==0,"its own pickaxe comes back in with the reader");
            h.assertTrue(count(p,Items.DIRT)==5&&count(keep.loose,Items.DIRT)==5,"dirt dug outside is the reader's to hold in the manor");
            HouseKeeping.update(p,Where.BEYOND);
            h.assertTrue(count(p,Items.IRON_PICKAXE)==1&&count(p,Items.DIRT)==0&&count(keep.drawer,Items.DIRT)==5,"past the hall the dirt is put away and the House's own pickaxe is not");
            HouseKeeping.update(p,Where.MANOR);
            // The lists survive a save.
            var tag=data.save(new CompoundTag(),h.getLevel().registryAccess());var loaded=HouseKeeping.load(tag,h.getLevel().registryAccess()).keep(p.getUUID());
            h.assertTrue(loaded.drawer.size()==keep.drawer.size()&&count(loaded.drawer,Items.COBBLESTONE)==32&&loaded.where==Where.MANOR,"saved and loaded, the drawer is as it was");
            data.forget(p.getUUID());
        }).thenSucceed();
    }

    /**
     * 0.4.704 (the owner: "the player needs a way to secure their 'favorite' item to slay the Minotaur with"): a hand on the hall stand
     * leaves one thing there, a second hand with another swaps them, an empty hand takes it back; arrows go in with a bow; the stand's
     * thing is not put in the drawer past the hall; another reader's stand is their own; the stand survives a save; at the cell door
     * the thing comes to the reader's hand as the one the Minotaur answers to; and the thing shown on the stand is sent to its reader
     * alone and never saved.
     */
    @GameTest(template="empty",batch="hall_stand",timeoutTicks=200)
    public static void theHallStandKeepsOneThingForTheCellDoor(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"stand_reader");var other=NativeTestPlayers.survival(h,"stand_other");
        var server=h.getLevel().getServer();var data=HouseKeeping.get(server);data.forget(p.getUUID());data.forget(other.getUUID());
        h.startSequence().thenExecute(()->{
            var inv=p.getInventory();inv.clearContent();other.getInventory().clearContent();
            HouseKeeping.update(p,Where.OUTSIDE);HouseKeeping.update(other,Where.OUTSIDE);
            inv.setItem(0,new ItemStack(Items.IRON_SWORD));inv.setItem(1,new ItemStack(Items.BOW));inv.setItem(2,new ItemStack(Items.ARROW,16));
            HouseKeeping.update(p,Where.MANOR);HouseKeeping.update(other,Where.MANOR);
            var keep=HouseKeeping.of(p);var at=p.blockPosition();
            // The sword on the stand.
            inv.selected=0;HouseKeeping.stand(p,at);
            h.assertTrue(keep.kept.is(Items.IRON_SWORD)&&keep.kept.getCount()==1&&count(p,Items.IRON_SWORD)==0&&count(keep.loose,Items.IRON_SWORD)==0,"one sword on the stand, out of hand and off the loose list");
            // The other reader's stand is their own.
            HouseKeeping.stand(other,at);
            h.assertTrue(HouseKeeping.of(other).kept.isEmpty()&&keep.kept.is(Items.IRON_SWORD),"an empty hand at another reader's stand finds it empty, and the first reader's sword is still there");
            // Back and forth: an empty hand takes it, a hand with it leaves it again.
            HouseKeeping.stand(p,at);
            h.assertTrue(keep.kept.isEmpty()&&p.getMainHandItem().is(Items.IRON_SWORD)&&count(keep.loose,Items.IRON_SWORD)==1,"taken back into the empty hand, loose again");
            HouseKeeping.stand(p,at);
            h.assertTrue(keep.kept.is(Items.IRON_SWORD)&&p.getMainHandItem().isEmpty(),"and left again");
            // The bow in its place: the sword comes back.
            inv.selected=1;HouseKeeping.stand(p,at);
            h.assertTrue(keep.kept.is(Items.BOW)&&count(p,Items.IRON_SWORD)==1&&count(p,Items.BOW)==0,"the bow on the stand and the sword back with the reader");
            // Arrows go in with the bow.
            inv.selected=2;HouseKeeping.stand(p,at);
            h.assertTrue(keep.kept.is(Items.BOW)&&keep.quiver.is(Items.ARROW)&&keep.quiver.getCount()==16&&count(p,Items.ARROW)==0,"the arrows in with the bow: "+keep.quiver);
            // Past the hall the sword is put away; the bow and arrows are on the stand, not in the drawer.
            HouseKeeping.update(p,Where.BEYOND);
            h.assertTrue(count(p,Items.IRON_SWORD)==0&&count(keep.drawer,Items.IRON_SWORD)==1&&count(keep.drawer,Items.BOW)==0&&count(keep.drawer,Items.ARROW)==0,"the drawer has the sword and not what is on the stand");
            // The stand survives a save.
            var loaded=HouseKeeping.load(data.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess()).keep(p.getUUID());
            h.assertTrue(loaded.kept.is(Items.BOW)&&loaded.quiver.getCount()==16,"saved and loaded, the stand is as it was");
            // At the cell door: in hand, the one that wounds, the arrows with it.
            var given=HouseKeeping.handOver(p);
            var bow=p.getMainHandItem();
            h.assertTrue(given.is(Items.BOW)&&bow.is(Items.BOW)&&count(p,Items.ARROW)==16&&keep.kept.isEmpty()&&keep.quiver.isEmpty(),"the bow in hand and the arrows in the pockets; the stand is empty");
            var id=io.github.knaitoe.theoldesthouse.labyrinth.WeaponHistory.identity(bow);
            h.assertTrue(id!=null&&id.equals(io.github.knaitoe.theoldesthouse.labyrinth.WeaponHistory.favorite(p))&&io.github.knaitoe.theoldesthouse.labyrinth.WeaponHistory.wounds(bow,id),"it is the reader's original, the one the Minotaur answers to");
            h.assertTrue(HouseKeeping.handOver(p).isEmpty(),"and nothing a second time");
            // Anything at all, not only a weapon: a spade left on the stand is made an original too.
            var spade=new ItemStack(Items.IRON_SHOVEL);keep.kept=spade;var made=HouseKeeping.handOver(p);
            var sid=io.github.knaitoe.theoldesthouse.labyrinth.WeaponHistory.identity(made);
            h.assertTrue(made.is(Items.IRON_SHOVEL)&&sid!=null&&sid.equals(io.github.knaitoe.theoldesthouse.labyrinth.WeaponHistory.favorite(p)),"a spade from the stand is the reader's original from then on");
            // What is shown on the stand is sent to its reader alone and never saved.
            var shown=io.github.knaitoe.theoldesthouse.house.HouseSitting.STAND_DISPLAY.get().create(h.getLevel());
            h.assertTrue(shown!=null,"the stand's display is registered");
            shown.reader(p.getUUID());shown.present(new ItemStack(Items.IRON_SWORD),net.minecraft.core.Direction.EAST);
            h.assertTrue(shown.broadcastToPlayer(p)&&!shown.broadcastToPlayer(other)&&!shown.shouldBeSaved()&&shown.shown().is(Items.IRON_SWORD),"shown to its reader alone, never saved");
            // How it stands, through the game's own turns: an item display is drawn turned half round (DisplayRenderer's
            // ItemDisplayRenderer), and a flat thing held the item frame's way is turned half round again by its model. A sword stands on
            // its point with its face to the hall, east of a stand facing east; a block sits on the tray's floor, a face to the hall.
            var east=net.minecraft.core.Direction.EAST;
            var sword=io.github.knaitoe.theoldesthouse.house.HallStandDisplay.look(new ItemStack(Items.IRON_SWORD),east);
            var drawn=new org.joml.Matrix4f(sword.transform().getMatrix()).rotateY((float)Math.PI).rotateY((float)Math.PI);
            var point=drawn.transformDirection(new org.joml.Vector3f(.5F,.5F,0));var face=drawn.transformDirection(new org.joml.Vector3f(0,0,1));
            h.assertTrue(sword.context()==net.minecraft.world.item.ItemDisplayContext.FIXED&&point.y<-.48F&&Math.abs(point.x)<.01F&&Math.abs(point.z)<.01F&&face.x>.69F,
                "a sword stands on its point, its face to the hall: point "+point+", face "+face);
            float tip=sword.transform().getTranslation().y()+point.y;
            h.assertTrue(tip>.5F/16&&tip<1.5F/16,"its point a pixel above the tray's floor: "+tip);
            var log=io.github.knaitoe.theoldesthouse.house.HallStandDisplay.look(new ItemStack(Items.OAK_LOG),east);
            var cube=new org.joml.Matrix4f(log.transform().getMatrix()).rotateY((float)Math.PI);var side=cube.transformDirection(new org.joml.Vector3f(0,0,1));
            float bottom=log.transform().getTranslation().y()-log.transform().getScale().y()/2;
            h.assertTrue(log.context()==net.minecraft.world.item.ItemDisplayContext.NONE&&side.x>.29F&&Math.abs(bottom-.5F/16)<.001F,"a block sits on the tray's floor, a face to the hall: "+side+", "+bottom);
            data.forget(p.getUUID());data.forget(other.getUUID());
            NativeTestPlayers.remove(p);NativeTestPlayers.remove(other);
        }).thenSucceed();
    }

    /**
     * 0.4.267 (the owner: "Add 'retrieve belongings', restore equipment to its previous slots where possible, and expose overflow
     * through pages or an overflow count"): a drawer of more than six rows turns in pages; "Retrieve belongings" puts each thing back in
     * the slot it was taken from, armour and the off hand included, then fills the free room, and leaves in the drawer only what will
     * not fit; what comes out is loose; the slots survive a save.
     */
    @GameTest(template="empty",batch="hall_drawers_back",timeoutTicks=200)
    public static void retrievingPutsThingsBackWhereTheyWereCarriedAndTheDrawerTurnsInPages(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"drawers_returner");var server=h.getLevel().getServer();var data=HouseKeeping.get(server);data.forget(p.getUUID());
        h.startSequence().thenExecute(()->{
            var inv=p.getInventory();inv.clearContent();
            HouseKeeping.update(p,Where.OUTSIDE);
            inv.setItem(0,new ItemStack(Items.COBBLESTONE,64));inv.setItem(1,new ItemStack(Items.COBBLESTONE,64));inv.setItem(4,new ItemStack(Items.IRON_SWORD));
            inv.setItem(7,new ItemStack(Items.BREAD,3));inv.armor.set(3,new ItemStack(Items.IRON_HELMET));inv.offhand.set(0,new ItemStack(Items.SHIELD));
            HouseKeeping.update(p,Where.MANOR);HouseKeeping.update(p,Where.BEYOND);HouseKeeping.update(p,Where.MANOR);
            var keep=HouseKeeping.of(p);
            h.assertTrue(inv.getItem(4).isEmpty()&&inv.armor.get(3).isEmpty()&&inv.offhand.get(0).isEmpty()&&count(p,Items.BREAD)==3,"taken past the hall, bread let in");
            h.assertTrue(keep.homes.stream().anyMatch(o->o.slot==4&&o.like.is(Items.IRON_SWORD))&&keep.homes.stream().anyMatch(o->o.slot==39&&o.like.is(Items.IRON_HELMET))&&keep.homes.stream().anyMatch(o->o.slot==40&&o.like.is(Items.SHIELD)),"the House remembers where each thing was: "+keep.homes.size());
            var loaded=HouseKeeping.load(data.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess()).keep(p.getUUID());
            h.assertTrue(loaded.homes.size()==keep.homes.size()&&loaded.homes.stream().anyMatch(o->o.slot==4&&o.count==1),"and still remembers after a save");
            // Things put away on other days: sixty named stones, more than one page.
            for(int i=0;i<60;i++){var stone=new ItemStack(Items.STONE,8);stone.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Stone "+i));keep.drawer.add(stone);}
            int total=keep.drawer.size();
            HouseKeeping.open(p,p.blockPosition());
            h.assertTrue(p.containerMenu instanceof HouseKeeping.Drawer,"the drawer opens as its six rows");var menu=(HouseKeeping.Drawer)p.containerMenu;
            h.assertTrue(menu.view().pages()==2&&menu.view().page()==0,"more than six rows turn into pages: "+menu.view().pages());
            h.assertTrue(menu.clickMenuButton(p,HouseKeeping.NEXT)&&menu.view().page()==1,"the next page");
            int shown=0;for(int i=0;i<HouseKeeping.SHOWN;i++)if(!menu.getSlot(i).getItem().isEmpty())shown++;
            h.assertTrue(shown==total-HouseKeeping.SHOWN,"the second page shows the rest: "+shown);
            h.assertTrue(!menu.clickMenuButton(p,HouseKeeping.NEXT)||menu.view().page()==1,"no page past the last");
            h.assertTrue(menu.clickMenuButton(p,HouseKeeping.PREVIOUS)&&menu.view().page()==0,"and back");
            h.assertTrue(keep.drawer.size()==total,"turning pages loses nothing");
            // A slot taken up since: the second stack of stone goes where there is room.
            inv.setItem(1,new ItemStack(Items.APPLE));
            h.assertTrue(menu.clickMenuButton(p,HouseKeeping.RETRIEVE),"retrieve belongings");
            h.assertTrue(inv.getItem(4).is(Items.IRON_SWORD),"the sword back in its own slot");
            h.assertTrue(inv.armor.get(3).is(Items.IRON_HELMET),"the helmet back on");
            h.assertTrue(inv.offhand.get(0).is(Items.SHIELD),"the shield back in the off hand");
            h.assertTrue(inv.getItem(0).is(Items.COBBLESTONE)&&inv.getItem(0).getCount()==64&&count(p,Items.COBBLESTONE)==128&&inv.getItem(1).is(Items.APPLE),"the stone back where it was, the rest of it where there was room");
            int free=0;for(int i=0;i<Inventory.INVENTORY_SIZE;i++)if(inv.getItem(i).isEmpty())free++;
            h.assertTrue(free==0&&count(keep.drawer,Items.STONE)>0&&count(p,Items.STONE)>0,"the pockets are filled and what will not fit stays in the drawer");
            h.assertTrue(count(keep.loose,Items.IRON_SWORD)==1&&count(keep.loose,Items.COBBLESTONE)==128&&count(keep.loose,Items.STONE)==count(p,Items.STONE),"what came out is loose, as if taken by hand");
            h.assertTrue(keep.homes.stream().noneMatch(o->o.like.is(Items.IRON_SWORD)),"a slot gone back to is forgotten");
            int inDrawer=0;for(int i=0;i<HouseKeeping.SHOWN;i++)if(!menu.getSlot(i).getItem().isEmpty())inDrawer++;
            h.assertTrue(inDrawer==keep.drawer.size()&&menu.view().pages()==1,"the drawer shows what is left: "+inDrawer);
            p.closeContainer();
            data.forget(p.getUUID());
            NativeTestPlayers.remove(p);
        }).thenSucceed();
    }
}
