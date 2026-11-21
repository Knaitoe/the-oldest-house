package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;
import org.jetbrains.annotations.Nullable;

/**
 * The multiplayer sweep for the stories built for each reader where they are (the backseat's road, the silo, Drisking). Three
 * readers ask at once and each is built their own site, one after another. The one being built logs out part way: the others are
 * built all the same, and on the same id the first comes back to the same site, finished (the silo and the road wait for their reader; Drisking goes on being built without them). Every site is
 * its own. A spectator at the door is not let in and is given no site. (The family copy is built from a reader's own home in the
 * Overworld, and its own cases have readers leave and come back.)
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_group") @PrefixGameTestTemplate(false)
public final class OwnPlaceSweepTests {
    private OwnPlaceSweepTests(){}
    private static final int TIMEOUT=24000;
    private static LiteraryTests.Fixture active;
    private static void close(){SiloSites.clear();BackseatSites.clear();BorrascaSites.clear();if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="own_silo") public static void siloDone(ServerLevel l){close();}
    @AfterBatch(batch="own_backseat") public static void backseatDone(ServerLevel l){close();}
    @AfterBatch(batch="own_borrasca") public static void borrascaDone(ServerLevel l){close();}

    /** One story's own sites: asking, whether a reader's is built, where it is, and (where it has one) its source door. */
    private record Site(String name,Function<ServerPlayer,Object> ask,BiFunction<MinecraftServer,UUID,Boolean> built,BiFunction<MinecraftServer,UUID,BlockPos> base,
                        @Nullable String source,@Nullable BiFunction<ServerPlayer,LabyrinthData.Door,Boolean> enter){}

    @GameTest(template="empty",batch="own_silo",timeoutTicks=TIMEOUT)
    public static void silo(GameTestHelper h){
        sweep(h,new Site("silo",SiloSites::prepareEntry,(s,id)->SiloSites.state(s,id).getBoolean("Built"),SiloSites::base,SiloSites.SOURCE,SiloSites::enter));
    }
    @GameTest(template="empty",batch="own_backseat",timeoutTicks=TIMEOUT)
    public static void backseat(GameTestHelper h){
        sweep(h,new Site("backseat",BackseatSites::prepareEntry,(s,id)->BackseatSites.state(s,id).getBoolean("Built"),BackseatSites::base,BackseatSites.SOURCE,BackseatSites::enter));
    }
    /** Drisking is entered through a dealt door in the halls, whose rules keep spectators out for every story. */
    @GameTest(template="empty",batch="own_borrasca",timeoutTicks=TIMEOUT)
    public static void borrasca(GameTestHelper h){
        sweep(h,new Site("borrasca",BorrascaSites::prepareEntry,BorrascaSites::ready,BorrascaSites::base,null,null));
    }

    private static void sweep(GameTestHelper h,Site site){
        close();var f=new LiteraryTests.Fixture(h);active=f;var s=h.getLevel().getServer();
        var readers=new ServerPlayer[3];var ids=new UUID[3];var names=new String[3];var gone=new boolean[1];var bases=new BlockPos[3];
        for(int i=0;i<3;i++){readers[i]=NativeTestPlayers.survival(h,"own_"+site.name()+i);f.players.add(readers[i]);ids[i]=readers[i].getUUID();names[i]=readers[i].getGameProfile().getName();}
        // Every living reader goes on asking, as one standing at the door does.
        h.onEachTick(()->{for(int i=0;i<3;i++)if(!(i==0&&gone[0])&&!site.built().apply(s,ids[i]))site.ask().apply(readers[i]);});
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(site.base().apply(s,ids[0])!=null&&site.base().apply(s,ids[1])!=null&&site.base().apply(s,ids[2])!=null,site.name()+": each reader is given a site"))
            .thenIdle(4)
            .thenExecute(()->{
                for(int i=0;i<3;i++)bases[i]=site.base().apply(s,ids[i]);
                h.assertTrue(new HashSet<>(Arrays.asList(bases)).size()==3,site.name()+": every site is its own: "+Arrays.toString(bases));
                h.assertTrue(!site.built().apply(s,ids[0]),site.name()+": the first is still being built when they go");
                gone[0]=true;f.players.remove(readers[0]);NativeTestPlayers.remove(readers[0]);})
            .thenWaitUntil(()->h.assertTrue(site.built().apply(s,ids[1])&&site.built().apply(s,ids[2]),site.name()+": the others are built with the first gone: "+site.built().apply(s,ids[1])+"/"+site.built().apply(s,ids[2])))
            .thenExecute(()->{
                readers[0]=NativeTestPlayers.survival(h,names[0],ids[0]);f.players.add(readers[0]);gone[0]=false;})
            .thenWaitUntil(()->h.assertTrue(site.built().apply(s,ids[0]),site.name()+": back on the same id, their site is finished"))
            .thenExecute(()->{
                h.assertTrue(bases[0].equals(site.base().apply(s,ids[0])),site.name()+": and it is the same site: "+bases[0]+" / "+site.base().apply(s,ids[0]));
                for(int i=1;i<3;i++)h.assertTrue(bases[i].equals(site.base().apply(s,ids[i])),site.name()+": the others' sites are where they were");
                if(site.source()==null)return;
                var from=f.origin.offset(0,0,-6);var door=new LabyrinthData.Door("native/own/"+site.name(),f.in.dimension(),from,Direction.SOUTH,site.source(),false);f.data().putDoor(door);
                var spectator=NativeTestPlayers.survival(h,"own_"+site.name()+"_eye");f.players.add(spectator);spectator.setGameMode(GameType.SPECTATOR);
                spectator.teleportTo(f.in,from.getX()+.5,from.getY(),from.getZ()+2,180,0);
                h.assertTrue(!site.enter().apply(spectator,door),site.name()+": a spectator at the door is not let in");
                h.assertTrue(site.base().apply(s,spectator.getUUID())==null,site.name()+": and is given no site");})
            .thenSucceed();
    }
}
