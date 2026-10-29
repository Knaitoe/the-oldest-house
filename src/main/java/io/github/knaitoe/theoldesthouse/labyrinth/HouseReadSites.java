package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Private quiet moments. Saved actions serve only these rooms; categories and later payoffs are deferred. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HouseReadSites {
    public static final String STATE="house_read_rooms_04138";
    /**
     * 0.4.159 rebuilt the waiting room, coatroom and shelter; a copy prepared ahead but never entered is built again.
     * 0.4.163: the porch's swing hangs from its ropes and the note is carved into the railing.
     */
    public static final int LAYOUT=167;
    public enum Room {
        WAITING(LabyrinthPlace.READ_WAITING,483328), BATHROOM(LabyrinthPlace.READ_BATHROOM,491520),
        COATROOM(LabyrinthPlace.READ_COATROOM,466944), SHELTER(LabyrinthPlace.READ_SHELTER,499712), PORCH(LabyrinthPlace.READ_PORCH,475136),
        /** 0.4.168: appended, so saved names and offsets keep their places; {@link #CHAIN} puts it first. */
        KITCHEN(LabyrinthPlace.READ_KITCHEN,507904);
        private final LabyrinthPlace place;public final int offsetZ;
        Room(LabyrinthPlace place,int offsetZ){this.place=place;this.offsetZ=offsetZ;}
        public String key(){return name().toLowerCase(Locale.ROOT);}
        public LabyrinthPlace place(){return place;}
        public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension(){return this==PORCH?HouseDimensions.OUTSIDE:HouseDimensions.INTERIOR;}
    }
    /**
     * The order the rooms are met in. 0.4.168 (owner: "The skin-color selection should be the first vignette the player
     * encounters"): the kitchen table comes first. A reader who entered the other five before it existed meets it next.
     */
    public static final List<Room> CHAIN=List.of(Room.KITCHEN,Room.WAITING,Room.BATHROOM,Room.COATROOM,Room.SHELTER,Room.PORCH);
    private record Job(UUID owner,Room room){}
    private record Request(String door,LabyrinthPlace next,Room room){}
    private static final TicketType<ChunkPos> TICKET=TicketType.create("house_read_site",Comparator.comparingLong(ChunkPos::toLong));
    private static final Set<Job> WAITING=new LinkedHashSet<>();
    private static final Map<UUID,Request> PENDING=new LinkedHashMap<>();
    private static final List<ChunkPos> CHUNKS=new ArrayList<>();
    private static LabyrinthData store;private static Job building;private static ServerLevel leased;private static BuildBlocks.Plan plan;
    private HouseReadSites(){}
    public static CompoundTag state(MinecraftServer s,UUID id){return LabyrinthData.get(s).stateEntry(STATE,id.toString());}
    static void save(MinecraftServer s,UUID id,CompoundTag t){LabyrinthData.get(s).setStateEntry(STATE,id.toString(),t);}
    public static @Nullable Room kind(LabyrinthPlace place){for(var room:Room.values())if(room.place()==place)return room;return null;}
    public static boolean added139(LabyrinthPlace place){return place==LabyrinthPlace.READ_WAITING||place==LabyrinthPlace.READ_BATHROOM||place==LabyrinthPlace.READ_SHELTER;}
    public static @Nullable BlockPos base(MinecraftServer s,UUID id,Room room){var own=state(s,id);var origin=HouseSavedData.get(s).houseOrigin();return origin==null||!own.contains("Index")?null:new BlockPos(origin.getX()+8192+own.getInt("Index")*4096,80,origin.getZ()-room.offsetZ);}
    public static @Nullable Room inside(ServerPlayer p){var own=state(p.server,p.getUUID());for(var room:Room.values()){var b=base(p.server,p.getUUID(),room);if(b!=null&&own.getCompound(room.key()).getBoolean("Built")&&p.level().dimension().equals(room.dimension())&&HouseReadRooms.bounds(b,room).contains(p.position()))return room;}return null;}
    /** Native room bounds also shelter an observer; ownership still gates every interaction. */
    public static boolean contains(ServerPlayer p){
        for(String key:LabyrinthData.get(p.server).state(STATE).getAllKeys())try{
            var id=UUID.fromString(key);var own=state(p.server,id);
            for(var room:Room.values())if(p.level().dimension().equals(room.dimension())&&own.getCompound(room.key()).getBoolean("Built")){
                var b=base(p.server,id,room);if(b!=null&&HouseReadRooms.bounds(b,room).contains(p.position()))return true;
            }
        }catch(IllegalArgumentException ignored){}
        return false;
    }
    public static List<AABB> areas(MinecraftServer s,boolean outside){var out=new ArrayList<AABB>();for(String key:LabyrinthData.get(s).state(STATE).getAllKeys())try{var id=UUID.fromString(key);for(var room:Room.values())if((room==Room.PORCH)==outside){var b=base(s,id,room);if(b!=null)out.add(HouseReadRooms.bounds(b,room));}}catch(IllegalArgumentException ignored){}return out;}
    public static boolean protectedPosition(MinecraftServer s,BlockPos at,boolean outside){return areas(s,outside).stream().anyMatch(a->a.contains(Vec3.atCenterOf(at)));}
    public static @Nullable Room next(MinecraftServer s,UUID id){var t=state(s,id);for(var room:CHAIN)if(!t.getCompound(room.key()).getBoolean("Entered"))return room;return null;}
    public static @Nullable LabyrinthData.Door prepare(ServerPlayer p,Room room){
        var d=LabyrinthData.get(p.server);if(store!=d){rebind();store=d;}var own=state(p.server,p.getUUID());
        if(!own.contains("Index")){var world=d.stateEntry(STATE,"World");int index=world.getInt("NextIndex");if(index>=4096)return null;own.putInt("Index",index);world.putInt("NextIndex",index+1);d.setStateEntry(STATE,"World",world);save(p.server,p.getUUID(),own);}
        var stale=own.getCompound(room.key());if(stale.getBoolean("Built")&&!stale.getBoolean("Entered")&&stale.getInt("Layout")<LAYOUT){stale.remove("Built");own.put(room.key(),stale);save(p.server,p.getUUID(),own);}
        var b=base(p.server,p.getUUID(),room);var l=p.server.getLevel(room.dimension());
        if(l!=null&&own.getCompound(room.key()).getBoolean("Built")&&l.getChunkSource().getChunkNow(b.getX()>>4,b.getZ()>>4)!=null&&l.areEntitiesLoaded(ChunkPos.asLong(b)))return d.door(doorId(p.getUUID(),room,"entry"));
        var job=new Job(p.getUUID(),room);if(!job.equals(building))WAITING.add(job);return null;
    }
    /** Preparing ahead changes no route, visit, action or inventory. */
    public static void prepareNext(ServerPlayer p){if(!io.github.knaitoe.theoldesthouse.opening.OpeningSequence.state(p).enteredHouse()||!LiteraryVignettes.participant(p))return;var room=next(p.server,p.getUUID());if(room!=null)prepare(p,room);}
    private static String doorId(UUID id,Room room,String name){return "house_read/"+id+"/"+room.key()+"/"+name;}
    /**
     * 0.4.160, the owner's direction: the quiet rooms come one at a time. A reader's first junction door leads into the first;
     * it ends in the hall that door was dealt, and after that the next story the House deals them is the next quiet room
     * ({@link #owe}), until they have all been entered.
     */
    public static boolean offer(ServerPlayer p,LabyrinthData.Door from,@Nullable LabyrinthPlace target){
        if(from.command||!LabyrinthData.DEALT.equals(from.destination))return false;
        var d=LabyrinthData.get(p.server);var room=next(p.server,p.getUUID());var own=state(p.server,p.getUUID());
        boolean first=from.id.startsWith(LabyrinthPlace.JUNCTION.id()+"/")&&Arrays.stream(Room.values()).noneMatch(r->own.getCompound(r.key()).getBoolean("Entered"));
        boolean owed=own.getList("Owed",Tag.TAG_STRING).stream().anyMatch(t->t.getAsString().equals(from.id));
        if(!first&&!owed)return false;
        if(room==null||target==null||!LabyrinthPacing.ordinary(target)||!io.github.knaitoe.theoldesthouse.opening.OpeningSequence.state(p).enteredHouse()||!LiteraryVignettes.participant(p)||inside(p)!=null)return false;
        if(!own.contains("Index")&&d.stateEntry(STATE,"World").getInt("NextIndex")>=4096)return false;
        var request=new Request(from.id,target,room);PENDING.put(p.getUUID(),request);enter(p,request);return true;
    }
    /** The next quiet room a reader has not entered, from saved data alone (the dealer has no player). */
    public static @Nullable Room next(LabyrinthData d,UUID id){var t=d.stateEntry(STATE,id.toString());for(var room:CHAIN)if(!t.getCompound(room.key()).getBoolean("Entered"))return room;return null;}
    /**
     * Whether a reader has begun the quiet rooms at a junction and still has one to come; until they have entered them all,
     * the next takes the place of every story the House deals. A reader who never began (an observer, or someone who never
     * came in by the front door) is dealt as before.
     */
    public static boolean owing(LabyrinthData d,UUID id){return owing(d.stateEntry(STATE,id.toString()));}
    /** {@link #owing} from a reader's own quiet-room record, as saved. */
    static boolean owing(CompoundTag t){boolean begun=false,left=false;for(var r:Room.values()){if(t.getCompound(r.key()).getBoolean("Entered"))begun=true;else left=true;}return begun&&left;}
    /** Marks a dealt door as the way into the next quiet room; it still leads on to its hall afterwards. */
    public static void owe(LabyrinthData d,UUID id,String door){
        var own=d.stateEntry(STATE,id.toString());
        var list=own.getList("Owed",Tag.TAG_STRING);list.removeIf(t->t.getAsString().equals(door));list.add(StringTag.valueOf(door));while(list.size()>8)list.remove(0);
        own.put("Owed",list);own.remove("OwedDry");d.setStateEntry(STATE,id.toString(),own);
    }
    private static void enter(ServerPlayer p,Request request){
        // A room is reached from beside the door that was dealt into it.
        var d=LabyrinthData.get(p.server);var from=d.door(request.door());boolean away=from==null||p.level().dimension()!=from.dimension||p.distanceToSqr(from.lower.getCenter())>36;
        if(away||LabyrinthDoors.isBusy(p)){PENDING.remove(p.getUUID());return;}
        var doorway=prepare(p,request.room());if(doorway==null)return;PENDING.remove(p.getUUID());
        var room=request.room();var b=base(p.server,p.getUUID(),room);var landing=b.offset(room==Room.PORCH?new BlockPos(0,0,10):BlockPos.ZERO);
        HouseTransitionEvents.beginDoorTransition(p,room.dimension(),null,arrived->{
            var own=state(arrived.server,arrived.getUUID());if(!own.getString("Active").isEmpty()){left(arrived);own=state(arrived.server,arrived.getUUID());}
            // Entering one settles every door that was owed: the next is dealt afresh, after a hall.
            own.remove("Owed");
            boolean first=true,late=room==Room.KITCHEN;for(var other:Room.values())if(other!=room){if(own.getCompound(other.key()).getBoolean("Entered"))first=false;else late=false;}
            var visit=own.getCompound(room.key());visit.putBoolean("Entered",true);if(late)visit.putBoolean("Late",true);visit.putString("SourceDoor",from.id);visit.putString("NextPlace",request.next().id());visit.putInt("BeforeFrozen",arrived.getTicksFrozen());visit.putBoolean("Here",true);own.put(room.key(),visit);own.putString("Active",room.key());save(arrived.server,arrived.getUUID(),own);
            // 0.4.163: the last quiet room entered, the House's depth counts again from here.
            // 0.4.168: a reader who met the kitchen after the other five keeps the depth they had.
            if(next(arrived.server,arrived.getUUID())==null&&!late)LabyrinthData.get(arrived.server).holdReturns(arrived.getUUID());
            HouseReadStory.open(arrived,room,first);
            prepareNext(arrived);
        },Vec3.atBottomCenterOf(landing),180);
    }
    /** Registered private doors are owner-bound and are handled before ordinary Return logic. */
    public static boolean door(ServerPlayer p,LabyrinthData.Door door){
        if(HouseReadEntrance.room(door)!=null){p.displayClientMessage(Component.literal("The door is locked."),true);return true;}
        if(!door.id.startsWith("house_read/"))return false;var room=inside(p);if(room==null||!door.id.startsWith("house_read/"+p.getUUID()+"/"+room.key()+"/")||p.level().dimension()!=door.dimension||p.distanceToSqr(door.lower.getCenter())>36)return true;
        // 0.4.166 (owner: "The doors can also just be locked ... Until the vignettes are completed."): both ways stay shut until
        // the House has what the room is for.
        if(!HouseReadStory.ready(p,room)){HouseReadStory.locked(p,door.lower);return true;}
        boolean forward=!door.id.endsWith("/entry");{if(room==Room.PORCH&&forward){String out=door.id.endsWith("/kitchen")?"kitchen":door.id.endsWith("/side")?"gate":"road";HouseRead.first(p,Room.PORCH,"Out",out);act(p,room,switch(out){case "kitchen"->"left through the front hall";case "gate"->"left by the side gate";default->"left by the road through the trees";});}leave(p,forward);}return true;
    }
    /** Leaves a complete room: the House's closing line on black first, then {@link #depart}. */
    static void leave(ServerPlayer p,boolean forward){
        var room=inside(p);if(room==null||LabyrinthDoors.isBusy(p)||!HouseReadStory.ready(p,room))return;
        HouseReadStory.close(p,room,forward,next(p.server,p.getUUID())==null&&!state(p.server,p.getUUID()).getCompound(room.key()).getBoolean("Late"));
    }
    static void depart(ServerPlayer p,boolean forward){
        var room=inside(p);if(room==null||LabyrinthDoors.isBusy(p))return;var t=state(p.server,p.getUUID()).getCompound(room.key());var d=LabyrinthData.get(p.server);
        // A retired 0.4.142 hallway door still has a way back: the far door's hallway side.
        var from=d.door(t.getString("SourceDoor"));if(from==null)from=d.door("hallway_end");if(from==null)return;var next=LabyrinthPlace.byId(t.getString("NextPlace"));
        // 0.4.160: no room leads straight into the next; the way on is always the hall its door was dealt.
        if(forward&&!t.getBoolean("Entrance")){if(next!=null)LabyrinthDoors.continueFromRead(p,from,next);}
        else HouseTransitionEvents.beginDoorTransition(p,from.dimension,null,HouseReadSites::left,Vec3.atBottomCenterOf(from.lower.relative(from.facing,2)),from.facing.toYRot());
    }
    public static void left(ServerPlayer p){
        var own=state(p.server,p.getUUID());String key=own.getString("Active");if(key.isEmpty())return;var visit=own.getCompound(key);restore(p,visit);HouseReadMoments.left(p,Room.valueOf(key.toUpperCase(Locale.ROOT)),visit);p.setTicksFrozen(visit.getInt("BeforeFrozen"));visit.putBoolean("Here",false);own.put(key,visit);own.remove("Active");save(p.server,p.getUUID(),own);
        for(var sound:HouseReadRegistry.sounds())p.connection.send(new ClientboundStopSoundPacket(sound.getId(),SoundSource.BLOCKS));
    }
    static void act(ServerPlayer p,Room room,String action){var own=state(p.server,p.getUUID());var visit=own.getCompound(room.key());var actions=visit.getList("Actions",Tag.TAG_STRING);if(actions.size()<64)actions.add(StringTag.valueOf(action));visit.put("Actions",actions);if(room==Room.COATROOM&&(action.startsWith("wore ")||action.startsWith("turned ")||action.startsWith("draped ")))visit.putBoolean("NoteArmed",true);own.put(room.key(),visit);save(p.server,p.getUUID(),own);}
    public static boolean loan(ItemStack item,UUID owner){var data=item.get(DataComponents.CUSTOM_DATA);return data!=null&&data.copyTag().hasUUID("HouseReadLoan")&&data.copyTag().getUUID("HouseReadLoan").equals(owner);}
    private static void give(ServerPlayer p,ItemStack item){if(item.isEmpty())return;if(!p.getInventory().add(item)){var entity=new net.minecraft.world.entity.item.ItemEntity(p.serverLevel(),p.getX(),p.getY(),p.getZ(),item);entity.setTarget(p.getUUID());p.serverLevel().addFreshEntity(entity);}}
    private static void wear(ServerPlayer p,int kind){
        var own=state(p.server,p.getUUID());var visit=own.getCompound(Room.COATROOM.key());var items=new Item[]{HouseReadRegistry.WOOL.get(),HouseReadRegistry.TRENCH.get(),HouseReadRegistry.BARN.get(),HouseReadRegistry.WOOL_BOOTS.get(),HouseReadRegistry.TRENCH_BOOTS.get(),HouseReadRegistry.BARN_BOOTS.get()};var garment=(HouseReadRegistry.Garment)items[kind];var slot=garment.getEquipmentSlot();String key=slot==EquipmentSlot.CHEST?"Chest":"Feet";
        if(!visit.getBoolean("Captured"+key)){visit.putBoolean("Captured"+key,true);visit.put("Original"+key,p.getItemBySlot(slot).saveOptional(p.registryAccess()));}
        else if(!loan(p.getItemBySlot(slot),p.getUUID()))give(p,p.getItemBySlot(slot).copy());
        // A swapped loan is still the room's layer, never an additional take-home item.
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(loan(p.getInventory().getItem(i),p.getUUID())&&p.getInventory().getItem(i).getItem() instanceof HouseReadRegistry.Garment g&&g.getEquipmentSlot()==slot)p.getInventory().setItem(i,ItemStack.EMPTY);
        var base=base(p.server,p.getUUID(),Room.COATROOM);for(var peg:HouseReadRooms.PEGS)for(var pos:List.of(base.offset(peg),base.offset(peg.east()))){var state=p.serverLevel().getBlockState(pos);if(!state.is(HouseReadRegistry.FITTING.get()))continue;int part=state.getValue(HouseReadPropBlock.PART).ordinal();if(part<6&&(part<3)==(kind<3))p.serverLevel().setBlock(pos,state.setValue(HouseReadPropBlock.TURNED,part==kind),Block.UPDATE_CLIENTS);}
        var layer=new ItemStack(garment);CustomData.update(DataComponents.CUSTOM_DATA,layer,t->t.putUUID("HouseReadLoan",p.getUUID()));p.setItemSlot(slot,layer);p.setTicksFrozen(0);visit.putBoolean("Dressed",true);own.put(Room.COATROOM.key(),visit);save(p.server,p.getUUID(),own);p.inventoryMenu.broadcastChanges();act(p,Room.COATROOM,"wore "+garment.cloth.replace('_',' '));dressed(p,garment);
    }
    /** The coats: the first garment taken sets the reader's presentation; wearing two from different sets at once reads neither. */
    private static final String[] SETS={"wool","trench","barn"},SELVES={"MASC","FEM","ANDROGYNOUS"};
    private static int set(ItemStack item){return item.getItem() instanceof HouseReadRegistry.Garment g?List.of(SETS).indexOf(g.cloth.split("_")[0]):-1;}
    private static void dressed(ServerPlayer p,HouseReadRegistry.Garment garment){
        var seen=HouseRead.seen(LabyrinthData.get(p.server),p.getUUID()).getCompound(Room.COATROOM.key());int kind=List.of(SETS).indexOf(garment.cloth.split("_")[0]);if(kind<0)return;
        boolean chest=garment.getEquipmentSlot()==EquipmentSlot.CHEST;HouseRead.first(p,Room.COATROOM,chest?"Coat":"Boots",SETS[kind]);
        if(seen.contains("MirrorFirst"))return;
        if(!seen.contains("Took")){HouseRead.first(p,Room.COATROOM,"Took",SETS[kind]);HouseRead.signal(p,HouseRead.Trait.SELF,SELVES[kind],false);return;}
        int other=set(p.getItemBySlot(chest?EquipmentSlot.FEET:EquipmentSlot.CHEST));
        if(!seen.getBoolean("Mixed")&&!seen.getBoolean("Looked")&&!seen.contains("Mirror")&&other>=0&&other!=kind&&kind!=2&&other!=2){HouseRead.flag(p,Room.COATROOM,"Mixed");HouseRead.restart(p,HouseRead.Trait.SELF,"ANDROGYNOUS");}
    }
    /** The hall glass: turned or covered before dressing settles UNSEEN; after dressing it makes UNSEEN the House's first guess. */
    private static void glass(ServerPlayer p,boolean away,boolean cover,boolean dressedNow){
        if(!away)return;var seen=HouseRead.seen(LabyrinthData.get(p.server),p.getUUID()).getCompound(Room.COATROOM.key());if(seen.contains("Mirror"))return;
        if(!dressedNow){HouseRead.see(p,Room.COATROOM,t->{t.putString("Mirror",cover?"covered_before":"turned_before");t.putBoolean("MirrorFirst",true);});HouseRead.signal(p,HouseRead.Trait.SELF,"UNSEEN",false);HouseRead.signal(p,HouseRead.Trait.SELF,"UNSEEN",true);return;}
        HouseRead.see(p,Room.COATROOM,t->t.putString("Mirror",cover?"covered_after":"turned_after"));HouseRead.restart(p,HouseRead.Trait.SELF,"UNSEEN");
    }
    private static void looked(ServerPlayer p){
        var d=LabyrinthData.get(p.server);var seen=HouseRead.seen(d,p.getUUID()).getCompound(Room.COATROOM.key());if(seen.contains("Mirror")||seen.getBoolean("Looked"))return;
        HouseRead.see(p,Room.COATROOM,t->{t.putBoolean("Looked",true);t.putString("Mirror","looked");});var now=HouseRead.raw(d,p.getUUID(),HouseRead.Trait.SELF);if(!now.value().isEmpty()&&!now.value().equals("UNSEEN"))HouseRead.signal(p,HouseRead.Trait.SELF,now.value(),true);
    }
    private static void restore(ServerPlayer p,CompoundTag visit){
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(loan(p.getInventory().getItem(i),p.getUUID()))p.getInventory().setItem(i,ItemStack.EMPTY);
        if(loan(p.containerMenu.getCarried(),p.getUUID()))p.containerMenu.setCarried(ItemStack.EMPTY);
        // Other quiet rooms have no coat custody: do not read or generate their unbuilt coatroom.
        if(!visit.getBoolean("CapturedChest")&&!visit.getBoolean("CapturedFeet")){p.inventoryMenu.broadcastChanges();return;}
        for(var slot:List.of(EquipmentSlot.CHEST,EquipmentSlot.FEET)){String key=slot==EquipmentSlot.CHEST?"Chest":"Feet";if(!visit.getBoolean("Captured"+key))continue;var original=ItemStack.parseOptional(p.registryAccess(),visit.getCompound("Original"+key));var current=p.getItemBySlot(slot);if(!current.isEmpty())give(p,current.copy());p.setItemSlot(slot,original);visit.remove("Original"+key);visit.putBoolean("Captured"+key,false);}
        var base=base(p.server,p.getUUID(),Room.COATROOM);var level=p.server.getLevel(HouseDimensions.INTERIOR);
        if(base!=null&&level!=null)for(var peg:HouseReadRooms.PEGS)for(var pos:List.of(base.offset(peg),base.offset(peg.east()))){var state=level.getBlockState(pos);if(state.is(HouseReadRegistry.FITTING.get())&&state.getValue(HouseReadPropBlock.PART).ordinal()<6)level.setBlock(pos,state.setValue(HouseReadPropBlock.TURNED,false),Block.UPDATE_CLIENTS);}
        if(base!=null&&level!=null)for(var dropped:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,HouseReadRooms.bounds(base,Room.COATROOM),item->loan(item.getItem(),p.getUUID())))dropped.discard();
        p.inventoryMenu.broadcastChanges();
    }
    public static void use(ServerPlayer p,BlockPos at,HouseReadPropBlock.Part part){
        var room=inside(p);if(room==null)return;var b=base(p.server,p.getUUID(),room);if(!HouseReadRooms.bounds(b,room).contains(at.getCenter()))return;
        if(part==HouseReadPropBlock.Part.CHAIR||part==HouseReadPropBlock.Part.BENCH){HouseSitting.sit(p,at);return;}
        if(HouseReadMoments.use(p,b,room,at,part))return;
        // 0.4.160: the coatroom's towel hangs on a bar beside the glass; taking it covers the glass, as the loose towel did.
        if(room==Room.COATROOM&&part==HouseReadPropBlock.Part.TOWEL_BAR)part=p.serverLevel().getBlockState(at).getValue(HouseReadPropBlock.TURNED)?HouseReadPropBlock.Part.COVERED_MIRROR:HouseReadPropBlock.Part.TOWEL;
        if(room==Room.COATROOM&&part.ordinal()<=HouseReadPropBlock.Part.BARN_BOOTS.ordinal()){wear(p,part.ordinal());return;}
        if(part==HouseReadPropBlock.Part.CURTAIN){if(!HouseReadStory.ready(p,room)){p.displayClientMessage(Component.literal("The curtain is heavy and won't part."),true);return;}act(p,room,"passed through the curtain");leave(p,true);return;}
        if(room==Room.COATROOM&&(part==HouseReadPropBlock.Part.MIRROR||part==HouseReadPropBlock.Part.MIRROR_TOP||part==HouseReadPropBlock.Part.TOWEL||part==HouseReadPropBlock.Part.COVERED_MIRROR)){
            var lower=b.offset(HouseReadRooms.MIRROR);
            if(part==HouseReadPropBlock.Part.TOWEL||part==HouseReadPropBlock.Part.COVERED_MIRROR){boolean cover=part==HouseReadPropBlock.Part.TOWEL;glass(p,cover,true,state(p.server,p.getUUID()).getCompound(Room.COATROOM.key()).getBoolean("Dressed"));for(var pos:List.of(lower,lower.above())){var kind=cover?HouseReadPropBlock.Part.COVERED_MIRROR:pos.equals(lower)?HouseReadPropBlock.Part.MIRROR:HouseReadPropBlock.Part.MIRROR_TOP;p.serverLevel().setBlock(pos,p.serverLevel().getBlockState(pos).setValue(HouseReadPropBlock.PART,kind).setValue(HouseReadPropBlock.TURNED,false),Block.UPDATE_CLIENTS);}
                var bar=b.offset(HouseReadRooms.TOWEL_AT);var barState=p.serverLevel().getBlockState(bar);if(barState.is(HouseReadRegistry.FITTING.get())&&barState.getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.TOWEL_BAR)p.serverLevel().setBlock(bar,barState.setValue(HouseReadPropBlock.TURNED,cover),Block.UPDATE_CLIENTS);act(p,room,cover?"draped the towel over the mirror":"lifted the towel from the mirror");return;}
            boolean turned=!p.serverLevel().getBlockState(lower).getValue(HouseReadPropBlock.TURNED);glass(p,turned,false,state(p.server,p.getUUID()).getCompound(Room.COATROOM.key()).getBoolean("Dressed"));for(var pos:List.of(lower,lower.above()))p.serverLevel().setBlock(pos,p.serverLevel().getBlockState(pos).setValue(HouseReadPropBlock.TURNED,turned),Block.UPDATE_CLIENTS);act(p,room,turned?"turned the mirror to the wall":"turned the mirror back");return;
        }
        if(part==HouseReadPropBlock.Part.UMBRELLA||part==HouseReadPropBlock.Part.RAIL){if(p.serverLevel().getBlockState(at).getValue(HouseReadPropBlock.WRITING))HouseReadTexts.open(p,room);else p.displayClientMessage(Component.literal(part==HouseReadPropBlock.Part.UMBRELLA?"A folded slip, its blank side showing.":"Smooth wood, worn by hands."),true);return;}
        act(p,room,switch(part){case RADIATOR->"warmed hands at the radiator";case SWING->"nudged the swing";case MAILBOX->"looked in the empty mailbox";case PLATE->"found the plate kept warm";default->"looked around";});
    }
    private static void remember(CompoundTag visit,String key,String words){if(visit.getBoolean(key))return;visit.putBoolean(key,true);visit.putBoolean("NoteArmed",true);var actions=visit.getList("Actions",Tag.TAG_STRING);if(actions.size()<64)actions.add(StringTag.valueOf(words));visit.put("Actions",actions);}
    private static boolean watched(ServerPlayer p,BlockPos at){
        var target=at.getCenter();var delta=target.subtract(p.getEyePosition());if(delta.lengthSqr()>1024||p.getLookAngle().dot(delta.normalize())<.15)return false;
        var start=p.getEyePosition();for(int i=0;i<12;i++){var hit=p.serverLevel().clip(new net.minecraft.world.level.ClipContext(start,target,net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,p));if(hit.getType()==HitResult.Type.MISS||hit.getBlockPos().equals(at))return true;var block=p.serverLevel().getBlockState(hit.getBlockPos()).getBlock();if(!(block==Blocks.GLASS||block instanceof StainedGlassBlock)&&!(block instanceof IronBarsBlock)&&!(block instanceof HouseReadPropBlock))return false;start=hit.getLocation().add(delta.normalize().scale(.15));if(start.distanceToSqr(target)<.025)return true;}return false;
    }
    static void sound(ServerPlayer p,SoundEvent event,BlockPos at,float volume){LiteraryVignettes.heard(p,event,at,volume,1);}
    private static void reader(ServerPlayer p){
        var room=inside(p);HouseReadStory.tick(p,room);var own=state(p.server,p.getUUID());if(room==null){if(!own.getString("Active").isEmpty())left(p);return;}if(!LiteraryVignettes.participant(p)||LabyrinthDoors.isBusy(p)||HouseReadStory.holding(p))return;
        var b=base(p.server,p.getUUID(),room);var visit=own.getCompound(room.key());if(!visit.getBoolean("Entered"))return;int age=visit.getInt("Age")+1;visit.putInt("Age",age);
        if(room==Room.COATROOM){if(!visit.getBoolean("Dressed"))p.setTicksFrozen(100);if(age>=200)visit.putBoolean("NoteArmed",true);
            // 0.4.166: the curtain hangs shut until the House has what the room is for; then it parts.
            if(!visit.getBoolean("CurtainParted")&&HouseReadStory.ready(p,room)){visit.putBoolean("CurtainParted",true);for(int y=0;y<=1;y++){var at=b.offset(HouseReadRooms.CURTAIN_AT).above(y);var c=p.serverLevel().getBlockState(at);if(c.is(HouseReadRegistry.FITTING.get()))p.serverLevel().setBlock(at,c.setValue(HouseReadPropBlock.TURNED,true),Block.UPDATE_CLIENTS);}sound(p,SoundEvents.WOOL_PLACE,b.offset(HouseReadRooms.CURTAIN_AT),.5f);}
            // 0.4.150: standing at the uncovered, unturned glass in the borrowed layers for two seconds, looking into it.
            var glass=b.offset(HouseReadRooms.MIRROR);var glassState=p.serverLevel().getBlockState(glass);
            boolean showing=glassState.is(HouseReadRegistry.FITTING.get())&&glassState.getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.MIRROR&&!glassState.getValue(HouseReadPropBlock.TURNED);
            if(visit.getBoolean("Dressed")&&showing&&p.getZ()>glass.getZ()+.5&&p.getZ()<glass.getZ()+4.5&&Math.abs(p.getX()-glass.getX()-.5)<2&&watched(p,glass.above())){int look=visit.getInt("MirrorLook")+1;visit.putInt("MirrorLook",look);if(look>=40){remember(visit,"AtMirror","stood at the hall glass in the borrowed coat");if(look==40)looked(p);}}else visit.putInt("MirrorLook",0);if(age%6==0){p.serverLevel().sendParticles(p,ParticleTypes.FALLING_WATER,false,b.getX()-.5+p.getRandom().nextDouble()*2,b.getY()+3.6,b.getZ()+3.6,2,.3,.1,.2,0);p.serverLevel().sendParticles(p,ParticleTypes.FALLING_WATER,false,b.getX()+5.5,b.getY()+2.9,b.getZ()-3+p.getRandom().nextDouble()*2,1,.1,.1,.3,0);}if(age%120==1){sound(p,HouseReadRegistry.RAIN.get(),b.offset(0,1,3),.5f);sound(p,HouseReadRegistry.RADIATOR.get(),b.offset(3,0,0),.35f);}if(p.getZ()<b.getZ()-9.4){own.put(room.key(),visit);save(p.server,p.getUUID(),own);leave(p,true);return;}}
        else if(room==Room.PORCH)porch(p,b,visit,age);
        else if(room==Room.KITCHEN)HouseReadKitchen.tick(p,b,visit,age);
        else HouseReadMoments.tick(p,b,room,visit,age);
        var note=b.offset(HouseReadRooms.note(room));if(visit.getBoolean("NoteArmed")&&!visit.getBoolean("Written")&&!watched(p,note)&&p.containerMenu==p.inventoryMenu){var s=p.serverLevel().getBlockState(note);if(s.is(HouseReadRegistry.FITTING.get())){p.serverLevel().setBlock(note,s.setValue(HouseReadPropBlock.WRITING,true),Block.UPDATE_CLIENTS);visit.putBoolean("Written",true);}}
        own.put(room.key(),visit);save(p.server,p.getUUID(),own);
    }
    private static boolean underPorch(ServerPlayer p,BlockPos b){return p.getY()<b.getY()+1&&Math.abs(p.getX()-b.getX()-.5)<6.6&&p.getZ()>b.getZ()-8.5&&p.getZ()<b.getZ()-1.5;}
    private static boolean onRoad(ServerPlayer p,BlockPos b){return p.getX()>b.getX()+10&&p.getX()<b.getX()+17&&p.getZ()<b.getZ()-18;}
    /** The porch (0.4.159): the 200 ticks after the whistle are its window; what the reader does first in them sets HOME. */
    private static void porch(ServerPlayer p,BlockPos b,CompoundTag visit,int age){
        if(!visit.contains("Whistle")&&age>=visit.getInt("WhistleAgain")&&(age>=200||p.distanceToSqr(b.offset(-10,0,2).getCenter())<16)){visit.putInt("Whistle",age);sound(p,HouseReadRegistry.WHISTLE.get(),b.offset(0,4,-8),1.6f);}
        int cue=visit.getInt("Whistle");boolean window=cue>0&&age<=cue+200;var seen=HouseRead.seen(LabyrinthData.get(p.server),p.getUUID()).getCompound(Room.PORCH.key());boolean first=seen.contains("First");
        if(cue>0){
            if(underPorch(p,b)){remember(visit,"UnderLattice","stood under the porch behind the lattice");if(window&&!first){home(p,"lattice","DANGER");first=true;}}
            if(onRoad(p,b)){remember(visit,"ToRoad","walked down the gravel toward the road");if(window&&!first){home(p,"road","RUNAWAY");first=true;}}
            if(onRoad(p,b)&&seen.getString("First").equals("road")){var house=Vec3.atCenterOf(b.offset(0,5,-16)).subtract(p.getEyePosition()).normalize();int back=p.getLookAngle().dot(house)>.5?visit.getInt("LookBack")+1:0;visit.putInt("LookBack",back);if(back>=20)visit.putBoolean("TurnedBack",true);}
            if(p.getX()>b.getX()+10&&p.getX()<b.getX()+17&&p.getZ()<b.getZ()-27){remember(visit,"PastMailbox","walked past the mailbox into the trees");if(seen.getString("First").equals("road")&&!visit.getBoolean("TurnedBack")&&!seen.getBoolean("Trees")){HouseRead.flag(p,Room.PORCH,"Trees");HouseRead.signal(p,HouseRead.Trait.HOME,"RUNAWAY",true);}}
            // Stopping on the lawn to look in at the lit window, then not going in, is the other way of hiding.
            if(window&&p.getY()<b.getY()+1.5&&p.getZ()>b.getZ()-2&&p.pick(32,1,false) instanceof BlockHitResult sight&&sight.getType()==HitResult.Type.BLOCK&&sight.getBlockPos().getZ()==b.getZ()-8&&Set.of(-3,-2,3,4).contains(sight.getBlockPos().getX()-b.getX())&&p.serverLevel().getBlockState(sight.getBlockPos()).is(HouseReadRegistry.WINDOW.get())){int look=visit.getInt("LookIn")+1;visit.putInt("LookIn",look);if(look>=30){remember(visit,"LookedIn","looked through the lit window from the lawn");HouseRead.flag(p,Room.PORCH,"LookedIn");}}
            if(age==cue+200&&!first&&visit.getBoolean("LookedIn")&&!visit.getBoolean("WentIn")){home(p,"window","DANGER");first=true;}
            // 0.4.166: the House needs the answer. A whistle nobody answered is blown again, five seconds later.
            if(age==cue+200&&!first){visit.remove("Whistle");visit.putInt("WhistleAgain",age+100);}
            if(p.isPassenger()&&p.getZ()<b.getZ()-15&&p.getZ()>b.getZ()-20){remember(visit,"AtTable","sat at the warm table");if(seen.getString("First").equals("in")&&!seen.getBoolean("Table")){HouseRead.flag(p,Room.PORCH,"Table");HouseRead.signal(p,HouseRead.Trait.HOME,"HAVEN",true);}}
        }
        if(cue>0&&age==cue+26)sound(p,HouseReadRegistry.SUPPER.get(),b.offset(1,4,-11),1.8f);if(cue>0&&age==cue+190)sound(p,HouseReadRegistry.STEP.get(),b.offset(1,3,-13),1f);
        if(age%240==1)sound(p,HouseReadRegistry.NEWS.get(),b.offset(4,4,-15),.5f);if(age%160==1)sound(p,HouseReadRegistry.DISHES.get(),b.offset(-3,4,-12),.7f);if(age==90)sound(p,HouseReadRegistry.ICE.get(),b.offset(-3,4,-11),.7f);
        if(cue>0&&age>=cue+200){visit.putBoolean("NoteArmed",true);if(!visit.getBoolean("LightsOut")&&underPorch(p,b)){visit.putBoolean("LightsOut",true);for(var light:HouseReadRooms.PORCH_LIGHTS){var lamp=p.serverLevel().getBlockState(b.offset(light));if(lamp.is(HouseReadRegistry.FITTING.get()))p.serverLevel().setBlock(b.offset(light),lamp.setValue(HouseReadPropBlock.TURNED,true),Block.UPDATE_CLIENTS);}for(var pos:List.of(b.offset(HouseReadRooms.SCREEN),b.offset(HouseReadRooms.SCREEN).above())){var door=p.serverLevel().getBlockState(pos);if(door.getBlock() instanceof DoorBlock)p.serverLevel().setBlock(pos,door.setValue(DoorBlock.OPEN,false),Block.UPDATE_CLIENTS);}sound(p,SoundEvents.WOODEN_DOOR_CLOSE,b.offset(HouseReadRooms.SCREEN),.5f);
            // Still hidden when the kitchen light goes off settles it.
            if(seen.getString("First").equals("lattice")||seen.getString("First").equals("window")){HouseRead.flag(p,Room.PORCH,"Hidden");HouseRead.signal(p,HouseRead.Trait.HOME,"DANGER",true);}}}
        if(!visit.getBoolean("WentIn")&&p.getZ()<b.getZ()-8&&Math.abs(p.getX()-b.getX())<5&&p.getY()>=b.getY()+2.8){visit.putBoolean("WentIn",true);visit.putBoolean("NoteArmed",true);var actions=visit.getList("Actions",Tag.TAG_STRING);actions.add(StringTag.valueOf("went through the screen door"));visit.put("Actions",actions);if(window&&!first){home(p,"in","HAVEN");first=true;}}
        if(age%13==0)p.serverLevel().sendParticles(p,ParticleTypes.GLOW,false,b.getX()-8+p.getRandom().nextDouble()*16,b.getY()+.7+p.getRandom().nextDouble()*1.3,b.getZ()+p.getRandom().nextDouble()*8,1,0,0,0,0);
    }
    private static void home(ServerPlayer p,String first,String value){HouseRead.first(p,Room.PORCH,"First",first);HouseRead.signal(p,HouseRead.Trait.HOME,value,false);}
    @SubscribeEvent public static void screen(PlayerInteractEvent.RightClickBlock e){if(e.getLevel().isClientSide||e.getHand()!=net.minecraft.world.InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p)||!LiteraryVignettes.participant(p)||inside(p)!=Room.PORCH)return;var b=base(p.server,p.getUUID(),Room.PORCH);if(e.getPos().equals(b.offset(HouseReadRooms.SCREEN))||e.getPos().equals(b.offset(HouseReadRooms.SCREEN).above()))act(p,Room.PORCH,"used the screen door");}
    @SubscribeEvent public static void harmless(LivingIncomingDamageEvent e){if(e.getEntity() instanceof ServerPlayer p&&inside(p)!=null)e.setCanceled(true);}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){advance(e.getServer());if(e.getServer().getTickCount()%100==0)HouseReadEntrance.retire(e.getServer());for(var id:List.copyOf(PENDING.keySet())){var p=e.getServer().getPlayerList().getPlayer(id);if(p==null)PENDING.remove(id);else enter(p,PENDING.get(id));}for(var p:List.copyOf(e.getServer().getPlayerList().getPlayers()))reader(p);}
    public static void advance(MinecraftServer s){
        var d=LabyrinthData.get(s);if(store!=d){rebind();store=d;}while(building==null&&!WAITING.isEmpty()){var job=WAITING.iterator().next();WAITING.remove(job);if(s.getPlayerList().getPlayer(job.owner())!=null)building=job;}if(building==null)return;
        var p=s.getPlayerList().getPlayer(building.owner());var b=base(s,building.owner(),building.room());var l=s.getLevel(building.room().dimension());if(p==null||b==null||l==null){release();return;}var own=state(s,building.owner());var visit=own.getCompound(building.room().key());
        if(CHUNKS.isEmpty()){leased=l;var area=HouseReadRooms.bounds(b,building.room()).inflate(2);for(int x=((int)area.minX)>>4;x<=((int)area.maxX)>>4;x++)for(int z=((int)area.minZ)>>4;z<=((int)area.maxZ)>>4;z++){var c=new ChunkPos(x,z);l.getChunkSource().addRegionTicket(TICKET,c,2,c);CHUNKS.add(c);}}
        if(CHUNKS.stream().anyMatch(c->l.getChunkSource().getChunkNow(c.x,c.z)==null||!l.areEntitiesLoaded(c.toLong())))return;
        if(visit.getBoolean("Built")){release();return;}if(plan==null){plan=BuildBlocks.record(l,()->HouseReadRooms.build(l,b,building.room(),building.owner()));plan.resume(visit.getCompound("BuildCursor"));}
        if(!plan.tick()){visit.put("BuildCursor",plan.checkpoint());own.put(building.room().key(),visit);save(s,building.owner(),own);return;}
        registerDoors(s,building.owner(),building.room(),b);visit.putBoolean("Built",true);visit.putInt("Layout",LAYOUT);visit.remove("BuildCursor");own.put(building.room().key(),visit);save(s,building.owner(),own);release();
    }
    public static void registerDoors(MinecraftServer s,UUID owner,Room room,BlockPos b){var d=LabyrinthData.get(s);var dim=room.dimension();d.putDoor(new LabyrinthData.Door(doorId(owner,room,"entry"),dim,b.offset(HouseReadRooms.entry(room)),Direction.SOUTH,"house_read:back",false));if(room!=Room.COATROOM&&room!=Room.PORCH)d.putDoor(new LabyrinthData.Door(doorId(owner,room,"on"),dim,b.offset(room==Room.KITCHEN?HouseReadKitchen.EXIT:HouseReadMoments.exit(room)),Direction.SOUTH,"house_read:on",false));if(room==Room.PORCH){d.putDoor(new LabyrinthData.Door(doorId(owner,room,"kitchen"),dim,b.offset(0,3,-24),Direction.SOUTH,"house_read:on",false));d.putDoor(new LabyrinthData.Door(doorId(owner,room,"side"),dim,b.offset(-15,0,-8),Direction.EAST,"house_read:on",false));d.putDoor(new LabyrinthData.Door(doorId(owner,room,"road"),dim,b.offset(14,0,-42),Direction.SOUTH,"house_read:on",false));}}
    private static void release(){if(leased!=null)for(var c:CHUNKS)leased.getChunkSource().removeRegionTicket(TICKET,c,2,c);CHUNKS.clear();leased=null;building=null;plan=null;}
    public static void clear(){rebind();HouseReadStory.clear();}
    /** A new saved-data store forgets the build queue; a card already on a reader's screen still runs its course. */
    private static void rebind(){HouseReadMoments.clear();release();WAITING.clear();PENDING.clear();store=null;}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clear();}
}
