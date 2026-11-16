package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.SecondPerson.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.SecondPersonSite.*;

/**
 * 0.4.633, Second Person, spring: the let (docs/SECOND_PERSON_0_4_633.md; the proposal's section 4 and ledger rows 19 to 23). Between
 * the winter and the last morning. "While you were in the cupboard: the tenancy came up."
 * <ul>
 * <li><b>The agent</b> comes up the stairs with a clipboard and is walked round the flat. A reader who stands close enough for the air
 * to go cold round them makes them hurry, and the rent stays where it was (ledger 19).</li>
 * <li><b>Deciding.</b> The resident sits at the kitchen table with the letter, thinking aloud by the House's read of home. A reader can
 * lay out what they would need to go (their coat by the door) or to stay (their things put where they live); either weighs in, never
 * decides (ledgers 20 and 21). What they decide is read off the year, and kept for the last morning.</li>
 * <li><b>The application.</b> The post brings a form for the flat, "for when the current tenant leaves", in the reader's handwriting
 * with the reader's name. Read, the resident reads it too and asks the room "Is this you?", and a word on the glass is the one they
 * keep (ledgers 22 and 23). Turned face down, they pick it up with the post and never turn it over. Signing it is the next slice.</li>
 * </ul>
 * The season's every step is in the reader's own record, like the rest of the year; nothing here can hurt, and there is no failing.
 */
public final class SecondPersonSpring {
    private SecondPersonSpring(){}
    public static final int S_AGENT=0,S_DECIDE=1,S_POST=2,SPRING_DONE=3;
    /** The agent: how long they stop at each place on the walk round, how close the cold has to be and for how long. */
    static final int STOP=100,COLD_TICKS=40;
    /** How long the resident sits with the letter before deciding, and how long after something is laid out they notice it. */
    static final int DECIDE_WAIT=600,NOTICE=80;
    /** How long the form lies on the mat before they go for the post, and how long their question waits for a word. */
    static final int POST_WAIT=700,ASK_WAIT=600;
    /** Where the agent stops on the walk round: the hall, the front window, the kitchen sink, the bathroom door. */
    static final List<Mark> ROUND=List.of(Mark.stand(1.5,-22.5,NORTH),Mark.stand(-3.5,-14.5,SOUTH),Mark.stand(-2.5,-21.5,NORTH),Mark.stand(1.5,-25.5,WEST));
    static final Vec3 LANDING_AT=at(4.5,UP,-27.5),STAIR_TOP=at(5.5,UP,-24.5);
    public static final BlockPos APPLICATION_AT=MAT;

    static String home(CompoundTag own){return own.getCompound("Snap").getString("Home");}
    /** Where they would go, if they went: with Sam, home, somewhere nobody has the address, or back to whoever wrote. */
    static String going(CompoundTag own){
        if(own.getBoolean("SamStayed"))return "sam";
        return switch(home(own)){case "HAVEN"->"home";case "DANGER"->"safe";case "RUNAWAY"->"write";default->"elsewhere";};
    }

    // ------------------------------------------------------------------------------------------------ marks
    static Mark residentMark(CompoundTag own){
        int st=step(own),sub=sub(own),dt=dt(own);
        return switch(st){
            case S_AGENT->switch(sub){case 0->dt<40?seat(ARMCHAIR,WEST):beside("door");case 1->beside("hall_table");case 2->beside("door");default->seat(RESIDENT_CHAIR,EAST);};
            case S_DECIDE->seat(RESIDENT_CHAIR,EAST);
            case S_POST->switch(sub){case 0->seat(RESIDENT_CHAIR,EAST);case 1,2->beside("door");case 3->beside("hall_table");default->seat(ARMCHAIR,WEST);};
            default->seat(ARMCHAIR,WEST);
        };
    }
    static Mark agentMark(CompoundTag own){
        if(seasonOf(own)!=SPRING||step(own)!=S_AGENT)return Mark.gone();
        int sub=sub(own),dt=dt(own);
        return switch(sub){
            case 0->dt>=50?Mark.stand(LANDING_AT.x,LANDING_AT.z,WEST):Mark.gone();
            case 1->ROUND.get(Math.min(ROUND.size()-1,own.getInt("AgentStop")));
            case 2->Mark.stand(LANDING_AT.x,LANDING_AT.z,WEST);
            default->dt<60?Mark.stand(LANDING_AT.x,LANDING_AT.z,EAST):Mark.gone();
        };
    }
    /** The agent's clipboard: the form in their hand the whole visit. */
    static void props(@Nullable LiteraryActor agent){
        if(agent==null)return;var want=prop(SecondPersonRegistry.FORM_MODEL,"Clipboard");
        if(!ItemStack.isSameItemSameComponents(agent.getMainHandItem(),want))agent.setItemSlot(EquipmentSlot.MAINHAND,want);
    }

