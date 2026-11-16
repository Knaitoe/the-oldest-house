package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.624, A Little Longer, the fourth slice (docs/A_LITTLE_LONGER_0_4_624.md): chapter 2, the kettle. The proposal: "Rowan offers tea.
 * Rowan: 'I'm making tea.' Player: 'Is that part of the repair?' Rowan: 'No.' A pause. Rowan: 'You could stay for it.'" Then the cups
 * that were never a pair, the comfortable chair Rowan will not give up, the toast, and three questions about now, never about before.
 *
 * <ul>
 * <li><b>The offer</b>: once the bench is put away, a hand on Rowan, or a reader by the chair a while, and Rowan says they're making
 * tea. The reader may ask whether that is part of the repair, or say nothing; either way a pause, with nothing to say into it, then
 * "You could stay for it." Staying, one cup, or going on (the offer made once more, on a later hand, and then let be).</li>
 * <li><b>Upstairs</b>: Rowan walks up the back stair on their own feet, puts the kettle on, fetches the two cups from the table while
 * it boils, pours when it clicks off and carries them back; the cracked one is theirs, the whole one is left at the reader's place. A
 * hand on the cups takes it (a real cup of tea).</li>
 * <li><b>The good chair</b>: Rowan sits in the armchair with their cup and is not getting up. A reader who has sat in it first keeps it
 * and Rowan takes the kitchen chair, which they say is fine. Where the reader is when Rowan sits is the first thing remarked: opposite
 * by the window, standing, or at the toaster.</li>
 * <li><b>The toast</b>: the reader pushes the toaster down. The left slice scorches while Rowan says the heat is manageable; a hand on
 * the toaster before it scorches pops it in time ("Show-off."). Otherwise one slice comes up burnt, and the reader teases, gives Rowan
 * the good one, or takes the burnt one to be brave. Both slices are real.</li>
 * <li><b>Now</b>: Rowan asks three things about the present (music or quiet, plans, the first thing out there), and answers each one
 * themselves. Nothing about anybody's past.</li>
 * </ul>
 *
 * <p>A reader who leaves part way finds Rowan back at the bench and the kitchen as it was when they come back; what they chose is kept.
 * Once the chapter is over Rowan stays in their chair upstairs.
 */
