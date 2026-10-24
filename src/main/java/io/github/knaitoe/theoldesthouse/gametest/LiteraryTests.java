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
        Fixture(GameTestHelper h,int originY){this.h=h;oldDay=h.getLevel().getServer().overworld().getDayTime();in=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.INTERIOR);out=HouseTestLevel.get(in.getServer(),HouseDimensions.OUTSIDE);oldHouse=HouseSavedData.get(in.getServer());oldData=LabyrinthData.get(in.getServer());oldMother=MotherCollection.get(in.getServer());oldCopies=LiteraryCopies.get(in.getServer());origin=new BlockPos(57000,originY,57000);var house=new HouseSavedData();house.markSpawned(origin);var store=in.getServer().overworld().getDataStorage();store.set("the_oldest_house",house);store.set("the_oldest_house_labyrinth",new LabyrinthData());store.set("the_oldest_house_mother",new MotherCollection());store.set("the_oldest_house_literary_copies",new LiteraryCopies());data().setBuilt(LabyrinthBuilder.VERSION,origin);active=this;}
        LabyrinthData data(){return LabyrinthData.get(in.getServer());}ServerLevel level(LabyrinthPlace p){return NovelRooms.outside(p)?out:in;}BlockPos base(LabyrinthPlace p){return LabyrinthPlaces.base(origin,p);}
        void build(LabyrinthPlace p){if(built.add(p)){LiteraryRooms.build(level(p),base(p),p);LabyrinthBuilder.registerDoors(data(),p,base(p));}}
        ServerPlayer player(String name,LabyrinthPlace place){build(place);var p=NativeTestPlayers.survival(h,name);players.add(p);arrive(p,place);return p;}
        void arrive(ServerPlayer p,LabyrinthPlace place){build(place);var b=base(place);p.stopRiding();p.teleportTo(level(place),b.getX()+.5,b.getY(),b.getZ()-3,180,0);p.hasChangedDimension();p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);LiteraryVignettes.onArrive(p,place);}
        void at(ServerPlayer p,LabyrinthPlace place,double x,double y,double z){var b=LiteraryVignettes.base(p,place);p.stopRiding();p.moveTo(b.getX()+x,b.getY()+y,b.getZ()+z);p.setDeltaMovement(Vec3.ZERO);}
        void look(ServerPlayer p,BlockPos at){p.lookAt(EntityAnchorArgument.Anchor.EYES,at.getCenter());}
        CompoundTag own(ServerPlayer p,LabyrinthPlace place){return LiteraryVignettes.personal(data(),p.getUUID(),place);}
        void click(ServerPlayer p,LabyrinthPlace place,BlockPos rel){var b=LiteraryVignettes.base(p,place);var at=b.offset(rel);p.moveTo(at.getX()+.5,at.getY(),at.getZ()+2.5);p.setDeltaMovement(Vec3.ZERO);look(p,at);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)));}
        void read(ServerPlayer p,LabyrinthPlace place,BlockPos rel){click(p,place,rel);h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,"actual scene surface opens its native private paper: "+place);var menu=(LiteraryVignettes.Pages)p.containerMenu;var book=menu.original().get(DataComponents.WRITTEN_BOOK_CONTENT);h.assertTrue(menu.clickMenuButton(p,100+book.pages().size()-1),"the actual final page is read");p.closeContainer();}
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
    @GameTest(template="empty",batch="literary_coffin",timeoutTicks=230)
    public static void nativeCoffinChoiceRequiresAReturnAndKeepsTheSameWoman(GameTestHelper h){var f=new Fixture(h);var place=LabyrinthPlace.USHER;var p=f.player("coffin_reader",place);f.source(p,place);h.runAfterDelay(10,()->{var world=LiteraryVignettes.shared(f.data(),place);h.assertTrue(world.hasUUID("Woman"),"the living native body is present before the decision");p.setShiftKeyDown(true);f.click(p,place,LiteraryRooms.COFFIN);p.setShiftKeyDown(false);h.assertTrue(f.own(p,place).getBoolean("LidClosed")&&!f.own(p,place).getBoolean("Ready"),"closing the physical lid grants no ending by itself");f.arrive(p,place);f.at(p,place,2,-3,-29);});h.runAfterDelay(150,()->{var world=LiteraryVignettes.shared(f.data(),place);h.assertTrue(world.getInt("Cracks")==1&&f.own(p,place).getBoolean("Ready"),"the next native visit brings a local crack and an earned consequence");h.assertTrue(f.in.getEntity(world.getUUID("Woman")) instanceof LiteraryActor,"the same woman's native UUID survives the return");f.read(p,place,LiteraryRooms.ending(place));h.succeed();});}
    @AfterBatch(batch="literary_coffin") public static void coffinDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_cylinders",timeoutTicks=750)
    public static void threeOwnedRecordsMustActuallyPlayThroughTheirNativeSongLengths(GameTestHelper h){var f=new Fixture(h);var place=LabyrinthPlace.CRIMSON_HALL;var p=f.player("cylinder_reader",place);f.source(p,place);int cylinder=0;for(var rel:List.of(new BlockPos(-12,0,-35),new BlockPos(12,0,-35),new BlockPos(0,7,-39))){f.click(p,place,rel);Item expected=new Item[]{LiteraryRegistry.CYLINDER_ONE.get(),LiteraryRegistry.CYLINDER_TWO.get(),LiteraryRegistry.CYLINDER_THREE.get()}[cylinder];h.assertTrue(p.getInventory().countItem(expected)==1,"actual cylinder surface "+cylinder+" supplies its finite record");cylinder++;}p.closeContainer();var at=f.base(place).offset(-3,0,-27);f.in.setBlock(at,Blocks.SNOW.defaultBlockState(),3);p.getInventory().selected=8;p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SHOVEL));var event=new BlockEvent.BreakEvent(f.in,at,f.in.getBlockState(at),p);NeoForge.EVENT_BUS.post(event);h.assertTrue(!event.isCanceled(),"the native shovel exception admits snow clearing");var shovel=p.getMainHandItem().copy();p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);var bare=new BlockEvent.BreakEvent(f.in,at,f.in.getBlockState(at),p);NeoForge.EVENT_BUS.post(bare);h.assertTrue(!bare.isCanceled(),"bare hands clear the snow too, since the House hands out no shovel");p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));var stick=new BlockEvent.BreakEvent(f.in,at,f.in.getBlockState(at),p);NeoForge.EVENT_BUS.post(stick);h.assertTrue(stick.isCanceled(),"anything else held does not break the hall");p.setItemInHand(InteractionHand.MAIN_HAND,shovel);f.in.setBlock(at,Blocks.AIR.defaultBlockState(),3);f.at(p,place,2,0,-30);var box=(JukeboxBlockEntity)f.in.getBlockEntity(f.base(place).offset(0,0,-30));Item[] records={LiteraryRegistry.CYLINDER_ONE.get(),LiteraryRegistry.CYLINDER_TWO.get(),LiteraryRegistry.CYLINDER_THREE.get()};
        for(int n=0;n<3;n++){final int index=n;h.runAfterDelay(10+n*210,()->{var disc=p.getInventory().items.stream().filter(s->s.is(records[index])).findFirst().orElseThrow().copy();box.setItem(0,disc);h.assertTrue(box.getSongPlayer().isPlaying(),"the owned cylinder starts native audio");});}
        h.runAfterDelay(25,()->h.assertTrue(f.own(p,place).getInt("HeardCylinders")==0,"a few present ticks cannot complete the first voice"));h.runAfterDelay(635,()->{h.assertTrue(f.own(p,place).getInt("HeardCylinders")==7&&f.own(p,place).getBoolean("Ready"),"all three actual native song lengths and the shovel action resolve the hall");f.read(p,place,LiteraryRooms.ending(place));h.succeed();});}
    @AfterBatch(batch="literary_cylinders") public static void cylindersDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_mapping",timeoutTicks=200)
    public static void theRealLowPassageChoiceAndReturnObserveTheBrothersConsequence(GameTestHelper h){var f=new Fixture(h);var place=LabyrinthPlace.MAPPING_INTERIOR;var p=f.player("mapping_reader",place);f.source(p,place);f.at(p,place,-6,0,-26);f.look(p,f.base(place).offset(-9,1,-26));h.runAfterDelay(10,()->{h.assertTrue(f.own(p,place).getBoolean("SilhouetteSeen"),"the native silhouette must actually be seen");f.at(p,place,0,-5,-42);});h.runAfterDelay(20,()->{h.assertTrue(f.own(p,place).getBoolean("BeyondRoof")&&p.getForcedPose()==Pose.SWIMMING,"the player actually crawls beyond the manufactured roof");f.click(p,place,LiteraryRooms.PILE);h.assertTrue(f.own(p,place).getBoolean("PileChosen")&&p.getInventory().countItem(LiteraryRegistry.PHOTOGRAPH.get())==1,"taking the actual pile creates one owned photograph");f.arrive(p,place);f.at(p,place,3,0,-31);f.look(p,f.base(place).offset(6,1,-32));});h.runAfterDelay(40,()->{h.assertTrue(f.own(p,place).getBoolean("Ready")&&!f.own(p,place).getBoolean("PileBroken"),"returning to see the fading brother resolves this player's kept-things choice");f.read(p,place,LiteraryRooms.ending(place));h.succeed();});}
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
    @GameTest(template="empty",batch="literary_food",timeoutTicks=100)
    public static void frozenFoodStaysCarriedAndTheSameNightMealCannotBeCountedTwice(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.HOLY_RABBIT;var p=f.player("hollow_reader",place);f.at(p,place,1,0,-22);
        var bread=new ItemStack(Items.BREAD,4);p.setItemInHand(InteractionHand.MAIN_HAND,bread);
        var arrival=new net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Start(p,bread,32);NeoForge.EVENT_BUS.post(arrival);
        h.assertTrue(!arrival.isCanceled()&&!LiteraryVignettes.retreatLocked(p),"an ordinary arrival allows food and retreat before any explicit commitment");
        h.runAfterDelay(10,()->{
            var father=f.out.getEntitiesOfClass(LiteraryActor.class,p.getBoundingBox().inflate(6),a->a.role()==LiteraryActor.FATHER&&a.owner().orElse(UUID.randomUUID()).equals(p.getUUID())).stream().findFirst().orElseThrow();
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
    public static void eligibleSourcesAndOneOptionalClosureLeaveTheWitnessReachable(GameTestHelper h){var data=new LabyrinthData();var player=UUID.randomUUID();h.assertTrue(WitnessAccount.Story.values().length==52&&WitnessAccount.REQUIRED==39,"the quota derives from all eligible authored stories");h.assertTrue(WitnessAccount.Story.of("old_cabin")==null,"the recurring witness cabin is guidance, not another source");int sources=0;for(var place:LabyrinthPlace.values())if(LiteraryRooms.isLiterary(place)&&place!=LabyrinthPlace.OLD_CABIN){h.assertTrue(WitnessAccount.Story.of(place.id())!=null&&LiteraryTexts.source(place).has(DataComponents.WRITTEN_BOOK_CONTENT),"every new eligible site has original discovery writing and its own resolution registration");sources++;}var unfinished=UUID.randomUUID();data.visit(unfinished,LabyrinthPlace.HILL_NURSERY);
        h.assertTrue(!LiteraryCabinChoices.canRetire(data,LabyrinthPlace.HILL_NURSERY)&&!LiteraryCabinChoices.canRetire(data,LabyrinthPlace.HOLLOWAY_CAMP),"offline unfinished readers and the finale shield route cannot be retired");
        WitnessAccount.resolve(data,unfinished,WitnessAccount.Story.HILL_NURSERY,"personally_resolved");h.assertTrue(LiteraryCabinChoices.canRetire(data,LabyrinthPlace.HILL_NURSERY),"a completed optional room can still close under the single shared outcome");
        h.assertTrue(sources==32&&WitnessAccount.Story.values().length-1>=WitnessAccount.REQUIRED,"one optional closure leaves fifty-one sources for thirty-nine required resolutions");for(var story:WitnessAccount.Story.values())WitnessAccount.resolve(data,player,story,"resolved");h.assertTrue(WitnessAccount.count(data,player)==52&&WitnessAccount.ready(data,player),"different choices in one story cannot add a second source");h.succeed();}
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
        h.runAfterDelay(60,()->{var after=f.own(p,place);h.assertTrue(after.getInt("Chapter")==chapter+1,"the answer, or the silence, completes the chapter, then he waits for a return");
            h.assertTrue(after.getString("JournalText").contains(ConfessionAnswers.QUESTIONS[chapter])&&after.getString("JournalText").contains(chapter==3?ConfessionAnswers.SILENCE:"My answer to "+chapter),"the journal takes down his question and the reply exactly");
            if(chapter<7){h.assertTrue(p.getInventory().countItem(Items.WRITABLE_BOOK)==1,"the same carried native quill remains through the spoken chapters");
                var idle=new net.neoforged.neoforge.event.ServerChatEvent(p,"hello, anyone",net.minecraft.network.chat.Component.literal("hello"));NeoForge.EVENT_BUS.post(idle);h.assertTrue(!idle.isCanceled(),"between chapters, ordinary chat is the reader's own again");
                f.arrive(p,place);f.at(p,place,1,0,-18);f.look(p,f.base(place).offset(1,1,-22));journalChapter(h,f,p,chapter+1);return;}
            h.assertTrue(p.getInventory().countItem(Items.WRITABLE_BOOK)==0&&after.contains("SignedJournal")&&!after.getBoolean("Ready"),"the actual quill is sealed once, without earning an unread ending");
            var journal=ItemStack.parseOptional(p.registryAccess(),after.getCompound("SignedJournal")).get(DataComponents.WRITTEN_BOOK_CONTENT);String pages=journal==null?"":journal.pages().stream().map(page->page.raw().getString()).collect(java.util.stream.Collectors.joining("\n"));
            h.assertTrue(pages.contains("My answer to 0")&&pages.contains("My answer to 7")&&pages.contains(ConfessionAnswers.SILENCE)&&pages.contains(ConfessionAnswers.QUESTIONS[5]),"the sealed journal is made of his questions and the reader's own answers: "+pages);
            h.assertTrue(ConfessionAnswers.last(after).startsWith("My answer to 7"),"the account will end on the reader's last answer");
            f.arrive(p,place);f.read(p,place,new BlockPos(10,1,-29));h.runAfterDelay(10,()->{h.assertTrue(f.own(p,place).getBoolean("Read_Journal")&&f.own(p,place).getBoolean("Ready"),"personally reading the exact sealed journal resolves the later return");f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.CONFESSION),"the owned final account yields this one source");h.succeed();});});});}
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
            h.assertTrue(l.getBlockState(b.offset(ChildRoom.BED_HEAD.below())).isAir()&&l.getBlockState(b.offset(ChildRoom.GAP)).is(Blocks.BIRCH_PLANKS)&&l.getBlockState(b.offset(ChildRoom.GAP.below())).is(Blocks.LADDER)&&l.getBlockState(b.offset(6,-3,-10)).isAir(),"a child fits under the bed, the way down is a floorboard like the rest, and there is a basement below");
            f.source(p,place);
            // Both readers face the door they came in by; the spectator watches the hall door.
            f.at(p,place,.5,0,-4.5);f.look(p,b.offset(0,1,1));f.at(peer,place,-.5,0,-4.5);f.look(peer,b.offset(0,1,1));
            f.at(observer,place,-8.5,0,-11.5);f.look(observer,b.offset(-11,0,-12));
        });
        h.runAfterDelay(430,()->{var world=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(world.getInt("Exits0474")==3&&l.getBlockState(b.offset(ChildRoom.EXITS.get(0).get(0))).is(Blocks.BIRCH_PLANKS)==false&&!l.getBlockState(b.offset(ChildRoom.EXITS.get(0).get(0))).is(Blocks.GLASS),"after the card, the windows went unseen: "+world.getInt("Exits0474"));
            h.assertTrue(l.getBlockState(b.offset(ChildRoom.EXITS.get(2).get(0))).getBlock() instanceof DoorBlock&&f.own(observer,place).getInt("ExitSeen0474")==0,"a spectator's gaze keeps the hall door, and earns nothing");
            h.assertTrue(l.getBlockState(b.offset(ChildRoom.CEILING_SLOTS.get(0))).getValue(NurseryBlock.STAGE)==NurseryBlock.CEILING&&!NurseryBlock.is(l.getBlockState(b.offset(ChildRoom.TOY_HOMES.get(0))),NurseryBlock.Kind.TEDDY),"the bear has left the floor for the ceiling");
            f.at(observer,place,-8.5,0,-4.5);f.look(observer,b.offset(-8,1,1));
        });
        h.runAfterDelay(700,()->{h.assertTrue(LiteraryVignettes.shared(f.data(),place).getInt("Exits0474")==ChildRoom.ALL&&!(l.getBlockState(b.offset(ChildRoom.EXITS.get(3).get(0))).getBlock() instanceof DoorBlock),"once unwatched the doors went too");
            h.assertTrue(f.own(p,place).getInt("ExitSeen0474")==0,"nothing counts as seen until the reader looks");});
        examine(h,f,p,710);
        h.runAfterDelay(840,()->{h.assertTrue(f.own(p,place).getInt("ExitSeen0474")==ChildRoom.ALL&&f.own(p,place).getBoolean("AllSeen0474")&&f.own(peer,place).getInt("ExitSeen0474")==0,"the reader has seen every exit themselves; the peer, who never read the card, has not");
            h.assertTrue(f.own(p,place).getBoolean("DoorShut0119")&&!f.own(peer,place).getBoolean("DoorShut0119"),"the doorway back to the House is wall to the reader shut in, not to the peer who never read the card");
            h.assertTrue(Integer.bitCount(f.own(p,place).getInt("Parents0119"))>=4&&f.own(peer,place).getInt("Parents0119")==0,"the reader heard a parent's voice as each exit went; the peer heard nothing");
            f.at(p,place,.5,0,-22.5);f.at(peer,place,6.5,-4,-12.5);
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
    @GameTest(template="empty",batch="literary_child_rebuild",timeoutTicks=300)
    public static void anOlderChildRoomIsRebuiltOnceOnlyWhenEmptyAndUnwatched(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.CHILD_ROOM;f.build(place);var b=f.base(place);var l=f.in;var d=f.data();
        // Make it the room as it stood before 0.4.74: no checkpoint, the old lectern, the figure on the ceiling, and old records.
        var done=d.state(ChildRoom.STATE);done.remove(Long.toString(b.asLong()));d.setState(ChildRoom.STATE,done);
        l.setBlock(b.offset(6,1,-20),Blocks.LECTERN.defaultBlockState(),3);
        var finishedId=UUID.randomUUID();var partwayId=UUID.randomUUID();
        var finished=new CompoundTag();finished.putBoolean("Completed",true);finished.putBoolean("Read_Source",true);finished.putString("Outcome","examined_the_lost_exits_and_crawled_below_the_bed");LiteraryVignettes.save(d,finishedId,place,finished);
        var partway=new CompoundTag();partway.putBoolean("Read_Source",true);partway.putBoolean("Ready",true);partway.putInt("ExaminedLostExits",31);partway.putInt("ExitSeen0474",15);LiteraryVignettes.save(d,partwayId,place,partway);
        var r=place.room();f.chunks.hold(l,new AABB(b.getX()+r.minX()-1,b.getY()+r.minY()-2,b.getZ()+r.minZ()-1,b.getX()+r.maxX()+2,b.getY()+r.maxY()+2,b.getZ()+r.maxZ()+2));
        final LiteraryActor[] figure={null};final net.minecraft.world.entity.decoration.ArmorStand[] stand={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&ChildRoom.loaded(l,b),"wait for native chunks and entity sections")).thenExecute(()->{
            figure[0]=LiteraryRegistry.ACTOR.get().create(l);figure[0].moveTo(Vec3.atBottomCenterOf(b.offset(7,4,-16)));figure[0].setNoGravity(true);l.addFreshEntity(figure[0]);
            var world=LiteraryVignettes.shared(d,place);world.putUUID("CeilingToy",figure[0].getUUID());world.putInt("LostExits",31);LiteraryVignettes.shared(d,place,world);
            stand[0]=new net.minecraft.world.entity.decoration.ArmorStand(l,b.getX()+.5,b.getY(),b.getZ()-10.5);l.addFreshEntity(stand[0]);
        }).thenExecute(()->{
            h.assertTrue(!ChildRoom.rebuild(l,b)&&l.getBlockState(b.offset(6,1,-20)).is(Blocks.LECTERN),"nothing changes while a body is in the room");
            stand[0].discard();var watcher=NativeTestPlayers.survival(h,"child_rebuild_watcher");f.players.add(watcher);watcher.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+8.5,180,0);
            h.assertTrue(!ChildRoom.rebuild(l,b)&&l.getBlockState(b.offset(6,1,-20)).is(Blocks.LECTERN),"nothing changes while someone stands where they could see in");
            watcher.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+120.5,180,0);
            h.assertTrue(ChildRoom.rebuild(l,b)&&ChildRoom.built(f.data(),b)&&!l.getBlockState(b.offset(6,1,-20)).is(Blocks.LECTERN)&&figure[0].isRemoved(),"empty and unwatched, the old room and its ceiling figure go");
            h.assertTrue(NurseryBlock.is(l.getBlockState(b.offset(ChildRoom.BED_HEAD)),NurseryBlock.Kind.BED_HEAD)&&NurseryBlock.is(l.getBlockState(b.offset(ChildRoom.ACCOUNT)),NurseryBlock.Kind.ACCOUNT)&&l.getBlockState(b.offset(6,-3,-10)).isAir(),"the new room, crawlspace and basement stand in its place");
            var after=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(after.getInt("Exits0474")==0&&!after.contains("LostExits")&&!after.hasUUID("CeilingToy"),"the room's exits are back, for everyone");
            var keep=LiteraryVignettes.personal(f.data(),finishedId,place);var redo=LiteraryVignettes.personal(f.data(),partwayId,place);
            h.assertTrue(keep.getBoolean("Completed")&&"examined_the_lost_exits_and_crawled_below_the_bed".equals(keep.getString("Outcome")),"a reader who finished keeps everything");
            h.assertTrue(!redo.contains("Ready")&&!redo.contains("ExaminedLostExits")&&!redo.contains("ExitSeen0474")&&redo.getBoolean("Read_Source"),"an unfinished reader starts the room over, the card still read");
            f.reload();h.assertTrue(ChildRoom.built(f.data(),b)&&ChildRoom.rebuild(l,b),"the checkpoint survives reload and never repeats");
        }).thenSucceed();
    }
    @AfterBatch(batch="literary_child_rebuild") public static void childRebuildDone(ServerLevel l){close();}
    // ------------------------------------------------------------------------------------------------ the Last Stop (0.4.78)
    private static List<ItemStack> drinks(ServerPlayer p){var out=new ArrayList<ItemStack>();for(var s:p.getInventory().items){var tag=s.get(DataComponents.CUSTOM_DATA);if(tag!=null&&tag.copyTag().contains(ElkLotNight.DRINK))out.add(s);}return out;}
    private static void order(ServerPlayer p,LiteraryActor marla,int slot){LiteraryVignettes.talk(p,marla);if(p.containerMenu instanceof LiteraryChoiceMenu menu)menu.clicked(slot,0,ClickType.PICKUP,p);}
    @GameTest(template="empty",batch="literary_last_stop_bar",timeoutTicks=400)
    public static void theLastStopServesOnEachReadersOwnTabAndPlaysWhatIsPicked(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_LOT;var p=f.player("tab_reader",place);var peer=f.player("tab_peer",place);var b=f.base(place);var l=f.out;
        h.assertTrue(ElkLot.built(f.data(),b)&&RoadhouseBlock.is(l.getBlockState(b.offset(ElkLot.REGISTER)),RoadhouseBlock.Kind.REGISTER)&&RoadhouseBlock.is(l.getBlockState(b.offset(ElkLot.BOARD)),RoadhouseBlock.Kind.CORKBOARD)
                &&l.getBlockState(b.offset(ElkLot.JUKEBOX)).is(Blocks.JUKEBOX)&&RoadhouseBlock.is(l.getBlockState(b.offset(ElkLot.dent())),RoadhouseBlock.Kind.PU_DOOR),"a fresh lot is the bar: its register, its noticeboard, its jukebox and the pickups outside");
        f.at(p,place,-21.5,0,-22.5);f.at(peer,place,-18.5,0,-20.5);
        h.runAfterDelay(15,()->{
            var world=LiteraryVignettes.shared(f.data(),place);
            for(var who:ElkLotNight.CAST)h.assertTrue(world.hasUUID(who.key())&&l.getEntity(world.getUUID(who.key())) instanceof LiteraryActor a&&a.phase()==who.phase()&&a.position().distanceToSqr(who.home(b))<.1,who.name()+" is in her or his place with their own skin");
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
            h.succeed();
        });
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
            elk.hurt(l.damageSources().playerAttack(p),2);h.assertTrue(f.own(p,place).getBoolean("ElkWounded")&&!f.own(peer,place).getBoolean("ElkWounded"),"the wound is the striker's own, for the fan room");
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

    // ------------------------------------------------------------------------------------------------ Three Fingers (0.4.81)
    private static ItemStack pawOf(ServerPlayer p,CompoundTag own){for(var s:p.getInventory().items)if(WitheredPawItem.mine(s,p,own))return s;return ItemStack.EMPTY;}
    private static void wish(ServerPlayer p,int slot){var paw=pawOf(p,LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.THREE_FINGERS));ThreeFingers.use(p,paw);if(p.containerMenu instanceof LiteraryChoiceMenu menu)menu.clicked(slot,0,ClickType.PICKUP,p);}
    /** A reader's part set where a test needs it, with their own working paw in hand. */
    private static void pawState(Fixture f,ServerPlayer p,int stage,int fingers,java.util.function.Consumer<CompoundTag> more){
        var place=LabyrinthPlace.THREE_FINGERS;var own=f.own(p,place);own.putInt("Stage",stage);own.putInt(ThreeFingers.FINGERS,fingers);own.putInt(ThreeFingers.ISSUE,own.getInt(ThreeFingers.ISSUE)+1);own.putBoolean("SergeantGone",true);
        more.accept(own);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);p.getInventory().add(WitheredPawItem.issue(p,own));
    }
    @GameTest(template="empty",batch="literary_paw_fire",timeoutTicks=600)
    public static void theSoldiersPawIsPulledFromTheFireAndEachReadersFirstWishIsTheirOwn(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.THREE_FINGERS;var p=f.player("paw_reader",place);var peer=f.player("paw_peer",place);var b=f.base(place);var l=f.in;
        h.assertTrue(CottageBlock.is(l.getBlockState(b.offset(ThreeFingers.POCKETBOOK)),CottageBlock.Kind.POCKETBOOK)&&CottageBlock.is(l.getBlockState(b.offset(ThreeFingers.BIBLE)),CottageBlock.Kind.BIBLE)
            &&l.getBlockState(b.offset(ThreeFingers.FIRE)).is(Blocks.CAMPFIRE)&&l.getBlockState(b.offset(ThreeFingers.FRONT_DOOR)).getBlock() instanceof DoorBlock&&CottageBlock.is(l.getBlockState(b.offset(ThreeFingers.LAMP)),CottageBlock.Kind.LAMP_HEAD),
            "the cottage stands: the pocketbook, the Bible, a real fire, the front door and the lamp on the road");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.own(p,place).hasUUID("CastSergeant")&&f.own(peer,place).hasUUID("CastSergeant"),"wait for each reader's own people")).thenExecute(()->{
            var mine=(LiteraryActor)l.getEntity(f.own(p,place).getUUID("CastSergeant"));var theirs=(LiteraryActor)l.getEntity(f.own(peer,place).getUUID("CastSergeant"));
            h.assertTrue(mine!=null&&theirs!=null&&mine!=theirs&&mine.owner().get().equals(p.getUUID())&&theirs.owner().get().equals(peer.getUUID())&&mine.phase()==ThreeFingers.SERGEANT,"each reader has their own soldier, seen by them alone");
            f.at(p,place,-5.5,0,-6.5);for(int i=0;i<3;i++)LiteraryVignettes.talk(p,mine);
            h.assertTrue(f.own(p,place).getInt("Tale")==3&&f.own(peer,place).getInt("Tale")==0,"the soldier's story goes at the pace of the reader asking");
        }).thenWaitUntil(()->h.assertTrue(f.own(p,place).getInt("Stage")==ThreeFingers.IN_FIRE,"wait for the paw to go on the fire")).thenExecute(()->{
            h.assertTrue(f.own(peer,place).getInt("Stage")==ThreeFingers.TELLING,"the peer's soldier is still telling it");
            p.setHealth(20);f.click(p,place,ThreeFingers.FIRE);var own=f.own(p,place);var paw=pawOf(p,own);
            h.assertTrue(own.getInt("Stage")==ThreeFingers.TAKEN&&!paw.isEmpty()&&WitheredPawItem.fingers(paw)==3&&p.getHealth()<20&&p.getHealth()>=19,"pulled from the coals: a burn of half a heart and the reader's own three-fingered paw");
            h.assertTrue(pawOf(peer,f.own(peer,place)).isEmpty()&&!WitheredPawItem.owned(paw,peer),"nobody else has it, and it is not theirs");
            ThreeFingers.use(p,paw);
            h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu menu&&!menu.getSlot(2).getItem().isEmpty()&&menu.getSlot(4).getItem().isEmpty()&&!menu.getSlot(6).getItem().isEmpty(),"with no companion lost, the paw offers money and the storm, not a name");
            p.closeContainer();
            // A real tamed wolf of the peer's dies: the House's own record of it is what the peer's paw may name.
            var world=p.server.overworld();var wolf=EntityType.WOLF.create(world);h.assertTrue(wolf!=null,"a native wolf exists");
            wolf.setTame(true,false);wolf.setOwnerUUID(peer.getUUID());wolf.setCustomName(net.minecraft.network.chat.Component.literal("Moss"));wolf.moveTo(57000,80,57000);world.addFreshEntity(wolf);
            h.assertTrue(wolf.hurt(world.damageSources().genericKill(),1000),"the wolf dies a native death");wolf.discard();
            h.assertTrue("Moss".equals(ThreeFingers.lostCompanion(f.data(),peer.getUUID()))&&ThreeFingers.lostCompanion(f.data(),p.getUUID())==null,"the lost companion is the one the House saw that reader lose");
            wish(p,2);own=f.own(p,place);paw=pawOf(p,own);
            h.assertTrue(own.getInt("Stage")==ThreeFingers.WISHED&&ThreeFingers.MONEY.equals(own.getString("Wish1"))&&own.getInt(ThreeFingers.FINGERS)==2&&WitheredPawItem.fingers(paw)==2&&paw.get(DataComponents.CUSTOM_MODEL_DATA).value()==1,"the first wish takes a finger, on the paw and in the record");
            h.assertTrue(!own.hasUUID("CastSergeant")&&own.getBoolean("SergeantGone"),"the soldier goes once the wish is made");
            h.assertTrue(f.own(peer,place).getInt("Stage")==ThreeFingers.TELLING&&f.own(peer,place).hasUUID("CastSon"),"the peer's son is in his chair; nothing of this reader's wish reaches the peer");
        }).thenSucceed();
    }
    @AfterBatch(batch="literary_paw_fire") public static void pawFireDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_paw_news",timeoutTicks=400)
    public static void theFirstWishComesHomeWrongAndTheDoorOpensOntoNobody(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.THREE_FINGERS;var p=f.player("news_reader",place);var peer=f.player("news_peer",place);
        pawState(f,p,ThreeFingers.WISHED,2,own->{own.putString("Wish1",ThreeFingers.COMPANION);own.putString("Companion","Moss");own.putInt("WishVisit",own.getInt("Visit"));own.putInt("Beat",own.getInt("Visit"));});
        int visit=f.own(p,place).getInt("Visit");
        f.arrive(p,place);
        h.assertTrue(f.own(p,place).getInt("Visit")==visit+1,"coming back after the wish is a new visit");
        // The reader's clock is moved on so the man from the mill says his piece at once.
        var start=f.own(p,place);start.putInt("T",2000);LiteraryVignettes.save(f.data(),p.getUUID(),place,start);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.own(p,place).getInt("Stage")==ThreeFingers.MOURNING,"wait for the man from the mill to have been")).thenExecute(()->{
            var own=f.own(p,place);
            h.assertTrue(own.getBoolean("SonGone")&&!own.hasUUID("CastSon")&&!own.hasUUID("CastMessenger"),"the son's chair is empty and the man from the mill has gone");
            ItemStack collar=ItemStack.EMPTY;for(var s:p.getInventory().items)if(s.is(Items.LEAD))collar=s;
            h.assertTrue(!collar.isEmpty()&&collar.get(DataComponents.ITEM_NAME).getString().equals("Moss's collar"),"the companion comes back only as its collar, by its own name");
            h.assertTrue(f.own(peer,place).getInt("Stage")==ThreeFingers.TELLING&&f.own(peer,place).hasUUID("CastSon"),"the peer's household is whole");
            wish(p,4);own=f.own(p,place);
            h.assertTrue(own.getInt("Stage")==ThreeFingers.KNOCKING&&own.getInt(ThreeFingers.FINGERS)==1,"the second wish, for the son back, takes another finger");
            ThreeFingers.use(p,pawOf(p,own));h.assertTrue(!(p.containerMenu instanceof LiteraryChoiceMenu),"there is no last wish until the knocking has begun");
            f.click(p,place,ThreeFingers.FRONT_DOOR);h.assertTrue(f.own(p,place).getInt("Stage")==ThreeFingers.KNOCKING,"before the knocking the door is only a door");
            own=f.own(p,place);own.putInt("KnockT",ThreeFingers.KNOCK_AT);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
            f.click(p,place,ThreeFingers.FRONT_DOOR);own=f.own(p,place);
            h.assertTrue(own.getBoolean("Ready")&&"opened_the_door_onto_the_empty_road".equals(own.getString("Outcome"))&&own.getInt(ThreeFingers.FINGERS)==0&&WitheredPawItem.fingers(pawOf(p,own))==0,"opened while the knocking goes on: nobody there, and the last finger down without a wish");
            h.assertTrue(f.own(peer,place).getInt("Stage")==ThreeFingers.TELLING&&!f.own(peer,place).contains("KnockT"),"the knocking was never the peer's");
            f.source(p,place);f.read(p,place,ThreeFingers.BIBLE);
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.THREE_FINGERS)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.THREE_FINGERS),"the family Bible resolves its own reader only");
            var text=new StringBuilder();for(var page:LiteraryTexts.ending(p,place,f.own(p,place)).get(DataComponents.WRITTEN_BOOK_CONTENT).pages())text.append(page.raw().getString());
            h.assertTrue(text.toString().contains("THE ROAD")&&text.toString().contains("Moss's collar"),"the account remembers the road and the collar: "+text);
        }).thenSucceed();
    }
    @AfterBatch(batch="literary_paw_news") public static void pawNewsDone(ServerLevel l){close();}
    @AfterBatch(batch="literary_paw_letter0483") public static void pawLetterDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_paw_letter0483",timeoutTicks=400)
    public static void theMillEnvelopeOpensARealLetterAndNeverPaysTheCompensationTwice(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.THREE_FINGERS;var p=f.player("mill_letter_reader",place);var peer=f.player("mill_letter_peer",place);
        h.assertTrue(CottageBlock.is(f.in.getBlockState(f.base(place).offset(-6,1,-11)),CottageBlock.Kind.CHESSBOARD),"the physical chessmen stand on the table between the two seats");
        pawState(f,p,ThreeFingers.WISHED,2,own->{own.putString("Wish1",ThreeFingers.MONEY);own.putInt("WishVisit",own.getInt("Visit"));own.putInt("Beat",own.getInt("Visit"));});f.arrive(p,place);
        var start=f.own(p,place);start.putInt("T",2000);LiteraryVignettes.save(f.data(),p.getUUID(),place,start);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.own(p,place).getInt("Stage")==ThreeFingers.MOURNING,"the messenger delivers the real compensation"))
        .thenExecute(()->{int slot=-1;for(int i=0;i<9;i++)if(p.getInventory().getItem(i).is(LiteraryRegistry.MILL_LETTER.get()))slot=i;
            h.assertTrue(slot>=0&&p.getInventory().countItem(Items.EMERALD)==7&&p.getInventory().countItem(Items.GOLD_INGOT)==3,"one readable envelope arrives with the finite exact payment");
            p.getInventory().selected=slot;var original=p.getMainHandItem();original.getItem().use(f.in,p,InteractionHand.MAIN_HAND);
            h.assertTrue(p.containerMenu instanceof net.minecraft.world.inventory.LecternMenu,"normal item use opens a native private letter view");
            var menu=p.containerMenu;h.assertTrue(menu.clickMenuButton(p,2)&&!menu.clickMenuButton(p,3),"the actual second page can be read and the private display cannot mint another envelope");p.closeContainer();
            MillLetterItem.upgrade(p);ThreeFingers.reader(p,f.base(place),f.own(p,place));
            h.assertTrue(p.getMainHandItem()==original&&p.getInventory().countItem(Items.EMERALD)==7&&p.getInventory().countItem(Items.GOLD_INGOT)==3&&peer.getInventory().countItem(LiteraryRegistry.MILL_LETTER.get())==0,"reading preserves the exact original and pays nobody again");
            h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.count(f.data(),peer.getUUID())==0,"the compensation letter is not an ending or peer progression");
        }).thenSucceed();
    }
    @GameTest(template="empty",batch="literary_paw_endings",timeoutTicks=300)
    public static void theLastWishTheFireAndALostPawEachEndOrResumeTheirOwnReadersPart(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.THREE_FINGERS;var last=f.player("last_wisher",place);var burner=f.player("paw_burner",place);var loser=f.player("paw_loser",place);var early=f.player("early_burner",place);
        pawState(f,last,ThreeFingers.KNOCKING,1,own->{own.putString("Wish1",ThreeFingers.MONEY);own.putInt("KnockT",ThreeFingers.HAMMER);own.putBoolean("SonGone",true);});
        pawState(f,burner,ThreeFingers.MOURNING,2,own->{own.putString("Wish1",ThreeFingers.STORM);own.putBoolean("SonGone",true);});
        pawState(f,loser,ThreeFingers.TAKEN,3,own->{});
        pawState(f,early,ThreeFingers.TAKEN,3,own->{});
        h.startSequence().thenIdle(10).thenExecute(()->{
            wish(last,4);var own=f.own(last,place);
            h.assertTrue(own.getBoolean("Ready")&&"made_the_last_wish_before_the_door".equals(own.getString("Outcome"))&&own.getInt(ThreeFingers.FINGERS)==0&&own.getInt("Stage")==ThreeFingers.DONE,"the last wish stops the knocking and ends the part");
            h.assertTrue(ThreeFingers.account(own).startsWith("THE LAST FINGER")&&ThreeFingers.account(own).contains("exact sum"),"its account names the last wish and the mill's money");
            h.assertTrue(burner.getMainHandItem().is(LiteraryRegistry.PAW.get()),"the burner holds their paw");
            f.click(burner,place,ThreeFingers.FIRE);own=f.own(burner,place);
            h.assertTrue(own.getBoolean("Ready")&&"gave_the_paw_back_to_the_fire".equals(own.getString("Outcome"))&&pawOf(burner,own).isEmpty(),"once the first wish has come home, the paw can go back on the fire");
            f.click(early,place,ThreeFingers.FIRE);own=f.own(early,place);
            h.assertTrue(own.getInt("Stage")==ThreeFingers.TAKEN&&!pawOf(early,own).isEmpty()&&!own.getBoolean("Ready"),"before any wish the fire will not take it");
            own=f.own(loser,place);var old=pawOf(loser,own).copy();loser.getInventory().clearContent();
            f.click(loser,place,ThreeFingers.FIRE);own=f.own(loser,place);var fresh=pawOf(loser,own);
            h.assertTrue(!fresh.isEmpty()&&WitheredPawItem.fingers(fresh)==3&&!WitheredPawItem.mine(old,loser,own)&&own.getInt("Stage")==ThreeFingers.TAKEN,"a lost paw is in the fire again with the same fingers, and the old one answers no more");
            ThreeFingers.use(loser,old);h.assertTrue(!(loser.containerMenu instanceof LiteraryChoiceMenu),"the old paw opens no wish");
            ThreeFingers.use(loser,fresh);h.assertTrue(loser.containerMenu instanceof LiteraryChoiceMenu,"the paw from the fire still offers the first wish");loser.closeContainer();
        }).thenSucceed();
    }
    @AfterBatch(batch="literary_paw_endings") public static void pawEndingsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_crimson_gallery",timeoutTicks=300)
    public static void theCrimsonGalleryStairIsRepairedOnceKeepingWhatAReaderPutThere(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.CRIMSON_HALL;f.build(place);var b=f.base(place);var l=f.in;var d=f.data();
        h.assertTrue(CrimsonGallery.built(d,b)&&l.getBlockState(b.offset(-11,6,-14)).is(Blocks.STONE_BRICK_STAIRS)&&l.getBlockState(b.offset(-11,6,-13)).isAir()&&l.getBlockState(b.offset(-14,7,-16)).isAir(),"a new hall has the whole stair and an opening onto the gallery");
        // The hall as it was built before 0.4.77, with one block a reader put in the railing.
        var done=d.state(CrimsonGallery.STATE);done.remove(Long.toString(b.asLong()));d.setState(CrimsonGallery.STATE,done);
        for(int x=-12;x<=-10;x++){l.setBlock(b.offset(x,6,-13),Blocks.DARK_OAK_PLANKS.defaultBlockState(),3);l.setBlock(b.offset(x,6,-14),Blocks.DARK_OAK_PLANKS.defaultBlockState(),3);l.setBlock(b.offset(x,5,-14),Blocks.AIR.defaultBlockState(),3);}
        for(int z=-17;z<=-15;z++)l.setBlock(b.offset(-14,7,z),Blocks.DARK_OAK_FENCE.defaultBlockState(),3);l.setBlock(b.offset(-14,7,-16),Blocks.COBBLESTONE.defaultBlockState(),3);
        f.chunks.hold(l,new AABB(b.getX()-16,b.getY()+4,b.getZ()-19,b.getX()-7,b.getY()+12,b.getZ()-10));
        final net.minecraft.world.entity.decoration.ArmorStand[] stand={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"wait for native chunks and entity sections")).thenExecute(()->{
            stand[0]=new net.minecraft.world.entity.decoration.ArmorStand(l,b.getX()-10.5,b.getY()+7,b.getZ()-15.5);stand[0].setNoGravity(true);l.addFreshEntity(stand[0]);
        }).thenExecute(()->{
            h.assertTrue(!CrimsonGallery.repair(l,b)&&l.getBlockState(b.offset(-11,6,-14)).is(Blocks.DARK_OAK_PLANKS),"nothing changes while a body stands on the landing");
            stand[0].discard();var watcher=NativeTestPlayers.survival(h,"crimson_watcher");f.players.add(watcher);watcher.teleportTo(l,b.getX()-11.5,b.getY(),b.getZ()+2.5,180,0);
            h.assertTrue(!CrimsonGallery.repair(l,b)&&l.getBlockState(b.offset(-11,6,-14)).is(Blocks.DARK_OAK_PLANKS),"nothing changes while someone could see it happen");
            watcher.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+120.5,180,0);
            h.assertTrue(CrimsonGallery.repair(l,b)&&CrimsonGallery.built(f.data(),b),"unwatched and empty, the stair is repaired");
            for(int x=-12;x<=-10;x++)h.assertTrue(l.getBlockState(b.offset(x,6,-14)).is(Blocks.STONE_BRICK_STAIRS)&&l.getBlockState(b.offset(x,5,-14)).is(Blocks.STONE_BRICKS)&&l.getBlockState(b.offset(x,6,-13)).isAir(),"the last tread has its stone and headroom at x "+x);
            h.assertTrue(l.getBlockState(b.offset(-14,7,-15)).isAir()&&l.getBlockState(b.offset(-14,7,-17)).isAir()&&l.getBlockState(b.offset(-14,7,-16)).is(Blocks.COBBLESTONE),"the railing opens onto the gallery, and the reader's own block stays");
            l.setBlock(b.offset(-11,6,-14),Blocks.DARK_OAK_PLANKS.defaultBlockState(),3);f.reload();
            h.assertTrue(CrimsonGallery.repair(l,b)&&l.getBlockState(b.offset(-11,6,-14)).is(Blocks.DARK_OAK_PLANKS),"the checkpoint survives reload and the repair never runs again");
        }).thenSucceed();
    }
    @AfterBatch(batch="literary_crimson_gallery") public static void crimsonGalleryDone(ServerLevel l){close();}
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
    @GameTest(template="empty",batch="literary_elk_rail",timeoutTicks=400)
    public static void privateKillerPhysicallyHopsYachtRailAndRejectsWallsAndMissingLandings(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("rail_reader",place);var b=f.base(place);
        f.at(p,place,3.5,9,6.5);
        // The killer only moves where entities tick; wait for the deck rather than lose its first ticks to loading.
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.out.isPositionEntityTicking(b.offset(-2,9,6))&&f.out.isPositionEntityTicking(b.offset(4,9,6)),"wait for the deck to tick entities")).thenExecute(()->{
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
    @GameTest(template="empty",batch="literary_elk_attack_delay",timeoutTicks=400)
    public static void nativeBoatAttacksKeepABriefSavedDelayAndNeverStrikeAnotherReadersBody(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("attack_reader",place);var peer=f.player("attack_peer",place);
        final long[] last={-1};final int[] swings={0};final boolean[] ready={false};
        h.startSequence().thenIdle(10).thenExecute(()->{
            var own=f.own(p,place);own.putInt("ElkStage",ElkHunt.HUNTED);own.putBoolean("ElkResetPending",false);own.putInt("ElkEmerge",0);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
            p.hasChangedDimension();peer.hasChangedDimension();p.connection.resetPosition();peer.connection.resetPosition();p.setInvulnerable(false);peer.setInvulnerable(false);ready[0]=true;
        }).thenIdle(ElkHunt.ATTACK_DELAY*2+20).thenExecute(()->{h.assertTrue(swings[0]>=2&&p.getHealth()<p.getMaxHealth()&&peer.getHealth()==peer.getMaxHealth(),"native swings deal actual owner damage with breathing room and leave the other reader unharmed: swings="+swings[0]+", ownerHealth="+p.getHealth()+", peerHealth="+peer.getHealth()+", own="+f.own(p,place));h.succeed();});
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
        f.at(p,place,.5,0,-2.5);f.look(p,b.offset(ElkCarcassMap.GUESTS.get(0)));
        // Up on the aft deck, where the party was: two more of them, in plain sight.
        h.runAfterDelay(10,()->{f.at(p,place,3.5,ElkCarcassMap.MAIN,-6.5);f.look(p,b.offset(ElkCarcassMap.GUESTS.get(6)));});
        h.runAfterDelay(20,()->{f.at(p,place,-4.5,ElkCarcassMap.MAIN,-4.5);f.look(p,b.offset(ElkCarcassMap.GUESTS.get(5)));});
        h.runAfterDelay(30,()->{var own=f.own(p,place);h.assertTrue(Integer.bitCount(own.getInt("ElkSeenVisit"))>=3&&own.getInt("ElkStage")==ElkHunt.ABOARD,"three of the dead are actually seen, and he still waits below: "+own);
            var killer=elkKiller(f,p);h.assertTrue(killer!=null&&killer.owner().isPresent()&&killer.owner().get().equals(p.getUUID())&&!killer.isNoAi(),"the reader has their own killer, waiting below");});
        h.runAfterDelay(225,()->{h.assertTrue(f.own(p,place).getInt("ElkStage")==ElkHunt.HUNTED,"he comes for them once they have found the dead");
            // Over the side, into the lake, away from the hull.
            f.at(p,place,24.5,-1.6,-20.5);});
        h.runAfterDelay(240,()->{h.assertTrue(f.own(p,place).getInt("ElkStage")==ElkHunt.IN_WATER,"the water is the way off the yacht");
            var killer=elkKiller(f,p);h.assertTrue(killer!=null&&ElkCarcassMap.aboard(killer.position().subtract(b.getX(),b.getY(),b.getZ())),"he does not swim after them");
            var shore=ElkCarcassMap.standAt(-50,-100);f.at(p,place,shore.getX()+.5,shore.getY(),shore.getZ()+.5);p.setOnGround(true);});
        h.runAfterDelay(255,()->{var own=f.own(p,place);h.assertTrue(own.getInt("ElkStage")==ElkHunt.ASHORE&&own.getBoolean("EscapedYacht"),"ashore in the woods, the second stage begins");
            // Under the carcasses, in the hollow, facing the gap.
            f.at(p,place,-22.5,ElkCarcassMap.CAVE_Y,-191.5);f.look(p,b.offset(-22,ElkCarcassMap.CAVE_Y,-186));});
        h.runAfterDelay(275,()->h.assertTrue(p.getForcedPose()==Pose.SWIMMING&&ElkHunt.hidden(p,b),"the reader is down in the hollow under the hides"));
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
                var gate=f.data().door(place.id()+"/service");h.assertTrue(gate!=null,"the crew's return gate is registered");
                // A same-dimension native source makes the complete callback observable in this fixture.
                var back=b.offset(12,ElkCarcassMap.GATE_Y,-208);f.out.setBlock(back.below(),Blocks.STONE.defaultBlockState(),3);
                f.data().pushReturn(p.getUUID(),new LabyrinthData.Waypoint(f.out.dimension(),Vec3.atBottomCenterOf(back),0,true));
                VignetteGate.begin(p,place);HouseExperience.arrived(p,place);
                p.moveTo(Vec3.atBottomCenterOf(gate.lower.relative(gate.facing.getOpposite(),2)));LabyrinthDoors.tickPlayer(p,f.origin);
                LabyrinthDoors.use(p,gate);h.assertTrue(f.out.getBlockState(gate.lower).getValue(DoorBlock.OPEN),"the actual completed gate opens");
                p.moveTo(Vec3.atBottomCenterOf(gate.lower.relative(gate.facing,2)));LabyrinthDoors.tickPlayer(p,f.origin);
                h.assertTrue(f.data().returnDepth(p.getUUID())==0&&p.position().distanceToSqr(back.getCenter())<16&&!HouseTransitionEvents.isPending(p),"walking through the crew's gate completes the personal return");h.succeed();});
        });
    }
    @AfterBatch(batch="literary_elk_journey") public static void elkJourneyDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_elk_betrayal",timeoutTicks=500)
    public static void elkKillersArePrivateAndMovingUnderThePileGivesTheReaderAway(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("elk_hider",place);var peer=f.player("elk_peer",place);var b=f.base(place);
        h.runAfterDelay(10,()->{var mine=elkKiller(f,p);var theirs=elkKiller(f,peer);
            h.assertTrue(mine!=null&&theirs!=null&&!mine.getUUID().equals(theirs.getUUID())&&mine.owner().get().equals(p.getUUID())&&theirs.owner().get().equals(peer.getUUID()),"each reader is hunted by their own killer");
            var own=f.own(p,place);own.putInt("ElkStage",ElkHunt.ASHORE);own.putBoolean("ElkLanded",true);own.putBoolean("EscapedYacht",true);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
            mine.moveTo(Vec3.atBottomCenterOf(b.offset(ElkCarcassMap.CAVE_ENTRY)));
            f.at(p,place,-22.5,ElkCarcassMap.CAVE_Y,-191.5);f.look(p,b.offset(-22,ElkCarcassMap.CAVE_Y,-186));});
        h.runAfterDelay(100,()->{var own=f.own(p,place);h.assertTrue(own.getInt("ElkStage")==ElkHunt.SEARCH,"held still under the pile, the search begins: "+own.getInt("ElkStage"));
            elkKiller(f,p).moveTo(Vec3.atBottomCenterOf(b.offset(ElkCarcassMap.PEER)));});
        h.runAfterDelay(110,()->f.at(p,place,-23.4,ElkCarcassMap.CAVE_Y,-191.5));
        h.runAfterDelay(125,()->{var own=f.own(p,place);h.assertTrue(own.getInt("ElkStage")==ElkHunt.ASHORE&&own.getBoolean("ElkFound")&&!own.getBoolean("PassedSearch"),"moving while he is at the pile gives the reader away: "+own);
            h.assertTrue(f.own(peer,place).getInt("ElkStage")==ElkHunt.ABOARD&&!f.own(peer,place).getBoolean("ElkFound"),"another reader's crossing is untouched");});
        h.runAfterDelay(240,()->{h.assertTrue(p.getHealth()<p.getMaxHealth(),"found under the hides, he reaches in");
            var peerKiller=elkKiller(f,peer);h.assertTrue(peer.getHealth()==peer.getMaxHealth()&&peerKiller.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(ElkCarcassMap.KILLER_START)))<4,"the peer's killer still waits below their own yacht deck");h.succeed();});
    }
    @AfterBatch(batch="literary_elk_betrayal") public static void elkBetrayalDone(ServerLevel l){close();}
    /** Ashore and landed, with nothing else pending: the plain woods hunt. */
    private static void elkAshore(Fixture f,ServerPlayer p){var own=f.own(p,LabyrinthPlace.ELK_CARCASSES);own.putInt("ElkStage",ElkHunt.ASHORE);own.putBoolean("ElkLanded",true);own.putBoolean("EscapedYacht",true);own.putBoolean("ElkResetPending",false);own.putInt("ElkEmerge",0);own.putLong("ElkGraceUntil",0);LiteraryVignettes.save(f.data(),p.getUUID(),LabyrinthPlace.ELK_CARCASSES,own);}
    @GameTest(template="empty",batch="literary_elk_shove",timeoutTicks=300)
    public static void elkReaderShovesTheKillerOffAndStopsHisRaisedAxeButCannotHurtHim(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("shove_reader",place);var b=f.base(place);
        final boolean[] staged={false};
        h.startSequence().thenIdle(10).thenExecute(()->{elkAshore(f,p);p.setInvulnerable(false);p.hasChangedDimension();p.connection.resetPosition();staged[0]=true;})
        .thenWaitUntil(()->h.assertTrue(f.own(p,place).getInt("ElkWindup")>0,"face to face, he raises the axe: "+f.own(p,place)))
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
    @GameTest(template="empty",batch="literary_elk_drag",timeoutTicks=700)
    public static void elkKillingBlowDragsTheReaderIntoThePileAndTheyTearTheirWayOut(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("drag_reader",place);var b=f.base(place);
        var wake=ElkCarcassMap.WAKE.add(b.getX(),b.getY(),b.getZ());
        // Past the native three seconds of spawn protection, so the blow can land at all.
        h.startSequence().thenIdle(70).thenExecute(()->{elkAshore(f,p);p.setInvulnerable(false);
            var killer=elkKiller(f,p);f.at(p,place,50.5,ElkCarcassMap.SITE_TOP+1,-138.5);killer.moveTo(b.getX()+50.5,b.getY()+ElkCarcassMap.SITE_TOP+1,b.getZ()-140);
            p.setHealth(2);p.hurt(p.damageSources().mobAttack(killer),10);
            var own=f.own(p,place);
            h.assertTrue(p.isAlive()&&p.getHealth()>0&&own.getInt("ElkStage")==ElkHunt.DRAGGED&&ElkDrag.active(p)&&own.getInt("ElkDrags")==1,"his killing blow does not kill: the drag begins: "+own);
            h.assertTrue(p.position().distanceToSqr(wake)<.5,"the body is already where it will wake, under the pile");})
        .thenIdle(ElkDrag.BLACKOUT+100).thenExecute(()->{var killer=elkKiller(f,p);
            h.assertTrue(killer.phase()==ElkHunt.DRAG,"he is pulling them along");
            var on=ElkCarcassMap.drag(ElkDrag.along(100)+1.6).add(b.getX(),b.getY(),b.getZ());
            h.assertTrue(killer.position().distanceToSqr(on)<4,"he walks the drag's real line, ahead of the reader's head: "+killer.position()+" / "+on);
            var world=LiteraryVignettes.shared(f.data(),place);var camera=world.hasUUID("DragCamera_"+p.getUUID())?f.out.getEntity(world.getUUID("DragCamera_"+p.getUUID())):null;
            h.assertTrue(camera instanceof LiteraryActor a&&a.role()==LiteraryActor.CAMERA&&a.isInvisible()&&a.owner().orElseThrow().equals(p.getUUID()),"the reader's own camera rides the drag");
            float health=p.getHealth();h.assertTrue(!p.hurt(p.damageSources().generic(),5)&&p.getHealth()==health&&p.position().distanceToSqr(wake)<.5,"nothing reaches them during it, and the body stays put");})
        .thenIdle(ElkDrag.DRAGGING-100+ElkDrag.DARK+10).thenExecute(()->{var own=f.own(p,place);long now=f.out.getGameTime();
            h.assertTrue(own.getInt("ElkStage")==ElkHunt.ASHORE&&own.getLong("ElkGraceUntil")>now&&p.getHealth()>=ElkDrag.WAKE_HEALTH&&!ElkDrag.active(p),"they wake in the pile, hurt but alive, and he leaves them a while: "+own);
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
            var killer=elkKiller(f,p);h.assertTrue(killer.isAlive()&&killer.phase()!=ElkHunt.RUN&&killer.phase()!=ElkHunt.RAISE,"he is walking away for now, still alive and still theirs");h.succeed();});
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
            h.assertTrue(MiniatureNotes.card(p,LabyrinthPlace.WINCHESTER,true,false).contains("Has not been here."),"a room still ahead is not described as walked");
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
    @GameTest(template="empty",batch="literary_elk_work",timeoutTicks=400)
    public static void elkKillerHacksAndHaulsCarcassesWhenNoOneIsInSight(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;var p=f.player("work_reader",place);var b=f.base(place);
        final int[] chops={0};final boolean[] watching={false};
        h.startSequence().thenIdle(10).thenExecute(()->{elkAshore(f,p);var far=ElkCarcassMap.standAt(-50,-100);f.at(p,place,far.getX()+.5,far.getY(),far.getZ()+.5);p.setYRot(90);
            var killer=elkKiller(f,p);var feet=b.offset(ElkCarcassMap.WORK.get(0).feet());killer.moveTo(Vec3.atBottomCenterOf(feet));
            var own=f.own(p,place);own.putInt("ElkWork",1);own.putInt("ElkWorkTicks",0);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);watching[0]=true;})
        .thenIdle(80).thenExecute(()->{var killer=elkKiller(f,p);
            h.assertTrue(killer.phase()==ElkHunt.HACK&&chops[0]>=2,"with no one in sight he stands over the carcass and hacks at it: chops="+chops[0]+", phase="+killer.phase());
            var feet=b.offset(ElkCarcassMap.WORK.get(3).feet());killer.moveTo(Vec3.atBottomCenterOf(feet));
            var own=f.own(p,place);own.putInt("ElkWork",4);own.putInt("ElkWorkTicks",0);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);})
        .thenIdle(40).thenExecute(()->{var killer=elkKiller(f,p);
            h.assertTrue(killer.phase()==ElkHunt.HAUL&&f.own(p,place).getInt("ElkWork")==4,"unseen, he takes another by the leg and hauls it off toward the cave");
            h.assertTrue(f.out.getBlockState(b.offset(40,ElkCarcassMap.SITE_TOP+2,-142)).is(LiteraryRegistry.PROP.get()),"the shared carcasses stay where they lie");h.succeed();});
        h.onEachTick(()->{if(!watching[0])return;var a=elkKiller(f,p);if(a!=null&&a.swinging&&a.swingTime==1)chops[0]++;});
    }
    @AfterBatch(batch="literary_elk_work") public static void elkWorkDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="literary_elk_upgrade",timeoutTicks=200)
    public static void elkRebuildKeepsTheSceneKillerPetsAndDroppedThings(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.ELK_CARCASSES;f.build(place);var b=f.base(place);var l=f.out;
        // Keep both the saved body's source and its landing entity section native and loaded.
        // The first reader's view does not reach the killer waiting under the far yacht deck.
        f.chunks.hold(l,new AABB(Vec3.atBottomCenterOf(b.offset(7,0,-43)),Vec3.atBottomCenterOf(b.offset(7,0,-43))).inflate(3));
        f.chunks.hold(l,new AABB(Vec3.atBottomCenterOf(b.offset(ElkCarcassMap.KILLER_START)),Vec3.atBottomCenterOf(b.offset(ElkCarcassMap.KILLER_START))).inflate(3));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the original and landing entity sections are ready before staging the saved residents")).thenExecute(()->{
        h.assertTrue(ElkUpgrade.rebuilds(32)&&ElkUpgrade.rebuilds(35)&&!ElkUpgrade.rebuilds(31)&&!ElkUpgrade.rebuilds(36),"only a saved layout with the earlier elk scene carves it again");
        // The earlier scene's shared killer, a pet left sitting, and a dropped original, as a saved world would hold them.
        var legacy=LiteraryRegistry.ACTOR.get().create(l);legacy.bind(place,null);legacy.appearance(LiteraryActor.KILLER,0);legacy.moveTo(Vec3.atBottomCenterOf(b.offset(7,0,-43)));l.addFreshEntity(legacy);f.extras.add(legacy);
        var world=LiteraryVignettes.shared(f.data(),place);world.putUUID("Killer",legacy.getUUID());LiteraryVignettes.shared(f.data(),place,world);
        var wolf=EntityType.WOLF.create(l);wolf.moveTo(Vec3.atBottomCenterOf(b.offset(30,ElkCarcassMap.surface(30,-150)-2,-150)));wolf.setTame(true,true);wolf.setOwnerUUID(UUID.randomUUID());wolf.setOrderedToSit(true);l.addFreshEntity(wolf);f.extras.add(wolf);
        var item=new net.minecraft.world.entity.item.ItemEntity(l,b.getX()+20.5,b.getY()-4,b.getZ()-20.5,new ItemStack(Items.WRITTEN_BOOK));item.setNoGravity(true);l.addFreshEntity(item);f.extras.add(item);
        var ids=List.of(legacy.getUUID(),wolf.getUUID(),item.getUUID());
        var visitor=NativeTestPlayers.survival(h,"elk_visitor");f.players.add(visitor);visitor.teleportTo(l,b.getX()+.5,b.getY()+4,b.getZ()-60.5,0,0);
        h.assertTrue(!ElkUpgrade.vacant(l,b,true),"the old scene is not taken down around anyone in it");
        visitor.teleportTo(f.in,b.getX()+.5,200,b.getZ()+.5,0,0);
        h.assertTrue(ElkUpgrade.vacant(l,b,true),"empty, it may be carved again");
        wolf.setHealth(11);var owner=wolf.getOwnerUUID();var itemData=new CompoundTag();item.saveWithoutId(itemData);itemData.putShort("Age",(short)37);item.load(itemData);
        ElkUpgrade.protectResidents(l,b);var savedPet=new CompoundTag();wolf.saveWithoutId(savedPet);wolf.load(savedPet);
        h.assertTrue(wolf.isNoAi()&&wolf.isInvulnerable()&&wolf.isNoGravity()&&!wolf.hurt(wolf.damageSources().inWall(),4)&&wolf.getHealth()==11,"the actual pet cannot suffocate or fall during bounded carving, including reload");
        ElkUpgrade.settle(l,b);
        item.saveWithoutId(itemData);h.assertTrue(wolf.getHealth()==11&&owner.equals(wolf.getOwnerUUID())&&!wolf.isNoAi()&&!wolf.isInvulnerable()&&!wolf.isNoGravity()&&itemData.getShort("Age")==37&&item.isNoGravity()&&!item.isInvulnerable(),"settling restores health, ownership, physics, native AI and the original finite item age");
        h.assertTrue(legacy.isAlive()&&legacy.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(ElkCarcassMap.KILLER_START)))<1,"the scene's own killer keeps his identity and waits below decks");
        h.assertTrue(wolf.isAlive()&&wolf.isOrderedToSit()&&l.noCollision(wolf,wolf.getBoundingBox())&&!wolf.isInWater(),"a buried pet is set down on the new ground with its orders");
        h.assertTrue(item.isAlive()&&!ElkUpgrade.stranded(l,item),"a dropped original is kept, set on dry ground");
        for(var id:ids)h.assertTrue(l.getEntity(id)!=null,"identity kept: "+id);
        var reader=f.player("elk_adopter",place);
        h.succeedWhen(()->{var shared=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(shared.hasUUID("Killer_"+reader.getUUID())&&shared.getUUID("Killer_"+reader.getUUID()).equals(legacy.getUUID())&&!shared.hasUUID("Killer"),"the next reader inherits the scene's killer rather than a new one");
            h.assertTrue(legacy.owner().isPresent()&&legacy.owner().get().equals(reader.getUUID())&&!legacy.isNoAi(),"he is now that reader's alone");});
        });
    }
    @AfterBatch(batch="literary_elk_upgrade") public static void elkUpgradeDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_scene_upgrade",timeoutTicks=200)
    public static void cabinAndTrailerRebuildsKeepNativeResidentsAndFiniteDroppedOriginals(GameTestHelper h){
        var f=new Fixture(h);f.build(LabyrinthPlace.END_WORLD_CABIN);
        var animals=new java.util.ArrayList<net.minecraft.world.entity.animal.Wolf>();var originals=new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        var scenes=List.of(LabyrinthPlace.END_WORLD_CABIN,LabyrinthPlace.GOATMAN);
        for(var place:scenes){var r=place.room();var b=f.base(place);f.chunks.hold(f.level(place),new AABB(b.getX()+r.minX()-1,b.getY()+r.minY()-2,b.getZ()+r.minZ()-1,b.getX()+r.maxX()+2,b.getY()+r.maxY()+2,b.getZ()+r.maxZ()+2));}
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native source and landing entity sections are ready before the rebuild starts")).thenExecute(()->{
        for(var place:scenes){var l=f.level(place);var b=f.base(place);f.built.add(place);
            for(int x=-4;x<=3;x++)for(int z=place==LabyrinthPlace.GOATMAN?-50:-13;z<=(place==LabyrinthPlace.GOATMAN?-45:-9);z++)l.setBlock(b.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);
            var wolf=new net.minecraft.world.entity.animal.Wolf(EntityType.WOLF,l);wolf.setTame(true,true);wolf.setOwnerUUID(java.util.UUID.randomUUID());wolf.setOrderedToSit(true);wolf.setHealth(7);wolf.moveTo(Vec3.atBottomCenterOf(b.offset(0,0,-12)));l.addFreshEntity(wolf);f.extras.add(wolf);animals.add(wolf);
            var item=new net.minecraft.world.entity.item.ItemEntity(l,b.getX()+1.5,b.getY(),b.getZ()-11.5,new ItemStack(Items.PAPER,3));item.setNoGravity(true);l.addFreshEntity(item);f.extras.add(item);originals.add(item);
            ElkUpgrade.protectResidents(l,b,place);var saved=new CompoundTag();wolf.saveWithoutId(saved);wolf.load(saved);
        }
        h.runAfterDelay(15,()->{for(int i=0;i<scenes.size();i++){var place=scenes.get(i);var l=f.level(place);var b=f.base(place);var wolf=animals.get(i);var item=originals.get(i);var wolfId=wolf.getUUID();var owner=wolf.getOwnerUUID();var itemId=item.getUUID();
            h.assertTrue(wolf.isNoAi()&&wolf.isInvulnerable()&&wolf.getHealth()==7&&item.getItem().getCount()==3,"a sliced scene rebuild protects its wounded native resident and finite original across entity reload");
            l.setBlock(wolf.blockPosition(),Blocks.STONE.defaultBlockState(),3);l.setBlock(item.blockPosition(),Blocks.STONE.defaultBlockState(),3);ElkUpgrade.settle(l,b,place);
            h.assertTrue(l.getEntity(wolfId)==wolf&&l.getEntity(itemId)==item&&wolf.getOwnerUUID().equals(owner)&&wolf.isOrderedToSit()&&wolf.getHealth()==7,"settling preserves exact bodies, ownership, orders and health: "+place+", wolf="+(l.getEntity(wolfId)==wolf)+", item="+(l.getEntity(itemId)==item)+", owner="+wolf.getOwnerUUID()+", sit="+wolf.isOrderedToSit()+", health="+wolf.getHealth());
            h.assertTrue(!wolf.isNoAi()&&!wolf.isInvulnerable()&&!wolf.isNoGravity()&&item.isNoGravity()&&item.getItem().getCount()==3&&!ElkUpgrade.stranded(l,wolf)&&!ElkUpgrade.stranded(l,item),"the original native physics return on safe ground without replenishing the item");
        }h.succeed();});
        });
    }
    @AfterBatch(batch="literary_scene_upgrade") public static void sceneUpgradeDone(ServerLevel l){close();}

}
