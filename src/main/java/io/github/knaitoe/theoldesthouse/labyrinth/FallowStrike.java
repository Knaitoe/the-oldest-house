package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.FallowStrikePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.674, Fallow, the third part's first half (docs/FALLOW_0_4_674.md): the four minutes. Fifteen seconds of the afternoon's clock after
 * the man on the road is gone, the ball game on the radio stops for a long tone and the television shows a blank card. Boyd laughs it
 * off; Harlan does not. Fifteen seconds on, out in the wheat both pads' lids grind open, and from then nobody leaves by the House's door.
 * Three seconds later the first goes up out of its hole on a column of fire, and the second, and Harlan sends everybody to the cellar.
 *
 * <p>Then four minutes, and the light. Harlan runs to the cellar's doors and holds them open, calling; he goes down only if a reader
 * takes the doors from him, and otherwise shuts them over the people below and stays out. Otis goes for Opal, who picks up her bean, and
 * carries her down. Faye runs to the car up on the lift and works at the lever to bring it down; a reader who takes her by the arm leads
 * her, and when that reader is at the cellar she goes down to Opal. Boyd goes to the rig's door and will not be talked into a hole in
 * the ground. Forty ticks before the light the doors are pulled shut; a reader still outside can open them and go down, and they come
 * down again behind. Below, Otis and Opal talk, and at the last she counts.
 *
 * <p>At the light, each reader keeps where they were (AtLight: outside or cellar) and what they did (HeldDoors, BroughtFaye). A reader
 * out in it is whited out and, a moment later, back out by the House's door: the House's faint, not a death. A reader below sees it
 * come in round the doors, the ground shakes and the dust comes down, and after a while the dark takes them too. What comes after is
 * the next half's (shadows on the wall, the station falling in) and the fourth part's (buried). Spectators see it and earn nothing.
 */
public final class FallowStrike {
    private FallowStrike(){}
    /** From the man's going to the tone; then, from the tone, the lids, each launch, the doors pulled shut and the light, in ticks of the
     * afternoon's clock; how long a reader out in the light stands before the faint, and how long one below waits in the dark. */
    public static final int AFTER_MAN=300,LIDS=300,LAUNCH_A=LIDS+60,LAUNCH_B=LIDS+90,GO=LAUNCH_A+70,CLOCK=4800,LIGHT=LIDS+CLOCK,SHUT=LIGHT-40,
        OUT_FAINT=40,BELOW_FAINT=200;
    /** The lids slide a column every this many ticks. */
    public static final int SLIDE_EVERY=13;
    /** Harlan's four minutes: running to the doors, holding them, going down, below. */
    public static final int TO_DOORS=1,HOLDING=2,GOING_DOWN=3,BELOW=4;
    /** Faye's: running to the car, at its lever, led by a reader, going down, below. */
    public static final int TO_CAR=1,AT_CAR=2,LED=3,FAYE_DOWN=4,FAYE_BELOW=5;
    /** Otis's: going for Opal, lifting her, carrying her down, below. */
    public static final int TO_OPAL=1,LIFTING=2,CARRYING=3,OTIS_BELOW=4;
    /** Boyd's: going to the rig, at its door. */
    public static final int TO_RIG=1,AT_RIG=2;
    /** How fast they run; how fast Otis goes with her in his arms. */
    static final double RUN=.2,CARRY_PACE=.16,LEAD_PACE=.22;

    /** Each station's four minutes, kept on its afternoon (Fallow.Site.strike). */
    static final class State{
        int goneAt=-1,toneAt=-1,slid,harlan,faye,otis,boyd,liftAt,calls,callAt,talk,talkAt=-1,count,boydAt,openedAt=-1,fayeStill;
        /** 0.4.680: which parts of the station the light has had this afternoon (FallowRuin). */
        int ruined;
        boolean padsWhole,shut;@Nullable UUID led,heldBy;
        final Set<UUID> done=new HashSet<>();final Map<UUID,Integer> faint=new HashMap<>();
        int since(int tick){return toneAt<0?-1:tick-toneAt;}
    }

    // ------------------------------------------------------------------------------------------------ where
    /** The top of the cellar's steps, in the yard; where Harlan holds the doors, beside them; down the steps into the room. */
    static final Vec3 DOOR_TOP=new Vec3(-13.5,0,-6.5),HOLD_AT=new Vec3(-11.5,0,-9.5),YARD_SIDE=new Vec3(-10.5,0,-6.5),FRONT=new Vec3(-10.5,0,-13.5);
    static final List<Vec3> STEPS=List.of(new Vec3(-13.5,0,-7.5),new Vec3(-13.5,-1,-8.5),new Vec3(-13.5,-2,-9.5),new Vec3(-13.5,-3,-10.5),new Vec3(-13.5,-3,-11.5));
    /** Where each comes to rest below: Opal by the cot, Otis at the far end, Harlan by the shelves near the steps, Faye beside Opal. */
    public static final Vec3 OPAL_BELOW=new Vec3(-11.5,-3,-14.5),OTIS_REST=new Vec3(-12.5,-3,-16.5),HARLAN_REST=new Vec3(-15.5,-3,-12.5),
        FAYE_REST=new Vec3(-11.5,-3,-15.5);
    /** Faye at the lift's lever; Boyd at the rig's door; Otis beside Opal's step. */
    public static final Vec3 LEVER=new Vec3(16.5,0,-9.5),RIG_DOOR=new Vec3(15.5,0,-19.5),BY_OPAL=new Vec3(3.5,0,-13.5);

