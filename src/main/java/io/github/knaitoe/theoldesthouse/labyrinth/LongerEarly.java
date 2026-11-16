package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.631, A Little Longer, the eleventh slice (docs/A_LITTLE_LONGER_0_4_631.md): leaving early and coming back. The proposal: "The
 * reader can leave and resume at chapter boundaries... An explicit early departure can produce a shorter encounter. Rowan can ask once
 * whether the reader would like company. If the reader agrees to continue together early, compress the later practical preparations and
 * stage the same severance without fabricating an intimacy that never developed... A player returning in another session can experience
 * a genuine reunion."
 *
 * <ul>
 * <li><b>Going early.</b> Once the work at the bench is done and before the door moves (LongerTomorrow), a reader who goes out into the
 * House's corridor, having been in the shop or the flat since they came in, is asked once by Rowan whether they want company. "Come with me": Rowan goes for their coat (up the stair on foot if
 * a reader can see them; otherwise they are at the hook when next seen), and then the same cut as anybody's (LongerCut), with nothing in
 * between. "I'll come back" or "Goodnight": the reader goes, and the evening waits where it was.</li>
 * <li><b>Coming back.</b> A reader who arrives again before the cut is met once by Rowan, the first time Rowan can see them near, with
 * a line for how far the evening had got. Each chapter already starts again from its own beginning (its arrive), so nothing half done
 * is carried over and nothing is invented.</li>
 * </ul>
 */
public final class LongerEarly {
    private LongerEarly(){}
    public static final String EARLY="Early0631",RETURNS="Returns0631",BACK_AT="BackAt0631",VISITS="Visits0631";
    public static final String ASKED="ASKED",WITH="WITH",BACK="BACK",ALONE="ALONE";
    /** How near Rowan has to be, seeing the reader, to greet them back. */
    public static final double NEAR=6;

    private static final class Copy{boolean greet,inside;int via=-1;}
    private static final Map<BlockPos,Copy> COPIES=new HashMap<>();
    private static Copy copy(BlockPos b){return COPIES.computeIfAbsent(b.immutable(),k->new Copy());}
    public static void clearAll(){COPIES.clear();}
    public static void forget(BlockPos b){COPIES.remove(b);}

    /** How many chapters are behind the reader: 1 the bench, 2 the kettle, 3 the radio, 4 the roof, 5 the return. */
    public static int done(CompoundTag own){
        if(LongerBench.stage(own)!=LongerBench.DONE)return 0;
        if(LongerKettle.stage(own)!=LongerKettle.DONE)return 1;
        if(LongerRadio.stage(own)!=LongerRadio.ROOF)return 2;
        if(LongerRoof.stage(own)!=LongerRoof.DONE)return 3;
        if(LongerReturn.stage(own)!=LongerReturn.DONE)return 4;
        return 5;
    }
    /** True while the evening is between its first chapter and the door: going early is possible, and coming back is a reunion. */
    static boolean between(CompoundTag own){return done(own)>=1&&LongerTomorrow.stage(own)==LongerTomorrow.NONE&&LongerCut.stage(own)==LongerCut.NONE;}
    /** The reader agreed to go early and the cut is still to come. */
    public static boolean going(CompoundTag own){return WITH.equals(own.getString(EARLY))&&!LongerCut.gone(own);}

    /** A reader arrives: a return to an evening already begun is met by Rowan when they next see the reader. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var c=copy(b);c.via=-1;c.inside=false;
        int visits=own.getInt(VISITS);
        if(visits>0&&between(own)&&!going(own))c.greet=true;
        own.putInt(VISITS,visits+1);LongerKettle.save(p,own);
        if(going(own)){var a=LongerRowan.find(p.serverLevel(),b);if(a!=null)atHook(p.serverLevel(),b,a);}
    }

    /** Once a tick for the copy, before the chapters: true while this has Rowan. */
    static boolean tick(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> here,boolean atBench){
        ServerPlayer p=null;for(var r:here)if(between(LongerKettle.own(r))||going(LongerKettle.own(r))){p=r;break;}
        if(p==null)return false;
        var own=LongerKettle.own(p);var c=copy(b);
        if(going(own))return go(l,b,a,here,p,own,c);
        var at=LongerKettle.rel(p,b);
        if(LongerSite.inShop(at)||LongerSite.inFlat(at))c.inside=true;
        // Going out by the corridor: asked once, whatever Rowan was doing, if Rowan is not on the way somewhere.
        if(own.getString(EARLY).isEmpty()&&c.inside&&LongerSite.inCorridor(at)&&!Talk.waiting(p)&&(atBench||!LongerSite.inShop(a.position().subtract(b.getX(),b.getY(),b.getZ())))){ask(p,a,own);return true;}
        // Back again: greeted once Rowan can see them near.
        if(c.greet&&!Talk.waiting(p)&&a.distanceTo(p)<NEAR&&a.hasLineOfSight(p)){c.greet=false;greet(p,a,own);return false;}
        return false;
    }

