package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.736, The list, stand-ins 2 (docs/THE_LIST_0_4_736.md): the deputy's patrol car on the caliche roads, where the deputy on foot stood
 * for the town's whole search. On night three and night four, while the hunt is on, a patrol car (TownCar.CRUISER, a two-tone sedan with a
 * light bar) drives one loop at a walking man's pace and a half: east along South Seventh from the school's lot, down the highway's
 * southbound side to South Ninth, west along Ninth to where the caliche ends, back east on its other side, up the highway and west along
 * Seventh again. Its headlights throw a real light ahead of it.
 * <p>
 * If it sees the one who went in (near enough, ahead of it, nothing in the way) it stops, puts its spotlight on them (a real light where
 * they stand, which is standing in light for the hunt) and calls the deputy over the radio: he leaves his round and goes there. It holds
 * the spotlight {@link #HOLD} ticks, following them while it can see them, then drives on and does not stop for them again for
 * {@link #AGAIN}. Nobody else is spotted. Everyone in town sees it; it is a puppet as the town's other cars are, never saved, and gone
 * two seconds after it is not told where it is.
 */
public final class TheListCruiser{
    private TheListCruiser(){}
    /** Its loop, in the town's coordinates: the lanes of Seventh, the highway and Ninth, on the side a car keeps to. */
    static final Vec3[] LOOP={new Vec3(-88,0,-69.5),new Vec3(19.5,0,-69.5),new Vec3(19.5,0,-29.5),new Vec3(-57,0,-29.5),
        new Vec3(-57,0,-26.5),new Vec3(22.5,0,-26.5),new Vec3(22.5,0,-72.5),new Vec3(-88,0,-72.5)};
    /** Blocks a tick; how far it sees and how wide; how far ahead its headlights fall; how long it holds the spotlight and waits after. */
    public static final double SPEED=.25,SEES=18,FACING=40,AHEAD=5,TURN=2.5;
    public static final int HOLD=100,AGAIN=300,RADIO=400;

    static final class Patrol{@Nullable UUID car,on;double along;int hold;long nextSpot=Long.MIN_VALUE;@Nullable BlockPos beam,spot;}
    static final Map<Long,Patrol> PATROLS=new HashMap<>();
    public static void clearAll(){PATROLS.clear();}

    /** The loop's length. */
    static double length(){double n=0;for(int i=0;i<LOOP.length;i++)n+=LOOP[i].distanceTo(LOOP[(i+1)%LOOP.length]);return n;}
    /** Where on the loop it is that far along it, and the way it points (turning through each corner over the last blocks). */
    static Vec3 at(double along){
        along=Mth.positiveModulo(along,length());
        for(int i=0;i<LOOP.length;i++){
            var a=LOOP[i];var b=LOOP[(i+1)%LOOP.length];double d=a.distanceTo(b);
            if(along<=d)return a.add(b.subtract(a).scale(along/d));
            along-=d;
        }
        return LOOP[0];
    }
    static float yaw(double along){
        along=Mth.positiveModulo(along,length());
        for(int i=0;i<LOOP.length;i++){
            var a=LOOP[i];var b=LOOP[(i+1)%LOOP.length];var c=LOOP[(i+2)%LOOP.length];double d=a.distanceTo(b);
            if(along<=d){
                float here=heading(a,b),next=heading(b,c);
                double left=d-along;
                return left>=TURN?here:Mth.rotLerp((float)(1-left/TURN)*.5F,here,next);
            }
            along-=d;
        }
        return heading(LOOP[0],LOOP[1]);
    }
    static float heading(Vec3 from,Vec3 to){var d=to.subtract(from);return (float)Math.toDegrees(Math.atan2(-d.x,d.z));}

    /** One tick of night three's or night four's hunt in a town. */
    static void tick(ServerLevel l,TheListNight.Night n,List<ServerPlayer> here){
        var b=n.base;var p=PATROLS.computeIfAbsent(b.asLong(),k->new Patrol());
        var slasher=TheListNight.slasher(l,n);
        var car=car(l,b,p);
        var now=l.getGameTime();
        boolean sees=car!=null&&slasher!=null&&sees(l,car,slasher);
        if(p.hold>0){
            p.hold--;
            if(sees)spot(l,p,slasher);
            if(p.hold==0){light(l,p.spot);p.spot=null;p.on=null;p.nextSpot=now+AGAIN;}
        }else if(sees&&now>=p.nextSpot){
            p.hold=HOLD;spot(l,p,slasher);
            l.playSound(null,car.getX(),car.getY()+1,car.getZ(),SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.NEUTRAL,.6F,.5F);
            TheList.cue(slasher,TheListTexts.CRUISER_SPOT);
            for(var q:here)if(q!=slasher&&LiteraryVignettes.participant(q)&&q.distanceTo(car)<16)TheList.cue(q,TheListTexts.CRUISER_RADIO);
            TheListDeputy.radio(l,b,slasher.position(),RADIO);
        }else p.along=Mth.positiveModulo(p.along+SPEED,length());
        place(l,b,p);
    }
    /** Whether the car has the one who went in in sight: near enough, ahead of it, nothing in the way. */
    static boolean sees(ServerLevel l,TownCar car,ServerPlayer p){
        if(!p.isAlive()||p.isSpectator()||car.distanceTo(p)>SEES)return false;
        var d=p.position().subtract(car.position());
        double yaw=Math.toDegrees(Math.atan2(-d.x,d.z));
        if(Math.abs(Mth.wrapDegrees(yaw-car.getYRot()))>FACING)return false;
        var from=car.position().add(0,1.5,0);
        return l.clip(new ClipContext(from,p.getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,car)).getType()==HitResult.Type.MISS;
    }
    static void place(ServerLevel l,BlockPos b,Patrol p){
        var at=Vec3.atLowerCornerOf(b).add(at(p.along));float yaw=yaw(p.along);
        if(p.car!=null&&l.getEntity(p.car) instanceof TownCar c&&c.isAlive())c.place(at,yaw);
        else{var c=CalderTraffic.make(l,TownCar.CRUISER,at,yaw);p.car=c==null?null:c.getUUID();}
        // The headlights: a light on the road ahead of its nose, moved as it moves.
        var look=Vec3.directionFromRotation(0,yaw);
        var cell=BlockPos.containing(at.x+look.x*AHEAD,at.y+1,at.z+look.z*AHEAD);
        if(!cell.equals(p.beam)){light(l,p.beam);p.beam=lit(l,cell)?cell:null;}
    }
    /** The spotlight on them: a light where they stand, moved with them. */
    static void spot(ServerLevel l,Patrol p,ServerPlayer q){
        p.on=q.getUUID();
        var cell=BlockPos.containing(q.getEyePosition());
        if(cell.equals(p.spot))return;
        light(l,p.spot);p.spot=lit(l,cell)?cell:lit(l,cell.above())?cell.above():null;
    }
    static boolean lit(ServerLevel l,BlockPos cell){
        if(!l.isLoaded(cell)||!l.getBlockState(cell).isAir())return false;
        l.setBlock(cell,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,14),Block.UPDATE_CLIENTS);return true;
    }
    static void light(ServerLevel l,@Nullable BlockPos cell){
        if(cell!=null&&l.isLoaded(cell)&&l.getBlockState(cell).is(Blocks.LIGHT))l.setBlock(cell,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
    }
    static @Nullable TownCar car(ServerLevel l,BlockPos b,Patrol p){return p.car!=null&&l.getEntity(p.car) instanceof TownCar c&&c.isAlive()?c:null;}

    /** The night over: the car gone, its lights out, its way forgotten. */
    static void reset(ServerLevel l,BlockPos b){
        var p=PATROLS.remove(b.asLong());if(p==null)return;
        light(l,p.beam);light(l,p.spot);
        if(car(l,b,p) instanceof TownCar c)c.discard();
    }
    /** For checks: the car, whether it is holding its spotlight on someone, and where the spotlight is. */
    public static @Nullable TownCar car(ServerLevel l,BlockPos b){var p=PATROLS.get(b.asLong());return p==null?null:car(l,b,p);}
    public static boolean holding(BlockPos b){var p=PATROLS.get(b.asLong());return p!=null&&p.hold>0;}
    /** Whether a patrol car is holding its spotlight on them: the hunt reads that as standing in light, whatever the light has reached. */
    public static boolean spotting(ServerPlayer q){for(var p:PATROLS.values())if(p.hold>0&&q.getUUID().equals(p.on))return true;return false;}
    public static @Nullable BlockPos spotAt(BlockPos b){var p=PATROLS.get(b.asLong());return p==null?null:p.spot;}
    /** For a check: put the car this far along its loop. */
    public static void moveTo(BlockPos b,double along){var p=PATROLS.get(b.asLong());if(p!=null)p.along=along;}
}