    /** The way down to the cellar from wherever someone is: out of the garage by its bay or the store by its front door, along the front
     * to the yard, round to the top of the steps and down them, then to their own place below. */
    static List<Vec3> toCellar(Vec3 r,Vec3 rest){
        var out=new ArrayList<Vec3>();
        if(!FallowSite.inCellar(r)){
            if(FallowSite.inGarage(r)){out.add(new Vec3(12.5,0,-9.5));out.add(new Vec3(12.5,0,-13.5));}
            else if(FallowSite.inStore(r)){out.add(new Vec3(.5,0,-10.5));out.add(new Vec3(.5,0,-13.5));}
            boolean yard=r.x<FallowSite.STORE_W-.5&&r.z>FallowSite.YARD_N+.5;
            if(!yard)out.add(FRONT);
            out.add(YARD_SIDE);out.add(DOOR_TOP);out.addAll(STEPS);
        }
        out.add(rest);return out;
    }

    /** Over the doors' curb and down the well, a body walks straight, a tread at a time (Fallow.walk): the footpath search, planning from
     * a body that had just stepped up onto the curb, found its way along the curb rather than down the steps. True once there, false
     * while going, null if this leg is not down the well (from the top of the steps into it, or on down it). */
    static @Nullable Boolean steps(ServerLevel l,Fallow.Site s,LiteraryActor a,Vec3 target,double speed){
        var base=Vec3.atLowerCornerOf(s.base);var from=a.position().subtract(base);var to=target.subtract(base);
        if(!well(to,FallowSite.WELL_S+1)||!well(from,FallowSite.WELL_S+1.6))return null;
        var flat=new Vec3(target.x-a.getX(),0,target.z-a.getZ());double far=flat.length();
        var next=far<=speed?new Vec3(target.x,a.getY(),target.z):a.position().add(flat.scale(speed/far));
        // Each foot on its tread: the highest thing to stand on under the body, no more than a curb above it or a step below.
        double floor=Double.NEGATIVE_INFINITY;
        for(var shape:l.getBlockCollisions(a,new AABB(next.x-.25,a.getY()-1.1,next.z-.25,next.x+.25,a.getY()+.6,next.z+.25)))
            for(var part:shape.toAabbs())if(part.maxY<=a.getY()+.6)floor=Math.max(floor,part.maxY);
        double y=Double.isFinite(floor)?floor:Mth.lerp(far<=speed?1:speed/far,a.getY(),target.y);
        float yaw=far>.001?(float)Math.toDegrees(Math.atan2(-flat.x,flat.z)):a.getYRot();
        a.moveTo(next.x,y,next.z,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.setDeltaMovement(Vec3.ZERO);a.setOnGround(true);
        a.walkAnimation.update((float)Math.min(far,speed)*4,.4F);
        return far<=speed;
    }
    /** Over the well, between the doors' curbs: from the top of the steps (south of this) to the landing at its foot. */
    static boolean well(Vec3 r,double south){return r.x>FallowSite.WELL_W-.2&&r.x<FallowSite.WELL_E+1.2&&r.z<south&&r.z>FallowSite.WELL_N-.2&&r.y<.6;}

    // ------------------------------------------------------------------------------------------------ the clock
    static void tick(ServerLevel l,Fallow.Site s,List<ServerPlayer> here){
        var st=s.strike;
        if(st.toneAt<0){
            // The pads are whole before anything happens; a fresh afternoon puts back a lid that was left slid.
            if(!st.padsWhole&&s.tick%40==0)st.padsWhole=FallowSite.lid(l,s.base,FallowSite.PAD_A,0)&FallowSite.lid(l,s.base,FallowSite.PAD_B,0);
            if(s.stranger==Fallow.GONE&&st.goneAt<0)st.goneAt=s.tick;
            if(st.goneAt<0||s.tick-st.goneAt<AFTER_MAN)return;
            begin(l,s,here);
        }
        int t=st.since(s.tick);
        if(t%20==0||t==LAUNCH_A||t==LAUNCH_B||t==LIGHT)send(l,s,t);
        tone(l,s,here,t);
        if(t>=LIDS)lids(l,s,here,t);
        if(t==LAUNCH_A||t==LAUNCH_B)launch(l,s,here,t==LAUNCH_A?FallowSite.PAD_A:FallowSite.PAD_B,t==LAUNCH_A);
        if(t==LAUNCH_A+30){var h=Fallow.find(l,s.base,Fallow.KEEPER);if(h!=null){h.say("Harlan",FallowTexts.GOING_UP);h.speaks(FallowTexts.GOING_UP);}}
        if(t==LAUNCH_B+80||t>LAUNCH_B+80&&t<LIGHT&&(t-LAUNCH_B-80)%360==0)for(var p:here){toward(p,p.position().add(60,4,-10),FallowRegistry.SIREN.get(),.9F,1F);if(t==LAUNCH_B+80)Fallow.cue(p,FallowTexts.SIREN);}
        if(t==GO)go(l,s);
        if(t>GO&&t<=LIGHT+BELOW_FAINT){harlan(l,s,here,t);otis(l,s,t);faye(l,s,here,t);boyd(l,s,t);}
        if(t>=GO)below(l,s,here,t);
        if(t>=SHUT)doors(l,s,here,t);
        if(t>=LIGHT){light(l,s,here,t);FallowRuin.tick(l,s,here,t-LIGHT);}
    }
    /** The tone: the television to its card, the people at the station laid off their afternoon. */
    static void begin(ServerLevel l,Fallow.Site s,List<ServerPlayer> here){
        var st=s.strike;st.toneAt=s.tick;
        s.keeper=Fallow.STRIKE;if(s.bean==Fallow.SET)s.bean=Fallow.TAKEN;
        tv(l,s.base,FallowBlock.CARD);
        for(var p:here)if(FallowSite.inStore(rel(s,p))||FallowSite.inGarage(rel(s,p)))Fallow.cue(p,FallowTexts.TONE);
    }
    /** The tone keeps on out of the radio until the light; the voice under it once; Boyd's laugh and Harlan's answer. */
    static void tone(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int t){
        if(t<LIGHT&&t%90==0)l.playSound(null,s.base.offset(FallowSite.RADIO),FallowRegistry.TONE.get(),SoundSource.BLOCKS,.8F,1F);
        if(t==120)for(var p:here)if(FallowSite.inStore(rel(s,p))||FallowSite.inGarage(rel(s,p)))Fallow.cue(p,FallowTexts.TONE_VOICE);
        if(t==60){var a=Fallow.find(l,s.base,Fallow.MAN);if(a!=null){a.say("Boyd",FallowTexts.BOYD_TEST);a.speaks(FallowTexts.BOYD_TEST);}}
        if(t==110){var a=Fallow.find(l,s.base,Fallow.KEEPER);if(a!=null){a.say("Harlan",FallowTexts.HARLAN_NOT_TEST);a.speaks(FallowTexts.HARLAN_NOT_TEST);}}
    }
    /** The lids grind open, both at once, a column at a time; from now nobody leaves by the House's door. */
    static void lids(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int t){
        var st=s.strike;
        if(t==LIDS){
            for(var p:here){
                for(var pad:new BlockPos[]{FallowSite.PAD_A,FallowSite.PAD_B})toward(p,Vec3.atCenterOf(s.base.offset(pad)),FallowRegistry.LIDS.get(),1.2F,1F);
                var r=rel(s,p);Fallow.cue(p,FallowSite.inStore(r)||FallowSite.inGarage(r)||FallowSite.inCellar(r)?FallowTexts.LIDS_INSIDE:FallowTexts.LIDS);
            }
            st.padsWhole=false;
        }
        if(t==LIDS+20){var a=Fallow.find(l,s.base,Fallow.DRIVER);if(a!=null){a.say("Otis",FallowTexts.OTIS_PADS);a.speaks(FallowTexts.OTIS_PADS);}}
        if(t==LIDS+34){var a=Fallow.find(l,s.base,Fallow.GIRL);if(a!=null){a.say("Opal",FallowTexts.OPAL_HUM);a.speaks(FallowTexts.OPAL_HUM);}}
        int want=Math.min(FallowSite.LID_SLIDE,(t-LIDS)/SLIDE_EVERY);
        if(want>st.slid&&FallowSite.lid(l,s.base,FallowSite.PAD_A,want)&FallowSite.lid(l,s.base,FallowSite.PAD_B,want))st.slid=want;
    }
    /** One goes up: the roar to every reader, from the pad's way; the first one's line, the second's. */
    static void launch(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,BlockPos pad,boolean first){
        var at=Vec3.atCenterOf(s.base.offset(pad));
        for(var p:here){toward(p,at,FallowRegistry.LAUNCH.get(),1.6F,first?1F:.93F);Fallow.cue(p,first?FallowTexts.LAUNCH:FallowTexts.LAUNCH_TWO);}
        l.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,at.x,at.y,at.z,24,2.5,1,2.5,.02);
    }
    /** Everybody in the cellar: they each go to what they go to. */
    static void go(ServerLevel l,Fallow.Site s){
        var st=s.strike;
        var h=Fallow.find(l,s.base,Fallow.KEEPER);if(h!=null){h.say("Harlan",FallowTexts.TO_THE_CELLAR);h.speaks(FallowTexts.TO_THE_CELLAR);}
        for(int who=0;who<Fallow.PEOPLE;who++){s.away.add(who);var a=Fallow.find(l,s.base,who);if(a!=null){a.gesture(0);a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);}}
        st.harlan=TO_DOORS;run(l,s,Fallow.KEEPER,HOLD_AT,RUN);
        st.otis=TO_OPAL;run(l,s,Fallow.DRIVER,BY_OPAL,RUN);
        st.faye=TO_CAR;run(l,s,Fallow.WOMAN,LEVER,RUN);
        st.boyd=TO_RIG;run(l,s,Fallow.MAN,RIG_DOOR,RUN);
    }
    /** Someone sent running to a place: out of the garage or the store first if they are in one, then straight there. */
    static void run(ServerLevel l,Fallow.Site s,int who,Vec3 to,double pace){
        var a=Fallow.find(l,s.base,who);var route=new ArrayList<Vec3>();
        if(a!=null){
            var r=a.position().subtract(Vec3.atLowerCornerOf(s.base));
            boolean garage=to.x>FallowSite.STORE_E+.5&&to.z>FallowSite.STORE_N+.5;
            if(FallowSite.inGarage(r)&&!garage){route.add(new Vec3(12.5,0,-9.5));route.add(new Vec3(12.5,0,-13.5));}
            else if(FallowSite.inStore(r)&&garage){route.add(new Vec3(6.5,0,-6.5));route.add(new Vec3(7.5,0,-4.5));route.add(new Vec3(9.5,0,-4.5));route.add(new Vec3(13.5,0,-7.5));}
            else if(FallowSite.inStore(r)){route.add(new Vec3(.5,0,-10.5));route.add(new Vec3(.5,0,-13.5));}
            if(to==HOLD_AT&&!(r.x<FallowSite.STORE_W-.5&&r.z>FallowSite.YARD_N+.5))route.add(FRONT);
            if(to==RIG_DOOR){route.add(new Vec3(16.5,0,-17.5));route.add(new Vec3(16.5,0,-19.5));}
        }
        route.add(to);Fallow.send(s,who,route);s.speed.put(who,pace);
    }

    // ------------------------------------------------------------------------------------------------ each of them
    /** Harlan to the doors, open them, hold them and call; down only when a reader takes them; otherwise he is out at the light. */
    static void harlan(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int t){
        var st=s.strike;var a=Fallow.find(l,s.base,Fallow.KEEPER);if(a==null)return;
        switch(st.harlan){
            case TO_DOORS->{if(!Fallow.busy(s,Fallow.KEEPER)){st.harlan=HOLDING;st.callAt=t+40;hold(l,s,a);}}
            case HOLDING->{
                if(t>=SHUT)return;
                // A reader who has let the doors down finds them thrown back.
                if(!Fallow.cellarOpen(l,s.base)){Fallow.cellarDoors(l,s.base,true);for(var p:here)if(p.distanceToSqr(s.world(HOLD_AT))<64)Fallow.cue(p,FallowTexts.THROWN_BACK);}
                Fallow.faceTo(a,s.world(DOOR_TOP).add(-1,0,-2));a.gesture(Fallow.REACH);
                if(t>=st.callAt){
                    String line=FallowTexts.CALLING[st.calls%FallowTexts.CALLING.length];st.calls++;
                    if(line==FallowTexts.CALLING[1]&&st.faye>=LED||line==FallowTexts.CALLING[2]&&st.boyd==0)line=FallowTexts.CALLING[3];
                    a.say("Harlan",line);a.speaks(line);st.callAt=t+150;
                }
            }
            case GOING_DOWN->{if(!Fallow.busy(s,Fallow.KEEPER)){st.harlan=BELOW;Fallow.faceTo(a,s.world(OPAL_BELOW));a.gesture(0);}}
            default->{}
        }
    }
    static void hold(ServerLevel l,Fallow.Site s,LiteraryActor a){if(!Fallow.cellarOpen(l,s.base))Fallow.cellarDoors(l,s.base,true);Fallow.faceTo(a,s.world(DOOR_TOP));a.gesture(Fallow.REACH);}
    /** Otis to Opal: she picks up her bean, he lifts her and carries her down, and sets her by the cot. */
    static void otis(ServerLevel l,Fallow.Site s,int t){
        var st=s.strike;var a=Fallow.find(l,s.base,Fallow.DRIVER);var girl=Fallow.find(l,s.base,Fallow.GIRL);if(a==null||girl==null)return;
        switch(st.otis){
            case TO_OPAL->{
                if(Fallow.busy(s,Fallow.DRIVER))return;
                st.otis=LIFTING;st.liftAt=t;Fallow.faceTo(a,girl.position());Fallow.faceTo(girl,a.position());
                a.say("Otis",FallowTexts.OTIS_COMING);a.speaks(FallowTexts.OTIS_COMING);
            }
            case LIFTING->{
                int k=t-st.liftAt;
                if(k==20){
                    // She takes her bean up off the forecourt in both hands, and gets up.
                    var at=s.base.offset(Fallow.BEAN_AT);if(FallowBlock.is(l.getBlockState(at),FallowBlock.Kind.BEAN))l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
                    girl.gesture(Fallow.CUP);girl.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(FallowRegistry.BEAN_CUP.get()));
                    girl.say("Opal",FallowTexts.OPAL_CUP);girl.speaks(FallowTexts.OPAL_CUP);
                }
                if(k==40){
                    a.gesture(Fallow.CARRY);
                    if(girl.startRiding(a,true)){a.say("Otis",FallowTexts.OTIS_HOLD);a.speaks(FallowTexts.OTIS_HOLD);}
                    st.otis=CARRYING;Fallow.send(s,Fallow.DRIVER,toCellar(a.position().subtract(Vec3.atLowerCornerOf(s.base)),new Vec3(-12.5,-3,-14.5)));s.speed.put(Fallow.DRIVER,CARRY_PACE);
                }
            }
            case CARRYING->{
                if(!girl.isPassenger())girl.startRiding(a,true);
                if(Fallow.busy(s,Fallow.DRIVER))return;
                girl.stopRiding();var at=s.world(OPAL_BELOW);girl.moveTo(at.x,at.y,at.z,a.getYRot(),0);girl.gesture(Fallow.CUP);
                a.gesture(0);a.say("Otis",FallowTexts.OTIS_DOWN);a.speaks(FallowTexts.OTIS_DOWN);
                st.otis=OTIS_BELOW;Fallow.send(s,Fallow.DRIVER,List.of(OTIS_REST));s.speed.put(Fallow.DRIVER,.11);st.talkAt=t+260;
            }
            case OTIS_BELOW->{if(!Fallow.busy(s,Fallow.DRIVER)&&t%40==0)Fallow.faceTo(a,girl.position());}
            default->{}
        }
    }
    /** Faye at the lift's lever until a reader takes her by the arm; then after them, and down to Opal once they are at the cellar. */
    static void faye(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int t){
        var st=s.strike;var a=Fallow.find(l,s.base,Fallow.WOMAN);if(a==null)return;
        switch(st.faye){
            case TO_CAR->{if(!Fallow.busy(s,Fallow.WOMAN)){st.faye=AT_CAR;Fallow.face(a,net.minecraft.core.Direction.EAST.toYRot());a.gesture(Fallow.REACH);a.say("Faye",FallowTexts.FAYE_LIFT);a.speaks(FallowTexts.FAYE_LIFT);}}
            case AT_CAR->{if(t%300==0){a.say("Faye",FallowTexts.FAYE_STAYS);a.speaks(FallowTexts.FAYE_STAYS);}}
            case LED->{
                ServerPlayer p=null;for(var q:here)if(q.getUUID().equals(st.led))p=q;
                if(p==null){st.faye=TO_CAR;st.led=null;run(l,s,Fallow.WOMAN,LEVER,RUN);return;}
                var r=rel(s,p);
                if(FallowSite.inCellar(r)||r.distanceToSqr(DOOR_TOP)<3.5*3.5){
                    st.faye=FAYE_DOWN;Fallow.send(s,Fallow.WOMAN,toCellar(a.position().subtract(Vec3.atLowerCornerOf(s.base)),FAYE_REST));s.speed.put(Fallow.WOMAN,RUN);return;
                }
                if(a.distanceToSqr(p)<2.4*2.4){Fallow.faceTo(a,p.position());st.fayeStill=0;return;}
                var before=a.position();
                SceneFootpaths.walk(a,p.position(),LEAD_PACE);
                // Caught on something where nobody sees her: she is at the reader's back.
                st.fayeStill=a.position().distanceToSqr(before)<1.0E-4?st.fayeStill+1:0;
                if(st.fayeStill>=60&&!Drawing.seen(here,a.position())){var back=p.position().subtract(p.getLookAngle().multiply(1.5,0,1.5));a.moveTo(back.x,p.getY(),back.z,a.getYRot(),0);st.fayeStill=0;}
            }
            case FAYE_DOWN->{
                if(Fallow.busy(s,Fallow.WOMAN))return;
                st.faye=FAYE_BELOW;var girl=Fallow.find(l,s.base,Fallow.GIRL);if(girl!=null)Fallow.faceTo(a,girl.position());a.gesture(0);
                a.say("Faye",FallowTexts.FAYE_FOUND);a.speaks(FallowTexts.FAYE_FOUND);
                for(var p:here)if(p.getUUID().equals(st.led)){var own=Fallow.own(p);own.putBoolean("BroughtFaye",true);Fallow.save(p,own);}
            }
            default->{}
        }
    }
    /** Boyd to the rig's door, where he stays, pulling at it and shouting. */
    static void boyd(ServerLevel l,Fallow.Site s,int t){
        var st=s.strike;var a=Fallow.find(l,s.base,Fallow.MAN);if(a==null)return;
        if(st.boyd==TO_RIG&&!Fallow.busy(s,Fallow.MAN)){st.boyd=AT_RIG;st.boydAt=t;Fallow.face(a,net.minecraft.core.Direction.NORTH.toYRot());a.gesture(Fallow.REACH);}
        if(st.boyd!=AT_RIG||t>=LIGHT)return;
        int k=t-st.boydAt;
        if(k==20){String line=st.faye<LED?FallowTexts.BOYD_FAYE:FallowTexts.BOYD[0];a.say("Boyd",line);a.speaks(line);}
        else if(k>20&&k%240==0){String line=FallowTexts.BOYD[(k/240)%FallowTexts.BOYD.length];a.say("Boyd",line);a.speaks(line);}
    }
    /** Below, in the dark, while they wait: a word every so often, then at the last Opal asks to count, and counts. */
    static void below(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int t){
        var st=s.strike;if(st.otis!=OTIS_BELOW||t>=LIGHT)return;
        String who=null,line=null;
        if(t==LIGHT-200){who="Opal";line=FallowTexts.WAITING[7][1];}
        else if(t==LIGHT-170){who="Otis";line=FallowTexts.WAITING[8][1];}
        else if(t>=LIGHT-140&&(LIGHT-140-t)%20==0){int n=(t-(LIGHT-140))/20;if(n<FallowTexts.COUNTING.length){who="Opal";line=FallowTexts.COUNTING[n];st.count=n+1;}}
        else if(st.talkAt>=0&&t>=st.talkAt&&t<LIGHT-230){
            while(st.talk<7){
                var said=FallowTexts.WAITING[st.talk++];
                if(said[0].equals("Harlan")&&st.harlan!=BELOW)continue;
                who=said[0];line=said[1];break;
            }
            st.talkAt=t+300;
        }
        if(line==null)return;
        int idx=who.equals("Opal")?Fallow.GIRL:who.equals("Otis")?Fallow.DRIVER:Fallow.KEEPER;
        var a=Fallow.find(l,s.base,idx);if(a!=null){a.say(who,line);a.speaks(line);}
    }
    /** Forty ticks before the light the doors are pulled shut: from below by Harlan or Otis, or by Harlan from outside, over them. A reader
     * outside can open them again and go down; they come down again behind whoever has. */
    static void doors(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int t){
        var st=s.strike;
        if(!st.shut){
            // Nobody is shut in under them: they come down once nobody stands on the upper steps, at the light at the latest.
            if(underDoors(l,s,here)&&t<LIGHT)return;
            st.shut=true;Fallow.cellarDoors(l,s.base,false);
            var h=Fallow.find(l,s.base,Fallow.KEEPER);
            if(st.harlan==HOLDING&&h!=null){h.say("Harlan",FallowTexts.SHUT_OVER);h.speaks(FallowTexts.SHUT_OVER);h.gesture(0);}
            l.playSound(null,s.base.offset(FallowSite.WELL_W,0,FallowSite.WELL_S-1),net.minecraft.sounds.SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,1F,.6F);
            for(var p:here){
                var r=rel(s,p);
                if(FallowSite.inCellar(r))Fallow.cue(p,st.harlan==BELOW?FallowTexts.SHUT_BELOW:st.harlan==HOLDING?FallowTexts.SHUT_OUT:FallowTexts.SHUT_BY_OTIS);
                else if(r.distanceToSqr(DOOR_TOP)<12*12)Fallow.cue(p,FallowTexts.SHUT_OUT);
            }
            return;
        }
        if(t>=LIGHT)return;
        if(!Fallow.cellarOpen(l,s.base)){st.openedAt=-1;return;}
        if(st.openedAt<0)st.openedAt=t;
        if(underDoors(l,s,here)||t-st.openedAt<30)return;
        Fallow.cellarDoors(l,s.base,false);st.openedAt=-1;
        l.playSound(null,s.base.offset(FallowSite.WELL_W,0,FallowSite.WELL_S-1),net.minecraft.sounds.SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,1F,.6F);
        for(var p:here)if(FallowSite.inCellar(rel(s,p)))Fallow.cue(p,FallowTexts.SHUT_AFTER);
    }
    /** Anybody, a reader or one of them, on the upper steps, where the doors would come down on their heads. */
    static boolean underDoors(ServerLevel l,Fallow.Site s,List<ServerPlayer> here){
        for(var p:here)if(!p.isSpectator()&&upper(rel(s,p)))return true;
        for(int who=0;who<Fallow.PEOPLE;who++){var a=Fallow.find(l,s.base,who);if(a!=null&&upper(a.position().subtract(Vec3.atLowerCornerOf(s.base))))return true;}
        return false;
    }
    /** A body (its feet at r, its width .6, its height 1.8) reaching up into where the shut leaves lie, over the well; beside the well,
     * where Harlan stands holding them, is not under them. */
    static boolean upper(Vec3 r){return r.x>FallowSite.WELL_W-.3&&r.x<FallowSite.WELL_E+1.3&&r.z<FallowSite.WELL_S+1.3&&r.z>FallowSite.WELL_N-.3&&r.y>-1.8&&r.y<.57;}
    /** The light: each reader keeps where it found them; out in it, the faint a moment after; below, the shaking, the dust and then the dark. */
    static void light(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int t){
        var st=s.strike;
        if(t==LIGHT)tv(l,s.base,FallowBlock.DARK);
        for(var p:here){
            if(st.done.contains(p.getUUID()))continue;
            st.done.add(p.getUUID());
            var r=rel(s,p);boolean below=FallowSite.inCellar(r);
            if(t<=LIGHT+1){
                var own=Fallow.own(p);
                own.putString("AtLight",below?"cellar":"outside");own.putInt("Strikes",own.getInt("Strikes")+1);
                own.putDouble("LightX",r.x);own.putDouble("LightY",r.y);own.putDouble("LightZ",r.z);own.putFloat("LightYaw",p.getYRot());
                if(p.getUUID().equals(st.heldBy))own.putBoolean("HeldDoors",true);
                // 0.4.680: out in front of the station, their shadow left on its front.
                int shadow=below?FallowRuin.NO_SHADOW:FallowRuin.shadow(l,s.base,p,r);
                if(shadow!=FallowRuin.NO_SHADOW)own.putInt("ShadowX",shadow);else own.remove("ShadowX");
                Fallow.save(p,own);
            }
            heard(p,FallowRegistry.BLAST.get(),below?.55F:1F);
            if(below){heard(p,FallowRegistry.RUMBLE.get(),1F);Fallow.cue(p,FallowTexts.CRACKS);}
            st.faint.put(p.getUUID(),s.tick+(below?BELOW_FAINT:OUT_FAINT));
        }
        if(t>LIGHT&&t<LIGHT+BELOW_FAINT){
            for(var p:here){
                if(!FallowSite.inCellar(rel(s,p)))continue;
                if(t%10==0)l.sendParticles(p,ParticleTypes.WHITE_ASH,true,p.getX(),p.getY()+2.2,p.getZ(),14,2.5,.1,2.5,.01);
                if(t==LIGHT+60)Fallow.cue(p,FallowTexts.DUST);
                if(t%60==0)heard(p,FallowRegistry.RUMBLE.get(),.7F);
            }
        }
        for(var p:here){
            Integer at=st.faint.get(p.getUUID());
            if(at!=null&&s.tick>=at&&LabyrinthDoors.sendBack(p,30,40,40,q->{}))st.faint.remove(p.getUUID());
        }
    }

    // ------------------------------------------------------------------------------------------------ a reader's hand
    /** A word with one of them during the four minutes; false before the tone, when the afternoon's own lines answer. */
    static boolean talk(ServerPlayer p,LiteraryActor a,@Nullable Fallow.Site s,int who,CompoundTag own){
        if(s==null||s.strike.toneAt<0)return false;
        var st=s.strike;int t=st.since(s.tick);
        String line;
        switch(who){
            case Fallow.KEEPER->line=st.harlan==HOLDING?FallowTexts.HARLAN_HOLDING:st.harlan==BELOW?FallowTexts.WAITING[1][1]:t<GO?FallowTexts.HARLAN_NOT_TEST:FallowTexts.TO_THE_CELLAR;
            case Fallow.WOMAN->{
                if(st.faye==TO_CAR||st.faye==AT_CAR){
                    // Taken by the arm: she turns from the car and comes.
                    st.faye=LED;st.led=p.getUUID();st.fayeStill=0;s.routes.remove(Fallow.WOMAN);a.gesture(0);
                    Fallow.cue(p,FallowTexts.FAYE_TAKEN);line=FallowTexts.FAYE_WHERE;
                }else line=st.faye>=FAYE_DOWN?FallowTexts.FAYE_FOUND:FallowTexts.FAYE_WHERE;
            }
            case Fallow.MAN->{int heard=own.getInt("HeardBoyd");own.putInt("HeardBoyd",heard+1);line=st.boyd==0?FallowTexts.BOYD_TEST:FallowTexts.BOYD[heard%FallowTexts.BOYD.length];}
            case Fallow.DRIVER->line=st.otis==OTIS_BELOW?FallowTexts.WAITING[3][1]:st.otis>=TO_OPAL?FallowTexts.OTIS_GO:FallowTexts.OTIS_PADS;
            case Fallow.GIRL->line=st.otis==CARRYING||st.otis==LIFTING?FallowTexts.OPAL_CUP:st.otis==OTIS_BELOW?FallowTexts.WAITING[2][1]:FallowTexts.OPAL_HUM;
            default->{return false;}
        }
        a.sayTo(p,FallowTexts.NAMES[who],line);a.speaks(line);
        return true;
    }
    /** A hand on the open cellar doors while Harlan holds them, near him: the reader takes them from him and he goes down. */
    static boolean takeDoors(ServerPlayer p,ServerLevel l,Fallow.Site s,boolean open,CompoundTag own){
        var st=s.strike;int t=st.since(s.tick);
        if(st.toneAt<0||!open||st.harlan!=HOLDING||t>=SHUT||p.distanceToSqr(s.world(HOLD_AT))>6*6)return false;
        var a=Fallow.find(l,s.base,Fallow.KEEPER);if(a==null)return false;
        st.heldBy=p.getUUID();st.harlan=GOING_DOWN;a.gesture(0);
        a.sayTo(p,"Harlan",FallowTexts.TAKE_DOORS);a.speaks(FallowTexts.TAKE_DOORS);Fallow.cue(p,FallowTexts.TAKEN_DOORS);
        own.putBoolean("HeldDoors",true);
        Fallow.send(s,Fallow.KEEPER,toCellar(a.position().subtract(Vec3.atLowerCornerOf(s.base)),HARLAN_REST));s.speed.put(Fallow.KEEPER,RUN);
        return true;
    }
    /** What the radio says to a hand while the tone is on. */
    static @Nullable String thing(@Nullable Fallow.Site s,FallowBlock.Kind kind,int stage){
        if(s==null||s.strike.toneAt<0||s.strike.since(s.tick)>=LIGHT)return null;
        return kind==FallowBlock.Kind.BACKSHELF&&stage==2?FallowTexts.RADIO_TONE:null;
    }

    // ------------------------------------------------------------------------------------------------ helpers
    /** Committed: from the lids until the light has had them, nobody leaves by the House's door. */
    static boolean locked(@Nullable Fallow.Site s,ServerPlayer p){return s!=null&&s.strike.toneAt>=0&&s.strike.since(s.tick)>=LIDS;}
    /** The station back as it was for a fresh afternoon: the television's picture, the lids on their seats, the doors down. */
    static void afresh(ServerLevel l,BlockPos b){
        tv(l,b,FallowBlock.PICTURE);
        FallowSite.lid(l,b,FallowSite.PAD_A,0);FallowSite.lid(l,b,FallowSite.PAD_B,0);
        // 0.4.680: a station the light has had is built again first, so the doors below are the doors as built.
        FallowRuin.mend(l,b);
        if(l.isLoaded(b.offset(FallowSite.WELL_W,0,FallowSite.WELL_S))&&Fallow.cellarOpen(l,b))Fallow.cellarDoors(l,b,false);
    }
    static void tv(ServerLevel l,BlockPos b,int stage){
        var at=b.offset(FallowSite.TV);if(!l.isLoaded(at))return;var st=l.getBlockState(at);
        if(FallowBlock.is(st,FallowBlock.Kind.TV)&&st.getValue(FallowBlock.STAGE)!=stage)l.setBlock(at,st.setValue(FallowBlock.STAGE,stage),3);
    }
    static Vec3 rel(Fallow.Site s,ServerPlayer p){return p.position().subtract(Vec3.atLowerCornerOf(s.base));}
    /** Everybody at the station, spectators too, told how far along it is. */
    static void send(ServerLevel l,Fallow.Site s,int t){
        var payload=new FallowStrikePayload(s.base,t);
        for(var p:l.players())if(FallowSite.inScene(rel(s,p)))HousePackets.send(p,payload);
    }
    /** A sound for one reader only, from the way of something far off, close enough to be heard. */
    static void toward(ServerPlayer p,Vec3 at,SoundEvent e,float volume,float pitch){
        var eye=p.getEyePosition();var d=at.subtract(eye);double len=d.length();var spot=len<10?at:eye.add(d.scale(10/len));
        p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(e),SoundSource.AMBIENT,spot.x,spot.y,spot.z,volume,pitch,p.getRandom().nextLong()));
    }
    static void heard(ServerPlayer p,SoundEvent e,float volume){toward(p,p.getEyePosition(),e,volume,1F);}

    // ------------------------------------------------------------------------------------------------ for tests
    static @Nullable State state(BlockPos b){var s=Fallow.afternoon(b);return s==null?null:s.strike;}
    /** The man gone a while since: the tone comes on the next tick. */
    public static void begin(BlockPos b){var s=Fallow.afternoon(b);if(s==null)return;s.stranger=Fallow.GONE;s.strike.goneAt=s.tick-AFTER_MAN;}
    /** The clock set on to this many ticks since the tone, as if that much had gone by. */
    public static void skip(BlockPos b,int since){var s=Fallow.afternoon(b);if(s!=null&&s.strike.toneAt>=0)s.tick=Math.max(s.tick,s.strike.toneAt+since);}
    public static int since(BlockPos b){var s=Fallow.afternoon(b);return s==null?-1:s.strike.since(s.tick);}
    public static int harlan(BlockPos b){var st=state(b);return st==null?-1:st.harlan;}
    public static int faye(BlockPos b){var st=state(b);return st==null?-1:st.faye;}
    public static int otis(BlockPos b){var st=state(b);return st==null?-1:st.otis;}
    public static int boyd(BlockPos b){var st=state(b);return st==null?-1:st.boyd;}
    public static int slid(BlockPos b){var st=state(b);return st==null?-1:st.slid;}
    public static int counted(BlockPos b){var st=state(b);return st==null?0:st.count;}
}
