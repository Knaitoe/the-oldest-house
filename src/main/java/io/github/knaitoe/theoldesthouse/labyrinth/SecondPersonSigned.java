package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.SecondPerson.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.SecondPersonSite.*;

/**
 * 0.4.634, Second Person, E2: their name on the post (docs/SECOND_PERSON_0_4_634.md; the proposal's ledger row 24). The reader signs
 * the form on the mat. For the last day of the year the reader is the one who lives there: Sam knocks and talks to them as the tenant,
 * the phone rings for them, and at dark, in the black street window, the resident stands behind them where the reader used to stand,
 * and the reader cannot make them hear. The door is on the latch; the reader goes out by it. The swap is the account's, not the
 * reader's real self: they still leave by the front door.
 */
public final class SecondPersonSigned {
    private SecondPersonSigned(){}
    /** The spring's step once the form is signed. */
    public static final int S_SIGNED=4;
    public static final String TALK_SAM="sp_sam_signed";
    /** How long Sam waits on the landing for the door, and how long the evening waits before the latch. */
    static final int SAM_WAIT=900,EVENING=1200,BEHIND_FOR=200;
    static final Vec3 BEHIND=at(-3.5,UP,-16.2);

    public static boolean signed(CompoundTag own){return own.getString("Application").equals("signed");}
    static boolean inSigned(CompoundTag own){return seasonOf(own)==SPRING&&step(own)==S_SIGNED;}

