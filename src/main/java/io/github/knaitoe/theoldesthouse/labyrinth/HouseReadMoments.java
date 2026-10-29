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
    /**
     * Forty seconds in the waiting room, then the receptionist calls the reader through. 0.4.160 cut it to fifteen; the owner
     * (0.4.165): "the wait time for the waiting office personality vignette can successfully be bumped back up to 40 seconds.
     * 15 seconds whizzes by with the amount of detail you've added."
     */
    public static final int WAIT=800;
    public static BlockPos exit(HouseReadSites.Room room){return switch(room){case WAITING->new BlockPos(0,0,-12);case BATHROOM->new BlockPos(4,0,-10);case SHELTER->new BlockPos(3,0,2);default->throw new IllegalArgumentException(room.name());};}
    public static boolean canProceed(ServerPlayer p,HouseReadSites.Room room){
        var visit=HouseReadSites.state(p.server,p.getUUID()).getCompound(room.key());
        return room!=WAITING&&room!=BATHROOM||room==WAITING&&visit.getInt("Age")>=WAIT||room==BATHROOM&&visit.getBoolean("BedroomOpen");
    }
    private static CompoundTag visit(ServerPlayer p,HouseReadSites.Room room){return HouseReadSites.state(p.server,p.getUUID()).getCompound(room.key());}
    private static void save(ServerPlayer p,HouseReadSites.Room room,CompoundTag visit){var own=HouseReadSites.state(p.server,p.getUUID());own.put(room.key(),visit);HouseReadSites.save(p.server,p.getUUID(),own);}
    private static void arm(ServerPlayer p,HouseReadSites.Room room,String words){var t=visit(p,room);t.putBoolean("NoteArmed",true);save(p,room,t);HouseReadSites.act(p,room,words);}
    private static void loanTag(ServerPlayer p,ItemStack item){CustomData.update(DataComponents.CUSTOM_DATA,item,t->t.putUUID("HouseReadLoan",p.getUUID()));}
    private static void keep(ServerPlayer p,ItemStack item){if(!item.isEmpty()&&!p.getInventory().add(item)){var e=new net.minecraft.world.entity.item.ItemEntity(p.serverLevel(),p.getX(),p.getY(),p.getZ(),item);e.setTarget(p.getUUID());p.serverLevel().addFreshEntity(e);}}
    private static void borrow(ServerPlayer p,Item item,boolean head){borrow(p,WAITING,item,head);}
    /** Lends a room's object into the reader's hand or onto their head; the original is kept to put back when they leave. */
    private static void borrow(ServerPlayer p,HouseReadSites.Room room,Item item,boolean head){
        var t=visit(p,room);int slot=head?39:t.contains("HandSlot")?t.getInt("HandSlot"):p.getInventory().selected;String key=head?"Head":"Hand";ItemStack displaced=ItemStack.EMPTY;
        if(!t.getBoolean("Borrowed"+key)){t.put("Before"+key,p.getInventory().getItem(slot).saveOptional(p.registryAccess()));t.putBoolean("Borrowed"+key,true);if(!head)t.putInt("HandSlot",slot);}
        else if(!HouseReadSites.loan(p.getInventory().getItem(slot),p.getUUID()))displaced=p.getInventory().getItem(slot).copy();
        if(!head)for(int i=0;i<p.getInventory().getContainerSize();i++)if(HouseReadSites.loan(p.getInventory().getItem(i),p.getUUID())&&p.getInventory().getItem(i).getItem() instanceof HouseReadBorrowedItem b&&!b.object.equals("glasses"))p.getInventory().setItem(i,ItemStack.EMPTY);
        var stack=new ItemStack(item);loanTag(p,stack);p.getInventory().setItem(slot,stack);keep(p,displaced);if(!head)p.getInventory().selected=slot;save(p,room,t);p.inventoryMenu.broadcastChanges();
        if(room!=WAITING)return;
        var base=HouseReadSites.base(p.server,p.getUUID(),WAITING);
        for(var at:List.of(HouseReadRemainingRooms.PHONE_AT,HouseReadRemainingRooms.GLASSES_AT)){var pos=base.offset(at);var state=p.serverLevel().getBlockState(pos);if(state.is(HouseReadRegistry.FITTING.get()))p.serverLevel().setBlock(pos,state.setValue(HouseReadPropBlock.TURNED,at.equals(HouseReadRemainingRooms.GLASSES_AT)?p.getItemBySlot(EquipmentSlot.HEAD).is(HouseReadRegistry.GLASSES.get()):item==HouseReadRegistry.PHONE.get()),Block.UPDATE_CLIENTS);}
    }
    public static void handUse(ServerPlayer p,String object){
        if(HouseReadSites.inside(p)!=WAITING||!HouseReadSites.loan(p.getMainHandItem(),p.getUUID()))return;
        if(object.equals("phone")){var state=new CompoundTag();state.putBoolean("WaitingRoom",true);state.putLong("DayTime",9700+visit(p,WAITING).getInt("Age"));state.putBoolean("Service",true);io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new io.github.knaitoe.theoldesthouse.network.HarriganPhonePayload(true,state));}
        else if(object.equals("paper"))HouseReadTexts.openLiteral(p,WAITING,"The local page",HouseReadTexts.LOCAL_PAGE);
        else if(object.equals("magazine"))HouseReadTexts.openLiteral(p,WAITING,"County and garden",HouseReadTexts.MAGAZINE);
    }
    /** Puts a lent object back where it came from: the reader's own item returns to the slot it was in. */
    private static void giveBack(ServerPlayer p,HouseReadSites.Room room,boolean head){
        var t=visit(p,room);String key=head?"Head":"Hand";if(!t.getBoolean("Borrowed"+key))return;int slot=head?39:t.getInt("HandSlot");
        var current=p.getInventory().getItem(slot);var displaced=HouseReadSites.loan(current,p.getUUID())?ItemStack.EMPTY:current.copy();
        p.getInventory().setItem(slot,ItemStack.parseOptional(p.registryAccess(),t.getCompound("Before"+key)));keep(p,displaced);t.remove("Before"+key);t.putBoolean("Borrowed"+key,false);save(p,room,t);p.inventoryMenu.broadcastChanges();
        if(room!=WAITING)return;var base=HouseReadSites.base(p.server,p.getUUID(),WAITING);
        for(var at:List.of(HouseReadRemainingRooms.PHONE_AT,HouseReadRemainingRooms.GLASSES_AT)){var pos=base.offset(at);var state=p.serverLevel().getBlockState(pos);if(state.is(HouseReadRegistry.FITTING.get()))p.serverLevel().setBlock(pos,state.setValue(HouseReadPropBlock.TURNED,at.equals(HouseReadRemainingRooms.GLASSES_AT)?!head&&p.getItemBySlot(EquipmentSlot.HEAD).is(HouseReadRegistry.GLASSES.get()):head&&p.getMainHandItem().is(HouseReadRegistry.PHONE.get())),Block.UPDATE_CLIENTS);}
    }
    private static boolean holding(ServerPlayer p,Item item){return p.getMainHandItem().is(item)&&HouseReadSites.loan(p.getMainHandItem(),p.getUUID());}
    public static void left(ServerPlayer p,HouseReadSites.Room room,CompoundTag t){
        if(room==WAITING||room==BATHROOM){for(String key:List.of("Hand","Head")){if(!t.getBoolean("Borrowed"+key))continue;int slot=key.equals("Head")?39:t.getInt("HandSlot");var displaced=p.getInventory().getItem(slot).copy();p.getInventory().setItem(slot,ItemStack.parseOptional(p.registryAccess(),t.getCompound("Before"+key)));keep(p,displaced);t.remove("Before"+key);t.putBoolean("Borrowed"+key,false);}p.inventoryMenu.broadcastChanges();
            var b=HouseReadSites.base(p.server,p.getUUID(),room);var level=p.server.getLevel(room.dimension());
            if(level!=null)for(var e:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,HouseReadRooms.bounds(b,room),e->HouseReadSites.loan(e.getItem(),p.getUUID())))e.discard();
            if(level!=null&&room==WAITING)for(var rel:List.of(HouseReadRemainingRooms.PHONE_AT,HouseReadRemainingRooms.GLASSES_AT)){var at=b.offset(rel);var s=level.getBlockState(at);if(s.is(HouseReadRegistry.FITTING.get()))level.setBlock(at,s.setValue(HouseReadPropBlock.TURNED,false),Block.UPDATE_CLIENTS);}
        }
        if(room==BATHROOM){var b=HouseReadSites.base(p.server,p.getUUID(),room);settle(p,b,t);var level=p.server.getLevel(room.dimension());
            // The routine item still in hand goes back to its place on the vanity.
            if(level!=null&&t.contains("HeldAt")){var at=BlockPos.of(t.getLong("HeldAt"));var s=level.getBlockState(at);if(HouseReadRooms.bounds(b,BATHROOM).contains(at.getCenter())&&s.is(HouseReadRegistry.FITTING.get()))level.setBlock(at,s.setValue(HouseReadPropBlock.LIFTED,0),Block.UPDATE_CLIENTS);}
            t.remove("Held");t.remove("HeldAt");}
        LAST_MUSIC.remove(p.getUUID());
    }
    public static boolean use(ServerPlayer p,BlockPos b,HouseReadSites.Room room,BlockPos at,HouseReadPropBlock.Part part){
        if(room==WAITING){
            switch(part){
                case ARMCHAIR->{io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,at);}
                case BELL->{HouseReadSites.sound(p,HouseReadRegistry.BELL.get(),at,.55f);HouseReadSites.act(p,room,"rang the reception bell");HouseRead.flag(p,WAITING,"Bell");}
                case PAPER,MAGAZINES,GLASSES,PHONE,COOLER->{
                    // What the reader already holds goes back where it came from.
                    var lent=switch(part){case PAPER->HouseReadRegistry.PAPER.get();case MAGAZINES->HouseReadRegistry.MAGAZINE.get();case PHONE->HouseReadRegistry.PHONE.get();default->null;};
                    if(lent!=null&&holding(p,lent)){giveBack(p,room,false);HouseReadSites.act(p,room,switch(part){case PAPER->"put the paper back";case MAGAZINES->"put the magazine back";default->"put the phone back on its charger";});return true;}
                    if(part==GLASSES&&HouseReadSites.loan(p.getItemBySlot(EquipmentSlot.HEAD),p.getUUID())){giveBack(p,room,true);HouseReadSites.act(p,room,"took off the reading glasses");return true;}
                    if(part==GLASSES)borrow(p,HouseReadRegistry.GLASSES.get(),true);
                    else borrow(p,switch(part){case PAPER->HouseReadRegistry.PAPER.get();case MAGAZINES->HouseReadRegistry.MAGAZINE.get();case PHONE->HouseReadRegistry.PHONE.get();default->HouseReadRegistry.CONE.get();},false);
                    age(p,switch(part){case PAPER->"paper";case MAGAZINES->"magazine";case GLASSES->"glasses";case PHONE->"phone";default->"cooler";},part==PHONE?"DIGITAL":part==COOLER?"TRANSITIONAL":"ANALOG");
                    arm(p,room,switch(part){case PAPER->"opened the local newspaper";case MAGAZINES->"opened the gardening magazine";case GLASSES->"put on the reading glasses";case PHONE->"picked up the phone from the armrest";default->"filled a paper cone at the water cooler";});
                    if(part==COOLER)HouseReadSites.sound(p,HouseReadRegistry.GLUG.get(),at,.55f);else if(part!=GLASSES)handUse(p,part==PAPER?"paper":part==MAGAZINES?"magazine":"phone");
                }
                case CRT->{var state=p.serverLevel().getBlockState(at);int channel=(state.getValue(HouseReadPropBlock.LIFTED)+1)%4;p.serverLevel().setBlock(at,state.setValue(HouseReadPropBlock.LIFTED,channel),Block.UPDATE_CLIENTS);HouseReadSites.sound(p,HouseReadRegistry.HISS.get(),at,.32f);arm(p,room,"turned the television knob to channel "+(channel+1));age(p,"tv","TRANSITIONAL");channels(p);}
                case ASHTRAY->p.displayClientMessage(Component.literal("A glass ashtray, wiped clean."),true);
                case OUTLET->p.displayClientMessage(Component.literal("The phone's charger, still plugged in."),true);
                case SIGN->p.displayClientMessage(Component.literal("Back in a moment."),true);
                case WALL_CLOCK->p.displayClientMessage(Component.literal("A plain office clock, a few minutes slow."),true);
                case RECEPTION,CALENDAR->{note(p,b,room,"The calendar shows this month.");}
                case CLOCK_BASE,CLOCK_FACE->p.displayClientMessage(Component.literal("The clock keeps its own steady time."),true);
                case TABLE_END,TABLE_MID,SIDE_TABLE->{}
                default->{return false;}
            }return true;
        }
        if(room==BATHROOM){
            if((part==MIRROR||part==MIRROR_TOP||part==SINK)&&groom(p,b,at))return true;
            if(part==SINK){part=TAP;}
            if(part==TOWEL_BAR){HouseReadSites.act(p,room,"felt the dry towel on the bar");p.displayClientMessage(Component.literal("A dry towel on the bar."),true);return true;}
            if(part==TOWEL){HouseReadSites.act(p,room,"felt the dry towel beside the sink");p.displayClientMessage(Component.literal("A dry towel, folded beside the sink."),true);return true;}
            if(part==LIGHT_SWITCH){var t=visit(p,room);boolean off=!t.getBoolean("LightOff");t.putBoolean("LightOff",off);t.putBoolean("BedroomOpen",true);t.putBoolean("NoteArmed",true);if(off)t.putInt("LeaveAt",t.getInt("Age")+80);else if(!t.getBoolean("Done"))t.remove("LeaveAt");save(p,room,t);lights(p,b,off);p.serverLevel().setBlock(at,p.serverLevel().getBlockState(at).setValue(HouseReadPropBlock.TURNED,off),Block.UPDATE_CLIENTS);unlock(p,b,room);HouseReadSites.sound(p,SoundEvents.LEVER_CLICK,at,.3f);HouseReadSites.act(p,room,off?(t.getInt("Touches")==0?"turned the light off without touching the counter":"turned the bathroom light off"):"turned the bathroom light back on");if(off&&!HouseRead.seen(LabyrinthData.get(p.server),p.getUUID()).getCompound(BATHROOM.key()).getBoolean("LightOff")){int touched=t.getInt("Touches");HouseRead.see(p,BATHROOM,s->{s.putBoolean("LightOff",true);if(touched==0)s.putBoolean("Untouched",true);});}return true;}
            if(part==TAP){var t=visit(p,room);boolean running=!t.getBoolean("TapRunning");t.putBoolean("TapRunning",running);t.putInt("TapSoundAt",t.getInt("Age"));save(p,room,t);var sink=b.offset(HouseReadRemainingRooms.SINK_AT);p.serverLevel().setBlock(sink,p.serverLevel().getBlockState(sink).setValue(HouseReadPropBlock.TURNED,running),Block.UPDATE_CLIENTS);arm(p,room,running?"ran the hot tap":"turned off the hot tap");if(running)tap(p);if(running)HouseReadSites.sound(p,HouseReadRegistry.TAP.get(),at,.5f);else p.connection.send(new ClientboundStopSoundPacket(HouseReadRegistry.TAP.getId(),SoundSource.BLOCKS));return true;}
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
    /**
     * 0.4.160, the owner's playtest notes: the reader picks up one routine item at a time. Picking another puts the first
     * back; using the one in hand at the mirror is the routine, and it ends the bathroom: the light goes out, the bedroom
     * door opens and the reader goes on.
     */
    public static boolean routineUse(ServerPlayer p,BlockPos at,BlockState state,BlockHitResult hit){
        int group=state.getValue(HouseReadPropBlock.PART).ordinal()-ROUTINE_A.ordinal();if(group<0||group>3)return false;
        if(HouseReadSites.inside(p)!=BATHROOM)return true;
        var b=HouseReadSites.base(p.server,p.getUUID(),BATHROOM);var t=visit(p,BATHROOM);
        int index=net.minecraft.util.Mth.clamp((int)((hit.getLocation().x-at.getX())*3),0,2),item=group*3+index,position=HouseReadRemainingRooms.ROUTINE_PLACES.indexOf(at.subtract(b));
        if(t.getBoolean("Done")){p.displayClientMessage(Component.literal("You have what you need."),true);return true;}
        int held=t.contains("Held")?t.getInt("Held"):-1;
        if(held>=0){putBack(p,b);if(held==item){HouseReadSites.act(p,BATHROOM,"put the "+ROUTINES[item]+" back");return true;}}
        borrow(p,BATHROOM,HouseReadRegistry.ROUTINES.get(item).get(),false);t=visit(p,BATHROOM);
        t.putInt("Held",item);t.putLong("HeldAt",at.asLong());t.putInt("Touches",t.getInt("Touches")+1);t.putBoolean("NoteArmed",true);save(p,BATHROOM,t);
        p.serverLevel().setBlock(at,state.setValue(HouseReadPropBlock.LIFTED,index+1),Block.UPDATE_CLIENTS);
        HouseReadSites.act(p,BATHROOM,"picked up the "+ROUTINES[item]+" from position "+(position+1));routine(p,item,position);
        p.displayClientMessage(Component.literal("The "+ROUTINES[item]+". Use it at the mirror."),true);return true;
    }
    /** The routine item in hand goes back to its own place on the counter. */
    private static void putBack(ServerPlayer p,BlockPos b){
        var t=visit(p,BATHROOM);if(t.contains("HeldAt")){var at=BlockPos.of(t.getLong("HeldAt"));var s=p.serverLevel().getBlockState(at);if(HouseReadRooms.bounds(b,BATHROOM).contains(at.getCenter())&&s.is(HouseReadRegistry.FITTING.get()))p.serverLevel().setBlock(at,s.setValue(HouseReadPropBlock.LIFTED,0),Block.UPDATE_CLIENTS);}
        t.remove("Held");t.remove("HeldAt");save(p,BATHROOM,t);giveBack(p,BATHROOM,false);
    }
    /** Using the routine item in hand at the mirror or the sink. */
    private static boolean groom(ServerPlayer p,BlockPos b,BlockPos at){
        var t=visit(p,BATHROOM);if(!t.contains("Held")||t.getBoolean("Done"))return false;int item=t.getInt("Held");
        if(!p.getMainHandItem().is(HouseReadRegistry.ROUTINES.get(item).get()))return false;
        p.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);HouseReadSites.sound(p,item==11?HouseReadRegistry.TAP.get():SoundEvents.ARMOR_EQUIP_LEATHER.value(),at,.6f);
        var seen=seen(p,BATHROOM);if(!seen.contains("Used")){HouseRead.see(p,BATHROOM,x->x.putInt("Used",item));if(!seen.getBoolean("LightOff"))HouseRead.signal(p,HouseRead.Trait.HAIR,HAIR[item/3],true);}
        t.putBoolean("Done",true);t.putInt("LightsOutAt",t.getInt("Age")+30);t.putBoolean("NoteArmed",true);save(p,BATHROOM,t);HouseReadSites.act(p,BATHROOM,"used the "+ROUTINES[item]+" at the mirror");return true;
    }
    /** The bathroom's two lights, by their switch. */
    private static void lights(ServerPlayer p,BlockPos b,boolean off){for(var lamp:HouseReadRemainingRooms.BATH_LIGHTS){var at=b.offset(lamp);var s=p.serverLevel().getBlockState(at);if(s.is(HouseReadRegistry.FITTING.get()))p.serverLevel().setBlock(at,s.setValue(HouseReadPropBlock.TURNED,off),Block.UPDATE_CLIENTS);}}
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
            if(age%5==0)p.serverLevel().sendParticles(p,ParticleTypes.FALLING_WATER,false,b.getX()+8+p.getRandom().nextDouble()*3,b.getY()+3.8,b.getZ()-10+p.getRandom().nextDouble()*12,2,.2,.1,.2,0);
            // The receptionist calls the reader through, and the House takes them on.
            if(age==WAIT){unlock(p,b,room);t.putBoolean("NoteArmed",true);doorClicked(p,b);HouseReadSites.sound(p,SoundEvents.WOODEN_DOOR_OPEN,b.offset(exit(room)),.5f);p.displayClientMessage(Component.literal("\u201cSorry for keeping you waiting.\u201d").withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),false);}
            if(age>=WAIT&&(p.getZ()<b.getZ()-12.6||age>=WAIT+40))HouseReadSites.leave(p,true);
        }else if(room==BATHROOM){
            if(age%120==1){HouseReadSites.sound(p,HouseReadRegistry.RAIN.get(),b.offset(-6,2,-5),.32f);HouseReadSites.sound(p,HouseReadRegistry.RADIATOR.get(),b.offset(-6,0,-1),.25f);}
            if(t.getBoolean("TapRunning")){if(age-t.getInt("TapSoundAt")>=60){HouseReadSites.sound(p,HouseReadRegistry.TAP.get(),b.offset(HouseReadRemainingRooms.TAP_AT),.5f);t.putInt("TapSoundAt",age);}if(age%5==0)p.serverLevel().sendParticles(p,ParticleTypes.CLOUD,false,b.getX()+.5,b.getY()+1.1,b.getZ()-8.4,1,.12,.04,.1,.005);}
            if(age>=200)t.putBoolean("NoteArmed",true);
            // After the routine the light goes out by itself; in the dark the House takes the reader on to bed.
            if(t.contains("LightsOutAt")&&age>=t.getInt("LightsOutAt")&&!t.getBoolean("LightOff")){t.putBoolean("LightOff",true);t.putBoolean("BedroomOpen",true);t.putInt("LeaveAt",age+60);lights(p,b,true);var sw=b.offset(HouseReadRemainingRooms.SWITCH_AT);var s=p.serverLevel().getBlockState(sw);if(s.is(HouseReadRegistry.FITTING.get()))p.serverLevel().setBlock(sw,s.setValue(HouseReadPropBlock.TURNED,true),Block.UPDATE_CLIENTS);unlock(p,b,room);HouseReadSites.sound(p,SoundEvents.LEVER_CLICK,sw,.3f);HouseRead.flag(p,BATHROOM,"LightOff");}
            if(t.getBoolean("BedroomOpen")&&(p.getZ()<b.getZ()-10.6&&p.getX()>b.getX()+3&&p.getX()<b.getX()+6||t.contains("LeaveAt")&&age>=t.getInt("LeaveAt"))){save(p,room,t);HouseReadSites.leave(p,true);}
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
    private static void age(ServerPlayer p,String kind,String value){if(visit(p,WAITING).getInt("Age")>=WAIT||seen(p,WAITING).contains("First"))return;HouseRead.first(p,WAITING,"First",kind);HouseRead.signal(p,HouseRead.Trait.AGE,value,false);}
    /** Flipping through more channels settles a reader who began at the cart. */
    private static void channels(ServerPlayer p){var s=seen(p,WAITING);int n=s.getInt("Channels")+1;HouseRead.see(p,WAITING,t->t.putInt("Channels",n));if(visit(p,WAITING).getInt("Age")<WAIT&&!s.contains("Second")&&n>=(s.getString("First").equals("tv")?3:2)&&s.contains("First")){HouseRead.see(p,WAITING,t->{t.putString("Second","TRANSITIONAL");t.putString("Kept","channels");});HouseRead.signal(p,HouseRead.Trait.AGE,"TRANSITIONAL",true);}}
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
        // Changing one's mind to another set's item makes the read uncertain; using the one in hand decides it.
        if(!s.contains("Swapped")&&s.getInt("Item")/3!=item/3){HouseRead.see(p,BATHROOM,t->t.putInt("Swapped",item));HouseRead.signal(p,HouseRead.Trait.HAIR,HAIR[item/3],true);}
    }
    /** The hot tap settles only the set whose washcloth hangs over the faucet; for the others it is an observation. */
    private static void tap(ServerPlayer p){
        var s=seen(p,BATHROOM);HouseRead.flag(p,BATHROOM,"Tap");
        if(!s.getBoolean("LightOff")&&s.contains("Item")&&s.getInt("Item")/3==3&&!s.contains("Used")&&!s.getBoolean("Second")){HouseRead.flag(p,BATHROOM,"Second");HouseRead.signal(p,HouseRead.Trait.HAIR,HAIR[3],true);}
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
