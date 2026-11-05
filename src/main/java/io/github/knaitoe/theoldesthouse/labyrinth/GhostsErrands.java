package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.247, ordinary errands between takes (docs/GHOSTS_ERRANDS_0_4_247.md). The owner, 2 November: "Give Ghosts Set more ordinary
 * behavior between scenes. In the earlier days, a parent clears a plate, someone retrieves a forgotten mug, Barry checks a cable while
 * talking. Small, purposeful actions would make the family feel less like a cast waiting for the player, and give the later
 * disruption more weight. Keep those actions separate from the carefully staged sequences."
 * <p>
 * On the first four days, before the day's take is rolling, the family and Barry go about small things of their own, one at a time,
 * a few seconds apart: Sarah takes a plate from the table to the sink and runs the tap; John goes back to the kitchen for the mug he
 * left by the coffee maker, and sits down with it; Barry gets up from the monitors, crouches over the cable where it is lifting and
 * presses it down, talking about the day the whole time. Each walks there and back through the house's own collision and sits back
 * down where the day put them. Nothing here touches the staged sequences: the errands happen only before a take has begun, and when
 * the take is called whoever is out on one goes straight back to their place, and the take waits for them. From the fifth day nobody
 * in the house is going about anything.
 * <p>
 * Everything is the reader's own, as the cast is: their actors, and the plate gone from the table in their view alone until the day
 * is over.
 */
public final class GhostsErrands {
    private GhostsErrands(){}
    /** One reader's errands for the day, cleared when another day begins. */
    public static final String KEY="Errands0247";
    /** The first errand comes this long into the reader's wait; each next one this long after the last ended. */
    public static final int FIRST=120,BETWEEN=240;
    /** The plate Sarah clears: the near one on her side of the table. */
    static final BlockPos PLATE=new BlockPos(1,1,-18);
    static final double WALK=.3,SIT=.16;

    public enum Errand {
        /** Sarah: the plate to the sink, and the tap run over it. */
        SARAH_PLATE(3,2.4,-17.5,90,new double[][]{{2.4,0,-16.55},{-6.5,0,-16.55},{-6.5,0,-23.4}},180,GhostsSet.STAND,60),
        /** John: back to the kitchen for his mug by the coffee maker, and sitting down with it. */
        JOHN_MUG(2,-.4,-17.5,-90,new double[][]{{-.4,0,-16.55},{-5.5,0,-16.55},{-5.5,0,-23.4}},180,GhostsSet.STAND,30),
        /** Barry: off his chair, crouched over the cable where it lifts by the den, pressing it down while he talks. */
        BARRY_CABLE(4,7.5,-8.5,-153,new double[][]{{8.4,0,-8.5},{8.4,0,-7.5},{7.4,0,-6.5}},90,GhostsSet.CROUCH,70);
        final int who;final double seatX,seatZ;final float seatYaw;final double[][] out;final float workYaw;final int workPose,workTicks;
        Errand(int who,double seatX,double seatZ,float seatYaw,double[][] out,float workYaw,int workPose,int workTicks){
            this.who=who;this.seatX=seatX;this.seatZ=seatZ;this.seatYaw=seatYaw;this.out=out;this.workYaw=workYaw;this.workPose=workPose;this.workTicks=workTicks;
        }
    }
    /** Each earlier day's errands, in the order they come. Day three's family is upstairs for the take, so only Barry has one. */
    public static List<Errand> day(int d){
        return switch(d){
            case 1->List.of(Errand.BARRY_CABLE,Errand.SARAH_PLATE);
            case 2->List.of(Errand.JOHN_MUG,Errand.BARRY_CABLE);
            case 3->List.of(Errand.BARRY_CABLE);
            case 4->List.of(Errand.JOHN_MUG,Errand.SARAH_PLATE);
            default->List.of();
        };
    }
    /** What Barry says while he is down at the cable: the day's work, as he would talk it through with the crew. */
    static String barry(int d){
        return switch(d){
            case 1->"Barry: Interviews in the front room first, then the girls one at a time. Who taped this? It's lifting at the corner.";
            case 2->"Barry: We go straight through supper, no cuts, so nobody gets up for anything. And somebody tape this down before Dad trips on it.";
            case 3->"Barry: Upstairs today, so this run has to reach the landing. It won't, will it. We'll need the long one.";
            default->"Barry: The basement's tight, so the cable goes down the stairs with us. Mind your feet on the way down.";
        };
    }

    /** Whether one of the reader's cast is out on an errand: a take does not begin until they are back. */
    public static boolean busy(CompoundTag own){return own.getCompound(KEY).getInt("Active")>0;}

