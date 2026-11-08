package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.371, the House's read of a reader's home (docs/HOUSE_READ_DESIGN.md, "Authority figures under DANGER"; docs/HOUSE_READ_HOME_0_4_371.md).
 * <p>
 * A tentative or settled {@code HAVEN} or {@code DANGER} read gives domestic rooms a mood: one line, and nothing more. Only a settled
 * {@code DANGER} changes how the authority figures in that reader's rooms behave, and only toward anticipation: somebody comes along
 * the hall, stops outside the door, stays there, tries the handle and lets it go, and later goes away again. Nobody moves faster or
 * hits harder, nothing is shut, and every house that does it keeps its second way out. All of it is the reader's own: the sounds and
 * the line go to them alone, so a friend in the same room hears an ordinary evening.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HomeRead {
    private HomeRead(){}

    /** A settled DANGER read: the authority figures in this reader's rooms linger. Tentative never does (it could be a hesitant player). */
    public static boolean danger(ServerPlayer p){return HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.HOME,true).equals("DANGER");}
    /** The mood a home read gives a domestic room, tentative or settled: "HAVEN", "DANGER", or "" (RUNAWAY and no read give none). */
    public static String mood(ServerPlayer p){var v=HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.HOME,false);return v.equals("HAVEN")||v.equals("DANGER")?v:"";}
    /** One of two lines by mood, or "" for none. */
    public static String mood(ServerPlayer p,String haven,String danger){return switch(mood(p)){case "HAVEN"->haven;case "DANGER"->danger;default->"";};}

    // ------------------------------------------------------------------------------------------------ someone in the hall
    /** One sound (or a line on the action bar) at a game time, for one reader. */
    record Beat(long at,@Nullable SoundEvent sound,Vec3 where,float volume,float pitch,String line){}
    private static final Map<UUID,ArrayDeque<Beat>> PENDING=new HashMap<>(),RINGS=new HashMap<>();
    public static void clearAll(){PENDING.clear();RINGS.clear();}
    /** Whether somebody is in this reader's hall now. */
    public static boolean lingering(ServerPlayer p){return PENDING.containsKey(p.getUUID());}
    /** How long the whole visit takes, from the first board to the last step away. */
    public static final int LENGTH=360;

    /**
     * Somebody comes along the hall from {@code from} to {@code outside} (just beyond the door), a step at a time; the board takes their
     * weight; they stay; if {@code door} is given they try its handle and let it go; then they go back the way they came. {@code line},
     * if any, comes on the action bar when they stop. About eighteen seconds, to this reader alone; nothing else in the world changes.
     */
    public static void linger(ServerPlayer p,Vec3 from,Vec3 outside,@Nullable Vec3 door,String line){
        if(lingering(p))return;
        long now=p.serverLevel().getGameTime();var q=new ArrayDeque<Beat>();
        q.add(new Beat(now,LiteraryRegistry.HOME_FLOORBOARD.get(),from,.6F,.9F,""));
        int steps=5;
        for(int i=1;i<=steps;i++)q.add(new Beat(now+20+i*15L,LiteraryRegistry.HOME_STEP.get(),from.lerp(outside,i/(double)steps),.5F+.07F*i,.92F,""));
        long stop=now+20+steps*15L;
        q.add(new Beat(stop+30,LiteraryRegistry.HOME_FLOORBOARD.get(),outside,.5F,1.05F,line));
        if(door!=null)q.add(new Beat(stop+120,LiteraryRegistry.HOME_HANDLE.get(),door,.8F,1,""));
        for(int i=1;i<=4;i++)q.add(new Beat(stop+190+i*17L,LiteraryRegistry.HOME_STEP.get(),outside.lerp(from,i/4.0),.75F-.12F*i,.92F,""));
        PENDING.put(p.getUUID(),q);
    }

    /**
     * Where someone outside the nearest door would stand: {from, outside, the door itself}. The nearest closed door within {@code reach}
     * that the reader is not standing in, the side of it away from the reader, and a few blocks along the wall for where they come from.
     * Null when there is no door near.
     */
    public static @Nullable Vec3[] nearestDoor(ServerPlayer p,int reach){
        var l=p.serverLevel();var at=p.blockPosition();BlockPos best=null;double bestD=Double.MAX_VALUE;
        for(var pos:BlockPos.betweenClosed(at.offset(-reach,-2,-reach),at.offset(reach,2,reach))){
            var s=l.getBlockState(pos);if(!(s.getBlock() instanceof DoorBlock)||s.getValue(DoorBlock.HALF)!=DoubleBlockHalf.LOWER||s.getValue(DoorBlock.OPEN))continue;
            double d=pos.getCenter().distanceToSqr(p.position());if(d<2.25||d>=bestD)continue;best=pos.immutable();bestD=d;
        }
        if(best==null)return null;
        Direction facing=l.getBlockState(best).getValue(DoorBlock.FACING);var door=Vec3.atBottomCenterOf(best).add(0,1,0);
        var normal=new Vec3(facing.getStepX(),0,facing.getStepZ());
        double side=p.position().subtract(door).dot(normal);
        var away=side>0?normal.scale(-1):normal;
        var outside=door.add(away.scale(1.1)).add(0,-.5,0);
        var along=new Vec3(-normal.z,0,normal.x);
        return new Vec3[]{outside.add(along.scale(5)),outside,door};
    }

    // ------------------------------------------------------------------------------------------------ a telephone down the hall
    /** 0.4.372 (docs/HOUSE_READ_AGE_0_4_372.md): the personal record of a reader's hall cues. */
    public static final String AGE_STATE="home_read_0372";
    /** Later halls: from the depth where the House starts to be strange. One shared hall cue in four rings instead, the second first. */
    public static final int LANDLINE_DEPTH=LabyrinthPacing.STRANGE_DEPTH,LANDLINE_EVERY=4,RINGS_EACH=4,RING_GAP=120;
    /** A reader the House reads as older, tentative or settled. */
    public static boolean older(ServerPlayer p){return HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.AGE,false).equals("ANALOG");}
    /** Whether a telephone is ringing for this reader now. */
    public static boolean ringing(ServerPlayer p){return RINGS.containsKey(p.getUUID());}

    /**
     * On a hall's shared cue, for one listener: an older reader deep enough in sometimes hears a telephone with a bell ring four times
     * from where the cue would have come, and stop. Nobody answers it and there is nothing to answer. True when it rings, so the caller
     * leaves out the ordinary cue for them; anyone else in the hall hears only the pipes.
     */
    public static boolean hallCue(ServerPlayer p,LabyrinthData d,Vec3 at){
        if(!p.isAlive()||p.isSpectator()||ringing(p)||!older(p)||d.returnDepth(p.getUUID())<LANDLINE_DEPTH)return false;
        String key=p.getUUID().toString();var own=d.stateEntry(AGE_STATE,key);int n=own.getInt("HallCues")+1;own.putInt("HallCues",n);
        boolean ring=n%LANDLINE_EVERY==2;if(ring)own.putInt("Rang",own.getInt("Rang")+1);
        d.setStateEntry(AGE_STATE,key,own);
        if(ring)ring(p,at);
        return ring;
    }
    /** Four rings of a telephone at {@code at}, six seconds apart, to this reader alone. */
    public static void ring(ServerPlayer p,Vec3 at){
        long now=p.serverLevel().getGameTime();var q=new ArrayDeque<Beat>();
        for(int i=0;i<RINGS_EACH;i++)q.add(new Beat(now+(long)i*RING_GAP,LiteraryRegistry.HOME_LANDLINE.get(),at,.75F,1,""));
        RINGS.put(p.getUUID(),q);
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        play(e.getServer(),PENDING);play(e.getServer(),RINGS);
    }
    private static void play(net.minecraft.server.MinecraftServer server,Map<UUID,ArrayDeque<Beat>> pending){
        if(pending.isEmpty())return;
        for(var it=pending.entrySet().iterator();it.hasNext();){
            var entry=it.next();var p=server.getPlayerList().getPlayer(entry.getKey());
            if(p==null||!p.isAlive()||p.isSpectator()){it.remove();continue;}
            var q=entry.getValue();long now=p.serverLevel().getGameTime();
            while(!q.isEmpty()&&q.peek().at()<=now){
                var beat=q.poll();
                // A reader who has gone (another room, another door) leaves the hall behind them.
                if(beat.where().distanceToSqr(p.position())>40*40){q.clear();break;}
                if(beat.sound()!=null)p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(beat.sound()),SoundSource.AMBIENT,
                        beat.where().x,beat.where().y,beat.where().z,beat.volume(),beat.pitch(),p.getRandom().nextLong()));
                if(!beat.line().isEmpty())p.displayClientMessage(Component.literal(beat.line()).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
            }
            if(q.isEmpty())it.remove();
        }
    }
}
