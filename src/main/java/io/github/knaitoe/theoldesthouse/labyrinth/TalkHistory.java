package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.TalkHistoryPayload;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.266, what each reader has heard (the owner's second quality-of-life note: "Let players revisit conversations they actually heard,
 * especially their own promises and answers", and then: "i'm okay with the conversation history being integrated into the player's
 * journal"). Every exchange the conversation panel showed a reader is kept, line for line with their own replies, and survives logging
 * out and the server stopping. The panel itself still holds only its last 48 lines; this keeps up to {@link #LINES} of each exchange and
 * the last {@link #CONVERSATIONS} exchanges. Only what reached the panel is kept: a line a scene set down and never showed is not.
 * The journal shows it in its own section, behind the ribbon ("What was said"); it is printed, not handwritten, and takes none of the
 * reader's pages.
 */
public final class TalkHistory {
    private TalkHistory(){}
    public static final String STATE="talk_history_0266";
    public static final int CONVERSATIONS=48,LINES=120;

    static CompoundTag own(MinecraftServer s,UUID id){return LabyrinthData.get(s).stateEntry(STATE,id.toString());}
    static void own(MinecraftServer s,UUID id,CompoundTag t){LabyrinthData.get(s).setStateEntry(STATE,id.toString(),t);}

    /** A new exchange's number for this reader, never reused. */
    static int next(ServerPlayer p){var t=own(p.server,p.getUUID());int n=t.getInt("Next")+1;t.putInt("Next",n);own(p.server,p.getUUID(),t);return n;}

    /** Where the reader is, as the House would put it, or empty. */
    static String where(ServerPlayer p){
        String id="";var place=LiteraryVignettes.current(p);if(place!=null)id=place.id();
        if(id.isEmpty()&&HouseDimensions.isHouseDimension(p.level().dimension())){var origin=HouseSavedData.get(p.server).houseOrigin();if(origin!=null){var at=LabyrinthPlaces.placeAt(origin,p.blockPosition());if(at!=null)id=at.id();}}
        if(place==GhostsSet.PLACE){var state=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),place);return "the Barrett house, filming day "+Math.max(1,Math.min(7,state.getInt("FilmDay")));}
        return StaircaseProse.named(id)?StaircaseProse.place(id):"";
    }
    /** The day it is, counted the way the reader's own pages count it. */
    static long day(ServerPlayer p){return p.server.overworld().getDayTime()/24000L+1;}

    /** The exchange as the panel has just shown it, kept under its number (replacing what was kept of it before). */
    static void keep(ServerPlayer p,int number,String who,String where,long day,List<String[]> lines){
        if(number<=0||p.isSpectator())return;
        var t=own(p.server,p.getUUID());var all=t.getList("Talks",Tag.TAG_COMPOUND);
        CompoundTag talk=null;int at=-1;
        for(int i=0;i<all.size();i++)if(all.getCompound(i).getInt("N")==number){talk=all.getCompound(i);at=i;break;}
        if(talk==null){talk=new CompoundTag();talk.putInt("N",number);talk.putString("Who",who);talk.putString("Where",where);talk.putLong("Day",day);}
        var kept=new ListTag();
        for(int i=Math.max(0,lines.size()-LINES);i<lines.size();i++){var l=new CompoundTag();l.putString("S",lines.get(i)[0]);l.putString("T",lines.get(i)[1]);if(lines.get(i).length>2)l.putBoolean("R",true);kept.add(l);}
        talk.put("Lines",kept);
        if(at>=0)all.set(at,talk);else all.add(talk);
        while(all.size()>CONVERSATIONS)all.remove(0);
        t.put("Talks",all);own(p.server,p.getUUID(),t);
    }

    /** A scene's actually delivered speech, grouped across its saved visit; never invents replies for the reader. */
    public static void heard(ServerPlayer p,String scene,String who,String where,String text){heard(p,scene,"The Barretts",who,where,text);}
    /** How far a line said aloud carries (as client.SpeechCaptions shows it). */
    public static final double HEARD=24;
    /**
     * 0.4.429 (owner: "spoken npc dialogue doesn't track to journal"): a line an actor has just said aloud, kept for every reader near
     * enough to hear it who can see the speaker (an actor kept private to another reader's scene is never theirs). One record per place
     * and day; its heading names who spoke in it. Scenes that keep their own record (the Barretts' filming days) are left to it, as is a
     * line the reader's open conversation panel already holds.
     */
    public static void overheard(LiteraryActor a,@javax.annotation.Nullable String speaker,String text){
        if(!(a.level() instanceof net.minecraft.server.level.ServerLevel l)||text.isBlank()||a.scene().equals(GhostsSet.PLACE.id()))return;
        String who=speaker==null||speaker.isBlank()?"Someone":speaker;
        for(var p:l.players()){
            if(p.isSpectator()||!p.isAlive()||p.distanceToSqr(a)>HEARD*HEARD||!a.broadcastToPlayer(p))continue;
            // Run 1498: a line the reader's own conversation panel has just set down is kept in that exchange's transcript, not twice.
            if(Talk.holds(p,text))continue;
            String where=where(p);heard(p,"said/"+where+"/"+day(p),who,who,where,text);
        }
    }
    /** As heard(p,scene,who,where,text), the record headed by title when it is new; a new speaker's name joins the heading (up to three). */
    static void heard(ServerPlayer p,String scene,String title,String who,String where,String text){
        if(p.isSpectator()||text.isBlank())return;
        var t=own(p.server,p.getUUID());var all=t.getList("Talks",Tag.TAG_COMPOUND);CompoundTag talk=null;
        for(int i=0;i<all.size();i++)if(all.getCompound(i).getString("Scene").equals(scene)){talk=all.getCompound(i);break;}
        if(talk==null){int number=t.getInt("Next")+1;t.putInt("Next",number);talk=new CompoundTag();talk.putInt("N",number);talk.putString("Scene",scene);talk.putString("Who",title);talk.putString("Where",where);talk.putLong("Day",day(p));all.add(talk);}
        else if(scene.startsWith("said/")&&!who.equals("Someone")){var names=new java.util.ArrayList<>(List.of(talk.getString("Who").split(", ")));names.remove("Someone");if(!names.contains(who)&&names.size()<3){names.add(who);talk.putString("Who",String.join(", ",names));}}
        var lines=talk.getList("Lines",Tag.TAG_COMPOUND);
        if(!lines.isEmpty()){var last=lines.getCompound(lines.size()-1);if(last.getString("S").equals(who)&&last.getString("T").equals(text))return;}
        var line=new CompoundTag();line.putString("S",who);line.putString("T",text);lines.add(line);while(lines.size()>LINES)lines.remove(0);talk.put("Lines",lines);
        while(all.size()>CONVERSATIONS)all.remove(0);t.put("Talks",all);own(p.server,p.getUUID(),t);
    }

    /** Every exchange this reader has kept, oldest first (each {@code N}, {@code Who}, {@code Where}, {@code Day}, {@code Lines}; a line the reader chose carries {@code R}, 0.4.275). */
    public static ListTag conversations(MinecraftServer s,UUID id){return own(s,id).getList("Talks",Tag.TAG_COMPOUND);}
    /** Sends the reader their own history, for the journal's section. */
    public static void send(ServerPlayer p){var t=new CompoundTag();t.put("Talks",conversations(p.server,p.getUUID()));HousePackets.send(p,new TalkHistoryPayload(t));}
}
