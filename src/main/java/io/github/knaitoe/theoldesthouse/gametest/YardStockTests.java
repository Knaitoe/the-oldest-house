package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.345, the Stock Gate (YardStock), played with readers in the yard: the herd driven into the lane while a pen gate stands open, the
 * other chute once that one is swung, the herd kept in its pen once both are, and the far gate reached; a reader shoved along the lane
 * by the cattle and another safe on a loading step; the same four cattle throughout.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class YardStockTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="yard_stock") public static void stockDone(ServerLevel l){close();}
    @AfterBatch(batch="yard_stock_push") public static void pushDone(ServerLevel l){close();}
    private static void close(){if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){active=new LiteraryTests.Fixture(h);var a=LabyrinthPlaces.slotBounds(active.origin,YardStock.PLACE);active.chunks.hold(active.out,new AABB(a.minX(),a.minY(),a.minZ(),a.maxX()+1,a.maxY()+1,a.maxZ()+1));return active;}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);p.setNoGravity(true);return p;}
    private static void at(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,180,0);p.setDeltaMovement(Vec3.ZERO);p.hasChangedDimension();}
    private static void use(ServerPlayer p,BlockPos pos){p.serverLevel().getBlockState(pos).useWithoutItem(p.serverLevel(),p,new BlockHitResult(pos.getCenter(),Direction.SOUTH,pos,false));}
    private static List<Cow> herd(ServerLevel l,CompoundTag s){var out=new ArrayList<Cow>();var ids=s.getList("Herd",11);for(int i=0;i<ids.size();i++)if(l.getEntity(net.minecraft.nbt.NbtUtils.loadUUID(ids.get(i))) instanceof Cow c)out.add(c);return out;}
    private static Vec3 rel(net.minecraft.world.entity.Entity e,BlockPos b){return e.position().subtract(Vec3.atLowerCornerOf(b));}
    private static boolean inLane(Cow c,BlockPos b){var r=rel(c,b);return r.x>0&&r.x<1&&r.z<-16.6;}
    private static void ready(LiteraryTests.Fixture f,ServerLevel l){var s=YardStock.state(l);s.putLong("Ready",0);f.data().setStateEntry(YardStock.STATE,YardStock.PLACE.id(),s);}
    private static boolean leaf(ServerLevel l,BlockPos b,BlockPos rel){return YardBlock.is(l.getBlockState(b.offset(rel)),YardBlock.Kind.STOCK_LEAF)&&YardBlock.is(l.getBlockState(b.offset(rel).above(2)),YardBlock.Kind.STOCK_LEAF);}

    @GameTest(template="empty",batch="yard_stock",timeoutTicks=6000)
    public static void theHerdComesIntoTheLaneUntilBothPenGatesAreSwung(GameTestHelper h){
        var f=fixture(h);var l=f.out;var b=f.base(YardStock.PLACE);var p=player(h,f,"stock_reader");List<UUID> ids=new ArrayList<>();var K=YardStock.GATES.get(0);var L=YardStock.GATES.get(1);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the stockyard is resident"))
            .thenExecute(()->{
                YardStock.build(l,b);LabyrinthBuilder.registerDoors(f.data(),YardStock.PLACE,b);var s=YardStock.state(l);
                h.assertTrue(YardStock.position(s,K)==1&&YardStock.position(s,L)==1&&leaf(l,b,K.one())&&leaf(l,b,L.one()),"both near chutes stand open to the pen");
                for(var c:herd(l,s))ids.add(c.getUUID());h.assertTrue(ids.size()==YardStock.COWS,"the four cattle are penned");
                h.assertTrue(l.getBlockState(b.offset(0,3,-7)).is(net.minecraft.world.level.block.Blocks.SPRUCE_PLANKS)&&l.getBlockState(b.offset(0,0,-7)).isAir()&&l.getBlockState(b.offset(0,1,-7)).isAir(),"the lane runs on under the loading platform");
                at(p,l,b,.5,0,-9.5);
            })
            .thenWaitUntil(()->h.assertTrue(herd(l,YardStock.state(l)).stream().anyMatch(c->inLane(c,b)),"the pen gate left open drives the herd into the lane: phase "+YardStock.state(l).getInt("Phase")))
            .thenWaitUntil(()->h.assertTrue(herd(l,YardStock.state(l)).stream().anyMatch(c->rel(c,b).z<-35),"away from the reader, up the lane to the far gate"))
            .thenExecute(()->h.assertTrue(YardStock.state(l).getLongArray("Path").length>0&&YardStock.state(l).getBoolean("Lane"),"the drive's way ran through the lane"))
            .thenWaitUntil(()->h.assertTrue(YardStock.state(l).getInt("Phase")==YardStock.HOME,"with the way behind them clear they go back to the pen"))
            .thenExecute(()->{
                h.assertTrue(herd(l,YardStock.state(l)).stream().allMatch(c->rel(c,b).x<-6),"back in the pen");
                // At K, round the first side chute's bend, the reader sees the pen through the open mouth and swings the gate across it.
                at(p,l,b,-3.5,0,-19.5);use(p,b.offset(K.one()).above());
                h.assertTrue(YardStock.position(YardStock.state(l),K)==2&&leaf(l,b,K.two())&&l.getBlockState(b.offset(K.one())).isAir(),"the gate swings across the mouth from the pen");
                at(p,l,b,.5,0,-9.5);ready(f,l);
            })
            .thenWaitUntil(()->h.assertTrue(YardStock.state(l).getInt("Phase")==YardStock.OUT,"driven again"))
            .thenExecute(()->{var path=YardStock.state(l).getLongArray("Path");boolean viaL=false;for(long v:path)if(BlockPos.of(v).equals(L.junction()))viaL=true;
                h.assertTrue(viaL&&YardStock.state(l).getInt("Pen")==1,"the herd takes the other chute, by the pen's second gate");})
            .thenWaitUntil(()->h.assertTrue(YardStock.state(l).getInt("Phase")==YardStock.HOME,"home again"))
            .thenExecute(()->{at(p,l,b,-3.5,0,-25.5);use(p,b.offset(L.one()));
                h.assertTrue(YardStock.position(YardStock.state(l),L)==2&&leaf(l,b,L.two()),"the second pen gate swung shut");at(p,l,b,.5,0,-9.5);ready(f,l);})
            .thenWaitUntil(()->h.assertTrue(YardStock.state(l).getBoolean("Held"),"with both shut the herd crowds the gate and goes nowhere"))
            .thenIdle(40)
            .thenExecute(()->{h.assertTrue(YardStock.state(l).getInt("Phase")==YardStock.HOME&&herd(l,YardStock.state(l)).stream().allMatch(c->rel(c,b).x<-6),"they stay penned");at(p,l,b,.5,0,-36.7);})
            .thenWaitUntil(()->h.assertTrue(PuzzleRooms.solved(f.data(),YardStock.PLACE),"the far gate reached"))
            .thenExecute(()->{
                var cows=herd(l,YardStock.state(l));h.assertTrue(cows.size()==YardStock.COWS&&cows.stream().allMatch(c->ids.contains(c.getUUID())),"the same four cattle throughout");
                var loaded=LabyrinthData.FACTORY.deserializer().apply(f.data().save(new CompoundTag(),l.registryAccess()),l.registryAccess());
                var s=loaded.stateEntry(YardStock.STATE,YardStock.PLACE.id());
                h.assertTrue(PuzzleRooms.solved(loaded,YardStock.PLACE)&&s.getInt("GateK")==2&&s.getInt("GateL")==2&&WitnessAccount.count(loaded,p.getUUID())==0,"the gates and the solved yard save, without story credit");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="yard_stock_push",timeoutTicks=3000)
    public static void aReaderInTheirWayIsShovedAndTheLoadingStepIsSafe(GameTestHelper h){
        var f=fixture(h);var l=f.out;var b=f.base(YardStock.PLACE);var foot=player(h,f,"stock_foot");var ahead=player(h,f,"stock_ahead");var step=player(h,f,"stock_step");double[] start={0};Vec3[] safe={null};boolean[] moving={false};
        h.onEachTick(()->{if(moving[0]){var v=ahead.getDeltaMovement();ahead.move(MoverType.SELF,new Vec3(v.x,0,v.z));ahead.setDeltaMovement(v.scale(.6));}});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the stockyard is resident"))
            .thenExecute(()->{
                YardStock.build(l,b);
                // One reader at the lane's foot, one up the lane past the first side chute, one standing on a loading step beside it.
                at(foot,l,b,.5,0,-9.5);at(ahead,l,b,.5,0,-24.5);at(step,l,b,1.5,.5,-25.5);start[0]=ahead.getZ();safe[0]=step.position();moving[0]=true;
            })
            .thenWaitUntil(()->h.assertTrue(ahead.getZ()<start[0]-2,"the cattle coming up the lane shove the reader in their way along it: "+rel(ahead,b)))
            .thenWaitUntil(()->h.assertTrue(herd(l,YardStock.state(l)).stream().anyMatch(c->rel(c,b).z<-27),"the herd passes the loading step"))
            .thenExecute(()->{h.assertTrue(step.position().distanceTo(safe[0])<.05,"the reader on the loading step is out of their way");
                h.assertTrue(ahead.getHealth()==ahead.getMaxHealth()&&foot.getHealth()==foot.getMaxHealth(),"nobody is hurt");})
            .thenSucceed();
    }
}
