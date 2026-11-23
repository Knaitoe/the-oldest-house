package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * 0.4.757, the cold room, part 4 (docs/COLD_ROOM_0_4_757.md): the offer and the end.
 *
 * <ul>
 * <li><b>The offer.</b> From the ninth page, she offers each reader in the bedroom something real, made from the House's records of them:
 * the last companion it saw them lose, otherwise their last death undone, otherwise the way home. It comes up on the conversation panel.
 * Answering her stops that reader's reading for the rest of the night; she laughs, they are at the foot of the stairs by the door, and
 * nothing is given back. Keeping on reading is the other reply.
 * <li><b>The last page.</b> It is blank. Every reader who comes within reach of the bed, and has not answered her, is offered the line the
 * book never prints: "Take me instead." The first to say it is taken: the view goes, they are at the window, then at the foot of the
 * steps under the lamp, alive, and after a while back in the kitchen. The night is over for everyone in the house.
 * <li><b>Dawn.</b> If nobody says it in half a minute, the morning comes: she sleeps, the cold goes, and her mother comes up to the landing.
 * </ul>
 *
 * The outcome is each reader's own: the one taken took her place; the others were in the room, or saw the morning. A reader who answered
 * her, or who never heard a page read, is not finished.
 */
public final class ColdEnd {
    private ColdEnd(){}
    /** The page she makes her offer from; ticks after the blank page before the morning; the taker's steps (window, steps, kitchen). */
    public static final int OFFER_PAGE=9,DAWN=600,AT_WINDOW=8,FADE_OUT=70,AT_FOOT=80,FADE_BACK=280,IN_KITCHEN=290;
    public static final String OFFER="cold_room_offer",LAST="cold_room_last",FROM_BED="From the bed";
    /** Where an answered reader is put: the hall, at the foot of the stairs, by the door. And where the taker comes back to. */
    public static final BlockPos BY_THE_DOOR=new BlockPos(0,0,-2),WINDOW=new BlockPos(0,5,-20),KITCHEN=new BlockPos(-6,0,-4),LANDING=new BlockPos(0,5,-11);