public final class LongerKettle {
    private LongerKettle(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LITTLE_LONGER;
    /** The reader's record: the chapter's stage, whether the offer was let go, what they said to it, how the toast went, the three answers. */
    public static final String SAID_NOTHING="Quiet0624",STAGE="Kettle0624",DECLINED="Declined0624",STAY="Stay0624",TOAST="Toast0624",CUP="TookCup0624",NOTICED="Noticed0624",
        MUSIC="Music0624",PLAN="Plan0624",FIRST="First0624";
    public static final int NONE=0,OFFER=1,PAUSE=2,STAY_ASK=3,UP=4,BOIL=5,POURED=6,SEATED=7,TOASTING=8,TOAST_ASK=9,NOW=10,DONE=11;
    public static final String RESCUED="RESCUED",TEASED="TEASED",OFFERED="OFFERED",BRAVE="BRAVE",UNTOASTED="UNTOASTED";
    /** Rowan's gestures for this chapter, after the bench's: a hand on the kettle, carrying the cups, pouring, sitting with a cup. */
    public static final int KETTLE=8,CARRY=9,POUR=10,CUP_HELD=11;
    /** Ticks: by the chair before the offer; the pause; the kettle to the boil (its click), and cold again; pouring; seated before the first remark. */
    public static final int NOTICE=100,QUIET=60,BOILED=122,COLD=140,POURING=50,SETTLE=30;
    /** The toast: Rowan's word on the heat, the scorching, the pop; how long Rowan waits for a hand on the toaster before going without. */
    public static final int HEAT_AT=70,SCORCH=95,POP=160,TOAST_WAIT=600;

    // ------------------------------------------------------------------------------------------------ where Rowan goes
    /** From the chair out to the aisle, along it, across to the foot of the back stair, up its six treads to the flat, and along the worktop to the kettle. */
    public static final List<Vec3> UPSTAIRS=List.of(new Vec3(-4.5,0,-12.4),new Vec3(.5,0,-12.4),new Vec3(.5,0,-8.5),
        new Vec3(1.5,1,-8.5),new Vec3(2.5,2,-8.5),new Vec3(3.5,3,-8.5),new Vec3(4.5,4,-8.5),new Vec3(5.5,5,-8.5),new Vec3(6.5,6,-8.5),
        new Vec3(5.5,6,-9.5),new Vec3(5.5,6,-12.5));
    /** At the kettle, facing it (east). */
    public static final LongerRowan.Spot KETTLE_SPOT=new LongerRowan.Spot(5.5,6,-12.5,-90,KETTLE);
    /** At the table's near side, facing it (south). */
    public static final LongerRowan.Spot TABLE_SPOT=new LongerRowan.Spot(1.5,6,-15.4,0,CARRY);
    /** The good chair, facing the table (east); the kitchen chair across it (west). */
    public static final LongerRowan.Spot ARMCHAIR_SPOT=new LongerRowan.Spot(.5,6,-16.5,-90,CUP_HELD),KITCHEN_SPOT=new LongerRowan.Spot(2.5,6,-16.5,90,CUP_HELD);
    private static final Vec3 BY_ARMCHAIR=new Vec3(.5,6,-15.5),BY_KITCHEN_CHAIR=new Vec3(2.5,6,-15.5);

    /** One copy's chapter: which reader it is for, Rowan's walk, and its clocks. */
    static final class Work {UUID reader;int near;long doneAt=-1,at=-1,boilAt=-1,toastAt=-1;int leg=-1,via;boolean rescued,cupsTaken,inKitchenChair;}
    private static final Map<BlockPos,Work> WORK=new HashMap<>();
    static Work work(BlockPos b){return WORK.computeIfAbsent(b.immutable(),k->new Work());}
    public static void clearAll(){WORK.clear();}
    static void forget(BlockPos b){WORK.remove(b);}

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    public static int stage(CompoundTag own){return own.getInt(STAGE);}
    private static void stage(ServerPlayer p,CompoundTag own,int st){own.putInt(STAGE,st);save(p,own);}
    private static void say(LiteraryActor a,String line){a.say(LongerTexts.ROWAN,line);a.speaks(line);}
    private static void line(ServerPlayer p,LiteraryActor a,String text){Talk.line(p,LongerTexts.ROWAN,text);say(a,text);}
    private static @Nullable BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    private static @Nullable LiteraryActor rowan(ServerPlayer p){var b=base(p);return b==null?null:LongerRowan.find(p.serverLevel(),b);}
    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}

    private static void fitting(ServerLevel l,BlockPos b,BlockPos at,LongerBlock.Kind kind,int stage){
        var s=l.getBlockState(b.offset(at));
        if(LongerBlock.is(s,kind)){if(s.getValue(LongerBlock.STAGE)!=stage)l.setBlock(b.offset(at),s.setValue(LongerBlock.STAGE,stage),3);}
        else if(s.isAir()&&kind==LongerBlock.Kind.CUPS)l.setBlock(b.offset(at),LongerBlock.of(kind,net.minecraft.core.Direction.NORTH,stage),3);
    }
    private static void cupsGone(ServerLevel l,BlockPos b){var at=b.offset(LongerSite.CUPS);if(LongerBlock.is(l.getBlockState(at),LongerBlock.Kind.CUPS))l.setBlock(at,Blocks.AIR.defaultBlockState(),3);}
    /** The kitchen as it was before the kettle went on. */
    private static void tidy(ServerLevel l,BlockPos b){
        fitting(l,b,LongerSite.KETTLE,LongerBlock.Kind.KETTLE,0);fitting(l,b,LongerSite.CUPS,LongerBlock.Kind.CUPS,0);fitting(l,b,LongerSite.TOASTER,LongerBlock.Kind.TOASTER,0);
    }
    /** Whether a reader is sat in this seat (by its cell). */
    static boolean sittingIn(ServerPlayer p,BlockPos b,BlockPos seat){
        if(!p.isPassenger())return false;var c=BlockPos.containing(rel(p,b).add(0,.2,0));
        return c.getX()==seat.getX()&&c.getZ()==seat.getZ()&&Math.abs(c.getY()-seat.getY())<=1;
    }

