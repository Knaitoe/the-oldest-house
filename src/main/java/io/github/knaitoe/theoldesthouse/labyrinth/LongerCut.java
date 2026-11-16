package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.629, A Little Longer, the ninth slice (docs/A_LITTLE_LONGER_0_4_629.md): chapter 6, the cut. The proposal: "The radio tune stops
 * halfway through a phrase. A narrow vertical seam appears through the room. It should look architectural at first... A stretch of wall,
 * part of the counter, and Rowan begin to recede into a thinner space. Rowan reaches for the reader... For a moment, Rowan is visible in a
 * narrowing lit space. Their hand is still reaching. They are trying to stay. Then the space is gone."
 *
 * <p>The House's power over space, not a test: no button saves Rowan and nothing the reader did causes it. It begins a while after the
 * choice about tomorrow, while Rowan unplugs the toaster before they go, wherever the reader is; it does not wait to be watched and works
 * whether they are beside Rowan, across the flat or down at the door. The wall by the toaster turns to a wrong edge, then opens into a
 * narrow passage of the same wrongness that the reader cannot enter (the House holds its mouth); the worktop's end, the toaster and Rowan
 * move back along it a block at a time, Rowan turned to the reader with a hand out, saying only "Wait."; the passage lowers, its light
 * goes, it closes, and where the wall was is a shallow alcove with bare brick at its back. The radio is off and the humming has stopped.
 * The kettle, the cups and the empty hook are where they were.</p>
 */
public final class LongerCut {
    private LongerCut(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LITTLE_LONGER;
    public static final String STAGE="Cut0629";
    public static final int NONE=0,TOASTER=1,CUTTING=2,DONE=3;
    /** Ticks: after the choice before Rowan goes to the toaster; the cut's beats; a step of the receding strip. */
    public static final int AFTER=300,EDGE=20,WAIT=40,STEP=14,STEPS=7,CLOSE=WAIT+STEP*STEPS+20;
    /** The strip that goes: the east wall's three cells at z -13, the worktop's end and the toaster; the passage behind it to x 14. */
    public static final int Z=-13,MOUTH=7,FAR=14;
    public static final LongerRowan.Spot TOASTER_SPOT=new LongerRowan.Spot(5.4,LongerSite.UP,-12.5,-90,LongerKettle.KETTLE);

    static final class Work {long at=-1,stood=-1;}
    private static final Map<BlockPos,Work> WORK=new HashMap<>();
    static Work work(BlockPos b){return WORK.computeIfAbsent(b.immutable(),k->new Work());}
    public static void clearAll(){WORK.clear();}
    static void forget(BlockPos b){WORK.remove(b);}
    static CompoundTag own(ServerPlayer p){return LongerKettle.own(p);}
    static void save(ServerPlayer p,CompoundTag own){LongerKettle.save(p,own);}
    public static int stage(CompoundTag own){return own.getInt(STAGE);}
    private static void stage(ServerPlayer p,CompoundTag own,int st){own.putInt(STAGE,st);save(p,own);}
    /** Rowan is gone for good once the cut has finished: they are never made again for this reader. */
    public static boolean gone(CompoundTag own){return stage(own)==DONE;}
    private static BlockState seam(){return LongerSite.mat("seam");}
    private static BlockState light(int level){return Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,level);}

