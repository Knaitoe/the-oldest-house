package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.718, The list part 4e-1 (docs/THE_LIST_0_4_718.md): the gin by the tracks, as the slasher uses it, and only on the fourth night. The
 * augers run in their trough down the middle of the building, from the module feeder at the yard end toward the lint cleaner (TheListSite
 * .gin); the feeder's lower half carries the switch, and anyone who is not a spectator throws it. The line's running is the town's own,
 * shared by everyone in it and never saved; the hunt's end puts it back.
 * <p>
 * What the slasher does at the augers is TheListHunt's (State.AUGER: auger, augered, augering, mangled).
 */
public final class TheListGin{
    private TheListGin(){}
    /** The auger run's line across, and its ends; the switch on the feeder's lower half, the feeder two wide. */
    public static final int MID=TheListSite.GIN_W+10,RUN_S=TheListSite.GIN_S-4,RUN_N=TheListSite.GIN_N+8;
    public static final BlockPos SWITCH=new BlockPos(MID,0,TheListSite.GIN_S-2);
    /** The night the gin runs: the five are taken there only then. */
    public static final int NIGHT=4;
    /** How near the trough the one held must be, and how far down the screw draws them; how long it takes, in ticks while the line runs. */
    public static final double REACH=2.6,DEPTH=.85;
    public static final int DRAW=100;
    /** How often the line is heard running. */
    static final int HUM=80;

    /** Per town: thrown off by a hand, and jammed on what it took. */
    static final Map<Long,Boolean> OFF=new HashMap<>(),JAMMED=new HashMap<>();
    public static void clearAll(){OFF.clear();JAMMED.clear();}
    /** The hunt ended: the switch back on and the screw cleared. */
    static void reset(BlockPos b){OFF.remove(b.asLong());JAMMED.remove(b.asLong());}

    /** Whether a block of the town is a cell of the auger run, or the feeder's switch (either half of the feeder's lower block). */
    public static boolean auger(BlockPos rel){return rel.getY()==0&&rel.getX()==MID&&rel.getZ()<=RUN_S&&rel.getZ()>=RUN_N;}
    public static boolean feeder(BlockPos rel){return rel.getY()==0&&rel.getZ()==SWITCH.getZ()&&(rel.getX()==MID||rel.getX()==MID+1);}
    /** Whether the line is running: a hunt on its fourth night, the switch on and nothing jammed in the screw. */
    public static boolean running(BlockPos b){return TheListHunt.night(b)>=NIGHT&&!off(b)&&!jammed(b);}
    public static boolean off(BlockPos b){return OFF.getOrDefault(b.asLong(),false);}
    public static boolean jammed(BlockPos b){return JAMMED.getOrDefault(b.asLong(),false);}
    static void jam(BlockPos b){JAMMED.put(b.asLong(),true);}

    /** Where one in the trough is, drawn a share of the way down: in its middle, sinking feet first into the screw. */
    public static Vec3 in(BlockPos b,BlockPos cell,double drawn){return Vec3.atBottomCenterOf(b.offset(cell)).add(0,.06-DEPTH*Math.min(1,drawn),0);}
    /** Where blood off the screw lands: on the floor beside the trough, on the side toward the feeder's door. */
    public static Vec3 beside(BlockPos b,BlockPos cell){return Vec3.atBottomCenterOf(b.offset(cell)).add(1,0,0);}

    /** 0.4.719: the bale press, its lower half the chamber (taken away while someone is in it, put back after); how long the ram takes
     * to come down. */
    public static final BlockPos PRESS=new BlockPos(TheListSite.GIN_W+4,0,TheListSite.GIN_N+3);
    public static final int RAM=80;
    public static boolean press(BlockPos rel){return rel.getX()==PRESS.getX()&&rel.getZ()==PRESS.getZ()&&(rel.getY()==0||rel.getY()==1);}
    /** Where one in the chamber stands, and the floor in front of it, toward the press door. */
    public static Vec3 chamber(BlockPos b){return Vec3.atBottomCenterOf(b.offset(PRESS));}
    public static Vec3 front(BlockPos b){return Vec3.atBottomCenterOf(b.offset(PRESS.north()));}
    /** The chamber opened for one shoved in, and shut again: the press's lower half as it was built. */
    static void open(ServerLevel l,BlockPos b){var at=b.offset(PRESS);if(l.isLoaded(at))l.setBlock(at,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);}
    public static void shut(ServerLevel l,BlockPos b){var at=b.offset(PRESS);if(l.isLoaded(at))l.setBlock(at,TheListBlock.of(TheListBlock.Kind.GIN,net.minecraft.core.Direction.NORTH,4),3);}

    /** A hand or a blow on the gin: the switch for anyone; an auger cell or the press, the hunt's first. False for a look. */
    public static boolean hand(ServerPlayer p,BlockPos b,BlockPos rel){
        if(p.isSpectator())return false;
        if(feeder(rel)){toggle(p.serverLevel(),b,p);return true;}
        if(press(rel))return TheListHunt.press(p,b);
        return auger(rel)&&TheListHunt.auger(p,b,rel);
    }
    /** The switch thrown: off, the augers run down and stop; on, they turn again. Before the fourth night, or jammed, nothing moves. */
    static void toggle(ServerLevel l,BlockPos b,ServerPlayer p){
        var at=b.offset(SWITCH);
        l.playSound(null,at,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,1F,.6F);
        if(TheListHunt.night(b)<NIGHT){TheList.cue(p,TheListTexts.GIN_DARK);return;}
        boolean wasOff=off(b);OFF.put(b.asLong(),!wasOff);
        if(jammed(b)){TheList.cue(p,TheListTexts.GIN_JAMMED);return;}
        TheList.cue(p,wasOff?TheListTexts.GIN_ON:TheListTexts.GIN_OFF);
        var mid=b.offset(MID,0,(RUN_S+RUN_N)/2);
        l.playSound(null,mid,TheListRegistry.STRAIN.get(),SoundSource.BLOCKS,wasOff?1.2F:.9F,wasOff?1.1F:.7F);
    }

    /** Each tick of a town with a hunt in it: the line heard and seen running while anyone is near. */
    static void tick(ServerLevel l,BlockPos b,int tick,List<ServerPlayer> here){
        if(!running(b))return;
        var mid=b.offset(MID,0,(RUN_S+RUN_N)/2);
        boolean near=false;for(var p:here)if(p.blockPosition().distSqr(mid)<=40*40){near=true;break;}
        if(!near)return;
        if(tick%HUM==0)l.playSound(null,mid,TheListRegistry.AUGER.get(),SoundSource.BLOCKS,1.2F,.85F);
        if(tick%4==0)for(int z=RUN_N;z<=RUN_S;z+=3)
            l.sendParticles(ParticleTypes.WHITE_ASH,b.getX()+MID+.5,b.getY()+.5,b.getZ()+z+.5,2,.25,.1,.25,.01);
    }
}
