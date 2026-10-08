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
    public static CompoundTag personal(LabyrinthData d,UUID p,LabyrinthPlace place){return d.stateEntry(state(place),p.toString());}
    public static void save(LabyrinthData d,UUID p,LabyrinthPlace place,CompoundTag own){d.setStateEntry(state(place),p.toString(),own);}
    public static CompoundTag shared(LabyrinthData d,LabyrinthPlace place){return d.stateEntry(state(place),"World");}
    public static void shared(LabyrinthData d,LabyrinthPlace place,CompoundTag s){d.setStateEntry(state(place),"World",s);}
    public static boolean participant(ServerPlayer p){return p.isAlive()&&p.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR&&!FinaleProgress.terminal(FinaleProgress.phase(p.server,p.getUUID()));}
    public static @Nullable BlockPos base(ServerPlayer p,LabyrinthPlace place){if(place==LabyrinthPlace.FAMILY_COPY||place==LabyrinthPlace.OLD_CABIN)return LiteraryCopies.base(p.server,p.getUUID(),place);var o=HouseSavedData.get(p.server).houseOrigin();return o==null?null:LabyrinthPlaces.base(o,place);}
    public static boolean inside(ServerPlayer p,LabyrinthPlace place){var b=base(p,place);return participant(p)&&b!=null&&p.level().dimension().equals(NovelRooms.dimension(place))&&(place==LabyrinthPlace.FAMILY_COPY||place==LabyrinthPlace.OLD_CABIN?LiteraryCopies.contains(p.server,p.getUUID(),place,p.position()):IndianLakeRooms.bounds(b,place).contains(p.position()));}
    public static @Nullable LabyrinthPlace current(ServerPlayer p){if(!participant(p)||!HouseDimensions.isHouseDimension(p.level().dimension()))return null;var copy=LiteraryCopies.placeAt(p.server,p.blockPosition());if(copy!=null&&inside(p,copy))return copy;var o=HouseSavedData.get(p.server).houseOrigin();if(o==null)return null;var place=LabyrinthPlaces.placeAt(o,p.blockPosition());return LiteraryRooms.isLiterary(place)&&inside(p,place)?place:null;}
    public static boolean canDeal(LabyrinthData d,UUID p,LabyrinthPlace place){if(!LiteraryRooms.isLiterary(place))return false;var world=d.state("literary_cabin_closure_0436");if(world.getString("Retired").equals(place.id()))return false;
        var story=WitnessAccount.Story.of(place.id());if(story!=null&&WitnessAccount.has(d,p,story))return false;
        if(place==LabyrinthPlace.ELK_FAN&&!personal(d,p,LabyrinthPlace.ELK_LOT).getBoolean("Ready"))return false;
        if(place==LabyrinthPlace.FAMILY_COPY||place==LabyrinthPlace.OLD_CABIN)return d.isReady("literary_copy_"+p+"_"+place.id())&&!personal(d,p,place).getBoolean("Completed");
        return place.kind()==LabyrinthPlace.Kind.RECURRING||!personal(d,p,place).getBoolean("Completed");}
    public static void onArrive(ServerPlayer p,LabyrinthPlace place){if(!LiteraryRooms.isLiterary(place)||!participant(p))return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID(),place);
        int visit=own.getInt("Visit");if(visit==0||own.getInt("Beat")>=visit)own.putInt("Visit",visit+1);own.putBoolean("Here",true);own.putInt("Present",0);own.putInt("ThisVisitTicks",0);own.putInt("ExamineTicks",0);own.putInt("FanTicks",0);own.putInt("StillTicks",0);own.putBoolean("Betrayed",false);own.remove("LastPosition");own.remove("CameraLease");own.putBoolean("Interrupted",false);
        save(d,p.getUUID(),place,own);if(place==LabyrinthPlace.FAMILY_COPY||place==LabyrinthPlace.OLD_CABIN)LiteraryCopies.arrived(p,place);
        if (place == LabyrinthPlace.HOLY_RABBIT && !rabbitCommitted(own)) {
            own.putBoolean("Committed", false);
            save(d, p.getUUID(), place, own);
        }
        if (place == LabyrinthPlace.CHILD_ROOM && !own.getBoolean("ExitProof0437")) {
            if (!own.getBoolean("Completed") && !WitnessAccount.has(d,p.getUUID(),WitnessAccount.Story.CHILD_ROOM)) own.putBoolean("Ready", false);
            own.putBoolean("ExitProof0437", true);
            own.putInt("ExitsAtArrival", shared(d, place).getInt("LostExits"));
            save(d, p.getUUID(), place, own);
        }
        if (place == LabyrinthPlace.MOVIE_NIGHT) ensureMovieCanoe(p, base(p, place));
        if (place == LabyrinthPlace.ELK_CARCASSES) { ElkHunt.arrive(p, own); save(d, p.getUUID(), place, own); }
    }
    public static ItemStack mark(ServerPlayer p,LabyrinthPlace place,String key,ItemStack item){var s=VignetteYields.mark(item.copy(),place.id());CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putUUID(OWNER,p.getUUID());t.putString(KEY,key);t.putString(PLACE,place.id());});return s;}
    public static void give(ServerPlayer p,ItemStack s){if(p.getInventory().add(s))return;var e=new ItemEntity(p.serverLevel(),p.getX(),p.getY()+.2,p.getZ(),s);e.setTarget(p.getUUID());p.serverLevel().addFreshEntity(e);}
    public static void reward(ServerPlayer p,LabyrinthPlace place,CompoundTag own,String key,ItemStack s){if(own.getBoolean("Yield_"+key))return;own.putBoolean("Yield_"+key,true);save(LabyrinthData.get(p.server),p.getUUID(),place,own);give(p,mark(p,place,key,s));}
    public static void ready(ServerPlayer p,LabyrinthPlace place,CompoundTag own,String outcome){if(!own.getBoolean("Ready")){own.putBoolean("Ready",true);own.putString("Outcome",outcome);p.displayClientMessage(Component.literal("The last account is here. Read it before you leave."),false);}own.putInt("Beat",own.getInt("Visit"));}
    public static void open(ServerPlayer p,LabyrinthPlace place,String key,ItemStack book,BlockPos surface){open(p,place,personal(LabyrinthData.get(p.server),p.getUUID(),place),key,book,surface);}
    private static void open(ServerPlayer p,LabyrinthPlace place,CompoundTag own,String key,ItemStack book,BlockPos surface){if(!inside(p,place)||p.distanceToSqr(surface.getCenter())>36)return;var d=LabyrinthData.get(p.server);String slot="Original_"+key;
        if(!own.contains(slot))own.put(slot,mark(p,place,key,book).save(p.registryAccess()));var original=ItemStack.parseOptional(p.registryAccess(),own.getCompound(slot));save(d,p.getUUID(),place,own);
        p.openMenu(new SimpleMenuProvider((id,inv,who)->new Pages(id,p,place,key,original,true,true,surface),original.getHoverName()));}
    public static final class Pages extends LecternMenu {
        private final ServerPlayer reader;private final LabyrinthPlace place;private final String key;private final ItemStack book;private final boolean owned,surface;private final BlockPos at;
        Pages(int id,ServerPlayer p,LabyrinthPlace place,String key,ItemStack book,boolean owned,boolean surface,BlockPos at){super(id,container(book),new SimpleContainerData(1));this.reader=p;this.place=place;this.key=key;this.book=book.copy();this.owned=owned;this.surface=surface;this.at=at;}
        private static Container container(ItemStack s){var c=new SimpleContainer(1);c.setItem(0,s.copy());return c;}
        public ItemStack original(){return book.copy();}
        @Override public boolean clickMenuButton(Player who,int button){if(who!=reader||!participant(reader)||(surface&&(!inside(reader,place)||reader.distanceToSqr(at.getCenter())>36)))return false;var d=LabyrinthData.get(reader.server);var own=personal(d,reader.getUUID(),place);
            if(button==3){if(!surface||!owned||own.getBoolean("Taken_"+key))return false;own.putBoolean("Taken_"+key,true);save(d,reader.getUUID(),place,own);give(reader,book.copy());return true;}
            if(!super.clickMenuButton(who,button))return false;int pages=book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();if(owned&&getPage()==pages-1){own.putBoolean("Read_"+key,true);if(key.startsWith("Diary")){int index=Integer.parseInt(key.substring(5));own.putInt("DiaryRead",own.getInt("DiaryRead")|(1<<index));if(own.getInt("DiaryRead")==255)ready(reader,place,own,"read_all_eight_original_pages");}
                if(key.equals("Ending")&&inside(reader,place)&&own.getBoolean("Read_Source")&&own.getBoolean("Ready")&&!own.getBoolean("Completed")){var story=WitnessAccount.Story.of(place.id());if(story!=null){WitnessAccount.resolve(reader,story,own.getString("Outcome"));own.putBoolean("Completed",true);d.setCompleted(place.id(),true);finishReward(reader,place,own);}}
                save(d,reader.getUUID(),place,own);
            }return true;}
    }
    private static void finishReward(ServerPlayer p,LabyrinthPlace place,CompoundTag own){switch(place){case ELK_FAN->reward(p,place,own,"Tooth",new ItemStack(LiteraryRegistry.TOOTH.get()));case WHEEL->reward(p,place,own,"Key",new ItemStack(LiteraryRegistry.KEY.get()));case END_WORLD_CABIN->reward(p,place,own,"Jar",new ItemStack(LiteraryRegistry.JAR.get()));case GHOSTS_SET->reward(p,place,own,"Film",playable(p,new ItemStack(LiteraryRegistry.FILM.get()),"family_film"));case HOLY_RABBIT->{reward(p,place,own,"Knife",new ItemStack(LiteraryRegistry.KNIFE.get()));reward(p,place,own,"RabbitFoot",new ItemStack(Items.RABBIT_FOOT));}default->{}}
    }
    private static ItemStack playable(ServerPlayer p,ItemStack stack,String song){return stack;}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void collected(PlayerInteractEvent.RightClickItem e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p))return;var s=p.getItemInHand(e.getHand());var custom=s.get(DataComponents.CUSTOM_DATA);if(custom==null||!s.has(DataComponents.WRITTEN_BOOK_CONTENT))return;var t=custom.copyTag();var place=LabyrinthPlace.byId(t.getString(PLACE));if(!LiteraryRooms.isLiterary(place)||!t.contains(KEY))return;boolean own=t.hasUUID(OWNER)&&t.getUUID(OWNER).equals(p.getUUID());p.openMenu(new SimpleMenuProvider((id,inv,who)->new Pages(id,p,place,t.getString(KEY),s.copy(),own,false,p.blockPosition()),s.getHoverName()));e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void click(PlayerInteractEvent.RightClickBlock e){if(e.isCanceled()||e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p))return;var place=current(p);if(place==null||p.distanceToSqr(e.getPos().getCenter())>36)return;
        if(place==LabyrinthPlace.ELK_CARCASSES)CarcassHunt.noise(p,e.getPos());var b=base(p,place);var rel=e.getPos().subtract(b);var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID(),place);boolean handled=false;
        if(rel.equals(LiteraryRooms.source(place))||SceneReview.readingAlias(p.serverLevel(),b,place,rel)){open(p,place,own,"Source",SceneHuntReview.sourceBook(p.serverLevel(),b,place),e.getPos());handled=true;}
        else if(rel.equals(LiteraryRooms.ending(place))&&own.getBoolean("Ready")){open(p,place,own,"Ending",LiteraryTexts.ending(p,place,own),e.getPos());handled=true;}
        else switch(place){
            case MASQUE->{if(rel.equals(LiteraryRooms.CLOCK)&&own.getInt("Rooms")==127){var s=p.serverLevel().getBlockState(e.getPos());if(s.is(LiteraryRegistry.PROP.get()))p.serverLevel().setBlock(e.getPos(),s.setValue(LiteraryPropBlock.STAGE,3),F);ready(p,place,own,"stopped_the_seventh_room_clock");handled=true;}}
            case USHER->{if(rel.distManhattan(LiteraryRooms.COFFIN)<=1){chooseLid(p,own);handled=true;}}
            case ELK_FAN->{if(rel.equals(SceneHuntReview.TOOLS)){reward(p,place,own,"RepairBoards",new ItemStack(Items.OAK_PLANKS,8));handled=true;}else if(rel.equals(LiteraryRooms.FAN)&&p.getY()>=b.getY()+4.5&&own.getBoolean("BuiltReach")){own.putBoolean("Repaired",true);handled=true;}}
            case MAPPING_INTERIOR->{if(rel.equals(LiteraryRooms.PILE)&&own.getBoolean("BeyondRoof")&&!own.getBoolean("PileChosen")){own.putBoolean("PileChosen",true);own.putBoolean("PileBroken",false);own.putInt("ChoiceVisit",own.getInt("Visit"));own.putInt("Beat",own.getInt("Visit"));reward(p,place,own,"Photograph",new ItemStack(LiteraryRegistry.PHOTOGRAPH.get()));handled=true;}}
            case CRIMSON_HALL->{int i=List.of(new BlockPos(-12,0,-35),new BlockPos(12,0,-35),new BlockPos(0,7,-39)).indexOf(rel);if(i>=0){Item[] discs={LiteraryRegistry.CYLINDER_ONE.get(),LiteraryRegistry.CYLINDER_TWO.get(),LiteraryRegistry.CYLINDER_THREE.get()};reward(p,place,own,"Cylinder"+i,playable(p,new ItemStack(discs[i]),"wax_cylinder_"+(i+1)));open(p,place,own,"CylinderText"+i,LiteraryTexts.cylinder(i),e.getPos());handled=true;}}
            case DEVILS_ROCK->{int index=diaryIndex(rel);if(index>=0&&(own.getInt("DiaryPlaced")&(1<<index))!=0){open(p,place,own,"Diary"+index,LiteraryTexts.diary(index,p.getGameProfile().getName()),e.getPos());handled=true;}else if(rel.equals(LiteraryRooms.CAMERA)){camera(p,place,own,45);own.putInt("CameraPrints",own.getInt("CameraPrints")+1);var arrivals=new StringBuilder("MOTION PRINT\n\n");for(int i=0;i<8;i++)if(own.contains("ArrivalTime"+i))arrivals.append("Page ").append(i+1).append(": ").append(own.getLong("ArrivalTime"+i)).append(" ticks\n");arrivals.append("\nThe paper was behind the lens before the image became mine.");open(p,place,own,"CameraPrint",HouseWriting.book("Motion print",p.getGameProfile().getName(),HouseWriting.WritingStyle.WILL,List.of(arrivals.toString())),e.getPos());handled=true;}}
            case WHEEL->{handled=wheelDoor(p,own,rel);}
            case GHOSTS_SET->{if(rel.equals(LiteraryRooms.BOOTH)){own.putBoolean("Booth",true);if(p.isShiftKeyDown()&&!own.contains("Confession"))own.putString("Confession","I chose to let the tape run in silence.");p.displayClientMessage(Component.literal("The booth is recording. Type your own words in chat, or crouch and touch the receiver to leave silence."),false);handled=true;}}
            case CONFESSION->{if(p.serverLevel().getBlockState(e.getPos()).is(Blocks.BOOKSHELF)&&Math.abs(rel.getX())==11){ConfessionBooks.shelf(p,own);handled=true;}
                else if(rel.equals(SceneReview.BOOK_TRAY)&&p.serverLevel().getBlockState(e.getPos()).is(HouseBlocks.VIGNETTE_DETAIL.get())){ConfessionBooks.table(p,own);handled=true;}
                else if(rel.equals(new BlockPos(10,1,-29))&&own.getInt("Visit")>own.getInt("SealedVisit")&&own.contains("SignedJournal")){open(p,place,own,"Journal",ItemStack.parseOptional(p.registryAccess(),own.getCompound("SignedJournal")),e.getPos());handled=true;}}
            case END_WORLD_CABIN->{if(rel.equals(LiteraryRooms.TV)){LiteraryCabinChoices.open(p);handled=true;}}
            case FAMILY_COPY,OLD_CABIN->{if(p.serverLevel().getBlockEntity(e.getPos()) instanceof net.minecraft.world.Container||p.serverLevel().getBlockState(e.getPos()).is(Blocks.ENDER_CHEST)){p.displayClientMessage(Component.literal("Every compartment is empty. None of the latches will open."),true);handled=true;}else handled=LiteraryCopies.click(p,place,e.getPos(),own);}
            default->{}
        }
        save(d,p.getUUID(),place,own);if(handled){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void knock(PlayerInteractEvent.LeftClickBlock e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||e.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START)return;var place=current(p);if(place==null)return;var own=personal(LabyrinthData.get(p.server),p.getUUID(),place);var b=base(p,place);if(p.distanceToSqr(e.getPos().getCenter())>36)return;
        if(place==LabyrinthPlace.HILL_NURSERY&&e.getPos().equals(b.offset(LiteraryRooms.HILL_KNOCK))){own.putInt("Knocks",own.getInt("Knocks")+1);own.putInt("EchoesPending",own.getInt("EchoesPending")+1);own.putInt("EchoDelay",10);e.setCanceled(true);save(LabyrinthData.get(p.server),p.getUUID(),place,own);}
        else if(place==LabyrinthPlace.FAMILY_COPY)LiteraryCopies.knock(p,e.getPos());
    }
    public static boolean mayBreak(ServerPlayer p,BlockPos pos){var place=current(p);if(place==null)return false;var b=base(p,place);var s=p.serverLevel().getBlockState(pos);return (place==LabyrinthPlace.WINTER_LAKE&&s.is(Blocks.ICE)&&pos.getY()==b.getY()-1&&Math.abs(pos.getX()-b.getX())<=28&&pos.getZ()<b.getZ()-12&&pos.getZ()>b.getZ()-73)
        ||(place==LabyrinthPlace.CRIMSON_HALL&&s.is(Blocks.SNOW)&&p.getMainHandItem().getItem() instanceof ShovelItem)
        ||(place==LabyrinthPlace.MAPPING_INTERIOR&&pos.equals(b.offset(LiteraryRooms.PILE))&&s.is(LiteraryRegistry.PROP.get())&&personal(LabyrinthData.get(p.server),p.getUUID(),place).getBoolean("BeyondRoof"))
        ||(place==LabyrinthPlace.ELK_FAN&&personal(LabyrinthData.get(p.server),p.getUUID(),place).getList("Platforms",Tag.TAG_LONG).stream().anyMatch(t->((LongTag)t).getAsLong()==pos.asLong()));}
    public static boolean mayPlace(ServerPlayer p,BlockPos pos){if(!inside(p,LabyrinthPlace.ELK_FAN))return false;var b=base(p,LabyrinthPlace.ELK_FAN);var rel=pos.subtract(b);return rel.getX()>=-2&&rel.getX()<=2&&rel.getZ()>=-18&&rel.getZ()<=-15&&rel.getY()>=0&&rel.getY()<=4;}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void broken(BlockEvent.BreakEvent e){if(e.isCanceled()||!(e.getPlayer() instanceof ServerPlayer p)||!mayBreak(p,e.getPos()))return;var place=current(p);var own=personal(LabyrinthData.get(p.server),p.getUUID(),place);
        if(place==LabyrinthPlace.WINTER_LAKE){own.putBoolean("BrokeIce",true);own.putLong("IceHole",e.getPos().asLong());}
        if(place==LabyrinthPlace.CRIMSON_HALL)own.putBoolean("ClearedSnow",true);
        if(place==LabyrinthPlace.MAPPING_INTERIOR&&!own.getBoolean("PileChosen")){own.putBoolean("PileChosen",true);own.putBoolean("PileBroken",true);own.putInt("ChoiceVisit",own.getInt("Visit"));own.putInt("Beat",own.getInt("Visit"));}
        save(LabyrinthData.get(p.server),p.getUUID(),place,own);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void placed(BlockEvent.EntityPlaceEvent e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!mayPlace(p,e.getPos()))return;var own=personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.ELK_FAN);var platforms=own.getList("Platforms",Tag.TAG_LONG);if(platforms.size()<40)platforms.add(LongTag.valueOf(e.getPos().asLong()));own.put("Platforms",platforms);own.putBoolean("BuiltReach",true);save(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.ELK_FAN,own);}
    private static void chooseLid(ServerPlayer p,CompoundTag own){var place=LabyrinthPlace.USHER;var d=LabyrinthData.get(p.server);if(own.getBoolean("LidChosen"))return;boolean close=p.isShiftKeyDown();var world=shared(d,place);if(!world.contains("LidClosed")){world.putBoolean("LidClosed",close);world.putInt("ChoiceVisit",own.getInt("Visit"));shared(d,place,world);}close=world.getBoolean("LidClosed");own.putBoolean("LidChosen",true);own.putBoolean("LidClosed",close);own.putInt("ChoiceVisit",own.getInt("Visit"));own.putInt("Beat",own.getInt("Visit"));var b=base(p,place);for(var rel:List.of(LiteraryRooms.COFFIN,LiteraryRooms.COFFIN.north())){var s=p.serverLevel().getBlockState(b.offset(rel));if(s.is(LiteraryRegistry.PROP.get()))p.serverLevel().setBlock(b.offset(rel),s.setValue(LiteraryPropBlock.STAGE,close?1:0),F);}p.displayClientMessage(Component.literal(close?"The lid has closed. Return and listen.":"The lid stays open. Return to the wing."),false);}
    public static void elkWound(ServerPlayer p){var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID(),LabyrinthPlace.ELK_LOT);own.putBoolean("ElkWounded",true);save(d,p.getUUID(),LabyrinthPlace.ELK_LOT,own);}
    public static void ate(ServerPlayer p,ItemStack s){var custom=s.get(DataComponents.CUSTOM_DATA);if(custom==null)return;var t=custom.copyTag();if(!t.hasUUID(OWNER)||!t.getUUID(OWNER).equals(p.getUUID()))return;var place=current(p);if(place==null)return;var own=personal(LabyrinthData.get(p.server),p.getUUID(),place);
        if(place==LabyrinthPlace.HOLY_RABBIT&&s.is(LiteraryRegistry.MEAL.get())&&t.getInt("Night")==own.getInt("Night")&&own.getInt("AteNight")!=own.getInt("Night")&&t.hasUUID("MealToken")&&own.hasUUID("MealToken")&&t.getUUID("MealToken").equals(own.getUUID("MealToken"))&&p.distanceToSqr(base(p,place).offset(0,0,-24).getCenter())<64){own.putBoolean("Committed",true);own.putString("CommitAction","ate_an_owned_meal");own.putInt("Meals",own.getInt("Meals")+1);own.putInt("AteNight",own.getInt("Night"));own.putInt("Drowse",0);}
        if(place==LabyrinthPlace.FAMILY_COPY&&s.is(LiteraryRegistry.STEW.get())&&own.getInt("Visit")==4&&t.hasUUID("MealToken")&&own.hasUUID("MealToken")&&t.getUUID("MealToken").equals(own.getUUID("MealToken"))){own.putBoolean("DinnerEaten",true);own.putInt("Beat",4);}
        save(LabyrinthData.get(p.server),p.getUUID(),place,own);
    }
    public static void talk(ServerPlayer p,LiteraryActor actor){var place=current(p);if(place==null||!actor.scene().equals(place.id())||p.distanceToSqr(actor)>36||actor.owner().isPresent()&&!actor.owner().get().equals(p.getUUID()))return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID(),place);
        if(place==LabyrinthPlace.END_WORLD_CABIN)LiteraryCabinChoices.open(p);
        else if(place==LabyrinthPlace.FAMILY_COPY||place==LabyrinthPlace.OLD_CABIN)LiteraryCopies.talk(p,place,actor,own);
        else if (place == LabyrinthPlace.HOLY_RABBIT && actor.role() == LiteraryActor.FATHER) {
            if (!rabbitCommitted(own) && !own.getBoolean("Ready")) {
                var icons = new HashMap<Integer, ItemStack>();
                icons.put(0, LiteraryChoiceMenu.icon(Items.CAMPFIRE, "Stay with Father through the cold nights"));
                icons.put(8, LiteraryChoiceMenu.icon(Items.OAK_DOOR, "Leave the choice for now"));
                LiteraryChoiceMenu.open(p, "Stay through the nights?", icons,
                        () -> inside(p, place) && p.distanceToSqr(actor) <= 36,
                        slot -> slot == 8 || slot == 0 && commitRabbit(p));
            } else actor.say(own.contains("RabbitName") ? "It is still out there. " + own.getString("RabbitName") + ". Alive." : "Stay awake. Take what I brought.");
        }
        else if(place==LabyrinthPlace.CONFESSION)actor.say("Bring a blank book. Sit where you can hear me.");
        save(d,p.getUUID(),place,own);
    }
    public static void attacked(ServerPlayer p,LiteraryActor actor){var place=current(p);if(place==null||!actor.scene().equals(place.id())||actor.owner().isPresent()&&!actor.owner().get().equals(p.getUUID()))return;
        // Blows do not stop him and do not end the visit: the only way through is to get away, or to hide.
        if(place==LabyrinthPlace.ELK_CARCASSES&&actor.role()==LiteraryActor.KILLER)return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID(),place);
        if(place==LabyrinthPlace.HOLY_RABBIT&&actor.role()==LiteraryActor.FATHER){actor.appearance(actor.role(),Math.min(2,actor.phase()));actor.say("I'm awake. I'm here.");own.putInt("FatherAwake",own.getInt("FatherAwake")+1);own.putInt("Drowse",Math.max(0,own.getInt("Drowse")-100));}
        else{own.putBoolean("Interrupted",true);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS,80));actor.say("You need to leave.");LabyrinthDoors.goToJunction(p);}
        save(d,p.getUUID(),place,own);
    }
    public static @Nullable LiteraryActor actor(ServerPlayer p,LabyrinthPlace place,String key,int role,BlockPos relative,boolean privateActor){var d=LabyrinthData.get(p.server);var s=shared(d,place);String id=privateActor?key+"_"+p.getUUID():key;
        if(s.hasUUID(id)){var e=p.serverLevel().getEntity(s.getUUID(id));return e instanceof LiteraryActor a?a:null;}
        var a=LiteraryRegistry.ACTOR.get().create(p.serverLevel());if(a==null)return null;a.bind(place,privateActor?p.getUUID():null);a.appearance(role,0);a.moveTo(Vec3.atBottomCenterOf(base(p,place).offset(relative)));a.setYRot(0);if(!p.serverLevel().addFreshEntity(a))return null;s.putUUID(id,a.getUUID());shared(d,place,s);return a;
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
        p.getFoodData().setFoodLevel(Math.min(6, p.getFoodData().getFoodLevel()));
        save(d, p.getUUID(), LabyrinthPlace.HOLY_RABBIT, own);
        return true;
    }
    public static boolean retreatLocked(ServerPlayer p) {
        if (!inside(p, LabyrinthPlace.HOLY_RABBIT)) return false;
        var own = personal(LabyrinthData.get(p.server), p.getUUID(), LabyrinthPlace.HOLY_RABBIT);
        return rabbitCommitted(own) && !own.getBoolean("Ready");
    }
    private static void scale(ServerPlayer p,boolean child){var a=p.getAttribute(Attributes.SCALE);if(a==null)return;if(child&&!a.hasModifier(CHILD))a.addTransientModifier(new AttributeModifier(CHILD,-.46,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));else if(!child)a.removeModifier(CHILD);}
    private static void crawl(ServerPlayer p,boolean active){var c=CRAWLS.get(p.getUUID());if(c!=null&&c.p()!=p){restore(c);CRAWLS.remove(p.getUUID());c=null;}if(active&&c==null){CRAWLS.put(p.getUUID(),new Crawl(p,p.getForcedPose(),p.getPose()));p.setForcedPose(Pose.SWIMMING);p.setPose(Pose.SWIMMING);p.refreshDimensions();}else if(!active&&c!=null){restore(c);CRAWLS.remove(p.getUUID());}}
    private static void restore(Crawl c){c.p().setForcedPose(c.forced());c.p().setPose(c.forced()==null?c.displayed():c.forced());c.p().refreshDimensions();}
    public static void clearAll(){for(var c:CRAWLS.values())restore(c);CRAWLS.clear();MOVIE_CANOES.clear();}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){crawl(p,false);scale(p,false);}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void frozenFood(LivingEntityUseItemEvent.Start e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!retreatLocked(p))return;var s=e.getItem();if(s.is(Items.SNOWBALL)&&s.has(DataComponents.FOOD))return;if(s.is(LiteraryRegistry.MEAL.get())){var t=s.get(DataComponents.CUSTOM_DATA);if(t!=null&&t.copyTag().hasUUID(OWNER)&&t.copyTag().getUUID(OWNER).equals(p.getUUID()))return;}if(s.has(DataComponents.FOOD)){e.setCanceled(true);p.displayClientMessage(Component.literal("It is frozen hard. He is bringing something down to you."),true);}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void snowNibble(PlayerInteractEvent.RightClickItem e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!inside(p,LabyrinthPlace.HOLY_RABBIT)||!p.getItemInHand(e.getHand()).is(Items.SNOWBALL))return;var snow=p.getItemInHand(e.getHand());snow.set(DataComponents.FOOD,new net.minecraft.world.food.FoodProperties.Builder().nutrition(0).saturationModifier(0).alwaysEdible().build());p.startUsingItem(e.getHand());e.setCanceled(true);e.setCancellationResult(InteractionResult.CONSUME);}
    @SubscribeEvent public static void pearl(EntityJoinLevelEvent e){if(e.getEntity() instanceof ThrownEnderpearl pearl&&pearl.getOwner() instanceof ServerPlayer p&&retreatLocked(p)){e.setCanceled(true);pearl.discard();}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void chat(ServerChatEvent e){var p=e.getPlayer();var place=current(p);if(place==LabyrinthPlace.GHOSTS_SET&&p.distanceToSqr(base(p,place).offset(LiteraryRooms.BOOTH).getCenter())<36){var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID(),place);if(own.getBoolean("Booth")&&!own.contains("Confession")){own.putString("Confession",e.getRawText().substring(0,Math.min(512,e.getRawText().length())));save(d,p.getUUID(),place,own);}}else if(place==LabyrinthPlace.OLD_CABIN)LiteraryCopies.nameSpoken(p,e.getRawText());}
    @SubscribeEvent public static void travel(net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre e){if(e.getEntity() instanceof ServerPlayer p&&retreatLocked(p)){p.stopFallFlying();p.stopRiding();}}
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){var server=event.getServer();LiteraryCopies.tick(server);LiteraryCabinChoices.reconcileClosure(server);if(server.getTickCount()%5!=0)return;
        for(var p:server.getPlayerList().getPlayers()){var place=current(p);if(place==null){crawl(p,false);scale(p,false);continue;}var d=LabyrinthData.get(server);var own=personal(d,p.getUUID(),place);if(!own.getBoolean("Here"))continue;var b=base(p,place);IndianLakeRooms.keepLoaded(p.serverLevel(),b,place);own.putInt("Present",Math.min(72000,own.getInt("Present")+5));own.putInt("ThisVisitTicks",Math.min(72000,own.getInt("ThisVisitTicks")+5));
            boolean child=place==LabyrinthPlace.CHILD_ROOM||place==LabyrinthPlace.HOLY_RABBIT||(place==LabyrinthPlace.WHEEL&&own.getBoolean("Memory"));scale(p,child);crawl(p,(place==LabyrinthPlace.MAPPING_INTERIOR&&p.getY()<b.getY()-2)||(place==LabyrinthPlace.CHILD_ROOM&&p.getY()<b.getY()-1)||(place==LabyrinthPlace.ELK_CARCASSES&&ElkHunt.crawl(p,b,CRAWLS.containsKey(p.getUUID()))));
            if(!own.getBoolean("Interrupted"))switch(place){case HILL_NURSERY->hill(p,b,own);case MINIATURES->miniatures(p,b,own);case MASQUE->masque(p,b,own);case USHER->usher(p,b,own);case WINCHESTER->{if(p.getY()>=b.getY()+7&&p.getZ()<b.getZ()-65&&own.getInt("Present")>=100)ready(p,place,own,"reached_the_upper_construction_ledger");}case CHILD_ROOM->child(p,b,own);case CRIMSON_HALL->crimson(p,b,own);case BLY_ROUTE->bly(p,b,own);case ELK_LOT->lot(p,b,own);case ELK_FAN->fan(p,b,own);case MAPPING_INTERIOR->mapping(p,b,own);case HOLY_RABBIT->rabbit(p,b,own);case CONFESSION->confession(p,b,own);case ELK_CARCASSES->ElkHunt.tick(p,b,own);case COSTUME_NIGHT,MOVIE_NIGHT,WINTER_LAKE,CAMP_BLOOD->hunt(p,b,place,own);case DEVILS_ROCK->diary(p,b,own);case WHEEL->{var mother=actor(p,place,"Mother",LiteraryActor.MOTHER,new BlockPos(14,0,-59),true);if(own.getBoolean("ReachedMother")&&mother!=null&&p.distanceToSqr(mother)<36&&HouseWatchers.sees(p,mother.getEyePosition())){own.putBoolean("LeftMother",true);mother.say("You can leave now. The door will open.");}if(own.getBoolean("LeftMother")&&p.getZ()>b.getZ()-6){own.putBoolean("Memory",false);ready(p,place,own,"left_after_finding_mothers_room");}}case GHOSTS_SET->set(p,b,own);case END_WORLD_CABIN->LiteraryCabinChoices.tick(p,own);case FAMILY_COPY,OLD_CABIN->LiteraryCopies.sceneTick(p,place,own);default->{}}
            save(d,p.getUUID(),place,own);
        }
    }
    private static void sound(ServerPlayer p,SoundEvent sound,BlockPos at,float volume,float pitch){p.serverLevel().playSound(null,at,sound,SoundSource.BLOCKS,volume,pitch);}
    private static void hill(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.HILL_NURSERY;var rel=p.blockPosition().subtract(b);if(rel.getX()<-3&&rel.getZ()<-10&&rel.getZ()>-25){p.setTicksFrozen(Math.min(150,p.getTicksFrozen()+20));own.putBoolean("ColdDoor",true);}
        if(HouseWatchers.sees(p,b.offset(LiteraryRooms.HILL_WALL).getCenter()))own.putBoolean("NameSeen",true);
        if(own.getInt("Present")%80==0){int z=Math.max(-41,Math.min(-4,rel.getZ()));sound(p,SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,b.offset(rel.getX()<-3?-4:rel.getX()>3?4:3,1,z),.75F,.75F);}
        if(own.getInt("EchoesPending")>0){int delay=own.getInt("EchoDelay")-5;own.putInt("EchoDelay",delay);if(delay<=0){sound(p,SoundEvents.STONE_HIT,b.offset(LiteraryRooms.HILL_KNOCK),.8F,.65F);own.putInt("EchoesPending",own.getInt("EchoesPending")-1);own.putInt("Echoes",own.getInt("Echoes")+1);own.putInt("EchoDelay",10);}}
        if(own.getBoolean("ColdDoor")&&own.getBoolean("NameSeen")&&own.getInt("Knocks")>=3&&own.getInt("Echoes")==own.getInt("Knocks"))ready(p,place,own,"heard_each_cellar_knock_return");
    }
    private static void miniatures(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.MINIATURES;var d=LabyrinthData.get(p.server);var at=b.offset(0,1,-18);if(p.distanceToSqr(at.getCenter())>25||!HouseWatchers.sees(p,at.getCenter()))return;
        if(own.getInt("ModelVisit")!=own.getInt("Visit")){var visits=d.visited(p.getUUID()).stream().map(LabyrinthPlace::byId).filter(Objects::nonNull).filter(v->v!=place&&v.room()!=null&&v!=LabyrinthPlace.FAMILY_COPY&&v!=LabyrinthPlace.OLD_CABIN).sorted(Comparator.comparingInt(v->{int age=d.recentVisit(p.getUUID(),v);return age<0?100+v.slot():age;})).toList();if(visits.isEmpty())return;
            var samples=new ListTag();for(int n=0;n<Math.min(3,visits.size());n++){var room=visits.get(n);boolean preview=false;if(n==2&&own.getInt("Visit")%2==0){var future=LabyrinthDealer.vignettesAvailable(d,p.getUUID()).stream().filter(v->v!=place&&v.room()!=null&&v!=LabyrinthPlace.FAMILY_COPY&&v!=LabyrinthPlace.OLD_CABIN&&!d.visited(p.getUUID()).contains(v.id())).sorted(Comparator.comparingInt(LabyrinthPlace::slot)).findFirst();if(future.isPresent()){room=future.get();preview=true;}}var origin=HouseSavedData.get(p.server).houseOrigin();var rb=LabyrinthPlaces.base(origin,room);var rl=p.server.getLevel(NovelRooms.dimension(room));if(rb==null||rl==null)continue;var sample=new CompoundTag();sample.putString("Room",room.id());sample.putBoolean("Preview",preview);int[] states=new int[256];for(int i=0;i<256;i++){int x=i%8-4,y=i/64-1,z=-3-(i/8)%8;states[i]=Block.getId(rl.getBlockState(rb.offset(x,y,z)));}sample.putIntArray("Blocks",states);sample.putBoolean("Behind",n==0);samples.add(sample);}
            own.put("Models",samples);own.putInt("ModelVisit",own.getInt("Visit"));
        }
        HousePackets.send(p,new LiteraryViewPayload(-1,0,0,modelDisplay(p,own)));
        own.putInt("ExamineTicks",own.getInt("ExamineTicks")+5);if(own.getInt("ExamineTicks")>=60){own.putInt("ModelsExamined",Math.max(own.getInt("ModelsExamined"),own.getInt("Visit")));own.putInt("Beat",own.getInt("Visit"));if(own.getInt("ModelsExamined")>=3)ready(p,place,own,"examined_three_personal_model_arrangements");}
    }
    public static CompoundTag modelDisplay(ServerPlayer p,CompoundTag own){var t=new CompoundTag();t.put("Models",own.getList("Models",Tag.TAG_COMPOUND).copy());t.putUUID("Reader",p.getUUID());t.putString("Name",p.getGameProfile().getName());return t;}
    private static void masque(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.MASQUE;int room=Math.max(0,Math.min(6,(-p.blockPosition().getZ()+b.getZ()-1)/11));own.putInt("Rooms",own.getInt("Rooms")|(1<<room));int time=(int)(p.serverLevel().getGameTime()%400);boolean chime=time<60;var d=LabyrinthData.get(p.server);var world=shared(d,place);boolean stopped=world.getBoolean("Stopped");var clock=p.serverLevel().getBlockState(b.offset(LiteraryRooms.CLOCK));if(clock.is(LiteraryRegistry.PROP.get())&&clock.getValue(LiteraryPropBlock.STAGE)==3){stopped=true;world.putBoolean("Stopped",true);shared(d,place,world);}
        for(int i=0;i<7;i++)for(int side:new int[]{-1,1}){var dancer=actor(p,place,"Dancer"+i+"_"+side,LiteraryActor.DANCER,new BlockPos(side*5,0,-11*i-6),false);if(dancer!=null){dancer.appearance(LiteraryActor.DANCER,chime||stopped?0:1);if(!chime&&!stopped)dancer.setYRot((p.serverLevel().getGameTime()%360));}}
        if(!stopped&&time<5)sound(p,SoundEvents.BELL_BLOCK,b.offset(LiteraryRooms.CLOCK),2,.6F);
        if(!stopped&&time==65)sound(p,LiteraryRegistry.PARTY.get(),b.offset(0,0,-room*11-6),.65F,1);
        if(chime&&!stopped&&own.contains("LastPosition")){Vec3 last=new Vec3(own.getDouble("LastX"),p.getY(),own.getDouble("LastZ"));if(last.distanceToSqr(p.position())>.0025){var red=actor(p,place,"RedFigure"+room,LiteraryActor.RED_FIGURE,new BlockPos(0,0,-11*room-8),false);if(red!=null)red.say("The clock is speaking.");own.putBoolean("MovedAtChime",true);}}
        own.putLong("LastPosition",p.blockPosition().asLong());own.putDouble("LastX",p.getX());own.putDouble("LastZ",p.getZ());
    }
    private static void usher(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.USHER;var d=LabyrinthData.get(p.server);var woman=actor(p,place,"Woman",LiteraryActor.COFFIN_WOMAN,LiteraryRooms.COFFIN,false);if(woman==null)return;var world=shared(d,place);
        woman.setNoGravity(true);if(world.getBoolean("LidClosed"))woman.appearance(LiteraryActor.COFFIN_WOMAN,2);if(!world.contains("LidClosed")){woman.appearance(LiteraryActor.COFFIN_WOMAN,1);return;}
        if(world.getBoolean("LidClosed")){if(own.getInt("Present")%80==0)sound(p,LiteraryRegistry.SCRATCH.get(),b.offset(LiteraryRooms.COFFIN),.4F+.1F*Math.min(3,own.getInt("Visit")),1);int cracks=Math.min(3,own.getInt("Visit")-1);if(cracks>world.getInt("Cracks")){for(int i=0;i<cracks;i++)for(int y=0;y<4;y++)LiteraryRooms.at(p.serverLevel(),b,-8,-3+y,-27-i*2,LiteraryRegistry.VAULT_CRACK.get());world.putInt("Cracks",cracks);shared(d,place,world);}}
        else if(own.getInt("Visit")>own.getInt("ChoiceVisit")&&own.getBoolean("LidChosen")&&!watched(p.serverLevel(),woman.position().add(0,1,0))&&!watched(p.serverLevel(),b.offset(12,0,-34).getCenter())){woman.moveTo(Vec3.atBottomCenterOf(b.offset(12,0,-34)));woman.appearance(LiteraryActor.COFFIN_WOMAN,0);world.putBoolean("WomanLeft",true);shared(d,place,world);}
        if(own.getBoolean("LidChosen")&&own.getInt("Visit")>own.getInt("ChoiceVisit")){boolean seen=world.getBoolean("LidClosed")?p.distanceToSqr(b.offset(LiteraryRooms.COFFIN).getCenter())<36:world.getBoolean("WomanLeft")&&HouseWatchers.sees(p,woman.getEyePosition());if(seen&&own.getInt("Present")>120)ready(p,place,own,world.getBoolean("LidClosed")?"returned_to_the_closed_lid":"found_her_outside_the_open_coffin");}
    }
    private static void child(ServerPlayer p, BlockPos b, CompoundTag own) {
        var place = LabyrinthPlace.CHILD_ROOM;
        var d = LabyrinthData.get(p.server);
        var world = shared(d, place);
        List<BlockPos> exits = List.of(new BlockPos(-11,0,-11), new BlockPos(11,0,-17),
                new BlockPos(-4,2,-24), new BlockPos(0,2,-24), new BlockPos(4,2,-24));
        int mask = world.getInt("LostExits");
        if (own.getInt("Present") % 60 == 0) {
            for (int i = 0; i < exits.size(); i++) {
                var at = b.offset(exits.get(i));
                if ((mask & (1 << i)) != 0 || !p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(at).expandTowards(0,1,0).inflate(.02),e->e.isAlive()&&!e.isSpectator()).isEmpty() || watched(p.serverLevel(), at.getCenter())
                        || watched(p.serverLevel(), at.above().getCenter())) continue;
                LiteraryRooms.box(p.serverLevel(), at, 0,0,0,0,1,0, Blocks.CALCITE);
                mask |= 1 << i;
                world.putInt("LostExits", mask);
                shared(d, place, world);
                break;
            }
        }
        int examined = own.getInt("ExaminedLostExits");
        for (int i = 0; i < exits.size(); i++) {
            var at = b.offset(exits.get(i));
            if ((mask & (1 << i)) == 0 || !own.getBoolean("Read_Source")
                    || p.distanceToSqr(at.getCenter()) > 100 || !HouseWatchers.sees(p, at.getCenter())) continue;
            String key = "ExitExamineTicks" + i;
            int ticks = Math.min(20, own.getInt(key) + 5);
            own.putInt(key, ticks);
            if (ticks >= 20) examined |= 1 << i;
        }
        own.putInt("ExaminedLostExits", examined);
        if (mask == 31 && examined == 31 && p.getY() < b.getY()-1 && p.getZ() < b.getZ()-21) {
            own.putBoolean("CrawledUnderBed", true);
            ready(p, place, own, own.getInt("ExitsAtArrival") == 0
                    ? "examined_the_lost_exits_and_crawled_below_the_bed"
                    : "examined_the_sealed_thresholds_and_found_the_remaining_crawl");
        }
        var toy = actor(p, place, "CeilingToy", LiteraryActor.FAMILY_CHILD, new BlockPos(7,4,-16), false);
        if (toy != null) { toy.setNoGravity(true); toy.setCustomName(Component.literal("Dinnerbone")); toy.setCustomNameVisible(false); }
    }
    private static void crimson(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.CRIMSON_HALL;var d=LabyrinthData.get(p.server);var world=shared(d,place);int layers=Math.min(5,own.getInt("Visit"));if(layers>world.getInt("SnowVisit")){for(int x=-3;x<=3;x++)for(int z=-27;z<=-21;z++){var at=b.offset(x,0,z);var s=p.serverLevel().getBlockState(at);if(s.isAir()||s.is(Blocks.SNOW))p.serverLevel().setBlock(at,Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,layers),F);}world.putInt("SnowVisit",layers);shared(d,place,world);}
        if(p.serverLevel().getBlockEntity(b.offset(0,0,-30)) instanceof JukeboxBlockEntity box){ItemStack disc=box.getItem(0);int index=disc.is(LiteraryRegistry.CYLINDER_ONE.get())?0:disc.is(LiteraryRegistry.CYLINDER_TWO.get())?1:disc.is(LiteraryRegistry.CYLINDER_THREE.get())?2:-1;
            var custom=disc.get(DataComponents.CUSTOM_DATA);boolean owns=custom!=null&&custom.copyTag().hasUUID(OWNER)&&custom.copyTag().getUUID(OWNER).equals(p.getUUID());if(index>=0&&owns&&box.getSongPlayer().isPlaying()&&box.getSongPlayer().getSong()!=null&&p.distanceToSqr(box.getBlockPos().getCenter())<225){if(own.getInt("Playing")!=index+1){own.putInt("Playing",index+1);own.putInt("Listen",0);}int listen=own.getInt("Listen")+5;own.putInt("Listen",listen);int length=(int)Math.ceil(box.getSongPlayer().getSong().lengthInSeconds()*20);if(listen>=length-5&&box.getSongPlayer().getTicksSinceSongStarted()>=length)own.putInt("HeardCylinders",own.getInt("HeardCylinders")|(1<<index));}else{own.putInt("Playing",0);own.putInt("Listen",0);}}
        if(own.getInt("HeardCylinders")==7&&own.getBoolean("ClearedSnow"))ready(p,place,own,"heard_all_three_cylinders_and_exposed_red_floor");else if(own.getInt("HeardCylinders")!=0)own.putInt("Beat",own.getInt("Visit"));
    }
    private static void bly(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.BLY_ROUTE;var lady=actor(p,place,"Lady",LiteraryActor.LADY,new BlockPos(0,0,-4),false);if(lady==null)return;int time=(int)(p.serverLevel().getGameTime()%900);double z=-4-Math.min(44,time*.06);if(time<740)lady.moveTo(b.getX()+.5,b.getY(),b.getZ()+z+.5,0,0);else lady.moveTo(b.getX()+.5,b.getY()-4,b.getZ()-48+.5,0,0);if(time%80<5)sound(p,LiteraryRegistry.DRIP.get(),lady.blockPosition(),.8F,1);
        boolean closet=Math.abs(p.getX()-b.getX())>4&&p.getZ()<b.getZ()-6&&p.getZ()>b.getZ()-34;boolean closed=false;if(closet){int zz=p.getZ()<b.getZ()-25?-31:p.getZ()<b.getZ()-14?-20:-9;var s=p.serverLevel().getBlockState(b.offset(p.getX()<b.getX()?-4:4,0,zz));closed=s.getBlock() instanceof DoorBlock&&!s.getValue(DoorBlock.OPEN);}
        if(closed&&time<740){own.putInt("RefugeTicks",own.getInt("RefugeTicks")+5);own.putBoolean("WaitedInCloset",true);}if(lady.distanceToSqr(p)<3&&Math.abs(p.getX()-b.getX()-.5)<1.1){p.setDeltaMovement(0,-.3,-.35);p.hurt(p.damageSources().mobAttack(lady),4);own.putBoolean("Dragged",true);}
        if(time>=740&&own.getInt("RefugeTicks")>=200&&!closet&&p.getZ()<b.getZ()-34&&p.getY()>=b.getY())ready(p,place,own,"waited_in_a_shut_closet_until_the_lake");
    }
    private static void lot(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.ELK_LOT;var d=LabyrinthData.get(p.server);var world=shared(d,place);LiteraryElk elk=null;if(world.hasUUID("Elk")&&p.serverLevel().getEntity(world.getUUID("Elk")) instanceof LiteraryElk e)elk=e;else if(!world.hasUUID("Elk")){elk=LiteraryRegistry.ELK.get().create(p.serverLevel());if(elk!=null){elk.moveTo(Vec3.atBottomCenterOf(b.offset(5,0,-23)));if(p.serverLevel().addFreshEntity(elk)){world.putUUID("Elk",elk.getUUID());shared(d,place,world);}}}
        int ticks=own.getInt("ThisVisitTicks");if(elk!=null){if(ticks<240)elk.move(MoverType.SELF,new Vec3(.03,0,-.1));else if(elk.getZ()>b.getZ()-53)elk.move(MoverType.SELF,new Vec3(0,0,-.22));if(HouseWatchers.sees(p,elk.getEyePosition()))own.putBoolean("ElkSeen",true);if(ticks==120){LiteraryRooms.at(p.serverLevel(),b,9,1,-23,Blocks.AIR);sound(p,SoundEvents.IRON_GOLEM_DAMAGE,b.offset(9,1,-23),1,.6F);}}
        for(int i=0;i<3;i++){var man=actor(p,place,"Accuser"+i,LiteraryActor.STRANGER,new BlockPos(i*8,0,-28),false);if(man!=null&&ticks%180<5)man.say(p.getGameProfile().getName()+"! You went out there. Tell us what you did.");}
        if(p.getZ()<b.getZ()-55&&own.getBoolean("ElkSeen")){own.putInt("FieldTicks",own.getInt("FieldTicks")+5);if(own.getInt("FieldTicks")%80==0){sound(p,LiteraryRegistry.HOOVES.get(),p.blockPosition().offset(3,0,-5),.75F,1.2F);for(int side:new int[]{-1,1}){var eye=actor(p,place,"Herd"+side,LiteraryActor.RED_FIGURE,new BlockPos(side*8,0,-75),true);if(eye!=null){eye.appearance(LiteraryActor.RED_FIGURE,3);eye.moveTo(p.getX()+side*7,p.getY(),p.getZ()-5,0,0);}}}
            if(p.getZ()<b.getZ()-103&&own.getInt("FieldTicks")>=180&&own.getInt("FieldLoops")>0){HousePackets.send(p,new HouseFadePayload(20,40,40));p.teleportTo(p.serverLevel(),b.getX()+.5,b.getY(),b.getZ()-6.5,180,0);ready(p,place,own,"escaped_the_looping_field_and_read_own_clipping");}}
        if(p.getZ()<b.getZ()-108&&!own.getBoolean("Ready")){p.teleportTo(p.serverLevel(),p.getX(),p.getY(),b.getZ()-67.5,p.getYRot(),p.getXRot());own.putInt("FieldLoops",own.getInt("FieldLoops")+1);}
    }
    private static void fan(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.ELK_FAN;int visit=own.getInt("Visit");var personalDisplay=new CompoundTag();personalDisplay.putUUID("Reader",p.getUUID());personalDisplay.putBoolean("Scar",personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.ELK_LOT).getBoolean("ElkWounded"));HousePackets.send(p,new LiteraryViewPayload(-1,0,0,personalDisplay));
        if(visit==1&&own.getBoolean("BuiltReach")&&own.getBoolean("Repaired")&&!own.getBoolean("BladeStrike")&&p.getY()>=b.getY()+4.5&&p.getZ()<b.getZ()-13&&p.getZ()>b.getZ()-22){own.putInt("FanTicks",own.getInt("FanTicks")+5);if(HouseWatchers.sees(p,b.offset(0,0,-17).getCenter()))own.putBoolean("ElkThroughFan",true);if(own.getInt("FanTicks")>=70){p.setDeltaMovement(.85,.15,-.4);p.hurt(p.damageSources().generic(),3);own.putBoolean("BladeStrike",true);own.putInt("Beat",1);}}
        if(visit==2&&own.getBoolean("ElkThroughFan")&&own.getBoolean("BladeStrike")){for(int x=-3;x<=3;x++)for(int z=-20;z<=-14;z++)if(Math.abs(x)==3||z==-20||z==-14)LiteraryRooms.at(p.serverLevel(),b,x,-1,z,LiteraryRegistry.TAPE.get());if(HouseWatchers.sees(p,b.offset(0,0,-17).getCenter())){own.putBoolean("TapeSeen",true);own.putInt("Beat",2);}}
        if(visit>=3&&own.getBoolean("TapeSeen")&&own.getBoolean("Repaired")&&own.getBoolean("BladeStrike")){for(int x=-3;x<=3;x++)for(int z=-20;z<=-14;z++)if(Math.abs(x)==3||z==-20||z==-14)LiteraryRooms.at(p.serverLevel(),b,x,-1,z,LiteraryRegistry.PEELED_TAPE.get());if(p.distanceToSqr(b.offset(0,0,-17).getCenter())<25)ready(p,place,own,"returned_to_tape_then_the_peeled_outline");}
    }
    private static void mapping(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.MAPPING_INTERIOR;var silhouette=actor(p,place,"Father",LiteraryActor.SILHOUETTE,new BlockPos(-9,0,-26),true);var brother=actor(p,place,"Brother",LiteraryActor.BROTHER,new BlockPos(6,0,-32),true);int visit=own.getInt("Visit");if(silhouette!=null){silhouette.appearance(LiteraryActor.SILHOUETTE,Math.min(3,visit-1));
            int step=(own.getInt("ThisVisitTicks")/100)%4;int[][] stops={{-9,-26},{-7,-26},{-7,-28},{-9,-28}};
            Vec3 destination=b.offset(stops[step][0],0,stops[step][1]).getCenter().add(0,-.5,0);Vec3 delta=destination.subtract(silhouette.position()).multiply(1,0,1);
            if(delta.lengthSqr()>.04){silhouette.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));silhouette.move(MoverType.SELF,delta.normalize().scale(.12));}
            else silhouette.setYRot((float)Math.toDegrees(Math.atan2(silhouette.getX()-p.getX(),p.getZ()-silhouette.getZ())));
            silhouette.setYHeadRot(silhouette.getYRot());silhouette.yBodyRot=silhouette.getYRot();if(HouseWatchers.sees(p,silhouette.getEyePosition()))own.putBoolean("SilhouetteSeen",true);}
        if(brother!=null){brother.appearance(LiteraryActor.BROTHER,own.getBoolean("PileBroken")?0:Math.min(3,visit));if(own.getBoolean("PileChosen")&&visit>own.getInt("ChoiceVisit")&&p.distanceToSqr(brother)<25&&HouseWatchers.sees(p,brother.getEyePosition()))ready(p,place,own,own.getBoolean("PileBroken")?"broke_the_fathers_things_and_returned_to_the_brother":"kept_the_fathers_things_and_saw_the_brother_fade");}
        if(own.getBoolean("SilhouetteSeen")&&p.getY()<b.getY()-2&&p.getZ()<b.getZ()-39){own.putBoolean("BeyondRoof",true);own.putInt("Beat",visit);}
    }
    private static void rabbit(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.HOLY_RABBIT;
        if (!rabbitCommitted(own) || own.getBoolean("Ready")) {
            actor(p, place, "Father", LiteraryActor.FATHER, new BlockPos(3, 0, -22), true);
            return;
        }
        p.stopRiding();p.stopFallFlying();boolean sheltered=Math.abs(p.getX()-b.getX())<3.5&&p.getZ()<b.getZ()-20&&p.getZ()>b.getZ()-29;if(!sheltered)p.setTicksFrozen(Math.min(150,p.getTicksFrozen()+15));int night=own.getInt("Night");if(night==0){night=1;own.putInt("Night",1);own.putInt("NightTicks",0);}int ticks=own.getInt("NightTicks")+5;own.putInt("NightTicks",ticks);int drowse=own.getInt("Drowse")+5;own.putInt("Drowse",drowse);HousePackets.send(p,new LiteraryViewPayload(-1,20,Math.min(90,drowse/4),new CompoundTag()));
        var father=actor(p,place,"Father",LiteraryActor.FATHER,new BlockPos(3,0,-22),true);if(father!=null){father.appearance(LiteraryActor.FATHER,night>=2?2:0);if(drowse>=320&&father.distanceToSqr(p)<36&&p.hurt(p.damageSources().mobAttack(father),1)){own.putInt("Drowse",0);father.say("Stay with me.");}if(ticks>100&&ticks<200){father.moveTo(b.getX()+6.5,b.getY(),b.getZ()-29.5,0,0);}else if(ticks>=200)father.moveTo(b.getX()+3.5,b.getY(),b.getZ()-22.5,0,0);}
        var d=LabyrinthData.get(p.server);var world=shared(d,place);String rabbitKey="Rabbit_"+p.getUUID();Rabbit rabbit=null;if(world.hasUUID(rabbitKey)&&p.serverLevel().getEntity(world.getUUID(rabbitKey)) instanceof Rabbit r)rabbit=r;else if(!world.hasUUID(rabbitKey)){rabbit=EntityType.RABBIT.create(p.serverLevel());if(rabbit!=null){rabbit.setVariant(Rabbit.Variant.WHITE);rabbit.setNoAi(true);rabbit.setInvulnerable(true);rabbit.setPersistenceRequired();rabbit.addTag("HolyRabbit");rabbit.moveTo(Vec3.atBottomCenterOf(b.offset(5,0,-23)));if(p.serverLevel().addFreshEntity(rabbit)){world.putUUID(rabbitKey,rabbit.getUUID());shared(d,place,world);}}}
        if(rabbit!=null){double a=p.serverLevel().getGameTime()*.012;rabbit.moveTo(b.getX()+.5+Math.cos(a)*7,b.getY(),b.getZ()-25.5+Math.sin(a)*6,0,0);if(rabbit.hasCustomName())own.putString("RabbitName",rabbit.getCustomName().getString());if(ticks<100&&HouseWatchers.sees(p,rabbit.getEyePosition()))own.putInt("RabbitMornings",own.getInt("RabbitMornings")|(1<<Math.min(7,night)));}
        if(ticks>=200&&own.getInt("MealNight")<night&&p.distanceToSqr(b.offset(0,0,-24).getCenter())<100){var meal=mark(p,place,"Meal"+night,new ItemStack(LiteraryRegistry.MEAL.get()));UUID token=UUID.randomUUID();CustomData.update(DataComponents.CUSTOM_DATA,meal,t->{t.putInt("Night",own.getInt("Night"));t.putUUID("MealToken",token);});own.putUUID("MealToken",token);own.putInt("MealNight",night);give(p,meal);if(father!=null)father.say(night==1?"I caught something. Eat.":"Here. Eat while you can.");if(night==1)LiteraryRooms.prop(p.serverLevel(),b,new BlockPos(6,0,-20),LiteraryPropBlock.Kind.ELK_HIDE,Direction.NORTH);}
        if(ticks>=500){own.putInt("Night",Math.min(8,night+1));own.putInt("NightTicks",0);if(night>=2&&!watched(p.serverLevel(),b.offset(0,3,-25).getCenter())){own.putBoolean("CarvingAppeared",true);if(p.serverLevel().getBlockEntity(b.offset(0,3,-25)) instanceof LiteraryModelBlockEntity model){var t=new CompoundTag();t.putString("Text",p.getGameProfile().getName());model.display(t);}}}
        if(own.getBoolean("CarvingAppeared")&&HouseWatchers.sees(p,b.offset(0,3,-25).getCenter()))own.putBoolean("CarvingRead",true);
        if(night>=3&&own.getInt("Meals")>=2){own.putBoolean("TrailOpen",true);if(father!=null&&!watched(p.serverLevel(),father.getEyePosition())){father.setInvisible(true);own.putBoolean("FatherGone",true);}}
        if(p.getZ()<b.getZ()-33){if(!own.getBoolean("TrailOpen")){p.setTicksFrozen(200);p.hurt(p.damageSources().freeze(),2);}else{own.putInt("TrailTicks",own.getInt("TrailTicks")+5);if(own.getInt("TrailTicks")>600){p.hurt(p.damageSources().freeze(),2);}if(p.getZ()<b.getZ()-118&&p.getFoodData().getFoodLevel()>6&&own.getBoolean("FatherGone")&&own.getBoolean("CarvingRead")&&Integer.bitCount(own.getInt("RabbitMornings"))>=2){own.putBoolean("Committed",false);ready(p,place,own,"ate_and_followed_the_fathers_drag_trail");}}}
        if(own.getInt("Present")%100==0)sound(p,LiteraryRegistry.BLIZZARD.get(),p.blockPosition(),.8F,1);
    }
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
        if(place==LabyrinthPlace.COSTUME_NIGHT&&own.getBoolean("WitchSeen")&&p.serverLevel().getBlockState(p.blockPosition().below()).is(Blocks.GRASS_BLOCK)&&p.getZ()<b.getZ()-54)ready(p,place,own,"reached_living_grass_beyond_the_costumes");
        var witch=huntBody(p,place,new BlockPos(14,0,-41));if(witch==null)return;boolean water=p.isUnderWater()&&p.getY()+p.getBbHeight()<b.getY();boolean grass=p.serverLevel().getBlockState(p.blockPosition().below()).is(Blocks.GRASS_BLOCK);
        if(place==LabyrinthPlace.COSTUME_NIGHT){if(HouseWatchers.sees(p,witch.getEyePosition()))own.putBoolean("WitchSeen",true);if(own.getBoolean("WitchSeen")&&grass&&p.getZ()<b.getZ()-54)ready(p,place,own,"reached_living_grass_beyond_the_costumes");}
        if(place==LabyrinthPlace.WINTER_LAKE){if(water){own.putInt("SubmergedTicks",own.getInt("SubmergedTicks")+5);}if(own.getBoolean("BrokeIce")&&own.getInt("SubmergedTicks")>=100&&p.getZ()<b.getZ()-76&&p.getY()>=b.getY())ready(p,place,own,"broke_ice_waited_beneath_it_and_reached_the_bank");}
        if(place==LabyrinthPlace.MOVIE_NIGHT){ensureMovieCanoe(p,b);if(p.getVehicle() instanceof LakeCanoeEntity canoe&&canoe.getTags().contains("LiteraryMovieCanoe")){own.putBoolean("Rowed",true);if(own.contains("LastRowX")){double dx=p.getX()-own.getDouble("LastRowX"),dz=p.getZ()-own.getDouble("LastRowZ");double dist=Math.sqrt(dx*dx+dz*dz);if(dist<3)own.putDouble("RowDistance",own.getDouble("RowDistance")+dist);}own.putDouble("LastRowX",p.getX());own.putDouble("LastRowZ",p.getZ());if(own.getInt("Present")%140==0){var rocket=new net.minecraft.world.entity.projectile.FireworkRocketEntity(p.serverLevel(),p.getX()+9,p.getY()+4,p.getZ()-6,new ItemStack(Items.FIREWORK_ROCKET));p.serverLevel().addFreshEntity(rocket);sound(p,SoundEvents.FIREWORK_ROCKET_LARGE_BLAST,p.blockPosition(),1,1);p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.FLASH,witch.getX(),b.getY()+2,witch.getZ(),1,0,0,0,0);own.putInt("Flashes",own.getInt("Flashes")+1);Vec3 toward=p.position().subtract(witch.position()).multiply(1,0,1).normalize();witch.move(MoverType.SELF,toward.scale(6));}}
            if(own.getBoolean("Rowed")&&own.getDouble("RowDistance")>=45&&own.getInt("Flashes")>=2&&p.getZ()<b.getZ()-79){if(p.getVehicle()!=null)p.stopRiding();ready(p,place,own,"rowed_to_the_far_pier_between_firework_exposures");}}

    }
    private static final Map<UUID,LakeCanoeEntity> MOVIE_CANOES=new HashMap<>();
    private static void ensureMovieCanoe(ServerPlayer p, BlockPos b) {
        var place = LabyrinthPlace.MOVIE_NIGHT;
        var d = LabyrinthData.get(p.server);
        var world = shared(d, place);
        var level = p.serverLevel();
        LakeCanoeEntity canoe = world.hasUUID("Canoe") && level.getEntity(world.getUUID("Canoe")) instanceof LakeCanoeEntity c ? c : null;
        if(canoe==null&&world.hasUUID("Canoe")){
            var queued=MOVIE_CANOES.get(world.getUUID("Canoe"));
            if(queued!=null&&!queued.isRemoved()&&queued.level()==level)canoe=queued;
        }
        if (canoe == null && world.hasUUID("Canoe") && !world.getBoolean("CanoeRemoved")) {
            // Absence in an unloaded entity chunk is not evidence of a lost canoe.
            var bounds = IndianLakeRooms.bounds(b, place);
            for (int x = ((int) bounds.minX) >> 4; x <= ((int) bounds.maxX) >> 4; x++) {
                for (int z = ((int) bounds.minZ) >> 4; z <= ((int) bounds.maxZ) >> 4; z++) {
                    level.getChunk(x, z);
                    if (!level.areEntitiesLoaded(ChunkPos.asLong(x, z))) return;
                }
            }
            if (world.contains("CanoePosition")) {
                var at = BlockPos.of(world.getLong("CanoePosition"));
                level.getChunk(at);
                if (!level.areEntitiesLoaded(new ChunkPos(at).toLong())) return;
            }
            if (level.getEntity(world.getUUID("Canoe")) instanceof LakeCanoeEntity found) canoe = found;
        }
        if (canoe == null) {
            canoe = DrownedTownRegistry.CAVE_CANOE.get().create(level);
            if (canoe == null) return;
            canoe.addTag("LiteraryMovieCanoe");
            canoe.mobile(true);
            canoe.moveTo(b.getX()+.5, b.getY()-.4, b.getZ()-15.5, 180, 0);
            if (!level.addFreshEntity(canoe)) return;
            MOVIE_CANOES.put(canoe.getUUID(),canoe);
            world.putUUID("Canoe", canoe.getUUID());
            world.remove("CanoeRemoved");
            world.remove("CanoeClaim");
        }
        ServerPlayer claimant = world.hasUUID("CanoeClaim") ? p.server.getPlayerList().getPlayer(world.getUUID("CanoeClaim")) : null;
        boolean active = claimant != null && inside(claimant, place) && !personal(d, claimant.getUUID(), place).getBoolean("Ready");
        if (canoe.getPassengers().isEmpty() && !active) {
            var start = new Vec3(b.getX()+.5, b.getY()-.4, b.getZ()-15.5);
            if (canoe.position().distanceToSqr(start) > 16) {
                canoe.moveTo(start.x, start.y, start.z, 180, 0);
                canoe.setDeltaMovement(Vec3.ZERO);
            }
            world.remove("CanoeClaim");
        }
        world.putLong("CanoePosition", canoe.blockPosition().asLong());
        shared(d, place, world);
    }

    @SubscribeEvent public static void movieCanoeRemoved(EntityLeaveLevelEvent event) {
        var entity = event.getEntity();
        MOVIE_CANOES.remove(entity.getUUID());
        if (!(event.getLevel() instanceof ServerLevel level) || !entity.getTags().contains("LiteraryMovieCanoe")) return;
        var d = LabyrinthData.get(level.getServer());
        var world = shared(d, LabyrinthPlace.MOVIE_NIGHT);
        if (!world.hasUUID("Canoe") || !world.getUUID("Canoe").equals(entity.getUUID())) return;
        world.putLong("CanoePosition", entity.blockPosition().asLong());
        var reason = entity.getRemovalReason();
        if (reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED) world.putBoolean("CanoeRemoved", true);
        shared(d, LabyrinthPlace.MOVIE_NIGHT, world);
    }

    public static boolean boardMovie(ServerPlayer p, LakeCanoeEntity canoe) {
        if (!inside(p, LabyrinthPlace.MOVIE_NIGHT) || !canoe.getTags().contains("LiteraryMovieCanoe")
                || !p.getMainHandItem().isEmpty() || p.distanceToSqr(canoe) >= 25 || !canoe.getPassengers().isEmpty()) return false;
        var d = LabyrinthData.get(p.server);
        var world = shared(d, LabyrinthPlace.MOVIE_NIGHT);
        if (!world.hasUUID("Canoe") || !world.getUUID("Canoe").equals(canoe.getUUID()) || !p.startRiding(canoe, true)) return false;
        world.putUUID("CanoeClaim", p.getUUID());
        shared(d, LabyrinthPlace.MOVIE_NIGHT, world);
        return true;
    }
    public static BlockPos diaryPosition(int i){return new BlockPos(new int[]{-9,7,-6,10,-11,5,12,-3}[i],0,new int[]{-12,-23,-30,-9,-36,-34,-29,-37}[i]);}
    private static int diaryIndex(BlockPos rel){for(int i=0;i<8;i++)if(rel.equals(diaryPosition(i)))return i;return -1;}
    private static void diary(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.DEVILS_ROCK;int target=Math.min(8,own.getInt("Visit")*3);if(own.getInt("ThisVisitTicks")>=100){for(int i=0;i<target;i++){if((own.getInt("DiaryPlaced")&(1<<i))!=0)continue;var at=b.offset(diaryPosition(i));if(watched(p.serverLevel(),at.getCenter())||watched(p.serverLevel(),at.above().getCenter()))continue;LiteraryRooms.prop(p.serverLevel(),b,diaryPosition(i),LiteraryPropBlock.Kind.PAGE,Direction.SOUTH);own.putInt("DiaryPlaced",own.getInt("DiaryPlaced")|(1<<i));own.putLong("ArrivalTime"+i,p.serverLevel().getGameTime());var plate=b.offset(i%2==0?-5:5,0,-14);var ps=p.serverLevel().getBlockState(plate);if(ps.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED)){p.serverLevel().setBlock(plate,ps.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED,true),3);p.serverLevel().scheduleTick(plate,ps.getBlock(),20);}sound(p,SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,plate,.35F,1);p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.SCULK_CHARGE_POP,at.getX()+.5,at.getY()+.1,at.getZ()+.5,3,.1,.1,.1,0);break;}}
        if(own.getInt("ThisVisitTicks")>=900&&Integer.bitCount(own.getInt("DiaryPlaced"))<target){int i=0;while(i<target&&(own.getInt("DiaryPlaced")&(1<<i))!=0)i++;if(i<target){own.putInt("DiaryPlaced",own.getInt("DiaryPlaced")|(1<<i));reward(p,place,own,"Diary"+i,LiteraryTexts.diary(i,p.getGameProfile().getName()));}}
        int expected=(1<<target)-1;if((own.getInt("DiaryRead")&expected)==expected)own.putInt("Beat",own.getInt("Visit"));
    }
    private static boolean wheelDoor(ServerPlayer p,CompoundTag own,BlockPos rel){if(rel.equals(new BlockPos(0,0,-7))&&!own.getBoolean("LeftMother")){var b=base(p,LabyrinthPlace.WHEEL);p.teleportTo(p.serverLevel(),b.getX()-13.5,b.getY(),b.getZ()-8.5,180,0);own.putBoolean("FrontLoop",true);return true;}if(Math.abs(rel.getX())!=14||!(rel.getZ()==-17||rel.getZ()==-34||rel.getZ()==-51))return false;int door=(-rel.getZ()/17-1)*2+(rel.getX()>0?1:0);int seen=own.getInt("WheelDoors");int target=switch(door){case 0->1;case 1->0;case 2->4;case 3->1;case 4->5;default->3;};seen|=1<<door;own.putInt("WheelDoors",seen);own.putBoolean("Memory",door==0||door==2||door==4);var b=base(p,LabyrinthPlace.WHEEL);int x=target%2==0?-14:14,z=-8-(target/2)*17;if(door==4&&Integer.bitCount(seen)>=4){own.putBoolean("ReachedMother",true);p.teleportTo(p.serverLevel(),b.getX()+14.5,b.getY(),b.getZ()-58.5,0,0);}else{p.teleportTo(p.serverLevel(),b.getX()+x+.5,b.getY(),b.getZ()+z+.5,180,0);if((seen&2)!=0)LiteraryRooms.at(p.serverLevel(),b,-10,0,-9,Blocks.FLOWER_POT);}return true;}
    private static void set(ServerPlayer p,BlockPos b,CompoundTag own){var place=LabyrinthPlace.GHOSTS_SET;boolean mark=Math.abs(p.getX()-b.getX())<=6&&p.getZ()<b.getZ()-19&&p.getZ()>b.getZ()-21&&p.serverLevel().getBlockState(p.blockPosition()).is(LiteraryRegistry.PROP.get());
        var mother=actor(p,place,"Mother",LiteraryActor.FAMILY_MOTHER,new BlockPos(-6,0,-31),false);var father=actor(p,place,"Father",LiteraryActor.FAMILY_FATHER,new BlockPos(3,0,-34),false);var child=actor(p,place,"Child",LiteraryActor.FAMILY_CHILD,new BlockPos(5,0,-33),false);
        if(mark){own.putInt("MarkTicks",own.getInt("MarkTicks")+5);if(own.getInt("MarkTicks")%40==5){camera(p,place,own,40);if(mother!=null)mother.say("We have everything we need. Say it again, for the camera.");if(father!=null)father.say("This is a good home. We are a family.");}for(int x:new int[]{-17,17})LiteraryRooms.at(p.serverLevel(),b,x,3,-25,Blocks.SEA_LANTERN);}
        else if(own.getInt("MarkTicks")>=120){own.putInt("OffMarkTicks",own.getInt("OffMarkTicks")+5);HousePackets.send(p,new LiteraryViewPayload(-1,0,0,new CompoundTag()));if(mother!=null)mother.say("The light is off. Please stop saying that.");if(child!=null){child.appearance(LiteraryActor.FAMILY_CHILD,3);if(!own.getBoolean("BehindPlaced")&&!watched(p.serverLevel(),child.getEyePosition())){child.moveTo(p.getX()-p.getLookAngle().x*3,p.getY(),p.getZ()-p.getLookAngle().z*3,0,0);own.putBoolean("BehindPlaced",true);}if(own.getBoolean("BehindPlaced")&&HouseWatchers.sees(p,child.getEyePosition()))own.putBoolean("BehindSeen",true);}if(own.getInt("OffMarkTicks")>=80&&own.contains("Confession")&&own.getBoolean("BehindSeen"))ready(p,place,own,"left_the_mark_and_read_own_confession_transcript");}
    }
    private static void camera(ServerPlayer p,LabyrinthPlace place,CompoundTag own,int ticks){var b=base(p,place);var camera=actor(p,place,"Camera",LiteraryActor.CAMERA,LiteraryRooms.CAMERA,true);if(camera==null)return;camera.setInvisible(true);camera.setNoGravity(true);camera.moveTo(b.getX()+.5,b.getY()+1.5,b.getZ()-28.5,180,8);HousePackets.send(p,new LiteraryViewPayload(camera.getId(),Math.min(60,ticks),0,new CompoundTag()));}
}
