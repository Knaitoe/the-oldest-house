package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.751, the waxworks, the third part (docs/WAXWORKS_0_4_751.md): the shift. One clock for the museum, from eight minutes to midnight
 * to six, two minutes of play an hour, running from the first card punched and while anyone is on the shift.
 *
 * <ul>
 * <li><b>The rounds.</b> Each hour wants a round: every gallery counted. A gallery is counted when the beam of someone on the shift has
 *   been on each figure standing in it this hour; the count goes in the log. The twenty-fourth adds to the Hall's count when the beam
 *   finds him, and a round does not wait for him.</li>
 * <li><b>Irregularities.</b> Every half hour, in a gallery nobody is in and with nobody looking, one figure is turned on its mark to face
 *   the door, or is gone from it. Unnoticed for half an hour, it comes nearer the office: to the ring outside its gallery, to the ring
 *   by the office door, into the office. Somebody on the shift who looks at it (with the light, or close) for a second and a half
 *   notices: it goes in the log, and the figure is back where it was the next time nobody sees it.</li>
 * <li><b>Failure.</b> When three have come into the office, the screens go dark, every flashlight goes out and is gone, and everyone on
 *   the shift is put out of the museum, unfinished. Nothing harms anyone.</li>
 * <li><b>Six.</b> At six, with every hour's round in the log, the shift is kept: a key turns in the front door, and everyone still on the
 *   shift has kept it. Short a round, the shift simply ends.</li>
 * </ul>
 * Shared: the clock, the rounds and counts, the irregularities and the log. Each reader's own record keeps what they counted and noticed.
 * The night begins again once the museum has been empty for ten seconds.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class WaxShift {
    private WaxShift(){}
    /** Ticks: an hour of the night, the eight minutes before the first one, and six o'clock. */
    public static final int HOUR=2400,BEFORE=320,HOURS=6,SIX=BEFORE+HOURS*HOUR;
    /** Irregularities: how often one is made, the first, how long unnoticed before it comes nearer, how many in the office end the night. */
    public static final int EVERY=1200,FIRST=700,NEARER=1200,ARRIVED=3,ENOUGH=3;
    /** Noticing: how near and how straight, and for how long. Counting: how straight the beam must be on a figure. */
    public static final double NOTICE_NEAR=12,NOTICE_ANGLE=12,CLOSE=4,COUNT_ANGLE=14;public static final int NOTICE_HOLD=30,EMPTY=200;
    /** An irregularity: the figure, whether it is turned (0) or gone (1), how near it has come (0 in its gallery, 3 in the office). */
    public static final class Irr{public final int who,kind;public int stage,since;public boolean noticed;public Irr(int who,int kind){this.who=who;this.kind=kind;}}
    public static final class Night{
        public int t,empty,hourSaid=-1;public boolean failed,over,kept,called,police;
        public final Set<UUID> punched=new HashSet<>();
        /** This hour's figures lit, by figure; galleries counted this hour; the hours whose round is in the log; the counts, hour by gallery. */
        public int lit,counted,rounds;public final int[][] counts=new int[HOURS][WaxSite.GALLERIES.size()];
        public final List<Irr> irregular=new ArrayList<>();public int noticed,made;
        public int hour(){return t<BEFORE?0:Math.min(HOURS-1,(t-BEFORE)/HOUR);}
        /** The clock's stage: 0 at 11:52, 1 at twelve, to 7 at six. */
        public int clock(){return t<BEFORE?0:Math.min(7,1+(t-BEFORE)/HOUR);}
        public int arrived(){int n=0;for(var i:irregular)if(i.stage>=ARRIVED&&!i.noticed)n++;return n;}
        public @Nullable Irr of(int who){for(var i:irregular)if(i.who==who)return i;return null;}
    }
    static final Map<Long,Night> NIGHTS=new ConcurrentHashMap<>();
    static final Map<UUID,Integer> LOOKING=new ConcurrentHashMap<>();
    public static @Nullable Night night(BlockPos site){return NIGHTS.get(site.asLong());}
    public static boolean on(ServerPlayer p,BlockPos site){var n=night(site);return n!=null&&n.punched.contains(p.getUUID())&&!n.over;}

    // ------------------------------------------------------------------------------------------------ the time clock
    /** Punching in: the reader is on the shift; the first card starts the night's clock. */
    public static void punch(ServerPlayer p,BlockPos b,CompoundTag own){
        if(p.isSpectator())return;
        var n=NIGHTS.computeIfAbsent(b.asLong(),k->new Night());
        if(n.over){Waxworks.cue(p,WaxTexts.PUNCH_OVER);return;}
        if(!n.punched.add(p.getUUID())){Waxworks.cue(p,WaxTexts.PUNCH_AGAIN);return;}
        own.putBoolean("Punched",true);own.putInt("Shifts",own.getInt("Shifts")+1);own.remove("CalledTonight");own.remove("PoliceTonight");own.remove("CalledAt");
        Waxworks.cue(p,WaxTexts.punched(n.t<BEFORE?"11:52":time(n.t)));
        p.serverLevel().playSound(null,b.offset(WaxSite.TIMECLOCK),WaxRegistry.PUNCH.get(),SoundSource.BLOCKS,.6F,1F);
    }
    public static String time(int t){
        if(t<BEFORE){int m=52+t*8/BEFORE;return "11:"+(m<10?"0":"")+m;}
        int into=t-BEFORE,h=into/HOUR,m=(into%HOUR)*60/HOUR;return (h==0?12:h)+":"+(m<10?"0":"")+m;
    }
    /** The time of day the police are at the front door, when somebody has called them: five o'clock. */
    public static final int POLICE=BEFORE+5*HOUR;
    /** 0.4.753, the desk telephone: somebody on the shift calls the police once a night, and they come at five. */
    public static String phone(ServerPlayer p,BlockPos b,CompoundTag own){
        var n=night(b);
        if(p.isSpectator()||n==null||!n.punched.contains(p.getUUID())||n.over)return WaxTexts.DESK_PHONE;
        if(n.police)return WaxTexts.POLICE_GONE;
        if(n.called)return WaxTexts.CALLED_AGAIN;
        n.called=true;own.putBoolean("Called",true);own.putBoolean("CalledTonight",true);own.putString("CalledAt",n.t<BEFORE?"11:52":time(n.t));
        p.serverLevel().playSound(null,b.offset(WaxSite.PHONE),SoundEvents.UI_BUTTON_CLICK.value(),SoundSource.BLOCKS,.4F,.6F);
        return n.t>=POLICE?WaxTexts.CALLED_LATE:WaxTexts.CALLED;
    }
    /** Five o'clock, called: two officers walk the galleries with the lights up, count twenty-three and go. What was out of place goes home. */
    static void police(ServerLevel l,BlockPos b,Night n,List<ServerPlayer> shift,LabyrinthData data){
        n.police=true;
        for(var i:n.irregular)i.noticed=true;
        l.playSound(null,b.offset(WaxSite.FRONT_LEFT),SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,.8F,.8F);
        for(var p:shift){
            var own=LiteraryVignettes.personal(data,p.getUUID(),Waxworks.PLACE);own.putBoolean("PoliceTonight",true);LiteraryVignettes.save(data,p.getUUID(),Waxworks.PLACE,own);
            Waxworks.cue(p,WaxTexts.POLICE);
        }
    }
    /** The log on the desk: the rounds so far, what has been noticed. */
    public static String log(BlockPos b){
        var n=night(b);if(n==null)return WaxTexts.DESK_LOG;
        return WaxTexts.log(Integer.bitCount(n.rounds),n.noticed,n.kept);
    }

    // ------------------------------------------------------------------------------------------------ where figures are
    /** Where an irregular figure is now: its mark (turned or empty), the ring by its gallery, the ring by the office, the office. */
    public record Place(Vec3 at,float yaw,boolean gone){}
    static final BlockPos[] RING_BY={new BlockPos(0,0,-60),new BlockPos(-15,0,-31),new BlockPos(15,0,-31),new BlockPos(-15,0,-49),new BlockPos(15,0,-49)};
    static final BlockPos[] BY_OFFICE={new BlockPos(-3,0,-20),new BlockPos(3,0,-20),new BlockPos(-6,0,-20),new BlockPos(6,0,-20)};
    static final BlockPos[] IN_OFFICE={new BlockPos(0,0,-16),new BlockPos(-1,0,-15),new BlockPos(1,0,-15),new BlockPos(0,0,-14)};
    /** The cell in front of a gallery's door, inside it, that a turned figure faces. */
    static final BlockPos[] DOOR_IN={new BlockPos(0,0,-63),new BlockPos(-12,0,-31),new BlockPos(12,0,-31),new BlockPos(-12,0,-49),new BlockPos(12,0,-49)};
    public static @Nullable Place place(BlockPos site,int who){
        var n=night(site);if(n==null)return null;var i=n.of(who);if(i==null)return null;
        var f=WaxSite.FIGURES.get(who);int k=n.irregular.indexOf(i);
        return switch(i.stage){
            case 0->{
                if(i.kind==1)yield new Place(WaxSite.feet(f),f.yaw(),true);
                var door=Vec3.atBottomCenterOf(DOOR_IN[f.gallery()]).subtract(WaxSite.feet(f));
                yield new Place(WaxSite.feet(f),(float)(Mth.atan2(door.z,door.x)*Mth.RAD_TO_DEG)-90,false);
            }
            case 1->new Place(Vec3.atBottomCenterOf(RING_BY[f.gallery()]),towardOffice(RING_BY[f.gallery()]),false);
            case 2->new Place(Vec3.atBottomCenterOf(BY_OFFICE[k%BY_OFFICE.length]),towardOffice(BY_OFFICE[k%BY_OFFICE.length]),false);
            default->new Place(Vec3.atBottomCenterOf(IN_OFFICE[k%IN_OFFICE.length]),0,false);
        };
    }
    static boolean facesDoor(WaxSite.Figure f){var door=Vec3.atBottomCenterOf(DOOR_IN[f.gallery()]).subtract(WaxSite.feet(f));return Math.abs(Mth.wrapDegrees((float)(Mth.atan2(door.z,door.x)*Mth.RAD_TO_DEG)-90-f.yaw()))<30;}
    static float towardOffice(BlockPos from){var d=Vec3.atCenterOf(new BlockPos(0,0,-17)).subtract(Vec3.atCenterOf(from));return (float)(Mth.atan2(d.z,d.x)*Mth.RAD_TO_DEG)-90;}
    /** Which gallery a figure stands in now, or -1 when it has left its gallery. */
    static int where(BlockPos site,int who){var n=night(site);var i=n==null?null:n.of(who);return i!=null&&(i.stage>0||i.kind==1)?-1:WaxSite.FIGURES.get(who).gallery();}

    // ------------------------------------------------------------------------------------------------ each tick
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(NIGHTS.isEmpty())return;
        var server=e.getServer();var l=server.getLevel(NovelRooms.dimension(Waxworks.PLACE));if(l==null)return;
        var data=LabyrinthData.get(server);
        for(long s:List.copyOf(NIGHTS.keySet())){
            var b=BlockPos.of(s);var n=NIGHTS.get(s);
            var readers=new ArrayList<ServerPlayer>();var shift=new ArrayList<ServerPlayer>();
            for(var p:l.players())if(p.isAlive()&&!p.isSpectator()&&b.equals(WaxNight.site(p))){readers.add(p);if(n.punched.contains(p.getUUID()))shift.add(p);}
            if(readers.isEmpty()){if(++n.empty>=EMPTY||!Waxworks.SITES.contains(s))reset(l,b);continue;}
            n.empty=0;
            if(n.over||shift.isEmpty())continue;
            n.t++;
            if(n.t%20==0)clock(l,b,n,shift);
            if(n.called&&!n.police&&n.t>=POLICE)police(l,b,n,shift,data);
            if(n.t%5==0){count(l,b,n,shift,data);notice(l,b,n,shift,data);}
            if(n.t%20==10)move(l,b,n,readers);
            if(n.t>=FIRST&&!n.police&&n.t%20==15&&n.made<1+(n.t-FIRST)/EVERY&&n.t<SIX-EVERY)make(l,b,n,readers);
            if(n.arrived()>=ENOUGH){fail(l,b,n,shift,data);continue;}
            if(n.t>=SIX)six(l,b,n,shift,data);
        }
    }
    /** Counting and noticing once, now, for whoever is on the shift (the tests' way of not waiting for the next fifth tick). */
    public static void step(ServerLevel l,BlockPos b){
        var n=night(b);if(n==null)return;var shift=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(p.isAlive()&&!p.isSpectator()&&b.equals(WaxNight.site(p))&&n.punched.contains(p.getUUID()))shift.add(p);
        var data=LabyrinthData.get(l.getServer());count(l,b,n,shift,data);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){NIGHTS.clear();LOOKING.clear();}

    /** The office clock, and each hour said once to whoever is on the shift. */
    static void clock(ServerLevel l,BlockPos b,Night n,List<ServerPlayer> shift){
        var at=b.offset(WaxSite.CLOCK);var st=l.getBlockState(at);
        if(WaxBlock.is(st,WaxBlock.Kind.CLOCK)&&st.getValue(WaxBlock.STAGE)!=n.clock())l.setBlock(at,st.setValue(WaxBlock.STAGE,n.clock()),Block.UPDATE_CLIENTS);
        if(n.t>=BEFORE&&n.hourSaid!=n.hour()){n.hourSaid=n.hour();n.lit=0;n.counted=0;for(var p:shift)Waxworks.cue(p,WaxTexts.hour(n.hour()));}
    }

    /** Counting: the beam of someone on the shift on each figure of a gallery, this hour. */
    static void count(ServerLevel l,BlockPos b,Night n,List<ServerPlayer> shift,LabyrinthData data){
        int hour=n.hour();
        for(var p:shift){
            if(!WaxNight.holding(p))continue;
            int g=WaxSite.gallery(p.position().subtract(b.getX(),b.getY(),b.getZ()));if(g<0)continue;
            var own=LiteraryVignettes.personal(data,p.getUUID(),Waxworks.PLACE);boolean changed=false;
            for(int who=0;who<Waxworks.FIGURES;who++){
                if(where(b,who)!=g||(n.lit&1<<who)!=0)continue;
                var a=Waxworks.find(l,b,who);if(a==null||a.isInvisible())continue;
                if(!lit(l,p,a.position().add(0,a.getBbHeight()*.6,0))&&!lit(l,p,a.getEyePosition()))continue;
                n.lit|=1<<who;own.putInt("Lit",own.getInt("Lit")|1<<who);changed=true;
                if(who==WaxSite.STRANGER){own.putBoolean("FoundTwentyFourth",true);if((n.counted&1<<g)!=0){n.counts[hour][g]=Integer.bitCount(n.lit&galleryMask(g,true));Waxworks.cue(p,WaxTexts.counted(g,n.counts[hour][g]));}}
            }
            if((n.counted&1<<g)==0){
                int need=0;for(int who=0;who<Waxworks.FIGURES;who++)if(who!=WaxSite.STRANGER&&where(b,who)==g)need|=1<<who;
                if((n.lit&need)==need){
                    n.counted|=1<<g;n.counts[hour][g]=Integer.bitCount(n.lit&galleryMask(g,true));
                    own.putInt("Galleries",own.getInt("Galleries")+1);changed=true;
                    Waxworks.cue(p,WaxTexts.counted(g,n.counts[hour][g]));
                    if(n.counted==(1<<WaxSite.GALLERIES.size())-1&&(n.rounds&1<<hour)==0){n.rounds|=1<<hour;for(var q:shift)Waxworks.cue(q,WaxTexts.round(hour));}
                }
            }
            if(changed)LiteraryVignettes.save(data,p.getUUID(),Waxworks.PLACE,own);
        }
    }
    static boolean lit(ServerLevel l,ServerPlayer p,Vec3 at){return p.getEyePosition().distanceTo(at)<=WaxNight.BEAM+1&&WaxNight.off(p,at)<=COUNT_ANGLE&&WaxNight.clear(l,p,at);}
    static int galleryMask(int g,boolean withStranger){int m=0;for(int who=0;who<Waxworks.FIGURES;who++)if(WaxSite.FIGURES.get(who).gallery()==g&&(withStranger||who!=WaxSite.STRANGER))m|=1<<who;return m;}

    /** Whether anyone in the museum could see this point. */
    static boolean seen(ServerLevel l,List<ServerPlayer> readers,Vec3 at){
        for(var p:readers){double d=p.getEyePosition().distanceTo(at);if(d<3||d<24&&WaxNight.off(p,at)<70&&WaxNight.clear(l,p,at))return true;}
        return false;
    }
    /** A new irregularity, in a gallery nobody is in, on a figure nobody can see. */
    static void make(ServerLevel l,BlockPos b,Night n,List<ServerPlayer> readers){
        var taken=new HashSet<Integer>();for(var i:n.irregular)taken.add(i.who);
        var occupied=new HashSet<Integer>();for(var p:readers)occupied.add(WaxSite.gallery(p.position().subtract(b.getX(),b.getY(),b.getZ())));
        var can=new ArrayList<Integer>();
        for(int who=0;who<Waxworks.FIGURES;who++){
            var f=WaxSite.FIGURES.get(who);
            if(who==WaxSite.STRANGER||who==WaxSite.MIRROR||f.pose()==WaxSite.COVERED||taken.contains(who)||occupied.contains(f.gallery())||WaxMonitors.watched(b,f.gallery()))continue;
            var a=Waxworks.find(l,b,who);if(a==null||seen(l,readers,a.getEyePosition()))continue;
            can.add(who);
        }
        if(can.isEmpty())return;
        int who=can.get(l.random.nextInt(can.size()));
        // 0.4.753: one already facing its gallery's door cannot be seen to turn to it, so it is gone from its plinth instead.
        int kind=l.random.nextInt(3)==0||facesDoor(WaxSite.FIGURES.get(who))?1:0;
        n.irregular.add(new Irr(who,kind));n.made++;WaxNight.unturn(b,who);
        var a=Waxworks.find(l,b,who);if(a!=null)Waxworks.stand(a,b,WaxSite.FIGURES.get(who),0);
    }
    /** Unnoticed ones come nearer, unseen; noticed ones go home, unseen. */
    static void move(ServerLevel l,BlockPos b,Night n,List<ServerPlayer> readers){
        for(var it=n.irregular.iterator();it.hasNext();){
            var i=it.next();var a=Waxworks.find(l,b,i.who);if(a==null)continue;
            var f=WaxSite.FIGURES.get(i.who);
            int feed=WaxMonitors.feedOf(i.who,i.stage),nextFeed=WaxMonitors.feedOf(i.who,Math.min(ARRIVED,i.stage+1));
            if(feed>=0&&WaxMonitors.watched(b,feed)||!i.noticed&&nextFeed>=0&&WaxMonitors.watched(b,nextFeed))continue;
            if(i.noticed){
                if(!seen(l,readers,a.getEyePosition())&&!seen(l,readers,WaxSite.feet(f).add(b.getX(),b.getY()+1.6,b.getZ()))){it.remove();WaxNight.unturn(b,i.who);Waxworks.stand(a,b,f,0);}
                continue;
            }
            i.since+=20;if(i.since<NEARER||i.stage>=ARRIVED)continue;
            i.stage++;var next=place(b,i.who);i.stage--;
            if(next==null||seen(l,readers,a.getEyePosition())||seen(l,readers,next.at().add(b.getX(),b.getY()+1.6,b.getZ())))continue;
            i.stage++;i.since=0;WaxNight.unturn(b,i.who);Waxworks.stand(a,b,f,0);
            if(i.stage>=ARRIVED)l.playSound(null,b.offset(0,1,-17),WaxRegistry.MONITORS.get(),SoundSource.BLOCKS,.4F,.6F);
        }
    }
    /** Noticing: somebody on the shift looks at an irregular figure (or the empty plinth) long enough. */
    static void notice(ServerLevel l,BlockPos b,Night n,List<ServerPlayer> shift,LabyrinthData data){
        for(var p:shift){
            Irr seen=null;
            for(var i:n.irregular){
                if(i.noticed)continue;var pl=place(b,i.who);if(pl==null)continue;
                var at=pl.at().add(b.getX(),b.getY()+(pl.gone()?1:1.5),b.getZ());double d=p.getEyePosition().distanceTo(at);
                if(d>NOTICE_NEAR||WaxNight.off(p,at)>NOTICE_ANGLE||!WaxNight.clear(l,p,at))continue;
                if(!WaxNight.holding(p)&&d>CLOSE&&i.stage<ARRIVED)continue;
                seen=i;break;
            }
            if(seen==null){LOOKING.remove(p.getUUID());continue;}
            int key=seen.who+1;int had=LOOKING.getOrDefault(p.getUUID(),0);
            int ticks=had/100==key?had%100+5:5;
            if(ticks<NOTICE_HOLD){LOOKING.put(p.getUUID(),key*100+ticks);continue;}
            LOOKING.remove(p.getUUID());
            noticed(p,n,seen,false);
        }
    }

    /** Noticed, in person or on a monitor: in the log and on the reader's record. */
    static void noticed(ServerPlayer p,Night n,Irr i,boolean onScreen){
        i.noticed=true;n.noticed++;
        var data=LabyrinthData.get(p.server);
        var own=LiteraryVignettes.personal(data,p.getUUID(),Waxworks.PLACE);own.putInt("Noticed",own.getInt("Noticed")+1);if(onScreen)own.putInt("NoticedOnScreen",own.getInt("NoticedOnScreen")+1);
        LiteraryVignettes.save(data,p.getUUID(),Waxworks.PLACE,own);
        Waxworks.cue(p,WaxTexts.noticed(i.stage,i.kind));
    }
    /** Three in the office: the screens go dark, the lights go out, and the shift is put out of the museum. Nobody is harmed. */
    static void fail(ServerLevel l,BlockPos b,Night n,List<ServerPlayer> shift,LabyrinthData data){
        n.failed=true;n.over=true;screens(l,b,0);
        for(var p:shift)if(WaxMonitors.WATCHING.containsKey(p.getUUID()))io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new io.github.knaitoe.theoldesthouse.network.WaxMonitorPayload(b,true,time(n.t)));
        l.playSound(null,b.offset(0,1,-14),SoundEvents.BEACON_DEACTIVATE,SoundSource.BLOCKS,.6F,.5F);
        for(var p:shift){
            WaxNight.reclaim(p);WaxNight.unbeam(l,p.getUUID());
            var own=LiteraryVignettes.personal(data,p.getUUID(),Waxworks.PLACE);own.putBoolean("Failed",true);own.putInt("Fails",own.getInt("Fails")+1);LiteraryVignettes.save(data,p.getUUID(),Waxworks.PLACE,own);
            Waxworks.cue(p,WaxTexts.FAILED);
            LabyrinthDoors.sendBack(p,30,40,30);
        }
    }
    /** Six o'clock: kept, with every round in the log; or short, and simply over. */
    static void six(ServerLevel l,BlockPos b,Night n,List<ServerPlayer> shift,LabyrinthData data){
        n.over=true;n.kept=n.rounds==(1<<HOURS)-1;
        var at=b.offset(WaxSite.CLOCK);var st=l.getBlockState(at);if(WaxBlock.is(st,WaxBlock.Kind.CLOCK))l.setBlock(at,st.setValue(WaxBlock.STAGE,7),Block.UPDATE_CLIENTS);
        if(n.kept)l.playSound(null,b.offset(WaxSite.FRONT_LEFT),SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.BLOCKS,.8F,.7F);
        for(var p:shift){
            var own=LiteraryVignettes.personal(data,p.getUUID(),Waxworks.PLACE);
            if(n.kept){
                own.putBoolean("Kept",true);own.putInt("Rounds",Integer.bitCount(n.rounds));own.putInt("NoticedTogether",n.noticed);own.remove("Short");
                // 0.4.753: a kept shift readies the account, the log on the desk.
                LiteraryVignettes.ready(p,Waxworks.PLACE,own,own.getBoolean("CalledTonight")?"called_the_police":"kept_the_shift");
            }
            else own.putBoolean("Short",true);
            LiteraryVignettes.save(data,p.getUUID(),Waxworks.PLACE,own);
            Waxworks.cue(p,n.kept?WaxTexts.KEPT:WaxTexts.SHORT);
        }
    }
    static void screens(ServerLevel l,BlockPos b,int stage){
        for(var m:WaxSite.MONITORS){var at=b.offset(m);var st=l.getBlockState(at);if(WaxBlock.is(st,WaxBlock.Kind.MONITOR)&&st.getValue(WaxBlock.STAGE)!=stage)l.setBlock(at,st.setValue(WaxBlock.STAGE,stage),Block.UPDATE_CLIENTS);}
    }
    /** The museum empty: the night is forgotten, the figures go back, the screens come on, the clock goes back to eight minutes to. */
    public static void forget(ServerLevel l,BlockPos b){reset(l,b);}
    static void reset(ServerLevel l,BlockPos b){
        var n=NIGHTS.remove(b.asLong());if(n==null)return;
        if(l.isLoaded(b.offset(WaxSite.CLOCK))){
            screens(l,b,1);
            var at=b.offset(WaxSite.CLOCK);var st=l.getBlockState(at);if(WaxBlock.is(st,WaxBlock.Kind.CLOCK))l.setBlock(at,st.setValue(WaxBlock.STAGE,0),Block.UPDATE_CLIENTS);
        }
        for(var i:n.irregular){WaxNight.unturn(b,i.who);var a=Waxworks.find(l,b,i.who);if(a!=null)Waxworks.stand(a,b,WaxSite.FIGURES.get(i.who),0);}
    }
}
