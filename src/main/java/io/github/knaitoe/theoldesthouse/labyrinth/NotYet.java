package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;

/**
 * 0.4.663, Not Yet, the first part (docs/NOT_YET_0_4_663.md), from the owner's approval of docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md.
 * The place alone: the passage from the House's door over the hatch, the ward of cots under their lamps with the painted window and the
 * counter, the mending room and its table under the ring of arms, and the long room past the ward, where three people the machine has
 * mended too often lie in their cots with their eyes open (stranger phases 240 to 242). The machine is the Kept's, deeper in.
 * Nothing happens yet: the hatch stays shut, the screens stay dark and the counter stands at nought. The House does not deal it until
 * part 3, when a reader can lie down.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class NotYet {
    private NotYet(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.NOT_YET;
    /** The three in the long room, on stranger phases 240 (Ines), 241 (Bram) and 242 (Odile), in the order of NotYetSite.PEOPLE_COTS. */
    public static final int FIRST=240,INES=0,BRAM=1,ODILE=2,PEOPLE=3;
    private static final String[] SKINS={"notyet_ines","notyet_bram","notyet_odile"};
    public static boolean cast(int phase){return phase>=FIRST&&phase<FIRST+PEOPLE;}
    public static String skin(int phase){return SKINS[phase-FIRST];}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** 0.4.665: dealt now a reader can lie down, from depth twelve, deep in the machine as The Kept is. */
    public static final int DEPTH=12;
    public static boolean dealable(LabyrinthData d,UUID reader){return d.returnDepth(reader)>=DEPTH;}

    /** A reader in: the three are put in their cots if they are not there, and the ward hums once round them. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();ensure(l,b);
        own.putInt("Visits",own.getInt("Visits")+1);
        l.playSound(null,b.offset(0,1,-20),NotYetRegistry.HUM.get(),SoundSource.AMBIENT,.6F,1F);
        NotYetMending.state(p,own);
    }

    // ------------------------------------------------------------------------------------------------ 0.4.664: each reader
    /** The counter's three kinds of window, by the House's read of the reader's age: turning drums, falling cards, lit bars. */
    public static final int DRUM=0,FLAP=1,SEGMENT=2;
    public static int counterKind(LabyrinthData d,UUID id){
        return switch(HouseRead.value(d,id,HouseRead.Trait.AGE,false)){case "DIGITAL"->SEGMENT;case "TRANSITIONAL"->FLAP;default->DRUM;};
    }
    /** Whether this reader let the Kept's four go, as the machine there saw it. */
    static boolean tookFour(ServerPlayer p){return (Kept.own(p).getInt("Released")&15)==15;}
    /** What the machine has said to this reader, the last twelve lines, for the account and for the House's own checks. */
    static void told(CompoundTag own,String line){
        var list=own.getList("Told",net.minecraft.nbt.Tag.TAG_STRING);list.add(net.minecraft.nbt.StringTag.valueOf(line));while(list.size()>12)list.remove(0);own.put("Told",list);
    }
    public static List<String> told(CompoundTag own){var list=own.getList("Told",net.minecraft.nbt.Tag.TAG_STRING);var out=new ArrayList<String>();for(int i=0;i<list.size();i++)out.add(list.getString(i));return out;}
    /**
     * Every five ticks for each reader in the ward. The machine speaks once a visit as they come in, and the first time they are in the
     * ward, once more, by what the House has read of home; its screens wake to them as it speaks. The rest is the mending's.
     */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var r=p.position().subtract(b.getX(),b.getY(),b.getZ());int visit=own.getInt("Visits");
        if(own.getInt("SpokeVisit")!=visit&&r.z<-1){
            own.putInt("SpokeVisit",visit);
            var line=NotYetTexts.arrival(tookFour(p),visit,NotYetMending.cot(own)!=null);
            Kept.machine(p,line);told(own,line);NotYetMending.wakeScreens(p,b);
        }else if(!own.getBoolean("HomeSaid")&&own.getInt("SpokeVisit")==visit&&NotYetSite.inWard(r)&&!NotYetMending.held(p)){
            own.putBoolean("HomeSaid",true);
            var line=NotYetTexts.home(HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.HOME,true));
            if(!line.isEmpty()){Kept.machine(p,line);told(own,line);}
        }else if(NotYetTrial.ready(own)&&!own.getBoolean("StayedDown")&&own.getInt("InvitedVisit")!=visit&&NotYetSite.inWard(r)&&!NotYetMending.held(p)&&!NotYetTrial.lying(p.getUUID())){
            // 0.4.665: mended, and told the rule: the machine asks them to lie down, once a visit, and their lamp comes on over their cot.
            own.putInt("InvitedVisit",visit);Kept.machine(p,NotYetTexts.INVITE);told(own,NotYetTexts.INVITE);
            var mine=NotYetMending.cot(own);if(mine!=null)NotYetTrial.lamp(p.serverLevel(),b,mine,true);
        }
        NotYetMending.reader(p,b,own);
    }

    /** The one lying in this cot of the long room, if their body is there. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){
        var at=b.offset(NotYetSite.PEOPLE_COTS[who]);
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(at).inflate(2),x->is(x)&&x.phase()==FIRST+who))if(a.isAlive())return a;
        return null;
    }
    /** Each of the three is made lying in their cot if they are not there; nothing is made while the long room's entities are unloaded. */
    public static void ensure(ServerLevel l,BlockPos b){
        for(int who=0;who<PEOPLE;who++){
            var at=b.offset(NotYetSite.PEOPLE_COTS[who]);
            if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))continue;
            var a=find(l,b,who);
            if(a==null){
                a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
                a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+who);
                lie(a,at);
                if(!l.addFreshEntity(a))continue;
            }
            lie(a,at);
        }
    }
    /**
     * On their back in the cot, the head on the pillow at the wall. The body is laid on the mattress by the renderer (LiteraryRenderers.
     * lieOnNotYet); here it sleeps at the head's cell, nine pixels up, so a hand finds it where it lies.
     */
    static void lie(LiteraryActor a,BlockPos head){
        float yaw=NotYetSite.cotFacing(head).toYRot();
        a.moveTo(head.getX()+.5,head.getY()+9/16.0,head.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
        a.setSleepingPos(head);a.setPose(Pose.SLEEPING);a.gesture(0);a.setDeltaMovement(Vec3.ZERO);
    }
    /**
     * They lie in cots, which are beds to the game, and the game's rule that gets up a sleeper whose bed has gone is not theirs: nothing
     * gets them up. They cannot move any more.
     */
    @SubscribeEvent public static void keepLying(CanContinueSleepingEvent e){
        if(e.getEntity() instanceof LiteraryActor a&&is(a))e.setContinueSleeping(true);
        // 0.4.664: a reader laid on the table or on a cot lies there until the machine (or, from part 3, the reader) says otherwise.
        else if(e.getEntity() instanceof net.minecraft.world.entity.player.Player p&&onMachineBed(p))e.setContinueSleeping(true);
    }
    /** Whether a body is lying on one of the ward's cots or on the mending table. */
    public static boolean onMachineBed(net.minecraft.world.entity.LivingEntity e){
        var at=e.getSleepingPos().orElse(null);if(at==null)return false;var s=e.level().getBlockState(at);
        return NotYetBlock.is(s,NotYetBlock.Kind.COT)||NotYetBlock.is(s,NotYetBlock.Kind.TABLE);
    }

    /**
     * A hand on one of the three. To a reader the machine has not yet mended they say nothing, and the reader sees how they lie. To one it
     * has, whichever of them is spoken to first tells the only rule they know; after that, a little more, in turn.
     */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        int who=a.phase()-FIRST;
        if(own.getInt("Mended")==0){cue(p,switch(who){case INES->NotYetTexts.INES;case BRAM->NotYetTexts.BRAM;default->NotYetTexts.ODILE;});return;}
        String line;
        if(!own.getBoolean("Rule")){line=NotYetTexts.rule(who,p);own.putBoolean("Rule",true);own.putInt("RuleFrom",who);}
        else{int heard=own.getInt("Heard"+who);line=NotYetTexts.after(who,heard);own.putInt("Heard"+who,heard+1);}
        a.say(NotYetTexts.NAMES[who],line);a.speaks(line);
    }

    /** A hand on something of the ward's. True if it was theirs to answer. The chart is the first paper: the House's own reading answers it. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        if(!NotYetBlock.is(st))return false;
        int stage=st.getValue(NotYetBlock.STAGE);
        // 0.4.665: a cot is to lie down on, for a reader the machine has asked.
        if((NotYetBlock.is(st,NotYetBlock.Kind.COT)||NotYetBlock.is(st,NotYetBlock.Kind.COT_FOOT))&&NotYetTrial.lie(p,b,rel,own))return true;
        switch(st.getValue(NotYetBlock.KIND)){
            case CHART->{return false;}
            case COT->cue(p,NotYetTexts.COT);
            case COT_FOOT->cue(p,NotYetTexts.COT_FOOT);
            case CURTAIN->cue(p,NotYetTexts.CURTAIN);
            case LAMP->cue(p,stage==1?NotYetTexts.LAMP_ON:NotYetTexts.LAMP);
            case SCREEN->cue(p,NotYetTexts.SCREEN);
            case COUNTER->cue(p,NotYetTexts.COUNTER);
            case TABLE,TABLE_FOOT->cue(p,NotYetTexts.TABLE);
            case ARM->cue(p,NotYetTexts.ARM);
            case HATCH->cue(p,NotYetTexts.HATCH);
            case VENT->cue(p,NotYetTexts.VENT);
            case WINDOW->cue(p,NotYetTexts.WINDOW);
            case DOOR->cue(p,stage>=2?NotYetTexts.DOOR_OPEN:NotYetTexts.DOOR);
        }
        return true;
    }
}
