package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.TalkPayload;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.257, the conversation panel (docs/A_PLACE_FOR_YOU_0_4_257.md). One reader's open exchange: who it is with, the lines said
 * so far (the reader's own replies among them) and the replies on offer. The panel only shows it and sends back which reply was
 * chosen; the scene decides the rest. Each offer has its own token, so a reply is honoured once and a stale click does nothing.
 * Closing the panel leaves the exchange waiting; the scene reopens it with {@link #resume} when the reader turns back to whoever
 * it is with. Nothing here is shared with another reader. The open exchange is not saved (a scene keeps its own story and offers its
 * question again), but what the panel showed is kept for the reader's journal (0.4.266, TalkHistory).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Talk {
    private Talk(){}
    /** A reply: what it says (its intention, in the reader's words) and what happens when it is chosen. */
    public record Reply(String label,Consumer<ServerPlayer> then){}
    public static Reply reply(String label,Consumer<ServerPlayer> then){return new Reply(label,then);}
    /** The most lines one exchange keeps for the panel; older ones drop off the top. */
    public static final int MAX_LINES=48;

    static final class Exchange {
        final String key,who;int token;boolean ended;
        Predicate<ServerPlayer> available;
        long disconnectedAt=-1;
        final List<String[]> lines=new ArrayList<>();List<Reply> replies=List.of();
        /** 0.4.266: the exchange as kept for the reader's journal (TalkHistory): its number, where and when, and every line, past the panel's 48. */
        int number;String where="";long day;final List<String[]> all=new ArrayList<>();
        Exchange(String key,String who){this.key=key;this.who=who;}
        Exchange(ServerPlayer p,String key,String who){this(key,who);number=TalkHistory.next(p);where=TalkHistory.where(p);day=TalkHistory.day(p);}
    }
    private static final Map<UUID,Exchange> OPEN=new HashMap<>();
    private static int tokens=1;
    public static void clearAll(){OPEN.clear();}

    // ------------------------------------------------------------------------------------------------ for scenes
    /** Starts a new exchange with someone, setting aside any other the reader had open. */
    public static void begin(ServerPlayer p,String key,String who){OPEN.put(p.getUUID(),new Exchange(p,key,who));}
    /** An encounter may keep its exchange across a disconnect while the same offer is still available. */
    static void beginResumable(ServerPlayer p,String key,String who,Predicate<ServerPlayer> available){
        begin(p,key,who);
        OPEN.get(p.getUUID()).available=available;
    }
    /** The key of the reader's open exchange, or empty. */
    public static String key(ServerPlayer p){var e=OPEN.get(p.getUUID());return e==null?"":e.key;}
    /** A line said in the reader's open exchange (a new one is begun with the speaker if none is). The panel is not sent until {@link #ask} or {@link #end}. */
    public static void line(ServerPlayer p,String speaker,String text){
        var e=OPEN.computeIfAbsent(p.getUUID(),k->new Exchange(p,speaker,speaker));e.lines.add(new String[]{speaker,text});e.all.add(new String[]{speaker,text});
        while(e.lines.size()>MAX_LINES)e.lines.removeFirst();while(e.all.size()>TalkHistory.LINES)e.all.removeFirst();
    }
    /** Offers replies and opens the panel (or brings an open one up to date). */
    public static void ask(ServerPlayer p,List<Reply> replies){
        var e=OPEN.get(p.getUUID());if(e==null)return;e.replies=List.copyOf(replies);e.ended=replies.isEmpty();e.token=tokens++;send(p,e,true);
    }
    public static void ask(ServerPlayer p,Reply... replies){ask(p,List.of(replies));}
    /** The exchange has said what it had to: the panel stays open to be read, with nothing left to choose. */
    public static void end(ServerPlayer p){ask(p,List.of());}
    /** Whether the reader has an exchange with replies still open. */
    public static boolean waiting(ServerPlayer p){var e=OPEN.get(p.getUUID());return e!=null&&!e.replies.isEmpty();}
    public static boolean waiting(ServerPlayer p,String key){var e=OPEN.get(p.getUUID());return e!=null&&!e.replies.isEmpty()&&e.key.equals(key);}
    static boolean interrupted(ServerPlayer p,String key){
        var e=OPEN.get(p.getUUID());
        return e!=null&&e.disconnectedAt>=0&&e.key.equals(key);
    }
    /** Reopens this speaker's current question or the end of an interrupted exchange. */
    public static boolean resume(ServerPlayer p,String key){
        var e=OPEN.get(p.getUUID());
        if(e==null||!e.key.equals(key)||e.replies.isEmpty()&&e.disconnectedAt<0)return false;
        if(e.available!=null&&!e.available.test(p)){close(p);return false;}
        if(e.disconnectedAt>=0){
            e.disconnectedAt=-1;
            e.token=tokens++;
        }
        send(p,e,true);
        return true;
    }
    /** Closes the reader's exchange and its panel. */
    public static void close(ServerPlayer p){if(OPEN.remove(p.getUUID())!=null)io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new TalkPayload(false,new CompoundTag()));}

    // ------------------------------------------------------------------------------------------------ from the panel
    /** A reply from the panel: honoured once, for the offer it was made to, and only while that offer stands. */
    public static void reply(ServerPlayer p,int token,int index){
        var e=OPEN.get(p.getUUID());if(e==null||e.disconnectedAt>=0||e.token!=token||index<0||index>=e.replies.size())return;
        if(e.available!=null&&!e.available.test(p)){close(p);return;}
        var chosen=e.replies.get(index);e.replies=List.of();e.token=tokens++;e.lines.add(new String[]{"",chosen.label()});
        // 0.4.275: kept marked as the reader's own reply, so it is never taken for a line of narration (the listener offers it back).
        e.all.add(new String[]{"",chosen.label(),"R"});
        chosen.then().accept(p);
        // A reply that asked nothing further leaves the exchange to be read.
        var now=OPEN.get(p.getUUID());if(now==e&&e.replies.isEmpty()&&!e.ended){e.ended=true;send(p,e,false);}
    }

    // ------------------------------------------------------------------------------------------------ for tests
    /** The replies the reader is being offered, in order. */
    public static List<String> replies(ServerPlayer p){var e=OPEN.get(p.getUUID());return e==null?List.of():e.replies.stream().map(Reply::label).toList();}
    /** The open exchange's lines, as "speaker: text" (the reader's own as "you: text"). */
    public static List<String> lines(ServerPlayer p){var e=OPEN.get(p.getUUID());if(e==null)return List.of();return e.lines.stream().map(l->(l[0].isEmpty()?"you":l[0])+": "+l[1]).toList();}
    public static int token(ServerPlayer p){var e=OPEN.get(p.getUUID());return e==null?-1:e.token;}
    /** Chooses the offered reply that starts with these words, as the panel would; false if none does. */
    public static boolean choose(ServerPlayer p,String start){var list=replies(p);for(int i=0;i<list.size();i++)if(list.get(i).startsWith(start)){reply(p,token(p),i);return true;}return false;}

    private static void send(ServerPlayer p,Exchange e,boolean open){
        var t=new CompoundTag();t.putInt("Token",e.token);t.putString("Who",e.who);t.putBoolean("Ended",e.ended);
        var lines=new ListTag();for(var l:e.lines){var line=new CompoundTag();line.putString("S",l[0]);line.putString("T",l[1]);lines.add(line);}t.put("Lines",lines);
        var replies=new ListTag();for(var r:e.replies)replies.add(StringTag.valueOf(r.label()));t.put("Replies",replies);
        io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new TalkPayload(open,t));
        // What reached the panel is the reader's to keep (TalkHistory), with their own replies.
        TalkHistory.keep(p,e.number,e.who,e.where,e.day,e.all);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        var exchange=OPEN.get(p.getUUID());
        if(exchange==null)return;
        if(exchange.available==null)OPEN.remove(p.getUUID());
        else exchange.disconnectedAt=p.server.overworld().getGameTime();
    }

    @SubscribeEvent public static void expire(ServerTickEvent.Post event){
        long now=event.getServer().overworld().getGameTime();
        if(now%20==0)OPEN.values().removeIf(e->e.disconnectedAt>=0&&now-e.disconnectedAt>Truant.APART);
    }

    @SubscribeEvent public static void stopped(ServerStoppedEvent event){clearAll();}
}
