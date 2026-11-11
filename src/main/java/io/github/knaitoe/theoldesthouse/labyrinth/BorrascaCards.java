package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.BorrascaCardsPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.455, the owner, of the page of text that told a reader what hung on the doctor's corkboard: "Try to show, don't tell. Try
 * to design an actual ui for this". The board is a board now (client.BorrascaCardsScreen): cork in a frame, the families' birth
 * cards pinned to it, and a hand that takes one down to look at it. This is what hangs there on a reader's visit.
 * <ul>
 * <li>Five families' cards, pinned in the order they came. Each is a printed card (a picture, a line of print) that the family
 * wrote on in its own hand: the child's name, the day, a line to the doctor, and who it is from. Nothing on the board says what
 * the cards have in common. The days are there to be counted and the names to be read down, and every line is to the doctor.</li>
 * <li>The envelope on the last pin. It has the doctor's name on it and nothing else, and it turns over: it is empty.</li>
 * <li>On the second visit the Tates' card is pinned over the envelope: the couple who were waiting on the first.</li>
 * <li>To a reader who comes back grown, a later year's cards hang over the old ones. The old ones are still under them, gone
 * yellow, an edge of each showing, and can be taken out and looked at too.</li>
 * </ul>
 * 0.4.424 (owner: "the children were named after their progenitor"): every baby's name begins with P or K, the Prescotts'
 * letter or the Sheriff's, in the childhood year and years later alike. Nothing on the board says so.
 * <p>
 * A piece counts as looked at once a reader has had it in the hand for a moment. Having looked at every piece that hangs on top
 * is having read the board (Read_Cards, Read_Cards2 or Read_CardsLater, the keys the pages had).
 */
public final class BorrascaCards {
    private BorrascaCards(){}
    /** The pictures printed on the cards, as they lie on the screen's sheet. */
    public static final int STORK=0,RATTLE=1,BOW=2,PRAM=3,BOTTLE=4,DUCK=5;
    /** The board's places, three across and two down. The envelope hangs in the last. */
    public static final int PLACES=6,ENVELOPE_PLACE=5;
    public static final String EARLY_YEAR="1992",LATER_YEAR="2009";
    /**
     * One piece on the board. A card has its family, what is printed on it and what they wrote; the envelope has only who it is
     * to (signed). place is where it hangs. over is a piece pinned on top of the one in its place, under one beneath another;
     * old is a piece of the childhood year seen years on.
     */
    public record Piece(boolean envelope,String family,String printed,String name,String detail,String date,String thanks,String signed,int picture,int paper,int hand,int place,boolean over,boolean under,boolean old){
        /** Everything written on it, for a test or a narrator: in the order an eye takes it. */
        public String text(){return envelope?signed:printed+"\n"+name+"\n"+(detail.isEmpty()?"":detail+"\n")+date+"\n"+thanks+"\n"+signed;}
        /** The day without its year: what can be read of the date from where a reader stands. */
        public String day(){int comma=date.indexOf(',');return comma<0?date:date.substring(0,comma);}
    }
    private static final String[] FAMILY={"Pruitt","Webb","Hollis","Corley","Maddox"},SIGNED={"the Pruitts","the Webbs","the Hollises","the Corleys","the Maddoxes"};
    private static final String[] PRINTED={"It's a Girl!","A New Arrival","Our Daughter","Twins!","A Son"};
    private static final String[] EARLY={"Kelsey Ann","Paul","Katelyn","Peyton and Kody","Kevin"},LATER={"Kennedy","Paxton","Kaylee","Piper and Kade","Kingston"};
    private static final String[] DETAIL={"","7 lb 2 oz","","",""};
    private static final String[] DAY={"March 3","March 20","April 6","April 28","May 15"};
    private static final String[] THANKS={"Thank you, Doc.","We can't thank you enough.","An answer to prayer.","Thank you for everything.","Thank you, Dr. Hale."};
    private static final int[] PICTURE={BOW,STORK,DUCK,PRAM,RATTLE};
    /** The card stock each family chose, in the childhood year and the later one: cream 0, pink 1, blue 2, yellow 3, green 4. */
    private static final int[] EARLY_PAPER={1,2,3,4,0},LATER_PAPER={3,0,1,2,4};
    /** Whose hand is whose (client.BorrascaCardsScreen.hand): the Webbs write heavily, the Maddoxes on a slant. */
    private static final int[] HAND={0,4,2,3,1};

    private static Piece card(int i,boolean later,boolean under){
        return new Piece(false,FAMILY[i],PRINTED[i],(later?LATER:EARLY)[i],later?"":DETAIL[i],DAY[i]+", "+(later?LATER_YEAR:EARLY_YEAR),THANKS[i],SIGNED[i],
            PICTURE[later?(i+2)%5:i],(later?LATER_PAPER:EARLY_PAPER)[i],HAND[i],i,false,under,under);
    }
    /** What hangs on the board on a visit, in the order it is drawn: what is underneath first. */
    public static List<Piece> board(int visit){
        boolean later=visit>=3;var out=new ArrayList<Piece>();
        if(later)for(int i=0;i<5;i++)out.add(card(i,false,true));
        for(int i=0;i<5;i++)out.add(card(i,later,false));
        out.add(new Piece(true,"","","","","","","Dr. Hale",0,5,5,ENVELOPE_PLACE,false,visit==2,later));
        // 0.4.426: the couple waiting on the first visit had their girl by the second; her card is the newest on the board.
        if(visit==2)out.add(new Piece(false,"Tate","It's a Girl!","Paige","","June "+EARLY_YEAR,"Thank you, Doc. We'd have been lost without you.","Roy and Jolene",BOTTLE,0,6,ENVELOPE_PLACE,true,false,false));
        return out;
    }
    /**
     * The order a hand goes along the board: place by place, and in each place what hangs on top before what is under it.
     * These are places in board(visit).
     */
    public static int[] order(int visit){
        var board=board(visit);
        return java.util.stream.IntStream.range(0,board.size()).boxed().sorted(java.util.Comparator.<Integer>comparingInt(i->board.get(i).place()).thenComparingInt(i->board.get(i).under()?1:0)).mapToInt(Integer::intValue).toArray();
    }
    /** The key the board's pages had, by visit: what having read it is recorded under. */
    public static String key(int visit){return visit>=3?"CardsLater":visit==2?"Cards2":"Cards";}
    /** Every piece that hangs on top, as bits: what a reader has to have looked at to have read the board. */
    public static int top(int visit){int bits=0;var board=board(visit);for(int i=0;i<board.size();i++)if(!board.get(i).under())bits|=1<<i;return bits;}

    /** A hand on the board: it opens as a board to the reader, with what they have already looked at. */
    public static void open(ServerPlayer p,CompoundTag own,int visit){
        own.putInt("CardsBoard",visit);
        HousePackets.send(p,new BorrascaCardsPayload(visit,own.getInt("Seen_"+key(visit))));
    }
    /** From the reader's screen: a piece they have had in the hand for a moment. */
    public static void seen(ServerPlayer p,int index){
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.BORRASCA);int visit=own.getInt("CardsBoard");
        if(visit<1||visit!=own.getInt("Visit")||index<0||index>=board(visit).size())return;
        var b=BorrascaSites.base(p.server,p.getUUID());if(b==null||p.distanceToSqr(b.offset(BorrascaShops.CARDS).getCenter())>64)return;
        String key=key(visit);int seen=own.getInt("Seen_"+key)|1<<index,top=top(visit);own.putInt("Seen_"+key,seen);
        if((seen&top)==top)own.putBoolean("Read_"+key,true);
        LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.BORRASCA,own);
    }
}
