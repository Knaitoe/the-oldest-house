package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static io.github.knaitoe.theoldesthouse.labyrinth.FallowSite.*;

/**
 * 0.4.681, Fallow, the fourth part (docs/FALLOW_0_4_681.md): buried. The readers who were in the cellar when the light came stay in it.
 * When the ground is still, the buried days begin, on the afternoon's own clock (which still runs only while somebody is at the station),
 * each day a little under a minute by Otis's watch, its last part night.
 *
 * <p>The lantern has the oil it has and no more. It burns while it is lit, half as fast turned low, and gutters at the last; any reader
 * below can turn it down, put it out or light it again with a match while there is oil. Otis puts it out for the night and lights it in
 * the morning. The roof's timbers are hot the first day, warm the second and cool the third, and until they are cool the doors over the
 * steps are too hot to dig at. Opal sets her bean down by the cot; it grows a stage for every so long it stands in the dark, and once it
 * has grown long and thin it leans toward the steps. On the second day Otis asks each reader below, one at a time, whether anybody is
 * waiting on them at home; whatever they answer, what he says to it follows the House's read of them.
 *
 * <p>From the third day the doors are dug out a stroke at a time: a reader's hand on them from the steps, or on the fall over them from the
 * yard for a reader who has come back. Otis digs alongside whoever is digging, and from the fourth day on his own. Each of the doors' eight
 * lengths comes clear in turn, letting in daylight, and when all are clear the doors give and go back over on their hinges. Whoever is
 * below then climbs out into what the light left. Each reader keeps their own: the days they were below, what they did with the lantern,
 * what they told Otis, how many strokes they dug and whether they came up.
 *
 * <p>The people the light found outside are gone from the station: Boyd, who would not go down, and Harlan and Faye if they did not. Boyd's
 * shadow is left on the garage's front beside where he stood at the rig. Otis and Opal are always below. Spectators dig nothing, turn
 * nothing and are asked nothing.
 */
public final class FallowBuried {
    private FallowBuried(){}
    /** Ticks from the light until the ground is still: the dust and the shaking of the third part. */
    public static final int SETTLE=FallowStrike.BELOW_FAINT;
    /** A day below, by Otis's watch, and when in it the lantern is put out for the night. */
    public static final int DAY=900,NIGHT_AT=660;
    /** The day (from 0) the roof is cool enough to dig; the day Otis begins alone. */
    public static final int COOL_DAY=2,ALONE_DAY=3;
    /** The lantern's oil, in ticks burning full; the last of it, when it gutters. */
    public static final int OIL=2400,GUTTER=240;
    /** Opal's bean grows a stage for every so many ticks it stands in the dark. */
    public static final int GROWS=160;
    /** Strokes to dig each of the doors' eight lengths clear; a reader's stroke at most this often; Otis's while he digs; how long after
     * a reader's last stroke he keeps at it with them. */
    public static final int PER_CELL=12,CELLS=8,DIG=PER_CELL*CELLS,STROKE=20,OTIS_STROKE=30,HELPING=80;
    /** How long a buried station is kept when nobody is at it (a reader below who has left the game), before it is let go. */
    public static final int GRACE=1200;
    /** Otis asks one reader at a time, so far apart; the people below say something every so often by day. */
    static final int ASK_GAP=240,TALK_GAP=320;
    /** Where Opal sets her bean, by the cot's foot; where she sits by it. */
    public static final BlockPos BEAN_BELOW=new BlockPos(-11,-3,-13);
    static final Vec3 BY_BEAN=new Vec3(-11.5,-3,-14.5);
    /** Otis on the third step, the doors just over his head; the way there from his place. */
    static final Vec3 DIG_AT=new Vec3(-13.5,-2,-9.5);
    static final List<Vec3> TO_DIG=List.of(new Vec3(-13.5,-3,-12.5),new Vec3(-13.5,-3,-11.5),new Vec3(-13.5,-3,-10.5),DIG_AT);
    /** The doors' eight lengths, in the order they come clear: from the top of the steps down. */
    static final int[][] ORDER={{WELL_E,WELL_S},{WELL_W,WELL_S},{WELL_E,WELL_S-1},{WELL_W,WELL_S-1},{WELL_E,WELL_S-2},{WELL_W,WELL_S-2},{WELL_E,WELL_N},{WELL_W,WELL_N}};
    /** Where each comes to when they are up: Otis at the top of the steps, Opal beside them, Faye by her, Harlan at what is left of the store. */
    static final Vec3 OTIS_UP=new Vec3(-13.5,0,-5.5),OPAL_UP=new Vec3(-11.5,0,-6.5),FAYE_UP=new Vec3(-10.5,0,-5.5),HARLAN_UP=new Vec3(-9.5,0,-3.5);
    /** The way up the steps from the room, and on out into the yard. */
    static final List<Vec3> UP=List.of(new Vec3(-13.5,-3,-12.5),new Vec3(-13.5,-3,-11.5),new Vec3(-13.5,-3,-10.5),new Vec3(-13.5,-2,-9.5),new Vec3(-13.5,-1,-8.5),
        new Vec3(-13.5,0,-7.5),new Vec3(-13.5,0,-6.5));
    static final String HOME_TALK="fallow_home";

