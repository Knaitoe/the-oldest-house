package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.614, The Ones Already Inside, its fourteenth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, E2: "Complete Dan. Daniel is brought to
 * the frame and held. The room grows shallower; the chair is pushed back into a wall with nothing behind it; Daniel's voice goes on
 * after there is no room for him. Dan lives, and is his own.").
 *
 * <ul>
 * <li><b>When.</b> The house settled as for E1 (the first attempt over, nobody in the walls), nobody walking Dan back, and Vera backed:
 * a reader's body steadied at the frame by its original, or a reader who promised her their hands when she called. Ten seconds after
 * that she says it is time, and calls the promised down.</li>
 * <li><b>Brought.</b> Dan, Rae and Vera come to Daniel's room. If Daniel's straps are open he has to be walked to his frame: only by the
 * readers who take him by the shoulders, faster with more of them, and he stops where he is when nobody has hold of him. A steadied
 * body steps aside to let him in, and puts its hands back.</li>
 * <li><b>Held.</b> In the frame he is held by what holds him: the straps if they are shut, the steadied body, every reader with their
 * hands on him. Held at all, the room goes shallower (the plaster takes him, his chair is pushed back across the boards and into the
 * wall); held by two or more, twice as fast. Nobody holding him for five seconds and he pulls out of it, and it is over: Vera does not
 * try again. The key, put to the straps, takes one hold away.</li>
 * <li><b>After.</b> His voice goes on in the wall when there is no room for him. Dan says Rae's name, and under it, from the same
 * mouth, Daniel finishes what he was asking. Vera says it settles. Dan puts his hand over his mouth; Rae tries to take it away.</li>
 * </ul>
 *
 * <p>Whose hands are on Daniel is each reader's own act; nobody's body is used but by their own reply (the steadied body's original gave
 * it in 0.4.612). The end is the house's, the same for everybody in it.
 */
public final class LodgingComplete {
    private LodgingComplete(){}
    public static final int NONE=0,GATHER=1,HOLDING=2,VOICE=3,CODA=4,DONE=5,BROKEN=6;
    public static final String STATE="Complete0614";
    /** Each reader's own. */
    public static final String CALLED="CalledDown0614",HELD="HeldDaniel0614",SAW="SawComplete0614",HEARD_UNDER="HeardUnder0614",LET_GO="LetDanielGo0614";
    /** How long after she is backed she starts; how long she waits for hands; held-by-nobody before he pulls out; the hold's length. */
    public static final int CALL_AFTER=200,WAIT_HANDS=1800,PULLS_OUT=100,TOTAL=300,VOICE_GAP=60,CODA_GAP=60;
    public static final double HOLD_REACH=2.6,HOLD_PACE=.04,HOLD_MAX=.1;
    /** Where Dan and Rae stand to see it; the chair's steps toward the wall; where a steadied body stands aside. */
    static final LodgingPeople.Spot DAN_WATCH=new LodgingPeople.Spot(-5.0,LodgingSite.GROUND,-25.2,135,LodgingPeople.WAIT),
        RAE_WATCH=new LodgingPeople.Spot(-4.0,LodgingSite.GROUND,-24.0,140,LodgingPeople.WAIT);
    static final Vec3 ASIDE=new Vec3(-6.3,0,-26.9);
    /** Dan with his hand over his mouth; Rae with both hands at his wrist. */
    public static final int MOUTH=23,TAKE=24;

    static CompoundTag state(CompoundTag house){if(!house.contains(STATE))house.put(STATE,new CompoundTag());return house.getCompound(STATE);}
    public static int phase(MinecraftServer s,BlockPos b){return state(LodgingStory.house(s,b)).getInt("Phase");}
    /** Begun and not given up: E1 is not offered. */
    public static boolean begun(MinecraftServer s,BlockPos b){int ph=phase(s,b);return ph!=NONE&&ph!=BROKEN;}
    /** Daniel is in the wall: LodgingPeople does not make him again. */
    public static boolean gone(MinecraftServer s,BlockPos b){int ph=phase(s,b);return ph>=VOICE&&ph<=DONE;}
    private static void go(CompoundTag in,int phase,int clock){in.putInt("Phase",phase);in.putInt("At",clock);}

