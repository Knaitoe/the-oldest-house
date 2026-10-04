package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import io.github.knaitoe.theoldesthouse.network.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
/** Explicit, native, reviewable choices. A single world closure cannot consume the playable pool. */
public final class LiteraryCabinChoices {
    public static final String CLOSURE="literary_cabin_closure_0436";
    private LiteraryCabinChoices(){}
    /** A world closure cannot interrupt a known reader or retire the finale's shield route. */
    public static boolean canRetire(LabyrinthData data,LabyrinthPlace place){
        var story=WitnessAccount.Story.of(place.id());
        return place.isFinishable()&&story!=null&&place!=LabyrinthPlace.HOLLOWAY_CAMP
                &&place!=LabyrinthPlace.END_WORLD_CABIN&&place!=LabyrinthPlace.FAMILY_COPY
                &&data.visitorsTo(place).stream().allMatch(id->WitnessAccount.has(data,id,story));
    }
    public static void reconcileClosure(net.minecraft.server.MinecraftServer server){
        var data=LabyrinthData.get(server);var closure=data.state(CLOSURE);
        if(closure.getBoolean("Safety0437"))return;
        var place=LabyrinthPlace.byId(closure.getString("Retired"));
        if(place!=null&&!canRetire(data,place)){
            closure.putString("OriginalRetired",place.id());closure.remove("Retired");
            closure.putString("ReopenedReason","protected_route_or_unfinished_reader");
        }
        closure.putBoolean("Safety0437",true);data.setState(CLOSURE,closure);
    }
    public static void open(ServerPlayer p){if(!LiteraryVignettes.inside(p,LabyrinthPlace.END_WORLD_CABIN))return;var held=p.getMainHandItem().copy();var pet=p.serverLevel().getEntitiesOfClass(TamableAnimal.class,p.getBoundingBox().inflate(5),a->a.isAlive()&&p.getUUID().equals(CompanionOrders.owner(a))&&!a.getTags().contains(LiteraryCopies.PROJECTION)&&!a.isLeashed()&&!a.isPassenger()).stream().findFirst().orElse(null);
        var icons=new HashMap<Integer,ItemStack>();icons.put(0,LiteraryChoiceMenu.icon(Items.NAME_TAG,held.isEmpty()?"Offer a named item or enchanted tool in your hand":"Give up: "+held.getHoverName().getString()+" (kept permanently)"));if(pet!=null)icons.put(2,LiteraryChoiceMenu.icon(Items.BONE,"Give up: "+pet.getName().getString()+" (kept permanently)"));icons.put(4,LiteraryChoiceMenu.icon(Items.BARRIER,"Refuse; one optional room closes"));icons.put(8,LiteraryChoiceMenu.icon(Items.OAK_DOOR,"Leave the choice for now"));
        LiteraryChoiceMenu.open(p,"Choose what the keeper will hold",icons,()->LiteraryVignettes.inside(p,LabyrinthPlace.END_WORLD_CABIN),slot->{if(slot==8)return true;if(slot==0&&!ItemStack.matches(p.getMainHandItem(),held))return false;if(slot==2&&(pet==null||!pet.isAlive()||p.distanceToSqr(pet)>25||!p.getUUID().equals(CompanionOrders.owner(pet))))return false;return choose(p,slot,pet);});
    }
    public static boolean choose(ServerPlayer p,int slot,TamableAnimal pet){var place=LabyrinthPlace.END_WORLD_CABIN;if(!LiteraryVignettes.inside(p,place))return false;var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),place);if(own.getBoolean("ChoiceMade"))return false;var collection=MotherCollection.get(p.server);boolean offered=false;
        if(slot==0){var s=p.getMainHandItem();if(s.isEmpty()||MotherCollection.claimOf(s)!=null||!(s.has(DataComponents.CUSTOM_NAME)||WeaponHistory.weapon(s)&&s.isEnchanted()))return false;var entry=collection.sealVignetteItem(s.copyWithCount(1),p.registryAccess(),p.getUUID(),p.serverLevel().getGameTime());if(entry==null)return false;own.putUUID("Offering",entry.id);s.shrink(1);p.getInventory().setChanged();offered=true;}
        else if(slot==2){if(pet==null||!pet.isAlive()||p.distanceToSqr(pet)>25||!p.getUUID().equals(CompanionOrders.owner(pet))||pet.getTags().contains(LiteraryCopies.PROJECTION)||pet.isLeashed()||pet.isPassenger())return false;var tag=new CompoundTag();if(!pet.save(tag))return false;var entry=collection.sealVignettePet(pet.getUUID(),tag,p.getUUID(),pet.getName().getString());if(entry==null)return false;own.putUUID("Offering",entry.id);pet.discard();offered=true;}
        else if(slot==4){reconcileClosure(p.server);var closure=d.state(CLOSURE);String retired=closure.getString("Retired");if(retired.isEmpty()){var candidates=Arrays.stream(LabyrinthPlace.values()).filter(v->canRetire(d,v)).filter(v->!d.hasReturn(point->{var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(p.server).houseOrigin();var room=origin==null?null:LabyrinthPlaces.base(origin,v);return room!=null&&point.dimension().equals(NovelRooms.dimension(v))&&IndianLakeRooms.bounds(room,v).contains(point.pos());})).filter(v->p.server.getPlayerList().getPlayers().stream().noneMatch(other->at(other,v))).toList();if(candidates.isEmpty()){p.displayClientMessage(Component.literal("The remaining rooms are occupied or unfinished. The strangers wait."),true);return false;}var already=candidates.stream().filter(v->WitnessAccount.has(d,p.getUUID(),WitnessAccount.Story.of(v.id()))).toList();var chosen=(already.isEmpty()?candidates:already).get(p.getRandom().nextInt((already.isEmpty()?candidates:already).size()));retired=chosen.id();closure.putString("Retired",retired);closure.putUUID("Chooser",p.getUUID());d.setState(CLOSURE,closure);}own.putString("ClosedRoom",retired);own.putBoolean("Refused",true);p.displayClientMessage(Component.literal("The television shows "+retired.replace('_',' ')+". Its lights go out."),false);}
        else return false;
        captureScreen(p,own);own.putBoolean("ChoiceMade",true);own.putBoolean("Offered",offered);own.putInt("ChoiceTicks",0);LiteraryVignettes.save(d,p.getUUID(),place,own);return true;
    }
    /** A bounded native view of the actual room, saved with this reader's decision. */
    private static void captureScreen(ServerPlayer p,CompoundTag own){var d=LabyrinthData.get(p.server);var room=LabyrinthPlace.byId(own.getString("ClosedRoom"));
        if(room==null)room=d.visited(p.getUUID()).stream().map(LabyrinthPlace::byId).filter(Objects::nonNull).filter(v->v.room()!=null&&v!=LabyrinthPlace.END_WORLD_CABIN&&v!=LabyrinthPlace.FAMILY_COPY&&v!=LabyrinthPlace.OLD_CABIN).min(Comparator.comparingInt(v->{int age=d.recentVisit(p.getUUID(),v);return age<0?100+v.slot():age;})).orElse(LabyrinthPlace.MOTHER_DEN);
        var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(p.server).houseOrigin();var b=origin==null?null:LabyrinthPlaces.base(origin,room);var level=p.server.getLevel(NovelRooms.dimension(room));if(b==null||level==null)return;
        var eye=b.offset(0,1,-4).getCenter();int[] pixels=new int[384];for(int y=0;y<16;y++)for(int x=0;x<24;x++){var ray=new Vec3((x-11.5)/18D,(7.5-y)/20D,-1).normalize();var hit=level.clip(new ClipContext(eye,eye.add(ray.scale(96)),ClipContext.Block.COLLIDER,ClipContext.Fluid.ANY,p));int color=0x111519;if(hit.getType()!=HitResult.Type.MISS){var at=hit.getBlockPos();var state=level.getBlockState(at);color=state.getMapColor(level,at).col;double shade=Math.max(.32,1-eye.distanceTo(hit.getLocation())/115)*(hit.getDirection().getAxis()==net.minecraft.core.Direction.Axis.Y?.88:1);color=((int)((color>>16&255)*shade)<<16)|((int)((color>>8&255)*shade)<<8)|(int)((color&255)*shade);}pixels[y*24+x]=color;}
        own.putIntArray("Screen",pixels);own.putString("ScreenRoom",room.id());
    }
    private static boolean at(ServerPlayer p,LabyrinthPlace place){if(!p.level().dimension().equals(NovelRooms.dimension(place)))return false;var o=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(p.server).houseOrigin();var b=o==null?null:LabyrinthPlaces.base(o,place);return b!=null&&IndianLakeRooms.bounds(b,place).contains(p.position());}
    public static void tick(ServerPlayer p,CompoundTag own){var place=LabyrinthPlace.END_WORLD_CABIN;var b=LiteraryVignettes.base(p,place);for(int i=0;i<4;i++){var actor=LiteraryVignettes.actor(p,place,"Stranger"+i,LiteraryActor.STRANGER,new net.minecraft.core.BlockPos(-5+i*3,0,-10),false);if(actor!=null){actor.appearance(LiteraryActor.STRANGER,i);if(own.getBoolean("Refused")&&i==3){actor.move(net.minecraft.world.entity.MoverType.SELF,new net.minecraft.world.phys.Vec3(0,0,-.12));if(actor.getZ()<b.getZ()-45)actor.setNoGravity(false);}else if(own.getInt("Present")%120<5)actor.say(own.getBoolean("ChoiceMade")?"Thank you. We heard your answer.":"We would like you to choose.");}}
        if(own.getBoolean("ChoiceMade")){if(own.getIntArray("Screen").length!=384)captureScreen(p,own);own.putInt("ChoiceTicks",own.getInt("ChoiceTicks")+5);var display=new CompoundTag();display.putUUID("Reader",p.getUUID());display.putIntArray("Screen",own.getIntArray("Screen"));display.putString("ScreenRoom",own.getString("ScreenRoom"));display.putBoolean("ScreenDark",own.getBoolean("Refused")&&own.getInt("ChoiceTicks")>=150);HousePackets.send(p,new LiteraryViewPayload(-1,30,0,display));if(own.getInt("ChoiceTicks")>=200&&p.distanceToSqr(b.offset(LiteraryRooms.TV).getCenter())<49&&HouseWatchers.sees(p,b.offset(LiteraryRooms.TV).getCenter()))LiteraryVignettes.ready(p,place,own,own.getBoolean("Offered")?"gave_an_original_to_permanent_sealed_custody":"refused_and_examined_the_closed_rooms_screen");}
    }
}