    /** A reader arrives: a cut left part way is finished, the room as it is after; Rowan is not made again. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var w=work(b);w.at=-1;w.stood=-1;int st=stage(own);
        if(st==CUTTING){after(p.serverLevel(),b);var a=LongerRowan.find(p.serverLevel(),b);if(a!=null)a.discard();own.putInt(STAGE,DONE);save(p,own);st=DONE;}
        if(st==DONE)after(p.serverLevel(),b);
        if(st==TOASTER){own.putInt(STAGE,NONE);save(p,own);}
    }

    /** Once a tick, after the choice about tomorrow (LongerTomorrow, DONE): true from the moment Rowan goes to the toaster. */
    static boolean tick(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> here,ServerPlayer p,CompoundTag own){
        var w=work(b);long now=l.getGameTime();int st=stage(own);
        switch(st){
            case NONE->{
                if(w.stood<0)w.stood=now;
                if(now-w.stood<AFTER||Talk.waiting(p))return false;
                stage(p,own,TOASTER);a.gesture(LongerRowan.WALK);Longer.cue(p,LongerTexts.UNPLUG);return true;
            }
            case TOASTER->{
                if(a.position().distanceTo(TOASTER_SPOT.at(b))>.25){LongerRowan.step(a,TOASTER_SPOT.at(b));return true;}
                LongerKettle.place(a,b,TOASTER_SPOT);a.gesture(LongerKettle.KETTLE);w.at=now;stage(p,own,CUTTING);
                silence(l,b,here);return true;
            }
            case CUTTING->{cutting(l,b,a,here,p,own,w,now);return true;}
            default->{return false;}
        }
    }

    /** The radio stops in the middle of the phrase: off, and the tune already playing cut for everyone in the place. */
    static void silence(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var at=b.offset(LongerSite.FLAT_RADIO);var s=l.getBlockState(at);
        if(LongerBlock.is(s,LongerBlock.Kind.RADIO))l.setBlock(at,s.setValue(LongerBlock.STAGE,0),3);
        for(var q:here){
            q.connection.send(new ClientboundStopSoundPacket(LongerRegistry.TUNE.getId(),null));
            q.connection.send(new ClientboundStopSoundPacket(LongerRegistry.TUNE_LOW.getId(),null));
            Longer.cue(q,LongerTexts.RADIO_STOPS);
        }
    }