    // ------------------------------------------------------------------------------------------------ the season
    static void spring(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a,@Nullable LiteraryActor agent){
        switch(step(own)){
            case S_AGENT->agent(l,f,p,own,a,agent);
            case S_DECIDE->decide(f,p,own,a);
            case S_POST->post(l,f,p,own,a);
            default->{if(dt(own)==60&&f.once.add("spring_done"))cue(p,"The flat goes quiet.");}
        }
    }

    static void agent(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a,@Nullable LiteraryActor agent){
        var b=f.base;int sub=sub(own),dt=dt(own);
        props(agent);cold(l,f,p,own,a,agent);
        if(sub==0){
            if(dt==30){sound(p,DoorSounds.KNOCK.get(),b,FLAT_DOOR,1,1);cue(p,"Somebody knocks at the flat door.");}
            if(dt==60)open(l,b,FLAT_DOOR,true);
            if(dt>80&&arrived(agent,b,agentMark(own))){say(f,agent,SecondPersonSpringTexts.AGENT_HELLO);own.putInt("AgentStop",0);own.remove("AgentAt");own.putInt("StopFrom",0);next(own,1);}
            else if(dt>400&&agent==null){own.putInt("AgentStop",0);own.remove("AgentAt");own.putInt("StopFrom",0);next(own,1);}
            return;
        }
        if(sub==1){
            // Each stop: walk there (or, unseen, be put there), stop a while with a word and a note on the clipboard, then on.
            int stop=own.getInt("AgentStop");var m=ROUND.get(Math.min(ROUND.size()-1,stop));
            if(!own.contains("AgentAt")){if(arrived(agent,b,m)||dt-own.getInt("StopFrom")>STOP*3)own.putInt("AgentAt",dt);else return;}
            int at=dt-own.getInt("AgentAt");
            if(at==20){say(f,agent,SecondPersonSpringTexts.AGENT_ROUND.get(Math.min(stop,SecondPersonSpringTexts.AGENT_ROUND.size()-1)));handle(agent);}
            if(at>=STOP){own.remove("AgentAt");own.putInt("StopFrom",dt);if(stop+1>=ROUND.size())next(own,2);else own.putInt("AgentStop",stop+1);}
            return;
        }
        if(sub==2){
            if(dt==40){boolean cold=own.getBoolean("AgentCold");own.putString("Rent",cold?"same":"up");say(f,agent,cold?SecondPersonSpringTexts.AGENT_SAME:SecondPersonSpringTexts.AGENT_UP);
                SecondPersonStory.turned(own,cold?"You stood close to the agent, and the rent stayed where it was.":"You let the agent go round, and the rent went up.");}
            if(dt==140)say(f,a,SecondPersonSpringTexts.AGENT_THANKS);
            if(dt>=180)next(own,3);
            return;
        }
        if(dt==40){open(l,b,FLAT_DOOR,false);sound(p,SoundEvents.WOODEN_DOOR_CLOSE,b,FLAT_DOOR,1,1);}
        if(dt>=160)go(own,S_DECIDE);
    }
    /** Standing close to the agent makes the air go cold round them; held long enough, they hurry. */
    static void cold(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a,@Nullable LiteraryActor agent){
        if(agent==null||agent.distanceToSqr(p)>=1.8){f.agentNear=0;return;}
        if(++f.agentNear%10==0)l.sendParticles(p,ParticleTypes.SNOWFLAKE,true,agent.getX(),agent.getY()+1.2,agent.getZ(),3,.25,.4,.25,0);
        if(f.agentNear==COLD_TICKS&&!own.getBoolean("AgentCold")){
            own.putBoolean("AgentCold",true);say(f,agent,SecondPersonSpringTexts.AGENT_COLD);
            say(f,a,own.contains("Seen")||own.getBoolean("AllRight")?SecondPersonSpringTexts.COLD_KNOWN:SecondPersonSpringTexts.COLD_OLD);
            SecondPersonStory.add(own,"Cold","the agent");
        }
    }
    static void touched(ServerPlayer p,Flat f,LiteraryActor agent,CompoundTag own){
        say(f,agent,SecondPersonSpringTexts.AGENT_TOUCHED);
        if(!own.getBoolean("AgentCold")){f.agentNear=COLD_TICKS-1;}
    }

