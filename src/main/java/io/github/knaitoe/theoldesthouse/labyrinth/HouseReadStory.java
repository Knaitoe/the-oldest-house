package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.HouseReadLinePayload;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;

/**
 * 0.4.166: the quiet rooms as a story the House tells about the reader (owner: "Make a story about the player. A line should
 * trigger at the beginning and end of each personality vignette ... the vignettes should feel harsher than simply popping in
 * and out of a room"; "the story should sort of 'welcome' the player into the house, end with the warning that this house has
 * ghosts in it"). Each room opens on black with the House's line for it and closes the same way, cut off rather than faded,
 * and the last room ends on the warning. The doors stay locked until the room has what the House came for (owner: "The doors
 * can also just be locked ... Until the vignettes are completed."). Nothing here names what the House read.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HouseReadStory {
    private HouseReadStory(){}
    /** Pages within one card. */
    public static final String PAGE="\f";
    public static final String WELCOME="Come in. Shut the door behind you; it sticks. Every house keeps a story about the people who live in it, and you've only just arrived, so I'll have to learn yours.";
    // 0.4.172 (owner: "I would have played it off of the domestic setting you just had the player in. ended with something like -
    // every house is a haunted house. But a warning, before you proceed: this house has ghosts in it."). 0.4.201, the owner's
    // own words: "Every house is haunted. Old arguments and worse habits keep these walls standing. A word of caution before the
    // door closes: This House Has Ghosts in It". The last page is the title.
    public static final String HAUNTED="Every house is haunted. Old arguments and worse habits keep these walls standing.",WARNING_LEAD="A word of caution before the door closes:",WARNING="This House Has Ghosts in It";

    public static String opening(HouseReadSites.Room room){return switch(room){
        case WAITING->"You grew up the way everyone does, mostly by waiting. A clock, a row of chairs, and a door that would open when it was ready.";
        case BATHROOM->"Some nights you came home late and worn through, and your hands knew what to do before bed.";
        case COATROOM->"Once you came in out of the rain, soaked to the skin, into a hall where nobody was looking, and dressed yourself.";
        case SHELTER->"There was a summer storm, and a roof over the water, and two people you hadn't found the nerve to talk to.";
        case PORCH->"And at the end of every day, somebody called you in for supper.";
        case KITCHEN->"You were small once. It was four o'clock, the kitchen was warm, and somebody had left the crayons out on the table.";
    };}
    public static String closing(HouseReadSites.Room room){return switch(room){
        case WAITING->"They called your name, and you went through.";
        case BATHROOM->"The light went out. You found the bed in the dark and slept.";
        case COATROOM->"Don't mind me. I wasn't looking.";
        case SHELTER->"The song ended. The rain kept on, and you went home in it.";
        case PORCH->"Welcome home. I've set a place for you at the table.";
        case KITCHEN->"There you are. Now I'll know you when I see you.";
    };}
    /** The sound each room is cut off with: its own door, switch or needle, heard in the dark. */
    static String cut(HouseReadSites.Room room){return switch(room){
        case WAITING->"minecraft:block.wooden_door.close";
        case BATHROOM->"minecraft:block.lever.click";
        case COATROOM->"minecraft:block.wooden_door.close";
        case SHELTER->"minecraft:block.note_block.bass";
        case PORCH->"minecraft:block.wooden_trapdoor.close";
        case KITCHEN->"minecraft:block.iron_trapdoor.close";
    };}

    /** How long a card holds, in ticks: a beat to arrive, then time to read every page. Shared with the client. */
    public static int ticks(String text){int total=0;for(String page:text.split(PAGE,-1))total+=page(page);return total;}
    public static int page(String page){int words=page.isBlank()?0:page.trim().split("\\s+").length;return Math.min(200,36+words*6);}

    /** Whether the House has what this room is for. Until then its doors stay locked. */
    public static boolean ready(ServerPlayer p,HouseReadSites.Room room){
        var visit=HouseReadSites.state(p.server,p.getUUID()).getCompound(room.key());
        var seen=HouseRead.seen(LabyrinthData.get(p.server),p.getUUID()).getCompound(room.key());
        return switch(room){
            case WAITING->visit.getBoolean("Called");
            case BATHROOM->visit.getBoolean("Done");
            case COATROOM->seen.contains("Took")||seen.contains("Mirror");
            case SHELTER->visit.getBoolean("Settled")||visit.getInt("SongAge")>=900&&answered(seen);
            case PORCH->seen.contains("First");
            case KITCHEN->visit.getBoolean("Hung");
        };
    }
    /** The shelter has its answer once the reader has gone to someone, stayed between them, or walked out past both. */
    static boolean answered(CompoundTag seen){return seen.contains("Looked")||seen.contains("Stood")||seen.getBoolean("FirstLantern");}
    static void locked(ServerPlayer p,HouseReadSites.Room room,net.minecraft.core.BlockPos at){
        // 0.4.202 (owner, stuck at the kitchen's near door for ten minutes): the kitchen says what its door is waiting for.
        p.displayClientMessage(Component.literal(room==HouseReadSites.Room.KITCHEN?HouseReadKitchen.waiting(p):"The door won't open."),true);HouseReadSites.sound(p,SoundEvents.CHEST_LOCKED,at,.6f);
    }

    private record Card(HouseReadLinePayload line,HouseReadSites.Room room,boolean closing,boolean forward,
                        long until,long goAt) {
        boolean manual(){return line.manual();}
        Card released(long now){
            return new Card(new HouseReadLinePayload(line.text(),line.sound(),line.title(),line.token(),false),
                    room,closing,forward,now,now);
        }
    }
    private static final Map<UUID,Card> CARDS=new HashMap<>();
    private static final Map<UUID,HouseReadLinePayload> LAST=new HashMap<>();
    private static int nextToken=1;

    /** On arrival, the room waits while the reader sees the opening card. */
    static void open(ServerPlayer p,HouseReadSites.Room room,boolean first){
        show(p,room,(first?WELCOME+PAGE:"")+opening(room),"",false,false,false);
    }

    /** The closing card covers the transition out of the room. */
    static void close(ServerPlayer p,HouseReadSites.Room room,boolean forward,boolean last){
        if(closing(p))return;
        String text=closing(room)+(last?PAGE+HAUNTED+PAGE+WARNING_LEAD+PAGE+WARNING:"");
        show(p,room,text,cut(room),last,true,forward);
    }

    private static void show(ServerPlayer p,HouseReadSites.Room room,String text,String sound,boolean title,boolean closing,boolean forward){
        var line=new HouseReadLinePayload(text,sound,title,nextToken++,false);
        long now=p.serverLevel().getGameTime();
        int length=ticks(text);
        CARDS.put(p.getUUID(),new Card(line,room,closing,forward,now+length,now+Math.max(10,length-20)));
        LAST.put(p.getUUID(),line);
        if(p.connection!=null)HousePackets.send(p,line);
    }

    /** Only the player holding this card can extend it or release its transition. */
    public static void cardAction(ServerPlayer p,int token,boolean finished){
        var id=p.getUUID();
        var card=CARDS.get(id);
        if(card==null||card.line().token()!=token||HouseReadSites.inside(p)!=card.room()||!p.isAlive()||p.isSpectator())return;
        long now=p.serverLevel().getGameTime();
        if(finished){
            if(card.manual())CARDS.put(id,card.released(now));
            return;
        }
        if(card.manual()||now>=(card.closing()?card.goAt():card.until()))return;
        var old=card.line();
        var line=new HouseReadLinePayload(old.text(),"",old.title(),token,true);
        CARDS.put(id,new Card(line,card.room(),card.closing(),card.forward(),card.until(),card.goAt()));
        if(p.connection!=null)HousePackets.send(p,line);
    }

    /** While a card holds the reader, the room does not go on around them. */
    public static boolean holding(ServerPlayer p){
        var card=CARDS.get(p.getUUID());
        return card!=null&&(card.manual()||card.closing()||p.serverLevel().getGameTime()<card.until());
    }

    static void tick(ServerPlayer p,@Nullable HouseReadSites.Room inside){
        var card=CARDS.get(p.getUUID());
        if(card==null)return;
        if(inside!=card.room()||!p.isAlive()||p.isSpectator()){CARDS.remove(p.getUUID());return;}
        if(card.manual())return;
        long now=p.serverLevel().getGameTime();
        if(!card.closing()){
            if(now>=card.until())CARDS.remove(p.getUUID());
        }else if(now>=card.goAt()&&!LabyrinthDoors.isBusy(p)){
            CARDS.remove(p.getUUID());
            HouseReadSites.depart(p,card.forward());
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        var card=CARDS.get(p.getUUID());
        if(card!=null&&card.manual())CARDS.put(p.getUUID(),card.released(p.serverLevel().getGameTime()));
    }

    public static boolean closing(ServerPlayer p){var card=CARDS.get(p.getUUID());return card!=null&&card.closing();}
    static void clear(){CARDS.clear();LAST.clear();}
    /** The last card this reader was shown, for tests and diagnostics. */
    public static @Nullable HouseReadLinePayload last(UUID reader){return LAST.get(reader);}
}
