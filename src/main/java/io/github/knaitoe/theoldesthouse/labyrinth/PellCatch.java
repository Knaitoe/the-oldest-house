package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * 0.4.601, a ball, and a game of catch with Callum (docs/PELL_HOUSE_NOTES_0_4_601.md; put by in 0.4.571). The owner, playing the
 * house: "there need to be more idle activities for players in the pell house, too ... things for characters to do or tasks they're
 * assigned with so they're not just stuck waiting out the day."
 *
 * <p><b>The ball.</b> On the first two visits a tennis ball lies in the drive by the kennel: the dog's, and there is no dog. It is
 * picked up by walking over it, as anything dropped is. Used, it is thrown the way its reader looks (the Polybius prize's own ball,
 * PrizeBall, with this one in it): it flies, bounces off whatever it meets with the sound of a ball, and lies where it stops.
 *
 * <p><b>Callum.</b> He does not go after it and he does not speak. While the house waits on its hour (the afternoon, the evening),
 * standing where he stands, he catches a ball a reader has thrown that comes within his reach before it has met a floor or a wall;
 * he holds it a beat (PellNight.BEAT, the beat he copies a reader by at night), and throws it back the way it came: to where its
 * reader's hand was when it left it, in the time it took to reach him. "He copies the older sibling's movements a beat late." A reader still standing there has it in their
 * hands again, if it reaches them before it reaches the floor; one who has moved watches it come down where they stood. He does not
 * look at the ball: he watches the reader.
 *
 * <p>The ball is the house's: one who leaves has it taken back, and one lost (over the pool's fence with the gate locked, up on a
 * roof, off down the street) is by the kennel again once nobody is looking there. Each house has its own. A reader's count of catches, and the most they
 * kept it going, are their own ("Catches0601", "Rally0601"). Nothing here changes what the visit does or when.
 */
public final class PellCatch {
    private PellCatch(){}
    /** Where the ball lies at the start of a visit: in the drive, before the kennel's mouth, beside the bowl. */
    public static final Vec3 KENNEL=new Vec3(18.4,.05,-79.5);
    /** How hard a reader throws it; the game's own pull and drag on anything thrown (ThrowableProjectile). */
    public static final float SPEED=.75F;
    static final double G=.03,DRAG=.99;
    /** How near Callum's chest a ball comes, or will have come by its next step, to be caught; how near a reader's hands; how high
     *  his hands are; how far in front of him. */
    public static final double REACH=1.4,HANDS=1.1,CHEST=.62,FRONT=.3;
    /** What a bounce of this ball keeps across the face it meets and along it: felt, not the prize's rubber (PrizeBall). */
    public static final double BOUNCE=.5,ROLL=.62;
    /** How long his throw stays in his arms after the ball has left them; the shortest and longest flights he sends it back in; and
     *  the hardest he throws. */
    public static final int THROWN=8,MIN_FLIGHT=6,MAX_FLIGHT=26;
    static final double MAX_SPEED=1.25;
    /** Where the catching is: nothing; the ball in Callum's hands; his throw in the air. */
    public static final int NONE=0,HELD=1,BACK=2;
    /** How long a ball lies out of reach (in the pool's yard behind its locked gate, up on a roof) before it is gone from there; how
     *  often the ball is looked for; and how high above the ground is out of reach. */
    public static final int POOL_WAIT=300,KEEP=20;
    static final double HIGH=2.5;
    /** In a reader's record: how many of his throws they have caught, the most there-and-backs they kept going, and that they have
     *  found the ball. */
    public static final String CATCHES="Catches0601",RALLY="Rally0601",FOUND="FoundBall0601";

