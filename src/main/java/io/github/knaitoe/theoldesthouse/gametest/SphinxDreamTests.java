package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.342, the Sphinx's dream (docs/SPHINX_DREAM_0_4_342.md), dreamed by three readers at once and refused to two others. A patient
 * reader who answered three riddles right, with their House closed by an ending, finds the survey in a door frame, sets it down beside
 * the bed of a real room, sleeps, sees her face, wakes in a likeness of that room in the morning, pushes the sand back from the door three
 * times, goes out and looks up, and she lifts away; back in their bed, the stone waits inside their door and only they can take it, deep,
 * patient and carved with the shelf. A hostile reader, locked out, finds the survey in a pocket; their door bangs open at once and she
 * launches with it; their stone is short, smooth and chipped. A dismissive reader's door sticks while she lifts off, settles when they
 * pause, then opens; she is waiting over the far field until they look. A reader who never met her cannot set a survey down; one who met
 * her with the House still open sets theirs down and sleeps, and dreams nothing. No reader is sent another's body of her.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class SphinxDreamTests {
    private SphinxDreamTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="sphinx_dream") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}SphinxDream.clearAll();}

    /** A floor, and a bed on it facing north; the bed's head. */
    private static BlockPos bed(ServerLevel l,BlockPos at){
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)l.setBlockAndUpdate(at.offset(x,-1,z),Blocks.STONE.defaultBlockState());
        var bed=Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH);
        l.setBlockAndUpdate(at.offset(0,0,1),bed.setValue(BedBlock.PART,BedPart.FOOT));l.setBlockAndUpdate(at,bed.setValue(BedBlock.PART,BedPart.HEAD));
        return at;
    }
    /** A small walled bedroom round a bed, with a door in its south wall: the room the dream copies. */
    private static BlockPos room(ServerLevel l,BlockPos o){
        for(int x=-1;x<=5;x++)for(int z=-1;z<=5;z++)for(int y=0;y<=3;y++){
            boolean wall=x==-1||x==5||z==-1||z==5;var at=o.offset(x,y,z);
            l.setBlockAndUpdate(at,y==0||y==3?Blocks.SPRUCE_PLANKS.defaultBlockState():wall?Blocks.WHITE_TERRACOTTA.defaultBlockState():Blocks.AIR.defaultBlockState());
        }
        var door=Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH);
        l.setBlockAndUpdate(o.offset(2,1,5),door.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));l.setBlockAndUpdate(o.offset(2,2,5),door.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        var bed=Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH);
        l.setBlockAndUpdate(o.offset(1,1,1),bed.setValue(BedBlock.PART,BedPart.FOOT));l.setBlockAndUpdate(o.offset(1,1,0),bed.setValue(BedBlock.PART,BedPart.HEAD));
        return o.offset(1,1,0);
    }
    private static void use(ServerLevel l,ServerPlayer p,BlockPos at){p.gameMode.useItemOn(p,l,p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.UP,at,false));}
    private static void holdSurvey(ServerPlayer p){
        var inv=p.getInventory();for(int i=0;i<inv.items.size();i++)if(SphinxField.isSurvey(inv.items.get(i))){var s=inv.items.get(i);inv.items.set(i,ItemStack.EMPTY);inv.items.set(inv.selected,s);return;}
    }
    private static void stand(ServerPlayer p,ServerLevel l,double x,double y,double z){p.teleportTo(l,x,y,z,180,0);p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;}
    private static int stage(GameTestHelper h,ServerPlayer p){return SphinxDream.stage(h.getLevel().getServer(),p.getUUID());}
    private static ItemEntity stone(ServerLevel ow,ServerPlayer p){
        var r=SphinxDream.record(ow.getServer(),p.getUUID());var sill=BlockPos.of(r.getLong("Sill"));
        for(var e:ow.getEntitiesOfClass(ItemEntity.class,new AABB(sill).inflate(1.5)))if(DreamStela.is(e.getItem())&&p.getUUID().equals(DreamStela.ownerOf(e.getItem())))return e;
        return null;
    }
    private static long stones(ServerLevel ow,ServerPlayer p){
        return ow.getEntitiesOfClass(ItemEntity.class,new AABB(p.blockPosition()).inflate(64)).stream().filter(e->DreamStela.is(e.getItem())&&p.getUUID().equals(DreamStela.ownerOf(e.getItem()))).count();
    }
    private static SphinxDreamEntity her(ServerPlayer p){
        for(var e:p.serverLevel().getEntitiesOfClass(SphinxDreamEntity.class,new AABB(p.blockPosition()).inflate(300)))if(p.getUUID().equals(e.owner()))return e;
        return null;
    }
    private static boolean doorOpen(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}

    @GameTest(template="empty",batch="sphinx_dream",timeoutTicks=5000)
    public static void theDreamComesToThoseWhoMetHerOnceTheHouseIsClosed(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var ow=h.getLevel();var s=ow.getServer();SphinxDream.clearAll();
        var a=NativeTestPlayers.survival(h,"dream_patient");f.players.add(a);
        var b=NativeTestPlayers.survival(h,"dream_hostile");f.players.add(b);
        var e=NativeTestPlayers.survival(h,"dream_dismissive");f.players.add(e);
        var c=NativeTestPlayers.survival(h,"dream_stranger");f.players.add(c);
        var d=NativeTestPlayers.survival(h,"dream_open");f.players.add(d);
        var o=h.absolutePos(new BlockPos(2,1,2));
        BlockPos[] beds=new BlockPos[5];
        h.startSequence()
            .thenExecute(()->{
                // Their meetings with her.
                SphinxCore.answer(ow,a.getUUID(),"early",true);SphinxCore.answer(ow,a.getUUID(),"stair",true);SphinxCore.answer(ow,a.getUUID(),"shelf",true);
                SphinxCore.answer(ow,b.getUUID(),"early",false);SphinxCore.forced(ow,b.getUUID(),"stair");SphinxCore.walked(ow,b.getUUID(),"shelf");
                SphinxCore.walked(ow,e.getUUID(),"early");SphinxCore.walked(ow,e.getUUID(),"stair");SphinxCore.answer(ow,e.getUUID(),"shelf",true);
                SphinxCore.answer(ow,d.getUUID(),"early",true);SphinxCore.answer(ow,d.getUUID(),"stair",true);SphinxCore.answer(ow,d.getUUID(),"field",true);
                h.assertTrue(SphinxCore.conduct(ow,a.getUUID())==SphinxCore.Conduct.PATIENT&&SphinxCore.conduct(ow,b.getUUID())==SphinxCore.Conduct.HOSTILE&&SphinxCore.conduct(ow,e.getUUID())==SphinxCore.Conduct.DISMISSIVE,"three manners");
                h.assertTrue(SphinxDream.met(s,a.getUUID())&&SphinxDream.met(s,b.getUUID())&&SphinxDream.met(s,e.getUUID())&&SphinxDream.met(s,d.getUUID())&&!SphinxDream.met(s,c.getUUID()),"four met her; one never did");
                // The House closed to three of them, one way or another; open to the fourth.
                FinaleProgress.phase(s,a.getUUID(),FinaleProgress.Phase.WITNESSED);FinaleProgress.phase(s,b.getUUID(),FinaleProgress.Phase.LOCKED_OUT);FinaleProgress.phase(s,e.getUUID(),FinaleProgress.Phase.ESCAPED);
                h.assertTrue(SphinxDream.closed(s,a.getUUID())&&SphinxDream.closed(s,b.getUUID())&&SphinxDream.closed(s,e.getUUID())&&!SphinxDream.closed(s,d.getUUID()),"closed to three");
                beds[0]=room(ow,o);beds[1]=bed(ow,o.offset(48,1,0));beds[2]=bed(ow,o.offset(96,1,0));beds[3]=bed(ow,o.offset(144,1,0));beds[4]=bed(ow,o.offset(192,1,0));
                // The survey: in the frame of the next ordinary door, once, and never for one who did not meet her.
                SphinxDream.arrived(a,LabyrinthPlace.GRAY_CORRIDOR);SphinxDream.arrived(a,LabyrinthPlace.GRAY_CORRIDOR);SphinxDream.arrived(c,LabyrinthPlace.GRAY_CORRIDOR);
                SphinxDream.arrived(e,LabyrinthPlace.LONG_HALLWAY);SphinxDream.arrived(d,LabyrinthPlace.GRAY_CORRIDOR);
                h.assertTrue(SphinxField.carries(a)&&SphinxField.carries(e)&&SphinxField.carries(d)&&!SphinxField.carries(c),"a survey in the door frame for each who met her");
                int n=0;for(var it:a.getInventory().items)if(SphinxField.isSurvey(it))n+=it.getCount();h.assertTrue(n==1,"and only one");
                stand(b,ow,beds[1].getX()+1.5,beds[1].getY(),beds[1].getZ()+.5);})
            // The locked-out reader who never came through another door finds one in a pocket.
            .thenWaitUntil(()->h.assertTrue(SphinxField.carries(b),"a survey in the pocket of a reader the House closed to"))
            .thenExecute(()->{
                // Setting it down beside a bed.
                stand(a,ow,o.getX()+2.5,o.getY()+1,o.getZ()+2.5);holdSurvey(a);var held=a.getMainHandItem().copy();use(ow,a,beds[0]);
                h.assertTrue(stage(h,a)==SphinxDream.INVITED&&!SphinxField.carries(a),"the patient reader's survey set down beside their bed (stage "+stage(h,a)+", held "+held+")");
                h.assertTrue(!ow.getEntitiesOfClass(ItemFrame.class,new AABB(beds[0]).inflate(2)).isEmpty(),"lying there for anyone to see");
                h.assertTrue(SphinxDream.record(s,a.getUUID()).contains("Room"),"their bedroom copied");
                holdSurvey(b);use(ow,b,beds[1]);holdSurvey(e);stand(e,ow,beds[2].getX()+1.5,beds[2].getY(),beds[2].getZ()+.5);use(ow,e,beds[2]);
                h.assertTrue(stage(h,b)==SphinxDream.INVITED&&stage(h,e)==SphinxDream.INVITED,"the others' too");
                c.setItemInHand(InteractionHand.MAIN_HAND,SphinxField.survey(c,false));use(ow,c,beds[3]);
                h.assertTrue(stage(h,c)==SphinxDream.NONE&&SphinxField.carries(c),"a reader who never met her cannot set one down");
                holdSurvey(d);stand(d,ow,beds[4].getX()+1.5,beds[4].getY(),beds[4].getZ()+.5);use(ow,d,beds[4]);
                h.assertTrue(stage(h,d)==SphinxDream.INVITED,"a reader whose House is open can");
                h.assertTrue(SphinxDream.record(s,a.getUUID()).getInt("Index")!=SphinxDream.record(s,b.getUUID()).getInt("Index"),"a dream site each");})
            .thenWaitUntil(()->h.assertTrue(SphinxDream.record(s,a.getUUID()).getBoolean("Built")&&SphinxDream.record(s,b.getUUID()).getBoolean("Built")&&SphinxDream.record(s,e.getUUID()).getBoolean("Built"),"their dream sites built"))
            .thenExecute(()->{a.startSleeping(beds[0]);b.startSleeping(beds[1]);e.startSleeping(beds[2]);d.startSleeping(beds[4]);})
            .thenWaitUntil(()->h.assertTrue(stage(h,a)>=SphinxDream.FACE&&stage(h,b)>=SphinxDream.FACE&&stage(h,e)>=SphinxDream.FACE,"asleep at their beds with the House closed, they dream"))
            .thenExecute(()->{
                h.assertTrue(a.level().dimension().equals(HouseDimensions.OUTSIDE)&&SphinxDream.inSite(a),"in the dream's bedroom");
                var ra=SphinxDream.record(s,a.getUUID());
                h.assertTrue(ra.getInt("Reach")==DreamStela.DEEP&&"patient".equals(ra.getString("Opening"))&&"shelf".equals(ra.getString("Carving"))&&!ra.getBoolean("Chipped"),"the patient record fixed as the dream begins: deep, patient, the shelf");
                var rb=SphinxDream.record(s,b.getUUID());
                h.assertTrue(rb.getInt("Reach")==DreamStela.SHORT&&"hostile".equals(rb.getString("Opening"))&&"smooth".equals(rb.getString("Carving"))&&rb.getBoolean("Chipped"),"the hostile record: short, smooth, chipped");
                var re=SphinxDream.record(s,e.getUUID());
                h.assertTrue(re.getInt("Reach")==DreamStela.MEDIUM&&"dismissive".equals(re.getString("Opening")),"the dismissive record: medium");
                h.assertTrue(stage(h,d)==SphinxDream.INVITED&&d.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD),"the reader whose House is open sleeps and dreams nothing");
                h.assertTrue(ow.getEntitiesOfClass(ItemFrame.class,new AABB(beds[0]).inflate(2)).isEmpty(),"the survey taken up into the dream");})
            .thenWaitUntil(()->h.assertTrue(stage(h,a)==SphinxDream.FIRST&&stage(h,b)==SphinxDream.FIRST&&stage(h,e)==SphinxDream.FIRST,"the first waking"))
            .thenExecute(()->{
                var site=SphinxDream.site(s,SphinxDream.record(s,a.getUUID()).getInt("Index"));
                h.assertTrue(SceneClock.at(f.origin,site.offset(0,1,6),HouseDimensions.OUTSIDE)==11,"morning");
                var her=her(a);h.assertTrue(her!=null&&her.phase()==SphinxDreamEntity.PERCHED,"her whole body crouched on the roof");
                h.assertTrue(her.broadcastToPlayer(a)&&!her.broadcastToPlayer(b)&&!her.broadcastToPlayer(e),"sent to the dreamer alone");
                h.assertTrue(!a.isSleeping()&&SphinxDream.inSite(a),"awake in the dream's bedroom");
                h.assertTrue(!doorOpen(a.serverLevel(),site),"the front door shut, sand against it");
                use(a.serverLevel(),a,site);})
            .thenIdle(5).thenExecute(()->{var site=SphinxDream.site(s,SphinxDream.record(s,a.getUUID()).getInt("Index"));h.assertTrue(!doorOpen(a.serverLevel(),site),"patient: the sand gives a little");use(a.serverLevel(),a,site);})
            .thenIdle(5).thenExecute(()->{var site=SphinxDream.site(s,SphinxDream.record(s,a.getUUID()).getInt("Index"));use(a.serverLevel(),a,site);h.assertTrue(doorOpen(a.serverLevel(),site),"and on the third push the door swings");
                h.assertTrue(her(a).phase()==SphinxDreamEntity.PERCHED,"she waits");
                // Hostile: the door comes free at once and she goes with it.
                var sb=SphinxDream.site(s,SphinxDream.record(s,b.getUUID()).getInt("Index"));use(b.serverLevel(),b,sb);
                h.assertTrue(doorOpen(b.serverLevel(),sb)&&her(b).phase()==SphinxDreamEntity.FLYING,"hostile: the door bangs open and she launches");
                // Dismissive: it sticks, and she lifts off while it does.
                var se=SphinxDream.site(s,SphinxDream.record(s,e.getUUID()).getInt("Index"));use(e.serverLevel(),e,se);
                h.assertTrue(!doorOpen(e.serverLevel(),se)&&her(e).phase()==SphinxDreamEntity.FLYING,"dismissive: the door sticks, and she is already going");
                // The patient reader goes out and looks up at her.
                stand(a,a.serverLevel(),site.getX()+.5,site.getY(),site.getZ()+7.5);var at=SphinxDream.regard(her(a));a.lookAt(EntityAnchorArgument.Anchor.EYES,at);})
            .thenIdle(40).thenExecute(()->{var se=SphinxDream.site(s,SphinxDream.record(s,e.getUUID()).getInt("Index"));use(e.serverLevel(),e,se);h.assertTrue(doorOpen(e.serverLevel(),se),"after a pause the sand settles, and the next push opens it");
                stand(e,e.serverLevel(),se.getX()+.5,se.getY(),se.getZ()+7.5);})
            .thenWaitUntil(()->h.assertTrue(her(a)==null||her(a).phase()==SphinxDreamEntity.FLYING,"looked at, she lifts off"))
            .thenWaitUntil(()->{var her=her(e);if(her!=null)e.lookAt(EntityAnchorArgument.Anchor.EYES,SphinxDream.regard(her));h.assertTrue(stage(h,e)>=SphinxDream.SECOND,"the dismissive reader looks up at her over the far field; she goes; the second waking");})
            .thenWaitUntil(()->h.assertTrue(stage(h,a)>=SphinxDream.SECOND&&stage(h,b)>=SphinxDream.SECOND,"the others wake again too"))
            // The stone is laid once the sill's entities have loaded, which can be a few ticks after the reader is back in their bed.
            .thenWaitUntil(()->{var sill=BlockPos.of(SphinxDream.record(s,b.getUUID()).getLong("Sill"));
                h.assertTrue(stone(ow,a)!=null&&stone(ow,b)!=null,"the stones on their sills (hostile sill entities loaded: "+ow.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(sill))+")");})
            .thenIdle(20)
            .thenExecute(()->{
                h.assertTrue(stones(ow,a)==1&&stones(ow,b)==1,"one stone each, never a second laid while the first was loading: "+stones(ow,a)+", "+stones(ow,b));
                h.assertTrue(a.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD)&&a.blockPosition().distSqr(beds[0])<9,"back in their real bed");
                var stone=stone(ow,a);h.assertTrue(stone!=null,"the stone on their sill");
                h.assertTrue(BlockPos.of(SphinxDream.record(s,a.getUUID()).getLong("Door")).equals(o.offset(2,1,5)),"at their own door");
                h.assertTrue(DreamStela.reachOf(stone.getItem())==DreamStela.DEEP&&"shelf".equals(DreamStela.carvingOf(stone.getItem()))&&!DreamStela.chipped(stone.getItem()),"deep, the shelf cut into it");
                var sb=stone(ow,b);h.assertTrue(sb!=null&&DreamStela.chipped(sb.getItem())&&"smooth".equals(DreamStela.carvingOf(sb.getItem())),"the hostile reader's, worn smooth with a corner broken");
                stone.setNoPickUpDelay();stand(c,ow,stone.getX(),stone.getY(),stone.getZ());stone.playerTouch(c);
                h.assertTrue(stone.isAlive()&&!c.getInventory().items.stream().anyMatch(DreamStela::is),"no one else can take it");
                stand(a,ow,stone.getX(),stone.getY(),stone.getZ());stone.playerTouch(a);})
            .thenWaitUntil(()->h.assertTrue(stage(h,a)==SphinxDream.TAKEN&&a.getInventory().items.stream().anyMatch(DreamStela::is),"theirs, taken"))
            .thenExecute(()->{
                h.assertTrue(stage(h,b)==SphinxDream.WAITING||stage(h,b)==SphinxDream.SECOND,"the hostile reader's claim waits for them");
                h.assertTrue(stage(h,d)==SphinxDream.INVITED,"the reader whose House is still open has not dreamed");
                h.assertTrue(WitnessAccount.Story.of("sphinx_dream")==null&&WitnessAccount.Story.of("sphinx_field")==null,"no Witness credit");})
            .thenSucceed();
    }
}