    // ------------------------------------------------------------------------------------------------ the light
    /** At the light: whoever of the five is out in it is gone from the station; Boyd's shadow is left on the garage's front. Otis and Opal
     * are below whatever has happened, so that nobody of theirs is out in it. */
    static void light(ServerLevel l,Fallow.Site s){
        var st=s.strike;if(st.lightDone)return;st.lightDone=true;
        for(int who:new int[]{Fallow.DRIVER,Fallow.GIRL}){
            var a=Fallow.find(l,s.base,who);if(a==null||below(s,a))continue;
            if(a.isPassenger())a.stopRiding();a.ejectPassengers();s.routes.remove(who);
            var at=s.world(who==Fallow.GIRL?FallowStrike.OPAL_BELOW:FallowStrike.OTIS_REST);a.moveTo(at.x,at.y,at.z,a.getYRot(),0);
        }
        for(int who=0;who<Fallow.PEOPLE;who++){
            if(who==Fallow.DRIVER||who==Fallow.GIRL)continue;
            var a=Fallow.find(l,s.base,who);if(a==null)continue;
            if(below(s,a)){st.below|=1<<who;continue;}
            var r=a.position().subtract(Vec3.atLowerCornerOf(s.base));
            if(who==Fallow.MAN)FallowRuin.shadow(l,s.base,a,r,FallowTexts.NAMES[who],a.gesture()==Fallow.REACH);
            st.lost|=1<<who;s.routes.remove(who);s.away.add(who);a.discard();
        }
        st.below|=1<<Fallow.DRIVER|1<<Fallow.GIRL;
    }
    static boolean below(Fallow.Site s,LiteraryActor a){return FallowSite.inCellar(a.position().subtract(Vec3.atLowerCornerOf(s.base)));}
    /** Whether one of the five is below in the buried days. */
    static boolean down(Fallow.Site s,int who){return (s.strike.below&1<<who)!=0;}

