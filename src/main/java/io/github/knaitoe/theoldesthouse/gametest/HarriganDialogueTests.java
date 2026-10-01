package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.authlib.GameProfile;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HarriganDialogueTests {
    /** Keep the real ServerPlayer/lectern path while observing the text sent to the explorer. */
    private static final class Listener extends FakePlayer {
        final List<String> messages = new ArrayList<>();
        Listener(ServerLevel level) { super(level,new GameProfile(UUID.randomUUID(),"harrigan_reader")); }
        @Override public void displayClientMessage(Component message, boolean actionBar) { messages.add(message.getString()); }
        boolean heard(String words) { return messages.stream().anyMatch(s->s.contains(words)); }
        long heardCount(String words) { return messages.stream().filter(s->s.contains(words)).count(); }
    }
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerLevel level;
        final BlockPos base;
        final HouseSavedData oldHouse;
        final LabyrinthData oldData;
        final Listener player;
        final Set<ChunkPos> chunks = new HashSet<>();
        int menuId;
        Fixture(GameTestHelper h,int coordinate,boolean legacyTicket) {
            helper=h;level=HouseTestLevel.get(h.getLevel().getServer());var server=level.getServer();
            oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);BlockPos origin=new BlockPos(coordinate,80,coordinate);
            var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);
            var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);CompoundTag state=data.state(HarriganVignette.ID);
            state.putInt("Visit",1);state.putBoolean("TicketVisible",legacyTicket);data.setState(HarriganVignette.ID,state);
            server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);base=LabyrinthPlaces.base(origin,LabyrinthPlace.HARRIGAN);
            for(int x=(base.getX()-9)>>4;x<=(base.getX()+9)>>4;x++)for(int z=(base.getZ()-26)>>4;z<=(base.getZ()+3)>>4;z++) {
                var chunk=new ChunkPos(x,z);chunks.add(chunk);level.getChunkSource().addRegionTicket(TicketType.PORTAL,chunk,3,base);level.getChunk(x,z);
            }
            HarriganVignette.build(server,level,base);
            player=new Listener(level);player.moveTo(Vec3.atBottomCenterOf(base.offset(HarriganVignette.LECTERN).south()));player.setNoGravity(true);
            level.addNewPlayer(player);
        }
        AABB bounds() { return new AABB(base.getX()-8,base.getY()-2,base.getZ()-25,base.getX()+9,base.getY()+7,base.getZ()+3); }
        LabyrinthData data() { return LabyrinthData.get(level.getServer()); }
        LecternMenu open() {
            BlockPos at=base.offset(HarriganVignette.LECTERN);
            NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,at,new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false)));
            var lectern=(LecternBlockEntity)level.getBlockEntity(at);
            player.containerMenu=lectern.createMenu(++menuId,player.getInventory(),player);
            helper.assertTrue(player.containerMenu instanceof LecternMenu,"the authored lectern provides its actual native menu");
            return (LecternMenu)player.containerMenu;
        }
        void page(int page) {
            var menu=(LecternMenu)player.containerMenu;
            helper.assertTrue(menu.clickMenuButton(player,100+page)&&menu.getPage()==page,"the real lectern page button changes the native page");
        }
        void closeBook() {
            var menu=player.containerMenu;NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Close(player,menu));menu.removed(player);player.containerMenu=player.inventoryMenu;
        }
        ItemFrame prop(Item item) { return level.getEntitiesOfClass(ItemFrame.class,bounds(),e->e.getItem().is(item)).getFirst(); }
        void take(ItemFrame frame) { NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(player,InteractionHand.MAIN_HAND,frame)); }
        public void close() {
            HarriganVignette.clearAll(level.getServer());player.discard();
            for(var entity:level.getEntitiesOfClass(Entity.class,bounds(),e->e instanceof ArmorStand||e instanceof ItemFrame))entity.discard();
            for(var chunk:chunks)level.getChunkSource().removeRegionTicket(TicketType.PORTAL,chunk,3,base);
            level.getServer().overworld().getDataStorage().set("the_oldest_house",oldHouse);
            level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);
        }
    }
    private static Fixture reading,legacy,phones;
    @AfterBatch(batch="harrigan_reading") public static void cleanReading(ServerLevel level){if(reading!=null){reading.close();reading=null;}}
    @AfterBatch(batch="harrigan_legacy_dialogue") public static void cleanLegacy(ServerLevel level){if(legacy!=null){legacy.close();legacy=null;}}
    @AfterBatch(batch="harrigan_phone_guidance") public static void cleanPhones(ServerLevel level){if(phones!=null){phones.close();phones=null;}}

    @GameTest(template="empty",batch="harrigan_reading",timeoutTicks=460)
    public static void readingPausesResumesAndDismissesOnceWithoutLaterGenericDialogue(GameTestHelper h) {
        reading=new Fixture(h,26800,false);Fixture f=reading;
        h.runAfterDelay(10,()->{f.open();f.page(0);});
        h.runAfterDelay(25,()->{
            f.closeBook();h.assertTrue(!f.data().state(HarriganVignette.ID).getBoolean("TicketVisible")&&!f.player.heard("That's enough for today"),"closing before the ending pauses the reading without a reward or dismissal");
            f.open();f.page(1);
        });
        h.runAfterDelay(40,()->f.page(2));
        h.runAfterDelay(55,()->f.page(1));
        h.runAfterDelay(70,()->f.page(2));
        h.runAfterDelay(85,()->f.page(3));
        h.runAfterDelay(100,()->f.page(4));
        h.runAfterDelay(115,()->{
            f.closeBook();
            h.assertTrue(f.player.heardCount("She kept going")==1&&f.player.heardCount("A light in a window")==1&&f.player.heardCount("Roads are promises")==1
                    &&f.player.heardCount("This is the last page")==1,"forward pages produce the authored sequence once; backtracking cannot cycle responses");
            h.assertTrue(f.player.heardCount("That's enough for today")==1&&f.data().state(HarriganVignette.ID).getBoolean("ReadingFinished"),"the final close gives one saved dismissal");
            int before=f.player.messages.size();f.open();f.closeBook();h.assertTrue(f.player.messages.size()==before,"reopening a finished reading cannot repeat the closing line");
        });
        h.runAfterDelay(130,()->{
            f.take(f.prop(LabyrinthRegistry.SCRATCH_TICKET.get()));
            h.assertTrue(f.player.getInventory().countItem(Items.EMERALD)==12&&f.player.getInventory().countItem(LabyrinthRegistry.PHONE.get())==1,"the actual ticket still grants twelve emeralds and one owned phone");
            h.assertTrue(!WitnessAccount.has(f.data(),f.player.getUUID(),WitnessAccount.Story.HARRIGAN),"the reading and gift do not award the funeral's resolution");
            var loaded=LabyrinthData.FACTORY.deserializer().apply(f.data().save(new CompoundTag(),f.level.registryAccess()),f.level.registryAccess());
            f.level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);HarriganVignette.clearAll(f.level.getServer());
        });
        h.runAfterDelay(410,()->{
            var npc=f.player.messages.stream().filter(s->s.startsWith("Mr. Harrigan:")).toList();
            h.assertTrue(npc.getLast().contains("That's enough for today")&&f.player.heardCount("That's enough for today")==1,"after native ticks and a saved-data reload the dismissal remains the last spoken line");
            h.assertTrue(f.level.getEntitiesOfClass(ItemFrame.class,f.bounds(),e->e.getItem().is(LabyrinthRegistry.SCRATCH_TICKET.get())).isEmpty(),"repeated menus cannot respawn an already scratched ticket");h.succeed();
        });
    }
    @GameTest(template="empty",batch="harrigan_legacy_dialogue",timeoutTicks=360)
    public static void anOlderVisibleTicketAlreadyMeansTheReadingIsOver(GameTestHelper h) {
        legacy=new Fixture(h,27100,true);Fixture f=legacy;WitnessAccount.resolve(f.data(),f.player.getUUID(),WitnessAccount.Story.HARRIGAN,"kept_phone");
        h.runAfterDelay(15,()->{
            h.assertTrue(f.level.getEntitiesOfClass(ItemFrame.class,f.bounds(),e->e.getItem().is(LabyrinthRegistry.SCRATCH_TICKET.get())).size()==1,"the old scene really has its ticket");
            f.open();f.page(4);f.closeBook();
        });
        h.runAfterDelay(310,()->{
            h.assertTrue(f.player.messages.stream().noneMatch(s->s.startsWith("Mr. Harrigan:")),"older finished readings remain silent without a new flag or a repeat dismissal");
            h.assertTrue(f.level.getEntitiesOfClass(ItemFrame.class,f.bounds(),e->e.getItem().is(LabyrinthRegistry.SCRATCH_TICKET.get())).size()==1
                    &&WitnessAccount.has(f.data(),f.player.getUUID(),WitnessAccount.Story.HARRIGAN),"the finite ticket and recorded resolution survive the upgrade");h.succeed();
        });
    }
    @GameTest(template="empty",batch="harrigan_phone_guidance",timeoutTicks=110)
    public static void bothPhonesExplainTheirUsesAndEitherFuneralChoiceKeepsNativeProgress(GameTestHelper h) {
        phones=new Fixture(h,27400,false);Fixture f=phones;
        h.runAfterDelay(10,()->{f.open();f.page(4);f.closeBook();});
        h.runAfterDelay(25,()->{
            f.take(f.prop(LabyrinthRegistry.SCRATCH_TICKET.get()));
            h.assertTrue(f.player.heard("spare phone")&&f.player.heard("time back home"),"the real gift explains whose phone it is and why its clock matters");
            var owned=f.player.getInventory().items.stream().filter(s->s.is(LabyrinthRegistry.PHONE.get())).findFirst().orElseThrow();
            var help=owned.getTooltipLines(Item.TooltipContext.of(f.level),f.player,TooltipFlag.NORMAL).stream().map(Component::getString).toList();
            h.assertTrue(help.stream().anyMatch(s->s.contains("Outside the House"))&&help.stream().anyMatch(s->s.contains("type a name in chat"))
                    &&help.stream().anyMatch(s->s.contains("Overworld bed")),"the inventory tooltip retains controls, place and delayed effect");
            HarriganVignette.onArrive(f.player,LabyrinthPlace.HARRIGAN);
        });
        h.runAfterDelay(45,()->{
            h.assertTrue(f.player.heard("funeral room")&&f.player.heard("Your spare phone stays yours"),"the second visit explains the two different phones");
            f.take(f.prop(LabyrinthRegistry.HARRIGANS_PHONE.get()));
            h.assertTrue(f.player.heard("right-click the casket")&&f.player.getInventory().countItem(LabyrinthRegistry.HARRIGANS_PHONE.get())==1,"taking his real phone explains both the keepsake and burial action");
            var his=f.player.getInventory().items.stream().filter(s->s.is(LabyrinthRegistry.HARRIGANS_PHONE.get())).findFirst().orElseThrow();
            var help=his.getTooltipLines(Item.TooltipContext.of(f.level),f.player,TooltipFlag.NORMAL).stream().map(Component::getString).toList();
            h.assertTrue(help.stream().anyMatch(s->s.contains("keepsake"))&&help.stream().anyMatch(s->s.contains("casket")),"the existing keepsake item ID now retains the choice in its tooltip");
            HarriganVignette.onDepart(f.player);
            h.assertTrue(WitnessAccount.has(f.data(),f.player.getUUID(),WitnessAccount.Story.HARRIGAN)&&f.player.heard("his number has no connection"),"keeping his phone preserves the personal resolution and explains the silent number");
            f.player.moveTo(Vec3.atBottomCenterOf(f.base.offset(HarriganVignette.CASKET).south()));
            for(int slot=0;slot<f.player.getInventory().items.size();slot++)if(f.player.getInventory().getItem(slot).is(LabyrinthRegistry.HARRIGANS_PHONE.get())){f.player.getInventory().selected=slot;break;}
            BlockPos at=f.base.offset(HarriganVignette.CASKET);
            NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(f.player,InteractionHand.MAIN_HAND,at,new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false)));
            h.assertTrue(f.player.getInventory().countItem(LabyrinthRegistry.HARRIGANS_PHONE.get())==0&&f.player.getInventory().countItem(LabyrinthRegistry.PHONE.get())==1,"native burial consumes only his phone and preserves the owned phone");
            h.assertTrue(f.player.heard("within 30 seconds")&&f.player.heard("One call a day")&&f.player.heard("64 blocks")&&f.player.heard("harmless creatures"),"the live connection explains the actual call controls, limits and consequences");
            var own=f.player.getInventory().items.stream().filter(s->s.is(LabyrinthRegistry.PHONE.get())).findFirst().orElseThrow();HarriganVignette.beginPhone(f.player,own);
            h.assertTrue(f.player.heard("Calls cannot leave the House"),"using the phone in the House explains dead air instead of giving an unexplained failure");h.succeed();
        });
    }
}
