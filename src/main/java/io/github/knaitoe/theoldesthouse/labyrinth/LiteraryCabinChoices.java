package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import javax.annotation.Nullable;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.network.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;
/**
 * What a refusal at the cabin closes, and the answers given before the bargain. Since 0.4.51 a refusal closes one room
 * for its reader alone; the single world-wide closure of 0.4.36 became the personal closure of the reader who chose it.
 */
public final class LiteraryCabinChoices {
    public static final String CLOSURE="literary_cabin_closure_0436",PERSONAL="literary_cabin_closures_0451";
    private LiteraryCabinChoices(){}
    /** A world closure cannot interrupt a known reader or retire the finale's shield route (kept for the 0.4.37 repair). */
    public static boolean canRetire(LabyrinthData data,LabyrinthPlace place){
        var story=WitnessAccount.Story.of(place.id());
        return place.isFinishable()&&story!=null&&place!=LabyrinthPlace.HOLLOWAY_CAMP
                &&place!=LabyrinthPlace.END_WORLD_CABIN&&place!=LabyrinthPlace.FAMILY_COPY
                &&data.visitorsTo(place).stream().allMatch(id->WitnessAccount.has(data,id,story));
    }
    public static void reconcileClosure(net.minecraft.server.MinecraftServer server){
        var data=LabyrinthData.get(server);var closure=data.state(CLOSURE);
        if(closure.getBoolean("Safety0437")&&closure.getBoolean("Personal0451"))return;
        if(!closure.getBoolean("Safety0437")){
            var place=LabyrinthPlace.byId(closure.getString("Retired"));
            if(place!=null&&!canRetire(data,place)){
                closure.putString("OriginalRetired",place.id());closure.remove("Retired");
                closure.putString("ReopenedReason","protected_route_or_unfinished_reader");
            }
            closure.putBoolean("Safety0437",true);
        }
        // One reader's refusal no longer closes a room for everyone: it stays closed for the reader who refused.
        var retired=closure.getString("Retired");
        if(!retired.isEmpty()){if(closure.hasUUID("Chooser"))close(data,closure.getUUID("Chooser"),LabyrinthPlace.byId(retired));closure.putString("WorldRetired0436",retired);closure.remove("Retired");}
        closure.putBoolean("Personal0451",true);data.setState(CLOSURE,closure);
    }
    /** Closed for this reader: by their own refusal, or by the world closure of a save that has not reconciled yet. */
    public static boolean closed(LabyrinthData d,UUID reader,LabyrinthPlace place){
        if(place==null)return false;
        return d.state(CLOSURE).getString("Retired").equals(place.id())||d.stateEntry(PERSONAL,reader.toString()).getString("Room").equals(place.id());
    }
    public static void close(LabyrinthData d,UUID reader,@Nullable LabyrinthPlace place){
        if(place==null)return;var own=d.stateEntry(PERSONAL,reader.toString());if(!own.getString("Room").isEmpty())return;
        own.putString("Room",place.id());d.setStateEntry(PERSONAL,reader.toString(),own);
    }
    /**
     * The room a refusal closes for this reader: the unfinished room they most recently walked out of; otherwise an
     * unfinished room they have not reached; only when nothing is left unfinished, a finished one. Never the finale's
     * shield route, the cabin itself or a family copy; never a room holding one of their saved ways back or their own
     * animals; never one that would leave their account short of its quota; never the elk lot while the elk fan, which it
     * opens, is still unfinished.
     */
    public static @Nullable LabyrinthPlace personalClosure(ServerPlayer p){
        var d=LabyrinthData.get(p.server);UUID id=p.getUUID();
        var all=Arrays.stream(LabyrinthPlace.values()).filter(v->eligible(d,p,v)).toList();
        var unfinished=all.stream().filter(v->!WitnessAccount.has(d,id,WitnessAccount.Story.of(v.id()))).toList();
        var seen=d.visited(id);
        var left=unfinished.stream().filter(v->seen.contains(v.id())).min(Comparator.comparingInt(v->{int age=d.recentVisit(id,v);return age<0?1000+v.slot():age;}));
        if(left.isPresent())return left.get();
        var unseen=unfinished.stream().filter(v->LabyrinthBuilder.isPlaceReady(d,v)).toList();
        if(!unseen.isEmpty())return unseen.get(p.getRandom().nextInt(unseen.size()));
        if(!unfinished.isEmpty())return unfinished.get(p.getRandom().nextInt(unfinished.size()));
        return all.isEmpty()?null:all.get(p.getRandom().nextInt(all.size()));
    }
    static boolean eligible(LabyrinthData d,ServerPlayer p,LabyrinthPlace v){
        var story=WitnessAccount.Story.of(v.id());UUID id=p.getUUID();
        if(story==null||!v.isFinishable()||v==LabyrinthPlace.HOLLOWAY_CAMP||v==LabyrinthPlace.END_WORLD_CABIN||v==LabyrinthPlace.FAMILY_COPY||v==LabyrinthPlace.OLD_CABIN||closed(d,id,v))return false;
        var fan=WitnessAccount.Story.of(LabyrinthPlace.ELK_FAN.id());
        if(v==LabyrinthPlace.ELK_LOT&&fan!=null&&!WitnessAccount.has(d,id,fan))return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();var room=origin==null?null:LabyrinthPlaces.base(origin,v);
        if(room!=null){var bounds=IndianLakeRooms.bounds(room,v);
            if(d.hasReturn(id,point->point.dimension().equals(NovelRooms.dimension(v))&&bounds.contains(point.pos())))return false;
            var level=p.server.getLevel(NovelRooms.dimension(v));
            if(level!=null&&!level.getEntitiesOfClass(TamableAnimal.class,bounds,a->id.equals(a.getOwnerUUID())).isEmpty())return false;}
        return reachableWithout(d,id,story);
    }
    /** Whether the reader's account can still reach its quota across two kinds with this story closed to them. */
    static boolean reachableWithout(LabyrinthData d,UUID id,WitnessAccount.Story closing){
        int count=0;var kinds=new HashSet<String>();
        for(var s:WitnessAccount.Story.values()){boolean has=WitnessAccount.has(d,id,s);if(!has&&(s==closing||closed(d,id,LabyrinthPlace.byId(s.id))))continue;count++;kinds.add(s.kind);}
        return count>=WitnessAccount.REQUIRED&&kinds.size()>=2;
    }

