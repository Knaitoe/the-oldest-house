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
        boolean ready(){boolean r=chunks.ready();if(!r)LockSupport.parkNanos(10_000_000L);return r;}
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
    private static LiteraryTests.Fixture park(GameTestHelper h){done();active=new LiteraryTests.Fixture(h);return active;}
    private static void done(){if(active!=null){active.close();active=null;}BikeNight.clearAll();NightBikes.forget();HEARD.clear();SOUNDED.clear();}
    private static AABB room(LiteraryTests.Fixture f){var b=f.base(P);var r=P.room();return new AABB(b.getX()+r.minX(),b.getY()+r.minY(),b.getZ()+r.minZ(),b.getX()+r.maxX()+1,b.getY()+r.maxY()+1,b.getZ()+r.maxZ()+1);}
    /** Everything a reader has been told so far has this in it. What is said reaches a reader when the tick ends, so cases wait on it. */
    /** What a reader was told and what they heard come down one pipe, and reading it empties it: both are kept as they are read. */
    private static void drain(ServerPlayer p){
        var said=HEARD.computeIfAbsent(p.getUUID(),k->new ArrayList<>());var rang=SOUNDED.computeIfAbsent(p.getUUID(),k->new ArrayList<>());
        for(var packet:NativeTestPlayers.packets(p)){
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)said.add(chat.content().getString());
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound)rang.add(sound.getSound().value().getLocation().getPath());
        }
    }
    private static boolean heard(ServerPlayer p,String part){drain(p);return HEARD.get(p.getUUID()).stream().anyMatch(m->m.contains(part));}
    private static boolean sounded(ServerPlayer p,String name){drain(p);return SOUNDED.get(p.getUUID()).contains(name);}
    private static void hush(ServerPlayer p){drain(p);HEARD.remove(p.getUUID());SOUNDED.remove(p.getUUID());}
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
                h.assertTrue(WitnessAccount.Story.of(P.id())==null&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED&&!LiteraryVignettes.canDeal(f.data(),UUID.randomUUID(),P),
                    "not a source yet, the quota as it was, and not dealt until the night can be finished");
                // The back wall and the shed's door in it; the paper and the rules beside it.
                h.assertTrue(l.getBlockState(b.offset(0,0,1)).getBlock() instanceof DoorBlock&&l.getBlockState(b.offset(0,1,1)).getBlock() instanceof DoorBlock,"the tool shed's door, in the back wall");
                var brick=BikeNightRegistry.material("brick");var kerb=BikeNightRegistry.material("kerb");
                for(int x:new int[]{-40,-20,-5,5,20,40})h.assertTrue(l.getBlockState(b.offset(x,2,1)).is(brick),"the back wall is brick at x="+x);
                h.assertTrue(l.getBlockState(b.offset(-30,6,1)).is(kerb)&&l.getBlockState(b.offset(0,7,1)).is(brick)&&l.getBlockState(b.offset(0,8,1)).is(kerb),"a stone coping on the wall, and on the shed's front, which stands two courses over it");
                h.assertTrue(CalderBlock.is(l.getBlockState(b.offset(BikeNightSite.SOURCE)),CalderBlock.Kind.NOTE)&&l.getBlockState(b.offset(BikeNightSite.SOURCE).south()).is(brick),"the sheet from a school pad, taped to the brick by the door");
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=String.join(" ",text.pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(text.author().equals("Gus")&&all.contains("HALLOWEEN PLAN")&&all.contains("Come through the tool shed")&&all.contains("Do NOT cut through the graveyard"),"the plan for the night, in Gus's hand: "+all);
                for(var sign:CalderSigns.Sign.values()){
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
                int leaves=0,litter=0;var strangers=new java.util.TreeSet<String>();var r=P.room();
                for(var at:BlockPos.betweenClosed(b.offset(r.minX(),r.minY(),r.minZ()),b.offset(r.maxX(),r.maxY(),r.maxZ()))){
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
                BikeNightTexts.CARVED,BikeNightTexts.FOUNTAIN,BikeNightTexts.FOUNTAIN_RUNNING,BikeNightTexts.PHONE,BikeNightTexts.PHONE_HELD)){
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
        Runnable with=()->{double going=Math.cos((seat[0].swingTicks(0)+1)*HouseReadSwing.RATE);p[0].setPlayerInput(0,going>0?1F:-1F,false,false);high[0]=Math.max(high[0],seat[0].reach());high[1]=Math.max(high[1],seat[0].getY()-b.getY());};
        Runnable against=()->{double going=Math.cos((seat[0].swingTicks(0)+1)*HouseReadSwing.RATE);p[0].setPlayerInput(0,going>0?-1F:1F,false,false);};
        // The fixture's readers are weightless, so that they stay where a case puts them. One in the air off a swing has to fall.
        Runnable fly=()->{if(BikeNight.inFlight(p[0])){p[0].setNoGravity(false);p[0].travel(Vec3.ZERO);}else p[0].setNoGravity(true);};
        h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("swing_high",P))
            .thenWaitUntil(()->h.assertTrue(BikeNight.bikeAt(b,NightBike.Make.WASP)!=null,"the night has begun"))
            .thenExecute(()->{f.click(p[0],P,BikeNightSite.swingAt(2));seat[0]=(SeatEntity)p[0].getVehicle();h.assertTrue(seat[0]!=null&&seat[0].pumped(),"on a swing");})
            // Leaning the way it goes, each way, takes it as high as it will go; and no higher.
            .thenExecuteFor(240,with)
            .thenExecute(()->{
                h.assertTrue(Math.abs(seat[0].reach()-HouseReadSwing.HIGH)<.02&&high[0]<=HouseReadSwing.HIGH+1e-9&&HouseReadSwing.HIGH>HouseReadSwing.REACH*2,"leaning with it takes the arc to its widest, well past a gentle one: "+seat[0].reach());
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
    private static double going(SeatEntity seat){return Math.cos(seat.swingTicks(0)*HouseReadSwing.RATE);}

    @GameTest(template="empty",batch="park_push",timeoutTicks=3000)
    public static void aReaderOnASwingIsPushedByAnotherOnTheSandWhoTimesIt(GameTestHelper h){
        var f=park(h);var b=f.base(P);var l=f.level(P);f.build(P);final ServerPlayer[] p={null,null,null};final SeatEntity[] seat={null,null};
        double bar=BikeNightSite.SWING_X+.5,line=BikeNightSite.SWING_SEATS[0]+.5;final double[] was={0,0};final int[] sent={0};
        double meet=Math.sin(HouseReadSwing.MEET*HouseReadSwing.RATE);
        // The one behind pushes each time the swing starts away from them.
        Runnable timed=()->{
            double before=seat[0].reach();
            if(seat[0].swingAngle(0)<0&&going(seat[0])>0&&push(p[1],p[0])&&seat[0].reach()>before+.05)sent[0]++;
            was[1]=Math.max(was[1],seat[0].reach());
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
            // Pushed each time it starts away, it goes as high as a swing goes, and no higher.
            .thenExecute(()->{sent[0]=0;was[1]=0;})
            .thenExecuteFor(520,timed)
            .thenExecute(()->{
                h.assertTrue(sent[0]>=5&&was[1]>HouseReadSwing.HIGH-1e-9&&was[1]<=HouseReadSwing.HIGH+1e-9&&seat[0].reach()>HouseReadSwing.HIGH-.12,
                    "pushed as it starts away each time, the swing goes as high as it goes, and no higher: "+sent[0]+" pushes, "+was[1]+", now "+seat[0].reach());
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
}
