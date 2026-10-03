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

/** One hotel account, eight connected experiences; custody and knowledge are always personal. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HotelVignette {
    public static final String STATE="hotel_0435",OWNER="HotelGuest",PAPER="HotelPaper";
    public static final int REST_TICKS=80;public static final long BOILER_LIMIT=72000;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private HotelVignette(){}
    public static CompoundTag personal(LabyrinthData d,UUID id){return d.stateEntry(STATE,id.toString());}
    public static void save(LabyrinthData d,UUID id,CompoundTag t){var all=d.state(STATE);all.put(id.toString(),t.copy());d.setState(STATE,all);}
    private static boolean participant(ServerPlayer p){return p.isAlive()&&p.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR&&!FinaleProgress.terminal(FinaleProgress.phase(p.server,p.getUUID()));}
    public static boolean inside(ServerPlayer p,LabyrinthPlace place){var b=IndianLakeRooms.base(p.server,place);return participant(p)&&b!=null&&p.level().dimension().equals(NovelRooms.dimension(place))&&IndianLakeRooms.bounds(b,place).contains(p.position());}
    public static @Nullable LabyrinthPlace current(ServerPlayer p){for(var place:List.of(LabyrinthPlace.HOTEL,LabyrinthPlace.HOTEL_GROUNDS,LabyrinthPlace.HOTEL_HALLWAY))if(inside(p,place))return place;return null;}
    public static void onArrive(ServerPlayer p,LabyrinthPlace place){if(place!=LabyrinthPlace.HOTEL&&place!=LabyrinthPlace.HOTEL_GROUNDS&&place!=LabyrinthPlace.HOTEL_HALLWAY)return;if(!participant(p))return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());own.putString("Here",place.id());own.remove("RestTicks");if(place==LabyrinthPlace.HOTEL){own.putInt("Visits",own.getInt("Visits")+1);p.displayClientMessage(Component.literal("Closing night. Dinner is at the remaining table. Read the desk's terms."),false);}save(d,p.getUUID(),own);}
    public static void reverse(ServerPlayer p,int shift,int remaining){if(!participant(p)||shift<=0)return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());own.putBoolean("AgainstNumbers",true);own.putBoolean("HallExit",remaining==0);save(d,p.getUUID(),own);}
    /** The physical old loop is preserved; an earned backward arrival may find the hotel at its entry. */
    public static @Nullable LabyrinthData.Door route(ServerPlayer p,LabyrinthData.Door door){if(!door.id.equals(LabyrinthPlace.HOTEL_HALLWAY.entryDoorId())||!inside(p,LabyrinthPlace.HOTEL_HALLWAY))return null;var own=personal(LabyrinthData.get(p.server),p.getUUID());
        if(!own.getBoolean("HallExit")||own.getInt("Visits")>0)return null;return new LabyrinthData.Door(door.id,door.dimension,door.lower,door.facing,"place:hotel",door.command);}
    private static void give(ServerPlayer p,ItemStack s){if(p.getInventory().add(s))return;var drop=new ItemEntity(p.serverLevel(),p.getX(),p.getY()+.3,p.getZ(),s);drop.setTarget(p.getUUID());p.serverLevel().addFreshEntity(drop);}
    private static ItemStack marked(ServerPlayer p,ItemStack s,String key){CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putUUID(OWNER,p.getUUID());t.putString(PAPER,key);});return VignetteYields.mark(s,"hotel");}
    private static ItemStack snapshot(ServerPlayer p,CompoundTag own,String key,ItemStack s){String slot="Original_"+key;if(!own.contains(slot))own.put(slot,marked(p,s,key).save(p.registryAccess()));return ItemStack.parseOptional(p.registryAccess(),own.getCompound(slot));}
    private static void reward(ServerPlayer p,CompoundTag own,String key,ItemStack s){if(own.getBoolean("Given_"+key))return;own.putBoolean("Given_"+key,true);give(p,snapshot(p,own,key,s));}
    public static boolean ready(CompoundTag t){return t.getBoolean("Dinner")&&t.getBoolean("Rested")&&t.getBoolean("Drank")&&t.getBoolean("Photographed")&&t.getBoolean("MasterKey")&&t.getBoolean("Read_Log")&&t.getBoolean("Read_Housekeeping")&&t.getBoolean("Maintained")&&t.getInt("Debt")==0;}
    private static void openPaper(ServerPlayer p,String key,ItemStack book,boolean ending){var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());var original=snapshot(p,own,key,book);save(d,p.getUUID(),own);p.openMenu(new SimpleMenuProvider((id,inv,who)->new Pages(id,p,original,key,ending,true,true),original.getHoverName()));}
    public static void readCollected(ServerPlayer p,ItemStack original){var data=original.get(DataComponents.CUSTOM_DATA);if(data==null||!original.has(DataComponents.WRITTEN_BOOK_CONTENT))return;var tag=data.copyTag();boolean owned=tag.hasUUID(OWNER)&&tag.getUUID(OWNER).equals(p.getUUID());String key=tag.getString(PAPER);p.openMenu(new SimpleMenuProvider((id,inv,who)->new Pages(id,p,original.copy(),key,key.equals("Account"),owned,false),original.getHoverName()));}
    public static final class Pages extends LecternMenu {
        private final ServerPlayer reader;private final ItemStack original;private final String key;private final boolean ending,owned,surface;
        Pages(int id,ServerPlayer p,ItemStack s,String key,boolean ending,boolean owned,boolean surface){super(id,bookContainer(s),new SimpleContainerData(1));reader=p;original=s.copy();this.key=key;this.ending=ending;this.owned=owned;this.surface=surface;}
        private static Container bookContainer(ItemStack s){var c=new SimpleContainer(1);c.setItem(0,s.copy());return c;}
        public ItemStack book(){return original.copy();}
        @Override public boolean clickMenuButton(Player player,int button){if(player!=reader||!participant(reader)||(surface&&current(reader)==null))return false;var d=LabyrinthData.get(reader.server);var own=personal(d,reader.getUUID());
            if(button==3){if(!owned||!surface||own.getBoolean("Taken_"+key))return false;own.putBoolean("Taken_"+key,true);save(d,reader.getUUID(),own);give(reader,original.copy());return true;}
            if(!super.clickMenuButton(player,button))return false;if(owned&&getPage()==original.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()-1){own.putBoolean("Read_"+key,true);if(ending&&inside(reader,LabyrinthPlace.HOTEL)&&ready(own)){WitnessAccount.resolve(reader,WitnessAccount.Story.HOTEL,"read_the_settled_account");d.setCompleted("hotel",true);own.putBoolean("ClosedAccount",true);}save(d,reader.getUUID(),own);}return true;
        }
    }
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
    public static boolean drink(ServerPlayer p){if(!inside(p,LabyrinthPlace.HOTEL))return false;var b=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);if(p.getX()>b.getX()-16||p.getZ()<b.getZ()-29)return false;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());if(own.getInt("Debt")>=256)return false;own.putInt("Debt",own.getInt("Debt")+1);own.putBoolean("Drank",true);save(d,p.getUUID(),own);p.heal(4);p.getFoodData().eat(2,.2F);return true;}
    public static void talk(ServerPlayer p,HotelActor actor){if(!inside(p,LabyrinthPlace.HOTEL)||p.distanceToSqr(actor)>36)return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());
        if(actor.role()==0){p.displayClientMessage(Component.literal("The desk: Your property is labelled. Crouch at the desk to collect it. Otherwise, read your account."),false);return;}
        if(actor.role()==1){long now=p.serverLevel().getGameTime();if(now-own.getLong("LastDrink")<40&&own.contains("LastDrink"))return;own.putLong("LastDrink",now);give(p,new ItemStack(HotelRegistry.DRINK.get()));save(d,p.getUUID(),own);p.displayClientMessage(Component.literal("The bartender: Complimentary. Drink here; it goes on your tab."),false);return;}
        if(actor.role()==2){var deaths=own.getList("Deaths",Tag.TAG_COMPOUND);if(deaths.isEmpty())p.displayClientMessage(Component.literal("The scarred guest: I have no story of your death. Sit long enough and somebody will offer me one."),false);
            else{var death=deaths.getCompound(deaths.size()-1);p.displayClientMessage(Component.literal("The scarred guest: They say "+death.getString("Name")+" died from "+death.getString("Cause")+". I heard it was a fall from the bandstand. They always change the room."),false);}return;}
        p.displayClientMessage(Component.literal(actor.role()==3?"The pianist: The second place has been kept all season.":"The dancer's hand passes through yours. The music continues."),false);
    }
    public static boolean rest(ServerPlayer p){if(!inside(p,LabyrinthPlace.HOTEL))return false;var b=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);if(p.distanceToSqr(b.offset(HotelRooms.BED).getCenter())>36)return false;var d=LabyrinthData.get(p.server);if(d.state(STATE).getBoolean("Cold")){p.displayClientMessage(Component.literal("The sheets are freezing. Restart the heating plant."),true);return false;}
        var own=personal(d,p.getUUID());if(own.getBoolean("Rested")){var c=new Property(p,"Drawers");int empty=0,occupied=0;for(int i=0;i<54;i++)if(c.getItem(i).isEmpty())empty++;for(var s:p.getInventory().items)if(!s.isEmpty())occupied++;if(empty<occupied){p.displayClientMessage(Component.literal("Empty enough drawer space before resting again."),true);return false;}}
        own.putInt("RestTicks",REST_TICKS);save(d,p.getUUID(),own);p.displayClientMessage(Component.literal(own.getBoolean("Rested")?"Stay beside the bed. Housekeeping will put your main pockets in your private drawers.":"Stay beside the bed. Housekeeping will sort your pockets. This bed does not set your spawn."),false);return true;
    }
    private static void finishRest(ServerPlayer p,CompoundTag own){
        if(own.getBoolean("Rested")){var c=new Property(p,"Drawers");for(int i=0;i<p.getInventory().items.size();i++){var s=p.getInventory().items.get(i);if(s.isEmpty())continue;for(int j=0;j<54;j++)if(c.getItem(j).isEmpty()){c.setItem(j,s.copy());c.persist();p.getInventory().items.set(i,ItemStack.EMPTY);break;}}}
        else{var items=new ArrayList<ItemStack>(p.getInventory().items);items.sort(java.util.Comparator.comparing((ItemStack s)->s.isEmpty()?"~":net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem()).toString()).thenComparing(s->s.getHoverName().getString()));for(int i=0;i<items.size();i++)p.getInventory().items.set(i,items.get(i));}
        p.getInventory().setChanged();var d=LabyrinthData.get(p.server);own=personal(d,p.getUUID());own.putBoolean("Rested",true);own.remove("RestTicks");save(d,p.getUUID(),own);
        var b=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);var candle=p.serverLevel().getBlockState(b.offset(7,6,-23));if(candle.is(Blocks.CANDLE))p.serverLevel().setBlock(b.offset(7,6,-23),candle.setValue(CandleBlock.LIT,false),F);
        var placements=own.getList("Torches",Tag.TAG_COMPOUND);int returned=0;for(var raw:placements){var row=(CompoundTag)raw;var at=BlockPos.of(row.getLong("At"));var s=p.serverLevel().getBlockState(at);if(s.is(Blocks.TORCH)||s.is(Blocks.WALL_TORCH)){p.serverLevel().setBlock(at,Blocks.AIR.defaultBlockState(),F);returned++;}}
        own.remove("Torches");save(d,p.getUUID(),own);if(returned>0)give(p,new ItemStack(Items.TORCH,returned));p.displayClientMessage(Component.literal("The bed is made. Check your pockets and your drawers."),false);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void placed(BlockEvent.EntityPlaceEvent e){if(e.getEntity() instanceof ServerPlayer p&&inside(p,LabyrinthPlace.HOTEL)&&(e.getPlacedBlock().is(Blocks.TORCH)||e.getPlacedBlock().is(Blocks.WALL_TORCH))){var b=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);var rel=e.getPos().subtract(b);if(rel.getY()<5||rel.getY()>8||Math.abs(rel.getX())>9||rel.getZ()<-25||rel.getZ()>-7)return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());var list=own.getList("Torches",Tag.TAG_COMPOUND);var row=new CompoundTag();row.putLong("At",e.getPos().asLong());list.add(row);own.put("Torches",list);save(d,p.getUUID(),own);}}
    public static int pressure(CompoundTag world,long now){return !world.getBoolean("PlantFound")||world.getBoolean("Cold")?0:(int)Math.min(3,Math.max(0,now-world.getLong("VentedAt"))/24000);}
    public static void maintain(ServerPlayer p,boolean restart){if(!inside(p,LabyrinthPlace.HOTEL))return;var b=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);if(p.distanceToSqr(b.offset(restart?HotelRooms.RESTART:HotelRooms.VALVE).getCenter())>36)return;var d=LabyrinthData.get(p.server);var world=d.state(STATE);if(!world.getBoolean("PlantFound"))return;if(world.getBoolean("Cold")&&!restart)return;world.putBoolean("Cold",false);world.putLong("VentedAt",p.serverLevel().getGameTime());d.setState(STATE,world);var own=personal(d,p.getUUID());own.putBoolean("Maintained",true);save(d,p.getUUID(),own);p.serverLevel().playSound(null,b.offset(HotelRooms.BOILER),SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.8F,.55F);p.serverLevel().setBlock(b.offset(HotelRooms.FIRE),Blocks.CAMPFIRE.defaultBlockState(),F);}
    public static void plantTick(ServerLevel l,BlockPos b,long now){var d=LabyrinthData.get(l.getServer());var world=d.state(STATE);if(!world.getBoolean("PlantFound"))return;int pressure=pressure(world,now);var at=b.offset(HotelRooms.GAUGE);var s=l.getBlockState(at);if(s.is(HotelRegistry.PROP.get())&&s.getValue(HotelPropBlock.PRESSURE)!=pressure)l.setBlock(at,s.setValue(HotelPropBlock.PRESSURE,pressure),F);
        if(world.getBoolean("Cold")||now-world.getLong("VentedAt")<BOILER_LIMIT)return;world.putBoolean("Cold",true);world.putInt("Blasts",world.getInt("Blasts")+1);d.setState(STATE,world);var fire=b.offset(HotelRooms.FIRE);if(l.getBlockState(fire).is(Blocks.CAMPFIRE))l.setBlock(fire,l.getBlockState(fire).setValue(CampfireBlock.LIT,false),F);
        var room=new AABB(b.offset(-9,-5,-64),b.offset(10,-1,-49));for(var item:l.getEntitiesOfClass(ItemEntity.class,room))item.setRemainingFireTicks(100);for(var p:l.players())if(room.contains(p.position())&&participant(p)){p.hurt(l.damageSources().hotFloor(),4);p.setRemainingFireTicks(40);}l.playSound(null,b.offset(HotelRooms.BOILER),SoundEvents.GENERIC_EXPLODE.value(),SoundSource.BLOCKS,1,.55F);
    }
    private static void cast(ServerLevel l,BlockPos b){var d=LabyrinthData.get(l.getServer());var all=d.state(STATE);int[][] positions={{4,-4},{-26,-18},{-19,-25},{-6,-25},{20,-32}};for(int i=0;i<5;i++){String key="Actor"+i;if(all.hasUUID(key))continue;var actor=HotelRegistry.ACTOR.get().create(l);if(actor==null)return;actor.appearance(i==4?3:i,false);actor.moveTo(b.getX()+positions[i][0]+.5,b.getY()+(i==4?1:0),b.getZ()+positions[i][1]+.5,0,0);if(l.addFreshEntity(actor)){all.putUUID(key,actor.getUUID());d.setState(STATE,all);}}
        if(!all.hasUUID("Hose")){var hose=HotelRegistry.HOSE.get().create(l);if(hose!=null){hose.moveTo(b.getX()+1.5,b.getY()+5,b.getZ()-5.5,180,0);if(l.addFreshEntity(hose)){all.putUUID("Hose",hose.getUUID());d.setState(STATE,all);}}}
    }
    public static void photograph(ServerPlayer p){if(!inside(p,LabyrinthPlace.HOTEL))return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());if(own.getBoolean("Photographed"))return;var b=IndianLakeRooms.base(p.server,LabyrinthPlace.HOTEL);var rel=p.position().subtract(Vec3.atLowerCornerOf(b));if(rel.x<16||rel.x>25||rel.z<-29||rel.z>-12||rel.y>2)return;own.putBoolean("Photographed",true);var card=new ItemStack(HotelRegistry.PHOTOGRAPH.get());card.set(DataComponents.PROFILE,new ResolvableProfile(p.getGameProfile()));card.set(DataComponents.CUSTOM_NAME,Component.literal("Closing night — "+p.getGameProfile().getName()));reward(p,own,"Photo",card);save(d,p.getUUID(),own);
        var at=b.offset(27,2,-19);p.serverLevel().setBlock(at,Blocks.PLAYER_WALL_HEAD.defaultBlockState().setValue(WallSkullBlock.FACING,Direction.WEST),F);if(p.serverLevel().getBlockEntity(at) instanceof SkullBlockEntity skull)skull.setOwner(new ResolvableProfile(p.getGameProfile()));
        var world=d.state(STATE);if(!world.getBoolean("DancersMade")){for(int i=0;i<6;i++){var actor=HotelRegistry.ACTOR.get().create(p.serverLevel());if(actor!=null){actor.appearance(4+i%2,true);actor.moveTo(b.getX()+17+(i%3)*3,b.getY(),b.getZ()-24+(i/3)*5,i%2==0?90:-90,0);if(p.serverLevel().addFreshEntity(actor))world.putUUID("Dancer"+i,actor.getUUID());}}world.putBoolean("DancersMade",true);d.setState(STATE,world);}HousePackets.send(p,new HotelAtmospherePayload(0,b.offset(HotelRooms.PHOTO),false,8));
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void click(PlayerInteractEvent.RightClickBlock e){if(!(e.getEntity() instanceof ServerPlayer p)||e.getHand()!=InteractionHand.MAIN_HAND)return;var place=current(p);if(place==null||p.distanceToSqr(e.getPos().getCenter())>36)return;var b=IndianLakeRooms.base(p.server,place);var rel=e.getPos().subtract(b);boolean handled=false;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());
        if(place==LabyrinthPlace.HOTEL_GROUNDS){if(rel.equals(HotelRooms.KEY.below())){own.putBoolean("MasterKey",true);reward(p,own,"Key",new ItemStack(HotelRegistry.MASTER_KEY.get()));save(d,p.getUUID(),own);p.displayClientMessage(Component.literal("The master key was warm under the snow."),false);handled=true;}
            else if(e.getLevel().getBlockState(e.getPos()).is(HotelRegistry.PROP.get())&&e.getLevel().getBlockState(e.getPos()).getValue(HotelPropBlock.KIND)==HotelPropBlock.Kind.HEADSTONE){var pages=new ArrayList<String>();for(var raw:own.getList("LostPets",Tag.TAG_COMPOUND))pages.add(((CompoundTag)raw).getString("Name"));if(pages.isEmpty())pages.add("No name of a lost tamed companion has been entered for this guest.");openPaper(p,"Cemetery_"+own.getList("LostPets",Tag.TAG_COMPOUND).size(),HouseWriting.book("Names in the snow","The groundskeeper",HouseWriting.WritingStyle.PLAIN,pages),false);handled=true;}}
        else if(place==LabyrinthPlace.HOTEL){
            if(rel.equals(HotelRooms.DESK)){if(p.isShiftKeyDown())openProperty(p,false);else{boolean ready=ready(own);openPaper(p,ready?"Account":"AccountPreview",HotelTexts.account(p.getGameProfile().getName(),own.getInt("Debt"),ready),ready);}handled=true;}
            else if(rel.equals(HotelRooms.DRAWER)){openProperty(p,true);handled=true;}
            else if(rel.equals(HotelRooms.BED)||rel.equals(HotelRooms.BED.relative(Direction.NORTH))){rest(p);handled=true;}
            else if(rel.equals(HotelRooms.LOG)){openPaper(p,"Log",HotelTexts.log(),false);handled=true;}
            else if(rel.equals(HotelRooms.TYPEWRITER)){openPaper(p,"Manuscript",HotelTexts.manuscript(),false);handled=true;}
            else if(rel.equals(new BlockPos(-6,5,-11))){openPaper(p,"Housekeeping",HotelTexts.housekeeping(),false);handled=true;}
            else if(rel.equals(new BlockPos(-3,0,-2))){openPaper(p,"Rules",HotelTexts.rules(),false);handled=true;}
            else if(rel.equals(new BlockPos(-5,-4,-56))){openPaper(p,"Plant",HotelTexts.boiler(),false);handled=true;}
            else if(rel.equals(new BlockPos(0,0,-39))||rel.equals(new BlockPos(0,1,-39))){if(p.getMainHandItem().is(HotelRegistry.MASTER_KEY.get()))NovelRooms.door(p.serverLevel(),b.offset(0,0,-39),Direction.SOUTH,Blocks.IRON_DOOR,true);else p.displayClientMessage(Component.literal("The service door needs the master key from the maze."),true);handled=true;}
            else if(rel.equals(HotelRooms.VALVE)||rel.equals(HotelRooms.RESTART)){maintain(p,rel.equals(HotelRooms.RESTART));handled=true;}
            else if(rel.equals(HotelRooms.PHOTO)||rel.equals(new BlockPos(27,2,-19))){openPaper(p,"Party",HouseWriting.book("Closing night, front row","The photographer",HouseWriting.WritingStyle.PLAIN,List.of(own.getBoolean("Photographed")?p.getGameProfile().getName()+" is standing in the front row. The print is dry. Nobody remembers the flash.":"An empty space in the front row. The dance floor is polished around it.")),false);handled=true;}
            else if(rel.equals(HotelRooms.MEAL)&&own.getBoolean("MealServed")&&!own.getBoolean("Dinner")){own.putBoolean("Dinner",true);own.putInt("Debt",own.getInt("Debt")+1);save(d,p.getUUID(),own);p.heal(3);p.getFoodData().eat(6,.5F);p.serverLevel().setBlock(b.offset(HotelRooms.MEAL),Blocks.AIR.defaultBlockState(),F);handled=true;}
            else if(rel.equals(HotelRooms.PATCH)){p.serverLevel().setBlock(e.getPos(),Blocks.AIR.defaultBlockState(),F);handled=true;}
        }if(handled){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent e){if(!(e.getEntity().level() instanceof ServerLevel l))return;UUID id=null;String key=null;if(e.getEntity() instanceof ServerPlayer p){id=p.getUUID();key="Deaths";}else if(e.getEntity() instanceof TamableAnimal t&&t.isTame()&&t.getOwnerUUID()!=null&&!t.getTags().contains(MotherOfStrays.PET)){id=t.getOwnerUUID();key="LostPets";}if(id==null)return;var d=LabyrinthData.get(l.getServer());var own=personal(d,id);var list=own.getList(key,Tag.TAG_COMPOUND);var row=new CompoundTag();row.putUUID("Entity",e.getEntity().getUUID());row.putString("Name",e.getEntity().getName().getString());row.putString("Cause",e.getSource().getMsgId());row.putLong("At",l.getGameTime());list.add(row);own.put(key,list);save(d,id,own);}
    private static void garden(ServerPlayer p,BlockPos b){
        var d=LabyrinthData.get(p.server);var world=d.state(STATE);var l=p.serverLevel();
        int[][] form={{0,0,0},{1,0,0},{2,0,0},{0,-1,0},{2,-1,0},{2,1,0},{3,1,0}};
        for(int group=0;group<4;group++)for(int part=0;part<form.length;part++){
            String key="Topiary"+group+"_"+part;if(world.hasUUID(key))continue;var display=EntityType.BLOCK_DISPLAY.create(l);if(display==null)continue;
            var tag=new CompoundTag();display.saveWithoutId(tag);tag.put("block_state",NbtUtils.writeBlockState(Blocks.OAK_LEAVES.defaultBlockState()));display.load(tag);
            display.moveTo(b.getX()+17+(group%2)*5+form[part][0],b.getY()+1+form[part][1],b.getZ()-12-(group/2)*10);display.addTag("HotelTopiary");
            if(l.addFreshEntity(display)){world.putUUID(key,display.getUUID());d.setState(STATE,world);}
        }
        if(l.getGameTime()%80==0)for(int group=0;group<4;group++){
            var pieces=new ArrayList<Entity>();for(int part=0;part<form.length;part++){String key="Topiary"+group+"_"+part;if(world.hasUUID(key)&&l.getEntity(world.getUUID(key))!=null)pieces.add(l.getEntity(world.getUUID(key)));}
            boolean seen=l.players().stream().filter(Player::isAlive).anyMatch(w->pieces.stream().anyMatch(a->HouseWatchers.sees(w,a.position().add(.5,.5,.5))));
            if(!seen){boolean turn=!world.getBoolean("TopiaryPose"+group);for(int part=0;part<pieces.size();part++){var a=pieces.get(part);double x=b.getX()+17+(group%2)*5+(turn?3-form[part][0]:form[part][0]);a.moveTo(x,b.getY()+1+form[part][1],b.getZ()-12-(group/2)*10+(turn?1:0));}world.putBoolean("TopiaryPose"+group,turn);d.setState(STATE,world);}
        }
        var own=personal(d,p.getUUID());int plot=world.getInt("Graves");
        for(var raw:own.getList("LostPets",Tag.TAG_COMPOUND)){
            var pet=(CompoundTag)raw;String key="Grave_"+pet.getUUID("Entity");if(world.getBoolean(key))continue;
            int x=-24+(plot%13)*4,z=-59-(plot/13)*3;var at=b.offset(x,0,z);
            l.setBlock(at.below(),Blocks.GRAVEL.defaultBlockState(),F);l.setBlock(at,HotelRegistry.PROP.get().defaultBlockState().setValue(HotelPropBlock.KIND,HotelPropBlock.Kind.HEADSTONE),F);
            var name=EntityType.TEXT_DISPLAY.create(l);if(name!=null){var tag=new CompoundTag();name.saveWithoutId(tag);tag.putString("text",Component.Serializer.toJson(Component.literal(pet.getString("Name")),l.registryAccess()));tag.putString("billboard","center");tag.putInt("background",0);tag.putInt("line_width",90);name.load(tag);name.moveTo(at.getX()+.5,at.getY()+1.8,at.getZ()+.5);name.addTag("HotelGrave");if(l.addFreshEntity(name))world.putUUID(key+"Label",name.getUUID());}
            world.putBoolean(key,true);world.putInt("Graves",++plot);d.setState(STATE,world);
        }
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();if(origin==null)return;var l=server.getLevel(HouseDimensions.INTERIOR);if(l==null)return;var b=LabyrinthPlaces.base(origin,LabyrinthPlace.HOTEL);var d=LabyrinthData.get(server);
        for(var p:server.getPlayerList().getPlayers()){var place=current(p);var own=personal(d,p.getUUID());if(place==null){if(!own.getString("Here").isEmpty()){own.remove("Here");own.remove("RestTicks");save(d,p.getUUID(),own);}continue;}if(!place.id().equals(own.getString("Here"))){onArrive(p,place);own=personal(d,p.getUUID());}
            if(place==LabyrinthPlace.HOTEL_GROUNDS){var gb=IndianLakeRooms.base(server,place);IndianLakeRooms.keepLoaded(p.serverLevel(),gb,place);HousePackets.send(p,new HotelAtmospherePayload(2,gb,false,0));garden(p,gb);continue;}if(place!=LabyrinthPlace.HOTEL)continue;
            IndianLakeRooms.keepLoaded(l,b,place);cast(l,b);var rel=p.position().subtract(Vec3.atLowerCornerOf(b));
            if(p.isPassenger()&&Math.abs(rel.x)<1.5&&rel.z>-20&&rel.z<-12&&!own.getBoolean("Dinner")){own.putBoolean("MealServed",true);l.setBlock(b.offset(HotelRooms.MEAL),HotelRegistry.PROP.get().defaultBlockState().setValue(HotelPropBlock.KIND,HotelPropBlock.Kind.MEAL),F);}
            if(own.getInt("RestTicks")>0){if(p.distanceToSqr(b.offset(HotelRooms.BED).getCenter())>36||d.state(STATE).getBoolean("Cold"))own.remove("RestTicks");else{own.putInt("RestTicks",own.getInt("RestTicks")-1);save(d,p.getUUID(),own);if(own.getInt("RestTicks")==0){finishRest(p,own);own=personal(d,p.getUUID());}}}
            if(rel.y<-2&&rel.z<-48){var world=d.state(STATE);if(!world.getBoolean("PlantFound")){world.putBoolean("PlantFound",true);world.putLong("VentedAt",l.getGameTime());d.setState(STATE,world);}}
            var plaque=b.offset(2,6,-5);if(rel.y>4&&HouseWatchers.sees(p,plaque.getCenter()))own.putBoolean("Saw217",true);
            if(own.getBoolean("Saw217")&&!d.state(STATE).getBoolean("NumberTurned")&&l.players().stream().filter(Player::isAlive).noneMatch(w->HouseWatchers.sees(w,plaque.getCenter()))){NovelRooms.sign(l,plaque,Direction.SOUTH,new String[]{"237","","",""});var world=d.state(STATE);world.putBoolean("NumberTurned",true);d.setState(STATE,world);}
            save(d,p.getUUID(),own);photograph(p);
            if(server.getTickCount()%80==0){p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(HotelRegistry.ORCHESTRA,SoundSource.RECORDS,b.getX()-6.5,b.getY()+1,b.getZ()-24.5,.4F,1,p.getRandom().nextLong()));p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(HotelRegistry.PIANO,SoundSource.RECORDS,b.getX()+20.5,b.getY()+2,b.getZ()-31.5,.45F,1,p.getRandom().nextLong()));}
        }if(server.getTickCount()%20==0)plantTick(l,b,l.getGameTime());
    }
}
