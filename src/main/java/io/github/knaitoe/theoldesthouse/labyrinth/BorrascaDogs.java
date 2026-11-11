package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 0.4.462, Borrasca (docs/BORRASCA_LIFE_PROPOSAL.md part 5, docs/BORRASCA_BISCUIT_0_4_462.md). The owner, 10 November: "I'd also
 * add more active play - hide and seek, tag, maybe the kids find an animal in the woods." The store's noticeboard has had "Lost:
 * brown dog, answers to Biscuit." since it was first written.
 *
 * <p>On the first visit Biscuit lies under a tree a few steps east of the first marker, a chewed end of rope trailing from his
 * collar. When Kendra and Patrick reach the marker with the reader, Kendra hears him and Patrick knows whose he is. A first hand
 * on him looks; a second takes up the rope, and he follows on it (the game's own lead, drawn from his collar to the hand). A hand
 * on him again lets the rope go, and so does getting more than {@link #ROPE} blocks ahead of him, mounting a bike, or leaving:
 * he sits down where he is and can be taken up again. Nothing waits on him and nothing is lost by leaving him.
 *
 * <p>He is the Corleys' (the kennel behind their house has stood empty). Led to within a few steps of their gate he pulls the
 * rope free and goes up the path to the porch, and is home: he lies there for the rest of that visit and on the second, the
 * notice is gone from the store's board, and the reader's account has it. Mrs. Corley, on the church's lawn that day, sees him go
 * by on the rope, and has something else to say once he is home.
 *
 * <p>Each reader's dog is their own: he is named in that reader's record, and one the record does not name is not let back into
 * the world.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BorrascaDogs {
    private BorrascaDogs(){}
    /** Where he is found: under a tree east of the first marker, where it was authored. */
    public static final BlockPos FOUND=new BlockPos(12,1,-76);
    /** His own gate, and his place on the porch beside the door; in the world relative to the base (BorrascaDressing's fourth house). */
    public static final BlockPos GATE=new BlockPos(5,0,-51),PORCH=new BlockPos(14,0,-50);
    public static final int WAITING=0,LED=1,LOOSE=2,GOING_HOME=3,HOME=4;
    /** How far the rope reaches, how far behind he lets the reader get before he comes on, how near he stops, and how near the gate is home. */
    public static final double ROPE=9,FOLLOW=2.6,CLOSE=1.8,GATE_NEAR=4;
    /** How often he is heard while nobody has found him, how near the reader is to hear it, and how long he is given to reach the porch on his feet. */
    public static final int WHINE_EVERY=160,WHINE_NEAR=22,HOME_WALK=400;
    private static final String FRESH="BorrascaFresh",READER="BorrascaReader";
    /** How near his place counts as in it, squared. */
    private static final double ARRIVED=1.7;

    public static final String KENDRA_HEARS="Wait. Hear that? There's a dog in there.",PATRICK_KNOWS="That's Biscuit. He's the one on the board at the store. He won't come to me.";
    public static final String SEEN="A brown dog lies under the tree, thin, a chewed end of rope trailing from his collar. He watches your hand.";
    public static final String TOOK="You take up the end of the rope. He gets to his feet.",LET_GO="You let the rope go. He sits down where he is.",SLIPPED="The rope pulls out of your hand. He sits down where he is.";
    public static final String AT_GATE="At his own gate he pulls the rope out of your hand and goes up the path.",AT_HOME="Biscuit, on his own porch. His tail thumps the boards twice.";
    public static final String CORLEY_SEES="Is that Biscuit? Lord, he's thin. Take him on home for me, hon. The gate's on the latch.",CORLEY_HOME="You brought him home. I'd stopped putting his bowl out.";

    // ---------------------------------------------------------------------------------------------------------------- where he is
    private static Vec3 found(BlockPos b){return Vec3.atBottomCenterOf(BorrascaRooms.at(b,FOUND));}
    private static Vec3 porch(BlockPos b){return Vec3.atBottomCenterOf(b.offset(PORCH));}
    /** Whether he is in the town at all for this reader now: on the first visit, and on the second if he was brought home. */
    public static boolean here(CompoundTag own){int visit=own.getInt("Visit");return visit==1||visit==2&&own.getBoolean("BiscuitHome");}
    /** What he is doing: on the second visit he is at home, whatever he was doing when the reader last saw him. */
    public static int state(CompoundTag own){return own.getInt("Visit")==2?HOME:own.getInt("BiscuitState");}
    /** This reader's dog, or null while he is not loaded or not made. */
    public static @Nullable BorrascaDog dog(ServerLevel l,CompoundTag own){return own.hasUUID("Biscuit")&&l.getEntity(own.getUUID("Biscuit")) instanceof BorrascaDog d&&d.isAlive()?d:null;}
    private static boolean loaded(ServerLevel l,BlockPos at){return l.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4));}
    /**
     * He is made once, where he belongs. As with the townspeople (0.4.444), one who has walked out of the loaded world is still out
     * there: another is made only when everywhere about the place he was last seen is loaded and he is not in it.
     */
    private static @Nullable BorrascaDog ensure(ServerPlayer p,CompoundTag own,Vec3 at,float yaw){
        var l=p.serverLevel();var known=dog(l,own);if(known!=null)return known;
        var cell=BlockPos.containing(at);if(!loaded(l,cell))return null;
        if(own.hasUUID("Biscuit")){var last=own.contains("BiscuitAt")?BlockPos.of(own.getLong("BiscuitAt")):cell;for(int dx=-16;dx<=16;dx+=16)for(int dz=-16;dz<=16;dz+=16)if(!loaded(l,last.offset(dx,0,dz)))return null;}
        var d=BorrascaRegistry.DOG.get().create(l);if(d==null)return null;
        d.moveTo(at.x,at.y,at.z,yaw,0);d.setYHeadRot(yaw);d.setYBodyRot(yaw);d.setInSittingPose(true);
        d.getPersistentData().putUUID(READER,p.getUUID());d.getPersistentData().putBoolean(FRESH,true);
        if(!l.addFreshEntity(d))return null;
        own.putUUID("Biscuit",d.getUUID());own.putLong("BiscuitAt",d.blockPosition().asLong());return d;
    }
    /** He lies where he belongs. One who is somewhere else is put there when nobody can see him go or come. */
    private static void rest(ServerLevel l,BorrascaDog d,Vec3 at,float yaw){
        d.getNavigation().stop();if(d.isLeashed())d.dropLeash(true,false);d.setInSittingPose(true);
        if(d.position().distanceToSqr(at)<ARRIVED)return;
        if(LiteraryVignettes.watched(l,d.getEyePosition())||LiteraryVignettes.watched(l,at.add(0,.5,0))||!loaded(l,BlockPos.containing(at)))return;
        d.moveTo(at.x,at.y,at.z,yaw,0);d.setYHeadRot(yaw);d.setYBodyRot(yaw);
    }
    private static void sit(BorrascaDog d){d.getNavigation().stop();d.setInSittingPose(true);}
    private static void loose(ServerPlayer p,CompoundTag own,BorrascaDog d,@Nullable String line){
        own.putInt("BiscuitState",LOOSE);sit(d);if(line!=null)BorrascaStory.cue(p,line);
    }

    // ---------------------------------------------------------------------------------------------------------------- each reader tick
    static void tick(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();
        if(!here(own)){var gone=dog(l,own);if(gone!=null){gone.dropLeash(true,false);gone.discard();own.remove("Biscuit");own.remove("BiscuitAt");}return;}
        int state=state(own);boolean home=state==HOME||state==GOING_HOME;
        var d=ensure(p,own,home?porch(b):found(b),home?90:180);if(d==null)return;
        own.putLong("BiscuitAt",d.blockPosition().asLong());
        switch(state){
            case WAITING->{rest(l,d,found(b),180);
                // Nobody has found him yet: he is heard now and then by a reader near enough.
                if(!own.getBoolean("BiscuitSeen")&&p.distanceToSqr(d)<WHINE_NEAR*WHINE_NEAR&&own.getInt("TrailClock")%WHINE_EVERY==0)BorrascaStory.heard(p,BorrascaRegistry.DOG_WHINE.get(),d.blockPosition(),1F);}
            case LED->led(p,b,own,d);
            case LOOSE->sit(d);
            case GOING_HOME->{int t=own.getInt("BiscuitHomeTicks")+5;own.putInt("BiscuitHomeTicks",t);var at=porch(b);
                if(d.position().distanceToSqr(at)<ARRIVED||t>=HOME_WALK){own.putInt("BiscuitState",HOME);if(d.position().distanceToSqr(at)<ARRIVED){sit(d);d.setYRot(90);d.setYHeadRot(90);d.setYBodyRot(90);}}
                else if(d.getNavigation().isDone())homeward(b,d);}
            default->rest(l,d,porch(b),90);
        }
    }
    /**
     * His way home on his own feet: in at the gate to the path beyond it, then up the path to the porch. Each leg is short enough
     * for him to find, and is walked to its end: the game's own paths stop a block short of where they are going, which left him
     * standing outside his gate.
     */
    private static void homeward(BlockPos b,BorrascaDog d){
        boolean inside=d.getX()>=b.getX()+GATE.getX()+1.5;var at=inside?porch(b):Vec3.atBottomCenterOf(b.offset(GATE).east(2));
        d.setInSittingPose(false);var nav=d.getNavigation();nav.moveTo(nav.createPath(at.x,at.y,at.z,0),1.25);
    }
    private static boolean nearGate(Entity e,BlockPos b){return e.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(GATE)))<GATE_NEAR*GATE_NEAR;}
    private static void led(ServerPlayer p,BlockPos b,CompoundTag own,BorrascaDog d){
        if(d.getLeashHolder()!=p){loose(p,own,d,SLIPPED);return;}
        double dist=d.distanceTo(p);
        if(dist>ROPE||p.isPassenger()||p.isSpectator()||!p.isAlive()){d.dropLeash(true,false);loose(p,own,d,SLIPPED);return;}
        if(nearGate(p,b)||nearGate(d,b)){
            d.dropLeash(true,false);own.putInt("BiscuitState",GOING_HOME);own.putBoolean("BiscuitHome",true);own.putInt("BiscuitHomeTicks",0);
            homeward(b,d);
            BorrascaStory.heard(p,BorrascaRegistry.DOG_BARK.get(),d.blockPosition(),1F);BorrascaStory.cue(p,AT_GATE);return;
        }
        d.setInSittingPose(false);
        if(dist>FOLLOW)d.getNavigation().moveTo(p,1.2);else if(dist<CLOSE)d.getNavigation().stop();
        // Mrs. Corley, on the church's lawn that day, sees him go by.
        if(!own.getBoolean("BiscuitCorley")){var corley=BorrascaPeople.keeper(p,own,BorrascaPeople.MRS_CORLEY);
            if(corley!=null&&!corley.isInvisible()&&corley.distanceToSqr(d)<100){own.putBoolean("BiscuitCorley",true);own.putInt("SupperChatTicks",0);
                float yaw=(float)(Math.atan2(d.getZ()-corley.getZ(),d.getX()-corley.getX())*180/Math.PI)-90;corley.setYRot(yaw);corley.setYHeadRot(yaw);corley.setYBodyRot(yaw);
                BorrascaStory.say(p,corley,CORLEY_SEES);}}
    }
    /**
     * At the first marker, before the two say goodbye: Kendra hears him and Patrick knows whose he is. True while one of them
     * had something to say. Nothing is said once the reader has had the rope, or if he is not there to be heard.
     */
    public static boolean atMarker(ServerPlayer p,CompoundTag own,LiteraryActor eli,LiteraryActor wren){
        int said=own.getInt("BiscuitHeard");
        if(said>=2||own.getInt("Visit")!=1||own.getInt("BiscuitState")!=WAITING||own.getBoolean("BiscuitLed"))return false;
        var d=dog(p.serverLevel(),own);if(d==null)return false;
        own.putInt("BiscuitHeard",said+1);
        if(said==0){BorrascaStory.heard(p,BorrascaRegistry.DOG_WHINE.get(),d.blockPosition(),1F);BorrascaStory.say(p,wren,KENDRA_HEARS);}
        else BorrascaStory.say(p,eli,PATRICK_KNOWS);
        return true;
    }
    /**
     * The reader has gone out of the town, or died in it. A dog on the rope, or left sitting part of the way, is back under his
     * tree; one on his way up the path is on the porch. He is moved at once if he is loaded: nobody is there to see.
     */
    public static void left(ServerPlayer p,CompoundTag own){
        int state=own.getInt("BiscuitState");if(state==HOME||state==WAITING&&!own.hasUUID("Biscuit"))return;
        if(state==LED||state==LOOSE)own.putInt("BiscuitState",WAITING);else if(state==GOING_HOME)own.putInt("BiscuitState",HOME);
        var b=BorrascaSites.base(p.server,p.getUUID());var l=p.server.getLevel(HouseDimensions.OUTSIDE);if(b==null||l==null)return;
        var d=dog(l,own);if(d==null)return;
        boolean home=own.getInt("BiscuitState")==HOME;var at=home?porch(b):found(b);if(!loaded(l,BlockPos.containing(at)))return;
        d.dropLeash(true,false);d.getNavigation().stop();d.setInSittingPose(true);float yaw=home?90:180;d.moveTo(at.x,at.y,at.z,yaw,0);d.setYHeadRot(yaw);d.setYBodyRot(yaw);
    }

    // ---------------------------------------------------------------------------------------------------------------- hands
    /** A hand on him: the line shown. The first looks; after that it takes up the rope, or lets it go. */
    public static String hand(ServerPlayer p,BorrascaDog d,CompoundTag own){
        int state=state(own);
        if(state==GOING_HOME||state==HOME)return AT_HOME;
        if(state==LED){d.dropLeash(true,false);own.putInt("BiscuitState",LOOSE);sit(d);return LET_GO;}
        if(!own.getBoolean("BiscuitSeen")){own.putBoolean("BiscuitSeen",true);BorrascaStory.heard(p,BorrascaRegistry.DOG_WHINE.get(),d.blockPosition(),.8F);return SEEN;}
        d.setInSittingPose(false);d.setLeashedTo(p,true);own.putInt("BiscuitState",LED);own.putBoolean("BiscuitLed",true);return TOOK;
    }
    /** Whether this dog is this reader's own. */
    public static boolean owns(ServerPlayer p,BorrascaDog d){var t=d.getPersistentData();return t.hasUUID(READER)&&t.getUUID(READER).equals(p.getUUID());}
    /**
     * A hand on him never reaches the game's own handling of a wolf or of a lead: the event is his alone, on both sides, so that a
     * client does not see a lead come off that the server kept on.
     */
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void touched(PlayerInteractEvent.EntityInteract event){
        if(!(event.getTarget() instanceof BorrascaDog d))return;
        event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
        if(!(event.getEntity() instanceof ServerPlayer p)||event.getHand()!=InteractionHand.MAIN_HAND||p.isSpectator()||!owns(p,d)||!BorrascaSites.inside(p))return;
        var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),LabyrinthPlace.BORRASCA);
        BorrascaStory.cue(p,hand(p,d,own));LiteraryVignettes.save(data,p.getUUID(),LabyrinthPlace.BORRASCA,own);
    }
    /** A dog the reader's record does not name, or one come back into a town he is not in, is not let back into the world. */
    @SubscribeEvent public static void joined(EntityJoinLevelEvent event){
        if(!(event.getEntity() instanceof BorrascaDog d)||!(event.getLevel() instanceof ServerLevel l))return;
        var t=d.getPersistentData();if(t.getBoolean(FRESH)){t.remove(FRESH);return;}
        if(!t.hasUUID(READER)){event.setCanceled(true);return;}
        var own=LiteraryVignettes.personal(LabyrinthData.get(l.getServer()),t.getUUID(READER),LabyrinthPlace.BORRASCA);
        if(!own.hasUUID("Biscuit")||!own.getUUID("Biscuit").equals(d.getUUID())||!here(own))event.setCanceled(true);
    }

    // ---------------------------------------------------------------------------------------------------------------- what is said after
    /** Whether the notice for him is gone from the store's board: he was brought home, and it is a later day. */
    public static boolean noticeDown(CompoundTag own){return own.getBoolean("BiscuitHome")&&own.getInt("Visit")>=2;}
    /** The account: the dog, if the reader found him, and whether they brought him home. */
    public static String told(CompoundTag own){
        if(!own.getBoolean("BiscuitSeen")&&!own.getBoolean("BiscuitLed"))return "";
        return "There was a dog in the trees by the first marker, a chewed rope trailing from his collar. It was Biscuit, off the notice at the store. "
            +(own.getBoolean("BiscuitHome")?"I led him down the street on it to his own gate. ":own.getBoolean("BiscuitLed")?"I led him part of the way home. ":"I left him where he was. ");
    }
}
