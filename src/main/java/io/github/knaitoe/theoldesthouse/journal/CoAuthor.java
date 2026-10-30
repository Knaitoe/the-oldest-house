package io.github.knaitoe.theoldesthouse.journal;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.journal.JournalPage.Mark;
import io.github.knaitoe.theoldesthouse.journal.JournalPage.Note;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * The other hand in the reader's journal (JOURNAL_DESIGN.md, step 4). Now and then, after the reader wakes from a night's
 * sleep, comes round from a blackout or leaves one of the worst rooms behind, something writes in their journal while they
 * are not looking: on the blank page after their last, through a name they wrote, or under a line that says which way they
 * went. One time in six, never twice in a House day, only on pages begun in the House and never on a page the reader has
 * open. It only adds: the reader's words stay, its crossing-out goes when they delete the word under it, and nothing else of
 * its ink can be rubbed out. Nothing tells the reader it happened.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class CoAuthor {
    public enum Trigger { SLEEP, BLACKOUT, DREAD }
    public enum Wrote { WROTE, NO_JOURNAL, ALREADY_TODAY, NOWHERE, NOT_THIS_TIME }
    public static final int ROLL=6;
    public static final String BLANK="Nothing to say? I was right behind you the whole time.";
    /** The spread each reader's journal lies open at, as their screen last said; never saved. */
    private static final Map<UUID,Integer> VIEWING=new HashMap<>();
    /** Readers the other hand always writes for: the tests' own, so they need not wait on a one in six. */
    private static final Set<UUID> CERTAIN=new HashSet<>();

    /**
     * The names a reader may have written, room by room, counted once the reader has been there. Words that are also common
     * words (Will, June, Ray, Dot, Bud) are left out, so the other hand never strikes through a month or a verb.
     */
    static final Map<String,List<String>> CAST=Map.ofEntries(
        Map.entry("harrigan",List.of("Harrigan")),
        Map.entry("three_fingers",List.of("Walter","Ruth")),
        Map.entry("elk_lot",List.of("Marla","Russ","Denny","Wade","Hollis")),
        Map.entry("lake_house",List.of("Tess")),
        Map.entry("shallows",List.of("Hank","Clay","Lonnie")),
        Map.entry("winter_lake",List.of("Hank","Clay","Lonnie")),
        Map.entry("costume_night",List.of("Hank")),
        Map.entry("movie_night",List.of("Frank","Jamie","Bobby","Lynn")),
        Map.entry("holy_rabbit",List.of("Slaney")),
        Map.entry("ghosts_set",List.of("Marjorie","Merry")),
        Map.entry("borrasca",List.of("Kimber","Kyle","Prescott")),
        Map.entry("holloway_camp",List.of("Holloway")),
        Map.entry("karen_room",List.of("Karen")),
        Map.entry("zampano_courtyard",List.of("Zampanò","Zampano")),
        Map.entry("whale",List.of("Pelafina")),
        Map.entry("goatman",List.of("Goatman")));
    /** The rooms whose ending leaves a reader shaken enough for the other hand to write afterwards. */
    public static final Set<WitnessAccount.Story> DREAD=Set.of(
        WitnessAccount.Story.GOATMAN,WitnessAccount.Story.TED_CAVER,WitnessAccount.Story.BARN_WELL,WitnessAccount.Story.HOLLOWAY,
        WitnessAccount.Story.CRIMSON_HALL,WitnessAccount.Story.ELK_CARCASSES,WitnessAccount.Story.CAMP_BLOOD,WitnessAccount.Story.WINTER_LAKE,
        WitnessAccount.Story.COSTUME_NIGHT,WitnessAccount.Story.DROWNED_TOWN,WitnessAccount.Story.BORRASCA,WitnessAccount.Story.CHILD_ROOM,
        WitnessAccount.Story.HILL_NURSERY,WitnessAccount.Story.CLAP,WitnessAccount.Story.PAINTED_INTERIOR,WitnessAccount.Story.SECOND_WAITRESS,
        WitnessAccount.Story.TUNNEL_PERIPHERY,WitnessAccount.Story.MISSING_SLEEP,WitnessAccount.Story.LAYERED_MAN,WitnessAccount.Story.MOTION_LIGHT,
        WitnessAccount.Story.BACKSEAT,WitnessAccount.Story.SILO,WitnessAccount.Story.DARE_IN_THE_DARK);
    private CoAuthor(){}

    /** One of the moments the other hand may write after: one time in six. */
    public static Wrote consider(ServerPlayer p,Trigger why){
        if(p.isSpectator())return Wrote.NOT_THIS_TIME;
        if(!CERTAIN.contains(p.getUUID())&&p.getRandom().nextInt(ROLL)!=0)return Wrote.NOT_THIS_TIME;
        return write(p,why);
    }
    /** A room's ending, as WitnessAccount records it: only the listed rooms count. */
    public static void ended(ServerPlayer p,WitnessAccount.Story story){if(DREAD.contains(story))consider(p,Trigger.DREAD);}
    /** The night was slept through and the morning woke them; leaving the bed early or being shaken awake is not that. */
    @SubscribeEvent public static void woke(PlayerWakeUpEvent e){if(e.getEntity() instanceof ServerPlayer p&&!e.wakeImmediately()&&!e.updateLevel())consider(p,Trigger.SLEEP);}

    /** One note, now, wherever the other hand finds something to answer. */
    public static Wrote write(ServerPlayer p,Trigger why){
        var data=Journals.get(p.server);UUID id=p.getUUID();
        if(data.issue(id)==0)return Wrote.NO_JOURNAL;
        var b=data.book(id);int today=Journals.day(p);
        if(b.inked==today)return Wrote.ALREADY_TODAY;
        var open=open(id);var random=p.getRandom();
        // Which forms have somewhere to go, then one of them, then one place for it.
        var struck=new ArrayList<Choice>();var answers=new ArrayList<Choice>();Choice blank=null;
        var names=met(p.server,id);int lastHouse=0;
        for(var e:b.pages.entrySet()){
            int i=e.getKey();var page=e.getValue();
            if(page.house()&&!page.empty())lastHouse=Math.max(lastHouse,i);
            if(!page.house()||open.contains(i)||page.notes().size()>=JournalPage.MAX_NOTES)continue;
            for(var name:names){var at=JournalPage.uses(page.text(),name,true);
                for(int u=0;u<at.size();u++)if(!opening(page.text(),at.get(u))&&!tied(page,Mark.STRUCK,name,u))struck.add(new Choice(i,new Note(Mark.STRUCK,"",name,u,0,0)));}
            if(page.notes().stream().noneMatch(n->n.mark()==Mark.DIRECTION))for(var word:List.of("left","right")){var at=JournalPage.uses(page.text(),word,false);
                for(int u=0;u<at.size();u++)answers.add(new Choice(i,new Note(Mark.DIRECTION,word.equals("left")?"No. Right.":"No. Left.",word,u,.11F,under(page.text(),at.get(u)))));}
        }
        int next=lastHouse+1;
        if(lastHouse>0&&next<=Journals.PAGES&&!open.contains(next)){var page=b.pages.getOrDefault(next,JournalPage.BLANK);if(page.empty()&&page.begun().isEmpty())blank=new Choice(next,new Note(Mark.WRITTEN,BLANK,"",0,.16F,.36F));}
        var forms=new ArrayList<List<Choice>>();if(!struck.isEmpty())forms.add(struck);if(!answers.isEmpty())forms.add(answers);if(blank!=null)forms.add(List.of(blank));
        if(forms.isEmpty())return Wrote.NOWHERE;
        var form=forms.get(random.nextInt(forms.size()));var choice=form.get(random.nextInt(form.size()));
        b.pages.put(choice.page(),b.pages.getOrDefault(choice.page(),JournalPage.BLANK).withNote(choice.note()));
        b.inked=today;data.setDirty();
        io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"journal_other_hand","trigger",why.name().toLowerCase(java.util.Locale.ROOT),"mark",choice.note().mark().id(),"page",choice.page());
        return Wrote.WROTE;
    }
    record Choice(int page,Note note){}
    static boolean tied(JournalPage page,Mark mark,String word,int use){return page.notes().stream().anyMatch(n->n.mark()==mark&&n.word().equals(word)&&n.use()==use);}
    /** A capital at the start of a sentence may be any word ("Will you"), so only names inside a sentence are struck. */
    static boolean opening(String text,int at){
        int i=at-1;while(i>=0&&text.charAt(i)==' ')i--;
        return i<0||".!?\n\"“".indexOf(text.charAt(i))>=0;
    }
    /** About where the line under a word lies, as a share of the page, for an answer whose line has since gone. */
    static float under(String text,int at){
        int row=0,since=0;for(int i=0;i<at;i++){if(text.charAt(i)=='\n'){row++;since=0;}else if(++since>=24){row++;since=0;}}
        return Mth.clamp((16+(row+1)*9F+1)/196F,0,.85F);
    }
    /** Every cast name from the rooms this reader has been in. */
    static Set<String> met(MinecraftServer s,UUID id){
        var names=new LinkedHashSet<String>();for(var place:LabyrinthData.get(s).visited(id)){var cast=CAST.get(place);if(cast!=null)names.addAll(cast);}
        return names;
    }

    /** The two pages of the spread the reader's screen has open, if it is open. */
    static Set<Integer> open(UUID id){var s=VIEWING.get(id);return s==null?Set.of():Set.of(s*2,s*2+1);}
    /** The reader's screen says where it lies open, or that it has shut (-1). */
    public static void viewing(ServerPlayer p,int spread){if(spread<0)VIEWING.remove(p.getUUID());else VIEWING.put(p.getUUID(),Mth.clamp(spread,0,Journals.PAGES/2));}
    /** For the tests: the other hand always writes for this reader, or goes back to one time in six. */
    public static void certain(UUID id,boolean always){if(always)CERTAIN.add(id);else CERTAIN.remove(id);}
    @SubscribeEvent public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent e){VIEWING.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){VIEWING.clear();CERTAIN.clear();}
}