    /**
     * Before the take, while the reader is about the house: the next errand when it is due, and the one under way stepped on. Once the
     * take has been called (the camera rolling) nobody sets out, and whoever is out comes straight back.
     */
    static void between(ServerPlayer p,BlockPos b,LiteraryActor[] cast,CompoundTag own,int d){
        var e=own.getCompound(KEY);
        if(e.getBoolean("Plate")&&p.level().getGameTime()%100<5)hidePlate(p,b);
        boolean called=own.getBoolean("Rolling");
        if(e.getInt("Active")==0){
            var list=day(d);
            if(called||own.getInt("ShotClock")>0||own.getBoolean("DayDone")||list.isEmpty()||e.getInt("Next")>=list.size()){own.put(KEY,e);return;}
            if(p.position().distanceToSqr(Vec3.atCenterOf(b.offset(0,0,-13)))>24*24){own.put(KEY,e);return;}
            int wait=e.getInt("Wait")+5;e.putInt("Wait",wait);
            int due=e.getInt("Next")==0?FIRST:e.getInt("Done")+BETWEEN;
            if(wait<due){own.put(KEY,e);return;}
            var next=list.get(e.getInt("Next"));
            e.putInt("Active",next.ordinal()+1);e.putInt("Phase",0);e.putInt("Step",0);e.putInt("Work",0);e.putInt("Hold",0);
        }
        if(called&&!e.getBoolean("Hurry")){e.putBoolean("Hurry",true);say(p,own,"Barry: Places, everyone.");}
        step(p,b,cast,own,e,d);
        own.put(KEY,e);
    }
    /** The take has been called with someone still out: they come straight back, and the take waits for them. */
    static void home(ServerPlayer p,BlockPos b,LiteraryActor[] cast,CompoundTag own,int d){
        for(var actor:cast)GhostsMotion.stop(actor);
        between(p,b,cast,own,d);
    }

