package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Four personal returns: the promise, the taking, the denial, and what is brought home. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BorrascaStory {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.BORRASCA;
    public static final String FRIEND="Kimber";
    public static final int MISSING=10,WORKER=20;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final BlockPos FRONT_LEVER=new BlockPos(-15,13,-168);
    private static final BlockPos[] CHILD_WALK={new BlockPos(1,0,-35),new BlockPos(1,0,-60),new BlockPos(3,0,-86),new BlockPos(3,6,-88),new BlockPos(1,6,-94)};
    private static final BlockPos[] DOWN={new BlockPos(2,6,-89),new BlockPos(2,6,-88),new BlockPos(3,6,-88),new BlockPos(3,0,-88),new BlockPos(2,0,-88),new BlockPos(1,0,-96)};
    private static final BlockPos[] TAKING_WALK={new BlockPos(1,0,-85),new BlockPos(2,0,-93),new BlockPos(3,0,-97),new BlockPos(3,0,-103),BorrascaRooms.trail(-108),BorrascaRooms.SHELTER_ONE,new BlockPos(6,2,-114),new BlockPos(6,3,-116),BorrascaRooms.trail(-125),BorrascaRooms.SHELTER_TWO,new BlockPos(1,6,-131),BorrascaRooms.trail(-141),BorrascaRooms.TAKING};
    private BorrascaStory(){}
    public static void arrive(ServerPlayer p,CompoundTag own){
        int visit=own.getInt("Visit");if(visit==0)visit=1;else if(own.getBoolean("BorrascaDeparted")&&(own.getInt("ConfirmedVisits")&(1<<(visit-1)))!=0)visit=Math.min(4,visit+1);
        own.putInt("Visit",visit);own.putBoolean("BorrascaDeparted",false);own.putBoolean("Here",true);own.putBoolean("Interrupted",false);own.remove("ExitEligible");own.remove("DepartVisit");own.putInt("Present",0);own.putInt("TrailClock",0);own.putBoolean("CastRecalled",false);
        p.displayClientMessage(Component.literal(switch(visit){case 1->"The pine trail. Someone is waiting at the sign.";case 2->"Kimber is waiting by the first marker. The note is beside the door.";case 3->"Years later. You remember the way to the three trees.";default->"The ridge after dark. The way back stays open.";}),false);
        send(p,own);
    }
    public static ItemStack markEvidence(ServerPlayer p,ItemStack s){CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putUUID("BorrascaEvidence",p.getUUID()));return s;}
    public static boolean carriesEvidence(ServerPlayer p,CompoundTag own){return BorrascaBelongings.has(p,own,"Patch");}
    public static void took(ServerPlayer p,String key,BlockPos at){}
    public static void read(ServerPlayer p,String key,CompoundTag own){if(BorrascaSites.inside(p)&&own.getBoolean("Here")&&own.getInt("Visit")==4&&key.equals("Evidence"))own.putBoolean("EvidenceRead",true);}
    public static void held(ServerPlayer p,ItemStack s,String key){
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);
        if(!BorrascaBelongings.valid(p,own,key,s))return;
        // 0.4.198: away from the trail the compass points (Keepsakes.compass); the patch only shows her name.
        if(!BorrascaSites.inside(p)){if(key.equals("Compass")&&own.getBoolean("Taken_Ending"))Keepsakes.compass(p);else p.displayClientMessage(Component.literal(key.equals("Compass")?"The red tape is worn thin around the brass case.":"Her name, stitched in the cloth."),true);return;}
        var b=BorrascaSites.base(p.server,p.getUUID());if(b==null)return;
        if(key.equals("Compass")&&own.getInt("Visit")==3){var sheriff=actor(p,own,"CastSheriff");if(sheriff!=null&&p.distanceToSqr(sheriff)<36){denial(p,sheriff,own);save(p,own);compassChoice(p);return;}}
        if(key.equals("Patch")&&own.getInt("Visit")==4&&near(p,b.offset(BorrascaRooms.CARVING),4)){restoreName(p,b,own);save(p,own);}
    }
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        int visit=own.getInt("Visit");
        if(rel.equals(BorrascaRooms.CARVING)){
            if(visit==1){if(!own.getBoolean("FriendCarved")){p.displayClientMessage(Component.literal("Kimber has kept a space beside her name."),true);return true;}if(!own.getBoolean("PactCarved")){own.putBoolean("PactCarved",true);own.putInt("Beat",1);bark(p,b,2);heard(p,BorrascaRegistry.CARVE.get(),b.offset(rel),.55F);var friend=actor(p,own,"CastKimber");if(friend!=null){friend.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);friend.gesture(2);say(p,friend,"There. Now it has to keep a place for both of us.");}heard(p,BorrascaRegistry.CHIME.get(),b.offset(BorrascaRooms.MACHINE),.35F);p.displayClientMessage(Component.literal("Kimber gestures back down the trail, before the light goes."),false);}return true;}
            if(visit>=3){own.putBoolean("GougeSeen",true);p.displayClientMessage(Component.literal("Fresh pale cuts. A flat steel tool has taken her name out. Yours is still there."),false);if(visit==4)restoreName(p,b,own);}return true;
        }
        if(rel.equals(BorrascaRooms.DROP)||rel.equals(BorrascaRooms.DROP.below())){
            if(visit==2&&own.getBoolean("FriendMissing")){own.putBoolean("CompassExamined",true);BorrascaBelongings.takeAt(p,own,"Compass",b.offset(BorrascaRooms.DROP));own.putInt("Beat",2);p.displayClientMessage(Component.literal("Her compass. The red tape is wet with clay. The heavy tracks lead uphill."),false);}return true;
        }
        if(rel.equals(BorrascaRooms.MARKER_FOUR)||rel.equals(BorrascaRooms.MARKER_FOUR.above())||rel.equals(BorrascaRooms.LOCK)){
            if(visit==3){own.putBoolean("PerimeterSeen",true);p.displayClientMessage(Component.literal("Chain link. A padlock. The vibration travels through the wire."),false);}return true;
        }
        if(rel.equals(BorrascaRooms.PACK)){
            if(visit==4){if(BorrascaBelongings.takeAt(p,own,"Patch",b.offset(rel))&&carriesEvidence(p,own))own.putBoolean("PatchRecovered",true);if(own.getBoolean("CompassConfiscated"))BorrascaBelongings.reclaim(p,own);p.displayClientMessage(Component.literal("The same teal pack. Her name is stitched inside the flap."),false);}return true;
        }
        // 0.4.121: the town's papers. Each visit's noticeboard and the aged notes are their own originals.
        if(rel.equals(BorrascaRooms.TRAIL_MAP)){LiteraryVignettes.open(p,PLACE,own,"Map",BorrascaTexts.document("Map"),b.offset(rel));return true;}
        if(Arrays.asList(BorrascaRooms.BOARD).contains(rel)){LiteraryVignettes.open(p,PLACE,own,BorrascaTexts.boardKey(visit),BorrascaTexts.board(visit),b.offset(rel));return true;}
        if(rel.equals(BorrascaRooms.NOTES)){LiteraryVignettes.open(p,PLACE,own,BorrascaTexts.notesKey(visit>=3),BorrascaTexts.notes(visit>=3),b.offset(rel));return true;}
        if(rel.equals(BorrascaRooms.EVIDENCE)){if(visit==4)LiteraryVignettes.open(p,PLACE,own,"Evidence",BorrascaTexts.document("Evidence",own.getBoolean("CompassEverConfiscated")),b.offset(rel));return true;}
        if(rel.equals(FRONT_LEVER)){if(visit==4){own.putBoolean("OfficeOpened",true);openDoor(p.serverLevel(),b.offset(BorrascaRooms.FACTORY_DOOR),true);}return true;}
        if(rel.equals(BorrascaRooms.POWER)){
            if(visit==4&&!own.getBoolean("PowerOff")){own.putBoolean("PowerOff",true);var state=p.serverLevel().getBlockState(b.offset(BorrascaRooms.MACHINE));if(state.is(BorrascaRegistry.MACHINE.get()))p.serverLevel().setBlock(b.offset(BorrascaRooms.MACHINE),state.setValue(BorrascaMachineBlock.RUNNING,false),F);var lever=p.serverLevel().getBlockState(b.offset(rel));if(lever.is(Blocks.LEVER))p.serverLevel().setBlock(b.offset(rel),lever.setValue(LeverBlock.POWERED,false),F);heard(p,BorrascaRegistry.BREAKER.get(),b.offset(rel),.7F);heard(p,BorrascaRegistry.MOTOR_STOP.get(),b.offset(BorrascaRooms.MACHINE),1);send(p,own);}return true;
        }
        if(rel.equals(BorrascaRooms.RELEASE)){if(visit==4){own.putBoolean("ReleaseOpened",true);openDoor(p.serverLevel(),b.offset(-17,12,-206),true);var s=p.serverLevel().getBlockState(b.offset(rel));if(s.is(Blocks.LEVER))p.serverLevel().setBlock(b.offset(rel),s.setValue(LeverBlock.POWERED,true),F);}return true;}
        return false;
    }
    private static void restoreName(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!own.getBoolean("PowerOff")||!own.getBoolean("EvidenceRead")||!carriesEvidence(p,own)||own.getBoolean("RestoredName"))return;
        own.putBoolean("RestoredName",true);bark(p,b,4);heard(p,BorrascaRegistry.CARVE.get(),b.offset(BorrascaRooms.CARVING),.6F);p.displayClientMessage(Component.literal("Kimber. Beside your name again. The steel cuts remain underneath."),false);
    }
    public static void openDoor(ServerLevel l,BlockPos at,boolean open){for(int y=0;y<2;y++){var s=l.getBlockState(at.above(y));if(s.getBlock() instanceof DoorBlock)l.setBlock(at.above(y),s.setValue(DoorBlock.OPEN,open),F);}}
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        int visit=own.getInt("Visit");
        if(a.role()==LiteraryActor.BORRASCA_KIMBER){
            if(own.getBoolean("FriendMissing")||visit>=3)return;
            if(visit==1){own.putBoolean("MetFriend",true);say(p,a,own.getBoolean("TreeReached")?"The Shiny Gentleman only comes past the second marker. That's what they say.":"Come on. I'll show you where the three trees meet.");}
            else if(own.getBoolean("Read_ContentNote")){own.putBoolean("FollowAgain",true);say(p,a,"Someone scratched another name out. Come and see. Then we'll find where the sound starts.");}
            else say(p,a,"There's a note by your door. I'll wait here.");
        }else if(a.role()==LiteraryActor.BORRASCA_SHERIFF){if(visit<3){a.gesture(1);say(p,a,"You two keep this side of Marker Two when the wind comes up.");own.putBoolean("MetSheriff",true);}else{denial(p,a,own);if(presentingCompass(p,own))compassChoice(p);}}
        else if(a.role()==LiteraryActor.BORRASCA_PRESCOTT&&a.phase()<WORKER){own.putBoolean("MetPrescott",true);say(p,a,visit==1?"Evening. Tell your folks the delivery's at the trailhead.":"This is a work entrance.");}
    }
    private static boolean presentingCompass(ServerPlayer p,CompoundTag own){return BorrascaBelongings.valid(p,own,"Compass",p.getMainHandItem())||BorrascaBelongings.valid(p,own,"Compass",p.getOffhandItem());}
    private static void denial(ServerPlayer p,LiteraryActor sheriff,CompoundTag own){if(own.getBoolean("HeardDenial"))return;own.putBoolean("HeardDenial",true);sheriff.gesture(2);say(p,sheriff,presentingCompass(p,own)?"Put that in my hand. There was nobody up there with you. You should have stayed this side of Marker Three.":"There is no case to reopen. You were alone up there. You should have stayed this side of Marker Three.");}
    private static void compassChoice(ServerPlayer p){
        var icons=new HashMap<Integer,ItemStack>();icons.put(0,LiteraryChoiceMenu.icon(BorrascaRegistry.COMPASS.get(),"Hand over the compass"));icons.put(8,LiteraryChoiceMenu.icon(Items.PAPER,"Keep it"));
        LiteraryChoiceMenu.open(p,"The Sheriff holds out his hand",icons,()->BorrascaSites.inside(p)&&near(p,BorrascaSites.base(p.server,p.getUUID()).offset(BorrascaRooms.SHERIFF),8),slot->{if(slot==8)return true;if(slot!=0)return false;var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);if(own.getInt("Visit")==3)BorrascaBelongings.confiscate(p,own);save(p,own);return true;});
    }
    private static @Nullable LiteraryActor actor(ServerPlayer p,CompoundTag own,String key){return own.hasUUID(key)&&p.serverLevel().getEntity(own.getUUID(key)) instanceof LiteraryActor a?a:null;}
    private static LiteraryActor ensure(ServerPlayer p,BlockPos b,CompoundTag own,String key,int role){
        var known=actor(p,own,key);if(known!=null||own.hasUUID(key))return known;var at=b.offset(BorrascaCast.target(key,own.getInt("Visit")));if(!p.serverLevel().areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return null;
        var a=LiteraryRegistry.ACTOR.get().create(p.serverLevel());if(a==null)return null;a.bind(PLACE,p.getUUID());a.appearance(role,key.startsWith("Worker")?WORKER:0);a.moveTo(Vec3.atBottomCenterOf(at),180,0);if(!p.serverLevel().addFreshEntity(a))return null;own.putUUID(key,a.getUUID());return a;
    }
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        if(LabyrinthDoors.isBusy(p))return;if(!own.getBoolean("Here")){if(own.getBoolean("BorrascaDeparted"))return;own.putBoolean("Here",true);own.remove("ExitEligible");}
        own.putInt("TrailClock",(own.getInt("TrailClock")+5)%60000);int visit=own.getInt("Visit");if(!own.getBoolean("CastRecalled"))BorrascaCast.prepare(p,b,own);
        var friend=ensure(p,b,own,"CastKimber",LiteraryActor.BORRASCA_KIMBER);var sheriff=ensure(p,b,own,"CastSheriff",LiteraryActor.BORRASCA_SHERIFF);var prescott=ensure(p,b,own,"CastPrescott",LiteraryActor.BORRASCA_PRESCOTT);
        var w0=ensure(p,b,own,"Worker0",LiteraryActor.BORRASCA_KYLE);var w1=ensure(p,b,own,"Worker1",LiteraryActor.BORRASCA_KYLE);
        if(!own.getBoolean("CastRecalled")&&friend!=null&&sheriff!=null&&prescott!=null&&w0!=null&&w1!=null){
            boolean all=true;for(String key:BorrascaCast.KEYS){var a=actor(p,own,key);all&=BorrascaCast.recall(p,a,b.offset(BorrascaCast.destination(own,key,b)));}if(all){own.putBoolean("CastRecalled",true);own.putInt("CastVisit",visit);BorrascaCast.release(p.getUUID());}
        }
        for(String key:BorrascaCast.KEYS)BorrascaCast.remember(own,key,actor(p,own,key));
        scene(p,b,own);
        if(!own.getBoolean("CastRecalled"))return;
        if(visit==1){
            if(sheriff!=null&&!own.getBoolean("MetSheriff")&&near(p,sheriff.blockPosition(),8)){talk(p,sheriff,own);own.putInt("HatTicks",40);}
            if(sheriff!=null&&own.getInt("HatTicks")>0){int hat=Math.max(0,own.getInt("HatTicks")-5);own.putInt("HatTicks",hat);if(hat==0)sheriff.gesture(0);}
            if(friend!=null&&!own.getBoolean("MetFriend")&&near(p,friend.blockPosition(),5))talk(p,friend,own);
            if(prescott!=null&&!own.getBoolean("MetPrescott")&&p.getZ()<b.getZ()-22&&p.getZ()>b.getZ()-34&&near(p,prescott.blockPosition(),13))talk(p,prescott,own);
        }
        if(friend!=null){
            if(visit>=3||own.getBoolean("FriendMissing"))missing(p,b,own,friend);
            else{friend.setInvisible(false);friend.appearance(LiteraryActor.BORRASCA_KIMBER,0);if(!own.getBoolean("CompassIssued"))friend.setItemSlot(EquipmentSlot.OFFHAND,BorrascaBelongings.original(p,own,"Compass"));if(visit==1)pact(p,b,own,friend);else{friend.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);friend.gesture(0);taking(p,b,own,friend);}}
        }
        if(visit>=3&&sheriff!=null){sheriff.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(BorrascaRegistry.FLASHLIGHT.get()));if(visit==3)blockade(p,b,own,sheriff);}
        if(visit==2&&own.getBoolean("FriendMissing")&&BorrascaBelongings.has(p,own,"Compass")){own.putBoolean("CompassExamined",true);own.putInt("Beat",2);}
        if(visit==3){int step=own.getInt("GullyStep");if(step<BorrascaRooms.GULLY.length&&p.isCrouching()&&near(p,b.offset(BorrascaRooms.GULLY[step]),3))own.putInt("GullyStep",step+1);if(near(p,b.offset(BorrascaRooms.MARKER_FOUR),9))own.putBoolean("PerimeterSeen",true);if(own.getBoolean("GougeSeen")&&own.getBoolean("HeardDenial")&&own.getBoolean("PerimeterSeen")&&own.getInt("GullyStep")==BorrascaRooms.GULLY.length)own.putInt("Beat",3);}
        if(visit==4){
            if(!own.getBoolean("PowerOff")&&own.getInt("TrailClock")%200==0)heard(p,BorrascaRegistry.CHIME.get(),b.offset(BorrascaRooms.MACHINE),1);
            if(carriesEvidence(p,own))own.putBoolean("PatchRecovered",true);
            if(own.getBoolean("ReleaseOpened")&&p.isCrouching()&&p.getX()>b.getX()-22&&p.getX()<b.getX()-14&&p.getZ()<b.getZ()-207&&p.getZ()>b.getZ()-211)own.putBoolean("CrouchedUnderPipe",true);
            if(own.getBoolean("PowerOff")&&own.getBoolean("EvidenceRead")&&own.getBoolean("PatchRecovered")&&carriesEvidence(p,own)&&(own.getInt("ConfirmedVisits")&7)==7&&p.getZ()>b.getZ()-106){own.putBoolean("EscapedWithOriginal",true);LiteraryVignettes.ready(p,PLACE,own,"silenced_the_mountain");}
        }
        if(own.getInt("Present")%20==0||own.getInt("TakingTicks")>0&&own.getInt("TakingTicks")<=60)send(p,own);
    }
    private static void missing(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend){
        friend.appearance(LiteraryActor.BORRASCA_KIMBER,MISSING);friend.setCustomNameVisible(false);friend.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
        if(own.getInt("Visit")>=3||own.getBoolean("FriendHidden")||!LiteraryVignettes.watched(p.serverLevel(),friend.getEyePosition())){friend.setInvisible(true);own.putBoolean("FriendHidden",true);BorrascaCast.stop(friend);return;}
        var corner=b.offset(-12,9,-146);BorrascaCast.walk(friend,Vec3.atBottomCenterOf(friend.getX()>corner.getX()+.5?corner:b.offset(BorrascaRooms.HIDE)),1.25);
    }
    private static void pact(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend){
        if(!own.getBoolean("MetFriend"))return;
        if(!own.getBoolean("TreeReached")){
            follow(p,b,own,friend,"ChildStep",CHILD_WALK);if(p.getY()>=b.getY()+5.9&&p.getZ()<b.getZ()-89&&p.getZ()>b.getZ()-100&&Math.abs(p.getX()-b.getX())<6&&own.getInt("ChildStep")>=CHILD_WALK.length){own.putBoolean("TreeReached",true);own.putInt("LegendTicks",0);say(p,friend,"They call him the Shiny Gentleman. Past the second marker. When you hear him, come down before dark.");}return;
        }
        int time=own.getInt("LegendTicks")+5;own.putInt("LegendTicks",time);if(time<80){BorrascaCast.stop(friend);return;}
        if(!own.getBoolean("FriendCarved")){
            if(!follow(p,b,own,friend,"DownStep",DOWN))return;friend.gesture(3);friend.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(LiteraryRegistry.KNIFE.get()));int n=own.getInt("CarveTicks")+5;own.putInt("CarveTicks",n);if(n>=40){own.putBoolean("FriendCarved",true);bark(p,b,1);friend.gesture(4);heard(p,BorrascaRegistry.CARVE.get(),b.offset(BorrascaRooms.CARVING),.5F);say(p,friend,"Your name beside mine. So the tree keeps us both.");}
        }
    }
    private static boolean follow(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend,String key,BlockPos[] points){
        int i=own.getInt(key);if(i>=points.length){BorrascaCast.stop(friend);return true;}var at=Vec3.atBottomCenterOf(b.offset(points[i]));if(friend.distanceToSqr(p)>18*18&&p.position().subtract(friend.position()).dot(at.subtract(friend.position()))<0){BorrascaCast.stop(friend);return false;}
        var delta=friend.position().subtract(at);if(delta.horizontalDistance()<.4&&Math.abs(delta.y)<.14){own.putInt(key,i+1);BorrascaCast.stop(friend);}else BorrascaCast.walk(friend,at,.8);return own.getInt(key)>=points.length;
    }
    private static void taking(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend){
        if(!own.getBoolean("Read_ContentNote")||!own.getBoolean("FollowAgain"))return;
        int clock=own.getInt("TakingTicks");
        if(clock>0){
            clock=Math.min(61,clock+5);own.putInt("TakingTicks",clock);var hide=b.offset(BorrascaRooms.HIDE);var corner=b.offset(-12,9,-146);BorrascaCast.walk(friend,friend.getX()>corner.getX()+.5?Vec3.atBottomCenterOf(corner):Vec3.atBottomCenterOf(hide),1.25);
            if(clock>=60){own.putBoolean("FriendMissing",true);friend.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);BorrascaBelongings.dropOnce(p,own,"Compass",b.offset(BorrascaRooms.DROP));fit(p,b,BorrascaRooms.DROP.below(),BorrascaFittingBlock.Kind.TRAIL);for(int i=0;i<8;i++)fit(p,b,new BlockPos(1,BorrascaRooms.trailY(-152-i),-152-i),BorrascaFittingBlock.Kind.TRACKS);}
            return;
        }
        int step=own.getInt("TakingStep");
        if(step==6||step==10){int t=own.getInt("Shelter"+step);if(p.isCrouching()&&p.distanceToSqr(friend)<25)own.putBoolean("Sheltered"+step,true);if(t<60||!own.getBoolean("Sheltered"+step)){friend.gesture(5);BorrascaCast.stop(friend);if(t==0){heard(p,BorrascaRegistry.CHIME.get(),b.offset(BorrascaRooms.MACHINE),.8F);dust(p);say(p,friend,"Down here. Behind the timber.");}own.putInt("Shelter"+step,Math.min(60,t+5));return;}}
        friend.gesture(0);if(follow(p,b,own,friend,"TakingStep",TAKING_WALK)&&near(p,b.offset(BorrascaRooms.TAKING),8)){own.putInt("TakingTicks",1);friend.gesture(2);say(p,friend,"Wait. Is that someone—");heard(p,BorrascaRegistry.CHIME.get(),b.offset(BorrascaRooms.MACHINE),1.6F);dust(p);send(p,own);}
    }
    public static boolean masking(CompoundTag own){return own.getInt("TrailClock")%200<60;}
    private static void blockade(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor sheriff){
        if(near(p,sheriff.blockPosition(),6))denial(p,sheriff,own);
        if(own.getInt("TrailClock")%200==0){heard(p,BorrascaRegistry.CHIME.get(),b.offset(BorrascaRooms.MACHINE),.8F);dust(p);}
        if(own.getBoolean("Escorting")){
            // 0.4.165: the Sheriff stops on the walk's end himself, so the reader is let go a block short of it, at the near side of
            // the tape; pressed against his body they could never reach his own spot (run 1330).
            var end=Vec3.atBottomCenterOf(b.offset(2,4,-119));if(p.getZ()>=end.z-1&&sheriff.position().distanceToSqr(end)<4){own.putBoolean("Escorting",false);own.putInt("CaughtCooldown",80);BorrascaCast.stop(sheriff);return;}
            if(sheriff.distanceToSqr(p)>9)BorrascaCast.walk(sheriff,p.position(),.6);else{BorrascaCast.walk(sheriff,end,.65);Vec3 delta=end.subtract(p.position()).multiply(1,0,1);if(delta.lengthSqr()>.16){p.push(delta.normalize().x*.045,0,delta.normalize().z*.045);p.hurtMarked=true;}}return;
        }
        int cooldown=Math.max(0,own.getInt("CaughtCooldown")-5);own.putInt("CaughtCooldown",cooldown);BorrascaCast.walk(sheriff,Vec3.atBottomCenterOf(b.offset(BorrascaRooms.SHERIFF)),.65);
        float yaw=flashlightYaw(own);sheriff.setYRot(yaw);sheriff.setYBodyRot(yaw);sheriff.setYHeadRot(yaw);sheriff.gesture(4);
        boolean onFarSide=p.getZ()<b.getZ()-123&&p.getZ()>b.getZ()-141&&p.getX()>b.getX()-20&&p.getX()<b.getX()+12;
        boolean caught=onFarSide&&cooldown==0&&sees(sheriff,p.getEyePosition())&&((flashlightSees(sheriff,p,yaw))||!p.isCrouching()&&!masking(own)&&sheriff.distanceToSqr(p)<14*14);
        int suspicion=caught?own.getInt("Suspicion")+5:Math.max(0,own.getInt("Suspicion")-5);own.putInt("Suspicion",suspicion);
        if(suspicion==10)say(p,sheriff,"Hold it. I can see you.");if(suspicion>=30){own.putBoolean("Escorting",true);own.putInt("Suspicion",0);sheriff.gesture(2);say(p,sheriff,"Back to the tape. Walk.");}
    }
    public static float flashlightYaw(CompoundTag own){return 180+55*(float)Math.sin(own.getInt("TrailClock")*.018);}
    public static boolean flashlightSees(LiteraryActor sheriff,ServerPlayer p,float yaw){Vec3 d=p.getEyePosition().subtract(sheriff.getEyePosition());if(d.lengthSqr()>22*22||d.lengthSqr()<.01)return false;Vec3 direction=new Vec3(-Math.sin(Math.toRadians(yaw)),0,Math.cos(Math.toRadians(yaw)));return direction.dot(d.normalize())>.965&&sees(sheriff,p.getEyePosition());}
    private static boolean sees(LiteraryActor a,Vec3 at){var hit=a.level().clip(new ClipContext(a.getEyePosition(),at,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a));return hit.getType()==HitResult.Type.MISS;}
    private static void scene(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean("PowerOff")&&p.serverLevel().hasChunkAt(b.offset(BorrascaRooms.MACHINE))){var at=b.offset(BorrascaRooms.MACHINE);var s=p.serverLevel().getBlockState(at);if(s.is(BorrascaRegistry.MACHINE.get())&&s.getValue(BorrascaMachineBlock.RUNNING))p.serverLevel().setBlock(at,s.setValue(BorrascaMachineBlock.RUNNING,false),F);var lamp=b.offset(BorrascaRooms.LAMP);var light=p.serverLevel().getBlockState(lamp);if(light.is(BorrascaRegistry.FITTING.get())&&light.getValue(BorrascaFittingBlock.KIND)==BorrascaFittingBlock.Kind.LAMP)fit(p,b,BorrascaRooms.LAMP,BorrascaFittingBlock.Kind.LAMP_OFF);}
        int visit=own.getInt("Visit");if(!p.serverLevel().hasChunkAt(b.offset(BorrascaRooms.CARVING))||visit==4&&!p.serverLevel().hasChunkAt(b.offset(BorrascaRooms.GATE)))return;
        int stage=own.getBoolean("RestoredName")?4:visit>=3?3:own.getBoolean("PactCarved")?2:own.getBoolean("FriendCarved")?1:0;bark(p,b,stage);
        if(own.getInt("SceneryVisit")==visit)return;
        if(visit>=3){for(int x=-5;x<=5;x++)for(int y=5;y<=11;y++)for(int z=-100;z<=-88;z++){var s=p.serverLevel().getBlockState(b.offset(x,y,z));if(s.is(Blocks.SPRUCE_PLANKS)||s.is(Blocks.BIRCH_PLANKS)||s.is(Blocks.OAK_PLANKS))fit(p,b,new BlockPos(x,y,z),BorrascaFittingBlock.Kind.AGED_WOOD);}for(int x=-1;x<=5;x++)fit(p,b,new BlockPos(x,5,-125),BorrascaFittingBlock.Kind.TAPE);}
        if(visit==2){p.serverLevel().setBlock(b.offset(2,0,-98),BorrascaRegistry.CARVING.get().defaultBlockState().setValue(BorrascaCarvingBlock.STAGE,3),F);var source=b.offset(BorrascaRooms.SOURCE);if(p.serverLevel().getBlockEntity(source) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity lectern){lectern.setBook(BorrascaTexts.contentNote());lectern.setChanged();p.serverLevel().sendBlockUpdated(source,lectern.getBlockState(),lectern.getBlockState(),F);}}
        if(visit==4){for(int x=-1;x<=1;x++)for(int y=12;y<=14;y++)p.serverLevel().setBlock(b.offset(x,y,-167),Blocks.AIR.defaultBlockState(),F);p.serverLevel().setBlock(b.offset(BorrascaRooms.LOCK),Blocks.AIR.defaultBlockState(),F);fit(p,b,new BlockPos(2,12,-168),BorrascaFittingBlock.Kind.LOCK);openDoor(p.serverLevel(),b.offset(BorrascaRooms.FACTORY_DOOR),true);}
        own.putInt("SceneryVisit",visit);
    }
    private static void bark(ServerPlayer p,BlockPos b,int stage){var at=b.offset(BorrascaRooms.CARVING);var s=p.serverLevel().getBlockState(at);if(s.is(BorrascaRegistry.CARVING.get())&&s.getValue(BorrascaCarvingBlock.STAGE)!=stage)p.serverLevel().setBlock(at,s.setValue(BorrascaCarvingBlock.STAGE,stage),F);BorrascaCarvingEntity.names(p.serverLevel(),at,p.getGameProfile().getName());}
    private static void fit(ServerPlayer p,BlockPos b,BlockPos rel,BorrascaFittingBlock.Kind kind){p.serverLevel().setBlock(b.offset(rel),BorrascaFittingBlock.of(kind,Direction.SOUTH),F);}
    private static boolean near(ServerPlayer p,BlockPos at,double distance){return at!=null&&p.distanceToSqr(Vec3.atBottomCenterOf(at))<distance*distance;}
    private static void say(ServerPlayer p,LiteraryActor a,String words){a.say(words);p.displayClientMessage(Component.literal((a.role()==LiteraryActor.BORRASCA_KIMBER?FRIEND:a.role()==LiteraryActor.BORRASCA_SHERIFF?"Sheriff":"Prescott")+": "+words),false);}
    private static void heard(ServerPlayer p,net.minecraft.sounds.SoundEvent sound,BlockPos at,float volume){LiteraryVignettes.heard(p,sound,at,volume,1);}
    private static void dust(ServerPlayer p){p.serverLevel().sendParticles(p,ParticleTypes.ASH,false,p.getX(),p.getY()+.4,p.getZ(),32,2,.5,2,.03);}
    private static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    private static void send(ServerPlayer p,CompoundTag own){var b=BorrascaSites.base(p.server,p.getUUID());var sheriff=actor(p,own,"CastSheriff");if(b!=null)HousePackets.send(p,new BorrascaViewPayload(b.offset(BorrascaRooms.MACHINE),own.getInt("Visit"),own.getBoolean("Here"),!own.getBoolean("PowerOff")&&(own.getInt("Visit")>1||own.getBoolean("PactCarved")),own.getInt("TakingTicks"),own.getInt("TrailClock"),sheriff==null?b.offset(BorrascaRooms.SHERIFF):sheriff.blockPosition(),flashlightYaw(own)));}
    /** Per-native-tick motion; native collision and ladder physics remain enabled. */
    public static void actorTick(LiteraryActor a){if(!a.scene().equals(PLACE.id())||!(a.level() instanceof ServerLevel l))return;var p=a.owner().map(l.getServer().getPlayerList()::getPlayer).orElse(null);if(p==null||!BorrascaSites.inside(p)){BorrascaCast.stop(a);return;}var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);if(!own.getBoolean("Here")||!own.getBoolean("CastRecalled"))BorrascaCast.stop(a);}
    /** Capture intent; the actual door travel callback is the sole return authority. */
    public static void depart(ServerPlayer p){BorrascaCast.release(p.getUUID());var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);boolean valid=BorrascaSites.inside(p)&&own.getBoolean("Here")&&!own.getBoolean("Interrupted")&&p.isAlive();if(valid&&own.getInt("Beat")>=own.getInt("Visit"))own.putInt("DepartVisit",own.getInt("Visit"));else own.remove("DepartVisit");own.putBoolean("ExitEligible",valid&&own.getInt("Visit")==4&&(own.getInt("ConfirmedVisits")&7)==7&&own.getBoolean("Ready")&&own.getBoolean("PowerOff")&&own.getBoolean("EvidenceRead")&&carriesEvidence(p,own));own.putBoolean("Here",false);save(p,own);}
    public static void returned(ServerPlayer p){var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("BorrascaDeparted",true);own.putBoolean("Escorting",false);own.putInt("Suspicion",0);int returned=own.getInt("DepartVisit");if(returned>=1&&returned<=3){own.putInt("ConfirmedVisits",own.getInt("ConfirmedVisits")|(1<<(returned-1)));own.putInt("CastNextVisit",returned+1);}if(own.getBoolean("ExitEligible")&&carriesEvidence(p,own)){own.putBoolean("NameReturned",own.getBoolean("RestoredName"));own.putBoolean("Completed",true);own.putInt("Beat",4);save(p,own);WitnessAccount.resolve(p,WitnessAccount.Story.BORRASCA,"silenced_the_mountain");d.setCompleted(PLACE.id(),true);WitnessAccount.updateBook(p,false);}own.remove("ExitEligible");own.remove("DepartVisit");save(p,own);}
    @SubscribeEvent public static void joined(EntityJoinLevelEvent event){if(!(event.getEntity() instanceof LiteraryActor a)||!(event.getLevel() instanceof ServerLevel l)||!a.scene().equals(PLACE.id())||a.role()!=LiteraryActor.BORRASCA_KIMBER||a.owner().isEmpty())return;var own=LiteraryVignettes.personal(LabyrinthData.get(l.getServer()),a.owner().get(),PLACE);if(own.getInt("Visit")>=3||own.getBoolean("FriendMissing")){a.appearance(LiteraryActor.BORRASCA_KIMBER,MISSING);a.setCustomNameVisible(false);a.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);if(own.getInt("Visit")>=3||own.getBoolean("FriendHidden")){a.setInvisible(true);BorrascaCast.stop(a);}}}
    @SubscribeEvent public static void rejoined(PlayerEvent.PlayerLoggedInEvent event){if(!(event.getEntity() instanceof ServerPlayer p)||!BorrascaSites.inside(p))return;var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);if(own.getBoolean("Here")){own.putBoolean("CastRecalled",false);save(p,own);}}
    @SubscribeEvent public static void died(LivingDeathEvent event){if(event.getEntity() instanceof ServerPlayer p&&BorrascaSites.inside(p)){BorrascaCast.release(p.getUUID());var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);own.putBoolean("Here",false);own.remove("ExitEligible");own.remove("DepartVisit");save(p,own);}}
}
