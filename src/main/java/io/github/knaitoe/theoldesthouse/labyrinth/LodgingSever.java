package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.615, The Ones Already Inside, its fifteenth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, E3: "Sever the house. The ratchet
 * released in the cellar, the catch at the occupied frame (in either order, slowly alone, faster with somebody at the other). Every
 * original walks out; Dan and the failed bodies begin to come apart. One begs for the frame back; one goes for a freed original to
 * force itself back in; one sits down.").
 *
 * <ul>
 * <li><b>Who.</b> Only a reader who knows the third way (the dead post, 0.4.610) finds the pawl on the ratchet's wheel or the catch in
 * Daniel's frame's jamb. Not once Dan has been put back or completed, or while either is under way.</li>
 * <li><b>The two.</b> Each is eight lifts, a lift a hand at most every six ticks. A lift while somebody else has worked the other in
 * the last three seconds counts twice. Alone it is slow, and louder (the wheel and the catch are heard all over the house). A reader
 * who stops leaves it where it is.</li>
 * <li><b>Severed.</b> When both are done the house settles. Daniel's straps open and he walks out of his room to the hall bench; Dan
 * comes to the bench as well, and holds the shape of a hand together under his sleeve; Daniel keeps leaning forward to stand without
 * the frame and sitting back. The body at the dead post begs for the frame back; the one at the upstairs hatch sits down where it is;
 * the one at the jib door comes through it for Daniel, to force itself back in, until it is struck off twice or gives up.</li>
 * </ul>
 */
public final class LodgingSever {
    private LodgingSever(){}
    public static final int NONE=0,SEVERED=1,TO_BENCH=2,DONE=3;
    public static final String STATE="Sever0615";
    /** Each reader's own. */
    public static final String RATCHET="WorkedRatchet0615",CATCH="WorkedCatch0615",SAW="SawSevered0615",STRUCK="StruckShirt0615",SAT_WITH="SawBench0615";
    public static final int STEPS=8,GAP=6,PARTNER=60,SHIRT_GIVES_UP=400,BLOWS=2;
    /** Daniel seated, leaning to stand; a failed body sat on the floor. */
    public static final int RISE=25,SAT=26;
    static final LodgingPeople.Spot BENCH_DAN=new LodgingPeople.Spot(3.5,LodgingSite.GROUND,-2.5,90,LodgingPeople.HURT),
        BENCH_DANIEL=new LodgingPeople.Spot(3.5,LodgingSite.GROUND,-1.5,90,RISE);
    static final Vec3 SHIRT_AT=new Vec3(2.0,0,-1.6);

    static CompoundTag state(CompoundTag house){if(!house.contains(STATE))house.put(STATE,new CompoundTag());return house.getCompound(STATE);}
    public static int phase(MinecraftServer s,BlockPos b){return state(LodgingStory.house(s,b)).getInt("Phase");}
    public static boolean severed(MinecraftServer s,BlockPos b){return phase(s,b)>=SEVERED;}
    public static int ratchet(MinecraftServer s,BlockPos b){return state(LodgingStory.house(s,b)).getInt("Ratchet");}
    public static int catch_(MinecraftServer s,BlockPos b){return state(LodgingStory.house(s,b)).getInt("Catch");}
    /** Neither of the other two ends under way or done, and the house settled. */
    static boolean open(MinecraftServer s,BlockPos b){
        return LodgingReturn.ready(s,b)&&LodgingReturn.phase(s,b)==LodgingReturn.NONE&&!LodgingComplete.begun(s,b)&&!severed(s,b);
    }
    private static void go(CompoundTag in,int phase,int clock){in.putInt("Phase",phase);in.putInt("At",clock);}

    /** Where one of the four is kept by this slice, or null. */
    static @Nullable LodgingPeople.Spot spot(MinecraftServer s,BlockPos b,int who){
        var in=state(LodgingStory.house(s,b));if(in.getInt("Phase")<SEVERED)return null;
        if(who==LodgingPeople.DAN&&in.getBoolean("DanSat"))return BENCH_DAN;
        if(who==LodgingPeople.DANIEL&&in.getBoolean("DanielSat"))return BENCH_DANIEL;
        return null;
    }