    /** A reader arrives: a chapter left part way is put back to before the offer (what they chose is kept); Rowan to their chair upstairs if it is over. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();var w=work(b);int st=stage(own);
        if(st>=OFFER&&st<DONE){own.putInt(STAGE,NONE);st=NONE;}
        w.reader=p.getUUID();w.leg=-1;w.near=0;w.at=-1;w.boilAt=-1;w.toastAt=-1;w.rescued=false;w.cupsTaken=false;
        tidy(l,b);
        var a=LongerRowan.find(l,b);
        if(a!=null){
            if(st==DONE){var spot=own.getBoolean("Kitchen0624")?KITCHEN_SPOT:ARMCHAIR_SPOT;place(a,b,spot);a.gesture(CUP_HELD);a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(LongerRegistry.TEA.get()));}
            else{a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);a.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);}
        }
    }
    static void place(LiteraryActor a,BlockPos b,LongerRowan.Spot s){var at=s.at(b);a.setPos(at.x,at.y,at.z);a.setYRot(s.look());a.setYBodyRot(s.look());a.setYHeadRot(s.look());}
    /** Kept where they are, doing this, turned this way; the head toward a reader near. */
    static void keep(LiteraryActor a,BlockPos b,LongerRowan.Spot s,List<ServerPlayer> here){
        var at=s.at(b);if(a.position().distanceToSqr(at)>1e-4)a.setPos(at.x,at.y,at.z);
        if(a.gesture()!=s.gesture())a.gesture(s.gesture());
        float body=Mth.approachDegrees(a.getYRot(),s.look(),LongerRowan.TURN);a.setYRot(body);a.setYBodyRot(body);
        LongerRowan.lookAt(a,here);
    }

