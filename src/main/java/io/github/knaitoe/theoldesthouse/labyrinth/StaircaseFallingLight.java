package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.StairLightFallPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.594: a light let fall into the great staircase's well (docs/STAIR_FALLING_LIGHT_0_4_594.md).
 *
 * <p>The owner, 15 November, on how the staircase should show its depth: "I remember the staircase as dark in the novel." In the
 * novel the well is never lit and never seen. Its depth is known by what the dark does to a light let fall into it: the light is
 * swallowed, and nobody hears it land. So a reader who drops anything that burns over a rail may watch it go. It falls as a point
 * of its own light, in view long after the stair's dark would have hidden it, until the dark takes it. It is heard to land only
 * where something is near enough below for it to land on.
 *
 * <p>Nothing is lit by it and no rule changes. The fall is worked out once, here, against the blocks that are really there, and
 * told to every reader in the well; each client draws it ({@code client.StaircaseFallingLights}). The thing itself is out of
 * the world only while it falls: when the fall is over it lies where it came down, as it would have had it fallen all that way
 * as a dropped thing. Nothing let fall is lost.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StaircaseFallingLight {
    /** A light is anything whose block gives at least this much light: a torch, a lantern, and the like. */
    public static final int LIGHT=7;
    /** How far below the hand that let it go, and still falling, it must be before the well has it. Short of that it is a dropped thing. */
    public static final double CLEAR=6;
    /** One tick of a fall: gravity, the most it falls in a tick, and how fast its sideways drift dies. */
    public static final double GRAVITY=.04,TERMINAL=2,DRIFT=.9;
    /** How far off its landing is heard, the longest a fall is followed (ticks), and how long one may be on its way to the well. */
    public static final double HEARD=48;
    public static final int LONGEST=2000,WAIT=200;
    public static final int WARM=0xFFB25C,SOUL=0x70E6EC,RED=0xFF5A3C;
    private StaircaseFallingLight(){}

    /** A falling light's place and speed. One step is one tick, and it is the same on the server and on every client. */
    public static final class Motion {
        public double x,y,z,vx,vy,vz;
        public Motion(Vec3 at,Vec3 speed){x=at.x;y=at.y;z=at.z;vx=speed.x;vy=Math.max(speed.y,-TERMINAL);vz=speed.z;}
        public void step(){vy=Math.max(vy-GRAVITY,-TERMINAL);vx*=DRIFT;vz*=DRIFT;x+=vx;y+=vy;z+=vz;}
    }
    /**
     * One fall: where it began and how fast, how many ticks it lasts and where it ends. {@code hit} says it came down on something;
     * otherwise the world it could be followed through ran out first.
     */
    public record Fall(Vec3 start,Vec3 speed,int ticks,Vec3 end,boolean hit,int tint){}

    public static boolean isLight(ItemStack stack){
        return stack.getItem() instanceof BlockItem item&&item.getBlock().defaultBlockState().getLightEmission()>=LIGHT;
    }
    /** The tint of its flame: a soul fire's, a redstone torch's, or an ordinary flame's. */
    public static int tint(ItemStack stack){
        Block block=stack.getItem() instanceof BlockItem item?item.getBlock():Blocks.AIR;
        if(block==Blocks.SOUL_TORCH||block==Blocks.SOUL_LANTERN||block==Blocks.SOUL_CAMPFIRE)return SOUL;
        return block==Blocks.REDSTONE_TORCH?RED:WARM;
    }
    /** Inside the well's walls, between its two caps. */
    public static boolean inWell(BlockPos origin,Vec3 at){
        var b=FinaleArchitecture.base(origin);
        return Math.abs(at.x-b.getX()-.5)<FinaleArchitecture.SHAFT_RADIUS&&Math.abs(at.z-b.getZ()-.5)<FinaleArchitecture.SHAFT_RADIUS
                &&at.y>=FinaleArchitecture.LOOP_BOTTOM&&at.y<=FinaleArchitecture.TOP+15;
    }

    private static boolean solid(ServerLevel level,BlockPos at){return !level.getBlockState(at).getCollisionShape(level,at).isEmpty();}
    /**
     * Follows a fall through the blocks that are there. A light that would drift into a wall on its way is followed again with no
     * drift, so the path a client draws from the same start and speed is the path taken here.
     */
    public static Fall fall(ServerLevel level,Vec3 start,Vec3 speed,int tint){
        var drifting=descend(level,start,speed,tint);
        return drifting!=null?drifting:descend(level,start,new Vec3(0,speed.y,0),tint);
    }
    private static Fall descend(ServerLevel level,Vec3 start,Vec3 speed,int tint){
        var m=new Motion(start,speed);Vec3 used=new Vec3(m.vx,m.vy,m.vz);var at=new BlockPos.MutableBlockPos();var beside=new BlockPos.MutableBlockPos();
        boolean drift=m.vx!=0||m.vz!=0;
        for(int t=1;t<=LONGEST;t++){
            double px=m.x,py=m.y,pz=m.z;m.step();
            // No more than half a block between looks, so nothing a block thick is passed through.
            int parts=Math.max(1,(int)Math.ceil(Math.max(Math.abs(m.y-py),Math.hypot(m.x-px,m.z-pz))*2));
            double lx=px,lz=pz;
            for(int i=1;i<=parts;i++){
                double f=i/(double)parts,x=px+(m.x-px)*f,y=py+(m.y-py)*f,z=pz+(m.z-pz)*f;at.set(Mth.floor(x),Mth.floor(y),Mth.floor(z));
                if(level.isOutsideBuildHeight(at)||!level.isLoaded(at))return new Fall(start,used,t,new Vec3(x,y,z),false,tint);
                if(solid(level,at)){
                    // Stopped from the side: the column it was in is still open at this height.
                    beside.set(Mth.floor(lx),at.getY(),Mth.floor(lz));
                    if(drift&&!beside.equals(at)&&!solid(level,beside))return null;
                    double top=at.getY()+level.getBlockState(at).getCollisionShape(level,at).max(Direction.Axis.Y);
                    return new Fall(start,used,t,new Vec3(x,top+.04,z),true,tint);
                }
                lx=x;lz=z;
            }
        }
        return new Fall(start,used,LONGEST,new Vec3(m.x,m.y,m.z),false,tint);
    }

    private record Tossed(ItemEntity item,double from,int since,BlockPos origin){}
    private record Landing(ServerLevel level,long due,Vec3 at,SoundEvent sound,ItemStack stack){}
    private static final List<Tossed> TOSSED=new ArrayList<>();
    private static final List<Landing> LANDINGS=new ArrayList<>();
    /** Forgets what is on its way: a stopped server, and fixtures between batches. */
    public static void clear(){TOSSED.clear();LANDINGS.clear();}
    public static int onTheirWay(){return TOSSED.size();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clear();}
    /** A stopping server sets down everything still on its way, so nothing let fall is lost with it. */
    @SubscribeEvent public static void stopping(ServerStoppingEvent e){
        for(var landing:List.copyOf(LANDINGS))if(landing.level.getServer()==e.getServer())settle(landing,false);
        LANDINGS.removeIf(landing->landing.level.getServer()==e.getServer());
    }

    @SubscribeEvent public static void tossed(ItemTossEvent e){
        if(!(e.getPlayer() instanceof ServerPlayer p)||p.isSpectator())return;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return;
        held(p,e.getEntity(),origin);track(p,e.getEntity(),origin);
    }
    /**
     * A reader leaning over a rail ({@link StaircaseLean}) lets a light go from the hand held out over it: it falls from there, not
     * from where they stand. Only a light. Anything else let go at a rail lands at the reader's feet, as it always did.
     */
    public static boolean held(ServerPlayer p,ItemEntity item,BlockPos origin){
        if(!p.serverLevel().dimension().equals(HouseDimensions.INTERIOR)||!isLight(item.getItem()))return false;
        var lean=StaircaseLean.over(p.level(),origin,p.position(),p.getEyeHeight(),p.getYRot(),p.getXRot());if(lean==null)return false;
        // It drifts on the way the reader leaned: straight out over a flight's rail, or along the diagonal over a corner (0.4.652).
        double across=Math.hypot(lean.x,lean.z);
        item.setPos(p.getX()+lean.x,p.getEyeY()+lean.y-.3,p.getZ()+lean.z);item.setDeltaMovement(lean.x/across*.02,-.05,lean.z/across*.02);
        return true;
    }
    /** A reader in the well has let go of something that burns: it is watched until it lands on the stair or the well has it. */
    public static boolean track(ServerPlayer p,ItemEntity item,BlockPos origin){
        if(!p.serverLevel().dimension().equals(HouseDimensions.INTERIOR)||!inWell(origin,p.position())||!isLight(item.getItem()))return false;
        if(TOSSED.size()>=64)TOSSED.remove(0);
        TOSSED.add(new Tossed(item,item.getY(),p.server.getTickCount(),origin));return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();
        if(!LANDINGS.isEmpty())for(var it=LANDINGS.iterator();it.hasNext();){var landing=it.next();
            if(landing.level.getServer()!=server){it.remove();continue;}
            if(landing.level.getGameTime()<landing.due)continue;
            it.remove();settle(landing,true);
        }
        if(TOSSED.isEmpty())return;
        for(var it=TOSSED.iterator();it.hasNext();){var tossed=it.next();var item=tossed.item;
            // One that has touched down stays watched: a throw that only clips the top of a rail goes over it a tick later.
            if(item.isRemoved()||server.getTickCount()-tossed.since>WAIT
                    ||!(item.level() instanceof ServerLevel level)||level.getServer()!=server){it.remove();continue;}
            if(!given(item,tossed.from,tossed.origin))continue;
            it.remove();let(level,item,tossed.origin);
        }
    }
    /** Whether the well has it: over a rail, still falling, CLEAR blocks below the hand. */
    public static boolean given(ItemEntity item,double from,BlockPos origin){
        return !item.onGround()&&item.getY()<=from-CLEAR&&inWell(origin,item.position());
    }
    /**
     * The well has it. Its fall is told to every reader in the well, and the thing itself is out of the world until the fall is
     * over; then it lies where it came down ({@link #settle}). A fall that cannot be followed to a landing is left alone: the
     * thing goes on falling as any dropped thing does, and this answers null.
     */
    public static Fall let(ServerLevel level,ItemEntity item,BlockPos origin){
        var stack=item.getItem().copy();var fall=fall(level,item.position(),item.getDeltaMovement(),tint(stack));
        if(!fall.hit())return null;
        var sound=stack.getItem() instanceof BlockItem block?block.getBlock().defaultBlockState().getSoundType().getFallSound():null;
        item.discard();
        LANDINGS.add(new Landing(level,level.getGameTime()+fall.ticks(),fall.end(),sound,stack));
        var payload=new StairLightFallPayload(fall.start(),fall.speed(),fall.ticks(),fall.end(),fall.hit(),fall.tint());
        for(var reader:audience(level,origin))HousePackets.send(reader,payload);
        return fall;
    }
    /** The fall is over: the thing lies where it came down, and whoever is near hears it land. */
    private static void settle(Landing landing,boolean heard){
        var at=landing.at;var back=new ItemEntity(landing.level,at.x,at.y,at.z,landing.stack);back.setDeltaMovement(Vec3.ZERO);back.setDefaultPickUpDelay();
        landing.level.addFreshEntity(back);
        // Loud enough to carry HEARD blocks and no further.
        if(heard&&landing.sound!=null)landing.level.playSound(null,at.x,at.y,at.z,landing.sound,SoundSource.BLOCKS,(float)(HEARD/16),.85F);
    }
    /** Everyone in the well, at any depth: a light let fall above goes past those below. */
    public static List<ServerPlayer> audience(ServerLevel level,BlockPos origin){
        return level.players().stream().filter(p->inWell(origin,p.position())).toList();
    }
}
