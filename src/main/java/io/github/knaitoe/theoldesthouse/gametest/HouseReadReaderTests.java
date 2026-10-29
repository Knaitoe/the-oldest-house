package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.util.regex.Pattern;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.gametest.*;
import io.github.knaitoe.theoldesthouse.network.HouseBodiesPayload;
import static io.github.knaitoe.theoldesthouse.labyrinth.HouseRead.Trait.*;

/** 0.4.159: the House's read of the reader, from the quiet rooms' signals to the account, the book, the glass and the deal. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_house_read") @PrefixGameTestTemplate(false)
public final class HouseReadReaderTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="read_logic") public static void logicDone(ServerLevel ignored){close();}
    @AfterBatch(batch="read_eaves") public static void eavesDone(ServerLevel ignored){close();}
    @AfterBatch(batch="read_yard") public static void yardDone(ServerLevel ignored){close();}
    @AfterBatch(batch="read_glass") public static void glassDone(ServerLevel ignored){close();}
    @AfterBatch(batch="read_body163") public static void bodyDone(ServerLevel ignored){close();}
    private static void close(){HouseReadSites.clear();if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){active=new LiteraryTests.Fixture(h);return active;}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);io.github.knaitoe.theoldesthouse.opening.OpeningSequence.state(p).markEntered();p.setNoGravity(true);return p;}
    private static void transition(ServerPlayer p){if(LabyrinthDoors.isBusy(p))HouseTransitionEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));}
    private static void click(ServerPlayer p,BlockPos at){p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));}
    private static LabyrinthData.Door source(LiteraryTests.Fixture f){var at=f.origin.offset(0,0,-5);for(var pos:BlockPos.betweenClosed(at.offset(-4,-1,-3),at.offset(4,-1,5)))f.in.setBlock(pos,Blocks.OAK_PLANKS.defaultBlockState(),18);NovelRooms.door(f.in,at,Direction.SOUTH,Blocks.OAK_DOOR,false);var door=new LabyrinthData.Door("native/read/source",f.in.dimension(),at,Direction.SOUTH,LabyrinthData.DEALT,false);f.data().putDoor(door);return door;}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos at,double x,double y,double z){p.stopRiding();p.teleportTo(l,at.getX()+x,at.getY()+y,at.getZ()+z,180,0);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();p.hasChangedDimension();}
    private static void seed(ServerPlayer p,HouseReadSites.Room room,LabyrinthData.Door from){var d=LabyrinthData.get(p.server);var own=HouseReadSites.state(p.server,p.getUUID());var visit=own.getCompound(room.key());visit.putBoolean("Entered",true);visit.putBoolean("Here",true);visit.putString("SourceDoor",from.id);visit.putString("NextPlace",LabyrinthPlace.GRAY_CORRIDOR.id());own.put(room.key(),visit);own.putString("Active",room.key());d.setStateEntry(HouseReadSites.STATE,p.getUUID().toString(),own);}
    private static boolean ready(ServerPlayer p,HouseReadSites.Room room){boolean ready=HouseReadSites.prepare(p,room)!=null;if(!ready)java.util.concurrent.locks.LockSupport.parkNanos(2_000_000L);return ready;}
    private static CompoundTag visit(ServerPlayer p,HouseReadSites.Room room){return HouseReadSites.state(p.server,p.getUUID()).getCompound(room.key());}
    private static HouseRead.Category raw(ServerPlayer p,HouseRead.Trait t){return HouseRead.raw(LabyrinthData.get(p.server),p.getUUID(),t);}
    private static CompoundTag tag(Object... pairs){var t=new CompoundTag();for(int i=0;i<pairs.length;i+=2){if(pairs[i+1] instanceof String v)t.putString((String)pairs[i],v);else if(pairs[i+1] instanceof Integer v)t.putInt((String)pairs[i],v);else t.putBoolean((String)pairs[i],(Boolean)pairs[i+1]);}return t;}
    private static void night(LabyrinthData d,UUID id,CompoundTag seen){var own=d.stateEntry(HouseRead.STATE,id.toString());own.put("Seen",seen);d.setStateEntry(HouseRead.STATE,id.toString(),own);}
    private static long looks(ServerPlayer p){var queue=new ArrayDeque<net.minecraft.network.protocol.Packet<?>>(NativeTestPlayers.packets(p));long n=0;while(!queue.isEmpty()){var packet=queue.removeFirst();if(packet instanceof net.minecraft.network.protocol.BundlePacket<?> bundle)bundle.subPackets().forEach(queue::add);else if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c&&c.payload() instanceof io.github.knaitoe.theoldesthouse.network.HouseReadLookPayload)n++;}return n;}
    private static final Pattern GENDERED=Pattern.compile("\\b(he|she|him|his|her|hers|himself|herself)\\b",Pattern.CASE_INSENSITIVE);
    private static final String OWNER="You read the local page until the door clicked, and put the silk on before the bathroom light went out. In the hall, you took the trench coat off the peg without looking in the glass, stood by the girl under the eaves until the song finished, and went straight in when the whistle blew.";

    private static List<HouseBodiesPayload> bodies(ServerPlayer p){
        var out=new ArrayList<HouseBodiesPayload>();var queue=new ArrayDeque<net.minecraft.network.protocol.Packet<?>>(NativeTestPlayers.packets(p));
        while(!queue.isEmpty()){var packet=queue.removeFirst();if(packet instanceof net.minecraft.network.protocol.BundlePacket<?> bundle)bundle.subPackets().forEach(queue::add);else if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c&&c.payload() instanceof HouseBodiesPayload b)out.add(b);}
        return out;
    }

    /** 0.4.163, the owner: "have you implemented the check for 'human' skins yet? my player skin is still displaying." */
    @GameTest(template="empty",batch="read_body163",timeoutTicks=200)
    public static void aReaderWhoseFaceIsNotHumanWearsTheBodyTheHouseReadInsideTheHouseOnly(GameTestHelper h){
        // The face check each client makes of its own skin: the front of the head, with the hat layer over it.
        int tone=0xffc8946e,green=0xff5ea948,grey=0xff3c3c3c;
        java.util.function.IntBinaryOperator person=(x,y)->x>=40?0:(y<10?0xff4a3326:tone),creeper=(x,y)->x>=40?0:green,wither=(x,y)->x>=40?0:grey;
        java.util.function.IntBinaryOperator masked=(x,y)->x>=40&&x<48&&y>=8&&y<16?green:(y<10?0xff4a3326:tone),bare=(x,y)->0;
        h.assertTrue(HouseBodies.humanFace(person)&&!HouseBodies.humanFace(creeper)&&!HouseBodies.humanFace(wither),"a face in human skin colours reads human; a creeper's or a wither's does not");
        h.assertTrue(!HouseBodies.humanFace(masked)&&!HouseBodies.humanFace(bare),"the hat layer over the face counts, and an empty face reads nothing human");
        for(int c:new int[]{0xf5d6c6,0xe0ac69,0xc68642,0x8d5524,0x5c3a21,0x3b2219})h.assertTrue(HouseBodies.skinColour(c>>16&255,c>>8&255,c&255),"every human skin tone reads as skin: "+Integer.toHexString(c));
        for(int c:new int[]{0x5ea948,0x3c3c3c,0xbebebe,0x4682dc,0x46825e,0x191919})h.assertTrue(!HouseBodies.skinColour(c>>16&255,c>>8&255,c&255),"greens, greys, blues and black do not: "+Integer.toHexString(c));
        var f=fixture(h);var p=player(h,f,"body_creeper");var q=player(h,f,"body_person");var d=f.data();
        // The body follows the read; what isn't read takes the neutral choices.
        h.assertTrue(HouseBodies.body(d,p.getUUID()).equals(new HouseBodies.Body(0,0,0,-1,-1)),"an unread reader is a young adult in plain clothes with short hair, in their account's default colours");
        HouseRead.restart(p,AGE,"ANALOG");HouseRead.restart(p,HAIR,"BRAIDED_AND_OILED");HouseRead.restart(p,SELF,"FEM");
        h.assertTrue(HouseBodies.body(d,p.getUUID()).equals(new HouseBodies.Body(2,0,2,-1,-1)),"age and hair follow the read; the clothes wait for a settled presentation: "+HouseBodies.body(d,p.getUUID()));
        HouseRead.signal(p,SELF,"FEM",true);
        h.assertTrue(HouseBodies.body(d,p.getUUID()).equals(new HouseBodies.Body(2,2,2,-1,-1))&&HouseBodies.body(d,p.getUUID()).sheet().equals("older_fem_braids"),"settled, the clothes follow too: "+HouseBodies.body(d,p.getUUID()).sheet());
        // Only an inhuman skin, and only inside the House.
        stand(p,f.out,f.origin,0,0,0);stand(q,f.out,f.origin,2,0,0);HouseBodies.report(p,false);HouseBodies.report(q,true);
        h.assertTrue(HouseBodies.worn(p)!=null&&HouseBodies.worn(q)==null,"inside the House the inhuman-skinned reader wears the body and the human-skinned reader keeps their own");
        bodies(p);bodies(q);HouseBodies.sync(p.server);
        for(var reader:List.of(p,q)){var sent=bodies(reader);h.assertTrue(sent.size()==1&&sent.getFirst().bodies().size()==1&&sent.getFirst().bodies().getFirst().reader().equals(p.getUUID())&&sent.getFirst().bodies().getFirst().body().sheet().equals("older_fem_braids"),"every reader there is told the body, and only the inhuman-skinned reader's: "+sent);}
        HouseBodies.sync(p.server);h.assertTrue(bodies(p).isEmpty(),"nothing is sent again until something changes");
        stand(p,h.getLevel(),h.absolutePos(BlockPos.ZERO).above(),0,0,0);
        h.assertTrue(HouseBodies.worn(p)==null,"outside the House their own skin returns");HouseBodies.sync(p.server);var after=bodies(q);
        h.assertTrue(after.size()==1&&after.getFirst().bodies().isEmpty(),"and everyone is told: "+after);
        stand(p,f.out,f.origin,0,0,0);
        try{HouseConfig.HOUSE_BODIES.set(false);h.assertTrue(HouseBodies.worn(p)==null,"the server option turns the House's body off");}finally{HouseConfig.HOUSE_BODIES.set(true);}
        HouseBodies.report(p,true);h.assertTrue(HouseBodies.worn(p)==null,"a reader whose skin is human again wears their own");
        h.succeed();
    }

    /** Every observation each room's clause can be written from: all its branches. */
    private static List<List<CompoundTag>> variants(){
        var waiting=new ArrayList<CompoundTag>(List.of(tag("First","paper","Kept","paper","Waited",true),tag("First","magazine","Kept","magazine","Waited",true),tag("First","glasses","Kept","glasses","Waited",true),tag("First","phone","Kept","phone","Waited",true),tag("First","tv","Kept","channels"),tag("First","tv","Kept","cart","Waited",true),tag("First","cooler","Kept","cart","Waited",true),tag("First","paper","Kept","chair","Waited",true),tag("First","glasses","Kept","paper","Waited",true),tag("First","phone","Waited",true),tag("First","cooler"),tag("Sat",true,"Waited",true),tag("Waited",true)));
        var bathroom=new ArrayList<CompoundTag>();for(int i=0;i<16;i++){bathroom.add(tag("Item",i,"Used",i,"LightOff",true));bathroom.add(tag("Item",i,"LightOff",i%3!=2));}
        bathroom.addAll(List.of(tag("Item",3,"Used",7,"LightOff",true),tag("Item",0,"Used",4,"Tap",true,"LightOff",true),tag("Item",11,"Used",11,"Tap",true,"LightOff",true),tag("Used",2),tag("Untouched",true,"LightOff",true),tag("Tap",true)));
        var coatroom=new ArrayList<CompoundTag>();for(String coat:new String[]{"wool","trench","barn"})for(String mirror:new String[]{"","looked","turned_after","covered_after"})coatroom.add(tag("Coat",coat,"Mirror",mirror));
        coatroom.addAll(List.of(tag("Coat","trench","Boots","trench"),tag("Coat","wool","Boots","trench","Mixed",true),tag("Boots","barn"),tag("Coat","barn","Mirror","turned_before","MirrorFirst",true),tag("Mirror","covered_before","MirrorFirst",true)));
        var shelter=new ArrayList<CompoundTag>();for(String stood:new String[]{"man","woman","middle","lantern"})for(boolean song:new boolean[]{false,true})shelter.add(tag("Stood",stood,"Song",song));for(String looked:new String[]{"man","woman","both"})shelter.add(tag("Looked",looked));
        var porch=new ArrayList<CompoundTag>(List.of(tag("First","in"),tag("First","in","Table",true),tag("First","lattice"),tag("First","lattice","Hidden",true),tag("First","window"),tag("First","window","Hidden",true),tag("First","road"),tag("First","road","Trees",true),tag("Out","kitchen"),tag("Out","gate"),tag("Out","road")));
        return List.of(waiting,bathroom,coatroom,shelter,porch);
    }
    private static void reads(GameTestHelper h,CompoundTag seen,boolean older,String home){
        var clauses=HouseReadProse.clauses(seen,older);String text=HouseReadProse.opening(clauses,HouseReadProse.close(home));String body=text.split("\n\n")[0];
        h.assertTrue(!clauses.isEmpty()&&!text.isEmpty(),"a night with rooms in it has an opening: "+seen);
        var sentences=body.split("(?<=\\.) ");int expected=clauses.size()>2&&clauses.getFirst().room()<HouseReadProse.COATROOM?2:1;
        h.assertTrue(sentences.length==expected,"one breath, or two from the hall on: "+body);
        for(String sentence:sentences)h.assertTrue(Character.isUpperCase(sentence.charAt(0))&&sentence.endsWith(".")&&!sentence.substring(0,sentence.length()-1).contains("."),"each sentence begins and ends cleanly: "+sentence);
        h.assertTrue(!body.contains("  ")&&!body.contains(" ,")&&!body.contains(",,")&&!body.contains("..")&&!body.contains("and and"),"no seams in the stitching: "+body);
        for(var c:clauses)h.assertTrue(body.contains(c.phrase())||c.locator()!=null&&body.contains(c.phrase().replace(c.locator(),"")),"every room's clause is there, in its place: "+c.phrase());
        int last=-1;for(var c:clauses){int at=body.indexOf(c.locator()!=null&&!body.contains(c.phrase())?c.phrase().replace(c.locator(),""):c.phrase());h.assertTrue(at>last,"the rooms come in their own order: "+body);last=at;}
        h.assertTrue(!GENDERED.matcher(text).find(),"the House's own writing never genders the reader: "+text);
        for(String page:HouseReadProse.pages(text))h.assertTrue(page.length()<=180&&!page.isBlank(),"each page fits a book page: "+page);
        h.assertTrue(String.join(" ",HouseReadProse.pages(text)).replaceAll("\\s+"," ").equals(text.replaceAll("\\s+"," ")),"pagination keeps every word");
    }

    @GameTest(template="empty",batch="read_logic",timeoutTicks=400)
    public static void theReadSettlesOnAgreementAndEveryNightReadsAsOneBreath(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"logic_reader");var q=player(h,f,"logic_other");
        // Strengths: a second never stands alone, agreement settles, disagreement is uncertain and the next decides.
        HouseRead.signal(p,AGE,"ANALOG",true);h.assertTrue(raw(p,AGE).strength()==HouseRead.Strength.UNKNOWN,"a second signal never stands alone");
        HouseRead.signal(p,AGE,"ANALOG",false);h.assertTrue(raw(p,AGE).equals(new HouseRead.Category("ANALOG",HouseRead.Strength.TENTATIVE))&&HouseRead.category(p,AGE).isPresent(),"one signal is tentative");
        HouseRead.signal(p,AGE,"ANALOG",true);h.assertTrue(raw(p,AGE).strength()==HouseRead.Strength.SETTLED,"two that agree settle it");
        HouseRead.signal(p,HAIR,"WRAPPED",false);HouseRead.signal(p,HAIR,"BRUSHED_DRY",true);h.assertTrue(raw(p,HAIR).strength()==HouseRead.Strength.UNCERTAIN&&HouseRead.hair(p,false).isEmpty()&&HouseRead.category(p,HAIR).isEmpty(),"two that disagree are uncertain, and read as unknown");
        HouseRead.signal(p,HAIR,"BRUSHED_DRY",true);h.assertTrue(raw(p,HAIR).equals(new HouseRead.Category("BRUSHED_DRY",HouseRead.Strength.SETTLED)),"the next decides, settled when seen twice");
        HouseRead.signal(p,HOME,"HAVEN",false);HouseRead.signal(p,HOME,"DANGER",true);HouseRead.signal(p,HOME,"RUNAWAY",true);h.assertTrue(raw(p,HOME).equals(new HouseRead.Category("RUNAWAY",HouseRead.Strength.TENTATIVE)),"a third, different signal decides tentatively");
        HouseRead.signal(p,SELF,"FEM",false);HouseRead.restart(p,SELF,"UNSEEN");h.assertTrue(raw(p,SELF).equals(new HouseRead.Category("UNSEEN",HouseRead.Strength.TENTATIVE))&&HouseRead.signals(p,SELF)==1,"a room revising its own first instinct starts the trait again");
        HouseRead.signal(p,DRAWN_TO,"NOT_A_READ",false);h.assertTrue(raw(p,DRAWN_TO).strength()==HouseRead.Strength.UNKNOWN,"an unknown value is never stored");
        h.assertTrue(raw(q,AGE).strength()==HouseRead.Strength.UNKNOWN&&HouseRead.seen(f.data(),q.getUUID()).isEmpty(),"another reader's record is untouched");
        // The server option: a trait turned off is never inferred and its saved read is ignored.
        HouseRead.signal(q,DRAWN_TO,"WOMEN",false);HouseRead.signal(q,DRAWN_TO,"WOMEN",true);
        if(HouseConfig.SPEC.isLoaded()){
            HouseConfig.READ_DRAWN_TO.set(false);try{HouseRead.signal(p,DRAWN_TO,"MEN",false);h.assertTrue(HouseRead.signals(p,DRAWN_TO)==0&&HouseRead.drawnTo(q).isEmpty()&&HouseRead.category(p,AGE).isPresent(),"with who a reader is drawn to turned off, nothing about it is read or used; the rest still is");}finally{HouseConfig.READ_DRAWN_TO.set(true);}
            HouseConfig.READ_READERS.set(false);try{HouseRead.signal(q,HOME,"HAVEN",false);h.assertTrue(HouseRead.signals(q,HOME)==0&&HouseRead.category(p,AGE).isEmpty(),"with inference off nobody is read");}finally{HouseConfig.READ_READERS.set(true);}
        }
        h.assertTrue(HouseRead.drawnTo(q).equals("WOMEN"),"turned back on, the kept read returns");
        f.reload();var d=f.data();h.assertTrue(HouseRead.raw(d,p.getUUID(),AGE).strength()==HouseRead.Strength.SETTLED,"the read survives a saved-data reload");
        // The account opening: the owner's own example night, then every combination of rooms and every branch of every room.
        var owner=tag();owner.put("waiting",tag("First","paper","Kept","paper","Waited",true));owner.put("bathroom",tag("Item",0,"Used",0,"LightOff",true));owner.put("coatroom",tag("Coat","trench"));owner.put("shelter",tag("Stood","woman","Song",true));owner.put("porch",tag("First","in"));
        h.assertTrue(HouseReadProse.opening(owner,false,"").equals(OWNER),"the owner's night reads exactly as the owner wrote it: "+HouseReadProse.opening(owner,false,""));
        h.assertTrue(HouseReadProse.opening(owner,true,"HAVEN").contains("stood by the woman under the eaves")&&HouseReadProse.opening(owner,true,"HAVEN").endsWith(HouseReadProse.close("HAVEN")),"an older reader's stranger is the woman, and HOME closes the opening");
        var all=variants();String[] keys={"waiting","bathroom","coatroom","shelter","porch"};String[] homes={"","HAVEN","DANGER","RUNAWAY"};
        for(int mask=1;mask<32;mask++){var seen=new CompoundTag();for(int r=0;r<5;r++)if((mask&1<<r)!=0)seen.put(keys[r],all.get(r).get(mask%all.get(r).size()));reads(h,seen,mask%3==0,homes[mask%4]);}
        for(int r=0;r<5;r++)for(var v:all.get(r)){var seen=owner.copy();seen.put(keys[r],v);reads(h,seen,r==3,homes[r%4]);var alone=new CompoundTag();alone.put(keys[r],v);reads(h,alone,false,"");}
        h.assertTrue(HouseReadProse.opening(new CompoundTag(),false,"HAVEN").isEmpty()&&HouseReadProse.pages("").isEmpty(),"a reader the House could not read keeps today's opening");
        // The account itself: two readers with different nights open differently; an unread reader's opens as today.
        night(d,p.getUUID(),owner);var other=owner.copy();other.put("waiting",tag("First","phone","Kept","phone","Waited",true));other.put("coatroom",tag("Coat","wool","Mirror","turned_after"));other.put("porch",tag("First","road","Trees",true));night(d,q.getUUID(),other);
        var first=WitnessAccount.book(d,p.getUUID(),"reader",false).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().getFirst().raw().getString();var second=WitnessAccount.book(d,q.getUUID(),"other",false).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().getFirst().raw().getString();
        var unread=WitnessAccount.book(d,new UUID(159,1),"unread",false).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().getFirst().raw().getString();
        h.assertTrue(first.startsWith("You read the local page")&&!first.equals(second)&&unread.startsWith("AN ACCOUNT"),"two friends' accounts open with their own nights; an unread one opens as before");
        // The House of Leaves: unchanged with no record, and two readers' leaves differ where the read writes them.
        var facts=List.of(StaircaseProse.fact("walked",9000,"").orElseThrow(),StaircaseProse.fact("bread",40,"").orElseThrow(),StaircaseProse.fact("deaths",3,"").orElseThrow(),StaircaseProse.fact("manor",4,"").orElseThrow());
        var a=new StaircaseProse.Read(3,"trench","","woman",true,false,"in","kitchen","HAVEN");var b=new StaircaseProse.Read(9,"wool","turned_after","lantern",false,true,"road","road","RUNAWAY");
        var endings=new HashSet<String>();
        for(long seed=0;seed<16;seed++){
            var plain=StaircaseProse.compose("reader",facts,seed);h.assertTrue(plain.equals(StaircaseProse.compose("reader",facts,seed,StaircaseProse.Read.NONE)),"with no read the book is today's book");
            var withA=StaircaseProse.compose("reader",facts,seed,a);var withB=StaircaseProse.compose("reader",facts,seed,b);
            h.assertTrue(withA.leaves().get(1).contains("the scalp oil")&&withA.leaves().get(1).contains("the trench coat")&&withA.leaves().get(2).contains("beside the girl")&&withA.leaves().get(3).contains("through the screen door"),"the hands, the eaves and the door are the reader's own: "+withA.leaves());
            h.assertTrue(withB.leaves().get(1).contains("turned the glass")||withB.leaves().get(1).contains("pocket comb"),"another reader's hands are their own");h.assertTrue(withB.leaves().get(2).contains("lantern")&&withB.leaves().get(3).contains("gravel"),"and so are their eaves and their door");
            h.assertTrue(!withA.leaves().get(4).equals(plain.leaves().get(4))&&!withA.leaves().get(4).equals(withB.leaves().get(4)),"the last leaf changes with home");endings.add(withA.voice()+":"+withA.leaves().get(4));
            for(var story:List.of(withA,withB))for(String leaf:story.leaves())h.assertTrue(!GENDERED.matcher(leaf).find(),"the book never genders the reader: "+leaf);
        }
        h.assertTrue(StaircaseAccount.read(d,p.getUUID()).routine()==0&&StaircaseAccount.read(d,p.getUUID()).door().equals("in")&&StaircaseAccount.read(d,new UUID(159,1)).equals(StaircaseProse.Read.NONE),"the book takes only the reader's own observations");
        // The House's version of the reader, sent only to that reader.
        HouseRead.restart(q,SELF,"MASC");HouseRead.signal(q,SELF,"MASC",true);HouseRead.restart(q,AGE,"ANALOG");var look=HouseReadLook.of(q);
        h.assertTrue(look.coat().equals("wool")&&look.glasses()&&!look.unseen(),"a settled presentation wears the coat taken; an older read wears glasses: "+look);
        HouseRead.restart(q,SELF,"UNSEEN");h.assertTrue(HouseReadLook.of(q).unseen()&&HouseReadLook.of(q).coat().equals("wool"),"a reader the glass did not agree with keeps the coat there, and has no face");
        looks(p);looks(q);HouseReadLook.send(q);long toQ=looks(q),toP=looks(p);h.assertTrue(toQ==1&&toP==0,"a reader's look goes only to their own client: "+toQ+" / "+toP);
        // A settled RUNAWAY is dealt roads and open air twice as often; nothing else changes.
        int before=LabyrinthDealer.dealWeight(d,q.getUUID(),LabyrinthPlace.PLAIN),indoor=LabyrinthDealer.dealWeight(d,q.getUUID(),LabyrinthPlace.HARRIGAN);
        HouseRead.restart(q,HOME,"RUNAWAY");h.assertTrue(LabyrinthDealer.dealWeight(d,q.getUUID(),LabyrinthPlace.PLAIN)==before,"a tentative read changes no deal");
        HouseRead.signal(q,HOME,"RUNAWAY",true);h.assertTrue(LabyrinthDealer.dealWeight(d,q.getUUID(),LabyrinthPlace.PLAIN)==before*2&&LabyrinthDealer.dealWeight(d,q.getUUID(),LabyrinthPlace.HARRIGAN)==indoor,"settled, the road and open air come up twice as often");
        h.succeed();
    }

    @GameTest(template="empty",batch="read_eaves",timeoutTicks=4000)
    public static void standingBesideTheWomanThroughTheSongReadsItAndTheStrangersAreTheReadersAge(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"eaves_reader");var at=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(at).inflate(8));BlockPos[] b={null};LabyrinthData.Door[] from={null};
        HouseRead.signal(p,AGE,"ANALOG",false);
        h.onEachTick(()->{transition(p);if(b[0]!=null&&visit(p,HouseReadSites.Room.SHELTER).hasUUID("Woman")&&f.in.getEntity(visit(p,HouseReadSites.Room.SHELTER).getUUID("Woman")) instanceof LiteraryActor w)p.lookAt(EntityAnchorArgument.Anchor.EYES,w.getEyePosition());});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"source loaded")).thenExecute(()->{from[0]=source(f);stand(p,f.in,at,.5,0,2);HouseReadSites.prepare(p,HouseReadSites.Room.SHELTER);})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.SHELTER),"the shelter is built"))
          .thenExecute(()->{b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.SHELTER);f.chunks.hold(f.in,HouseReadRooms.bounds(b[0],HouseReadSites.Room.SHELTER));seed(p,HouseReadSites.Room.SHELTER,from[0]);stand(p,f.in,b[0],8,0,-8.5);})
          .thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.SHELTER).hasUUID("Woman")&&visit(p,HouseReadSites.Room.SHELTER).hasUUID("Man")&&f.in.getEntity(visit(p,HouseReadSites.Room.SHELTER).getUUID("Woman"))!=null&&f.in.getEntity(visit(p,HouseReadSites.Room.SHELTER).getUUID("Man"))!=null,"both strangers are there"))
          .thenExecute(()->{var t=visit(p,HouseReadSites.Room.SHELTER);for(String key:List.of("Man","Woman")){var a=(LiteraryActor)f.in.getEntity(t.getUUID(key));f.extras.add(a);h.assertTrue(a.phase()==HouseReadShelter.phase(2,false),"an older reader's strangers are older too");}})
          .thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.SHELTER).getInt("SongAge")>=900,"the whole song plays"))
          .thenExecute(()->{
              var seen=HouseRead.seen(f.data(),p.getUUID()).getCompound("shelter");var read=raw(p,DRAWN_TO);
              h.assertTrue(seen.getString("Looked").equals("woman")&&seen.getString("Stood").equals("woman")&&seen.getBoolean("Song"),"her share of the reader's gaze and time, and staying beside her to the end, are kept: "+seen);
              h.assertTrue(read.equals(new HouseRead.Category("WOMEN",HouseRead.Strength.SETTLED))&&HouseRead.drawnTo(p).equals("WOMEN"),"the first share and the song's end agree and settle it: "+read);
              var woman=(LiteraryActor)f.in.getEntity(visit(p,HouseReadSites.Room.SHELTER).getUUID("Woman"));h.assertTrue(woman.phase()==HouseReadShelter.phase(2,true),"she gives the reader her half-smile");
              h.assertTrue(HouseReadProse.opening(f.data(),p.getUUID()).equals("Under the eaves, you stood by the woman until the song finished."),"the account says who the reader stood by, by the reader's age: "+HouseReadProse.opening(f.data(),p.getUUID()));
          }).thenSucceed();
    }

    @GameTest(template="empty",batch="read_yard",timeoutTicks=6000)
    public static void thePorchReadsWhatEachReaderDoesFirstWhenTheWhistleBlows(GameTestHelper h){
        var f=fixture(h);var in=player(h,f,"yard_in");var hid=player(h,f,"yard_hid");var ran=player(h,f,"yard_ran");var readers=List.of(in,hid,ran);var at=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(at).inflate(8));Map<UUID,BlockPos> base=new HashMap<>();LabyrinthData.Door[] from={null};
        h.onEachTick(()->readers.forEach(HouseReadReaderTests::transition));
        var sequence=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"source loaded")).thenExecute(()->{from[0]=source(f);for(var p:readers){stand(p,f.in,at,.5,0,2);HouseReadSites.prepare(p,HouseReadSites.Room.PORCH);}});
        for(var p:readers)sequence.thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.PORCH),"each reader's own yard is built"));
        sequence.thenExecute(()->{for(var p:readers){var b=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.PORCH);base.put(p.getUUID(),b);f.chunks.hold(f.out,HouseReadRooms.bounds(b,HouseReadSites.Room.PORCH));seed(p,HouseReadSites.Room.PORCH,from[0]);stand(p,f.out,b,-9.5,0,2.5);}})
          .thenWaitUntil(()->h.assertTrue(readers.stream().allMatch(p->visit(p,HouseReadSites.Room.PORCH).contains("Whistle")),"reaching the swing set brings the whistle at once"))
          .thenExecute(()->{stand(in,f.out,base.get(in.getUUID()),.5,3,-12.5);stand(hid,f.out,base.get(hid.getUUID()),.5,0,-5);stand(ran,f.out,base.get(ran.getUUID()),13.5,0,-20);})
          .thenIdle(5).thenExecute(()->{
              h.assertTrue(raw(in,HOME).equals(new HouseRead.Category("HAVEN",HouseRead.Strength.TENTATIVE)),"going in when the whistle blows is a haven, tentatively");
              h.assertTrue(raw(hid,HOME).equals(new HouseRead.Category("DANGER",HouseRead.Strength.TENTATIVE)),"ducking under the porch is danger, tentatively");
              h.assertTrue(raw(ran,HOME).equals(new HouseRead.Category("RUNAWAY",HouseRead.Strength.TENTATIVE)),"walking down the gravel to the road is running, tentatively");
              stand(ran,f.out,base.get(ran.getUUID()),13.5,0,-30.5);
          }).thenIdle(3).thenExecute(()->h.assertTrue(raw(ran,HOME).equals(new HouseRead.Category("RUNAWAY",HouseRead.Strength.SETTLED)),"past the mailbox into the trees without turning back settles it"))
          .thenWaitUntil(()->h.assertTrue(visit(hid,HouseReadSites.Room.PORCH).getBoolean("LightsOut"),"the kitchen light goes off over the hidden reader"))
          .thenExecute(()->{
              h.assertTrue(raw(hid,HOME).equals(new HouseRead.Category("DANGER",HouseRead.Strength.SETTLED)),"still hidden when the light goes off settles it");
              h.assertTrue(HouseReadProse.opening(f.data(),hid.getUUID()).endsWith(HouseReadProse.close("DANGER"))&&HouseReadProse.opening(f.data(),ran.getUUID()).contains("walked down the gravel to the road when the whistle blew and kept on past the mailbox into the trees without looking back"),"each account recites that reader's own yard, and closes with what home was");
              h.assertTrue(StaircaseAccount.read(f.data(),ran.getUUID()).door().equals("road")&&StaircaseAccount.read(f.data(),in.getUUID()).door().equals("in"),"and each book's door is theirs");
              h.assertTrue(LabyrinthDealer.dealWeight(f.data(),ran.getUUID(),LabyrinthPlace.PLAIN)==2*LabyrinthDealer.dealWeight(f.data(),in.getUUID(),LabyrinthPlace.PLAIN),"the reader who took the road is dealt the open air more often");
          }).thenSucceed();
    }

    @GameTest(template="empty",batch="read_glass",timeoutTicks=4000)
    public static void theHallGlassTurnedBeforeOrAfterDressingAndAMixedOutfitAreRead(GameTestHelper h){
        var f=fixture(h);var unseen=player(h,f,"glass_before");var mixed=player(h,f,"glass_mixed");var at=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(at).inflate(8));Map<UUID,BlockPos> base=new HashMap<>();LabyrinthData.Door[] from={null};
        h.onEachTick(()->{transition(unseen);transition(mixed);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"source loaded")).thenExecute(()->{from[0]=source(f);for(var p:List.of(unseen,mixed)){stand(p,f.in,at,.5,0,2);HouseReadSites.prepare(p,HouseReadSites.Room.COATROOM);}})
          .thenWaitUntil(()->h.assertTrue(ready(unseen,HouseReadSites.Room.COATROOM),"the first coatroom is built")).thenWaitUntil(()->h.assertTrue(ready(mixed,HouseReadSites.Room.COATROOM),"the second coatroom is built"))
          .thenExecute(()->{
              for(var p:List.of(unseen,mixed)){var b=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.COATROOM);base.put(p.getUUID(),b);seed(p,HouseReadSites.Room.COATROOM,from[0]);}
              var b=base.get(unseen.getUUID());h.assertTrue(f.in.getBlockState(b.offset(3,0,-2)).getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.BENCH,"the hall has its wooden bench");stand(unseen,f.in,b,2.5,0,-6.5);click(unseen,b.offset(HouseReadRooms.MIRROR));
              h.assertTrue(raw(unseen,SELF).equals(new HouseRead.Category("UNSEEN",HouseRead.Strength.SETTLED)),"turning the glass to the wall before dressing settles it");
              var peg=b.offset(HouseReadRooms.PEGS.get(0));stand(unseen,f.in,b,-1.5,0,-1.5);click(unseen,peg);var took=((HouseReadRegistry.Garment)unseen.getItemBySlot(EquipmentSlot.CHEST).getItem()).cloth;
              h.assertTrue(raw(unseen,SELF).value().equals("UNSEEN")&&HouseRead.seen(f.data(),unseen.getUUID()).getCompound("coatroom").getString("CoatId").equals(took)&&HouseRead.seen(f.data(),unseen.getUUID()).getCompound("coatroom").getString("Coat").equals(HouseReadWardrobe.SETS[HouseReadWardrobe.set(took)]),"the coat taken afterwards is only observed, by itself and by its set");
              h.assertTrue(HouseReadLook.of(unseen).unseen()&&HouseReadLook.of(unseen).coat().equals(took),"in glass and copies the coat shows and the face does not");
              h.assertTrue(HouseReadProse.coatroom(HouseRead.seen(f.data(),unseen.getUUID()).getCompound("coatroom")).startsWith("turned the hall glass to the wall before you took"),"the account says so in that order");
              // The second reader takes a coat from the wool or trench set and boots from the other of those two sets.
              var m=base.get(mixed.getUUID());BlockPos coat=null,boots=null;int coatSet=-1;
              for(var rel:HouseReadRooms.PEGS){var g=HouseReadWardrobe.of(f.in.getBlockState(m.offset(rel)).getValue(HouseReadPropBlock.PART));if(coat==null&&g!=null&&g.set()<2){coat=m.offset(rel);coatSet=g.set();}}
              for(var rel:HouseReadWardrobe.BOOT_PLACES){var g=HouseReadWardrobe.of(f.in.getBlockState(m.offset(rel)).getValue(HouseReadPropBlock.PART));if(g!=null&&g.set()<2&&g.set()!=coatSet)boots=m.offset(rel);}
              h.assertTrue(coat!=null&&boots!=null,"the wardrobe has a coat from one of those sets and boots from the other");
              stand(mixed,f.in,m,-.5,0,-2);click(mixed,coat);h.assertTrue(raw(mixed,SELF).equals(new HouseRead.Category(coatSet==0?"MASC":"FEM",HouseRead.Strength.TENTATIVE)),"the first coat sets the presentation tentatively");
              click(mixed,boots);h.assertTrue(raw(mixed,SELF).equals(new HouseRead.Category("ANDROGYNOUS",HouseRead.Strength.TENTATIVE)),"one garment from each side reads as neither: "+raw(mixed,SELF));
              stand(mixed,f.in,m,2.5,0,-6.5);click(mixed,m.offset(HouseReadRooms.MIRROR));h.assertTrue(raw(mixed,SELF).equals(new HouseRead.Category("UNSEEN",HouseRead.Strength.TENTATIVE))&&HouseRead.seen(f.data(),mixed.getUUID()).getCompound("coatroom").getString("Mirror").equals("turned_after"),"turning the glass after dressing makes UNSEEN the House's first guess");
          }).thenSucceed();
    }
}