    // ------------------------------------------------------------------------------------------------ each tick
    /**
     * Once a tick for the copy (LongerRowan.tick), after the bench: true while this chapter has Rowan, so their errands wait. From the
     * offer on, and for good once the tea is drunk.
     */
    static boolean tick(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> here,boolean atBench){
        var w=work(b);long now=l.getGameTime();
        ServerPlayer p=null;for(var r:here)if(r.getUUID().equals(w.reader))p=r;
        if(p==null&&!here.isEmpty()){p=here.get(0);w.reader=p.getUUID();}
        if(p==null)return false;
        var own=own(p);if(LongerBench.stage(own)!=LongerBench.DONE)return false;
        int st=stage(own);var at=rel(p,b);
        switch(st){
            case NONE->{
                if(!atBench||own.getInt(DECLINED)>0)return false;
                var chair=LongerRowan.BENCH.at(b);
                if(LongerSite.inShop(at)&&Math.hypot(p.getX()-chair.x,p.getZ()-chair.z)<3.2&&a.hasLineOfSight(p)&&!Talk.waiting(p)){if(++w.near>=NOTICE){w.near=0;offer(p,a,own);}}
                else w.near=0;
                return false;
            }
            case OFFER,STAY_ASK->{LongerRowan.hold(a,LongerBench.REST,here);return true;}
            case PAUSE->{
                LongerRowan.hold(a,LongerBench.REST,here);
                // The pause: nothing to say into it. Then the offer, in the same panel if it is still open, or at the next hand if not.
                if(now-w.at>=QUIET){stage(p,own,STAY_ASK);if(Talk.key(p).equals(LongerRowan.TALK)){line(p,a,LongerTexts.STAY_FOR);askStay(p);}}
                return true;
            }
            case UP->{walkTick(l,b,p,a,own,w,now,here);return true;}
            case BOIL->{boilTick(l,b,p,a,own,w,now,here);return true;}
            case POURED->{pouredTick(l,b,p,a,own,w,now,here);return true;}
            case SEATED->{
                keep(a,b,seat(own),here);
                if(w.at>=0&&now-w.at==SETTLE)remark(l,b,p,a,own);
                if(w.at>=0&&now-w.at>SETTLE+TOAST_WAIT){own.putString(TOAST,UNTOASTED);stage(p,own,NOW);w.at=now;ask(p,a,0,true);}
                return true;
            }
            case TOASTING->{keep(a,b,seat(own),here);toastTick(l,b,p,a,own,w,now);return true;}
            case TOAST_ASK,NOW->{keep(a,b,seat(own),here);return true;}
            case DONE->{
                if(!a.getMainHandItem().is(LongerRegistry.TEA.get()))a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(LongerRegistry.TEA.get()));
                // 0.4.625: then the radio chapter (LongerRadio), which has Rowan from here on.
                if(w.doneAt<0)w.doneAt=now;
                if(LongerRadio.tick(l,b,a,here,p,own,w.doneAt))return true;
                keep(a,b,seat(own),here);return true;}
            default->{return false;}
        }
    }
    private static LongerRowan.Spot seat(CompoundTag own){return own.getBoolean("Kitchen0624")?KITCHEN_SPOT:ARMCHAIR_SPOT;}

    // ------------------------------------------------------------------------------------------------ the offer
    private static void offer(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=base(p);if(b==null)return;work(b).reader=p.getUUID();
        stage(p,own,OFFER);
        Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerTexts.MAKING_TEA);askPart(p);
    }
    private static void askPart(ServerPlayer p){Talk.ask(p,Talk.reply(LongerTexts.PART,r->pause(r,true)),Talk.act(LongerTexts.SAY_NOTHING,r->pause(r,false)));}
    private static void pause(ServerPlayer p,boolean asked){
        var b=base(p);var a=rowan(p);if(b==null||a==null)return;var own=own(p);if(stage(own)!=OFFER)return;
        if(asked)line(p,a,LongerTexts.NO);else own.putBoolean(SAID_NOTHING,true);
        work(b).at=p.serverLevel().getGameTime();stage(p,own,PAUSE);Talk.end(p);
    }
    private static void askStay(ServerPlayer p){
        Talk.ask(p,Talk.reply(LongerTexts.YES,r->stay(r,"YES")),Talk.reply(LongerTexts.ONE_CUP,r->stay(r,"ONE_CUP")),Talk.reply(LongerTexts.KEEP_GOING,LongerKettle::decline));
    }
    private static void stay(ServerPlayer p,String how){
        var b=base(p);var a=rowan(p);if(b==null||a==null)return;var own=own(p);if(stage(own)!=STAY_ASK)return;
        own.putString(STAY,how);stage(p,own,UP);Talk.end(p);
        var w=work(b);w.leg=0;w.via=0;w.reader=p.getUUID();a.gesture(LongerRowan.WALK);
    }
    private static void decline(ServerPlayer p){
        var a=rowan(p);var own=own(p);if(stage(own)!=STAY_ASK)return;
        own.putInt(DECLINED,own.getInt(DECLINED)+1);stage(p,own,NONE);
        if(a!=null)line(p,a,LongerTexts.GO_ON);Talk.end(p);
    }

    // ------------------------------------------------------------------------------------------------ upstairs
    /** Up the stair to the kettle; the kettle on; while it boils, down the flat to the table for the cups and back with them. */
    private static void walkTick(ServerLevel l,BlockPos b,ServerPlayer p,LiteraryActor a,CompoundTag own,Work w,long now,List<ServerPlayer> here){
        if(w.via<UPSTAIRS.size()){if(LongerRowan.step(a,UPSTAIRS.get(w.via).add(b.getX(),b.getY(),b.getZ())))w.via++;return;}
        // At the kettle: a hand on it, and it goes on.
        keep(a,b,KETTLE_SPOT,here);
        stage(p,own,BOIL);w.boilAt=now;w.leg=0;w.via=0;
        fitting(l,b,LongerSite.KETTLE,LongerBlock.Kind.KETTLE,1);
        var k=Vec3.atCenterOf(b.offset(LongerSite.KETTLE));
        l.playSound(null,k.x,k.y,k.z,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.35F,1.3F);l.playSound(null,k.x,k.y,k.z,LongerRegistry.KETTLE.get(),SoundSource.BLOCKS,.7F,1);
    }
    /**
     * While it boils: Rowan's legs of the errand (leg 0: at the kettle a moment; 1: to the table; 2: the cups taken up; 3: back to the
     * kettle; 4: waiting for the click; 5: pouring; 6: to the table with both; 7: set down, the cracked one taken, to the chair).
     */
    private static void boilTick(ServerLevel l,BlockPos b,ServerPlayer p,LiteraryActor a,CompoundTag own,Work w,long now,List<ServerPlayer> here){
        long t=now-w.boilAt;var k=Vec3.atCenterOf(b.offset(LongerSite.KETTLE));
        if(t>=40&&t<BOILED&&t%4==0)l.sendParticles(ParticleTypes.CLOUD,k.x,k.y+.45,k.z-.2,1,.03,.05,.03,.01);
        if(t==BOILED){l.playSound(null,k.x,k.y,k.z,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.3F,1.7F);}
        if(t>=COLD)fitting(l,b,LongerSite.KETTLE,LongerBlock.Kind.KETTLE,0);
        switch(w.leg){
            case 0->{keep(a,b,KETTLE_SPOT,here);if(t>=20){w.leg=1;a.gesture(LongerRowan.WALK);}}
            case 1->{if(LongerRowan.step(a,TABLE_SPOT.at(b))){w.leg=2;w.at=now;}}
            case 2->{
                keep(a,b,TABLE_SPOT,here);
                if(now-w.at==10){cupsGone(l,b);a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(LongerRegistry.TEA.get()));l.playSound(null,a.getX(),a.getY()+1,a.getZ(),SoundEvents.DECORATED_POT_HIT,SoundSource.NEUTRAL,.25F,1.6F);}
                if(now-w.at>=22){w.leg=3;a.gesture(CARRY);}
            }
            case 3->{a.gesture(CARRY);if(LongerRowan.step(a,KETTLE_SPOT.at(b))){w.leg=4;}}
            case 4->{keep(a,b,new LongerRowan.Spot(KETTLE_SPOT.x(),KETTLE_SPOT.y(),KETTLE_SPOT.z(),KETTLE_SPOT.look(),CARRY),here);if(t>=BOILED+6){w.leg=5;w.at=now;a.gesture(POUR);a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);l.playSound(null,k.x,k.y,k.z,LongerRegistry.POUR.get(),SoundSource.BLOCKS,.6F,1);}}
            case 5->{keep(a,b,new LongerRowan.Spot(KETTLE_SPOT.x(),KETTLE_SPOT.y(),KETTLE_SPOT.z(),KETTLE_SPOT.look(),POUR),here);
                if(now-w.at<POURING-10&&(now-w.at)%3==0)l.sendParticles(ParticleTypes.CLOUD,a.getX()+.35,a.getY()+1.15,a.getZ()-.1,1,.02,.04,.02,.005);
                if(now-w.at>=POURING){w.leg=6;a.gesture(CARRY);a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(LongerRegistry.TEA.get()));}}
            case 6->{a.gesture(CARRY);if(LongerRowan.step(a,TABLE_SPOT.at(b))){w.leg=7;w.at=now;}}
            case 7->{
                keep(a,b,TABLE_SPOT,here);
                if(now-w.at==8){fitting(l,b,LongerSite.CUPS,LongerBlock.Kind.CUPS,0);l.playSound(null,b.getX()+1.5,b.getY()+7,b.getZ()-16.5,SoundEvents.DECORATED_POT_HIT,SoundSource.BLOCKS,.25F,1.4F);say(a,LongerTexts.POURED);}
                if(now-w.at==30){fitting(l,b,LongerSite.CUPS,LongerBlock.Kind.CUPS,2);stage(p,own,POURED);w.leg=0;w.at=now;a.gesture(LongerRowan.WALK);}
            }
            default->{}
        }
    }
    /** The cups poured: Rowan to the good chair, or, if the reader is already sat in it, to the kitchen chair. */
    private static void pouredTick(ServerLevel l,BlockPos b,ServerPlayer p,LiteraryActor a,CompoundTag own,Work w,long now,List<ServerPlayer> here){
        if(w.leg==0){
            boolean taken=false;for(var r:here)taken|=sittingIn(r,b,LongerSite.ARMCHAIR);
            if(taken!=own.getBoolean("Kitchen0624")){own.putBoolean("Kitchen0624",taken);save(p,own);}
            w.leg=1;
        }
        var by=own.getBoolean("Kitchen0624")?BY_KITCHEN_CHAIR:BY_ARMCHAIR;
        if(w.leg==1){if(LongerRowan.step(a,by.add(b.getX(),b.getY(),b.getZ())))w.leg=2;return;}
        if(w.leg==2&&LongerRowan.step(a,seat(own).at(b))){
            keep(a,b,seat(own),here);stage(p,own,SEATED);w.at=now;
            if(own.getBoolean("Kitchen0624"))say(a,LongerTexts.GOOD_TAKEN);
        }
    }
    /** The first thing said, seated: where the reader is. Then the toaster, unless they are already at it. */
    private static void remark(ServerLevel l,BlockPos b,ServerPlayer p,LiteraryActor a,CompoundTag own){
        var r=rel(p,b);var toaster=Vec3.atCenterOf(LongerSite.TOASTER);
        boolean helping=Math.hypot(r.x-toaster.x,r.z-toaster.z)<1.8&&LongerSite.inFlat(r);
        boolean opposite=!own.getBoolean("Kitchen0624")&&sittingIn(p,b,LongerSite.KITCHEN_CHAIR);
        if(own.getBoolean("Kitchen0624"))say(a,LongerTexts.TOAST_ASK);
        else if(opposite){say(a,LongerTexts.OPPOSITE);}
        else if(helping)say(a,LongerTexts.HELPING);
        else say(a,LongerTexts.STAND_SIT);
        if(!helping&&!own.getBoolean("Kitchen0624"))say(a,LongerTexts.TOAST_ASK);
    }

    // ------------------------------------------------------------------------------------------------ hands on the kitchen
    /** A hand on the cups once poured: the whole one is the reader's. True if the chapter answered it. */
    public static boolean cups(ServerPlayer p,BlockPos b,BlockPos at,BlockState st,CompoundTag own){
        int s=stage(own);if(s<POURED||s>DONE||own.getBoolean(CUP)||st.getValue(LongerBlock.STAGE)!=2)return false;
        var l=p.serverLevel();l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
        give(p,new ItemStack(LongerRegistry.TEA.get()));own.putBoolean(CUP,true);save(p,own);
        l.playSound(null,at,SoundEvents.DECORATED_POT_HIT,SoundSource.BLOCKS,.25F,1.5F);Longer.cue(p,LongerTexts.TOOK_CUP);
        return true;
    }
    /** A hand on the toaster while Rowan sits: down it goes; again before it scorches, up it comes in time. True if the chapter answered it. */
    public static boolean toaster(ServerPlayer p,BlockPos b,BlockPos at,BlockState st,CompoundTag own){
        int s=stage(own);var l=p.serverLevel();var w=work(b);long now=l.getGameTime();
        if(s==SEATED){
            stage(p,own,TOASTING);w.toastAt=now;w.rescued=false;
            l.setBlock(at,st.setValue(LongerBlock.STAGE,1),3);l.playSound(null,at,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.5F,.7F);
            Longer.cue(p,LongerTexts.TOASTER_DOWN_NOW);
            return true;
        }
        if(s==TOASTING){
            if(now-w.toastAt<SCORCH){w.rescued=true;pop(l,b,p,own,w,false);}
            else pop(l,b,p,own,w,false);
            return true;
        }
        return false;
    }
    private static void toastTick(ServerLevel l,BlockPos b,ServerPlayer p,LiteraryActor a,CompoundTag own,Work w,long now){
        long t=now-w.toastAt;var top=Vec3.atCenterOf(b.offset(LongerSite.TOASTER)).add(0,.4,0);
        if(t%20==0)l.playSound(null,top.x,top.y,top.z,SoundEvents.COMPARATOR_CLICK,SoundSource.BLOCKS,.08F,1.9F);
        if(t==HEAT_AT)say(a,LongerTexts.HEAT);
        if(t>=SCORCH&&t%5==0)l.sendParticles(ParticleTypes.SMOKE,top.x,top.y,top.z+.12,1,.04,.02,.04,.004);
        // The toaster's spring: the cue's pop lands on the moment it comes up.
        if(t==POP-40)l.playSound(null,top.x,top.y,top.z,LongerRegistry.TOASTER.get(),SoundSource.BLOCKS,.6F,1);
        if(t>=POP)pop(l,b,p,own,w,true);
    }
    /** Up: both slices good if it was popped in time, one burnt if not. Then what the reader makes of it. */
    private static void pop(ServerLevel l,BlockPos b,ServerPlayer p,CompoundTag own,Work w,boolean onItsOwn){
        if(stage(own)!=TOASTING)return;
        var at=b.offset(LongerSite.TOASTER);var st=l.getBlockState(at);
        if(LongerBlock.is(st,LongerBlock.Kind.TOASTER))l.setBlock(at,st.setValue(LongerBlock.STAGE,0),3);
        if(!onItsOwn)l.playSound(null,at,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.5F,1.6F);
        var a=LongerRowan.find(l,b);if(a==null)return;
        Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);
        if(w.rescued){
            own.putString(TOAST,RESCUED);stage(p,own,TOAST_ASK);
            give(p,new ItemStack(LongerRegistry.TOAST.get()));a.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(LongerRegistry.TOAST.get()));
            line(p,a,LongerTexts.RESCUED_LINE);toNow(p,a,own);return;
        }
        stage(p,own,TOAST_ASK);
        Longer.cue(p,LongerTexts.ONE_BURNT);
        Talk.ask(p,Talk.reply(LongerTexts.TEASE,r->toast(r,TEASED)),Talk.act(LongerTexts.OFFER,r->toast(r,OFFERED)),Talk.reply(LongerTexts.BRAVE_ASK,r->toast(r,BRAVE)));
    }
    private static void toast(ServerPlayer p,String how){
        var a=rowan(p);var own=own(p);if(a==null||stage(own)!=TOAST_ASK)return;
        own.putString(TOAST,how);save(p,own);
        boolean readerGood=how.equals(TEASED);
        give(p,new ItemStack(readerGood?LongerRegistry.TOAST.get():LongerRegistry.BURNT_TOAST.get()));
        a.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(readerGood?LongerRegistry.BURNT_TOAST.get():LongerRegistry.TOAST.get()));
        switch(how){
            case TEASED->line(p,a,LongerTexts.TEASED_LINE);
            case OFFERED->line(p,a,LongerTexts.OFFERED_LINE);
            default->{line(p,a,LongerTexts.POLITE);Talk.line(p,"",LongerTexts.BRAVE_LINE);line(p,a,LongerTexts.WORSE);}
        }
        toNow(p,a,own);
    }

    // ------------------------------------------------------------------------------------------------ now
    private static void toNow(ServerPlayer p,LiteraryActor a,CompoundTag own){stage(p,own,NOW);ask(p,a,0,false);}
    private static final String[] KEYS={MUSIC,PLAN,FIRST};
    private static String question(int i){return switch(i){case 0->LongerTexts.Q_MUSIC;case 1->LongerTexts.Q_PLAN;default->LongerTexts.Q_FIRST;};}
    private static String answer(int i){return switch(i){case 0->LongerTexts.A_MUSIC;case 1->LongerTexts.A_PLAN;default->LongerTexts.A_FIRST;};}
    /** Rowan's question i; the reader's three answers; Rowan's own answer after whichever is given. */
    private static void ask(ServerPlayer p,LiteraryActor a,int i,boolean begin){
        if(begin||!Talk.key(p).equals(LongerRowan.TALK))Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);
        line(p,a,question(i));
        var options=LongerTexts.ANSWERS_NOW.get(i);
        Talk.ask(p,Talk.reply(options.get(0),r->answered(r,i,0)),Talk.reply(options.get(1),r->answered(r,i,1)),Talk.reply(options.get(2),r->answered(r,i,2)));
    }
    private static void answered(ServerPlayer p,int i,int choice){
        var a=rowan(p);var own=own(p);if(a==null||stage(own)!=NOW||own.contains(KEYS[i]))return;
        own.putInt(KEYS[i],choice);save(p,own);line(p,a,answer(i));
        if(i<2)ask(p,a,i+1,false);
        else{stage(p,own,DONE);Talk.end(p);var b=base(p);if(b!=null)work(b).doneAt=p.serverLevel().getGameTime();}
    }

    // ------------------------------------------------------------------------------------------------ a hand on Rowan
    /** A hand on Rowan once the bench is put away. True if this chapter answered it. */
    static boolean talk(ServerPlayer p,LiteraryActor a,CompoundTag own,boolean atBench){
        if(LongerBench.stage(own)!=LongerBench.DONE)return false;
        var b=base(p);if(b==null)return false;var w=work(b);
        int st=stage(own);
        switch(st){
            case NONE->{
                if(!atBench)return false;
                int declined=own.getInt(DECLINED);
                if(declined==0){w.reader=p.getUUID();offer(p,a,own);return true;}
                if(declined==1){
                    // The offer made once more, and then let be.
                    own.putInt(DECLINED,2);stage(p,own,STAY_ASK);w.reader=p.getUUID();
                    Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerTexts.STILL_THERE);line(p,a,LongerTexts.STAY_FOR);askStay(p);return true;
                }
                return false;
            }
            case OFFER->{if(!Talk.resume(p,LongerRowan.TALK)){Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerTexts.MAKING_TEA);askPart(p);}return true;}
            case PAUSE->{return true;}
            case STAY_ASK->{if(!Talk.resume(p,LongerRowan.TALK)){Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerTexts.STAY_FOR);askStay(p);}return true;}
            case UP,BOIL,POURED->{Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerTexts.KETTLE_ON);Talk.end(p);return true;}
            case SEATED,TOASTING->{
                Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);
                if(!own.getBoolean("Kitchen0624")&&!own.getBoolean(NOTICED)){own.putBoolean(NOTICED,true);save(p,own);line(p,a,LongerTexts.NOTICED_LINE);}
                else line(p,a,own.getBoolean(CUP)?(st==SEATED?LongerTexts.TOAST_ASK:LongerTexts.HEAT):LongerTexts.ROWAN_TEA);
                Talk.end(p);return true;
            }
            case TOAST_ASK,NOW->{Talk.resume(p,LongerRowan.TALK);return true;}
            case DONE->{
                if((own.getBoolean("Kitchen0624")||own.getBoolean(NOTICED))&&LongerRadio.talk(p,a,own))return true;
                Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);
                if(!own.getBoolean("Kitchen0624")&&!own.getBoolean(NOTICED)){own.putBoolean(NOTICED,true);save(p,own);line(p,a,LongerTexts.NOTICED_LINE);}
                else line(p,a,LongerTexts.ROWAN_KETTLE_AFTER);
                Talk.end(p);return true;
            }
            default->{return false;}
        }
    }

    /** Into the reader's hands, or at their feet if those are full. */
    private static void give(ServerPlayer p,ItemStack s){if(!p.getInventory().add(s)){var e=new ItemEntity(p.serverLevel(),p.getX(),p.getY()+.3,p.getZ(),s);e.setPickUpDelay(0);p.serverLevel().addFreshEntity(e);}}

    // ------------------------------------------------------------------------------------------------ for cases
    /** Rowan's leg of the walk or errand this chapter. */
    public static int leg(BlockPos b){return work(b).leg;}
    /** For cases: the tea finished this long ago. */
    public static void doneWaited(BlockPos b,long ticks){work(b).doneAt-=ticks;}
    /** The kettle's clock, as if this many ticks had passed. */
    public static void boiled(BlockPos b,long ticks){var w=work(b);w.boilAt-=ticks;}
    /** The toaster's clock, as if this many ticks had passed. */
    public static void toasted(BlockPos b,long ticks){var w=work(b);w.toastAt-=ticks;}
    /** The offer's clock (the pause, or by the chair), as if this many ticks had passed. */
    public static void waited(BlockPos b,long ticks){var w=work(b);w.at-=ticks;}
}
