package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
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
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.463, Bike night's first slice: the six bikes. Each case rides on a road of its own in the test world's sky, where no other case
 * builds. Riders are real players on the server; the keys a client would send are given to the bike directly (pedal, keys), as
 * Borrasca's bike cases do.
 *
 * <p>0.4.464, the park: built whole of its own things; a reader comes in at child height at midnight; whichever bike a reader gets
 * on is theirs for the night; the failing lamp; the night over when the park stands empty; what a hand on the park's things says.
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
    private static final Map<UUID,List<String>> HEARD=new HashMap<>();
    private static LiteraryTests.Fixture park(GameTestHelper h){done();active=new LiteraryTests.Fixture(h);return active;}
    private static void done(){if(active!=null){active.close();active=null;}BikeNight.clearAll();NightBikes.forget();HEARD.clear();}
    private static AABB room(LiteraryTests.Fixture f){var b=f.base(P);var r=P.room();return new AABB(b.getX()+r.minX(),b.getY()+r.minY(),b.getZ()+r.minZ(),b.getX()+r.maxX()+1,b.getY()+r.maxY()+1,b.getZ()+r.maxZ()+1);}
    /** Everything a reader has been told so far has this in it. What is said reaches a reader when the tick ends, so cases wait on it. */
    private static boolean heard(ServerPlayer p,String part){var all=HEARD.computeIfAbsent(p.getUUID(),k->new ArrayList<>());all.addAll(NativeTestPlayers.messages(p));return all.stream().anyMatch(m->m.contains(part));}
    private static void hush(ServerPlayer p){NativeTestPlayers.messages(p);HEARD.remove(p.getUUID());}
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
            new Thing(new BlockPos(6,1,0),CalderSigns.Sign.RULES.look()),new Thing(new BlockPos(0,3,BikeNightSite.NORTH),CalderSigns.Sign.ARCH.look()));
        var seq=h.startSequence().thenExecute(()->f.chunks.hold(l,room(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the park is resident"))
            .thenExecute(()->p[0]=f.player("park_hand",P));
        for(var thing:things)seq=seq.thenExecute(()->{hush(p[0]);f.click(p[0],P,thing.at());})
            .thenWaitUntil(()->h.assertTrue(heard(p[0],thing.says()),"a hand at "+thing.at().toShortString()+" says: "+thing.says()));
        seq.thenExecute(()->{
            for(var line:List.of(BikeNightTexts.GATE,BikeNightTexts.RAILINGS,BikeNightTexts.LAMP,BikeNightTexts.LAMP_FAILING,BikeNightTexts.BENCH,BikeNightTexts.BIN,BikeNightTexts.RACK,BikeNightTexts.BORDER,BikeNightTexts.ARRIVE,BikeNightTexts.YOURS,BikeNightTexts.SWAPPED,CalderSigns.Sign.RULES.look(),CalderSigns.Sign.ARCH.look())){
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
}
