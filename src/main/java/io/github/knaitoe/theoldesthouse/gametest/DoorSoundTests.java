package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.229, sound through connected doors: where a door leads for one reader and not another; a knock heard at the door and faintly
 * at its far side; a resident knocking back once from behind a locked door; a door shutting heard as a thud on its far side, but not
 * the two doors of one crossing; and a room heard through a closed door that leads to it, by the reader standing there alone.
 */
@GameTestHolder(TheOldestHouse.MOD_ID) @PrefixGameTestTemplate(false)
public final class DoorSoundTests {
    private DoorSoundTests(){}

    @GameTest(template="empty")
    public static void aDoorLeadsWhereItIsDealtForEachReaderAndFromInsideBackTheWayTheyCame(GameTestHelper h){
        var data=new LabyrinthData();UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        LabyrinthBuilder.registerDoors(data,LabyrinthPlace.JUNCTION,new BlockPos(0,64,0));
        LabyrinthBuilder.registerDoors(data,LabyrinthPlace.LAST_SWIMMER,new BlockPos(2048,64,0));
        var west=data.door("junction/west");h.assertTrue(west!=null,"the west door exists");
        h.assertTrue(DoorSounds.far(data,a,west)==null,"a door the House has not dealt leads nowhere yet");
        data.deal(a,west,LabyrinthPlace.LAST_SWIMMER.id(),false);data.deal(b,west,LabyrinthPlace.GRAY_CORRIDOR.id(),false);
        var entry=data.door(LabyrinthPlace.LAST_SWIMMER.entryDoorId());h.assertTrue(entry!=null,"the pool's entry door exists");
        h.assertTrue(entry.globalPos().equals(DoorSounds.far(data,a,west)),"for a, the west door comes out at the pool's own door");
        h.assertTrue(DoorSounds.leadsTo(data,b,west)==LabyrinthPlace.GRAY_CORRIDOR,"for b it leads somewhere else");
        h.assertTrue(DoorSounds.carries(LabyrinthPlace.LAST_SWIMMER)==DoorSounds.Carry.POOL&&DoorSounds.carries(LabyrinthPlace.GRAY_CORRIDOR)==null,"the pool carries its water; a plain corridor carries nothing");
        // From inside, a room's own door leads back the way the reader came.
        var back=new LabyrinthData.Waypoint(Level.OVERWORLD,Vec3.atBottomCenterOf(new BlockPos(5,64,5)),0,true,false);data.pushReturn(a,back);
        h.assertTrue(GlobalPos.of(Level.OVERWORLD,new BlockPos(5,64,5)).equals(DoorSounds.far(data,a,entry))&&DoorSounds.far(data,b,entry)==null,"from inside, back to the reader's own door, and nobody else's");
        var locked=new LabyrinthData.Door("test/locked",Level.OVERWORLD,new BlockPos(9,64,9),Direction.NORTH,LabyrinthData.LOCKED,true);data.putDoor(locked);
        h.assertTrue(DoorSounds.far(data,a,locked)==null,"a resident's door leads to nobody's side");
        h.succeed();
    }

