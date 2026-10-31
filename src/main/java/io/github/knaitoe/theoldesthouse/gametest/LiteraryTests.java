package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.*;
/** Native interactions, present-time playback, saved originals and private projection boundaries. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class LiteraryTests {
    private static Fixture active;
    static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final NativeTestChunks chunks=new NativeTestChunks();final ServerLevel in,out;final BlockPos origin;final HouseSavedData oldHouse;final LabyrinthData oldData;final MotherCollection oldMother;final LiteraryCopies oldCopies;final List<ServerPlayer> players=new ArrayList<>();final Set<LabyrinthPlace> built=new HashSet<>();final long oldDay;final List<Entity> extras=new ArrayList<>();
        Fixture(GameTestHelper h){this(h,80);}
        // These fixtures build their selected rooms directly; they do not enter or test the manor terrain copy.
        Fixture(GameTestHelper h,int originY){this.h=h;oldDay=h.getLevel().getServer().overworld().getDayTime();in=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.INTERIOR);out=HouseTestLevel.get(in.getServer(),HouseDimensions.OUTSIDE);oldHouse=HouseSavedData.get(in.getServer());oldData=LabyrinthData.get(in.getServer());oldMother=MotherCollection.get(in.getServer());oldCopies=LiteraryCopies.get(in.getServer());origin=new BlockPos(57000,originY,57000);var house=new HouseSavedData();house.markSpawned(origin);house.markInteriorInitialized();var store=in.getServer().overworld().getDataStorage();store.set("the_oldest_house",house);store.set("the_oldest_house_labyrinth",new LabyrinthData());store.set("the_oldest_house_mother",new MotherCollection());store.set("the_oldest_house_literary_copies",new LiteraryCopies());data().setBuilt(LabyrinthBuilder.VERSION,origin);active=this;}
        LabyrinthData data(){return LabyrinthData.get(in.getServer());}ServerLevel level(LabyrinthPlace p){return NovelRooms.outside(p)?out:in;}BlockPos base(LabyrinthPlace p){return LabyrinthPlaces.base(origin,p);}
        void build(LabyrinthPlace p){if(built.add(p)){LiteraryRooms.build(level(p),base(p),p);LabyrinthBuilder.registerDoors(data(),p,base(p));}}
        ServerPlayer player(String name,LabyrinthPlace place){build(place);var p=NativeTestPlayers.survival(h,name);players.add(p);arrive(p,place);return p;}
        void arrive(ServerPlayer p,LabyrinthPlace place){build(place);var b=base(place);p.stopRiding();p.teleportTo(level(place),b.getX()+.5,b.getY(),b.getZ()-3,180,0);p.hasChangedDimension();p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);LiteraryVignettes.onArrive(p,place);}
        void at(ServerPlayer p,LabyrinthPlace place,double x,double y,double z){var b=LiteraryVignettes.base(p,place);p.stopRiding();p.moveTo(b.getX()+x,b.getY()+y,b.getZ()+z);p.setDeltaMovement(Vec3.ZERO);}
        void look(ServerPlayer p,BlockPos at){p.lookAt(EntityAnchorArgument.Anchor.EYES,at.getCenter());}
        CompoundTag own(ServerPlayer p,LabyrinthPlace place){return LiteraryVignettes.personal(data(),p.getUUID(),place);}
        void click(ServerPlayer p,LabyrinthPlace place,BlockPos rel){var b=LiteraryVignettes.base(p,place);var at=b.offset(rel);p.moveTo(at.getX()+.5,at.getY(),at.getZ()+2.5);p.setDeltaMovement(Vec3.ZERO);look(p,at);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)));}
        void read(ServerPlayer p,LabyrinthPlace place,BlockPos rel){if(place==LabyrinthPlace.SECOND_WAITRESS&&rel.equals(LiteraryRooms.source(place))){var handed=UncannyBreakfast.menu(p);p.setItemInHand(InteractionHand.MAIN_HAND,handed);handed.use(p.serverLevel(),p,InteractionHand.MAIN_HAND);p.closeContainer();handed.use(p.serverLevel(),p,InteractionHand.MAIN_HAND);}else click(p,place,rel);h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,"actual scene surface opens its native private paper: "+place);var menu=(LiteraryVignettes.Pages)p.containerMenu;if(UncannyRooms.isUncanny(place)&&place!=LabyrinthPlace.SECOND_WAITRESS&&rel.equals(LiteraryRooms.source(place))){h.assertTrue(menu.clickMenuButton(p,UncannyReading.FULL),"the full original remains available explicitly");menu=(LiteraryVignettes.Pages)p.containerMenu;}var book=menu.original().get(DataComponents.WRITTEN_BOOK_CONTENT);h.assertTrue(menu.clickMenuButton(p,100+book.pages().size()-1),"the actual final page is read");p.closeContainer();}
        void source(ServerPlayer p,LabyrinthPlace place){read(p,place,LiteraryRooms.source(place));}
        void reload(){var l=in;var store=l.getServer().overworld().getDataStorage();store.set("the_oldest_house_labyrinth",LabyrinthData.FACTORY.deserializer().apply(data().save(new CompoundTag(),l.registryAccess()),l.registryAccess()));}
        @Override public void close(){LiteraryVignettes.clearAll();LiteraryCopies.clearAll();for(var p:players){for(var place:List.of(LabyrinthPlace.FAMILY_COPY,LabyrinthPlace.OLD_CABIN)){var b=LiteraryCopies.base(in.getServer(),p.getUUID(),place);if(b!=null)for(var entity:out.getEntitiesOfClass(Entity.class,new AABB(b.getX()-36,b.getY()-20,b.getZ()-100,b.getX()+37,b.getY()+36,b.getZ()+6)))entity.discard();}NativeTestPlayers.remove(p);}extras.forEach(Entity::discard);chunks.close();in.getServer().overworld().setDayTime(oldDay);for(var place:built){var b=base(place);var r=place.room();for(var e:level(place).getEntitiesOfClass(Entity.class,new AABB(b.getX()+r.minX()-2,b.getY()+r.minY()-2,b.getZ()+r.minZ()-2,b.getX()+r.maxX()+3,b.getY()+r.maxY()+3,b.getZ()+5)))e.discard();}var s=in.getServer().overworld().getDataStorage();s.set("the_oldest_house",oldHouse);s.set("the_oldest_house_labyrinth",oldData);s.set("the_oldest_house_mother",oldMother);s.set("the_oldest_house_literary_copies",oldCopies);}
    }
    private static void close(){if(active!=null){active.close();active=null;}}
    @GameTest(template="empty",batch="literary_papers",timeoutTicks=300)
    public static void discoveryOriginalsAreFiniteReaderOwnedAndImmutableAcrossReload(GameTestHelper h){var f=new Fixture(h);var place=LabyrinthPlace.HILL_NURSERY;var p=f.player("literary_reader",place);var peer=f.player("literary_peer",place);f.at(peer,place,0,0,-6);f.source(p,place);var snapshot=f.own(p,place).getCompound("Original_Source").copy();h.assertTrue(!snapshot.isEmpty(),"opening the note persists its exact original instead of overwriting it with pre-menu state");f.click(p,place,LiteraryRooms.source(place));var menu=(LiteraryVignettes.Pages)p.containerMenu;h.assertTrue(menu.clickMenuButton(p,3)&&!menu.clickMenuButton(p,3),"the native paper may be taken once");p.closeContainer();f.reload();f.source(p,place);h.assertTrue(snapshot.equals(f.own(p,place).getCompound("Original_Source")),"native SavedData reload and rereading preserve the original bytes");h.assertTrue(!f.own(peer,place).getBoolean("Read_Source")&&!f.own(peer,place).getBoolean("Taken_Source"),"one explorer's source is not another's discovery");peer.setGameMode(GameType.SPECTATOR);h.assertTrue(!LiteraryVignettes.inside(peer,place),"native observers have no participation authority");h.succeed();}
    @GameTest(template="empty",batch="literary_one_page",timeoutTicks=300)
    public static void aOnePageSourceIsReadByOpeningItAsTheNativeLecternAllows(GameTestHelper h){var f=new Fixture(h);var place=LabyrinthPlace.WINTER_LAKE;var p=f.player("one_page_reader",place);var peer=f.player("one_page_peer",place);f.at(peer,place,0,0,-6);
        h.assertTrue(LiteraryTexts.source(place).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==1,"Bud's note about the frozen lake is a single page");
        // The native lectern screen shows no page buttons for one page, so no button is pressed here.
        f.click(p,place,LiteraryRooms.source(place));h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,"the actual source surface opens its native private paper");
        h.assertTrue(f.own(p,place).getBoolean("Read_Source"),"opening a one-page paper reads it to the end");h.assertTrue(!f.own(peer,place).getBoolean("Read_Source"),"one reader's opening is not another's");
        p.closeContainer();h.succeed();}
    /** 0.4.105: the lake nights tell one story: the ribbon goes out in winter and is back on Hank's table by June; what she takes, the lake keeps. */
    @GameTest(template="empty") public static void theLakeNightsTellOneStory(GameTestHelper h){
        var night=new CompoundTag();night.putInt("Missing0502",MovieNightCast.JAMIE_BIT);var stranger=MovieNight.account(night);
        h.assertTrue(stranger.contains("nobody drowns in Indian Lake")&&!stranger.contains("boys from the shallows"),"a reader new to the lake hears only what Proofrock says");
        night.putBoolean("Congregation0505",true);night.putBoolean("Shallows0505",true);var known=MovieNight.account(night);
        h.assertTrue(known.contains("the lake keeps its people in rows")&&known.contains("boys from the shallows")&&!known.contains("nobody drowns"),"a reader who walked among the congregation knows where the missing go, and Ray knows a boy from the shallows");
        h.assertTrue(WinterLake.account(new CompoundTag()).contains("In June Hank found it on his table"),"the ribbon given back in winter is on Hank's table by June");
        var costume=new CompoundTag();costume.putBoolean("HankDare0503",true);var contest=CostumeNight.account(costume);
        h.assertTrue(contest.contains("the winner is the same")&&contest.contains("back on his table by June"),"Hank's contest crowns her every year, with the ribbon that came back");
        h.succeed();
    }
    @AfterBatch(batch="literary_papers") public static void papersDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_hill",timeoutTicks=200)
    public static void actualColdDoorNameAndCountedKnocksEarnOnlyTheReadersAccount(GameTestHelper h){var f=new Fixture(h);var place=LabyrinthPlace.HILL_NURSERY;var p=f.player("hill_reader",place);f.source(p,place);f.at(p,place,-9.5,0,-17);f.look(p,f.base(place).offset(LiteraryRooms.HILL_WALL));h.runAfterDelay(10,()->{h.assertTrue(f.own(p,place).getBoolean("ColdDoor")&&f.own(p,place).getBoolean("NameSeen"),"native doorway presence and line of sight are required");f.at(p,place,-9.5,-4,-34);for(int i=0;i<3;i++)NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(p,f.base(place).offset(LiteraryRooms.HILL_KNOCK),Direction.SOUTH,PlayerInteractEvent.LeftClickBlock.Action.START));});h.runAfterDelay(50,()->{h.assertTrue(f.own(p,place).getInt("Knocks")==3&&f.own(p,place).getInt("Echoes")==3&&f.own(p,place).getBoolean("Ready"),"each actual knock returns once at its cellar source");f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.HILL_NURSERY),"reading the own ending grants one eligible source");h.succeed();});}
    @AfterBatch(batch="literary_hill") public static void hillDone(ServerLevel l){close();}
    /** The pounding waits at the reader's nearest wall: a knock back there is answered once. The cold never wounds, and the cellar stair is walled in. */
    @GameTest(template="empty",batch="literary_hill_answer",timeoutTicks=300)
    public static void poundingWaitsForAKnockBackTheColdNeverWoundsAndTheStairIsWalled(GameTestHelper h){var f=new Fixture(h);var place=LabyrinthPlace.HILL_NURSERY;var p=f.player("hill_answer",place);f.source(p,place);f.at(p,place,-6.5,0,-17);var b=f.base(place);
        for(int z=-29;z<=-24;z++)for(int y=-6;y<=-1;y++)for(int x:new int[]{-2,2})h.assertTrue(!f.in.getBlockState(b.offset(x,y,z)).isAir(),"the cellar stair is walled at "+x+","+y+","+z);
        var cold=new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(p,new net.neoforged.neoforge.common.damagesource.DamageContainer(p.damageSources().freeze(),2));NeoForge.EVENT_BUS.post(cold);h.assertTrue(cold.isCanceled(),"the nursery's cold never wounds");
        h.runAfterDelay(100,()->{var own=f.own(p,place);h.assertTrue(own.contains("PoundAt"),"the pounding came to a wall");var at=b.offset(BlockPos.of(own.getLong("PoundAt")));
            NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(p,at,Direction.EAST,PlayerInteractEvent.LeftClickBlock.Action.START));
            h.runAfterDelay(40,()->{var after=f.own(p,place);h.assertTrue(after.getInt("Knocks")==1&&after.getInt("Echoes")==1,"one knock back is answered once: "+after.getInt("Knocks")+"/"+after.getInt("Echoes"));
                NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(p,b.offset(-6,0,-23),Direction.EAST,PlayerInteractEvent.LeftClickBlock.Action.START));h.assertTrue(f.own(p,place).getInt("Knocks")==1,"a knock anywhere else is not an answer");h.succeed();});});}
    @AfterBatch(batch="literary_hill_answer") public static void hillAnswerDone(ServerLevel l){close();}
    /** 0.4.105: standing still through the chime costs nothing; moving puts the reader back a room, once a chime. */
    @GameTest(template="empty",batch="literary_masque_chime",timeoutTicks=900)
    public static void movingWhileTheClockSpeaksPutsTheReaderBackARoom(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.MASQUE;var p=f.player("masque_reader",place);var b=f.base(place);
        java.util.function.IntPredicate at=t->{long time=f.in.getGameTime()%400;return time>=t&&time<t+10;};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.in.getGameTime()%400>=100&&f.in.getGameTime()%400<300,"a quiet stretch between chimes"))
            .thenExecute(()->f.at(p,place,.5,0,-38.5))
            .thenWaitUntil(()->h.assertTrue(at.test(40),"the clock is speaking"))
            .thenExecute(()->{var own=f.own(p,place);h.assertTrue(!own.getBoolean("MovedAtChime")&&Math.abs(p.getZ()-(b.getZ()-38.5))<.01&&(own.getInt("Rooms")&8)!=0,"a reader who stands still through the chime keeps their place and their rooms");f.at(p,place,.5,0,-40.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("MovedAtChime"),"moving while the clock speaks is noticed"))
            .thenExecute(()->{h.assertTrue(Math.abs(p.getZ()-(b.getZ()-23.5))<.01&&Math.abs(p.getX()-(b.getX()+.5))<.01,"the reader is put back at the near door of the room before: "+p.position().subtract(Vec3.atLowerCornerOf(b)));
                h.assertTrue((f.own(p,place).getInt("Rooms")&~7)==0,"the rooms past it must be crossed again");f.at(p,place,.5,0,-27.5);})
            .thenIdle(12)
            .thenExecute(()->h.assertTrue(Math.abs(p.getZ()-(b.getZ()-27.5))<.01||f.in.getGameTime()%400>=60,"one chime puts a reader back only once"))
            .thenSucceed();
    }
    @AfterBatch(batch="literary_masque_chime") public static void masqueChimeDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_masque_own",timeoutTicks=800)
    public static void aStoppedClockStopsOnlyTheEveningOfTheReaderWhoStoppedIt(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.MASQUE;var p=f.player("masque_stopper",place);var late=f.player("masque_late",place);var b=f.base(place);
        var own=f.own(p,place);own.putInt("Rooms",127);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
        f.click(p,place,LiteraryRooms.CLOCK);
        h.assertTrue(f.own(p,place).getBoolean("ClockStopped0506")&&f.own(p,place).getBoolean("Ready"),"the reader's hand on the clock stops their evening and readies their account");
        h.assertTrue(MasqueBlock.is(f.in.getBlockState(b.offset(LiteraryRooms.CLOCK)),MasqueBlock.Kind.CLOCK_FACE)&&f.in.getBlockState(b.offset(LiteraryRooms.CLOCK)).getValue(MasqueBlock.STAGE)==0&&MasqueBlock.is(f.in.getBlockState(b.offset(LiteraryRooms.CLOCK).below()),MasqueBlock.Kind.CLOCK_CASE)&&f.in.getBlockState(b.offset(LiteraryRooms.CLOCK).below()).getValue(MasqueBlock.STAGE)==0,"the world's clock keeps going for every other reader");
        java.util.function.Function<ServerPlayer,LiteraryActor> dancer=reader->{var world=LiteraryVignettes.shared(f.data(),place);String key="Dancer0_1_"+reader.getUUID();return world.hasUUID(key)&&f.in.getEntity(world.getUUID(key)) instanceof LiteraryActor a?a:null;};
        h.startSequence().thenWaitUntil(()->{long time=f.in.getGameTime()%400;h.assertTrue(time>=100&&time<300&&dancer.apply(p)!=null&&dancer.apply(late)!=null&&dancer.apply(late).phase()==1,"a quiet stretch with both readers' own dancers in the room");})
            .thenExecute(()->{var mine=dancer.apply(p);h.assertTrue(mine.phase()==0&&mine.owner().orElseThrow().equals(p.getUUID())&&dancer.apply(late).owner().orElseThrow().equals(late.getUUID()),"the stopper's own dancers stand still while the later reader's still dance");
                h.assertTrue(!LiteraryVignettes.shared(f.data(),place).hasUUID("Dancer0_1"),"no dancer is shared between readers");
                h.assertTrue(!f.own(late,place).getBoolean("ClockStopped0506")&&!f.own(late,place).getBoolean("Ready"),"another reader's hand on the clock stops nothing for this one");})
            .thenSucceed();
    }
    @AfterBatch(batch="literary_masque_own") public static void masqueOwnDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_thin_structures",timeoutTicks=600)
    public static void theMasqueWindsByBrazierLightAndBlyKeepsItsCupboardsLineAndPond(GameTestHelper h){
        var f=new Fixture(h);var masque=LabyrinthPlace.MASQUE;var bly=LabyrinthPlace.BLY_ROUTE;f.build(masque);f.build(bly);var m=f.base(masque);var y=f.base(bly);var l=f.in;
        for(var place:List.of(masque,bly))h.assertTrue(!ScenePolish.applies(place)&&!SceneReview.applies(place)&&!SceneCraft.applies(place),"the scenery passes leave "+place.id()+" as it is built");
        h.succeedWhen(()->{
            for(int i=0;i<MasqueSuite.ROOMS;i++){int mid=-11*i-6;
                for(int x:new int[]{-10,10}){var lancet=l.getBlockState(m.offset(x,2,mid));var fire=m.offset(x<0?-11:11,1,mid);
                    h.assertTrue(MasqueBlock.is(lancet,MasqueBlock.Kind.LANCET)&&lancet.getValue(MasqueBlock.STAGE)==i,"chamber "+i+" has a window of its own colour in each side wall");
                    h.assertTrue(MasqueBlock.is(l.getBlockState(fire),MasqueBlock.Kind.BRAZIER)&&l.getBlockState(fire).getLightEmission()==15,"a brazier burns in the closed corridor opposite chamber "+i+"'s window");}
                for(int x=-9;x<=9;x++)for(int yy=0;yy<=7;yy++)h.assertTrue(l.getBlockState(m.offset(x,yy,mid)).getLightEmission()==0,"there is no lamp or candle inside chamber "+i);
                if(i<MasqueSuite.ROOMS-1){int d=MasqueSuite.door(i),far=-11*i-11;
                    h.assertTrue(l.getBlockState(m.offset(d,0,far)).isAir()&&l.getBlockState(m.offset(d,0,far-1)).isAir()&&!l.getBlockState(m.offset(-d,0,far)).isAir(),"chamber "+i+" opens on to the next only off to one side");}}
            h.assertTrue(MasqueBlock.is(l.getBlockState(m.offset(0,-1,-72)),MasqueBlock.Kind.VELVET)&&l.getBlockState(m.offset(0,-1,-72)).getValue(MasqueBlock.STAGE)==MasqueBlock.BLACK,"the seventh chamber is black velvet down to the carpet");
            h.assertTrue(MasqueBlock.is(l.getBlockState(m.offset(MasqueSuite.FACE)),MasqueBlock.Kind.CLOCK_FACE)&&MasqueSuite.clock(MasqueSuite.FACE.below(2))&&!MasqueSuite.clock(MasqueSuite.FACE.above()),"the clock stands in the black room, and a hand on any part of it below the face counts");
            for(int zz:BlyManor.CUPBOARDS)for(int s:new int[]{-1,1}){var door=new BlockPos(4*s,0,zz);var state=l.getBlockState(y.offset(door));
                h.assertTrue(state.getBlock() instanceof DoorBlock&&!state.getValue(DoorBlock.OPEN),"each cupboard has a shut door in the corridor wall");
                h.assertTrue(door.equals(BlyManor.cupboardDoor(new BlockPos(6*s,0,zz)))&&BlyManor.cupboardDoor(new BlockPos(6*s,0,-14))==null&&BlyManor.cupboardDoor(new BlockPos(2*s,0,zz))==null,"only the inside of a press counts as a cupboard");
                h.assertTrue(l.getBlockState(y.offset(6*s,0,zz)).isAir()&&ManorBlock.is(l.getBlockState(y.offset(7*s,0,zz-1)),ManorBlock.Kind.LINEN),"the cupboard has room to stand among its linen");}
            for(int z=-7;z>=-34;z--)h.assertTrue(ManorBlock.is(l.getBlockState(y.offset(0,-1,z)),ManorBlock.Kind.WET_BOARDS),"the corridor's boards are wet down her line at "+z);
            for(int z=-3;z>=-48;z--)for(int yy=0;yy<=1;yy++){var at=y.offset(0,yy,z);h.assertTrue(l.getBlockState(at).getCollisionShape(l,at).isEmpty(),"nothing stands in her way at "+z+","+yy);}
            h.assertTrue(l.getFluidState(y.offset(0,-4,-48)).isSource()&&l.getFluidState(y.offset(0,-3,-44)).isSource(),"the pond is deep where she goes under and where a reader she takes comes up");
            h.assertTrue(l.getBlockState(y.offset(-12,0,-13)).is(Blocks.CAMPFIRE)&&ManorBlock.is(l.getBlockState(y.offset(LiteraryRooms.source(bly))),ManorBlock.Kind.NOTE)&&ManorBlock.is(l.getBlockState(y.offset(LiteraryRooms.ending(bly)).below()),ManorBlock.Kind.BENCH),"the drawing room has its fire and the gardener's note and the account lie on his bench");
        });
    }
    @AfterBatch(batch="literary_thin_structures") public static void thinStructuresDone(ServerLevel l){close();}
    @GameTest(template="empty")
    public static void theOldManTellsOnePartForEachWholeNameAnsweredOnAVisit(GameTestHelper h){
        var own=new CompoundTag();own.putString("Name","Elias");own.putInt("Visit",1);
        h.assertTrue(LiteraryCopies.answer(own,"Eli")==null&&own.getInt("Told0506")==0&&!own.getBoolean("MorseAnswered"),"part of a name is not his name");
        String first=LiteraryCopies.answer(own," elias "),again=LiteraryCopies.answer(own,"Elias");
        h.assertTrue(first.equals(LiteraryCopies.STORY[0])&&!again.equals(LiteraryCopies.STORY[1])&&own.getInt("Told0506")==1,"one part a visit, however often today's name is said");
        own.putInt("Visit",2);own.putString("Name","Samuel");h.assertTrue(LiteraryCopies.answer(own,"Elias")==null,"yesterday's name is not today's");
        String second=LiteraryCopies.answer(own,"Samuel");own.putInt("Visit",3);own.putString("Name","Joseph");String third=LiteraryCopies.answer(own,"JOSEPH");
        h.assertTrue(second.equals(LiteraryCopies.STORY[1])&&third.equals(LiteraryCopies.STORY[2])&&own.getInt("Told0506")==3,"each visit's whole name buys the next part");
        own.putInt("Visit",4);own.putString("Name","Daniel");String after=LiteraryCopies.answer(own,"Daniel");
        h.assertTrue(after!=null&&Arrays.stream(LiteraryCopies.STORY).noneMatch(after::equals)&&own.getInt("Told0506")==3,"once told, the story is not told again");
        h.assertTrue(LiteraryTexts.ending("reader",LabyrinthPlace.OLD_CABIN,own).has(DataComponents.WRITTEN_BOOK_CONTENT),"the account he readies can be written");
        h.succeed();
    }
    /** 0.4.181: the house of Usher is built whole: the causeway over the tarn, the fissure down its front, the hall, the stair to the vault, the studio. */
    @GameTest(template="empty",batch="literary_usher181",timeoutTicks=600)
    public static void theHouseOfUsherStandsOverItsTarnWithItsFissureVaultAndStudio(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.USHER;f.build(place);var b=f.base(place);var l=f.in;
        h.assertTrue(!ScenePolish.applies(place)&&!SceneReview.applies(place)&&!SceneCraft.applies(place)&&!VignetteInteriors.applies(place),"the scenery passes and the old refit leave the house as it is built");
        h.succeedWhen(()->{
            var cells=UsherHouse.fissure();
            h.assertTrue(cells.size()>UsherHouse.FISSURE_X.length,"the fissure steps aside as it runs down: "+cells.size());
            for(var cell:cells){var s=l.getBlockState(b.offset(cell.x(),cell.y(),UsherHouse.FRONT));
                h.assertTrue(UsherBlock.is(s,UsherBlock.Kind.FISSURE)||UsherBlock.is(s,UsherBlock.Kind.FISSURE_B),"the fissure runs down the front at "+cell.x()+","+cell.y()+": "+s);}
            h.assertTrue(cells.getLast().y()<0&&l.getBlockState(b.offset(cells.getLast().x(),cells.getLast().y(),UsherHouse.FRONT+1)).is(Blocks.WATER),"and is lost in the tarn");
            h.assertTrue(UsherBlock.is(l.getBlockState(b.offset(0,-1,-8)),UsherBlock.Kind.CAUSEWAY)&&l.getBlockState(b.offset(-8,-1,-8)).is(Blocks.WATER)&&l.getBlockState(b.offset(8,-1,-6)).is(Blocks.WATER),"a causeway crosses the tarn");
            for(var at:List.of(new BlockPos(0,0,-2),new BlockPos(0,0,-8),UsherHouse.ARCH,new BlockPos(0,0,-18),new BlockPos(11,0,-17),new BlockPos(-11,0,-17),new BlockPos(7,-3,-21),new BlockPos(0,-3,-27),new BlockPos(0,0,-28),new BlockPos(14,0,-30),new BlockPos(0,0,-38))){
                var c=b.offset(at);h.assertTrue(l.noCollision(null,new AABB(c.getX()+.2,c.getY()+.01,c.getZ()+.2,c.getX()+.8,c.getY()+1.8,c.getZ()+.8)),"a body passes at "+at);}
            h.assertTrue(l.getBlockState(b.offset(UsherHouse.VAULT_DOOR)).getBlock() instanceof DoorBlock&&l.getBlockState(b.offset(UsherHouse.VAULT_DOOR)).is(Blocks.WAXED_WEATHERED_COPPER_DOOR),"the vault's door is iron sheathed in copper");
            for(var rel:List.of(LiteraryRooms.COFFIN,LiteraryRooms.COFFIN.north()))h.assertTrue(l.getBlockState(b.offset(rel)).is(LiteraryRegistry.PROP.get()),"her coffin lies in the vault");
            h.assertTrue(UsherBlock.is(l.getBlockState(b.offset(UsherHouse.CHAIR)),UsherBlock.Kind.CHAIR)&&UsherBlock.is(l.getBlockState(b.offset(UsherHouse.EASEL)),UsherBlock.Kind.EASEL)&&UsherBlock.is(l.getBlockState(b.offset(UsherHouse.GUITAR)),UsherBlock.Kind.GUITAR),"Roderick's chair, easel and guitar are in the studio");
            int hearths=0,windows=0;for(var at:BlockPos.betweenClosed(b.offset(-18,-4,-42),b.offset(18,13,0))){var s=l.getBlockState(at);if(s.is(Blocks.CAMPFIRE))hearths++;if(UsherBlock.is(s,UsherBlock.Kind.WINDOW))windows++;}
            h.assertTrue(hearths==1&&l.getBlockState(b.offset(UsherHouse.HEARTH)).is(Blocks.CAMPFIRE),"one hearth, in the studio: "+hearths);
            h.assertTrue(windows>=30,"long pointed windows, front and studio: "+windows);
        });
    }
    @AfterBatch(batch="literary_usher181") public static void usherDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_coffin",timeoutTicks=230)
    public static void nativeCoffinChoiceRequiresAReturnAndKeepsTheSameWoman(GameTestHelper h){var f=new Fixture(h);var place=LabyrinthPlace.USHER;var p=f.player("coffin_reader",place);f.source(p,place);h.runAfterDelay(10,()->{var world=LiteraryVignettes.shared(f.data(),place);h.assertTrue(world.hasUUID("Woman"),"the living native body is present before the decision");p.setShiftKeyDown(true);f.click(p,place,LiteraryRooms.COFFIN);p.setShiftKeyDown(false);h.assertTrue(f.own(p,place).getBoolean("LidClosed")&&!f.own(p,place).getBoolean("Ready"),"closing the physical lid grants no ending by itself");f.arrive(p,place);f.at(p,place,2,-3,-29);});h.runAfterDelay(150,()->{var world=LiteraryVignettes.shared(f.data(),place);h.assertTrue(world.getInt("Cracks")==1&&UsherStory.ownStorm(f.own(p,place),world)&&!f.own(p,place).getBoolean("Ready"),"the next native visit brings a local crack and the storm night; the ending waits for the reading (0.4.182)");h.assertTrue(f.in.getEntity(world.getUUID("Woman")) instanceof LiteraryActor,"the same woman's native UUID survives the return");h.succeed();});}
    /** 0.4.182: Roderick in his chair, the storm night's reading answered from the vault, her at the doors, the fall, and the flight over the causeway. */
    @GameTest(template="empty",batch="literary_usher_fall182",timeoutTicks=1000)
    public static void theStormNightsReadingIsAnsweredFromTheVaultAndTheReaderFleesAsTheHouseFalls(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.USHER;var p=f.player("usher_reader",place);f.source(p,place);var books=new BlockPos(1,0,-26);
        h.startSequence()
            .thenWaitUntil(()->{var r=usherActor(f,"Roderick");h.assertTrue(r!=null&&r.role()==LiteraryActor.STRANGER&&r.phase()==UsherStory.RODERICK&&r.blockPosition().equals(f.base(place).offset(UsherHouse.CHAIR))&&r.broadcastToPlayer(p),"Roderick sits in his chair, and the reader sees him: "+r);})
            .thenExecute(()->{f.at(p,place,-4,0,-28);LiteraryVignettes.talk(p,usherActor(f,"Roderick"));h.assertTrue(f.own(p,place).getInt("Talk0507")==1,"he answers when spoken to");
                f.click(p,place,books);h.assertTrue(UsherStory.stage(f.data())==0,"before the lid is chosen the books are only damp romances");
                p.setShiftKeyDown(true);f.click(p,place,LiteraryRooms.COFFIN);p.setShiftKeyDown(false);h.assertTrue(f.own(p,place).getBoolean("LidClosed")&&!f.own(p,place).getBoolean("Ready"),"the lid is closed");
                f.arrive(p,place);f.at(p,place,-2,0,-27);})
            .thenWaitUntil(()->h.assertTrue(UsherStory.ownStorm(f.own(p,place),LiteraryVignettes.shared(f.data(),place))&&UsherStory.storm(p,f.own(p,place))&&usherActor(f,"Roderick")!=null,"the return is the storm night, and he is still in his chair"))
            .thenExecute(()->{f.click(p,place,books);h.assertTrue(UsherStory.stage(f.data())==1&&f.own(p,place).getInt("NightId0507")>0,"the strewn books open the romance");f.at(p,place,-2,0,-27);})
            .thenWaitUntil(()->h.assertTrue(UsherStory.answered(f.data())>=1,"the vault answers the first passage"))
            .thenWaitUntil(()->h.assertTrue(UsherStory.stage(f.data())>=UsherStory.AT_DOOR&&UsherStory.answered(f.data())==3,"three passages, three answers, then she is at the door: "+UsherStory.stage(f.data())))
            .thenExecute(()->{var her=usherActor(f,"Madeline");h.assertTrue(her!=null&&her.role()==LiteraryActor.COFFIN_WOMAN&&her.broadcastToPlayer(p)&&her.position().distanceTo(Vec3.atBottomCenterOf(f.base(place).offset(UsherHouse.STUDIO_DOOR)))<2.5,"she stands at the studio doors, for the night's reader");
                h.assertTrue(f.in.getEntity(LiteraryVignettes.shared(f.data(),place).getUUID("Woman")) instanceof LiteraryActor,"the coffin's woman is untouched");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("Fell0507"),"she goes to him and the two go down together"))
            .thenExecute(()->{var r=usherActor(f,"Roderick");h.assertTrue(r!=null&&!r.broadcastToPlayer(p)&&usherActor(f,"Madeline")==null&&!f.own(p,place).getBoolean("Ready")&&UsherStory.stage(f.data())==0,"he is gone from this reader's sight, she is gone, the night is over; inside the house the reader has not yet got away");
                var first=UsherHouse.fissure().getFirst();var s=f.in.getBlockState(f.base(place).offset(first.x(),first.y(),UsherHouse.FRONT));h.assertTrue(UsherBlock.is(s,UsherBlock.Kind.FISSURE)||UsherBlock.is(s,UsherBlock.Kind.FISSURE_B),"the opened fissure is only in the reader's view; the house's own blocks are unchanged");
                f.at(p,place,0,0,-2);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("Ready")&&"fled_as_the_house_fell".equals(f.own(p,place).getString("Outcome")),"over the causeway to the bank, the reader has fled as the house fell"))
            .thenExecute(()->{var text=usherPages(p,f,place);h.assertTrue(text.contains("causeway")&&text.contains("I read the old romance aloud")&&!text.contains("ran with me"),"the account tells the reading and the flight, alone: "+text);f.read(p,place,LiteraryRooms.ending(place));f.arrive(p,place);})
            .thenExecuteAfter(15,()->h.assertTrue(!usherActor(f,"Roderick").broadcastToPlayer(p),"after the fall he is not in his chair again for this reader"))
            .thenSucceed();
    }
    static @org.jetbrains.annotations.Nullable LiteraryActor usherActor(Fixture f,String key){var world=LiteraryVignettes.shared(f.data(),LabyrinthPlace.USHER);return world.hasUUID(key)&&f.in.getEntity(world.getUUID(key)) instanceof LiteraryActor a&&!a.isRemoved()?a:null;}
    static String usherPages(ServerPlayer p,Fixture f,LabyrinthPlace place){return LiteraryTexts.ending(p.getGameProfile().getName(),place,f.own(p,place)).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(x->x.raw().getString()).reduce("",(a,b)->a+" "+b).replaceAll("\\s+"," ");}
    /** 0.4.183: readers in the house together share one night: one Roderick, one reading, one Madeline, the fall for all; each flees on their own, and a later reader still finds him. */
    @GameTest(template="empty",batch="literary_usher_together183",timeoutTicks=1100)
    public static void readersInTheHouseTogetherShareOneStormNightAndEachFleesOnTheirOwn(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.USHER;var a=f.player("usher_chooser",place);var b=f.player("usher_friend",place);var books=new BlockPos(1,0,-26);
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(usherActor(f,"Roderick")!=null,"he is in his chair"))
            .thenExecute(()->{var r=usherActor(f,"Roderick");h.assertTrue(r.broadcastToPlayer(a)&&r.broadcastToPlayer(b)&&r.owner().isEmpty(),"one Roderick, seen by both");
                f.at(b,place,0,-3,-27);a.setShiftKeyDown(true);f.click(a,place,LiteraryRooms.COFFIN);a.setShiftKeyDown(false);})
            .thenWaitUntil(()->h.assertTrue(f.own(b,place).contains("LidKnown0507")&&f.own(a,place).contains("LidKnown0507"),"both know the lid was chosen on this visit"))
            .thenExecute(()->{f.arrive(a,place);f.arrive(b,place);f.at(a,place,-2,0,-27);f.at(b,place,2,0,-27);})
            .thenWaitUntil(()->h.assertTrue(UsherStory.storm(a,f.own(a,place))&&UsherStory.storm(b,f.own(b,place)),"the friend who watched has the storm night too"))
            .thenExecute(()->{f.click(b,place,books);f.at(b,place,2,0,-27);int id=f.own(a,place).getInt("NightId0507");h.assertTrue(UsherStory.stage(f.data())==1&&id>0&&id==f.own(b,place).getInt("NightId0507"),"one reading, and both are in it");})
            .thenWaitUntil(()->h.assertTrue(UsherStory.stage(f.data())>=UsherStory.AT_DOOR,"the reading runs to the doors for both"))
            .thenExecute(()->{var her=usherActor(f,"Madeline");h.assertTrue(her!=null&&her.broadcastToPlayer(a)&&her.broadcastToPlayer(b),"one Madeline, at the doors for both");})
            .thenWaitUntil(()->h.assertTrue(f.own(a,place).getBoolean("Fell0507")&&f.own(b,place).getBoolean("Fell0507"),"the fall is for everyone who was in it"))
            .thenExecute(()->{h.assertTrue(f.own(a,place).getString("With0507").equals("usher_friend")&&f.own(b,place).getString("With0507").equals("usher_chooser")&&f.own(b,place).getBoolean("ReadAloud0507")&&!f.own(a,place).getBoolean("ReadAloud0507"),"each remembers who was with them and who read");
                f.at(a,place,0,0,-2);})
            .thenWaitUntil(()->h.assertTrue(f.own(a,place).getBoolean("Ready")&&!f.own(b,place).getBoolean("Ready"),"each flees on their own feet: the one on the bank is out, the one inside is not"))
            .thenExecute(()->{var mine=usherPages(a,f,place);h.assertTrue(mine.contains("I closed the lid")&&mine.contains("usher_friend read the old romance aloud")&&mine.contains("usher_friend ran with me"),"the chooser's account: "+mine);
                f.at(b,place,1,0,-2);})
            .thenWaitUntil(()->h.assertTrue(f.own(b,place).getBoolean("Ready"),"the reader follows over the causeway"))
            .thenExecute(()->{var theirs=usherPages(b,f,place);h.assertTrue(theirs.contains("The lid had been closed")&&theirs.contains("I read the old romance aloud")&&theirs.contains("usher_chooser ran with me"),"the reader's account: "+theirs);
                var c=f.player("usher_later",place);h.assertTrue(usherActor(f,"Roderick").broadcastToPlayer(c)&&!usherActor(f,"Roderick").broadcastToPlayer(a),"a later reader still finds him in his chair; those who saw the fall do not");})
            .thenSucceed();
    }
    @AfterBatch(batch="literary_usher_together183") public static void usherTogetherDone(ServerLevel l){close();}
    /** 0.4.183: Roderick and the account know the house the reader came from, by the House's read; nothing changes without one. */
    @GameTest(template="empty",batch="literary_usher_home183",timeoutTicks=200)
    public static void roderickAndTheAccountKnowTheHouseTheReaderCameFrom(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.USHER;
        h.assertTrue(UsherStory.homeLine("").isEmpty(),"without a read he says nothing of the reader's house");
        var seen=new java.util.HashSet<String>();
        for(String home:List.of("HAVEN","DANGER","RUNAWAY")){String line=UsherStory.homeLine(home);h.assertTrue(!line.isEmpty()&&seen.add(line),"his line for "+home);
            var own=new CompoundTag();own.putBoolean("Fell0507",true);own.putBoolean("ReadAloud0507",true);own.putString("Home0507",home);var plain=new CompoundTag();plain.putBoolean("Fell0507",true);plain.putBoolean("ReadAloud0507",true);
            String told=UsherStory.account(own),bare=UsherStory.account(plain);h.assertTrue(told.startsWith(bare)&&told.length()>bare.length(),"the account closes on the reader's own house for "+home);}
        var p=f.player("usher_home",place);var d=f.data();var read=d.stateEntry(HouseRead.STATE,p.getUUID().toString());var home=new CompoundTag();home.putString("Value","RUNAWAY");home.putString("Strength","TENTATIVE");read.put("HOME",home);d.setStateEntry(HouseRead.STATE,p.getUUID().toString(),read);
        h.assertTrue(HouseRead.category(p,HouseRead.Trait.HOME).isPresent()&&UsherStory.greeting(p).startsWith("You came. "),"a tentative home read, and no settled self-read: the plain greeting");
        h.succeed();
    }
    @AfterBatch(batch="literary_usher_home183") public static void usherHomeDone(ServerLevel l){close();}
    /** 0.4.183: on an open lid, once she has been seen standing in the wing, she comes to the studio doors while nobody looks, and stays there. */
    @GameTest(template="empty",batch="literary_usher_open183",timeoutTicks=900)
    public static void onAnOpenLidSheIsSeenInTheWingAndThenStandsAtTheStudioDoors(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.USHER;var p=f.player("usher_open",place);var b=f.base(place);
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(LiteraryVignettes.shared(f.data(),place).hasUUID("Woman"),"she lies in the coffin"))
            .thenExecute(()->{f.click(p,place,LiteraryRooms.COFFIN);h.assertTrue(LiteraryVignettes.shared(f.data(),place).contains("LidClosed")&&!LiteraryVignettes.shared(f.data(),place).getBoolean("LidClosed"),"the lid is left open");
                f.arrive(p,place);f.at(p,place,0,0,-16);f.look(p,b.offset(-8,1,-16));})
            .thenWaitUntil(()->h.assertTrue(LiteraryVignettes.shared(f.data(),place).getBoolean("WomanLeft"),"unwatched, she leaves the coffin for the wing"))
            .thenExecute(()->{f.at(p,place,14,0,-26);f.look(p,b.offset(12,1,-34));})
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("Ready")&&"found_her_outside_the_open_coffin".equals(f.own(p,place).getString("Outcome")),"seen standing in the wing, the open lid's ending is ready"))
            .thenExecute(()->{f.at(p,place,0,0,-2);f.look(p,b.offset(0,1,4));})
            .thenWaitUntil(()->{var world=LiteraryVignettes.shared(f.data(),place);var woman=f.in.getEntity(world.getUUID("Woman"));h.assertTrue(world.getBoolean("AtDoor0507")&&woman!=null&&woman.position().distanceTo(Vec3.atBottomCenterOf(b.offset(UsherHouse.STUDIO_DOOR)))<2,"while nobody looks she comes to the studio doors");})
            .thenExecuteAfter(40,()->{var world=LiteraryVignettes.shared(f.data(),place);h.assertTrue(f.in.getEntity(world.getUUID("Woman")).position().distanceTo(Vec3.atBottomCenterOf(b.offset(UsherHouse.STUDIO_DOOR)))<2,"and she stays there, never back to the wing");})
            .thenSucceed();
    }
    @AfterBatch(batch="literary_usher_open183") public static void usherOpenDone(ServerLevel l){close();}
    /**
     * 0.4.184: the binary; 0.4.185: the trade. Still inside when the house falls: no credit, set back on the bank, the tarn
     * keeps what was in the reader's hands and gives them a leaf of the register with their name; nothing follows them home.
     * A later night got out in time gives back what the tarn kept; reading the account gives the stone and takes the house.
     */
    @GameTest(template="empty",batch="literary_usher_buried184",timeoutTicks=2400)
    public static void stayingInsideAsTheHouseFallsFailsAndTheTarnKeepsWhatWasInHandUntilALaterNightIsSurvived(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.USHER;var p=f.player("usher_buried",place);f.source(p,place);var books=new BlockPos(1,0,-26);UsherStory.FLIGHT=40;
        h.assertTrue(UsherStory.tarnTakes(new ItemStack(Items.LANTERN))&&!UsherStory.tarnTakes(new ItemStack(LiteraryRegistry.USHER_STONE.get()))&&!UsherStory.tarnTakes(ItemStack.EMPTY),"the tarn keeps only an ordinary thing, never anything of the House's");
        var marked=new ItemStack(Items.COMPASS);net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,marked,t->t.putBoolean("Original",true));h.assertTrue(!UsherStory.tarnTakes(marked),"never an original or anything bound to a reader");
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(usherActor(f,"Roderick")!=null,"he is in his chair"))
            .thenExecute(()->{p.setShiftKeyDown(true);f.click(p,place,LiteraryRooms.COFFIN);p.setShiftKeyDown(false);f.arrive(p,place);f.at(p,place,-2,0,-27);})
            .thenWaitUntil(()->h.assertTrue(UsherStory.storm(p,f.own(p,place)),"the storm night"))
            .thenExecute(()->{f.click(p,place,books);f.at(p,place,-2,0,-27);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.LANTERN,3));})
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("Fell0507"),"the house falls"))
            .thenExecute(()->f.at(p,place,-2,0,-27))
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("BuriedOnce0507"),"still inside, the house closes over the reader"))
            .thenExecute(()->{var own=f.own(p,place);h.assertTrue(!own.getBoolean("Ready")&&!own.getBoolean("Fell0507")&&p.getZ()-f.base(place).getZ()>-3,"no credit; the tarn gives the reader back on the bank: z "+(p.getZ()-f.base(place).getZ()));
                h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.USHER)&&!usherActor(f,"Roderick").broadcastToPlayer(p),"nothing is resolved, and he is gone for the rest of this visit");
                h.assertTrue(own.contains(UsherStory.KEPT)&&p.getInventory().countItem(Items.LANTERN)==0&&!p.getMainHandItem().is(Items.LANTERN),"the tarn keeps what was in the reader's hands (the leaf comes into the empty hand): hand "+p.getMainHandItem()+", kept "+own.contains(UsherStory.KEPT)+", lanterns "+p.getInventory().countItem(Items.LANTERN)+", slot "+p.getInventory().selected);
                var leaf=p.getInventory().items.stream().filter(s->s.is(Items.WRITTEN_BOOK)&&s.getHoverName().getString().contains("vault register")).findFirst();
                h.assertTrue(leaf.isPresent()&&leaf.get().get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().getFirst().raw().getString().contains(p.getGameProfile().getName()),"and gives them a leaf of the vault register with their own name in it");
                f.click(p,place,books);h.assertTrue(UsherStory.stage(f.data())==0,"not tonight: the books will not begin again on this visit");})
            .thenExecute(()->{f.arrive(p,place);f.at(p,place,-2,0,-27);})
            .thenWaitUntil(()->h.assertTrue(usherActor(f,"Roderick")!=null&&usherActor(f,"Roderick").broadcastToPlayer(p)&&UsherStory.storm(p,f.own(p,place)),"a later visit: he is back in his chair, and it is the storm night again"))
            .thenExecute(()->{f.click(p,place,books);h.assertTrue(UsherStory.stage(f.data())==1,"the reading begins again");f.at(p,place,-2,0,-27);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("Fell0507"),"the house falls again"))
            .thenExecute(()->f.at(p,place,0,0,-2))
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("Ready"),"over the causeway in time: the night is survived"))
            .thenExecute(()->{h.assertTrue(p.getInventory().countItem(Items.LANTERN)==3&&!f.own(p,place).contains(UsherStory.KEPT),"what the tarn kept lies on the bank: the reader has it back");
                h.assertTrue(usherPages(p,f,place).contains("The first time the house fell, I was still inside it. The tarn gave me back, and kept what I had in my hands"),"the account remembers the first time");
                h.assertTrue(LiteraryVignettes.canDeal(f.data(),p.getUUID(),place),"until the account is read, the house is still there for them");
                f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(p.getInventory().countItem(LiteraryRegistry.USHER_STONE.get())==1,"the house's stone is the reader's own");
                h.assertTrue(UsherStoneItem.nearestHidden(p)!=null,"held to the ear on the bank, something hidden in the house answers");
                h.assertTrue(LiteraryVignettes.gone(f.data(),p.getUUID(),place)&&!LiteraryVignettes.canDeal(f.data(),p.getUUID(),place),"winning takes the house: its door never comes to this reader again");})
            .thenSucceed();
    }
    @AfterBatch(batch="literary_usher_buried184") public static void usherBuriedDone(ServerLevel l){UsherStory.FLIGHT=400;close();}
    @AfterBatch(batch="literary_usher_fall182") public static void usherFallDone(ServerLevel l){close();}
    @AfterBatch(batch="literary_coffin") public static void coffinDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_cylinders",timeoutTicks=750)
    public static void threeOwnedRecordsMustActuallyPlayThroughTheirNativeSongLengths(GameTestHelper h){var f=new Fixture(h);var place=LabyrinthPlace.CRIMSON_HALL;var p=f.player("cylinder_reader",place);f.source(p,place);int cylinder=0;for(var rel:List.of(new BlockPos(-12,0,-35),new BlockPos(12,0,-35),new BlockPos(0,7,-39))){f.click(p,place,rel);Item expected=new Item[]{LiteraryRegistry.CYLINDER_ONE.get(),LiteraryRegistry.CYLINDER_TWO.get(),LiteraryRegistry.CYLINDER_THREE.get()}[cylinder];h.assertTrue(p.getInventory().countItem(expected)==1,"actual cylinder surface "+cylinder+" supplies its finite record");cylinder++;}p.closeContainer();var at=f.base(place).offset(-3,0,-27);f.in.setBlock(at,Blocks.SNOW.defaultBlockState(),3);p.getInventory().selected=8;p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SHOVEL));var event=new BlockEvent.BreakEvent(f.in,at,f.in.getBlockState(at),p);NeoForge.EVENT_BUS.post(event);h.assertTrue(!event.isCanceled(),"the native shovel exception admits snow clearing");var shovel=p.getMainHandItem().copy();p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);var bare=new BlockEvent.BreakEvent(f.in,at,f.in.getBlockState(at),p);NeoForge.EVENT_BUS.post(bare);h.assertTrue(!bare.isCanceled(),"bare hands clear the snow too, since the House hands out no shovel");p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));var stick=new BlockEvent.BreakEvent(f.in,at,f.in.getBlockState(at),p);NeoForge.EVENT_BUS.post(stick);h.assertTrue(stick.isCanceled(),"anything else held does not break the hall");p.setItemInHand(InteractionHand.MAIN_HAND,shovel);f.in.setBlock(at,Blocks.AIR.defaultBlockState(),3);f.at(p,place,2,0,-30);var box=(JukeboxBlockEntity)f.in.getBlockEntity(f.base(place).offset(0,0,-30));Item[] records={LiteraryRegistry.CYLINDER_ONE.get(),LiteraryRegistry.CYLINDER_TWO.get(),LiteraryRegistry.CYLINDER_THREE.get()};
        for(int n=0;n<3;n++){final int index=n;h.runAfterDelay(10+n*210,()->{var disc=p.getInventory().items.stream().filter(s->s.is(records[index])).findFirst().orElseThrow().copy();box.setItem(0,disc);h.assertTrue(box.getSongPlayer().isPlaying(),"the owned cylinder starts native audio");});}
        h.runAfterDelay(25,()->h.assertTrue(f.own(p,place).getInt("HeardCylinders")==0,"a few present ticks cannot complete the first voice"));h.runAfterDelay(635,()->{h.assertTrue(f.own(p,place).getInt("HeardCylinders")==7&&f.own(p,place).getBoolean("Ready"),"all three actual native song lengths and the shovel action resolve the hall");f.read(p,place,LiteraryRooms.ending(place));h.succeed();});}
    @AfterBatch(batch="literary_cylinders") public static void cylindersDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_mapping",timeoutTicks=900)
    public static void theRealLowPassageChoiceAndReturnObserveTheBrothersConsequence(GameTestHelper h){var f=new Fixture(h);var place=LabyrinthPlace.MAPPING_INTERIOR;var p=f.player("mapping_reader",place);f.source(p,place);f.at(p,place,-6,0,-26);f.look(p,f.base(place).offset(-9,1,-26));
        // Each step waits for the one before it, so a slow full run cannot spend the clock before the brother arrives.
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("SilhouetteSeen"),"the native silhouette must actually be seen")).thenExecute(()->f.at(p,place,0,-5,-42))
          .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("BeyondRoof")&&p.getForcedPose()==Pose.SWIMMING,"the player actually crawls beyond the manufactured roof"))
          .thenExecute(()->{f.click(p,place,LiteraryRooms.PILE);h.assertTrue(f.own(p,place).getBoolean("PileChosen")&&p.getInventory().countItem(LiteraryRegistry.PHOTOGRAPH.get())==1,"taking the actual pile creates one owned photograph");f.arrive(p,place);f.at(p,place,3,0,-31);f.look(p,f.base(place).offset(6,1,-32));})
          .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("Ready")&&!f.own(p,place).getBoolean("PileBroken"),"returning to see the fading brother resolves this player's kept-things choice"))
          .thenExecute(()->f.read(p,place,LiteraryRooms.ending(place))).thenSucceed();}
    @AfterBatch(batch="literary_mapping") public static void mappingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_fan",timeoutTicks=250)
    public static void ActualPlacedReachRepairAndFiveSecondsCompleteTheFan(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_FAN;var p=f.player("fan_reader",place);f.source(p,place);f.click(p,place,SceneHuntReview.TOOLS);
        var at=f.base(place).offset(1,0,-16);f.in.setBlock(at,Blocks.COBBLESTONE.defaultBlockState(),3);var event=new BlockEvent.EntityPlaceEvent(net.neoforged.neoforge.common.util.BlockSnapshot.create(f.in.dimension(),f.in,at),Blocks.AIR.defaultBlockState(),p);NeoForge.EVENT_BUS.post(event);
        h.assertTrue(!event.isCanceled()&&f.own(p,place).getBoolean("BuiltReach"),"a genuine final placed block records the reach");
        for(int slot=0;slot<9;slot++)if(p.getInventory().getItem(slot).is(LiteraryRegistry.LIGHTBULB.get()))p.getInventory().selected=slot;
        f.data().pushReturn(p.getUUID(),new LabyrinthData.Waypoint(f.in.dimension(),Vec3.atBottomCenterOf(f.base(place).offset(0,0,40)),0,true));
        f.click(p,place,LiteraryRooms.FAN);f.at(p,place,.5,5.25,-16.5);f.look(p,f.base(place).offset(0,0,-17));
        h.runAfterDelay(75,()->h.assertTrue(!f.own(p,place).getBoolean("BladeStrike"),"the fan cannot knock the reader off before five seconds"));
        h.runAfterDelay(115,()->{var own=f.own(p,place);h.assertTrue(own.getBoolean("Repaired")&&own.getBoolean("BladeStrike")&&own.getBoolean("ElkThroughFan")&&own.getBoolean("Completed"),"five seconds of real standing completes the personally seen sequence");
            h.assertTrue(!LiteraryVignettes.inside(p,place)&&WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id()))&&p.getInventory().countItem(LiteraryRegistry.TOOTH.get())==1,"the player returns outside with one earned source and one original tooth");
            f.arrive(p,place);f.click(p,place,SceneHuntReview.TOOLS);h.assertTrue(p.getInventory().countItem(LiteraryRegistry.LIGHTBULB.get())==0&&p.getInventory().countItem(LiteraryRegistry.TOOTH.get())==1,"returning never restocks the bulb or the tooth");
            // 0.4.105: every account is titled with its room's prose name, within a written book title's 32 characters.
            for(var each:LabyrinthPlace.values()){try{LiteraryRooms.ending(each);}catch(IllegalArgumentException notLiterary){continue;}
                var title=LiteraryTexts.accountTitle(each);h.assertTrue(title.length()<=32&&!title.contains("_")&&!title.endsWith(" the room"),"an account is titled with its room's prose name: "+title);}
            h.assertTrue(LiteraryTexts.accountTitle(place).equals("The account: the fan room"),"the fan's account has a shortened prose title");h.succeed();});
    }
    @AfterBatch(batch="literary_fan") public static void fanDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_food",timeoutTicks=200)
    public static void frozenFoodStaysCarriedAndTheSameNightMealCannotBeCountedTwice(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.HOLY_RABBIT;var p=f.player("hollow_reader",place);f.at(p,place,1,0,-22);
        var bread=new ItemStack(Items.BREAD,4);p.setItemInHand(InteractionHand.MAIN_HAND,bread);
        var arrival=new net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Start(p,bread,32);NeoForge.EVENT_BUS.post(arrival);
        h.assertTrue(!arrival.isCanceled()&&!LiteraryVignettes.retreatLocked(p),"an ordinary arrival allows food and retreat before any explicit commitment");
        // The reader's own tick places Father, and he can be found once his entity section loads: on a slow host that is
        // later than ten ticks, so the case waits for him rather than a fixed delay.
        final boolean[] met={false};
        h.onEachTick(()->{if(met[0])return;
            var found=f.out.getEntitiesOfClass(LiteraryActor.class,p.getBoundingBox().inflate(6),a->a.role()==LiteraryActor.FATHER&&a.owner().orElse(UUID.randomUUID()).equals(p.getUUID())).stream().findFirst();
            if(found.isEmpty())return;met[0]=true;var father=found.get();
            LiteraryVignettes.talk(p,father);h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu,"Father offers a real explicit native choice");
            p.containerMenu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,p);
            var eating=new net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Start(p,bread,32);NeoForge.EVENT_BUS.post(eating);
            h.assertTrue(LiteraryVignettes.retreatLocked(p)&&eating.isCanceled()&&bread.getCount()==4,"only the chosen stay freezes carried food and commits the visit");
            for(var item:List.of(Items.POTION,Items.MILK_BUCKET)){
                var use=new net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Start(p,new ItemStack(item),32);NeoForge.EVENT_BUS.post(use);
                h.assertTrue(!use.isCanceled(),"the cold never blocks native medicine or milk");
            }
            h.runAfterDelay(5,()->{var own=f.own(p,place);h.assertTrue(own.getInt("Night")==1,"the actual chosen stay starts its first native night before food arrives");var token=UUID.randomUUID();own.putUUID("MealToken",token);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
            var meal=LiteraryVignettes.mark(p,place,"Meal1",new ItemStack(LiteraryRegistry.MEAL.get()));net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA,meal,t->{t.putInt("Night",own.getInt("Night"));t.putUUID("MealToken",token);});
            var replay=meal.copy();meal.getItem().finishUsingItem(meal,f.out,p);LiteraryVignettes.ate(p,replay);
            h.assertTrue(f.own(p,place).getInt("Meals")==1&&p.getFoodData().getFoodLevel()==1,"actual native eating consumes this night's permission once while the hollow stays hungry");h.succeed();});
        });
    }
    @AfterBatch(batch="literary_food") public static void foodDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_frozen",timeoutTicks=100)
    public static void frozenNativeMachineryAndFarmKeepTheirMeshShapeAndNoPower(GameTestHelper h){var f=new Fixture(h);var place=LabyrinthPlace.MAPPING_INTERIOR;f.build(place);var at=f.base(place).offset(18,0,-10);var original=Blocks.PISTON.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,Direction.UP);f.out.setBlock(at,LiteraryRegistry.FROZEN.get().defaultBlockState(),3);var be=(LiteraryModelBlockEntity)f.out.getBlockEntity(at);var t=new CompoundTag();t.put("FrozenState",NbtUtils.writeBlockState(original));be.display(t);h.assertTrue(LiteraryFrozenBlock.original(f.out,at).equals(original)&&!f.out.getBlockState(at).isRandomlyTicking()&&!f.out.getBlockState(at).isSignalSource(),"the native captured piston retains its exact visual state without native activity");h.assertTrue(!f.out.getBlockState(at).getCollisionShape(f.out,at).isEmpty(),"frozen mechanisms retain physical collision");h.assertTrue(LiteraryFrozenBlock.freezes(Blocks.WHEAT.defaultBlockState())&&LiteraryFrozenBlock.freezes(Blocks.FARMLAND.defaultBlockState()),"both crops and their supporting farmland use the inactive native projection");var saved=be.saveWithFullMetadata(f.out.registryAccess());be.loadWithComponents(saved,f.out.registryAccess());h.assertTrue(LiteraryFrozenBlock.original(f.out,at).equals(original),"the original mesh state survives native block-entity serialization");h.succeed();}
    @AfterBatch(batch="literary_frozen") public static void frozenDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_registry",timeoutTicks=100)
    public static void eligibleSourcesAndOneOptionalClosureLeaveTheWitnessReachable(GameTestHelper h){var data=new LabyrinthData();var player=UUID.randomUUID();h.assertTrue(WitnessAccount.Story.values().length==56&&WitnessAccount.REQUIRED==42,"the quota derives from all eligible authored stories");h.assertTrue(WitnessAccount.Story.of("old_cabin")==null,"the recurring witness cabin is guidance, not another source");int sources=0;for(var place:LabyrinthPlace.values())if(LiteraryRooms.isLiterary(place)&&place!=LabyrinthPlace.OLD_CABIN){h.assertTrue(WitnessAccount.Story.of(place.id())!=null&&LiteraryTexts.source(place).has(DataComponents.WRITTEN_BOOK_CONTENT),"every new eligible site has original discovery writing and its own resolution registration");sources++;}var unfinished=UUID.randomUUID();data.visit(unfinished,LabyrinthPlace.HILL_NURSERY);
        h.assertTrue(!LiteraryCabinChoices.canRetire(data,LabyrinthPlace.HILL_NURSERY)&&!LiteraryCabinChoices.canRetire(data,LabyrinthPlace.HOLLOWAY_CAMP),"offline unfinished readers and the finale shield route cannot be retired");
        WitnessAccount.resolve(data,unfinished,WitnessAccount.Story.HILL_NURSERY,"personally_resolved");h.assertTrue(LiteraryCabinChoices.canRetire(data,LabyrinthPlace.HILL_NURSERY),"a completed optional room can still close under the single shared outcome");
        h.assertTrue(sources==34&&WitnessAccount.Story.values().length-1>=WitnessAccount.REQUIRED,"one optional closure leaves fifty-five sources for forty-two required resolutions");for(var story:WitnessAccount.Story.values())WitnessAccount.resolve(data,player,story,"resolved");h.assertTrue(WitnessAccount.count(data,player)==56&&WitnessAccount.ready(data,player),"different choices in one story cannot add a second source");h.succeed();}
    @GameTest(template="empty",batch="literary_copy",timeoutTicks=500)
    public static void anActualSleptHomeCreatesPrivateFrozenCopiesWithEmptyOriginalContainers(GameTestHelper h){var f=new Fixture(h,64);var p=f.player("copy_reader",LabyrinthPlace.HILL_NURSERY);var peer=f.player("copy_peer",LabyrinthPlace.HILL_NURSERY);var world=p.server.overworld();var home=new BlockPos(63500,80,63500);var bed=home.offset(2,0,-3);f.chunks.hold(world,new AABB(home).inflate(34));LiteraryRooms.box(world,home,-32,-1,-32,31,-1,31,Blocks.STONE_BRICKS);NovelRooms.room(world,home,-10,10,-10,10,0,6,Blocks.OAK_PLANKS,Blocks.BIRCH_PLANKS);NovelRooms.door(world,home.offset(0,0,10),Direction.SOUTH,Blocks.OAK_DOOR,false);NovelRooms.bed(world,bed,Blocks.WHITE_BED,Direction.NORTH);world.setBlock(home.offset(-5,0,-3),Blocks.CRAFTING_TABLE.defaultBlockState(),3);world.setBlock(home.offset(-4,0,-4),Blocks.CHEST.defaultBlockState(),3);((ChestBlockEntity)world.getBlockEntity(home.offset(-4,0,-4))).setItem(0,new ItemStack(Items.DIAMOND,7));world.setBlock(home.offset(5,0,-5),Blocks.PISTON.defaultBlockState(),3);world.setBlock(home.offset(5,30,-5),Blocks.LAPIS_BLOCK.defaultBlockState(),3);world.setBlock(home.offset(7,0,-5),Blocks.HOPPER.defaultBlockState(),3);world.setBlock(home.offset(6,-1,-5),Blocks.FARMLAND.defaultBlockState(),3);world.setBlock(home.offset(6,0,-5),Blocks.WHEAT.defaultBlockState(),3);
        var stand=EntityType.ARMOR_STAND.create(world);h.assertTrue(stand!=null,"the actual home armor stand exists");
        var helmet=new ItemStack(Items.DIAMOND_HELMET);helmet.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("The real helmet"));helmet.setDamageValue(29);
        stand.setItemSlot(EquipmentSlot.HEAD,helmet);stand.moveTo(home.getX()-2.5,home.getY(),home.getZ()-4.5);world.addFreshEntity(stand);f.extras.add(stand);

        h.runAfterDelay(10,()->{p.teleportTo(world,home.getX()+.5,home.getY(),home.getZ()+11.5,180,0);p.hasChangedDimension();var door=home.offset(0,0,10);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,door,new BlockHitResult(door.getCenter(),Direction.SOUTH,door,false)));world.setDayTime(18000);p.moveTo(bed.getX()+.5,bed.getY(),bed.getZ()+1.5);p.startSleeping(bed);});
        final boolean[] awake={false},checked={false};
        h.runAfterDelay(100,()->{p.stopSleepInBed(true,true);awake[0]=true;});
        // Capture waits for native entity sections; assertion timing must follow completion.
        h.onEachTick(()->{if(!awake[0]||checked[0])return;var readyFamily=LiteraryCopies.base(p.server,p.getUUID(),LabyrinthPlace.FAMILY_COPY);var readyCabin=LiteraryCopies.base(p.server,p.getUUID(),LabyrinthPlace.OLD_CABIN);if(readyFamily==null||readyCabin==null)return;f.chunks.hold(f.out,new AABB(readyFamily.getX()-34,readyFamily.getY()-18,readyFamily.getZ()-70,readyFamily.getX()+35,readyFamily.getY()+34,readyFamily.getZ()+5));f.chunks.hold(f.out,new AABB(readyCabin).inflate(12));if(!f.chunks.ready())return;checked[0]=true;h.assertTrue(readyFamily!=null&&readyCabin!=null,"real native sleep captures and constructs both independent bounded snapshots");h.assertTrue(LiteraryCopies.heldCaptureChunks()==0,"both completed captures release every native source ticket");h.assertTrue(readyFamily.getY()==80&&f.out.getBlockState(readyFamily.offset(3,30,-36)).is(Blocks.LAPIS_BLOCK),"the full captured height survives even when the logical room slot reaches the outside world ceiling");h.assertTrue(LiteraryCopies.base(p.server,peer.getUUID(),LabyrinthPlace.FAMILY_COPY)==null,"an unslept peer receives no borrowed home");h.assertTrue(LiteraryCopies.prepareEntry(p,LabyrinthPlace.FAMILY_COPY)==null,"the family waits two actual in-game weeks");var chest=(ChestBlockEntity)f.out.getBlockEntity(readyFamily.offset(-6,0,-35));h.assertTrue(chest!=null&&chest.isEmpty(),"the actual copied container is empty while the seven source diamonds remain in the native home");h.assertTrue(((ChestBlockEntity)world.getBlockEntity(home.offset(-4,0,-4))).getItem(0).getCount()==7,"capture never takes source property");h.assertTrue(f.out.getBlockState(readyFamily.offset(3,0,-36)).is(LiteraryRegistry.FROZEN.get()),"the actual captured piston uses its inactive original mesh");h.assertTrue(f.out.getBlockState(readyFamily.offset(5,0,-36)).is(LiteraryRegistry.FROZEN.get())&&LiteraryFrozenBlock.original(f.out,readyFamily.offset(5,0,-36)).is(Blocks.HOPPER),"the captured hopper retains its native state without a ticking inventory or overwritten display data");world.setDayTime(14*24000L+18000);
        h.runAfterDelay(15,()->{var family=LiteraryCopies.base(p.server,p.getUUID(),LabyrinthPlace.FAMILY_COPY);p.teleportTo(f.out,family.getX()+.5,family.getY(),family.getZ()-3,180,0);p.hasChangedDimension();IndianLakeRooms.keepLoaded(f.out,family,LabyrinthPlace.FAMILY_COPY);});
        h.runAfterDelay(40,()->{var family=LiteraryCopies.base(p.server,p.getUUID(),LabyrinthPlace.FAMILY_COPY);h.assertTrue(LiteraryCopies.prepareEntry(p,LabyrinthPlace.FAMILY_COPY)!=null,"maturity opens this reader's exact private entry");
            var duplicate=f.out.getEntitiesOfClass(net.minecraft.world.entity.decoration.ArmorStand.class,new AABB(family).inflate(80),a->a.getTags().contains(LiteraryCopies.PROJECTION)).stream().findFirst().orElseThrow();
            p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            var swap=new PlayerInteractEvent.EntityInteractSpecific(p,InteractionHand.MAIN_HAND,duplicate,new Vec3(0,1.7,0));NeoForge.EVENT_BUS.post(swap);
            h.assertTrue(swap.isCanceled(),"the specific armor-exchange packet is blocked for a copied stand");
            duplicate.interactAt(p,new Vec3(0,1.7,0),InteractionHand.MAIN_HAND);
            h.assertTrue(duplicate.getItemBySlot(EquipmentSlot.HEAD).is(Items.DIAMOND_HELMET)&&duplicate.getItemBySlot(EquipmentSlot.HEAD).get(DataComponents.CUSTOM_DATA).copyTag().getBoolean(WeaponHistory.COPY)&&p.getInventory().countItem(Items.DIAMOND_HELMET)==0,"native disabled slots retain visible copied armor without yielding equipment");
            h.assertTrue(ItemStack.matches(stand.getItemBySlot(EquipmentSlot.HEAD),helmet),"the real home's equipment keeps its exact components");
            for(var visitor:List.of(p,peer)){
                var cabin=LiteraryCopies.base(p.server,p.getUUID(),LabyrinthPlace.OLD_CABIN);
                visitor.teleportTo(f.out,cabin.getX()+.5,cabin.getY(),cabin.getZ()-3,180,0);visitor.hasChangedDimension();OutdoorBounds.remember(visitor);var safe=visitor.position();
                visitor.moveTo(cabin.getX()+24,cabin.getY()-50,cabin.getZ()-16);visitor.fallDistance=50;
                h.assertTrue(OutdoorBounds.check(visitor,f.origin)&&visitor.position().distanceToSqr(safe)<.01&&visitor.fallDistance==0,"the actual small private island returns owner and physical peer before the void");
            }
            p.setGameMode(GameType.CREATIVE);p.moveTo(family.getX()+50,family.getY()-50,family.getZ()-16);h.assertTrue(!OutdoorBounds.check(p,f.origin),"creative inspection remains free");p.setGameMode(GameType.SURVIVAL);
p.teleportTo(f.out,family.getX()+.5,family.getY(),family.getZ()-3,180,0);p.hasChangedDimension();LiteraryVignettes.onArrive(p,LabyrinthPlace.FAMILY_COPY);var chestAt=family.offset(-6,0,-35);p.moveTo(chestAt.getX()+.5,chestAt.getY(),chestAt.getZ()+2.5);var locked=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,chestAt,new BlockHitResult(chestAt.getCenter(),Direction.SOUTH,chestAt,false));NeoForge.EVENT_BUS.post(locked);h.assertTrue(locked.isCanceled(),"the empty copied chest is natively locked instead of accepting the visitor's property");f.source(p,LabyrinthPlace.FAMILY_COPY);var original=f.own(p,LabyrinthPlace.FAMILY_COPY).getCompound("Original_Source").copy();var copies=LiteraryCopies.get(p.server);p.server.overworld().getDataStorage().set("the_oldest_house_literary_copies",LiteraryCopies.load(copies.save(new CompoundTag(),p.registryAccess()),p.registryAccess()));h.assertTrue(family.equals(LiteraryCopies.base(p.server,p.getUUID(),LabyrinthPlace.FAMILY_COPY)),"native SavedData reload retains the private address and completed build cursor");f.source(p,LabyrinthPlace.FAMILY_COPY);h.assertTrue(original.equals(f.own(p,LabyrinthPlace.FAMILY_COPY).getCompound("Original_Source")),"copy readers keep the same finite paper after reload");peer.teleportTo(f.out,chestAt.getX()+.5,chestAt.getY(),chestAt.getZ()+2.5,180,0);peer.hasChangedDimension();var peerLatch=new PlayerInteractEvent.RightClickBlock(peer,InteractionHand.MAIN_HAND,chestAt,new BlockHitResult(chestAt.getCenter(),Direction.SOUTH,chestAt,false));NeoForge.EVENT_BUS.post(peerLatch);h.assertTrue(peerLatch.isCanceled(),"a physical peer cannot fill or unlock another reader's copied chest");try{ArchitectureTests.export(f.out,family,LabyrinthPlace.FAMILY_COPY);ArchitectureTests.export(f.out,LiteraryCopies.base(p.server,p.getUUID(),LabyrinthPlace.OLD_CABIN),LabyrinthPlace.OLD_CABIN);}catch(Exception e){throw new IllegalStateException(e);}h.succeed();});});}
    @AfterBatch(batch="literary_copy") public static void copyDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_stairs",timeoutTicks=200)
    public static void everyNativeWinchesterFlightAndTurnSupportsTheActualPlayerBody(GameTestHelper h){var f=new Fixture(h);f.build(LabyrinthPlace.WINCHESTER);ArchitectureTests.literaryRoutes(h,f.in,f.base(LabyrinthPlace.WINCHESTER));h.succeed();}
    @AfterBatch(batch="literary_stairs") public static void stairsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_cabin",timeoutTicks=1000)
    public static void theVisitorsAskInAFixedOrderAndEverythingGivenIsKeptForGood(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.END_WORLD_CABIN;var p=f.player("cabin_giver",place);var peer=f.player("cabin_peer",place);final int T=75;
        CabinBargain.awaken(p);var home=f.base(place);
        h.assertTrue(f.own(p,place).getBoolean("Woke")&&!p.isSleeping()&&p.getZ()<home.getZ()-25&&p.getX()<home.getX()-4,"the reader begins the visit waking in a bed in the cabin's bedroom");
        f.source(p,place);double health=p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);var kept=p.getMainArm();
        h.runAfterDelay(T-5,()->{var own=f.own(p,place);h.assertTrue(own.getInt("Knocks")>=1&&!own.getBoolean("Met")&&!own.getBoolean("Answered"),"they knock at the front door and wait to be answered");
            f.click(p,place,LiteraryRooms.CABIN_DOOR);h.assertTrue(f.own(p,place).getBoolean("Answered"),"opening the front door answers it");});
        h.runAfterDelay(T+15,()->{var own=f.own(p,place);
            h.assertTrue(own.getBoolean("Met")&&own.getString("Speech").equals("meet")&&!f.own(peer,place).getBoolean("Met"),"answering the door starts only this reader's first speech");
            var shared=LiteraryVignettes.shared(f.data(),place);for(int i=0;i<4;i++){var visitor=shared.hasUUID("Visitor"+i+"_"+p.getUUID())?f.out.getEntity(shared.getUUID("Visitor"+i+"_"+p.getUUID())):null;
                h.assertTrue(visitor instanceof LiteraryActor a&&a.owner().isPresent()&&a.owner().get().equals(p.getUUID()),"each of the four visitors is this reader's own");}
            CabinBargain.open(p);h.assertTrue(!(p.containerMenu instanceof LiteraryChoiceMenu),"nothing can be answered before they have asked");CabinBargain.hurry(p);});
        h.runAfterDelay(T+30,()->{h.assertTrue(f.own(p,place).getBoolean("Asking"),"after the first speech Leonard waits for an answer");CabinBargain.open(p);h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu,"speaking to them opens the native answer");
            p.containerMenu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,p);var own=f.own(p,place);
            h.assertTrue(BodyLoss.hearts(p.server,p.getUUID())==1&&p.getMaxHealth()==health-2&&own.getInt("Given")==1&&own.getString("Speech").equals("ask2"),"one heart is given and Adriane is next");CabinBargain.hurry(p);});
        h.runAfterDelay(T+45,()->{CabinBargain.open(p);p.containerMenu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,p);
            h.assertTrue(BodyLoss.hearts(p.server,p.getUUID())==2&&p.getMaxHealth()==health-4&&f.own(p,place).getString("Speech").equals("ask3"),"another heart, then Sabrina");CabinBargain.hurry(p);});
        h.runAfterDelay(T+60,()->{p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.TORCH,3));CabinBargain.open(p);p.containerMenu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,p);
            h.assertTrue(f.own(p,place).getBoolean("Confirm")&&!BodyLoss.oneArmed(p),"the arm is asked for twice before it is taken");});
        h.runAfterDelay(T+66,()->{h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu,"the second question is its own native dialogue");p.containerMenu.clicked(2,0,net.minecraft.world.inventory.ClickType.PICKUP,p);var own=f.own(p,place);
            h.assertTrue(BodyLoss.oneArmed(p)&&BodyLoss.missing(p)==kept.getOpposite()&&p.getMainArm()==kept&&CabinBargain.held(p)&&own.getBoolean("Sacrificed"),"the off-hand arm is given and the reader is held for the scene");
            h.assertTrue(p.getOffhandItem().isEmpty()&&p.getInventory().items.stream().anyMatch(s->s.is(Items.TORCH)&&s.getCount()==3),"what the hand held goes back into the pack, whole");
            var arm=MotherCollection.get(p.server).entry(own.getUUID("Offering"));h.assertTrue(arm!=null&&arm.sealed&&arm.item(p.registryAccess()).is(LiteraryRegistry.GIVEN_ARM.get()),"the keeper holds the arm, sealed");
            h.assertTrue(!f.own(peer,place).getBoolean("Met")&&!BodyLoss.oneArmed(peer)&&BodyLoss.hearts(p.server,peer.getUUID())==0,"nothing is taken from the peer");});
        h.runAfterDelay(T+66+CabinBargain.DONE+15,()->{var own=f.own(p,place);h.assertTrue(!CabinBargain.held(p)&&own.getBoolean("CutDone")&&own.getString("Speech").equals("after"),"the scene ends and the four speak once more");
            p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
            h.assertTrue(p.getOffhandItem().isEmpty()&&p.getInventory().items.stream().anyMatch(s->s.is(Items.SHIELD)),"nothing stays in a hand that is not there");
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BREAD));var swap=new net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent.Hands(p);NeoForge.EVENT_BUS.post(swap);h.assertTrue(swap.isCanceled(),"there is no other hand to pass it to");
            var modifier=p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);modifier.removeModifier(BodyLoss.HEARTS);BodyLoss.apply(p);h.assertTrue(p.getMaxHealth()==health-4,"the hearts are restored from the saved record after any fresh body");
            CabinBargain.hurry(p);});
        h.runAfterDelay(T+66+CabinBargain.DONE+60,()->{var own=f.own(p,place);var b=f.base(place);var shared=LiteraryVignettes.shared(f.data(),place);var leonard=f.out.getEntity(shared.getUUID("Visitor0_"+p.getUUID()));
            h.assertTrue(own.contains("Walk0")&&(leonard==null||leonard.position().distanceTo(new Vec3(b.getX()+.5,leonard.getY(),b.getZ()-10.2))>.5),"after the gift the four go to the lake");
            f.at(p,place,7,0,-21);f.look(p,b.offset(LiteraryRooms.TV));});
        h.runAfterDelay(T+66+CabinBargain.DONE+420,()->{var own=f.own(p,place);h.assertTrue(own.getIntArray(CabinScreen.HOME).length==CabinScreen.PIXELS&&own.getBoolean("Ready"),"the reader's home, pictured without loading anything, is watched on the television");
            f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.END_WORLD_CABIN)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.END_WORLD_CABIN),"giving everything resolves only the giver's story");
            var globe=SnowGlobeItem.carried(p);h.assertTrue(globe!=null&&globe.is(LiteraryRegistry.SNOW_GLOBE.get()),"the whole globe is left for the reader who gave everything");
            var death=new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(p,p.damageSources().generic());NeoForge.EVENT_BUS.post(death);
            h.assertTrue(death.isCanceled()&&p.getHealth()==2&&f.own(p,place).contains("GlobeFell"),"once a day the globe will not let its keeper die");
            var again=new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(p,p.damageSources().generic());NeoForge.EVENT_BUS.post(again);h.assertTrue(!again.isCanceled(),"until the snow settles, it cannot catch them again");
            h.succeed();});
    }
    @AfterBatch(batch="literary_cabin") public static void cabinDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_cabin_refusal",timeoutTicks=600)
    public static void refusingKeepsWhatWasGivenAndClosesOnlyTheReadersOwnRoom(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.END_WORLD_CABIN;var p=f.player("cabin_refuser",place);var peer=f.player("cabin_onlooker",place);f.at(peer,place,-9,0,-2);
        f.build(LabyrinthPlace.HILL_NURSERY);f.data().visit(p.getUUID(),LabyrinthPlace.HILL_NURSERY);f.data().visit(peer.getUUID(),LabyrinthPlace.HILL_NURSERY);final int T=75;
        CabinBargain.awaken(p);
        // The account answers its source: read too soon, it says so rather than counting silently for nothing.
        f.source(p,place);
        h.runAfterDelay(T-5,()->f.click(p,place,LiteraryRooms.CABIN_DOOR));
        h.runAfterDelay(T+15,()->CabinBargain.hurry(p));
        h.runAfterDelay(T+30,()->{CabinBargain.open(p);p.containerMenu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,p);CabinBargain.hurry(p);});
        h.runAfterDelay(T+45,()->{CabinBargain.open(p);p.containerMenu.clicked(4,0,net.minecraft.world.inventory.ClickType.PICKUP,p);var own=f.own(p,place);var d=f.data();
            h.assertTrue(own.getBoolean("Refused")&&own.getInt("RefusedAt")==1&&BodyLoss.hearts(p.server,p.getUUID())==1,"a refusal is an answer; the heart already given stays given");
            h.assertTrue(own.getString("ClosedRoom").equals(LabyrinthPlace.HILL_NURSERY.id())&&LiteraryCabinChoices.closed(d,p.getUUID(),LabyrinthPlace.HILL_NURSERY)&&!LiteraryCabinChoices.closed(d,peer.getUUID(),LabyrinthPlace.HILL_NURSERY),"the unfinished room the reader left goes dark for that reader alone");
            h.assertTrue(!LiteraryVignettes.canDeal(d,p.getUUID(),LabyrinthPlace.HILL_NURSERY)&&LiteraryVignettes.canDeal(d,peer.getUUID(),LabyrinthPlace.HILL_NURSERY),"the House deals the closed room to everyone else");
            h.assertTrue(own.getString("Speech").equals("refuse1"),"Adriane answers the refusal");CabinBargain.hurry(p);});
        h.runAfterDelay(T+110,()->{var b=f.base(place);var shared=LiteraryVignettes.shared(f.data(),place);var adriane=f.out.getEntity(shared.getUUID("Visitor1_"+p.getUUID()));var leonard=f.out.getEntity(shared.getUUID("Visitor0_"+p.getUUID()));
            h.assertTrue(adriane!=null&&adriane.position().distanceTo(new Vec3(b.getX()-2.6,adriane.getY(),b.getZ()-10.8))>2&&leonard!=null&&Math.abs(leonard.getX()-b.getX()-.5)<.1&&Math.abs(leonard.getZ()-b.getZ()+10.2)<.1,"only the one who asked walks toward the lake");
            f.at(p,place,7,0,-21);f.look(p,b.offset(LiteraryRooms.TV));});
        h.runAfterDelay(T+320,()->{var own=f.own(p,place);h.assertTrue(own.getIntArray(CabinScreen.ROOM).length==CabinScreen.PIXELS&&own.getBoolean("Ready"),"the closed room is pictured and watched going dark");
            f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.END_WORLD_CABIN),"refusing, read on the screen, resolves the story too");
            var globe=SnowGlobeItem.carried(p);h.assertTrue(globe!=null&&globe.is(LiteraryRegistry.CRACKED_GLOBE.get()),"the cracked globe is left for the reader who refused");
            var death=new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(p,p.damageSources().generic());NeoForge.EVENT_BUS.post(death);
            h.assertTrue(death.isCanceled()&&SnowGlobeItem.dry(globe)&&f.own(p,place).getBoolean("CrackedSpent"),"the cracked globe catches its keeper once");
            var again=new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(p,p.damageSources().generic());NeoForge.EVENT_BUS.post(again);h.assertTrue(!again.isCanceled(),"then it is dry for good");
            h.succeed();});
    }
    @AfterBatch(batch="literary_cabin_refusal") public static void cabinRefusalDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_closure_personal",timeoutTicks=100)
    public static void anEarlierWorldClosureNowBelongsOnlyToTheReaderWhoChoseIt(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.END_WORLD_CABIN;var p=f.player("old_chooser",place);var other=f.player("later_reader",place);f.build(LabyrinthPlace.HILL_NURSERY);
        var own=f.own(p,place);own.putBoolean("ChoiceMade",true);own.putBoolean("Refused",true);own.putString("ClosedRoom",LabyrinthPlace.HILL_NURSERY.id());LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
        var closure=new CompoundTag();closure.putString("Retired",LabyrinthPlace.HILL_NURSERY.id());closure.putUUID("Chooser",p.getUUID());closure.putBoolean("Safety0437",true);f.data().setState(LiteraryCabinChoices.CLOSURE,closure);
        LiteraryCabinChoices.reconcileClosure(p.server);var d=f.data();
        h.assertTrue(d.state(LiteraryCabinChoices.CLOSURE).getString("Retired").isEmpty()&&LiteraryCabinChoices.closed(d,p.getUUID(),LabyrinthPlace.HILL_NURSERY)&&!LiteraryCabinChoices.closed(d,other.getUUID(),LabyrinthPlace.HILL_NURSERY),"the earlier refusal stays the chooser's own and the room reopens for everyone else");
        h.assertTrue(CabinBargain.legacy(f.own(p,place))&&!CabinBargain.legacy(f.own(other,place)),"the earlier answer is kept as it was given; a new reader meets the visitors");
        f.reload();h.assertTrue(f.data().state(LiteraryCabinChoices.CLOSURE).getBoolean("Personal0451")&&LiteraryCabinChoices.closed(f.data(),p.getUUID(),LabyrinthPlace.HILL_NURSERY),"the migration persists through native SavedData reload");
        h.succeed();
    }
    @AfterBatch(batch="literary_closure_personal") public static void closurePersonalDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_confession",timeoutTicks=7000)
    public static void theActualCarriedQuillResumesAcrossVisitsAndTheSealedJournalIsReadPersonally(GameTestHelper h){var f=new Fixture(h);var place=LabyrinthPlace.CONFESSION;var p=f.player("quill_reader",place);f.source(p,place);p.getInventory().add(new ItemStack(Items.WRITABLE_BOOK));f.at(p,place,1,0,-18);f.look(p,f.base(place).offset(1,1,-22));journalChapter(h,f,p,0);}
    /**
     * 0.4.121: each chapter ends in the listener's question. The reader answers by actual chat, which never reaches public
     * chat; the fourth question is left in silence and goes down as unanswered.
     */
    private static void journalChapter(GameTestHelper h,Fixture f,ServerPlayer p,int chapter){answerWhenAsked(h,f,p,chapter,0);}
    private static void answerWhenAsked(GameTestHelper h,Fixture f,ServerPlayer p,int chapter,int waited){h.runAfterDelay(15,()->{var place=LabyrinthPlace.CONFESSION;var own=f.own(p,place);
        if(!own.getBoolean("Asked")){h.assertTrue(waited<700,"the listener reaches chapter "+chapter+"'s question while the reader stays before him");answerWhenAsked(h,f,p,chapter,waited+15);return;}
        h.assertTrue(own.getInt("Chapter")==chapter,"he waits on the question before the chapter is done");
        if(chapter==3){own.putInt("AskTicks",ConfessionAnswers.SILENCE_TICKS-15);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);}
        else{var early=new net.neoforged.neoforge.event.ServerChatEvent(p,"My answer to "+chapter+": the cellar door was open.",net.minecraft.network.chat.Component.literal("answer"));NeoForge.EVENT_BUS.post(early);
            h.assertTrue(early.isCanceled(),"the answer to the listener never reaches public chat");h.assertTrue(f.own(p,place).getString("Answer").startsWith("My answer to "+chapter),"his question takes the reader's own words");}
        afterAnswer(h,f,p,chapter,own.getInt("Visit"),0);});}
    /** 0.4.123: the answer settles the chapter; he rests a moment and goes on in the same sitting. */
    private static void afterAnswer(GameTestHelper h,Fixture f,ServerPlayer p,int chapter,int visit,int waited){h.runAfterDelay(5,()->{var place=LabyrinthPlace.CONFESSION;var after=f.own(p,place);
            if(after.getInt("Chapter")==chapter){h.assertTrue(waited<80,"the answer, or the silence, completes the chapter");afterAnswer(h,f,p,chapter,visit,waited+5);return;}
            h.assertTrue(after.getInt("Chapter")==chapter+1,"the answer, or the silence, completes the chapter");
            h.assertTrue(after.getString("JournalText").contains(ConfessionAnswers.QUESTIONS[chapter])&&after.getString("JournalText").contains(chapter==3?ConfessionAnswers.SILENCE:"My answer to "+chapter),"the journal takes down his question and the reply exactly");
            if(chapter<7){h.assertTrue(p.getInventory().countItem(Items.WRITABLE_BOOK)==1,"the same carried native quill remains through the spoken chapters");
                h.assertTrue(after.getInt("RestTicks")>0&&after.getInt("Visit")==visit,"he rests a moment and goes on in the same sitting, without waiting for a return");
                var idle=new net.neoforged.neoforge.event.ServerChatEvent(p,"hello, anyone",net.minecraft.network.chat.Component.literal("hello"));NeoForge.EVENT_BUS.post(idle);h.assertTrue(!idle.isCanceled(),"between chapters, ordinary chat is the reader's own again");
                // Leaving and coming back still resumes where he stopped.
                if(chapter==1){f.arrive(p,place);f.at(p,place,1,0,-18);f.look(p,f.base(place).offset(1,1,-22));}
                journalChapter(h,f,p,chapter+1);return;}
            h.assertTrue(p.getInventory().countItem(Items.WRITABLE_BOOK)==0&&after.contains("SignedJournal")&&!after.getBoolean("Ready"),"the actual quill is sealed once, without earning an unread ending");
            var journal=ItemStack.parseOptional(p.registryAccess(),after.getCompound("SignedJournal")).get(DataComponents.WRITTEN_BOOK_CONTENT);String pages=journal==null?"":journal.pages().stream().map(page->page.raw().getString()).collect(java.util.stream.Collectors.joining("\n"));
            h.assertTrue(pages.contains("My answer to 0")&&pages.contains("My answer to 7")&&pages.contains(ConfessionAnswers.SILENCE)&&pages.contains(ConfessionAnswers.QUESTIONS[5]),"the sealed journal is made of his questions and the reader's own answers: "+pages);
            h.assertTrue(ConfessionAnswers.last(after).startsWith("My answer to 7"),"the account will end on the reader's last answer");
            f.arrive(p,place);f.read(p,place,new BlockPos(10,1,-29));h.runAfterDelay(10,()->{h.assertTrue(f.own(p,place).getBoolean("Read_Journal")&&f.own(p,place).getBoolean("Ready"),"personally reading the exact sealed journal resolves the later return");f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.CONFESSION),"the owned final account yields this one source");h.succeed();});});}
    @GameTest(template="empty",batch="literary_confession_words",timeoutTicks=100)
    public static void theListenerRepeatsAFragmentOfTheReadersOwnWords(GameTestHelper h){
        h.assertTrue(ConfessionAnswers.fragment("I opened the cellar door, twice.").equals("opened the cellar"),"he repeats the longest word and the two after it");
        h.assertTrue(ConfessionAnswers.fragment("Me.").equals("me")&&ConfessionAnswers.fragment("...").isEmpty(),"a short answer is repeated whole, punctuation alone not at all");
        h.assertTrue(ConfessionAnswers.QUESTIONS.length==LiteraryTexts.CONFESSION.length,"one question for every chapter he dictates");
        var own=new CompoundTag();var pages=ConfessionAnswers.pages(own,"Counts.","Reader");
        h.assertTrue(pages.size()==ConfessionAnswers.QUESTIONS.length+1&&pages.get(0).contains(ConfessionAnswers.SILENCE)&&pages.get(pages.size()-1).endsWith("Signed: Reader"),"a journal with no answers says so under every question, and is still signed");
        h.succeed();
    }
    @AfterBatch(batch="literary_confession") public static void confessionDone(ServerLevel l){close();}


    /** 0.4.74: posts a reader's own click where they stand, as the client sends it. */
    private static boolean use(ServerPlayer p,BlockPos at,InteractionHand hand){var e=new PlayerInteractEvent.RightClickBlock(p,hand,at,new BlockHitResult(at.getCenter(),Direction.UP,at,false));NeoForge.EVENT_BUS.post(e);return e.isCanceled();}
    private static int stage(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.is(LiteraryRegistry.NURSERY.get())?s.getValue(NurseryBlock.STAGE):-1;}
    /** Look at each sealed exit long enough to take it in, from where a child would stand. */
    private static void examine(GameTestHelper h,Fixture f,ServerPlayer p,int from){var place=LabyrinthPlace.CHILD_ROOM;var b=f.base(place);
        double[][] stands={{-6.5,0,-18.5},{6.5,0,-18.5},{-6.5,0,-11.5},{7.5,0,-7.5}};
        for(int i=0;i<ChildRoom.EXITS.size();i++){final int exit=i;h.runAfterDelay(from+i*30,()->{var s=stands[exit];f.at(p,place,s[0],s[1],s[2]);f.look(p,b.offset(ChildRoom.EXITS.get(exit).get(ChildRoom.EXITS.get(exit).size()-1)));});}
    }
    @GameTest(template="empty",batch="literary_child_sequence",timeoutTicks=1300)
    public static void childRoomExitsGoUnseenAfterTheCardAndOnlyTheReadersOwnCrawlToTheBasementResolves(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.CHILD_ROOM;var p=f.player("child_reader",place);var peer=f.player("child_peer",place);var observer=f.player("child_spectator",place);observer.setGameMode(GameType.SPECTATOR);
        var b=f.base(place);var l=f.in;
        h.runAfterDelay(10,()->{
            h.assertTrue(l.getBlockState(b.offset(ChildRoom.EXITS.get(0).get(0))).is(Blocks.GLASS)&&NurseryBlock.is(l.getBlockState(b.offset(ChildRoom.TOY_HOMES.get(0))),NurseryBlock.Kind.TEDDY)
                    &&l.getBlockState(b.offset(ChildRoom.EXITS.get(2).get(0))).getBlock() instanceof DoorBlock,"the room stands whole: two windows, its own doors, the bear by the bed");
            h.assertTrue(l.noCollision(new AABB(b.getX()+.3,b.getY()+.01,b.getZ()-21.7,b.getX()+.7,b.getY()+.34,b.getZ()-21.3))&&!l.noCollision(new AABB(b.getX()+.3,b.getY()+.4,b.getZ()-21.7,b.getX()+.7,b.getY()+.9,b.getZ()-21.3))
                    &&ChildRoom.besideBed(new BlockPos(0,0,-21))&&l.getBlockState(b.offset(ChildRoom.GAP)).is(Blocks.BIRCH_PLANKS)&&l.getBlockState(b.offset(ChildRoom.GAP.below())).is(Blocks.LADDER)&&l.getBlockState(b.offset(6,-3,-10)).isAir(),"the bed is low: a child lying flat fits under it, standing does not; the way down is a floorboard like the rest, and there is a basement below");
            h.assertTrue(!NurseryBlock.is(l.getBlockState(b.offset(-9,1,-20)),NurseryBlock.Kind.DOLL)&&NurseryBlock.is(l.getBlockState(b.offset(ChildRoom.TOY_HOMES.get(1))),NurseryBlock.Kind.DOLL),"one doll, on the shelf, and nothing on the toy chest");
            // 0.4.121: a hand on the desk finds the small card, and opening it starts the room, read to the end or not.
            f.click(p,place,ChildRoom.CARD.below());h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages&&ChildRoom.readCard(f.own(p,place))&&!f.own(p,place).getBoolean("Read_Source"),"the desk opens the bedtime card, and opening it is enough to start the room");p.closeContainer();
            // Both readers face the door they came in by; the spectator watches the hall door.
            f.at(p,place,.5,0,-4.5);f.look(p,b.offset(0,1,1));f.at(peer,place,-.5,0,-4.5);f.look(peer,b.offset(0,1,1));
            f.at(observer,place,-8.5,0,-11.5);f.look(observer,b.offset(-11,0,-12));
        });
        h.runAfterDelay(430,()->{var world=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(world.getInt("Exits0474")==3&&l.getBlockState(b.offset(ChildRoom.EXITS.get(0).get(0))).is(Blocks.BIRCH_PLANKS)==false&&!l.getBlockState(b.offset(ChildRoom.EXITS.get(0).get(0))).is(Blocks.GLASS),"after the card, the windows went unseen: "+world.getInt("Exits0474"));
            h.assertTrue(l.getBlockState(b.offset(ChildRoom.EXITS.get(2).get(0))).getBlock() instanceof DoorBlock&&f.own(observer,place).getInt("ExitSeen0474")==0,"a spectator's gaze keeps the hall door, and earns nothing");
            h.assertTrue(l.getBlockState(b.offset(ChildRoom.CEILING_SLOTS.get(0))).getValue(NurseryBlock.STAGE)==NurseryBlock.CEILING&&!NurseryBlock.is(l.getBlockState(b.offset(ChildRoom.TOY_HOMES.get(0))),NurseryBlock.Kind.TEDDY),"the bear has left the floor for the ceiling");
            h.assertTrue(Integer.bitCount(f.own(p,place).getInt("Parents0119"))==1&&f.own(p,place).getLong("ParentsNext127")>l.getGameTime(),"the parents leave a real pause after the first line while the exits continue disappearing");
            f.at(observer,place,-8.5,0,-4.5);f.look(observer,b.offset(-8,1,1));
        });
        h.runAfterDelay(700,()->{h.assertTrue(LiteraryVignettes.shared(f.data(),place).getInt("Exits0474")==ChildRoom.ALL&&!(l.getBlockState(b.offset(ChildRoom.EXITS.get(3).get(0))).getBlock() instanceof DoorBlock),"once unwatched the doors went too");
            h.assertTrue(f.own(p,place).getInt("ExitSeen0474")==0,"nothing counts as seen until the reader looks");});
        examine(h,f,p,710);
        h.runAfterDelay(840,()->{h.assertTrue(f.own(p,place).getInt("ExitSeen0474")==ChildRoom.ALL&&f.own(p,place).getBoolean("AllSeen0474")&&f.own(peer,place).getInt("ExitSeen0474")==0,"the reader has seen every exit themselves; the peer, who never read the card, has not");
            h.assertTrue(f.own(p,place).getBoolean("DoorShut0119")&&!f.own(peer,place).getBoolean("DoorShut0119"),"the doorway back to the House is wall to the reader shut in, not to the peer who never read the card");
            h.assertTrue(Integer.bitCount(f.own(p,place).getInt("Parents0119"))==2&&f.own(p,place).getLong("ParentsNext127")>l.getGameTime()&&f.own(peer,place).getInt("Parents0119")==0,"the reader hears only two spaced parental lines before crawling; the peer hears nothing");
            f.source(p,place);p.closeContainer();f.at(p,place,.5,0,-22.5);f.at(peer,place,6.5,-4,-12.5);
            NurseryToys.play(p,l,b.offset(ChildRoom.BED_FOOT),l.getBlockState(b.offset(ChildRoom.BED_FOOT)),b);
            h.assertTrue(l.getBlockState(b.offset(ChildRoom.GAP)).getBlock() instanceof TrapDoorBlock&&l.getBlockState(b.offset(ChildRoom.GAP)).getValue(TrapDoorBlock.OPEN),"looking under the bed finds the trapdoor in the boards");});
        h.runAfterDelay(852,()->{h.assertTrue(!f.own(p,place).getBoolean("Ready"),"under the bed is not yet the way out");f.at(p,place,.5,-2,-22.5);});
        h.runAfterDelay(864,()->{h.assertTrue(f.own(p,place).getBoolean("Crawled0474"),"the reader has gone below the floor");f.at(p,place,6.5,-2,-18.5);});
        h.runAfterDelay(876,()->f.at(p,place,6.5,-4,-12.5));
        h.runAfterDelay(890,()->{
            h.assertTrue(f.own(p,place).getBoolean("Ready")&&"examined_the_lost_exits_and_crawled_below_the_bed".equals(f.own(p,place).getString("Outcome")),"seen, crawled and stood up below: the last account is the reader's");
            h.assertTrue(!f.own(peer,place).getBoolean("Ready")&&!f.own(observer,place).getBoolean("Ready"),"a peer who walked into the basement and a spectator have nothing");
            h.assertTrue(!f.own(p,place).getBoolean("DoorShut0119"),"finished, the reader sees the doorway again");
            var world=LiteraryVignettes.shared(f.data(),place);h.assertTrue(world.hasUUID("Dinnerbone")&&l.getEntity(world.getUUID("Dinnerbone")) instanceof LiteraryActor figure&&"Dinnerbone".equals(figure.getName().getString())&&figure.isNoGravity()&&Math.abs(figure.getY()-b.getY()-4.1)<.01,"Dinnerbone stands on the ceiling of the shut room, the wrong way up");
            f.read(p,place,LiteraryRooms.ending(place));
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id()))&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.of(place.id())),"only the reader's own reading resolves the room");
            // The way back up: the hatch opens from below and shuts itself once nobody is in it.
            f.at(p,place,4.5,-3,-4.5);h.assertTrue(use(p,b.offset(ChildRoom.HATCH),InteractionHand.MAIN_HAND)&&l.getBlockState(b.offset(ChildRoom.HATCH)).getValue(TrapDoorBlock.OPEN),"the hatch opens from below");
            f.at(p,place,.5,0,-6.5);f.at(peer,place,-2.5,0,-6.5);
        });
        h.runAfterDelay(1010,()->{h.assertTrue(!l.getBlockState(b.offset(ChildRoom.HATCH)).getValue(TrapDoorBlock.OPEN),"and it shuts itself behind them");h.succeed();});
    }
    @AfterBatch(batch="literary_child_sequence") public static void childSequenceDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_child_aftermath",timeoutTicks=400)
    public static void lateReadersMustSeeEverySealedExitThemselvesBeforeTheirOwnCrawlCounts(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.CHILD_ROOM;f.build(place);var b=f.base(place);
        for(int i=0;i<ChildRoom.EXITS.size();i++)h.assertTrue(ChildRoom.seal(f.in,b,i),"an exit goes while nobody is there to see it: "+i);
        var world=LiteraryVignettes.shared(f.data(),place);world.putInt("Exits0474",ChildRoom.ALL);LiteraryVignettes.shared(f.data(),place,world);
        var late=f.player("child_late",place);
        h.runAfterDelay(5,()->{h.assertTrue(f.own(late,place).getInt("ExitsAtArrival0474")==ChildRoom.ALL,"the late reader came into a shut room");f.at(late,place,.5,-2,-22.5);});
        h.runAfterDelay(15,()->f.at(late,place,6.5,-4,-12.5));
        h.runAfterDelay(27,()->{h.assertTrue(!f.own(late,place).getBoolean("Ready")&&!f.own(late,place).getBoolean("Crawled0474"),"crawling alone cannot borrow the shared losses");f.source(late,place);});
        examine(h,f,late,35);
        h.runAfterDelay(165,()->{h.assertTrue(f.own(late,place).getInt("ExitSeen0474")==ChildRoom.ALL,"every sealed threshold was looked at by the late reader");f.at(late,place,.5,-2,-22.5);});
        h.runAfterDelay(177,()->f.at(late,place,6.5,-4,-12.5));
        h.runAfterDelay(190,()->{h.assertTrue(f.own(late,place).getBoolean("Ready")&&"examined_the_sealed_thresholds_and_found_the_remaining_crawl".equals(f.own(late,place).getString("Outcome")),"their own crawl found the remaining way");h.succeed();});
    }
    @AfterBatch(batch="literary_child_aftermath") public static void childAftermathDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_child_toys",timeoutTicks=400)
    public static void childRoomToysPlayInTheirOwnSharedStatesAndConferNothing(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.CHILD_ROOM;var p=f.player("child_player",place);var peer=f.player("child_watcher",place);var b=f.base(place);var l=f.in;
        var top=new BlockPos(3,0,-8);var music=new BlockPos(-10,1,-15);var ball=ChildRoom.TOY_HOMES.get(3);
        h.runAfterDelay(5,()->{
            var teddy=ChildRoom.TOY_HOMES.get(0);f.click(p,place,teddy);
            h.assertTrue(l.getBlockState(b.offset(teddy)).getValue(NurseryBlock.FACING)==Direction.SOUTH,"the bear turns to the reader who squeezed it");
            var blocks=ChildRoom.TOY_HOMES.get(2);for(int i=1;i<=3;i++){f.click(p,place,blocks);h.assertTrue(stage(l,b.offset(blocks))==i%3,"the blocks stack, stack again, and fall: "+i);}
            var jack=new BlockPos(-6,0,-14);for(int i=0;i<7;i++)f.click(p,place,jack);h.assertTrue(stage(l,b.offset(jack))==0,"seven turns of the handle and nothing yet");
            f.click(p,place,jack);h.assertTrue(stage(l,b.offset(jack))==1,"the eighth turn pops the jack");f.click(p,place,jack);h.assertTrue(stage(l,b.offset(jack))==0,"and he is pushed back in");
            f.click(p,place,music);h.assertTrue(stage(l,b.offset(music))==1,"the music box opens and plays");
            f.click(p,place,top);h.assertTrue(NurseryToys.busy(b.offset(top)),"the top spins");
            f.click(p,place,ChildRoom.TRACK.get(0));h.assertTrue(NurseryToys.busy(b.offset(ChildRoom.TRACK.get(0))),"the train sets off round its track");
            f.click(p,place,ball);
            for(var lid:List.of(new BlockPos(-9,0,-20),new BlockPos(-9,0,-6))){f.click(p,place,lid);h.assertTrue(stage(l,b.offset(lid))==1,"the lid or front opens: "+lid);}
            f.click(p,place,new BlockPos(10,0,-13));h.assertTrue(stage(l,b.offset(10,0,-13))==1&&stage(l,b.offset(10,1,-13))==1,"both wardrobe doors open together");
            f.click(p,place,new BlockPos(-11,0,-12));h.assertTrue(!l.getBlockState(b.offset(-11,0,-12)).getValue(DoorBlock.OPEN),"the room's own doors stay shut");
            f.at(p,place,4.5,0,-3.5);h.assertTrue(use(p,b.offset(ChildRoom.HATCH),InteractionHand.MAIN_HAND)&&!l.getBlockState(b.offset(ChildRoom.HATCH)).getValue(TrapDoorBlock.OPEN),"the hatch is fastened from underneath");
            f.at(p,place,.5,0,-12.5);h.assertTrue(use(p,b.offset(ChildRoom.TOY_HOMES.get(2)),InteractionHand.OFF_HAND)&&stage(l,b.offset(ChildRoom.TOY_HOMES.get(2)))==0,"the client's off-hand try sets nothing down on a toy");
        });
        h.runAfterDelay(30,()->{h.assertTrue(!NurseryBlock.is(l.getBlockState(b.offset(ball)),NurseryBlock.Kind.BALL)&&NurseryBlock.is(l.getBlockState(b.offset(ball).north(4)),NurseryBlock.Kind.BALL),"the ball rolls four blocks the way it was pushed");});
        h.runAfterDelay(220,()->{
            h.assertTrue(stage(l,b.offset(music))==0&&stage(l,b.offset(top))==1&&NurseryBlock.is(l.getBlockState(b.offset(ChildRoom.TRACK.get(0))),NurseryBlock.Kind.TRAIN),"the tune ends, the top falls over, the train comes home");
            h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0&&!f.own(p,place).getBoolean("Ready")&&LiteraryVignettes.shared(f.data(),place).getInt("Exits0474")==0,"play is play: no exit goes and nothing is earned");
            h.succeed();});
    }
    @AfterBatch(batch="literary_child_toys") public static void childToysDone(ServerLevel l){close();}
    // ------------------------------------------------------------------------------------------------ the Last Stop (0.4.78)
    private static List<ItemStack> drinks(ServerPlayer p){var out=new ArrayList<ItemStack>();for(var s:p.getInventory().items){var tag=s.get(DataComponents.CUSTOM_DATA);if(tag!=null&&tag.copyTag().contains(ElkLotNight.DRINK))out.add(s);}return out;}
    private static void order(ServerPlayer p,LiteraryActor marla,int slot){LiteraryVignettes.talk(p,marla);if(p.containerMenu instanceof LiteraryChoiceMenu menu)menu.clicked(slot,0,ClickType.PICKUP,p);}
    @GameTest(template="empty",batch="literary_last_stop_bar",timeoutTicks=400)
    public static void theLastStopServesOnEachReadersOwnTabAndPlaysWhatIsPicked(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_LOT;var p=f.player("tab_reader",place);var peer=f.player("tab_peer",place);var b=f.base(place);var l=f.out;
        h.assertTrue(ElkLot.built(f.data(),b)&&RoadhouseBlock.is(l.getBlockState(b.offset(ElkLot.REGISTER)),RoadhouseBlock.Kind.REGISTER)&&RoadhouseBlock.is(l.getBlockState(b.offset(ElkLot.BOARD)),RoadhouseBlock.Kind.CORKBOARD)
                &&l.getBlockState(b.offset(ElkLot.JUKEBOX)).is(Blocks.JUKEBOX)&&RoadhouseBlock.is(l.getBlockState(b.offset(ElkLot.dent())),RoadhouseBlock.Kind.PU_DOOR),"a fresh lot is the bar: its register, its noticeboard, its jukebox and the pickups outside");
        f.at(p,place,-21.5,0,-22.5);f.at(peer,place,-18.5,0,-20.5);
        var room=place.room();f.chunks.hold(l,new AABB(b.getX()+room.minX()-1,b.getY()+room.minY()-1,b.getZ()+room.minZ()-1,b.getX()+room.maxX()+2,b.getY()+room.maxY()+2,b.getZ()+room.maxZ()+2));
        h.startSequence().thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready()&&ElkLot.loaded(l,b),"the actual bar chunks and native cast sections are resident");
            var world=LiteraryVignettes.shared(f.data(),place);
            for(var who:ElkLotNight.CAST){var original=world.hasUUID(who.key())?l.getEntity(world.getUUID(who.key())):null;
                h.assertTrue(original instanceof LiteraryActor a&&a.phase()==who.phase()&&a.position().distanceToSqr(who.home(b))<.1,who.name()+" is in her or his place with their own skin; original="+original);}
        }).thenExecute(()->{
            var world=LiteraryVignettes.shared(f.data(),place);
            var marla=(LiteraryActor)l.getEntity(world.getUUID(ElkLotNight.MARLA.key()));
            order(p,marla,1);
            h.assertTrue(drinks(p).size()==1&&f.own(p,place).getInt("Tab")==250&&f.own(p,place).getInt("Drinks")==1&&f.own(peer,place).getInt("Tab")==0,"a beer goes on the reader's own tab and nobody else's");
            for(int i=0;i<6;i++)order(p,marla,3);
            h.assertTrue(f.own(p,place).getInt("Drinks")==ElkLotNight.MAX_DRINKS&&f.own(p,place).getInt("Tab")==250+5*400&&drinks(p).size()==ElkLotNight.MAX_DRINKS,"five whiskeys more and then no more: six drinks a reader");
            var whiskey=drinks(p).get(drinks(p).size()-1);
            NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish(p,whiskey.copy(),0,new ItemStack(Items.GLASS_BOTTLE)));
            h.assertTrue(p.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION),"a whiskey goes to the head");
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GLASS_BOTTLE));LiteraryVignettes.talk(p,marla);
            h.assertTrue(p.getMainHandItem().isEmpty()&&!(p.containerMenu instanceof LiteraryChoiceMenu),"Marla takes an empty glass back");
            h.assertTrue(l.getBlockEntity(b.offset(ElkLot.JUKEBOX)) instanceof JukeboxBlockEntity box&&box.getTheItem().is(Items.MUSIC_DISC_STAL),"the jukebox is playing when the night starts");
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.MUSIC_DISC_CAT));f.click(p,place,ElkLot.JUKEBOX);
            h.assertTrue(p.getMainHandItem().is(Items.MUSIC_DISC_CAT)&&((JukeboxBlockEntity)l.getBlockEntity(b.offset(ElkLot.JUKEBOX))).getTheItem().is(Items.MUSIC_DISC_MALL),"a press picks the next record and the reader's own disc stays in hand");
            for(int i=0;i<3;i++)f.click(p,place,ElkLot.JUKEBOX);
            h.assertTrue(((JukeboxBlockEntity)l.getBlockEntity(b.offset(ElkLot.JUKEBOX))).getTheItem().isEmpty()&&l.getEntitiesOfClass(ItemEntity.class,new AABB(b.offset(ElkLot.JUKEBOX)).inflate(4)).isEmpty(),"round the records to silence, and nothing ever comes out of it");
            f.click(p,place,ElkLot.POOL_L);
            h.assertTrue(l.getBlockState(b.offset(ElkLot.POOL_L)).getValue(RoadhouseBlock.STAGE)==1&&l.getBlockState(b.offset(ElkLot.POOL_R)).getValue(RoadhouseBlock.STAGE)==1,"the break scatters the balls across both halves of the table");
            f.click(p,place,ElkLot.BOARD);h.assertTrue(!(p.containerMenu instanceof LiteraryVignettes.Pages),"the clipping is not anyone's yet");
            f.source(p,place);h.assertTrue(f.own(p,place).getBoolean("Read_Source")&&!f.own(peer,place).getBoolean("Read_Source"),"the till receipt by the register is the source, read by its own reader");
        }).thenSucceed();
    }
    @AfterBatch(batch="literary_last_stop_bar") public static void lastStopBarDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_last_stop_night",timeoutTicks=1200)
    public static void theElkDentsATruckLooksAtTheReaderAndTheMenHerdWithoutTouching(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_LOT;var p=f.player("night_reader",place);var b=f.base(place);var l=f.out;var room=place.room();
        f.chunks.hold(l,new AABB(b.getX()+room.minX(),b.getY()-4,b.getZ()+room.minZ(),b.getX()+room.maxX()+1,b.getY()+12,b.getZ()+2));
        boolean[] named={false};double[] closest={99};
        h.onEachTick(()->{var world=LiteraryVignettes.shared(f.data(),place);
            for(var who:ElkLotNight.MEN){if(!world.hasUUID(who.key())||!(l.getEntity(world.getUUID(who.key())) instanceof LiteraryActor man))continue;
                if(man.getPersistentData().getInt("Leg0478")>0)closest[0]=Math.min(closest[0],man.distanceTo(p));
                if(man.getCustomName()!=null&&man.isCustomNameVisible()&&man.getCustomName().getString().contains("night_reader"))named[0]=true;}});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&ElkLot.loaded(l,b),"wait for native chunks and entity sections")).thenExecute(()->{
            f.at(p,place,-3.5,0,-8.5);p.setYRot(180);p.setXRot(10);
            var world=LiteraryVignettes.shared(f.data(),place);world.putInt("Night",ElkLotNight.EARLY-2);LiteraryVignettes.shared(f.data(),place,world);
        }).thenIdle(75).thenExecute(()->{
            var world=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(world.contains("CrashAt")&&world.getInt("Alarm")>0&&RoadhouseBlock.is(l.getBlockState(b.offset(ElkLot.dent())),RoadhouseBlock.Kind.PU_DOOR_DENT),"a reader out in the lot brings it on: the truck is dented and its alarm going");
            h.assertTrue(world.hasUUID("Elk")&&l.getEntity(world.getUUID("Elk")) instanceof LiteraryElk elk&&!elk.isInvisible()&&elk.wounded(),"the native crash leaves a visible wound on the same real elk");
            var elk=(LiteraryElk)l.getEntity(world.getUUID("Elk"));var saved=new CompoundTag();elk.saveWithoutId(saved);
            h.assertTrue(saved.getBoolean("Wound")&&!f.own(p,place).getBoolean("ElkWounded"),"the crash wound persists without assigning a reader the separate act of striking it");
            var legacy=saved.copy();legacy.putBoolean("Wound",false);elk.load(legacy);
            h.assertTrue(!elk.wounded(),"an earlier saved post-crash body can still have the healthy flag");
        }).thenIdle(120).thenExecute(()->{
            h.assertTrue(f.own(p,place).getBoolean("ElkSeen"),"the reader it stood and looked at saw it");
            var world=LiteraryVignettes.shared(f.data(),place);h.assertTrue(l.getEntity(world.getUUID("Elk")) instanceof LiteraryElk elk&&elk.wounded(),"the ongoing post-crash night restores the wound on that native saved body");
        }).thenIdle(200).thenExecute(()->{
            var world=LiteraryVignettes.shared(f.data(),place);
            for(var who:ElkLotNight.MEN)h.assertTrue(l.getEntity(world.getUUID(who.key())) instanceof LiteraryActor man&&!ElkLotNight.inBar(man.position(),b),who.name()+" has come out");
            var side=l.getBlockState(b.offset(ElkLot.SIDE_DOOR));h.assertTrue(side.getBlock() instanceof DoorBlock&&side.getValue(DoorBlock.OPEN),"they came out of the side door and left it open");
            int lit=0;for(int i=0;i<3;i++)if(world.contains("Beam"+i)&&l.getBlockState(BlockPos.of(world.getLong("Beam"+i))).is(Blocks.LIGHT))lit++;
            h.assertTrue(lit>=2,"their flashlights put real light where they point: "+lit);
        }).thenIdle(380).thenExecute(()->{
            h.assertTrue(named[0],"the men say the reader's name");
            h.assertTrue(closest[0]>=1.5,"nobody laid a hand on the reader: closest "+closest[0]);
            var world=LiteraryVignettes.shared(f.data(),place);var russ=(LiteraryActor)l.getEntity(world.getUUID(ElkLotNight.RUSS.key()));
            h.assertTrue(russ.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(0,0,-6)))<9,"by now one of them stands on the road back");
            var released=(LiteraryElk)l.getEntity(world.getUUID("Elk"));h.assertTrue(world.getInt("ElkStage")==6&&released.position().distanceToSqr(ElkLotNight.REST.add(b.getX(),b.getY(),b.getZ()))<.1,"the same wounded elk physically clears the pickups, fallen rail and ditch to reach the field: stage="+world.getInt("ElkStage")+" leg="+world.getInt("ElkLeg")+" feet="+released.position().subtract(b.getX(),b.getY(),b.getZ()));
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+200.5,0,0);
        }).thenIdle(ElkLotNight.EMPTY_RESET+20).thenExecute(()->{
            var world=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(!world.contains("Night")&&!world.contains("CrashAt"),"empty and unwatched, the night starts over");
            h.assertTrue(RoadhouseBlock.is(l.getBlockState(b.offset(ElkLot.dent())),RoadhouseBlock.Kind.PU_DOOR)&&!l.getBlockState(b.offset(ElkLot.SIDE_DOOR)).getValue(DoorBlock.OPEN),"the truck is mended and the side door shut");
            for(var who:ElkLotNight.CAST)h.assertTrue(l.getEntity(world.getUUID(who.key())) instanceof LiteraryActor a&&a.position().distanceToSqr(who.home(b))<.1,who.name()+" is back in place");
            h.assertTrue(l.getEntity(world.getUUID("Elk")) instanceof LiteraryElk elk&&elk.isInvisible(),"the elk is out of sight again, the same body");
            for(int i=0;i<3;i++)h.assertTrue(!world.contains("Beam"+i),"no flashlight is left burning");
        }).thenSucceed();
    }
    @AfterBatch(batch="literary_last_stop_night") public static void lastStopNightDone(ServerLevel l){close();}
    /**
     * 0.4.187, after The Only Good Indians: striking her fails the night. The reader wakes on the porch to nothing and she
     * follows them home; a later night that ends without a hand raised to her, with nothing killed since, lets them go and
     * resolves as any night does; reading the clipping gives the cap and the tab comes due.
     */
    @GameTest(template="empty",batch="literary_last_stop_struck187",timeoutTicks=1400)
    public static void strikingTheElkFailsTheNightAndSheFollowsUntilALaterNightLetsHerPass(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_LOT;var p=f.player("struck_reader",place);var b=f.base(place);var l=f.out;
        var own=f.own(p,place);own.putBoolean("ElkSeen",true);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);var room=place.room();
        f.chunks.hold(l,new AABB(b.getX()+room.minX(),b.getY()-4,b.getZ()+room.minZ(),b.getX()+room.maxX()+1,b.getY()+12,b.getZ()+2));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&ElkLot.loaded(l,b)&&LiteraryVignettes.shared(f.data(),place).hasUUID("Elk"),"wait for native chunks, entity sections and the elk")).thenExecute(()->{
                var elk=(LiteraryElk)l.getEntity(LiteraryVignettes.shared(f.data(),place).getUUID("Elk"));elk.hurt(l.damageSources().playerAttack(p),2);
                h.assertTrue(ElkFollows.struckTonight(f.own(p,place)),"the strike is remembered as tonight's");
                var mine=f.own(p,place);mine.putInt("FieldLoops",2);mine.putInt("FieldTicks",400);LiteraryVignettes.save(f.data(),p.getUUID(),place,mine);f.at(p,place,4.5,0,-97.2);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean(ElkFollows.FOLLOWS),"the herd takes the striker as it takes anyone, and she follows them home"))
            .thenExecute(()->{var after=f.own(p,place);
                h.assertTrue(!after.getBoolean("Ready")&&after.getString("Outcome").isEmpty()&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.ELK_LOT)&&ElkFollows.overTonight(after),"nothing is resolved: no clipping tonight");
                h.assertTrue(p.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(ElkLot.PORCH)))<.1,"the reader still wakes on the porch");
                p.getInventory().add(new ItemStack(Items.EMERALD,5));f.arrive(p,place);
                var next=f.own(p,place);next.putBoolean("ElkSeen",true);next.putInt("FieldLoops",2);next.putInt("FieldTicks",400);next.putInt("Drinks",2);LiteraryVignettes.save(f.data(),p.getUUID(),place,next);
                h.assertTrue(!ElkFollows.overTonight(next)&&!ElkFollows.struckTonight(next),"a later night is a new night");f.at(p,place,4.5,0,-97.2);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("Ready"),"a later night without a hand raised to her ends as any night does"))
            .thenExecute(()->{var after=f.own(p,place);
                h.assertTrue(ElkLotEnding.HERD.equals(after.getString("Outcome"))&&!after.getBoolean(ElkFollows.FOLLOWS),"nothing killed since: she passes and no longer follows");
                f.source(p,place);f.read(p,place,ElkLot.BOARD);
                h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.ELK_LOT),"the clipping resolves the night");
                h.assertTrue(p.getInventory().items.stream().anyMatch(s->s.is(Items.LEATHER_HELMET)&&s.getHoverName().getString().equals("The cap from the broken post")),"winning gives the cap from the broken post");
                h.assertTrue(p.getInventory().countItem(Items.EMERALD)==3,"and the tab comes due, an emerald a drink: "+p.getInventory().countItem(Items.EMERALD));})
            .thenSucceed();
    }
    @AfterBatch(batch="literary_last_stop_struck187") public static void lastStopStruckDone(ServerLevel l){close();}
    /**
     * 0.4.189: Bly's Lady follows the one she held. Near water at night in the reader's world, the dark closes in, they are held
     * a moment and walked a few steps they did not take, and set down at the water's edge, never in it.
     */
    @GameTest(template="empty",batch="literary_bly_follows189",timeoutTicks=200)
    public static void theLadyFollowsTheOneSheHeldAndSetsThemDownAtTheWatersEdge(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"lady_followed");var d=LabyrinthData.get(h.getLevel().getServer());
        for(int x=0;x<=9;x++)for(int z=0;z<=4;z++){h.setBlock(new BlockPos(x,1,z),Blocks.STONE);h.setBlock(new BlockPos(x,2,z),Blocks.AIR);h.setBlock(new BlockPos(x,3,z),Blocks.AIR);}
        h.setBlock(new BlockPos(8,1,2),Blocks.WATER);
        var start=h.absoluteVec(new Vec3(2.5,2,2.5));p.teleportTo(h.getLevel(),start.x,start.y,start.z,0,0);
        var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.BLY_ROUTE);own.putBoolean(BlyFollows.FOLLOWS,true);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.BLY_ROUTE,own);
        h.assertTrue(BlyFollows.follows(p)&&BlyFollows.carry(p)&&p.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS),"near water, the dark closes in and something takes their arm");
        h.runAfterDelay(BlyFollows.HOLD+BlyFollows.CARRY+5,()->{
            try{var edge=h.absoluteVec(new Vec3(7.5,2,2.5));
                h.assertTrue(!BlyFollows.carrying(p)&&p.position().distanceTo(edge)<.6&&!p.isInWater(),"set down at the water's edge, never in it: "+p.position().subtract(edge));
            }finally{var o=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.BLY_ROUTE);o.remove(BlyFollows.FOLLOWS);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.BLY_ROUTE,o);NativeTestPlayers.remove(p);}
            h.succeed();});
    }
    /**
     * 0.4.187: while she follows, she answers what the reader does to animals in their own world. The meat and hide spoil as they
     * fall; a young one costs them the night; in the morning one of their penned animals is gone and the gate stands open. The
     * men read the reader's own record back to them.
     */
    @GameTest(template="empty",batch="literary_elk_follows187",timeoutTicks=300)
    public static void whileSheFollowsSheAnswersWhatTheReaderDoesToAnimals(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"elk_followed");var d=LabyrinthData.get(h.getLevel().getServer());
        try{
            h.assertTrue(ElkFollows.accusation(p).startsWith("Never even shot a deer"),"a reader who has killed nothing leaves the accusation faltering");
            var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.ELK_LOT);own.putBoolean(ElkFollows.FOLLOWS,true);own.putInt(ElkFollows.BASE,0);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.ELK_LOT,own);
            var cow=h.spawn(EntityType.COW,new BlockPos(2,2,2));cow.hurt(h.getLevel().damageSources().playerAttack(p),100);
            var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(h.absolutePos(new BlockPos(2,2,2))).inflate(3));
            h.assertTrue(!cow.isAlive()&&drops.stream().noneMatch(i->i.getItem().is(Items.BEEF)||i.getItem().is(Items.LEATHER))&&drops.stream().anyMatch(i->i.getItem().is(Items.ROTTEN_FLESH)),"the meat and the hide spoil as they fall: "+drops.stream().map(i->i.getItem().toString()).toList());
            h.assertTrue(ElkFollows.kills(p)==1&&ElkFollows.accusation(p).startsWith("One cow, %s"),"the men read the reader's own count back to them: "+ElkFollows.accusation(p));
            h.assertTrue(!ElkFollows.restless(p),"a grown animal does not cost the night");
            var calf=h.spawn(EntityType.COW,new BlockPos(4,2,2));calf.setBaby(true);calf.hurt(h.getLevel().damageSources().playerAttack(p),100);
            h.assertTrue(ElkFollows.restless(p),"a young one costs them the night");
            // A pen near where they sleep: one cow inside a ring of fence with a gate.
            for(int x=6;x<=10;x++)for(int z=6;z<=10;z++)if(x==6||x==10||z==6||z==10)h.setBlock(new BlockPos(x,2,z),x==8&&z==6?Blocks.OAK_FENCE_GATE:Blocks.OAK_FENCE);
            var penned=h.spawn(EntityType.COW,new BlockPos(8,2,8));var named=h.spawn(EntityType.SHEEP,new BlockPos(8,2,9));named.setCustomName(net.minecraft.network.chat.Component.literal("Clover"));
            p.setRespawnPosition(h.getLevel().dimension(),h.absolutePos(new BlockPos(8,2,8)),0,true,false);
            h.assertTrue(ElkFollows.morning(p),"in the morning she takes one of theirs in kind");
            h.assertTrue(!penned.isAlive()&&named.isAlive()&&h.getBlockState(new BlockPos(8,2,6)).getValue(net.minecraft.world.level.block.FenceGateBlock.OPEN),"the penned cow is gone and the gate stands open; the named sheep is never taken");
            h.assertTrue(ElkFollows.morning(p)==false||LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.ELK_LOT).getInt(ElkFollows.IN_KIND)>=0,"never more than there were");
        }finally{var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.ELK_LOT);own.remove(ElkFollows.FOLLOWS);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.ELK_LOT,own);NativeTestPlayers.remove(p);}
        h.succeed();
    }

    @GameTest(template="empty",batch="literary_last_stop_field",timeoutTicks=750)
    public static void theFieldRepeatsUnseenAndCarriesOnlyItsOwnReaderBackToTheirClipping(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_LOT;var p=f.player("field_reader",place);var peer=f.player("field_peer",place);var b=f.base(place);var l=f.out;
        for(int z=-49;z>=-75;z--)for(int x=-34;x<=34;x++)for(int y=-2;y<=8;y++){var here=l.getBlockState(b.offset(x,y,z));var there=l.getBlockState(b.offset(x,y,z-ElkLot.PERIOD));
            if(!here.equals(there)){h.fail("the field repeats exactly: "+x+","+y+","+z+" "+here+" against "+there);return;}}
        var own=f.own(p,place);own.putBoolean("ElkSeen",true);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);var room=place.room();
        f.chunks.hold(l,new AABB(b.getX()+room.minX(),b.getY()-4,b.getZ()+room.minZ(),b.getX()+room.maxX()+1,b.getY()+12,b.getZ()+2));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&ElkLot.loaded(l,b)&&LiteraryVignettes.shared(f.data(),place).hasUUID("Elk"),"wait for native chunks, entity sections and the elk")).thenExecute(()->{f.at(p,place,4.5,0,-97.2);f.at(peer,place,8.5,0,-97.2);}).thenIdle(10).thenExecute(()->{
            h.assertTrue(Math.abs(p.getZ()-(b.getZ()-97.2+ElkLot.PERIOD))<.01&&f.own(p,place).getInt("FieldLoops")==1,"at the field's far end the reader is one repeat back, in an identical place");
            h.assertTrue(f.own(peer,place).getInt("FieldLoops")==0&&f.own(peer,place).getInt("FieldTicks")==0,"a reader who never saw the elk is turned round, and it counts for nothing");
            var world=LiteraryVignettes.shared(f.data(),place);var elk=(LiteraryElk)l.getEntity(world.getUUID("Elk"));
            // 0.4.187: striking her fails the striker's night, so here the peer strikes and the reader goes on.
            elk.hurt(l.damageSources().playerAttack(peer),2);h.assertTrue(f.own(peer,place).getBoolean("ElkWounded")&&!f.own(p,place).getBoolean("ElkWounded"),"the wound is the striker's own, for the fan room");
            var mine=f.own(p,place);mine.putInt("FieldLoops",2);mine.putInt("FieldTicks",400);mine.putInt("Tab",650);mine.putInt("Drinks",2);LiteraryVignettes.save(f.data(),p.getUUID(),place,mine);
            f.at(p,place,4.5,0,-97.2);
            h.startSequence().thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("Ready"),"the original herd must physically reach this reader before the ending")).thenExecute(()->{
                var after=f.own(p,place);h.assertTrue(ElkLotEnding.HERD.equals(after.getString("Outcome"))&&after.getBoolean("CaptureWitness0492"),"the field ending follows actual native contact, not a loop counter");
                h.assertTrue(p.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(ElkLot.PORCH)))<.1,"the reader wakes on the bar's porch");
                h.assertTrue(LiteraryTexts.ending(p,place,after).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().anyMatch(page->page.raw().getString().contains("$6.50")),"the clipping remembers the open tab");
                f.source(p,place);f.read(p,place,ElkLot.BOARD);
                h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.ELK_LOT)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.ELK_LOT),"the clipping on the board resolves its own reader only");
                h.succeed();
            }).thenSucceed();
        });
    }
    @AfterBatch(batch="literary_last_stop_field") public static void lastStopFieldDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_last_stop_saved",timeoutTicks=300)
    public static void aSavedElkLotIsReCarvedOnceKeepingItsElkAndEveryReader(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_LOT;f.build(place);var b=f.base(place);var l=f.out;var d=f.data();var room=place.room();
        var done=d.state(ElkLot.STATE);done.remove(Long.toString(b.asLong()));d.setState(ElkLot.STATE,done);
        var finishedId=UUID.randomUUID();var partwayId=UUID.randomUUID();
        var finished=new CompoundTag();finished.putBoolean("Completed",true);finished.putString("Outcome","escaped_the_looping_field_and_read_own_clipping");LiteraryVignettes.save(d,finishedId,place,finished);
        var partway=new CompoundTag();partway.putBoolean("ElkSeen",true);partway.putBoolean("ElkWounded",true);partway.putInt("FieldLoops",1);LiteraryVignettes.save(d,partwayId,place,partway);
        f.chunks.hold(l,new AABB(b.getX()+room.minX()-1,b.getY()+room.minY()-2,b.getZ()+room.minZ()-1,b.getX()+room.maxX()+2,b.getY()+room.maxY()+2,b.getZ()+room.maxZ()+2));
        final LiteraryElk[] elk={null};final LiteraryActor[] accuser={null};final Entity[] pig={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&ElkLot.loaded(l,b),"wait for native chunks and entity sections")).thenExecute(()->{
            l.setBlock(b.offset(ElkLot.OLD_SOURCE),Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.HAS_BOOK,true),3);
            if(l.getBlockEntity(b.offset(ElkLot.OLD_SOURCE)) instanceof LecternBlockEntity lectern)lectern.setBook(LiteraryTexts.source(place));
            elk[0]=LiteraryRegistry.ELK.get().create(l);elk[0].moveTo(Vec3.atBottomCenterOf(b.offset(5,0,-23)));elk[0].wound();l.addFreshEntity(elk[0]);
            accuser[0]=LiteraryRegistry.ACTOR.get().create(l);accuser[0].bind(place,null);accuser[0].appearance(LiteraryActor.STRANGER,0);accuser[0].moveTo(Vec3.atBottomCenterOf(b.offset(8,0,-28)));l.addFreshEntity(accuser[0]);
            var world=new CompoundTag();world.putUUID("Elk",elk[0].getUUID());world.putUUID("Accuser0",accuser[0].getUUID());LiteraryVignettes.shared(d,place,world);
            pig[0]=EntityType.PIG.create(l);pig[0].moveTo(Vec3.atBottomCenterOf(b.offset(10,0,-30)));l.addFreshEntity(pig[0]);
        }).thenExecute(()->{
            h.assertTrue(!ElkLot.rebuild(l,b)&&!ElkLot.built(f.data(),b),"nothing changes while a living body is in the lot");
            pig[0].discard();var watcher=NativeTestPlayers.survival(h,"lot_rebuild_watcher");f.players.add(watcher);watcher.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+8.5,180,0);
            h.assertTrue(!ElkLot.rebuild(l,b)&&!ElkLot.built(f.data(),b),"nothing changes while someone stands where they could see in");
            watcher.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+160.5,180,0);
            h.assertTrue(ElkLot.rebuild(l,b)&&ElkLot.built(f.data(),b),"empty and unwatched, the lot is re-carved");
            var world=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(world.hasUUID("Elk")&&world.getUUID("Elk").equals(elk[0].getUUID())&&!elk[0].isRemoved()&&elk[0].wounded()&&elk[0].isInvisible()&&!world.hasUUID("Accuser0")&&accuser[0].isRemoved(),"the elk keeps its body, its wound and its UUID, out of sight; the old accusers go");
            h.assertTrue(l.getEntitiesOfClass(ItemEntity.class,new AABB(b.offset(room.minX(),room.minY(),room.minZ()).getCenter(),b.offset(room.maxX(),room.maxY(),room.maxZ()).getCenter())).isEmpty(),"the old lectern's book was not thrown on the floor");
            h.assertTrue(RoadhouseBlock.is(l.getBlockState(b.offset(ElkLot.REGISTER)),RoadhouseBlock.Kind.REGISTER)&&l.getBlockState(b.offset(ElkLot.JUKEBOX)).is(Blocks.JUKEBOX),"the bar stands where the old lot was");
            var keep=LiteraryVignettes.personal(f.data(),finishedId,place);var mid=LiteraryVignettes.personal(f.data(),partwayId,place);
            h.assertTrue(keep.getBoolean("Completed")&&mid.getBoolean("ElkSeen")&&mid.getBoolean("ElkWounded")&&mid.getInt("FieldLoops")==1,"every reader's record is kept as it was");
            f.reload();h.assertTrue(ElkLot.built(f.data(),b)&&ElkLot.rebuild(l,b),"the checkpoint survives reload and never repeats");
        }).thenSucceed();
    }
    @AfterBatch(batch="literary_last_stop_saved") public static void lastStopSavedDone(ServerLevel l){close();}

    // ------------------------------------------------------------------------------------------------ Three Fingers (0.4.81, shared 0.4.147)
    private static CompoundTag pawThread(Fixture f,ServerPlayer p){return VignetteThreads.thread(f.data(),LabyrinthPlace.THREE_FINGERS,p.getUUID());}
    private static CompoundTag pawStory(Fixture f,ServerPlayer p){return pawThread(f,p).getCompound("Story");}
    private static void savePaw(Fixture f,ServerPlayer p,CompoundTag thread){VignetteThreads.saveThread(f.data(),LabyrinthPlace.THREE_FINGERS,p.getUUID(),thread);}
    private static ItemStack pawOf(ServerPlayer p,CompoundTag story){for(var s:p.getInventory().items)if(WitheredPawItem.mine(s,p,story))return s;return ItemStack.EMPTY;}
    private static void wish(ServerPlayer p,int slot){var story=VignetteThreads.thread(LabyrinthData.get(p.server),LabyrinthPlace.THREE_FINGERS,p.getUUID()).getCompound("Story");ThreeFingers.use(p,pawOf(p,story));if(p.containerMenu instanceof LiteraryChoiceMenu menu)menu.clicked(slot,0,ClickType.PICKUP,p);}
    /** A reader on their own: out of whatever thread they walked in with, into one of their own. */
    private static void apart(Fixture f,ServerPlayer p){VignetteThreads.forget(f.data(),LabyrinthPlace.THREE_FINGERS,p.getUUID());VignetteThreads.solo(p,LabyrinthPlace.THREE_FINGERS);}
    private static ListTag saw(ServerPlayer... readers){var list=new ListTag();for(var p:readers)list.add(StringTag.valueOf(p.getUUID().toString()));return list;}
    /** A thread's story set where a test needs it, with the reader holding its working paw. */
    private static void pawState(Fixture f,ServerPlayer p,int stage,int fingers,java.util.function.Consumer<CompoundTag> more){
        var thread=pawThread(f,p);var story=thread.getCompound("Story");
        story.putInt("Stage",stage);story.putInt(ThreeFingers.FINGERS,fingers);story.putInt(ThreeFingers.ISSUE,story.getInt(ThreeFingers.ISSUE)+1);story.putBoolean("SergeantGone",true);story.putInt("Evening",thread.getInt("Visit"));
        more.accept(story);thread.put("Story",story);if(stage>=ThreeFingers.TAKEN)VignetteThreads.hold(thread,p);savePaw(f,p,thread);p.getInventory().add(WitheredPawItem.issue(p,story));
    }
    /** What a reader has been told so far. A test runs while the server holds back each tick's packets, so they are let out first. */
    private static boolean heard(ServerPlayer p,String words){p.connection.resumeFlushing();return NativeTestPlayers.messages(p).stream().anyMatch(m->m.contains(words));}
    private static String witnessPages(Fixture f,ServerPlayer p){var text=new StringBuilder();for(var page:WitnessAccount.book(f.data(),p.getUUID(),p.getGameProfile().getName(),false).get(DataComponents.WRITTEN_BOOK_CONTENT).pages())text.append(page.raw().getString()).append('\n');return text.toString();}

    @GameTest(template="empty",batch="literary_paw_fire",timeoutTicks=600)
    public static void theSoldierTellsTheWholeThreadOneReaderHoldsThePawAndTheOthersSeeItsFingerCurl(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.THREE_FINGERS;var p=f.player("paw_reader",place);var peer=f.player("paw_peer",place);var stranger=f.player("paw_stranger",place);var b=f.base(place);var l=f.in;
        h.assertTrue(CottageBlock.is(l.getBlockState(b.offset(ThreeFingers.POCKETBOOK)),CottageBlock.Kind.POCKETBOOK)&&CottageBlock.is(l.getBlockState(b.offset(ThreeFingers.BIBLE)),CottageBlock.Kind.BIBLE)
            &&l.getBlockState(b.offset(ThreeFingers.FIRE)).is(Blocks.CAMPFIRE)&&l.getBlockState(b.offset(ThreeFingers.FRONT_DOOR)).getBlock() instanceof DoorBlock&&CottageBlock.is(l.getBlockState(b.offset(ThreeFingers.LAMP)),CottageBlock.Kind.LAMP_HEAD),
            "the cottage stands: the pocketbook, the Bible, a real fire, the front door and the lamp on the road");
        var id=VignetteThreads.threadId(f.data(),place,p.getUUID());
        h.assertTrue(id!=null&&id.equals(VignetteThreads.threadId(f.data(),place,peer.getUUID())),"readers who come in together are one thread");
        apart(f,stranger);h.assertTrue(!id.equals(VignetteThreads.threadId(f.data(),place,stranger.getUUID())),"a reader of another thread has their own evening");
        h.startSequence().thenWaitUntil(()->h.assertTrue(pawStory(f,p).hasUUID("CastSergeant")&&pawStory(f,stranger).hasUUID("CastSergeant"),"wait for each thread's people")).thenExecute(()->{
            var sergeant=(LiteraryActor)l.getEntity(pawStory(f,p).getUUID("CastSergeant"));var theirs=(LiteraryActor)l.getEntity(pawStory(f,stranger).getUUID("CastSergeant"));
            h.assertTrue(sergeant!=null&&theirs!=null&&sergeant!=theirs&&sergeant.owner().isEmpty()&&sergeant.phase()==ThreeFingers.SERGEANT,"one soldier for the thread, bound to no single reader");
            h.assertTrue(sergeant.broadcastToPlayer(p)&&sergeant.broadcastToPlayer(peer)&&!sergeant.broadcastToPlayer(stranger)&&theirs.broadcastToPlayer(stranger)&&!theirs.broadcastToPlayer(p),"everyone in the thread sees its soldier, and nobody else does");
            f.at(p,place,-5.5,0,-6.5);for(int i=0;i<3;i++)LiteraryVignettes.talk(p,sergeant);
            h.assertTrue(pawStory(f,p).getInt("Tale")==3&&pawStory(f,stranger).getInt("Tale")==0,"the story goes at the pace of whoever asks, for their whole thread");
            h.assertTrue(heard(peer,"Twenty-one years out there")&&!heard(stranger,"Twenty-one years out there"),"the friend hears the soldier tell it");
        }).thenWaitUntil(()->h.assertTrue(pawStory(f,p).getInt("Stage")==ThreeFingers.IN_FIRE,"wait for the paw to go on the fire")).thenExecute(()->{
            h.assertTrue(heard(peer,"He flings the withered paw onto the coals"),"the friend sees it thrown on the fire");
            p.setHealth(20);f.click(p,place,ThreeFingers.FIRE);var story=pawStory(f,p);var paw=pawOf(p,story);
            h.assertTrue(story.getInt("Stage")==ThreeFingers.TAKEN&&!paw.isEmpty()&&WitheredPawItem.fingers(paw)==3&&p.getHealth()<20&&p.getHealth()>=19,"pulled from the coals: a burn of half a heart and a three-fingered paw");
            h.assertTrue(p.getUUID().equals(VignetteThreads.holder(pawThread(f,p))),"whoever pulls it out holds it");
            f.click(peer,place,ThreeFingers.FIRE);
            h.assertTrue(pawOf(peer,pawStory(f,peer)).isEmpty()&&!WitheredPawItem.owned(paw,peer)&&p.getUUID().equals(VignetteThreads.holder(pawThread(f,p))),"there is one paw, and the friend cannot take it from the fire too");
            ThreeFingers.use(peer,paw.copy());h.assertTrue(!(peer.containerMenu instanceof LiteraryChoiceMenu),"it does not move for anyone else");
            h.assertTrue(VignetteThreads.waiting(f.data(),peer.getUUID(),place)&&!VignetteThreads.waiting(f.data(),p.getUUID(),place),"the first wish waits for its holder: without them the friend is dealt something else");
            ThreeFingers.use(p,paw);
            h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu menu&&!menu.getSlot(2).getItem().isEmpty()&&menu.getSlot(3).getItem().isEmpty()&&!menu.getSlot(6).getItem().isEmpty()&&!menu.getSlot(0).getItem().isEmpty()&&!menu.getSlot(4).getItem().isEmpty()&&!menu.getSlot(8).getItem().isEmpty(),"with no companion lost, the paw offers money, the storm, the larder, Will safe and the fire, not a name");
            p.closeContainer();
            // A real tamed wolf of the friend's dies: the companion a holder may name is one the House saw them lose.
            var world=p.server.overworld();var wolf=EntityType.WOLF.create(world);h.assertTrue(wolf!=null,"a native wolf exists");
            wolf.setTame(true,false);wolf.setOwnerUUID(peer.getUUID());wolf.setCustomName(net.minecraft.network.chat.Component.literal("Moss"));wolf.moveTo(57000,80,57000);world.addFreshEntity(wolf);
            h.assertTrue(wolf.hurt(world.damageSources().genericKill(),1000),"the wolf dies a native death");wolf.discard();
            h.assertTrue("Moss".equals(ThreeFingers.lostCompanion(f.data(),peer.getUUID()))&&ThreeFingers.lostCompanion(f.data(),p.getUUID())==null,"the lost companion is the one the House saw that reader lose");
            long now=l.getGameTime();wish(p,2);story=pawStory(f,p);paw=pawOf(p,story);
            h.assertTrue(story.getInt("Stage")==ThreeFingers.WISHED&&ThreeFingers.MONEY.equals(story.getString("Wish1"))&&story.getInt(ThreeFingers.FINGERS)==2&&WitheredPawItem.fingers(paw)==2&&paw.get(DataComponents.CUSTOM_MODEL_DATA).value()==1,"the first wish takes a finger, on the paw and in the thread's story");
            h.assertTrue(WitheredPawItem.clench(paw,now)==1&&WitheredPawItem.clench(paw,now+WitheredPawItem.CLENCH/2)>0&&WitheredPawItem.clench(paw,now+WitheredPawItem.CLENCH)==0,"the finger curls on the held paw for everyone who can see the hand, then stays down");
            h.assertTrue(heard(p,"It twists in your hand")&&heard(peer,"In paw_reader's hand the paw twists"),"the holder feels it; the friend watches it");
            h.assertTrue(!story.hasUUID("CastSergeant")&&story.getBoolean("SergeantGone"),"the soldier goes once the wish is made");
            var sawWish=story.getList("SawWish",net.minecraft.nbt.Tag.TAG_STRING);h.assertTrue(sawWish.size()==2,"both of the thread saw the wish");
            h.assertTrue(pawStory(f,stranger).getInt("Stage")==ThreeFingers.TELLING&&pawStory(f,stranger).hasUUID("CastSon")&&!heard(stranger,"paw twists"),"the other thread's son is in his chair; nothing of this wish reaches them");
        }).thenSucceed();
    }
    @AfterBatch(batch="literary_paw_fire") public static void pawFireDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_paw_news",timeoutTicks=600)
    public static void theWishComesHomeToTheWholeThreadAndEveryWayOfBeingThereHasItsAccount(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.THREE_FINGERS;var p=f.player("news_reader",place);var peer=f.player("news_peer",place);var absent=f.player("news_absent",place);var away=f.player("news_away",place);
        var id=VignetteThreads.threadId(f.data(),place,p.getUUID());
        h.assertTrue(id!=null&&id.equals(VignetteThreads.threadId(f.data(),place,away.getUUID())),"four readers came in together");
        // Two of them saw the wish; one was away for it.
        pawState(f,p,ThreeFingers.WISHED,2,story->{story.putString("Wish1",ThreeFingers.COMPANION);story.putString("Companion","Moss");story.putInt("WishVisit",1);story.putInt("Beat",1);story.put("SawWish",saw(p,peer,away));});
        // A later evening: everyone leaves, and all but one come back.
        var outside=f.base(place).offset(0,0,64);for(var r:List.of(p,peer,absent,away)){r.teleportTo(f.in,outside.getX()+.5,outside.getY(),outside.getZ()+.5,0,0);}
        for(var r:List.of(p,peer,absent)){var b=f.base(place);r.teleportTo(f.in,b.getX()+.5,b.getY(),b.getZ()-3,180,0);VignetteThreads.arrived(r,place);f.arrive(r,place);}
        h.assertTrue(pawThread(f,p).getInt("Visit")==2,"coming back after the wish is a new visit for the thread");
        // The thread's clock is moved on so the man from the mill says his piece at once.
        var start=pawThread(f,p);var s0=start.getCompound("Story");s0.putInt("T",2000);start.put("Story",s0);savePaw(f,p,start);
        h.startSequence().thenWaitUntil(()->h.assertTrue(pawStory(f,p).getInt("Stage")==ThreeFingers.MOURNING,"wait for the man from the mill to have been")).thenExecute(()->{
            var story=pawStory(f,p);
            h.assertTrue(story.getBoolean("SonGone")&&!story.hasUUID("CastSon")&&!story.hasUUID("CastMessenger"),"the son's chair is empty and the man from the mill has gone");
            ItemStack collar=ItemStack.EMPTY;for(var s:p.getInventory().items)if(s.is(Items.LEAD))collar=s;
            h.assertTrue(!collar.isEmpty()&&collar.get(DataComponents.ITEM_NAME).getString().equals("Moss's collar")&&peer.getInventory().countItem(Items.LEAD)==0,"what was wished for comes to whoever wished it, by its own name");
            h.assertTrue(heard(peer,"I come from Fenwick's")&&heard(absent,"I come from Fenwick's"),"the whole thread there hears the news");
            // A reader new to the cottage comes in with them, partway.
            var late=f.player("news_late",place);
            h.assertTrue(id.equals(VignetteThreads.threadId(f.data(),place,late.getUUID()))&&VignetteThreads.partway(pawThread(f,p),late.getUUID()),"a newcomer joins the thread partway");
            wish(p,4);story=pawStory(f,p);
            h.assertTrue(story.getInt("Stage")==ThreeFingers.KNOCKING&&story.getInt(ThreeFingers.FINGERS)==1,"the second wish, for the son back, takes another finger");
            ThreeFingers.use(p,pawOf(p,story));h.assertTrue(!(p.containerMenu instanceof LiteraryChoiceMenu),"there is no last wish until the knocking has begun");
            f.click(p,place,ThreeFingers.FRONT_DOOR);h.assertTrue(pawStory(f,p).getInt("Stage")==ThreeFingers.KNOCKING,"before the knocking the door is only a door");
            var thread=pawThread(f,p);story=thread.getCompound("Story");story.putInt("KnockT",ThreeFingers.KNOCK_AT);thread.put("Story",story);savePaw(f,p,thread);
            f.click(peer,place,ThreeFingers.FRONT_DOOR);
            h.assertTrue(pawStory(f,p).getInt("Stage")==ThreeFingers.KNOCKING&&heard(peer,"He isn't knocking for you"),"it is not the friend's door to open");
            f.click(p,place,ThreeFingers.FRONT_DOOR);var own=f.own(p,place);story=pawStory(f,p);
            h.assertTrue(own.getBoolean("Ready")&&"opened_the_door_onto_the_empty_road".equals(own.getString("Outcome"))&&story.getInt(ThreeFingers.FINGERS)==0&&WitheredPawItem.fingers(pawOf(p,story))==0&&story.getInt("Stage")==ThreeFingers.DONE,"the holder opens it onto nobody, and the last finger goes down without a wish");
            var tf=WitnessAccount.Story.THREE_FINGERS;
            h.assertTrue(WitnessAccount.role(f.data(),peer.getUUID(),tf)==VignetteRole.WITNESS&&witnessPages(f,peer).contains("news_reader took the paw out of the coals"),"the friend who saw it all keeps a witness's account, naming who held the paw");
            h.assertTrue(WitnessAccount.role(f.data(),absent.getUUID(),tf)==VignetteRole.ABSENT&&witnessPages(f,absent).contains("I was away when news_reader wished"),"the one away for the wish saw only what it brought");
            h.assertTrue(WitnessAccount.role(f.data(),late.getUUID(),tf)==VignetteRole.PARTWAY&&witnessPages(f,late).contains("I came in partway through"),"the newcomer's account is a late joiner's");
            h.assertTrue(!WitnessAccount.has(f.data(),away.getUUID(),tf)&&!WitnessAccount.has(f.data(),p.getUUID(),tf),"nobody away is credited, and the holder still reads their own account");
            f.source(p,place);f.read(p,place,ThreeFingers.BIBLE);
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),tf)&&WitnessAccount.role(f.data(),p.getUUID(),tf)==VignetteRole.ACTOR,"the family Bible resolves the holder as the one who acted");
            var text=new StringBuilder();for(var page:LiteraryTexts.ending(p,place,f.own(p,place)).get(DataComponents.WRITTEN_BOOK_CONTENT).pages())text.append(page.raw().getString());
            h.assertTrue(text.toString().contains("THE ROAD")&&text.toString().contains("Moss's collar"),"the holder's account remembers the road and the collar: "+text);
            // The one who was away comes back to its aftermath.
            var b=f.base(place);away.teleportTo(f.in,b.getX()+.5,b.getY(),b.getZ()-3,180,0);VignetteThreads.arrived(away,place);
            h.assertTrue(WitnessAccount.role(f.data(),away.getUUID(),tf)==VignetteRole.AFTERMATH&&witnessPages(f,away).contains("I was not there at the end"),"the member who missed the ending reads its aftermath");
        }).thenSucceed();
    }
    @AfterBatch(batch="literary_paw_news") public static void pawNewsDone(ServerLevel l){close();}
    @AfterBatch(batch="literary_paw_letter0483") public static void pawLetterDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_paw_letter0483",timeoutTicks=400)
    public static void theMillEnvelopeOpensARealLetterAndNeverPaysTheCompensationTwice(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.THREE_FINGERS;var p=f.player("mill_letter_reader",place);var peer=f.player("mill_letter_peer",place);
        h.assertTrue(CottageBlock.is(f.in.getBlockState(f.base(place).offset(-6,1,-11)),CottageBlock.Kind.CHESSBOARD),"the physical chessmen stand on the table between the two seats");
        pawState(f,p,ThreeFingers.WISHED,2,story->{story.putString("Wish1",ThreeFingers.MONEY);story.putInt("WishVisit",0);story.putInt("Beat",0);});
        var start=pawThread(f,p);var s0=start.getCompound("Story");s0.putInt("T",2000);start.put("Story",s0);savePaw(f,p,start);
        h.startSequence().thenWaitUntil(()->h.assertTrue(pawStory(f,p).getInt("Stage")==ThreeFingers.MOURNING,"the messenger delivers the real compensation"))
        .thenExecute(()->{int slot=-1;for(int i=0;i<9;i++)if(p.getInventory().getItem(i).is(LiteraryRegistry.MILL_LETTER.get()))slot=i;
            h.assertTrue(slot>=0&&p.getInventory().countItem(Items.EMERALD)==7&&p.getInventory().countItem(Items.GOLD_INGOT)==3&&peer.getInventory().countItem(Items.EMERALD)==0,"one readable envelope arrives with the finite exact payment, to the holder alone");
            p.getInventory().selected=slot;var original=p.getMainHandItem();original.getItem().use(f.in,p,InteractionHand.MAIN_HAND);
            h.assertTrue(p.containerMenu instanceof net.minecraft.world.inventory.LecternMenu,"normal item use opens a native private letter view");
            var menu=p.containerMenu;h.assertTrue(menu.clickMenuButton(p,2)&&!menu.clickMenuButton(p,3),"the actual second page can be read and the private display cannot mint another envelope");p.closeContainer();
            MillLetterItem.upgrade(p);ThreeFingers.reader(p,f.base(place),f.own(p,place));ThreeFingers.reader(peer,f.base(place),f.own(peer,place));
            h.assertTrue(p.getMainHandItem()==original&&p.getInventory().countItem(Items.EMERALD)==7&&p.getInventory().countItem(Items.GOLD_INGOT)==3&&peer.getInventory().countItem(LiteraryRegistry.MILL_LETTER.get())==0,"reading preserves the exact original and pays nobody again");
            h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.count(f.data(),peer.getUUID())==0,"the compensation letter is not an ending or anyone's progression");
        }).thenSucceed();
    }
    @GameTest(template="empty",batch="literary_paw_endings",timeoutTicks=300)
    public static void theLastWishTheFireALostPawAndAHolderGoneTooLongEachEndOrResumeTheirThread(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.THREE_FINGERS;var last=f.player("last_wisher",place);var burner=f.player("paw_burner",place);var loser=f.player("paw_loser",place);var early=f.player("early_burner",place);
        for(var r:List.of(burner,loser,early))apart(f,r);
        pawState(f,last,ThreeFingers.KNOCKING,1,story->{story.putString("Wish1",ThreeFingers.MONEY);story.putInt("KnockT",ThreeFingers.HAMMER);story.putBoolean("SonGone",true);});
        pawState(f,burner,ThreeFingers.MOURNING,2,story->{story.putString("Wish1",ThreeFingers.STORM);story.putBoolean("SonGone",true);});
        pawState(f,loser,ThreeFingers.TAKEN,3,story->{});
        pawState(f,early,ThreeFingers.TAKEN,3,story->{});
        h.startSequence().thenIdle(10).thenExecute(()->{
            wish(last,4);var own=f.own(last,place);var story=pawStory(f,last);
            h.assertTrue(own.getBoolean("Ready")&&"made_the_last_wish_before_the_door".equals(own.getString("Outcome"))&&story.getInt(ThreeFingers.FINGERS)==0&&story.getInt("Stage")==ThreeFingers.DONE,"the last wish stops the knocking and ends the thread's story");
            h.assertTrue(ThreeFingers.account(own).startsWith("THE LAST FINGER")&&ThreeFingers.account(own).contains("exact sum"),"its account names the last wish and the mill's money");
            h.assertTrue(burner.getMainHandItem().is(LiteraryRegistry.PAW.get()),"the burner holds their paw");
            f.click(burner,place,ThreeFingers.FIRE);own=f.own(burner,place);
            h.assertTrue(own.getBoolean("Ready")&&"gave_the_paw_back_to_the_fire".equals(own.getString("Outcome"))&&pawOf(burner,pawStory(f,burner)).isEmpty(),"once the first wish has come home, the paw can go back on the fire");
            f.click(early,place,ThreeFingers.FIRE);own=f.own(early,place);
            h.assertTrue(pawStory(f,early).getInt("Stage")==ThreeFingers.TAKEN&&!pawOf(early,pawStory(f,early)).isEmpty()&&!own.getBoolean("Ready"),"before any wish the fire will not take it");
            var old=pawOf(loser,pawStory(f,loser)).copy();loser.getInventory().clearContent();
            f.click(loser,place,ThreeFingers.FIRE);story=pawStory(f,loser);var fresh=pawOf(loser,story);
            h.assertTrue(!fresh.isEmpty()&&WitheredPawItem.fingers(fresh)==3&&!WitheredPawItem.mine(old,loser,story)&&story.getInt("Stage")==ThreeFingers.TAKEN,"a lost paw is in the fire again with the same fingers, and the old one answers no more");
            ThreeFingers.use(loser,old);h.assertTrue(!(loser.containerMenu instanceof LiteraryChoiceMenu),"the old paw opens no wish");
            ThreeFingers.use(loser,fresh);h.assertTrue(loser.containerMenu instanceof LiteraryChoiceMenu,"the paw from the fire still offers the first wish");loser.closeContainer();
            // A holder gone three days: the paw goes back in the fire for whoever comes, and the one they carry is only a dried paw.
            var held=pawOf(early,pawStory(f,early)).copy();var b=f.base(place);early.teleportTo(f.in,b.getX()+.5,b.getY(),b.getZ()+64.5,0,0);
            var thread=pawThread(f,early);thread.putLong("HolderSince",f.in.getServer().overworld().getGameTime()-VignetteThreads.RELEASE-1);savePaw(f,early,thread);
            VignetteThreads.releaseOverdue(f.in.getServer());
            h.assertTrue(VignetteThreads.holder(pawThread(f,early))==null&&!WitheredPawItem.mine(held,early,pawStory(f,early)),"the paw has gone back to the fire");
            f.arrive(early,place);f.click(early,place,ThreeFingers.FIRE);story=pawStory(f,early);
            h.assertTrue(early.getUUID().equals(VignetteThreads.holder(pawThread(f,early)))&&!pawOf(early,story).isEmpty()&&WitheredPawItem.fingers(pawOf(early,story))==3,"whoever of the thread comes takes it out of the fire again");
        }).thenSucceed();
    }
    @AfterBatch(batch="literary_paw_endings") public static void pawEndingsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_movie_recovery",timeoutTicks=1400)
    public static void fourCanoesRowToTheFarPierAndAnEmptyCanoeReturnsToItsSlot(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.MOVIE_NIGHT;var p=f.player("movie_rower",place);var peer=f.player("movie_next",place);f.source(p,place);
        final LakeCanoeEntity[] boat={null};final boolean[] finished={false};final int[] leg={0};
        // West of the screen's raft, up past it, then in to the end of the far pier.
        final double[][] route={{-16,-30},{-16,-50},{.5,-58.2}};
        h.runAfterDelay(10,()->{var world=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(f.out.getEntitiesOfClass(LakeCanoeEntity.class,IndianLakeRooms.bounds(f.base(place),place),c->c.getTags().contains(MovieNight.CANOE)).size()==4,"simultaneous native arrivals moor exactly four canoes at the dock");
            boat[0]=MovieNight.canoe(f.out,world,0);f.at(p,place,-2.5,0,-13);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);peer.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            h.assertTrue(boat[0]!=null&&MovieNight.board(p,boat[0]),"the actual empty-handed reader takes a canoe from the dock");
            f.at(peer,place,-2.5,0,-13);h.assertTrue(!MovieNight.board(peer,boat[0]),"nobody else climbs into a canoe already in use");f.at(peer,place,8,0,-4);});
        h.onEachTick(()->{if(boat[0]==null||finished[0])return;var b=f.base(place);
            if(p.getVehicle()==boat[0]){
                var target=route[Math.min(leg[0],route.length-1)];boolean hold=leg[0]==2&&f.own(p,place).getInt("Flashes")<2;
                Vec3 at=boat[0].position(),goal=new Vec3(b.getX()+target[0],b.getY()-.4,b.getZ()+target[1]),step=goal.subtract(at).multiply(1,0,1);
                if(step.length()<.4){if(leg[0]<2)leg[0]++;}else if(!hold){step=step.normalize().scale(.35);at=at.add(step);}
                boat[0].moveTo(at.x,b.getY()-.4,at.z,180,0);boat[0].setDeltaMovement(Vec3.ZERO);p.setPos(boat[0].getX(),b.getY(),boat[0].getZ());p.setHealth(20);
                var witch=MovieNight.witch(f.out,b);if(witch!=null)p.lookAt(EntityAnchorArgument.Anchor.EYES,witch.getEyePosition());
            }
            if(f.own(p,place).getBoolean("Ready")){finished[0]=true;
                h.assertTrue(f.own(p,place).getDouble("RowDistance")>=30&&f.own(p,place).getInt("Flashes")>=2&&!f.own(peer,place).getBoolean("Ready"),"actual rowing and two native exposures belong only to their passenger");
                h.assertTrue(p.getVehicle()==null&&MovieNight.onPier(p,b),"coming alongside the far pier puts the reader up on its boards");}
        });
        h.runAfterDelay(1200,()->{h.assertTrue(finished[0],"actual canoe completion: distance="+f.own(p,place).getDouble("RowDistance")+", flashes="+f.own(p,place).getInt("Flashes")+", bursts="+f.own(p,place).getInt("Bursts0502"));
            var slot=MovieNight.slot(f.base(place),0);h.assertTrue(boat[0].position().distanceToSqr(slot)<1,"the empty canoe goes back to its own place at the dock for the next reader: "+boat[0].position());
            h.assertTrue(f.own(peer,place).getInt("Bursts0502")==0,"a reader standing on the beach is not out under the bursts");
            var old=boat[0].getUUID();boat[0].discard();
            h.runAfterDelay(15,()->{var world=LiteraryVignettes.shared(f.data(),place);var replacement=MovieNight.canoe(f.out,world,0);
                h.assertTrue(replacement!=null&&!replacement.getUUID().equals(old)&&f.out.getEntitiesOfClass(LakeCanoeEntity.class,IndianLakeRooms.bounds(f.base(place),place),c->c.getTags().contains(MovieNight.CANOE)).size()==4,"an actual discard brings one usable replacement to the same place");
                h.assertTrue(!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.of(place.id()))&&f.own(peer,place).getDouble("RowDistance")==0,"canoe recovery never manufactures personal travel or credit");
                f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id())),"the rower's own account completes the scene");h.succeed();});
        });
    }
    @AfterBatch(batch="literary_movie_recovery") public static void movieRecoveryDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_closure_repair",timeoutTicks=100)
    public static void oldCriticalClosureReopensWithoutErasingTheReadersSealedChoice(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("closure_repair",LabyrinthPlace.END_WORLD_CABIN);var own=f.own(p,LabyrinthPlace.END_WORLD_CABIN);own.putBoolean("ChoiceMade",true);own.putBoolean("Refused",true);own.putString("ClosedRoom",LabyrinthPlace.HOLLOWAY_CAMP.id());LiteraryVignettes.save(f.data(),p.getUUID(),LabyrinthPlace.END_WORLD_CABIN,own);
        var closure=new CompoundTag();closure.putString("Retired",LabyrinthPlace.HOLLOWAY_CAMP.id());f.data().setState(LiteraryCabinChoices.CLOSURE,closure);LiteraryCabinChoices.reconcileClosure(p.server);
        h.assertTrue(f.data().state(LiteraryCabinChoices.CLOSURE).getString("Retired").isEmpty()&&f.own(p,LabyrinthPlace.END_WORLD_CABIN).getBoolean("ChoiceMade")&&f.own(p,LabyrinthPlace.END_WORLD_CABIN).getBoolean("Refused"),"repair reopens the critical route without rewriting the original personal refusal");f.reload();h.assertTrue(f.data().state(LiteraryCabinChoices.CLOSURE).getBoolean("Safety0437"),"the repair checkpoint persists through native SavedData reload");h.succeed();
    }
    @AfterBatch(batch="literary_closure_repair") public static void closureRepairDone(ServerLevel l){close();}

    // ------------------------------------------------------------------
    // 0.4.50: the elk carcasses in two stages.

    private static LiteraryActor elkKiller(Fixture f,ServerPlayer p){var id=LiteraryVignettes.shared(f.data(),LabyrinthPlace.ELK_CARCASSES).hasUUID("Killer_"+p.getUUID())?LiteraryVignettes.shared(f.data(),LabyrinthPlace.ELK_CARCASSES).getUUID("Killer_"+p.getUUID()):null;return id!=null&&f.out.getEntity(id) instanceof LiteraryActor a?a:null;}
    private static void elkStage(Fixture f,ServerPlayer p,int stage){var own=f.own(p,LabyrinthPlace.ELK_CARCASSES);own.putInt("ElkStage",stage);LiteraryVignettes.save(f.data(),p.getUUID(),LabyrinthPlace.ELK_CARCASSES,own);}
    @GameTest(template="empty",batch="literary_elk_porthole",timeoutTicks=250)
    public static void nativePortholeBreakAdmitsARealCrawlingEscapeWithoutHullMiningOrPeerCredit(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("porthole_reader",place);var b=f.base(place);var at=b.offset(-9,1,-3);
        f.at(p,place,-7.5,1,-2.5);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        h.assertTrue(f.out.getBlockState(at).is(LiteraryRegistry.YACHT_PORTHOLE.get())&&p.gameMode.destroyBlock(at)&&YachtGlazingBlock.shattered(f.out.getBlockState(at)),"native survival mining leaves the shattered porthole frame");
        h.assertTrue(!p.gameMode.destroyBlock(at.above())&&f.out.getBlockState(at.above()).is(LiteraryRegistry.YACHT_HULL.get()),"the surrounding hull stays protected");
        var observer=NativeTestPlayers.survival(h,"porthole_camera");f.players.add(observer);observer.teleportTo(f.out,b.getX()+7.5,b.getY()+1,b.getZ()-2.5,0,0);observer.setGameMode(GameType.SPECTATOR);
        var untouched=b.offset(9,1,-3);var attempt=new BlockEvent.BreakEvent(f.out,untouched,f.out.getBlockState(untouched),observer);NeoForge.EVENT_BUS.post(attempt);
        h.assertTrue(attempt.isCanceled()&&!LiteraryVignettes.mayBreak(observer,untouched),"an actual observer cannot use the new mining exception");
        p.setShiftKeyDown(true);
        h.startSequence().thenIdle(7).thenExecute(()->{
            h.assertTrue(p.getForcedPose()==Pose.SWIMMING&&p.getBbHeight()<.7,"crouching at the broken window gives a real crawl body");
            for(int i=0;i<25;i++)p.move(MoverType.SELF,new Vec3(-.12,0,0));
            h.assertTrue(p.getX()<b.getX()-9.8&&f.out.noCollision(p,p.getBoundingBox())&&!ElkCarcassMap.aboard(p.position().subtract(b.getX(),b.getY(),b.getZ())),"the body physically passes through the one-block opening into the lake exterior: relative="+p.position().subtract(b.getX(),b.getY(),b.getZ())+", body="+p.getBoundingBox()+", pose="+p.getPose()+", clear="+f.out.noCollision(p,p.getBoundingBox()));
            f.reload();h.assertTrue(YachtGlazingBlock.shattered(f.out.getBlockState(at))&&f.out.getBlockState(untouched).is(LiteraryRegistry.YACHT_PORTHOLE.get())&&WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.count(f.data(),observer.getUUID())==0,"a reload keeps the shattered frame and the finite untouched windows without granting an ending");h.succeed();
        });
    }
    @AfterBatch(batch="literary_elk_porthole") public static void portholeDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_elk_rail",timeoutTicks=1200)
    public static void privateKillerPhysicallyHopsYachtRailAndRejectsWallsAndMissingLandings(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("rail_reader",place);var b=f.base(place);
        f.at(p,place,3.5,9,6.5);
        // The killer only moves where entities tick. Hold the deck's chunks for the fixture and wait for their entity sections
        // first: in the full suite the server can spend this case's whole clock before the disk worker returns them, and the
        // bare entity-ticking wait has no pacing of its own (run 1360).
        f.chunks.hold(f.out,new AABB(Vec3.atLowerCornerOf(b.offset(-4,8,5)),Vec3.atLowerCornerOf(b.offset(5,13,8))));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&f.out.isPositionEntityTicking(b.offset(-2,9,6))&&f.out.isPositionEntityTicking(b.offset(4,9,6)),"wait for the deck to tick entities")).thenExecute(()->{
        // This corridor and the cleared deck lie outside the room box a rebuild clears, so they are put back after the batch.
        railSaved=new HashMap<>();for(var at:BlockPos.betweenClosed(b.offset(-4,-10,5),b.offset(4,12,7)))railSaved.put(at.immutable(),f.out.getBlockState(at));
        // A deck corridor prevents walking around the railing. Its floor and roof are real blocks.
        for(int x=-4;x<=4;x++)for(int z=5;z<=7;z++){
            f.out.setBlock(b.offset(x,8,z),LiteraryRegistry.YACHT_TEAK.get().defaultBlockState(),3);
            for(int y=9;y<=12;y++)f.out.setBlock(b.offset(x,y,z),z==6?Blocks.AIR.defaultBlockState():LiteraryRegistry.YACHT_HULL.get().defaultBlockState(),3);
            f.out.setBlock(b.offset(x,12,z),LiteraryRegistry.YACHT_HULL.get().defaultBlockState(),3);
        }
        var rail=b.offset(-1,9,6);f.out.setBlock(rail,LiteraryRegistry.YACHT_RAIL.get().defaultBlockState(),3);
        var a=LiteraryRegistry.ACTOR.get().create(f.out);h.assertTrue(a!=null,"the killer has a native body");
        a.appearance(LiteraryActor.KILLER,ElkHunt.RUN);a.bind(place,p.getUUID());a.setNoAi(false);a.moveTo(b.getX()-1.5,b.getY()+9,b.getZ()+6.5);a.setOnGround(true);f.out.addFreshEntity(a);f.extras.add(a);
        var id=a.getUUID();var before=new Vec3[]{a.position()};var peak=new double[]{a.getY()};
        KillerNavigation.request(a,new Vec3(b.getX()+2.5,b.getY()+9,b.getZ()+6.5),1);
        h.onEachTick(()->{peak[0]=Math.max(peak[0],a.getY());double step=a.position().subtract(before[0]).multiply(1,0,1).length();
            h.assertTrue(step<=KillerNavigation.CHASE_STEP+.001,"the hop keeps native horizontal pursuit speed: "+step);before[0]=a.position();});
        h.runAfterDelay(65,()->{
            h.assertTrue(a.getUUID().equals(id)&&a.getX()>rail.getX()+1&&peak[0]>b.getY()+10&&f.out.getBlockState(rail).is(LiteraryRegistry.YACHT_RAIL.get()),"the original killer clears the rail through gravity and collision without removing it: "+a.position()+" peak="+peak[0]);
            KillerNavigation.stop(a);a.moveTo(b.getX()-1.5,b.getY()+9,b.getZ()+6.5);a.setDeltaMovement(Vec3.ZERO);a.setOnGround(true);before[0]=a.position();
            // A hull wall is two blocks tall. Vanilla navigation may jump a one-block step.
            f.out.setBlock(rail,LiteraryRegistry.YACHT_HULL.get().defaultBlockState(),3);f.out.setBlock(rail.above(),LiteraryRegistry.YACHT_HULL.get().defaultBlockState(),3);KillerNavigation.request(a,new Vec3(b.getX()+2.5,b.getY()+9,b.getZ()+6.5),1);
        });
        h.runAfterDelay(100,()->{
            h.assertTrue(a.getX()<rail.getX()&&a.getY()<b.getY()+9.1,"a full hull wall cannot be vaulted: "+a.position());
            KillerNavigation.stop(a);a.moveTo(b.getX()-1.5,b.getY()+9,b.getZ()+6.5);a.setDeltaMovement(Vec3.ZERO);a.setOnGround(true);before[0]=a.position();
            f.out.setBlock(rail,LiteraryRegistry.YACHT_RAIL.get().defaultBlockState(),3);f.out.setBlock(rail.above(),Blocks.AIR.defaultBlockState(),3);
            // Remove the lower authored yacht decks as well: a one-tile recess has a safe
            // vanilla step-down and is not an unsupported landing.
            for(int x=0;x<=2;x++)for(int y=-10;y<=8;y++)f.out.setBlock(b.offset(x,y,6),Blocks.AIR.defaultBlockState(),3);
            KillerNavigation.request(a,new Vec3(b.getX()+3.5,b.getY()+9,b.getZ()+6.5),1);
        });
        h.runAfterDelay(135,()->{h.assertTrue(a.getX()<b.getX()-.05&&!a.getPersistentData().contains("YachtVault0464"),"the original killer does not take the custom hop across an actual unsupported gap: "+a.position());h.succeed();});
        });
    }
    private static Map<BlockPos,net.minecraft.world.level.block.state.BlockState> railSaved;
    @AfterBatch(batch="literary_elk_rail") public static void railDone(ServerLevel l){if(railSaved!=null&&active!=null)railSaved.forEach((at,state)->active.out.setBlock(at,state,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE));railSaved=null;close();}
    @GameTest(template="empty",batch="literary_elk_landing",timeoutTicks=450)
    public static void theOriginalKillerLeavesTheBoatOnlyUnseenAndLurksOnDryGroundAmongRealTrees(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("shore_reader",place);var b=f.base(place);
        h.startSequence().thenWaitUntil(()->h.assertTrue(elkKiller(f,p)!=null,"the original private yacht killer loads before its observed departure is tested")).thenExecute(()->{
            var a=elkKiller(f,p);h.assertTrue(a!=null,"the original private yacht killer exists");var id=a.getUUID();a.moveTo(b.getX()+.5,b.getY()+ElkCarcassMap.MAIN,b.getZ()-3.5);a.setNoGravity(true);
            var shore=ElkCarcassMap.standAt(-32,-100);f.at(p,place,shore.getX()+.5,shore.getY(),shore.getZ()+.5);p.setOnGround(true);p.setYRot(180);
            var camera=NativeTestPlayers.survival(h,"shore_camera");f.players.add(camera);camera.teleportTo(f.out,b.getX()+.5,b.getY()+ElkCarcassMap.MAIN+1,b.getZ()-.5,180,0);camera.setNoGravity(true);camera.setGameMode(GameType.SPECTATOR);camera.lookAt(EntityAnchorArgument.Anchor.EYES,a.getEyePosition());
            var before=a.position();var own=f.own(p,place);
            h.assertTrue(!ElkHunt.land(p,b,a,own)&&a.position().equals(before)&&!own.getBoolean("ElkLanded"),"even a native spectator's camera prevents a visible departure from the boat");
            camera.moveTo(b.getX()+.5,b.getY()+ElkCarcassMap.MAIN+1,b.getZ()-63.5);camera.lookAt(EntityAnchorArgument.Anchor.EYES,a.getEyePosition());
            h.assertTrue(camera.distanceTo(a)>48&&!ElkHunt.land(p,b,a,own)&&a.position().equals(before),"a camera sixty blocks away still protects the visible original boat body");
            camera.moveTo(b.getX()+.5,b.getY()+ElkCarcassMap.MAIN+1,b.getZ()-.5);
            camera.setYRot(0);camera.setXRot(0);
            h.assertTrue(ElkHunt.land(p,b,a,own),"turning the actual camera away permits an unseen shore pursuit");
            var r=a.position().subtract(b.getX(),b.getY(),b.getZ());
            h.assertTrue(a.getUUID().equals(id)&&a.owner().orElseThrow().equals(p.getUUID())&&ElkCarcassMap.wooded(r.x,r.z)&&f.out.getFluidState(a.blockPosition()).isEmpty()&&!f.out.getBlockState(a.blockPosition().below()).getCollisionShape(f.out,a.blockPosition().below()).isEmpty(),"the original body is on real dry wooded terrain, never visibly swimming or replaced");
            h.assertTrue(a.distanceTo(p)>=12&&WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.count(f.data(),camera.getUUID())==0,"shore arrival starts from cover without close-range popping or transferred personal evidence");h.succeed();
        });
    }
    @AfterBatch(batch="literary_elk_landing") public static void shoreLandingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_elk_attack_delay",timeoutTicks=700)
    public static void nativeBoatAttacksKeepABriefSavedDelayAndNeverStrikeAnotherReadersBody(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("attack_reader",place);var peer=f.player("attack_peer",place);
        final long[] last={-1};final int[] swings={0};final boolean[] ready={false};
        h.startSequence().thenIdle(10).thenExecute(()->{
            var own=f.own(p,place);own.putInt("ElkStage",ElkHunt.HUNTED);own.putBoolean("ElkResetPending",false);own.putInt("ElkEmerge",0);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
            p.hasChangedDimension();peer.hasChangedDimension();p.connection.resetPosition();peer.connection.resetPosition();p.setInvulnerable(false);peer.setInvulnerable(false);ready[0]=true;
        // Run 1337: a loaded runner can stage the killer late, so the case waits for the two swings rather than a fixed time;
        // every swing is still held to the saved delay below.
        }).thenWaitUntil(()->h.assertTrue(swings[0]>=2,"two native swings: "+swings[0])).thenExecute(()->{h.assertTrue(swings[0]>=2&&p.getHealth()<p.getMaxHealth()&&peer.getHealth()==peer.getMaxHealth(),"native swings deal actual owner damage with breathing room and leave the other reader unharmed: swings="+swings[0]+", ownerHealth="+p.getHealth()+", peerHealth="+peer.getHealth()+", own="+f.own(p,place));h.succeed();});
        h.onEachTick(()->{if(!ready[0])return;var a=elkKiller(f,p);if(a==null)return;var b=f.base(place);f.at(p,place,.5,0,-8.5);f.at(peer,place,1.5,0,-8.5);a.moveTo(b.getX()+.5,b.getY(),b.getZ()-9.5);a.setDeltaMovement(Vec3.ZERO);a.setNoGravity(true);
            h.assertTrue(f.out.noCollision(p,p.getBoundingBox())&&f.out.noCollision(a,a.getBoundingBox()),"the axe test uses the real clear cabin corridor, outside the companionway stairs");
            long stamp=f.own(p,place).getLong("ElkStrikeAt");if(stamp>0&&stamp!=last[0]){if(last[0]>0)h.assertTrue(stamp-last[0]>=ElkHunt.ATTACK_DELAY,"every physical axe swing observes the saved one-and-a-half-second recovery");last[0]=stamp;swings[0]++;}
        });
    }
    @AfterBatch(batch="literary_elk_attack_delay") public static void attackDelayDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_elk_journey",timeoutTicks=2600)
    public static void elkReaderWakesAboardEscapesHidesUnderTheCarcassesAndLeavesByTheGate(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("elk_reader",place);var b=f.base(place);
        h.assertTrue(f.out.getBlockState(b.offset(0,-1,-3)).is(LiteraryRegistry.YACHT_CARPET.get()),"the reader comes through the door into the yacht's cabin");
        f.source(p,place);
        // The hunt runs once the reader's own killer is loaded; on a loaded runner that can take a while, so every look waits for the one before it.
        h.startSequence().thenWaitUntil(()->h.assertTrue(elkKiller(f,p)!=null&&f.own(p,place).getInt("ElkClock")>0,"the reader's own killer is loaded and the hunt is running: "+f.own(p,place).getInt("ElkClock")))
        .thenExecute(()->{f.at(p,place,.5,0,-2.5);f.look(p,b.offset(ElkCarcassMap.GUESTS.get(0)));})
        .thenWaitUntil(()->h.assertTrue((f.own(p,place).getInt("ElkSeenVisit")&1)!=0,"the first of the dead is seen from the cabin"))
        // Up on the aft deck, where the party was: two more of them, in plain sight.
        .thenExecute(()->{f.at(p,place,3.5,ElkCarcassMap.MAIN,-6.5);f.look(p,b.offset(ElkCarcassMap.GUESTS.get(6)));})
        .thenWaitUntil(()->h.assertTrue((f.own(p,place).getInt("ElkSeenVisit")&1<<6)!=0,"the second is seen on the aft deck"))
        .thenExecute(()->{f.at(p,place,-4.5,ElkCarcassMap.MAIN,-4.5);f.look(p,b.offset(ElkCarcassMap.GUESTS.get(5)));})
        .thenWaitUntil(()->h.assertTrue((f.own(p,place).getInt("ElkSeenVisit")&1<<5)!=0,"the third is seen on the aft deck"))
        .thenExecute(()->{var own=f.own(p,place);h.assertTrue(Integer.bitCount(own.getInt("ElkSeenVisit"))>=3&&own.getInt("ElkStage")==ElkHunt.ABOARD,"three of the dead are actually seen, and he still waits below: "+own);
            var killer=elkKiller(f,p);h.assertTrue(killer!=null&&killer.owner().isPresent()&&killer.owner().get().equals(p.getUUID())&&!killer.isNoAi(),"the reader has their own killer, waiting below");})
        .thenWaitUntil(()->h.assertTrue(f.own(p,place).getInt("ElkStage")==ElkHunt.HUNTED,"he comes for them once they have found the dead"))
        // Over the side, into the lake, away from the hull.
        .thenExecute(()->f.at(p,place,24.5,-1.6,-20.5))
        .thenWaitUntil(()->h.assertTrue(f.own(p,place).getInt("ElkStage")==ElkHunt.IN_WATER,"the water is the way off the yacht"))
        .thenExecute(()->{var killer=elkKiller(f,p);h.assertTrue(killer!=null&&ElkCarcassMap.aboard(killer.position().subtract(b.getX(),b.getY(),b.getZ())),"he does not swim after them");
            var shore=ElkCarcassMap.standAt(-50,-100);f.at(p,place,shore.getX()+.5,shore.getY(),shore.getZ()+.5);p.setOnGround(true);})
        .thenWaitUntil(()->{var own=f.own(p,place);h.assertTrue(own.getInt("ElkStage")==ElkHunt.ASHORE&&own.getBoolean("EscapedYacht"),"ashore in the woods, the second stage begins");})
        // Under the carcasses, in the hollow, facing the gap.
        .thenExecute(()->{f.at(p,place,-22.5,ElkCarcassMap.CAVE_Y,-191.5);f.look(p,b.offset(-22,ElkCarcassMap.CAVE_Y,-186));})
        .thenWaitUntil(()->h.assertTrue(p.getForcedPose()==Pose.SWIMMING&&ElkHunt.hidden(p,b),"the reader is down in the hollow under the hides"));
        final boolean[] done={false};
        h.onEachTick(()->{
            if(done[0])return;var own=f.own(p,place);
            if(own.getInt("ElkStage")==ElkHunt.SEARCH||own.getInt("ElkStage")==ElkHunt.ASHORE&&own.getInt("ElkClock")<10){
                // Keep looking out through the gap.
                if(h.getTick()%10==0)f.look(p,b.offset(-22,ElkCarcassMap.CAVE_Y,-186));
            }
            if(!own.getBoolean("PassedSearch"))return;
            done[0]=true;
            h.assertTrue(own.getBoolean("BootsSeen")&&own.getInt("ElkStage")==ElkHunt.SPENT,"he searched the cave, his boots passed the gap and he left: "+own);
            h.assertTrue(own.getInt("ElkPasses")>=2,"his boots passed the opening twice: "+own.getInt("ElkPasses"));
            f.at(p,place,ElkCarcassMap.ENDING.getX()+.5,ElkCarcassMap.GATE_Y,ElkCarcassMap.ENDING.getZ()+1.5);p.setOnGround(true);
            h.runAfterDelay(15,()->{h.assertTrue(f.own(p,place).getBoolean("Ready"),"the crew's gate holds the last account");
                f.read(p,place,LiteraryRooms.ending(place));
                h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.ELK_CARCASSES),"only the whole personal account resolves the source");
                // 0.4.144: the crew's gate at the valley head is the scene's way on into the halls.
                var gate=f.data().door(place.id()+"/service");
                h.assertTrue(gate!=null&&LabyrinthData.DEALT.equals(gate.destination)&&OnwardDoors.placeOf(gate)==place&&f.out.getBlockState(gate.lower).getBlock() instanceof DoorBlock,"the crew's gate is the scene's actual way on");
                f.data().deal(p.getUUID(),gate,LabyrinthPlace.SEANCE.id(),false);LabyrinthDealer.dealOnward(f.data(),p.getUUID(),gate);
                var to=LabyrinthPlace.byId(f.data().deal(p.getUUID(),gate).place());
                h.assertTrue(to!=null&&(LabyrinthPacing.ordinary(to)||LabyrinthPacing.quiet(to)),"leaving by the gate leads into an ordinary hall: "+to);h.succeed();});
        });
    }
    @AfterBatch(batch="literary_elk_journey") public static void elkJourneyDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_elk_betrayal",timeoutTicks=500)
    public static void elkKillersArePrivateAndMovingUnderThePileGivesTheReaderAway(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("elk_hider",place);var peer=f.player("elk_peer",place);var b=f.base(place);
        h.startSequence().thenWaitUntil(()->h.assertTrue(elkKiller(f,p)!=null&&elkKiller(f,peer)!=null,"both original private killers are loaded")).thenExecute(()->{var mine=elkKiller(f,p);var theirs=elkKiller(f,peer);
            h.assertTrue(mine!=null&&theirs!=null&&!mine.getUUID().equals(theirs.getUUID())&&mine.owner().get().equals(p.getUUID())&&theirs.owner().get().equals(peer.getUUID()),"each reader is hunted by their own killer");
            // This case starts with an UNSEEN arrival under the pile. The opening yacht ticks can already
            // have recorded a sighting; carrying it across the fixture's relocation correctly betrays the reader.
            elkAshore(f,p);var own=f.own(p,place);own.remove("ElkLastSeen");own.putBoolean("ElkBetrayed",false);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);KillerNavigation.stop(mine);
            mine.moveTo(Vec3.atBottomCenterOf(b.offset(ElkCarcassMap.CAVE_ENTRY)));
            f.at(p,place,-22.5,ElkCarcassMap.CAVE_Y,-191.5);f.look(p,b.offset(-22,ElkCarcassMap.CAVE_Y,-186));})
        .thenWaitUntil(()->{var own=f.own(p,place);h.assertTrue(own.getInt("ElkStage")==ElkHunt.SEARCH,"held still under the pile, the search begins: "+own.getInt("ElkStage"));})
        .thenExecute(()->{KillerNavigation.stop(elkKiller(f,p));elkKiller(f,p).moveTo(Vec3.atBottomCenterOf(b.offset(ElkCarcassMap.PEER)));})
        .thenIdle(10).thenExecute(()->f.at(p,place,-23.4,ElkCarcassMap.CAVE_Y,-191.5))
        .thenWaitUntil(()->{var own=f.own(p,place);h.assertTrue(own.getInt("ElkStage")==ElkHunt.ASHORE&&own.getBoolean("ElkFound")&&!own.getBoolean("PassedSearch"),"moving while he is at the pile gives the reader away: "+own);})
        .thenExecute(()->h.assertTrue(f.own(peer,place).getInt("ElkStage")==ElkHunt.ABOARD&&!f.own(peer,place).getBoolean("ElkFound"),"another reader's crossing is untouched"))
        .thenWaitUntil(()->h.assertTrue(p.getHealth()<p.getMaxHealth(),"found under the hides, he reaches in"))
        .thenExecute(()->{var peerKiller=elkKiller(f,peer);h.assertTrue(peer.getHealth()==peer.getMaxHealth()&&peerKiller.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(ElkCarcassMap.KILLER_START)))<4,"the peer's killer still waits below their own yacht deck");}).thenSucceed();
    }
    @AfterBatch(batch="literary_elk_betrayal") public static void elkBetrayalDone(ServerLevel l){close();}
    /** Ashore and landed, with nothing else pending: the plain woods hunt. */
    private static void elkAshore(Fixture f,ServerPlayer p){var own=f.own(p,LabyrinthPlace.ELK_CARCASSES);own.putInt("ElkStage",ElkHunt.ASHORE);own.putBoolean("ElkLanded",true);own.putBoolean("EscapedYacht",true);own.putBoolean("ElkResetPending",false);own.putInt("ElkEmerge",0);own.putLong("ElkGraceUntil",0);LiteraryVignettes.save(f.data(),p.getUUID(),LabyrinthPlace.ELK_CARCASSES,own);}
    @GameTest(template="empty",batch="literary_elk_shove",timeoutTicks=900)
    public static void elkReaderShovesTheKillerOffAndStopsHisRaisedAxeButCannotHurtHim(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("shove_reader",place);var b=f.base(place);
        final boolean[] staged={false};
        // Run 1337: on a loaded runner the shore site, far from the scene's base, was not entity-ticking yet; hold it first.
        f.chunks.hold(f.out,new AABB(b.offset(50,ElkCarcassMap.SITE_TOP,-140)).inflate(24,8,24));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the shore site is resident")).thenExecute(()->{elkAshore(f,p);p.setInvulnerable(false);p.hasChangedDimension();p.connection.resetPosition();staged[0]=true;})
        .thenWaitUntil(()->h.assertTrue(f.own(p,place).getInt("ElkWindup")>0,"face to face, he raises the axe: killer "+(elkKiller(f,p)!=null)+", "+f.own(p,place)))
        .thenExecute(()->{var killer=elkKiller(f,p);float health=killer.getHealth();var before=killer.position();
            h.assertTrue(killer.phase()==ElkHunt.RAISE,"the axe is up before it comes down");
            p.attack(killer);
            var own=f.own(p,place);
            h.assertTrue(own.getInt("ElkWindup")==0&&own.getInt("ElkStagger")>0&&killer.phase()==ElkHunt.STAGGER,"a real blow rocks him back and the raised axe never falls: "+own);
            var push=killer.getDeltaMovement();var away=before.subtract(p.position());
            h.assertTrue(push.x*away.x+push.z*away.z>0&&killer.isAlive()&&killer.getHealth()==health,"he is pushed away from the reader and is not hurt at all");
            long shoved=own.getLong("ElkShovedAt");ElkHunt.shoved(p,killer);
            h.assertTrue(f.own(p,place).getLong("ElkShovedAt")==shoved,"a second blow straight after only nudges him; he cannot be held off for ever");
            staged[0]=false;})
        .thenIdle(ElkHunt.STAGGER_TICKS+10).thenExecute(()->{h.assertTrue(f.own(p,place).getInt("ElkStagger")<=0&&elkKiller(f,p).isAlive(),"he comes on again after the stagger");h.succeed();});
        h.onEachTick(()->{if(!staged[0])return;var a=elkKiller(f,p);if(a==null)return;f.at(p,place,50.5,ElkCarcassMap.SITE_TOP+1,-138.5);p.setYRot(180);a.moveTo(b.getX()+50.5,b.getY()+ElkCarcassMap.SITE_TOP+1,b.getZ()-140,0,0);a.setDeltaMovement(Vec3.ZERO);});
    }
    @AfterBatch(batch="literary_elk_shove") public static void elkShoveDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_elk_drag",timeoutTicks=760)
    public static void elkKillingBlowDragsTheReaderIntoThePileAndTheyTearTheirWayOut(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("drag_reader",place);var b=f.base(place);
        var wake=ElkCarcassMap.WAKE.add(b.getX(),b.getY(),b.getZ());
        // 0.4.173 (run 1338): the drag runs from the ridge to the cave, far from the scene's base; held, so he is still in
        // the level to be asked about when the blackout lifts on a loaded runner.
        f.chunks.hold(f.out,new AABB(b.getX()-40,b.getY()+ElkCarcassMap.CAVE_Y-8,b.getZ()-212,b.getX()+76,b.getY()+ElkCarcassMap.SITE_TOP+10,b.getZ()-116));
        // Past the native three seconds of spawn protection, so the blow can land at all.
        h.startSequence().thenIdle(70).thenExecute(()->{elkAshore(f,p);p.setInvulnerable(false);
            var killer=elkKiller(f,p);f.at(p,place,50.5,ElkCarcassMap.SITE_TOP+1,-138.5);killer.moveTo(b.getX()+50.5,b.getY()+ElkCarcassMap.SITE_TOP+1,b.getZ()-140);
            p.setHealth(2);p.hurt(p.damageSources().mobAttack(killer),10);
            var own=f.own(p,place);
            h.assertTrue(p.isAlive()&&p.getHealth()>0&&own.getInt("ElkStage")==ElkHunt.DRAGGED&&ElkDrag.active(p)&&own.getInt("ElkDrags")==1,"his killing blow does not kill: the drag begins: "+own);
            h.assertTrue(p.position().distanceToSqr(wake)<.5,"the body is already where it will wake, under the pile");})
        .thenIdle(ElkDrag.BLACKOUT+100).thenExecute(()->{var killer=elkKiller(f,p);
            h.assertTrue(killer!=null&&killer.phase()==ElkHunt.DRAG,"he is pulling them along: "+(killer==null?"his body is not in the level":"phase "+killer.phase()));
            var on=ElkCarcassMap.drag(ElkDrag.along(100)+1.6).add(b.getX(),b.getY(),b.getZ());
            h.assertTrue(killer.position().distanceToSqr(on)<4,"he walks the drag's real line, ahead of the reader's head: "+killer.position()+" / "+on);
            var world=LiteraryVignettes.shared(f.data(),place);var camera=world.hasUUID("DragCamera_"+p.getUUID())?f.out.getEntity(world.getUUID("DragCamera_"+p.getUUID())):null;
            h.assertTrue(camera instanceof LiteraryActor a&&a.role()==LiteraryActor.CAMERA&&a.isInvisible()&&a.owner().orElseThrow().equals(p.getUUID()),"the reader's own camera rides the drag");
            h.assertTrue(!f.out.getBlockState(BlockPos.containing(camera.getX(),camera.getEyeY()-.6,camera.getZ())).isAir(),"their eyes are dragged along the ground, not carried at standing height: "+camera.getEyeY());
            float health=p.getHealth();h.assertTrue(!p.hurt(p.damageSources().generic(),5)&&p.getHealth()==health&&p.position().distanceToSqr(wake)<.5,"nothing reaches them during it, and the body stays put");})
        .thenIdle(ElkDrag.DRAGGING-100+ElkDrag.DARK+10).thenExecute(()->{var own=f.own(p,place);long now=f.out.getGameTime();
            h.assertTrue(own.getInt("ElkStage")==ElkHunt.ASHORE&&own.getLong("ElkGraceUntil")>now+ElkDrag.BURIED_GRACE/2&&p.getHealth()>=ElkDrag.WAKE_HEALTH&&!ElkDrag.active(p),"they wake in the pile, hurt but alive, and he keeps away while they are in it: "+own);
            h.assertTrue(own.getInt("ElkBurial")==15&&ElkDrag.buried(p),"the gap and the cell beyond it are packed with their own carcasses");
            h.assertTrue(f.out.getBlockState(b.offset(ElkCarcassMap.BURIAL.get(0))).isAir(),"the shared pile itself is not touched");
            for(int i=0;i<ElkDrag.HITS-1;i++)ElkDrag.hit(p,b.offset(ElkCarcassMap.BURIAL.get(0)),false);
            h.assertTrue((f.own(p,place).getInt("ElkBurial")&1)!=0,"one blow does not tear a carcass loose");
            ElkDrag.hit(p,b.offset(ElkCarcassMap.BURIAL.get(0)),false);
            h.assertTrue((f.own(p,place).getInt("ElkBurial")&1)==0,"a few do");
            h.assertTrue(!ElkDrag.hit(p,b.offset(ElkCarcassMap.BURIAL.get(0)),false),"a torn-out carcass takes no more blows");
            ElkDrag.hit(p,b.offset(ElkCarcassMap.BURIAL.get(2)),true);
            h.assertTrue(f.own(p,place).getInt("ElkBurial")==0b1010,"one carcass the reader's own tool broke out clears its cell; a way through is open");
            f.at(p,place,-22.5,ElkCarcassMap.CAVE_Y,-186.5);})
        .thenIdle(5).thenExecute(()->{h.assertTrue(!ElkDrag.buried(p)&&f.own(p,place).getInt("ElkBurial")==0,"out of the pile, the carcasses only they saw go too");
            h.assertTrue(f.own(p,place).getLong("ElkGraceUntil")>=f.out.getGameTime()+ElkDrag.ESCAPE_GRACE-10,"once out they have a head start before he hunts again: "+f.own(p,place));
            var killer=elkKiller(f,p);h.assertTrue(killer.isAlive()&&killer.phase()!=ElkHunt.RUN&&killer.phase()!=ElkHunt.RAISE,"he is walking away for now, still alive and still theirs");
            // A second killing blow on the same crossing is a death.
            elkAshore(f,p);killer.moveTo(p.getX(),p.getY(),p.getZ()-1.5);p.setHealth(2);p.hurt(p.damageSources().mobAttack(killer),10);
            h.assertTrue(p.isDeadOrDying()&&!ElkDrag.active(p)&&f.own(p,place).getInt("ElkDrags")==1,"he does not drag them twice in one crossing: the second blow kills: "+f.own(p,place));h.succeed();});
    }
    @AfterBatch(batch="literary_elk_drag") public static void elkDragDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_miniature_cards",timeoutTicks=100)
    public static void miniatureCardsTellTheReaderWhatTheyDidInEachRoom(GameTestHelper h){
        try(var f=new Fixture(h)){
            var mini=LabyrinthPlace.MINIATURES;var hill=LabyrinthPlace.HILL_NURSERY;var p=f.player("card_reader",mini);var d=f.data();var b=f.base(mini);
            d.visit(p.getUUID(),hill);d.visit(p.getUUID(),mini);
            var own=LiteraryVignettes.personal(d,p.getUUID(),hill);own.putInt("Visit",2);own.putInt("Present",2400);own.putBoolean("Read_Source",true);own.putBoolean("Ready",true);own.putBoolean("Completed",true);own.putString("Outcome","heard_each_cellar_knock_return");LiteraryVignettes.save(d,p.getUUID(),hill,own);
            String name=p.getGameProfile().getName(),card=MiniatureNotes.card(p,hill,false,true);
            h.assertTrue(card.startsWith(name+", in the Hill House nursery. Came in twice; the last time, stayed about two minutes. The room just before this one."),"the card names the reader, the room, the visits and the stay: "+card);
            h.assertTrue(card.contains("Read what had been left out to read. Knocked, and heard every knock come back from the cellar. Read the last account before leaving."),"it says what they read, what they did and how it ended: "+card);
            h.assertTrue(card.endsWith("No one came in with "+name+"."),"the room crossed alone says so, under the model with the second figure: "+card);
            h.assertTrue(!card.matches("(?s).*\\b(he|she|his|hers|him|they|them|their)\\b.*"),"no pronoun is guessed for the reader: "+card);
            own.putBoolean("Completed",false);own.putBoolean("Ready",false);own.putBoolean("Read_Source",false);LiteraryVignettes.save(d,p.getUUID(),hill,own);
            card=MiniatureNotes.card(p,hill,false,false);
            h.assertTrue(card.contains("Did not read what had been left out. Left before it was finished.")&&!card.contains("No one came in"),"an unfinished room is told as unfinished: "+card);
            h.assertTrue(MiniatureNotes.card(p,LabyrinthPlace.WINCHESTER,true,false).contains("Has not been here."),"a room still ahead is not described as walked");for(var place:LabyrinthPlace.values()){if(place.room()==null)continue;String look=MiniatureNotes.flavour(place);h.assertTrue(!look.isBlank()&&!look.matches("(?s).*\\b(he|she|his|hers|him|they|them|their)\\b.*"),"every model carries a flavour line of its own, with no pronoun: "+place);if(WitnessAccount.Story.of(place.id())!=null)h.assertTrue(MiniatureNotes.ownFlavour(place),"each story room's model is described in its own words: "+place);}
            var state=LiteraryVignettes.personal(d,p.getUUID(),mini);var models=new ListTag();var sample=new CompoundTag();sample.putString("Room",hill.id());sample.putBoolean("Behind",true);models.add(sample);state.put("Models",models);
            h.assertTrue(MiniatureNotes.click(p,b,new BlockPos(-8,1,-18),state)&&(state.getInt("CardsRead")&1)==1,"the left stand answers a touch with its card");
            h.assertTrue(MiniatureNotes.click(p,b,new BlockPos(0,1,-18),state)&&(state.getInt("CardsRead")&2)==0,"an empty board answers without a card");
            h.assertTrue(!MiniatureNotes.click(p,b,new BlockPos(3,1,-18),state),"nothing else in the workshop is a stand");
            state.putInt("ThisVisitTicks",MiniatureNotes.MOTHER_AFTER-5);h.assertTrue(MiniatureNotes.mother(p,b,state)==null,"the mother keeps out of sight for the first twenty seconds");
            state.putInt("ThisVisitTicks",MiniatureNotes.MOTHER_AFTER);var mother=MiniatureNotes.mother(p,b,state);
            h.assertTrue(mother!=null&&mother.getCompound("MiniatureMother").getLongArray("Corners").length==4&&mother.getCompound("MiniatureMother").getUUID("Reader").equals(p.getUUID()),"then the reader's own client is told the four upper corners");
            for(long c:mother.getCompound("MiniatureMother").getLongArray("Corners")){var at=BlockPos.of(c);h.assertTrue(f.level(mini).getBlockState(at).isAir()&&!f.level(mini).getBlockState(at.above()).isAir(),"each corner is open air under the ceiling: "+at.subtract(b));}
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="literary_elk_work",timeoutTicks=1200)
    public static void elkKillerHacksAndHaulsCarcassesWhenNoOneIsInSight(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("work_reader",place);var b=f.base(place);
        final int[] chops={0};final boolean[] watching={false};
        // The killer is moved to two carcasses away from the reader; their chunks are held so he is not unloaded there.
        for(int i:new int[]{0,3})f.chunks.hold(f.out,new AABB(b.offset(ElkCarcassMap.WORK.get(i).feet())).inflate(8));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the work sites' sections are loaded")).thenIdle(10).thenExecute(()->{elkAshore(f,p);var far=ElkCarcassMap.standAt(-50,-100);f.at(p,place,far.getX()+.5,far.getY(),far.getZ()+.5);p.setYRot(90);})
        .thenWaitUntil(()->h.assertTrue(elkKiller(f,p)!=null,"the same original killer is actually seeded before the work fixture moves it"))
        .thenExecute(()->{var killer=elkKiller(f,p);var feet=b.offset(ElkCarcassMap.WORK.get(0).feet());killer.moveTo(Vec3.atBottomCenterOf(feet));
            var own=f.own(p,place);own.putInt("ElkWork",1);own.putInt("ElkWorkTicks",0);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);watching[0]=true;})
        .thenWaitUntil(()->{var killer=elkKiller(f,p);h.assertTrue(killer!=null&&killer.phase()==ElkHunt.HACK&&chops[0]>=2,"with no one in sight he stands over the carcass and hacks at it: chops="+chops[0]+", phase="+(killer==null?"unloaded":killer.phase()));})
        .thenExecute(()->{var killer=elkKiller(f,p);
            var feet=b.offset(ElkCarcassMap.WORK.get(3).feet());killer.moveTo(Vec3.atBottomCenterOf(feet));
            var own=f.own(p,place);own.putInt("ElkWork",4);own.putInt("ElkWorkTicks",0);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);})
        .thenWaitUntil(()->{var killer=elkKiller(f,p);h.assertTrue(killer!=null&&killer.phase()==ElkHunt.HAUL&&f.own(p,place).getInt("ElkWork")==4,"unseen, he takes another by the leg and hauls it off toward the cave: "+(killer==null?"unloaded":killer.phase()+" work="+f.own(p,place).getInt("ElkWork")));})
        .thenExecute(()->{
            h.assertTrue(f.out.getBlockState(b.offset(40,ElkCarcassMap.SITE_TOP+2,-142)).is(LiteraryRegistry.PROP.get()),"the shared carcasses stay where they lie");h.succeed();});
        h.onEachTick(()->{if(!watching[0])return;var a=elkKiller(f,p);if(a!=null&&a.swinging&&a.swingTime==1)chops[0]++;});
    }
    @AfterBatch(batch="literary_elk_work") public static void elkWorkDone(ServerLevel l){close();}


}
