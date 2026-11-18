package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseSleep;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.703 (owner: "One player sleeping in the house triggers day phase. That player should just be synced in with the rest of the player
 * sleeping count, if possible."): a sleeper in the House counts beside the Overworld's players; the House never passes the night alone.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class HouseSleepTests {
    private HouseSleepTests(){}

    @GameTest(template="empty",batch="house_sleep")
    public static void aSleeperInTheHouseCountsBesideTheOverworld(GameTestHelper h){
        var server=h.getLevel().getServer();var house=server.getLevel(HouseDimensions.INTERIOR);
        h.assertTrue(house!=null,"the House is there");
        h.assertTrue(HouseSleep.shared(Level.OVERWORLD)&&HouseSleep.shared(HouseDimensions.INTERIOR)&&!HouseSleep.shared(HouseDimensions.OUTSIDE)
                &&!HouseSleep.shared(HouseDimensions.BETWEEN)&&!HouseSleep.shared(Level.NETHER),"the Overworld and the House share one count; the other places keep their own");
        h.assertTrue(HouseSleep.needed(2,100)==2&&HouseSleep.needed(3,50)==2&&HouseSleep.needed(0,100)==1&&HouseSleep.needed(4,0)==1,"as many must sleep as the game asks of one level");
        ServerPlayer awake=h.makeMockServerPlayerInLevel(),sleeper=h.makeMockServerPlayerInLevel(),watcher=h.makeMockServerPlayerInLevel();
        awake.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);sleeper.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);watcher.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
        BlockPos origin=HouseSavedData.get(server).houseOrigin();BlockPos at=origin!=null?origin.above(2):new BlockPos(0,120,0);
        sleeper.teleportTo(house,at.getX()+.5,at.getY(),at.getZ()+.5,0,0);
        sleeper.startSleeping(sleeper.blockPosition());
        h.assertTrue(sleeper.level()==house&&sleeper.isSleeping(),"a reader lies down in the House");
        var everyone=HouseSleep.players(server);
        h.assertTrue(everyone.contains(awake)&&everyone.contains(sleeper),"both are counted together");
        var own=new SleepStatus();own.update(List.of(sleeper));
        h.assertTrue(own.areEnoughSleeping(100),"counted by the House alone, as the game counts a level, one sleeper would have passed the night");
        h.assertTrue(!HouseSleep.enoughSleeping(house,own,100)&&!HouseSleep.enoughDeepSleeping(house,own,100,List.of(sleeper)),"the House never passes the night by itself");
        h.assertTrue(!HouseSleep.enoughSleeping(h.getLevel(),new SleepStatus(),100),"nor does the Overworld while a reader there is awake");
        var c=HouseSleep.count(List.of(awake,sleeper,watcher));
        h.assertTrue(c.active()==2&&c.sleeping()==1,"two count, the spectator not, and one of them is asleep: "+c);
        h.assertTrue(!HouseSleep.enough(c,100)&&HouseSleep.enough(c,50),"one of two asleep is half: enough at half, not at all");
        awake.startSleeping(awake.blockPosition());
        c=HouseSleep.count(List.of(awake,sleeper,watcher));
        h.assertTrue(c.sleeping()==2&&HouseSleep.enough(c,100)&&!HouseSleep.deepEnough(c,100),"both asleep is enough to begin; the night passes once both have slept long enough");
        sleeper.stopSleeping();awake.stopSleeping();
        h.succeed();
    }
}
