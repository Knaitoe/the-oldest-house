package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;

/**
 * 0.4.702, The list, part 1d (docs/THE_LIST_0_4_702.md): the town's people at their places by night. Amber at her desk under her window;
 * Jenna counting the drive-in's drawer behind the order window; Janice emptying the video store's return slot; Deek and Wes on the home
 * team's bench; Mel and Lesley apart on the top row of the stands; Shannon on the bus bench; Stace up on the water tower's catwalk; the
 * deputy outside the substation; and the reader's parents asleep. One of each in the town, shared by every reader in it; what each says
 * to a reader is that reader's own: a first line, then a second, then the second again. Nobody moves yet.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TheListPeople {
    private TheListPeople(){}
    public static final int FIRST=269,AMBER=0,JENNA=1,JANICE=2,DEEK=3,WES=4,MEL=5,LESLEY=6,SHANNON=7,STACE=8,DEPUTY=9,MOTHER=10,FATHER=11,CAST=12;
    private static final String[] SKINS={"list_amber","list_jenna","list_janice","list_deek","list_wes","list_mel","list_lesley","list_shannon","list_stace",
        "list_deputy","list_mother","list_father"};
    /**
     * Where each is: Amber in her desk chair; Jenna in the stand behind the first order window; Janice on the sidewalk at the return slot;
     * Deek and Wes on the home bench; Mel and Lesley on the top row of the home stands, seven apart; Shannon on the bus bench; Stace on the
     * catwalk's west side, at the rail; the deputy on the sidewalk by the substation's door; the parents with their heads on the two
     * pillows of their bed.
     */
    public static final BlockPos[] AT={new BlockPos(5,0,-81),new BlockPos(35,0,-134),new BlockPos(-5,0,-124),
        new BlockPos(TheListSite.FIELD_W-2,0,TheListSite.FIELD_Z-2),new BlockPos(TheListSite.FIELD_W-2,0,TheListSite.FIELD_Z-3),
        new BlockPos(TheListSite.FIELD_X-29,3,TheListSite.FIELD_Z-3),new BlockPos(TheListSite.FIELD_X-29,3,TheListSite.FIELD_Z+4),
        new BlockPos(-36,0,TheListSite.WALK_S_N),new BlockPos(TheListSite.TOWER_X-3,TheListSite.CATWALK_Y,TheListSite.TOWER_Z),
        new BlockPos(36,0,TheListSite.WALK_S_S),new BlockPos(-11,0,-15),new BlockPos(-11,0,-14)};
    /** Which way each faces: Amber her desk and window; Jenna and Janice their windows; the bench, the stands and the bus bench the way
     * their seats face; Stace out over the town; the deputy the street. The parents lie as their bed does. */
    private static final Direction[] FACING={Direction.SOUTH,Direction.SOUTH,Direction.NORTH,Direction.EAST,Direction.EAST,Direction.EAST,Direction.EAST,
        Direction.NORTH,Direction.WEST,Direction.NORTH,Direction.EAST,Direction.EAST};
    /**
     * 0.4.714: the stand's far corner from the fryer, by the back wall. Once one of the five has been forced into the fryer, Jenna backs
     * away into it from her window and gets down there, facing the fryer, until the hunt ends.
     */
    public static final BlockPos CORNER=new BlockPos(TheListSite.AVE_E+10,0,-140);
    /** Jenna, walking back from the window to the corner (the hunt's fryer). */
    static void backAway(LiteraryActor jenna,BlockPos b){
        jenna.setPose(Pose.STANDING);jenna.gesture(0);
        BorrascaCast.walk(jenna,Vec3.atBottomCenterOf(b.offset(CORNER)),1.2);
    }
    /** In the corner and still walking there no longer: down on her heels, facing the fryer. Each tick of a hunt that has used it. */
    static void cornered(ServerLevel l,BlockPos b){
        var jenna=find(l,b,JENNA);if(jenna==null||jenna.getPersistentData().contains("BorrascaSpeed"))return;
        var at=Vec3.atBottomCenterOf(b.offset(CORNER));
        if(jenna.position().distanceTo(at)>.5){backAway(jenna,b);return;}
        if(jenna.gesture()==TheListHunt.COWERING)return;
        BorrascaCast.stop(jenna);jenna.setNoGravity(true);
        var d=Vec3.atCenterOf(b.offset(TheListFryer.AT)).subtract(jenna.position());float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));
        jenna.moveTo(at.x,at.y,at.z,yaw,0);jenna.setYBodyRot(yaw);jenna.setYHeadRot(yaw);
        jenna.setPose(Pose.CROUCHING);jenna.gesture(TheListHunt.COWERING);
    }
    /** How far above its cell each one's feet are: Stace stands on the catwalk's grating, two pixels thick. */
    private static double lift(int who){return who==STACE?2/16.0:0;}
    /** How far above the pillow's cell a sleeper is held: the game's own height for one in bed. */
    public static final double SLEEPER_Y=11/16.0;

    public static boolean cast(int phase){return phase>=FIRST&&phase<FIRST+CAST;}
    /** 0.4.707: what they hold is drawn in their hands (Mel's camcorder, Amber's page); a hand is empty unless the night gives it something. */
    public static boolean carries(int phase){return cast(phase);}
    /** 0.4.707: Amber at her window with a page in her hand: the page is up, at the end of her arm, its face to the street. */
    public static boolean pageUp(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&a.phase()==FIRST+AMBER&&a.getMainHandItem().is(net.minecraft.world.item.Items.PAPER);}
    public static String skin(int phase){return SKINS[phase-FIRST];}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    /** The reader's parents, asleep in their bed. */
    public static boolean asleep(int phase){return phase==FIRST+MOTHER||phase==FIRST+FATHER;}
    public static int who(LiteraryActor a){return a.phase()-FIRST;}

    /**
     * 0.4.706: where one of them is now, and which way they face. While night one has the five out they are at the stock tank for the
     * party and the ride, at the gin's yard for the seed house, and nowhere after it (gone home); everyone else, and the five on any
     * other night, at their own places.
     */
    public record Spot(BlockPos at,Direction facing){}
    public static @Nullable Spot spot(int phase,int who){
        int i=-1;for(int k=0;k<TheListNight.FIVE.length;k++)if(TheListNight.FIVE[k]==who)i=k;
        if(i>=0&&phase!=TheListNight.NONE&&phase<TheListNight.HOME2){
            if(phase==TheListNight.PARTY||phase==TheListNight.RIDE)return new Spot(TheListNight.AT_TANK[i],TheListNight.FACE_TANK[i]);
            if(phase==TheListNight.SEED||phase==TheListNight.SHUT)return new Spot(TheListNight.AT_GIN[i],TheListNight.FACE_GIN[i]);
            return null;
        }
        // 0.4.707: from the cut until first light Amber is up at her window, over the street.
        if(who==AMBER&&phase==TheListNight.AFTER)return new Spot(TheListNight.AT_WINDOW,Direction.SOUTH);
        // 0.4.720: on night two she is in her den, in front of the television, until she sets off for the field.
        if(who==AMBER&&phase>=TheListNight.HOME2)return new Spot(TheListDen.DEN_AT,Direction.WEST);
        return new Spot(AT[who],FACING[who]);
    }
    /** One of the town's people near where they are now; a second of the same is let go. 0.4.708: one the hunt has sent running or hiding
     * is wherever the hunt has them. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){
        if(TheListHunt.holds(b,who))return TheListHunt.actor(l,b,who);
        // 0.4.720: Amber, out on night two's tests, is wherever she has got to.
        if(TheListDen.holds(l.getServer(),b,who)){for(var a:l.getEntitiesOfClass(LiteraryActor.class,TheListBody.bounds(b),x->is(x)&&x.phase()==FIRST+who&&x.isAlive()))return a;return null;}
        // 0.4.721: one dead on an earlier night is nowhere.
        if(TheListNight.gone(l.getServer(),b,who))return null;
        var s=spot(TheListNight.phase(l.getServer(),b),who);if(s==null)return null;
        LiteraryActor found=null;
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(s.at())).inflate(6),x->is(x)&&x.phase()==FIRST+who&&x.isAlive())){if(found==null)found=a;else a.discard();}
        return found;
    }
    /**
     * Everyone put where they are now, made if they are not there and their place is loaded (nothing is made while it is not); any of them
     * the night has sent home is let go. One look over the whole town finds them wherever they were, so nobody is made twice.
     */
    public static void ensure(ServerLevel l,BlockPos b){
        int phase=TheListNight.phase(l.getServer(),b);
        TheListHunt.leftover(l,b);
        var found=new LiteraryActor[CAST];
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,TheListBody.bounds(b),x->is(x)&&x.isAlive())){int w=who(a);if(found[w]==null)found[w]=a;else a.discard();}
        for(int who=0;who<CAST;who++){
            var s=spot(phase,who);var a=found[who];
            if(s!=null&&TheListNight.gone(l.getServer(),b,who)&&!TheListHunt.holds(b,who))s=null;
            if(s==null){if(a!=null)a.discard();continue;}
            var at=b.offset(s.at());
            if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))continue;
            if(a==null){
                a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
                a.bind(TheList.PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+who);
                place(a,at,who,s.facing());
                if(!l.addFreshEntity(a))continue;
            }
            // 0.4.708: one the hunt has sent running or hiding is left where they are. 0.4.714: Jenna, once the fryer has been used, is
            // left to her corner.
            if(who==JENNA&&TheListHunt.fryerUsed(b))cornered(l,b);
            else if(!TheListHunt.holds(b,who)&&!TheListDen.holds(l.getServer(),b,who))place(a,at,who,s.facing());
            // Mel has the camera out from the party until the seed house, and through a hunt until she lets it fall (0.4.709); Amber, at her window after the cut, a page out of a notebook
            // (0.4.707); nobody else holds anything.
            var want=who==MEL&&(phase>=TheListNight.PARTY&&phase<=TheListNight.SHUT||TheListHunt.on(b)&&!TheListHunt.dropped(b,MEL))?LiteraryRegistry.CAMCORDER.get()
                :who==AMBER&&phase==TheListNight.AFTER?net.minecraft.world.item.Items.PAPER:null;
            var held=a.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
            if(want!=null&&!held.is(want))a.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(want));
            else if(want==null&&!held.isEmpty())a.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);
        }
    }
    /** At the cell, turned the way given, held there: they do not fall from the stands or the catwalk. The parents are laid in bed. */
    static void place(LiteraryActor a,BlockPos at,int who,Direction facing){
        a.setNoGravity(true);a.setDeltaMovement(Vec3.ZERO);
        if(who==MOTHER||who==FATHER){lie(a,at);return;}
        if(a.getPose()==Pose.SLEEPING){a.stopSleeping();a.setPose(Pose.STANDING);}
        // 0.4.714: got up from wherever the hunt had them down.
        if(a.getPersistentData().contains("BorrascaSpeed"))BorrascaCast.stop(a);
        if(a.getPose()==Pose.CROUCHING)a.setPose(Pose.STANDING);
        float yaw=facing.toYRot();
        a.moveTo(at.getX()+.5,at.getY()+lift(who),at.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.gesture(0);
    }
    /** On their back, the head on the pillow; the renderer lays the body along the bed (LiteraryRenderers.lieOnTheList). Asleep at the
     * pillow's cell, eleven pixels up, where the game itself keeps a sleeper, so a hand finds them where they lie. */
    static void lie(LiteraryActor a,BlockPos head){
        float yaw=FACING[who(a)].getOpposite().toYRot();
        a.moveTo(head.getX()+.5,head.getY()+SLEEPER_Y,head.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
        a.setSleepingPos(head);a.setPose(Pose.SLEEPING);a.gesture(0);
    }
    /** Their bed is a bed of the town's, not the game's; the game's rule that gets up a sleeper whose bed has gone is not theirs. */
    @SubscribeEvent public static void keepLying(CanContinueSleepingEvent e){
        if(e.getEntity() instanceof LiteraryActor a&&is(a)&&asleep(a.phase()))e.setContinueSleeping(true);
    }

    /**
     * A word with one of them, the reader's own: the first line, then the second, then the second again; the reader's parents say nothing,
     * and a hand on them is a cue to that reader alone.
     */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!is(a))return;
        // 0.4.709: during the hunt a hand on one of the five is the hunt's (a catch, a twist away, a body that does not move).
        if(TheListHunt.hand(p,a))return;
        // 0.4.706: at the party, the five have the night's own lines, and Deek takes the one he picked.
        if(TheListNight.talk(p,a,own))return;
        int who=who(a);
        if(who==MOTHER){TheList.cue(p,TheListTexts.MOTHER);return;}
        if(who==FATHER){TheList.cue(p,TheListTexts.FATHER);return;}
        int heard=own.getInt("Heard"+who);own.putInt("Heard"+who,heard+1);
        String line=TheListTexts.said(who,heard);
        a.sayTo(p,TheListTexts.NAMES[who],line);a.speaks(line);
    }
    /** A knuckle on a window one of them is behind (Amber's, the drive-in's order window): the cue, and then they answer through the
     * glass. False when they are not there to answer. */
    static boolean knock(ServerPlayer p,BlockPos b,int who,String cue,CompoundTag own){
        var a=find(p.serverLevel(),b,who);
        if(a==null||p.distanceToSqr(a)>36)return false;
        TheList.cue(p,cue);talk(p,a,own);
        return true;
    }
}