    static void decide(Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a){
        int dt=dt(own),sub=sub(own);
        if(sub==0){
            if(dt==60)say(f,a,SecondPersonSpringTexts.thinking(going(own)));
            if(dt==260)say(f,a,SecondPersonSpringTexts.OR_STAY);
            int laid=own.getInt("LaidAt");
            boolean noticed=laid>0&&own.getInt("Clock")-laid>=NOTICE;
            if(noticed||dt>=DECIDE_WAIT){
                if(own.getBoolean("LaidGo")&&!own.getBoolean("LaidStay"))say(f,a,SecondPersonSpringTexts.SAW_GO);
                else if(own.getBoolean("LaidStay")&&!own.getBoolean("LaidGo"))say(f,a,SecondPersonSpringTexts.SAW_STAY);
                next(own,1);
            }
            return;
        }
        if(dt==100){
            boolean stays=SecondPersonStory.stays(own);own.putString("Decision",stays?"stay":"go");own.putString("Going",stays?"":going(own));
            say(f,a,stays?SecondPersonSpringTexts.STAYING:SecondPersonSpringTexts.decided(going(own)));
        }
        if(dt>=240)go(own,S_POST);
    }
    /** The reader sets a thing down while they decide: their coat by the door is going; their things where they live is staying. */
    static void laid(CompoundTag own,String thing,String how,@Nullable BlockPos at){
        if(seasonOf(own)!=SPRING||step(own)!=S_DECIDE||sub(own)!=0)return;
        boolean go=how.equals("placed")&&at!=null&&byTheDoor(at)&&(thing.equals("coat")||thing.equals("photo")||thing.equals("drawing")||thing.equals("routine"));
        boolean stay=how.equals("home");
        if(!go&&!stay)return;
        own.putBoolean(go?"LaidGo":"LaidStay",true);own.putInt("LaidAt",own.getInt("Clock"));
        SecondPersonStory.turned(own,go?"You laid "+SecondPersonStory.thing(own,thing)+" by the door, as if for going.":"You put "+SecondPersonStory.thing(own,thing)+" where it lives, as if for staying.");
    }

    static void post(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,@Nullable LiteraryActor a){
        var b=f.base;int sub=sub(own),dt=dt(own);String app=own.getString("Application");
        if(sub==0){
            if(dt==40){sound(p,SoundEvents.BOOK_PAGE_TURN,b,MAT,1,.8F);cue(p,"The post comes through the door.");
                if(l.getBlockState(b.offset(APPLICATION_AT)).isAir())SecondPersonSite.set(l,b,APPLICATION_AT,SecondPersonBlock.of(SecondPersonBlock.Kind.APPLICATION,Direction.SOUTH,0));
                own.putString("Application","mat");}
            // They go for the post in their own time; sooner once the reader has had their hand on the form.
            boolean touched=app.equals("read")||app.equals("down");
            if((dt>=POST_WAIT||touched&&dt>=200)&&!Talk.waiting(p,TALK))next(own,1);
            return;
        }
        if(sub==1){if(dt>20&&arrived(a,b,residentMark(own))||dt>400){handle(a);next(own,2);}return;}
        if(sub==2){
            if(app.equals("down")){
                if(dt==1){application(l,b,false);cue(p,"They pick it up with the rest of the post, and never turn it over.");}
                if(dt>=60)next(own,3);
                return;
            }
            if(app.equals("read")){
                if(dt==1){application(l,b,false);say(f,a,SecondPersonSpringTexts.READS);}
                if(dt==90){say(f,a,SecondPersonSpringTexts.ASK);own.putInt("AskedAt",own.getInt("Clock"));SecondPersonStory.turned(own,"They read the form with your name on it, and asked the room if it was you.");}
                boolean answered=!own.getString("SpringAnswer").isEmpty();
                if(dt>90&&(answered&&own.getInt("Clock")-own.getInt("AnsweredAt")>=80||dt>=90+ASK_WAIT)){own.remove("AskedAt");if(!answered)say(f,a,SecondPersonSpringTexts.NO_ANSWER);next(own,3);}
                return;
            }
            if(dt==1){application(l,b,false);say(f,a,SecondPersonSpringTexts.KEEN);own.putString("Application","theirs");SecondPersonStory.turned(own,"You left the form on the mat, and they read it and said somebody was keen.");}
            if(dt>=60)next(own,3);
            return;
        }
        if(sub==3){if(dt>20&&arrived(a,b,residentMark(own))||dt>300){handle(a);sound(p,SoundEvents.CHISELED_BOOKSHELF_PICKUP,b,HALL_TABLE,.7F,1);cue(p,"They put the form in the hall table drawer.");next(own,4);}return;}
        if(dt>=120)go(own,SPRING_DONE);
    }
    static void application(ServerLevel l,BlockPos b,boolean there){if(!there&&SecondPersonBlock.is(l.getBlockState(b.offset(APPLICATION_AT)),SecondPersonBlock.Kind.APPLICATION))l.setBlock(b.offset(APPLICATION_AT),Blocks.AIR.defaultBlockState(),3);}

