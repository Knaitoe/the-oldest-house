package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BikeBeamBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.BikeNightRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.BikeRampBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.NightBike;
import io.github.knaitoe.theoldesthouse.labyrinth.NightBikeBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.NightBikes;
import io.github.knaitoe.theoldesthouse.labyrinth.RiddenBike;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.463, Bike night's first slice: the six bikes. Each case rides on a road of its own in the test world's sky, where no other case
 * builds. Riders are real players on the server; the keys a client would send are given to the bike directly (pedal, keys), as
 * Borrasca's bike cases do.
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
}
