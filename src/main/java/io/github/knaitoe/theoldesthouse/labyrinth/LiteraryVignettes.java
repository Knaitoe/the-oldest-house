package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
/** Personal, saved native action accounts. World changes never substitute for another reader's actions. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LiteraryVignettes {
    public static final String OWNER="LiteraryReader",KEY="LiteraryOriginal",PLACE="LiteraryPlace";
    public static final ResourceLocation CHILD=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"literary_child_scale");
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private record Crawl(ServerPlayer p,Pose forced,Pose displayed){}
    private static final Map<UUID,Crawl> CRAWLS=new HashMap<>();
    private LiteraryVignettes(){}
    private static String state(LabyrinthPlace place){return "literary_0436_"+place.id();}
    public static String stateId(LabyrinthPlace place){return state(place);}
    public static CompoundTag personal(LabyrinthData d,UUID p,LabyrinthPlace place){return d.stateEntry(state(place),p.toString());}
    public static void save(LabyrinthData d,UUID p,LabyrinthPlace place,CompoundTag own){d.setStateEntry(state(place),p.toString(),own);}
    public static CompoundTag shared(LabyrinthData d,LabyrinthPlace place){return d.stateEntry(state(place),"World");}
    public static void shared(LabyrinthData d,LabyrinthPlace place,CompoundTag s){d.setStateEntry(state(place),"World",s);}
    public static boolean participant(ServerPlayer p){return p.isAlive()&&p.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR&&!FinaleProgress.terminal(FinaleProgress.phase(p.server,p.getUUID()));}
    public static @Nullable BlockPos base(ServerPlayer p,LabyrinthPlace place){if(place==LabyrinthPlace.BORRASCA)return BorrascaSites.base(p.server,p.getUUID());if(place==LabyrinthPlace.LAKE_HOUSE)return LakeHouse.readerBase(p);if(place==LabyrinthPlace.FAMILY_COPY||place==LabyrinthPlace.OLD_CABIN)return LiteraryCopies.base(p.server,p.getUUID(),place);var o=HouseSavedData.get(p.server).houseOrigin();return o==null?null:LabyrinthPlaces.base(o,place);}
    public static boolean inside(ServerPlayer p,LabyrinthPlace place){if(place==LabyrinthPlace.BORRASCA)return participant(p)&&BorrascaSites.inside(p);var b=base(p,place);return participant(p)&&b!=null&&p.level().dimension().equals(NovelRooms.dimension(place))&&(place==LabyrinthPlace.FAMILY_COPY||place==LabyrinthPlace.OLD_CABIN?LiteraryCopies.contains(p.server,p.getUUID(),place,p.position()):IndianLakeRooms.bounds(b,place).contains(p.position()));}
    public static @Nullable LabyrinthPlace current(ServerPlayer p){if(participant(p)&&BorrascaSites.inside(p))return LabyrinthPlace.BORRASCA;if(!participant(p)&&BorrascaSites.placeAt(p.server,p.blockPosition())!=null)return null;if(participant(p)&&LakeHouse.inside(p))return LabyrinthPlace.LAKE_HOUSE;if(!participant(p)||!HouseDimensions.isHouseDimension(p.level().dimension()))return null;var copy=LiteraryCopies.placeAt(p.server,p.blockPosition());if(copy!=null&&inside(p,copy))return copy;var o=HouseSavedData.get(p.server).houseOrigin();if(o==null)return null;var place=LabyrinthPlaces.placeAt(o,p.blockPosition());return LiteraryRooms.isLiterary(place)&&inside(p,place)?place:null;}
    public static boolean canDeal(LabyrinthData d,UUID p,LabyrinthPlace place){if(!LiteraryRooms.isLiterary(place))return false;if(UncannyRooms.isUncanny(place)&&personal(d,p,place).getBoolean("UncannyFailed"))return false;if(LiteraryCabinChoices.closed(d,p,place))return false;
        var story=WitnessAccount.Story.of(place.id());if(story!=null&&WitnessAccount.has(d,p,story))return false;
        if(place==LabyrinthPlace.ELK_FAN&&!personal(d,p,LabyrinthPlace.ELK_LOT).getBoolean("Ready"))return false;
        if(place==LabyrinthPlace.FAMILY_COPY||place==LabyrinthPlace.OLD_CABIN)return d.isReady("literary_copy_"+p+"_"+place.id())&&!personal(d,p,place).getBoolean("Completed");
        return place.kind()==LabyrinthPlace.Kind.RECURRING||!personal(d,p,place).getBoolean("Completed");}
    public static void onArrive(ServerPlayer p,LabyrinthPlace place){if(place==LabyrinthPlace.LAKE_HOUSE_EMPTY)place=LabyrinthPlace.LAKE_HOUSE;if(!LiteraryRooms.isLiterary(place)||!participant(p))return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID(),place);
        int visit=own.getInt("Visit");if(place==LabyrinthPlace.BORRASCA){BorrascaStory.arrive(p,own);save(d,p.getUUID(),place,own);return;}if(place==LabyrinthPlace.LAKE_HOUSE)own.putBoolean("LakeFreshVisit",visit==0||own.getBoolean("LakeDeparted"));if(visit==0||own.getInt("Beat")>=visit&&(place!=LabyrinthPlace.LAKE_HOUSE||own.getBoolean("LakeDeparted")))own.putInt("Visit",visit+1);own.putBoolean("Here",true);own.putInt("Present",0);own.putInt("ThisVisitTicks",0);own.putInt("ExamineTicks",0);own.putInt("FanTicks",0);own.putInt("StillTicks",0);own.putBoolean("Betrayed",false);own.remove("LastPosition");own.remove("CameraLease");own.putBoolean("Interrupted",false);
        save(d,p.getUUID(),place,own);if(place==LabyrinthPlace.FAMILY_COPY||place==LabyrinthPlace.OLD_CABIN)LiteraryCopies.arrived(p,place);
        if (place == LabyrinthPlace.HOLY_RABBIT && !rabbitCommitted(own)) {
            own.putBoolean("Committed", false);
            save(d, p.getUUID(), place, own);
        }
        if (place == LabyrinthPlace.CHILD_ROOM) { ChildRoom.arrive(p, own); save(d, p.getUUID(), place, own); }
        if (place == LabyrinthPlace.MOVIE_NIGHT) { MovieNight.arrive(p, own); save(d, p.getUUID(), place, own); MovieNight.canoes(p, base(p, place)); }
        if (place == LabyrinthPlace.COSTUME_NIGHT) { CostumeNight.arrive(p, own); save(d, p.getUUID(), place, own); }
        if (place == LabyrinthPlace.WINTER_LAKE) { WinterLake.arrive(p, own); save(d, p.getUUID(), place, own); }
        if (place == LabyrinthPlace.ELK_CARCASSES) { ElkHunt.arrive(p, own); save(d, p.getUUID(), place, own); }
        if (place == LabyrinthPlace.END_WORLD_CABIN) { CabinBargain.arrive(p, own); save(d, p.getUUID(), place, own); }
        if (place == LabyrinthPlace.THREE_FINGERS) { ThreeFingers.arrive(p, own); save(d, p.getUUID(), place, own); }
        if (place == LabyrinthPlace.GHOSTS_SET) { GhostsSet.arrive(p, own); save(d, p.getUUID(), place, own); }
        if (place == LabyrinthPlace.LAKE_HOUSE) { LakeHouse.arrive(p, own); save(d, p.getUUID(), place, own); }
        if(UncannyRooms.isUncanny(place)){UncannyStories.arrive(p,place,own);save(d,p.getUUID(),place,own);}
    }
    public static ItemStack mark(ServerPlayer p,LabyrinthPlace place,String key,ItemStack item){var s=VignetteYields.mark(item.copy(),place.id());CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putUUID(OWNER,p.getUUID());t.putString(KEY,key);t.putString(PLACE,place.id());});if(place==LabyrinthPlace.BORRASCA&&key.equals("Evidence"))BorrascaStory.markEvidence(p,s);return s;}
    public static void give(ServerPlayer p,ItemStack s){if(p.getInventory().add(s))return;var e=new ItemEntity(p.serverLevel(),p.getX(),p.getY()+.2,p.getZ(),s);e.setTarget(p.getUUID());p.serverLevel().addFreshEntity(e);}
    public static void reward(ServerPlayer p,LabyrinthPlace place,CompoundTag own,String key,ItemStack s){if(own.getBoolean("Yield_"+key))return;own.putBoolean("Yield_"+key,true);save(LabyrinthData.get(p.server),p.getUUID(),place,own);give(p,mark(p,place,key,s));}
    public static void ready(ServerPlayer p,LabyrinthPlace place,CompoundTag own,String outcome){if(!own.getBoolean("Ready")){own.putBoolean("Ready",true);own.putString("Outcome",outcome);p.displayClientMessage(Component.literal(place==LabyrinthPlace.BORRASCA?"Her name is with you. The Tree is still down the trail.":"The last account is here. Read it before you leave."),false);}own.putInt("Beat",own.getInt("Visit"));}
    public static void open(ServerPlayer p,LabyrinthPlace place,String key,ItemStack book,BlockPos surface){open(p,place,personal(LabyrinthData.get(p.server),p.getUUID(),place),key,book,surface);}
    static void open(ServerPlayer p,LabyrinthPlace place,CompoundTag own,String key,ItemStack book,BlockPos surface){if(!inside(p,place)||p.distanceToSqr(surface.getCenter())>36)return;var d=LabyrinthData.get(p.server);String slot="Original_"+key;
        if(!own.contains(slot))own.put(slot,mark(p,place,key,book).save(p.registryAccess()));var original=ItemStack.parseOptional(p.registryAccess(),own.getCompound(slot));save(d,p.getUUID(),place,own);
        p.openMenu(new SimpleMenuProvider((id,inv,who)->new Pages(id,p,place,key,original,true,true,surface),original.getHoverName()));if(p.containerMenu instanceof Pages pages)pages.opened(own);}
    public static final class Pages extends LecternMenu {
        private final ServerPlayer reader;private final LabyrinthPlace place;private final String key;private final ItemStack book;private final boolean owned,surface;private final BlockPos at;
        Pages(int id,ServerPlayer p,LabyrinthPlace place,String key,ItemStack book,boolean owned,boolean surface,BlockPos at){super(id,container(book),new SimpleContainerData(1));this.reader=p;this.place=place;this.key=key;this.book=book.copy();this.owned=owned;this.surface=surface;this.at=at;}
        private static Container container(ItemStack s){var c=new SimpleContainer(1);c.setItem(0,s.copy());return c;}
        public ItemStack original(){return book.copy();}
        @Override public boolean clickMenuButton(Player who,int button){if(who!=reader||!participant(reader)||(surface&&(!inside(reader,place)||reader.distanceToSqr(at.getCenter())>36)))return false;var d=LabyrinthData.get(reader.server);var own=personal(d,reader.getUUID(),place);
            if(button==3){if(!surface||!owned||own.getBoolean("Taken_"+key))return false;own.putBoolean("Taken_"+key,true);save(d,reader.getUUID(),place,own);give(reader,book.copy());if(place==LabyrinthPlace.BORRASCA)BorrascaStory.took(reader,key,at);return true;}
            if(!super.clickMenuButton(who,button))return false;if(getPage()==book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()-1)reached(d,own);return true;}
        /** A one-page paper has no page to turn to, and the lectern sends nothing for it: opening it is reading it to the end. */
        public void opened(){opened(personal(LabyrinthData.get(reader.server),reader.getUUID(),place));}
        /** Marks the record the caller still holds and will save, so the read is not lost to a stale copy. */
        void opened(CompoundTag own){if(book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()>1||!participant(reader)||surface&&(!inside(reader,place)||reader.distanceToSqr(at.getCenter())>36))return;reached(LabyrinthData.get(reader.server),own);}
        private void reached(LabyrinthData d,CompoundTag own){if(owned){own.putBoolean("Read_"+key,true);if(key.equals("Source")&&!own.getBoolean("Hint0505")&&LiteraryTexts.hint(place)!=null){own.putBoolean("Hint0505",true);reader.displayClientMessage(Component.literal(LiteraryTexts.hint(place)).withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),false);}if(place==LabyrinthPlace.BORRASCA)BorrascaStory.read(reader,key,own);if(key.startsWith("Diary")){int index=Integer.parseInt(key.substring(5));own.putInt("DiaryRead",own.getInt("DiaryRead")|(1<<index));if(own.getInt("DiaryRead")==255)ready(reader,place,own,"read_all_eight_original_pages");}
                // An account answers its source: said aloud, rather than silently not counting.
                if(place!=LabyrinthPlace.BORRASCA&&key.equals("Ending")&&own.getBoolean("Ready")&&!own.getBoolean("Read_Source")&&!own.getBoolean("Completed")){var source=LiteraryTexts.source(place).get(DataComponents.WRITTEN_BOOK_CONTENT);
                    reader.displayClientMessage(Component.literal("This account answers \""+(source==null?"the first paper":source.title().raw())+"\". Read that first, then read this again."),false);}
                if(place!=LabyrinthPlace.BORRASCA&&key.equals("Ending")&&inside(reader,place)&&own.getBoolean("Read_Source")&&own.getBoolean("Ready")&&!own.getBoolean("Completed")){var story=WitnessAccount.Story.of(place.id());if(story!=null){WitnessAccount.resolve(reader,story,own.getString("Outcome"));own.putBoolean("Completed",true);d.setCompleted(place.id(),true);finishReward(reader,place,own);}}
                save(d,reader.getUUID(),place,own);
            }}
    }
    private static void finishReward(ServerPlayer p,LabyrinthPlace place,CompoundTag own){if(UncannyRooms.isUncanny(place)){UncannyStories.finishReward(p,place,own);return;}switch(place){case ELK_FAN->reward(p,place,own,"Tooth",new ItemStack(LiteraryRegistry.TOOTH.get()));case WHEEL->reward(p,place,own,"Key",new ItemStack(LiteraryRegistry.KEY.get()));case END_WORLD_CABIN->{if(!own.getBoolean("Bargain0451"))reward(p,place,own,"Jar",new ItemStack(LiteraryRegistry.JAR.get()));else if(own.getBoolean("Sacrificed"))reward(p,place,own,"Globe",new ItemStack(LiteraryRegistry.SNOW_GLOBE.get()));else reward(p,place,own,"CrackedGlobe",new ItemStack(LiteraryRegistry.CRACKED_GLOBE.get()));}case GHOSTS_SET->reward(p,place,own,"Film",playable(p,new ItemStack(LiteraryRegistry.FILM.get()),"family_film"));case HOLY_RABBIT->{reward(p,place,own,"Knife",new ItemStack(LiteraryRegistry.KNIFE.get()));reward(p,place,own,"RabbitFoot",new ItemStack(Items.RABBIT_FOOT));}default->{}}
    }
    private static ItemStack playable(ServerPlayer p,ItemStack stack,String song){return stack;}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void collected(PlayerInteractEvent.RightClickItem e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p))return;var s=p.getItemInHand(e.getHand());var custom=s.get(DataComponents.CUSTOM_DATA);if(custom==null||!s.has(DataComponents.WRITTEN_BOOK_CONTENT))return;var t=custom.copyTag();var place=LabyrinthPlace.byId(t.getString(PLACE));if(!LiteraryRooms.isLiterary(place)||!t.contains(KEY))return;boolean own=t.hasUUID(OWNER)&&t.getUUID(OWNER).equals(p.getUUID());p.openMenu(new SimpleMenuProvider((id,inv,who)->new Pages(id,p,place,t.getString(KEY),s.copy(),own,false,p.blockPosition()),s.getHoverName()));if(p.containerMenu instanceof Pages pages)pages.opened();e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void click(PlayerInteractEvent.RightClickBlock e){if(e.isCanceled()||e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p))return;var place=current(p);if(place==null||p.distanceToSqr(e.getPos().getCenter())>36)return;
        if(place==LabyrinthPlace.ELK_CARCASSES)CarcassHunt.noise(p,e.getPos());var b=base(p,place);var rel=e.getPos().subtract(b);var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID(),place);boolean handled=false;
        // A saved elk lot keeps its old source and ending until it is re-carved (0.4.78).
        boolean oldLot=place==LabyrinthPlace.ELK_LOT&&!ElkLot.built(d,b);
        if(place==LabyrinthPlace.BORRASCA&&own.getInt("Visit")>=2&&rel.equals(BorrascaRooms.SOURCE)){open(p,place,own,"ContentNote",BorrascaTexts.contentNote(),e.getPos());handled=true;}
        else if(rel.equals(LiteraryRooms.source(place))&&!oldLot||oldLot&&rel.equals(ElkLot.OLD_SOURCE)||SceneReview.readingAlias(p.serverLevel(),b,place,rel)){open(p,place,own,"Source",SceneHuntReview.sourceBook(p.serverLevel(),b,place),e.getPos());handled=true;}
        else if((oldLot?rel.equals(ElkLot.OLD_ENDING):rel.equals(LiteraryRooms.ending(place)))&&own.getBoolean("Ready")){open(p,place,own,"Ending",LiteraryTexts.ending(p,place,own),e.getPos());handled=true;}
        else switch(place){
            case PAINTED_INTERIOR,SECOND_WAITRESS,TUNNEL_PERIPHERY,MISSING_SLEEP,LAYERED_MAN,MOTION_LIGHT->handled=UncannyStories.click(p,b,rel,place,own);
            case MASQUE->{if(MasqueSuite.clock(rel)&&own.getInt("Rooms")==127){own.putBoolean("ClockStopped0506",true);MasqueSuite.stoppedView(p,b);p.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(LiteraryRegistry.PARTY.getId(),SoundSource.BLOCKS));ready(p,place,own,"stopped_the_seventh_room_clock");handled=true;}}
            case USHER->{if(rel.distManhattan(LiteraryRooms.COFFIN)<=1){chooseLid(p,own);handled=true;}}
            case ELK_LOT->{if(!oldLot)handled=ElkLotNight.click(p,b,rel,own);}
            case THREE_FINGERS->handled=ThreeFingers.click(p,b,rel,own);
            case ELK_FAN->handled=ElkFanSequence.click(p,b,rel,own);
            case LAKE_HOUSE->handled=LakeHouse.click(p,b,rel,own);case BORRASCA->handled=BorrascaStory.click(p,b,rel,own);
            case MAPPING_INTERIOR->{if(rel.equals(LiteraryRooms.PILE)&&own.getBoolean("BeyondRoof")&&!own.getBoolean("PileChosen")){own.putBoolean("PileChosen",true);own.putBoolean("PileBroken",false);own.putInt("ChoiceVisit",own.getInt("Visit"));own.putInt("Beat",own.getInt("Visit"));reward(p,place,own,"Photograph",new ItemStack(LiteraryRegistry.PHOTOGRAPH.get()));handled=true;}}
            case CRIMSON_HALL->{int i=List.of(new BlockPos(-12,0,-35),new BlockPos(12,0,-35),new BlockPos(0,7,-39)).indexOf(rel);if(i>=0){Item[] discs={LiteraryRegistry.CYLINDER_ONE.get(),LiteraryRegistry.CYLINDER_TWO.get(),LiteraryRegistry.CYLINDER_THREE.get()};reward(p,place,own,"Cylinder"+i,playable(p,new ItemStack(discs[i]),"wax_cylinder_"+(i+1)));open(p,place,own,"CylinderText"+i,LiteraryTexts.cylinder(i),e.getPos());handled=true;}}
            case DEVILS_ROCK->{int index=diaryIndex(rel);if(index>=0&&(own.getInt("DiaryPlaced")&(1<<index))!=0){open(p,place,own,"Diary"+index,LiteraryTexts.diary(index),e.getPos());handled=true;}else if(rel.equals(LiteraryRooms.CAMERA)){camera(p,place,own,45);own.putInt("CameraPrints",own.getInt("CameraPrints")+1);var arrivals=new StringBuilder("MOTION PRINT\n\n");for(int i=0;i<8;i++)if(own.contains("ArrivalTime"+i))arrivals.append("Page ").append(i+1).append(": ").append(own.getLong("ArrivalTime"+i)).append(" ticks\n");arrivals.append("\nThe paper was behind the lens before the image became mine.");open(p,place,own,"CameraPrint",HouseWriting.book("Motion print",p.getGameProfile().getName(),HouseWriting.WritingStyle.WILL,List.of(arrivals.toString())),e.getPos());handled=true;}}
            case MINIATURES->handled=MiniatureNotes.click(p,b,rel,own);
            case WHEEL->{handled=wheelDoor(p,own,rel);}
            case GHOSTS_SET->{if(rel.equals(LiteraryRooms.BOOTH)){own.putBoolean("Booth",true);if(p.isShiftKeyDown()&&!own.contains("Confession"))own.putString("Confession","I chose to let the tape run in silence.");p.displayClientMessage(Component.literal("The booth is recording. Type your own words in chat, or crouch and touch the receiver to leave silence."),false);handled=true;}else handled=GhostsSetInteractions.click(p,b,rel,own);}
            case CONFESSION->{if(p.serverLevel().getBlockState(e.getPos()).is(Blocks.BOOKSHELF)&&Math.abs(rel.getX())==11){ConfessionBooks.shelf(p,own);handled=true;}
                else if(rel.equals(SceneReview.BOOK_TRAY)&&p.serverLevel().getBlockState(e.getPos()).is(HouseBlocks.VIGNETTE_DETAIL.get())){ConfessionBooks.table(p,own);handled=true;}
                else if(rel.equals(new BlockPos(10,1,-29))&&own.getInt("Visit")>own.getInt("SealedVisit")&&own.contains("SignedJournal")){open(p,place,own,"Journal",ItemStack.parseOptional(p.registryAccess(),own.getCompound("SignedJournal")),e.getPos());handled=true;}}
            case END_WORLD_CABIN->{if(rel.equals(LiteraryRooms.TV)){CabinBargain.open(p);handled=true;}else if(rel.equals(LiteraryRooms.CABIN_DOOR)||rel.equals(LiteraryRooms.CABIN_DOOR.above()))CabinBargain.answer(p,own);}
            case FAMILY_COPY,OLD_CABIN->{if(p.serverLevel().getBlockEntity(e.getPos()) instanceof net.minecraft.world.Container||p.serverLevel().getBlockState(e.getPos()).is(Blocks.ENDER_CHEST)){p.displayClientMessage(Component.literal("Every compartment is empty. None of the latches will open."),true);handled=true;}else handled=LiteraryCopies.click(p,place,e.getPos(),own);}
            default->{}
        }
        save(d,p.getUUID(),place,own);if(handled){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    }
    /** The nursery's cold is the house's, not weather: the frost closes in and the breath shows, and it never hurts. */
    @SubscribeEvent public static void cold(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e){if(e.getEntity() instanceof ServerPlayer p&&e.getSource().is(net.minecraft.world.damagesource.DamageTypes.FREEZE)&&current(p)==LabyrinthPlace.HILL_NURSERY)e.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void knock(PlayerInteractEvent.LeftClickBlock e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||e.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START)return;var place=current(p);if(place==null)return;var own=personal(LabyrinthData.get(p.server),p.getUUID(),place);var b=base(p,place);if(p.distanceToSqr(e.getPos().getCenter())>36)return;
        if(place==LabyrinthPlace.HILL_NURSERY&&e.getPos().equals(b.offset(LiteraryRooms.HILL_KNOCK))){own.remove("EchoAt");own.putInt("Knocks",own.getInt("Knocks")+1);own.putInt("EchoesPending",own.getInt("EchoesPending")+1);own.putInt("EchoDelay",10);e.setCanceled(true);save(LabyrinthData.get(p.server),p.getUUID(),place,own);}
        else if(place==LabyrinthPlace.HILL_NURSERY&&own.contains("PoundAt")&&own.getInt("Present")-own.getInt("PoundTick")<=100&&e.getPos().distSqr(b.offset(BlockPos.of(own.getLong("PoundAt"))))<=6.25){
            own.putLong("EchoAt",BlockPos.of(own.getLong("PoundAt")).asLong());own.putInt("PoundTick",own.getInt("Present"));own.putInt("QuietUntil",own.getInt("Present")+240);
            own.putInt("Knocks",own.getInt("Knocks")+1);own.putInt("EchoesPending",own.getInt("EchoesPending")+1);own.putInt("EchoDelay",10);e.setCanceled(true);save(LabyrinthData.get(p.server),p.getUUID(),place,own);}
        else if(place==LabyrinthPlace.FAMILY_COPY)LiteraryCopies.knock(p,e.getPos());
        // 0.4.77: nothing in the House hands out a shovel, so snow comes away by hand too; with anything else held, say so.
        else if(place==LabyrinthPlace.CRIMSON_HALL&&p.serverLevel().getBlockState(e.getPos()).is(Blocks.SNOW)&&!mayBreak(p,e.getPos()))p.displayClientMessage(Component.literal("Dig the snow away with a shovel, or with your bare hands."),true);
    }
    public static boolean mayBreak(ServerPlayer p,BlockPos pos){var place=current(p);if(place==null)return false;var b=base(p,place);var s=p.serverLevel().getBlockState(pos);return (place==LabyrinthPlace.CRIMSON_HALL&&s.is(Blocks.SNOW)&&(p.getMainHandItem().getItem() instanceof ShovelItem||p.getMainHandItem().isEmpty()))
        ||(place==LabyrinthPlace.ELK_CARCASSES&&participant(p)&&!YachtGlazingBlock.shattered(s)&&
                (ElkCarcassMap.porthole(pos.subtract(b))&&s.is(LiteraryRegistry.YACHT_PORTHOLE.get())||s.is(LiteraryRegistry.YACHT_WINDOW.get())&&ElkCarcassMap.aboard(pos.subtract(b).getCenter())))
        ||(place==LabyrinthPlace.MAPPING_INTERIOR&&pos.equals(b.offset(LiteraryRooms.PILE))&&s.is(LiteraryRegistry.PROP.get())&&personal(LabyrinthData.get(p.server),p.getUUID(),place).getBoolean("BeyondRoof"))
        ||(place==LabyrinthPlace.ELK_FAN&&personal(LabyrinthData.get(p.server),p.getUUID(),place).getList("Platforms",Tag.TAG_LONG).stream().anyMatch(t->((LongTag)t).getAsLong()==pos.asLong()));}
    public static boolean mayPlace(ServerPlayer p,BlockPos pos){if(!inside(p,LabyrinthPlace.ELK_FAN))return false;var b=base(p,LabyrinthPlace.ELK_FAN);var rel=pos.subtract(b);return rel.getX()>=-2&&rel.getX()<=2&&rel.getZ()>=-18&&rel.getZ()<=-15&&rel.getY()>=0&&rel.getY()<=4;}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void broken(BlockEvent.BreakEvent e){if(e.isCanceled()||!(e.getPlayer() instanceof ServerPlayer p)||!mayBreak(p,e.getPos()))return;var place=current(p);var own=personal(LabyrinthData.get(p.server),p.getUUID(),place);
        if(place==LabyrinthPlace.CRIMSON_HALL)own.putBoolean("ClearedSnow",true);
        if(place==LabyrinthPlace.MAPPING_INTERIOR&&!own.getBoolean("PileChosen")){own.putBoolean("PileChosen",true);own.putBoolean("PileBroken",true);own.putInt("ChoiceVisit",own.getInt("Visit"));own.putInt("Beat",own.getInt("Visit"));}
        save(LabyrinthData.get(p.server),p.getUUID(),place,own);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void placed(BlockEvent.EntityPlaceEvent e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!mayPlace(p,e.getPos()))return;var own=personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.ELK_FAN);var platforms=own.getList("Platforms",Tag.TAG_LONG);if(platforms.size()<40)platforms.add(LongTag.valueOf(e.getPos().asLong()));own.put("Platforms",platforms);own.putBoolean("BuiltReach",true);save(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.ELK_FAN,own);}
    private static void chooseLid(ServerPlayer p,CompoundTag own){var place=LabyrinthPlace.USHER;var d=LabyrinthData.get(p.server);if(own.getBoolean("LidChosen"))return;boolean close=p.isShiftKeyDown();var world=shared(d,place);if(!world.contains("LidClosed")){world.putBoolean("LidClosed",close);world.putInt("ChoiceVisit",own.getInt("Visit"));shared(d,place,world);}close=world.getBoolean("LidClosed");own.putBoolean("LidChosen",true);own.putBoolean("LidClosed",close);own.putInt("ChoiceVisit",own.getInt("Visit"));own.putInt("Beat",own.getInt("Visit"));var b=base(p,place);for(var rel:List.of(LiteraryRooms.COFFIN,LiteraryRooms.COFFIN.north())){var s=p.serverLevel().getBlockState(b.offset(rel));if(s.is(LiteraryRegistry.PROP.get()))p.serverLevel().setBlock(b.offset(rel),s.setValue(LiteraryPropBlock.STAGE,close?1:0),F);}p.displayClientMessage(Component.literal(close?"The lid has closed. Return and listen.":"The lid stays open. Return to the wing."),false);}
    public static void elkWound(ServerPlayer p){var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID(),LabyrinthPlace.ELK_LOT);own.putBoolean("ElkWounded",true);save(d,p.getUUID(),LabyrinthPlace.ELK_LOT,own);}
    public static void ate(ServerPlayer p,ItemStack s){var custom=s.get(DataComponents.CUSTOM_DATA);if(custom==null)return;var t=custom.copyTag();if(!t.hasUUID(OWNER)||!t.getUUID(OWNER).equals(p.getUUID()))return;var place=current(p);if(place==null)return;var own=personal(LabyrinthData.get(p.server),p.getUUID(),place);
        if(place==LabyrinthPlace.HOLY_RABBIT&&s.is(LiteraryRegistry.MEAL.get())&&t.getInt("Night")==own.getInt("Night")&&own.getInt("AteNight")!=own.getInt("Night")&&t.hasUUID("MealToken")&&own.hasUUID("MealToken")&&t.getUUID("MealToken").equals(own.getUUID("MealToken"))&&p.distanceToSqr(base(p,place).offset(0,0,-24).getCenter())<64){own.putBoolean("Committed",true);own.putString("CommitAction","ate_an_owned_meal");own.putInt("Meals",own.getInt("Meals")+1);own.putInt("AteNight",own.getInt("Night"));own.putInt("Drowse",0);HolyRabbit.hunger(p);}
        if(place==LabyrinthPlace.FAMILY_COPY&&s.is(LiteraryRegistry.STEW.get())&&own.getInt("Visit")==4&&t.hasUUID("MealToken")&&own.hasUUID("MealToken")&&t.getUUID("MealToken").equals(own.getUUID("MealToken"))){own.putBoolean("DinnerEaten",true);own.putInt("Beat",4);}
        save(LabyrinthData.get(p.server),p.getUUID(),place,own);
    }
    public static void talk(ServerPlayer p,LiteraryActor actor){var place=current(p);if(place==null||!actor.scene().equals(place.id())||p.distanceToSqr(actor)>36||actor.owner().isPresent()&&!actor.owner().get().equals(p.getUUID()))return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID(),place);
        if(UncannyRooms.isUncanny(place))UncannyStories.talk(p,actor,place,own);
        else if(place==LabyrinthPlace.END_WORLD_CABIN)CabinBargain.open(p);
        else if(place==LabyrinthPlace.FAMILY_COPY||place==LabyrinthPlace.OLD_CABIN)LiteraryCopies.talk(p,place,actor,own);
        else if (place == LabyrinthPlace.HOLY_RABBIT && actor.role() == LiteraryActor.FATHER) {
            if (!rabbitCommitted(own) && !own.getBoolean("Ready")) {
                var icons = new HashMap<Integer, ItemStack>();
                icons.put(0, LiteraryChoiceMenu.icon(Items.CAMPFIRE, "Stay with Father through the cold nights"));
                icons.put(8, LiteraryChoiceMenu.icon(Items.OAK_DOOR, "Leave the choice for now"));
                LiteraryChoiceMenu.open(p, "Stay through the nights?", icons,
                        () -> inside(p, place) && p.distanceToSqr(actor) <= 36,
                        slot -> slot == 8 || slot == 0 && commitRabbit(p));
            } else HolyRabbitDialogue.talk(p,actor,own);
        }
        else if(place==LabyrinthPlace.CONFESSION)actor.say("Bring a blank book. Sit where you can hear me.");
        else if(place==LabyrinthPlace.ELK_LOT)ElkLotNight.talk(p,actor,own);
        else if(place==LabyrinthPlace.MOVIE_NIGHT)MovieNightCast.talk(p,actor,own);
        else if(place==LabyrinthPlace.COSTUME_NIGHT)CostumeNight.talk(p,actor,own);
        else if(place==LabyrinthPlace.WINTER_LAKE)WinterLake.talk(p,actor,own);
        else if(place==LabyrinthPlace.THREE_FINGERS)ThreeFingers.talk(p,actor,own);
        else if(place==LabyrinthPlace.GHOSTS_SET)GhostsSet.talk(p,actor,own);
        else if(place==LabyrinthPlace.LAKE_HOUSE)LakeHouse.talk(p,actor,own);else if(place==LabyrinthPlace.BORRASCA)BorrascaStory.talk(p,actor,own);
        save(d,p.getUUID(),place,own);
    }
    public static void attacked(ServerPlayer p,LiteraryActor actor){var place=current(p);if(place==null||!actor.scene().equals(place.id())||actor.owner().isPresent()&&!actor.owner().get().equals(p.getUUID()))return;
        // Blows do not hurt him and do not end the visit; they only push him off (0.4.120). The way through is to get away, or to hide.
        if(place==LabyrinthPlace.ELK_CARCASSES&&actor.role()==LiteraryActor.KILLER){ElkHunt.shoved(p,actor);return;}var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID(),place);
        if(place==LabyrinthPlace.HOLY_RABBIT&&actor.role()==LiteraryActor.FATHER){actor.appearance(actor.role(),actor.phase());actor.say("I'm awake. I'm here.");own.putInt("FatherAwake",own.getInt("FatherAwake")+1);own.putInt("Drowse",Math.max(0,own.getInt("Drowse")-100));}
        else{own.putBoolean("Interrupted",true);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS,80));actor.say("You need to leave.");LabyrinthDoors.goToJunction(p);}
        save(d,p.getUUID(),place,own);
    }
    public static @Nullable LiteraryActor actor(ServerPlayer p,LabyrinthPlace place,String key,int role,BlockPos relative,boolean privateActor){var d=LabyrinthData.get(p.server);var s=shared(d,place);String id=privateActor?key+"_"+p.getUUID():key;
        if(s.hasUUID(id)){var e=p.serverLevel().getEntity(s.getUUID(id));return e instanceof LiteraryActor a?a:null;}
        var a=LiteraryRegistry.ACTOR.get().create(p.serverLevel());if(a==null)return null;a.bind(place,privateActor?p.getUUID():null);a.appearance(role,0);a.moveTo(Vec3.atBottomCenterOf(base(p,place).offset(relative)));a.setYRot(0);if(place==LabyrinthPlace.BLY_ROUTE&&role==LiteraryActor.LADY)LiteraryMotion.initialLady(a,base(p,place));if(!p.serverLevel().addFreshEntity(a))return null;s.putUUID(id,a.getUUID());shared(d,place,s);return a;
    }
    public static boolean watched(ServerLevel l,Vec3 point){for(var p:l.players()){if(!p.isAlive()||p.isSleeping())continue;var delta=point.subtract(p.getEyePosition());double dist=delta.length();if(dist>48||dist>1.5&&p.getLookAngle().dot(delta.normalize())<.26)continue;var hit=l.clip(new ClipContext(p.getEyePosition(),point,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));if(hit.getType()==HitResult.Type.MISS||hit.getBlockPos().distManhattan(BlockPos.containing(point))<=1)return true;}return false;}
    private static boolean rabbitCommitted(CompoundTag own) {
        return own.getBoolean("Committed") && (own.contains("CommitAction") || own.getInt("Meals") > 0);
    }
    private static boolean commitRabbit(ServerPlayer p) {
        if (!inside(p, LabyrinthPlace.HOLY_RABBIT)) return false;
        var d = LabyrinthData.get(p.server);
        var own = personal(d, p.getUUID(), LabyrinthPlace.HOLY_RABBIT);
        if (rabbitCommitted(own) || own.getBoolean("Ready")) return false;
        own.putBoolean("Committed", true);
        own.putString("CommitAction", "agreed_to_stay_with_father");
        HolyRabbit.hunger(p);
        save(d, p.getUUID(), LabyrinthPlace.HOLY_RABBIT, own);
        return true;
    }
    public static boolean retreatLocked(ServerPlayer p) {
        if(UncannyStories.locked(p))return true;
        if(ElkLotEnding.held(p))return true;
        if (LakeHouse.inside(p)&&personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.LAKE_HOUSE).getInt("Finish")>0)return true;
        if (CabinBargain.held(p)) return true;
        if (!inside(p, LabyrinthPlace.HOLY_RABBIT)) return false;
        var own = personal(LabyrinthData.get(p.server), p.getUUID(), LabyrinthPlace.HOLY_RABBIT);
        return rabbitCommitted(own) && !own.getBoolean("Ready");
    }
    private static void scale(ServerPlayer p,boolean child){var a=p.getAttribute(Attributes.SCALE);if(a==null)return;if(child&&!a.hasModifier(CHILD)){a.addTransientModifier(new AttributeModifier(CHILD,-.46,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));p.refreshDimensions();}else if(!child&&a.hasModifier(CHILD)){a.removeModifier(CHILD);p.refreshDimensions();}}
    private static void crawl(ServerPlayer p,boolean active){var c=CRAWLS.get(p.getUUID());if(c!=null&&c.p()!=p){restore(c);CRAWLS.remove(p.getUUID());c=null;}if(active&&c==null){CRAWLS.put(p.getUUID(),new Crawl(p,p.getForcedPose(),p.getPose()));p.setForcedPose(Pose.SWIMMING);p.setPose(Pose.SWIMMING);p.refreshDimensions();}else if(!active&&c!=null){restore(c);CRAWLS.remove(p.getUUID());}
        if(active&&(inside(p,LabyrinthPlace.ELK_CARCASSES)||inside(p,LabyrinthPlace.GHOSTS_SET))){
            if(c==null){var house=HouseSavedData.get(p.server);var origin=house.houseOrigin();if(origin!=null)HousePackets.send(p,new io.github.knaitoe.theoldesthouse.network.HouseSightlineStatePayload(origin,house.isImpossibleDoorRevealed()));}
            HousePackets.send(p,new NovelScenePayload(16,inside(p,LabyrinthPlace.GHOSTS_SET)?1:0,"",0,0));
        }
        else if(!active&&c!=null)HousePackets.send(p,new NovelScenePayload(17,0,"",0,0));
    }
    private static void restore(Crawl c){c.p().setForcedPose(c.forced());c.p().setPose(c.forced()==null?c.displayed():c.forced());c.p().refreshDimensions();}
    public static void clearAll(){for(var c:CRAWLS.values())restore(c);CRAWLS.clear();LiteraryMotion.clearAll();HolyRabbit.clearAll();ElkLotEnding.clearAll();ElkDrag.clearAll();MovieNight.clearAll();NurseryToys.clearAll();GhostsSet.clearAll();}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){crawl(p,false);scale(p,false);HolyRabbit.release(p);}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void frozenFood(LivingEntityUseItemEvent.Start e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!inside(p,LabyrinthPlace.HOLY_RABBIT)||!retreatLocked(p))return;var s=e.getItem();if(s.is(Items.SNOWBALL)&&s.has(DataComponents.FOOD))return;if(s.is(LiteraryRegistry.MEAL.get())){var t=s.get(DataComponents.CUSTOM_DATA);if(t!=null&&t.copyTag().hasUUID(OWNER)&&t.copyTag().getUUID(OWNER).equals(p.getUUID()))return;}if(s.has(DataComponents.FOOD)){e.setCanceled(true);p.displayClientMessage(Component.literal("It is frozen hard. He is bringing something down to you."),true);}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void snowNibble(PlayerInteractEvent.RightClickItem e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!inside(p,LabyrinthPlace.HOLY_RABBIT)||!p.getItemInHand(e.getHand()).is(Items.SNOWBALL))return;var snow=p.getItemInHand(e.getHand());snow.set(DataComponents.FOOD,new net.minecraft.world.food.FoodProperties.Builder().nutrition(0).saturationModifier(0).alwaysEdible().build());p.startUsingItem(e.getHand());e.setCanceled(true);e.setCancellationResult(InteractionResult.CONSUME);}
    @SubscribeEvent public static void pearl(EntityJoinLevelEvent e){if(e.getEntity() instanceof ThrownEnderpearl pearl&&pearl.getOwner() instanceof ServerPlayer p&&retreatLocked(p)){e.setCanceled(true);pearl.discard();}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void chat(ServerChatEvent e){var p=e.getPlayer();var place=current(p);if(place==LabyrinthPlace.GHOSTS_SET&&p.distanceToSqr(base(p,place).offset(LiteraryRooms.BOOTH).getCenter())<36){var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID(),place);if(own.getBoolean("Booth")&&!own.contains("Confession")){own.putString("Confession",e.getRawText().substring(0,Math.min(512,e.getRawText().length())));save(d,p.getUUID(),place,own);}}else if(place==LabyrinthPlace.OLD_CABIN)LiteraryCopies.nameSpoken(p,e.getRawText());}
    @SubscribeEvent public static void travel(net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre e){if(e.getEntity() instanceof ServerPlayer p&&retreatLocked(p)){p.stopFallFlying();p.stopRiding();}}
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){var server=event.getServer();LiteraryCopies.tick(server);LiteraryCabinChoices.reconcileClosure(server);ElkLotNight.tick(server);if(server.getTickCount()%5!=0)return;
        for(var p:server.getPlayerList().getPlayers()){var place=current(p);if(place!=LabyrinthPlace.HOLY_RABBIT)HolyRabbit.release(p);if(place==null){crawl(p,false);scale(p,false);continue;}var d=LabyrinthData.get(server);var own=personal(d,p.getUUID(),place);if(!own.getBoolean("Here"))continue;var b=base(p,place);IndianLakeRooms.keepLoaded(p.serverLevel(),b,place);own.putInt("Present",Math.min(72000,own.getInt("Present")+5));own.putInt("ThisVisitTicks",Math.min(72000,own.getInt("ThisVisitTicks")+5));
            boolean child=place==LabyrinthPlace.CHILD_ROOM||place==LabyrinthPlace.HOLY_RABBIT||(place==LabyrinthPlace.BORRASCA&&own.getInt("Visit")<=2)||(place==LabyrinthPlace.WHEEL&&own.getBoolean("Memory"));scale(p,child);crawl(p,(place==LabyrinthPlace.GHOSTS_SET&&GhostsSet.cameraCrawl(p,b))||(place==LabyrinthPlace.MAPPING_INTERIOR&&p.getY()<b.getY()-2)||(place==LabyrinthPlace.CHILD_ROOM&&ChildRoom.crawling(p,b))||(place==LabyrinthPlace.ELK_CARCASSES&&ElkHunt.crawl(p,b,CRAWLS.containsKey(p.getUUID()))));
            if(!own.getBoolean("Interrupted"))switch(place){case PAINTED_INTERIOR,SECOND_WAITRESS,TUNNEL_PERIPHERY,MISSING_SLEEP,LAYERED_MAN,MOTION_LIGHT->UncannyStories.reader(p,b,place,own);case HILL_NURSERY->hill(p,b,own);case MINIATURES->miniatures(p,b,own);case MASQUE->masque(p,b,own);case USHER->usher(p,b,own);case WINCHESTER->{if(p.getY()>=b.getY()+7&&p.getZ()<b.getZ()-65&&own.getInt("Present")>=100)ready(p,place,own,"reached_the_upper_construction_ledger");}case CHILD_ROOM->child(p,b,own);case CRIMSON_HALL->crimson(p,b,own);case BLY_ROUTE->bly(p,b,own);case ELK_LOT->{if(ElkLot.built(d,b))ElkLotNight.reader(p,b,own);else lot(p,b,own);}case ELK_FAN->fan(p,b,own);case MAPPING_INTERIOR->mapping(p,b,own);case HOLY_RABBIT->rabbit(p,b,own);case CONFESSION->confession(p,b,own);case ELK_CARCASSES->ElkHunt.tick(p,b,own);case WINTER_LAKE->WinterLake.reader(p,b,own);case COSTUME_NIGHT,MOVIE_NIGHT,CAMP_BLOOD->hunt(p,b,place,own);case DEVILS_ROCK->diary(p,b,own);case WHEEL->{var mother=actor(p,place,"Mother",LiteraryActor.MOTHER,new BlockPos(14,0,-59),true);if(own.getBoolean("ReachedMother")&&mother!=null&&p.distanceToSqr(mother)<36&&HouseWatchers.sees(p,mother.getEyePosition())){own.putBoolean("LeftMother",true);mother.say("You can leave now. The door will open.");}if(own.getBoolean("LeftMother")&&p.getZ()>b.getZ()-6){own.putBoolean("Memory",false);ready(p,place,own,"left_after_finding_mothers_room");}}case GHOSTS_SET->GhostsSet.reader(p,b,own);case END_WORLD_CABIN->CabinBargain.tick(p,b,own);case FAMILY_COPY,OLD_CABIN->LiteraryCopies.sceneTick(p,place,own);case THREE_FINGERS->ThreeFingers.reader(p,b,own);case LAKE_HOUSE->LakeHouse.reader(p,b,own);case BORRASCA->BorrascaStory.reader(p,b,own);default->{}}
            HousePackets.send(p,new io.github.knaitoe.theoldesthouse.network.EndingBookPayload(b.offset(place==LabyrinthPlace.ELK_LOT&&!ElkLot.built(d,b)?ElkLot.OLD_ENDING:LiteraryRooms.ending(place)),own.getBoolean("Ready")&&!own.getBoolean("Taken_Ending")));
            save(d,p.getUUID(),place,own);
        }
    }
    static void sound(ServerPlayer p,SoundEvent sound,BlockPos at,float volume,float pitch){p.serverLevel().playSound(null,at,sound,SoundSource.BLOCKS,volume,pitch);}
    /** A sound only this reader hears, at the block. */
    static void heard(ServerPlayer p,SoundEvent sound,BlockPos at,float volume,float pitch){p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),SoundSource.BLOCKS,at.getX()+.5,at.getY()+.5,at.getZ()+.5,volume,pitch,p.getRandom().nextLong()));}
    private static void hill(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.HILL_NURSERY;var rel=p.blockPosition().subtract(b);if(rel.getX()<-3&&rel.getZ()<-10&&rel.getZ()>-25){p.setTicksFrozen(Math.min(150,p.getTicksFrozen()+20));own.putBoolean("ColdDoor",true);}
        if(HouseWatchers.sees(p,b.offset(LiteraryRooms.HILL_WALL).getCenter()))own.putBoolean("NameSeen",true);
        // The pounding comes to the wall nearest the reader, then waits. Knocking back on that wall is answered, knock for knock.
        if(own.getInt("Present")%80==0&&own.getInt("Present")>=own.getInt("QuietUntil")){int z=Math.max(-41,Math.min(-4,rel.getZ()));var at=new BlockPos(rel.getX()<-3?-4:rel.getX()>3?4:3,1,z);sound(p,SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,b.offset(at),.75F,.75F);own.putLong("PoundAt",at.asLong());own.putInt("PoundTick",own.getInt("Present"));
            if(!own.getBoolean("PoundHint")){own.putBoolean("PoundHint",true);p.displayClientMessage(Component.literal("The pounding stops, as if it is listening."),true);}}
        if(own.getInt("EchoesPending")>0){int delay=own.getInt("EchoDelay")-5;own.putInt("EchoDelay",delay);if(delay<=0){if(own.contains("EchoAt"))sound(p,SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,b.offset(BlockPos.of(own.getLong("EchoAt"))),.55F,.9F);else sound(p,SoundEvents.STONE_HIT,b.offset(LiteraryRooms.HILL_KNOCK),.8F,.65F);own.putInt("EchoesPending",own.getInt("EchoesPending")-1);own.putInt("Echoes",own.getInt("Echoes")+1);own.putInt("EchoDelay",10);}}
        if(own.getBoolean("ColdDoor")&&own.getBoolean("NameSeen")&&own.getInt("Knocks")>=3&&own.getInt("Echoes")==own.getInt("Knocks"))ready(p,place,own,"heard_each_cellar_knock_return");
    }
    private static void miniatures(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.MINIATURES;var d=LabyrinthData.get(p.server);var at=b.offset(0,1,-18);var mother=MiniatureNotes.mother(p,b,own);if(p.distanceToSqr(at.getCenter())>25||!HouseWatchers.sees(p,at.getCenter())){if(mother!=null)HousePackets.send(p,new LiteraryViewPayload(-1,0,0,mother));return;}
        if(own.getInt("ModelVisit")!=own.getInt("Visit")){var visits=d.visited(p.getUUID()).stream().map(LabyrinthPlace::byId).filter(Objects::nonNull).filter(v->v!=place&&v.room()!=null&&v!=LabyrinthPlace.FAMILY_COPY&&v!=LabyrinthPlace.OLD_CABIN).sorted(Comparator.comparingInt(v->{int age=d.recentVisit(p.getUUID(),v);return age<0?100+v.slot():age;})).toList();if(visits.isEmpty())return;
            var samples=new ListTag();for(int n=0;n<Math.min(3,visits.size());n++){var room=visits.get(n);boolean preview=false;if(n==2&&own.getInt("Visit")%2==0){var future=LabyrinthDealer.vignettesAvailable(d,p.getUUID()).stream().filter(v->v!=place&&v.room()!=null&&v!=LabyrinthPlace.FAMILY_COPY&&v!=LabyrinthPlace.OLD_CABIN&&!d.visited(p.getUUID()).contains(v.id())).sorted(Comparator.comparingInt(LabyrinthPlace::slot)).findFirst();if(future.isPresent()){room=future.get();preview=true;}}var origin=HouseSavedData.get(p.server).houseOrigin();var rb=LabyrinthPlaces.base(origin,room);var rl=p.server.getLevel(NovelRooms.dimension(room));if(rb==null||rl==null)return;int[] states=MiniatureSamples.capture(rl,rb);if(states==null)return;var sample=new CompoundTag();sample.putString("Room",room.id());sample.putBoolean("Preview",preview);sample.putIntArray("Blocks",states);sample.putBoolean("Behind",n==0);samples.add(sample);}
            if(samples.isEmpty())return;
            own.put("Models",samples);own.putInt("ModelVisit",own.getInt("Visit"));
        }
        var display=modelDisplay(p,own);if(mother!=null)display.merge(mother);HousePackets.send(p,new LiteraryViewPayload(-1,0,0,display));
        own.putInt("ExamineTicks",own.getInt("ExamineTicks")+5);if(own.getInt("ExamineTicks")>=60){own.putInt("ModelsExamined",Math.max(own.getInt("ModelsExamined"),own.getInt("Visit")));own.putInt("Beat",own.getInt("Visit"));if(own.getInt("ModelsExamined")>=3)ready(p,place,own,"examined_three_personal_model_arrangements");}
    }
    public static CompoundTag modelDisplay(ServerPlayer p,CompoundTag own){var t=new CompoundTag();t.put("Models",own.getList("Models",Tag.TAG_COMPOUND).copy());t.putUUID("Reader",p.getUUID());t.putString("Name",p.getGameProfile().getName());return t;}
    private static void masque(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.MASQUE;int room=Math.max(0,Math.min(6,(-p.blockPosition().getZ()+b.getZ()-1)/11));own.putInt("Rooms",own.getInt("Rooms")|(1<<room));int time=(int)(p.serverLevel().getGameTime()%400);boolean chime=time<60;
        // 0.4.105: the party is each reader's own. Their dancers, the figure that scolds them and the chimes are private, and the hand
        // they put on the clock stops only their evening: a later reader finds the clock still going and the rooms still dancing.
        boolean stopped=own.getBoolean("ClockStopped0506");if(stopped&&time%100<5)MasqueSuite.stoppedView(p,b);
        for(int i=0;i<7;i++)for(int side:new int[]{-1,1}){var dancer=actor(p,place,"Dancer"+i+"_"+side,LiteraryActor.DANCER,new BlockPos(side*5,0,-11*i-6),true);if(dancer!=null){dancer.appearance(LiteraryActor.DANCER,chime||stopped?0:1);
            // Once their clock has stopped, the still dancers turn to whoever stopped it.
            if(stopped){float yaw=(float)Math.toDegrees(Math.atan2(-(p.getX()-dancer.getX()),p.getZ()-dancer.getZ()));dancer.setYRot(yaw);dancer.setYBodyRot(yaw);dancer.setYHeadRot(yaw);}}}
        if(!stopped&&time<5)heard(p,SoundEvents.BELL_BLOCK,b.offset(LiteraryRooms.CLOCK),2,.6F);
        // This runs every fifth tick, so the music needs a five-tick window to start in every world.
        if(!stopped&&time>=65&&time<70)heard(p,LiteraryRegistry.PARTY.get(),b.offset(0,0,-room*11-6),.65F,1);
        // 0.4.105: the steward's rule costs something. A second after the first stroke, a reader still moving is put back
        // a room, at its near door, and the rooms past it count again only once they are crossed again. Once a chime.
        if(chime&&time>=20&&!stopped&&own.contains("LastPosition")){Vec3 last=new Vec3(own.getDouble("LastX"),p.getY(),own.getDouble("LastZ"));long stroke=p.serverLevel().getGameTime()/400+1;
            if(last.distanceToSqr(p.position())>.0025&&own.getLong("CaughtChime0505")!=stroke){int back=Math.max(0,room-1);own.putLong("CaughtChime0505",stroke);own.putBoolean("MovedAtChime",true);
                own.putInt("Rooms",own.getInt("Rooms")&((1<<(back+1))-1));p.teleportTo(p.serverLevel(),b.getX()+.5,b.getY(),b.getZ()-11*back-1.5,180,p.getXRot());p.setDeltaMovement(Vec3.ZERO);
                var red=actor(p,place,"RedFigure"+back,LiteraryActor.RED_FIGURE,new BlockPos(0,0,-11*back-8),true);if(red!=null)red.say("The clock is speaking.");
                p.displayClientMessage(Component.literal("You moved while the clock spoke. You are a room further back."),true);}}
        own.putLong("LastPosition",p.blockPosition().asLong());own.putDouble("LastX",p.getX());own.putDouble("LastZ",p.getZ());
    }
    private static void usher(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.USHER;var d=LabyrinthData.get(p.server);var woman=actor(p,place,"Woman",LiteraryActor.COFFIN_WOMAN,LiteraryRooms.COFFIN,false);if(woman==null)return;var world=shared(d,place);
        woman.setNoGravity(true);if(world.getBoolean("LidClosed"))woman.appearance(LiteraryActor.COFFIN_WOMAN,2);if(!world.contains("LidClosed")){woman.appearance(LiteraryActor.COFFIN_WOMAN,1);return;}
        if(world.getBoolean("LidClosed")){if(own.getInt("Present")%80==0)sound(p,LiteraryRegistry.SCRATCH.get(),b.offset(LiteraryRooms.COFFIN),.4F+.1F*Math.min(3,own.getInt("Visit")),1);int cracks=Math.min(3,own.getInt("Visit")-1);if(cracks>world.getInt("Cracks")){for(int i=0;i<cracks;i++)for(int y=0;y<4;y++)LiteraryRooms.at(p.serverLevel(),b,-8,-3+y,-27-i*2,LiteraryRegistry.VAULT_CRACK.get());world.putInt("Cracks",cracks);shared(d,place,world);}}
        else if(own.getInt("Visit")>own.getInt("ChoiceVisit")&&own.getBoolean("LidChosen")&&!watched(p.serverLevel(),woman.position().add(0,1,0))&&!watched(p.serverLevel(),b.offset(12,0,-34).getCenter())){woman.moveTo(Vec3.atBottomCenterOf(b.offset(12,0,-34)));woman.appearance(LiteraryActor.COFFIN_WOMAN,0);world.putBoolean("WomanLeft",true);shared(d,place,world);}
        if(own.getBoolean("LidChosen")&&own.getInt("Visit")>own.getInt("ChoiceVisit")){boolean seen=world.getBoolean("LidClosed")?p.distanceToSqr(b.offset(LiteraryRooms.COFFIN).getCenter())<36:world.getBoolean("WomanLeft")&&HouseWatchers.sees(p,woman.getEyePosition());if(seen&&own.getInt("Present")>120)ready(p,place,own,world.getBoolean("LidClosed")?"returned_to_the_closed_lid":"found_her_outside_the_open_coffin");}
    }
    /** 0.4.74: the rebuilt child's room keeps its own sequence; see {@link ChildRoom}. */
    private static void child(ServerPlayer p, BlockPos b, CompoundTag own) { ChildRoom.tick(p, b, own); }
    private static void crimson(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.CRIMSON_HALL;var d=LabyrinthData.get(p.server);var world=shared(d,place);int layers=Math.min(5,own.getInt("Visit"));if(layers>world.getInt("SnowVisit")){for(int x=-3;x<=3;x++)for(int z=-27;z<=-21;z++){var at=b.offset(x,0,z);var s=p.serverLevel().getBlockState(at);if(s.isAir()||s.is(Blocks.SNOW))p.serverLevel().setBlock(at,Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,layers),F);}world.putInt("SnowVisit",layers);shared(d,place,world);}
        if(p.serverLevel().getBlockEntity(b.offset(0,0,-30)) instanceof JukeboxBlockEntity box){ItemStack disc=box.getItem(0);int index=disc.is(LiteraryRegistry.CYLINDER_ONE.get())?0:disc.is(LiteraryRegistry.CYLINDER_TWO.get())?1:disc.is(LiteraryRegistry.CYLINDER_THREE.get())?2:-1;
            var custom=disc.get(DataComponents.CUSTOM_DATA);boolean owns=custom!=null&&custom.copyTag().hasUUID(OWNER)&&custom.copyTag().getUUID(OWNER).equals(p.getUUID());if(index>=0&&owns&&box.getSongPlayer().isPlaying()&&box.getSongPlayer().getSong()!=null&&p.distanceToSqr(box.getBlockPos().getCenter())<225){if(own.getInt("Playing")!=index+1){own.putInt("Playing",index+1);own.putInt("Listen",0);}int listen=own.getInt("Listen")+5;own.putInt("Listen",listen);int length=(int)Math.ceil(box.getSongPlayer().getSong().lengthInSeconds()*20);if(listen>=length-5&&box.getSongPlayer().getTicksSinceSongStarted()>=length)own.putInt("HeardCylinders",own.getInt("HeardCylinders")|(1<<index));}else{own.putInt("Playing",0);own.putInt("Listen",0);}}
        if(own.getInt("HeardCylinders")==7&&own.getBoolean("ClearedSnow"))ready(p,place,own,"heard_all_three_cylinders_and_exposed_red_floor");else if(own.getInt("HeardCylinders")!=0)own.putInt("Beat",own.getInt("Visit"));
    }
    /** Long enough to see her face as the dark comes in; the reader is moved while the screen is black. */
    static final int BLY_HOLD=40;
    private static void bly(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.BLY_ROUTE;var lady=actor(p,place,"Lady",LiteraryActor.LADY,new BlockPos(0,0,-4),false);if(lady==null)return;int time=(int)(p.serverLevel().getGameTime()%900);if(time%80<5)sound(p,LiteraryRegistry.DRIP.get(),lady.blockPosition(),.8F,1);
        // 0.4.105: standing in her way costs the reader their place. She holds them to her face while the room goes dark, and they
        // come up in the lake room's water with the cupboard they waited in no longer counting. The wait starts again.
        long now=p.serverLevel().getGameTime();if(own.contains("HeldUntil0506")){long until=own.getLong("HeldUntil0506");if(now<until)return;own.remove("HeldUntil0506");LiteraryMotion.release(p);
            if(now-until<100){p.teleportTo(p.serverLevel(),b.getX()+.5,b.getY()-3,b.getZ()-44.5,180,0);p.setDeltaMovement(Vec3.ZERO);own.putInt("RefugeTicks",0);own.putBoolean("WaitedInCloset",false);p.displayClientMessage(Component.literal("She took you down to the water with her. The cupboard you waited in does not count now."),true);}}
        // A reader is in a cupboard only inside one of the six presses (0.4.105); the rooms off the corridor are not cupboards.
        var door=BlyManor.cupboardDoor(p.blockPosition().subtract(b));boolean closet=door!=null;boolean closed=false;if(closet){var s=p.serverLevel().getBlockState(b.offset(door));closed=s.getBlock() instanceof DoorBlock&&!s.getValue(DoorBlock.OPEN);}
        if(closed&&time<740){own.putInt("RefugeTicks",own.getInt("RefugeTicks")+5);own.putBoolean("WaitedInCloset",true);}if(lady.distanceToSqr(p)<3&&Math.abs(p.getX()-b.getX()-.5)<1.1){own.putBoolean("Dragged",true);own.putLong("HeldUntil0506",now+BLY_HOLD);LiteraryMotion.hold(p,now+BLY_HOLD);float hurt=Math.min(4,p.getHealth()-2);if(hurt>0)p.hurt(p.damageSources().drown(),hurt);
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS,BLY_HOLD+40));HousePackets.send(p,new HouseFadePayload(30,20,30));sound(p,SoundEvents.PLAYER_HURT_DROWN,p.blockPosition(),1,.7F);return;}
        if(time>=740&&lady.getY()<=b.getY()-3.95&&own.getInt("RefugeTicks")>=200&&!closet&&p.getZ()<b.getZ()-34&&p.getY()>=b.getY())ready(p,place,own,"waited_in_a_shut_closet_until_the_lake");
    }
    private static void lot(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.ELK_LOT;var d=LabyrinthData.get(p.server);var world=shared(d,place);LiteraryElk elk=null;if(world.hasUUID("Elk")&&p.serverLevel().getEntity(world.getUUID("Elk")) instanceof LiteraryElk e)elk=e;else if(!world.hasUUID("Elk")){elk=LiteraryRegistry.ELK.get().create(p.serverLevel());if(elk!=null){elk.moveTo(Vec3.atBottomCenterOf(b.offset(5,0,-23)));if(p.serverLevel().addFreshEntity(elk)){world.putUUID("Elk",elk.getUUID());shared(d,place,world);}}}
        int ticks=own.getInt("ThisVisitTicks");if(elk!=null){if(ticks<240)LiteraryMotion.legacyElk(elk,new Vec3(.03,0,-.1));else if(elk.getZ()>b.getZ()-53)LiteraryMotion.legacyElk(elk,new Vec3(0,0,-.22));if(HouseWatchers.sees(p,elk.getEyePosition()))own.putBoolean("ElkSeen",true);if(ticks==120){LiteraryRooms.at(p.serverLevel(),b,9,1,-23,Blocks.AIR);sound(p,SoundEvents.IRON_GOLEM_DAMAGE,b.offset(9,1,-23),1,.6F);}}
        for(int i=0;i<3;i++){var man=actor(p,place,"Accuser"+i,LiteraryActor.STRANGER,new BlockPos(i*8,0,-28),false);if(man!=null&&ticks%180<5)man.say(p.getGameProfile().getName()+"! You went out there. Tell us what you did.");}
        if(p.getZ()<b.getZ()-55&&own.getBoolean("ElkSeen")){own.putInt("FieldTicks",own.getInt("FieldTicks")+5);if(own.getInt("FieldTicks")%80==0){sound(p,LiteraryRegistry.HOOVES.get(),p.blockPosition().offset(3,0,-5),.75F,1.2F);for(int side:new int[]{-1,1}){var eye=actor(p,place,"Herd"+side,LiteraryActor.RED_FIGURE,new BlockPos(side*8,0,-75),true);if(eye!=null){eye.appearance(LiteraryActor.RED_FIGURE,3);eye.moveTo(p.getX()+side*7,p.getY(),p.getZ()-5,0,0);}}}
            if(p.getZ()<b.getZ()-103&&own.getInt("FieldTicks")>=180&&own.getInt("FieldLoops")>0){HousePackets.send(p,new HouseFadePayload(20,40,40));p.teleportTo(p.serverLevel(),b.getX()+.5,b.getY(),b.getZ()-6.5,180,0);ready(p,place,own,"escaped_the_looping_field_and_read_own_clipping");}}
        if(p.getZ()<b.getZ()-108&&!own.getBoolean("Ready")){p.teleportTo(p.serverLevel(),p.getX(),p.getY(),b.getZ()-67.5,p.getYRot(),p.getXRot());own.putInt("FieldLoops",own.getInt("FieldLoops")+1);}
    }
    private static void fan(ServerPlayer p,BlockPos b,CompoundTag own){ElkFanSequence.reader(p,b,own);}
    /** The owner's five-second fan ending resolves its existing source before the native return. */
    static void finishFan(ServerPlayer p,CompoundTag own){
        if(!inside(p,LabyrinthPlace.ELK_FAN)||!own.getBoolean("BladeStrike")||!own.getBoolean("ElkThroughFan")||!own.getBoolean("Read_Source")||own.getBoolean("Completed"))return;
        own.putBoolean("Ready",true);own.putString("Outcome","held_the_fan_platform_and_was_thrown_out");WitnessAccount.resolve(p,WitnessAccount.Story.of(LabyrinthPlace.ELK_FAN.id()),own.getString("Outcome"));own.putBoolean("Completed",true);LabyrinthData.get(p.server).setCompleted(LabyrinthPlace.ELK_FAN.id(),true);finishReward(p,LabyrinthPlace.ELK_FAN,own);
    }
    private static void mapping(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.MAPPING_INTERIOR;var silhouette=actor(p,place,"Father",LiteraryActor.SILHOUETTE,new BlockPos(-9,0,-26),true);var brother=actor(p,place,"Brother",LiteraryActor.BROTHER,new BlockPos(6,0,-32),true);int visit=own.getInt("Visit");if(silhouette!=null){silhouette.appearance(LiteraryActor.SILHOUETTE,Math.min(3,visit-1));
            int step=(own.getInt("ThisVisitTicks")/100)%4;int[][] stops={{-9,-26},{-7,-26},{-7,-28},{-9,-28}};
            Vec3 destination=b.offset(stops[step][0],0,stops[step][1]).getCenter().add(0,-.5,0);Vec3 delta=destination.subtract(silhouette.position()).multiply(1,0,1);
            if(delta.lengthSqr()>.04)LiteraryMotion.toward(silhouette,destination,.12);
            else silhouette.setYRot((float)Math.toDegrees(Math.atan2(silhouette.getX()-p.getX(),p.getZ()-silhouette.getZ())));
            silhouette.setYHeadRot(silhouette.getYRot());silhouette.yBodyRot=silhouette.getYRot();if(HouseWatchers.sees(p,silhouette.getEyePosition()))own.putBoolean("SilhouetteSeen",true);}
        if(brother!=null){brother.appearance(LiteraryActor.BROTHER,own.getBoolean("PileBroken")?0:Math.min(3,visit));if(own.getBoolean("PileChosen")&&visit>own.getInt("ChoiceVisit")&&p.distanceToSqr(brother)<25&&HouseWatchers.sees(p,brother.getEyePosition()))ready(p,place,own,own.getBoolean("PileBroken")?"broke_the_fathers_things_and_returned_to_the_brother":"kept_the_fathers_things_and_saw_the_brother_fade");}
        if(own.getBoolean("SilhouetteSeen")&&p.getY()<b.getY()-2&&p.getZ()<b.getZ()-39){own.putBoolean("BeyondRoof",true);own.putInt("Beat",visit);}
    }
    private static void rabbit(ServerPlayer p,BlockPos b,CompoundTag own){HolyRabbit.reader(p,b,own);}
    private static void confession(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.CONFESSION;
        int chapter=own.getInt("Chapter");if(chapter>=LiteraryTexts.CONFESSION.length){if(own.getInt("Visit")>own.getInt("SealedVisit")&&own.getBoolean("Read_Journal"))ready(p,place,own,"read_the_sealed_journal_signed_with_own_name");return;}
        var visitor=actor(p,place,"Visitor",LiteraryActor.VISITOR,new BlockPos(1,0,-22),false);if(visitor==null||p.distanceToSqr(visitor)>64||!HouseWatchers.sees(p,visitor.getEyePosition()))return;
        if(own.getInt("ChapterVisit")==own.getInt("Visit"))return;
        var laid=ConfessionBooks.tableBook(p,own);int slot=-1;
        if(laid==null)for(int i=0;i<p.getInventory().getContainerSize();i++){var book=p.getInventory().getItem(i);if(!book.is(Items.WRITABLE_BOOK)||!ConfessionBooks.owned(book,p))continue;
            var pages=book.get(DataComponents.WRITABLE_BOOK_CONTENT);if(!own.hasUUID("Journal")&&pages!=null&&pages.pages().stream().anyMatch(page->!page.raw().isBlank()))continue;
            var custom=book.get(DataComponents.CUSTOM_DATA);if(!own.hasUUID("Journal")||custom!=null&&custom.copyTag().hasUUID("Journal")&&custom.copyTag().getUUID("Journal").equals(own.getUUID("Journal"))){slot=i;break;}}
        if(laid==null&&slot<0){if(own.getInt("Present")%100==0)visitor.say("There is a clean book on the low shelf. Put it between us.");return;}
        var quill=laid==null?p.getInventory().getItem(slot):laid.getItem();
        if(!own.hasUUID("Journal")){own.putUUID("Journal",UUID.randomUUID());CustomData.update(DataComponents.CUSTOM_DATA,quill,t->{t.putUUID("Journal",own.getUUID("Journal"));t.putUUID(OWNER,p.getUUID());});var initial=quill.get(DataComponents.WRITABLE_BOOK_CONTENT);if(initial!=null)own.putString("InitialWriting",initial.pages().stream().map(v->v.raw()).collect(java.util.stream.Collectors.joining("\n\n")));}

        String sentence=LiteraryTexts.CONFESSION[chapter];int offset=own.getInt("WordOffset");String[] words=sentence.split(" ");int timer=own.getInt("WordTicks")+5;own.putInt("WordTicks",timer);if(timer<15)return;own.putInt("WordTicks",0);
        String transcript=own.getString("JournalText");if(offset<words.length){transcript+=words[offset]+" ";own.putString("JournalText",transcript);own.putInt("WordOffset",offset+1);visitor.say(sentence.substring(0,Math.min(sentence.length(),transcript.length())));}
        else{chapter++;own.putInt("ChapterVisit",own.getInt("Visit"));own.putInt("Chapter",chapter);own.putInt("WordOffset",0);own.putInt("Beat",own.getInt("Visit"));if(chapter==LiteraryTexts.CONFESSION.length){transcript+="\n\n"+LiteraryTexts.counts(p)+"\n\nSigned: "+p.getGameProfile().getName();own.putString("JournalText",transcript);own.putString("Statistics",LiteraryTexts.counts(p));var signed=HouseWriting.book("The listener's confession",p.getGameProfile().getName(),HouseWriting.WritingStyle.WILL,List.of(own.getString("InitialWriting"),transcript));CustomData.update(DataComponents.CUSTOM_DATA,signed,t->{t.putUUID("Journal",own.getUUID("Journal"));t.putUUID(OWNER,p.getUUID());});own.put("SignedJournal",signed.save(p.registryAccess()));own.putInt("SealedVisit",own.getInt("Visit"));quill.shrink(1);if(laid!=null){laid.discard();own.remove("TableJournal");}LiteraryRooms.at(p.serverLevel(),b,10,1,-29,Blocks.CRACKED_STONE_BRICKS);p.displayClientMessage(Component.literal("The book is gone from your hand. The wall behind the desk has a new seam. Return to examine it."),false);return;}}
        var nativePages=new ArrayList<net.minecraft.server.network.Filterable<String>>();String written=own.getString("InitialWriting")+"\n\n"+transcript;for(int start=0;start<written.length();start+=250)nativePages.add(net.minecraft.server.network.Filterable.passThrough(written.substring(start,Math.min(written.length(),start+250))));quill.set(DataComponents.WRITABLE_BOOK_CONTENT,new WritableBookContent(nativePages));if(laid!=null)laid.setItem(quill.copy());else p.getInventory().setChanged();
    }
    public static @Nullable LakeWitchEntity huntBody(ServerPlayer p,LabyrinthPlace place,BlockPos rel){var d=LabyrinthData.get(p.server);var world=shared(d,place);if(world.hasUUID("Witch")){var e=p.serverLevel().getEntity(world.getUUID("Witch"));if(e instanceof LakeWitchEntity w){w.literaryHunt(base(p,place),place);return w;}return null;}var w=DrownedTownRegistry.LAKE_WITCH.get().create(p.serverLevel());if(w==null)return null;w.setNoAi(true);w.setNoGravity(true);w.literaryHunt(base(p,place),place);w.setPersistenceRequired();w.addTag("LiteraryHunt");w.moveTo(Vec3.atBottomCenterOf(base(p,place).offset(rel)));if(!p.serverLevel().addFreshEntity(w))return null;world.putUUID("Witch",w.getUUID());shared(d,place,world);return w;}
    private static void pursue(ServerPlayer p,Entity body,double speed,boolean water){Vec3 delta=p.position().subtract(body.position()).multiply(1,0,1);if(delta.lengthSqr()<.01)return;body.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));body.move(MoverType.SELF,delta.normalize().scale(speed*5));if(water)body.setPos(body.getX(),base(p,current(p)).getY(),body.getZ());}
    private static void hunt(ServerPlayer p,BlockPos b,LabyrinthPlace place,CompoundTag own){
        if(place==LabyrinthPlace.CAMP_BLOOD){var killer=actor(p,place,"Killer",LiteraryActor.KILLER,new BlockPos(12,0,-48),false);if(killer==null)return;if(own.getBoolean("Pursued")&&p.getZ()<b.getZ()-95&&own.getInt("Present")>180)ready(p,place,own,"escaped_past_the_last_cabins");return;}
        var witch=huntBody(p,place,place==LabyrinthPlace.MOVIE_NIGHT?MovieNight.WITCH_START:CostumeNight.WITCH_START);
        // The canoes, the pier and the film (and the costume party) go on whether or not her body is loaded here.
        if(witch==null){if(place==LabyrinthPlace.MOVIE_NIGHT)MovieNight.reader(p,b,own,null);if(place==LabyrinthPlace.COSTUME_NIGHT)CostumeNight.reader(p,b,own,null);return;}
        if(place==LabyrinthPlace.COSTUME_NIGHT)CostumeNight.reader(p,b,own,witch);
        if(place==LabyrinthPlace.MOVIE_NIGHT)MovieNight.reader(p,b,own,witch);

    }
    public static BlockPos diaryPosition(int i){return new BlockPos(new int[]{-9,7,-6,10,-11,5,12,-3}[i],0,new int[]{-12,-23,-30,-9,-36,-34,-29,-37}[i]);}
    private static int diaryIndex(BlockPos rel){for(int i=0;i<8;i++)if(rel.equals(diaryPosition(i)))return i;return -1;}
    private static void diary(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.DEVILS_ROCK;int target=Math.min(8,own.getInt("Visit")*3);if(own.getInt("ThisVisitTicks")>=100){for(int i=0;i<target;i++){if((own.getInt("DiaryPlaced")&(1<<i))!=0)continue;var at=b.offset(diaryPosition(i));if(watched(p.serverLevel(),at.getCenter())||watched(p.serverLevel(),at.above().getCenter()))continue;LiteraryRooms.prop(p.serverLevel(),b,diaryPosition(i),LiteraryPropBlock.Kind.PAGE,Direction.SOUTH);own.putInt("DiaryPlaced",own.getInt("DiaryPlaced")|(1<<i));own.putLong("ArrivalTime"+i,p.serverLevel().getGameTime());var plate=b.offset(i%2==0?-5:5,0,-14);var ps=p.serverLevel().getBlockState(plate);if(ps.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED)){p.serverLevel().setBlock(plate,ps.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED,true),3);p.serverLevel().scheduleTick(plate,ps.getBlock(),20);}sound(p,SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,plate,.35F,1);p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.SCULK_CHARGE_POP,at.getX()+.5,at.getY()+.1,at.getZ()+.5,3,.1,.1,.1,0);break;}}
        if(own.getInt("ThisVisitTicks")>=900&&Integer.bitCount(own.getInt("DiaryPlaced"))<target){int i=0;while(i<target&&(own.getInt("DiaryPlaced")&(1<<i))!=0)i++;if(i<target){own.putInt("DiaryPlaced",own.getInt("DiaryPlaced")|(1<<i));reward(p,place,own,"Diary"+i,LiteraryTexts.diary(i));}}
        int expected=(1<<target)-1;if((own.getInt("DiaryRead")&expected)==expected)own.putInt("Beat",own.getInt("Visit"));
    }
    private static boolean wheelDoor(ServerPlayer p,CompoundTag own,BlockPos rel){
        var b=base(p,LabyrinthPlace.WHEEL);
        if(rel.equals(new BlockPos(0,0,-7))&&!own.getBoolean("LeftMother")){
            var landing=wheelLanding(p,new Vec3(b.getX()-13.5,b.getY(),b.getZ()-8.5));
            if(landing!=null){p.teleportTo(p.serverLevel(),landing.x,landing.y,landing.z,180,0);if(p.position().distanceToSqr(landing)<.01)own.putBoolean("FrontLoop",true);}return true;
        }
        if(Math.abs(rel.getX())!=14||!(rel.getZ()==-17||rel.getZ()==-34||rel.getZ()==-51))return false;
        int door=(-rel.getZ()/17-1)*2+(rel.getX()>0?1:0),seen=own.getInt("WheelDoors")|(1<<door);
        int target=switch(door){case 0->1;case 1->0;case 2->4;case 3->1;case 4->5;default->3;};
        boolean mother=door==4&&Integer.bitCount(seen)>=4;
        int x=target%2==0?-14:14,z=-8-(target/2)*17;
        var landing=wheelLanding(p,mother?new Vec3(b.getX()+14.5,b.getY(),b.getZ()-58.5):new Vec3(b.getX()+x+.5,b.getY(),b.getZ()+z+.5));
        if(landing==null)return true;
        p.teleportTo(p.serverLevel(),landing.x,landing.y,landing.z,mother?0:180,0);
        if(p.position().distanceToSqr(landing)>=.01)return true;
        own.putInt("WheelDoors",seen);own.putBoolean("Memory",door==0||door==2||door==4);
        if(mother)own.putBoolean("ReachedMother",true);else if((seen&2)!=0)LiteraryRooms.at(p.serverLevel(),b,-10,0,-9,Blocks.FLOWER_POT);
        return true;
    }
    /** One authored landing shared a cell with its armchair. Keep the room, but use loaded clear floor. */
    private static @Nullable Vec3 wheelLanding(ServerPlayer p,Vec3 target){
        var level=p.serverLevel();
        for(int forward=0;forward<=1;forward++){
            var at=target.add(0,0,forward);var cell=BlockPos.containing(at);
            if(!level.hasChunkAt(cell)||!level.areEntitiesLoaded(ChunkPos.asLong(cell.getX()>>4,cell.getZ()>>4)))continue;
            if(level.getBlockState(cell.below()).getCollisionShape(level,cell.below()).isEmpty())continue;
            if(level.noCollision(p,p.getBoundingBox().move(at.subtract(p.position()))))return at;
        }
        p.displayClientMessage(Component.literal("There is no clear space beyond this door yet."),true);return null;
    }
    private static void set(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.GHOSTS_SET;boolean mark=Math.abs(p.getX()-b.getX())<=6&&p.getZ()<b.getZ()-19&&p.getZ()>b.getZ()-21&&p.serverLevel().getBlockState(p.blockPosition()).is(LiteraryRegistry.PROP.get());
        var mother=actor(p,place,"Mother",LiteraryActor.FAMILY_MOTHER,new BlockPos(-6,0,-31),false);var father=actor(p,place,"Father",LiteraryActor.FAMILY_FATHER,new BlockPos(3,0,-34),false);var child=actor(p,place,"Child",LiteraryActor.FAMILY_CHILD,new BlockPos(5,0,-33),false);
        if(mark){own.putInt("MarkTicks",own.getInt("MarkTicks")+5);if(own.getInt("MarkTicks")%40==5){camera(p,place,own,40);if(mother!=null)mother.say("We have everything we need. Say it again, for the camera.");if(father!=null)father.say("This is a good home. We are a family.");}for(int x:new int[]{-17,17})LiteraryRooms.at(p.serverLevel(),b,x,3,-25,Blocks.SEA_LANTERN);}
        else if(own.getInt("MarkTicks")>=120){own.putInt("OffMarkTicks",own.getInt("OffMarkTicks")+5);HousePackets.send(p,new LiteraryViewPayload(-1,0,0,new CompoundTag()));if(mother!=null)mother.say("The light is off. Please stop saying that.");if(child!=null){child.appearance(LiteraryActor.FAMILY_CHILD,3);if(!own.getBoolean("BehindPlaced")&&!watched(p.serverLevel(),child.getEyePosition())){child.moveTo(p.getX()-p.getLookAngle().x*3,p.getY(),p.getZ()-p.getLookAngle().z*3,0,0);own.putBoolean("BehindPlaced",true);}if(own.getBoolean("BehindPlaced")&&HouseWatchers.sees(p,child.getEyePosition()))own.putBoolean("BehindSeen",true);}if(own.getInt("OffMarkTicks")>=80&&own.contains("Confession")&&own.getBoolean("BehindSeen"))ready(p,place,own,"left_the_mark_and_read_own_confession_transcript");}
    }
    private static void camera(ServerPlayer p,LabyrinthPlace place,CompoundTag own,int ticks){var b=base(p,place);var camera=actor(p,place,"Camera",LiteraryActor.CAMERA,LiteraryRooms.CAMERA,true);if(camera==null)return;camera.setInvisible(true);camera.setNoGravity(true);camera.moveTo(b.getX()+.5,b.getY()+1.5,b.getZ()-28.5,180,8);HousePackets.send(p,new LiteraryViewPayload(camera.getId(),Math.min(60,ticks),0,new CompoundTag()));}
}