    // ------------------------------------------------------------------------------------------------ the form on the mat
    static final String TALK="sp_application";
    /** The reader's hand on the form on the mat. */
    static boolean click(ServerPlayer p,BlockPos b,CompoundTag own){
        String app=own.getString("Application");
        if(app.equals("down")){cue(p,"Face down.");return true;}
        if(app.equals("signed")){cue(p,SecondPersonSpringTexts.REREAD);return true;}
        if(app.equals("read")){
            // 0.4.634: read, it can still be signed while it lies there.
            if(sub(own)>=2){cue(p,SecondPersonSpringTexts.REREAD);return true;}
            Talk.begin(p,TALK,SecondPersonSpringTexts.FORM);Talk.line(p,"",SecondPersonSpringTexts.REREAD);
            Talk.ask(p,Talk.act(SecondPersonSpringTexts.SIGN_IT,q->choose(q,b,"sign")),Talk.act(SecondPersonSpringTexts.LEAVE_IT,Talk::close));return true;}
        if(!app.equals("mat")||sub(own)>=2)return true;
        Talk.begin(p,TALK,SecondPersonSpringTexts.FORM);
        for(var line:SecondPersonSpringTexts.form(p.getGameProfile().getName()))Talk.line(p,"",line);
        Talk.ask(p,Talk.act(SecondPersonSpringTexts.READ_IT,q->choose(q,b,"read")),Talk.act(SecondPersonSpringTexts.SIGN_IT,q->choose(q,b,"sign")),Talk.act(SecondPersonSpringTexts.FACE_DOWN,q->choose(q,b,"down")),Talk.act(SecondPersonSpringTexts.LEAVE_IT,Talk::close));
        return true;
    }
    static void choose(ServerPlayer p,BlockPos b,String how){
        var own=own(p);String was=own.getString("Application");var l=p.serverLevel();
        if(how.equals("sign")){if((was.equals("mat")||was.equals("read"))&&step(own)==S_POST&&sub(own)<2)SecondPersonSigned.sign(p,own);else Talk.close(p);return;}
        if(!was.equals("mat")){Talk.close(p);return;}
        own.putString("Application",how);
        if(how.equals("read")){for(var line:SecondPersonSpringTexts.READ_ALL)Talk.line(p,"",line);Talk.end(p);SecondPersonStory.turned(own,"You read the form on the mat. It was in your handwriting.");}
        else{Talk.close(p);SecondPersonSite.set(l,b,APPLICATION_AT,SecondPersonBlock.of(SecondPersonBlock.Kind.APPLICATION,Direction.SOUTH,1));sound(p,SoundEvents.BOOK_PAGE_TURN,b,APPLICATION_AT,.7F,1.2F);SecondPersonStory.turned(own,"You turned the form face down.");}
        save(p,own);
    }
    /** While they wait for an answer to "Is this you?", a word on the glass is read at once, and kept. */
    static boolean asking(CompoundTag own){return seasonOf(own)==SPRING&&step(own)==S_POST&&own.contains("AskedAt")&&own.getString("SpringAnswer").isEmpty();}
    static void answered(Flat f,@Nullable LiteraryActor a,CompoundTag own,String word){
        own.putString("SpringAnswer",word);own.putInt("AnsweredAt",own.getInt("Clock"));
        say(f,a,SecondPersonSpringTexts.answer(word));SecondPersonStory.turned(own,"They asked if it was you. You wrote "+word+" on the glass.");
    }

    // ------------------------------------------------------------------------------------------------ the account
    static void account(StringBuilder b,CompoundTag own){
        if(own.getInt("Season")!=SPRING&&!own.contains("Rent")&&own.getString("Application").isEmpty())return;
        b.append("In the spring the tenancy came up. A letting agent came round with a clipboard");
        b.append(switch(own.getString("Rent")){case "same"->". You stood close enough for the cold to be felt, and they hurried, and the rent stayed where it was.";case "up"->", and the rent went up.";default->".";});
        if(own.getBoolean("LaidGo"))b.append(" While they decided, you laid their things by the door.");
        else if(own.getBoolean("LaidStay"))b.append(" While they decided, you put their things where they lived.");
        switch(own.getString("Application")){
            case "read"->{b.append(" Then a form for the flat came through the door, for when the current tenant leaves, in your handwriting, with your name on it. You read it. They read it too, and asked the room if it was you.");
                if(!own.getString("SpringAnswer").isEmpty())b.append(" You wrote ").append(own.getString("SpringAnswer")).append(" on the glass, and they kept the word.");}
            case "down"->b.append(" Then a form for the flat came through the door, in your handwriting, with your name on it. You turned it face down, and they never turned it over.");
            case "signed"->b.append(" Then a form for the flat came through the door, for when the current tenant leaves, in your handwriting, with your name on it.");
            case "theirs","mat"->b.append(" Then a form for the flat came through the door, in your handwriting, with your name on it. You left it on the mat. They read it and said somebody was keen.");
            default->{}
        }
        b.append("\n\n");
    }
}