    /** The reader writes their name on the line. */
    static void sign(ServerPlayer p,CompoundTag own){
        var l=p.serverLevel();var b=LiteraryVignettes.base(p,SecondPerson.PLACE);if(b==null)return;var f=flat(b);
        own.putString("Application","signed");
        SecondPersonSite.set(l,b,SecondPersonSpring.APPLICATION_AT,SecondPersonBlock.of(SecondPersonBlock.Kind.APPLICATION,Direction.SOUTH,2));
        for(var line:SecondPersonSpringTexts.SIGNED)Talk.line(p,"",line);Talk.end(p);
        sound(p,SoundEvents.BOOK_PAGE_TURN,b,SecondPersonSpring.APPLICATION_AT,.8F,.9F);
        SecondPersonStory.turned(own,"You signed the form on the mat.");
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,80,0,false,false));
        go(own,S_SIGNED);
        if(f!=null){var a=actor(l,f.resident);if(a!=null){if(a.isSleeping())a.stopSleeping();a.discard();}f.resident=null;}
        save(p,own);
    }

    static Mark residentMark(CompoundTag own){return sub(own)==3?new Mark(BEHIND,SOUTH,G_REACH,false,true):Mark.gone();}
    static Mark samMark(CompoundTag own){
        if(!inSigned(own)||sub(own)!=1)return Mark.gone();
        return own.getString("SamDoor").isEmpty()||dt(own)<own.getInt("SamGoneAt")+80?Mark.stand(4.5,UP,-27.5,WEST):Mark.gone();
    }
    static long hour(CompoundTag own){return sub(own)>=3?13800:sub(own)==2?6200:2800;}
    static boolean dark(CompoundTag own){return inSigned(own)&&sub(own)>=3;}

    static void day(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,Vec3 r,@Nullable LiteraryActor a,@Nullable LiteraryActor sam){
        var b=f.base;int sub=sub(own),dt=dt(own);
        switch(sub){
            case 0->{
                if(dt==1){lamp(l,b,false);cue(p,SecondPersonSpringTexts.LAST_DAY);}
                if(dt>=100)next(own,1);
            }
            case 1->{
                if(dt==30){sound(p,DoorSounds.KNOCK.get(),b,FLAT_DOOR,1,1);cue(p,"Somebody knocks at the flat door.");}
                if(!own.getString("SamDoor").isEmpty()){if(dt>=own.getInt("SamGoneAt")+120)next(own,2);return;}
                boolean near=p.position().distanceTo(Vec3.atCenterOf(b.offset(FLAT_DOOR)))<4;
                if(sam!=null&&dt>40&&near&&!Talk.waiting(p,TALK_SAM))samTalks(p,b,sam);
                else if(dt>=SAM_WAIT&&!Talk.waiting(p,TALK_SAM)){own.putString("SamDoor","missed");own.putInt("SamGoneAt",dt);cue(p,SecondPersonSpringTexts.SAM_GONE);}
            }
            case 2->{
                if(dt==60){f.ring=RING;own.putInt("Ringing",4);}
                if(!own.getString("Call4").isEmpty()&&f.ring<0&&!Talk.waiting(p,"sp_call_4")&&dt>80)next(own,3);
            }
            case 3->{
                if(dt==20){cue(p,SecondPersonSpringTexts.DARK);open(l,b,FLAT_DOOR,false);}
                boolean atWindow=r.x>=-5.6&&r.x<=-1.4&&r.z>=-15.2&&r.z<=-13.2&&r.y>=UP-.5&&r.y<UP+2.5;
                if(a!=null&&atWindow&&!own.getBoolean("SawBehind")){
                    own.putBoolean("SawBehind",true);own.putInt("BehindAt",dt);cue(p,SecondPersonSpringTexts.BEHIND);
                    SecondPersonStory.turned(own,"In the black street window you saw them behind you, where you used to stand.");}
                if(own.getBoolean("SawBehind")){int since=dt-own.getInt("BehindAt");
                    if(since==60)say(f,a,SecondPersonSpringTexts.THEY_ASK);
                    if(since==150)say(f,a,SecondPersonSpringTexts.THEY_ASK_AGAIN);
                    if(since>=BEHIND_FOR)next(own,4);}
                else if(dt>=EVENING)next(own,4);
            }
            case 4->{
                if(dt==1){open(l,b,FLAT_DOOR,true);cue(p,"The front door is on the latch.");}
                if(onStreet(r)){next(own,5);benchBook(l,b,true);LiteraryVignettes.ready(p,SecondPerson.PLACE,own,"their_name_on_the_post");cue(p,"On the bench at the bus stop, where nobody waits, there is a book.");}
            }
            default->{}
        }
    }
    static void samTalks(ServerPlayer p,net.minecraft.core.BlockPos b,LiteraryActor sam){
        Talk.begin(p,TALK_SAM,"Sam");
        for(var line:SecondPersonSpringTexts.SAM_HELLO){Talk.line(p,"Sam",line);}
        sam.say("Sam",SecondPersonSpringTexts.SAM_HELLO.get(0));
        Talk.ask(p,Talk.reply(SecondPersonSpringTexts.COME_IN,q->samAnswer(q,"in")),Talk.reply(SecondPersonSpringTexts.NOT_TODAY,q->samAnswer(q,"out")));
    }
    static void samAnswer(ServerPlayer p,String how){
        var own=own(p);if(!inSigned(own)||!own.getString("SamDoor").isEmpty()){Talk.close(p);return;}
        own.putString("SamDoor",how);own.putInt("SamGoneAt",dt(own));
        Talk.line(p,"Sam",how.equals("in")?SecondPersonSpringTexts.SAM_IN:SecondPersonSpringTexts.SAM_OUT);Talk.end(p);
        SecondPersonStory.turned(own,how.equals("in")?"Sam came to the door and talked to you as if you lived there. You asked them in.":"Sam came to the door and talked to you as if you lived there. You said not today.");
        save(p,own);
    }
    /** A hand on them, in the dark behind the reader: the cold round them, and nothing they can hear. */
    static void touched(Flat f,LiteraryActor a,CompoundTag own){
        say(f,a,"...");own.putBoolean("TouchedBehind",true);
        ((ServerLevel)a.level()).sendParticles(ParticleTypes.SNOWFLAKE,a.getX(),a.getY()+1.2,a.getZ(),6,.25,.4,.25,0);
    }

    static String page(CompoundTag own){
        return switch(sub(own)){
            case 0->SecondPersonSpringTexts.LAST_DAY+"\n\n";
            case 1->"Somebody is at the flat door.\n\n1. Open it.\n2. Or let them knock.\n";
            case 2->"The telephone. It is for you.\n\n1. Lift it, and listen.\n2. Or let it ring.\n";
            case 3->"It is dark. The street window is black.\n\n1. Stand at it.\n";
            default->"The front door is on the latch.\n\n1. Go out by it and down to the street.\n";
        };
    }
    static void account(StringBuilder b,CompoundTag own){
        b.append("You signed the form. For the last day of the year you lived there.");
        b.append(switch(own.getString("SamDoor")){case "in"->" Sam came to the door and talked to you as if you always had, and you asked them in.";case "out"->" Sam came to the door and talked to you as if you always had, and you said not today.";default->" Somebody knocked, and went back down the stairs.";});
        b.append(switch(own.getString("Call4")){case "lifted"->" The phone rang for you, and you listened.";case "put_down"->" The phone rang for you, and you put it down.";case "off"->" The phone rang for you, and you left it off the hook.";default->" The phone rang for you, and you let it.";});
        if(own.getBoolean("SawBehind"))b.append(" At dark, in the black window, they stood behind you where you used to stand"+(own.getBoolean("TouchedBehind")?", and you put a hand on them, and they shivered, and did not turn round.":", and you could not make them hear."));
        b.append(" The year was ordinary, and you had it.\n\nThe door was on the latch. You went out by it.");
    }
}
