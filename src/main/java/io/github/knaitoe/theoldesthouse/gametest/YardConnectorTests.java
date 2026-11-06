package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class YardConnectorTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="yard_ditch") public static void ditchDone(ServerLevel l){close();}
    @AfterBatch(batch="yard_sheets") public static void sheetsDone(ServerLevel l){close();}
    private static void close(){YardConnectors.clearAll();if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h,LabyrinthPlace place){
        active=new LiteraryTests.Fixture(h);var bounds=LabyrinthPlaces.slotBounds(active.origin,place);
        active.chunks.hold(active.out,new AABB(bounds.minX(),bounds.minY(),bounds.minZ(),bounds.maxX()+1,bounds.maxY()+1,bounds.maxZ()+1));return active;
    }
    private static ServerPlayer reader(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);p.setNoGravity(true);return p;}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double z){p.teleportTo(l,b.getX()+x,b.getY(),b.getZ()+z,180,0);p.hasChangedDimension();}
    private static void use(ServerPlayer p,ServerLevel l,BlockPos at){l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));}
    private static void build(LiteraryTests.Fixture f,LabyrinthPlace place,BlockPos b){YardConnectors.build(f.out,b,place);LabyrinthBuilder.registerDoors(f.data(),place,b);}

    @GameTest(template="empty",batch="yard_ditch",timeoutTicks=1800)
    public static void theDryCulvertIsCrawledBothWaysAndRestoresTheReadersActualPose(GameTestHelper h){
        var place=LabyrinthPlace.YARD_DITCH;var f=fixture(h,place);var l=f.out;var b=f.base(place);var p=reader(h,f,"ditch_crawler");int[] direction={0};int witnesses=WitnessAccount.count(f.data(),p.getUUID());
        h.onEachTick(()->{if(direction[0]!=0)p.move(MoverType.SELF,new Vec3(0,0,direction[0]*.12));});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the ditch and pipe are resident"))
            .thenExecute(()->{
                build(f,place,b);stand(p,l,b,.5,-11.5);
                h.assertTrue(NovelRooms.dimension(place).equals(HouseDimensions.OUTSIDE)&&place.isGray(),"the ditch is an outdoor passage, with no story credit");
                h.assertTrue(l.getBlockState(b.offset(0,0,-17)).isAir()&&l.getBlockState(b.offset(0,1,-17)).is(Blocks.STONE),"the road leaves a real one-block pipe below it");
                h.assertTrue(!l.noCollision(p,new AABB(b.getX()+.2,b.getY()+.01,b.getZ()-17.8,b.getX()+.8,b.getY()+1.8,b.getZ()-17.2)),"a standing body cannot fit the pipe");
                use(p,l,b.offset(YardConnectors.LIP));
                h.assertTrue(YardCrawl.crawling(p)&&p.getForcedPose()==Pose.SWIMMING&&p.getBbHeight()<.7,"a hand on the lip puts the real body into a crawl");direction[0]=-1;
            })
            .thenWaitUntil(()->h.assertTrue(p.getZ()<b.getZ()-17,"the actual body moves through the first half of the pipe"))
            .thenExecute(()->{direction[0]=0;YardCrawl.clear(p);})
            .thenWaitUntil(()->h.assertTrue(YardCrawl.crawling(p)&&p.getBbHeight()<.7,"a lost pose lease is reacquired inside the one-block pipe"))
            .thenExecute(()->direction[0]=-1)
            .thenWaitUntil(()->h.assertTrue(p.getZ()<b.getZ()-24.5,"native movement clears the far lip"))
            .thenExecute(()->direction[0]=0)
            .thenWaitUntil(()->h.assertTrue(!YardCrawl.crawling(p)&&p.getForcedPose()==null&&p.getBbHeight()>1.7,"on dry open ground the original standing pose returns"))
            .thenExecute(()->{
                stand(p,l,b,.5,-23.5);use(p,l,b.offset(YardConnectors.FAR_LIP));h.assertTrue(YardCrawl.crawling(p),"the far lip also admits a return crawl");direction[0]=1;
            })
            .thenWaitUntil(()->h.assertTrue(p.getZ()>b.getZ()-10.5,"the real body crawls back under the road"))
            .thenExecute(()->direction[0]=0)
            .thenWaitUntil(()->h.assertTrue(!YardCrawl.crawling(p)&&p.getForcedPose()==null,"leaving at the original mouth restores the pose too"))
            .thenExecute(()->{
                h.assertTrue(p.getAirSupply()>200&&p.getHealth()==p.getMaxHealth(),"the raised dry pipe does not become a breath or damage challenge");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==witnesses,"the ordinary crossing grants no Witness credit");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="yard_sheets",timeoutTicks=1800)
    public static void sheetsYieldForThePartyAndFollowingStepsEndBeyondTheLastOne(GameTestHelper h){
        var place=LabyrinthPlace.YARD_CLOTHESLINE;var f=fixture(h,place);var l=f.out;var b=f.base(place);var p=reader(h,f,"sheet_walker");var q=reader(h,f,"sheet_friend");boolean[] walking={false};int[] heard={0};
        h.onEachTick(()->{
            if(walking[0])p.move(MoverType.SELF,new Vec3(0,0,-.10));
            for(var s:NativeTestPlayers.sounds(p))if(s.getSound().value()==PuzzleRegistry.YARD_FOLLOW.get())heard[0]++;
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the complete washing line is resident"))
            .thenExecute(()->{
                build(f,place,b);stand(p,l,b,.5,-7.5);stand(q,l,b,-.5,-8.5);
                use(p,l,b.offset(0,1,YardConnectors.SHEETS[0]));
                h.assertTrue(YardConnectors.folded(l,b,-9)&&l.getBlockState(b.offset(0,1,-9)).isAir(),"a hand gathers the actual sheet to its post");walking[0]=true;
            })
            .thenWaitUntil(()->h.assertTrue(p.getZ()<b.getZ()-17,"the body walks through the second curtain"))
            .thenExecute(()->{
                h.assertTrue(YardConnectors.folded(l,b,-9),"the first sheet stays gathered while the other reader is still beside it");
                h.assertTrue(heard[0]>0,"following footsteps are delivered at a position behind the moving reader");
            })
            .thenWaitUntil(()->h.assertTrue(p.getZ()<b.getZ()-24,"the reader has pushed past the final sheet"))
            .thenExecute(()->{
                walking[0]=false;l.setBlock(b.offset(0,1,-23),Blocks.DIAMOND_BLOCK.defaultBlockState(),18);
                stand(q,l,b,.5,-29);stand(p,l,b,.5,-29);
            })
            .thenWaitUntil(()->h.assertTrue(!YardConnectors.folded(l,b,-9)&&!YardConnectors.folded(l,b,-16)&&!YardConnectors.folded(l,b,-23),"the linen falls back only after everyone has cleared it"))
            .thenExecute(()->{
                h.assertTrue(l.getBlockState(b.offset(0,1,-23)).is(Blocks.DIAMOND_BLOCK),"resetting cloth preserves a replacement block");
                heard[0]=0;walking[0]=true;
            })
            .thenIdle(45).thenExecute(()->{walking[0]=false;h.assertTrue(heard[0]==0,"the following footsteps stop beyond the final sheet");}).thenSucceed();
    }
}
