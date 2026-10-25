package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.HazardViewPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Appended hazards: saved shared occupied clocks, native mechanics, private reflections and owned attribute leases. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HazardVariety {
    public static final String ROOMS="hazard_rooms_0480",READERS="hazard_readers_0480";
    public static final ResourceLocation COLD=id("hazard_cold"),GRAVITY=id("hazard_gravity"),JUMP=id("hazard_jump"),FALL=id("hazard_fall"),SPEED=id("hazard_weight_speed"),SCALE=id("hazard_nested");
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final Map<MinecraftServer,Engine> ENGINES=new WeakHashMap<>();
    private record Sample(double x,double y,double z,float yaw,float pitch,boolean crouch,float stride){}
    private static final class Reader {
        final ServerPlayer player;LabyrinthPlace place;CompoundTag own;double lastZ;int frost,restore;double restoreFrom;
        final ArrayDeque<Sample> trail=new ArrayDeque<>();
        Reader(ServerPlayer p){player=p;lastZ=p.getZ();}
    }
    private static final class Engine {
        final MinecraftServer server;final LabyrinthData data;final BlockPos origin;
        final Map<LabyrinthPlace,CompoundTag> rooms=new EnumMap<>(LabyrinthPlace.class);
        final Map<UUID,Reader> readers=new HashMap<>();
        Engine(MinecraftServer s){server=s;data=LabyrinthData.get(s);origin=HouseSavedData.get(s).houseOrigin();}
        CompoundTag room(LabyrinthPlace p){return rooms.computeIfAbsent(p,k->data.stateEntry(ROOMS,key(LabyrinthPlaces.base(origin,k))));}
        void flush(){if(origin==null)return;rooms.forEach((p,t)->data.setStateEntry(ROOMS,key(LabyrinthPlaces.base(origin,p)),t));for(var r:readers.values())if(r.place!=null&&r.own!=null)data.setStateEntry(READERS,readerKey(r.player,r.place),r.own);}
    }
    private HazardVariety(){}
    private static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,s);}
    private static String key(BlockPos b){return Long.toString(b.asLong());}
    private static String readerKey(ServerPlayer p,LabyrinthPlace place){return p.getUUID()+":"+place.id();}
    private static Engine engine(MinecraftServer s){var e=ENGINES.get(s);var origin=HouseSavedData.get(s).houseOrigin();if(e==null||e.data!=LabyrinthData.get(s)||!Objects.equals(e.origin,origin)){if(e!=null)for(var r:e.readers.values())clear(r.player);e=new Engine(s);ENGINES.put(s,e);}return e;}
    public static boolean added(LabyrinthPlace p){return p!=null&&p.slot()>=78&&p.slot()<=93;}
    public static boolean newKind(LabyrinthPlace p){return added(p)&&p.slot()<=85;}
    public static boolean sibling(LabyrinthPlace p){return added(p)&&p.slot()>=86;}
    public static LabyrinthPlace original(LabyrinthPlace p){if(p==null)return null;return switch(p){
        case BENT_CISTERN->LabyrinthPlace.FLOODED_PASSAGE;case SPIRAL_WALKWAY->LabyrinthPlace.FRACTURED_WALKWAY;case CORNER_PRESSURE->LabyrinthPlace.COMPRESSION_PASSAGE;
        case RECEDING_HALL->LabyrinthPlace.FALSE_DISTANCE;case LAST_SCONCE->LabyrinthPlace.LIGHT_SINK;case WATCHED_THRESHOLD->LabyrinthPlace.MOVING_THRESHOLD;
        case DUPLICATE_ROOMS->LabyrinthPlace.DUPLICATE_PASSAGE;case TURNING_GRAVITY->LabyrinthPlace.GRAVITY_DRIFT;default->p;};}
    public static int tier(LabyrinthPlace p){return switch(p){
        case COLD_STORE,LISTING_HALL,QUIET_HALL,BENT_CISTERN,RECEDING_HALL->1;
        case UNWATCHED_MAZE,MIRROR_HALL,HEAVY_AND_LIGHT,SPIRAL_WALKWAY,LAST_SCONCE,WATCHED_THRESHOLD->2;
        case NESTED_ROOMS,COUNTING_STAIR,CORNER_PRESSURE,DUPLICATE_ROOMS,TURNING_GRAVITY->3;default->0;};}
    public static LabyrinthPlace routeVariant(LabyrinthPlace p,long route,LabyrinthData data){var a=switch(original(p)){
        case FLOODED_PASSAGE->LabyrinthPlace.BENT_CISTERN;case FRACTURED_WALKWAY->LabyrinthPlace.SPIRAL_WALKWAY;case COMPRESSION_PASSAGE->LabyrinthPlace.CORNER_PRESSURE;
        case FALSE_DISTANCE->LabyrinthPlace.RECEDING_HALL;case LIGHT_SINK->LabyrinthPlace.LAST_SCONCE;case MOVING_THRESHOLD->LabyrinthPlace.WATCHED_THRESHOLD;
        case DUPLICATE_PASSAGE->LabyrinthPlace.DUPLICATE_ROOMS;case GRAVITY_DRIFT->LabyrinthPlace.TURNING_GRAVITY;default->null;};
        long mixed=route^(route>>>33)^0x534556454eL;mixed*=0xff51afd7ed558ccdL;
        return a!=null&&(mixed&1)!=0&&LabyrinthBuilder.isPlaceReady(data,a)?a:original(p);}
    public static void fresh(ServerLevel l,BlockPos b,LabyrinthPlace p){var d=LabyrinthData.get(l.getServer());var t=new CompoundTag();t.putLong("Clock",0);t.putLong("Mask",HazardMaze.initialMask(b.asLong()));t.putInt("DoorZ",-34);t.putLong("Sconce",b.offset(-8,2,-7).asLong());d.setStateEntry(ROOMS,key(b),t);var e=ENGINES.get(l.getServer());if(e!=null)e.rooms.remove(p);}
    public static void onArrive(ServerPlayer p,LabyrinthPlace place){var e=engine(p.server);var old=e.readers.remove(p.getUUID());if(old!=null){leave(e,old);if(old.restore>0&&!added(place)&&p.isAlive()&&!p.isSpectator()){e.readers.put(p.getUUID(),old);return;}}clear(p);if(!added(place)||p.isSpectator()||!p.isAlive())return;
        var r=new Reader(p);r.place=place;r.own=new CompoundTag();r.own.putInt("MirrorDoor",Math.floorMod(p.getUUID().hashCode()^Long.hashCode(e.room(place).getLong("Clock")),3));e.readers.put(p.getUUID(),r);save(e,r);}
    private static void save(Engine e,Reader r){if(r.place!=null&&r.own!=null)e.data.setStateEntry(READERS,readerKey(r.player,r.place),r.own);}
    /** All live cameras count, including spectator cameras and a player spectating another entity. */
    public static boolean watched(ServerLevel l,BlockPos at){var target=at.getCenter();for(var p:l.players())if(p.isAlive()){
        Entity camera=p.getCamera();var eye=camera.getEyePosition();var d=target.subtract(eye);double length=d.length();if(length>48)continue;if(length<1.2)return true;
        if(camera.getLookAngle().dot(d.scale(1/length))<.5)continue;var hit=l.clip(new ClipContext(eye,target,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,camera));
        if(hit.getType()==HitResult.Type.MISS||hit.getBlockPos().equals(at)||hit.getLocation().distanceToSqr(target)<1)return true;
    }return false;}
    private static LabyrinthPlace location(ServerPlayer p,BlockPos origin){return origin!=null&&p.level().dimension().equals(HouseDimensions.INTERIOR)?LabyrinthPlaces.placeAt(origin,p.blockPosition()):null;}
    private static boolean loaded(ServerLevel l,BlockPos b,LabyrinthPlace p){var r=p.room();for(int x=(b.getX()+r.minX()-1)>>4;x<=(b.getX()+r.maxX()+1)>>4;x++)for(int z=(b.getZ()+r.minZ()-1)>>4;z<=(b.getZ()+17)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;return true;}
    @SubscribeEvent public static void post(ServerTickEvent.Post event){tick(event.getServer());}
    public static void tick(MinecraftServer s){var e=engine(s);if(e.origin==null)return;var l=s.getLevel(HouseDimensions.INTERIOR);if(l==null)return;var groups=new EnumMap<LabyrinthPlace,List<Reader>>(LabyrinthPlace.class);var present=new HashSet<UUID>();
        for(var p:s.getPlayerList().getPlayers()){
            var place=location(p,e.origin);var r=e.readers.get(p.getUUID());
            if(added(place)&&p.isAlive()&&!p.isSpectator()&&!p.isCreative()&&LabyrinthBuilder.isPlaceReady(e.data,place)){
                if(r==null||r.player!=p||r.place!=place){if(r!=null)leave(e,r);r=new Reader(p);r.place=place;r.own=e.data.stateEntry(READERS,readerKey(p,place));if(!r.own.contains("MirrorDoor"))r.own.putInt("MirrorDoor",Math.floorMod(p.getUUID().hashCode(),3));e.readers.put(p.getUUID(),r);}
                present.add(p.getUUID());groups.computeIfAbsent(place,k->new ArrayList<>()).add(r);
            }else if(r!=null){if(r.place!=null)leave(e,r);if(r.restore>0&&p.isAlive()){r.restore--;modifier(p,Attributes.SCALE,SCALE,r.restoreFrom*r.restore/60.);if(r.restore==0){remove(p,Attributes.SCALE,SCALE);e.readers.remove(p.getUUID());}}else{clear(p);e.readers.remove(p.getUUID());}}
        }
        for(var entry:groups.entrySet()){var place=entry.getKey();var b=LabyrinthPlaces.base(e.origin,place);if(b==null||!loaded(l,b,place))continue;var world=e.room(place);world.putLong("Clock",world.getLong("Clock")+1);long clock=world.getLong("Clock");var here=entry.getValue();
            switch(place){
                case COLD_STORE->cold(l,b,world,here);case LISTING_HALL->listing(l,b,clock,here);case QUIET_HALL->quiet(l,b,world,here);
                case UNWATCHED_MAZE->{if(clock%80==0)maze(l,b,world);}case MIRROR_HALL->mirror(b,clock,here);case HEAVY_AND_LIGHT->weight(b,here);
                case NESTED_ROOMS->nested(b,here);case COUNTING_STAIR->counting(b,clock,here);case BENT_CISTERN->{if(clock%40==0)here.forEach(r->r.player.causeFoodExhaustion(.16F));}
                case SPIRAL_WALKWAY->spiral(b,clock,here);case CORNER_PRESSURE->pressure(l,b,clock,here);case RECEDING_HALL->receding(l,b,world,here);
                case LAST_SCONCE->{if(clock%20==0)sconce(l,b,world);}case WATCHED_THRESHOLD->{if(clock%70==0)threshold(l,b,world);}
                case TURNING_GRAVITY->gravity(l,b,clock,here);default->{}
            }
            for(var r:here){r.lastZ=r.player.getZ();if(clock%20==0)save(e,r);}if(clock%20==0)e.data.setStateEntry(ROOMS,key(b),world);
        }
        // Disconnected sessions cannot retain cached bodies or attribute leases.
        for(var it=e.readers.entrySet().iterator();it.hasNext();){var r=it.next().getValue();if(s.getPlayerList().getPlayer(r.player.getUUID())!=r.player){save(e,r);clear(r.player);it.remove();}}
    }
    private static void leave(Engine e,Reader r){save(e,r);if(r.place==LabyrinthPlace.NESTED_ROOMS){var a=r.player.getAttribute(Attributes.SCALE);var m=a==null?null:a.getModifier(SCALE);if(m!=null){r.restoreFrom=m.amount();r.restore=60;}}
        clearExceptScale(r.player);if(r.restore==0)remove(r.player,Attributes.SCALE,SCALE);r.place=null;r.own=null;r.trail.clear();HousePackets.send(r.player,HazardViewPayload.clear());}
    private static void modifier(LivingEntity p,Holder<Attribute> attr,ResourceLocation id,double value){var a=p.getAttribute(attr);if(a==null)return;var old=a.getModifier(id);if(value==0){if(old!=null){a.removeModifier(id);if(attr.equals(Attributes.SCALE))p.refreshDimensions();}return;}if(old!=null&&Math.abs(old.amount()-value)<.00001)return;if(old!=null)a.removeModifier(id);a.addTransientModifier(new AttributeModifier(id,value,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));if(attr.equals(Attributes.SCALE))p.refreshDimensions();}
    private static void remove(LivingEntity p,Holder<Attribute> attr,ResourceLocation id){var a=p.getAttribute(attr);if(a!=null&&a.getModifier(id)!=null){a.removeModifier(id);if(attr.equals(Attributes.SCALE))p.refreshDimensions();}}
    private static void clearExceptScale(ServerPlayer p){if(p.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(COLD)!=null)p.setTicksFrozen(0);remove(p,Attributes.MOVEMENT_SPEED,COLD);remove(p,Attributes.MOVEMENT_SPEED,SPEED);remove(p,Attributes.GRAVITY,GRAVITY);remove(p,Attributes.JUMP_STRENGTH,JUMP);remove(p,Attributes.FALL_DAMAGE_MULTIPLIER,FALL);}
    public static void clear(ServerPlayer p){clearExceptScale(p);remove(p,Attributes.SCALE,SCALE);HousePackets.send(p,HazardViewPayload.clear());}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){clear(p);var engine=ENGINES.get(p.server);if(engine!=null)engine.readers.remove(p.getUUID());}}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer p){var e=ENGINES.get(p.server);if(e!=null){var r=e.readers.remove(p.getUUID());if(r!=null)save(e,r);e.flush();}clear(p);}}
    @SubscribeEvent public static void died(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p){var w=ENGINES.get(p.server);if(w!=null){var r=w.readers.remove(p.getUUID());if(r!=null)save(w,r);}clear(p);}}
    @SubscribeEvent public static void clone(PlayerEvent.Clone e){if(e.getOriginal() instanceof ServerPlayer p)clear(p);if(e.getEntity() instanceof ServerPlayer p)clear(p);}
    @SubscribeEvent public static void stopped(ServerStoppingEvent e){var w=ENGINES.remove(e.getServer());if(w!=null){w.flush();w.readers.values().forEach(r->clear(r.player));}}
    /** The room's environmental costs cannot execute a reader. Other damage retains its native meaning. */
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void damage(LivingDamageEvent.Pre event){if(!(event.getEntity() instanceof ServerPlayer p))return;var place=location(p,HouseSavedData.get(p.server).houseOrigin());if(!added(place))return;
        var source=event.getSource();if(source.is(DamageTypes.FREEZE)&&place==LabyrinthPlace.COLD_STORE||source.is(DamageTypes.FALL)||source.is(DamageTypes.DROWN)||source.is(DamageTypes.IN_WALL))event.setNewDamage(Math.min(event.getNewDamage(),Math.max(0,p.getHealth()-6)));}
    public static boolean compressorOpen(long occupied){return Math.floorMod(occupied,600)>=400;}
    private static void sound(ServerLevel l,BlockPos at,net.minecraft.sounds.SoundEvent sound,float volume,float pitch){l.playSound(null,at,sound,SoundSource.BLOCKS,volume,pitch);}
    private static void cold(ServerLevel l,BlockPos b,CompoundTag w,List<Reader> here){long clock=w.getLong("Clock");boolean open=compressorOpen(clock);var gate=b.offset(0,0,-43);if(open||vacant(l,List.of(gate,gate.above()),false))for(var at:List.of(gate,gate.above())){var state=l.getBlockState(at);if(state.getBlock() instanceof DoorBlock&&state.getValue(DoorBlock.OPEN)!=open)l.setBlock(at,state.setValue(DoorBlock.OPEN,open),FLAGS);}if(clock%600==380)sound(l,b.offset(0,1,-41),SoundEvents.IRON_DOOR_CLOSE,1.4F,.6F);if(clock%600==400)sound(l,b.offset(0,0,-43),SoundEvents.IRON_DOOR_OPEN,1.2F,.7F);if(!open&&clock%80==0)sound(l,b.offset(0,1,-41),SoundEvents.PISTON_CONTRACT,.55F,.55F);
        for(var r:here){var p=r.player;boolean warm=false;for(int z=-5;z>=-37;z-=8)if(Math.abs(p.getZ()-b.getZ()-z)<2.2&&Math.abs(p.getX()-b.getX())<2)warm=true;boolean together=here.stream().anyMatch(other->other!=r&&other.player.distanceToSqr(p)<6.25);
            if(warm)r.frost=Math.max(0,r.frost-8);else if(!together||clock%2==0)r.frost=Math.min(240,r.frost+1);p.setTicksFrozen(r.frost);modifier(p,Attributes.MOVEMENT_SPEED,COLD,-.25*Math.min(1,r.frost/140.));
            if(!warm&&r.frost>=140&&clock%40==0&&p.getHealth()>6)p.hurt(p.damageSources().freeze(),1);}
    }
    private static boolean holding(Reader r,long clock){return r.player.isShiftKeyDown()||r.own.getLong("RailUntil")>=clock&&r.own.contains("RailUntil");}
    @SubscribeEvent public static void rail(PlayerInteractEvent.RightClickBlock e){if(e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p)||!HazardBlock.is(p.level().getBlockState(e.getPos()),HazardBlock.Kind.RAIL))return;var w=engine(p.server);var r=w.readers.get(p.getUUID());if(r!=null&&r.place!=null&&(r.place==LabyrinthPlace.LISTING_HALL||r.place==LabyrinthPlace.TURNING_GRAVITY)&&p.distanceToSqr(e.getPos().getCenter())<9){r.own.putLong("RailUntil",w.room(r.place).getLong("Clock")+40);e.setCanceled(true);}}
    public static double list(long clock){return Math.sin(clock*Math.PI/300);}
    private static void listing(ServerLevel l,BlockPos b,long clock,List<Reader> here){double tilt=list(clock);if(clock%5!=0)return;for(var r:here){HousePackets.send(r.player,HazardViewPayload.list(b,(float)tilt,holding(r,clock)?0:(float)(tilt*.012)));if(Math.abs(tilt)>.16&&!holding(r,clock))push(r.player,tilt*.06,0);}
        if(Math.abs(tilt)>.16)for(var item:l.getEntitiesOfClass(ItemEntity.class,area(b,LabyrinthPlace.LISTING_HALL)))item.setDeltaMovement(item.getDeltaMovement().add(tilt*.04,0,0));if(clock%300==150)sound(l,b.offset(0,2,-22),SoundEvents.WOODEN_DOOR_CLOSE,.7F,.5F);}
    private static void push(ServerPlayer p,double x,double z){p.push(x,0,z);p.hurtMarked=true;}
    private static AABB area(BlockPos b,LabyrinthPlace p){var r=p.room();return new AABB(b.getX()+r.minX()+1,b.getY()+r.minY(),b.getZ()+r.minZ()+1,b.getX()+r.maxX(),b.getY()+r.maxY(),b.getZ());}
    private static void lamps(ServerLevel l,BlockPos b,boolean dark){for(int z=-5;z>=-37;z-=6){var at=b.offset(0,HazardMaps.underCeiling(LabyrinthPlace.QUIET_HALL),z);var s=l.getBlockState(at);if(HazardBlock.is(s,HazardBlock.Kind.HEAT_LAMP))l.setBlock(at,s.setValue(HazardBlock.STAGE,dark?1:0),FLAGS);}}
    private static void quiet(ServerLevel l,BlockPos b,CompoundTag w,List<Reader> here){long clock=w.getLong("Clock"),active=0;int noise=w.getInt("Noise");var sensors=HazardMaps.sensors(b);for(int i=0;i<sensors.size();i++){var s=l.getBlockState(sensors.get(i));if(s.is(Blocks.SCULK_SENSOR)&&s.getValue(SculkSensorBlock.PHASE)==SculkSensorPhase.ACTIVE)active|=1L<<i;}if(!w.getBoolean("Sweep"))noise+=Long.bitCount(active&~w.getLong("Sensors"));w.putLong("Sensors",active);w.putInt("Noise",noise);
        if(noise>=6&&!w.getBoolean("Sweep")){w.putBoolean("Sweep",true);w.putLong("SweepAt",clock);w.putInt("Generation",w.getInt("Generation")+1);lamps(l,b,true);sound(l,b.offset(0,0,-40),SoundEvents.SCULK_SHRIEKER_SHRIEK,1,.65F);}
        if(!w.getBoolean("Sweep"))return;long elapsed=clock-w.getLong("SweepAt");double z=-41+elapsed*.1;LiteraryActor walker=w.hasUUID("Walker")&&l.getEntity(w.getUUID("Walker")) instanceof LiteraryActor a?a:null;
        if(walker==null){walker=LiteraryRegistry.ACTOR.get().create(l);if(walker!=null){if(w.hasUUID("Walker"))walker.setUUID(w.getUUID("Walker"));walker.appearance(LiteraryActor.SILHOUETTE,5);walker.bind(LabyrinthPlace.QUIET_HALL,null);walker.setNoGravity(true);walker.noPhysics=true;walker.setInvulnerable(true);modifier(walker,Attributes.SCALE,id("hazard_walker"),.85);walker.moveTo(b.getX()+.5,b.getY(),b.getZ()-41,0,0);l.addFreshEntity(walker);w.putUUID("Walker",walker.getUUID());}}
        if(walker!=null){walker.moveTo(b.getX()+.5,b.getY(),b.getZ()+Math.min(-2,z),0,0);walker.setDeltaMovement(Vec3.ZERO);}
        if(elapsed%18==0)sound(l,b.offset(0,0,(int)Math.min(-2,z)),SoundEvents.WARDEN_STEP,.75F,.5F);
        for(var r:here)if(r.own.getInt("Swept")!=w.getInt("Generation")&&r.player.getZ()-b.getZ()<=z+1){r.own.putInt("Swept",w.getInt("Generation"));back(r.player,b);}
        if(z>=-2){if(walker!=null)walker.discard();w.remove("Walker");w.putBoolean("Sweep",false);w.putInt("Noise",0);lamps(l,b,false);}
    }
    private static void maze(ServerLevel l,BlockPos b,CompoundTag w){long mask=w.contains("Mask")?w.getLong("Mask"):HazardMaze.initialMask(b.asLong());var rng=new Random(w.getLong("Clock")^b.asLong());var closed=new ArrayList<Integer>();for(int i=0;i<60;i++)if((mask&(1L<<i))==0)closed.add(i);Collections.shuffle(closed,rng);
        for(int source:closed){var edge=HazardMaze.EDGES.get(source);var candidates=new ArrayList<>(HazardMaze.path(mask,edge.a(),edge.b()));Collections.shuffle(candidates,rng);for(int target:candidates){long next=HazardMaze.exchange(l,b,mask,source,target);if(next!=mask){w.putLong("Mask",next);LabyrinthData.get(l.getServer()).setStateEntry(ROOMS,key(b),w);sound(l,b.offset(edge.centre()),SoundEvents.PISTON_CONTRACT,.45F,.65F);return;}}}}
    private static void mirror(BlockPos b,long clock,List<Reader> here){for(var r:here){var p=r.player;var before=r.trail.peekLast();float stride=before==null?0:(float)Math.min(.8,Math.hypot(p.getX()-before.x(),p.getZ()-before.z())*4);r.trail.addLast(new Sample(p.getX(),p.getY(),p.getZ(),p.getYRot(),p.getXRot(),p.isShiftKeyDown(),stride));while(r.trail.size()>41)r.trail.removeFirst();int delay=clock<240?20:40;Sample sample=r.trail.peekFirst();if(r.trail.size()>delay){int skip=r.trail.size()-delay-1;for(var s:r.trail){if(skip--==0){sample=s;break;}}}if(clock%4!=0)continue;
            boolean answer=p.getZ()-b.getZ()<-30;double x=answer?b.getX()-14-(-4+4*r.own.getInt("MirrorDoor"))+.5:2*(b.getX()-6.5)-sample.x();double z=answer?b.getZ()-39:sample.z();float yaw=answer?0:-sample.yaw();HousePackets.send(p,HazardViewPayload.mirror(b,x,answer?b.getY():sample.y(),z,yaw,sample.pitch(),!answer&&sample.crouch(),answer?0:sample.stride()));}}
    private static void weight(BlockPos b,List<Reader> here){for(var r:here){var p=r.player;boolean heavy=p.getZ()-b.getZ()>-22;modifier(p,Attributes.GRAVITY,GRAVITY,heavy?2:-.65);modifier(p,Attributes.JUMP_STRENGTH,JUMP,heavy?-.5:.3);modifier(p,Attributes.FALL_DAMAGE_MULTIPLIER,FALL,-1);modifier(p,Attributes.MOVEMENT_SPEED,SPEED,heavy?-.2:0);
            if(heavy&&p.onClimbable()&&p.getDeltaMovement().y>.06)p.setDeltaMovement(p.getDeltaMovement().multiply(1,0,1).add(0,.06,0));if(!heavy&&p.getZ()-b.getZ()<-29&&p.getZ()-b.getZ()>-32&&p.getY()>b.getY()-.5){p.push(0,-.025,0);p.hurtMarked=true;}}}
    public static double nestedScale(double relativeZ){return relativeZ<=-26?.34:relativeZ<=-17?.5:relativeZ<=-8?.7:1;}
    private static void nested(BlockPos b,List<Reader> here){for(var r:here)modifier(r.player,Attributes.SCALE,SCALE,nestedScale(r.player.getZ()-b.getZ())-1);}
    public static int landingAt(Vec3 pos,BlockPos b){for(int i=1;i<=7;i++){var at=b.offset(HazardMaps.landing(i));if(Math.abs(pos.y-at.getY())<.8&&Math.abs(pos.z-at.getZ())<2.1&&pos.x-b.getX()>-2.4&&pos.x-b.getX()<6.5)return i;}return 0;}
    private static void counting(BlockPos b,long clock,List<Reader> here){for(var r:here){int at=landingAt(r.player.position(),b);if(at>0&&at!=r.own.getInt("LastLanding")){int count=r.own.getInt("Landings");if(at==count+1){r.own.putInt("Landings",count+1);r.player.causeFoodExhaustion(.3F);}r.own.putInt("LastLanding",at);}else if(at==0)r.own.putInt("LastLanding",0);if(clock%400==0)r.player.causeFoodExhaustion(.25F);}}
    private static void spiral(BlockPos b,long clock,List<Reader> here){if(clock%5!=0)return;for(var r:here){var p=r.player;double x=p.getX()-b.getX(),z=p.getZ()-b.getZ();if(p.getY()>b.getY()+.5&&Math.abs(x)>7)push(p,Math.signum(x)*.026,0);else if(p.getY()>b.getY()+.5&&(z<-23||z>-5))push(p,0,z<-23?-.026:.026);}}
    private static boolean vacant(ServerLevel l,List<BlockPos> cells,boolean cameras){for(var at:cells)if(!l.hasChunkAt(at)||l.getBlockEntity(at)!=null||!l.getEntities(null,new AABB(at).inflate(.12)).isEmpty()||cameras&&watched(l,at))return false;return true;}
    private static void pressure(ServerLevel l,BlockPos b,long clock,List<Reader> here){if(clock%12!=0||here.stream().noneMatch(r->r.player.getZ()-b.getZ()<-5))return;int reach=(int)Math.min(26,(clock%800)/12);var route=HazardMaps.pressureRoute();var lane=new HashSet<>(route);for(int n=0;n<Math.min(reach,route.size());n++){int at=Math.max(0,12+n/2*(n%2==0?1:-1));if(at>=route.size())continue;var center=route.get(at);boolean horizontal=at>=12;var cells=new ArrayList<BlockPos>();for(int side:new int[]{-1,1}){var edge=center.offset(horizontal?0:side,0,horizontal?side:0);if(lane.contains(edge))continue;for(int y=0;y<=2;y++)cells.add(b.offset(edge).above(y));}
            for(var r:here)if(cells.stream().anyMatch(pos->l.getBlockState(pos).isAir()&&r.player.getBoundingBox().intersects(new AABB(pos)))){if(clock>=r.own.getLong("PressureHit")){r.player.hurt(l.damageSources().inWall(),1);r.player.causeFoodExhaustion(.4F);r.own.putLong("PressureHit",clock+20);}double pull=(horizontal?b.getZ()+center.getZ()+.5-r.player.getZ():b.getX()+center.getX()+.5-r.player.getX());push(r.player,horizontal?.08:Math.clamp(pull,-.22,.22),horizontal?Math.clamp(pull,-.22,.22):.08);}
            if(!vacant(l,cells,false))continue;for(var p:cells)if(l.getBlockState(p).isAir())l.setBlock(p,HazardBlock.of(HazardBlock.Kind.PRESSURE_PANEL),FLAGS);}
        if(clock%120==0)sound(l,b.offset(0,1,-14),SoundEvents.PISTON_EXTEND,.8F,.6F);}
    private static List<BlockPos> doorCells(BlockPos b,int z){var out=new ArrayList<BlockPos>();for(int x=-2;x<=2;x++)for(int y=0;y<=3;y++)out.add(b.offset(x,y,z));return out;}
    private static void receding(ServerLevel l,BlockPos b,CompoundTag w,List<Reader> here){if(w.getLong("Clock")%5!=0||w.getLong("NearUntil")>w.getLong("Clock"))return;double delta=0;int movers=0;for(var r:here){double d=r.player.getZ()-r.lastZ;if(Math.abs(d)>.001&&Math.abs(d)<2){delta+=d;movers++;}}if(movers==0)return;int old=w.getInt("DoorZ"),next=old+(delta<0?-1:3);next=Math.max(-64,Math.min(-5,next));if(next==old)return;boolean reached=false;
        if(delta>0){double nearest=here.stream().mapToDouble(r->r.player.getZ()-b.getZ()).min().orElse(-3);next=Math.min(next,(int)Math.floor(nearest-2));if(next-old<=0)return;reached=next>=nearest-3;}
        var before=doorCells(b,old);var after=doorCells(b,next);var all=new ArrayList<>(before);all.addAll(after);for(int z=Math.min(old,next)+1;z<Math.max(old,next);z++)all.addAll(doorCells(b,z));if(!vacant(l,all,false))return;for(var at:after)if(!l.getBlockState(at).isAir())return;for(var at:before){var s=l.getBlockState(at);if(!s.is(Blocks.LIGHT_GRAY_TERRACOTTA)&&!(s.getBlock() instanceof DoorBlock))return;}
        before.forEach(at->l.setBlock(at,Blocks.AIR.defaultBlockState(),FLAGS));after.forEach(at->l.setBlock(at,Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState(),FLAGS));LabyrinthBuilder.placeDoor(l,b.offset(0,0,next),Direction.SOUTH);
        var d=LabyrinthData.get(l.getServer());var spec=LabyrinthPlace.RECEDING_HALL.doors().get(1);d.putDoor(new LabyrinthData.Door(LabyrinthPlace.RECEDING_HALL.doorId(spec),l.dimension(),b.offset(0,0,next),Direction.SOUTH,spec.destination(),false));w.putInt("DoorZ",next);if(reached)w.putLong("NearUntil",w.getLong("Clock")+160);d.setStateEntry(ROOMS,key(b),w);
    }
    private static int warmth(BlockState s){if(s.is(Blocks.TORCH)||s.is(Blocks.WALL_TORCH))return 14;if(s.is(Blocks.LANTERN))return 15;if(s.getBlock() instanceof CandleBlock&&s.hasProperty(CandleBlock.LIT)&&s.getValue(CandleBlock.LIT))return 3*s.getValue(CandleBlock.CANDLES);return 0;}
    private static void sconce(ServerLevel l,BlockPos b,CompoundTag w){var room=LabyrinthPlace.LAST_SCONCE.room();var lights=BlockPos.betweenClosed(b.offset(room.minX()+1,0,room.minZ()+1),b.offset(room.maxX()-1,room.maxY()-1,-1));BlockPos warm=null;int best=0;for(var pos:lights){int value=warmth(l.getBlockState(pos));if(value>best){best=value;warm=pos.immutable();}}
        if(warm!=null){var old=BlockPos.of(w.getLong("Sconce"));var next=new BlockPos(b.getX()+(warm.getX()<b.getX()?-8:8),b.getY()+2,warm.getZ());if(!next.equals(old)&&vacant(l,List.of(old,next),false)&&l.getBlockState(next).isAir()&&HazardBlock.is(l.getBlockState(old),HazardBlock.Kind.SCONCE)){l.setBlock(old,Blocks.AIR.defaultBlockState(),FLAGS);l.setBlock(next,HazardBlock.of(HazardBlock.Kind.SCONCE),FLAGS);w.putLong("Sconce",next.asLong());}}
        // The strongest portable flame becomes the one exception; weaker flames still have a finite life here.
        var lives=w.getCompound("Flames");var kept=new HashSet<String>();for(var pos:lights){var s=l.getBlockState(pos);if(!LabyrinthLighting.isPortableLight(s)&&warmth(s)==0)continue;String key=key(pos);kept.add(key);if(pos.equals(warm)){lives.remove(key);continue;}if(!lives.contains(key))lives.putLong(key,w.getLong("Clock"));else if(w.getLong("Clock")-lives.getLong(key)>=120&&l.getBlockEntity(pos)==null){if(s.getBlock() instanceof CandleBlock)l.setBlock(pos,s.setValue(CandleBlock.LIT,false),FLAGS);else{l.setBlock(pos,Blocks.AIR.defaultBlockState(),FLAGS);Block.popResource(l,pos,new net.minecraft.world.item.ItemStack(s.getBlock()));}lives.remove(key);}}
        for(var k:new ArrayList<>(lives.getAllKeys()))if(!kept.contains(k))lives.remove(k);w.put("Flames",lives);
    }
    public static boolean moveThreshold(ServerLevel l,BlockPos b,CompoundTag w,int next){int old=w.getInt("DoorIndex");if(next==old||next<0||next>2)return false;var p=LabyrinthPlace.WATCHED_THRESHOLD;var a=p.doors().get(old+1);var z=p.doors().get(next+1);var source=List.of(b.offset(a.rel()),b.offset(a.rel()).above());var target=List.of(b.offset(z.rel()),b.offset(z.rel()).above());var cells=new ArrayList<>(source);cells.addAll(target);if(!vacant(l,cells,true))return false;
        for(var at:source)if(!(l.getBlockState(at).getBlock() instanceof DoorBlock))return false;for(var at:target)if(!l.getBlockState(at).is(Blocks.LIGHT_GRAY_TERRACOTTA))return false;
        source.forEach(at->l.setBlock(at,Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState(),FLAGS));LabyrinthBuilder.placeDoor(l,b.offset(z.rel()),z.facing());w.putInt("DoorIndex",next);LabyrinthData.get(l.getServer()).setStateEntry(ROOMS,key(b),w);sound(l,b.offset(z.rel()),SoundEvents.IRON_DOOR_CLOSE,.55F,.8F);return true;}
    private static void threshold(ServerLevel l,BlockPos b,CompoundTag w){moveThreshold(l,b,w,(w.getInt("DoorIndex")+1)%3);}
    private static void gravity(ServerLevel l,BlockPos b,long clock,List<Reader> here){if(clock%5!=0)return;int quarter=(int)((clock/400)%4);double x=quarter==0?.045:quarter==2?-.045:0,z=quarter==1?.045:quarter==3?-.045:0;for(var r:here)if(!holding(r,clock))push(r.player,x,z);for(var item:l.getEntitiesOfClass(ItemEntity.class,area(b,LabyrinthPlace.TURNING_GRAVITY)))item.setDeltaMovement(item.getDeltaMovement().add(x,0,z));if(clock%400==0)sound(l,b.offset(0,2,-14),SoundEvents.PISTON_CONTRACT,.8F,.5F);}
    private static void back(ServerPlayer p,BlockPos b){p.teleportTo(p.serverLevel(),b.getX()+.5,b.getY(),b.getZ()-2.5,p.getYRot(),p.getXRot());p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;}
    /** Loop doors stay within this physical room and never mutate the reader's route or return stack. */
    public static boolean blocksDoor(ServerPlayer p,LabyrinthData.Door door){if(p.isSpectator()||p.isCreative())return false;var e=engine(p.server);var place=location(p,e.origin);if(!added(place)||LabyrinthData.RETURN.equals(door.destination))return false;var b=LabyrinthPlaces.base(e.origin,place);var w=e.room(place);var r=e.readers.get(p.getUUID());var own=r!=null&&r.place==place?r.own:e.data.stateEntry(READERS,readerKey(p,place));
        if(place==LabyrinthPlace.COLD_STORE&&!compressorOpen(w.getLong("Clock"))){sound(p.serverLevel(),door.lower,SoundEvents.IRON_DOOR_CLOSE,.6F,.65F);return true;}
        int index=-1;for(int i=1;i<place.doors().size();i++)if(place.doorId(place.doors().get(i)).equals(door.id))index=i-1;
        if(place==LabyrinthPlace.WATCHED_THRESHOLD&&index!=w.getInt("DoorIndex"))return true;
        boolean wrong=place==LabyrinthPlace.DUPLICATE_ROOMS&&index!=HazardMaps.missingRoom(b)||place==LabyrinthPlace.MIRROR_HALL&&index!=own.getInt("MirrorDoor")||place==LabyrinthPlace.COUNTING_STAIR&&(index!=6||own.getInt("Landings")!=7||landingAt(p.position(),b)!=7);
        if(wrong){back(p,b);if(place==LabyrinthPlace.COUNTING_STAIR){own.putInt("Landings",0);own.putInt("LastLanding",0);e.data.setStateEntry(READERS,readerKey(p,place),own);}sound(p.serverLevel(),b.offset(0,0,-2),SoundEvents.WOODEN_DOOR_CLOSE,.7F,.6F);return true;}return false;
    }
}
