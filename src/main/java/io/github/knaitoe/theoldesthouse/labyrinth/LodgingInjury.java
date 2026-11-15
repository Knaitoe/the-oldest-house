package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.606, The Ones Already Inside, its sixth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, N4: "the intrusion, the glass, the cut on
 * both; the pin; how failed bodies move, batter and take").
 *
 * <ul>
 * <li><b>The intrusion.</b> With Vera out of the workroom and Dan and Rae down in the hall, the failed body in the west passage pulls at
 * the papered jib door, then batters its latch, and comes through into the back passage.</li>
 * <li><b>How it moves.</b> A slow walk with its arms out, toward the nearest reader on the ground floor. A blow stops it and puts it
 * back a step.</li>
 * <li><b>How it takes.</b> Reaching a reader, it closes a hand on them and walks them back toward the jib door. Two blows from somebody
 * else, or three from the one held, break the grip. A reader it gets as far as the door it lets go in the passage, and goes on.</li>
 * <li><b>The glass and the cut.</b> Dan waits by the hall stand. When it comes within reach of him, he puts himself in its way; it
 * throws him back against the stand, the glass breaks, and his hand is cut. At the same moment Daniel's hand opens in the same place.
 * Then it goes back through the jib door and pulls it to.</li>
 * <li><b>Kept off.</b> Five blows before it reaches Dan drive it back through the door, and nothing is cut. That stands: the rule
 * comes later, with both men there, when Vera takes Dan down to Daniel's frame and pricks his hand with a pin.</li>
 * </ul>
 *
 * <p>Its state is the house's ({@code Injury} in the chapter's saved record); what each reader saw and did is their own.
 */
public final class LodgingInjury {
    private LodgingInjury(){}
    /** What the incident is doing: not yet, pulling at the jib door, battering it, loose in the house, going back, over. */
    public static final int NONE=0,PULL=1,BATTER=2,LOOSE=3,BACK=4,OVER=5;
    /** Ticks it pulls at the door, then batters it. */
    public static final int PULLING=60,BATTERING=60;
    /** Its pace, and Dan's when he goes to it; how near is a reach; how far a blow puts it back, and for how long it stops. */
    public static final double PACE=.075,DAN_PACE=.12,REACH=1.1,KNOCK=.5,DAN_REACH=1.25,DRAG=.05;
    public static final int STUN=15;
    /** Blows that drive it off; blows that break a grip, from others and from the one held. */
    public static final int KEEP_OFF=5,FREE_OTHERS=2,FREE_SELF=3;
    /** Where Dan waits for it (in front of the hall stand), and how near it comes to him there before he goes to it. */
    public static final Vec3 DAN_POST=new Vec3(-1.6,LodgingSite.GROUND,-7.9);
    public static final double DAN_WATCH=5;
    /** How long after it is over, with nothing cut, before Vera takes Dan down to the frame; how long Dan holds his cut hand. */
    public static final int PIN_WAIT=600,HURT_FOR=200;
    /** The marks the two men carry (LiteraryActor.marks): the glass's cut, the pin's prick. */
    public static final int CUT=1,PRICK=2;
    /** Each reader's own record. */
    public static final String HEARD_JIB="HeardJib0606",STRUCK="StruckFailed0606",SAW_DAN_CUT="SawDanCut0606",SAW_DANIEL_CUT="SawDanielCut0606",
        KEPT_OFF="KeptOff0606",TAKEN="Taken0606",HELD="Held0606",SAW_PIN="SawPin0606";
    /** Where it stands in the passage, through the jib door, and the mouth of the hall it heads for. */
    static final Vec3 THROUGH=new Vec3(-12.5,LodgingSite.GROUND,-13.5),MOUTH=new Vec3(.5,LodgingSite.GROUND,-13.5);

