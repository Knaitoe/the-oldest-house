package io.github.knaitoe.theoldesthouse.journal;

import io.github.knaitoe.theoldesthouse.house.HouseConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.FilteredText;

/**
 * Where the House quotes the reader (JOURNAL_DESIGN.md, step 8): rarely, exactly, and only to that reader. Three moments: a
 * chapter of their own House of Leaves that tells of a place where they wrote quotes one line written there (StaircaseStory),
 * the other hand once copies out one of their lines on a later blank page (CoAuthor), and at an ending the House's last page in
 * their journal is the first line they ever wrote in the House.
 *
 * <p>What may be quoted: the reader's own words, unaltered, from pages begun in the House, a few words long, that the text
 * filter left as they were. Never a line that names another player, a line on a page that has been torn out, or a line the
 * reader has since deleted or changed. A server that has turned the House's read off never quotes anyone; the read itself still
 * never looks at the journal.
 */
public final class Quotes {
    public static final int MIN_WORDS=3, MAX_CHARS=JournalPage.MAX_RUN, KEPT=400;
    /** Where the House's last page lies on the page, in its hand. */
    public static final float ENDING_X=.16F, ENDING_Y=.42F;
    private Quotes(){}

    /** A line the reader wrote in the House: its words, the page it is on, the place it was written (an id, or none) and its order. */
    public record Line(String text,int page,String place,long order){
        CompoundTag save(){var t=new CompoundTag();t.putString("Text",text);t.putInt("Page",page);if(!place.isEmpty())t.putString("Place",place);t.putLong("Order",order);return t;}
        static Line load(CompoundTag t){var text=t.getString("Text");int page=t.getInt("Page");return text.isBlank()||text.length()>MAX_CHARS||page<1||page>Journals.PAGES?null:new Line(text,page,t.getString("Place"),t.getLong("Order"));}
    }

    /** Whether the House may quote anyone on this server: not once its read has been turned off. */
    public static boolean allowed(){return HouseConfig.readEnabled(false);}

    /**
     * The lines in a page's writing the House could ever quote: each written line, or for a line too long to quote whole, each
     * of its sentences; a few words long and short enough to copy out. Kept exactly as written, only trimmed of spaces at the ends.
     */
    public static List<String> lines(String text){
        var out=new ArrayList<String>();
        for(var line:text.split("\n")){
            var whole=line.strip();
            if(whole.length()<=MAX_CHARS){if(quotable(whole))out.add(whole);continue;}
            for(var sentence:whole.split("(?<=[.!?])\\s+")){var s=sentence.strip();if(s.length()<=MAX_CHARS&&quotable(s))out.add(s);}
        }
        return out;
    }
    static boolean quotable(String s){return !s.isEmpty()&&s.split("\\s+").length>=MIN_WORDS;}

    /**
     * A page the reader has just written, as Journals keeps it: the lines new to it are remembered with where they were written,
     * if the filter left them as they were, and the lines no longer on it are forgotten, so a line deleted or changed is never
     * quoted. Only pages begun in the House.
     */
    static void wrote(Journals.Book b,int index,JournalPage before,JournalPage kept,FilteredText filtered,String place){
        var now=lines(kept.text());
        b.lines.removeIf(l->l.page()==index&&!now.contains(l.text()));
        if(!kept.house())return;
        for(var line:fresh(before.text(),kept.text(),filtered.raw()))if(b.lines.size()<KEPT)b.lines.add(new Line(line,index,place,++b.lineOrder));
    }
    /** The lines new to a page's writing that the filter left exactly as the reader typed them (in the raw text too). */
    public static List<String> fresh(String before,String after,String raw){
        var had=new ArrayList<>(lines(before));var passed=new HashSet<>(lines(raw));var out=new ArrayList<String>();
        for(var line:lines(after)){if(had.remove(line))continue;if(passed.contains(line))out.add(line);}
        return out;
    }
    /** A page torn out: none of its lines is ever quoted, even once it is back in its stub. */
    static void torn(Journals.Book b,int index){b.lines.removeIf(l->l.page()==index);}

    /** The names of every other player the House knows: those here now and every other reader with a journal. */
    static Set<String> others(MinecraftServer s,UUID reader){
        var names=new LinkedHashSet<String>();
        for(var p:s.getPlayerList().getPlayers())if(!p.getUUID().equals(reader))names.add(p.getGameProfile().getName());
        for(var e:Journals.get(s).books().entrySet())if(!e.getKey().equals(reader)&&!e.getValue().name.isBlank())names.add(e.getValue().name);
        var own=Journals.get(s).books().get(reader);if(own!=null)names.remove(own.name);
        return names;
    }
    static boolean names(String line,Set<String> others){for(var name:others)if(name.length()>=3&&!JournalPage.uses(line,name,false).isEmpty())return true;return false;}

    /** The reader's lines the House may quote now, oldest first. */
    public static List<Line> eligible(MinecraftServer s,UUID reader){
        if(!allowed())return List.of();
        var b=Journals.get(s).books().get(reader);if(b==null||b.lines.isEmpty())return List.of();
        var others=others(s,reader);var out=new ArrayList<Line>();
        for(var l:b.lines){
            if(b.torn.containsKey(l.page()))continue;
            var page=b.pages.get(l.page());if(page==null||!page.house()||!lines(page.text()).contains(l.text()))continue;
            if(names(l.text(),others))continue;
            out.add(l);
        }
        out.sort(java.util.Comparator.comparingLong(Line::order));
        return out;
    }
    /** The first line the reader ever wrote in the House that may still be quoted. */
    public static Optional<Line> first(MinecraftServer s,UUID reader){var all=eligible(s,reader);return all.isEmpty()?Optional.empty():Optional.of(all.getFirst());}

    /**
     * An ending: the House's last page, the first blank page after the reader's writing, holds the first line they ever wrote in
     * the House, in its hand, and nothing else. Once for each reader, and only in their own journal, which nobody else opens.
     */
    public static boolean ending(MinecraftServer s,UUID reader){
        var data=Journals.get(s);var b=data.books().get(reader);if(b==null||b.issue==0||b.ended>0)return false;
        var line=first(s,reader).orElse(null);if(line==null)return false;
        int last=0;for(var e:b.pages.entrySet())if(!e.getValue().empty())last=Math.max(last,e.getKey());for(int i:b.torn.keySet())last=Math.max(last,i);
        int at=last+1;if(at>Journals.PAGES)return false;
        b.pages.put(at,b.pages.getOrDefault(at,JournalPage.BLANK).withNote(new JournalPage.Note(JournalPage.Mark.QUOTED,line.text(),"",0,ENDING_X,ENDING_Y)));
        b.ended=at;data.setDirty();return true;
    }
    /** The page the House's last page went on, or 0. */
    public static int endingPage(MinecraftServer s,UUID reader){var b=Journals.get(s).books().get(reader);return b==null?0:b.ended;}
}
