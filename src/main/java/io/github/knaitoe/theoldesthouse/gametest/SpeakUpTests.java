package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.327, Speak Up (docs/SPEAK_UP_0_4_327.md). The dealing: a reader who has begun the quiet rooms is held back from every scene
 * that asks them to speak until they have spoken; after the quiet rooms the dining room comes on a story door, among the unvoiced
 * stories, within three of them, and never again after.
 * The room: two readers at their own tables, each talked across, each finally asked, each answering with a real consequence, and each
 * hearing their own answer given back by the empty chair.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class SpeakUpTests {
    private SpeakUpTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="speak_up_table") public static void tableDone(ServerLevel l){if(active!=null){active.close();active=null;}Talk.clearAll();}

    private static void enterEveryQuietRoom(LabyrinthData d,UUID id){
        var own=d.stateEntry(HouseReadSites.STATE,id.toString());
        for(var room:HouseReadSites.CHAIN){var visit=own.getCompound(room.key());visit.putBoolean("Entered",true);own.put(room.key(),visit);}
        own.remove("Owed");d.setStateEntry(HouseReadSites.STATE,id.toString(),own);
    }
    private static List<LabyrinthData.Door> doors(GameTestHelper h,UUID id,String tag,int n){
        var out=new ArrayList<LabyrinthData.Door>();
        for(int i=0;i<n;i++)out.add(new LabyrinthData.Door("native/speak_up/"+tag+"/"+id+"/"+i,h.getLevel().dimension(),new BlockPos(0,-60,i*4),Direction.NORTH,LabyrinthData.DEALT,false));
        return out;
    }
    private static long dealtTo(LabyrinthData d,UUID id,List<LabyrinthData.Door> doors,LabyrinthPlace place){return doors.stream().map(door->d.deal(id,door)).filter(Objects::nonNull).filter(deal->deal.place().equals(place.id())).count();}

    /** One hall's worth of doors dealt at story pace (the dry spell spent, so a story door is certain); what the story door opened on. */
    private static LabyrinthPlace storyDoor(GameTestHelper h,LabyrinthData d,UUID u,String tag,int seed){
        d.setDryDeals(u,100);var hall=doors(h,u,tag,3);LabyrinthDealer.deal(d,u,hall,LabyrinthPlace.JUNCTION,RandomSource.create(seed));
        LabyrinthPlace story=null;
        for(var door:hall){var deal=d.deal(u,door);var place=deal==null?null:LabyrinthPlace.byId(deal.place());if(place==null)continue;
            h.assertTrue(!SpeakUp.needsVoice(place)||!SpeakUp.held(d,u),"no scene that asks for a spoken answer while the reader has none: "+place);
            if(place==SpeakUp.PLACE||place.isVignette())story=place;}
        return story;
    }

    @GameTest(template="empty",batch="speak_up_dealing")
    public static void afterTheQuietRoomsTheTableComesAmongTheStoriesAndUnlocksTheScenesThatAskTheReaderToSpeak(GameTestHelper h){
        var d=new LabyrinthData();var u=UUID.randomUUID();
        h.assertTrue(SpeakUp.PLACE.isGray()&&!SpeakUp.PLACE.isVignette()&&!NovelRooms.outside(SpeakUp.PLACE),"an ordinary indoor room, not a story");
        h.assertTrue(!LabyrinthDealer.grayAvailable(d,u).contains(SpeakUp.PLACE),"never dealt as a hall: only to the reader it is owed to");
        for(var place:List.of(LabyrinthPlace.GHOSTS_SET,LabyrinthPlace.SECOND_PERSON,LabyrinthPlace.CONFESSION,LabyrinthPlace.PLACE_FOR_YOU,LabyrinthPlace.TRUANT_DOOR))h.assertTrue(SpeakUp.needsVoice(place),"a scene that asks for a spoken answer: "+place);
        h.assertTrue(!SpeakUp.needsVoice(LabyrinthPlace.JUNCTION)&&!SpeakUp.needsVoice(LabyrinthPlace.GOATMAN),"other places do not wait for it");
        // An older record, or a fixture that never began the quiet rooms, is neither held back nor sent to the table.
        h.assertTrue(!SpeakUp.held(d,u)&&!SpeakUp.owed(d,u),"a reader who never began the quiet rooms is not held back");
        // Partway through the quiet rooms: held, and the quiet rooms still come first.
        var own=d.stateEntry(HouseReadSites.STATE,u.toString());var kitchen=own.getCompound(HouseReadSites.Room.KITCHEN.key());kitchen.putBoolean("Entered",true);own.put(HouseReadSites.Room.KITCHEN.key(),kitchen);d.setStateEntry(HouseReadSites.STATE,u.toString(),own);
        h.assertTrue(SpeakUp.held(d,u)&&!SpeakUp.owed(d,u),"partway through the quiet rooms the reader is held, and the quiet rooms still come first");
        enterEveryQuietRoom(d,u);
        h.assertTrue(SpeakUp.owed(d,u),"after the last quiet room the table is owed");
        h.assertTrue(LabyrinthDealer.vignettesAvailable(d,u).stream().noneMatch(SpeakUp::needsVoice),"no scene that asks for a spoken answer is available yet");
        h.assertTrue(!LabyrinthDealer.vignettesAvailable(d,u).isEmpty(),"but the stories that do not are");
        // In the domestic halls after the quiet rooms there are no stories yet, and so no table.
        h.assertTrue(storyDoor(h,d,u,"domestic",320)==null,"no story door, and no table, before the story depth");
        while(d.returnDepth(u)<LabyrinthPacing.STORY_DEPTH)d.pushReturn(u,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.ZERO,0));
        // At the story depth, a story door opens on the table or on an unvoiced story; across halls, either can come first.
        boolean tableFirst=false,storyFirst=false;
        for(int seed=0;seed<40&&!(tableFirst&&storyFirst);seed++){
            var t=SpeakUp.record(d,u);t.remove("Stories");d.setStateEntry(SpeakUp.STATE,u.toString(),t);
            var first=storyDoor(h,d,u,"first"+seed,3200+seed);h.assertTrue(first!=null,"the hall has a story door (seed "+seed+")");
            if(first==SpeakUp.PLACE)tableFirst=true;else storyFirst=true;
        }
        h.assertTrue(storyFirst,"an unvoiced story can come before the table");
        h.assertTrue(tableFirst,"and the table can come first");
        // However the rolls go, two other stories are the most that come before it.
        for(int run=0;run<12;run++){
            var t=SpeakUp.record(d,u);t.remove("Stories");d.setStateEntry(SpeakUp.STATE,u.toString(),t);
            int others=0;LabyrinthPlace story=null;
            for(int hall=0;hall<=SpeakUp.STORIES_FIRST&&story!=SpeakUp.PLACE;hall++){story=storyDoor(h,d,u,"run"+run+"_"+hall,3300+run*7+hall);if(story!=SpeakUp.PLACE)others++;}
            h.assertTrue(story==SpeakUp.PLACE&&others<=SpeakUp.STORIES_FIRST,"the table comes within "+(SpeakUp.STORIES_FIRST+1)+" story doors (run "+run+", "+others+" others first)");
        }
        // Once the reader has spoken: never dealt again, and nothing is held back.
        var record=SpeakUp.record(d,u);record.putBoolean("Voice",true);d.setStateEntry(SpeakUp.STATE,u.toString(),record);
        h.assertTrue(SpeakUp.voice(d,u)&&!SpeakUp.held(d,u)&&!SpeakUp.owed(d,u),"a reader with a voice is neither held nor owed");
        for(int seed=0;seed<8;seed++)h.assertTrue(storyDoor(h,d,u,"after"+seed,3400+seed)!=SpeakUp.PLACE,"the table is never dealt again (seed "+seed+")");
        h.succeed();
    }

    @GameTest(template="empty",batch="speak_up_table",timeoutTicks=1800)
    public static void theTableAnswersForTheReaderUntilRuthSaysYouCanAnswerAndTheEmptyChairRepeatsIt(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=SpeakUp.PLACE;var l=f.in;var b=f.base(place);
        var a=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(l,new AABB(a.minX(),a.minY(),a.minZ(),a.maxX()+1,a.maxY()+1,a.maxZ()+1));
        var p=NativeTestPlayers.survival(h,"speaks_first");var q=NativeTestPlayers.survival(h,"speaks_second");f.players.add(p);f.players.add(q);
        Map<UUID,Integer> seen=new HashMap<>();
        java.util.function.BiFunction<ServerPlayer,Integer,LiteraryActor> cast=(reader,phase)->l.getEntitiesOfClass(LiteraryActor.class,SpeakUp.room(b).inflate(2),x->x.owner().map(reader.getUUID()::equals).orElse(false)&&x.phase()==phase).stream().findFirst().orElse(null);
        java.util.function.Function<ServerPlayer,CompoundTag> rec=reader->SpeakUp.record(f.data(),reader.getUUID());
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the dining room is resident"))
            .thenExecute(()->{
                SpeakUp.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(l.getBlockState(b.offset(SpeakUp.DELL_CHAIR)).is(HouseBlocks.HOUSEHOLD_FURNITURE.get()),"a chair stands in front of the way on");
                h.assertTrue(l.getBlockState(b.offset(SpeakUp.WAY_ON)).getBlock() instanceof net.minecraft.world.level.block.DoorBlock,"the way on is a door in the north wall");
                h.assertTrue(f.data().door(place.id()+"/far")!=null&&f.data().door(place.id()+"/far").dimension.equals(HouseDimensions.INTERIOR),"its far door is registered indoors");
                for(var reader:List.of(p,q)){enterEveryQuietRoom(f.data(),reader.getUUID());h.assertTrue(SpeakUp.owed(f.data(),reader.getUUID()),"each reader is owed the table");}
                p.teleportTo(l,b.getX()-2.5,b.getY(),b.getZ()-8.5,-90,0);q.teleportTo(l,b.getX()-2.5,b.getY(),b.getZ()-6.5,-90,0);
                for(var reader:List.of(p,q)){reader.hasChangedDimension();reader.setDeltaMovement(Vec3.ZERO);SpeakUp.arrived(reader,place);}
            })
            .thenWaitUntil(()->{for(var reader:List.of(p,q))for(int phase=SpeakUp.JOAN;phase<=SpeakUp.VOICE;phase++)h.assertTrue(cast.apply(reader,phase)!=null,"each reader's table is laid with its own four and the voice ("+reader.getScoreboardName()+", "+phase+")");})
            .thenExecute(()->{
                for(var reader:List.of(p,q)){
                    var own=l.getEntitiesOfClass(LiteraryActor.class,SpeakUp.room(b).inflate(2),x->x.scene().equals(place.id())&&x.owner().map(reader.getUUID()::equals).orElse(false));
                    h.assertTrue(own.size()==5,"exactly five at "+reader.getScoreboardName()+"'s table: "+own.size());
                    seen.put(reader.getUUID(),own.size());
                }
                var ruth=cast.apply(p,SpeakUp.RUTH);h.assertTrue(ruth.blockPosition().equals(b.offset(SpeakUp.RUTH_CHAIR)),"Ruth sits opposite the reader's place");
                h.assertTrue(cast.apply(p,SpeakUp.VOICE).fade()==100,"the empty chair's voice is not seen");
            })
            .thenWaitUntil(()->h.assertTrue(rec.apply(p).getInt("T")>165,"the table talks: "+rec.apply(p).getInt("T")))
            .thenExecute(()->{
                var lines=Talk.lines(p);
                h.assertTrue(lines.contains("Ruth: There's room. Sit down."),"Ruth offers the seat: "+lines);
                h.assertTrue(lines.contains("Pat: Would you like something to drink?")&&lines.contains("Joan: They're fine."),"someone else answers the question put to the reader: "+lines);
                h.assertTrue(Talk.replies(p).isEmpty()&&rec.apply(p).getInt("Stage")==0,"and the reader is given nothing to say yet");
                h.assertTrue(lines.stream().noneMatch(line->line.startsWith("you:")),"nothing is said in the reader's name");
                // Skip the rest of the talking across.
                for(var reader:List.of(p,q)){var t=rec.apply(reader);t.putInt("T",430);f.data().setStateEntry(SpeakUp.STATE,reader.getUUID().toString(),t);}
            })
            .thenWaitUntil(()->{for(var reader:List.of(p,q))h.assertTrue(rec.apply(reader).getInt("Stage")==1,"Ruth asks "+reader.getScoreboardName()+" at last");})
            .thenExecute(()->{
                h.assertTrue(Talk.lines(p).contains("Ruth: You can answer."),"Ruth says it: "+Talk.lines(p));
                h.assertTrue(Talk.replies(p).equals(List.of(SpeakUp.WATER,SpeakUp.FINE,SpeakUp.LEAVE)),"three replies for a reader the House does not read as unseen: "+Talk.replies(p));
                h.assertTrue(!SpeakUp.voice(f.data(),p.getUUID()),"no voice before an answer");
                // One reader asks to leave: Dell clears the way on. The other asks for water and is given a glass.
                h.assertTrue(Talk.choose(p,"I'd like to leave"),"the first reader can ask to leave");
                // 0.4.329: what a reader says from the panel shows over their head for the other reader, never for themselves.
                h.assertTrue(Spoken.lastHeard(q,p).equals(SpeakUp.LEAVE),"the other reader sees the words over the first reader's head: \""+Spoken.lastHeard(q)+"\"");
                h.assertTrue(Spoken.lastHeard(p).isEmpty(),"the speaker is not sent their own words: \""+Spoken.lastHeard(p)+"\"");
                h.assertTrue(SpeakUp.voice(f.data(),p.getUUID())&&!SpeakUp.owed(f.data(),p.getUUID()),"the answer gives the reader a voice, and the table is no longer owed");
                h.assertTrue(!SpeakUp.voice(f.data(),q.getUUID()),"one reader's answer is not the other's");
                h.assertTrue(l.getBlockState(b.offset(SpeakUp.DELL_CHAIR)).isAir()&&l.getBlockState(b.offset(SpeakUp.DELL_MOVED)).is(HouseBlocks.HOUSEHOLD_FURNITURE.get()),"Dell's chair is dragged aside");
                var at=Vec3.atBottomCenterOf(b.offset(SpeakUp.DELL_CHAIR));h.assertTrue(l.noCollision(null,new AABB(at.x-.3,at.y+.01,at.z-.3,at.x+.3,at.y+1.8,at.z+.3)),"and the way on is clear to walk to");
                h.assertTrue(Talk.choose(q,"Some water"),"the second reader can ask for water");
                h.assertTrue(Spoken.lastHeard(p,q).equals(SpeakUp.WATER),"and the first reader sees those words: \""+Spoken.lastHeard(p)+"\"");
                h.assertTrue(q.getInventory().items.stream().anyMatch(s->s.is(Items.POTION)&&s.has(DataComponents.CUSTOM_NAME)&&s.getHoverName().getString().equals("A glass of water")),"and is handed a glass of water to keep");
                for(var reader:List.of(p,q)){var t=rec.apply(reader);t.putInt("T2",190);f.data().setStateEntry(SpeakUp.STATE,reader.getUUID().toString(),t);}
            })
            .thenWaitUntil(()->{for(var reader:List.of(p,q))h.assertTrue(rec.apply(reader).getInt("Stage")==3,"the supper goes on for "+reader.getScoreboardName()+": "+rec.apply(reader).getInt("T2"));})
            .thenExecute(()->{
                // The empty chair gives the reader's own answer back, word for word, as one of their replies.
                h.assertTrue(Talk.lines(p).contains("Joan: Would you like something to drink?"),"Joan asks the empty chair: "+Talk.lines(p));
                h.assertTrue(Talk.lines(p).stream().filter(line->line.equals("you: "+SpeakUp.LEAVE)).count()==2,"the reader's answer, and the chair's in their words: "+Talk.lines(p));
                h.assertTrue(Talk.lines(q).stream().filter(line->line.equals("you: "+SpeakUp.WATER)).count()==2,"each chair repeats its own reader: "+Talk.lines(q));
                h.assertTrue(Talk.lines(p).stream().noneMatch(line->line.contains(SpeakUp.WATER))&&Talk.lines(q).stream().noneMatch(line->line.contains(SpeakUp.LEAVE)),"and never the other reader's answer");
                var voice=cast.apply(p,SpeakUp.VOICE);h.assertTrue(voice.getCustomName()!=null&&voice.getCustomName().getString().equals(SpeakUp.LEAVE),"the words show over the empty chair");
                // They have turned to the empty chair.
                var pat=cast.apply(p,SpeakUp.PAT);var to=Vec3.atCenterOf(b.offset(SpeakUp.EMPTY)).subtract(pat.getEyePosition());float yaw=(float)Math.toDegrees(Math.atan2(-to.x,to.z));
                h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(pat.getYHeadRot()-yaw))<20,"Pat has turned to the empty chair: "+pat.getYHeadRot()+" vs "+yaw);
                h.assertTrue(cast.apply(p,SpeakUp.DELL).blockPosition().equals(b.offset(2,0,-11)),"Dell stands clear of the way on");
                // Coming back with a voice, the table is already eating.
                SpeakUp.arrived(p,place);h.assertTrue(rec.apply(p).getInt("Stage")==3&&SpeakUp.voice(f.data(),p.getUUID()),"a reader who has spoken finds them eating");
                h.assertTrue(Spoken.lastHeard(p,q).equals(SpeakUp.WATER)&&Spoken.lastHeard(q,p).equals(SpeakUp.LEAVE),"the empty chairs' words are not the readers' own speech");
                // A reply that is something done, not said, shows nothing over the reader's head.
                Talk.begin(q,"spoken_probe","The table");Talk.ask(q,Talk.act("Take the glass.",Talk::end));
                h.assertTrue(Talk.choose(q,"Take the glass")&&Spoken.lastHeard(p,q).equals(SpeakUp.WATER),"something done is not heard: \""+Spoken.lastHeard(p)+"\"");
            })
            .thenSucceed();
    }
}
