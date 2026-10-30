package io.github.knaitoe.theoldesthouse.journal;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPacing;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount;
import io.github.knaitoe.theoldesthouse.network.GroundingPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.199, journal step 6 (JOURNAL_DESIGN.md, "Writing as grounding"; the owner's idea 4). When one of the House's worst rooms
 * ends for a reader, a lingering static stays on them: a faint ringing in the ears, a slight unsteadiness of the view and a darkness
 * at the edges, all scaled by Minecraft's screen-effects setting down to none. It never blocks anything, and it fades by itself
 * over about five minutes in the game. One full line written in the reader's journal, at least twenty-four letters long, in an
 * ordinary hallway, with no story, hazard or hunter about, settles it within three seconds, under the graphite's sound. Nothing
 * says so.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Grounding {
    public static final String STATE="grounding_0199";
    /** Five minutes of play to fade by itself; a line written settles it in three seconds; a hunter this near keeps a hall from being quiet. */
    public static final int FADE=6000, SETTLE=60, LINE=24, HUNTER=24;
    private Grounding(){}

    /** The static on a reader now, 0 to 1. */
    public static float level(MinecraftServer server,UUID id){return Mth.clamp(LabyrinthData.get(server).stateEntry(STATE,id.toString()).getInt("Static")/(float)FADE,0F,1F);}
    static void set(MinecraftServer server,UUID id,int ticks){var data=LabyrinthData.get(server);var all=data.state(STATE);var own=all.getCompound(id.toString());own.putInt("Static",Math.max(0,ticks));all.put(id.toString(),own);data.setState(STATE,all);}
    static int ticks(MinecraftServer server,UUID id){return LabyrinthData.get(server).stateEntry(STATE,id.toString()).getInt("Static");}

    /** One of the worst rooms has ended for this reader: the static comes on, full. The same rooms the other hand answers (CoAuthor.DREAD). */
    public static void ended(ServerPlayer p,WitnessAccount.Story story){if(CoAuthor.DREAD.contains(story))begin(p);}
    public static void begin(ServerPlayer p){if(p.isSpectator())return;set(p.server,p.getUUID(),FADE);HousePackets.send(p,new GroundingPayload(1F,false));}

    /**
     * The reader has written in their journal. If the static is on them, they are in an ordinary hallway with nothing about, and the
     * writing has a new full line of at least twenty-four letters, it settles. Returns whether it did.
     */
    public static boolean wrote(ServerPlayer p,String before,String after){
        if(ticks(p.server,p.getUUID())<=0||!quiet(p)||!newLine(before,after))return false;
        set(p.server,p.getUUID(),0);HousePackets.send(p,new GroundingPayload(0F,true));
        return true;
    }
    /** Whether the writing has a line of at least twenty-four letters that was not there before. */
    public static boolean newLine(String before,String after){
        var old=new HashSet<String>();for(String line:before.split("\n"))old.add(line.trim());
        for(String line:after.split("\n")){String t=line.trim();if(t.length()>=LINE&&!old.contains(t))return true;}
        return false;
    }
    /** An ordinary hallway of the House with no story, hazard or hunter about: the only place writing settles the static. */
    public static boolean quiet(ServerPlayer p){
        if(!p.level().dimension().equals(HouseDimensions.INTERIOR))return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());
        if(place==null||!LabyrinthPacing.ordinary(place)||place.isVignette())return false;
        return p.level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(HUNTER),m->m.isAlive()&&(m instanceof Enemy||m.getTarget()==p)).isEmpty();
    }
    /** How strongly the static shows: its level scaled by the reader's screen-effects setting, down to none. */
    public static float strength(float level,float screenEffects){return Mth.clamp(level,0F,1F)*Mth.clamp(screenEffects,0F,1F);}

    /** It fades only while the reader is playing: a second for every second they are in the world. */
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%20!=0)return;
        for(var p:server.getPlayerList().getPlayers()){
            int left=ticks(server,p.getUUID());if(left<=0)continue;
            left=Math.max(0,left-20);set(server,p.getUUID(),left);
            HousePackets.send(p,new GroundingPayload(left/(float)FADE,false));
        }
    }
    /** Back in the world after time away, whatever was left of it is still there. */
    @SubscribeEvent public static void loggedIn(PlayerEvent.PlayerLoggedInEvent e){
        if(e.getEntity() instanceof ServerPlayer p){int left=ticks(p.server,p.getUUID());if(left>0)HousePackets.send(p,new GroundingPayload(left/(float)FADE,false));}
    }
}
