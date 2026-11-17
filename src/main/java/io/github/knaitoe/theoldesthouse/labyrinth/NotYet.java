package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
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
    /** Not dealt until part 3. */
    public static boolean dealable(LabyrinthData d,UUID reader){return false;}

    /** A reader in: the three are put in their cots if they are not there, and the ward hums once round them. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();ensure(l,b);
        own.putInt("Visits",own.getInt("Visits")+1);
        l.playSound(null,b.offset(0,1,-20),NotYetRegistry.HUM.get(),SoundSource.AMBIENT,.6F,1F);
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
    }

    /** A hand on one of the three. They do not speak yet (part 2); a reader sees how they lie. */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        cue(p,switch(a.phase()-FIRST){case INES->NotYetTexts.INES;case BRAM->NotYetTexts.BRAM;default->NotYetTexts.ODILE;});
    }

    /** A hand on something of the ward's. True if it was theirs to answer. The chart is the first paper: the House's own reading answers it. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        if(!NotYetBlock.is(st))return false;
        int stage=st.getValue(NotYetBlock.STAGE);
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