    private static void cutting(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> here,ServerPlayer p,CompoundTag own,Work w,long now){
        long t=now-w.at;
        if(t==EDGE){
            // The wrong edge: the wall by the toaster, floor to ceiling, a strip of depth that does not agree with the room.
            for(int y=0;y<3;y++)LongerSite.set(l,b,MOUTH,LongerSite.UP+y,Z,seam());
            for(var q:here)Longer.cue(q,LongerTexts.EDGE);
        }
        if(t==WAIT){
            // Rowan sees it, turns to the reader with a hand out: one word. The wall opens into a passage the House keeps to itself.
            a.gesture(LongerRoof.REACH);a.setYRot(90);a.setYBodyRot(90);a.setYHeadRot(90);
            Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);Talk.line(p,LongerTexts.ROWAN,LongerTexts.WAIT_R);a.say(LongerTexts.ROWAN,LongerTexts.WAIT_R);a.speaks(LongerTexts.WAIT_R);Talk.end(p);
            passage(l,b);
        }
        if(t>WAIT&&t<=WAIT+STEP*STEPS){
            // A block at a time the worktop's end, the toaster and Rowan go back along it, Rowan still turned to the reader, still reaching.
            long k=t-WAIT;int step=(int)(k/STEP);
            if(k%STEP==0&&step>=1){
                int from=6+step-1,to=6+step;
                var top=l.getBlockState(b.offset(from,LongerSite.UP+1,Z));var low=l.getBlockState(b.offset(from,LongerSite.UP,Z));
                // What they leave behind: the gap in the worktop on the room's side; the passage's mouth, which the House keeps shut.
                var left=from==MOUTH?Blocks.BARRIER.defaultBlockState():Blocks.AIR.defaultBlockState();
                LongerSite.set(l,b,from,LongerSite.UP+1,Z,left);LongerSite.set(l,b,from,LongerSite.UP,Z,left);
                LongerSite.set(l,b,to,LongerSite.UP,Z,low);LongerSite.set(l,b,to,LongerSite.UP+1,Z,top);
                // The passage lowers as it goes: its top course turns to the edge behind them.
                if(step>=STEPS-2)for(int x=MOUTH+1;x<=FAR;x++)LongerSite.set(l,b,x,LongerSite.UP+2,Z,seam());
            }
            double x=5.4+(k/(double)STEP);
            a.setPos(b.getX()+x,b.getY()+LongerSite.UP,b.getZ()+Z+.5);
            a.setYRot(90);a.setYBodyRot(90);a.setYHeadRot(90);a.gesture(LongerRoof.REACH);
        }
        if(t==WAIT+STEP*STEPS+8){LongerSite.set(l,b,FAR,LongerSite.UP+1,Z,Blocks.AIR.defaultBlockState());}
        if(t>=CLOSE){
            a.discard();LongerRowan.shop(b).phase=-1;after(l,b);stage(p,own,DONE);
            for(var q:here)Longer.cue(q,LongerTexts.WALL_AGAIN);
        }
    }

    /** The passage: the wall cells at the toaster opened, a narrow way of the same wrongness behind them, lit at its far end. */
    public static void passage(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        for(int x=MOUTH;x<=FAR+1;x++){
            for(int y=-1;y<=3;y++){LongerSite.set(l,b,x,LongerSite.UP+y,Z-1,seam());LongerSite.set(l,b,x,LongerSite.UP+y,Z+1,seam());}
            LongerSite.set(l,b,x,LongerSite.UP-1,Z,seam());LongerSite.set(l,b,x,LongerSite.UP+3,Z,seam());
            if(x<=FAR)for(int y=0;y<3;y++)LongerSite.set(l,b,x,LongerSite.UP+y,Z,x==MOUTH?Blocks.BARRIER.defaultBlockState():air);
            else for(int y=0;y<3;y++)LongerSite.set(l,b,x,LongerSite.UP+y,Z,seam());
        }
        LongerSite.set(l,b,FAR,LongerSite.UP+1,Z,light(11));
    }

    /** The room as it is after: the passage closed back into the yard's brick, and a shallow alcove where the wall was, bare brick at its back. */
    public static void after(ServerLevel l,BlockPos b){
        var facade=LongerSite.mat("facade");var brick=LongerSite.mat("brick");var paper=LongerSite.mat("paper");var air=Blocks.AIR.defaultBlockState();
        for(int x=MOUTH;x<=FAR+1;x++)for(int y=-1;y<=3;y++)for(int z=Z-1;z<=Z+1;z++)LongerSite.set(l,b,x,LongerSite.UP+y,z,x==MOUTH+1||y==-1&&x==MOUTH?brick:facade);
        // The flat's own wall either side as it was, papered; between them a recess a block deep, bare brick at its back and floor.
        for(int y=0;y<=3;y++){LongerSite.set(l,b,MOUTH,LongerSite.UP+y,Z-1,paper);LongerSite.set(l,b,MOUTH,LongerSite.UP+y,Z+1,paper);}
        for(int y=0;y<3;y++)LongerSite.set(l,b,MOUTH,LongerSite.UP+y,Z,air);LongerSite.set(l,b,MOUTH,LongerSite.UP+3,Z,paper);
        // The worktop's end and the toaster are not where they were: a gap in the worktop under the shelf.
        LongerSite.set(l,b,6,LongerSite.UP,Z,air);LongerSite.set(l,b,6,LongerSite.UP+1,Z,air);
        // The hook stays empty; the radio stays off.
        LongerSite.set(l,b,LongerSite.HOOK,LongerSite.fit(LongerBlock.Kind.HOOK,Direction.WEST,1));
        var r=b.offset(LongerSite.FLAT_RADIO);var s=l.getBlockState(r);if(LongerBlock.is(s,LongerBlock.Kind.RADIO)&&!LongerAfter.is(b))l.setBlock(r,s.setValue(LongerBlock.STAGE,0),3);
        // 0.4.630: the room afterward begins (LongerAfter).
        LongerAfter.begin(l,b);
    }

    /** A hand where Rowan was: nothing answers. */
    static boolean talk(ServerPlayer p,LiteraryActor a,CompoundTag own){return stage(own)==CUTTING||stage(own)==DONE;}

    // ------------------------------------------------------------------------------------------------ for cases
    public static void waited(BlockPos b,long ticks){var w=work(b);if(w.stood>=0)w.stood-=ticks;if(w.at>=0)w.at-=ticks;}
}