    // ------------------------------------------------------------------------------------------------ hands on the two
    /** A hand on the ratchet's wheel in the cellar. True if this slice answered it. */
    public static boolean ratchet(ServerPlayer p,BlockPos b){return work(p,b,"Ratchet","Catch",RATCHET,LodgingTexts.RATCHET_LIFT,LodgingTexts.RATCHET_AGAIN,LodgingTexts.RATCHET_DONE);}
    /** A hand on the jamb of Daniel's frame, from his room. True if this slice answered it. */
    public static boolean catch_(ServerPlayer p,BlockPos b){return work(p,b,"Catch","Ratchet",CATCH,LodgingTexts.CATCH_LIFT,LodgingTexts.CATCH_AGAIN,LodgingTexts.CATCH_DONE);}
    private static boolean work(ServerPlayer p,BlockPos b,String mine,String other,String record,String first,String again,String done){
        var own=LodgingHouse.own(p);if(!own.getBoolean(LodgingCellar.KNOWS_THIRD))return false;
        var s=p.server;var h=LodgingStory.house(s,b);var in=state(h);
        if(in.getInt(mine)>=STEPS){LodgingHouse.cue(p,done);return true;}
        if(!open(s,b))return false;
        int clock=h.getInt("Clock");if(clock-own.getInt("SeverTry0615")<GAP&&own.contains("SeverTry0615"))return true;
        own.putInt("SeverTry0615",clock);
        // Somebody else at the other one just now: both go quicker.
        boolean partner=in.contains(other+"At")&&clock-in.getInt(other+"At")<=PARTNER&&!in.getString(other+"By").equals(p.getUUID().toString());
        int was=in.getInt(mine),now=Math.min(STEPS,was+(partner?2:1));in.putInt(mine,now);in.putInt(mine+"At",clock);in.putString(mine+"By",p.getUUID().toString());
        own.putBoolean(record,true);LodgingHouse.save(p,own);
        var l=p.serverLevel();var at=b.offset(mine.equals("Ratchet")?LodgingSite.RATCHET:LodgingSite.DANIEL_FRAME);
        // Alone, it is louder: heard through the house.
        l.playSound(null,at,mine.equals("Ratchet")?SoundEvents.CHAIN_PLACE:SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.BLOCKS,partner?.5F:1.4F,mine.equals("Ratchet")?.6F+.05F*now:1.4F);
        l.sendParticles(ParticleTypes.WHITE_ASH,at.getX()+.5,at.getY()+1,at.getZ()+.5,6,.3,.3,.3,0);
        LodgingHouse.cue(p,now>=STEPS?done:was==0?first:again);
        if(now>=STEPS&&in.getInt(other)>=STEPS)sever(l,b,h,in,clock);
        LodgingStory.save(s,b,h);p.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ severed
    private static void sever(ServerLevel l,BlockPos b,CompoundTag h,CompoundTag in,int clock){
        go(in,SEVERED,clock);
        for(var p:LodgingHouse.readers(l,b)){if(p.isSpectator())continue;LodgingHouse.cue(p,LodgingTexts.SEVERED);var own=LodgingHouse.own(p);own.putBoolean(SAW,true);LodgingHouse.save(p,own);}
        l.playSound(null,b.offset(LodgingSite.RATCHET),SoundEvents.ANVIL_LAND,SoundSource.BLOCKS,.6F,.5F);
        var s=l.getServer();
        // Every original walks out: Daniel's straps open where they are still shut.
        if(!LodgingKey.free(s,b)){LodgingKey.release(l,b,h,false);}
        h.putBoolean(LodgingKey.IN,true);
        var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);if(daniel!=null){in.putString("DanielFrom",zone(b,daniel));daniel.gesture(LodgingPeople.WAIT);daniel.noPhysics=false;daniel.setNoGravity(false);}
        var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);if(dan!=null){in.putString("DanFrom",zone(b,dan));dan.gesture(LodgingPeople.WAIT);}
        // The failed bodies: one begs, one sits, one comes for Daniel.
        var post=LodgingPeople.find(l,b,LodgingPeople.FAILED_POST);if(post!=null){post.gesture(19);aloud(l,b,post,"The failed body",LodgingTexts.BEG_FRAME);}
        var dress=LodgingPeople.find(l,b,LodgingPeople.FAILED_DRESS);if(dress!=null)dress.gesture(SAT);
        var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);if(vera!=null)aloud(l,b,vera,"Vera",LodgingTexts.VERA_SEVERED);
        in.putInt("ShirtLeg",0);
        go(in,TO_BENCH,clock);
    }
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var s=l.getServer();var h=LodgingStory.house(s,b);var in=state(h);if(in.getInt("Phase")!=TO_BENCH)return;
        int clock=h.getInt("Clock");
        var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);
        if(!in.getBoolean("DanielSat")&&daniel!=null&&walk(l,b,in,"DanielLeg",daniel,in.getString("DanielFrom"),BENCH_DANIEL,.07)){
            in.putBoolean("DanielSat",true);seat(daniel,BENCH_DANIEL);aloud(l,b,daniel,"Daniel",LodgingTexts.DANIEL_SITS);}
        if(!in.getBoolean("DanSat")&&dan!=null&&walk(l,b,in,"DanLeg",dan,in.getString("DanFrom"),BENCH_DAN,.06)){
            in.putBoolean("DanSat",true);seat(dan,BENCH_DAN);}
        shirt(l,b,in,daniel,here,clock);
        if((in.getBoolean("DanielSat")||daniel==null)&&(in.getBoolean("DanSat")||dan==null)&&in.getBoolean("ShirtDone")){
            for(var p:here){if(p.isSpectator())continue;var near=dan!=null?dan:daniel;if(near!=null&&p.distanceTo(near)<=12){LodgingHouse.cue(p,LodgingTexts.ON_THE_BENCH);var own=LodgingHouse.own(p);own.putBoolean(SAT_WITH,true);LodgingHouse.save(p,own);}}
            go(in,DONE,clock);
        }
        LodgingStory.save(s,b,h);
    }
    /** The body at the jib door: through it and along to Daniel, to force itself back in, until struck off or it gives up. */
    private static void shirt(ServerLevel l,BlockPos b,CompoundTag in,@Nullable LiteraryActor daniel,List<ServerPlayer> here,int clock){
        if(in.getBoolean("ShirtDone"))return;
        var shirt=LodgingPeople.find(l,b,LodgingPeople.FAILED_SHIRT);
        if(shirt==null||daniel==null){in.putBoolean("ShirtDone",true);return;}
        if(in.getBoolean("ShirtThere")&&clock-in.getInt("ThereAt")>=SHIRT_GIVES_UP){sitDown(l,b,in,shirt);return;}
        if(!in.getBoolean("ShirtThere")){
            shirt.gesture(LodgingPeople.REACH);
            var legs=List.of(LodgingTaken.P0,LodgingTaken.P1,new Vec3(0,0,-13.5),new Vec3(0,0,-5.5),SHIRT_AT);
            if(LodgingTaken.walk(l,b,in,"ShirtLeg",shirt,legs,Map.of(1,LodgingSite.JIB),.09)){in.putBoolean("ShirtThere",true);in.putInt("ThereAt",clock);}
            return;
        }
        // At him: its hands on him, trying.
        LodgingInjury.step(l,shirt,daniel.position(),.03);face(shirt,LodgingPeople.yawTo(shirt,daniel.position()));
        if(shirt.gesture()!=LodgingPeople.GRAB){shirt.gesture(LodgingPeople.GRAB);aloud(l,b,daniel,"Daniel",LodgingTexts.DANIEL_HANDS);
            for(var p:here)if(!p.isSpectator()&&p.distanceTo(shirt)<=12)LodgingHouse.cue(p,LodgingTexts.SHIRT_AT_HIM);}
    }
    private static void sitDown(ServerLevel l,BlockPos b,CompoundTag in,LiteraryActor shirt){shirt.gesture(SAT);in.putBoolean("ShirtDone",true);
        for(var p:LodgingHouse.readers(l,b))if(!p.isSpectator()&&p.distanceTo(shirt)<=12)LodgingHouse.cue(p,LodgingTexts.SHIRT_SITS);}
    /** A blow on the body at the jib door while it is after Daniel. True if this slice took it. */
    public static boolean struck(ServerPlayer p,LiteraryActor body){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null)return false;var l=p.serverLevel();var h=LodgingStory.house(l.getServer(),b);var in=state(h);
        if(in.getInt("Phase")!=TO_BENCH||in.getBoolean("ShirtDone"))return false;
        int n=in.getInt("ShirtBlows")+1;in.putInt("ShirtBlows",n);
        var own=LodgingHouse.own(p);own.putBoolean(STRUCK,true);LodgingHouse.save(p,own);
        l.playSound(null,body.blockPosition(),SoundEvents.PLAYER_ATTACK_WEAK,SoundSource.PLAYERS,.8F,.8F);
        if(n>=BLOWS)sitDown(l,b,in,body);
        LodgingStory.save(l.getServer(),b,h);return true;
    }

    // ------------------------------------------------------------------------------------------------ walking
    private static String zone(BlockPos b,LiteraryActor a){var r=LodgingInjury.rel(b,a);return LodgingSite.inParlor(r)?"parlor":inFrame(r)?"frame":LodgingSite.inDaniels(r)?"daniels":"hall";}
    /** His frame stands against the room's north wall, a little past where the room is counted from. */
    private static boolean inFrame(Vec3 r){return r.y<LodgingSite.UPPER-1&&r.y>LodgingSite.CELLAR+1&&r.x>=-13&&r.x<=-3&&r.z>=LodgingSite.ROOM_N-1&&r.z<-28;}
    /** From where they are to their end of the bench, by the doors between. */
    private static boolean walk(ServerLevel l,BlockPos b,CompoundTag in,String key,LiteraryActor a,String from,LodgingPeople.Spot to,double pace){
        var end=new Vec3(to.x(),0,to.z());
        List<Vec3> legs;Map<Integer,BlockPos> doors;
        switch(from){
            case "frame"->{legs=List.of(new Vec3(-7.5,0,-27.4),new Vec3(-5.6,0,-26.6),new Vec3(-5.6,0,-20),new Vec3(-7.5,0,-18.5),new Vec3(-7.5,0,-14),new Vec3(0,0,-14),new Vec3(0,0,-5.5),new Vec3(2.0,0,to.z()),end);doors=Map.of(4,LodgingSite.DANIEL_DOOR);}
            case "daniels"->{legs=List.of(new Vec3(-5.6,0,-26.6),new Vec3(-5.6,0,-20),new Vec3(-7.5,0,-18.5),new Vec3(-7.5,0,-14),new Vec3(0,0,-14),new Vec3(0,0,-5.5),new Vec3(2.0,0,to.z()),end);doors=Map.of(3,LodgingSite.DANIEL_DOOR);}
            case "parlor"->{legs=List.of(new Vec3(-5.6,0,-8.5),new Vec3(-2.6,0,-8.5),new Vec3(0,0,-8.5),new Vec3(0,0,-5.5),new Vec3(2.0,0,to.z()),end);doors=Map.of(1,LodgingSite.PARLOR_DOOR);}
            default->{legs=List.of(new Vec3(2.0,0,to.z()),end);doors=Map.of();}
        }
        if(a.gesture()!=LodgingPeople.WAIT&&!in.getBoolean(key+"Done"))a.gesture(LodgingPeople.WAIT);
        boolean there=LodgingTaken.walk(l,b,in,key,a,legs,doors,pace);if(there)in.putBoolean(key+"Done",true);
        return there;
    }
    private static void seat(LiteraryActor a,LodgingPeople.Spot at){face(a,at.look());a.gesture(at.gesture());a.setDeltaMovement(Vec3.ZERO);}
    private static void face(LiteraryActor a,float yaw){a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    private static void aloud(ServerLevel l,BlockPos b,LiteraryActor a,String name,String line){
        for(var p:LodgingHouse.readers(l,b))if(!p.isSpectator()&&p.distanceTo(a)<=LodgingDaniel.HEARD)Talk.line(p,name,line);
        LodgingStory.speak(l,b,a,name,line);
    }
    /** For a house built again. */
    public static void clear(MinecraftServer s,BlockPos b){LodgingStory.house(s,b).remove(STATE);}
}
