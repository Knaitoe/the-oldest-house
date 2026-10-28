package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Explicit category fixtures exercise only the authorized read side; they do not assign a player's traits. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_tailoring") @PrefixGameTestTemplate(false)
public final class PersonalityTailoringTests {
    private static LiteraryTests.Fixture active;
    private static void close(){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="tailoring_read") public static void readDone(ServerLevel l){close();}
    @AfterBatch(batch="tailoring_card") public static void cardDone(ServerLevel l){close();}
    @AfterBatch(batch="tailoring_words") public static void wordsDone(ServerLevel l){close();}
    @AfterBatch(batch="tailoring_props") public static void propsDone(ServerLevel l){close();}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){active=new LiteraryTests.Fixture(h);return active;}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);p.setNoGravity(true);return p;}
    private static void profile(ServerPlayer p,HouseRead.Trait trait,String value,String strength){var d=LabyrinthData.get(p.server);var own=d.stateEntry(HouseRead.STATE,p.getUUID().toString());var read=new CompoundTag();read.putString("Value",value);read.putString("Strength",strength);own.put(trait.name(),read);d.setStateEntry(HouseRead.STATE,p.getUUID().toString(),own);}
    private static void at(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.stopRiding();p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,180,0);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();p.hasChangedDimension();}
    private static List<String> pages(ItemStack book){return book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(p->p.raw().getString()).toList();}
    private static LiteraryActor literary(LiteraryTests.Fixture f,LabyrinthPlace place,int role,int phase,UUID owner,BlockPos at){var a=LiteraryRegistry.ACTOR.get().create(f.level(place));a.appearance(role,phase);a.bind(place,owner);a.setNoGravity(true);a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5);f.level(place).addFreshEntity(a);f.extras.add(a);return a;}
    private static HotelActor hotel(LiteraryTests.Fixture f,int role,BlockPos at){var a=HotelRegistry.ACTOR.get().create(f.in);a.appearance(role,false);a.setNoGravity(true);a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5);f.in.addFreshEntity(a);f.extras.add(a);return a;}

    @GameTest(template="empty",batch="tailoring_read",timeoutTicks=200)
    public static void unknownUncertainTentativeAndUnseenNeverGuessAnAddressOrRewriteTheOriginalCard(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"read_strength");var original=LiteraryTexts.source(LabyrinthPlace.CHILD_ROOM);var state=f.data().state(HouseRead.STATE);
        h.assertTrue(HouseRead.hair(p,false).isEmpty()&&VignetteTailoring.childCard(p,original)==original&&HouseRead.address(p,"you","man","maam","friend").equals("you"),"missing reads leave the exact native fallback and ordinary address");
        h.assertTrue(f.data().state(HouseRead.STATE).equals(state),"the read side creates no inferred observation or category");
        for(String hair:List.of("WRAPPED","BRAIDED_AND_OILED","BRUSHED_DRY","WASHED_AND_BARBERED"))for(var strength:HouseRead.Strength.values()){
            profile(p,HouseRead.Trait.HAIR,hair,strength.name());boolean known=strength==HouseRead.Strength.TENTATIVE||strength==HouseRead.Strength.SETTLED;
            h.assertTrue(HouseRead.hair(p,false).isPresent()==known,"small props accept only a tentative or settled valid read");
            var card=VignetteTailoring.childCard(p,original);h.assertTrue((card==original)==(strength!=HouseRead.Strength.SETTLED),"the larger child memory requires a settled read");
            if(strength==HouseRead.Strength.SETTLED){var a=pages(original);var b=pages(card);h.assertTrue(b.size()==a.size()+1&&b.getFirst().equals(a.getFirst())&&b.getLast().equals(a.getLast()),"all original supplied card words surround one authored memory");}
        }
        for(String self:List.of("MASC","FEM","ANDROGYNOUS","UNSEEN"))for(var strength:HouseRead.Strength.values()){
            profile(p,HouseRead.Trait.SELF,self,strength.name());String wanted=strength!=HouseRead.Strength.SETTLED||self.equals("UNSEEN")?"you":self.equals("MASC")?"man":self.equals("FEM")?"maam":"friend";
            var before=f.data().state(HouseRead.STATE);h.assertTrue(HouseRead.address(p,"you","man","maam","friend").equals(wanted),"spoken address has a neutral fallback for "+self+"/"+strength);h.assertTrue(f.data().state(HouseRead.STATE).equals(before),"reading an address never writes its own evidence");
        }
        profile(p,HouseRead.Trait.HAIR,"NOT_A_READ","SETTLED");profile(p,HouseRead.Trait.SELF,"FEM","bad");h.assertTrue(HouseRead.hair(p,false).isEmpty()&&VignetteTailoring.usual(p).equals("Your usual."),"malformed saved values remain neutral");h.succeed();
    }

    @GameTest(template="empty",batch="tailoring_card",timeoutTicks=700)
    public static void actualBedtimeCardsSnapshotTheirFirstOriginalAcrossProfileChangesReloadAndFiniteTake(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.CHILD_ROOM;f.chunks.hold(f.in,IndianLakeRooms.bounds(f.base(place),place));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the actual child's source surface is resident")).thenExecute(()->{
            f.build(place);int i=0;var variants=new HashSet<String>();
            for(String hair:List.of("WRAPPED","BRAIDED_AND_OILED","BRUSHED_DRY","WASHED_AND_BARBERED")){
                var p=player(h,f,"card_"+(i++));profile(p,HouseRead.Trait.HAIR,hair,"SETTLED");f.arrive(p,place);f.click(p,place,LiteraryRooms.source(place));
                h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,"the real desk opens the native reader");var book=((LiteraryVignettes.Pages)p.containerMenu).original();var content=book.get(DataComponents.WRITTEN_BOOK_CONTENT);variants.add(pages(book).get(1));
                h.assertTrue(content.pages().size()==3&&content.author().equals("Dad")&&ChildRoom.readCard(f.own(p,place))&&!f.own(p,place).getBoolean("Read_Source"),"opening the original still starts closing; the inserted memory is not a new reading gate");
                var saved=ItemStack.parseOptional(p.registryAccess(),(CompoundTag)book.save(p.registryAccess()));h.assertTrue(ItemStack.matches(book,saved),"every full original survives native item codecs");p.closeContainer();
                profile(p,HouseRead.Trait.HAIR,"BRUSHED_DRY","SETTLED");f.reload();f.click(p,place,LiteraryRooms.source(place));var menu=(LiteraryVignettes.Pages)p.containerMenu;
                h.assertTrue(ItemStack.matches(book,menu.original()),"a later read and native SavedData reload never reauthor an original");h.assertTrue(menu.clickMenuButton(p,102)&&menu.clickMenuButton(p,3)&&!menu.clickMenuButton(p,3),"the unchanged native final-page and Take controls transfer one finite original");p.closeContainer();
            }
            h.assertTrue(variants.size()==4,"four hair reads have distinct authored memories");
            var neutral=player(h,f,"card_neutral");f.arrive(neutral,place);f.click(neutral,place,LiteraryRooms.source(place));var original=((LiteraryVignettes.Pages)neutral.containerMenu).original();h.assertTrue(pages(original).equals(pages(LiteraryTexts.source(place))),"an unread player opens the exact original prose");neutral.closeContainer();profile(neutral,HouseRead.Trait.HAIR,"WRAPPED","SETTLED");f.click(neutral,place,LiteraryRooms.source(place));h.assertTrue(ItemStack.matches(original,((LiteraryVignettes.Pages)neutral.containerMenu).original()),"even an original neutral card stays exact after later observations");neutral.closeContainer();h.succeed();
        });
    }

    @GameTest(template="empty",batch="tailoring_words",timeoutTicks=700)
    public static void nativeLastStopStanleyAndBarryRepliesStayPrivateAndKeepTheirOriginalCountersAndMechanics(GameTestHelper h){
        var f=fixture(h);var a=player(h,f,"reply_a");var b=player(h,f,"reply_b");profile(a,HouseRead.Trait.SELF,"MASC","SETTLED");profile(b,HouseRead.Trait.SELF,"FEM","SETTLED");
        for(var place:List.of(LabyrinthPlace.ELK_LOT,LabyrinthPlace.HOTEL,LabyrinthPlace.GHOSTS_SET)){f.built.add(place);var at=f.base(place).offset(0,0,-4);f.chunks.hold(f.level(place),new AABB(at).inflate(5));}
        LiteraryActor[] russ={null},producer={null};HotelActor[] porter={null},bar={null},piano={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"all actual original speaker locations are resident")).thenExecute(()->{
            var lot=f.base(LabyrinthPlace.ELK_LOT);russ[0]=literary(f,LabyrinthPlace.ELK_LOT,LiteraryActor.STRANGER,ElkLotNight.RUSS.phase(),null,lot.offset(0,0,-4));
            for(var p:List.of(a,b))at(p,f.out,lot,.5,0,-2.5);NativeTestPlayers.messages(a);NativeTestPlayers.messages(b);russ[0].interact(a,InteractionHand.MAIN_HAND);
        }).thenIdle(3).thenExecute(()->{
            var am=NativeTestPlayers.messages(a);var bm=NativeTestPlayers.messages(b);
            h.assertTrue(am.stream().anyMatch(s->s.contains("Hey, man."))&&bm.stream().noneMatch(s->s.contains("Hey, man.")),"the delivered Last Stop reply addresses only its actual reader: A="+am+", B="+bm);
            h.assertTrue(!russ[0].getName().getString().contains("Hey, man.")&&f.own(a,LabyrinthPlace.ELK_LOT).getInt("Talk_Russ")==1,"the shared canonical nametag and original conversation cursor remain ordinary");russ[0].interact(b,InteractionHand.MAIN_HAND);
        }).thenIdle(3).thenExecute(()->{
            var bm=NativeTestPlayers.messages(b);var am=NativeTestPlayers.messages(a);
            h.assertTrue(bm.stream().anyMatch(s->s.contains("Hey, miss."))&&am.stream().noneMatch(s->s.contains("Hey, miss."))&&!russ[0].getName().getString().contains("Hey, miss."),"the same original body delivers a private peer address without a shared gendered bubble: A="+am+", B="+bm);
            var hb=f.base(LabyrinthPlace.HOTEL);porter[0]=hotel(f,0,hb.offset(0,0,-4));bar[0]=hotel(f,1,hb.offset(0,0,-4));piano[0]=hotel(f,3,hb.offset(0,0,-4));
            for(var p:List.of(a,b)){at(p,f.in,hb,.5,0,-2.5);var own=HotelVignette.personal(f.data(),p.getUUID());own.putBoolean("Signed",true);own.putString("Here","hotel");own.putBoolean("Stay",false);own.putInt("Visits",5);own.putInt("BarLine04112",1);own.putInt("Debt",7);HotelVignette.save(f.data(),p.getUUID(),own);}
        }).thenIdle(3).thenExecute(()->{
            NativeTestPlayers.messages(a);NativeTestPlayers.messages(b);porter[0].interact(a,InteractionHand.MAIN_HAND);piano[0].interact(a,InteractionHand.MAIN_HAND);bar[0].interact(a,InteractionHand.MAIN_HAND);
        }).thenIdle(3).thenExecute(()->{
            var lines=NativeTestPlayers.messages(a);var peer=NativeTestPlayers.messages(b);
            h.assertTrue(lines.stream().filter(s->s.contains("sir")&&(s.startsWith("The porter:")||s.startsWith("The pianist:")||s.startsWith("The bartender:"))).count()==3&&peer.stream().noneMatch(s->s.startsWith("The porter:")||s.startsWith("The pianist:")||s.startsWith("The bartender:")),"porter, pianist and bartender deliver their actual native replies only to the reader: "+lines);
            var own=HotelVignette.personal(f.data(),a.getUUID());h.assertTrue(own.getInt("BarLine04112")==2&&own.getInt("Visits")==5&&own.getInt("Debt")==7&&a.getInventory().countItem(HotelRegistry.DRINK.get())==1,"the original drink, night, account and conversation mechanics remain");bar[0].interact(a,InteractionHand.MAIN_HAND);h.assertTrue(a.getInventory().countItem(HotelRegistry.DRINK.get())==1,"the original forty-tick drink cooldown still applies");porter[0].interact(b,InteractionHand.MAIN_HAND);
        }).thenIdle(3).thenExecute(()->{
            var peer=NativeTestPlayers.messages(b);var first=NativeTestPlayers.messages(a);h.assertTrue(peer.stream().anyMatch(s->s.startsWith("The porter:")&&s.contains("ma’am"))&&first.stream().noneMatch(s->s.startsWith("The porter:")),"the same original porter keeps its delivered peer reply private");
            var gb=f.base(LabyrinthPlace.GHOSTS_SET);at(a,f.in,gb,.5,0,-2.5);at(b,f.in,gb,1.5,0,-2.5);producer[0]=literary(f,LabyrinthPlace.GHOSTS_SET,LiteraryActor.GHOST_PRODUCER,GhostsSet.STAND,a.getUUID(),gb.offset(0,0,-4));var day=f.own(a,LabyrinthPlace.GHOSTS_SET);day.putBoolean("Filming0485",true);day.putInt("FilmDay",2);day.putInt("ShotClock",520);LiteraryVignettes.save(f.data(),a.getUUID(),LabyrinthPlace.GHOSTS_SET,day);NativeTestPlayers.messages(a);NativeTestPlayers.messages(b);producer[0].interact(a,InteractionHand.MAIN_HAND);
        }).thenIdle(3).thenExecute(()->{
            var day=f.own(a,LabyrinthPlace.GHOSTS_SET);var first=NativeTestPlayers.messages(a);var peer=NativeTestPlayers.messages(b);h.assertTrue(first.stream().anyMatch(s->s.contains("Barry: All right, man."))&&peer.stream().noneMatch(s->s.startsWith("Barry:")),"Barry's actual owner-bound conversation is delivered privately");h.assertTrue(day.getInt("FilmDay")==2&&day.getInt("ShotClock")==520&&day.getInt("GhostTalk_2_4")==1&&!day.getBoolean("DayDone"),"the crew line changes no consent, shot, day or film progress");a.closeContainer();
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="tailoring_props",timeoutTicks=800)
    public static void samePhysicalSinkAndVanityKeepDifferentOwnerOnlyNativeOriginalsAcrossReloadAndRemoval(GameTestHelper h){
        var f=fixture(h);var a=player(h,f,"prop_a");var b=player(h,f,"prop_b");profile(a,HouseRead.Trait.HAIR,"WRAPPED","TENTATIVE");profile(b,HouseRead.Trait.HAIR,"BRUSHED_DRY","SETTLED");
        var hotel=f.base(LabyrinthPlace.HOTEL);var lake=f.base(LabyrinthPlace.LAKE_HOUSE);var sink=hotel.offset(VignetteRoutineEchoes.HOTEL_SINK);var vanity=lake.offset(VignetteRoutineEchoes.LAKE_VANITY);f.chunks.hold(f.in,new AABB(sink).inflate(5));f.chunks.hold(f.out,new AABB(vanity).inflate(5));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"both actual native counter locations and entity sections are resident")).thenExecute(()->{
            f.in.setBlock(sink,StanleyBlock.of(StanleyBlock.Kind.SINK,Direction.SOUTH),2);f.out.setBlock(vanity,LakeHouseFittingBlock.of(LakeHouseFittingBlock.Kind.TABLE,Direction.EAST),2);var bowl=LakeHouseFittingBlock.of(LakeHouseFittingBlock.Kind.KEY_BOWL,Direction.EAST);f.out.setBlock(vanity.above(),bowl,2);
            var ha=new CompoundTag();var hb=new CompoundTag();for(var p:List.of(a,b))at(p,f.in,sink,.5,0,1.5);VignetteRoutineEchoes.hotel(a,hotel,ha);VignetteRoutineEchoes.hotel(b,hotel,hb);
            var ea=(RoutineEchoDisplay)f.in.getEntity(ha.getCompound(VignetteRoutineEchoes.KEY).getUUID("Id"));var eb=(RoutineEchoDisplay)f.in.getEntity(hb.getCompound(VignetteRoutineEchoes.KEY).getUUID("Id"));
            h.assertTrue(ea!=null&&eb!=null&&!ea.getUUID().equals(eb.getUUID())&&ea.position().equals(eb.position()),"two readers have distinct original entities at the same actual sink");h.assertTrue(ea.broadcastToPlayer(a)&&!ea.broadcastToPlayer(b)&&eb.broadcastToPlayer(b)&&!eb.broadcastToPlayer(a),"the native entity tracker admits only the owner");
            f.extras.add(ea);f.extras.add(eb);HotelVignette.save(f.data(),a.getUUID(),ha);HotelVignette.save(f.data(),b.getUUID(),hb);var sa=ea.saveWithoutId(new CompoundTag());var sb=eb.saveWithoutId(new CompoundTag());h.assertTrue(ItemStack.parseOptional(a.registryAccess(),sa.getCompound("item")).is(HouseReadRegistry.ROUTINES.get(0).get())&&ItemStack.parseOptional(b.registryAccess(),sb.getCompound("item")).is(HouseReadRegistry.ROUTINES.get(6).get()),"the actual native display stacks reflect the saved hair read");h.assertTrue(ea.getY()==sink.getY()+14/16D,"the original item's native transform starts on the actual porcelain rim");
            var copy=HouseReadRegistry.ROUTINE_ECHO.get().create(f.in);copy.load(sa);h.assertTrue(copy.ownedBy(a.getUUID())&&!copy.ownedBy(b.getUUID())&&ItemStack.matches(ItemStack.parseOptional(a.registryAccess(),sa.getCompound("item")),ItemStack.parseOptional(a.registryAccess(),copy.saveWithoutId(new CompoundTag()).getCompound("item"))),"native entity NBT retains owner, exact components and support across a real decode");
            profile(a,HouseRead.Trait.HAIR,"WASHED_AND_BARBERED","SETTLED");f.reload();ha=HotelVignette.personal(f.data(),a.getUUID());VignetteRoutineEchoes.hotel(a,hotel,ha);h.assertTrue(ha.getCompound(VignetteRoutineEchoes.KEY).getUUID("Id").equals(ea.getUUID())&&ha.getCompound(VignetteRoutineEchoes.KEY).getString("Hair").equals("WRAPPED"),"later observations and SavedData reload never replace a selected original");ea.discard();VignetteRoutineEchoes.hotel(a,hotel,ha);h.assertTrue(ha.getCompound(VignetteRoutineEchoes.KEY).getUUID("Id").equals(ea.getUUID()),"a removed original's retained ID prevents replenishment");f.in.setBlock(sink,Blocks.GOLD_BLOCK.defaultBlockState(),2);eb.tick();h.assertTrue(eb.isRemoved()&&f.in.getBlockState(sink).is(Blocks.GOLD_BLOCK),"an edited support is retained and its finite display cannot float over it");
            at(a,f.out,vanity,.5,0,1.5);var la=new CompoundTag();VignetteRoutineEchoes.lake(a,lake,la);var el=(RoutineEchoDisplay)f.out.getEntity(la.getCompound(VignetteRoutineEchoes.KEY).getUUID("Id"));if(el!=null)f.extras.add(el);h.assertTrue(el!=null&&el.ownedBy(a.getUUID())&&el.getY()==vanity.getY()+13/16D&&f.out.getBlockState(vanity.above()).equals(bowl),"the actual upstairs vanity keeps its original bowl and the private object touches its native tabletop");
            var neutral=player(h,f,"prop_neutral");at(neutral,f.out,vanity,.5,0,1.5);var ln=new CompoundTag();VignetteRoutineEchoes.lake(neutral,lake,ln);h.assertTrue(ln.getCompound(VignetteRoutineEchoes.KEY).getBoolean("Picked")&&!ln.getCompound(VignetteRoutineEchoes.KEY).hasUUID("Id"),"an unread player receives the exact ordinary counter");h.succeed();
        });
    }
}