    static ItemStack ball(BlockPos b){return PellJobs.tagged(PellRegistry.BALL.get(),1,b);}
    static boolean is(ItemStack s,@Nullable BlockPos b){return PellJobs.of(s,PellRegistry.BALL.get(),b);}
    /** Whether Callum may play: the house waiting on its hour on the first two visits. */
    static boolean open(PellPeople.Household h){int w=PellIdle.window(h);return h.visit<PellWeek.LAST&&w!=PellIdle.NONE&&!PellIdle.due(h,w);}
    /** Standing where he stands, doing nothing that takes his hands off the ground: not walking, not at the table, not asleep, not sat
     *  against the wall. */
    static boolean standing(PellPeople.Household h,LiteraryActor callum){
        int c=PellPeople.CALLUM;
        return h.way[c].isEmpty()&&!h.seated[c]&&!h.asleep[c]&&h.perch[c]==null&&callum.gesture()!=PellPeople.SITTING&&!h.callumOut&&!h.callumStrip;
    }
    /** The ball in his hands: his errands and the hour wait for him to throw it (PellIdle). */
    static boolean busy(PellPeople.Household h){return h.catching==HELD;}
    private static Vec3 chest(LiteraryActor callum){return callum.position().add(0,CHEST,0);}
    /** How near a ball in the air comes to a point between where it is and where its next step takes it. */
    private static double pass(PrizeBall ball,Vec3 point){
        var at=ball.position();var v=ball.getDeltaMovement();double len=v.lengthSqr();
        double t=len<1e-8?0:Mth.clamp(point.subtract(at).dot(v)/len,0,1);
        return at.add(v.scale(t)).distanceTo(point);
    }
    private static Vec3 hands(ServerPlayer p){return p.getEyePosition().add(0,-.35,0);}
    /** What he does with his arms when he has no ball: what his errand has him doing, or nothing. */
    private static void rest(PellPeople.Household h,LiteraryActor callum){
        int c=PellPeople.CALLUM;var e=h.idleErrand[c];
        callum.gesture(h.idleState[c]==PellIdle.AT&&e!=null?e.gesture():0);
    }

