package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.SpokenPayload;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * 0.4.329 (owner: a reader's chosen lines should be shown to other players). When a reader says something from the conversation
 * panel, the words show over their head for every other player within earshot, the way the House's people speak: never in chat.
 * Only replies that are things said are spoken this way; a reply that is something done (taking a cup, walking on, a word breathed
 * onto glass) is marked {@link Talk#act} and shows nothing. The speaker has the panel and is not sent their own words.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Spoken {
    private Spoken(){}
    /** How far a reader's words carry, in blocks. */
    public static final double EARSHOT=16;
    private record Heard(UUID speaker,String text){}
    private static final Map<UUID,Heard> LAST=new HashMap<>();

    /** The reader says these words aloud: everyone else near enough to hear sees them over the reader's head, as they come out of the
     * reader's mouth (0.4.354: broken, for a reader turned at the flood station after dark). Returns the words as they were heard. */
    public static String say(ServerPlayer p,String text){
        var said=voice(p,text);sayAsHeard(p,said);return said;
    }
    /** What a reader's words come out as: their own, unless something has got into their throat (0.4.354, LastCrossingNightTwo). */
    public static String voice(ServerPlayer p,String text){return text==null?"":LastCrossingNightTwo.voice(p,text);}
    /** Words already as they come out, shown over the reader's head to everyone near enough to hear. */
    public static void sayAsHeard(ServerPlayer p,String text){
        if(p.isSpectator()||text.isBlank())return;
        var payload=new SpokenPayload(p.getId(),text,ticks(text));
        for(var other:p.serverLevel().players()){
            if(other==p||other.distanceToSqr(p)>EARSHOT*EARSHOT)continue;
            LAST.put(other.getUUID(),new Heard(p.getUUID(),text));
            HousePackets.send(other,payload);
        }
    }
    /** How long words stay up: long enough to read, longer for more of them. */
    public static int ticks(String text){return Mth.clamp(60+text.length()*2,60,180);}

    // ------------------------------------------------------------------------------------------------ for tests
    /** The last words this listener heard from that speaker, or empty if the last words they heard were someone else's or none. */
    public static String lastHeard(ServerPlayer listener,ServerPlayer speaker){var h=LAST.get(listener.getUUID());return h==null||!h.speaker().equals(speaker.getUUID())?"":h.text();}
    /** The last words this listener heard from anyone, or empty. */
    public static String lastHeard(ServerPlayer listener){var h=LAST.get(listener.getUUID());return h==null?"":h.text();}

    @SubscribeEvent public static void stopped(ServerStoppedEvent event){LAST.clear();}
}
