package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.347, the storm field (YardStorm), with readers in it: from the rise the farmhouse, the elevator and the power line, lost again from
 * an aisle in the corn; a reader cutting north across the rows, slowed, leaving a track of bent stalks; the yard light swung from the rise
 * to land over the corn; then the rain, the windsock lifting; footsteps up the ditch heard from inside the shed, by that reader alone; the
 * storm, the ditch filling and carrying its wader back toward the rise, and the light no longer reaching; and the field as it was.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class YardStormTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="yard_storm") public static void stormDone(ServerLevel l){close();}
    private static void close(){if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){active=new LiteraryTests.Fixture(h);var a=LabyrinthPlaces.slotBounds(active.origin,YardStorm.PLACE);active.chunks.hold(active.out,new AABB(a.minX(),a.minY(),a.minZ(),a.maxX()+1,a.maxY()+1,a.maxZ()+1));return active;}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);p.setNoGravity(true);return p;}
    private static void at(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z,float yaw,float pitch){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,yaw,pitch);p.setYRot(yaw);p.setXRot(pitch);p.setYHeadRot(yaw);p.setDeltaMovement(Vec3.ZERO);p.hasChangedDimension();}
    private static void clock(LiteraryTests.Fixture f,ServerLevel l,long c){var s=YardStorm.state(l);s.putLong("Clock",c);f.data().setStateEntry(YardStorm.STATE,YardStorm.PLACE.id(),s);}
    private static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(Vec3.atLowerCornerOf(b));}
    /** What a sight line from {@code eye} toward {@code target} (both local) meets first: null if nothing before the target's own cell. */
    private static BlockPos blocked(ServerLevel l,BlockPos b,Vec3 eye,BlockPos target){
        var from=Vec3.atLowerCornerOf(b).add(eye);var to=Vec3.atCenterOf(b.offset(target));
        var hit=l.clip(new ClipContext(from,to,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty()));
        if(hit.getType()!=HitResult.Type.BLOCK)return null;var cell=hit.getBlockPos().subtract(b);return cell.distManhattan(target)<=2?null:cell;
    }
    private static boolean stalk(ServerLevel l,BlockPos b,int x,int y,int z,int stage){var st=l.getBlockState(b.offset(x,y,z));return YardBlock.is(st,YardBlock.Kind.STALK)&&st.getValue(YardBlock.STAGE)==stage;}
    private static int follows(ServerPlayer p){int n=0;for(var s:NativeTestPlayers.sounds(p))if(s.getSound().value()==PuzzleRegistry.YARD_FOLLOW.get())n++;return n;}

    @GameTest(template="empty",batch="yard_storm",timeoutTicks=2400)
    public static void theFieldIsReadFromTheRiseCrossedThroughTheCornAndTakenByTheStorm(GameTestHelper h){
        var f=fixture(h);var l=f.out;var b=f.base(YardStorm.PLACE);
        var watcher=player(h,f,"storm_rise");var walker=player(h,f,"storm_corn");var shed=player(h,f,"storm_shed");var wader=player(h,f,"storm_ditch");
        var farmhouse=new BlockPos(0,5,-62);var elevator=new BlockPos(15,10,-61);var pole=new BlockPos(18,7,-34);
        var topEye=new Vec3(-10.5,YardStorm.TOP_Y+1.62,-7.5);var aisleEye=new Vec3(.5,1.62,-25.5);
        boolean[] walking={false};boolean[] carry={false};double[] start={0,0};int[] heard={0,0};
        h.onEachTick(()->{
            if(walking[0]){walker.setDeltaMovement(0,0,-.13);walker.move(MoverType.SELF,walker.getDeltaMovement());}
            if(carry[0]){var v=wader.getDeltaMovement();wader.move(MoverType.SELF,new Vec3(v.x,wader.onGround()?0:-.08,v.z));}
            heard[0]+=follows(shed);heard[1]+=follows(watcher);
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the field is resident"))
            .thenExecute(()->{
                YardStorm.build(l,b);LabyrinthBuilder.registerDoors(f.data(),YardStorm.PLACE,b);
                for(var t:List.of(farmhouse,elevator,pole))h.assertTrue(blocked(l,b,topEye,t)==null,"from the rise, "+t+" is in sight: "+blocked(l,b,topEye,t));
                for(var t:List.of(farmhouse,elevator,pole))h.assertTrue(blocked(l,b,aisleEye,t)!=null&&stalk(l,b,blocked(l,b,aisleEye,t).getX(),blocked(l,b,aisleEye,t).getY(),blocked(l,b,aisleEye,t).getZ(),blocked(l,b,aisleEye,t).getY()),"at eye level in the corn, "+t+" is lost behind the stalks");
                h.assertTrue(l.getBlockState(b.offset(-18,-1,-30)).isAir(),"the ditch boot-deep, easy to follow");
                h.assertTrue(l.getBlockState(b.offset(0,0,-62)).getBlock() instanceof net.minecraft.world.level.block.DoorBlock,"the farmhouse's back door is the way on: "+l.getBlockState(b.offset(0,0,-62)));
                at(watcher,l,b,-10.5,YardStorm.TOP_Y,-7.5,0,0);at(walker,l,b,.5,0,-15.5,180,0);at(shed,l,b,-3.5,0,-8.5,180,0);at(wader,l,b,-17.5,-1.5,-30.5,180,0);
                start[0]=walker.getZ();walking[0]=true;
            })
            .thenIdle(80)
            .thenExecute(()->{
                walking[0]=false;double gone=start[0]-walker.getZ();
                h.assertTrue(gone>6,"cutting across the rows is quick: "+gone);
                h.assertTrue(stalk(l,b,0,0,-16,3)&&stalk(l,b,0,0,-18,3)&&l.getBlockState(b.offset(0,1,-16)).isAir(),"and leaves a track of bent stalks: "+l.getBlockState(b.offset(0,0,-16)));
                h.assertTrue(stalk(l,b,-3,0,-16,0)&&stalk(l,b,-3,2,-16,2),"the rows either side still standing");
                // The yard light, swung from the rise to where the watcher is looking: down into the corn ahead of the walker.
                var target=Vec3.atLowerCornerOf(b).add(.5,3,-24.5);var d=target.subtract(watcher.getEyePosition());
                at(watcher,l,b,-10.5,YardStorm.TOP_Y,-7.5,(float)(Math.toDegrees(Math.atan2(d.z,d.x))-90),(float)-Math.toDegrees(Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z))));
                var spot=YardStorm.signal(l,b,watcher);
                h.assertTrue(spot!=null&&Math.abs(spot.getX())<=3&&Math.abs(spot.getZ()+24)<=4&&l.getBlockState(b.offset(spot)).is(Blocks.LIGHT),"the light lands over the corn where the watcher looks: "+spot);
                clock(f,l,YardStorm.RAIN-1);
            })
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(YardStorm.WINDSOCK)).getValue(YardBlock.STAGE)==1,"the rain comes, the windsock lifting"))
            .thenExecute(()->{at(shed,l,b,7.5,0,-35.5,180,0);heard[0]=0;heard[1]=0;})
            .thenWaitUntil(()->h.assertTrue(heard[0]>=3,"from inside the shed, someone splashing up the ditch: "+heard[0]))
            .thenExecute(()->{
                h.assertTrue(heard[1]==0,"heard by the reader in the shed alone");
                h.assertTrue(l.getBlockState(b.offset(-18,-1,-30)).isAir(),"the ditch still low in the rain");
                clock(f,l,YardStorm.STORM-1);start[1]=wader.getZ();carry[0]=true;
            })
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(-18,-1,-30)).is(Blocks.WATER)&&l.getBlockState(b.offset(YardStorm.WINDSOCK)).getValue(YardBlock.STAGE)==2,"the storm: the windsock full, the ditch filling"))
            .thenWaitUntil(()->h.assertTrue(wader.getZ()-start[1]>2.5,"the ditch running back toward the rise, carrying its wader: "+rel(wader,b)))
            .thenExecute(()->{
                carry[0]=false;
                h.assertTrue(wader.getHealth()==wader.getMaxHealth()&&wader.getAirSupply()==wader.getMaxAirSupply(),"unhurt, breath kept");
                h.assertTrue(l.getBlockState(b.offset(-16,0,-30)).isAir()&&!l.getBlockState(b.offset(-16,-1,-30)).is(Blocks.WATER),"the water stays in its banks");
                h.assertTrue(YardStorm.signal(l,b,watcher)==null,"the rain has taken the view: the light reaches no further");
                YardStorm.reset(l,b,YardStorm.state(l));
                h.assertTrue(stalk(l,b,0,0,-16,0)&&stalk(l,b,0,2,-16,2)&&stalk(l,b,0,0,-18,0),"the corn stands again");
                h.assertTrue(!l.getBlockState(b.offset(-18,-1,-30)).is(Blocks.WATER)&&l.getBlockState(b.offset(YardStorm.WINDSOCK)).getValue(YardBlock.STAGE)==0,"the ditch low, the windsock slack");
                h.assertTrue(WitnessAccount.count(f.data(),walker.getUUID())==0,"no story credit");
            })
            .thenSucceed();
    }
}
