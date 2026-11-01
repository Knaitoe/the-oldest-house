package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
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

/**
 * Four personal returns: the promise, the taking, the denial, and what is brought home.
 *
 * <p>0.4.235 (docs/BORRASCA_MODULAR_0_4_235.md): the owner, 1 November: "the vignette needs to be more modular, rather than assuming
 * the player isn't going to go explore the entire map as soon as they enter." Every beat now waits for its place, not for a sequence.
 * The friend goes to the Tree at her own pace and tells the legend whenever the reader is there with her; the taking fires whenever both
 * of them reach the clearing, and she calls the reader back if they wander off; the gouge is noticed by looking at it; the blockade is
 * passed by any route the Sheriff does not see, and he gives himself away as soon as he has the reader in sight. The refinery is
 * sealed until the last night. Men come down the ridge road in the light and lead her away. The friend is a new child, Nell: the owner, 1 November, "the vignette was supposed to occupy a separate cast in the same environment... the events of borrasca had to have played out for many, many casts of kids." Only the Sheriff and the Prescott family last across the generations. People speak above their heads and on
 * the action bar, never in chat.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BorrascaStory {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.BORRASCA;
    /** 0.4.235: a child of a later Drisking generation than the original story's; only the Sheriff and the Prescotts last that long. */
    public static final String FRIEND="Nell";
    public static final int MISSING=10,WORKER=20;
    /** The taking: the longest the men may take to reach her before she is led anyway, and how long she is led before she is gone. */
    static final int TAKEN_BY=160,LED_FOR=25;
    /** 0.4.235 gestures: the friend led by the arm, and a taker's hand held out to her. */
    public static final int LED=6,TAKING_HAND=7;
    /** How long a spoken line stays over the speaker's head. */
    public static final int SPEECH_TICKS=100;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final BlockPos FRONT_LEVER=new BlockPos(-15,13,-168);
    private static final BlockPos[] CHILD_WALK={new BlockPos(1,0,-35),new BlockPos(1,0,-60),new BlockPos(3,0,-86),new BlockPos(3,6,-88),new BlockPos(1,6,-94)};
    private static final BlockPos[] DOWN={new BlockPos(2,6,-89),new BlockPos(2,6,-88),new BlockPos(3,6,-88),new BlockPos(3,0,-88),new BlockPos(2,0,-88),new BlockPos(1,0,-96)};
    private static final BlockPos[] TAKING_WALK={new BlockPos(1,0,-85),new BlockPos(2,0,-93),new BlockPos(3,0,-97),new BlockPos(3,0,-103),BorrascaRooms.trail(-108),BorrascaRooms.SHELTER_ONE,new BlockPos(6,2,-114),new BlockPos(6,3,-116),BorrascaRooms.trail(-125),BorrascaRooms.SHELTER_TWO,new BlockPos(1,6,-131),BorrascaRooms.trail(-141),BorrascaRooms.TAKING};
    /** The fort the friend climbs to, and the point she tells the legend from. */
    public static final BlockPos FORT=new BlockPos(1,6,-94);
    /** Where the takers come down the ridge road from, in the light's direction, and where they lead her: up the trail toward the wire. */
    static final BlockPos[] TAKERS_FROM={new BlockPos(-3,12,-161),new BlockPos(1,12,-163),new BlockPos(2,12,-165),new BlockPos(-1,12,-165)};
    static final BlockPos LED_TO=new BlockPos(1,12,-163);
    /** The second marker's line, and how far past it the Sheriff watches. */
    static final int MARKER_TWO_Z=-124,TAPE_Z=-125;
    private BorrascaStory(){}

    public static void arrive(ServerPlayer p,CompoundTag own){
        int visit=own.getInt("Visit");if(visit==0)visit=1;else if(own.getBoolean("BorrascaDeparted")&&(own.getInt("ConfirmedVisits")&(1<<(visit-1)))!=0)visit=Math.min(4,visit+1);
        own.putInt("Visit",visit);own.putBoolean("BorrascaDeparted",false);own.putBoolean("Here",true);own.putBoolean("Interrupted",false);own.remove("ExitEligible");own.remove("DepartVisit");own.putInt("Present",0);own.putInt("TrailClock",0);own.putBoolean("CastRecalled",false);
        cue(p,switch(visit){case 1->"The pine trail. Someone is waiting at the sign.";case 2->FRIEND+" is waiting by the first marker. A note is waiting by the door.";case 3->"Years later. You remember the way to the three trees.";default->"The ridge after dark. The way back stays open.";});
        send(p,own);
    }
    public static ItemStack markEvidence(ServerPlayer p,ItemStack s){CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putUUID("BorrascaEvidence",p.getUUID()));return s;}
    public static boolean carriesEvidence(ServerPlayer p,CompoundTag own){return BorrascaBelongings.has(p,own,"Patch");}
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

    // ------------------------------------------------------------------ hands
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        int visit=own.getInt("Visit");
        if(rel.equals(BorrascaRooms.CARVING)){
            if(visit==1){
                if(!own.getBoolean("FriendCarved")){cue(p,own.getBoolean("MetFriend")?"Smooth bark at the foot of the three trunks. "+FRIEND+" said this is where the names go.":"Smooth bark at the foot of the three trunks. Old names, worn soft.");return true;}
                if(!own.getBoolean("PactCarved")){
                    own.putBoolean("PactCarved",true);own.putInt("Beat",1);bark(p,b,2);heard(p,BorrascaRegistry.CARVE.get(),b.offset(rel),.55F);
                    var friend=actor(p,own,"CastKimber");if(friend!=null){friend.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);friend.gesture(2);say(p,friend,"There. Now it has to keep a place for both of us.");}
                    echo(p,b,own,.35F);own.putBoolean("PactEcho",true);
                    return true;
                }
                cue(p,"Both names, side by side in the bark.");return true;
            }
            if(visit==2){cue(p,"Both names are still there. Lower down the trunk, someone has scraped another name away.");return true;}
            noticeGouge(p,own);if(visit==4)restoreName(p,b,own);
            if(own.getBoolean("RestoredName"))cue(p,FRIEND+". Cut again beside your name. The steel gouges are still under it.");
            return true;
        }
        if(rel.equals(BorrascaRooms.DROP)||rel.equals(BorrascaRooms.DROP.below())){
            if(visit==2&&own.getBoolean("FriendMissing")){
                own.putBoolean("CompassExamined",true);own.putInt("Beat",2);
                boolean carried=BorrascaBelongings.takeAt(p,own,"Compass",b.offset(BorrascaRooms.DROP));
                cue(p,carried?"Her compass. The red tape is wet with clay. The heavy tracks lead uphill.":"Her compass, in the clay. Your hands are too full to carry it. The heavy tracks lead uphill.");
            }
            return true;
        }
        if(rel.equals(BorrascaRooms.MARKER_FOUR)||rel.equals(BorrascaRooms.MARKER_FOUR.above())||rel.equals(BorrascaRooms.LOCK)){
            if(visit==3){perimeter(p,own);return true;}
            if(visit<3)cue(p,"Chain link and a padlock. The wire hums under your hand.");
            return true;
        }
        if(rel.equals(BorrascaRooms.PACK)){
            if(visit==4){if(BorrascaBelongings.takeAt(p,own,"Patch",b.offset(rel))&&carriesEvidence(p,own))own.putBoolean("PatchRecovered",true);if(own.getBoolean("CompassConfiscated"))BorrascaBelongings.reclaim(p,own);cue(p,"The same teal pack. Her name is stitched inside the flap.");}
            return true;
        }
        // 0.4.121: the town's papers. Each visit's noticeboard and the aged notes are their own originals.
        if(rel.equals(BorrascaRooms.TRAIL_MAP)){LiteraryVignettes.open(p,PLACE,own,"Map",BorrascaTexts.document("Map"),b.offset(rel));return true;}
        if(Arrays.asList(BorrascaRooms.BOARD).contains(rel)){LiteraryVignettes.open(p,PLACE,own,BorrascaTexts.boardKey(visit),BorrascaTexts.board(visit),b.offset(rel));return true;}
        if(rel.equals(BorrascaRooms.NOTES)){LiteraryVignettes.open(p,PLACE,own,BorrascaTexts.notesKey(visit>=3),BorrascaTexts.notes(visit>=3),b.offset(rel));return true;}
        if(rel.equals(BorrascaRooms.EVIDENCE)){if(visit==4)LiteraryVignettes.open(p,PLACE,own,"Evidence",BorrascaTexts.document("Evidence",own.getBoolean("CompassEverConfiscated")),b.offset(rel));return true;}
        if(rel.equals(FRONT_LEVER)){if(visit==4)openDoor(p.serverLevel(),b.offset(BorrascaRooms.FACTORY_DOOR),true);return true;}
        if(rel.equals(BorrascaRooms.POWER)){
            if(visit==4&&!own.getBoolean("PowerOff")){own.putBoolean("PowerOff",true);var state=p.serverLevel().getBlockState(b.offset(BorrascaRooms.MACHINE));if(state.is(BorrascaRegistry.MACHINE.get()))p.serverLevel().setBlock(b.offset(BorrascaRooms.MACHINE),state.setValue(BorrascaMachineBlock.RUNNING,false),F);var lever=p.serverLevel().getBlockState(b.offset(rel));if(lever.is(Blocks.LEVER))p.serverLevel().setBlock(b.offset(rel),lever.setValue(LeverBlock.POWERED,false),F);heard(p,BorrascaRegistry.BREAKER.get(),b.offset(rel),.7F);heard(p,BorrascaRegistry.MOTOR_STOP.get(),b.offset(BorrascaRooms.MACHINE),1);send(p,own);}
            return true;
        }
        if(rel.equals(BorrascaRooms.RELEASE)){
            if(visit==4){own.putBoolean("ReleaseOpened",true);openDoor(p.serverLevel(),b.offset(BorrascaRooms.SERVICE_DOOR),true);openDoor(p.serverLevel(),b.offset(BorrascaRooms.SERVICE_GATE),true);var s=p.serverLevel().getBlockState(b.offset(rel));if(s.is(Blocks.LEVER))p.serverLevel().setBlock(b.offset(rel),s.setValue(LeverBlock.POWERED,true),F);}
            return true;
        }
        return false;
    }
    private static void restoreName(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!own.getBoolean("PowerOff")||!own.getBoolean("EvidenceRead")||!carriesEvidence(p,own)||own.getBoolean("RestoredName"))return;
        own.putBoolean("RestoredName",true);bark(p,b,4);heard(p,BorrascaRegistry.CARVE.get(),b.offset(BorrascaRooms.CARVING),.6F);
    }
    public static void openDoor(ServerLevel l,BlockPos at,boolean open){for(int y=0;y<2;y++){var s=l.getBlockState(at.above(y));if(s.getBlock() instanceof DoorBlock)l.setBlock(at.above(y),s.setValue(DoorBlock.OPEN,open),F);}}

    // ------------------------------------------------------------------ words
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        int visit=own.getInt("Visit");
        if(a.role()==LiteraryActor.BORRASCA_KIMBER){
            if(own.getBoolean("FriendMissing")||visit>=3)return;
            if(visit==1){own.putBoolean("MetFriend",true);say(p,a,own.getBoolean("LegendDone")?"Don't go past the second marker. I mean it.":own.getBoolean("KimberAtFort")?"Up here. Come and sit.":"Come on. I'll show you where the three trees meet.");}
            else if(own.getBoolean("Read_ContentNote")){own.putBoolean("FollowAgain",true);say(p,a,"Someone scratched another name out. Come and see. Then we'll find where the sound starts.");}
            else{say(p,a,"I'll wait here.");house(p,"A note is waiting by the door.");}
        }else if(a.role()==LiteraryActor.BORRASCA_SHERIFF){if(visit<3){a.gesture(1);say(p,a,"You two keep this side of Marker Two when the wind comes up.");own.putBoolean("MetSheriff",true);}else{denial(p,a,own);if(presentingCompass(p,own))compassChoice(p);}}
        else if(a.role()==LiteraryActor.BORRASCA_PRESCOTT&&a.phase()<WORKER){own.putBoolean("MetPrescott",true);say(p,a,visit==1?"Evening. Tell your folks the delivery's at the trailhead.":"This is a work entrance.");}
    }
    private static boolean presentingCompass(ServerPlayer p,CompoundTag own){return BorrascaBelongings.valid(p,own,"Compass",p.getMainHandItem())||BorrascaBelongings.valid(p,own,"Compass",p.getOffhandItem());}
    private static void denial(ServerPlayer p,LiteraryActor sheriff,CompoundTag own){if(own.getBoolean("HeardDenial"))return;own.putBoolean("HeardDenial",true);sheriff.gesture(2);say(p,sheriff,presentingCompass(p,own)?"Put that in my hand. There was nobody up there with you. You should have stayed this side of Marker Three.":"There is no case to reopen. You were alone up there. You should have stayed this side of Marker Three.");}
    private static void compassChoice(ServerPlayer p){
        var icons=new HashMap<Integer,ItemStack>();icons.put(0,LiteraryChoiceMenu.icon(BorrascaRegistry.COMPASS.get(),"Hand over the compass"));icons.put(8,LiteraryChoiceMenu.icon(Items.PAPER,"Keep it"));
        LiteraryChoiceMenu.open(p,"The Sheriff holds out his hand",icons,()->BorrascaSites.inside(p)&&near(p,BorrascaSites.base(p.server,p.getUUID()).offset(BorrascaRooms.SHERIFF),8),slot->{if(slot==8)return true;if(slot!=0)return false;var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);if(own.getInt("Visit")==3&&BorrascaBelongings.confiscate(p,own)){var sheriff=actor(p,own,"CastSheriff");if(sheriff!=null)say(p,sheriff,"That's county property now.");}save(p,own);return true;});
    }

    // ------------------------------------------------------------------ the cast
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
        cast(own,visit,prescott,w0,w1);
        switch(visit){
            case 1->visitOne(p,b,own,friend,sheriff,prescott);
            case 2->visitTwo(p,b,own,friend,w0,w1);
            case 3->visitThree(p,b,own,friend,sheriff);
            default->visitFour(p,b,own,friend);
        }
        boolean light=lit(own);if(own.getInt("Present")%20==0||light||light!=own.getBoolean("LightSent")){own.putBoolean("LightSent",light);send(p,own);}
    }
    /** The ridge light is on from the moment she sees someone until she is gone. */
    static boolean lit(CompoundTag own){return own.getInt("TakingTicks")>0&&!own.getBoolean("FriendMissing");}
    /** Who is about on this visit: no men on the ridge in childhood until they come for her; men at work behind the wire years later; nobody in the yard on the last night. */
    private static void cast(CompoundTag own,int visit,@Nullable LiteraryActor prescott,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1){
        boolean taking=visit==2&&own.getInt("TakingTicks")>0;
        for(var w:new LiteraryActor[]{w0,w1}){if(w==null)continue;if(visit==3){w.setInvisible(false);w.gesture(0);}else if(!taking){w.setInvisible(true);BorrascaCast.stop(w);}}
        if(prescott!=null){prescott.setInvisible(visit>=4);if(visit>=4)BorrascaCast.stop(prescott);}
    }

    // ------------------------------------------------------------------ the first visit: the pact
    private static void visitOne(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor friend,@Nullable LiteraryActor sheriff,@Nullable LiteraryActor prescott){
        if(sheriff!=null&&!own.getBoolean("MetSheriff")&&near(p,sheriff.blockPosition(),8)){talk(p,sheriff,own);own.putInt("HatTicks",40);}
        if(sheriff!=null&&own.getInt("HatTicks")>0){int hat=Math.max(0,own.getInt("HatTicks")-5);own.putInt("HatTicks",hat);if(hat==0)sheriff.gesture(0);}
        if(prescott!=null&&!own.getBoolean("MetPrescott")&&p.getZ()<b.getZ()-22&&p.getZ()>b.getZ()-34&&near(p,prescott.blockPosition(),13))talk(p,prescott,own);
        if(friend==null)return;
        friend.setInvisible(false);friend.appearance(LiteraryActor.BORRASCA_KIMBER,0);if(!own.getBoolean("CompassIssued"))friend.setItemSlot(EquipmentSlot.OFFHAND,BorrascaBelongings.original(p,own,"Compass"));
        pastMarkerTwo(p,b,own,friend);
        pact(p,b,own,friend);
    }
    private static boolean inFort(ServerPlayer p,BlockPos b){return p.getY()>=b.getY()+5.9&&p.getZ()<b.getZ()-89&&p.getZ()>b.getZ()-100&&Math.abs(p.getX()-b.getX())<6;}
    private static void pact(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend){
        if(!own.getBoolean("TreeReached")&&inFort(p,b))own.putBoolean("TreeReached",true);
        boolean ahead=p.getZ()<b.getZ()-40;
        if(!own.getBoolean("MetFriend")){
            if(near(p,friend.blockPosition(),6))talk(p,friend,own);
            else if(ahead&&!own.getBoolean("CalledAfter")){own.putBoolean("CalledAfter",true);say(p,friend,"Hey! Wait for me!");}
            if(!own.getBoolean("MetFriend")&&!ahead){BorrascaCast.stop(friend);return;}
        }
        // She goes to the Tree at her own pace, whether the reader is beside her, behind her or already there.
        if(!own.getBoolean("KimberAtFort")){if(!follow(p,b,own,friend,"ChildStep",CHILD_WALK))return;own.putBoolean("KimberAtFort",true);}
        // The legend, whenever the reader is at the Tree with her: in the fort or at its foot.
        if(!own.getBoolean("LegendDone")){
            if(!near(p,b.offset(FORT),9)){BorrascaCast.stop(friend);return;}
            int t=own.getInt("LegendTicks")+5;own.putInt("LegendTicks",t);
            String line=switch(t){
                case 5->"They call him the Shiny Gentleman. He lives past the second marker.";
                case 65->"You hear him before you see him. Three times, like somebody hitting a pipe a long way off.";
                case 125->"Dana Pruitt went up past the marker to look at him. Then she moved away. That's what they said.";
                case 185->"So if you hear him, you come down. You don't wait for anybody, and you don't look back up.";
                default->null;
            };
            if(line!=null)say(p,friend,line);
            if(t>=245)own.putBoolean("LegendDone",true);
            BorrascaCast.stop(friend);return;
        }
        // Down the ladder to the trunk; she carves once the reader is near enough to see it.
        if(!own.getBoolean("FriendCarved")){
            if(!follow(p,b,own,friend,"DownStep",DOWN))return;
            if(!near(p,b.offset(BorrascaRooms.CARVING),10)){friend.gesture(0);if(!own.getBoolean("CalledDown")){own.putBoolean("CalledDown",true);say(p,friend,"Come down here. Bring your name.");}return;}
            friend.gesture(3);friend.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(LiteraryRegistry.KNIFE.get()));int n=own.getInt("CarveTicks")+5;own.putInt("CarveTicks",n);
            if(n>=40){own.putBoolean("FriendCarved",true);bark(p,b,1);friend.gesture(4);heard(p,BorrascaRegistry.CARVE.get(),b.offset(BorrascaRooms.CARVING),.5F);say(p,friend,"Your turn. Right there, next to mine.");}
        }
    }
    /** A child who goes past the second marker hears it, the noise echoing off the ridge; the friend has something to say about it later. */
    private static void pastMarkerTwo(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend){
        int visit=own.getInt("Visit");if(visit>2||own.getBoolean("FriendMissing"))return;
        if(p.getZ()<b.getZ()+MARKER_TWO_Z&&!own.getBoolean("PastTwo"+visit)){own.putBoolean("PastTwo"+visit,true);echo(p,b,own,.9F);}
        if(own.getBoolean("PastTwo"+visit)&&!own.getBoolean("WarnedTwo"+visit)&&p.getZ()>b.getZ()+MARKER_TWO_Z+3&&near(p,friend.blockPosition(),8)){own.putBoolean("WarnedTwo"+visit,true);say(p,friend,"You went past the second marker? Don't. That's where he is.");}
    }
    /** The noise from the ridge: three strokes, heard wherever the reader is, the dust lifting with it. */
    private static void echo(ServerPlayer p,BlockPos b,CompoundTag own,float volume){heard(p,BorrascaRegistry.CHIME.get(),b.offset(BorrascaRooms.MACHINE),volume);dust(p);own.putInt("Echoes",own.getInt("Echoes")+1);own.putInt("EchoAt",own.getInt("TrailClock"));}
    /** She walks a route at her own pace; she waits only for a reader far behind her, and calls them once each time. True at its end. */
    private static boolean follow(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend,String key,BlockPos[] points){
        int i=own.getInt(key);if(i>=points.length){BorrascaCast.stop(friend);return true;}var at=Vec3.atBottomCenterOf(b.offset(points[i]));
        // Behind means further back along the whole route, not off the line of the next step: a reader waiting up ahead is never waited for.
        var start=Vec3.atBottomCenterOf(b.offset(points[0]));var way=Vec3.atBottomCenterOf(b.offset(points[points.length-1])).subtract(start).multiply(1,0,1);
        boolean behind=friend.distanceToSqr(p)>20*20&&p.position().subtract(start).dot(way)<friend.position().subtract(start).dot(way);
        if(behind){BorrascaCast.stop(friend);if(!own.getBoolean(key+"Called")){own.putBoolean(key+"Called",true);say(p,friend,"Come on!");}return false;}
        own.putBoolean(key+"Called",false);
        var delta=friend.position().subtract(at);if(delta.horizontalDistance()<.4&&Math.abs(delta.y)<.14){own.putInt(key,i+1);BorrascaCast.stop(friend);}else BorrascaCast.walk(friend,at,.8);return own.getInt(key)>=points.length;
    }

    // ------------------------------------------------------------------ the second visit: the taking
    private static void visitTwo(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor friend,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1){
        if(own.getBoolean("FriendMissing")){
            if(friend!=null)gone(p,b,own,friend,"FriendHidden",0);
            if(w0!=null)gone(p,b,own,w0,"Taker0Hidden",-1);if(w1!=null)gone(p,b,own,w1,"Taker1Hidden",1);
            if(BorrascaBelongings.has(p,own,"Compass")){own.putBoolean("CompassExamined",true);own.putInt("Beat",2);}
            return;
        }
        if(friend==null)return;
        friend.setInvisible(false);friend.appearance(LiteraryActor.BORRASCA_KIMBER,0);if(!own.getBoolean("CompassIssued"))friend.setItemSlot(EquipmentSlot.OFFHAND,BorrascaBelongings.original(p,own,"Compass"));
        friend.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
        pastMarkerTwo(p,b,own,friend);
        taking(p,b,own,friend,w0,w1);
    }
    private static void taking(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1){
        if(own.getInt("TakingTicks")>0){taken(p,b,own,friend,w0,w1);return;}
        if(!own.getBoolean("Read_ContentNote")){friend.gesture(0);BorrascaCast.stop(friend);return;}
        if(!own.getBoolean("FollowAgain")){if(near(p,friend.blockPosition(),6))talk(p,friend,own);else{friend.gesture(0);BorrascaCast.stop(friend);return;}}
        int step=own.getInt("TakingStep");
        // The two shelters: where she is when the noise comes. She ducks; the reader is not made to.
        if((step==6||step==10)&&own.getInt("Shelter"+step)<60){
            int t=own.getInt("Shelter"+step);if(t==0){echo(p,b,own,.8F);say(p,friend,"Down. Behind the timber.");}
            friend.gesture(5);BorrascaCast.stop(friend);if(p.isCrouching()&&p.distanceToSqr(friend)<36)own.putBoolean("Sheltered"+step,true);own.putInt("Shelter"+step,Math.min(60,t+5));return;
        }
        friend.gesture(0);
        if(!follow(p,b,own,friend,"TakingStep",TAKING_WALK))return;
        // At the clearing she waits for the reader, wherever they have got to, and calls for them.
        if(!near(p,b.offset(BorrascaRooms.TAKING),20)){int w=own.getInt("ClearingWait")+5;own.putInt("ClearingWait",w);if(w%200==5)say(p,friend,p.getGameProfile().getName()+"? Where'd you go?");return;}
        own.putInt("TakingTicks",1);friend.gesture(2);say(p,friend,"Wait. Is that someone—");echo(p,b,own,1.6F);comeDown(p,b,own,w0,w1);send(p,own);
    }
    /** The men come down the ridge road in the light: from wherever the reader is not looking, if there is such a place. */
    private static void comeDown(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1){
        var l=p.serverLevel();int used=-1;
        for(var w:new LiteraryActor[]{w0,w1}){
            if(w==null)continue;int pick=-1;
            for(int i=0;i<TAKERS_FROM.length;i++)if(i!=used&&!LiteraryVignettes.watched(l,b.offset(TAKERS_FROM[i]).getCenter().add(0,1,0))){pick=i;break;}
            if(pick<0)pick=used==TAKERS_FROM.length-1?TAKERS_FROM.length-2:TAKERS_FROM.length-1;used=pick;
            BorrascaCast.stop(w);w.moveTo(Vec3.atBottomCenterOf(b.offset(TAKERS_FROM[pick])),0,0);w.setInvisible(false);w.appearance(LiteraryActor.BORRASCA_KYLE,WORKER);w.gesture(TAKING_HAND);
        }
    }
    /** They walk down to her, take her arm, and lead her up toward the wire. She calls the reader's name once. */
    private static void taken(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1){
        int clock=Math.min(TAKEN_BY+LED_FOR+1,own.getInt("TakingTicks")+5);own.putInt("TakingTicks",clock);
        var led=Vec3.atBottomCenterOf(b.offset(LED_TO));
        int side=-1;for(var w:new LiteraryActor[]{w0,w1}){
            if(w==null){side=1;continue;}w.setInvisible(false);w.gesture(TAKING_HAND);
            BorrascaCast.walk(w,own.getBoolean("Led")?led.add(side*.8,0,.6):friend.position().add(side*.7,0,-.5),1.1);
            if(clock%10==1||clock%10==6)LiteraryVignettes.heard(p,SoundEvents.GRAVEL_STEP,w.blockPosition(),1.4F,.55F);
            side=1;
        }
        if(!own.getBoolean("Led")){
            boolean reached=false;for(var w:new LiteraryActor[]{w0,w1})if(w!=null&&w.distanceToSqr(friend)<1.8*1.8)reached=true;
            // They walk the whole way down; only if something has stopped them does she go anyway.
            if(reached||clock>=TAKEN_BY){own.putBoolean("Led",true);own.putInt("LedAt",clock);friend.gesture(LED);say(p,friend,p.getGameProfile().getName()+"!");}
            else BorrascaCast.stop(friend);
        }
        if(own.getBoolean("Led"))BorrascaCast.walk(friend,led,1.0);
        if(own.getBoolean("Led")&&clock>=own.getInt("LedAt")+LED_FOR){
            own.putBoolean("FriendMissing",true);friend.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);BorrascaBelongings.dropOnce(p,own,"Compass",b.offset(BorrascaRooms.DROP));
            fit(p,b,BorrascaRooms.DROP.below(),BorrascaFittingBlock.Kind.TRAIL);for(int i=0;i<8;i++)fit(p,b,new BlockPos(1,BorrascaRooms.trailY(-152-i),-152-i),BorrascaFittingBlock.Kind.TRACKS);
        }
    }
    /** After the taking: whoever is still in view goes on up the trail; once nobody is looking, they are gone. Nobody vanishes while watched. */
    private static void gone(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor a,String key,int side){
        if(a.role()==LiteraryActor.BORRASCA_KIMBER){a.appearance(LiteraryActor.BORRASCA_KIMBER,MISSING);a.setCustomNameVisible(false);a.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);}
        if(own.getBoolean(key)||a.isInvisible()&&!own.contains(key)||!LiteraryVignettes.watched(p.serverLevel(),a.getEyePosition())){a.setInvisible(true);own.putBoolean(key,true);BorrascaCast.stop(a);return;}
        BorrascaCast.walk(a,Vec3.atBottomCenterOf(b.offset(LED_TO)).add(side*.8,0,0),1.0);
    }

    // ------------------------------------------------------------------ the third visit: the denial
    private static void visitThree(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor friend,@Nullable LiteraryActor sheriff){
        if(friend!=null){friend.appearance(LiteraryActor.BORRASCA_KIMBER,MISSING);friend.setInvisible(true);BorrascaCast.stop(friend);}
        // The gouge is seen when it is looked at, from close enough to read the bark; no hand is needed.
        if(!own.getBoolean("GougeSeen")&&looksAt(p,b.offset(BorrascaRooms.CARVING),7))noticeGouge(p,own);
        if(sheriff!=null){sheriff.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(BorrascaRegistry.FLASHLIGHT.get()));blockade(p,b,own,sheriff);}
        if(near(p,b.offset(BorrascaRooms.MARKER_FOUR),9))perimeter(p,own);
        if(own.getBoolean("GougeSeen")&&own.getBoolean("PerimeterSeen"))own.putInt("Beat",3);
    }
    private static void noticeGouge(ServerPlayer p,CompoundTag own){if(own.getBoolean("GougeSeen"))return;own.putBoolean("GougeSeen",true);if(!own.getBoolean("RestoredName"))cue(p,"Fresh pale cuts. A flat steel tool has taken her name out. Yours is still there.");}
    private static void perimeter(ServerPlayer p,CompoundTag own){if(own.getBoolean("PerimeterSeen"))return;own.putBoolean("PerimeterSeen",true);cue(p,"Chain link. A padlock. The vibration travels through the wire.");}
    /** Whether the reader is looking at a block from within a distance, with nothing in the way. */
    static boolean looksAt(ServerPlayer p,BlockPos at,double distance){
        var eye=p.getEyePosition();var c=at.getCenter();var d=c.subtract(eye);if(d.lengthSqr()>distance*distance||d.lengthSqr()<.01)return false;
        if(p.getLookAngle().dot(d.normalize())<.82)return false;
        var hit=p.level().clip(new ClipContext(eye,c,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));return hit.getType()==HitResult.Type.MISS||hit.getBlockPos().equals(at);
    }
    public static boolean masking(CompoundTag own){return own.getInt("TrailClock")%200<60;}
    private static void blockade(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor sheriff){
        // He gives himself away as soon as he has the reader in sight, from either side of the tape.
        if(!own.getBoolean("HeardDenial")&&sheriff.distanceToSqr(p)<20*20&&sees(sheriff,p.getEyePosition()))denial(p,sheriff,own);
        if(own.getInt("TrailClock")%200==0){heard(p,BorrascaRegistry.CHIME.get(),b.offset(BorrascaRooms.MACHINE),.8F);dust(p);}
        if(own.getBoolean("Escorting")){escort(p,b,own,sheriff);return;}
        int cooldown=Math.max(0,own.getInt("CaughtCooldown")-5);own.putInt("CaughtCooldown",cooldown);
        BorrascaCast.walk(sheriff,Vec3.atBottomCenterOf(b.offset(BorrascaRooms.SHERIFF)),.65);
        float yaw=flashlightYaw(own);sheriff.setYRot(yaw);sheriff.setYBodyRot(yaw);sheriff.setYHeadRot(yaw);sheriff.gesture(4);
        // Past the tape and in his light, or in plain sight and noisy near him, wherever the reader came from.
        boolean beyond=p.getZ()<b.getZ()+TAPE_Z-.5&&p.getZ()>b.getZ()-167;
        boolean caught=beyond&&cooldown==0&&(flashlightSees(sheriff,p,yaw)||!p.isCrouching()&&!masking(own)&&sheriff.distanceToSqr(p)<14*14&&sees(sheriff,p.getEyePosition()));
        int suspicion=caught?own.getInt("Suspicion")+5:Math.max(0,own.getInt("Suspicion")-5);own.putInt("Suspicion",suspicion);
        // The beam holds on whoever it found until they are out of it.
        if(caught)own.putFloat("BeamYaw",toward(sheriff,p));
        if(caught&&suspicion==10)say(p,sheriff,"Hold it. I can see you.");
        if(suspicion>=40){own.putBoolean("Escorting",true);own.putInt("Suspicion",0);own.putInt("EscortStill",0);own.remove("EscortX");sheriff.gesture(2);say(p,sheriff,"Back to the tape. Walk.");}
    }
    /** He walks the reader back behind them, the light on them; he waits when they stop and tells them to keep going. */
    private static void escort(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor sheriff){
        var end=Vec3.atBottomCenterOf(b.offset(2,4,-119));
        float yaw=toward(sheriff,p);own.putFloat("BeamYaw",yaw);sheriff.setYRot(yaw);sheriff.setYBodyRot(yaw);sheriff.setYHeadRot(yaw);sheriff.gesture(4);
        if(p.getZ()>=end.z-1){own.putBoolean("Escorting",false);own.putInt("CaughtCooldown",80);BorrascaCast.stop(sheriff);say(p,sheriff,"Stay this side.");return;}
        var behind=p.position().add(0,0,-2.2);if(sheriff.distanceToSqr(p)>3.5*3.5)BorrascaCast.walk(sheriff,behind,.7);else BorrascaCast.stop(sheriff);
        double moved=own.contains("EscortX")?Math.hypot(p.getX()-own.getDouble("EscortX"),p.getZ()-own.getDouble("EscortZ")):1;own.putDouble("EscortX",p.getX());own.putDouble("EscortZ",p.getZ());
        int still=moved<.08?own.getInt("EscortStill")+5:0;own.putInt("EscortStill",still);if(still==40||still>40&&still%160==40)say(p,sheriff,"Keep walking.");
    }
    static float toward(LiteraryActor from,ServerPlayer to){var d=to.position().subtract(from.position());return (float)Math.toDegrees(Math.atan2(-d.x,d.z));}
    public static float flashlightYaw(CompoundTag own){return own.getInt("Suspicion")>0||own.getBoolean("Escorting")?own.getFloat("BeamYaw"):180+55*(float)Math.sin(own.getInt("TrailClock")*.018);}
    public static boolean flashlightSees(LiteraryActor sheriff,ServerPlayer p,float yaw){Vec3 d=p.getEyePosition().subtract(sheriff.getEyePosition());if(d.lengthSqr()>22*22||d.lengthSqr()<.01)return false;Vec3 direction=new Vec3(-Math.sin(Math.toRadians(yaw)),0,Math.cos(Math.toRadians(yaw)));return direction.dot(d.normalize())>.965&&sees(sheriff,p.getEyePosition());}
    private static boolean sees(LiteraryActor a,Vec3 at){var hit=a.level().clip(new ClipContext(a.getEyePosition(),at,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a));return hit.getType()==HitResult.Type.MISS;}

    // ------------------------------------------------------------------ the fourth visit: the names
    private static void visitFour(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor friend){
        if(friend!=null){friend.appearance(LiteraryActor.BORRASCA_KIMBER,MISSING);friend.setInvisible(true);BorrascaCast.stop(friend);}
        if(!own.getBoolean("PowerOff")&&own.getInt("TrailClock")%200==0)heard(p,BorrascaRegistry.CHIME.get(),b.offset(BorrascaRooms.MACHINE),1);
        if(!own.getBoolean("GougeSeen")&&looksAt(p,b.offset(BorrascaRooms.CARVING),7))noticeGouge(p,own);
        if(carriesEvidence(p,own))own.putBoolean("PatchRecovered",true);
        if(own.getBoolean("PowerOff")&&own.getBoolean("EvidenceRead")&&own.getBoolean("PatchRecovered")&&carriesEvidence(p,own)&&(own.getInt("ConfirmedVisits")&7)==7&&p.getZ()>b.getZ()-106){own.putBoolean("EscapedWithOriginal",true);LiteraryVignettes.ready(p,PLACE,own,"silenced_the_mountain");}
    }

    // ------------------------------------------------------------------ the place
    private static void scene(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean("PowerOff")&&p.serverLevel().hasChunkAt(b.offset(BorrascaRooms.MACHINE))){var at=b.offset(BorrascaRooms.MACHINE);var s=p.serverLevel().getBlockState(at);if(s.is(BorrascaRegistry.MACHINE.get())&&s.getValue(BorrascaMachineBlock.RUNNING))p.serverLevel().setBlock(at,s.setValue(BorrascaMachineBlock.RUNNING,false),F);var lamp=b.offset(BorrascaRooms.LAMP);var light=p.serverLevel().getBlockState(lamp);if(light.is(BorrascaRegistry.FITTING.get())&&light.getValue(BorrascaFittingBlock.KIND)==BorrascaFittingBlock.Kind.LAMP)fit(p,b,BorrascaRooms.LAMP,BorrascaFittingBlock.Kind.LAMP_OFF);}
        int visit=own.getInt("Visit");if(!p.serverLevel().hasChunkAt(b.offset(BorrascaRooms.CARVING))||!p.serverLevel().hasChunkAt(b.offset(BorrascaRooms.PACK))||visit==4&&!p.serverLevel().hasChunkAt(b.offset(BorrascaRooms.GATE)))return;
        int stage=own.getBoolean("RestoredName")?4:visit>=3?3:own.getBoolean("PactCarved")?2:own.getBoolean("FriendCarved")?1:0;bark(p,b,stage);
        if(own.getInt("SceneryVisit")==visit)return;
        if(visit>=3){for(int x=-5;x<=5;x++)for(int y=5;y<=11;y++)for(int z=-100;z<=-88;z++){var s=p.serverLevel().getBlockState(b.offset(x,y,z));if(s.is(Blocks.SPRUCE_PLANKS)||s.is(Blocks.BIRCH_PLANKS)||s.is(Blocks.OAK_PLANKS))fit(p,b,new BlockPos(x,y,z),BorrascaFittingBlock.Kind.AGED_WOOD);}for(int x=-1;x<=5;x++)fit(p,b,new BlockPos(x,5,-125),BorrascaFittingBlock.Kind.TAPE);}
        // Her pack is not at the sorting table until after she is taken.
        var pack=p.serverLevel().getBlockState(b.offset(BorrascaRooms.PACK));
        if(visit<=2&&pack.is(BorrascaRegistry.FITTING.get())&&pack.getValue(BorrascaFittingBlock.KIND)==BorrascaFittingBlock.Kind.PACK)p.serverLevel().setBlock(b.offset(BorrascaRooms.PACK),Blocks.AIR.defaultBlockState(),F);
        if(visit>=3&&pack.isAir())fit(p,b,BorrascaRooms.PACK,BorrascaFittingBlock.Kind.PACK);
        if(visit==2){p.serverLevel().setBlock(b.offset(2,0,-98),BorrascaRegistry.CARVING.get().defaultBlockState().setValue(BorrascaCarvingBlock.STAGE,3),F);var source=b.offset(BorrascaRooms.SOURCE);if(p.serverLevel().getBlockEntity(source) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity lectern){lectern.setBook(BorrascaTexts.contentNote());lectern.setChanged();p.serverLevel().sendBlockUpdated(source,lectern.getBlockState(),lectern.getBlockState(),F);}}
        if(visit==4){for(int x=-1;x<=1;x++)for(int y=12;y<=14;y++)p.serverLevel().setBlock(b.offset(x,y,-167),Blocks.AIR.defaultBlockState(),F);p.serverLevel().setBlock(b.offset(BorrascaRooms.LOCK),Blocks.AIR.defaultBlockState(),F);fit(p,b,new BlockPos(2,12,-168),BorrascaFittingBlock.Kind.LOCK);openDoor(p.serverLevel(),b.offset(BorrascaRooms.FACTORY_DOOR),true);}
        own.putInt("SceneryVisit",visit);
    }
    private static void bark(ServerPlayer p,BlockPos b,int stage){var at=b.offset(BorrascaRooms.CARVING);var s=p.serverLevel().getBlockState(at);if(s.is(BorrascaRegistry.CARVING.get())&&s.getValue(BorrascaCarvingBlock.STAGE)!=stage)p.serverLevel().setBlock(at,s.setValue(BorrascaCarvingBlock.STAGE,stage),F);BorrascaCarvingEntity.names(p.serverLevel(),at,p.getGameProfile().getName());}
    private static void fit(ServerPlayer p,BlockPos b,BlockPos rel,BorrascaFittingBlock.Kind kind){p.serverLevel().setBlock(b.offset(rel),BorrascaFittingBlock.of(kind,Direction.SOUTH),F);}
    private static boolean near(ServerPlayer p,BlockPos at,double distance){return at!=null&&p.distanceToSqr(Vec3.atBottomCenterOf(at))<distance*distance;}
    /** A person speaks: over their head, and on the reader's action bar. Never in chat. */
    private static void say(ServerPlayer p,LiteraryActor a,String words){a.say(words);a.getPersistentData().putInt("BorrascaSaid",a.tickCount);p.displayClientMessage(Component.literal(speaker(a)+": "+words),true);}
    static String speaker(LiteraryActor a){return a.role()==LiteraryActor.BORRASCA_KIMBER?FRIEND:a.role()==LiteraryActor.BORRASCA_SHERIFF?"Sheriff":a.role()==LiteraryActor.BORRASCA_PRESCOTT&&a.phase()<WORKER?"Prescott":"A man";}
    /** What the reader notices, on the action bar. */
    private static void cue(ServerPlayer p,String text){p.displayClientMessage(Component.literal(text),true);}
    /** The House's own word, grey and italic. */
    private static void house(ServerPlayer p,String text){p.displayClientMessage(Component.literal(text).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    private static void heard(ServerPlayer p,net.minecraft.sounds.SoundEvent sound,BlockPos at,float volume){LiteraryVignettes.heard(p,sound,at,volume,1);}
    private static void dust(ServerPlayer p){p.serverLevel().sendParticles(p,ParticleTypes.ASH,false,p.getX(),p.getY()+.4,p.getZ(),32,2,.5,2,.03);}
    private static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    private static void send(ServerPlayer p,CompoundTag own){var b=BorrascaSites.base(p.server,p.getUUID());var sheriff=actor(p,own,"CastSheriff");if(b!=null)HousePackets.send(p,new BorrascaViewPayload(b.offset(BorrascaRooms.MACHINE),own.getInt("Visit"),own.getBoolean("Here"),!own.getBoolean("PowerOff")&&(own.getInt("Visit")>1||own.getBoolean("PactCarved")),lit(own)?own.getInt("TakingTicks"):0,own.getInt("TrailClock"),sheriff==null?b.offset(BorrascaRooms.SHERIFF):sheriff.blockPosition(),flashlightYaw(own)));}
    /** Per-native-tick motion; native collision and ladder physics remain enabled. Spoken lines clear after a few seconds. */
    public static void actorTick(LiteraryActor a){
        if(!a.scene().equals(PLACE.id())||!(a.level() instanceof ServerLevel l))return;
        if(a.isCustomNameVisible()){int said=a.getPersistentData().getInt("BorrascaSaid");if(a.tickCount-said>SPEECH_TICKS||a.tickCount<said)a.setCustomNameVisible(false);}
        var p=a.owner().map(l.getServer().getPlayerList()::getPlayer).orElse(null);if(p==null||!BorrascaSites.inside(p)){BorrascaCast.stop(a);return;}var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);if(!own.getBoolean("Here")||!own.getBoolean("CastRecalled"))BorrascaCast.stop(a);
    }
    /** Capture intent; the actual door travel callback is the sole return authority. */
    public static void depart(ServerPlayer p){BorrascaCast.release(p.getUUID());var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);boolean valid=BorrascaSites.inside(p)&&own.getBoolean("Here")&&!own.getBoolean("Interrupted")&&p.isAlive();if(valid&&own.getInt("Beat")>=own.getInt("Visit"))own.putInt("DepartVisit",own.getInt("Visit"));else own.remove("DepartVisit");own.putBoolean("ExitEligible",valid&&own.getInt("Visit")==4&&(own.getInt("ConfirmedVisits")&7)==7&&own.getBoolean("Ready")&&own.getBoolean("PowerOff")&&own.getBoolean("EvidenceRead")&&carriesEvidence(p,own));own.putBoolean("Here",false);save(p,own);}
    public static void returned(ServerPlayer p){var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("BorrascaDeparted",true);own.putBoolean("Escorting",false);own.putInt("Suspicion",0);int returned=own.getInt("DepartVisit");if(returned>=1&&returned<=3){own.putInt("ConfirmedVisits",own.getInt("ConfirmedVisits")|(1<<(returned-1)));own.putInt("CastNextVisit",returned+1);}if(own.getBoolean("ExitEligible")&&carriesEvidence(p,own)){own.putBoolean("NameReturned",own.getBoolean("RestoredName"));own.putBoolean("Completed",true);own.putInt("Beat",4);save(p,own);WitnessAccount.resolve(p,WitnessAccount.Story.BORRASCA,"silenced_the_mountain");d.setCompleted(PLACE.id(),true);WitnessAccount.updateBook(p,false);}own.remove("ExitEligible");own.remove("DepartVisit");save(p,own);}
    @SubscribeEvent public static void joined(EntityJoinLevelEvent event){if(!(event.getEntity() instanceof LiteraryActor a)||!(event.getLevel() instanceof ServerLevel l)||!a.scene().equals(PLACE.id())||a.role()!=LiteraryActor.BORRASCA_KIMBER||a.owner().isEmpty())return;var own=LiteraryVignettes.personal(LabyrinthData.get(l.getServer()),a.owner().get(),PLACE);if(own.getInt("Visit")>=3||own.getBoolean("FriendMissing")){a.appearance(LiteraryActor.BORRASCA_KIMBER,MISSING);a.setCustomNameVisible(false);a.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);if(own.getInt("Visit")>=3||own.getBoolean("FriendHidden")){a.setInvisible(true);BorrascaCast.stop(a);}}}
    @SubscribeEvent public static void rejoined(PlayerEvent.PlayerLoggedInEvent event){if(!(event.getEntity() instanceof ServerPlayer p)||!BorrascaSites.inside(p))return;var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);if(own.getBoolean("Here")){own.putBoolean("CastRecalled",false);save(p,own);}}
    @SubscribeEvent public static void died(LivingDeathEvent event){if(event.getEntity() instanceof ServerPlayer p&&BorrascaSites.inside(p)){BorrascaCast.release(p.getUUID());var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);own.putBoolean("Here",false);own.remove("ExitEligible");own.remove("DepartVisit");save(p,own);}}
}
