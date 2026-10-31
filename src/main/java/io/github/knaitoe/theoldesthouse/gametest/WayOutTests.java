package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.journal.JournalPage;
import io.github.knaitoe.theoldesthouse.journal.Journals;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID) @PrefixGameTestTemplate(false)
public final class WayOutTests {
    private static LiteraryTests.Fixture way;
    @AfterBatch(batch="way_out") public static void done(ServerLevel l){if(way!=null){way.close();way=null;}}
    static CompoundTag own(ServerPlayer p){return WayOut.own(p.server,p.getUUID());}
    static void set(ServerPlayer p,String key,int value){var t=own(p);t.putInt(key,value);LabyrinthData.get(p.server).setStateEntry(WayOut.STATE,p.getUUID().toString(),t);}
    static boolean lined(ServerPlayer p){var data=Journals.get(p.server);for(int i=1;i<=Journals.PAGES;i++)for(var n:data.page(p.getUUID(),i).notes())if(n.mark()==JournalPage.Mark.WRITTEN&&n.text().equals(WayOut.LINE))return true;return false;}

    /** 0.4.199: the way out looks like the reader's own world, turns wrong, and ends with the line, in the journal too, back in the hall. */
    @GameTest(template="empty",batch="way_out",timeoutTicks=1200)
    public static void theWayOutLooksLikeOutsideTurnsWrongAndEndsWithTheLineBackInTheHall(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);way=f;var place=LabyrinthPlace.WAY_OUT;var b=f.base(place);var l=f.out;var server=l.getServer();
        var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(l,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));
        var p=NativeTestPlayers.survival(h,"way_out_reader");var q=NativeTestPlayers.survival(h,"way_out_homecomer");f.players.add(p);f.players.add(q);
        var hall=new Vec3(f.origin.getX()+.5,f.origin.getY(),f.origin.getZ()+.5);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the meadow is resident"))
            .thenExecute(()->{
                WayOutRooms.site(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);h.assertTrue(OnwardDoors.install(l,b,place),"the cottage's front door is the way on");
                h.assertTrue(place.isVignette()&&WitnessAccount.Story.of(place.id())==null&&NovelRooms.dimension(place).equals(HouseDimensions.OUTSIDE)&&!ScenePolish.applies(place)&&!VignetteArchitecture.applies(place),"a vignette outside, built whole, and no Witness story");
                h.assertTrue(l.getBlockState(b.offset(0,0,1)).getBlock() instanceof DoorBlock&&l.getBlockState(b.offset(WayOut.COTTAGE_DOOR)).getBlock() instanceof DoorBlock,"the door in from the hall, and the cottage door on the far side");
                h.assertTrue(l.getBlockState(b.offset(0,-1,-6)).is(Blocks.GRASS_BLOCK)||l.getBlockState(b.offset(0,-1,-6)).is(Blocks.DIRT_PATH),"daylight and grass beyond the shed");
                h.assertTrue(l.getEntitiesOfClass(Animal.class,WayOut.bounds(b)).size()>=12,"cows, sheep, pigs and chickens about the meadow");
                h.assertTrue(SceneClock.at(f.origin,b.offset(0,0,-20),HouseDimensions.OUTSIDE)==12&&SceneClock.time(12,0,0)==SceneClock.time(12,9999,123456),"a fine morning, and the sun never moves");
                // Dealt only to a reader who has been in the House long enough to want out.
                h.assertTrue(!WayOut.canDeal(f.data(),p.getUUID()),"not to a newcomer");
                int n=0;for(var story:WitnessAccount.Story.values()){if(n>=WayOut.RESOLUTIONS)break;if(WitnessAccount.resolve(f.data(),p.getUUID(),story,"resolved"))n++;}
                for(var story:WitnessAccount.Story.values()){if(WitnessAccount.count(f.data(),q.getUUID())>=WayOut.RESOLUTIONS)break;WitnessAccount.resolve(f.data(),q.getUUID(),story,"resolved");}
                h.assertTrue(WayOut.canDeal(f.data(),p.getUUID())&&LabyrinthDealer.vignettesAvailable(f.data(),p.getUUID()).contains(place),"dealt once they have resolved enough");
                p.getInventory().add(Journals.get(server).issue(p));p.getInventory().add(new ItemStack(Items.BREAD,5));
                f.data().pushReturn(p.getUUID(),new LabyrinthData.Waypoint(f.in.dimension(),hall,0F,false));
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-6.5,180,0);p.hasChangedDimension();p.setOnGround(true);
            }).thenWaitUntil(()->h.assertTrue(own(p).getInt("Ticks")>3,"the reader's time there counts"))
            .thenExecute(()->{
                h.assertTrue(own(p).getInt("NextBlip")==0&&WayOut.echoing(p.getUUID())==0,"at first nothing is wrong");
                set(p,"Ticks",WayOut.CALM);
            }).thenIdle(12)
            .thenExecute(()->{
                var animals=l.getEntitiesOfClass(Animal.class,WayOut.bounds(b));
                h.assertTrue(animals.stream().allMatch(Animal::isNoAi),"after a while the animals stand still");
                var eye=p.getEyePosition();var look=p.getViewVector(1F);int facing=0,unseen=0;
                for(var a:animals){if(WayOut.seen(p,a,eye,look))continue;unseen++;var to=eye.subtract(a.getEyePosition());float yaw=(float)(Mth.atan2(to.z,to.x)*Mth.RAD_TO_DEG)-90F;if(Math.abs(Mth.wrapDegrees(yaw-a.getYHeadRot()))<2F)facing++;}
                h.assertTrue(unseen>0&&facing==unseen,"every one the reader is not looking at has turned to face them: "+facing+"/"+unseen);
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-9.0,180,0);p.setOnGround(true);
            }).thenWaitUntil(()->{p.setOnGround(true);h.assertTrue(WayOut.echoing(p.getUUID())==1,"a stride's step is coming back late");})
            .thenWaitUntil(()->h.assertTrue(WayOut.echoing(p.getUUID())==0,"and has come"))
            .thenExecute(()->{
                set(p,"Ticks",WayOut.STATIC_FROM);set(p,"NextBlip",0);
            }).thenIdle(3)
            .thenExecute(()->{
                h.assertTrue(own(p).getInt("NextBlip")>WayOut.STATIC_FROM,"the static has begun, and the next blip is set");
                // A death there is no death: the end begins, and nothing is lost.
                p.kill();
                h.assertTrue(p.isAlive()&&p.getHealth()==p.getMaxHealth()&&p.getInventory().countItem(Items.BREAD)==5&&own(p).getInt("Ending")>=1,"dying is a way out, healed and holding everything");
            }).thenWaitUntil(()->h.assertTrue(own(p).getBoolean("Seen")&&own(p).getInt("Ending")==0&&p.level().dimension().equals(HouseDimensions.INTERIOR),"after the line, the reader wakes in the hall they came from"))
            .thenExecute(()->{
                h.assertTrue(lined(p),"the line is in the reader's journal, in the other hand");
                h.assertTrue(!WayOut.canDeal(f.data(),p.getUUID())&&!LabyrinthDealer.vignettesAvailable(f.data(),p.getUUID()).contains(place),"never dealt to them again");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==WayOut.RESOLUTIONS,"and it counts toward nothing");
                // Reaching the cottage door, the way home, ends it the same way.
                q.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+WayOut.COTTAGE_DOOR.getZ()+2.5,180,0);q.hasChangedDimension();
                f.data().pushReturn(q.getUUID(),new LabyrinthData.Waypoint(f.in.dimension(),hall,0F,false));
            }).thenWaitUntil(()->h.assertTrue(own(q).getInt("Ending")>=1||own(q).getBoolean("Seen"),"at the cottage door the end begins"))
            .thenWaitUntil(()->h.assertTrue(own(q).getBoolean("Seen")&&q.level().dimension().equals(HouseDimensions.INTERIOR),"and that reader too wakes in the hall"))
            .thenExecute(()->{
                h.assertTrue(!lined(q),"no journal, no line: nothing is written for a reader who carries none");
                h.assertTrue(l.getEntitiesOfClass(Animal.class,WayOut.bounds(b)).stream().noneMatch(Animal::isNoAi),"with nobody left, the animals graze again");
                try{ArchitectureTests.export(l,b,place);}catch(Exception e){throw new IllegalStateException(e);}
            }).thenSucceed();
    }
}
