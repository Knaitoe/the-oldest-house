package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import com.mojang.math.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import static io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSites.Room.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.HouseReadPropBlock.Part.*;

/** Literal local acts and physical sequences only. No personality categories or later-room effects. */
public final class HouseReadMoments {
    private static final String LIFT="house_read_routine139";
    private static final Map<UUID,Long> LAST_MUSIC=new HashMap<>();
    public static final String[] ROUTINES={"silk headwrap","curl cream","wide-tooth pick","scalp oil","parting comb","hair ties","paddle brush","dry shampoo","claw clip","pocket comb","pomade tin","cotton washcloth"};
    private HouseReadMoments(){}
    public static BlockPos exit(HouseReadSites.Room room){return switch(room){case WAITING->new BlockPos(0,0,-14);case BATHROOM->new BlockPos(3,0,-8);case SHELTER->new BlockPos(3,0,2);default->throw new IllegalArgumentException(room.name());};}
    public static boolean canProceed(ServerPlayer p,HouseReadSites.Room room){
        var visit=HouseReadSites.state(p.server,p.getUUID()).getCompound(room.key());
        return room!=WAITING&&room!=BATHROOM||room==WAITING&&visit.getInt("Age")>=600||room==BATHROOM&&visit.getBoolean("BedroomOpen");
    }
    private static CompoundTag visit(ServerPlayer p,HouseReadSites.Room room){return HouseReadSites.state(p.server,p.getUUID()).getCompound(room.key());}
    private static void save(ServerPlayer p,HouseReadSites.Room room,CompoundTag visit){var own=HouseReadSites.state(p.server,p.getUUID());own.put(room.key(),visit);HouseReadSites.save(p.server,p.getUUID(),own);}
    private static void arm(ServerPlayer p,HouseReadSites.Room room,String words){var t=visit(p,room);t.putBoolean("NoteArmed",true);save(p,room,t);HouseReadSites.act(p,room,words);}
    private static void loanTag(ServerPlayer p,ItemStack item){CustomData.update(DataComponents.CUSTOM_DATA,item,t->t.putUUID("HouseReadLoan",p.getUUID()));}
    private static void keep(ServerPlayer p,ItemStack item){if(!item.isEmpty()&&!p.getInventory().add(item)){var e=new net.minecraft.world.entity.item.ItemEntity(p.serverLevel(),p.getX(),p.getY(),p.getZ(),item);e.setTarget(p.getUUID());p.serverLevel().addFreshEntity(e);}}
    private static void borrow(ServerPlayer p,Item item,boolean head){
        var t=visit(p,WAITING);int slot=head?39:t.contains("HandSlot")?t.getInt("HandSlot"):p.getInventory().selected;String key=head?"Head":"Hand";ItemStack displaced=ItemStack.EMPTY;
        if(!t.getBoolean("Borrowed"+key)){t.put("Before"+key,p.getInventory().getItem(slot).saveOptional(p.registryAccess()));t.putBoolean("Borrowed"+key,true);if(!head)t.putInt("HandSlot",slot);}
        else if(!HouseReadSites.loan(p.getInventory().getItem(slot),p.getUUID()))displaced=p.getInventory().getItem(slot).copy();
        if(!head)for(int i=0;i<p.getInventory().getContainerSize();i++)if(HouseReadSites.loan(p.getInventory().getItem(i),p.getUUID())&&p.getInventory().getItem(i).getItem() instanceof HouseReadBorrowedItem b&&!b.object.equals("glasses"))p.getInventory().setItem(i,ItemStack.EMPTY);
        var stack=new ItemStack(item);loanTag(p,stack);p.getInventory().setItem(slot,stack);keep(p,displaced);if(!head)p.getInventory().selected=slot;save(p,WAITING,t);p.inventoryMenu.broadcastChanges();
        var base=HouseReadSites.base(p.server,p.getUUID(),WAITING);
        for(var at:List.of(HouseReadRemainingRooms.PHONE_AT,HouseReadRemainingRooms.GLASSES_AT)){var pos=base.offset(at);var state=p.serverLevel().getBlockState(pos);if(state.is(HouseReadRegistry.FITTING.get()))p.serverLevel().setBlock(pos,state.setValue(HouseReadPropBlock.TURNED,at.equals(HouseReadRemainingRooms.GLASSES_AT)?p.getItemBySlot(EquipmentSlot.HEAD).is(HouseReadRegistry.GLASSES.get()):item==HouseReadRegistry.PHONE.get()),Block.UPDATE_CLIENTS);}
    }
    public static void handUse(ServerPlayer p,String object){
        if(HouseReadSites.inside(p)!=WAITING||!HouseReadSites.loan(p.getMainHandItem(),p.getUUID()))return;
        if(object.equals("phone")){var state=new CompoundTag();state.putBoolean("WaitingRoom",true);state.putLong("DayTime",9700+visit(p,WAITING).getInt("Age"));state.putBoolean("Service",true);io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new io.github.knaitoe.theoldesthouse.network.HarriganPhonePayload(true,state));}
        else if(object.equals("paper"))HouseReadTexts.openLiteral(p,WAITING,"The local page",HouseReadTexts.LOCAL_PAGE);
        else if(object.equals("magazine"))HouseReadTexts.openLiteral(p,WAITING,"County and garden",HouseReadTexts.MAGAZINE);
    }
    public static void left(ServerPlayer p,HouseReadSites.Room room,CompoundTag t){
        if(room==WAITING){for(String key:List.of("Hand","Head")){if(!t.getBoolean("Borrowed"+key))continue;int slot=key.equals("Head")?39:t.getInt("HandSlot");var displaced=p.getInventory().getItem(slot).copy();p.getInventory().setItem(slot,ItemStack.parseOptional(p.registryAccess(),t.getCompound("Before"+key)));keep(p,displaced);t.remove("Before"+key);t.putBoolean("Borrowed"+key,false);}p.inventoryMenu.broadcastChanges();
            var b=HouseReadSites.base(p.server,p.getUUID(),room);var level=p.server.getLevel(room.dimension());
            if(level!=null)for(var e:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,HouseReadRooms.bounds(b,room),e->HouseReadSites.loan(e.getItem(),p.getUUID())))e.discard();
            if(level!=null)for(var rel:List.of(HouseReadRemainingRooms.PHONE_AT,HouseReadRemainingRooms.GLASSES_AT)){var at=b.offset(rel);var s=level.getBlockState(at);if(s.is(HouseReadRegistry.FITTING.get()))level.setBlock(at,s.setValue(HouseReadPropBlock.TURNED,false),Block.UPDATE_CLIENTS);}
        }
        if(room==BATHROOM){var b=HouseReadSites.base(p.server,p.getUUID(),room);settle(p,b,t);}
        LAST_MUSIC.remove(p.getUUID());
    }
    public static boolean use(ServerPlayer p,BlockPos b,HouseReadSites.Room room,BlockPos at,HouseReadPropBlock.Part part){
        if(room==WAITING){
            switch(part){
                case ARMCHAIR->{io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,at);}
                case BELL->{HouseReadSites.sound(p,HouseReadRegistry.BELL.get(),at,.55f);HouseReadSites.act(p,room,"rang the reception bell");HouseRead.flag(p,WAITING,"Bell");}
                case PAPER,MAGAZINES,GLASSES,PHONE,COOLER->{
                    if(part==GLASSES)borrow(p,HouseReadRegistry.GLASSES.get(),true);
                    else borrow(p,switch(part){case PAPER->HouseReadRegistry.PAPER.get();case MAGAZINES->HouseReadRegistry.MAGAZINE.get();case PHONE->HouseReadRegistry.PHONE.get();default->HouseReadRegistry.CONE.get();},false);
                    age(p,switch(part){case PAPER->"paper";case MAGAZINES->"magazine";case GLASSES->"glasses";case PHONE->"phone";default->"cooler";},part==PHONE?"DIGITAL":part==COOLER?"TRANSITIONAL":"ANALOG");
                    arm(p,room,switch(part){case PAPER->"opened the local newspaper";case MAGAZINES->"opened the gardening magazine";case GLASSES->"put on the reading glasses";case PHONE->"picked up the phone from the armrest";default->"filled a paper cone at the water cooler";});
                    if(part==COOLER)HouseReadSites.sound(p,HouseReadRegistry.GLUG.get(),at,.55f);else if(part!=GLASSES)handUse(p,part==PAPER?"paper":part==MAGAZINES?"magazine":"phone");
                }
                case CRT->{var state=p.serverLevel().getBlockState(at);int channel=(state.getValue(HouseReadPropBlock.LIFTED)+1)%4;p.serverLevel().setBlock(at,state.setValue(HouseReadPropBlock.LIFTED,channel),Block.UPDATE_CLIENTS);HouseReadSites.sound(p,HouseReadRegistry.HISS.get(),at,.32f);arm(p,room,"turned the television knob to channel "+(channel+1));age(p,"tv","TRANSITIONAL");channels(p);}
                case ASHTRAY->p.displayClientMessage(Component.literal("A glass ashtray, wiped clean."),true);
                case RECEPTION,CALENDAR->{note(p,b,room,"The calendar shows this month.");}
                case CLOCK_BASE,CLOCK_FACE->p.displayClientMessage(Component.literal("The clock keeps its own steady time."),true);
                default->{return false;}
            }return true;
        }
        if(room==BATHROOM){
            if(part==TOWEL){HouseReadSites.act(p,room,"felt the dry towel beside the sink");p.displayClientMessage(Component.literal("A dry towel, folded beside the sink."),true);return true;}
            if(part==LIGHT_SWITCH){var t=visit(p,room);boolean off=!t.getBoolean("LightOff");t.putBoolean("LightOff",off);t.putBoolean("BedroomOpen",true);t.putBoolean("NoteArmed",true);save(p,room,t);for(var lamp:HouseReadRemainingRooms.BATH_LIGHTS)p.serverLevel().setBlock(b.offset(lamp),off?Blocks.AIR.defaultBlockState():Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),Block.UPDATE_CLIENTS);p.serverLevel().setBlock(at,p.serverLevel().getBlockState(at).setValue(HouseReadPropBlock.TURNED,off),Block.UPDATE_CLIENTS);unlock(p,b,room);HouseReadSites.sound(p,SoundEvents.LEVER_CLICK,at,.3f);HouseReadSites.act(p,room,off?(t.getInt("Touches")==0?"turned the light off without touching the counter":"turned the bathroom light off"):"turned the bathroom light back on");if(off&&!HouseRead.seen(LabyrinthData.get(p.server),p.getUUID()).getCompound(BATHROOM.key()).getBoolean("LightOff")){int touched=t.getInt("Touches");HouseRead.see(p,BATHROOM,s->{s.putBoolean("LightOff",true);if(touched==0)s.putBoolean("Untouched",true);});}return true;}
            if(part==TAP){var t=visit(p,room);boolean running=!t.getBoolean("TapRunning");t.putBoolean("TapRunning",running);t.putInt("TapSoundAt",t.getInt("Age"));save(p,room,t);p.serverLevel().setBlock(at,p.serverLevel().getBlockState(at).setValue(HouseReadPropBlock.TURNED,running),Block.UPDATE_CLIENTS);arm(p,room,running?"ran the hot tap":"turned off the hot tap");if(running)tap(p);if(running)HouseReadSites.sound(p,HouseReadRegistry.TAP.get(),at,.5f);else p.connection.send(new ClientboundStopSoundPacket(HouseReadRegistry.TAP.getId(),SoundSource.BLOCKS));return true;}
            if(part==POSTCARD){note(p,b,room,"A picture of a lake, wedged into the mirror frame.");return true;}
            if(part==MIRROR||part==MIRROR_TOP)return true;
        }
        if(room==SHELTER){
            if(part==JUKEBOX){var t=visit(p,room);start(p,b,t);save(p,room,t);note(p,b,room,"One free credit. A slow instrumental; no names on the song strips.");return true;}
            if(part==BOTTLE){HouseReadSites.act(p,room,"looked at the cold bottle on the rail");return true;}
            if(part==ROWBOAT){HouseReadSites.act(p,room,"rested beside the covered rowboat");return true;}
        }
        return false;
    }
    private static void note(ServerPlayer p,BlockPos b,HouseReadSites.Room room,String blank){var at=b.offset(HouseReadRooms.note(room));if(p.serverLevel().getBlockState(at).getValue(HouseReadPropBlock.WRITING))HouseReadTexts.open(p,room);else p.displayClientMessage(Component.literal(blank),true);}
    public static boolean routineUse(ServerPlayer p,BlockPos at,BlockState state,BlockHitResult hit){
        int group=state.getValue(HouseReadPropBlock.PART).ordinal()-ROUTINE_A.ordinal();if(group<0||group>3)return false;
        if(HouseReadSites.inside(p)!=BATHROOM)return true;
        var b=HouseReadSites.base(p.server,p.getUUID(),BATHROOM);var t=visit(p,BATHROOM);settle(p,b,t);
        int index=net.minecraft.util.Mth.clamp((int)((hit.getLocation().x-at.getX())*3),0,2),item=group*3+index;
        var display=new ItemDisplay(EntityType.ITEM_DISPLAY,p.serverLevel());var data=display.saveWithoutId(new CompoundTag());data.put("item",new ItemStack(HouseReadRegistry.ROUTINES.get(item).get()).save(p.registryAccess()));data.putString("item_display","fixed");display.load(data);display.setPos(at.getX()+.5,at.getY()+.5,at.getZ()+.5);display.addTag(LIFT);display.getPersistentData().putUUID("Reader",p.getUUID());
        pose(display,index,.01f,0);if(!p.serverLevel().addFreshEntity(display))return true;pose(display,index,.32f,.10f);t.putInt("LiftIndex",index);t.putBoolean("LiftLowered",false);
        p.serverLevel().setBlock(at,state.setValue(HouseReadPropBlock.LIFTED,index+1),Block.UPDATE_CLIENTS);t.putLong("LiftAt",at.asLong());t.putInt("LiftUntil",t.getInt("Age")+24);t.putUUID("LiftDisplay",display.getUUID());t.putInt("Touches",t.getInt("Touches")+1);t.putBoolean("NoteArmed",true);save(p,BATHROOM,t);
        int position=HouseReadRemainingRooms.ROUTINE_PLACES.indexOf(at.subtract(b));HouseReadSites.act(p,BATHROOM,"lifted the "+ROUTINES[item]+" from position "+(position+1));routine(p,item,position);p.displayClientMessage(new ItemStack(HouseReadRegistry.ROUTINES.get(item).get()).getHoverName(),true);return true;
    }
    private static ListTag floats(float... values){var list=new ListTag();for(float value:values)list.add(FloatTag.valueOf(value));return list;}
    private static void pose(ItemDisplay display,int index,float lift,float roll){var tag=display.saveWithoutId(new CompoundTag());var transform=new CompoundTag();transform.put("translation",floats((index-1)*5/16f,lift,0));transform.put("scale",floats(1,1,1));transform.put("left_rotation",floats(0,0,(float)Math.sin(roll/2),(float)Math.cos(roll/2)));transform.put("right_rotation",floats(0,0,0,1));tag.put("transformation",transform);tag.putInt("interpolation_duration",5);tag.putInt("start_interpolation",0);display.load(tag);}
    private static void settle(ServerPlayer p,BlockPos b,CompoundTag t){
        if(t.contains("LiftAt")){var at=BlockPos.of(t.getLong("LiftAt"));if(HouseReadRooms.bounds(b,BATHROOM).contains(at.getCenter())){var s=p.serverLevel().getBlockState(at);if(s.is(HouseReadRegistry.FITTING.get()))p.serverLevel().setBlock(at,s.setValue(HouseReadPropBlock.LIFTED,0),Block.UPDATE_CLIENTS);}t.remove("LiftAt");}
        for(var e:p.serverLevel().getEntitiesOfClass(ItemDisplay.class,HouseReadRooms.bounds(b,BATHROOM),d->d.getTags().contains(LIFT)&&d.getPersistentData().hasUUID("Reader")&&d.getPersistentData().getUUID("Reader").equals(p.getUUID())))e.discard();t.remove("LiftDisplay");
    }
    private static void unlock(ServerPlayer p,BlockPos b,HouseReadSites.Room room){for(var at:List.of(b.offset(exit(room)),b.offset(exit(room)).above())){var s=p.serverLevel().getBlockState(at);if(s.getBlock() instanceof DoorBlock)p.serverLevel().setBlock(at,s.setValue(DoorBlock.OPEN,true),Block.UPDATE_CLIENTS);}}
    private static void start(ServerPlayer p,BlockPos b,CompoundTag t){if(t.getBoolean("SongStarted"))return;t.putBoolean("SongStarted",true);t.putInt("SongAge",0);HouseReadSites.sound(p,HouseReadRegistry.START.get(),b.offset(HouseReadRemainingRooms.JUKEBOX_AT),.5f);}
    public static void tick(ServerPlayer p,BlockPos b,HouseReadSites.Room room,CompoundTag t,int age){
        if(room==WAITING){
            if(p.isPassenger()&&!t.getBoolean("Sat")){t.putBoolean("Sat",true);literal(t,"sat in a waiting chair");}
            if(age%20==1)HouseReadSites.sound(p,HouseReadRegistry.CLOCK.get(),b.offset(-6,0,-10),.30f);
            if(age%120==1)HouseReadSites.sound(p,HouseReadRegistry.RAIN.get(),b.offset(-8,2,-5),.4f);
            if(age==600){unlock(p,b,room);t.putBoolean("NoteArmed",true);doorClicked(p,b);HouseReadSites.sound(p,SoundEvents.IRON_DOOR_OPEN,b.offset(exit(room)),.4f);}
            if(age>=600&&p.getZ()<b.getZ()-14.6)HouseReadSites.leave(p,true);
        }else if(room==BATHROOM){
            if(age%120==1){HouseReadSites.sound(p,HouseReadRegistry.RAIN.get(),b.offset(-6,2,-5),.32f);HouseReadSites.sound(p,HouseReadRegistry.RADIATOR.get(),b.offset(-6,0,-1),.25f);}
            if(t.getBoolean("TapRunning")){if(age-t.getInt("TapSoundAt")>=60){HouseReadSites.sound(p,HouseReadRegistry.TAP.get(),b.offset(HouseReadRemainingRooms.TAP_AT),.5f);t.putInt("TapSoundAt",age);}if(age%5==0)p.serverLevel().sendParticles(p,ParticleTypes.CLOUD,false,b.getX()+.5,b.getY()+1.2,b.getZ()-6.25,1,.12,.04,.1,.005);}
            if(t.contains("LiftAt")&&!t.getBoolean("LiftLowered")&&age>=t.getInt("LiftUntil")-7&&t.hasUUID("LiftDisplay")&&p.serverLevel().getEntity(t.getUUID("LiftDisplay")) instanceof ItemDisplay display){pose(display,t.getInt("LiftIndex"),.01f,0);t.putBoolean("LiftLowered",true);}
            if(t.contains("LiftAt")&&age>=t.getInt("LiftUntil"))settle(p,b,t);
            if(age>=200)t.putBoolean("NoteArmed",true);
            if(t.getBoolean("BedroomOpen")&&p.getZ()<b.getZ()-8.6&&p.getX()>b.getX()+2&&p.getX()<b.getX()+5)HouseReadSites.leave(p,true);
        }else if(room==SHELTER){
            HouseReadShelter.ensure(p,b,t);
            if(age%120==1)HouseReadSites.sound(p,HouseReadRegistry.RAIN.get(),b.offset(0,1,-26),.65f);
            if(age%7==0){int side=p.getRandom().nextBoolean()?1:-1;p.serverLevel().sendParticles(p,ParticleTypes.FALLING_WATER,false,b.getX()+side*11.5,b.getY()+4,b.getZ()-2-p.getRandom().nextDouble()*20,3,.1,.15,.3,0);}
            if(p.getZ()<b.getZ()-4)start(p,b,t);
            if(t.getBoolean("SongStarted")&&t.getInt("SongAge")<900){
                int song=t.getInt("SongAge");long now=p.serverLevel().getGameTime();Long last=LAST_MUSIC.put(p.getUUID(),now);
                if(song%100==0||last==null||now-last>2){for(var sound:HouseReadRegistry.MUSIC)p.connection.send(new ClientboundStopSoundPacket(sound.getId(),SoundSource.BLOCKS));HouseReadSites.sound(p,HouseReadRegistry.MUSIC.get(Math.min(8,song/100)).get(),b.offset(HouseReadRemainingRooms.JUKEBOX_AT),.85f);}
                t.putInt("SongAge",song+1);if(song==899)t.putBoolean("NoteArmed",true);
                if(HouseReadRemainingRooms.onDock(b,p.position())&&!t.getBoolean("Lantern")){t.putBoolean("Lantern",true);t.putBoolean("NoteArmed",true);literal(t,"stood by the lantern at the dock steps");}
                shelterRead(p,b,t);
            }
            if(p.getZ()>b.getZ()+2.6&&Math.abs(p.getX()-b.getX()-3.5)<1.2)HouseReadSites.leave(p,true);
        }
    }
    // ---- 0.4.159: the read's signals. The first deliberate act inside a room's window sets a trait; later poking is observation only.
    public static final String[] HAIR={"WRAPPED","BRAIDED_AND_OILED","BRUSHED_DRY","WASHED_AND_BARBERED"};
    private static CompoundTag seen(ServerPlayer p,HouseReadSites.Room room){return HouseRead.seen(LabyrinthData.get(p.server),p.getUUID()).getCompound(room.key());}
    /** The waiting room's first time-killer, inside the wait. */
    private static void age(ServerPlayer p,String kind,String value){if(visit(p,WAITING).getInt("Age")>=600||seen(p,WAITING).contains("First"))return;HouseRead.first(p,WAITING,"First",kind);HouseRead.signal(p,HouseRead.Trait.AGE,value,false);}
    /** Flipping through more channels settles a reader who began at the cart. */
    private static void channels(ServerPlayer p){var s=seen(p,WAITING);int n=s.getInt("Channels")+1;HouseRead.see(p,WAITING,t->t.putInt("Channels",n));if(visit(p,WAITING).getInt("Age")<600&&!s.contains("Second")&&n>=(s.getString("First").equals("tv")?3:2)&&s.contains("First")){HouseRead.see(p,WAITING,t->{t.putString("Second","TRANSITIONAL");t.putString("Kept","channels");});HouseRead.signal(p,HouseRead.Trait.AGE,"TRANSITIONAL",true);}}
    /** What the reader is doing as the door clicks: the rest of the wait. Sitting or pacing alone is only observed. */
    private static void doorClicked(ServerPlayer p,BlockPos b){
        var s=seen(p,WAITING);HouseRead.see(p,WAITING,t->{t.putBoolean("Waited",true);if(p.isPassenger())t.putBoolean("Sat",true);});if(!s.contains("First")||s.contains("Second"))return;
        var hand=p.getMainHandItem();boolean loan=HouseReadSites.loan(hand,p.getUUID()),glasses=HouseReadSites.loan(p.getItemBySlot(EquipmentSlot.HEAD),p.getUUID());
        boolean cart=p.position().distanceTo(Vec3.atBottomCenterOf(b.offset(HouseReadRemainingRooms.CRT_AT)))<3.5||p.position().distanceTo(Vec3.atBottomCenterOf(b.offset(HouseReadRemainingRooms.COOLER_AT)))<3.5;
        String kept=loan&&hand.is(HouseReadRegistry.PHONE.get())?"phone":loan&&hand.is(HouseReadRegistry.PAPER.get())?"paper":loan&&hand.is(HouseReadRegistry.MAGAZINE.get())?"magazine":glasses?"glasses":cart?"cart":p.isPassenger()?"chair":"";
        if(kept.isEmpty())return;String value=kept.equals("phone")?"DIGITAL":kept.equals("cart")?"TRANSITIONAL":"ANALOG";
        HouseRead.see(p,WAITING,t->{t.putString("Second",value);t.putString("Kept",kept);});HouseRead.signal(p,HouseRead.Trait.AGE,value,true);
    }
    /** The bathroom: the first routine item sets the routine, a second from the same set settles it, before the light goes out. */
    private static void routine(ServerPlayer p,int item,int position){
        var s=seen(p,BATHROOM);if(s.getBoolean("LightOff"))return;
        if(!s.contains("Item")){HouseRead.see(p,BATHROOM,t->{t.putInt("Item",item);t.putInt("Position",position);});HouseRead.signal(p,HouseRead.Trait.HAIR,HAIR[item/3],false);return;}
        if(!s.getBoolean("Second")&&s.getInt("Item")/3==item/3&&s.getInt("Item")!=item){HouseRead.see(p,BATHROOM,t->{t.putBoolean("Second",true);t.putInt("SecondItem",item);});HouseRead.signal(p,HouseRead.Trait.HAIR,HAIR[item/3],true);}
    }
    /** The hot tap settles only the set whose washcloth hangs over the faucet; for the others it is an observation. */
    private static void tap(ServerPlayer p){
        var s=seen(p,BATHROOM);HouseRead.flag(p,BATHROOM,"Tap");
        if(!s.getBoolean("LightOff")&&s.contains("Item")&&s.getInt("Item")/3==3&&!s.getBoolean("Second")){HouseRead.flag(p,BATHROOM,"Second");HouseRead.signal(p,HouseRead.Trait.HAIR,HAIR[3],true);}
    }
    /** A stranger's face near the middle of the reader's view, within eight degrees, with nothing in between. */
    static boolean framed(ServerPlayer p,net.minecraft.world.entity.Entity a){
        var eye=p.getEyePosition();var ray=a.getEyePosition().subtract(eye);if(ray.lengthSqr()>32*32||ray.lengthSqr()<.01||p.getLookAngle().dot(ray.normalize())<Math.cos(Math.toRadians(8)))return false;
        return p.serverLevel().clip(new net.minecraft.world.level.ClipContext(eye,a.getEyePosition(),net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;
    }
    /**
     * The shelter, while the song plays: each stranger gets gaze and time within five blocks, and the House compares their
     * shares. Walking out past both to the lantern first is reading no one.
     */
    private static void shelterRead(ServerPlayer p,BlockPos b,CompoundTag t){
        int song=t.getInt("SongAge")-1;var s=seen(p,SHELTER);boolean given=s.contains("Looked")||s.contains("FirstLantern");
        int[] score={t.getInt("ManScore"),t.getInt("WomanScore")};
        for(int i=0;i<2;i++){String key=i==0?"Man":"Woman";if(!t.hasUUID(key)||!(p.serverLevel().getEntity(t.getUUID(key)) instanceof LiteraryActor a))continue;if(p.distanceToSqr(a)<25)score[i]++;if(framed(p,a))score[i]++;}
        t.putInt("ManScore",score[0]);t.putInt("WomanScore",score[1]);
        boolean dock=HouseReadRemainingRooms.onDock(b,p.position()),middle=HouseReadRemainingRooms.middle(b,p.position());
        t.putInt("DockTicks",dock?t.getInt("DockTicks")+1:Math.max(0,t.getInt("DockTicks")-3));t.putInt("MiddleTicks",middle?t.getInt("MiddleTicks")+1:Math.max(0,t.getInt("MiddleTicks")-3));
        if(t.getInt("MiddleTicks")==60)HouseRead.first(p,SHELTER,"Stood","middle");
        if(!given&&dock){HouseRead.see(p,SHELTER,x->{x.putBoolean("FirstLantern",true);if(!x.contains("Stood"))x.putString("Stood","lantern");});HouseRead.signal(p,HouseRead.Trait.DRAWN_TO,"NO_ONE",false);given=true;}
        int total=score[0]+score[1];
        if(!given&&(total>=120||song>=899&&total>=40)){double share=score[0]/(double)total;String looked=share>=.65?"man":share<=.35?"woman":score[0]>=30&&score[1]>=30?"both":"";
            if(!looked.isEmpty()){HouseRead.first(p,SHELTER,"Looked",looked);HouseRead.signal(p,HouseRead.Trait.DRAWN_TO,looked.equals("man")?"MEN":looked.equals("woman")?"WOMEN":"BOTH",false);}}
        if(song!=899)return;
        // Staying beside one until the song ends, between them, or out on the dock with the lantern, settles it.
        String stayed=t.getInt("ManNear")>=200?"man":t.getInt("WomanNear")>=200?"woman":t.getInt("MiddleTicks")>=200?"middle":t.getInt("DockTicks")>=200?"lantern":"";
        if(stayed.isEmpty())return;HouseRead.see(p,SHELTER,x->{x.putString("Stood",stayed);x.putBoolean("Song",true);});
        HouseRead.signal(p,HouseRead.Trait.DRAWN_TO,switch(stayed){case "man"->"MEN";case "woman"->"WOMEN";case "middle"->"BOTH";default->"NO_ONE";},true);
    }
    static void literal(CompoundTag t,String words){var list=t.getList("Actions",Tag.TAG_STRING);if(list.size()<64)list.add(StringTag.valueOf(words));t.put("Actions",list);}
    public static void clear(){LAST_MUSIC.clear();}
}
