package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Movie Night's audience (0.4.102). Ray runs the projector from the end of the dock; Dot sells tickets in the hut on the far
 * bank; the Pruitts (Frank, June and their boy Jamie) watch from lawn chairs on the sand; Bobby and Lynn sit in a canoe
 * moored by the screen. They talk when spoken to and answer the film as it plays.
 * <p>One shared clock counts while anyone is here. As the night goes on the audience goes missing, one at a time: the boy,
 * then the two in the canoe, then Frank, then June. Nobody is taken in the light of a burst, and nobody while any living
 * reader or spectator can see them; they are simply not there the next time someone looks. Dot, across the water, stays.
 * When the scene has been empty a while and no camera is near, everyone is back in their places for the next reader.
 */
public final class MovieNightCast {
    static final LabyrinthPlace PLACE=LabyrinthPlace.MOVIE_NIGHT;
    /** The moored canoe the two teenagers sit in. Nobody else gets in. */
    public static final String TEEN_CANOE="LiteraryMovieTeens";
    public static final int JAMIE_PHASE=18,EMPTY_RESET=100;
    /** Missing, as bits: the boy, the two in the canoe, Frank, June. */
    public static final int JAMIE_BIT=1,TEENS_BIT=2,FRANK_BIT=4,JUNE_BIT=8;
    /** Occupied ticks before each is due to go, once it is dark and nobody can see them. */
    public static final int JAMIE_GOES=600,TEENS_GO=1100,FRANK_GOES=1600,JUNE_GOES=2200;
    private MovieNightCast(){}

    /** One of the audience: a stranger phase (their skin), where they sit or stand, which way they face, and their missing bit. */
    public record Person(String key,String name,int phase,double x,double z,float yaw,int bit,int due){
        public Vec3 home(BlockPos b){return new Vec3(b.getX()+x,b.getY(),b.getZ()+z);}
    }
    public static final Person RAY=new Person("Ray","Ray",14,1.5,-16.5,180,0,0),DOT=new Person("Dot","Dot",15,.5,-86.5,0,0,0),
        FRANK=new Person("Frank","Frank",16,-6.5,-5.5,180,FRANK_BIT,FRANK_GOES),JUNE=new Person("June","June",17,-4.5,-5.5,180,JUNE_BIT,JUNE_GOES),
        JAMIE=new Person("Jamie","Jamie",18,-5.5,-5.5,180,JAMIE_BIT,JAMIE_GOES),
        BOBBY=new Person("Bobby","Bobby",19,12.5,-33.5,180,TEENS_BIT,TEENS_GO),LYNN=new Person("Lynn","Lynn",20,12.5,-33.5,180,TEENS_BIT,TEENS_GO);
    public static final List<Person> CAST=List.of(RAY,DOT,FRANK,JUNE,JAMIE,BOBBY,LYNN);
    /** Where the teenagers' canoe is moored, beside the screen and out of the beam. */
    public static final Vec3 MOORING=new Vec3(12.5,-.4,-33.5);
    /** The three lawn chairs, Frank's first; the blanket in front, the cooler and the boy's popcorn. */
    public static final BlockPos FRANK_CHAIR=new BlockPos(-7,0,-6),JAMIE_CHAIR=new BlockPos(-6,0,-6),JUNE_CHAIR=new BlockPos(-5,0,-6),POPCORN=new BlockPos(-6,0,-7);

    public static boolean teen(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&(a.phase()==BOBBY.phase()||a.phase()==LYNN.phase());}
    public static @Nullable Person person(LiteraryActor a){if(a.role()!=LiteraryActor.STRANGER)return null;for(var p:CAST)if(p.phase()==a.phase())return p;return null;}
    private static @Nullable LiteraryActor cast(ServerLevel l,CompoundTag w,Person who){return w.hasUUID("Cast"+who.key())&&l.getEntity(w.getUUID("Cast"+who.key())) instanceof LiteraryActor a?a:null;}
    public static @Nullable LiteraryActor cast(ServerLevel l,Person who){return cast(l,LiteraryVignettes.shared(LabyrinthData.get(l.getServer()),PLACE),who);}
    public static @Nullable LakeCanoeEntity teenCanoe(ServerLevel l,CompoundTag w){return w.hasUUID("TeenCanoe")&&l.getEntity(w.getUUID("TeenCanoe")) instanceof LakeCanoeEntity c?c:null;}
    public static int gone(ServerLevel l){return LiteraryVignettes.shared(LabyrinthData.get(l.getServer()),PLACE).getInt("Gone0502");}
    private static void turn(LivingEntity a,float yaw){a.setYRot(yaw);a.setYHeadRot(yaw);a.yBodyRot=yaw;a.yRotO=yaw;}

