package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/**
 * The Minotaur out of its cell (0.4.84). It has killed the one who opened it. It waits by the bars until they wake on the
 * other side of them, looks at them a while, then lumbers off up the corridor and into the dark of the stairs, where it
 * stays. It harms nobody once it is out; it walks, stops, and walks again, and moves without being seen only when stuck.
 * <p>At most {@link #KEPT} roam at once: an older one goes, unseen, when a newer one gets out.
 */
public final class MinotaurLoose {
    public static final String LIST="LooseCreatures0484";
    public static final int KEPT=3;
    /** How long it waits for the one it killed to wake, how long it looks at them, and its walking pace. */
    public static final int WAIT_FOR=1200,LOOK=60;
    public static final double PACE=.11;
    /** How far up the stairs it goes, at most, before it settles; and how long it rests between walks. */
    public static final int CLIMB=360,REST=160;
    private MinotaurLoose(){}

    /** Remember a newly loose one; the oldest beyond the limit is told to go. */
    public static void register(MinecraftServer server,UUID id){
        CompoundTag world=FinaleProgress.world(server);ListTag list=world.getList(LIST,Tag.TAG_INT_ARRAY);
        list.add(NbtUtils.createUUID(id));world.put(LIST,list);LabyrinthData.get(server).setState(FinaleProgress.STATE,world);
    }
    static boolean kept(MinecraftServer server,UUID id){
        ListTag list=FinaleProgress.world(server).getList(LIST,Tag.TAG_INT_ARRAY);
        // Only the newest few are kept; anything older or forgotten goes.
        for(int i=Math.max(0,list.size()-KEPT);i<list.size();i++)if(NbtUtils.loadUUID(list.get(i)).equals(id))return true;
        return false;
    }
    private static void forget(MinecraftServer server,UUID id){
        CompoundTag world=FinaleProgress.world(server);ListTag list=world.getList(LIST,Tag.TAG_INT_ARRAY),kept=new ListTag();
        for(Tag t:list)if(!NbtUtils.loadUUID(t).equals(id))kept.add(t);
        world.put(LIST,kept);LabyrinthData.get(server).setState(FinaleProgress.STATE,world);
    }

    public static void tick(MinotaurEntity m,ServerLevel level){
        MinecraftServer server=level.getServer();BlockPos origin=HouseSavedData.get(server).houseOrigin();
        if(origin==null||!level.dimension().equals(HouseDimensions.INTERIOR)){m.getNavigation().stop();return;}
        if(!kept(server,m.getUUID())){if(!m.observed()){forget(server,m.getUUID());m.discard();}return;}
        long now=level.getGameTime();BlockPos b=FinaleArchitecture.base(origin);
        switch(m.looseStage()){
            case 0->{
                // By the bars, facing in, until the one it killed is standing on the other side of them.
                Vec3 bars=Vec3.atBottomCenterOf(b.offset(0,FinaleArchitecture.ARENA,56));walk(m,bars,PACE);
                ServerPlayer killed=m.owner()==null?null:server.getPlayerList().getPlayer(m.owner());
                boolean awake=killed!=null&&killed.isAlive()&&killed.serverLevel()==level&&FinaleArchitecture.inCell(origin,killed.position());
                if(awake){m.getLookControl().setLookAt(killed,20,20);face(m,killed.position());}
                else face(m,Vec3.atBottomCenterOf(FinaleArchitecture.cell(origin)));
                CompoundTag own=m.getPersistentData();
                if(awake&&!own.contains("SeenAwake0484")){own.putLong("SeenAwake0484",now);m.playSound(SoundEvent.createVariableRangeEvent(Growl.Kind.NEAR.sound()),.7F,.6F);}
                if(awake&&now-own.getLong("SeenAwake0484")>=LOOK||now-m.looseSince()>=WAIT_FOR){
                    m.bellow(level,1.6F);m.looseStage(1,now);m.looseStep(-1);
                    if(killed!=null&&awake)FinaleController.words(killed,FinaleArchitecture.cell(origin).above(2),"It turns away from the bars, and goes off into the dark.");
                }
            }
            case 1->{
                // Out through the arena's mouth, down the corridor to the foot of the stairs.
                List<BlockPos> route=FinaleArchitecture.staircaseRoute(origin);BlockPos foot=route.get(route.size()-1);
                Vec3[] way={Vec3.atBottomCenterOf(b.offset(0,FinaleArchitecture.ARENA,31)),Vec3.atBottomCenterOf(b.offset(0,FinaleArchitecture.ARENA,27)),Vec3.atBottomCenterOf(new BlockPos(b.getX(),foot.getY(),foot.getZ())),Vec3.atBottomCenterOf(foot)};
                int leg=Math.max(0,m.looseStep());
                if(leg>=way.length){m.looseStage(2,now);m.looseStep(route.size()-1);return;}
                if(walk(m,way[leg],PACE))m.looseStep(leg+1);
            }
            default->{
                // Up the stairs a way, then still for a while; then a little further, or back down. It never leaves them.
                List<BlockPos> route=FinaleArchitecture.staircaseRoute(origin);int step=Math.max(0,Math.min(route.size()-1,m.looseStep()));
                CompoundTag own=m.getPersistentData();
                if(!own.contains("Goal0484"))own.putInt("Goal0484",Math.max(0,route.size()-1-40-m.getRandom().nextInt(CLIMB)));
                if(now<own.getLong("RestUntil0484")){m.getNavigation().stop();return;}
                int goal=own.getInt("Goal0484");
                if(step==goal){own.putLong("RestUntil0484",now+REST+m.getRandom().nextInt(REST));
                    own.putInt("Goal0484",Math.max(0,Math.min(route.size()-1,route.size()-1-20-m.getRandom().nextInt(CLIMB))));
                    if(m.getRandom().nextInt(3)==0)m.playSound(SoundEvent.createVariableRangeEvent(Growl.Kind.FAR.sound()),.8F,.7F);return;}
                int next=step+(goal<step?-1:1);
                if(walk(m,Vec3.atBottomCenterOf(route.get(next)),PACE))m.looseStep(next);
            }
        }
    }
    /** One lumbering step toward a point; true once there. A body wedged for three seconds moves on only where nobody sees. */
    private static boolean walk(MinotaurEntity m,Vec3 to,double pace){
        Vec3 delta=to.subtract(m.position()).multiply(1,0,1);double length=delta.length();
        if(length<.35)return true;
        face(m,to);Vec3 before=m.position();
        m.move(MoverType.SELF,delta.scale(Math.min(pace,length)/length));
        CompoundTag own=m.getPersistentData();
        if(m.position().distanceToSqr(before)<1e-4){int stuck=own.getInt("Stuck0484")+1;own.putInt("Stuck0484",stuck);
            if(stuck>=60&&!m.observed()){m.moveTo(to.x,to.y,to.z,m.getYRot(),0);own.putInt("Stuck0484",0);return true;}}
        else own.putInt("Stuck0484",0);
        return false;
    }
    private static void face(MinotaurEntity m,Vec3 at){
        Vec3 d=at.subtract(m.position());if(d.horizontalDistanceSqr()<1e-4)return;
        float yaw=(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90;m.setYRot(yaw);m.setYHeadRot(yaw);m.yBodyRot=yaw;
    }
}
