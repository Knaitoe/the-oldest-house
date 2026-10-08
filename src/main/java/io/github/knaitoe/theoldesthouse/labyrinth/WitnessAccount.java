package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Knowledge belongs to the explorer, never to a traded book or a world's room flags. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class WitnessAccount {
    public static final String STATE="witness_0410", BOOK_OWNER="HouseWitnessAccount";
    public static final int REQUIRED=requiredForPoolSize(Story.values().length);
    public enum Story {
        FLOORBOARDS("floorboards", "The floor", "understanding",
                "I took up the board. The sound ended. The words underneath belonged to someone who had stayed to care for him."),
        CLAP("hide_and_clap", "The wardrobe", "survival",
                "The game ended at the wardrobe. There was a drawing inside. A child had left a way to remember the room."),
        HARRIGAN("harrigan", "The study", "connection",
                "Reading had given us somewhere to sit together. The telephone was still there when his voice was gone."),
        MODEL_HOME("model_home", "The window", "survival",
                "The tree had crossed the glass. A room sold as a home could no longer keep the outside out."),
        DROWNED_TOWN("drowned_town", "The lake", "understanding",
                "I dried the school essays and returned for the key. Beneath the lake, a preacher kept singing. I opened the roof, and the hymn reached the shore."),
        PRESERVED_CAVE("preserved_cave", "The congregation", "understanding",
                "I walked between the people the lake had kept. Their clothes belonged to different years. Their faces had not changed. The canoe was behind the last row."),
        SHALLOWS("shallows", "The boys", "memory",
                "I carried her to the water. I let go. The school pages remembered my name among the boys on the bank."),
        PHONE_CANOE("phone_canoe","The recording","memory",
                "I took the canoe over the drowned steeple. The telephone fell. I could still see the sky, but I could no longer turn toward the shore. Something kept recording after my hands were empty."),
        GOATMAN("goatman","The trailer","survival",
                "There were more of us than there were places at the table. Someone knocked at the door and used a voice we knew. I kept it closed until the knocking stopped. I still could not say who had been outside."),
        TED_CAVER("ted_caver","The small passage","survival",
                "I worked until the hole would take my shoulders. Beyond it, the stone moved and the line tightened. I crawled back through the same opening and climbed above the rope. I left without learning what had been pulling it."),
        ZAMPANO("zampano_courtyard","The readers","understanding","I coaxed the cat from the mat and read the survey behind the sealed windows. The gouges had been measured without anyone asking what made them."),
        WHALE("whale","The undated letter","connection","The letters found me in ordinary chests. Their beginnings made a knock. In the middle attic, I read a page without a next date."),
        BARN_WELL("barn_well","The cover","memory","I climbed below the initials. The cover shut above me. I waited until someone opened it, then climbed back into the yard."),
        PLAIN("plain","The distant frame","memory","The bird circled something beyond the dunes. I held it in the spyglass and heard the shutter. The photograph came with me. The distance remained."),
        HOSPITAL("hospital","The call button","understanding","I stayed through the ward's short night. I pressed the call button. The alarms stopped at dawn, without anyone coming through the door."),
        HOLLOWAY("holloway_camp","The counted lights","survival",
                "I took supplies from the dirt hut. When I returned, someone followed the light I had left. I passed the pillars, somebody's stairs and the broken stone. Beyond the service latch I found a shield and half a map. The missing half looked safer."),
        MOTHER("mother_of_strays", "The keeper", "release",
                "She let something go. For a moment, keeping it safe and keeping it forever were different things."),
        SEANCE("seance","The empty chair","connection",
                "They could hear the cupboard, but they could not see my hand. The album had names beneath its photographs. The final space had mine."),
        WALLPAPER("wallpaper_nursery","The pattern","understanding",
                "I watched the shape cross the seams. Under four strips of paper she had kept her own words. I read the last page before I left."),
        HOTEL("hotel","The closing account","understanding","I was in the photograph. I took the key from the snow, read the log and vented the plant. The desk had kept my missing things. I read the closing account before I left."),
        HILL_NURSERY("hill_nursery","The returned knocks","understanding","The cellar returned every knock I made. Upstairs, my name had been written beside the cold door."),
        MINIATURES("miniatures","The little rooms","memory","I recognized the rooms on the tables. The latest figure wore my face; another figure stood behind it."),
        MASQUE("masque","The last clock","release","I crossed seven colored rooms and put my hand on the clock. The next chime never came."),
        USHER("usher","The coffin lid","understanding","I returned to the consequence of the lid I had chosen. The sound had followed the stone."),
        WINCHESTER("winchester","The upper ledger","understanding","A long stair had gained one floor. The ledger at its end bought another day of construction."),
        CHILD_ROOM("child_room","The disappearing exits","survival","I crawled below the bed after the windows and doors had gone. I found a floor that still led somewhere."),
        CRIMSON_HALL("crimson_hall","The recorded voices","connection","I played all three voices through their endings. Beneath the snow, the floor was red."),
        BLY_ROUTE("bly_route","The wet boards","survival","I waited in a shut cupboard until the footsteps reached the lake room. Then I came out."),
        ELK_LOT("elk_lot","The looping field","survival","The field turned back upon itself. The clipping at the parking lot had my name and an ending I had not seen."),
        ELK_FAN("elk_fan","The broken light","understanding","I repaired the light above the carpet. On later returns the shape became tape, then a tooth."),
        MAPPING_INTERIOR("mapping_interior","The crawl beyond the roof","connection","The passage under the house ran farther than the roof. I returned to the boy after choosing what to do with his father's things."),
        HOLY_RABBIT("holy_rabbit","The food in the hollow","survival","The rabbit lived through each morning. I ate what he brought and followed the line his leg had left in the snow."),
        CONFESSION("confession","The listener's signature","understanding","I wrote until the speaker began counting the listener. The journal sealed in the wall carried my name."),
        ELK_CARCASSES("elk_carcasses","The passing boots","survival","I held still beneath the hides while the boots passed. When the search ended, I took the service path."),
        COSTUME_NIGHT("costume_night","The coats on the beach","survival","One costume moved when nobody watched it. I reached the living grass and kept it in sight."),
        MOVIE_NIGHT("movie_night","The flashes over water","survival","Each firework found it closer to the canoe. I kept rowing until the pier was beneath my hands."),
        WINTER_LAKE("winter_lake","The broken ice","survival","I broke a hole and waited beneath the ice. After she passed, I came up and crossed to the bank."),
        CAMP_BLOOD("camp_blood","The service road","survival","The essay had remembered an ordinary summer. I escaped beyond the last cabin before that summer returned."),
        DEVILS_ROCK("devils_rock","The eight loose pages","memory","I read the eight pages in order. The last one reached this room without a person bringing it."),
        WHEEL("wheel","The numbered doors","memory","I found Mother beyond the numbered doors. The front door finally opened onto an outside where I could stand up."),
        GHOSTS_SET("ghosts_set","The light and the marks","connection","The lit family kept repeating its lines. I stepped off the mark and left an account in the booth."),
        END_WORLD_CABIN("end_world_cabin","The visitors at dusk","release","I answered the visitors for myself. The screen held the consequence after my answer."),
        FAMILY_COPY("family_copy","The house they remembered","memory","They welcomed me into a house I had actually lived in. I returned until their welcome ended.");
        public final String id, title, kind, text;
        Story(String id,String title,String kind,String text){this.id=id;this.title=title;this.kind=kind;this.text=text;}
        public static @Nullable Story of(String id){for(Story story:values())if(story.id.equals(id))return story;return null;}
    }
    private WitnessAccount(){}
    /** Seventy-five percent of shipped Witness stories, rounded up as the pool grows. */
    public static int requiredForPoolSize(int availableStories){return (3*availableStories+3)/4;}
    public static CompoundTag record(LabyrinthData data,UUID player){return data.stateEntry(STATE,player.toString());}
    private static void save(LabyrinthData data,UUID player,CompoundTag record){
        data.setStateEntry(STATE,player.toString(),record);
    }
    public static boolean has(LabyrinthData data,UUID player,Story story){return record(data,player).getCompound("Stories").contains(story.id);}
    public static int count(LabyrinthData data,UUID player){
        CompoundTag stories=record(data,player).getCompound("Stories");int count=0;
        for(Story story:Story.values())if(stories.contains(story.id))count++;return count;
    }
    /** Arrival records a beginning only for this actual participant, never a resolution. */
    public static void begin(ServerPlayer player,LabyrinthPlace place){
        Story story=Story.of(place.id());
        if(story==null||player.isSpectator()||!player.isAlive()||FinaleProgress.terminal(FinaleProgress.phase(player.server,player.getUUID())))return;
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag own=record(data,player.getUUID()),begun=own.getCompound("Begun");
        if(begun.getBoolean(story.id)||has(data,player.getUUID(),story))return;
        begun.putBoolean(story.id,true);own.put("Begun",begun);save(data,player.getUUID(),own);updateBook(player,false);
    }
    public static boolean begun(LabyrinthData data,UUID player,Story story){
        LabyrinthPlace place=LabyrinthPlace.byId(story.id);
        return record(data,player).getCompound("Begun").getBoolean(story.id)
                ||place!=null&&data.visited(player).contains(place.id())
                ||HouseExperience.record(data,player).getCompound("Retreats").getInt(story.id)>0;
    }
    private static Set<String> kinds(LabyrinthData data,UUID player){
        Set<String> kinds=new HashSet<>();CompoundTag stories=record(data,player).getCompound("Stories");
        for(Story story:Story.values())if(stories.contains(story.id))kinds.add(story.kind);return kinds;
    }
    private static String words(int n){
        String[] small={"no","one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen"};
        if(n<20)return small[n];String[] tens={"","","twenty","thirty","forty","fifty","sixty","seventy","eighty","ninety"};
        if(n>=100)return Integer.toString(n);return tens[n/10]+(n%10==0?"":"-"+small[n%10]);
    }
    static String progress(LabyrinthData data,UUID player){
        int n=count(data,player);
        if(ready(data,player))return "The account holds together. The passage at the cell can be read.";
        if(n>=REQUIRED)return "Enough rooms have ended. Their endings are too much alike.";
        if(n*10>=REQUIRED*9)return "Only a few gaps remain.";
        if(n*3>=REQUIRED*2)return "Most of the account is here.";
        if(n*3>=REQUIRED)return "The account is taking shape.";
        return "Much of the account is still unwritten.";
    }
    public static boolean ready(LabyrinthData data,UUID player){
        CompoundTag stories=record(data,player).getCompound("Stories");Set<String> kinds=new HashSet<>();int count=0;
        for(Story story:Story.values())if(stories.contains(story.id)){count++;kinds.add(story.kind);}
        return count>=REQUIRED&&kinds.size()>=2;
    }
    public static boolean resolve(LabyrinthData data,UUID player,Story story,String outcome){
        if(has(data,player,story))return false;
        CompoundTag record=record(data,player),stories=record.getCompound("Stories");
        stories.putString(story.id,outcome);record.put("Stories",stories);save(data,player,record);ExpeditionRhythm.request(data,player);return true;
    }
    public static void resolve(ServerPlayer player,Story story,String outcome){
        if(player.isSpectator()||FinaleProgress.terminal(FinaleProgress.phase(player.server,player.getUUID())))return;
        LabyrinthData data=LabyrinthData.get(player.server);
        if(resolve(data,player.getUUID(),story,outcome)){
            CompoundTag stories=record(data,player.getUUID()).getCompound("Stories");Set<String> kinds=new HashSet<>();
            for(Story s:Story.values())if(stories.contains(s.id))kinds.add(s.kind);
            io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(player,"story_resolve","story",story.id,"kind",story.kind,"outcome",outcome,"count",count(data,player.getUUID()),"kinds",kinds.size(),"ready",ready(data,player.getUUID()));
            updateBook(player,true);
            player.displayClientMessage(Component.literal(ready(data,player.getUUID())
                    ?"A passage in your account is readable now. It describes a cell."
                    :"You can set this part of the account down now."),false);
        }
    }
    public static boolean readPlay(LabyrinthData data,UUID player){return record(data,player).getBoolean("ReadPlay");}
    public static void markRead(LabyrinthData data,UUID player){
        if(!ready(data,player))return;CompoundTag record=record(data,player);record.putBoolean("ReadPlay",true);save(data,player,record);
    }
    public static boolean ownedBook(ItemStack stack,UUID player){
        CustomData custom=stack.get(DataComponents.CUSTOM_DATA);return custom!=null&&custom.copyTag().hasUUID(BOOK_OWNER)
                &&player.equals(custom.copyTag().getUUID(BOOK_OWNER));
    }
    public static ItemStack book(LabyrinthData data,UUID player,String name,boolean epilogue){
        List<Component> pages=new ArrayList<>();
        pages.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,
                "AN ACCOUNT\n\nI am writing down what I saw, before I begin remembering something else.\n\nThere are still gaps."));
        int resolved=count(data,player);
        pages.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,"THE ACCOUNT SO FAR\n\nI have heard "+words(resolved)
                +(resolved==1?" room":" rooms")+" to the end.\n\n"+progress(data,player)));
        Set<String> kinds=kinds(data,player);
        if(kinds.size()==1){String missing=switch(kinds.iterator().next()){
            case "survival"->"Everything here ends with me surviving.";
            case "understanding"->"Everything here ends with me understanding something.";
            case "memory"->"Everything here ends with something remembered.";
            case "connection"->"Everything here ends with someone answering.";
            default->"Everything here ends with something let go.";};
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,"OTHER ENDINGS\n\n"+missing+"\n\nThe account needs another kind of ending."));
        }
        CompoundTag stories=record(data,player).getCompound("Stories");
        for(Story story:Story.values())if(stories.contains(story.id)){
            String text=stories.getString(story.id).equals("aftermath")
                    ?"Someone reached this room before me. I examined what remained.\n\n"+switch(story){
                        case FLOORBOARDS->"The board was gone. The floor was quiet. There had been somebody waiting beside the bed.";
                        case CLAP->"The wardrobe stood open. The game had ended here. Something had been left inside it.";
                        case HARRIGAN->"The casket was closed. I remembered the study, and the chair where someone had listened.";
                        case MODEL_HOME->"The tree was inside the window. The glass had failed to keep the world outside.";
                        case DROWNED_TOWN->"The church roof was open. The voice below had reached the air. I could hear it from the bank.";
                        case PRESERVED_CAVE->"The pews were empty. The people who had faced the water were standing on its bank. The canoe remained at the back.";
                        case SHALLOWS->"The shore remembered who had been there.";
                        case PHONE_CANOE->"The recording kept looking at the sky.";
                        case TED_CAVER->"The line still descended into the small passage.";
                        case GOATMAN->"The door was closed. The table had too few places.";
                        case ZAMPANO->"The windows stayed sealed. The gouges remained beside the trunk.";
                        case WHALE->"An undated letter waited in the middle attic.";
                        case BARN_WELL->"Two pairs of initials remained below the cover.";
                        case PLAIN->"A bird still circled beyond the dunes.";
                        case HOSPITAL->"The chair remained beside the empty incubator.";
                        case HOLLOWAY->"The supplies had been counted. The service latch was still worn.";
                        case MOTHER->"The shelves remained, but she had stopped keeping the things upon them.";
                        case SEANCE->"The chairs were empty. A name had been written below a photograph that was missing.";
                        case WALLPAPER->"The plaster showed through four seams. Someone had kept the pages the room was meant to conceal.";
                        case HOTEL->"The second place was empty. The desk still held the missing things. The heating plant had been attended.";
                        default->story.text;
                    }:story.text;
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,story.title+"\n\n"+text));
        }
        for(Story story:Story.values())if(!stories.contains(story.id)&&begun(data,player,story)){
            boolean left=HouseExperience.record(data,player).getCompound("Retreats").getInt(story.id)>0;
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,story.title+"\n\n"
                    +(left?"I left before I heard the end.":"I crossed its threshold. I have not heard how it ends.")
                    +"\n\nI have left this part unfinished."));
        }
        int n=resolved;
        pages.add(HouseWriting.page(HouseWriting.WritingStyle.ZAMPANO,
                "A PLAY WITHOUT AN AUDIENCE\n\nTHE PRISONER: They draw a monster so that nobody will ask who locked the door.\n\nTHE KEEPER: ")
                .copy().append(Component.literal("There is no one inside.").withStyle(s->s.withColor(0xA52A2A).withStrikethrough(true))));
        if(n>0)pages.add(HouseWriting.page(HouseWriting.WritingStyle.ZAMPANO,
                "THE PRISONER: Who remembers the room when its door is closed?\n\n[The next line has been crossed out.]"));
        if(n>=2)pages.add(HouseWriting.page(HouseWriting.WritingStyle.ZAMPANO,
                "THE WITNESS: I stayed long enough to hear the end.\n\nTHE KEEPER: ")
                .copy().append(Component.literal("There is no one inside.").withStyle(s->s.withColor(0xA52A2A).withStrikethrough(true))));
        if(ready(data,player)){
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.ZAMPANO,
                    "THE WITNESS: There is someone inside.\n\n[The weapon is laid beside the cell. The witness bows, with both hands empty, and opens the door.]"));
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,
                    "The weapon is the original. The old man can tell you which.\n\nRead the play at the cell's lectern. Lay the weapon nearby. Crouch and open the bars with empty hands.\n\nLeave the door open."));
        }
        if(epilogue){
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,
                    "I returned with the things I carried. The day was still the same day.\n\nI have left a space here for what passed me on the stairs."));
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.ZAMPANO,
                    "From inside the cell\n\nI heard "+name+" put the weapon down.\n\nThe door opened. For once, there was someone on the other side who had stayed to listen."));
        }
        ItemStack book=HouseWriting.book(epilogue?"The completed account":"An account",name,pages);
        CustomData.update(DataComponents.CUSTOM_DATA,book,tag->tag.putUUID(BOOK_OWNER,player));return book;
    }
    public static void updateBook(ServerPlayer player,boolean give){
        List<Integer> slots=new ArrayList<>();
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(ownedBook(player.getInventory().getItem(i),player.getUUID()))slots.add(i);
        boolean cursor=ownedBook(player.containerMenu.getCarried(),player.getUUID()),found=!slots.isEmpty()||cursor;
        if(!give&&!found)return;
        ItemStack book=book(LabyrinthData.get(player.server),player.getUUID(),player.getGameProfile().getName(),false);
        for(int slot:slots)player.getInventory().setItem(slot,book.copy());
        if(cursor)player.containerMenu.setCarried(book.copy());
        if(give&&!found&&!player.getInventory().add(book))player.drop(book,false);
        player.inventoryMenu.broadcastChanges();
    }
    @SubscribeEvent public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event){
        if(event.getEntity() instanceof ServerPlayer player&&!player.isSpectator()
                &&!FinaleProgress.terminal(FinaleProgress.phase(player.server,player.getUUID())))updateBook(player,false);
    }
    public static void onArrive(ServerPlayer player,LabyrinthPlace place){
        LabyrinthData data=LabyrinthData.get(player.server);Story story=Story.of(place.id());
        if(story==null||LiteraryRooms.isLiterary(place)||place==LabyrinthPlace.HOTEL||NovelVignettes.isNovel(place)||ClassicsVignettes.isClassic(place)||!data.isCompleted(place.id())||has(data,player.getUUID(),story))return;
        String prop=switch(story){case FLOORBOARDS->"the exposed space beneath the loose board";
            case CLAP->"the open wardrobe";case HARRIGAN->"the casket";case MODEL_HOME->"the child's window";
            case DROWNED_TOWN->"the church's open roof hatch";case PRESERVED_CAVE->"the canoe behind the empty pews";case SHALLOWS->"the bank";case PHONE_CANOE->"the canoe";case GOATMAN->"the trailer door";case TED_CAVER->"the cave landing";case ZAMPANO->"the survey";case WHALE->"the undated letter";case BARN_WELL->"the well";case PLAIN->"the distant shape";case HOSPITAL->"the dawn chart";case HOLLOWAY->"the service latch";case MOTHER->"the keeper's record";case SEANCE->"the family album";case WALLPAPER->"the collected pages";case HOTEL->"the closing account";default->"the closing account";};
        player.displayClientMessage(Component.literal("Someone reached the end before you. Crouch and examine "+prop+" to record what remains."),false);
    }
    /** Later explorers must inspect a resolved room's ending prop themselves. */
    public static boolean aftermathTarget(LabyrinthData data,LabyrinthPlace place,BlockPos base,BlockPos at){
        if(!data.isCompleted(place.id()))return false;BlockPos rel=at.subtract(base);
        return switch(place){
            case FLOORBOARDS->rel.equals(TellTaleFloorboards.LOOSE_BOARD.below());
            case HIDE_AND_CLAP->{CompoundTag state=data.state(HideAndClap.ID);
                if(!state.getBoolean("Opened")||!state.contains("Wardrobe"))yield false;
                BlockPos wardrobe=BlockPos.of(state.getLong("Wardrobe"));yield rel.equals(wardrobe)||rel.equals(wardrobe.above());}
            case HARRIGAN->rel.distManhattan(HarriganVignette.CASKET)<=1;
            case MODEL_HOME->rel.equals(ModelHome.WINDOW)||rel.equals(ModelHome.WINDOW.above());
            case DROWNED_TOWN->data.state(DrownedTown.ID).getBoolean("RoofOpened")&&rel.equals(DrownedTown.ROOF_HATCH);
            case PRESERVED_CAVE->IndianLakeProgress.deadOnShore(data)&&rel.distManhattan(PreservedCave.CANOE)<=1;
            default->false;
        };
    }
    @SubscribeEvent(priority=EventPriority.HIGH) public static void inspect(PlayerInteractEvent.RightClickBlock event){
        if(!(event.getEntity() instanceof ServerPlayer player)||player.isSpectator()||event.getHand()!=InteractionHand.MAIN_HAND
                ||!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR))return;
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();if(origin==null)return;
        LabyrinthData data=LabyrinthData.get(player.server);
        if(event.getPos().equals(FinaleArchitecture.lectern(origin))&&!FinaleProgress.committed(FinaleProgress.phase(player.server,player.getUUID()))&&!ready(data,player.getUUID())){
            ExpeditionRhythm.refuse(player,event.getPos(),"witness_lectern",progress(data,player.getUUID())+" Hear more rooms to their end.");
        }
        if(event.getPos().equals(FinaleArchitecture.lectern(origin))
                &&!FinaleProgress.committed(FinaleProgress.phase(player.server,player.getUUID()))
                &&player.serverLevel().getBlockEntity(event.getPos()) instanceof LecternBlockEntity desk){
            // Each reading is generated from this explorer's evidence. Sharing the page never shares its unlock.
            desk.setBook(book(data,player.getUUID(),player.getGameProfile().getName(),false));desk.setChanged();
            player.serverLevel().setBlock(event.getPos(),player.serverLevel().getBlockState(event.getPos()).setValue(LecternBlock.HAS_BOOK,true),3);
            event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
            if(player.openMenu(desk).isPresent())markRead(data,player.getUUID());updateBook(player,true);return;
        }
        LabyrinthPlace place=LabyrinthPlaces.placeAt(origin,player.blockPosition());
        if(place==null||Story.of(place.id())==null||!player.isShiftKeyDown())return;
        if(aftermathTarget(data,place,LabyrinthPlaces.base(origin,place),event.getPos())){
            resolve(player,Story.of(place.id()),"aftermath");event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
