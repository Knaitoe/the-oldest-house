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
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * One hotel account (0.4.105). The house is the guest's for the closing night: dinner at the remaining table, the bar, the
 * ballroom floor and Room 217, everything free and everything on the account. Each fresh stay is another night; the snow
 * stands higher at that guest's own windows each night and the hose is further from its rack. On the third night the
 * account is due, and the guest closes it one of three ways, each its own ending: settles it at the desk and reads the
 * settled account, signs the register's second line and stays (being in the photograph first), or goes out by the staff
 * door with the master key before it is presented, leaving the hotel what it holds of theirs. Custody and knowledge are
 * always personal.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HotelVignette {
    public static final String STATE="hotel_0435",OWNER="HotelGuest",PAPER="HotelPaper";
    public static final String WORLD_STATE="hotel_world_0437";
    public static final int DEATH_HISTORY=16,PET_HISTORY=52,GRAVE_LIMIT=52;
    public static final int REST_TICKS=80;public static final long BOILER_LIMIT=72000;
    /** The night the account is presented. */
    public static final int NIGHTS=3;
    /** The door a guest comes through from the numbered corridor's 217 into the hotel's own: not a registered transition. */
    public static final String ROOM_217="hotel/217";
    public static final String SETTLED="read_the_settled_account",SIGNED="signed_the_second_place",WALKED="walked_out_before_closing";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private HotelVignette(){}
    public static CompoundTag personal(LabyrinthData d,UUID id){
        var own=d.stateEntry(STATE,id.toString());
        boolean changed=trimHistory(own,"Deaths",DEATH_HISTORY)|trimHistory(own,"LostPets",PET_HISTORY);
        if(changed)d.setStateEntry(STATE,id.toString(),own);return own;
    }
    private static boolean trimHistory(CompoundTag own,String key,int limit){
        var list=own.getList(key,Tag.TAG_COMPOUND);int before=list.size();
        if(before<=limit)return false;
        own.putLong(key+"Total",Math.max(own.getLong(key+"Total"),before));
        while(list.size()>limit)list.remove(0);own.put(key,list);return true;
    }
    public static void save(LabyrinthData d,UUID id,CompoundTag t){d.setStateEntry(STATE,id.toString(),t);}
    /** Move shared scenery away from every guest's exact saved belongings, once per world. */
    public static CompoundTag world(LabyrinthData d){
        var world=d.state(WORLD_STATE);if(world.getBoolean("Migrated"))return world;
        var legacy=d.state(STATE);
        for(var key:new ArrayList<>(legacy.getAllKeys())){
            try{UUID.fromString(key);continue;}catch(IllegalArgumentException ignored){}
            world.put(key,legacy.get(key).copy());legacy.remove(key);
        }
        if(world.getInt("Graves")>=GRAVE_LIMIT){
            world.putInt("Graves",GRAVE_LIMIT);
            for(var key:new ArrayList<>(world.getAllKeys()))if(key.startsWith("Grave_"))world.remove(key);
        }
        world.putBoolean("Migrated",true);d.setState(STATE,legacy);saveWorld(d,world);return world;
    }
    public static void saveWorld(LabyrinthData d,CompoundTag world){d.setState(WORLD_STATE,world);}
    private static boolean participant(ServerPlayer p){return p.isAlive()&&p.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR&&!FinaleProgress.terminal(FinaleProgress.phase(p.server,p.getUUID()));}
    public static boolean inside(ServerPlayer p,LabyrinthPlace place){var b=IndianLakeRooms.base(p.server,place);return participant(p)&&b!=null&&p.level().dimension().equals(NovelRooms.dimension(place))&&IndianLakeRooms.bounds(b,place).contains(p.position());}
    public static @Nullable LabyrinthPlace current(ServerPlayer p){for(var place:List.of(LabyrinthPlace.HOTEL,LabyrinthPlace.HOTEL_GROUNDS,LabyrinthPlace.HOTEL_HALLWAY))if(inside(p,place))return place;return null;}

    // ------------------------------------------------------------------------------------------------ nights and the account
    /** How high the snow stands at this guest's windows: at the sill the first night, nearly to the top by the fourth. */
    public static int snow(CompoundTag own){return Math.max(0,Math.min(3,own.getInt("Visits")-1));}
    /** The last night of the season has come for this guest, and the account is open. */
    public static boolean due(CompoundTag own){return own.getInt("Visits")>=NIGHTS&&!own.getBoolean("ClosedAccount");}
    public static boolean closed(CompoundTag own){return own.getBoolean("ClosedAccount");}
    public static String ending(CompoundTag own){return own.getString("Ending");}
    /**
     * A guest steps into the hotel, its grounds or the corridor beyond it. Coming in from outside starts a stay; the first time
     * a stay reaches the hotel itself it is another night. Walking between the three is the same night.
     */
    public static void onArrive(ServerPlayer p,LabyrinthPlace place){if(place!=LabyrinthPlace.HOTEL&&place!=LabyrinthPlace.HOTEL_GROUNDS&&place!=LabyrinthPlace.HOTEL_HALLWAY)return;if(!participant(p))return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());if(own.getString("Here").equals(place.id()))return;
        if(own.getString("Here").isEmpty())own.putBoolean("Stay",true);
        own.putString("Here",place.id());own.remove("Away");own.remove("RestTicks");
        if(place==LabyrinthPlace.HOTEL&&own.getBoolean("Stay")){own.remove("Stay");int night=own.getInt("Visits")+1;own.putInt("Visits",night);p.displayClientMessage(Component.literal(night(own,night)),false);}
        save(d,p.getUUID(),own);
        if(place==LabyrinthPlace.HOTEL){var b=IndianLakeRooms.base(p.server,place);if(b!=null)views(p,b,own);}}
    private static String night(CompoundTag own,int night){
        if(closed(own))return switch(ending(own)){case "signed"->"Good evening. The keys are where you left them.";case "walked"->"Closing night. The desk keeps what it holds of yours.";default->"Closing night. Your account is settled; the house is still yours.";};
        if(night>=NIGHTS)return "The last night of the season. Your account is due at the desk.";
        return night==1?"Closing night. Everything is complimentary; it goes on your account. Dinner is at the remaining table.":"Closing night, again. The snow is higher against the glass.";
    }
    public static void reverse(ServerPlayer p,int shift,int remaining){if(!participant(p)||shift<=0)return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());own.putBoolean("AgainstNumbers",true);own.putBoolean("HallExit",remaining==0);save(d,p.getUUID(),own);}
    /**
     * The numbered corridor beyond the hotel's own: its displayed 217 opens into Room 217 (see {@link #destinationEntry}), and a
     * guest who has never stayed and walks back against the numbers to its door finds it lets out into the hotel's upstairs
     * corridor, as if they had come that way. Every other door is the corridor's as built.
     */
    public static @Nullable LabyrinthData.Door route(ServerPlayer p,LabyrinthData.Door door){if(!inside(p,LabyrinthPlace.HOTEL_HALLWAY))return null;
        var base=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL_HALLWAY);
        if(door.dimension.equals(p.level().dimension())&&door.id.startsWith("hotel_hallway/room_")&&LabyrinthData.LOCKED.equals(door.destination)&&LabyrinthLoops.roomNumberAt(p,base,door.lower)==217)
            return new LabyrinthData.Door(door.id,door.dimension,door.lower,door.facing,"place:hotel",door.command);
        if(!door.id.equals(LabyrinthPlace.HOTEL_HALLWAY.entryDoorId()))return null;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());
        var hotel=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);
        if(!own.getBoolean("HallExit")||own.getInt("Visits")>0||hotel==null||!LabyrinthBuilder.isPlaceReady(p.server,LabyrinthPlace.HOTEL))return null;
        var upstairs=new LabyrinthData.Waypoint(NovelRooms.dimension(LabyrinthPlace.HOTEL),Vec3.atBottomCenterOf(hotel.offset(HotelRooms.NUMBERS)),Direction.SOUTH.toYRot(),true);
        if(!upstairs.equals(d.peekReturn(p.getUUID())))d.pushReturn(p.getUUID(),upstairs);
        return null;}
    /**
     * Where an arrival lands. From the numbered corridor's 217 a guest steps into the hotel's own upstairs corridor in front of
     * 217's door, just as they stood in front of the other: the two corridors are built alike, so nothing is copied over this
     * one. Going out by the staff door is noticed here, before the guest is anywhere else.
     */
    public static LabyrinthData.Door destinationEntry(ServerPlayer p,@Nullable LabyrinthData.Door from,LabyrinthData.Door entry,LabyrinthPlace place){
        if(from!=null&&from.id.equals(LabyrinthPlace.HOTEL.id()+"/service"))walkedOut(p);
        if(place!=LabyrinthPlace.HOTEL||from==null||!from.id.startsWith(LabyrinthPlace.HOTEL_HALLWAY.id()+"/room_"))return entry;
        var b=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);if(b==null)return entry;
        return new LabyrinthData.Door(ROOM_217,NovelRooms.dimension(LabyrinthPlace.HOTEL),b.offset(HotelRooms.ROOM_DOOR),Direction.EAST,LabyrinthData.RETURN,false);
    }
    public static boolean withinRoom(@Nullable LabyrinthData.Door door){return door!=null&&ROOM_217.equals(door.id);}
    private static void give(ServerPlayer p,ItemStack s){if(p.getInventory().add(s))return;var drop=new ItemEntity(p.serverLevel(),p.getX(),p.getY()+.3,p.getZ(),s);drop.setTarget(p.getUUID());p.serverLevel().addFreshEntity(drop);}
    private static ItemStack marked(ServerPlayer p,ItemStack s,String key){CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putUUID(OWNER,p.getUUID());t.putString(PAPER,key);});return VignetteYields.mark(s,"hotel");}
    private static ItemStack snapshot(ServerPlayer p,CompoundTag own,String key,ItemStack s){String slot="Original_"+key;if(!own.contains(slot))own.put(slot,marked(p,s,key).save(p.registryAccess()));return ItemStack.parseOptional(p.registryAccess(),own.getCompound(slot));}
    private static void reward(ServerPlayer p,CompoundTag own,String key,ItemStack s){if(own.getBoolean("Given_"+key))return;own.putBoolean("Given_"+key,true);give(p,snapshot(p,own,key,s));}
    /** A fixed paper: the original is kept as first read. */
    private static void openPaper(ServerPlayer p,String key,ItemStack book){var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());var original=snapshot(p,own,key,book);save(d,p.getUUID(),own);p.openMenu(new SimpleMenuProvider((id,inv,who)->new Pages(id,p,original,key,false,true,true),original.getHoverName()));}
    /** A paper that changes with the stay: the account, the register. Read as it stands now. */
    private static void openLive(ServerPlayer p,String key,ItemStack book,boolean ending){var page=marked(p,book,key);p.openMenu(new SimpleMenuProvider((id,inv,who)->new Pages(id,p,page,key,ending,true,true),page.getHoverName()));}
    private static void openCemetery(ServerPlayer p,CompoundTag own,ItemStack current){
        var d=LabyrinthData.get(p.server);snapshot(p,own,"Cemetery",current);save(d,p.getUUID(),own);
        p.openMenu(new SimpleMenuProvider((id,inv,who)->new Pages(id,p,marked(p,current,"Cemetery"),"Cemetery",false,true,true),current.getHoverName()));
    }
    public static void readCollected(ServerPlayer p,ItemStack original){var data=original.get(DataComponents.CUSTOM_DATA);if(data==null||!original.has(DataComponents.WRITTEN_BOOK_CONTENT))return;var tag=data.copyTag();boolean owned=tag.hasUUID(OWNER)&&tag.getUUID(OWNER).equals(p.getUUID());String key=tag.getString(PAPER);p.openMenu(new SimpleMenuProvider((id,inv,who)->new Pages(id,p,original.copy(),key,false,owned,false),original.getHoverName()));}
    public static final class Pages extends LecternMenu {
        private final ServerPlayer reader;private final ItemStack original;private final String key;private final boolean ending,owned,surface;
        Pages(int id,ServerPlayer p,ItemStack s,String key,boolean ending,boolean owned,boolean surface){super(id,bookContainer(s),new SimpleContainerData(1));reader=p;original=s.copy();this.key=key;this.ending=ending;this.owned=owned;this.surface=surface;}
        private static Container bookContainer(ItemStack s){var c=new SimpleContainer(1);c.setItem(0,s.copy());return c;}
        public ItemStack book(){return original.copy();}
        @Override public boolean clickMenuButton(Player player,int button){if(player!=reader||!participant(reader)||(surface&&current(reader)==null))return false;var d=LabyrinthData.get(reader.server);var own=personal(d,reader.getUUID());
            if(button==3){if(!owned||!surface||own.getBoolean("Taken_"+key))return false;own.putBoolean("Taken_"+key,true);save(d,reader.getUUID(),own);give(reader,key.equals("Cemetery")?ItemStack.parseOptional(reader.registryAccess(),own.getCompound("Original_Cemetery")):original.copy());return true;}
            if(!super.clickMenuButton(player,button))return false;
            if(owned&&getPage()==original.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()-1){own.putBoolean("Read_"+key,true);save(d,reader.getUUID(),own);
                // Only the endings readied for this guest, read through here in the hotel, close the account.
                if(ending&&inside(reader,LabyrinthPlace.HOTEL)&&!closed(own)){
                    if(key.equals("Account")&&own.getBoolean("Settled"))close(reader,"settled",SETTLED);
                    else if(key.equals("Register")&&due(own)&&own.getBoolean("Photographed"))close(reader,"signed",SIGNED);}}
            return true;
        }
    }
    /** The account closes one way and stays closed. Each way is the same story's ending in the reader's Witness account. */
    private static void close(ServerPlayer p,String ending,String outcome){var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());if(closed(own))return;
        own.putBoolean("ClosedAccount",true);own.putString("Ending",ending);if(ending.equals("signed"))own.putBoolean("Signed",true);save(d,p.getUUID(),own);
        WitnessAccount.resolve(p,WitnessAccount.Story.HOTEL,outcome);d.setCompleted("hotel",true);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void held(PlayerInteractEvent.RightClickItem e){if(!(e.getEntity() instanceof ServerPlayer p))return;var s=p.getItemInHand(e.getHand());if(s.has(DataComponents.WRITTEN_BOOK_CONTENT)&&s.has(DataComponents.CUSTOM_DATA)&&s.get(DataComponents.CUSTOM_DATA).copyTag().contains(PAPER)){readCollected(p,s);e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}}
    public static final class Property extends SimpleContainer {
        final ServerPlayer owner;final String key;private boolean loading=true;
        Property(ServerPlayer p,String key){super(54);owner=p;this.key=key;var t=personal(LabyrinthData.get(p.server),p.getUUID());for(var raw:t.getList(key,Tag.TAG_COMPOUND)){var row=(CompoundTag)raw;int slot=row.getInt("Slot");if(slot>=0&&slot<54)setItem(slot,ItemStack.parseOptional(p.registryAccess(),row.getCompound("Stack")));}loading=false;}
        @Override public void setChanged(){super.setChanged();if(!loading)persist();}
        void persist(){var d=LabyrinthData.get(owner.server);var own=personal(d,owner.getUUID());var list=new ListTag();for(int i=0;i<getContainerSize();i++)if(!getItem(i).isEmpty()){var row=new CompoundTag();row.putInt("Slot",i);row.put("Stack",getItem(i).save(owner.registryAccess()));list.add(row);}own.put(key,list);save(d,owner.getUUID(),own);}
        @Override public boolean stillValid(Player p){return p==owner&&inside(owner,LabyrinthPlace.HOTEL);}
    }
    public static void openProperty(ServerPlayer p,boolean drawer){if(!inside(p,LabyrinthPlace.HOTEL))return;var c=new Property(p,drawer?"Drawers":"LostProperty");p.openMenu(new SimpleMenuProvider((id,inv,who)->new ChestMenu(MenuType.GENERIC_9x6,id,inv,c,6),Component.literal(drawer?"217 — your drawers":"The desk — your lost property")));}
    /** Runs only after a real native chest menu opens; canceled block interactions cannot collect a tab. */
    @SubscribeEvent public static void chest(PlayerContainerEvent.Open e){if(e.getEntity() instanceof ServerPlayer p&&e.getContainer() instanceof ChestMenu)collectTab(p);}
    public static boolean collectTab(ServerPlayer p){if(!participant(p)||p.containerMenu instanceof ChestMenu menu&&menu.getContainer() instanceof Property)return false;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());if(own.getInt("Debt")<=0)return false;var property=new Property(p,"LostProperty");
        int free=-1;for(int i=0;i<54;i++)if(property.getItem(i).isEmpty()){free=i;break;}if(free<0)return false;
        for(int i=0;i<p.getInventory().items.size();i++){var s=p.getInventory().items.get(i);if(s.isEmpty())continue;var one=s.copyWithCount(1);
            // Save the original stack's complete native components before touching the live inventory.
            property.setItem(free,one);property.persist();s.shrink(1);p.getInventory().setChanged();own=personal(d,p.getUUID());own.putInt("Debt",Math.max(0,own.getInt("Debt")-1));save(d,p.getUUID(),own);p.displayClientMessage(Component.literal("One place on the hotel tab is settled. The desk holds the missing item."),false);return true;}
        return false;
    }
    /** A charge on the account. Once the account is closed the house asks nothing more. */
    private static void charge(CompoundTag own){if(closed(own))return;own.putInt("Debt",own.getInt("Debt")+1);own.putBoolean("Charged",true);}
    public static boolean drink(ServerPlayer p){if(!inside(p,LabyrinthPlace.HOTEL))return false;var b=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);var rel=p.position().subtract(Vec3.atLowerCornerOf(b));
        if(rel.x<HotelRooms.BAR_X0||rel.x>HotelRooms.BAR_X1+1||rel.z<HotelRooms.BAR_Z0||rel.z>HotelRooms.BAR_Z1+1)return false;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());if(own.getInt("Debt")>=256)return false;
        charge(own);own.putInt("Drinks",own.getInt("Drinks")+1);own.putBoolean("Drank",true);save(d,p.getUUID(),own);var world=world(d);world.putBoolean("GuestsArrived",true);saveWorld(d,world);p.heal(4);p.getFoodData().eat(2,.2F);return true;}
    public static void talk(ServerPlayer p,HotelActor actor){if(!inside(p,LabyrinthPlace.HOTEL)||p.distanceToSqr(actor)>36)return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());boolean signed=own.getBoolean("Signed");
        if(actor.role()==0){actor.gesture(HotelActor.GREET,50);p.displayClientMessage(Component.literal("The porter: "+(signed?VignetteTailoring.caretakerGreeting(p):closed(own)?(ending(own).equals("walked")?"We keep what we hold.":"Your account is settled. Your things are at the desk whenever you want them."):due(own)?"Your account is due tonight. Ring when you are ready.":"Your property is labelled; crouch at the desk to collect it. Your account is kept here.")),false);return;}
        if(actor.role()==1){long now=p.serverLevel().getGameTime();if(now-own.getLong("LastDrink")<40&&own.contains("LastDrink"))return;own.putLong("LastDrink",now);actor.gesture(HotelActor.POUR,60);give(p,new ItemStack(HotelRegistry.DRINK.get()));int line=own.getInt("BarLine04112");own.putInt("BarLine04112",line+1);String[] words=signed?new String[]{"For the caretaker. On the house.",VignetteTailoring.usual(p),"We keep your place ready.","A quiet evening. We prefer it that way."}:closed(own)?new String[]{"On the house.","There is nothing left to settle.","Another? We have all evening."}:new String[]{"Complimentary. Drink it here; it goes on your account.","A little something against the cold.","I remember what you drink.","The others will be along shortly.","No need to pay tonight. We keep an account."};save(d,p.getUUID(),own);p.displayClientMessage(Component.literal("The bartender: "+words[line%words.length]),false);return;}
        if(actor.role()==2||(actor.role()==4&&!actor.dancing())){actor.gesture(HotelActor.LISTEN,80);var deaths=own.getList("Deaths",Tag.TAG_COMPOUND);if(deaths.isEmpty())p.displayClientMessage(Component.literal("The scarred guest: I have no story of your death. Sit long enough and somebody will offer me one."),false);
            else{var death=deaths.getCompound(deaths.size()-1);String told=death.contains("Message")?death.getString("Message")+(death.getString("Place").isEmpty()?"":", "+death.getString("Place")):death.getString("Name")+" died";
                long inside=own.getLong("HouseDeathsTotal");String count=inside<=0?"":" They tell me you have died "+inside+(inside==1?" time":" times")+" in this house.";
                p.displayClientMessage(Component.literal("The scarred guest: They say "+told+". I heard it was "+(actor.role()==2?"a fall from the bandstand":"a walk into the frozen hedges")+". They always change the room."+count),false);}return;}
        p.displayClientMessage(Component.literal(actor.role()==3?(signed?"The pianist: "+VignetteTailoring.pianistGreeting(p):"The pianist: The second place has been kept all season."):"The dancer's hand passes through yours. The music continues."),false);
    }
    public static boolean hotelBed(Level level,BlockPos at){if(!level.dimension().equals(HouseDimensions.INTERIOR)||level.getServer()==null)return false;var b=IndianLakeRooms.base(level.getServer(),LabyrinthPlace.HOTEL);return b!=null&&(at.equals(b.offset(HotelRooms.BED))||at.equals(b.offset(HotelRooms.BED_HEAD)))&&level.getBlockState(at).getBlock() instanceof BedBlock&&!world(LabyrinthData.get(level.getServer())).getBoolean("Cold");}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());if(own.getInt("RestTicks")>0){own.remove("RestTicks");save(d,p.getUUID(),own);if(p.isSleeping())p.stopSleepInBed(true,false);}}}
    public static boolean rest(ServerPlayer p){if(!inside(p,LabyrinthPlace.HOTEL))return false;var b=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);if(p.distanceToSqr(b.offset(HotelRooms.BED).getCenter())>16)return false;var d=LabyrinthData.get(p.server);if(world(d).getBoolean("Cold")){p.displayClientMessage(Component.literal("The sheets are freezing. Restart the heating plant."),true);return false;}
        var own=personal(d,p.getUUID());if(!own.getString("Here").equals("hotel")){onArrive(p,LabyrinthPlace.HOTEL);own=personal(d,p.getUUID());}if(own.getBoolean("Rested")){var c=new Property(p,"Drawers");int empty=0,occupied=0;for(int i=0;i<54;i++)if(c.getItem(i).isEmpty())empty++;for(var s:p.getInventory().items)if(!s.isEmpty())occupied++;if(empty<occupied){p.displayClientMessage(Component.literal("Empty enough drawer space before resting again."),true);return false;}}
        var head=b.offset(HotelRooms.BED_HEAD);if(p.serverLevel().players().stream().anyMatch(other->other!=p&&other.isSleeping()&&other.getSleepingPos().filter(head::equals).isPresent()))return false;
        if(p.startSleepInBed(head).left().isPresent())return false;
        own.putInt("RestTicks",REST_TICKS);save(d,p.getUUID(),own);p.displayClientMessage(Component.literal(own.getBoolean("Rested")?"Stay beside the bed. Housekeeping will put your main pockets in your private drawers.":"Stay beside the bed. Housekeeping will sort your pockets. This bed does not set your spawn."),false);return true;
    }
    private static void finishRest(ServerPlayer p,CompoundTag own){
        if(p.isSleeping())p.stopSleepInBed(true,false);
        if(own.getBoolean("Rested")){var c=new Property(p,"Drawers");for(int i=0;i<p.getInventory().items.size();i++){var s=p.getInventory().items.get(i);if(s.isEmpty())continue;for(int j=0;j<54;j++)if(c.getItem(j).isEmpty()){c.setItem(j,s.copy());c.persist();p.getInventory().items.set(i,ItemStack.EMPTY);break;}}}
        else{var items=new ArrayList<ItemStack>(p.getInventory().items);items.sort(java.util.Comparator.comparing((ItemStack s)->s.isEmpty()?"~":net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem()).toString()).thenComparing(s->s.getHoverName().getString()));for(int i=0;i<items.size();i++)p.getInventory().items.set(i,items.get(i));}
        p.getInventory().setChanged();var d=LabyrinthData.get(p.server);own=personal(d,p.getUUID());own.putBoolean("Rested",true);own.remove("RestTicks");save(d,p.getUUID(),own);
        var b=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);var candle=p.serverLevel().getBlockState(b.offset(HotelRooms.CANDLE));if(candle.is(Blocks.CANDLE))p.serverLevel().setBlock(b.offset(HotelRooms.CANDLE),candle.setValue(CandleBlock.LIT,false),F);
        var placements=own.getList("Torches",Tag.TAG_COMPOUND);var returnedPositions=new HashSet<BlockPos>();var world=world(d);var owners=world.getCompound("TorchOwners");int returned=0;
        for(var raw:placements){var row=(CompoundTag)raw;var at=BlockPos.of(row.getLong("At"));String key=Long.toString(at.asLong());var s=p.serverLevel().getBlockState(at);
            if(owners.hasUUID(key)&&owners.getUUID(key).equals(p.getUUID())){owners.remove(key);if(returnedPositions.add(at)&&isTorch(s)){p.serverLevel().setBlock(at,Blocks.AIR.defaultBlockState(),F);returned++;}}}
        world.put("TorchOwners",owners);saveWorld(d,world);own.remove("Torches");save(d,p.getUUID(),own);if(returned>0)give(p,new ItemStack(Items.TORCH,returned));p.displayClientMessage(Component.literal("The bed is made. Check your pockets and your drawers."),false);
    }
    private static boolean isTorch(net.minecraft.world.level.block.state.BlockState s){return s.is(Blocks.TORCH)||s.is(Blocks.WALL_TORCH);}
    /** A guest may light 217 with their own torches; housekeeping hands them back. */
    public static boolean allowsTorchPlacing(Level l,BlockPos at,net.minecraft.world.level.block.state.BlockState s){if(l.getServer()==null||!l.dimension().equals(HouseDimensions.INTERIOR)||!isTorch(s))return false;var b=IndianLakeRooms.base(l.getServer(),LabyrinthPlace.HOTEL);if(b==null)return false;var r=at.subtract(b);return r.getY()>=6&&r.getY()<=8&&r.getX()>=HotelRooms.ROOM_X0&&r.getX()<=HotelRooms.ROOM_X1&&r.getZ()>=HotelRooms.ROOM_Z0&&r.getZ()<=HotelRooms.ROOM_Z1;}
    public static boolean mayBreakTorch(ServerLevel l,BlockPos at,Player p){if(!allowsTorchPlacing(l,at,l.getBlockState(at)))return false;var owners=world(LabyrinthData.get(l.getServer())).getCompound("TorchOwners");String key=Long.toString(at.asLong());return owners.hasUUID(key)&&owners.getUUID(key).equals(p.getUUID());}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void broken(BlockEvent.BreakEvent e){if(e.isCanceled()||!(e.getLevel() instanceof ServerLevel l)||!allowsTorchPlacing(l,e.getPos(),e.getState()))return;var d=LabyrinthData.get(l.getServer());var world=world(d);var owners=world.getCompound("TorchOwners");owners.remove(Long.toString(e.getPos().asLong()));world.put("TorchOwners",owners);saveWorld(d,world);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void placed(BlockEvent.EntityPlaceEvent e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!inside(p,LabyrinthPlace.HOTEL)||!allowsTorchPlacing(p.serverLevel(),e.getPos(),e.getPlacedBlock()))return;
        var d=LabyrinthData.get(p.server);var world=world(d);var owners=world.getCompound("TorchOwners");owners.putUUID(Long.toString(e.getPos().asLong()),p.getUUID());world.put("TorchOwners",owners);saveWorld(d,world);
        var own=personal(d,p.getUUID());var list=own.getList("Torches",Tag.TAG_COMPOUND);for(int i=list.size()-1;i>=0;i--)if(list.getCompound(i).getLong("At")==e.getPos().asLong())list.remove(i);var row=new CompoundTag();row.putLong("At",e.getPos().asLong());list.add(row);own.put("Torches",list);save(d,p.getUUID(),own);
    }
    public static int pressure(CompoundTag world,long now){return !world.getBoolean("PlantFound")||world.getBoolean("Cold")?0:(int)Math.min(3,Math.max(0,now-world.getLong("VentedAt"))/24000);}
    public static void maintain(ServerPlayer p,boolean restart){if(!inside(p,LabyrinthPlace.HOTEL))return;var b=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);if(p.distanceToSqr(b.offset(restart?HotelRooms.RESTART:HotelRooms.VALVE).getCenter())>36)return;var d=LabyrinthData.get(p.server);var world=world(d);if(!world.getBoolean("PlantFound"))return;if(world.getBoolean("Cold")&&!restart)return;world.putBoolean("Cold",false);world.putLong("VentedAt",p.serverLevel().getGameTime());saveWorld(d,world);var own=personal(d,p.getUUID());own.putBoolean("Maintained",true);save(d,p.getUUID(),own);p.serverLevel().playSound(null,b.offset(HotelRooms.BOILER),SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.8F,.55F);p.serverLevel().setBlock(b.offset(HotelRooms.FIRE),Blocks.CAMPFIRE.defaultBlockState(),F);}
    public static void plantTick(ServerLevel l,BlockPos b,long now){var d=LabyrinthData.get(l.getServer());var world=world(d);if(!world.getBoolean("PlantFound"))return;int pressure=pressure(world,now);var at=b.offset(HotelRooms.GAUGE);var s=l.getBlockState(at);if(s.is(HotelRegistry.PROP.get())&&s.getValue(HotelPropBlock.PRESSURE)!=pressure)l.setBlock(at,s.setValue(HotelPropBlock.PRESSURE,pressure),F);
        if(world.getBoolean("Cold")||now-world.getLong("VentedAt")<BOILER_LIMIT)return;world.putBoolean("Cold",true);world.putInt("Blasts",world.getInt("Blasts")+1);saveWorld(d,world);var fire=b.offset(HotelRooms.FIRE);if(l.getBlockState(fire).is(Blocks.CAMPFIRE))l.setBlock(fire,l.getBlockState(fire).setValue(CampfireBlock.LIT,false),F);
        var room=new AABB(Vec3.atLowerCornerOf(b.offset(HotelRooms.PLANT_X0,-5,HotelRooms.PLANT_Z0)),Vec3.atLowerCornerOf(b.offset(HotelRooms.PLANT_X1+1,-1,HotelRooms.PLANT_Z1+1)));for(var item:l.getEntitiesOfClass(ItemEntity.class,room))item.setRemainingFireTicks(100);for(var p:l.players())if(room.contains(p.position())&&participant(p)){p.hurt(l.damageSources().hotFloor(),4);p.setRemainingFireTicks(40);}l.playSound(null,b.offset(HotelRooms.BOILER),SoundEvents.GENERIC_EXPLODE.value(),SoundSource.BLOCKS,1,.55F);
    }
    /** The porter behind the desk, the bartender, the two scarred guests once the bar has company, the pianist once the photograph is taken. */
    private static final double[][] CAST={{-8.5,0,-4.5,-90},{22.5,0,-7.5,90},{19.5,0,-11.5,-90},{0,0,0,0},{.5,1,-37.5,0},{-22.5,0,-30.5,-90},{-26.5,6.45,-24.1,0}};
    private static void cast(ServerLevel l,BlockPos b){var d=LabyrinthData.get(l.getServer());var all=world(d);for(int i=0;i<6;i++){if(i==3)continue;if((i==2||i==5)&&!all.getBoolean("GuestsArrived"))continue;if(i==4&&!all.getBoolean("DancersMade"))continue;String key="Actor"+i;if(all.hasUUID(key))continue;var actor=HotelRegistry.ACTOR.get().create(l);if(actor==null)return;actor.appearance(i==4?3:i==5?4:i,false);actor.moveTo(b.getX()+CAST[i][0],b.getY()+CAST[i][1],b.getZ()+CAST[i][2],(float)CAST[i][3],0);if(l.addFreshEntity(actor)){all.putUUID(key,actor.getUUID());saveWorld(d,all);}}
        if(!all.hasUUID("BathGhost")){var ghost=HotelRegistry.ACTOR.get().create(l);if(ghost!=null){ghost.appearance(6,false);ghost.setNoGravity(true);ghost.bathState(HotelBathHaunt.DORMANT,0);ghost.noPhysics=true;ghost.moveTo(HotelBathHaunt.submerged(b));if(l.addFreshEntity(ghost)){all.putUUID("BathGhost",ghost.getUUID());saveWorld(d,all);}}}
        for(int i=0;i<6;i++)if(all.hasUUID("Dancer"+i)&&l.getEntity(all.getUUID("Dancer"+i)) instanceof HotelActor dancer)dancer.danceSlot(i);
        if(!all.hasUUID("Hose")){var hose=HotelRegistry.HOSE.get().create(l);if(hose!=null){hose.moveTo(b.getX()-13.6,b.getY()+6,b.getZ()-19.5,180,0);if(l.addFreshEntity(hose)){all.putUUID("Hose",hose.getUUID());saveWorld(d,all);}}}
    }
    /** Standing on the ballroom floor puts the guest in the 1921 photograph: the front row's empty place is theirs. */
    public static void photograph(ServerPlayer p){if(!inside(p,LabyrinthPlace.HOTEL))return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());if(own.getBoolean("Photographed"))return;var b=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);var rel=p.position().subtract(Vec3.atLowerCornerOf(b));
        if(rel.x<HotelRooms.FLOOR_X0||rel.x>HotelRooms.FLOOR_X1+1||rel.z<HotelRooms.FLOOR_Z0||rel.z>HotelRooms.FLOOR_Z1+1||rel.y>.5||rel.y<-.5)return;own.putBoolean("Photographed",true);var card=new ItemStack(HotelRegistry.PHOTOGRAPH.get());card.set(DataComponents.PROFILE,new ResolvableProfile(p.getGameProfile()));card.set(DataComponents.CUSTOM_NAME,Component.literal("Closing night — "+p.getGameProfile().getName()));reward(p,own,"Photo",card);save(d,p.getUUID(),own);
        if(p.serverLevel().getBlockEntity(b.offset(HotelRooms.PHOTO)) instanceof HotelPhotoBlockEntity print)print.sitter(p.getGameProfile());
        var world=world(d);if(!world.getBoolean("DancersMade")){for(int i=0;i<6;i++){var actor=HotelRegistry.ACTOR.get().create(p.serverLevel());if(actor!=null){actor.appearance(4+i%2,true);actor.danceSlot(i);actor.moveTo(HotelActor.dancePosition(b,i,p.serverLevel().getGameTime()));if(p.serverLevel().addFreshEntity(actor))world.putUUID("Dancer"+i,actor.getUUID());}}world.putBoolean("DancersMade",true);saveWorld(d,world);}HousePackets.send(p,new HotelAtmospherePayload(0,b.offset(HotelRooms.PHOTO),false,8));
    }
    /** Actual native hits, personal cooldowns and positional sounds make the furnishings usable. */
    private static void playPiano(ServerPlayer p,BlockPos at,CompoundTag own){long now=p.serverLevel().getGameTime();if(own.contains("LastPiano")&&now-own.getLong("LastPiano")<80)return;own.putLong("LastPiano",now);save(LabyrinthData.get(p.server),p.getUUID(),own);p.connection.send(new ClientboundSoundPacket(HotelRegistry.PIANO,SoundSource.RECORDS,at.getX()+.5,at.getY()+1,at.getZ()+.5,.65F,1,p.getRandom().nextLong()));p.displayClientMessage(Component.literal("The keys are warm. The second place in the tune stays empty."),false);}
    /**
     * The bell. On the last night it settles the account: the porter takes what is owing from the guest's pack, one thing for
     * each uncollected place, and keeps it; once nothing is owing the settled account is the guest's to read. Before then, or
     * once the account is closed, the porter only acknowledges it.
     */
    private static void ringBell(ServerPlayer p,BlockPos at,CompoundTag own){long now=p.serverLevel().getGameTime();if(own.contains("LastBell")&&now-own.getLong("LastBell")<24)return;own.putLong("LastBell",now);var d=LabyrinthData.get(p.server);save(d,p.getUUID(),own);p.connection.send(new ClientboundSoundPacket(HotelRegistry.BELL,SoundSource.BLOCKS,at.getX()+.5,at.getY()+1.1,at.getZ()+.5,.65F,1,p.getRandom().nextLong()));var all=world(d);if(all.hasUUID("Actor0")&&p.serverLevel().getEntity(all.getUUID("Actor0")) instanceof HotelActor porter)porter.gesture(HotelActor.GREET,50);
        if(!due(own)){p.displayClientMessage(Component.literal(closed(own)?"The porter acknowledges the bell.":"The porter acknowledges the bell. Your property is kept at reception."),false);return;}
        if(own.getBoolean("Settled")){openLive(p,"Account",HotelTexts.settled(p.getGameProfile().getName(),own.getList("Paid",Tag.TAG_COMPOUND).size()),true);return;}
        if(!own.getBoolean("Charged")){p.displayClientMessage(Component.literal("The porter waits. There is nothing on your account to settle."),false);return;}
        int took=pay(p,own);own=personal(d,p.getUUID());
        if(own.getInt("Debt")>0){p.displayClientMessage(Component.literal(took==0?"The porter waits. You carry nothing to settle the account with.":"The porter takes what you carry against the account. "+own.getInt("Debt")+" places remain."),false);return;}
        own.putBoolean("Settled",true);save(d,p.getUUID(),own);openLive(p,"Account",HotelTexts.settled(p.getGameProfile().getName(),own.getList("Paid",Tag.TAG_COMPOUND).size()),true);
    }
    /** One unit of whatever the guest carries for each place owing, last pocket first, kept by the house with its every component. */
    private static int pay(ServerPlayer p,CompoundTag own){var d=LabyrinthData.get(p.server);int took=0;var paid=own.getList("Paid",Tag.TAG_COMPOUND);var items=p.getInventory().items;
        for(int i=items.size()-1;i>=0&&own.getInt("Debt")>0;i--){var s=items.get(i);while(!s.isEmpty()&&own.getInt("Debt")>0){paid.add(s.copyWithCount(1).save(p.registryAccess()));s.shrink(1);own.putInt("Debt",own.getInt("Debt")-1);took++;}}
        own.put("Paid",paid);p.getInventory().setChanged();save(d,p.getUUID(),own);return took;}
    /** The register: the 1921 party's names and a blank second line, which on the last night a guest in the photograph may sign. */
    private static void register(ServerPlayer p,CompoundTag own){String name=p.getGameProfile().getName();
        if(own.getBoolean("Signed")){openLive(p,"Register",HotelTexts.signed(name),false);return;}
        if(due(own)&&own.getBoolean("Photographed")){openLive(p,"Register",HotelTexts.sign(name),true);return;}
        if(due(own))p.displayClientMessage(Component.literal("The second line is for someone in the photograph."),true);
        openLive(p,"Register",HotelTexts.register(),false);}
    /** Out by the staff door on the last night, with the master key: the hotel keeps what it holds of the guest's, and the account is closed. */
    private static void walkedOut(ServerPlayer p){if(!participant(p))return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());if(!due(own)||!own.getBoolean("MasterKey"))return;
        own.remove("LostProperty");own.remove("Drawers");save(d,p.getUUID(),own);close(p,"walked",WALKED);
        p.displayClientMessage(Component.literal("You go out by the staff door before the account is presented. Behind you the hotel keeps what it held of yours."),false);}
    /** This guest's own view of the hotel: the snow at their windows, and the table for two as they left it. */
    public static void views(ServerPlayer p,BlockPos b,CompoundTag own){var l=p.serverLevel();int snow=snow(own);
        for(var w:HotelRooms.WINDOWS)for(int dy=0;dy<=1;dy++){var at=b.offset(w.low()).above(dy);var s=l.getBlockState(at);if(s.is(HotelRegistry.STANLEY.get())&&s.getValue(StanleyBlock.KIND).window())p.connection.send(new ClientboundBlockUpdatePacket(at,s.setValue(StanleyBlock.STAGE,snow)));}
        var table=b.offset(HotelRooms.TABLE);var s=l.getBlockState(table);if(StanleyBlock.is(s,StanleyBlock.Kind.TABLE_SET))p.connection.send(new ClientboundBlockUpdatePacket(table,s.setValue(StanleyBlock.STAGE,own.getBoolean("Dinner")?StanleyBlock.CLEARED:own.getBoolean("MealServed")?StanleyBlock.SERVED:StanleyBlock.LAID)));
    }
    private static boolean door(BlockPos rel,BlockPos lower){return rel.equals(lower)||rel.equals(lower.above());}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void click(PlayerInteractEvent.RightClickBlock e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||e.getHand()!=InteractionHand.MAIN_HAND)return;var place=current(p);if(place==null||p.distanceToSqr(e.getPos().getCenter())>36)return;var b=IndianLakeRooms.base(p.server,place);var rel=e.getPos().subtract(b);boolean handled=false;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());var surface=p.serverLevel().getBlockState(e.getPos());
        if(place==LabyrinthPlace.HOTEL_GROUNDS){if(rel.equals(HotelRooms.KEY.below())){own.putBoolean("MasterKey",true);reward(p,own,"Key",new ItemStack(HotelRegistry.MASTER_KEY.get()));save(d,p.getUUID(),own);p.displayClientMessage(Component.literal("The master key was warm under the snow."),false);handled=true;}
            else if(surface.is(HotelRegistry.PROP.get())&&surface.getValue(HotelPropBlock.KIND)==HotelPropBlock.Kind.HEADSTONE){var pages=new ArrayList<String>();for(var raw:own.getList("LostPets",Tag.TAG_COMPOUND))pages.add(((CompoundTag)raw).getString("Name"));if(pages.isEmpty())pages.add("No name of a lost tamed companion has been entered for this guest.");openCemetery(p,own,HouseWriting.book("Names in the snow","The groundskeeper",HouseWriting.WritingStyle.PLAIN,pages));handled=true;}
            else if(door(rel,HotelRooms.STAFF_DOOR_OUTSIDE)){p.displayClientMessage(Component.literal("The staff door is bolted from inside."),true);handled=true;}}
        else if(place==LabyrinthPlace.HOTEL){
            if(rel.equals(HotelRooms.DESK)){var hit=e.getHitVec().getLocation().subtract(Vec3.atLowerCornerOf(e.getPos()));if(surface.is(HotelRegistry.PROP.get()))hit=HotelPropBlock.localHit(surface,hit);
                if(p.isShiftKeyDown())openProperty(p,false);else if(hit.y>=.99&&hit.x>=.6&&hit.x<=.9&&hit.z>=.2&&hit.z<=.55)ringBell(p,e.getPos(),own);else account(p,own);handled=true;}
            else if(rel.equals(HotelRooms.REGISTER)){register(p,own);handled=true;}
            else if(surface.is(HotelRegistry.PROP.get())&&surface.getValue(HotelPropBlock.KIND)==HotelPropBlock.Kind.PIANO){playPiano(p,e.getPos(),own);handled=true;}
            else if(rel.equals(HotelRooms.GAUGE)||rel.equals(HotelRooms.BOILER)){var all=world(d);var gauge=p.serverLevel().getBlockState(b.offset(HotelRooms.GAUGE));String pressure=gauge.is(HotelRegistry.PROP.get())?new String[]{"low","steady","rising","critical"}[gauge.getValue(HotelPropBlock.PRESSURE)]:"gauge missing";p.displayClientMessage(Component.literal(all.getBoolean("Cold")?"The boiler is cold. Its restart switch is beside the vent valve.":"Pressure: "+pressure+". The vent valve is to the right of the gauge."),false);handled=true;}
            else if(rel.equals(HotelRooms.HOSE_RACK)){var all=world(d);boolean away=all.hasUUID("Hose")&&p.serverLevel().getEntity(all.getUUID("Hose")) instanceof HotelHose hose&&hose.phase()>0;p.displayClientMessage(Component.literal(away?"The rack is empty. The hose is somewhere along the corridor.":"The old hose is tightly wound. Its rubber smells warm."),false);handled=true;}
            else if(rel.equals(HotelRooms.DRAWER)){openProperty(p,true);handled=true;}
            else if(rel.equals(HotelRooms.BED)||rel.equals(HotelRooms.BED_HEAD)){rest(p);handled=true;}
            else if(rel.equals(HotelRooms.LOG)){if(own.getBoolean("Signed"))openLive(p,"Log",HotelTexts.log(p.getGameProfile().getName()),false);else openPaper(p,"Log",HotelTexts.log());handled=true;}
            else if(rel.equals(HotelRooms.TYPEWRITER)){if(own.getBoolean("Signed"))openLive(p,"Manuscript",HotelTexts.manuscript(p.getGameProfile().getName()),false);else openPaper(p,"Manuscript",HotelTexts.manuscript());handled=true;}
            else if(rel.equals(HotelRooms.HOUSEKEEPING)){openPaper(p,"Housekeeping",HotelTexts.housekeeping());handled=true;}
            else if(rel.equals(HotelRooms.RULES)){openPaper(p,"Rules",HotelTexts.rules());handled=true;}
            else if(rel.equals(HotelRooms.PLANT)){openPaper(p,"Plant",HotelTexts.boiler());handled=true;}
            else if(rel.equals(HotelRooms.OFFICE_NOTES)){openPaper(p,"Office",HotelTexts.office());handled=true;}
            else if(rel.equals(HotelRooms.FAMILY_NOTE)){openPaper(p,"Family",HotelTexts.family());handled=true;}
            else if(rel.equals(HotelRooms.TIME_CLOCK)){p.displayClientMessage(Component.literal("The staff cards are punched out for the season. One card in the rack has no name on it yet."),false);handled=true;}
            else if(door(rel,HotelRooms.SERVICE_DOOR)){if(p.getMainHandItem().is(HotelRegistry.MASTER_KEY.get()))NovelRooms.door(p.serverLevel(),b.offset(HotelRooms.SERVICE_DOOR),Direction.SOUTH,Blocks.IRON_DOOR,true);else p.displayClientMessage(Component.literal("The service door needs the master key."),true);handled=true;}
            else if(rel.equals(HotelRooms.VALVE)||rel.equals(HotelRooms.RESTART)){maintain(p,rel.equals(HotelRooms.RESTART));handled=true;}
            else if(rel.equals(HotelRooms.PHOTO)||rel.equals(HotelRooms.FRONT_ROW)){openLive(p,"Party",HotelTexts.party(p.getGameProfile().getName(),own.getBoolean("Photographed")),false);handled=true;}
            else if(rel.equals(HotelRooms.TABLE)){dine(p,b,own);handled=true;}
            else if(rel.equals(HotelRooms.PATCH)){p.serverLevel().setBlock(e.getPos(),Blocks.AIR.defaultBlockState(),F);handled=true;}
            else for(var room:HotelRooms.LOCKED)if(door(rel,room.door())){p.serverLevel().playSound(null,e.getPos(),SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.5F,.6F);p.displayClientMessage(Component.literal("The door is locked. Someone lives here."),true);handled=true;break;}
        }if(handled){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    }
    /** The account as it stands, at the desk. */
    private static void account(ServerPlayer p,CompoundTag own){String name=p.getGameProfile().getName();int held=own.getList("LostProperty",Tag.TAG_COMPOUND).size(),drawers=own.getList("Drawers",Tag.TAG_COMPOUND).size();
        int dinner=own.getBoolean("Dinner")?1:0,drinks=own.getInt("Drinks"),debt=own.getInt("Debt");
        if(closed(own)){switch(ending(own)){case "signed"->openLive(p,"Register",HotelTexts.signed(name),false);case "walked"->p.displayClientMessage(Component.literal("The desk: Your account was closed when you went out by the staff door. We keep what we held."),false);default->openLive(p,"Account",HotelTexts.settled(name,own.getList("Paid",Tag.TAG_COMPOUND).size()),false);}return;}
        if(own.getBoolean("Settled")){openLive(p,"Account",HotelTexts.settled(name,own.getList("Paid",Tag.TAG_COMPOUND).size()),true);return;}
        openLive(p,"AccountOpen",due(own)?HotelTexts.due(name,dinner,drinks,debt,held,drawers):HotelTexts.account(name,dinner,drinks,debt,held,drawers),false);}
    /** The table for two: sit, and dinner is served; eat it, and it goes on the account. */
    private static void dine(ServerPlayer p,BlockPos b,CompoundTag own){var d=LabyrinthData.get(p.server);
        if(own.getBoolean("MealServed")&&!own.getBoolean("Dinner")){own.putBoolean("Dinner",true);charge(own);save(d,p.getUUID(),own);p.heal(3);p.getFoodData().eat(6,.5F);views(p,b,own);return;}
        p.displayClientMessage(Component.literal(own.getBoolean("Dinner")?"Your plate has been cleared. The other place is still laid.":"The table is laid for two. Sit, and dinner will be served."),true);}
    /** Where in the House a death happened, as the House names its rooms; out in the world it is somewhere else. */
    private static String where(ServerPlayer p){
        if(!io.github.knaitoe.theoldesthouse.house.HouseDimensions.isHouseDimension(p.level().dimension()))return "out in the world";
        LabyrinthPlace place=LiteraryVignettes.current(p);if(place==null)place=NovelVignettes.current(p);if(place==null)place=ClassicsVignettes.current(p);if(place==null)place=current(p);
        var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(p.server).houseOrigin();if(place==null&&origin!=null)place=LabyrinthPlaces.placeAt(origin,p.blockPosition());
        return place==null?"in the House":"in "+StaircaseProse.place(place.id());
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent e){
        if(e.isCanceled()||!(e.getEntity().level() instanceof ServerLevel l)||e.getEntity().getTags().contains(LiteraryCopies.PROJECTION))return;
        UUID id=null;String key=null;
        if(e.getEntity() instanceof ServerPlayer p){id=p.getUUID();key="Deaths";}
        else if(e.getEntity() instanceof TamableAnimal t&&t.isTame()&&t.getOwnerUUID()!=null&&!t.getTags().contains(MotherOfStrays.PET)){id=t.getOwnerUUID();key="LostPets";}
        if(id==null)return;var d=LabyrinthData.get(l.getServer());var own=personal(d,id);var list=own.getList(key,Tag.TAG_COMPOUND);
        var row=new CompoundTag();row.putUUID("Entity",e.getEntity().getUUID());row.putString("Name",e.getEntity().getName().getString());row.putString("Cause",e.getSource().getMsgId());row.putLong("At",l.getGameTime());
        // How and where, in words the strangers can retell: the game's own death message and the room it happened in.
        row.putString("Message",e.getSource().getLocalizedDeathMessage(e.getEntity()).getString());if(e.getEntity() instanceof ServerPlayer p){row.putString("Place",where(p));if(io.github.knaitoe.theoldesthouse.house.HouseDimensions.isHouseDimension(p.level().dimension()))own.putLong("HouseDeathsTotal",own.getLong("HouseDeathsTotal")+1);}list.add(row);
        own.putLong(key+"Total",Math.max(own.getLong(key+"Total"),list.size()-1L)+1);
        while(list.size()>(key.equals("Deaths")?DEATH_HISTORY:PET_HISTORY))list.remove(0);
        own.put(key,list);save(d,id,own);
    }
    /** Legacy overflow labels clean up only their own loaded headstone and support. */
    private static void boundGrave(Entity label){
        if(!label.getTags().contains("HotelGrave")||!(label.level() instanceof ServerLevel level)||!level.dimension().equals(HouseDimensions.OUTSIDE))return;
        var b=IndianLakeRooms.base(level.getServer(),LabyrinthPlace.HOTEL_GROUNDS);if(b==null)return;
        var rel=label.position().subtract(Vec3.atLowerCornerOf(b));
        if(rel.x>=-23.5&&rel.x<=24.5&&rel.z>=-67.5&&rel.z<=-58.5)return;
        var at=BlockPos.containing(label.getX(),label.getY()-1.8,label.getZ());var state=level.getBlockState(at);
        if(state.is(HotelRegistry.PROP.get())&&state.getValue(HotelPropBlock.KIND)==HotelPropBlock.Kind.HEADSTONE){
            level.setBlock(at,Blocks.AIR.defaultBlockState(),F);
            if(level.getBlockState(at.below()).is(Blocks.GRAVEL))level.setBlock(at.below(),Blocks.AIR.defaultBlockState(),F);
        }
        label.discard();
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void graveLoaded(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e){if(!e.isCanceled()){boundGrave(e.getEntity());if(e.getEntity().getTags().contains("HotelGrave")&&e.getEntity().isRemoved())e.setCanceled(true);}}
    /** The four hedge animals stand two each side of the walk from the terrace to the maze. */
    private static int topiaryX(int group){return group%2==0?-10:6;}
    private static int topiaryZ(int group){return -12-(group/2)*8;}
    private static void garden(ServerPlayer p,BlockPos b){
        var d=LabyrinthData.get(p.server);var world=world(d);var l=p.serverLevel();
        if(!world.getBoolean("GraveFoundation0437")){
            // The saved grounds include z=-68, one row beyond the original dirt fill.
            // Fill only that missing foundation; keep old grave positions and scenery.
            for(int x=-27;x<=27;x++)for(int y=-3;y<=-1;y++){
                var at=b.offset(x,y,-68);if(l.getBlockState(at).isAir())l.setBlock(at,Blocks.DIRT.defaultBlockState(),F);
            }
            world.putBoolean("GraveFoundation0437",true);saveWorld(d,world);
        }
        int[][] form={{0,0,0},{1,0,0},{2,0,0},{0,-1,0},{2,-1,0},{2,1,0},{3,1,0}};
        for(int group=0;group<4;group++)for(int part=0;part<form.length;part++){
            String key="Topiary"+group+"_"+part;if(world.hasUUID(key))continue;var display=EntityType.BLOCK_DISPLAY.create(l);if(display==null)continue;
            var tag=new CompoundTag();display.saveWithoutId(tag);tag.put("block_state",NbtUtils.writeBlockState(Blocks.OAK_LEAVES.defaultBlockState()));display.load(tag);
            display.moveTo(b.getX()+topiaryX(group)+form[part][0],b.getY()+1+form[part][1],b.getZ()+topiaryZ(group));display.addTag("HotelTopiary");
            if(l.addFreshEntity(display)){world.putUUID(key,display.getUUID());saveWorld(d,world);}
        }
        if(l.getGameTime()%80==0)for(int group=0;group<4;group++){
            var pieces=new ArrayList<Entity>();for(int part=0;part<form.length;part++){String key="Topiary"+group+"_"+part;if(world.hasUUID(key)&&l.getEntity(world.getUUID(key))!=null)pieces.add(l.getEntity(world.getUUID(key)));}
            boolean seen=l.players().stream().filter(Player::isAlive).anyMatch(w->pieces.stream().anyMatch(a->HouseWatchers.sees(w,a.position().add(.5,.5,.5))));
            if(!seen){boolean turn=!world.getBoolean("TopiaryPose"+group);for(int part=0;part<pieces.size();part++){var a=pieces.get(part);double x=b.getX()+topiaryX(group)+(turn?3-form[part][0]:form[part][0]);a.moveTo(x,b.getY()+1+form[part][1],b.getZ()+topiaryZ(group)+(turn?1:0));}world.putBoolean("TopiaryPose"+group,turn);saveWorld(d,world);}
        }
        if(l.getGameTime()%20==0)for(var entity:List.copyOf(l.getEntitiesOfClass(Display.TextDisplay.class,new AABB(b).inflate(256),a->a.getTags().contains("HotelGrave"))))boundGrave(entity);
        var own=personal(d,p.getUUID());int plot=Math.min(GRAVE_LIMIT,world.getInt("Graves"));
        for(var raw:own.getList("LostPets",Tag.TAG_COMPOUND)){
            if(plot>=GRAVE_LIMIT)break;var pet=(CompoundTag)raw;String key="Grave_"+pet.getUUID("Entity");if(world.getBoolean(key))continue;
            int x=-24+(plot%13)*4,z=-59-(plot/13)*3;var at=b.offset(x,0,z);
            l.setBlock(at.below(),Blocks.GRAVEL.defaultBlockState(),F);l.setBlock(at,HotelRegistry.PROP.get().defaultBlockState().setValue(HotelPropBlock.KIND,HotelPropBlock.Kind.HEADSTONE),F);
            var name=EntityType.TEXT_DISPLAY.create(l);if(name!=null){var tag=new CompoundTag();name.saveWithoutId(tag);tag.putString("text",Component.Serializer.toJson(Component.literal(pet.getString("Name")),l.registryAccess()));tag.putString("billboard","center");tag.putInt("background",0);tag.putInt("line_width",90);name.load(tag);name.moveTo(at.getX()+.5,at.getY()+1.8,at.getZ()+.5);name.addTag("HotelGrave");if(l.addFreshEntity(name))world.putUUID(key+"Label",name.getUUID());}
            world.putBoolean(key,true);world.putInt("Graves",++plot);saveWorld(d,world);
        }
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();if(origin==null)return;var l=server.getLevel(HouseDimensions.INTERIOR);if(l==null)return;var b=LabyrinthPlaces.base(origin,LabyrinthPlace.HOTEL);var d=LabyrinthData.get(server);
        for(var p:server.getPlayerList().getPlayers()){var place=current(p);var own=personal(d,p.getUUID());
            if(place==null){if(!own.getString("Here").isEmpty()){
                    // A rest ends at once; the stay itself ends only once the guest has been gone a while, so crossing to the grounds is the same night.
                    if(own.getInt("RestTicks")>0){if(p.isSleeping())p.stopSleepInBed(true,false);own.remove("RestTicks");}
                    long now=l.getGameTime();if(!own.contains("Away"))own.putLong("Away",now);else if(now-own.getLong("Away")>100){own.remove("Here");own.remove("Away");own.remove("Stay");}
                    save(d,p.getUUID(),own);}continue;}
            if(!place.id().equals(own.getString("Here"))){onArrive(p,place);own=personal(d,p.getUUID());}
            else if(own.contains("Away")){own.remove("Away");save(d,p.getUUID(),own);}
            if(place==LabyrinthPlace.HOTEL_GROUNDS){var gb=IndianLakeRooms.base(server,place);IndianLakeRooms.keepLoaded(p.serverLevel(),gb,place);HousePackets.send(p,new HotelAtmospherePayload(2,gb,false,0));garden(p,gb);continue;}if(place!=LabyrinthPlace.HOTEL)continue;
            IndianLakeRooms.keepLoaded(l,b,place);cast(l,b);VignetteRoutineEchoes.hotel(p,b,own);
            var castState=world(d);if(castState.hasUUID("Hose")&&l.getEntity(castState.getUUID("Hose")) instanceof HotelHose hose){int next=Math.min(2,Math.max(0,own.getInt("Visits")-1));if(next>hose.phase()&&l.players().stream().filter(Player::isAlive).noneMatch(w->HouseWatchers.sees(w,hose.position().add(0,.2,0))))hose.phase(next);}
            var rel=p.position().subtract(Vec3.atLowerCornerOf(b));
            if(p.isPassenger()&&Math.abs(rel.x-HotelRooms.TABLE.getX()-.5)<1.5&&Math.abs(rel.z-HotelRooms.TABLE.getZ()-.5)<2.5&&rel.y<1.5&&!own.getBoolean("Dinner")&&!own.getBoolean("MealServed")){own.putBoolean("MealServed",true);save(d,p.getUUID(),own);views(p,b,own);}
            if(own.getInt("RestTicks")>0){if(!p.isSleeping()||p.distanceToSqr(b.offset(HotelRooms.BED).getCenter())>36||world(d).getBoolean("Cold")){own.remove("RestTicks");if(p.isSleeping())p.stopSleepInBed(true,false);}else{own.putInt("RestTicks",own.getInt("RestTicks")-1);save(d,p.getUUID(),own);if(own.getInt("RestTicks")==0){finishRest(p,own);own=personal(d,p.getUUID());}}}
            if(rel.y<-2&&rel.z<HotelRooms.PLANT_Z1+1){var world=world(d);if(!world.getBoolean("PlantFound")){world.putBoolean("PlantFound",true);world.putLong("VentedAt",l.getGameTime());saveWorld(d,world);}}
            var plaque=b.offset(HotelRooms.PLAQUE_217);if(rel.y>5&&HouseWatchers.sees(p,plaque.getCenter()))own.putBoolean("Saw217",true);
            if(own.getBoolean("Saw217")&&!world(d).getBoolean("NumberTurned")&&l.players().stream().filter(Player::isAlive).noneMatch(w->HouseWatchers.sees(w,plaque.getCenter()))){HotelRoomPlaqueBlockEntity.number(l,plaque,237);var world=world(d);world.putBoolean("NumberTurned",true);saveWorld(d,world);}
            save(d,p.getUUID(),own);photograph(p);
            if((server.getTickCount()+(p.getId()&63))%100==0)views(p,b,own);
            if(server.getTickCount()%80==0){var o=HotelRooms.ORCHESTRA;p.connection.send(new ClientboundSoundPacket(HotelRegistry.ORCHESTRA,SoundSource.RECORDS,b.getX()+o.getX()+.5,b.getY()+o.getY(),b.getZ()+o.getZ()+.5,.4F,1,p.getRandom().nextLong()));var piano=HotelRooms.PIANO;p.connection.send(new ClientboundSoundPacket(HotelRegistry.PIANO,SoundSource.RECORDS,b.getX()+piano.getX()+.5,b.getY()+piano.getY()+1,b.getZ()+piano.getZ()+.5,.45F,1,p.getRandom().nextLong()));}
        }if(server.getTickCount()%20==0)plantTick(l,b,l.getGameTime());
    }
}
