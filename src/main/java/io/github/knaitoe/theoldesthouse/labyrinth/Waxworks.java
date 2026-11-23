package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.749, the waxworks, the first part (docs/WAXWORKS_0_4_749.md), from the owner's request of 23 November. The place alone:
 * Merriman's Wax Museum after closing, the staff corridor, the security office under its monitors, the ring corridor and the five
 * galleries, and its twenty-four figures (stranger phases 290 to 313), each on its mark. Nothing moves yet; the House does not deal it
 * until part 2, when the reader has a flashlight and a shift.
 */
public final class Waxworks {
    private Waxworks(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.WAXWORKS;
    /** The figures, on stranger phases 290 onward, in the order of WaxSite.FIGURES. */
    public static final int FIRST=290,FIGURES=24;
    /** One museum holds a night guard and three more: a party of four (docs/WAXWORKS_AND_COLD_ROOM_0_4_749.md). */
    public static final int PARTY=4;
    public static boolean cast(int phase){return phase>=FIRST&&phase<FIRST+FIGURES;}
    public static String skin(int phase){return "wax_"+WaxSite.FIGURES.get(phase-FIRST).key();}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    public static int which(LiteraryActor a){return a.phase()-FIRST;}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** Not dealt until part 2. */
    public static boolean dealable(LabyrinthData d,UUID reader){return false;}

    /** A reader in: the figures are put on their marks if they are not there, and the building's night sound goes round them once. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();ensure(l,b);
        own.putInt("Visits",own.getInt("Visits")+1);
        l.playSound(null,b.offset(0,1,-6),WaxRegistry.ROOM.get(),SoundSource.AMBIENT,.6F,1F);
    }

    /** The figure with this index, if its body is there. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){
        var at=b.offset(WaxSite.FIGURES.get(who).at());
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(at).inflate(3),x->is(x)&&x.phase()==FIRST+who))if(a.isAlive())return a;
        return null;
    }
    /** Each figure is made on its mark if it is not there; nothing is made where the galleries' entities are not loaded. */
    public static void ensure(ServerLevel l,BlockPos b){
        for(int who=0;who<FIGURES;who++){
            var f=WaxSite.FIGURES.get(who);var at=b.offset(f.at());
            if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))continue;
            var a=find(l,b,who);
            if(a==null){
                a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
                a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+who);
                stand(a,b,f);
                if(!l.addFreshEntity(a))continue;
                continue;
            }
            stand(a,b,f);
        }
    }
    /** On its mark, turned the way the gallery shows it, in its pose; nothing of it moves. */
    static void stand(LiteraryActor a,BlockPos b,WaxSite.Figure f){
        var feet=WaxSite.feet(f).add(b.getX(),b.getY(),b.getZ());
        a.moveTo(feet.x,feet.y,feet.z,f.yaw(),0);a.setYBodyRot(f.yaw());a.setYHeadRot(f.yaw());a.gesture(f.pose());a.setDeltaMovement(Vec3.ZERO);
    }

    /** A hand on a figure: it is wax, cold and a little tacky, and it does not answer. Each says what it is. */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        int who=which(a);
        cue(p,WaxTexts.touch(who));
        own.putInt("Touched",own.getInt("Touched")|1<<Math.min(30,who));
    }

    /** A hand on something of the museum's. True if it was theirs to answer. The notice is the first paper: the House's own reading answers it. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        if(!WaxBlock.is(st))return false;
        int stage=st.getValue(WaxBlock.STAGE);
        switch(st.getValue(WaxBlock.KIND)){
            case NOTICE->{return false;}
            case PLAQUE->{
                for(int i=0;i<WaxSite.FIGURES.size();i++)if(rel.equals(WaxSite.FIGURES.get(i).plaque())){cue(p,WaxTexts.plaque(i));return true;}
                cue(p,WaxTexts.PLAQUE);
            }
            case PLINTH->cue(p,WaxTexts.PLINTH);
            case ROPE->cue(p,WaxTexts.ROPE);
            case CAMERA->cue(p,WaxTexts.CAMERA);
            case MONITOR->cue(p,WaxTexts.MONITOR);
            case DESK->cue(p,stage==1?WaxTexts.DESK_LOG:stage==2?WaxTexts.DESK_PHONE:WaxTexts.DESK);
            case CHAIR->cue(p,WaxTexts.CHAIR);
            case CLOCK->cue(p,WaxTexts.CLOCK);
            case TIMECLOCK->{cue(p,WaxTexts.TIMECLOCK);l.playSound(null,b.offset(rel),WaxRegistry.PUNCH.get(),SoundSource.BLOCKS,.5F,1F);}
            case LOCKER,LOCKER_TOP->cue(p,WaxTexts.LOCKER);
            case FRONT->cue(p,WaxTexts.FRONT);
            case SIGN->cue(p,WaxTexts.SIGN);
            case BOOTH,BOOTH_TOP->cue(p,WaxTexts.BOOTH);
            case TABLE->cue(p,WaxTexts.TABLE);
            case DRESSER->cue(p,WaxTexts.DRESSER);
            case STOVE->cue(p,WaxTexts.STOVE);
            case COFFIN,COFFIN_FOOT->cue(p,WaxTexts.COFFIN);
            case GRAVESTONE->cue(p,WaxTexts.GRAVESTONE);
            case VAT->cue(p,WaxTexts.VAT);
            case BENCH->cue(p,stage==1?WaxTexts.BENCH_HEAD:stage==2?WaxTexts.BENCH_HANDS:WaxTexts.BENCH);
            case HEADS->cue(p,WaxTexts.HEADS);
        }
        return true;
    }
}