    /** Where one of the four is kept by this slice, or null. */
    static @Nullable LodgingPeople.Spot spot(MinecraftServer s,BlockPos b,int who){
        var in=state(LodgingStory.house(s,b));int ph=in.getInt("Phase");
        if(ph==NONE)return null;
        if(who==LodgingPeople.DANIEL&&in.getBoolean("PulledOut"))return LodgingReturn.DANIEL_OUT;
        if(ph==BROKEN)return null;
        if(who==LodgingPeople.DAN&&in.getBoolean("DanThere"))return DAN_WATCH;
        if(who==LodgingPeople.RAE&&in.getBoolean("RaeThere"))return RAE_WATCH;
        if(who==LodgingPeople.VERA&&in.getBoolean("VeraThere"))return LodgingTaken.VERA_AT;
        return null;
    }
    /** Vera backed: a body steadied at the frame, or hands promised her. */
    static boolean backed(CompoundTag h){
        var v=LodgingTaken.vera(h);int vp=v.getInt("Phase");
        return vp==LodgingTaken.V_ASK&&v.getBoolean("Steadied")||vp==LodgingTaken.V_CALLED&&v.getInt("Agreed")>0;
    }
    static boolean steadied(CompoundTag h){var v=LodgingTaken.vera(h);return v.getInt("Phase")==LodgingTaken.V_ASK&&v.getBoolean("Steadied");}