    // ------------------------------------------------------------------------------------------------ state
    static CompoundTag state(CompoundTag house){if(!house.contains("Injury"))house.put("Injury",new CompoundTag());return house.getCompound("Injury");}
    public static CompoundTag state(net.minecraft.server.MinecraftServer s,BlockPos b){return state(LodgingStory.house(s,b));}
    public static int phase(net.minecraft.server.MinecraftServer s,BlockPos b){return state(s,b).getInt("Phase");}
    public static boolean cut(net.minecraft.server.MinecraftServer s,BlockPos b){return state(s,b).getBoolean("Cut");}
    public static boolean kept(net.minecraft.server.MinecraftServer s,BlockPos b){return state(s,b).getBoolean("Kept");}
    /** Whether the failed body is the incident's to move just now (LodgingPeople leaves its head alone). */
    public static boolean moving(net.minecraft.server.MinecraftServer s,BlockPos b){int ph=phase(s,b);return ph>=PULL&&ph<OVER;}
    static void go(CompoundTag in,int phase,int clock){in.putInt("Phase",phase);in.putInt("At",clock);}

    /** The chapter reaches it: the body at the jib door begins. */
    public static void begin(CompoundTag house,int clock){var in=state(house);if(in.getInt("Phase")==NONE)go(in,PULL,clock);}

