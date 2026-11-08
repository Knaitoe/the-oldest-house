package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;

/**
 * Two original, private, quiet strangers, mirror images on the west and east rails (0.4.159). They are the reader's age when the
 * waiting room read it: young adults, around forty, or older. Fixed meeting points never chase a retreating reader.
 * <p>
 * 0.4.172 (owner: "Fix the posture of the dancing npcs ... It needs a decisive way for the player to end it. The npcs approaching
 * the player ... isn't very reliable. I looked at the boy for a fraction of the time I did the girl and he was halfway across ...
 * Unless the intent is for the player to exit. If so, that should be presented more clearly."): once the song starts, a stranger
 * comes across only while the reader looks at them, and stops an arm's length away with a hand held out. Taking it (using them)
 * is a slow dance; standing between the two once both have come is a dance with both; going out to the dock lantern and using it
 * is being with no one. Any of them ends the song there and then, lights the lamp over the way on and opens its door.
 */
public final class HouseReadShelter {
    private HouseReadShelter(){}
    /** The strangers' gestures, drawn by the cast renderer: on the rail, standing, a hand held out, dancing, stepped back after. */
    public static final int LEAN=0,STAND=1,HAND=2,DANCE=3,AFTER=4;
    /** How long a dance lasts before the song is let go, and how long the reader stands between two held-out hands. */
    public static final int DANCE_TICKS=160,BOTH_TICKS=100;
    /** Blocks a tick while looked at; where they stop; how near a hand is held out; how far a hand can be taken from. */
    static final double STEP=.045,STOP=2.0,OFFER=2.6,REACH=3.5;
    public static boolean role(int role){return role==LiteraryActor.READ_MAN||role==LiteraryActor.READ_WOMAN;}
    /** The appearance phase: two per age (quiet, then the half-smile), young adults first. */
    public static int phase(int age,boolean smile){return age*2+(smile?1:0);}
    public static int age(ServerPlayer p){return switch(HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.AGE,false)){case "ANALOG"->2;case "TRANSITIONAL"->1;default->0;};}
    /** What the account and the hints call them: the boy and the girl, or the man and the woman for older readers. */
    static String name(boolean man,int age){return man?age>0?"the man":"the boy":age>0?"the woman":"the girl";}
    public static void ensure(ServerPlayer p,BlockPos b,CompoundTag t){
        if(!t.contains("CastAge"))t.putInt("CastAge",age(p));
        for(int i=0;i<2;i++){String key=i==0?"Man":"Woman";if(t.hasUUID(key))continue;double x=b.getX()+.5+(i==0?-HouseReadRemainingRooms.FIGURE_X:HouseReadRemainingRooms.FIGURE_X),z=b.getZ()+HouseReadRemainingRooms.FIGURE_Z;if(!p.serverLevel().areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(BlockPos.containing(x,b.getY(),z))))continue;var actor=LiteraryRegistry.ACTOR.get().create(p.serverLevel());if(actor==null)continue;
            // Each leans back on their own rail, facing across the floor.
            float yaw=i==0?-90:90;actor.appearance(i==0?LiteraryActor.READ_MAN:LiteraryActor.READ_WOMAN,phase(t.getInt("CastAge"),false));actor.bind(LabyrinthPlace.READ_SHELTER,p.getUUID());actor.setNoGravity(true);actor.moveTo(x,b.getY(),z,yaw,0);actor.setYHeadRot(yaw);actor.yBodyRot=yaw;actor.setCustomNameVisible(false);if(p.serverLevel().addFreshEntity(actor))t.putUUID(key,actor.getUUID());}
    }
    public static void tick(LiteraryActor a){
        if(!role(a.role())||a.owner().isEmpty())return;var p=a.level().getServer().getPlayerList().getPlayer(a.owner().get());if(p==null||HouseReadSites.inside(p)!=HouseReadSites.Room.SHELTER){a.setDeltaMovement(Vec3.ZERO);return;}
        var own=HouseReadSites.state(p.server,p.getUUID());var t=own.getCompound(HouseReadSites.Room.SHELTER.key());
        boolean man=a.role()==LiteraryActor.READ_MAN;String key=man?"ManNear":"WomanNear",dance=t.getString("Dance");int age=t.getInt("CastAge");
        if(dance.equals(man?"man":"woman")||dance.equals("both"))partner(a,p,man,dance.equals("both"),t.getBoolean("Settled"),t);
        else if(t.getBoolean("Settled")||!dance.isEmpty()){if(a.gesture()==HAND)a.gesture(STAND);if(p.distanceToSqr(a)<100)face(a,p,true);}
        else if(t.getBoolean("SongStarted")&&t.getInt("SongAge")<900){
            approach(p,a,t,man,age);
            // A step away does not undo standing beside them; walking off does.
            boolean near=p.distanceToSqr(a)<9;int linger=near?t.getInt(key)+1:Math.max(0,t.getInt(key)-3);t.putInt(key,linger);
            if(linger>=60&&near){a.appearance(a.role(),phase(age,true));if(!t.getBoolean(key+"Noted")){t.putBoolean(key+"Noted",true);t.putBoolean("NoteArmed",true);boolean older=age>0;HouseReadMoments.literal(t,man?(older?"stood beside the man by the rail":"stood beside the boy by the rail"):(older?"stood beside the woman with the bottle":"stood beside the girl with the bottle"));HouseRead.first(p,HouseReadSites.Room.SHELTER,"Stood",man?"man":"woman");}}
        }
        a.setDeltaMovement(Vec3.ZERO);own.put(HouseReadSites.Room.SHELTER.key(),t);HouseReadSites.save(p.server,p.getUUID(),own);
    }
    /** Looked at, they come across the floor; not looked at, they stay where they are and never follow. Near enough, a hand. */
    private static void approach(ServerPlayer p,LiteraryActor a,CompoundTag t,boolean man,int age){
        double distance=Math.sqrt(p.distanceToSqr(a));String key=man?"Man":"Woman";
        if(distance<=OFFER){
            face(a,p,true);a.gesture(HAND);
            if(!t.getBoolean(key+"Offered")){t.putBoolean(key+"Offered",true);String who=name(man,age);p.displayClientMessage(Component.literal(Character.toUpperCase(who.charAt(0))+who.substring(1)+" holds out a hand. Take it to dance.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
        }else if(watched(p,a)){
            var to=p.position().subtract(a.position()).multiply(1,0,1);var step=to.normalize().scale(Math.min(STEP,distance-STOP));var before=a.position();a.move(MoverType.SELF,step);a.walkAnimation.update((float)a.position().distanceTo(before)*8,.4f);
            a.gesture(STAND);face(a,p,false);t.putBoolean(key+"Came",true);
        }else if(!t.getBoolean(key+"Came")){
            // Never drawn off the rail: back to leaning on it, facing across the floor.
            if(a.gesture()!=LEAN){a.gesture(LEAN);float yaw=man?-90:90;a.setYRot(yaw);a.yBodyRot=yaw;a.setYHeadRot(yaw);}
        }else{if(a.gesture()==HAND)a.gesture(STAND);if(distance<10)face(a,p,false);}
    }
    /**
     * Dancing: an arm's length in front of the reader on the line between them (side by side with both), swaying in place.
     * After: one step back from where the reader stood when the song was let go, and there they stay. 0.4.199 (owner: "Make sure
     * npc stops following player after the dance."): the step back was aimed at the reader every tick, so they trailed after.
     */
    private static void partner(LiteraryActor a,ServerPlayer p,boolean man,boolean both,boolean after,CompoundTag t){
        String back=man?"ManBack":"WomanBack";Vec3 target;
        if(after&&t.contains(back+"X"))target=new Vec3(t.getDouble(back+"X"),a.getY(),t.getDouble(back+"Z"));
        else{
            var away=a.position().subtract(p.position()).multiply(1,0,1);if(away.lengthSqr()<1e-4)away=new Vec3(man?-1:1,0,0);away=away.normalize();
            var side=new Vec3(-away.z,0,away.x);double reach=after?1.6:.85;
            target=p.position().add(away.scale(reach)).add(both?side.scale(man?.6:-.6):Vec3.ZERO);
            if(after){t.putDouble(back+"X",target.x);t.putDouble(back+"Z",target.z);}
        }
        // 0.4.397: in the dance the partner has weight (DanceWeight): they keep their ground and close on the reader slowly, so the
        // reader is the one who swings about them; stepping back after, they go at an ordinary pace.
        double pace=after?.12:DanceWeight.SETTLE;
        var to=target.subtract(a.position()).multiply(1,0,1);
        if(after)SWIRL.remove(a.getUUID());
        else if(swirl(a,p,to)){face(a,p,true);a.gesture(DANCE);return;}
        if(to.length()>.02){var before=a.position();a.move(MoverType.SELF,to.length()>pace?to.normalize().scale(pace):to);a.walkAnimation.update((float)a.position().distanceTo(before)*4,.4f);}
        if(!after||p.distanceToSqr(a)<100)face(a,p,true);a.gesture(after?AFTER:DANCE);
    }
    /** Each dancing partner's swirl: ticks pulled, the turn about the reader carried (radians), the reader's last bearing from them, which way the reader has been swinging, and where the reader stood last tick. */
    private static final Map<UUID,double[]> SWIRL=new HashMap<>();
    /**
     * 0.4.399, the owner: "continuing to pull against the weight should swirl your partner". A reader who keeps leaning away has the
     * partner pulled behind arm's length tick after tick; held long enough (DanceWeight.PULL_TICKS), the pull swings the partner round
     * the reader, the way the reader was last swinging, faster the longer it is held, and carrying on a little after it is let go.
     * While swirling the partner keeps the distance the reader holds them at, so the lean is not given up. Returns whether the
     * partner was swirled this tick.
     */
    private static boolean swirl(LiteraryActor a,ServerPlayer p,Vec3 toTarget){
        var st=SWIRL.computeIfAbsent(a.getUUID(),k->new double[]{0,0,Double.NaN,0,Double.NaN,Double.NaN});
        var rel=a.position().subtract(p.position()).multiply(1,0,1);double r=rel.length();if(r<1e-3)return false;
        double bearing=Math.atan2(p.getZ()-a.getZ(),p.getX()-a.getX());
        if(!Double.isNaN(st[2])){double d=bearing-st[2];st[3]=st[3]*.95+Math.atan2(Math.sin(d),Math.cos(d));}st[2]=bearing;
        // Pulled: arm's length lies between the partner and the reader, further than PULL from where the partner stands; or the reader
        // keeps going away from them (a lean the partner is following, step for step, so the gap itself stays small).
        double away=Double.isNaN(st[4])?0:-((p.getX()-st[4])*rel.x+(p.getZ()-st[5])*rel.z)/r;st[4]=p.getX();st[5]=p.getZ();
        boolean pulling=toTarget.dot(rel.scale(1/r))<-DanceWeight.PULL||away>DanceWeight.DRIFT;
        st[0]=pulling?st[0]+1:Math.max(0,st[0]-2);
        st[1]=DanceWeight.swirl(st[1],pulling,(int)st[0],st[3]<0?-1:1);
        if(st[1]==0)return false;
        double c=Math.cos(st[1]),s=Math.sin(st[1]),hold=Math.max(DanceWeight.HOLD,Math.min(DanceWeight.HOLD+DanceWeight.GIVE,r))/r;
        var at=p.position().add(new Vec3(rel.x*c-rel.z*s,0,rel.x*s+rel.z*c).scale(hold));
        var before=a.position();a.move(MoverType.SELF,at.subtract(a.position()).multiply(1,0,1));a.walkAnimation.update((float)a.position().distanceTo(before)*4,.4f);
        // The way the partner now goes round becomes the way the reader is swinging, so a swirl let go and taken up again goes on the same way.
        st[2]=Math.atan2(p.getZ()-a.getZ(),p.getX()-a.getX());
        return true;
    }
    /** For the tests: the partner's swirl about the reader (radians a tick), or 0. */
    public static double swirling(LiteraryActor a){var st=SWIRL.get(a.getUUID());return st==null?0:st[1];}
    private static void face(LiteraryActor a,ServerPlayer p,boolean body){
        float yaw=(float)Math.toDegrees(Math.atan2(-(p.getX()-a.getX()),p.getZ()-a.getZ()));a.setYHeadRot(yaw);if(body){a.setYRot(yaw);a.yBodyRot=yaw;}
    }
    /** Looked at: the stranger anywhere near the middle of the view, with nothing in between. */
    static boolean watched(ServerPlayer p,LiteraryActor a){
        var eye=p.getEyePosition();var ray=a.position().add(0,1.1,0).subtract(eye);if(ray.lengthSqr()>24*24||ray.lengthSqr()<.01||p.getLookAngle().dot(ray.normalize())<Math.cos(Math.toRadians(14)))return false;
        return p.serverLevel().clip(new net.minecraft.world.level.ClipContext(eye,a.position().add(0,1.1,0),net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getType()==net.minecraft.world.phys.HitResult.Type.MISS;
    }
    /** Using a stranger: taking the held-out hand. Close enough while the song plays, it is a dance; too far, it says so. */
    public static void take(ServerPlayer p,LiteraryActor a){
        if(HouseReadSites.inside(p)!=HouseReadSites.Room.SHELTER||!a.owner().map(p.getUUID()::equals).orElse(false))return;
        var own=HouseReadSites.state(p.server,p.getUUID());var t=own.getCompound(HouseReadSites.Room.SHELTER.key());
        if(!t.getBoolean("SongStarted")||t.getBoolean("Settled")||!t.getString("Dance").isEmpty())return;
        if(p.distanceToSqr(a)>REACH*REACH){p.displayClientMessage(Component.literal("Too far away to reach.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);return;}
        begin(p,t,a.role()==LiteraryActor.READ_MAN?"man":"woman");own.put(HouseReadSites.Room.SHELTER.key(),t);HouseReadSites.save(p.server,p.getUUID(),own);
    }
    /** The dance begins, and with it the answer: who was danced with, kept by itself, settles the read. */
    static void begin(ServerPlayer p,CompoundTag t,String who){
        int age=t.getInt("CastAge");t.putString("Dance",who);t.putInt("DanceAt",t.getInt("Age"));t.putBoolean("NoteArmed",true);
        HouseReadMoments.literal(t,who.equals("both")?"danced between the two strangers":"took "+name(who.equals("man"),age)+"'s hand and danced");
        HouseRead.see(p,HouseReadSites.Room.SHELTER,x->{x.putString("Stood",who.equals("both")?"middle":who);x.putBoolean("Song",true);x.putBoolean("Danced",true);});
        decide(p,switch(who){case "man"->"MEN";case "woman"->"WOMEN";default->"BOTH";});
    }
    /** A dance or the lantern is the plainest answer the room has: whatever glances came first, the read starts again from it, settled. */
    private static void decide(ServerPlayer p,String value){HouseRead.restart(p,HouseRead.Trait.DRAWN_TO,value);HouseRead.signal(p,HouseRead.Trait.DRAWN_TO,value,true);}
    /** Using the dock lantern while the song plays: staying out by it, with neither of them. */
    public static void lantern(ServerPlayer p,BlockPos b,CompoundTag t){
        if(!t.getBoolean("SongStarted")||t.getBoolean("Settled")||!t.getString("Dance").isEmpty())return;
        HouseReadMoments.literal(t,"stayed out by the lantern at the dock steps");
        HouseRead.see(p,HouseReadSites.Room.SHELTER,x->{x.putString("Stood","lantern");x.putBoolean("Song",true);x.putBoolean("FirstLantern",true);});
        decide(p,"NO_ONE");settle(p,b,t);
    }
    /** Both strangers have come and hold out a hand, and the reader stands between them. */
    static boolean bothOffer(ServerPlayer p,BlockPos b,CompoundTag t){
        if(!HouseReadRemainingRooms.middle(b,p.position()))return false;
        for(String key:List.of("Man","Woman"))if(!t.hasUUID(key)||!(p.serverLevel().getEntity(t.getUUID(key)) instanceof LiteraryActor a)||a.gesture()!=HAND||p.distanceToSqr(a)>OFFER*OFFER)return false;
        return true;
    }
    /**
     * The end: the song is let go, the lamp over the way on comes on and its door swings open. Settled, the room is ready and
     * the strangers stay where they are.
     */
    public static void settle(ServerPlayer p,BlockPos b,CompoundTag t){
        if(t.getBoolean("Settled"))return;t.putBoolean("Settled",true);t.putInt("SongAge",900);t.putBoolean("NoteArmed",true);var l=p.serverLevel();
        for(var sound:HouseReadRegistry.MUSIC)p.connection.send(new ClientboundStopSoundPacket(sound.getId(),SoundSource.BLOCKS));
        HouseReadSites.sound(p,SoundEvents.WOODEN_BUTTON_CLICK_OFF,b.offset(HouseReadRemainingRooms.JUKEBOX_AT),.5f);
        var exit=b.offset(HouseReadMoments.exit(HouseReadSites.Room.SHELTER));
        for(var at:List.of(exit,exit.above())){var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock)l.setBlock(at,s.setValue(DoorBlock.OPEN,true),Block.UPDATE_CLIENTS);}
        HouseReadSites.sound(p,SoundEvents.WOODEN_DOOR_OPEN,exit,.5f);
        var lamp=b.offset(HouseReadRemainingRooms.EXIT_LAMP);var s=l.getBlockState(lamp);if(s.is(HouseReadRegistry.FITTING.get()))l.setBlock(lamp,s.setValue(HouseReadPropBlock.TURNED,false),Block.UPDATE_CLIENTS);
        p.displayClientMessage(Component.literal("The jukebox clicks off. The door under the lamp is open.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
    }
}
