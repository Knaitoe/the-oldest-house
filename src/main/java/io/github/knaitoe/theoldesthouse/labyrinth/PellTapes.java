package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.PellTapePayload;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.536, the tapes (docs/PELL_HOUSE_TAPES_0_4_536.md), from the proposal's "2. A week in": "The tapes: the shed is open. The older
 * sibling can watch the tapes in order, and the rite becomes readable." And from "The tapes": nine home recordings by an older woman,
 * never named, speaking to somebody off camera who has lost a child; in fragments, the ring, the one kept in it, the other child and
 * the water; "Out of order they are only a frightening woman talking."
 *
 * <ul>
 * <li><b>The shed is open.</b> A week in, its padlock hangs open on the hasp until June goes out on her rounds; a hand opens and shuts
 * its door. On her rounds she padlocks it again, as on the first night.</li>
 * <li><b>The deck.</b> The tape in the deck when a visit begins is the eighth, the one about the water: its case is the empty one.
 * A hand on the television plays the tape in the deck from its start, or, while it plays, stops it. The picture is drawn on each
 * reader's own client (client.PellTapeClient), and her lines are captions from the screen.</li>
 * <li><b>The shelf.</b> A hand on the tapes opens the conversation panel: the spines in order, the empty case named, and each other
 * tape to take. The deck's tape goes back in its case and the chosen one plays from its start.</li>
 * <li><b>Watching.</b> The deck is the house's, so everybody near it hears the same tape, and anybody can stop it or change it. A
 * reader watching from its start (in the shed, or near the screen with nothing in the way) and still within earshot at its end has
 * watched that tape ("TapesWatched"). Watched in turn from the first, they read the tapes ("TapeRun", then "ReadTheTapes"); a tape
 * watched before its turn is kept too ("WatchedOutOfOrder"). Each line a reader hears is kept once in their journal, under the
 * tape's date.</li>
 * <li><b>June.</b> Coming for the tin on her rounds, she turns the deck off if it is playing.</li>
 * </ul>
 * Before a week in, the television and the tapes are only what they are.
 */
public final class PellTapes {
    private PellTapes(){}
    public static final int TAPES=PellTexts.TAPE_LINES.length;
    /** The tape in the deck when a visit begins: the eighth, about the water. */
    public static final int IN_DECK=7;
    /** The television on the bench (from the house's base), and which way its screen faces. */
    public static final BlockPos TV=new BlockPos(7,1,-93);
    public static final Direction FACING=Direction.SOUTH;
    /** Ticks of hiss before her first line and after her last; ticks between her lines. */
    public static final int LEAD=40,TAIL=40,PAUSE=12;
    /** How near the screen a reader begins watching from; how far off they stop; how far her voice carries. */
    public static final double WATCH=5,AWAY=8,EARSHOT=8;
    /** The conversation panel's key for the shelf. */
    static final String KEY="pell_tapes";

    // ------------------------------------------------------------------------------------------------ timing (both sides)
    /** When each of a tape's lines begins, in ticks from its start at a reading pace (Dialogue), and, last, the tape's whole length. */
    public static int[] schedule(int tape,int pace){
        var lines=PellTexts.TAPE_LINES[tape];var at=new int[lines.length+1];int t=LEAD;
        for(int i=0;i<lines.length;i++){at[i]=t;t+=Dialogue.ticks(lines[i],pace)+PAUSE;}
        at[lines.length]=t-PAUSE+TAIL;return at;
    }
    /** A tape's length in ticks at a reading pace. */
    public static int length(int tape,int pace){var s=schedule(tape,pace);return s[s.length-1];}
    /** Which of a tape's lines is up this many ticks into it, or -1: before the first, between two, after the last. */
    public static int lineAt(int tape,int pace,int t){
        if(tape<0||tape>=TAPES)return -1;
        var s=schedule(tape,pace);var lines=PellTexts.TAPE_LINES[tape];
        for(int i=0;i<lines.length;i++)if(t>=s[i]&&t<s[i]+Dialogue.ticks(lines[i],pace))return i;
        return -1;
    }