    private static void ask(ServerPlayer p,LiteraryActor a,CompoundTag own){
        own.putString(EARLY,ASKED);LongerKettle.save(p,own);
        Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);
        Talk.line(p,LongerTexts.ROWAN,LongerTexts.EARLY_ASK);a.say(LongerTexts.ROWAN,LongerTexts.EARLY_ASK);a.speaks(LongerTexts.EARLY_ASK);
        Talk.ask(p,
            Talk.reply(LongerTexts.EARLY_WITH,r->answer(r,a,WITH,LongerTexts.EARLY_WITH_R)),
            Talk.reply(LongerTexts.EARLY_BACK,r->answer(r,a,BACK,LongerTexts.EARLY_BACK_R)),
            Talk.reply(LongerTexts.EARLY_ALONE,r->answer(r,a,ALONE,LongerTexts.EARLY_ALONE_R)));
    }
    private static void answer(ServerPlayer p,LiteraryActor a,String which,String line){
        var own=LongerKettle.own(p);if(!ASKED.equals(own.getString(EARLY))){Talk.close(p);return;}
        own.putString(EARLY,which);LongerKettle.save(p,own);
        Talk.line(p,LongerTexts.ROWAN,line);a.say(LongerTexts.ROWAN,line);a.speaks(line);Talk.end(p);
    }

    private static void greet(ServerPlayer p,LiteraryActor a,CompoundTag own){
        int done=done(own);
        String line=done<=1?LongerTexts.BACK_BENCH:done<4?LongerTexts.BACK_TEA:LongerTexts.BACK_ROOF;
        own.putInt(RETURNS,own.getInt(RETURNS)+1);if(!own.contains(BACK_AT))own.putInt(BACK_AT,done);LongerKettle.save(p,own);
        Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);
        Talk.line(p,LongerTexts.ROWAN,line);a.say(LongerTexts.ROWAN,line);a.speaks(line);Talk.end(p);
    }

    /** Going together: Rowan to the hook for the coat, then the cut, the same as anybody's. */
    private static boolean go(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> here,ServerPlayer p,CompoundTag own,Copy c){
        var hook=LongerTomorrow.HOOK_SPOT;
        boolean there=a.position().distanceTo(hook.at(b))<.3;
        if(!there&&LongerCut.stage(own)==LongerCut.NONE){
            var rel=a.position().subtract(b.getX(),b.getY(),b.getZ());
            boolean seen=false;for(var r:here)if(r.hasLineOfSight(a))seen=true;
            if(c.via<0&&!seen){atHook(l,b,a);return true;}
            // Seen in the shop at the bench: up the back stair on their own feet, the way the kettle chapter goes.
            if(c.via<0&&LongerSite.inShop(rel)&&rel.distanceTo(new net.minecraft.world.phys.Vec3(LongerRowan.BENCH.x(),0,LongerRowan.BENCH.z()))<.6)c.via=0;
            if(c.via<0)return false;
            var route=LongerKettle.UPSTAIRS;
            var target=c.via<route.size()?route.get(c.via):new net.minecraft.world.phys.Vec3(hook.x(),hook.y(),hook.z());
            a.gesture(LongerRowan.WALK);
            if(LongerRowan.step(a,target.add(b.getX(),b.getY(),b.getZ()))){if(c.via<route.size())c.via++;else atHook(l,b,a);}
            return true;
        }
        // At the hook in the coat: the cut takes it from here, and once it has begun it has Rowan wherever they are.
        if(LongerCut.tick(l,b,a,here,p,own))return true;
        LongerKettle.keep(a,b,hook,here);return true;
    }
    private static void atHook(ServerLevel l,BlockPos b,LiteraryActor a){
        LongerKettle.place(a,b,LongerTomorrow.HOOK_SPOT);a.gesture(LongerBench.REST);
        a.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);
        LongerTomorrow.coatOn(l,b,a);
    }
}
