package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Personal scenery, finite original readings, saved returns and bounded native room work. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StaircaseLeaks {
    public static final String STATE="staircase_leaks_0449",ROOMS="staircase_leak_rooms_0449";
    private static final String BACKUP="HouseStaircaseLeakReturn",WAITING="HouseStaircaseLeakWaiting",VIRTUAL="HouseStaircaseLeakVirtual";
    private static final Map<MinecraftServer,Engine> ENGINES=new WeakHashMap<>();
    private static final TicketType<Long> TICKET=TicketType.create("the_oldest_house_leak",Long::compare);
    private static long nextLease;
    private StaircaseLeaks(){}
    private static final class Lease implements AutoCloseable {
        final ServerLevel level;final long key=++nextLease;final List<ChunkPos> chunks=new ArrayList<>();
        Lease(ServerLevel level,AABB box){this.level=level;for(int x=((int)Math.floor(box.minX)-1)>>4;x<=((int)Math.ceil(box.maxX)+1)>>4;x++)for(int z=((int)Math.floor(box.minZ)-1)>>4;z<=((int)Math.ceil(box.maxZ)+1)>>4;z++){var c=new ChunkPos(x,z);chunks.add(c);level.getChunkSource().addRegionTicket(TICKET,c,3,key);}}
        boolean ready(){return chunks.stream().allMatch(c->level.hasChunk(c.x,c.z)&&level.areEntitiesLoaded(c.toLong()));}
        @Override public void close(){for(var c:chunks)level.getChunkSource().removeRegionTicket(TICKET,c,3,key);chunks.clear();}
    }
    private static final class Offer {
        final ServerPlayer reader;final Vec3 position;final BlockPos paper;final int index;final long after;int slot=-1;long entering;
        Offer(ServerPlayer reader,BlockPos paper,int index){this.reader=reader;this.paper=paper.immutable();this.index=index;position=reader.position();after=reader.serverLevel().getGameTime()+60;}
    }
    private static final class Room {
        final int slot;final BlockPos base;final StaircaseLeakRooms.Kind kind;final StaircaseLeakRooms.Template template;final Lease lease;final CompoundTag record;
        Room(ServerLevel l,int slot,CompoundTag record){this.slot=slot;this.record=record;base=BlockPos.of(record.getLong("Base"));kind=StaircaseLeakRooms.kind(record.getInt("Index"));template=StaircaseLeakRooms.Template.load(l,record.getCompound("Template"));lease=new Lease(l,StaircaseLeakRooms.bounds(base));}
        void close(){lease.close();}
    }
    private static final class Engine {
        final MinecraftServer server;final LabyrinthData data;final Map<UUID,Offer> offers=new HashMap<>();final Map<UUID,CompoundTag> sessions=new HashMap<>();final Set<UUID> idle=new HashSet<>();final Map<UUID,Lease> sources=new HashMap<>();final Map<Integer,Room> work=new LinkedHashMap<>();
        final ArrayDeque<String> savedWork=new ArrayDeque<>();
        Engine(MinecraftServer server){this.server=server;data=LabyrinthData.get(server);for(var key:data.stateKeys(ROOMS))if(!key.equals("meta"))savedWork.add(key);}
        void close(){for(var lease:sources.values())lease.close();for(var room:work.values())room.close();offers.clear();sessions.clear();idle.clear();sources.clear();work.clear();}
    }
    private static Engine engine(MinecraftServer server){var e=ENGINES.get(server);if(e==null||e.data!=LabyrinthData.get(server)){if(e!=null)e.close();e=new Engine(server);ENGINES.put(server,e);}return e;}
    public static void clearForServer(MinecraftServer server){var e=ENGINES.remove(server);if(e!=null)e.close();}
    private static CompoundTag own(ServerPlayer p){return LabyrinthData.get(p.server).stateEntry(STATE,p.getUUID().toString());}
    private static void save(ServerPlayer p,CompoundTag own){LabyrinthData.get(p.server).setStateEntry(STATE,p.getUUID().toString(),own);}
    /**
     * The player's own saved copy decides whether they are in a scene: it is saved with their position
     * and inventory, so it can never be older than them. The world's copy is only a mirror. A reader with
     * no scene is remembered as such, so the hot paths (every tick, pet tick and damage event) do not
     * copy saved records.
     */
    private static CompoundTag session(ServerPlayer p){
        var e=engine(p.server);var id=p.getUUID();var cached=e.sessions.get(id);if(cached!=null)return cached;
        if(e.idle.contains(id))return null;
        if(!p.getPersistentData().contains(BACKUP)){e.idle.add(id);return null;}
        var active=p.getPersistentData().getCompound(BACKUP).copy();e.sessions.put(id,active);return active;
    }
    /**
     * A world record of a scene that the player's own data no longer has is stale: the world was saved
     * mid-scene, the player returned and was saved afterwards, and the server stopped before the world
     * saved again. The player is already where they belong, so they are not moved; the record is cleared,
     * waiting pets and the reader's echo are released, and the room is queued for restoration.
     */
    private static void dropStale(ServerPlayer p){
        if(p.getPersistentData().contains(BACKUP))return;var record=own(p);var stale=record.getCompound("Active");if(stale.isEmpty())return;
        var e=engine(p.server);cleanupSource(p,stale);record.remove("Active");save(p,record);
        var room=request(e,stale.getInt("Slot"));if(room!=null){room.record.putBoolean("Dirty",true);room.record.putInt("Cursor",0);e.data.setStateEntry(ROOMS,Integer.toString(room.slot),room.record);}
    }
    public static boolean active(ServerPlayer p){return session(p)!=null;}
    public static boolean active(MinecraftServer server,UUID id){var p=server.getPlayerList().getPlayer(id);return p!=null&&session(p)!=null&&!session(p).getBoolean("Recovering");}
    public static boolean offered(ServerPlayer p){return engine(p.server).offers.containsKey(p.getUUID());}
    public static BlockPos activeBase(ServerPlayer p){var s=session(p);return s==null?null:BlockPos.of(s.getLong("RoomBase"));}
    public static int activeIndex(ServerPlayer p){var s=session(p);return s==null?-1:s.getInt("Index");}
    public static CompoundTag progress(ServerPlayer p){var s=session(p);return s==null?new CompoundTag():s.getCompound("Chore");}
    public static void progress(ServerPlayer p,CompoundTag chore){var s=session(p);if(s==null)return;s.put("Chore",chore.copy());persist(p,s);}
    private static void persist(ServerPlayer p,CompoundTag session){var record=own(p);record.put("Active",session.copy());save(p,record);p.getPersistentData().put(BACKUP,session.copy());engine(p.server).idle.remove(p.getUUID());}

    /** Only the server's personal surface-reader menu calls this; normal book copies send no offer. */
    static void offer(ServerPlayer p,BlockPos paper,ItemStack original){
        if(!eligible(p)||active(p))return;var pages=LabyrinthData.get(p.server).stateEntry(StaircaseWriting.ID,p.getUUID().toString());String key=Long.toString(paper.asLong());
        if(!pages.getCompound("Books").contains(key)||!ItemStack.isSameItemSameComponents(original,ItemStack.parseOptional(p.registryAccess(),pages.getCompound("Books").getCompound(key))))return;
        var indices=pages.getCompound("Indices");int index=indices.contains(key)?indices.getInt(key):Math.floorMod(StaircaseWriting.ordinal(HouseSavedData.get(p.server).houseOrigin(),paper),StaircaseNotes.TEXTS.size());
        if(StaircaseLeakRooms.kind(index)==null||own(p).getCompound("Done").getBoolean(Integer.toString(index)))return;
        engine(p.server).offers.put(p.getUUID(),new Offer(p,paper,index));
    }
    private static boolean eligible(ServerPlayer p){
        var origin=HouseSavedData.get(p.server).houseOrigin();var phase=FinaleProgress.phase(p.server,p.getUUID());
        return p.isAlive()&&!p.isSpectator()&&!p.isPassenger()&&origin!=null&&p.serverLevel().dimension().equals(HouseDimensions.INTERIOR)&&FinaleArchitecture.contains(origin,p.blockPosition())&&(phase==FinaleProgress.Phase.STAIRCASE||phase==FinaleProgress.Phase.HOMEWARD)&&!HouseTransitionEvents.isPending(p);
    }
    private static boolean safe(Offer offer){var p=offer.reader;return !p.isRemoved()&&eligible(p)&&p.containerMenu==p.inventoryMenu&&p.distanceToSqr(offer.position)<.0025&&p.hurtTime==0&&p.serverLevel().getEntitiesOfClass(Monster.class,p.getBoundingBox().inflate(16),LivingEntity::isAlive).isEmpty();}

    private static int allocate(ServerPlayer p,int index){
        var e=engine(p.server);var record=own(p);var slots=record.getCompound("Slots");String key=Integer.toString(index);
        if(slots.contains(key))return slots.getInt(key);
        var meta=e.data.stateEntry(ROOMS,"meta");int slot=meta.getInt("Next");meta.putInt("Next",slot+1);e.data.setStateEntry(ROOMS,"meta",meta);
        var room=new CompoundTag();room.putUUID("Owner",p.getUUID());room.putInt("Index",index);room.putLong("Base",StaircaseLeakRooms.base(HouseSavedData.get(p.server).houseOrigin(),slot).asLong());room.putBoolean("Dirty",true);room.putInt("Cursor",0);room.put("Source",pose(p));
        room.put("Template",StaircaseLeakRooms.authored(StaircaseLeakRooms.kind(index),record.getIntArray("KitchenCups")).save());e.data.setStateEntry(ROOMS,Integer.toString(slot),room);
        slots.putInt(key,slot);record.put("Slots",slots);save(p,record);request(e,slot);return slot;
    }
    private static Room request(Engine e,int slot){var old=e.work.get(slot);if(old!=null)return old;var level=e.server.getLevel(HouseDimensions.INTERIOR);var record=e.data.stateEntry(ROOMS,Integer.toString(slot));if(level==null||record.isEmpty())return null;var room=new Room(level,slot,record);e.work.put(slot,room);return room;}
    private static void build(Engine e){
        // Restart unfinished restoration after a save reload, one saved record at a time.
        if(e.work.size()<2&&!e.savedWork.isEmpty()){var key=e.savedWork.removeFirst();var saved=e.data.stateEntry(ROOMS,key);if(saved.getBoolean("Dirty"))request(e,Integer.parseInt(key));}
        for(var room:new ArrayList<>(e.work.values())){
            if(!room.lease.ready())continue;
            if(!room.record.getBoolean("Dirty"))continue;
            var level=room.lease.level;var area=StaircaseLeakRooms.bounds(room.base);
            if(level.players().stream().anyMatch(p->area.contains(p.position())||area.contains(p.getCamera().position())))continue;
            // Only entities created inside this private scenery are removed; waiting pets remain on the tread.
            level.getEntities((Entity)null,area,entity->entity.getPersistentData().getBoolean(VIRTUAL)).forEach(Entity::discard);
            int cursor=Math.max(0,room.record.getInt("Cursor")),end=Math.min(StaircaseLeakRooms.CELLS,cursor+512);long deadline=System.nanoTime()+3_000_000;
            for(;cursor<end&&System.nanoTime()<deadline;cursor++){var at=StaircaseLeakRooms.position(room.base,cursor);var state=room.template.state(cursor);if(!level.getBlockState(at).equals(state))level.setBlock(at,state,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
            room.record.putInt("Cursor",cursor);if(cursor==StaircaseLeakRooms.CELLS){room.record.putBoolean("Dirty",false);room.record.putBoolean("Ready",true);}
            e.data.setStateEntry(ROOMS,Integer.toString(room.slot),room.record);return;
        }
    }
    /** Where a stray visitor to a room is sent back to; nothing else of theirs is kept with the room. */
    private static CompoundTag pose(ServerPlayer p){var s=new CompoundTag();s.putDouble("X",p.getX());s.putDouble("Y",p.getY());s.putDouble("Z",p.getZ());s.putFloat("Yaw",p.getYRot());s.putFloat("Pitch",p.getXRot());return s;}
    /** A room whose authored template has changed is rebuilt from it before its next visit. */
    private static void retemplate(Engine e,int slot,StaircaseLeakRooms.Template template){
        var record=e.data.stateEntry(ROOMS,Integer.toString(slot));if(record.isEmpty())return;
        record.put("Template",template.save());record.putBoolean("Dirty",true);record.remove("Ready");record.putInt("Cursor",0);e.data.setStateEntry(ROOMS,Integer.toString(slot),record);
        var old=e.work.remove(slot);if(old!=null)old.close();
    }
    private static CompoundTag snapshot(ServerPlayer p){
        var s=new CompoundTag();s.put("Inventory",p.getInventory().save(new ListTag()));var food=new CompoundTag();p.getFoodData().addAdditionalSaveData(food);s.put("Food",food);
        var effects=new ListTag();for(var effect:p.getActiveEffects())effects.add(effect.save());s.put("Effects",effects);
        s.putFloat("Health",p.getHealth());s.putFloat("Absorption",p.getAbsorptionAmount());s.putInt("Selected",p.getInventory().selected);s.putInt("Level",p.experienceLevel);s.putInt("Experience",p.totalExperience);s.putFloat("Progress",p.experienceProgress);
        s.putDouble("X",p.getX());s.putDouble("Y",p.getY());s.putDouble("Z",p.getZ());s.putFloat("Yaw",p.getYRot());s.putFloat("Pitch",p.getXRot());s.putDouble("Vx",p.getDeltaMovement().x);s.putDouble("Vy",p.getDeltaMovement().y);s.putDouble("Vz",p.getDeltaMovement().z);return s;
    }
    private static void restore(ServerPlayer p,CompoundTag s){
        p.getInventory().load(s.getList("Inventory",Tag.TAG_COMPOUND));p.getFoodData().readAdditionalSaveData(s.getCompound("Food"));p.removeAllEffects();
        for(var tag:s.getList("Effects",Tag.TAG_COMPOUND)){var effect=MobEffectInstance.load((CompoundTag)tag);if(effect!=null)p.addEffect(effect);}
        p.setHealth(s.getFloat("Health"));p.setAbsorptionAmount(s.getFloat("Absorption"));p.getInventory().selected=s.getInt("Selected");p.connection.send(new ClientboundSetCarriedItemPacket(p.getInventory().selected));p.experienceLevel=s.getInt("Level");p.totalExperience=s.getInt("Experience");p.experienceProgress=s.getFloat("Progress");
        p.inventoryMenu.broadcastChanges();p.connection.send(new ClientboundSetHealthPacket(p.getHealth(),p.getFoodData().getFoodLevel(),p.getFoodData().getSaturationLevel()));p.connection.send(new ClientboundSetExperiencePacket(p.experienceProgress,p.totalExperience,p.experienceLevel));
    }
    private static void enter(ServerPlayer p,Offer offer,Room room){
        var e=engine(p.server);var s=new CompoundTag();s.putInt("Index",offer.index);s.putLong("Paper",offer.paper.asLong());s.putInt("Slot",room.slot);s.putLong("RoomBase",room.base.asLong());s.put("Return",snapshot(p));s.putLong("Started",p.serverLevel().getGameTime());s.put("Chore",new CompoundTag());
        var waits=new ListTag();for(var pet:CompanionOrders.followingAll(p)){var tag=new CompoundTag();tag.putUUID("Id",pet.getUUID());tag.putBoolean("NoAI",pet.isNoAi());waits.add(tag);pet.getNavigation().stop();pet.getPersistentData().putUUID(WAITING,p.getUUID());pet.getPersistentData().putBoolean(WAITING+"NoAI",pet.isNoAi());pet.setNoAi(true);}
        s.put("Pets",waits);e.sources.put(p.getUUID(),new Lease(p.serverLevel(),p.getBoundingBox().inflate(34,5,34)));e.sessions.put(p.getUUID(),s);persist(p,s);
        var echo=StaircaseLeakRegistry.READER.get().create(p.serverLevel());if(echo!=null){echo.reader(p);echo.moveTo(p.position());echo.setYRot(p.getYRot());echo.setYHeadRot(p.getYHeadRot());if(p.serverLevel().addFreshEntity(echo))s.putUUID("Echo",echo.getUUID());}
        for(var spectator:p.serverLevel().players())if(spectator.isSpectator()&&spectator.getCamera()==p)spectator.setCamera(spectator);
        HouseInternalTeleport.shiftPlayerOnly(p,StaircaseLeakRooms.spawn(room.base,room.kind),180,0);p.setDeltaMovement(Vec3.ZERO);p.connection.send(new ClientboundSetEntityMotionPacket(p));
        if(room.kind==StaircaseLeakRooms.Kind.CAR){HouseSitting.sit(p,room.base.offset(1,0,0));if(p.getVehicle()!=null){p.getVehicle().getPersistentData().putBoolean(VIRTUAL,true);s.putUUID("Seat",p.getVehicle().getUUID());}}
        persist(p,s);HousePackets.send(p,new StaircaseLeakPayload(true,room.kind.ordinal(),0));
        io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"note_scene_enter","index",offer.index);
        // One faint call through the floorboards; this is not a second staircase quake clock.
        p.playNotifySound(SoundEvent.createVariableRangeEvent(Growl.Kind.BELOW.sound()),SoundSource.AMBIENT,.16F,.68F);
    }
    public static void returnAfterBeat(ServerPlayer p,int ticks){var s=session(p);if(s==null||s.contains("Closing"))return;s.putLong("Closing",p.serverLevel().getGameTime()+Math.max(1,ticks));persist(p,s);HousePackets.send(p,new HouseFadePayload(Math.max(1,ticks),2,12));}
    public static void returnNow(ServerPlayer p){
        var s=session(p);if(s==null)return;var e=engine(p.server);var level=p.server.getLevel(HouseDimensions.INTERIOR);if(level==null)return;
        var source=s.getCompound("Return");var pos=new Vec3(source.getDouble("X"),source.getDouble("Y"),source.getDouble("Z"));
        var lease=e.sources.computeIfAbsent(p.getUUID(),ignored->new Lease(level,new AABB(BlockPos.containing(pos)).inflate(34,5,34)));if(!lease.ready()){s.putBoolean("Recovering",true);persist(p,s);return;}
        p.closeContainer();p.stopRiding();
        if(p.serverLevel()==level)HouseInternalTeleport.shiftPlayerOnly(p,pos,source.getFloat("Yaw"),source.getFloat("Pitch"));else{p.teleportTo(level,pos.x,pos.y,pos.z,source.getFloat("Yaw"),source.getFloat("Pitch"));p.connection.resetPosition();}
        restore(p,source);p.setDeltaMovement(new Vec3(source.getDouble("Vx"),source.getDouble("Vy"),source.getDouble("Vz")));p.connection.send(new ClientboundSetEntityMotionPacket(p));p.resetFallDistance();
        io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"note_scene_leave","index",s.getInt("Index"),"finished",s.contains("Closing"),"recovered",s.getBoolean("Recovering"),"seconds",(p.serverLevel().getGameTime()-s.getLong("Started"))/20);
        cleanupSource(p,s);var record=own(p);var done=record.getCompound("Done");done.putBoolean(Integer.toString(s.getInt("Index")),true);record.put("Done",done);
        if(s.getInt("Index")==0&&s.getCompound("Chore").getInt("Cups")==3){
            record.putIntArray("KitchenCups",s.getCompound("Chore").getIntArray("HookColors"));
            // A late kitchen allocated before this one was finished is rebuilt with the cups where they now hang.
            var slots=record.getCompound("Slots");if(slots.contains("32")&&!record.getCompound("Done").getBoolean("32"))
                retemplate(e,slots.getInt("32"),StaircaseLeakRooms.authored(StaircaseLeakRooms.Kind.LATE_KITCHEN,record.getIntArray("KitchenCups")));
        }
        record.remove("Active");save(p,record);p.getPersistentData().remove(BACKUP);e.sessions.remove(p.getUUID());
        var room=request(e,s.getInt("Slot"));if(room!=null){room.record.putBoolean("Dirty",true);room.record.putInt("Cursor",0);e.data.setStateEntry(ROOMS,Integer.toString(room.slot),room.record);}var held=e.sources.remove(p.getUUID());if(held!=null)held.close();
        HousePackets.send(p,new StaircaseLeakPayload(false,0,s.getInt("Index")==32?200:0));HousePackets.send(p,new HouseFadePayload(0,1,12));
        if(s.contains("Closing")&&!s.getBoolean("Recovering")&&p.isAlive()&&!p.isSpectator()&&p.server.getPlayerList().getPlayer(p.getUUID())==p){var paper=BlockPos.of(s.getLong("Paper"));if(p.serverLevel().getBlockState(paper).is(HouseBlocks.NOTE_SURFACE.get())&&StaircaseWriting.open(p,paper)){var book=p.containerMenu.getSlot(0).getItem().get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT);if(book!=null&&book.pages().size()>1)p.containerMenu.clickMenuButton(p,100+book.pages().size()-1);}}
    }
    private static void cleanupSource(ServerPlayer p,CompoundTag s){
        var l=p.server.getLevel(HouseDimensions.INTERIOR);if(l==null)return;
        if(s.hasUUID("Echo")){var entity=l.getEntity(s.getUUID("Echo"));if(entity instanceof StaircaseReaderEcho)entity.discard();}
        if(s.hasUUID("Seat")){var entity=l.getEntity(s.getUUID("Seat"));if(entity!=null&&entity.getPersistentData().getBoolean(VIRTUAL))entity.discard();}
        for(var tag:s.getList("Pets",Tag.TAG_COMPOUND)){var pet=(CompoundTag)tag;var entity=l.getEntity(pet.getUUID("Id"));if(entity instanceof Mob mob){mob.setNoAi(pet.getBoolean("NoAI"));mob.getPersistentData().remove(WAITING);mob.getPersistentData().remove(WAITING+"NoAI");}}
    }

    /** This guard runs before the generic House boundary can classify a private room as a breach. */
    public static boolean guardTick(ServerPlayer p){
        var s=session(p);if(s==null){
            if(!p.serverLevel().dimension().equals(HouseDimensions.INTERIOR))return false;
            var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
            int x=Math.floorDiv(p.blockPosition().getX()-origin.getX()-8192+12,64),z=Math.floorDiv(p.blockPosition().getZ()-origin.getZ()-8192+20,64);
            if(x<0||x>=64||z<0)return false;
            var e=engine(p.server);var room=e.data.stateEntry(ROOMS,Integer.toString(z*64+x));
            if(room.isEmpty()||!StaircaseLeakRooms.bounds(BlockPos.of(room.getLong("Base"))).contains(p.position()))return false;
            var source=room.getCompound("Source");if(source.isEmpty())return false;
            var pos=new Vec3(source.getDouble("X"),source.getDouble("Y"),source.getDouble("Z"));
            var lease=e.sources.computeIfAbsent(p.getUUID(),ignored->new Lease(p.serverLevel(),new AABB(BlockPos.containing(pos)).inflate(2)));
            p.setCamera(p);if(lease.ready()){HouseInternalTeleport.shiftPlayerOnly(p,pos,source.getFloat("Yaw"),source.getFloat("Pitch"));lease.close();e.sources.remove(p.getUUID());}return true;
        }
        if(s.getBoolean("Recovering")||!p.serverLevel().dimension().equals(HouseDimensions.INTERIOR)||p.isSpectator()){returnNow(p);return true;}
        var b=BlockPos.of(s.getLong("RoomBase"));var kind=StaircaseLeakRooms.kind(s.getInt("Index"));
        if(!StaircaseLeakRooms.inside(b,kind,p.position())||kind==StaircaseLeakRooms.Kind.CAR&&!p.isPassenger()){returnNow(p);return true;}
        p.resetFallDistance();return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        var server=event.getServer();var e=engine(server);build(e);
        for(var offer:new ArrayList<>(e.offers.values())){
            if(!safe(offer)){e.offers.remove(offer.reader.getUUID());if(offer.entering>0&&!HouseTransitionEvents.isPending(offer.reader))HousePackets.send(offer.reader,new HouseFadePayload(0,0,6));continue;}
            if(offer.reader.serverLevel().getGameTime()<offer.after)continue;
            if(offer.slot<0)offer.slot=allocate(offer.reader,offer.index);var room=request(e,offer.slot);
            if(room!=null&&room.lease.ready()&&room.record.getBoolean("Ready")&&!room.record.getBoolean("Dirty")){
                if(offer.entering==0){offer.entering=offer.reader.serverLevel().getGameTime()+8;HousePackets.send(offer.reader,new HouseFadePayload(8,3,12));}
                else if(offer.reader.serverLevel().getGameTime()>=offer.entering){enter(offer.reader,offer,room);e.offers.remove(offer.reader.getUUID());}
            }
        }
        for(var p:new ArrayList<>(server.getPlayerList().getPlayers())){
            var s=session(p);if(s==null)continue;
            for(var spectator:server.getPlayerList().getPlayers())if(spectator.isSpectator()&&spectator.getCamera()==p){spectator.setCamera(spectator);var back=s.getCompound("Return");if(spectator.serverLevel()==p.serverLevel())HouseInternalTeleport.shiftPlayerOnly(spectator,new Vec3(back.getDouble("X"),back.getDouble("Y"),back.getDouble("Z")),back.getFloat("Yaw"),back.getFloat("Pitch"));}
            if(s.getBoolean("Recovering")||s.contains("Closing")&&p.serverLevel().getGameTime()>=s.getLong("Closing")){returnNow(p);continue;}
            if(!p.isAlive()){returnNow(p);continue;}var kind=StaircaseLeakRooms.kind(s.getInt("Index"));long elapsed=p.serverLevel().getGameTime()-s.getLong("Started");
            if(kind!=StaircaseLeakRooms.Kind.REPAIR&&elapsed>=2400){returnNow(p);continue;}
            if(server.getTickCount()%40==0)HousePackets.send(p,new StaircaseLeakPayload(true,kind.ordinal(),0));
            if(elapsed%100==0&&(kind==StaircaseLeakRooms.Kind.KITCHEN||kind==StaircaseLeakRooms.Kind.LATE_KITCHEN))p.playNotifySound(StaircaseLeakRegistry.DRIP.get(),SoundSource.AMBIENT,.18F,1);
            if(elapsed%80==0&&kind!=StaircaseLeakRooms.Kind.CAR)p.playNotifySound(StaircaseLeakRegistry.RAIN.get(),SoundSource.AMBIENT,.07F,1);
            if(elapsed%20==0&&kind==StaircaseLeakRooms.Kind.REPAIR)p.playNotifySound(StaircaseLeakRegistry.CLOCK.get(),SoundSource.AMBIENT,.12F,1);
            if(elapsed%100==0&&kind==StaircaseLeakRooms.Kind.CAR&&!s.getCompound("Chore").getBoolean("RadioOff"))p.playNotifySound(StaircaseLeakRegistry.RADIO.get(),SoundSource.AMBIENT,.1F,1);
        }
        // A completed, clean room needs no perpetual ticket or in-memory decoded template.
        for(var it=e.work.entrySet().iterator();it.hasNext();){var room=it.next().getValue();if(!room.record.getBoolean("Dirty")&&e.sessions.values().stream().noneMatch(s->s.getInt("Slot")==room.slot)&&e.offers.values().stream().noneMatch(o->o.slot==room.slot)){room.close();it.remove();}}
    }
    public static void caption(ServerPlayer p,String text){p.displayClientMessage(Component.literal(text),true);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void use(PlayerInteractEvent.RightClickBlock event){if(event.getLevel().isClientSide()||!(event.getEntity() instanceof ServerPlayer p)||!active(p))return;
        // The main hand does the chore; nothing either hand holds is used or placed in the room.
        if(event.getHand()==InteractionHand.MAIN_HAND)StaircaseLeakChores.interact(p,event.getPos());event.setCanceled(true);event.setCancellationResult(event.getHand()==InteractionHand.MAIN_HAND?InteractionResult.SUCCESS:InteractionResult.FAIL);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void hit(PlayerInteractEvent.LeftClickBlock event){if(event.getEntity() instanceof ServerPlayer p&&active(p))event.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void mine(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event){if(event.getPlayer() instanceof ServerPlayer p&&active(p))event.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void place(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event){if(event.getEntity() instanceof ServerPlayer p&&active(p))event.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void item(PlayerInteractEvent.RightClickItem event){if(event.getEntity() instanceof ServerPlayer p&&active(p)){event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);}}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void pickup(ItemEntityPickupEvent.Pre event){if(event.getItemEntity().getPersistentData().getBoolean(VIRTUAL)||event.getPlayer() instanceof ServerPlayer p&&active(p))event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void attack(AttackEntityEvent event){if(event.getEntity() instanceof ServerPlayer p)engine(p.server).offers.remove(p.getUUID());}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void damage(LivingIncomingDamageEvent event){if(event.getSource().getEntity() instanceof ServerPlayer attacker)engine(attacker.server).offers.remove(attacker.getUUID());if(event.getEntity() instanceof ServerPlayer p){engine(p.server).offers.remove(p.getUUID());if(active(p)){event.setCanceled(true);returnNow(p);}}}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void death(LivingDeathEvent event){if(event.getEntity() instanceof ServerPlayer p&&active(p)){event.setCanceled(true);p.setHealth(Math.max(1,session(p).getCompound("Return").getFloat("Health")));returnNow(p);}}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void login(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer p){var e=engine(p.server);e.idle.remove(p.getUUID());e.sessions.remove(p.getUUID());dropStale(p);var s=session(p);if(s!=null){s.putBoolean("Recovering",true);HousePackets.send(p,new HouseFadePayload(1,20,12));returnNow(p);}}}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void respawn(PlayerEvent.PlayerRespawnEvent event){if(event.getEntity() instanceof ServerPlayer p){var s=session(p);if(s!=null){s.putBoolean("Recovering",true);returnNow(p);}}}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer p){var e=engine(p.server);e.offers.remove(p.getUUID());e.idle.remove(p.getUUID());var s=session(p);if(s!=null){s.putBoolean("Recovering",true);persist(p,s);returnNow(p);if(session(p)!=null){cleanupSource(p,s);var room=request(e,s.getInt("Slot"));if(room!=null){room.record.putBoolean("Dirty",true);room.record.putInt("Cursor",0);e.data.setStateEntry(ROOMS,Integer.toString(room.slot),room.record);}e.sessions.remove(p.getUUID());var held=e.sources.remove(p.getUUID());if(held!=null)held.close();}}}}
    @SubscribeEvent public static void join(EntityJoinLevelEvent event){if(!(event.getLevel() instanceof ServerLevel l))return;
        var entity=event.getEntity();if(entity instanceof Mob mob&&mob.getPersistentData().hasUUID(WAITING)&&!active(l.getServer(),mob.getPersistentData().getUUID(WAITING))){mob.setNoAi(mob.getPersistentData().getBoolean(WAITING+"NoAI"));mob.getPersistentData().remove(WAITING);mob.getPersistentData().remove(WAITING+"NoAI");}
        if(entity instanceof ItemEntity||entity instanceof ExperienceOrb)for(var session:engine(l.getServer()).sessions.values())if(StaircaseLeakRooms.bounds(BlockPos.of(session.getLong("RoomBase"))).contains(entity.position())){entity.getPersistentData().putBoolean(VIRTUAL,true);if(entity instanceof ExperienceOrb){event.setCanceled(true);entity.discard();}break;}
    }
    @SubscribeEvent public static void stopping(ServerStoppingEvent event){for(var p:new ArrayList<>(event.getServer().getPlayerList().getPlayers()))if(active(p))logout(new PlayerEvent.PlayerLoggedOutEvent(p));clearForServer(event.getServer());}
    /** Late logouts during shutdown can recreate the engine; drop it again so it cannot hold the stopped server. */
    @SubscribeEvent public static void stopped(ServerStoppedEvent event){clearForServer(event.getServer());}
}
