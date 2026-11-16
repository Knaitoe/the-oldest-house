package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.609, The Ones Already Inside, its ninth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, N8: "Vera's offer and the truth under
 * challenge").
 *
 * <p>In Act II a hand on Vera, wherever she is, gets her offer once: she can mend Dan, and needs help keeping the other one quiet while
 * she does it. Nobody can back her until they have heard what it costs: the reply that asks after the other one is always there, and a
 * reader who has seen the rule (either cut, or the pin) or heard Daniel's own account can put that to her instead. Either way she says
 * the rest of the script: he won't come through it; he has been cared for; or Dan can be sent back through the frame and the other walks
 * out, which would be a waste. Only then does the reader answer her: help her, tell her to send Dan back, refuse, or say nothing.
 *
 * <p>What each reader heard and answered is their own record; nothing here moves anybody yet (N11 and N12 act on it). If Rae brought
 * her the key, she has it in her hand.
 */
public final class LodgingVera {
    private LodgingVera(){}
    /** Each reader's own: offered, told the cost, and their answer. */
    public static final String OFFERED="VeraOffered0609",TRUTH="VeraTruth0609",ANSWER="VeraAnswer0609";
    /** A reader's answer: none yet, help her, send Dan back, no. */
    public static final int NONE=0,HELP=1,SEND_BACK=2,REFUSE=3;
    /** 0.4.610, each reader's own: put the cellar to her. */
    public static final String CELLAR="VeraCellar0610";

    /** A hand on Vera in Act II. True if it was answered here. */
    static boolean talk(ServerPlayer p,BlockPos b,LiteraryActor vera,CompoundTag own){
        if(own.getInt(ANSWER)!=NONE)return false;
        Talk.begin(p,LodgingPeople.TALK+LodgingPeople.VERA,"Vera");
        if(!own.getBoolean(OFFERED)){
            own.putBoolean(OFFERED,true);LodgingHouse.save(p,own);
            for(var line:LodgingTexts.VERA_OFFER)LodgingPeople.says(p,vera,"Vera",line);
            LodgingSelf.offer(p,vera,own);
        }else LodgingHouse.cue(p,LodgingTexts.VERA_WAITS);
        if(LodgingKey.VERA.equals(LodgingKey.where(p.server,b)))LodgingHouse.cue(p,LodgingTexts.VERA_HAS_KEY);
        asks(p);
        return true;
    }
    private static void asks(ServerPlayer p){
        var own=LodgingHouse.own(p);var replies=new ArrayList<Talk.Reply>();
        if(!own.getBoolean(TRUTH)){
            replies.add(Talk.reply(LodgingTexts.R_OTHER_ONE,LodgingVera::truth));
            if(LodgingPeople.sawTheRule(own))replies.add(Talk.reply(LodgingTexts.R_IT_CUT_HIM,LodgingVera::truth));
            if(own.getBoolean(LodgingPeople.DANIEL_TOLD))replies.add(Talk.reply(LodgingTexts.R_HE_TOLD_ME,LodgingVera::truth));
        }else{
            // The cost is known before anybody can help.
            replies.add(Talk.reply(LodgingTexts.R_HELP_VERA,r->answered(r,HELP)));
            replies.add(Talk.reply(LodgingTexts.R_SEND_BACK,r->answered(r,SEND_BACK)));
            replies.add(Talk.reply(LodgingTexts.R_REFUSE_VERA,r->answered(r,REFUSE)));
        }
        // 0.4.610: a reader who saw the dead post's body come apart can put it to her.
        if(own.getBoolean(LodgingCellar.KNOWS_THIRD)&&!own.getBoolean(CELLAR))replies.add(Talk.reply(LodgingTexts.R_CELLAR,LodgingVera::cellar));
        replies.add(Talk.act(LodgingTexts.R_NOTHING,Talk::close));
        Talk.ask(p,replies);
    }
    private static void truth(ServerPlayer p){
        var vera=LodgingPeople.near(p,LodgingPeople.VERA);if(vera==null){Talk.close(p);return;}
        var own=LodgingHouse.own(p);own.putBoolean(TRUTH,true);LodgingHouse.save(p,own);
        for(var line:LodgingTexts.VERA_TRUTH)LodgingPeople.says(p,vera,"Vera",line);
        asks(p);
    }
    private static void cellar(ServerPlayer p){
        var vera=LodgingPeople.near(p,LodgingPeople.VERA);if(vera==null){Talk.close(p);return;}
        var own=LodgingHouse.own(p);own.putBoolean(CELLAR,true);LodgingHouse.save(p,own);
        LodgingPeople.says(p,vera,"Vera",LodgingTexts.VERA_CELLAR);
        asks(p);
    }
    private static void answered(ServerPlayer p,int answer){
        var vera=LodgingPeople.near(p,LodgingPeople.VERA);if(vera==null){Talk.close(p);return;}
        var own=LodgingHouse.own(p);own.putInt(ANSWER,answer);LodgingHouse.save(p,own);
        LodgingPeople.says(p,vera,"Vera",switch(answer){case HELP->LodgingTexts.VERA_GOOD;case SEND_BACK->LodgingTexts.VERA_WASTE;default->LodgingTexts.VERA_KEEP_OUT;});
        Talk.end(p);
    }
}
