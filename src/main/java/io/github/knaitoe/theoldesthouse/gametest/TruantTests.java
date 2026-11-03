package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseExperience;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffects;
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
 * <p>
 * 0.4.282, the second slice (docs/TRUANT_0_4_282.md): a reader past twenty with a history with him meets the claimant and the
 * collector, and a reader under twenty standing with them keeps every meeting free of anything hostile.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class TruantTests {
    private TruantTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="truant") public static void done(ServerLevel l){
        Talk.clearAll();
        if(active!=null){for(var place:TruantPlaces.PLACES)for(var a:active.in.getEntitiesOfClass(LiteraryActor.class,room(active.base(place),place)))a.discard();active.close();active=null;}
        Truant.fresh();Truant.apart();Truant.force(null);
    }
    @AfterBatch(batch="truant_after") public static void doneAfter(ServerLevel l){done(l);}
    @AfterBatch(batch="truant_reconnect") public static void doneReconnect(ServerLevel l){done(l);}
    static AABB room(BlockPos b,LabyrinthPlace place){var r=place.room();return new AABB(b.getX()+r.minX(),b.getY()+r.minY(),b.getZ()+r.minZ(),b.getX()+r.maxX()+1,b.getY()+r.maxY()+1,b.getZ()+r.maxZ()+1);}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z,float yaw){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,yaw,0);p.setYRot(yaw);p.setYHeadRot(yaw);p.setXRot(0);p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);}
    /** Somewhere in the House that is none of his places. */
    private static void away(ServerPlayer p,LiteraryTests.Fixture f){var b=f.base(LabyrinthPlace.JUNCTION);p.teleportTo(f.in,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);p.setNoGravity(true);}
    private static boolean open(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(TruantPlaces.HEAVY_DOOR));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    private static int truants(ServerLevel l,BlockPos b,LabyrinthPlace place){return l.getEntitiesOfClass(LiteraryActor.class,room(b,place),a->a.isAlive()&&Truant.isTruant(a)).size();}
    private static String said(ServerLevel l){var a=Truant.body(l);return a==null||!a.hasCustomName()?"":a.getCustomName().getString();}
    private static String where(LiteraryTests.Fixture f){var a=Truant.body(f.in);if(a==null)return "nobody; "+Truant.debug(f.in.getServer());var at=Truant.at(f.in.getServer());var b=at==null?BlockPos.ZERO:f.base(at);return String.format("at %.2f %.2f %.2f in %s gesture %d phase %d; ",a.getX()-b.getX(),a.getY()-b.getY(),a.getZ()-b.getZ(),at,a.gesture(),a.phase())+Truant.debug(f.in.getServer());}

    /** A veteran's history with him: three meetings, all three kinds of help, a promise made and kept, one place told and answered, twenty passes noted at the second meeting, his name. */
    private static CompoundTag history(){
        var own=new CompoundTag();own.putInt("Meetings",3);own.putInt("Talked",3);own.putInt("Help",Truant.HELP_DOOR|Truant.HELP_STEADY|Truant.HELP_LAMP);
        own.putString("Promise","agreed");own.putInt("PromisedAt",1);own.putBoolean("Kept",true);own.putString("KeptPlace","winter_lake");own.putInt("KeptAt",2);own.putBoolean("Thanked",true);
        var told=new ListTag();told.add(StringTag.valueOf("winter_lake"));own.put("Told",told);var answers=new CompoundTag();answers.putInt("winter_lake",0);own.put("Answers",answers);
        own.putBoolean("Twenty",true);own.putInt("TwentyAt",2);own.putBoolean("Named",true);return own;
    }
    private static void deepest(LiteraryTests.Fixture f,ServerPlayer p,int depth){var d=f.data();var exp=HouseExperience.record(d,p.getUUID());exp.putInt("Deepest",depth);HouseExperience.save(d,p.getUUID(),exp);}
    private static boolean lit(ServerLevel l,BlockPos lamp){return l.getBlockState(lamp.offset(TruantPlaces.LAMP_LIGHT)).is(Blocks.LANTERN);}

    @AfterBatch(batch="truant_hospitality") public static void doneHospitality(ServerLevel l){done(l);}
    @GameTest(template="empty",batch="truant_hospitality",timeoutTicks=1800)
    public static void johnnyUsesPrivateHospitalityAgainstTheReader(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;
        var l=f.in;var server=l.getServer();var lamp=f.base(TruantPlaces.LAMP);
        hold(f,TruantPlaces.LAMP);hold(f,TruantPlaces.DOOR);
        var reader=NativeTestPlayers.survival(h,"bread_reader");
        var witness=NativeTestPlayers.survival(h,"bread_witness");
        var companion=NativeTestPlayers.survival(h,"bread_friend");
        f.players.add(reader);f.players.add(witness);f.players.add(companion);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"Johnny's lamp and the far destination are resident"))
            .thenExecute(()->{
                for(var place:java.util.List.of(TruantPlaces.LAMP,TruantPlaces.DOOR)){
                    TruantPlaces.build(l,f.base(place),place);
                    LabyrinthBuilder.registerDoors(f.data(),place,f.base(place));
                }
                for(var p:f.players)deepest(f,p,25);
                var own=history();own.putInt("BreadGiftAt",3);own.putInt("CoffeeGiftAt",3);
                Truant.remember(server,reader.getUUID(),own);
                Truant.force(Truant.Turn.CLAIM);
                stand(reader,l,lamp,-2.8,0,-7.7,90);
                stand(witness,l,lamp,-2.6,0,-8.6,90);
                stand(companion,l,lamp,-9.5,0,-15.5,180);
            }).thenWaitUntil(()->h.assertTrue(Truant.lampStage()==3&&Truant.turn()==Truant.Turn.CLAIM,"he has come to the lamp"))
            .thenExecute(()->{
                var far=f.data().door(TruantPlaces.LAMP.id()+"/far");
                f.data().deal(companion.getUUID(),far,TruantPlaces.DOOR.id(),false);
                stand(companion,l,lamp,-9.5,0,-18.4,180);
                LabyrinthDoors.use(companion,far);
                stand(reader,l,lamp,-4.5,0,-7.9,180);
                stand(witness,l,lamp,-3.9,0,-8.6,180);
            }).thenWaitUntil(()->h.assertTrue(room(f.base(TruantPlaces.DOOR),TruantPlaces.DOOR).contains(companion.position()),
                    "the companion actually took the lamp hall's far door"))
            .thenWaitUntil(()->h.assertTrue(Truant.body(l).distanceToSqr(Vec3.atLowerCornerOf(lamp).add(TruantPlaces.LAMP_SPOT))<.04,
                    "Johnny is in talking range at his lamp"))
            .thenExecute(()->{
                var a=Truant.body(l);Truant.talk(reader,a);
                h.assertTrue(Talk.lines(reader).stream().anyMatch(line->line.contains("Which door did bread_friend take?")),"he asks about the actual companion");
                h.assertTrue(Talk.choose(reader,"I'm not telling you.")&&said(l).contains("did I ask why you were alone"),"he makes the accusation aloud");
                witness.connection.resumeFlushing();
                h.assertTrue(NativeTestPlayers.messages(witness).stream().anyMatch(line->line.contains("did I ask why you were alone")),
                        "the witness hears the same accusation");
                h.assertTrue(Talk.choose(reader,"Call out to warn them."),"the reader can call through to the companion");
                companion.connection.resumeFlushing();
                h.assertTrue(NativeTestPlayers.messages(companion).stream().anyMatch(line->line.contains("Johnny's coming")),
                        "the warning reaches the companion on the other side");
            }).thenWaitUntil(()->h.assertTrue(Truant.body(l).getX()<lamp.getX()-5.6,"Johnny walks toward the actual far door"))
            .thenExecute(()->{
                var a=Truant.body(l);stand(witness,l,lamp,a.getX()-lamp.getX(),0,-7.4,180);Truant.talk(witness,a);
                h.assertTrue(said(l).equals("All right. Leave it."),"a witness can intervene");
                Talk.close(reader);Truant.apart();Truant.fresh();
                stand(reader,l,lamp,-2.8,0,-7.7,90);
                stand(witness,l,lamp,-2.6,0,-8.6,90);
            }).thenWaitUntil(()->h.assertTrue(Truant.lampStage()==3&&Truant.turn()==Truant.Turn.CLAIM,"later he comes to the lamp again"))
            .thenExecute(()->stand(reader,l,lamp,-4.5,0,-7.9,180))
            .thenWaitUntil(()->h.assertTrue(Truant.body(l).distanceToSqr(Vec3.atLowerCornerOf(lamp).add(TruantPlaces.LAMP_SPOT))<.04,
                    "Johnny returns to his lamp"))
            .thenExecute(()->{
                Truant.talk(reader,Truant.body(l));
                h.assertTrue(Talk.choose(reader,"Take the cup.")&&Talk.choose(reader,"Set the cup down and go."),"the second cup can be set down to leave");
                h.assertTrue(said(l).equals("You said you'd wait with me.")&&!reader.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"he reaches before the grab");
            }).thenWaitUntil(()->h.assertTrue(Truant.record(server,reader.getUUID()).getInt("CoffeeHeld")==1
                    &&reader.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"he catches the reader's wrist"))
            .thenExecute(()->{
                stand(witness,l,lamp,-4,0,-8.4,180);Truant.talk(witness,Truant.body(l));
                h.assertTrue(said(l).equals("All right. Leave it."),"a companion can make him let go");
            }).thenWaitUntil(()->h.assertTrue(!reader.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"the grip expires immediately after the interruption"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="truant_reconnect",timeoutTicks=2500)
    public static void johnnyResumesAnInterruptedQuestionWithTheSameReader(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;
        var door=f.base(TruantPlaces.DOOR);
        hold(f,TruantPlaces.DOOR);
        var id=java.util.UUID.randomUUID();
        var a=new ServerPlayer[]{NativeTestPlayers.survival(h,"truant_rejoin",id)};
        var b=NativeTestPlayers.survival(h,"truant_peer");
        f.players.add(a[0]);f.players.add(b);
        int[] prior=new int[1];
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the held door is resident"))
            .thenExecute(()->{
                Truant.fresh();Truant.apart();Truant.force(Truant.Turn.HELP);
                TruantPlaces.build(f.in,door,TruantPlaces.DOOR);
                LabyrinthBuilder.registerDoors(f.data(),TruantPlaces.DOOR,door);
                stand(a[0],f.in,door,2.5,0,-12.5,180);
                stand(b,f.in,door,2.5,0,-13.5,180);
            })
            .thenWaitUntil(()->h.assertTrue(Truant.body(f.in)!=null,"Johnny is by the held door"))
            .thenExecute(()->{
                Truant.talk(a[0],Truant.body(f.in));
                Truant.talk(b,Truant.body(f.in));
                h.assertTrue(Talk.replies(a[0]).contains("Who are you?")&&Talk.waiting(b),"both readers have their own live question");
                prior[0]=Talk.token(a[0]);
                f.players.remove(a[0]);NativeTestPlayers.remove(a[0]);
                h.assertTrue(Talk.choose(b,"Who are you?"),"the other reader can still answer while the first is disconnected");
            })
            .thenIdle(5)
            .thenExecute(()->{
                a[0]=NativeTestPlayers.survival(h,"truant_rejoin",id);f.players.add(a[0]);
                stand(a[0],f.in,door,2.5,0,-12.5,180);
                Talk.reply(a[0],prior[0],1);
                h.assertTrue(!Truant.record(f.in.getServer(),id).getBoolean("Named"),"the disconnected offer rejects a late answer");
                Truant.talk(a[0],Truant.body(f.in));
                h.assertTrue(Talk.token(a[0])!=prior[0]&&Talk.replies(a[0]).contains("Who are you?"),"same question, fresh token");
                Talk.reply(a[0],prior[0],1);
                h.assertTrue(!Truant.record(f.in.getServer(),id).getBoolean("Named"),"the previous token still cannot answer after reopening");
                h.assertTrue(Talk.choose(a[0],"Who are you?")&&Talk.choose(a[0],"Nothing I can put into words."),"the recovered question can be answered");
                h.assertTrue(Talk.choose(a[0],"Maybe.")&&!Talk.waiting(a[0]),"the reply commits the promise");
                f.players.remove(a[0]);NativeTestPlayers.remove(a[0]);
            })
            .thenIdle(5)
            .thenExecute(()->{
                a[0]=NativeTestPlayers.survival(h,"truant_rejoin",id);f.players.add(a[0]);
                stand(a[0],f.in,door,2.5,0,-12.5,180);
                Truant.talk(a[0],Truant.body(f.in));
                var own=Truant.record(f.in.getServer(),id);
                h.assertTrue(!Talk.waiting(a[0])&&Talk.lines(a[0]).contains("you: Maybe.")&&own.getString("Promise").equals("maybe")&&own.getInt("Declined")==1,"an answer already accepted reopens as the finished exchange");
                h.assertTrue(TalkHistory.conversations(f.in.getServer(),id).size()==1,"recovery keeps one transcript");
                h.assertTrue(Talk.waiting(b),"the other reader's reply remains independent");
                var peerId=b.getUUID();
                f.players.remove(b);NativeTestPlayers.remove(b);
                Truant.fresh();
                var peer=NativeTestPlayers.survival(h,"truant_peer",peerId);f.players.add(peer);
                stand(peer,f.in,door,2.5,0,-13.5,180);
                Truant.talk(peer,Truant.body(f.in));
                h.assertTrue(!Talk.waiting(peer)&&said(f.in).equals(TruantTexts.INTERRUPTED),"an expired meeting's offer stays closed");
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="truant_after",timeoutTicks=5000)
    public static void pastTwentyHeClaimsCollectsAndShovesButStillHelps(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var l=f.in;var server=l.getServer();
        var door=f.base(TruantPlaces.DOOR);var ledge=f.base(TruantPlaces.LEDGE);var lamp=f.base(TruantPlaces.LAMP);
        for(var place:TruantPlaces.PLACES)hold(f,place);
        var a=NativeTestPlayers.survival(h,"truant_veteran");var b=NativeTestPlayers.survival(h,"truant_early");f.players.add(a);f.players.add(b);
        int[] at=new int[1];
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"his three places are resident"))
            .thenExecute(()->{
                Truant.fresh();Truant.apart();
                for(var place:TruantPlaces.PLACES){TruantPlaces.build(l,f.base(place),place);LabyrinthBuilder.registerDoors(f.data(),place,f.base(place));}
                deepest(f,a,25);deepest(f,b,5);Truant.remember(server,a.getUUID(),history());f.data().visit(a.getUUID(),LabyrinthPlace.CAMP_BLOOD);
                // The gate: with a reader under twenty beside them, the covered light becomes a claim, and nothing hostile happens.
                Truant.force(Truant.Turn.COVER);
                stand(a,l,lamp,.5,0,-2.5,180);a.lookAt(EntityAnchorArgument.Anchor.EYES,Vec3.atLowerCornerOf(lamp).add(TruantPlaces.PEEK_SPOT.x+.33,1.55,TruantPlaces.PEEK_SPOT.z));
                stand(b,l,lamp,.5,0,-1.5,180);
            })
            .thenWaitUntil(()->h.assertTrue(Truant.turn()==Truant.Turn.CLAIM&&Truant.record(server,a.getUUID()).getInt("Revealed")==4,"with an early reader there, he is a claimant, not the covered light: "+where(f)))
            .thenWaitUntil(()->h.assertTrue(said(l).equals(TruantTexts.LEAN),"the half smile leans further out before it draws back: "+where(f)))
            .thenWaitUntil(()->h.assertTrue(Truant.lampStage()==3&&Truant.body(l).position().distanceTo(Vec3.atLowerCornerOf(lamp).add(TruantPlaces.LAMP_SPOT))<.2,"then to his lamp: "+where(f)))
            .thenExecute(()->{stand(a,l,lamp,-4.5,0,-7.6,0);stand(b,l,lamp,-3.5,0,-7.6,0);})
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(a)&&Talk.waiting(b)&&Talk.key(a).equals(Truant.KEY),"he helps both, and asks: "+where(f)))
            .thenExecute(()->{
                var lines=Talk.lines(a);
                h.assertTrue(lines.contains("you: "+TruantTexts.PAGE_NOTE)&&lines.stream().anyMatch(x->x.startsWith("Johnny: \"")&&x.contains("frozen lake")),"his papers: the reader's own place, in the first person: "+lines);
                h.assertTrue(lines.contains("Johnny: You didn't tell me what you left behind."),"a kept promise still leaves what he claims: "+lines);
                var own=Truant.record(server,a.getUUID());var claims=own.getList("Claims",Tag.TAG_COMPOUND);
                h.assertTrue(claims.getCompound(claims.size()-1).getString("Claim").equals("the_rest")&&own.getString("Promise").equals("agreed")&&own.getBoolean("Kept")&&own.getList("Papers",Tag.TAG_STRING).getString(0).equals("winter_lake"),"what he claims, what was said and what he wrote are kept apart: "+own);
                h.assertTrue(Talk.replies(a).contains("Tell him about Camp Blood.")&&Talk.replies(a).contains("I already told you that.")&&Talk.replies(a).contains("That's all you get."),"the reader can tell, challenge or set a limit: "+Talk.replies(a));
                h.assertTrue(Talk.choose(a,"That happened to me.")&&Truant.record(server,a.getUUID()).getInt("Corrected")==1&&Talk.lines(a).contains("Johnny: Put the lamp out. I want to see what's past it."),"corrected, he keeps it, and asks a favour: "+Talk.lines(a));
                h.assertTrue(Talk.choose(a,"No.")&&Talk.lines(a).contains("Johnny: "+TruantTexts.STILL_MINE)&&Truant.record(server,a.getUUID()).getList("Favours",Tag.TAG_COMPOUND).getCompound(0).getString("Answer").equals("refused"),"refused, and the refusal kept as said");
                var early=Truant.record(server,b.getUUID());
                h.assertTrue((early.getInt("Help")&Truant.HELP_LAMP)!=0&&early.getList("Claims",Tag.TAG_COMPOUND).isEmpty()&&Talk.lines(b).contains("The man: "+TruantTexts.ASK),"the early reader meets the man they know: "+early+" "+Talk.lines(b));
                h.assertTrue(lit(l,lamp)&&!a.hasEffect(MobEffects.BLINDNESS),"the lamp stayed lit");
                Talk.close(a);Talk.close(b);away(b,f);Truant.apart();Truant.fresh();
                stand(a,l,lamp,.5,0,-6.5,180);
            })
            // The covered light, alone.
            .thenWaitUntil(()->h.assertTrue(Truant.turn()==Truant.Turn.COVER&&Truant.lampStage()==3&&Truant.body(l).position().distanceTo(Vec3.atLowerCornerOf(lamp).add(TruantPlaces.LAMP_SPOT))<.2,"alone, at his lamp: "+where(f)))
            .thenExecute(()->stand(a,l,lamp,-4.5,0,-7.6,0))
            .thenWaitUntil(()->h.assertTrue(!lit(l,lamp)&&a.hasEffect(MobEffects.BLINDNESS),"he reaches across his lamp and it goes out: "+where(f)))
            .thenWaitUntil(()->h.assertTrue(Truant.coverStage()==3&&said(l).equals(TruantTexts.WRIST)&&a.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)&&Truant.body(l).distanceTo(a)<1.6,"across the path, a hand on the wrist: "+where(f)))
            .thenExecute(()->{var own=Truant.record(server,a.getUUID());at[0]=own.getInt("Meetings");h.assertTrue(own.getInt("Covered")==1&&own.getInt("HarmAt")==at[0]&&own.getString("Harm").equals("cover"),"the harm is kept: "+own);})
            .thenWaitUntil(()->h.assertTrue(Truant.coverStage()==4&&lit(l,lamp)&&!a.hasEffect(MobEffects.BLINDNESS)&&!a.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"he lets go and the lamp is lit again: "+where(f)))
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(a),"then he asks: "+where(f)))
            .thenExecute(()->{
                h.assertTrue(Talk.choose(a,"That's all you get.")&&Truant.record(server,a.getUUID()).getInt("Limited")==1&&!Talk.waiting(a),"a limit set, and he lets it end");
                Talk.close(a);Truant.apart();Truant.fresh();Truant.force(Truant.Turn.HELP);stand(a,l,lamp,.5,0,-6.5,180);
            })
            // Help after harm.
            .thenWaitUntil(()->h.assertTrue(Truant.lampStage()==3&&Truant.body(l).position().distanceTo(Vec3.atLowerCornerOf(lamp).add(TruantPlaces.LAMP_SPOT))<.2,"at his lamp again: "+where(f)))
            .thenExecute(()->stand(a,l,lamp,-4.5,0,-7.6,0))
            .thenWaitUntil(()->h.assertTrue(Talk.key(a).equals(Truant.KEY)&&Talk.lines(a).contains("Johnny: Sit, if you want. I'm not asking you for anything."),"after harm he helps, and asks for nothing: "+Talk.lines(a)))
            .thenExecute(()->{
                var lines=Talk.lines(a);var own=Truant.record(server,a.getUUID());
                h.assertTrue(lines.contains("Johnny: I put your light out. I had hold of you.")&&!lines.contains("Johnny: "+TruantTexts.PROMISE)&&!Talk.waiting(a),"he says what he did, and there is no question and no promise: "+lines);
                h.assertTrue(own.getInt("AfterHarm")==1&&own.getInt("ForboreAt")==own.getInt("Meetings")&&own.getInt("HisPromise")==own.getInt("Meetings"),"forbearance and his own promise are kept as his: "+own);
                Talk.close(a);Truant.apart();Truant.fresh();Truant.force(Truant.Turn.DOORWAY);stand(a,l,door,.5,0,-6.5,180);
            })
            // The hand in the doorway.
            .thenWaitUntil(()->h.assertTrue(Truant.at(server)==TruantPlaces.DOOR&&Truant.turn()==Truant.Turn.DOORWAY&&Truant.body(l)!=null&&Truant.body(l).gesture()==Truant.FRAME&&Truant.body(l).phase()==Truant.LEER&&open(l,door)&&Truant.body(l).position().distanceTo(Vec3.atLowerCornerOf(door).add(TruantPlaces.IN_DOORWAY))<.2,"he stands in the doorway he once held, a hand on the frame: "+where(f)))
            .thenExecute(()->stand(a,l,door,.5,0,-8.5,180))
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(a)&&Talk.lines(a).contains("Johnny: "+TruantTexts.NOT_YET)&&Talk.lines(a).contains("Johnny: I said I wouldn't ask. I know I said it."),"not yet; and he remembers saying he wouldn't ask: "+Talk.lines(a)))
            .thenExecute(()->{Talk.close(a);stand(a,l,door,.5,0,-10.5,180);})
            .thenWaitUntil(()->h.assertTrue(a.getZ()-door.getZ()>-10&&Truant.record(server,a.getUUID()).getInt("Struggled")==1&&!Truant.steppedAside(),"pushed past, the first time he shoves them back out of the doorway: reader at "+a.position().subtract(Vec3.atLowerCornerOf(door))+" "+where(f)))
            .thenExecute(()->stand(a,l,door,.5,0,-10.5,180))
            .thenWaitUntil(()->h.assertTrue(Truant.steppedAside()&&said(l).equals(TruantTexts.GIVE_WAY)&&Truant.body(l).position().distanceTo(Vec3.atLowerCornerOf(door).add(TruantPlaces.DOOR_SPOT))<.2,"the second time he gives way: "+where(f)))
            .thenExecute(()->{
                Talk.close(a);Truant.apart();Truant.fresh();Truant.force(Truant.Turn.SHOVE);
                stand(a,l,ledge,2.5,0,-2.5,180);stand(b,l,ledge,.5,0,-1.5,180);
            })
            // The gate again, at the crossing: an early reader there, and he only helps.
            .thenWaitUntil(()->h.assertTrue(Truant.at(server)==TruantPlaces.LEDGE&&Truant.turn()==Truant.Turn.CLAIM&&Truant.shoveStage()==0&&Truant.body(l).position().distanceTo(Vec3.atLowerCornerOf(ledge).add(TruantPlaces.LEDGE_SPOT))<.2,"at the crossing with an early reader, no shove: "+where(f)))
            .thenExecute(()->{
                away(b,f);deepest(f,b,22);Truant.apart();Truant.fresh();
                stand(a,l,ledge,2.5,0,-2.5,180);stand(b,l,ledge,2.5,0,-14.5,0);
            })
            .thenWaitUntil(()->h.assertTrue(Truant.turn()==Truant.Turn.SHOVE&&Truant.shoveStage()==0&&Truant.body(l).position().distanceTo(Vec3.atLowerCornerOf(ledge).add(TruantPlaces.SHOVE_WAIT))<.2,"in the dark end of the near landing: "+where(f)))
            .thenExecute(()->stand(a,l,ledge,4.5,0,-6.5,180))
            .thenWaitUntil(()->h.assertTrue(Truant.shoveStage()==1,"a few steps out on the ledge, he starts coming: "+where(f)))
            .thenWaitUntil(()->{var t=Truant.body(l);if(t!=null)a.lookAt(EntityAnchorArgument.Anchor.EYES,t.getEyePosition());
                h.assertTrue(Truant.shoveStage()>=4&&Truant.record(server,a.getUUID()).getString("StoppedBy").equals("turned")&&Truant.record(server,a.getUUID()).getInt("Shoved")==0,"turning to look at him stops him, hand still up: "+where(f));})
            .thenExecute(()->{
                h.assertTrue(said(l).equals(TruantTexts.STEADY),"caught coming, he says what he said the first time");
                stand(a,l,ledge,2.5,0,-2.5,180);Truant.apart();Truant.fresh();
            })
            .thenWaitUntil(()->h.assertTrue(Truant.turn()==Truant.Turn.SHOVE&&Truant.shoveStage()==0,"waiting again: "+where(f)))
            .thenExecute(()->stand(a,l,ledge,4.5,0,-6.5,180))
            .thenWaitUntil(()->h.assertTrue(Truant.record(server,a.getUUID()).getInt("Shoved")==1,"his hands land between their shoulders: "+where(f)))
            .thenExecute(()->{
                var own=Truant.record(server,a.getUUID());var w=Truant.record(server,b.getUUID());
                h.assertTrue(a.getDeltaMovement().x<-.5&&a.getDeltaMovement().y>.2&&Truant.shoveStage()==3,"pushed west over the drop and off their feet: "+a.getDeltaMovement()+" "+where(f));
                h.assertTrue(own.getString("Harm").equals("shove")&&own.getInt("ShovedAt")==own.getInt("Meetings")&&w.getInt("Witnessed")==1&&w.getInt("Shoved")==0,"the shove is the target's; the friend on the far landing saw it: "+own+" / "+w);
                stand(a,l,ledge,2.5,TruantPlaces.LOWER,-8.5,180);
            })
            .thenWaitUntil(()->h.assertTrue(said(l).equals(TruantTexts.CAREFUL)&&Truant.body(l).gesture()==Truant.OVER,"he looks over and says one word: "+where(f)))
            .thenWaitUntil(()->h.assertTrue(Truant.shoveStage()==6&&Truant.body(l).position().distanceTo(Vec3.atLowerCornerOf(ledge).add(TruantPlaces.SHOVE_WAIT))<.2,"and goes back the way he came: "+where(f)))
            .thenExecute(()->{
                var own=Truant.record(server,a.getUUID());
                h.assertTrue(own.getString("Promise").equals("agreed")&&own.getBoolean("Kept")&&own.getList("Told",Tag.TAG_STRING).size()==1,"through all of it, what the reader said stays as said: "+own);
                h.assertTrue(Truant.body(l).isAlive(),"he is still there");
            })
            .thenSucceed();
    }

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
                var walker=java.util.UUID.randomUUID();
                for(int i=0;i<4;i++)f.data().visit(walker,LabyrinthPlace.STRAIGHT_HALL);
                h.assertTrue(TruantPlaces.weight(f.data(),walker,8)==TruantPlaces.WEIGHT,"four halls keep the base weight");
                f.data().visit(walker,LabyrinthPlace.BENT_HALL);
                int boosted=TruantPlaces.weight(f.data(),walker,8);
                h.assertTrue(boosted>TruantPlaces.WEIGHT,"the fifth hall raises Johnny's weight");
                for(int i=0;i<20;i++)f.data().visit(walker,LabyrinthPlace.ALCOVE_HALL);
                h.assertTrue(TruantPlaces.weight(f.data(),walker,8)>boosted,"a longer dry spell raises it further");
                f.data().visit(walker,TruantPlaces.DOOR);
                h.assertTrue(TruantPlaces.weight(f.data(),walker,8)==0,"a Truant hall cannot immediately follow another");
                f.data().visit(a.getUUID(),LabyrinthPlace.WINTER_LAKE);
                // The first sight of him: the lamp, looking up the corridor at the corner.
                stand(a,l,lamp,.5,0,-2.5,180);a.lookAt(EntityAnchorArgument.Anchor.EYES,Vec3.atLowerCornerOf(lamp).add(TruantPlaces.PEEK_SPOT.x+.33,1.55,TruantPlaces.PEEK_SPOT.z));
            })
            .thenWaitUntil(()->h.assertTrue(Truant.at(server)==TruantPlaces.LAMP&&Truant.body(l)!=null&&Truant.lampStage()==0&&Truant.body(l).phase()==Truant.LEER&&Truant.body(l).gesture()==Truant.PEEK,"half his face round the corner, the leer: "+where(f)))
            .thenExecute(()->h.assertTrue(TruantPlaces.weight(f.data(),a.getUUID(),8)==TruantPlaces.WEIGHT,"no recent visit to his places yet in the dealer's record"))
            .thenWaitUntil(()->h.assertTrue(Truant.lampStage()>=1&&Truant.record(server,a.getUUID()).getInt("Peeked")==1,"looked at, the face draws back, and the reader's record has seen it: "+where(f)))
            .thenWaitUntil(()->h.assertTrue(Truant.lampStage()==3&&Truant.body(l).phase()==Truant.FACE&&Truant.body(l).position().distanceTo(Vec3.atLowerCornerOf(lamp).add(TruantPlaces.LAMP_SPOT))<.2,"round the corner, at his lamp, with his ordinary face: "+where(f)))
            .thenExecute(()->{a.getFoodData().setFoodLevel(10);stand(a,l,lamp,-4.5,0,-7.6,0);})
            .thenWaitUntil(()->h.assertTrue((Truant.record(server,a.getUUID()).getInt("Help")&Truant.HELP_LAMP)!=0&&said(l).equals(TruantTexts.LAMP),"he makes room at his lamp: "+where(f)))
            .thenWaitUntil(()->h.assertTrue(Talk.key(a).equals(Truant.KEY)&&Talk.waiting(a),"after helping, he asks, in the panel"))
            .thenExecute(()->{
                h.assertTrue(Talk.replies(a).contains("Take some bread.")&&Talk.choose(a,"Take the coffee."),"hungry at his lamp, the reader can accept coffee");
                h.assertTrue(a.getFoodData().getFoodLevel()==12&&Truant.record(server,a.getUUID()).getString("LastRefreshment").equals("coffee"),"coffee restores a little hunger and records the choice");
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