    // ------------------------------------------------------------------------------------------------ their words
    private static final Map<String,String[]> IDLE=Map.of(
        "Ray",new String[]{"Canoes are free. Take one out past the sand; it plays better from the water.","Keep out of my beam unless you want to be in the picture.","This print's older than I am. It jumps in the boat scene.","Pier's across the lake. Dot keeps the hut open till the last reel.","Every summer somebody asks if there's sharks in the lake. There's no sharks in the lake."},
        "Dot",new String[]{"You walked round? Nobody walks round.","Ticket's free tonight, hon. Ray says the print's paid for itself.","I can hear it from here, you know. Every word. I've seen it forty times."},
        "Frank",new String[]{"Down in front, would you?","We saw this one at the drive-in in '74. Still gets me.","June, pass the popcorn.","That boat's too small. Everybody knows that boat's too small."},
        "June",new String[]{"Jamie, sit back in your chair.","It's only a movie, sweetheart.","Frank, did you lock the car?","Mind the cooler. There's pop in it if you want one."},
        "Jamie",new String[]{"Is it real?","I'm not going in the water tomorrow.","Mom. There's somebody standing on the lake.","Can I have the last of the popcorn?"},
        "Bobby",new String[]{"Find your own canoe.","Shh. This is the good part.","She's scared. Don't tell her I said."},
        "Lynn",new String[]{"Ignore him.","Want some gum?","Something bumped the paddle. Bobby. Something bumped the paddle."});
    /** What the family says once someone is missing: the boy, then the boy and Frank. */
    private static final String[] FRANK_AFTER={"Boy went to the restroom. Shouldn't take this long.","Jamie? He'll turn up. He always turns up.","I'm going to have a look along the water. Watch my chair."};
    private static final String[] JUNE_AFTER={"He went to the restroom. He'll be right back.","Have you seen a little boy? Red shirt?","Frank, go and look. Please."};
    private static final String[] JUNE_ALONE={"Frank went to look for Jamie. He took the flashlight.","They'll be back for the end. They never miss the end.","Will you sit with me? Just till they're back."};
    private static final String[] RAY_AFTER={"People wander off in the dark. They always come back for the end.","Somebody's canoe's come loose out there. Can't leave the projector.","Pruitts' chairs are empty. Wind, probably."};
    private static final String[] DOT_AFTER={"Did you see the Pruitts on your way across? Frank paid for three.","Those two kids never brought the canoe back.","You're the only one who's come round tonight."};
    /** Lines the audience says over the film, at moments in it: shot, tick in the shot, who, and the line. */
    private record Reaction(int shot,int at,Person who,String line,Person instead,String insteadLine){}
    private static final List<Reaction> REACTIONS=List.of(
        new Reaction(2,80,LYNN,"Bobby, don't. Not in the water.",RAY,"Night swim. Always a night swim."),
        new Reaction(3,40,FRANK,"Here it comes.",RAY,"Here it comes."),
        new Reaction(6,60,JUNE,"That man should close his beach.",RAY,"Close the beach, you idiot."),
        new Reaction(8,90,JAMIE,"Mom!",LYNN,"Bobby!"),
        new Reaction(11,140,JAMIE,"Mom? Is it gone?",JUNE,"Jamie? Jamie, it's over."),
        new Reaction(12,20,RAY,"Lacing it up again. Same picture all night.",RAY,"Lacing it up again."));

    private static void say(LiteraryActor a,String line){a.say(line);a.getPersistentData().putLong("Said0502",a.level().getGameTime());}
    /** A line said to the readers near enough to hear it. */
    private static void speak(LiteraryActor a,Person who,String line,List<ServerPlayer> near){say(a,line);for(var p:near)if(p.distanceToSqr(a)<24*24)p.displayClientMessage(Component.literal(who.name()+": “"+line+"”"),false);}

