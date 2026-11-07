package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;

/**
 * 0.4.334, what every Sphinx crossing shares (docs/SPHINX_0_4_334.md): the records the House keeps of each reader's meetings with her,
 * the co-op survey pins, and the latch in motion.
 * <p>
 * "Keep the physical result of a crossing shared. Keep each reader's answer and conduct separate." Each crossing keeps one shared room
 * under {@code sphinx_0306}, and each reader keeps one record per riddle: Heard, First (1 right, 2 wrong; the first deliberate answer to
 * that riddle), Solved and SolvedAt (a correct answer, by latch or pin, later ones included), Walked (deliberate departures with the
 * question heard and unanswered), Forced (pulled a stop pin), Retreated, Swept, Pinned.
 * <p>
 * The survey pins (the owner's co-op rule): "let each reader set a small survey pin at their pick while the latches are still shut. The
 * first committed crossing locks the submitted answers for that attempt, then moves the room forward for everybody. The reader at the
 * latch records that choice if they haven't placed a pin. Someone who pinned the right gate still earns credit if a friend pulled a
 * different latch. A reader who leaves their pin in hand remains unanswered." In solo play the first latch is the first answer.
 * <p>
 * "A latch already in motion consumes the current attempt once, so simultaneous use can't double-trigger the Sphinx or reopen a sealed
 * route": {@link #begin} refuses a second commitment while the first is still moving.
 */
public final class SphinxCore {
    private SphinxCore(){}
    public static final String STATE=Sphinx.STATE;
    /** The four riddles, in the order the House offers them: the domestic halls, the stone halls, the dark halls, outside. */
    public static final List<String> RIDDLES=List.of("early","stair","shelf","field");
    /** How long a committed latch (or rail, ladder, gate) is in motion: no other commitment in that room until it settles. */
    public static final int MOTION=4;

    // ------------------------------------------------------------------ the shared rooms
    public static CompoundTag room(ServerLevel l,String key){return LabyrinthData.get(l.getServer()).state(STATE).getCompound(key);}
    public static void room(ServerLevel l,String key,CompoundTag room){var data=LabyrinthData.get(l.getServer());var all=data.state(STATE);all.put(key,room);data.setState(STATE,all);}

    // ------------------------------------------------------------------ each reader's records
    public static CompoundTag record(ServerLevel l,UUID reader,String riddle){return LabyrinthData.get(l.getServer()).stateEntry(STATE,reader.toString()).getCompound(riddle);}
    public static void record(ServerLevel l,UUID reader,String riddle,CompoundTag rec){
        var data=LabyrinthData.get(l.getServer());var all=data.state(STATE);var own=all.getCompound(reader.toString());own.put(riddle,rec);all.put(reader.toString(),own);data.setState(STATE,all);
    }
    /** One deliberate answer: the first is kept for accuracy; a correct one, now or later, is a solve, dated for the stone's carving. */
    public static void answer(ServerLevel l,UUID reader,String riddle,boolean right){
        var rec=record(l,reader,riddle);if(!rec.contains("First"))rec.putInt("First",right?1:2);
        if(right){rec.putBoolean("Solved",true);rec.putLong("SolvedAt",l.getGameTime());}
        record(l,reader,riddle,rec);
    }
    /** A deliberate departure with the question heard and unanswered, while she was not provoked. */
    public static void walked(ServerLevel l,UUID reader,String riddle){var rec=record(l,reader,riddle);rec.putInt("Walked",rec.getInt("Walked")+1);record(l,reader,riddle,rec);}
    public static void forced(ServerLevel l,UUID reader,String riddle){var rec=record(l,reader,riddle);rec.putBoolean("Forced",true);record(l,reader,riddle,rec);}
    public static void heard(ServerLevel l,UUID reader,String riddle){var rec=record(l,reader,riddle);rec.putBoolean("Heard",true);record(l,reader,riddle,rec);}