    // ------------------------------------------------------------------------------------------------ every tick
    /** Once a tick while the chapter is in N4 (LodgingStory). True once it is over. */
    static boolean tick(ServerLevel l,BlockPos b,CompoundTag house,List<ServerPlayer> here,int clock){
        var in=state(house);int ph=in.getInt("Phase");int into=clock-in.getInt("At");
        var body=LodgingPeople.find(l,b,LodgingPeople.FAILED_SHIRT);
        var jib=b.offset(LodgingSite.JIB);
        switch(ph){
            case PULL->{
                if(body!=null&&body.gesture()!=LodgingPeople.FRAME)body.gesture(LodgingPeople.FRAME);
                if(into%15==0)l.playSound(null,jib,SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.NEUTRAL,.35F,.55F+.05F*(into/15%3));
                if(!in.getBoolean("Heard")){in.putBoolean("Heard",true);for(var p:here){if(!near(p,jib,24))continue;var own=LodgingHouse.own(p);own.putBoolean(HEARD_JIB,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.JIB_PULLED);}}
                if(into>=PULLING){go(in,BATTER,clock);if(body!=null)body.gesture(LodgingPeople.BATTER);}
            }
            case BATTER->{
                if(body!=null&&body.gesture()!=LodgingPeople.BATTER)body.gesture(LodgingPeople.BATTER);
                if(into==0||into==14||into==26||into==36||into==44||into==50)l.playSound(null,jib,SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,SoundSource.NEUTRAL,.6F,.8F+into*.003F);
                if(into>=BATTERING){
                    if(!LodgingSite.open(l,b,LodgingSite.JIB))LodgingHouse.toggle(l,b,LodgingSite.JIB);
                    l.playSound(null,jib,SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR,SoundSource.NEUTRAL,.5F,1.1F);
                    in.putInt("Leg",0);go(in,LOOSE,clock);if(body!=null)body.gesture(LodgingPeople.REACH);
                }
            }
            case LOOSE->loose(l,b,house,in,here,body,clock);
            case BACK->{
                if(body==null){go(in,OVER,clock);break;}
                var home=LodgingPeople.world(b,LodgingPeople.SPOTS[LodgingPeople.FAILED_SHIRT]);
                Vec3 to=lined(b,body,rel(b,body).x>THROUGH.x+.6?abs(b,THROUGH):home);
                if(step(l,body,to,PACE)&&to==home){
                    body.gesture(LodgingPeople.FRAME);var s=LodgingPeople.SPOTS[LodgingPeople.FAILED_SHIRT];body.setYRot(s.look());body.setYBodyRot(s.look());body.setYHeadRot(s.look());
                    if(in.getBoolean("Kept"))for(var p:here)if(LodgingHouse.own(p).getInt(STRUCK)>0){var own=LodgingHouse.own(p);own.putBoolean(KEPT_OFF,true);LodgingHouse.save(p,own);}
                    go(in,OVER,clock);
                }else if(body.gesture()!=LodgingPeople.REACH)body.gesture(LodgingPeople.REACH);
            }
            case OVER->{
                // It pulls the jib door to behind it, once nobody is standing in the doorway.
                if(!in.getBoolean("Shut")){
                    if(!LodgingSite.open(l,b,LodgingSite.JIB))in.putBoolean("Shut",true);
                    else if(!LodgingStory.someoneIn(l,jib,1.2,body)){LodgingHouse.toggle(l,b,LodgingSite.JIB);in.putBoolean("Shut",true);}
                }
            }
            default->{}
        }
        // Dan, back to his place by the bench once it has gone; his hand held to him a while after the glass.
        var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);
        if(dan!=null){
            if(dan.gesture()==LodgingPeople.HURT&&in.getBoolean("Cut")&&clock-in.getInt("CutAt")>=HURT_FOR)dan.gesture(LodgingPeople.POCKET);
            if(in.getBoolean("Cut")&&!in.getBoolean("DanSaid")&&clock-in.getInt("CutAt")>=40){in.putBoolean("DanSaid",true);dan.say("Dan",LodgingTexts.DAN_CUT);dan.speaks(LodgingTexts.DAN_CUT);}
            int now=in.getInt("Phase");
            if(now==BACK||now==OVER){var spot=LodgingPeople.world(b,LodgingStory.DAN_HALL);if(dan.position().distanceToSqr(spot)>.01){if(step(l,dan,spot,DAN_PACE)){var s=LodgingStory.DAN_HALL;dan.setYRot(s.look());dan.setYBodyRot(s.look());}}}
        }
        var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);
        if(daniel!=null&&daniel.gesture()==LodgingPeople.STRAIN&&in.contains("FlinchUntil")&&clock>=in.getInt("FlinchUntil")&&!LodgingDaniel.straining(b)){daniel.gesture(LodgingPeople.HELD);in.remove("FlinchUntil");}
        return in.getInt("Phase")==OVER&&in.getBoolean("Shut")&&(dan==null||dan.position().distanceToSqr(LodgingPeople.world(b,LodgingStory.DAN_HALL))<.02);
    }

    /** Loose in the house: after its reader, or with one in its hand; Dan in its way. */
    private static void loose(ServerLevel l,BlockPos b,CompoundTag house,CompoundTag in,List<ServerPlayer> here,@Nullable LiteraryActor body,int clock){
        if(body==null){go(in,BACK,clock);return;}
        var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);
        var held=in.hasUUID("Held")?l.getServer().getPlayerList().getPlayer(in.getUUID("Held")):null;
        if(held!=null&&(!held.isAlive()||held.isSpectator()||!here.contains(held))){release(in,held,false);held=null;}
        boolean stunned=clock<in.getInt("StunUntil");
        // Through the door first.
        if(in.getInt("Leg")==0){
            if(stunned||step(l,body,abs(b,THROUGH),PACE))in.putInt("Leg",1);
            body.gesture(LodgingPeople.REACH);dan(l,b,in,dan,body,held,clock);return;
        }
        if(held!=null){
            // Walking its reader back to the door, the reader a step behind it.
            body.gesture(LodgingPeople.DRAG);
            var door=abs(b,new Vec3(LodgingSite.PASS_W+.5,LodgingSite.GROUND,LodgingSite.JIB.getZ()+.5));
            var drag=lined(b,body,rel(b,body).x>THROUGH.x+.6?abs(b,THROUGH):door);
            boolean there=!stunned&&step(l,body,drag,DRAG)&&drag==door;
            var back=body.position().subtract(body.getLookAngle().multiply(1,0,1).normalize().scale(-.9));
            held.teleportTo(l,back.x,body.getY(),back.z,held.getYRot(),held.getXRot());
            if(there){
                // It lets go of them in the passage, and goes on without them.
                var own=LodgingHouse.own(held);own.putBoolean(TAKEN,true);LodgingHouse.save(held,own);LodgingHouse.cue(held,LodgingTexts.TAKEN);
                held.teleportTo(l,b.getX()+LodgingSite.PASS_W+.5,b.getY()+LodgingSite.GROUND,b.getZ()+LodgingSite.JIB.getZ()-1.5,held.getYRot(),held.getXRot());
                release(in,held,false);go(in,BACK,clock);return;
            }
            dan(l,b,in,dan,body,held,clock);return;
        }
        // After the nearest reader on the ground floor; by the mouth of the hall if there is nobody it can reach from the passage.
        ServerPlayer target=null;double best=Double.MAX_VALUE;
        for(var p:here){
            if(p.isSpectator()||!p.isAlive())continue;var r=rel(b,p);
            if(!LodgingSite.inBackPassage(r)&&!LodgingSite.inHall(r))continue;
            double d=p.distanceToSqr(body);if(d<best){best=d;target=p;}
        }
        body.gesture(LodgingPeople.REACH);
        if(!stunned){
            if(target==null)step(l,body,abs(b,MOUTH),PACE);
            else{
                var r=rel(b,body);var tr=rel(b,target);
                // From the passage into the hall by its mouth, not through the wall.
                Vec3 to=LodgingSite.inBackPassage(r)&&LodgingSite.inHall(tr)&&Math.abs(r.x-MOUTH.x)>1.2?abs(b,MOUTH):target.position();
                step(l,body,to,PACE);
                if(body.distanceTo(target)<=REACH&&!in.getBoolean("Kept"))grip(in,target,clock);
            }
        }
        dan(l,b,in,dan,body,null,clock);
    }
    /** Dan, by the hall stand: he goes to it when it comes near, and puts himself in its way. */
    private static void dan(ServerLevel l,BlockPos b,CompoundTag in,@Nullable LiteraryActor dan,LiteraryActor body,@Nullable ServerPlayer held,int clock){
        if(dan==null||in.getBoolean("Cut")||in.getBoolean("Kept"))return;
        var post=abs(b,DAN_POST);
        boolean coming=body.position().distanceTo(post)<=DAN_WATCH;
        // A reader already at it, and nobody in its hand: he holds back and lets them.
        boolean fought=held==null&&clock<in.getInt("FoughtUntil");
        Vec3 to=coming&&!fought?body.position():post;
        if(dan.position().distanceToSqr(to)>.01)step(l,dan,coming&&!fought?approach(dan.position(),to,DAN_REACH*.8):to,DAN_PACE);
        if(dan.gesture()!=LodgingPeople.WAIT&&dan.gesture()!=LodgingPeople.HURT)dan.gesture(LodgingPeople.WAIT);
        if(coming&&!fought&&dan.distanceTo(body)<=DAN_REACH)glass(l,b,in,dan,body,held,clock);
    }
    /** It throws him back against the hall stand; the glass breaks; his hand is cut, and Daniel's opens in the same place. */
    private static void glass(ServerLevel l,BlockPos b,CompoundTag in,LiteraryActor dan,LiteraryActor body,@Nullable ServerPlayer held,int clock){
        in.putBoolean("Cut",true);in.putInt("CutAt",clock);
        if(held!=null)release(in,held,true);
        var stand=b.offset(LodgingSite.HALL_STAND).above();var st=l.getBlockState(stand);
        if(st.is(LodgingRegistry.FITTING.get()))l.setBlock(stand,st.setValue(LodgingBlock.STAGE,2),3);
        l.playSound(null,stand,SoundEvents.GLASS_BREAK,SoundSource.NEUTRAL,1F,.9F);
        var against=abs(b,new Vec3(LodgingSite.HALL_STAND.getX()+1.3,LodgingSite.GROUND,LodgingSite.HALL_STAND.getZ()+.5));
        dan.setPos(against.x,against.y,against.z);dan.setYRot(90);dan.setYBodyRot(90);dan.setYHeadRot(90);
        dan.gesture(LodgingPeople.HURT);dan.marks(dan.marks()|CUT);
        var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);
        if(daniel!=null){daniel.marks(daniel.marks()|CUT);daniel.gesture(LodgingPeople.STRAIN);in.putInt("FlinchUntil",clock+30);}
        for(var p:LodgingHouse.readers(l,b)){
            if(p.isSpectator())continue;var own=LodgingHouse.own(p);boolean changed=false;
            if(p.distanceTo(dan)<=16&&p.hasLineOfSight(dan)){own.putBoolean(SAW_DAN_CUT,true);changed=true;LodgingHouse.cue(p,LodgingTexts.GLASS_BREAKS);}
            if(daniel!=null&&(LodgingDaniel.reach(p,b)||p.distanceTo(daniel)<=6&&p.hasLineOfSight(daniel))){
                own.putBoolean(SAW_DANIEL_CUT,true);changed=true;LodgingHouse.cue(p,LodgingTexts.DANIEL_HAND_OPENS);
                Talk.line(p,LodgingPeople.name(LodgingPeople.DANIEL,own),LodgingTexts.DANIEL_CUT);
            }
            if(changed)LodgingHouse.save(p,own);
        }
        if(daniel!=null){daniel.say(LodgingPeople.name(LodgingPeople.DANIEL,new CompoundTag()),LodgingTexts.DANIEL_CUT);daniel.speaks(LodgingTexts.DANIEL_CUT);}
        go(in,BACK,clock);
    }
    private static void grip(CompoundTag in,ServerPlayer p,int clock){
        in.putUUID("Held",p.getUUID());in.putInt("Others",0);in.putInt("Own",0);
        var own=LodgingHouse.own(p);own.putBoolean(HELD,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.GRIPPED);
    }
    private static void release(CompoundTag in,ServerPlayer p,boolean say){in.remove("Held");if(say)LodgingHouse.cue(p,LodgingTexts.GRIP_BROKEN);}

    /** A blow on the failed body, from a reader. True if the incident took it. */
    public static boolean struck(ServerPlayer p,LiteraryActor body){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null)return false;var l=p.serverLevel();var s=l.getServer();
        var house=LodgingStory.house(s,b);var in=state(house);int ph=in.getInt("Phase");
        if(ph!=LOOSE)return false;
        int clock=house.getInt("Clock");
        var own=LodgingHouse.own(p);own.putInt(STRUCK,own.getInt(STRUCK)+1);LodgingHouse.save(p,own);
        int blows=in.getInt("Blows")+1;in.putInt("Blows",blows);in.putInt("StunUntil",clock+STUN);in.putInt("FoughtUntil",clock+60);
        // Put back a step, away from whoever hit it.
        var away=body.position().subtract(p.position()).multiply(1,0,1);
        // Never into a wall or the door's jambs: the whole body clear where it lands.
        if(away.lengthSqr()>1e-4){var to=body.position().add(away.normalize().scale(KNOCK));if(!l.getBlockCollisions(body,body.getBoundingBox().move(to.subtract(body.position())).inflate(.02,-.02,.02)).iterator().hasNext())body.setPos(to.x,body.getY(),to.z);}
        l.playSound(null,body.blockPosition(),SoundEvents.PLAYER_ATTACK_WEAK,SoundSource.PLAYERS,.8F,.8F);
        if(in.hasUUID("Held")){
            var held=s.getPlayerList().getPlayer(in.getUUID("Held"));
            boolean self=held!=null&&held.getUUID().equals(p.getUUID());
            int n=in.getInt(self?"Own":"Others")+1;in.putInt(self?"Own":"Others",n);
            if(held!=null&&n>=(self?FREE_SELF:FREE_OTHERS)){release(in,held,true);in.putInt("StunUntil",clock+STUN*2);}
        }
        if(blows>=KEEP_OFF&&!in.getBoolean("Cut")){
            in.putBoolean("Kept",true);
            if(in.hasUUID("Held")){var held=s.getPlayerList().getPlayer(in.getUUID("Held"));if(held!=null)release(in,held,true);else in.remove("Held");}
            go(in,BACK,clock);
        }else LodgingHouse.cue(p,LodgingTexts.STRUCK_FAILED);
        LodgingStory.save(s,b,house);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the pin
    /** Whether the pin is due: it is over, nothing was cut, and the minute or so has passed. */
    static boolean pinDue(CompoundTag house,int clock){var in=state(house);return in.getInt("Phase")==OVER&&!in.getBoolean("Cut")&&clock-in.getInt("At")>=PIN_WAIT;}
    /** At the frame: Vera pricks Dan's hand, and Daniel's bleeds where it did. */
    static void pin(ServerLevel l,BlockPos b){
        var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);
        if(dan!=null){dan.marks(dan.marks()|PRICK);}
        if(daniel!=null){daniel.marks(daniel.marks()|PRICK);daniel.gesture(LodgingPeople.STRAIN);state(l.getServer(),b).putInt("FlinchUntil",LodgingStory.house(l.getServer(),b).getInt("Clock")+30);}
        if(vera!=null){vera.say("Vera",LodgingTexts.VERA_PIN);vera.speaks(LodgingTexts.VERA_PIN);}
        for(var p:LodgingHouse.readers(l,b)){
            if(p.isSpectator()||vera==null||p.distanceTo(vera)>10)continue;
            var own=LodgingHouse.own(p);own.putBoolean(SAW_PIN,true);LodgingHouse.save(p,own);Talk.line(p,"Vera",LodgingTexts.VERA_PIN);
        }
    }

    // ------------------------------------------------------------------------------------------------ moving a body
    static Vec3 rel(BlockPos b,net.minecraft.world.entity.Entity e){return e.position().subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 abs(BlockPos b,Vec3 r){return r.add(b.getX(),b.getY(),b.getZ());}
    private static boolean near(ServerPlayer p,BlockPos at,double d){return !p.isSpectator()&&p.position().distanceToSqr(Vec3.atCenterOf(at))<=d*d;}
    /** Near the jib door, back onto the passage's middle line before going through it (the doorway is a body and a little wide). */
    private static Vec3 lined(BlockPos b,LiteraryActor body,Vec3 to){
        var r=rel(b,body);double mid=THROUGH.z;
        // From the hall, out by its mouth into the back passage first, not through the wall's corner.
        if(LodgingSite.inHall(r)&&!LodgingSite.inHall(to.subtract(b.getX(),b.getY(),b.getZ())))return abs(b,MOUTH);
        if(r.x<THROUGH.x+1&&Math.abs(r.z-mid)>.12)return abs(b,new Vec3(r.x,LodgingSite.GROUND,mid));
        return to;
    }
    /** A point short of where it is going, this far from it. */
    private static Vec3 approach(Vec3 from,Vec3 to,double shortBy){var d=to.subtract(from);double len=d.horizontalDistance();if(len<=shortBy)return from;return to.subtract(d.multiply(1,0,1).normalize().scale(shortBy));}
    /** A step toward it on its own feet (LodgingStory.climb). True once there. */
    static boolean step(ServerLevel l,LiteraryActor a,Vec3 to,double pace){return LodgingStory.climb(l,a,to,pace);}
    /** Everybody's hand off the body and the readers it held; for a house built again. */
    public static void clear(net.minecraft.server.MinecraftServer s,BlockPos b){LodgingStory.house(s,b).remove("Injury");}
    /** The reader it is holding, for a case. */
    public static @Nullable UUID held(net.minecraft.server.MinecraftServer s,BlockPos b){var in=state(s,b);return in.hasUUID("Held")?in.getUUID("Held"):null;}
}