    /** A reader who gave an object or a pet, or refused, before 0.4.51: their screen and their account, unchanged. */
    public static void legacyTick(ServerPlayer p,BlockPos b,CompoundTag own){
        var place=LabyrinthPlace.END_WORLD_CABIN;
        if(own.getIntArray(CabinScreen.LEGACY).length!=CabinScreen.PIXELS){
            if(!CabinScreen.pending(p.getUUID(),CabinScreen.LEGACY)){var room=legacyRoom(p,own);if(room==null||!CabinScreen.requestRoom(p,room,CabinScreen.LEGACY))own.putIntArray(CabinScreen.LEGACY,CabinScreen.blank());}
            return;
        }
        own.putInt("ChoiceTicks",own.getInt("ChoiceTicks")+5);var display=new CompoundTag();display.putUUID("Reader",p.getUUID());display.putIntArray("Screen",own.getIntArray(CabinScreen.LEGACY));display.putString("ScreenRoom",own.getString("ScreenRoom"));display.putBoolean("ScreenDark",own.getBoolean("Refused")&&own.getInt("ChoiceTicks")>=150);HousePackets.send(p,new LiteraryViewPayload(-1,30,0,display));
        var tv=b.offset(LiteraryRooms.TV).getCenter();
        if(own.getInt("ChoiceTicks")>=200&&p.distanceToSqr(tv)<49&&HouseWatchers.sees(p,tv))LiteraryVignettes.ready(p,place,own,own.getBoolean("Offered")?"gave_an_original_to_permanent_sealed_custody":"refused_and_examined_the_closed_rooms_screen");
    }
    private static @Nullable LabyrinthPlace legacyRoom(ServerPlayer p,CompoundTag own){
        var room=LabyrinthPlace.byId(own.getString("ClosedRoom"));if(room!=null)return room;var d=LabyrinthData.get(p.server);
        return d.visited(p.getUUID()).stream().map(LabyrinthPlace::byId).filter(Objects::nonNull).filter(v->v.room()!=null&&v!=LabyrinthPlace.END_WORLD_CABIN&&v!=LabyrinthPlace.FAMILY_COPY&&v!=LabyrinthPlace.OLD_CABIN).min(Comparator.comparingInt(v->{int age=d.recentVisit(p.getUUID(),v);return age<0?100+v.slot():age;})).orElse(LabyrinthPlace.MOTHER_DEN);
    }
}
