package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.333, State Route 9 (docs/STATE_ROUTE_0_4_333.md), after the owner's design "State Route 9 (the-median)": a four-lane divided
 * highway at night, a key in a mailbox on the far side, and a padlocked gate on this one. One road and one traffic for everyone on it
 * (StateRouteScene); what each reader did is their own record.
 *
 * <p>Beat 1: the traffic is judged by ear and by the shine on the tar. A pickup chugs and throws its lights short; a semi hisses on the
 * wet tar three seconds before it is there, its white streak sliding down the lane ahead of it, and blows its horn going by. A semi
 * that misses still pulls at a reader near its lane. Struck squarely, the screen goes black to a screech and the reader comes to on
 * the starting shoulder (or, on an impact that would have killed them, back in the House). Beat 2: whoever first drops into the median
 * brings a convoy down both sides at once; crouched in the weeds the wind cannot touch them. Beat 3: the key out of the mailbox, every
 * vehicle drops to a crawl, bumper to bumper, every car empty but one wood-panelled wagon full of people who turn to watch; back on the
 * starting shoulder the key goes into the padlock, every horn on the highway sounds at once, and the lights go out.
 *
 * <p>Together: one road for the party. Whoever opens the mailbox brings the crawl for everyone on the road. A reader who stays on the
 * starting shoulder can lean on the fence and call the gaps aloud, or hold the gate as far as its chain goes; everyone on the road when
 * the padlock opens gets their own account (crossed for the key, crossed with someone, or watched from the fence).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StateRoute {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.STATE_ROUTE;
    public static final double PICKUP_SPEED=.35,CAR_SPEED=.9,SEMI_SPEED=1.6,CRAWL_SPEED=.2;
    /** How long before a semi's nose reaches the middle of the road its tires are heard and its shine slides down the lane. */
    public static final int WARN=60;
    /** The convoy: how long after a reader drops into the median it reaches them, and how long it runs. */
    public static final int CONVOY_DELAY=40,CONVOY_LENGTH=110;
    /** Struck squarely: a car's harm, a semi's; a pickup's knock. Never more than the reader has to lose, short of an impact that would kill. */
    public static final float CAR_HARM=8,SEMI_HARM=12,PICKUP_HARM=4,WAKE_HARM=2;
    /**
     * 0.4.336 (owner: "getting hit renders the player's corpse, injured and torn by the vehicles. It frames it for a few seconds then
     * spawns them in their bed"; "items should be dropped as per normal death, so the mother of strays can collect"): how long the
     * reader's own camera holds on what is left of them before they wake at home; how long the body stays on the tar for everyone else;
     * how long they lie in their bed before they wake.
     */
    public static final int FRAME=90,WRECK_LIFE=1200,ABED=50;
    /** The road's own way of killing: a vehicle at speed, through armour and everything else. */
    public static final net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> VEHICLE=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"route_vehicle"));
    /** Readers killed on the road and not yet woken at home, and readers lying in their own bed until they wake. */
    private static final Map<UUID,Long> WRECKED=new HashMap<>(),ABED_UNTIL=new HashMap<>();
    public static final ResourceLocation RUNAWAY_SPEED=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"route_runaway");
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** A vehicle not yet out of the hedge: its lane, kind, speed and the tick it comes out. Server only. */
    record Plan(int lane,int kind,double speed,long at){}
    private static final List<Plan> PLANS=new ArrayList<>();
    private static final Map<UUID,Long> HIT=new HashMap<>();
    private static final Map<UUID,Set<Integer>> WOKEN=new HashMap<>();
    private static final Set<Integer> HORNED=new HashSet<>();
    private static final Map<UUID,Long> CALLED=new HashMap<>();
    private static final Set<UUID> RUNNING=new HashSet<>();
    private static final Set<UUID> REFLECTOR=new HashSet<>();
    private static long medianEmptySince=-1,wagonNext=-1,flickerAt=-1;
    private StateRoute(){}
    public static boolean wrecked(UUID reader){return WRECKED.containsKey(reader);}
    public static void clearAll(){WRECKED.clear();ABED_UNTIL.clear();PLANS.clear();HIT.clear();WOKEN.clear();HORNED.clear();CALLED.clear();RUNNING.clear();medianEmptySince=-1;wagonNext=-1;flickerAt=-1;}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();REFLECTOR.clear();}

    // ------------------------------------------------------------------ finding the road
    public static @Nullable BlockPos base(MinecraftServer s){var o=HouseSavedData.get(s).houseOrigin();return o==null?null:LabyrinthPlaces.base(o,PLACE);}
    public static @Nullable StateRouteScene scene(MinecraftServer s){
        var b=base(s);var l=s.getLevel(NovelRooms.dimension(PLACE));if(b==null||l==null||!l.isLoaded(b.offset(StateRouteSite.ANCHOR)))return null;
        return l.getBlockEntity(b.offset(StateRouteSite.ANCHOR)) instanceof StateRouteScene sc?sc:null;
    }
    static boolean onRoad(ServerPlayer p){return LiteraryVignettes.current(p)==PLACE;}
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static List<ServerPlayer> readers(StateRouteScene s){var out=new ArrayList<ServerPlayer>();if(!(s.getLevel() instanceof ServerLevel l))return out;for(var p:l.players())if(onRoad(p)&&LiteraryVignettes.participant(p))out.add(p);return out;}
    static void say(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(net.minecraft.ChatFormatting.GRAY),true);}
    static void sound(StateRouteScene s,Vec3 at,SoundEvent e,float volume,float pitch){if(s.getLevel()!=null)s.getLevel().playSound(null,at.x,at.y,at.z,e,SoundSource.AMBIENT,volume,pitch);}
    static Vec3 at(StateRouteScene s,double x,double y,double z){return Vec3.atLowerCornerOf(s.base()).add(x,y,z);}
    static Vec3 rel(StateRouteScene s,ServerPlayer p){return p.position().subtract(Vec3.atLowerCornerOf(s.base()));}
    static Vec3 at(StateRouteScene s,StateRouteScene.Vehicle v){return at(s,v.x,1,StateRouteSite.LANE_Z[v.lane]);}
    /** Dealt once each, and only to a reader who has crossed the House's yard: the road the joists hall hears overhead. */
    public static boolean dealable(LabyrinthData d,UUID p){
        if(LiteraryVignettes.personal(d,p,PLACE).getBoolean("Completed"))return false;
        var seen=d.visited(p);for(var y:YardCrossings.ORDINARY)if(seen.contains(y.id()))return true;return false;
    }
    /** A reader carrying the reflector off the road: roads and open air come up for them a little more often. */
    public static boolean carriesReflector(UUID p){return REFLECTOR.contains(p);}

    // ------------------------------------------------------------------ arriving
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        own.putBoolean("RouteHere",true);own.putInt("RouteVisit",own.getInt("RouteVisit")+1);
        var age=HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.AGE,false);if(!age.isEmpty())own.putString("RouteAge",age);
        var s=scene(p.server);if(s!=null){s.party.add(p.getUUID());s.partySeen.put(p.getUUID(),p.serverLevel().getGameTime());s.setChanged();}
        say(p,s!=null&&s.mode==StateRouteScene.DARK?"The road is dark and still. The gate stands open.":"Across four lanes, under the one streetlight, a mailbox with its flag up. The gate beside you is padlocked.");
    }

    // ------------------------------------------------------------------ the road's clock
    public static void tick(StateRouteScene s){
        if(!(s.getLevel() instanceof ServerLevel l))return;long now=l.getGameTime();
        var here=readers(s);
        if(now%20==0&&s.wrecks.removeIf(w->now-w.at()>WRECK_LIFE||now<w.at()))s.sync();
        if(now%20==0){prune(s,l);if(here.isEmpty()&&s.party.isEmpty()){if(++s.emptyTicks>=5&&(s.mode!=StateRouteScene.FAST||s.keyOut||!s.traffic.isEmpty()))reset(s,l);}else s.emptyTicks=0;}
        if(here.isEmpty())return;
        boolean changed=false;
        if(s.mode==StateRouteScene.FAST)changed|=plan(s,l,now);
        else if(s.mode==StateRouteScene.CRAWL)changed|=crawl(s,l,now);
        changed|=spawn(s,l,now);
        move(s,l,now);
        if(s.mode!=StateRouteScene.DARK){for(var p:here)touch(s,l,p,now);cues(s,l,now,here);}
        convoy(s,l,now,here);
        lamp(s,l,now);
        if(changed||now%10==0)s.sync();else s.setChanged();
    }
    private static void prune(StateRouteScene s,ServerLevel l){
        long now=l.getGameTime();
        for(var id:List.copyOf(s.party)){
            var p=l.getServer().getPlayerList().getPlayer(id);
            if(p!=null&&onRoad(p)){s.partySeen.put(id,now);continue;}
            long seen=s.partySeen.getOrDefault(id,now);
            if(p!=null&&now-seen>200||p==null&&now-seen>2400){s.party.remove(id);s.partySeen.remove(id);if(id.equals(s.keyHolder))keyBack(s,l);}
        }
    }

    // ------------------------------------------------------------------ the traffic at speed
    private static final int[][] MIX={{40,50,10},{15,50,35},{10,50,40},{35,50,15}};
    private static int kind(StateRouteScene s,int lane,Random r){
        int[] w=MIX[lane];int roll=r.nextInt(w[0]+w[1]+w[2]);int k=roll<w[0]?StateRouteScene.PICKUP:roll<w[0]+w[1]?StateRouteScene.CAR:StateRouteScene.SEMI;
        // A semi only where it will not run up the back of something slow before the middle of the road.
        if(k==StateRouteScene.SEMI)for(var v:s.traffic)if(v.lane==lane&&v.speed<.8&&Math.abs(v.x-(-v.dir()*StateRouteSite.SPAWN))<70)return StateRouteScene.CAR;
        return k;
    }
    static double speed(int kind,Random r){return switch(kind){case StateRouteScene.PICKUP->PICKUP_SPEED+(r.nextDouble()-.5)*.06;case StateRouteScene.SEMI->SEMI_SPEED+(r.nextDouble()-.5)*.2;case StateRouteScene.WAGON->CRAWL_SPEED;default->CAR_SPEED+(r.nextDouble()-.5)*.1;};}
    /** The tick a vehicle out of the hedge at this tick reaches the middle of the road. */
    static long arrives(long at,double speed){return at+(long)Math.ceil(StateRouteSite.SPAWN/speed);}
    /** Decide each lane's next vehicle half a second before it comes out of the hedge, so a semi is heard before it is seen. */
    private static boolean plan(StateRouteScene s,ServerLevel l,long now){
        var r=new Random(now*31+s.nextId);boolean changed=false;
        for(int lane=0;lane<4;lane++){
            if(s.nextSpawn[lane]==0)s.nextSpawn[lane]=now+20+lane*23+r.nextInt(40);
            final int ln=lane;if(PLANS.stream().anyMatch(p->p.lane()==ln))continue;
            if(now<s.nextSpawn[lane]-30)continue;
            int k=kind(s,lane,r);double v=speed(k,r);long at=s.nextSpawn[lane];
            PLANS.add(new Plan(lane,k,v,at));
            if(k==StateRouteScene.SEMI){s.coming.add(new StateRouteScene.Coming(lane,arrives(at,v)));changed=true;}
            s.nextSpawn[lane]=at+55+r.nextInt(85)+(k==StateRouteScene.SEMI?30:0)+(k==StateRouteScene.PICKUP?40:0);
        }
        return changed;
    }
    private static boolean spawn(StateRouteScene s,ServerLevel l,long now){
        boolean changed=false;
        for(var it=PLANS.iterator();it.hasNext();){var p=it.next();if(now<p.at())continue;it.remove();
            var v=new StateRouteScene.Vehicle(s.nextId++,p.kind(),p.lane(),-StateRouteSite.dir(p.lane())*StateRouteSite.SPAWN,p.speed(),(int)Math.floorMod(now*7+p.lane(),6));
            // Never out of the hedge into the back of the one in front.
            boolean clear=true;for(var o:s.traffic)if(o.lane==v.lane&&Math.abs(o.x-v.x)<(o.length()+v.length())/2+2)clear=false;
            if(clear){s.traffic.add(v);changed=true;}
        }
        s.coming.removeIf(c->now>c.arrives()+20);
        return changed;
    }
    /** Everything runs at its own speed, but never closer than a car's length and a bit to the one in front. */
    private static void move(StateRouteScene s,ServerLevel l,long now){
        if(s.mode==StateRouteScene.DARK)return;
        s.traffic.sort(Comparator.comparingDouble(v->-v.dir()*v.x));
        var front=new HashMap<Integer,StateRouteScene.Vehicle>();
        for(var v:s.traffic){
            if(s.mode==StateRouteScene.CRAWL&&v.speed>CRAWL_SPEED)v.speed=Math.max(CRAWL_SPEED,v.speed*.85);
            double nx=v.x+v.dir()*v.speed;var ahead=front.get(v.lane);
            if(ahead!=null){double limit=ahead.x-v.dir()*((ahead.length()+v.length())/2+(s.mode==StateRouteScene.CRAWL?2:1.5));if(v.dir()>0?nx>limit:nx<limit){nx=v.dir()>0?Math.max(v.x,limit):Math.min(v.x,limit);}}
            v.x=nx;front.put(v.lane,v);
        }
        s.traffic.removeIf(v->v.dir()*v.x>StateRouteSite.SPAWN+v.length());
    }

    // ------------------------------------------------------------------ what the road sounds like
    private static void cues(StateRouteScene s,ServerLevel l,long now,List<ServerPlayer> here){
        for(var c:s.coming)if(now==c.arrives()-WARN){
            // The tires first, from far down the lane the semi is coming up.
            int dir=StateRouteSite.dir(c.lane());sound(s,at(s,-dir*36,1,StateRouteSite.LANE_Z[c.lane()]),StateRouteRegistry.HISS.get(),2.4F,1F);
        }
        for(var v:s.traffic){
            if(s.mode==StateRouteScene.FAST){
                if(v.kind==StateRouteScene.PICKUP&&(now+v.id*7)%30==0)sound(s,at(s,v),StateRouteRegistry.DIESEL.get(),1.4F,.95F+(v.id%5)*.02F);
                double prev=v.x-v.dir()*v.speed;
                if(v.kind==StateRouteScene.SEMI&&crossed(prev,v.x,-v.dir()*14)&&HORNED.add(v.id))sound(s,at(s,v),StateRouteRegistry.HORN.get(),3F,1F);
                if(v.kind==StateRouteScene.CAR&&crossed(prev,v.x,-v.dir()*6))sound(s,at(s,v),StateRouteRegistry.PASS.get(),1.6F,.9F+(v.id%7)*.03F);
            }
            else if(s.mode==StateRouteScene.CRAWL&&(now+v.id*13)%80==0&&Math.abs(v.x)<30)sound(s,at(s,v),StateRouteRegistry.TICK.get(),.5F,.9F+(v.id%5)*.04F);
        }
        if(HORNED.size()>200)HORNED.clear();
    }
    private static boolean crossed(double a,double b,double line){return (a-line)*(b-line)<=0&&a!=b;}

    // ------------------------------------------------------------------ bumpers, the wind off a trailer
    static AABB box(StateRouteScene s,StateRouteScene.Vehicle v,double grow){
        double half=v.length()/2,z=StateRouteSite.LANE_Z[v.lane];var b=s.base();
        return new AABB(b.getX()+v.x-half-grow,b.getY()-.5,b.getZ()+z-1.2-grow,b.getX()+v.x+half+grow,b.getY()+StateRouteSite.height(v.kind),b.getZ()+z+1.2+grow);
    }
    private static void touch(StateRouteScene s,ServerLevel l,ServerPlayer p,long now){
        if(LabyrinthDoors.isBusy(p)||p.isSpectator()||p.isCreative()||p.isDeadOrDying())return;
        var r=rel(s,p);
        for(var v:List.copyOf(s.traffic)){
            if(box(s,v,.05).intersects(p.getBoundingBox())){
                if(s.mode==StateRouteScene.CRAWL){push(p,v);continue;}
                if(now-HIT.getOrDefault(p.getUUID(),-100L)<20)return;HIT.put(p.getUUID(),now);
                if(v.kind==StateRouteScene.PICKUP)knock(s,p,v);else struck(s,l,p,v);
                return;
            }
            if(s.mode==StateRouteScene.FAST&&v.kind==StateRouteScene.SEMI)wake(s,p,v,r);
        }
    }
    /** Bumper to bumper at a crawl the cars only nudge a body along. */
    private static void push(ServerPlayer p,StateRouteScene.Vehicle v){p.push(v.dir()*.12,0,0);p.hurtMarked=true;}
    private static float harm(ServerPlayer p,float want){return Math.max(0,Math.min(want,p.getHealth()-1));}
    /** The road's harm lands even through the moment of grace after joining; it never takes the last heart. */
    private static void hit(ServerPlayer p,float amount){if(amount<=0)return;float before=p.getHealth();if(!p.hurt(p.damageSources().flyIntoWall(),amount)||p.getHealth()>=before)p.setHealth(Math.max(1,before-amount));}
    static void knock(StateRouteScene s,ServerPlayer p,StateRouteScene.Vehicle v){
        hit(p,harm(p,PICKUP_HARM));
        double z=StateRouteSite.LANE_Z[v.lane];double out=rel(s,p).z>z?1:-1;
        p.setDeltaMovement(v.dir()*.8,.35,out*.45);p.hurtMarked=true;
        sound(s,p.position(),StateRouteRegistry.SCREECH.get(),.9F,1.3F);
        var o=own(p);o.putInt("RoutePickup",o.getInt("RoutePickup")+1);save(p,o);
        say(p,"The pickup clips you and puts you down on the tar.");
    }
    /**
     * Struck squarely by a car or a semi: dead on the tar. The body is carried a little way along the lane and left at its edge, twisted
     * and torn, and stays there a minute for everyone on the road to see. The reader's own camera holds on it for a few seconds; then
     * they wake in their own bed (or wherever the world would put them). It is an ordinary death: whatever they carried falls where the
     * body lies, for the Mother of Strays as any death in the House, except the key, which goes back into the mailbox.
     */
    static void struck(StateRouteScene s,ServerLevel l,ServerPlayer p,StateRouteScene.Vehicle v){
        long now=l.getGameTime();var r=rel(s,p);boolean semi=v.kind==StateRouteScene.SEMI;
        sound(s,p.position(),StateRouteRegistry.SCREECH.get(),2F,1F);sound(s,p.position(),StateRouteRegistry.IMPACT.get(),1.8F,semi?.8F:1F);
        var o=own(p);o.putInt("RouteStruck",o.getInt("RouteStruck")+1);o.putString("RouteStruckBy",semi?"semi":"car");save(p,o);
        dropKey(s,l,p);s.party.remove(p.getUUID());s.partySeen.remove(p.getUUID());
        // Carried along the lane and thrown to its edge, further by a semi.
        double laneZ=StateRouteSite.LANE_Z[v.lane];double x=Mth.clamp(r.x+v.dir()*(semi?7:3.5),-StateRouteSite.HEDGE+2,StateRouteSite.HEDGE-2);
        double z=laneZ+(r.z>=laneZ?1.0:-1.0);float yaw=(v.dir()>0?-90:90)+(float)(l.random.nextGaussian()*25);
        var body=io.github.knaitoe.theoldesthouse.house.HouseConfig.houseBodies()?HouseBodies.body(LabyrinthData.get(l.getServer()),p.getUUID()):null;
        s.wrecks.removeIf(w->w.reader().equals(p.getUUID()));s.wrecks.add(new StateRouteScene.Wreck(p.getUUID(),x,z,yaw,now,v.kind,body));while(s.wrecks.size()>6)s.wrecks.remove(0);s.sync();
        // Everyone else on the road sees it.
        for(var q:readers(s)){if(q==p||q.distanceToSqr(p)>36*36)continue;var oq=own(q);oq.putString("RouteSawWreck",p.getGameProfile().getName());oq.putInt("RouteSawWrecks",oq.getInt("RouteSawWrecks")+1);save(q,oq);}
        // The reader's last sight of the road, then home.
        WRECKED.put(p.getUUID(),now);
        var at=at(s,x,0,z);
        HousePackets.send(p,new StateRouteWreckPayload(at.x,at.y,at.z,yaw,FRAME));
        p.stopRiding();p.teleportTo(l,at.x,at.y,at.z,yaw,0);p.setDeltaMovement(Vec3.ZERO);
        p.hurt(new net.minecraft.world.damagesource.DamageSource(l.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(VEHICLE)),Float.MAX_VALUE);
        if(!p.isDeadOrDying())WRECKED.remove(p.getUUID());
    }
    /** Woken at home: in their own bed, lying in it a moment first, or standing where the world put them. */
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.isEndConquered()||WRECKED.remove(p.getUUID())==null)return;
        var bed=p.getRespawnPosition();var l=p.serverLevel();
        if(bed!=null&&p.getRespawnDimension().equals(l.dimension())){var st=l.getBlockState(bed);
            if(st.getBlock() instanceof BedBlock){var head=st.getValue(BedBlock.PART)==net.minecraft.world.level.block.state.properties.BedPart.HEAD?bed:bed.relative(st.getValue(BedBlock.FACING));
                p.teleportTo(l,head.getX()+.5,head.getY()+.6,head.getZ()+.5,p.getYRot(),0);p.startSleeping(head);ABED_UNTIL.put(p.getUUID(),l.getGameTime()+ABED);
                var o=own(p);o.putInt("RouteAbed",o.getInt("RouteAbed")+1);save(p,o);
                say(p,"You wake in your own bed. Your clothes are wet through, and there is grit in your hair.");return;}}
        say(p,"You wake on cold ground, wet through, with grit in your hair.");
    }
    static{
        // A reader put to bed after the road stays lying there for their moment even by day; the event's package moved between NeoForge versions.
        for(String name:new String[]{"net.neoforged.neoforge.event.entity.living.CanContinueSleepingEvent","net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent"}){
            try{@SuppressWarnings("unchecked") var type=(Class<net.neoforged.bus.api.Event>)Class.forName(name);var allow=type.getMethod("setContinueSleeping",boolean.class);
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOW,false,type,event->{
                    if(event instanceof net.neoforged.neoforge.event.entity.living.LivingEvent living&&living.getEntity() instanceof ServerPlayer p&&ABED_UNTIL.containsKey(p.getUUID()))
                        try{allow.invoke(event,true);}catch(ReflectiveOperationException ignored){}});
                break;}catch(ReflectiveOperationException|ClassCastException ignored){}
        }
    }
    /** Up out of bed when their moment is over. */
    static void wakeAbed(MinecraftServer server){
        if(ABED_UNTIL.isEmpty())return;
        for(var it=ABED_UNTIL.entrySet().iterator();it.hasNext();){var e=it.next();var p=server.getPlayerList().getPlayer(e.getKey());
            if(p==null){it.remove();continue;}if(p.serverLevel().getGameTime()<e.getValue())continue;it.remove();if(p.isSleeping())p.stopSleepInBed(false,true);}
    }
    private static void wake(StateRouteScene s,ServerPlayer p,StateRouteScene.Vehicle v,Vec3 r){
        double z=StateRouteSite.LANE_Z[v.lane],dz=Math.abs(r.z-z);
        if(Math.abs(r.x-v.x)>v.length()/2+.5||dz<=1.45||dz>3.0||r.y<-1.6||r.y>2)return;
        var seen=WOKEN.computeIfAbsent(p.getUUID(),k->new HashSet<>());if(!seen.add(v.id))return;if(seen.size()>64)seen.clear();
        var o=own(p);boolean median=StateRouteSite.inMedian(r);
        // Down in the median's weeds the wind goes over them.
        if(median&&p.isCrouching()){o.putBoolean("RouteCrouched",true);save(p,o);return;}
        if(median)o.putBoolean("RouteStood",true);
        o.putInt("RouteWake",o.getInt("RouteWake")+1);save(p,o);
        hit(p,harm(p,WAKE_HARM));
        p.push(0,0,Math.signum(z-r.z)*.25);p.hurtMarked=true;
        LiteraryVignettes.heard(p,StateRouteRegistry.WAKE.get(),p.blockPosition(),1F,1F);
    }

    // ------------------------------------------------------------------ the median
    private static void convoy(StateRouteScene s,ServerLevel l,long now,List<ServerPlayer> here){
        boolean anyone=false;
        for(var p:here){var r=rel(s,p);if(!StateRouteSite.inMedian(r))continue;anyone=true;
            if(s.mode==StateRouteScene.FAST&&s.convoyAt<0&&!s.convoyDone){s.convoyAt=now+CONVOY_DELAY;callConvoy(s,now,r.x);
                for(var q:here)if(StateRouteSite.inMedian(rel(s,q)))say(q,"Headlights close in from both sides at once. Get down in the weeds.");}}
        if(anyone)medianEmptySince=-1;else if(medianEmptySince<0)medianEmptySince=now;
        if(s.convoyAt>=0&&now>s.convoyAt+CONVOY_LENGTH){s.convoyAt=-1;s.convoyDone=true;}
        // A second crossing gets its own convoy once the median has been empty a while.
        if(s.convoyDone&&medianEmptySince>=0&&now-medianEmptySince>400)s.convoyDone=false;
    }
    /** A semi down each lane beside the median timed to reach the reader together, then a solid run of cars; then a fair gap. */
    private static void callConvoy(StateRouteScene s,long now,double x){
        PLANS.removeIf(p->p.lane()==1||p.lane()==2);s.coming.removeIf(c->c.lane()==1||c.lane()==2);
        for(int lane:new int[]{1,2}){
            int dir=StateRouteSite.dir(lane);double run=dir*x+StateRouteSite.SPAWN;
            long first=Math.max(now,s.convoyAt+8-(long)Math.ceil(run/SEMI_SPEED));
            PLANS.add(new Plan(lane,StateRouteScene.SEMI,SEMI_SPEED,first));s.coming.add(new StateRouteScene.Coming(lane,arrives(first,SEMI_SPEED)));
            for(int i=0;i<6;i++)PLANS.add(new Plan(lane,i==3?StateRouteScene.SEMI:StateRouteScene.CAR,i==3?SEMI_SPEED:CAR_SPEED,first+14+i*13));
            s.nextSpawn[lane]=first+14+6*13+90;
        }
    }

    // ------------------------------------------------------------------ the crawl
    /** Bumper to bumper at ten miles an hour in all four lanes, a car's gap between bumpers; one wagon in the third lane. */
    private static boolean crawl(StateRouteScene s,ServerLevel l,long now){
        boolean changed=false;
        for(int lane=0;lane<4;lane++){
            int dir=StateRouteSite.dir(lane);double edge=-dir*StateRouteSite.SPAWN;
            StateRouteScene.Vehicle last=null;for(var v:s.traffic)if(v.lane==lane&&(last==null||dir*v.x<dir*last.x))last=v;
            boolean wagon=lane==2&&now>=wagonNext&&s.traffic.stream().noneMatch(v->v.kind==StateRouteScene.WAGON);
            int kind=wagon?StateRouteScene.WAGON:StateRouteScene.CAR;double len=StateRouteSite.length(kind);
            if(last==null||dir*(last.rear()-edge)>=len/2+2){s.traffic.add(new StateRouteScene.Vehicle(s.nextId++,kind,lane,edge,CRAWL_SPEED,(int)Math.floorMod(now+lane*3,6)));changed=true;if(wagon)wagonNext=now+600;}
        }
        return changed;
    }
    /** Fill every lane at once, so the roar dies to a crawl across the whole road; the wagon placed to come by the middle soon. */
    static void startCrawl(StateRouteScene s,ServerLevel l){
        long now=l.getGameTime();s.mode=StateRouteScene.CRAWL;s.modeAt=now;PLANS.clear();s.coming.clear();s.convoyAt=-1;
        for(int lane=0;lane<4;lane++){int dir=StateRouteSite.dir(lane);
            for(double x=-dir*StateRouteSite.SPAWN;dir*x<StateRouteSite.SPAWN;x+=dir*(StateRouteSite.length(StateRouteScene.CAR)+2)){
                final double fx=x;final int ln=lane;boolean wagon=lane==2&&Math.abs(x-14)<3.3;
                if(s.traffic.stream().anyMatch(v->v.lane==ln&&Math.abs(v.x-fx)<(v.length()+4.6)/2+2))continue;
                if(Math.abs(x)<1.5&&lane!=2)continue;
                s.traffic.add(new StateRouteScene.Vehicle(s.nextId++,wagon?StateRouteScene.WAGON:StateRouteScene.CAR,lane,x,CRAWL_SPEED,(int)Math.floorMod((long)x+lane,6)));
            }}
        if(s.traffic.stream().noneMatch(v->v.kind==StateRouteScene.WAGON)){s.traffic.removeIf(v->v.lane==2&&Math.abs(v.x-14)<6);s.traffic.add(new StateRouteScene.Vehicle(s.nextId++,StateRouteScene.WAGON,2,14,CRAWL_SPEED,0));}
        wagonNext=now+600;
        for(var p:readers(s)){var o=own(p);o.putBoolean("RouteCrawl",true);save(p,o);say(p,"The roar dies. Every engine on the road drops to a crawl, bumper to bumper, ticking in the rain.");}
        s.sync();
    }
    public static @Nullable StateRouteScene.Vehicle wagon(StateRouteScene s){for(var v:s.traffic)if(v.kind==StateRouteScene.WAGON)return v;return null;}

    // ------------------------------------------------------------------ the mailbox, the gate, the fence
    /** A hand on the mailbox: the flag comes down, the key comes out with its note, and the road slows for everyone on it. */
    public static boolean mailbox(ServerPlayer p,BlockPos b,CompoundTag own,BlockPos pos){
        var s=scene(p.server);if(s==null)return false;var l=(ServerLevel)s.getLevel();
        if(s.mode==StateRouteScene.FAST&&!s.keyOut){
            var at=b.offset(StateRouteSite.MAILBOX);var st=l.getBlockState(at);if(StateRouteBlock.is(st,StateRouteBlock.Kind.MAILBOX))l.setBlock(at,st.setValue(StateRouteBlock.STAGE,1),F);
            sound(s,Vec3.atCenterOf(at),SoundEvents.IRON_TRAPDOOR_OPEN,.7F,1.4F);
            s.keyOut=true;s.keyHolder=p.getUUID();LiteraryVignettes.give(p,new ItemStack(StateRouteRegistry.KEY.get()));
            own.putBoolean("RouteKey",true);own.putBoolean("RouteCrossed",true);
            if(HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.HOME,true).equals("RUNAWAY")&&!own.getBoolean("RoutePostcard")){own.putBoolean("RoutePostcard",true);LiteraryVignettes.give(p,StateRouteTexts.postcard());}
            save(p,own);startCrawl(s,l);
        }
        LiteraryVignettes.open(p,PLACE,"Source",StateRouteTexts.source(),pos);
        return true;
    }
    /** The gate: with the key, the padlock; without it, the chain gives a hand's width. */
    public static boolean gate(ServerPlayer p,BlockPos b,CompoundTag own,ItemStack held){
        var s=scene(p.server);if(s==null)return false;var l=(ServerLevel)s.getLevel();
        if(s.mode==StateRouteScene.DARK){say(p,"The padlock hangs open on its chain.");return true;}
        if(held.is(StateRouteRegistry.KEY.get())){release(s,l,p);return true;}
        sound(s,Vec3.atCenterOf(b.offset(StateRouteSite.GATE)),StateRouteRegistry.PADLOCK.get(),.8F,1F);
        if(!own.getBoolean("RouteHeld")&&!own.getBoolean("RouteCrossed")){own.putBoolean("RouteHeld",true);save(p,own);}
        say(p,s.keyOut?"The chain gives a hand's width and no more. The key is coming.":"The chain gives a hand's width and no more. The padlock is heavy and cold.");
        return true;
    }
    /**
     * Leaning on the fence, a reader on the starting shoulder calls the gap for whoever is out on the road, aloud and over their head
     * like anything else said: what is coming in the two lanes ahead of the runner, judged from the road as it really is.
     */
    public static boolean call(ServerPlayer p){
        var s=scene(p.server);if(s==null||s.mode!=StateRouteScene.FAST)return false;var l=(ServerLevel)s.getLevel();long now=l.getGameTime();
        if(!StateRouteSite.onStart(rel(s,p))&&!StateRouteSite.behindFence(rel(s,p)))return false;
        ServerPlayer runner=null;for(var q:readers(s))if(q!=p&&StateRouteSite.onRoad(rel(s,q))&&!StateRouteSite.onStart(rel(s,q))){runner=q;break;}
        if(runner==null){say(p,"There's nobody out on the road to call to.");return true;}
        if(now-CALLED.getOrDefault(p.getUUID(),-100L)<30)return true;CALLED.put(p.getUUID(),now);
        Spoken.say(p,gapCall(s,rel(s,runner),now));
        var o=own(p);o.putInt("RouteCalls",o.getInt("RouteCalls")+1);save(p,o);
        return true;
    }
    /** What a watcher sees coming at the runner in the two lanes they have still to cross on this half. */
    public static String gapCall(StateRouteScene s,Vec3 r,long now){
        boolean near=r.z>StateRouteSite.MEDIAN_NEAR-.01;boolean going=!s.keyOut;
        int[] lanes=going?(near?new int[]{0,1}:new int[]{2,3}):(r.z<StateRouteSite.MEDIAN_FAR?new int[]{2,3}:new int[]{0,1});
        String worst=null;
        for(var v:s.traffic){if(v.lane!=lanes[0]&&v.lane!=lanes[1])continue;double gap=(r.x-v.front())*v.dir();if(gap<-v.length()-1)continue;double t=gap/Math.max(.05,v.speed);
            if(t<45){if(v.kind==StateRouteScene.SEMI)return "Wait! Truck!";worst=v.kind==StateRouteScene.PICKUP?"Wait, there's a pickup, let it by.":"Wait—car!";}}
        if(worst!=null)return worst;
        for(var c:s.coming)if((c.lane()==lanes[0]||c.lane()==lanes[1])&&c.arrives()-now<WARN+20&&c.arrives()>now-10)return "Hold on, I can hear a truck coming.";
        return "Now! Go, go!";
    }
    /** The key in the padlock: every horn on the highway at once, then every light out. Everyone on the road gets their own account. */
    static void release(StateRouteScene s,ServerLevel l,ServerPlayer runner){
        long now=l.getGameTime();var b=s.base();
        for(var v:s.traffic)if(Math.abs(v.x)<40&&v.id%3==0)sound(s,at(s,v),StateRouteRegistry.CHORD.get(),2.5F,1F);
        sound(s,Vec3.atCenterOf(b.offset(StateRouteSite.GATE)),StateRouteRegistry.CHORD.get(),3F,1F);
        sound(s,Vec3.atCenterOf(b.offset(StateRouteSite.GATE)),StateRouteRegistry.PADLOCK.get(),1F,.8F);
        s.mode=StateRouteScene.DARK;s.modeAt=now;s.releasedAt=now;s.releasedBy=runner.getUUID();PLANS.clear();s.coming.clear();
        for(var st:runner.getInventory().items)if(st.is(StateRouteRegistry.KEY.get()))st.setCount(0);
        for(var st:runner.getInventory().offhand)if(st.is(StateRouteRegistry.KEY.get()))st.setCount(0);
        s.keyHolder=null;
        // The gate stands open; the lamp over the mailbox goes out with the rest.
        for(var at:new BlockPos[]{b.offset(StateRouteSite.GATE),b.offset(StateRouteSite.GATE).above()}){var st=l.getBlockState(at);if(st.getBlock() instanceof DoorBlock)l.setBlock(at,st.setValue(DoorBlock.OPEN,true),F);}
        var lamp=b.offset(StateRouteSite.LAMP);if(StateRouteBlock.is(l.getBlockState(lamp),StateRouteBlock.Kind.LAMP))l.setBlock(lamp,l.getBlockState(lamp).setValue(StateRouteBlock.STAGE,1),F);
        String name=runner.getGameProfile().getName();var d=LabyrinthData.get(l.getServer());
        for(var p:readers(s)){
            var o=own(p);if(o.getBoolean("Completed"))continue;
            boolean me=p==runner;String role=me?"runner":o.getBoolean("RouteCrossed")?"crossed":"watcher";
            String outcome=me?"carried_the_key_back_across_the_9":role.equals("crossed")?"crossed_the_9_with_them":"watched_from_the_fence";
            o.putString("RouteRole",role);o.putString("RouteRunner",me?"":name);o.putBoolean("RouteReleased",me);
            o.putBoolean("Ready",true);o.putString("Outcome",outcome);o.putBoolean("Completed",true);o.putBoolean("Taken_Ending",true);o.putInt("Beat",o.getInt("Visit"));
            LiteraryVignettes.save(d,p.getUUID(),PLACE,o);
            if(me)WitnessAccount.resolve(p,WitnessAccount.Story.STATE_ROUTE,outcome);
            else WitnessAccount.resolveAs(p,WitnessAccount.Story.STATE_ROUTE,outcome,role.equals("crossed")?VignetteRole.ACTOR:VignetteRole.WITNESS,name);
            d.setCompleted(PLACE.id(),true);
            LiteraryVignettes.reward(p,PLACE,o,"Ending",StateRouteTexts.account(p.getGameProfile().getName(),o));
            LiteraryVignettes.reward(p,PLACE,o,"Reflector",new ItemStack(StateRouteRegistry.REFLECTOR.get()));
            say(p,me?"Every horn on the highway sounds at once. Then the lights go out.":name+" turns the key. Every horn on the highway sounds at once. Then the lights go out.");
        }
        s.sync();
    }
    /** The key goes back into the mailbox (whoever had it left the road or was struck), and the road picks up speed again. */
    static void keyBack(StateRouteScene s,ServerLevel l){
        if(s.mode==StateRouteScene.DARK)return;
        s.keyOut=false;s.keyHolder=null;var at=s.base().offset(StateRouteSite.MAILBOX);var st=l.getBlockState(at);if(StateRouteBlock.is(st,StateRouteBlock.Kind.MAILBOX))l.setBlock(at,st.setValue(StateRouteBlock.STAGE,0),F);
        if(s.mode==StateRouteScene.CRAWL){s.mode=StateRouteScene.FAST;s.modeAt=l.getGameTime();s.traffic.clear();PLANS.clear();Arrays.fill(s.nextSpawn,0);
            for(var p:readers(s))say(p,"Down the road an engine opens up again, then another. The flag on the mailbox is up.");}
        s.sync();
    }
    private static void dropKey(StateRouteScene s,ServerLevel l,ServerPlayer p){
        boolean had=false;for(var st:p.getInventory().items)if(st.is(StateRouteRegistry.KEY.get())){st.setCount(0);had=true;}
        for(var st:p.getInventory().offhand)if(st.is(StateRouteRegistry.KEY.get())){st.setCount(0);had=true;}
        if(had||p.getUUID().equals(s.keyHolder))keyBack(s,l);
    }

    // ------------------------------------------------------------------ the lamp
    private static void lamp(StateRouteScene s,ServerLevel l,long now){
        if(s.mode==StateRouteScene.DARK)return;var at=s.base().offset(StateRouteSite.LAMP);var st=l.getBlockState(at);if(!StateRouteBlock.is(st,StateRouteBlock.Kind.LAMP))return;
        if(flickerAt<0)flickerAt=now+160+l.getRandom().nextInt(200);
        if(now==flickerAt)l.setBlock(at,st.setValue(StateRouteBlock.STAGE,1),F);
        else if(now==flickerAt+3){l.setBlock(at,st.setValue(StateRouteBlock.STAGE,0),F);flickerAt=-1;}
    }

    // ------------------------------------------------------------------ what each reader did
    /** Every five ticks, for each reader on the road. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var s=scene(p.server);if(s==null||own.getBoolean("Completed"))return;
        if(!s.party.contains(p.getUUID())){s.party.add(p.getUUID());s.partySeen.put(p.getUUID(),p.serverLevel().getGameTime());}
        var r=rel(s,p);
        if(StateRouteSite.inMedian(r))own.putBoolean("RouteMedian",true);
        if(StateRouteSite.onFar(r))own.putBoolean("RouteCrossed",true);
        if(s.mode==StateRouteScene.CRAWL){own.putBoolean("RouteCrawl",true);if(own.getBoolean("RouteCrossed")&&StateRouteSite.lane(r.z)>=0)own.putBoolean("RouteBetween",true);
            var w=wagon(s);if(w!=null){var c=at(s,w);var to=c.subtract(p.getEyePosition());double dist=to.length();
                if(dist<7&&p.getLookAngle().dot(to.scale(1/dist))>.6)own.putBoolean("RouteWagon",true);
                boolean danger=HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.HOME,true).equals("DANGER");
                boolean hidden=danger&&p.isCrouching()&&StateRouteSite.inMedian(r);
                if(hidden&&dist<12)own.putBoolean("RouteUnseen",true);
                if(hidden?s.unseen.add(p.getUUID()):s.unseen.remove(p.getUUID()))s.sync();}}
        else if(s.unseen.remove(p.getUUID()))s.sync();
        // The runaway's body already knows how to cross a state route in the dark.
        boolean run=p.isSprinting()&&StateRouteSite.lane(r.z)>=0&&s.mode!=StateRouteScene.DARK&&HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.HOME,true).equals("RUNAWAY");
        runaway(p,run);
        if(p.tickCount%100<5)signs(p,b,own);
    }
    static void runaway(ServerPlayer p,boolean on){
        var a=p.getAttribute(Attributes.MOVEMENT_SPEED);if(a==null)return;
        if(on&&!a.hasModifier(RUNAWAY_SPEED)){a.addTransientModifier(new AttributeModifier(RUNAWAY_SPEED,.1,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));RUNNING.add(p.getUUID());}
        else if(!on&&a.hasModifier(RUNAWAY_SPEED)){a.removeModifier(RUNAWAY_SPEED);RUNNING.remove(p.getUUID());}
    }
    /** The route shield and the mile marker the House read this reader for: the painted steel kind, or the reflective panel. */
    static void signs(ServerPlayer p,BlockPos b,CompoundTag own){
        int stage=own.getString("RouteAge").equals("DIGITAL")?1:0;
        for(var rel:new BlockPos[]{StateRouteSite.SHIELD,StateRouteSite.MILE}){var at=b.offset(rel);var st=p.serverLevel().getBlockState(at);if(st.is(StateRouteRegistry.FITTING.get()))p.connection.send(new ClientboundBlockUpdatePacket(at,st.setValue(StateRouteBlock.STAGE,stage)));}
    }

    // ------------------------------------------------------------------ off the road
    /** Back through the door the reader came in by. */
    public static void returned(ServerPlayer p,LabyrinthData.Door door){if(SceneInstances.plain(door.id).equals(PLACE.entryDoorId()))leave(p,"back");}
    /** Through the open gate (the way on). */
    public static void onward(ServerPlayer p,LabyrinthData.Door door){if(door.id.equals(OnwardDoors.id(PLACE)))leave(p,"gate");}
    public static void leave(ServerPlayer p,String how){
        var s=scene(p.server);runaway(p,false);
        if(s!=null){s.party.remove(p.getUUID());s.unseen.remove(p.getUUID());dropKey(s,(ServerLevel)s.getLevel(),p);s.setChanged();}
        var d=LabyrinthData.get(p.server);var o=LiteraryVignettes.personal(d,p.getUUID(),PLACE);
        if(o.getBoolean("Completed"))return;
        o.putString("RouteExit",how);LiteraryVignettes.save(d,p.getUUID(),PLACE,o);
    }

    // ------------------------------------------------------------------ the road set back
    /** Nobody left on the road: the next reader finds the traffic at speed, the key in the mailbox, the gate padlocked. */
    public static void reset(StateRouteScene s,ServerLevel l){
        var b=s.base();
        var mail=b.offset(StateRouteSite.MAILBOX);if(StateRouteBlock.is(l.getBlockState(mail),StateRouteBlock.Kind.MAILBOX))l.setBlock(mail,l.getBlockState(mail).setValue(StateRouteBlock.STAGE,0),F);
        var lamp=b.offset(StateRouteSite.LAMP);if(StateRouteBlock.is(l.getBlockState(lamp),StateRouteBlock.Kind.LAMP))l.setBlock(lamp,l.getBlockState(lamp).setValue(StateRouteBlock.STAGE,0),F);
        for(var at:new BlockPos[]{b.offset(StateRouteSite.GATE),b.offset(StateRouteSite.GATE).above()}){var st=l.getBlockState(at);if(st.getBlock() instanceof DoorBlock)l.setBlock(at,st.setValue(DoorBlock.OPEN,false),F);}
        var area=new AABB(b.getX()+StateRouteSite.WEST,b.getY()-4,b.getZ()+StateRouteSite.FAR,b.getX()+StateRouteSite.EAST,b.getY()+8,b.getZ()+2);
        for(var item:l.getEntitiesOfClass(ItemEntity.class,area,e->e.getItem().is(StateRouteRegistry.KEY.get())))item.discard();
        PLANS.clear();WOKEN.clear();HORNED.clear();medianEmptySince=-1;wagonNext=-1;
        s.mode=StateRouteScene.FAST;s.modeAt=l.getGameTime();s.traffic.clear();s.coming.clear();Arrays.fill(s.nextSpawn,0);s.keyOut=false;s.keyHolder=null;
        s.convoyAt=-1;s.convoyDone=false;s.releasedAt=-1;s.releasedBy=null;s.party.clear();s.partySeen.clear();s.unseen.clear();s.emptyTicks=0;s.wrecks.clear();
        s.sync();
    }

    // ------------------------------------------------------------------ cheating, companions, the keepsake
    /** A pearl thrown across the lanes is taken out of the air by a trailer's wind and drops in the median. */
    @SubscribeEvent public static void pearl(EntityTeleportEvent.EnderPearl e){
        var p=e.getPlayer();if(p==null||!onRoad(p))return;var s=scene(p.server);if(s==null)return;
        e.setCanceled(true);var b=s.base();
        var drop=new ItemEntity(p.serverLevel(),Mth.clamp(e.getTargetX(),b.getX()-40,b.getX()+40),b.getY()-.9,b.getZ()-12,new ItemStack(Items.ENDER_PEARL));drop.setDefaultPickUpDelay();p.serverLevel().addFreshEntity(drop);
        say(p,"The wind off a trailer takes the pearl out of the air. It drops in the median.");
    }
    @SubscribeEvent public static void serverTick(ServerTickEvent.Post e){
        var server=e.getServer();wakeAbed(server);if(server.getTickCount()%20!=0)return;
        // Who carries the reflector off the road.
        REFLECTOR.clear();for(var p:server.getPlayerList().getPlayers()){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(StateRouteRegistry.REFLECTOR.get())){REFLECTOR.add(p.getUUID());break;}}
        for(var id:List.copyOf(RUNNING)){var p=server.getPlayerList().getPlayer(id);if(p==null){RUNNING.remove(id);continue;}if(!onRoad(p))runaway(p,false);}
        // Companions wait behind the fence: they are put back in the lane among the hedges, never sent out into traffic. Their orders stand.
        var s=scene(server);if(s==null||!(s.getLevel() instanceof ServerLevel l))return;var b=s.base();
        var road=new AABB(b.getX()+StateRouteSite.WEST,b.getY()-3,b.getZ()+StateRouteSite.FAR,b.getX()+StateRouteSite.EAST,b.getY()+6,b.getZ()+StateRouteSite.FENCE_Z);
        for(var m:l.getEntitiesOfClass(Mob.class,road,m->m instanceof OwnableEntity o&&o.getOwnerUUID()!=null&&!m.isPassenger())){
            m.getNavigation().stop();m.teleportTo(b.getX()+.5,b.getY(),b.getZ()-1.2);m.setDeltaMovement(Vec3.ZERO);
        }
    }
    static boolean isGate(BlockPos rel){return rel.equals(StateRouteSite.GATE)||rel.equals(StateRouteSite.GATE.above());}
    static boolean isMailbox(BlockPos rel){return rel.equals(StateRouteSite.MAILBOX);}
    private static final class Mth{static double clamp(double v,double a,double b){return v<a?a:v>b?b:v;}}
}

