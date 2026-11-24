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
            h.assertTrue(fitting(l,b,ColdSite.STREETLAMP.above(2),ColdBlock.Kind.STREETLAMP),"the lamp at the foot of the steps");
            var walk=walked(l,b,new BlockPos(0,0,-1));
            h.assertTrue(walk.contains(new BlockPos(-6,0,-5)),"the kitchen, through its door: "+walk.size());
            h.assertTrue(walk.contains(new BlockPos(0,5,-11))&&walk.contains(new BlockPos(-1,5,-15))&&walk.contains(new BlockPos(1,5,-19)),"up the stair, across the landing and into the bedroom");
            h.assertFalse(walk.stream().anyMatch(c->c.getZ()<ColdSite.HOUSE_NORTH),"nothing outside is reached on foot from the house");
            ColdRoom.ensure(l,b);
        }).thenWaitUntil(()->h.assertTrue(l.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,b.offset(ColdSite.STREETLAMP.above(3)))>0,"the lamp at the foot of the steps is lit, once the light has spread")).thenWaitUntil(()->{
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
                ColdSite.DRESSER,ColdSite.CRUCIFIX,ColdSite.LAMP_TABLE,ColdSite.FLASK_TABLE,ColdSite.RADIATOR[0],ColdSite.BULBS[0],new BlockPos(-1,6,-22)))f.click(p,P,at);
            for(int who=0;who<ColdRoom.CAST;who++)ColdRoom.talk(p,ColdRoom.find(l,b,who),f.own(p,P));
            f.click(p,P,ColdSite.BOOK);
        }).thenIdle(2).thenExecute(()->{
            var why=told(p);
            for(String line:List.of(ColdTexts.HOOKS,ColdTexts.KITCHEN_TABLE,ColdTexts.STOVE,ColdTexts.SINK,ColdTexts.CHAIR,ColdTexts.BEDSIDE_CHAIR,ColdTexts.PILLOW,ColdTexts.BED,
                ColdTexts.DRESSER,ColdTexts.RITE_BEGINS,ColdTexts.CRUCIFIX,ColdTexts.LAMP_TABLE,ColdTexts.FLASK_TABLE,ColdTexts.RADIATOR,ColdTexts.BULB,ColdTexts.WINDOW))
                h.assertTrue(why.contains(line),"each says what it is: missing \""+line+"\" in "+why);
            for(int who=0;who<ColdRoom.CAST;who++)h.assertTrue(why.contains(ColdTexts.touch(who)),"person "+who+" answers a hand");
            var paper=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);String text=String.join(" ",paper.pages().stream().map(pg->pg.raw().getString()).toList());
            h.assertTrue(text.contains("back bedroom")&&text.contains("Keep your coat on"),"the letter tells whoever comes where she is");
            h.assertTrue(LiteraryTexts.accountTitle(P).length()<=32,"the account's title fits a book: "+LiteraryTexts.accountTitle(P));
            h.assertTrue(ColdRoom.dealable(f.data(),p.getUUID()),"0.4.757: the House deals it, now that it has its end");
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

    /**
     * 0.4.755, what moves and her voice. A reader in the back bedroom looking out of the window: behind and beside them the bed rises off
     * the boards, the drawers slide out and the cross turns, one at a time, and each is heard. Turned to the bed, the bed and the cross
     * stay as they are and only the drawers behind them move; a spectator looking at the drawers keeps those still too. Put back, the room
     * is as it was. The voice speaks to one reader at a time, in turn: to the first by name, and from what the House really kept of them
     * (here, a companion lost); the second hears their own lines and not the first's. A spectator is never spoken to.
     */
    @GameTest(template="empty",batch="cold_room_night",timeoutTicks=1600)
    public static void whatMovesUnseenAndTheVoice(GameTestHelper h){
        var f=place(h);var p=f.player("cold_still",P);var q=f.player("cold_other",P);var l=f.level(P);var b=f.base(P);
        net.minecraft.server.level.ServerPlayer[] s={null};
        h.startSequence().thenIdle(5).thenExecute(()->{
            ColdRoom.ensure(l,b);ColdRoom.arrive(p,b,f.own(p,P));ColdRoom.arrive(q,b,f.own(q,P));
            p.teleportTo(l,b.getX()-1.5,b.getY()+5,b.getZ()-16.5,180,0);q.teleportTo(l,b.getX()+.5,b.getY()+1,b.getZ()-1.5,180,0);
        }).thenIdle(3).thenExecute(()->{
            boolean rose=false;
            for(int i=0;i<12;i++){
                h.assertTrue(ColdNight.stirNow(l,b),"something moves while nobody looks, try "+i);
                if(stage(l,b,ColdSite.BED_HEAD)>=2&&stage(l,b,ColdSite.BED_FOOT)>=2)rose=true;
                h.assertTrue(stage(l,b,ColdSite.BED_HEAD)/2==stage(l,b,ColdSite.BED_FOOT)/2,"the bed's head and foot rise and fall together");
            }
            h.assertTrue(rose,"the bed rose off the boards");
            h.assertTrue(stage(l,b,ColdSite.DRESSER)==1&&stage(l,b,ColdSite.CRUCIFIX)==1,"the drawers out and the cross turned");
            h.assertTrue(f.own(p,P).getInt("StirsHeard")>=12&&f.own(q,P).getInt("StirsHeard")>=12,"everyone in the house heard each: "+f.own(q,P).getInt("StirsHeard"));
            ColdNight.putBack(l,b);
            h.assertTrue(stage(l,b,ColdSite.BED_HEAD)==0&&stage(l,b,ColdSite.BED_FOOT)==1&&stage(l,b,ColdSite.DRESSER)==0&&stage(l,b,ColdSite.CRUCIFIX)==0,"put back, the room is as it was");
            p.teleportTo(l,b.getX()-1.5,b.getY()+5,b.getZ()-16.5,-90,0);
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(ColdNight.stirNow(l,b),"with the reader turned to the bed, the drawers behind them still move");
            h.assertTrue(stage(l,b,ColdSite.DRESSER)==1&&stage(l,b,ColdSite.BED_HEAD)==0&&stage(l,b,ColdSite.CRUCIFIX)==0,"what is looked at stays as it is");
            ColdNight.putBack(l,b);
            s[0]=NativeTestPlayers.survival(h,"cold_camera");f.players.add(s[0]);s[0].setGameMode(GameType.SPECTATOR);
            s[0].teleportTo(l,b.getX()-.5,b.getY()+5,b.getZ()-17.5,90,0);
        }).thenIdle(2).thenExecute(()->{
            h.assertFalse(ColdNight.stirNow(l,b),"a spectator's camera on the drawers keeps them still as well");
            h.assertTrue(stage(l,b,ColdSite.DRESSER)==0,"the drawers shut");
            var d=f.data();var rec=HotelVignette.personal(d,p.getUUID());var lost=new net.minecraft.nbt.ListTag();var pet=new net.minecraft.nbt.CompoundTag();
            pet.putString("Name","Biscuit");pet.putString("Message","Biscuit was slain by Zombie");lost.add(pet);rec.put("LostPets",lost);HotelVignette.save(d,p.getUUID(),rec);
            var lines=ColdNight.lines(p,List.of(p,q));
            h.assertTrue(lines.get(0).text().contains(p.getName().getString())&&lines.stream().anyMatch(x->x.text().contains("Biscuit")&&x.kind()==ColdNight.COMPANION),"what she has for the first is from what the House kept: "+lines);
            h.assertFalse(ColdNight.lines(q,List.of(p,q)).stream().anyMatch(x->x.text().contains("Biscuit")),"nothing of the first's in what she has for the second");
            q.teleportTo(l,b.getX()+.5,b.getY()+5,b.getZ()-19.5,0,0);told(p);told(q);
        }).thenIdle(2).thenExecute(()->{
            int pv=f.own(p,P).getInt("Voice"),qv=f.own(q,P).getInt("Voice");
            ColdNight.voiceNow(l,b);ColdNight.voiceNow(l,b);
            h.assertTrue(f.own(p,P).getInt("Voice")==pv+1&&f.own(q,P).getInt("Voice")==qv+1,"she speaks to each in turn: "+pv+">"+f.own(p,P).getInt("Voice")+" "+qv+">"+f.own(q,P).getInt("Voice"));
            h.assertTrue(f.own(s[0],P).getInt("Voice")==0,"a spectator is never spoken to");
            var mine=told(p);var theirs=told(q);
            h.assertTrue(mine.stream().anyMatch(x->x.startsWith("From the bed")&&x.contains(p.getName().getString())),"the first hears their own name: "+mine);
            h.assertTrue(theirs.stream().anyMatch(x->x.contains(q.getName().getString()))&&theirs.stream().noneMatch(x->x.contains(p.getName().getString()+". That's")),"the second hears their own, not the first's: "+theirs);
            h.assertTrue((f.own(p,P).getInt("VoiceUsed")&ColdNight.NAME)!=0,"what she used is kept for the account");
            done();h.succeed();
        });
    }

    /**
     * 0.4.756, the rite. A hand on the book opens it and the priest kneels by the bed. A reader kneeling with their eyes on the book reads,
     * and the pages turn and are said to them; a spectator kneeling there reads nothing. When she speaks to the reader, they lose their
     * place and read again only after looking back at the page. Nobody reading, the cold rises: breath, frost short of frozen, and a hurt
     * that never takes the last three hearts. The flask holds six. A second reader kneeling at the bedside holds her, and the risen bed
     * comes down under them. At the eleventh page the priest is found in the chair; the twelfth page is blank, and reading stops there.
     */
    @GameTest(template="empty",batch="cold_room_rite",timeoutTicks=2400)
    public static void theRiteItsPagesTheColdTheWaterAndThePriest(GameTestHelper h){
        var f=place(h);var p=f.player("cold_reads",P);var q=f.player("cold_holds",P);var l=f.level(P);var b=f.base(P);
        net.minecraft.server.level.ServerPlayer[] s={null};float[] progress={0};
        h.startSequence().thenIdle(5).thenExecute(()->{
            ColdRoom.ensure(l,b);ColdRoom.arrive(p,b,f.own(p,P));ColdRoom.arrive(q,b,f.own(q,P));
            p.teleportTo(l,b.getX()-1.5,b.getY()+5,b.getZ()-16.5,90,20);q.teleportTo(l,b.getX()-6.5,b.getY(),b.getZ()-5.5,0,0);
        }).thenWaitUntil(()->h.assertTrue(ColdRoom.find(l,b,ColdRoom.PRIEST)!=null,"the priest is in the house")).thenExecute(()->{
            p.teleportTo(l,b.getX()-1.5,b.getY()+5,b.getZ()-16.5,90,20);p.setShiftKeyDown(true);told(p);
            f.click(p,P,ColdSite.BOOK);
            var r=ColdNight.rite(b);
            h.assertTrue(r!=null&&r.on&&stage(l,b,ColdSite.BOOK)==1,"a hand on the book opens it and the rite begins: "+(r==null?"no night":r.on+" "+stage(l,b,ColdSite.BOOK))+" at "+p.position().subtract(b.getX(),b.getY(),b.getZ())+" "+told(p));
            var priest=ColdRoom.find(l,b,ColdRoom.PRIEST);
            h.assertTrue(priest.getPose()==Pose.CROUCHING&&priest.blockPosition().equals(b.offset(ColdRite.KNEEL)),"the priest kneels by the bed: "+priest.blockPosition().subtract(b));
            for(int i=0;i<ColdRite.PAGE/20;i++)ColdNight.riteNow(l,b);
            h.assertTrue(r.page>=1&&f.own(p,P).getInt("Pages")>=1,"kneeling with eyes on the book, a page is read: "+r.page);
            h.assertTrue(told(p).stream().anyMatch(x->x.startsWith("The priest reads, and you read with him")),"the page is said to the reader");
            s[0]=NativeTestPlayers.survival(h,"cold_kneels");f.players.add(s[0]);s[0].setGameMode(GameType.SPECTATOR);
            s[0].teleportTo(l,b.getX()-1.5,b.getY()+5,b.getZ()-15.5,90,20);s[0].setShiftKeyDown(true);
        }).thenIdle(2).thenExecute(()->{
            var r=ColdNight.rite(b);
            h.assertFalse(ColdRite.reads(ColdNight.night(b),s[0],b),"a spectator kneeling at the book reads nothing");
            ColdNight.voiceNow(l,b);progress[0]=(float)r.progress;ColdNight.riteNow(l,b);
            h.assertTrue(r.progress==progress[0],"spoken to, the reader has lost their place: "+progress[0]+" "+r.progress);
            h.assertTrue(told(p).contains(ColdTexts.LOST_PLACE),"and is told so");
        }).thenIdle(ColdRite.INTERRUPT+5).thenExecute(()->{
            var r=ColdNight.rite(b);progress[0]=(float)r.progress;ColdNight.riteNow(l,b);
            h.assertTrue(r.progress>progress[0],"looking back at the page after a moment, they read again");
            p.setShiftKeyDown(false);double before=r.cold;
            for(int i=0;i<10;i++)ColdNight.riteNow(l,b);
            h.assertTrue(r.cold>before+20,"nobody reading, the cold rises: "+before+" > "+r.cold);
            h.assertTrue(p.getTicksFrozen()>0&&p.getTicksFrozen()<p.getTicksRequiredToFreeze(),"the frost comes, short of frozen: "+p.getTicksFrozen());
            r.cold=100;p.setHealth(8);
        }).thenIdle(220).thenExecute(()->{
            h.assertTrue(p.getHealth()>=6&&p.getHealth()<8,"the worst of the cold hurts, never past the last three hearts: "+p.getHealth());
            h.assertTrue(p.getTicksFrozen()<p.getTicksRequiredToFreeze(),"and never freezes anyone through");
            p.teleportTo(l,b.getX()+2.5,b.getY()+5,b.getZ()-20.5,0,30);
            for(int i=0;i<ColdRite.WATER;i++)f.click(p,P,ColdSite.FLASK_TABLE);
            var r=ColdNight.rite(b);
            h.assertTrue(r.water==0&&ColdRite.quiet(ColdNight.night(b))&&f.own(p,P).getInt("Sprinkled")==ColdRite.WATER,"the flask holds six, and the water quiets her: "+r.water+" "+f.own(p,P).getInt("Sprinkled")+" "+told(p));
            told(p);f.click(p,P,ColdSite.FLASK_TABLE);
            h.assertTrue(told(p).contains(ColdTexts.FLASK_EMPTY),"then it is empty");
            q.teleportTo(l,b.getX()+1.3,b.getY()+5,b.getZ()-16.4,90,30);q.setShiftKeyDown(true);
            for(var at:List.of(ColdSite.BED_HEAD,ColdSite.BED_FOOT)){var st=l.getBlockState(b.offset(at));l.setBlock(b.offset(at),st.setValue(ColdBlock.STAGE,st.getValue(ColdBlock.STAGE)%2+2),3);}
        }).thenIdle(2).thenExecute(()->{
            ColdNight.riteNow(l,b);
            h.assertTrue(ColdRite.holds(q,b)&&stage(l,b,ColdSite.BED_HEAD)==0&&f.own(q,P).getInt("HeldSeconds")>=1,"kneeling at the bedside holds her, and the bed comes down under them");
            for(int i=0;i<4;i++)h.assertFalse(ColdNight.stirNow(l,b)&&stage(l,b,ColdSite.BED_HEAD)>=2,"while she is held, the bed does not rise");
            var r=ColdNight.rite(b);r.progress=ColdRite.PAGE*(ColdRite.PRIEST_GIVES_OUT)-1;r.page=ColdRite.PRIEST_GIVES_OUT-1;r.quietUntil=-1;
            p.setShiftKeyDown(true);p.teleportTo(l,b.getX()-1.5,b.getY()+5,b.getZ()-16.5,90,20);
        }).thenIdle(2).thenExecute(()->{
            var r=ColdNight.rite(b);r.lostClear(p);told(p);ColdNight.riteNow(l,b);
            h.assertTrue(r.page==ColdRite.PRIEST_GIVES_OUT&&r.priest==ColdRite.DOWN,"at the eleventh page the priest gives out: "+r.page);
            h.assertTrue(ActorSeat.of(ColdRoom.find(l,b,ColdRoom.PRIEST))!=null,"he is found in the chair by the bed");
            h.assertTrue(told(p).contains(ColdTexts.PRIEST_DOWN),"and the reader is told");
            for(int i=0;i<ColdRite.PAGE/10;i++){r.lostClear(p);ColdNight.riteNow(l,b);}
            h.assertTrue(r.page==ColdRite.PAGES&&told(p).contains(ColdTexts.LAST_PAGE),"the twelfth page is blank: "+r.page);
            for(int i=0;i<10;i++)ColdNight.riteNow(l,b);
            h.assertTrue(r.page==ColdRite.PAGES,"and the reading stops there");
            done();h.succeed();
        });
    }
    private static String text(net.minecraft.world.item.ItemStack book){
        var c=book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT);var out=new StringBuilder();
        if(c!=null)for(var page:c.pages())out.append(page.raw().getString()).append(' ');
        return out.toString().replace('\n',' ');
    }
    private static void kneel(ServerPlayer p,ServerLevel l,BlockPos b,double z){p.teleportTo(l,b.getX()-1.5,b.getY()+5,b.getZ()+z,90,20);p.setShiftKeyDown(true);}
    private static void toPage(ServerLevel l,BlockPos b,int page,ServerPlayer... readers){
        var r=ColdNight.rite(b);r.page=page-1;r.progress=ColdRite.PAGE*(page-1);r.quietUntil=-1;
        for(int i=0;i<ColdRite.PAGE/10&&r.page<page;i++){for(var p:readers)r.lostClear(p);ColdNight.riteNow(l,b);}
    }
    private static boolean in(ServerPlayer p,BlockPos b,ColdSite.Area a){return a.contains(p.position().subtract(b.getX(),b.getY(),b.getZ()));}

    /**
     * 0.4.757, the offer and the one who is taken. From the ninth page she makes each reader in the bedroom an offer from their own
     * records: to the first their lost companion, by name; to the second, who has lost nothing the House saw, the way home. The second
     * answers her: they are sent down to the hall, and read no more that night. The first keeps reading. At the blank last page the first,
     * within reach of the bed, is asked; the second, who answered, is not, and a spectator is never asked. The first says it: they are
     * taken, at the window, at the foot of the steps, and back in the kitchen; they took her place. The one who answered is not finished.
     * The account says what the taker did.
     */
    @GameTest(template="empty",batch="cold_room_offer",timeoutTicks=2400)
    public static void theOfferAnsweredAndTheOneWhoIsTaken(GameTestHelper h){
        var f=place(h);var p=f.player("cold_keeps",P);var q=f.player("cold_answers",P);var l=f.level(P);var b=f.base(P);
        ServerPlayer[] s={null};
        h.startSequence().thenIdle(5).thenExecute(()->{
            ColdRoom.ensure(l,b);ColdRoom.arrive(p,b,f.own(p,P));ColdRoom.arrive(q,b,f.own(q,P));
        }).thenWaitUntil(()->h.assertTrue(ColdRoom.find(l,b,ColdRoom.PRIEST)!=null,"the priest is in the house")).thenExecute(()->{
            var d=f.data();var rec=HotelVignette.personal(d,p.getUUID());var lost=new net.minecraft.nbt.ListTag();var pet=new net.minecraft.nbt.CompoundTag();
            pet.putString("Name","Biscuit");pet.putString("Message","Biscuit was slain by Zombie");lost.add(pet);rec.put("LostPets",lost);HotelVignette.save(d,p.getUUID(),rec);
            kneel(p,l,b,-16.5);kneel(q,l,b,-17.5);
            f.click(p,P,ColdSite.BOOK);
            s[0]=NativeTestPlayers.survival(h,"cold_offer_watch");f.players.add(s[0]);s[0].setGameMode(GameType.SPECTATOR);s[0].teleportTo(l,b.getX()+.5,b.getY()+5,b.getZ()-15.5,90,20);
        }).thenIdle(2).thenExecute(()->{
            toPage(l,b,ColdEnd.OFFER_PAGE,p,q);
            var r=ColdNight.rite(b);
            h.assertTrue(r.page==ColdEnd.OFFER_PAGE,"the ninth page is read: "+r.page);
            h.assertTrue(Talk.waiting(p,ColdEnd.OFFER)&&Talk.waiting(q,ColdEnd.OFFER),"at the ninth page she makes each reader in the room an offer");
            h.assertFalse(Talk.waiting(s[0],ColdEnd.OFFER),"a spectator is offered nothing");
            h.assertTrue(f.own(p,P).getInt("Offered")==ColdEnd.COMPANION&&f.own(p,P).getString("OfferedWhat").equals("Biscuit")&&Talk.lines(p).stream().anyMatch(x->x.contains("Biscuit")),"to the first, the companion the House saw them lose, by name: "+Talk.lines(p));
            h.assertTrue(f.own(q,P).getInt("Offered")==ColdEnd.HOME&&Talk.lines(q).stream().noneMatch(x->x.contains("Biscuit")),"to the second, who lost nothing the House saw, only the way home, and nothing of the first's");
            h.assertTrue(Talk.replies(p).equals(List.of(ColdTexts.ANSWER,ColdTexts.KEEP_READING)),"she can be answered, or the reading kept: "+Talk.replies(p));
            Talk.choose(q,ColdTexts.ANSWER);Talk.choose(p,ColdTexts.KEEP_READING);
            h.assertTrue(r.answered(q)&&f.own(q,P).getInt("Answered")==1&&in(q,b,ColdSite.HALL),"the one who answered is sent down to the hall, by the door: "+q.position().subtract(b.getX(),b.getY(),b.getZ()));
            h.assertTrue(f.own(p,P).getBoolean("KeptReading")&&!r.answered(p),"the other keeps reading");
            kneel(q,l,b,-17.5);
        }).thenIdle(2).thenExecute(()->{
            var n=ColdNight.night(b);var r=ColdNight.rite(b);r.lostClear(q);
            h.assertFalse(ColdRite.reads(n,q,b),"the one who answered reads no more that night, even kneeling at the book");
            toPage(l,b,ColdRite.PAGES,p);
            h.assertTrue(r.page==ColdRite.PAGES,"the last page is reached: "+r.page);
            p.teleportTo(l,b.getX()+1.5,b.getY()+5,b.getZ()-16.5,90,30);q.teleportTo(l,b.getX()+1.5,b.getY()+5,b.getZ()-17.8,90,30);s[0].teleportTo(l,b.getX()+1.5,b.getY()+5,b.getZ()-15.6,90,30);
        }).thenIdle(2).thenExecute(()->{
            ColdNight.riteNow(l,b);
            h.assertTrue(Talk.waiting(p,ColdEnd.LAST)&&Talk.replies(p).equals(List.of(ColdTexts.TAKE_ME,ColdTexts.SAY_NOTHING)),"within reach of the bed, the reader is asked to say what the book does not: "+Talk.replies(p));
            h.assertFalse(Talk.waiting(q,ColdEnd.LAST),"the one who answered her is not asked");
            h.assertFalse(Talk.waiting(s[0],ColdEnd.LAST),"nor is a spectator");
            Talk.choose(p,ColdTexts.TAKE_ME);
            var r=ColdNight.rite(b);
            h.assertTrue(r.over()&&p.getUUID().equals(r.taken)&&r.cold==0,"the first to say it is taken, and the cold goes");
            var own=f.own(p,P);
            h.assertTrue(own.getBoolean("TookHerPlace")&&own.getBoolean("Ready")&&own.getString("Outcome").equals("took_her_place"),"they took her place: "+own.getString("Outcome"));
            h.assertFalse(f.own(q,P).getBoolean("Ready"),"the one who answered her is not finished");
            h.assertFalse(f.own(s[0],P).getBoolean("Ready"),"nor is the spectator");
        }).thenIdle(ColdEnd.AT_WINDOW+3).thenExecute(()->{
            h.assertTrue(in(p,b,ColdSite.BEDROOM)&&p.getZ()-b.getZ()<-19,"they are at the window: "+p.position().subtract(b.getX(),b.getY(),b.getZ()));
        }).thenIdle(ColdEnd.AT_FOOT-ColdEnd.AT_WINDOW).thenExecute(()->{
            h.assertTrue(Math.abs(p.getY()-b.getY()-ColdSite.FOOT_Y)<1.01&&p.getZ()-b.getZ()<ColdSite.FOOT_Z0+.5,"then at the foot of the steps: "+p.position().subtract(b.getX(),b.getY(),b.getZ()));
        }).thenIdle(ColdEnd.IN_KITCHEN-ColdEnd.AT_FOOT+2).thenExecute(()->{
            h.assertTrue(in(p,b,ColdSite.KITCHEN),"and back in the kitchen: "+p.position().subtract(b.getX(),b.getY(),b.getZ()));
            f.click(p,P,ColdSite.KITCHEN_TABLE);
            h.assertTrue(f.own(p,P).contains("Original_Ending"),"the account waits on the kitchen table");
            String account=text(ColdTexts.account(p.getName().getString(),f.own(p,P)));
            h.assertTrue(account.contains("I said what goes there")&&account.contains("Biscuit")&&account.contains("I kept reading"),"the account says what the taker did: "+account);
            done();h.succeed();
        });
    }

    /**
     * 0.4.757, nobody says it. Two readers read to the last page and are both asked; both say nothing (a spectator is never asked). At
     * dawn she sleeps, the cold goes and her mother comes up to the landing, and stays there (one of her, however often the house is
     * looked after). Both saw the morning; each account names the other reader and how it ended.
     */
    @GameTest(template="empty",batch="cold_room_dawn",timeoutTicks=1600)
    public static void nobodySaysItAndTheMorningComes(GameTestHelper h){
        var f=place(h);var p=f.player("cold_dawn_one",P);var q=f.player("cold_dawn_two",P);var l=f.level(P);var b=f.base(P);
        ServerPlayer[] s={null};
        h.startSequence().thenIdle(5).thenExecute(()->{
            ColdRoom.ensure(l,b);ColdRoom.arrive(p,b,f.own(p,P));ColdRoom.arrive(q,b,f.own(q,P));
        }).thenWaitUntil(()->h.assertTrue(ColdRoom.find(l,b,ColdRoom.MOTHER)!=null&&ColdRoom.find(l,b,ColdRoom.PRIEST)!=null,"the mother and the priest are in the house")).thenExecute(()->{
            kneel(p,l,b,-16.5);kneel(q,l,b,-17.5);f.click(p,P,ColdSite.BOOK);
            s[0]=NativeTestPlayers.survival(h,"cold_dawn_watch");f.players.add(s[0]);s[0].setGameMode(GameType.SPECTATOR);
        }).thenIdle(2).thenExecute(()->{
            toPage(l,b,ColdRite.PAGES,p,q);
            for(var x:List.of(p,q))if(Talk.waiting(x,ColdEnd.OFFER))Talk.choose(x,ColdTexts.KEEP_READING);
            h.assertTrue(ColdNight.rite(b).page==ColdRite.PAGES,"the last page is reached");
            p.teleportTo(l,b.getX()+1.5,b.getY()+5,b.getZ()-16.2,90,30);q.teleportTo(l,b.getX()+1.5,b.getY()+5,b.getZ()-17.8,90,30);s[0].teleportTo(l,b.getX()+1.5,b.getY()+5,b.getZ()-15.6,90,30);
        }).thenIdle(2).thenExecute(()->{
            ColdNight.riteNow(l,b);
            h.assertTrue(Talk.waiting(p,ColdEnd.LAST)&&Talk.waiting(q,ColdEnd.LAST),"both are asked");
            h.assertFalse(Talk.waiting(s[0],ColdEnd.LAST),"the spectator is not");
            Talk.choose(p,ColdTexts.SAY_NOTHING);Talk.choose(q,ColdTexts.SAY_NOTHING);
            h.assertFalse(ColdNight.rite(b).over(),"saying nothing ends nothing");
            ColdNight.riteNow(l,b);
            h.assertFalse(Talk.waiting(p,ColdEnd.LAST),"a reader who said nothing is not asked again");
            ColdEnd.dawnNow(l,b);
            var r=ColdNight.rite(b);
            h.assertTrue(r.over()&&ColdNight.dawn(b)&&r.cold==0,"the morning comes, and the cold goes");
            var mother=ColdRoom.find(l,b,ColdRoom.MOTHER);
            h.assertTrue(mother!=null&&mother.blockPosition().equals(b.offset(ColdEnd.LANDING)),"her mother has come up to the landing: "+(mother==null?"gone":mother.blockPosition().subtract(b)));
            ColdRoom.ensure(l,b);ColdRoom.arrive(p,b,f.own(p,P));
            int mothers=l.getEntitiesOfClass(LiteraryActor.class,new AABB(b).inflate(64),a->ColdRoom.is(a)&&ColdRoom.which(a)==ColdRoom.MOTHER).size();
            h.assertTrue(mothers==1&&ColdRoom.find(l,b,ColdRoom.MOTHER).blockPosition().equals(b.offset(ColdEnd.LANDING)),"and stays there, one of her: "+mothers);
            for(var x:List.of(p,q)){var own=f.own(x,P);h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals("saw_the_morning"),x.getName().getString()+" saw the morning: "+own.getString("Outcome"));}
            h.assertFalse(f.own(s[0],P).getBoolean("Ready"),"the spectator is not finished");
            String one=text(ColdTexts.account(p.getName().getString(),f.own(p,P)));
            h.assertTrue(one.contains(q.getName().getString())&&one.contains("nobody said anything"),"the account names the other reader and how it ended: "+one);
            h.assertFalse(ColdNight.stirNow(l,b),"after the morning nothing in the room moves");
            done();h.succeed();
        });
    }
}