    private static void door(GameTestHelper h,BlockPos rel,boolean open){
        var s=Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.OPEN,open);
        h.setBlock(rel,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));h.setBlock(rel.above(),s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }

    @GameTest(template="empty",timeoutTicks=200)
    public static void aKnockCarriesThroughAResidentKnocksBackAndARoomIsHeardThroughItsDoor(GameTestHelper h){
        DoorSounds.clearAll();
        var l=h.getLevel();var data=new LabyrinthData();
        // A hall door here, and the pool's own door a few blocks off, both in loaded ground.
        BlockPos hallAt=h.absolutePos(new BlockPos(1,1,1)),poolAt=h.absolutePos(new BlockPos(6,1,1)),lockedAt=h.absolutePos(new BlockPos(1,1,5));
        door(h,new BlockPos(1,1,1),false);door(h,new BlockPos(6,1,1),false);door(h,new BlockPos(1,1,5),false);
        var hall=new LabyrinthData.Door("test/hall",l.dimension(),hallAt,Direction.SOUTH,"place:"+LabyrinthPlace.LAST_SWIMMER.id(),true);
        var pool=new LabyrinthData.Door(LabyrinthPlace.LAST_SWIMMER.entryDoorId(),l.dimension(),poolAt,Direction.SOUTH,LabyrinthData.RETURN,true);
        var locked=new LabyrinthData.Door("test/locked",l.dimension(),lockedAt,Direction.SOUTH,LabyrinthData.LOCKED,true);
        data.putDoor(hall);data.putDoor(pool);data.putDoor(locked);
        @SuppressWarnings("removal") var p=h.makeMockServerPlayerInLevel();p.setGameMode(GameType.SURVIVAL);
        var front=Vec3.atBottomCenterOf(hallAt.relative(Direction.SOUTH));p.teleportTo(front.x,front.y,front.z);
        h.assertTrue(DoorSounds.knock(p,data,hall),"a knock on the closed door");
        h.assertTrue(DoorSounds.heard().stream().anyMatch(s->s.what().equals("knock")&&s.at().pos().equals(hallAt)),"heard at the door struck");
        h.assertTrue(DoorSounds.heard().stream().anyMatch(s->s.what().equals("knock_far")&&s.at().pos().equals(poolAt)),"and faintly at the pool's door, where it leads: "+DoorSounds.heard());
        h.assertTrue(!DoorSounds.knock(p,data,hall),"one knock at a time from the same hand");
        // Standing at the door, the reader hears the pool behind it, for them alone: 0.4.246, its own rhythm, the pace clock and a far turn at the wall.
        h.assertTrue(DoorSounds.listen(p,data,10_000)&&DoorSounds.heard().stream().anyMatch(s->s.what().equals("through_pool")&&p.getUUID().equals(s.only())),"the pool behind the door, heard by the reader at it");
        h.assertTrue(!DoorSounds.listen(p,data,10_001),"not again at once");
        // A door shutting is heard on its far side; the pool's own door shutting a moment after (a crossing) carries nothing back.
        DoorSounds.used(p,hall);int before=(int)DoorSounds.heard().stream().filter(s->s.what().equals("thud_far")).count();
        DoorSounds.changed(l,data,hallAt,false);
        h.assertTrue(DoorSounds.heard().stream().filter(s->s.what().equals("thud_far")).count()==before+1,"the door shutting, heard at the pool's door");
        h.runAfterDelay(DoorSounds.KNOCK_GAP+1,()->{
            DoorSounds.knock(p,data,locked);
            h.runAfterDelay(DoorSounds.REPLY_AFTER+2,()->{
                h.assertTrue(DoorSounds.heard().stream().anyMatch(s->s.what().equals("reply")&&s.at().pos().equals(lockedAt)),"someone behind the locked door knocks back: "+DoorSounds.heard());
                long replies=DoorSounds.heard().stream().filter(s->s.what().equals("reply")).count();
                h.runAfterDelay(DoorSounds.KNOCK_GAP+1,()->{DoorSounds.knock(p,data,locked);
                    h.runAfterDelay(DoorSounds.REPLY_AFTER+2,()->{h.assertTrue(DoorSounds.heard().stream().filter(s->s.what().equals("reply")).count()==replies,"only once");DoorSounds.clearAll();h.succeed();});});
            });
        });
    }

    /** 0.4.239: a room sounds as it is now. The séance house empty once the family is driven out; Masque's clock stopped for whoever stopped it; Harrigan's study a funeral on the last visit. */
    @GameTest(template="empty")
    public static void aRoomSoundsThroughItsDoorAsItIsNow(GameTestHelper h){
        var server=h.getLevel().getServer();var data=new LabyrinthData();UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        h.assertTrue(DoorSounds.heard(server,data,a,null,null)==null&&DoorSounds.heard(server,data,a,LabyrinthPlace.GRAY_CORRIDOR,null)==null,"nowhere, and a plain corridor, carry nothing");
        h.assertTrue(DoorSounds.heard(server,data,a,LabyrinthPlace.NIGHTCLUB,null).carry()==DoorSounds.Carry.MUSIC,"a room with nothing of its own to change sounds as its kind does");
        h.assertTrue(DoorSounds.heard(server,data,a,LabyrinthPlace.SEANCE,null).carry()==DoorSounds.Carry.VOICES,"the family at the table");
        var all=data.state(ClassicsVignettes.STATE);all.putBoolean("SittingEnded",true);all.putString("SharedOutcome","drove_the_family_out");data.setState(ClassicsVignettes.STATE,all);
        h.assertTrue(DoorSounds.heard(server,data,a,LabyrinthPlace.SEANCE,null).carry()==DoorSounds.Carry.EMPTY,"driven out, and the house empty");
        h.assertTrue(DoorSounds.heard(server,data,a,LabyrinthPlace.MASQUE,null).carry()==DoorSounds.Carry.CLOCK,"the clock in the seventh room");
        var own=LiteraryVignettes.personal(data,a,LabyrinthPlace.MASQUE);own.putBoolean("ClockStopped0506",true);LiteraryVignettes.save(data,a,LabyrinthPlace.MASQUE,own);
        h.assertTrue(DoorSounds.heard(server,data,a,LabyrinthPlace.MASQUE,null)==null&&DoorSounds.heard(server,data,b,LabyrinthPlace.MASQUE,null).carry()==DoorSounds.Carry.CLOCK,"stopped for whoever stopped it, and only for them");
        h.assertTrue(DoorSounds.heard(server,data,a,LabyrinthPlace.HARRIGAN,null).carry()==DoorSounds.Carry.CLOCK,"the study's clock");
        var room=data.state(LabyrinthPlace.HARRIGAN.id());room.putInt("Visit",HarriganVignette.FINAL_VISIT);data.setState(LabyrinthPlace.HARRIGAN.id(),room);
        h.assertTrue(DoorSounds.heard(server,data,a,LabyrinthPlace.HARRIGAN,null).carry()==DoorSounds.Carry.VOICES,"on the last visit, people talking low by the casket");
        h.assertTrue(VacantPossession.heard(null).carry()==DoorSounds.Carry.SOMEONE&&LastSwimmer.heard(null).carry()==DoorSounds.Carry.POOL,"a flat and a pool nobody is in are as they were before anyone came");
        h.succeed();
    }
}
