package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import io.github.knaitoe.theoldesthouse.house.PlaytestLog;

/** A real bed, the original extra child, and the original cousins on the morning trail. */
public final class GoatmanBedtime {
    public static final int QUIET_TICKS=240,TALK_GAP=160,SETTLED=600;
    /**
     * 0.4.145 (owner): it stands laughing longer, and more times, before it comes for a bed; it crawls there,
     * jumps up at the foot of the bed, and the reader goes black and out of the trailer.
     * 0.4.199 (owner: "The Goatman ending sequence doesn't show the Goatman-kid crawl. When the Goatman jumps up to the player
     * it should be as the Goatman, not in his kid disguise"): the crawl is seen, the child it is wearing down on its hands and
     * knees along the aisle to the foot of the bed; there it comes up as itself, the Goatman, horns and all. Going to the next
     * bed it is down on the floor again in the child's shape.
     */
    public static final int LAUGH_TICKS=140,LAUGHS=4,JUMP_TICKS=16,BLACK_AT=6;
    private static final double[] HOP={0,.35,.55,.35,0};
    private static final Set<UUID> ending=new HashSet<>();
    private GoatmanBedtime(){}
    public static boolean enabled(CompoundTag r){return GoatmanVignette.brisk(r);}
    public static boolean called(CompoundTag r){return enabled(r)&&r.getBoolean("BedtimeCalled131");}
    /** Once it has first got up out of its bunk, the in-bed chat fades for the sleepers. */
    public static boolean risen(CompoundTag r){return enabled(r)&&r.getInt("Phase")==GoatmanVignette.VIGIL&&r.getInt("BedStage131")>=1;}
    public static boolean quiet(CompoundTag r){return enabled(r)&&r.contains("BedQuietAt131")&&r.getInt("Phase")==GoatmanVignette.VIGIL;}
    private static boolean admitted(CompoundTag r){int x=r.getInt("ExtraState");return x==GoatmanVignette.X_WINDOW||x==GoatmanVignette.X_INSIDE||x==GoatmanVignette.X_FLOOR;}
    public static boolean locked(ServerPlayer p){return locked(p,GoatmanVignette.run(LabyrinthData.get(p.server)));}
    private static boolean locked(ServerPlayer p,CompoundTag r){
        var own=r.getCompound("Cohort").getCompound(p.getUUID().toString());
        return !ending.contains(p.getUUID())&&GoatmanVignette.inside(p)&&enabled(r)&&r.getInt("Phase")==GoatmanVignette.VIGIL&&admitted(r)&&own.getBoolean("Active")&&!own.getBoolean("Failed")&&own.contains("Bed131");
    }
    /** Only the actual vignette conclusion may release a committed reader. Respawn metadata is untouched. */
    static void wake(ServerPlayer p){ending.add(p.getUUID());try{if(p.isSleeping())p.stopSleepInBed(true,false);}finally{ending.remove(p.getUUID());}}
    static void restore(ServerPlayer p,CompoundTag r){
        if(p.isSleeping()||!locked(p,r))return;var head=BlockPos.of(r.getCompound("Cohort").getCompound(p.getUUID().toString()).getLong("Bed131"));
        if(p.serverLevel().getBlockState(head).getBlock() instanceof BedBlock)p.startSleeping(head);
    }
    static int bunk(CompoundTag r,int child){int wrong=r.getInt("Wrong");return child==wrong?GoatmanVignette.count(r)-1:child<wrong?child:child-1;}
    static void call(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> readers){
        if(!enabled(r)||r.contains("BedQuietAt131"))return;
        r.putInt("BedQuietAt131",r.getInt("Clock"));r.remove("Demand");r.remove("DemandAt");GoatmanFear.clearDialogue(r);
        for(var p:readers)GoatmanVignette.scene(p,r);
    }
    /** Uses LivingEntity's bed pose directly: it never assigns a respawn position or skips the world's night. */
    public static boolean use(ServerPlayer p,BlockPos b,BlockPos clicked){
        var d=LabyrinthData.get(p.server);var r=GoatmanVignette.run(d);var cohort=r.getCompound("Cohort");var own=cohort.getCompound(p.getUUID().toString());
        if(!called(r)||r.getInt("Phase")!=GoatmanVignette.VIGIL||!own.getBoolean("Active")||own.getBoolean("Failed")||p.containerMenu!=p.inventoryMenu||p.distanceToSqr(clicked.getCenter())>36)return false;
        var state=p.serverLevel().getBlockState(clicked);if(!(state.getBlock() instanceof BedBlock))return false;
        var head=state.getValue(BedBlock.PART)==BedPart.HEAD?clicked:clicked.relative(state.getValue(BedBlock.FACING));
        // 0.4.150: the bunk of a cousin whose place a reader took is that reader's to sleep in.
        for(int i=0;i<GoatmanVignette.count(r);i++)if(!GoatmanVignette.playedBy(r,i)&&head.equals(b.offset(GoatmanWoods.bunkHead(bunk(r,i)))))return false;
        if(state.getValue(BedBlock.OCCUPIED))return false;
        p.startSleeping(head);if(!p.isSleeping())return false;
        own.putLong("Bed131",head.asLong());own.putInt("SleepTicks131",0);cohort.put(p.getUUID().toString(),own);r.put("Cohort",cohort);GoatmanVignette.saveRun(d,r);return true;
    }
    static boolean cousin(ServerLevel l,BlockPos b,CompoundTag r,GoatmanChild c,List<ServerPlayer> readers){
        if(!enabled(r))return false;
        if(r.getInt("Phase")==GoatmanVignette.DAWN&&r.getBoolean("Right")){morning(l,b,r,c,readers);return true;}
        if(!called(r)||r.getInt("Phase")!=GoatmanVignette.VIGIL)return false;
        int i=c.getPersistentData().getInt(GoatmanVignette.INDEX);
        if(!c.isSleeping()&&r.getInt("Clock")-r.getInt("BedCallAt131")<TALK_GAP*2+8*bunk(r,i))return false;
        if(i==r.getInt("Wrong")){
            int xs=r.getInt("ExtraState");if(xs!=GoatmanVignette.X_INSIDE&&xs!=GoatmanVignette.X_FLOOR)return false;
            if(r.getInt("BedStage131")==0)GoatmanVignette.bunk(c,b,bunk(r,i),l);
        }else{
            int rs=r.getInt("RunnerState");if(i==r.getInt("Runner")&&rs!=GoatmanVignette.R_INSIDE&&rs!=GoatmanVignette.R_HOME)return false;
            GoatmanVignette.bunk(c,b,bunk(r,i),l);
        }
        c.fear(0);c.fearStage(0);c.cower(false);return true;
    }
    static boolean canDawn(CompoundTag r,List<ServerPlayer> readers){
        if(!enabled(r))return true;if(!called(r))return false;
        boolean inside=r.getInt("ExtraState")==GoatmanVignette.X_FLOOR||r.getInt("ExtraState")==GoatmanVignette.X_INSIDE;
        if(inside&&r.getInt("BedStage131")<8||!r.getBoolean("CousinsAsleep131")||r.getInt("BedTalk131")<4)return false;
        // 0.4.150: a reader who stayed up is never its target; once it has finished with every bed, morning comes for them too.
        var cohort=r.getCompound("Cohort");return readers.stream().allMatch(p->{var own=cohort.getCompound(p.getUUID().toString());return inside?own.getBoolean("BedsideEnded131")||!p.isSleeping()&&r.getInt("BedStage131")>=8:own.getInt("SleepTicks131")>=100;});
    }
    static void tick(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> readers,List<GoatmanChild> cast){
        if(!enabled(r)||r.getInt("Phase")!=GoatmanVignette.VIGIL)return;
        if(!called(r)){
            if(!r.contains("BedQuietAt131")||r.getInt("Clock")-r.getInt("BedQuietAt131")<QUIET_TICKS)return;
            r.putBoolean("BedtimeCalled131",true);r.putInt("BedCallAt131",r.getInt("Clock"));r.putInt("BedTalk131",1);
            GoatmanWoods.beds(l,b,GoatmanVignette.expected(r,r.getCompound("Cohort").getAllKeys().size())+1);
            GoatmanVignette.say(readers,readers.size()>1?"A cousin: Nobody's going out there. We stay in here, all of us. Everybody take a bunk.":"A cousin: I'm not going out there. We've got to stay here.");
        }
        int settling=r.getInt("Clock")-r.getInt("BedCallAt131"),talk=r.getInt("BedTalk131");
        if(talk==1&&settling>=TALK_GAP){GoatmanVignette.say(readers,"A cousin: Are you sure? What if it comes back?");r.putInt("BedTalk131",2);}
        else if(talk==2&&settling>=TALK_GAP*2){GoatmanVignette.say(readers,"A cousin: We'll leave as soon as it's light. All of us.");r.putInt("BedTalk131",3);}
        else if(talk==3&&settling>=TALK_GAP*3){GoatmanVignette.say(readers,"A cousin: Okay. Leave that light on. Please.");r.putInt("BedTalk131",4);}
        var cohort=r.getCompound("Cohort");
        for(var p:readers){restore(p,r);var own=cohort.getCompound(p.getUUID().toString());boolean sleeping=own.contains("Bed131")&&p.getSleepingPos().filter(BlockPos.of(own.getLong("Bed131"))::equals).isPresent();own.putInt("SleepTicks131",sleeping?own.getInt("SleepTicks131")+1:0);cohort.put(p.getUUID().toString(),own);}
        r.put("Cohort",cohort);
        for(var c:cast)if(c.isSleeping()){c.fear(0);c.fearStage(0);c.cower(false);}
        r.putBoolean("CousinsAsleep131",cast.stream().filter(c->c.getPersistentData().getInt(GoatmanVignette.INDEX)!=r.getInt("Wrong")).allMatch(GoatmanChild::isSleeping));
        int xs=r.getInt("ExtraState");if(xs!=GoatmanVignette.X_FLOOR&&xs!=GoatmanVignette.X_INSIDE)return;
        var original=cast.stream().filter(c->c.getPersistentData().getInt(GoatmanVignette.INDEX)==r.getInt("Wrong")).findFirst().orElse(null);if(original==null)return;
        var target=r.hasUUID("BedTarget131")?readers.stream().filter(p->p.getUUID().equals(r.getUUID("BedTarget131"))).findFirst().orElse(null):null;
        int stage=r.getInt("BedStage131");
        if(stage!=5&&original.isInvisible())original.setInvisible(false);
        if(target==null){
            target=next(r,readers);
            if(target==null)return;r.putUUID("BedTarget131",target.getUUID());if(stage>=7){crawl(original,b,BlockPos.of(r.getCompound("Cohort").getCompound(target.getUUID().toString()).getLong("Bed131")),r);stage=5;r.putInt("BedStage131",stage);r.putInt("BedBeat131",0);}
        }
        var own=r.getCompound("Cohort").getCompound(target.getUUID().toString());
        if(!target.isSleeping()&&!own.getBoolean("BedsideEnded131")){original.heave(false);original.pose(false);return;}
        int t=r.getInt("BedBeat131");
        switch(stage){
            case 0->{if(!original.isSleeping()||!r.getBoolean("CousinsAsleep131")||r.getInt("BedTalk131")<4||settling<SETTLED)return;rise(original,b,bunk(r,r.getInt("Wrong")));stage=1;t=0;}
            case 1,3->{
                if(!GoatmanVignette.advance(original,b,.09,l,true))return;
                original.pose(false);original.heave(true);face(original,target.position());
                if(++t>=LAUGH_TICKS){original.heave(false);GoatmanVignette.bunk(original,b,bunk(r,r.getInt("Wrong")),l);stage++;t=0;r.putInt("BedLaughs131",r.getInt("BedLaughs131")+1);}
            }
            case 2,4->{
                if(!GoatmanVignette.bunk(original,b,bunk(r,r.getInt("Wrong")),l))return;
                if(++t>=50){if(r.getInt("BedLaughs131")<LAUGHS){rise(original,b,bunk(r,r.getInt("Wrong")));stage=3;}else{crawl(original,b,BlockPos.of(own.getLong("Bed131")),r);stage=5;}t=0;}
            }
            case 5->{
                // The child it is wearing, down on its hands and knees, crawling the aisle to the foot of the bed.
                original.indoors(true);original.setCrawling(true);original.setPose(Pose.SWIMMING);original.heave(false);original.setInvisible(false);
                if(original.monsterAppearance())original.disguise(r.contains("BedDisguise199")?r.getInt("BedDisguise199"):-1);
                if(!GoatmanVignette.advance(original,b,.09,l,true))return;original.pose(false);r.putDouble("BedJumpY131",original.getY());
                // It comes up as itself.
                original.disguise(-2);original.setCrawling(false);original.setPose(Pose.STANDING);stage=6;t=0;
            }
            case 6->{
                // Up at the foot of the bed, in front of the sleeper's eyes: the Goatman, not the child.
                original.setInvisible(false);original.setCrawling(false);original.setPose(Pose.STANDING);original.pose(false);original.disguise(-2);face(original,target.getEyePosition());
                t++;double floor=r.contains("BedJumpY131")?r.getDouble("BedJumpY131"):original.getY();original.moveTo(original.getX(),floor+(t<HOP.length?HOP[t]:0),original.getZ(),original.getYRot(),0);
                if(t==1){l.playSound(null,original.blockPosition(),SoundEvents.WOOD_STEP,SoundSource.HOSTILE,1F,.6F);l.playSound(null,original.blockPosition(),GoatmanRegistry.LAUGH.get(),SoundSource.HOSTILE,1F,.85F);}
                if(t>=JUMP_TICKS){
                    GoatmanHaunt.haunt(target,r.contains("BedFace199")?r.getInt("BedFace199"):original.visibleSkin(),r.getInt("Tells"));own.putBoolean("BedsideEnded131",true);own.putBoolean("TookItHome",true);
                    cohort=r.getCompound("Cohort");cohort.put(target.getUUID().toString(),own);r.put("Cohort",cohort);
                    io.github.knaitoe.theoldesthouse.network.HousePackets.send(target,new io.github.knaitoe.theoldesthouse.network.HouseFadePayload(3,BLACK_AT+24,30));
                    PlaytestLog.event(target,"goatman_bedside");stage=7;t=0;
                }
            }
            case 7->{
                // Black by now: the reader is out of the trailer, and it can go to the next bed.
                if(++t>=BLACK_AT){own.putBoolean("Active",false);own.putBoolean("Failed",true);cohort=r.getCompound("Cohort");cohort.put(target.getUUID().toString(),own);r.put("Cohort",cohort);
                    GoatmanVignette.carriedOff(target);r.remove("BedTarget131");r.remove("BedJumpY131");stage=8;t=0;}
            }
            default->{}
        }
        r.putInt("BedStage131",stage);r.putInt("BedBeat131",t);
    }
    /**
     * 0.4.150: whose bed it comes to next. Only a reader asleep in their bunk: the latecomer first, then the others from the
     * bunk farthest from the door inward, the west bunk of a pair first. A reader who stayed up is never chosen.
     */
    public static ServerPlayer next(CompoundTag r,List<ServerPlayer> readers){
        var cohort=r.getCompound("Cohort");
        return readers.stream().filter(p->{var own=cohort.getCompound(p.getUUID().toString());return p.isSleeping()&&own.contains("Bed131")&&!own.getBoolean("BedsideEnded131");})
                .min(Comparator.<ServerPlayer>comparingInt(p->cohort.getCompound(p.getUUID().toString()).getBoolean("Latecomer0150")?0:1)
                        .thenComparingInt(p->BlockPos.of(cohort.getCompound(p.getUUID().toString()).getLong("Bed131")).getZ())
                        .thenComparingInt(p->BlockPos.of(cohort.getCompound(p.getUUID().toString()).getLong("Bed131")).getX())).orElse(null);
    }
    private static void rise(GoatmanChild c,BlockPos b,int bunk){
        if(c.isSleeping())c.stopSleeping();c.setCrawling(false);c.setPose(Pose.STANDING);c.heave(false);
        var head=GoatmanWoods.bunkHead(bunk);GoatmanVignette.script(c,List.of(new Vec3(bunk%2==0?-3.6:4.4,1,head.getZ()+.5)),"bed_front131");
    }
    private static void crawl(GoatmanChild c,BlockPos b,BlockPos target,CompoundTag r){
        // Which child it is wearing, kept for the next bed and the haunting; going to the next bed it is that child again.
        if(!c.monsterAppearance()){r.putInt("BedDisguise199",c.disguise());r.putInt("BedFace199",c.visibleSkin());}
        else c.disguise(r.contains("BedDisguise199")?r.getInt("BedDisguise199"):-1);
        if(c.isSleeping())c.stopSleeping();c.setPose(Pose.SWIMMING);c.setCrawling(true);c.heave(false);c.setInvisible(false);
        var at=c.position().subtract(b.getX(),b.getY(),b.getZ());boolean west=target.getX()<b.getX();double x=west?-3:4,z=target.getZ()-b.getZ()+.5;
        var path=new ArrayList<Vec3>();double current=at.x<.5?-3:4;path.add(new Vec3(current,1,at.z));
        if(current!=x){double around=Math.abs(at.z+71.5)+Math.abs(z+71.5)<Math.abs(at.z+59.5)+Math.abs(z+59.5)?-71.5:-59.5;path.add(new Vec3(current,1,around));path.add(new Vec3(x,1,around));}
        path.add(new Vec3(x,1,z));path.add(new Vec3(west?-3.65:4.65,1,z));GoatmanVignette.script(c,path,"bed_crawl131");
    }
    private static void morning(ServerLevel l,BlockPos b,CompoundTag r,GoatmanChild c,List<ServerPlayer> readers){
        var data=c.getPersistentData();var leader=readers.stream().filter(p->r.getCompound("Cohort").getCompound(p.getUUID().toString()).getBoolean("Resolved")).min(Comparator.comparing(p->p.getUUID().toString())).orElse(null);if(leader==null)return;
        var relative=leader.position().subtract(b.getX(),b.getY(),b.getZ());if(GoatmanWoods.indoors(relative))return;
        if(!data.getBoolean("MorningWalk131")){
            c.setInvisible(false);if(c.isSleeping())c.stopSleeping();c.heave(false);c.cower(false);c.fear(0);c.fearStage(0);
            var here=c.position().subtract(b.getX(),b.getY(),b.getZ());var path=new ArrayList<Vec3>();path.add(new Vec3(here.x<.5?-3:4,1,here.z));path.add(new Vec3(here.x<.5?-3:4,1,-57.5));path.add(GoatmanVignette.ENTRY);for(int k=GoatmanVignette.UP.length-1;k>=0;k--)path.add(GoatmanVignette.UP[k]);path.add(GoatmanVignette.YARD);
            GoatmanVignette.script(c,path,"morning_exit131");data.putBoolean("MorningWalk131",true);
        }
        if(!data.getBoolean("MorningTrail131")){if(GoatmanVignette.advance(c,b,.12,l,true))data.putBoolean("MorningTrail131",true);return;}
        int index=c.getPersistentData().getInt(GoatmanVignette.INDEX);double progress=GoatmanWoods.project(relative).progress();
        if(GoatmanWoods.clearing(relative)){c.pose(false);return;}
        double behind=2+Math.floorMod(index,7)*.65,current=GoatmanWoods.project(c.position().subtract(b.getX(),b.getY(),b.getZ())).progress();Vec3 target=GoatmanWoods.path(Math.min(GoatmanWoods.CLEARING+4,Math.max(current-.7,progress+behind)));
        if(c.position().distanceToSqr(target.add(b.getX(),b.getY(),b.getZ()))>1){GoatmanVignette.script(c,List.of(target),"morning_trail131");GoatmanVignette.advance(c,b,.12,l,true);}else c.pose(false);
        if(index==checker(r)){
            int line=progress<8?3:progress<24?2:progress<40?1:0;
            if(line>r.getInt("MorningLine131")){String text=switch(line){case 1->readers.size()>1?"A cousin: I'm only waiting because you all walk so slow.":"A cousin: I'm only waiting because you walk so slow.";case 2->"A cousin: You heard it too, right? At the window? ... Never mind.";default->"A cousin: There. See? Told you we'd get out.";};GoatmanVignette.say(readers,text);r.putInt("MorningLine131",line);}
        }
    }
    private static int checker(CompoundTag r){for(int i=0;i<GoatmanVignette.count(r);i++)if(i!=r.getInt("Wrong")&&i!=r.getInt("Runner"))return i;return 0;}
    private static void face(GoatmanChild c,Vec3 target){var d=target.subtract(c.position());float yaw=(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90;c.setYRot(yaw);c.yBodyRot=yaw;c.setYHeadRot(yaw);}
}
