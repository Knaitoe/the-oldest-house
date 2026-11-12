package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.516, Borrasca (docs/BORRASCA_FACES_AND_PRESCOTT_0_4_516.md). The owner, 12 November: "Maybe let Prescott be encounterable
 * after discovering the orrery, too?"
 *
 * <p>The depot's papers have had him there all along: the night sheet has "10:10 J. Prescott", and every page of the night book is
 * "Counted" by a Prescott, father and son. He was nowhere on the last night. Now, once the reader has seen the machine, he comes
 * out to the door of his office on the yard, when nobody sees him come, and stands looking at the refinery. He knows who the
 * reader is. A word to him gets the next of what he has to say, which is what he tells himself: the rule in the front of the book,
 * the red water, that he only counts.
 *
 * <p>He is no fighter. A grown reader's blow puts him down against his own door, bargaining; a blow after that, once he is down,
 * kills him, and his body stays where it fell (as the Sheriff's does: BorrascaStory.retribution). Nothing waits on him, the
 * vignette's ending does not ask for him, and the account has only what the reader did.
 */
public final class BorrascaPrescott {
    private BorrascaPrescott(){}
    /** In the yard, one step outside the east door of the depot's front office. */
    public static final BlockPos DOORWAY=new BlockPos(-13,12,-179);
    /** How near the machine counts as having seen it, with no look needed; how far a look at it reaches; how near he speaks first. */
    public static final int MACHINE_NEAR=9,MACHINE_LOOK=20,GREETS=8;
    /** What one blow takes from him (he never dies of the blow that puts him down), and how long after going down a blow can finish him. */
    public static final float BLOW=3;public static final int FINISH_AFTER=20;
    /** What he says first, by what the night is: the machine stopped; the Sheriff dead; otherwise. */
    public static final String FIRST_STOPPED="It's stopped. All my life I've heard that, and I never once heard it stop.",
        FIRST_SHERIFF="You've killed a county officer. I'd go home, if I were you.",
        FIRST="You were with two sixty-three the night she came up. I had you down as gone home.";
    /** What he says to a word, in order; the last is said again. The fourth only to a reader who has found her room. */
    public static final String[] LINES={
        "None come down before eighteen. That's written in the front of the book. My father wrote it, and I've kept it.",
        "The water ran red two years and there wasn't a child born in Drisking. There's been one every year since.",
        "I don't go below. I count what comes up, and I sign for it. Somebody has to be able to say how many.",
        "Your friend had a boy and a girl. They're in town. They're well. I could tell you the street.",
        "Go home. You've seen it. Seeing it doesn't change what it's for."};
    public static final int CHILDREN=3;
    public static final String DOWN="All right. All right. There's a safe in the office. It's open.";
    public static final String[] PLEAS={
        "I can put you on nights tomorrow. Nobody on nights goes short.",
        "You want her up? I'll sign a transfer. I've signed them before.",
        "It isn't me you want. I only count. Look at who approves.",
        "Do it, then. There's a Prescott away at school. They'll have him home by Christmas."};

    /** Whether he is out at his door on the last night (until then he is nowhere to be seen). */
    public static boolean out(CompoundTag own){return own.getBoolean("PrescottOut");}

    private static boolean sees(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean("PowerOff"))return true;
        var machine=BorrascaRooms.at(b,BorrascaRooms.MACHINE);
        return p.distanceToSqr(Vec3.atCenterOf(machine))<MACHINE_NEAR*MACHINE_NEAR||BorrascaStory.looksAt(p,machine.above(),MACHINE_LOOK);
    }
    private static void face(LiteraryActor a,float yaw){a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}

    /** Every reader tick of the last night. */
    static void tick(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor a){
        if(a==null)return;
        if(!own.getBoolean("MachineSeen")){if(!sees(p,b,own))return;own.putBoolean("MachineSeen",true);}
        long now=p.serverLevel().getGameTime();
        if(own.getBoolean("PrescottDead")){
            // Where he fell, facing the way he fell, the blood spreading under him for ten seconds (as under the Sheriff).
            BorrascaCast.stop(a);a.setCustomNameVisible(false);a.setInvisible(false);a.gesture(BorrascaStory.FALLEN);
            a.torn((int)Math.min(BorrascaStory.POOL_FULL,(now-own.getLong("PrescottDiedAt"))*5));
            if(own.contains("PrescottBody")){var at=Vec3.atBottomCenterOf(BlockPos.of(own.getLong("PrescottBody")));float yaw=own.getFloat("PrescottBodyYaw");if(a.position().distanceToSqr(at)>1)a.moveTo(at,yaw,0);if(a.getYRot()!=yaw)face(a,yaw);}
            return;
        }
        if(!out(own)){
            // He comes to his door when nobody sees him come.
            if(!BorrascaCast.recall(p,a,BorrascaRooms.at(b,DOORWAY)))return;
            own.putBoolean("PrescottOut",true);a.setInvisible(false);a.setHealth(a.getMaxHealth());a.torn(0);
        }
        BorrascaCast.stop(a);
        if(own.getBoolean("PrescottDown")){a.gesture(BorrascaStory.KNELT);face(a,BorrascaStory.toward(a,p));return;}
        a.gesture(0);
        boolean near=p.distanceToSqr(a)<GREETS*GREETS&&p.hasLineOfSight(a);
        // He watches the refinery's doorway, east of him, until somebody is near enough to be looked at.
        face(a,near?BorrascaStory.toward(a,p):-90);
        if(near&&!own.getBoolean("PrescottMet")&&!BorrascaStory.recentSpeech(p)){
            own.putBoolean("PrescottMet",true);
            BorrascaStory.say(p,a,own.getBoolean("PowerOff")?FIRST_STOPPED:own.getBoolean("SheriffDead")?FIRST_SHERIFF:FIRST);
        }
    }

    /** A word to him on the last night. */
    static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!out(own)||own.getBoolean("PrescottDead"))return;
        if(own.getBoolean("PrescottDown")){int n=own.getInt("PrescottPleas");own.putInt("PrescottPleas",n+1);BorrascaStory.say(p,a,PLEAS[n%PLEAS.length]);return;}
        own.putBoolean("PrescottMet",true);
        int n=Math.min(own.getInt("PrescottSaid"),LINES.length-1);
        // What became of her children is for a reader who knows she was below.
        if(n==CHILDREN&&!own.getBoolean("NellsRoom"))n++;
        if(n==CHILDREN)own.putBoolean("PrescottChildren",true);
        own.putInt("PrescottSaid",Math.min(n+1,LINES.length-1));own.putBoolean("PrescottHeard",true);
        BorrascaStory.say(p,a,LINES[n]);
    }

    /** The reader's record, when a blow of theirs can land on him: the last night, with him out at his door and alive. */
    public static @Nullable CompoundTag night(ServerPlayer p,LiteraryActor a){
        if(!a.scene().equals(BorrascaStory.PLACE.id())||a.owner().filter(p.getUUID()::equals).isEmpty()||!BorrascaSites.inside(p))return null;
        var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),BorrascaStory.PLACE);
        return own.getInt("Visit")>=4&&own.getBoolean("Here")&&out(own)&&!own.getBoolean("PrescottDead")?own:null;
    }
    /** A blow that landed on him standing: he goes down where he is. */
    static void struck(ServerPlayer p,LiteraryActor a,CompoundTag own){
        own.putBoolean("PrescottDown",true);own.putBoolean("PrescottBeaten",true);own.putBoolean("PrescottMet",true);own.putLong("PrescottDownAt",p.serverLevel().getGameTime());
        BorrascaCast.stop(a);a.gesture(BorrascaStory.KNELT);BorrascaStory.say(p,a,DOWN);BorrascaStory.save(p,own);
    }
    /** A blow at him once he is down. A moment after he goes down, it kills him. Returns whether it did. */
    static boolean finished(ServerPlayer p,LiteraryActor a,CompoundTag own){
        long now=p.serverLevel().getGameTime();
        if(!own.getBoolean("PrescottDown")||now-own.getLong("PrescottDownAt")<FINISH_AFTER)return false;
        own.putBoolean("PrescottDead",true);own.putLong("PrescottDiedAt",now);own.remove("PrescottDown");own.putLong("PrescottBody",a.blockPosition().asLong());own.putFloat("PrescottBodyYaw",a.getYRot());
        BorrascaCast.stop(a);a.gesture(BorrascaStory.FALLEN);a.setCustomNameVisible(false);
        BorrascaStory.house(p,"He doesn't get up.");BorrascaStory.save(p,own);return true;
    }

    /** For the account: only what the reader did, and what he told them. */
    static String told(CompoundTag own){
        if(!own.getBoolean("PrescottMet"))return "";
        return " Prescott was at the door of his office on the yard, watching the refinery."
            +(own.getBoolean("PrescottChildren")?" He told me her children were in town, and that he could tell me the street.":"")
            +(own.getBoolean("PrescottDead")?" I killed him there. He was still offering me things.":own.getBoolean("PrescottBeaten")?" I put him down against his own door and left him there, offering me things.":"");
    }
}
