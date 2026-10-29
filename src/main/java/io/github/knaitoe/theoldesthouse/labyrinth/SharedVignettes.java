package io.github.knaitoe.theoldesthouse.labyrinth;

import static io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace.*;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Which vignettes friends go into together (owner, October 28), and which of them are drafted for every way of being
 * there. Friends follow each other into any shared scene and each still plays it as their own; only a drafted scene
 * gives a reader who watched, joined late or missed the ending their own account and the Witness credit.
 */
public final class SharedVignettes {
    /** A drafted shared scene: its account for each way of being there, and the beats that need one particular member. */
    public interface Scene {
        /** The account for a reader who was there as {@code role}; {@code actor} is the player name of who acted. Null where that way of being there is not drafted. */
        @Nullable String line(VignetteRole role,String outcome,String actor);
        /** The member the thread's next beat needs, if any. Without them the House deals the others something else. */
        default @Nullable UUID waitsFor(LabyrinthData data,CompoundTag thread){return null;}
        /** The holder has been gone too long: whatever they hold goes back where it came from. Changes to the thread are kept. */
        default void release(MinecraftServer server,CompoundTag thread){}
        /** How a member was part of the ending, where the scene knows better than the thread, such as one who saw only the consequence. */
        default VignetteRole role(LabyrinthData data,CompoundTag thread,UUID member,VignetteRole proposed){return proposed;}
        /** The story belongs to the world rather than a thread (0.4.148): everyone in the scene when it ends watched it. */
        default boolean whole(){return false;}
    }
    /**
     * Private scenes that stand once rather than as a copy for each reader (owner, October 28): reading to Harrigan,
     * the confession in the reader's own hand, the Holy Rabbit's one child, the Wheel house measuring the reader against
     * Mother, the Whale's letters, the canoe's recording, the diner's one diner and the split-feed bedroom's own body.
     * They are one reader's at a time. 0.4.158: so is the clapping game, played blindfolded and alone (owner, October 29).
     */
    private static final Set<LabyrinthPlace> ONE_AT_A_TIME=EnumSet.of(HARRIGAN,CONFESSION,HOLY_RABBIT,WHEEL,WHALE,PHONE_CANOE,SECOND_WAITRESS,MISSING_SLEEP,HIDE_AND_CLAP);
    /** One reader's own: the backseat's one seat, the private sites and copies, the Red Room, and the scenes above. The quiet rooms are not dealt at all. */
    private static final Set<LabyrinthPlace> PRIVATE=EnumSet.of(BACKSEAT,SILO,BORRASCA,FAMILY_COPY,OLD_CABIN,RED_ROOM);
    static{PRIVATE.addAll(ONE_AT_A_TIME);}
    private static final Map<LabyrinthPlace,Scene> DRAFTED=new EnumMap<>(LabyrinthPlace.class);
    private SharedVignettes(){}
    static {
        // 0.4.147: the monkey's paw, the first scene drafted for every way of being there; 0.4.148: the drowned town.
        draft(THREE_FINGERS,ThreeFingers.SHARED);
        draft(DROWNED_TOWN,DrownedTown.SHARED);
    }

    public static boolean followable(@Nullable LabyrinthPlace place){
        return place!=null&&place.isVignette()&&place.slot()>=0&&!PRIVATE.contains(place);
    }
    public static boolean privateScene(LabyrinthPlace place){return PRIVATE.contains(place);}
    /** Someone other than {@code reader}, living and not a spectator, is in a private scene that stands once: it is theirs until they leave. */
    public static @Nullable ServerPlayer occupant(MinecraftServer server,@Nullable LabyrinthPlace place,UUID reader){
        if(place==null||!ONE_AT_A_TIME.contains(place))return null;
        for(var p:server.getPlayerList().getPlayers())if(!p.getUUID().equals(reader)&&IndianLakeRooms.inside(p,place))return p;
        return null;
    }
    public static @Nullable Scene drafted(@Nullable LabyrinthPlace place){return place==null?null:DRAFTED.get(place);}
    public static Set<LabyrinthPlace> drafted(){return Collections.unmodifiableSet(DRAFTED.keySet());}
    /** A scene whose accounts are written for every way of being there. */
    public static void draft(LabyrinthPlace place,Scene scene){if(followable(place))DRAFTED.put(place,scene);}
    /** Fixtures only: forget a drafted scene they registered. */
    public static void undraft(LabyrinthPlace place){DRAFTED.remove(place);}
}