    // ------------------------------------------------------------------------------------------------ the shared clock
    /** Every tick from Movie Night's own clock, with the readers who are here. */
    public static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> readers){
        var d=LabyrinthData.get(l.getServer());var w=LiteraryVignettes.shared(d,PLACE);
        if(readers.isEmpty()){
            if(w.contains("CastNight")){w.putInt("CastEmpty",w.getInt("CastEmpty")+1);
                if(w.getInt("CastEmpty")>=EMPTY_RESET&&l.players().stream().noneMatch(p->p.getCamera().distanceToSqr(Vec3.atCenterOf(b.offset(0,0,-45)))<70*70))reset(w);
                LiteraryVignettes.shared(d,PLACE,w);}
            else if(w.getBoolean("CastSettle")){if(settle(l,b,w))w.remove("CastSettle");LiteraryVignettes.shared(d,PLACE,w);}
            return;
        }
        w.putInt("CastEmpty",0);int night=w.getInt("CastNight")+1;w.putInt("CastNight",night);
        settle(l,b,w);
        boolean dark=!MovieNight.lit(l,b);
        for(var who:List.of(JAMIE,BOBBY,FRANK,JUNE))if(dark&&night>=who.due()&&(w.getInt("Gone0502")&who.bit())==0)vanish(l,b,w,who,readers);
        react(l,w,readers);drift(l,b,w);
        if(night%240==120)idle(l,w,readers);
        // Lines fade from over their heads after a few seconds.
        if(night%10==0)for(var who:CAST){var a=cast(l,w,who);if(a!=null&&a.isCustomNameVisible()&&l.getGameTime()-a.getPersistentData().getLong("Said0502")>100)a.setCustomNameVisible(false);}
        LiteraryVignettes.shared(d,PLACE,w);
    }
    /** The night starts over for whoever comes next: everyone back in their places, the canoe moored, the chair stood up. */
    private static void reset(CompoundTag w){
        for(var k:List.of("CastNight","CastEmpty","Gone0502","CastLines"))w.remove(k);w.putInt("CastGen",w.getInt("CastGen")+1);w.putBoolean("CastSettle",true);
    }
    /** Puts each loaded piece back as the current night needs it; true once everything is in place. */
    private static boolean settle(ServerLevel l,BlockPos b,CompoundTag w){
        int gen=w.getInt("CastGen"),gone=w.getInt("Gone0502");boolean all=true;
        var canoe=teenCanoe(l,w);if(canoe==null&&w.hasUUID("TeenCanoe"))all=false;
        if(canoe!=null&&canoe.getPersistentData().getInt("Gen0502")!=gen&&(gone&TEENS_BIT)==0){var at=Vec3.atLowerCornerOf(b).add(MOORING);canoe.ejectPassengers();canoe.moveTo(at.x,at.y,at.z,180,0);canoe.getPersistentData().putInt("Gen0502",gen);}
        for(var who:CAST){var a=cast(l,w,who);if(a==null){all&=!w.hasUUID("Cast"+who.key());continue;}
            if(a.getPersistentData().getInt("Gen0502")==gen&&(!teen(a)||a.isPassenger()||(gone&TEENS_BIT)!=0))continue;
            if((gone&who.bit())!=0)continue;
            home(a,who,b,canoe);a.getPersistentData().putInt("Gen0502",gen);}
        if(l.hasChunkAt(b.offset(FRANK_CHAIR))&&l.hasChunkAt(b.offset(POPCORN))){
            if((gone&FRANK_BIT)==0&&MoviePropBlock.is(l.getBlockState(b.offset(FRANK_CHAIR)),MoviePropBlock.Kind.LAWN_CHAIR_TIPPED))l.setBlock(b.offset(FRANK_CHAIR),MoviePropBlock.of(MoviePropBlock.Kind.LAWN_CHAIR,Direction.NORTH),Block.UPDATE_CLIENTS);
            if((gone&JAMIE_BIT)==0&&MoviePropBlock.is(l.getBlockState(b.offset(POPCORN)),MoviePropBlock.Kind.POPCORN_SPILLED))l.setBlock(b.offset(POPCORN),MoviePropBlock.of(MoviePropBlock.Kind.POPCORN,Direction.NORTH),Block.UPDATE_CLIENTS);
        }else all=false;
        return all;
    }
    private static void home(LiteraryActor a,Person who,BlockPos b,@Nullable LakeCanoeEntity canoe){
        a.setInvisible(false);a.setNoGravity(false);a.setCustomNameVisible(false);
        if(teen(a)&&canoe!=null){if(a.getVehicle()!=canoe){a.stopRiding();a.moveTo(canoe.getX(),canoe.getY(),canoe.getZ(),who.yaw(),0);a.startRiding(canoe,true);}turn(a,canoe.getYRot());return;}
        if(a.isPassenger())a.stopRiding();var at=who.home(b);a.moveTo(at.x,at.y,at.z,who.yaw(),0);turn(a,who.yaw());a.setDeltaMovement(Vec3.ZERO);
    }
    /** Out of everyone's sight: under the far woods, unseen and still. */
    private static void hide(LiteraryActor a,BlockPos b){a.stopRiding();a.setInvisible(true);a.setNoGravity(true);a.setCustomNameVisible(false);a.moveTo(b.getX()+.5,b.getY()-4,b.getZ()-91.5,0,0);a.setDeltaMovement(Vec3.ZERO);}

    /** Whether anyone in the level (spectators too) could see this point now: in front of them, within the scene, nothing in the way. */
    public static boolean seen(ServerLevel l,Vec3 point){
        for(var p:l.players()){
            if(!p.isAlive()||p.isSleeping())continue;var eye=p.getEyePosition();var delta=point.subtract(eye);double dist=delta.length();
            if(dist>110)continue;if(dist>1.5&&p.getLookAngle().dot(delta.normalize())<.26)continue;
            var hit=l.clip(new ClipContext(eye,point,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));
            if(hit.getType()==HitResult.Type.MISS||hit.getBlockPos().distManhattan(BlockPos.containing(point))<=1)return true;
        }
        return false;
    }
    private static boolean seen(ServerLevel l,Entity e){return seen(l,e.getEyePosition())||seen(l,e.position().add(0,e.getBbHeight()*.4,0));}
    /** Takes them if it is dark and nobody can see them or where they were; otherwise tries again next tick. */
    private static void vanish(ServerLevel l,BlockPos b,CompoundTag w,Person who,List<ServerPlayer> readers){
        var people=who.bit()==TEENS_BIT?List.of(BOBBY,LYNN):List.of(who);var actors=new ArrayList<LiteraryActor>();
        for(var p:people){var a=cast(l,w,p);if(a==null)return;actors.add(a);}
        for(var a:actors)if(!a.isInvisible()&&seen(l,a))return;
        if(who.bit()==TEENS_BIT){var canoe=teenCanoe(l,w);if(canoe!=null&&seen(l,canoe.position().add(0,.4,0)))return;}
        for(var a:actors)hide(a,b);
        w.putInt("Gone0502",w.getInt("Gone0502")|who.bit());
        // A soft splash from the water's edge nearest where they were, then nothing.
        var at=who.bit()==TEENS_BIT?Vec3.atLowerCornerOf(b).add(MOORING):who.home(b).add(0,0,-7);
        l.playSound(null,at.x,b.getY(),at.z,SoundEvents.GENERIC_SPLASH,SoundSource.AMBIENT,.35F,.7F);
        if(who==JAMIE&&MoviePropBlock.is(l.getBlockState(b.offset(POPCORN)),MoviePropBlock.Kind.POPCORN))l.setBlock(b.offset(POPCORN),MoviePropBlock.of(MoviePropBlock.Kind.POPCORN_SPILLED,Direction.NORTH),Block.UPDATE_CLIENTS);
        if(who==FRANK&&MoviePropBlock.is(l.getBlockState(b.offset(FRANK_CHAIR)),MoviePropBlock.Kind.LAWN_CHAIR))l.setBlock(b.offset(FRANK_CHAIR),MoviePropBlock.of(MoviePropBlock.Kind.LAWN_CHAIR_TIPPED,Direction.NORTH),Block.UPDATE_CLIENTS);
        // Each reader who was here when it happened has someone missing from their night.
        var d=LabyrinthData.get(l.getServer());
        for(var p:readers){if(p.isSpectator())continue;var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putInt("Missing0502",own.getInt("Missing0502")|who.bit());LiteraryVignettes.save(d,p.getUUID(),PLACE,own);}
    }
    /** The teenagers' empty canoe turns slowly and drifts off down the lake, away from the screen. */
    private static void drift(ServerLevel l,BlockPos b,CompoundTag w){
        if((w.getInt("Gone0502")&TEENS_BIT)==0)return;var canoe=teenCanoe(l,w);if(canoe==null)return;
        double x=canoe.getX()-b.getX(),z=canoe.getZ()-b.getZ();if(x>=21.5||z>=-24)return;
        canoe.setPos(canoe.getX()+.008,canoe.getY(),canoe.getZ()+.012);canoe.setYRot(canoe.getYRot()+.15F);
    }
    private static void react(ServerLevel l,CompoundTag w,List<ServerPlayer> readers){
        int frame=MovieNight.frame(l.getGameTime()),gone=w.getInt("Gone0502");
        for(var r:REACTIONS){
            if(frame!=MovieNight.shotStart(r.shot())+r.at())continue;
            boolean first=(gone&r.who().bit())==0||r.who().bit()==0;var who=first?r.who():r.instead();
            if(!first&&(gone&who.bit())!=0&&who.bit()!=0)continue;var a=cast(l,w,who);if(a==null||a.isInvisible())continue;
            speak(a,who,first?r.line():r.insteadLine(),readers);
        }
    }
    private static void idle(ServerLevel l,CompoundTag w,List<ServerPlayer> readers){
        int n=w.getInt("CastLines");w.putInt("CastLines",n+1);var who=CAST.get(n%CAST.size());var a=cast(l,w,who);if(a==null||a.isInvisible())return;
        ServerPlayer near=null;double best=16*16;for(var p:readers){double e=p.distanceToSqr(a);if(e<best){best=e;near=p;}}if(near==null)return;
        say(a,line(who,w.getInt("Gone0502"),n/CAST.size()));
    }
    private static String line(Person who,int gone,int n){
        String[] pool=IDLE.get(who.key());
        if(who==RAY&&gone!=0)pool=RAY_AFTER;else if(who==DOT&&gone!=0)pool=DOT_AFTER;
        else if(who==FRANK&&(gone&JAMIE_BIT)!=0)pool=FRANK_AFTER;else if(who==JUNE&&(gone&FRANK_BIT)!=0)pool=JUNE_ALONE;else if(who==JUNE&&(gone&JAMIE_BIT)!=0)pool=JUNE_AFTER;
        return pool[Math.floorMod(n,pool.length)];
    }

    // ------------------------------------------------------------------------------------------------ each reader
    /** Every five ticks for each reader here: everyone and the teenagers' canoe exist, once, in their places. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();var d=LabyrinthData.get(p.server);var w=LiteraryVignettes.shared(d,PLACE);
        var canoe=teenCanoe(l,w);
        if(canoe==null&&!w.hasUUID("TeenCanoe")){
            canoe=DrownedTownRegistry.CAVE_CANOE.get().create(l);
            if(canoe!=null){var at=Vec3.atLowerCornerOf(b).add(MOORING);canoe.addTag(TEEN_CANOE);canoe.mobile(false);canoe.paint(5);canoe.moveTo(at.x,at.y,at.z,180,0);canoe.getPersistentData().putInt("Gen0502",w.getInt("CastGen"));
                if(l.addFreshEntity(canoe)){w.putUUID("TeenCanoe",canoe.getUUID());LiteraryVignettes.shared(d,PLACE,w);}else canoe=null;}
        }
        int gone=w.getInt("Gone0502");
        for(var who:CAST){
            var a=LiteraryVignettes.actor(p,PLACE,"Cast"+who.key(),LiteraryActor.STRANGER,BlockPos.containing(who.x(),0,who.z()),false);
            if(a==null||a.phase()==who.phase())continue;
            a.appearance(LiteraryActor.STRANGER,who.phase());a.getPersistentData().putInt("Gen0502",LiteraryVignettes.shared(d,PLACE).getInt("CastGen"));
            if((gone&who.bit())!=0)hide(a,b);else home(a,who,b,canoe);
        }
        // The ticket seller on the far bank greets each reader who comes round, once.
        var dot=cast(l,LiteraryVignettes.shared(d,PLACE),DOT);
        if(dot!=null&&!p.isSpectator()&&!own.getBoolean("MetDot0502")&&p.distanceToSqr(dot)<25){own.putBoolean("MetDot0502",true);speak(dot,DOT,gone!=0?DOT_AFTER[0]:IDLE.get("Dot")[0],List.of(p));}
    }
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var who=person(a);if(who==null)return;int gone=LiteraryVignettes.shared(LabyrinthData.get(p.server),PLACE).getInt("Gone0502");
        int n=own.getInt("Talk_"+who.key());own.putInt("Talk_"+who.key(),n+1);
        String line;
        if(who==DOT){own.putBoolean("MetDot0502",true);line=!own.getBoolean("Ready")?"You came round early, hon. It isn't over out there yet.":n%2==0?"There's your account, on the stand. Somebody wrote it all down while you were out there.":line(who,gone,n/2+1);}
        else line=line(who,gone,n);
        speak(a,who,line,List.of(p));
    }
    /** Right-clicking the teenagers' canoe. */
    public static void teenCanoe(ServerPlayer p,LakeCanoeEntity canoe){
        boolean empty=canoe.getPassengers().isEmpty();
        p.displayClientMessage(Component.literal(empty?"Their canoe is empty. A paddle is still across the seats, and the gum wrapper.":"Bobby and Lynn are in that one. It's tied off to the buoy."),true);
    }
    public static void clearAll(){}
}
