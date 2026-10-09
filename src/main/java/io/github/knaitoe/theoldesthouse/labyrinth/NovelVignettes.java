package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.*;
import io.github.knaitoe.theoldesthouse.opening.SceneRecord;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Personal verbs and real ending props, never arrival or ownership of borrowed paper. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class NovelVignettes {
    public static final String STATE="novel_0423",PHOTO_OWNER="PlainPhotoOwner",PHOTO_ID="PlainPhotoId",CAT="CourtyardCat";
    public static final int WELL_WAIT=1200,WARD_NIGHT=3600,MAIL_INTERVAL=1200;
    public static final List<LabyrinthPlace> PLACES=List.of(LabyrinthPlace.ZAMPANO_COURTYARD,LabyrinthPlace.WHALE,LabyrinthPlace.BARN_WELL,LabyrinthPlace.PLAIN,LabyrinthPlace.HOSPITAL,LabyrinthPlace.KAREN_ROOM);
    private static final ResourceLocation SMALL=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"well_child_scale");
    private static final Map<UUID,ServerPlayer> SCALED=new HashMap<>();
    private static final Map<UUID,SceneRecord> RECORDS=new HashMap<>();
    private record PendingPhoto(LabyrinthPlace place,int due){}
    private static final Map<UUID,PendingPhoto> PENDING_PHOTOS=new HashMap<>();
    private static final Map<UUID,Long> LAST_TICK=new HashMap<>();
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private NovelVignettes(){}
    public static boolean isNovel(LabyrinthPlace p){return PLACES.contains(p);}
    public static boolean canDeal(LabyrinthData data,UUID id,LabyrinthPlace p){var story=WitnessAccount.Story.of(p.id());return p.kind()==LabyrinthPlace.Kind.RECURRING||story!=null&&!WitnessAccount.has(data,id,story);}
    public static CompoundTag personal(LabyrinthData data,UUID id){return data.state(STATE).getCompound(id.toString()).copy();}
    public static void save(LabyrinthData data,UUID id,CompoundTag record){var all=data.state(STATE);all.put(id.toString(),record);data.setState(STATE,all);}
    private static boolean participant(ServerPlayer p){return p.isAlive()&&p.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR&&!FinaleProgress.terminal(FinaleProgress.phase(p.server,p.getUUID()));}
    public static boolean inside(ServerPlayer p,LabyrinthPlace place){var origin=HouseSavedData.get(p.server).houseOrigin();var b=origin==null?null:LabyrinthPlaces.base(origin,place);
        return b!=null&&participant(p)&&p.level().dimension().equals(NovelRooms.dimension(place))&&IndianLakeRooms.bounds(b,place).contains(p.position());}
    public static @Nullable LabyrinthPlace current(ServerPlayer p){for(var place:PLACES)if(inside(p,place))return place;return null;}
    public static void onArrive(ServerPlayer p,LabyrinthPlace place){
        recordVisit(p,place);var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());own.remove("Cue");own.remove("CueUntil");own.remove("CuePlace");save(data,p.getUUID(),own);HousePackets.send(p,new NovelScenePayload(0,0,"",0,0));if(!isNovel(place)||!participant(p))return;
        if(place==LabyrinthPlace.BARN_WELL){BarnFarm.animals(p);Farmstead.stock(p);cue(p,own,"Feed lies beside the barn door. Beyond the yard, an old well waits beneath its little roof.");}
        if(place==LabyrinthPlace.ZAMPANO_COURTYARD){ensureCats(p);var c=own.getCompound("Courtyard");int visits=c.getInt("Visits")+1;c.putInt("Visits",visits);own.put("Courtyard",c);thinCats(p,visits);
            var visited=data.visited(p.getUUID()).stream().map(LabyrinthPlace::byId).filter(q->q!=null&&q.slot()>=0&&q!=place&&!q.isOneShot()).toList();
            for(int i=1;i<place.doors().size();i++){var d=data.door(place.doorId(place.doors().get(i)));if(d!=null)data.deal(p.getUUID(),d,visited.isEmpty()?place.id():visited.get((i-1)%visited.size()).id(),false);}
        }
        if(place==LabyrinthPlace.WHALE&&!own.contains("MailDue")){own.putLong("MailDue",p.serverLevel().getGameTime()+MAIL_INTERVAL);own.putBoolean("Correspondence",true);}
        if(place==LabyrinthPlace.HOSPITAL&&!own.getBoolean("WardFinished")){own.putInt("WardTicks",0);own.putInt("WardCalls",0);own.putBoolean("Alarm",false);}
        if(place==LabyrinthPlace.KAREN_ROOM){showRecord(p,own);p.displayClientMessage(Component.literal("The bed remembers your place. Use the projector to change photographs; sneak-use it to take your originals."),false);}
        save(data,p.getUUID(),own);
    }
    public static void recordVisit(ServerPlayer p,LabyrinthPlace place){
        if(!participant(p)||place.room()==null||place==LabyrinthPlace.KAREN_ROOM)return;var origin=HouseSavedData.get(p.server).houseOrigin();var b=origin==null?null:LabyrinthPlaces.base(origin,place);if(b==null)return;
        PENDING_PHOTOS.put(p.getUUID(),new PendingPhoto(place,p.tickCount+40));
    }
    public static void ensureCats(ServerPlayer p){var data=LabyrinthData.get(p.server);var all=data.state(STATE);if(all.getBoolean("CatsMade"))return;
        var b=IndianLakeRooms.base(p.server,LabyrinthPlace.ZAMPANO_COURTYARD);if(b==null)return;ListTag cats=new ListTag();
        for(int i=0;i<5;i++){Cat cat=EntityType.CAT.create(p.serverLevel());if(cat==null)continue;cat.setPersistenceRequired();cat.getPersistentData().putInt(CAT,i+1);
            if(i==4){cat.setNoAi(true);cat.setOrderedToSit(true);cat.setInSittingPose(true);cat.moveTo(Vec3.atBottomCenterOf(b.offset(NovelRooms.MAT)));}
            else{cat.getPersistentData().putBoolean(LabyrinthEncounters.STRAY,true);cat.moveTo(b.getX()-7+i*4,b.getY(),b.getZ()-9,180,0);}
            p.serverLevel().addFreshEntity(cat);CompoundTag entry=new CompoundTag();entry.putUUID("Cat",cat.getUUID());entry.putInt("Index",i+1);cats.add(entry);}
        all.put("Cats",cats);all.putBoolean("CatsMade",true);data.setState(STATE,all);
    }
    private static void thinCats(ServerPlayer p,int visits){var data=LabyrinthData.get(p.server);var all=data.state(STATE);var cats=all.getList("Cats",Tag.TAG_COMPOUND);
        int keep=Math.max(1,6-visits);int count=0;for(Entity e:p.serverLevel().getAllEntities())if(e instanceof Cat c&&c.getPersistentData().contains(CAT)&&!c.isTame())count++;
        for(int i=0;i<cats.size()&&count>keep;i++){var entry=cats.getCompound(i);if(entry.getInt("Index")==5||entry.getBoolean("Kept"))continue;
            if(p.serverLevel().getEntity(entry.getUUID("Cat")) instanceof Cat cat&&!cat.isTame()){
                CompoundTag contents=new CompoundTag();cat.save(contents);var kept=MotherCollection.get(p.server).keepPet(cat.getUUID(),contents,null,"Zampano's cat");
                if(kept!=null){cat.discard();entry.putBoolean("Kept",true);count--;}}
        }all.put("Cats",cats);data.setState(STATE,all);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void feed(PlayerInteractEvent.EntityInteract e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getTarget() instanceof Cat cat)||cat.getPersistentData().getInt(CAT)!=5||!inside(p,LabyrinthPlace.ZAMPANO_COURTYARD))return;
        var data=LabyrinthData.get(p.server);var all=data.state(STATE);if(all.getBoolean("CatCoaxed"))return;e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(!cat.isAlive()||cat.isTame()||cat.distanceToSqr(p)>16)return;
        var food=p.getItemInHand(e.getHand());if(!LabyrinthEncounters.isVanillaMeat(food.getItem())){p.displayClientMessage(Component.literal("The cat watches your empty hand."),true);return;}
        cat.setNoAi(false);cat.setOrderedToSit(false);cat.setInSittingPose(false);cat.getPersistentData().putBoolean(LabyrinthEncounters.STRAY,true);
        LabyrinthEncounters.feedStray(p,cat,e.getHand());
        cat.getNavigation().moveTo(cat.getX()+3,cat.getY(),cat.getZ()+1,.8);all.putBoolean("CatCoaxed",true);data.setState(STATE,all);
        ItemStack key=VignetteYields.mark(new ItemStack(NovelRegistry.ARCHIVE_KEY.get()),LabyrinthPlace.ZAMPANO_COURTYARD.id());ItemEntity dropped=new ItemEntity(p.serverLevel(),cat.getX(),cat.getY()+.1,cat.getZ(),key);dropped.setTarget(p.getUUID());p.serverLevel().addFreshEntity(dropped);
        p.displayClientMessage(Component.literal("A small key was under the mat."),false);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void block(PlayerInteractEvent.RightClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getHand()!=InteractionHand.MAIN_HAND)return;var place=current(p);if(place==null)return;
        var b=IndianLakeRooms.base(p.server,place);var rel=e.getPos().subtract(b);if(p.distanceToSqr(e.getPos().getCenter())>36)return;
        var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());boolean handled=true;
        if(place==LabyrinthPlace.ZAMPANO_COURTYARD&&rel.distManhattan(NovelRooms.ARCHIVE_DOOR)<=1){
            var all=data.state(STATE);if(all.getBoolean("ArchiveOpen")||p.getMainHandItem().is(NovelRegistry.ARCHIVE_KEY.get())){
                NovelRooms.door(p.serverLevel(),b.offset(NovelRooms.ARCHIVE_DOOR),Direction.SOUTH,Blocks.IRON_DOOR,true);all.putBoolean("ArchiveOpen",true);data.setState(STATE,all);
            }else p.displayClientMessage(Component.literal("The cat is sitting over something on the mat."),true);
        }else if(place==LabyrinthPlace.ZAMPANO_COURTYARD&&rel.equals(NovelRooms.ARCHIVE_DESK)){
            if(data.state(STATE).getBoolean("ArchiveOpen"))open(p,place,SceneHuntReview.sourceBook(p.serverLevel(),b,place),"ArchiveBook",true,own);else handled=false;
        }else if(place==LabyrinthPlace.ZAMPANO_COURTYARD&&SceneReview.DRAFTS.contains(rel)&&p.serverLevel().getBlockState(e.getPos()).is(HouseBlocks.VIGNETTE_DETAIL.get())){
            int index=SceneReview.DRAFTS.indexOf(rel);open(p,place,NovelTexts.archiveDraft(index),"ArchiveDraft"+index,false,own);
        }else if(place==LabyrinthPlace.WHALE&&rel.equals(NovelRooms.MAIL)){
            var book=p.getMainHandItem().get(DataComponents.WRITTEN_BOOK_CONTENT);if(book!=null){
                String text=book.pages().stream().map(q->q.raw().getString()).reduce("",(a,q)->a+" "+q).toLowerCase(Locale.ROOT);String keyword=text.contains("cat")?"the cat":text.contains("home")?"home":text.contains("door")?"the door":"waiting";
                ListTag posted=own.getList("PostedBooks",Tag.TAG_COMPOUND);if(posted.size()<8){posted.add(p.getMainHandItem().copyWithCount(1).save(p.registryAccess()));own.put("PostedBooks",posted);p.getMainHandItem().shrink(1);give(p,VignetteYields.mark(NovelTexts.reply(keyword),place.id()));}
            }else open(p,place,NovelTexts.whaleOpening(),"WhaleOpening",false,own);
        }else if(place==LabyrinthPlace.WHALE&&rel.equals(NovelRooms.ATTIC_DESK)){
            if(own.getBoolean("AtticKnocked")&&own.getInt("Letters")>=3)open(p,place,NovelTexts.whaleLast(),"WhaleLast",true,own);else cue(p,own,"The letter has no address for you yet.");
        }else if(place==LabyrinthPlace.WHALE&&rel.equals(new BlockPos(-7,1,-25)))open(p,place,NovelTexts.whaleOpening(),"WhaleOpening",false,own);
        else if(place==LabyrinthPlace.BARN_WELL&&rel.equals(NovelRooms.WELL)){
            if(waitingBelow(p.server)&&own.getInt("WellTicks")<WELL_WAIT)cue(p,own,"The cover will not lift. Someone remains above the shaft.");
            else NovelRooms.cover(p.serverLevel(),b,false);
        }else if(place==LabyrinthPlace.BARN_WELL&&(rel.equals(NovelRooms.CARVING)||rel.equals(NovelRooms.OLD_CARVING)&&p.level().getBlockState(b.offset(rel)).is(NovelRegistry.CARVINGS.get()))){own.putBoolean("Initials",true);cue(p,own,"K. G. / D. G. Two pairs of cuts in the stone above you.");}
        else if(place==LabyrinthPlace.BARN_WELL&&rel.equals(NovelRooms.RIBBON)){if(own.getBoolean("WellReturned"))reward(p,own,"Ribbon",VignetteYields.mark(new ItemStack(NovelRegistry.RIBBON.get()),place.id()));}
        else if(place==LabyrinthPlace.BARN_WELL&&rel.equals(new BlockPos(-4,0,-17)))open(p,place,NovelTexts.well(),"WellBook",false,own);
        else if(place==LabyrinthPlace.PLAIN&&rel.equals(NovelRooms.APOLOGY))open(p,place,NovelTexts.apology(),"Apology",false,own);
        else if(place==LabyrinthPlace.PLAIN&&rel.equals(new BlockPos(3,0,-7)))reward(p,own,"Spyglass",new ItemStack(Items.SPYGLASS));
        else if(place==LabyrinthPlace.HOSPITAL&&rel.equals(NovelRooms.BUTTON)){
            if(own.getBoolean("Alarm")){own.putBoolean("Alarm",false);own.putInt("WardCalls",own.getInt("WardCalls")+1);cue(p,own,"The button clicks. No footsteps follow.");}
        }else if(place==LabyrinthPlace.HOSPITAL&&rel.equals(NovelRooms.WARD_NOTE)){
            open(p,place,own.getBoolean("WardFinished")?NovelTexts.hospitalLast():NovelTexts.hospitalOpening(),own.getBoolean("WardFinished")?"WardLast":"WardOpening",own.getBoolean("WardFinished"),own);
        }else if(place==LabyrinthPlace.KAREN_ROOM&&rel.equals(NovelRooms.PROJECTOR)){if(p.isShiftKeyDown())SecretPhotographs.open(p);else{own.putInt("Projection",own.getInt("Projection")+1);showRecord(p,own);}}
        else if(place==LabyrinthPlace.KAREN_ROOM&&rel.equals(new BlockPos(6,1,-11)))open(p,place,NovelTexts.karen(),"KarenBook",false,own);
        else handled=false;
        if(handled){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);save(data,p.getUUID(),own);}
        trackPhotoStore(p,e.getPos(),own);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void knock(PlayerInteractEvent.LeftClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START||!inside(p,LabyrinthPlace.WHALE))return;
        var b=IndianLakeRooms.base(p.server,LabyrinthPlace.WHALE);if(!e.getPos().equals(b.offset(NovelRooms.ATTIC_DOOR))||p.distanceToSqr(e.getPos().getCenter())>36)return;
        e.setCanceled(true);var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());long now=p.serverLevel().getGameTime(),last=own.getLong("LastKnock");
        if(own.contains("LastKnock")&&now-last<5)return;int[] groups=own.getIntArray("Knocks");
        if(groups.length==0||now-last>80)groups=new int[]{1};else if(now-last>=20){groups=Arrays.copyOf(groups,groups.length+1);groups[groups.length-1]=1;}else groups[groups.length-1]++;
        if(groups.length>3||Arrays.stream(groups).sum()>6)groups=new int[]{1};own.putIntArray("Knocks",groups);own.putLong("LastKnock",now);save(data,p.getUUID(),own);
        p.serverLevel().playSound(null,e.getPos(),SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.5F,.75F);
    }
    @SubscribeEvent public static void opened(PlayerContainerEvent.Open e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!participant(p)||!(e.getContainer() instanceof ChestMenu menu))return;var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());
        if(!p.serverLevel().dimension().equals(net.minecraft.world.level.Level.OVERWORLD)
                || !own.getBoolean("Correspondence")||own.getInt("Letters")>=8||p.serverLevel().getGameTime()<own.getLong("MailDue"))return;
        var chest=menu.getContainer();if(!chest.isEmpty())return;
        for(int i=0;i<chest.getContainerSize();i++)if(chest.getItem(i).isEmpty()){
            int n=own.getInt("Letters");ItemStack letter=VignetteYields.mark(NovelTexts.letter(n,p.getGameProfile().getName()),LabyrinthPlace.WHALE.id());CustomData.update(DataComponents.CUSTOM_DATA,letter,t->t.putUUID("LetterTo",p.getUUID()));
            chest.setItem(i,letter);chest.setChanged();menu.broadcastChanges();own.putInt("Letters",n+1);own.putLong("MailDue",p.serverLevel().getGameTime()+MAIL_INTERVAL);save(data,p.getUUID(),own);break;}
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(e.getServer().getTickCount()%20==0){var origin=HouseSavedData.get(e.getServer()).houseOrigin();var level=e.getServer().getLevel(HouseDimensions.INTERIOR);if(origin!=null&&level!=null&&LabyrinthData.get(e.getServer()).builtVersion()>=21){var b=LabyrinthPlaces.base(origin,LabyrinthPlace.WHALE);if(b!=null&&level.hasChunkAt(b))MailPlaqueBlock.repair(level,b);}}
        var server=e.getServer();BlockPos origin=HouseSavedData.get(server).houseOrigin();if(origin==null){clearAll();return;}
        Set<UUID> small=new HashSet<>();
        for(ServerPlayer p:server.getPlayerList().getPlayers()){
            if(!participant(p)){restoreScale(p);continue;}long now=p.serverLevel().getGameTime();if(Objects.equals(LAST_TICK.put(p.getUUID(),now),now))continue;
            var data=LabyrinthData.get(server);var own=personal(data,p.getUUID());var place=current(p);
            if(place!=null){var b=LabyrinthPlaces.base(origin,place);IndianLakeRooms.keepLoaded(p.serverLevel(),b,place);double y=p.getY()-b.getY();
                if(place==LabyrinthPlace.BARN_WELL){scale(p,true);small.add(p.getUUID());
                    boolean shaft=Math.abs(p.getX()-b.getX()-.5)<.6 && Math.abs(p.getZ()-b.getZ()+22.5)<.6;
                    if(shaft&&y< -10.5)own.putBoolean("WellEntered",true);
                    if(shaft&&y<1&&own.getBoolean("WellEntered")&&own.getInt("WellTicks")<WELL_WAIT){int ticks=Math.min(WELL_WAIT,own.getInt("WellTicks")+1);own.putInt("WellTicks",ticks);
                        if(ticks==1){cue(p,own,"Wood scrapes overhead. A hand reaches across the opening.");HousePackets.send(p,new NovelScenePayload(14,ticks,own.getString("Cue"),140,0));}if(ticks>=WellSequence.DARK_END&&ticks<WELL_WAIT&&ticks%20==0)p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS,50,0,false,false));
                        if(ticks==380)cue(p,own,"A voice above the boards: Who would know where to look?");
                        if(ticks==780)cue(p,own,"The voice comes again, farther away: They will stop asking.");
                        if(ticks==WELL_WAIT){p.removeEffect(net.minecraft.world.effect.MobEffects.DARKNESS);cue(p,own,"The waiting loosens. Climb toward the cover.");}}
                    if(own.getBoolean("WellEntered")&&own.getInt("WellTicks")>=WELL_WAIT&&y>=-.2&&!own.getBoolean("WellReturned")){own.putBoolean("WellReturned",true);WitnessAccount.resolve(p,WitnessAccount.Story.BARN_WELL,"waited_and_climbed_out");data.setCompleted(LabyrinthPlace.BARN_WELL.id(),true);cue(p,own,"A ribbon catches on the barrel beside the well.");}
                }else if(place==LabyrinthPlace.WHALE&&own.contains("LastKnock")&&now-own.getLong("LastKnock")>=40){
                    if(own.getInt("Letters")>=3&&Arrays.equals(own.getIntArray("Knocks"),new int[]{3,1,2})){own.putBoolean("AtticKnocked",true);NovelRooms.door(p.serverLevel(),b.offset(NovelRooms.ATTIC_DOOR),Direction.EAST,Blocks.IRON_DOOR,true);cue(p,own,"The middle attic answers after the last pause.");}
                    own.remove("LastKnock");own.remove("Knocks");
                }else if(place==LabyrinthPlace.PLAIN)tickPlain(p,b,own);
                else if(place==LabyrinthPlace.HOSPITAL&&!own.getBoolean("WardFinished")){
                    int ticks=own.getInt("WardTicks")+1;own.putInt("WardTicks",ticks);int gap=Math.max(80,240-ticks/24);
                    if(ticks==1||ticks>=own.getInt("NextAlarm")){own.putBoolean("Alarm",true);own.putInt("NextAlarm",ticks+gap);p.playNotifySound(NovelRegistry.MONITOR.get(),SoundSource.BLOCKS,.45F,1);cue(p,own,ticks>WARD_NIGHT-500?"The monitor calls sooner. The door stays shut.":"The monitor calls. The button is beside the incubator.");}
                    if(ticks==1200||ticks==2600){p.playNotifySound(NovelRegistry.RADIO_STATIC.get(),SoundSource.PLAYERS,.35F,.8F);cue(p,own,"Tom, over the radio: Is anybody there? I need—");}
                    if(ticks>=WARD_NIGHT){own.putBoolean("WardFinished",true);own.putBoolean("Alarm",false);cue(p,own,"Dawn. The alarms stop. The chart has a final page.");}
                }
                if(p.tickCount%20==0){boolean same=place.id().equals(own.getString("CuePlace"));int remaining=same?(int)Math.max(0,own.getLong("CueUntil")-p.serverLevel().getGameTime()):0;boolean covered=place==LabyrinthPlace.BARN_WELL&&coveredWait(p,own);
                    boolean descending=place==LabyrinthPlace.BARN_WELL&&WellSequence.shaft(p,b);
                    HousePackets.send(p,new NovelScenePayload(covered?14:descending?WellSequence.DESCENDING_MODE:PLACES.indexOf(place)+1,covered?own.getInt("WellTicks"):own.getInt("WardTicks"),remaining>0?own.getString("Cue"):"",remaining,0));}
            }else if(own.getInt("WardTicks")>0&&!own.getBoolean("WardFinished")){own.putInt("WardTicks",0);own.putInt("NextAlarm",0);own.putBoolean("Alarm",false);}
            var pending=PENDING_PHOTOS.get(p.getUUID());
            if(pending!=null&&p.tickCount>=pending.due()&&participant(p)){
                var site=pending.place();var b=LabyrinthPlaces.base(origin,site);var r=site.room();
                var box=new AABB(Vec3.atLowerCornerOf(b.offset(r.minX(),r.minY(),r.minZ())),Vec3.atLowerCornerOf(b.offset(r.maxX()+1,r.maxY()+1,r.maxZ()+1)));
                if(p.level().dimension().equals(NovelRooms.dimension(site))&&box.contains(p.getEyePosition())&&p.getZ()<b.getZ()-3){
                    RECORDS.put(p.getUUID(),new SceneRecord(p.serverLevel(),b.offset(r.minX(),r.minY(),r.minZ()),b.offset(r.maxX(),r.maxY(),r.maxZ()),p.getEyePosition(),p.getViewVector(1)));PENDING_PHOTOS.remove(p.getUUID());
                }else if(p.tickCount>pending.due()+1200)PENDING_PHOTOS.remove(p.getUUID());
            }
            var job=RECORDS.get(p.getUUID());if(job!=null&&job.tick()){var stack=job.finish();stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,Component.literal("Secret photograph"));var photos=own.getList("Record",Tag.TAG_COMPOUND);photos.add(stack.save(p.registryAccess()));while(photos.size()>6)photos.remove(0);own.put("Record",photos);RECORDS.remove(p.getUUID());}
            if(p.tickCount%20==0){returnPhoto(p,own);tickTom(p,origin,own);if(place==LabyrinthPlace.KAREN_ROOM)SecretPhotographs.project(p,own);}
            save(data,p.getUUID(),own);
        }
        for(var p:new ArrayList<>(SCALED.values()))if(!small.contains(p.getUUID()))restoreScale(p);
        List<Entity> doubles=new ArrayList<>();for(var level:server.getAllLevels())for(Entity entity:level.getAllEntities())if(entity instanceof NovelActor a&&a.role()==1){
            var viewer=a.owner().map(server.getPlayerList()::getPlayer).orElse(null);if(viewer==null||viewer.level()!=level||level.getGameTime()>=a.getPersistentData().getLong("DoubleUntil")||viewer.position().distanceToSqr(a.position())>16)doubles.add(a);
        }doubles.forEach(Entity::discard);
        var well=LabyrinthPlaces.base(origin,LabyrinthPlace.BARN_WELL);if(well!=null)WellSequence.tick(server,well);
    }
    public static boolean waitingBelow(MinecraftServer server) {
        var level=server.getLevel(HouseDimensions.OUTSIDE);var origin=HouseSavedData.get(server).houseOrigin();
        var b=origin==null?null:LabyrinthPlaces.base(origin,LabyrinthPlace.BARN_WELL);if(level==null||b==null)return false;
        return level.players().stream().anyMatch(p->{var own=personal(LabyrinthData.get(server),p.getUUID());
            return participant(p)&&inside(p,LabyrinthPlace.BARN_WELL)&&p.getY()<b.getY()+1
                    &&Math.abs(p.getX()-b.getX()-.5)<.6&&Math.abs(p.getZ()-b.getZ()+22.5)<.6
                    &&own.getBoolean("WellEntered")&&own.getInt("WellTicks")<WELL_WAIT;});
    }
    public static boolean coveredWait(ServerPlayer p,CompoundTag own){
        var b=IndianLakeRooms.base(p.server,LabyrinthPlace.BARN_WELL);
        return b!=null&&inside(p,LabyrinthPlace.BARN_WELL)&&p.getY()<b.getY()+1
                &&Math.abs(p.getX()-b.getX()-.5)<.6&&Math.abs(p.getZ()-b.getZ()+22.5)<.6
                &&own.getBoolean("WellEntered")&&own.getInt("WellTicks")<WELL_WAIT;
    }
    public static boolean exitLocked(ServerPlayer p,LabyrinthData.Door door) {
        return participant(p)&&door.id.equals(LabyrinthPlace.BARN_WELL.entryDoorId())
                &&!personal(LabyrinthData.get(p.server),p.getUUID()).getBoolean("WellReturned")
                &&!WitnessAccount.has(LabyrinthData.get(p.server),p.getUUID(),WitnessAccount.Story.BARN_WELL);
    }
    private static void tickPlain(ServerPlayer p,BlockPos b,CompoundTag own){
        BlockPos target=b.offset(NovelRooms.FIGURE);p.serverLevel().getChunkAt(target);p.serverLevel().getChunkSource().addRegionTicket(TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(target),3,target);
        var birds=p.serverLevel().getEntitiesOfClass(NovelVulture.class,new AABB(target).inflate(18));if(birds.isEmpty()&&p.tickCount%40==0){var v=NovelRegistry.VULTURE.get().create(p.serverLevel());if(v!=null){v.circle(target);v.moveTo(target.getX()+7,target.getY()+9,target.getZ());p.serverLevel().addFreshEntity(v);}}
        if(own.contains("Photo"))return;Vec3 look=target.getCenter().subtract(p.getEyePosition());boolean aim=p.isUsingItem()&&p.getUseItem().is(Items.SPYGLASS)&&look.lengthSqr()>400&&p.getViewVector(1).dot(look.normalize())>.997;
        int ticks=aim?own.getInt("Aim")+1:0;own.putInt("Aim",ticks);if(ticks<40)return;
        byte[] pixels=plainPixels();var photo=io.github.knaitoe.theoldesthouse.opening.NavidsonLetter.createSnapshot(p.serverLevel(),pixels);VignetteYields.mark(photo,LabyrinthPlace.PLAIN.id());
        UUID id=UUID.randomUUID();CustomData.update(DataComponents.CUSTOM_DATA,photo,t->{t.putUUID(PHOTO_OWNER,p.getUUID());t.putUUID(PHOTO_ID,id);});photo.set(DataComponents.CUSTOM_NAME,Component.literal("The distant frame"));
        own.put("Photo",photo.save(p.registryAccess()));own.putLong("PhotoDay",p.server.overworld().getDayTime()/24000);give(p,photo);p.playNotifySound(NovelRegistry.SHUTTER.get(),SoundSource.PLAYERS,.8F,1);
        WitnessAccount.resolve(p,WitnessAccount.Story.PLAIN,"held_the_distant_frame");LabyrinthData.get(p.server).setCompleted(LabyrinthPlace.PLAIN.id(),true);cue(p,own,"The frame is in your hand. The distance has not changed.");
    }
    /** Original silhouette map art. It deliberately refuses a detailed face or close-up. */
    public static byte[] plainPixels(){byte[] pixels=new byte[128*128];for(int y=0;y<128;y++)for(int x=0;x<128;x++){
        int c=y<70?net.minecraft.world.level.material.MapColor.SAND.id:net.minecraft.world.level.material.MapColor.TERRACOTTA_BROWN.id;int shade=y<70?2:Math.floorMod(x*17+y*31,5)==0?1:2;pixels[x+y*128]=(byte)(c*4+shade);}
        for(int y=64;y<77;y++)for(int x=61;x<=65;x++)pixels[x+y*128]=(byte)(net.minecraft.world.level.material.MapColor.COLOR_BLACK.id*4);for(int x=55;x<=71;x++)pixels[x+61*128]=(byte)(net.minecraft.world.level.material.MapColor.COLOR_BLACK.id*4);return pixels;}
    private static boolean photo(ItemStack stack,UUID owner){var custom=stack.get(DataComponents.CUSTOM_DATA);return custom!=null&&custom.copyTag().hasUUID(PHOTO_OWNER)&&owner.equals(custom.copyTag().getUUID(PHOTO_OWNER));}
    @SubscribeEvent public static void photoStore(PlayerInteractEvent.RightClickBlock e){if(e.getEntity() instanceof ServerPlayer p&&participant(p)){var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());trackPhotoStore(p,e.getPos(),own);}}
    private static void trackPhotoStore(ServerPlayer p,BlockPos at,CompoundTag own){if(!own.contains("Photo")||!(p.serverLevel().getBlockEntity(at) instanceof Container))return;
        var list=own.getList("PhotoStores",Tag.TAG_COMPOUND);String dim=p.level().dimension().location().toString();if(list.stream().anyMatch(t->((CompoundTag)t).getLong("Pos")==at.asLong()&&((CompoundTag)t).getString("Dimension").equals(dim)))return;
        if(list.size()>=128)return;CompoundTag tag=new CompoundTag();tag.putLong("Pos",at.asLong());tag.putString("Dimension",dim);list.add(tag);own.put("PhotoStores",list);save(LabyrinthData.get(p.server),p.getUUID(),own);
    }
    private static void returnPhoto(ServerPlayer p,CompoundTag own){
        if(!own.contains("Photo")||own.getBoolean("PhotoGivenAway"))return;
        if(MotherCollection.get(p.server).all().stream().filter(e->!e.pet).anyMatch(e->photo(e.item(p.registryAccess()),p.getUUID()))){own.putBoolean("PhotoGivenAway",true);return;}
        long day=p.server.overworld().getDayTime()/24000;if(day<=own.getLong("PhotoDay"))return;own.putLong("PhotoDay",day);
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(photo(p.getInventory().getItem(i),p.getUUID()))return;
        if(photo(p.containerMenu.getCarried(),p.getUUID()))return;
        // Remove the same original from every observed storage location before recalling it.
        List<Entity> dropped=new ArrayList<>();for(var level:p.server.getAllLevels())for(Entity entity:level.getAllEntities())if(entity instanceof ItemEntity item&&photo(item.getItem(),p.getUUID()))dropped.add(item);dropped.forEach(Entity::discard);
        for(var other:p.server.getPlayerList().getPlayers()){
            for(int i=0;i<other.getInventory().getContainerSize();i++)if(photo(other.getInventory().getItem(i),p.getUUID()))other.getInventory().setItem(i,ItemStack.EMPTY);
            if(photo(other.containerMenu.getCarried(),p.getUUID())){other.containerMenu.setCarried(ItemStack.EMPTY);other.containerMenu.broadcastChanges();}
        }
        var stores=own.getList("PhotoStores",Tag.TAG_COMPOUND);for(int n=0;n<stores.size();n++){var loc=stores.getCompound(n);var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,ResourceLocation.parse(loc.getString("Dimension")));var level=p.server.getLevel(key);if(level==null)continue;var at=BlockPos.of(loc.getLong("Pos"));level.getChunkAt(at);
            if(level.getBlockEntity(at) instanceof Container c){for(int i=0;i<c.getContainerSize();i++)if(photo(c.getItem(i),p.getUUID()))c.setItem(i,ItemStack.EMPTY);c.setChanged();}}
        give(p,ItemStack.parseOptional(p.registryAccess(),own.getCompound("Photo")));cue(p,own,"The photograph is back. It has not become easier to look at.");
    }
    private static void open(ServerPlayer p,LabyrinthPlace place,ItemStack book,String key,boolean ending,CompoundTag own){
        var data=LabyrinthData.get(p.server);String slot="Original_"+key;
        if(!own.contains(slot))own.put(slot,book.save(p.registryAccess()));var original=ItemStack.parseOptional(p.registryAccess(),own.getCompound(slot));save(data,p.getUUID(),own);
        p.openMenu(new SimpleMenuProvider((id,inv,who)->new NovelBookMenu(id,p,place,original,key,ending),Component.literal(original.getHoverName().getString())));
    }
    public static final class NovelBookMenu extends LecternMenu {
        private final ServerPlayer reader;private final LabyrinthPlace place;private final ItemStack original;private final String key;private final boolean ending;private int page;
        NovelBookMenu(int id,ServerPlayer p,LabyrinthPlace place,ItemStack book,String key,boolean ending){super(id,container(book),new SimpleContainerData(1));reader=p;this.place=place;original=VignetteYields.mark(book.copy(),place.id());this.key=key;this.ending=ending;}
        private static Container container(ItemStack book){var c=new SimpleContainer(1);c.setItem(0,book.copy());return c;}
        public ItemStack book(){return original.copy();}
        @Override public boolean clickMenuButton(net.minecraft.world.entity.player.Player p,int button){if(p!=reader||!inside(reader,place))return false;var data=LabyrinthData.get(reader.server);var own=personal(data,reader.getUUID());
            if(button==3){boolean taken=own.getBoolean("Taken_"+key);if(taken)return false;own.putBoolean("Taken_"+key,true);save(data,reader.getUUID(),own);give(reader,original.copy());return true;}
            int pages=original.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();int next=button==1?page-1:button==2?page+1:button>=100?button-100:-1;if(next<0||next>=pages)return false;
            boolean ok=super.clickMenuButton(p,button);if(!ok)return false;page=next;
            if(ending&&page==pages-1){var story=WitnessAccount.Story.of(place.id());if(story!=null){WitnessAccount.resolve(reader,story,"read_the_final_page");if(place.isFinishable())data.setCompleted(place.id(),true);}
                if(place==LabyrinthPlace.ZAMPANO_COURTYARD){own=personal(data,reader.getUUID());reward(reader,own,"Collar",VignetteYields.mark(new ItemStack(NovelRegistry.COLLAR.get()),place.id()));save(data,reader.getUUID(),own);}}
            return true;
        }
    }
    private static void give(ServerPlayer p,ItemStack stack){if(!p.getInventory().add(stack))p.drop(stack,false);}
    private static void reward(ServerPlayer p,CompoundTag own,String key,ItemStack stack){if(own.getBoolean("Yield_"+key))return;own.putBoolean("Yield_"+key,true);give(p,stack);}
    private static void cue(ServerPlayer p,CompoundTag own,String text){own.putString("Cue",text);own.putLong("CueUntil",p.serverLevel().getGameTime()+140);var place=current(p);own.putString("CuePlace",place==null?"":place.id());p.displayClientMessage(Component.literal(text),true);}
    private static void scale(ServerPlayer p,boolean on){var attr=p.getAttribute(Attributes.SCALE);if(attr==null)return;if(on){if(!attr.hasModifier(SMALL))attr.addTransientModifier(new AttributeModifier(SMALL,-.3,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));SCALED.put(p.getUUID(),p);}else{attr.removeModifier(SMALL);SCALED.remove(p.getUUID());}}
    private static void restoreScale(ServerPlayer p){scale(p,false);}
    public static boolean childScale(ServerPlayer p){return p.getAttribute(Attributes.SCALE).hasModifier(SMALL);}
    public static boolean karenBed(net.minecraft.world.level.Level level,BlockPos pos){if(!(level instanceof ServerLevel l)||!level.dimension().equals(HouseDimensions.INTERIOR))return false;var b=IndianLakeRooms.base(l.getServer(),LabyrinthPlace.KAREN_ROOM);return b!=null&&(pos.equals(b.offset(NovelRooms.BED))||pos.equals(b.offset(NovelRooms.BED).north()));}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(!(e.getEntity() instanceof ServerPlayer p))return;
        var state=personal(LabyrinthData.get(p.server),p.getUUID());state.remove("Cue");state.remove("CueUntil");state.remove("CuePlace");save(LabyrinthData.get(p.server),p.getUUID(),state);HousePackets.send(p,new NovelScenePayload(0,0,"",0,0));
        if(!karenBed(p.serverLevel(),p.getRespawnPosition()==null?BlockPos.ZERO:p.getRespawnPosition()))return;
        var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());int wakes=own.getInt("Wakes")+1;own.putInt("Wakes",wakes);var b=IndianLakeRooms.base(p.server,LabyrinthPlace.KAREN_ROOM);
        var at=b.offset(-6,0,-11);var old=p.serverLevel().getBlockState(at);if(old.isAir()||old.is(Blocks.FLOWER_POT)||old.is(Blocks.POTTED_DEAD_BUSH))p.serverLevel().setBlock(at,(wakes%2==0?Blocks.FLOWER_POT:Blocks.POTTED_DEAD_BUSH).defaultBlockState(),F);
        if(wakes%3==0){var actor=NovelRegistry.ACTOR.get().create(p.serverLevel());if(actor!=null){actor.appearance(p.getUUID(),1);actor.moveTo(Vec3.atBottomCenterOf(b.offset(NovelRooms.BED).south(2)));actor.getPersistentData().putLong("DoubleUntil",p.serverLevel().getGameTime()+100);p.serverLevel().addFreshEntity(actor);}}
        save(data,p.getUUID(),own);
    }
    private static void showRecord(ServerPlayer p,CompoundTag own){
        var b=IndianLakeRooms.base(p.server,LabyrinthPlace.KAREN_ROOM);if(b==null)return;
        // Remove the old small, shared projection once; native original maps remain in saved albums.
        p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.decoration.ItemFrame.class,new AABB(b.offset(0,2,-14)).inflate(2),f->f.isInvulnerable()).forEach(Entity::discard);
        SecretPhotographs.project(p,own);
        if(own.getList("Record",Tag.TAG_COMPOUND).isEmpty())p.displayClientMessage(Component.literal("No photographs yet. Spend a moment inside another room, then return."),true);
        p.playNotifySound(SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.3F,.7F);
    }
    public static void staircaseArrival(ServerPlayer p){
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null||!participant(p))return;
        var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());tickTom(p,origin,own);save(data,p.getUUID(),own);
    }
    private static void tickTom(ServerPlayer p,BlockPos origin,CompoundTag own){
        var phase=FinaleProgress.phase(p.server,p.getUUID());if(phase!=FinaleProgress.Phase.STAIRCASE||!FinaleArchitecture.contains(origin,p.blockPosition()))return;
        var at=FinaleRepairs.tom(origin);if(p.distanceToSqr(at.getCenter())>500)return;
        if(!p.serverLevel().hasChunkAt(at)||p.serverLevel().getBlockState(at.below()).getCollisionShape(p.serverLevel(),at.below()).isEmpty())return;
        if(own.hasUUID("Tom")&&p.serverLevel().getEntity(own.getUUID("Tom")) instanceof NovelActor)return;
        if(own.hasUUID("Tom")){if(!own.contains("TomMissing")){own.putLong("TomMissing",p.serverLevel().getGameTime());return;}if(p.serverLevel().getGameTime()-own.getLong("TomMissing")<60)return;}
        if(p.serverLevel().getEntitiesOfClass(NovelActor.class,new AABB(at).inflate(6),a->a.role()==0&&a.owner().filter(p.getUUID()::equals).isPresent()).isEmpty()){
            var actor=NovelRegistry.ACTOR.get().create(p.serverLevel());if(actor!=null){actor.appearance(p.getUUID(),0);actor.moveTo(Vec3.atBottomCenterOf(at));p.serverLevel().addFreshEntity(actor);own.putUUID("Tom",actor.getUUID());own.remove("TomMissing");}}
    }
    public static void meetTom(ServerPlayer p,NovelActor actor){if(actor.role()!=0||!participant(p)||actor.owner().filter(p.getUUID()::equals).isEmpty()||p.distanceToSqr(actor)>36)return;var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());reward(p,own,"Radio",new ItemStack(NovelRegistry.RADIO.get()));
        boolean handed=!own.getBoolean("Yield_Lighter");reward(p,own,"Lighter",new ItemStack(NovelRegistry.LIGHTER.get()));
        // The finite original may be in a chest, on the ground or with a peer. An empty carried
        // inventory does not establish its loss and must not mint another lighter.
        if(handed)actor.swing(InteractionHand.MAIN_HAND);
        cue(p,own,handed?"Tom: Here. Keep your thumb over the lid. It opens in my pocket.":"Tom: I'll stay here. Leave the radio on.");save(data,p.getUUID(),own);}
    public static void radio(ServerPlayer p){var own=personal(LabyrinthData.get(p.server),p.getUUID());String line;
        var phase=FinaleProgress.phase(p.server,p.getUUID());if(phase==FinaleProgress.Phase.COLLAPSE||phase==FinaleProgress.Phase.ESCAPE)line="Tom: The landing's moving. Find the water below the broken wall. Keep—";
        else if(LabyrinthData.get(p.server).returnDepth(p.getUUID())>=12)line="Tom: ...same doors. I can still hear you.";
        else line="Tom: The numbers change. Your way back still runs through the doors you opened.";cue(p,own,line);save(LabyrinthData.get(p.server),p.getUUID(),own);p.playNotifySound(NovelRegistry.RADIO_STATIC.get(),SoundSource.PLAYERS,.2F,1);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){restoreScale(p);RECORDS.remove(p.getUUID());PENDING_PHOTOS.remove(p.getUUID());LAST_TICK.remove(p.getUUID());}}
    @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p){restoreScale(p);RECORDS.remove(p.getUUID());PENDING_PHOTOS.remove(p.getUUID());}}
    public static void clearAll(){for(var p:new ArrayList<>(SCALED.values()))restoreScale(p);RECORDS.clear();PENDING_PHOTOS.clear();LAST_TICK.clear();}
}
