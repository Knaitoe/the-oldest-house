package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;

/** One saved cousin body circles the house. Only its presentation changes, between unseen sightings. */
public final class GoatmanStalking {
    private static final Vec3[] CIRCUIT={
        new Vec3(-5.5,0,-53.5),new Vec3(-9.25,0,-54.5),new Vec3(-9.25,0,-58.5),
        new Vec3(-9.25,0,-66.5),new Vec3(-9.25,0,-72.5),new Vec3(-9.25,0,-78.5),
        new Vec3(.5,0,-78.5),new Vec3(10.25,0,-78.5),new Vec3(10.25,0,-74.5),
        new Vec3(10.25,0,-66.5),new Vec3(10.25,0,-58.5),new Vec3(10.25,0,-53.5),
        new Vec3(5.5,0,-53.5),new Vec3(.5,0,-51.5),new Vec3(.5,.5,-52.6),new Vec3(.5,1,-53.8)
    };
    private static final Vec3[] TO_WINDOW={new Vec3(5.5,0,-53.5),new Vec3(10.25,0,-53.5),new Vec3(10.25,0,-66.5),new Vec3(10.25,0,-74.5)};
    public static final int[] DEMAND_TICKS={0,160,330,500,665,830,1000};
    public static final int ASSAULT_END=1080;
    private GoatmanStalking(){}
    private static Vec3 abs(BlockPos b,Vec3 p){return p.add(b.getX(),b.getY(),b.getZ());}
    /** The deck is consumed only when an appearance actually changes; exhaustion never recycles a child. */
    public static int nextChild(CompoundTag r){
        int used=r.getInt("StalkUsed0465"),cursor=r.getInt("StalkCursor0465");int[] deck=r.getIntArray("StalkDeck0465");
        while(cursor<deck.length){int skin=deck[cursor++];if(skin>=0&&skin<9&&(used&(1<<skin))==0){r.putInt("StalkCursor0465",cursor);r.putInt("StalkUsed0465",used|(1<<skin));return skin;}}
        r.putInt("StalkCursor0465",cursor);return -2;
    }
    public static boolean obscured(ServerLevel l,GoatmanChild c){
        for(var p:l.players()){
            var to=c.getEyePosition().subtract(p.getEyePosition());double d=to.length();
            if(d<72&&d>.01&&p.getLookAngle().dot(to.scale(1/d))>-.15&&p.hasLineOfSight(c))return false;
        }
        return true;
    }
    private static void change(ServerLevel l,CompoundTag r,GoatmanChild c){
        if(!r.getBoolean("StalkChange0465")||!obscured(l,c))return;
        int turn=r.getInt("StalkTurn0465"),skin=turn%2==0?-2:nextChild(r);
        c.disguise(skin);r.putInt("StalkTurn0465",turn+1);r.putBoolean("StalkChange0465",false);
    }
    public static void step(ServerLevel l,BlockPos b,CompoundTag r,GoatmanChild c,int clock){
        if(!r.getBoolean("StalkStarted0465")){
            r.putBoolean("StalkStarted0465",true);r.putInt("StalkUsed0465",1<<c.skin());r.putBoolean("StalkChange0465",true);
            c.disguise(c.skin());c.getPersistentData().remove("Route");c.pose(false);c.cower(false);c.heave(false);
        }
        change(l,r,c);
        boolean window=r.getBoolean("AssaultDone0465")&&clock>=1900;
        if(!window&&r.getInt("StalkLeg0465")>=CIRCUIT.length){
            c.pose(false);face(c,abs(b,new Vec3(.5,2,-55)));
            if(clock>=600&&!r.contains("AssaultAt0465"))r.putInt("AssaultAt0465",clock+1);
            return;
        }
        int leg=r.getInt(window?"StalkWindowLeg0465":"StalkLeg0465");Vec3[] route=window?TO_WINDOW:CIRCUIT;
        if(leg>=route.length){c.pose(false);face(c,Vec3.atCenterOf(b.offset(GoatmanWoods.WINDOW)));return;}
        if(clock<r.getInt("StalkPause0465")){c.pose(false);return;}
        Vec3 target=abs(b,route[leg]),delta=target.subtract(c.position());double length=delta.length();
        Vec3 movement=length<=.25?delta:delta.scale(.25/length);var before=c.position();
        c.setOnGround(true);c.move(net.minecraft.world.entity.MoverType.SELF,movement);c.setDeltaMovement(Vec3.ZERO);
        float yaw=length>.01?(float)(Math.atan2(delta.z,delta.x)*180/Math.PI)-90:c.getYRot();
        c.setYRot(yaw);c.setYHeadRot(yaw);c.yBodyRot=yaw;c.animate(c.position().distanceTo(before),false,false);
        if(c.position().distanceToSqr(target)>.012)return;
        r.putInt(window?"StalkWindowLeg0465":"StalkLeg0465",leg+1);
        if(window&&leg==route.length-1){impact(l,b,r,c,GoatmanWoods.WINDOW,2);return;}
        if(!window){
            BlockPos hit=switch(leg){case 2->new BlockPos(-8,2,-59);case 3->new BlockPos(-8,2,-67);case 4->new BlockPos(-8,2,-73);case 6->new BlockPos(0,2,-77);case 8->GoatmanWoods.WINDOW;case 9->new BlockPos(8,2,-67);case 10->new BlockPos(8,2,-59);default->null;};
            if(hit!=null){face(c,Vec3.atCenterOf(b.offset(hit)));impact(l,b,r,c,hit,leg%3==0?2:1);r.putInt("StalkPause0465",clock+20+leg%3*5);r.putBoolean("StalkChange0465",true);}
        }
    }
    private static void face(GoatmanChild c,Vec3 p){var d=p.subtract(c.position());float yaw=(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90;c.setYRot(yaw);c.setYHeadRot(yaw);c.yBodyRot=yaw;}
    public static void assault(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> players,int clock){
        if(!r.contains("AssaultAt0465"))return;int elapsed=clock-r.getInt("AssaultAt0465");
        if(elapsed>=ASSAULT_END){r.putBoolean("AssaultDone0465",true);return;}
        int stage=0;while(stage+1<DEMAND_TICKS.length&&elapsed>=DEMAND_TICKS[stage+1])stage++;
        int beat=elapsed-DEMAND_TICKS[stage];
        if(beat==0){r.putInt("Demand",101+stage);r.putInt("DemandAt",clock);r.putInt("DemandStage0465",stage+1);}
        boolean hit=switch(stage){case 0->beat==0||beat==14;case 1->beat==0||beat==22||beat==44;case 2->beat==12;case 3->beat==0||beat==10||beat==20;case 4->beat==30;case 5->beat==12;default->beat<80&&beat%6==0;};
        if(!hit)return;
        var it=l.getEntitiesOfClass(GoatmanChild.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),c->c.getTags().contains(GoatmanVignette.ACTOR)&&c.getPersistentData().getInt(GoatmanVignette.INDEX)==r.getInt("Wrong")&&c.getPersistentData().hasUUID(GoatmanVignette.ROUND)&&c.getPersistentData().getUUID(GoatmanVignette.ROUND).equals(r.getUUID("Id"))).stream().findFirst().orElse(null);
        if(it==null||it.position().distanceToSqr(abs(b,CIRCUIT[CIRCUIT.length-1]))>.6)return;
        impact(l,b,r,it,GoatmanWoods.DOOR,stage==0?1:stage<3?2:3);
        if(stage==6)r.putInt("FinalBlows0465",r.getInt("FinalBlows0465")+1);
    }
    public static void impact(ServerLevel l,BlockPos b,CompoundTag r,GoatmanChild c,BlockPos relative,int force){
        var at=b.offset(relative);var state=l.getBlockState(at);var center=Vec3.atCenterOf(at);boolean door=relative.equals(GoatmanWoods.DOOR);
        var toward=c.position().subtract(center);double x=center.x,z=center.z;
        if(Math.abs(toward.x)>Math.abs(toward.z))x+=Math.signum(toward.x)*.49;else z+=Math.signum(toward.z)*.49;
        double y=center.y+(door?.55:0);
        l.playSound(null,x,y,z,force==1?LiteraryRegistry.CABIN_KNOCK.get():force==2?GoatmanRegistry.CLAW.get():GoatmanRegistry.HAMMER.get(),SoundSource.BLOCKS,.65F+force*.22F,force==1?1.08F:.9F);
        if(force>=2)l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,state),x,y,z,force==3?12:5,.22,.28,.035,.012);
        if(force==3){l.sendParticles(ParticleTypes.POOF,x,y,z,3,.18,.25,.05,.008);l.playSound(null,x,y,z,SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.24F,.65F);}
        if(door&&l.getBlockEntity(at) instanceof TrailerDoorBlockEntity frame)frame.impact(force);
        c.swing(InteractionHand.MAIN_HAND);r.putLong("LastImpact0465",l.getGameTime());r.putLong("LastImpactPos0465",at.asLong());
    }
}
