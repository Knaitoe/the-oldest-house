package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.List;
import java.util.Map;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 0.4.749, the waxworks, the first part (docs/WAXWORKS_0_4_749.md), from the owner's request of 23 November. The place alone:
 * Merriman's Wax Museum after closing, the staff corridor, the security office under its monitors, the ring corridor and the five
 * galleries, and its twenty-four figures (stranger phases 290 to 313), each on its mark. Nothing moves yet; the House does not deal it
 * until part 4, when it has its account (part 2, the flashlight and what the figures do, is WaxNight).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
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
    /** 0.4.753: dealt now that it has its account. */
    public static boolean dealable(LabyrinthData d,UUID reader){return true;}

    /** A reader in: the figures are put on their marks if they are not there, and the building's night sound goes round them once. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();ensure(l,b);SITES.add(b.asLong());
        own.putInt("Visits",own.getInt("Visits")+1);
        l.playSound(null,b.offset(0,1,-6),WaxRegistry.ROOM.get(),SoundSource.AMBIENT,.6F,1F);
    }

    /**
     * Run 1776: the galleries run seventy blocks from the door, and on arrival their entity sections can still be unloaded, so
     * while a living reader is anywhere in the museum the figures are looked for again every second.
     */
    static final java.util.Set<Long> SITES=java.util.concurrent.ConcurrentHashMap.newKeySet();
    @SubscribeEvent public static void keepFigures(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){
        var server=e.getServer();
        if(SITES.isEmpty()||server.getTickCount()%20!=11)return;
        var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(long site:List.copyOf(SITES)){
            var b=BlockPos.of(site);
            if(l.players().stream().anyMatch(p->!p.isSpectator()&&p.isAlive()&&p.distanceToSqr(Vec3.atCenterOf(b))<112*112))ensure(l,b);
            else SITES.remove(site);
        }
    }
    @SubscribeEvent public static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent e){SITES.clear();FOUND.clear();}

    /** The figure with this index, if its body is there: where it was last found, else by its mark, else (when the shift has moved it) anywhere in the museum. */
    static final Map<Long,int[]> FOUND=new java.util.concurrent.ConcurrentHashMap<>();
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){
        var ids=FOUND.computeIfAbsent(b.asLong(),k->{var a=new int[FIGURES];java.util.Arrays.fill(a,-1);return a;});
        if(ids[who]>=0&&l.getEntity(ids[who]) instanceof LiteraryActor a&&a.isAlive()&&is(a)&&a.phase()==FIRST+who&&a.distanceToSqr(Vec3.atCenterOf(b))<110*110)return a;
        var at=b.offset(WaxSite.FIGURES.get(who).at());
        var box=WaxShift.place(b,who)==null?new AABB(at).inflate(3):new AABB(b.offset(WaxSite.WEST,-1,WaxSite.NORTH)).minmax(new AABB(b.offset(WaxSite.EAST,WaxSite.TOP,1)));
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,box,x->is(x)&&x.phase()==FIRST+who))if(a.isAlive()){ids[who]=a.getId();return a;}
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
                stand(a,b,f,0);
                if(!l.addFreshEntity(a))continue;
                continue;
            }
            stand(a,b,f,0);
        }
    }
    /** On its mark, turned the way the gallery shows it, in its pose; or wherever the shift has it (WaxShift.place), turned or gone. */
    static void stand(LiteraryActor a,BlockPos b,WaxSite.Figure f,float turned){
        var moved=WaxShift.place(b,WaxSite.FIGURES.indexOf(f));
        var feet=(moved==null?WaxSite.feet(f):moved.at()).add(b.getX(),b.getY(),b.getZ());float yaw=moved==null?f.yaw():moved.yaw();
        a.setInvisible(moved!=null&&moved.gone());
        a.moveTo(feet.x,feet.y,feet.z,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw+turned);a.gesture(f.pose());a.setDeltaMovement(Vec3.ZERO);
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
            case MONITOR->WaxMonitors.sit(p,b,own);
            case DESK->cue(p,stage==1?WaxShift.log(b):stage==2?WaxShift.phone(p,b,own):WaxTexts.DESK);
            case CHAIR->cue(p,WaxTexts.CHAIR);
            case CLOCK->cue(p,WaxTexts.CLOCK);
            case TIMECLOCK->WaxShift.punch(p,b,own);
            case LOCKER,LOCKER_TOP->WaxNight.locker(p,own);
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