    // ------------------------------------------------------------------------------------------------ every tick
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var s=l.getServer();var h=LodgingStory.house(s,b);var in=state(h);int clock=h.getInt("Clock");int ph=in.getInt("Phase");
        var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);
        boolean changed=switch(ph){
            case NONE->start(l,b,h,in,clock);
            case GATHER->gather(l,b,h,in,daniel,here,clock);
            case HOLDING->hold(l,b,h,in,daniel,here,clock);
            case VOICE,CODA->after(l,b,h,in,here,clock);
            case BROKEN->pulledOut(l,b,in,daniel);
            default->false;
        };
        if(ph!=NONE&&ph!=DONE)changed|=company(l,b,h,in);
        if(changed)LodgingStory.save(s,b,h);
    }
    private static boolean start(ServerLevel l,BlockPos b,CompoundTag h,CompoundTag in,int clock){
        var s=l.getServer();
        if(!LodgingReturn.ready(s,b)||LodgingReturn.phase(s,b)!=LodgingReturn.NONE||!backed(h)||LodgingPeople.find(l,b,LodgingPeople.DANIEL)==null)return false;
        if(!in.contains("BackedAt")){in.putInt("BackedAt",clock);return true;}
        if(clock-in.getInt("BackedAt")<CALL_AFTER)return false;
        var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);
        boolean called=!steadied(h);
        for(var p:LodgingHouse.readers(l,b)){
            if(p.isSpectator())continue;
            var own=LodgingHouse.own(p);
            if(called){for(var line:LodgingTexts.VERA_COME_DOWN)Talk.line(p,"Vera",line);
                if(own.getBoolean(LodgingTaken.AGREED)){own.putBoolean(CALLED,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.YOUR_CALL);}}
            else if(vera!=null&&p.distanceTo(vera)<=LodgingDaniel.HEARD)for(var line:LodgingTexts.VERA_NOW)Talk.line(p,"Vera",line);
        }
        if(vera!=null){var line=called?LodgingTexts.VERA_COME_DOWN[0]:LodgingTexts.VERA_NOW[0];vera.say("Vera",line);vera.speaks(line);}
        if(!called)in.putBoolean("VeraThere",true);
        in.putBoolean("Called",called);go(in,GATHER,clock);
        var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);if(dan!=null)in.putString("DanFrom",zone(b,dan));
        var rae=LodgingPeople.find(l,b,LodgingPeople.RAE);if(rae!=null)in.putString("RaeFrom",zone(b,rae));
        var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);if(daniel!=null)in.putString("DanielFrom",zone(b,daniel));
        in.putInt("HeldAt",clock);
        return true;
    }
    /** Dan, Rae and Vera on their way to his room, each by the doors between. */
    private static boolean company(ServerLevel l,BlockPos b,CompoundTag h,CompoundTag in){
        boolean changed=false;
        if(!in.getBoolean("DanThere")){var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);
            if(dan==null||walkTo(l,b,in,"DanLeg",dan,in.getString("DanFrom"),DAN_WATCH,.09)){in.putBoolean("DanThere",true);
                if(dan!=null){face(dan,DAN_WATCH.look());dan.gesture(LodgingPeople.WAIT);if(in.getInt("Phase")<=HOLDING)aloud(l,b,dan,"Dan",LodgingTexts.DAN_WHAT);}}
            changed=true;}
        if(!in.getBoolean("RaeThere")){var rae=LodgingPeople.find(l,b,LodgingPeople.RAE);
            if(rae==null||walkTo(l,b,in,"RaeLeg",rae,in.getString("RaeFrom"),RAE_WATCH,.09)){in.putBoolean("RaeThere",true);if(rae!=null){face(rae,RAE_WATCH.look());rae.gesture(LodgingPeople.WAIT);}}
            changed=true;}
        if(!in.getBoolean("VeraThere")){
            if(!LodgingStory.walkVia(l,b,h,"CompleteVera1",LodgingPeople.VERA,LodgingMeeting.VERA_DOWN,new LodgingPeople.Spot(0,LodgingSite.GROUND,-14,180,LodgingPeople.WAIT),LodgingSite.WORK_DOOR,1)){}
            else if(LodgingStory.walkVia(l,b,h,"CompleteVera2",LodgingPeople.VERA,LodgingMeeting.TO_FRAME.subList(2,LodgingMeeting.TO_FRAME.size()),LodgingTaken.VERA_AT,LodgingSite.DANIEL_DOOR,1))
                in.putBoolean("VeraThere",true);
            changed=true;}
        return changed;
    }
    /** Daniel brought to his frame: at once if he is strapped in it, by readers' hands if he is free. */
    private static boolean gather(ServerLevel l,BlockPos b,CompoundTag h,CompoundTag in,@Nullable LiteraryActor daniel,List<ServerPlayer> here,int clock){
        if(daniel==null){go(in,BROKEN,clock);return true;}
        if(!LodgingKey.free(l.getServer(),b)){go(in,HOLDING,clock);in.putInt("HeldAt",clock);daniel.gesture(LodgingPeople.STRAIN);return true;}
        int near=holders(in,daniel,here);
        if(near==0){
            if(daniel.gesture()==LodgingPeople.STRAIN)daniel.gesture(LodgingPeople.WAIT);
            if(clock-in.getInt("HeldAt")>=WAIT_HANDS){go(in,BROKEN,clock);say(l,b,LodgingPeople.VERA,LodgingTexts.VERA_LOST);}
            return false;
        }
        in.putInt("HeldAt",clock);daniel.gesture(LodgingPeople.STRAIN);
        var steadied=steadied(h)?LodgingDoubles.sleeper(l,b,LodgingTaken.vera(h).getInt("Room")):null;
        // The last of the way, from in front of the frame back into it: the steadied body steps aside first.
        boolean last=in.getInt("DanielLeg")>=route(in.getString("DanielFrom")).size();
        if(last&&steadied!=null&&!in.getBoolean("Aside")){if(LodgingInjury.step(l,steadied,ASIDE.add(b.getX(),b.getY(),b.getZ()),.08))in.putBoolean("Aside",true);return true;}
        if(!last){LodgingTaken.walk(l,b,in,"DanielLeg",daniel,route(in.getString("DanielFrom")),doors(in.getString("DanielFrom")),Math.min(HOLD_MAX,HOLD_PACE*near));return true;}
        daniel.noPhysics=true;daniel.setNoGravity(true);
        if(LodgingInjury.step(l,daniel,LodgingReturn.IN.add(b.getX(),b.getY(),b.getZ()),.04)){
            face(daniel,0);go(in,HOLDING,clock);in.putInt("HeldAt",clock);
            l.playSound(null,daniel.blockPosition(),SoundEvents.WOOD_PLACE,SoundSource.NEUTRAL,.5F,.6F);
        }
        return true;
    }
    /** Held in the frame: the room goes shallower while anything holds him; nobody, and he pulls out of it. */
    private static boolean hold(ServerLevel l,BlockPos b,CompoundTag h,CompoundTag in,@Nullable LiteraryActor daniel,List<ServerPlayer> here,int clock){
        if(daniel==null){go(in,BROKEN,clock);return true;}
        var s=l.getServer();
        // A steadied body that stood aside goes back and puts its hands on him.
        var steadied=steadied(h)?LodgingDoubles.sleeper(l,b,LodgingTaken.vera(h).getInt("Room")):null;
        if(steadied!=null&&in.getBoolean("Aside")){var at=LodgingTaken.DOUBLE_AT;
            if(LodgingInjury.step(l,steadied,new Vec3(at.x()+b.getX(),at.y()+b.getY(),at.z()+b.getZ()),.08)){in.putBoolean("Aside",false);steadied.gesture(LodgingTaken.STEADY);face(steadied,180);}}
        int strength=holders(in,daniel,here);
        if(!LodgingKey.free(s,b))strength++;
        if(steadied!=null&&!in.getBoolean("Aside")&&steadied.distanceTo(daniel)<1.6)strength++;
        if(strength==0){
            if(clock-in.getInt("HeldAt")>=PULLS_OUT){
                go(in,BROKEN,clock);in.putBoolean("PulledOut",true);in.putInt("OutLeg",0);daniel.gesture(LodgingPeople.WAIT);daniel.noPhysics=false;daniel.setNoGravity(false);
                aloud(l,b,daniel,"Daniel",LodgingTexts.DANIEL_PULLS_OUT);say(l,b,LodgingPeople.VERA,LodgingTexts.VERA_LOST);
            }
            return true;
        }
        in.putInt("HeldAt",clock);
        int was=in.getInt("Progress"),now=Math.min(TOTAL,was+(strength>=2?2:1));in.putInt("Progress",now);
        if(daniel.gesture()!=LodgingPeople.STRAIN)daniel.gesture(LodgingPeople.STRAIN);
        // Taken back into the plaster as Dan was, a little at a time.
        face(daniel,0);daniel.noPhysics=true;daniel.setNoGravity(true);daniel.setDeltaMovement(Vec3.ZERO);
        var at=LodgingInjury.abs(b,LodgingReturn.IN);daniel.setPos(at.x,at.y,at.z-.75*now/(double)TOTAL);
        // What he says as it goes; the dust off the back wall; the chair across the boards.
        for(int i=0;i<LodgingTexts.DANIEL_HELD.length;i++)if(was<TOTAL*i/3+1&&now>=TOTAL*i/3+1)aloud(l,b,daniel,"Daniel",LodgingTexts.DANIEL_HELD[i]);
        if(now/20!=was/20)shallower(l,b);
        for(int step=1;step<=3;step++)if(was<TOTAL*step/4&&now>=TOTAL*step/4)chair(l,b,in,step);
        if(was==0)for(var p:here)if(!p.isSpectator()&&sees(b,p,daniel))LodgingHouse.cue(p,LodgingTexts.ROOM_SHALLOWER);
        if(now>=TOTAL){
            chair(l,b,in,4);
            for(var p:here){if(p.isSpectator()||p.distanceTo(daniel)>16)continue;
                var own=LodgingHouse.own(p);if(sees(b,p,daniel))own.putBoolean(SAW,true);LodgingHouse.save(p,own);}
            l.playSound(null,BlockPos.containing(at),SoundEvents.SAND_PLACE,SoundSource.NEUTRAL,.7F,.5F);
            daniel.discard();go(in,VOICE,clock);
        }
        return true;
    }
    /** After: his voice in the wall, then Dan, the voice under his, Vera, and Dan's hand. */
    private static boolean after(ServerLevel l,BlockPos b,CompoundTag h,CompoundTag in,List<ServerPlayer> here,int clock){
        int into=clock-in.getInt("At"),beat=in.getInt("Beat");var wall=LodgingInjury.abs(b,LodgingReturn.IN);
        if(beat<LodgingTexts.DANIEL_WALL.length&&into>=VOICE_GAP*(beat+1)/2){
            for(var p:here)if(!p.isSpectator()&&p.position().distanceTo(wall)<=LodgingDaniel.HEARD){Talk.line(p,"Daniel",LodgingTexts.DANIEL_WALL[beat]);if(beat==0)LodgingHouse.cue(p,LodgingTexts.VOICE_GOES_ON);}
            in.putInt("Beat",beat+1);if(beat+1==LodgingTexts.DANIEL_WALL.length)go(in,CODA,clock);return true;
        }
        if(in.getInt("Phase")!=CODA)return false;
        int c=in.getInt("Coda");var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);var rae=LodgingPeople.find(l,b,LodgingPeople.RAE);
        if(c==0&&into>=CODA_GAP){
            if(dan!=null){aloud(l,b,dan,"Dan",LodgingTexts.DAN_RAE);
                for(var p:here)if(!p.isSpectator()&&p.distanceTo(dan)<=LodgingDaniel.HEARD){Talk.line(p,"Daniel",LodgingTexts.DANIEL_UNDER);LodgingHouse.cue(p,LodgingTexts.UNDER_IT);
                    var own=LodgingHouse.own(p);own.putBoolean(HEARD_UNDER,true);LodgingHouse.save(p,own);}}
            in.putInt("Coda",1);return true;
        }
        if(c==1&&into>=CODA_GAP*2){say(l,b,LodgingPeople.VERA,LodgingTexts.VERA_SETTLES);in.putInt("Coda",2);return true;}
        if(c==2&&into>=CODA_GAP*3){
            if(dan!=null)dan.gesture(MOUTH);
            if(rae!=null&&dan!=null){face(rae,LodgingPeople.yawTo(rae,dan.position()));}
            in.putInt("Coda",3);return true;
        }
        if(c==3){
            if(rae!=null&&!in.getBoolean("RaeThere"))return false;
            // Rae across to him, close enough to take his wrist.
            if(rae!=null&&dan!=null){var to=dan.position().add(rae.position().subtract(dan.position()).normalize().scale(.7));
                if(!LodgingInjury.step(l,rae,to,.05))return true;
                face(rae,LodgingPeople.yawTo(rae,dan.position()));face(dan,LodgingPeople.yawTo(dan,rae.position()));rae.gesture(TAKE);
                for(var p:here)if(!p.isSpectator()&&p.distanceTo(dan)<=LodgingDaniel.HEARD)LodgingHouse.cue(p,LodgingTexts.RAE_TAKES);}
            in.putBoolean("RaeHand",true);go(in,DONE,clock);return true;
        }
        return false;
    }
    /** Pulled out of it: he steps out of the frame and aside, and stands where he can see it. */
    private static boolean pulledOut(ServerLevel l,BlockPos b,CompoundTag in,@Nullable LiteraryActor daniel){
        if(!in.getBoolean("PulledOut")||in.getBoolean("Out")||daniel==null)return false;
        if(LodgingTaken.walk(l,b,in,"OutLeg",daniel,List.of(new Vec3(-7.5,0,-27.2),LodgingReturn.STEP_OUT),Map.of(),.08)){
            in.putBoolean("Out",true);daniel.gesture(LodgingPeople.WAIT);face(daniel,LodgingReturn.DANIEL_OUT.look());
        }
        return true;
    }
    /** The back wall comes in: plaster dust off its whole width, and the boards creak under it. */
    private static void shallower(ServerLevel l,BlockPos b){
        for(double x=-12.5;x<=-3.5;x+=1.5)l.sendParticles(ParticleTypes.WHITE_ASH,b.getX()+x,b.getY()+1+l.random.nextDouble()*1.8,b.getZ()-28.85,3,.3,.4,.05,0);
        l.playSound(null,b.offset(-8,1,-28),SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.4F,.5F);
    }
    /** His chair, a block nearer the wall each quarter; on the last, into it. */
    private static void chair(ServerLevel l,BlockPos b,CompoundTag in,int step){
        var from=b.offset(LodgingSite.DANIEL_CHAIR.getX(),LodgingSite.DANIEL_CHAIR.getY(),LodgingSite.DANIEL_CHAIR.getZ()-in.getInt("ChairSteps"));
        var st=l.getBlockState(from);if(!LodgingThing.is(st,LodgingThing.Kind.CHAIR))return;
        if(step>=4||from.getZ()-1<=b.getZ()+LodgingSite.ROOM_N){
            l.setBlock(from,Blocks.AIR.defaultBlockState(),3);l.playSound(null,from,SoundEvents.WOOD_BREAK,SoundSource.BLOCKS,.6F,.5F);
            l.sendParticles(ParticleTypes.WHITE_ASH,from.getX()+.5,from.getY()+.6,from.getZ()+.2,12,.3,.3,.1,0);in.putInt("ChairSteps",9);return;
        }
        var to=from.north();
        if(!l.getBlockState(to).isAir()||!l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new net.minecraft.world.phys.AABB(to)).isEmpty())return;
        l.setBlock(to,st,3);l.setBlock(from,Blocks.AIR.defaultBlockState(),3);in.putInt("ChairSteps",in.getInt("ChairSteps")+1);
        l.playSound(null,to,SoundEvents.WOOD_STEP,SoundSource.BLOCKS,.8F,.5F);
    }
    /** In his room, or in sight of him: the frame is in the back wall, and by the end he is inside it. */
    private static boolean sees(BlockPos b,ServerPlayer p,LiteraryActor a){return p.distanceTo(a)<=16&&(LodgingSite.inDaniels(LodgingInjury.rel(b,p))||p.hasLineOfSight(a));}
    private static int holders(CompoundTag in,LiteraryActor daniel,List<ServerPlayer> here){
        int near=0;var list=in.getList("Holders",Tag.TAG_STRING);
        for(var p:here){if(p.isSpectator()||!p.isAlive()||p.distanceTo(daniel)>HOLD_REACH)continue;for(int i=0;i<list.size();i++)if(list.getString(i).equals(p.getUUID().toString()))near++;}
        return near;
    }
    private static void say(ServerLevel l,BlockPos b,int who,String line){var a=LodgingPeople.find(l,b,who);if(a!=null)aloud(l,b,a,LodgingTaken.name(who),line);}
    /** Said aloud: over their head, and in the panel of every reader who hears it. */
    private static void aloud(ServerLevel l,BlockPos b,LiteraryActor a,String name,String line){
        for(var p:LodgingHouse.readers(l,b))if(!p.isSpectator()&&p.distanceTo(a)<=LodgingDaniel.HEARD)Talk.line(p,name,line);
        LodgingStory.speak(l,b,a,name,line);
    }
    private static void face(LiteraryActor a,float yaw){a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    private static String zone(BlockPos b,LiteraryActor a){var r=LodgingInjury.rel(b,a);return LodgingSite.inParlor(r)?"parlor":LodgingSite.inDaniels(r)?"daniels":LodgingSite.inHall(r)||LodgingSite.inBackPassage(r)?"hall":"";}
    private static List<Vec3> route(String from){return LodgingReturn.route(from);}
    private static Map<Integer,BlockPos> doors(String from){return LodgingReturn.doors(from);}
    /** One of them along their zone's way to Daniel's room, ending at a spot of their own; nowhere known, and they stay. */
    private static boolean walkTo(ServerLevel l,BlockPos b,CompoundTag in,String key,LiteraryActor a,String from,LodgingPeople.Spot to,double pace){
        if(from.isEmpty())return true;
        var legs=new ArrayList<>(route(from));legs.set(legs.size()-1,new Vec3(to.x(),0,to.z()));
        return LodgingTaken.walk(l,b,in,key,a,legs,doors(from),pace);
    }

    // ------------------------------------------------------------------------------------------------ a hand on Daniel
    /** A hand on Daniel while he is being brought or held. True if this slice answered it. */
    static boolean talk(ServerPlayer p,BlockPos b,LiteraryActor a,int who,CompoundTag own){
        if(who!=LodgingPeople.DANIEL)return false;
        var in=state(LodgingStory.house(p.server,b));int ph=in.getInt("Phase");
        if(ph!=GATHER&&ph!=HOLDING)return false;
        Talk.begin(p,LodgingPeople.TALK+"complete","Daniel");
        var replies=new ArrayList<Talk.Reply>();
        if(holding(in,p))replies.add(Talk.reply(LodgingTexts.R_LET_DANIEL_GO,LodgingComplete::letGo));
        else replies.add(Talk.reply(LodgingTexts.R_HOLD_DANIEL,LodgingComplete::take));
        replies.add(Talk.act(LodgingTexts.R_NOTHING,Talk::close));Talk.ask(p,replies);
        return true;
    }
    private static boolean holding(CompoundTag in,ServerPlayer p){var l=in.getList("Holders",Tag.TAG_STRING);for(int i=0;i<l.size();i++)if(l.getString(i).equals(p.getUUID().toString()))return true;return false;}
    /** "Take his shoulders." */
    private static void take(ServerPlayer p){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null){Talk.close(p);return;}
        var s=p.server;var h=LodgingStory.house(s,b);var in=state(h);int ph=in.getInt("Phase");
        var daniel=LodgingPeople.find(p.serverLevel(),b,LodgingPeople.DANIEL);
        if(daniel==null||ph!=GATHER&&ph!=HOLDING||holding(in,p)){Talk.close(p);return;}
        var list=in.getList("Holders",Tag.TAG_STRING);list.add(StringTag.valueOf(p.getUUID().toString()));in.put("Holders",list);
        var own=LodgingHouse.own(p);own.putBoolean(HELD,true);own.remove(LET_GO);LodgingHouse.save(p,own);
        LodgingHouse.cue(p,LodgingTexts.YOU_HOLD_DANIEL);LodgingPeople.says(p,daniel,"Daniel",LodgingTexts.DANIEL_HELD_BY_YOU);
        LodgingStory.save(s,b,h);Talk.end(p);
    }
    /** "Let go of him." */
    private static void letGo(ServerPlayer p){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null){Talk.close(p);return;}
        var s=p.server;var h=LodgingStory.house(s,b);var in=state(h);
        var list=in.getList("Holders",Tag.TAG_STRING);var kept=new net.minecraft.nbt.ListTag();
        for(int i=0;i<list.size();i++)if(!list.getString(i).equals(p.getUUID().toString()))kept.add(StringTag.valueOf(list.getString(i)));
        in.put("Holders",kept);var own=LodgingHouse.own(p);own.putBoolean(LET_GO,true);LodgingHouse.save(p,own);
        LodgingHouse.cue(p,LodgingTexts.YOU_LET_DANIEL_GO);LodgingStory.save(s,b,h);Talk.end(p);
    }
    /** For a house built again. */
    public static void clear(MinecraftServer s,BlockPos b){LodgingStory.house(s,b).remove(STATE);}
}