    // ------------------------------------------------------------------------------------------------ for tests
    /** The tape in the deck. */
    public static int deck(BlockPos b){return PellPeople.household(b).deck;}
    /** Whether the deck is playing. */
    public static boolean playing(BlockPos b){return PellPeople.household(b).tapePlaying;}
    /** Who is watching the tape playing now. */
    public static List<UUID> watchers(BlockPos b){return List.copyOf(PellPeople.household(b).tapeWatchers);}
    /** The tape playing put on to just before its end. */
    public static void nearEnd(ServerLevel l,BlockPos b){var h=PellPeople.household(b);if(!h.tapePlaying)return;h.tapeStart=l.getGameTime()-length(h.deck,h.tapePace)+2;}
    /** This reader as if they had watched these tapes, in this order. */
    public static void asWatched(ServerPlayer p,int... tapes){for(int t:tapes)watched(p,t);}

    // ------------------------------------------------------------------------------------------------ hands
    /** Whether the shed's padlock hangs open: a week in, until June goes out on her rounds. */
    static boolean unpadlocked(PellPeople.Household h){return h.visit>=2&&h.step<PellNight.ROUNDS;}
    /** A hand on the shed's door: a week in, it opens and shuts. False while it is padlocked. */
    static boolean shedHand(ServerPlayer p,ServerLevel l,BlockPos b){
        var h=PellPeople.household(b);if(!unpadlocked(h))return false;
        boolean opening=!PellSite.open(l,b,PellSite.SHED_DOOR);
        PellHouse.toggle(l,b,PellSite.SHED_DOOR);
        if(opening)PellHouse.cue(p,PellTexts.SHED_OPEN);
        return true;
    }
    /** A hand on the television a week in: the tape in the deck from its start, or, while it plays, the picture gone. */
    static boolean tv(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();var h=PellPeople.household(b);if(h.visit<2)return false;
        if(h.tapePlaying){stop(l,b,h);PellHouse.cue(p,PellTexts.TAPE_STOP);}
        else{play(l,b,h);PellHouse.cue(p,PellTexts.TAPE_PLAY);}
        return true;
    }
    /** A hand on the tapes a week in: the shelf on the conversation panel. */
    static boolean shelf(ServerPlayer p,BlockPos b){
        var h=PellPeople.household(b);if(h.visit<2)return false;
        Talk.begin(p,KEY,PellTexts.TAPES_WHO);
        Talk.line(p,"",PellTexts.TAPES_LOOK);
        Talk.line(p,"",PellTexts.spine(h.deck,true)+".");
        var replies=new ArrayList<Talk.Reply>();
        for(int t=0;t<TAPES;t++){if(t==h.deck)continue;int tape=t;replies.add(Talk.act(PellTexts.spine(t,false),r->take(r,b,tape)));}
        replies.add(Talk.act(PellTexts.TAPES_LEAVE,Talk::close));
        Talk.ask(p,replies);
        return true;
    }
    /** A tape from the shelf: the deck's back in its case, this one in the deck, and played from its start. */
    private static void take(ServerPlayer p,BlockPos b,int tape){
        Talk.close(p);
        var l=p.serverLevel();var h=PellPeople.household(b);
        if(h.visit<2||tape==h.deck||tape<0||tape>=TAPES)return;
        h.deck=tape;play(l,b,h);
        PellHouse.cue(p,PellTexts.TAPE_SWAP);
    }

