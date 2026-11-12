package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSitting;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.463, Bike night's first slice: the six bikes. Each case rides on a road of its own in the test world's sky, where no other case
 * builds. Riders are real players on the server; the keys a client would send are given to the bike directly (pedal, keys), as
 * Borrasca's bike cases do.
 *
 * <p>0.4.464, the park: built whole of its own things; a reader comes in at child height at midnight; whichever bike a reader gets
 * on is theirs for the night; the failing lamp; the night over when the park stands empty; what a hand on the park's things says.
 *
 * <p>0.4.465, the swings and the sand: the frame and what hangs from it; two readers on swings side by side; a swing pumped high, let
 * go of, and the furthest landing marked in the sand; sand packed into balls, thrown, bursting, and gone outside the night.
 *
 * <p>0.4.466, the slide: what it is built of, that its ladder is climbed and its chute stood on, and a go down it.
 * These build the place itself, one case to a batch, as the literary cases do.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_bike_night") @PrefixGameTestTemplate(false)
public final class BikeNightTests {
    private BikeNightTests(){}
    private static final List<Strip> STRIPS=new ArrayList<>();
    /** A straight road of stone, five wide, running north from its start; bikes on it point north. */
    private static final class Strip {
        final GameTestHelper h;final ServerLevel l;final BlockPos at;final int length;final NativeTestChunks chunks=new NativeTestChunks();final List<ServerPlayer> players=new ArrayList<>();
        Strip(GameTestHelper h,int index,int length){
            this.h=h;l=h.getLevel();this.length=length;at=new BlockPos(123000+index*48,140,123000);
            chunks.hold(l,new AABB(at.getX()-6,at.getY()-6,at.getZ()-length-6,at.getX()+7,at.getY()+12,at.getZ()+7));STRIPS.add(this);
        }
        /**
         * Its chunks are in and both ends of the road tick entities. Loaded is not enough (run 1542): a chunk is promoted to ticking
         * entities a ticket-level update after it loads, and a case that measures a few ticks from the moment it puts its riders
         * on was measuring bikes that had not ticked at all.
         */
        boolean ready(){boolean r=chunks.entityTicking(l,at,at.offset(0,0,-length));if(!r)LockSupport.parkNanos(10_000_000L);return r;}
        void lay(){
            for(int z=3;z>=-length;z--)for(int x=-2;x<=2;x++){l.setBlock(at.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);for(int y=0;y<9;y++)l.setBlock(at.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);}
        }
        ServerPlayer reader(String name){var p=NativeTestPlayers.survival(h,name);players.add(p);p.teleportTo(l,at.getX()+.5,at.getY(),at.getZ()+2.5,180,0);NightBikes.forget(p);NativeTestPlayers.messages(p);return p;}
        /** A bike standing on the road pointing north, x across from the middle and z along from the start, and this reader on it. */
        NightBike on(ServerPlayer p,NightBike.Make make,int x,int z){
            var where=at.offset(x,0,z);var s=NightBikeBlock.of(make,Direction.NORTH,false);l.setBlock(where,s,3);p.setYRot(180);p.setXRot(0);
            var b=NightBikes.mount(p,l,where,s);h.assertTrue(b!=null&&p.getVehicle()==b,"on the "+make.id());return b;
        }
        /** How far up the road a bike has gone from the start, and how high over the road it is. */
        double along(RiddenBike b){return at.getZ()+.5-b.getZ();}
        double over(RiddenBike b){return b.getY()-at.getY();}
        void close(){for(var p:players)NativeTestPlayers.remove(p);players.clear();chunks.close();}
    }
    @AfterBatch(batch="bike_night") public static void close(ServerLevel l){for(var s:STRIPS)s.close();STRIPS.clear();NightBikes.forget();}

    /** Pushes the pedals as a rider who knows how: in turn every few ticks, and twice on the far pedal when the bike starts to go over. */
    private static void pedalWell(RiddenBike b,int tick,int every){
        if(b.leanNow()>.3F&&tick%2==0)b.pedal(-1);else if(b.leanNow()< -.3F&&tick%2==0)b.pedal(1);else if(tick%every==0)b.pedal(b.strokes()<0?1:-1);
    }
    private static boolean said(ServerPlayer p,String part){return NativeTestPlayers.messages(p).stream().anyMatch(m->m.contains(part));}

    @GameTest(template="empty",batch="bike_night",timeoutTicks=900)
    public static void aHandLooksAtABikeStandsItUpGetsOnAndSneakingGetsOff(GameTestHelper h){
        var s=new Strip(h,0,12);final NightBike[] bike={null};final ServerPlayer[] p={null,null};final BlockPos[] where={null,null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(s.ready(),"the road's sections are in")).thenExecute(()->{
            s.lay();p[0]=s.reader("bike_hand");var at=s.at.offset(0,0,-3);where[0]=at;
            s.l.setBlock(at,NightBikeBlock.of(NightBike.Make.WASP,Direction.NORTH,true),3);
            NightBikes.use(p[0],s.l,at,s.l.getBlockState(at));
            var heard=NativeTestPlayers.messages(p[0]);
            h.assertTrue(heard.stream().anyMatch(m->m.startsWith("The Wasp.")&&m.endsWith(NightBikes.STOOD)),"the first hand on a bike lying down is a look at it and stands it up: "+heard);
            h.assertTrue(!s.l.getBlockState(at).getValue(NightBikeBlock.LYING)&&!p[0].isPassenger(),"it is on its stand, and nobody is on it yet");
            NightBikes.use(p[0],s.l,at,s.l.getBlockState(at));
            h.assertTrue(p[0].getVehicle() instanceof NightBike,"the next hand gets on");bike[0]=(NightBike)p[0].getVehicle();
            h.assertTrue(s.l.getBlockState(at).isAir()&&bike[0].make()==NightBike.Make.WASP,"the block is the bike while it is ridden, and it is the Wasp");
            heard=NativeTestPlayers.messages(p[0]);
            h.assertTrue(heard.stream().anyMatch(m->m.contains("push the pedals in turn")&&m.contains("hop")&&m.contains("get off")&&!m.contains("card")),"the keys are shown once, the hop among them: "+heard);
        }).thenIdle(20).thenExecute(()->{
            h.assertTrue(p[0].getVehicle()==bike[0]&&Math.abs(bike[0].leanNow())<.05F,"stopped, a foot is down and it stays up");
            double hips=p[0].getY()+.675*p[0].getScale()-bike[0].getY();
            h.assertTrue(Math.abs(hips-(NightBike.Make.WASP.saddle()/16-.03))<.03,"the rider sits on this bike's own saddle, lower than a road bike's: "+hips);
            p[0].setPlayerInput(0,0,false,true);
        }).thenIdle(3).thenExecute(()->{
            p[0].setPlayerInput(0,0,false,false);
            var left=s.l.getBlockState(where[0]);
            h.assertTrue(!p[0].isPassenger()&&bike[0].isRemoved(),"sneaking gets off");
            h.assertTrue(left.is(BikeNightRegistry.BIKE_BLOCK.get())&&left.getValue(NightBikeBlock.MAKE)==NightBike.Make.WASP&&!left.getValue(NightBikeBlock.LYING)&&left.getValue(NightBikeBlock.FACING)==Direction.NORTH,
                "and leaves the same bike on its stand where it stopped, pointing the way it was going: "+left);
            // Somebody who has never seen it: the first hand only looks.
            p[1]=s.reader("bike_second");NightBikes.use(p[1],s.l,where[0],left);
            h.assertTrue(!p[1].isPassenger()&&said(p[1],"The Wasp. A low BMX"),"a first hand on a standing bike is a look at it");
            // The Rattler has a key of its own, shown the first time it is the Rattler; the other keys are not shown twice.
            // (What a reader is told reaches them when the tick ends, so each hand is looked at in the tick after.)
            where[1]=s.at.offset(1,0,-6);s.l.setBlock(where[1],NightBikeBlock.of(NightBike.Make.RATTLER,Direction.NORTH,false),3);
            NativeTestPlayers.messages(p[0]);NightBikes.use(p[0],s.l,where[1],s.l.getBlockState(where[1]));
        }).thenIdle(1).thenExecute(()->{
            var heard=NativeTestPlayers.messages(p[0]);
            h.assertTrue(!p[0].isPassenger()&&heard.stream().anyMatch(m->m.startsWith("The Rattler.")),"the Rattler is looked at first too: "+heard);
            NightBikes.use(p[0],s.l,where[1],s.l.getBlockState(where[1]));
        }).thenIdle(1).thenExecute(()->{
            var heard=NativeTestPlayers.messages(p[0]);
            h.assertTrue(p[0].getVehicle() instanceof NightBike r&&r.make()==NightBike.Make.RATTLER,"then ridden");
            h.assertTrue(heard.stream().anyMatch(m->m.contains("card")&&!m.contains("push the pedals")),"its own key is shown, and the rest are not shown again: "+heard);
            // A reader already riding who puts a hand on another bike only looks at it.
            NightBikes.use(p[0],s.l,where[0],s.l.getBlockState(where[0]));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(p[0].getVehicle() instanceof NightBike r&&r.make()==NightBike.Make.RATTLER&&s.l.getBlockState(where[0]).is(BikeNightRegistry.BIKE_BLOCK.get()),"a rider's hand on another bike takes nothing");
            h.assertTrue(said(p[0],"The Wasp."),"and is a look at it");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="bike_night",timeoutTicks=900)
    public static void theHareComesUpToSpeedInHalfTheTimeAndTheCometGoesFastestAndStopsSlowest(GameTestHelper h){
        var s=new Strip(h,1,90);final NightBike[] b={null,null,null};final ServerPlayer[] p={null,null,null};final int[] t={0};final float[] before=new float[3];
        h.startSequence().thenWaitUntil(()->h.assertTrue(s.ready(),"the road's sections are in")).thenExecute(()->{
            s.lay();
            p[0]=s.reader("bike_hare");p[1]=s.reader("bike_lamp");p[2]=s.reader("bike_comet");
            b[0]=s.on(p[0],NightBike.Make.HARE,-2,0);b[1]=s.on(p[1],NightBike.Make.LAMP,0,0);b[2]=s.on(p[2],NightBike.Make.COMET,2,0);
        }).thenIdle(2).thenExecuteFor(12,()->{
            // Three pushes in turn on each.
            if(t[0]%4==0)for(var bike:b)bike.pedal(t[0]%8==0?-1:1);
            t[0]++;
        }).thenExecute(()->{
            h.assertTrue(b[0].speedNow()>b[1].speedNow()*1.6F&&b[1].speedNow()<.19F,"three pushes take the Hare nearly to its top and an ordinary bike a little over half way: "+b[0].speedNow()+" against "+b[1].speedNow());
            h.assertTrue(Math.abs(b[2].speedNow()-b[1].speedNow())<.01F,"the Comet gets going no faster than any other: "+b[2].speedNow());
            t[0]=0;
        }).thenExecuteFor(110,()->{for(var bike:b)if(bike.getFirstPassenger()!=null)pedalWell(bike,t[0],3);t[0]++;}).thenExecute(()->{
            for(int i=0;i<3;i++)h.assertTrue(p[i].getVehicle()==b[i],"pedalled in turn, all three are still up");
            h.assertTrue(b[2].speedNow()>.42F&&b[1].speedNow()<=RiddenBike.TOP+.005F&&b[0].speedNow()<=RiddenBike.TOP+.005F,"flat out the Comet is far faster than the others: "+b[2].speedNow()+" against "+b[1].speedNow()+" and "+b[0].speedNow());
            h.assertTrue(s.along(b[2])>s.along(b[1])+6,"and well ahead up the road: "+s.along(b[2])+" against "+s.along(b[1]));
            for(int i=0;i<3;i++){before[i]=b[i].speedNow();p[i].setPlayerInput(0,-1,false,false);}
        }).thenIdle(8).thenExecute(()->{
            for(var q:p)q.setPlayerInput(0,0,false,false);
            float comet=b[2].speedNow()/before[2],lamp=b[1].speedNow()/before[1];
            h.assertTrue(comet>lamp+.15F,"the back key stops an ordinary bike much sooner than the Comet: it keeps "+lamp+" of its speed, the Comet "+comet);
        }).thenSucceed();
    }

    /**
     * 0.4.492: square into a wall above the crash speed, the rider comes off, wherever in its stride the bike meets the wall. It used
     * to depend on that: what a bike "kept" was how much of the tick's move it got in before the wall, so one with a good half of
     * a tick's travel still to go was let off as a glance, was under the crash speed by the next tick, and stood against the
     * wall with its rider on. The graveyard's own case found it, one run in three.
     */
    @AfterBatch(batch="bike_square") public static void squareDone(ServerLevel l){close(l);}
    // A batch of its own: the cases on the strips each take the makes they need, and two cases at once cannot both have the Wasp.
    @GameTest(template="empty",batch="bike_square",timeoutTicks=900)
    public static void squareIntoAWallFlatOutTheRiderComesOffWhereverInItsStrideTheBikeMeetsIt(GameTestHelper h){
        var s=new Strip(h,10,20);final NightBike[] b=new NightBike[5];final ServerPlayer[] p=new ServerPlayer[5];
        var makes=new NightBike.Make[]{NightBike.Make.HARE,NightBike.Make.HOPPER,NightBike.Make.LAMP,NightBike.Make.RATTLER,NightBike.Make.WASP};
        // How much of a tick's travel each has left to the wall when the tick starts.
        double[] left={.1,.3,.5,.7,.9};
        h.startSequence().thenWaitUntil(()->h.assertTrue(s.ready(),"the road's sections are in")).thenExecute(()->{
            s.lay();
            for(int x=-2;x<=2;x++)for(int y=0;y<3;y++)s.l.setBlock(s.at.offset(x,y,-12),Blocks.STONE.defaultBlockState(),3);
            for(int i=0;i<5;i++){
                p[i]=s.reader("bike_square_"+i);b[i]=s.on(p[i],makes[i],i-2,-2);
                p[i].setYRot(180);b[i].setYRot(180);b[i].steady(0,RiddenBike.TOP);
                // The wall's face is eleven blocks up the road; a bike is three tenths of a block from its middle to its front.
                b[i].setPos(s.at.getX()+i-2+.5,s.at.getY(),s.at.getZ()-11+.3+RiddenBike.TOP*left[i]);
            }
        }).thenIdle(3).thenExecute(()->{
            for(int i=0;i<5;i++)h.assertTrue(p[i].getVehicle()==null,"the rider who had "+left[i]+" of a tick's travel left to the wall comes off: still on the "+makes[i].id()+(b[i].isAlive()?" going "+b[i].speedNow():""));
        }).thenSucceed();
    }
    @GameTest(template="empty",batch="bike_night",timeoutTicks=900)
    public static void theWaspTurnsTighterAndAWallHardlySlowsIt(GameTestHelper h){
        var s=new Strip(h,2,20);final NightBike[] b={null,null};final ServerPlayer[] p={null,null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(s.ready(),"the road's sections are in")).thenExecute(()->{
            s.lay();p[0]=s.reader("bike_wasp");p[1]=s.reader("bike_plain");
            b[0]=s.on(p[0],NightBike.Make.WASP,-1,-2);b[1]=s.on(p[1],NightBike.Make.LAMP,1,-2);
            // Both riders look hard round to the west, at an easy speed.
            for(int i=0;i<2;i++){b[i].steady(0,.2F);p[i].setYRot(90);}
        }).thenIdle(4).thenExecute(()->{
            float wasp=Math.abs(Mth.wrapDegrees(b[0].getYRot()-180)),plain=Math.abs(Mth.wrapDegrees(b[1].getYRot()-180));
            h.assertTrue(wasp>plain*1.5F&&plain>10,"in the same four ticks the Wasp has turned much further: "+wasp+" degrees against "+plain);
            h.assertTrue(NightBike.Make.WASP.stats.turnLean<RiddenBike.Stats.PLAIN.turnLean*.5F,"and a turn leans it less than half as far");
            // A wall across the road, met at an angle: half of each bike's way is along it and the rest into it.
            for(int x=-2;x<=2;x++)for(int y=0;y<3;y++)s.l.setBlock(s.at.offset(x,y,-12),Blocks.STONE.defaultBlockState(),3);
            for(int i=0;i<2;i++){
                p[i].setYRot(210);b[i].setYRot(210);b[i].steady(0,.3F);
                b[i].setPos(s.at.getX()+(i==0?-1.5:.5),s.at.getY(),s.at.getZ()-10.69);
            }
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(p[0].getVehicle()==b[0]&&p[1].getVehicle()==b[1],"neither rider comes off at a glance");
            h.assertTrue(b[0].speedNow()>.22F&&b[1].speedNow()<.17F,"the wall takes half an ordinary bike's way and a fifth of the Wasp's: "+b[1].speedNow()+" against "+b[0].speedNow());
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="bike_night",timeoutTicks=1200)
    public static void aLongerHoldHopsHigherTheAirShiftsWeightAndACrookedLandingTips(GameTestHelper h){
        var s=new Strip(h,3,40);final NightBike[] b={null,null};final ServerPlayer[] p={null,null};final double[] top={0,0};final float[] kept={0,0,0,0};final boolean[] flown={false};
        h.startSequence().thenWaitUntil(()->h.assertTrue(s.ready(),"the road's sections are in")).thenExecute(()->{
            s.lay();p[0]=s.reader("bike_hop");p[1]=s.reader("bike_hopper");
            b[0]=s.on(p[0],NightBike.Make.HARE,-1,0);b[1]=s.on(p[1],NightBike.Make.HOPPER,1,0);
        }).thenIdle(3).thenExecute(()->b[0].keys(true,false)).thenIdle(1).thenExecute(()->{
            h.assertTrue(b[0].crouch()>0&&b[0].crouch()<.2F,"a touch of the jump key crouches the rider a little: "+b[0].crouch());b[0].keys(false,false);top[0]=0;
        }).thenExecuteFor(14,()->top[0]=Math.max(top[0],s.over(b[0]))).thenExecute(()->{
            h.assertTrue(top[0]>.35&&top[0]<.75&&b[0].onGround(),"a tap is a small hop, and the bike is down again: "+top[0]);
            for(var bike:b)bike.keys(true,false);
        }).thenIdle(RiddenBike.CHARGE+2).thenExecute(()->{
            h.assertTrue(b[0].crouch()==1&&b[0].onGround(),"held, the rider is right down over the bars and the bike has not left the road");
            for(var bike:b)bike.keys(false,false);top[0]=top[1]=0;
        }).thenExecuteFor(6,()->{for(int i=0;i<2;i++)top[i]=Math.max(top[i],s.over(b[i]));}).thenExecute(()->{
            // In the air a push on a pedal drives nothing and turns no crank: it shifts the rider's weight that way.
            h.assertTrue(!b[0].onGround(),"six ticks on it is still in the air");
            float lean=b[0].leanNow(),speed=b[0].speedNow();int strokes=b[0].strokes();b[0].pedal(1);
            h.assertTrue(Math.abs(b[0].leanNow()-lean-RiddenBike.AIR_TIP)<.001F&&b[0].speedNow()==speed&&b[0].strokes()==strokes,"a push in the air tips the rider's weight and nothing else: "+b[0].leanNow());
            b[0].pedal(-1);
        }).thenExecuteFor(14,()->{for(int i=0;i<2;i++)top[i]=Math.max(top[i],s.over(b[i]));}).thenExecute(()->{
            h.assertTrue(top[0]>1.1&&top[0]<1.35,"a full hold is a hop of a block and a quarter: "+top[0]);
            h.assertTrue(top[1]>1.9&&top[1]<2.25,"and the Hopper's goes over two: "+top[1]);
            h.assertTrue(b[0].onGround()&&b[1].onGround()&&p[0].getVehicle()==b[0]&&p[1].getVehicle()==b[1],"both are down again with their riders on");
            // Landing level keeps the way on. Both ride on at a good speed and hop as high as each can.
            for(var bike:b){bike.steady(0,.3F);bike.keys(true,false);}
        }).thenExecuteFor(RiddenBike.CHARGE+1,()->{for(var bike:b)bike.steady(0,.3F);}).thenExecute(()->{for(var bike:b)bike.keys(false,false);flown[0]=false;})
          .thenExecuteFor(22,()->{
            // Level all the way: the rider keeps the bike upright in the air.
            for(int i=0;i<2;i++){if(!b[i].onGround()){b[i].steady(0,b[i].speedNow());flown[0]=true;}else if(flown[0]&&kept[i]==0)kept[i]=b[i].speedNow();}
        }).thenExecute(()->{
            h.assertTrue(kept[0]>.24F&&kept[1]>.22F,"down on both wheels, each keeps nearly all its way: "+kept[0]+" and "+kept[1]);
            for(var bike:b){bike.steady(0,.3F);bike.keys(true,false);}
        }).thenExecuteFor(RiddenBike.CHARGE+1,()->{for(var bike:b)bike.steady(0,.3F);}).thenExecute(()->{for(var bike:b)bike.keys(false,false);flown[0]=false;})
          .thenExecuteFor(22,()->{
            // Crooked all the way: half over, and held there until the wheels touch.
            for(int i=0;i<2;i++){if(!b[i].onGround()){b[i].steady(.5F,b[i].speedNow());flown[0]=true;}else if(flown[0]&&kept[2+i]==0)kept[2+i]=b[i].leanNow();}
        }).thenExecute(()->{
            h.assertTrue(kept[2]>.72F,"down crooked, an ordinary bike is tipped much further the way it leaned: from a half to "+kept[2]);
            h.assertTrue(kept[3]>.5F&&kept[3]<.63F,"the Hopper, from higher up, hardly at all: "+kept[3]);
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="bike_night",timeoutTicks=900)
    public static void aRampThrowsAMovingBikeUpByItsOwnSpeed(GameTestHelper h){
        var s=new Strip(h,4,40);final NightBike[] b={null,null,null};final ServerPlayer[] p={null,null,null};final double[] top={0,0,0};final int[] air={0,0,0};final boolean[] left={false,false,false};
        h.startSequence().thenWaitUntil(()->h.assertTrue(s.ready(),"the road's sections are in")).thenExecute(()->{
            s.lay();
            // The long ramp on the left, the kicker in the middle, and the long ramp again on the right for a bike that only crawls up it.
            for(int x:new int[]{-2,2}){s.l.setBlock(s.at.offset(x,0,-12),BikeRampBlock.of(Direction.NORTH,BikeRampBlock.Part.LOW),3);s.l.setBlock(s.at.offset(x,0,-13),BikeRampBlock.of(Direction.NORTH,BikeRampBlock.Part.HIGH),3);}
            s.l.setBlock(s.at.offset(0,0,-12),BikeRampBlock.of(Direction.NORTH,BikeRampBlock.Part.STEEP),3);
            p[0]=s.reader("bike_ramp");p[1]=s.reader("bike_kicker");p[2]=s.reader("bike_crawl");
            b[0]=s.on(p[0],NightBike.Make.LAMP,-2,-4);b[1]=s.on(p[1],NightBike.Make.LAMP,0,-4);b[2]=s.on(p[2],NightBike.Make.LAMP,2,-11);
        }).thenIdle(2).thenExecuteFor(70,()->{
            for(int i=0;i<3;i++){
                if(p[i].getVehicle()!=b[i])continue;
                boolean past=s.along(b[i])>(i==1?12.5:13.5);
                // Held upright and at one speed until the ramp's end; after it the bike is left to itself.
                if(!past)b[i].steady(0,i==2?.1F:.32F);
                else{if(!b[i].onGround()){left[i]=true;air[i]++;top[i]=Math.max(top[i],s.over(b[i]));b[i].steady(0,b[i].speedNow());}}
            }
        }).thenExecute(()->{
            double lip=2*BikeRampBlock.LONG/16;
            h.assertTrue(left[0]&&top[0]>lip+.2&&air[0]>=7,"off the long ramp at speed the bike goes up well past the lip and is in the air a good while: "+top[0]+" over a lip at "+lip+", for "+air[0]+" ticks");
            h.assertTrue(left[1]&&top[1]>1.7,"off the kicker it goes nearly two blocks up: "+top[1]);
            h.assertTrue(left[2]&&top[2]<lip+.08,"crawling off the end, it hardly leaves the sheet's own line before it drops: "+top[2]);
            for(int i=0;i<3;i++)h.assertTrue(p[i].getVehicle()==b[i],"and all three riders are still on");
            // What the ramp is to stand on: eight steps, none more than two pixels, to the height of the sheet.
            for(var part:BikeRampBlock.Part.values()){
                var shape=BikeRampBlock.of(Direction.NORTH,part).getCollisionShape(s.l,s.at);
                h.assertTrue(Math.abs(shape.max(Direction.Axis.Y)*16-(part.to-(part.to-part.from)/16))<.01,part+" stands as high as its sheet's top step: "+shape.max(Direction.Axis.Y)*16);
            }
        }).thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.478: hills
    @GameTest(template="empty",batch="bike_night",timeoutTicks=900)
    public static void aHillTakesTheWayOffABikeGoingUpAndGivesItBackComingDown(GameTestHelper h){
        var s=new Strip(h,9,40);final NightBike[] b={null,null,null};final ServerPlayer[] p={null,null,null};
        final float[] lip={0,0,0},foot={0,0},after={0};final boolean[] over={false,false,false},flew={false};final double[] off={0};final int[] t={0,0};
        double slope=BikeRampBlock.LONG/16;
        // How high the long ramp's sheet is over the road, that far up the road: its foot is eleven and a half blocks up, and it is two long.
        java.util.function.DoubleUnaryOperator sheet=along->Mth.clamp(along-11.5,0,2)*slope;
        // The first bike again, on the ramp near its top, pointing back down it and going that fast.
        java.util.function.Consumer<Float> atTheTop=speed->{
            b[0].setPos(s.at.getX()-1.5,s.at.getY()+sheet.applyAsDouble(13.3),s.at.getZ()+.5-13.3);b[0].setYRot(0);p[0].setYRot(0);p[0].setYHeadRot(0);b[0].steady(0,speed);
        };
        h.startSequence().thenWaitUntil(()->h.assertTrue(s.ready(),"the road's sections are in")).thenExecute(()->{
            s.lay();
            for(int x:new int[]{-2,0,2}){s.l.setBlock(s.at.offset(x,0,-12),BikeRampBlock.of(Direction.NORTH,BikeRampBlock.Part.LOW),3);s.l.setBlock(s.at.offset(x,0,-13),BikeRampBlock.of(Direction.NORTH,BikeRampBlock.Part.HIGH),3);}
            p[0]=s.reader("hill_fast");p[1]=s.reader("hill_slow");p[2]=s.reader("hill_hare");
            b[0]=s.on(p[0],NightBike.Make.LAMP,-2,-4);b[1]=s.on(p[1],NightBike.Make.LAMP,0,-4);b[2]=s.on(p[2],NightBike.Make.HARE,2,-4);
        }).thenIdle(2).thenExecuteFor(110,()->{
            for(int i=0;i<3;i++){
                if(p[i].getVehicle()!=b[i])continue;double along=s.along(b[i]);
                // Held upright all the way. Held at one speed as far as the foot of the ramp, and from there left to roll.
                if(along<11.5)b[i].steady(0,i==0?.32F:.16F);
                else{b[i].steady(0,b[i].speedNow());if(along>13.5&&!over[i]){over[i]=true;lip[i]=b[i].speedNow();}}
            }
        }).thenExecute(()->{
            for(int i=0;i<3;i++)h.assertTrue(p[i].getVehicle()==b[i],"all three riders are still on");
            h.assertTrue(over[0]&&lip[0]>.17F&&lip[0]<.25F,"flat out, the ramp takes a third of a bike's way before it is over the top: "+lip[0]);
            double at=s.along(b[1]);
            h.assertTrue(!over[1]&&b[1].speedNow()<RiddenBike.STOPPED&&at>12&&at<13.4&&Math.abs(s.over(b[1])-sheet.applyAsDouble(at))<.03,
                "at half that speed it stalls on the ramp, and stands where it stopped with a foot down: "+at+" up the road, "+s.over(b[1])+" over it, at "+b[1].speedNow());
            h.assertTrue(over[2]&&lip[2]>.04F,"the Hare, which climbs, is over the top from the same speed: "+lip[2]);
            // Down again: from near the top, barely moving, and left to roll.
            atTheTop.accept(.06F);
        }).thenExecuteFor(90,()->{
            if(p[0].getVehicle()!=b[0])return;double along=s.along(b[0]);b[0].steady(0,b[0].speedNow());t[0]++;
            if(along>11.55){if(t[0]>2){off[0]=Math.max(off[0],Math.abs(s.over(b[0])-sheet.applyAsDouble(along)));flew[0]|=!b[0].onGround();}}
            else{if(foot[0]==0)foot[0]=b[0].speedNow();if(++t[1]==40)after[0]=b[0].speedNow();}
        }).thenExecute(()->{
            h.assertTrue(p[0].getVehicle()==b[0],"the rider is still on");
            h.assertTrue(!flew[0]&&off[0]<.03,"rolling down, it stays on the ramp's sheet all the way: "+off[0]+" off it at the most, in the air "+flew[0]);
            h.assertTrue(foot[0]>.15F,"let roll from the top with hardly a push, it is going well at the foot: "+foot[0]);
            h.assertTrue(after[0]>foot[0]*.45F&&after[0]<foot[0]*.75F,"and on the level it runs that way off again, a little at a time: "+after[0]+" from "+foot[0]);
            // And from flat out at the top.
            foot[0]=0;t[0]=0;atTheTop.accept(.32F);
        }).thenExecuteFor(25,()->{
            if(p[0].getVehicle()!=b[0])return;b[0].steady(0,b[0].speedNow());
            if(s.along(b[0])<=11.55&&foot[1]==0)foot[1]=b[0].speedNow();
        }).thenExecute(()->{
            h.assertTrue(foot[1]>RiddenBike.TOP+.015F&&foot[1]<=RiddenBike.TOP*1.4F,"a bike already flat out comes off the foot of the ramp faster than it can be pedalled: "+foot[1]);
            p[0].setPlayerInput(0,-1,false,false);
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="bike_night",timeoutTicks=900)
    public static void theHopperCarriesASecondRiderOnItsPegsAndBothComeOffTogether(GameTestHelper h){
        var s=new Strip(h,5,30);final NightBike[] b={null,null};final ServerPlayer[] p={null,null,null};final float[] health={0,0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(s.ready(),"the road's sections are in")).thenExecute(()->{
            s.lay();p[0]=s.reader("bike_pedals");p[1]=s.reader("bike_pegs");p[2]=s.reader("bike_other");
            b[0]=s.on(p[0],NightBike.Make.HOPPER,-1,0);b[1]=s.on(p[2],NightBike.Make.WASP,1,0);
            h.assertTrue(b[0].isPickable()&&!b[1].isPickable(),"a Hopper with one rider can be touched by somebody else; no other bike can");
            h.assertTrue(!b[1].interact(p[1],InteractionHand.MAIN_HAND).consumesAction()&&!p[1].isPassenger(),"the Wasp carries nobody");
            h.assertTrue(b[0].interact(p[1],InteractionHand.MAIN_HAND).consumesAction()&&p[1].getVehicle()==b[0],"a hand on the Hopper puts a second reader on its pegs");
            h.assertTrue(said(p[1],"pegs")&&!b[0].isPickable(),"they are told so, and there is no room for a third");
            b[1].pedal(-1);b[1].pedal(1);b[0].pedal(-1);b[0].pedal(1);
            h.assertTrue(Math.abs(b[0].speedNow()-b[1].speedNow()*NightBike.CARRYING)<.001F,"two aboard, each push drives it on a little less: "+b[0].speedNow()+" against "+b[1].speedNow());
        }).thenIdle(2).thenExecute(()->{
            // Over the back axle, feet on the pegs, behind the one pedalling.
            double behind=p[1].getZ()-b[0].getZ(),up=p[1].getY()-b[0].getY();
            h.assertTrue(Math.abs(behind-(8-NightBike.Make.HOPPER.backX())/16)<.02&&Math.abs(up-NightBike.Make.HOPPER.backY()/16)<.02,"the second rider stands over the back axle, feet on the pegs: "+behind+" behind, "+up+" up");
            p[1].setPlayerInput(0,0,false,true);
        }).thenIdle(3).thenExecute(()->{
            p[1].setPlayerInput(0,0,false,false);
            h.assertTrue(!p[1].isPassenger()&&p[0].getVehicle()==b[0]&&!b[0].isRemoved(),"the one on the pegs steps off and the bike goes on");
            h.assertTrue(b[0].interact(p[1],InteractionHand.MAIN_HAND).consumesAction()&&p[1].getVehicle()==b[0],"and gets on again");
        // A player newly on the server cannot be hurt for three seconds, and these two only count them while they ride.
        }).thenIdle(64).thenExecute(()->{
            for(int i=0;i<2;i++)health[i]=p[i].getHealth();
            // Over it goes.
            b[0].steady(.99F,.2F);b[0].pedal(1);
        }).thenIdle(3).thenExecute(()->{
            h.assertTrue(!p[0].isPassenger()&&!p[1].isPassenger()&&b[0].isRemoved(),"when it goes over, both come off");
            h.assertTrue(p[0].getHealth()==health[0]-1&&p[1].getHealth()==health[1]-1,"and it costs each of them half a heart: "+p[0].getHealth()+" and "+p[1].getHealth());
            boolean lying=false;
            for(var at:BlockPos.betweenClosed(s.at.offset(-4,-1,-6),s.at.offset(3,2,3))){var st=s.l.getBlockState(at);if(st.is(BikeNightRegistry.BIKE_BLOCK.get())&&st.getValue(NightBikeBlock.MAKE)==NightBike.Make.HOPPER&&st.getValue(NightBikeBlock.LYING))lying=true;}
            h.assertTrue(lying&&said(p[0],NightBikes.OVER),"the Hopper lies on its side where it went over, and its rider is told");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="bike_night",timeoutTicks=600)
    public static void aBikeLetGoOfInTheAirComesToRestOnTheGroundUnderIt(GameTestHelper h){
        var s=new Strip(h,8,12);final NightBike[] b={null};final ServerPlayer[] p={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(s.ready(),"the road's sections are in")).thenExecute(()->{
            s.lay();p[0]=s.reader("bike_air");b[0]=s.on(p[0],NightBike.Make.COMET,0,-4);
        }).thenIdle(2).thenExecute(()->{
            // Nine blocks over the road, as off the top of something, and the rider lets go.
            b[0].setPos(b[0].getX(),s.at.getY()+9,b[0].getZ());p[0].setPlayerInput(0,0,false,true);
        }).thenIdle(3).thenExecute(()->{
            p[0].setPlayerInput(0,0,false,false);
            h.assertTrue(!p[0].isPassenger()&&b[0].isRemoved(),"the rider is off");
            BlockPos found=null;int count=0;
            for(var at:BlockPos.betweenClosed(s.at.offset(-2,0,-8),s.at.offset(2,12,0))){var st=s.l.getBlockState(at);if(st.is(BikeNightRegistry.BIKE_BLOCK.get())){count++;found=at.immutable();}}
            h.assertTrue(count==1&&found!=null&&found.getY()==s.at.getY()&&s.l.getBlockState(found).getValue(NightBikeBlock.MAKE)==NightBike.Make.COMET,
                "the Comet stands on the road under where it was let go, and nowhere else: "+count+" at "+found);
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="bike_night",timeoutTicks=1200)
    public static void theLampThrowsARealLightAheadBrighterTheFasterAndLeavesNoneBehind(GameTestHelper h){
        var s=new Strip(h,6,60);final NightBike[] b={null};final ServerPlayer[] p={null};final BlockPos[] stray={null,null};final boolean[] inGlass={false};
        h.startSequence().thenWaitUntil(()->h.assertTrue(s.ready(),"the road's sections are in")).thenExecute(()->{
            s.lay();p[0]=s.reader("bike_light");b[0]=s.on(p[0],NightBike.Make.LAMP,0,0);
            // A light with no lamp behind it, as a server stopped in mid-ride would leave: it must put itself out.
            stray[0]=s.at.offset(2,1,-40);s.l.setBlock(stray[0],BikeBeamBlock.of(15),3);
        }).thenIdle(3).thenExecute(()->{
            h.assertTrue(b[0].lit()==0&&b[0].beam()==null,"standing still, the dynamo makes no light");
        }).thenExecuteFor(10,()->b[0].steady(0,.3F)).thenExecute(()->{
            var beam=b[0].beam();
            h.assertTrue(beam!=null&&s.l.getBlockState(beam).is(BikeNightRegistry.BEAM.get()),"going, it lights the air ahead of it: "+beam);
            h.assertTrue(s.l.getBlockState(beam).getValue(BikeBeamBlock.LEVEL)==15&&b[0].lit()==15&&s.l.getBlockState(beam).getLightEmission(s.l,beam)==15,"at a good speed as brightly as anything can: "+s.l.getBlockState(beam)+", lit "+b[0].lit());
            double ahead=b[0].getZ()-(beam.getZ()+.5);
            h.assertTrue(ahead>1&&ahead<3&&beam.getY()==s.at.getY()+1,"two blocks ahead, at the height of the bars: "+ahead);
            int lights=0;for(var at:BlockPos.betweenClosed(s.at.offset(-2,0,-s.length),s.at.offset(2,4,3)))if(s.l.getBlockState(at).is(BikeNightRegistry.BEAM.get())&&!at.equals(stray[0]))lights++;
            h.assertTrue(lights==1,"and only there: the light behind is taken away as the bike goes on: "+lights);
        }).thenExecuteFor(6,()->b[0].steady(0,.1F)).thenExecute(()->{
            var beam=b[0].beam();
            h.assertTrue(beam!=null&&s.l.getBlockState(beam).getValue(BikeBeamBlock.LEVEL)==12&&b[0].lit()==12,"slower, the lamp is dimmer: "+b[0].lit());
            // Something in the way at the height of the bars, three blocks on: the light falls nearer as the bike comes up to it, never into it.
            stray[1]=BlockPos.containing(b[0].getX(),s.at.getY()+1,b[0].getZ()-3);s.l.setBlock(stray[1],Blocks.GLASS.defaultBlockState(),3);
        }).thenExecuteFor(8,()->{b[0].steady(0,.3F);if(stray[1]!=null&&stray[1].equals(b[0].beam()))inGlass[0]=true;}).thenExecute(()->{
            h.assertTrue(!inGlass[0]&&s.l.getBlockState(stray[1]).is(Blocks.GLASS)&&b[0].beam()!=null,"the pane is still a pane, and the light went round it");
        }).thenExecuteFor(2,()->b[0].steady(0,.02F)).thenExecute(()->{
            h.assertTrue(b[0].lit()==0&&b[0].beam()==null,"stopped again, it is dark, and its light is gone from the air");
        }).thenExecuteFor(8,()->b[0].steady(0,.3F)).thenExecute(()->{
            h.assertTrue(b[0].beam()!=null,"going again it lights again");
            p[0].setPlayerInput(0,0,false,true);
        }).thenIdle(3).thenExecute(()->{
            p[0].setPlayerInput(0,0,false,false);
            h.assertTrue(!p[0].isPassenger()&&b[0].isRemoved(),"the rider gets off");
        }).thenIdle(BikeBeamBlockCheck.TICKS).thenExecute(()->{
            int lights=0;for(var at:BlockPos.betweenClosed(s.at.offset(-2,0,-s.length),s.at.offset(2,4,3)))if(s.l.getBlockState(at).is(BikeNightRegistry.BEAM.get()))lights++;
            h.assertTrue(lights==0,"and no light is left standing anywhere on the road, the stray one included: "+lights);
        }).thenSucceed();
    }
    /** How long to wait for a light with no lamp to put itself out: a little over one of its checks. */
    private static final class BikeBeamBlockCheck{static final int TICKS=26;}

    @GameTest(template="empty",batch="bike_night",timeoutTicks=1500)
    public static void theRattlersCardTakesItPastItsTopForTwoSecondsAndComesBackWithSteadyRiding(GameTestHelper h){
        var s=new Strip(h,7,110);final NightBike[] b={null};final ServerPlayer[] p={null};final float[] fastest={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(s.ready(),"the road's sections are in")).thenExecute(()->{
            s.lay();p[0]=s.reader("bike_card");b[0]=s.on(p[0],NightBike.Make.RATTLER,0,0);
            h.assertTrue(b[0].turboNow()==1,"the card is ready when the bike is taken");
            b[0].keys(false,true);
        }).thenIdle(3).thenExecute(()->{
            h.assertTrue(b[0].turboNow()==1&&b[0].speedNow()==0,"standing still the sprint key does nothing, and spends nothing");
            b[0].steady(0,.3F);NativeTestPlayers.sounds(p[0]);
        }).thenExecuteFor(NightBike.TURBO_TICKS,()->{b[0].steady(0,b[0].speedNow());fastest[0]=Math.max(fastest[0],b[0].speedNow());}).thenExecute(()->{
            h.assertTrue(fastest[0]>RiddenBike.TOP*1.4F&&fastest[0]<=RiddenBike.TOP*NightBike.TURBO_TOP+.001F,"held while riding, the card takes the bike well past its top speed: "+fastest[0]);
            h.assertTrue(b[0].turboNow()<.03F,"and is spent in two seconds: "+b[0].turboNow());
            var heard=NativeTestPlayers.sounds(p[0]).stream().map(x->x.getSound().value().getLocation().getPath()).toList();
            h.assertTrue(heard.contains("bike.turbo")&&heard.contains("bike.card"),"the card is heard, and its roar: "+heard);
        }).thenExecuteFor(30,()->b[0].steady(0,b[0].speedNow())).thenExecute(()->{
            h.assertTrue(b[0].turboNow()<.03F&&b[0].speedNow()<RiddenBike.TOP*1.2F,"still held, it gives nothing more and comes back nothing; the bike slows to its own speed: "+b[0].speedNow());
            b[0].keys(false,false);
        }).thenExecuteFor(150,()->b[0].steady(0,.3F)).thenExecute(()->{
            h.assertTrue(Math.abs(b[0].turboNow()-.5F)<.06F,"let go, steady riding brings it back: half in seven and a half seconds: "+b[0].turboNow());
            h.assertTrue(Math.abs(b[0].turbo()-b[0].turboNow())<.03F,"and the rider's screen is told how much: "+b[0].turbo());
        }).thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.464: the park
    private static final LabyrinthPlace P=LabyrinthPlace.BIKE_NIGHT;
    private static LiteraryTests.Fixture active;
    private static final Map<UUID,List<String>> HEARD=new HashMap<>(),SOUNDED=new HashMap<>();
    /** 0.4.495: for how many ticks a reader was last told the mist is over everything. */
    private static final Map<UUID,Integer> OVER=new HashMap<>(),SHARE=new HashMap<>();
    /** 0.4.476: the playground's own cases have it to themselves: Gus stays at his place and says nothing, but for the cases that are his. */
    private static LiteraryTests.Fixture park(GameTestHelper h){done();BikeNightGus.keptHome=true;BikeNightBullies.keptAway=true;BikeNightSpecters.keptAway=true;active=new LiteraryTests.Fixture(h);return active;}
    private static void done(){if(active!=null){active.close();active=null;}BikeNight.clearAll();NightBikes.forget();HEARD.clear();SOUNDED.clear();OVER.clear();SHARE.clear();BikeNightSpecters.keptAway=false;BikeNightSpecters.only=null;BikeNightGus.keptHome=false;
        BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=BikeNightBullies.ARRIVE;BikeNightBullies.patience=BikeNightBullies.PATIENCE;}
    /** The park and Park Street: what the park's own cases keep resident. (0.4.481: the place is the town too; its cases keep that, {@link #town}.) */
    private static AABB room(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()-58,b.getY()-5,b.getZ()-74,b.getX()+59,b.getY()+19,b.getZ()+2);}
    /** The town's ground, and Park Street with it. */
    private static AABB town(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()+CalderTown.W-1,b.getY()-5,b.getZ()+CalderTown.N-1,b.getX()+CalderTown.E+2,b.getY()+CalderTown.TOP+3,b.getZ()+BikeNightSite.NORTH+3);}
    /** Everything a reader has been told so far has this in it. What is said reaches a reader when the tick ends, so cases wait on it. */
    /** What a reader was told and what they heard come down one pipe, and reading it empties it: both are kept as they are read. */
    private static void drain(ServerPlayer p){
        var said=HEARD.computeIfAbsent(p.getUUID(),k->new ArrayList<>());var rang=SOUNDED.computeIfAbsent(p.getUUID(),k->new ArrayList<>());
        for(var packet:NativeTestPlayers.packets(p)){
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)said.add(chat.content().getString());
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound)rang.add(sound.getSound().value().getLocation().getPath());
            if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom&&custom.payload() instanceof io.github.knaitoe.theoldesthouse.network.BikeNightMistPayload mist&&mist.over()>0){OVER.put(p.getUUID(),mist.over());SHARE.put(p.getUUID(),mist.share());}
        }
    }
    private static boolean heard(ServerPlayer p,String part){drain(p);return HEARD.get(p.getUUID()).stream().anyMatch(m->m.contains(part));}
    private static boolean sounded(ServerPlayer p,String name){drain(p);return SOUNDED.get(p.getUUID()).contains(name);}
    private static void hush(ServerPlayer p){drain(p);HEARD.remove(p.getUUID());SOUNDED.remove(p.getUUID());OVER.remove(p.getUUID());SHARE.remove(p.getUUID());}
    /** A hand on a bike nobody is on: the block's own use. */
    private static void hand(LiteraryTests.Fixture f,ServerPlayer p,BlockPos at){var l=f.level(P);NightBikes.use(p,l,at,l.getBlockState(at));}
    /** A reader who has looked at this bike gets on it where it stands in the rack. */
    private static NightBike take(GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer p,NightBike.Make make){
        var at=f.base(P).offset(BikeNightSite.bikeAt(make));hand(f,p,at);if(!p.isPassenger())hand(f,p,at);
        h.assertTrue(p.getVehicle() instanceof NightBike b&&b.make()==make,p.getGameProfile().getName()+" is on the "+make.id());return (NightBike)p.getVehicle();
    }
    private static void getOff(ServerPlayer p){p.setPlayerInput(0,0,false,true);}
    private static void stand(ServerPlayer p){p.setPlayerInput(0,0,false,false);}
    @AfterBatch(batch="park_built") public static void builtDone(ServerLevel l){done();}
    @AfterBatch(batch="park_arrive") public static void arriveDone(ServerLevel l){done();}
    @AfterBatch(batch="park_bikes") public static void bikesDone(ServerLevel l){done();}
    @AfterBatch(batch="park_lamp") public static void lampDone(ServerLevel l){done();}
    @AfterBatch(batch="park_empty") public static void emptyDone(ServerLevel l){done();}
    @AfterBatch(batch="park_hands") public static void handsDone(ServerLevel l){done();}
    @AfterBatch(batch="park_rails") public static void railsDone(ServerLevel l){done();}
    @AfterBatch(batch="park_swings") public static void swingsDone(ServerLevel l){done();}
    @AfterBatch(batch="park_pump") public static void pumpDone(ServerLevel l){done();}
    @AfterBatch(batch="park_push") public static void pushDone(ServerLevel l){done();}
    @AfterBatch(batch="park_rope") public static void ropeDone(ServerLevel l){done();}
    @AfterBatch(batch="park_over") public static void overDone(ServerLevel l){done();}
    @AfterBatch(batch="park_gus") public static void gusDone(ServerLevel l){done();}
    @AfterBatch(batch="park_slide_bike") public static void slideBikeDone(ServerLevel l){done();}
    @AfterBatch(batch="park_slide_speed") public static void slideSpeedDone(ServerLevel l){done();}
    @AfterBatch(batch="park_bullies") public static void bulliesDone(ServerLevel l){done();}
    @AfterBatch(batch="park_bullies_sand") public static void bulliesSandDone(ServerLevel l){done();}
    @AfterBatch(batch="park_bullies_time") public static void bulliesTimeDone(ServerLevel l){done();}
    @AfterBatch(batch="park_gus_seesaw") public static void gusSeesawDone(ServerLevel l){done();}
    @AfterBatch(batch="park_gus_swing") public static void gusSwingDone(ServerLevel l){done();}
    @AfterBatch(batch="park_sand") public static void sandDone(ServerLevel l){done();}
    @AfterBatch(batch="park_slide") public static void slideDone(ServerLevel l){done();}
    @AfterBatch(batch="park_bars") public static void barsDone(ServerLevel l){done();}
    @AfterBatch(batch="park_seesaw") public static void seesawDone(ServerLevel l){done();}
    @AfterBatch(batch="park_round") public static void roundDone(ServerLevel l){done();}
    @AfterBatch(batch="park_small") public static void smallDone(ServerLevel l){done();}
    @AfterBatch(batch="park_dome") public static void domeDone(ServerLevel l){done();}
    @AfterBatch(batch="park_spring") public static void springDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="park_built",timeoutTicks=2400)
    public static void theParkIsBuiltWholeOfItsOwnThingsWithSixBikesInTheRack(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.kind()==LabyrinthPlace.Kind.ONE_SHOT&&P.authored()&&SceneClock.at(f.origin,b.offset(0,0,-20),HouseDimensions.OUTSIDE)==9,"a literary place outside, finished once, built whole, at midnight");
                // 0.4.508: the night can be finished, so it is a source, and is dealt to somebody who has not had it.
                h.assertTrue(WitnessAccount.Story.of(P.id())==WitnessAccount.Story.BIKE_NIGHT&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED&&BikeNight.dealable(f.data(),UUID.randomUUID()),
                    "a source, the quota derived from the pool with it in, and dealt to somebody who has not had it");
                // The back wall and the shed's door in it; the paper and the rules beside it.
                h.assertTrue(l.getBlockState(b.offset(0,0,1)).getBlock() instanceof DoorBlock&&l.getBlockState(b.offset(0,1,1)).getBlock() instanceof DoorBlock,"the tool shed's door, in the back wall");
                var brick=BikeNightRegistry.material("brick");var kerb=BikeNightRegistry.material("kerb");
                for(int x:new int[]{-40,-20,-5,5,20,40})h.assertTrue(l.getBlockState(b.offset(x,2,1)).is(brick),"the back wall is brick at x="+x);
                h.assertTrue(l.getBlockState(b.offset(-30,6,1)).is(kerb)&&l.getBlockState(b.offset(0,7,1)).is(brick)&&l.getBlockState(b.offset(0,8,1)).is(kerb),"a stone coping on the wall, and on the shed's front, which stands two courses over it");
                h.assertTrue(CalderBlock.is(l.getBlockState(b.offset(BikeNightSite.SOURCE)),CalderBlock.Kind.NOTE)&&l.getBlockState(b.offset(BikeNightSite.SOURCE).south()).is(brick),"the sheet from a school pad, taped to the brick by the door");
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=String.join(" ",text.pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(text.author().equals("Gus")&&all.contains("HALLOWEEN PLAN")&&all.contains("Come through the tool shed")&&all.contains("Do NOT cut through the graveyard"),"the plan for the night, in Gus's hand: "+all);
                // The park's own two. (0.4.481: the town has signs of its own, which its own cases look at.)
                for(var sign:new CalderSigns.Sign[]{CalderSigns.Sign.ARCH,CalderSigns.Sign.RULES}){
                    var at=sign==CalderSigns.Sign.ARCH?b.offset(BikeNightSite.PIER_E,3,BikeNightSite.NORTH):b.offset(8,1,0);
                    for(int row=0;row<sign.rows;row++)for(int col=0;col<sign.cols;col++){var s=l.getBlockState(CalderSigns.cell(sign,at,Direction.NORTH,col,row));
                        h.assertTrue(CalderSignBlock.is(s,sign)&&s.getValue(CalderSignBlock.PIECE)==sign.first()+row*sign.cols+col&&s.getValue(CalderSignBlock.FACING)==Direction.NORTH,"the "+sign+" board, block "+col+","+row+": "+s);}
                }
                h.assertTrue(CalderSigns.Sign.ARCH.look().contains("CALDER PARK")&&CalderSigns.Sign.RULES.look().contains("No cycling")&&CalderSigns.Sign.RULES.look().contains("NO FUN"),"what a hand reads on each");
                // The gate: two leaves of two, shut and chained, the padlock on the park's side; as tall as the railings and a half over.
                int[] lower={0,2,3,0};Direction[] turned={Direction.NORTH,Direction.SOUTH,Direction.NORTH,Direction.SOUTH};
                for(int i=0;i<4;i++){var at=b.offset(BikeNightSite.PATH_W+i,0,BikeNightSite.NORTH);var s=l.getBlockState(at);var up=l.getBlockState(at.above());
                    h.assertTrue(CalderBlock.is(s,CalderBlock.Kind.GATE)&&s.getValue(CalderBlock.STAGE)==lower[i]&&s.getValue(CalderBlock.FACING)==turned[i],"the gate's lower block "+i+": "+s);
                    h.assertTrue(CalderBlock.is(up,CalderBlock.Kind.GATE)&&up.getValue(CalderBlock.STAGE)==1&&up.getValue(CalderBlock.FACING)==turned[i]&&up.getCollisionShape(l,at.above()).max(Direction.Axis.Y)==1.5,"and its upper, which stops a body half a block over its top: "+up);}
                // The railings: every block of the three lines is railing, pier or gate; the piers are capped; the corners are piers.
                int piers=0;
                for(int x=BikeNightSite.WEST;x<=BikeNightSite.EAST;x++){var at=b.offset(x,0,BikeNightSite.NORTH);var s=l.getBlockState(at);
                    if(s.is(brick)){piers++;h.assertTrue(l.getBlockState(at.above()).is(brick)&&CalderBlock.is(l.getBlockState(at.above(2)),CalderBlock.Kind.CAP),"a pier is brick two high under its cap, at x="+x);}
                    else if(x>=BikeNightSite.PATH_W&&x<=BikeNightSite.PATH_E)h.assertTrue(CalderBlock.is(s,CalderBlock.Kind.GATE),"the gateway at x="+x);
                    else h.assertTrue(CalderBlock.is(s,CalderBlock.Kind.FENCE)&&s.getValue(CalderBlock.STAGE)==0&&s.getValue(CalderBlock.FACING)==Direction.NORTH&&CalderBlock.is(l.getBlockState(at.above()),CalderBlock.Kind.FENCE)&&l.getBlockState(at.above()).getValue(CalderBlock.STAGE)==1,"railings two high along the street at x="+x+": "+s);}
                h.assertTrue(piers==10&&l.getBlockState(b.offset(BikeNightSite.WEST,0,BikeNightSite.NORTH)).is(brick)&&l.getBlockState(b.offset(BikeNightSite.EAST,0,BikeNightSite.NORTH)).is(brick)
                    &&l.getBlockState(b.offset(BikeNightSite.PIER_W,0,BikeNightSite.NORTH)).is(brick)&&l.getBlockState(b.offset(BikeNightSite.PIER_E,0,BikeNightSite.NORTH)).is(brick),"ten piers along the street, one at each corner and each side of the gate: "+piers);
                for(int x:new int[]{BikeNightSite.WEST,BikeNightSite.EAST})for(int z=BikeNightSite.NORTH+1;z<=0;z++){var s=l.getBlockState(b.offset(x,0,z));
                    h.assertTrue(s.is(brick)||CalderBlock.is(s,CalderBlock.Kind.FENCE)&&s.getValue(CalderBlock.FACING).getAxis()==Direction.Axis.X,"the side railings run unbroken to the back wall at "+x+","+z+": "+s);}
                // The lamps, the benches, the bin.
                for(var at:BikeNightSite.LAMPS){
                    var foot=l.getBlockState(b.offset(at));var top=b.offset(at).above(BikeNightSite.LAMP_TOP);
                    h.assertTrue(CalderBlock.is(foot,CalderBlock.Kind.LAMP)&&foot.getValue(CalderBlock.STAGE)==CalderBlock.FOOT&&l.getBlockState(b.offset(at).above()).getValue(CalderBlock.STAGE)==CalderBlock.POST&&l.getBlockState(b.offset(at).above(2)).getValue(CalderBlock.STAGE)==CalderBlock.POST,"a lamp post's foot and two of post at "+at);
                    h.assertTrue(BikeNightSite.lit(l,b,at)&&l.getBlockState(top).getLightEmission(l,top)==14,"and its lantern, lit, giving light");
                }
                h.assertTrue(CalderBlock.is(l.getBlockState(b.offset(3,0,-16)),CalderBlock.Kind.BENCH)&&l.getBlockState(b.offset(3,0,-16)).getValue(CalderBlock.STAGE)==1&&l.getBlockState(b.offset(3,0,-17)).getValue(CalderBlock.STAGE)==2
                    &&l.getBlockState(b.offset(3,0,-16)).getValue(CalderBlock.FACING)==Direction.WEST&&CalderBlock.is(l.getBlockState(b.offset(3,0,-18)),CalderBlock.Kind.BIN),"a bench two blocks long looking across the path, a bin by it");
                // The rack and the six bikes.
                for(var make:NightBike.Make.values()){
                    var rack=l.getBlockState(b.offset(BikeNightSite.RACK_X+make.ordinal(),0,BikeNightSite.RACK_Z));var bike=l.getBlockState(b.offset(BikeNightSite.bikeAt(make)));
                    h.assertTrue(rack.is(BikeNightRegistry.RACK.get())&&rack.getValue(BikeRackBlock.FACING)==Direction.NORTH,"the rack's place "+make.ordinal());
                    h.assertTrue(bike.is(BikeNightRegistry.BIKE_BLOCK.get())&&bike.getValue(NightBikeBlock.MAKE)==make&&bike.getValue(NightBikeBlock.FACING)==Direction.NORTH&&!bike.getValue(NightBikeBlock.LYING),"the "+make.id()+" on its stand in it: "+bike);
                }
                // The playground's sand in its edging, open where the side path comes in.
                h.assertTrue(l.getBlockState(b.offset(-16,-1,-22)).is(Blocks.SAND)&&l.getBlockState(b.offset(BikeNightSite.SAND_W,-1,BikeNightSite.SAND_N)).is(Blocks.SAND)&&l.getBlockState(b.offset(BikeNightSite.SAND_W-1,-1,BikeNightSite.SAND_N)).is(Blocks.GRASS_BLOCK),"sand, and grass round it");
                int[][] corner={{BikeNightSite.SAND_W,BikeNightSite.SAND_N},{BikeNightSite.SAND_E,BikeNightSite.SAND_N},{BikeNightSite.SAND_E,BikeNightSite.SAND_S},{BikeNightSite.SAND_W,BikeNightSite.SAND_S}};
                Direction[] faces={Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
                for(int i=0;i<4;i++){var s=l.getBlockState(b.offset(corner[i][0],0,corner[i][1]));h.assertTrue(CalderBlock.is(s,CalderBlock.Kind.BORDER)&&s.getValue(CalderBlock.STAGE)==1&&s.getValue(CalderBlock.FACING)==faces[i],"the edging turns the corner "+i+": "+s);}
                h.assertTrue(CalderBlock.is(l.getBlockState(b.offset(-16,0,BikeNightSite.SAND_N)),CalderBlock.Kind.BORDER)&&l.getBlockState(b.offset(BikeNightSite.SAND_E,0,BikeNightSite.SIDE_S)).isAir()&&l.getBlockState(b.offset(BikeNightSite.SAND_E,0,BikeNightSite.SIDE_N)).isAir()
                    &&l.getBlockState(b.offset(-5,-1,BikeNightSite.SIDE_S)).is(BikeNightRegistry.material("paving")),"timbers along the edge, and none across the side path");
                // Park Street.
                h.assertTrue(l.getBlockState(b.offset(10,-1,BikeNightSite.WALK_S)).is(BikeNightRegistry.material("paving"))&&l.getBlockState(b.offset(10,-1,BikeNightSite.KERB_S)).is(kerb)&&l.getBlockState(b.offset(10,-1,BikeNightSite.ROAD_S)).is(BikeNightRegistry.material("asphalt"))
                    &&l.getBlockState(b.offset(10,-1,BikeNightSite.LINE)).is(BikeNightRegistry.ROAD_LINE.get())&&l.getBlockState(b.offset(10,-1,BikeNightSite.LINE)).getValue(RotatedPillarBlock.AXIS)==Direction.Axis.X
                    &&l.getBlockState(b.offset(10,-1,BikeNightSite.KERB_N)).is(kerb)&&l.getBlockState(b.offset(10,-1,BikeNightSite.WALK_N)).is(BikeNightRegistry.material("paving")),"the pavement, the kerb, the road and its line, the far kerb and pavement");
                // Trees and what has come down from them; and nothing in the park that is one of the game's own blocks standing for something else.
                // (0.4.481: the place is the town as well now. This looks at the park and its street; the town's own case looks at the town.)
                int leaves=0,litter=0;var strangers=new java.util.TreeSet<String>();var r=P.room();
                for(var at:BlockPos.betweenClosed(b.offset(r.minX(),r.minY(),BikeNightSite.LINE-6),b.offset(r.maxX(),r.maxY(),r.maxZ()))){
                    var s=l.getBlockState(at);if(s.isAir())continue;
                    if(s.is(BikeNightRegistry.LEAVES.get()))leaves++;if(s.is(BikeNightRegistry.LITTER.get()))litter++;
                    var id=BuiltInRegistries.BLOCK.getKey(s.getBlock());
                    if(!id.getNamespace().equals(TheOldestHouse.MOD_ID)&&!s.is(Blocks.DIRT)&&!s.is(Blocks.GRASS_BLOCK)&&!s.is(Blocks.SAND)&&!s.is(Blocks.OAK_LOG)&&!(s.getBlock() instanceof DoorBlock))strangers.add(id.toString());
                }
                h.assertTrue(l.getBlockState(b.offset(-20,0,-5)).is(Blocks.OAK_LOG)&&leaves>400&&litter>60,"oaks in October leaf, and leaves down under them: "+leaves+" in the crowns, "+litter+" on the ground");
                h.assertTrue(strangers.isEmpty(),"the park is earth, grass, sand, oak and its own things, and nothing else: "+strangers);
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="park_arrive",timeoutTicks=1600)
    public static void aReaderComesInAtChildHeightAtMidnightAndReadsThePlan(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("park_reader",P))
            .thenWaitUntil(()->h.assertTrue(heard(p[0],"A town park after dark, on the last night of October"),"told where they are, in the bar over the hotbar"))
            .thenWaitUntil(()->h.assertTrue(Math.abs(p[0].getScale()-.54F)<.02F,"at a child's height: "+p[0].getScale()))
            .thenExecute(()->{
                var own=f.own(p[0],P);
                h.assertTrue(LiteraryVignettes.current(p[0])==P&&own.getInt("Nights")==1&&own.getString("Bike").isEmpty(),"in the park, on their first night there, with no bike yet: "+own);
                h.assertTrue(BikeNightSite.inPark(p[0].position().subtract(b.getX(),b.getY(),b.getZ()))&&!BikeNightSite.onSand(p[0].position().subtract(b.getX(),b.getY(),b.getZ())),"inside the railings, on the path");
                f.source(p[0],P);
                h.assertTrue(f.own(p[0],P).getBoolean("Read_Source"),"the sheet by the door is read to its end");
                h.assertTrue(BikeNightTexts.HINT.equals(LiteraryTexts.hint(P)),"and its hint is the rack's rule");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="park_bikes",timeoutTicks=2400)
    public static void whicheverBikeAReaderGetsOnIsTheirsForTheNight(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final NightBike[] bike={null,null};
        final BlockPos[] left={null};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{p[0]=f.player("park_first",P);p[1]=f.player("park_second",P);f.at(p[0],P,4.5,0,-40.5);f.at(p[1],P,6.5,0,-40.5);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.owner(b,NightBike.Make.WASP)==null&&BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun, and nobody has a bike"))
            .thenExecute(()->{
                var wasp=b.offset(BikeNightSite.bikeAt(NightBike.Make.WASP));
                hand(f,p[0],wasp);h.assertTrue(!p[0].isPassenger()&&BikeNight.owner(b,NightBike.Make.WASP)==null,"a first hand on a bike is a look at it, and takes nothing");
                bike[0]=take(h,f,p[0],NightBike.Make.WASP);
            })
            .thenWaitUntil(()->h.assertTrue(p[0].getUUID().equals(BikeNight.owner(b,NightBike.Make.WASP))&&BikeNight.bikeAt(b,NightBike.Make.WASP)==null&&f.own(p[0],P).getString("Bike").equals("wasp"),"on it, it is theirs, and their record says which"))
            .thenExecute(()->{hush(p[0]);BikeNight.tellNow(b);})
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.YOURS),"a little after getting on, they are told it is theirs"))
            .thenExecute(()->getOff(p[0])).thenIdle(3)
            .thenExecute(()->{
                stand(p[0]);left[0]=BikeNight.bikeAt(b,NightBike.Make.WASP);
                h.assertTrue(!p[0].isPassenger()&&left[0]!=null&&l.getBlockState(left[0]).is(BikeNightRegistry.BIKE_BLOCK.get())&&l.getBlockState(left[0]).getValue(NightBikeBlock.MAKE)==NightBike.Make.WASP,"off it, the night knows where it stands: "+left[0]);
                h.assertTrue(p[0].getUUID().equals(BikeNight.owner(b,NightBike.Make.WASP)),"and it is still theirs");
                // The other reader cannot have it.
                hush(p[1]);hand(f,p[1],left[0]);
            })
            .thenWaitUntil(()->h.assertTrue(!p[1].isPassenger()&&heard(p[1],"park_first has that one tonight."),"somebody else's bike is refused, with whose it is"))
            .thenExecute(()->{
                hush(p[1]);hand(f,p[1],left[0]);h.assertTrue(!p[1].isPassenger(),"however often it is tried");
                bike[1]=take(h,f,p[1],NightBike.Make.HARE);
            })
            .thenWaitUntil(()->h.assertTrue(p[1].getUUID().equals(BikeNight.owner(b,NightBike.Make.HARE))&&f.own(p[1],P).getString("Bike").equals("hare"),"the second reader has the Hare"))
            // The first reader changes their mind: the Comet is theirs, and the Wasp is anybody's again.
            .thenExecute(()->bike[0]=take(h,f,p[0],NightBike.Make.COMET))
            .thenWaitUntil(()->h.assertTrue(p[0].getUUID().equals(BikeNight.owner(b,NightBike.Make.COMET))&&BikeNight.owner(b,NightBike.Make.WASP)==null&&f.own(p[0],P).getString("Bike").equals("comet")&&f.own(p[0],P).getInt("Swaps")==1,
                "getting on another that nobody has gives the first back"))
            .thenExecute(()->{hush(p[0]);BikeNight.tellNow(b);})
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.SWAPPED),"and they are told so"))
            .thenExecute(()->{getOff(p[1]);})
            .thenIdle(3)
            .thenExecute(()->{
                stand(p[1]);h.assertTrue(!p[1].isPassenger(),"the second reader is off the Hare");
                hand(f,p[1],left[0]);if(!p[1].isPassenger())hand(f,p[1],left[0]);
                h.assertTrue(p[1].getVehicle() instanceof NightBike w&&w.make()==NightBike.Make.WASP,"and may now take the Wasp");
            })
            .thenWaitUntil(()->h.assertTrue(p[1].getUUID().equals(BikeNight.owner(b,NightBike.Make.WASP))&&BikeNight.owner(b,NightBike.Make.HARE)==null&&f.own(p[1],P).getString("Bike").equals("wasp"),"which is theirs, and the Hare nobody's"))
            .thenExecute(()->{
                h.assertTrue(BikeNightTexts.account("x",f.own(p[1],P)).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().anyMatch(pg->pg.raw().getString().contains("I took the Wasp")),"their account has the bike they ended with");
                h.assertTrue(LiteraryTexts.accountTitle(P).equals("The account: Calder Falls"),"and is titled with the town");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="park_lamp",timeoutTicks=1600)
    public static void theFailingLampGoesOutAndComesBackAndTheOthersStayLit(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final int[] before={0};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("park_lamp",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null&&BikeNight.failingLampLit(b)&&BikeNightSite.lit(l,b,BikeNightSite.FLICKER),"the night has begun, with every lamp lit"))
            .thenExecute(()->{before[0]=BikeNight.outages(b);BikeNight.lampNow(b);})
            .thenWaitUntil(()->h.assertTrue(!BikeNight.failingLampLit(b)&&!BikeNightSite.lit(l,b,BikeNightSite.FLICKER)&&BikeNight.outages(b)==before[0]+1,"the failing lamp goes out"))
            .thenExecute(()->{
                var top=b.offset(BikeNightSite.FLICKER).above(BikeNightSite.LAMP_TOP);
                h.assertTrue(l.getBlockState(top).getValue(CalderBlock.STAGE)==CalderBlock.DARK&&l.getBlockState(top).getLightEmission(l,top)==0,"its lantern dark, and giving no light");
                for(var at:BikeNightSite.LAMPS)if(!at.equals(BikeNightSite.FLICKER))h.assertTrue(BikeNightSite.lit(l,b,at),"the others stay lit: "+at);
                BikeNight.lampNow(b);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.failingLampLit(b)&&BikeNightSite.lit(l,b,BikeNightSite.FLICKER),"and comes back"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="park_empty",timeoutTicks=2400)
    public static void whenTheParkStandsEmptyTheNightIsOverAndTheBikesAreBackInTheRack(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final BlockPos[] left={null};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{p[0]=f.player("park_last",P);f.at(p[0],P,9.5,0,-40.5);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.RATTLER)!=null,"the night has begun"))
            .thenExecute(()->{var bike=take(h,f,p[0],NightBike.Make.RATTLER);bike.moveTo(b.getX()+12.5,b.getY(),b.getZ()-20.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(p[0].getUUID().equals(BikeNight.owner(b,NightBike.Make.RATTLER)),"the Rattler is theirs"))
            .thenExecute(()->getOff(p[0])).thenIdle(3)
            .thenExecute(()->{
                stand(p[0]);left[0]=BikeNight.bikeAt(b,NightBike.Make.RATTLER);
                h.assertTrue(left[0]!=null&&!left[0].equals(b.offset(BikeNightSite.bikeAt(NightBike.Make.RATTLER)))&&l.getBlockState(left[0]).is(BikeNightRegistry.BIKE_BLOCK.get()),"left out on the grass, away from the rack: "+left[0]);
                var saved=LiteraryVignettes.shared(f.data(),P).getCompound("Bikes_"+b.asLong());
                h.assertTrue(saved.getLong("rattler")==left[0].asLong()&&saved.contains("wasp"),"where each bike is, is kept with the world: "+saved);
                // The reader goes: out of the park altogether.
                p[0].teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+40,f.origin.getZ()+.5,0,0);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.RATTLER)==null&&BikeNight.owner(b,NightBike.Make.RATTLER)==null,"after a while empty, the night is over"))
            .thenExecute(()->{
                var home=b.offset(BikeNightSite.bikeAt(NightBike.Make.RATTLER));
                h.assertTrue(l.getBlockState(home).is(BikeNightRegistry.BIKE_BLOCK.get())&&l.getBlockState(home).getValue(NightBikeBlock.MAKE)==NightBike.Make.RATTLER&&!l.getBlockState(left[0]).is(BikeNightRegistry.BIKE_BLOCK.get()),"the Rattler is back in the rack, and not where it was left");
                h.assertTrue(BikeNightSite.lit(l,b,BikeNightSite.FLICKER),"and the failing lamp is lit for whoever comes next");
                h.assertTrue(f.own(p[0],P).getString("Bike").equals("rattler"),"the reader's own record keeps the bike they had");
                // A night cut short by the server stopping: the bike is found where the world says it was left, and taken back.
                var stray=b.offset(-12,0,-6);l.setBlock(stray,NightBikeBlock.of(NightBike.Make.LAMP,Direction.EAST,true),3);l.setBlock(b.offset(BikeNightSite.bikeAt(NightBike.Make.LAMP)),Blocks.AIR.defaultBlockState(),3);
                var world=LiteraryVignettes.shared(f.data(),P);var t=world.getCompound("Bikes_"+b.asLong());t.putLong("lamp",stray.asLong());world.put("Bikes_"+b.asLong(),t);LiteraryVignettes.shared(f.data(),P,world);
                left[0]=stray;f.arrive(p[0],P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.LAMP)!=null&&BikeNight.bikeAt(b,NightBike.Make.LAMP).equals(b.offset(BikeNightSite.bikeAt(NightBike.Make.LAMP)))
                &&l.getBlockState(b.offset(BikeNightSite.bikeAt(NightBike.Make.LAMP))).is(BikeNightRegistry.BIKE_BLOCK.get())&&l.getBlockState(left[0]).isAir(),"the next night begins with the Lamp back in the rack and nothing left lying where it was"))
            .thenExecute(()->h.assertTrue(f.own(p[0],P).getInt("Nights")==2,"their second night in the park"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="park_hands",timeoutTicks=1600)
    public static void aHandOnTheParksThingsSaysWhatIsThere(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};
        record Thing(BlockPos at,String says){}
        var things=List.of(new Thing(new BlockPos(BikeNightSite.PATH_W+1,0,BikeNightSite.NORTH),BikeNightTexts.GATE),new Thing(new BlockPos(-6,0,BikeNightSite.NORTH),BikeNightTexts.RAILINGS),
            new Thing(BikeNightSite.FLICKER,BikeNightTexts.LAMP_FAILING),new Thing(BikeNightSite.PLAY_LAMP.above(),BikeNightTexts.LAMP),new Thing(new BlockPos(3,0,-16),BikeNightTexts.BENCH),
            new Thing(new BlockPos(3,0,-18),BikeNightTexts.BIN),new Thing(new BlockPos(BikeNightSite.RACK_X+2,0,BikeNightSite.RACK_Z),BikeNightTexts.RACK),new Thing(new BlockPos(-16,0,BikeNightSite.SAND_N),BikeNightTexts.BORDER),
            new Thing(new BlockPos(6,1,0),CalderSigns.Sign.RULES.look()),new Thing(new BlockPos(0,3,BikeNightSite.NORTH),CalderSigns.Sign.ARCH.look()),
            new Thing(BikeNightSite.SLIDE,BikeNightTexts.SLIDE),new Thing(BikeNightSite.slideEnd(),BikeNightTexts.SLIDE),
            new Thing(BikeNightSite.barsLadder(0),BikeNightTexts.BARS),new Thing(BikeNightSite.barsLadder(1).above(),BikeNightTexts.BARS),new Thing(BikeNightSite.rungsAt(2),BikeNightTexts.BARS_FAR),new Thing(BikeNightSite.SEESAW,BikeNightTexts.SEESAW),new Thing(BikeNightSite.ROUNDABOUT,BikeNightTexts.ROUNDABOUT),
            new Thing(BikeNightSite.DOME.above(),BikeNightTexts.DOME),new Thing(new BlockPos(-5,0,-23),BikeNightTexts.CARVED),new Thing(BikeNightSite.FOUNTAIN,BikeNightTexts.FOUNTAIN),new Thing(BikeNightSite.PHONE,BikeNightTexts.PHONE),
            new Thing(new BlockPos(BikeNightSite.SWING_X,1,BikeNightSite.SWING_POSTS[1]),BikeNightTexts.SWINGS),new Thing(new BlockPos(BikeNightSite.SWING_X,BikeNightSite.SWING_BAR,BikeNightSite.SWING_POSTS[1]+1),BikeNightTexts.SWINGS));
        var seq=h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("park_hand",P));
        for(var thing:things)seq=seq.thenExecute(()->{hush(p[0]);f.click(p[0],P,thing.at());})
            .thenWaitUntil(()->h.assertTrue(heard(p[0],thing.says()),"a hand at "+thing.at().toShortString()+" says: "+thing.says()));
        seq.thenExecute(()->{
            for(var line:List.of(BikeNightTexts.GATE,BikeNightTexts.RAILINGS,BikeNightTexts.LAMP,BikeNightTexts.LAMP_FAILING,BikeNightTexts.BENCH,BikeNightTexts.BIN,BikeNightTexts.RACK,BikeNightTexts.BORDER,BikeNightTexts.ARRIVE,BikeNightTexts.YOURS,BikeNightTexts.SWAPPED,CalderSigns.Sign.RULES.look(),CalderSigns.Sign.ARCH.look(),
                BikeNightTexts.SWINGS,BikeNightTexts.SWING_HINT,BikeNightTexts.JUMP,BikeNightTexts.JUMP_LEVEL,BikeNightTexts.JUMP_BEST,BikeNightTexts.JUMP_OFF,BikeNightTexts.MARK,BikeNightTexts.MARK_OWN,BikeNightTexts.MARK_THEIRS,
                BikeNightTexts.SAND,BikeNightTexts.SAND_FULL,BikeNightTexts.SAND_NO_ROOM,BikeNightTexts.SAND_GONE,BikeNightTexts.SLIDE,
                BikeNightTexts.BARS,BikeNightTexts.BARS_LOW,BikeNightTexts.BARS_ON,BikeNightTexts.BARS_FAR,BikeNightTexts.BARS_HINT,BikeNightTexts.BARS_ACROSS,BikeNightTexts.BARS_EARLY,BikeNightTexts.SEESAW_THROWN,BikeNightTexts.SEESAW_THREW,
                BikeNightTexts.SEESAW,BikeNightTexts.SEESAW_TAKEN,BikeNightTexts.SEESAW_HINT,
                BikeNightTexts.ROUNDABOUT,BikeNightTexts.ROUNDABOUT_ON,BikeNightTexts.ROUNDABOUT_FAR,BikeNightTexts.ROUNDABOUT_HINT,
                BikeNightTexts.DOME,BikeNightTexts.DOME_TOP,BikeNightTexts.SPRING,BikeNightTexts.SPRING_TAKEN,BikeNightTexts.SPRING_HINT,
                BikeNightTexts.CARVED,BikeNightTexts.FOUNTAIN,BikeNightTexts.FOUNTAIN_RUNNING,BikeNightTexts.PHONE,BikeNightTexts.PHONE_HELD,
                HouseReadSwing.PUSH_HINT,HouseReadSwing.PUSH_EARLY,HouseReadSwing.PUSH_FAR,HouseReadSwing.PUSH_BESIDE,HouseReadSwing.PUSHED_BY,
                BikeNightTexts.SCRATCH_ADD,BikeNightTexts.SCRATCH_OWN,BikeNightTexts.SCRATCH_FRESH,BikeNightTexts.OVER_BAR,BikeNightTexts.OVER_SEEN,
                BikeNightTexts.GATE_OPENED,BikeNightTexts.GET_THEM_CUE,BikeNightTexts.GATE_OPEN,BikeNightTexts.SLIDE_BIKE,BikeNightTexts.HUNG_CAP)){
                var low=line.toLowerCase(java.util.Locale.ROOT);
                for(var word:List.of(" red"," green"," blue"," yellow"," orange"," black"," white"," grey"," gray"," brown"," he "," she "," his "," her "," boy"," girl"," afraid"," scared"," you feel"))
                    h.assertTrue(!low.contains(word),"the park's own lines name no colour, gender the reader never, and do not say how they feel: \""+line+"\" has \""+word.trim()+"\"");
            }
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="park_rails",timeoutTicks=2400)
    public static void theBestHopThereIsDoesNotClearTheRailings(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final NightBike[] bike={null};final double[] high={0};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{p[0]=f.player("park_hopper",P);f.at(p[0],P,7.5,0,-40.5);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.HOPPER)!=null,"the night has begun"))
            .thenExecute(()->{
                bike[0]=take(h,f,p[0],NightBike.Make.HOPPER);
                // Out on the grass east of the path, pointing at the east railings, with a run-up.
                bike[0].moveTo(b.getX()+22.5,b.getY(),b.getZ()-12.5,270,0);p[0].setYRot(270);bike[0].keys(true,false);
            })
            .thenExecuteFor(RiddenBike.CHARGE+2,()->{p[0].setYRot(270);pedalWell(bike[0],bike[0].tickCount,3);})
            .thenExecute(()->bike[0].keys(false,false))
            .thenExecuteFor(60,()->{
                p[0].setYRot(270);if(p[0].getVehicle()==bike[0]){pedalWell(bike[0],bike[0].tickCount,3);high[0]=Math.max(high[0],bike[0].getY()-b.getY());
                    if(bike[0].onGround()&&bike[0].getX()>b.getX()+BikeNightSite.EAST-6){bike[0].keys(true,false);}}
            })
            .thenExecute(()->{
                h.assertTrue(p[0].getX()<b.getX()+BikeNightSite.EAST+.5&&(bike[0].isRemoved()||bike[0].getX()<b.getX()+BikeNightSite.EAST+.5),"neither the rider nor the bike is past the east railings: rider "+(p[0].getX()-b.getX())+", highest "+high[0]);
                h.assertTrue(BikeNightSite.inPark(p[0].position().subtract(b.getX(),b.getY(),b.getZ())),"the rider is still in the park");
            }).thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.465: the swings and the sand
    private static BlockPos swing(BlockPos b,int seat){return b.offset(BikeNightSite.swingAt(seat));}
    private static int balls(ServerPlayer p){return p.getInventory().countItem(BikeNightRegistry.SAND_BALL.get());}

    @GameTest(template="empty",batch="park_swings",timeoutTicks=2400)
    public static void theSwingsHangFromTheirFrameAndTwoReadersSwingSideBySide(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final SeatEntity[] seat={null,null};
        var forward=BikeNightSite.SWING_FORWARD;var behind=forward.getOpposite();int x=BikeNightSite.SWING_X,top=BikeNightSite.SWING_BAR;
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{
                // The frame: three posts on the sand, a bar over them, and under the bar two seats to a bay, each on its chains.
                int first=BikeNightSite.SWING_POSTS[0],last=BikeNightSite.SWING_POSTS[BikeNightSite.SWING_POSTS.length-1],seats=0;
                for(int z=first;z<=last;z++){
                    boolean post=false,hung=false;for(int at:BikeNightSite.SWING_POSTS)post|=at==z;for(int at:BikeNightSite.SWING_SEATS)hung|=at==z;
                    var foot=l.getBlockState(b.offset(x,0,z));var up=l.getBlockState(b.offset(x,top,z));
                    h.assertTrue(l.getBlockState(b.offset(x,-1,z)).is(Blocks.SAND),"the swings stand on the playground's sand at z="+z);
                    if(post){
                        h.assertTrue(CalderPlayBlock.is(foot,CalderPlayBlock.Kind.POST)&&foot.getValue(CalderPlayBlock.STAGE)==CalderPlayBlock.FOOT&&foot.getValue(CalderPlayBlock.FACING).getAxis()==forward.getAxis(),"a post's foot, its stays the way the swings go, at z="+z+": "+foot);
                        for(int y=1;y<top;y++){var s=l.getBlockState(b.offset(x,y,z));h.assertTrue(CalderPlayBlock.is(s,CalderPlayBlock.Kind.POST)&&s.getValue(CalderPlayBlock.STAGE)==CalderPlayBlock.SHAFT,"its shaft at y="+y+": "+s);}
                        int stage=z==first||z==last?CalderPlayBlock.END:CalderPlayBlock.THROUGH;
                        // A bar goes off to the east of an end post facing north: turned, to the south of the north post and the north of the south one.
                        var want=z==first?Direction.EAST:z==last?Direction.WEST:behind;
                        h.assertTrue(CalderPlayBlock.is(up,CalderPlayBlock.Kind.POST)&&up.getValue(CalderPlayBlock.STAGE)==stage&&up.getValue(CalderPlayBlock.FACING)==want,"its top, the bar ending on it or running on over it, at z="+z+": "+up);
                        h.assertTrue(!foot.getCollisionShape(l,b.offset(x,0,z)).isEmpty(),"a post stops a body");
                        continue;
                    }
                    h.assertTrue(CalderPlayBlock.is(up,CalderPlayBlock.Kind.BAR)&&up.getValue(CalderPlayBlock.STAGE)==(hung?CalderPlayBlock.HANGERS:0)&&up.getValue(CalderPlayBlock.FACING).getAxis()==forward.getAxis(),"the bar, with hangers only over a seat, at z="+z+": "+up);
                    if(!hung){for(int y=0;y<top;y++)h.assertTrue(l.getBlockState(b.offset(x,y,z)).isAir(),"nothing under the bar between a seat and a post at z="+z+" y="+y);continue;}
                    seats++;
                    h.assertTrue(CalderPlayBlock.is(foot,CalderPlayBlock.Kind.SWING)&&foot.getValue(CalderPlayBlock.STAGE)==0&&foot.getValue(CalderPlayBlock.FACING)==behind&&HouseReadSwing.is(foot)&&HouseReadSwing.pumped(foot)&&HouseReadSwing.forward(foot)==forward
                        &&HouseSitting.isSeat(foot)&&foot.getCollisionShape(l,b.offset(x,0,z)).isEmpty()&&!foot.getShape(l,b.offset(x,0,z)).isEmpty(),"a belt seat looking out over the sand, to be sat on, and no bar to a body, at z="+z+": "+foot);
                    for(int y=1;y<top;y++){var s=l.getBlockState(b.offset(x,y,z));h.assertTrue(CalderPlayBlock.is(s,CalderPlayBlock.Kind.CHAIN)&&s.getValue(CalderPlayBlock.STAGE)==0&&s.getValue(CalderPlayBlock.FACING)==behind&&s.getCollisionShape(l,b.offset(x,y,z)).isEmpty(),"its chains at y="+y+": "+s);}
                    h.assertTrue(HouseReadSwing.pair(l,b.offset(x,0,z)).equals(List.of(b.offset(x,0,z)))&&HouseReadSwing.pivot(HouseReadSwing.pair(l,b.offset(x,0,z))).equals(new Vec3(b.getX()+x+.5,b.getY()+top,b.getZ()+z+.5)),"one block of seat, hung from the underside of the bar");
                }
                h.assertTrue(seats==4&&BikeNightSite.LAMPS.contains(BikeNightSite.SWING_LAMP)&&BikeNightSite.lit(l,b,BikeNightSite.SWING_LAMP),"four swings, and a lamp of their own behind them");
                var gone=CalderPlayBlock.of(CalderPlayBlock.Kind.SWING,behind,CalderPlayBlock.GONE);
                h.assertTrue(gone.getShape(l,BlockPos.ZERO).isEmpty()&&CalderPlayBlock.of(CalderPlayBlock.Kind.CHAIN,behind,CalderPlayBlock.GONE).getShape(l,BlockPos.ZERO).isEmpty()&&HouseSitting.isSeat(gone),"a seat somebody is on is not there to be touched, and is still a seat");
                p[0]=f.player("swing_one",P);p[1]=f.player("swing_two",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenExecute(()->{
                hush(p[0]);f.click(p[0],P,BikeNightSite.swingAt(0));
                h.assertTrue(p[0].getVehicle() instanceof SeatEntity s&&s.swinging()&&s.pumped()&&s.reach()==0&&s.support().equals(swing(b,0)),"a hand on a seat sits on it, and the swing starts from still");
                seat[0]=(SeatEntity)p[0].getVehicle();
                h.assertTrue(Mth.degreesDifferenceAbs(p[0].getYRot(),forward.toYRot())<1,"looking out over the sand: "+p[0].getYRot());
                var s=l.getBlockState(swing(b,0));
                h.assertTrue(CalderPlayBlock.is(s,CalderPlayBlock.Kind.SWING)&&s.getValue(CalderPlayBlock.STAGE)==CalderPlayBlock.GONE,"the still seat gives way to the moving one: "+s);
                for(int y=1;y<top;y++)h.assertTrue(l.getBlockState(swing(b,0).above(y)).getValue(CalderPlayBlock.STAGE)==CalderPlayBlock.GONE,"and so do its chains, at y="+y);
                h.assertTrue(HouseReadSwing.still(s).getValue(CalderPlayBlock.STAGE)==0&&CalderPlayBlock.is(HouseReadSwing.hanger(s),CalderPlayBlock.Kind.CHAIN)&&HouseReadSwing.hanger(s).getValue(CalderPlayBlock.FACING)==behind,"the moving swing is drawn from the seat and its chains");
                // Nobody else gets on it; the next one along is free, with somebody swinging beside it.
                f.at(p[1],P,x+1.5,0,BikeNightSite.SWING_SEATS[0]+.5);
                h.assertTrue(!HouseSitting.sit(p[1],swing(b,0))&&!p[1].isPassenger(),"nobody else gets on a swing somebody is on");
            })
            .thenIdle(30)
            .thenExecute(()->{
                h.assertTrue(!HouseSitting.sit(p[1],swing(b,0)),"nor while it is away from where it hangs");
                f.click(p[1],P,BikeNightSite.swingAt(1));
                h.assertTrue(p[1].getVehicle() instanceof SeatEntity s&&s!=seat[0]&&s.support().equals(swing(b,1))&&p[0].getVehicle()==seat[0],"the next swing along takes a second reader while the first swings");
                seat[1]=(SeatEntity)p[1].getVehicle();
            })
            .thenIdle(40)
            .thenExecute(()->{
                // Left alone a swing comes up to a gentle arc, and the seat carries its reader along it.
                double reach=seat[0].reach();
                h.assertTrue(reach>.2&&reach<=HouseReadSwing.REACH&&seat[1].reach()>.1&&seat[1].reach()<reach,"left alone each comes up towards a gentle arc: "+reach+", "+seat[1].reach());
                var pivot=HouseReadSwing.pivot(List.of(swing(b,0)));
                double now=seat[0].position().distanceTo(HouseReadSwing.seat(pivot,forward,seat[0].swingAngle(0))),before=seat[0].position().distanceTo(HouseReadSwing.seat(pivot,forward,seat[0].swingAngle(-1)));
                h.assertTrue(Math.min(now,before)<1e-6&&Math.abs(seat[0].getZ()-pivot.z)<1e-6,"the seat is where the arc puts it, and swings the way it faces: "+now+" "+before);
                h.assertTrue(p[0].getVehicle()==seat[0]&&seat[0].getX()!=seat[1].getX(),"each on their own arc");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.SWING_HINT)&&f.own(p[0],P).getBoolean("Swung")&&f.own(p[0],P).getBoolean("SwingTold"),"told once how a swing is taken higher and let go of, and it is in their record"))
            .thenWaitUntil(()->h.assertTrue(sounded(p[0],"park.swing_creak"),"the chains are heard as it comes through the bottom"))
            .thenExecute(()->{p[0].stopRiding();})
            .thenIdle(3)
            .thenExecute(()->{
                var s=l.getBlockState(swing(b,0));
                h.assertTrue(seat[0].isRemoved()&&s.getValue(CalderPlayBlock.STAGE)==0&&l.getBlockState(swing(b,0).above(2)).getValue(CalderPlayBlock.STAGE)==0,"off it, the swing hangs still again: "+s);
                h.assertTrue(p[1].getVehicle()==seat[1]&&l.getBlockState(swing(b,1)).getValue(CalderPlayBlock.STAGE)==CalderPlayBlock.GONE,"and the one beside it swings on");
                // A swing left as if somebody were on it (a server stopped under a reader) hangs still again when the night begins.
                p[1].stopRiding();seat[1].discard();
                h.assertTrue(l.getBlockState(swing(b,1)).getValue(CalderPlayBlock.STAGE)==CalderPlayBlock.GONE,"a seat whose rider went with the server is left as it was");
                BikeNight.resetNow(l,b);
                h.assertTrue(l.getBlockState(swing(b,1)).getValue(CalderPlayBlock.STAGE)==0&&l.getBlockState(swing(b,1).above()).getValue(CalderPlayBlock.STAGE)==0,"and is put back with the night");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="park_pump",timeoutTicks=3000)
    public static void aSwingPumpedHighThrowsItsRiderOutOverTheSandAndTheFurthestIsMarked(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final SeatEntity[] seat={null};
        final double[] high={0,0};final Vec3[] away={null};final int[] paces={0,0};final BlockPos[] mark={null};var forward=BikeNightSite.SWING_FORWARD;
        // 0.4.475: leaning the way it goes, but not to the tick: the lean is let go for a few ticks either side of each turn. (A lean
        // thrown over exactly as the swing turns, end after end, is another matter: it goes over the bar, and has its own case.)
        Runnable with=()->{double going=Math.cos((seat[0].swingTicks(0)+1)*HouseReadSwing.RATE);p[0].setPlayerInput(0,Math.abs(going)<Math.sin(4.5*HouseReadSwing.RATE)?0:going>0?1F:-1F,false,false);high[0]=Math.max(high[0],seat[0].reach());high[1]=Math.max(high[1],seat[0].getY()-b.getY());};
        Runnable against=()->{double going=Math.cos((seat[0].swingTicks(0)+1)*HouseReadSwing.RATE);p[0].setPlayerInput(0,going>0?-1F:1F,false,false);};
        // The fixture's readers are weightless, so that they stay where a case puts them. One in the air off a swing has to fall.
        Runnable fly=()->{if(BikeNight.inFlight(p[0])){p[0].setNoGravity(false);p[0].travel(Vec3.ZERO);}else p[0].setNoGravity(true);};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("swing_high",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenExecute(()->{f.click(p[0],P,BikeNightSite.swingAt(2));seat[0]=(SeatEntity)p[0].getVehicle();h.assertTrue(seat[0]!=null&&seat[0].pumped(),"on a swing");})
            // Leaning the way it goes, each way, takes it as high as it will go; and no higher.
            .thenExecuteFor(420,with)
            .thenExecute(()->{
                h.assertTrue(Math.abs(seat[0].reach()-HouseReadSwing.HIGH)<.02&&high[0]<=HouseReadSwing.HIGH+1e-9&&seat[0].kicks()==0&&HouseReadSwing.HIGH>HouseReadSwing.REACH*2,"leaning with it takes the arc to its widest, well past a gentle one: "+seat[0].reach());
                h.assertTrue(high[1]>2.3&&high[1]<3,"which carries the seat well over two blocks up at each end: "+high[1]);
            })
            // Leaning against it brings it down.
            .thenExecuteFor(50,against)
            .thenExecute(()->{
                h.assertTrue(seat[0].reach()<HouseReadSwing.HIGH-.2,"leaning against it narrows the arc: "+seat[0].reach());
                p[0].setPlayerInput(0,0,false,false);seat[0].reach(HouseReadSwing.HIGH);
            })
            // Let go on the way up and forward.
            .thenWaitUntil(()->{double a=seat[0].swingAngle(0);h.assertTrue(a>.55&&a<.75&&seat[0].swingAngle(1)>a,"waiting for the upswing: "+a);})
            .thenExecute(()->{
                hush(p[0]);away[0]=HouseReadSwing.jump(p[0]);
                h.assertTrue(away[0]!=null&&!p[0].isPassenger()&&away[0].x>.6&&away[0].y>.5&&Math.abs(away[0].z)<.01&&p[0].getDeltaMovement().equals(away[0]),"letting go there throws the reader up and out the way the swing faces: "+away[0]);
                h.assertTrue(BikeNight.inFlight(p[0])&&BikeNight.far(b)==0&&BikeNight.mark(b)==null,"in the air, and nobody has landed yet tonight");
            })
            .thenExecuteFor(70,fly)
            .thenExecute(()->{
                var r=p[0].position().subtract(b.getX(),b.getY(),b.getZ());double out=r.x-(BikeNightSite.SWING_X+.5);paces[0]=(int)Math.floor(out*BikeNight.PACES);
                h.assertTrue(p[0].onGround()&&!BikeNight.inFlight(p[0])&&BikeNightSite.onSand(r)&&out>5&&Math.abs(r.z-(BikeNightSite.SWING_SEATS[2]+.5))<.5,"down on the sand, more than five blocks out from under the bar: "+r);
                h.assertTrue(BikeNight.far(b)==paces[0]&&p[0].getUUID().equals(BikeNight.farWho(b)),"paced out from under the bar, a child's paces, and the furthest tonight: "+BikeNight.far(b)+" for "+out);
                mark[0]=BikeNight.mark(b);var s=mark[0]==null?null:l.getBlockState(mark[0]);
                h.assertTrue(mark[0]!=null&&mark[0].equals(p[0].blockPosition())&&s.is(BikeNightRegistry.MARK.get())&&s.getValue(CalderLitterBlock.FACING)==forward&&s.canBeReplaced()&&s.getCollisionShape(l,mark[0]).isEmpty(),"a heel mark in the sand where they came down, across the way they were going, that gives way to whatever is set down on it: "+s);
                // The sand is soft, and only the sand.
                var soft=new net.neoforged.neoforge.event.entity.living.LivingFallEvent(p[0],6F,1F);BikeNight.fall(soft);
                h.assertTrue(soft.getDamageMultiplier()==0&&p[0].getHealth()==p[0].getMaxHealth(),"nobody is hurt coming down on the playground's sand");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],String.format(BikeNightTexts.JUMP_BEST,io.github.knaitoe.theoldesthouse.labyrinth.Stillness.words(paces[0]))),"told how far, and that nobody has landed further"))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt("SwingBest")==paces[0],"and it is in their record"))
            .thenExecute(()->{
                // A hand on the mark says whose it is.
                hush(p[0]);f.click(p[0],P,mark[0].subtract(b));
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.MARK_OWN),"a hand on the mark says whose it is"))
            // A second jump, off a gentle arc: shorter. The mark stays where it is.
            .thenExecute(()->{
                f.click(p[0],P,BikeNightSite.swingAt(3));seat[0]=(SeatEntity)p[0].getVehicle();h.assertTrue(seat[0]!=null&&seat[0].support().equals(swing(b,3)),"on another swing");
                seat[0].reach(HouseReadSwing.REACH);
            })
            .thenWaitUntil(()->{double a=seat[0].swingAngle(0);h.assertTrue(a>.3&&seat[0].swingAngle(1)>a,"waiting for the upswing: "+a);})
            .thenExecute(()->{hush(p[0]);HouseReadSwing.jump(p[0]);})
            .thenExecuteFor(70,fly)
            .thenExecute(()->{
                var r=p[0].position().subtract(b.getX(),b.getY(),b.getZ());paces[1]=(int)Math.floor((r.x-(BikeNightSite.SWING_X+.5))*BikeNight.PACES);
                h.assertTrue(p[0].onGround()&&paces[1]>=BikeNight.LEAST&&paces[1]<paces[0],"a jump off a gentle arc comes down nearer: "+paces[1]+" against "+paces[0]);
                h.assertTrue(BikeNight.far(b)==paces[0]&&mark[0].equals(BikeNight.mark(b))&&l.getBlockState(mark[0]).is(BikeNightRegistry.MARK.get()),"the furthest is still the furthest, and its mark is where it was");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],String.format(BikeNightTexts.JUMP,io.github.knaitoe.theoldesthouse.labyrinth.Stillness.words(paces[1]),io.github.knaitoe.theoldesthouse.labyrinth.Stillness.words(paces[0]))),"told how far, and how far the furthest is"))
            .thenExecute(()->{
                h.assertTrue(f.own(p[0],P).getInt("SwingBest")==paces[0],"their record keeps their best");
                var account=String.join(" ",BikeNightTexts.account("swing_high",f.own(p[0],P)).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(account.contains("let go of it")&&account.contains(io.github.knaitoe.theoldesthouse.labyrinth.Stillness.words(paces[0])+" paces"),"and their account has it: "+account);
                // The night over: the mark is gone with it.
                BikeNight.resetNow(l,b);
                h.assertTrue(!l.getBlockState(mark[0]).is(BikeNightRegistry.MARK.get()),"when the night is over the mark is gone");
            }).thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.474: a push on the swings
    /** A hand on somebody: what the game makes of a use on another player. True if something took it. */
    private static boolean push(ServerPlayer from,ServerPlayer on){
        var e=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract(from,InteractionHand.MAIN_HAND,on);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(e);return e.isCanceled();
    }
    /** Which way the swing is going (forward is positive), by the turn of its arc. */
    private static double going(SeatEntity seat){return Math.cos(seat.swung(0)*HouseReadSwing.RATE);}

    @GameTest(template="empty",batch="park_push",timeoutTicks=3000)
    public static void aReaderOnASwingIsPushedByAnotherOnTheSandWhoTimesIt(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null,null};final SeatEntity[] seat={null,null};
        double bar=BikeNightSite.SWING_X+.5,line=BikeNightSite.SWING_SEATS[0]+.5;final double[] was={0,0};final int[] sent={0,0,0};
        double meet=Math.sin(HouseReadSwing.MEET*HouseReadSwing.RATE);
        // The one behind pushes each time the swing starts away from them.
        Runnable timed=()->{
            double before=seat[0].reach();
            if(seat[0].swingAngle(0)<0&&going(seat[0])>0&&push(p[1],p[0])&&seat[0].reach()>before+.05)sent[0]++;
            was[1]=Math.max(was[1],seat[0].reach());sent[1]=Math.max(sent[1],seat[0].kicks());sent[2]|=seat[0].turning(0)>=0?1:0;
        };
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{p[0]=f.player("swing_rider",P);p[1]=f.player("swing_hands",P);p[2]=f.player("swing_other",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenExecute(()->{
                h.assertTrue(HouseReadSwing.PUSH_WAIT>(int)Math.ceil(Math.PI/2/HouseReadSwing.RATE)+HouseReadSwing.MEET,"the same hands get one push in as a swing leaves them, not two");
                f.click(p[0],P,BikeNightSite.swingAt(0));seat[0]=(SeatEntity)p[0].getVehicle();
                h.assertTrue(seat[0]!=null&&seat[0].pumped()&&seat[0].reach()==0&&seat[0].arc()==0,"on a swing, hanging still");
                // A hand on somebody who is not on a swing is nothing of the swings'.
                f.at(p[1],P,bar-2.6,0,line);
                h.assertTrue(!push(p[2],p[1]),"a hand on somebody standing on the sand is not a push");
                // From beside the swing nothing is pushed.
                f.at(p[1],P,bar+.3,0,line+1.2);hush(p[1]);
                h.assertTrue(push(p[1],p[0])&&seat[0].reach()==0,"from beside it the hand is taken, and pushes nothing");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[1],HouseReadSwing.PUSH_BESIDE),"and they are told where to stand"))
            .thenExecute(()->{
                // With a ball of sand in the hand, the hand throws.
                f.at(p[1],P,bar-2.6,0,line);
                p[1].setItemInHand(InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(BikeNightRegistry.SAND_BALL.get()));
                h.assertTrue(!push(p[1],p[0]),"a ball of sand in the hand is thrown, not put down for a push");
                p[1].setItemInHand(InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);
                // From behind, a swing hanging still is started: away from the hands, from the bottom of its arc.
                hush(p[1]);hush(p[0]);
                h.assertTrue(seat[0].reach()<HouseReadSwing.STILL&&push(p[1],p[0]),"a hand on the one riding is taken");
                h.assertTrue(seat[0].reach()>=HouseReadSwing.PUSHED&&seat[0].reach()<HouseReadSwing.PUSHED+HouseReadSwing.STILL&&Math.abs(seat[0].swingAngle(0))<1e-9&&seat[0].swingAngle(1)>.01,
                    "a push from behind starts a still swing forward from the bottom of its arc: "+seat[0].reach()+" "+seat[0].swingAngle(1));
                // The same hands do not push again at once.
                was[0]=seat[0].reach();h.assertTrue(push(p[1],p[0])&&seat[0].reach()==was[0],"the same hands do not push again at once");
                seat[0].reach(HouseReadSwing.REACH);
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[1],HouseReadSwing.PUSH_HINT)&&heard(p[0],String.format(HouseReadSwing.PUSHED_BY,"swing_hands"))&&sounded(p[1],"park.swing_creak"),
                "the one pushing is told how, the one pushed is told who, and the chains are heard"))
            // Out past the middle from the hands, the swing is out of reach. (Inside the wait between two pushes nothing is said at all.)
            .thenIdle(HouseReadSwing.PUSH_WAIT)
            .thenWaitUntil(()->h.assertTrue(seat[0].swingAngle(0)>.1,"waiting for it to go out past the middle"))
            .thenExecute(()->{
                hush(p[1]);was[0]=seat[0].reach();
                h.assertTrue(push(p[1],p[0])&&seat[0].reach()==was[0],"out past the middle a push does nothing");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[1],HouseReadSwing.PUSH_FAR),"and they are told it is out of reach"))
            // Coming back at the hands, and not yet near the top of its arc: a push now is too soon, and takes some of the arc off.
            .thenWaitUntil(()->h.assertTrue(seat[0].swingAngle(0)<-.05&&going(seat[0])<-(meet+.08),"waiting for it to come back at the hands: "+seat[0].swingAngle(0)+" "+going(seat[0])))
            .thenExecute(()->{
                hush(p[1]);was[0]=seat[0].reach();
                h.assertTrue(push(p[1],p[0])&&Math.abs(was[0]-seat[0].reach()-HouseReadSwing.CHECKED)<1e-9,"a push made while it is still coming takes some of the swing off it: "+was[0]+" to "+seat[0].reach());
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[1],HouseReadSwing.PUSH_EARLY),"and they are told it was too soon"))
            // As it stops at the top of its arc on their side, hands meet it.
            .thenIdle(HouseReadSwing.PUSH_WAIT)
            .thenWaitUntil(()->h.assertTrue(seat[0].swingAngle(0)<-.05&&going(seat[0])<0&&going(seat[0])>-(meet-.05),"waiting for it to come up to the top of its arc at the hands: "+going(seat[0])))
            .thenExecute(()->{
                was[0]=seat[0].reach();
                h.assertTrue(push(p[1],p[0])&&Math.abs(seat[0].reach()-was[0]-HouseReadSwing.PUSHED)<1e-9,"a push as it stops at the top sends it on: "+was[0]+" to "+seat[0].reach());
            })
            // Pushed each time it starts away, it goes as high as a swing goes. (0.4.475: a push that meets it as it turns is a kick,
            // so hands at one end take it a step past that for a moment; with the other end going by unkicked it never goes over.)
            .thenExecute(()->{sent[0]=0;was[1]=0;})
            .thenExecuteFor(520,timed)
            .thenExecute(()->{
                h.assertTrue(sent[0]>=5&&was[1]>HouseReadSwing.HIGH-1e-9&&was[1]<=HouseReadSwing.HIGH+HouseReadSwing.OVER_STEP+1e-9&&seat[0].reach()>HouseReadSwing.HIGH-.12&&sent[1]<=1&&sent[2]==0,
                    "pushed as it starts away each time, the swing goes as high as it goes, a step past it at most, and never over the bar: "+sent[0]+" pushes, "+was[1]+", now "+seat[0].reach()+", "+sent[1]+" kicks");
                h.assertTrue(HouseReadSwing.HIGH>HouseReadSwing.REACH*2,"far higher than a swing left alone");
            })
            .thenWaitUntil(()->h.assertTrue(f.own(p[1],P).getInt("SwingPushes")>=5&&f.own(p[1],P).getString("SwingPushed").equals("swing_rider")&&f.own(p[0],P).getString("SwingPushedBy").equals("swing_hands"),
                "it is in both their records: "+f.own(p[1],P).getInt("SwingPushes")))
            .thenExecute(()->{
                // Nobody pushes from a seat of their own.
                f.click(p[2],P,BikeNightSite.swingAt(1));seat[1]=(SeatEntity)p[2].getVehicle();
                h.assertTrue(seat[1]!=null&&!push(p[2],p[0]),"nobody pushes from a seat of their own");
                p[2].stopRiding();
                // From in front, the push comes as the swing starts back.
                f.at(p[2],P,bar+3.4,0,line);seat[0].reach(HouseReadSwing.REACH);hush(p[0]);hush(p[2]);
            })
            .thenWaitUntil(()->h.assertTrue(seat[0].swingAngle(0)>.05&&going(seat[0])<0,"waiting for it to start back from in front"))
            .thenExecute(()->{
                was[0]=seat[0].reach();
                h.assertTrue(push(p[2],p[0])&&Math.abs(seat[0].reach()-was[0]-HouseReadSwing.PUSHED)<1e-9,"a push from in front, as it starts back, sends it on: "+was[0]+" to "+seat[0].reach());
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[2],HouseReadSwing.PUSH_HINT)&&heard(p[0],String.format(HouseReadSwing.PUSHED_BY,"swing_other"))&&f.own(p[0],P).getString("SwingPushedBy").equals("swing_other"),
                "new hands are told how, and the one pushed is told whose they are"))
            .thenExecute(()->{
                // A still swing pushed from in front starts backward.
                p[0].stopRiding();
            })
            .thenIdle(3)
            .thenExecute(()->{
                f.click(p[0],P,BikeNightSite.swingAt(0));seat[0]=(SeatEntity)p[0].getVehicle();
                h.assertTrue(seat[0]!=null&&seat[0].reach()==0&&push(p[2],p[0]),"back on it, hanging still, and pushed from in front");
                h.assertTrue(seat[0].reach()>=HouseReadSwing.PUSHED&&Math.abs(seat[0].swingAngle(0))<.01&&seat[0].swingAngle(1)<-.01,"a push from in front starts a still swing backward: "+seat[0].swingAngle(0)+" "+seat[0].swingAngle(1));
                // Their accounts.
                var hands=told("swing_hands",p[1],f);var rider=told("swing_rider",p[0],f);
                h.assertTrue(hands.contains("I pushed swing_rider on the swings."),"the one who pushed has it in their account: "+hands);
                h.assertTrue(rider.contains("I sat on one of the swings, and swing_other pushed me."),"and so has the one pushed: "+rider);
                var jumped=f.own(p[0],P).copy();jumped.putInt("SwingBest",9);
                var after=String.join(" ",BikeNightTexts.account("swing_rider",jumped).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(after.contains("as high as it would go, with swing_other pushing, and let go of it"),"one who was pushed and then let go says so: "+after);
            }).thenSucceed();
    }

    /**
     * Any swing a reader can sit on can be pushed. The porch's kind (Drisking's yard swing is one) hangs on ropes and has one gentle
     * arc: a push takes it a little wider, never as wide as there is not room for. One is hung in the air over the park for the case.
     */
    @GameTest(template="empty",batch="park_rope",timeoutTicks=2400)
    public static void aRopeSwingIsPushedALittleWiderAndNoFurtherThanThereIsRoom(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final SeatEntity[] seat={null};
        int rx=-15,ry=14,rz=-23;var at=b.offset(rx,ry,rz);double mid=rx+1,line=rz+.5;final double[] was={0};
        java.util.function.DoublePredicate clear=arc->{
            // The seat and the one on it at the back end of an arc this wide.
            var end=HouseReadSwing.seat(HouseReadSwing.pivot(HouseReadSwing.pair(l,at)),Direction.SOUTH,-arc);
            return l.noCollision(new AABB(end.x-.95,end.y-.1,end.z-.3,end.x+.95,end.y+1.1,end.z+.3));
        };
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{p[0]=f.player("rope_rider",P);p[1]=f.player("rope_hands",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenExecute(()->{
                // A rope swing as Drisking's yard has it: a plank two blocks wide on its ropes under a bar, facing south.
                var plank=HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,HouseReadPropBlock.Part.SWING).setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,Direction.NORTH);
                var rope=HouseReadRegistry.ROPE.get().defaultBlockState().setValue(net.minecraft.world.level.block.ChainBlock.AXIS,Direction.Axis.Y);
                for(int dx=0;dx<=1;dx++){l.setBlock(at.east(dx),plank,3);for(int y=1;y<=3;y++)l.setBlock(at.east(dx).above(y),rope,3);}
                for(int dx=-1;dx<=2;dx++)l.setBlock(at.east(dx).above(4),Blocks.STRIPPED_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X),3);
                h.assertTrue(HouseReadSwing.is(l.getBlockState(at))&&!HouseReadSwing.pumped(l.getBlockState(at))&&HouseReadSwing.forward(l.getBlockState(at))==Direction.SOUTH&&HouseReadSwing.pair(l,at).size()==2,"a rope swing, facing south");
                f.at(p[0],P,mid,ry,line+1.5);h.assertTrue(HouseSitting.sit(p[0],at)&&p[0].getVehicle() instanceof SeatEntity s&&s.swinging()&&!s.pumped(),"a reader sits on it");
                seat[0]=(SeatEntity)p[0].getVehicle();
                // Hanging still, a push from behind starts it forward.
                f.at(p[1],P,mid,ry,line-2.8);hush(p[1]);hush(p[0]);
                h.assertTrue(seat[0].arc()==0&&push(p[1],p[0]),"a hand on the one riding is taken");
                h.assertTrue(Math.abs(seat[0].pushed()-HouseReadSwing.PUSHED)<1e-6&&Math.abs(seat[0].swingAngle(0))<1e-9&&seat[0].swingAngle(1)>.01,"a push from behind starts a still rope swing forward: "+seat[0].pushed()+" "+seat[0].swingAngle(1));
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[1],HouseReadSwing.PUSH_HINT)&&heard(p[0],String.format(HouseReadSwing.PUSHED_BY,"rope_hands")),"the one pushing is told how, and the one pushed is told who"))
            .thenExecute(()->{
                // Left alone the push eases off, and the swing comes to the gentle arc it has always had.
                seat[0].swing(l.getGameTime()-400);
            })
            .thenIdle(200)
            .thenExecute(()->{
                h.assertTrue(seat[0].pushed()==0&&Math.abs(seat[0].arc()-HouseReadSwing.REACH)<1e-9&&Math.abs(seat[0].swingAngle(0)-HouseReadSwing.angle(seat[0].swingTicks(0)))<1e-12,
                    "left alone the push eases off, and the swing is on the gentle arc it has always had: "+seat[0].pushed()+" "+seat[0].arc());
            })
            // Pushed as it starts away, twice: a little wider than its gentle arc, and no wider than a rope swing goes.
            .thenWaitUntil(()->h.assertTrue(seat[0].swingAngle(0)<-.05&&going(seat[0])>0,"waiting for it to start away"))
            .thenExecute(()->h.assertTrue(push(p[1],p[0])&&Math.abs(seat[0].pushed()-HouseReadSwing.PUSHED)<1e-6,"a push as it starts away takes it wider: "+seat[0].pushed()))
            .thenIdle(HouseReadSwing.PUSH_WAIT)
            .thenWaitUntil(()->h.assertTrue(seat[0].swingAngle(0)<-.05&&going(seat[0])>0,"waiting for it to start away again"))
            .thenExecute(()->{
                h.assertTrue(push(p[1],p[0])&&Math.abs(seat[0].arc()-HouseReadSwing.ROPE_HIGH)<1e-6&&HouseReadSwing.ROPE_HIGH>HouseReadSwing.REACH&&HouseReadSwing.ROPE_HIGH<HouseReadSwing.HIGH,
                    "and again, to the widest a rope swing is pushed, which is well short of a swing that is pumped: "+seat[0].arc());
                h.assertTrue(clear.test(seat[0].arc()),"with nothing behind it there is room for that");
                // Something behind it, where the widest arc would reach: it goes only as wide as there is room for.
                for(int dx=-1;dx<=2;dx++)for(int y=0;y<=2;y++)l.setBlock(at.offset(dx,y,-3),Blocks.STONE.defaultBlockState(),3);
                h.assertTrue(!clear.test(HouseReadSwing.ROPE_HIGH)&&clear.test(HouseReadSwing.ROPE_HIGH-.05),"a wall three blocks behind it stands where the widest arc would reach, and not where one a little narrower would");
            })
            .thenIdle(HouseReadSwing.PUSH_WAIT)
            .thenWaitUntil(()->h.assertTrue(seat[0].swingAngle(0)<-.05&&going(seat[0])>0,"waiting for it to start away a third time"))
            .thenExecute(()->{
                h.assertTrue(push(p[1],p[0])&&seat[0].arc()<=HouseReadSwing.ROPE_HIGH-.05+1e-6&&seat[0].arc()>HouseReadSwing.REACH+.05&&clear.test(seat[0].arc()),
                    "pushed again with the wall there, it goes only as wide as there is room for: "+seat[0].arc());
                p[0].stopRiding();
            })
            .thenIdle(3)
            .thenExecute(()->{
                h.assertTrue(seat[0].isRemoved()&&l.getBlockState(at.above(2)).is(HouseReadRegistry.ROPE.get()),"off it, the swing hangs still on its ropes again");
                for(int dx=-1;dx<=2;dx++)for(int y=0;y<=4;y++)for(int dz=-3;dz<=0;dz++)l.setBlock(at.offset(dx,y,dz),Blocks.AIR.defaultBlockState(),3);
            }).thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.475: over the bar
    @GameTest(template="empty",batch="park_over",timeoutTicks=4800)
    public static void aSwingKickedExactlyAsItTurnsEndAfterEndGoesAllTheWayOverTheBar(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final SeatEntity[] seat={null};
        double bar=BikeNightSite.SWING_X+.5,line=BikeNightSite.SWING_SEATS[0]+.5;var post=new BlockPos(BikeNightSite.SWING_X,1,BikeNightSite.SWING_POSTS[1]);var forward=BikeNightSite.SWING_FORWARD;
        final double[] top={0,0,0};final int[] most={0};final boolean[] went={false,false};final Vec3[] away={null};var levels=new java.util.TreeSet<Integer>();
        Runnable watch=()->{most[0]=Math.max(most[0],seat[0].kicks());top[0]=Math.max(top[0],seat[0].reach());top[1]=Math.max(top[1],seat[0].getY()-b.getY());top[2]=Math.max(top[2],Math.abs(seat[0].swingAngle(0)));
            levels.add((int)Math.round(seat[0].reach()*100));went[0]|=seat[0].turning(0)>=0;};
        // Leaning with the swing, but late: the lean goes over five ticks after the swing has turned.
        Runnable sloppy=()->{p[0].setPlayerInput(0,Math.cos((seat[0].swung(0)-4)*HouseReadSwing.RATE)>0?1F:-1F,false,false);watch.run();};
        // Leaning exactly with it: the lean goes over on the tick the swing turns. Once it is going over the bar, the lean is let go.
        Runnable exact=()->{if(went[0])p[0].setPlayerInput(0,0,false,false);else p[0].setPlayerInput(0,Math.cos((seat[0].swung(0)+1)*HouseReadSwing.RATE)>0?1F:-1F,false,false);watch.run();};
        // Exact at the front end of the arc only: round the back end the lean is let go, and taken up again too late to count.
        Runnable front=()->{double turn=(seat[0].swung(0)+1)*HouseReadSwing.RATE;boolean back=Math.sin(turn)<0&&Math.abs(Math.cos(turn))<Math.sin(6.5*HouseReadSwing.RATE);
            if(went[0]||back)p[0].setPlayerInput(0,0,false,false);else p[0].setPlayerInput(0,Math.cos(turn)>0?1F:-1F,false,false);
            // And somebody behind, who pushes as it starts away from them.
            if(!went[0]&&seat[0].swingAngle(0)<0&&going(seat[0])>0)push(p[1],p[0]);
            watch.run();};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{p[0]=f.player("over_rider",P);p[1]=f.player("over_watcher",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenExecute(()->{
                h.assertTrue(HouseReadSwing.OVER_REACH>Math.PI/2&&HouseReadSwing.OVER_REACH<Math.PI&&Math.abs(HouseReadSwing.over(0))<1e-9&&Math.abs(HouseReadSwing.over(.5)-Math.PI)<1e-9&&Math.abs(HouseReadSwing.over(1)-2*Math.PI)<1e-6,
                    "a turn over the bar is a whole turn, and the top of it is half way through");
                double start=HouseReadSwing.over(.01)/(.01*HouseReadSwing.OVER_TICKS),over=(HouseReadSwing.over(.505)-HouseReadSwing.over(.495))/(.01*HouseReadSwing.OVER_TICKS);
                h.assertTrue(Math.abs(start-HouseReadSwing.OVER_REACH*HouseReadSwing.RATE)<.01&&over<start*.45&&over>start*.35,"it leaves the bottom at the speed of the arc it was sent from, and is slowest over the top: "+start+" "+over);
                // The post is the park's only word on it.
                hush(p[1]);f.click(p[1],P,post);
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[1],"T.R. WENT OVER THE BAR")&&heard(p[1],"DID NOT")&&!heard(p[1],"DID TOO"),"a hand on the frame finds what is scratched into its post"))
            .thenExecute(()->{
                f.at(p[1],P,bar+6,0,line+4);
                f.click(p[0],P,BikeNightSite.swingAt(0));seat[0]=(SeatEntity)p[0].getVehicle();h.assertTrue(seat[0]!=null&&seat[0].pumped(),"on a swing");
                seat[0].reach(HouseReadSwing.HIGH);
            })
            // Leaning with it, however long, without the timing: as high as a swing goes, and not a hair past it.
            .thenExecuteFor(170,sloppy)
            .thenExecute(()->{
                h.assertTrue(most[0]==0&&top[0]<=HouseReadSwing.HIGH+1e-9&&!went[0]&&seat[0].reach()>HouseReadSwing.HIGH-.05,"leaning with it late, end after end, never takes it past its widest arc: "+top[0]+", "+most[0]+" kicks");
                levels.clear();
            })
            // Exactly as it turns, end after end: a step past its widest each time, and after the fifth, over the bar.
            .thenExecuteFor(250,exact)
            .thenExecute(()->{
                h.assertTrue(went[0]&&most[0]==HouseReadSwing.OVER_KICKS&&Math.abs(top[0]-HouseReadSwing.OVER_REACH)<1e-6,"five kicks in time, one end after another, and it goes over: "+most[0]+" kicks, "+top[0]);
                h.assertTrue(levels.containsAll(List.of(140,165,190,215)),"each kick takes the arc a step past its widest, and it is held there: "+levels);
                h.assertTrue(top[1]>HouseReadSwing.PIVOT+HouseReadSwing.LENGTH-.3&&top[2]>6,"the seat goes up over the bar and all the way round: "+top[1]+" up, "+top[2]+" round");
                h.assertTrue(seat[0].turning(0)<0&&seat[0].kicks()==0&&seat[0].reach()<=HouseReadSwing.HIGH+1e-9&&p[0].getVehicle()==seat[0],
                    "and comes down swinging; with the lean let go, the next end goes by without a kick and the arc is back to its widest: "+seat[0].reach());
            })
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt("OverBar")==1&&f.own(p[0],P).getString("OverSeenBy").equals("over_watcher")&&f.own(p[1],P).getString("SawOver").equals("over_rider"),"it is in their record, and in the record of the one who saw"))
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.OVER_BAR)&&heard(p[1],String.format(BikeNightTexts.OVER_SEEN,"over_rider")),"the one who went over is told, and so is the one who saw"))
            // Two kicks in time, and then an end that goes by without one: everything the kicks had gained is lost.
            .thenExecute(()->{went[0]=false;most[0]=0;top[0]=0;})
            .thenWaitUntil(()->{exact.run();h.assertTrue(seat[0].kicks()==2,"waiting for two kicks in time: "+seat[0].kicks());})
            .thenExecute(()->p[0].setPlayerInput(0,0,false,false))
            .thenExecuteFor(70,watch)
            .thenExecute(()->{
                h.assertTrue(!went[0]&&seat[0].kicks()==0&&top[0]>HouseReadSwing.HIGH+HouseReadSwing.OVER_STEP&&seat[0].reach()<=HouseReadSwing.HIGH+1e-9&&f.own(p[0],P).getInt("OverBar")==1,
                    "an end that goes by without its kick loses what the kicks had gained, and the swing does not go over: "+top[0]+" to "+seat[0].reach());
                // A push that meets the swing as it turns is a kick. Somebody behind takes the back end; the rider has only the front to time.
                f.at(p[1],P,bar-3.7,0,line);seat[0].reach(HouseReadSwing.HIGH);went[0]=false;most[0]=0;
            })
            .thenWaitUntil(()->{front.run();h.assertTrue(went[0],"waiting for the rider at the front and the hands at the back to send it over between them: "+seat[0].kicks());})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt("OverBar")==2,"over the bar a second time, with a friend's hands for half the kicks"))
            .thenExecute(()->{
                // Letting go of a swing going over the bar throws nobody further than its widest arc would, and nobody is hurt coming down.
                p[0].setPlayerInput(0,0,false,false);seat[0].reach(HouseReadSwing.HIGH);went[0]=false;f.at(p[1],P,bar+6,0,line+4);
            })
            .thenIdle(40)
            .thenWaitUntil(()->{exact.run();h.assertTrue(seat[0].turning(0)>.2,"waiting to be a little way round a third turn");})
            .thenExecute(()->{
                away[0]=HouseReadSwing.jump(p[0]);
                var carried=away[0]==null?null:away[0].subtract(HouseReadSwing.launch(Vec3.ZERO,forward));
                h.assertTrue(away[0]!=null&&!p[0].isPassenger()&&carried.length()<=HouseReadSwing.FASTEST*HouseReadSwing.CARRY+1e-6&&carried.length()>HouseReadSwing.FASTEST*HouseReadSwing.CARRY-1e-6,
                    "letting go a little way round throws them no harder than the swing's widest arc would: "+away[0]);
                // High over the park, where the sand's own softness does not reach.
                p[0].teleportTo(p[0].getX(),b.getY()+9,p[0].getZ());
                var hard=new net.neoforged.neoforge.event.entity.living.LivingFallEvent(p[0],14F,1F);BikeNight.fall(hard);
                h.assertTrue(BikeNight.inFlight(p[0])&&!BikeNightSite.onSand(p[0].position().subtract(b.getX(),b.getY(),b.getZ()))&&hard.getDamageMultiplier()==0,"and nobody is hurt coming down off a swing, wherever it threw them");
            })
            .thenIdle(60)
            .thenExecute(()->{
                h.assertTrue(f.own(p[0],P).getInt("OverBar")==2,"a turn they let go of is not a time over the bar");
                // Back at the post, they have something to add.
                hush(p[0]);f.click(p[0],P,post);
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.SCRATCH_ADD)&&f.own(p[0],P).getBoolean("Scratched"),"one who has been over the bar scratches their answer under it"))
            .thenExecute(()->{hush(p[0]);f.click(p[0],P,post);hush(p[1]);f.click(p[1],P,post);})
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.SCRATCH_OWN)&&!heard(p[0],BikeNightTexts.SCRATCH_ADD)&&heard(p[1],BikeNightTexts.SCRATCH_FRESH),"once; after that it is there, for them and for anybody else that night"))
            .thenExecute(()->{
                var rider=told("over_rider",p[0],f);var watcher=told("over_watcher",p[1],f);
                h.assertTrue(rider.contains("I went all the way over the bar on one of the swings, and then did it again.")&&rider.contains("over_watcher saw.")&&rider.contains("I scratched DID TOO into the post, under DID NOT."),"their account has it: "+rider);
                h.assertTrue(watcher.contains("I saw over_rider go all the way over the bar on the swings."),"and so has the account of the one who saw: "+watcher);
                var alone=new net.minecraft.nbt.CompoundTag();alone.putInt("OverBar",1);alone.putBoolean("Swung",true);
                var unseen=String.join(" ",BikeNightTexts.account("over_rider",alone).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(unseen.contains("I went all the way over the bar on one of the swings. Nobody saw."),"one who went over with nobody there says so: "+unseen);
            }).thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.476: Gus
    /** The line over his head now, or nothing. */
    private static String saying(LiteraryActor a){return a.saying()&&a.getCustomName()!=null?a.getCustomName().getString():"";}
    private static double flat(net.minecraft.world.entity.Entity a,BlockPos b,Vec3 r){double x=a.getX()-b.getX()-r.x,z=a.getZ()-b.getZ()-r.z;return Math.sqrt(x*x+z*z);}

    @GameTest(template="empty",batch="park_gus",timeoutTicks=9000)
    public static void gusIsAtThePlaygroundTalkingTheWholeTimeAboutNothing(GameTestHelper h){
        var f=park(h);BikeNightGus.keptHome=false;var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] gus={null,null};
        var said=new ArrayList<String>();final String[] last={""};final boolean[] over={false},waved={false};var item=BikeNightRegistry.SAND_BALL.get();
        // Every line he says, in order; and whether one ever began while the one before it was still up.
        Runnable listen=()->{var now=saying(gus[0]);if(!now.isEmpty()&&!now.equals(last[0])){if(!last[0].isEmpty())over[0]=true;said.add(now);}last[0]=now;waved[0]|=gus[0].gesture()==BikeNightGus.WAVE;};
        // Listening, and bringing on his next line about nothing as soon as he has finished the last.
        Runnable hurry=()->{listen.run();if(!gus[0].saying())BikeNightGus.idleNow(b);};
        var idle=List.of(BikeNightTexts.GUS_IDLE);var story=List.of(BikeNightTexts.GUS_STORY);
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{
                // How he talks: every line of his is a sentence somebody could say in one breath, and none of it is unkind.
                h.assertTrue(BikeNightTexts.GUS_IDLE.length>=16&&BikeNightTexts.GUS_STORY.length==4&&new java.util.HashSet<>(BikeNightTexts.gusLines()).size()==BikeNightTexts.gusLines().size(),"he has plenty to say about nothing, a story in four parts, and says nothing twice");
                for(var line:BikeNightTexts.gusLines()){
                    h.assertTrue(line.length()<=130&&(line.endsWith(".")||line.endsWith("!")||line.endsWith("?")),"a line of his is short enough to read over his head, and ends: \""+line+"\"");
                    var low=" "+line.toLowerCase(java.util.Locale.ROOT)+" ";
                    for(var word:List.of(" stupid"," idiot"," shut up"," hate "," kill"," damn"," hell "," dude"," bro "," buddy"," you feel"," you are scared"," you're scared"))
                        h.assertTrue(!low.contains(word),"he is never unkind, never says how the reader feels, and calls the reader nothing: \""+line+"\" has \""+word.trim()+"\"");
                }
                p[0]=f.player("gus_reader",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenWaitUntil(()->{gus[0]=BikeNightGus.find(l,b);h.assertTrue(gus[0]!=null,"waiting for Gus");})
            // (A case's reader is made the park's size a few ticks after they arrive.)
            .thenWaitUntil(()->h.assertTrue(p[0].getBbHeight()<1.1,"waiting for the reader to be the park's size"))
            .thenExecute(()->{
                h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,room(f).inflate(4),BikeNightGus::is).size()==1,"there is one of him");
                h.assertTrue(gus[0].role()==LiteraryActor.STRANGER&&gus[0].phase()==BikeNightGus.GUS&&BikeNightGus.skin(gus[0].phase()).equals("bike_gus")&&BikeNightGus.skin(BikeNightGus.GUS_BARE).equals("bike_gus_bare")&&gus[0].scene().equals(P.id()),"Gus, in his cap, and the park's");
                h.assertTrue(gus[0].getBbHeight()>.9&&gus[0].getBbHeight()<1.05&&Math.abs(gus[0].getBbHeight()-p[0].getBbHeight())<.12,"he is the size the readers are: "+gus[0].getBbHeight()+" beside "+p[0].getBbHeight());
                h.assertTrue(flat(gus[0],b,BikeNightGus.HOME)<.3&&BikeNightSite.onSand(BikeNightGus.HOME)&&BikeNightGus.doing(b)==BikeNightGus.WAITING&&!gus[0].saying(),"waiting on the sand by the side path, saying nothing: "+gus[0].position());
            })
            // With nobody near, he says nothing.
            .thenExecuteFor(60,listen)
            .thenExecute(()->{
                h.assertTrue(said.isEmpty()&&BikeNightGus.doing(b)==BikeNightGus.WAITING&&!f.own(p[0],P).getBoolean("GusMet"),"with nobody near he waits and says nothing");
                f.at(p[0],P,BikeNightGus.HOME.x+5,0,BikeNightGus.HOME.z);
            })
            // Somebody comes: hello.
            .thenWaitUntil(()->{listen.run();h.assertTrue(saying(gus[0]).equals(BikeNightTexts.GUS_GREETING),"waiting for him to say hello");})
            .thenExecute(()->{
                h.assertTrue(gus[0].speaker().equals(BikeNightGus.NAME)&&waved[0]&&BikeNightGus.doing(b)==BikeNightGus.FREE&&f.own(p[0],P).getBoolean("GusMet"),"he says hello over his head, by name, with a wave, and it is in the reader's record");
                h.assertTrue(!heard(p[0],"You came"),"and nothing of it is sent to the reader as a message");
            })
            // Then he talks: a few lines about nothing, his story in its four parts, and on about nothing.
            .thenWaitUntil(()->{hurry.run();h.assertTrue(said.size()>=1+BikeNightGus.STORY_AFTER+story.size()+1,"waiting for him to talk: "+said.size()+" lines so far");})
            .thenExecute(()->{
                var first=said.subList(1,1+BikeNightGus.STORY_AFTER);var told=said.subList(1+BikeNightGus.STORY_AFTER,1+BikeNightGus.STORY_AFTER+story.size());var next=said.get(1+BikeNightGus.STORY_AFTER+story.size());
                h.assertTrue(!over[0],"he never starts a line over one of his own that is still up");
                h.assertTrue(idle.containsAll(first)&&new java.util.HashSet<>(first).size()==first.size(),"a few lines about nothing, no two the same: "+first);
                h.assertTrue(told.equals(story),"then why nobody cuts through the graveyard on Halloween, in order, and not told properly: "+told);
                h.assertTrue(idle.contains(next)&&!first.contains(next),"and on about nothing: "+next);
                h.assertTrue(f.own(p[0],P).getBoolean("GusStory"),"a reader near enough to hear the end of the story has it in their record");
            })
            // A hand on him gets an answer, unless he is in the middle of saying something.
            .thenWaitUntil(()->h.assertTrue(!gus[0].saying(),"waiting for him to finish"))
            .thenExecute(()->{
                LiteraryVignettes.talk(p[0],gus[0]);
                h.assertTrue(saying(gus[0]).equals(BikeNightTexts.GUS_TALK_NO_BIKE),"a hand on him, from a reader with no bike yet: "+saying(gus[0]));
                LiteraryVignettes.talk(p[0],gus[0]);
                h.assertTrue(saying(gus[0]).equals(BikeNightTexts.GUS_TALK_NO_BIKE),"a hand on him while he is saying something changes nothing");
            })
            .thenWaitUntil(()->h.assertTrue(!gus[0].saying(),"waiting for him to finish"))
            .thenExecute(()->{LiteraryVignettes.talk(p[0],gus[0]);h.assertTrue(saying(gus[0]).equals(BikeNightTexts.GUS_TALK[0]),"another hand gets the next thing he has to say: "+saying(gus[0]));})
            // He says something about the bike a reader took, when they are back in earshot.
            .thenWaitUntil(()->h.assertTrue(!gus[0].saying(),"waiting for him to finish"))
            .thenExecute(()->{take(h,f,p[0],NightBike.Make.WASP);getOff(p[0]);})
            .thenWaitUntil(()->h.assertTrue(!p[0].isPassenger(),"off the bike"))
            .thenExecute(()->{stand(p[0]);f.at(p[0],P,BikeNightGus.HOME.x+4,0,BikeNightGus.HOME.z);})
            .thenWaitUntil(()->h.assertTrue(saying(gus[0]).equals(BikeNightTexts.gusBike("wasp")),"he says something about the Wasp: "+saying(gus[0])))
            // A ball of sand that meets him.
            .thenWaitUntil(()->h.assertTrue(!gus[0].saying(),"waiting for him to finish"))
            .thenExecute(()->{
                p[0].setItemInHand(InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(item));p[0].getCooldowns().removeCooldown(item);
                f.at(p[0],P,gus[0].getX()-b.getX()+2.5,0,gus[0].getZ()-b.getZ());
                p[0].lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,gus[0].position().add(0,.45,0));
                p[0].gameMode.useItem(p[0],l,p[0].getMainHandItem(),InteractionHand.MAIN_HAND);
            })
            .thenWaitUntil(()->h.assertTrue(saying(gus[0]).equals(BikeNightTexts.GUS_SAND[0]),"a ball of sand that meets him gets a word: "+saying(gus[0])))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt("SandHits")==1&&gus[0].getHealth()==gus[0].getMaxHealth(),"it is in the thrower's record, and he is not hurt"))
            // He keeps near whoever is playing on the sand, and goes back to his place when nobody is.
            .thenExecute(()->{p[0].setItemInHand(InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);f.at(p[0],P,-20,0,-27.5);})
            .thenWaitUntil(()->h.assertTrue(gus[0].distanceTo(p[0])<BikeNightGus.BY+2&&gus[0].distanceTo(p[0])>1.5,"waiting for him to come over, and stop short of them: "+gus[0].distanceTo(p[0])))
            .thenExecute(()->{
                h.assertTrue(BikeNightSite.onSand(gus[0].position().subtract(b.getX(),b.getY(),b.getZ())),"he came across the sand: "+gus[0].position());
                f.at(p[0],P,6,0,-40);
            })
            .thenWaitUntil(()->h.assertTrue(flat(gus[0],b,BikeNightGus.HOME)<1.3,"with nobody playing he goes back to his place: "+gus[0].position()))
            .thenExecute(()->{
                var account=told("gus_reader",p[0],f);
                h.assertTrue(account.contains("Gus was there, in his cap, talking the whole time about nothing.")&&account.contains("He told it badly."),"their account has him in it: "+account);
                // The night over: nobody is left of him; the next night has one of him, at his place, with it all to say again.
                gus[1]=gus[0];BikeNight.resetNow(l,b);
                h.assertTrue(gus[1].isRemoved()&&BikeNightGus.find(l,b)==null,"when the night is over he is gone with it");
            })
            .thenWaitUntil(()->{gus[0]=BikeNightGus.find(l,b);h.assertTrue(gus[0]!=null&&gus[0]!=gus[1],"waiting for the next night's Gus");})
            .thenExecute(()->h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,room(f).inflate(4),BikeNightGus::is).size()==1&&flat(gus[0],b,BikeNightGus.HOME)<.3&&BikeNightGus.doing(b)==BikeNightGus.WAITING&&!gus[0].saying(),"one of him again, waiting at his place"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="park_gus_seesaw",timeoutTicks=6000)
    public static void gusTakesTheOtherEndOfTheSeesawForAReaderOnTheirOwn(GameTestHelper h){
        var f=park(h);BikeNightGus.keptHome=false;var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final LiteraryActor[] gus={null};
        final SeesawSeat[] seat={null,null};final boolean[] up={false,false};final int[] pushes={0};final long[] downSince={-1};var lines=new java.util.HashSet<String>();
        var stand=b.offset(BikeNightSite.SEESAW);
        java.util.function.Supplier<SeesawBlock.Board> board=()->(SeesawBlock.Board)l.getBlockEntity(stand);
        // The reader pushes off when their end has been down a moment, which is a push and never a bounce; and what Gus says is kept.
        // (A bounce on top of one of his sends the board over hard enough to throw the reader off their own end, which is the
        // seesaw's own case, not this one: how often he bounces is the game's dice.)
        Runnable ride=()->{if(!saying(gus[0]).isEmpty())lines.add(saying(gus[0]));if(seat[0]!=null&&p[0].getVehicle()==seat[0]){
            boolean down=board.get().down(seat[0].end());
            if(!down){up[0]=true;downSince[0]=-1;return;}
            if(downSince[0]<0)downSince[0]=l.getGameTime();
            if((up[0]||pushes[0]==0)&&l.getGameTime()-downSince[0]>SeesawBlock.Board.GOOD+2&&SeesawSeat.jump(p[0])){up[0]=false;pushes[0]++;}}};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("seesaw_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenWaitUntil(()->{gus[0]=BikeNightGus.find(l,b);h.assertTrue(gus[0]!=null,"waiting for Gus");})
            .thenExecute(()->{f.click(p[0],P,BikeNightSite.seesawEnd(0));seat[0]=(SeesawSeat)p[0].getVehicle();h.assertTrue(seat[0]!=null&&seat[0].end()==0,"the reader is on an end of the seesaw");})
            // He comes over and gets on the other end.
            .thenWaitUntil(()->{if(!saying(gus[0]).isEmpty())lines.add(saying(gus[0]));h.assertTrue(gus[0].getVehicle() instanceof SeesawSeat s&&s.end()==1,"waiting for Gus to get on the other end: he is "+BikeNightGus.doing(b)+" at "+gus[0].position());})
            .thenExecute(()->{
                seat[1]=(SeesawSeat)gus[0].getVehicle();
                h.assertTrue(BikeNightGus.doing(b)==BikeNightGus.ON_SEESAW&&gus[0].gesture()==BikeNightGus.SIT&&board.get().ridden(1)&&board.get().seat(1)==seat[1]&&p[0].getVehicle()==seat[0],"on it, his hands on the handle, and the board has his weight");
                h.assertTrue(Mth.degreesDifferenceAbs(gus[0].getYRot(),BikeNightSite.SEESAW_FACING.toYRot())<1,"facing the reader across the stand: "+gus[0].getYRot());
            })
            // The reader pushes off; he pushes off when his end comes down; and so it goes.
            .thenWaitUntil(()->{ride.run();h.assertTrue(pushes[0]>=4,"waiting for the board to have gone over a few times: "+pushes[0]+" pushes, tilt "+board.get().tilt()+", the reader "+(p[0].getVehicle()==seat[0]?"on it":"off it"));})
            // (He says hello first, and does not talk over himself.)
            .thenWaitUntil(()->{ride.run();h.assertTrue(lines.contains(BikeNightTexts.GUS_SEESAW_ON),"waiting for him to say how it is done: "+lines);})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("SeesawWith").equals("Gus")&&f.own(p[0],P).getInt("Seesaw")>=4,"it is in the reader's record, with whom"))
            .thenExecute(()->{
                h.assertTrue(told("seesaw_reader",p[0],f).contains("I went up and down on the seesaw with Gus on the other end."),"and in their account: "+told("seesaw_reader",p[0],f));
                // The reader gets off: after a moment so does he.
                p[0].stopRiding();
            })
            .thenWaitUntil(()->h.assertTrue(!gus[0].isPassenger()&&BikeNightGus.doing(b)==BikeNightGus.FREE&&gus[0].gesture()!=BikeNightGus.SIT,"left on it, he gets off: "+BikeNightGus.doing(b)))
            .thenExecute(()->{
                h.assertTrue(BikeNightSite.onSand(gus[0].position().subtract(b.getX(),b.getY(),b.getZ()))&&gus[0].position().distanceTo(Vec3.atBottomCenterOf(b.offset(BikeNightSite.seesawEnd(1))))<2,"onto the sand beside his end: "+gus[0].position());
                // Two readers on it: it is theirs.
                p[1]=f.player("seesaw_other",P);
            })
            // 0.4.478: and for the board to know Gus is off it. It counts an end as taken for two ticks after its rider has gone, and a
            // reader who arrived quickly was now and then refused the end he had just left.
            .thenWaitUntil(()->h.assertTrue(p[1].getBbHeight()<1.1&&BikeNight.here(p[1])&&!board.get().ridden(0)&&!board.get().ridden(1),"waiting for the second reader to have arrived, and for the board to be empty"))
            .thenExecute(()->{
                f.click(p[0],P,BikeNightSite.seesawEnd(0));f.click(p[1],P,BikeNightSite.seesawEnd(1));
                h.assertTrue(p[0].getVehicle() instanceof SeesawSeat&&p[1].getVehicle() instanceof SeesawSeat,"two readers on the seesaw: "+p[0].getVehicle()+", "+p[1].getVehicle());
            })
            .thenIdle(BikeNightGus.ALONE*3)
            .thenExecute(()->h.assertTrue(!gus[0].isPassenger()&&BikeNightGus.doing(b)==BikeNightGus.FREE,"with two readers on it he leaves it to them"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="park_gus_swing",timeoutTicks=9000)
    public static void gusPushesAReaderWhoIsOnASwingAloneAndHelpsThemOverTheBar(GameTestHelper h){
        var f=park(h);BikeNightGus.keptHome=false;var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final LiteraryActor[] gus={null};final SeatEntity[] seat={null};
        double bar=BikeNightSite.SWING_X+.5;final boolean[] seen={false,false,false};var lines=new java.util.HashSet<String>();
        Runnable watch=()->{if(!saying(gus[0]).isEmpty())lines.add(saying(gus[0]));seen[0]|=gus[0].gesture()==BikeNightGus.PUSH;seen[1]|=seat[0].turning(0)>=0;
            // He never walks through where the swings swing.
            var r=gus[0].position().subtract(b.getX(),b.getY(),b.getZ());seen[2]|=Math.abs(r.x-bar)<.7&&r.z>BikeNightSite.SWING_POSTS[0]&&r.z<BikeNightSite.SWING_POSTS[2]+1;};
        // The reader leans exactly with the front end of the arc and lets the lean go round the back, which is Gus's.
        Runnable front=()->{double turn=(seat[0].swung(0)+1)*HouseReadSwing.RATE;boolean back=Math.sin(turn)<0&&Math.abs(Math.cos(turn))<Math.sin(6.5*HouseReadSwing.RATE);
            if(seen[1]||back)p[0].setPlayerInput(0,0,false,false);else p[0].setPlayerInput(0,Math.cos(turn)>0?1F:-1F,false,false);watch.run();};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("swing_alone",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenWaitUntil(()->{gus[0]=BikeNightGus.find(l,b);h.assertTrue(gus[0]!=null,"waiting for Gus");})
            .thenExecute(()->{f.click(p[0],P,BikeNightSite.swingAt(0));seat[0]=(SeatEntity)p[0].getVehicle();h.assertTrue(seat[0]!=null&&seat[0].pumped(),"the reader is on a swing");})
            // He goes round behind it and pushes.
            .thenWaitUntil(()->{watch.run();h.assertTrue(BikeNightGus.doing(b)==BikeNightGus.PUSHING,"waiting for Gus to get behind the swing: he is "+BikeNightGus.doing(b)+" at "+gus[0].position());})
            .thenExecute(()->{
                var r=gus[0].position().subtract(b.getX(),b.getY(),b.getZ());
                h.assertTrue(Math.abs(r.x-(bar-BikeNightGus.BEHIND))<1.2&&Math.abs(r.z-(BikeNightSite.SWING_SEATS[0]+.5))<1.2&&!seen[2],"behind the swing, having gone round the end of the frame and not through it: "+r);
                h.assertTrue(lines.contains(BikeNightTexts.GUS_SWING_OFFER),"having said he would: "+lines);
            })
            .thenWaitUntil(()->{watch.run();h.assertTrue(seat[0].reach()>HouseReadSwing.REACH+.3,"waiting for his pushes to take the swing well past its gentle arc: "+seat[0].reach());})
            .thenExecute(()->h.assertTrue(seen[0],"his hands go out as he pushes"))
            .thenWaitUntil(()->h.assertTrue(heard(p[0],String.format(HouseReadSwing.PUSHED_BY,"Gus"))&&f.own(p[0],P).getString("SwingPushedBy").equals("Gus"),"the reader is told who is pushing, and it is in their record"))
            // As high as pushing takes it; and with him at the back, a reader who times only the front end goes over the bar.
            .thenWaitUntil(()->{watch.run();h.assertTrue(seat[0].reach()>=HouseReadSwing.HIGH-.03,"waiting for the swing to be as high as it goes: "+seat[0].reach());})
            .thenWaitUntil(()->{front.run();h.assertTrue(seen[1],"waiting for the reader at the front and Gus at the back to send it over between them: "+seat[0].kicks()+" kicks");})
            .thenWaitUntil(()->{watch.run();h.assertTrue(f.own(p[0],P).getInt("OverBar")==1,"over the bar");})
            .thenWaitUntil(()->{watch.run();h.assertTrue(lines.contains(BikeNightTexts.GUS_OVER),"and he saw it: "+lines);})
            .thenExecute(()->{
                h.assertTrue(lines.contains(BikeNightTexts.GUS_PAST),"he had something to say when it first went past its widest: "+lines);
                h.assertTrue(told("swing_alone",p[0],f).contains("Gus saw."),"their account says who saw: "+told("swing_alone",p[0],f));
                p[0].setPlayerInput(0,0,false,false);p[0].stopRiding();
            })
            .thenWaitUntil(()->h.assertTrue(BikeNightGus.doing(b)==BikeNightGus.FREE,"the reader off the swing, he is at a loose end again"))
            // With somebody else in the park, the pushing is theirs to do.
            .thenExecute(()->p[1]=f.player("swing_friend",P))
            .thenWaitUntil(()->h.assertTrue(p[1].getBbHeight()<1.1&&BikeNight.here(p[1]),"waiting for the second reader to have arrived"))
            .thenExecute(()->{f.click(p[0],P,BikeNightSite.swingAt(1));h.assertTrue(p[0].getVehicle() instanceof SeatEntity,"on a swing again, with another reader in the park");})
            .thenIdle(BikeNightGus.SWING_FIRST*3)
            .thenExecute(()->h.assertTrue(BikeNightGus.doing(b)==BikeNightGus.FREE,"with another reader in the park he leaves the pushing to them: "+BikeNightGus.doing(b)))
            .thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.477: a bike on the slide
    @GameTest(template="empty",batch="park_slide_bike",timeoutTicks=4000)
    public static void aBikeIsRiddenUpTheSlideTurnedRoundOnItsDeckAndRiddenDownAgain(GameTestHelper h){
        var f=park(h);BikeNightGus.keptHome=false;var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final NightBike[] bike={null};final LiteraryActor[] gus={null};
        var deck=BikeNightSite.slideDeck();var end=BikeNightSite.slideEnd();double line=deck.getX()+.5;final int[] t={0};final double[] top={0};var lines=new java.util.HashSet<String>();
        java.util.function.Supplier<Vec3> at=()->bike[0].position().subtract(b.getX(),b.getY(),b.getZ());
        Runnable hear=()->{if(gus[0]!=null&&!saying(gus[0]).isEmpty())lines.add(saying(gus[0]));};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("slide_rider",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenWaitUntil(()->{gus[0]=BikeNightGus.find(l,b);h.assertTrue(gus[0]!=null&&p[0].getBbHeight()<1.1,"waiting for Gus, and for the reader to be the park's size");})
            .thenExecute(()->{
                bike[0]=take(h,f,p[0],NightBike.Make.WASP);
                // On the sand below the end of the chute, looking up it.
                bike[0].setPos(b.getX()+line,b.getY(),b.getZ()+end.getZ()+2.5);bike[0].setYRot(180);bike[0].steady(0,0);p[0].setYRot(180);p[0].setYHeadRot(180);
            })
            // Up the chute, pedalling: a bike rolls up it as it rolls up any steps.
            .thenWaitUntil(()->{hear.run();t[0]++;var r=at.get();top[0]=Math.max(top[0],r.y);
                // Pedalled whenever it slows, all the way up and slowly on over the deck's edge; the brake only once it is well onto
                // the deck (0.4.478: a brake that grips, held a block short of the top, stops a bike on the chute).
                boolean on=r.y>deck.getY()+.95,well=on&&r.z<deck.getZ()+.75;p[0].setPlayerInput(0,well?-1:0,false,false);if(!well&&bike[0].speedNow()<(on?.08F:.2F))pedalWell(bike[0],t[0],3);
                h.assertTrue(p[0].getVehicle()==bike[0],"still on the bike at "+r);
                h.assertTrue(r.y>deck.getY()+.95&&Math.abs(r.z-(deck.getZ()+.5))<.6&&bike[0].speedNow()<.02,"waiting to be stopped on the deck: "+r+" at "+bike[0].speedNow());})
            .thenExecute(()->{
                var r=at.get();
                h.assertTrue(Math.abs(r.x-line)<.3&&Math.abs(r.y-(deck.getY()+1))<.05,"up on the deck, between its guards: "+r);
                // Turned round where it stands, to look down the chute.
                p[0].setPlayerInput(0,0,false,false);p[0].setYRot(0);p[0].setYHeadRot(0);
            })
            .thenWaitUntil(()->{hear.run();h.assertTrue(p[0].getVehicle()==bike[0]&&Math.abs(Mth.wrapDegrees(bike[0].getYRot()))<6,"waiting for the bike to come round: "+bike[0].getYRot());})
            .thenExecute(()->{var r=at.get();h.assertTrue(r.y>deck.getY()+.95&&Math.abs(r.z-(deck.getZ()+.5))<.6,"it turned round where it stood, on the deck: "+r);hush(p[0]);})
            // And down.
            .thenWaitUntil(()->{hear.run();t[0]++;var r=at.get();if(bike[0].speedNow()<.12F)pedalWell(bike[0],t[0],4);
                h.assertTrue(p[0].getVehicle()==bike[0],"still on the bike at "+r);
                h.assertTrue(r.z>end.getZ()+1.2&&r.y<.5,"waiting to be down off the end of the chute: "+r);})
            .thenWaitUntil(()->{hear.run();h.assertTrue(f.own(p[0],P).getString("SlideBike").equals("wasp")&&f.own(p[0],P).getInt("SlideRides")==1&&heard(p[0],BikeNightTexts.SLIDE_BIKE),"ridden down the slide: told, and in their record");})
            .thenExecute(()->{p[0].setPlayerInput(0,-1,false,false);
                h.assertTrue(told("slide_rider",p[0],f).contains("I rode the Wasp up the slide, turned it round at the top, and rode it down again."),"and in their account: "+told("slide_rider",p[0],f));})
            // Gus is impressed.
            .thenWaitUntil(()->{hear.run();h.assertTrue(lines.contains(BikeNightTexts.GUS_SLIDE_BIKE),"waiting for Gus to say so: "+lines);})
            // Across the end of the chute, never having been up it, is not a ride down it.
            .thenExecute(()->{
                p[0].setPlayerInput(0,0,false,false);
                bike[0].setPos(b.getX()+line,b.getY(),b.getZ()+end.getZ()+3);bike[0].setYRot(180);bike[0].steady(0,0);p[0].setYRot(180);p[0].setYHeadRot(180);
            })
            .thenWaitUntil(()->{t[0]++;var r=at.get();pedalWell(bike[0],t[0],3);h.assertTrue(r.y>.6,"waiting to be a little way up the chute: "+r);})
            .thenExecute(()->{p[0].setPlayerInput(0,-1,false,false);p[0].setYRot(0);p[0].setYHeadRot(0);})
            .thenWaitUntil(()->{h.assertTrue(Math.abs(Mth.wrapDegrees(bike[0].getYRot()))<6,"waiting for the bike to come round");})
            .thenExecute(()->p[0].setPlayerInput(0,0,false,false))
            .thenWaitUntil(()->{t[0]++;var r=at.get();if(bike[0].speedNow()<.12F)pedalWell(bike[0],t[0],4);h.assertTrue(r.z>end.getZ()+1.2&&r.y<.5,"waiting to be down again: "+r);})
            .thenIdle(BikeNight.LOOK*3)
            .thenExecute(()->h.assertTrue(f.own(p[0],P).getInt("SlideRides")==1,"part way up and down again is not the slide ridden down"))
            .thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.478: the chute as a hill
    @GameTest(template="empty",batch="park_slide_speed",timeoutTicks=3000)
    public static void aBikeRollsDownTheSlidesChuteGatheringSpeedAndLosesItGoingUp(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final NightBike[] bike={null};
        var deck=BikeNightSite.slideDeck();var end=BikeNightSite.slideEnd();double line=deck.getX()+.5,head=deck.getZ()+1,foot=end.getZ();
        final double[] off={0},stood={0};final boolean[] flew={false};final float[] bottom={0};final int[] t={0};
        java.util.function.Supplier<Vec3> at=()->bike[0].position().subtract(b.getX(),b.getY(),b.getZ());
        // How high the chute's surface is over the ground, that far south: from the deck's own height at its head to the run-out's at its foot.
        java.util.function.DoubleUnaryOperator sheet=z->deck.getY()+1-CalderPlayBlock.SLOPE*(Mth.clamp(z,head,foot)-head);
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("chute_rider",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.LAMP)!=null&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night, and for the reader to be the park's size"))
            .thenExecute(()->{
                bike[0]=take(h,f,p[0],NightBike.Make.LAMP);
                // Up on the deck, looking down the chute, with hardly a push.
                bike[0].setPos(b.getX()+line,b.getY()+deck.getY()+1,b.getZ()+deck.getZ()+.5);bike[0].setYRot(0);p[0].setYRot(0);p[0].setYHeadRot(0);bike[0].steady(0,.06F);
            })
            .thenWaitUntil(()->{
                var r=at.get();bike[0].steady(0,bike[0].speedNow());
                h.assertTrue(p[0].getVehicle()==bike[0],"still on the bike at "+r);
                // Clear of the deck, and as far as the run-out: on the chute.
                if(r.z>head+.45&&r.z<foot-.05){off[0]=Math.max(off[0],Math.abs(r.y-sheet.applyAsDouble(r.z)));flew[0]|=!bike[0].onGround();}
                if(r.z>=foot&&bottom[0]==0)bottom[0]=bike[0].speedNow();
                h.assertTrue(r.z>foot+1.5&&r.y<.5,"waiting to be down off the end of the chute: "+r);})
            .thenExecute(()->{
                h.assertTrue(!flew[0]&&off[0]<.03,"a bike rides the chute's own surface all the way down, never off it: "+off[0]+" from it at the most, in the air "+flew[0]);
                h.assertTrue(bottom[0]>.2F,"let roll from the deck with hardly a push, it is going well by the bottom: "+bottom[0]);
                // Up it. From the sand below the run-out, at a good speed, and from the run-out on left to roll.
                bike[0].setPos(b.getX()+line,b.getY(),b.getZ()+foot+3.5);bike[0].setYRot(180);p[0].setYRot(180);p[0].setYHeadRot(180);bike[0].steady(0,.3F);
            })
            .thenWaitUntil(()->{
                var r=at.get();bike[0].steady(0,r.z>foot+1.6?.3F:bike[0].speedNow());
                h.assertTrue(p[0].getVehicle()==bike[0],"still on the bike at "+r);
                h.assertTrue(r.z<foot&&bike[0].speedNow()<RiddenBike.STOPPED,"waiting for the chute to stop it: "+r+" at "+bike[0].speedNow());})
            .thenExecute(()->{
                var r=at.get();stood[0]=r.z;
                h.assertTrue(r.z>head+.4&&Math.abs(r.y-sheet.applyAsDouble(r.z))<.03,"not pedalled, it does not reach the top: it stalls on the chute, on its surface: "+r);
            })
            .thenExecuteFor(20,()->bike[0].steady(0,bike[0].speedNow()))
            .thenExecute(()->{
                var r=at.get();
                h.assertTrue(p[0].getVehicle()==bike[0]&&Math.abs(r.z-stood[0])<.01&&Math.abs(r.y-sheet.applyAsDouble(r.z))<.03,"and stands where it stopped, a foot down: "+r+" from "+stood[0]);
            })
            // Pedalled, it climbs the rest.
            .thenWaitUntil(()->{
                t[0]++;var r=at.get();
                boolean on=r.y>deck.getY()+.95,well=on&&r.z<deck.getZ()+.75;p[0].setPlayerInput(0,well?-1:0,false,false);if(!well&&bike[0].speedNow()<(on?.08F:.2F))pedalWell(bike[0],t[0],3);
                h.assertTrue(p[0].getVehicle()==bike[0],"still on the bike at "+r);
                h.assertTrue(r.y>deck.getY()+.95&&r.z<head&&bike[0].speedNow()<.02,"waiting to be stopped on the deck: "+r+" at "+bike[0].speedNow());})
            .thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.479: the three who come
    /** A ball of sand put in a reader's hand and thrown at somebody on a bike, from a little way ahead of them on the line they are riding. */
    private static void throwAhead(ServerPlayer p,PackBike bike,LiteraryActor at){
        var item=BikeNightRegistry.SAND_BALL.get();double rad=Math.toRadians(bike.getYRot());
        p.stopRiding();p.moveTo(bike.getX()-Math.sin(rad)*2.4,bike.getY(),bike.getZ()+Math.cos(rad)*2.4);p.setDeltaMovement(Vec3.ZERO);
        p.setItemInHand(InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(item));p.getCooldowns().removeCooldown(item);
        p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,at.position().add(0,at.getBbHeight()*.5,0));
        p.gameMode.useItem(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND);
    }
    /** The three, once they are in the park. */
    private static boolean three(ServerLevel l,BlockPos b,LiteraryActor[] who,PackBike[] bikes){
        for(int i=0;i<3;i++){who[i]=BikeNightBullies.find(l,b,i);bikes[i]=BikeNightBullies.bikeOf(l,b,i);if(who[i]==null||bikes[i]==null)return false;}
        return true;
    }

    @GameTest(template="empty",batch="park_bullies",timeoutTicks=8000)
    public static void threeComeOnBikesUndoTheGateCircleThePlaygroundAndMakeThemselvesAtHome(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=1000000;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who={null,null,null};final PackBike[] bikes={null,null,null};
        final boolean[] lit={false},onSand={false};final boolean[] corner=new boolean[4];var said=new ArrayList<String>();final int[] end={0};
        double[][] loop={{-.5,-37.5},{-29.5,-37.5},{-29.5,-9.5},{-.5,-9.5}};var stand=b.offset(BikeNightSite.SEESAW);
        java.util.function.Supplier<SeesawBlock.Board> board=()->(SeesawBlock.Board)l.getBlockEntity(stand);
        Runnable hear=()->{for(var a:who){if(a==null)continue;var line=saying(a);if(!line.isEmpty()&&!said.contains(line))said.add(line);}};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("park_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night, and for the reader to be the park's size"))
            .thenExecute(()->{
                f.at(p[0],P,-14.5,0,-20.5);hush(p[0]);
                h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.QUIET&&!BikeNightSite.gateIsOpen(l,b)&&BikeNightBullies.find(l,b,0)==null,"before they come there is nobody, and the gate is chained");
            })
            // The car: its light goes along the street, and is gone.
            .thenWaitUntil(()->{
                for(int x=BikeNightSite.FAR_WEST;x<=BikeNightSite.FAR_EAST;x++)lit[0]|=l.getBlockState(b.offset(x,1,BikeNightSite.LINE+2)).is(BikeNightRegistry.BEAM.get());
                h.assertTrue(BikeNightBullies.stage(b)>=BikeNightBullies.COMING&&three(l,b,who,bikes),"waiting for the car to have gone by and the three to be on the street: stage "+BikeNightBullies.stage(b));})
            .thenExecute(()->{
                boolean left=false;for(int x=BikeNightSite.FAR_WEST;x<=BikeNightSite.FAR_EAST;x++)left|=l.getBlockState(b.offset(x,1,BikeNightSite.LINE+2)).is(BikeNightRegistry.BEAM.get());
                h.assertTrue(lit[0]&&!left&&sounded(p[0],"park.car_pass"),"a car's lights went along the street, were heard from the sand, and left no light behind: lit "+lit[0]+", left "+left);
                for(int i=0;i<3;i++){
                    h.assertTrue(who[i].getVehicle()==bikes[i]&&bikes[i].look()==i&&who[i].gesture()==BikeNightBullies.RIDE,BikeNightBullies.NAMES[i]+" is on a bike of his own, his hands on its bars");
                    h.assertTrue(who[i].getBbHeight()>p[0].getBbHeight()+.05&&who[i].getBbHeight()<p[0].getBbHeight()*1.3,BikeNightBullies.NAMES[i]+" is a child, a little bigger than a reader: "+who[i].getBbHeight()+" against "+p[0].getBbHeight());
                }
            })
            // Tully undoes his padlock, and the gate is open.
            .thenWaitUntil(()->{hear.run();h.assertTrue(BikeNightBullies.stage(b)>=BikeNightBullies.CIRCLING,"waiting for the gate to be opened: stage "+BikeNightBullies.stage(b));})
            .thenExecute(()->{
                h.assertTrue(BikeNightSite.gateIsOpen(l,b)&&BikeNightBullies.open(b),"the gate is open");
                for(int x=BikeNightSite.PATH_W;x<=BikeNightSite.PATH_E;x++)for(int y=0;y<2;y++)h.assertTrue(l.getBlockState(b.offset(x,y,BikeNightSite.NORTH)).isAir(),"the gateway is clear from pier to pier");
                for(int x:new int[]{BikeNightSite.PATH_W,BikeNightSite.PATH_E})for(int z=BikeNightSite.NORTH-2;z<BikeNightSite.NORTH;z++)
                    h.assertTrue(CalderBlock.is(l.getBlockState(b.offset(x,0,z)),CalderBlock.Kind.GATE)&&CalderBlock.is(l.getBlockState(b.offset(x,1,z)),CalderBlock.Kind.GATE),"each leaf stands out over the pavement, end on");
                h.assertTrue(said.contains(BikeNightTexts.TULLY_GATE),"the padlock was Tully's, and he said so: "+said);
            })
            .thenWaitUntil(()->{hear.run();h.assertTrue(heard(p[0],BikeNightTexts.GATE_OPENED)&&sounded(p[0],"park.padlock")&&f.own(p[0],P).getBoolean("Bullies"),"the reader is told, heard the padlock from the sand, and it is in their record");})
            // Round the playground, twice, and never onto the sand; then off their bikes.
            .thenWaitUntil(()->{
                hear.run();
                for(int i=0;i<3;i++){
                    if(bikes[i].getPassengers().isEmpty())continue;var r=bikes[i].position().subtract(b.getX(),b.getY(),b.getZ());
                    onSand[0]|=BikeNightSite.onSand(r);
                    if(i==0)for(int c=0;c<4;c++)corner[c]|=Math.hypot(r.x-loop[c][0],r.z-loop[c][1])<4;
                }
                h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.LOITER,"waiting for them to have gone round and got off: stage "+BikeNightBullies.stage(b)+", Tully at "+who[0].position().subtract(b.getX(),b.getY(),b.getZ()));})
            .thenExecute(()->{
                h.assertTrue(corner[0]&&corner[1]&&corner[2]&&corner[3]&&!onSand[0],"they rode all the way round the playground, and never onto its sand: "+java.util.Arrays.toString(corner)+", on the sand "+onSand[0]);
                String first=said.stream().filter(line->List.of(BikeNightTexts.JEERS).contains(line)).findFirst().orElse("");
                h.assertTrue(first.equals(BikeNightTexts.JEERS[0])&&said.stream().filter(line->List.of(BikeNightTexts.JEERS).contains(line)||line.equals(BikeNightTexts.jeerAt("swing"))).count()>=3,"they jeered as they went, the science club first: "+said);
                for(var line:BikeNightTexts.bullyLines())h.assertTrue(!heard(p[0],line),"what they say is over their heads and never in chat: "+line);
                // Tully has Gus's cap on.
                var gus=BikeNightGus.find(l,b);
                h.assertTrue(who[0].phase()==BikeNightBullies.TULLY_CAP&&gus!=null&&gus.phase()==BikeNightGus.GUS_BARE&&said.contains(BikeNightTexts.TULLY_HAT)&&f.own(p[0],P).getBoolean("CapTaken"),"Tully took Gus's cap and is wearing it: "+who[0].phase()+", "+said);
                // Ray-Ray is on the sand; Duff is on an end of the seesaw with his arms folded; their bikes stand where they left them.
                h.assertTrue(BikeNightSite.onSand(who[1].position().subtract(b.getX(),b.getY(),b.getZ()))&&!who[1].isPassenger(),"Ray-Ray is out on the sand: "+who[1].position());
                h.assertTrue(who[2].getVehicle() instanceof SeesawSeat&&who[2].gesture()==BikeNightBullies.ARMS&&said.contains(BikeNightTexts.DUFF_MINE),"Duff has an end of the seesaw, arms folded: "+who[2].getVehicle()+", "+said);
                for(int i=0;i<3;i++)h.assertTrue(bikes[i].isAlive()&&bikes[i].getPassengers().isEmpty()&&bikes[i].speedNow()<.01F,BikeNightBullies.NAMES[i]+"'s bike stands where he left it");
                // How they talk: short, ended, unkind and no worse. Never a slur, never about a body, never what the reader is.
                for(var line:BikeNightTexts.bullyLines()){
                    h.assertTrue(line.length()<=70&&(line.endsWith(".")||line.endsWith("!")||line.endsWith("?")),"a line of theirs is short, and ends: \""+line+"\"");
                    var low=" "+line.toLowerCase(java.util.Locale.ROOT)+" ";
                    for(var word:List.of(" stupid"," idiot"," retard"," fat"," ugly"," gay"," damn"," hell "," crap"," suck"," kill"," die "," hurt"," punch"," boy"," girl"," he "," she "," his "," her "," sissy"," freak"," loser"))
                        h.assertTrue(!low.contains(word),"they are unkind and no worse, and say nothing of what a reader is: \""+line+"\" has \""+word.trim()+"\"");
                }
                // The other end of the seesaw: Duff keeps his down, and whoever gets on is kept up.
                end[0]=((SeesawSeat)who[2].getVehicle()).end();hush(p[0]);
                f.click(p[0],P,BikeNightSite.seesawEnd(1-end[0]));
                h.assertTrue(p[0].getVehicle() instanceof SeesawSeat,"a reader can get on the other end");
            })
            .thenWaitUntil(()->{hear.run();h.assertTrue(board.get().down(end[0])&&!board.get().down(1-end[0])&&said.contains(BikeNightTexts.DUFF_UP),"waiting for Duff's end to be down and the reader's up, and for him to say so: tilt "+board.get().tilt()+", "+said);})
            .thenExecute(()->h.assertTrue(!SeesawSeat.jump(p[0])&&p[0].getVehicle() instanceof SeesawSeat,"up there a push does nothing: there is no sand under their feet"))
            .thenIdle(40)
            .thenExecute(()->{
                h.assertTrue(board.get().down(end[0])&&who[2].getVehicle() instanceof SeesawSeat,"and Duff stays where he is");
                getOff(p[0]);
            })
            // Ray-Ray kicks sand.
            .thenWaitUntil(()->{hear.run();stand(p[0]);h.assertTrue(sounded(p[0],"park.sand_kick"),"waiting for Ray-Ray to kick sand");})
            .thenExecute(()->{
                String told=told("park_reader",p[0],f);
                h.assertTrue(told.contains("Then three more came on bikes of their own")&&told.contains("He took Gus's cap and wore it.")&&!told.contains("Tully said to get us"),"it is in the reader's account, as far as it has gone: "+told);
                // A hand on the open gate.
                hush(p[0]);f.click(p[0],P,new BlockPos(BikeNightSite.PATH_W,0,BikeNightSite.NORTH-1));
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.GATE_OPEN),"the open gate, looked at, is open"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="park_bullies_sand",timeoutTicks=6000)
    public static void aBallOfSandStopsOneOfThemAndAFewStartTheChase(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=1000000;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who={null,null,null};final PackBike[] bikes={null,null,null};
        var said=new ArrayList<String>();final int[] t={0};final double[] nearest={99};
        Runnable hear=()->{for(var a:who){if(a==null)continue;var line=saying(a);if(!line.isEmpty()&&!said.contains(line))said.add(line);}};
        // One of them riding well inside the park, to throw at.
        java.util.function.IntPredicate riding=i->who[i].getVehicle()==bikes[i]&&bikes[i].speedNow()>.18F&&bikes[i].getZ()>b.getZ()+BikeNightSite.NORTH+4;
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("sand_thrower",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night, and for the reader to be the park's size"))
            .thenExecute(()->f.at(p[0],P,-14.5,0,-20.5))
            .thenWaitUntil(()->h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CIRCLING&&three(l,b,who,bikes)&&riding.test(0),"waiting for Tully to be riding round the playground: stage "+BikeNightBullies.stage(b)))
            // One ball at Tully, as he rides.
            .thenExecute(()->{hush(p[0]);throwAhead(p[0],bikes[0],who[0]);})
            .thenWaitUntil(()->{hear.run();h.assertTrue(BikeNightBullies.hits(b)==1,"waiting for the ball to land on Tully");})
            .thenExecute(()->{
                h.assertTrue(who[0].gesture()==BikeNightBullies.COUGH&&said.contains(BikeNightTexts.HIT[0])&&f.own(p[0],P).getInt("BullyHits")==1,"he coughs into his hands and says so, and the reader's record has it: "+who[0].gesture()+", "+said);
                // A second, while he is still coughing, adds nothing.
                throwAhead(p[0],bikes[0],who[0]);
            })
            .thenIdle(25)
            .thenExecute(()->{
                h.assertTrue(BikeNightBullies.hits(b)==1&&sounded(p[0],"park.cough"),"one ball, one stop: a second while he coughs is not counted; and the cough is heard");
                h.assertTrue(bikes[0].speedNow()<.01F&&who[0].getVehicle()==bikes[0]&&riding.test(1)|riding.test(2)|bikes[1].speedNow()>.1F,"he has stopped where he is, still on his bike, and the others ride on: "+bikes[0].speedNow()+", "+bikes[1].speedNow());
                f.at(p[0],P,-14.5,0,-20.5);
            })
            .thenWaitUntil(()->{hear.run();h.assertTrue(who[0].gesture()==BikeNightBullies.RIDE&&bikes[0].speedNow()>.15F,"waiting for him to ride on: "+bikes[0].speedNow());})
            // One at Ray-Ray, and one at Duff: three, and Tully has had enough.
            .thenWaitUntil(()->h.assertTrue(riding.test(1),"waiting for Ray-Ray to be riding in the park"))
            .thenExecute(()->throwAhead(p[0],bikes[1],who[1]))
            .thenWaitUntil(()->{hear.run();h.assertTrue(BikeNightBullies.hits(b)==2&&said.contains(BikeNightTexts.HIT[1]),"waiting for the ball to land on Ray-Ray: "+BikeNightBullies.hits(b));})
            .thenExecute(()->f.at(p[0],P,-14.5,0,-20.5))
            .thenWaitUntil(()->h.assertTrue(riding.test(2),"waiting for Duff to be riding in the park"))
            .thenExecute(()->{hush(p[0]);throwAhead(p[0],bikes[2],who[2]);})
            .thenWaitUntil(()->{hear.run();h.assertTrue(BikeNightBullies.hits(b)==3&&said.contains(BikeNightTexts.HIT[2]),"waiting for the ball to land on Duff: "+BikeNightBullies.hits(b));})
            .thenExecute(()->f.at(p[0],P,-.5,0,-25.5))
            .thenWaitUntil(()->{hear.run();h.assertTrue(said.contains(BikeNightTexts.GET_THEM)&&heard(p[0],BikeNightTexts.GET_THEM_CUE)&&f.own(p[0],P).getBoolean("GetThem"),"waiting for Tully to say it, and the reader to be told: "+said);})
            // The chase: all three on their bikes, coming for the reader.
            .thenWaitUntil(()->{
                hear.run();t[0]++;
                for(int i=0;i<3;i++)nearest[0]=Math.min(nearest[0],bikes[i].position().distanceTo(p[0].position()));
                h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE&&who[0].getVehicle()==bikes[0]&&who[1].getVehicle()==bikes[1]&&who[2].getVehicle()==bikes[2]&&nearest[0]<3
                    &&said.stream().anyMatch(line->List.of(BikeNightTexts.CHASE_JEERS).contains(line)),"waiting for all three to be riding at the reader, calling after them: stage "+BikeNightBullies.stage(b)+", nearest "+nearest[0]+", "+said);})
            .thenExecute(()->{
                h.assertTrue(p[0].getHealth()==p[0].getMaxHealth(),"reached, the reader is not hurt");
                String told=told("sand_thrower",p[0],f);
                h.assertTrue(told.contains("I threw sand at them, and three balls of it landed.")&&told.contains("Tully said to get us."),"and it is in their account: "+told);
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="park_bullies_time",timeoutTicks=8000)
    public static void leftAloneLongEnoughTullySaysItAnywayAndTheyComeOutByTheGate(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who={null,null,null};final PackBike[] bikes={null,null,null};
        var said=new ArrayList<String>();final double[] crossed={Double.NaN};final boolean[] was={true,true,true};final double[] nearest={99};
        double fence=b.getZ()+BikeNightSite.NORTH+.5;
        Runnable hear=()->{for(var a:who){if(a==null)continue;var line=saying(a);if(!line.isEmpty()&&!said.contains(line))said.add(line);}};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("park_waiter",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night, and for the reader to be the park's size"))
            .thenExecute(()->f.at(p[0],P,-14.5,0,-20.5))
            .thenWaitUntil(()->{three(l,b,who,bikes);hear.run();h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.LOITER,"waiting for them to be standing about: stage "+BikeNightBullies.stage(b));})
            .thenExecute(()->h.assertTrue(BikeNightBullies.hits(b)==0&&who[2].getVehicle() instanceof SeesawSeat,"nobody has thrown anything, and Duff is on the seesaw"))
            // With nothing thrown, time does it.
            .thenWaitUntil(()->{hear.run();h.assertTrue(said.contains(BikeNightTexts.GET_THEM),"waiting for Tully to have had enough: stage "+BikeNightBullies.stage(b));})
            // Out on the street, beyond the railings: they come by the gate.
            .thenExecute(()->f.at(p[0],P,-.5,0,-50.5))
            .thenWaitUntil(()->{
                hear.run();
                for(int i=0;i<3;i++){
                    boolean inside=bikes[i].getZ()>fence;
                    if(was[i]&&!inside&&Double.isNaN(crossed[0]))crossed[0]=bikes[i].getX()-b.getX();
                    was[i]=inside;nearest[0]=Math.min(nearest[0],bikes[i].position().distanceTo(p[0].position()));
                }
                h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE&&!Double.isNaN(crossed[0])&&nearest[0]<3,"waiting for one of them to be out on the street after the reader: stage "+BikeNightBullies.stage(b)+", crossed at "+crossed[0]+", nearest "+nearest[0]);})
            .thenExecute(()->{
                for(int i=0;i<3;i++)h.assertTrue(who[i].getVehicle()==bikes[i]&&who[i].gesture()==BikeNightBullies.RIDE,BikeNightBullies.NAMES[i]+" ran for his bike and is on it");
                h.assertTrue(crossed[0]>BikeNightSite.PATH_W-.2&&crossed[0]<BikeNightSite.PATH_E+1.2,"and he came out through the gateway, not the railings: at "+crossed[0]);
                // The night over: nobody, no bikes of theirs, and the gate chained again.
                BikeNight.resetNow(l,b);
                h.assertTrue(!BikeNightSite.gateIsOpen(l,b)&&l.getEntitiesOfClass(PackBike.class,room(f).inflate(40)).stream().noneMatch(Entity::isAlive)
                    &&l.getEntitiesOfClass(LiteraryActor.class,room(f).inflate(40),BikeNightBullies::is).stream().noneMatch(Entity::isAlive),"the night over, they are gone, bikes and all, and the gate is chained");
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="park_sand",timeoutTicks=2400)
    public static void sandIsPackedIntoBallsThrownAndBurstsAndIsGoneOutsideTheNight(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};var item=BikeNightRegistry.SAND_BALL.get();var sand=new BlockPos(-16,-1,-22);
        java.util.function.Supplier<List<SandBall>> flying=()->l.getEntitiesOfClass(SandBall.class,room(f));
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{p[0]=f.player("sand_one",P);p[1]=f.player("sand_two",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null&&BikeNight.failingLampLit(b),"the night has begun"))
            .thenExecute(()->{
                h.assertTrue(BikeNightSite.sandAt(sand)&&!BikeNightSite.sandAt(sand.above())&&!BikeNightSite.sandAt(new BlockPos(-28,-1,-10))&&item.getDefaultMaxStackSize()==SandBallItem.MOST,"the playground's sand, and no other ground");
                hush(p[0]);f.click(p[0],P,sand);
                h.assertTrue(balls(p[0])==1&&p[0].getMainHandItem().is(item)&&p[0].getCooldowns().isOnCooldown(item),"a hand in the sand packs a ball of it, into the hand");
                f.click(p[0],P,sand);
                h.assertTrue(balls(p[0])==1,"one at a time");
                f.click(p[0],P,new BlockPos(-28,-1,-10));
                h.assertTrue(balls(p[0])==1,"grass packs nothing");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.SAND)&&sounded(p[0],"park.sand_scoop"),"told so, and heard"))
            .thenExecute(()->{
                hush(p[0]);
                for(int i=0;i<12;i++){p[0].getCooldowns().removeCooldown(item);f.click(p[0],P,sand);}
                h.assertTrue(balls(p[0])==SandBallItem.MOST,"eight at most: "+balls(p[0]));
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.SAND_FULL),"told their pockets are full"))
            .thenExecute(()->{
                // Thrown at another reader two blocks off: it bursts on them and hurts nobody.
                f.at(p[0],P,-16.5,0,-22.5);f.at(p[1],P,-16.5,0,-24.5);p[0].getCooldowns().removeCooldown(item);hush(p[1]);
                // Aimed at their middle, from wherever the thrower's eyes are (a case's reader keeps a grown body's eyes until it is next measured).
                p[0].lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,p[1].position().add(0,.45,0));
                p[0].gameMode.useItem(p[0],l,p[0].getMainHandItem(),InteractionHand.MAIN_HAND);
                var thrown=flying.get();
                h.assertTrue(balls(p[0])==SandBallItem.MOST-1&&thrown.size()==1&&thrown.getFirst().getOwner()==p[0]&&thrown.getFirst().getDeltaMovement().z< -.9&&p[0].getCooldowns().isOnCooldown(item),"using it throws it, the way the reader looks: "+thrown);
            })
            .thenWaitUntil(()->h.assertTrue(flying.get().isEmpty(),"the ball is gone where it burst"))
            .thenExecute(()->{
                h.assertTrue(p[1].getHealth()==p[1].getMaxHealth()&&p[1].getDeltaMovement().z<0,"it bursts on the other reader, who is not hurt, and is knocked a little the way it was going: "+p[1].getDeltaMovement()+", health "+p[1].getHealth());
                p[1].setDeltaMovement(Vec3.ZERO);
            })
            .thenWaitUntil(()->h.assertTrue(sounded(p[1],"park.sand_burst"),"and is heard"))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt("Sand")==1&&f.own(p[0],P).getInt("SandHits")==1,"a ball thrown and a ball that met somebody, in the thrower's record: "+f.own(p[0],P)))
            // The failing lamp, hit while it is out, comes back on.
            .thenExecute(()->BikeNight.lampNow(b))
            .thenWaitUntil(()->h.assertTrue(!BikeNight.failingLampLit(b),"the failing lamp goes out"))
            .thenExecute(()->{
                BikeNight.lampHold(b,100000);
                var lamp=BikeNightSite.FLICKER;f.at(p[0],P,lamp.getX()+.5,0,lamp.getZ()+2.5);f.look(p[0],b.offset(lamp));p[0].getCooldowns().removeCooldown(item);
                p[0].gameMode.useItem(p[0],l,p[0].getMainHandItem(),InteractionHand.MAIN_HAND);
                h.assertTrue(flying.get().size()==1&&!BikeNight.failingLampLit(b),"a ball thrown at it while it is out");
            })
            .thenWaitUntil(()->h.assertTrue(flying.get().isEmpty()&&BikeNight.failingLampLit(b)&&BikeNightSite.lit(l,b,BikeNightSite.FLICKER),"and it comes back on, as a kicked one does"))
            .thenExecute(()->{
                // With sand in the hand, a hand on the park's things is not looking: it says nothing, and the throw is let happen.
                hush(p[0]);h.assertTrue(p[0].getMainHandItem().is(item)&&!BikeNight.click(p[0],b,new BlockPos(3,0,-16),f.own(p[0],P)),"sand in the hand does not stop to look at a bench");
                // Let fall, a ball falls apart where it lands.
                var dropped=new net.minecraft.world.entity.item.ItemEntity(l,b.getX()-15.5,b.getY()+.1,b.getZ()-20.5,new net.minecraft.world.item.ItemStack(item));dropped.setOnGround(true);
                h.assertTrue(item.onEntityItemUpdate(dropped.getItem(),dropped)&&dropped.isRemoved(),"let fall, it falls apart");
                // Carried out of the night, there is nothing left of it.
                int had=balls(p[0]);h.assertTrue(had==SandBallItem.MOST-2,"six left: "+had);
                // The ball looks once a second, on its holder's own count of ticks; the case gives it the tick it looks on.
                p[0].tickCount=40;p[0].getMainHandItem().inventoryTick(l,p[0],p[0].getInventory().selected,true);
                h.assertTrue(balls(p[0])==had,"in the night it keeps");
                p[0].teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+40,f.origin.getZ()+.5,0,0);
            })
            .thenWaitUntil(()->h.assertTrue(LiteraryVignettes.current(p[0])!=P,"the reader is out of the night"))
            .thenExecute(()->{
                hush(p[0]);var held=p[0].getMainHandItem();
                p[0].tickCount=41;held.inventoryTick(p[0].level(),p[0],p[0].getInventory().selected,true);
                h.assertTrue(balls(p[0])==SandBallItem.MOST-2,"it does not look on every tick");
                p[0].tickCount=60;held.inventoryTick(p[0].level(),p[0],p[0].getInventory().selected,true);
                h.assertTrue(balls(p[0])==0,"carried out of the night, the sand is gone: "+balls(p[0]));
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.SAND_GONE),"and they are told it ran out through their fingers"))
            .thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.466: the slide
    @GameTest(template="empty",batch="park_slide",timeoutTicks=2400)
    public static void theSlideIsClimbedAndAReaderWhoComesIntoTheTopOfItsChuteGoesDownIt(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final SlideRide[] ride={null};final Vec3[] last={null};final double[] fastest={0};
        var fwd=BikeNightSite.SLIDE_FORWARD;var ladder=b.offset(BikeNightSite.SLIDE);var deck=b.offset(BikeNightSite.slideDeck());var top=b.offset(BikeNightSite.slideTop());var end=b.offset(BikeNightSite.slideEnd());
        java.util.function.Function<BlockPos,Double> surface=at->at.getY()-b.getY()+l.getBlockState(at).getCollisionShape(l,at).max(Direction.Axis.Y);
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{
                // The ladder, two blocks of it, its rungs against the deck; the deck two blocks up under its guard.
                for(int y=0;y<2;y++){var s=l.getBlockState(ladder.above(y));
                    h.assertTrue(CalderPlayBlock.is(s,CalderPlayBlock.Kind.LADDER)&&s.getValue(CalderPlayBlock.STAGE)==y&&s.getValue(CalderPlayBlock.FACING)==fwd&&s.isLadder(l,ladder.above(y),null),"the ladder's block "+y+", which is climbed: "+s);}
                h.assertTrue(l.getBlockState(ladder.below()).is(Blocks.SAND)&&l.getBlockState(ladder.above(2)).isAir(),"it stands on the sand, and is open above");
                var d=l.getBlockState(deck);var guard=l.getBlockState(deck.above());
                h.assertTrue(deck.equals(ladder.relative(fwd).above())&&CalderPlayBlock.is(d,CalderPlayBlock.Kind.DECK)&&d.getValue(CalderPlayBlock.STAGE)==0&&d.getValue(CalderPlayBlock.FACING)==fwd&&!d.isLadder(l,deck,null)&&Math.abs(surface.apply(deck)-CalderPlayBlock.DECK_HEIGHT)<1e-6,"the deck, two blocks up: "+d+" at "+surface.apply(deck));
                h.assertTrue(CalderPlayBlock.is(guard,CalderPlayBlock.Kind.DECK)&&guard.getValue(CalderPlayBlock.STAGE)==CalderPlayBlock.GUARD&&l.getBlockState(deck.below()).isAir(),"its guard over it, and room under it: "+guard);
                var walls=guard.getCollisionShape(l,deck.above());var side=fwd.getClockWise().getAxis();
                h.assertTrue(walls.min(side)==0&&walls.max(side)==1&&walls.max(Direction.Axis.Y)==1&&walls.toAabbs().size()==2&&walls.toAabbs().stream().allMatch(box->side==Direction.Axis.X?box.getXsize()<.2:box.getZsize()<.2),"the guard is a rail each side and open at both ends: "+walls);
                // The chute: four blocks, each a little lower than the last, by the same fall; then the level end.
                double before=CalderPlayBlock.DECK_HEIGHT;
                for(int k=0;k<CalderPlayBlock.CHUTES;k++){
                    var at=b.offset(BikeNightSite.slideTop().relative(fwd,k));if(CalderPlayBlock.chuteLow(k))at=at.below();var s=l.getBlockState(at);
                    h.assertTrue(CalderPlayBlock.is(s,CalderPlayBlock.Kind.CHUTE)&&s.getValue(CalderPlayBlock.STAGE)==k&&s.getValue(CalderPlayBlock.FACING)==fwd,"the chute's block "+k+": "+s+" at "+at.subtract(b));
                    double here=surface.apply(at),want=CalderPlayBlock.DECK_HEIGHT-(k+.25)*CalderPlayBlock.SLOPE;
                    h.assertTrue(Math.abs(here-want)<1e-6&&here<before&&before-here<.6,"it is stood on, a step down from the last that a child takes: "+here+" after "+before);before=here;
                }
                h.assertTrue(l.getBlockState(top.below()).isAir()&&l.getBlockState(top.relative(fwd).below()).isAir(),"a body goes under the top of the chute");
                var out=l.getBlockState(end);
                h.assertTrue(CalderPlayBlock.is(out,CalderPlayBlock.Kind.RUNOUT)&&out.getValue(CalderPlayBlock.FACING)==fwd&&Math.abs(surface.apply(end)-(CalderPlayBlock.DECK_HEIGHT-CalderPlayBlock.CHUTES*CalderPlayBlock.SLOPE))<1e-6&&l.getBlockState(end.relative(fwd)).isAir()&&l.getBlockState(end.relative(fwd).below()).is(Blocks.SAND),"the level end, a third of a block up, and open sand past it");
                // The ride's own arithmetic: faster all the way down, level at the end, and where the chute's surface is.
                h.assertTrue(SlideRide.along(0)==0&&Math.abs(SlideRide.along(SlideRide.DOWN)-CalderPlayBlock.CHUTES)<1e-9&&Math.abs(SlideRide.along(SlideRide.WHOLE)-(CalderPlayBlock.CHUTES+SlideRide.LEVEL))<1e-9&&SlideRide.along(10)-SlideRide.along(9)>SlideRide.along(5)-SlideRide.along(4),"down the chute and along the level end, gaining all the way");
                var start=SlideRide.at(top,fwd,0);var bottom=SlideRide.at(top,fwd,CalderPlayBlock.CHUTES);
                h.assertTrue(Math.abs(start.y-b.getY()-CalderPlayBlock.DECK_HEIGHT)<1e-9&&Math.abs(bottom.y-b.getY()-surface.apply(end))<1e-9&&start.distanceTo(Vec3.atBottomCenterOf(top).add(-fwd.getStepX()*.5,1,-fwd.getStepZ()*.5))<1e-9,"from the deck's height at the chute's uphill edge to the level end's");
                p[0]=f.player("slide_one",P);p[1]=f.player("slide_two",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenExecute(()->{
                // A reader comes into the top of the chute, as one stepping off the deck does.
                hush(p[0]);f.at(p[0],P,BikeNightSite.slideTop().getX()+.5,1.9,BikeNightSite.slideTop().getZ()+.5);
                l.getBlockState(top).entityInside(l,top,p[0]);
                h.assertTrue(p[0].getVehicle() instanceof SlideRide r&&r.top().equals(top)&&r.forward()==fwd&&Mth.degreesDifferenceAbs(p[0].getYRot(),fwd.toYRot())<1,"they sit, looking down it");
                ride[0]=(SlideRide)p[0].getVehicle();last[0]=ride[0].position();
                h.assertTrue(SlideRide.begin(p[0],top,fwd)==null,"somebody already going down is not taken again");
                // Coming into any other block of it takes nobody.
                l.getBlockState(top.relative(fwd)).entityInside(l,top.relative(fwd),p[1]);l.getBlockState(deck).entityInside(l,deck,p[1]);
                h.assertTrue(!p[1].isPassenger(),"only the top of the chute takes a reader");
            })
            .thenExecuteFor((int)SlideRide.DOWN-2,()->{
                var at=ride[0].position();double t=ride[0].ticks(0);
                double now=at.distanceTo(SlideRide.at(top,fwd,SlideRide.along(t))),before=at.distanceTo(SlideRide.at(top,fwd,SlideRide.along(t-1)));
                h.assertTrue(Math.min(now,before)<1e-6,"the ride is where the chute's surface is: "+now+" "+before);
                double step=at.distanceTo(last[0]);
                h.assertTrue(at.y<=last[0].y+1e-9&&step>=fastest[0]-1e-9&&p[0].getVehicle()==ride[0],"lower and faster every tick: "+step+" after "+fastest[0]);
                fastest[0]=Math.max(fastest[0],step);last[0]=at;
            })
            .thenWaitUntil(()->h.assertTrue(ride[0].isRemoved()&&!p[0].isPassenger(),"let go off the level end"))
            .thenExecute(()->{
                var off=SlideRide.at(top,fwd,CalderPlayBlock.CHUTES+SlideRide.LEVEL);var v=p[0].getDeltaMovement();
                h.assertTrue(p[0].position().distanceTo(off.add(0,.05,0))<1e-6&&fastest[0]>.3&&fastest[0]<.45,"at the end of it, having come down fast: "+p[0].position().subtract(off)+", "+fastest[0]+" blocks a tick");
                h.assertTrue(Math.abs(v.x-fwd.getStepX()*SlideRide.SPEED*SlideRide.KEEP)<1e-6&&Math.abs(v.z-fwd.getStepZ()*SlideRide.SPEED*SlideRide.KEEP)<1e-6&&Math.abs(v.y-SlideRide.LIFT)<1e-6,"with the speed they came down with, out over the sand: "+v);
                p[0].setDeltaMovement(Vec3.ZERO);
            })
            .thenWaitUntil(()->h.assertTrue(sounded(p[0],"park.slide"),"and it was heard"))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt("Slid")==1,"once down, in their record"))
            .thenExecute(()->{
                // Getting off in the top of the chute puts a reader back on the deck: left inside the top, they would be taken again at once.
                f.at(p[1],P,BikeNightSite.slideTop().getX()+.5,1.9,BikeNightSite.slideTop().getZ()+.5);l.getBlockState(top).entityInside(l,top,p[1]);
                h.assertTrue(p[1].getVehicle() instanceof SlideRide,"a second reader goes down");ride[0]=(SlideRide)p[1].getVehicle();
            })
            .thenIdle(4)
            .thenExecute(()->{var first=ride[0];p[1].stopRiding();var on=Vec3.atBottomCenterOf(deck).add(0,1,0);
                h.assertTrue(SlideRide.along(first.ticks(0))<1&&p[1].position().distanceTo(on)<.05&&!p[1].getBoundingBox().intersects(new net.minecraft.world.phys.AABB(top)),"off it in the top of the chute, back on the deck and out of the chute: "+p[1].position().subtract(on));})
            .thenIdle(5)
            .thenExecute(()->{
                h.assertTrue(ride[0].isRemoved()&&!p[1].isPassenger()&&f.own(p[1],P).getInt("Slid")==0,"that ride is gone, and was not a go down it");
                // Further down, getting off leaves a reader on the chute where they were.
                f.at(p[1],P,BikeNightSite.slideTop().getX()+.5,1.9,BikeNightSite.slideTop().getZ()+.5);l.getBlockState(top).entityInside(l,top,p[1]);
                h.assertTrue(p[1].getVehicle() instanceof SlideRide,"they go again");ride[0]=(SlideRide)p[1].getVehicle();
            })
            .thenIdle(14)
            .thenExecute(()->{var at=ride[0].position();double along=SlideRide.along(ride[0].ticks(0));p[1].stopRiding();
                h.assertTrue(along>=1&&along<CalderPlayBlock.CHUTES&&p[1].position().distanceTo(at.add(0,.1,0))<.05,"off it part of the way down, where they were: "+p[1].position().subtract(at)+" at "+along);})
            .thenIdle(30)
            .thenExecute(()->{
                h.assertTrue(ride[0].isRemoved()&&f.own(p[1],P).getInt("Slid")==0,"the ride is gone with its rider, and that was not a go down it");
                // Twice more for the first reader: their account says they went round again.
                f.at(p[0],P,BikeNightSite.slideTop().getX()+.5,1.9,BikeNightSite.slideTop().getZ()+.5);l.getBlockState(top).entityInside(l,top,p[0]);
            })
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt("Slid")==2,"twice down"))
            .thenExecute(()->{p[0].setDeltaMovement(Vec3.ZERO);f.at(p[0],P,BikeNightSite.slideTop().getX()+.5,1.9,BikeNightSite.slideTop().getZ()+.5);l.getBlockState(top).entityInside(l,top,p[0]);})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt("Slid")==3,"three times down"))
            .thenExecute(()->{
                var account=String.join(" ",BikeNightTexts.account("slide_one",f.own(p[0],P)).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(account.contains("went round and did it again"),"and their account has it: "+account);
            }).thenSucceed();
    }

    /** A hand on a block from where the reader is (the fixture's click moves them first). */
    private static void hand(ServerPlayer p,BlockPos at){
        p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,at.getCenter());
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new net.minecraft.world.phys.BlockHitResult(at.getCenter(),Direction.DOWN,at,false)));
    }

    @GameTest(template="empty",batch="park_bars",timeoutTicks=2400)
    public static void theMonkeyBarsAreCrossedHandOverHandByReadersWhoCanReachThem(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final BarsHang[] hold={null,null};final double[] last={0};
        int n=BikeNightSite.rungs();var first=b.offset(BikeNightSite.rungsAt(0));var end=b.offset(BikeNightSite.rungsAt(n-1));var mid=b.offset(BikeNightSite.rungsAt(2));
        double z=BikeNightSite.BARS_Z+.5,lo=BikeNightSite.BARS_W+.25,hi=BikeNightSite.BARS_E+.75,high=BikeNightSite.BARS_Y+CalderPlayBlock.RUNG;
        var seq=h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{
                // A ladder at each end, facing the rungs, in the bars' paint, and climbed; the rungs between them.
                for(int e=0;e<2;e++){
                    var toward=e==0?Direction.EAST:Direction.WEST;var foot=b.offset(BikeNightSite.barsLadder(e));
                    for(int y=0;y<BikeNightSite.BARS_Y;y++){
                        var at=foot.above(y);var s=l.getBlockState(at);
                        h.assertTrue(CalderPlayBlock.is(s,CalderPlayBlock.Kind.LADDER)&&s.getValue(CalderPlayBlock.STAGE)==CalderPlayBlock.BARS+(y==BikeNightSite.BARS_Y-1?1:0)&&s.getValue(CalderPlayBlock.FACING)==toward&&s.isLadder(l,at,null),"the bars' ladder "+e+", block "+y+": "+s);
                    }
                    h.assertTrue(l.getBlockState(foot.below()).is(Blocks.SAND)&&l.getBlockState(foot.above(BikeNightSite.BARS_Y)).isAir(),"it stands on the sand, and is open above");
                    h.assertTrue(foot.above(BikeNightSite.BARS_Y).relative(toward).equals(e==0?first:end),"the rungs begin beside the top of it");
                }
                for(int k=0;k<n;k++){
                    var at=b.offset(BikeNightSite.rungsAt(k));var s=l.getBlockState(at);boolean ends=k==0||k==n-1;
                    h.assertTrue(CalderPlayBlock.is(s,CalderPlayBlock.Kind.RUNGS)&&s.getValue(CalderPlayBlock.STAGE)==(ends?CalderPlayBlock.RUNG_END:0)&&s.getValue(CalderPlayBlock.FACING)==(k==0?Direction.WEST:Direction.EAST),"the rungs' block "+k+": "+s);
                    var shape=s.getCollisionShape(l,at);
                    h.assertTrue(Math.abs(shape.max(Direction.Axis.Y)-CalderPlayBlock.RUNGS_THICK/16)<1e-9&&shape.min(Direction.Axis.Y)==0&&shape.min(Direction.Axis.X)==0&&shape.max(Direction.Axis.X)==1,"they are stood on, a thin run at the bottom of their block: "+shape);
                    h.assertTrue(l.getBlockState(at.below()).isAir()&&l.getBlockState(at.below(2)).isAir()&&l.getBlockState(at.below(3)).is(Blocks.SAND),"with room under them, over the sand");
                }
                var run=BarsHang.run(l,mid);
                h.assertTrue(Math.abs(run[0]-(b.getX()+lo))<1e-9&&Math.abs(run[1]-(b.getX()+hi))<1e-9&&n>=4,"the first and last rungs: "+run[0]+" "+run[1]);
                p[0]=f.player("bars_one",P);p[1]=f.player("bars_two",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            // A reader is made a child on their first tick in the night, a few ticks after they arrive.
            .thenWaitUntil(()->h.assertTrue(p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1,"readers here are children: "+p[0].getBbHeight()))
            .thenExecute(()->{
                // A child on the ground cannot reach them.
                double tall=p[0].getBbHeight();
                h.assertTrue(tall*BarsHang.REACH+BarsHang.STRETCH<BikeNightSite.BARS_Y,"the rungs are out of a standing child's reach: "+tall);
                hush(p[0]);f.at(p[0],P,BikeNightSite.BARS_W+2.5,0,z);hand(p[0],mid);
                h.assertTrue(!p[0].isPassenger()&&BikeNightTexts.BARS_LOW.equals(BarsHang.cannot(p[0],mid)),"from the ground they are out of reach");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.BARS_LOW),"and they are told so"))
            .thenExecute(()->{
                // Nor from beside them, nor from past their end, nor from on top of them.
                f.at(p[0],P,BikeNightSite.BARS_W+2.5,1,z+1.5);h.assertTrue(BikeNightTexts.BARS_FAR.equals(BarsHang.cannot(p[0],mid)),"not from beside them");
                f.at(p[0],P,BikeNightSite.BARS_W-3,1,z);h.assertTrue(BikeNightTexts.BARS_FAR.equals(BarsHang.cannot(p[0],first)),"not from well past their end");
                f.at(p[0],P,BikeNightSite.BARS_W+2.5,BikeNightSite.BARS_Y+CalderPlayBlock.RUNGS_THICK/16,z);
                h.assertTrue(BikeNightTexts.BARS_ON.equals(BarsHang.cannot(p[0],mid))&&BarsHang.take(p[0],mid)==null,"not from on top of them");
                // From up the west ladder, a hand on the first rungs takes hold: over the first rung.
                hush(p[0]);f.at(p[0],P,BikeNightSite.BARS_W-.6,.9,z);hand(p[0],first);
                h.assertTrue(p[0].getVehicle() instanceof BarsHang,"from up the ladder a hand on the rungs takes hold");
                hold[0]=(BarsHang)p[0].getVehicle();
                h.assertTrue(hold[0].alongX()&&Math.abs(hold[0].getX()-(b.getX()+lo))<1e-9&&Math.abs(hold[0].getY()-(b.getY()+high))<1e-9&&Math.abs(hold[0].getZ()-(b.getZ()+z))<1e-9&&!hold[0].shouldRiderSit(),"the hold is on the first rung, at the rungs' height, and does not sit them down: "+hold[0].position().subtract(b.getX(),b.getY(),b.getZ()));
                h.assertTrue(BarsHang.take(p[0],first)==null,"somebody hanging takes no second hold");
                last[0]=hold[0].getX();
            })
            .thenIdle(3)
            .thenExecute(()->{
                // They hang at their full length under it, clear of the sand; hanging still.
                double feet=hold[0].getY()-p[0].getBbHeight()*BarsHang.REACH;
                h.assertTrue(Math.abs(p[0].getY()-feet)<1e-6&&Math.abs(p[0].getX()-hold[0].getX())<1e-6&&p[0].getY()-b.getY()>.6&&p[0].getY()+p[0].getBbHeight()<hold[0].getY()+.05,"hanging by their hands, their feet off the sand: "+(p[0].getY()-b.getY()));
                h.assertTrue(hold[0].getX()==last[0]&&hold[0].swingTicks(0)<0&&BarsHang.swung(-1)==0,"hanging still");
                // The walk keys take them nowhere: it is done with the hands.
                p[0].setYRot(-90);p[0].setXRot(0);p[0].setPlayerInput(0,1,false,false);
            })
            .thenIdle(5)
            .thenExecute(()->{
                h.assertTrue(hold[0].getX()==last[0]&&hold[0].swingTicks(0)<0,"the walk keys move nothing");
                // Looking square across the bars, a strafe key swings them neither way.
                p[0].setYRot(0);p[0].setPlayerInput(1,0,false,false);
            })
            .thenIdle(2)
            .thenExecute(()->{
                h.assertTrue(hold[0].swingTicks(0)<0,"a look square across swings them neither way");
                p[0].setPlayerInput(0,0,false,false);p[0].setYRot(-90);
            })
            .thenIdle(1)
            // Looking east along them, a strafe key swings the body that way; their hands stay where they are.
            .thenExecute(()->p[0].setPlayerInput(1,0,false,false))
            .thenIdle(1)
            .thenExecute(()->{
                p[0].setPlayerInput(0,0,false,false);
                h.assertTrue(hold[0].swingTicks(0)>=0&&hold[0].swingWay()==1&&hold[0].getX()==last[0],"a strafe key swings the body the way they look: "+hold[0].swingTicks(0));
                h.assertTrue(BarsHang.swung(BarsHang.TOP)>BarsHang.SWING*.9&&BarsHang.swung(BarsHang.TOP)>BarsHang.swung(BarsHang.EARLY-3)&&Math.abs(BarsHang.swung(2*BarsHang.TOP))<1e-9&&BarsHang.swung(2.5*BarsHang.TOP)<0&&BarsHang.swung(BarsHang.STILL)==0,
                    "the body swings out to its top, back through the middle, a little the other way, and is still");
            });
        // A strafe key at the top of each swing catches the next rung, and that catch is the next swing's beginning.
        Runnable press=()->p[0].setPlayerInput(-1,0,false,false);
        java.util.function.IntFunction<Runnable> caught=k->()->{
            p[0].setPlayerInput(0,0,false,false);
            h.assertTrue(Math.abs(hold[0].getX()-last[0]-BarsHang.HAND)<1e-9&&hold[0].swingTicks(0)<=1&&p[0].getVehicle()==hold[0],"catch "+k+": at the top of the swing a strafe key catches the next rung: "+(hold[0].getX()-last[0])+" after "+hold[0].swingTicks(0));
            last[0]=hold[0].getX();
        };
        for(int k=1;k<=2;k++)seq=seq.thenIdle(BarsHang.TOP-1).thenExecute(press).thenIdle(1).thenExecute(caught.apply(k));
        seq=seq
            .thenExecute(()->{
                // A second reader jumps under the far end: in the air there, near enough, they catch a rung without a hand on it.
                f.at(p[1],P,BikeNightSite.BARS_E+.6,.9,z);p[1].setOnGround(false);
            })
            .thenWaitUntil(()->h.assertTrue(p[1].getVehicle() instanceof BarsHang other&&other!=hold[0],"a jump at the bars grabs one; two readers hang from the same bars"))
            .thenExecute(()->{
                hold[1]=(BarsHang)p[1].getVehicle();
                h.assertTrue(Math.abs(hold[1].getX()-(b.getX()+hi))<1e-9,"the rung nearest over them: "+(hold[1].getX()-b.getX()));
            })
            // The first reader lets the swing go back: a strafe key then swings them again, and nothing is lost.
            .thenIdle(BarsHang.LATE+2)
            .thenExecute(press).thenIdle(1)
            .thenExecute(()->{
                p[0].setPlayerInput(0,0,false,false);
                h.assertTrue(hold[0].getX()==last[0]&&hold[0].swingTicks(0)>=0&&hold[0].swingTicks(0)<=1&&p[0].getVehicle()==hold[0],"after the swing has gone back a strafe key swings them again: "+hold[0].swingTicks(0));
                // The second reader swings, and goes for the next rung too early: the hand misses, and they drop.
                hush(p[1]);p[1].setYRot(90);p[1].setXRot(0);p[1].setPlayerInput(1,0,false,false);
            })
            .thenIdle(1).thenExecute(()->p[1].setPlayerInput(0,0,false,false))
            .thenIdle(1).thenExecute(()->{h.assertTrue(hold[1].swingTicks(0)>=0&&hold[1].swingWay()==-1&&p[1].getVehicle()==hold[1],"the second reader swings west");p[1].setPlayerInput(-1,0,false,false);})
            .thenIdle(1)
            .thenExecute(()->{
                p[1].setPlayerInput(0,0,false,false);var feet=new Vec3(hold[1].getX(),hold[1].getY()-p[1].getBbHeight()*BarsHang.REACH,hold[1].getZ());
                h.assertTrue(!p[1].isPassenger()&&p[1].position().distanceTo(feet)<.05,"too early: the hand misses and they drop where they hung");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[1],BikeNightTexts.BARS_EARLY),"and they are told why"))
            .thenIdle(6)
            .thenExecute(()->{
                h.assertTrue(!p[1].isPassenger()&&hold[1].isRemoved(),"having let go they do not catch the bars again in the same fall, and their hold is gone");
                // On the sand, and up again: a jump catches them once more.
                p[1].setOnGround(true);
            })
            .thenIdle(2)
            .thenExecute(()->p[1].setOnGround(false))
            .thenWaitUntil(()->h.assertTrue(p[1].getVehicle() instanceof BarsHang,"back on the sand and up again, a jump catches the bars once more"))
            .thenExecute(()->hold[1]=(BarsHang)p[1].getVehicle());
        // The first reader has hung still all this while: a strafe key swings them again, and they go on, rung by rung, to the last.
        seq=seq.thenExecute(press).thenIdle(1).thenExecute(()->{
            p[0].setPlayerInput(0,0,false,false);
            h.assertTrue(hold[0].getX()==last[0]&&hold[0].swingTicks(0)>=0&&hold[0].swingTicks(0)<=1,"hanging still again, a strafe key swings them: "+hold[0].swingTicks(0));
        });
        int rungs=(int)Math.round((hi-lo)/BarsHang.HAND);
        for(int k=3;k<=rungs;k++)seq=seq.thenIdle(BarsHang.TOP-1).thenExecute(press).thenIdle(1).thenExecute(caught.apply(k));
        seq.thenExecute(()->h.assertTrue(Math.abs(hold[0].getX()-(b.getX()+hi))<1e-9,"at the last rung: "+(hold[0].getX()-b.getX())))
            // On the last rung there is no next: a strafe key at the top swings them, and they stay.
            .thenIdle(BarsHang.TOP-1).thenExecute(press).thenIdle(1)
            .thenExecute(()->{p[0].setPlayerInput(0,0,false,false);h.assertTrue(Math.abs(hold[0].getX()-(b.getX()+hi))<1e-9&&p[0].getVehicle()==hold[0]&&hold[0].swingTicks(0)<=1,"and no further");})
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.BARS_ACROSS)&&heard(p[0],BikeNightTexts.BARS_HINT),"across, and told so; and told once how it is done"))
            .thenWaitUntil(()->h.assertTrue(sounded(p[0],"park.bars"),"and the rungs were heard"))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt("Crossed")==1&&f.own(p[0],P).getInt("Hung")==1&&f.own(p[0],P).getInt("BarsMissed")==0&&f.own(p[1],P).getInt("Hung")==2&&f.own(p[1],P).getInt("Crossed")==0&&f.own(p[1],P).getInt("BarsMissed")==1,
                "in their records: one across; the other twice on them, and one rung missed"))
            .thenExecute(()->{
                // Letting go drops them where they hang.
                var feet=new Vec3(hold[0].getX(),hold[0].getY()-p[0].getBbHeight()*BarsHang.REACH,hold[0].getZ());p[0].setPlayerInput(0,0,false,false);p[0].stopRiding();
                h.assertTrue(p[0].position().distanceTo(feet)<.05&&!p[0].isPassenger(),"letting go, they are where they hung: "+p[0].position().subtract(feet));
            })
            .thenIdle(3)
            .thenExecute(()->{
                h.assertTrue(hold[0].isRemoved()&&!hold[1].isRemoved()&&p[1].getVehicle()==hold[1],"that hold is gone with its rider; the other reader's is not");
                // The rungs taken away from over somebody: their hold goes with them.
                l.setBlock(end,Blocks.AIR.defaultBlockState(),3);
            })
            .thenIdle(3)
            .thenExecute(()->{
                h.assertTrue(hold[1].isRemoved()&&!p[1].isPassenger(),"no rungs, no hold");
                String one=told("bars_one",p[0],f),two=told("bars_two",p[1],f);
                h.assertTrue(one.contains("all the way across the monkey bars")&&two.contains("too early")&&!two.contains("all the way across"),"and their accounts have it: "+one+" / "+two);
            }).thenSucceed();
    }

    private static String told(String name,ServerPlayer p,LiteraryTests.Fixture f){
        return String.join(" ",BikeNightTexts.account(name,f.own(p,P)).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList());
    }

    @GameTest(template="empty",batch="park_seesaw",timeoutTicks=3000)
    public static void theSeesawGoesOverWhenWhoeverIsDownPushesOffAndOneAloneComesBackDown(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null,null};final SeesawBlock.Board[] board={null};final double[] low={0};
        var stand=b.offset(BikeNightSite.SEESAW);var facing=BikeNightSite.SEESAW_FACING;var end0=b.offset(BikeNightSite.seesawEnd(0));var end1=b.offset(BikeNightSite.seesawEnd(1));double max=SeesawBlock.MAX;
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{
                // The stand, which keeps the board, and the place each end of the board is over.
                var s=l.getBlockState(stand);
                h.assertTrue(SeesawBlock.is(s,SeesawBlock.Part.STAND)&&s.getValue(SeesawBlock.FACING)==facing&&l.getBlockEntity(stand) instanceof SeesawBlock.Board,"the stand, which keeps the board: "+s);
                h.assertTrue(!s.getCollisionShape(l,stand).isEmpty()&&l.getBlockState(stand.below()).is(Blocks.SAND),"it stops a body, and stands on the sand");
                board[0]=(SeesawBlock.Board)l.getBlockEntity(stand);
                for(int e=0;e<2;e++){
                    var at=e==0?end0:end1;var out=e==0?facing:facing.getOpposite();var es=l.getBlockState(at);
                    h.assertTrue(SeesawBlock.is(es,SeesawBlock.Part.END)&&es.getValue(SeesawBlock.FACING)==out&&at.equals(stand.relative(out,SeesawBlock.OUT))&&l.getBlockEntity(at)==null,"the place end "+e+" is over: "+es);
                    h.assertTrue(es.getCollisionShape(l,at).isEmpty()&&!es.getShape(l,at).isEmpty(),"a hand finds it, and it stops nobody");
                    h.assertTrue(SeesawBlock.board(l,at)==board[0]&&SeesawBlock.endOf(l,at)==e,"it knows its board, and which end of it it is");
                    h.assertTrue(l.getBlockState(stand.relative(out)).isAir()&&l.getBlockState(at.below()).is(Blocks.SAND),"between it and the stand the board runs over open air, over the sand");
                }
                // It stands with its first end down on its bumper.
                h.assertTrue(max>.15&&max<.3&&Math.abs(board[0].tilt()-max)<1e-9&&board[0].speed()==0&&board[0].down(0)&&!board[0].down(1)&&!board[0].ridden(0)&&!board[0].ridden(1),"it stands with its first end down, still, nobody on it: "+board[0].tilt());
                var lowSeat=SeesawBlock.seat(stand,facing,max,0);var highSeat=SeesawBlock.seat(stand,facing,max,1);var bumper=SeesawBlock.at(stand,facing,max,SeesawBlock.BUMPER_AT,-SeesawBlock.BUMPER);
                h.assertTrue(Math.abs(bumper.y-stand.getY())<1e-9,"as far over as it goes, the bumper under the low end is on the sand: "+(bumper.y-stand.getY()));
                h.assertTrue(lowSeat.y-stand.getY()>.15&&lowSeat.y-stand.getY()<.5&&highSeat.y-stand.getY()>.9&&highSeat.y-lowSeat.y>.6,"the low seat is one a child sits down onto, the high one is over their head: "+(lowSeat.y-stand.getY())+" "+(highSeat.y-stand.getY()));
                h.assertTrue(Math.abs(lowSeat.z-(end0.getZ()+.5))<.15&&Math.abs(highSeat.z-(end1.getZ()+.5))<.15&&Math.abs(lowSeat.x-(stand.getX()+.5))<1e-9,"each seat is over its end's place");
                var turned=SeesawBlock.seat(stand,facing,-max,1);
                h.assertTrue(Math.abs(turned.y-lowSeat.y)<1e-9&&Math.abs((turned.z-(stand.getZ()+.5))+(lowSeat.z-(stand.getZ()+.5)))<1e-9,"and the other way over is the same, turned about");
                p[0]=f.player("seesaw_one",P);p[1]=f.player("seesaw_two",P);p[2]=f.player("seesaw_three",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenWaitUntil(()->h.assertTrue(p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&p[2].getBbHeight()<1.1,"readers here are children"))
            .thenExecute(()->{
                // A hand on the low end sits on it.
                hush(p[0]);f.at(p[0],P,BikeNightSite.seesawEnd(0).getX()+1.5,0,BikeNightSite.seesawEnd(0).getZ()+.5);hand(p[0],end0);
                h.assertTrue(p[0].getVehicle() instanceof SeesawSeat seat&&seat.end()==0&&seat.stand().equals(stand),"a hand on the low end sits on it");
                h.assertTrue(!SeesawSeat.jump(p[1]),"nobody pushes it who is not on it");
            })
            .thenIdle(4)
            .thenExecute(()->{
                var seat=(SeesawSeat)p[0].getVehicle();var where=SeesawBlock.seat(stand,facing,board[0].tilt(),0);
                h.assertTrue(seat.position().distanceTo(where)<1e-6&&board[0].ridden(0)&&board[0].seat(0)==seat&&board[0].tilt()==max&&board[0].speed()==0,"the seat is on the board's end, and the board knows; their weight holds it down: "+board[0].tilt()+" "+board[0].speed());
                h.assertTrue(Math.abs(p[0].getX()-seat.getX())<1e-6&&Math.abs(p[0].getZ()-seat.getZ())<1e-6&&p[0].getY()<seat.getY()&&seat.getY()-p[0].getY()<.7,"and they are sat on it: "+(seat.getY()-p[0].getY()));
                // Alone, a push off the sand takes them up, and their weight brings them back down.
                hush(p[0]);h.assertTrue(SeesawSeat.jump(p[0]),"down, they push off");low[0]=max;
            })
            .thenExecuteFor(10,()->low[0]=Math.min(low[0],board[0].tilt()))
            .thenExecute(()->{
                h.assertTrue(board[0].tilt()<max-.1&&!board[0].down(0),"up off the sand: "+board[0].tilt());
                h.assertTrue(!SeesawSeat.jump(p[0]),"in the air there is nothing to push off");
            })
            .thenWaitUntil(()->{low[0]=Math.min(low[0],board[0].tilt());h.assertTrue(board[0].down(0)&&board[0].speed()==0,"and they come back down");})
            .thenExecute(()->h.assertTrue(low[0]>-max+.05&&low[0]<max-.15,"alone it never goes over: "+low[0]))
            .thenWaitUntil(()->h.assertTrue(sounded(p[0],"park.seesaw_creak")&&sounded(p[0],"park.seesaw_thud"),"the axle and the bumper were heard"))
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.SEESAW_HINT),"and they were told once how it is done"))
            .thenExecute(()->{
                // A second reader takes the high end, and a third finds both ends taken.
                f.at(p[1],P,BikeNightSite.seesawEnd(1).getX()+1.5,0,BikeNightSite.seesawEnd(1).getZ()+.5);hand(p[1],end1);
                h.assertTrue(p[1].getVehicle() instanceof SeesawSeat seat&&seat.end()==1,"a second reader sits on the high end");
                hush(p[2]);f.at(p[2],P,BikeNightSite.seesawEnd(0).getX()-1.5,0,BikeNightSite.seesawEnd(0).getZ()+.5);hand(p[2],end0);
                h.assertTrue(!p[2].isPassenger(),"nobody sits where somebody is");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[2],BikeNightTexts.SEESAW_TAKEN),"and they are told so"))
            .thenIdle(5)
            .thenExecute(()->{
                h.assertTrue(board[0].ridden(1)&&board[0].tilt()==max&&board[0].speed()==0&&p[1].getY()>p[0].getY()+.6,"two on it, of one weight: it stays as it was, the second up in the air");
                h.assertTrue(!SeesawSeat.jump(p[1]),"the one in the air cannot push");
                h.assertTrue(SeesawSeat.jump(p[0]),"the one who is down pushes off");
            })
            .thenWaitUntil(()->h.assertTrue(board[0].down(1)&&board[0].speed()==0,"and between two it goes all the way over"))
            .thenExecute(()->h.assertTrue(Math.abs(board[0].tilt()+max)<1e-9&&p[0].getY()>p[1].getY()+.6,"the first reader is up now, the second down"))
            // A push made some while after landing is only a push.
            .thenIdle(SeesawBlock.Board.GOOD+2)
            .thenExecute(()->{
                h.assertTrue(SeesawSeat.jump(p[1]),"and the second pushes off in their turn");
                h.assertTrue(Math.abs(Math.abs(board[0].speed())-SeesawBlock.Board.PUSH)<1e-9,"a push, some while after they landed, is only a push: "+board[0].speed());
            })
            .thenWaitUntil(()->h.assertTrue(board[0].down(0)&&board[0].speed()==0&&p[1].getY()>p[0].getY()+.6,"back over"))
            .thenIdle(SeesawBlock.Board.GOOD+2)
            .thenExecute(()->h.assertTrue(SeesawSeat.jump(p[0]),"over again"))
            .thenWaitUntil(()->h.assertTrue(board[0].down(1)&&board[0].speed()==0,"the first reader up in the air"))
            .thenExecute(()->{
                // The one who is down gets off: the one in the air comes down, with a bump.
                hush(p[0]);var seat=(SeesawSeat)p[1].getVehicle();var beside=seat.getDismountLocationForPassenger(p[1]);p[1].stopRiding();
                h.assertTrue(p[1].position().distanceTo(beside)<.05&&Math.abs(p[1].getY()-stand.getY())<1e-6&&Math.abs(Math.abs(p[1].getX()-(stand.getX()+.5))-SeesawSeat.ASIDE)<1e-6,"getting off puts them on the sand beside their end: "+p[1].position().subtract(beside));
            })
            .thenWaitUntil(()->h.assertTrue(board[0].down(0)&&board[0].speed()==0&&!board[0].ridden(1),"with nobody on the other end, the one in the air comes down"))
            .thenWaitUntil(()->h.assertTrue(sounded(p[0],"park.seesaw_thud"),"with a bump"))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt("Seesaw")==3&&"seesaw_two".equals(f.own(p[0],P).getString("SeesawWith"))&&f.own(p[1],P).getInt("Seesaw")==1&&"seesaw_one".equals(f.own(p[1],P).getString("SeesawWith"))&&f.own(p[0],P).getBoolean("SeesawSat")&&f.own(p[2],P).getInt("Seesaw")==0,
                "in their records: who pushed off how often, and with whom: "+f.own(p[0],P).getInt("Seesaw")+" "+f.own(p[1],P).getInt("Seesaw")))
            // The bounce. The second reader gets back on the high end, and the first pushes off.
            .thenExecute(()->{
                f.at(p[1],P,BikeNightSite.seesawEnd(1).getX()+1.5,0,BikeNightSite.seesawEnd(1).getZ()+.5);hand(p[1],end1);
                h.assertTrue(p[1].getVehicle() instanceof SeesawSeat,"the second reader gets back on");hush(p[0]);hush(p[1]);
            })
            .thenIdle(SeesawBlock.Board.GOOD+2)
            .thenExecute(()->h.assertTrue(SeesawSeat.jump(p[0]),"the first pushes off"))
            .thenWaitUntil(()->h.assertTrue(board[0].down(1)&&board[0].speed()==0,"over"))
            // A push made as a rider's end lands is a bounce: it turns round part of the speed that end came down with.
            .thenExecute(()->{
                h.assertTrue(SeesawSeat.jump(p[1]),"a push as they land");
                h.assertTrue(Math.abs(board[0].speed())>SeesawBlock.Board.PUSH+.01&&Math.abs(board[0].speed())<=SeesawBlock.Board.FASTEST,"is a bounce: the board goes back over faster than a push sends it: "+board[0].speed());
            })
            .thenWaitUntil(()->h.assertTrue(board[0].down(0)&&board[0].speed()==0,"back over, faster"))
            .thenExecute(()->{
                h.assertTrue(SeesawSeat.jump(p[0]),"and the first bounces in their turn");
                h.assertTrue(Math.abs(board[0].speed())>SeesawBlock.Board.PUSH+.025&&Math.abs(board[0].speed())<=SeesawBlock.Board.FASTEST,"faster again: "+board[0].speed());
            })
            // That brings the other end down hard, and the reader up on top is thrown off it.
            .thenWaitUntil(()->h.assertTrue(!p[0].isPassenger(),"a big bounce throws the one on top off"))
            .thenExecute(()->{
                var v=p[0].getDeltaMovement();
                h.assertTrue(v.y>.2&&v.y<=SeesawSeat.HIGHEST+1e-9&&v.horizontalDistance()>.05&&p[1].getVehicle() instanceof SeesawSeat&&board[0].down(1),"up and off it and a little outward; the other still sat on their end, down: "+v);
                h.assertTrue(p[0].getY()-stand.getY()>.9,"from the top: "+(p[0].getY()-stand.getY()));
                p[0].setDeltaMovement(Vec3.ZERO);
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.SEESAW_THROWN)&&heard(p[1],String.format(BikeNightTexts.SEESAW_THREW,"seesaw_one")),"and both are told"))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean("SeesawThrown")&&"seesaw_one".equals(f.own(p[1],P).getString("SeesawThrew"))&&f.own(p[1],P).getInt("SeesawBounces")==1&&f.own(p[0],P).getInt("SeesawBounces")==1,
                "and it is in their records: "+f.own(p[0],P).getInt("SeesawBounces")+" "+f.own(p[1],P).getInt("SeesawBounces")))
            .thenExecute(()->p[1].stopRiding())
            // The first reader gets off too. An end is free a moment after whoever was on it has gone.
            .thenExecute(()->f.at(p[0],P,BikeNightSite.seesawEnd(0).getX()+1.5,0,BikeNightSite.seesawEnd(0).getZ()+.5))
            .thenWaitUntil(()->h.assertTrue(!board[0].ridden(0)&&board[0].seat(0)==null,"the end is free again"))
            .thenExecute(()->{
                // The third reader has it alone, and their account says so.
                f.at(p[2],P,BikeNightSite.seesawEnd(0).getX()-1.5,0,BikeNightSite.seesawEnd(0).getZ()+.5);hand(p[2],end0);
                h.assertTrue(p[2].getVehicle() instanceof SeesawSeat,"and the third reader takes it");
            })
            // It was left with that end up: their weight brings it down.
            .thenWaitUntil(()->h.assertTrue(board[0].down(0)&&board[0].speed()==0,"their weight brings their end down"))
            .thenIdle(SeesawBlock.Board.GOOD+2)
            .thenExecute(()->h.assertTrue(SeesawSeat.jump(p[2]),"and they push off, alone"))
            .thenIdle(6)
            .thenWaitUntil(()->h.assertTrue(board[0].down(0)&&board[0].speed()==0&&f.own(p[2],P).getInt("Seesaw")==1,"and comes back down"))
            .thenExecute(()->p[2].stopRiding())
            .thenIdle(10)
            .thenExecute(()->{
                h.assertTrue(!board[0].ridden(0)&&!board[0].ridden(1)&&board[0].tilt()==max&&board[0].speed()==0,"left alone it stays as it was left");
                h.assertTrue(l.getEntitiesOfClass(SeesawSeat.class,new AABB(stand).inflate(6)).isEmpty(),"and no seat is left on it");
                String one=told("seesaw_one",p[0],f),two=told("seesaw_two",p[1],f),three=told("seesaw_three",p[2],f);
                h.assertTrue(one.contains("on the seesaw with seesaw_two on the other end")&&two.contains("on the seesaw with seesaw_one on the other end")&&three.contains("with nobody on the other"),"and their accounts have it: "+one+" / "+two+" / "+three);
                h.assertTrue(one.contains("a hard bump threw me off the top")&&two.contains("I bounced seesaw_one off the top"),"and who threw whom: "+one+" / "+two);
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="park_round",timeoutTicks=3000)
    public static void theMerryGoRoundIsShovedRoundFromTheSandAndCarriesWhoeverStandsOnIt(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final RoundaboutBlock.Wheel[] wheel={null};final double[] was={0,0};final net.minecraft.world.entity.Entity[] thing={null};
        var hub=b.offset(BikeNightSite.ROUNDABOUT);double cx=BikeNightSite.ROUNDABOUT.getX()+.5,cz=BikeNightSite.ROUNDABOUT.getZ()+.5,deck=RoundaboutBlock.DECK,top=RoundaboutBlock.Wheel.TOP;int wait=RoundaboutBlock.Wheel.WAIT;
        // How far round the hub something is, from east toward south, in degrees.
        java.util.function.Function<net.minecraft.world.entity.Entity,Double> round=e->Math.toDegrees(Math.atan2(e.getZ()-(b.getZ()+cz),e.getX()-(b.getX()+cx)));
        java.util.function.Function<net.minecraft.world.entity.Entity,Double> out=e->Math.hypot(e.getX()-(b.getX()+cx),e.getZ()-(b.getZ()+cz));
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{
                // The hub, which keeps the wheel, and the rest of the ground its deck is over.
                for(var cell:RoundaboutBlock.Cell.values()){
                    if(cell==RoundaboutBlock.Cell.WHEEL)continue;
                    var at=hub.offset(cell.dx,0,cell.dz);var s=l.getBlockState(at);
                    h.assertTrue(RoundaboutBlock.is(s)&&s.getValue(RoundaboutBlock.CELL)==cell&&RoundaboutBlock.hub(at,s).equals(hub)&&RoundaboutBlock.wheel(l,at)!=null&&l.getBlockState(at.below()).is(Blocks.SAND),"the merry-go-round's ground at "+cell+": "+s);
                    h.assertTrue((l.getBlockEntity(at) instanceof RoundaboutBlock.Wheel)==(cell==RoundaboutBlock.Cell.HUB),"only its hub keeps the wheel");
                    double high=s.getCollisionShape(l,at).max(Direction.Axis.Y);
                    h.assertTrue(Math.abs(high-(cell==RoundaboutBlock.Cell.HUB?RoundaboutBlock.POST/16.:deck))<1e-9,"the deck's height over it, and over the hub its post: "+high);
                    h.assertTrue(Math.abs(s.getShape(l,at).max(Direction.Axis.Y)-RoundaboutBlock.POST/16.)<1e-9,"a hand finds it up to its rails' height");
                }
                wheel[0]=(RoundaboutBlock.Wheel)l.getBlockEntity(hub);
                // The deck is round: where it is over the ground a body stands on it, and past its rim on the sand.
                java.util.function.BiFunction<Double,Double,Boolean> stood=(x,z)->{
                    var at=BlockPos.containing(b.getX()+x,b.getY()+.05,b.getZ()+z);var shape=l.getBlockState(at).getCollisionShape(l,at);
                    return !shape.isEmpty()&&shape.toAabbs().stream().anyMatch(box->box.move(at).contains(b.getX()+x,b.getY()+deck-.01,b.getZ()+z));
                };
                h.assertTrue(stood.apply(cx+1.3,cz)&&stood.apply(cx,cz-1.3)&&stood.apply(cx-.95,cz+.95)&&stood.apply(cx+.2,cz+.6),"the deck is stood on out to its rim, every way");
                h.assertTrue(!stood.apply(cx+1.4,cz+1.4)&&!stood.apply(cx-1.2,cz-1.2)&&!stood.apply(cx+1.49,cz),"and not past it: its ground's corners are open sand");
                h.assertTrue(wheel[0].speed()==0&&wheel[0].angle()==0,"it stands still");
                p[0]=f.player("round_one",P);p[1]=f.player("round_two",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenWaitUntil(()->h.assertTrue(p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1,"readers here are children"))
            .thenExecute(()->{
                // From on the deck, from too far off, and straight at its middle: no shove, and they are told why.
                f.at(p[0],P,cx+1,deck,cz);h.assertTrue(wheel[0].on(p[0])&&BikeNightTexts.ROUNDABOUT_ON.equals(wheel[0].push(p[0])),"not from on it");
                f.at(p[0],P,cx+4,0,cz);h.assertTrue(!wheel[0].on(p[0])&&BikeNightTexts.ROUNDABOUT_FAR.equals(wheel[0].push(p[0])),"not from too far off");
                f.at(p[0],P,cx+2.2,0,cz);p[0].setYRot(90);p[0].setXRot(0);
                h.assertTrue(BikeNightTexts.ROUNDABOUT.equals(wheel[0].push(p[0]))&&wheel[0].speed()==0,"a shove straight at its middle turns it neither way");
                // East of it, a hand on its rim to their left (the south-east of it) shoves its near side that way.
                hush(p[0]);hand(p[0],hub.offset(1,0,1));
                h.assertTrue(Math.abs(wheel[0].speed()-RoundaboutBlock.Wheel.PUSH)<1e-9,"a hand on its rim to their left shoves it round that way: "+wheel[0].speed());
                h.assertTrue(wheel[0].push(p[0])==null&&Math.abs(wheel[0].speed()-RoundaboutBlock.Wheel.PUSH)<1e-9,"one shove at a time: a second at once adds nothing");
                was[0]=wheel[0].angle();
            })
            .thenIdle(wait+1)
            .thenExecute(()->{
                h.assertTrue(wheel[0].angle()>was[0]&&wheel[0].speed()>0&&wheel[0].speed()<RoundaboutBlock.Wheel.PUSH,"it turns, and slows by itself: "+wheel[0].speed());
                // Facing the other way along its rim, a shove is the other way.
                p[0].setYRot(180);double before=wheel[0].speed();
                h.assertTrue(wheel[0].push(p[0])==null&&wheel[0].speed()<before-.02,"a shove the other way slows it");
            })
            .thenIdle(wait+1)
            .thenExecute(()->{
                wheel[0].set(0,top);p[0].setYRot(0);
                h.assertTrue(wheel[0].push(p[0])==null&&wheel[0].speed()<=top,"it goes no faster than its fastest");
                // A reader standing on the deck goes round with it, as their own game carries them (here, the case does);
                // and something left lying on the deck goes round too, which the server carries.
                wheel[0].set(0,.05);f.at(p[1],P,cx+1,deck,cz);p[1].setYRot(0);was[0]=0;
                var stick=new net.minecraft.world.entity.item.ItemEntity(l,b.getX()+cx-1,b.getY()+deck,b.getZ()+cz,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK));
                stick.setDeltaMovement(Vec3.ZERO);l.addFreshEntity(stick);thing[0]=stick;
            })
            .thenExecuteFor(40,()->{was[0]+=wheel[0].speed();h.assertTrue(wheel[0].carry(p[1],true),"on the deck");})
            .thenExecute(()->{
                double gone=Math.toDegrees(was[0]);
                h.assertTrue(gone>60&&Math.abs(Mth.wrapDegrees(round.apply(p[1])-gone))<.01&&Math.abs(out.apply(p[1])-1)<1e-6,"they have gone round with it, as far as it has turned, no nearer its middle and no further: "+round.apply(p[1])+" of "+gone);
                h.assertTrue(Math.abs(Mth.wrapDegrees(p[1].getYRot()-(float)gone))<.01,"and been turned with it");
                h.assertTrue(Math.abs(Mth.wrapDegrees(round.apply(thing[0])-180-gone))<8&&Math.abs(out.apply(thing[0])-1)<.1,"and so has what was left lying on it: "+round.apply(thing[0]));
                thing[0].discard();
                // Standing up on it while it goes fast they are pushed outward; keeping low, they hold on.
                wheel[0].set(0,.2);f.at(p[1],P,cx+1,deck,cz);
            })
            .thenIdle(1)
            .thenExecute(()->{
                wheel[0].carry(p[1],true);
                h.assertTrue(p[1].getDeltaMovement().horizontalDistance()<1e-9,"keeping low, they hold on");
                wheel[0].carry(p[1],false);var thrown=p[1].getDeltaMovement();
                h.assertTrue(thrown.horizontalDistance()>.01&&thrown.x*(p[1].getX()-(b.getX()+cx))+thrown.z*(p[1].getZ()-(b.getZ()+cz))>0,"standing up, they are pushed outward: "+thrown);
                wheel[0].set(0,.05);p[1].setDeltaMovement(Vec3.ZERO);wheel[0].carry(p[1],false);
                h.assertTrue(p[1].getDeltaMovement().horizontalDistance()<1e-9,"going slowly it throws nobody");
                // Their record: how far round they have gone on it; and they were told once how to stay on, when they first went round.
                wheel[0].set(0,.2);f.at(p[1],P,cx+1,deck,cz);p[1].setDeltaMovement(Vec3.ZERO);
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[1],BikeNightTexts.ROUNDABOUT_HINT)&&f.own(p[1],P).getDouble("RoundTurned")>0,"told once how to stay on, and their turning is in their record"))
            .thenWaitUntil(()->h.assertTrue(sounded(p[1],"park.roundabout_squeak"),"and its bearing is heard as it comes round"))
            // Out past its rim, standing, while it still goes fast: it threw them.
            .thenExecute(()->{wheel[0].set(0,.2);f.at(p[1],P,cx+2.2,0,cz);})
            .thenWaitUntil(()->h.assertTrue(f.own(p[1],P).getInt("RoundFlung")==1,"off it while it was going fast: it threw them"))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt("RoundPushed")==3,"and the first reader's three shoves are in their record: "+f.own(p[0],P).getInt("RoundPushed")))
            // Left alone it comes to a stop.
            .thenExecute(()->wheel[0].set(1,.003))
            .thenWaitUntil(()->h.assertTrue(wheel[0].speed()==0&&wheel[0].angle()>1,"left alone it runs down and stops"))
            .thenExecute(()->{
                String one=told("round_one",p[0],f),two=told("round_two",p[1],f);
                var rode=new net.minecraft.nbt.CompoundTag();rode.putDouble("RoundTurned",Math.PI*2*3+.1);
                var three=String.join(" ",BikeNightTexts.account("round_three",rode).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(one.contains("pushed the merry-go-round")&&two.contains("it threw me off")&&three.contains("round on the merry-go-round three times"),"and their accounts have it: "+one+" / "+two+" / "+three);
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="park_small",timeoutTicks=2400)
    public static void theFountainRunsThePayphoneWantsACoinAndOneBenchIsCarved(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};
        var fountain=b.offset(BikeNightSite.FOUNTAIN);var phone=b.offset(BikeNightSite.PHONE);var bench=b.offset(-5,0,-23);
        double fx=BikeNightSite.FOUNTAIN.getX()+.5,fz=BikeNightSite.FOUNTAIN.getZ()+.5;
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{
                var s=l.getBlockState(fountain);
                h.assertTrue(CalderBlock.is(s,CalderBlock.Kind.FOUNTAIN)&&s.getValue(CalderBlock.STAGE)==0&&l.getBlockState(fountain.below()).is(Blocks.GRASS_BLOCK),"a drinking fountain on the grass by the side path, not running: "+s);
                h.assertTrue(Math.abs(s.getCollisionShape(l,fountain).max(Direction.Axis.Y)-(CalderBlock.FOUNTAIN_HIGH+3)/16.)<1e-9&&(CalderBlock.FOUNTAIN_HIGH+3)/16.<.7,"its bowl is under a child's chin: "+s.getCollisionShape(l,fountain).max(Direction.Axis.Y));
                var ph=l.getBlockState(phone);
                h.assertTrue(CalderBlock.is(ph,CalderBlock.Kind.PHONE)&&ph.getValue(CalderBlock.STAGE)==0&&ph.getValue(CalderBlock.FACING)==Direction.NORTH&&l.getBlockState(phone.south()).is(BikeNightRegistry.material("brick")),"a payphone on the back wall, its handset on its hook: "+ph);
                var seat=l.getBlockState(bench);
                h.assertTrue(CalderBlock.is(seat,CalderBlock.Kind.BENCH)&&seat.getValue(CalderBlock.STAGE)==CalderBlock.CARVED,"the bench by the playground is the carved one: "+seat);
                p[0]=f.player("small_one",P);p[1]=f.player("small_two",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            // The fountain runs for whoever presses its button, and is running for everybody while it does.
            .thenExecute(()->{f.at(p[0],P,fx,0,fz+1.6);f.at(p[1],P,fx+1.6,0,fz);hush(p[0]);hush(p[1]);hand(p[0],fountain);})
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.FOUNTAIN)&&sounded(p[0],"park.fountain")&&sounded(p[1],"park.fountain"),"a hand on the fountain drinks from it, and it is heard by both"))
            .thenExecute(()->{
                h.assertTrue(l.getBlockState(fountain).getValue(CalderBlock.STAGE)==CalderBlock.RUNNING&&f.own(p[0],P).getBoolean("Drank"),"it runs, and they drank");
                hand(p[1],fountain);
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[1],BikeNightTexts.FOUNTAIN_RUNNING),"another hand while it runs is told it is running"))
            .thenExecute(()->h.assertTrue(!f.own(p[1],P).getBoolean("Drank"),"and has not drunk"))
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(fountain).getValue(CalderBlock.STAGE)==0,"it stops by itself"))
            .thenExecute(()->{hush(p[1]);hand(p[1],fountain);})
            .thenWaitUntil(()->h.assertTrue(heard(p[1],BikeNightTexts.FOUNTAIN)&&f.own(p[1],P).getBoolean("Drank"),"then the second reader drinks"))
            // The payphone gives a dial tone and wants a coin; its handset goes back on its hook by itself.
            .thenExecute(()->{f.at(p[0],P,BikeNightSite.PHONE.getX()+.5,0,BikeNightSite.PHONE.getZ()-1.2);hush(p[0]);hand(p[0],phone);})
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.PHONE)&&sounded(p[0],"park.phone_tone"),"a hand on the payphone lifts its handset: a dial tone, and no coin"))
            .thenExecute(()->{
                h.assertTrue(l.getBlockState(phone).getValue(CalderBlock.STAGE)==CalderBlock.LIFTED&&f.own(p[0],P).getBoolean("Phoned"),"its handset is off its hook");
                hand(p[0],phone);
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.PHONE_HELD),"a hand on it again: the tone goes on"))
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(phone).getValue(CalderBlock.STAGE)==0&&sounded(p[0],"park.phone_hook"),"it is hung up again, and that is heard"))
            // The carved bench says what is cut into it.
            .thenExecute(()->{f.at(p[0],P,-3.5,0,-22.5);hush(p[0]);hand(p[0],bench);})
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.CARVED),"a hand on the carved bench reads the heart and the initials"))
            .thenExecute(()->{
                String one=told("small_one",p[0],f),two=told("small_two",p[1],f);
                h.assertTrue(one.contains("drank from the fountain, and lifted the payphone")&&two.contains("The water tasted of the pipe")&&!two.contains("payphone"),"and their accounts have what each did: "+one+" / "+two);
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="park_dome",timeoutTicks=2400)
    public static void theDomeIsClimbedOnEveryBarStoodOnAtItsTopAndGotIntoKeepingLow(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};
        var mid=b.offset(BikeNightSite.DOME);double cx=BikeNightSite.DOME.getX()+.5,cz=BikeNightSite.DOME.getZ()+.5,top=26/16.;
        // How high over the sand the dome stops a body, over a spot; less than nothing where it does not.
        java.util.function.BiFunction<Double,Double,Double> stops=(x,z)->{
            double high=-1,wx=b.getX()+x,wz=b.getZ()+z;
            for(int y=0;y<DomeBlock.HIGH;y++){
                var at=BlockPos.containing(wx,b.getY()+y+.5,wz);var s=l.getBlockState(at);if(!DomeBlock.is(s))continue;
                for(var box:s.getCollisionShape(l,at).toAabbs()){var m=box.move(at);if(m.minX<=wx&&wx<m.maxX&&m.minZ<=wz&&wz<m.maxZ)high=Math.max(high,m.maxY-b.getY());}
            }
            return high;
        };
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{
                int cells=0,kept=0,edging=0;double lowest=9,highest=0;
                for(int y=0;y<DomeBlock.HIGH;y++)for(int x=0;x<DomeBlock.WIDE;x++)for(int z=0;z<DomeBlock.WIDE;z++){
                    var at=mid.offset(x-DomeBlock.MID,y,z-DomeBlock.MID);var s=l.getBlockState(at);
                    // Its ground's outside row runs out over the sandpit's edging to the west and the south: the edging stays.
                    if(y==0&&(x==0||z==DomeBlock.WIDE-1)&&CalderBlock.is(s,CalderBlock.Kind.BORDER)){edging++;continue;}
                    h.assertTrue(DomeBlock.is(s)&&s.getValue(DomeBlock.X)==x&&s.getValue(DomeBlock.Y)==y&&s.getValue(DomeBlock.Z)==z&&DomeBlock.middle(at,s).equals(mid),"the dome's ground at "+x+","+y+","+z+": "+s);
                    cells++;if(l.getBlockEntity(at)!=null)kept++;
                    if(y==0)h.assertTrue(l.getBlockState(at.below()).is(Blocks.SAND),"it stands on the sand");
                    for(var box:s.getCollisionShape(l,at).toAabbs()){lowest=Math.min(lowest,box.minY+y);highest=Math.max(highest,box.maxY+y);}
                }
                h.assertTrue(cells==41&&edging==9&&kept==1&&l.getBlockEntity(mid) instanceof DomeBlock.Bars,"forty-one cells and the nine timbers of the edging they stop at, and only the middle cell on the sand keeps what draws it: "+cells+", "+edging);
                for(int x=1;x<4;x++)for(int z=1;z<4;z++)for(int y=0;y<DomeBlock.HIGH;y++)h.assertTrue(DomeBlock.is(l.getBlockState(mid.offset(x-DomeBlock.MID,y,z-DomeBlock.MID))),"every cell its bars are in is there");
                // Its bars: eight feet, two rings of eight and a top, every joint on the one ball; fifty-six bars, each about a child's reach long.
                h.assertTrue(DomeBlock.JOINTS.size()==25&&DomeBlock.BARS.size()==56,"twenty-five joints and fifty-six bars");
                var heart=new Vec3(0,DomeBlock.HEART,0);
                for(var joint:DomeBlock.JOINTS)h.assertTrue(Math.abs(joint.distanceTo(heart)-DomeBlock.BALL)<1e-9&&joint.y>-1e-9,"every joint is on the one ball, over the sand: "+joint);
                for(var bar:DomeBlock.BARS){double length=DomeBlock.JOINTS.get(bar[0]).distanceTo(DomeBlock.JOINTS.get(bar[1]));h.assertTrue(length>.5&&length<1.2,"a bar about a child's reach long: "+length);}
                var used=new java.util.HashSet<Integer>();for(var bar:DomeBlock.BARS){used.add(bar[0]);used.add(bar[1]);}
                h.assertTrue(used.size()==25,"and a bar on every joint");
                // What stops a body: nothing under its first ring; from there the skin, up to the top.
                h.assertTrue(Math.abs(lowest-14/16.)<1e-9&&Math.abs(highest-top)<1e-9,"it stops a body from its first ring up to its top: "+lowest+" to "+highest);
                h.assertTrue(Math.abs(stops.apply(cx,cz)-top)<1e-9&&stops.apply(cx+1.35,cz)<0&&stops.apply(cx+2,cz)<0&&stops.apply(cx+1,cz+1)<0,"its top over its middle; nothing past its first ring");
                // From the first ring to the top the skin is walked up: every way, each step in is no lower and no more than three pixels higher.
                for(double round=0;round<Math.PI*2;round+=Math.PI/8){
                    double before=-1;
                    for(double out=1.2;out>=0;out-=.125){
                        double here=stops.apply(cx+out*Math.cos(round),cz+out*Math.sin(round));
                        h.assertTrue(here>0&&here>=before-1e-9&&(before<0||here-before<=3/16.+1e-9),"the skin is walked up from its first ring to its top, "+Math.toDegrees(round)+" round, "+out+" out: "+before+" then "+here);
                        before=here;
                    }
                }
                p[0]=f.player("dome_one",P);p[1]=f.player("dome_two",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenWaitUntil(()->h.assertTrue(p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1,"readers here are children"))
            .thenExecute(()->{
                double half=p[0].getBbWidth()/2,tall=p[0].getBbHeight(),low=tall*1.5/1.8;
                // Whether a body that tall has room on the sand, that far east of the dome's middle.
                java.util.function.BiFunction<Double,Double,Boolean> fits=(out,high)->l.noCollision(new AABB(b.getX()+cx+out-half,b.getY()+.001,b.getZ()+cz-half,b.getX()+cx+out+half,b.getY()+high,b.getZ()+cz+half));
                f.at(p[0],P,cx+4,0,cz+3);f.at(p[1],P,cx+4,0,cz-3);
                h.assertTrue(fits.apply(0.,tall)&&fits.apply(.5,tall),"inside it there is room for a child to stand");
                h.assertTrue(!fits.apply(1.15,tall)&&fits.apply(1.15,low)&&fits.apply(1.6,tall),"a child standing does not get in under its first ring; one keeping low does: "+tall+", "+low);
                // Every bar is a rung: a body at the skin is on a ladder, inside it or out; in the middle of it, or away from it, it is not.
                f.at(p[0],P,cx+1.45,0,cz);h.assertTrue(p[0].onClimbable(),"at its foot, outside it");
                f.at(p[0],P,cx+1.3,.6,cz);h.assertTrue(p[0].onClimbable(),"part of the way up the outside of it");
                f.at(p[0],P,cx,0,cz-1);h.assertTrue(p[0].onClimbable(),"inside it, against its bars");
                f.at(p[0],P,cx,0,cz);h.assertTrue(!p[0].onClimbable(),"standing in the middle of it is not climbing it");
                f.at(p[0],P,cx-2.3,0,cz);h.assertTrue(!p[0].onClimbable(),"nor is standing off from it");
                // A hand on it says what it is, and how it is climbed and got into.
                hush(p[1]);f.at(p[1],P,cx+2.2,0,cz);hand(p[1],mid.above());
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[1],BikeNightTexts.DOME),"a hand on the dome says what it is"))
            // Standing on its top a reader is told, once, what is to be seen from there.
            .thenExecute(()->{hush(p[0]);f.at(p[0],P,cx,top,cz);h.assertTrue(DomeBlock.onTop(mid,p[0])&&!DomeBlock.onTop(mid,p[1]),"one of them is on its top");})
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.DOME_TOP)&&f.own(p[0],P).getBoolean("DomeTop"),"told what is to be seen from the top"))
            .thenExecute(()->{h.assertTrue(!f.own(p[1],P).getBoolean("DomeTop"),"which is theirs alone");hush(p[0]);f.at(p[0],P,cx+4,0,cz+3);})
            .thenIdle(12)
            .thenExecute(()->f.at(p[0],P,cx,top,cz))
            .thenIdle(12)
            .thenExecute(()->{
                h.assertTrue(!heard(p[0],BikeNightTexts.DOME_TOP),"and told once only");
                // There is room on it for two.
                double out=.4;f.at(p[1],P,cx+out,stops.apply(cx+out,cz),cz);
                h.assertTrue(DomeBlock.onTop(mid,p[1])&&DomeBlock.onTop(mid,p[0]),"two of them on its top");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[1],BikeNightTexts.DOME_TOP)&&f.own(p[1],P).getBoolean("DomeTop"),"and the second is told as well"))
            .thenExecute(()->{
                String one=told("dome_one",p[0],f);
                h.assertTrue(one.contains("top of the climbing dome"),"and their account has it: "+one);
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="park_spring",timeoutTicks=3000)
    public static void theSpringRiderLeansWithItsRiderAndGoesFurtherWhenItIsRocked(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final SpringBlock.Rocker[] r={null};final double[] far={0,0};
        var foot=b.offset(BikeNightSite.SPRING);var facing=BikeNightSite.SPRING_FACING;double sx=BikeNightSite.SPRING.getX()+.5,sz=BikeNightSite.SPRING.getZ()+.5;
        double held=SpringBlock.Rocker.LEAN/SpringBlock.Rocker.SPRING;
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->{
                var s=l.getBlockState(foot);
                h.assertTrue(SpringBlock.is(s,SpringBlock.Part.FOOT)&&s.getValue(SpringBlock.FACING)==facing&&l.getBlockState(foot.below()).is(Blocks.SAND)&&l.getBlockEntity(foot) instanceof SpringBlock.Rocker,"the spring rider's foot, bolted down on the sand: "+s);
                r[0]=(SpringBlock.Rocker)l.getBlockEntity(foot);
                h.assertTrue(r[0].angle()==0&&r[0].speed()==0&&!r[0].ridden(),"it stands upright and still");
                h.assertTrue(held<SpringBlock.Rocker.ROCKED&&SpringBlock.Rocker.ROCKED<SpringBlock.MAX,"a lean held is short of rocking, and rocking is short of its stops");
                p[0]=f.player("spring_one",P);p[1]=f.player("spring_two",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenWaitUntil(()->h.assertTrue(p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1,"readers here are children"))
            .thenExecute(()->{f.at(p[0],P,sx,0,sz+1.5);f.at(p[1],P,sx,0,sz-1.5);hush(p[0]);hush(p[1]);hand(p[0],foot);})
            .thenWaitUntil(()->h.assertTrue(p[0].getVehicle() instanceof SpringSeat&&heard(p[0],BikeNightTexts.SPRING_HINT)&&f.own(p[0],P).getBoolean("SpringSat"),"a hand on the horse sits on it, and they are told once how it is rocked"))
            .thenExecute(()->{
                h.assertTrue(p[0].getVehicle().position().distanceTo(SpringBlock.saddle(foot,facing,0))<1e-6&&r[0].ridden(),"they sit on its saddle");
                hand(p[1],foot);
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[1],BikeNightTexts.SPRING_TAKEN)&&!p[1].isPassenger(),"there is room on it for one"))
            // A lean held (the strafe keys, right for the way the horse looks) holds it a little over, short of rocking.
            .thenExecute(()->p[0].setPlayerInput(-1,0,false,false))
            .thenExecuteFor(200,()->far[0]=Math.max(far[0],Math.abs(r[0].angle())))
            .thenExecute(()->{
                h.assertTrue(Math.abs(r[0].angle()-held)<.02&&far[0]<SpringBlock.Rocker.ROCKED,"a lean held holds it a little over the way it looks, and never as far as rocking: "+r[0].angle()+", at most "+far[0]);
                var seat=p[0].getVehicle();
                h.assertTrue(seat.position().distanceTo(SpringBlock.saddle(foot,facing,r[0].angle()))<1e-6&&seat.getX()>b.getX()+sx+.1&&p[0].position().distanceTo(seat.position())<.5,"and its rider goes over with it");
                h.assertTrue(f.own(p[0],P).getInt("Sprung")==0,"which is not rocking");
            })
            // Leaning with it, back and forth, takes it out to its stops.
            .thenExecuteFor(200,()->{p[0].setPlayerInput(r[0].speed()>=0?-1:1,0,false,false);far[1]=Math.max(far[1],Math.abs(r[0].angle()));})
            .thenExecute(()->h.assertTrue(far[1]>=SpringBlock.MAX-1e-9,"leaning with it, back and forth, takes it out to its stops: "+far[1]))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt("Sprung")>2&&sounded(p[0],"park.spring")&&sounded(p[1],"park.spring"),"that is rocking, in their record, and the spring is heard by both"))
            // The walk keys lean it as well; and with no lean it runs down.
            .thenExecute(()->{r[0].set(0,0);p[0].setPlayerInput(0,-1,false,false);})
            .thenIdle(200)
            .thenExecute(()->{h.assertTrue(Math.abs(r[0].angle()+held)<.02,"walking back leans it back: "+r[0].angle());p[0].setPlayerInput(0,0,false,false);})
            .thenIdle(200)
            .thenExecute(()->{h.assertTrue(Math.abs(r[0].angle())<.02&&r[0].ridden(),"with no lean it comes upright under them: "+r[0].angle());getOff(p[0]);})
            // Sneak gets off, beside it; and left alone it stands still, for the next.
            .thenWaitUntil(()->h.assertTrue(!p[0].isPassenger(),"sneak gets off"))
            .thenExecute(()->{
                stand(p[0]);var side=facing.getClockWise();
                h.assertTrue(Math.abs(p[0].getX()-(b.getX()+sx+side.getStepX()*SpringSeat.ASIDE))<.01&&Math.abs(p[0].getZ()-(b.getZ()+sz+side.getStepZ()*SpringSeat.ASIDE))<.01&&Math.abs(p[0].getY()-b.getY())<.01,"onto the sand beside it: "+p[0].position());
            })
            .thenWaitUntil(()->h.assertTrue(r[0].angle()==0&&r[0].speed()==0&&!r[0].ridden()&&r[0].seat()==null,"left alone it stands upright and still, with nobody on it"))
            .thenExecute(()->{hush(p[1]);hand(p[1],foot);})
            .thenWaitUntil(()->h.assertTrue(p[1].getVehicle() instanceof SpringSeat&&heard(p[1],BikeNightTexts.SPRING_HINT),"and the second reader sits on it"))
            .thenExecute(()->{
                String one=told("spring_one",p[0],f),two=told("spring_two",p[1],f);
                h.assertTrue(one.contains("rocked the horse on its spring")&&two.contains("sat on the horse on its spring")&&!two.contains("rocked"),"and their accounts have it: "+one+" / "+two);
                getOff(p[1]);
            })
            .thenWaitUntil(()->h.assertTrue(!p[1].isPassenger(),"and gets off"))
            .thenExecute(()->stand(p[1])).thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.481: the town
    @AfterBatch(batch="town_built") public static void townBuiltDone(ServerLevel l){done();}
    @AfterBatch(batch="town_ways") public static void townWaysDone(ServerLevel l){done();}
    @AfterBatch(batch="town_ride") public static void townRideDone(ServerLevel l){done();}
    @AfterBatch(batch="town_clock") public static void townClockDone(ServerLevel l){done();}
    private static boolean mine(net.minecraft.world.level.block.state.BlockState s){return BuiltInRegistries.BLOCK.getKey(s.getBlock()).getNamespace().equals(TheOldestHouse.MOD_ID);}
    private static boolean sign(ServerLevel l,BlockPos b,int x,int y,int z,CalderSigns.Sign sign){return CalderSignBlock.is(l.getBlockState(b.offset(x,y,z)),sign);}
    /** The kinds of the town's fittings that stand in a part of it. */
    private static java.util.Set<CalderTownBlock.Kind> kinds(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1){
        var out=java.util.EnumSet.noneOf(CalderTownBlock.Kind.class);
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)for(int y=0;y<=4;y++){var s=l.getBlockState(b.offset(x,y,z));if(s.is(BikeNightRegistry.TOWN.get()))out.add(s.getValue(CalderTownBlock.KIND));}
        return out;
    }
    /** Whether there is a lamp standing anywhere in a part of the town. */
    private static boolean lamp(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1){
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)if(CalderBlock.is(l.getBlockState(b.offset(x,0,z)),CalderBlock.Kind.LAMP))return true;
        return false;
    }

    @GameTest(template="empty",batch="town_built",timeoutTicks=2400)
    public static void theTownStandsNorthOfTheParkOnStreetsThatAreNotAGridBuiltOfItsOwnThings(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,town(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the town is resident"))
            .thenExecute(()->{
                var asphalt=BikeNightRegistry.material("asphalt");var kerb=BikeNightRegistry.material("kerb");var paving=BikeNightRegistry.material("paving");var line=BikeNightRegistry.ROAD_LINE.get();
                int road=CalderTown.ROAD;
                // Every place a street takes is laid as the street has it.
                int wrong=0;String first="";
                for(int x=CalderTown.W;x<=CalderTown.E;x++)for(int z=CalderTown.N;z<=CalderTown.S;z++){
                    int k=CalderTown.surface(x,z);if(k==CalderTown.NONE)continue;var s=l.getBlockState(b.offset(x,-1,z));
                    boolean ok=k==road?s.is(asphalt)||s.is(line):k==CalderTown.KERB?s.is(kerb):s.is(paving);
                    if(!ok&&wrong++==0)first=x+","+z+" is "+s;
                }
                h.assertTrue(wrong==0,"road, kerb and pavement lie where the streets have them: "+wrong+" places do not, the first "+first);
                // The owner: "less even streets". They are not a grid: they differ in width, one slants, one goes round, two bend, one ends in a circle.
                int main=0,elm=0,park=0;
                for(int dz=-8;dz<=8;dz++){if(CalderTown.surface(20,-92+dz)==road)main++;if(CalderTown.surface(30,-121+dz)==road)elm++;if(CalderTown.surface(20,BikeNightSite.LINE+dz)==road)park++;}
                h.assertTrue(main==9&&park==7&&elm==5,"Main Street is the wide one, Park Street between, Elm narrow: "+main+", "+park+", "+elm);
                h.assertTrue(CalderTown.surface(-40,-129)==road&&CalderTown.surface(30,-121)==road&&CalderTown.surface(30,-129)!=road&&CalderTown.surface(-40,-121)!=road,"Elm Street slants: it is eight rows further south at its east end");
                h.assertTrue(CalderTown.surface(-40,-165)==road&&CalderTown.surface(0,-171)==road&&CalderTown.surface(0,-165)!=road&&CalderTown.surface(40,-165)==road,"Church Lane goes round the foot of the hill, nearest the cemetery in its middle");
                h.assertTrue(CalderTown.surface(50,-70)==road&&CalderTown.surface(46,-130)==road&&CalderTown.surface(50,-130)!=road&&CalderTown.surface(-50,-70)==road&&CalderTown.surface(-46,-140)==road&&CalderTown.surface(-50,-140)!=road,"both avenues bend");
                h.assertTrue(CalderTown.surface(0,-70)==road&&CalderTown.surface(2,-110)==road&&CalderTown.surface(-3,-110)!=road&&CalderTown.surface(2,-128)!=road,"Mill Street steps aside at Main, and ends on Elm");
                h.assertTrue(CalderTown.surface(22,-141)==road&&CalderTown.surface(18,-141)==road&&CalderTown.surface(26,-141)==road&&CalderTown.surface(22,-145)==road&&CalderTown.surface(22,-146)==CalderTown.KERB&&CalderTown.surface(22,-148)==CalderTown.PAVEMENT
                    &&CalderTown.surface(22,-149)==CalderTown.NONE,"Elm Court ends in a turning circle, kerb and pavement round it");
                var mid=l.getBlockState(b.offset(20,-1,-92));var up=l.getBlockState(b.offset(0,-1,-70));
                h.assertTrue(mid.is(line)&&mid.getValue(RotatedPillarBlock.AXIS)==Direction.Axis.X&&up.is(line)&&up.getValue(RotatedPillarBlock.AXIS)==Direction.Axis.Z&&l.getBlockState(b.offset(0,-1,-92)).is(asphalt)&&l.getBlockState(b.offset(22,-1,-130)).is(asphalt),
                    "a line down the middle of a street, along it; none across a crossing, and none in the court");
                // The fronts and names are up.
                record Hung(CalderSigns.Sign sign,int x,int y,int z){}
                for(var s:List.of(new Hung(CalderSigns.Sign.WASH_NAME,10,3,-99),new Hung(CalderSigns.Sign.WASH_FRONT,10,0,-99),new Hung(CalderSigns.Sign.VIDEO_NAME,22,2,-99),new Hung(CalderSigns.Sign.VIDEO_FRONT,23,0,-99),
                        new Hung(CalderSigns.Sign.BARBER_NAME,33,3,-99),new Hung(CalderSigns.Sign.BARBER_FRONT,34,0,-99),new Hung(CalderSigns.Sign.HARDWARE_NAME,-42,3,-85),new Hung(CalderSigns.Sign.HARDWARE_FRONT,-42,0,-85),
                        new Hung(CalderSigns.Sign.BANK_NAME,-31,2,-85),new Hung(CalderSigns.Sign.BANK_FRONT,-30,0,-85),new Hung(CalderSigns.Sign.THEATER_FRONT,-17,0,-85),new Hung(CalderSigns.Sign.BIGTOP_FRONT,22,0,-66),
                        new Hung(CalderSigns.Sign.BIGTOP_NAME,22,2,-66),new Hung(CalderSigns.Sign.BIGTOP_NAME,22,2,-77),new Hung(CalderSigns.Sign.FOODFAIR_NAME,-29,3,-112),new Hung(CalderSigns.Sign.FOOD_DOOR,-27,0,-112),
                        new Hung(CalderSigns.Sign.FOOD_A,-37,0,-112),new Hung(CalderSigns.Sign.FOOD_B,-32,0,-112),new Hung(CalderSigns.Sign.FOOD_C,-22,0,-112),
                        new Hung(CalderSigns.Sign.SCHOOL_FRONT,-38,0,-138),new Hung(CalderSigns.Sign.TROPHY,-31,0,-138),new Hung(CalderSigns.Sign.JRHIGH_NAME,-38,3,-138),new Hung(CalderSigns.Sign.CEMETERY,-3,3,CalderTown.RAILS),
                        new Hung(CalderSigns.Sign.DRIVE_THRU,33,3,-66),new Hung(CalderSigns.Sign.DRIVE_THRU,33,3,-77),new Hung(CalderSigns.Sign.DELIVERIES,14,3,-110),new Hung(CalderSigns.Sign.SHUTTER,15,1,-110),
                        new Hung(CalderSigns.Sign.TAG_RULES,-19,1,-71),new Hung(CalderSigns.Sign.TAG_HERE,-22,1,-81),new Hung(CalderSigns.Sign.FOR_SALE,-18,2,-60),new Hung(CalderSigns.Sign.BEWARE,-30,1,-65),
                        new Hung(CalderSigns.Sign.POSTER_A,-6,1,CalderTown.POSTERS[0]),new Hung(CalderSigns.Sign.POSTER_B,-6,2,CalderTown.POSTERS[1]+1),new Hung(CalderSigns.Sign.POSTER_C,-6,1,CalderTown.POSTERS[2]),
                        new Hung(CalderSigns.Sign.SCOREBOARD,CalderTown.FIELD_W,2,CalderTown.SCORE_LO+2)))
                    h.assertTrue(sign(l,b,s.x,s.y,s.z,s.sign),"the "+s.sign+" is up at "+s.x+","+s.y+","+s.z+": "+l.getBlockState(b.offset(s.x,s.y,s.z)));
                h.assertTrue(CalderTown.feature(l,b)==0&&java.util.Arrays.equals(CalderTown.clockShows(l,b),new int[]{1,1,CalderDigitBlock.COLON,4,1}),"the first double feature on the marquee; the bank's clock at 11:41");
                // One look at everything standing in the town: nothing vanilla is built there; what hangs on a wall has a wall behind it; a dumpster has both its halves.
                int stray=0,hanging=0,halves=0,doors=0,lit=0;String strays="",hangs="";
                for(int x=CalderTown.W;x<=CalderTown.E;x++)for(int z=CalderTown.N;z<=CalderTown.S;z++)for(int y=-1;y<=CalderTown.TOP;y++){
                    var at=b.offset(x,y,z);var s=l.getBlockState(at);if(s.isAir())continue;
                    if(!mine(s)){
                        boolean natural=y==-1&&(s.is(Blocks.GRASS_BLOCK)||s.is(Blocks.COARSE_DIRT))||s.is(Blocks.OAK_LOG);
                        if(!natural&&stray++<4)strays+=" "+s.getBlock()+" at "+x+","+y+","+z;
                        continue;
                    }
                    Direction wall=null;
                    if(CalderSignBlock.is(s)){
                        var mount=CalderSignBlock.sign(s).mount;if(mount==BorrascaSigns.Mount.WALL||mount==BorrascaSigns.Mount.DECAL)wall=s.getValue(CalderSignBlock.FACING).getOpposite();
                        if(CalderSignBlock.sign(s)==CalderSigns.Sign.HOUSE_DOOR)doors++;
                    }else if(s.is(BikeNightRegistry.TOWN.get())){
                        var kind=s.getValue(CalderTownBlock.KIND);var facing=s.getValue(CalderTownBlock.FACING);int stage=s.getValue(CalderTownBlock.STAGE);
                        if(kind==CalderTownBlock.Kind.PIPE||kind==CalderTownBlock.Kind.UNIT||kind==CalderTownBlock.Kind.AWNING||kind==CalderTownBlock.Kind.POLE)wall=facing.getOpposite();
                        if(kind==CalderTownBlock.Kind.DUMPSTER){
                            var other=l.getBlockState(at.relative(stage%2==0?facing.getClockWise():facing.getCounterClockWise()));
                            if(CalderTownBlock.is(other,CalderTownBlock.Kind.DUMPSTER)&&other.getValue(CalderTownBlock.FACING)==facing&&other.getValue(CalderTownBlock.STAGE)==(stage^1))halves++;
                            else if(hanging++<4)hangs+=" half a dumpster at "+x+","+y+","+z;
                        }
                    }else if(CalderBlock.is(s,CalderBlock.Kind.LAMP)&&s.getValue(CalderBlock.STAGE)==CalderBlock.LIT)lit++;
                    if(wall!=null&&!l.getBlockState(at.relative(wall)).isCollisionShapeFullBlock(l,at.relative(wall))&&hanging++<4)hangs+=" "+s.getBlock()+" at "+x+","+y+","+z+" on nothing";
                }
                h.assertTrue(stray==0,"nothing vanilla is built in the town but grass, worn earth and the trees' trunks: "+stray+" such,"+strays);
                h.assertTrue(hanging==0,"what hangs on a wall has a wall behind it, and a dumpster is whole: "+hanging+" are not,"+hangs);
                h.assertTrue(halves==10&&doors==44&&lit>=30,"five dumpsters, twenty-two houses' front doors, and the lamps lit: "+halves/2+", "+doors/2+", "+lit);
                // The owner: "space between buildings". A body gets through each gap between two shops, from the street to their alley.
                int[][] gaps={{-34,-33,-84,-77},{-23,-22,-84,-75},{19,20,-108,-100},{30,31,-107,-100}};
                for(int[] gap:gaps)for(int z=gap[2];z<=gap[3];z++){
                    boolean open=false;
                    for(int x=gap[0];x<=gap[1];x++){var foot=b.offset(x,0,z);open|=l.getBlockState(foot).getCollisionShape(l,foot).isEmpty()&&l.getBlockState(foot.above()).getCollisionShape(l,foot.above()).isEmpty();}
                    h.assertTrue(open,"the gap at x="+gap[0]+" is open at z="+z);
                }
                // The owner: "clutter and decor in alleys ... dumpsters, trash". Each alley has its own.
                var all=java.util.EnumSet.of(CalderTownBlock.Kind.DUMPSTER,CalderTownBlock.Kind.BAGS,CalderTownBlock.Kind.CRATE,CalderTownBlock.Kind.BOXES,CalderTownBlock.Kind.DRUM,CalderTownBlock.Kind.TYRES,
                    CalderTownBlock.Kind.TRASH,CalderTownBlock.Kind.PIPE,CalderTownBlock.Kind.UNIT);
                var south=kinds(l,b,-43,-84,-7,-70);var north=kinds(l,b,8,-114,40,-100);var yard=kinds(l,b,-11,-123,-4,-113);var back=kinds(l,b,19,-82,31,-77);
                h.assertTrue(south.containsAll(all)&&north.containsAll(all),"behind the shops on each side of Main Street: a dumpster, bags, crates, boxes, drums, tyres, trash, a drainpipe, and what is fixed to a wall: "+south+" / "+north);
                h.assertTrue(yard.containsAll(java.util.EnumSet.of(CalderTownBlock.Kind.DUMPSTER,CalderTownBlock.Kind.BAGS,CalderTownBlock.Kind.CRATE,CalderTownBlock.Kind.BOXES,CalderTownBlock.Kind.TRASH,CalderTownBlock.Kind.UNIT))
                    &&back.containsAll(java.util.EnumSet.of(CalderTownBlock.Kind.DUMPSTER,CalderTownBlock.Kind.BAGS,CalderTownBlock.Kind.CRATE,CalderTownBlock.Kind.DRUM,CalderTownBlock.Kind.TRASH,CalderTownBlock.Kind.UNIT)),
                    "and in the grocery's yard and behind the burger place: "+yard+" / "+back);
                // Main Street is lit. The alleys, the cut and the field are not.
                h.assertTrue(lamp(l,b,-50,-99,50,-85),"lamps along Main Street");
                h.assertTrue(!lamp(l,b,-43,-84,-7,-70)&&!lamp(l,b,8,-115,40,-100)&&!lamp(l,b,CalderTown.CUT_W-1,CalderTown.CUT_N,CalderTown.CUT_E+1,CalderTown.CUT_S)&&!lamp(l,b,CalderTown.FIELD_W,CalderTown.FIELD_N-1,CalderTown.FIELD_E,CalderTown.FIELD_S+1),
                    "none in either alley, in the cut or on the school's field");
                // The school's fence and its holes; the bleachers stand clear of the ground.
                for(var hole:new BlockPos[]{CalderTown.HOLE_SOUTH,CalderTown.HOLE_NORTH}){
                    var low=l.getBlockState(b.offset(hole));var high=l.getBlockState(b.offset(hole).above());
                    h.assertTrue(CalderTownBlock.is(low,CalderTownBlock.Kind.CHAINLINK)&&low.getValue(CalderTownBlock.STAGE)==CalderTownBlock.TORN&&high.getValue(CalderTownBlock.STAGE)==CalderTownBlock.TORN_UPPER
                        &&low.getCollisionShape(l,b.offset(hole)).isEmpty()&&high.getCollisionShape(l,b.offset(hole).above()).isEmpty(),"the wire is cut and pulled back at "+hole);
                    var beside=l.getBlockState(b.offset(hole).east());
                    h.assertTrue(CalderTownBlock.is(beside,CalderTownBlock.Kind.CHAINLINK)&&beside.getValue(CalderTownBlock.STAGE)==CalderTownBlock.LOWER,"and whole beside it");
                }
                h.assertTrue(CalderTown.HOLE_SOUTH.getX()==CalderTown.HOLE_NORTH.getX(),"one hole is due north of the other");
                // The field is a football field: yard lines chalked across it, and the middle one is the way from hole to hole.
                var chalk=BikeNightRegistry.material("turf_line");var turf=BikeNightRegistry.material("turf");int yards=0;
                for(int x=CalderTown.FIELD_W;x<=CalderTown.FIELD_E;x++){
                    boolean chalked=l.getBlockState(b.offset(x,-1,CalderTown.YARD_N)).is(chalk);if(chalked)yards++;
                    for(int z=CalderTown.YARD_N;z<=CalderTown.YARD_S;z++)h.assertTrue(l.getBlockState(b.offset(x,-1,z)).is(chalked?chalk:turf),"the field at "+x+","+z+" is "+(chalked?"a yard line":"turf")+" from end to end");
                }
                h.assertTrue(yards==5&&l.getBlockState(b.offset(CalderTown.HOLE_SOUTH.getX(),-1,-145)).is(chalk)&&l.getBlockState(b.offset(CalderTown.HOLE_SOUTH.getX(),-1,CalderTown.FIELD_S)).is(turf),
                    "five yard lines, the middle one on the line between the holes, and none of them out to the fence: "+yards);
                // The way to the cemetery is named where the cut leaves Elm Street, on a blade lying along the cut, beside its mouth and not in it.
                int blade=0,inWay=0;
                for(int x=CalderTown.CUT_W-6;x<=CalderTown.CUT_E+6;x++)for(int z=CalderTown.CUT_S-1;z<=CalderTown.CUT_S+8;z++){
                    var s=l.getBlockState(b.offset(x,3,z));
                    if(CalderSignBlock.is(s,CalderSigns.Sign.TO_CEMETERY)){blade++;if(s.getValue(CalderSignBlock.FACING).getAxis()!=Direction.Axis.X)inWay++;}
                    if(x>=CalderTown.CUT_W&&x<=CalderTown.CUT_E&&!l.getBlockState(b.offset(x,0,z)).getCollisionShape(l,b.offset(x,0,z)).isEmpty())inWay++;
                }
                h.assertTrue(blade==3&&inWay==0,"CEMETERY, on a blade along the cut at its mouth, and nothing standing in the mouth: "+blade+" blocks of it, "+inWay+" things wrong");
                for(int k=0;k<CalderTown.STAND_ROWS;k++)h.assertTrue(CalderTownBlock.is(l.getBlockState(b.offset(CalderTown.HOLE_NORTH.getX(),k+1,CalderTown.STAND_FRONT-k)),CalderTownBlock.Kind.BLEACHER)
                    &&l.getBlockState(b.offset(CalderTown.HOLE_NORTH.getX(),0,CalderTown.STAND_FRONT-k)).isAir()&&l.getBlockState(b.offset(CalderTown.HOLE_NORTH.getX(),k,CalderTown.STAND_FRONT-k)).isAir(),"row "+k+" of the bleachers is a block clear of the ground on the way between the holes");
                // The dock, the boards up onto it, and the lot's ramp.
                for(int z=CalderTown.DOCK_N;z<=CalderTown.DOCK_S;z++){
                    var ramp=l.getBlockState(b.offset(CalderTown.DOCK_RAMP,0,z));
                    h.assertTrue(ramp.getBlock() instanceof BikeRampBlock&&ramp.getValue(BikeRampBlock.FACING)==Direction.EAST&&ramp.getValue(BikeRampBlock.PART)==BikeRampBlock.Part.STEEP&&l.getBlockState(b.offset(CalderTown.DOCK_W,0,z)).is(BikeNightRegistry.material("concrete"))
                        &&l.getBlockState(b.offset(CalderTown.DOCK_E,0,z)).is(BikeNightRegistry.material("concrete"))&&l.getBlockState(b.offset(CalderTown.DOCK_E+1,0,z)).getCollisionShape(l,b.offset(CalderTown.DOCK_E+1,0,z)).isEmpty(),"the dock: boards up at its west end, concrete along it, a drop off its east end, at z="+z);
                }
                var low=l.getBlockState(b.offset(CalderTown.LOT_RAMP));var high=l.getBlockState(b.offset(CalderTown.LOT_RAMP).west());
                h.assertTrue(low.getBlock() instanceof BikeRampBlock&&low.getValue(BikeRampBlock.PART)==BikeRampBlock.Part.LOW&&high.getValue(BikeRampBlock.PART)==BikeRampBlock.Part.HIGH&&low.getValue(BikeRampBlock.FACING)==Direction.WEST
                    &&l.getBlockState(b.offset(CalderTown.LOT_RAMP).below()).is(Blocks.COARSE_DIRT)&&l.getBlockState(b.offset(CalderTown.LOT_GAP)).getValue(CalderTownBlock.STAGE)==CalderTownBlock.TORN,"the empty lot's ramp on its trail, and the boards gone from the fence at the trail's end");
                // The cemetery's railings from one side of the ground to the other, and its gate shut.
                for(int x=CalderTown.W;x<=CalderTown.E;x++){var at=b.offset(x,0,CalderTown.RAILS);h.assertTrue(!l.getBlockState(at).getCollisionShape(l,at).isEmpty(),"the cemetery's railings are whole at x="+x);}
                for(int x=CalderTown.GATE_W;x<=CalderTown.GATE_E;x++)h.assertTrue(CalderBlock.is(l.getBlockState(b.offset(x,0,CalderTown.RAILS)),CalderBlock.Kind.GATE)&&CalderBlock.is(l.getBlockState(b.offset(x,1,CalderTown.RAILS)),CalderBlock.Kind.GATE),"its gate, shut, at x="+x);
            }).thenSucceed();
    }

    /** Whether a child on foot can be somewhere: nothing in a body that size standing there, or standing on what is there if that is no more than a block high. */
    private static boolean fits(ServerLevel l,BlockPos b,double x,double z){
        double wx=b.getX()+x,wz=b.getZ()+z,half=.16;
        return l.noCollision(new AABB(wx-half,b.getY()+.02,wz-half,wx+half,b.getY()+.95,wz+half))||l.noCollision(new AABB(wx-half,b.getY()+1.02,wz-half,wx+half,b.getY()+1.95,wz+half));
    }
    /** Everywhere a child on foot can get to from Mill Street by quarter blocks, as a set of places; null in its first place names a way off the town's ground. */
    private static java.util.BitSet reach(ServerLevel l,BlockPos b,java.util.function.Predicate<BlockPos> shut,String[] off){
        int x0=CalderTown.W-2,z0=CalderTown.N-2,wide=(CalderTown.E+3-x0)*4,deep=(BikeNightSite.NORTH+3-z0)*4;
        var seen=new java.util.BitSet(wide*deep);var todo=new java.util.ArrayDeque<int[]>();
        int si=(int)((.5-x0)*4),sj=(int)((-60.5-z0)*4);seen.set(sj*wide+si);todo.add(new int[]{si,sj});
        int[][] step={{1,0},{-1,0},{0,1},{0,-1}};
        while(!todo.isEmpty()){
            var at=todo.poll();
            for(int[] d:step){
                int i=at[0]+d[0],j=at[1]+d[1];if(i<0||j<0||i>=wide||j>=deep||seen.get(j*wide+i))continue;
                seen.set(j*wide+i);
                double x=x0+i/4.0,z=z0+j/4.0;var cell=BlockPos.containing(x,0,z);
                if(shut.test(cell)||!fits(l,b,x,z)){seen.clear(j*wide+i);todo.remove(null);continue;}
                boolean out=x<CalderTown.W||x>=CalderTown.E+1||z<CalderTown.RAILS||z>=BikeNightSite.NORTH+1||l.getBlockState(b.offset(cell).below()).isAir();
                if(out&&off[0]==null)off[0]=x+","+z;
                if(!out)todo.add(new int[]{i,j});
            }
        }
        return seen;
    }
    private static boolean reached(java.util.BitSet seen,double x,double z){
        int x0=CalderTown.W-2,z0=CalderTown.N-2,wide=(CalderTown.E+3-x0)*4;return seen.get((int)((z-z0)*4)*wide+(int)((x-x0)*4));
    }

    @GameTest(template="empty",batch="town_ways",timeoutTicks=2400)
    public static void nobodyGetsOffTheTownsGroundAndEveryWayThroughIsOpen(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,town(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the town is resident"))
            .thenExecute(()->{
                var off=new String[]{null};var seen=reach(l,b,cell->false,off);
                h.assertTrue(off[0]==null,"from Mill Street a child on foot gets nowhere off the town's ground: but does at "+off[0]);
                // The streets, and the ways through that are not streets.
                Object[][] places={{"Main Street",20.5,-91.5},{"Elm Street's west end",-30.5,-128.5},{"Elm Street's east end",30.5,-120.5},{"Church Lane by the gate",.5,-170.5},{"West Avenue",-49.5,-80.5},
                    {"West Avenue past its bend",-45.5,-140.5},{"East Avenue",49.5,-70.5},{"East Avenue past its bend",46.5,-140.5},{"Elm Court's circle",22.5,-140.5},
                    {"the gap between the hardware store and the bank",-33.5,-80.5},{"the alley behind them",-20.5,-70.5},{"the gap between the bank and the theater",-22.5,-79.5},
                    {"the empty lot's trail",-22.5,-66.5},{"the alley behind the laundromat, west of the dock",9.5,-113.5},{"the same alley east of it",30.5,-112.5},
                    {"the gap between the laundromat and the Video Hut",19.5,-104.5},{"the gap between the Video Hut and the barber",30.5,-104.5},{"Elm Street's pavement, through the alley's fence",28.5,-116.5},
                    {"the drive-thru lane",34.5,-70.5},{"the lot behind the burger place",25.5,-80.5},{"the grocery's yard",-7.5,-118.5},
                    {"the school's field",-18.5,-145.5},{"the grass north of the field",-17.5,-161.5},{"the cut",-5.5,-150.5},{"the yard by the cut",-1.5,-144.5},
                    {"the yard by East Avenue",39.5,-144.5},{"the path out of the court",21.5,-155.5},{"the cemetery's gate",-.5,-175.5},{"the school's lawn",-35.5,-136.5}};
                for(var place:places)h.assertTrue(reached(seen,(double)place[1],(double)place[2]),"a child on foot gets to "+place[0]);
                // But for its two holes the field's fence is whole, to a child as much as to a bike.
                var none=new String[]{null};
                var shut=reach(l,b,cell->cell.getZ()==CalderTown.HOLE_SOUTH.getZ()&&cell.getX()==CalderTown.HOLE_SOUTH.getX()||cell.getZ()==CalderTown.HOLE_NORTH.getZ()&&cell.getX()==CalderTown.HOLE_NORTH.getX(),none);
                h.assertTrue(!reached(shut,-18.5,-145.5)&&reached(shut,-17.5,-161.5)&&reached(shut,20.5,-91.5),"with the two holes stopped nobody gets onto the field, and everybody still gets everywhere else");
            }).thenSucceed();
    }

    /** One of the three's bikes with somebody on it, for riding the town's ways: where, and looking which way (180 is north). */
    private static PackBike mounted(ServerLevel l,BlockPos b,double x,double z,float yaw){
        var bike=BikeNightRegistry.PACK_BIKE.get().create(l);var a=LiteraryRegistry.ACTOR.get().create(l);
        bike.look(PackBike.RAY);bike.moveTo(b.getX()+x,b.getY(),b.getZ()+z,yaw,0);
        a.bind(P,null);a.appearance(LiteraryActor.STRANGER,BikeNightBullies.RAY);a.moveTo(b.getX()+x,b.getY(),b.getZ()+z,yaw,0);
        l.addFreshEntity(bike);l.addFreshEntity(a);a.startRiding(bike,true);a.gesture(BikeNightBullies.RIDE);
        return bike;
    }

    @GameTest(template="empty",batch="town_ride",timeoutTicks=6000)
    public static void aBikeIsRiddenFromTheParksGateToTheCemeterysByTheBackWays(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final PackBike[] bike={null};final int[] at={0},leg={0};final double[] top={0,0};final boolean[] offDock={false};
        // Out of the gate and up Mill Street; west along Main and down the gap between the hardware store and the bank; along the alley behind
        // the bank and the theater; up Mill Street again and into the laundromat's alley, over its dock; out through the alley's fence to Elm
        // Street; up Elm Court, round into the path at its head, and out to Church Lane and the cemetery's gate.
        double[][] way={{.5,-60},{.5,-84},{-6,-90.5},{-30,-90.5},{-33.5,-87},{-33.5,-78},{-31,-71.5},{-12,-70.6},{-5.5,-70.5},{-2,-76},{.5,-86},{2.5,-98},{2.5,-108},{6,-111},
            {9.5,-111},{14,-111},{20.5,-111},{26,-112.5},{28.9,-114},{28.9,-117.5},{26,-121},{22.5,-126},{22.5,-136},{21.6,-145},{21.6,-150},{21.6,-160},{18,-166.5},{4,-170.5},{-.5,-173},{-.5,-175.4}};
        h.startSequence().thenExecute(()->f.chunks.hold(l,town(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the town is resident"))
            // A night with nobody in it is put away, and its bikes with it: somebody is in the park while this one is ridden.
            .thenExecute(()->f.player("town_rider",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b),"waiting for the night to begin"))
            .thenExecute(()->{bike[0]=mounted(l,b,.5,-50.5,180);top[1]=bike[0].getY();})
            .thenWaitUntil(()->{
                var here=bike[0].position();double x=here.x-b.getX(),z=here.z-b.getZ(),y=here.y-b.getY();
                if(x>=CalderTown.DOCK_W&&x<=CalderTown.DOCK_E+1&&z>=CalderTown.DOCK_N&&z<=CalderTown.DOCK_S+1)top[0]=Math.max(top[0],y);
                if(at[0]>16&&x>CalderTown.DOCK_E+1.5&&y<.1)offDock[0]=true;
                var to=way[at[0]];
                h.assertTrue(bike[0].isAlive()&&bike[0].getFirstPassenger() instanceof LiteraryActor,"the bike and its rider are still there");
                if(Math.hypot(x-to[0],z-to[1])<1){at[0]++;leg[0]=0;if(at[0]==way.length){bike[0].toward(null,0);return;}to=way[at[0]];}
                bike[0].toward(new Vec3(b.getX()+to[0],b.getY(),b.getZ()+to[1]),BikeNightBullies.APPROACH);leg[0]++;
                h.assertTrue(at[0]==way.length,"waiting for the bike to get there: it is at "+x+","+y+","+z+", bound for "+to[0]+","+to[1]+" (place "+at[0]+" of "+way.length+"), "+leg[0]+" ticks on the way"+(bike[0].heldUp()?", held up":""));
            })
            .thenExecute(()->{
                h.assertTrue(top[0]>.95&&offDock[0],"it went up the boards onto the dock and off the end of it: as high as "+top[0]);
                var here=bike[0].position();
                h.assertTrue(here.z-b.getZ()<CalderTown.RAILS+3&&Math.abs(here.x-b.getX())<2&&bike[0].getFirstPassenger() instanceof LiteraryActor,"and stands at the cemetery's gate, its rider on it: "+here.subtract(Vec3.atLowerCornerOf(b)));
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="town_clock",timeoutTicks=1200)
    public static void theBanksClockKeepsTheNightsTimeAndTheMarqueeChangesWithTheNight(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};
        int colon=CalderDigitBlock.COLON,deg=CalderDigitBlock.DEGREES;
        h.assertTrue(java.util.Arrays.equals(CalderTown.shown(0),new int[]{1,1,colon,4,1})&&java.util.Arrays.equals(CalderTown.shown(1200*19),new int[]{1,2,colon,0,0})&&java.util.Arrays.equals(CalderTown.shown(1200*26+100),new int[]{1,2,colon,0,7}),
            "the night starts at 11:41, it is 12:00 nineteen minutes in and 12:07 seven after that");
        h.assertTrue(java.util.Arrays.equals(CalderTown.shown(150),new int[]{-1,4,1,deg,-1})&&java.util.Arrays.equals(CalderTown.shown(6000*3+150),new int[]{-1,3,8,deg,-1})&&java.util.Arrays.equals(CalderTown.shown(6000*40+150),new int[]{-1,3,3,deg,-1}),
            "three seconds in every ten it shows the temperature: 41 at first, a degree colder every five minutes, and never under 33");
        h.startSequence().thenExecute(()->f.chunks.hold(l,town(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the town is resident"))
            .thenExecute(()->{
                h.assertTrue(CalderTown.feature(l,b)==0,"as built, the first double feature is up");
                CalderTown.clock(l,b,1200*26);
                h.assertTrue(java.util.Arrays.equals(CalderTown.clockShows(l,b),new int[]{1,2,colon,0,7}),"the clock's places show what it is set to: "+java.util.Arrays.toString(CalderTown.clockShows(l,b)));
                p[0]=f.player("town_reader",P);
            })
            // A night begins: the next double feature goes up, and the clock is the night's.
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null&&CalderTown.feature(l,b)==1,"waiting for the night to put the next feature up: "+CalderTown.feature(l,b)))
            .thenWaitUntil(()->h.assertTrue(java.util.Arrays.equals(CalderTown.clockShows(l,b),new int[]{1,1,colon,4,1}),"and for the clock to be the night's own: "+java.util.Arrays.toString(CalderTown.clockShows(l,b))))
            .thenExecute(()->{
                for(int col=0;col<7;col++)for(int row=0;row<2;row++)h.assertTrue(sign(l,b,CalderTown.MARQUEE.getX()+col,CalderTown.MARQUEE.getY()+row,CalderTown.MARQUEE.getZ(),CalderSigns.Sign.MARQUEE_B),"the whole of the second feature is up, and nothing of the first");
                CalderTown.newNight(l,b);h.assertTrue(CalderTown.feature(l,b)==2,"the night after, the third");
                CalderTown.newNight(l,b);h.assertTrue(CalderTown.feature(l,b)==0,"and then the first again");
            }).thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.482: the hunt
    @AfterBatch(batch="hunt_ways") public static void huntWaysDone(ServerLevel l){done();}
    @AfterBatch(batch="hunt_split") public static void huntSplitDone(ServerLevel l){done();}
    @AfterBatch(batch="hunt_caught") public static void huntCaughtDone(ServerLevel l){done();}
    /** The whole place: the park and the town. */
    private static AABB whole(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()+CalderTown.W-1,b.getY()-5,b.getZ()+CalderTown.N-1,b.getX()+CalderTown.E+2,b.getY()+CalderTown.TOP+3,b.getZ()+2);}
    private static List<String> named(int[] path){var out=new ArrayList<String>();for(int id:path)out.add(CalderRoutes.place(id).name());return out;}
    /** A ball of sand from where a reader stands, at one of the three. */
    private static void throwAt(ServerPlayer p,LiteraryActor at){
        var item=BikeNightRegistry.SAND_BALL.get();
        p.setItemInHand(InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(item));p.getCooldowns().removeCooldown(item);
        p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,at.position().add(0,at.getBbHeight()*.5,0));
        p.gameMode.useItem(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND);
    }

    @GameTest(template="empty",batch="hunt_ways",timeoutTicks=2400)
    public static void everyWayThroughTheTownIsClearForABikeAndTheyAllJoinUp(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                // The park's gate is chained until the three open it: the way through it is looked at as they leave it.
                BikeNightSite.gateOpen(l,b);
                var blocked=new ArrayList<String>();
                for(var w:CalderRoutes.ways()){var why=CalderRoutes.blocked(l,b,w);if(why!=null)blocked.add(why);}
                h.assertTrue(blocked.isEmpty(),"a bike gets along every one of the town's ways without touching anything: "+blocked.size()+" do not, "+blocked.stream().limit(8).toList());
                int gate=CalderRoutes.id("gate_in"),cemetery=CalderRoutes.id("cemetery_gate");
                for(int i=0;i<CalderRoutes.places();i++){var path=CalderRoutes.path(gate,i,-1);h.assertTrue(path[0]==gate&&path[path.length-1]==i,"there is a way from the park's gate to "+CalderRoutes.place(i).name());}
                // The way to the cemetery's gate; a way round a place where there is one; and through it where there is not.
                var north=CalderRoutes.path(gate,cemetery,-1);var names=named(north);
                h.assertTrue(names.contains("mill_main")&&names.get(names.size()-2).equals("church_gate")&&CalderRoutes.length(north)<190,"from the park's gate to the cemetery's: "+names+", "+CalderRoutes.length(north)+" blocks");
                var round=named(CalderRoutes.path(CalderRoutes.id("park_mill"),CalderRoutes.id("mill_alley"),CalderRoutes.id("mill_lot")));
                h.assertTrue(!round.contains("mill_lot")&&round.contains("lot_gap")&&round.get(round.size()-1).equals("mill_alley"),"up Mill Street, keeping clear of a place on it, is by the empty lot and the alley: "+round);
                var through=CalderRoutes.path(gate,cemetery,CalderRoutes.id("church_gate"));
                h.assertTrue(through[through.length-1]==cemetery,"the cemetery's gate is reached by the middle of Church Lane whatever is to be kept clear of");
                // The back ways are among them. What only a reader fits through is not: nothing of theirs goes onto the school's field.
                int tight=0,back=0;for(var w:CalderRoutes.ways()){if(w.tight())tight++;if(w.back())back++;}
                h.assertTrue(tight>=20&&back>tight,"the gaps, the alleys, the dock, the lot, the lane, the yard, the cut and the path are ways: "+tight+" tight, "+back+" back");
                for(int i=0;i<CalderRoutes.places();i++){var at=CalderRoutes.place(i);
                    h.assertTrue(!(at.x()>CalderTown.FIELD_W&&at.x()<CalderTown.FIELD_E&&at.z()>CalderTown.FIELD_N&&at.z()<CalderTown.FIELD_S),"no place of theirs is on the school's field: "+at.name());}
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="hunt_split",timeoutTicks=9000)
    public static void inTheTownTheySplitUpOneBehindAndTwoByTheBackWaysToHeadAReaderOff(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who={null,null,null};final PackBike[] bikes={null,null,null};
        final int[] aims={-1,-1,-1};final boolean[] gap={false},west={false},east={false};final long[] threw={-100};
        // The reader stands on Main Street between the gap by the bank and the Food Fair's way in.
        double rx=-29.5,rz=-91.5;
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("hunt_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night, and for the reader to be the park's size"))
            .thenExecute(()->f.at(p[0],P,-14.5,0,-20.5))
            .thenWaitUntil(()->{three(l,b,who,bikes);h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE,"waiting for the chase: stage "+BikeNightBullies.stage(b));})
            .thenExecute(()->f.at(p[0],P,rx,0,rz))
            .thenWaitUntil(()->{
                three(l,b,who,bikes);
                for(int i=0;i<3;i++){int aim=BikeNightBullies.aim(b,i);if(aim>=0&&aims[i]<0)aims[i]=aim;}
                // Tully is kept off with sand, as a reader would: each ball stops him where he is for three seconds.
                if(who[0]!=null&&bikes[0]!=null&&bikes[0].position().distanceTo(p[0].position())<5&&l.getGameTime()-threw[0]>20){threw[0]=l.getGameTime();throwAt(p[0],who[0]);}
                if(bikes[0]!=null&&bikes[0].getX()-b.getX()>rx+1)east[0]=true;
                if(bikes[1]!=null){double x=bikes[1].getX()-b.getX(),z=bikes[1].getZ()-b.getZ();
                    if(Math.abs(x+33)<1.2&&z>-85.5&&z<-76.5)gap[0]=true;
                    if(gap[0]&&x<rx-1&&z<-86)west[0]=true;}
                h.assertTrue(aims[0]>=0&&aims[1]>=0&&aims[2]>=0&&gap[0]&&west[0]&&east[0],"waiting for Ray-Ray to come out of the gap behind the reader with Tully in front of them: aims "
                    +(aims[0]<0?"none":CalderRoutes.place(aims[0]).name())+", "+(aims[1]<0?"none":CalderRoutes.place(aims[1]).name())+", "+(aims[2]<0?"none":CalderRoutes.place(aims[2]).name())
                    +"; through the gap "+gap[0]+", west of the reader "+west[0]+", Tully east "+east[0]+"; Ray-Ray at "+(bikes[1]==null?"nowhere":bikes[1].position().subtract(Vec3.atLowerCornerOf(b))));
            })
            .thenExecute(()->{
                h.assertTrue(CalderRoutes.place(aims[0]).name().equals("main_food"),"Tully made for the end of the reader's way that is nearer them: "+CalderRoutes.place(aims[0]).name());
                h.assertTrue(CalderRoutes.place(aims[1]).name().equals("main_gap_hb"),"Ray-Ray for its other end, by a way that does not go through the reader: "+CalderRoutes.place(aims[1]).name());
                h.assertTrue(CalderRoutes.place(aims[2]).name().equals("gap_hb"),"and Duff for the place past that, the mouth of the gap: "+CalderRoutes.place(aims[2]).name());
                h.assertTrue(p[0].getHealth()==p[0].getMaxHealth(),"nobody is hurt");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="hunt_close",timeoutTicks=9000)
    public static void eachOfThreeReadersHasOneOfThemAndTheOneBehindSomethingIsGoneInAfter(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null,null};final LiteraryActor[] who={null,null,null};final PackBike[] bikes={null,null,null};
        // One reader stands on Main Street with a bike stood across the road on either side of them. The other two stand in the open: on Mill Street, and outside the theater.
        double[][] at={{-29.5,-91.5},{.5,-80},{22.5,-91.5}};var east=b.offset(-29,0,-92);var west=b.offset(-31,0,-92);final boolean[] went={false,false,false};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{p[0]=f.player("hunt_hidden",P);p[1]=f.player("hunt_open",P);p[2]=f.player("hunt_theater",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&p[2].getBbHeight()<1.1&&BikeNight.here(p[0])&&BikeNight.here(p[1])&&BikeNight.here(p[2]),
                "waiting for the night, and for the three readers to be the park's size"))
            .thenExecute(()->{f.at(p[0],P,-14.5,0,-20.5);f.at(p[1],P,-12.5,0,-20.5);f.at(p[2],P,-10.5,0,-20.5);})
            .thenWaitUntil(()->{three(l,b,who,bikes);h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE,"waiting for the chase: stage "+BikeNightBullies.stage(b));})
            .thenExecute(()->{
                l.setBlock(east,NightBikeBlock.of(NightBike.Make.HARE,Direction.NORTH,false),3);l.setBlock(west,NightBikeBlock.of(NightBike.Make.LAMP,Direction.NORTH,false),3);
                for(int i=0;i<3;i++)f.at(p[i],P,at[i][0],0,at[i][1]);
                h.assertTrue(!BikeNightHunt.clear(l,Vec3.atLowerCornerOf(b).add(-27.5,0,-91.5),p[0].position())&&!BikeNightHunt.clear(l,Vec3.atLowerCornerOf(b).add(-33,0,-91.5),p[0].position()),
                    "from neither end of that stretch of the street is there a clear way to the one between the bikes");
            })
            .thenWaitUntil(()->{
                three(l,b,who,bikes);for(int i=0;i<3;i++)went[i]|=BikeNightBullies.closing(b,i);
                var where=new StringBuilder();
                for(int i=0;i<3;i++){var bike=BikeNightBullies.bikeOf(l,b,i);int aim=BikeNightBullies.aim(b,i);
                    where.append("; ").append(BikeNightBullies.NAMES[i]).append(BikeNightBullies.gone(b,i)?" gone":bike==null?" nowhere":" at "+bike.position().subtract(Vec3.atLowerCornerOf(b))+(bike.heldUp()?" held up":"")
                        +(BikeNightBullies.closing(b,i)?" going in":" for "+(aim<0?"a reader":CalderRoutes.place(aim).name())));}
                h.assertTrue(BikeNightBullies.caught(b,p[0])&&BikeNightBullies.caught(b,p[1])&&BikeNightBullies.caught(b,p[2]),"waiting for each of the three readers to be caught: "
                    +BikeNightBullies.caught(b,p[0])+", "+BikeNightBullies.caught(b,p[1])+", "+BikeNightBullies.caught(b,p[2])+where);
            })
            .thenExecute(()->{
                int[] by={f.own(p[0],P).getInt("CaughtBy"),f.own(p[1],P).getInt("CaughtBy"),f.own(p[2],P).getInt("CaughtBy")};
                h.assertTrue(by[0]!=by[1]&&by[1]!=by[2]&&by[0]!=by[2],"each of the three had one of the readers: "+by[0]+", "+by[1]+", "+by[2]);
                h.assertTrue(went[by[0]],"the one who had the reader between the bikes went in after them from the end of the street: "+BikeNightBullies.NAMES[by[0]]);
                h.assertTrue(l.getBlockState(east).getBlock() instanceof NightBikeBlock&&l.getBlockState(west).getBlock() instanceof NightBikeBlock,"and both bikes are still standing");
                h.assertTrue(f.own(p[0],P).getString("CaughtOn").equals("Main Street")&&f.own(p[1],P).getString("CaughtOn").equals("Mill Street")&&f.own(p[2],P).getString("CaughtOn").equals("Main Street"),
                    "each record keeps its own street: "+f.own(p[0],P).getString("CaughtOn")+", "+f.own(p[1],P).getString("CaughtOn")+", "+f.own(p[2],P).getString("CaughtOn"));
                for(var q:p)h.assertTrue(q.getHealth()==q.getMaxHealth()&&heard(q,BikeNightTexts.CAUGHT_NO_BIKE_CUE),"nobody is hurt, and a reader with no bike is told they are down in the leaves");
            }).thenSucceed();
    }
    @AfterBatch(batch="hunt_close") public static void huntCloseDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="hunt_caught",timeoutTicks=9000)
    public static void withinReachForTwoSecondsAReaderIsCaughtTheBikeGoesWithThemAndNobodyIsHurt(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who={null,null,null};final PackBike[] bikes={null,null,null};
        final NightBike[] mine={null};final int[] by={-1};final long[] first={-1},at={-1};final PackBike[] taken={null};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("hunt_rider",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night, and for the reader to be the park's size"))
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.WASP);})
            // The reader sits on it at the rack while the three come in, go round and stand about.
            .thenWaitUntil(()->{three(l,b,who,bikes);h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE,"waiting for the chase: stage "+BikeNightBullies.stage(b));})
            // On the Wasp, standing on Main Street.
            .thenExecute(()->{
                h.assertTrue(p[0].getVehicle()==mine[0]&&!BikeNightBullies.caught(b,p[0]),"the reader is still on the Wasp, and nobody has had it");
                mine[0].moveTo(b.getX()-27,b.getY(),b.getZ()-91.5,90,0);hush(p[0]);
            })
            .thenWaitUntil(()->{
                three(l,b,who,bikes);
                boolean near=false;for(int i=0;i<3;i++)if(bikes[i]!=null&&bikes[i].position().distanceTo(p[0].position())<BikeNightHunt.REACH)near=true;
                if(near&&first[0]<0)first[0]=l.getGameTime();
                if(BikeNightBullies.caught(b,p[0])&&at[0]<0){at[0]=l.getGameTime();for(int i=0;i<3;i++)if(BikeNightBullies.taken(l,b,i)!=null){by[0]=i;taken[0]=BikeNightBullies.taken(l,b,i);}}
                h.assertTrue(at[0]>=0,"waiting for one of them to have had the reader within reach for two seconds: first within reach at "+first[0]+", now "+l.getGameTime());
            })
            .thenExecute(()->{
                h.assertTrue(first[0]>=0&&at[0]-first[0]>=BikeNightHunt.CAUGHT-2,"not before two seconds within reach: "+(at[0]-first[0])+" ticks");
                h.assertTrue(p[0].getVehicle()==null&&!mine[0].isAlive(),"the reader is off the bike, and it is not theirs to ride");
                // It went with him: it is not also left standing in the road where they were.
                var left=new ArrayList<BlockPos>();
                for(var cell:BlockPos.betweenClosed(p[0].blockPosition().offset(-5,-1,-5),p[0].blockPosition().offset(5,2,5)))if(l.getBlockState(cell).getBlock() instanceof NightBikeBlock)left.add(cell.immutable());
                h.assertTrue(left.isEmpty(),"no bike is left standing where they were caught: "+left);
                h.assertTrue(by[0]>=0&&taken[0]!=null&&taken[0].look()==PackBike.MAKES+NightBike.Make.WASP.ordinal()&&taken[0].getPassengers().isEmpty()
                    &&taken[0].position().distanceTo(bikes[by[0]].position())<1.6,"the one who caught them has the Wasp beside his own: "+by[0]);
                h.assertTrue(p[0].getHealth()==p[0].getMaxHealth(),"and the reader is not hurt: "+p[0].getHealth());
                h.assertTrue(heard(p[0],String.format(BikeNightTexts.CAUGHT_CUE,BikeNightBullies.NAMES[by[0]])),"they are told who has it");
                var own=f.own(p[0],P);
                h.assertTrue(own.getBoolean("Caught")&&own.getInt("CaughtBy")==by[0]&&own.getString("CaughtOn").equals("Main Street"),"and their record keeps who, and where: "+own.getString("CaughtOn"));
                h.assertTrue(saying(who[by[0]]).equals(BikeNightTexts.CAUGHT[by[0]]),"he says so: "+saying(who[by[0]]));
            })
            // He is off with it, and with nobody left to go after the others go too.
            .thenWaitUntil(()->h.assertTrue(BikeNightBullies.gone(b,0)&&BikeNightBullies.gone(b,1)&&BikeNightBullies.gone(b,2)&&BikeNightBullies.stage(b)==BikeNightBullies.GONE,
                "waiting for the three to be gone: "+BikeNightBullies.gone(b,0)+", "+BikeNightBullies.gone(b,1)+", "+BikeNightBullies.gone(b,2)+", stage "+BikeNightBullies.stage(b)))
            .thenExecute(()->{
                h.assertTrue(!taken[0].isAlive()&&l.getEntitiesOfClass(PackBike.class,whole(f)).isEmpty()&&l.getEntitiesOfClass(LiteraryActor.class,whole(f),BikeNightBullies::is).isEmpty(),"none of them is left in the town, and no bike of theirs, and not the Wasp");
                h.assertTrue(BikeNightBullies.out(b,p[0])&&f.own(p[0],P).getBoolean("Out")&&heard(p[0],BikeNightTexts.OUT_CUE),"with nobody to carry them the reader is out, and is told the way home");
                h.assertTrue(p[0].getHealth()==p[0].getMaxHealth()&&BikeNight.bikeAt(b,NightBike.Make.WASP)==null,"still not hurt; and the Wasp is not in the park");
                // They do not take another: the rest of the rack is other people's.
                f.at(p[0],P,BikeNightSite.bikeAt(NightBike.Make.HARE).getX()+.5,0,BikeNightSite.bikeAt(NightBike.Make.HARE).getZ()+1.5);hush(p[0]);
                hand(f,p[0],b.offset(BikeNightSite.bikeAt(NightBike.Make.HARE)));
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.NO_MORE),"waiting for them to be told why: "+HEARD.get(p[0].getUUID())))
            .thenExecute(()->{
                var hare=b.offset(BikeNightSite.bikeAt(NightBike.Make.HARE));
                h.assertTrue(!p[0].isPassenger()&&l.getBlockState(hare).getBlock() instanceof NightBikeBlock,"a hand on another bike in the rack leaves it where it is, and them off it");
                hand(f,p[0],hare);
                h.assertTrue(!p[0].isPassenger()&&l.getBlockState(hare).getBlock() instanceof NightBikeBlock,"and so does a second");
            }).thenSucceed();
    }

    @AfterBatch(batch="hunt_north") public static void huntNorthDone(ServerLevel l){done();}
    @AfterBatch(batch="hunt_pegs") public static void huntPegsDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="hunt_north",timeoutTicks=9000)
    public static void nobodyHeadsAReaderOffToTheNorthAndSomebodyDoesToTheWest(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who={null,null,null};final PackBike[] bikes={null,null,null};
        final double[] at={2.5,-94};final String[] wrong={null};final boolean[] headed={false};final int[] planned={0};
        // Each tick: where everybody is, and is making for, against where the reader is.
        Runnable north=()->{
            three(l,b,who,bikes);
            for(int i=0;i<3;i++){
                if(bikes[i]!=null&&bikes[i].getZ()-b.getZ()<at[1]-2.5&&wrong[0]==null)wrong[0]=BikeNightBullies.NAMES[i]+" is north of the reader, at "+(bikes[i].getZ()-b.getZ())+" to their "+at[1];
                int aim=BikeNightBullies.aim(b,i);if(aim<0)continue;planned[0]++;
                if(CalderRoutes.place(aim).z()<at[1]-BikeNightHunt.NORTH_OPEN-7&&wrong[0]==null)wrong[0]=BikeNightBullies.NAMES[i]+" is making for "+CalderRoutes.place(aim).name()+", north of the reader at "+at[1];
            }
        };
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("hunt_north",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night, and for the reader to be the park's size"))
            .thenExecute(()->f.at(p[0],P,-14.5,0,-20.5))
            .thenWaitUntil(()->{three(l,b,who,bikes);h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE,"waiting for the chase: stage "+BikeNightBullies.stage(b));})
            // Standing on Mill Street north of Main, until the first of them is near.
            .thenExecute(()->f.at(p[0],P,at[0],0,at[1]))
            .thenWaitUntil(()->{
                north.run();double nearest=99;for(var bike:bikes)if(bike!=null)nearest=Math.min(nearest,bike.position().distanceTo(p[0].position()));
                h.assertTrue(nearest<12,"waiting for one of them to be near the reader on Mill Street: "+nearest);})
            // Then north, faster than they ride, up to Elm Street.
            .thenWaitUntil(()->{
                at[1]=Math.max(-123.4,at[1]-.32);f.at(p[0],P,at[0],0,at[1]);north.run();
                h.assertTrue(at[1]<=-123.4,"the reader is riding north: at "+at[1]);})
            .thenExecute(()->h.assertTrue(wrong[0]==null&&planned[0]>20,"nobody headed the reader off to the north, standing or going: "+wrong[0]+" ("+planned[0]+" looks at where they were making for)"))
            // And west along Elm Street, where somebody does.
            .thenWaitUntil(()->{
                at[0]=Math.max(-40,at[0]-.32);at[1]=at[0]>-11.5?-123.4-(2.5-at[0])*(5.1/14):-128.5;f.at(p[0],P,at[0],0,at[1]);three(l,b,who,bikes);
                for(int i=1;i<3;i++){int aim=BikeNightBullies.aim(b,i);if(aim>=0&&CalderRoutes.place(aim).x()<at[0]-1&&Math.abs(CalderRoutes.place(aim).z()-at[1])<12)headed[0]=true;}
                h.assertTrue(headed[0],"waiting for one of the two who head a reader off to be making for a place ahead of them on Elm Street: the reader is at "+at[0]+","+at[1]
                    +", they are making for "+(BikeNightBullies.aim(b,1)<0?"nowhere":CalderRoutes.place(BikeNightBullies.aim(b,1)).name())+" and "+(BikeNightBullies.aim(b,2)<0?"nowhere":CalderRoutes.place(BikeNightBullies.aim(b,2)).name()));})
            .thenExecute(()->h.assertTrue(p[0].getHealth()==p[0].getMaxHealth()&&!BikeNightBullies.caught(b,p[0]),"and the reader, who kept ahead of them, has not been caught"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="hunt_pegs",timeoutTicks=12000)
    public static void aCaughtReaderOnAFriendsPegsIsStillInItAndOutWhenNobodyIsLeftToCarryThem(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final LiteraryActor[] who={null,null,null};final PackBike[] bikes={null,null,null};
        final NightBike[] ride={null,null};final long[] since={-1};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{p[0]=f.player("hunt_carrier",P);p[1]=f.player("hunt_carried",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&BikeNight.here(p[0])&&BikeNight.here(p[1]),"waiting for the night, and for both to be the park's size"))
            .thenExecute(()->{ride[0]=take(h,f,p[0],NightBike.Make.HOPPER);ride[1]=take(h,f,p[1],NightBike.Make.WASP);})
            .thenWaitUntil(()->{three(l,b,who,bikes);h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE,"waiting for the chase: stage "+BikeNightBullies.stage(b));})
            // The Wasp stands on Main Street. The Hopper is a long way off, up on Church Lane.
            .thenExecute(()->{ride[1].moveTo(b.getX()-29.5,b.getY(),b.getZ()-91.5,90,0);ride[0].moveTo(b.getX()+40,b.getY(),b.getZ()-164.5,90,0);hush(p[1]);})
            .thenWaitUntil(()->{
                // The Hopper keeps away from whoever comes for it, as a rider would: to the other end of Church Lane.
                three(l,b,who,bikes);for(var bike:bikes)if(bike!=null&&bike.isAlive()&&bike.position().distanceTo(ride[0].position())<16){ride[0].moveTo(b.getX()+(ride[0].getX()-b.getX()>0?-40:40),b.getY(),b.getZ()-164.5,90,0);break;}
                h.assertTrue(BikeNightBullies.caught(b,p[1]),"waiting for the one on the Wasp to be caught");})
            .thenExecute(()->{
                h.assertTrue(!BikeNightBullies.caught(b,p[0])&&!BikeNightBullies.out(b,p[1])&&p[1].getVehicle()==null&&p[0].getVehicle()==ride[0],"caught, with a friend still riding: not out yet, and the friend is not caught");
                // The friend comes back for them: the Hopper beside them, and they get up on its pegs.
                ride[0].moveTo(p[1].getX()+1.2,b.getY(),p[1].getZ(),90,0);
                h.assertTrue(ride[0].interact(p[1],InteractionHand.MAIN_HAND).consumesAction()&&p[1].getVehicle()==ride[0]&&ride[0].getFirstPassenger()==p[0],"a hand on the Hopper puts them on its pegs");
                ride[0].moveTo(b.getX()+40,b.getY(),b.getZ()-164.5,90,0);since[0]=l.getGameTime();
            })
            // Longer than a reader on foot is given, carried all the while and kept away from the two who are left.
            .thenWaitUntil(()->{
                three(l,b,who,bikes);for(var bike:bikes)if(bike!=null&&bike.isAlive()&&bike.position().distanceTo(ride[0].position())<16){ride[0].moveTo(b.getX()+(ride[0].getX()-b.getX()>0?-40:40),b.getY(),b.getZ()-164.5,90,0);break;}
                h.assertTrue(l.getGameTime()-since[0]>BikeNightHunt.AFOOT+100,"waiting past the time a reader on foot is given");})
            .thenExecute(()->{
                h.assertTrue(p[1].getVehicle()==ride[0]&&!BikeNightBullies.out(b,p[1])&&!f.own(p[1],P).getBoolean("Out"),"carried on the pegs, they are still in the night");
                // Then the Hopper stands still, and is caught too.
                ride[0].moveTo(b.getX()-29.5,b.getY(),b.getZ()-91.5,90,0);hush(p[0]);hush(p[1]);
            })
            .thenWaitUntil(()->{
                var where=new StringBuilder();
                for(int i=0;i<3;i++){var bike=BikeNightBullies.bikeOf(l,b,i);int aim=BikeNightBullies.aim(b,i);
                    where.append("; ").append(BikeNightBullies.NAMES[i]).append(BikeNightBullies.gone(b,i)?" gone":bike==null?" nowhere":" at "+bike.position().subtract(Vec3.atLowerCornerOf(b))+(bike.heldUp()?" held up":"")+" for "+(aim<0?"the reader":CalderRoutes.place(aim).name())
                        +(bike==null?"":", clear "+BikeNightHunt.clear(l,bike.position(),p[0].position())));}
                where.append("; the reader at ").append(p[0].position().subtract(Vec3.atLowerCornerOf(b))).append(" on ").append(p[0].getVehicle()).append(", the Hopper alive ").append(ride[0].isAlive()).append(", the block there ").append(l.getBlockState(p[0].blockPosition()));
                h.assertTrue(BikeNightBullies.caught(b,p[0])&&BikeNightBullies.out(b,p[0])&&BikeNightBullies.out(b,p[1]),"waiting for the Hopper to be caught, and both of them to be out: "
                    +BikeNightBullies.caught(b,p[0])+", "+BikeNightBullies.out(b,p[0])+", "+BikeNightBullies.out(b,p[1])+", stage "+BikeNightBullies.stage(b)+where);})
            .thenExecute(()->{
                h.assertTrue(p[0].getVehicle()==null&&p[1].getVehicle()==null&&!ride[0].isAlive(),"both are off it, and it is gone");
                h.assertTrue(heard(p[0],BikeNightTexts.OUT_CUE)&&heard(p[1],BikeNightTexts.OUT_CUE)&&f.own(p[0],P).getBoolean("Out")&&f.own(p[1],P).getBoolean("Out"),"both are told the way home, and their records say so");
                h.assertTrue(p[0].getHealth()==p[0].getMaxHealth()&&p[1].getHealth()==p[1].getMaxHealth(),"and neither is hurt");
            }).thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.483: Gus's bike
    @GameTest(template="empty",batch="gus_bike",timeoutTicks=9000)
    public static void gusHasABikeOfHisOwnByTheBenchAndAHandOnItGetsAWordFromHim(GameTestHelper h){
        var f=park(h);BikeNightGus.keptHome=false;var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] gus={null};final PackBike[] his={null};
        final int[] times={0};final String[] last={""};
        Runnable listen=()->{var now=saying(gus[0]);if(!now.equals(last[0])&&now.equals(BikeNightTexts.GUS_HIS_BIKE))times[0]++;last[0]=now;};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("gus_bike_reader",P))
            .thenWaitUntil(()->{gus[0]=BikeNightGus.find(l,b);his[0]=BikeNightGus.bikeOf(l,b);
                h.assertTrue(BikeNight.begun(b)&&gus[0]!=null&&his[0]!=null&&p[0].getBbHeight()<1.1,"waiting for the night, Gus, his bike, and the reader to be the park's size");})
            .thenExecute(()->{
                h.assertTrue(his[0].look()==PackBike.GUS&&his[0].getPassengers().isEmpty()&&flat(his[0],b,BikeNightGus.BIKE_AT)<.2&&Math.abs(net.minecraft.util.Mth.wrapDegrees(his[0].getYRot()-BikeNightGus.BIKE_YAW))<1,
                    "his bike stands by the bench, pointing at the gate, with nobody on it: "+his[0].position().subtract(Vec3.atLowerCornerOf(b)));
                h.assertTrue(l.getEntitiesOfClass(PackBike.class,room(f).inflate(4)).size()==1,"and it is the only one of its kind in the park");
                h.assertTrue(l.noCollision(his[0].getBoundingBox().deflate(.02))&&!BikeNightSite.onSand(BikeNightGus.BIKE_AT),"it stands clear of the bench, and off the sand");
                h.assertTrue(flat(gus[0],b,BikeNightGus.BIKE_AT)<6,"a few steps from where he waits: "+flat(gus[0],b,BikeNightGus.BIKE_AT));
                f.at(p[0],P,BikeNightGus.BIKE_AT.x+1.6,0,BikeNightGus.BIKE_AT.z);
            })
            // He says hello first. Then a hand on the bike.
            .thenWaitUntil(()->h.assertTrue(BikeNightGus.doing(b)==BikeNightGus.FREE&&!gus[0].saying(),"waiting for him to have said hello"))
            .thenExecute(()->{
                hush(p[0]);
                h.assertTrue(his[0].isPickable()&&his[0].interact(p[0],InteractionHand.MAIN_HAND).consumesAction()&&!p[0].isPassenger()&&his[0].getPassengers().isEmpty(),"a hand on it: nobody is on it for that");
            })
            .thenWaitUntil(()->{listen.run();h.assertTrue(times[0]==1,"waiting for him to say whose it is");})
            // A second hand gets no more of it: he has said it.
            .thenExecute(()->h.assertTrue(his[0].interact(p[0],InteractionHand.MAIN_HAND).consumesAction(),"a second hand on it"))
            .thenExecuteFor(200,listen)
            .thenExecute(()->{
                h.assertTrue(times[0]==1,"he says it once: "+times[0]);
                h.assertTrue(!heard(p[0],BikeNightTexts.GUS_HIS_BIKE),"over his head, and never in chat");
                h.assertTrue(BikeNightGus.doing(b)<BikeNightGus.TO_BIKE&&his[0].isAlive()&&flat(his[0],b,BikeNightGus.BIKE_AT)<.2,"with nobody come for anybody he has not gone for it, and it has not moved");
            }).thenSucceed();
    }
    @AfterBatch(batch="gus_bike") public static void gusBikeDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="gus_ride",timeoutTicks=9000)
    public static void gusGetsOnHisBikeAndRidesBesideAReaderThroughTheGateAndTheTown(GameTestHelper h){
        var f=park(h);BikeNightGus.keptHome=false;var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] gus={null};final PackBike[] his={null};
        final NightBike[] mine={null};final double[] last={99},gate={99};var origin=Vec3.atLowerCornerOf(b);
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("gus_ride_reader",P))
            .thenWaitUntil(()->{gus[0]=BikeNightGus.find(l,b);his[0]=BikeNightGus.bikeOf(l,b);
                h.assertTrue(BikeNight.begun(b)&&gus[0]!=null&&his[0]!=null&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night, Gus, his bike, and the reader to be the park's size");})
            // The reader is on the Wasp at the rack, the gate stands open, and he is told to ride.
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.WASP);BikeNightSite.gateOpen(l,b);BikeNightGus.rideNow(b);})
            .thenWaitUntil(()->{
                if(BikeNightGus.doing(b)==BikeNightGus.TO_BIKE)last[0]=gus[0].position().distanceTo(his[0].position());
                h.assertTrue(BikeNightGus.doing(b)==BikeNightGus.RIDING&&gus[0].getVehicle()==his[0],"waiting for him to be on his bike: doing "+BikeNightGus.doing(b)+", "+last[0]+" from it");})
            .thenExecute(()->{
                h.assertTrue(last[0]<2.4,"he went to it on his own feet: "+last[0]+" from it when he got on");
                h.assertTrue(gus[0].gesture()==BikeNightGus.RIDE&&his[0].getPassengers().size()==1&&!his[0].isPickable(),"his hands are on the bars; and with him on it, it is not to be touched");
            })
            // In the park he comes over to the reader, and stops a little way off.
            .thenWaitUntil(()->h.assertTrue(his[0].position().distanceTo(mine[0].position())<BikeNightGus.KEEP+.6&&his[0].speedNow()<.02,"waiting for him beside the reader at the rack: "+his[0].position().distanceTo(mine[0].position())))
            .thenExecute(()->{
                h.assertTrue(his[0].position().distanceTo(mine[0].position())>1.2,"a little way off, and not on top of them: "+his[0].position().distanceTo(mine[0].position()));
                mine[0].moveTo(b.getX()-27,b.getY(),b.getZ()-91.5,90,0);
            })
            // The reader is on Main Street. He comes by the gate and the streets.
            .thenWaitUntil(()->{
                if(gate[0]>90&&his[0].getZ()-b.getZ()<BikeNightSite.NORTH)gate[0]=his[0].getX()-b.getX();
                h.assertTrue(his[0].position().distanceTo(mine[0].position())<BikeNightGus.KEEP+.6,"waiting for him on Main Street: at "+his[0].position().subtract(origin)+", "+CalderRoutes.place(CalderRoutes.nearest(b,his[0].position())).street());})
            .thenExecute(()->{
                h.assertTrue(Math.abs(gate[0]+.5)<2.1,"he left the park by its gate: at x "+gate[0]);
                h.assertTrue(gus[0].getVehicle()==his[0]&&CalderRoutes.place(CalderRoutes.nearest(b,his[0].position())).street().equals("Main Street"),"he is on his bike, on Main Street");
                // Out of his sight, round behind the shops.
                var to=CalderRoutes.at(b,CalderRoutes.id("alley_south_gap"));mine[0].moveTo(to.x,to.y,to.z,90,0);
            })
            .thenWaitUntil(()->h.assertTrue(his[0].position().distanceTo(mine[0].position())<BikeNightGus.KEEP+.6,"waiting for him round behind the shops: at "+his[0].position().subtract(origin)+", "+CalderRoutes.place(CalderRoutes.nearest(b,his[0].position())).street()))
            .thenExecute(()->h.assertTrue(gus[0].getVehicle()==his[0]&&gus[0].isAlive()&&p[0].getVehicle()==mine[0]&&p[0].getHealth()==p[0].getMaxHealth(),"he is beside them on his bike, and the reader is on theirs"))
            .thenSucceed();
    }
    @AfterBatch(batch="gus_ride") public static void gusRideDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="gus_calls",timeoutTicks=12000)
    public static void onTheRideGusCallsOutWhereTheThreeAreAndTheyNeverGoForHim(GameTestHelper h){
        var f=park(h);BikeNightGus.keptHome=false;BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who={null,null,null};final PackBike[] bikes={null,null,null};
        final LiteraryActor[] gus={null};final PackBike[] his={null};final boolean[] round={false},behind={false},nobody={false},calling={false};
        var said=new ArrayList<String>();final String[] last={""};final double[] near={99,99};double rx=-29.5,rz=-91.5;
        // Every line of his, in order. While the case is waiting on what he calls out about the three, anything he says is cut short as
        // soon as it has been heard, and his next call is due at once: the three are a few seconds from the reader, and he has two things to say.
        Runnable listen=()->{
            three(l,b,who,bikes);var now=saying(gus[0]);
            if(!now.isEmpty()&&!now.equals(last[0])){
                said.add(now);nobody[0]|=now.equals(BikeNightTexts.GUS_NOBODY);
                // How near the nearest of the three was to the reader when he called each.
                double nearest=99;for(var bike:bikes)if(bike!=null)nearest=Math.min(nearest,bike.position().distanceTo(p[0].position()));
                if(List.of(BikeNightTexts.GUS_ROUND).contains(now)&&!round[0]){round[0]=true;near[0]=nearest;}
                if(List.of(BikeNightTexts.GUS_BEHIND).contains(now)&&!behind[0]){behind[0]=true;near[1]=nearest;}
            }
            last[0]=now;
            if(calling[0]&&!(round[0]&&behind[0])){if(gus[0].saying()){gus[0].hush();last[0]="";}BikeNightGus.warnNow(b);}
        };
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("gus_calls_reader",P))
            .thenWaitUntil(()->{gus[0]=BikeNightGus.find(l,b);his[0]=BikeNightGus.bikeOf(l,b);
                h.assertTrue(BikeNight.begun(b)&&gus[0]!=null&&his[0]!=null&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night, Gus, his bike, and the reader to be the park's size");})
            // The reader takes the Wasp, which is theirs once they have sat on it, leaves it at the rack, and waits on the sand.
            .thenExecute(()->take(h,f,p[0],NightBike.Make.WASP))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("wasp"),"waiting for the Wasp to be theirs"))
            .thenExecute(()->f.at(p[0],P,-14.5,0,-20.5))
            .thenWaitUntil(()->{listen.run();h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE&&BikeNightGus.doing(b)==BikeNightGus.RIDING,"waiting for the chase, and for him to be on his bike: stage "+BikeNightBullies.stage(b)+", doing "+BikeNightGus.doing(b));})
            // The reader stands on Main Street with Gus beside them, and the three come: one along the street, two by the back ways.
            .thenExecute(()->{f.at(p[0],P,rx,0,rz);his[0].moveTo(b.getX()+rx,b.getY(),b.getZ()+rz-3,90,0);calling[0]=true;})
            .thenWaitUntil(()->{
                listen.run();
                h.assertTrue(round[0]&&behind[0],"waiting for him to call out that one of them has gone round, and that one is right behind: "+(BikeNightBullies.caught(b,p[0])?"the reader has been caught; ":"")+"he has said "+said);
            })
            .thenExecute(()->{
                h.assertTrue(near[0]>BikeNightGus.BEHIND_THEM&&near[0]<=BikeNightGus.ROUND_THEM,"he called that one had gone round while they were still coming: the nearest was "+near[0]+" off");
                h.assertTrue(near[1]<BikeNightGus.BEHIND_THEM,"and that one was right behind when one was: "+near[1]+" off");
                h.assertTrue(gus[0].getVehicle()==his[0],"he is on his bike");
            })
            // Nobody keeps them off: one of them has the reader, and takes the Wasp from the rack.
            .thenWaitUntil(()->{listen.run();h.assertTrue(BikeNightBullies.caught(b,p[0]),"waiting for the reader to be caught");})
            .thenWaitUntil(()->{listen.run();h.assertTrue(nobody[0]&&BikeNightBullies.stage(b)==BikeNightBullies.GONE,"waiting for the three to be gone, and for him to say that nobody looked at his: stage "+BikeNightBullies.stage(b)+", he has said "+said);})
            .thenExecute(()->{
                h.assertTrue(said.contains(BikeNightTexts.GUS_CAUGHT),"he saw them get a bike: he said "+said.subList(Math.max(0,said.size()-8),said.size())+"; the reader's record "+f.own(p[0],P));
                h.assertTrue(BikeNightGus.bikeOf(l,b)==his[0]&&his[0].isAlive()&&his[0].look()==PackBike.GUS&&gus[0].isAlive()&&gus[0].getVehicle()==his[0],"none of them went for him: he is on his bike, and it is his");
                h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)==null,"and the Wasp went with them");
                for(var line:BikeNightTexts.gusLines())h.assertTrue(!heard(p[0],line),"what he says is over his head and never in chat: "+line);
                h.assertTrue(p[0].getHealth()==p[0].getMaxHealth(),"nobody is hurt");
            }).thenSucceed();
    }
    @AfterBatch(batch="gus_calls") public static void gusCallsDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="sand_close",timeoutTicks=9000)
    public static void aBallOfSandThrownAtOneOfThemWhoIsRightOnTopOfAReaderStillMeetsHim(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who={null,null,null};final PackBike[] bikes={null,null,null};final int[] at={-1};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("sand_close_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night, and for the reader to be the park's size"))
            .thenExecute(()->f.at(p[0],P,-14.5,0,-20.5))
            .thenWaitUntil(()->{three(l,b,who,bikes);h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE,"waiting for the chase: stage "+BikeNightBullies.stage(b));})
            // On the grass by the main path, on foot, with the three riding at them.
            .thenExecute(()->f.at(p[0],P,6.5,0,-30.5))
            .thenWaitUntil(()->{
                three(l,b,who,bikes);
                for(int i=0;i<3&&at[0]<0;i++)if(who[i]!=null&&bikes[i]!=null&&who[i].getBoundingBox().contains(p[0].getEyePosition()))at[0]=i;
                h.assertTrue(at[0]>=0,"waiting for one of them to be right on top of the reader");
            })
            .thenExecute(()->{
                h.assertTrue(who[at[0]].gesture()!=BikeNightBullies.COUGH&&!BikeNightBullies.caught(b,p[0]),"he is not coughing yet, and has not had them");
                throwAt(p[0],who[at[0]]);
            })
            .thenWaitUntil(()->h.assertTrue(who[at[0]].gesture()==BikeNightBullies.COUGH,"waiting for the ball thrown from inside his reach to meet him"))
            .thenExecute(()->h.assertTrue(!BikeNightBullies.caught(b,p[0])&&p[0].getHealth()==p[0].getMaxHealth(),"and the reader has not been caught"))
            .thenSucceed();
    }
    @AfterBatch(batch="sand_close") public static void sandCloseDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.484: the cars
    /** How far along a way round the place nearest somewhere is, and how far off it that is. */
    private static double[] nearestAlong(BlockPos b,int round,Vec3 at){
        double best=0,least=Double.MAX_VALUE;
        for(double d=0;d<CalderTraffic.length(round);d+=.5){double q=CalderTraffic.point(b,round,d).distanceTo(new Vec3(at.x,b.getY(),at.z));if(q<least){least=q;best=d;}}
        return new double[]{best,least};
    }
    private static List<TownCar> cars(ServerLevel l,LiteraryTests.Fixture f){return l.getEntitiesOfClass(TownCar.class,whole(f));}

    @GameTest(template="empty",batch="traffic_rounds",timeoutTicks=9000)
    public static void theCarsRoundsKeepToTheRoadAndEachWayRoundHasItsOwnLane(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final int[] kerb={0},samples={0};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                h.assertTrue(CalderTraffic.rounds()==4&&TownCar.MOVING==4,"two rounds, each driven both ways, and a car for each way");
                for(int r=0;r<CalderTraffic.rounds();r++){
                    double length=CalderTraffic.length(r);
                    h.assertTrue(length>230&&length<340&&CalderTraffic.point(b,r,0).distanceTo(CalderTraffic.point(b,r,length))<1e-6,"a way round comes back to where it began, a few hundred blocks on: "+length);
                    for(double d=0;d<length;d+=.5){
                        var at=CalderTraffic.point(b,r,d);float yaw=CalderTraffic.yaw(b,r,d);double rad=Math.toRadians(yaw);
                        // The middle of the car is on the road, and both its sides are on the road or on the kerb, which is level with it
                        // (where a street slants, a car's outer wheels run along the kerb's edge). A body the size of one touches nothing there.
                        for(double side:new double[]{-TownCar.WIDE,0,TownCar.WIDE}){
                            double x=at.x-Math.cos(rad)*side-b.getX(),z=at.z-Math.sin(rad)*side-b.getZ();int on=CalderTown.surface((int)Math.floor(x),(int)Math.floor(z));
                            h.assertTrue(on==CalderTown.ROAD||side!=0&&on==CalderTown.KERB,"way "+r+" is on the road all the way round: at "+d+", "+x+", "+z+" is "+on);
                            if(on==CalderTown.KERB)kerb[0]++;
                        }
                        samples[0]++;
                        h.assertTrue(l.noCollision(new AABB(at.x-.9,at.y+.2,at.z-.9,at.x+.9,at.y+1.4,at.z+.9)),"nothing stands in way "+r+": at "+d+", "+at.subtract(Vec3.atLowerCornerOf(b)));
                        h.assertTrue(at.z-b.getZ()>-135,"no car goes up to Church Lane: way "+r+" at "+d);
                    }
                }
                h.assertTrue(kerb[0]*20<samples[0],"a car's side is over the kerb in few places: "+kerb[0]+" of "+samples[0]);
                // The two ways round a round are two lanes.
                for(int r=0;r<4;r+=2)for(double d=0;d<CalderTraffic.length(r);d+=1){
                    double off=nearestAlong(b,r+1,CalderTraffic.point(b,r,d))[1];
                    h.assertTrue(off>2.2,"the two ways round are not in each other's lane: ways "+r+" and "+(r+1)+" are "+off+" apart at "+d);
                }
                // A reader, for a night to ask where its cars start.
                f.player("traffic_rounds_reader",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b),"waiting for the night"))
            .thenExecute(()->{
                var gate=Vec3.atLowerCornerOf(b).add(-.5,0,BikeNightSite.NORTH);
                for(int i=0;i<TownCar.MOVING;i++){
                    double far=CalderTraffic.point(b,CalderTraffic.roundOf(i),CalderTraffic.along(b,i)).distanceTo(gate);
                    h.assertTrue(far>30,"no car starts by the park's gate: the "+TownCar.NAMES[i]+" is "+far+" from it");
                }
                h.assertTrue(!CalderTraffic.going(b)&&cars(l,f).isEmpty(),"and with no chase there are no cars yet");
            }).thenSucceed();
    }
    @AfterBatch(batch="traffic_rounds") public static void trafficRoundsDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="traffic_drive",timeoutTicks=9000)
    public static void fourCarsDriveTheirWaysRoundWithTheirLightsOn(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final double[] from=new double[4];final TownCar[] body=new TownCar[4];
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("traffic_drive_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            // The reader stands on the pavement of Main Street, and the cars are started.
            .thenExecute(()->{f.at(p[0],P,-20.5,0,-86.5);hush(p[0]);CalderTraffic.startNow(b);for(int i=0;i<4;i++)from[i]=CalderTraffic.along(b,i);})
            .thenWaitUntil(()->{for(int i=0;i<4;i++)body[i]=CalderTraffic.body(l,b,i);h.assertTrue(body[0]!=null&&body[1]!=null&&body[2]!=null&&body[3]!=null,"waiting for each car to have a body");})
            .thenIdle(300)
            .thenExecute(()->{
                h.assertTrue(cars(l,f).stream().filter(c->c.kind()<TownCar.MOVING).count()==4,"four cars on the streets, and no more: "+cars(l,f).size());
                var origin=Vec3.atLowerCornerOf(b);
                for(int i=0;i<4;i++){
                    int r=CalderTraffic.roundOf(i);double length=CalderTraffic.length(r),gone=(CalderTraffic.along(b,i)-from[i]+length)%length;
                    h.assertTrue(body[i].isAlive()&&body[i].kind()==i&&CalderTraffic.body(l,b,i)==body[i],"the "+TownCar.NAMES[i]+" has the body it had");
                    h.assertTrue(gone>70&&gone<110,"it has driven on at a car's pace: "+gone+" blocks in fifteen seconds");
                    h.assertTrue(body[i].position().distanceTo(CalderTraffic.point(b,r,CalderTraffic.along(b,i)))<.6,"its body is where it is");
                    var at=body[i].position().subtract(origin);
                    h.assertTrue(CalderTown.surface((int)Math.floor(at.x),(int)Math.floor(at.z))==CalderTown.ROAD,"on the road: "+at);
                    // It points the way it is going, and its light is on the road ahead of its nose.
                    double rad=Math.toRadians(body[i].getYRot());var ahead=CalderTraffic.point(b,r,CalderTraffic.along(b,i)+3).subtract(body[i].position());
                    h.assertTrue((-Math.sin(rad)*ahead.x+Math.cos(rad)*ahead.z)/ahead.length()>.9,"it points the way it is going");
                    var light=CalderTraffic.light(b,i);
                    h.assertTrue(light!=null&&l.getBlockState(light).is(BikeNightRegistry.BEAM.get())&&l.getBlockState(light).getLightEmission(l,light)>=12,"its light is lit: "+light);
                    var to=Vec3.atBottomCenterOf(light).subtract(body[i].position());
                    h.assertTrue(to.length()>TownCar.LONG&&to.length()<TownCar.LONG+4&&-Math.sin(rad)*to.x+Math.cos(rad)*to.z>0,"ahead of its nose: "+to.length());
                }
                h.assertTrue(sounded(p[0],"town.engine"),"a car going by is heard");
                h.assertTrue(p[0].getHealth()==p[0].getMaxHealth(),"and a reader on the pavement is left alone");
            }).thenSucceed();
    }
    @AfterBatch(batch="traffic_drive") public static void trafficDriveDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="traffic_stop",timeoutTicks=9000)
    public static void aCarStopsShortOfSomebodyInItsLaneSoundsItsHornAndGoesOnWhenTheyHaveGone(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final TownCar[] body={null};final boolean[] under={false};final long[] stopped={-1};
        int car=TownCar.WAGON,round=CalderTraffic.roundOf(car);double start=150;
        Runnable watch=()->{body[0]=CalderTraffic.body(l,b,car);if(body[0]!=null&&body[0].over(p[0].position(),0))under[0]=true;};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("traffic_stop_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            // The station wagon is on Main Street, and the reader stands in its lane sixteen blocks ahead of it.
            .thenExecute(()->{
                CalderTraffic.startNow(b);CalderTraffic.put(b,car,start);
                var at=CalderTraffic.point(b,round,start+16).subtract(Vec3.atLowerCornerOf(b));f.at(p[0],P,at.x,0,at.z);hush(p[0]);
            })
            .thenWaitUntil(()->{watch.run();
                if(body[0]!=null&&CalderTraffic.speed(b,car)==0&&CalderTraffic.along(b,car)>start+3&&stopped[0]<0)stopped[0]=l.getGameTime();
                h.assertTrue(stopped[0]>=0,"waiting for it to come up to them and stop: at "+CalderTraffic.along(b,car)+" going "+CalderTraffic.speed(b,car));})
            .thenExecute(()->{
                double nose=body[0].position().distanceTo(p[0].position())-TownCar.LONG;
                h.assertTrue(nose>=.9&&nose<3,"it stops short of them: its nose is "+nose+" off");
                h.assertTrue(!under[0],"and it was never over them");
                h.assertTrue(sounded(p[0],"town.horn_wagon"),"they have had its horn");
                h.assertTrue(!body[0].saying(),"its driver has not said anything yet");
            })
            // They stay where they are: after a while the driver says so.
            .thenWaitUntil(()->{watch.run();h.assertTrue(body[0].saying(),"waiting for the driver to say something");})
            .thenExecute(()->{
                h.assertTrue(body[0].getCustomName()!=null&&body[0].getCustomName().getString().equals(BikeNightTexts.GET_OFF[car])&&body[0].isCustomNameVisible(),"what the driver calls: "+body[0].getCustomName());
                h.assertTrue(l.getGameTime()-stopped[0]>=CalderTraffic.WAITED-30,"not at once: "+(l.getGameTime()-stopped[0])+" ticks after it stopped");
                h.assertTrue(CalderTraffic.speed(b,car)==0&&!under[0],"and it has not moved");
                // Off the road, onto the pavement.
                var at=p[0].position().subtract(Vec3.atLowerCornerOf(b));f.at(p[0],P,at.x,0,at.z+6);
            })
            .thenWaitUntil(()->{watch.run();h.assertTrue(CalderTraffic.speed(b,car)>.3F&&CalderTraffic.along(b,car)>start+24,"waiting for it to go on past where they were: at "+CalderTraffic.along(b,car)+" going "+CalderTraffic.speed(b,car));})
            .thenExecute(()->{
                h.assertTrue(!heard(p[0],BikeNightTexts.GET_OFF[car]),"what the driver called was never in chat");
                h.assertTrue(p[0].getHealth()==p[0].getMaxHealth(),"and nobody is hurt");
            }).thenSucceed();
    }
    @AfterBatch(batch="traffic_stop") public static void trafficStopDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="traffic_crash",timeoutTicks=9000)
    public static void aRiderWhoRidesIntoACarComesOffAndOneGoingSlowlyDoesNot(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final NightBike[] mine={null};final TownCar[] body={null};final int[] t={0};
        int car=TownCar.SEDAN,round=CalderTraffic.roundOf(car);final double[] hold={0};final Vec3[] at={null};
        // The sedan is held where it is, on Main Street, for as long as the case wants it there.
        Runnable keep=()->{CalderTraffic.put(b,car,hold[0]);body[0]=CalderTraffic.body(l,b,car);};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("traffic_crash_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            .thenExecute(()->{
                mine[0]=take(h,f,p[0],NightBike.Make.WASP);CalderTraffic.startNow(b);
                hold[0]=nearestAlong(b,round,Vec3.atLowerCornerOf(b).add(-10.5,0,-92.5))[0];at[0]=CalderTraffic.point(b,round,hold[0]);keep.run();
            })
            .thenWaitUntil(()->{keep.run();h.assertTrue(body[0]!=null&&f.own(p[0],P).getString("Bike").equals("wasp"),"waiting for the sedan's body, and for the Wasp to be theirs");})
            // Slowly, at its side from the south: nothing comes of it, and the car stays where it is while they are under it.
            .thenExecute(()->{mine[0].moveTo(at[0].x,at[0].y,at[0].z+1.1,180,0);p[0].setYRot(180);})
            .thenExecuteFor(30,()->{keep.run();})
            .thenExecute(()->{
                h.assertTrue(body[0].over(mine[0].position(),.25)&&p[0].getVehicle()==mine[0]&&mine[0].speedNow()<=RiddenBike.CRASH,"against its side, going slowly, the rider is still on: "+mine[0].speedNow());
                h.assertTrue(CalderTraffic.speed(b,car)==0,"and the car does not move with somebody under it");
                // From eight blocks off, at a run.
                mine[0].moveTo(at[0].x,at[0].y,at[0].z+9,180,0);p[0].setYRot(180);hush(p[0]);t[0]=0;
            })
            .thenWaitUntil(()->{
                keep.run();
                if(p[0].getVehicle()==mine[0]){p[0].setYRot(180);pedalWell(mine[0],t[0]++,3);}
                h.assertTrue(p[0].getVehicle()==null,"waiting for them to ride into it and come off: "+(mine[0].isAlive()?mine[0].position().subtract(at[0])+" going "+mine[0].speedNow():"the bike is gone"));
            })
            .thenWaitUntil(()->{keep.run();h.assertTrue(heard(p[0],NightBikes.HEAD_ON),"waiting for them to be told");})
            .thenExecute(()->{
                var lying=new ArrayList<BlockPos>();
                for(var cell:BlockPos.betweenClosed(BlockPos.containing(at[0]).offset(-4,-1,-4),BlockPos.containing(at[0]).offset(4,1,4)))if(l.getBlockState(cell).getBlock() instanceof NightBikeBlock&&l.getBlockState(cell).getValue(NightBikeBlock.LYING))lying.add(cell.immutable());
                h.assertTrue(lying.size()==1,"the Wasp lies in the road by the car: "+lying);
                h.assertTrue(body[0].isAlive()&&body[0].position().distanceTo(at[0])<.5,"and the car is where it was");
                h.assertTrue(p[0].getHealth()>=p[0].getMaxHealth()-1,"they are no more hurt than by a wall: "+p[0].getHealth());
            }).thenSucceed();
    }
    @AfterBatch(batch="traffic_crash") public static void trafficCrashDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="traffic_told",timeoutTicks=12000)
    public static void aDriverTellsOneOfTheThreeOffAndHeStopsForItAndTheirBikesGoRoundACar(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who={null,null,null};final PackBike[] bikes={null,null,null};
        final int[] toldWho={-1,-1},car={-1,-1};final long[] toldAt={-1,-1};final boolean[] answered={false,false},inside={false};final Vec3[] stoodAt={null,null};
        // Every tick: none of the three's bikes is inside a car.
        Runnable watch=()->{three(l,b,who,bikes);for(var body:cars(l,f))for(var bike:bikes)if(bike!=null&&body.over(bike.position(),-.15))inside[0]=true;};
        // A car is put a little behind one of them who is riding in the town beside its way, going at its pace: its driver comes by him.
        java.util.function.IntPredicate comeBy=slot->{
            for(int i=0;i<3;i++){
                if(bikes[i]==null||who[i]==null||who[i].gesture()==BikeNightBullies.COUGH||i==toldWho[0]||bikes[i].speedNow()<.1F||bikes[i].getZ()-b.getZ()>-60)continue;
                for(int c=0;c<TownCar.MOVING;c++){
                    if(c==car[0])continue;
                    var near=nearestAlong(b,CalderTraffic.roundOf(c),bikes[i].position());
                    if(near[1]>1.5&&near[1]<3){CalderTraffic.put(b,c,near[0]-3.5,CalderTraffic.CRUISE);toldWho[slot]=i;car[slot]=c;toldAt[slot]=l.getGameTime();stoodAt[slot]=bikes[i].position();return true;}
                }
            }
            return false;
        };
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("traffic_told_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            .thenExecute(()->f.at(p[0],P,-14.5,0,-20.5))
            .thenWaitUntil(()->{watch.run();h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE,"waiting for the chase: stage "+BikeNightBullies.stage(b));})
            // The reader is a long way off, in the turning circle of Elm Court, and the three have the town to cross.
            .thenExecute(()->{h.assertTrue(CalderTraffic.going(b),"the cars start with the chase");f.at(p[0],P,22.5,0,-141.5);})
            .thenWaitUntil(()->{watch.run();h.assertTrue(toldWho[0]>=0||comeBy.test(0),"waiting for one of the three to ride beside a car's way");})
            .thenWaitUntil(()->{watch.run();h.assertTrue(CalderTraffic.told(b)==1,"waiting for the driver to tell him off: "+CalderTraffic.told(b));})
            .thenExecuteFor(CalderTraffic.REPLY+6,()->{watch.run();if(saying(who[toldWho[0]]).equals(BikeNightTexts.TOLD_BACK[toldWho[0]]))answered[0]=true;})
            .thenExecute(()->{
                var body=CalderTraffic.body(l,b,car[0]);
                h.assertTrue(body!=null&&body.getCustomName()!=null&&body.getCustomName().getString().equals(BikeNightTexts.TOLD_OFF[toldWho[0]]),"the driver said what they think of him: "+(body==null?null:body.getCustomName()));
                h.assertTrue(answered[0],"and he answered back: "+saying(who[toldWho[0]]));
                h.assertTrue(bikes[toldWho[0]].speedNow()<.02F,"he has stopped for it: "+bikes[toldWho[0]].speedNow());
                h.assertTrue(CalderTraffic.speed(b,car[0])<=CalderTraffic.CRAWL+.01F,"and the car crawls beside him: "+CalderTraffic.speed(b,car[0]));
            })
            // He is off again when it is over, and a second driver tells another of them off.
            .thenWaitUntil(()->{watch.run();h.assertTrue(bikes[toldWho[0]]!=null&&bikes[toldWho[0]].speedNow()>.15F,"waiting for him to ride on");})
            .thenExecute(()->h.assertTrue(l.getGameTime()-toldAt[0]>=CalderTraffic.TOLD-2,"not before it was over: "+(l.getGameTime()-toldAt[0])+" ticks"))
            .thenWaitUntil(()->{watch.run();h.assertTrue(toldWho[1]>=0||comeBy.test(1),"waiting for another of them to ride beside a car's way");})
            .thenWaitUntil(()->{watch.run();h.assertTrue(CalderTraffic.told(b)==2,"waiting for a second driver to tell another of them off: "+CalderTraffic.told(b));})
            // A car that is where one of their bikes is: the bike is put to one side of it.
            .thenExecute(()->{
                int c=0;while(c==car[0]||c==car[1])c++;
                var near=nearestAlong(b,CalderTraffic.roundOf(c),bikes[toldWho[1]].position());
                CalderTraffic.put(b,c,near[0]);bikes[toldWho[1]].setPos(CalderTraffic.point(b,CalderTraffic.roundOf(c),near[0]));car[0]=c;
            })
            .thenIdle(3)
            .thenExecute(()->{
                var body=CalderTraffic.body(l,b,car[0]);
                h.assertTrue(body!=null&&!body.over(bikes[toldWho[1]].position(),.15),"a bike a car is put on top of is beside it a moment later: "+bikes[toldWho[1]].position().subtract(body.position()));
                h.assertTrue(CalderTraffic.told(b)==2,"and no third driver has anything to say: "+CalderTraffic.told(b));
                h.assertTrue(!inside[0],"none of their bikes was ever inside a car");
                for(var line:BikeNightTexts.TOLD_OFF)h.assertTrue(!heard(p[0],line),"what a driver says is never in chat");
            }).thenSucceed();
    }
    @AfterBatch(batch="traffic_told") public static void trafficToldDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.485: the people on the pavements
    private static boolean people(ServerLevel l,BlockPos b,LiteraryActor[] who){
        boolean every=true;for(int i=0;i<who.length;i++){who[i]=CalderPeople.find(l,b,i);every&=who[i]!=null;}return every;
    }
    private static Vec3 rel(BlockPos b,net.minecraft.world.entity.Entity e){return e.position().subtract(Vec3.atLowerCornerOf(b));}

    @GameTest(template="empty",batch="people_cast",timeoutTicks=3000)
    public static void thePeopleOfTheTownEachHaveASkinAndAPlaceOfTheirOwn(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who=new LiteraryActor[CalderPeople.COUNT];
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("people_cast_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            .thenIdle(30)
            .thenExecute(()->{
                people(l,b,who);
                h.assertTrue(java.util.Arrays.stream(who).allMatch(java.util.Objects::isNull)&&!CalderTraffic.going(b)&&l.getEntitiesOfClass(LiteraryActor.class,whole(f),CalderPeople::is).isEmpty(),"before the gate is opened there is nobody on the pavements");
                CalderTraffic.startNow(b);
            })
            .thenWaitUntil(()->h.assertTrue(people(l,b,who)&&CalderTraffic.parked(l,b,0)!=null,"waiting for all of them, and the cars four of them sit on"))
            .thenIdle(12)
            .thenExecute(()->{
                var skins=new java.util.HashSet<String>();
                for(int i=0;i<who.length;i++){
                    var a=who[i];int phase=a.phase();
                    h.assertTrue(a.role()==LiteraryActor.STRANGER&&phase==CalderPeople.FIRST+i&&CalderPeople.is(a)&&!BikeNightGus.is(a)&&!BikeNightBullies.is(a),"who "+i+" is: "+phase);
                    h.assertTrue(skins.add(CalderPeople.skin(phase))&&CalderPeople.class.getResource("/assets/the_oldest_house/textures/entity/literary_"+CalderPeople.skin(phase)+".png")!=null,"each has a painted skin of their own: "+CalderPeople.skin(phase));
                    double size=a.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE).getBaseValue();
                    h.assertTrue(i<CalderPeople.TRICK?size<.6:size>.75&&size<.85,"the trick-or-treaters are children and the rest are teenagers: "+i+" is "+size);
                    h.assertTrue(a.ownGestures()&&!a.reaching()&&!a.crawling(),"their gestures are their own");
                    h.assertTrue(a.getMainHandItem().is(BikeNightRegistry.TREAT_SACK.get())==(i<CalderPeople.TRICK),"a pillowcase in a trick-or-treater's hand and in nobody else's: "+i);
                    h.assertTrue(a.scene().equals(P.id()),"they belong to the night");
                }
                h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,whole(f),CalderPeople::is).size()==CalderPeople.COUNT,"and there is one of each");
                // The line, down the pavement in front of the theater, each looking at the back of the next.
                for(int i=0;i<CalderPeople.LINE;i++){
                    var a=who[CalderPeople.LINE_FIRST+i];var spot=CalderPeople.lineSpot(i);var r=rel(b,a);
                    h.assertTrue(Math.hypot(r.x-spot.x,r.z-spot.z)<.2&&Math.abs(r.y)<.05&&Math.abs(net.minecraft.util.Mth.wrapDegrees(a.getYRot()-CalderPeople.LINE_LOOK))<5,"one of the line in their place: "+r+" looking "+a.getYRot());
                    for(var at:new Vec3[]{spot,CalderPeople.asideSpot(i)})h.assertTrue(CalderTown.surface(net.minecraft.util.Mth.floor(at.x),net.minecraft.util.Mth.floor(at.z))!=CalderTown.ROAD,"which is not in the road, nor is where they step aside to: "+at);
                    h.assertTrue(CalderTown.surface(net.minecraft.util.Mth.floor(spot.x),net.minecraft.util.Mth.floor(spot.z))==CalderTown.PAVEMENT,"the line is on the pavement");
                }
                // Two on the hood of each parked car: over it, sitting on its top, looking the way it does.
                h.assertTrue(CalderTraffic.parked(l,b,0).kind()==TownCar.BLUE&&CalderTraffic.parked(l,b,1)!=null&&CalderTraffic.parked(l,b,1).kind()==TownCar.WHITE,"two cars are parked in the burger place's lot");
                for(int i=CalderPeople.HOOD_FIRST;i<CalderPeople.HOOD_FIRST+CalderPeople.HOOD;i++){
                    var a=who[i];var car=CalderTraffic.parked(l,b,(i-CalderPeople.HOOD_FIRST)/2);
                    h.assertTrue(car.over(a.position(),0)&&Math.abs(a.getY()-(b.getY()+CalderPeople.HOOD_TOP-CalderPeople.HIPS))<.01,"on the hood: "+a.position().subtract(car.position()));
                    h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(a.getYRot()-car.getYRot()))<1,"looking the way the car does");
                }
                // Each door of the court's round is a house's door, and the trick-or-treaters stop on its step.
                int doors=0;
                for(var stop:CalderPeople.ROUTES[0]){
                    if(!stop.stops())continue;doors++;
                    var at=BlockPos.containing(b.getX()+stop.x(),b.getY()+.5,b.getZ()+stop.z());var state=l.getBlockState(at);
                    h.assertTrue(state.is(BikeNightRegistry.SIGN.get())&&CalderSignBlock.sign(state)==CalderSigns.Sign.HOUSE_DOOR,"a stop of the court's round is at a house's door: "+at.subtract(b)+" is "+state);
                }
                h.assertTrue(doors==4,"four doors: "+doors);
                // How they talk: short, ended, and nothing of what a reader is.
                var lines=BikeNightTexts.peopleLines();
                h.assertTrue(new java.util.HashSet<>(lines).size()>=lines.size()-1&&lines.contains("Go, kid, go!")&&lines.contains("Leave 'em alone, Tully!"),"the proposal's two calls are among them");
                var barred=java.util.regex.Pattern.compile("\\b(he|she|him|his|her|hers|boy|girl|man|woman|son|daughter|sir|miss|stupid|idiot|fat|ugly|kill|die|hurt)\\b");
                for(var line:lines){
                    h.assertTrue(line.length()<=60&&(line.endsWith(".")||line.endsWith("!")||line.endsWith("?")),"a line of theirs is short, and ends: \""+line+"\"");
                    h.assertTrue(!barred.matcher(line.toLowerCase(java.util.Locale.ROOT)).find(),"and says nothing of what a reader is: \""+line+"\"");
                }
                // A hand on one of the line, and on one of the two on the hood.
                f.at(p[0],P,CalderPeople.lineSpot(2).x,0,CalderPeople.KERB_Z);
                LiteraryVignettes.talk(p[0],who[CalderPeople.LINE_FIRST+2]);
                h.assertTrue(List.of(BikeNightTexts.PEOPLE_TALK[2]).contains(saying(who[CalderPeople.LINE_FIRST+2])),"a hand on one of the line gets a word: "+saying(who[CalderPeople.LINE_FIRST+2]));
                h.assertTrue(who[CalderPeople.LINE_FIRST+2].getCustomName()!=null&&who[CalderPeople.LINE_FIRST+2].isCustomNameVisible(),"over their head");
            })
            .thenIdle(3)
            .thenExecute(()->{
                for(var line:BikeNightTexts.peopleLines())h.assertTrue(!heard(p[0],line),"what they say is never in chat: "+line);
                // The night over: none of them is anywhere.
                BikeNight.resetNow(l,b);
                h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,whole(f),CalderPeople::is).isEmpty(),"the night over, none of them is anywhere");
            }).thenSucceed();
    }
    @AfterBatch(batch="people_cast") public static void peopleCastDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="people_rounds",timeoutTicks=9000)
    public static void trickOrTreatersGoFromDoorToDoorRoundTheCourtAndAlongMainStreet(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who=new LiteraryActor[CalderPeople.TRICK];
        final var stood=new java.util.TreeSet<String>();final var spoke=new java.util.TreeSet<String>();final var speakers=new java.util.TreeSet<Integer>();
        final int[] laps={0,0},last={1,1};final double[] apart={0},high={0};final boolean[] road={false};
        Runnable watch=()->{
            people(l,b,who);
            for(int pair=0;pair<2;pair++){
                var a=who[pair*2];var c=who[pair*2+1];if(a==null||c==null)continue;
                int next=CalderPeople.next(b,pair);if(next<last[pair])laps[pair]++;last[pair]=next;
                apart[0]=Math.max(apart[0],a.distanceTo(c));
                for(var body:new LiteraryActor[]{a,c}){
                    high[0]=Math.max(high[0],Math.abs(body.getY()-b.getY()));
                    // Main Street's pair keep to its pavement and kerb.
                    if(pair==1&&CalderTown.surface(net.minecraft.util.Mth.floor(body.getX()-b.getX()),net.minecraft.util.Mth.floor(body.getZ()-b.getZ()))==CalderTown.ROAD)road[0]=true;
                }
                if(!CalderPeople.standing(b,pair))continue;
                var stop=CalderPeople.ROUTES[pair][next];var r=rel(b,a);
                if(Math.hypot(r.x-stop.x(),r.z-stop.z())<.3&&Math.abs(net.minecraft.util.Mth.wrapDegrees(a.getYRot()-stop.look()))<20)stood.add(pair+":"+next);
                for(int i=0;i<2;i++)if(saying(who[pair*2+i]).equals(BikeNightTexts.AT_A_STOP[stop.say()])){spoke.add(pair+":"+next);speakers.add(pair*2+i);}
            }
        };
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("people_rounds_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            // They go at a run for this, and stand a second and a half at each stop.
            .thenExecute(()->{CalderTraffic.startNow(b);CalderPeople.hurry(b,.3,30);f.at(p[0],P,22.5,0,-141.5);})
            .thenWaitUntil(()->h.assertTrue(people(l,b,who),"waiting for the four of them"))
            .thenExecute(()->{
                for(int pair=0;pair<2;pair++){
                    var first=CalderPeople.ROUTES[pair][0];var r=rel(b,who[pair*2]);
                    h.assertTrue(Math.hypot(r.x-first.x(),r.z-first.z())<.5&&who[pair*2].distanceTo(who[pair*2+1])<1.2,"a pair begins at the first place of its round, one behind the other: "+r);
                }
            })
            .thenWaitUntil(()->{watch.run();h.assertTrue(laps[0]>=1&&laps[1]>=1,"waiting for both pairs to go once round: the court's is making for "+CalderPeople.next(b,0)+" at "+(who[0]==null?null:rel(b,who[0]))+", Main Street's for "+CalderPeople.next(b,1)+" at "+(who[2]==null?null:rel(b,who[2]))+"; stood at "+stood);})
            .thenExecute(()->{
                h.assertTrue(stood.equals(new java.util.TreeSet<>(List.of("0:2","0:5","0:8","0:11","1:1","1:3"))),"the first of each pair stood at each stop of its round, looking at it: "+stood);
                h.assertTrue(spoke.equals(stood),"and at each of them one of the pair said what is said there: "+spoke);
                h.assertTrue(speakers.size()==4,"they take it in turns: "+speakers);
                h.assertTrue(apart[0]<3.5,"the second of a pair is never far behind the first: "+apart[0]);
                h.assertTrue(high[0]<.6,"and they stay on the ground: "+high[0]);
                h.assertTrue(!road[0],"Main Street's pair keep out of the road");
                h.assertTrue(CalderPeople.stops(b,0)>=4&&CalderPeople.stops(b,1)>=2,"four doors and two windows: "+CalderPeople.stops(b,0)+", "+CalderPeople.stops(b,1));
                for(var line:BikeNightTexts.AT_A_STOP)h.assertTrue(!heard(p[0],line),"what they say is never in chat: "+line);
            }).thenSucceed();
    }
    @AfterBatch(batch="people_rounds") public static void peopleRoundsDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="people_line",timeoutTicks=9000)
    public static void theyCheerARiderGoingByAndTheLinePartsForOneOnThePavementAndIsBackBehindThem(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who=new LiteraryActor[CalderPeople.COUNT];
        final NightBike[] mine={null};final int[] t={0};final double[] nearest={9},kept={9};final boolean[] parted={false,false},cheered={false,false},called={false};final long[] at={-1,-1};
        // Riding east along a row, pedalling.
        Runnable ride=()->{if(p[0].getVehicle()==mine[0]){p[0].setYRot(270);pedalWell(mine[0],t[0]++,3);}};
        Runnable watch=()->{
            people(l,b,who);
            for(int i=0;i<CalderPeople.LINE;i++){var a=who[CalderPeople.LINE_FIRST+i];if(a!=null)nearest[0]=Math.min(nearest[0],Math.sqrt(a.distanceToSqr(mine[0].getX(),a.getY(),mine[0].getZ())));}
        };
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("people_line_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.WASP);CalderTraffic.startNow(b);})
            .thenWaitUntil(()->h.assertTrue(people(l,b,who)&&f.own(p[0],P).getString("Bike").equals("wasp"),"waiting for the twelve of them, and for the Wasp to be theirs"))
            // Standing on a bike in the road in front of the line: nobody cheers somebody who is not going anywhere.
            .thenExecute(()->{mine[0].moveTo(b.getX()-14.5,b.getY(),b.getZ()-90.5,270,0);p[0].setYRot(270);})
            .thenExecuteFor(30,()->{watch.run();for(int i=CalderPeople.LINE_FIRST;i<CalderPeople.HOOD_FIRST;i++)if(who[i].gesture()!=0||who[i].saying())called[0]=true;if(CalderPeople.parted(b))parted[0]=true;})
            .thenExecute(()->{
                h.assertTrue(!called[0],"nobody cheers a rider who is standing still");
                h.assertTrue(!parted[0],"and the line does not part for somebody in the road");
                // Along the road past the line, from west of it.
                mine[0].moveTo(b.getX()-27.5,b.getY(),b.getZ()-89.5,270,0);p[0].setYRot(270);t[0]=0;
            })
            .thenWaitUntil(()->{ride.run();watch.run();if(CalderPeople.parted(b))parted[0]=true;
                boolean arms=true,one=false;for(int i=CalderPeople.LINE_FIRST;i<CalderPeople.HOOD_FIRST;i++){arms&=who[i].gesture()==CalderPeople.CHEER;one|=List.of(BikeNightTexts.CHEERS[2]).contains(saying(who[i]));}
                if(arms&&one&&at[0]<0)at[0]=l.getGameTime();
                h.assertTrue(at[0]>=0,"waiting for the line to cheer them going by: the Wasp at "+rel(b,mine[0])+" going "+mine[0].speedNow());})
            .thenExecute(()->{
                h.assertTrue(!parted[0],"they cheer a rider in the road, and stay where they are");
                h.assertTrue(rel(b,mine[0]).x>CalderPeople.LINE_WEST-CalderPeople.CHEER_WITHIN[2]-1,"not before they were near: "+rel(b,mine[0]));
            })
            .thenExecuteFor(CalderPeople.ARMS+20,()->{ride.run();watch.run();})
            .thenExecute(()->{
                for(int i=CalderPeople.LINE_FIRST;i<CalderPeople.HOOD_FIRST;i++)h.assertTrue(who[i].gesture()==0,"their arms come down again");
                // Now along the pavement, through where the line is. (From this side of the lamp that stands at the kerb.)
                nearest[0]=9;mine[0].moveTo(b.getX()-23.2,b.getY(),b.getZ()-85,270,0);p[0].setYRot(270);t[0]=0;
            })
            .thenWaitUntil(()->{ride.run();watch.run();
                var r=rel(b,mine[0]);
                if(CalderPeople.parted(b)&&at[1]<0)at[1]=l.getGameTime();
                if(r.x>CalderPeople.LINE_WEST-1&&r.x<CalderPeople.LINE_WEST+(CalderPeople.LINE-1)*CalderPeople.LINE_GAP+1)kept[0]=Math.min(kept[0],nearest[0]);
                h.assertTrue(r.x>CalderPeople.LINE_WEST+(CalderPeople.LINE-1)*CalderPeople.LINE_GAP+CalderPeople.PART_WITHIN+1,"waiting for them to ride the length of the line and on: at "+r+" going "+mine[0].speedNow());})
            .thenExecute(()->{
                h.assertTrue(at[1]>=0,"the line parted for them");
                h.assertTrue(kept[0]>=.55&&kept[0]<2,"and none of it was in their way as they went through: the nearest was "+kept[0]+" off");
                h.assertTrue(Math.abs(rel(b,mine[0]).z+85)<.6&&p[0].getVehicle()==mine[0],"they rode straight through, and are still on: "+rel(b,mine[0]));
                h.assertTrue(!CalderPeople.parted(b),"and with them gone by it is no longer parted");
            })
            .thenIdle(25)
            .thenExecute(()->{
                people(l,b,who);
                for(int i=0;i<CalderPeople.LINE;i++){
                    var a=who[CalderPeople.LINE_FIRST+i];var spot=CalderPeople.lineSpot(i);var r=rel(b,a);
                    h.assertTrue(Math.hypot(r.x-spot.x,r.z-spot.z)<.2&&Math.abs(net.minecraft.util.Mth.wrapDegrees(a.getYRot()-CalderPeople.LINE_LOOK))<5,"each of the line is back in their place: "+r);
                }
                // And by the burger place's lot, along Park Street: the two on the hood.
                mine[0].moveTo(b.getX()+1.5,b.getY(),b.getZ()-54.5,270,0);p[0].setYRot(270);t[0]=0;hush(p[0]);
            })
            .thenWaitUntil(()->{ride.run();people(l,b,who);
                boolean arms=who[CalderPeople.HOOD_FIRST].gesture()==CalderPeople.CHEER&&who[CalderPeople.HOOD_FIRST+1].gesture()==CalderPeople.CHEER;
                boolean one=List.of(BikeNightTexts.CHEERS[3]).contains(saying(who[CalderPeople.HOOD_FIRST]))||List.of(BikeNightTexts.CHEERS[3]).contains(saying(who[CalderPeople.HOOD_FIRST+1]));
                h.assertTrue(arms&&one,"waiting for the two on the hood to cheer them going by: the Wasp at "+rel(b,mine[0]));})
            .thenExecute(()->{
                for(int i=CalderPeople.HOOD_FIRST;i<CalderPeople.HOOD_FIRST+CalderPeople.HOOD;i++)h.assertTrue(CalderTraffic.parked(l,b,(i-CalderPeople.HOOD_FIRST)/2).over(who[i].position(),0)&&who[i].gesture()==CalderPeople.CHEER,"all four of them, and they are still sitting on their cars");
                for(var lot:BikeNightTexts.CHEERS)for(var line:lot)h.assertTrue(!heard(p[0],line),"what they call is never in chat: "+line);
            }).thenSucceed();
    }
    @AfterBatch(batch="people_line") public static void peopleLineDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="people_boo",timeoutTicks=12000)
    public static void theyBooTheThreeAndTheLineStopsOneOfThemWhoRidesIntoItAndWalksHimOutIntoTheRoad(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who={null,null,null};final PackBike[] bikes={null,null,null};
        final LiteraryActor[] them=new LiteraryActor[CalderPeople.COUNT];final boolean[] booed={false},told={false},answered={false},moved={false};final long[] at={-1};final int[] teller={-1};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("people_boo_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            .thenExecute(()->f.at(p[0],P,-14.5,0,-20.5))
            .thenWaitUntil(()->h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE,"waiting for the chase: stage "+BikeNightBullies.stage(b)))
            // The reader is a long way off, in the turning circle of Elm Court.
            .thenExecute(()->{h.assertTrue(CalderTraffic.going(b),"the people are out from when the gate is opened");f.at(p[0],P,22.5,0,-141.5);})
            .thenWaitUntil(()->h.assertTrue(people(l,b,them)&&three(l,b,who,bikes)&&bikes[0].getPassengers().contains(who[0])&&who[0].gesture()!=BikeNightBullies.COUGH,"waiting for the twelve of them, and Tully riding"))
            // Tully, in the road in front of the theater.
            .thenExecute(()->bikes[0].setPos(b.getX()-14.5,b.getY(),b.getZ()-90.5))
            .thenWaitUntil(()->{
                boolean hands=true,one=false;for(int i=CalderPeople.LINE_FIRST;i<CalderPeople.HOOD_FIRST;i++){hands&=them[i].gesture()==CalderPeople.BOO;one|=saying(them[i]).equals(BikeNightTexts.BOOS[0][0]);}
                h.assertTrue(hands&&one,"waiting for the line to boo him: Tully's bike at "+rel(b,bikes[0]));})
            .thenExecute(()->{
                h.assertTrue(BikeNightTexts.BOOS[0][0].equals("Leave 'em alone, Tully!"),"what the proposal has them call");
                h.assertTrue(!CalderPeople.held(b,0),"in the road he is not the line's to stop");
                h.assertTrue(three(l,b,who,bikes)&&bikes[1].getPassengers().contains(who[1]),"Ray-Ray is riding");
                // Ray-Ray, into the middle of the line.
                var spot=CalderPeople.lineSpot(2);bikes[1].setPos(b.getX()+spot.x+.6,b.getY(),b.getZ()+spot.z);at[0]=l.getGameTime();
            })
            .thenExecuteFor(CalderPeople.HELD-2,()->{
                three(l,b,who,bikes);
                if(CalderPeople.held(b,1))told[0]|=java.util.stream.IntStream.range(CalderPeople.LINE_FIRST,CalderPeople.HOOD_FIRST).anyMatch(i->saying(them[i]).equals(BikeNightTexts.NO_CUTTING[1]));
                if(l.getGameTime()-at[0]>6&&bikes[1].speedNow()>.05F)moved[0]=true;
            })
            .thenExecute(()->{
                h.assertTrue(told[0],"one of the line tells him there is no cutting");
                h.assertTrue(!moved[0],"and he is stopped: "+bikes[1].speedNow());
            })
            .thenWaitUntil(()->{three(l,b,who,bikes);if(saying(who[1]).equals(BikeNightTexts.CUT_BACK[1]))answered[0]=true;
                h.assertTrue(!CalderPeople.held(b,1)&&answered[0],"waiting for them to let him go, and for what he says to that: "+saying(who[1]));})
            .thenExecute(()->{
                var r=rel(b,bikes[1]);
                h.assertTrue(CalderTown.surface(net.minecraft.util.Mth.floor(r.x),net.minecraft.util.Mth.floor(r.z))!=CalderTown.PAVEMENT,"he has been walked out off the pavement: "+r);
                for(int i=0;i<CalderPeople.LINE;i++){var a=them[CalderPeople.LINE_FIRST+i];var spot=CalderPeople.lineSpot(i);var q=rel(b,a);h.assertTrue(Math.hypot(q.x-spot.x,q.z-spot.z)<.2,"and the line has not moved for him: "+q);}
                for(var lot:BikeNightTexts.BOOS)for(var line:lot)h.assertTrue(!heard(p[0],line),"what they call is never in chat: "+line);
                for(var line:BikeNightTexts.NO_CUTTING)h.assertTrue(!heard(p[0],line),"nor what the line says: "+line);
            }).thenSucceed();
    }
    @AfterBatch(batch="people_boo") public static void peopleBooDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.486: what changes in the town
    @GameTest(template="empty",batch="evening_signs",timeoutTicks=9000)
    public static void theBurgerPlacesBadLetterGoesOutAndComesBackAndTheGroceryClosesUpAisleByAisle(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};
        final boolean[] out={false},back={false},apart={false};final long[] wentDark={-1,-1,-1};final int[] order={0};
        Runnable watch=()->{
            boolean south=CalderTown.neonDim(l,b,false),north=CalderTown.neonDim(l,b,true);
            if(south!=north||south!=CalderEvening.dim(b))apart[0]=true;
            if(south)out[0]=true;else if(out[0])back[0]=true;
            for(int k=0;k<3;k++)if(wentDark[k]<0&&!CalderTown.aisleLit(l,b,k)){wentDark[k]=l.getGameTime();order[0]=order[0]*10+k+1;}
        };
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{p[0]=f.player("evening_grocery_reader",P);p[1]=f.player("evening_burger_reader",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0])&&BikeNight.here(p[1]),"waiting for the night"))
            // One reader in the Food Fair's lot, the other in the burger place's.
            .thenExecute(()->{f.at(p[0],P,-30.5,0,-104.5);f.at(p[1],P,25.5,0,-62.5);hush(p[0]);hush(p[1]);})
            .thenExecuteFor(60,watch)
            .thenExecute(()->{
                h.assertTrue(!out[0]&&order[0]==0&&CalderEvening.dark(b)==0,"until the town wakes, the sign is whole and the grocery is lit");
                for(int k=0;k<3;k++)h.assertTrue(CalderTown.aisleLit(l,b,k),"window "+k+" is lit");
                CalderTraffic.startNow(b);CalderEvening.closing(b,40);
            })
            .thenIdle(2)
            .thenExecute(()->CalderEvening.neonNow(b))
            .thenWaitUntil(()->{watch.run();h.assertTrue(back[0]&&CalderEvening.dark(b)==3,"waiting for the G to go out and come back, and for the grocery to be dark: "+CalderEvening.dark(b)+" aisles, out "+out[0]+", back "+back[0]);})
            .thenExecute(()->{
                h.assertTrue(!apart[0],"both of the burger place's signs go out and come back together");
                h.assertTrue(order[0]==123,"the aisles go dark from the west, one after another: "+order[0]);
                h.assertTrue(wentDark[1]-wentDark[0]>=CalderEvening.AISLE_GAP-2&&wentDark[2]-wentDark[1]>=CalderEvening.AISLE_GAP-2,"some seconds apart: "+(wentDark[1]-wentDark[0])+", "+(wentDark[2]-wentDark[1]));
                h.assertTrue(CalderSignBlock.is(l.getBlockState(b.offset(-29,3,-112)),CalderSigns.Sign.FOODFAIR_NAME),"its name is still lit over the door");
                h.assertTrue(sounded(p[0],"town.breaker")&&!sounded(p[1],"town.breaker"),"the switch is heard in the lot, and not across town");
                h.assertTrue(sounded(p[1],"town.neon")&&!sounded(p[0],"town.neon"),"the tube is heard by the burger place, and not across town");
                // The pictures that move are told to: each has its frames' card beside it.
                for(var name:List.of("wash_front","wash_front_glow","bigtop_front","bigtop_front_glow"))
                    h.assertTrue(CalderEvening.class.getResource("/assets/the_oldest_house/textures/block/calder_sign/"+name+".png.mcmeta")!=null,"the moving picture "+name+" has its frames' card");
                // The night over: the windows are lit again and the sign is whole.
                BikeNight.resetNow(l,b);
                for(int k=0;k<3;k++)h.assertTrue(CalderTown.aisleLit(l,b,k),"the night over, window "+k+" is lit again");
                h.assertTrue(!CalderTown.neonDim(l,b,false)&&!CalderTown.neonDim(l,b,true),"and the sign is whole");
            }).thenSucceed();
    }
    @AfterBatch(batch="evening_signs") public static void eveningSignsDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="evening_tracks",timeoutTicks=9000)
    public static void aBikeLeavesTracksInTheDewOnTheFieldAndSomebodyOnFootLeavesNone(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final NightBike[] mine={null};final int[] t={0};
        var turf=BikeNightRegistry.material("turf").defaultBlockState();var tracked=BikeNightRegistry.material("turf_track").defaultBlockState();var chalk=BikeNightRegistry.material("turf_line").defaultBlockState();
        int row=-16,from=-140;
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{p[0]=f.player("evening_tracks_rider",P);p[1]=f.player("evening_tracks_walker",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0])&&BikeNight.here(p[1]),"waiting for the night"))
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.WASP);CalderTraffic.startNow(b);})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("wasp"),"waiting for the Wasp to be theirs"))
            // On the field, between two of its yard lines, looking north; and somebody standing on it a little way off.
            .thenExecute(()->{
                h.assertTrue(l.getBlockState(b.offset(row,-1,from))==turf&&l.getBlockState(b.offset(-18,-1,from))==chalk,"the field is turf, with its lines chalked on it");
                mine[0].moveTo(b.getX()+row+.5,b.getY(),b.getZ()+from+.5,180,0);p[0].setYRot(180);f.at(p[1],P,-12.5,0,-145.5);
            })
            .thenIdle(12)
            .thenExecute(()->{
                h.assertTrue(l.getBlockState(b.offset(row,-1,from))==turf&&CalderEvening.tracks(b)==0,"a bike standing still leaves nothing");
                h.assertTrue(l.getBlockState(b.offset(-13,-1,-146))==turf,"and neither does somebody on foot");
            })
            .thenWaitUntil(()->{if(p[0].getVehicle()==mine[0]){p[0].setYRot(180);pedalWell(mine[0],t[0]++,3);}
                h.assertTrue(mine[0].getZ()-b.getZ()<from-9,"waiting for them to ride ten blocks up the field: at "+rel(b,mine[0])+" going "+mine[0].speedNow());})
            .thenExecute(()->{
                int marked=0;for(int z=from-8;z<=from-1;z++)if(l.getBlockState(b.offset(row,-1,z))==tracked)marked++;
                h.assertTrue(marked>=7,"the turf they rode over is tracked: "+marked+" of eight");
                h.assertTrue(CalderEvening.tracks(b)>=marked&&CalderEvening.tracks(b)<=16,"and only where they rode: "+CalderEvening.tracks(b));
                for(int z=from-8;z<=from;z++){
                    h.assertTrue(l.getBlockState(b.offset(row-1,-1,z))==turf&&l.getBlockState(b.offset(row+1,-1,z))==turf,"the turf either side of their way is as it was");
                    h.assertTrue(l.getBlockState(b.offset(-18,-1,z))==chalk,"and the lines are still chalk");
                }
                h.assertTrue(l.getBlockState(b.offset(-13,-1,-146))==turf,"somebody who has stood there all this time has left nothing");
                // The night over: nothing on the field.
                getOff(p[0]);
            })
            .thenIdle(3)
            .thenExecute(()->{
                BikeNight.resetNow(l,b);
                for(int x=CalderTown.FIELD_W;x<=CalderTown.FIELD_E;x++)for(int z=CalderTown.FIELD_N;z<=CalderTown.FIELD_S;z++)h.assertTrue(l.getBlockState(b.offset(x,-1,z))!=tracked,"the night over, there is no track on the field: "+x+", "+z);
            }).thenSucceed();
    }
    @AfterBatch(batch="evening_tracks") public static void eveningTracksDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="evening_sounds",timeoutTicks=9000)
    public static void theDryersTheRadioAndTheTheatersLaughterAreHeardByWhoeverIsNearAndNobodyElse(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{p[0]=f.player("evening_sounds_near",P);p[1]=f.player("evening_sounds_far",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0])&&BikeNight.here(p[1]),"waiting for the night"))
            // One reader on the pavement outside the laundromat; the other stays in the park.
            .thenExecute(()->{f.at(p[0],P,13.5,0,-97.5);f.at(p[1],P,-14.5,0,-20.5);hush(p[0]);hush(p[1]);})
            .thenIdle(CalderEvening.DRYERS_EVERY+12)
            .thenExecute(()->{
                h.assertTrue(!sounded(p[0],"town.dryers"),"until the town wakes there is nothing to hear of it");
                CalderTraffic.startNow(b);
            })
            .thenWaitUntil(()->h.assertTrue(sounded(p[0],"town.dryers"),"waiting for the dryers to be heard outside the laundromat"))
            .thenExecute(()->{hush(p[0]);})
            .thenIdle(CalderEvening.DRYERS_EVERY+4)
            .thenExecute(()->{
                h.assertTrue(sounded(p[0],"town.dryers"),"and they go on");
                // By the car in the burger place's lot.
                f.at(p[0],P,11.5,0,-58.5);hush(p[0]);
            })
            .thenWaitUntil(()->h.assertTrue(sounded(p[0],"town.radio"),"waiting for the car's radio"))
            .thenExecute(()->{
                h.assertTrue(!sounded(p[0],"town.dryers"),"the dryers are not heard from there");
                // On the pavement under the theater's marquee.
                f.at(p[0],P,-14.5,0,-85.5);hush(p[0]);
            })
            .thenIdle(3)
            .thenExecute(()->CalderEvening.laughNow(b))
            .thenWaitUntil(()->h.assertTrue(sounded(p[0],"town.laughter"),"waiting for the house to laugh"))
            .thenExecute(()->{
                for(var name:List.of("town.dryers","town.radio","town.laughter"))h.assertTrue(!sounded(p[1],name),"none of it carries to the park: "+name);
            }).thenSucceed();
    }
    @AfterBatch(batch="evening_sounds") public static void eveningSoundsDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.487: the parked cars are in the way
    @GameTest(template="empty",batch="parked_cars",timeoutTicks=9000)
    public static void aParkedCarIsAThingInTheWayAndTheTownsWaysGoRoundTheTwoOfThem(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final NightBike[] mine={null};final int[] t={0};final double[] furthest={-99};
        final TownCar[] car={null,null};final LiteraryActor[] who=new LiteraryActor[CalderPeople.COUNT];
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("parked_cars_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.WASP);CalderTraffic.startNow(b);BikeNightSite.gateOpen(l,b);})
            .thenWaitUntil(()->{car[0]=CalderTraffic.parked(l,b,0);car[1]=CalderTraffic.parked(l,b,1);
                h.assertTrue(car[0]!=null&&car[1]!=null&&people(l,b,who)&&CalderTraffic.body(l,b,TownCar.WAGON)!=null&&f.own(p[0],P).getString("Bike").equals("wasp"),"waiting for the two parked cars, the people, a car that drives, and for the Wasp to be theirs");})
            .thenIdle(2)
            .thenExecute(()->{
                for(var body:car){
                    var box=body.getBoundingBox();
                    h.assertTrue(body.parked()&&body.canBeCollidedWith(),"a parked car is a thing in the way");
                    h.assertTrue(Math.abs(box.getXsize()-TownCar.WIDE*2)<.01&&Math.abs(box.getZsize()-TownCar.LONG*2)<.01&&Math.abs(box.getYsize()-TownCar.HIGH)<.01,"as long, as wide and as high as it is: "+box);
                    h.assertTrue(!l.noCollision(box.deflate(.2)),"and the world says so");
                }
                var wagon=CalderTraffic.body(l,b,TownCar.WAGON);
                h.assertTrue(!wagon.parked()&&!wagon.canBeCollidedWith(),"one that drives is not: it is never where anybody is, by its own rule");
                // The town's ways, with the two of them standing in their bays: none of them goes through a car.
                var blocked=new ArrayList<String>();
                for(var w:CalderRoutes.ways()){var why=CalderRoutes.blocked(l,b,w);if(why!=null)blocked.add(why);}
                h.assertTrue(blocked.isEmpty(),"a bike gets along every one of the town's ways with the cars parked: "+blocked.size()+" do not, "+blocked.stream().limit(6).toList());
                // And the three do not think the way through the bays is a clear ride.
                var o=Vec3.atLowerCornerOf(b);
                h.assertTrue(!BikeNightHunt.clear(l,o.add(8,0,-62),o.add(25,0,-62)),"across the bays is not a clear ride");
                h.assertTrue(BikeNightHunt.clear(l,o.add(8.5,0,-59.8),o.add(19.5,0,-59.8)),"in front of their noses is");
                // A reader rides at the side of the blue one from Mill Street, flat out.
                mine[0].moveTo(b.getX()+2.5,b.getY(),b.getZ()-63.5,270,0);p[0].setYRot(270);hush(p[0]);
            })
            .thenWaitUntil(()->{
                if(p[0].getVehicle()==mine[0]){p[0].setYRot(270);pedalWell(mine[0],t[0]++,3);}
                if(mine[0].isAlive())furthest[0]=Math.max(furthest[0],mine[0].getX()-b.getX());
                h.assertTrue(p[0].getVehicle()==null,"waiting for them to ride into it and come off: "+(mine[0].isAlive()?rel(b,mine[0])+" going "+mine[0].speedNow():"the bike is gone"));
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],NightBikes.HEAD_ON),"waiting for them to be told"))
            .thenExecute(()->{
                h.assertTrue(furthest[0]<car[0].getX()-b.getX()-TownCar.WIDE+.45,"the bike never went into the car: it got as far as "+furthest[0]);
                h.assertTrue(p[0].getHealth()>=4,"and they are not badly hurt: "+p[0].getHealth());
                for(int i=CalderPeople.HOOD_FIRST;i<CalderPeople.HOOD_FIRST+2;i++)h.assertTrue(car[0].over(who[i].position(),0),"the two on its hood are still on it");
            }).thenSucceed();
    }
    @AfterBatch(batch="parked_cars") public static void parkedCarsDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.488: the stock boy and his carts
    private static boolean chain(ServerLevel l,BlockPos b,TownCar[] carts){
        boolean every=true;for(int i=0;i<carts.length;i++){carts[i]=CalderCarts.cart(l,b,i);every&=carts[i]!=null;}return every;
    }

    @GameTest(template="empty",batch="carts_chain",timeoutTicks=9000)
    public static void theStockBoyWheelsAChainOfCartsAlongTheLotAndWalksRoundItAtEachEnd(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final TownCar[] carts=new TownCar[CalderCarts.CARTS];final LiteraryActor[] boy={null};
        final double[] off={0},was={0};final boolean[] pushed={false},walked={false};final int[] skip={0};
        // Every tick: each cart is where the chain says, on the aisle's line. (Not for a moment after the case has moved the chain itself.)
        Runnable watch=()->{
            if(skip[0]>0){skip[0]--;return;}
            if(!chain(l,b,carts))return;boy[0]=CalderPeople.find(l,b,CalderPeople.STOCK);double west=CalderCarts.west(b);
            for(int i=0;i<carts.length;i++)off[0]=Math.max(off[0],Math.hypot(carts[i].getX()-b.getX()-west-CalderCarts.along(i),carts[i].getZ()-b.getZ()-CalderCarts.AISLE_Z));
        };
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("carts_chain_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            // The reader is on Main Street's pavement by the lot, out of the chain's way and near enough to hear it.
            .thenExecute(()->{
                h.assertTrue(l.getEntitiesOfClass(TownCar.class,whole(f),c->c.kind()==TownCar.CART).isEmpty(),"before the town wakes there is no chain");
                f.at(p[0],P,-20.5,0,-98.5);hush(p[0]);CalderTraffic.startNow(b);BikeNightSite.gateOpen(l,b);
            })
            .thenWaitUntil(()->{watch.run();h.assertTrue(chain(l,b,carts)&&boy[0]!=null,"waiting for the eight carts and the stock boy");})
            .thenIdle(5)
            .thenExecute(()->{
                watch.run();double west=CalderCarts.west(b);
                h.assertTrue(CalderCarts.dir(b)==1&&west>=CalderCarts.WEST&&west<CalderCarts.WEST+2,"the chain sets out east from the west end of the lot: "+west);
                for(var c:carts){
                    var box=c.getBoundingBox();
                    h.assertTrue(c.kind()==TownCar.CART&&c.parked()&&c.canBeCollidedWith(),"a cart is a thing in the way");
                    h.assertTrue(Math.abs(box.getXsize()-TownCar.CART_LONG*2)<.01&&Math.abs(box.getZsize()-TownCar.CART_WIDE*2)<.01&&Math.abs(box.getYsize()-TownCar.CART_HIGH)<.01,"as long, as wide and as high as it is: "+box);
                    h.assertTrue(box.getYsize()>1,"and too high for a bike to roll up onto");
                }
                h.assertTrue(Math.abs(carts[CalderCarts.GAP_AFTER].getX()-carts[CalderCarts.GAP_AFTER-1].getX()-CalderCarts.STEP-CalderCarts.GAP)<.01&&CalderCarts.gapTo()-CalderCarts.gapFrom()>1.4,"there is a gap after the fourth, wide enough for a bike: "+(CalderCarts.gapTo()-CalderCarts.gapFrom()));
                h.assertTrue(Math.abs(rel(b,boy[0]).x-(west-CalderCarts.BEHIND))<.4&&Math.abs(rel(b,boy[0]).z-CalderCarts.AISLE_Z)<.2,"he is behind the last of them: "+rel(b,boy[0]));
                h.assertTrue(boy[0].phase()==CalderPeople.FIRST+CalderPeople.STOCK&&boy[0].gesture()==CalderPeople.PUSH,"pushing");
                // The four carts that stood in the aisle are the chain now; the one left out by the bays is still there.
                h.assertTrue(l.getBlockState(b.offset(-16,0,-105)).isAir()&&l.getBlockState(b.offset(-34,0,-103)).is(BikeNightRegistry.TOWN.get()),"the row that stood in the aisle is gone, and the one left out is not");
                // With the chain at this end, every way of the town is clear.
                var blocked=new ArrayList<String>();
                for(var w:CalderRoutes.ways()){var why=CalderRoutes.blocked(l,b,w);if(why!=null)blocked.add(why);}
                h.assertTrue(blocked.isEmpty(),"a bike gets along every way of the town with the chain at the west end: "+blocked.stream().limit(6).toList());
                h.assertTrue(BikeNightHunt.clear(l,CalderRoutes.at(b,CalderRoutes.id("food_east")),CalderRoutes.at(b,CalderRoutes.id("yard_south"))),"the way into the yard among them");
                was[0]=west;
            })
            .thenIdle(40)
            .thenExecute(()->{
                watch.run();double went=CalderCarts.west(b)-was[0];
                h.assertTrue(Math.abs(went-40*CalderCarts.PACE)<.15,"it goes at a walk: "+went+" blocks in two seconds");
                h.assertTrue(sounded(p[0],"town.carts"),"and is heard");
                // Near the east end of its run, and quicker, for the case.
                CalderCarts.put(b,CalderCarts.EAST-CalderCarts.span()-1.5,1,.1);skip[0]=3;
            })
            .thenWaitUntil(()->{watch.run();h.assertTrue(CalderCarts.dir(b)==-1&&CalderCarts.rounding(b),"waiting for it to come to the east end: "+CalderCarts.west(b));})
            .thenExecute(()->{
                double west=CalderCarts.west(b);
                h.assertTrue(Math.abs(west+CalderCarts.span()-CalderCarts.EAST)<.01,"its last cart is at the east end of its run: "+(west+CalderCarts.span()));
                // The mouth of the yard is barred from side to side, but for the gap.
                var o=Vec3.atLowerCornerOf(b);int open=0,shut=0;
                for(double x=-11;x<=-4.2;x+=.2){
                    boolean free=l.noCollision(new AABB(o.x+x-.05,o.y+.3,o.z+CalderCarts.AISLE_Z-.05,o.x+x+.05,o.y+.8,o.z+CalderCarts.AISLE_Z+.05));
                    boolean gap=x>west+CalderCarts.gapFrom()&&x<west+CalderCarts.gapTo();
                    if(free&&!gap&&x>west-TownCar.CART_LONG+.1){boolean between=false;for(int i=0;i+1<carts.length;i++)between|=x>carts[i].getX()-o.x+TownCar.CART_LONG&&x<carts[i+1].getX()-o.x-TownCar.CART_LONG;h.assertTrue(between,"the chain is in the way at "+x);}
                    if(gap){h.assertTrue(free,"the gap is open at "+x);open++;}else if(!free)shut++;
                }
                h.assertTrue(open>=6&&shut>=20,"barred but for the gap: "+open+" open, "+shut+" shut");
                h.assertTrue(!BikeNightHunt.clear(l,CalderRoutes.at(b,CalderRoutes.id("food_east")),CalderRoutes.at(b,CalderRoutes.id("yard_south"))),"and the way into the yard is not a clear ride");
            })
            // He walks round to the other end of them.
            .thenWaitUntil(()->{watch.run();if(boy[0].gesture()==0&&Math.abs(rel(b,boy[0]).z-CalderCarts.ROUND_Z)<.3)walked[0]=true;
                h.assertTrue(!CalderCarts.rounding(b),"waiting for him to walk round to its east end: "+rel(b,boy[0]));})
            .thenExecute(()->{
                double west=CalderCarts.west(b);
                h.assertTrue(walked[0],"he went round beside them, not through them");
                h.assertTrue(Math.abs(rel(b,boy[0]).x-(west+CalderCarts.span()+CalderCarts.BEHIND))<.3&&Math.abs(rel(b,boy[0]).z-CalderCarts.AISLE_Z)<.3,"and is at their east end: "+rel(b,boy[0]));
                h.assertTrue(Math.abs(west+CalderCarts.span()-CalderCarts.EAST)<.01,"they have not moved while he walked");
                CalderCarts.put(b,west,-1,.1);skip[0]=3;
            })
            .thenWaitUntil(()->{watch.run();if(boy[0].gesture()==CalderPeople.PUSH&&Math.abs(net.minecraft.util.Mth.wrapDegrees(boy[0].getYRot()-90))<5)pushed[0]=true;
                h.assertTrue(CalderCarts.west(b)<CalderCarts.EAST-CalderCarts.span()-4,"waiting for it to go back west: "+CalderCarts.west(b));})
            .thenExecute(()->{
                h.assertTrue(pushed[0],"he pushes it back the way it came");
                h.assertTrue(off[0]<.02,"no cart was ever out of its place in the chain: "+off[0]);
                h.assertTrue(BikeNightHunt.clear(l,CalderRoutes.at(b,CalderRoutes.id("food_east")),CalderRoutes.at(b,CalderRoutes.id("yard_south"))),"and the way into the yard is clear again");
                // The night over: there is no chain.
                BikeNight.resetNow(l,b);
                h.assertTrue(l.getEntitiesOfClass(TownCar.class,whole(f),c->c.kind()==TownCar.CART&&c.isAlive()).isEmpty(),"the night over, there is no chain");
            }).thenSucceed();
    }
    @AfterBatch(batch="carts_chain") public static void cartsChainDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="carts_wait",timeoutTicks=9000)
    public static void theChainWaitsForSomebodyInItsWayAndAReaderRidesThroughTheGapOrComesOffAtACart(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final NightBike[] mine={null};final TownCar[] carts=new TownCar[CalderCarts.CARTS];final LiteraryActor[] boy={null};
        final int[] t={0};final double[] was={0},nearest={9},least={9};final boolean[] under={false},said={false};double start=-30;
        Runnable watch=()->{
            chain(l,b,carts);boy[0]=CalderPeople.find(l,b,CalderPeople.STOCK);
            for(var c:carts)if(c!=null&&mine[0]!=null&&mine[0].isAlive()){if(c.over(mine[0].position(),0))under[0]=true;nearest[0]=Math.min(nearest[0],Math.abs(mine[0].getX()-c.getX())+(Math.abs(mine[0].getZ()-c.getZ())<.6?0:9));}
        };
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("carts_wait_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.WASP);CalderTraffic.startNow(b);})
            .thenWaitUntil(()->{watch.run();h.assertTrue(chain(l,b,carts)&&boy[0]!=null&&f.own(p[0],P).getString("Bike").equals("wasp"),"waiting for the chain, the stock boy, and for the Wasp to be theirs");})
            // The chain mid-lot, going east; and the reader standing on their bike a block in front of its first cart.
            .thenExecute(()->{
                CalderCarts.put(b,start,1,CalderCarts.PACE);
                mine[0].moveTo(b.getX()+start+CalderCarts.span()+TownCar.CART_LONG+1,b.getY(),b.getZ()+CalderCarts.AISLE_Z,0,0);was[0]=start;hush(p[0]);
            })
            .thenExecuteFor(CalderCarts.MIND_AFTER+20,()->{watch.run();if(boy[0]!=null&&List.of(BikeNightTexts.CARTS_MIND).contains(saying(boy[0])))said[0]=true;})
            .thenExecute(()->{
                h.assertTrue(CalderCarts.waiting(b)&&Math.abs(CalderCarts.west(b)-was[0])<.11,"the chain waits for somebody in its way: it has gone "+(CalderCarts.west(b)-was[0]));
                h.assertTrue(!under[0],"and was never where they are");
                h.assertTrue(said[0]&&boy[0].getCustomName()!=null,"and after a moment he says so: "+saying(boy[0]));
                h.assertTrue(boy[0].gesture()==0,"he is not pushing");
                // They go, and it goes on.
                mine[0].moveTo(b.getX()-20.5,b.getY(),b.getZ()-98.5,0,0);was[0]=CalderCarts.west(b);
            })
            .thenIdle(30)
            .thenExecute(()->{
                h.assertTrue(!CalderCarts.waiting(b)&&CalderCarts.west(b)-was[0]>1,"with them gone it goes on: "+(CalderCarts.west(b)-was[0]));
                // Standing still (as near as makes no matter), and the reader rides north at the gap from the lot's south side.
                double west=-26;CalderCarts.put(b,west,1,.00001);under[0]=false;nearest[0]=9;hush(p[0]);
                mine[0].moveTo(b.getX()+west+(CalderCarts.gapFrom()+CalderCarts.gapTo())/2,b.getY(),b.getZ()+CalderCarts.AISLE_Z+4.5,180,0);p[0].setYRot(180);t[0]=0;
            })
            .thenIdle(2)
            .thenWaitUntil(()->{watch.run();if(p[0].getVehicle()==mine[0]){p[0].setYRot(180);pedalWell(mine[0],t[0]++,3);}
                h.assertTrue(mine[0].getZ()-b.getZ()<CalderCarts.AISLE_Z-2,"waiting for them to ride through the gap: at "+rel(b,mine[0])+" going "+mine[0].speedNow()+(p[0].getVehicle()==mine[0]?"":", off the bike"));})
            .thenExecute(()->{
                h.assertTrue(p[0].getVehicle()==mine[0]&&!under[0],"they are through, still on, and touched no cart");
                h.assertTrue(CalderCarts.through(b)==1,"and it is counted: "+CalderCarts.through(b));
                h.assertTrue(List.of(BikeNightTexts.CARTS_THROUGH).contains(saying(boy[0]))||said[0],"he has something to say to it: "+saying(boy[0]));
                // And flat out at the side of the second cart, from Main Street's pavement.
                under[0]=false;hush(p[0]);
                mine[0].moveTo(carts[1].getX(),b.getY(),b.getZ()+CalderCarts.AISLE_Z+7,180,0);p[0].setYRot(180);t[0]=0;
            })
            .thenIdle(2)
            .thenWaitUntil(()->{watch.run();
                if(p[0].getVehicle()==mine[0]){p[0].setYRot(180);pedalWell(mine[0],t[0]++,3);}
                if(mine[0].isAlive())least[0]=Math.min(least[0],mine[0].getZ()-b.getZ()-CalderCarts.AISLE_Z);
                h.assertTrue(p[0].getVehicle()==null,"waiting for them to ride into a cart and come off: "+(mine[0].isAlive()?rel(b,mine[0])+" going "+mine[0].speedNow():"the bike is gone"));})
            .thenWaitUntil(()->h.assertTrue(heard(p[0],NightBikes.HEAD_ON),"waiting for them to be told"))
            .thenExecute(()->{
                h.assertTrue(least[0]>TownCar.CART_WIDE-.05&&!under[0],"the bike never went into the cart: it got to "+least[0]+" from the aisle's line");
                h.assertTrue(p[0].getHealth()>=4,"and they are not badly hurt: "+p[0].getHealth());
                for(var line:BikeNightTexts.CARTS_MIND)h.assertTrue(!heard(p[0],line),"what he says is never in chat: "+line);
                for(var line:BikeNightTexts.CARTS_THROUGH)h.assertTrue(!heard(p[0],line),"what he says is never in chat: "+line);
            }).thenSucceed();
    }
    @AfterBatch(batch="carts_wait") public static void cartsWaitDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.489, the graveyard
    private static AABB graves(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()+CalderGraves.W-1,b.getY()-5,b.getZ()+CalderGraves.BACK-4,b.getX()+CalderGraves.E+2,b.getY()+CalderGraves.TOP+2,b.getZ()+CalderGraves.FRONT+4);}
    /** The rider's own brake, held until the bike has stopped; then let go. */
    private static boolean halted(ServerPlayer p,RiddenBike bike){
        if(p.getVehicle()!=bike)return true;
        if(bike.speedNow()>.001F){p.setPlayerInput(0,-1,false,false);return false;}
        p.setPlayerInput(0,0,false,false);return true;
    }
    private static net.minecraft.world.level.block.state.BlockState stood(ServerLevel l,BlockPos b,int x,int z){return l.getBlockState(b.offset(x,CalderGraves.level(z),z));}
    /** The first stone of a row, counting from the west, that is (or is not) a low marker, well away from the gate's line and from any tree. */
    private static int stoneIn(ServerLevel l,BlockPos b,int row,boolean low){
        for(int x=CalderGraves.W+3;x<=CalderGraves.E-3;x++){
            var s=stood(l,b,x,row);
            if(!CalderGraveBlock.is(s,CalderGraveBlock.Kind.STONE)||CalderGraveBlock.low(s)!=low||Math.abs(x)<16||Math.abs(Math.abs(x)-35)<4||Math.abs(Math.abs(x)-52)<4)continue;
            // With nothing between it and the back railings but grass.
            boolean open=true;for(int z=row-1;z>=CalderGraves.BACK+3;z--)if(!stood(l,b,x,z).isAir())open=false;
            if(open)return x;
        }
        return Integer.MIN_VALUE;
    }

    @GameTest(template="empty",batch="graves_built",timeoutTicks=2400)
    public static void theCemeteryStandsOnItsRiseBehindTheTownInsideRailingsJoinedAllTheWayRound(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,graves(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the cemetery is resident"))
            .thenExecute(()->{
                var turf=BikeNightRegistry.material("grave_turf");var gravel=BikeNightRegistry.material("gravel");var earth=BikeNightRegistry.material("grave_earth");var marble=BikeNightRegistry.material("marble");
                // The proposal: "It sits on a rise at the edge of town". Three banks, each a row of half blocks, the ground a block higher behind it.
                h.assertTrue(CalderGraves.level(CalderGraves.FRONT-1)==0&&CalderGraves.level(CalderGraves.BACK+1)==CalderGraves.BANKS.length&&CalderGraves.BANKS.length==3,"the back of it is three blocks above the gate");
                int wrong=0;String first="";
                for(int z=CalderGraves.BACK+1;z<CalderGraves.FRONT;z++)for(int x=CalderGraves.W;x<=CalderGraves.E;x++){
                    int y=CalderGraves.level(z);var ground=l.getBlockState(b.offset(x,y-1,z));
                    // The row the hill's ground ends on is the Hollow's edge, and is sod: the side of the hill.
                    if(z==CalderHollow.EDGE)continue;
                    boolean pit=x>=CalderGraves.PIT_W&&x<=CalderGraves.PIT_E&&z>=CalderGraves.PIT_N&&z<=CalderGraves.HEAP;
                    // "The paths are gravel."
                    boolean ok=pit||ground.is(CalderGraves.path(x,z)?gravel:turf);
                    if(ok&&CalderGraves.bank(z)&&x>CalderGraves.W&&x<CalderGraves.E){
                        var step=l.getBlockState(b.offset(x,y,z));
                        ok=step.getBlock() instanceof net.minecraft.world.level.block.SlabBlock&&step.getValue(net.minecraft.world.level.block.SlabBlock.TYPE)==net.minecraft.world.level.block.state.properties.SlabType.BOTTOM
                            &&step.is(CalderGraves.path(x,z)?BikeNightRegistry.GRAVEL_SLAB.get():BikeNightRegistry.GRAVE_TURF_SLAB.get());
                    }
                    if(!ok&&wrong++==0)first=x+","+z+" is "+ground;
                }
                h.assertTrue(wrong==0,"grass, gravel on the paths, and half blocks up each bank: "+wrong+" places are not, the first "+first);
                // "the iron fence with spear tops, which must be connected properly": no gap anywhere in it, a pier at every corner and on every bank.
                var brick=BikeNightRegistry.material("brick");
                for(int z=CalderGraves.FRONT;z>CalderGraves.BACK;z--)for(int x:new int[]{CalderGraves.W,CalderGraves.E}){
                    int y=CalderGraves.level(z);var at=b.offset(x,y,z);var s=l.getBlockState(at);
                    h.assertTrue(!s.getCollisionShape(l,at).isEmpty()&&!l.getBlockState(at.above()).getCollisionShape(l,at.above()).isEmpty(),"the side railings are whole, two high, at "+x+","+z);
                    boolean corner=z==CalderGraves.FRONT||z==CalderGraves.BACK+1;
                    if(corner||CalderGraves.bank(z))h.assertTrue(s.is(brick),"a pier at "+(corner?"the corner ":"the bank ")+x+","+z+": "+s);
                    else h.assertTrue(s.is(brick)||CalderBlock.is(s,CalderBlock.Kind.FENCE)&&s.getValue(CalderBlock.FACING).getAxis()==Direction.Axis.X,"railings along the side at "+x+","+z+": "+s);
                    // A pier on a bank reaches the rails above it as well as the ones below.
                    if(CalderGraves.bank(z))h.assertTrue(l.getBlockState(at.above(2)).is(brick)&&CalderBlock.is(l.getBlockState(at.above(3)),CalderBlock.Kind.CAP),"the pier on the bank at "+x+","+z+" stands a block taller");
                }
                // (0.4.494: the back railings and the back gate are on the Hollow's far side, and are looked at there.)
                // "headstones at every lean and height": rows of them across the hill, fuller toward the back, with the gate's line clear in every one.
                var kinds=new java.util.HashSet<Integer>();int stones=0,crosses=0,lowOnes=0,goods=0,lanterns=0;int[] inRow=new int[CalderGraves.ROWS.length];
                for(int r=0;r<CalderGraves.ROWS.length;r++){
                    int z=CalderGraves.ROWS[r];
                    for(int x=CalderGraves.W+1;x<CalderGraves.E;x++){
                        var s=stood(l,b,x,z);
                        boolean stone=CalderGraveBlock.is(s,CalderGraveBlock.Kind.STONE),cross=CalderGraveBlock.is(s,CalderGraveBlock.Kind.CROSS);
                        if(!stone&&!cross)continue;
                        inRow[r]++;if(stone){stones++;kinds.add(s.getValue(CalderGraveBlock.STAGE));if(CalderGraveBlock.low(s))lowOnes++;}else crosses++;
                        h.assertTrue(!CalderGraves.path(x,z)&&!CalderGraves.kept(x,z)&&l.getBlockState(b.offset(x,CalderGraves.level(z)-1,z)).is(turf),"a stone stands on grass, off the paths and out of the gate's line: "+x+","+z);
                        h.assertTrue(s.getValue(CalderGraveBlock.FACING)==Direction.SOUTH,"its name looks down the hill at whoever is coming up: "+x+","+z);
                    }
                    for(int x=CalderGraves.PATH_W;x<=CalderGraves.PATH_E;x++)h.assertTrue(stood(l,b,x,z).isAir(),"the gate's line is clear in the row at "+z);
                }
                for(int z=CalderGraves.BACK+1;z<CalderGraves.FRONT;z++)for(int x=CalderGraves.W+1;x<CalderGraves.E;x++){var s=stood(l,b,x,z);if(CalderGraveBlock.is(s,CalderGraveBlock.Kind.GOODS)){goods++;if(s.getValue(CalderGraveBlock.STAGE)==CalderGraveBlock.LANTERN)lanterns++;}}
                h.assertTrue(stones>=180&&crosses>=6&&kinds.size()==CalderGraveBlock.STONES&&lowOnes>=20&&lowOnes<stones/2,"stones of every shape, low ones among them, and crosses: "+stones+" stones of "+kinds.size()+" kinds, "+lowOnes+" low, "+crosses+" crosses");
                h.assertTrue(inRow[0]<inRow[CalderGraves.ROWS.length-1]&&inRow[0]+inRow[1]+inRow[2]<inRow[9]+inRow[10]+inRow[11],"the rows fill toward the back: "+java.util.Arrays.toString(inRow));
                h.assertTrue(goods>=8&&lanterns>=1,"things are left at some of them, a lantern among them: "+goods+", "+lanterns+" lanterns");
                // "One row of small headstones near the back": six of them, three each side of the gate's line, behind every other row.
                for(int i=0;i<CalderGraves.SMALL_X.length;i++){
                    var s=stood(l,b,CalderGraves.SMALL_X[i],CalderGraves.SMALL_ROW);
                    h.assertTrue(CalderGraveBlock.is(s,CalderGraveBlock.Kind.SMALL)&&s.getValue(CalderGraveBlock.STAGE)==i,"the small stone "+i+" at x="+CalderGraves.SMALL_X[i]+": "+s);
                }
                h.assertTrue(CalderGraves.SMALL_X.length==CalderGraveBlock.SMALL_STONES&&CalderGraves.SMALL_ROW<CalderGraves.ROWS[CalderGraves.ROWS.length-1]&&CalderGraves.SMALL_ROW>CalderGraves.BACK+4,"the small row is the last, near the back");
                // "an angel; an obelisk; a family mausoleum with iron doors".
                for(int i=0;i<3;i++){
                    h.assertTrue(CalderGraveBlock.is(l.getBlockState(b.offset(CalderGraves.ANGEL.above(i))),CalderGraveBlock.Kind.ANGEL)&&l.getBlockState(b.offset(CalderGraves.ANGEL.above(i))).getValue(CalderGraveBlock.STAGE)==i,"the angel, block "+i);
                    h.assertTrue(CalderGraveBlock.is(l.getBlockState(b.offset(CalderGraves.OBELISK.above(i))),CalderGraveBlock.Kind.OBELISK)&&l.getBlockState(b.offset(CalderGraves.OBELISK.above(i))).getValue(CalderGraveBlock.STAGE)==i,"the obelisk, block "+i);
                }
                h.assertTrue(CalderGraves.ANGEL.getY()==CalderGraves.level(CalderGraves.ANGEL.getZ())&&CalderGraves.OBELISK.getY()==CalderGraves.level(CalderGraves.OBELISK.getZ()),"each stands on the ground of its own level");
                int floor=CalderGraves.level(CalderGraves.TOMB_S);
                for(int i=0;i<4;i++){
                    var s=l.getBlockState(b.offset(i%2==0?CalderGraves.DOOR_W:CalderGraves.DOOR_E,floor+i/2,CalderGraves.TOMB_S));
                    h.assertTrue(CalderGraveBlock.is(s,CalderGraveBlock.Kind.DOOR)&&s.getValue(CalderGraveBlock.STAGE)==i&&s.getValue(CalderGraveBlock.FACING)==Direction.SOUTH,"the mausoleum's iron doors, quarter "+i+": "+s);
                    h.assertTrue(l.getBlockState(b.offset(i%2==0?CalderGraves.DOOR_W:CalderGraves.DOOR_E,floor+i/2,CalderGraves.TOMB_S-1)).is(marble),"and stone behind them: nobody goes in");
                }
                h.assertTrue(l.getBlockState(b.offset(CalderGraves.TOMB_W,floor,CalderGraves.TOMB_S)).is(marble)&&l.getBlockState(b.offset(CalderGraves.TOMB_E,floor+CalderGraves.TOMB_HIGH-1,CalderGraves.TOMB_N)).is(marble)
                    &&l.getBlockState(b.offset(CalderGraves.TOMB_W-1,floor+CalderGraves.TOMB_HIGH,CalderGraves.TOMB_S+1)).is(marble)&&CalderGraves.path(CalderGraves.DOOR_W,CalderGraves.TOMB_S+1),"it is marble, its roof oversails its walls, and gravel leads to its doors");
                // "a fresh open grave with a mound and a tarp": where the gate's path ends.
                int y=CalderGraves.level(CalderGraves.PIT_S);
                for(int x=CalderGraves.PIT_W;x<=CalderGraves.PIT_E;x++){
                    for(int z=CalderGraves.PIT_N+1;z<=CalderGraves.PIT_S;z++)h.assertTrue(l.getBlockState(b.offset(x,y-1,z)).isAir()&&l.getBlockState(b.offset(x,y-2,z)).isAir()&&l.getBlockState(b.offset(x,y-3,z)).is(earth),"the pit is two deep, with earth at the bottom: "+x+","+z);
                    h.assertTrue(l.getBlockState(b.offset(x,y-2,CalderGraves.PIT_N)).is(earth)&&l.getBlockState(b.offset(x,y-1,CalderGraves.PIT_N)).isAir(),"a step of earth is left at its north end");
                    var heap=l.getBlockState(b.offset(x,y,CalderGraves.HEAP));
                    h.assertTrue(CalderGraveBlock.is(heap,CalderGraveBlock.Kind.MOUND)&&heap.getValue(CalderGraveBlock.FACING)==Direction.NORTH,"its heap at its south end, the way up it north: "+heap);
                    h.assertTrue(CalderGraves.path(x,CalderGraves.HEAP+1)&&CalderGraves.PATH_END==CalderGraves.HEAP+1,"and the gate's path runs straight to it");
                }
                h.assertTrue(CalderGraveBlock.is(l.getBlockState(b.offset(CalderGraves.PIT_E+1,y,CalderGraves.PIT_S-1)),CalderGraveBlock.Kind.TARP)&&CalderGraveBlock.is(l.getBlockState(b.offset(CalderGraves.PIT_W-1,y,CalderGraves.PIT_S-1)),CalderGraveBlock.Kind.TARP),"a tarpaulin on each side of it");
                // Nothing vanilla stands in it but earth under the grass and the trunks of its trees; and it has trees.
                int stray=0,trunks=0;String strays="";
                for(int x=CalderGraves.W;x<=CalderGraves.E;x++)for(int z=CalderGraves.BACK+1;z<CalderGraves.FRONT;z++)for(int yy=-1;yy<=CalderGraves.TOP;yy++){
                    var s=l.getBlockState(b.offset(x,yy,z));if(s.isAir()||mine(s))continue;
                    if(s.is(Blocks.OAK_LOG)){if(yy==CalderGraves.level(z))trunks++;continue;}
                    if(s.is(Blocks.DIRT)&&yy<CalderGraves.level(z)-1)continue;
                    if(stray++<4)strays+=" "+s.getBlock()+" at "+x+","+yy+","+z;
                }
                h.assertTrue(stray==0&&trunks>=20,"nothing vanilla is built there: "+stray+strays+"; "+trunks+" trees");
                // What a bike makes of it: a standing stone is higher than a bike's step, a low one is not, and the heap is a slope to it and no steps.
                int tall=stoneIn(l,b,CalderGraves.ROWS[11],false),lowAt=stoneIn(l,b,CalderGraves.ROWS[11],true);
                h.assertTrue(tall!=Integer.MIN_VALUE&&lowAt!=Integer.MIN_VALUE,"a standing stone and a low one in the last row with open grass behind them");
            }).thenSucceed();
    }
    @AfterBatch(batch="graves_built") public static void gravesBuiltDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="graves_hands",timeoutTicks=2400)
    public static void aHandOnAStoneIsToldWhatIsCutOnItAndTheSmallRowHasNamesAndTheOneDateAndNothingElse(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};
        record Thing(BlockPos at,String says){}
        var things=new ArrayList<Thing>();var small=new ArrayList<String>();
        // The proposal: "One row of small headstones near the back all carries the same autumn date, years before, with names and nothing else."
        for(int i=0;i<CalderGraves.SMALL_X.length;i++){
            var cut=BikeNightTexts.SMALL_NAMES[i]+". "+BikeNightTexts.SMALL_DATE+".";small.add(cut);
            things.add(new Thing(new BlockPos(CalderGraves.SMALL_X[i],CalderGraves.level(CalderGraves.SMALL_ROW),CalderGraves.SMALL_ROW),cut));
        }
        // The memorials, from any part of them; and the mausoleum's doors.
        for(int i=0;i<3;i++){things.add(new Thing(CalderGraves.ANGEL.above(i),BikeNightTexts.ANGEL_STONE));things.add(new Thing(CalderGraves.OBELISK.above(i),BikeNightTexts.OBELISK_STONE));}
        things.add(new Thing(new BlockPos(CalderGraves.DOOR_W,CalderGraves.level(CalderGraves.TOMB_S),CalderGraves.TOMB_S),BikeNightTexts.TOMB_DOORS));
        things.add(new Thing(new BlockPos(CalderGraves.DOOR_E,CalderGraves.level(CalderGraves.TOMB_S)+1,CalderGraves.TOMB_S),BikeNightTexts.TOMB_DOORS));
        var seq=h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(graves(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("graves_hands_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&BikeNight.here(p[0]),"waiting for the night"));
        for(var thing:things)seq=seq.thenExecute(()->{hush(p[0]);f.click(p[0],P,thing.at());})
            .thenWaitUntil(()->h.assertTrue(heard(p[0],thing.says()),"a hand at "+thing.at().toShortString()+" is told: "+thing.says()));
        // What is not carved says nothing of the kind: the heap.
        seq.thenExecute(()->{hush(p[0]);f.click(p[0],P,new BlockPos(CalderGraves.PIT_W,CalderGraves.level(CalderGraves.HEAP),CalderGraves.HEAP));})
            .thenIdle(6)
            .thenExecute(()->{
                for(var line:BikeNightTexts.graveLines())h.assertTrue(!heard(p[0],line),"the heap has nothing cut on it: "+line);
                for(var cut:small)h.assertTrue(cut.matches("[A-Z]+ [A-Z]+\\. OCTOBER 31, 1957\\."),"a name and the date and nothing else: "+cut);
                h.assertTrue(new java.util.HashSet<>(small).size()==CalderGraveBlock.SMALL_STONES,"six names, each its own: "+small);
                for(int i=0;i<CalderGraves.SMALL_X.length;i++){
                    var rel=new BlockPos(CalderGraves.SMALL_X[i],CalderGraves.level(CalderGraves.SMALL_ROW),CalderGraves.SMALL_ROW);
                    h.assertTrue(CalderGraves.inscription(l.getBlockState(b.offset(rel)),rel).equals(small.get(i)),"the small stone "+i+" says "+small.get(i));
                }
                // Every other stone: a name, two years, and for some a line. Nobody under one died young, none after the last year, and the row's year is the row's.
                int read=0,lined=0,wooden=0;var families=new java.util.HashSet<String>();var said=new java.util.HashSet<String>();
                for(int r=0;r<CalderGraves.ROWS.length;r++)for(int x=CalderGraves.W+1;x<CalderGraves.E;x++){
                    var rel=new BlockPos(x,CalderGraves.level(CalderGraves.ROWS[r]),CalderGraves.ROWS[r]);var s=l.getBlockState(b.offset(rel));
                    if(!CalderGraveBlock.carved(s))continue;
                    var cut=CalderGraves.inscription(s,rel);read++;said.add(cut);
                    h.assertTrue(!cut.contains("1957"),"the row's year is on nothing but the row: "+cut+" at "+x+","+rel.getZ());
                    if(CalderGraveBlock.is(s,CalderGraveBlock.Kind.CROSS)&&s.getValue(CalderGraveBlock.STAGE)==1){
                        h.assertTrue(cut.matches("[A-Z]+ [A-Z]+\\. \\d{4}\\."),"a wooden cross has a name and a year: "+cut);wooden++;continue;
                    }
                    var m=java.util.regex.Pattern.compile("([A-Z]+) ([A-Z]+)\\. (\\d{4})-(\\d{4})\\.( [A-Z' ]+\\.)?").matcher(cut);
                    h.assertTrue(m.matches(),"a name, two years and perhaps a line: "+cut);
                    int born=Integer.parseInt(m.group(3)),died=Integer.parseInt(m.group(4));
                    h.assertTrue(died-born>=21&&died-born<=85&&died<=BikeNightTexts.LAST_YEAR&&died>=BikeNightTexts.FIRST_YEAR-1,"nobody under a stone here died young, or lately: "+cut);
                    if(CalderGraveBlock.low(s))h.assertTrue(died==BikeNightTexts.MARKER_YEARS[s.getValue(CalderGraveBlock.STAGE)-CalderGraveBlock.LOW],"a low marker's year is the one on its face: "+cut+" on "+s);
                    if(m.group(5)!=null)lined++;
                    families.add(m.group(2));
                    h.assertTrue(List.of(BikeNightTexts.FAMILIES).contains(m.group(2))&&List.of(BikeNightTexts.GIVEN).contains(m.group(1)),"the town's own names: "+cut);
                }
                h.assertTrue(read>=180&&lined>read/4&&lined<read*3/4&&families.size()==BikeNightTexts.FAMILIES.length&&said.size()>read*9/10,"they are not all alike: "+read+" read, "+said.size()+" different, "+lined+" with a line, "+families.size()+" families, "+wooden+" wooden crosses");
                // A family lies together: the next stone along has the same name more often than not.
                int pairs=0,same=0;
                for(int r=0;r<CalderGraves.ROWS.length;r++){String last=null;int lastX=0;
                    for(int x=CalderGraves.W+1;x<CalderGraves.E;x++){
                        var rel=new BlockPos(x,CalderGraves.level(CalderGraves.ROWS[r]),CalderGraves.ROWS[r]);var s=l.getBlockState(b.offset(rel));if(!CalderGraveBlock.carved(s))continue;
                        var family=CalderGraves.inscription(s,rel).split("\\.")[0].split(" ")[1];
                        if(last!=null&&x-lastX<=3){pairs++;if(family.equals(last))same++;}
                        last=family;lastX=x;
                    }
                }
                h.assertTrue(pairs>60&&same*2>pairs,"neighbours share a name more often than not: "+same+" of "+pairs);
                // What is cut is cut in capitals; what is said of the doors is said.
                for(var line:BikeNightTexts.graveLines())h.assertTrue(line.equals(BikeNightTexts.TOMB_DOORS)||line.equals(line.toUpperCase(java.util.Locale.ROOT)),"what is cut is cut in capitals: "+line);
            }).thenSucceed();
    }
    @AfterBatch(batch="graves_hands") public static void gravesHandsDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="graves_ride",timeoutTicks=9000)
    public static void aBikeRidesUpTheHillFromTheGateAndAStandingStoneIsAWallToItAndALowOneIsABump(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final NightBike[] mine={null};final int[] t={0},at={0,0};final double[] least={9},most={-9,-999};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(graves(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("graves_ride_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            .thenExecute(()->mine[0]=take(h,f,p[0],NightBike.Make.LAMP))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp"),"waiting for the Lamp to be theirs"))
            // Just inside the gate, on the gravel, looking up the hill.
            .thenExecute(()->{mine[0].moveTo(b.getX()-.5,b.getY(),b.getZ()+CalderGraves.FRONT-1.5,180,0);p[0].setYRot(180);})
            .thenWaitUntil(()->{
                if(p[0].getVehicle()==mine[0]){p[0].setYRot(180);pedalWell(mine[0],t[0]++,3);}
                double z=mine[0].getZ()-b.getZ();
                // Once it is going, what it loses on a bank.
                if(z<CalderGraves.BANKS[0]+6)least[0]=Math.min(least[0],mine[0].speedNow());
                h.assertTrue(p[0].getVehicle()==mine[0],"they are still on it: at "+rel(b,mine[0]));
                h.assertTrue(z<CalderGraves.PATH_END+2,"waiting for them to ride up the path to its end: at "+rel(b,mine[0])+" going "+mine[0].speedNow());
            })
            .thenExecute(()->{
                h.assertTrue(Math.abs(mine[0].getY()-b.getY()-3)<.02,"three banks up: "+rel(b,mine[0]));
                h.assertTrue(least[0]>RiddenBike.TOP*.8F,"and a bank does not slow a bike: its least way on the hill was "+least[0]);
                at[0]=stoneIn(l,b,CalderGraves.ROWS[11],false);at[1]=stoneIn(l,b,CalderGraves.ROWS[11],true);
            })
            .thenWaitUntil(()->h.assertTrue(halted(p[0],mine[0]),"waiting for them to stop"))
            // A low marker in the last row, with open grass north of it: ridden at from behind, from standing.
            .thenExecute(()->{
                mine[0].moveTo(b.getX()+at[1]+.5,b.getY()+3,b.getZ()+CalderGraves.BACK+3.5,0,0);p[0].setYRot(0);t[0]=0;
            })
            .thenWaitUntil(()->{
                h.assertTrue(p[0].getVehicle()==mine[0],"they are on it at the low marker: "+rel(b,mine[0]));
                p[0].setYRot(0);pedalWell(mine[0],t[0]++,3);most[0]=Math.max(most[0],mine[0].getY()-b.getY());
                h.assertTrue(mine[0].getZ()-b.getZ()>CalderGraves.ROWS[11]+2.5,"waiting for them to ride over the low marker: at "+rel(b,mine[0])+" going "+mine[0].speedNow());
            })
            .thenExecute(()->{
                // "Low stones can be hopped": or bumped over, half a block up and down again.
                h.assertTrue(most[0]>3.4&&most[0]<3.75&&p[0].getVehicle()==mine[0],"a low marker is a bump to a bike, and they are still on it: it rose to "+most[0]);
            })
            .thenWaitUntil(()->h.assertTrue(halted(p[0],mine[0]),"waiting for them to stop"))
            // A standing stone in the same row, the same way, flat out.
            .thenExecute(()->{
                mine[0].moveTo(b.getX()+at[0]+.5,b.getY()+3,b.getZ()+CalderGraves.BACK+3.5,0,0);p[0].setYRot(0);hush(p[0]);t[0]=0;most[0]=-9;
            })
            .thenWaitUntil(()->{
                if(p[0].getVehicle()==mine[0]){p[0].setYRot(0);pedalWell(mine[0],t[0]++,3);}
                if(mine[0].isAlive()){most[0]=Math.max(most[0],mine[0].speedNow());most[1]=Math.max(most[1],mine[0].getZ()-b.getZ());}
                h.assertTrue(p[0].getVehicle()==null,"waiting for them to ride into the stone and come off: "+(mine[0].isAlive()?rel(b,mine[0])+" going "+mine[0].speedNow()+", at most "+most[0]+", leaning "+mine[0].leanNow()+", on the ground "+mine[0].onGround()+", looking "+mine[0].getYRot():"the bike is gone"));
            })
            .thenExecute(()->{
                // The proposal: "A stone is a wall: ... a head-on hit puts the rider over the bars."
                h.assertTrue(most[0]>RiddenBike.CRASH&&most[1]<CalderGraves.ROWS[11]+6.5/16,"it was going "+most[0]+" and got no further than the stone's back: "+most[1]);
                h.assertTrue(CalderGraveBlock.is(stood(l,b,at[0],CalderGraves.ROWS[11]),CalderGraveBlock.Kind.STONE),"and the stone is where it was");
                // To a bike it is a block and a half; to anybody on foot it is as high as it looks, and the heap is steps.
                var stone=b.offset(at[0],3,CalderGraves.ROWS[11]);var heap=b.offset(CalderGraves.PIT_W,3,CalderGraves.HEAP);
                var onFoot=net.minecraft.world.phys.shapes.CollisionContext.of(p[0]);
                h.assertTrue(l.getBlockState(stone).getCollisionShape(l,stone,onFoot).max(Direction.Axis.Y)<1&&!l.getBlockState(heap).getCollisionShape(l,heap,onFoot).isEmpty(),"on foot a stone can be climbed, and the heap is stood on");
            }).thenSucceed();
    }
    @AfterBatch(batch="graves_ride") public static void gravesRideDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="graves_pit",timeoutTicks=9000)
    public static void theHeapByTheOpenGraveThrowsAFastBikeOverThePitAndASlowOneGoesInAndClimbsOutByTheStep(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final NightBike[] mine={null};final int[] t={0};final double[] low={99},high={-99};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(graves(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("graves_pit_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            .thenExecute(()->mine[0]=take(h,f,p[0],NightBike.Make.LAMP))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp"),"waiting for the Lamp to be theirs"))
            // On the top level, on the gate's path, fifteen blocks short of the heap.
            .thenExecute(()->{mine[0].moveTo(b.getX()-.5,b.getY()+3,b.getZ()+CalderGraves.HEAP+15.5,180,0);p[0].setYRot(180);})
            .thenWaitUntil(()->{
                if(p[0].getVehicle()==mine[0]){p[0].setYRot(180);pedalWell(mine[0],t[0]++,3);}
                double z=mine[0].getZ()-b.getZ(),y=mine[0].getY()-b.getY();
                if(z<CalderGraves.PIT_S+1&&z>CalderGraves.PIT_N){low[0]=Math.min(low[0],y);high[0]=Math.max(high[0],y);}
                h.assertTrue(p[0].getVehicle()==mine[0],"they are still on it: at "+rel(b,mine[0]));
                h.assertTrue(z<CalderGraves.PIT_N-.5&&mine[0].onGround(),"waiting for them to come down past the pit: at "+rel(b,mine[0])+" going "+mine[0].speedNow());
            })
            .thenExecute(()->{
                // The proposal: "a fresh open grave with a mound and a tarp, which is a hazard to jump"; "the mound is the ramp".
                h.assertTrue(low[0]>=3&&high[0]>3.75,"flat out, the heap threw them over the pit: over it they were never lower than "+low[0]+" and as high as "+high[0]);
                h.assertTrue(Math.abs(mine[0].getY()-b.getY()-3)<.02,"and they are down on the grass behind it: "+rel(b,mine[0]));
            })
            .thenWaitUntil(()->h.assertTrue(halted(p[0],mine[0]),"waiting for them to stop"))
            // Again from two blocks short of the heap, from standing.
            .thenExecute(()->{
                mine[0].moveTo(b.getX()-.5,b.getY()+3,b.getZ()+CalderGraves.HEAP+2.5,180,0);p[0].setYRot(180);t[0]=0;low[0]=99;
            })
            .thenIdle(10)
            .thenWaitUntil(()->{
                // At a walk: pushed only when it is going slower than one.
                if(p[0].getVehicle()==mine[0]){p[0].setYRot(180);if(mine[0].speedNow()<.12F)pedalWell(mine[0],t[0]++,3);}
                double z=p[0].getZ()-b.getZ(),y=p[0].getY()-b.getY();
                h.assertTrue(y<2.7&&z<CalderGraves.PIT_S+1&&z>CalderGraves.PIT_N&&(p[0].getVehicle()==null?p[0].onGround():mine[0].onGround()),"waiting for the slow one to go in: at "+rel(b,p[0])+" going "+(mine[0].isAlive()?mine[0].speedNow():0));
            })
            .thenExecute(()->{
                // "falling in costs time and nothing else": the bike's own rule for a landing is all there is to it, and the drop is not a hard one.
                h.assertTrue(p[0].getVehicle()==mine[0]&&p[0].getHealth()>=p[0].getMaxHealth(),"they are in it, still on the bike, and not hurt: health "+p[0].getHealth());
                // Out by the step, north.
                mine[0].moveTo(b.getX()-.5,b.getY()+1,b.getZ()+CalderGraves.PIT_S+.5,180,0);p[0].setYRot(180);t[0]=0;
            })
            .thenWaitUntil(()->{
                h.assertTrue(p[0].getVehicle()==mine[0],"they are on it in the pit: "+rel(b,mine[0]));
                p[0].setYRot(180);pedalWell(mine[0],t[0]++,3);
                h.assertTrue(mine[0].getZ()-b.getZ()<CalderGraves.PIT_N-.5&&Math.abs(mine[0].getY()-b.getY()-3)<.02,"waiting for them to ride up the step and out: at "+rel(b,mine[0])+" going "+mine[0].speedNow());
            })
            .thenExecute(()->{
                // And on foot: from the bottom, each rise is one block, which anybody jumps.
                int y=CalderGraves.level(CalderGraves.PIT_S);
                h.assertTrue(l.getBlockState(b.offset(CalderGraves.PIT_W,y-3,CalderGraves.PIT_S)).isCollisionShapeFullBlock(l,b)&&l.getBlockState(b.offset(CalderGraves.PIT_W,y-2,CalderGraves.PIT_N)).isCollisionShapeFullBlock(l,b)
                    &&l.getBlockState(b.offset(CalderGraves.PIT_W,y-1,CalderGraves.PIT_N-1)).isCollisionShapeFullBlock(l,b)&&l.getBlockState(b.offset(CalderGraves.PIT_W,y,CalderGraves.PIT_N-1)).isAir(),"the bottom, the step and the grass are a block apart each");
            }).thenSucceed();
    }
    @AfterBatch(batch="graves_pit") public static void gravesPitDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.490, what happens in the graveyard
    @GameTest(template="empty",batch="yard_gate",timeoutTicks=9000)
    public static void theCemeterysGateIsShutAllEveningAndStandsOpenOnceTheChaseIsOnAndNobodySeesItMove(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final int[] t={0};
        int rails=CalderTown.RAILS,w=CalderGraves.PATH_W,e=CalderGraves.PATH_E;
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(graves(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("yard_gate_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            // A reader on Church Lane, looking at the gate, all the while the three are in the park.
            .thenExecute(()->{f.at(p[0],P,.5,0,-171.5);p[0].setYRot(180);p[0].setXRot(0);})
            .thenWaitUntil(()->{
                p[0].setYRot(180);p[0].setXRot(0);
                h.assertTrue(!CalderGraves.gateIsOpen(l,b)&&!BikeNightGraveyard.gateOpen(b),"it is shut all evening");
                h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE,"waiting for the chase: stage "+BikeNightBullies.stage(b));
            })
            // The chase is on, and they are still looking at it: it does not move.
            .thenWaitUntil(()->{
                p[0].setYRot(180);p[0].setXRot(0);
                h.assertTrue(!CalderGraves.gateIsOpen(l,b),"it does not open while somebody is looking at it");
                h.assertTrue(++t[0]>=40,"waiting two seconds with the reader looking at it");
            })
            // They look away, down the lane.
            .thenExecute(()->{p[0].setYRot(0);hush(p[0]);})
            .thenWaitUntil(()->h.assertTrue(CalderGraves.gateIsOpen(l,b)&&BikeNightGraveyard.gateOpen(b),"waiting for it to be standing open"))
            .thenExecute(()->{
                // The answer: "Nobody says why." Nothing is said of it and nothing is heard.
                h.assertTrue(HEARD.getOrDefault(p[0].getUUID(),List.of()).isEmpty()||!heard(p[0],"gate"),"nothing is said of it");
                for(int x=w;x<=e;x++)for(int y=0;y<2;y++)h.assertTrue(l.getBlockState(b.offset(x,y,rails)).isAir(),"the gateway is empty at x="+x);
                for(int x:new int[]{w,e})for(int z=rails-2;z<=rails-1;z++)for(int y=0;y<2;y++){
                    var leaf=l.getBlockState(b.offset(x,y,z));
                    h.assertTrue(CalderBlock.is(leaf,CalderBlock.Kind.GATE)&&leaf.getValue(CalderBlock.FACING).getAxis()==Direction.Axis.X,"each leaf stands end on to the gateway, inside it: "+x+","+y+","+z+" is "+leaf);
                }
                // A bike gets through it from the lane to the path inside, and so does anybody.
                var o=Vec3.atLowerCornerOf(b);
                h.assertTrue(BikeNightGraveyard.clear(l,b,o.add(0,0,rails+2.5),o.add(0,0,rails-3.5)),"the way through is a clear ride");
                h.assertTrue(l.noCollision(new AABB(b.getX()-.9,b.getY()+.05,b.getZ()+rails-3,b.getX()+.9,b.getY()+1.8,b.getZ()+rails+2)),"and wide enough for two abreast");
                h.assertTrue(BikeNightGraveyard.in(b,o.add(0,0,rails-1.5))&&!BikeNightGraveyard.in(b,o.add(0,0,rails+1.5)),"inside the railings is in, the lane is not");
                // The night over, it is shut again, as it was built.
                BikeNight.resetNow(l,b);
                h.assertTrue(!CalderGraves.gateIsOpen(l,b),"shut again when the night is over");
                for(int x=w;x<=e;x++)h.assertTrue(CalderBlock.is(l.getBlockState(b.offset(x,0,rails)),CalderBlock.Kind.GATE)&&CalderBlock.is(l.getBlockState(b.offset(x,1,rails)),CalderBlock.Kind.GATE),"its leaves across the gateway at x="+x);
                for(int x:new int[]{w,e})for(int z=rails-2;z<=rails-1;z++)h.assertTrue(l.getBlockState(b.offset(x,0,z)).isAir(),"and nothing left standing inside it");
            }).thenSucceed();
    }
    @AfterBatch(batch="yard_gate") public static void yardGateDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="yard_three",timeoutTicks=12000)
    public static void oneOfTheThreeStopsAtTheFenceAndTheOthersComeInSlowerAndFallBackAndAreGoneWhenNobodyCanSeeThemGo(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] who={null,null,null};final PackBike[] bikes={null,null,null};
        final int[] t={0};final double[] fastest={0,0},north={0,0};var said=new ArrayList<String>();final String[] last={"","",""};final int[] after={-1};
        Runnable listen=()->{
            three(l,b,who,bikes);
            for(int i=0;i<3;i++){
                if(who[i]==null)continue;var now=saying(who[i]);
                if(!now.isEmpty()&&!now.equals(last[i]))said.add(now);
                last[i]=now;
                if(i<2&&bikes[i]!=null&&BikeNightGraveyard.in(b,bikes[i].position())){fastest[i]=Math.max(fastest[i],bikes[i].speedNow());north[i]=Math.min(north[i],bikes[i].getZ()-b.getZ());}
            }
        };
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(graves(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("yard_three_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            .thenExecute(()->f.at(p[0],P,-14.5,0,-20.5))
            .thenWaitUntil(()->{three(l,b,who,bikes);h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE&&BikeNightGraveyard.gateOpen(b),"waiting for the chase, and the gate: stage "+BikeNightBullies.stage(b));})
            // The reader is up the hill, on its third level, looking back down at the gate; the three are on Church Lane.
            .thenExecute(()->{
                f.at(p[0],P,-.5,2,-212.5);p[0].setYRot(0);p[0].setXRot(0);
                for(int i=0;i<3;i++)bikes[i].moveTo(b.getX()-6+6*i+.5,b.getY(),b.getZ()-170.5,180,0);
                north[0]=north[1]=0;
            })
            .thenWaitUntil(()->{
                p[0].setYRot(0);p[0].setXRot(0);listen.run();
                h.assertTrue(BikeNightGraveyard.state(b,2)==BikeNightGraveyard.STOPPED,"waiting for Duff to stop at the fence: he is "+BikeNightGraveyard.state(b,2)+" at "+(bikes[2]==null?"nowhere":rel(b,bikes[2])));
            })
            .thenExecute(()->{
                // The proposal: "one stops at the fence ('I'm not going in there')".
                h.assertTrue(said.contains(BikeNightTexts.NOT_GOING_IN)&&!BikeNightGraveyard.in(b,bikes[2].position())&&bikes[2].getZ()-b.getZ()<CalderTown.RAILS+3.5,"he says so, outside the gate: "+said+", at "+rel(b,bikes[2]));
            })
            .thenWaitUntil(()->{
                p[0].setYRot(0);p[0].setXRot(0);listen.run();
                h.assertTrue(BikeNightGraveyard.state(b,0)==BikeNightGraveyard.STOPPED&&BikeNightGraveyard.state(b,1)==BikeNightGraveyard.STOPPED,"waiting for the other two to come in and stop: Tully "+BikeNightGraveyard.state(b,0)+" at "+(bikes[0]==null?"nowhere":rel(b,bikes[0]))+", Ray-Ray "+BikeNightGraveyard.state(b,1)+" at "+(bikes[1]==null?"nowhere":rel(b,bikes[1])));
            })
            .thenExecute(()->{
                // "they come through the gate, slower": never faster in there than two thirds of the pace they hunt at.
                h.assertTrue(fastest[0]>.1&&fastest[1]>.1&&fastest[0]<=BikeNightGraveyard.PACE+.001&&fastest[1]<=BikeNightGraveyard.PACE+.001&&BikeNightGraveyard.PACE<BikeNightBullies.HUNT*.7F,"slower in there: Tully at most "+fastest[0]+", Ray-Ray "+fastest[1]);
                // "the other two fall back": each has only so much hill in him, Ray-Ray less than Tully, and neither reaches the reader.
                h.assertTrue(north[1]>BikeNightGraveyard.NERVE[1]-2&&north[0]>BikeNightGraveyard.NERVE[0]-2&&north[0]<north[1]-8,"Ray-Ray got as far as "+north[1]+" and Tully as "+north[0]);
                h.assertTrue(said.contains(BikeNightTexts.FALL_BACK[0])&&said.contains(BikeNightTexts.FALL_BACK[1]),"each says one thing to the other when he stops: "+said);
                h.assertTrue(!BikeNightBullies.caught(b,p[0]),"and the reader is not caught");
                after[0]=said.size();t[0]=0;
            })
            // They are watched: seven seconds, and they are all still there, and none of them says anything.
            .thenWaitUntil(()->{
                p[0].setYRot(0);p[0].setXRot(0);listen.run();
                for(int i=0;i<3;i++)h.assertTrue(!BikeNightBullies.gone(b,i)&&who[i]!=null&&bikes[i]!=null,"nobody is taken while somebody can see him: "+BikeNightBullies.NAMES[i]);
                h.assertTrue(++t[0]>=140,"waiting seven seconds with the reader watching them");
            })
            .thenExecute(()->{
                // "and then their jeers stop altogether."
                h.assertTrue(said.size()==after[0],"they say nothing more: "+said.subList(after[0],said.size()));
                for(int i=0;i<3;i++)h.assertTrue(bikes[i].speedNow()<.01F,BikeNightBullies.NAMES[i]+" has stopped where he is");
                // The reader turns to look up the hill.
                p[0].setYRot(180);
            })
            .thenWaitUntil(()->{
                p[0].setYRot(180);
                boolean all=true;for(int i=0;i<3;i++)all&=BikeNightBullies.gone(b,i)&&BikeNightBullies.find(l,b,i)==null&&BikeNightBullies.bikeOf(l,b,i)==null&&BikeNightGraveyard.state(b,i)==BikeNightGraveyard.TAKEN;
                h.assertTrue(all,"waiting for all three to be gone, and their bikes");
            })
            .thenWaitUntil(()->h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.GONE,"waiting for the night to know they are gone: stage "+BikeNightBullies.stage(b)))
            .thenExecute(()->{
                // "Nothing is shown happening to them": nothing was said or heard of it.
                h.assertTrue(said.size()==after[0]&&!BikeNightBullies.caught(b,p[0])&&p[0].getHealth()>=p[0].getMaxHealth(),"nothing more was said, and the reader is as they were");
                for(var line:List.of(BikeNightTexts.NOT_GOING_IN,BikeNightTexts.FALL_BACK[0],BikeNightTexts.FALL_BACK[1]))h.assertTrue(!heard(p[0],line),"what they say is never in chat: "+line);
            })
            // 0.4.509: "the bullies' three bikes lie abandoned by the front gate, and the bullies are nowhere." The reader is still
            // looking up the hill.
            .thenWaitUntil(()->{p[0].setYRot(180);h.assertTrue(BikeNightGraveyard.dropped(b)==3,"waiting for what they rode to be lying outside the gate: "+BikeNightGraveyard.dropped(b));})
            .thenExecute(()->{
                var lying=l.getEntitiesOfClass(PackBike.class,new AABB(b.getX()-14,b.getY()-1,b.getZ()+CalderTown.RAILS,b.getX()+14,b.getY()+3,b.getZ()+CalderTown.RAILS+8),e->e.isAlive()&&e.laid()!=0);
                h.assertTrue(lying.size()==3&&lying.stream().map(PackBike::look).distinct().count()==3&&lying.stream().allMatch(e->e.getPassengers().isEmpty()),"one of each of theirs, on its side, with nobody on it: "+lying.size());
                for(int i=0;i<3;i++)h.assertTrue(BikeNightBullies.find(l,b,i)==null,"and "+BikeNightBullies.NAMES[i]+" is nowhere");
            }).thenSucceed();
    }
    @AfterBatch(batch="yard_three") public static void yardThreeDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="yard_gus",timeoutTicks=12000)
    public static void gusComesThroughTheStonesAfterHisFriendAndSaysNothingAtTheRow(GameTestHelper h){
        var f=park(h);BikeNightGus.keptHome=false;BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] gus={null};final PackBike[] his={null};
        var said=new ArrayList<String>();final String[] last={""};final int[] t={0},held={0},after={-1};
        Runnable listen=()->{var now=saying(gus[0]);if(!now.isEmpty()&&!now.equals(last[0]))said.add(now);last[0]=now;if(his[0].heldUp())held[0]++;};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(graves(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("yard_gus_reader",P))
            .thenWaitUntil(()->{gus[0]=BikeNightGus.find(l,b);his[0]=BikeNightGus.bikeOf(l,b);
                h.assertTrue(BikeNight.begun(b)&&gus[0]!=null&&his[0]!=null&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night, Gus, his bike, and the reader to be the park's size");})
            .thenExecute(()->take(h,f,p[0],NightBike.Make.WASP))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("wasp"),"waiting for the Wasp to be theirs"))
            .thenExecute(()->f.at(p[0],P,-14.5,0,-20.5))
            .thenWaitUntil(()->{listen.run();h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE&&BikeNightGus.doing(b)==BikeNightGus.RIDING&&BikeNightGraveyard.gateOpen(b),"waiting for the chase, the gate, and for him to be on his bike: stage "+BikeNightBullies.stage(b)+", doing "+BikeNightGus.doing(b));})
            // The reader is just inside the gate; Gus is on the lane outside it.
            .thenExecute(()->{f.at(p[0],P,-.5,0,CalderTown.RAILS-8.5);his[0].moveTo(b.getX()+.5,b.getY(),b.getZ()+CalderTown.RAILS+4.5,180,0);said.clear();held[0]=0;})
            .thenWaitUntil(()->{
                listen.run();
                h.assertTrue(BikeNightGraveyard.in(b,his[0].position())&&his[0].position().distanceTo(p[0].position())<BikeNightGus.KEEP+1.5&&said.contains(BikeNightTexts.GUS_YARD),"waiting for him to come in by the gate after them, and say where they are: at "+rel(b,his[0])+", said "+said);
            })
            // Up the first bank.
            .thenExecute(()->f.at(p[0],P,-.5,1,CalderGraves.BANKS[0]-6.5))
            .thenWaitUntil(()->{
                listen.run();
                h.assertTrue(his[0].getZ()-b.getZ()<CalderGraves.BANKS[0]&&his[0].position().distanceTo(p[0].position())<BikeNightGus.KEEP+1.5&&said.contains(BikeNightTexts.GUS_YARD_ON),"waiting for him up the first bank beside them, saying his other thing: at "+rel(b,his[0])+", said "+said);
            })
            // And to the row of small stones, behind every other row.
            .thenExecute(()->{f.at(p[0],P,-.5,3,CalderGraves.SMALL_ROW+2.5);held[0]=0;})
            .thenWaitUntil(()->{
                listen.run();
                h.assertTrue(BikeNightGraveyard.hushed(b,his[0].position())&&his[0].position().distanceTo(p[0].position())<BikeNightGus.KEEP+1.5&&his[0].speedNow()<.01F,"waiting for him to be beside his friend at the row: at "+rel(b,his[0])+" going "+his[0].speedNow()+", held up for "+held[0]+" ticks");
            })
            .thenExecute(()->{
                // Through twelve rows of stones, round the open grave, and up three banks: weaving, not butting his way.
                h.assertTrue(Math.abs(his[0].getY()-b.getY()-3)<.05&&held[0]<60,"he is on the top of the hill, and was held up for "+held[0]+" ticks on the way");
                after[0]=said.size();t[0]=0;
                // A hand on him.
                BikeNightGus.talk(p[0],gus[0]);
            })
            // The answer: "At the row he says nothing."
            .thenWaitUntil(()->{
                listen.run();
                h.assertTrue(saying(gus[0]).isEmpty()||said.size()==after[0],"he says nothing at the row: "+said.subList(Math.min(after[0],said.size()),said.size()));
                if(t[0]==100)BikeNightGus.talk(p[0],gus[0]);
                h.assertTrue(++t[0]>=300,"waiting fifteen seconds at the row");
            })
            .thenExecute(()->{
                h.assertTrue(said.size()==after[0],"not of his own accord and not to a hand on him: "+said.subList(after[0],said.size()));
                for(var line:List.of(BikeNightTexts.GUS_YARD,BikeNightTexts.GUS_YARD_ON))h.assertTrue(!heard(p[0],line),"what he says is never in chat: "+line);
            }).thenSucceed();
    }
    @AfterBatch(batch="yard_gus") public static void yardGusDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.491, the mist and the town's lights
    /** What a reader has just been told of their mist, the newest of it; or -1 if they have been told nothing since this was last asked. */
    private static int toldMist(ServerPlayer p){
        int last=-1;
        for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom&&custom.payload() instanceof io.github.knaitoe.theoldesthouse.network.BikeNightMistPayload mist)last=mist.mist();
        return last;
    }
    private static List<BlockPos> townLamps(ServerLevel l,BlockPos b){return BikeNightGraveyard.townLamps(l,b);}

    @GameTest(template="empty",batch="yard_mist",timeoutTicks=9000)
    public static void theMistIsEachReadersOwnThickerWithEachRowAndALampThatIsMakingLightCutsItForWhoeverIsNear(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final NightBike[] mine={null};final int[] t={0},seen={-1,-1,-1},n={0};
        int full=BikeNightGraveyard.MIST_BACK,gate=BikeNightGraveyard.MIST_GATE;
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(graves(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{p[0]=f.player("yard_mist_rider",P);p[1]=f.player("yard_mist_walker",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0])&&BikeNight.here(p[1]),"waiting for the night"))
            .thenExecute(()->mine[0]=take(h,f,p[0],NightBike.Make.LAMP))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp"),"waiting for the Lamp to be theirs"))
            .thenExecute(()->{
                // The proposal: "It rises off the grass and thickens with each row." None outside the railings; some just inside them; more past each row; all of it past the last.
                f.at(p[1],P,.5,0,CalderTown.RAILS+3.5);
                h.assertTrue(BikeNightGraveyard.mist(l,b,p[1])==0,"none on the lane");
                f.at(p[1],P,.5,0,CalderTown.RAILS-1.5);
                int last=BikeNightGraveyard.mist(l,b,p[1]);
                h.assertTrue(last==gate&&gate>0&&gate<30,"some just inside the gate: "+last);
                for(int row:CalderGraves.ROWS){
                    f.at(p[1],P,.5,CalderGraves.level(row+1),row+1.5);
                    h.assertTrue(BikeNightGraveyard.mist(l,b,p[1])==last,"no thicker until the row at "+row+" is passed");
                    f.at(p[1],P,.5,CalderGraves.level(row-1),row-.5);
                    int now=BikeNightGraveyard.mist(l,b,p[1]);
                    h.assertTrue(now>last&&now-last>=6&&now-last<=8,"thicker past the row at "+row+": "+last+" to "+now);
                    last=now;
                }
                h.assertTrue(last==full&&full==100,"all of it past the last row: "+last);
                // And the reader is told, and nobody else is told theirs.
                f.at(p[1],P,.5,3,CalderGraves.SMALL_ROW+1.5);toldMist(p[0]);toldMist(p[1]);
            })
            .thenWaitUntil(()->{int told=toldMist(p[1]);if(told>=0)seen[0]=told;int other=toldMist(p[0]);if(other>0)seen[1]=other;
                h.assertTrue(seen[0]==full,"waiting for the reader at the back to be told all of it: "+seen[0]);})
            .thenExecute(()->{
                // As answered: "The mist is each reader's own." The one still in the park has been told nothing of it.
                h.assertTrue(seen[1]<0,"the reader in the park is told of no mist: "+seen[1]);
                f.at(p[1],P,.5,0,CalderTown.RAILS+3.5);seen[0]=-1;
            })
            .thenWaitUntil(()->{int told=toldMist(p[1]);if(told>=0)seen[0]=told;h.assertTrue(seen[0]==0,"waiting for them to be told it is gone, out on the lane: "+seen[0]);})
            .thenExecute(()->{t[0]=0;n[0]=0;})
            .thenWaitUntil(()->{if(toldMist(p[1])>=0)n[0]++;h.assertTrue(++t[0]>=60,"waiting three seconds on the lane");})
            .thenExecute(()->{
                h.assertTrue(n[0]==0,"and are told nothing more of it while they are out of it: "+n[0]+" times");
                // The proposal: "The Lamp's headlight cuts it." Along the top path across the hill, from the west, with a reader standing by the path.
                int row=CalderGraves.ACROSS[3];
                mine[0].moveTo(b.getX()-24.5,b.getY()+3,b.getZ()+row+.5,-90,0);p[0].setYRot(-90);
                f.at(p[1],P,.5,3,row-1.5);t[0]=0;seen[0]=seen[1]=seen[2]=-1;
            })
            .thenWaitUntil(()->{
                if(p[0].getVehicle()==mine[0]){p[0].setYRot(-90);pedalWell(mine[0],t[0]++,3);}
                double x=mine[0].getX()-b.getX();int walker=BikeNightGraveyard.mist(l,b,p[1]),rider=BikeNightGraveyard.mist(l,b,p[0]);
                // The walker's, with the Lamp far off to the west, beside them, and gone on to the east; the rider's, once it is making light.
                if(x<-12&&seen[0]<0)seen[0]=walker;
                if(Math.abs(x)<3&&mine[0].lit()>0&&seen[1]<0){seen[1]=walker;seen[2]=rider;}
                h.assertTrue(p[0].getVehicle()==mine[0],"they are still on it: "+rel(b,mine[0]));
                h.assertTrue(x>14,"waiting for the Lamp to ride past the walker and on: at "+rel(b,mine[0])+" going "+mine[0].speedNow()+", lit "+mine[0].lit());
            })
            .thenExecute(()->{
                int walker=BikeNightGraveyard.mist(l,b,p[1]),cut=Math.round(seen[0]*BikeNightGraveyard.LAMP_CUTS);
                h.assertTrue(seen[0]>80&&seen[0]<full,"with the Lamp far off the walker's mist is nearly all of it: "+seen[0]);
                h.assertTrue(seen[1]==cut&&seen[2]==cut&&cut<seen[0]/2,"beside it, theirs and its rider's are under half of that: "+seen[1]+" and "+seen[2]+" of "+seen[0]);
                h.assertTrue(walker==seen[0],"and when it has gone on, theirs is what it was: "+walker);
                h.assertTrue(BikeNightGraveyard.lamp(l,p[0]),"its rider has it still");
            })
            // "Its dynamo makes light only while the wheel turns": stopped, it cuts nothing.
            .thenWaitUntil(()->h.assertTrue(halted(p[0],mine[0])&&mine[0].lit()==0,"waiting for the Lamp to stop and go dark: going "+mine[0].speedNow()+", lit "+mine[0].lit()))
            .thenExecute(()->h.assertTrue(!BikeNightGraveyard.lamp(l,p[0])&&BikeNightGraveyard.mist(l,b,p[0])==seen[0],"a Lamp standing still cuts nothing: "+BikeNightGraveyard.mist(l,b,p[0])))
            .thenSucceed();
    }
    @AfterBatch(batch="yard_mist") public static void yardMistDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="yard_dark",timeoutTicks=12000)
    public static void theTownsLampsGoOutAStreetAtATimeOnceEveryReaderStillInTheNightIsOnTheHill(GameTestHelper h){
        var f=park(h);BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final int[] t={0},dark={0};final long[] at={0,0,0,0,0};var lamps=new ArrayList<BlockPos>();
        java.util.function.IntFunction<int[]> lit=street->{int on=0,all=0;int south=BikeNightGraveyard.STREETS[street],north=street==0?Integer.MIN_VALUE:BikeNightGraveyard.STREETS[street-1];
            for(var lamp:lamps)if(lamp.getZ()<south&&lamp.getZ()>=north){all++;if(BikeNightSite.lit(l,b,lamp))on++;}return new int[]{on,all};};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(graves(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{p[0]=f.player("yard_dark_first",P);p[1]=f.player("yard_dark_second",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0])&&BikeNight.here(p[1]),"waiting for the night"))
            .thenExecute(()->{
                f.at(p[0],P,-14.5,0,-20.5);f.at(p[1],P,-12.5,0,-20.5);lamps.addAll(townLamps(l,b));
                h.assertTrue(lamps.size()>=30,"the town has its lamps: "+lamps.size());
                // A street's name hangs on a post with a lamp's foot and no lantern: it is not a lamp, and nothing is put out on it.
                int feet=0;
                for(int x=CalderTown.W;x<=CalderTown.E;x++)for(int z=CalderTown.N;z<BikeNightSite.NORTH;z++){var s=l.getBlockState(b.offset(x,0,z));if(CalderBlock.is(s,CalderBlock.Kind.LAMP)&&s.getValue(CalderBlock.STAGE)==CalderBlock.FOOT)feet++;}
                h.assertTrue(feet>lamps.size(),"the posts the streets' names hang on are not lamps: "+feet+" feet, "+lamps.size()+" lamps");
                for(int i=0;i<BikeNightGraveyard.STREETS.length;i++){var one=lit.apply(i);h.assertTrue(one[1]>=3&&one[0]==one[1],"street "+i+" has lamps and they are lit: "+one[0]+" of "+one[1]);}
            })
            .thenWaitUntil(()->h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE&&BikeNightGraveyard.gateOpen(b),"waiting for the chase, and the gate: stage "+BikeNightBullies.stage(b)))
            // One reader is up the hill, past its first bank. The other is still down in the town, at the far end of Church Lane.
            .thenExecute(()->{f.at(p[0],P,-.5,1,CalderGraves.BANKS[0]-6.5);f.at(p[1],P,-40.5,0,-164.5);t[0]=0;})
            .thenWaitUntil(()->{
                h.assertTrue(BikeNightGraveyard.dark(b)==0,"nothing goes out while a reader is still in the town");
                h.assertTrue(++t[0]>=100,"waiting five seconds with one reader in the town");
            })
            .thenExecute(()->{
                for(var lamp:lamps)h.assertTrue(BikeNightSite.lit(l,b,lamp),"every lamp is lit: "+lamp.toShortString());
                // They come up the hill too.
                f.at(p[1],P,2.5,1,CalderGraves.BANKS[0]-6.5);hush(p[0]);hush(p[1]);
            })
            .thenWaitUntil(()->{
                int now=BikeNightGraveyard.dark(b);
                if(now>dark[0]){
                    at[now]=l.getGameTime();dark[0]=now;
                    // The streets north of this one are dark, this one has just gone, and the ones south of it are lit.
                    for(int i=0;i<BikeNightGraveyard.STREETS.length;i++){var one=lit.apply(i);
                        h.assertTrue(i<now?one[0]==0:one[0]==one[1],"with "+now+" streets dark, street "+i+" has "+one[0]+" of "+one[1]+" lamps lit");}
                    if(now>1)h.assertTrue(at[now]-at[now-1]>=BikeNightGraveyard.DARK_EVERY,"a street at a time: "+(at[now]-at[now-1])+" ticks after the last");
                }
                h.assertTrue(now==BikeNightGraveyard.STREETS.length,"waiting for the town's streets to go dark, from the cemetery down: "+now);
            })
            .thenExecute(()->{
                for(var lamp:lamps)h.assertTrue(!BikeNightSite.lit(l,b,lamp),"every lamp of the town is out: "+lamp.toShortString());
                // The park's are the park's.
                h.assertTrue(BikeNightSite.lit(l,b,BikeNightSite.PLAY_LAMP)&&BikeNightSite.lit(l,b,BikeNightSite.RACK_LAMP),"the park's lamps are lit");
                // Each street's breaker is heard on the hill.
                h.assertTrue(sounded(p[0],"town.breaker")&&sounded(p[1],"town.breaker"),"they hear it go");
                // The night over, the town is lit again.
                BikeNight.resetNow(l,b);
                for(var lamp:lamps)h.assertTrue(BikeNightSite.lit(l,b,lamp),"lit again when the night is over: "+lamp.toShortString());
                h.assertTrue(BikeNightGraveyard.dark(b)==0,"and nothing is dark");
            }).thenSucceed();
    }
    @AfterBatch(batch="yard_dark") public static void yardDarkDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.494, the Hollow's plain way
    private static AABB hollow(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()+CalderGraves.W-1,b.getY()-6,b.getZ()+CalderHollow.FAR-1,b.getX()+CalderGraves.E+2,b.getY()+CalderGraves.TOP+2,b.getZ()+CalderGraves.SMALL_ROW+8);}

    @GameTest(template="empty",batch="hollow_built",timeoutTicks=2400)
    public static void pastTheHillsEdgeThereIsNothingButThePlainWayWhichNoGapOfBreaksForMoreThanATapClearsButOne(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,hollow(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the Hollow is resident"))
            .thenExecute(()->{
                var way=CalderHollow.WAY;int top=CalderGraves.level(CalderHollow.EDGE);
                // The proposal: "a course of floating ground, high and low, over nothing". From the hill's edge to the far side, on the
                // gate's line and well to either side of it, there is the way and nothing else.
                int stray=0;String strays="";
                for(int x=-12;x<=12;x++)for(int z=CalderHollow.FAR_SIDE+1;z<=CalderHollow.EDGE-1;z++)for(int y=-4;y<=CalderGraves.TOP;y++){
                    // Off the gate's line the hill's own edge is ragged, a row or three further: that is the hill's, not the way's.
                    if(Math.abs(x)>6&&z>=CalderHollow.EDGE-3)continue;
                    var s=l.getBlockState(b.offset(x,y,z));if(s.isAir())continue;
                    double here=CalderHollow.top(x,z);
                    // What a piece is: its ground, what hangs under it, what stands on it.
                    boolean part=!Double.isNaN(here)&&y>=Math.floor(here)-2&&y<=Math.floor(here)+1;
                    // The steep way's bank is earth down to its foot.
                    part|=CalderHollow.bankRow(x+.5,z+.5)>=0&&y>=CalderHollow.BANK_FOOT-1&&y<here;
                    // The tight way's tendril has walls on it, and gates.
                    part|=CalderHollow.tendrilRow(x+.5,z+.5)>=0&&y>=0&&y<CalderHollow.GATE_HIGH;
                    if(!part&&stray++<4)strays+=" "+s.getBlock()+" at "+x+","+y+","+z;
                    h.assertTrue(mine(s),"nothing vanilla floats there: "+s+" at "+x+","+y+","+z);
                }
                h.assertTrue(stray==0,"nothing but the way: "+stray+strays);
                // The hill ends on the gate's line at its edge, with the way's first piece two blocks off.
                for(int x=-3;x<=3;x++)h.assertTrue(!l.getBlockState(b.offset(x,top-1,CalderHollow.EDGE)).isAir()&&l.getBlockState(b.offset(x,top-1,CalderHollow.EDGE-1)).isAir()&&l.getBlockState(b.offset(x,top-1,CalderHollow.EDGE-2)).isAir(),"the hill's ground stops at its edge at x="+x);
                h.assertTrue(way.get(0).zSouth()==CalderHollow.EDGE-3&&way.get(0).top()==top-1,"the first piece is two blocks off the edge, a block below the hill");
                // Piece to piece: never more than three blocks of nothing, and three only once; never up by more than a step; always in line.
                int threes=0,twos=0,ones=0,level=0;double highest=0,lowest=99;
                for(int i=0;i+1<way.size();i++){
                    var a=way.get(i);var c=way.get(i+1);int gap=a.zNorth()-c.zSouth()-1;
                    h.assertTrue(gap>=0&&gap<=3,"from "+a.name()+" to "+c.name()+" is "+gap+" blocks of nothing");
                    if(gap==3)threes++;if(gap==2)twos++;if(gap==1)ones++;
                    // The three comes down a block; a two that does not come down is the harder kind, and there are few.
                    if(gap==3)h.assertTrue(c.top()<=a.top()-1,"the gap of three comes down a block: "+a.top()+" to "+c.top());
                    if(gap==2&&c.top()>=a.top())level++;
                    h.assertTrue(c.top()<=a.top()+.5,"nothing is reached by jumping up: "+a.name()+" at "+a.top()+" to "+c.name()+" at "+c.top());
                    if(gap==0)h.assertTrue(Math.abs(c.top()-a.top())<=.5,"what touches is a step apart at the most: "+a.name()+" and "+c.name());
                    int shared=Math.min(a.x1(),c.x1())-Math.max(a.x0(),c.x0())+1;
                    // Over a gap, by two blocks at least; where they touch (the vine swings a block at a time), by one.
                    h.assertTrue(shared>=(gap==0?1:2),"each piece is in line with the last: "+a.name()+" and "+c.name()+" share "+shared+" over "+gap);
                    highest=Math.max(highest,c.top());lowest=Math.min(lowest,c.top());
                }
                h.assertTrue(threes==1&&twos>=6&&ones==5,"one gap wants the hop held, the lids have a block between them, the rest a tap clears: "+threes+" of three, "+twos+" of two, "+ones+" of one");
                h.assertTrue(level<=3,"most gaps of two come down, to give a hop time to carry: "+level+" do not");
                h.assertTrue(highest==5&&lowest==0,"\"high and low\": from "+lowest+" to "+highest);
                var last=way.get(way.size()-1);
                h.assertTrue(last.zNorth()==CalderHollow.FAR_SIDE+1&&last.top()==top,"the last piece runs onto the far side at its height");
                // Each piece is what it is made of.
                var sod=BikeNightRegistry.material("hollow_sod");var vine=BikeNightRegistry.material("vine");int roots=0,kinds=0;
                for(var piece:way){
                    int y=(int)Math.floor(piece.top());boolean whole=piece.top()==y;
                    var s=l.getBlockState(b.offset(piece.x0(),whole?y-1:y,piece.zNorth()));
                    boolean ok=switch(piece.made()){
                        case SOD->whole?s.is(sod):s.is(BikeNightRegistry.GRAVE_TURF_SLAB.get());
                        case LID->s.is(BikeNightRegistry.COFFIN_LID.get())&&s.getValue(net.minecraft.world.level.block.SlabBlock.TYPE)==net.minecraft.world.level.block.state.properties.SlabType.TOP;
                        case VINE->whole?s.is(vine):s.is(BikeNightRegistry.VINE_SLAB.get());
                        case STONE->s.is(BikeNightRegistry.STONE_STEP.get());
                        case EARTH,BANK->s.is(BikeNightRegistry.material("hollow_earth"));
                        case TENDRIL->s.is(vine);};
                    h.assertTrue(ok,piece.name()+" is made of "+piece.made()+": "+s);
                    // Where a body stands on it is where the piece says.
                    var at=b.offset(piece.x0(),whole?y-1:y,piece.zNorth());
                    h.assertTrue(Math.abs(at.getY()+s.getCollisionShape(l,at).max(Direction.Axis.Y)-b.getY()-piece.top())<.001,piece.name()+" is stood on at "+piece.top());
                    if(piece.made()==CalderHollow.Made.SOD&&whole)for(int x=piece.x0();x<=piece.x1();x++)for(int z=piece.zNorth();z<=piece.zSouth();z++)if(CalderGraveBlock.is(l.getBlockState(b.offset(x,y-2,z)),CalderGraveBlock.Kind.ROOTS))roots++;
                }
                for(var made:CalderHollow.Made.values()){boolean any=false;for(var piece:way)any|=piece.made()==made;if(any)kinds++;}
                h.assertTrue(roots>=30&&kinds==4,"turf with roots under it, lids, the vine, and stone stairs: "+roots+" roots, "+kinds+" kinds");
                // "islands of graveyard turf with their headstones still in them; crooked fences".
                int stones=0,pickets=0;
                for(int x=-6;x<=6;x++)for(int z=CalderHollow.FAR_SIDE+1;z<=CalderHollow.EDGE-1;z++){double here=CalderHollow.top(x,z);if(Double.isNaN(here))continue;
                    var s=l.getBlockState(b.offset(x,(int)here,z));if(CalderGraveBlock.is(s,CalderGraveBlock.Kind.STONE)||CalderGraveBlock.is(s,CalderGraveBlock.Kind.CROSS))stones++;if(CalderGraveBlock.is(s,CalderGraveBlock.Kind.PICKET))pickets++;}
                h.assertTrue(stones>=8&&pickets>=6,"stones still in the graves and fences across the strip: "+stones+" and "+pickets);
                // The far side: the cemetery's ground again, its railings whole, the back gate shut between two piers, and the lane behind.
                var brick=BikeNightRegistry.material("brick");int far=CalderHollow.BACK;
                for(int x=-CalderHollow.SIDE-1;x<=CalderHollow.SIDE+1;x++){
                    var at=b.offset(x,top,far);var s=l.getBlockState(at);
                    h.assertTrue(!s.getCollisionShape(l,at).isEmpty(),"the back railings are whole at x="+x);
                    if(x>=CalderGraves.PATH_W&&x<=CalderGraves.PATH_E)h.assertTrue(CalderBlock.is(s,CalderBlock.Kind.GATE)&&CalderBlock.is(l.getBlockState(at.above()),CalderBlock.Kind.GATE),"the back gate, shut, at x="+x);
                }
                h.assertTrue(l.getBlockState(b.offset(CalderGraves.PATH_W-1,top,far)).is(brick)&&l.getBlockState(b.offset(CalderGraves.PATH_E+1,top,far)).is(brick),"it hangs between two piers");
                for(int z=CalderHollow.FAR_SIDE;z>far;z--)for(int x:new int[]{-CalderHollow.SIDE-1,CalderHollow.SIDE+1}){var at=b.offset(x,top,z);h.assertTrue(!l.getBlockState(at).getCollisionShape(l,at).isEmpty(),"the far side's railings are whole at "+x+","+z);}
                h.assertTrue(l.getBlockState(b.offset(0,top-1,CalderHollow.LANE_S)).is(BikeNightRegistry.material("asphalt"))&&l.getBlockState(b.offset(0,top-1,far+3)).is(BikeNightRegistry.material("gravel")),"gravel to the gate, and the lane behind it");
            }).thenSucceed();
    }
    @AfterBatch(batch="hollow_built") public static void hollowBuiltDone(ServerLevel l){done();}

    /** Where a rider on the plain way steers for, by how far north they are: the middle of the piece ahead, and round the fences. */
    private static double hollowAim(double z){
        if(z> -264)return 0;
        if(z<=-291&&z> -299)return 1.5;
        if(z<=-299&&z> -307)return -1;
        if(z<=-307&&z>=-314)return .5;
        // The piece a little ahead, or the one after the gap.
        for(var piece:CalderHollow.WAY)if(z-2.5<=piece.zSouth()+1&&z-2.5>=piece.zNorth())return (piece.x0()+piece.x1()+1)/2.0;
        for(var piece:CalderHollow.WAY)if(piece.zSouth()+1<z-2.5)return (piece.x0()+piece.x1()+1)/2.0;
        return 0;
    }
    /** How wide the nothing ahead of a rider is, and how far off: {width, distance to it}; or {0, 99} with none within six blocks. */
    private static double[] hollowGap(double z){
        var way=CalderHollow.WAY;
        if(z>CalderHollow.EDGE-2)return new double[]{2,z-(CalderHollow.EDGE)};
        for(int i=0;i+1<way.size();i++){
            var a=way.get(i);var c=way.get(i+1);int gap=a.zNorth()-c.zSouth()-1;
            if(gap>0&&z>=a.zNorth()&&z<=a.zSouth()+1&&z-a.zNorth()<6)return new double[]{gap,z-a.zNorth()};
        }
        return new double[]{0,99};
    }

    @GameTest(template="empty",batch="hollow_ride",timeoutTicks=9000)
    public static void aPlainBikeIsRiddenTheWholeOfThePlainWayFromTheHillToTheFarSide(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final NightBike[] mine={null};final int[] t={0},hops={0,0};final double[] lowest={99},highest={0};
        final boolean[] held={false};var crossed=new java.util.TreeSet<String>();
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("hollow_ride_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            .thenExecute(()->mine[0]=take(h,f,p[0],NightBike.Make.LAMP))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp"),"waiting for the Lamp to be theirs"))
            // On the hill's top, on the gate's line, a little behind the small stones.
            .thenExecute(()->{mine[0].moveTo(b.getX()+.0,b.getY()+3,b.getZ()+CalderGraves.SMALL_ROW+3.5,180,0);p[0].setYRot(180);})
            .thenWaitUntil(()->{
                h.assertTrue(p[0].getVehicle()==mine[0],"they are still on it: at "+rel(b,mine[0])+", over "+crossed);
                double x=mine[0].getX()-b.getX(),z=mine[0].getZ()-b.getZ(),y=mine[0].getY()-b.getY();
                lowest[0]=Math.min(lowest[0],y);highest[0]=Math.max(highest[0],y);
                h.assertTrue(y>-1.5,"they have not fallen off the way: at "+rel(b,mine[0])+", over "+crossed);
                // Steering: at the middle of what is ahead.
                double aim=hollowAim(z);float yaw=(float)Math.toDegrees(Math.atan2(-(aim-x),-3.5));
                p[0].setYRot(mine[0].onGround()?yaw:mine[0].getYRot());
                // The hop: a tap at the brink of two blocks of nothing; held from four blocks short of three, and let go at its brink.
                var gap=hollowGap(z);boolean brink=gap[1]<=.4&&mine[0].onGround();
                if(gap[0]>=3){
                    if(gap[1]<=4.3&&!held[0]&&mine[0].onGround()){mine[0].keys(true,false);held[0]=true;}
                    if(held[0]&&brink){mine[0].keys(false,false);held[0]=false;hops[1]++;}
                }else if(gap[0]>=2&&brink&&!held[0]){mine[0].keys(true,false);held[0]=true;hops[0]++;}
                else if(held[0]&&gap[0]<3){mine[0].keys(false,false);held[0]=false;}
                if(mine[0].onGround())pedalWell(mine[0],t[0]++,3);
                for(var piece:CalderHollow.WAY)if(mine[0].onGround()&&piece.has((int)Math.floor(x),(int)Math.floor(z)))crossed.add(piece.name().replaceAll(" [0-9]+$",""));
                h.assertTrue(z<CalderHollow.FAR_SIDE-3,"waiting for them to ride the way to the far side: at "+rel(b,mine[0])+" going "+mine[0].speedNow()+", over "+crossed);
            })
            .thenExecute(()->{
                h.assertTrue(Math.abs(mine[0].getY()-b.getY()-3)<.05&&mine[0].onGround(),"they are on the far side's ground: "+rel(b,mine[0]));
                h.assertTrue(crossed.containsAll(java.util.List.of("first grave","second grave","third grave","the fences","lid","the grave past the lids","vine","stair","the top","stretch")),"over every part of it: "+crossed);
                h.assertTrue(highest[0]>4.9&&lowest[0]>=-.1,"as high as the top and never under the lowest piece: "+lowest[0]+" to "+highest[0]);
                h.assertTrue(hops[1]==1&&hops[0]>=6,"with a tap at each gap of two and the one hop held: "+hops[0]+" taps, "+hops[1]+" held");
            }).thenSucceed();
    }
    @AfterBatch(batch="hollow_ride") public static void hollowRideDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.495, somebody who misses
    /** Where somebody is set down on an island, in the world. */
    private static Vec3 islandPlace(BlockPos b,int island){return CalderHollow.ISLANDS.get(island).place().add(b.getX(),b.getY(),b.getZ());}
    private static boolean mistOver(ServerPlayer p){drain(p);return OVER.getOrDefault(p.getUUID(),0)>0;}
    /** Where a reader is, from the park's base. */
    private static String where(BlockPos b,net.minecraft.world.entity.Entity e){return String.format("%.2f, %.2f, %.2f",e.getX()-b.getX(),e.getY()-b.getY(),e.getZ()-b.getZ());}

    @GameTest(template="empty",batch="hollow_islands",timeoutTicks=2400)
    public static void theHollowsIslandsAreTheHillEachRunOfGroundLongEnoughToGetGoingOnAndTheFarSideAndEachHasAPlaceToBeSetDown(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                var islands=CalderHollow.ISLANDS;var names=islands.stream().map(CalderHollow.Island::name).toList();
                h.assertTrue(names.equals(List.of("the hill","first grave","second grave","third grave","the fences","the grave past the lids","vine","stair and the top","stretch","stretch","the far side",
                    "high grave","high grave","high grave and the high way's foot","long step and the long way's foot","the steep way's turn and the loose earth","the steep way's head","the tight way's step and the tight way's foot","the dark way's first island","the dark way's second island","the dark way's third island")),"the islands: the plain way's in the order they are ridden, the far side, then the other ways' in turn: "+names);
                h.assertTrue(islands.get(CalderHollow.FAR_ISLAND).name().equals("the far side"),"the far side is where it says");
                h.assertTrue(CalderHollow.HILL==0&&islands.get(0).name().equals("the hill"),"the hill is the first");
                // The lids are a bridge: somebody standing on one has no island under them.
                for(var piece:CalderHollow.WAY)if(piece.made()==CalderHollow.Made.LID)h.assertTrue(CalderHollow.stood((piece.x0()+piece.x1()+1)/2.0,piece.top(),(piece.zSouth()+piece.zNorth()+1)/2.0)==-1,piece.name()+" is not an island");
                for(int i=0;i<islands.size();i++){
                    var island=islands.get(i);var at=islandPlace(b,i);String what=island.name()+" ("+i+")";
                    // A run short of its far end, or a block in from its near end where it is shorter than that.
                    if(i!=CalderHollow.HILL&&i!=CalderHollow.FAR_ISLAND){
                        h.assertTrue(island.zNear()-island.zFar()+1>=CalderHollow.STAND,what+" is long enough to get going on");
                        h.assertTrue(island.z()-island.zFar()==Math.min(CalderHollow.RUN,island.zNear()-1-island.zFar()),what+": its place is a run short of its far end, or a block in from its near end: "+island);
                    }
                    h.assertTrue(CalderHollow.stood(island.x(),island.top(),island.z()+.5)==i,what+": somebody at its place is standing on it");
                    // Ground under it, and room for a rider over it.
                    h.assertTrue(!l.noCollision(new AABB(at.x-.25,at.y-.2,at.z-.25,at.x+.25,at.y-.02,at.z+.25)),what+": there is ground under its place: "+island);
                    h.assertTrue(l.noCollision(new AABB(at.x-.3,at.y+.05,at.z-.3,at.x+.3,at.y+1.8,at.z+.3)),what+": its place is clear: "+island);
                    // And somewhere by it to lay a bike: on whole ground, not where they stand, no more than four rows behind.
                    var laid=BikeNightHollow.beside(l,at);
                    h.assertTrue(laid!=null&&l.getBlockState(laid).isAir()&&l.getBlockState(laid.below()).isFaceSturdy(l,laid.below(),Direction.UP),what+": there is somewhere by its place to lay a bike: "+laid);
                    h.assertTrue(laid.getZ()>=Mth.floor(at.z)&&laid.getZ()-Mth.floor(at.z)<=4&&Math.abs(laid.getX()+.5-at.x)<=2.5&&!new AABB(laid).intersects(new AABB(at.x-.3,at.y,at.z-.3,at.x+.3,at.y+1,at.z+.3)),what+": beside or a little behind, and not where they stand: "+laid.subtract(b));
                }
                // What is near enough to come down on, by how far north somebody is.
                h.assertTrue(CalderHollow.floor(.5,CalderHollow.EDGE-1)==2,"by the hill's edge the lowest ground near is the first grave: "+CalderHollow.floor(.5,CalderHollow.EDGE-1));
                h.assertTrue(CalderHollow.floor(0,-322)==0,"at the lids it is the lids: "+CalderHollow.floor(0,-322));
                h.assertTrue(CalderHollow.floor(.5,-392)==4,"on the top it is the last stretch's beginning: "+CalderHollow.floor(.5,-392));
                h.assertTrue(CalderHollow.floor(-4.5,-392)==5&&CalderHollow.floor(5,-392)==1,"off the top's west side it is the top, and off its east side the steep way's run, four blocks under: "+CalderHollow.floor(-4.5,-392)+", "+CalderHollow.floor(5,-392));
                h.assertTrue(CalderHollow.floor(12.5,-285.5)==1&&CalderHollow.floor(-20,-392)==1,"well off to one side, over nothing, it is the lowest ground that far north: "+CalderHollow.floor(12.5,-285.5)+", "+CalderHollow.floor(-20,-392));
                h.assertTrue(CalderHollow.floor(.5,CalderHollow.FAR_SIDE+2)==3,"and at the far side, the far side: "+CalderHollow.floor(.5,CalderHollow.FAR_SIDE+2));
            }).thenSucceed();
    }
    @AfterBatch(batch="hollow_islands") public static void hollowIslandsDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="hollow_missed",timeoutTicks=4000)
    public static void aRiderWhoMissesInTheHollowIsSetDownOnTheLastIslandTheyStoodOnStillOnTheirBikeAndALidIsNotAnIsland(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final NightBike[] mine={null};final int[] t={0};final float[] health={0};
        int first=1,second=2,fences=4;
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("hollow_missed_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            .thenExecute(()->mine[0]=take(h,f,p[0],NightBike.Make.LAMP))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp"),"waiting for the Lamp to be theirs"))
            .thenExecute(()->h.assertTrue(BikeNightHollow.island(p[0])==CalderHollow.HILL&&BikeNightHollow.fell(p[0])==0,"until they have stood on another, their island is the hill"))
            // On the first grave, at its place, pointing east: off its side.
            .thenExecute(()->{var at=islandPlace(b,first);mine[0].moveTo(at.x,at.y,at.z,-90,0);p[0].setYRot(-90);health[0]=p[0].getHealth();})
            .thenWaitUntil(()->h.assertTrue(mine[0].onGround()&&BikeNightHollow.island(p[0])==first,"waiting for the first grave to be the island they stood on: "+BikeNightHollow.island(p[0])))
            .thenExecute(()->hush(p[0]))
            .thenWaitUntil(()->{
                if(BikeNightHollow.fell(p[0])==0){p[0].setYRot(-90);if(mine[0].onGround())pedalWell(mine[0],t[0]++,3);}
                h.assertTrue(BikeNightHollow.fell(p[0])==1,"waiting for them to ride off the grave's side and go down: at "+where(b,mine[0]));
            })
            .thenExecute(()->{
                var at=islandPlace(b,first);
                h.assertTrue(p[0].getVehicle()==mine[0]&&mine[0].isAlive(),"they are still on the Lamp");
                h.assertTrue(mine[0].position().distanceTo(at)<.1,"on the first grave again, at its place: "+where(b,mine[0]));
                h.assertTrue(mine[0].speedNow()==0&&Math.abs(mine[0].leanNow())<.01,"stopped and upright: "+mine[0].speedNow()+", "+mine[0].leanNow());
                h.assertTrue(Math.abs(Mth.wrapDegrees(mine[0].getYRot()-180))<1&&Math.abs(Mth.wrapDegrees(p[0].getYRot()-180))<3,"the bike and its rider pointing the way on: "+mine[0].getYRot()+", "+p[0].getYRot());
                h.assertTrue(p[0].getHealth()==health[0],"unhurt: "+p[0].getHealth());
                h.assertTrue(BikeNightHollow.island(p[0])==first,"and it is still their island");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.UNDER_RIDING)&&sounded(p[0],"hollow.under")&&mistOver(p[0]),"they are told, with what is in the mist heard and the mist over them"))
            // They stay where they were set down: a bike put back does not roll on, tip, or come down hard.
            .thenIdle(30)
            .thenExecute(()->h.assertTrue(p[0].getVehicle()==mine[0]&&mine[0].position().distanceTo(islandPlace(b,first))<.1&&BikeNightHollow.fell(p[0])==1,"a second and a half on, they are where they were set down: "+where(b,mine[0])))
            // The second grave is the last they stood on once they have stood on it: off its west side.
            .thenExecute(()->{var at=islandPlace(b,second);mine[0].moveTo(at.x,at.y,at.z,90,0);p[0].setYRot(90);})
            .thenWaitUntil(()->h.assertTrue(mine[0].onGround()&&BikeNightHollow.island(p[0])==second,"waiting for the second grave to be their island"))
            .thenWaitUntil(()->{
                if(BikeNightHollow.fell(p[0])==1){p[0].setYRot(90);if(mine[0].onGround())pedalWell(mine[0],t[0]++,3);}
                h.assertTrue(BikeNightHollow.fell(p[0])==2,"waiting for them to go down off the second grave: at "+where(b,mine[0]));
            })
            .thenExecute(()->h.assertTrue(p[0].getVehicle()==mine[0]&&mine[0].position().distanceTo(islandPlace(b,second))<.1,"set down on the second grave, not the first: "+where(b,mine[0])))
            // The fences, and from them onto a lid: a lid is not an island, so off a lid they go back to the fences.
            .thenExecute(()->{var at=islandPlace(b,fences);mine[0].moveTo(at.x,at.y,at.z,180,0);p[0].setYRot(180);})
            .thenWaitUntil(()->h.assertTrue(mine[0].onGround()&&BikeNightHollow.island(p[0])==fences,"waiting for the fences to be their island"))
            .thenExecute(()->{mine[0].moveTo(b.getX()+0.0,b.getY()+0,b.getZ()-324.0,-90,0);p[0].setYRot(-90);})
            .thenWaitUntil(()->h.assertTrue(mine[0].onGround(),"waiting for the bike to be on the third lid"))
            .thenExecute(()->h.assertTrue(BikeNightHollow.island(p[0])==fences,"on a lid, the fences are still the last island they stood on: "+BikeNightHollow.island(p[0])))
            .thenWaitUntil(()->{
                if(BikeNightHollow.fell(p[0])==2){p[0].setYRot(-90);if(mine[0].onGround())pedalWell(mine[0],t[0]++,3);}
                h.assertTrue(BikeNightHollow.fell(p[0])==3,"waiting for them to go down off the lid: at "+where(b,mine[0]));
            })
            .thenExecute(()->{
                h.assertTrue(p[0].getVehicle()==mine[0]&&mine[0].position().distanceTo(islandPlace(b,fences))<.1,"set down on the fences, a run short of their far end: "+where(b,mine[0]));
                h.assertTrue(p[0].getHealth()==health[0],"and still unhurt: "+p[0].getHealth());
            }).thenSucceed();
    }
    @AfterBatch(batch="hollow_missed") public static void hollowMissedDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="hollow_lost",timeoutTicks=4000)
    public static void somebodyOnFootWhoMissesIsSetDownOnFootAndABikeLetGoOfOverNothingOrLeftWhereTheyCannotWalkToItIsLaidByThem(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final NightBike[] mine={null};final BlockPos[] want={null},mineLid={null};final float[] health={0};
        int third=3;var rack=b.offset(BikeNightSite.bikeAt(NightBike.Make.WASP));
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->p[0]=f.player("hollow_lost_reader",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night"))
            // On foot, with no bike: off what is left of the hill's last rows, well off the gate's line.
            .thenExecute(()->{health[0]=p[0].getHealth();p[0].teleportTo(b.getX()+30.5,b.getY()+3,b.getZ()+CalderHollow.EDGE-.5);})
            .thenIdle(3)
            .thenExecute(()->{h.assertTrue(BikeNightHollow.fell(p[0])==0,"standing at the hill's edge is not missing");hush(p[0]);p[0].teleportTo(b.getX()+30.5,b.getY()-.5,b.getZ()+CalderHollow.EDGE-2.5);})
            .thenWaitUntil(()->h.assertTrue(BikeNightHollow.fell(p[0])==1,"waiting for them to have gone down off the hill"))
            .thenExecute(()->{
                h.assertTrue(p[0].position().distanceTo(islandPlace(b,CalderHollow.HILL))<.05&&!p[0].isPassenger(),"they are set down on the hill, on the gate's line, on foot: "+where(b,p[0]));
                h.assertTrue(Math.abs(Mth.wrapDegrees(p[0].getYRot()-180))<1&&p[0].getHealth()==health[0],"pointing the way on, unhurt");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.UNDER_WALKING)&&sounded(p[0],"hollow.under")&&mistOver(p[0]),"they are told, with what is in the mist heard and the mist over them"))
            // The Wasp, on the third grave; then out over nothing, where they let go of it.
            .thenExecute(()->mine[0]=take(h,f,p[0],NightBike.Make.WASP))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("wasp"),"waiting for the Wasp to be theirs"))
            .thenExecute(()->{var at=islandPlace(b,third);mine[0].moveTo(at.x,at.y,at.z,180,0);p[0].setYRot(180);})
            .thenWaitUntil(()->h.assertTrue(mine[0].onGround()&&BikeNightHollow.island(p[0])==third,"waiting for the third grave to be their island"))
            .thenExecute(()->{
                want[0]=BikeNightHollow.beside(l,islandPlace(b,third));
                h.assertTrue(want[0]!=null&&l.getBlockState(want[0]).isAir(),"there is somewhere by the third grave's place to lay a bike");
                mine[0].moveTo(b.getX()+12.5,b.getY()+2,b.getZ()-285.5,180,0);getOff(p[0]);
            })
            .thenWaitUntil(()->h.assertTrue(!p[0].isPassenger()&&mine[0].isRemoved(),"waiting for them to be off it"))
            .thenExecute(()->{
                stand(p[0]);var s=l.getBlockState(want[0]);
                h.assertTrue(s.is(BikeNightRegistry.BIKE_BLOCK.get())&&s.getValue(NightBikeBlock.MAKE)==NightBike.Make.WASP&&s.getValue(NightBikeBlock.LYING),"let go of over nothing, the Wasp lies by the third grave's place: "+s);
                h.assertTrue(l.getBlockState(rack).isAir(),"and has not gone back to the rack");
                // Its rider is in the air where they let go: down they go.
                p[0].teleportTo(b.getX()+12.5,b.getY()-1.5,b.getZ()-285.5);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNightHollow.fell(p[0])==2,"waiting for them to have gone down after it"))
            .thenExecute(()->{
                h.assertTrue(p[0].position().distanceTo(islandPlace(b,third))<.05&&!p[0].isPassenger(),"they are set down on the third grave on foot: "+where(b,p[0]));
                var s=l.getBlockState(want[0]);
                h.assertTrue(s.is(BikeNightRegistry.BIKE_BLOCK.get())&&s.getValue(NightBikeBlock.LYING)&&p[0].position().distanceTo(Vec3.atBottomCenterOf(want[0]))<3,"with the Wasp lying where it was laid, in reach");
                // A hand stands it up, and a second gets them on it.
                hand(f,p[0],want[0]);hand(f,p[0],want[0]);
                h.assertTrue(p[0].getVehicle() instanceof NightBike again&&again.make()==NightBike.Make.WASP,"and they can get back on it");
                mine[0]=(NightBike)p[0].getVehicle();
                // Onto the first lid, and off the bike there: it stands on the lid.
                mine[0].moveTo(b.getX()+0.0,b.getY()+0,b.getZ()-318.0,180,0);
            })
            .thenWaitUntil(()->h.assertTrue(mine[0].onGround(),"waiting for the Wasp to be on the first lid"))
            .thenExecute(()->getOff(p[0]))
            .thenWaitUntil(()->h.assertTrue(!p[0].isPassenger()&&mine[0].isRemoved(),"waiting for them to be off it on the lid"))
            .thenExecute(()->{
                stand(p[0]);
                var on=BlockPos.betweenClosedStream(b.offset(-3,0,-320),b.offset(2,1,-317)).map(BlockPos::immutable).filter(at->l.getBlockState(at).is(BikeNightRegistry.BIKE_BLOCK.get())).toList();
                h.assertTrue(on.size()==1&&l.getBlockState(want[0]).isAir(),"the Wasp stands on the lid, and nowhere else: "+on);
                mineLid[0]=on.get(0);
                // On foot off the lid's side.
                p[0].teleportTo(b.getX()+3.5,b.getY()-2.5,b.getZ()-318.0);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNightHollow.fell(p[0])==3,"waiting for them to have gone down off the lid"))
            .thenExecute(()->{
                h.assertTrue(p[0].position().distanceTo(islandPlace(b,third))<.05,"they are set down on the third grave, the last island they stood on: "+where(b,p[0]));
                var s=l.getBlockState(want[0]);
                h.assertTrue(l.getBlockState(mineLid[0]).isAir()&&s.is(BikeNightRegistry.BIKE_BLOCK.get())&&s.getValue(NightBikeBlock.MAKE)==NightBike.Make.WASP&&s.getValue(NightBikeBlock.LYING),"and the Wasp, which they could not have walked back to, is off the lid and lying by them: "+s);
                h.assertTrue(p[0].getHealth()==health[0],"unhurt all through: "+p[0].getHealth());
            }).thenSucceed();
    }
    @AfterBatch(batch="hollow_lost") public static void hollowLostDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="hollow_pegs",timeoutTicks=4000)
    public static void whoeverIsOnTheHoppersPegsGoesDownAndIsSetDownWithTheOnePedalling(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final NightBike[] mine={null};final int[] t={0};
        int top=7;
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{p[0]=f.player("hollow_pegs_pedalling",P);p[1]=f.player("hollow_pegs_carried",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&BikeNight.here(p[0])&&BikeNight.here(p[1]),"waiting for the night"))
            .thenExecute(()->mine[0]=take(h,f,p[0],NightBike.Make.HOPPER))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("hopper"),"waiting for the Hopper to be theirs"))
            .thenExecute(()->h.assertTrue(mine[0].interact(p[1],InteractionHand.MAIN_HAND).consumesAction()&&p[1].getVehicle()==mine[0]&&mine[0].getFirstPassenger()==p[0],"a hand on the Hopper puts the second reader on its pegs"))
            // On the stairs under the top, at that island's place, pointing west: off its side. (East of them is the steep way.)
            .thenExecute(()->{var at=islandPlace(b,top);mine[0].moveTo(at.x,at.y,at.z,90,0);p[0].setYRot(90);})
            .thenWaitUntil(()->h.assertTrue(mine[0].onGround()&&BikeNightHollow.island(p[0])==top&&BikeNightHollow.island(p[1])==top,"waiting for the stairs and the top to be the island both stood on: "+BikeNightHollow.island(p[0])+", "+BikeNightHollow.island(p[1])))
            .thenExecute(()->{hush(p[0]);hush(p[1]);})
            .thenWaitUntil(()->{
                if(BikeNightHollow.fell(p[0])==0){p[0].setYRot(90);if(mine[0].onGround())pedalWell(mine[0],t[0]++,3);}
                h.assertTrue(BikeNightHollow.fell(p[0])==1,"waiting for them to ride off the stairs' side and go down: at "+where(b,mine[0]));
            })
            .thenExecute(()->{
                h.assertTrue(p[0].getVehicle()==mine[0]&&p[1].getVehicle()==mine[0]&&mine[0].getFirstPassenger()==p[0],"both are still on the Hopper, the same one pedalling");
                h.assertTrue(mine[0].position().distanceTo(islandPlace(b,top))<.1&&p[1].position().distanceTo(mine[0].position())<1.5,"set down together at the island's place: "+where(b,mine[0])+"; "+where(b,p[1]));
                h.assertTrue(BikeNightHollow.fell(p[1])==1,"and the one on the pegs went down too");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.UNDER_RIDING)&&heard(p[1],BikeNightTexts.UNDER_RIDING)&&sounded(p[0],"hollow.under")&&sounded(p[1],"hollow.under")&&mistOver(p[0])&&mistOver(p[1]),"each is told, hears what is in the mist and has it over them"))
            .thenSucceed();
    }
    @AfterBatch(batch="hollow_pegs") public static void hollowPegsDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.496, the high way
    /** How wide the nothing ahead of a rider on a way is, and how far off: {width, distance to it}; or {0, 99} with none within six blocks. */
    private static double[] gapOn(List<CalderHollow.Piece> way,double z){
        for(int i=0;i+1<way.size();i++){
            var a=way.get(i);var c=way.get(i+1);int gap=a.zNorth()-c.zSouth()-1;
            if(gap>0&&z>=a.zNorth()&&z<=a.zSouth()+1&&z-a.zNorth()<6)return new double[]{gap,z-a.zNorth()};
        }
        return new double[]{0,99};
    }
    /** Somebody on the second grave, on the line the high way is on, going flat out north with the hop held; let go at the brink. */
    private static void atTheLedge(ServerPlayer p,NightBike bike,BlockPos b){
        bike.moveTo(b.getX()+5.0,b.getY()+2,b.getZ()-273.4,180,0);p.setYRot(180);bike.steady(0,RiddenBike.TOP);bike.keys(true,false);
    }

    @GameTest(template="empty",batch="hollow_high",timeoutTicks=6000)
    public static void theHighWayIsReachedByTheHoppersHeldHopAndByNoOtherBikesAndRunsOverTheFencesAndTheLidsToTheGravePastThem(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final NightBike[] mine={null,null};final int[] t={0},taps={0};
        final boolean[] held={false},let={false},cameOff={false};final double[] highest={0};var over=new java.util.TreeSet<String>();
        var high=CalderHollow.HIGH;var second=CalderHollow.WAY.get(1);var past=CalderHollow.WAY.stream().filter(piece->piece.name().equals("the grave past the lids")).findFirst().orElseThrow();
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                // Where it is: a ledge two blocks up across two blocks of nothing from the second grave, in line with its east side.
                var ledge=high.get(0);
                h.assertTrue(second.name().equals("second grave")&&ledge.top()==second.top()+2&&second.zNorth()-ledge.zSouth()-1==2,"the ledge is two blocks up from the second grave across two blocks of nothing: "+ledge);
                h.assertTrue(Math.min(second.x1(),ledge.x1())-Math.max(second.x0(),ledge.x0())+1>=2,"and in line with it by two blocks");
                // Over the fences and the lids, clear of them: nothing of it is over or against the plain way but its foot.
                for(var piece:high)for(var plain:CalderHollow.WAY){
                    boolean beside=piece.x0()<=plain.x1()+1&&piece.x1()>=plain.x0()-1&&piece.zNorth()<=plain.zSouth()&&piece.zSouth()>=plain.zNorth();
                    if(beside&&!piece.name().equals("the high way's foot"))h.assertTrue(piece.x0()>plain.x1()+1||piece.top()<=plain.top()+2.5||plain==past,piece.name()+" hangs over "+plain.name());
                }
                // On it: gaps of two that a tap clears, level or coming down; and down by half blocks to its foot, level with the grave past the lids and against it.
                for(int i=0;i+1<high.size();i++){
                    var a=high.get(i);var c=high.get(i+1);int gap=a.zNorth()-c.zSouth()-1;
                    h.assertTrue((gap==0||gap==2)&&c.top()<=a.top()&&(gap>0||a.top()-c.top()<=.5),"from "+a.name()+" to "+c.name()+": "+gap+" blocks of nothing, "+a.top()+" to "+c.top());
                }
                var foot=high.get(high.size()-1);
                h.assertTrue(foot.top()==past.top()&&foot.x0()==past.x1()+1&&foot.zNorth()==past.zNorth()&&foot.zSouth()>=past.zSouth(),"its foot is level with the grave past the lids and lies along the whole of its east side: "+foot);
                p[0]=f.player("hollow_high_hopper",P);p[1]=f.player("hollow_high_plain",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&BikeNight.here(p[0])&&BikeNight.here(p[1]),"waiting for the night"))
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.HOPPER);mine[1]=take(h,f,p[1],NightBike.Make.COMET);})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("hopper")&&f.own(p[1],P).getString("Bike").equals("comet"),"waiting for the bikes to be theirs"))
            // The Comet first, the fastest of the others and the one that jumps furthest: it does not get up.
            .thenExecute(()->{var at=islandPlace(b,2);mine[1].moveTo(at.x,at.y,at.z,180,0);p[1].setYRot(180);})
            .thenWaitUntil(()->h.assertTrue(mine[1].onGround()&&BikeNightHollow.island(p[1])==2,"waiting for the second grave to be the Comet's rider's island"))
            .thenExecute(()->{atTheLedge(p[1],mine[1],b);mine[1].steady(0,NightBike.Make.COMET.stats.top);let[0]=false;})
            .thenWaitUntil(()->{
                if(!let[0]&&mine[1].isAlive()&&mine[1].getZ()-b.getZ()<=second.zNorth()+.3){mine[1].keys(false,false);let[0]=true;}
                if(!let[0]&&mine[1].isAlive()&&mine[1].onGround())mine[1].steady(0,NightBike.Make.COMET.stats.top);
                // Thrown off against the ledge's side, they are in the air over nothing; a test's reader does not fall by themselves.
                if(let[0]&&!p[1].isPassenger()&&BikeNightHollow.fell(p[1])==0){
                    h.assertTrue(p[1].getY()-b.getY()<high.get(0).top()-.5&&p[1].getZ()-b.getZ()>high.get(0).zSouth()+1,"they came off against the ledge's face with their feet well under its top, short of it: "+where(b,p[1]));
                    cameOff[0]=true;p[1].teleportTo(p[1].getX(),b.getY()-1.5,p[1].getZ());
                }
                h.assertTrue(BikeNightHollow.fell(p[1])==1,"waiting for the Comet's rider to have gone down: at "+where(b,p[1]));
            })
            .thenExecute(()->{
                h.assertTrue(BikeNightHollow.island(p[1])==2&&p[1].position().distanceTo(islandPlace(b,2))<1.6,"they are back on the second grave, never having been on the ledge: "+where(b,p[1])+", island "+BikeNightHollow.island(p[1]));
                boolean riding=p[1].getVehicle() instanceof NightBike;
                var laid=BlockPos.betweenClosedStream(b.offset(-2,1,-281),b.offset(7,6,-270)).map(BlockPos::immutable).filter(at->l.getBlockState(at).is(BikeNightRegistry.BIKE_BLOCK.get())).toList();
                h.assertTrue(riding||laid.size()==1&&laid.get(0).getY()-b.getY()==2,"and the Comet is with them, on the second grave and not up on the ledge: riding "+riding+", lying "+laid);
                h.assertTrue(cameOff[0]!=riding,"either they came off against the ledge's side and it was laid by them, or they went under it still riding: came off "+cameOff[0]);
            })
            // The Hopper: up onto the ledge, and the whole of the high way.
            .thenExecute(()->{var at=islandPlace(b,2);mine[0].moveTo(at.x,at.y,at.z,180,0);p[0].setYRot(180);})
            .thenWaitUntil(()->h.assertTrue(mine[0].onGround()&&BikeNightHollow.island(p[0])==2,"waiting for the second grave to be the Hopper's rider's island"))
            .thenExecute(()->{atTheLedge(p[0],mine[0],b);let[0]=false;})
            .thenWaitUntil(()->{
                h.assertTrue(p[0].getVehicle()==mine[0]&&BikeNightHollow.fell(p[0])==0,"the Hopper's rider is on it and has not gone down: at "+where(b,mine[0])+", over "+over);
                double x=mine[0].getX()-b.getX(),z=mine[0].getZ()-b.getZ(),y=mine[0].getY()-b.getY();highest[0]=Math.max(highest[0],y);
                if(!let[0]){
                    // Flat out with the hop held, and let go at the brink.
                    if(mine[0].onGround())mine[0].steady(0,RiddenBike.TOP);
                    if(z<=second.zNorth()+.3){mine[0].keys(false,false);let[0]=true;}
                }else if(z> -330.5){
                    // Along it: a tap at each gap, the middle of what is left to ride on.
                    p[0].setYRot(mine[0].onGround()?(float)Math.toDegrees(Math.atan2(-(5.0-x),-3.5)):mine[0].getYRot());
                    var gap=gapOn(high,z);boolean brink=gap[1]<=.4&&mine[0].onGround();
                    if(gap[0]>=2&&brink&&!held[0]){mine[0].keys(true,false);held[0]=true;taps[0]++;}else if(held[0]){mine[0].keys(false,false);held[0]=false;}
                    if(mine[0].onGround())pedalWell(mine[0],t[0]++,3);
                }else{
                    // At its foot: round onto the grave past the lids, at its middle.
                    if(mine[0].onGround()){p[0].setYRot((float)Math.toDegrees(Math.atan2(-(.5-x),-336.5-z)));pedalWell(mine[0],t[0]++,3);}
                }
                for(var piece:high)if(mine[0].onGround()&&piece.has((int)Math.floor(x),(int)Math.floor(z))&&Math.abs(y-piece.top())<.1)over.add(piece.name().replaceAll(" [0-9]+$",""));
                h.assertTrue(mine[0].onGround()&&x<past.x1()&&CalderHollow.stood(x,y,z)==5,"waiting for the Hopper to come down the high way onto the grave past the lids: at "+where(b,mine[0])+" going "+mine[0].speedNow()+", over "+over);
            })
            .thenExecute(()->{
                h.assertTrue(over.containsAll(List.of("high grave","the high way's foot")),"over the high way to its foot (the steps are come down mostly in the air): "+over);
                h.assertTrue(highest[0]>4.5&&taps[0]==2,"up past the ledge's height, and a tap at each of its two gaps: "+highest[0]+", "+taps[0]+" taps");
                h.assertTrue(BikeNightHollow.island(p[0])==5&&BikeNightHollow.fell(p[0])==0,"and the grave past the lids is their island now");
            }).thenSucceed();
    }
    @AfterBatch(batch="hollow_high") public static void hollowHighDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.497, the long way
    private static int islandNamed(String name){for(int i=0;i<CalderHollow.ISLANDS.size();i++)if(CalderHollow.ISLANDS.get(i).name().equals(name))return i;throw new IllegalStateException(name);}
    private static boolean runRowThere(ServerLevel l,BlockPos b,int row){return l.getBlockState(b.offset(CalderHollow.RUN_X0,-1,CalderHollow.RUN_SOUTH-row)).is(BikeNightRegistry.COFFIN_LID.get())&&l.getBlockState(b.offset(CalderHollow.RUN_X1,-1,CalderHollow.RUN_SOUTH-row)).is(BikeNightRegistry.COFFIN_LID.get());}
    /**
     * One rider at the long way: from the grave past the lids (so that it is their island) to the head of the run, then north along
     * it pedalling as hard as a rider can, the Rattler with its card held from the run's first row. {@code far[i]} is the furthest
     * row they had under them; the step ends when they are on the long way's foot or have gone down.
     */
    private static net.minecraft.gametest.framework.GameTestSequence longRide(net.minecraft.gametest.framework.GameTestSequence q,GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer[] p,NightBike[] mine,int i,int[] far,int[] t,boolean[] behind){
        var b=f.base(P);var l=f.level(P);int past=islandNamed("the grave past the lids"),foot=islandNamed("long step and the long way's foot");
        return q.thenExecute(()->{var at=islandPlace(b,past);mine[i].moveTo(at.x,at.y,at.z,180,0);p[i].setYRot(180);})
            .thenWaitUntil(()->h.assertTrue(mine[i].onGround()&&BikeNightHollow.island(p[i])==past,"waiting for the grave past the lids to be their island"))
            .thenExecute(()->{
                h.assertTrue(!BikeNightHollow.runGoing(b)&&BikeNightHollow.runGone(b)==0&&runRowThere(l,b,0)&&runRowThere(l,b,CalderHollow.RUN_ROWS-1),"the run is whole and still before they are on it");
                mine[i].moveTo(b.getX()+CalderHollow.RUN_X0+1.0,b.getY(),b.getZ()-336.6,180,0);p[i].setYRot(180);far[i]=-1;behind[0]=false;
            })
            .thenWaitUntil(()->{
                int fell=BikeNightHollow.fell(p[i]);
                if(fell==0&&p[i].getVehicle()==mine[i]){
                    double x=mine[i].getX()-b.getX(),z=mine[i].getZ()-b.getZ(),y=mine[i].getY()-b.getY();
                    p[i].setYRot(mine[i].onGround()?(float)Math.toDegrees(Math.atan2(-(CalderHollow.RUN_X0+1.0-x),-3.5)):mine[i].getYRot());
                    if(mine[i].make()==NightBike.Make.RATTLER&&z<=CalderHollow.RUN_SOUTH+1)mine[i].keys(false,true);
                    if(mine[i].onGround()){
                        pedalWell(mine[i],t[0]++,3);int row=CalderHollow.runRow(x,z);
                        if(row>=0&&Math.abs(y)<.1){
                            far[i]=Math.max(far[i],row);
                            // Well along it, the rows they first rode over are gone.
                            if(row>=12){h.assertTrue(BikeNightHollow.runGoing(b)&&!runRowThere(l,b,0)&&!runRowThere(l,b,3),"with them on row "+row+" the run has given way behind them: "+BikeNightHollow.runGone(b)+" rows gone");behind[0]=true;}
                            h.assertTrue(runRowThere(l,b,row),"the row a bike is on the ground on is there: "+row);
                        }
                    }
                }
                double x=mine[i].getX()-b.getX(),z=mine[i].getZ()-b.getZ(),y=mine[i].getY()-b.getY();
                h.assertTrue(fell>0||mine[i].isAlive()&&mine[i].onGround()&&CalderHollow.stood(x,y,z)==foot&&BikeNightHollow.island(p[i])==foot,"waiting for the "+mine[i].make().id()+"'s rider to be over the run or to have gone down: at "+where(b,p[i])+" going "+(mine[i].isAlive()?mine[i].speedNow():0)+", furthest row "+far[i]+", "+BikeNightHollow.runGone(b)+" gone");
            });
    }

    @GameTest(template="empty",batch="hollow_long",timeoutTicks=8000)
    public static void theLongWaysRunGivesWayBehindWhoeverIsOnItAndOnlyTheCometAndTheRattlerInItsBurstStayAheadOfIt(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null,null};final NightBike[] mine={null,null,null};final int[] far={-1,-1,-1},t={0};final boolean[] behind={false};
        var way=CalderHollow.LONG;int rows=CalderHollow.RUN_ROWS;int pastIsland=islandNamed("the grave past the lids"),foot=islandNamed("long step and the long way's foot");
        var q=h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                var past=CalderHollow.WAY.stream().filter(piece->piece.name().equals("the grave past the lids")).findFirst().orElseThrow();
                var head=way.get(0);var last=way.get(way.size()-1);
                h.assertTrue(head.top()==past.top()&&head.x1()==past.x0()-1&&head.zSouth()<=past.zSouth()&&head.zSouth()>=past.zNorth(),"its head is level with the grave past the lids and against its west side: "+head);
                int lids=0;for(var piece:way)if(piece.made()==CalderHollow.Made.LID){h.assertTrue(piece.top()==head.top()&&piece.x0()==CalderHollow.RUN_X0&&piece.x1()==CalderHollow.RUN_X1&&piece.zSouth()==CalderHollow.RUN_SOUTH-lids&&piece.zNorth()==piece.zSouth(),"the run is lids, two wide, one row each, level: "+piece);lids++;}
                h.assertTrue(lids==rows&&rows>=24,"a long run: "+lids+" rows");
                // Past it, up by half blocks to its foot, which is against the stairs and level with the pair it meets.
                var stair=CalderHollow.WAY.stream().filter(piece->piece.name().startsWith("stair")&&piece.zSouth()<=last.zSouth()&&piece.zSouth()>=last.zNorth()&&piece.top()==last.top()).findFirst();
                h.assertTrue(stair.isPresent()&&last.x1()==stair.get().x0()-1,"its foot is against the stairs, level with the pair it meets: "+last);
                // It is no way round anything on the plain way: nothing of it is within a block of the vine, or of the gap between the vine and the stairs.
                for(var piece:way)for(var plain:CalderHollow.WAY)if(plain.name().startsWith("vine"))
                    h.assertTrue(piece.x1()<plain.x0()-1||piece.zSouth()<plain.zNorth()-2||piece.zNorth()>plain.zSouth()+2,piece.name()+" is within reach of "+plain.name());
                // How fast it goes: faster than a bike is pedalled, slower than the Comet and than the Rattler's burst.
                h.assertTrue(BikeNightHollow.PACE>RiddenBike.TOP*1.15F&&BikeNightHollow.PACE<NightBike.Make.COMET.stats.top*.92F&&BikeNightHollow.PACE<RiddenBike.TOP*NightBike.TURBO_TOP*.92F,"it gives way faster than a bike is pedalled and slower than the Comet or the Rattler's burst: "+BikeNightHollow.PACE);
                for(var make:NightBike.Make.values())if(make!=NightBike.Make.COMET)h.assertTrue(make.stats.top<BikeNightHollow.PACE,"no bike but the Comet is pedalled as fast as it goes: "+make.id());
                p[0]=f.player("hollow_long_lamp",P);p[1]=f.player("hollow_long_comet",P);p[2]=f.player("hollow_long_rattler",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&p[2].getBbHeight()<1.1&&BikeNight.here(p[2]),"waiting for the night"))
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.LAMP);mine[1]=take(h,f,p[1],NightBike.Make.COMET);mine[2]=take(h,f,p[2],NightBike.Make.RATTLER);})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp")&&f.own(p[1],P).getString("Bike").equals("comet")&&f.own(p[2],P).getString("Bike").equals("rattler"),"waiting for the bikes to be theirs"));
        // The Lamp, pedalled as hard as any bike but the Comet can be: caught.
        q=longRide(q,h,f,p,mine,0,far,t,behind)
            .thenExecute(()->{
                h.assertTrue(BikeNightHollow.fell(p[0])==1&&far[0]>=4&&far[0]<rows-6,"the run went from under the Lamp partway along it: furthest row "+far[0]);
                h.assertTrue(p[0].getVehicle()==mine[0]&&mine[0].position().distanceTo(islandPlace(b,pastIsland))<.1,"and its rider is set down on the grave past the lids, still on it: "+where(b,mine[0]));
            })
            // All of it goes, and then it is whole again.
            .thenWaitUntil(()->h.assertTrue(BikeNightHollow.runGone(b)==rows&&!runRowThere(l,b,rows-1),"waiting for all of the run to have gone: "+BikeNightHollow.runGone(b)))
            .thenWaitUntil(()->h.assertTrue(!BikeNightHollow.runGoing(b)&&BikeNightHollow.runGone(b)==0&&runRowThere(l,b,0)&&runRowThere(l,b,rows-1),"waiting for the run to be whole again"));
        // The Comet: over it.
        q=longRide(q,h,f,p,mine,1,far,t,behind)
            .thenExecute(()->{
                h.assertTrue(BikeNightHollow.fell(p[1])==0&&p[1].getVehicle()==mine[1]&&far[1]>=rows-2&&behind[0],"the Comet stayed ahead of it the whole way, and it gave way behind: furthest row "+far[1]);
                h.assertTrue(BikeNightHollow.island(p[1])==foot,"and the long way's foot is its rider's island");
            })
            .thenWaitUntil(()->h.assertTrue(!BikeNightHollow.runGoing(b)&&BikeNightHollow.runGone(b)==0&&runRowThere(l,b,0),"waiting for the run to be whole again"));
        // The Rattler, with its card held from the first row.
        longRide(q,h,f,p,mine,2,far,t,behind)
            .thenExecute(()->h.assertTrue(BikeNightHollow.fell(p[2])==0&&p[2].getVehicle()==mine[2]&&far[2]>=rows-2&&behind[0],"the Rattler in its burst stayed ahead of it too: furthest row "+far[2]))
            .thenSucceed();
    }
    @AfterBatch(batch="hollow_long") public static void hollowLongDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.498, the way out
    @GameTest(template="empty",batch="hollow_out",timeoutTicks=4000)
    public static void theBackGateOpensForWhoeverReachesTheFarSideAndGoingThroughItIsInTheirRecordWithTheTownsLightsOnAgain(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final NightBike[] mine={null};final int[] t={0};final boolean[] openedBefore={false};
        int top=CalderGraves.level(CalderHollow.EDGE),back=CalderHollow.BACK,high=islandNamed("high grave"),stretch=CalderHollow.FAR_ISLAND-1;
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{p[0]=f.player("hollow_out_pedalling",P);p[1]=f.player("hollow_out_carried",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&BikeNight.here(p[0])&&BikeNight.here(p[1]),"waiting for the night"))
            .thenExecute(()->{
                h.assertTrue(!CalderHollow.backGateOpen(l,b)&&!BikeNightHollow.gateOpen(b),"the back gate is shut");
                // The town's lamps have gone out behind them, as they do.
                BikeNightGraveyard.darkNow(l,b);var lit=BikeNightGraveyard.lit(l,b);
                h.assertTrue(lit[1]>=12&&lit[0]==0&&BikeNightGraveyard.dark(b)==BikeNightGraveyard.STREETS.length,"every street's lamps are out: "+lit[0]+" of "+lit[1]+" lit");
                mine[0]=take(h,f,p[0],NightBike.Make.HOPPER);
            })
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("hopper"),"waiting for the Hopper to be theirs"))
            .thenExecute(()->h.assertTrue(mine[0].interact(p[1],InteractionHand.MAIN_HAND).consumesAction()&&p[1].getVehicle()==mine[0],"a hand on the Hopper puts the second reader on its pegs"))
            // They have been on the high way; then they are on the last stretch.
            .thenExecute(()->{var at=islandPlace(b,high);mine[0].moveTo(at.x,at.y,at.z,180,0);p[0].setYRot(180);})
            .thenWaitUntil(()->h.assertTrue(mine[0].onGround()&&BikeNightHollow.island(p[0])==high&&BikeNightHollow.island(p[1])==high,"waiting for a high grave to be their island"))
            .thenExecute(()->{var at=islandPlace(b,stretch);mine[0].moveTo(at.x,at.y,at.z,180,0);p[0].setYRot(180);})
            .thenWaitUntil(()->h.assertTrue(mine[0].onGround()&&BikeNightHollow.island(p[0])==stretch,"waiting for the last stretch to be their island"))
            .thenExecute(()->{h.assertTrue(!CalderHollow.backGateOpen(l,b),"out on the last stretch, the gate is still shut");hush(p[0]);hush(p[1]);})
            .thenWaitUntil(()->{
                double x=mine[0].getX()-b.getX(),z=mine[0].getZ()-b.getZ();
                h.assertTrue(p[0].getVehicle()==mine[0]&&BikeNightHollow.fell(p[0])==0,"they are riding: at "+where(b,mine[0]));
                // Up the gate's line, at the middle of the gateway.
                p[0].setYRot((float)Math.toDegrees(Math.atan2(-(0.0-x),-3.5)));if(mine[0].onGround())pedalWell(mine[0],t[0]++,3);
                if(z-back<3&&z>back)openedBefore[0]|=CalderHollow.backGateOpen(l,b);
                if(z-back>BikeNightHollow.GATE_NEAR+1)h.assertTrue(!CalderHollow.backGateOpen(l,b),"it does not open until somebody is near it: "+(z-back)+" off");
                h.assertTrue(BikeNightHollow.through(p[0])&&BikeNightHollow.through(p[1]),"waiting for them to ride through the back gate: at "+where(b,mine[0]));
            })
            .thenExecute(()->{
                h.assertTrue(openedBefore[0]&&CalderHollow.backGateOpen(l,b)&&BikeNightHollow.gateOpen(b),"the gate stood open before they reached it, and stands open");
                for(int x:new int[]{CalderGraves.PATH_W,CalderGraves.PATH_E})for(int z=back-2;z<=back-1;z++)for(int y=top;y<top+2;y++)
                    h.assertTrue(CalderBlock.is(l.getBlockState(b.offset(x,y,z)),CalderBlock.Kind.GATE),"its leaves stand end-on on the lane's side at "+x+","+y+","+z);
                for(int x=CalderGraves.PATH_W;x<=CalderGraves.PATH_E;x++)h.assertTrue(l.getBlockState(b.offset(x,top,back)).isAir()&&l.getBlockState(b.offset(x,top+1,back)).isAir(),"nothing hangs between its piers at x="+x);
                h.assertTrue(mine[0].getZ()-b.getZ()<back&&Math.abs(mine[0].getY()-b.getY()-top)<.6,"they are on the lane: "+where(b,mine[0]));
                for(var who:p){
                    var own=f.own(who,P);
                    h.assertTrue(own.getBoolean("Through")&&own.getString("ThroughOn").equals("hopper")&&own.getString("HollowWays").equals("high")&&own.getInt("HollowFell")==0,who.getGameProfile().getName()+"'s record has that they are through, on the Hopper, by the high way, never having gone down: "+own);
                }
                var lit=BikeNightGraveyard.lit(l,b);
                h.assertTrue(lit[0]==lit[1]&&lit[1]>=12&&BikeNightGraveyard.dark(b)==0,"the town's lamps are on again: "+lit[0]+" of "+lit[1]);
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.THROUGH)&&heard(p[1],BikeNightTexts.THROUGH),"each is told"))
            // And they stay on: what put them out does not put them out again.
            .thenExecute(()->BikeNightGraveyard.darkNow(l,b))
            .thenIdle(30)
            .thenExecute(()->{var lit=BikeNightGraveyard.lit(l,b);h.assertTrue(lit[0]==lit[1]&&BikeNightGraveyard.dark(b)==0,"the lamps stay on for the rest of the night: "+lit[0]+" of "+lit[1]);})
            .thenSucceed();
    }
    @AfterBatch(batch="hollow_out") public static void hollowOutDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.499, the witch, and being caught
    private static Vec3 atRow(BlockPos b,int row,double south){var w=BikeNightSpecters.SWOOPS.get(row);return new Vec3(b.getX()+w.x(),b.getY()+w.top(),b.getZ()+w.z()+south);}

    @GameTest(template="empty",batch="hollow_witch",timeoutTicks=5000)
    public static void aWitchCrossesTheWayForOneReaderAndGoesOverWhoeverIsDownOverTheirBarsAndWhoeverIsNotIsCaughtAndComesRoundOutsideTheGate(GameTestHelper h){
        var f=park(h);BikeNightSpecters.keptAway=false;BikeNightSpecters.only=java.util.Set.of("witch");var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null,null};final NightBike[] mine={null,null,null};
        final double[] lowest={99},west={99},east={-99},lastZ={0};final boolean[] seen={false},own={true};final float[] health={0};
        var rows=BikeNightSpecters.SWOOPS;var rack=b.offset(BikeNightSite.bikeAt(NightBike.Make.LAMP));
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                // Her rows are on the plain way; the first and the last are a little short of a brink.
                h.assertTrue(rows.size()==3,"three rows: "+rows);
                for(int i=0;i<3;i++){
                    var w=rows.get(i);var gap=hollowGap(w.z()+.5);
                    h.assertTrue(!Double.isNaN(CalderHollow.top((int)Math.floor(w.x()),w.z()))&&w.top()==CalderHollow.top((int)Math.floor(w.x()),w.z()),"a row on the way, at its height: "+w);
                    if(i!=1)h.assertTrue(gap[0]>=2&&gap[1]<=3,"within three blocks of a brink: "+w+", "+gap[0]+" wide "+gap[1]+" off");
                }
                p[0]=f.player("hollow_witch_lamp",P);p[1]=f.player("hollow_witch_hare",P);p[2]=f.player("hollow_witch_hopper",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&p[2].getBbHeight()<1.1&&BikeNight.here(p[2]),"waiting for the night"))
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.LAMP);mine[1]=take(h,f,p[1],NightBike.Make.HARE);mine[2]=take(h,f,p[2],NightBike.Make.HOPPER);health[0]=p[0].getHealth();})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp")&&f.own(p[1],P).getString("Bike").equals("hare")&&f.own(p[2],P).getString("Bike").equals("hopper"),"waiting for the bikes to be theirs"))
            // The other two wait on the hill. The Lamp's rider stands half a block short of her first row, down over the bars.
            .thenExecute(()->{
                for(int i=1;i<3;i++){var at=islandPlace(b,CalderHollow.HILL);mine[i].moveTo(at.x+(i==1?-1.5:1.5),at.y,at.z+4,180,0);}
                var at=atRow(b,0,1);mine[0].moveTo(at.x,at.y,at.z,180,0);p[0].setYRot(180);mine[0].keys(true,false);for(var who:p)hush(who);
            })
            .thenWaitUntil(()->{
                var w=BikeNightSpecters.witch(p[0]);
                if(w!=null){
                    seen[0]=true;double x=w.getX()-b.getX();lowest[0]=Math.min(lowest[0],w.getY()-b.getY()-rows.get(0).top());west[0]=Math.min(west[0],x);east[0]=Math.max(east[0],x);
                    // She is her reader's alone: the others are not told of her at all.
                    own[0]&=BikeNightSpecters.is(w)&&w.gesture()==BikeNightSpecters.FLY&&w.owner().isPresent()&&w.owner().get().equals(p[0].getUUID())&&w.broadcastToPlayer(p[0])&&!w.broadcastToPlayer(p[1])&&!w.broadcastToPlayer(p[2]);
                    h.assertTrue(Math.abs(w.getZ()-b.getZ()-rows.get(0).z()-.5)<.01,"she keeps to her row");
                }
                h.assertTrue(!BikeNightHollow.taken(p[0]),"down over the bars, they are not caught");
                h.assertTrue(seen[0]&&w==null&&BikeNightSpecters.over(p[0])==1,"waiting for a witch to have come and gone over them: seen "+seen[0]+", over "+BikeNightSpecters.over(p[0]));
            })
            .thenExecute(()->{
                h.assertTrue(own[0],"she was there for her reader and nobody else");
                h.assertTrue(west[0]<rows.get(0).x()-BikeNightSpecters.FROM+1&&east[0]>rows.get(0).x()+BikeNightSpecters.FROM-1,"she crossed the way from one side to the other: "+west[0]+" to "+east[0]);
                h.assertTrue(Math.abs(lowest[0]-BikeNightSpecters.LOW)<.1,"as low as a rider's head over the way, and no lower: "+lowest[0]);
                h.assertTrue(sounded(p[0],"hollow.cackle")&&!sounded(p[1],"hollow.cackle")&&!sounded(p[2],"hollow.cackle"),"her reader heard her coming, and nobody else did");
                h.assertTrue(p[0].getVehicle()==mine[0]&&BikeNightHollow.fell(p[0])==0&&BikeNightSpecters.had(p[0])==1&&BikeNightSpecters.had(p[1])==0&&BikeNightSpecters.had(p[2])==0,"they are still riding; nobody on the hill has had a witch come");
                mine[0].keys(false,false);
            })
            // That row is done with for them: standing in it again, nothing comes.
            .thenIdle(40)
            .thenExecute(()->{
                h.assertTrue(BikeNightSpecters.witch(p[0])==null&&BikeNightSpecters.had(p[0])==1,"her first row is crossed once for a reader");
                // Half a block short of her second row, sitting up.
                var at=atRow(b,1,1);mine[0].moveTo(at.x,at.y,at.z,180,0);p[0].setYRot(180);mine[0].steady(0,0);hush(p[0]);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNightHollow.taken(p[0]),"waiting for the witch at the second row to catch them: over "+BikeNightSpecters.over(p[0])))
            .thenExecute(()->{
                var out=new Vec3(b.getX()+BikeNightGraveyard.GATE_X,b.getY(),b.getZ()+CalderTown.RAILS+3.5);
                h.assertTrue(BikeNightSpecters.witch(p[0])==null&&BikeNightSpecters.had(p[0])==2&&BikeNightSpecters.over(p[0])==1,"she is gone, having caught them");
                h.assertTrue(!p[0].isPassenger()&&p[0].position().distanceTo(out)<.05&&Math.abs(Mth.wrapDegrees(p[0].getYRot()-180))<1,"they are on the pavement outside the cemetery's front gate, on foot, facing it: "+where(b,p[0]));
                h.assertTrue(l.noCollision(p[0])&&!l.noCollision(p[0].getBoundingBox().move(0,-.1,0)),"with room to stand there and ground under them");
                h.assertTrue(p[0].getHealth()==health[0],"unhurt: "+p[0].getHealth());
                // The bike is gone: not under them, not where they were, not back in the rack.
                h.assertTrue(mine[0].isRemoved()&&l.getBlockState(rack).isAir(),"the Lamp is gone from under them and has not gone back to the rack");
                var near=atRow(b,1,1);int lying=0;for(var at:BlockPos.betweenClosed(BlockPos.containing(near.x-4,near.y-2,near.z-4),BlockPos.containing(near.x+4,near.y+3,near.z+4)))if(l.getBlockState(at).is(BikeNightRegistry.BIKE_BLOCK.get()))lying++;
                h.assertTrue(lying==0&&l.getEntitiesOfClass(NightBike.class,hollow(f),bike->bike.make()==NightBike.Make.LAMP).isEmpty(),"and is nowhere on the course");
                var rec=f.own(p[0],P);h.assertTrue(rec.getBoolean("Taken")&&rec.getString("TakenBy").equals("witch")&&!rec.getBoolean("Through"),"it is in their record, with what caught them: "+rec);
                // And they take no other bike tonight.
                var wasp=b.offset(BikeNightSite.bikeAt(NightBike.Make.WASP));hand(f,p[0],wasp);hand(f,p[0],wasp);
                h.assertTrue(!p[0].isPassenger()&&l.getBlockState(wasp).is(BikeNightRegistry.BIKE_BLOCK.get()),"a hand on another bike does not put them on it");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[0],BikeNightTexts.TAKEN)&&heard(p[0],BikeNightTexts.NO_MORE)&&OVER.getOrDefault(p[0].getUUID(),0)==BikeNightHollow.TAKEN_OVER&&sounded(p[0],"hollow.under"),"they are told, with the mist over them for as long as that takes"))
            .thenExecute(()->h.assertTrue(!BikeNightHollow.taken(p[1])&&!BikeNightHollow.taken(p[2])&&p[1].getVehicle()==mine[1]&&p[2].getVehicle()==mine[2],"the others ride on"))
            // The Hare, flat out up the fences from well short of her first row, sitting up: she times herself to them, and has them at her row.
            .thenExecute(()->{mine[1].moveTo(b.getX()+.5,b.getY()+1,b.getZ()-293.0,180,0);p[1].setYRot(180);lastZ[0]=0;})
            .thenWaitUntil(()->{
                if(!BikeNightHollow.taken(p[1])){p[1].setYRot(180);if(mine[1].onGround())mine[1].steady(0,RiddenBike.TOP);lastZ[0]=mine[1].getZ()-b.getZ();
                    h.assertTrue(lastZ[0]>rows.get(0).z()-1.2,"they did not get past her row: "+lastZ[0]);}
                h.assertTrue(BikeNightHollow.taken(p[1]),"waiting for her to catch the Hare's rider at her row: at "+lastZ[0]);
            })
            .thenExecute(()->h.assertTrue(Math.abs(lastZ[0]-rows.get(0).z()-.5)<1.6&&f.own(p[1],P).getString("TakenBy").equals("witch"),"caught at her row, going flat out: "+lastZ[0]))
            // The Hopper the same, but down over the bars from six blocks short: she goes over, and they ride on.
            .thenExecute(()->{mine[2].moveTo(b.getX()+.5,b.getY()+1,b.getZ()-293.0,180,0);p[2].setYRot(180);})
            .thenWaitUntil(()->{
                double z=mine[2].getZ()-b.getZ();
                if(BikeNightSpecters.over(p[2])==0){p[2].setYRot(180);if(mine[2].onGround())mine[2].steady(0,RiddenBike.TOP);if(z<=rows.get(0).z()+6)mine[2].keys(true,false);}
                h.assertTrue(!BikeNightHollow.taken(p[2]),"down over the bars, the Hopper's rider is not caught: at "+z);
                h.assertTrue(BikeNightSpecters.over(p[2])==1,"waiting for her to go over the Hopper's rider: at "+z);
            })
            .thenExecute(()->h.assertTrue(p[2].getVehicle()==mine[2]&&Math.abs(mine[2].getZ()-b.getZ()-rows.get(0).z()-.5)<1.6,"she went over them at her row, and they are still riding: "+where(b,mine[2])))
            .thenSucceed();
    }
    @AfterBatch(batch="hollow_witch") public static void hollowWitchDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.500, the ghosts
    @GameTest(template="empty",batch="hollow_ghosts",timeoutTicks=5000)
    public static void ghostsDriftAcrossAHauntedStretchForOneReaderHalfThereAndOneThatGoesThroughThemChillsTheirViewAndCatchesNobody(GameTestHelper h){
        var f=park(h);BikeNightSpecters.keptAway=false;BikeNightSpecters.only=java.util.Set.of("ghosts");var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final NightBike[] mine={null};
        final int[] most={0},come={0};final boolean[] own={true},thin={false},full={false};final double[] west={99},east={-99};final float[] health={0};final LiteraryActor[] inside={null};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                // The haunted stretches are on the plain way.
                for(var haunt:BikeNightSpecters.HAUNTS)h.assertTrue(CalderHollow.WAY.stream().anyMatch(piece->piece.zSouth()==haunt.zSouth())&&CalderHollow.WAY.stream().anyMatch(piece->piece.zNorth()==haunt.zNorth()),"a haunted stretch begins and ends with pieces of the plain way: "+haunt);
                p[0]=f.player("hollow_ghosts_lamp",P);p[1]=f.player("hollow_ghosts_hill",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&BikeNight.here(p[1]),"waiting for the night"))
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.LAMP);health[0]=p[0].getHealth();var hill=islandPlace(b,CalderHollow.HILL);p[1].teleportTo(hill.x,hill.y,hill.z+4);})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp"),"waiting for the Lamp to be theirs"))
            // Standing on the foot of the stairs, which are haunted.
            .thenExecute(()->{mine[0].moveTo(b.getX()+.5,b.getY()+1,b.getZ()-370.5,180,0);p[0].setYRot(180);hush(p[0]);hush(p[1]);})
            .thenWaitUntil(()->h.assertTrue(BikeNightSpecters.ghosts(p[0]).size()==1,"waiting for a ghost to come for them"))
            .thenExecute(()->{
                var g=BikeNightSpecters.ghosts(p[0]).get(0);double x=g.getX()-b.getX(),z=g.getZ()-b.getZ(),y=g.getY()-b.getY();
                h.assertTrue(Math.abs(Math.abs(x-.5)-BikeNightSpecters.OUT)<.3&&z<=-370.5-BikeNightSpecters.AHEAD+.01&&z>=-370.5-BikeNightSpecters.AHEAD-3.01,"it comes from nine blocks to one side, five to eight ahead of them: "+x+", "+z);
                double ground=CalderHollow.top(0,(int)Math.floor(z));h.assertTrue(!Double.isNaN(ground)&&Math.abs(y-ground-BikeNightSpecters.OVER_GROUND)<.15,"a little over the stairs where it will cross them: "+y+" over "+ground);
                h.assertTrue(BikeNightSpecters.ghost(g.phase())&&g.gesture()==BikeNightSpecters.DRIFT&&g.fade()>BikeNightSpecters.GHOST_FADE,"one of the three, adrift, and thin as it comes: "+g.phase()+", "+g.fade());
            })
            // They come one after another, never more than two; each is this reader's alone; none of them catches anybody.
            .thenExecuteFor(330,()->{
                var ghosts=BikeNightSpecters.ghosts(p[0]);most[0]=Math.max(most[0],ghosts.size());
                for(var g:ghosts){
                    own[0]&=g.owner().isPresent()&&g.owner().get().equals(p[0].getUUID())&&g.broadcastToPlayer(p[0])&&!g.broadcastToPlayer(p[1]);
                    thin[0]|=g.fade()>90;full[0]|=g.fade()==BikeNightSpecters.GHOST_FADE;h.assertTrue(g.fade()>=BikeNightSpecters.GHOST_FADE,"never more than half there: "+g.fade());
                    double x=g.getX()-b.getX();west[0]=Math.min(west[0],x);east[0]=Math.max(east[0],x);
                }
                h.assertTrue(!BikeNightHollow.taken(p[0])&&p[0].getVehicle()==mine[0]&&BikeNightHollow.fell(p[0])==0,"nothing happens to somebody they do not touch");
            })
            .thenExecute(()->{
                h.assertTrue(most[0]==BikeNightSpecters.AT_ONCE&&BikeNightSpecters.ghostsCome(p[0])>=4,"one after another, two at a time at the most: "+most[0]+" at once, "+BikeNightSpecters.ghostsCome(p[0])+" in all");
                h.assertTrue(own[0]&&thin[0]&&full[0],"each is their reader's alone, thin as it comes and goes and half there between: "+own[0]+", "+thin[0]+", "+full[0]);
                h.assertTrue(west[0]<.5-BikeNightSpecters.OUT+1&&east[0]>.5+BikeNightSpecters.OUT-1,"they cross the way from one side to the other: "+west[0]+" to "+east[0]);
                h.assertTrue(BikeNightSpecters.ghostsCome(p[1])==0&&BikeNightSpecters.ghosts(p[1]).isEmpty()&&BikeNightSpecters.chills(p[0])==0,"none comes for the reader on the hill, and none has gone through anybody");
            })
            // Into one's way as it crosses the stairs: it goes through them.
            .thenWaitUntil(()->{
                for(var g:BikeNightSpecters.ghosts(p[0])){
                    double x=g.getX()-b.getX(),z=g.getZ()-b.getZ();
                    if(Math.abs(x-.5)<.6&&BikeNightSpecters.chills(p[0])==0){double ground=CalderHollow.top(0,(int)Math.floor(z));mine[0].moveTo(g.getX(),b.getY()+ground,g.getZ(),180,0);mine[0].steady(0,0);inside[0]=g;}
                }
                h.assertTrue(BikeNightSpecters.chills(p[0])==1,"waiting for a ghost to go through them");
            })
            .thenWaitUntil(()->h.assertTrue(sounded(p[0],"hollow.ghost")&&OVER.getOrDefault(p[0].getUUID(),0)==BikeNightSpecters.CHILL&&SHARE.getOrDefault(p[0].getUUID(),0)==BikeNightSpecters.CHILL_SHARE,"they hear it, and some of the mist is over them for a moment: "+OVER.get(p[0].getUUID())+" ticks, "+SHARE.get(p[0].getUUID())+" of a hundred"))
            // Kept inside it for a second as it drifts on: it chills them the once. (The other one that is out may go through them too.)
            .thenExecuteFor(20,()->{
                if(inside[0]==null||!inside[0].isAlive())return;double x=inside[0].getX()-b.getX(),z=inside[0].getZ()-b.getZ();
                if(Math.abs(x-.5)<1.4){mine[0].moveTo(inside[0].getX(),b.getY()+CalderHollow.top(0,(int)Math.floor(z)),inside[0].getZ(),180,0);mine[0].steady(0,0);}
            })
            .thenExecute(()->{
                h.assertTrue(BikeNightSpecters.chills(p[0])>=1&&BikeNightSpecters.chills(p[0])<=BikeNightSpecters.AT_ONCE,"a ghost chills its reader once, however long they are in it: "+BikeNightSpecters.chills(p[0])+" chills from twenty ticks inside one");
                h.assertTrue(!BikeNightHollow.taken(p[0])&&p[0].getVehicle()==mine[0]&&p[0].getHealth()==health[0]&&!sounded(p[1],"hollow.ghost"),"they are not caught or hurt, and are still riding; the reader on the hill heard nothing");
                // Off the haunted stretch, onto the fences: no more come, and the ones that are out go.
                var at=islandPlace(b,4);mine[0].moveTo(at.x,at.y,at.z,180,0);come[0]=BikeNightSpecters.ghostsCome(p[0]);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNightSpecters.ghosts(p[0]).isEmpty(),"waiting for the ghosts that were out to have gone"))
            .thenIdle(BikeNightSpecters.EVERY*2)
            .thenExecute(()->h.assertTrue(BikeNightSpecters.ghosts(p[0]).isEmpty()&&BikeNightSpecters.ghostsCome(p[0])==come[0],"off the haunted stretch no more come: "+BikeNightSpecters.ghostsCome(p[0])+" after "+come[0]))
            .thenSucceed();
    }
    @AfterBatch(batch="hollow_ghosts") public static void hollowGhostsDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.501, the werewolf
    @GameTest(template="empty",batch="hollow_wolf",timeoutTicks=5000)
    public static void theWerewolfRunsUpBesideOneReaderAndLeapsTheGapOnHisRhythmAndCatchesWhoeverIsOverItAsHeCrossesAndNobodyOnTheGround(GameTestHelper h){
        var f=park(h);BikeNightSpecters.keptAway=false;BikeNightSpecters.only=java.util.Set.of("wolf");var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final NightBike[] mine={null,null};
        final double[] west={99},east={-99},highest={0},ran={0};final boolean[] own={true},gathered={false},air={false},held={false};final int[] t={0},leapt={0};final long[] landed={0};
        var g=CalderHollow.LEAPS.get(0);
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                // His ground: beside two of the plain way's gaps, level with them, and nowhere within two blocks of ground anybody rides.
                h.assertTrue(CalderHollow.LEAPS.size()==2&&CalderHollow.WOLFS.size()==6,"two gaps, and a run and two perches at each");
                for(var leap:CalderHollow.LEAPS){
                    var gap=hollowGap(leap.south()+.5);
                    h.assertTrue(gap[0]==2&&Math.abs(gap[1]-.5)<.01&&leap.south()-leap.north()==2&&leap.z()==(leap.south()+leap.north())/2,"one of the plain way's gaps of two, by its brinks: "+leap);
                    for(double x:new double[]{leap.west(),leap.east()})h.assertTrue(CalderHollow.top((int)Math.floor(x),(int)Math.floor(leap.z()))==leap.top()&&CalderHollow.stood(x,leap.top(),leap.z())==-1,"a perch at "+x+", which is ground and nobody's island");
                    h.assertTrue(CalderHollow.top((int)Math.floor(leap.west()-.5),(int)Math.floor(leap.run()))==leap.top(),"his run begins on his own ground: "+leap);
                }
                for(var piece:CalderHollow.WOLFS){
                    for(var way:CalderHollow.ALL){
                        boolean apartX=piece.x0()>way.x1()+2||piece.x1()<way.x0()-2,apartZ=piece.zNorth()>way.zSouth()+2||piece.zSouth()<way.zNorth()-2;
                        h.assertTrue(apartX||apartZ,piece.name()+" is within two blocks of "+way.name());
                    }
                    h.assertTrue(l.getBlockState(b.offset(piece.x0(),(int)piece.top()-1,piece.zNorth())).is(BikeNightRegistry.material("hollow_sod")),piece.name()+" is sod");
                }
                p[0]=f.player("hollow_wolf_lamp",P);p[1]=f.player("hollow_wolf_hare",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&BikeNight.here(p[1]),"waiting for the night"))
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.LAMP);mine[1]=take(h,f,p[1],NightBike.Make.HARE);})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp")&&f.own(p[1],P).getString("Bike").equals("hare"),"waiting for the bikes to be theirs"))
            // The Hare's rider waits on the hill. The Lamp's stands on the first grave, a block short of the brink.
            .thenExecute(()->{
                var hill=islandPlace(b,CalderHollow.HILL);mine[1].moveTo(hill.x,hill.y,hill.z+4,180,0);
                mine[0].moveTo(b.getX()+g.x(),b.getY()+g.top(),b.getZ()+g.south()+1,180,0);p[0].setYRot(180);mine[0].steady(0,0);hush(p[0]);hush(p[1]);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNightSpecters.wolf(p[0])!=null,"waiting for him to come"))
            .thenExecute(()->{var w=BikeNightSpecters.wolf(p[0]);ran[0]=w.getZ()-b.getZ();
                h.assertTrue(w.phase()==BikeNightSpecters.WOLF&&Math.abs(w.getZ()-b.getZ()-g.run())<.6&&Math.abs(w.getX()-b.getX()-g.west()+.5)<.01,"he comes at the far end of his run, beside the way: "+where(b,w));})
            // He runs up it to his perch, howls, and leaps the gap on his rhythm, back and forth. On the ground, a block from the brink, they are not his.
            .thenExecuteFor(BikeNightSpecters.WOLF_EVERY*4+60,()->{
                var w=BikeNightSpecters.wolf(p[0]);h.assertTrue(w!=null,"he stays while they are at his gap");
                double x=w.getX()-b.getX();west[0]=Math.min(west[0],x);east[0]=Math.max(east[0],x);highest[0]=Math.max(highest[0],w.getY()-b.getY()-g.top());
                own[0]&=w.owner().isPresent()&&w.owner().get().equals(p[0].getUUID())&&w.broadcastToPlayer(p[0])&&!w.broadcastToPlayer(p[1]);
                gathered[0]|=w.gesture()==BikeNightSpecters.CROUCH;air[0]|=w.gesture()==BikeNightSpecters.LEAP;
                if(w.gesture()==BikeNightSpecters.LEAP)h.assertTrue(Math.abs(w.getZ()-b.getZ()-g.z())<.01,"he crosses over the middle of the gap");
                h.assertTrue(!BikeNightHollow.taken(p[0])&&p[0].getVehicle()==mine[0],"on the ground they are not his, however near he passes");
            })
            .thenExecute(()->{
                h.assertTrue(own[0]&&gathered[0]&&air[0],"he is his reader's alone; he gathers and he leaps: "+own[0]+", "+gathered[0]+", "+air[0]);
                h.assertTrue(west[0]<g.west()+.01&&east[0]>g.east()-.01&&BikeNightSpecters.leaps(p[0])>=3,"from one perch to the other and back, over and over: "+west[0]+" to "+east[0]+", "+BikeNightSpecters.leaps(p[0])+" leaps");
                h.assertTrue(Math.abs(highest[0]-BikeNightSpecters.WOLF_HIGH)<.15,"in an arc over the gap: "+highest[0]);
                h.assertTrue(sounded(p[0],"hollow.howl")&&!sounded(p[1],"hollow.howl")&&BikeNightSpecters.wolf(p[1])==null,"his reader heard him howl; the reader on the hill has no wolf and heard nothing");
            })
            // Just after he lands is the time: a tap at the brink, over, and on. He goes when they are well past.
            .thenWaitUntil(()->{
                if(leapt[0]==0)leapt[0]=BikeNightSpecters.leaps(p[0]);
                h.assertTrue(BikeNightSpecters.leaps(p[0])>leapt[0],"waiting for him to land again");
            })
            .thenExecute(()->{landed[0]=l.getGameTime();mine[0].moveTo(b.getX()+g.x(),b.getY()+g.top(),b.getZ()+g.south()+1.6,170,0);mine[0].steady(0,RiddenBike.TOP);})
            .thenWaitUntil(()->{
                double x=mine[0].getX()-b.getX(),z=mine[0].getZ()-b.getZ();
                h.assertTrue(!BikeNightHollow.taken(p[0])&&p[0].getVehicle()==mine[0]&&BikeNightHollow.fell(p[0])==0,"they are not caught and do not go down: at "+where(b,mine[0]));
                p[0].setYRot(mine[0].onGround()?(float)Math.toDegrees(Math.atan2(-(2.5-x),-3.5)):mine[0].getYRot());
                if(mine[0].onGround()){mine[0].steady(0,RiddenBike.TOP);boolean brink=z-g.south()<=.4&&z>g.south();
                    if(brink&&!held[0]){mine[0].keys(true,false);held[0]=true;}else if(held[0]){mine[0].keys(false,false);held[0]=false;}}
                h.assertTrue(BikeNightHollow.island(p[0])==2&&BikeNightSpecters.wolf(p[0])==null,"waiting for them to be on the second grave and him to be gone: at "+where(b,mine[0])+", island "+BikeNightHollow.island(p[0]));
            })
            .thenExecute(()->h.assertTrue(l.getGameTime()-landed[0]<200,"across within a few seconds of his landing"))
            // The Hare's rider, over the gap as he crosses: his.
            .thenExecute(()->{mine[1].moveTo(b.getX()+g.x(),b.getY()+g.top(),b.getZ()+g.south()+1,180,0);p[1].setYRot(180);mine[1].steady(0,0);hush(p[1]);})
            .thenWaitUntil(()->{
                var w=BikeNightSpecters.wolf(p[1]);
                // In the air and nearly at the middle of the way: out over the gap, in front of him.
                if(w!=null&&w.gesture()==BikeNightSpecters.LEAP&&Math.abs(w.getX()-b.getX()-g.x())<2.2&&!BikeNightHollow.taken(p[1]))mine[1].moveTo(b.getX()+g.x(),b.getY()+g.top()+.4,b.getZ()+g.z(),180,0);
                h.assertTrue(BikeNightHollow.taken(p[1]),"waiting for him to catch the Hare's rider over the gap");
            })
            .thenExecute(()->{
                var out=new Vec3(b.getX()+BikeNightGraveyard.GATE_X,b.getY(),b.getZ()+CalderTown.RAILS+3.5);
                h.assertTrue(BikeNightSpecters.wolf(p[1])==null&&!p[1].isPassenger()&&p[1].position().distanceTo(out)<.05&&mine[1].isRemoved(),"he is gone, and they are outside the cemetery's front gate on foot with the Hare gone: "+where(b,p[1]));
                h.assertTrue(f.own(p[1],P).getString("TakenBy").equals("wolf")&&!BikeNightHollow.taken(p[0]),"it is in their record that it was him; the Lamp's rider is where they were");
            }).thenSucceed();
    }
    @AfterBatch(batch="hollow_wolf") public static void hollowWolfDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.502, the headless rider
    @GameTest(template="empty",batch="hollow_rider",timeoutTicks=5000)
    public static void theHeadlessRiderComesUpBehindOneReaderOnTheLastStretchAndHasWhoeverStopsAndNotWhoeverKeepsGoing(GameTestHelper h){
        var f=park(h);BikeNightSpecters.keptAway=false;BikeNightSpecters.only=java.util.Set.of("rider");var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final NightBike[] mine={null,null};
        final long[] came={0};final double[] nearest={99},first={0};final boolean[] own={true},held={false};final int[] t={0};int stretch=CalderHollow.FAR_ISLAND-2,top=7;
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{p[0]=f.player("hollow_rider_lamp",P);p[1]=f.player("hollow_rider_hare",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&BikeNight.here(p[1]),"waiting for the night"))
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.LAMP);mine[1]=take(h,f,p[1],NightBike.Make.HARE);})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp")&&f.own(p[1],P).getString("Bike").equals("hare"),"waiting for the bikes to be theirs"))
            // On the top, both: he does not come for anybody who is not on the last stretch.
            .thenExecute(()->{var at=islandPlace(b,top);for(int i=0;i<2;i++){mine[i].moveTo(b.getX()+(i==0?-1.5:2.5),b.getY()+5,b.getZ()-391.5,180,0);p[i].setYRot(180);hush(p[i]);}})
            .thenIdle(40)
            .thenExecute(()->{
                h.assertTrue(BikeNightSpecters.rider(p[0])==null&&BikeNightSpecters.rider(p[1])==null,"on the top, nobody is behind anybody");
                // The Lamp's rider onto the last stretch, and stops there.
                var at=islandPlace(b,stretch);mine[0].moveTo(at.x,at.y,at.z,180,0);mine[0].steady(0,0);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNightSpecters.rider(p[0])!=null,"waiting for him to come up behind the Lamp's rider"))
            .thenExecute(()->{
                var r=BikeNightSpecters.rider(p[0]);came[0]=l.getGameTime();first[0]=r.getZ()-mine[0].getZ();
                h.assertTrue(r.phase()==BikeNightSpecters.RIDER&&r.gesture()==BikeNightSpecters.GALLOP&&Math.abs(first[0]-BikeNightSpecters.RIDER_BEHIND)<.5,"he comes ten blocks behind them, on the way: "+first[0]+" behind, at "+where(b,r));
                h.assertTrue(Math.abs(r.getY()-b.getY()-CalderHollow.top(0,(int)Math.floor(r.getZ()-b.getZ())))<.01,"on the ground that is there");
            })
            .thenWaitUntil(()->{
                var r=BikeNightSpecters.rider(p[0]);
                if(r!=null){
                    double off=r.getZ()-mine[0].getZ();h.assertTrue(off<nearest[0]+.001,"he comes on and never falls back: "+off);nearest[0]=off;
                    own[0]&=r.owner().isPresent()&&r.owner().get().equals(p[0].getUUID())&&r.broadcastToPlayer(p[0])&&!r.broadcastToPlayer(p[1]);
                }
                h.assertTrue(BikeNightHollow.taken(p[0]),"waiting for him to reach the reader who has stopped: "+nearest[0]+" off");
            })
            .thenExecute(()->{
                long took=l.getGameTime()-came[0];double should=(BikeNightSpecters.RIDER_BEHIND-BikeNightSpecters.RIDER_REACH)/BikeNightSpecters.RIDER_PACE;
                h.assertTrue(Math.abs(took-should)<6&&nearest[0]<BikeNightSpecters.RIDER_REACH+BikeNightSpecters.RIDER_PACE+.01,"at his own pace, in about two seconds: "+took+" ticks for "+should);
                h.assertTrue(own[0]&&sounded(p[0],"hollow.hooves")&&!sounded(p[1],"hollow.hooves"),"he was his reader's alone, who heard his horse; the other did not");
                h.assertTrue(BikeNightSpecters.rider(p[0])==null&&f.own(p[0],P).getString("TakenBy").equals("rider")&&!p[0].isPassenger()&&mine[0].isRemoved(),"they are caught, and it is in their record that it was him");
                h.assertTrue(!BikeNightHollow.taken(p[1])&&BikeNightSpecters.rider(p[1])==null,"the reader on the top is not his");
            })
            // The Hare's rider rides the last stretch without stopping: he comes, and does not catch them.
            .thenExecute(()->{var at=islandPlace(b,stretch);mine[1].moveTo(at.x,at.y,at.z+2,180,0);p[1].setYRot(180);nearest[0]=99;})
            .thenWaitUntil(()->{
                double x=mine[1].getX()-b.getX(),z=mine[1].getZ()-b.getZ();
                h.assertTrue(!BikeNightHollow.taken(p[1])&&p[1].getVehicle()==mine[1]&&BikeNightHollow.fell(p[1])==0,"the rider who keeps going is not caught and does not go down: at "+where(b,mine[1])+", him "+nearest[0]+" off");
                var r=BikeNightSpecters.rider(p[1]);if(r!=null)nearest[0]=Math.min(nearest[0],r.getZ()-mine[1].getZ());
                p[1].setYRot(mine[1].onGround()?(float)Math.toDegrees(Math.atan2(-(.5-x),-3.5)):mine[1].getYRot());
                var gap=hollowGap(z);boolean brink=gap[1]<=.4&&mine[1].onGround();
                if(gap[0]>=2&&brink&&!held[0]){mine[1].keys(true,false);held[0]=true;}else if(held[0]){mine[1].keys(false,false);held[0]=false;}
                if(mine[1].onGround())pedalWell(mine[1],t[0]++,3);
                h.assertTrue(BikeNightHollow.island(p[1])==CalderHollow.FAR_ISLAND&&BikeNightSpecters.rider(p[1])==null,"waiting for them to be on the far side and him to have pulled up: at "+where(b,mine[1]));
            })
            .thenExecute(()->h.assertTrue(nearest[0]>3&&nearest[0]<90,"he came, and was never within three blocks of them: "+nearest[0]))
            .thenSucceed();
    }
    @AfterBatch(batch="hollow_rider") public static void hollowRiderDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.503, what jumps out
    private static Vec3 popPlace(BlockPos b,int i,double aside,double south){var pop=BikeNightSpecters.POPS.get(i);return new Vec3(b.getX()+pop.x()+aside,b.getY()+pop.top(),b.getZ()+pop.z()+south);}

    @GameTest(template="empty",batch="hollow_pops",timeoutTicks=6000)
    public static void whatJumpsOutIsInEveryBikesWayAndStopsOneRiddenIntoItAndIsGoneRoundOrHoppedOverAndHasNobody(GameTestHelper h){
        var f=park(h);BikeNightSpecters.keptAway=false;BikeNightSpecters.only=java.util.Set.of("pops");var b=f.base(P);var l=f.level(P);f.build(P);
        final ServerPlayer[] p={null,null,null};final NightBike[] mine={null,null,null};var pops=BikeNightSpecters.POPS;
        final boolean[] seen={false},up={false},shared={true};final double[] lowest={99},highest={-99},far={0};final long[] since={0};final float[] health={0};final int[] t={0};final Vec3[] stood={null};
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                h.assertTrue(pops.size()==6,"six places: "+pops);
                for(var pop:pops){
                    double top=CalderHollow.top((int)Math.floor(pop.x()),(int)Math.floor(pop.z()));
                    if(pop.phase()==BikeNightSpecters.GOBLIN){
                        // A goblin's place is on a grave, and where it hides is behind a stone that is there and is taller than it is.
                        h.assertTrue(top==pop.top()&&pop.outY()==top&&pop.fromY()==top,"a goblin's place is on the way, at its height: "+pop);
                        var stone=l.getBlockState(b.offset((int)Math.floor(pop.fromX()),(int)top,(int)Math.floor(pop.fromZ())));
                        h.assertTrue(stone.getBlock() instanceof CalderGraveBlock&&stone.getShape(l,BlockPos.ZERO).max(net.minecraft.core.Direction.Axis.Y)>=.8,"behind a stone that hides it: "+pop+", "+stone);
                        h.assertTrue(pop.fromZ()-Math.floor(pop.fromZ())<.3&&Math.hypot(pop.fromX()-pop.x(),pop.fromZ()-pop.z())>1.5,"on the stone's far side as a rider comes, a jump from the way: "+pop);
                    }else h.assertTrue(pop.fromX()==pop.x()&&pop.fromZ()==pop.z()&&pop.fromY()==pop.top()-BikeNightSpecters.GHOUL_UNDER&&pop.outY()==pop.top()-BikeNightSpecters.GHOUL_OUT,"a ghoul comes straight up: "+pop);
                }
                // The ghoul between the lids comes up through nothing, with a lid either side; the vine's two through the vine's middle.
                var lid=pops.get(2);h.assertTrue(Double.isNaN(CalderHollow.top(0,-323))&&CalderHollow.top(0,-322)==lid.top()&&CalderHollow.top(-1,-324)==lid.top(),"between two lids");
                for(int i=4;i<6;i++){var v=pops.get(i);h.assertTrue(CalderHollow.top((int)Math.floor(v.x()-.5),(int)Math.floor(v.z()))==v.top()&&CalderHollow.top((int)Math.floor(v.x()+.4),(int)Math.floor(v.z()))==v.top(),"in the middle of the vine: "+v);}
                p[0]=f.player("hollow_pops_lamp",P);p[1]=f.player("hollow_pops_hare",P);p[2]=f.player("hollow_pops_wasp",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&p[2].getBbHeight()<1.1&&BikeNight.here(p[2]),"waiting for the night"))
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.LAMP);mine[1]=take(h,f,p[1],NightBike.Make.HARE);mine[2]=take(h,f,p[2],NightBike.Make.WASP);health[0]=p[0].getHealth();})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp")&&f.own(p[1],P).getString("Bike").equals("hare")&&f.own(p[2],P).getString("Bike").equals("wasp"),"waiting for the bikes to be theirs"))
            // The other two wait on the hill. The Lamp's rider is on the first lid, five blocks short of the ghoul's place, and rides at it flat out.
            .thenExecute(()->{
                for(int i=1;i<3;i++){var at=islandPlace(b,CalderHollow.HILL);mine[i].moveTo(at.x+(i==1?-1.5:1.5),at.y,at.z+4,180,0);}
                var at=popPlace(b,2,0,5);mine[0].moveTo(at.x,at.y,at.z,180,0);p[0].setYRot(180);far[0]=99;for(var who:p)hush(who);
            })
            .thenWaitUntil(()->{
                var a=BikeNightSpecters.sprung(l,b,2);var pop=pops.get(2);
                if(a!=null){
                    seen[0]=true;double y=a.getY()-b.getY();lowest[0]=Math.min(lowest[0],y);highest[0]=Math.max(highest[0],y);up[0]|=a.gesture()==BikeNightSpecters.UP;
                    // It is the course's: nobody's own, and everybody is told of it.
                    shared[0]&=BikeNightSpecters.is(a)&&a.phase()==BikeNightSpecters.GHOUL&&a.owner().isEmpty()&&a.broadcastToPlayer(p[0])&&a.broadcastToPlayer(p[1]);
                    h.assertTrue(Math.abs(a.getX()-b.getX()-pop.x())<.01&&Math.abs(a.getZ()-b.getZ()-pop.z())<.01,"it comes straight up: "+where(b,a));
                }
                if(p[0].getVehicle()==mine[0]){
                    p[0].setYRot(180);if(mine[0].onGround())mine[0].steady(0,RiddenBike.TOP);far[0]=mine[0].getZ()-b.getZ();
                    h.assertTrue(far[0]>pop.z()-.2,"they did not ride through it: "+far[0]);
                }
                h.assertTrue(BikeNightSpecters.stopped(p[0])==1&&p[0].getVehicle()==null,"waiting for the Lamp's rider to ride into it and come off: at "+far[0]+", seen "+seen[0]);
            })
            .thenExecute(()->{
                var pop=pops.get(2);
                h.assertTrue(seen[0]&&up[0]&&shared[0],"a ghoul came up, for everybody: seen "+seen[0]+", up "+up[0]+", shared "+shared[0]);
                h.assertTrue(Math.abs(lowest[0]-pop.fromY())<.01&&Math.abs(highest[0]-pop.outY())<.01,"from wholly under the lids to its chest over them: "+lowest[0]+" to "+highest[0]);
                h.assertTrue(sounded(p[0],"hollow.pop")&&!sounded(p[1],"hollow.pop"),"whoever is near heard it come; nobody back on the hill did");
                h.assertTrue(far[0]>pop.z()+.2&&far[0]<pop.z()+BikeNightSpecters.POP_REACH+.4,"stopped at it: "+far[0]);
                h.assertTrue(p[0].isAlive()&&!BikeNightHollow.taken(p[0])&&p[0].getHealth()>=health[0]-1&&BikeNightSpecters.hopped(p[0])==0,"they are off their bike as off any wall, and nothing has them: "+where(b,p[0])+", health "+p[0].getHealth());
                since[0]=l.getGameTime();
            })
            .thenWaitUntil(()->h.assertTrue(BikeNightSpecters.sprung(l,b,2)==null,"waiting for it to go back"))
            // On foot, five blocks short of its place again: it is quiet for a while.
            .thenExecute(()->{
                h.assertTrue(l.getGameTime()-since[0]<=BikeNightSpecters.POP_STAY+BikeNightSpecters.POP_BACK+2,"it stays as long as it stays, and goes: "+(l.getGameTime()-since[0]));
                var at=popPlace(b,2,0,5);p[0].teleportTo(at.x,at.y,at.z);
            })
            .thenIdle(60)
            .thenExecute(()->h.assertTrue(BikeNightSpecters.sprung(l,b,2)==null,"its place is quiet for a while after"))
            // The Wasp's rider comes onto the grave past the lids a block and a half to one side of where its goblin jumps to, and rides by.
            .thenExecute(()->{var at=popPlace(b,3,1.6,3.2);mine[2].moveTo(at.x,at.y,at.z,180,0);p[2].setYRot(180);seen[0]=false;up[0]=false;far[0]=99;hush(p[2]);})
            .thenWaitUntil(()->{
                var a=BikeNightSpecters.sprung(l,b,3);var pop=pops.get(3);
                if(a!=null){
                    if(!seen[0]){seen[0]=true;h.assertTrue(a.phase()==BikeNightSpecters.GOBLIN&&Math.abs(a.getX()-b.getX()-pop.fromX())<.01&&Math.abs(a.getZ()-b.getZ()-pop.fromZ())<.01&&a.getBbHeight()<.8,"a goblin, small, behind its stone: "+where(b,a)+", "+a.getBbHeight()+" high");}
                    if(a.gesture()==BikeNightSpecters.UP){up[0]=true;h.assertTrue(Math.abs(a.getX()-b.getX()-pop.x())<.01&&Math.abs(a.getZ()-b.getZ()-pop.z())<.01&&Math.abs(a.getY()-b.getY()-pop.top())<.01,"out, it is in the way, on the ground: "+where(b,a));}
                }
                double z=mine[2].getZ()-b.getZ();p[2].setYRot(180);if(mine[2].onGround())mine[2].steady(0,z>pop.z()-1.5?RiddenBike.TOP:0);
                if(Math.abs(z-pop.z())<.5)far[0]=Math.min(far[0],Math.abs(mine[2].getX()-b.getX()-pop.x()));
                h.assertTrue(p[2].getVehicle()==mine[2],"they are still riding: "+where(b,mine[2]));
                h.assertTrue(z<pop.z()-1.5&&seen[0]&&up[0],"waiting for them to ride by it: at "+z+", seen "+seen[0]+", up "+up[0]);
            })
            .thenExecute(()->h.assertTrue(BikeNightSpecters.stopped(p[2])==0&&far[0]>BikeNightSpecters.POP_REACH&&far[0]<3&&sounded(p[2],"hollow.pop"),"round it, it stops nobody: passed "+far[0]+" off"))
            // The Hare's rider, on the third grave three blocks short of where its goblin jumps to, rides at it more slowly than a bike comes off at.
            .thenExecute(()->{var at=popPlace(b,1,0,3);mine[1].moveTo(at.x,at.y,at.z,180,0);p[1].setYRot(180);far[0]=99;t[0]=0;})
            .thenWaitUntil(()->{
                var pop=pops.get(1);double z=mine[1].getZ()-b.getZ();far[0]=Math.min(far[0],z);
                p[1].setYRot(180);if(mine[1].onGround())mine[1].steady(0,.12F);
                h.assertTrue(p[1].getVehicle()==mine[1],"going slowly, they do not come off: "+where(b,mine[1]));
                h.assertTrue(z>pop.z()+.2,"and do not get through it, however long they push: "+z);
                if(BikeNightSpecters.stopped(p[1])==1)t[0]++;
                h.assertTrue(t[0]>=20,"waiting for the Hare's rider to be stopped by it, and kept there: at "+z);
            })
            .thenExecute(()->{var pop=pops.get(1);h.assertTrue(far[0]>pop.z()+BikeNightSpecters.POP_REACH-.15&&p[1].getHealth()==health[0]&&!BikeNightHollow.taken(p[1]),"kept at its edge, unhurt: "+far[0]);})
            // It goes back, and they ride on over where it was.
            .thenWaitUntil(()->{
                var pop=pops.get(1);double z=mine[1].getZ()-b.getZ();p[1].setYRot(180);if(mine[1].onGround())mine[1].steady(0,z>pop.z()-1.5?.12F:0);
                h.assertTrue(p[1].getVehicle()==mine[1],"still riding: "+where(b,mine[1]));
                h.assertTrue(z<pop.z()-1.5&&BikeNightSpecters.sprung(l,b,1)==null,"waiting for it to go back and for them to ride on over its place: at "+z);
            })
            .thenExecute(()->h.assertTrue(BikeNightSpecters.stopped(p[1])==1&&BikeNightSpecters.hopped(p[1])==0,"stopped the once, and that is not a hop"))
            // The Wasp's rider, on the vine a little short of its second ghoul, winds the hop while it comes up, and goes over it.
            .thenExecute(()->{var at=popPlace(b,5,0,2.3);mine[2].moveTo(at.x,at.y,at.z,180,0);p[2].setYRot(180);mine[2].steady(0,0);mine[2].keys(true,false);lowest[0]=99;})
            .thenWaitUntil(()->{var a=BikeNightSpecters.sprung(l,b,5);h.assertTrue(a!=null&&a.gesture()==BikeNightSpecters.UP&&mine[2].crouch()>=.99F,"waiting for the vine's ghoul to be up and the hop wound: "+mine[2].crouch());})
            .thenWaitUntil(()->{
                var pop=pops.get(5);double z=mine[2].getZ()-b.getZ(),y=mine[2].getY()-b.getY()-pop.top();p[2].setYRot(180);
                if(mine[2].onGround()){if(z>pop.z()+1.6)mine[2].steady(0,RiddenBike.TOP);else if(z>pop.z())mine[2].keys(false,false);else mine[2].steady(0,0);}
                if(Math.abs(z-pop.z())<BikeNightSpecters.POP_REACH)lowest[0]=Math.min(lowest[0],y);
                h.assertTrue(p[2].getVehicle()==mine[2]&&BikeNightSpecters.sprung(l,b,5)!=null,"they are still riding, and it is still up: "+where(b,mine[2]));
                h.assertTrue(z<pop.z()-1&&mine[2].onGround(),"waiting for them to come down past it: at "+z+", "+y+" over");
            })
            .thenExecute(()->h.assertTrue(lowest[0]>=BikeNightSpecters.POP_CLEAR&&lowest[0]<3,"over it, well clear: "+lowest[0]))
            .thenWaitUntil(()->h.assertTrue(BikeNightSpecters.sprung(l,b,5)==null,"waiting for it to go back"))
            .thenExecute(()->h.assertTrue(BikeNightSpecters.hopped(p[2])==1&&BikeNightSpecters.stopped(p[2])==0&&p[2].getVehicle()==mine[2],"they hopped it, and it stopped nobody"))
            // The ghoul between the lids comes up again in its time, for somebody on foot: who is only startled.
            .thenWaitUntil(()->{var a=BikeNightSpecters.sprung(l,b,2);h.assertTrue(a!=null&&a.gesture()==BikeNightSpecters.UP,"waiting for the ghoul between the lids to come up again, for somebody on foot");})
            .thenExecute(()->{var at=popPlace(b,2,0,.7);p[0].teleportTo(at.x,at.y,at.z);stood[0]=p[0].position();})
            .thenIdle(10)
            .thenExecute(()->{
                h.assertTrue(BikeNightSpecters.sprung(l,b,2)!=null&&p[0].position().distanceTo(stood[0])<.01&&BikeNightSpecters.stopped(p[0])==1,"standing at it, they are where they stood: "+where(b,p[0]));
                for(var who:p)h.assertTrue(who.isAlive()&&!BikeNightHollow.taken(who)&&BikeNightHollow.fell(who)==0,"and it has had nobody, and put nobody down: "+who.getGameProfile().getName());
            })
            .thenSucceed();
    }
    @AfterBatch(batch="hollow_pops") public static void hollowPopsDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.504, the steep way
    /** Whether every block of the steep way's bank is earth ({@code there}), or every one is air. */
    private static boolean bankIs(ServerLevel l,BlockPos b,boolean there){
        var earth=BikeNightRegistry.material("hollow_earth");
        for(int i=0;i<CalderHollow.BANK_ROWS;i++)for(int x=CalderHollow.BANK_X0;x<=CalderHollow.BANK_X1;x++)for(int y=CalderHollow.BANK_FOOT-1;y<=CalderHollow.BANK_FOOT+i;y++){
            var s=l.getBlockState(b.offset(x,y,CalderHollow.BANK_SOUTH-i));if(there?!s.is(earth):!s.isAir())return false;
        }
        return true;
    }
    /**
     * One rider at the steep way: from the south end of its turn, north along its run and onto the loose earth, with a push on the
     * pedals every {@code every} ticks ({@code flat}, if it is anything, is the speed they are kept at as far as the loose earth;
     * {@code card} holds the Rattler's card from there). The step ends when they are on the head, or after {@code ticks}.
     * {@code far[i]} is the furthest north they got, {@code high[i]} the highest, {@code on[i]} how long they were on the bank.
     */
    private static net.minecraft.gametest.framework.GameTestSequence steepRide(net.minecraft.gametest.framework.GameTestSequence q,GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer[] p,NightBike[] mine,int i,int every,float flat,boolean card,int ticks,double[] far,double[] high,int[] on){
        var b=f.base(P);final int[] t={0};
        return q.thenExecute(()->{mine[i].moveTo(b.getX()+4.0,b.getY()+CalderHollow.BANK_FOOT,b.getZ()-370.6,180,0);p[i].setYRot(180);mine[i].steady(0,flat);far[i]=99;high[i]=0;on[i]=0;})
            .thenWaitUntil(()->{
                h.assertTrue(p[i].getVehicle()==mine[i],"they are still on it: "+where(b,p[i]));
                double x=mine[i].getX()-b.getX(),y=mine[i].getY()-b.getY(),z=mine[i].getZ()-b.getZ();boolean loose=z<=CalderHollow.LOOSE_SOUTH+1;
                far[i]=Math.min(far[i],z);high[i]=Math.max(high[i],y);if(CalderHollow.bankRow(x,z)>=0&&y>CalderHollow.BANK_FOOT+.5)on[i]++;
                p[i].setYRot(mine[i].onGround()?(float)Math.toDegrees(Math.atan2(-(5.0-x),-3.5)):mine[i].getYRot());
                if(card&&loose)mine[i].keys(false,true);
                if(mine[i].onGround()){if(flat>0&&!loose)mine[i].steady(0,flat);else pedalWell(mine[i],t[0],every);}
                boolean up=mine[i].onGround()&&z<CalderHollow.BANK_SOUTH-CalderHollow.BANK_ROWS+.5&&Math.abs(y-CalderHollow.BANK_FOOT-CalderHollow.BANK_ROWS)<.05;
                h.assertTrue(up||++t[0]>=ticks,"waiting for them to be on the head, or to have had long enough: at "+where(b,mine[i])+" going "+mine[i].speedNow());
            })
            .thenExecute(()->{mine[i].keys(false,false);mine[i].steady(0,0);});
    }

    @GameTest(template="empty",batch="hollow_steep",timeoutTicks=9000)
    public static void theSteepWaysLooseEarthStopsEveryBikeAtItsBankButTheHareWhichIsUpItBeforeItSlidesAway(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null,null,null};final NightBike[] mine={null,null,null,null};
        final double[] far=new double[4],high=new double[4];final int[] on=new int[4];final long[] since={0};
        int island=islandNamed("the steep way's turn and the loose earth"),head=islandNamed("the steep way's head");double face=CalderHollow.BANK_SOUTH+1;
        var q=h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                var way=CalderHollow.STEEP;var earth=BikeNightRegistry.material("hollow_earth");int loose=0,bank=0;
                // It is nothing else's ground, and each of its pieces runs on from the last.
                for(var piece:way)for(int x=piece.x0();x<=piece.x1();x++)for(int z=piece.zNorth();z<=piece.zSouth();z++){int over=0;for(var other:CalderHollow.ALL)if(other.has(x,z))over++;h.assertTrue(over==1,piece.name()+" shares "+x+","+z);}
                for(int i=0;i+1<way.size();i++)h.assertTrue(way.get(i+1).zSouth()==way.get(i).zNorth()-1&&way.get(i+1).x0()<=way.get(i).x1()&&way.get(i+1).x1()>=way.get(i).x0(),way.get(i+1).name()+" runs on from "+way.get(i).name());
                // Off the stairs' first pair, level with it; onto the last stretch's first piece, level with it.
                h.assertTrue(CalderHollow.top(1,-371)==CalderHollow.BANK_FOOT&&CalderHollow.top(2,-371)==CalderHollow.BANK_FOOT,"its turn is off the stairs' first pair, at its height");
                h.assertTrue(CalderHollow.top(1,-397)==CalderHollow.top(2,-397)&&CalderHollow.top(2,-397)==CalderHollow.BANK_FOOT+CalderHollow.BANK_ROWS,"its head is beside the last stretch's first piece, at its height");
                for(var piece:way){
                    if(piece.made()==CalderHollow.Made.EARTH){loose++;h.assertTrue(piece.top()==CalderHollow.BANK_FOOT&&l.getBlockState(b.offset(piece.x0(),CalderHollow.BANK_FOOT-1,piece.zNorth())).is(earth),piece.name()+" is loose earth, level with the run");}
                    if(piece.made()==CalderHollow.Made.BANK){h.assertTrue(piece.top()==CalderHollow.BANK_FOOT+1+bank,piece.name()+" is a whole block up from the last");bank++;}
                }
                h.assertTrue(loose==CalderHollow.LOOSE_ROWS&&bank==CalderHollow.BANK_ROWS&&bankIs(l,b,true),"rows of loose earth, and the bank, earth down to its foot: "+loose+" and "+bank);
                // The bank is no island; what is under it and what is over it are.
                var under=CalderHollow.ISLANDS.get(island);var over=CalderHollow.ISLANDS.get(head);
                h.assertTrue(under.way().equals("steep")&&over.way().equals("steep")&&under.zFar()==CalderHollow.BANK_SOUTH+1&&over.zNear()==CalderHollow.BANK_SOUTH-CalderHollow.BANK_ROWS,"an island up to the bank and one past it: "+under+", "+over);
                for(int i=0;i<CalderHollow.BANK_ROWS;i++)h.assertTrue(CalderHollow.stood(CalderHollow.BANK_X0+1,CalderHollow.BANK_FOOT+1+i,CalderHollow.BANK_SOUTH-i+.5)==-1,"the bank's row "+i+" is nobody's island");
                String[] names={"hollow_steep_lamp","hollow_steep_comet","hollow_steep_hare","hollow_steep_rattler"};for(int i=0;i<4;i++)p[i]=f.player(names[i],P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[3].getBbHeight()<1.1&&BikeNight.here(p[3]),"waiting for the night"))
            .thenExecute(()->{NightBike.Make[] makes={NightBike.Make.LAMP,NightBike.Make.COMET,NightBike.Make.HARE,NightBike.Make.RATTLER};for(int i=0;i<4;i++)mine[i]=take(h,f,p[i],makes[i]);})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp")&&f.own(p[1],P).getString("Bike").equals("comet")&&f.own(p[2],P).getString("Bike").equals("hare")&&f.own(p[3],P).getString("Bike").equals("rattler"),"waiting for the bikes to be theirs"))
            .thenExecute(()->{for(int i=0;i<4;i++){var at=islandPlace(b,CalderHollow.HILL);mine[i].moveTo(at.x-3+2*i,at.y,at.z+4,180,0);}});
        // A plain bike, pedalled as fast as hands can: a push every other tick, ten a second.
        q=steepRide(q,h,f,p,mine,0,2,0,false,260,far,high,on)
            .thenExecute(()->{
                h.assertTrue(far[0]<face+1&&far[0]>face+.05&&high[0]<CalderHollow.BANK_FOOT+.05,"the Lamp is ridden to the bank's face and no further, and never up it: "+far[0]+", "+high[0]+" high");
                h.assertTrue(mine[0].speedNow()<.2&&!BikeNightHollow.bankGoing(b)&&!BikeNightHollow.bankGone(b)&&BikeNightHollow.island(p[0])==island,"at a crawl; the bank has had nobody on it; the ground under it is their island");
            });
        // The Comet, flat out as far as the loose earth, and after that a push every other tick: ten a second.
        q=steepRide(q,h,f,p,mine,1,2,.46F,false,200,far,high,on)
            .thenExecute(()->{
                h.assertTrue(far[1]<face+1&&far[1]>face+.05&&high[1]<CalderHollow.BANK_FOOT+.05,"the Comet's speed is gone in the loose earth, and it is not up the bank either: "+far[1]+", "+high[1]+" high");
                h.assertTrue(!BikeNightHollow.bankGoing(b)&&!BikeNightHollow.bankGone(b),"the bank has had nobody on it");
            });
        // The Hare, with a push every fifth tick: four a second.
        q=steepRide(q,h,f,p,mine,2,5,0,false,400,far,high,on)
            .thenExecute(()->{
                h.assertTrue(far[2]<CalderHollow.BANK_SOUTH-CalderHollow.BANK_ROWS+.5&&Math.abs(high[2]-CalderHollow.BANK_FOOT-CalderHollow.BANK_ROWS)<.05,"the Hare is up the bank and on the head: "+far[2]+", "+high[2]+" high");
                h.assertTrue(on[2]>0&&on[2]<BikeNightHollow.BANK_HOLDS-10,"in time, at the slowest it can be pedalled up at all: "+on[2]+" ticks on it");
                h.assertTrue(BikeNightHollow.ways(p[2]).contains("steep")&&BikeNightHollow.fell(p[2])==0,"the steep way is one they have had under them: "+BikeNightHollow.ways(p[2]));
                h.assertTrue(BikeNightHollow.bankGoing(b)&&bankIs(l,b,true),"the bank is whole yet, and will go");
            })
            .thenWaitUntil(()->h.assertTrue(BikeNightHollow.island(p[2])==head,"waiting for the head to be their island"))
            // It goes when its time is up, with nobody on it, and comes back.
            .thenWaitUntil(()->h.assertTrue(BikeNightHollow.bankGone(b),"waiting for the bank to slide away"))
            .thenExecute(()->{h.assertTrue(bankIs(l,b,false)&&!BikeNightHollow.bankGoing(b),"all of it is gone");h.assertTrue(p[2].getVehicle()==mine[2]&&BikeNightHollow.fell(p[2])==0,"the Hare's rider is past it");since[0]=l.getGameTime();})
            .thenWaitUntil(()->h.assertTrue(!BikeNightHollow.bankGone(b),"waiting for the bank to be back"))
            .thenExecute(()->h.assertTrue(bankIs(l,b,true)&&l.getGameTime()-since[0]>=BikeNightHollow.BANK_BACK-1,"whole again, after its time: "+(l.getGameTime()-since[0])));
        // The Rattler, with a push every third tick and its card held from the loose earth: its burst has it up too.
        q=steepRide(q,h,f,p,mine,3,3,0,true,400,far,high,on)
            .thenExecute(()->h.assertTrue(far[3]<CalderHollow.BANK_SOUTH-CalderHollow.BANK_ROWS+.5&&Math.abs(high[3]-CalderHollow.BANK_FOOT-CalderHollow.BANK_ROWS)<.05&&on[3]<BikeNightHollow.BANK_HOLDS,"the Rattler in its burst is up it too: "+far[3]+", "+high[3]+" high, "+on[3]+" ticks on it"))
            .thenWaitUntil(()->h.assertTrue(BikeNightHollow.bankGone(b),"waiting for the bank to go again"))
            .thenWaitUntil(()->h.assertTrue(!BikeNightHollow.bankGone(b)&&!BikeNightHollow.bankGoing(b),"waiting for the bank to be back again"))
            // The Hare's rider again: onto the bank's first step, and stops there.
            .thenExecute(()->{mine[2].moveTo(b.getX()+5.0,b.getY()+CalderHollow.BANK_FOOT,b.getZ()+face+1.2,180,0);p[2].setYRot(180);mine[2].steady(0,.2F);hush(p[2]);})
            .thenWaitUntil(()->{
                double y=mine[2].getY()-b.getY(),z=mine[2].getZ()-b.getZ();
                // On until the middle of the bike is over the step, and no further.
                if(mine[2].onGround())mine[2].steady(0,z>face-.55?.2F:0);
                h.assertTrue(mine[2].onGround()&&Math.abs(y-CalderHollow.BANK_FOOT-1)<.05&&z<=face-.55&&BikeNightHollow.bankGoing(b),"waiting for the Hare to be wholly on the bank's first step: "+where(b,mine[2]));
            })
            .thenExecute(()->since[0]=l.getGameTime())
            .thenWaitUntil(()->{
                if(mine[2].onGround()&&!BikeNightHollow.bankGone(b))mine[2].steady(0,0);
                h.assertTrue(BikeNightHollow.fell(p[2])==1,"waiting for the bank to go from under them, and for them to have gone down: "+where(b,mine[2]));
            })
            .thenExecute(()->{
                h.assertTrue(BikeNightHollow.bankGone(b)&&bankIs(l,b,false)&&l.getGameTime()-since[0]>=BikeNightHollow.BANK_HOLDS-2,"it held as long as it holds, and went: "+(l.getGameTime()-since[0]));
                var at=islandPlace(b,island);
                h.assertTrue(p[2].getVehicle()==mine[2]&&mine[2].position().distanceTo(at)<.05&&BikeNightHollow.island(p[2])==island,"they are back on the run under it, on their bike: "+where(b,mine[2]));
                h.assertTrue(sounded(p[2],"hollow.slide")&&sounded(p[2],"hollow.under"),"having heard it go, and what was under it");
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[2],BikeNightTexts.UNDER_RIDING),"and they are told"));
        q.thenSucceed();
    }
    @AfterBatch(batch="hollow_steep") public static void hollowSteepDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.505, the tight way
    /** Where across the tendril the line through its gates is at a row (or a part of one): through each opening's middle, swinging between. */
    private static double tightLine(double row){
        int gates=(CalderHollow.TIGHT_ROWS-1-CalderHollow.GATE_FIRST)/CalderHollow.GATE_EVERY+1;double last=CalderHollow.GATE_FIRST+CalderHollow.GATE_EVERY*(gates-1.0);
        double west=CalderHollow.TIGHT_X0+.5,mid=(CalderHollow.TIGHT_X0+CalderHollow.TIGHT_X1+1)/2.0;
        if(row<=CalderHollow.GATE_FIRST)return west;
        return mid-(mid-west)*Math.cos(Math.PI*(Math.min(row,last+CalderHollow.GATE_EVERY)-CalderHollow.GATE_FIRST)/CalderHollow.GATE_EVERY);
    }
    /**
     * One rider at the tight way: from the high way's foot into the tendril and along the line through its gates, looking {@code
     * lead} rows ahead, with a push on the pedals every {@code every} ticks and going no faster than {@code cap} (if that is
     * anything). The step ends when they are on the tight way's foot ({@code out[i]} 0), off their bike (1), gone down (2), or
     * after {@code ticks} (3). {@code far[i]} is the furthest north they rode; {@code took[i]} how long it was.
     */
    private static net.minecraft.gametest.framework.GameTestSequence tightRide(net.minecraft.gametest.framework.GameTestSequence q,GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer[] p,NightBike[] mine,int i,int every,float cap,double lead,int ticks,double[] far,int[] out,int[] took){
        var b=f.base(P);final int[] t={0,0};
        return q.thenExecute(()->{mine[i].moveTo(b.getX()+CalderHollow.TIGHT_X0+.5,b.getY(),b.getZ()+CalderHollow.TIGHT_SOUTH+2.5,180,0);p[i].setYRot(180);mine[i].steady(0,0);far[i]=99;out[i]=-1;took[i]=0;t[0]=0;t[1]=BikeNightHollow.fell(p[i]);})
            .thenWaitUntil(()->{
                double x=mine[i].getX()-b.getX(),y=mine[i].getY()-b.getY(),z=mine[i].getZ()-b.getZ();boolean riding=p[i].getVehicle()==mine[i],down=BikeNightHollow.fell(p[i])>t[1];
                if(riding&&!down){
                    far[i]=Math.min(far[i],z);double row=CalderHollow.TIGHT_SOUTH+.5-z;
                    // The way the line is going a little ahead, and back onto it by how far off it they are.
                    double slope=(tightLine(row+lead+.1)-tightLine(row+lead-.1))/.2,off=tightLine(row)-x;
                    p[i].setYRot(mine[i].onGround()?(float)Math.toDegrees(Math.atan2(-(slope+1.5*off),-1)):mine[i].getYRot());
                    if(mine[i].onGround()){pedalWell(mine[i],t[0],every);if(cap>0&&mine[i].speedNow()>cap)mine[i].steady(mine[i].leanNow(),cap);}
                }
                boolean up=riding&&!down&&mine[i].onGround()&&z<CalderHollow.TIGHT_SOUTH-CalderHollow.TIGHT_ROWS-1.5&&Math.abs(y-1)<.05;
                if(up||down||!riding||++t[0]>=ticks){out[i]=up?0:!riding?1:down?2:3;took[i]=t[0];}
                h.assertTrue(out[i]>=0,"waiting for them to be through, off, down, or to have had long enough: at "+where(b,mine[i]));
            })
            // Out of the way, on their bike, on the hill: so that the tendril can come back.
            .thenExecute(()->{
                var hill=islandPlace(b,CalderHollow.HILL);var make=mine[i].make();
                if(p[i].getVehicle()!=mine[i]){p[i].teleportTo(hill.x,hill.y,hill.z+4);remount(h,f,p[i],mine,i,make);}
                if(p[i].getVehicle() instanceof NightBike bike){mine[i]=bike;bike.keys(false,false);bike.steady(0,0);bike.moveTo(hill.x-5+2*i,hill.y,hill.z+4,180,0);}
            })
            .thenWaitUntil(()->h.assertTrue(p[i].getVehicle()==mine[i]&&!BikeNightHollow.curlGoing(b)&&BikeNightHollow.curlGone(b)==0,"waiting for them to be on their bike on the hill, and the tendril whole again: "+where(b,p[i])+", "+BikeNightHollow.curlGone(b)+" rows gone"));
    }

    @GameTest(template="empty",batch="hollow_tight",timeoutTicks=9000)
    public static void theTightWayCurlsUpBehindWhoeverIsInItAndOnlyTheWaspIsThroughItsGatesInTime(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p=new ServerPlayer[3];final NightBike[] mine=new NightBike[3];
        final double[] far=new double[3];final int[] out=new int[3],took=new int[3];final long[] since={0};
        NightBike.Make[] makes={NightBike.Make.WASP,NightBike.Make.LAMP,NightBike.Make.COMET};var vine=BikeNightRegistry.material("vine");
        int rows=CalderHollow.TIGHT_ROWS,foot=islandNamed("the tight way's step and the tight way's foot");
        // Where the second gate's wall faces a rider coming up to it, and the third's.
        double second=CalderHollow.TIGHT_SOUTH-CalderHollow.GATE_FIRST-CalderHollow.GATE_EVERY+1,third=second-CalderHollow.GATE_EVERY;
        var q=h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                var way=CalderHollow.TIGHT;int gates=0;
                // It is nothing else's ground; it runs on from the high way's foot, and onto the steep way's turn by the stairs' foot.
                for(var piece:way)for(int x=piece.x0();x<=piece.x1();x++)for(int z=piece.zNorth();z<=piece.zSouth();z++){int over=0;for(var other:CalderHollow.ALL)if(other.has(x,z))over++;h.assertTrue(over==1,piece.name()+" shares "+x+","+z);}
                h.assertTrue(CalderHollow.top(CalderHollow.TIGHT_X0,CalderHollow.TIGHT_SOUTH+1)==0&&CalderHollow.top(CalderHollow.TIGHT_X0,CalderHollow.TIGHT_SOUTH)==0,"its near end is level with the high way's foot, and runs on from it");
                var last=way.get(way.size()-1);h.assertTrue(CalderHollow.top(last.x0(),last.zNorth()-1)==last.top()&&last.top()==CalderHollow.BANK_FOOT,"its foot runs onto the steep way's turn, level with it: "+last);
                // Walls either side, three high; a gate every six rows from the sixth, open one block wide, west and east by turns.
                for(int row=0;row<rows;row++){
                    int z=CalderHollow.TIGHT_SOUTH-row,gate=CalderHollow.gate(row);if(gate>=0)gates++;
                    for(int x=CalderHollow.TIGHT_X0-1;x<=CalderHollow.TIGHT_X1+1;x++){
                        h.assertTrue(l.getBlockState(b.offset(x,-1,z)).is(vine),"the tendril's floor at "+x+","+z);
                        boolean wall=x<CalderHollow.TIGHT_X0||x>CalderHollow.TIGHT_X1||gate>=0&&x!=CalderHollow.gateOpen(gate);
                        for(int y=0;y<CalderHollow.GATE_HIGH;y++)h.assertTrue(wall?l.getBlockState(b.offset(x,y,z)).is(vine):l.getBlockState(b.offset(x,y,z)).isAir(),(wall?"a wall":"nothing")+" at "+x+","+y+","+z+": "+l.getBlockState(b.offset(x,y,z)));
                    }
                    h.assertTrue(CalderHollow.stood(CalderHollow.TIGHT_X0+.5,0,z+.5)==-1,"the tendril's row "+row+" is nobody's island");
                }
                h.assertTrue(gates==4&&CalderHollow.gate(CalderHollow.GATE_FIRST)==0&&CalderHollow.gateOpen(0)==CalderHollow.TIGHT_X0&&CalderHollow.gateOpen(1)==CalderHollow.TIGHT_X1&&CalderHollow.gateOpen(2)==CalderHollow.TIGHT_X0,"four gates, west and east by turns: "+gates);
                h.assertTrue(CalderHollow.ISLANDS.get(foot).way().equals("tight"),"its foot is an island of its own");
                for(int i=0;i<3;i++)p[i]=f.player("hollow_tight_"+makes[i].getSerializedName(),P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[2].getBbHeight()<1.1&&BikeNight.here(p[2]),"waiting for the night"))
            .thenExecute(()->{for(int i=0;i<3;i++)mine[i]=take(h,f,p[i],makes[i]);})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("wasp")&&f.own(p[1],P).getString("Bike").equals("lamp")&&f.own(p[2],P).getString("Bike").equals("comet"),"waiting for the bikes to be theirs"))
            .thenExecute(()->{for(int i=0;i<3;i++){var at=islandPlace(b,CalderHollow.HILL);mine[i].moveTo(at.x-5+2*i,at.y,at.z+4,180,0);}
                h.assertTrue(!BikeNightHollow.curlGoing(b)&&BikeNightHollow.curlGone(b)==0,"the tendril is whole and still before anybody is on it");});
        // A plain bike, pedalled as hard as it goes: it cannot be turned into the second gate, and its rider comes off at the gate's wall.
        q=tightRide(q,h,f,p,mine,1,3,0,.5,700,far,out,took)
            .thenExecute(()->h.assertTrue(out[1]==1&&far[1]>second&&far[1]<second+1,"the Lamp's rider comes off at the second gate's wall: "+out[1]+", at "+far[1]+" after "+took[1]));
        // The same, going slowly enough to be turned through a gate: the tendril has gone from under it before the third.
        q=tightRide(q,h,f,p,mine,1,3,.18F,.5,700,far,out,took)
            .thenExecute(()->h.assertTrue(out[1]==2&&far[1]>third,"going slowly enough to turn, the Lamp's rider goes down with the tendril before the third gate: "+out[1]+", at "+far[1]+" after "+took[1]));
        // The Comet, flat out: no better.
        q=tightRide(q,h,f,p,mine,2,3,0,.5,700,far,out,took)
            .thenExecute(()->h.assertTrue(out[2]!=0&&far[2]>CalderHollow.TIGHT_SOUTH-rows,"the Comet's rider is not through it either: "+out[2]+", at "+far[2]+" after "+took[2]));
        // The Wasp, pedalled as hard: through every gate and onto the foot, ahead of it.
        q=q.thenExecute(()->since[0]=BikeNightHollow.fell(p[0]));
        q=tightRide(q,h,f,p,mine,0,3,0,.5,700,far,out,took)
            .thenExecute(()->{
                h.assertTrue(out[0]==0&&far[0]<CalderHollow.TIGHT_SOUTH-rows-1.5,"the Wasp is through every gate and on the tight way's foot: "+out[0]+", at "+far[0]+" after "+took[0]);
                h.assertTrue(BikeNightHollow.ways(p[0]).contains("tight")&&BikeNightHollow.fell(p[0])==since[0],"the tight way is one they have had under them, and they did not go down: "+BikeNightHollow.ways(p[0]));
                h.assertTrue(took[0]<BikeNightHollow.CURL_GRACE+rows/BikeNightHollow.CURL_PACE,"in less time than the tendril takes to go: "+took[0]);
            });
        // And with a push every fourth tick, it is through still.
        q=tightRide(q,h,f,p,mine,0,4,0,.5,700,far,out,took)
            .thenExecute(()->h.assertTrue(out[0]==0,"a Wasp pushed five times a second is through it too: "+out[0]+", at "+far[0]+" after "+took[0]));
        // The tendril itself: somebody standing in its first row sets it going; it goes row by row, walls and gates and all, and comes back whole.
        q.thenExecute(()->{p[1].stopRiding();p[1].teleportTo(b.getX()+CalderHollow.TIGHT_X0+2.5,b.getY(),b.getZ()+CalderHollow.TIGHT_SOUTH+.5);})
            .thenWaitUntil(()->h.assertTrue(BikeNightHollow.curlGoing(b),"waiting for somebody on foot in it to set it going"))
            .thenExecute(()->{since[0]=l.getGameTime();h.assertTrue(BikeNightHollow.curlGone(b)==0&&l.getBlockState(b.offset(CalderHollow.TIGHT_X0,-1,CalderHollow.TIGHT_SOUTH)).is(vine),"none of it has gone yet");})
            .thenWaitUntil(()->h.assertTrue(BikeNightHollow.curlGone(b)>=CalderHollow.GATE_FIRST+1,"waiting for it to have curled up past its first gate"))
            .thenExecute(()->{
                long took2=l.getGameTime()-since[0];int gone=BikeNightHollow.curlGone(b);
                h.assertTrue(Math.abs(gone-(took2-BikeNightHollow.CURL_GRACE)*BikeNightHollow.CURL_PACE)<1.5,"at its pace, after its grace: "+gone+" rows in "+took2);
                int z=CalderHollow.TIGHT_SOUTH-CalderHollow.GATE_FIRST;
                for(int x=CalderHollow.TIGHT_X0-1;x<=CalderHollow.TIGHT_X1+1;x++)for(int y=-1;y<CalderHollow.GATE_HIGH;y++)h.assertTrue(l.getBlockState(b.offset(x,y,z)).isAir(),"its first gate's row is gone, floor and walls and gate: "+x+","+y);
                h.assertTrue(l.getBlockState(b.offset(CalderHollow.TIGHT_X0,-1,CalderHollow.TIGHT_SOUTH-rows+1)).is(vine),"its far end is there yet");
                // They go down with it (a reader in a case is moved down by hand).
                p[1].teleportTo(p[1].getX(),b.getY()-4,p[1].getZ());
            })
            .thenWaitUntil(()->h.assertTrue(BikeNightHollow.curlGone(b)==rows&&!BikeNightHollow.curlGoing(b),"waiting for all of it to have gone"))
            .thenExecute(()->{since[0]=l.getGameTime();h.assertTrue(sounded(p[1],"hollow.curl"),"heard going");})
            .thenWaitUntil(()->h.assertTrue(BikeNightHollow.curlGone(b)==0,"waiting for it to be back"))
            .thenExecute(()->{
                h.assertTrue(l.getGameTime()-since[0]>=BikeNightHollow.CURL_BACK-1,"after its time: "+(l.getGameTime()-since[0]));
                int z=CalderHollow.TIGHT_SOUTH-CalderHollow.GATE_FIRST;
                h.assertTrue(l.getBlockState(b.offset(CalderHollow.TIGHT_X0,-1,z)).is(vine)&&l.getBlockState(b.offset(CalderHollow.TIGHT_X0+1,0,z)).is(vine)&&l.getBlockState(b.offset(CalderHollow.TIGHT_X0,0,z)).isAir()&&l.getBlockState(b.offset(CalderHollow.TIGHT_X0-1,2,z)).is(vine),"whole again: floor, gate with its opening, walls");
            })
            .thenSucceed();
    }
    private static boolean remount(GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer p,NightBike[] mine,int i,NightBike.Make make){
        var b=f.base(P);var l=f.level(P);
        // Their bike is lying somewhere: a new one of its make, with them on it.
        for(var at:BlockPos.betweenClosed(b.offset(-12,-6,CalderHollow.FAR),b.offset(14,8,CalderHollow.EDGE+10)))if(l.getBlockState(at).is(BikeNightRegistry.BIKE_BLOCK.get())&&l.getBlockState(at).getValue(NightBikeBlock.MAKE)==make){hand(f,p,at.immutable());hand(f,p,at.immutable());break;}
        if(p.getVehicle() instanceof NightBike bike){mine[i]=bike;return true;}
        return false;
    }
    @AfterBatch(batch="hollow_tight") public static void hollowTightDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.506, the dark way
    @GameTest(template="empty",batch="hollow_dark",timeoutTicks=6000)
    public static void theDarkWaysBankIsTooThickToSeeTheNextIslandInButBesideALampThatIsThrowingItsLight(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null,null};final NightBike[] mine={null,null,null};
        var way=CalderHollow.DARK;int first=islandNamed("the dark way's first island"),second=islandNamed("the dark way's second island"),third=islandNamed("the dark way's third island");
        int top=CalderGraves.level(CalderHollow.EDGE),cut=Math.round(BikeNightGraveyard.DARK_MIST*BikeNightGraveyard.LAMP_CUTS);final boolean[] held={false};final int[] t={0},taps={0};
        var over=new java.util.TreeSet<Integer>();
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                var sod=BikeNightRegistry.material("hollow_sod");
                // Three islands, nothing else's ground, all of them in the bank.
                h.assertTrue(way.size()==3,"three islands: "+way);
                for(var piece:way)for(int x=piece.x0();x<=piece.x1();x++)for(int z=piece.zNorth();z<=piece.zSouth();z++){
                    int shared=0;for(var other:CalderHollow.ALL)if(other.has(x,z))shared++;
                    h.assertTrue(shared==1&&CalderHollow.dark(x+.5,z+.5)&&l.getBlockState(b.offset(x,(int)piece.top()-1,z)).is(sod),piece.name()+" at "+x+","+z+": its own, in the bank, and sod");
                }
                // The hill ends square at its mouth: three rows of nothing to the first island from wherever along it.
                for(int x=CalderHollow.DARK_MOUTH_W;x<=CalderHollow.DARK_MOUTH_E;x++){
                    h.assertTrue(l.getBlockState(b.offset(x,top-1,CalderHollow.EDGE)).is(sod)&&l.getBlockState(b.offset(x,top-1,CalderHollow.EDGE-1)).isAir(),"the hill's edge is square at the mouth: x="+x);
                    h.assertTrue(x<way.get(0).x0()||x>way.get(0).x1()||way.get(0).zSouth()==CalderHollow.EDGE-4,"three rows of nothing off the hill");
                }
                // Each gap is three rows; each island has one block's width in line with the last, and it is not the same block twice.
                int[] line=new int[2];
                for(int i=0;i+1<way.size();i++){
                    var a=way.get(i);var c=way.get(i+1);int lo=Math.max(a.x0(),c.x0()),hi=Math.min(a.x1(),c.x1());
                    h.assertTrue(c.zSouth()==a.zNorth()-4&&hi==lo,c.name()+" is three rows on from "+a.name()+" with one block in line: "+lo+".."+hi);line[i]=lo;
                    // Straight on from the middle of the nearer one is nothing.
                    double mid=(a.x0()+a.x1()+1)/2.0;for(double x:new double[]{mid-.3,mid+.3})for(int z=a.zNorth()-1;z>=c.zNorth();z--)h.assertTrue(Double.isNaN(CalderHollow.top((int)Math.floor(x),z)),"nothing straight on from the middle of "+a.name()+" at "+x+","+z);
                }
                h.assertTrue(line[0]!=line[1],"no one line through all three: "+line[0]+", "+line[1]);
                // And off the third's east end, two blocks over, the third grave, a block lower.
                var last=way.get(2);h.assertTrue(Double.isNaN(CalderHollow.top(last.x1()+1,last.zNorth()+2))&&Double.isNaN(CalderHollow.top(last.x1()+2,last.zNorth()+2))&&CalderHollow.top(last.x1()+3,last.zNorth()+2)==last.top()-1&&CalderHollow.stood(last.x1()+3.5,last.top()-1,last.zNorth()+2.5)==3,"the third grave is two blocks off its east end");
                p[0]=f.player("hollow_dark_lamp",P);p[1]=f.player("hollow_dark_wasp",P);p[2]=f.player("hollow_dark_walker",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&BikeNight.here(p[2]),"waiting for the night"))
            .thenExecute(()->{mine[0]=take(h,f,p[0],NightBike.Make.LAMP);mine[1]=take(h,f,p[1],NightBike.Make.WASP);})
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp")&&f.own(p[1],P).getString("Bike").equals("wasp"),"waiting for the bikes to be theirs"))
            // How thick it is. The Wasp's rider on the first island; the walker on the plain way's first grave; the Lamp standing on the hill's back, well away.
            .thenExecute(()->{
                var at=islandPlace(b,first);mine[1].moveTo(at.x,at.y,at.z,180,0);p[1].setYRot(180);
                var grave=islandPlace(b,1);p[2].teleportTo(grave.x,grave.y,grave.z);
                mine[0].moveTo(b.getX()+10.5,b.getY()+top,b.getZ()+CalderHollow.EDGE+2.5,180,0);p[0].setYRot(180);
            })
            .thenIdle(BikeNightGraveyard.MIST_EVERY+2)
            .thenExecute(()->{
                h.assertTrue(BikeNightGraveyard.mist(l,b,p[1])==BikeNightGraveyard.DARK_MIST&&toldMist(p[1])==BikeNightGraveyard.DARK_MIST,"on the dark way's first island the bank is as thick as it gets, and its rider is told: "+BikeNightGraveyard.mist(l,b,p[1]));
                h.assertTrue(BikeNightGraveyard.DARK_MIST>BikeNightGraveyard.MIST_BACK&&BikeNightGraveyard.mist(l,b,p[0])==BikeNightGraveyard.MIST_BACK,"thicker than the cemetery's thickest, which is what the hill's back has away from the mouth: "+BikeNightGraveyard.mist(l,b,p[0]));
                h.assertTrue(BikeNightGraveyard.mist(l,b,p[2])==0,"and there is none out on the plain way: "+BikeNightGraveyard.mist(l,b,p[2]));
                // The walker to the hill's edge at the mouth: the bank is over the hill there already.
                p[2].teleportTo(b.getX()+CalderHollow.DARK_MOUTH_W+2.5,b.getY()+top,b.getZ()+CalderHollow.EDGE+1.5);
            })
            .thenIdle(2)
            .thenExecute(()->{
                h.assertTrue(BikeNightGraveyard.mist(l,b,p[2])==BikeNightGraveyard.DARK_MIST,"the bank is over the hill at its mouth: "+BikeNightGraveyard.mist(l,b,p[2]));
                // The Lamp, standing by the Wasp on the first island: stopped, it throws no light and cuts nothing.
                var at=islandPlace(b,first);mine[0].moveTo(at.x+1,at.y,at.z,180,0);mine[0].steady(0,0);p[0].setYRot(180);
            })
            .thenIdle(12)
            .thenExecute(()->{
                h.assertTrue(mine[0].lit()==0&&BikeNightGraveyard.mist(l,b,p[0])==BikeNightGraveyard.DARK_MIST&&BikeNightGraveyard.mist(l,b,p[1])==BikeNightGraveyard.DARK_MIST,"a Lamp that is standing cuts nothing, for its rider or anybody: "+mine[0].lit()+", "+BikeNightGraveyard.mist(l,b,p[0]));
                // The Wasp's rider straight on from the middle of the first island, with the hop held: nothing is there.
                mine[1].keys(true,false);
            })
            .thenWaitUntil(()->{
                if(BikeNightHollow.fell(p[1])==0){p[1].setYRot(180);double z=mine[1].getZ()-b.getZ();
                    if(mine[1].onGround()){mine[1].steady(0,RiddenBike.TOP);if(z<way.get(0).zNorth()+.4)mine[1].keys(false,false);}}
                h.assertTrue(BikeNightHollow.fell(p[1])==1,"waiting for the Wasp's rider to go down off the first island: at "+where(b,mine[1]));
            })
            .thenExecute(()->{
                h.assertTrue(p[1].getVehicle()==mine[1]&&mine[1].position().distanceTo(islandPlace(b,first))<.05&&BikeNightHollow.island(p[1])==first,"straight on is nothing: they are set back on the first island: "+where(b,mine[1]));
                // The Lamp from the hill, on the line the first two islands share.
                mine[0].moveTo(b.getX()+way.get(1).x0()+.5,b.getY()+top,b.getZ()+CalderHollow.EDGE+3.5,180,0);p[0].setYRot(180);t[0]=0;held[0]=false;
            })
            .thenWaitUntil(()->{
                h.assertTrue(p[0].getVehicle()==mine[0]&&BikeNightHollow.fell(p[0])==0,"the Lamp's rider is still on it and has not gone down: "+where(b,mine[0]));
                double x=mine[0].getX()-b.getX(),y=mine[0].getY()-b.getY(),z=mine[0].getZ()-b.getZ();boolean ground=mine[0].onGround();
                int on=CalderHollow.stood(x,y,z);if(ground&&on>0)over.add(on);
                // While it is going its lamp is lit, and the bank is cut for its rider.
                if(ground&&mine[0].lit()>0&&CalderHollow.dark(x,z))h.assertTrue(BikeNightGraveyard.mist(l,b,p[0])==cut,"a Lamp that is throwing its light cuts the bank for its rider: "+BikeNightGraveyard.mist(l,b,p[0]));
                var a=way.get(0);var c=way.get(1);var d=way.get(2);
                if(on==third||ground&&z<d.zSouth()+1&&Math.abs(y-d.top())<.1){
                    // Along the third island to its east end, and a tap off it.
                    p[0].setYRot(-90);double brink=d.x1()+1-x;
                    if(ground){pedalWell(mine[0],t[0]++,3);if(brink<=.4&&!held[0]){mine[0].keys(true,false);held[0]=true;taps[0]++;}else if(held[0]){mine[0].keys(false,false);held[0]=false;}}
                }else if(ground){
                    // North: on the hill and the first island along the block they share with the second; on the second, for the third's west end.
                    boolean onSecond=z<c.zSouth()+1&&z>=c.zNorth();
                    double aimX=onSecond?d.x0()+1.5:c.x0()+.5,aimZ=onSecond?d.zSouth()+.5:z-3.5;
                    p[0].setYRot((float)Math.toDegrees(Math.atan2(-(aimX-x),aimZ-z)));pedalWell(mine[0],t[0]++,3);
                    // The hop held from the start of each run, and let go at its brink.
                    double brink=z>CalderHollow.EDGE?z-CalderHollow.EDGE:z>a.zNorth()?z-a.zNorth():z-c.zNorth();
                    if(brink>.4){mine[0].keys(true,false);held[0]=true;}else if(held[0]){mine[0].keys(false,false);held[0]=false;}
                }
                h.assertTrue(ground&&CalderHollow.stood(x,y,z)==3,"waiting for the Lamp to be ridden island to island and onto the third grave: at "+where(b,mine[0])+", over "+over);
            })
            .thenExecute(()->{
                mine[0].keys(false,false);mine[0].steady(0,0);
                h.assertTrue(over.containsAll(List.of(first,second,third)),"over all three islands: "+over);
                h.assertTrue(BikeNightHollow.ways(p[0]).contains("dark")&&BikeNightHollow.fell(p[0])==0&&taps[0]==1,"the dark way is one they have had under them, and they never went down: "+BikeNightHollow.ways(p[0]));
            })
            .thenWaitUntil(()->h.assertTrue(BikeNightHollow.island(p[0])==3,"waiting for the third grave to be their island"))
            // And whoever rides close: the walker beside the Lamp as it goes along the first island; not the Wasp's rider, away on the third.
            .thenExecute(()->{
                var at=islandPlace(b,first);mine[0].moveTo(at.x,at.y,at.z+1,180,0);p[0].setYRot(180);p[2].teleportTo(at.x+1.5,at.y,at.z);
                var far=islandPlace(b,third);mine[1].moveTo(far.x,far.y,far.z,180,0);mine[1].steady(0,0);
            })
            .thenWaitUntil(()->{
                if(mine[0].onGround())mine[0].steady(0,.1F);p[0].setYRot(180);
                h.assertTrue(mine[0].lit()>0&&BikeNightGraveyard.mist(l,b,p[2])==cut,"waiting for the Lamp's light to be cutting the bank for somebody beside it: "+mine[0].lit()+", "+BikeNightGraveyard.mist(l,b,p[2]));
            })
            .thenExecute(()->{
                h.assertTrue(p[1].distanceTo(mine[0])>BikeNightGraveyard.LAMP_NEAR&&BikeNightGraveyard.mist(l,b,p[1])==BikeNightGraveyard.DARK_MIST,"and for nobody out of its reach: "+BikeNightGraveyard.mist(l,b,p[1])+" at "+p[1].distanceTo(mine[0]));
                mine[0].steady(0,0);
            })
            .thenSucceed();
    }
    @AfterBatch(batch="hollow_dark") public static void hollowDarkDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.507, Gus in the Hollow
    /** How far somewhere (from the park's base) is from the nearest place on the Hollow's line. */
    private static double offTrail(Vec3 at){double best=99;for(var m:CalderHollow.TRAIL)best=Math.min(best,m.at().distanceTo(at));return best;}

    @GameTest(template="empty",batch="hollow_gus",timeoutTicks=14000)
    public static void gusRidesThePlainWayAfterHisFriendOverEveryGapSayingNothingAndIsAtTheBackGateWhenTheyAre(GameTestHelper h){
        var f=park(h);BikeNightGus.keptHome=false;BikeNightBullies.keptAway=false;BikeNightBullies.arriveAt=60;BikeNightBullies.patience=120;BikeNightSpecters.keptAway=false;
        var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null};final LiteraryActor[] gus={null};final PackBike[] his={null};
        var trail=CalderHollow.TRAIL;int last=trail.size()-1,level=CalderGraves.level(CalderHollow.EDGE);
        final double[] off={0},high={0},low={99},north={0};final int[] spoke={0},specters={0},t={0};final boolean[] rode={true};final String[] lastSaid={""};final int[] seen={0};var words=new ArrayList<String>();
        // What is watched while he is on the line: that he is on it, on his bike, saying nothing, and that nothing of the Hollow's is his.
        Runnable watch=()->{
            var at=his[0].position().subtract(b.getX(),b.getY(),b.getZ());double along=BikeNightHollow.gusTrail(b);
            // A line he begins out there, not the end of one he began before it.
            var now=saying(gus[0]);
            // (0.4.509: the line's last three blocks are the far side's ground, where he may talk again. Until this slice he never
            // rode them while the case watched: he stopped short of his friend, who stood at its end.)
            if(along>=0&&along<last){off[0]=Math.max(off[0],offTrail(at));if(!now.isEmpty()&&!now.equals(lastSaid[0])&&BikeNightGraveyard.hushed(b,his[0].position()))spoke[0]++;}
            if(!now.isEmpty()&&!now.equals(lastSaid[0]))words.add(now);
            lastSaid[0]=now;
            high[0]=Math.max(high[0],at.y);if(along>0)low[0]=Math.min(low[0],at.y);north[0]=at.z;rode[0]&=gus[0].getVehicle()==his[0];
            // Whatever is out there for somebody is out there for the reader: none of it is anybody else's.
            for(var a:l.getEntitiesOfClass(LiteraryActor.class,hollow(f),BikeNightSpecters::is)){seen[0]++;if(a.owner().isPresent()&&!a.owner().get().equals(p[0].getUUID()))specters[0]++;}
            if(++t[0]%40==0)BikeNightGus.idleNow(b);
        };
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(graves(f)).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                // The line: from the hill to the far side's ground, a fifth of a block at a time, in the air only over nothing.
                h.assertTrue(trail.get(0).at().z>CalderHollow.EDGE&&trail.get(last).at().z<CalderHollow.FAR_SIDE&&trail.get(0).at().y==level&&trail.get(last).at().y==level,"from the hill to the far side: "+trail.get(0)+" to "+trail.get(last));
                int air=0,hops=0;boolean was=false;
                for(int i=0;i<=last;i++){
                    var m=trail.get(i);if(i>0)h.assertTrue(m.at().distanceTo(trail.get(i-1).at())<.36&&m.at().z<=trail.get(i-1).at().z+1e-6,"its places are close, and none is back along it: "+i);
                    double top=CalderHollow.top((int)Math.floor(m.at().x),(int)Math.floor(m.at().z));boolean ground=m.at().z>=CalderHollow.EDGE||m.at().z<CalderHollow.FAR_SIDE+1;
                    if(m.air()){air++;if(!was)hops++;h.assertTrue(m.at().y>-.01,"its arcs are over the course, not under it");}
                    else h.assertTrue(ground?Math.abs(m.at().y-level)<.01:!Double.isNaN(top)&&m.at().y>=top-.51&&m.at().y<=top+.51,"on the ground it is on a piece, at its height or coming up to it: "+m+" over "+top);
                    was=m.air();
                }
                h.assertTrue(hops>=14&&air>60,"an arc over every gap: "+hops+" of them");
                p[0]=f.player("hollow_gus_reader",P);
            })
            .thenWaitUntil(()->{gus[0]=BikeNightGus.find(l,b);his[0]=BikeNightGus.bikeOf(l,b);
                h.assertTrue(BikeNight.begun(b)&&gus[0]!=null&&his[0]!=null&&p[0].getBbHeight()<1.1&&BikeNight.here(p[0]),"waiting for the night, Gus, his bike, and the reader to be the park's size");})
            .thenExecute(()->take(h,f,p[0],NightBike.Make.WASP))
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("wasp"),"waiting for the Wasp to be theirs"))
            .thenExecute(()->f.at(p[0],P,-14.5,0,-20.5))
            .thenWaitUntil(()->h.assertTrue(BikeNightBullies.stage(b)==BikeNightBullies.CHASE&&BikeNightGus.doing(b)==BikeNightGus.RIDING&&BikeNightGraveyard.gateOpen(b),"waiting for the chase, the gate, and for him to be on his bike: stage "+BikeNightBullies.stage(b)+", doing "+BikeNightGus.doing(b)))
            // The reader is out on the first grave; Gus is on the hill's top, on the gate's line, short of the small stones.
            .thenExecute(()->{var at=CalderHollow.ISLANDS.get(1).place();f.at(p[0],P,at.x,at.y,at.z);his[0].moveTo(b.getX()+.5,b.getY()+level,b.getZ()+CalderGraves.SMALL_ROW+6.5,180,0);})
            .thenWaitUntil(()->{watch.run();h.assertTrue(BikeNightHollow.gusTrail(b)>=0,"waiting for him to come to the edge after them and be on the line: at "+rel(b,his[0]));})
            .thenExecute(()->t[0]=0)
            .thenWaitUntil(()->{watch.run();h.assertTrue(t[0]>=60,"giving him three seconds to go on if he is going to");})
            .thenExecute(()->{
                // With them a grave out, he is at the hill's edge: the first grave's near end is nearer them than he keeps.
                h.assertTrue(north[0]>CalderHollow.EDGE&&north[0]<CalderHollow.EDGE+1.5&&his[0].speedNow()==0,"he waits at the hill's edge while they are on the first grave: at "+rel(b,his[0]));
                var at=CalderHollow.ISLANDS.get(3).place();f.at(p[0],P,at.x,at.y,at.z);high[0]=0;
            })
            // To the third grave: he is over two gaps and on the second grave's far end.
            .thenWaitUntil(()->{watch.run();h.assertTrue(his[0].speedNow()==0&&north[0]<-270,"waiting for him to have come on after them and stopped: at "+rel(b,his[0]));})
            .thenExecute(()->{
                var second=CalderHollow.WAY.get(1);
                h.assertTrue(Math.abs(north[0]-second.zNorth()-.3)<.3&&Math.abs(his[0].getY()-b.getY()-second.top())<.01,"on the second grave's far end, as near them as he keeps: at "+rel(b,his[0]));
                h.assertTrue(high[0]>second.top()+.4&&low[0]>=second.top()-.01,"over the two gaps on the way, and never under the graves: "+low[0]+" to "+high[0]);
                // To the far side, looking back over the Hollow. 0.4.509: the three are gone by now, Tully with the cap on.
                var at=CalderHollow.ISLANDS.get(CalderHollow.FAR_ISLAND).place();f.at(p[0],P,at.x,at.y,at.z);p[0].setYRot(0);p[0].setXRot(0);high[0]=0;low[0]=99;
                for(int who=0;who<3;who++)BikeNightGraveyard.takeNow(l,b,who,true);
            })
            // "the taken cap is hanging on the gate": it is on the post before he is off the Hollow's line.
            .thenWaitUntil(()->{watch.run();p[0].setYRot(0);h.assertTrue(CalderHollow.capHung(l,b),"waiting for the cap to be on the back gate's post");})
            // His friend goes on toward the gate, and he comes off the line after them: to the post first.
            .thenExecute(()->{
                h.assertTrue(BikeNightHollow.gusTrail(b)<last&&!BikeNightGus.capBack(b),"it is there before he is: he is "+BikeNightHollow.gusTrail(b)+" of "+last+" along");
                f.at(p[0],P,.5,level,CalderHollow.BACK+5.5);p[0].setYRot(0);
            })
            .thenWaitUntil(()->{
                watch.run();p[0].setYRot(0);
                h.assertTrue(BikeNightGus.capBack(b)&&words.containsAll(List.of(BikeNightTexts.GUS_CAP_BACK))&&north[0]<CalderHollow.FAR_SIDE+2&&his[0].speedNow()<.03&&his[0].position().distanceTo(p[0].position())<BikeNightGus.KEEP+2,
                    "waiting for him to ride the rest of it, take his cap down off the post, say whose it is, and stop by them: at "+rel(b,his[0])+", "+BikeNightHollow.gusTrail(b)+" of "+last+", cap back "+BikeNightGus.capBack(b)+", "+words);
            })
            .thenExecute(()->{
                // 0.4.509: off the post and on him; what he says of it, in order, over his head; and it is in his friend's record.
                h.assertTrue(!CalderHollow.capHung(l,b)&&gus[0].phase()==BikeNightGus.GUS,"the cap is off the post and on him: phase "+gus[0].phase());
                int first=words.indexOf(BikeNightTexts.GUS_CAP_BACK[0]),second=words.indexOf(BikeNightTexts.GUS_CAP_BACK[1]);
                h.assertTrue(first>=0&&second>first,"he says whose it is, and then what he is not asking: "+words);
                for(var line:BikeNightTexts.GUS_CAP_BACK)h.assertTrue(!heard(p[0],line),"what he says is over his head and never in chat: "+line);
                var own=f.own(p[0],P).copy();own.putBoolean("Through",true);
                h.assertTrue(f.own(p[0],P).getBoolean("CapBack")&&String.join(" ",BikeNightTexts.account("x",own).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList()).contains("Gus's cap was on a nail in the gatepost."),"and it is in the record, and the account, of who was there");
                h.assertTrue(rode[0]&&off[0]<.25,"he was on his bike and on the line all the way: "+off[0]+" off it at most");
                h.assertTrue(high[0]>5.1&&low[0]>-.01,"as high as over the top, and never under the lowest piece: "+low[0]+" to "+high[0]);
                h.assertTrue(spoke[0]==0,"he began nothing out there, with something to say every two seconds: "+spoke[0]);
                h.assertTrue(specters[0]==0&&seen[0]>0&&BikeNightHollow.fell(p[0])==0,"what the Hollow sent out while he rode it was his friend's, and none of it his: "+specters[0]+" of "+seen[0]);
                h.assertTrue(his[0].position().distanceTo(p[0].position())<BikeNightGus.KEEP+2,"he is by them on the far side: "+rel(b,his[0]));
                // Out by the back gate, onto the lane.
                h.assertTrue(BikeNightHollow.gateOpen(b),"the back gate has opened for them");
                f.at(p[0],P,.5,level,CalderHollow.BACK-2.5);
            })
            .thenWaitUntil(()->{watch.run();h.assertTrue(BikeNightHollow.through(p[0])&&north[0]<CalderHollow.FAR_SIDE-6&&his[0].position().distanceTo(p[0].position())<BikeNightGus.KEEP+1.5&&his[0].speedNow()<.03,"waiting for them to be through the back gate and for him to come up to it after them: at "+rel(b,his[0]));})
            .thenExecute(()->h.assertTrue(rode[0]&&BikeNightHollow.through(p[0])&&north[0]<CalderHollow.FAR_SIDE-6,"he is at the back gate when they are through it: at "+rel(b,his[0])))
            .thenSucceed();
    }
    @AfterBatch(batch="hollow_gus") public static void hollowGusDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.509, what follows
    @GameTest(template="empty",batch="after_gates",timeoutTicks=6000)
    public static void theThreesBikesLieOutsideTheFrontGateAndTheCapHangsOnTheBackGatesPostEachPutThereOnlyWhenNobodyCouldBeLooking(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null};final PackBike[] lies={null,null,null};final int[] t={0};
        int level=CalderGraves.level(CalderHollow.EDGE),rails=CalderTown.RAILS;var post=b.offset(CalderHollow.CAP_X,level+CalderHollow.CAP_UP,CalderHollow.CAP_Z);
        java.util.function.Supplier<List<PackBike>> lying=()->l.getEntitiesOfClass(PackBike.class,new AABB(b.getX()-14,b.getY()-1,b.getZ()+rails,b.getX()+14,b.getY()+3,b.getZ()+rails+8),e->e.isAlive()&&e.laid()!=0);
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(graves(f)).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{p[0]=f.player("after_front",P);p[1]=f.player("after_back",P);})
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&BikeNight.here(p[0])&&BikeNight.here(p[1]),"waiting for the night"))
            .thenExecute(()->{
                // Where they lie: pavement beside the gateway, outside the railings, off the lane, with nothing standing in it.
                for(int who=0;who<3;who++){
                    var d=BikeNightGraveyard.DROPS[who];int x=(int)Math.floor(d.x),z=(int)Math.floor(d.z);var cell=b.offset(x,0,z);
                    h.assertTrue(Math.abs(d.x)>CalderTown.GATE_E+2&&d.z>rails+1&&d.z<rails+4,"beside the gateway, outside the railings: "+d);
                    h.assertTrue(CalderTown.surface(x,z)!=CalderTown.ROAD&&l.getBlockState(cell).isAir()&&l.getBlockState(cell.below()).isCollisionShapeFullBlock(l,cell.below()),
                        "on open ground that is not the lane, for "+BikeNightBullies.NAMES[who]+"'s: surface "+CalderTown.surface(x,z)+", "+l.getBlockState(cell)+" over "+l.getBlockState(cell.below()));
                    // A bike on its side is as long as it stands and as wide as it stood high: a block every way from its middle is clear.
                    h.assertTrue(l.noCollision(new AABB(b.getX()+d.x-1,b.getY()+.05,b.getZ()+d.z-1,b.getX()+d.x+1,b.getY()+.7,b.getZ()+d.z+1)),"with room to lie in, for "+BikeNightBullies.NAMES[who]+"'s");
                }
                for(int i=0;i<3;i++)for(int j=i+1;j<3;j++)h.assertTrue(Math.abs(Mth.wrapDegrees(BikeNightGraveyard.DROP_YAW[i]-BikeNightGraveyard.DROP_YAW[j]))>25&&BikeNightGraveyard.DROPS[i].distanceTo(BikeNightGraveyard.DROPS[j])>2,"no two of them lie the same way or on top of each other: dropped, not stood in a row");
                h.assertTrue(lying.get().isEmpty()&&!CalderHollow.capHung(l,b)&&BikeNightGraveyard.dropped(b)==0,"nothing lies there and nothing hangs there while the three are about");
                // One reader stands on the lane looking at the front gate; the other on the far side, looking at the back gate.
                f.at(p[0],P,.5,0,rails+9.5);p[0].setYRot(180);p[0].setXRot(0);
                f.at(p[1],P,.5,level,CalderHollow.BACK+7.5);p[1].setYRot(180);p[1].setXRot(0);
                for(int who=0;who<3;who++)BikeNightGraveyard.takeNow(l,b,who,true);t[0]=0;
            })
            .thenWaitUntil(()->{
                p[0].setYRot(180);p[1].setYRot(180);
                h.assertTrue(lying.get().isEmpty()&&BikeNightGraveyard.dropped(b)==0&&!CalderHollow.capHung(l,b)&&!BikeNightGraveyard.capHung(b),"nothing is put where somebody could be looking: "+BikeNightGraveyard.dropped(b)+" bikes, the cap "+CalderHollow.capHung(l,b));
                h.assertTrue(++t[0]>=60,"waiting three seconds with both of them looking");
            })
            // They turn their backs.
            .thenWaitUntil(()->{p[0].setYRot(0);p[1].setYRot(0);h.assertTrue(BikeNightGraveyard.dropped(b)==3&&BikeNightGraveyard.capHung(b),"waiting for the bikes and the cap once nobody is looking: "+BikeNightGraveyard.dropped(b)+" bikes, the cap "+BikeNightGraveyard.capHung(b));})
            .thenExecute(()->{
                var all=lying.get();h.assertTrue(all.size()==3,"three bikes lie outside the front gate: "+all.size());
                for(var bike:all){
                    int who=bike.look();h.assertTrue(who>=0&&who<3&&lies[who]==null,"each is one of the three's, and there is one of each: "+who);lies[who]=bike;
                    var d=BikeNightGraveyard.DROPS[who];
                    h.assertTrue(Math.abs(bike.getX()-b.getX()-d.x)<.05&&Math.abs(bike.getZ()-b.getZ()-d.z)<.05&&Math.abs(bike.getY()-b.getY())<.05,"where it was dropped: "+rel(b,bike));
                    h.assertTrue(bike.laid()==BikeNightGraveyard.DROP_SIDE[who]&&Math.abs(Mth.wrapDegrees(bike.getYRot()-BikeNightGraveyard.DROP_YAW[who]))<.5&&bike.getPassengers().isEmpty()&&!bike.isPickable(),
                        BikeNightBullies.NAMES[who]+"'s is on its side, pointing as it fell, with nobody on it, and is nobody's to take");
                }
                // The last of them was the one at the fence: his wheels are still going round. The others' have stopped.
                h.assertTrue(lies[BikeNightGraveyard.AT_FENCE].speedNow()>0&&lies[0].speedNow()==0&&lies[1].speedNow()==0,"the last one's wheels are still going round: "+lies[2].speedNow());
                for(int i=0;i<3;i++)h.assertTrue(BikeNightBullies.find(l,b,i)==null&&BikeNightBullies.bikeOf(l,b,i)==null,"and "+BikeNightBullies.NAMES[i]+" is nowhere");
                // The cap: against the town's side of the back gate's east post, at a child's eye, facing whoever comes up out of the Hollow.
                var s=l.getBlockState(post);
                h.assertTrue(CalderBlock.is(s,CalderBlock.Kind.HAT)&&s.getValue(CalderBlock.FACING)==Direction.SOUTH&&l.getBlockState(post.north()).is(BikeNightRegistry.material("brick"))&&CalderBlock.is(l.getBlockState(post.north().above()),CalderBlock.Kind.CAP),"the cap hangs on the gatepost, under its coping: "+s+" on "+l.getBlockState(post.north()));
                h.assertTrue(post.getX()-b.getX()==CalderGraves.PATH_E+1&&post.getZ()-b.getZ()==CalderHollow.BACK+1&&s.getCollisionShape(l,post).isEmpty(),"beside the gateway, and in nobody's way");
                // A hand on it.
                hush(p[1]);f.click(p[1],P,post.subtract(b));
                // The reader at the front walks up to the bikes.
                f.at(p[0],P,-2.5,0,rails+4.5);t[0]=0;
            })
            .thenWaitUntil(()->h.assertTrue(heard(p[1],BikeNightTexts.HUNG_CAP),"a hand on the cap says whose it is"))
            .thenWaitUntil(()->{t[0]++;h.assertTrue(lies[2].speedNow()==0,"waiting for the wheels to run down with somebody by: "+lies[2].speedNow());})
            .thenExecute(()->{
                h.assertTrue(t[0]>=100&&t[0]<=200,"they went on a while with somebody there to see them, and stopped: "+t[0]+" ticks");
                h.assertTrue(f.own(p[0],P).getInt("BikesSeen")==3&&f.own(p[1],P).getInt("BikesSeen")==0,"whoever came on them has it in their record, and nobody else");
                var own=f.own(p[0],P).copy();own.putBoolean("Through",true);
                var told=String.join(" ",BikeNightTexts.account("x",own).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(told.contains("Their three bikes were lying on the pavement outside the cemetery's front gate. Nobody was with them."),"and their account has it: "+told);
                own.putInt("BikesSeen",1);
                h.assertTrue(String.join(" ",BikeNightTexts.account("x",own).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList()).contains("One of their bikes was lying on the pavement outside the cemetery's front gate. Nobody was with it."),"or one, if one was all that lay there");
                // The night over: nothing lies there and nothing hangs there.
                BikeNight.resetNow(l,b);
                h.assertTrue(lying.get().isEmpty()&&!CalderHollow.capHung(l,b),"the night over, the bikes are gone from the gate and the cap from the post");
            })
            // Another night, in which Tully never had the cap on.
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&BikeNightGraveyard.dropped(b)==0,"waiting for another night"))
            .thenExecute(()->{
                f.at(p[0],P,.5,0,rails+9.5);f.at(p[1],P,.5,level,CalderHollow.BACK+7.5);p[0].setYRot(0);p[1].setYRot(0);
                // A cap left from a night that ended with its post out of reach.
                h.assertTrue(CalderHollow.hungCap(l,b,true),"a cap is left on the post");
                for(int who=0;who<3;who++)BikeNightGraveyard.takeNow(l,b,who,false);t[0]=0;
            })
            .thenWaitUntil(()->{p[0].setYRot(0);p[1].setYRot(0);h.assertTrue(BikeNightGraveyard.dropped(b)==3&&++t[0]>=60,"waiting three seconds, and for the bikes");})
            .thenExecute(()->h.assertTrue(!BikeNightGraveyard.capHung(b)&&!CalderHollow.capHung(l,b),"with no cap on him there is none to hang, and one left from another night has come down"))
            .thenSucceed();
    }
    @AfterBatch(batch="after_gates") public static void afterGatesDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.508, out of it
    /** How many copies of the night's account somebody is carrying. */
    private static int accounts(ServerPlayer p){
        int n=0;String title=LiteraryTexts.accountTitle(P);
        for(int i=0;i<p.getInventory().getContainerSize();i++){var book=p.getInventory().getItem(i).get(DataComponents.WRITTEN_BOOK_CONTENT);if(book!=null&&book.title().raw().equals(title))n++;}
        return n;
    }

    @GameTest(template="empty",batch="ending_out",timeoutTicks=6000)
    public static void whoeverIsThroughTheBackGateAndLeavesHasFinishedItOnceWithTheirAccountAndWhoeverWasCaughtIsNeverDealtItAgain(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null,null};final NightBike[] mine={null,null,null};final int[] t={0};
        int top=CalderGraves.level(CalderHollow.EDGE),back=CalderHollow.BACK;var story=WitnessAccount.Story.BIKE_NIGHT;
        h.startSequence().thenExecute(()->f.chunks.hold(l,whole(f).minmax(hollow(f))))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the place is resident"))
            .thenExecute(()->{
                // The wall along the lane's far verge, capped, standing back from the lane, and round the lane's two ends.
                var brick=BikeNightRegistry.material("brick");
                h.assertTrue(CalderHollow.WALL<CalderHollow.LANE_N-2&&back-CalderHollow.WALL>=8,"the wall stands back from the lane, eight blocks from the gate");
                for(int x=-CalderHollow.SIDE-1;x<=CalderHollow.SIDE+1;x++){
                    for(int dy=0;dy<CalderHollow.WALL_HIGH;dy++)if(x!=CalderHollow.DOOR_X||dy>=2)h.assertTrue(l.getBlockState(b.offset(x,top+dy,CalderHollow.WALL)).is(brick),"the wall is whole at "+x+", "+dy+" up");
                    h.assertTrue(CalderBlock.is(l.getBlockState(b.offset(x,top+CalderHollow.WALL_HIGH,CalderHollow.WALL)),CalderBlock.Kind.CAP),"and capped at "+x);
                }
                for(int x:new int[]{-CalderHollow.SIDE-1,CalderHollow.SIDE+1})for(int z=CalderHollow.FAR;z<back;z++)h.assertTrue(l.getBlockState(b.offset(x,top,z)).is(brick)&&l.getBlockState(b.offset(x,top+2,z)).is(brick),"the lane's end is walled at "+x+","+z);
                // The door in it, east of the gate's line, with the House knowing of it.
                // A fixture builds the place without its way on (the builder fits that, or the pass that waits until nobody is looking): the case fits it itself, so it never waits on who else is about.
                var at=b.offset(CalderHollow.DOOR_X,top,CalderHollow.WALL);h.assertTrue(f.data().door(OnwardDoors.id(P))!=null||OnwardDoors.install(l,b,P),"the fitted door goes in where it was placed: "+l.getBlockState(at)+" before "+l.getBlockState(at.relative(net.minecraft.core.Direction.SOUTH)));var door=f.data().door(OnwardDoors.id(P));
                h.assertTrue(CalderHollow.DOOR_X>CalderGraves.PATH_E+2,"the door is off the gate's line");
                h.assertTrue(l.getBlockState(at).is(OnwardRegistry.PANEL.get())&&l.getBlockState(at.above()).is(OnwardRegistry.PANEL.get()),"a door stands in the wall: "+l.getBlockState(at));
                h.assertTrue(door!=null&&door.lower.equals(at),"and it is the night's way on: "+(door==null?null:door.lower));
                h.assertTrue(story.kind.equals("survival")&&WitnessAccount.Story.of(P.id())==story,"the night is a source of the survival kind");
                p[0]=f.player("ending_rider",P);p[1]=f.player("ending_taken",P);p[2]=f.player("ending_early",P);
            })
            .thenWaitUntil(()->h.assertTrue(BikeNight.begun(b)&&p[0].getBbHeight()<1.1&&p[1].getBbHeight()<1.1&&BikeNight.here(p[2]),"waiting for the night"))
            .thenExecute(()->{
                for(var who:p)h.assertTrue(BikeNight.dealable(f.data(),who.getUUID())&&!WitnessAccount.has(f.data(),who.getUUID(),story),"nobody has finished it, and it may be dealt to any of them");
                mine[0]=take(h,f,p[0],NightBike.Make.LAMP);
            })
            .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getString("Bike").equals("lamp"),"waiting for the Lamp to be theirs"))
            // Along Main Street for a second, and down Mill Street for another: the streets they took are kept, in order.
            .thenExecute(()->{mine[0].moveTo(b.getX()+12.5,b.getY(),b.getZ()-91.5,90,0);p[0].setYRot(90);})
            .thenWaitUntil(()->{if(mine[0].onGround())mine[0].steady(0,.1F);h.assertTrue(f.own(p[0],P).getString("Streets").equals("main"),"waiting for Main Street to be one they took: "+f.own(p[0],P).getString("Streets"));})
            .thenExecute(()->{mine[0].moveTo(b.getX()+.5,b.getY(),b.getZ()-70.5,0,0);p[0].setYRot(0);})
            .thenWaitUntil(()->{if(mine[0].onGround())mine[0].steady(0,.1F);h.assertTrue(f.own(p[0],P).getString("Streets").equals("main,mill"),"waiting for Mill Street to be the next: "+f.own(p[0],P).getString("Streets"));})
            // Somebody who leaves before the back gate has not finished it, and may come back.
            .thenExecute(()->{
                mine[0].steady(0,0);
                BikeNight.leave(p[2],"park");var own=f.own(p[2],P);
                h.assertTrue(!own.getBoolean("Completed")&&accounts(p[2])==0&&!WitnessAccount.has(f.data(),p[2].getUUID(),story)&&BikeNight.dealable(f.data(),p[2].getUUID()),"leaving before the back gate finishes nothing");
                // Somebody the Hollow has had: there is nothing for them to finish, and it is never dealt to them again.
                var taken=f.own(p[1],P);taken.putBoolean("Taken",true);taken.putString("TakenBy","witch");LiteraryVignettes.save(f.data(),p[1].getUUID(),P,taken);
                BikeNight.leave(p[1],"park");
                h.assertTrue(!f.own(p[1],P).getBoolean("Completed")&&accounts(p[1])==0&&!WitnessAccount.has(f.data(),p[1].getUUID(),story)&&!BikeNight.dealable(f.data(),p[1].getUUID()),"somebody who was caught in the Hollow has no account, and is never dealt the night again");
                // And somebody the three caught in the town.
                var out=f.own(p[2],P);out.putBoolean("Out",true);LiteraryVignettes.save(f.data(),p[2].getUUID(),P,out);
                h.assertTrue(!BikeNight.dealable(f.data(),p[2].getUUID()),"nor is somebody who was caught in the town");
                // The Lamp's rider to the far side, on the gate's line.
                var at=islandPlace(b,CalderHollow.FAR_ISLAND);mine[0].moveTo(at.x,at.y,at.z,180,0);p[0].setYRot(180);
            })
            .thenWaitUntil(()->{
                double x=mine[0].getX()-b.getX();
                p[0].setYRot((float)Math.toDegrees(Math.atan2(-(0.0-x),-3.5)));if(mine[0].onGround())pedalWell(mine[0],t[0]++,3);
                h.assertTrue(BikeNightHollow.through(p[0]),"waiting for them to ride through the back gate: at "+where(b,mine[0]));
            })
            .thenExecute(()->{
                mine[0].steady(0,0);var own=f.own(p[0],P);
                h.assertTrue(own.getBoolean("Through")&&!own.getBoolean("ThroughOnPegs")&&!own.getBoolean("Completed")&&accounts(p[0])==0,"through the gate, and not finished until they leave: "+own);
                // Through the door in the wall.
                BikeNight.onward(p[0],f.data().door(OnwardDoors.id(P)));
            })
            .thenExecute(()->{
                var own=f.own(p[0],P);String told=told("ending_rider",p[0],f);
                h.assertTrue(own.getBoolean("Completed")&&own.getString("Outcome").equals("rode_out_by_the_back_gate")&&own.getString("Left").equals("lane"),"finished, by the lane's door: "+own.getString("Outcome"));
                h.assertTrue(WitnessAccount.has(f.data(),p[0].getUUID(),story)&&!BikeNight.dealable(f.data(),p[0].getUUID()),"it is theirs with the Witness, and is not dealt to them again");
                h.assertTrue(accounts(p[0])==1,"their account goes out with them: "+accounts(p[0]));
                h.assertTrue(told.contains("I took the Lamp.")&&told.contains("I went by Main Street and Mill Street.")&&told.contains("I kept to what was straight ahead of me.")
                    &&told.contains("I rode out by the back gate onto a lane, and down the hill the lights of the town were on again.")&&told.contains("There was a door in the wall across the lane."),"and tells of the bike, the streets, the way through and the way out: "+told);
                h.assertTrue(!told.contains("pegs")&&!told.contains("went down into the mist"),"and of nothing that did not happen: "+told);
                // Again: nothing more.
                BikeNight.onward(p[0],f.data().door(OnwardDoors.id(P)));BikeNight.leave(p[0],"park");
                h.assertTrue(accounts(p[0])==1&&f.own(p[0],P).getString("Left").equals("lane"),"once");
                // What the account says of the other ways, of going down, and of the pegs.
                var other=f.own(p[0],P).copy();other.putString("HollowWays","dark,high");other.putInt("HollowFell",2);other.putBoolean("ThroughOnPegs",true);other.putBoolean("Carried",true);other.putString("Left","park");
                String told2=String.join(" ",BikeNightTexts.account("x",other).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(told2.contains("I went through the bank of mist, by the lamp and over the high graves, where only a bike that jumps will go.")&&told2.contains("I went down into the mist two times.")
                    &&told2.contains("I came out by the back gate on the pegs of somebody else's bike, onto a lane")&&told2.contains("For some of it I was on the pegs of somebody else's bike.")&&told2.contains("I went back the way I had come."),"the other ways, going down, the pegs, and back by the park: "+told2);
            })
            .thenSucceed();
    }
    @AfterBatch(batch="ending_out") public static void endingOutDone(ServerLevel l){done();}
}
