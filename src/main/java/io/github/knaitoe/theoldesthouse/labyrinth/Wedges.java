package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 0.4.231: the door wedge (docs/TRACES_0_4_231.md). The owner: "a wedge holds a door where it was left", and "a door wedge becomes a
 * way to hold an unstable route open."
 *
 * <p>A reader's wedge goes in under an open door with a right-click while it is in either hand (the door is opened first if it is
 * shut) and comes out with a right-click on the door, into the hand of whoever pulls it. In a door on a closer (the cloakroom's, the
 * sprung rooms') it keeps the door from swinging shut. In the House's own doors it goes only into the door a reader came in by, from
 * inside an ordinary hall, a quiet room or a puzzle room (never a story, and never a room copied for each reader), and there:
 * <ul>
 * <li>the door is not shut behind anyone, coming or going, so the stretch of hall copied behind it stays in view;</li>
 * <li>the hall behind it stays what it was: for the one who set it, the door they came through still leads here, however they come
 * back to it, and the House's growls ({@link GrowlChanges}) never deal it elsewhere;</li>
 * <li>nobody can come in by that door from anywhere else while it stands open on the hall it shows: their door gives an inch and stops
 * against something. If the one who set it is not in the game, the wedge is knocked loose instead and lies by the door;</li>
 * <li>anything that shuts the door hard (a story's exit closing) knocks it loose the same way.</li>
 * </ul>
 * One wedge to a reader (TraceRooms' hooks); nothing replaces a lost one. Saved; the wedge itself is a display entity at the door's
 * foot, so it stays where it was left for everyone to see.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Wedges {
    private Wedges(){}
    public static final String STATE="wedges_0231";
    public static final String TAG="the_oldest_house_wedge";
    static String key(Level l,BlockPos lower){return l.dimension().location()+"|"+lower.asLong();}
    static @Nullable CompoundTag record(Level l,BlockPos lower){
        if(l.getServer()==null)return null;var data=LabyrinthData.get(l.getServer());var tag=data.stateEntry(STATE,key(l,lower));return tag.isEmpty()?null:tag;
    }
    /** Whether a wedge holds this door (its lower half). */
    public static boolean at(Level l,BlockPos lower){return record(l,lower)!=null;}
    /** Where the reader's wedge is, if it is in a door: the dealt place a House door it holds keeps behind the door they came in by. */
    public static @Nullable String pin(LabyrinthData data,UUID reader,String doorId){
        for(var key:data.stateKeys(STATE)){var r=data.stateEntry(STATE,key);if(r.getBoolean("House")&&r.hasUUID("Owner")&&r.getUUID("Owner").equals(reader)&&doorId.equals(r.getString("From")))return r.getString("Deal");}
        return null;
    }
    /** Whether a reader's wedge holds the door into this place. */
    public static boolean ownedIn(LabyrinthData data,UUID reader,LabyrinthPlace place){
        for(var key:data.stateKeys(STATE)){var r=data.stateEntry(STATE,key);if(r.hasUUID("Owner")&&r.getUUID("Owner").equals(reader)&&place.id().equals(r.getString("Place")))return true;}
        return false;
    }
    /** House doors a wedge may hold: the entry of an ordinary hall, a quiet room or a puzzle room, the same room for everyone. */
    public static boolean holdsIn(LabyrinthPlace place){
        return !place.isVignette()&&!SceneInstances.copyable(place)&&(LabyrinthPacing.ordinary(place)||LabyrinthPacing.quiet(place)||PuzzleRooms.is(place)||place==LabyrinthPlace.FAMILIAR_KITCHEN);
    }

    // ------------------------------------------------------------------ hands
    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void click(PlayerInteractEvent.RightClickBlock e){
        if(e.getLevel().isClientSide()||!(e.getLevel() instanceof ServerLevel l)||!(e.getEntity() instanceof ServerPlayer p))return;
        if(!l.dimension().equals(HouseDimensions.INTERIOR))return;
        var s=l.getBlockState(e.getPos());if(!(s.getBlock() instanceof DoorBlock))return;
        var lower=s.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER?e.getPos().below():e.getPos();
        boolean wedged=at(l,lower);boolean holding=p.getMainHandItem().is(TraceRegistry.WEDGE.get())||p.getOffhandItem().is(TraceRegistry.WEDGE.get());
        // Only a door the wedge can hold takes it; with the wedge in hand every other door is used as ever.
        if(!wedged&&!(holding&&(TraceRooms.spring(l,lower)||takes(p,l,lower))))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(e.getHand()!=InteractionHand.MAIN_HAND||p.isSpectator())return;
        if(wedged)lift(p,l,lower);else wedge(p,l,lower);
    }
    /** Whether this House door is one the reader's wedge would hold: the entry of the room they stand in, if it is a room a wedge holds. */
    public static boolean takes(ServerPlayer p,ServerLevel l,BlockPos lower){
        var door=LabyrinthData.get(l.getServer()).doorAt(l.dimension(),lower);var origin=HouseSavedData.get(l.getServer()).houseOrigin();if(door==null||origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());return place!=null&&holdsIn(place)&&door.id.equals(place.entryDoorId());
    }
    /** A reader's wedge into a door. Returns whether it went in. */
    public static boolean wedge(ServerPlayer p,ServerLevel l,BlockPos lower){
        var hand=p.getMainHandItem().is(TraceRegistry.WEDGE.get())?InteractionHand.MAIN_HAND:p.getOffhandItem().is(TraceRegistry.WEDGE.get())?InteractionHand.OFF_HAND:null;
        if(hand==null||at(l,lower))return false;
        var data=LabyrinthData.get(l.getServer());var r=new CompoundTag();r.putUUID("Owner",p.getUUID());
        var door=data.doorAt(l.dimension(),lower);
        if(door!=null){
            var origin=HouseSavedData.get(l.getServer()).houseOrigin();var place=origin==null?null:LabyrinthPlaces.placeAt(origin,p.blockPosition());
            if(place==null||!holdsIn(place)||!door.id.equals(place.entryDoorId())){PuzzleRooms.say(p,"The wedge won't hold this door.");return false;}
            var back=data.peekReturn(p.getUUID());var from=back==null?null:data.doorAt(back.dimension(),BlockPos.containing(back.pos()));
            r.putBoolean("House",true);r.putString("Door",door.id);r.putString("Place",place.id());r.putString("Deal",place.id());if(from!=null)r.putString("From",from.id);
            LabyrinthDoors.setDoorOpen(l,lower,true,p);
        }else if(TraceRooms.spring(l,lower)){
            var origin=HouseSavedData.get(l.getServer()).houseOrigin();var place=origin==null?null:LabyrinthPlaces.placeAt(origin,lower);if(place!=null)r.putString("Place",place.id());
            open(l,lower);
        }else return false;
        var facing=l.getBlockState(lower).getValue(DoorBlock.FACING);
        var shown=display(l,lower,facing);if(shown!=null)r.putUUID("Display",shown.getUUID());
        data.setStateEntry(STATE,key(l,lower),r);
        p.getItemInHand(hand).shrink(1);
        l.playSound(null,lower,SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.8F,1.3F);
        PuzzleRooms.say(p,"The wedge goes in under the door, and the door stays where you left it.");
        return true;
    }
    /** The wedge out of a door, into the hand that pulls it. */
    public static boolean lift(ServerPlayer p,ServerLevel l,BlockPos lower){
        var r=record(l,lower);if(r==null)return false;
        remove(l,lower,r);TraceRooms.give(p,new ItemStack(TraceRegistry.WEDGE.get()));
        l.playSound(null,lower,SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.7F,1.3F);
        PuzzleRooms.say(p,"You work the wedge out from under the door.");
        return true;
    }
    /** Something shuts the door hard, or comes at it from the other side with nobody to hold it: the wedge skids loose and lies by the door. */
    public static void knockLoose(ServerLevel l,BlockPos lower){
        var r=record(l,lower);if(r==null)return;remove(l,lower,r);
        var at=Vec3.atBottomCenterOf(lower).add(0,.15,0);var item=new ItemEntity(l,at.x,at.y,at.z,new ItemStack(TraceRegistry.WEDGE.get()));
        item.setDeltaMovement(0,.05,0);item.setUnlimitedLifetime();l.addFreshEntity(item);
        l.playSound(null,lower,SoundEvents.WOOD_HIT,SoundSource.BLOCKS,1F,.7F);
    }
    static void remove(ServerLevel l,BlockPos lower,CompoundTag r){
        if(r.hasUUID("Display")){var shown=l.getEntity(r.getUUID("Display"));if(shown!=null)shown.discard();}
        for(var d:l.getEntitiesOfClass(Display.ItemDisplay.class,new AABB(lower).inflate(1),e->e.getTags().contains(TAG)))d.discard();
        LabyrinthData.get(l.getServer()).setStateEntry(STATE,key(l,lower),new CompoundTag());
    }
    static void open(ServerLevel l,BlockPos lower){
        for(var half:new BlockPos[]{lower,lower.above()}){var s=l.getBlockState(half);if(s.getBlock() instanceof DoorBlock&&!s.getValue(DoorBlock.OPEN))l.setBlock(half,s.setValue(DoorBlock.OPEN,true),Block.UPDATE_ALL);}
        l.playSound(null,lower,SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,1F,1F);
    }
    /** The wedge itself, lying at the door's foot. */
    static @Nullable Display.ItemDisplay display(ServerLevel l,BlockPos lower,Direction facing){
        var d=EntityType.ITEM_DISPLAY.create(l);if(d==null)return null;
        var tag=d.saveWithoutId(new CompoundTag());tag.put("item",new ItemStack(TraceRegistry.WEDGE.get()).save(l.registryAccess()));tag.putString("item_display","fixed");
        var t=new CompoundTag();t.put("scale",floats(.5F,.5F,.5F));t.put("translation",floats(0,0,0));
        t.put("left_rotation",floats(-.70710677F,0,0,.70710677F));t.put("right_rotation",floats(0,0,0,1));tag.put("transformation",t);d.load(tag);
        var at=Vec3.atBottomCenterOf(lower).add(facing.getStepX()*.3,.04,facing.getStepZ()*.3);
        d.moveTo(at.x,at.y,at.z,facing.toYRot(),0);d.addTag(TAG);l.addFreshEntity(d);return d;
    }
    private static ListTag floats(float... values){var list=new ListTag();for(float v:values)list.add(FloatTag.valueOf(v));return list;}

    // ------------------------------------------------------------------ the House's doors
    /**
     * Someone coming in through {@code from} to a room whose entry a wedge holds open on the hall it was set from: only that hall's
     * door may come in. From anywhere else the door gives an inch and stops; unless whoever set the wedge is not in the game, when it
     * is knocked loose and they come in as ever. Returns whether they are kept out.
     */
    public static boolean blocks(ServerPlayer p,LabyrinthData.Door from,LabyrinthData.Door entry){
        if(!held(p,from,entry))return false;
        stops(p,from);
        return true;
    }
    /** Whether a wedge holds this entry open on another hall than {@code from}'s. If whoever set it is not in the game, it is knocked loose and nothing holds it. */
    static boolean held(ServerPlayer p,LabyrinthData.Door from,LabyrinthData.Door entry){
        var l=p.server.getLevel(entry.dimension);if(l==null)return false;var r=record(l,entry.lower);if(r==null||!r.getBoolean("House"))return false;
        if(from.id.equals(r.getString("From")))return false;
        var owner=r.hasUUID("Owner")?p.server.getPlayerList().getPlayer(r.getUUID("Owner")):null;
        if(owner==null){knockLoose(l,entry.lower);return false;}
        return true;
    }
    static void stops(ServerPlayer p,LabyrinthData.Door from){
        p.serverLevel().playSound(null,from.lower,SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.6F,.7F);
        p.serverLevel().playSound(null,from.lower,SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.8F,.6F);
        PuzzleRooms.say(p,"The door gives an inch and stops against something on the other side.");
    }

    // ------------------------------------------------------------------ rooms people meet in (0.4.239)
    /**
     * The familiar kitchen is where readers find each other, so one reader's wedge must not shut it to everyone else. A reader whose
     * way in is its wedged door, from another hall, comes in by another of its doors instead (the side door, else the far one): the
     * wedge still holds the entry on the hall it was set from, and that hall still leads there for its owner. The door they came in by
     * is their way back while they are in the room, as an entry is.
     */
    static final Map<LabyrinthPlace,List<String>> OTHER_WAYS=Map.of(LabyrinthPlace.FAMILIAR_KITCHEN,List.of("side","far"));
    public static final String WAYS="wedge_ways_0239";
    /** How far the hall a reader came from is laid behind the other door: a short stretch, inside the room's own ground. */
    public static final int WAY_DEPTH=6,WAY_HALF_WIDTH=2;
    /**
     * The door a reader arriving through {@code from} comes in by: the entry as ever; another of the room's doors, if a wedge holds the
     * entry against them and the room is one people meet in; or nothing, when they are kept out (the door gives an inch and stops).
     */
    public static @Nullable LabyrinthData.Door way(ServerPlayer p,LabyrinthData.Door from,LabyrinthData.Door entry,LabyrinthPlace place){
        var data=LabyrinthData.get(p.server);
        if(!held(p,from,entry)){forgetWay(data,p.getUUID());return entry;}
        var other=otherWay(p.server,data,entry,place);
        if(other!=null){var r=new CompoundTag();r.putString("Place",place.id());r.putString("Door",other.id);data.setStateEntry(WAYS,p.getUUID().toString(),r);return other;}
        stops(p,from);
        return null;
    }
    /** Another door into a room people meet in, as an entry: a real door of the room, not itself wedged, facing out. */
    static @Nullable LabyrinthData.Door otherWay(net.minecraft.server.MinecraftServer server,LabyrinthData data,LabyrinthData.Door entry,LabyrinthPlace place){
        var names=OTHER_WAYS.get(place);if(names==null)return null;int copy=SceneInstances.copyOf(entry.id);
        for(var name:names){
            var door=data.door(SceneInstances.doorId(place,copy,name));if(door==null)continue;
            var l=server.getLevel(door.dimension);if(l==null||!l.isLoaded(door.lower)||at(l,door.lower)||!(l.getBlockState(door.lower).getBlock() instanceof DoorBlock))continue;
            return asEntry(door);
        }
        return null;
    }
    /** A way on, taken as a way in: the same door, facing the side it is come at from. */
    static LabyrinthData.Door asEntry(LabyrinthData.Door door){return new LabyrinthData.Door(door.id+"/in",door.dimension,door.lower,door.facing.getOpposite(),LabyrinthData.RETURN,false);}
    /** Whether this is a way on taken as a way in (see {@link #asEntry}). */
    public static boolean otherWay(LabyrinthData.Door door){return door.id.endsWith("/in")&&LabyrinthData.RETURN.equals(door.destination);}
    /** Without changing anything: whether a wedge holds this entry against someone coming through {@code from}, with its owner in the game. */
    static boolean holds(net.minecraft.server.MinecraftServer server,LabyrinthData.Door from,LabyrinthData.Door entry){
        var l=server.getLevel(entry.dimension);if(l==null)return false;var r=record(l,entry.lower);if(r==null||!r.getBoolean("House")||from.id.equals(r.getString("From")))return false;
        return r.hasUUID("Owner")&&server.getPlayerList().getPlayer(r.getUUID("Owner"))!=null;
    }
    /** Where someone coming through {@code from} would come out: the entry, or the other door of a room people meet in whose entry is held. */
    public static @Nullable LabyrinthData.Door wouldComeIn(net.minecraft.server.MinecraftServer server,LabyrinthData data,LabyrinthData.Door from,LabyrinthData.Door entry,LabyrinthPlace place){
        if(!holds(server,from,entry))return entry;return otherWay(server,data,entry,place);
    }
    /** Whether this door is one the reader came in by in place of a wedged entry (it is their way back, as an entry is). */
    public static boolean cameInBy(ServerPlayer p,LabyrinthData.Door door){
        var r=LabyrinthData.get(p.server).stateEntry(WAYS,p.getUUID().toString());return !r.isEmpty()&&(door.id+"/in").equals(r.getString("Door"));
    }
    /** The entry a reader in {@code place} goes back out by: the door they came in by, if it was not the entry. */
    public static LabyrinthData.Door entryFor(ServerPlayer p,LabyrinthPlace place,LabyrinthData.Door entry){
        var data=LabyrinthData.get(p.server);var r=data.stateEntry(WAYS,p.getUUID().toString());
        if(r.isEmpty()||!place.id().equals(r.getString("Place")))return entry;
        String id=r.getString("Door");if(!id.endsWith("/in"))return entry;var door=data.door(id.substring(0,id.length()-3));
        return door==null?entry:asEntry(door);
    }
    /** Whether a reader came into this room by another door than its entry. */
    public static boolean cameInOtherWay(LabyrinthData data,UUID reader,LabyrinthPlace place){var r=data.stateEntry(WAYS,reader.toString());return !r.isEmpty()&&place.id().equals(r.getString("Place"));}
    public static void forgetWay(LabyrinthData data,UUID reader){if(!data.stateEntry(WAYS,reader.toString()).isEmpty())data.setStateEntry(WAYS,reader.toString(),new CompoundTag());}
}