    // ------------------------------------------------------------------------------------------------ the days
    /** So many ticks into the buried days. */
    static void tick(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int k){
        var st=s.strike;
        if(!st.began)begin(l,s,here);
        int day=k/DAY,within=k%DAY;boolean night=within>=NIGHT_AT;
        if(!st.open){
            // A head on the steps kept a length of the doors clear when the fall came down; it comes down there once the head is gone.
            if(k%20==0&&st.cleared==0)FallowRuin.recover(l,s.base);
            lantern(l,s,here,k,within,night);
            bean(l,s,here);
            people(l,s,here,k,day,within,night);
            ask(l,s,here,k,day,night);
            otis(l,s,here,k,day,night);
            if(within==0&&k>0)morning(l,s,here,day);
            if(day==0&&k%300==150)for(var p:here)if(cellar(s,p))FallowStrike.heard(p,FallowRegistry.SETTLE.get(),.6F);
        }else up(l,s,k);
        for(var p:here)reader(s,p,day);
    }
    /** The ground still: the five who are below each at their place, Opal's bean set down by the cot, the lantern burning. */
    static void begin(ServerLevel l,Fallow.Site s,List<ServerPlayer> here){
        var st=s.strike;st.began=true;st.oil=OIL;st.dayTalk=0;st.dayTalkAt=-1;
        for(int who=0;who<Fallow.PEOPLE;who++){
            if(!down(s,who))continue;var a=Fallow.find(l,s.base,who);if(a==null)continue;
            if(a.isPassenger())a.stopRiding();
            s.routes.remove(who);s.speed.put(who,.09);
            // Each goes to their own place below, walking there if they are not at it.
            var at=s.world(rest(who));
            if(!below(s,a))a.moveTo(at.x,at.y,at.z,a.getYRot(),0);else if(a.position().distanceToSqr(at)>.25)Fallow.send(s,who,List.of(rest(who)));
            a.gesture(0);a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        }
        var girl=Fallow.find(l,s.base,Fallow.GIRL);
        var at=s.base.offset(BEAN_BELOW);
        if(l.isLoaded(at)&&l.getBlockState(at).isAir()){
            BuildBlocks.set(l,at,FallowBlock.of(FallowBlock.Kind.BEAN,Direction.SOUTH,0),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
            if(girl!=null){Fallow.faceTo(girl,Vec3.atCenterOf(at));girl.gesture(Fallow.KNEEL);st.kneel=40;}
        }
        lantern(l,s.base,1);
        for(var p:here)if(cellar(s,p))Fallow.cue(p,FallowTexts.STILL);
    }
    /** Where each sits below: Otis at the far end, Opal by her bean, Harlan by the shelves, Faye by Opal. */
    static Vec3 rest(int who){
        return switch(who){case Fallow.DRIVER->FallowStrike.OTIS_REST;case Fallow.GIRL->BY_BEAN;case Fallow.KEEPER->FallowStrike.HARLAN_REST;default->FallowStrike.FAYE_REST;};
    }
    /** What each sits facing: the room, her bean, the steps. */
    static Vec3 faces(Fallow.Site s,int who){
        return s.world(switch(who){case Fallow.GIRL->Vec3.atCenterOf(BEAN_BELOW);case Fallow.KEEPER->new Vec3(-12.5,-3,-12.5);case Fallow.WOMAN->Vec3.atCenterOf(BEAN_BELOW);
            default->new Vec3(-12.5,-3,-13.5);});
    }
    /** Each morning: the day told to whoever is below, and Otis's watch. */
    static void morning(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int day){
        var st=s.strike;
        var otis=Fallow.find(l,s.base,Fallow.DRIVER);
        int stage=lantern(l,s.base);
        String line;
        if(st.oil>0&&stage==FallowBlock.LANTERN_DARK){lantern(l,s.base,FallowBlock.LANTERN_LIT);play(l,s,FallowRegistry.MATCH.get(),.7F);line=FallowTexts.OTIS_MORNING;}
        else line=st.oil>0?FallowTexts.OTIS_MORNING:FallowTexts.OTIS_MORNING_DARK;
        if(otis!=null){otis.say("Otis",line);otis.speaks(line);}
        String told=FallowTexts.MORNING[Math.min(day-1,FallowTexts.MORNING.length-1)];
        for(var p:here)if(cellar(s,p))Fallow.cue(p,told);
        st.dayTalk=0;st.dayTalkAt=-1;
    }

    // ------------------------------------------------------------------------------------------------ the lantern
    /** The lantern's stage, or -1 if it is not there. */
    static int lantern(ServerLevel l,BlockPos b){var at=b.offset(LANTERN);if(!l.isLoaded(at))return -1;var st=l.getBlockState(at);return FallowBlock.is(st,FallowBlock.Kind.CROCK)?st.getValue(FallowBlock.STAGE):-1;}
    static void lantern(ServerLevel l,BlockPos b,int stage){
        var at=b.offset(LANTERN);if(!l.isLoaded(at))return;var st=l.getBlockState(at);
        if(FallowBlock.is(st,FallowBlock.Kind.CROCK)&&st.getValue(FallowBlock.STAGE)!=stage)l.setBlock(at,st.setValue(FallowBlock.STAGE,stage),3);
    }
    static boolean lit(int stage){return stage==FallowBlock.LANTERN_LIT||stage==FallowBlock.LANTERN_LOW;}
    /** The oil burns while the lantern is lit, half as fast turned low; it gutters at the last and goes out for good. Otis puts it out at
     * nightfall. While it is dark, Opal's bean grows. */
    static void lantern(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int k,int within,boolean night){
        var st=s.strike;int stage=lantern(l,s.base);if(stage<0)return;
        if(lit(stage)){
            if(stage==FallowBlock.LANTERN_LIT||k%2==0)st.oil--;
            if(st.oil<=0){
                lantern(l,s.base,FallowBlock.LANTERN_DARK);play(l,s,FallowRegistry.WICK.get(),.5F);
                for(var p:here)if(cellar(s,p))Fallow.cue(p,FallowTexts.LANTERN_GONE);
                return;
            }
            if(st.oil<=GUTTER){
                if(!st.guttered){st.guttered=true;for(var p:here)if(cellar(s,p))Fallow.cue(p,FallowTexts.LANTERN_GUTTERS);}
                // The flame shrinks and jumps: low and high by turns, never in step.
                if(k%5==0)lantern(l,s.base,(k/5)%3==1||l.getRandom().nextInt(4)==0?FallowBlock.LANTERN_LOW:FallowBlock.LANTERN_LIT);
            }
            if(within==NIGHT_AT){
                var otis=Fallow.find(l,s.base,Fallow.DRIVER);
                lantern(l,s.base,FallowBlock.LANTERN_DARK);play(l,s,FallowRegistry.WICK.get(),.5F);
                if(otis!=null){otis.say("Otis",FallowTexts.OTIS_NIGHT);otis.speaks(FallowTexts.OTIS_NIGHT);}
            }
        }else st.dark++;
    }
    /** A reader's hand on the lantern: down, out, and lit again with a match while there is oil. */
    static void turn(ServerPlayer p,ServerLevel l,Fallow.Site s,CompoundTag own){
        var st=s.strike;int stage=lantern(l,s.base);
        String cue;
        if(stage==FallowBlock.LANTERN_LIT){lantern(l,s.base,FallowBlock.LANTERN_LOW);cue=FallowTexts.LANTERN_LOW;play(l,s,FallowRegistry.WICK.get(),.5F);}
        else if(stage==FallowBlock.LANTERN_LOW){lantern(l,s.base,FallowBlock.LANTERN_DARK);cue=FallowTexts.LANTERN_DOUSED;play(l,s,FallowRegistry.WICK.get(),.5F);}
        else if(st.oil<=0)cue=FallowTexts.LANTERN_DRY;
        else{
            lantern(l,s.base,FallowBlock.LANTERN_LIT);cue=FallowTexts.LANTERN_LIT;play(l,s,FallowRegistry.MATCH.get(),.7F);
            // At night Otis would rather it stayed dark; he says so once a night.
            int k=since(s);
            if(k%DAY>=NIGHT_AT&&st.leaveIt!=k/DAY){
                st.leaveIt=k/DAY;var otis=Fallow.find(l,s.base,Fallow.DRIVER);
                if(otis!=null){Fallow.faceTo(otis,p.position());otis.sayTo(p,"Otis",FallowTexts.OTIS_LEAVE_IT);otis.speaks(FallowTexts.OTIS_LEAVE_IT);}
            }
        }
        Fallow.cue(p,cue);
        own.putInt("Lantern",own.getInt("Lantern")+1);
    }

    // ------------------------------------------------------------------------------------------------ Opal's bean
    /** Her bean grows in the dark; when the light comes back it has grown, and those below see it. */
    static void bean(ServerLevel l,Fallow.Site s,List<ServerPlayer> here){
        var st=s.strike;var at=s.base.offset(BEAN_BELOW);if(!l.isLoaded(at))return;var bs=l.getBlockState(at);
        if(!FallowBlock.is(bs,FallowBlock.Kind.BEAN))return;
        int want=Math.min(3,st.dark/GROWS),now=bs.getValue(FallowBlock.STAGE);
        if(want>now){l.setBlock(at,bs.setValue(FallowBlock.STAGE,want),3);st.grew=true;}
        if(st.grew&&lit(lantern(l,s.base))){
            st.grew=false;
            for(var p:here)if(cellar(s,p))Fallow.cue(p,FallowTexts.BEAN_GREW);
            var girl=Fallow.find(l,s.base,Fallow.GIRL);
            if(girl!=null){String line=FallowTexts.BEAN_BELOW[Math.min(3,want)];girl.say("Opal",line);girl.speaks(line);}
        }
    }

    // ------------------------------------------------------------------------------------------------ the people below
    /** Each below sits at their place by day and dozes by night; a word every so often by day from whoever is there to say it. */
    static void people(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int k,int day,int within,boolean night){
        var st=s.strike;
        if(st.kneel>0)st.kneel--;
        for(int who=0;who<Fallow.PEOPLE;who++){
            if(!down(s,who))continue;var a=Fallow.find(l,s.base,who);if(a==null||Fallow.busy(s,who))continue;
            if(who==Fallow.DRIVER&&st.digging)continue;
            if(who==Fallow.GIRL&&st.kneel>0)continue;
            int want=night?Fallow.DOZE:Fallow.SIT;
            if(a.gesture()!=want){a.gesture(want);Fallow.faceTo(a,faces(s,who));}
            // Seated, the body is still; only the head turns to a reader who speaks to them (LiteraryActor).
            a.setDeltaMovement(Vec3.ZERO);
        }
        if(night)return;
        if(st.dayTalkAt<0)st.dayTalkAt=within+120;
        if(within<st.dayTalkAt)return;
        var lines=FallowTexts.BELOW_TALK[Math.min(day,FallowTexts.BELOW_TALK.length-1)];
        while(st.dayTalk<lines.length){
            var said=lines[st.dayTalk++];
            int who=index(said[0]);if(!down(s,who))continue;
            if(!said[2].isEmpty()&&!holds(l,s,said[2]))continue;
            var a=Fallow.find(l,s.base,who);if(a==null)continue;
            a.say(said[0],said[1]);a.speaks(said[1]);break;
        }
        st.dayTalkAt=within+TALK_GAP;
    }
    /** Whether a line's condition holds: Faye below or not, Harlan below, the bean grown. */
    static boolean holds(ServerLevel l,Fallow.Site s,String when){
        return switch(when){
            case "faye"->down(s,Fallow.WOMAN);case "nofaye"->!down(s,Fallow.WOMAN);case "harlan"->down(s,Fallow.KEEPER);
            case "grown"->{var bs=l.getBlockState(s.base.offset(BEAN_BELOW));yield FallowBlock.is(bs,FallowBlock.Kind.BEAN)&&bs.getValue(FallowBlock.STAGE)>=1;}
            default->true;
        };
    }
    static int index(String name){for(int i=0;i<FallowTexts.NAMES.length;i++)if(FallowTexts.NAMES[i].equals(name))return i;return -1;}

    /** On the second day Otis asks each reader below, one at a time, whether anybody is waiting on them at home. */
    static void ask(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int k,int day,boolean night){
        var st=s.strike;
        if(day<1||night||st.digging||k-st.askedAt<ASK_GAP)return;
        var otis=Fallow.find(l,s.base,Fallow.DRIVER);if(otis==null||Fallow.busy(s,Fallow.DRIVER))return;
        for(var p:here){
            if(!cellar(s,p)||Talk.waiting(p))continue;
            var own=Fallow.own(p);if(own.getBoolean("AskedHome"))continue;
            own.putBoolean("AskedHome",true);Fallow.save(p,own);st.askedAt=k;
            Fallow.faceTo(otis,p.position());otis.sayTo(p,"Otis",FallowTexts.HOME_ASK);otis.speaks(FallowTexts.HOME_ASK);
            Talk.begin(p,HOME_TALK,"Otis");Talk.line(p,"Otis",FallowTexts.HOME_ASK);
            Talk.ask(p,Talk.act(FallowTexts.TELL_HIM,q->answered(q,true)),Talk.act(FallowTexts.SAY_NOTHING,q->answered(q,false)));
            return;
        }
    }
    /** What Otis says to the answer follows the House's read of the reader: somebody to get back to, somewhere not to go back to, nowhere
     * at all, or nothing read. To silence he says it is all right. */
    static void answered(ServerPlayer p,boolean told){
        var own=Fallow.own(p);var read=HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.HOME,false);
        own.putBoolean(told?"ToldHome":"QuietHome",true);if(told)own.putString("HomeRead",read);Fallow.save(p,own);
        String line=told?FallowTexts.homeAnswer(read):FallowTexts.HOME_QUIET;
        Talk.line(p,"Otis",line);Talk.end(p);
        var s=of(p);if(s==null)return;
        var otis=Fallow.find(p.serverLevel(),s.base,Fallow.DRIVER);
        if(otis!=null){otis.sayTo(p,"Otis",line);otis.speaks(line);}
    }

