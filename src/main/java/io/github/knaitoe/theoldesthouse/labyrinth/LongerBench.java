package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.623, A Little Longer, the third slice (docs/A_LITTLE_LONGER_0_4_623.md): chapter 1, the workbench. The proposal: "Rowan is trying
 * to refit its back panel. A loose screw rolls toward the counter edge. The player can stop it, return it, or simply speak." Then the
 * burst of noise, the look at the visitor, four ways to answer, the penciled route with one crossed-out turn, and Rowan putting the tool
 * down.
 *
 * <ul>
 * <li><b>The ask</b>: the first hand on Rowan at the bench, or a reader near the chair for three seconds, opens the panel with "Could you
 * hold that side a second?" The reader may hold it (and then must: a reader within reach of the radio looking at it for three seconds,
 * Rowan's hands quickening while they do), watch, or say not now.</li>
 * <li><b>Alone</b>: the last screw goes off the bench and rolls across the floor toward the counter, a real screw. A reader may pick it
 * up and hand it back. If nobody picks it up Rowan gets up, walks to it, stoops for it and comes back; if a reader keeps it, Rowan takes
 * another from the tray in the end. While Rowan works, a hand on the lamp turns it onto the radio.</li>
 * <li><b>The burst</b>: done, Rowan switches the set on; it plays a moment and pops. Then the panel: "That sounded expensive." The
 * reader's questions, each asked once; after the first, the route on its paper in Rowan's hand; the stairs; the crossed-out turn; then
 * the tool put down. The next chapter starts with the next slice.</li>
 * <li><b>The one remembered act</b>: the first of these the reader actually does, and only that, never assumed: waited at the back door
 * before coming in, held the panel, returned the screw, moved the lamp. Kept in the reader's record for the roof.</li>
 * </ul>
 *
 * <p>Nothing here waits on a clock to move the story; the clocks only end what the reader left (a panel never held, a screw never given
 * back). A reader who leaves part way through finds the work as it was before the ask when they come back; what they did is kept.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LongerBench {
    private LongerBench(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LITTLE_LONGER;
    /** The reader's record: where they are in the chapter, the act Rowan will remember, the questions asked, the route's replies used. */
    public static final String STAGE="Bench0623",ACT="Act0623",ASKED="Asked0623",ROUTE="Route0623",WAITED="Waited0623",ENTERED="InShop0623";
    public static final int NONE=0,ASK=1,HOLDING=2,ALONE=3,FINISHING=4,TALK=5,DONE=6;
    public static final String WAITED_AT_DOOR="WAITED_AT_DOOR",HELD_PANEL="HELD_PANEL",RETURNED_SCREW="RETURNED_SCREW",MOVED_LAMP="MOVED_LAMP";
    /** Rowan's gestures for this chapter, after LongerRowan's: hands quick at the radio, stooping to the floor, holding out the route, the tool put down. */
    public static final int HURRY=4,STOOP=5,SHOW=6,REST=7;
    /** Ticks: near the chair before Rowan asks; looking at the radio to hold it; how long a hold may wait; the screw left before Rowan fetches it, and kept before the spare. */
    public static final int NOTICE=60,HOLD_NEED=60,HOLD_WAIT=600,FETCH_AFTER=300,SPARE_AFTER=900,DOOR_WAIT=100;
    /** Finishing: the set switched on, the burst, the panel. */
    public static final int SWITCH_ON=30,BURST=70,PANEL=100;
    public static final String SCREW_TAG="LongerScrew0623";

    /** One copy's chapter: which reader it is for, and its clocks. */
    static final class Work {UUID reader;int near;int held;long at=-1;UUID screw;int fetch=-1,via;Vec3 to;boolean thanked;long screwGone=-1;}
    private static final Map<BlockPos,Work> WORK=new HashMap<>();
    static Work work(BlockPos b){return WORK.computeIfAbsent(b.immutable(),k->new Work());}
    public static void clearAll(){WORK.clear();}
    static void forget(BlockPos b){WORK.remove(b);}

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    public static int stage(CompoundTag own){return own.getInt(STAGE);}
    /** The act Rowan will remember, or empty: only the first thing actually done. */
    public static String act(CompoundTag own){return own.getString(ACT);}
    static void did(ServerPlayer p,CompoundTag own,String act){if(!own.contains(ACT)){own.putString(ACT,act);}}
    private static void stage(ServerPlayer p,CompoundTag own,int st){own.putInt(STAGE,st);save(p,own);}
    /** Rowan rests at the bench once the chapter is over: the tool down, no humming. */
    public static boolean rested(ServerLevel l,BlockPos b){var w=WORK.get(b);if(w==null||w.reader==null)return false;var p=l.getServer().getPlayerList().getPlayer(w.reader);return p!=null&&stage(own(p))==DONE;}

    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    private static BlockPos radio(BlockPos b){return b.offset(LongerSite.RADIO);}
    private static void radio(ServerLevel l,BlockPos b,int stage){var at=radio(b);var s=l.getBlockState(at);if(LongerBlock.is(s,LongerBlock.Kind.RADIO)&&s.getValue(LongerBlock.STAGE)!=stage)l.setBlock(at,s.setValue(LongerBlock.STAGE,stage),3);}
    private static void say(LiteraryActor a,String line){a.say(LongerTexts.ROWAN,line);a.speaks(line);}

    /** A reader arrives: a chapter left part way is put back to before the ask (what they did is kept), and the radio as it was then. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();var w=work(b);w.reader=p.getUUID();
        int st=stage(own);
        if(st>=ASK&&st<=FINISHING){own.putInt(STAGE,NONE);st=NONE;discardScrew(l,w);w.fetch=-1;w.held=0;w.near=0;}
        radio(l,b,st>=TALK?0:2);
    }

    /** The screw: a real one, named, marked as the shop's. */
    public static ItemStack screw(){var s=new ItemStack(Items.IRON_NUGGET);s.set(DataComponents.CUSTOM_NAME,Component.literal(LongerTexts.SCREW));var tag=new CompoundTag();tag.putBoolean(SCREW_TAG,true);s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return s;}
    public static boolean isScrew(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);return d!=null&&d.copyTag().getBoolean(SCREW_TAG);}
    private static boolean holdsScrew(ServerPlayer p){for(var s:p.getInventory().items)if(isScrew(s))return true;return isScrew(p.getOffhandItem());}
    private static void takeScrew(ServerPlayer p){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(isScrew(inv.getItem(i)))inv.setItem(i,ItemStack.EMPTY);}
    private static @Nullable ItemEntity screwOnFloor(ServerLevel l,Work w){return w.screw!=null&&l.getEntity(w.screw) instanceof ItemEntity e&&e.isAlive()?e:null;}
    private static void discardScrew(ServerLevel l,Work w){var e=screwOnFloor(l,w);if(e!=null)e.discard();w.screw=null;}
    /** For cases: the screw on the floor, if it is there. */
    public static @Nullable ItemEntity screwOnFloor(ServerLevel l,BlockPos b){return screwOnFloor(l,work(b));}
    /** For cases: Rowan's errand for the screw (-1 none, 0 out, 1 stooping, 2 back). */
    public static int fetching(BlockPos b){return work(b).fetch;}
    /** For cases: the screw has been on the floor this long already. */
    public static void screwWaited(BlockPos b,long ticks){var w=work(b);w.at-=ticks;}

    // ------------------------------------------------------------------------------------------------ each tick
    /**
     * Once a tick for the copy (LongerRowan.tick): true while the chapter has Rowan, so their errands wait. Rowan is never sent on an
     * errand in the middle of it.
     */
    static boolean tick(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> here,boolean atBench){
        var w=work(b);long now=l.getGameTime();
        ServerPlayer p=null;for(var r:here)if(r.getUUID().equals(w.reader))p=r;
        if(p==null&&!here.isEmpty()){p=here.get(0);w.reader=p.getUUID();}
        if(p==null)return false;
        var own=own(p);int st=stage(own);var at=rel(p,b);
        switch(st){
            case NONE->{
                if(!atBench)return false;
                // Waiting at the back door before coming in: the House's corridor, near the shop's door, the shop not yet entered.
                if(!own.getBoolean(ENTERED)){
                    if(LongerSite.inShop(at)){own.putBoolean(ENTERED,true);save(p,own);}
                    else if(LongerSite.inCorridor(at)&&Math.hypot(at.x-.5,at.z+6.5)<2&&own.getInt(WAITED)<DOOR_WAIT){own.putInt(WAITED,own.getInt(WAITED)+1);if(own.getInt(WAITED)>=DOOR_WAIT)did(p,own,WAITED_AT_DOOR);save(p,own);}
                }
                var chair=LongerRowan.BENCH.at(b);
                if(LongerSite.inShop(at)&&Math.hypot(p.getX()-chair.x,p.getZ()-chair.z)<3.2&&a.hasLineOfSight(p)){if(++w.near>=NOTICE){w.near=0;ask(p,a,own);}}
                else w.near=0;
                return false;
            }
            case ASK->{LongerRowan.hold(a,LongerRowan.WORK,here);return true;}
            case HOLDING->{
                boolean holding=holdingRadio(p,b);
                LongerRowan.hold(a,holding?HURRY:LongerRowan.WORK,here);
                if(holding){if(w.held==0)Longer.cue(p,LongerTexts.HOLD_CUE);if(++w.held>=HOLD_NEED){did(p,own,HELD_PANEL);w.thanked=true;finish(l,b,p,a,own,w,now);}}
                else if(now-w.at>HOLD_WAIT){say(a,LongerTexts.ONE_HANDED);alone(l,b,p,a,own,w,now);}
                return true;
            }
            case ALONE->{aloneTick(l,b,p,a,own,w,now,here);return true;}
            case FINISHING->{finishTick(l,b,p,a,own,w,now,here);return true;}
            case TALK->{
                // "They switch it off and turn in the chair to look properly at the reader": turned toward them, as far as a body turns
                // in a chair with a back (45 degrees off the bench, clear of its back), the head the rest of the way.
                var v=p.position().subtract(a.position());float toward=(float)(Mth.atan2(v.z,v.x)*Mth.RAD_TO_DEG)-90;
                float look=LongerRowan.BENCH.look()+Mth.clamp(Mth.wrapDegrees(toward-LongerRowan.BENCH.look()),-45,45);
                LongerRowan.hold(a,Talk.waiting(p,LongerRowan.TALK)&&own.getInt(ROUTE)!=0?SHOW:REST,here,look);
                return true;
            }
            default->{return false;}
        }
    }
    /** A reader within reach of the radio, looking at it. */
    static boolean holdingRadio(ServerPlayer p,BlockPos b){
        var r=Vec3.atCenterOf(radio(b));var eye=p.getEyePosition();if(eye.distanceTo(r)>2.6)return false;
        return p.getViewVector(1).dot(r.subtract(eye).normalize())>.9;
    }

    private static void ask(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;var w=work(b);w.reader=p.getUUID();
        own.putBoolean(LongerRowan.MET,true);stage(p,own,ASK);
        Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerSelf.askHold(p));
        askHold(p);
    }
    private static void askHold(ServerPlayer p){
        Talk.ask(p,Talk.act(LongerTexts.HOLD_IT,LongerBench::chooseHold),Talk.act(LongerTexts.WATCH,r->chooseAlone(r,null)),
            Talk.reply(LongerTexts.NOT_NOW,r->chooseAlone(r,LongerTexts.ONE_HANDED)));
    }
    private static void line(ServerPlayer p,LiteraryActor a,String text){Talk.line(p,LongerTexts.ROWAN,text);say(a,text);}
    private static @Nullable LiteraryActor rowan(ServerPlayer p){var b=LiteraryVignettes.base(p,PLACE);return b==null?null:LongerRowan.find(p.serverLevel(),b);}
    private static void chooseHold(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PLACE);var a=rowan(p);if(b==null||a==null)return;var own=own(p);
        if(stage(own)!=ASK)return;var w=work(b);w.held=0;w.at=p.serverLevel().getGameTime();
        line(p,a,LongerTexts.HOLD_WHERE);stage(p,own,HOLDING);
    }
    private static void chooseAlone(ServerPlayer p,@Nullable String said){
        var b=LiteraryVignettes.base(p,PLACE);var a=rowan(p);if(b==null||a==null)return;var own=own(p);
        if(stage(own)!=ASK)return;
        if(said!=null)line(p,a,said);
        alone(p.serverLevel(),b,p,a,own,work(b),p.serverLevel().getGameTime());
    }

    /** Rowan does it alone: the last screw goes off the edge of the bench and rolls away across the floor. */
    private static void alone(ServerLevel l,BlockPos b,ServerPlayer p,LiteraryActor a,CompoundTag own,Work w,long now){
        stage(p,own,ALONE);w.at=now;w.fetch=-1;w.screwGone=-1;discardScrew(l,w);
        var e=new ItemEntity(l,b.getX()-4.85,b.getY()+1.08,b.getZ()-11.7,screw(),.11,.02,-.13);
        e.setPickUpDelay(12);e.setUnlimitedLifetime();
        if(l.addFreshEntity(e))w.screw=e.getUUID();
        l.playSound(null,e.getX(),e.getY(),e.getZ(),SoundEvents.CHAIN_FALL,SoundSource.BLOCKS,.35F,1.9F);
        say(a,LongerTexts.SCREW_GONE);
    }
    private static void aloneTick(ServerLevel l,BlockPos b,ServerPlayer p,LiteraryActor a,CompoundTag own,Work w,long now,List<ServerPlayer> here){
        var floor=screwOnFloor(l,w);
        if(w.fetch>=0){fetchTick(l,b,p,a,own,w,now,here,floor);return;}
        LongerRowan.hold(a,LongerRowan.WORK,here);
        if(floor!=null){
            // Rolling a little and coming to rest; then, left there, Rowan goes for it.
            if(now-w.at>FETCH_AFTER&&floor.onGround()&&floor.getDeltaMovement().horizontalDistanceSqr()<1e-4){w.fetch=0;w.via=0;w.to=floor.position();a.gesture(LongerRowan.WALK);}
            return;
        }
        // Off the floor: in a reader's pocket, or gone some other way.
        if(w.screwGone<0){w.screwGone=now;w.screw=null;}
        boolean pocketed=false;for(var r:here)pocketed|=holdsScrew(r);
        if(!pocketed||now-w.screwGone>SPARE_AFTER){if(pocketed)say(a,LongerTexts.SCREW_SPARE);finish(l,b,p,a,own,w,now);}
    }
    /** The way out of the chair to the aisle, then to the screw; and back the same way. */
    private static void fetchTick(ServerLevel l,BlockPos b,ServerPlayer p,LiteraryActor a,CompoundTag own,Work w,long now,List<ServerPlayer> here,@Nullable ItemEntity floor){
        var aisle=new Vec3(b.getX()-4.5,b.getY(),b.getZ()-12.4);
        switch(w.fetch){
            case 0->{
                if(floor==null){w.fetch=2;w.via=0;return;}
                var screwAt=floor.position();var from=a.position();var d=new Vec3(screwAt.x-from.x,0,screwAt.z-from.z);
                // Stopping a short reach before the screw, as a body does, to stoop for it.
                Vec3 target=w.via==0?aisle:new Vec3(screwAt.x,b.getY(),screwAt.z).subtract(d.length()>0?d.normalize().scale(.55):Vec3.ZERO);
                if(w.via==1&&d.length()<.6){w.fetch=1;w.at=now;a.gesture(STOOP);return;}
                if(LongerRowan.step(a,target)){if(w.via==0)w.via=1;else{w.fetch=1;w.at=now;a.gesture(STOOP);}}
            }
            case 1->{
                if(floor!=null){var v=floor.position().subtract(a.position());float want=(float)(Mth.atan2(v.z,v.x)*Mth.RAD_TO_DEG)-90;a.setYRot(Mth.approachDegrees(a.getYRot(),want,18));a.setYBodyRot(a.getYRot());a.setYHeadRot(a.getYRot());}
                if(now-w.at>=24&&floor!=null){floor.discard();w.screw=null;l.playSound(null,a.getX(),a.getY(),a.getZ(),SoundEvents.ITEM_PICKUP,SoundSource.NEUTRAL,.25F,.8F);}
                if(now-w.at>=36){w.fetch=2;w.via=0;a.gesture(LongerRowan.WALK);}
            }
            case 2->{
                var chair=LongerRowan.BENCH.at(b);Vec3 target=w.via==0?aisle:chair;
                if(LongerRowan.step(a,target)){if(w.via==0)w.via=1;else{w.fetch=-1;LongerRowan.hold(a,LongerRowan.WORK,here);finish(l,b,p,a,own,w,now);}}
            }
            default->{}
        }
    }

    /** The panel is on; Rowan says what they say for it, switches the set on, and it bursts. */
    private static void finish(ServerLevel l,BlockPos b,ServerPlayer p,LiteraryActor a,CompoundTag own,Work w,long now){
        discardScrew(l,w);stage(p,own,FINISHING);w.at=now;
        say(a,w.thanked||!act(own).isEmpty()&&!act(own).equals(WAITED_AT_DOOR)?LongerTexts.THANKS:LongerTexts.THERE);
        radio(l,b,0);l.playSound(null,radio(b),SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.4F,1.5F);
    }
    private static void finishTick(ServerLevel l,BlockPos b,ServerPlayer p,LiteraryActor a,CompoundTag own,Work w,long now,List<ServerPlayer> here){
        long t=now-w.at;
        LongerRowan.hold(a,t<BURST?LongerRowan.WORK:REST,here);
        if(t==SWITCH_ON){radio(l,b,1);l.playSound(null,radio(b),SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.4F,.8F);l.playSound(null,radio(b),LongerRegistry.TUNING.get(),SoundSource.BLOCKS,.45F,1.1F);}
        if(t==BURST){
            var r=Vec3.atCenterOf(radio(b));
            l.playSound(null,r.x,r.y,r.z,LongerRegistry.BURST.get(),SoundSource.BLOCKS,.8F,1);
            l.sendParticles(ParticleTypes.ELECTRIC_SPARK,r.x+.3,r.y,r.z,14,.15,.15,.15,.25);l.sendParticles(ParticleTypes.SMOKE,r.x+.2,r.y+.2,r.z,6,.1,.1,.1,.01);
            radio(l,b,0);
        }
        if(t>=PANEL&&LongerSite.inShop(rel(p,b))&&p.distanceTo(a)<8&&!Talk.waiting(p))talk(p,a,own);
    }

    // ------------------------------------------------------------------------------------------------ the panel after the burst
    private static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(stage(own)==FINISHING){stage(p,own,TALK);Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerTexts.EXPENSIVE);line(p,a,LongerTexts.STANDING);offer(p);return;}
        if(!Talk.resume(p,LongerRowan.TALK)){Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerTexts.STANDING);offer(p);}
    }
    /** Bits of ROUTE: shown (1), doubted (2), three places (4), which turn (8). */
    private static void offer(ServerPlayer p){
        var own=own(p);int asked=own.getInt(ASKED),route=own.getInt(ROUTE);var replies=new ArrayList<Talk.Reply>();
        if((route&1)!=0){
            if((route&2)==0)replies.add(Talk.reply(LongerTexts.DOUBT,r->routeReply(r,2)));
            else if((route&4)==0)replies.add(Talk.reply(LongerTexts.THREE,r->routeReply(r,4)));
            if((route&8)==0)replies.add(Talk.reply(LongerTexts.WHICH,r->routeReply(r,8)));
        }
        for(int i=0;i<LongerTexts.QUESTIONS.size();i++)if((asked&1<<i)==0){int q=i;replies.add(Talk.reply(LongerTexts.QUESTIONS.get(i),r->question(r,q)));}
        if((route&1)!=0)replies.add(Talk.act(LongerTexts.LEAVE,LongerBench::leave));
        if(replies.size()==1&&(route&1)!=0){leave(p);return;}
        Talk.ask(p,replies);
    }
    private static void question(ServerPlayer p,int q){
        var a=rowan(p);if(a==null)return;var own=own(p);if(stage(own)!=TALK)return;
        own.putInt(ASKED,own.getInt(ASKED)|1<<q);save(p,own);line(p,a,LongerTexts.ANSWERS.get(q));
        if((own.getInt(ROUTE)&1)==0){
            own.putInt(ROUTE,1);save(p,own);
            a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.PAPER));a.gesture(SHOW);
            line(p,a,LongerTexts.ROUTE_TRIED);Talk.line(p,LongerTexts.ON_PAPER,LongerTexts.ROUTE);
        }
        offer(p);
    }
    private static void routeReply(ServerPlayer p,int bit){
        var a=rowan(p);if(a==null)return;var own=own(p);if(stage(own)!=TALK)return;
        own.putInt(ROUTE,own.getInt(ROUTE)|bit);save(p,own);
        line(p,a,switch(bit){case 2->LongerTexts.STAIRS;case 4->LongerTexts.RIGHT;default->LongerTexts.WHICH_ANSWER;});
        offer(p);
    }
    /** The route goes back in their pocket and the tool down on the bench: the work is over for tonight. */
    private static void leave(ServerPlayer p){
        var a=rowan(p);var own=own(p);if(stage(own)!=TALK)return;
        stage(p,own,DONE);
        if(a!=null){a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);a.gesture(REST);l(p).playSound(null,a.getX(),a.getY()+.8,a.getZ(),SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.3F,1.8F);}
        Talk.end(p);
    }
    private static ServerLevel l(ServerPlayer p){return p.serverLevel();}

    // ------------------------------------------------------------------------------------------------ hands
    /** A hand on Rowan during the chapter. True if the chapter answered it. */
    static boolean talk(ServerPlayer p,LiteraryActor a,CompoundTag own,boolean atBench){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return false;var w=work(b);
        int st=stage(own);
        switch(st){
            case NONE->{if(!atBench)return false;w.reader=p.getUUID();ask(p,a,own);return true;}
            case ASK->{if(!Talk.resume(p,LongerRowan.TALK)){Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerSelf.askHold(p));askHold(p);}return true;}
            case HOLDING->{Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerTexts.HOLD_REMIND);Talk.end(p);return true;}
            case ALONE->{
                Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);
                if(holdsScrew(p)&&w.fetch<0){takeScrew(p);did(p,own,RETURNED_SCREW);w.thanked=true;line(p,a,LongerTexts.SCREW_BACK);Talk.end(p);finish(p.serverLevel(),b,p,a,own,w,p.serverLevel().getGameTime());return true;}
                line(p,a,w.fetch>=0?LongerTexts.ROWAN_WALKING:LongerTexts.ROWAN_BENCH);Talk.end(p);return true;
            }
            case FINISHING,TALK->{if(st==FINISHING&&p.serverLevel().getGameTime()-w.at<BURST)return true;talk(p,a,own);return true;}
            default->{return false;}
        }
    }
    /** A hand on the lamp while Rowan works: it is turned onto the radio. True if the chapter answered it. */
    public static boolean lamp(ServerPlayer p,BlockPos b,BlockPos at,BlockState st,CompoundTag own){
        int s=stage(own);var w=work(b);
        if(!(s==HOLDING||s==ALONE&&w.fetch<0)||st.getValue(LongerBlock.STAGE)==2)return false;
        var l=p.serverLevel();l.setBlock(at,st.setValue(LongerBlock.STAGE,2),3);l.playSound(null,at,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.4F,1.2F);
        did(p,own,MOVED_LAMP);Longer.cue(p,LongerTexts.LAMP_TURNED);
        var a=LongerRowan.find(l,b);if(a!=null)say(a,LongerTexts.LAMP_BETTER);
        return true;
    }

    /** The shop's screw stays in the shop: a reader who leaves with it in a pocket finds it gone. */
    @SubscribeEvent public static void serverTick(ServerTickEvent.Post e){
        if(WORK.isEmpty()||e.getServer().getTickCount()%20!=0)return;
        var l=e.getServer().getLevel(NovelRooms.dimension(PLACE));
        for(var p:e.getServer().getPlayerList().getPlayers()){
            if(!holdsScrew(p))continue;
            var b=p.level()==l?LiteraryVignettes.base(p,PLACE):null;
            if(b==null||!LongerSite.inside(rel(p,b)))takeScrew(p);
        }
    }
}
