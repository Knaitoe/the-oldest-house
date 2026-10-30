package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** 0.4.173, "Third Floor Girls'": the dare in native time, with the real door, the candle and the reader's own gaze. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class DareTests {
    private static final LabyrinthPlace P=LabyrinthPlace.DARE_IN_THE_DARK;
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="dare173_room") public static void roomDone(ServerLevel l){close();}
    @AfterBatch(batch="dare173_count") public static void countDone(ServerLevel l){close();}
    @AfterBatch(batch="dare173_handle") public static void handleDone(ServerLevel l){close();}
    @AfterBatch(batch="dare173_early") public static void earlyDone(ServerLevel l){close();}
    private static void close(){if(active!=null){active.close();active=null;}DareInTheDark.clearAll();}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){var f=new LiteraryTests.Fixture(h);active=f;f.build(P);f.chunks.hold(f.in,IndianLakeRooms.bounds(f.base(P),P).inflate(4));return f;}
    private static void use(ServerPlayer p,BlockPos at){p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.WEST,at,false));}
    /** On the mat, square to the glass, at the reader's own eyes. */
    private static void mat(LiteraryTests.Fixture f,ServerPlayer p){f.at(p,P,DareRooms.MAT.getX()+1,0,DareRooms.MAT.getZ()+.2);p.setYRot(180);p.setXRot(0);p.setYHeadRot(180);}
    private static void inHall(LiteraryTests.Fixture f,ServerPlayer p){f.at(p,P,1.5,0,DareRooms.DOOR.getZ()+.5);}
    private static boolean candleInHand(ServerPlayer p){
        for(int i=0;i<p.getInventory().items.size();i++)if(p.getInventory().items.get(i).is(DareRegistry.CANDLE.get())){var s=p.getInventory().items.get(i);p.getInventory().items.set(i,ItemStack.EMPTY);p.setItemInHand(InteractionHand.MAIN_HAND,s);return true;}
        return DareInTheDark.holding(p);
    }
    private static List<String> chat(ServerPlayer p){var out=new ArrayList<String>();for(var x:NativeTestPlayers.packets(p))if(x instanceof ClientboundSystemChatPacket c)out.add(c.content().getString());return out;}
    private static boolean doorOpen(LiteraryTests.Fixture f){return DareRooms.doorOpen(f.in,f.base(P));}

    @GameTest(template="empty",batch="dare173_room",timeoutTicks=200)
    public static void theThirdFloorHasItsGlassStallsMatDoorAndTheOwnersNote(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.in;
        h.assertTrue(LiteraryRooms.isLiterary(P)&&P.slot()==113&&WitnessAccount.Story.of(P.id())==WitnessAccount.Story.DARE_IN_THE_DARK&&WitnessAccount.Story.DARE_IN_THE_DARK.kind.equals("survival"),"a literary survival source at slot 113");
        h.assertTrue(SceneInstances.copyable(P)&&SharedVignettes.privateScene(P),"a dare a reader takes alone, in their own copy");
        for(int x=0;x<DareMirrorBlock.WIDTH;x++)for(int y=0;y<DareMirrorBlock.HEIGHT;y++){var s=l.getBlockState(b.offset(DareRooms.MIRROR).offset(x,y,0));
            h.assertTrue(s.is(DareRegistry.MIRROR.get())&&s.getValue(DareMirrorBlock.PART)==x+y*DareMirrorBlock.WIDTH&&!s.getValue(DareMirrorBlock.SHATTERED)&&s.getValue(DareMirrorBlock.FACING)==Direction.SOUTH,"the wide mirror hangs whole over the sinks: "+x+","+y);
            h.assertTrue(DareFittingBlock.is(l.getBlockState(b.offset(DareRooms.MIRROR.getX()+x,0,DareRooms.BATH_Z0)),DareFittingBlock.Kind.SINK),"a sink under each part of the glass");}
        h.assertTrue(DareFittingBlock.is(l.getBlockState(b.offset(DareRooms.MAT)),DareFittingBlock.Kind.BATHMAT)&&DareFittingBlock.is(l.getBlockState(b.offset(DareRooms.MAT).east()),DareFittingBlock.Kind.BATHMAT),"the worn mat before the middle two sinks");
        var stall=l.getBlockState(b.offset(DareRooms.SHUT_STALL));h.assertTrue(DareFittingBlock.is(stall,DareFittingBlock.Kind.STALL_DOOR)&&stall.getValue(DareFittingBlock.STAGE)==0&&DareFittingBlock.is(l.getBlockState(b.offset(DareRooms.SHUT_STALL).above()),DareFittingBlock.Kind.STALL_DOOR),"the first stall is shut, in the real room");
        var door=l.getBlockState(DareRooms.door(b));h.assertTrue(door.getBlock() instanceof DareDoorBlock&&door.getValue(DoorBlock.OPEN),"the heavy door stands open to the hall");
        var shut=door.setValue(DoorBlock.OPEN,false);h.assertTrue(shut.getLightBlock(l,DareRooms.door(b))==15&&door.getLightBlock(l,DareRooms.door(b))==0,"shut, it lets no light from the hall through");
        h.assertTrue(DareFittingBlock.is(l.getBlockState(b.offset(DareRooms.NOTE)),DareFittingBlock.Kind.NOTE)&&LiteraryRooms.source(P).equals(DareRooms.NOTE)&&LiteraryRooms.ending(P).equals(DareRooms.ACCOUNT),"the folded square lies by the door");
        h.assertTrue(l.getBlockState(b.offset(DareRooms.MAT).above(3)).isSolidRender(l,b.offset(DareRooms.MAT).above(3))&&l.getBlockState(b.offset(DareRooms.MAT).above()).isAir(),"the bathroom has its own low ceiling and room to stand");
        for(int z:new int[]{-4,-13,-22})h.assertTrue(DareFittingBlock.is(l.getBlockState(b.offset(0,3,z)),DareFittingBlock.Kind.HALL_LIGHT),"the hall is lit");
        h.assertTrue(DareRooms.inBathroom(b,Vec3.atBottomCenterOf(b.offset(DareRooms.MAT)))&&!DareRooms.inBathroom(b,Vec3.atBottomCenterOf(b.offset(DareRooms.KID_A))),"the bathroom and the hall are told apart");
        // The owner's note, word for word, in a child's purple hand.
        var content=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);
        String plain=DareTexts.NOTE.replaceAll("\\s+"," ").strip();String kept=String.join(" ",content.pages().stream().map(p->p.raw().getString()).toList()).replaceAll("\\s+"," ").strip();
        h.assertTrue(kept.length()==plain.length(),"the note keeps every character of the owner's words: "+kept.length()+" / "+plain.length());
        h.assertTrue(content.pages().stream().allMatch(p->p.raw().visit((style,text)->style.getColor()!=null&&style.getColor().getValue()==0x6a2fa0?Optional.of(true):Optional.empty(),net.minecraft.network.chat.Style.EMPTY).isPresent()),"in purple gel pen");
        var own=new net.minecraft.nbt.CompoundTag();own.putBoolean("Read_Source",true);own.putInt(DareInTheDark.LOST,2);own.putBoolean(DareInTheDark.OPENED_EARLY,true);var account=DareTexts.account(own);
        h.assertTrue(account.startsWith("The note said nothing happens on the first two.")&&account.contains("started again twice")&&account.contains("they said it didn't count")&&account.endsWith("I did not turn round."),"the account follows what the reader did: "+account);
        for(String word:List.of(" boy"," girl "," young"," old ","white","black","brown"))h.assertTrue(!(" "+account.toLowerCase(Locale.ROOT)+" ").contains(word),"the account never reads the reader: "+word);
        h.succeed();
    }

    @GameTest(template="empty",batch="dare173_count",timeoutTicks=1100)
    public static void theCountNeedsTheLatchTheMatTheCandleAndTheEyesAndAGlanceAwayStartsItAgain(GameTestHelper h){
        var f=fixture(h);ServerPlayer[] p={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the third floor is resident")).thenExecute(()->{p[0]=f.player("dare_reader",P);inHall(f,p[0]);})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean(DareInTheDark.CANDLE_GIVEN)&&DareInTheDark.kidA(p[0])!=null&&DareInTheDark.kidB(p[0])!=null,"at the door the kids hand over the candle"))
        .thenExecute(()->{h.assertTrue(candleInHand(p[0]),"the candle stub is the reader's to hold");h.assertTrue(DareInTheDark.forwardHeld(p[0],P),"the stairwell door is held shut until the dare is done");
            mat(f,p[0]);})
        .thenIdle(40).thenExecute(()->{var own=f.own(p[0],P);h.assertTrue(!own.getBoolean(DareInTheDark.LATCHED)&&own.getInt(DareInTheDark.STARE)==0,"with the door standing open nothing is counted");
            use(p[0],DareRooms.door(f.base(P)));h.assertTrue(!doorOpen(f),"the reader shuts the door");})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean(DareInTheDark.LATCHED),"it latches"))
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt(DareInTheDark.COUNT)==1,"a few seconds of their own eyes, and it is said once"))
        .thenExecute(()->{var kid=DareInTheDark.kidA(p[0]);h.assertTrue(kid!=null&&kid.phase()==1,"outside, they giggle");h.assertTrue(chat(p[0]).stream().anyMatch(s->s.contains("Bloody Mary…")),"the reader says it");
            p[0].setYRot(110);})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt(DareInTheDark.COUNT)==0&&f.own(p[0],P).getInt(DareInTheDark.LOST)==1,"looking away loses the count"))
        .thenExecute(()->{p[0].setYRot(186);p[0].setXRot(5);})
        .thenIdle(30).thenExecute(()->{var own=f.own(p[0],P);h.assertTrue(own.getInt(DareInTheDark.STARE)>=20,"a little movement is forgiven: "+own.getInt(DareInTheDark.STARE));
            p[0].setXRot(40);})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt(DareInTheDark.STARE)==0,"looking down at the sink starts it again"))
        .thenExecute(()->{p[0].setXRot(0);f.at(p[0],P,DareRooms.MAT.getX()+1,0,DareRooms.MAT.getZ()+2.6);p[0].setYRot(180);})
        .thenIdle(30).thenExecute(()->{h.assertTrue(f.own(p[0],P).getInt(DareInTheDark.STARE)==0,"off the mat it is not counted");mat(f,p[0]);
            var held=p[0].getMainHandItem();p[0].setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p[0].getInventory().setItem(8,held);})
        .thenIdle(30).thenExecute(()->{h.assertTrue(f.own(p[0],P).getInt(DareInTheDark.STARE)==0,"nor without the candle in hand");h.succeed();});
    }

    @GameTest(template="empty",batch="dare173_handle",timeoutTicks=900)
    public static void afterTheThirdTheHandleIsHeldThenGivesAndTheGlassBreaksAsTheReaderCrosses(GameTestHelper h){
        var f=fixture(h);ServerPlayer[] p={null};int[] heldAt={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the third floor is resident")).thenExecute(()->{p[0]=f.player("dare_third",P);inHall(f,p[0]);})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean(DareInTheDark.CANDLE_GIVEN),"the candle is handed over"))
        .thenExecute(()->{candleInHand(p[0]);f.source(p[0],P);mat(f,p[0]);use(p[0],DareRooms.door(f.base(P)));})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean(DareInTheDark.LATCHED),"latched"))
        .thenExecute(()->{var own=f.own(p[0],P);own.putInt(DareInTheDark.COUNT,2);own.putInt(DareInTheDark.STARE,DareInTheDark.THIRD-20);LiteraryVignettes.save(f.data(),p[0].getUUID(),P,own);})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt(DareInTheDark.COUNT)==3,"the third count is said"))
        .thenExecute(()->{var kid=DareInTheDark.kidB(p[0]);h.assertTrue(kid!=null&&kid.phase()==2,"outside, they have gone silent");
            p[0].setYRot(0);})
        .thenIdle(20).thenExecute(()->{h.assertTrue(f.own(p[0],P).getInt(DareInTheDark.COUNT)==3,"once it is done, looking away does not restart it");
            use(p[0],DareRooms.door(f.base(P)));var own=f.own(p[0],P);heldAt[0]=own.getInt(DareInTheDark.HOLD_AT);
            h.assertTrue(own.getBoolean(DareInTheDark.HOLDING)&&!doorOpen(f),"the handle will not turn: someone holds it from the hall");})
        .thenIdle(25).thenExecute(()->{use(p[0],DareRooms.door(f.base(P)));var own=f.own(p[0],P);h.assertTrue(own.getInt(DareInTheDark.HOLD_AT)==heldAt[0]&&!doorOpen(f),"trying again does not start it over");})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean(DareInTheDark.BURST)&&doorOpen(f),"then it gives all at once"))
        .thenExecute(()->{var b=f.base(P);h.assertTrue(DareInTheDark.kidA(p[0])==null&&DareInTheDark.kidB(p[0])==null,"the kids are gone");
            h.assertTrue(DareFittingBlock.is(f.in.getBlockState(b.offset(DareRooms.KID_A)),DareFittingBlock.Kind.SHEET)&&DareFittingBlock.is(f.in.getBlockState(b.offset(DareRooms.KID_B)),DareFittingBlock.Kind.SHEET),"their two sheets lie crumpled where they stood");
            h.assertTrue(!f.in.getBlockState(b.offset(DareRooms.MIRROR)).getValue(DareMirrorBlock.SHATTERED)&&DareInTheDark.forwardHeld(p[0],P),"the glass and the way on wait for the reader to cross");
            p[0].moveTo(b.getX()+5.5,b.getY(),b.getZ()+DareRooms.DOOR.getZ()+.5);})
        .thenIdle(10).thenExecute(()->{var b=f.base(P);p[0].moveTo(b.getX()+1.5,b.getY(),b.getZ()+DareRooms.DOOR.getZ()+.5);})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean(DareInTheDark.CROSSED),"the reader crosses back into the hall"))
        .thenExecute(()->{var b=f.base(P);var own=f.own(p[0],P);
            for(int x=0;x<DareMirrorBlock.WIDTH;x++)for(int y=0;y<DareMirrorBlock.HEIGHT;y++)h.assertTrue(f.in.getBlockState(b.offset(DareRooms.MIRROR).offset(x,y,0)).getValue(DareMirrorBlock.SHATTERED),"the glass breaks behind them");
            var exit=f.in.getBlockState(b.offset(0,0,DareRooms.EXIT_Z));h.assertTrue(own.getBoolean("Ready")&&!DareInTheDark.forwardHeld(p[0],P)&&(!(exit.getBlock() instanceof DoorBlock)||exit.getValue(DoorBlock.OPEN)),"the stairwell door opens and the account is ready");
            h.assertTrue(!WitnessAccount.has(f.data(),p[0].getUUID(),WitnessAccount.Story.DARE_IN_THE_DARK),"nothing is resolved until the account is read");
            f.read(p[0],P,LiteraryRooms.ending(P));})
        .thenWaitUntil(()->h.assertTrue(WitnessAccount.has(f.data(),p[0].getUUID(),WitnessAccount.Story.DARE_IN_THE_DARK)&&f.own(p[0],P).getBoolean("Completed"),"reading the account resolves the dare for this reader"))
        .thenExecute(()->{p[0].closeContainer();p[0].teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+1,f.origin.getZ()+.5,0,0);})
        .thenWaitUntil(()->h.assertTrue(!DareInTheDark.holding(p[0])&&p[0].getInventory().items.stream().noneMatch(s->s.is(DareRegistry.CANDLE.get())),"off the third floor the candle is handed back"))
        .thenExecute(h::succeed);
    }

    @GameTest(template="empty",batch="dare173_early",timeoutTicks=700)
    public static void openingTheDoorBeforeTheThirdFailsThatAttemptAndItMayBeTriedAgain(GameTestHelper h){
        var f=fixture(h);ServerPlayer[] p={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the third floor is resident")).thenExecute(()->{p[0]=f.player("dare_early",P);inHall(f,p[0]);})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean(DareInTheDark.CANDLE_GIVEN),"the candle is handed over"))
        .thenExecute(()->{candleInHand(p[0]);mat(f,p[0]);use(p[0],DareRooms.door(f.base(P)));})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt(DareInTheDark.COUNT)==1,"the first count"))
        .thenExecute(()->{chat(p[0]);use(p[0],DareRooms.door(f.base(P)));var own=f.own(p[0],P);
            h.assertTrue(doorOpen(f)&&own.getInt(DareInTheDark.COUNT)==0&&own.getBoolean(DareInTheDark.OPENED_EARLY)&&!own.getBoolean(DareInTheDark.LATCHED)&&own.getInt(DareInTheDark.LOST)==1,"opening the door ends that attempt");})
        .thenIdle(2).thenExecute(()->{h.assertTrue(chat(p[0]).stream().anyMatch(s->s.contains("That didn't count. You came straight back out.")),"and they say so");
            use(p[0],DareRooms.door(f.base(P)));})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean(DareInTheDark.LATCHED)&&f.own(p[0],P).getInt(DareInTheDark.STARE)>0,"shut again, the dare begins again"))
        .thenExecute(()->{
            // Gone after the door gave but before crossing back: the next visit starts the dare over, the sheets picked up.
            var b=f.base(P);var own=f.own(p[0],P);own.putBoolean(DareInTheDark.BURST,true);own.putInt(DareInTheDark.COUNT,3);
            for(var rel:java.util.List.of(DareRooms.KID_A,DareRooms.KID_B))f.in.setBlock(b.offset(rel),DareFittingBlock.of(DareFittingBlock.Kind.SHEET,net.minecraft.core.Direction.NORTH,0),3);
            DareInTheDark.arrive(p[0],own);LiteraryVignettes.save(f.data(),p[0].getUUID(),P,own);
            h.assertTrue(!own.getBoolean(DareInTheDark.BURST)&&own.getInt(DareInTheDark.COUNT)==0&&f.in.getBlockState(b.offset(DareRooms.KID_A)).isAir()&&f.in.getBlockState(b.offset(DareRooms.KID_B)).isAir(),"a later visit starts the dare from the beginning");})
        .thenExecute(h::succeed);
    }
}
