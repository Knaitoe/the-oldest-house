package io.github.knaitoe.theoldesthouse.journal;

import java.util.Arrays;
import java.util.Objects;

/**
 * 0.4.265, when the journal screen sends what is being written (the owner: "Autosave journal writing. Currently, edits are sent when
 * turning a page or closing the journal. Save periodically while typing to reduce lost writing after a disconnect or crash."). It
 * still sends on a page turn and on closing; besides that, writing not yet sent goes back a second after the hand stops, and at least
 * every five seconds while it keeps going. A dropped connection or a crash loses a few words at most. Both are well inside the
 * server's own allowance for edits (Journals.TOKENS, refilled every Journals.REFILL_TICKS), even with both pages open.
 */
public final class WritingClock {
    /** Ticks without a keystroke before unsent writing goes back, and the longest unsent writing waits while typing goes on. */
    public static final int SETTLE=20,EVERY=100;
    private final String[] seen=new String[2];
    private int still,unsent;

    /**
     * One tick of the open screen. {@code now} is what each open page holds (null where no page is being written), {@code sent} what
     * the server last had for it. True when it is time to send; the caller sends, and the clock starts over.
     */
    public boolean tick(String[] now,String[] sent){
        boolean typed=false,pending=false;
        for(int side=0;side<seen.length;side++){
            String text=side<now.length?now[side]:null;
            if(text==null){seen[side]=null;continue;}
            if(seen[side]!=null&&!text.equals(seen[side]))typed=true;
            seen[side]=text;
            if(!text.equals(side<sent.length?sent[side]:null))pending=true;
        }
        if(!pending){still=0;unsent=0;return false;}
        unsent++;still=typed?0:still+1;
        if(still>=SETTLE||unsent>=EVERY){still=0;unsent=0;return true;}
        return false;
    }
    /** After a send for any other reason (a page turned): the wait starts over. */
    public void sent(){still=0;unsent=0;}
    @Override public String toString(){return "WritingClock[still="+still+", unsent="+unsent+", seen="+Arrays.stream(seen).map(s->s==null?"-":Integer.toString(Objects.hashCode(s))).toList()+"]";}
}