    // ------------------------------------------------------------------------------------------------ the offer
    /** What she has to offer this reader, from the House's records of them: a companion back, a death undone, or the way home. */
    public record Offer(int kind,String text,String what){}
    public static final int COMPANION=1,DEATH=2,HOME=3;
    public static Offer offer(ServerPlayer p){
        var record=HotelVignette.personal(LabyrinthData.get(p.server),p.getUUID());
        var pets=record.getList("LostPets",Tag.TAG_COMPOUND);
        if(!pets.isEmpty()){String pet=pets.getCompound(pets.size()-1).getString("Name");if(!pet.isBlank())return new Offer(COMPANION,ColdTexts.offerPet(pet),pet);}
        var deaths=record.getList("Deaths",Tag.TAG_COMPOUND);
        if(!deaths.isEmpty()){var d=deaths.getCompound(deaths.size()-1);if(!d.getString("Message").isBlank())return new Offer(DEATH,ColdTexts.offerDeath(d.getString("Message"),d.getString("Place")),d.getString("Message"));}
        return new Offer(HOME,ColdTexts.OFFER_HOME,"");
    }
    /** The offer put to one reader in the bedroom. */
    static void put(ServerPlayer p,BlockPos b,ColdRite.Rite r){
        r.offered.add(p.getUUID());var o=offer(p);
        var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),ColdRoom.PLACE);
        own.putInt("Offered",o.kind());if(!o.what().isEmpty())own.putString("OfferedWhat",o.what());LiteraryVignettes.save(data,p.getUUID(),ColdRoom.PLACE,own);
        LiteraryVignettes.heard(p,ColdRegistry.VOICE.get(),b.offset(ColdSite.BED_HEAD),1F,.75F);
        Talk.begin(p,OFFER,FROM_BED);Talk.line(p,FROM_BED,o.text());
        Talk.ask(p,Talk.reply(ColdTexts.ANSWER,q->answer(q,b)),Talk.act(ColdTexts.KEEP_READING,q->keep(q,b)));
    }
    /** She is answered: the reading stops for this reader for the rest of the night, and they are down the stairs by the door. */
    static void answer(ServerPlayer p,BlockPos b){
        var r=ColdNight.rite(b);if(r==null||r.over()||!r.answered.add(p.getUUID()))return;
        mark(p,o->o.putInt("Answered",o.getInt("Answered")+1));
        Talk.line(p,FROM_BED,ColdTexts.LAUGH);Talk.end(p);
        LiteraryVignettes.heard(p,ColdRegistry.VOICE.get(),b.offset(ColdSite.BED_HEAD),1F,1.3F);
        HousePackets.send(p,new HouseFadePayload(6,24,20));
        put(p,b,BY_THE_DOOR,net.minecraft.core.Direction.SOUTH.toYRot());
        ColdRoom.cue(p,ColdTexts.SENT_DOWN);
    }
    static void keep(ServerPlayer p,BlockPos b){mark(p,o->o.putBoolean("KeptReading",true));Talk.close(p);}

    // ------------------------------------------------------------------------------------------------ the last page
    /** Within reach of the bed, standing or kneeling: close enough to say it to her. */
    static boolean overHer(ServerPlayer p,BlockPos b){
        if(p.isSpectator()||!ColdSite.inBedroom(ColdNight.rel(p,b)))return false;
        var bed=ColdRite.bedAt(b);return Math.abs(p.getX()-bed.x)<2.6&&Math.abs(p.getZ()-bed.z)<2.2;
    }
    static void ask(ServerPlayer p,BlockPos b,ColdRite.Rite r){
        r.asked.add(p.getUUID());
        Talk.begin(p,LAST,FROM_BED);Talk.line(p,"",ColdTexts.LAST_PAGE_OPEN);
        Talk.ask(p,Talk.reply(ColdTexts.TAKE_ME,q->take(q,b)),Talk.act(ColdTexts.SAY_NOTHING,q->{mark(q,o->o.putBoolean("SaidNothing",true));Talk.close(q);}));
    }
    /** Every second of the rite, after the pages: the offer from the ninth page, the last page's line, and the morning. */
    static void tick(ServerLevel l,BlockPos b,ColdNight.Night n,List<ServerPlayer> readers){
        var r=n.rite;if(r.over())return;
        for(var p:readers){if(ColdRite.reads(n,p,b)||ColdRite.holds(p,b))r.company.put(p.getUUID(),p.getName().getString());}
        if(r.page>=OFFER_PAGE&&r.page<ColdRite.PAGES)
            for(var p:readers)if(ColdSite.inBedroom(ColdNight.rel(p,b))&&!r.offered.contains(p.getUUID())&&!r.answered.contains(p.getUUID()))put(p,b,r);
        if(r.page<ColdRite.PAGES)return;
        if(r.lastAt<0)r.lastAt=n.t;
        for(var p:readers)if(overHer(p,b)&&!r.answered.contains(p.getUUID())&&!r.asked.contains(p.getUUID()))ask(p,b,r);
        if(n.t-r.lastAt>=DAWN)dawn(l,b,n);
    }
    /** For the tests: the morning, now. */
    public static void dawnNow(ServerLevel l,BlockPos b){var n=ColdNight.night(b);if(n!=null&&!n.rite.over())dawn(l,b,n);}

    /** "Take me instead": the first to say it is taken; the night is over for everyone in the house. */
    static void take(ServerPlayer p,BlockPos b){
        var n=ColdNight.night(b);if(n==null)return;var r=n.rite;
        if(r.over()||r.answered.contains(p.getUUID())){Talk.close(p);return;}
        var l=p.serverLevel();
        r.end=TAKEN;r.taken=p.getUUID();r.takenAt=l.getGameTime();r.cold=0;
        Talk.close(p);
        HousePackets.send(p,new HouseFadePayload(4,30,20));
        ColdRoom.cue(p,ColdTexts.WHITE);
        l.playSound(null,b.offset(ColdSite.BED_HEAD),ColdRegistry.BED.get(),SoundSource.BLOCKS,1F,.7F);
        ColdNight.bed(l,b,false);
        var others=ColdNight.who(l,b).get(0);
        mark(p,o->{o.putBoolean("TookHerPlace",true);company(o,r,p.getUUID());LiteraryVignettes.ready(p,ColdRoom.PLACE,o,"took_her_place");});
        for(var q:others){
            if(q==p)continue;
            if(Talk.waiting(q,LAST)||Talk.waiting(q,OFFER))Talk.close(q);
            q.displayClientMessage(Component.literal(ColdTexts.takenOthers(p.getName().getString())).withStyle(ChatFormatting.GRAY),false);
            finish(q,r,"were_in_the_room",o->o.putString("Taker",p.getName().getString()));
        }
    }
    static final String TAKEN="taken",MORNING="dawn";
    /** Nobody said it: the morning. She sleeps, the cold goes, and her mother comes up to the landing. */
    static void dawn(ServerLevel l,BlockPos b,ColdNight.Night n){
        var r=n.rite;r.end=MORNING;r.cold=0;
        ColdNight.bed(l,b,false);
        var mother=ColdRoom.find(l,b,ColdRoom.MOTHER);if(mother!=null)ColdRoom.place(mother,b,ColdRoom.MOTHER);
        l.playSound(null,b.offset(LANDING),SoundEvents.WOOD_STEP,SoundSource.BLOCKS,.8F,.9F);
        for(var q:ColdNight.who(l,b).get(0)){
            if(Talk.waiting(q,LAST)||Talk.waiting(q,OFFER))Talk.close(q);
            q.displayClientMessage(Component.literal(ColdTexts.DAWN).withStyle(ChatFormatting.GRAY),false);
            finish(q,r,"saw_the_morning",o->o.putBoolean("SawMorning",true));
        }
    }
    /** A reader at the end: finished, if they did not answer her and heard at least one page read. */
    static void finish(ServerPlayer q,ColdRite.Rite r,String outcome,java.util.function.Consumer<CompoundTag> also){
        if(r.answered.contains(q.getUUID()))return;
        mark(q,o->{also.accept(o);company(o,r,q.getUUID());if(o.getInt("Pages")>0)LiteraryVignettes.ready(q,ColdRoom.PLACE,o,outcome);});
    }
    /** Who else read or held her this night, for the account. */
    static void company(CompoundTag own,ColdRite.Rite r,UUID self){
        var names=new ArrayList<String>();r.company.forEach((u,name)->{if(!u.equals(self))names.add(name);});
        own.putString("Company",String.join("\n",names));
    }

    /** The one taken, each tick until they are back indoors: the window, then the steps outside, then the kitchen. */
    static void taken(ServerLevel l,BlockPos b,ColdNight.Night n){
        var r=n.rite;if(r.taken==null||r.home)return;
        long k=l.getGameTime()-r.takenAt;
        var p=l.getServer().getPlayerList().getPlayer(r.taken);
        if(p==null||p.level()!=l){if(k>IN_KITCHEN)r.home=true;return;}
        if(k==AT_WINDOW){put(p,b,WINDOW,net.minecraft.core.Direction.NORTH.toYRot());ColdRoom.cue(p,ColdTexts.AT_WINDOW);}
        else if(k==FADE_OUT)HousePackets.send(p,new HouseFadePayload(10,30,30));
        else if(k==AT_FOOT){put(p,b,ColdSite.FOOT,net.minecraft.core.Direction.SOUTH.toYRot());ColdRoom.cue(p,ColdTexts.AT_FOOT);}
        else if(k==FADE_BACK)HousePackets.send(p,new HouseFadePayload(10,20,30));
        else if(k>=IN_KITCHEN){put(p,b,KITCHEN,net.minecraft.core.Direction.WEST.toYRot());ColdRoom.cue(p,ColdTexts.IN_KITCHEN);r.home=true;}
    }

    static void put(ServerPlayer p,BlockPos b,BlockPos rel,float yaw){
        var at=b.offset(rel);p.teleportTo(p.serverLevel(),at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);p.fallDistance=0;
    }
    static void mark(ServerPlayer p,java.util.function.Consumer<CompoundTag> change){
        var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),ColdRoom.PLACE);change.accept(own);LiteraryVignettes.save(data,p.getUUID(),ColdRoom.PLACE,own);
    }
}