    private static void step(ServerPlayer p,BlockPos b,LiteraryActor[] cast,CompoundTag own,CompoundTag e,int d){
        var errand=Errand.values()[e.getInt("Active")-1];var a=cast[errand.who];
        boolean hurry=e.getBoolean("Hurry");int phase=e.getInt("Phase");
        double seatY=GhostsSet.seatedHeight(a.getScale());
        switch(phase){
            case 0->{
                // Out: off the seat to the side first, at the seat's height, then down to the floor and on along the way.
                int s=e.getInt("Step");var at=errand.out[s];
                pose(a,GhostsSet.STAND);
                double y=s==0&&horizontal(a,b,at)>.15&&a.getY()-b.getY()>.1?a.getY()-b.getY():at[1];
                move(a,b,at[0],y,at[2],WALK);
                if(arrived(a,b,at[0],at[1],at[2])){
                    if(s==0)setOut(p,b,a,errand,own,e);
                    if(s+1>=errand.out.length){e.putInt("Phase",1);e.putInt("Work",0);pose(a,errand.workPose);atWork(p,b,a,errand,own,d);}
                    else e.putInt("Step",s+1);
                }
            }
            case 1->{
                pose(a,errand.workPose);a.setYRot(Mth.approachDegrees(a.getYRot(),errand.workYaw,30));a.setYBodyRot(a.getYRot());a.setYHeadRot(a.getYRot());
                int work=e.getInt("Work")+5;e.putInt("Work",work);
                if(errand==Errand.SARAH_PLATE&&work==30)sound(p,a,SoundEvents.BUCKET_EMPTY,.45F,1.3F);
                if(hurry||work>=errand.workTicks){done(p,b,a,errand,own,hurry);e.putInt("Phase",2);e.putInt("Step",errand.out.length-1);}
            }
            case 2->{
                int s=e.getInt("Step");var at=errand.out[s];pose(a,GhostsSet.STAND);move(a,b,at[0],at[1],at[2],WALK);
                if(arrived(a,b,at[0],at[1],at[2])){if(s==0)e.putInt("Phase",3);else e.putInt("Step",s-1);}
            }
            case 3->{
                // Back to the seat: up to its height where they left it, then onto it.
                var first=errand.out[0];pose(a,GhostsSet.STAND);
                // Barry's director's chair has its back right behind where he sits: he comes back onto the seat a hair in front of it.
                if(a.getY()-b.getY()<seatY-.02)move(a,b,first[0],seatY,first[2],SIT);
                else move(a,b,errand.seatX,seatY,errand.seatZ-(errand==Errand.BARRY_CABLE?.06:0),SIT);
                if(a.position().distanceToSqr(Vec3.atLowerCornerOf(b).add(errand.seatX,seatY,errand.seatZ))<.04){pose(a,GhostsSet.SIT);e.putInt("Phase",4);e.putInt("Hold",0);}
            }
            default->{
                pose(a,GhostsSet.SIT);a.setYRot(Mth.approachDegrees(a.getYRot(),errand.seatYaw,30));a.setYBodyRot(a.getYRot());a.setYHeadRot(Mth.approachDegrees(a.getYHeadRot(),errand.seatYaw,30));
                int hold=e.getInt("Hold")+5;e.putInt("Hold",hold);
                // John keeps his mug a moment, then puts it down by his plate.
                if(errand==Errand.JOHN_MUG&&!hurry&&hold<60)return;
                if(!a.getMainHandItem().isEmpty()){a.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);sound(p,a,SoundEvents.DECORATED_POT_HIT,.4F,1.6F);}
                e.putInt("Active",0);e.putInt("Next",e.getInt("Next")+1);e.putInt("Done",e.getInt("Wait"));e.remove("Hurry");
            }
        }
    }
    /** Up and away from the seat. */
    private static void setOut(ServerPlayer p,BlockPos b,LiteraryActor a,Errand errand,CompoundTag own,CompoundTag e){
        switch(errand){
            case SARAH_PLATE->{
                // She reaches over and takes the plate on her side of the table; it is gone from it.
                a.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(GoatmanRegistry.PLATE.get()));e.putBoolean("Plate",true);hidePlate(p,b);
                sound(p,a,SoundEvents.DECORATED_POT_HIT,.35F,1.7F);say(p,own,"Sarah: I'll just get these out of the way before you start.",a);
            }
            case JOHN_MUG->say(p,own,"John: Hang on. Where did I put my coffee?",a);
            case BARRY_CABLE->{}
        }
    }
    /** At the place the errand is for. */
    private static void atWork(ServerPlayer p,BlockPos b,LiteraryActor a,Errand errand,CompoundTag own,int d){
        switch(errand){
            case SARAH_PLATE->{a.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);sound(p,a,SoundEvents.DECORATED_POT_HIT,.45F,1.5F);}
            case JOHN_MUG->{}
            case BARRY_CABLE->say(p,own,barry(d));
        }
    }
    /** The errand done (or cut short when the take is called). */
    private static void done(ServerPlayer p,BlockPos b,LiteraryActor a,Errand errand,CompoundTag own,boolean hurry){
        switch(errand){
            case SARAH_PLATE->{}
            case JOHN_MUG->{if(!hurry){a.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(LiteraryRegistry.GHOSTS_MUG.get()));say(p,own,"John: There it is. Still warm, nearly.");}}
            case BARRY_CABLE->sound(p,a,SoundEvents.WOOL_PLACE,.6F,.8F);
        }
    }

    // ------------------------------------------------------------------ helpers
    private static void pose(LiteraryActor a,int phase){if(a.phase()!=phase)a.appearance(a.role(),phase);}
    private static void move(LiteraryActor a,BlockPos b,double x,double y,double z,double speed){GhostsMotion.toward(a,new Vec3(b.getX()+x,b.getY()+y,b.getZ()+z),speed);}
    private static double horizontal(LiteraryActor a,BlockPos b,double[] at){double dx=a.getX()-b.getX()-at[0],dz=a.getZ()-b.getZ()-at[2];return Math.sqrt(dx*dx+dz*dz);}
    private static boolean arrived(LiteraryActor a,BlockPos b,double x,double y,double z){return a.position().distanceToSqr(Vec3.atLowerCornerOf(b).add(x,y,z))<.02;}
    private static void say(ServerPlayer p,CompoundTag own,String line){
        p.displayClientMessage(Component.literal(line),true);own.putLong("FilmIdle0490",p.level().getGameTime());
    }
    /** As above, with the speaker's mouth moving while the line is up. */
    private static void say(ServerPlayer p,CompoundTag own,String line,LiteraryActor speaker){say(p,own,line);if(speaker!=null)speaker.speaks(line);}
    private static void sound(ServerPlayer p,LiteraryActor a,net.minecraft.sounds.SoundEvent sound,float volume,float pitch){
        p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),SoundSource.NEUTRAL,a.getX(),a.getY()+1,a.getZ(),volume,pitch,p.getRandom().nextLong()));
    }
    /** The plate Sarah took, gone from the table in this reader's view (sent again now and then, so a section loaded afresh agrees). */
    static void hidePlate(ServerPlayer p,BlockPos b){p.connection.send(new ClientboundBlockUpdatePacket(b.offset(PLATE),Blocks.AIR.defaultBlockState()));}
    /** A new day: the table as it really is, and nobody carrying anything. */
    static void reset(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getCompound(KEY).getBoolean("Plate"))p.connection.send(new ClientboundBlockUpdatePacket(p.serverLevel(),b.offset(PLATE)));
        own.remove(KEY);
    }
    /** For tests: the errand under way, its phase, and how many of the day's are done. */
    public static String state(CompoundTag own){var e=own.getCompound(KEY);int active=e.getInt("Active");return active==0?"none/"+e.getInt("Next"):Errand.values()[active-1].name()+"/"+e.getInt("Phase")+"/"+e.getInt("Next");}
    public static boolean plateTaken(CompoundTag own){return own.getCompound(KEY).getBoolean("Plate");}
}