    // ------------------------------------------------------------------------------------------------ the deck
    /** The deck takes the tape in it and plays it from its start, for everybody near it. */
    static void play(ServerLevel l,BlockPos b,PellPeople.Household h){
        h.tapePlaying=true;h.tapeStart=l.getGameTime();h.tapeLine=0;h.tapePace=Dialogue.percent();h.tapeWatchers.clear();h.tapeTold.clear();
        screen(l,b,true);
        var at=Vec3.atCenterOf(b.offset(TV));l.playSound(null,at.x,at.y,at.z,PellRegistry.TAPE_PLAY.get(),SoundSource.BLOCKS,.8F,1F);
        for(var p:PellHouse.readers(l,b))if(atDeck(b,p))h.tapeWatchers.add(p.getUUID());
    }
    /** The picture gone, for everybody: nobody is watching any more. */
    static void stop(ServerLevel l,BlockPos b,PellPeople.Household h){
        h.tapePlaying=false;h.tapeLine=0;h.tapeWatchers.clear();
        screen(l,b,false);
        for(var id:h.tapeTold){var p=l.getServer().getPlayerList().getPlayer(id);if(p!=null)HousePackets.send(p,PellTapePayload.STOP);}
        h.tapeTold.clear();
    }
    /** Every tick of a week in, with readers at the house: her lines, who is watching, and the tape's end. */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        if(!h.tapePlaying)return;
        // A reader new to the house, or back, is sent the tape playing (and keeps seeing it until it ends or stops).
        for(var p:here)if(h.tapeTold.add(p.getUUID()))HousePackets.send(p,payload(b,h));
        int t=(int)(now-h.tapeStart);var s=schedule(h.deck,h.tapePace);var lines=PellTexts.TAPE_LINES[h.deck];
        if(t<=s[0])for(var p:here)if(atDeck(b,p))h.tapeWatchers.add(p.getUUID());
        h.tapeWatchers.removeIf(id->{var p=find(here,id);return p==null||!p.isAlive()||p.isSpectator()||p.position().distanceToSqr(screen(b))>AWAY*AWAY;});
        while(h.tapeLine<lines.length&&t>=s[h.tapeLine]){say(l,b,h,here,h.tapeLine);h.tapeLine++;}
        if(t>=s[lines.length]){
            int tape=h.deck;
            for(var id:h.tapeWatchers){var p=find(here,id);if(p!=null)watched(p,tape);}
            stop(l,b,h);
        }
    }
    /** One of her lines: heard from the set, and kept once in the journal of everybody near enough to hear it. */
    private static void say(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,int line){
        var at=screen(b);l.playSound(null,at.x,at.y,at.z,PellRegistry.TAPE_VOICE.get(),SoundSource.BLOCKS,.7F,1F);
        String text=PellTexts.TAPE_LINES[h.deck][line];long bit=1L<<(h.deck*4+line);
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||p.position().distanceToSqr(at)>EARSHOT*EARSHOT)continue;
            var own=PellHouse.own(p);long heard=own.getLong("TapeLines");if((heard&bit)!=0)continue;
            own.putLong("TapeLines",heard|bit);PellHouse.save(p,own);
            TalkHistory.heard(p,"pell/tape/"+h.deck,PellTexts.tapeHeading(h.deck),PellTexts.TAPE_VOICE,TalkHistory.where(p),text);
        }
    }
    /** A tape watched through: kept, and the reader's way through them in turn, from the first, goes on if it was the next one. */
    private static void watched(ServerPlayer p,int tape){
        var own=PellHouse.own(p);
        own.putInt("TapesWatched",own.getInt("TapesWatched")|1<<tape);
        int run=own.getInt("TapeRun");
        if(tape==run){run++;own.putInt("TapeRun",run);if(run>=TAPES)own.putBoolean("ReadTheTapes",true);}
        else if(tape>run)own.putBoolean("WatchedOutOfOrder",true);
        PellHouse.save(p,own);
    }
    /** June, come for the tin on her rounds: the deck turned off if it is playing, and anybody in earshot sees her do it. */
    static void juneStops(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here){
        if(!h.tapePlaying)return;
        var at=screen(b);
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&p.position().distanceToSqr(at)<=EARSHOT*EARSHOT)PellHouse.cue(p,PellTexts.TAPE_JUNE_OFF);
        stop(l,b,h);
    }
    /** The visit begun again: the eighth tape back in the deck, the deck stopped. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h){
        stop(l,b,h);h.deck=IN_DECK;
    }

    // ------------------------------------------------------------------------------------------------ helpers
    /** Watching from the start: in the shed, or near the screen with nothing in the way. */
    static boolean atDeck(BlockPos b,ServerPlayer p){
        if(!p.isAlive()||p.isSpectator())return false;
        var r=p.position().subtract(b.getX(),b.getY(),b.getZ());if(PellSite.inShed(r))return true;
        return PellNight.sees(p,screen(b),WATCH,-1);
    }
    /** The middle of the screen, in the world. */
    static Vec3 screen(BlockPos b){return Vec3.atCenterOf(b.offset(TV));}
    private static void screen(ServerLevel l,BlockPos b,boolean on){
        var at=b.offset(TV);var s=l.getBlockState(at);
        if(PellThing.is(s,PellThing.Kind.SHED_TV)&&s.getValue(PellThing.STAGE)!=(on?1:0))l.setBlock(at,s.setValue(PellThing.STAGE,on?1:0),3);
    }
    private static PellTapePayload payload(BlockPos b,PellPeople.Household h){return new PellTapePayload(b.offset(TV),FACING.get2DDataValue(),h.deck,h.tapeStart,h.tapePace);}
    private static @Nullable ServerPlayer find(List<ServerPlayer> here,UUID id){for(var p:here)if(p.getUUID().equals(id))return p;return null;}
}