    // ------------------------------------------------------------------ the survey pins
    /** Who has pinned which choice in this room's current attempt. */
    public static Map<UUID,Integer> pins(ServerLevel l,String key){
        var out=new LinkedHashMap<UUID,Integer>();var tag=room(l,key).getCompound("Pins");
        for(var k:tag.getAllKeys()){try{out.put(UUID.fromString(k),tag.getInt(k));}catch(IllegalArgumentException ignored){}}
        return out;
    }
    public static int pinCount(ServerLevel l,String key,int choice){int n=0;for(int c:pins(l,key).values())if(c==choice)n++;return n;}
    public static int pinOf(ServerLevel l,String key,UUID reader){var tag=room(l,key).getCompound("Pins");return tag.contains(reader.toString())?tag.getInt(reader.toString()):-1;}
    /**
     * A reader sets their pin at a choice, moves it from another, or takes it back out (the same choice again). Returns the choice their pin
     * now marks, or -1 if they hold it again.
     */
    public static int pin(ServerLevel l,String key,UUID reader,int choice){
        var r=room(l,key);var tag=r.getCompound("Pins");int was=tag.contains(reader.toString())?tag.getInt(reader.toString()):-1;
        if(was==choice)tag.remove(reader.toString());else tag.putInt(reader.toString(),choice);
        r.put("Pins",tag);room(l,key,r);return was==choice?-1:choice;
    }
    /** A reader leaves or goes offline with their pin set: it comes out with them, unanswered. */
    public static boolean unpin(ServerLevel l,String key,UUID reader){
        var r=room(l,key);var tag=r.getCompound("Pins");if(!tag.contains(reader.toString()))return false;
        tag.remove(reader.toString());r.put("Pins",tag);room(l,key,r);return true;
    }
    /**
     * Starts a commitment: false while another latch in the room is still moving, so two readers lifting latches on the same tick make one
     * attempt, not two.
     */
    public static boolean begin(ServerLevel l,String key){
        var r=room(l,key);long now=l.getGameTime();if(r.getLong("MovingUntil")>now)return false;
        r.putLong("MovingUntil",now+MOTION);room(l,key,r);return true;
    }
    /**
     * The first committed crossing locks this attempt's pins: every pinned reader's pick is their answer, the reader at the latch answers with
     * the latch if they placed no pin, and every pin comes out for the next attempt. Returns who answered, and with which choice.
     */
    public static Map<UUID,Integer> commit(ServerLevel l,String key,String riddle,ServerPlayer puller,int choice,int right){
        var answered=new LinkedHashMap<UUID,Integer>(pins(l,key));
        answered.putIfAbsent(puller.getUUID(),choice);
        for(var e:answered.entrySet()){
            answer(l,e.getKey(),riddle,e.getValue()==right);
            var rec=record(l,e.getKey(),riddle);if(!e.getKey().equals(puller.getUUID())||pinOf(l,key,puller.getUUID())>=0){rec.putInt("Pinned",e.getValue()+1);record(l,e.getKey(),riddle,rec);}
        }
        // The hands on the latch also did the work: a right latch is a solve for its puller even if their pin said otherwise.
        if(choice==right)answer(l,puller.getUUID(),riddle,true);
        var r=room(l,key);r.remove("Pins");room(l,key,r);
        return answered;
    }

    // ------------------------------------------------------------------ what she will remember in the dream
    /** Whether a reader has resolved this riddle: answered it, chosen to walk away from it, or forced it. */
    public static boolean resolved(CompoundTag rec){return rec.contains("First")||rec.getInt("Walked")>0||rec.getBoolean("Forced");}
    public static int resolvedCount(ServerLevel l,UUID reader){int n=0;for(var r:RIDDLES)if(resolved(record(l,reader,r)))n++;return n;}
    /** Correct first answers over distinct riddles deliberately answered; -1 with nothing answered. */
    public static double accuracy(ServerLevel l,UUID reader){
        int answered=0,right=0;for(var r:RIDDLES){var rec=record(l,reader,r);if(rec.contains("First")){answered++;if(rec.getInt("First")==1)right++;}}
        return answered==0?-1:(double)right/answered;
    }
    public static int answered(ServerLevel l,UUID reader){int n=0;for(var r:RIDDLES)if(record(l,reader,r).contains("First"))n++;return n;}
    public enum Conduct{PATIENT,DISMISSIVE,HOSTILE}
    /** "Deliberate provocation takes precedence. Otherwise, two deliberate walkaways set the dismissive version. A reader who stayed to engage gets the patient version even if their answers were wrong." */
    public static Conduct conduct(ServerLevel l,UUID reader){
        int walked=0;for(var r:RIDDLES){var rec=record(l,reader,r);if(rec.getBoolean("Forced"))return Conduct.HOSTILE;walked+=rec.getInt("Walked");}
        return walked>=2?Conduct.DISMISSIVE:Conduct.PATIENT;
    }
    /** The riddle the reader most recently solved themselves (a successful retry included), for the stone's carving; null if none. */
    public static @Nullable String latestSolve(ServerLevel l,UUID reader){
        String best=null;long at=Long.MIN_VALUE;
        for(var r:RIDDLES){var rec=record(l,reader,r);if(rec.getBoolean("Solved")&&rec.getLong("SolvedAt")>=at){at=rec.getLong("SolvedAt");best=r;}}
        return best;
    }

    // ------------------------------------------------------------------ her voice
    /**
     * 0.4.341 (the owner: "Ensure the mouth moves when she speaks too"): she is speaking to this reader for as long as the line takes to
     * say, and wherever they can see her face her jaw works for that long.
     */
    public static void speaks(ServerPlayer p,String line){
        io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new io.github.knaitoe.theoldesthouse.network.SphinxVoicePayload(speaking(line)));
    }
    /** How long a line takes her to say, in ticks: a little slower than a person reads it. */
    public static int speaking(String line){return Math.max(20,Math.min(140,Math.round(line.length()*1.4F)+8));}

    // ------------------------------------------------------------------ small shared hands
    /** The socket beside a choice shows how many pins stand in it (none, one, more). */
    public static void socket(ServerLevel l,BlockPos at,Direction facing,int pins){
        var s=l.getBlockState(at);if(!SphinxBlock.is(s,SphinxBlock.Kind.SURVEY))return;
        l.setBlock(at,SphinxBlock.of(SphinxBlock.Kind.SURVEY,facing,Math.min(2,pins)),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
    }
    /** Setting or taking back a pin: the small sound of iron in a socket, and a line to the reader alone. */
    public static void pinned(ServerLevel l,ServerPlayer p,BlockPos at,int now,String where){
        l.playSound(null,at,PuzzleRegistry.SPHINX_LATCH.get(),SoundSource.BLOCKS,.35F,1.8F);
        PuzzleRooms.say(p,now<0?"You take your survey pin back out. You have not answered.":"You set your survey pin "+where+". Nothing is answered until someone commits.");
    }
}
