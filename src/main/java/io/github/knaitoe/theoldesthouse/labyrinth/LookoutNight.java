package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.202, the Whistlers in the Pines (docs/WHISTLERS_0_4_202.md): the four nights in the fire lookout.
 *
 * <p>One rule runs every night. Something in the woods whistles from one side of the cab: the pendulum swings toward that
 * wall, the lamp over its window lights and frost ripples on its glass. A reader in the cab who looks toward that wall while
 * its blind is up (or torn, or the glass gone) draws it in, a tier at a time: from the treeline to the shed roof, to the
 * stair frame, to the catwalk glass; a look at the glass breaks it, and the night is over. Looking down is always safe, and
 * the cord for each blind hangs low on the wall where a reader with their eyes on the boards can find it.
 *
 * <ol>
 * <li>Orientation: Seven welcomes the reader on the tape; the reader checks in on the radio and logs three flares by bearing
 * with the fire finder; two whistles, north and west, teach the blinds.</li>
 * <li>The blizzard outage: no power, no heat, frost coming in. The crank from its hook, down the stair to the shed, prime and
 * crank the diesel, and back up. On the stair the landing lanterns turn cold when it whistles there, and a reader outside
 * the cab must be down behind the rail until they burn again.</li>
 * <li>The whiteout: the gale has torn three blinds and there are two tarps. Six whistles round the walls are survived by
 * moving the tarps to whichever torn frame it whistles from, eyes down. Three knocks on the roof hatch end it. The tape
 * starts printing what the reader does.</li>
 * <li>The forest of towers: the reader comes to in tower four with nothing under its stilts and eight lookouts round it at
 * cab height, a figure in seven of them doing what the reader does. The north one stands still with binoculars. Locking the
 * fire finder on it (bearing 000) opens the hatch, and the hatch lets the reader down into tower four's own cab, where the
 * fire log waits.</li>
 * </ol>
 *
 * <p>The tower is one for everyone in it (a party shares a copy, unrelated readers have their own): the whistle wall, the
 * tier, the blinds, the power and the tarps are the tower's, and any reader's look raises the one tier. Each reader's own
 * record keeps what they did. A night's events are for the reader whose night it is (the first reader in, by their own
 * visit); anyone else in the tower keeps them company and is not credited for it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LookoutNight {
    private LookoutNight(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.WHISTLERS;
    public static final String STATE="lookout_0202";
    /** How long a whistle lasts, how often it sounds within that, and how close a look has to be to count (the dot of the look on the wall's outward direction). */
    public static final int WHISTLE_TICKS=100,WHISTLE_EVERY=20,STAIR_TICKS=60;
    public static final double LOOK_DOT=.64,LOOK_DOWN=45;
    /** The tier backs off by one after this long without a look. */
    public static final int TIER_EASE=900;
    /** The flares' bearings on the first night; a bearing holds when within this of one for this many ticks. */
    public static final int[] FLARES={47,158,291};
    public static final double TOLERANCE=1.5;public static final int STEADY=10;
    /** Turns of the crank, within this long, that start the diesel; the frost creeps every so often with the power off. */
    public static final int TURNS=4,TURN_WINDOW=120,FROST_EVERY=200;
    /** The third night's six whistles, by wall (north, east, west, north, west, east), and the gap before each. */
    public static final int[] ROTATION={0,1,3,0,3,1};public static final int PHASE_GAP=120,PHASE_TICKS=160;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final Map<Long,Set<UUID>> PRESENT=new HashMap<>();

    // ------------------------------------------------------------------------------------------------ state
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    public static CompoundTag tower(MinecraftServer s,BlockPos b){return LabyrinthData.get(s).stateEntry(STATE,Long.toString(b.asLong()));}
    static void tower(MinecraftServer s,BlockPos b,CompoundTag t){LabyrinthData.get(s).setStateEntry(STATE,Long.toString(b.asLong()),t);}
    public static boolean done(CompoundTag own){return own.getBoolean("Ready")||own.getBoolean("Completed");}
    /** The night a reader is on: their own visit, one to four. */
    public static int night(CompoundTag own){return Math.max(1,Math.min(4,own.getInt("Visit")));}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);quiet(p,60);}
    /** A line of Seven's tape: on the reader's bar in the teleprinter's own colour, and kept on their record. */
    static void tape(ServerLevel l,BlockPos b,List<ServerPlayer> readers,String line){
        for(var p:readers){p.displayClientMessage(Component.literal("TTY  "+line).withStyle(ChatFormatting.GREEN),true);quiet(p,80);var own=own(p);LookoutTexts.keep(own,line);save(p,own);}
        LookoutTower.stage(l,b,LookoutTower.TELEPRINTER,1);l.playSound(null,b.offset(LookoutTower.TELEPRINTER),LookoutRegistry.TTY.get(),SoundSource.BLOCKS,.8F,1F);
        var t=tower(l.getServer(),b);t.putInt("TtyLit",t.getInt("Clock")+40);t.putString("LastTape",line);tower(l.getServer(),b,t);
    }
    private static final Map<UUID,Long> QUIET=new HashMap<>();
    private static void quiet(ServerPlayer p,int ticks){QUIET.put(p.getUUID(),p.serverLevel().getGameTime()+ticks);}
    private static boolean barFree(ServerPlayer p){return p.serverLevel().getGameTime()>=QUIET.getOrDefault(p.getUUID(),0L);}
    static void sound(ServerLevel l,Vec3 at,SoundEvent s,float volume,float pitch){l.playSound(null,at.x,at.y,at.z,s,SoundSource.AMBIENT,volume,pitch);}
    private static Vec3 at(BlockPos b,Vec3 rel){return Vec3.atLowerCornerOf(b).add(rel);}
    private static Vec3 rel(BlockPos b,Vec3 abs){return abs.subtract(Vec3.atLowerCornerOf(b));}
    static boolean inMainCab(ServerPlayer p,BlockPos b){return LookoutTower.inCab(rel(b,p.position()),LookoutTower.CZ);}
    static boolean inVoidCab(ServerPlayer p,BlockPos b){return LookoutTower.inCab(rel(b,p.position()),LookoutTower.VZ);}
    static boolean inVoid(ServerPlayer p,BlockPos b){return p.getZ()-b.getZ()<LookoutTower.VZ+60;}
    static int[] normal(int wall){return switch(wall){case 0->new int[]{0,-1};case 1->new int[]{1,0};case 2->new int[]{0,1};default->new int[]{-1,0};};}
    /** Whether a reader is looking toward a wall: their horizontal look within the counted angle of its outward direction, and not down at the floor. */
    public static boolean looking(ServerPlayer p,int wall){
        if(p.getXRot()>=LOOK_DOWN)return false;var v=p.getLookAngle();double h=Math.hypot(v.x,v.z);if(h<1e-4)return false;int[] n=normal(wall);
        return (v.x*n[0]+v.z*n[1])/h>LOOK_DOT;
    }
    public static int bearing(float yaw){return Math.floorMod(Math.round(yaw+180),360);}
    static double bearingOf(float yaw){double b=(yaw+180)%360;return b<0?b+360:b;}
    static double off(double a,double target){double d=Math.abs(a-target)%360;return d>180?360-d:d;}

    // ------------------------------------------------------------------------------------------------ arrival
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        own.putBoolean("Alidade202",false);own.putInt("Steady202",0);
    }

    // ------------------------------------------------------------------------------------------------ every tick
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();var l=s.getLevel(HouseDimensions.OUTSIDE);if(l==null)return;
        var towers=new HashMap<Long,List<ServerPlayer>>();
        for(var p:s.getPlayerList().getPlayers()){
            if(p.serverLevel()!=l||!LiteraryVignettes.inside(p,PLACE))continue;var b=LiteraryVignettes.base(p,PLACE);if(b==null)continue;
            towers.computeIfAbsent(b.asLong(),k->new ArrayList<>()).add(p);
        }
        for(var entry:towers.entrySet())tick(l,BlockPos.of(entry.getKey()),entry.getValue());
        // A tower everyone has left is set for its next night.
        for(var it=PRESENT.entrySet().iterator();it.hasNext();){var t=it.next();if(!towers.containsKey(t.getKey())){reset(l,BlockPos.of(t.getKey()),t.getValue());it.remove();}}
        for(var entry:towers.entrySet()){var ids=new HashSet<UUID>();for(var p:entry.getValue())ids.add(p.getUUID());PRESENT.put(entry.getKey(),ids);}
        // The crank and the tarps belong to the tower: nobody carries them out of it.
        if(s.getTickCount()%20==0)for(var p:s.getPlayerList().getPlayers())if(!LiteraryVignettes.inside(p,PLACE))strip(p);
    }
    static void strip(ServerPlayer p){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var st=inv.getItem(i);if(st.is(LookoutRegistry.CRANK_ITEM.get())||st.is(LookoutRegistry.TARP_ITEM.get()))inv.setItem(i,ItemStack.EMPTY);}}

    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> all){
        var s=l.getServer();var t=tower(s,b);
        var readers=all.stream().filter(LiteraryVignettes::participant).toList();
        if(readers.isEmpty())return;
        if(t.getInt("Night")==0)begin(l,b,t,readers);
        int night=t.getInt("Night");
        boolean started=t.getBoolean("Started");
        if(!started){for(var p:readers)if(night==4?inVoidCab(p,b):inMainCab(p,b)){started=true;t.putBoolean("Started",true);break;}}
        if(started&&!t.getBoolean("Over"))t.putInt("Clock",t.getInt("Clock")+1);
        int clock=t.getInt("Clock");
        if(night==4)forest(l,b,t,readers);
        for(var p:readers)alidade(l,b,t,p,night);
        if(clock%5==0&&started&&!t.getBoolean("Over")&&night>0){
            switch(night){case 1->first(l,b,t,readers);case 2->second(l,b,t,readers);case 3->third(l,b,t,readers);default->{}}
            if(night<=3){whistle(l,b,t,readers);stair(l,b,t,readers);whistler(l,b,t,readers);}
            if(t.getInt("TtyLit")>0&&clock>=t.getInt("TtyLit")){LookoutTower.stage(l,b,LookoutTower.TELEPRINTER,t.getBoolean("Power")?0:2);t.putInt("TtyLit",0);}
            weather(l,b,t,readers,night);
        }
        tower(s,b,t);
    }

    /** The first reader into an empty tower decides the night: their own visit. The tower is set for it. */
    static void begin(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> readers){
        int night=-1;for(var p:readers){var own=own(p);if(done(own))continue;night=Math.max(night,night(own));}
        t.putInt("Night",night<0?-1:night);t.putInt("Clock",0);t.putBoolean("Started",false);t.putInt("Tier",0);t.putInt("Wall",-1);t.putInt("LastWall",0);t.putInt("Step",0);
        t.putBoolean("Power",night!=2);t.putBoolean("Over",false);t.putInt("Flare",-1);t.putBoolean("Crank",false);t.putBoolean("Primed",false);t.putInt("Turns",0);
        t.putInt("Frost",0);t.putInt("Stair",-1);t.putBoolean("Locked",false);
        for(int w=0;w<4;w++)LookoutTower.setWall(l,b,w,LookoutTower.CZ,night==3&&w!=2?LookoutBlock.Kind.TORN:LookoutBlock.Kind.PANE,night==3&&w!=2?1:0);
        power(l,b,night!=2);
        LookoutTower.stage(l,b,LookoutTower.CRANK_HOOK,0);
        for(var at:LookoutTower.TARPS)if(night==3)LookoutTower.kind(l,b,at,LookoutBlock.Kind.TARP_PILE,Direction.NORTH,0);else if(LookoutBlock.is(l.getBlockState(b.offset(at)),LookoutBlock.Kind.TARP_PILE))l.setBlock(b.offset(at),Blocks.AIR.defaultBlockState(),F);
        for(var lt:LookoutTower.LANDINGS)LookoutTower.facing(l,b,LookoutTower.lantern(lt),Direction.NORTH,0);
        // The forest of towers is lit only on its own night.
        voidLights(l,b,night==4);
        for(var c:LookoutTower.TRAPDOORS)l.setBlock(b.offset(c.getX(),c.getY(),LookoutTower.VZ+(c.getZ()-LookoutTower.CZ)),LookoutBlock.of(LookoutBlock.Kind.HATCH,Direction.NORTH,0),F);
        LookoutTower.clearWindows(l,b,LookoutTower.VZ);
    }
    static void power(ServerLevel l,BlockPos b,boolean on){
        LookoutTower.stage(l,b,LookoutTower.LAMP,on?0:1);LookoutTower.stage(l,b,LookoutTower.TELEPRINTER,on?0:2);LookoutTower.stage(l,b,LookoutTower.STOVE,on?0:1);
        LookoutTower.stage(l,b,LookoutTower.GENERATOR,on?1:0);LookoutTower.stage(l,b,LookoutTower.SHED_LAMP,on?0:1);
    }
    static void voidLights(ServerLevel l,BlockPos b,boolean on){
        var lamp=LookoutTower.LAMP;LookoutTower.stage(l,b,new BlockPos(lamp.getX(),lamp.getY(),LookoutTower.VZ+(lamp.getZ()-LookoutTower.CZ)),on?0:1);
        for(int k=0;k<8;k++){var c=LookoutTower.ring(k);LookoutTower.stage(l,b,new BlockPos(c.getX()+(lamp.getX()-LookoutTower.CX),lamp.getY(),c.getZ()+(lamp.getZ()-LookoutTower.CZ)),on?0:1);}
    }
    /** An emptied tower: the blinds, the glass, the power, the crank, the tarps and the lamps as the next night needs them; no figures in the forest. */
    static void reset(ServerLevel l,BlockPos b,Set<UUID> was){
        var s=l.getServer();var t=tower(s,b);
        for(var id:was){var p=s.getPlayerList().getPlayer(id);dropMirrors(s,id);if(p!=null){var own=own(p);own.putBoolean("Alidade202",false);save(p,own);}}
        var whistler=whistler(s,t,l);if(whistler!=null)park(whistler,b);
        tower(s,b,new CompoundTag());
        for(int w=0;w<4;w++)LookoutTower.setWall(l,b,w,LookoutTower.CZ,LookoutBlock.Kind.PANE,0);
        power(l,b,true);voidLights(l,b,false);LookoutTower.stage(l,b,LookoutTower.CRANK_HOOK,0);LookoutTower.stage(l,b,LookoutTower.PENDULUM,0);
        for(int w=0;w<4;w++)LookoutTower.stage(l,b,LookoutTower.quadrant(w,LookoutTower.CZ),0);
        for(var at:LookoutTower.TARPS)if(LookoutBlock.is(l.getBlockState(b.offset(at)),LookoutBlock.Kind.TARP_PILE))l.setBlock(b.offset(at),Blocks.AIR.defaultBlockState(),F);
    }

    // ------------------------------------------------------------------------------------------------ the nights
    /** Whose night it is: the readers whose own visit is the tower's night and who have not finished. */
    static List<ServerPlayer> keepers(CompoundTag t,List<ServerPlayer> readers){int night=t.getInt("Night");return readers.stream().filter(p->{var own=own(p);return !done(own)&&night(own)==night;}).toList();}
    static void beat(CompoundTag t,List<ServerPlayer> readers,String flag){for(var p:keepers(t,readers)){var own=own(p);own.putInt("Beat",own.getInt("Visit"));own.putBoolean(flag,true);save(p,own);}}
    static int since(CompoundTag t){return t.getInt("Clock")-t.getInt("StepAt");}
    static void step(CompoundTag t,int step){t.putInt("Step",step);t.putInt("StepAt",t.getInt("Clock"));}

    /** The first night: the welcome, the check-in, three flares and two whistles. */
    static void first(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> readers){
        int step=t.getInt("Step"),clock=t.getInt("Clock");
        switch(step){
            case 0->{if(clock>=40){tape(l,b,readers,LookoutTexts.WELCOME);step(t,1);}}
            case 1->{if(t.getBoolean("CheckedIn")){tape(l,b,readers,LookoutTexts.CHECKED_IN);step(t,2);}}
            case 2,5,8->{if(since(t)>=(step==2?60:200)&&t.getInt("Wall")<0){int i=step==2?0:step==5?1:2;t.putInt("Flare",i);t.putInt("FlareAt",clock);flare(l,b,i);tape(l,b,readers,LookoutTexts.flare(i));step(t,step+1);}}
            case 3,6->{if(t.getInt("Flare")<0)step(t,step+1);}
            case 4,7->{if(since(t)>=80){startWhistle(l,b,t,readers,step==4?0:3);step(t,step+1);}}
            case 9->{if(t.getInt("Flare")<0){tape(l,b,readers,LookoutTexts.THREE);beat(t,readers,"Logged202");step(t,10);}}
            default->{}
        }
        // An unlogged flare goes up again now and then.
        if(t.getInt("Flare")>=0&&clock-t.getInt("FlareAt")>=160){t.putInt("FlareAt",clock);flare(l,b,t.getInt("Flare"));}
    }
    /** The second night: the dark cab, the crank, the stair, the shed and back. */
    static void second(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> readers){
        int step=t.getInt("Step");
        switch(step){
            case 0->{for(var p:readers)cue(p,"The bulb is dark and the stove is cold. Frost is coming in at the edges of the glass. The crank hangs by the hatch.");step(t,1);}
            case 1->{if(t.getBoolean("Crank"))step(t,2);}
            case 2->{if(t.getBoolean("Power")){tape(l,b,readers,LookoutTexts.POWER);step(t,3);}}
            case 3->{for(var p:keepers(t,readers))if(inMainCab(p,b)){tape(l,b,readers,LookoutTexts.WARM);beat(t,readers,"Powered202");step(t,4);break;}}
            default->{}
        }
        // While the power is off the frost comes in from the edges of every pane; once the stove is lit it goes.
        if(t.getInt("Clock")%FROST_EVERY==0){int frost=Mth.clamp(t.getInt("Frost")+(t.getBoolean("Power")?-1:1),0,3);if(frost!=t.getInt("Frost")){t.putInt("Frost",frost);frost(l,b,frost);}}
        // In the cab with the power off, it whistles at the glass now and then.
        if(step>=1&&step<=2&&t.getInt("Wall")<0&&t.getInt("Stair")<0&&readers.stream().allMatch(p->inMainCab(p,b))&&t.getInt("Clock")>=t.getInt("NextCab")){
            startWhistle(l,b,t,readers,l.getRandom().nextInt(4));t.putInt("NextCab",t.getInt("Clock")+260+l.getRandom().nextInt(100));
        }
    }
    /** The third night: the torn blinds, the two tarps, six whistles round the walls, three knocks on the hatch. */
    static void third(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> readers){
        int step=t.getInt("Step");
        if(step==0){tape(l,b,readers,LookoutTexts.STORM);for(var p:readers)cue(p,"The gale has torn three of the blinds away. Two tarps are folded by the stove.");step(t,1);return;}
        if(step>=1&&step<=ROTATION.length){
            if(t.getInt("Wall")<0&&since(t)>=PHASE_GAP+(t.getBoolean("PhaseRan")?PHASE_TICKS:0)){
                if(t.getBoolean("PhaseRan")){t.putBoolean("PhaseRan",false);step(t,step+1);if(step==2||step==4)tape(l,b,readers,LookoutTexts.WRONG[step/2-1]);return;}
                startWhistle(l,b,t,readers,ROTATION[step-1]);t.putInt("WhistleEnd",t.getInt("Clock")+PHASE_TICKS);t.putBoolean("PhaseRan",true);t.putInt("StepAt",t.getInt("Clock"));
            }
            return;
        }
        if(step==ROTATION.length+1){
            int knocks=t.getInt("Knocks");
            if(since(t)>=40+knocks*20&&knocks<3){
                var hatch=at(b,Vec3.atCenterOf(LookoutTower.HATCH));sound(l,hatch,LookoutRegistry.KNOCK.get(),1.2F,.9F+knocks*.05F);
                l.sendParticles(ParticleTypes.SNOWFLAKE,hatch.x,hatch.y-.6,hatch.z,18,.5,.05,.5,.01);t.putInt("Knocks",knocks+1);
            }
            if(t.getInt("Knocks")>=3&&since(t)>=140){tape(l,b,readers,LookoutTexts.DAWN);beat(t,readers,"Tarps202");step(t,ROTATION.length+2);}
        }
    }

    // ------------------------------------------------------------------------------------------------ the whistle
    static void startWhistle(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> readers,int wall){
        t.putInt("Wall",wall);t.putInt("LastWall",wall);t.putInt("WhistleEnd",t.getInt("Clock")+WHISTLE_TICKS);t.putBoolean("Failed",false);t.putInt("WhistleNext",t.getInt("Clock"));
        LookoutTower.facing(l,b,LookoutTower.PENDULUM,LookoutTower.WALLS[wall],1);LookoutTower.stage(l,b,LookoutTower.quadrant(wall,LookoutTower.CZ),1);
        for(var p:readers){var own=own(p);if(!own.getBoolean("Hinted202")){own.putBoolean("Hinted202",true);save(p,own);cue(p,"A whistle, from the "+LookoutTower.WALL_NAMES[wall]+". Eyes on the floor. Find that wall's cord.");}}
    }
    static void endWhistle(ServerLevel l,BlockPos b,CompoundTag t){
        int wall=t.getInt("Wall");if(wall<0)return;t.putInt("Wall",-1);
        LookoutTower.stage(l,b,LookoutTower.PENDULUM,0);LookoutTower.stage(l,b,LookoutTower.quadrant(wall,LookoutTower.CZ),0);
    }
    static Vec3 wallOutside(BlockPos b,int wall,double out){int[] n=normal(wall);return at(b,new Vec3(LookoutTower.CX+.5+n[0]*out,22,LookoutTower.CZ+.5+n[1]*out));}
    static void whistle(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> readers){
        int wall=t.getInt("Wall"),clock=t.getInt("Clock");
        if(t.getInt("Tier")>0&&clock-t.getInt("TierAt")>=TIER_EASE){t.putInt("Tier",t.getInt("Tier")-1);t.putInt("TierAt",clock);}
        if(wall<0)return;
        if(clock>=t.getInt("WhistleEnd")){endWhistle(l,b,t);return;}
        if(clock>=t.getInt("WhistleNext")){
            t.putInt("WhistleNext",clock+WHISTLE_EVERY);var from=anchor(b,t.getInt("Tier"),wall);
            sound(l,from,LookoutRegistry.WHISTLE.get(),t.getInt("Tier")>=2?1.4F:2.2F,1F);
            // Frost rings on that glass, and ice falling off it outside.
            var glass=wallOutside(b,wall,5.7);l.sendParticles(ParticleTypes.SNOWFLAKE,glass.x,glass.y,glass.z,10,wall%2==0?1.2:.1,.6,wall%2==0?.1:1.2,.02);
            var kind=LookoutTower.wall(l,b,wall,LookoutTower.CZ);if(kind==LookoutBlock.Kind.PANE||kind==LookoutBlock.Kind.TORN)for(var c:LookoutTower.window(wall,LookoutTower.CZ))LookoutTower.stage(l,b,c,Math.min(3,t.getInt("Frost")+1+((clock/WHISTLE_EVERY)&1)));
        }
        if(t.getBoolean("Failed"))return;
        var kind=LookoutTower.wall(l,b,wall,LookoutTower.CZ);if(!kind.open())return;
        for(var p:readers)if(inMainCab(p,b)&&looking(p,wall)){fail(l,b,t,readers,p,wall);return;}
    }
    /** A reader looked: it is nearer by a tier; at the glass, the glass goes. */
    static void fail(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> readers,ServerPlayer who,int wall){
        t.putBoolean("Failed",true);t.putInt("TierAt",t.getInt("Clock"));var own=own(who);own.putInt("Looked202",own.getInt("Looked202")+1);save(who,own);
        int tier=t.getInt("Tier");
        if(tier>=3){shatter(l,b,t,readers,wall);return;}
        t.putInt("Tier",tier+1);
        cue(who,switch(tier+1){case 1->"Something moved on the shed roof. You looked.";case 2->"It is on the frame below the cab. You looked.";default->"It is at the glass. Do not look again.";});
        sound(l,anchor(b,tier+1,wall),LookoutRegistry.GUST.get(),.6F,.7F);
    }
    /** The glass goes, and the night with it: everyone in the tower comes to at the foot of the stair. */
    static void shatter(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> readers,int wall){
        LookoutTower.setWall(l,b,wall,LookoutTower.CZ,LookoutBlock.Kind.BROKEN,0);sound(l,wallOutside(b,wall,5),LookoutRegistry.SHATTER.get(),1.6F,1F);
        endWhistle(l,b,t);t.putBoolean("Over",true);
        for(var p:readers){
            var own=own(p);own.putInt("Broke202",own.getInt("Broke202")+1);own.putBoolean("Alidade202",false);save(p,own);
            HousePackets.send(p,new HouseFadePayload(4,40,30));var foot=at(b,new Vec3(LookoutTower.CX+4.5,0,LookoutTower.CZ+6.5));
            p.teleportTo(l,foot.x,foot.y,foot.z,180,0);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();
            cue(p,"The glass went in, and it was against the frame where the glass had been. Then the snow at the foot of the tower, and the cab dark above you. The night is over.");
        }
    }

    // ------------------------------------------------------------------------------------------------ the stair
    /** On the second night, with someone out on the stair or the ground, it whistles at a landing instead: the lanterns turn cold, and a reader outside must be down. */
    static void stair(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> readers){
        if(t.getInt("Night")!=2)return;int clock=t.getInt("Clock"),landing=t.getInt("Stair");
        var outside=readers.stream().filter(p->!inMainCab(p,b)&&!inShed(p,b)).toList();
        if(landing>=0){
            if(clock>=t.getInt("StairEnd")){t.putInt("Stair",-1);for(var lt:LookoutTower.LANDINGS)LookoutTower.facing(l,b,LookoutTower.lantern(lt),Direction.NORTH,0);return;}
            if(!t.getBoolean("StairFailed"))for(var p:outside)if(!p.isCrouching()){t.putBoolean("StairFailed",true);fail(l,b,t,readers,p,t.getInt("LastWall"));t.putBoolean("Failed",false);break;}
            return;
        }
        if(!t.getBoolean("Crank")||t.getInt("Step")>=3||outside.isEmpty()||t.getInt("Wall")>=0||clock<t.getInt("NextStair"))return;
        int k=l.getRandom().nextInt(4);var lt=LookoutTower.LANDINGS.get(k);t.putInt("Stair",k);t.putInt("StairEnd",clock+STAIR_TICKS);t.putBoolean("StairFailed",false);t.putInt("NextStair",clock+160+l.getRandom().nextInt(80));
        // Toward it: the side of the frame the landing is on.
        int wall=lt.z()<LookoutTower.CZ?0:lt.x()>LookoutTower.CX?1:2;if(lt.x()<LookoutTower.CX&&lt.z()>LookoutTower.CZ)wall=3;t.putInt("LastWall",wall);
        for(var each:LookoutTower.LANDINGS)LookoutTower.facing(l,b,LookoutTower.lantern(each),LookoutTower.WALLS[wall],1);
        sound(l,at(b,new Vec3(lt.x()+.5+normal(wall)[0]*3,lt.h()+1,lt.z()+.5+normal(wall)[1]*3)),LookoutRegistry.WHISTLE.get(),1.8F,.95F);
        for(var p:outside){var own=own(p);if(!own.getBoolean("StairHint202")){own.putBoolean("StairHint202",true);save(p,own);cue(p,"The lanterns on the landings burn cold. Get down behind the rail, and stay down.");}}
    }
    static boolean inShed(ServerPlayer p,BlockPos b){var r=rel(b,p.position());return r.x>=-14&&r.x<-11&&r.z>=-27&&r.z<-24&&r.y<3;}

    // ------------------------------------------------------------------------------------------------ the whistler
    /** Where it stands for a tier and a wall: the treeline, the shed roof, the stair frame, the catwalk outside that glass. */
    static Vec3 anchor(BlockPos b,int tier,int wall){
        int[] n=normal(wall);
        return at(b,switch(tier){
            case 0->new Vec3(LookoutTower.CX+.5+n[0]*21,0,LookoutTower.CZ+.5+n[1]*21);
            case 1->LookoutTower.SHED_ROOF;
            case 2->new Vec3(LookoutTower.CX-6.5,9,LookoutTower.CZ-1.5);
            default->new Vec3(LookoutTower.CX+.5+n[0]*6,20,LookoutTower.CZ+.5+n[1]*6);
        });
    }
    static @Nullable LiteraryActor whistler(MinecraftServer s,CompoundTag t,ServerLevel l){if(!t.hasUUID("Whistler"))return null;return l.getEntity(t.getUUID("Whistler")) instanceof LiteraryActor a?a:null;}
    static void park(LiteraryActor a,BlockPos b){a.moveTo(b.getX()+.5,b.getY()-6,b.getZ()-60.5,0,0);a.setInvisible(true);}
    /** It never paths: it is where its tier puts it, and goes there only while nobody is looking at where it is or where it goes. */
    static void whistler(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> readers){
        var a=LiteraryVignettes.actor(readers.get(0),PLACE,"Whistler",LiteraryActor.WHISTLER,new BlockPos(0,-6,-60),false);if(a==null)return;
        if(!t.hasUUID("Whistler")||!t.getUUID("Whistler").equals(a.getUUID()))t.putUUID("Whistler",a.getUUID());
        a.noPhysics=true;a.setNoGravity(true);
        var target=anchor(b,t.getInt("Tier"),t.getInt("LastWall"));
        if(a.position().distanceToSqr(target)>.25){
            boolean seen=!a.isInvisible()&&LiteraryVignettes.watched(l,a.position().add(0,1.5,0))||LiteraryVignettes.watched(l,target.add(0,1.5,0));
            if(!seen){a.setInvisible(false);var face=at(b,new Vec3(LookoutTower.CX+.5,22,LookoutTower.CZ+.5)).subtract(target);float yaw=(float)(Mth.atan2(face.z,face.x)*180/Math.PI)-90;
                a.moveTo(target.x,target.y,target.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.setDeltaMovement(Vec3.ZERO);
                sound(l,target,net.minecraft.sounds.SoundEvents.SNOW_STEP,.5F,.6F);}
        }
        a.appearance(LiteraryActor.WHISTLER,t.getInt("Tier"));
    }

    // ------------------------------------------------------------------------------------------------ weather and the cab
    static void weather(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> readers,int night){
        if(night==2||night==3){var c=at(b,new Vec3(LookoutTower.CX+.5,12,LookoutTower.CZ+.5));l.sendParticles(ParticleTypes.SNOWFLAKE,c.x,c.y,c.z,night==3?60:40,14,10,14,.08);
            if(t.getInt("Clock")%120==0)sound(l,c.add(0,10,0),LookoutRegistry.GUST.get(),night==3?1.4F:.9F,.8F+l.getRandom().nextFloat()*.3F);}
    }
    static void frost(ServerLevel l,BlockPos b,int frost){for(int w=0;w<4;w++){var k=LookoutTower.wall(l,b,w,LookoutTower.CZ);if(k==LookoutBlock.Kind.PANE||k==LookoutBlock.Kind.TORN)for(var c:LookoutTower.window(w,LookoutTower.CZ))LookoutTower.stage(l,b,c,frost);}}
    /** A flare from a crew in the woods, at its bearing, well past the treeline. */
    static void flare(ServerLevel l,BlockPos b,int i){
        double a=Math.toRadians(FLARES[i]);var c=at(b,new Vec3(LookoutTower.CX+.5+Math.sin(a)*46,0,LookoutTower.CZ+.5-Math.cos(a)*46));
        for(int y=8;y<=34;y+=2)l.sendParticles(ParticleTypes.FIREWORK,c.x,c.y+y,c.z,2,.05,.4,.05,.01);
        l.sendParticles(ParticleTypes.FLASH,c.x,c.y+36,c.z,1,0,0,0,0);l.sendParticles(ParticleTypes.END_ROD,c.x,c.y+36,c.z,30,1.2,1.2,1.2,.05);
        sound(l,c.add(0,30,0),net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_LAUNCH,4F,.8F);
    }

    // ------------------------------------------------------------------------------------------------ the fire finder
    /** A reader holding the alidade turns it with their body; the ring reads their bearing, and a bearing held steady on a mark locks. */
    static void alidade(ServerLevel l,BlockPos b,CompoundTag t,ServerPlayer p,int night){
        var own=own(p);if(!own.getBoolean("Alidade202"))return;
        var finder=at(b,Vec3.atBottomCenterOf(inVoid(p,b)?LookoutTower.VOID_FINDER:LookoutTower.FINDER));
        if(Math.hypot(p.getX()-finder.x,p.getZ()-finder.z)>1.9||Math.abs(p.getY()-finder.y)>1.5){own.putBoolean("Alidade202",false);save(p,own);cue(p,"You let go of the alidade.");return;}
        double bearing=bearingOf(p.getYRot());Double target=null;
        if(night==1&&t.getInt("Flare")>=0)target=(double)FLARES[t.getInt("Flare")];
        if(night==4&&inVoid(p,b)&&!t.getBoolean("Locked"))target=0D;
        boolean on=target!=null&&off(bearing,target)<=TOLERANCE;
        own.putInt("Steady202",on?own.getInt("Steady202")+1:0);
        if(p.tickCount%5==0&&barFree(p))p.displayClientMessage(Component.literal(String.format(Locale.ROOT,"Alidade  %03d°%s",Math.floorMod((int)Math.round(bearing),360),on?"  on the mark":"")).withStyle(ChatFormatting.GOLD),true);
        if(on&&own.getInt("Steady202")>=STEADY){own.putInt("Steady202",0);save(p,own);lock(l,b,t,p,night,(int)Math.round(target));return;}
        save(p,own);
    }
    static void lock(ServerLevel l,BlockPos b,CompoundTag t,ServerPlayer p,int night,int bearing){
        var readers=readers(l,b);
        sound(l,p.position(),net.minecraft.sounds.SoundEvents.LEVER_CLICK,.8F,1.3F);
        if(night==1){int i=t.getInt("Flare");t.putInt("Flare",-1);for(var r:keepers(t,readers)){var own=own(r);var list=own.getList("Flares202",3);if(list.size()<=i)list.add(IntTag.valueOf(bearing));own.put("Flares202",list);save(r,own);}
            tape(l,b,readers,LookoutTexts.logged(i,bearing));return;}
        if(night==4){t.putBoolean("Locked",true);
            for(var c:LookoutTower.TRAPDOORS)l.setBlock(b.offset(c.getX(),c.getY(),LookoutTower.VZ+(c.getZ()-LookoutTower.CZ)),Blocks.AIR.defaultBlockState(),F);
            for(var r:readers)if(inVoid(r,b))cue(r,"000. The figure in the north tower lowers its binoculars. Under your feet the hatch has come open.");
        }
    }
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){var out=new ArrayList<ServerPlayer>();for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.inside(p,PLACE)&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);return out;}

    // ------------------------------------------------------------------------------------------------ the forest of towers
    /** The fourth night: each reader comes to in tower four; seven figures in the towers round it do what they do. */
    static void forest(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> readers){
        for(var p:readers){
            var own=own(p);int visit=own.getInt("Visit");
            if(!inVoid(p,b)){
                if(own.getInt("Returned202")==visit)continue;
                HousePackets.send(p,new HouseFadePayload(6,30,30));var w=at(b,LookoutTower.VOID_WAKE);p.teleportTo(l,w.x,w.y,w.z,180,10);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();
                cue(p,"No storm. No ground. The tower stands on its legs over nothing, and there are other towers round it.");continue;
            }
            var rel=rel(b,p.position());
            // Down through the opened hatch, back into the tower's own cab; any other way down, back up into the cab.
            if(rel.y<LookoutTower.FLOOR-.2&&Math.abs(rel.x-LookoutTower.CX-.5)<=6&&Math.abs(rel.z-LookoutTower.VZ-.5)<=6&&t.getBoolean("Locked")){comeDown(l,b,p,own);continue;}
            if(rel.y<LookoutTower.FLOOR-14){HousePackets.send(p,new HouseFadePayload(4,20,20));var w=at(b,LookoutTower.VOID_WAKE);p.teleportTo(l,w.x,w.y,w.z,180,10);p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;p.connection.resetPosition();continue;}
            mirrors(l,b,p,rel);
        }
    }
    static void comeDown(ServerLevel l,BlockPos b,ServerPlayer p,CompoundTag own){
        dropMirrors(l.getServer(),p.getUUID());
        HousePackets.send(p,new HouseFadePayload(4,30,30));var w=at(b,LookoutTower.CAB_WAKE);p.teleportTo(l,w.x,w.y,w.z,0,0);p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;p.connection.resetPosition();
        own.putInt("Returned202",own.getInt("Visit"));
        if(!done(own)&&night(own)==4){own.putBoolean("North202",true);LookoutTexts.keep(own,LookoutTexts.LAST);save(p,own);
            p.displayClientMessage(Component.literal("TTY  "+LookoutTexts.LAST).withStyle(ChatFormatting.GREEN),true);quiet(p,80);
            LiteraryVignettes.ready(p,PLACE,own,"locked_the_north_tower");}
        save(p,own);
    }
    /** Seven figures in the seven other towers, each where the reader is in their own cab; the north one only watches. */
    static void mirrors(ServerLevel l,BlockPos b,ServerPlayer p,Vec3 rel){
        double dx=rel.x-LookoutTower.CX,dy=rel.y,dz=rel.z-LookoutTower.VZ;boolean crouch=p.isCrouching();
        for(int k=0;k<8;k++){
            var a=LiteraryVignettes.actor(p,PLACE,"Mirror"+k,LiteraryActor.LOOKOUT_DOUBLE,new BlockPos(0,20,LookoutTower.VZ),true);if(a==null)continue;
            a.noPhysics=true;a.setNoGravity(true);var c=LookoutTower.ring(k);
            if(k==0){a.appearance(LiteraryActor.LOOKOUT_DOUBLE,0);a.gesture(1);a.moveTo(b.getX()+c.getX()+.5,b.getY()+20,b.getZ()+c.getZ()+3.5,0,0);a.setYHeadRot(0);a.setYBodyRot(0);continue;}
            a.appearance(LiteraryActor.LOOKOUT_DOUBLE,crouch?1:0);a.gesture(0);
            a.moveTo(b.getX()+c.getX()+dx,b.getY()+dy,b.getZ()+c.getZ()+dz,p.getYRot(),p.getXRot());a.setYHeadRot(p.getYHeadRot());a.setYBodyRot(p.yBodyRot);
        }
    }
    static void dropMirrors(MinecraftServer s,UUID who){
        var d=LabyrinthData.get(s);var shared=LiteraryVignettes.shared(d,PLACE);var l=s.getLevel(HouseDimensions.OUTSIDE);boolean changed=false;
        for(int k=0;k<8;k++){String id="Mirror"+k+"_"+who;if(!shared.hasUUID(id))continue;if(l!=null&&l.getEntity(shared.getUUID(id)) instanceof LiteraryActor a)a.discard();shared.remove(id);changed=true;}
        if(changed)LiteraryVignettes.shared(d,PLACE,shared);
    }

    // ------------------------------------------------------------------------------------------------ hands
    /** The cords and blinds, the tarps, the fire finder, the radio, the teleprinter, the crank and the generator. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        // The handlers below keep every reader's record through own()/save(), the clicking reader's included; the dispatcher
        // saves the tag it passed in afterwards, so it is brought up to date here rather than left to overwrite them.
        save(p,own);boolean handled=act(p,b,rel,own(p));
        var fresh=own(p);for(var key:new ArrayList<>(own.getAllKeys()))own.remove(key);own.merge(fresh);
        return handled;
    }
    private static boolean act(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var t=tower(p.server,b);var s=l.getBlockState(b.offset(rel));var kind=LookoutBlock.kind(s);
        boolean voidCab=Math.abs(rel.getZ()-LookoutTower.VZ)<=7;int cz=voidCab?LookoutTower.VZ:LookoutTower.CZ;
        int wall=LookoutTower.wallOf(rel,cz);
        if(wall>=0){blind(l,b,t,p,wall,cz);tower(p.server,b,t);return true;}
        if(kind==null)return false;
        var readers=readers(l,b);
        switch(kind){
            case FIRE_FINDER->{boolean holding=!own.getBoolean("Alidade202");own.putBoolean("Alidade202",holding);own.putInt("Steady202",0);save(p,own);
                cue(p,holding?"You take the alidade. It turns as you turn; the ring reads where you are facing, and you can keep your eyes on it.":"You let go of the alidade.");return true;}
            case RADIO->{if(t.getInt("Night")==1&&t.getInt("Step")==1&&!t.getBoolean("CheckedIn")){t.putBoolean("CheckedIn",true);tower(p.server,b,t);own.putBoolean("CheckedIn202",true);save(p,own);
                    sound(l,Vec3.atCenterOf(b.offset(rel)),LookoutRegistry.RADIO.get(),.8F,1F);cue(p,"You key the handset: tower four, on station. Static, then the teleprinter starts.");}
                else{sound(l,Vec3.atCenterOf(b.offset(rel)),LookoutRegistry.RADIO.get(),.6F,.9F);cue(p,t.getInt("Night")==3?"The set hisses. You did not call anyone, and you do not answer.":"Only static on the set.");}
                return true;}
            case TELEPRINTER->{var line=t.getString("LastTape");cue(p,line.isEmpty()?"The tape is blank.":"The last of the tape: "+line);return true;}
            case CRANK_HOOK->{if(s.getValue(LookoutBlock.STAGE)==0){LookoutTower.stage(l,b,rel,1);t.putBoolean("Crank",true);tower(p.server,b,t);LiteraryVignettes.give(p,new ItemStack(LookoutRegistry.CRANK_ITEM.get()));
                    cue(p,"You take the starter crank down from its hook. The generator is in the shed at the foot of the tower.");if(t.getInt("Night")==3)watched(l,b,t,readers,"took the crank");}
                else cue(p,"The hook is empty.");return true;}
            case TARP_PILE->{l.setBlock(b.offset(rel),Blocks.AIR.defaultBlockState(),F);LiteraryVignettes.give(p,new ItemStack(LookoutRegistry.TARP_ITEM.get()));cue(p,"You pick up a folded tarp.");return true;}
            case GENERATOR->{generator(l,b,t,p,readers);tower(p.server,b,t);return true;}
            case HATCH->{cue(p,voidCab?"It will not lift.":"The roof hatch is bolted. The binder says not to open it from inside.");return true;}
            case CORD->{return false;}
            default->{return false;}
        }
    }
    /** A wall's blind: down or up by its cord; a torn frame patched with a tarp, or its tarp taken off again. */
    static void blind(ServerLevel l,BlockPos b,CompoundTag t,ServerPlayer p,int wall,int cz){
        var kind=LookoutTower.wall(l,b,wall,cz);var name=LookoutTower.WALL_NAMES[wall];var readers=readers(l,b);
        var held=p.getMainHandItem();
        switch(kind){
            case PANE->{LookoutTower.setWall(l,b,wall,cz,LookoutBlock.Kind.SHUTTER,0);sound(l,p.position(),LookoutRegistry.SHUTTER.get(),.9F,1F);cue(p,"You pull the "+name+" cord. The canvas comes down over the glass.");if(t.getInt("Night")==3)watched(l,b,t,readers,"pulled the "+name+" cord");}
            case SHUTTER->{LookoutTower.setWall(l,b,wall,cz,LookoutBlock.Kind.PANE,Math.max(0,t.getInt("Frost")));sound(l,p.position(),LookoutRegistry.SHUTTER.get(),.7F,1.2F);cue(p,"You let the "+name+" blind roll up.");}
            case TORN->{if(held.is(LookoutRegistry.TARP_ITEM.get())){held.shrink(1);LookoutTower.setWall(l,b,wall,cz,LookoutBlock.Kind.TARP,0);sound(l,p.position(),LookoutRegistry.SHUTTER.get(),.9F,.8F);cue(p,"You lash the tarp over the "+name+" frame.");if(t.getInt("Night")==3)watched(l,b,t,readers,"put a tarp on the "+name+" frame");}
                else cue(p,"The "+name+" cord hangs loose. The canvas is gone. A tarp would hold the frame.");}
            case TARP->{if(held.isEmpty()){LookoutTower.setWall(l,b,wall,cz,LookoutBlock.Kind.TORN,t.getInt("Frost"));LiteraryVignettes.give(p,new ItemStack(LookoutRegistry.TARP_ITEM.get()));cue(p,"You unlash the tarp from the "+name+" frame.");if(t.getInt("Night")==3)watched(l,b,t,readers,"took the tarp off the "+name+" frame");}
                else cue(p,"The tarp is lashed over the "+name+" frame. Take it off with an empty hand.");}
            default->cue(p,"There is no glass in the "+name+" frame now.");
        }
    }
    /** The third night: the tape prints what the reader does as they do it. */
    static void watched(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> readers,String what){
        tape(l,b,readers,LookoutTexts.watching(what));for(var r:keepers(t,readers)){var own=own(r);own.putInt("Watched202",own.getInt("Watched202")+1);save(r,own);}
    }
    /** Prime it, then crank it: four turns close together and the diesel catches. */
    static void generator(ServerLevel l,BlockPos b,CompoundTag t,ServerPlayer p,List<ServerPlayer> readers){
        var at=Vec3.atCenterOf(b.offset(LookoutTower.GENERATOR));
        if(t.getBoolean("Power")){cue(p,"The diesel is running. The lamp over the door is lit.");return;}
        if(!t.getBoolean("Primed")){t.putBoolean("Primed",true);sound(l,at,net.minecraft.sounds.SoundEvents.BUCKET_FILL,.6F,1.4F);cue(p,"You work the primer until fuel shows in the little glass.");return;}
        if(!p.getMainHandItem().is(LookoutRegistry.CRANK_ITEM.get())){cue(p,"It is primed. It needs the starter crank.");return;}
        int clock=t.getInt("Clock");int turns=clock-t.getInt("TurnAt")<=TURN_WINDOW?t.getInt("Turns")+1:1;t.putInt("Turns",turns);t.putInt("TurnAt",clock);
        sound(l,at,LookoutRegistry.CRANK.get(),.9F,.9F+turns*.06F);
        if(turns<TURNS){cue(p,turns==1?"You fit the crank and heave it round.":"Again.");return;}
        t.putBoolean("Power",true);power(l,b,true);sound(l,at,LookoutRegistry.GENERATOR.get(),1.2F,1F);
        l.sendParticles(ParticleTypes.LARGE_SMOKE,b.getX()-13.5,b.getY()+5,b.getZ()-24.5,8,.1,.3,.1,.02);
        for(var r:readers)if(r!=p)cue(r,"Down in the shed the diesel catches. Light in the cab again.");
        cue(p,"It catches. The lamp over the door goes white, and up in the cab the bulb comes on.");
        var own=own(p);own.putBoolean("Cranked202",true);save(p,own);
    }
}
