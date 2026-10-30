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

@GameTestHolder(TheOldestHouse.MOD_ID+"_wake")
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
            for(var entity:level.getEntitiesOfClass(Entity.class,bounds(),e->e instanceof LiteraryActor||e instanceof ItemFrame))entity.discard();
            for(var chunk:chunks)level.getChunkSource().removeRegionTicket(TicketType.PORTAL,chunk,3,base);
            level.getServer().overworld().getDataStorage().set("the_oldest_house",oldHouse);
            level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);
        }
    }
    private static Fixture reading,legacy,phones,observer,deadReload,casketReload;
    @AfterBatch(batch="harrigan_dead_reload0506") public static void cleanDeadReload(ServerLevel level){if(deadReload!=null){deadReload.close();deadReload=null;}}
    @AfterBatch(batch="harrigan_casket_reload0506") public static void cleanCasketReload(ServerLevel level){if(casketReload!=null){casketReload.close();casketReload=null;}}
    @GameTest(template="empty",batch="harrigan_dead_reload0506",timeoutTicks=220)
    public static void aLateOriginalCannotAnswerFromItsLivingPhaseAfterTheSecondVisitWasSaved(GameTestHelper h){
        deadReload=new Fixture(h,27900,false);var f=deadReload;
        final java.util.UUID[] original={null};final float[] head={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(HarriganVignette.harrigan(f.level,f.base,-1)!=null,"the original first-visit body has loaded"))
            .thenExecute(()->{
                var body=HarriganVignette.harrigan(f.level,f.base,-1);original[0]=body.getUUID();
                var saved=new CompoundTag();body.saveWithoutId(saved);
                var state=f.data().state(HarriganVignette.ID);state.putInt("Visit",2);state.putBoolean("ReadingFinished",true);f.data().setState(HarriganVignette.ID,state);
                // This is the body's actual pre-arrival NBT, restored after the story's newer saved visit.
                body.load(saved);f.player.moveTo(body.position().add(0,0,-2));f.player.messages.clear();
                body.interact(f.player,InteractionHand.MAIN_HAND);
                h.assertTrue(f.player.messages.isEmpty(),"even before the next scene tick, the dead original neither speaks nor responds to a click");
            }).thenIdle(25).thenExecute(()->{
                var body=HarriganVignette.harrigan(f.level,f.base,-1);head[0]=body.getYHeadRot();
                h.assertTrue(body.getUUID().equals(original[0])&&body.phase()==HarriganVignette.DEAD_PHASE&&body.isNoAi()&&body.isNoGravity()&&body.isSilent(),"native ticks reconcile the same UUID to the motionless dead chair state");
                f.player.moveTo(body.position().add(2,0,0));f.player.messages.clear();body.interact(f.player,InteractionHand.MAIN_HAND);
            }).thenIdle(25).thenExecute(()->{
                var body=HarriganVignette.harrigan(f.level,f.base,-1);
                h.assertTrue(body.getYHeadRot()==head[0]&&f.player.messages.isEmpty(),"he does not follow the reader's head or react to another interaction");
                h.assertTrue(!WitnessAccount.has(f.data(),f.player.getUUID(),WitnessAccount.Story.HARRIGAN)&&f.player.getInventory().isEmpty(),"the dead body grants neither ending credit nor items");
            }).thenSucceed();
    }
    @GameTest(template="empty",batch="harrigan_casket_reload0506",timeoutTicks=220)
    public static void anOlderBodyLoadedAfterTheFuneralSetupIsPlacedInItsActualCasketWithoutAnotherArrival(GameTestHelper h){
        casketReload=new Fixture(h,28200,false);var f=casketReload;final java.util.UUID[] original={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(HarriganVignette.harrigan(f.level,f.base,-1)!=null,"the original body is loaded before its older entity snapshot"))
            .thenExecute(()->{
                var body=HarriganVignette.harrigan(f.level,f.base,-1);original[0]=body.getUUID();var saved=new CompoundTag();body.saveWithoutId(saved);
                var state=f.data().state(HarriganVignette.ID);state.putInt("Visit",3);f.data().setState(HarriganVignette.ID,state);
                HarriganVignette.onArrive(f.player,LabyrinthPlace.HARRIGAN);
                h.assertTrue(f.data().state(HarriganVignette.ID).getBoolean("FuneralHome0505"),"the real third-visit setup has built its funeral home");
                body.load(saved);f.player.moveTo(f.base.getX()+2.5,f.base.getY(),f.base.getZ()-20);f.player.messages.clear();
            }).thenIdle(25).thenExecute(()->{
                var body=HarriganVignette.harrigan(f.level,f.base,-1);var at=f.base.offset(HarriganVignette.CASKET);
                h.assertTrue(body.getUUID().equals(original[0])&&body.phase()==HarriganVignette.CASKET_PHASE&&body.position().distanceToSqr(new Vec3(at.getX()+.5,at.getY()+.9,at.getZ()+.5))<.0001,"ordinary native ticks bring the late original into the actual casket, with no new arrival or replacement");
                h.assertTrue(f.level.getEntitiesOfClass(LiteraryActor.class,f.bounds(),a->a.role()==LiteraryActor.HARRIGAN).size()==1&&f.level.getBlockState(at).is(LabyrinthRegistry.HARRIGAN_FINISH.get()),"one body rests in the shipped casket block");
                h.assertTrue(f.level.getEntitiesOfClass(LiteraryActor.class,f.bounds(),a->a.role()==LiteraryActor.HARRIGAN_MOURNER).size()==3,"his original three mourners remain present");
                f.player.messages.clear();body.interact(f.player,InteractionHand.MAIN_HAND);
                h.assertTrue(f.player.messages.isEmpty()&&!WitnessAccount.has(f.data(),f.player.getUUID(),WitnessAccount.Story.HARRIGAN),"the casket body remains silent and the kept/buried-phone decision is still required");
                var saved=new CompoundTag();body.saveWithoutId(saved);body.load(saved);
                h.assertTrue(body.getUUID().equals(original[0])&&body.phase()==HarriganVignette.CASKET_PHASE&&body.getBbHeight()==.5F,"native save/load keeps the original corpse identity, phase and low collision body");
            }).thenSucceed();
    }
    @AfterBatch(batch="harrigan_observer0493") public static void cleanObserver(ServerLevel level){if(observer!=null){observer.close();observer=null;}}
    @GameTest(template="empty",batch="harrigan_observer0493",timeoutTicks=120)
    public static void anObserverCannotFinishTheReadingOrTakeItsOriginalGift(GameTestHelper h){
        observer=new Fixture(h,27600,false);var f=observer;
        h.runAfterDelay(10,()->{f.player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SPECTATOR);f.open();f.page(4);});
        h.runAfterDelay(30,()->{
            f.closeBook();var state=f.data().state(HarriganVignette.ID);
            h.assertTrue(!state.getBoolean("ReadingStarted")&&!state.getBoolean("ReadingFinished")&&!state.getBoolean("TicketVisible")&&f.player.messages.stream().noneMatch(line->line.startsWith("Mr. Harrigan:")),"native observer pages and closing neither start the performance nor release the original gift");
            f.player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);f.open();f.page(4);f.closeBook();
        });
        h.runAfterDelay(50,()->{
            h.assertTrue(f.data().state(HarriganVignette.ID).getBoolean("TicketVisible"),"the actual participating reader can still finish the same preserved book");
            var ticket=f.prop(LabyrinthRegistry.SCRATCH_TICKET.get());var id=ticket.getUUID();f.player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SPECTATOR);f.take(ticket);
            h.assertTrue(!ticket.isRemoved()&&ticket.getUUID().equals(id)&&f.player.getInventory().countItem(Items.EMERALD)==0,"an observer interaction does not consume or replenish the original ticket");
            f.player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);f.take(ticket);
            h.assertTrue(f.player.getInventory().countItem(Items.EMERALD)==12&&f.player.getInventory().countItem(LabyrinthRegistry.PHONE.get())==1,"the preserved original remains available once to the participant");h.succeed();
        });
    }
    private static HouseSavedData phoneHouse;private static LabyrinthData phoneData;private static final List<ServerPlayer> PHONE_READERS=new ArrayList<>();
    @AfterBatch(batch="harrigan_phone_screen0505") public static void cleanPhoneScreen(ServerLevel level){
        for(var p:PHONE_READERS)NativeTestPlayers.remove(p);PHONE_READERS.clear();HarriganVignette.clearAll(level.getServer());
        if(phoneHouse!=null){level.getServer().overworld().getDataStorage().set("the_oldest_house",phoneHouse);level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",phoneData);phoneHouse=null;phoneData=null;}
    }
    private static void line(LabyrinthData data,ServerPlayer p,java.util.function.Consumer<CompoundTag> change){
        var state=data.state(HarriganVignette.ID);var players=state.getCompound("Players");var own=players.getCompound(p.getUUID().toString());change.accept(own);players.put(p.getUUID().toString(),own);state.put("Players",players);data.setState(HarriganVignette.ID,state);
    }
    private static CompoundTag own(LabyrinthData data,ServerPlayer p){return data.state(HarriganVignette.ID).getCompound("Players").getCompound(p.getUUID().toString());}
    private static String last(LabyrinthData data,ServerPlayer p,int back){var thread=own(data,p).getList("Thread0505",net.minecraft.nbt.Tag.TAG_COMPOUND);return thread.size()<=back?"":thread.getCompound(thread.size()-1-back).getString("Text");}
    /** 0.4.105: the phone's screen texts and calls him; a name does what the phone's notes say, once a day, and nothing leaves the House. */
    @GameTest(template="empty",batch="harrigan_phone_screen0505",timeoutTicks=100)
    public static void thePhoneTextsAndCallsHimByItsNotes(GameTestHelper h){
        var server=h.getLevel().getServer();phoneHouse=HouseSavedData.get(server);phoneData=LabyrinthData.get(server);
        var house=new HouseSavedData();house.markSpawned(new BlockPos(27800,80,27800));server.overworld().getDataStorage().set("the_oldest_house",house);
        var data=new LabyrinthData();server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
        var caller=NativeTestPlayers.survival(h,"phone_caller");var kept=NativeTestPlayers.survival(h,"phone_keeper");var inside=NativeTestPlayers.survival(h,"phone_inside");
        PHONE_READERS.addAll(List.of(caller,kept,inside));
        for(var p:PHONE_READERS)p.getInventory().add(HarriganVignette.phoneFor(p));
        caller.setRespawnPosition(net.minecraft.world.level.Level.OVERWORLD,caller.blockPosition(),0,true,false);
        line(data,caller,t->t.putBoolean("Contact",true));line(data,inside,t->t.putBoolean("Contact",true));
        line(data,kept,t->t.putBoolean("KeptHarriganPhone",true));kept.getInventory().add(new ItemStack(LabyrinthRegistry.HARRIGANS_PHONE.get()));
        inside.teleportTo(HouseTestLevel.get(server),0.5,80,0.5,0,0);
        h.runAfterDelay(5,()->{
            HarriganVignette.phoneCall(caller);
            h.assertTrue(last(data,caller,0).equals("Call · his voicemail"),"a connected call rings through to his voicemail: "+last(data,caller,0));
            HarriganVignette.phoneText(caller,"zombie",false);
            h.assertTrue(last(data,caller,1).equals("zombie")&&last(data,caller,0).equals("delivered"),"a text goes to him and he answers it");
            h.assertTrue(own(data,caller).getString("HostileType").equals("minecraft:zombie")&&own(data,caller).getLong("HostileDay")==HouseCalendar.today(server)+1,"naming a hostile species marks it for the next dawn by the caller's bed");
            HarriganVignette.phoneText(caller,"skeleton",false);
            h.assertTrue(last(data,caller,0).equals("No answer.")&&own(data,caller).getString("HostileType").equals("minecraft:zombie"),"one a day: a second name gets no answer and changes nothing");
            HarriganVignette.phoneCall(caller);
            h.assertTrue(last(data,caller,0).equals("Call · no answer"),"and a second call that day goes unanswered");
            line(data,caller,t->t.putLong("LastCallDay",HouseCalendar.today(server)-1));
            HarriganVignette.phoneText(caller,"creeper",true);
            h.assertTrue(last(data,caller,1).equals("Voicemail: creeper")&&last(data,caller,0).equals("i heard you")&&own(data,caller).getString("HostileType").equals("minecraft:creeper"),"a name left on his voicemail counts as the day's call");
            HarriganVignette.phoneCall(kept);HarriganVignette.phoneText(kept,"zombie",false);
            h.assertTrue(last(data,kept,2).equals("Call · it rang in your pocket")&&last(data,kept,0).equals("His phone buzzes in your pocket.")&&!own(data,kept).contains("HostileType"),"with his phone kept, his number rings in the caller's own pocket and nothing reaches him");
            HarriganVignette.phoneText(inside,"zombie",false);HarriganVignette.phoneCall(inside);
            h.assertTrue(last(data,inside,1).equals("Not delivered. No service.")&&last(data,inside,0).equals("Call · no service")&&!own(data,inside).contains("HostileType"),"nothing leaves the House");
            var shown=HarriganVignette.phoneState(caller,null);
            h.assertTrue(shown.getBoolean("Service")&&shown.getString("Line").equals("connected")&&shown.getList("Thread",net.minecraft.nbt.Tag.TAG_COMPOUND).size()==own(data,caller).getList("Thread0505",net.minecraft.nbt.Tag.TAG_COMPOUND).size()&&shown.getBoolean("Today"),"the screen is sent the clock, the line and the whole thread");
            h.succeed();
        });
    }
    @AfterBatch(batch="harrigan_reading") public static void cleanReading(ServerLevel level){if(reading!=null){reading.close();reading=null;}}
    @AfterBatch(batch="harrigan_legacy_dialogue") public static void cleanLegacy(ServerLevel level){if(legacy!=null){legacy.close();legacy=null;}}
    @AfterBatch(batch="harrigan_phone_guidance") public static void cleanPhones(ServerLevel level){if(phones!=null){phones.close();phones=null;}}

    @GameTest(template="empty",batch="harrigan_reading",timeoutTicks=460)
    public static void readingPausesResumesAndDismissesOnceWithoutLaterGenericDialogue(GameTestHelper h) {
        reading=new Fixture(h,26800,false);Fixture f=reading;
        h.runAfterDelay(9,()->h.assertTrue(f.player.messages.stream().noneMatch(line->line.startsWith("Mr. Harrigan:")),"entering the study cannot start commentary before the actual reading"));
        h.runAfterDelay(10,()->{f.open();f.page(0);});
        h.runAfterDelay(25,()->{
            f.closeBook();h.assertTrue(!f.data().state(HarriganVignette.ID).getBoolean("TicketVisible")&&!f.player.heard("That's enough for today"),"closing before the ending pauses the reading without a reward or dismissal");
            var phone=f.prop(LabyrinthRegistry.PHONE.get());var id=phone.getUUID();for(int n=0;n<4;n++)HarriganVignette.onArrive(f.player,LabyrinthPlace.HARRIGAN);h.assertTrue(f.level.getEntitiesOfClass(ItemFrame.class,f.bounds(),frame->frame.getItem().is(LabyrinthRegistry.PHONE.get())).size()==1&&f.prop(LabyrinthRegistry.PHONE.get()).getUUID().equals(id),"repeated staging retains the one actual spare phone frame");
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
    @GameTest(template="empty",batch="harrigan_phone_guidance",timeoutTicks=140)
    public static void bothPhonesExplainTheirUsesAndEitherFuneralChoiceKeepsNativeProgress(GameTestHelper h) {
        phones=new Fixture(h,27400,false);Fixture f=phones;
        h.runAfterDelay(10,()->{f.open();f.page(4);f.closeBook();});
        h.runAfterDelay(25,()->{
            f.take(f.prop(LabyrinthRegistry.SCRATCH_TICKET.get()));
            h.assertTrue(f.player.heard("spare phone")&&f.player.heard("time back home"),"the real gift explains whose phone it is and why its clock matters");
            var owned=f.player.getInventory().items.stream().filter(s->s.is(LabyrinthRegistry.PHONE.get())).findFirst().orElseThrow();
            var help=owned.getTooltipLines(Item.TooltipContext.of(f.level),f.player,TooltipFlag.NORMAL).stream().map(Component::getString).toList();
            h.assertTrue(help.stream().anyMatch(s->s.contains("Outside the House"))&&help.stream().anyMatch(s->s.contains("text him a name"))
                    &&help.stream().anyMatch(s->s.contains("Overworld bed")),"the inventory tooltip retains controls, place and delayed effect");
            HarriganVignette.onArrive(f.player,LabyrinthPlace.HARRIGAN);
        });
        h.runAfterDelay(45,()->{
            h.assertTrue(f.player.heard("died in his chair")&&f.player.heard("Your spare phone stays yours"),"the second visit explains the two different phones");
            f.take(f.prop(LabyrinthRegistry.HARRIGANS_PHONE.get()));
            h.assertTrue(f.player.heard("right-click the casket")&&f.player.getInventory().countItem(LabyrinthRegistry.HARRIGANS_PHONE.get())==1,"taking his real phone explains both the keepsake and burial action");
            var his=f.player.getInventory().items.stream().filter(s->s.is(LabyrinthRegistry.HARRIGANS_PHONE.get())).findFirst().orElseThrow();
            var help=his.getTooltipLines(Item.TooltipContext.of(f.level),f.player,TooltipFlag.NORMAL).stream().map(Component::getString).toList();
            h.assertTrue(help.stream().anyMatch(s->s.contains("keepsake"))&&help.stream().anyMatch(s->s.contains("casket")),"the existing keepsake item ID now retains the choice in its tooltip");
            HarriganVignette.onDepart(f.player);
            h.assertTrue(!WitnessAccount.has(f.data(),f.player.getUUID(),WitnessAccount.Story.HARRIGAN),"carried out of the study, his phone is only on its way to the funeral");
            HarriganVignette.onArrive(f.player,LabyrinthPlace.HARRIGAN);
        });
        h.runAfterDelay(65,()->{
            h.assertTrue(f.data().state(HarriganVignette.ID).getInt("Visit")==3&&f.player.heard("made ready for his funeral"),"the third visit is the funeral, and says so");
            HarriganVignette.onDepart(f.player);
            h.assertTrue(WitnessAccount.has(f.data(),f.player.getUUID(),WitnessAccount.Story.HARRIGAN)&&f.player.heard("his number has no connection"),"leaving the funeral with his phone keeps it, resolves the story and explains the silent number");
            f.player.moveTo(Vec3.atBottomCenterOf(f.base.offset(HarriganVignette.CASKET).south()));
            for(int slot=0;slot<f.player.getInventory().items.size();slot++)if(f.player.getInventory().getItem(slot).is(LabyrinthRegistry.HARRIGANS_PHONE.get())){f.player.getInventory().selected=slot;break;}
            BlockPos at=f.base.offset(HarriganVignette.CASKET);
            NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(f.player,InteractionHand.MAIN_HAND,at,new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false)));
            h.assertTrue(f.player.getInventory().countItem(LabyrinthRegistry.HARRIGANS_PHONE.get())==0&&f.player.getInventory().countItem(LabyrinthRegistry.PHONE.get())==1,"native burial consumes only his phone and preserves the owned phone");
            var original=f.player.getInventory().items.stream().filter(stack->stack.is(LabyrinthRegistry.PHONE.get())).findFirst().orElseThrow();var before=original.copy();net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,original,tag->tag.remove("HarriganOwner"));
            h.assertTrue(HarriganVignette.repairLegacyPhone(f.player,original)&&ItemStack.matches(before,original),"the completed reader's blank legacy gift is repaired in place with all other components retained");
            h.assertTrue(!HarriganVignette.repairLegacyPhone(f.player,new ItemStack(LabyrinthRegistry.PHONE.get())),"the same evidence cannot bind another blank phone");
            h.assertTrue(f.player.heard("text him a creature's name")&&f.player.heard("One call a day")&&f.player.heard("64 blocks")&&!f.player.heard("harmless creatures"),"the live connection explains controls and limits without revealing the friendly-mob consequence");
            var own=f.player.getInventory().items.stream().filter(s->s.is(LabyrinthRegistry.PHONE.get())).findFirst().orElseThrow();HarriganVignette.beginPhone(f.player,own);
            h.assertTrue(f.player.heard("Calls cannot leave the House"),"using the phone in the House explains dead air instead of giving an unexplained failure");h.succeed();
        });
    }

    /**
     * 0.4.188, the funeral's choice decides the phone. Kept: he is freed, and once a House day a kind line comes into the
     * reader's thread from his number; he takes no name. Buried: he carries the obligation; the more names, the more his
     * greeting rots, and at the last he rings the reader's own phone at night until it goes to a missed call.
     */
    @GameTest(template="empty",timeoutTicks=200)
    public static void keepingHisPhoneFreesHimAndBuryingItLetsTheRotStart(GameTestHelper h){
        var kept=NativeTestPlayers.survival(h,"kept_his_phone");var buried=NativeTestPlayers.survival(h,"buried_his_phone");var d=LabyrinthData.get(h.getLevel().getServer());
        try{
            var ks=HarriganVignette.playerState(d,kept.getUUID());ks.putBoolean("KeptHarriganPhone",true);HarriganVignette.savePlayerState(d,kept.getUUID(),ks);
            h.assertTrue(HarriganPhones.kind(kept),"a reader who kept his phone hears from him");
            var thread=HarriganVignette.phoneState(kept,null).getList("Thread",10);var last=thread.getCompound(thread.size()-1);
            h.assertTrue(last.getInt("Who")==HarriganVignette.HIM&&Arrays.asList(HarriganPhones.KIND).contains(last.getString("Text")),"a kind line from his number: "+last);
            h.assertTrue(!HarriganPhones.kind(kept),"once a House day");
            h.assertTrue(!HarriganPhones.ring(kept),"freed, he never rings on his own");
            var bs=HarriganVignette.playerState(d,buried.getUUID());bs.putBoolean("Contact",true);bs.putInt("TotalCalls",0);HarriganVignette.savePlayerState(d,buried.getUUID(),bs);
            h.assertTrue(!HarriganPhones.kind(buried),"buried with his phone, he sends nothing kind");
            h.assertTrue(HarriganPhones.greeting(bs).equals("This is Harrigan. I don't pick up this phone. If you have a name for me, leave it after the tone."),"before any name, the greeting is his own");
            bs.putInt("TotalCalls",2);h.assertTrue(HarriganPhones.greeting(bs).contains("…")&&HarriganPhones.greeting(bs).contains("Harrigan"),"after names it slows: "+HarriganPhones.greeting(bs));
            bs.putInt("TotalCalls",HarriganPhones.RINGS_AFTER);h.assertTrue(HarriganPhones.greeting(bs).contains("H rr gan"),"then it loses letters: "+HarriganPhones.greeting(bs));
            HarriganVignette.savePlayerState(d,buried.getUUID(),bs);
            h.assertTrue(!HarriganPhones.ring(buried),"he rings only a reader carrying their own phone");
            buried.getInventory().add(new ItemStack(LabyrinthRegistry.PHONE.get()));
            h.assertTrue(HarriganPhones.ring(buried)&&!HarriganPhones.ring(buried),"at the last he rings on his own, once a night");
            for(int i=0;i<HarriganPhones.RINGS;i++)HarriganPhones.ringing(buried);
            var calls=HarriganVignette.phoneState(buried,null).getList("Thread",10);
            h.assertTrue(calls.getCompound(calls.size()-1).getString("Text").equals("Missed call · Mr. Harrigan"),"it rings out to a missed call");
        }finally{NativeTestPlayers.remove(kept);NativeTestPlayers.remove(buried);}
        h.succeed();
    }
}
