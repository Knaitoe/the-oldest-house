package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
/** Private frozen native homes. Capture and construction resume from saved cursors; originals never refill. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LiteraryCopies extends SavedData {
    public static final String PROJECTION="LiteraryCopyProjection",HEIRLOOM="LiteraryCopyHeirloom";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static final Factory<LiteraryCopies> FACTORY=new Factory<>(LiteraryCopies::new,LiteraryCopies::load);
    private static final class Copy {
        final UUID reader;final LabyrinthPlace place;final int index;final BlockPos bed;final long day;RoomSnapshot snapshot;RoomSnapshot.Builder capture;int capturedColumns,buildCursor;boolean captured,built;CompoundTag meta=new CompoundTag();ListTag entities=new ListTag();
        Copy(UUID reader,LabyrinthPlace p,int index,BlockPos bed,long day){this.reader=reader;place=p;this.index=index;this.bed=bed.immutable();this.day=day;int half=p==LabyrinthPlace.FAMILY_COPY?32:8,below=p==LabyrinthPlace.FAMILY_COPY?16:8,height=p==LabyrinthPlace.FAMILY_COPY?48:24;capture=new RoomSnapshot.Builder(new BlockPos(-half,-below,-half),new BlockPos(half-1,height-below-1,half-1),Blocks.AIR.defaultBlockState());}
        int offset(){return meta.contains("SceneOffset")?meta.getInt("SceneOffset"):place==LabyrinthPlace.FAMILY_COPY?-34:-16;}
        String key(){return reader+"/"+place.id();}
    }
    private final Map<String,Copy> copies=new LinkedHashMap<>();private final Map<UUID,CompoundTag> readers=new LinkedHashMap<>();private int nextIndex;
    public static LiteraryCopies get(MinecraftServer s){return s.overworld().getDataStorage().computeIfAbsent(FACTORY,"the_oldest_house_literary_copies");}
    private Copy copy(UUID reader,LabyrinthPlace p){return copies.get(reader+"/"+p.id());}
    private static BlockPos address(MinecraftServer s,Copy c){var o=HouseSavedData.get(s).houseOrigin();if(o==null)return null;var b=LabyrinthPlaces.base(o,c.place);return b==null?null:new BlockPos(b.getX()+4096+c.index*256,80,b.getZ());}
    public static @Nullable BlockPos base(MinecraftServer s,UUID reader,LabyrinthPlace p){var c=get(s).copy(reader,p);return c==null||!c.built?null:address(s,c);}
    public static @Nullable LabyrinthPlace placeAt(MinecraftServer s,BlockPos at){for(var c:get(s).copies.values()){var b=address(s,c);if(b!=null&&c.built&&bounds(b,c).contains(Vec3.atCenterOf(at)))return c.place;}return null;}
    private static AABB bounds(BlockPos b,Copy c){var min=c.snapshot==null?new BlockPos(-34,-17,-34):c.snapshot.min();var max=c.snapshot==null?new BlockPos(34,33,34):c.snapshot.max();return new AABB(b.getX()+min.getX()-3,b.getY()+min.getY()-3,b.getZ()+min.getZ()+c.offset()-3,b.getX()+max.getX()+4,b.getY()+max.getY()+4,b.getZ()+10);}
    public static boolean contains(MinecraftServer s,UUID reader,LabyrinthPlace place,Vec3 point){var c=get(s).copy(reader,place);var b=c==null?null:address(s,c);return c!=null&&c.built&&b!=null&&bounds(b,c).contains(point);}
    public static boolean protectedPosition(MinecraftServer s,BlockPos at){for(var c:get(s).copies.values()){var b=address(s,c);if(b!=null&&bounds(b,c).contains(Vec3.atCenterOf(at)))return true;}return false;}
    public static List<AABB> outdoorAreas(MinecraftServer server) {
        List<AABB> areas = new ArrayList<>();
        for (var copy : get(server).copies.values()) {
            var base = address(server, copy);
            if (copy.built && base != null) areas.add(bounds(base, copy));
        }
        return areas;
    }

    private static void markProjectionItem(ItemStack item){
        CustomData.update(DataComponents.CUSTOM_DATA,item,tag->{tag.remove(WeaponHistory.ORIGINAL);tag.putBoolean(WeaponHistory.COPY,true);});
    }

    /** Native equipment stays visible, but neither interaction packet can exchange it. */
    private static void freezeProjection(Entity entity) {
        entity.setInvulnerable(true);
        if (entity instanceof LivingEntity living) {
            for (var slot : EquipmentSlot.values()) {
                var item = living.getItemBySlot(slot);
                if (!item.isEmpty()) markProjectionItem(item);
            }
        }
        if (entity instanceof ItemFrame frame) {
            if (!frame.getItem().isEmpty()) markProjectionItem(frame.getItem());
            fixFrame(frame);
        }
        if (entity instanceof ArmorStand stand) {
            stand.setNoGravity(true);
            var tag = new CompoundTag();
            stand.saveWithoutId(tag);
            tag.putInt("DisabledSlots", 0x7fffffff);
            stand.readAdditionalSaveData(tag);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void repairProjection(EntityJoinLevelEvent event) {
        if (!event.isCanceled() && event.getLevel() instanceof ServerLevel
                && event.getEntity().getTags().contains(PROJECTION)) freezeProjection(event.getEntity());
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void specificProjection(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getTarget().getTags().contains(PROJECTION)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void projectionAttack(AttackEntityEvent event) {
        if (event.getTarget().getTags().contains(PROJECTION)) event.setCanceled(true);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void projectionDrops(LivingDropsEvent event) {
        if (event.getEntity().getTags().contains(PROJECTION)) { event.getDrops().clear(); event.setCanceled(true); }
    }
    public static @Nullable LabyrinthData.Door prepareEntry(ServerPlayer p,LabyrinthPlace place){var c=get(p.server).copy(p.getUUID(),place);if(c==null||!c.built||place==LabyrinthPlace.FAMILY_COPY&&p.server.overworld().getDayTime()/24000-c.day<14)return null;return LabyrinthData.get(p.server).door(c.key()+"/entry");}
    public static @Nullable LabyrinthData.Door entry(ServerPlayer p,LabyrinthPlace place){var c=get(p.server).copy(p.getUUID(),place);return c==null?null:LabyrinthData.get(p.server).door(c.key()+"/entry");}
    public static void tick(MinecraftServer server){var data=get(server);var lab=LabyrinthData.get(server);for(var p:server.getPlayerList().getPlayers()){
            if(!LiteraryVignettes.participant(p))continue;var own=data.readers.computeIfAbsent(p.getUUID(),id->new CompoundTag());if(HouseDimensions.isHouseDimension(p.level().dimension())||!lab.visited(p.getUUID()).isEmpty()){if(!own.getBoolean("HouseSeen")){own.putBoolean("HouseSeen",true);data.setDirty();}}
            if(!p.level().dimension().equals(Level.OVERWORLD)||!p.isSleeping()||p.getSleepingPos().isEmpty())continue;var bed=p.getSleepingPos().get();long day=server.overworld().getDayTime()/24000;boolean newNight=own.getLong("SleepDay")!=day+1;if(newNight){own.putLong("SleepDay",day+1);own.putInt("Nights",own.getInt("Nights")+1);data.setDirty();}
            if(data.copy(p.getUUID(),LabyrinthPlace.OLD_CABIN)==null){var c=new Copy(p.getUUID(),LabyrinthPlace.OLD_CABIN,data.nextIndex++,bed,day);data.copies.put(c.key(),c);data.setDirty();}
            if(newNight&&own.getBoolean("HouseSeen")&&(own.getInt("Nights")>=3||own.contains("Front"))&&hasHomeDoor(server.overworld(),bed)&&data.copy(p.getUUID(),LabyrinthPlace.FAMILY_COPY)==null){var c=new Copy(p.getUUID(),LabyrinthPlace.FAMILY_COPY,data.nextIndex++,bed,day);if(own.contains("Front"))c.meta.putLong("OriginalFront",own.getLong("Front"));data.copies.put(c.key(),c);data.setDirty();}
        }
        int budget=2,buildBudget=4096;for(var c:data.copies.values()){
            if(!c.captured&&budget>0){int used=data.capture(server,c,budget);budget-=used;}
            if(c.captured&&!c.built&&buildBudget>0){int used=data.construct(server,c,buildBudget);buildBudget-=used;}
            if(c.built){boolean ready=c.place==LabyrinthPlace.OLD_CABIN||server.overworld().getDayTime()/24000-c.day>=14;String key="literary_copy_"+c.reader+"_"+c.place.id();if(lab.isReady(key)!=ready)lab.setReady(key,ready);}
        }
    }
    private int capture(MinecraftServer s,Copy c,int budget){var l=s.overworld();int half=c.place==LabyrinthPlace.FAMILY_COPY?32:8,below=c.place==LabyrinthPlace.FAMILY_COPY?16:8,height=c.place==LabyrinthPlace.FAMILY_COPY?48:24;int size=half*2;int minX=c.bed.getX()-half,minZ=c.bed.getZ()-half;int cols=((minX+size-1)>>4)-(minX>>4)+1,rows=((minZ+size-1)>>4)-(minZ>>4)+1,total=cols*rows,used=0;
        while(c.capturedColumns<total&&used<budget){int cx=(minX>>4)+c.capturedColumns%cols,cz=(minZ>>4)+c.capturedColumns/cols;l.getChunk(cx,cz);for(int x=Math.max(minX,cx*16);x<Math.min(minX+size,cx*16+16);x++)for(int z=Math.max(minZ,cz*16);z<Math.min(minZ+size,cz*16+16);z++)for(int y=c.bed.getY()-below;y<c.bed.getY()+height-below;y++){
                var at=new BlockPos(x,y,z);var state=l.getBlockState(at);var be=l.getBlockEntity(at);CompoundTag tag=null;if(be instanceof SignBlockEntity||be instanceof BannerBlockEntity||be instanceof SkullBlockEntity)tag=be.saveWithFullMetadata(l.registryAccess());c.capture.set(at.subtract(c.bed),state,tag);
                if(state.getBlock() instanceof DoorBlock&&state.getValue(DoorBlock.HALF)==DoubleBlockHalf.LOWER&&(!c.meta.contains("Front")||c.meta.contains("OriginalFront")&&c.meta.getLong("OriginalFront")==at.asLong())){c.meta.putLong("Front",at.subtract(c.bed).asLong());c.meta.putString("FrontFacing",state.getValue(DoorBlock.FACING).getName());}if(state.getBlock() instanceof BedBlock&&!c.meta.contains("Bedroom"))c.meta.putLong("Bedroom",at.subtract(c.bed).asLong());if(state.is(Blocks.CRAFTING_TABLE)&&!c.meta.contains("Kitchen"))c.meta.putLong("Kitchen",at.subtract(c.bed).asLong());}
            c.capturedColumns++;used++;setDirty();}
        if(c.capturedColumns>=total){c.snapshot=c.capture.build(s.overworld().getGameTime(),c.day,c.bed.asLong());if(c.place==LabyrinthPlace.FAMILY_COPY&&c.meta.contains("Front"))c.meta.putInt("SceneOffset",Math.min(-34,-14-BlockPos.of(c.meta.getLong("Front")).getZ()));c.captured=true;c.capture=null;var box=new AABB(Vec3.atLowerCornerOf(c.bed.offset(-half,-below,-half)),Vec3.atLowerCornerOf(c.bed.offset(half,height-below,half)));for(var e:l.getEntities((Entity)null,box,e->e instanceof TamableAnimal||e instanceof net.minecraft.world.entity.animal.Animal||e instanceof ItemFrame||e instanceof ArmorStand)){if(c.entities.size()>=48)break;var tag=new CompoundTag();if(e.save(tag)){tag.remove("UUID");tag.remove("Owner");tag.remove("Leash");tag.remove("Passengers");tag.putDouble("CopyX",e.getX()-c.bed.getX());tag.putDouble("CopyY",e.getY()-c.bed.getY());tag.putDouble("CopyZ",e.getZ()-c.bed.getZ());c.entities.add(tag);}}setDirty();}
        return used;
    }
    private static boolean hasHomeDoor(ServerLevel l,BlockPos bed){for(var at:BlockPos.betweenClosed(bed.offset(-24,-8,-24),bed.offset(24,8,24)))if(l.getBlockState(at).getBlock() instanceof DoorBlock)return true;return false;}
    /** A grey walk ends outside the captured door. It adds supports only in air and never carves originals. */
    private static void approach(ServerLevel l,BlockPos b,Copy c){var target=front(c);var face=Direction.byName(c.meta.getString("FrontFacing"));if(face==null)face=Direction.SOUTH;var destination=target.relative(face,2);if(!clear(l,b.offset(destination)))destination=target.relative(face.getOpposite(),2);var start=new BlockPos(0,0,-9);var todo=new ArrayDeque<BlockPos>();var previous=new HashMap<BlockPos,BlockPos>();todo.add(start);previous.put(start,start);int count=0;while(!todo.isEmpty()&&count++<24000){var at=todo.remove();if(at.equals(destination))break;for(var side:Direction.Plane.HORIZONTAL)for(int dy:new int[]{0,-1,1}){var next=at.relative(side).offset(0,dy,0);if(Math.abs(next.getX())>35||next.getZ()>-8||next.getZ()<c.offset()-35||next.getY()<-16||next.getY()>31||previous.containsKey(next)||!clear(l,b.offset(next)))continue;previous.put(next,at);todo.add(next);}}if(!previous.containsKey(destination))return;var at=destination;while(!at.equals(start)){var floor=b.offset(at).below();if(l.getBlockState(floor).isAir())l.setBlock(floor,Blocks.GRAY_CONCRETE.defaultBlockState(),F);at=previous.get(at);}}
    private static boolean clear(ServerLevel l,BlockPos at){return l.getBlockState(at).getCollisionShape(l,at).isEmpty()&&l.getBlockState(at.above()).getCollisionShape(l,at.above()).isEmpty();}
    private static void fixFrame(ItemFrame frame){var t=new CompoundTag();frame.saveWithoutId(t);t.putBoolean("Fixed",true);frame.readAdditionalSaveData(t);frame.setInvulnerable(true);}
    private static BlockState freeze(BlockState s){if(s.hasProperty(BlockStateProperties.POWERED))s=s.setValue(BlockStateProperties.POWERED,false);if(s.hasProperty(BlockStateProperties.POWER))s=s.setValue(BlockStateProperties.POWER,0);if(s.hasProperty(BlockStateProperties.ENABLED))s=s.setValue(BlockStateProperties.ENABLED,false);if(s.hasProperty(BlockStateProperties.LIT)&&s.getBlock() instanceof RedstoneLampBlock)s=s.setValue(BlockStateProperties.LIT,false);return s;}
    private int construct(MinecraftServer s,Copy c,int budget){var b=address(s,c);var l=s.getLevel(HouseDimensions.OUTSIDE);if(b==null||l==null)return 0;var min=c.snapshot.min();var max=c.snapshot.max();int sx=max.getX()-min.getX()+1,sz=max.getZ()-min.getZ()+1,used=0;int offset=c.offset();
        while(c.buildCursor<c.snapshot.blockCount()&&used<budget){int i=c.buildCursor++;var rel=new BlockPos(min.getX()+i%sx,min.getY()+i/(sx*sz),min.getZ()+(i/sx)%sz);var at=b.offset(rel).offset(0,0,offset);var state=c.snapshot.stateAt(rel);boolean frozen=LiteraryFrozenBlock.freezes(state);l.setBlock(at,frozen?LiteraryRegistry.FROZEN.get().defaultBlockState():freeze(state),F);if(frozen&&l.getBlockEntity(at) instanceof LiteraryModelBlockEntity display){var nativeState=new CompoundTag();nativeState.put("FrozenState",NbtUtils.writeBlockState(state));display.display(nativeState);}var tag=c.snapshot.blockEntityAt(rel);var be=l.getBlockEntity(at);if(!frozen&&tag!=null&&be!=null){tag=tag.copy();tag.putInt("x",at.getX());tag.putInt("y",at.getY());tag.putInt("z",at.getZ());be.loadWithComponents(tag,l.registryAccess());be.setChanged();}if(be instanceof net.minecraft.world.Container container)container.clearContent();used++;}setDirty();
        if(c.buildCursor>=c.snapshot.blockCount()){
            for(int i=0;i<c.entities.size();i++){var tag=c.entities.getCompound(i).copy();tag.putUUID("UUID",UUID.randomUUID());var target=BlockPos.containing(b.getX()+tag.getDouble("CopyX"),b.getY()+tag.getDouble("CopyY"),b.getZ()+offset+tag.getDouble("CopyZ"));if(tag.contains("TileX")){tag.putInt("TileX",target.getX());tag.putInt("TileY",target.getY());tag.putInt("TileZ",target.getZ());}var e=EntityType.loadEntityRecursive(tag,l,entity->entity);if(e==null)continue;e.moveTo(b.getX()+tag.getDouble("CopyX"),b.getY()+tag.getDouble("CopyY"),b.getZ()+offset+tag.getDouble("CopyZ"),e.getYRot(),e.getXRot());e.addTag(PROJECTION);e.setInvulnerable(true);if(e instanceof Mob mob){mob.setNoAi(true);mob.setPersistenceRequired();}if(e instanceof TamableAnimal tame)tame.setOwnerUUID(null);if(e instanceof ItemFrame frame){var item=frame.getItem();if(WeaponHistory.weapon(item))markProjectionItem(item);frame.setItem(item,false);fixFrame(frame);}if(e instanceof ArmorStand stand){stand.setNoGravity(true);for(var slot:EquipmentSlot.values()){var item=stand.getItemBySlot(slot);if(WeaponHistory.weapon(item))markProjectionItem(item);}}freezeProjection(e);l.addFreshEntity(e);}
            LiteraryRooms.box(l,b,-4,-1,-9,4,-1,4,Blocks.SMOOTH_STONE);LiteraryRooms.box(l,b,-4,0,-8,4,3,-1,Blocks.AIR);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,Blocks.GRAY_TERRACOTTA.defaultBlockState(),Blocks.SMOOTH_STONE.defaultBlockState(),Blocks.GRAY_TERRACOTTA.defaultBlockState());NovelRooms.door(l,b.offset(0,0,1),Direction.SOUTH,Blocks.DARK_OAK_DOOR,false);
            LiteraryRooms.paper(l,b,LiteraryRooms.SOURCE,LiteraryTexts.source(c.place));LiteraryRooms.prop(l,b,LiteraryRooms.ending(c.place),LiteraryPropBlock.Kind.LEDGER,Direction.SOUTH);
            LabyrinthData.get(s).putDoor(new LabyrinthData.Door(c.key()+"/entry",HouseDimensions.OUTSIDE,b.offset(0,0,1),Direction.SOUTH,LabyrinthData.RETURN,false));if(c.place==LabyrinthPlace.FAMILY_COPY)approach(l,b,c);c.built=true;setDirty();
        }return used;
    }
    public static void arrived(ServerPlayer p,LabyrinthPlace place){var c=get(p.server).copy(p.getUUID(),place);if(c==null||!c.built)return;var b=address(p.server,c);if(place==LabyrinthPlace.OLD_CABIN){var a=LiteraryVignettes.actor(p,place,"OldMan",LiteraryActor.OLD_MAN,new BlockPos(1,0,-15),true);if(a!=null){var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),place);String[] names={"Elias","Samuel","Joseph","Daniel","Thomas","Isaac","Jonah","Arthur"};own.putString("Name",names[Math.floorMod(own.getInt("Visit")-1,names.length)]);own.putInt("Beat",own.getInt("Visit"));a.say("My name is "+own.getString("Name")+". This was your room once.");LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),place,own);}}}
    private static BlockPos front(Copy c){return c.meta.contains("Front")?BlockPos.of(c.meta.getLong("Front")).offset(0,0,c.offset()):new BlockPos(0,0,-12);}
    public static void knock(ServerPlayer p,BlockPos at){if(!LiteraryVignettes.inside(p,LabyrinthPlace.FAMILY_COPY))return;var c=get(p.server).copy(p.getUUID(),LabyrinthPlace.FAMILY_COPY);if(c==null||at.distManhattan(address(p.server,c).offset(front(c)))>1)return;var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.FAMILY_COPY);own.putBoolean("Knocked",true);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.FAMILY_COPY,own);var a=LiteraryVignettes.actor(p,LabyrinthPlace.FAMILY_COPY,"Father",LiteraryActor.FAMILY_FATHER,front(c).north(2),true);if(a!=null)a.say("What do you want? This is our house.");p.serverLevel().playSound(null,at,net.minecraft.sounds.SoundEvents.WOOD_HIT,net.minecraft.sounds.SoundSource.BLOCKS,.6F,1);}
    public static boolean click(ServerPlayer p,LabyrinthPlace place,BlockPos at,CompoundTag own){if(place==LabyrinthPlace.OLD_CABIN)return false;var c=get(p.server).copy(p.getUUID(),place);if(c==null)return false;var b=address(p.server,c);if(at.distManhattan(b.offset(front(c)))<=1){if(!own.getBoolean("Admitted")){own.putBoolean("Knocked",true);knock(p,at);talk(p,place,LiteraryVignettes.actor(p,place,"Father",LiteraryActor.FAMILY_FATHER,front(c).north(2),true),own);return true;}}
        if(c.meta.contains("Extra")&&at.distManhattan(b.offset(BlockPos.of(c.meta.getLong("Extra"))))<=1){if(own.getInt("Visit")>=5){own.putBoolean("FoundChild",true);own.putInt("Beat",5);NovelRooms.door(p.serverLevel(),at,Direction.SOUTH,Blocks.IRON_DOOR,true);}else p.displayClientMessage(Component.literal("You did not build this door."),true);return true;}return false;}
    public static void talk(ServerPlayer p,LabyrinthPlace place,@Nullable LiteraryActor a,CompoundTag own){if(a==null)return;if(place==LabyrinthPlace.OLD_CABIN){WeaponHistory.seed(p);var favorite=WeaponHistory.favorite(p);var hand=p.getMainHandItem();String words=hand.isEmpty()?"Bring the weapon that remembers the most of you.":WeaponHistory.wounds(hand,favorite)?"This one is the original. It has been with you.":hand.has(DataComponents.CUSTOM_DATA)&&hand.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean(WeaponHistory.COPY)?"This one is the House's copy. It has no days in it.":"That weapon is real, but it is not the one I am waiting for.";a.say(words);p.displayClientMessage(Component.literal(words),false);return;}
        var offered=p.getMainHandItem().copy();Map<Integer,ItemStack> icons=new HashMap<>();icons.put(0,LiteraryChoiceMenu.icon(Items.OAK_DOOR,own.getInt("Visit")==8?"I will leave":"May I come in?"));if(own.getInt("Visit")==4){icons.put(2,LiteraryChoiceMenu.icon(Items.SUSPICIOUS_STEW,"Accept dinner"));icons.put(4,LiteraryChoiceMenu.icon(Items.BOWL,"Refuse dinner"));}if(own.getInt("Visit")==8)icons.put(4,LiteraryChoiceMenu.icon(Items.BARRIER,"I am staying"));
        LiteraryChoiceMenu.open(p,"The father's welcome",icons,()->LiteraryVignettes.inside(p,place)&&p.distanceToSqr(a)<36,slot->{var d=LabyrinthData.get(p.server);var now=LiteraryVignettes.personal(d,p.getUUID(),place);var c=get(p.server).copy(p.getUUID(),place);var b=address(p.server,c);boolean done=false;
            if(slot==0&&now.getInt("Visit")==8&&now.getBoolean("AskedToLeave")){LiteraryVignettes.ready(p,place,now,"left_when_the_family_asked");done=true;}
            else if(slot==0&&now.getBoolean("Knocked")){now.putBoolean("Admitted",true);var frontState=p.serverLevel().getBlockState(b.offset(front(c)));if(frontState.getBlock() instanceof DoorBlock){p.serverLevel().setBlock(b.offset(front(c)),frontState.setValue(DoorBlock.OPEN,true),3);var top=p.serverLevel().getBlockState(b.offset(front(c)).above());if(top.getBlock() instanceof DoorBlock)p.serverLevel().setBlock(b.offset(front(c)).above(),top.setValue(DoorBlock.OPEN,true),3);}a.say("A quick look. We have lived here a long time.");done=true;}
            else if(slot==2&&now.getInt("Visit")==4&&!now.getBoolean("DinnerGiven")){var meal=LiteraryVignettes.mark(p,place,"Dinner",new ItemStack(LiteraryRegistry.STEW.get()));UUID token=UUID.randomUUID();CustomData.update(DataComponents.CUSTOM_DATA,meal,t->t.putUUID("MealToken",token));now.putUUID("MealToken",token);now.putBoolean("DinnerGiven",true);LiteraryVignettes.give(p,meal);done=true;}
            else if(slot==4&&now.getInt("Visit")==4){now.putBoolean("DinnerRefused",true);now.putInt("Beat",4);a.say("I thought you would stay for dinner.");done=true;}
            else if(slot==4&&now.getInt("Visit")==8&&now.getBoolean("AskedToLeave")){LiteraryVignettes.ready(p,place,now,"refused_the_familys_request_to_leave");io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new io.github.knaitoe.theoldesthouse.network.HouseFadePayload(20,40,40));LabyrinthDoors.goToJunction(p);done=true;}
            LiteraryVignettes.save(d,p.getUUID(),place,now);return done;});
    }
    public static void sceneTick(ServerPlayer p,LabyrinthPlace place,CompoundTag own){var c=get(p.server).copy(p.getUUID(),place);if(c==null)return;var b=address(p.server,c);if(place==LabyrinthPlace.OLD_CABIN){var man=LiteraryVignettes.actor(p,place,"OldMan",LiteraryActor.OLD_MAN,new BlockPos(1,0,-15),true);if(man!=null&&own.getInt("Present")%30==0){String code=morse(own.getString("Name"));int i=own.getInt("Present")/30%(code.length()+8);LiteraryRooms.at(p.serverLevel(),b,5,2,-16,i<code.length()&&code.charAt(i)!=' '?Blocks.SEA_LANTERN:Blocks.GRAY_CONCRETE);}return;}
        int visit=own.getInt("Visit");var father=LiteraryVignettes.actor(p,place,"Father",LiteraryActor.FAMILY_FATHER,front(c).north(2),true);var kitchen=c.meta.contains("Kitchen")?BlockPos.of(c.meta.getLong("Kitchen")).offset(0,0,c.offset()):new BlockPos(0,0,-32);var bedroom=c.meta.contains("Bedroom")?BlockPos.of(c.meta.getLong("Bedroom")).offset(0,0,c.offset()):new BlockPos(5,0,-37);
        var mother=LiteraryVignettes.actor(p,place,"Mother",LiteraryActor.FAMILY_MOTHER,kitchen.east(2),true);var child=LiteraryVignettes.actor(p,place,"Youngest",LiteraryActor.FAMILY_CHILD,bedroom.east(2),true);
        if(visit==1&&own.getBoolean("Admitted")&&p.getZ()<b.getZ()-12&&own.getInt("Present")>=100)own.putInt("Beat",1);
        if(visit==2&&own.getBoolean("Admitted")){int rooms=own.getInt("Tour");for(int i=0;i<3;i++){var marker=b.offset(i==0?bedroom:i==1?kitchen:front(c));if(p.distanceToSqr(marker.getCenter())<36){rooms|=1<<i;if(father!=null)father.say(new String[]{"This is our bedroom. We kept the window.","We are proud of the old kitchen. We have not changed it.","This was always our front door."}[i]);}}own.putInt("Tour",rooms);if(rooms==7)own.putInt("Beat",2);}
        if(visit>=3&&!c.meta.contains("Extra")){var extra=extraWall(p.serverLevel(),b,bedroom);var at=b.offset(extra);if(!LiteraryVignettes.watched(p.serverLevel(),at.getCenter())){NovelRooms.door(p.serverLevel(),at,Direction.SOUTH,Blocks.IRON_DOOR,false);c.meta.putLong("Extra",extra.asLong());get(p.server).setDirty();}}
        if(visit==3&&c.meta.contains("Extra")&&HouseWatchers.sees(p,b.offset(BlockPos.of(c.meta.getLong("Extra"))).getCenter())){own.putBoolean("ExtraSeen",true);own.putInt("Beat",3);}
        if(visit==5&&c.meta.contains("Extra")){var extra=BlockPos.of(c.meta.getLong("Extra"));if(child!=null&&!LiteraryVignettes.watched(p.serverLevel(),child.getEyePosition())){child.moveTo(Vec3.atBottomCenterOf(b.offset(extra.north(3))));if(own.getInt("Present")%100==0)p.serverLevel().playSound(null,child.blockPosition(),LiteraryRegistry.GIGGLE.get(),net.minecraft.sounds.SoundSource.BLOCKS,.65F,1);}
            if(!c.meta.getBoolean("StairMade")&&!LiteraryVignettes.watched(p.serverLevel(),b.offset(extra).getCenter())){NovelRooms.door(p.serverLevel(),b.offset(extra),Direction.SOUTH,Blocks.IRON_DOOR,true);for(int i=1;i<=5;i++){LiteraryRooms.box(p.serverLevel(),b,extra.getX()-1,extra.getY()-i,extra.getZ()-i,extra.getX()+1,extra.getY()+2,extra.getZ()-i,Blocks.AIR);LiteraryRooms.box(p.serverLevel(),b,extra.getX()-1,extra.getY()-i-1,extra.getZ()-i,extra.getX()+1,extra.getY()-i-1,extra.getZ()-i,Blocks.STONE_BRICKS);}c.meta.putBoolean("StairMade",true);get(p.server).setDirty();}
            if(p.getY()<b.getY()+extra.getY()-2&&p.distanceToSqr(b.offset(extra.north(4)).getCenter())<64){own.putBoolean("FoundChild",true);own.putInt("Beat",5);}}
        if(visit==6&&father!=null&&mother!=null&&child!=null){if(!own.getBoolean("FamilyChanged")&&!LiteraryVignettes.watched(p.serverLevel(),father.getEyePosition())&&!LiteraryVignettes.watched(p.serverLevel(),mother.getEyePosition())&&!LiteraryVignettes.watched(p.serverLevel(),child.getEyePosition())&&!LiteraryVignettes.watched(p.serverLevel(),b.offset(bedroom.above().east(3)).getCenter())&&!LiteraryVignettes.watched(p.serverLevel(),b.offset(kitchen.west(2)).above().getCenter())){father.appearance(father.role(),1);mother.appearance(mother.role(),1);child.appearance(child.role(),1);LiteraryVignettes.actor(p,place,"NewChild",LiteraryActor.FAMILY_CHILD,kitchen.west(2),true);LiteraryRooms.prop(p.serverLevel(),b,bedroom.above().east(3),LiteraryPropBlock.Kind.PHOTO,Direction.SOUTH);own.putBoolean("FamilyChanged",true);}if(own.getBoolean("FamilyChanged")&&HouseWatchers.sees(p,father.getEyePosition()))own.putInt("Beat",6);}
        if(visit==7&&!c.meta.hasUUID("Heirloom")){WeaponHistory.seed(p);UUID favorite=WeaponHistory.favorite(p);ItemStack sample=favorite==null?ItemStack.EMPTY:WeaponHistory.sample(p,favorite);if(sample.isEmpty()){if(father!=null)father.say("You have not brought a weapon with days in it. Carry one before you return.");return;}WeaponHistory.markCopy(sample);var at=b.offset(kitchen.above().north());LiteraryRooms.at(p.serverLevel(),b,kitchen.getX(),kitchen.getY()+1,kitchen.getZ()-2,Blocks.DARK_OAK_PLANKS);var frame=new ItemFrame(p.serverLevel(),at,Direction.SOUTH);frame.addTag(HEIRLOOM);frame.getPersistentData().putUUID("Reader",p.getUUID());fixFrame(frame);frame.setItem(sample,false);if(p.serverLevel().addFreshEntity(frame)){c.meta.putUUID("Heirloom",frame.getUUID());get(p.server).setDirty();}}
        if(visit==7&&own.getBoolean("HeirloomTaken"))own.putInt("Beat",7);
        if(visit>=8&&own.getInt("ThisVisitTicks")>=300&&father!=null){own.putBoolean("AskedToLeave",true);father.say("We have been polite. It is time for you to leave.");}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void latches(PlayerInteractEvent.RightClickBlock e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!e.getLevel().dimension().equals(HouseDimensions.OUTSIDE)||!protectedPosition(p.server,e.getPos()))return;if(e.getLevel().getBlockEntity(e.getPos()) instanceof net.minecraft.world.Container||e.getLevel().getBlockState(e.getPos()).is(Blocks.ENDER_CHEST)){e.setCanceled(true);e.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);p.displayClientMessage(Component.literal("The compartment is empty. The latch will not move."),true);}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void frame(PlayerInteractEvent.EntityInteract e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!(e.getTarget() instanceof ItemFrame frame))return;if(frame.getTags().contains(PROJECTION)){e.setCanceled(true);return;}if(!frame.getTags().contains(HEIRLOOM)||!LiteraryVignettes.inside(p,LabyrinthPlace.FAMILY_COPY)||!frame.getPersistentData().hasUUID("Reader")||!frame.getPersistentData().getUUID("Reader").equals(p.getUUID())||p.distanceToSqr(frame)>36||!p.getMainHandItem().isEmpty())return;var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.FAMILY_COPY);if(own.getInt("Visit")!=7||own.getBoolean("HeirloomTaken")||frame.getItem().isEmpty())return;var actual=frame.getItem().copy();WeaponHistory.markCopy(actual);frame.setItem(ItemStack.EMPTY,false);own.putBoolean("HeirloomTaken",true);own.putInt("Beat",7);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.FAMILY_COPY,own);LiteraryVignettes.give(p,actual);e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    private static BlockPos extraWall(ServerLevel l,BlockPos b,BlockPos bedroom){for(int radius=3;radius<=12;radius++)for(var direction:Direction.Plane.HORIZONTAL){var at=b.offset(bedroom.relative(direction,radius));if(l.getBlockState(at).isSolid()&&l.getBlockState(at.above()).isSolid()&&l.getBlockEntity(at)==null&&l.getBlockEntity(at.above())==null)return at.subtract(b);}return bedroom.west(3);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void projections(PlayerInteractEvent.EntityInteract e){if(e.getTarget().getTags().contains(PROJECTION))e.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void containers(PlayerInteractEvent.RightClickBlock e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p))return;if(p.level().dimension().equals(Level.OVERWORLD)&&p.serverLevel().getBlockState(e.getPos()).getBlock() instanceof DoorBlock){var d=get(p.server);var own=d.readers.computeIfAbsent(p.getUUID(),id->new CompoundTag());String key="Door_"+e.getPos().asLong();int count=own.getInt(key)+1;own.putInt(key,count);if(count>own.getInt("FrontCount")){own.putInt("FrontCount",count);own.putLong("Front",e.getPos().asLong());}d.setDirty();return;}if(!p.level().dimension().equals(HouseDimensions.OUTSIDE)||!protectedPosition(p.server,e.getPos()))return;var be=p.serverLevel().getBlockEntity(e.getPos());if(be instanceof net.minecraft.world.Container&&!(be instanceof LecternBlockEntity)){e.setCanceled(true);p.displayClientMessage(Component.literal("The copy is locked and empty."),true);}}
    @SubscribeEvent public static void trample(BlockEvent.FarmlandTrampleEvent e){if(e.getLevel() instanceof ServerLevel l&&l.dimension().equals(HouseDimensions.OUTSIDE)&&protectedPosition(l.getServer(),e.getPos()))e.setCanceled(true);}
    public static void nameSpoken(ServerPlayer p,String text){var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.OLD_CABIN);if(!own.getString("Name").equalsIgnoreCase(text.trim()))return;var a=LiteraryVignettes.actor(p,LabyrinthPlace.OLD_CABIN,"OldMan",LiteraryActor.OLD_MAN,new BlockPos(1,0,-15),true);if(a!=null)a.say("You waited for the whole name. Most people bring only the first letter.");own.putBoolean("MorseAnswered",true);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.OLD_CABIN,own);}
    private static String morse(String word){String[] code={".-","-...","-.-.","-..",".","..-.","--.","....","..",".---","-.-",".-..","--","-.","---",".--.","--.-",".-.","...","-","..-","...-",".--","-..-","-.--","--.."};var out=new StringBuilder();for(char c:word.toLowerCase(java.util.Locale.ROOT).toCharArray())if(c>='a'&&c<='z'){for(char pulse:code[c-'a'].toCharArray())out.append(pulse=='.'?"x ":"xxx ");out.append("  ");}return out.toString();}
    @Override public CompoundTag save(CompoundTag t,HolderLookup.Provider r){var list=new ListTag();for(var c:copies.values()){var s=new CompoundTag();s.putUUID("Reader",c.reader);s.putString("Place",c.place.id());s.putInt("Index",c.index);s.putLong("Bed",c.bed.asLong());s.putLong("Day",c.day);s.putInt("CaptureCursor",c.capturedColumns);s.putInt("BuildCursor",c.buildCursor);s.putBoolean("Captured",c.captured);s.putBoolean("Built",c.built);s.put("Snapshot",c.snapshot!=null?c.snapshot.save():c.capture.build(0,c.day,c.bed.asLong()).save());s.put("Meta",c.meta);s.put("Entities",c.entities);list.add(s);}t.put("Copies",list);t.putInt("NextIndex",nextIndex);var people=new ListTag();readers.forEach((id,state)->{var own=state.copy();own.putUUID("Reader",id);people.add(own);});t.put("Readers",people);return t;}
    public static LiteraryCopies load(CompoundTag t,HolderLookup.Provider r){var d=new LiteraryCopies();d.nextIndex=t.getInt("NextIndex");var list=t.getList("Copies",Tag.TAG_COMPOUND);for(int i=0;i<list.size();i++){var s=list.getCompound(i);var p=LabyrinthPlace.byId(s.getString("Place"));if(p!=LabyrinthPlace.FAMILY_COPY&&p!=LabyrinthPlace.OLD_CABIN)continue;var c=new Copy(s.getUUID("Reader"),p,s.getInt("Index"),BlockPos.of(s.getLong("Bed")),s.getLong("Day"));c.capturedColumns=s.getInt("CaptureCursor");c.buildCursor=s.getInt("BuildCursor");c.captured=s.getBoolean("Captured");c.built=s.getBoolean("Built");c.snapshot=RoomSnapshot.load(s.getCompound("Snapshot"),r.lookupOrThrow(Registries.BLOCK));if(c.snapshot==null)continue;if(!c.captured){for(var pos:BlockPos.betweenClosed(c.snapshot.min(),c.snapshot.max()))c.capture.set(pos,c.snapshot.stateAt(pos),c.snapshot.blockEntityAt(pos));c.snapshot=null;}else c.capture=null;c.meta=s.getCompound("Meta").copy();c.entities=s.getList("Entities",Tag.TAG_COMPOUND).copy();d.copies.put(c.key(),c);}var people=t.getList("Readers",Tag.TAG_COMPOUND);for(int i=0;i<people.size();i++){var own=people.getCompound(i);if(own.hasUUID("Reader"))d.readers.put(own.getUUID("Reader"),own.copy());}return d;}
}
