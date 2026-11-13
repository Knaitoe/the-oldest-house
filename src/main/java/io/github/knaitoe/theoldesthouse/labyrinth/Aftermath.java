package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.244, aftermath rooms (docs/AFTERMATH_0_4_244.md). The owner, 2 November: "Let some finished scenes return as quiet aftermath
 * rooms. The pool is covered and closed. Vacant Possession is empty, or its window remains lit because you returned the chair. No new
 * objective or Witness credit; players recognize the consequences of what they did."
 * <p>
 * A reader who has finished one of these scenes can be dealt it again, at most {@link #RETURNS} times and never within their last
 * eight crossings. It comes back as they left it, with nobody in it to ask anything of them: nothing to do there, nothing to earn, the
 * way on where it always was. Each scene dresses its own aftermath when a finished reader is in it alone (a reader who has not finished
 * it gets the scene as it always was, and anyone finished with them simply walks through it).
 */
public final class Aftermath {
    private Aftermath(){}
    /**
     * The scenes that come back. A scene joins by being listed here and dressing its own aftermath in its own controller:
     * <ul>
     * <li>it shows its aftermath only when {@link #alone} says everyone in it has finished it, and plays as always otherwise; a scene
     *     whose every difference is already private to each reader (the Masque, the Ghosts Set, Three Fingers' threads), or whose
     *     consequence already stands in the room for everyone (the child's room), needs no solitude;</li>
     * <li>what differs between readers' outcomes is shown to each reader alone (private actors and block views), never by
     *     changing the room for everyone;</li>
     * <li>it calls {@link #counted} once a visit for each finished reader it shows the aftermath to, with a quiet line;</li>
     * <li>its door sound answers for a finished listener what they would find (DoorSounds' listener-aware source).</li>
     * </ul>
     * The House does the rest: dealing it back ({@link #canDeal}), keeping it solitary (GroupCrossings), keeping a deliberate scent
     * on unfinished stories (LabyrinthDealer), and never giving credit (the scenes' endings check Completed).
     */
    public static final Set<LabyrinthPlace> PLACES=EnumSet.of(LabyrinthPlace.LAST_SWIMMER,LabyrinthPlace.VACANT_POSSESSION,LabyrinthPlace.MASQUE,LabyrinthPlace.GHOSTS_SET,
            LabyrinthPlace.THREE_FINGERS,LabyrinthPlace.CHILD_ROOM,LabyrinthPlace.PELL_HOUSE);
    /** How many times a finished scene comes back to the reader who finished it. */
    public static final int RETURNS=2;
    /** 0.4.542: how many times this scene comes back; the Pell house once, "FOR SALE" or lived in (PellAftermath). */
    public static int limit(LabyrinthPlace place){return place==LabyrinthPlace.PELL_HOUSE?1:RETURNS;}
    /**
     * 0.4.542: whether a reader has finished a scene: its account read ("Completed"), or an ending that has landed for them, read or not
     * ("Ended": the Pell house's at first light).
     */
    public static boolean finished(CompoundTag own){return own.getBoolean("Completed")||own.getBoolean("Ended");}
    /** Whether everyone in a scene has finished it, so that it can stand as its aftermath. No one in it is not alone. */
    public static boolean alone(Collection<ServerPlayer> readers,LabyrinthPlace place){
        if(readers.isEmpty())return false;
        for(var r:readers)if(!finished(LiteraryVignettes.personal(LabyrinthData.get(r.server),r.getUUID(),place)))return false;
        return true;
    }
    /** Each reader's count of returns to a scene, and the visit the last one was counted on. */
    public static final String COUNT="Aftermath244",AT="AftermathAt244";

    /** Whether this finished scene can come back to this reader now. */
    public static boolean canDeal(LabyrinthData d,UUID reader,LabyrinthPlace place){
        if(!PLACES.contains(place))return false;
        var own=LiteraryVignettes.personal(d,reader,place);
        return finished(own)&&own.getInt(COUNT)<limit(place)&&d.recentVisit(reader,place)<0;
    }
    /** Whether this scene is one this reader has finished and would find again only as its aftermath. */
    public static boolean returning(LabyrinthData d,UUID reader,LabyrinthPlace place){
        return PLACES.contains(place)&&finished(LiteraryVignettes.personal(d,reader,place));
    }
    /** For tests: how many times this scene has come back to this reader. */
    public static int returns(LabyrinthData d,UUID reader,LabyrinthPlace place){return LiteraryVignettes.personal(d,reader,place).getInt(COUNT);}
    /**
     * Counts this visit as a return, once a visit. True the first time, when the scene shows its aftermath to this reader. The visit's
     * beat is marked as well, so that the reader's next arrival counts as a new visit (LiteraryVignettes.onArrive moves a reader's
     * visit on only past a beat), and a third return is never mistaken for the second.
     */
    public static boolean counted(ServerPlayer p,LabyrinthPlace place){
        var d=LabyrinthData.get(p.server);CompoundTag own=LiteraryVignettes.personal(d,p.getUUID(),place);
        if(!count(own))return false;LiteraryVignettes.save(d,p.getUUID(),place,own);return true;
    }
    /** The same, on a record the caller holds and saves itself (LiteraryVignettes' own tick saves the copy it read afterwards). */
    public static boolean count(CompoundTag own){
        int visit=own.getInt("Visit");if(own.contains(AT)&&own.getInt(AT)==visit)return false;
        own.putInt(AT,visit);own.putInt(COUNT,own.getInt(COUNT)+1);own.putInt("Beat",Math.max(own.getInt("Beat"),visit));return true;
    }
}
