package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Copies of private scenes (owner, October 28; docs/SCENE_COPIES_0_4_153.md). A private scene stands once in the House; a
 * reader who would find it held by someone else gets a copy of their own. Copies stand a million blocks west of the House,
 * built in bounded slices by the scene's own builder and dressed by the same passes as the original, run against a shifted
 * origin so that their once-only checkpoints are the copy's own. A reader keeps the copy they entered until they finish its
 * story, so whatever they changed in it is still there when they come back; then it can be another reader's. The House
 * builds the next copy ahead, and keeps no more copies of a scene than the server's {@code sceneCopies} setting.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SceneInstances {
    public static final String STATE="scene_copies_0153";
    /** Copies stand this far west of their original, and each further copy of a scene this much further on. Both are wider than the whole slot grid, so no copy overlaps another scene. */
    public static final int WEST=1<<20,STEP=1<<16;
    /**
     * The private scenes that have copies. Each takes its place from the reader. 0.4.154: the Whale institute and the phone
     * canoe; 0.4.155: Harrigan, whose room keeps its own story in each copy; 0.4.156: the lake house, two readers to a copy.
     * 0.4.513: and Bike night, which is not private at all: six readers to a town ({@link #crowd}). 0.4.547: and the Pell house, two
     * readers to a house ({@link #pell}). 0.4.551: and every story written for a party ({@link #PARTIES}). 0.4.552: and the Goatman's trailer,
     * whose night is kept by copy (GoatmanVignette.run). 0.4.553: and the Ghosts Set, whose lead is kept by copy (GhostsCrew).
     * 0.4.554: and the Shallows, whose recollections and boys are kept by bank (Shallows.COPY). 0.4.555: and three of the yard's stories and
     * the arcade, whose suppers, fields, days and games were already kept by where they stand. 0.4.556: and Last orders, whose evenings were too.
     * 0.4.557: and the séance, whose sitting is kept by copy (ClassicsVignettes.world).
     */
    private static final Set<LabyrinthPlace> COPYABLE=EnumSet.of(LabyrinthPlace.CONFESSION,LabyrinthPlace.HOLY_RABBIT,LabyrinthPlace.WHEEL,LabyrinthPlace.SECOND_WAITRESS,LabyrinthPlace.MISSING_SLEEP,LabyrinthPlace.WHALE,LabyrinthPlace.PHONE_CANOE,LabyrinthPlace.HARRIGAN,LabyrinthPlace.LAKE_HOUSE,LabyrinthPlace.HIDE_AND_CLAP,LabyrinthPlace.DARE_IN_THE_DARK,LabyrinthPlace.WINCHESTER,LabyrinthPlace.WHISTLERS,LabyrinthPlace.WORK_PRINT,LabyrinthPlace.POLAROID_TRAIL,LabyrinthPlace.LAST_SWIMMER,LabyrinthPlace.PEOPLE_LIGHTS,LabyrinthPlace.PLACE_FOR_YOU,LabyrinthPlace.SECOND_PERSON,LabyrinthPlace.VACANT_POSSESSION,LabyrinthPlace.BIKE_NIGHT,LabyrinthPlace.PELL_HOUSE,
        LabyrinthPlace.LAST_CROSSING,LabyrinthPlace.PINEHOOK,LabyrinthPlace.ONES_INSIDE,LabyrinthPlace.GOATMAN,LabyrinthPlace.GHOSTS_SET,LabyrinthPlace.SHALLOWS,
        LabyrinthPlace.MARSH_SUPPER,LabyrinthPlace.TALL_GRASS,LabyrinthPlace.GROWING_THINGS,LabyrinthPlace.POLYBIUS,LabyrinthPlace.LAST_ORDERS,LabyrinthPlace.SEANCE);
    /**
     * 0.4.551 (owner, 13 November: "All multiplayer vignettes should be hooked through that system. If full"): the stories written
     * for a party, and how many readers one copy of each holds. A copy whose places are all held ({@link #held}) is full: the next
     * reader through the door goes into another, made ready for them as Bike night's second town is ({@link #crowd}). Bike night six
     * to a town (0.4.513); The Last Crossing a crew of four to six; Pinehook two to six; The Ones Already Inside six to a house, the
     * seventh to a house of their own. A copy with a place left still takes a latecomer, who comes in partway as each story has
     * always let them. The lake house and the Pell house keep their pairs ({@link #lake}, {@link #pell}). 0.4.552: the Goatman's
     * trailer sixteen, its own limit on an evening's children; a latecomer after supper is still turned away by the trailer's own rule.
     * 0.4.553: the Ghosts Set four cameras, the lead's and three second cameras; a fifth leads a production of their own on another set.
     * 0.4.554: the Shallows six, two hands to each of the bank's three carry places; a bank is also full to a reader who could neither
     * join a recollection there nor find a carry place free ({@link #fits}).
     * 0.4.555: The Supper five, one to each chair laid for a guest; In the Tall Grass six, a carload for the wagon on the shoulder; Growing
     * Things four, a pair of hands at each end of the kitchen table and two more; Polybius four, Block Party's four seats. 0.4.556: Last orders
     * five, one to each stool along the bar but Benny's. 0.4.557: the séance four, an unseen guest for each of the family at the table.
     */
    private static final Map<LabyrinthPlace,Integer> PARTIES=Map.ofEntries(Map.entry(LabyrinthPlace.BIKE_NIGHT,BikeNight.RIDERS),Map.entry(LabyrinthPlace.LAST_CROSSING,LastCrossing.MAX_CREW),
        Map.entry(LabyrinthPlace.PINEHOOK,Pinehook.MAX_PARTY),Map.entry(LabyrinthPlace.ONES_INSIDE,LodgingHouse.PARTY),Map.entry(LabyrinthPlace.GOATMAN,GoatmanVignette.MAX_PLAYERS),
        Map.entry(LabyrinthPlace.GHOSTS_SET,GhostsCrew.CAMERAS),Map.entry(LabyrinthPlace.SHALLOWS,Shallows.PLACES),
        Map.entry(LabyrinthPlace.MARSH_SUPPER,MarshSupper.PLACES),Map.entry(LabyrinthPlace.TALL_GRASS,TallGrass.PLACES),Map.entry(LabyrinthPlace.GROWING_THINGS,GrowingThings.PLACES),
        Map.entry(LabyrinthPlace.POLYBIUS,Polybius.PLACES),Map.entry(LabyrinthPlace.LAST_ORDERS,LastOrders.PLACES),Map.entry(LabyrinthPlace.SEANCE,ClassicsVignettes.PLACES));
    /** How many readers one copy of a party's story holds, or 0 for a scene that is not one. */
    public static int party(@Nullable LabyrinthPlace p){return p==null?0:PARTIES.getOrDefault(p,0);}
    /**
     * The readers copy {@code k}'s story keeps a place for, whether or not they are in it now: The Last Crossing's crew on the roll,
     * Pinehook's roll, the children of an evening at the Goatman's trailer while anyone is still there.
     */
    private static Set<UUID> roll(MinecraftServer s,LabyrinthPlace place,int k){
        var b=base(s,place,k);if(b==null)return Set.of();
        return switch(place){
            case LAST_CROSSING->LastCrossing.places(b);
            case PINEHOOK->Pinehook.places(b);
            case GOATMAN->GoatmanVignette.places(s,k);
            default->Set.of();
        };
    }
    /**
     * How many of copy {@code k}'s places are held by readers other than this one: whoever is in it now, and whoever its story keeps a
     * place for though they have stepped out to the House. One of the crew who goes out and comes back finds their place still theirs.
     */
    public static int held(MinecraftServer s,LabyrinthPlace place,int k,UUID reader){
        var ids=new HashSet<UUID>(roll(s,place,k));
        for(var q:s.getPlayerList().getPlayers())if(q.isAlive()&&!q.isSpectator()&&at(s,place,q.level().dimension(),q.position())==k)ids.add(q.getUUID());
        ids.remove(reader);
        return ids.size();
    }
    /** 0.4.554: whether copy {@code k} has room for this reader: a place not held, and for the Shallows a recollection to join or a carry place free. */
    private static boolean fits(MinecraftServer s,LabyrinthPlace place,int k,UUID reader,int most){
        return held(s,place,k,reader)<most&&(place!=LabyrinthPlace.SHALLOWS||Shallows.room(s,k,reader));
    }
    private static final TicketType<ChunkPos> TICKET=TicketType.create("scene_copy",Comparator.comparingLong(ChunkPos::toLong));
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    /** Columns of an interior copy's slot filled solid per tick, before its rooms are carved into it, as the original's are. */
    private static final int FILL_COLUMNS=48;
    private SceneInstances(){}

    public static boolean copyable(@Nullable LabyrinthPlace p){return p!=null&&COPYABLE.contains(p);}
    public static Set<LabyrinthPlace> copyable(){return Collections.unmodifiableSet(COPYABLE);}
    private static @Nullable Integer fixtureCap;
    public static int cap(){return fixtureCap!=null?fixtureCap:HouseConfig.setting(HouseConfig.SCENE_COPIES);}
    /** Fixtures only: the cap a server setting would give, or null for the setting. */
    public static void fixtureCap(@Nullable Integer cap){fixtureCap=cap;}

    // ------------------------------------------------------------------------------------------------ where copies stand
    /** The House origin moved so that a scene's own place falls on its copy: everything keyed by origin and place then reaches the copy. */
    public static BlockPos shiftedOrigin(BlockPos origin,int copy){return copy<=0?origin:origin.offset(-WEST-(copy-1)*STEP,0,0);}
    /** 0.4.553: the House origin moved to the copy whose band holds a position, or the origin itself outside every band, for the client. */
    public static BlockPos originAt(BlockPos origin,BlockPos pos){
        long west=(long)origin.getX()-WEST-pos.getX();if(west<-STEP/2)return origin;
        long k=Math.floorDiv(west+STEP/2,(long)STEP)+1;return k<1||k>64?origin:shiftedOrigin(origin,(int)k);
    }
    /** Copy 0 is the original. */
    public static @Nullable BlockPos base(MinecraftServer s,LabyrinthPlace p,int copy){
        var o=HouseSavedData.get(s).houseOrigin();return o==null?null:LabyrinthPlaces.base(shiftedOrigin(o,copy),p);
    }
    /** Which copy of a scene stands at a base: 0 for the original or anything else. */
    public static int copyAt(MinecraftServer s,LabyrinthPlace p,BlockPos base){
        var original=base(s,p,0);if(original==null||base.equals(original)||base.getY()!=original.getY()||base.getZ()!=original.getZ())return 0;
        long west=(long)original.getX()-base.getX()-WEST;return west<0||west%STEP!=0?0:(int)(west/STEP)+1;
    }
    /** The original and every copy that stands, for the passes that walk each scene's ground. */
    public static List<BlockPos> bases(MinecraftServer s,LabyrinthPlace p){
        var out=new ArrayList<BlockPos>();var original=base(s,p,0);if(original!=null)out.add(original);
        for(var a:areas(s))if(a.place==p)out.add(a.base);return out;
    }

    // ------------------------------------------------------------------------------------------------ saved copies and keepers
    private static CompoundTag scene(LabyrinthData d,LabyrinthPlace p){return d.stateEntry(STATE,p.id());}
    private static void scene(LabyrinthData d,LabyrinthPlace p,CompoundTag t){d.setStateEntry(STATE,p.id(),t);version++;}
    public static int copies(LabyrinthData d,LabyrinthPlace p){return scene(d,p).getInt("Copies");}
    private static CompoundTag copy(LabyrinthData d,LabyrinthPlace p,int k){return scene(d,p).getCompound("C"+k);}
    private static void copy(LabyrinthData d,LabyrinthPlace p,int k,CompoundTag t){var all=scene(d,p);all.put("C"+k,t);scene(d,p,all);}
    public static boolean built(LabyrinthData d,LabyrinthPlace p,int k){return k==0?LabyrinthBuilder.isPlaceReady(d,p):k<=copies(d,p)&&copy(d,p,k).getBoolean("Built");}
    /** The copy a reader keeps, or -1. */
    public static int kept(LabyrinthData d,UUID reader,LabyrinthPlace p){var b=scene(d,p).getCompound("Kept");return b.contains(reader.toString())?b.getInt(reader.toString()):-1;}
    private static void keep(LabyrinthData d,UUID reader,LabyrinthPlace p,int k){var all=scene(d,p);var b=all.getCompound("Kept");b.putInt(reader.toString(),k);all.put("Kept",b);scene(d,p,all);}
    /** A reader keeps their copy while its story can still be dealt to them. */
    private static boolean unfinished(LabyrinthData d,UUID reader,LabyrinthPlace p){
        if(p==LabyrinthPlace.PHONE_CANOE)return PhoneCanoe.canDeal(d,reader);
        if(p==LabyrinthPlace.HARRIGAN)return !WitnessAccount.has(d,reader,WitnessAccount.Story.HARRIGAN);
        if(p==LabyrinthPlace.HIDE_AND_CLAP)return HideAndClap.canDeal(d,reader);
        if(p==LabyrinthPlace.GOATMAN)return GoatmanVignette.canDeal(d,reader);
        if(p==LabyrinthPlace.SHALLOWS)return IndianLakeProgress.canDealShallows(d,reader);
        if(p==LabyrinthPlace.SEANCE)return !WitnessAccount.has(d,reader,WitnessAccount.Story.SEANCE);
        if(NovelVignettes.isNovel(p))return NovelVignettes.canDeal(d,reader,p);
        return LiteraryVignettes.canDeal(d,reader,p);
    }
    /** Every reader with a saved part in a scene, from the scene's own store. */
    private static Set<String> readers(LabyrinthData d,LabyrinthPlace p){
        if(p==LabyrinthPlace.PHONE_CANOE)return d.state(PhoneCanoe.ID).getAllKeys();
        if(p==LabyrinthPlace.HARRIGAN)return d.state(HarriganVignette.ID).getCompound("Players").getAllKeys();
        // The clapping game kept one round for the world: only a reader in the middle of it has a part to keep.
        if(p==LabyrinthPlace.HIDE_AND_CLAP){var round=d.state(HideAndClap.ID).getCompound("Session");return round.hasUUID("Player")?Set.of(round.getUUID("Player").toString()):Set.of();}
        // The trailer's evening was one for the world: the children of the evening it holds keep it.
        if(p==LabyrinthPlace.GOATMAN)return GoatmanVignette.run(d).getCompound("Cohort").getAllKeys();
        if(p==LabyrinthPlace.SHALLOWS)return d.state(Shallows.ID).getCompound("Players").getAllKeys();
        // The séance keeps each reader's record beside the original sitting's own fields, under the reader's id.
        if(p==LabyrinthPlace.SEANCE){var out=new HashSet<String>();for(var key:d.stateKeys(ClassicsVignettes.STATE))try{UUID.fromString(key);out.add(key);}catch(IllegalArgumentException ignored){}return out;}
        if(NovelVignettes.isNovel(p))return d.state(NovelVignettes.STATE).getAllKeys();
        return LiteraryVignettes.readers(d,p);
    }
    /** Someone other than {@code reader} keeps copy {@code k} for a story they have not finished. */
    private static boolean keptByAnother(LabyrinthData d,LabyrinthPlace p,int k,UUID reader){
        // Harrigan's original room keeps the story the world has already told; once it is over, new readers are given copies.
        if(p==LabyrinthPlace.HARRIGAN&&k==0&&d.isCompleted(HarriganVignette.ID))return true;
        // So does the clapping game's original room once its game is won: its open wardrobe is the world's.
        if(p==LabyrinthPlace.HIDE_AND_CLAP&&k==0&&d.isCompleted(HideAndClap.ID))return true;
        var b=scene(d,p).getCompound("Kept");
        for(var key:b.getAllKeys()){if(b.getInt(key)!=k||key.equals(reader.toString()))continue;try{if(unfinished(d,UUID.fromString(key),p))return true;}catch(IllegalArgumentException ignored){}}
        return false;
    }
    /** Readers already partway through a scene before it had copies keep the original, which still holds what they did there. */
    private static void settle(LabyrinthData d,LabyrinthPlace p){
        var all=scene(d,p);if(all.getBoolean("Settled"))return;var b=all.getCompound("Kept");
        for(var key:readers(d,p)){if(b.contains(key))continue;try{var id=UUID.fromString(key);if(d.visited(id).contains(p.id()))b.putInt(key,0);}catch(IllegalArgumentException ignored){}}
        all.put("Kept",b);all.putBoolean("Settled",true);scene(d,p,all);
    }

    // ------------------------------------------------------------------------------------------------ which copy a position is in
    private record Area(LabyrinthPlace place,int copy,BlockPos base,ResourceKey<Level> dimension,BoundingBox slot,AABB room){}
    private static List<Area> areas=List.of();private static LabyrinthData cached;private static int version,cachedVersion=-1;
    private static List<Area> areas(MinecraftServer s){
        var d=LabyrinthData.get(s);
        if(d!=cached||cachedVersion!=version){
            cached=d;cachedVersion=version;var out=new ArrayList<Area>();var o=HouseSavedData.get(s).houseOrigin();
            if(o!=null)for(var p:COPYABLE)for(int k=1;k<=copies(d,p);k++){
                if(!copy(d,p,k).getBoolean("Built"))continue;var shifted=shiftedOrigin(o,k);var b=LabyrinthPlaces.base(shifted,p);var slot=LabyrinthPlaces.slotBounds(shifted,p);
                if(b!=null&&slot!=null)out.add(new Area(p,k,b,NovelRooms.dimension(p),slot,IndianLakeRooms.bounds(b,p)));
            }
            areas=List.copyOf(out);
        }
        return areas;
    }
    /**
     * The scene whose copy's slot holds a position, for {@link LabyrinthPlaces#placeAt}. Copies stand at fixed offsets, so
     * this needs only the origin and the position: the client asks it too, for the scene's sky and view.
     */
    public static @Nullable LabyrinthPlace placeAt(BlockPos origin,BlockPos pos){
        long west=(long)origin.getX()-WEST-pos.getX();if(west<-STEP/2)return null;
        long k=Math.floorDiv(west+STEP/2,(long)STEP)+1;if(k<1||k>64)return null;
        var shifted=shiftedOrigin(origin,(int)k);
        // The lake house stands at its own height rather than its slot's layer, as its original does.
        var lake=LabyrinthPlaces.base(shifted,LabyrinthPlace.LAKE_HOUSE);
        if(lake!=null&&IndianLakeRooms.bounds(lake,LabyrinthPlace.LAKE_HOUSE).contains(Vec3.atCenterOf(pos)))return LabyrinthPlace.LAKE_HOUSE;
        for(var p:COPYABLE){if(p==LabyrinthPlace.LAKE_HOUSE)continue;var slot=LabyrinthPlaces.reach(shifted,p);if(slot!=null&&slot.isInside(pos))return p;}
        return null;
    }
    /** Which copy of a scene a position is in: 0 for the original, -1 for neither. */
    public static int at(MinecraftServer s,LabyrinthPlace p,ResourceKey<Level> dimension,Vec3 pos){
        if(!dimension.equals(NovelRooms.dimension(p)))return -1;
        var original=base(s,p,0);if(original!=null&&IndianLakeRooms.bounds(original,p).contains(pos))return 0;
        for(var a:areas(s))if(a.place==p&&a.room.contains(pos))return a.copy;
        return -1;
    }
    /** The copy this reader stands in, else the one they keep, else the original. */
    public static int of(ServerPlayer p,LabyrinthPlace place){
        int k=at(p.server,place,p.level().dimension(),p.position());if(k>=0)return k;
        return Math.max(0,kept(LabyrinthData.get(p.server),p.getUUID(),place));
    }
    public static @Nullable BlockPos base(ServerPlayer p,LabyrinthPlace place){return base(p.server,place,of(p,place));}
    /** Whether a living player other than {@code reader} (or the other half of their pair, at the lake or the Pell house) is in copy {@code k}. Spectators hold nothing. */
    private static boolean occupied(MinecraftServer s,LabyrinthPlace p,int k,UUID reader){
        var pair=pairOf(LabyrinthData.get(s),p,reader);
        for(var q:s.getPlayerList().getPlayers())if(!q.getUUID().equals(reader)&&!q.getUUID().equals(pair)&&q.isAlive()&&!q.isSpectator()&&at(s,p,q.level().dimension(),q.position())==k)return true;
        return false;
    }

    // ------------------------------------------------------------------------------------------------ doors
    /** A copy's doors carry its number after the original's id, so everything that reads a door's scene from its id still reads it. */
    public static String doorId(LabyrinthPlace p,int copy,String name){return copy<=0?p.id()+"/"+name:p.id()+"/"+name+"@"+copy;}
    /** The original door a copy's door stands for. */
    public static String plain(String id){int at=id.lastIndexOf('@');return at<0?id:id.substring(0,at);}
    public static int copyOf(String id){int at=id.lastIndexOf('@');if(at<0)return 0;try{return Integer.parseInt(id.substring(at+1));}catch(NumberFormatException e){return 0;}}
    public static @Nullable LabyrinthData.Door entry(LabyrinthData d,LabyrinthPlace p,int copy){return d.door(doorId(p,copy,"entry"));}
    /** The entry of the copy a reader stands in; the original's otherwise. */
    public static @Nullable LabyrinthData.Door entryAt(ServerPlayer p,LabyrinthPlace place){
        int k=copyable(place)?at(p.server,place,p.level().dimension(),p.position()):0;return entry(LabyrinthData.get(p.server),place,Math.max(0,k));
    }
    /** The entry belonging to the same copy as one of its doors. */
    public static @Nullable LabyrinthData.Door entryOf(LabyrinthData d,LabyrinthPlace place,LabyrinthData.Door door){return entry(d,place,copyOf(door.id));}

    /**
     * What a reader's door into a scene with copies opens onto: a door, a copy still being made ready, or nothing free. At
     * the lake house a partner may instead be waiting for their diver ({@code waitsFor}).
     */
    public record Entry(@Nullable LabyrinthData.Door door,int copy,boolean waiting,@Nullable UUID waitsFor){
        public Entry(@Nullable LabyrinthData.Door door,int copy,boolean waiting){this(door,copy,waiting,null);}
    }
    public static Entry enter(ServerPlayer p,LabyrinthPlace place){
        var chosen=choose(p,place);
        // 0.4.547: a reader through the door into a Pell house counts as in it, for a friend through the door behind them, until they arrive.
        if(place==LabyrinthPlace.PELL_HOUSE&&chosen.door()!=null){int now=p.server.getTickCount();ENTERING.values().removeIf(e->now-e.tick()>ENTERING_TICKS);ENTERING.put(p.getUUID(),new Entering(chosen.copy(),now));}
        return chosen;
    }
    private static Entry choose(ServerPlayer p,LabyrinthPlace place){
        var s=p.server;var d=LabyrinthData.get(s);var me=p.getUUID();settle(d,place);
        if(place==LabyrinthPlace.LAKE_HOUSE){var lake=lake(p,d,me);if(lake!=null)return lake;}
        if(place==LabyrinthPlace.PELL_HOUSE){var house=pell(p,d,me);if(house!=null)return house;}
        // 0.4.551: a story written for a party fills a copy before the next is begun.
        if(party(place)>0)return crowd(p,d,me,place,party(place));
        int mine=kept(d,me,place);
        if(mine>=0&&built(d,place,mine)){
            // A copy they keep and nobody else is in. Only an original shared by readers from before copies can be busy.
            if(!occupied(s,place,mine,me)){ahead(s,d,place,mine);return new Entry(entry(d,place,mine),mine,false);}
            return new Entry(null,mine,false);
        }
        for(int k=0;k<=copies(d,place);k++){
            if(!built(d,place,k)||occupied(s,place,k,me)||keptByAnother(d,place,k,me))continue;
            keep(d,me,place,k);
            // Harrigan's room keeps its own story: a copy passed to a new reader starts it again.
            if(place==LabyrinthPlace.HARRIGAN&&k>0)HarriganVignette.freshStory(d,k);
            // So does the clapping game's: a copy passed to a new reader is put back as new.
            if(place==LabyrinthPlace.HIDE_AND_CLAP&&k>0)HideAndClap.freshRound(d,k);
            ahead(s,d,place,k);return new Entry(entry(d,place,k),k,false);
        }
        // Nothing stands free: a copy is made ready, unless every copy the server allows exists already.
        boolean making=making(d,place)||allocate(d,place);
        return new Entry(null,-1,making);
    }
    /**
     * 0.4.513, Bike night (docs/BIKE_NIGHT_PROPOSAL.md: "More than six readers get a town of their own (the scene-copy
     * system)"). A town is not one reader's but six readers', one to a bike, so who keeps it holds nobody out. A reader goes
     * back into the town they keep while it has room, and otherwise into the first that stands and has room: friends go into
     * the same one, and the seventh into the next. The next is made ready when a town is down to its last place and no other
     * has room, so the seventh seldom waits; until it stands, their door waits as any door onto a copy being made does.
     * 0.4.551: the same for every story written for a party ({@link #PARTIES}); a copy's room is its places not held ({@link #held}).
     */
    private static Entry crowd(ServerPlayer p,LabyrinthData d,UUID me,LabyrinthPlace place,int most){
        var s=p.server;int mine=kept(d,me,place);
        // 0.4.559: a companion with another reader's scent leads into the copy that reader is in, when it has room, unless this reader
        // has a place kept in their own copy's story (Scents).
        int toward=Scents.copyToward(s,d,me,place);boolean placed=mine>=0&&roll(s,place,mine).contains(me);
        int chosen=!placed&&toward>=0&&built(d,place,toward)&&fits(s,place,toward,me,most)?toward:mine>=0&&built(d,place,mine)&&fits(s,place,mine,me,most)?mine:-1;
        for(int k=0;chosen<0&&k<=copies(d,place);k++)if(built(d,place,k)&&fits(s,place,k,me,most))chosen=k;
        if(chosen<0)return new Entry(null,-1,making(d,place)||allocate(d,place));
        if(chosen!=mine)keep(d,me,place,chosen);
        if(held(s,place,chosen,me)+1>=most-1){
            boolean room=false;
            for(int k=0;k<=copies(d,place);k++)room|=k!=chosen&&(!built(d,place,k)||fits(s,place,k,me,most));
            if(!room)allocate(d,place);
        }
        return new Entry(entry(d,place,chosen),chosen,false);
    }
    /** 0.4.559: the copies in the order a reader tries them: the one their companion's scent leads toward first (Scents), then the rest. */
    private static int[] order(MinecraftServer s,LabyrinthData d,UUID me,LabyrinthPlace place){
        int n=copies(d,place),toward=Scents.copyToward(s,d,me,place);int[] out=new int[n+1];int i=0;
        if(toward>=0&&toward<=n)out[i++]=toward;
        for(int k=0;k<=n;k++)if(k!=toward)out[i++]=k;
        return out;
    }
    /** How many living readers other than {@code reader} are in copy {@code k}. Spectators are nobody. */
    private static int others(MinecraftServer s,LabyrinthPlace p,int k,UUID reader){
        int n=0;
        for(var q:s.getPlayerList().getPlayers())if(!q.getUUID().equals(reader)&&q.isAlive()&&!q.isSpectator()&&at(s,p,q.level().dimension(),q.position())==k)n++;
        return n;
    }
    /** While one copy stands free beside the one just taken, nothing more is built; otherwise the next is made ready ahead. */
    private static void ahead(MinecraftServer s,LabyrinthData d,LabyrinthPlace place,int taken){
        // A reader alone on the server never needs another copy.
        long readers=s.getPlayerList().getPlayers().stream().filter(q->!q.isSpectator()).count();if(readers<2)return;
        // 0.4.547: two readers to a Pell house: another is made ready only once there are more readers on the server than the houses that
        // stand, or are being made, hold. Two friends alone on a server never cause a second house to be built.
        if(place==LabyrinthPlace.PELL_HOUSE&&readers<=2L*(copies(d,place)+1))return;
        for(int k=0;k<=copies(d,place);k++)if(k!=taken&&(!built(d,place,k)||!occupied(s,place,k,new UUID(0,0))&&!keptByAnother(d,place,k,new UUID(0,0))))return;
        allocate(d,place);
    }
    private static boolean making(LabyrinthData d,LabyrinthPlace p){for(int k=1;k<=copies(d,p);k++)if(!copy(d,p,k).getBoolean("Built"))return true;return false;}
    private static boolean allocate(LabyrinthData d,LabyrinthPlace p){
        int n=copies(d,p);if(n>=cap()||making(d,p))return false;
        var all=scene(d,p);all.putInt("Copies",n+1);all.put("C"+(n+1),new CompoundTag());scene(d,p,all);return true;
    }

    // ------------------------------------------------------------------------------------------------ the lake house's pairs
    /*
     * 0.4.156 (owner, October 28; docs/LAKE_HOUSE_PAIRS_0_4_156.md): a second reader inhabits Tess's role. Each copy of the
     * lake house is two readers': the diver whose story it is, and a partner in Tess's place. A third reader gets a copy of
     * their own with Tess, a fourth joins them, a fifth is on their own again. Pairs are saved as partner -> diver.
     */
    private static Map<UUID,UUID> partners=Map.of();private static LabyrinthData partnersData;private static int partnersVersion=-1;
    private static Map<UUID,UUID> partners(LabyrinthData d){
        if(d!=partnersData||partnersVersion!=version){
            partnersData=d;partnersVersion=version;var out=new HashMap<UUID,UUID>();var t=scene(d,LabyrinthPlace.LAKE_HOUSE).getCompound("Partners");
            for(var key:t.getAllKeys())try{if(t.hasUUID(key))out.put(UUID.fromString(key),t.getUUID(key));}catch(IllegalArgumentException ignored){}
            partners=Map.copyOf(out);
        }
        return partners;
    }
    /** The diver a reader goes down to the lake with, if they are a partner. */
    public static @Nullable UUID diverOf(LabyrinthData d,UUID partner){return partners(d).get(partner);}
    /** The partner who goes down with a diver, if any. */
    public static @Nullable UUID partnerOf(LabyrinthData d,UUID diver){for(var e:partners(d).entrySet())if(e.getValue().equals(diver))return e.getKey();return null;}
    /** The other half of a reader's pair, either way round. */
    public static @Nullable UUID pairOf(LabyrinthData d,UUID reader){var diver=diverOf(d,reader);return diver!=null?diver:partnerOf(d,reader);}
    private static void pair(LabyrinthData d,UUID partner,UUID diver){var all=scene(d,LabyrinthPlace.LAKE_HOUSE);var t=all.getCompound("Partners");t.putUUID(partner.toString(),diver);all.put("Partners",t);scene(d,LabyrinthPlace.LAKE_HOUSE,all);}
    /** A partner who chose to go down without their diver is let go: they keep nothing of the diver's copy and start their own story. */
    private static void unpair(LabyrinthData d,UUID partner){
        var all=scene(d,LabyrinthPlace.LAKE_HOUSE);var t=all.getCompound("Partners");t.remove(partner.toString());all.put("Partners",t);
        var b=all.getCompound("Kept");b.remove(partner.toString());all.put("Kept",b);scene(d,LabyrinthPlace.LAKE_HOUSE,all);
        LiteraryVignettes.save(d,partner,LabyrinthPlace.LAKE_HOUSE,new CompoundTag());
    }
    /**
     * The lake house's own choice before the usual one. A reader whose story ends in the empty lake is sent on there. A partner
     * goes down only with their diver, into the diver's copy; once the diver's story is over, the partner comes back like
     * anyone else to read their account. A new reader joins a diver alone in their copy as their partner, before taking a copy
     * of their own. Null when the usual choice applies.
     */
    private static @Nullable Entry lake(ServerPlayer p,LabyrinthData d,UUID me){
        var place=LabyrinthPlace.LAKE_HOUSE;var s=p.server;
        if(LakeHouse.SAID_HELLO.equals(LiteraryVignettes.personal(d,me,place).getString("Outcome")))return new Entry(entry(d,place,0),0,false);
        var diver=diverOf(d,me);
        if(diver!=null&&LakeHousePairs.diving(d,diver)){
            int k=kept(d,diver,place);var there=s.getPlayerList().getPlayer(diver);
            if(k>=0&&built(d,place,k)&&there!=null&&there.isAlive()&&!there.isSpectator()&&at(s,place,there.level().dimension(),there.position())==k){keep(d,me,place,k);return new Entry(entry(d,place,k),k,false);}
            // A second try at the door while the diver is away lets the partner go down alone, as a reader of their own.
            if(!LakeHousePairs.release(p))return new Entry(null,-1,false,diver);
            unpair(d,me);LakeHousePairs.released(p,diver);return null;
        }
        if(diver!=null||kept(d,me,place)>=0)return null;
        for(int k:order(s,d,me,place)){
            if(!built(d,place,k))continue;var alone=lone(s,d,place,k,me);if(alone==null)continue;
            pair(d,me,alone.getUUID());keep(d,me,place,k);LakeHousePairs.paired(p,alone);ahead(s,d,place,k);
            return new Entry(entry(d,place,k),k,false);
        }
        return null;
    }
    /** The other half of a reader's pair, where a scene's copies are two readers' each: the lake house's diver and partner (0.4.156), and
     *  the Pell house's two readers (0.4.547). */
    public static @Nullable UUID pairOf(LabyrinthData d,LabyrinthPlace place,UUID reader){
        return place==LabyrinthPlace.LAKE_HOUSE?pairOf(d,reader):place==LabyrinthPlace.PELL_HOUSE?siblingOf(d,reader):null;
    }

    // ------------------------------------------------------------------------------------------------ 0.4.547, the Pell house's pairs
    /*
     * The owner, 13 November: "the house should generate a second instance for a third and fourth player". Each copy of the Pell house is
     * two readers': the siblings (one of them may take the cane and be Tilly) on every visit until their own endings. Unlike the lake
     * house's diver and partner, neither's story is the other's, so a pair is saved both ways round, and either goes back into the pair's
     * house with the other there or not. A third reader gets a house of their own, a fourth joins them, a fifth has one of their own again.
     */
    /** How long a reader through the door into a Pell house counts as in it, before they have arrived: ten seconds. A friend through the
     *  same door behind them joins them however long their own way in takes. */
    public static final int ENTERING_TICKS=200;
    private record Entering(int copy,int tick){}
    private static final Map<UUID,Entering> ENTERING=new HashMap<>();
    /** The other reader of a Pell house's pair, if any. */
    public static @Nullable UUID siblingOf(LabyrinthData d,UUID reader){var t=scene(d,LabyrinthPlace.PELL_HOUSE).getCompound("Pairs");return t.hasUUID(reader.toString())?t.getUUID(reader.toString()):null;}
    /** Two readers are a pair; whoever either was paired with before is not paired with them any more. */
    private static void siblings(LabyrinthData d,UUID a,UUID b){
        var all=scene(d,LabyrinthPlace.PELL_HOUSE);var t=all.getCompound("Pairs");
        for(var id:List.of(a,b)){var was=t.hasUUID(id.toString())?t.getUUID(id.toString()):null;if(was!=null&&t.hasUUID(was.toString())&&t.getUUID(was.toString()).equals(id))t.remove(was.toString());}
        t.putUUID(a.toString(),b);t.putUUID(b.toString(),a);all.put("Pairs",t);scene(d,LabyrinthPlace.PELL_HOUSE,all);
    }
    /** The reader's pair, while the pair still has the house to finish. */
    private static @Nullable UUID liveSibling(LabyrinthData d,UUID reader){var other=siblingOf(d,reader);return other!=null&&unfinished(d,other,LabyrinthPlace.PELL_HOUSE)?other:null;}
    /**
     * The Pell house's own choice before the usual one. A reader whose ending has landed sees its aftermath only in a house nobody is in
     * or keeps for a story under way. A reader goes into their pair's house, with the pair there or not. A reader with nobody joins a
     * reader alone in a house of theirs whose story is under way and who has nobody (or whose pair has had their ending). Null when the
     * usual choice applies: the house they keep, else one nobody keeps, else the next made ready.
     */
    private static @Nullable Entry pell(ServerPlayer p,LabyrinthData d,UUID me){
        var place=LabyrinthPlace.PELL_HOUSE;var s=p.server;
        if(!unfinished(d,me,place)){
            // A reader whose ending has landed comes back to the house as its aftermath (PellAftermath) only in a house nobody is in and
            // nobody keeps for a story still under way: their own first, else any such house; with none, the door opens onto a hall.
            // Another house seen so is not made theirs: the house they keep is still the one they were at.
            int mine=kept(d,me,place);
            if(mine>=0&&built(d,place,mine)&&!occupied(s,place,mine,me)&&!keptByAnother(d,place,mine,me))return new Entry(entry(d,place,mine),mine,false);
            for(int k=0;k<=copies(d,place);k++)if(built(d,place,k)&&!occupied(s,place,k,me)&&!keptByAnother(d,place,k,me))return new Entry(entry(d,place,k),k,false);
            return new Entry(null,-1,false);
        }
        var pair=liveSibling(d,me);
        if(pair!=null){
            int k=kept(d,pair,place);if(k<0||!built(d,place,k))return null;
            if(kept(d,me,place)!=k)keep(d,me,place,k);
            // Only somebody with the house still to finish holds it from its own pair; a reader come back to its aftermath does not.
            if(held(s,d,place,k,me))return new Entry(null,k,false);
            ahead(s,d,place,k);return new Entry(entry(d,place,k),k,false);
        }
        for(int k:order(s,d,me,place)){
            if(!built(d,place,k))continue;var alone=alone(s,d,place,k,me);if(alone==null)continue;
            siblings(d,me,alone.getUUID());keep(d,me,place,k);ahead(s,d,place,k);
            return new Entry(entry(d,place,k),k,false);
        }
        return null;
    }
    /** Somebody other than the reader and their pair, with the Pell house still to finish, is in copy {@code k}. */
    private static boolean held(MinecraftServer s,LabyrinthData d,LabyrinthPlace place,int k,UUID reader){
        var pair=pairOf(d,place,reader);
        for(var q:s.getPlayerList().getPlayers())
            if(!q.getUUID().equals(reader)&&!q.getUUID().equals(pair)&&q.isAlive()&&!q.isSpectator()&&at(s,place,q.level().dimension(),q.position())==k&&unfinished(d,q.getUUID(),place))return true;
        return false;
    }
    /**
     * The only living reader in copy {@code k} of the Pell house, there now or through its door a moment ago (ENTERING), if it is their
     * house, their story is under way, their account is not ready, and they have nobody with them (or whoever it was has had their ending).
     */
    private static @Nullable ServerPlayer alone(MinecraftServer s,LabyrinthData d,LabyrinthPlace place,int k,UUID me){
        ServerPlayer found=null;int now=s.getTickCount();
        for(var q:s.getPlayerList().getPlayers()){
            if(q.getUUID().equals(me)||!q.isAlive()||q.isSpectator())continue;
            int at=at(s,place,q.level().dimension(),q.position());var e=ENTERING.get(q.getUUID());
            if(at!=k&&!(at<0&&e!=null&&e.copy()==k&&now-e.tick()<=ENTERING_TICKS))continue;
            if(found!=null)return null;found=q;
        }
        if(found==null)return null;var id=found.getUUID();
        if(liveSibling(d,id)!=null||kept(d,id,place)!=k||!unfinished(d,id,place)||LiteraryVignettes.personal(d,id,place).getBoolean("Ready"))return null;
        return found;
    }

    /** The only living reader in copy {@code k}, if it is their copy, their story is under way and nobody goes down with them yet. */
    private static @Nullable ServerPlayer lone(MinecraftServer s,LabyrinthData d,LabyrinthPlace place,int k,UUID me){
        ServerPlayer found=null;
        for(var q:s.getPlayerList().getPlayers()){
            if(q.getUUID().equals(me)||!q.isAlive()||q.isSpectator()||at(s,place,q.level().dimension(),q.position())!=k)continue;
            if(found!=null)return null;found=q;
        }
        if(found==null)return null;var id=found.getUUID();
        if(diverOf(d,id)!=null||partnerOf(d,id)!=null||kept(d,id,place)!=k||!unfinished(d,id,place)||LiteraryVignettes.personal(d,id,place).getBoolean("Ready"))return null;
        return found;
    }

    // ------------------------------------------------------------------------------------------------ building
    private static final class Job {
        final LabyrinthPlace place;final int copy;final ServerLevel level;final BlockPos base,origin;final BoundingBox slot;final List<ChunkPos> chunks=new ArrayList<>();
        BuildBlocks.Plan plan,dressing;
        Job(LabyrinthPlace place,int copy,ServerLevel level,BlockPos base,BlockPos origin,BoundingBox slot){this.place=place;this.copy=copy;this.level=level;this.base=base;this.origin=origin;this.slot=slot;}
    }
    private static Job job;private static LabyrinthData store;
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){advance(e.getServer());}
    /** One copy at a time, in bounded slices that resume from their saved cursors after a restart. */
    public static void advance(MinecraftServer s){
        var d=LabyrinthData.get(s);if(store!=d){release();store=d;}
        if(job==null){job=next(s,d);if(job==null)return;}
        var j=job;var l=j.level;
        if(j.chunks.isEmpty()){
            // An outdoor copy holds the same wider ground its original is built over, woods and banks included.
            // 0.4.521: Bike night's copy holds its town and its fold's far side, not the thousand empty blocks between (OutdoorRelocation.parts).
            var boxes=!NovelRooms.outside(j.place)?List.of(AABB.of(j.slot)):j.place==LabyrinthPlace.BIKE_NIGHT?OutdoorRelocation.parts(j.base,j.place):List.of(OutdoorRelocation.bounds(j.base,j.place).minmax(AABB.of(j.slot)));
            for(var box:boxes)for(int x=((int)Math.floor(box.minX)-2)>>4;x<=((int)Math.ceil(box.maxX)+2)>>4;x++)for(int z=((int)Math.floor(box.minZ)-2)>>4;z<=((int)Math.ceil(box.maxZ)+2)>>4;z++){var c=new ChunkPos(x,z);if(j.chunks.contains(c))continue;l.getChunkSource().addRegionTicket(TICKET,c,1,c);j.chunks.add(c);}
        }
        // Never call a synchronous chunk getter as a fallback; the entity sections take the scene's own installs.
        for(var c:j.chunks)if(l.getChunkSource().getChunkNow(c.x,c.z)==null||!l.areEntitiesLoaded(c.toLong()))return;
        var t=copy(d,j.place,j.copy);
        if(!t.getBoolean("Filled")){if(fill(j,t)){t.putBoolean("Filled",true);t.remove("FillColumn");}copy(d,j.place,j.copy,t);return;}
        if(!t.getBoolean("Carved")){
            if(j.plan==null){j.plan=BuildBlocks.record(l,()->author(s,l,j.base,j.place));j.plan.resume(t.getCompound("BuildCursor"));}
            if(!j.plan.tick()){t.put("BuildCursor",j.plan.checkpoint());copy(d,j.place,j.copy,t);return;}
            t.putBoolean("Carved",true);t.remove("BuildCursor");copy(d,j.place,j.copy,t);
            // The original's finishing passes, at the copy: their checkpoints are keyed by the shifted origin.
            if(!LiteraryRooms.isLiterary(j.place))VignetteArchitecture.decorateOnce(l,j.origin,j.place);
            ScenePolish.polishOnce(l,j.origin,j.place);j.dressing=SceneHuntReview.prepareFresh(l,j.origin,j.place);return;
        }
        if(j.dressing!=null&&!j.dressing.tick())return;
        OnwardDoors.install(l,j.base,j.place,doorId(j.place,j.copy,OnwardDoors.name(j.place)));
        for(var spec:j.place.doors()){if(OnwardDoors.isOnward(spec))continue;d.putDoor(new LabyrinthData.Door(doorId(j.place,j.copy,spec.name()),NovelRooms.dimension(j.place),j.base.offset(spec.rel()),spec.facing(),spec.destination(),false));}
        t.putBoolean("Built",true);copy(d,j.place,j.copy,t);
        TheOldestHouse.LOGGER.info("A copy of {} stands ready: copy {} at {}",j.place.id(),j.copy,j.base.toShortString());
        release();
    }
    /** The scene's own builder, as the House builds its original. */
    private static void author(MinecraftServer s,ServerLevel l,BlockPos b,LabyrinthPlace p){
        if(LiteraryRooms.isLiterary(p)){LiteraryRooms.build(l,b,p);return;}
        if(NovelVignettes.isNovel(p)){NovelRooms.build(s,l,b,p);return;}
        if(p==LabyrinthPlace.PHONE_CANOE){PhoneCanoe.build(s,l,b);LakeLandscape.dress(l,b,p);NovelRooms.safeApproach(l,b);}
        if(p==LabyrinthPlace.SHALLOWS){Shallows.build(s,l,b);LakeLandscape.dress(l,b,p);NovelRooms.safeApproach(l,b);}
        if(p==LabyrinthPlace.SEANCE){ClassicsRooms.build(l,b,p);io.github.knaitoe.theoldesthouse.house.HouseFurnishings.decorate(l,b,p);}
        if(p==LabyrinthPlace.HARRIGAN){HarriganVignette.build(s,l,b);io.github.knaitoe.theoldesthouse.house.HouseFurnishings.decorate(l,b,p);}
        if(p==LabyrinthPlace.HIDE_AND_CLAP){HideAndClap.build(s,l,b);io.github.knaitoe.theoldesthouse.house.HouseFurnishings.decorate(l,b,p);}
        if(p==LabyrinthPlace.GOATMAN){GoatmanVignette.build(s,l,b);io.github.knaitoe.theoldesthouse.house.HouseFurnishings.decorate(l,b,p);}
    }
    private static @Nullable Job next(MinecraftServer s,LabyrinthData d){
        var o=HouseSavedData.get(s).houseOrigin();if(o==null)return null;
        for(var p:COPYABLE)for(int k=1;k<=copies(d,p);k++){
            if(copy(d,p,k).getBoolean("Built"))continue;
            var l=s.getLevel(NovelRooms.dimension(p));var shifted=shiftedOrigin(o,k);var b=LabyrinthPlaces.base(shifted,p);var slot=LabyrinthPlaces.slotBounds(shifted,p);
            if(l!=null&&b!=null&&slot!=null)return new Job(p,k,l,b,shifted,slot);
        }
        return null;
    }
    /** An interior copy's whole slot is filled solid first, a few columns a tick, as the original's was. */
    private static boolean fill(Job j,CompoundTag t){
        if(!LabyrinthBuilder.prefills(j.place))return true;
        int width=j.slot.getXSpan(),columns=width*j.slot.getZSpan(),at=t.getInt("FillColumn");
        int minY=Math.max(j.slot.minY(),j.level.getMinBuildHeight()),maxY=Math.min(j.slot.maxY(),j.level.getMaxBuildHeight()-1);var pos=new BlockPos.MutableBlockPos();
        for(int n=0;n<FILL_COLUMNS&&at<columns;n++,at++){
            int x=j.slot.minX()+at%width,z=j.slot.minZ()+at/width;
            for(int y=minY;y<=maxY;y++){pos.set(x,y,z);if(j.level.getBlockState(pos)!=LabyrinthBuilder.SOLID)j.level.setBlock(pos,LabyrinthBuilder.SOLID,F);}
        }
        t.putInt("FillColumn",at);return at>=columns;
    }
    private static void release(){if(job!=null)for(var c:job.chunks)job.level.getChunkSource().removeRegionTicket(TICKET,c,1,c);job=null;}
    /** Fixtures only: whether a copy is under construction. */
    public static boolean building(){return job!=null;}
    public static void clear(){release();store=null;cached=null;areas=List.of();partnersData=null;partners=Map.of();ENTERING.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clear();}
}
