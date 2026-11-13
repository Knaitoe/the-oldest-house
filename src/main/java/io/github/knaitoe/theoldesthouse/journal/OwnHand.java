package io.github.knaitoe.theoldesthouse.journal;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.537 (docs/PELL_HOUSE_ENTRIES_0_4_537.md): a line in a reader's journal in their own lettering and graphite, as if they had
 * written it, and they never did: the Pell house's false entries (docs/PELL_HOUSE_PROPOSAL.md, "Being framed": "This is the reader's
 * lettering, which is the horror."). It is not the other hand (CoAuthor), which writes in a lettering of its own.
 *
 * <p>Like the other hand, it only adds. It never touches the reader's words; it goes only on a page begun in the House (or on the blank
 * page after the reader's last, which it begins there); never on a page the reader has open; and nothing of it can be rubbed out.
 * It sits on the rows after the reader's own writing on its page, as the last thing written there; written on later, the page keeps
 * it after their words. Torn out, the page carries it.
 */
public final class OwnHand {
    private OwnHand(){}
    /** The rows a page's writing has, as the screen draws it (JournalPageRenderer.LINES), and about how many characters go in a row,
     *  for judging room here; the screen wraps by width. */
    static final int ROWS=15,ROW=20;

    /**
     * Writes the line in the reader's own hand now if it can go in: on the reader's last page begun in the House if it has room below
     * their writing, otherwise on the blank page after their last. Returns the page, or 0 when it cannot go in now (no journal, or
     * the page for it is open on the reader's screen) or at all (the journal is full).
     */
    public static int write(ServerPlayer p,String text){
        if(p.isSpectator()||text.isBlank())return 0;
        var data=Journals.get(p.server);var id=p.getUUID();var b=data.books().get(id);
        if(b==null||b.issue==0)return 0;
        var open=CoAuthor.open(id);int need=rows(text);
        int lastHouse=0,last=0;
        for(var e:b.pages.entrySet()){
            var page=e.getValue();if(page.empty()&&page.begun().isEmpty())continue;
            last=Math.max(last,e.getKey());if(page.house()&&!page.empty())lastHouse=Math.max(lastHouse,e.getKey());
        }
        for(int i:b.torn.keySet())last=Math.max(last,i);
        int at=0;
        if(lastHouse>0&&!open.contains(lastHouse)&&!b.torn.containsKey(lastHouse)){
            var page=b.pages.get(lastHouse);
            if(page.notes().size()<JournalPage.MAX_NOTES&&used(page)+need<=ROWS)at=lastHouse;
        }
        if(at==0){int next=last+1;if(next>Journals.PAGES||open.contains(next))return 0;at=next;}
        var page=b.pages.getOrDefault(at,JournalPage.BLANK);
        if(page.begun().isEmpty()&&page.day()==0)page=page.begunAt(Journals.where(p),Journals.day(p),HouseDimensions.isHouseDimension(p.level().dimension()));
        b.pages.put(at,page.withNote(new JournalPage.Note(JournalPage.Mark.OWN,text,"",0,0,0)));
        data.setDirty();
        io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"journal_own_hand","page",at);
        return at;
    }
    /** About how many rows a page's writing and its lines in the reader's own hand take. */
    static int used(JournalPage page){int n=rows(page.text());for(var note:page.notes())if(note.mark()==JournalPage.Mark.OWN)n+=rows(note.text());return n;}
    /** About how many rows writing takes, at ROW characters a row, each line of it starting a row. */
    static int rows(String text){
        if(text.isBlank())return 0;int n=0;
        for(var line:text.split("\n",-1))n+=Math.max(1,(line.length()+ROW-1)/ROW);
        return n;
    }
    /** Every line in the reader's own hand in their journal, by page, in page order (for tests). */
    public static List<String> lines(MinecraftServer s,UUID id){
        var out=new ArrayList<String>();var data=Journals.get(s);
        for(int i=1;i<=Journals.PAGES;i++)for(var n:data.page(id,i).notes())if(n.mark()==JournalPage.Mark.OWN)out.add(i+": "+n.text());
        return out;
    }
}
