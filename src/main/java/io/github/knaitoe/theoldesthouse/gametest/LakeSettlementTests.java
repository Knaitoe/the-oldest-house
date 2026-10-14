package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

/** The periodic lakeside repair cannot generate an undealt or already finished scene. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_playtest488") @PrefixGameTestTemplate(false)
public final class LakeSettlementTests {
    @GameTest(template="empty",batch="lake_scenery_loading0493",timeoutTicks=100)
    public static void undealtAndCheckpointedLakeRepairsDoNotLoadNativeChunks(GameTestHelper h){
        var level=HouseTestLevel.get(h.getLevel().getServer(),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE);
        var data=LabyrinthData.get(level.getServer());var old=data.state(LakeSettlement.STATE).copy();
        var place=LabyrinthPlace.SHALLOWS;var base=new BlockPos(712000,80,-712000);
        var entry=base.offset(0,-1,-3);var key=base.asLong()+":"+place.id();
        try{
            h.assertTrue(!level.hasChunkAt(entry),"the undealt lake begins without a native chunk");
            for(int i=0;i<20;i++)LakeSettlement.decorateOnce(level,base,place);
            h.assertTrue(!level.hasChunkAt(entry)&&!data.state(LakeSettlement.STATE).getBoolean(key),
                    "periodic scenery repair neither generates an absent lake nor completes its checkpoint");
            var done=data.state(LakeSettlement.STATE);done.putBoolean(key,true);data.setState(LakeSettlement.STATE,done);
            for(int i=0;i<20;i++)LakeSettlement.decorateOnce(level,base,place);
            h.assertTrue(!level.hasChunkAt(entry)&&data.state(LakeSettlement.STATE).getBoolean(key),
                    "a completed saved lake is not probed or reloaded to repeat its scenery");
            h.succeed();
        }finally{data.setState(LakeSettlement.STATE,old);}
    }
}
