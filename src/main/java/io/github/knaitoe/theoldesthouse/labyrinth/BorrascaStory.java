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
 * The friend goes to the Tree at her own pace and tells the legend once the reader is in the fort with her; the taking fires whenever both
 * of them reach the clearing, and she calls the reader back if they wander off; the gouge is noticed by looking at it; the blockade is
 * passed by any route the Sheriff does not see, and he gives himself away as soon as he has the reader in sight. The refinery is
 * sealed until the last night. Men come down the ridge road in the light and lead her away. The friend is a new child, Nell: the owner, 1 November, "the vignette was supposed to occupy a separate cast in the same environment... the events of borrasca had to have played out for many, many casts of kids." Only the Sheriff and the Prescott family last across the generations. People speak above their heads and on
 * the action bar, never in chat.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BorrascaStory {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.BORRASCA;
    /** 0.4.235: a child of a later Drisking generation than the original story's; only the Sheriff and the Prescotts last that long. */
    public static final String FRIEND="Kelly";
    public static final int MISSING=10,WORKER=20;
    /** 0.4.438: the friend found grown in room 6 on the last night; drawn on her own grown skin, at full size. */
    public static final int GROWN=11;
    public static final int ELI=1,WREN=2;
    /**
     * The taking (0.4.262): the longest the men may take to reach her before she is led anyway, how long she is led before her compass
     * falls and she is gone, and the paces: they walk down the ridge road deliberately, and lead her back up it no faster than she can be
     * made to go.
     */
    static final int TAKEN_BY=1200,LED_FOR=60;static final double TAKER_PACE=.8,LED_PACE=.5;
    /** 0.4.236: the Shiny Gentleman stands as long as the strokes last, between these distances from a child, this tall. */
    public static final int GENT_TICKS=70;public static final double GENT_NEAR=28,GENT_FAR=200,GENT_HEIGHT=4.5;
    /**
     * 0.4.434 (the owner: "Ensure the shiny gentleman still appears as an actual monster out by the far marker too"). The hill drawn out
     * in 0.4.419 put the second marker a long way past the Tree, beyond the seventy blocks he used to be seen within; now he may stand out
     * there as far as GENT_FAR from the child, and the farther off he stands the taller he is (GENT_SCALE_PER blocks to each of his own
     * heights, up to GENT_SCALE_MAX), so that from the Tree he still towers over the pines on the ridge.
     */
    public static final double GENT_SCALE_PER=40,GENT_SCALE_MAX=2.6;
    public static float gentScale(double distance){return (float)Math.max(1,Math.min(GENT_SCALE_MAX,distance/GENT_SCALE_PER));}
    /** How tall he stands for this reader now, in his own heights (one when close, as on the ridge behind the men). */
    public static float gentScale(CompoundTag own){return own.contains("GentScale")?Math.max(1,own.getFloat("GentScale")):1;}
    /** 0.4.235 gestures: the friend led by the arm, and a taker's hand held out to her; 0.4.262: a man's hand on her arm, his left or his right. */
    public static final int LED=6,TAKING_HAND=7,HOLD_LEFT=8,HOLD_RIGHT=9,CARRIED=11,CARRY=12,WARNING=13,SCARED=14;
    /** 0.4.427: the Sheriff beaten down into the dirt, sitting with his hands up (KNELT), and dead on his back (FALLEN). */
    public static final int KNELT=15,FALLEN=16;
    /** 0.4.439: Dana, 141-A, on the town cast's phase DANA; lying on her back (LAID, arms over the sides, swaying), and LAID_STILL. */
    public static final int DANA=3,LAID=17,LAID_STILL=18;
    /** 0.4.439: Dana at the blade, still alive (TORN_LIVE, thrashing and screaming) and after (TORN_DEAD). Both lie on the belt. */
    public static final int TORN_LIVE=19,TORN_DEAD=20;
    /** At or below this much health (of 40) the Sheriff goes down; each blow does three to eight. */
    public static final float SHERIFF_DOWN_HEALTH=14;
    /** How long a spoken line stays over the speaker's head. */
    public static final int SPEECH_TICKS=100;
    private static final int FORT_LISTEN_TICKS=100; // The chime lasts 70 ticks. Leave a beat of silence after it.
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final BlockPos FRONT_LEVER=new BlockPos(-15,13,-168);
    /** What a hand on the Tree's old names finds (0.4.448). */
    private static final String[] NAMES_SEEN={"Names, and older names under them. Some are only grooves now.","The bark has closed over most of these. A few can still be read.","Every one cut with a different knife.","Among the names, flat grey patches where a name was taken off."};
    private static final BlockPos[] CHILD_WALK={new BlockPos(1,0,-35),new BlockPos(1,0,-60),new BlockPos(3,0,-86),new BlockPos(3,6,-88),new BlockPos(1,6,-94)};
    private static final BlockPos[] DOWN={new BlockPos(2,6,-89),new BlockPos(2,6,-88),new BlockPos(3,6,-88),new BlockPos(3,0,-88),new BlockPos(2,0,-88),new BlockPos(1,0,-96)};
    private static final BlockPos[] TAKING_WALK={new BlockPos(1,0,-85),new BlockPos(2,0,-93),new BlockPos(3,0,-97),new BlockPos(3,0,-103),BorrascaRooms.trail(-108),BorrascaRooms.SHELTER_ONE,new BlockPos(6,2,-114),new BlockPos(6,3,-116),BorrascaRooms.trail(-125),BorrascaRooms.SHELTER_TWO,new BlockPos(1,6,-131),BorrascaRooms.trail(-141),BorrascaRooms.TAKING};
    /**
     * 0.4.432 (owner: the longer hill "gives Nell and the player and other potential npcs ... time to talk and bond"). On the walk up to
     * the Tree she talks, one line at a time, while the reader keeps near her: WALK_TALK_GAP reader ticks after her last line and once
     * nothing else is being said. A reader who runs on ahead or hangs back misses what she says while they are away.
     */
    public static final String[] WALK_TALK={
        "My mom says I can go as far as the Tree and not past it. So the Tree's the edge of everything.",
        "Dana used to walk me up here. She knew all the birds. I only know the loud ones.",
        "If you hear the knocking, count it in your head. Don't say the number out loud. That's the rule.",
        "Do you want to be best friends? You don't have to say. You can just keep walking."
    };
    /**
     * 0.4.432: the second visit's walk on from the Tree toward the mill, the same way: Patrick is missing, and she talks about it between
     * the two shelters' calls.
     */
    public static final String[] WALK_TALK_TWO={
        "Patrick's dad sat out on the porch with the light on all night. Kendra says he never went in.",
        "The sheriff told my mom Patrick ran off. He didn't even take his bike.",
        "Same rule as before. If you hear the knocking, count it in your head.",
        "If something happens, you run home. Don't wait for me. Promise."
    };
    public static final int WALK_TALK_GAP=80,WALK_TALK_NEAR=8;
    /**
     * 0.4.433 (owner: the walk "gives Nell and the player and other potential npcs, if not occupied by players, time to talk and bond").
     * On the first visit, once Kelly has set off for the Tree, Patrick and Kendra come up the trail behind her in single file, never past
     * her, as far as the first marker, where they stand either side of the trail. There they stop: Kendra is not let past it, and Patrick will go up by the second
     * marker some other time. When they have said so to a reader near enough to hear (or the reader has long gone on), they turn back
     * down the trail, and are home in town again the moment nobody is watching them.
     */
    private static final BlockPos[] ALONG={new BlockPos(1,0,-35),new BlockPos(1,0,-60),new BlockPos(3,0,-75)};
    public static final String[] ALONG_ELI={"Race you to the first marker. Kelly doesn't count, she stops for every bird."};
    public static final String[] ALONG_WREN={"He says that every time, and then he walks."};
    public static final String FAREWELL_WREN="This is as far as I go. My mom can see the marker from our porch.";
    public static final String FAREWELL_ELI="I'm going up by the second marker some other time. Don't tell my brother.";
    /** Reader ticks the two wait at the marker for someone to say goodbye to before they go home anyway. */
    public static final int ALONG_WAIT=1200,ALONG_NEAR=10;
    /** The fort the friend climbs to, and the point she tells the legend from. */
    public static final BlockPos FORT=new BlockPos(1,6,-94);
    /**
     * 0.4.262 (owner: the workers "need proper walking animations ... sequence that with the ringing too, have the shiny gentleman looming
     * above them from behind as they approach ... and need an exit path too"). The way the men come for her: from the turning bay where
     * their truck stands, round the shoulder, down the cutting outside the east wire, along the ridge road in front of the fence, and down
     * the lane into the clearing (BorrascaLand.ROAD). They appear wherever along the cutting nobody can see (SPAWNS, nearest first), and
     * lead her back out the same way (EXIT), walking until nobody is looking.
     */
    static final BlockPos[] APPROACH={new BlockPos(37,12,-203),new BlockPos(35,12,-197),new BlockPos(31,12,-191),new BlockPos(31,12,-182),new BlockPos(31,12,-173),new BlockPos(30,12,-165),new BlockPos(21,12,-162),new BlockPos(10,12,-162),new BlockPos(3,12,-162),new BlockPos(2,11,-157),new BlockPos(2,10,-153),new BlockPos(1,9,-149)};
    /** 0.4.440: the nearest unseen place on their road first, so that they come into sight soon and are not long coming. */
    static final int[] SPAWNS={6,5,4,3,2,0};
    /** The point on the approach at the head of the lane: the second stroke comes as they turn down it toward her. */
    static final int LANE_HEAD=8;
    static final BlockPos[] EXIT;
    static{EXIT=new BlockPos[APPROACH.length];for(int i=0;i<APPROACH.length;i++)EXIT[i]=APPROACH[APPROACH.length-1-i];}
    /** The second marker's line, and how far past it the Sheriff watches. */
    private static final BlockPos[] SHERIFF_PATROL={BorrascaRooms.YARD_PATROL_START,new BlockPos(2,12,-180),
            new BlockPos(7,12,-180),new BlockPos(7,12,-174)};
    /** 0.4.440: at the refinery's doorway beside the belt, looking in at the stopped machine (the machine has a hall of its own now). */
    public static final BlockPos SHERIFF_SHUTDOWN_CHECK=new BlockPos(1,12,-180);
    static final int MARKER_TWO_Z=-124,TAPE_Z=-125;
    private BorrascaStory(){}

    public static void arrive(ServerPlayer p,CompoundTag own){
        int visit=own.getInt("Visit");if(visit==0)visit=1;else if(own.getBoolean("BorrascaDeparted")&&(own.getInt("ConfirmedVisits")&(1<<(visit-1)))!=0)visit=Math.min(4,visit+1);
        own.putInt("Visit",visit);own.putInt("GrabTicks",0);own.putInt("Approach",0);own.remove("ApproachArmed");own.putInt("ApproachAway",0);own.putBoolean("BorrascaDeparted",false);own.putBoolean("Here",true);own.putBoolean("Interrupted",false);own.remove("ExitEligible");own.remove("DepartVisit");p.getPersistentData().remove("BorrascaSpeechAt");own.putInt("Present",0);own.putInt("TrailClock",0);own.putBoolean("CastRecalled",false);
        BorrascaArcadeFriends.arrived(p,own); // 0.4.446: an afternoon at the cabinets left unfinished is over
        cue(p,switch(visit){case 1->"The pine trail. Someone is waiting at the sign.";case 2->FRIEND+" is waiting by the first marker. A note is waiting by the door.";case 3->"Years later. You remember the way to the three trees.";default->"The ridge after dark. The way back stays open.";});
        send(p,own);
    }
    public static ItemStack markEvidence(ServerPlayer p,ItemStack s){CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putUUID("BorrascaEvidence",p.getUUID()));return s;}
    public static boolean carriesEvidence(ServerPlayer p,CompoundTag own){return BorrascaBelongings.has(p,own,"Patch");}
    public static void read(ServerPlayer p,String key,CompoundTag own){if(!BorrascaSites.inside(p)||!own.getBoolean("Here")||own.getInt("Visit")!=4)return;switch(key){case "Evidence"->own.putBoolean("EvidenceRead",true);case "Ledger"->own.putBoolean("LedgerRead",true);case "Letter"->own.putBoolean("LetterRead",true);default->{}}}
    public static void held(ServerPlayer p,ItemStack s,String key){
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);
        if(!BorrascaBelongings.valid(p,own,key,s))return;
        if(key.equals(BorrascaComic.KEY)){BorrascaComic.read(p,s,own);return;} // 0.4.445: the comic opens wherever it is carried
        // 0.4.198: away from the trail the compass points (Keepsakes.compass); the patch only shows her name.
        if(!BorrascaSites.inside(p)){if(key.equals("Compass")&&own.getBoolean("Taken_Ending"))Keepsakes.compass(p);else p.displayClientMessage(Component.literal(key.equals("Compass")?"The red tape is worn thin around the brass case.":"Her name, stitched in the cloth."),true);return;}
        var b=BorrascaSites.base(p.server,p.getUUID());if(b==null)return;
        if(key.equals("Compass")&&own.getInt("Visit")==3&&!own.getBoolean("SheriffDead")&&!own.getBoolean("SheriffDown")){var sheriff=actor(p,own,"CastSheriff");if(sheriff!=null&&p.distanceToSqr(sheriff)<36){denial(p,sheriff,own);save(p,own);compassChoice(p);return;}}
        if(key.equals("Patch")&&own.getInt("Visit")==4&&near(p,BorrascaRooms.at(b,BorrascaRooms.CARVING),4)){restoreName(p,b,own);save(p,own);}
    }

    // ------------------------------------------------------------------ hands
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        if(own.getInt("Visit")==4&&BorrascaFeeding.click(p,b,BorrascaRooms.authored(rel),own))return true; // 0.4.439: the band, and what is on the pile, where they were authored
        // 0.4.449: a hand on one of the town's painted signs reads it in full, the lines too small to letter included.
        var board=p.serverLevel().getBlockState(b.offset(rel));
        if(BorrascaSignBlock.is(board)){cue(p,BorrascaSignBlock.sign(board).look());return true;}
        // 0.4.422: the shops along the street past the chapel are placed in the world (that street has no authored coordinates).
        if(BorrascaShops.click(p,b,rel,own))return true;
        rel=BorrascaRooms.authored(rel); // 0.4.420: the clicked block, where it was authored
        int visit=own.getInt("Visit");
        if(BorrascaComic.click(p,b,rel,own))return true; // 0.4.445: the comic in the fort's window
        // 0.4.449: the yard's water tank, down its hatch. The town's history says its water ran red for two years.
        if(BorrascaRooms.tankSilt(rel)){cue(p,"Dry. Red silt a hand deep on the boards, and a red line round the staves where the water stood.");return true;}
        // 0.4.444 (owner: "Keep prison cell door shut. Allow player to speak to npc through bars."): a hand on the cell's bars or its
        // door speaks to whoever is in it.
        if(BorrascaPeople.cell(rel)){
            var earl=BorrascaPeople.keeper(p,own,BorrascaPeople.EARL);
            if(earl!=null&&!earl.isInvisible()&&earl.distanceToSqr(p)<49)BorrascaPeople.talk(p,earl,own);else cue(p,"The cell is empty, and locked.");
            return true;
        }
        // 0.4.448: the names all over the Tree, and the two beside the carving.
        if(rel.equals(BorrascaRooms.FRIENDS_NAMES)){cue(p,visit<=1?"PAT. KEN. Cut this year. The wood is still pale.":visit==2?"KEN, and over it a flat pale patch the length of a short name.":visit==3?"Two flat pale patches, one over the other.":"Two flat patches, one over the other, gone grey.");return true;}
        if(BorrascaNamesBlock.is(p.serverLevel().getBlockState(BorrascaRooms.at(b,rel)))){cue(p,NAMES_SEEN[Math.floorMod(rel.getX()*7+rel.getY()*3+rel.getZ(),NAMES_SEEN.length)]);return true;}
        if(rel.equals(BorrascaRooms.CARVING)){
            if(visit==1){
                if(!own.getBoolean("FriendCarved")){cue(p,"Some names are only grooves now. There are old scars between them.");return true;}
                if(!own.getBoolean("PactCarved")){
                    // 0.4.429 (owner: "players should be able to take the knife from the kid, use it to carve their name"): the reader
                    // takes her pocketknife from her and cuts their own name with it, a few letters at each cut.
                    var friend=actor(p,own,"CastKimber");
                    if(!BorrascaBelongings.valid(p,own,BorrascaBelongings.KNIFE,p.getMainHandItem())){
                        if(BorrascaBelongings.has(p,own,BorrascaBelongings.KNIFE))cue(p,"The knife needs to be in your hand.");
                        else if(friend!=null){friend.gesture(4);say(p,friend,"Here. Take the knife.");}
                        else cue(p,"You'd need something to cut with.");
                        return true;
                    }
                    int cuts=own.getInt("KnifeCuts")+1;own.putInt("KnifeCuts",cuts);var at=BorrascaRooms.at(b,rel);
                    p.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);heard(p,BorrascaRegistry.CARVE.get(),at,.4F);
                    p.serverLevel().sendParticles(p,new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,Blocks.SPRUCE_LOG.defaultBlockState()),false,at.getX()+.5,at.getY()+.55,at.getZ()+1.05,6,.15,.12,.02,.02);
                    String name=p.getGameProfile().getName();
                    if(cuts<CUTS){
                        var bark=p.serverLevel().getBlockState(at);if(bark.is(BorrascaRegistry.CARVING.get())&&bark.getValue(BorrascaCarvingBlock.STAGE)!=2)p.serverLevel().setBlock(at,bark.setValue(BorrascaCarvingBlock.STAGE,2),F);
                        BorrascaCarvingEntity.names(p.serverLevel(),at,name.substring(0,Math.max(1,(int)Math.ceil(name.length()*cuts/(double)CUTS))));
                        if(cuts==1)cue(p,"The bark is softer than it looks.");
                        return true;
                    }
                    own.putBoolean("PactCarved",true);own.putInt("Beat",1);bark(p,b,2);heard(p,BorrascaRegistry.CARVE.get(),at,.55F);
                    if(friend!=null){friend.gesture(2);say(p,friend,"There. Now it has to keep a place for both of us.");}
                    echo(p,b,own,.35F);own.putBoolean("PactEcho",true);
                    return true;
                }
                cue(p,"Both names, side by side in the bark.");return true;
            }
            if(visit==2){cue(p,"Both names are still there. Beside them, someone has scraped another name away.");return true;}
            noticeGouge(p,own);if(visit==4)restoreName(p,b,own);
            if(own.getBoolean("RestoredName"))cue(p,FRIEND+". Cut again beside your name. The steel gouges are still under it.");
            return true;
        }
        if(rel.equals(BorrascaRooms.DROP)||rel.equals(BorrascaRooms.DROP.below())){
            if(visit==2&&own.getBoolean("FriendMissing")){
                own.putBoolean("CompassExamined",true);own.putInt("Beat",2);
                boolean carried=BorrascaBelongings.takeAt(p,own,"Compass",BorrascaRooms.at(b,BorrascaRooms.DROP));
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
            if(visit==4){if(BorrascaBelongings.takeAt(p,own,"Patch",BorrascaRooms.at(b,rel))&&carriesEvidence(p,own))own.putBoolean("PatchRecovered",true);if(own.getBoolean("CompassConfiscated"))BorrascaBelongings.reclaim(p,own);cue(p,"The same teal pack. Her name is stitched inside the flap.");}
            return true;
        }
        // 0.4.121: the town's papers. Each visit's noticeboard and the aged notes are their own originals.
        if(rel.equals(BorrascaRooms.TRAIL_MAP)){LiteraryVignettes.open(p,PLACE,own,"Map",BorrascaTexts.document("Map"),BorrascaRooms.at(b,rel));return true;}
        // 0.4.422: the framed history hangs in the historical society now, on the street past the chapel (BorrascaShops.click).
        // 0.4.451: once the front has been papered over, the board is its own last original.
        if(Arrays.asList(BorrascaRooms.BOARD).contains(rel)){boolean covered=own.getInt("TownPostersVisit")>=BorrascaPapers.PAPERED;LiteraryVignettes.open(p,PLACE,own,covered?"BoardAfter":BorrascaTexts.boardKey(visit),covered?BorrascaTexts.boardAfter():BorrascaTexts.board(visit),BorrascaRooms.at(b,rel));return true;}
        if(rel.equals(BorrascaRooms.NOTES)){LiteraryVignettes.open(p,PLACE,own,BorrascaTexts.notesKey(visit>=3),BorrascaTexts.notes(visit>=3),BorrascaRooms.at(b,rel));return true;}
        if(visit>=2&&rel.equals(BorrascaRooms.ELI_POSTER)){
            LiteraryVignettes.open(p,PLACE,own,"EliPoster",BorrascaTexts.document("EliPoster"),BorrascaRooms.at(b,rel));
            return true;
        }
        if(visit>=3&&rel.equals(BorrascaRooms.WREN_POSTER)){
            LiteraryVignettes.open(p,PLACE,own,"WrenPoster",BorrascaTexts.document("WrenPoster"),BorrascaRooms.at(b,rel));
            return true;
        }
        // 0.4.451: any other sheet on the store's front answers a hand with what is on it.
        var sheet=p.serverLevel().getBlockState(BorrascaRooms.at(b,rel));
        if(BorrascaPaperBlock.is(sheet)&&!BorrascaPaperBlock.sheet(sheet).look.isEmpty()){cue(p,BorrascaPaperBlock.sheet(sheet).look);return true;}
        if(rel.equals(BorrascaRooms.GATE_LOG)){LiteraryVignettes.open(p,PLACE,own,"GateLog",BorrascaTexts.document("GateLog"),BorrascaRooms.at(b,rel));return true;}
        // 0.4.444 (owner: "Documents are not interactable."): the rest of the depot's paperwork, and the forms on the Sheriff's desk.
        for(var paper:PAPERS)if(rel.equals(paper.at())){LiteraryVignettes.open(p,PLACE,own,paper.key(),BorrascaTexts.document(paper.key()),BorrascaRooms.at(b,rel));own.putBoolean("Read_"+paper.key(),true);return true;}
        // 0.4.237: the desk under the depot, and the numbers behind the shed.
        if(rel.equals(BorrascaRooms.NIGHT_BOOK)){LiteraryVignettes.open(p,PLACE,own,"Ledger",BorrascaTexts.document("Ledger"),BorrascaRooms.at(b,rel));return true;}
        if(rel.equals(BorrascaRooms.LETTER)){LiteraryVignettes.open(p,PLACE,own,"Letter",BorrascaTexts.document("Letter"),BorrascaRooms.at(b,rel));return true;}
        for(int i=0;i<BorrascaRooms.GRAVE_TAGS.length;i++)if(rel.equals(BorrascaRooms.grave(i))){graves(p,own);cue(p,"A number on a stake: "+BorrascaRooms.GRAVE_TAGS[i]+". No name.");return true;}
        if(rel.equals(BorrascaRooms.EVIDENCE)){if(visit==4)LiteraryVignettes.open(p,PLACE,own,"Evidence",BorrascaTexts.document("Evidence",own.getBoolean("CompassEverConfiscated")),BorrascaRooms.at(b,rel));return true;}
        if(rel.equals(BorrascaRooms.WATER)){p.serverLevel().playSound(null,BorrascaRooms.at(b,rel),HouseReadRegistry.GLUG.get(),net.minecraft.sounds.SoundSource.BLOCKS,.55F,1F);cue(p,"A bubble goes up through the bottle.");return true;}
        // 0.4.447 (owner, of the depot's front: "No entry"): the door and its lever say why nothing happens. The lever is the way in on
        // the last visit and did nothing, without a word, on any other; the door itself has no handle and said nothing at all.
        if(rel.equals(FRONT_LEVER)){if(visit==4)openDoor(p.serverLevel(),BorrascaRooms.at(b,BorrascaRooms.FACTORY_DOOR),true);else cue(p,"The lever is chained to its bracket. It does not move.");return true;}
        if(rel.equals(BorrascaRooms.FACTORY_DOOR)||rel.equals(BorrascaRooms.FACTORY_DOOR.above())){
            var door=p.serverLevel().getBlockState(BorrascaRooms.at(b,BorrascaRooms.FACTORY_DOOR));
            if(door.getBlock() instanceof DoorBlock&&!door.getValue(DoorBlock.OPEN)){cue(p,visit==4?"Steel, and no handle on this side. A lever is set in the wall beside it.":"Steel, and no handle on this side.");return true;}
        }
        if(rel.equals(BorrascaRooms.POWER)){
            if(visit==4&&!own.getBoolean("PowerOff")){own.putBoolean("PowerOff",true);var state=p.serverLevel().getBlockState(BorrascaRooms.at(b,BorrascaRooms.MACHINE));if(state.is(BorrascaRegistry.MACHINE.get()))p.serverLevel().setBlock(BorrascaRooms.at(b,BorrascaRooms.MACHINE),state.setValue(BorrascaMachineBlock.RUNNING,false),F);var lever=p.serverLevel().getBlockState(BorrascaRooms.at(b,rel));if(lever.is(Blocks.LEVER))p.serverLevel().setBlock(BorrascaRooms.at(b,rel),lever.setValue(LeverBlock.POWERED,false),F);heard(p,BorrascaRegistry.BREAKER.get(),BorrascaRooms.at(b,rel),.7F);heard(p,BorrascaRegistry.MOTOR_STOP.get(),BorrascaRooms.at(b,BorrascaRooms.MACHINE),1);send(p,own);}
            return true;
        }
        if(rel.equals(BorrascaRooms.RELEASE)){
            if(visit==4){own.putBoolean("ReleaseOpened",true);openDoor(p.serverLevel(),BorrascaRooms.at(b,BorrascaRooms.SERVICE_DOOR),true);openDoor(p.serverLevel(),BorrascaRooms.at(b,BorrascaRooms.SERVICE_GATE),true);var s=p.serverLevel().getBlockState(BorrascaRooms.at(b,rel));if(s.is(Blocks.LEVER))p.serverLevel().setBlock(BorrascaRooms.at(b,rel),s.setValue(LeverBlock.POWERED,true),F);}
            return true;
        }
        return false;
    }
    /** 0.4.444: a paper that is only read: where it lies, and its document. */
    record Paper(BlockPos at,String key){}
    static final Paper[] PAPERS={new Paper(BorrascaRooms.DISPATCH,"Dispatch"),new Paper(BorrascaRooms.PAYROLL,"Payroll"),new Paper(BorrascaRooms.SIGN_IN,"SignIn"),new Paper(BorrascaRooms.INTAKE,"Intake"),new Paper(BorrascaRooms.TAGS,"Tags"),new Paper(BorrascaRooms.FORMS,"Forms")};
    private static void restoreName(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!own.getBoolean("PowerOff")||!own.getBoolean("EvidenceRead")||!carriesEvidence(p,own)||own.getBoolean("RestoredName"))return;
        own.putBoolean("RestoredName",true);bark(p,b,4);heard(p,BorrascaRegistry.CARVE.get(),BorrascaRooms.at(b,BorrascaRooms.CARVING),.6F);
    }
    public static void openDoor(ServerLevel l,BlockPos at,boolean open){for(int y=0;y<2;y++){var s=l.getBlockState(at.above(y));if(s.getBlock() instanceof DoorBlock)l.setBlock(at.above(y),s.setValue(DoorBlock.OPEN,open),F);}}

    // ------------------------------------------------------------------ words
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        // 0.4.439: Dana does not answer, take a hand, or look round (the owner: "She does not respond to the player").
        if(a.role()==LiteraryActor.BORRASCA_KYLE&&a.phase()==DANA){BorrascaFeeding.reached(p,own);save(p,own);return;}
        if(BorrascaPeople.talk(p,a,own))return; // 0.4.425: the shops' keepers and Earl in the cell
        int visit=own.getInt("Visit");
        if(a.role()==LiteraryActor.BORRASCA_KIMBER){
            if(visit==4&&a.phase()==GROWN){grownTalk(p,a,own);return;}
            if(own.getBoolean("FriendMissing")||visit>=3)return;
            if(visit==1){
                if(knifeTalk(p,a,own))return;
                if(!own.getBoolean("MetFriend")){own.putBoolean("MetFriend",true);return;}
                if(!own.getBoolean("PlayDone")&&own.getInt("ChildStep")==0)return;
                if(own.getBoolean("KimberAtFort")&&!own.getBoolean("LegendDone"))return;
                idleFriend(p,a,own);
            }
            else if(own.getBoolean("Read_ContentNote")){
                if(!own.getBoolean("FollowAgain")){
                    own.putBoolean("FollowAgain",true);say(p,a,"Someone scratched another name out. Come and see. Then we'll find where the sound starts.");
                }else if(own.getInt("TakingTicks")==0)idleFriend(p,a,own);
            }
            else{say(p,a,"I'll wait here.");house(p,"A note is waiting by the door.");}
        }else if(townChild(a.role(),a.phase())){
            talkTownChild(p,a,own,visit);
        }else if(a.role()==LiteraryActor.BORRASCA_SHERIFF){
            if(visit>=3&&own.getBoolean("SheriffDead"))return;
            if(visit>=3&&own.getBoolean("SheriffDown")){sheriffPleads(p,a,own);return;}
            if(visit>=3&&own.getInt("SheriffHits")>0){say(p,a,"Stay back.");return;}
            if(visit<3){
                a.gesture(1);own.putInt("HatTicks",40);
                if(visit==2&&own.getBoolean("FriendMissing")){int n=own.getInt("SheriffAfter");own.putInt("SheriffAfter",n+1);say(p,a,SHERIFF_AFTER[Math.min(n,SHERIFF_AFTER.length-1)]);}
                else say(p,a,"You two keep this side of Marker Two when the wind comes up.");
                own.putBoolean("MetSheriff",true);
            }else if(visit==3){
                denial(p,a,own);if(presentingCompass(p,own))compassChoice(p);
            }else if(own.getBoolean("PowerOff"))sheriffShutdownLine(p,a,own);
            else say(p,a,"Yard's closed. Go back the way you came.");
        }
        else if(a.role()==LiteraryActor.BORRASCA_PRESCOTT&&a.phase()<WORKER){own.putBoolean("MetPrescott",true);say(p,a,visit==1?"Evening. Tell your folks the delivery's at the trailhead.":"This is a work entrance.");}
    }
    public static boolean worker(int role,int phase){return role==LiteraryActor.BORRASCA_KYLE&&(phase==WORKER||phase==WORKER+1);}

    public static boolean townChild(int role,int phase){
        return role==LiteraryActor.BORRASCA_KYLE&&(phase==ELI||phase==WREN);
    }

    private static void talkTownChild(ServerPlayer p,LiteraryActor child,CompoundTag own,int visit){
        if(child.isInvisible())return;
        if(BorrascaArcadeFriends.talk(p,child,own))return; // 0.4.446: at the cabinets, they have other things to say
        boolean eli=child.phase()==ELI;
        String key=(eli?"Eli":"Wren")+"Talk"+visit; // 0.4.421: the keys keep the old first names (the children are now Patrick and Kendra, 0.4.424)
        int said=own.getInt(key);
        String[] lines=eli?new String[]{
            "I can ride all the way to the fort. Kendra says pushing on the steep part doesn't count.",
            "My brother and I count the knocks from our porch. If we get four he says the last one's somebody coming down.",
            "The Sheriff heard us counting once. He said it was a truck backing up. My brother asked him why it never got any closer.",
            "If my bike's still at the store, I'm up by the second marker. Tell Kendra I'll wait."
        }:visit==1?new String[]{
            "If Patrick says he rode all the way up, ask him to show you his knees.",
            "Dana taught me how to get the chain back on. You turn the bike over first so it looks like it's dead.",
            "Her mom still goes up to the tree. She puts her thumb in the D where the trunks join. I watched her do it once.",
            "The store took Dana's picture down when school started. Her mom put it right back up."
        }:new String[]{
            "They keep saying he went to his dad's. His bike was still at the store. I walked it back to his mom.",
            "I went to his dad's. He asked me if I'd seen Patrick.",
            "I used to think if your name was up there, nobody could take you.",
            "I'm going to the tree. If he comes back he'll look for his name first."
        };
        if(eli)own.putBoolean("MetEli",true);
        else own.putBoolean(visit==1?"MetWren":"WrenRememberedEli",true);
        // 0.4.445: each of them knows whose comic the reader is carrying, and says so once before anything else.
        String comic=BorrascaComic.child(p,own,eli,visit);
        // 0.4.446: and Kendra, the next time, has one thing to say about the afternoon at the cabinets.
        if(comic==null&&!eli)comic=BorrascaArcadeFriends.later(own,visit);
        if(comic==null)own.putInt(key,(said+1)%lines.length);
        say(p,child,comic!=null?comic:lines[said%lines.length]);
        float yaw=toward(child,p);
        child.setYRot(yaw);child.setYBodyRot(yaw);child.setYHeadRot(yaw);
    }

    /** How many cuts the reader's own name takes. */
    public static final int CUTS=4;
    /**
     * 0.4.429: her pocketknife. When she has cut her name she holds it out; the reader takes it from her, cuts their own name with it
     * and gives it back. Returns whether the knife was what this was about.
     */
    private static boolean knifeTalk(ServerPlayer p,LiteraryActor friend,CompoundTag own){
        if(!own.getBoolean("FriendCarved")||own.getBoolean("KnifeBack"))return false;
        boolean held=BorrascaBelongings.has(p,own,BorrascaBelongings.KNIFE);
        if(!own.getBoolean("PactCarved")){
            if(held){say(p,friend,"Under mine. Press hard or it won't last.");return true;}
            if(!BorrascaBelongings.hand(p,own,BorrascaBelongings.KNIFE)){say(p,friend,"Your hands are full.");return true;}
            friend.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);friend.gesture(2);own.putBoolean("KnifeTaken",true);
            say(p,friend,"Careful. It's sharp.");return true;
        }
        if(!held)return false;
        giveBack(p,friend,own,false);return true;
    }
    /** The knife goes back to her pocket: handed over, or taken back. */
    private static void giveBack(ServerPlayer p,LiteraryActor friend,CompoundTag own,boolean taken){
        if(BorrascaBelongings.remove(p,own,BorrascaBelongings.KNIFE).isEmpty())return;
        own.putBoolean("KnifeBack",true);own.remove("KnifeWait");friend.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);friend.gesture(0);
        if(taken)cue(p,FRIEND+" takes her knife back out of your hand and folds it.");else say(p,friend,"Thanks. My dad counts his knives.");
    }
    /** After the names, she wants her knife; if it is not given, she takes it. */
    private static void knifeBack(ServerPlayer p,LiteraryActor friend,CompoundTag own){
        if(!own.getBoolean("PactCarved")||own.getBoolean("KnifeBack")||!BorrascaBelongings.has(p,own,BorrascaBelongings.KNIFE))return;
        int wait=own.getInt("KnifeWait")+5;own.putInt("KnifeWait",wait);
        if(wait==80){friend.gesture(4);say(p,friend,"Can I have my knife back?");}
        else if(wait>=400&&p.distanceToSqr(friend)<9)giveBack(p,friend,own,true);
    }
    private static void idleFriend(ServerPlayer p,LiteraryActor friend,CompoundTag own){
        String[] lines=own.getBoolean("KimberAtFort")?new String[]{
            "If you get here first, save me the window side.",
            "I can tell where the store is from here. You can't see the road, but I know where it bends.",
            "Our names are on it now. That means he can't have us."
        }:new String[]{
            "When I get lost, I pick one thing I remember passing. Then I go back to that.",
            "You can hold the compass when we come back. I know this part.",
            "Patrick says he can ride all the way up. I want to see him try that with his chain the way it is."
        };
        int line=own.getInt("NellSmallTalk")%lines.length;
        own.putInt("NellSmallTalk",line+1);say(p,friend,lines[line]);
    }

    private static void greetChildren(ServerPlayer p,CompoundTag own,int visit,@Nullable LiteraryActor eli,@Nullable LiteraryActor wren){
        if(visit>2||visit==2&&own.getInt("TakingTicks")>0)return;
        for(var child:new LiteraryActor[]{eli,wren}){
            if(child==null||child.isInvisible()||p.distanceToSqr(child)>36||!p.hasLineOfSight(child))continue;
            String key=child.phase()==ELI?"EliTrailGreeting":"WrenTrailGreeting"+visit;
            if(own.getBoolean(key))continue;
            if(visit==1&&own.getBoolean("AlongStarted")){own.putBoolean(key,true);continue;} // 0.4.433: on the trail with Kelly, not in town
            own.putBoolean(key,true);
            if(recentSpeech(p)){
                // Still let a passing reader meet the child without replacing Nell's current subtitle.
                child.say(visit==2?"I'm waiting for Patrick.":child.phase()==ELI?"Kelly's down by the sign.":"Hi. Watch out for Patrick's bike.");
                child.getPersistentData().putInt("BorrascaSaid",child.tickCount);
            }else talkTownChild(p,child,own,visit);
            return;
        }
    }

    private static boolean presentingCompass(ServerPlayer p,CompoundTag own){return BorrascaBelongings.valid(p,own,"Compass",p.getMainHandItem())||BorrascaBelongings.valid(p,own,"Compass",p.getOffhandItem());}
    private static void denial(ServerPlayer p,LiteraryActor sheriff,CompoundTag own){if(own.getBoolean("HeardDenial"))return;own.putBoolean("HeardDenial",true);sheriff.gesture(2);say(p,sheriff,presentingCompass(p,own)?"Put that in my hand. There was nobody up there with you. You should have stayed this side of Marker Three.":"There is no case to reopen. You were alone up there. You should have stayed this side of Marker Three.");}
    private static void compassChoice(ServerPlayer p){
        var icons=new HashMap<Integer,ItemStack>();icons.put(0,LiteraryChoiceMenu.icon(BorrascaRegistry.COMPASS.get(),"Hand over the compass"));icons.put(8,LiteraryChoiceMenu.icon(Items.PAPER,"Keep it"));
        LiteraryChoiceMenu.open(p,"The Sheriff holds out his hand",icons,()->BorrascaSites.inside(p)&&near(p,BorrascaRooms.at(BorrascaSites.base(p.server,p.getUUID()),BorrascaRooms.SHERIFF),8),slot->{if(slot==8)return true;if(slot!=0)return false;var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);if(own.getInt("Visit")==3&&BorrascaBelongings.confiscate(p,own)){var sheriff=actor(p,own,"CastSheriff");if(sheriff!=null)say(p,sheriff,"That's county property now.");}save(p,own);return true;});
    }

    // ------------------------------------------------------------------ the cast
    /** 0.4.426: the friend, while she is still about on a childhood visit (BorrascaPeople has her put in her change). */
    static @Nullable LiteraryActor friend(ServerPlayer p,CompoundTag own){var a=actor(p,own,"CastKimber");return a==null||a.isInvisible()||own.getBoolean("FriendMissing")||own.getInt("Visit")>=3?null:a;}
    private static @Nullable LiteraryActor actor(ServerPlayer p,CompoundTag own,String key){return own.hasUUID(key)&&p.serverLevel().getEntity(own.getUUID(key)) instanceof LiteraryActor a?a:null;}
    /** Each of the two men has his own face (0.4.262): the first plant worker on phase WORKER, the second on WORKER + 1. */
    static int workerPhase(String key){return key.equals("Worker0")?WORKER:key.equals("Worker1")?WORKER+1:0;}
    private static LiteraryActor ensure(ServerPlayer p,BlockPos b,CompoundTag own,String key,int role){
        var known=actor(p,own,key);if(known!=null||own.hasUUID(key))return known;var at=b.offset(BorrascaCast.target(key,own.getInt("Visit")));if(!p.serverLevel().areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return null;
        var a=LiteraryRegistry.ACTOR.get().create(p.serverLevel());if(a==null)return null;a.bind(PLACE,p.getUUID());a.appearance(role,switch(key){case "TownEli"->ELI;case "TownWren"->WREN;case "CastDana"->DANA;default->workerPhase(key);});a.moveTo(Vec3.atBottomCenterOf(at),180,0);if(!p.serverLevel().addFreshEntity(a))return null;own.putUUID(key,a.getUUID());return a;
    }
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        if(LabyrinthDoors.isBusy(p))return;if(!own.getBoolean("Here")){if(own.getBoolean("BorrascaDeparted"))return;own.putBoolean("Here",true);own.remove("ExitEligible");BorrascaArcadeFriends.arrived(p,own);}
        own.putInt("TrailClock",(own.getInt("TrailClock")+5)%60000);int visit=own.getInt("Visit");
        var standing=gentleman(own);if(standing!=null&&!own.getBoolean("SawGentleman")&&seesGentleman(p,standing,gentScale(own)))own.putBoolean("SawGentleman",true);if(own.getInt("GentTicks")>0)own.putInt("GentTicks",visit<=2?Math.max(0,own.getInt("GentTicks")-5):0);if(!own.getBoolean("CastRecalled"))BorrascaCast.prepare(p,b,own);
        var friend=ensure(p,b,own,"CastKimber",LiteraryActor.BORRASCA_KIMBER);var sheriff=ensure(p,b,own,"CastSheriff",LiteraryActor.BORRASCA_SHERIFF);var prescott=ensure(p,b,own,"CastPrescott",LiteraryActor.BORRASCA_PRESCOTT);
        var w0=ensure(p,b,own,"Worker0",LiteraryActor.BORRASCA_KYLE);var w1=ensure(p,b,own,"Worker1",LiteraryActor.BORRASCA_KYLE);
        var dana=visit==4?ensure(p,b,own,"CastDana",LiteraryActor.BORRASCA_KYLE):null; // 0.4.439: only on the last night
        var eli=ensure(p,b,own,"TownEli",LiteraryActor.BORRASCA_KYLE);
        var wren=ensure(p,b,own,"TownWren",LiteraryActor.BORRASCA_KYLE);
        if(eli!=null)eli.setInvisible(visit>=2);
        if(wren!=null)wren.setInvisible(visit>=3);
        if(!own.getBoolean("CastRecalled")&&eli!=null&&wren!=null&&friend!=null&&sheriff!=null&&prescott!=null&&w0!=null&&w1!=null){
            boolean all=true;for(String key:BorrascaCast.KEYS){var a=actor(p,own,key);all&=BorrascaCast.recall(p,a,b.offset(BorrascaCast.destination(own,key,b)));}if(all){own.putBoolean("CastRecalled",true);own.putInt("CastVisit",visit);BorrascaCast.release(p.getUUID());}
        }
        for(String key:BorrascaCast.KEYS)BorrascaCast.remember(own,key,actor(p,own,key));
        townPosters(p,b,own,visit);
        BorrascaShops.years(p.serverLevel(),b,visit>=3); // 0.4.454: the station's board shows the reader's own year's prices
        townBike(p,b,own,visit);
        BorrascaComic.keep(p,b,own,visit); // 0.4.445: the comic in the fort's window, for as long as it is left there
        BorrascaPeople.tick(p,b,own,visit); // 0.4.425: the keepers at their counters, the purse for this visit, the bar's door
        scene(p,b,own);
        if(!own.getBoolean("CastRecalled"))return;
        cast(own,visit,prescott,w0,w1);
        if(visit<=2&&sheriff!=null&&own.getInt("HatTicks")>0){
            int hat=Math.max(0,own.getInt("HatTicks")-5);
            own.putInt("HatTicks",hat);
            if(hat==0)sheriff.gesture(0);
        }
        BorrascaArcadeFriends.tick(p,b,own,eli,wren); // 0.4.446: Patrick and Kendra at the drugstore's cabinets
        if(visit==1&&!BorrascaArcadeFriends.on(own))tagAlong(p,b,own,friend,eli,wren);
        switch(visit){
            case 1->visitOne(p,b,own,friend,sheriff,prescott);
            case 2->visitTwo(p,b,own,friend,w0,w1);
            case 3->visitThree(p,b,own,friend,sheriff);
            default->{visitFour(p,b,own,friend,sheriff);BorrascaFeeding.tick(p,b,own,dana,w0,w1);}
        }
        if((visit!=1||!own.getBoolean("KimberAtFort")||own.getBoolean("LegendDone"))&&!BorrascaArcadeFriends.on(own))greetChildren(p,own,visit,eli,wren);
        boolean light=lit(own);if(own.getInt("Present")%20==0||light||light!=own.getBoolean("LightSent")){own.putBoolean("LightSent",light);send(p,own);}
    }
    /** The ridge light is on from the moment she sees someone until she is gone. */
    static boolean lit(CompoundTag own){return own.getInt("TakingTicks")>0&&!own.getBoolean("FriendMissing");}
    /** Who is about on this visit: no men on the ridge in childhood until they come for her; men at work behind the wire years later. The Sheriff has his own rounds on the last night. */
    private static void cast(CompoundTag own,int visit,@Nullable LiteraryActor prescott,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1){
        boolean taking=visit==2&&own.getInt("TakingTicks")>0;
        for(var w:new LiteraryActor[]{w0,w1}){if(w==null)continue;if(w.phase()==WORKER&&w==w1)w.appearance(LiteraryActor.BORRASCA_KYLE,WORKER+1);if(visit==3){w.setInvisible(false);w.gesture(0);}else if(!taking&&!(visit==4&&BorrascaFeeding.workersOut(own))){w.setInvisible(true);BorrascaCast.stop(w);}}
        if(prescott!=null){prescott.setInvisible(visit>=4);if(visit>=4)BorrascaCast.stop(prescott);}
    }

    // ------------------------------------------------------------------ the first visit: the pact
    private static void visitOne(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor friend,@Nullable LiteraryActor sheriff,@Nullable LiteraryActor prescott){
        if(sheriff!=null&&!own.getBoolean("MetSheriff")&&near(p,sheriff.blockPosition(),8))talk(p,sheriff,own);
        if(prescott!=null&&!own.getBoolean("MetPrescott")&&BorrascaRooms.rel(p,b).z< -22&&BorrascaRooms.rel(p,b).z> -34&&near(p,prescott.blockPosition(),13))talk(p,prescott,own);
        if(friend==null)return;
        friend.setInvisible(false);friend.appearance(LiteraryActor.BORRASCA_KIMBER,0);if(!own.getBoolean("CompassIssued"))friend.setItemSlot(EquipmentSlot.OFFHAND,BorrascaBelongings.original(p,own,"Compass"));
        pastMarkerTwo(p,b,own,friend);approach(p,b,own,true);
        if(!own.getBoolean("KimberAtFort")||own.getBoolean("LegendDone"))childhoodEcho(p,b,own,friend);
        pact(p,b,own,friend);
    }
    private static boolean inFort(ServerPlayer p,BlockPos b){var r=BorrascaRooms.rel(p,b);return r.y>=5.9&&r.z< -89&&r.z> -100&&Math.abs(r.x)<6;}
    private static void pact(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend){
        if(!own.getBoolean("TreeReached")&&inFort(p,b))own.putBoolean("TreeReached",true);
        boolean ahead=BorrascaRooms.rel(p,b).z< -40;
        if(!own.getBoolean("MetFriend")){
            if(near(p,friend.blockPosition(),6))talk(p,friend,own);
            else if(ahead&&!own.getBoolean("CalledAfter")){own.putBoolean("CalledAfter",true);say(p,friend,"Hey! Wait for me!");}
            if(!own.getBoolean("MetFriend")&&!ahead){BorrascaCast.stop(friend);return;}
        }
        if(ahead)own.putBoolean("PlayDone",true);
        if(BorrascaPlay.tick(p,b,own,friend))return;
        // 0.4.446: on her way up the street she comes into the drugstore with the others while the reader is at the cabinets.
        if(!own.getBoolean("KimberAtFort")&&BorrascaArcadeFriends.kelly(p,b,own,friend))return;
        // She goes to the Tree at her own pace, whether the reader is beside her, behind her or already there.
        if(!own.getBoolean("KimberAtFort")){walkTalk(p,own,friend,WALK_TALK,"WalkTalk");if(!follow(p,b,own,friend,"ChildStep",CHILD_WALK))return;own.putBoolean("KimberAtFort",true);}
        if(!own.getBoolean("LegendDone")){
            // 0.4.445 (owner: "Let her tuck a borrowed comic into that sill for you"): the window settled, she crosses to it with
            // Patrick's comic and comes back, whether or not the reader stays up there to watch. The ridge waits for her.
            if(BorrascaComic.tucking(own)){if(BorrascaComic.tuck(p,b,own,friend))own.putInt("FortPlayTicks",Math.max(own.getInt("FortPlayTicks"),FORT_AFTER_COMIC));return;}
            BorrascaCast.stop(friend);
            if(inFort(p,b))fort(p,b,own,friend);
            else if(!own.getBoolean("TreeReached")&&!own.getBoolean("CalledUp")&&near(p,BorrascaRooms.at(b,FORT),12)){
                own.putBoolean("CalledUp",true);say(p,friend,"Up here! There's a ladder around the other side.");
            }
            return;
        }
        // Down the ladder to the trunk; she carves once the reader is near enough to see it.
        if(!own.getBoolean("FriendCarved")){
            if(!follow(p,b,own,friend,"DownStep",DOWN))return;
            if(!near(p,BorrascaRooms.at(b,BorrascaRooms.CARVING),10)){friend.gesture(0);if(!own.getBoolean("CalledDown")){own.putBoolean("CalledDown",true);say(p,friend,"Come down here. Bring your name.");}return;}
            friend.gesture(3);friend.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(LiteraryRegistry.KNIFE.get()));int n=own.getInt("CarveTicks")+5;own.putInt("CarveTicks",n);
            if(n>=40){own.putBoolean("FriendCarved",true);bark(p,b,1);friend.gesture(4);heard(p,BorrascaRegistry.CARVE.get(),BorrascaRooms.at(b,BorrascaRooms.CARVING),.5F);say(p,friend,"Your turn. Take the knife.");}
            return;
        }
        knifeBack(p,friend,own);
    }
    /** 0.4.445: where the fort's clock stands when she is back from the window, a few seconds before the ridge is heard. */
    public static final int FORT_AFTER_COMIC=260;
    /** The room belongs to them for a little while before the ridge intrudes. */
    private static void fort(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend){
        int play=own.getInt("FortPlayTicks");
        if(play<320&&!own.getBoolean("FortEcho")){
            play+=5;own.putInt("FortPlayTicks",play);
            if(play==5)say(p,friend,"This is our house. Pick a window. I'll see which way it faces.");
            var r0=BorrascaRooms.rel(p,b);double x=r0.x,z=r0.z;
            if(!own.getBoolean("SharedWindow")&&play>=100&&play<=220&&z>=-97&&z<-93&&(x<-3||x>4)){
                own.putBoolean("SharedWindow",true);own.putString("FortWindow",x<-3?"west":"east"); // 0.4.445: which one is kept
                friend.gesture(BorrascaPlay.COMPASS);
                say(p,friend,x<-3
                        ?"West. We can leave a lamp there. If you come up after me, I'll leave it on."
                        :"East. That's your window, then. If you come up after me, I'll wait right there.");
            }else if(play==220&&!own.getBoolean("SharedWindow")){
                own.putString("FortWindow","west"); // 0.4.445: a reader who chose none is given hers
                friend.gesture(BorrascaPlay.COMPASS);
                say(p,friend,"I'll leave a lamp in the window. That way you'll know if I'm here first.");
            }
            return;
        }
        if(!own.getBoolean("FortEcho")){
            friend.gesture(0);
            echo(p,b,own,.85F);own.putBoolean("FortEcho",true);
            return;
        }
        int t=own.getInt("LegendTicks")+5;own.putInt("LegendTicks",t);
        String line=switch(t){
            case FORT_LISTEN_TICKS->"That's the Shiny Gentleman. The bigger kids say he waits past the second marker. I only ever hear him.";
            case FORT_LISTEN_TICKS+110->"You're supposed to put your name on the tree before you come up. If you don't, he takes you. We didn't.";
            case FORT_LISTEN_TICKS+220->"Some of those names were there when my mom was little. She says the bark grows around them.";
            case FORT_LISTEN_TICKS+320->"Follow me! There's room for both of us down by the roots.";
            default->null;
        };
        if(line!=null)say(p,friend,line);
        if(t>=FORT_LISTEN_TICKS+420)own.putBoolean("LegendDone",true);
    }
    /** A child who goes past the second marker hears it, the noise echoing off the ridge; the friend has something to say about it later. */
    private static void pastMarkerTwo(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend){
        int visit=own.getInt("Visit");if(visit>2||own.getBoolean("FriendMissing"))return;
        if(BorrascaRooms.rel(p,b).z<MARKER_TWO_Z&&!own.getBoolean("PastTwo"+visit)){own.putBoolean("PastTwo"+visit,true);echo(p,b,own,.9F);}
        if(own.getBoolean("PastTwo"+visit)&&!own.getBoolean("WarnedTwo"+visit)&&BorrascaRooms.rel(p,b).z>MARKER_TWO_Z+3&&near(p,friend.blockPosition(),8)){own.putBoolean("WarnedTwo"+visit,true);say(p,friend,"You went past the second marker? Don't go that far again.");}
    }
    /** The noise from the ridge: three strokes, heard wherever the reader is, the dust lifting with it. */
    private static void childhoodEcho(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend){
        int clock=own.getInt("TrailClock");
        if(clock<240||p.distanceToSqr(friend)>100||Math.floorMod(clock-own.getInt("EchoAt"),60000)<360)return;
        if(recentSpeech(p))return;
        echo(p,b,own,.65F);
    }

    private static void echo(ServerPlayer p,BlockPos b,CompoundTag own,float volume){
        // Keep the ridge's bearing, within the client's positional sound range even from town.
        var ridge=BorrascaRooms.at(b,BorrascaRooms.MACHINE).getCenter();
        var offset=ridge.subtract(p.position());
        var source=offset.lengthSqr()>36?p.position().add(offset.normalize().scale(6)):ridge;
        heard(p,BorrascaRegistry.CHIME.get(),BlockPos.containing(source),volume);dust(p);own.putInt("Echoes",own.getInt("Echoes")+1);own.putInt("EchoAt",own.getInt("TrailClock"));if(own.getInt("Visit")<=2)glimpse(p,b,own);}
    /** 0.4.236: while the strokes last, a child may see him standing in the pines past the second marker. Only that reader sees him. */
    private static void glimpse(ServerPlayer p,BlockPos b,CompoundTag own){
        var at=gentlemanSpot(p,b);float scale;
        if(at!=null)scale=gentScale(at.distanceTo(p.getEyePosition()));
        else{var ridge=ridgeSpot(p,b);if(ridge==null)return;at=ridge.at();scale=ridge.scale();}
        own.putFloat("GentScale",scale);own.remove("GentBehind");
        own.putDouble("GentX",at.x);own.putDouble("GentY",at.y);own.putDouble("GentZ",at.z);own.putInt("GentTicks",GENT_TICKS);own.putInt("Gentlemen",own.getInt("Gentlemen")+1);send(p,own);
    }
    /** 0.4.434: for tests, the noise's glimpse of him from where the reader is now; where he stands, or null if nowhere is in sight. */
    public static @Nullable Vec3 glimpseFor(ServerPlayer p,CompoundTag own){var b=BorrascaSites.base(p.server,p.getUUID());if(b==null)return null;glimpse(p,b,own);return gentleman(own);}
    /**
     * Where a child can see him from here: on the ground among the pines past the second marker, GENT_NEAR to GENT_FAR blocks off, with
     * room for him at his height there and his head in clear sight, preferring what is already in front of them.
     */
    static @Nullable Vec3 gentlemanSpot(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();var eye=p.getEyePosition();var look=p.getLookAngle();Vec3 best=null;double bestScore=-9;
        for(int z=MARKER_TWO_Z-4;z>=-165;z-=3)for(int x=-30;x<=30;x+=3){
            var column=BorrascaRooms.at(b,x,0,z);if(!l.hasChunkAt(column))continue;
            int y=l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,column.getX(),column.getZ());
            if(y<column.getY()-1||y>column.getY()+16)continue;var feet=new BlockPos(column.getX(),y,column.getZ());
            // On the ground, not on a trunk or a rock, with room to stand.
            var under=l.getBlockState(feet.below());if(under.is(net.minecraft.tags.BlockTags.LOGS)||under.is(net.minecraft.tags.BlockTags.LEAVES)||under.is(BorrascaRegistry.FITTING.get()))continue;
            var base=Vec3.atBottomCenterOf(feet);double far=base.distanceTo(eye);if(far<GENT_NEAR-GENT_HEIGHT||far>GENT_FAR)continue;double tall=GENT_HEIGHT*gentScale(far);
            boolean room=true;for(int i=0;i<Math.ceil(tall)&&room;i++)room=l.getBlockState(feet.above(i)).getCollisionShape(l,feet.above(i)).isEmpty();if(!room)continue;
            var head=base.add(0,tall-.4,0);double d=head.distanceTo(eye);if(d<GENT_NEAR||d>GENT_FAR)continue;
            if(l.clip(new ClipContext(eye,head,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p)).getType()!=HitResult.Type.MISS)continue;
            double score=look.dot(head.subtract(eye).normalize())+p.getRandom().nextDouble()*.2;if(score>bestScore){bestScore=score;best=base;}
        }
        return best;
    }
    /** Where he stands, and how tall. */
    record Spot(Vec3 at,float scale){}
    /** The most he grows to show over the pines from far off. */
    static final float RIDGE_SCALE_MAX=3.6F;
    /**
     * 0.4.440 (the owner: "the shiny gentleman's visage is supposed to flash in the distance, to loom over the player when they're a child -
     * their active imagination. that was in there, and now it's not triggering"). Under the pines a clear place with his head in sight is
     * rare, and then the noise showed nothing. Failing one, he stands up the ridge anyway, the way the noise comes from, past the second
     * marker, tall enough that his hat and face stand over the tops of the pines there; in sight if any such place is, and otherwise where
     * the child is looking.
     */
    static @Nullable Spot ridgeSpot(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();var eye=p.getEyePosition();var look=p.getLookAngle();var toward=BorrascaRooms.at(b,BorrascaRooms.MACHINE).getCenter().subtract(eye);
        double bearing=Math.atan2(toward.z,toward.x);Spot best=null;double bestScore=-9;
        for(double far=40;far<=104;far+=8)for(int k=-5;k<=5;k++){
            double angle=bearing+k*.14,x=eye.x+Math.cos(angle)*far,z=eye.z+Math.sin(angle)*far;var column=BlockPos.containing(x,eye.y,z);if(!l.hasChunkAt(column))continue;
            double rz=BorrascaRooms.authored(column.subtract(b)).getZ();if(rz>=MARKER_TWO_Z-2||rz<=-166)continue;
            int y=l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,column.getX(),column.getZ());
            int crowns=l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,column.getX(),column.getZ());if(Math.abs(y-eye.y)>40)continue;
            var base=new Vec3(column.getX()+.5,y,column.getZ()+.5);double d=base.distanceTo(eye);
            float scale=(float)Mth.clamp(Math.max(GENT_HEIGHT*gentScale(d),crowns-y+3)/GENT_HEIGHT,1,RIDGE_SCALE_MAX);
            var head=base.add(0,GENT_HEIGHT*scale-.4,0);
            boolean seen=l.clip(new ClipContext(eye,head,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;
            double score=look.dot(head.subtract(eye).normalize())+(seen?3:0)+(head.y>crowns?.5:0)+p.getRandom().nextDouble()*.2;
            if(score>bestScore){bestScore=score;best=new Spot(base,scale);}
        }
        return best;
    }
    // ------------------------------------------------------------------ 0.4.440: past the second marker
    /*
     * The owner: "have the klangs get closer while the player is past the mile marker and grow more frequent. the gentleman grows closer
     * to a child player with each until it's directly above them. it picks them up and eats them after that, sending the player to the
     * triple tree again"; and "the monster is the child's imagination". A child past the second marker hears the machine strike, and with
     * each stroke he stands nearer, the stroke coming from where he stands, louder and sooner than the last: NEARER blocks off, then
     * directly over the child. Then he bends down and takes them (GRAB_TICKS: lifted, held, the view goes into his face and to black;
     * nothing of the child is shown), and they are under the Triple Tree with their back to the trunk. Going back below the marker stops
     * the strokes, and he falls back a step for every FALL_BACK ticks spent there. Visit one always; visit two before Kelly leads them up
     * and after the compass is found, never on her way to the clearing.
     */
    public static final double[] NEARER={64,44,30,20,12,6};
    public static final int[] NEARER_GAP={150,115,85,65,50,40};
    public static final int NEARER_FIRST=60,OVERHEAD=30,GRAB_TICKS=60,FALL_BACK=100;
    /** How far off his feet are when he stands over the child: far enough that, bent, his shoulders are over them. */
    public static final double OVER=2.5;
    /** How tall he stands this near: taller for distance far off (gentScale), and taller again as he comes over the child. */
    public static float approachScale(double distance){return (float)Math.max(gentScale(distance),Math.min(GENT_SCALE_MAX,1+(NEARER[0]-distance)/NEARER[0]*1.6));}
    private static void approach(ServerPlayer p,BlockPos b,CompoundTag own,boolean allowed){
        int grab=own.getInt("GrabTicks");if(grab>0){grabbed(p,b,own,grab);return;}
        int step=own.getInt("Approach");
        boolean past=allowed&&own.getInt("Visit")<=2&&BorrascaRooms.rel(p,b).z<MARKER_TWO_Z;
        if(!past){
            own.remove("ApproachArmed");
            if(step>0){int away=own.getInt("ApproachAway")+5;if(away>=FALL_BACK){away=0;own.putInt("Approach",step-1);}own.putInt("ApproachAway",away);}
            return;
        }
        own.putInt("ApproachAway",0);
        if(!own.getBoolean("ApproachArmed")){own.putBoolean("ApproachArmed",true);own.putInt("ApproachWait",NEARER_FIRST);return;}
        int wait=own.getInt("ApproachWait")-5;if(wait>0){own.putInt("ApproachWait",wait);return;}
        if(step<NEARER.length){
            var at=approachSpot(p,b,NEARER[step]);klang(p,own,at,NEARER[step]);
            stand(own,at,approachScale(NEARER[step]),NEARER_GAP[step]+40);
            own.putInt("Approach",step+1);own.putInt("ApproachWait",NEARER_GAP[step]);send(p,own);return;
        }
        if(step==NEARER.length){
            // Directly over the child: his feet a step away, the rest of him above.
            var at=approachSpot(p,b,OVER);klang(p,own,at,OVER);stand(own,at,(float)GENT_SCALE_MAX,OVERHEAD+GRAB_TICKS+20);
            own.putInt("Approach",step+1);own.putInt("ApproachWait",OVERHEAD);send(p,own);return;
        }
        own.putInt("GrabTicks",GRAB_TICKS);own.putInt("Approach",0);own.remove("ApproachArmed");own.putInt("GentTicks",GRAB_TICKS+20);send(p,own);
    }
    /** The stroke, from where he stands. */
    private static void klang(ServerPlayer p,CompoundTag own,Vec3 at,double distance){
        heard(p,BorrascaRegistry.STROKE.get(),BlockPos.containing(at.add(0,GENT_HEIGHT*.8,0)),(float)Math.max(1.4,distance/12));dust(p);
        own.putInt("Echoes",own.getInt("Echoes")+1);own.putInt("EchoAt",own.getInt("TrailClock"));
    }
    private static void stand(CompoundTag own,Vec3 at,float scale,int ticks){
        own.putDouble("GentX",at.x);own.putDouble("GentY",at.y);own.putDouble("GentZ",at.z);own.putFloat("GentScale",scale);own.putInt("GentTicks",ticks);
        own.putInt("Gentlemen",own.getInt("Gentlemen")+1);own.remove("GentBehind");
    }
    /** Ground about this far from the child, on the side the noise comes from (up the ridge, toward the machine), a little to one side or the other. */
    static Vec3 approachSpot(ServerPlayer p,BlockPos b,double distance){
        var l=p.serverLevel();var eye=p.position();var toward=BorrascaRooms.at(b,BorrascaRooms.MACHINE).getCenter().subtract(eye);
        double angle=Math.atan2(toward.z,toward.x)+(p.getRandom().nextDouble()-.5)*(distance>4?.7:.3);
        double x=eye.x+Math.cos(angle)*distance,z=eye.z+Math.sin(angle)*distance;var column=BlockPos.containing(x,eye.y,z);
        if(!l.hasChunkAt(column))return new Vec3(x,eye.y,z);
        int y=l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,column.getX(),column.getZ());
        if(Math.abs(y-eye.y)>12)y=(int)Math.floor(eye.y);
        return new Vec3(x,y,z);
    }
    /** He has the child: lifted off the ground and held while his face comes down, then they are at the Triple Tree. */
    private static void grabbed(ServerPlayer p,BlockPos b,CompoundTag own,int grab){
        grab-=5;own.putInt("GrabTicks",Math.max(0,grab));
        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.LEVITATION,10,0,false,false,false));
        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,10,6,false,false,false));p.resetFallDistance();
        own.putInt("GentTicks",Math.max(own.getInt("GentTicks"),grab+20));
        if(grab==GRAB_TICKS-5){var g=gentleman(own);if(g!=null)heard(p,BorrascaRegistry.STROKE.get(),BlockPos.containing(p.getEyePosition()),2.4F);}
        if(grab>0)return;
        swallowed(p,b,own);
    }
    static void swallowed(ServerPlayer p,BlockPos b,CompoundTag own){
        own.putInt("GrabTicks",0);own.putInt("GentTicks",0);own.putInt("Approach",0);own.remove("ApproachArmed");own.putInt("ApproachAway",0);own.putInt("Swallowed",own.getInt("Swallowed")+1);
        p.removeEffect(net.minecraft.world.effect.MobEffects.LEVITATION);p.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
        // 0.4.444 (owner: "capture spawns on top of triple tree rather than inside"): inside the fort, on its floor. The foot of the
        // Tree was found from the top of whatever stood in that column, which is the fort's roof.
        var inside=fortFloor(p,b);var at=inside!=null?inside:treeFoot(p,b);p.teleportTo(p.serverLevel(),at.x,at.y,at.z,180,0);p.setDeltaMovement(Vec3.ZERO);p.resetFallDistance();p.connection.resetPosition();
        cue(p,inside==null?(own.getBoolean("PactCarved")?"The Triple Tree, at your back. Your name is in the bark beside your shoulder.":"The Triple Tree, at your back, and all the names in its bark.")
            :own.getBoolean("PactCarved")?"The fort in the Triple Tree. Your name is in the bark under the floor.":"The fort in the Triple Tree, and under its floor all the names in the bark.");
        send(p,own);
    }
    /** 0.4.444: somewhere clear to stand on the fort's floor, or null if the fort is not loaded or its floor is full. */
    static @Nullable Vec3 fortFloor(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();
        for(int[] o:new int[][]{{0,-92},{1,-92},{-1,-92},{2,-93},{0,-91},{-2,-92}}){
            var feet=BorrascaRooms.at(b,o[0],6,o[1]);if(!l.hasChunkAt(feet))continue;
            if(l.getBlockState(feet).getCollisionShape(l,feet).isEmpty()&&l.getBlockState(feet.above()).getCollisionShape(l,feet.above()).isEmpty()&&!l.getBlockState(feet.below()).getCollisionShape(l,feet.below()).isEmpty())return Vec3.atBottomCenterOf(feet);
        }
        return null;
    }
    /** Somewhere to stand at the foot of the Triple Tree, facing up the ridge, with the trunk behind. */
    static Vec3 treeFoot(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();var tree=BorrascaRooms.trail(-96);
        for(int[] o:new int[][]{{0,0},{2,0},{-2,0},{0,2},{3,1},{-3,1},{1,3},{-1,3},{4,2},{-4,2}}){
            var column=BorrascaRooms.at(b,tree.getX()+o[0],0,tree.getZ()+o[1]);if(!l.hasChunkAt(column))continue;
            int y=l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,column.getX(),column.getZ());var feet=new BlockPos(column.getX(),y,column.getZ());
            if(l.getBlockState(feet).getCollisionShape(l,feet).isEmpty()&&l.getBlockState(feet.above()).getCollisionShape(l,feet.above()).isEmpty()&&!l.getBlockState(feet.below()).getCollisionShape(l,feet.below()).isEmpty())return Vec3.atBottomCenterOf(feet);
        }
        var c=BorrascaRooms.at(b,tree.getX(),0,tree.getZ()+2);return Vec3.atBottomCenterOf(c.atY(l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,c.getX(),c.getZ())));
    }
    /** Where he stands for this reader now, or null when he is not there. */
    public static @Nullable Vec3 gentleman(CompoundTag own){return own.getInt("GentTicks")>0&&own.getInt("Visit")<=2?new Vec3(own.getDouble("GentX"),own.getDouble("GentY"),own.getDouble("GentZ")):null;}
    /** Whether the reader is looking straight at him, with nothing in the way. */
    // 0.4.242 (run 1399): where he stands is chosen with his head in clear sight; his middle can be behind a rock or a branch. Looking
    // at him counts when either is in clear sight.
    static boolean seesGentleman(ServerPlayer p,Vec3 at,float scale){
        var eye=p.getEyePosition();var look=p.getLookAngle();double tall=GENT_HEIGHT*scale;
        for(double h:new double[]{tall*.6,tall-.4}){var point=at.add(0,h,0);
            if(look.dot(point.subtract(eye).normalize())>=.95&&p.level().clip(new ClipContext(eye,point,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS)return true;}
        return false;
    }
    /** 0.4.433: Patrick and Kendra up the trail behind Kelly as far as the first marker, their goodbye there, and home again. */
    private static void tagAlong(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor friend,@Nullable LiteraryActor eli,@Nullable LiteraryActor wren){
        if(own.getBoolean("AlongDone")||eli==null||wren==null||eli.isInvisible()||wren.isInvisible())return;
        if(!own.getBoolean("AlongStarted")){
            if(friend==null||!own.getBoolean("PlayDone")||own.getInt("ChildStep")<1&&!own.getBoolean("KimberAtFort"))return;
            own.putBoolean("AlongStarted",true);
        }
        var children=new LiteraryActor[]{eli,wren};
        if(own.getBoolean("AlongHome")){
            // Back down the trail; home in town as soon as nobody sees them go.
            boolean both=true;
            for(var child:children){
                String key=child.phase()==ELI?"TownEli":"TownWren";var home=b.offset(BorrascaCast.target(key,1));
                if(child.position().distanceToSqr(Vec3.atBottomCenterOf(home))<1){BorrascaCast.stop(child);continue;}
                if(BorrascaCast.recall(p,child,home))continue;
                both=false;String step=child.phase()==ELI?"EliAlong":"WrenAlong";int i=Math.min(own.getInt(step),ALONG.length-1);
                // Back the way they came, step by step along the trail.
                var to=Vec3.atBottomCenterOf(BorrascaRooms.at(b,ALONG[Math.max(0,i-1)]));var base0=Vec3.atLowerCornerOf(b);
                if(child.position().subtract(to).horizontalDistance()<.7){if(i>0)own.putInt(step,i-1);BorrascaCast.stop(child);continue;}
                BorrascaCast.walk(child,i>0?BorrascaRooms.toward(child.position().subtract(base0),ALONG[i],ALONG[i-1]).add(base0):to,.8);
            }
            if(both){own.putBoolean("AlongDone",true);for(var child:children)BorrascaCast.remember(own,child.phase()==ELI?"TownEli":"TownWren",child);}
            return;
        }
        boolean there=true;
        // Single file on her line (the trail is narrow where the street gives out to the woods): Patrick a few steps behind her, never
        // past her, and Kendra behind him. Only at the marker do they stand side by side, either side of the trail.
        for(var child:children){
            boolean lead=child.phase()==ELI;String step=lead?"EliAlong":"WrenAlong";int i=own.getInt(step);
            if(i>=ALONG.length){BorrascaCast.stop(child);continue;}
            there=false;
            boolean last=i==ALONG.length-1;
            var ahead=lead?(own.getBoolean("KimberAtFort")?null:friend):own.getInt("EliAlong")>=ALONG.length?null:eli;
            // Behind means further back up the trail, which runs north: the trail's own progress, not the distance between them.
            if(ahead!=null&&child.getZ()<ahead.getZ()+(lead?3:2)){BorrascaCast.stop(child);continue;}
            var at=last?along(b,i,lead?-1:1):Vec3.atBottomCenterOf(BorrascaRooms.at(b,ALONG[i]));
            var base0=Vec3.atLowerCornerOf(b);
            var steer=i>0&&child.position().subtract(at).horizontalDistance()>3?BorrascaRooms.toward(child.position().subtract(base0),ALONG[i-1],ALONG[i]).add(base0):at;
            if(child.position().subtract(at).horizontalDistance()<.7&&Math.abs(child.getY()-at.y)<1.2){own.putInt(step,i+1);BorrascaCast.stop(child);}
            else BorrascaCast.walk(child,steer,.8);
        }
        if(!there){
            walkTalk(p,own,eli,ALONG_ELI,"EliAlongTalk");
            if(own.getInt("EliAlongTalk")>=ALONG_ELI.length)walkTalk(p,own,wren,ALONG_WREN,"WrenAlongTalk");
            return;
        }
        // At the marker: they face the trail up, and say goodbye to a reader near enough to hear.
        for(var child:children){float yaw=toward(child,p);child.setYHeadRot(yaw);}
        boolean near=p.distanceToSqr(eli)<ALONG_NEAR*ALONG_NEAR||p.distanceToSqr(wren)<ALONG_NEAR*ALONG_NEAR;
        if(!near){
            int wait=own.getInt("AlongWait")+5;own.putInt("AlongWait",wait);
            if(wait>=ALONG_WAIT)own.putBoolean("AlongHome",true);
            return;
        }
        if(recentSpeech(p))return;
        if(!own.getBoolean("AlongWrenSaid")){own.putBoolean("AlongWrenSaid",true);say(p,wren,FAREWELL_WREN);return;}
        own.putBoolean("AlongHeard",true);own.putBoolean("AlongHome",true);say(p,eli,FAREWELL_ELI);
    }
    /** Where a child stands at the marker: the trail's last step, a little to their own side of it. */
    private static Vec3 along(BlockPos b,int i,int side){return Vec3.atBottomCenterOf(BorrascaRooms.at(b,ALONG[i])).add(right(b,i).scale(side*.9));}
    /** The trail's right hand at a step, along the ground. */
    private static Vec3 right(BlockPos b,int i){
        var from=Vec3.atBottomCenterOf(BorrascaRooms.at(b,ALONG[Math.max(0,i-1)]));var to=Vec3.atBottomCenterOf(BorrascaRooms.at(b,ALONG[Math.max(1,i)]));
        if(i==0){from=Vec3.atBottomCenterOf(BorrascaRooms.at(b,ALONG[0]));to=Vec3.atBottomCenterOf(BorrascaRooms.at(b,ALONG[1]));}
        var d=to.subtract(from).multiply(1,0,1);if(d.lengthSqr()<1e-6)return Vec3.ZERO;d=d.normalize();return new Vec3(-d.z,0,d.x);
    }
    /** 0.4.432: the next of her walk lines (kept count under key), if the reader is beside her and it is her turn to speak. */
    private static void walkTalk(ServerPlayer p,CompoundTag own,LiteraryActor friend,String[] lines,String key){
        int said=own.getInt(key);if(said>=lines.length)return;
        if(p.distanceToSqr(friend)>WALK_TALK_NEAR*WALK_TALK_NEAR){own.putInt(key+"Wait",0);return;}
        int wait=own.getInt(key+"Wait")+5;own.putInt(key+"Wait",wait);
        if(wait<WALK_TALK_GAP||recentSpeech(p))return;
        say(p,friend,lines[said]);own.putInt(key,said+1);own.putInt(key+"Wait",0);
    }
    /** She walks a route at her own pace; she waits only for a reader far behind her, and calls them once each time. True at its end. */
    private static boolean follow(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend,String key,BlockPos[] points){
        int i=own.getInt(key);if(i>=points.length){BorrascaCast.stop(friend);return true;}var at=Vec3.atBottomCenterOf(BorrascaRooms.at(b,points[i]));
        // 0.4.420: on the long slopes she keeps to the trail, a few blocks ahead at a time.
        var base0=Vec3.atLowerCornerOf(b);var steer=i>0?BorrascaRooms.toward(friend.position().subtract(base0),points[i-1],points[i]).add(base0):at;
        // Behind means further back along the whole route, not off the line of the next step: a reader waiting up ahead is never waited for.
        var start=Vec3.atBottomCenterOf(BorrascaRooms.at(b,points[0]));var way=Vec3.atBottomCenterOf(BorrascaRooms.at(b,points[points.length-1])).subtract(start).multiply(1,0,1);
        boolean behind=friend.distanceToSqr(p)>20*20&&p.position().subtract(start).dot(way)<friend.position().subtract(start).dot(way);
        if(behind){BorrascaCast.stop(friend);if(!own.getBoolean(key+"Called")){own.putBoolean(key+"Called",true);say(p,friend,"Come on!");}return false;}
        own.putBoolean(key+"Called",false);
        var delta=friend.position().subtract(at);if(delta.horizontalDistance()<.4&&Math.abs(delta.y)<.14){own.putInt(key,i+1);BorrascaCast.stop(friend);}else BorrascaCast.walk(friend,steer,.8);return own.getInt(key)>=points.length;
    }

    // ------------------------------------------------------------------ the second visit: the taking
    private static void visitTwo(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor friend,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1){
        if(own.getBoolean("FriendMissing")){
            if(own.getInt("TakingTicks")>0&&!own.getBoolean("TakingReturned")&&accost(p,b,own,new LiteraryActor[]{w0,w1}))return;
            approach(p,b,own,own.getBoolean("CompassExamined"));
            if(friend!=null)gone(p,b,own,friend,"FriendHidden",0);
            if(w0!=null)gone(p,b,own,w0,"Taker0Hidden",-1);if(w1!=null)gone(p,b,own,w1,"Taker1Hidden",1);
            if(BorrascaBelongings.has(p,own,"Compass")){own.putBoolean("CompassExamined",true);own.putInt("Beat",2);}
            return;
        }
        if(friend==null)return;
        friend.setInvisible(false);friend.appearance(LiteraryActor.BORRASCA_KIMBER,0);if(!own.getBoolean("CompassIssued"))friend.setItemSlot(EquipmentSlot.OFFHAND,BorrascaBelongings.original(p,own,"Compass"));
        friend.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
        pastMarkerTwo(p,b,own,friend);approach(p,b,own,!own.getBoolean("FollowAgain")&&own.getInt("TakingTicks")==0);
        if(own.getInt("TakingTicks")==0)childhoodEcho(p,b,own,friend);
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
        walkTalk(p,own,friend,WALK_TALK_TWO,"WalkTalkTwo");
        if(!follow(p,b,own,friend,"TakingStep",TAKING_WALK))return;
        // At the clearing she waits for the reader, wherever they have got to, and calls for them.
        if(!near(p,BorrascaRooms.at(b,BorrascaRooms.TAKING),20)){int w=own.getInt("ClearingWait")+5;own.putInt("ClearingWait",w);if(w%200==5)say(p,friend,p.getGameProfile().getName()+"? Where'd you go?");return;}
        own.putInt("TakingTicks",1);friend.gesture(SCARED);say(p,friend,"Wait. Is that someone—");comeDown(p,b,own,w0,w1);stroke(p,b,own,1,w0,w1);send(p,own);
    }
    /**
     * The men come down the ridge road in the light (0.4.262). They are somewhere along the cutting outside the east wire where nobody can
     * see them come from, nearest first, falling back on the turning bay behind the shoulder; each wears his own face, and both walk.
     */
    private static void comeDown(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1){
        var l=p.serverLevel();int start=0;
        for(int k:SPAWNS){var at=BorrascaRooms.at(b,APPROACH[k]).getCenter().add(0,1,0);var behind=BorrascaRooms.at(b,APPROACH[Math.max(0,k-1)]).getCenter().add(0,1,0);if(!LiteraryVignettes.watched(l,at)&&!LiteraryVignettes.watched(l,behind)){start=k;break;}}
        var men=new LiteraryActor[]{w0,w1};
        for(int i=0;i<2;i++){
            var w=men[i];if(w==null)continue;var at=Vec3.atBottomCenterOf(BorrascaRooms.at(b,APPROACH[start]));var next=Vec3.atBottomCenterOf(BorrascaRooms.at(b,APPROACH[start+1]));var heading=next.subtract(at).multiply(1,0,1).normalize();var right=new Vec3(-heading.z,0,heading.x);
            var pos=at.add(right.scale(i==0?-.6:.6)).subtract(heading.scale(i*.9));float yaw=(float)Math.toDegrees(Math.atan2(-heading.x,heading.z));
            BorrascaCast.stop(w);w.moveTo(pos,yaw,0);w.setYBodyRot(yaw);w.setYHeadRot(yaw);w.setInvisible(false);w.appearance(LiteraryActor.BORRASCA_KYLE,WORKER+i);w.gesture(0);resync(p,w);
            own.putInt("Taker"+i,start+1);own.remove("Exit"+i);
        }
        own.putInt("Strokes",0);own.remove("ExitF");
    }
    /**
     * One stroke from the ridge, each where it belongs in the taking: the first with the light, the second as the men turn down the lane
     * toward her, the third as a hand closes on her arm. With the first two the Shiny Gentleman stands on the ridge behind the men,
     * taller than they are, where the reader can see him over their shoulders; he stays while they come, and fades as the last stroke dies.
     */
    private static void stroke(ServerPlayer p,BlockPos b,CompoundTag own,int n,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1){
        own.putInt("Strokes",n);heard(p,BorrascaRegistry.STROKE.get(),BorrascaRooms.at(b,BorrascaRooms.MACHINE),n==3?1.8F:1.5F);dust(p);own.putInt("Echoes",own.getInt("Echoes")+1);own.putInt("EchoAt",own.getInt("TrailClock"));
        if(n>=3){if(own.getInt("GentTicks")>0)own.putInt("GentTicks",40);return;}
        // He is only ever behind them: while they are still out of sight in the cutting there is nowhere for him to stand, and he comes
        // with the next stroke. Where the reader is already looking at him, he stays.
        var standing=gentleman(own);if(standing!=null&&seesGentleman(p,standing,gentScale(own)))return;
        var at=behind(p,b,w0,w1);if(at==null)return;
        own.putDouble("GentX",at.x);own.putDouble("GentY",at.y);own.putDouble("GentZ",at.z);own.putFloat("GentScale",1);own.putInt("GentTicks",GENT_TICKS);own.putInt("Gentlemen",own.getInt("Gentlemen")+1);own.putBoolean("GentBehind",true);
    }
    /** Ground behind the men as the reader sees them, on the ridge, with room for him and his head in the reader's sight. */
    static @Nullable Vec3 behind(ServerPlayer p,BlockPos b,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1){
        Vec3 mid=null;int n=0;for(var w:new LiteraryActor[]{w0,w1})if(w!=null&&!w.isInvisible()){mid=mid==null?w.position():mid.add(w.position());n++;}
        if(mid==null)return null;mid=mid.scale(1.0/n);var l=p.serverLevel();var eye=p.getEyePosition();var away=mid.subtract(eye).multiply(1,0,1);if(away.lengthSqr()<1)return null;away=away.normalize();
        for(int k=5;k<=18;k++)for(int sway:new int[]{0,2,-2}){
            var c=mid.add(away.scale(k)).add(new Vec3(-away.z,0,away.x).scale(sway));var column=BlockPos.containing(c.x,b.getY(),c.z);if(!l.hasChunkAt(column))continue;
            int ly=b.getY()+(int)Math.round(BorrascaRooms.lift(BorrascaRooms.authoredZ(column.getZ()-b.getZ())));
            int y=l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,column.getX(),column.getZ());if(y<ly+6||y>ly+22)continue;
            var feet=new BlockPos(column.getX(),y,column.getZ());var under=l.getBlockState(feet.below());if(under.is(net.minecraft.tags.BlockTags.LOGS)||under.is(net.minecraft.tags.BlockTags.LEAVES)||under.is(BorrascaRegistry.FITTING.get())||under.getBlock() instanceof net.minecraft.world.level.block.FenceBlock)continue;
            boolean room=true;for(int i=0;i<5&&room;i++)room=l.getBlockState(feet.above(i)).getCollisionShape(l,feet.above(i)).isEmpty();if(!room)continue;
            var base=Vec3.atBottomCenterOf(feet);var head=base.add(0,GENT_HEIGHT-.4,0);if(head.distanceTo(eye)<12)continue;
            var hit=l.clip(new ClipContext(eye,head,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));if(hit.getType()!=HitResult.Type.MISS&&hit.getLocation().distanceTo(head)>1.5)continue;
            return base;
        }
        return null;
    }
    /**
     * An actor along a route at its own pace, a little to one side of the line it walks; true once it has reached the end. A man who has
     * stood stuck for three seconds where nobody can see him is moved on to the point he was making for.
     */
    private static boolean route(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor a,String key,BlockPos[] points,double side,double pace){
        int i=own.getInt(key);if(i>=points.length){BorrascaCast.stop(a);return true;}
        var at=Vec3.atBottomCenterOf(BorrascaRooms.at(b,points[i]));var from=i>0?Vec3.atBottomCenterOf(BorrascaRooms.at(b,points[i-1])):a.position();
        var heading=at.subtract(from).multiply(1,0,1);if(heading.lengthSqr()<.01)heading=at.subtract(a.position()).multiply(1,0,1);if(heading.lengthSqr()<.01)heading=new Vec3(0,0,-1);heading=heading.normalize();
        var goal=at.add(new Vec3(-heading.z,0,heading.x).scale(side));
        if(a.position().subtract(goal).horizontalDistance()<.7){own.putInt(key,i+1);own.remove(key+"Stuck");if(i+1>=points.length){BorrascaCast.stop(a);return true;}return false;}
        // 0.4.420: down (or up) the drawn-out lane a few blocks at a time, keeping to it.
        var base0=Vec3.atLowerCornerOf(b);var steer=i>0?BorrascaRooms.toward(a.position().subtract(base0),points[i-1],points[i]).add(base0):at;if(steer!=at)steer=steer.add(new Vec3(-heading.z,0,heading.x).scale(side));else steer=goal;
        // Only the men are ever moved on; she never jumps, watched or not.
        BorrascaCast.walk(a,steer,pace);if(a.role()!=LiteraryActor.BORRASCA_KIMBER)unstick(p,own,a,key,steer);return false;
    }
    /**
     * 0.4.440: after a man is set somewhere new, tell the reader's client where he is and how he looks, at once. A client that has him gets
     * him there; one that has not yet been sent him ignores it, and is sent him whole as soon as he is in its range.
     */
    static void resync(ServerPlayer p,LiteraryActor a){
        p.connection.send(new net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket(a));
        var data=a.getEntityData().getNonDefaultValues();if(data!=null)p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket(a.getId(),data));
    }
    private static void unstick(ServerPlayer p,CompoundTag own,LiteraryActor a,String key,Vec3 goal){
        var t=own.getCompound(key+"Stuck");var last=new Vec3(t.getDouble("X"),t.getDouble("Y"),t.getDouble("Z"));int ticks=t.getInt("T")+5;
        if(!t.contains("X")||a.position().distanceToSqr(last)>.3*.3){t.putDouble("X",a.getX());t.putDouble("Y",a.getY());t.putDouble("Z",a.getZ());ticks=0;}
        if(ticks>=60&&!LiteraryVignettes.watched(p.serverLevel(),a.getEyePosition())&&!LiteraryVignettes.watched(p.serverLevel(),goal.add(0,1.6,0))){BorrascaCast.stop(a);a.moveTo(goal.x,goal.y,goal.z,a.getYRot(),0);resync(p,a);ticks=0;}
        t.putInt("T",ticks);own.put(key+"Stuck",t);
    }
    /** Where a man walks beside her: a little behind her shoulder, on his own side of the way she is going. */
    private static Vec3 beside(BlockPos b,CompoundTag own,LiteraryActor friend,int side){
        int i=Math.min(EXIT.length-1,own.getInt("ExitF"));var heading=Vec3.atBottomCenterOf(BorrascaRooms.at(b,EXIT[i])).subtract(friend.position()).multiply(1,0,1);if(heading.lengthSqr()<.04)heading=Vec3.atBottomCenterOf(BorrascaRooms.at(b,EXIT[Math.min(EXIT.length-1,i+1)])).subtract(friend.position()).multiply(1,0,1);if(heading.lengthSqr()<.01)heading=new Vec3(1,0,0);heading=heading.normalize();
        return friend.position().add(new Vec3(-heading.z,0,heading.x).scale(side*.78)).subtract(heading.scale(.12));
    }
    private static void footsteps(ServerPlayer p,LiteraryActor w,int clock){
        if(!w.getPersistentData().contains("BorrascaSpeed")||clock%10!=1&&clock%10!=6)return;
        var level=p.serverLevel();
        var sound=level.getBlockState(w.blockPosition().below()).getSoundType().getStepSound();
        LiteraryVignettes.heard(p,sound,w.blockPosition(),.7F+w.getRandom().nextFloat()*.35F,
                .58F+w.getRandom().nextFloat()*.35F);
    }
    /**
     * They walk down to her side by side, take her by the arms, and lead her back up the lane and along the ridge road toward their truck.
     * She calls the reader's name once. Her compass falls where she is pulled up the lane.
     */
    private static void taken(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1){
        int clock=Math.min(TAKEN_BY+LED_FOR+1,own.getInt("TakingTicks")+5);own.putInt("TakingTicks",clock);
        var men=new LiteraryActor[]{w0,w1};
        if(accost(p,b,own,men))return;
        if(!own.getBoolean("Led")){
            BorrascaCast.stop(friend);friend.gesture(SCARED);friend.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,men[0]!=null?men[0].getEyePosition():Vec3.atCenterOf(BorrascaRooms.at(b,BorrascaRooms.LAMP)));
            boolean reached=false,turned=false;
            for(int i=0;i<2;i++){
                var w=men[i];if(w==null)continue;w.setInvisible(false);int side=i==0?-1:1;String key="Taker"+i;
                double near=w.distanceTo(friend);
                if(own.getInt(key)<APPROACH.length&&near>3.2){route(p,b,own,w,key,APPROACH,side*.6,TAKER_PACE);w.gesture(0);}
                else{var at=beside(b,own,friend,side);BorrascaCast.walk(w,at,.45);w.gesture(near<2.6?TAKING_HAND:0);if(near<1.9||w.position().subtract(at).horizontalDistance()<.45)reached=true;}
                if(own.getInt(key)>LANE_HEAD)turned=true;footsteps(p,w,clock);
            }
            if(own.getInt("Strokes")<2&&(turned||clock>=TAKEN_BY*2/3))stroke(p,b,own,2,w0,w1);
            if(own.getInt("Strokes")<3&&own.getBoolean("GentBehind")&&own.getInt("GentTicks")>0)own.putInt("GentTicks",GENT_TICKS);
            if(reached||clock>=TAKEN_BY){own.putBoolean("Led",true);own.putInt("LedAt",clock);own.putInt("ExitF",0);friend.gesture(LED);say(p,friend,p.getGameProfile().getName()+"!");stroke(p,b,own,3,w0,w1);}
            return;
        }
        var carrier=friend.getVehicle() instanceof LiteraryActor w?w:null;
        if(carrier==null){
            // 0.4.440: only a man who is there to be seen picks her up.
            for(var worker:men)if(worker!=null&&!worker.isInvisible()&&!worker.isVehicle()&&worker.distanceToSqr(friend)<6.25&&friend.startRiding(worker,true)){
                carrier=worker;BorrascaCast.stop(friend);break;
            }
        }
        friend.gesture(carrier==null?LED:CARRIED);
        route(p,b,own,carrier==null?friend:carrier,"ExitF",EXIT,0,LED_PACE);
        for(int i=0;i<2;i++){
            var w=men[i];if(w==null)continue;
            if(w==carrier)w.gesture(CARRY);
            else{int side=i==0?-1:1;BorrascaCast.walk(w,beside(b,own,friend,side),.75);w.gesture(side>0?HOLD_LEFT:HOLD_RIGHT);}
            footsteps(p,w,clock);
        }
        if(clock>=own.getInt("LedAt")+LED_FOR){
            own.putBoolean("FriendMissing",true);friend.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);BorrascaBelongings.dropOnce(p,own,"Compass",BorrascaRooms.at(b,BorrascaRooms.DROP));
            fit(p,b,BorrascaRooms.DROP.below(),BorrascaFittingBlock.Kind.TRAIL);for(int i=0;i<8;i++)fit(p,b,new BlockPos(1,BorrascaRooms.trailY(-152-i),-152-i),BorrascaFittingBlock.Kind.TRACKS);
            for(int i=0;i<2;i++)if(!own.contains("Exit"+i))own.putInt("Exit"+i,own.getInt("ExitF"));
        }
    }
    private static boolean accost(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor[] workers){
        var r0=BorrascaRooms.rel(p,b);double x=r0.x,z=r0.z;
        if(x<-.5||x>5.5||z>-152||z<-167)return false;
        LiteraryActor guard=null;
        for(var worker:workers)if(worker!=null&&!worker.isInvisible()&&worker.distanceToSqr(p)<64
                &&(guard==null||worker.distanceToSqr(p)<guard.distanceToSqr(p)))guard=worker;
        if(guard==null)return false;
        if(!own.getBoolean("TakingWarned")){
            own.putBoolean("TakingWarned",true);own.putInt("TakingWarnAt",own.getInt("TrailClock"));
            say(p,guard,"That's far enough. Go back down.");
            return false;
        }
        if(z>-154||Math.floorMod(own.getInt("TrailClock")-own.getInt("TakingWarnAt"),60000)<35
                ||guard.distanceToSqr(p)>25)return false;
        if(!LabyrinthDoors.sendBack(p,9,18,22,BorrascaStory::returned))return false;
        guard.gesture(WARNING);guard.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        say(p,guard,"I told you to go back.");
        p.hurt(p.damageSources().mobAttack(guard),2);
        // The attack ends this attempt. The abduction still leaves its evidence on the ridge.
        own.putBoolean("TakingReturned",true);
        own.putBoolean("FriendMissing",true);
        BorrascaBelongings.dropOnce(p,own,"Compass",BorrascaRooms.at(b,BorrascaRooms.DROP));
        own.putBoolean("Here",false);own.remove("DepartVisit");own.remove("ExitEligible");
        return true;
    }

    /**
     * After the taking: whoever is still in view walks on up the lane, along the ridge road and up the cutting to the truck, she between
     * them; once nobody is looking, they are gone. Nobody vanishes while watched.
     */
    private static void gone(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor a,String key,int side){
        if(a.role()==LiteraryActor.BORRASCA_KIMBER){a.appearance(LiteraryActor.BORRASCA_KIMBER,MISSING);a.setCustomNameVisible(false);a.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);}
        if(own.getBoolean(key)||a.isInvisible()&&!own.contains(key)||!LiteraryVignettes.watched(p.serverLevel(),a.getEyePosition())){
            // 0.4.440: whoever he was carrying is set down walking, not left in his arms with nobody there.
            for(var rider:a.getPassengers())if(rider instanceof LiteraryActor r)r.gesture(LED);
            a.stopRiding();a.ejectPassengers();a.setInvisible(true);own.putBoolean(key,true);BorrascaCast.stop(a);return;
        }
        if(side==0){
            if(a.getVehicle() instanceof LiteraryActor carrier){
                a.gesture(CARRIED);carrier.gesture(CARRY);route(p,b,own,carrier,"ExitF",EXIT,0,LED_PACE);
            }else{a.gesture(LED);route(p,b,own,a,"ExitF",EXIT,0,LED_PACE);}
            return;
        }
        if(a.isVehicle())return;
        String step="Exit"+(side<0?0:1);if(!own.contains(step))own.putInt(step,own.getInt("ExitF"));
        var friend=actor(p,own,"CastKimber");
        if(friend!=null&&!friend.isInvisible()&&a.distanceTo(friend)<4){BorrascaCast.walk(a,beside(b,own,friend,side),.75);a.gesture(side>0?HOLD_LEFT:HOLD_RIGHT);own.putInt(step,own.getInt("ExitF"));}
        else{a.gesture(0);route(p,b,own,a,step,EXIT,side*.6,TAKER_PACE);}
    }

    // ------------------------------------------------------------------ the third visit: the denial
    private static void visitThree(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor friend,@Nullable LiteraryActor sheriff){
        if(friend!=null){friend.stopRiding();friend.appearance(LiteraryActor.BORRASCA_KIMBER,MISSING);friend.setInvisible(true);BorrascaCast.stop(friend);}
        // The gouge is seen when it is looked at, from close enough to read the bark; no hand is needed.
        if(!own.getBoolean("GougeSeen")&&looksAt(p,BorrascaRooms.at(b,BorrascaRooms.CARVING),7))noticeGouge(p,own);
        if(sheriff!=null&&!retribution(p,own,sheriff)){sheriff.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(BorrascaRegistry.FLASHLIGHT.get()));blockade(p,b,own,sheriff);}
        if(near(p,BorrascaRooms.at(b,BorrascaRooms.MARKER_FOUR),9))perimeter(p,own);
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
        if(own.getInt("TrailClock")%200==0){heard(p,BorrascaRegistry.CHIME.get(),BorrascaRooms.at(b,BorrascaRooms.MACHINE),.8F);dust(p);}
        if(own.getBoolean("Escorting")){escort(p,b,own,sheriff);return;}
        int cooldown=Math.max(0,own.getInt("CaughtCooldown")-5);own.putInt("CaughtCooldown",cooldown);
        BorrascaCast.walk(sheriff,Vec3.atBottomCenterOf(BorrascaRooms.at(b,BorrascaRooms.SHERIFF)),.65);
        float yaw=flashlightYaw(own);sheriff.setYRot(yaw);sheriff.setYBodyRot(yaw);sheriff.setYHeadRot(yaw);sheriff.gesture(4);
        // Past the tape and in his light, or in plain sight and noisy near him, wherever the reader came from.
        double rz=BorrascaRooms.rel(p,b).z;boolean beyond=rz<TAPE_Z-.5&&rz> -167;
        boolean caught=beyond&&cooldown==0&&(flashlightSees(sheriff,p,yaw)||!p.isCrouching()&&!masking(own)&&sheriff.distanceToSqr(p)<14*14&&sees(sheriff,p.getEyePosition()));
        int suspicion=caught?own.getInt("Suspicion")+5:Math.max(0,own.getInt("Suspicion")-5);own.putInt("Suspicion",suspicion);
        // The beam holds on whoever it found until they are out of it.
        if(caught)own.putFloat("BeamYaw",toward(sheriff,p));
        if(caught&&suspicion==10)say(p,sheriff,"Hold it. I can see you.");
        if(suspicion>=40){own.putBoolean("Escorting",true);own.putInt("Suspicion",0);own.putInt("EscortStill",0);own.putInt("EscortTicks",0);own.remove("EscortX");sheriff.gesture(2);say(p,sheriff,"Back to the tape. Walk.");}
    }
    /** He walks behind the reader with the light on them, and enforces the boundary if they refuse to go. */
    private static void escort(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor sheriff){
        var end=Vec3.atBottomCenterOf(BorrascaRooms.at(b,2,BorrascaLand.ground(2,-119),-119));
        float yaw=toward(sheriff,p);own.putFloat("BeamYaw",yaw);sheriff.setYRot(yaw);sheriff.setYBodyRot(yaw);sheriff.setYHeadRot(yaw);sheriff.gesture(4);
        if(p.getZ()>=end.z-1){finishEscort(p,own,sheriff);return;}
        int elapsed=own.getInt("EscortTicks")+5;own.putInt("EscortTicks",elapsed);
        if(elapsed>=160){
            p.teleportTo(p.serverLevel(),end.x,end.y,end.z,0,0);
            p.setDeltaMovement(Vec3.ZERO);p.resetFallDistance();p.connection.resetPosition();
            finishEscort(p,own,sheriff);
            return;
        }
        var behind=p.position().add(0,0,-2.2);if(sheriff.distanceToSqr(p)>3.5*3.5)BorrascaCast.walk(sheriff,behind,.7);else BorrascaCast.stop(sheriff);
        double moved=own.contains("EscortX")?Math.hypot(p.getX()-own.getDouble("EscortX"),p.getZ()-own.getDouble("EscortZ")):1;own.putDouble("EscortX",p.getX());own.putDouble("EscortZ",p.getZ());
        int still=moved<.08?own.getInt("EscortStill")+5:0;own.putInt("EscortStill",still);if(still==40)say(p,sheriff,"Keep walking.");
    }
    private static void finishEscort(ServerPlayer p,CompoundTag own,LiteraryActor sheriff){
        own.putBoolean("Escorting",false);own.putInt("CaughtCooldown",80);
        BorrascaCast.stop(sheriff);say(p,sheriff,"Stay this side.");
    }
    static float toward(LiteraryActor from,ServerPlayer to){var d=to.position().subtract(from.position());return (float)Math.toDegrees(Math.atan2(-d.x,d.z));}
    public static float flashlightYaw(CompoundTag own){return own.getInt("Suspicion")>0||own.getBoolean("Escorting")?own.getFloat("BeamYaw"):180+55*(float)Math.sin(own.getInt("TrailClock")*.018);}
    public static boolean flashlightSees(LiteraryActor sheriff,ServerPlayer p,float yaw){Vec3 d=p.getEyePosition().subtract(sheriff.getEyePosition());if(d.lengthSqr()>22*22||d.lengthSqr()<.01)return false;Vec3 direction=new Vec3(-Math.sin(Math.toRadians(yaw)),0,Math.cos(Math.toRadians(yaw)));return direction.dot(d.normalize())>.965&&sees(sheriff,p.getEyePosition());}
    private static boolean sees(LiteraryActor a,Vec3 at){var hit=a.level().clip(new ClipContext(a.getEyePosition(),at,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a));return hit.getType()==HitResult.Type.MISS;}

    // ------------------------------------------------------------------ the fourth visit: the names
    private static void visitFour(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor friend,@Nullable LiteraryActor sheriff){
        if(friend!=null)grownKelly(p,b,own,friend);
        if(sheriff!=null&&!retribution(p,own,sheriff))patrolRefinery(p,b,own,sheriff);
        if(!own.getBoolean("PowerOff")&&own.getInt("TrailClock")%200==0)heard(p,BorrascaRegistry.CHIME.get(),BorrascaRooms.at(b,BorrascaRooms.MACHINE),1);
        if(!own.getBoolean("GougeSeen")&&looksAt(p,BorrascaRooms.at(b,BorrascaRooms.CARVING),7))noticeGouge(p,own);
        if(carriesEvidence(p,own))own.putBoolean("PatchRecovered",true);
        lowerRooms(p,b,own);
        if(own.getBoolean("PowerOff")&&own.getBoolean("EvidenceRead")&&own.getBoolean("PatchRecovered")&&carriesEvidence(p,own)&&(own.getInt("ConfirmedVisits")&7)==7&&BorrascaRooms.rel(p,b).z> -106){own.putBoolean("EscapedWithOriginal",true);LiteraryVignettes.ready(p,PLACE,own,"silenced_the_mountain");}
    }

    // ------------------------------------------------------------------ 0.4.427: retribution
    /**
     * The owner, November 9: "Ensure the player can enact violent retribution against the sheriff too, when they're an adult." On the two
     * grown visits a blow starts a real fight: he turns on the reader with his flashlight. Beaten (SHERIFF_DOWN_HEALTH), he goes down in the
     * dirt with his hands up and talks; a blow after that, once he is down, kills him. His body stays where it falls for the rest of that
     * visit and every visit after: he never vanishes (owner: "If he dies he shouldn't poof away"). If he dies on the third visit he is still
     * lying by the tape on the last night, nobody having gone for him, and the town, the tip line and the account know it. A child's blows
     * do nothing (sheriffFight is null before the third visit); a beaten Sheriff who is left alive is on his feet again next visit.
     */
    static @Nullable CompoundTag sheriffFight(ServerPlayer p,LiteraryActor sheriff){
        if(!sheriff.scene().equals(PLACE.id())||sheriff.owner().filter(p.getUUID()::equals).isEmpty()||!BorrascaSites.inside(p))return null;
        var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);
        return own.getInt("Visit")>=3&&own.getBoolean("Here")&&!own.getBoolean("SheriffDead")?own:null;
    }
    /** A blow that landed. Returns nothing; the Sheriff's state is in the reader's record. */
    static void sheriffStruck(ServerPlayer p,LiteraryActor sheriff,CompoundTag own){
        int hits=own.getInt("SheriffHits")+1;own.putInt("SheriffHits",hits);own.putBoolean("Escorting",false);own.putInt("Suspicion",0);
        BorrascaCast.stop(sheriff);long now=p.serverLevel().getGameTime();
        if(sheriff.getHealth()<=SHERIFF_DOWN_HEALTH){
            own.putBoolean("SheriffDown",true);own.putBoolean("SheriffBeaten",true);own.putLong("SheriffDownAt",now);
            var held=sheriff.getMainHandItem();if(!held.isEmpty()){sheriff.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);}
            sheriff.gesture(KNELT);say(p,sheriff,"Wait. Wait. You don't know what this town is.");
        }else{
            if(hits==1)say(p,sheriff,"That's the worst thing you've done in your life.");else if(hits==3)say(p,sheriff,"You're going in a hole up here.");
            own.putLong("SheriffStrikeAt",Math.min(own.getLong("SheriffStrikeAt"),now+10));
        }
        save(p,own);
    }
    /** A blow at the Sheriff once he is down. A moment after he goes down, it kills him. Returns whether it did. */
    static boolean sheriffFinished(ServerPlayer p,LiteraryActor sheriff,CompoundTag own){
        if(!own.getBoolean("SheriffDown")||p.serverLevel().getGameTime()-own.getLong("SheriffDownAt")<20)return false;
        own.putBoolean("SheriffDead",true);own.putInt("SheriffDiedVisit",own.getInt("Visit"));own.putLong("SheriffDiedAt",p.serverLevel().getGameTime());own.remove("SheriffDown");own.putLong("SheriffBody",sheriff.blockPosition().asLong());own.putFloat("SheriffBodyYaw",sheriff.getYRot());
        BorrascaCast.stop(sheriff);sheriff.gesture(FALLEN);sheriff.setCustomNameVisible(false);sheriff.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
        house(p,"He doesn't get up.");save(p,own);return true;
    }
    /** 0.4.444: the blood under the Sheriff's body, as his synced torn: spreading up to POOL_FULL, and POOL_DRIED on a later visit. */
    public static final int POOL_FULL=1000,POOL_DRIED=2000;
    /** 0.4.444 (owner: "Dialogue should change on second day, after Kelly/friend is taken"): what he says to a child who comes down alone. */
    static final String[] SHERIFF_AFTER={"You came down by yourself. Then you went up by yourself. Go on home.","Nobody's been on that road today. I'd have seen them.","Kids run off. I'll have a word with her folks. You stay off that trail."};
    private static final String[] PLEAS={"It was never my idea. There was a Sheriff before me, and he signed for it too.","You think it stops with me? There's a waiting list for this badge.","The Prescotts own the ground. I only ever kept the road clear.","Go on. Get it over with, or get out of my county."};
    private static void sheriffPleads(ServerPlayer p,LiteraryActor sheriff,CompoundTag own){int n=own.getInt("SheriffPleas");own.putInt("SheriffPleas",n+1);say(p,sheriff,PLEAS[n%PLEAS.length]);}
    /**
     * The Sheriff when there is a fight, a beating or a body: returns true when that is all he does this tick (his patrol and the blockade
     * wait). A new visit heals a beaten man; a dead one lies where he fell, that visit and every visit after.
     */
    private static boolean retribution(ServerPlayer p,CompoundTag own,LiteraryActor sheriff){
        int visit=own.getInt("Visit");
        if(own.getInt("SheriffVisit")!=visit){own.putInt("SheriffVisit",visit);if(!own.getBoolean("SheriffDead")){sheriff.setHealth(sheriff.getMaxHealth());own.remove("SheriffHits");own.remove("SheriffDown");own.remove("SheriffPleas");}}
        if(own.getBoolean("SheriffDead")){
            BorrascaCast.stop(sheriff);sheriff.setCustomNameVisible(false);
            sheriff.setInvisible(false);sheriff.gesture(FALLEN);sheriff.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
            // 0.4.444 (owner: "pool blood around corpse?"): it spreads under him for ten seconds after he falls (his synced torn, in
            // thousandths), and on a later visit it has dried (2000). client.BorrascaNativeArt draws it.
            sheriff.torn(own.getInt("SheriffDiedVisit")!=visit?POOL_DRIED:(int)Math.min(POOL_FULL,(p.serverLevel().getGameTime()-own.getLong("SheriffDiedAt"))*5));
            // Where he fell, facing the way he fell: a recall or a reload never moves the body.
            if(own.contains("SheriffBody")){var at=Vec3.atBottomCenterOf(BlockPos.of(own.getLong("SheriffBody")));float yaw=own.getFloat("SheriffBodyYaw");if(sheriff.position().distanceToSqr(at)>1)sheriff.moveTo(at,yaw,0);if(sheriff.getYRot()!=yaw){sheriff.setYRot(yaw);sheriff.setYBodyRot(yaw);sheriff.setYHeadRot(yaw);}}
            return true;
        }
        if(own.getBoolean("SheriffDown")){BorrascaCast.stop(sheriff);sheriff.gesture(KNELT);float yaw=toward(sheriff,p);sheriff.setYRot(yaw);sheriff.setYBodyRot(yaw);sheriff.setYHeadRot(yaw);return true;}
        if(own.getInt("SheriffHits")<=0)return false;
        // The fight: he comes at the reader and swings his flashlight; if the reader runs far enough he lets them go, for now.
        double d=sheriff.distanceToSqr(p);
        if(d>24*24){own.remove("SheriffHits");BorrascaCast.walk(sheriff,sheriff.position(),.5);return false;}
        if(!sheriff.getMainHandItem().is(BorrascaRegistry.FLASHLIGHT.get()))sheriff.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(BorrascaRegistry.FLASHLIGHT.get()));
        float yaw=toward(sheriff,p);sheriff.setYRot(yaw);sheriff.setYBodyRot(yaw);sheriff.setYHeadRot(yaw);
        if(d>2.4*2.4){sheriff.gesture(4);BorrascaCast.walk(sheriff,p.position(),.95);return true;}
        BorrascaCast.stop(sheriff);sheriff.gesture(2);
        long now=p.serverLevel().getGameTime();
        if(now>=own.getLong("SheriffStrikeAt")&&sees(sheriff,p.getEyePosition())){sheriff.swing(net.minecraft.world.InteractionHand.MAIN_HAND);p.hurt(p.damageSources().mobAttack(sheriff),3);own.putLong("SheriffStrikeAt",now+24);}
        return true;
    }

    private static void patrolRefinery(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor sheriff){
        if(!sheriff.getMainHandItem().is(BorrascaRegistry.FLASHLIGHT.get()))
            sheriff.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(BorrascaRegistry.FLASHLIGHT.get()));

        boolean powerOff=own.getBoolean("PowerOff");
        int stop=own.getInt("SheriffPatrolStop")%SHERIFF_PATROL.length;
        var target=Vec3.atBottomCenterOf(BorrascaRooms.at(b,powerOff?SHERIFF_SHUTDOWN_CHECK:SHERIFF_PATROL[stop]));
        if(sheriff.distanceToSqr(target)>.09){
            sheriff.gesture(0);
            BorrascaCast.walk(sheriff,target,.72);
            return;
        }

        BorrascaCast.stop(sheriff);
        sheriff.gesture(4);
        float facing=powerOff?180:switch(stop){case 0->0;case 1->180;case 2->-90;default->0;};
        float yaw=facing+22*(float)Math.sin(own.getInt("TrailClock")*.045);
        sheriff.setYRot(yaw);sheriff.setYBodyRot(yaw);sheriff.setYHeadRot(yaw);
        if(powerOff){
            if(!own.getBoolean("HeardSheriffShutdown")&&sheriff.distanceToSqr(p)<100&&sees(sheriff,p.getEyePosition()))
                sheriffShutdownLine(p,sheriff,own);
        }else{
            int wait=own.getInt("SheriffPatrolWait")+5;
            if(wait>=40){
                own.putInt("SheriffPatrolStop",(stop+1)%SHERIFF_PATROL.length);
                wait=0;
            }
            own.putInt("SheriffPatrolWait",wait);
        }
    }

    private static void sheriffShutdownLine(ServerPlayer p,LiteraryActor sheriff,CompoundTag own){
        own.putBoolean("HeardSheriffShutdown",true);
        say(p,sheriff,"Leave it off. I'll make the call.");
    }

    /** 0.4.237: what the reader finds under the depot and behind the shed on the last night. Nothing is shown happening; it is all after. */
    private static void lowerRooms(ServerPlayer p,BlockPos b,CompoundTag own){
        var r0=BorrascaRooms.rel(p,b);double x=r0.x,y=r0.y,z=r0.z;
        if(!own.getBoolean("LowerRooms")&&BorrascaRooms.inLowerRooms(x,y,z)){own.putBoolean("LowerRooms",true);cue(p,"A corridor under the depot, rooms along both sides. Every bolt is on this side of the door.");}
        if(!own.getBoolean("NellsRoom")&&BorrascaRooms.inRoom(BorrascaRooms.NELL_ROOM,x,y,z)){own.putBoolean("NellsRoom",true);cue(p,"Room 6. Scratched into the wall: "+FRIEND.toUpperCase(Locale.ROOT)+", and beside it your name, the way you both cut them into the tree. A woman is standing under them. She looks up.");
            var friend=actor(p,own,"CastKimber");if(friend!=null&&friend.phase()==GROWN){own.putBoolean("KellyFound",true);say(p,friend,GROWN_LINES[0]);own.putInt("KellyLine",1);}}
        // 0.4.439: room 7 is made up for 264-A, who is with the doctor first; room 2 is Dana's, on her cot until the men fetch her.
        if(!own.getBoolean("MarasRoom")&&BorrascaRooms.inRoom(BorrascaRooms.MARA_ROOM,x,y,z)){own.putBoolean("MarasRoom",true);cue(p,"Room 7. The cot is made up with a clean blanket. A coat from town hangs on the hook.");}
        if(BorrascaRooms.inRoom(BorrascaFeeding.DANA_ROOM,x,y,z)){
            // 0.4.444: she is already up on the belt when the night begins.
            if(!own.getBoolean("DanaRoomEmpty")){own.putBoolean("DanaRoomEmpty",true);cue(p,"Room 2. The blanket on the cot is folded back. It is still warm.");}
        }
        if(!own.getBoolean("GravesSeen")&&x>=-7.5&&x<7.5&&z>=-211&&z<-201&&y>11&&y<14)graves(p,own);
    }
    /**
     * 0.4.438 (owner: "Why not have Kelly etc aged up if the player sees them as adults?"): on the last night the friend is in room 6,
     * grown. The same saved actor as the child, on her grown skin and at full size, standing by the cot under the two names, turned to
     * the open door. She knows the reader, will not come up, and asks them to stop the machine and take her name up instead of her.
     */
    static final String[] GROWN_LINES={
        "Don't touch the other doors. They count them in the morning.",
        "You still have your name on the tree? I did ours on every wall they gave me.",
        "I'm not coming up. Two of mine are up there already. They have other people's names now.",
        "The machine on the ridge is the only thing up there that's theirs. Stop it.",
        "Take my name up with you. Not me. Go on."};
    private static void grownKelly(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend){
        friend.stopRiding();if(friend.phase()!=GROWN||friend.role()!=LiteraryActor.BORRASCA_KIMBER)friend.appearance(LiteraryActor.BORRASCA_KIMBER,GROWN);
        // 0.4.444 (owner: "Kelly's dialogue text appears for like, half a second"): her words were being hidden again on every tick here.
        friend.setInvisible(false);BorrascaCast.stop(friend);
        var at=BorrascaRooms.at(b,BorrascaRooms.KELLY);var spot=Vec3.atBottomCenterOf(at);
        if(friend.position().distanceToSqr(spot)>2.25&&p.serverLevel().hasChunkAt(at))friend.teleportTo(spot.x,spot.y,spot.z);
        // Turned to the door until the reader is in the room, then to the reader.
        float yaw=BorrascaRooms.inRoom(BorrascaRooms.NELL_ROOM,BorrascaRooms.rel(p,b).x,BorrascaRooms.rel(p,b).y,BorrascaRooms.rel(p,b).z)?(float)Math.toDegrees(Math.atan2(-(p.getX()-friend.getX()),p.getZ()-friend.getZ())):90;
        friend.setYRot(yaw);friend.setYHeadRot(yaw);friend.yBodyRot=yaw;
    }
    private static void grownTalk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        // 0.4.445 (owner: "she remembers which window you chose and asks if you ever finished it"): once she has warned the reader
        // off the other doors, the window and the comic; then her own lines go on.
        if(own.getInt("KellyLine")>=1&&BorrascaComic.room(p,a,own)){own.putBoolean("KellyFound",true);save(p,own);return;}
        int i=Math.min(own.getInt("KellyLine"),GROWN_LINES.length-1);say(p,a,GROWN_LINES[i]);own.putInt("KellyLine",i+1);
        own.putBoolean("KellyFound",true);if(i>=3)own.putBoolean("KellyAsked",true);save(p,own);
    }
    private static void graves(ServerPlayer p,CompoundTag own){if(own.getBoolean("GravesSeen"))return;own.putBoolean("GravesSeen",true);cue(p,"Behind the shed, low mounds in rows, a number on a stake at the head of each. No names.");}

    // ------------------------------------------------------------------ the place
    /** How far the store's front has got for a reader: papered over once the machine has been fed or stopped, whichever they did. */
    public static int frontStage(CompoundTag own,int visit){return BorrascaPapers.stage(visit,own.getBoolean("DanaFed")||own.getBoolean("PowerOff"));}
    /**
     * The store's front as the reader's own visits have left it (BorrascaPapers): Patrick's sheet from the second visit, Kendra's from
     * the third, both gone yellow on the last night, and, once the machine has been fed or stopped, the whole front papered over.
     * TownPostersVisit is the stage last hung.
     */
    private static void townPosters(ServerPlayer p,BlockPos b,CompoundTag own,int visit){
        int stage=frontStage(own,visit);
        if(stage<BorrascaPapers.PATRICK||own.getInt("TownPostersVisit")>=stage||!BorrascaPapers.loaded(p.serverLevel(),b))return;
        BorrascaPapers.hang(p.serverLevel(),b,stage);
        own.putInt("TownPostersVisit",stage);
    }

    /**
     * 0.4.431: the children's bikes. On the first visit Patrick's lies on its side outside the store where he dropped it ("If my bike's
     * still at the store, I'm up by the second marker"), and Kendra's stands on its kickstand by where she waits. By the second visit
     * Kendra has walked his home to his mother; hers is gone with her on the third.
     */
    private static void townBike(ServerPlayer p,BlockPos b,CompoundTag own,int visit){
        if(own.getInt("TownBikeVisit")>=visit)return;
        var his=BorrascaRooms.at(b,BorrascaRooms.PATRICK_BIKE);var hers=BorrascaRooms.at(b,BorrascaRooms.KENDRA_BIKE);
        var l=p.serverLevel();if(!l.hasChunkAt(his)||!l.hasChunkAt(hers))return;
        var none=net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        // 0.4.435: a bike ridden and left somewhere else on an earlier visit goes with that visit.
        for(int whose=0;whose<2;whose++){String key="BikeAt"+whose;if(!own.contains(key))continue;var left=BlockPos.of(own.getLong(key));if(!l.hasChunkAt(left))return;
            if(BorrascaShopBlock.is(l.getBlockState(left),BorrascaShopBlock.Kind.BIKE))l.setBlock(left,none,F);own.remove(key);}
        l.setBlock(his,visit==1?BorrascaShopBlock.of(BorrascaShopBlock.Kind.BIKE,Direction.EAST,1):none,F);
        l.setBlock(hers,visit<=2?BorrascaShopBlock.of(BorrascaShopBlock.Kind.BIKE,Direction.EAST,0):none,F);
        own.putInt("TownBikeVisit",visit);
    }

    // ------------------------------------------------------------------ 0.4.435: the bikes, ridden
    /**
     * A bike clicked in the town, on a childhood visit. The first click on each is a look at it. Lying down, a click stands it up.
     * Standing, a click gets on it (BorrascaBike), with the keys shown once in grey. A grown reader only looks.
     */
    static boolean bikeClick(ServerPlayer p,BlockPos b,CompoundTag own,BlockPos at,net.minecraft.world.level.block.state.BlockState s){
        var l=p.serverLevel();int whose=whoseBike(b,own,at);boolean lying=s.getValue(BorrascaShopBlock.STAGE)==1;
        String look=BorrascaShopTexts.bike(whose,lying);String seen="BikeLooked"+whose;
        if(own.getInt("Visit")>=3||p.isPassenger()){cue(p,look);return true;}
        if(lying){l.setBlock(at,s.setValue(BorrascaShopBlock.STAGE,0),F);cue(p,(own.getBoolean(seen)?"":look+" ")+"You pick it up and kick the stand down.");own.putBoolean(seen,true);return true;}
        if(!own.getBoolean(seen)){own.putBoolean(seen,true);cue(p,look);return true;}
        var bike=BorrascaRegistry.BIKE.get().create(l);if(bike==null)return true;
        float yaw=s.getValue(BorrascaShopBlock.FACING).getClockWise().toYRot();
        l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
        bike.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);bike.setUp(p,whose,yaw);l.addFreshEntity(bike);
        if(!p.startRiding(bike,true)){bike.discard();l.setBlock(at,s,F);return true;}
        own.remove("BikeAt"+whose);
        if(!own.getBoolean("BikeKeys")){own.putBoolean("BikeKeys",true);
            p.displayClientMessage(Component.empty().append(Component.keybind("key.left")).append(" and ").append(Component.keybind("key.right")).append(" push the pedals in turn  ·  ")
                .append(Component.keybind("key.back")).append(" brake  ·  ").append(Component.keybind("key.sneak")).append(" get off").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
        return true;
    }
    /** Whose bike stands here: one this reader rode and left here, else the one put out in town at this spot. */
    public static int whoseBike(BlockPos b,CompoundTag own,BlockPos at){
        for(int w=0;w<2;w++)if(own.contains("BikeAt"+w)&&BlockPos.of(own.getLong("BikeAt"+w)).equals(at))return w;
        return at.equals(BorrascaRooms.at(b,BorrascaRooms.PATRICK_BIKE))?BorrascaBike.PATRICK:BorrascaBike.KENDRA;
    }
    /** Every few ticks while a reader rides: that they rode it, and Patrick or Kendra saying something about it, once a visit. */
    static void bikeRidden(ServerPlayer p,BorrascaBike bike){
        if(bike.tickCount%5!=0)return;
        var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),PLACE);int whose=bike.whose(),visit=own.getInt("Visit");boolean changed=false;
        if(!own.getBoolean("BikeRode"+whose)){own.putBoolean("BikeRode"+whose,true);changed=true;}
        for(String key:new String[]{"TownWren","TownEli"}){
            var child=actor(p,own,key);if(child==null||child.isInvisible()||p.distanceToSqr(child)>100||!p.hasLineOfSight(child))continue;
            String said=key+"OnBike"+visit;if(own.getBoolean(said)||recentSpeech(p))continue;
            boolean patrick=child.phase()==ELI;
            say(p,child,patrick?(whose==BorrascaBike.PATRICK?"Don't put it in the ditch. And pedal both sides, not just the one.":"She lets me ride hers too. You have to keep pedaling or it tips.")
                :whose==BorrascaBike.KENDRA?(visit==2?"Bring it back here after. I'm waiting for Patrick.":"You can ride it. Just bring it back."):"That's Patrick's. He won't care.");
            own.putBoolean(said,true);changed=true;break;
        }
        if(changed)LiteraryVignettes.save(data,p.getUUID(),PLACE,own);
    }
    /** The reader came off: over it went, or straight into something. */
    static void bikeFell(ServerPlayer p,int whose,boolean crashed){
        var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),PLACE);own.putInt("BikeFalls",own.getInt("BikeFalls")+1);
        cue(p,crashed?"Straight into it. You go over the bars, and the bike goes down in the road.":"It goes over, and you go with it. The bike lies in the road.");
        var kendra=actor(p,own,"TownWren");String said="WrenBikeFall"+own.getInt("Visit");
        if(kendra!=null&&!kendra.isInvisible()&&p.distanceToSqr(kendra)<144&&!own.getBoolean(said)&&!recentSpeech(p)){own.putBoolean(said,true);say(p,kendra,whose==BorrascaBike.KENDRA?"You have to keep going or it falls over. Is it scratched?":"You have to keep going or it falls over.");}
        LiteraryVignettes.save(data,p.getUUID(),PLACE,own);
    }
    /** Where a bike was left, so a click there knows whose it is and the next visit clears it. With nowhere to stand it, it goes back to its own place in town. */
    static void bikeParked(ServerLevel l,@Nullable UUID reader,int whose,@Nullable BlockPos at){
        if(reader==null)return;var data=LabyrinthData.get(l.getServer());var own=LiteraryVignettes.personal(data,reader,PLACE);
        if(at==null){var b=BorrascaSites.base(l.getServer(),reader);if(b==null)return;var home=BorrascaRooms.at(b,whose==BorrascaBike.PATRICK?BorrascaRooms.PATRICK_BIKE:BorrascaRooms.KENDRA_BIKE);
            if(!l.getBlockState(home).isAir())return;l.setBlock(home,BorrascaShopBlock.of(BorrascaShopBlock.Kind.BIKE,Direction.EAST,0),F);at=home;}
        own.putLong("BikeAt"+whose,at.asLong());LiteraryVignettes.save(data,reader,PLACE,own);
    }

    private static void scene(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean("PowerOff")&&p.serverLevel().hasChunkAt(BorrascaRooms.at(b,BorrascaRooms.MACHINE))){var at=BorrascaRooms.at(b,BorrascaRooms.MACHINE);var s=p.serverLevel().getBlockState(at);if(s.is(BorrascaRegistry.MACHINE.get())&&s.getValue(BorrascaMachineBlock.RUNNING))p.serverLevel().setBlock(at,s.setValue(BorrascaMachineBlock.RUNNING,false),F);var lamp=BorrascaRooms.at(b,BorrascaRooms.LAMP);var light=p.serverLevel().getBlockState(lamp);if(light.is(BorrascaRegistry.FITTING.get())&&light.getValue(BorrascaFittingBlock.KIND)==BorrascaFittingBlock.Kind.LAMP)fit(p,b,BorrascaRooms.LAMP,BorrascaFittingBlock.Kind.LAMP_OFF);}
        int visit=own.getInt("Visit");if(!p.serverLevel().hasChunkAt(BorrascaRooms.at(b,BorrascaRooms.CARVING))||!p.serverLevel().hasChunkAt(BorrascaRooms.at(b,BorrascaRooms.PACK))||visit==4&&!p.serverLevel().hasChunkAt(BorrascaRooms.at(b,BorrascaRooms.GATE)))return;
        int stage=own.getBoolean("RestoredName")?4:visit>=3?3:own.getBoolean("PactCarved")?2:own.getBoolean("FriendCarved")?1:0;bark(p,b,stage);
        if(visit==4&&p.serverLevel().hasChunkAt(BorrascaRooms.at(b,BorrascaRooms.NELL_WALL)))BorrascaCarvingEntity.names(p.serverLevel(),BorrascaRooms.at(b,BorrascaRooms.NELL_WALL),FRIEND,p.getGameProfile().getName());
        if(own.getInt("SceneryVisit")==visit)return;
        if(visit>=3){for(int x=-5;x<=5;x++)for(int y=5;y<=11;y++)for(int z=-100;z<=-88;z++){var s=p.serverLevel().getBlockState(BorrascaRooms.at(b,x,y,z));if(s.is(Blocks.SPRUCE_PLANKS)||s.is(Blocks.BIRCH_PLANKS)||s.is(Blocks.OAK_PLANKS))fit(p,b,new BlockPos(x,y,z),BorrascaFittingBlock.Kind.AGED_WOOD);}for(int x=-1;x<=5;x++)fit(p,b,new BlockPos(x,5,-125),BorrascaFittingBlock.Kind.TAPE);}
        // Her pack is not at the sorting table until after she is taken.
        var pack=p.serverLevel().getBlockState(BorrascaRooms.at(b,BorrascaRooms.PACK));
        if(visit<=2&&pack.is(BorrascaRegistry.FITTING.get())&&pack.getValue(BorrascaFittingBlock.KIND)==BorrascaFittingBlock.Kind.PACK)p.serverLevel().setBlock(BorrascaRooms.at(b,BorrascaRooms.PACK),Blocks.AIR.defaultBlockState(),F);
        if(visit>=3&&pack.isAir())fit(p,b,BorrascaRooms.PACK,BorrascaFittingBlock.Kind.PACK);
        // 0.4.448: the two names beside the carving go as their owners do: his by the second visit, hers by the third. (A second carving
        // used to be set here on the second visit, with a gouge and the same three old names as the first.)
        if(visit>=2)p.serverLevel().setBlock(BorrascaRooms.at(b,BorrascaRooms.FRIENDS_NAMES),BorrascaNamesBlock.friends(visit),F);
        if(visit==2){var source=BorrascaRooms.at(b,BorrascaRooms.SOURCE);if(p.serverLevel().getBlockEntity(source) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity lectern){lectern.setBook(BorrascaTexts.contentNote());lectern.setChanged();p.serverLevel().sendBlockUpdated(source,lectern.getBlockState(),lectern.getBlockState(),F);}}
        if(visit==4){for(int x=-1;x<=1;x++)for(int y=12;y<=14;y++)p.serverLevel().setBlock(BorrascaRooms.at(b,x,y,-167),Blocks.AIR.defaultBlockState(),F);fit(p,b,new BlockPos(2,12,-168),BorrascaFittingBlock.Kind.LOCK);openDoor(p.serverLevel(),BorrascaRooms.at(b,BorrascaRooms.FACTORY_DOOR),true);}
        own.putInt("SceneryVisit",visit);
    }
    private static void bark(ServerPlayer p,BlockPos b,int stage){var at=BorrascaRooms.at(b,BorrascaRooms.CARVING);var s=p.serverLevel().getBlockState(at);if(s.is(BorrascaRegistry.CARVING.get())&&s.getValue(BorrascaCarvingBlock.STAGE)!=stage)p.serverLevel().setBlock(at,s.setValue(BorrascaCarvingBlock.STAGE,stage),F);BorrascaCarvingEntity.names(p.serverLevel(),at,p.getGameProfile().getName());}
    private static void fit(ServerPlayer p,BlockPos b,BlockPos rel,BorrascaFittingBlock.Kind kind){p.serverLevel().setBlock(BorrascaRooms.at(b,rel),BorrascaFittingBlock.of(kind,Direction.SOUTH),F);}
    private static boolean near(ServerPlayer p,BlockPos at,double distance){return at!=null&&p.distanceToSqr(Vec3.atBottomCenterOf(at))<distance*distance;}
    /** A person speaks: a caption with their name (client.SpeechCaptions), kept in the reader's journal. Never in chat. */
    static boolean recentSpeech(ServerPlayer p){
        int elapsed=p.tickCount-p.getPersistentData().getInt("BorrascaSpeechAt");
        return p.getPersistentData().contains("BorrascaSpeechAt")&&elapsed>=0&&elapsed<SPEECH_TICKS;
    }
    static void say(ServerPlayer p,LiteraryActor a,String words){a.say(speaker(a),words);a.getPersistentData().putInt("BorrascaSaid",a.tickCount);p.getPersistentData().putInt("BorrascaSpeechAt",p.tickCount);}
    static String speaker(LiteraryActor a){if(a.role()==LiteraryActor.BORRASCA_KYLE&&a.phase()==DANA)return "";if(BorrascaPeople.keeper(a))return BorrascaPeople.name(a);if(townChild(a.role(),a.phase()))return a.phase()==ELI?"Patrick":"Kendra";return a.role()==LiteraryActor.BORRASCA_KIMBER?FRIEND:a.role()==LiteraryActor.BORRASCA_SHERIFF?"Sheriff":a.role()==LiteraryActor.BORRASCA_PRESCOTT&&a.phase()<WORKER?"Prescott":"A man";}
    /** What the reader notices, on the action bar. */
    static void cue(ServerPlayer p,String text){p.displayClientMessage(Component.literal(text),true);}
    /** The House's own word, grey and italic. */
    private static void house(ServerPlayer p,String text){p.displayClientMessage(Component.literal(text).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void heard(ServerPlayer p,net.minecraft.sounds.SoundEvent sound,BlockPos at,float volume){LiteraryVignettes.heard(p,sound,at,volume,1);}
    private static void dust(ServerPlayer p){p.serverLevel().sendParticles(p,ParticleTypes.ASH,false,p.getX(),p.getY()+.4,p.getZ(),32,2,.5,2,.03);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    private static void send(ServerPlayer p,CompoundTag own){var view=view(p,own);if(view!=null)HousePackets.send(p,view);}
    /** What this reader's client is told: the machine, the visit, the light, the Sheriff's beam and, for a child, where he stands. */
    public static @Nullable BorrascaViewPayload view(ServerPlayer p,CompoundTag own){var b=BorrascaSites.base(p.server,p.getUUID());var sheriff=actor(p,own,"CastSheriff");if(b==null)return null;var standing=gentleman(own);return new BorrascaViewPayload(BorrascaRooms.at(b,BorrascaRooms.MACHINE),own.getInt("Visit"),own.getBoolean("Here"),!own.getBoolean("PowerOff")&&(own.getInt("Visit")>1||own.getBoolean("PactCarved")),lit(own)?own.getInt("TakingTicks"):0,own.getInt("TrailClock"),sheriff==null?BorrascaRooms.at(b,BorrascaRooms.SHERIFF):sheriff.blockPosition(),flashlightYaw(own),standing==null?Vec3.ZERO:standing,standing==null?0:own.getInt("GentTicks"),gentScale(own),own.getInt("GrabTicks"),own.getInt("Swallowed"));}
    /** Per-native-tick motion; native collision and ladder physics remain enabled. Spoken lines clear after a few seconds. */
    public static void actorTick(LiteraryActor a){
        if(!a.scene().equals(PLACE.id())||!(a.level() instanceof ServerLevel l))return;
        if(a.isCustomNameVisible()){int said=a.getPersistentData().getInt("BorrascaSaid");if(a.tickCount-said>SPEECH_TICKS||a.tickCount<said)a.setCustomNameVisible(false);}
        var p=a.owner().map(l.getServer().getPlayerList()::getPlayer).orElse(null);if(p==null||!BorrascaSites.inside(p)){BorrascaCast.stop(a);return;}var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);if(!own.getBoolean("Here")||!own.getBoolean("CastRecalled"))BorrascaCast.stop(a);
    }
    /** Capture intent; the actual door travel callback is the sole return authority. */
    public static void depart(ServerPlayer p){BorrascaCast.release(p.getUUID());BorrascaPeople.leave(p);var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);
        // 0.4.429: her knife never leaves the hill in a pocket; it went back to her.
        if(!BorrascaBelongings.remove(p,own,BorrascaBelongings.KNIFE).isEmpty()){own.putBoolean("KnifeBack",true);own.putBoolean("KnifeKept",true);}boolean valid=BorrascaSites.inside(p)&&own.getBoolean("Here")&&!own.getBoolean("Interrupted")&&p.isAlive();if(valid&&own.getInt("Beat")>=own.getInt("Visit"))own.putInt("DepartVisit",own.getInt("Visit"));else own.remove("DepartVisit");own.putBoolean("ExitEligible",valid&&own.getInt("Visit")==4&&(own.getInt("ConfirmedVisits")&7)==7&&own.getBoolean("Ready")&&own.getBoolean("PowerOff")&&own.getBoolean("EvidenceRead")&&carriesEvidence(p,own));own.putBoolean("Here",false);save(p,own);}
    public static void returned(ServerPlayer p){BorrascaPeople.leave(p);var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("BorrascaDeparted",true);own.putBoolean("Escorting",false);own.putInt("Suspicion",0);int returned=own.getInt("DepartVisit");if(returned>=1&&returned<=3){own.putInt("ConfirmedVisits",own.getInt("ConfirmedVisits")|(1<<(returned-1)));own.putInt("CastNextVisit",returned+1);}if(own.getBoolean("ExitEligible")&&carriesEvidence(p,own)){own.putBoolean("NameReturned",own.getBoolean("RestoredName"));own.putBoolean("Completed",true);own.putInt("Beat",4);save(p,own);WitnessAccount.resolve(p,WitnessAccount.Story.BORRASCA,"silenced_the_mountain");d.setCompleted(PLACE.id(),true);WitnessAccount.updateBook(p,false);}own.remove("ExitEligible");own.remove("DepartVisit");save(p,own);}
    /** 0.4.444 (owner: "Two dorseys"): a townsperson the reader's record no longer names does not come back into the world. */
    @SubscribeEvent public static void strayKeeper(EntityJoinLevelEvent event){
        if(!(event.getEntity() instanceof LiteraryActor a)||!(event.getLevel() instanceof ServerLevel l)||!a.scene().equals(PLACE.id())||a.owner().isEmpty())return;
        if(BorrascaPeople.stray(a,LiteraryVignettes.personal(LabyrinthData.get(l.getServer()),a.owner().get(),PLACE)))event.setCanceled(true);
    }
    @SubscribeEvent public static void joined(EntityJoinLevelEvent event){if(!(event.getEntity() instanceof LiteraryActor a)||!(event.getLevel() instanceof ServerLevel l)||!a.scene().equals(PLACE.id())||a.role()!=LiteraryActor.BORRASCA_KIMBER||a.owner().isEmpty())return;var own=LiteraryVignettes.personal(LabyrinthData.get(l.getServer()),a.owner().get(),PLACE);if(own.getInt("Visit")==4){a.appearance(LiteraryActor.BORRASCA_KIMBER,GROWN);a.setCustomNameVisible(false);a.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);a.setInvisible(false);return;}if(own.getInt("Visit")>=3||own.getBoolean("FriendMissing")){a.appearance(LiteraryActor.BORRASCA_KIMBER,MISSING);a.setCustomNameVisible(false);a.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);if(own.getInt("Visit")>=3||own.getBoolean("FriendHidden")){a.setInvisible(true);BorrascaCast.stop(a);}}}
    @SubscribeEvent public static void rejoined(PlayerEvent.PlayerLoggedInEvent event){if(!(event.getEntity() instanceof ServerPlayer p)||!BorrascaSites.inside(p))return;var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);if(own.getBoolean("Here")){own.putBoolean("CastRecalled",false);BorrascaArcadeFriends.arrived(p,own);save(p,own);}}
    @SubscribeEvent public static void died(LivingDeathEvent event){if(event.getEntity() instanceof ServerPlayer p&&BorrascaSites.inside(p)){BorrascaCast.release(p.getUUID());BorrascaPeople.leave(p);var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);own.putBoolean("Here",false);own.remove("ExitEligible");own.remove("DepartVisit");save(p,own);}}
}
