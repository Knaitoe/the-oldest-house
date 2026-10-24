package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.SeanceViewPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Native shared rooms, finite personal originals, present time and personally examined endings. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ClassicsVignettes {
    public static final String STATE="classics_0434",OWNER="ClassicReader",KEY="ClassicPage";
    public static final int SITTING_TICKS=720,NIGHT_PERIOD=320;
    /** The shared family moves once per server tick however many readers are in the room. */
    private static int seanceTick=-1;
    /** Each of them stands at the table in front of their own chair, hands on the wood. Rows are x,z block centres. */
    private static final double[][] STANDS={{.5,-13.5},{-1.5,-14.5},{1.5,-14.5},{.5,-16.5}};
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private ClassicsVignettes(){}
    public static boolean isClassic(LabyrinthPlace place){return place==LabyrinthPlace.SEANCE||place==LabyrinthPlace.WALLPAPER_NURSERY;}
    public static CompoundTag personal(LabyrinthData data,UUID reader){return data.stateEntry(STATE,reader.toString());}
    public static void save(LabyrinthData data,UUID reader,CompoundTag own){var all=data.state(STATE);all.put(reader.toString(),own.copy());data.setState(STATE,all);}
    private static boolean participant(ServerPlayer p){return p.isAlive()&&p.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR&&!FinaleProgress.terminal(FinaleProgress.phase(p.server,p.getUUID()));}
    public static @Nullable BlockPos base(ServerPlayer p,LabyrinthPlace place){var origin=HouseSavedData.get(p.server).houseOrigin();return origin==null?null:LabyrinthPlaces.base(origin,place);}
    public static boolean inside(ServerPlayer p,LabyrinthPlace place){var b=base(p,place);return participant(p)&&p.level().dimension().equals(HouseDimensions.INTERIOR)&&b!=null&&IndianLakeRooms.bounds(b,place).contains(p.position());}
    public static @Nullable LabyrinthPlace current(ServerPlayer p){for(var place:List.of(LabyrinthPlace.SEANCE,LabyrinthPlace.WALLPAPER_NURSERY))if(inside(p,place))return place;return null;}
    public static void onArrive(ServerPlayer p,LabyrinthPlace place){
        if(!isClassic(place)||!participant(p))return;var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());
        own.putString("Here",place.id());own.remove("SeanceViewUntil");
        if(place==LabyrinthPlace.WALLPAPER_NURSERY){own.putInt("NurseryLimit",Math.min(4,Integer.bitCount(own.getInt("Found"))+1));own.putInt("NurseryVisits",own.getInt("NurseryVisits")+1);}
        save(data,p.getUUID(),own);
    }
    public static void ensureCast(ServerLevel level,BlockPos base){
        var data=LabyrinthData.get(level.getServer());var all=data.state(STATE);
        if(all.getBoolean("CastMade"))return;
        float[] facing={180,-90,90,0};
        for(int role=0;role<4;role++){
            if(all.hasUUID("Actor"+role))continue;
            var actor=ClassicsRegistry.ACTOR.get().create(level);if(actor==null)return;actor.appearance(role,false,false);
            actor.moveTo(base.getX()+STANDS[role][0],base.getY(),base.getZ()+STANDS[role][1],facing[role],0);actor.setYHeadRot(facing[role]);actor.yBodyRot=facing[role];
            if(!level.addFreshEntity(actor))return;all.putUUID("Actor"+role,actor.getUUID());data.setState(STATE,all);
        }
        all.putBoolean("CastMade",true);data.setState(STATE,all);
    }
    public static List<SeanceActor> cast(ServerLevel level){var all=LabyrinthData.get(level.getServer()).state(STATE);var cast=new ArrayList<SeanceActor>();for(int i=0;i<4;i++)if(all.hasUUID("Actor"+i)&&level.getEntity(all.getUUID("Actor"+i)) instanceof SeanceActor actor)cast.add(actor);return cast;}
    private static void say(ServerPlayer p,int role,String words){cast(p.serverLevel()).stream().filter(a->a.role()==role).findFirst().ifPresent(a->a.say(words));}
    private static void give(ServerPlayer p,ItemStack stack){if(p.getInventory().add(stack))return;var drop=new ItemEntity(p.serverLevel(),p.getX(),p.getY()+.2,p.getZ(),stack);drop.setTarget(p.getUUID());p.serverLevel().addFreshEntity(drop);}
    private static ItemStack original(ServerPlayer p,ItemStack book,String key,LabyrinthPlace place){var marked=VignetteYields.mark(book,place.id());CustomData.update(DataComponents.CUSTOM_DATA,marked,t->{t.putUUID(OWNER,p.getUUID());t.putString(KEY,key);});return marked;}
    private static ItemStack snapshot(ServerPlayer p,CompoundTag own,String key,ItemStack book,LabyrinthPlace place){String slot="Original_"+key;if(!own.contains(slot))own.put(slot,original(p,book,key,place).save(p.registryAccess()));return ItemStack.parseOptional(p.registryAccess(),own.getCompound(slot));}
    private static void open(ServerPlayer p,LabyrinthPlace place,String key,ItemStack book,boolean ending){var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());var saved=snapshot(p,own,key,book,place);save(data,p.getUUID(),own);p.openMenu(new SimpleMenuProvider((id,inv,who)->new PageMenu(id,p,place,saved,key,ending,true,true),saved.getHoverName()));}
    public static void readCollected(ServerPlayer p,ItemStack stack){var custom=stack.get(DataComponents.CUSTOM_DATA);if(custom==null||!stack.has(DataComponents.WRITTEN_BOOK_CONTENT))return;var tag=custom.copyTag();String key=tag.getString(KEY);var place=key.startsWith("Wallpaper")||key.equals("Folio")?LabyrinthPlace.WALLPAPER_NURSERY:LabyrinthPlace.SEANCE;
        boolean own=tag.hasUUID(OWNER)&&tag.getUUID(OWNER).equals(p.getUUID());p.openMenu(new SimpleMenuProvider((id,inv,who)->new PageMenu(id,p,place,stack.copy(),key,key.equals("Album")||key.equals("Wallpaper3")||key.equals("Folio"),own,false),stack.getHoverName()));}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void held(PlayerInteractEvent.RightClickItem event){if(!(event.getEntity() instanceof ServerPlayer p))return;var item=p.getItemInHand(event.getHand());if(item.has(DataComponents.WRITTEN_BOOK_CONTENT)&&item.has(DataComponents.CUSTOM_DATA)&&item.get(DataComponents.CUSTOM_DATA).copyTag().contains(KEY)){readCollected(p,item);event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);}}
    public static final class PageMenu extends LecternMenu {
        private final ServerPlayer reader;private final LabyrinthPlace place;private final ItemStack original;private final String key;private final boolean ending,owned,surface;
        PageMenu(int id,ServerPlayer reader,LabyrinthPlace place,ItemStack original,String key,boolean ending,boolean owned,boolean surface){super(id,container(original),new SimpleContainerData(1));this.reader=reader;this.place=place;this.original=original.copy();this.key=key;this.ending=ending;this.owned=owned;this.surface=surface;}
        private static Container container(ItemStack original){var c=new SimpleContainer(1);c.setItem(0,original.copy());return c;}
        public ItemStack book(){return original.copy();}
        @Override public boolean clickMenuButton(Player player,int button){
            if(player!=reader||!participant(reader)||(surface&&!inside(reader,place)))return false;
            var data=LabyrinthData.get(reader.server);var own=personal(data,reader.getUUID());
            if(button==3){if(!surface||!owned||own.getBoolean("Taken_"+key))return false;own.putBoolean("Taken_"+key,true);save(data,reader.getUUID(),own);give(reader,original.copy());return true;}
            if(!super.clickMenuButton(player,button))return false;int count=original.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();
            if(owned&&getPage()==count-1){own.putBoolean("Read_"+key,true);save(data,reader.getUUID(),own);
                if(ending&&inside(reader,place)){
                    if(place==LabyrinthPlace.SEANCE&&seanceEvidence(data,own)){String outcome=own.getBoolean("SeanceFinished")?own.getString("SeanceOutcome"):"examined_the_empty_sitting";WitnessAccount.resolve(reader,WitnessAccount.Story.SEANCE,outcome);data.setCompleted(place.id(),true);reward(reader,own,"Planchette",original(reader,new ItemStack(ClassicsRegistry.PLANCHETTE.get()),"Planchette",place));}
                    if(place==LabyrinthPlace.WALLPAPER_NURSERY&&own.getInt("Found")==15){WitnessAccount.resolve(reader,WitnessAccount.Story.WALLPAPER,"kept_the_hidden_pages");data.setCompleted(place.id(),true);reward(reader,own,"Folio",original(reader,ClassicsTexts.folio(),"Folio",place));}
                    save(data,reader.getUUID(),own);
                }
            }return true;
        }
    }
    private static void reward(ServerPlayer p,CompoundTag own,String key,ItemStack stack){if(own.getBoolean("Yield_"+key))return;own.putBoolean("Yield_"+key,true);give(p,stack);}
    private static boolean seanceEvidence(LabyrinthData data,CompoundTag own){return own.getBoolean("SeanceFinished")||(data.state(STATE).getBoolean("SittingEnded")&&own.getInt("Exposure")>=100&&own.getBoolean("Read_Grace")&&own.getBoolean("Read_Sitting"));}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void click(PlayerInteractEvent.RightClickBlock event){
        if(!(event.getEntity() instanceof ServerPlayer p)||event.getHand()!=InteractionHand.MAIN_HAND)return;var place=current(p);if(place==null||p.distanceToSqr(event.getPos().getCenter())>36)return;var b=base(p,place);var rel=event.getPos().subtract(b);boolean handled=false;
        if(place==LabyrinthPlace.WALLPAPER_NURSERY){int index=ClassicsRooms.PANELS.indexOf(rel);var panel=p.serverLevel().getBlockState(event.getPos());if(index>=0&&panel.is(ClassicsRegistry.WALLPAPER.get())){if(p.getMainHandItem().getItem() instanceof AxeItem){peel(p,index);handled=true;}else if(panel.getValue(WallpaperPanelBlock.PEELED)){readPanel(p,index);handled=true;}}}
        else if(rel.equals(ClassicsRooms.GRACE)){open(p,place,"Grace",ClassicsTexts.grace(),false);handled=true;}
        else if(rel.equals(ClassicsRooms.SITTING)){open(p,place,"Sitting",ClassicsTexts.medium(),false);handled=true;}
        else if(rel.equals(ClassicsRooms.ALBUM)){open(p,place,"Album",ClassicsTexts.albumRead(),true);handled=true;}
        else if(rel.equals(ClassicsRooms.CUPBOARD)){var cupboard=p.serverLevel().getBlockState(event.getPos());if(cupboard.is(ClassicsRegistry.FITTING.get())){boolean open=!cupboard.getValue(SeanceFittingBlock.OPEN);p.serverLevel().setBlock(event.getPos(),cupboard.setValue(SeanceFittingBlock.OPEN,open),F);p.serverLevel().playSound(null,event.getPos(),open?SoundEvents.WOODEN_DOOR_OPEN:SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.6F,1.3F);}disturb(p,0);handled=true;}
        else if(ClassicsRooms.CANDLES.contains(rel)&&p.getMainHandItem().isEmpty()){int index=ClassicsRooms.CANDLES.indexOf(rel);var state=p.serverLevel().getBlockState(event.getPos());if(state.is(Blocks.CANDLE)&&state.getValue(CandleBlock.LIT)){p.serverLevel().setBlock(event.getPos(),state.setValue(CandleBlock.LIT,false),F);p.serverLevel().playSound(null,event.getPos(),SoundEvents.CANDLE_EXTINGUISH,SoundSource.BLOCKS,.6F,1);disturb(p,index+1);}handled=true;}
        else if(rel.distManhattan(ClassicsRooms.GATE)<=1){handled=true;if(wingReady(p.serverLevel(),b))NovelRooms.door(p.serverLevel(),b.offset(ClassicsRooms.GATE),Direction.SOUTH,Blocks.IRON_DOOR,true);else say(p,1,"Shut the other door. And the shutters. All of them.");}
        if(handled){event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);}
    }
    public static boolean wingReady(ServerLevel l,BlockPos b){var door=l.getBlockState(b.offset(-5,0,-22));if(door.getBlock() instanceof DoorBlock&&door.getValue(DoorBlock.OPEN))return false;var entry=l.getBlockState(b.offset(0,0,1));if(entry.getBlock() instanceof DoorBlock&&entry.getValue(DoorBlock.OPEN))return false;for(var pos:ClassicsRooms.SHUTTERS){var s=l.getBlockState(b.offset(pos));if(!(s.getBlock() instanceof TrapDoorBlock)||!s.getValue(TrapDoorBlock.OPEN))return false;}return true;}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void strip(PlayerInteractEvent.LeftClickBlock event){if(!(event.getEntity() instanceof ServerPlayer p)||event.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START||!inside(p,LabyrinthPlace.WALLPAPER_NURSERY)||p.distanceToSqr(event.getPos().getCenter())>36)return;int index=ClassicsRooms.PANELS.indexOf(event.getPos().subtract(base(p,LabyrinthPlace.WALLPAPER_NURSERY)));if(index<0||!(p.getMainHandItem().getItem() instanceof AxeItem))return;event.setCanceled(true);peel(p,index);}
    public static boolean peel(ServerPlayer p,int index){
        if(!inside(p,LabyrinthPlace.WALLPAPER_NURSERY)||index<0||index>3)return false;var b=base(p,LabyrinthPlace.WALLPAPER_NURSERY);var at=b.offset(ClassicsRooms.PANELS.get(index));if(p.distanceToSqr(at.getCenter())>36||!(p.getMainHandItem().getItem() instanceof AxeItem))return false;
        var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());if((own.getInt("Seen")&(1<<index))==0||index>=own.getInt("NurseryLimit")){p.displayClientMessage(Component.literal("Only old paste under this seam."),true);return false;}
        var state=p.serverLevel().getBlockState(at);if(!state.is(ClassicsRegistry.WALLPAPER.get()))return false;
        if(!state.getValue(WallpaperPanelBlock.PEELED)){p.serverLevel().setBlock(at,state.setValue(WallpaperPanelBlock.PEELED,true),F);p.getMainHandItem().hurtAndBreak(1,p,EquipmentSlot.MAINHAND);p.serverLevel().playSound(null,at,SoundEvents.WOOL_BREAK,SoundSource.BLOCKS,.65F,.55F);}
        readPanel(p,index);return true;
    }
    private static void readPanel(ServerPlayer p,int index){var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());if(index>=own.getInt("NurseryLimit")||(own.getInt("Seen")&(1<<index))==0)return;own.putInt("Found",own.getInt("Found")|(1<<index));save(data,p.getUUID(),own);open(p,LabyrinthPlace.WALLPAPER_NURSERY,"Wallpaper"+index,ClassicsTexts.wallpaper(index),index==3);}
    private static void disturb(ServerPlayer p,int bit){if(!inside(p,LabyrinthPlace.SEANCE))return;var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());var all=data.state(STATE);if(all.getBoolean("SittingEnded"))return;int mask=own.getInt("Disturbances");if((mask&(1<<bit))!=0)return;own.putInt("Disturbances",mask|(1<<bit));save(data,p.getUUID(),own);
        cast(p.serverLevel()).forEach(a->a.appearance(a.role(),false,true));say(p,Integer.bitCount(mask)%3+1,bit==0?"The cupboard. There was a hand on it.":"That candle was burning. I watched you light it.");
        if(Integer.bitCount(mask|(1<<bit))>=3)finish(p,"drove_the_family_out");
    }
    public static void attacked(ServerPlayer p,SeanceActor actor){if(!inside(p,LabyrinthPlace.SEANCE))return;var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());own.putBoolean("Interrupted",true);own.putInt("ListenTicks",0);save(data,p.getUUID(),own);actor.appearance(actor.role(),false,true);actor.say("Something touched me. Get the children out.");if(!data.state(STATE).getBoolean("SittingEnded"))finish(p,"drove_the_family_out");}
    private static void finish(ServerPlayer p,String outcome){var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());own.putBoolean("SeanceFinished",true);own.putString("SeanceOutcome",outcome);own.remove("SeanceViewUntil");save(data,p.getUUID(),own);var all=data.state(STATE);all.putBoolean("SittingEnded",true);all.putString("SharedOutcome",outcome);data.setState(STATE,all);
        if(outcome.equals("drove_the_family_out")){var b=base(p,LabyrinthPlace.SEANCE);NovelRooms.door(p.serverLevel(),b.offset(-5,0,-22),Direction.SOUTH,Blocks.DARK_OAK_DOOR,true);startFlight(p.serverLevel(),b,data);}
        else say(p,0,p.getGameProfile().getName()+". I know you are here. This house is occupied. Please go.");
        HousePackets.send(p,new SeanceViewPayload(-1,0,false));
    }
    // ------------------------------------------------------------------------------------------------ 0.4.77: the family leaves
    /** Each one's way from their place at the table, round it, through the west door and into the back room. Rows are x,z. */
    private static final double[][][] FLIGHT={
        {{.5,-13.5},{-4.5,-13.5},{-4.5,-19.5},{-4.5,-21.5},{-4.5,-23.5},{-8.5,-29.5}},
        {{-1.5,-14.5},{-1.5,-13.5},{-4.5,-13.5},{-4.5,-19.5},{-4.5,-21.5},{-4.5,-23.5},{-3.5,-29.5}},
        {{1.5,-14.5},{1.5,-13.5},{-4.5,-13.5},{-4.5,-19.5},{-4.5,-21.5},{-4.5,-23.5},{-9.5,-24.5}},
        {{.5,-16.5},{-1.5,-16.5},{-1.5,-17.5},{-4.5,-19.5},{-4.5,-20.5},{-4.5,-21.5},{-4.5,-23.5},{-2.5,-28.5}}};
    private static final String FLEE="Flee0477";
    private static int flightLeg(SeanceActor a,BlockPos b){
        var route=FLIGHT[a.role()];int nearest=1;double distance=Double.MAX_VALUE;
        for(int n=1;n<route.length;n++){double d=a.position().distanceToSqr(new Vec3(b.getX()+route[n][0],a.getY(),b.getZ()+route[n][1]));if(d<distance){nearest=n;distance=d;}}
        return nearest;
    }
    private static void startFlight(ServerLevel level,BlockPos b,LabyrinthData data){
        var all=data.state(STATE);all.putBoolean("Fleeing0477",true);data.setState(STATE,all);
        for(var a:cast(level)){a.appearance(a.role(),false,true);a.setNoAi(true);a.setNoGravity(true);a.getNavigation().stop();a.getPersistentData().putInt(FLEE,flightLeg(a,b));a.getPersistentData().putBoolean("FlightRoute0492",true);}
    }
    /**
     * Driven out, they get up and hurry, one behind another, through the west door into the back room, and the last one in
     * shuts it behind them, never on anyone standing in it. Saved actors keep their place on the way across a reload; a
     * sitting that ended in fear before 0.4.77, with the family still at the table, sets off now.
     */
    private static void flee(ServerLevel level,BlockPos b){
        var data=LabyrinthData.get(level.getServer());var all=data.state(STATE);
        if(all.getBoolean("Fled0477")||!all.getBoolean("SittingEnded")||!"drove_the_family_out".equals(all.getString("SharedOutcome")))return;
        if(!all.getBoolean("Fleeing0477")){startFlight(level,b,data);all=data.state(STATE);}
        boolean done=true;
        for(var a:cast(level)){
            // Older routes began by lowering feet into a seat or steering through its back. Resume the same body.
            if(!a.getPersistentData().getBoolean("FlightRoute0492")){
                a.getPersistentData().putInt(FLEE,flightLeg(a,b));a.getPersistentData().putBoolean("FlightRoute0492",true);a.setNoGravity(true);
            }
            var route=FLIGHT[a.role()];int i=a.getPersistentData().getInt(FLEE);if(i>=route.length)continue;done=false;
            double y=b.getY();
            Vec3 to=new Vec3(b.getX()+route[i][0],y,b.getZ()+route[i][1]),d=to.subtract(a.position());double len=d.horizontalDistance();
            Vec3 next=a.position().add(len<.001?0:d.x/len*Math.min(.16,len),Math.clamp(to.y-a.getY(),-.08,.12),len<.001?0:d.z/len*Math.min(.16,len));
            float yaw=len>.01?(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90:a.getYRot();a.move(net.minecraft.world.entity.MoverType.SELF,next.subtract(a.position()));a.setDeltaMovement(Vec3.ZERO);a.setYRot(yaw);a.setYHeadRot(yaw);a.yBodyRot=yaw;
            if(a.position().distanceToSqr(to)<.0025)a.getPersistentData().putInt(FLEE,i+1);
        }
        if(!done)return;
        var door=b.offset(-5,0,-22);
        if(!level.getEntitiesOfClass(LivingEntity.class,new AABB(door).expandTowards(0,1,0).inflate(.1),e->e.isAlive()).isEmpty())return;
        NovelRooms.door(level,door,Direction.SOUTH,Blocks.DARK_OAK_DOOR,false);level.playSound(null,door,SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,1,.8F);
        cast(level).stream().filter(a->a.role()==2).findFirst().ifPresent(a->a.say("It walks through doors. It doesn't need them. Keep this one shut anyway."));
        cast(level).forEach(a->a.setNoGravity(false));all=data.state(STATE);all.putBoolean("Fled0477",true);all.remove("Fleeing0477");data.setState(STATE,all);
    }
    // ------------------------------------------------------------------------------------------------ 0.4.77: what a ghost can do
    /**
     * Once the sitting is over, a reader who leans into the shut iron door for a moment finds it is not there for them, and is
     * on the other side. Either way through; the door stays shut and locked to the living.
     */
    private static void lean(ServerPlayer p,BlockPos b,CompoundTag own,CompoundTag all){
        var gate=p.serverLevel().getBlockState(b.offset(ClassicsRooms.GATE));
        if(!all.getBoolean("SittingEnded")||!(gate.getBlock() instanceof DoorBlock)||gate.getValue(DoorBlock.OPEN)){own.remove("Lean0477");return;}
        var rel=p.position().subtract(b.getX(),b.getY(),b.getZ());var look=p.getLookAngle();
        boolean column=rel.x>ClassicsRooms.GATE.getX()+.05&&rel.x<ClassicsRooms.GATE.getX()+.95&&rel.y>-.5&&rel.y<1;
        int side=column&&rel.z>-21.75&&rel.z<-20.8&&look.z< -.6?1:column&&rel.z< -22.2&&rel.z>-23.2&&look.z>.6?-1:0;
        if(side==0){own.remove("Lean0477");return;}
        int lean=own.getInt("Lean0477")+1;own.putInt("Lean0477",lean);
        if(lean==10&&!own.getBoolean("GhostHint0477")){own.putBoolean("GhostHint0477",true);p.displayClientMessage(Component.literal("The iron is cold. Then it is hardly there."),true);}
        if(lean<30)return;
        own.remove("Lean0477");own.putBoolean("WalkedThrough0477",true);
        var at=b.offset(ClassicsRooms.GATE);p.serverLevel().playSound(null,at,SoundEvents.CANDLE_EXTINGUISH,SoundSource.PLAYERS,.9F,.5F);
        p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,at.getX()+.5,at.getY()+1,at.getZ()+.1,12,.3,.6,.05,.01);
        p.connection.teleport(b.getX()+ClassicsRooms.GATE.getX()+.5,p.getY(),b.getZ()+(side>0?-23.1:-20.6),p.getYRot(),p.getXRot());p.setDeltaMovement(Vec3.ZERO);
    }
    /** Each of them feels a touch differently. It unsettles the sitting but counts for nothing by itself. */
    private static final String[][] TOUCH={
        {"Cold. A hand on my shoulder. Sit still, all of you.","Don't take hold of me. Stay where we can speak to you.","It wants to be noticed. It has been noticed."},
        {"Who touched me? Who's there?","There was a hand in my hair. Please. Not the children.","Leave them alone. Whatever you are, leave them alone."},
        {"Nobody move. Something brushed past me.","I felt it. I won't pretend I didn't.","That's enough. Whoever you are, that's enough."},
        {"Something's holding my hand.","It's cold. Mummy, it's so cold.","Is it the lady from upstairs? She's sad."}};
    @SubscribeEvent(priority=EventPriority.HIGH) public static void touch(PlayerInteractEvent.EntityInteract e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getTarget() instanceof SeanceActor a))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(e.getHand()!=InteractionHand.MAIN_HAND||!inside(p,LabyrinthPlace.SEANCE)||a.distanceToSqr(p)>12)return;
        int n=a.getPersistentData().getInt("Touched0477");a.getPersistentData().putInt("Touched0477",n+1);
        a.say(TOUCH[a.role()][n%TOUCH[a.role()].length]);a.appearance(a.role(),a.seated(),true);a.getPersistentData().putLong("Flinch0477",a.level().getGameTime()+60);
        a.level().playSound(null,a.blockPosition(),SoundEvents.PLAYER_HURT_FREEZE,SoundSource.NEUTRAL,.35F,1.3F+a.role()*.08F);
        var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());own.putInt("ListenTicks",0);own.putInt("TouchedFamily0491",own.getInt("TouchedFamily0491")|(1<<a.role()));save(data,p.getUUID(),own);
        if(Integer.bitCount(own.getInt("TouchedFamily0491"))>=3&&!data.state(STATE).getBoolean("SittingEnded"))finish(p,"drove_the_family_out");
    }
    /** A flinch passes while the sitting goes on. */
    private static void settle(ServerLevel level,CompoundTag all){
        if(all.getBoolean("SittingEnded"))return;
        for(var a:cast(level))if(a.getPersistentData().contains("Flinch0477")&&level.getGameTime()>=a.getPersistentData().getLong("Flinch0477")){a.getPersistentData().remove("Flinch0477");a.appearance(a.role(),a.seated(),false);}
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        var server=event.getServer();var origin=HouseSavedData.get(server).houseOrigin();var level=server.getLevel(HouseDimensions.INTERIOR);if(origin==null||level==null)return;
        var visitors=new ArrayList<ServerPlayer>();for(var p:server.getPlayerList().getPlayers()){var place=current(p);var data=LabyrinthData.get(server);var own=personal(data,p.getUUID());
            if(place==null){if(!own.getString("Here").isEmpty()){own.remove("Here");own.remove("SeanceViewUntil");save(data,p.getUUID(),own);HousePackets.send(p,new SeanceViewPayload(-1,0,false));}continue;}
            if(!place.id().equals(own.getString("Here"))){onArrive(p,place);own=personal(data,p.getUUID());}
            var b=base(p,place);IndianLakeRooms.keepLoaded(level,b,place);
            if(place==LabyrinthPlace.SEANCE){ensureCast(level,b);var all=data.state(STATE);if(seanceTick!=server.getTickCount()){seanceTick=server.getTickCount();flee(level,b);settle(level,all);all=data.state(STATE);}lean(p,b,own,all);boolean watching=HouseWatchers.sees(p,b.offset(0,1,-15).getCenter());if(watching)own.putInt("Exposure",Math.min(1000,own.getInt("Exposure")+1));
                if(!all.getBoolean("SittingEnded")&&own.getInt("Disturbances")==0&&!own.getBoolean("Interrupted")&&watching&&p.getZ()<b.getZ()-9){int ticks=own.getInt("ListenTicks")+1;own.putInt("ListenTicks",ticks);save(data,p.getUUID(),own);
                    if(ticks==100)say(p,1,"We heard the stairs again. There is nobody in the hall.");if(ticks==260)say(p,3,"Someone is standing behind the empty chair.");if(ticks==440)say(p,2,"Do not frighten your mother. There is no one there.");
                    if(ticks==600){var medium=cast(level).stream().filter(a->a.role()==0).findFirst().orElse(null);if(medium!=null){own.putLong("SeanceViewUntil",level.getGameTime()+40);HousePackets.send(p,new SeanceViewPayload(medium.getId(),40,true));}}
                    if(ticks>=SITTING_TICKS){save(data,p.getUUID(),own);finish(p,"heard_the_medium");own=personal(data,p.getUUID());}}
            }else visitors.add(p);
            save(data,p.getUUID(),own);
        }
        if(!visitors.isEmpty()){
            var b=LabyrinthPlaces.base(origin,LabyrinthPlace.WALLPAPER_NURSERY);int phase=(int)(level.getGameTime()%NIGHT_PERIOD),index=(phase/60)%4;boolean night=phase<240;
            for(int i=0;i<4;i++){var at=b.offset(ClassicsRooms.PANELS.get(i));var state=level.getBlockState(at);if(!state.is(ClassicsRegistry.WALLPAPER.get()))continue;boolean shown=night&&i==index;if(state.getValue(WallpaperPanelBlock.FIGURE)!=shown)level.setBlock(at,state.setValue(WallpaperPanelBlock.FIGURE,shown),F);
                if(shown)for(var p:visitors){if(p.distanceToSqr(at.getCenter())>100||!HouseWatchers.sees(p,at.getCenter()))continue;var data=LabyrinthData.get(server);var own=personal(data,p.getUUID());if(i>=own.getInt("NurseryLimit"))continue;int mask=own.getInt("Seen");if((mask&(1<<i))==0){own.putInt("Seen",mask|(1<<i));save(data,p.getUUID(),own);}}
            }
        }
        if(server.getTickCount()%20==0){var expired=new ArrayList<Entity>();for(var e:level.getAllEntities())if(e.getTags().contains("SeanceSpeech")&&e.getPersistentData().getLong("Until")<level.getGameTime())expired.add(e);expired.forEach(Entity::discard);}
    }
}
