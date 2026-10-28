package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Locale;

/**
 * How a reader was part of a shared vignette, for their own account (owner, October 28). Every reader is still "you"
 * in their own account; each role has its own drafted prose, never the actor's account with the subject swapped.
 */
public enum VignetteRole {
    /** Did the key act. Every account written before 0.4.146 is an actor's. */
    ACTOR,
    /** Was there throughout and watched another reader act. */
    WITNESS,
    /** Joined the thread after its first visit. */
    PARTWAY,
    /** Came back after the ending happened without them. */
    AFTERMATH,
    /** Saw the consequence of something done while they were away. */
    ABSENT;

    public String id(){return name().toLowerCase(Locale.ROOT);}
    public static VignetteRole of(String id){for(var role:values())if(role.id().equals(id))return role;return ACTOR;}
}