    // ------------------------------------------------------------------------------------------------ thrown
    /** A reader uses the ball (PellBallItem): it leaves their hand the way they look. */
    static void thrown(ServerPlayer p,ItemStack one){
        var l=p.serverLevel();var ball=new PrizeBall(l,p);
        ball.setItem(one);ball.shootFromRotation(p,p.getXRot(),p.getYRot(),0F,SPEED,0F);
        if(!l.addFreshEntity(ball)){if(!p.getInventory().add(one))p.drop(one,false);return;}
        var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null||!is(one,b))return;
        var h=PellPeople.household(b);
        // A throw while he has the ball, or while his own is in the air, is not his to answer: the one he is answering stays.
        if(h.catching!=NONE)return;
        h.ball=ball.getUUID();h.ballBy=p.getUUID();h.ballFrom=ball.position();h.ballAt=l.getGameTime();
    }
    /** The ball met something (PrizeBall): the sound of it, and what June says of a ball in her hall. */
    static void bounced(PrizeBall ball,BlockHitResult hit,double speed){
        if(!(ball.level() instanceof ServerLevel l))return;
        if(speed>.06)l.playSound(null,ball.getX(),ball.getY(),ball.getZ(),PellRegistry.BALL_BOUNCE.get(),SoundSource.NEUTRAL,(float)Math.min(.8,.2+speed*.7),(float)(.85+Math.min(.4,speed*.4)));
        if(!(ball.getOwner() instanceof ServerPlayer p))return;
        var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null||!is(ball.getItem(),b))return;
        if(PellSite.inHouse(ball.position().subtract(b.getX(),b.getY(),b.getZ())))PellPeople.household(b).ballIndoors=l.getGameTime();
    }

    // ------------------------------------------------------------------------------------------------ every tick
    /** Once a tick for the house, after the visit and the errands. */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        if(h.visit>=PellWeek.LAST)return;
        var callum=PellPeople.find(l,b,PellPeople.CALLUM);
        if(h.catching==HELD){
            if(callum==null){h.catching=NONE;h.ball=null;}
            else if(now-h.caughtAt>=PellNight.BEAT)sendBack(l,b,h,callum,now);
        }else if(h.ball!=null){
            var flying=l.getEntity(h.ball) instanceof PrizeBall ball&&ball.isAlive()?ball:null;
            // Caught on the full or not at all: once it has met a floor or a wall it is nobody's catch, and goes where it goes.
            if(flying==null||flying.bounces>0)landed(l,b,h,callum,here,now);
            else if(h.catching==BACK)toReader(l,b,h,callum,flying,now);
            else toCallum(l,b,h,callum,flying,now);
        }
        // His arms out of the throw.
        if(callum!=null&&h.catching!=HELD&&callum.gesture()==PellPeople.THROW&&now-h.thrownAt>=THROWN)rest(h,callum);
        june(l,b,h,here,now);
        if(now%KEEP==0)keep(l,b,h,here,now);
    }
    /** A reader's throw in the air: his hands come up for it while it comes his way, and within his reach he has it. */
    private static void toCallum(ServerLevel l,BlockPos b,PellPeople.Household h,@Nullable LiteraryActor callum,PrizeBall ball,long now){
        if(callum==null||!open(h)||!standing(h,callum))return;
        var to=chest(callum).subtract(ball.position());double d=to.length();
        boolean coming=d<12&&ball.getDeltaMovement().dot(to)>0;
        if(coming&&callum.gesture()!=PellPeople.CATCH&&callum.getMainHandItem().isEmpty())callum.gesture(PellPeople.CATCH);
        if(!coming&&callum.gesture()==PellPeople.CATCH)rest(h,callum);
        if(pass(ball,chest(callum))>REACH)return;
        var stack=ball.getItem().copy();ball.discard();
        callum.setItemInHand(InteractionHand.MAIN_HAND,stack);callum.gesture(PellPeople.CATCH);
        l.playSound(null,callum.getX(),callum.getY()+CHEST,callum.getZ(),PellRegistry.BALL_BOUNCE.get(),SoundSource.NEUTRAL,.35F,1.5F);
        // How long it would have been in the air to reach him, had his hands not come out to it.
        double whole=h.ballFrom==null?0:chest(callum).distanceTo(h.ballFrom),gone=h.ballFrom==null?0:ball.position().distanceTo(h.ballFrom);
        double flight=(now-h.ballAt)*(gone>1e-3&&whole>gone?whole/gone:1);
        h.catching=HELD;h.caughtAt=now;h.ballFlight=(int)Math.max(MIN_FLIGHT,Math.min(MAX_FLIGHT,Math.round(flight)));h.ball=null;
    }
    /** A beat after he caught it: back the way it came, to where its reader's hand was, in the time it took to reach him; or, with no
     *  way through the air for it, along the floor. */
    private static void sendBack(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor callum,long now){
        var stack=callum.getMainHandItem().copy();callum.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        callum.gesture(PellPeople.THROW);h.thrownAt=now;
        if(stack.isEmpty()||h.ballFrom==null){h.catching=NONE;return;}
        var flat=new Vec3(h.ballFrom.x-callum.getX(),0,h.ballFrom.z-callum.getZ());
        var dir=flat.lengthSqr()<1e-4?Vec3.directionFromRotation(0,callum.getYRot()):flat.normalize();
        var from=chest(callum).add(dir.scale(FRONT));
        var v=lob(l,from,h.ballFrom,h.ballFlight);
        if(v==null)v=new Vec3(dir.x*.42,.12,dir.z*.42);
        var ball=new PrizeBall(l,callum);ball.setItem(stack);ball.setPos(from.x,from.y,from.z);ball.setDeltaMovement(v);
        if(!l.addFreshEntity(ball)){lie(l,from,stack);h.catching=NONE;h.ball=null;return;}
        h.ball=ball.getUUID();h.catching=BACK;h.ballAt=now;
    }
    /**
     * The speed that takes something thrown from one place to another in so many ticks, by the game's own rule for it (each tick it
     * moves by its speed, then its speed is scaled by DRAG and pulled down by G). If the way is blocked, or would need a harder throw
     * than he has, a flatter and quicker one is tried, down to the quickest; null if none is clear.
     */
    static @Nullable Vec3 lob(ServerLevel l,Vec3 from,Vec3 to,int ticks){
        var d=to.subtract(from);
        for(int t=Math.min(MAX_FLIGHT,ticks);t>=MIN_FLIGHT;t--){
            double a=(1-Math.pow(DRAG,t))/(1-DRAG);
            var v=new Vec3(d.x/a,(d.y+G*(t-a)/(1-DRAG))/a,d.z/a);
            if(v.length()>MAX_SPEED)break;
            if(clear(l,from,v,t))return v;
        }
        return null;
    }
    private static boolean clear(ServerLevel l,Vec3 from,Vec3 v,int ticks){
        var at=from;
        for(int i=0;i<ticks;i++){
            var next=at.add(v);
            if(l.clip(new ClipContext(at,next,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,CollisionContext.empty())).getType()!=HitResult.Type.MISS)return false;
            at=next;v=new Vec3(v.x*DRAG,v.y*DRAG-G,v.z*DRAG);
        }
        return true;
    }
    /** His throw in the air: within reach of the hands of the reader he threw it to, it is theirs again. */
    private static void toReader(ServerLevel l,BlockPos b,PellPeople.Household h,@Nullable LiteraryActor callum,PrizeBall ball,long now){
        var p=h.ballBy==null?null:l.getServer().getPlayerList().getPlayer(h.ballBy);
        if(p==null||p.level()!=l||!p.isAlive()||p.isSpectator()||pass(ball,hands(p))>HANDS)return;
        var stack=ball.getItem().copy();ball.discard();
        if(p.getMainHandItem().isEmpty())p.setItemInHand(InteractionHand.MAIN_HAND,stack);
        else if(!p.getInventory().add(stack))p.drop(stack,false);
        l.playSound(null,p.getX(),p.getY()+1,p.getZ(),SoundEvents.ITEM_PICKUP,SoundSource.PLAYERS,.3F,.8F);
        h.catching=NONE;h.ball=null;h.rally++;
        var own=PellHouse.own(p);int n=own.getInt(CATCHES)+1;own.putInt(CATCHES,n);own.putInt(RALLY,Math.max(own.getInt(RALLY),h.rally));PellHouse.save(p,own);
        String line=n==1?PellTexts.BALL_BACK:n==2?PellTexts.BALL_WET:h.rally==5?PellTexts.BALL_NOT_LOOKED:h.rally>5&&h.rally%5==0?PellTexts.rally(h.rally):null;
        if(line!=null)PellHouse.cue(p,line);
    }
    /** The ball has come down, or is gone: nobody caught it. */
    private static void landed(ServerLevel l,BlockPos b,PellPeople.Household h,@Nullable LiteraryActor callum,List<ServerPlayer> here,long now){
        var p=h.ballBy==null?null:l.getServer().getPlayerList().getPlayer(h.ballBy);
        boolean his=h.catching==BACK;
        h.catching=NONE;h.ball=null;h.rally=0;
        if(callum!=null&&callum.gesture()==PellPeople.CATCH)rest(h,callum);
        if(p==null||!here.contains(p))return;
        if(his){if(!h.ballMissed){h.ballMissed=true;PellHouse.cue(p,PellTexts.BALL_WHERE_YOU_STOOD);}}
        // A throw of theirs he let go by: only said of one thrown in the house while he was there to catch it.
        else if(!h.ballPassed&&callum!=null&&open(h)&&standing(h,callum)&&h.ballFrom!=null&&PellSite.inHouse(h.ballFrom.subtract(b.getX(),b.getY(),b.getZ()))
            &&PellSite.inHouse(callum.position().subtract(b.getX(),b.getY(),b.getZ()))){h.ballPassed=true;PellHouse.cue(p,PellTexts.CALLUM_LETS_IT_GO);}
    }
    /** June, at her stove in the afternoon, of a ball in her hall: once a visit, when she has the breath. */
    private static void june(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        if(h.ballJune||h.ballIndoors==0||now-h.ballIndoors>60||PellIdle.window(h)!=PellIdle.AFTERNOON)return;
        var june=PellPeople.find(l,b,PellPeople.JUNE);
        if(june==null||!PellVisit.inKitchen(l,b,PellPeople.JUNE)||!PellVisit.ready(h,now,june))return;
        h.ballJune=true;PellVisit.say(h,june,PellPeople.JUNE,PellTexts.JUNE_BALL,now);
    }
    /** A hand on Callum with the ball in it: he looks at it, and at where it should be thrown from. True if it answered. */
    static boolean shown(ServerPlayer p,BlockPos b,PellPeople.Household h,LiteraryActor callum,CompoundTag own){
        if(!is(p.getMainHandItem(),b))return false;
        PellHouse.cue(p,open(h)&&standing(h,callum)?PellTexts.CALLUM_BALL:PellTexts.CALLUM_BALL_NOT_NOW);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ keeping
    private static void lie(ServerLevel l,Vec3 at,ItemStack stack){
        var item=new ItemEntity(l,at.x,at.y,at.z,stack);item.setDeltaMovement(Vec3.ZERO);item.setDefaultPickUpDelay();item.setUnlimitedLifetime();
        l.addFreshEntity(item);
    }
    private static boolean carries(ServerPlayer p,BlockPos b){
        if(is(p.containerMenu.getCarried(),b))return true;
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(is(inv.getItem(i),b))return true;
        return false;
    }
    private static void takeFrom(ServerPlayer p,BlockPos b){
        if(is(p.containerMenu.getCarried(),b))p.containerMenu.setCarried(ItemStack.EMPTY);
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(is(inv.getItem(i),b))inv.setItem(i,ItemStack.EMPTY);
    }
    /** Every one of this house's balls, wherever it is, gone. */
    private static void gather(ServerLevel l,BlockPos b){
        for(var p:l.getServer().getPlayerList().getPlayers())takeFrom(p,b);
        var area=PellPeople.area(b).inflate(24);
        for(var e:l.getEntitiesOfClass(ItemEntity.class,area,e->is(e.getItem(),b)))e.discard();
        for(var e:l.getEntitiesOfClass(PrizeBall.class,area,e->is(e.getItem(),b)))e.discard();
        var callum=PellPeople.find(l,b,PellPeople.CALLUM);if(callum!=null&&is(callum.getMainHandItem(),b))callum.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
    }
    /**
     * Once a second: a reader who has just found the ball is told what it is; one who has left the house has it taken back; a ball
     * lying out of reach (in the pool's yard behind its locked gate, or up on a roof) is gone from there after a while, unseen; and a
     * ball that is nowhere is by the kennel again, the first moment nobody is looking there.
     */
    private static void keep(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        boolean found=h.ball!=null||h.catching==HELD;
        for(var p:l.getServer().getPlayerList().getPlayers()){
            if(!here.contains(p)){takeFrom(p,b);continue;}
            if(!carries(p,b))continue;
            found=true;
            var own=PellHouse.own(p);
            if(!own.getBoolean(FOUND)){own.putBoolean(FOUND,true);PellHouse.save(p,own);PellHouse.cue(p,PellTexts.BALL_FOUND);}
        }
        var area=PellPeople.area(b).inflate(8);
        for(var e:l.getEntitiesOfClass(PrizeBall.class,area,e->is(e.getItem(),b)))if(e.isAlive())found=true;
        boolean pooled=false;
        for(var e:l.getEntitiesOfClass(ItemEntity.class,area,e->is(e.getItem(),b))){
            if(!e.isAlive())continue;
            var rel=e.position().subtract(b.getX(),b.getY(),b.getZ());
            if(PellSite.inPoolYard(rel)&&!PellSite.open(l,b,PellSite.POOL_GATE)||rel.y>=HIGH){
                pooled=true;
                if(h.ballPooled==0)h.ballPooled=now;
                else if(now-h.ballPooled>=POOL_WAIT&&!LiteraryVignettes.watched(l,e.position())){e.discard();continue;}
            }
            found=true;
        }
        if(!pooled)h.ballPooled=0;
        if(found)return;
        var at=PellVisit.world(b,KENNEL);
        if(!PellPeople.live(l,at)||LiteraryVignettes.watched(l,at.add(0,.2,0)))return;
        // One that has gone further than the house looks for it (over a fence, down the street) is gone: there is one ball.
        var wide=PellPeople.area(b).inflate(48);
        for(var e:l.getEntitiesOfClass(ItemEntity.class,wide,e->is(e.getItem(),b)))e.discard();
        for(var e:l.getEntitiesOfClass(PrizeBall.class,wide,e->is(e.getItem(),b)))e.discard();
        lie(l,at,ball(b));
    }
    /** The visit begun again (PellVisit.restart): no ball anywhere but by the kennel, on the first two visits; nothing in the air. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h,int visit){
        gather(l,b);
        h.ball=null;h.ballBy=null;h.ballFrom=null;h.ballAt=0;h.caughtAt=0;h.thrownAt=0;h.ballIndoors=0;h.ballPooled=0;h.catching=NONE;h.rally=0;h.ballFlight=0;
        h.ballMissed=false;h.ballPassed=false;h.ballJune=false;
        if(visit<PellWeek.LAST)lie(l,PellVisit.world(b,KENNEL),ball(b));
    }

    // ------------------------------------------------------------------------------------------------ for the cases
    /** Where the catching is at this house (NONE, HELD, BACK), and how many there-and-backs have been kept going. */
    public static int state(BlockPos b){return PellPeople.household(b).catching;}
    public static int rally(BlockPos b){return PellPeople.household(b).rally;}
    /** The ball in the air at this house, if one is being answered. */
    public static @Nullable PrizeBall flying(ServerLevel l,BlockPos b){var id=PellPeople.household(b).ball;return id!=null&&l.getEntity(id) instanceof PrizeBall ball&&ball.isAlive()?ball:null;}
    /** This house's ball lying somewhere, if it is. */
    public static @Nullable ItemEntity lying(ServerLevel l,BlockPos b){
        for(var e:l.getEntitiesOfClass(ItemEntity.class,PellPeople.area(b).inflate(8),e->is(e.getItem(),b)))if(e.isAlive())return e;
        return null;
    }
    public static boolean isBall(ItemStack s,BlockPos b){return is(s,b);}
}
