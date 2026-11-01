package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 0.4.229, sound through connected doors (docs/DOOR_SOUNDS_0_4_229.md). Sound follows the House's actual connections. Striking a
 * closed House door knocks on it: heard at that door, and faintly at the door it leads to for the knocker, however impossible the
 * place. A door that shuts is heard as a dull thud on its far side. A room's own sound (a clock, water, music, voices) is heard
 * faintly by someone standing at a closed door that leads to it, through the wood. Two readers can find each other by knocking and
 * listening; one alone can know a room by what it sounds like from another entrance.
 * <p>
 * The far side is always the knocker's or listener's own: the place the door is dealt to for them, or, from inside a room, the door
 * they came in by. A door the House has not dealt yet leads nowhere, and carries nothing.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class DoorSounds {
    private DoorSounds(){}
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The knock where it is struck, the knock heard through the door, a door shutting heard through it; drawn by tools/generate_door_sound_audio.py. */
    public static final DeferredHolder<SoundEvent,SoundEvent> KNOCK=sound("door.knock"),KNOCK_FAR=sound("door.knock_far"),THUD_FAR=sound("door.thud_far");
    /** What a room sounds like from the other side of its door. */
    public enum Carry {
        CLOCK("door.through_clock",40,"Through the door, faintly: a clock ticking."),WATER("door.through_water",50,"Through the door: water, moving."),
        MUSIC("door.through_music",44,"Through the door: music, only the low part of it."),VOICES("door.through_voices",60,"Through the door: people talking. You can't make out the words.");
        final DeferredHolder<SoundEvent,SoundEvent> sound;final int every;final String line;
        Carry(String id,int every,String line){sound=sound(id);this.every=every;this.line=line;}
    }
    public static void register(IEventBus bus){Carry.values();SOUNDS.register(bus);}

    /** Between one knock and the next from the same hand; how long a resident takes to knock back. */
    public static final int KNOCK_GAP=6,REPLY_AFTER=24;
    /** How close a listener stands to a closed door to hear the room behind it. */
    public static final double LISTEN=1.75;

    /** The rooms that carry, and what they carry. A room not here is silent through its door. */
    private static final Map<LabyrinthPlace,Carry> CARRIES=new EnumMap<>(LabyrinthPlace.class);
    static{
        for(var p:List.of(LabyrinthPlace.WINCHESTER,LabyrinthPlace.MASQUE,LabyrinthPlace.HARRIGAN,LabyrinthPlace.CONFESSION,LabyrinthPlace.USHER,LabyrinthPlace.WHALE,LabyrinthPlace.KAREN_ROOM,LabyrinthPlace.READ_WAITING,LabyrinthPlace.READ_KITCHEN,LabyrinthPlace.FAMILIAR_KITCHEN))CARRIES.put(p,Carry.CLOCK);
        for(var p:List.of(LabyrinthPlace.LAST_SWIMMER,LabyrinthPlace.LAKE_HOUSE,LabyrinthPlace.FLOODED_PASSAGE,LabyrinthPlace.BARN_WELL,LabyrinthPlace.SHALLOWS,LabyrinthPlace.DROWNED_TOWN,LabyrinthPlace.BENT_CISTERN,LabyrinthPlace.READ_BATHROOM))CARRIES.put(p,Carry.WATER);
        for(var p:List.of(LabyrinthPlace.NIGHTCLUB,LabyrinthPlace.POLYBIUS,LabyrinthPlace.MOVIE_NIGHT,LabyrinthPlace.ELK_LOT,LabyrinthPlace.DEAD_AIR))CARRIES.put(p,Carry.MUSIC);
        for(var p:List.of(LabyrinthPlace.HOTEL,LabyrinthPlace.GOATMAN,LabyrinthPlace.SEANCE,LabyrinthPlace.GHOSTS_SET,LabyrinthPlace.COSTUME_NIGHT,LabyrinthPlace.VACANT_POSSESSION))CARRIES.put(p,Carry.VOICES);
    }
    @Nullable public static Carry carries(@Nullable LabyrinthPlace p){return p==null?null:CARRIES.get(p);}

    // ------------------------------------------------------------------------------------------------ where a door leads
    /** Where this door leads for this reader, as a place in the world: the door they would come out of. Nothing if it is undealt or locked. */
    @Nullable public static GlobalPos far(LabyrinthData data,UUID reader,LabyrinthData.Door door){
        if(LabyrinthData.LOCKED.equals(door.destination))return null;
        if(LabyrinthData.RETURN.equals(door.destination)||LabyrinthData.HALLWAY_OR_RETURN.equals(door.destination)){
            var back=data.peekReturn(reader);if(back==null)return null;
            return GlobalPos.of(back.dimension(),BlockPos.containing(back.pos()));
        }
        var place=leadsTo(data,reader,door);if(place==null||place.slot()<0)return null;
        var entry=SceneInstances.copyable(place)?SceneInstances.entry(data,place,Math.max(0,SceneInstances.kept(data,reader,place))):data.door(place.entryDoorId());
        return entry==null?null:entry.globalPos();
    }
    /** The place a door is dealt to for this reader, without dealing it. */
    @Nullable public static LabyrinthPlace leadsTo(LabyrinthData data,UUID reader,LabyrinthData.Door door){
        if(LabyrinthData.DEALT.equals(door.destination)){var deal=data.deal(reader,door);return deal==null?null:LabyrinthPlace.byId(deal.place());}
        if(door.destination!=null&&door.destination.startsWith("place:"))return LabyrinthPlace.byId(door.destination.substring(6));
        return null;
    }
    /** The place on the far side for this reader: a dealt door's place, or the place they came in from. */
    @Nullable static LabyrinthPlace farPlace(MinecraftServer server,LabyrinthData data,UUID reader,LabyrinthData.Door door){
        var lead=leadsTo(data,reader,door);if(lead!=null)return lead;
        var at=far(data,reader,door);if(at==null)return null;
        var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(server).houseOrigin();
        return origin==null?null:LabyrinthPlaces.placeAt(origin,at.pos());
    }

    // ------------------------------------------------------------------------------------------------ what was heard (tests read this)
    /** One sound carried: what, and where it was heard. */
    public record Heard(String what,GlobalPos at,@Nullable UUID only){}
    private static final Deque<Heard> HEARD=new ArrayDeque<>();
    public static List<Heard> heard(){return List.copyOf(HEARD);}
    public static void clearAll(){HEARD.clear();LAST_KNOCK.clear();REPLIES.clear();REPLIED.clear();LAST_USER.clear();LISTENING.clear();CHANGED.clear();NOTICED.clear();}
    private static void note(String what,GlobalPos at,@Nullable UUID only){HEARD.addLast(new Heard(what,at,only));while(HEARD.size()>64)HEARD.removeFirst();}

    // ------------------------------------------------------------------------------------------------ knocking
    private static final Map<UUID,Long> LAST_KNOCK=new HashMap<>();
    private record Reply(GlobalPos at,long due,UUID knocker){}
    private static final List<Reply> REPLIES=new ArrayList<>();
    private static final Set<String> REPLIED=new HashSet<>();

    @SubscribeEvent public static void strike(PlayerInteractEvent.LeftClickBlock e){
        if(e.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START||!(e.getEntity() instanceof ServerPlayer p)||p.isSpectator())return;
        var l=p.serverLevel();if(!HouseDimensions.isHouseDimension(l.dimension()))return;
        var data=LabyrinthData.get(p.server);var door=data.doorAt(l.dimension(),e.getPos());if(door==null)return;
        var s=l.getBlockState(door.lower);if(!(s.getBlock() instanceof DoorBlock)||s.getValue(DoorBlock.OPEN))return;
        knock(p,data,door);
    }
    /** A knock on a closed door: at the door, and faintly where it leads for the knocker. A resident behind a locked door knocks back, once. */
    public static boolean knock(ServerPlayer p,LabyrinthData data,LabyrinthData.Door door){
        long now=p.server.getTickCount();Long last=LAST_KNOCK.get(p.getUUID());if(last!=null&&now-last<KNOCK_GAP)return false;LAST_KNOCK.put(p.getUUID(),now);
        var l=p.serverLevel();
        l.playSound(null,door.lower.above(),KNOCK.get(),SoundSource.BLOCKS,1F,.92F+l.random.nextFloat()*.16F);
        note("knock",GlobalPos.of(l.dimension(),door.lower),null);
        if(LabyrinthData.LOCKED.equals(door.destination)){String key=p.getUUID()+"|"+door.id;if(REPLIED.add(key))REPLIES.add(new Reply(door.globalPos(),now+REPLY_AFTER,p.getUUID()));return true;}
        var far=far(data,p.getUUID(),door);if(far==null)return true;
        var level=p.server.getLevel(far.dimension());if(level==null||!level.isLoaded(far.pos()))return true;
        level.playSound(null,far.pos().above(),KNOCK_FAR.get(),SoundSource.BLOCKS,1F,.9F+level.random.nextFloat()*.1F);
        note("knock_far",far,null);
        // Whoever is near that door, on that side, knows someone is at it.
        for(var other:level.players())if(other!=p&&!other.isSpectator()&&other.position().distanceToSqr(Vec3.atCenterOf(far.pos()))<36)cue(other,"Someone knocks, on the other side of the door.");
        return true;
    }

    // ------------------------------------------------------------------------------------------------ doors shutting
    /** Who last opened each door: its far side, when it shuts, is theirs. */
    private static final Map<String,UUID> LAST_USER=new HashMap<>();
    /** When each door last opened or shut, so a crossing (one door shutting, then the other) carries nothing back and forth. */
    private static final Map<GlobalPos,Long> CHANGED=new HashMap<>();
    public static void used(ServerPlayer p,LabyrinthData.Door door){LAST_USER.put(door.id,p.getUUID());}
    /** A House door has opened or shut; a shut one is heard on the far side as a dull thud, unless that side has just moved itself. */
    public static void changed(ServerLevel level,BlockPos lower,boolean open){changed(level,LabyrinthData.get(level.getServer()),lower,open);}
    public static void changed(ServerLevel level,LabyrinthData data,BlockPos lower,boolean open){
        long now=level.getServer().getTickCount();var here=GlobalPos.of(level.dimension(),lower);CHANGED.put(here,now);
        if(open)return;
        var door=data.doorAt(level.dimension(),lower);if(door==null)return;
        var who=LAST_USER.get(door.id);if(who==null)return;
        var far=far(data,who,door);if(far==null)return;
        Long moved=CHANGED.get(far);if(moved!=null&&now-moved<=3)return;
        var user=level.getServer().getPlayerList().getPlayer(who);
        if(user!=null&&user.level().dimension().equals(far.dimension())&&user.position().distanceToSqr(Vec3.atCenterOf(far.pos()))<16)return;
        var there=level.getServer().getLevel(far.dimension());if(there==null||!there.isLoaded(far.pos()))return;
        there.playSound(null,far.pos().above(),THUD_FAR.get(),SoundSource.BLOCKS,1F,.85F+there.random.nextFloat()*.1F);
        note("thud_far",far,null);
    }

    // ------------------------------------------------------------------------------------------------ listening at a door
    /** When each listener last heard each door's room. */
    private static final Map<String,Long> LISTENING=new HashMap<>();
    private static final Set<String> NOTICED=new HashSet<>();
    static void cue(ServerPlayer p,String line){p.displayClientMessage(net.minecraft.network.chat.Component.literal(line).withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();long now=server.getTickCount();
        for(var it=REPLIES.iterator();it.hasNext();){var r=it.next();if(now<r.due())continue;it.remove();var level=server.getLevel(r.at().dimension());
            if(level!=null&&level.isLoaded(r.at().pos())){level.playSound(null,r.at().pos().above(),KNOCK_FAR.get(),SoundSource.BLOCKS,.9F,.8F);note("reply",r.at(),null);
                var knocker=server.getPlayerList().getPlayer(r.knocker());if(knocker!=null&&knocker.position().distanceToSqr(Vec3.atCenterOf(r.at().pos()))<64)cue(knocker,"From behind the door, someone knocks back. Twice, slowly.");}}
        if(now%10!=0)return;
        var data=LabyrinthData.get(server);
        for(var p:server.getPlayerList().getPlayers()){
            if(p.isSpectator()||!p.isAlive()||!HouseDimensions.isHouseDimension(p.level().dimension())||LabyrinthDoors.isBusy(p))continue;
            listen(p,data,now);
        }
        if(CHANGED.size()>256)CHANGED.entrySet().removeIf(en->now-en.getValue()>40);
    }
    /** A reader standing at a closed door hears the room behind it, faintly, if it is a room that carries. */
    public static boolean listen(ServerPlayer p,LabyrinthData data,long now){
        var door=nearestClosed(p,data);if(door==null)return false;
        var place=farPlace(p.server,data,p.getUUID(),door);var carry=carries(place);if(carry==null)return false;
        String key=p.getUUID()+"|"+door.id;Long last=LISTENING.get(key);if(last!=null&&now-last<carry.every)return false;LISTENING.put(key,now);
        var at=Vec3.atBottomCenterOf(door.lower).add(0,1.1,0);
        p.connection.send(new ClientboundSoundPacket(Holder.direct(carry.sound.get()),SoundSource.AMBIENT,at.x,at.y,at.z,.55F,.95F,p.getRandom().nextLong()));
        note("through_"+carry.name().toLowerCase(Locale.ROOT),GlobalPos.of(p.level().dimension(),door.lower),p.getUUID());
        // The first time a reader hears a kind of room through a door, they are told what it is.
        if(NOTICED.add(p.getUUID()+"|"+carry.name()))cue(p,carry.line);
        return true;
    }
    /** The closed House door the reader is standing at, on either side, if any. */
    @Nullable static LabyrinthData.Door nearestClosed(ServerPlayer p,LabyrinthData data){
        var l=p.serverLevel();var feet=p.blockPosition();
        for(var d:net.minecraft.core.Direction.Plane.HORIZONTAL){
            for(int k=0;k<=1;k++){var at=feet.relative(d,k==0?1:2);
                var door=data.doorAt(l.dimension(),at);if(door==null)door=data.doorAt(l.dimension(),at.below());
                if(door==null)continue;var s=l.getBlockState(door.lower);if(!(s.getBlock() instanceof DoorBlock)||s.getValue(DoorBlock.OPEN))continue;
                if(p.position().distanceToSqr(Vec3.atBottomCenterOf(door.lower))<=LISTEN*LISTEN+1)return door;
            }
        }
        return null;
    }
}