    // ------------------------------------------------------------------------------------------------ digging out
    /** Otis digs alongside whoever is digging once the roof is cool, and from the fourth day on his own; by day. */
    static void otis(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int k,int day,boolean night){
        var st=s.strike;var a=Fallow.find(l,s.base,Fallow.DRIVER);if(a==null)return;
        boolean alone=day>=ALONE_DAY&&s.tick-st.dugAt>=HELPING;
        boolean dig=day>=COOL_DAY&&!night&&(s.tick-st.dugAt<HELPING||day>=ALONE_DAY);
        if(dig){
            if(!st.digging){
                st.digging=true;a.gesture(0);Fallow.send(s,Fallow.DRIVER,TO_DIG);
                // Each said once: the first time he goes up to dig with somebody, and the first time he goes up alone.
                int bit=alone?2:1;
                if((st.otisSaid&bit)==0){st.otisSaid|=bit;String line=alone?FallowTexts.OTIS_ALONE:FallowTexts.OTIS_DIG;a.say("Otis",line);a.speaks(line);}
                return;
            }
            if(Fallow.busy(s,Fallow.DRIVER))return;
            Fallow.face(a,Direction.NORTH.toYRot());a.gesture(Fallow.WORK);
            if(k%OTIS_STROKE==0)stroke(l,s);
        }else if(st.digging){
            st.digging=false;a.gesture(0);
            var back=new ArrayList<>(TO_DIG);Collections.reverse(back);back.remove(0);back.add(FallowStrike.OTIS_REST);Fallow.send(s,Fallow.DRIVER,back);
        }
    }
    /** A reader's hand on the doors from the steps, or on the fall over them from the yard: a stroke, once the roof is cool. */
    static boolean dig(ServerPlayer p,ServerLevel l,Fallow.Site s,BlockPos rel,CompoundTag own){
        var st=s.strike;
        if(!st.began||st.open||!door(rel))return false;
        boolean from=cellar(s,p);
        if(since(s)/DAY<COOL_DAY){Fallow.cue(p,from?FallowTexts.HOT_DOORS:FallowTexts.HOT_HEAP);return true;}
        Integer last=st.struck.get(p.getUUID());
        if(last!=null&&s.tick-last<STROKE)return true;
        st.struck.put(p.getUUID(),s.tick);st.dugAt=s.tick;
        p.swing(InteractionHand.MAIN_HAND,true);
        String key=from?"Dug":"DugAbove";
        if(own.getInt(key)==0)Fallow.cue(p,from?FallowTexts.DIG_FIRST:FallowTexts.DIG_FIRST_ABOVE);
        own.putInt(key,own.getInt(key)+1);
        stroke(l,s);
        // The record the hand came with is the one kept after it: the stroke that opens the doors is marked on it too.
        if(st.open)own.putBoolean("DugOut",true);
        return true;
    }
    /** One of the doors' cells, where the fall lies or a cleared length shows. */
    static boolean door(BlockPos rel){return rel.getY()==0&&rel.getX()>=WELL_W&&rel.getX()<=WELL_E&&rel.getZ()<=WELL_S&&rel.getZ()>=WELL_N;}
    /** A stroke at the doors, by a reader (from below or from the yard) or by Otis: a thud, the dirt sifting down, and when enough are
     * done, a length of the doors clear; when all eight are, the doors give. */
    static void stroke(ServerLevel l,Fallow.Site s){
        var st=s.strike;st.dug++;
        var mid=Vec3.atLowerCornerOf(s.base).add(WELL_W+1,0,(WELL_S+WELL_N)/2.0+.5);
        l.playSound(null,mid.x,mid.y,mid.z,FallowRegistry.DIG.get(),SoundSource.BLOCKS,.8F,.9F+l.getRandom().nextFloat()*.2F);
        l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.COARSE_DIRT.defaultBlockState()),mid.x,mid.y-.3,mid.z,6,.6,.05,1.4,0);
        int cells=Math.min(CELLS,st.dug/PER_CELL);
        while(st.cleared<cells)clear(l,s,st.cleared++);
        if(st.cleared>=CELLS&&!st.open)give(l,s);
    }
    /** A length of the doors dug clear: the leaf under it shows again, and daylight comes through between its boards. */
    static void clear(ServerLevel l,Fallow.Site s,int i){
        int x=ORDER[i][0],z=ORDER[i][1];var at=s.base.offset(x,0,z);
        int seg=z==WELL_S?0:z==WELL_N?2:1;
        BuildBlocks.set(l,at,FallowBlock.of(FallowBlock.Kind.CELLAR,Direction.NORTH,x==WELL_W?seg:seg+3),Block.UPDATE_ALL);
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.COARSE_DIRT.defaultBlockState()),at.getX()+.5,at.getY()+.7,at.getZ()+.5,12,.35,.2,.35,.05);
        String cue=i==0?FallowTexts.CRACK:i==CELLS/2-1?FallowTexts.HALF:null;
        if(cue!=null)for(var p:Fallow.readers(l,s.base))if(cellar(s,p))Fallow.cue(p,cue);
    }
    /** The doors give and go back over on their hinges; whoever is below can climb out, and the five who are below come up after them. */
    static void give(ServerLevel l,Fallow.Site s){
        var st=s.strike;st.open=true;st.openAt=s.tick;
        for(int i=0;i<CELLS;i++){var at=s.base.offset(ORDER[i][0],0,ORDER[i][1]);if(!FallowBlock.is(l.getBlockState(at),FallowBlock.Kind.CELLAR))clear(l,s,i);}
        Fallow.cellarDoors(l,s.base,true);
        var mid=Vec3.atLowerCornerOf(s.base).add(WELL_W+1,.5,(WELL_S+WELL_N)/2.0+.5);
        l.playSound(null,mid.x,mid.y,mid.z,FallowRegistry.GIVE.get(),SoundSource.BLOCKS,1.2F,1F);
        l.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,mid.x,mid.y+.5,mid.z,10,.8,.4,1.6,.01);
        l.sendParticles(ParticleTypes.WHITE_ASH,mid.x,mid.y,mid.z,60,1.2,1,2,.02);
        for(var p:Fallow.readers(l,s.base)){
            Fallow.cue(p,cellar(s,p)?FallowTexts.GIVE:FallowTexts.GIVE_ABOVE);
            var own=Fallow.own(p);if(own.getInt("Dug")+own.getInt("DugAbove")>0){own.putBoolean("DugOut",true);Fallow.save(p,own);}
        }
        var otis=Fallow.find(l,s.base,Fallow.DRIVER);
        if(otis!=null){otis.gesture(0);otis.say("Otis",FallowTexts.OTIS_GIVE);otis.speaks(FallowTexts.OTIS_GIVE);}
        st.digging=false;
    }
    /** After the doors give: the five who were below come up the steps one after another and stand in the yard. */
    static void up(ServerLevel l,Fallow.Site s,int k){
        var st=s.strike;int after=s.tick-st.openAt;
        int[][] order={{Fallow.DRIVER,40},{Fallow.GIRL,90},{Fallow.WOMAN,130},{Fallow.KEEPER,170}};
        for(var o:order){
            int who=o[0];if(!down(s,who)||(st.cameUp&1<<who)!=0||after<o[1])continue;
            var a=Fallow.find(l,s.base,who);if(a==null)continue;
            st.cameUp|=1<<who;a.gesture(0);
            var route=new ArrayList<Vec3>();
            if(who==Fallow.DRIVER&&a.position().distanceToSqr(s.world(DIG_AT))<2.25)route.addAll(UP.subList(4,UP.size()));else route.addAll(UP);
            route.add(switch(who){case Fallow.DRIVER->OTIS_UP;case Fallow.GIRL->OPAL_UP;case Fallow.WOMAN->FAYE_UP;default->HARLAN_UP;});
            Fallow.send(s,who,route);s.speed.put(who,who==Fallow.GIRL?.08:.09);
        }
        // Up, each stops where they came to: Otis looking out at the road, Opal sat by the steps with her bean, Faye by her, Harlan at his store.
        for(var o:order){
            int who=o[0];if((st.cameUp&1<<who)==0||(st.stood&1<<who)!=0)continue;
            var a=Fallow.find(l,s.base,who);if(a==null||Fallow.busy(s,who))continue;
            st.stood|=1<<who;
            switch(who){
                case Fallow.DRIVER->{Fallow.faceTo(a,s.world(new Vec3(12.5,0,-20.5)));say(a,"Otis",FallowTexts.OTIS_LOOKS);}
                case Fallow.GIRL->{
                    Fallow.faceTo(a,s.world(new Vec3(-13.5,0,-8.5)));a.gesture(Fallow.SIT);st.satAt=s.tick;
                    // The bean comes up with her, in her hands; the cup is set down in the ash by the steps in the fifth part.
                    a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(FallowRegistry.BEAN_CUP.get()));
                    var bs=l.getBlockState(s.base.offset(BEAN_BELOW));if(FallowBlock.is(bs,FallowBlock.Kind.BEAN)){st.beanStage=bs.getValue(FallowBlock.STAGE);l.setBlock(s.base.offset(BEAN_BELOW),Blocks.AIR.defaultBlockState(),3);}
                    say(a,"Opal",FallowTexts.OPAL_UP);
                }
                case Fallow.WOMAN->{Fallow.faceTo(a,s.world(new Vec3(16.5,0,-19.5)));say(a,"Faye",FallowTexts.FAYE_UP_LINE);}
                default->{Fallow.faceTo(a,s.world(new Vec3(0,0,-6)));say(a,"Harlan",FallowTexts.HARLAN_UP_LINE);}
            }
        }
    }
    static void say(LiteraryActor a,String who,String line){a.say(who,line);a.speaks(line);}

    // ------------------------------------------------------------------------------------------------ each reader
    /** What each reader below keeps: the days they were below; and once they are out of the cellar after the doors give, that they came up. */
    static void reader(Fallow.Site s,ServerPlayer p,int day){
        if(p.isSpectator())return;var st=s.strike;
        boolean in=cellar(s,p);
        if(in&&!st.open){
            if(st.buried.add(p.getUUID())||s.tick%100==0){var own=Fallow.own(p);if(own.getInt("Days")<day+1){own.putInt("Days",day+1);Fallow.save(p,own);}}
            return;
        }
        if(st.open&&!in&&st.buried.contains(p.getUUID())&&st.out.add(p.getUUID())){
            var own=Fallow.own(p);own.putBoolean("CameUp",true);Fallow.save(p,own);Fallow.cue(p,FallowTexts.CAME_UP);
        }
    }

    // ------------------------------------------------------------------------------------------------ a reader's hand
    /** A hand on something below in the buried days: the lantern, the roof, the crock, the shelves, the doors. True if it was answered here. */
    static boolean click(ServerPlayer p,ServerLevel l,Fallow.Site s,BlockPos rel,BlockState state,CompoundTag own){
        var st=s.strike;if(!st.began)return false;
        if(dig(p,l,s,rel,own))return true;
        int day=since(s)/DAY;
        if(rel.equals(LANTERN)&&FallowBlock.is(state,FallowBlock.Kind.CROCK)&&!st.open){turn(p,l,s,own);return true;}
        if(roof(rel,state)){Fallow.cue(p,FallowTexts.CEILING[Math.min(day,FallowTexts.CEILING.length-1)]);return true;}
        if(FallowBlock.is(state,FallowBlock.Kind.CROCK)&&state.getValue(FallowBlock.STAGE)==0){Fallow.cue(p,FallowTexts.CROCK_DAYS[Math.min(day,FallowTexts.CROCK_DAYS.length-1)]);return true;}
        if(FallowBlock.is(state,FallowBlock.Kind.SHELF)){Fallow.cue(p,FallowTexts.SHELF_DAYS[Math.min(day,FallowTexts.SHELF_DAYS.length-1)]);return true;}
        return false;
    }
    /** The cellar's roof, overhead in the room. */
    static boolean roof(BlockPos rel,BlockState state){
        return rel.getY()==-1&&rel.getX()>CELLAR_W&&rel.getX()<CELLAR_E&&rel.getZ()<CELLAR_S&&rel.getZ()>CELLAR_N&&state.is(FallowRegistry.material("cellar_roof"));
    }
    /** A word with one of the five below; null where the buried days have nothing of their own to say. */
    static @Nullable String talk(ServerPlayer p,ServerLevel l,Fallow.Site s,int who,CompoundTag own){
        var st=s.strike;if(!st.began)return null;
        int day=since(s)/DAY,heard=own.getInt("HeardBelow"+who);own.putInt("HeardBelow"+who,heard+1);
        if(st.open)return FallowTexts.upTalk(who);
        return switch(who){
            case Fallow.DRIVER->day<COOL_DAY?FallowTexts.OTIS_WAIT[heard%FallowTexts.OTIS_WAIT.length]:FallowTexts.OTIS_PUSH;
            case Fallow.GIRL->{var bs=l.getBlockState(s.base.offset(BEAN_BELOW));int stage=FallowBlock.is(bs,FallowBlock.Kind.BEAN)?bs.getValue(FallowBlock.STAGE):0;yield FallowTexts.OPAL_BELOW[Math.min(stage,FallowTexts.OPAL_BELOW.length-1)];}
            case Fallow.KEEPER->FallowTexts.HARLAN_BELOW[heard%FallowTexts.HARLAN_BELOW.length];
            case Fallow.WOMAN->FallowTexts.FAYE_BELOW[heard%FallowTexts.FAYE_BELOW.length];
            default->null;
        };
    }

    // ------------------------------------------------------------------------------------------------ helpers
    static int since(Fallow.Site s){return s.strike.since(s.tick)-FallowStrike.LIGHT-SETTLE;}
    static boolean cellar(Fallow.Site s,ServerPlayer p){return !p.isSpectator()&&FallowSite.inCellar(FallowStrike.rel(s,p));}
    static void play(ServerLevel l,Fallow.Site s,SoundEvent e,float volume){var at=s.base.offset(LANTERN);l.playSound(null,at,e,SoundSource.BLOCKS,volume,1F);}
    /** The afternoon a reader is at, if any. */
    static @Nullable Fallow.Site of(ServerPlayer p){
        for(var s:Fallow.ACTIVE.values())if(FallowSite.inScene(FallowStrike.rel(s,p)))return s;
        return null;
    }
    /** Whether a station is buried with readers' days still running below: it is kept a while if nobody is at it. */
    static boolean holding(Fallow.Site s){return s.strike.began&&!s.strike.open&&!s.strike.buried.isEmpty();}

    // ------------------------------------------------------------------------------------------------ for tests
    public static int day(BlockPos b){var s=Fallow.afternoon(b);return s==null||!s.strike.began?-1:since(s)/DAY;}
    public static int oil(BlockPos b){var s=Fallow.afternoon(b);return s==null?-1:s.strike.oil;}
    public static int dug(BlockPos b){var s=Fallow.afternoon(b);return s==null?-1:s.strike.dug;}
    public static boolean open(BlockPos b){var s=Fallow.afternoon(b);return s!=null&&s.strike.open;}
    public static boolean digging(BlockPos b){var s=Fallow.afternoon(b);return s!=null&&s.strike.digging;}
    public static boolean down(BlockPos b,int who){var s=Fallow.afternoon(b);return s!=null&&down(s,who);}
    /** The clock set on to so many ticks into the buried days. */
    public static void skip(BlockPos b,int k){var s=Fallow.afternoon(b);if(s!=null&&s.strike.toneAt>=0)s.tick=Math.max(s.tick,s.strike.toneAt+FallowStrike.LIGHT+SETTLE+k);}
    /** As if the lantern had stood dark so long. */
    public static void dark(BlockPos b,int ticks){var s=Fallow.afternoon(b);if(s!=null)s.strike.dark=Math.max(s.strike.dark,ticks);}
    /** As if the doors had had so many strokes, short of the next length coming clear. */
    public static void dugTo(BlockPos b,int strokes){var s=Fallow.afternoon(b);if(s!=null)s.strike.dug=Math.max(s.strike.dug,strokes);}
}
