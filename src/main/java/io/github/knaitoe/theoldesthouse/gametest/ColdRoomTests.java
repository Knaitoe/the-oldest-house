package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.754, the cold room (docs/COLD_ROOM_0_4_754.md). Each case builds the place itself, one case to a batch, as the waxworks' do.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_cold_room") @PrefixGameTestTemplate(false)
public final class ColdRoomTests {
    private ColdRoomTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.COLD_ROOM;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture place(GameTestHelper h){done();active=new LiteraryTests.Fixture(h);return active;}
    private static void done(){if(active!=null){active.close();active=null;}}
    private static boolean is(ServerLevel l,BlockPos b,int x,int y,int z,net.minecraft.world.level.block.Block block){return l.getBlockState(b.offset(x,y,z)).is(block);}
    private static boolean fitting(ServerLevel l,BlockPos b,BlockPos at,ColdBlock.Kind kind){return ColdBlock.is(l.getBlockState(b.offset(at)),kind);}
    private static int stage(ServerLevel l,BlockPos b,BlockPos at){return l.getBlockState(b.offset(at)).getValue(ColdBlock.STAGE);}
    private static List<String> told(ServerPlayer p){
        NativeTestPlayers.flush(p);
        var out=new java.util.ArrayList<String>();
        for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)out.add(chat.content().getString());
        return out;
    }
    private static double feet(ServerLevel l,BlockPos b,BlockPos cell){
        var at=b.offset(cell);var here=l.getBlockState(at).getCollisionShape(l,at);
        double f;
        if(here.isEmpty()){var below=l.getBlockState(at.below()).getCollisionShape(l,at.below());if(below.isEmpty())return Double.NaN;f=at.getY()-1+below.max(Direction.Axis.Y);if(f<at.getY()-.51)return Double.NaN;}
        else{double top=here.max(Direction.Axis.Y);if(top>1.01)return Double.NaN;f=at.getY()+top;}
        var body=new AABB(at.getX()+.2,f+.01,at.getZ()+.2,at.getX()+.8,f+1.79,at.getZ()+.8);
        return l.noCollision(body)?f:Double.NaN;
    }
    /** Every cell a body can walk to from a start without opening anything, up or down a block at a time, within the house. */
    private static Set<BlockPos> walked(ServerLevel l,BlockPos b,BlockPos from){
        var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();seen.add(from);queue.add(from);
        while(!queue.isEmpty()){
            var at=queue.poll();double f=feet(l,b,at);if(Double.isNaN(f))continue;
            for(var d:Direction.Plane.HORIZONTAL)for(int dy=-1;dy<=1;dy++){
                var to=at.relative(d).above(dy);
                if(seen.contains(to)||to.getX()<ColdSite.WEST||to.getX()>ColdSite.EAST||to.getZ()<ColdSite.NORTH||to.getZ()>1||to.getY()<ColdSite.BOTTOM||to.getY()>ColdSite.TOP)continue;
                double g=feet(l,b,to);if(Double.isNaN(g)||g-f>1.01)continue;
                var a=b.offset(at);var c=b.offset(to);double y=Math.max(f,g);
                var edge=new AABB((a.getX()+c.getX())/2.0+.2,y+.01,(a.getZ()+c.getZ())/2.0+.2,(a.getX()+c.getX())/2.0+.8,y+1.79,(a.getZ()+c.getZ())/2.0+.8);
                if(!l.noCollision(edge))continue;
                seen.add(to);queue.add(to);
            }
        }
        seen.removeIf(c->Double.isNaN(feet(l,b,c)));
        return seen;
    }

    /**
     * The house, built of its own things: the hall's boards, the paper on every wall, plaster overhead, the kitchen's linoleum, the
     * runner on the landing, brick outside. The letter on the hall table and the coats; the kitchen's table, chairs, stove and sink; the
     * stair of five treads to the landing; the back bedroom's bed (head and foot), its two bedside tables (the lamp lit), the cross
     * upright over the pillow, the chest of drawers shut with the book shut on it, the radiator under the glass. Outside, the steps fall
     * north from the window down to the lamp, and none of it is reached on foot from the house. A body walks from the House's door into
     * the kitchen, up the stair, across the landing and into the bedroom. The three are each where they keep, one of each however often
     * they are looked for: the mother in her chair, her daughter lying in the bed, the priest at the window. Everything answers a hand.
     * The letter names no reader. The House does not deal it.
     */
    @GameTest(template="empty",batch="cold_room_house",timeoutTicks=1600)
    public static void theHouseItsRoomsAndItsPeople(GameTestHelper h){
        var f=place(h);var p=f.player("cold_reader",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            h.assertTrue(is(l,b,-1,-1,-2,ColdRegistry.material("boards"))&&is(l,b,-3,1,-2,ColdRegistry.material("paper"))&&is(l,b,-1,3,-2,ColdRegistry.material("plaster")),"the hall: boards, paper, plaster");
            h.assertTrue(is(l,b,-6,-1,-5,ColdRegistry.material("lino"))&&is(l,b,-1,4,-10,ColdRegistry.material("runner"))&&is(l,b,0,4,-17,ColdRegistry.material("boards")),"the kitchen's linoleum, the landing's runner, the bedroom's boards");
            h.assertTrue(is(l,b,ColdSite.HOUSE_WEST,0,-5,ColdRegistry.material("brick")),"brick round the house");
            h.assertTrue(fitting(l,b,ColdSite.HALL_TABLE,ColdBlock.Kind.HALL_TABLE)&&stage(l,b,ColdSite.HALL_TABLE)==1&&fitting(l,b,ColdSite.HOOKS,ColdBlock.Kind.HOOKS),"the letter on the hall table, the coats on their hooks");
            h.assertTrue(fitting(l,b,ColdSite.KITCHEN_TABLE,ColdBlock.Kind.KITCHEN_TABLE)&&fitting(l,b,ColdSite.MOTHER_CHAIR,ColdBlock.Kind.CHAIR)&&fitting(l,b,ColdSite.OTHER_CHAIR,ColdBlock.Kind.CHAIR)&&fitting(l,b,ColdSite.STOVE,ColdBlock.Kind.STOVE)&&fitting(l,b,ColdSite.SINK,ColdBlock.Kind.SINK),"the kitchen's table, chairs, stove and sink");
            for(int i=0;i<ColdSite.TREADS;i++)h.assertTrue(is(l,b,0,i,-4-i,ColdRegistry.TREADS.get())&&is(l,b,1,i,-4-i,ColdRegistry.TREADS.get()),"tread "+i+" of the stair");
            h.assertTrue(fitting(l,b,ColdSite.BED_HEAD,ColdBlock.Kind.BED)&&stage(l,b,ColdSite.BED_HEAD)==0&&fitting(l,b,ColdSite.BED_FOOT,ColdBlock.Kind.BED)&&stage(l,b,ColdSite.BED_FOOT)==1,"the bed, its head and foot");
            h.assertTrue(stage(l,b,ColdSite.LAMP_TABLE)==0&&stage(l,b,ColdSite.FLASK_TABLE)==1&&fitting(l,b,ColdSite.FLASK_TABLE,ColdBlock.Kind.NIGHTSTAND),"a bedside table with the lamp, one with the flask");
            h.assertTrue(fitting(l,b,ColdSite.CRUCIFIX,ColdBlock.Kind.CRUCIFIX)&&stage(l,b,ColdSite.CRUCIFIX)==0,"the cross, upright, over the pillow");
            h.assertTrue(fitting(l,b,ColdSite.DRESSER,ColdBlock.Kind.DRESSER)&&stage(l,b,ColdSite.DRESSER)==0&&fitting(l,b,ColdSite.BOOK,ColdBlock.Kind.BOOK)&&stage(l,b,ColdSite.BOOK)==0,"the chest of drawers shut, the book shut on it");
            for(var at:ColdSite.RADIATOR)h.assertTrue(fitting(l,b,at,ColdBlock.Kind.RADIATOR),"the radiator at "+at.toShortString());
            h.assertTrue(is(l,b,-1,6,-22,net.minecraft.world.level.block.Blocks.GLASS_PANE)&&is(l,b,0,7,-22,net.minecraft.world.level.block.Blocks.GLASS_PANE),"the window over the radiator");
            h.assertTrue(is(l,b,0,-1,ColdSite.STEP_TOP_Z,ColdRegistry.STEPS.get())&&is(l,b,0,ColdSite.FOOT_Y-1,ColdSite.FOOT_Z0,ColdRegistry.material("flags")),"the steps outside fall to the street");
            h.assertTrue(fitting(l,b,ColdSite.STREETLAMP.above(2),ColdBlock.Kind.STREETLAMP)&&l.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,b.offset(ColdSite.STREETLAMP.above(3)))>0,"the lamp at the foot of the steps is lit");
            var walk=walked(l,b,new BlockPos(0,0,-1));
            h.assertTrue(walk.contains(new BlockPos(-6,0,-5)),"the kitchen, through its door: "+walk.size());
            h.assertTrue(walk.contains(new BlockPos(0,5,-11))&&walk.contains(new BlockPos(-1,5,-15))&&walk.contains(new BlockPos(1,5,-19)),"up the stair, across the landing and into the bedroom");
            h.assertFalse(walk.stream().anyMatch(c->c.getZ()<ColdSite.HOUSE_NORTH),"nothing outside is reached on foot from the house");
            ColdRoom.ensure(l,b);
        }).thenWaitUntil(()->{
            ColdRoom.ensure(l,b);
            for(int who=0;who<ColdRoom.CAST;who++)h.assertTrue(ColdRoom.find(l,b,who)!=null,"person "+who+" is where they keep");
        }).thenExecute(()->{
            ColdRoom.ensure(l,b);
            for(int who=0;who<ColdRoom.CAST;who++){
                int phase=ColdRoom.FIRST+who;
                var them=l.getEntitiesOfClass(LiteraryActor.class,new AABB(b).inflate(64),a->ColdRoom.is(a)&&a.phase()==phase);
                h.assertTrue(them.size()==1,"one of each, however often they are looked for: "+who+" "+them.size());
            }
            var mother=ColdRoom.find(l,b,ColdRoom.MOTHER);var girl=ColdRoom.find(l,b,ColdRoom.GIRL);var priest=ColdRoom.find(l,b,ColdRoom.PRIEST);
            h.assertTrue(ActorSeat.of(mother)!=null,"the mother sits on her chair: "+mother.position());
            h.assertTrue(girl.getPose()==Pose.SLEEPING&&girl.getSleepingPos().map(s->s.equals(b.offset(ColdSite.BED_HEAD))).orElse(false),"her daughter lies in the bed, her head on the pillow");
            h.assertTrue(ColdSite.inBedroom(priest.position().subtract(b.getX(),b.getY(),b.getZ())),"the priest is in the back bedroom: "+priest.position());
            p.teleportTo(l,b.getX()+.5,b.getY()+5,b.getZ()-16.5,90,0);told(p);
        }).thenIdle(40).thenExecute(()->{
            var girl=ColdRoom.find(l,b,ColdRoom.GIRL);
            h.assertTrue(girl!=null&&girl.getPose()==Pose.SLEEPING,"with a reader in the room she still lies there");
            for(var at:List.of(ColdSite.HOOKS,ColdSite.KITCHEN_TABLE,ColdSite.STOVE,ColdSite.SINK,ColdSite.MOTHER_CHAIR,ColdSite.CHAIR,ColdSite.BED_HEAD,ColdSite.BED_FOOT,
                ColdSite.DRESSER,ColdSite.BOOK,ColdSite.CRUCIFIX,ColdSite.LAMP_TABLE,ColdSite.FLASK_TABLE,ColdSite.RADIATOR[0],ColdSite.BULBS[0],new BlockPos(-1,6,-22)))f.click(p,P,at);
            for(int who=0;who<ColdRoom.CAST;who++)ColdRoom.talk(p,ColdRoom.find(l,b,who),f.own(p,P));
        }).thenIdle(2).thenExecute(()->{
            var why=told(p);
            for(String line:List.of(ColdTexts.HOOKS,ColdTexts.KITCHEN_TABLE,ColdTexts.STOVE,ColdTexts.SINK,ColdTexts.CHAIR,ColdTexts.BEDSIDE_CHAIR,ColdTexts.PILLOW,ColdTexts.BED,
                ColdTexts.DRESSER,ColdTexts.BOOK,ColdTexts.CRUCIFIX,ColdTexts.LAMP_TABLE,ColdTexts.FLASK_TABLE,ColdTexts.RADIATOR,ColdTexts.BULB,ColdTexts.WINDOW))
                h.assertTrue(why.contains(line),"each says what it is: missing \""+line+"\" in "+why);
            for(int who=0;who<ColdRoom.CAST;who++)h.assertTrue(why.contains(ColdTexts.touch(who)),"person "+who+" answers a hand");
            var paper=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);String text=String.join(" ",paper.pages().stream().map(pg->pg.raw().getString()).toList());
            h.assertTrue(text.contains("back bedroom")&&text.contains("Keep your coat on"),"the letter tells whoever comes where she is");
            h.assertTrue(LiteraryTexts.accountTitle(P).length()<=32,"the account's title fits a book: "+LiteraryTexts.accountTitle(P));
            h.assertFalse(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P),"the House does not deal it yet");
            done();h.succeed();
        });
    }

    /**
     * Two readers in the house and a spectator: each reader's hand is their own, the three are made once for both, and a spectator on
     * the landing reads nothing.
     */
    @GameTest(template="empty",batch="cold_room_two",timeoutTicks=1200)
    public static void twoReadersAndASpectator(GameTestHelper h){
        var f=place(h);var p=f.player("cold_one",P);var q=f.player("cold_two",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            ColdRoom.ensure(l,b);
        }).thenWaitUntil(()->{
            ColdRoom.ensure(l,b);
            for(int who=0;who<ColdRoom.CAST;who++)h.assertTrue(ColdRoom.find(l,b,who)!=null,"person "+who+" is where they keep");
        }).thenExecute(()->{
            ColdRoom.arrive(p,b,f.own(p,P));ColdRoom.arrive(q,b,f.own(q,P));
            int all=l.getEntitiesOfClass(LiteraryActor.class,new AABB(b).inflate(64),ColdRoom::is).size();
            h.assertTrue(all==ColdRoom.CAST,"two readers in, and still one of each: "+all);
            var mine=f.own(p,P);ColdRoom.talk(p,ColdRoom.find(l,b,ColdRoom.GIRL),mine);LiteraryVignettes.save(f.data(),p.getUUID(),P,mine);
            h.assertTrue((f.own(p,P).getInt("Touched")&1<<ColdRoom.GIRL)!=0&&f.own(q,P).getInt("Touched")==0,"what one reader touched is theirs alone");
            var s=NativeTestPlayers.survival(h,"cold_watch");f.players.add(s);s.setGameMode(GameType.SPECTATOR);
            s.teleportTo(l,b.getX()+.5,b.getY()+5,b.getZ()-10.5,180,0);
            try{f.click(s,P,LiteraryRooms.source(P));}catch(RuntimeException ignored){/* a spectator's click may find nothing */}
            h.assertFalse(f.own(s,P).getBoolean("Read_Source"),"a spectator at the letter reads nothing");
            h.assertFalse(LiteraryVignettes.inside(s,P),"a spectator is not in the house");
            done();h.succeed();
        });
    }
}
