package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.KingStatePayload;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.642, A sort of thing, the second part (docs/SLEEPING_KING_0_4_642.md): his breath, and how much of a thing each reader is.
 *
 * <p><b>The breath.</b> One clock for the country, in for four seconds and out for four ({@link #CYCLE}), counted only while a reader is
 * there. Every reader hears each breath, louder the nearer him. On the in-breath the edge of the filled land moves a block out, toward
 * the house and toward the far ranks; on the out-breath it moves back ({@link KingSite#ruled(int,int,int)}): the blocks whose drawing that
 * changes go over to their twin and back.
 *
 * <p><b>The one sum</b> (the proposal's table, of which this part builds the land, being looked at, his in-breath and nowhere):
 * <ul>
 * <li>the land underfoot: filled land a thing (3), the band where the fill gives out a sort of thing (2), ruled land a line (1); the
 *     house is filled;</li>
 * <li>being looked at by another reader who is at least a sort of thing: one step up, and no further than a thing;</li>
 * <li>his in-breath, beside him: one step up, which is the only way past a thing so far (too much of one, 4);</li>
 * <li>off the country altogether, which only a line can reach through its hedges: nowhere (0), and the dream puts them back on the
 *     last square that counted them, unhurt.</li>
 * </ul>
 * What each state does: too much of a thing is rooted (no walking, no jumping); a thing is bound (the hedges are walls, a brook is
 * crossed only at its crossing: KingHedgeBlock, KingVergeBlock); a sort of thing goes through hedges slowly and its feet are soft; a
 * line goes through hedges and brooks and its feet are silent. Each reader's own amount is sent to every reader in the country
 * ({@link KingStatePayload}) and shown on their body (client.KingClient).
 */
public final class KingBreath {
    private KingBreath(){}
    /** In for four seconds, out for four. */
    public static final int CYCLE=160,IN=80;
    /** Beside him: close enough that his in-breath makes too much of a thing. */
    public static final double BESIDE=7;
    public static final int NOWHERE=0,LINE=1,SORT=2,THING=3,TOO_MUCH=4;
    private static final ResourceLocation ROOTED=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"king_rooted");

    /** How much of a thing each reader in the country is; anybody else is their whole self. The client fills it from the payload too. */
    public static final Map<UUID,Integer> STATES=new ConcurrentHashMap<>();
    public static int of(Entity e){return e instanceof Player?STATES.getOrDefault(e.getUUID(),THING):THING;}

    /** The breath, per country: how far through it, the last server tick it was counted on, and the shift the land now shows (-1 unknown). */
    private static final class Clock{int ticks;int last=-1;int shown=-1;int shownLevel=-1;}
    private static final Map<Long,Clock> CLOCKS=new HashMap<>();
    static Clock clock(BlockPos b){return CLOCKS.computeIfAbsent(b.asLong(),k->new Clock());}
    /** Where in the breath the country is (0 to CYCLE-1); the in-breath is the first half. */
    public static int phase(BlockPos b){return clock(b).ticks%CYCLE;}
    public static boolean in(BlockPos b){return phase(b)<IN;}
    /** 0.4.643: which breath this is, counting from the first. */
    public static int cycle(BlockPos b){return clock(b).ticks/CYCLE;}
    /** For tests: set the breath. */
    public static void set(ServerLevel l,BlockPos b,int phase){var c=clock(b);c.ticks=phase;show(l,b,phase<IN?1:0);}
    /** 0.4.643: the land shows his sleep's depth at once (KingSleep, when it changes and when it is put back). */
    static void redraw(ServerLevel l,BlockPos b){var c=clock(b);show(l,b,c.shown<0?0:c.shown);}

    /** Every quarter second for each reader in the country (LiteraryVignettes). */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();var c=clock(b);int now=l.getServer().getTickCount();
        if(c.last!=now){
            // 0.4.643: when he wakes the breathing stops in the middle of a breath (KingSleep), and the clock with it.
            boolean stopped=KingSleep.waking(b);
            int before=c.ticks%CYCLE;if(!stopped)c.ticks+=c.last<0?0:Math.min(20,Math.max(0,now-c.last));c.last=now;int after=c.ticks%CYCLE;
            boolean breathedIn=before>=IN&&after<IN||c.shown<0,breathedOut=before<IN&&after>=IN;
            if(!stopped&&(breathedIn||breathedOut))breathe(l,b,after<IN);
            show(l,b,after<IN?1:0);
            KingSleep.tick(l,b);
        }
        int state=sum(p,b,own);
        // 0.4.643: off the board, a reader who has been to him and heard the brothers leaves him his sleep; anybody else is put back.
        if(state==NOWHERE){if(KingSleep.walkedOff(p,b,own))return;back(p,b,own);state=SORT;}
        STATES.put(p.getUUID(),state);own.putInt("KingState",state);
        root(p,state==TOO_MUCH||KingQueen.holding(p,b));
        KingSleep.reader(p,b,own,state);
        tell(l,b);
    }
    /** A reader leaves the country: their whole self again. */
    public static void leave(ServerPlayer p){
        if(STATES.remove(p.getUUID())!=null)root(p,false);
        // 0.4.644: whatever was swallowed wears off, and the bottle and the cake stay in the country.
        SWALLOWED.remove(p.getUUID());
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(KingDraughtItem.is(inv.getItem(i)))inv.setItem(i,net.minecraft.world.item.ItemStack.EMPTY);
    }

    // ------------------------------------------------------------------------------------------------ the sum
    static int sum(ServerPlayer p,BlockPos b,CompoundTag own){
        var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
        if(!KingSite.inside(r)||r.y<-2.5)return NOWHERE;
        int land=land(p,b,own);
        int state=land;
        // Notice, one step up and no further than a thing: another reader's look, a look from one of the dream's people, or this
        // square's peg having counted them (0.4.644).
        if(state<THING&&(noticed(p,b)||lookedAtByTheDream(p,b)||counted(own,KingSite.rank((int)Math.floor(r.z)))))state++;
        // 0.4.646: named by the court and not yet out, they are a thing while it attends to them; a swallow still takes them a step under.
        if(KingCourt.holding(p,b,own))state=Math.max(state,THING);
        // 0.4.644: what they have swallowed, one step either way, for its twenty seconds; cake past a thing only on a thing.
        var swallowed=SWALLOWED.get(p.getUUID());
        if(swallowed!=null){if(p.level().getGameTime()>=swallowed.until)SWALLOWED.remove(p.getUUID());else state=swallowed.more?Math.min(TOO_MUCH,state+1):Math.max(NOWHERE,state-1);}
        // Held by the Queen's hand or under the Guard's glass, they are a thing.
        if(KingQueen.holding(p,b)||KingGuard.holding(p,b))state=Math.max(state,THING);
        if(state==NOWHERE)return NOWHERE;
        if(beside(r)&&in(b))state=Math.min(TOO_MUCH,state+1);
        // The last square that counted them: where they last stood as a thing on the ground.
        if(state>=THING&&p.onGround()){own.putDouble("KingBackX",r.x);own.putDouble("KingBackY",r.y);own.putDouble("KingBackZ",r.z);}
        return state;
    }
    /** What the ground under them makes of them; in the air, what it last made of them. */
    static int land(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();var under=BlockPos.containing(p.getX(),p.getY()-.2,p.getZ());var st=l.getBlockState(under);
        if(st.isAir())st=l.getBlockState(under.below());
        int land;
        if(st.isAir())land=own.contains("KingLand")?own.getInt("KingLand"):THING;
        else if(!KingRegistry.isCountry(st))land=THING;
        else if(KingRegistry.ruled(st))land=LINE;
        else{var rel=under.subtract(b);var c=clock(b);land=KingSite.ruledness(rel.getX(),rel.getZ(),c.shown<0?0:c.shown,Math.max(0,c.shownLevel))>0?SORT:THING;}
        own.putInt("KingLand",land);
        return land;
    }
    public static boolean beside(Vec3 r){var k=Vec3.atBottomCenterOf(KingSite.KING);return r.distanceTo(k)<BESIDE;}
    /** Being looked at by another reader in the country who is at least a sort of thing, with nothing between them. */
    static boolean noticed(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();
        for(var q:l.players()){
            if(q==p||q.isSpectator()||!q.isAlive()||STATES.getOrDefault(q.getUUID(),NOWHERE)<SORT)continue;
            if(!KingSite.inside(q.position().subtract(b.getX(),b.getY(),b.getZ()))||q.distanceTo(p)>48)continue;
            if(looksAt(q,p))return true;
        }
        return false;
    }
    /** 0.4.644: looked at by one of the dream's people (the Queen, the Guard, the brothers), with nothing between them. */
    static boolean lookedAtByTheDream(ServerPlayer p,BlockPos b){
        for(var a:p.serverLevel().getEntitiesOfClass(LiteraryActor.class,p.getBoundingBox().inflate(32),a->KingQueen.is(a)||KingGuard.is(a)||KingBrothers.is(a)||KingCaterpillar.is(a)||KingCourt.is(a))){
            if(!a.isAlive())continue;
            var eye=a.getEyePosition();var to=p.getBoundingBox().getCenter().subtract(eye);double d=to.length();if(d<1e-3)return true;
            var view=Vec3.directionFromRotation(a.getXRot(),a.getYHeadRot());
            if(view.dot(to.scale(1/d))<Math.cos(Math.toRadians(25)))continue;
            var hit=a.level().clip(new ClipContext(eye,p.getBoundingBox().getCenter(),ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,a));
            if(hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceTo(eye)>=d-.6)return true;
        }
        return false;
    }
    /** 0.4.644: whether this square's peg has counted them. */
    static boolean counted(CompoundTag own,int rank){return rank>=2&&(own.getInt(KingSleep.COUNTED)&1<<rank)!=0;}
    /** 0.4.644: a swallow from the bottle (less) or a bite of the cake (more), until a game time. */
    record Swallowed(boolean more,long until){}
    static final Map<UUID,Swallowed> SWALLOWED=new ConcurrentHashMap<>();
    public static void swallow(ServerPlayer p,boolean more,long until){SWALLOWED.put(p.getUUID(),new Swallowed(more,until));}
    /** 0.4.646: whether a swallow from the bottle is still taking this reader a step under what they would be. */
    public static boolean swallowedLess(ServerPlayer p){var w=SWALLOWED.get(p.getUUID());return w!=null&&!w.more&&p.level().getGameTime()<w.until;}
    public static boolean looksAt(Player q,Entity p){
        var eye=q.getEyePosition();var to=p.getBoundingBox().getCenter().subtract(eye);double d=to.length();if(d<1e-3)return true;
        if(q.getViewVector(1).dot(to.scale(1/d))<Math.cos(Math.toRadians(25)))return false;
        var hit=q.level().clip(new ClipContext(eye,p.getBoundingBox().getCenter(),ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,q));
        return hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceTo(eye)>=d-.6;
    }

    // ------------------------------------------------------------------------------------------------ what the states do
    static void root(ServerPlayer p,boolean on){
        for(var attribute:List.of(Attributes.MOVEMENT_SPEED,Attributes.JUMP_STRENGTH)){
            var a=p.getAttribute(attribute);if(a==null)continue;
            if(on&&!a.hasModifier(ROOTED))a.addTransientModifier(new AttributeModifier(ROOTED,-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            if(!on)a.removeModifier(ROOTED);
        }
    }
    /** Nowhere: the dream puts them back on the last square that counted them, a sort of thing, unhurt. */
    static void back(ServerPlayer p,BlockPos b,CompoundTag own){
        double x=own.contains("KingBackX")?own.getDouble("KingBackX"):.5,y=own.contains("KingBackY")?own.getDouble("KingBackY"):0,z=own.contains("KingBackZ")?own.getDouble("KingBackZ"):KingSite.NEAR-2;
        p.fallDistance=0;p.setDeltaMovement(Vec3.ZERO);
        p.teleportTo(p.serverLevel(),b.getX()+x,b.getY()+y,b.getZ()+z,p.getYRot(),p.getXRot());
        p.displayClientMessage(Component.literal(KingTexts.NOWHERE).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
    }
    static void tell(ServerLevel l,BlockPos b){
        var list=new ArrayList<KingStatePayload.Entry>();var here=new ArrayList<ServerPlayer>();
        for(var q:l.players()){var s=STATES.get(q.getUUID());if(s==null||!KingSite.inside(q.position().subtract(b.getX(),b.getY(),b.getZ())))continue;list.add(new KingStatePayload.Entry(q.getUUID(),s));here.add(q);}
        var payload=new KingStatePayload(list);
        for(var q:here)HousePackets.send(q,payload);
    }

    // ------------------------------------------------------------------------------------------------ the breath
    /** Each reader hears the breath from his direction, louder the nearer him, over the whole country. */
    static void breathe(ServerLevel l,BlockPos b,boolean in){
        var sound=in?KingRegistry.BREATH_IN.get():KingRegistry.BREATH_OUT.get();var holder=BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound);
        var king=Vec3.atCenterOf(b.offset(KingSite.KING));
        for(var q:l.players()){
            if(!STATES.containsKey(q.getUUID())&&!q.isSpectator())continue;
            if(!KingSite.inside(q.position().subtract(b.getX(),b.getY(),b.getZ())))continue;
            var to=king.subtract(q.getEyePosition());double d=to.length();
            var at=d<6?king:q.getEyePosition().add(to.scale(6/d));
            float volume=(float)Math.max(.18,Math.min(1,1.15-d/110));
            ((ServerPlayer)q).connection.send(new ClientboundSoundPacket(holder,SoundSource.AMBIENT,at.x,at.y,at.z,volume,in?1F:.96F,l.random.nextLong()));
        }
    }
    /** The land shows the shift it should: each block whose drawing differs between the two goes over to the right twin. */
    static void show(ServerLevel l,BlockPos b,int shift){
        var c=clock(b);int level=KingSleep.level(b);if(c.shown==shift&&c.shownLevel==level)return;
        // 0.4.643: a change in how deep he sleeps redraws every column that any depth draws differently; a breath only its own band.
        for(long cell:c.shownLevel!=level?KingSite.changing():KingSite.breathing(level)){
            int x=BlockPos.getX(cell),z=BlockPos.getZ(cell);var column=b.offset(x,0,z);if(!l.isLoaded(column))continue;
            boolean ruled=KingSite.ruled(x,z,shift,level);
            for(int y=-3;y<=8;y++){var at=column.atY(b.getY()+y);var st=l.getBlockState(at);var twin=KingRegistry.twin(st,ruled);if(twin!=null&&twin!=st)l.setBlock(at,twin,net.minecraft.world.level.block.Block.UPDATE_CLIENTS|net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE);}
        }
        c.shown=shift;c.shownLevel=level;
    }
    /** For tests: the shift the land shows (-1 before the first breath). */
    public static int shown(BlockPos b){return clock(b).shown;}
    /** For tests: the depth of sleep the land shows (-1 before the first breath). */
    public static int shownLevel(BlockPos b){return clock(b).shownLevel;}
}
