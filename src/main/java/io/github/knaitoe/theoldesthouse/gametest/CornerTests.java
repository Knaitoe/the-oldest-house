package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.408, The House on the Corner, with real readers. The street built whole at siesta; the exercise book on the kerb. The dare: the
 * children take the reader through the gate into the front room, the jars on the boards, her stories about her arm, the door at the back
 * that will not open, and home. The last room: a reader who lost an arm in the House hears "we match" and the true story, holds her hand
 * to the last door, and opens it on a room smaller than the door; the account resolves the story. Two readers on the last day: one holds
 * her hand and opens the door, one waits outside until the front door opens onto the street; each account is their own. After: the
 * street at night, her brother older on the kerb, the windows lit and one jar more.
 * <p>0.4.578, the lot's ironwork: the fence, its posts, its corner, the gate and the balconies' rails are the street's own fittings,
 * none of them the game's bars; the dog lies along the pavement.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class CornerTests {
    private CornerTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.CORNER_HOUSE;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);Corner.clearAll();return active;}
    private static AABB box(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()-37,b.getY()-4,b.getZ()-123,b.getX()+45,b.getY()+16,b.getZ()+2);}
    private static String account(LiteraryTests.Fixture f,ServerPlayer p){var book=CornerTexts.account(p.getGameProfile().getName(),f.own(p,P));var c=book.get(DataComponents.WRITTEN_BOOK_CONTENT);return String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());}
    /** A reader who has had the first two days. */
    private static void twoDays(LiteraryTests.Fixture f,ServerPlayer p){var own=f.own(p,P);own.putInt("Days",2);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);}
    private static boolean open(LiteraryTests.Fixture f,net.minecraft.core.BlockPos door){var s=f.level(P).getBlockState(f.base(P).offset(door));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    /** Takes her hand at the mouth of the corridor and goes down it with her, two steps behind, to the last door. */
    private static void downTheCorridor(LiteraryTests.Fixture f,ServerPlayer p){
        var l=f.level(P);var b=f.base(P);var her=Corner.actor(l,b,Corner.ROMINA);
        her.moveTo(b.getX()+18.5,b.getY(),b.getZ()-117.5,180,0);f.at(p,P,18.5,0,-115.5);
    }

    @GameTest(template="empty",batch="corner_dare",timeoutTicks=4000)
    public static void theDareTheChildrenTakeTheReaderInAndTheBackDoorWillNotOpen(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] me=new ServerPlayer[1];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the street is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.kind()==LabyrinthPlace.Kind.RECURRING&&P.authored()&&SceneClock.at(f.origin,b.offset(0,0,-30),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE)==19,"a literary street outside, held every visit, built whole, at siesta");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("memory")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"a memory story, the quota derived");
                h.assertTrue(CornerBlock.is(l.getBlockState(b.offset(CornerSite.NOTEBOOK)),CornerBlock.Kind.NOTEBOOK)&&CornerBlock.is(l.getBlockState(b.offset(CornerSite.NOTE)),CornerBlock.Kind.NOTE),"the exercise book and the pebble on the kerb");
                h.assertTrue(CornerBlock.is(l.getBlockState(b.offset(CornerSite.GATE_W)),CornerBlock.Kind.GATE)&&!CornerBlock.open(l.getBlockState(b.offset(CornerSite.GATE_W))),"the tall gate, shut");
                int jars=0;for(var at:CornerSite.JARS)if(l.getBlockState(b.offset(at)).getValue(CornerBlock.STAGE)>0)jars++;
                h.assertTrue(jars==CornerSite.FIRST_JARS,"three jars on the boards: "+jars);
                h.assertTrue(l.getBlockState(b.offset(CornerSite.LAST_DOOR)).getBlock() instanceof DoorBlock&&l.getBlockState(b.offset(CornerSite.NICHE)).isAir()&&!l.getBlockState(b.offset(CornerSite.NICHE.above())).isAir(),"the last door, and behind it a room smaller than the door");
                for(int z=CornerSite.BACK_DOOR.getZ()-1;z>=CornerSite.HALL_N;z--)h.assertTrue(l.getBlockState(b.offset(18,0,z)).isAir()&&l.getBlockState(b.offset(18,1,z)).isAir(),"the corridor runs clear to the last door at z="+z);
                h.assertTrue(CornerSite.HALL_N<CornerSite.HOUSE_N-20,"the corridor goes on long past the back of the house");
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=String.join(" ",text.pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(all.contains("Something that happened to me")&&all.contains("One day I am going to go all the way in"),"the exercise book: "+all);
                me[0]=f.player("corner_dare",P);f.at(me[0],P,18.5,0,-58.5);
            })
            .thenWaitUntil(()->h.assertTrue(Corner.dayOf(b)==Corner.DARE&&Corner.actor(l,b,Corner.ROMINA)!=null&&Corner.actor(l,b,Corner.NACHO)!=null&&Corner.actor(l,b,Corner.LULI)!=null,"the dare: three children on the kerb"))
            .thenExecute(()->{
                var her=Corner.actor(l,b,Corner.ROMINA);h.assertTrue(Corner.oneArm(her.phase())&&Corner.child(her.phase()),"the girl with one arm, a child");
                Corner.talk(me[0],her);h.assertTrue(Talk.waiting(me[0],Corner.TALK)&&f.own(me[0],P).getList("Stories",3).size()==1,"she tells a story about her arm");Talk.close(me[0]);
                Corner.talk(me[0],her);Talk.close(me[0]);h.assertTrue(f.own(me[0],P).getList("Stories",3).size()==2,"a different one the second time");
            })
            .thenWaitUntil(()->h.assertTrue(Corner.stage(b)>=Corner.GOING&&CornerBlock.open(l.getBlockState(b.offset(CornerSite.GATE_W)))&&open(f,CornerSite.FRONT_DOOR),"they go over: the gate and the front door open"))
            .thenWaitUntil(()->h.assertTrue(Corner.stage(b)==Corner.INSIDE,"the children are in the front room"))
            .thenExecute(()->{
                f.at(me[0],P,16.5,0,-81.5);
                f.click(me[0],P,CornerSite.JARS.get(0));h.assertTrue(f.own(me[0],P).getBoolean("Jars"),"a jar of small teeth");
                f.click(me[0],P,CornerSite.BACK_DOOR);h.assertTrue(!open(f,CornerSite.BACK_DOOR),"the door at the back will not open on the first day");
                Corner.skip(b,Corner.ROOM_BEAT+99);
            })
            .thenWaitUntil(()->h.assertTrue(Corner.stage(b)>=Corner.HOME,"they go home"))
            .thenExecute(()->{
                var o=f.own(me[0],P);h.assertTrue(o.getInt("Days")==1&&o.getBoolean("Whole")&&o.getString("Done").isEmpty()&&!o.getBoolean("Ready"),"the dare counts as the first day; nothing is ready yet: "+o);
                h.assertTrue(LiteraryVignettes.canDeal(f.data(),me[0].getUUID(),P),"the house is there again on the next visit");
                Corner.resetNow(l,b);
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="corner_last",timeoutTicks=4000)
    public static void theLastRoomWeMatchHerHandTheDoorAndTheAccount(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] me=new ServerPlayer[1];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the street is resident"))
            .thenExecute(()->{me[0]=f.player("corner_last",P);twoDays(f,me[0]);BodyLoss.takeArm(me[0]);f.at(me[0],P,18.5,0,-58.5);})
            .thenWaitUntil(()->h.assertTrue(Corner.dayOf(b)==Corner.LAST&&Corner.actor(l,b,Corner.ROMINA)!=null,"the third day"))
            .thenExecute(()->{
                Corner.talk(me[0],Corner.actor(l,b,Corner.ROMINA));Talk.close(me[0]);
                h.assertTrue(f.own(me[0],P).getBoolean("TrueStory"),"she sees the arm the House took: we match, and the true story, once");
                Corner.talk(me[0],Corner.actor(l,b,Corner.ROMINA));Talk.close(me[0]);
                h.assertTrue(f.own(me[0],P).getList("Stories",3).size()==1,"after that, her other stories again");
            })
            .thenWaitUntil(()->h.assertTrue(Corner.stage(b)==Corner.INSIDE,"in the front room; her brother and their friend stay at the gate"))
            .thenExecute(()->{var n=Corner.actor(l,b,Corner.NACHO);h.assertTrue(n!=null&&n.getZ()-b.getZ()>CornerSite.HOUSE_S,"Nacho won't go in");f.at(me[0],P,18.5,0,-84.5);Corner.skip(b,Corner.ROOM_BEAT-1);})
            .thenWaitUntil(()->h.assertTrue(open(f,CornerSite.BACK_DOOR)&&Corner.actor(l,b,Corner.ROMINA).gesture()==Corner.HAND_OUT,"the back door opens; she holds out her hand"))
            .thenExecute(()->{f.at(me[0],P,18.5,0,-85.5);Corner.talk(me[0],Corner.actor(l,b,Corner.ROMINA));h.assertTrue(me[0].getUUID().equals(Corner.holder(b))&&f.own(me[0],P).getBoolean("HeldEver"),"they take her hand");})
            .thenWaitUntil(()->h.assertTrue(Corner.stage(b)==Corner.WALKING,"down the corridor"))
            .thenExecute(()->downTheCorridor(f,me[0]))
            .thenWaitUntil(()->h.assertTrue(Corner.stage(b)==Corner.GONE,"she lets go, goes into the last room, and the door shuts"))
            .thenExecute(()->{
                var o=f.own(me[0],P);h.assertTrue(o.getBoolean("HeldToDoor")&&o.getBoolean("SawGo")&&o.getBoolean("Hall")&&Corner.actor(l,b,Corner.ROMINA)==null&&!open(f,CornerSite.LAST_DOOR),"held to the door, saw her go, walked the corridor: "+o);
                f.at(me[0],P,18.5,0,-118.5);f.click(me[0],P,CornerSite.LAST_DOOR);
                o=f.own(me[0],P);h.assertTrue(o.getString("Done").equals("opened")&&o.getBoolean("Ready")&&!open(f,CornerSite.LAST_DOOR),"opened, for them alone: the account is ready: "+o);
                f.source(me[0],P);f.read(me[0],P,CornerSite.NOTE);
                o=f.own(me[0],P);h.assertTrue(o.getBoolean("Completed")&&WitnessAccount.has(f.data(),me[0].getUUID(),WitnessAccount.Story.CORNER_HOUSE),"the account read: the story resolved");
                var text=account(f,me[0]);
                h.assertTrue(text.contains("said we matched")&&text.contains("a fever")&&text.contains("I held her hand all the way to the last door")&&text.contains("smaller than the door was")&&text.contains("Hers is the only true story"),"the account: "+text);
                h.assertTrue(!text.matches(".*\\b(afraid|terrified|happy|sad|scared)\\b.*"),"never told how they felt: "+text);
                Corner.resetNow(l,b);
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="corner_two",timeoutTicks=5000)
    public static void twoReadersOneHoldsHerHandOneWaitsOutsideAndTheStreetAtNightAfter(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] r=new ServerPlayer[3];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the street is resident"))
            .thenExecute(()->{r[0]=f.player("corner_hand",P);r[1]=f.player("corner_wait",P);twoDays(f,r[0]);twoDays(f,r[1]);f.at(r[0],P,18.5,0,-58.5);f.at(r[1],P,19.5,0,-58.5);})
            .thenWaitUntil(()->h.assertTrue(Corner.dayOf(b)==Corner.LAST&&Corner.stage(b)==Corner.INSIDE,"the third day, the children over the road"))
            .thenExecute(()->{f.at(r[0],P,18.5,0,-84.5);Corner.skip(b,Corner.ROOM_BEAT-1);})
            .thenWaitUntil(()->h.assertTrue(Corner.actor(l,b,Corner.ROMINA).gesture()==Corner.HAND_OUT,"her hand out"))
            .thenExecute(()->{f.at(r[0],P,18.5,0,-85.5);Corner.talk(r[0],Corner.actor(l,b,Corner.ROMINA));h.assertTrue(r[0].getUUID().equals(Corner.holder(b)),"one reader holds her hand");})
            .thenWaitUntil(()->h.assertTrue(Corner.stage(b)==Corner.WALKING,"down the corridor"))
            .thenExecute(()->downTheCorridor(f,r[0]))
            .thenWaitUntil(()->h.assertTrue(Corner.stage(b)==Corner.GONE,"she has gone in"))
            .thenExecute(()->{f.click(r[0],P,CornerSite.LAST_DOOR);h.assertTrue(f.own(r[0],P).getString("Done").equals("opened"),"the one at the door opens it");})
            .thenWaitUntil(()->h.assertTrue(f.own(r[1],P).getString("Done").equals("waited")&&open(f,CornerSite.FRONT_DOOR),"the one outside waits until the front door opens onto the street: "+f.own(r[1],P)))
            .thenExecute(()->{
                var a=account(f,r[0]);var w=account(f,r[1]);
                h.assertTrue(a.contains("I held her hand all the way")&&a.contains("I opened the door")&&!a.contains("Nobody came out"),"the one who held her hand: "+a);
                h.assertTrue(w.contains("I didn't take her hand")&&w.contains("Nobody came out")&&!w.contains("I opened the door"),"the one who waited: "+w);
                h.assertTrue(f.own(r[0],P).getBoolean("Ready")&&f.own(r[1],P).getBoolean("Ready"),"each account ready for its own reader");
                Corner.resetNow(l,b);
            })
            .thenExecute(()->{r[2]=f.player("corner_after",P);var own=f.own(r[2],P);own.putInt("Days",3);own.putString("Done","opened");LiteraryVignettes.save(f.data(),r[2].getUUID(),P,own);f.at(r[2],P,18.5,0,-58.5);})
            .thenWaitUntil(()->h.assertTrue(Corner.dayOf(b)==Corner.AFTER&&Corner.stage(b)==Corner.NIGHT&&Corner.actor(l,b,Corner.NACHO_GROWN)!=null,"the street at night, her brother older on the kerb"))
            .thenExecute(()->{
                h.assertTrue(Corner.actor(l,b,Corner.ROMINA)==null&&!Corner.child(Corner.NACHO_GROWN),"no children on the kerb now");
                h.assertTrue(l.getBlockState(b.offset(CornerSite.JARS.get(CornerSite.FIRST_JARS))).getValue(CornerBlock.STAGE)>0,"one jar more on the boards");
                h.assertTrue(l.getBlockState(b.offset(CornerSite.WINDOWS.get(0))).getValue(CornerBlock.STAGE)==1,"the house's windows lit");
                h.assertTrue(f.own(r[2],P).getInt("After")==1,"one night after");
                Corner.talk(r[2],Corner.actor(l,b,Corner.NACHO_GROWN));h.assertTrue(f.own(r[2],P).getBoolean("AfterTalked"),"he tells them what he has been hearing");Talk.close(r[2]);
                Corner.resetNow(l,b);
            })
            .thenSucceed();
    }

    private static void close(){if(active!=null){active.close();active=null;}Corner.clearAll();}
    /**
     * 0.4.578 (the owner: the iron fence is improper, loose bars, no top rail, a gap above the base rail, grey bars against a black
     * gate). The fence was the game's iron bars set with their default state, so no bar joined the next. Every cell of it is the
     * street's own fitting now, three high, its stage the cell's height; a post stands at each side of the gate and at every third
     * cell, the gate's head is the fence's top cell, the corner is one piece, and each leaf of the gate hangs from its own post.
     */
    @GameTest(template="empty",batch="corner_iron",timeoutTicks=2400)
    public static void theLotsIronworkIsItsOwnAndStandsWhereItShould(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the street is resident"))
            .thenExecute(()->{
                int front=CornerSite.NORTH_FRONT,gw=CornerSite.GATE_W.getX(),ge=CornerSite.GATE_E.getX();
                h.assertTrue(CornerSite.GATE_W.getZ()==front&&CornerSite.GATE_E.getZ()==front&&ge==gw+1,"the gate stands in the line of the fence, two cells wide");
                int posts=0;
                for(int x=CornerSite.LOT_W;x<=CornerSite.LOT_E;x++)for(int y=0;y<=2;y++){
                    var s=l.getBlockState(b.offset(x,y,front));
                    if((x==gw||x==ge)&&y<2){h.assertTrue(CornerBlock.is(s,CornerBlock.Kind.GATE)&&!CornerBlock.open(s)&&s.getValue(CornerBlock.STAGE)==y*2,"a leaf of the gate, shut, at "+x+","+y+": "+s);continue;}
                    h.assertTrue((CornerBlock.is(s,CornerBlock.Kind.FENCE)||CornerBlock.is(s,CornerBlock.Kind.FENCE_POST))&&s.getValue(CornerBlock.STAGE)==y&&s.getValue(CornerBlock.FACING).getAxis()==Direction.Axis.Z,"the fence along the front at "+x+","+y+": "+s);
                    if(x==gw||x==ge)h.assertTrue(CornerBlock.is(s,CornerBlock.Kind.FENCE),"the fence's top cell over the gate at "+x);
                    if(y==0&&CornerBlock.is(s,CornerBlock.Kind.FENCE_POST))posts++;
                }
                for(int y=0;y<=2;y++){
                    var west=l.getBlockState(b.offset(gw-1,y,front));var east=l.getBlockState(b.offset(ge+1,y,front));
                    h.assertTrue(CornerBlock.is(west,CornerBlock.Kind.FENCE_POST)&&west.getValue(CornerBlock.FACING)==Direction.SOUTH&&CornerBlock.is(east,CornerBlock.Kind.FENCE_POST)&&east.getValue(CornerBlock.FACING)==Direction.NORTH,"a post at each side of the gate, each at the gate's edge of its cell, at height "+y+": "+west+" "+east);
                    var corner=l.getBlockState(b.offset(CornerSite.LOT_E+1,y,front));
                    h.assertTrue(CornerBlock.is(corner,CornerBlock.Kind.FENCE_CORNER)&&corner.getValue(CornerBlock.STAGE)==y,"the corner post at height "+y+": "+corner);
                }
                h.assertTrue(posts>=(CornerSite.LOT_E-CornerSite.LOT_W)/CornerSite.FENCE_POSTS,"a post at every third cell of the front: "+posts);
                var leafW=l.getBlockState(b.offset(CornerSite.GATE_W));var leafE=l.getBlockState(b.offset(CornerSite.GATE_E));
                h.assertTrue(leafW.getValue(CornerBlock.FACING)==Direction.NORTH&&leafE.getValue(CornerBlock.FACING)==Direction.SOUTH,"each leaf of the gate hangs from its own post: "+leafW+" "+leafE);
                var shutW=leafW.getCollisionShape(l,b.offset(CornerSite.GATE_W));var openW=leafW.setValue(CornerBlock.STAGE,1).getShape(l,b.offset(CornerSite.GATE_W));var openE=leafE.setValue(CornerBlock.STAGE,1).getShape(l,b.offset(CornerSite.GATE_E));
                h.assertTrue(!shutW.isEmpty()&&shutW.max(Direction.Axis.Y)>1.4&&leafW.setValue(CornerBlock.STAGE,1).getCollisionShape(l,b.offset(CornerSite.GATE_W)).isEmpty(),"a shut leaf stops a body and is too tall to jump; an open one does not");
                h.assertTrue(openW.max(Direction.Axis.X)<.2&&openE.min(Direction.Axis.X)>.8,"open, the west leaf stands at the west post and the east leaf at the east: "+openW.bounds()+" "+openE.bounds());
                var run=l.getBlockState(b.offset(CornerSite.LOT_W+1,0,front)).getCollisionShape(l,b.offset(CornerSite.LOT_W+1,0,front));
                h.assertTrue(!run.isEmpty()&&run.max(Direction.Axis.Y)>1.4&&run.min(Direction.Axis.X)==0&&run.max(Direction.Axis.X)==1,"a cell of the fence stops a body from end to end and is too tall to jump: "+run.bounds());
                int east=0,west=0;
                for(int z=front-1;z>=CornerSite.HOUSE_N;z--)for(int y=0;y<=2;y++){
                    var s=l.getBlockState(b.offset(CornerSite.LOT_E+1,y,z));
                    h.assertTrue((CornerBlock.is(s,CornerBlock.Kind.FENCE)||CornerBlock.is(s,CornerBlock.Kind.FENCE_POST))&&s.getValue(CornerBlock.STAGE)==y&&s.getValue(CornerBlock.FACING).getAxis()==Direction.Axis.X,"the fence down the lot's east side at "+z+","+y+": "+s);east++;
                    var w=l.getBlockState(b.offset(CornerSite.LOT_W-1,y,z));
                    if(CornerBlock.fence(w)){h.assertTrue(w.getValue(CornerBlock.STAGE)==y&&w.getValue(CornerBlock.FACING).getAxis()==Direction.Axis.X,"the fence down the lot's west side at "+z+","+y+": "+w);west++;}
                }
                h.assertTrue(east>=60&&west>=24,"both sides of the lot are fenced: "+east+" cells east, "+west+" west");
                int bars=0,rails=0,loose=0,fences=0;BlockPos first=null;var box=box(f);
                for(var at:BlockPos.betweenClosed((int)box.minX,(int)box.minY,(int)box.minZ,(int)box.maxX,(int)box.maxY,(int)box.maxZ)){
                    var s=l.getBlockState(at);
                    if(s.is(Blocks.IRON_BARS)){bars++;if(first==null)first=at.immutable();}
                    if(CornerBlock.fence(s))fences++;
                    if(CornerBlock.is(s,CornerBlock.Kind.RAILING)){
                        rails++;var under=l.getBlockState(at.below());
                        if(!under.is(Blocks.SMOOTH_STONE_SLAB)||under.getValue(SlabBlock.TYPE)!=SlabType.TOP){loose++;if(first==null)first=at.immutable();}
                    }
                }
                h.assertTrue(bars==0,"none of the game's iron bars is left on the street: "+bars+", the first at "+first);
                h.assertTrue(rails>=6&&rails%3==0&&loose==0,"every balcony has a rail three cells long, each cell of it on the ledge: "+rails+" cells, "+loose+" on nothing, the first at "+first);
                h.assertTrue(fences>=east+west+3*(CornerSite.LOT_E-CornerSite.LOT_W),"the lot's fence is whole: "+fences+" cells");
                var dog=l.getBlockState(b.offset(6,0,-34));var lying=dog.getShape(l,b.offset(6,0,-34));
                h.assertTrue(CornerBlock.is(dog,CornerBlock.Kind.DOG)&&dog.getValue(CornerBlock.FACING)==Direction.NORTH,"the dog asleep on the pavement, its head up the street: "+dog);
                h.assertTrue(lying.max(Direction.Axis.Y)<=.3&&lying.max(Direction.Axis.Z)-lying.min(Direction.Axis.Z)>.95&&lying.max(Direction.Axis.X)-lying.min(Direction.Axis.X)>.6,"it lies low, along the pavement and across a good part of it: "+lying.bounds());
            }).thenSucceed();
    }

    @AfterBatch(batch="corner_iron") public static void ironDone(net.minecraft.server.level.ServerLevel l){close();}
    @AfterBatch(batch="corner_dare") public static void dareDone(net.minecraft.server.level.ServerLevel l){close();}
    @AfterBatch(batch="corner_last") public static void lastDone(net.minecraft.server.level.ServerLevel l){close();}
    @AfterBatch(batch="corner_two") public static void twoDone(net.minecraft.server.level.ServerLevel l){close();}
}
