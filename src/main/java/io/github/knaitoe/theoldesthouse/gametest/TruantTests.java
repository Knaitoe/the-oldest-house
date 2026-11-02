package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseExperience;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.264, Johnny Truant's first slice (docs/TRUANT_0_4_264.md), played with two readers through all three of his places: the half face
 * round the corner at the lamp, which stays until it has been looked at and then draws back, and the talk there (his name, a story told,
 * a promise made as "maybe"); one body, which moves to the held door only once the lamp is empty; the door held for a reader and let go
 * behind them with his question; a hand on the crossing that catches a slip and says "There." at the far end; the door's closer on its
 * own, shoving a reader in the doorway back while he is with someone else; and a later meeting that remembers the "maybe" and the place,
 * counts the promise kept when a new story is told, and notes twenty passes.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class TruantTests {
    private TruantTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="truant") public static void done(ServerLevel l){
        if(active!=null){for(var place:TruantPlaces.PLACES)for(var a:active.in.getEntitiesOfClass(LiteraryActor.class,room(active.base(place),place)))a.discard();active.close();active=null;}
        Truant.fresh();Truant.apart();
    }
    static AABB room(BlockPos b,LabyrinthPlace place){var r=place.room();return new AABB(b.getX()+r.minX(),b.getY()+r.minY(),b.getZ()+r.minZ(),b.getX()+r.maxX()+1,b.getY()+r.maxY()+1,b.getZ()+r.maxZ()+1);}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z,float yaw){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,yaw,0);p.setYRot(yaw);p.setYHeadRot(yaw);p.setXRot(0);p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);}
    /** Somewhere in the House that is none of his places. */
    private static void away(ServerPlayer p,LiteraryTests.Fixture f){var b=f.base(LabyrinthPlace.JUNCTION);p.teleportTo(f.in,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);p.setNoGravity(true);}
    private static boolean open(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(TruantPlaces.HEAVY_DOOR));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    private static int truants(ServerLevel l,BlockPos b,LabyrinthPlace place){return l.getEntitiesOfClass(LiteraryActor.class,room(b,place),a->a.isAlive()&&Truant.isTruant(a)).size();}
    private static String said(ServerLevel l){var a=Truant.body(l);return a==null||!a.hasCustomName()?"":a.getCustomName().getString();}
    private static String where(LiteraryTests.Fixture f){var a=Truant.body(f.in);if(a==null)return "nobody; "+Truant.debug(f.in.getServer());var at=Truant.at(f.in.getServer());var b=at==null?BlockPos.ZERO:f.base(at);return String.format("at %.2f %.2f %.2f in %s gesture %d phase %d; ",a.getX()-b.getX(),a.getY()-b.getY(),a.getZ()-b.getZ(),at,a.gesture(),a.phase())+Truant.debug(f.in.getServer());}

    @GameTest(template="empty",batch="truant",timeoutTicks=2400)
    public static void johnnyHelpsAtHisThreePlacesAndRemembersWhatEachReaderSaid(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var l=f.in;var server=l.getServer();
        var door=f.base(TruantPlaces.DOOR);var ledge=f.base(TruantPlaces.LEDGE);var lamp=f.base(TruantPlaces.LAMP);
        for(var place:TruantPlaces.PLACES)hold(f,place);
        var a=NativeTestPlayers.survival(h,"truant_reader");var b=NativeTestPlayers.survival(h,"truant_second");f.players.add(a);f.players.add(b);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"his three places are resident"))
            .thenExecute(()->{
                Truant.fresh();Truant.apart();
                for(var place:TruantPlaces.PLACES){h.assertTrue(place.isGray()&&!place.isVignette()&&WitnessAccount.Story.of(place.id())==null,"an ordinary hall, and no story");TruantPlaces.build(l,f.base(place),place);LabyrinthBuilder.registerDoors(f.data(),place,f.base(place));}
                var heavy=l.getBlockState(door.offset(TruantPlaces.HEAVY_DOOR));
                h.assertTrue(heavy.is(Blocks.DARK_OAK_DOOR)&&!heavy.getValue(DoorBlock.OPEN),"the heavy door stands shut in its partition");
                h.assertTrue(l.getBlockState(door.offset(TruantPlaces.DOOR_LAMP)).is(Blocks.LANTERN)&&l.getBlockState(lamp.offset(TruantPlaces.LAMP_LIGHT)).is(Blocks.LANTERN),"his lamps are lit in the recess and the alcove");
                h.assertTrue(!l.getBlockState(ledge.offset(TruantPlaces.LEDGE_X,-1,-8)).isAir()&&l.getBlockState(ledge.offset(TruantPlaces.LEDGE_X-1,-1,-8)).isAir()&&!l.getBlockState(ledge.offset(0,TruantPlaces.LOWER-1,-8)).isAir(),"a one-block ledge along the wall over a drop to a lower landing");
                h.assertTrue(TruantPlaces.weight(f.data(),a.getUUID(),5)==0&&TruantPlaces.weight(f.data(),a.getUUID(),6)==TruantPlaces.WEIGHT,"his places are dealt from the crossing where stories begin");
                f.data().visit(a.getUUID(),LabyrinthPlace.WINTER_LAKE);
                // The first sight of him: the lamp, looking up the corridor at the corner.
                stand(a,l,lamp,.5,0,-2.5,180);a.lookAt(EntityAnchorArgument.Anchor.EYES,Vec3.atLowerCornerOf(lamp).add(TruantPlaces.PEEK_SPOT.x+.33,1.55,TruantPlaces.PEEK_SPOT.z));
            })
            .thenWaitUntil(()->h.assertTrue(Truant.at(server)==TruantPlaces.LAMP&&Truant.body(l)!=null&&Truant.lampStage()==0&&Truant.body(l).phase()==Truant.LEER&&Truant.body(l).gesture()==Truant.PEEK,"half his face round the corner, the leer: "+where(f)))
            .thenExecute(()->h.assertTrue(TruantPlaces.weight(f.data(),a.getUUID(),8)==TruantPlaces.WEIGHT,"no recent visit to his places yet in the dealer's record"))
            .thenWaitUntil(()->h.assertTrue(Truant.lampStage()>=1&&Truant.record(server,a.getUUID()).getInt("Peeked")==1,"looked at, the face draws back, and the reader's record has seen it: "+where(f)))
            .thenWaitUntil(()->h.assertTrue(Truant.lampStage()==3&&Truant.body(l).phase()==Truant.FACE&&Truant.body(l).position().distanceTo(Vec3.atLowerCornerOf(lamp).add(TruantPlaces.LAMP_SPOT))<.2,"round the corner, at his lamp, with his ordinary face: "+where(f)))
            .thenExecute(()->stand(a,l,lamp,-4.5,0,-7.6,0))
            .thenWaitUntil(()->h.assertTrue((Truant.record(server,a.getUUID()).getInt("Help")&Truant.HELP_LAMP)!=0&&said(l).equals(TruantTexts.LAMP),"he makes room at his lamp: "+where(f)))
            .thenWaitUntil(()->h.assertTrue(Talk.key(a).equals(Truant.KEY)&&Talk.waiting(a),"after helping, he asks, in the panel"))
            .thenExecute(()->{
                var lines=Talk.lines(a);
                h.assertTrue(lines.contains("The man: "+TruantTexts.ASK)&&Talk.replies(a).contains("Tell him about the frozen lake.")&&Talk.replies(a).contains("Who are you?"),"what did you find, with the last story to tell him about: "+lines+" "+Talk.replies(a));
                h.assertTrue(Talk.choose(a,"Who are you?")&&Truant.record(server,a.getUUID()).getBoolean("Named")&&Talk.lines(a).contains("The man: Johnny."),"he gives Johnny and no more");
                h.assertTrue(!Talk.replies(a).contains("Who are you?")&&Talk.choose(a,"Tell him about"),"asked again, the reader tells him about the frozen lake");
                h.assertTrue(Truant.record(server,a.getUUID()).getList("Told",Tag.TAG_STRING).getString(0).equals("winter_lake")&&Talk.lines(a).contains("Johnny: "+TruantTexts.follow("winter_lake").question()),"named, he asks one thing after it: "+Talk.lines(a));
                h.assertTrue(Talk.choose(a,Talk.replies(a).get(0))&&Talk.lines(a).contains("Johnny: "+TruantTexts.PROMISE),"then the promise");
                h.assertTrue(Talk.choose(a,"Maybe.")&&Truant.record(server,a.getUUID()).getString("Promise").equals("maybe")&&!Talk.waiting(a),"kept as said: maybe");
                var own=Truant.record(server,a.getUUID());h.assertTrue(own.getInt("Meetings")==1&&own.getInt("Talked")==1,"one meeting, one talk");
                Talk.close(a);away(a,f);
                stand(b,l,door,.5,0,-6.5,180);
            })
            // One body: he leaves the lamp only once it is empty, and stands at the held door instead.
            .thenWaitUntil(()->h.assertTrue(Truant.at(server)==TruantPlaces.DOOR&&truants(l,door,TruantPlaces.DOOR)==1&&truants(l,lamp,TruantPlaces.LAMP)==0,"one Truant, now at the held door: "+where(f)))
            .thenWaitUntil(()->h.assertTrue(open(l,door)&&Truant.body(l).gesture()==Truant.HOLD,"he holds the heavy door for the reader coming up to it: "+where(f)))
            .thenIdle(20)
            .thenExecute(()->{h.assertTrue(open(l,door),"held, the door does not shut itself");stand(b,l,door,.5,0,-13.5,180);})
            .thenWaitUntil(()->h.assertTrue((Truant.record(server,b.getUUID()).getInt("Help")&Truant.HELP_DOOR)!=0,"through, the help is the reader's"))
            .thenWaitUntil(()->h.assertTrue(!open(l,door)&&said(l).equals(TruantTexts.ASK_DOOR),"he lets it go behind them and asks what was behind them: "+where(f)))
            .thenExecute(()->{Talk.close(b);stand(b,l,ledge,2.5,0,-2.5,180);})
            .thenWaitUntil(()->h.assertTrue(Truant.at(server)==TruantPlaces.LEDGE&&truants(l,ledge,TruantPlaces.LEDGE)==1&&truants(l,door,TruantPlaces.DOOR)==0,"at the near end of the crossing: "+where(f)))
            .thenExecute(()->stand(b,l,ledge,4.5,0,-5.5,180))
            .thenWaitUntil(()->h.assertTrue(Truant.body(l).gesture()==Truant.STEADY&&Truant.body(l).getZ()>b.getZ()+.5,"a hand between their shoulders, a step behind: "+where(f)))
            .thenExecute(()->stand(b,l,ledge,2.5,-3,-8.5,180))
            .thenWaitUntil(()->h.assertTrue(Math.abs(b.getY()-ledge.getY())<.05&&b.getX()-ledge.getX()>3.9&&(Truant.record(server,b.getUUID()).getInt("Help")&Truant.HELP_CATCH)!=0&&said(l).equals(TruantTexts.CAUGHT),"a slip off the edge is caught back onto the ledge: "+where(f)+" reader at "+b.position().subtract(Vec3.atLowerCornerOf(ledge))))
            .thenExecute(()->stand(b,l,ledge,2.5,0,-14.5,180))
            .thenWaitUntil(()->h.assertTrue((Truant.record(server,b.getUUID()).getInt("Help")&Truant.HELP_STEADY)!=0&&said(l).equals(TruantTexts.THERE),"there, at the far end: "+where(f)))
            // The door's closer on its own, while he is with someone at the crossing.
            .thenExecute(()->{
                var pos=door.offset(TruantPlaces.HEAVY_DOOR);var st=l.getBlockState(pos);((DoorBlock)st.getBlock()).setOpen(null,l,st,pos,true);
                stand(a,l,door,.5,0,-10.5,180);
            })
            .thenWaitUntil(()->h.assertTrue(!open(l,door)&&a.getZ()-door.getZ()>-9.8,"alone, the door shuts itself and shoves the reader in the doorway back: reader at "+a.position().subtract(Vec3.atLowerCornerOf(door))))
            .thenExecute(()->{
                h.assertTrue(Truant.at(server)==TruantPlaces.LEDGE&&(Truant.record(server,a.getUUID()).getInt("Help")&Truant.HELP_DOOR)==0,"he stayed with the reader at the crossing, and held nothing for this one");
                // A later meeting: apart a long while, deeper than twenty, with another story behind them.
                Truant.apart();var d=f.data();var exp=HouseExperience.record(d,a.getUUID());exp.putInt("Deepest",21);HouseExperience.save(d,a.getUUID(),exp);
                d.visit(a.getUUID(),LabyrinthPlace.CAMP_BLOOD);
                away(b,f);stand(a,l,door,.5,0,-13.5,180);
            })
            .thenWaitUntil(()->h.assertTrue(Truant.at(server)==TruantPlaces.DOOR&&Truant.body(l)!=null&&truants(l,door,TruantPlaces.DOOR)==1&&Truant.record(server,a.getUUID()).getInt("Meetings")==2,"he is back at the door for the second meeting: "+where(f)))
            .thenExecute(()->{
                var own=Truant.record(server,a.getUUID());
                h.assertTrue(own.getBoolean("Twenty")&&own.getInt("TwentyAt")==2&&own.getList("Depths",Tag.TAG_INT).getInt(1)==21,"twenty passes are noted, at the meeting they were reached");
                stand(a,l,door,1.5,0,-14,180);Truant.talk(a,Truant.body(l));
                var lines=Talk.lines(a);
                h.assertTrue(lines.contains("Johnny: You said maybe.")&&lines.contains("Johnny: Last time it was the frozen lake.")&&!lines.contains("Johnny: You said you'd tell me."),"he remembers what this reader actually said, and the place they told him about: "+lines);
                h.assertTrue(Talk.choose(a,"Tell him about Camp Blood.")&&Truant.record(server,a.getUUID()).getBoolean("Kept")&&Truant.record(server,a.getUUID()).getString("KeptPlace").equals("camp_blood"),"a new story told after the promise keeps it");
                h.assertTrue(Talk.choose(a,Talk.replies(a).get(0))&&Talk.lines(a).contains("Johnny: "+TruantTexts.GOODBYE)&&!Talk.waiting(a),"a promise once made is not asked for again");
                Truant.struck(a,Truant.body(l));Talk.close(a);Truant.talk(a,Truant.body(l));
                h.assertTrue(!Talk.waiting(a)&&Truant.record(server,a.getUUID()).getInt("Struck")==1&&Truant.body(l).isAlive(),"struck, he flinches and keeps his distance; he does not die");
                var other=Truant.record(server,b.getUUID());
                h.assertTrue(other.getString("Promise").isEmpty()&&other.getList("Told",Tag.TAG_STRING).isEmpty(),"the other reader's record is their own");
            })
            .thenSucceed();
    }
}
