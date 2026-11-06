package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.TallyCounterItem;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;

/** The outdoor count is a sphere, including the vertical distance and the exact boundary. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_goatman") @PrefixGameTestTemplate(false)
public final class CounterRangeTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="counter_radius") public static void done(ServerLevel ignored){if(active!=null){active.close();active=null;}}
    @GameTest(template="empty",batch="counter_radius",timeoutTicks=400)
    public static void anOutdoorClickCountsTheRadiusNotTheCornersOfItsSearchBox(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var l=h.getLevel().getServer().overworld();
        var at=h.absolutePos(BlockPos.ZERO).offset(90,50,90);
        f.chunks.hold(l,new AABB(at).inflate(40));
        var p=NativeTestPlayers.survival(h,"counter_radius");f.players.add(p);
        p.teleportTo(l,at.getX()+.5,at.getY(),at.getZ()+.5,0,0);p.setNoGravity(true);
        var mobs=new ArrayList<Zombie>();var messages=new ArrayList<String>();var packetKinds=new HashSet<String>();
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the whole count radius is loaded"))
            .thenExecute(()->{
                for(var offset:List.of(new BlockPos(30,0,30),new BlockPos(32,0,0),new BlockPos(0,32,0),new BlockPos(0,33,0),new BlockPos(31,0,0))){
                    var z=EntityType.ZOMBIE.create(l);z.setNoAi(true);z.setNoGravity(true);
                    z.moveTo(p.getX()+offset.getX(),p.getY()+offset.getY(),p.getZ()+offset.getZ());
                    l.addFreshEntity(z);f.extras.add(z);mobs.add(z);
                }
                var dead=EntityType.ZOMBIE.create(l);dead.moveTo(p.getX()+2,p.getY(),p.getZ());dead.setHealth(0);l.addFreshEntity(dead);f.extras.add(dead);
                var animal=EntityType.PIG.create(l);animal.moveTo(p.getX()+3,p.getY(),p.getZ());l.addFreshEntity(animal);f.extras.add(animal);
                var counted=TallyCounterItem.nearbyHostiles(p,TallyCounterItem.OUT_THERE);
                h.assertTrue(counted.size()==3&&counted.containsAll(List.of(mobs.get(1),mobs.get(2),mobs.get(4)))&&!counted.contains(mobs.get(0)),"inside and exact horizontal/vertical boundaries count; the diagonal corner, dead mob and animal do not");
                p.setItemInHand(InteractionHand.MAIN_HAND,TallyCounterItem.forReader(p.getUUID()));
                NativeTestPlayers.packets(p);
                TallyCounterItem.click(p,p.getMainHandItem());
            }).thenWaitUntil(()->{
                for(var packet:NativeTestPlayers.packets(p)){
                    packetKinds.add(packet.getClass().getSimpleName());
                    if(packet instanceof ClientboundSetActionBarTextPacket bar)messages.add(bar.text().getString());
                    if(packet instanceof ClientboundSystemChatPacket chat&&chat.overlay())messages.add(chat.content().getString());
                }
                h.assertTrue(messages.contains("Click. Three out there."),"the actual counter reports the radial count through the native action bar: "+messages+" packets="+packetKinds);
            }).thenExecute(()->{
                // The sleeping watch uses the same distance rule at its smaller radius.
                for(var z:mobs)z.moveTo(p.getX()+30,p.getY(),p.getZ()+30);
                mobs.get(0).moveTo(p.getX()+8,p.getY(),p.getZ()+8);
                h.assertTrue(TallyCounterItem.nearbyHostiles(p,TallyCounterItem.WATCH_RANGE).isEmpty(),"a diagonal beyond ten cannot wake the keeper");
                mobs.get(0).moveTo(p.getX()+10,p.getY(),p.getZ());
                h.assertTrue(TallyCounterItem.nearbyHostiles(p,TallyCounterItem.WATCH_RANGE).size()==1,"the watch includes exactly ten blocks");
            }).thenSucceed();
    }
}
