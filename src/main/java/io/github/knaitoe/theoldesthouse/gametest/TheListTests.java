package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.labyrinth.TheListBlock.Kind;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.699, The list (docs/THE_LIST_0_4_699.md), part 1a: the middle of Lamesa at night, the place alone. Each case builds the place
 * itself, one case to a batch, as Fallow's cases do.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_the_list") @PrefixGameTestTemplate(false)
public final class TheListTests {
    private TheListTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.THE_LIST;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture place(GameTestHelper h){done();active=new LiteraryTests.Fixture(h);return active;}
    private static void done(){if(active!=null){active.close();active=null;}}
    private static boolean is(ServerLevel l,BlockPos b,BlockPos at,net.minecraft.world.level.block.Block block){return l.getBlockState(b.offset(at)).is(block);}
    private static boolean mat(ServerLevel l,BlockPos b,int x,int y,int z,String name){return is(l,b,new BlockPos(x,y,z),TheListRegistry.material(name));}
    private static boolean fitting(ServerLevel l,BlockPos b,BlockPos at,Kind kind){return TheListBlock.is(l.getBlockState(b.offset(at)))&&l.getBlockState(b.offset(at)).getValue(TheListBlock.KIND)==kind;}
    private static boolean fitting(ServerLevel l,BlockPos b,BlockPos at,Kind kind,int stage){return fitting(l,b,at,kind)&&l.getBlockState(b.offset(at)).getValue(TheListBlock.STAGE)==stage;}
    private static int stage(ServerLevel l,BlockPos b,BlockPos at){return l.getBlockState(b.offset(at)).getValue(TheListBlock.STAGE);}
    private static boolean shut(ServerLevel l,BlockPos b,BlockPos door){var s=l.getBlockState(b.offset(door));return s.getBlock() instanceof DoorBlock&&!s.getValue(DoorBlock.OPEN);}
    private static void open(ServerLevel l,BlockPos b,BlockPos door,boolean open){var at=b.offset(door);l.setBlock(at,l.getBlockState(at).setValue(DoorBlock.OPEN,open),2);l.setBlock(at.above(),l.getBlockState(at.above()).setValue(DoorBlock.OPEN,open),2);}
    /** The grey lines the reader has been shown since last asked: the action bar, either way the server sends it. */
    private static List<String> cues(ServerPlayer p){
        NativeTestPlayers.flush(p);
        var out=new ArrayList<String>();
        for(var packet:NativeTestPlayers.packets(p)){
            if(packet instanceof ClientboundSetActionBarTextPacket bar)out.add(bar.text().getString());
            if(packet instanceof ClientboundSystemChatPacket chat&&chat.overlay())out.add(chat.content().getString());
        }
        return out;
    }
    private static boolean heard(ServerPlayer p,net.minecraft.sounds.SoundEvent sound){
        NativeTestPlayers.flush(p);
        for(var packet:NativeTestPlayers.packets(p))if(packet instanceof ClientboundSoundPacket s&&s.getSound().value()==sound)return true;
        return false;
    }
    /** A hand on a thing, the reader stood at a spot of their own near it, as a client would send it. */
    private static void use(ServerPlayer p,BlockPos b,BlockPos rel,BlockPos stand){
        var at=b.offset(rel);
        p.moveTo(b.getX()+stand.getX()+.5,b.getY()+stand.getY(),b.getZ()+stand.getZ()+.5);p.setDeltaMovement(Vec3.ZERO);
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.UP,at,false)));
    }
    private static final double[][] OFFSETS={{0,0},{.2,0},{-.2,0},{0,.2},{0,-.2}};
    /** Where a body's feet rest in a cell, or NaN: on what is under it when there is room above, else on top of what stands in it. */
    private static double feet(ServerLevel l,BlockPos b,BlockPos cell){
        var at=b.offset(cell);var here=l.getBlockState(at).getCollisionShape(l,at);var below=l.getBlockState(at.below()).getCollisionShape(l,at.below());
        if(!below.isEmpty()){
            double f=at.getY()-1+below.max(Direction.Axis.Y);
            if(f>=at.getY()-.51&&f<=at.getY()+.01)for(var o:OFFSETS){
                var body=new AABB(at.getX()+.2+o[0],f+.01,at.getZ()+.2+o[1],at.getX()+.8+o[0],f+1.79,at.getZ()+.8+o[1]);
                if(l.noCollision(body))return f;
            }
        }
        if(here.isEmpty())return Double.NaN;
        double top=here.max(Direction.Axis.Y);if(top>1.01)return Double.NaN;
        double f=at.getY()+top;
        var body=new AABB(at.getX()+.2,f+.01,at.getZ()+.2,at.getX()+.8,f+1.79,at.getZ()+.8);
        return l.noCollision(body)?f:Double.NaN;
    }
    /** Every cell a body can walk to from a start without opening anything, up or down a block at a time, within a box. */
    private static Set<BlockPos> walked(ServerLevel l,BlockPos b,BlockPos from,int x0,int z0,int x1,int z1){
        var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();seen.add(from);queue.add(from);
        while(!queue.isEmpty()){
            var at=queue.poll();double f=feet(l,b,at);if(Double.isNaN(f))continue;
            for(var d:Direction.Plane.HORIZONTAL)for(int dy=-1;dy<=1;dy++){
                var to=at.relative(d).above(dy);
                if(seen.contains(to)||to.getX()<x0||to.getX()>x1||to.getZ()<z0||to.getZ()>z1||to.getY()<-2||to.getY()>3)continue;
                double g=feet(l,b,to);if(Double.isNaN(g)||g-f>1.01)continue;
                var a=b.offset(at);var c=b.offset(to);double y=Math.max(f,g);
                var edge=new AABB((a.getX()+c.getX())/2.0+.2,y+.5,(a.getZ()+c.getZ())/2.0+.2,(a.getX()+c.getX())/2.0+.8,y+1.79,(a.getZ()+c.getZ())/2.0+.8);
                if(!l.noCollision(edge))continue;
                seen.add(to);queue.add(to);
            }
        }
        seen.removeIf(c->Double.isNaN(feet(l,b,c)));
        return seen;
    }

    /** Which cells on the way from the front room to South Ninth a walk reached, and what stands in each, for a failure's message. */
    private static String path(Set<BlockPos> out,ServerLevel l,BlockPos b){
        var sb=new StringBuilder();
        for(int z=-16;z>=-27;z--){var c=new BlockPos(5,0,z);sb.append(z).append(out.contains(c)?"+":"-").append(l.getBlockState(b.offset(c)).getBlock().getName().getString()).append('/').append(l.getBlockState(b.offset(c).below()).getBlock().getName().getString()).append(' ');}
        return sb.toString();
    }
    /**
     * Lamesa at midnight, built of its own things. The reader's house: brick at the foot of its walls and siding above, lino in the kitchen,
     * carpet in the rooms, a ceiling, a roof of shingle slabs; the telephone where the first paper is, the desk where the account will be,
     * the bed, its doors. South Ninth in caliche, Avenue K paved with its line, Main Street with its line, curbs and sidewalks. The stores'
     * names read left to right from the street, the drugstore's back shelves inside its back wall, the video store a room deep with its
     * horror wall, its television on the east wall and the return slot in a pier by the door; the substation with no awning under its low
     * sign. The drive-in's sign, the tracks with caliche where the gin road crosses and crossbucks by it; the gin, its modules, the seed
     * house; the water tower with LAMESA read left to right on two faces and a ladder up a leg through a gap in the catwalk's rail; the playa,
     * Main Street running on across it. Cotton to every edge.
     *
     * A body walks from the House's door through the kitchen, the hall and the front room, but not into the reader's room or out of the
     * front door while they are shut. Out of the front door it walks to South Ninth, the empty house's step, Main Street, the drive-in's lot,
     * the gin road and its crossing, the gin yard, the foot of the tower and the playa; not into a neighbor's house or a store. It is midnight;
     * the House does not deal it in this part; the account's title fits.
     */
    @GameTest(template="empty",batch="the_list_place",timeoutTicks=2400)
    public static void lamesaAtMidnight(GameTestHelper h){
        var f=place(h);var p=f.player("list_reader",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            // The house.
            h.assertTrue(mat(l,b,TheListSite.HOUSE_W,0,-6,"wainscot")&&mat(l,b,TheListSite.HOUSE_W,1,-6,"siding")&&mat(l,b,-5,-1,-5,"lino")&&mat(l,b,6,-1,-4,"carpet")
                &&mat(l,b,0,TheListSite.CEILING,-5,"ceiling"),"the house: brick at the foot, siding, lino in the kitchen, carpet, a ceiling");
            var ridge=l.getBlockState(b.offset(0,TheListSite.CEILING+1,TheListSite.HOUSE_N-1));
            h.assertTrue(ridge.is(TheListRegistry.SHINGLE_SLAB.get())&&ridge.getValue(SlabBlock.TYPE)==SlabType.BOTTOM,"the roof's eave in shingle slabs: "+ridge);
            h.assertTrue(fitting(l,b,TheListSite.SOURCE,Kind.KITCHEN,6)&&LiteraryRooms.source(P).equals(TheListSite.SOURCE),"the telephone on the kitchen wall is the first paper");
            h.assertTrue(fitting(l,b,TheListSite.ENDING,Kind.ROOM,1)&&LiteraryRooms.ending(P).equals(TheListSite.ENDING),"the desk where the account will be");
            h.assertTrue(fitting(l,b,TheListSite.BED_HEAD,Kind.BED,0)&&fitting(l,b,TheListSite.BED_FOOT,Kind.BED,1),"the reader's bed");
            h.assertTrue(shut(l,b,TheListSite.ROOM_DOOR)&&shut(l,b,TheListSite.FRONT_DOOR)&&is(l,b,TheListSite.FRONT_DOOR,TheListRegistry.HOUSE_DOOR.get()),"the reader's door and the front door, shut");
            // The streets.
            h.assertTrue(mat(l,b,0,-1,-28,"caliche")&&mat(l,b,20,-1,-50,"asphalt")&&mat(l,b,TheListSite.AVE_LINE,-1,-48,"main_line"),"South Ninth in caliche, Avenue K paved with its line");
            h.assertTrue(mat(l,b,1,-1,-115,"asphalt")&&mat(l,b,0,-1,TheListSite.MAIN_LINE,"main_line")&&mat(l,b,0,-1,TheListSite.CURB_S,"curb")&&mat(l,b,0,-1,-111,"sidewalk"),"Main Street, its line, curb and sidewalk");
            // The stores, read from the street: the viewer's left first.
            for(int i=0;i<4;i++)h.assertTrue(fitting(l,b,new BlockPos(-9-i,4,TheListSite.MAIN_FRONT_S-1),Kind.SIGN_DRUG,i),"Dawson Drug's name, piece "+i);
            for(int i=0;i<5;i++)h.assertTrue(fitting(l,b,new BlockPos(-8+i,4,TheListSite.MAIN_FRONT_N+1),Kind.SIGN_VIDEO,i),"the video store's name, piece "+i);
            for(int x=-17;x<=-5;x++)if(x!=-6)h.assertTrue(fitting(l,b,new BlockPos(x,0,TheListSite.MAIN_BACK_S-1),Kind.DRUG,4),"the drugstore's back shelves inside its back wall at "+x);
            h.assertTrue(fitting(l,b,new BlockPos(-6,0,TheListSite.MAIN_BACK_S-3),Kind.DRUG,2)&&fitting(l,b,new BlockPos(-5,0,TheListSite.MAIN_BACK_S-3),Kind.DRUG,3),"the counter and register before them");
            h.assertTrue(mat(l,b,-6,0,TheListSite.MAIN_BACK_N,"cinder")&&fitting(l,b,new BlockPos(-13,0,-130),Kind.VIDEO,0)&&fitting(l,b,new BlockPos(1,0,-130),Kind.VIDEO,1),"the video store a room deep, the horror wall and new releases");
            h.assertTrue(fitting(l,b,new BlockPos(1,2,-130),Kind.VIDEO,7)&&l.getBlockState(b.offset(1,2,-130)).getValue(TheListBlock.FACING)==Direction.WEST,"its television on the east wall");
            h.assertTrue(fitting(l,b,new BlockPos(-5,1,TheListSite.MAIN_FRONT_N),Kind.FRONT,5)&&fitting(l,b,new BlockPos(-5,1,TheListSite.MAIN_FRONT_N+1),Kind.VIDEO,5),"the return slot in a pier by the door");
            h.assertTrue(fitting(l,b,new BlockPos(-30,3,TheListSite.MAIN_FRONT_S-1),Kind.FRONT,2)&&l.getBlockState(b.offset(31,3,TheListSite.MAIN_FRONT_S-1)).isAir()
                &&fitting(l,b,new BlockPos(37,3,TheListSite.MAIN_FRONT_S-1),Kind.SIGN_LAW,0),"the feed store's awning; none under the substation's low sign");
            // The drive-in's sign, the tracks.
            for(int col=0;col<3;col++)h.assertTrue(fitting(l,b,new BlockPos(27+col,7,TheListSite.WALK_N_N-1),Kind.SIGN_DRIVEIN,1+col)&&fitting(l,b,new BlockPos(27+col,6,TheListSite.WALK_N_N-1),Kind.SIGN_DRIVEIN,4+col),"the drive-in's sign, column "+col);
            h.assertTrue(fitting(l,b,new BlockPos(TheListSite.TRACK_X,0,-60),Kind.TRACK)&&mat(l,b,TheListSite.TRACK_X+1,-1,-60,"ballast")&&mat(l,b,TheListSite.TRACK_X+1,-1,-28,"caliche")
                &&fitting(l,b,new BlockPos(TheListSite.TRACK_W-1,0,TheListSite.NINTH_S),Kind.SIGNPOST,5)&&fitting(l,b,new BlockPos(TheListSite.TRACK_E+1,0,TheListSite.NINTH_N),Kind.SIGNPOST,5),"the rail on its ballast, caliche at the gin road's crossing and a crossbuck either side");
            // The gin, its yard, the seed house, the tower.
            var wall=b.offset(TheListSite.GIN_E,3,-50);
            h.assertTrue(l.getBlockState(wall).is(TheListRegistry.material("galvanized"))||l.getBlockState(wall).is(TheListRegistry.material("rusted")),"the gin's iron walls");
            h.assertTrue(fitting(l,b,new BlockPos(TheListSite.GIN_W+1,0,TheListSite.MODULES_S-2),Kind.BALE,3)&&fitting(l,b,new BlockPos(TheListSite.GIN_W+4,0,TheListSite.GIN_N+3),Kind.GIN,4)
                &&!fitting(l,b,new BlockPos(TheListSite.GIN_W+4,2,TheListSite.GIN_N+3),Kind.GIN),"a module's end in the yard; the press, once");
            h.assertTrue(mat(l,b,TheListSite.SEED_W,3,-80,"galvanized")&&fitting(l,b,new BlockPos(TheListSite.SEED_E+1,4,TheListSite.SEED_S-3),Kind.SEED,3),"the seed house and its ladder");
            int cx=TheListSite.TOWER_X,cz=TheListSite.TOWER_Z;
            for(int i=0;i<3;i++)h.assertTrue(fitting(l,b,new BlockPos(cx-2,26,cz-1+i),Kind.TANK,1+i)&&fitting(l,b,new BlockPos(cx-1+i,26,cz+2),Kind.TANK,1+i),"LAMESA on the tank, piece "+i);
            for(int y=0;y<=TheListSite.CATWALK_Y;y++)h.assertTrue(fitting(l,b,new BlockPos(cx+2,y,cz+3),Kind.TOWER,2),"the ladder up the leg at "+y);
            h.assertTrue(fitting(l,b,new BlockPos(cx+2,0,cz+2),Kind.TOWER)&&l.getBlockState(b.offset(cx+2,TheListSite.CATWALK_Y+1,cz+3)).isAir()&&fitting(l,b,new BlockPos(cx+1,TheListSite.CATWALK_Y+1,cz+3),Kind.TOWER,4),"on a leg, through a gap in the catwalk's rail");
            h.assertTrue(mat(l,b,80,-1,-150,"playa")&&mat(l,b,81,-1,-116,"asphalt"),"the playa, Main Street on across it");
            for(var at:List.of(new BlockPos(TheListSite.WEST,0,-40),new BlockPos(TheListSite.EAST,0,10),new BlockPos(-20,0,TheListSite.NORTH),new BlockPos(-40,0,TheListSite.SOUTH)))
                h.assertTrue(fitting(l,b,at,Kind.COTTON)&&mat(l,b,at.getX(),-1,at.getZ(),"soil"),"cotton to the edge, at "+at.toShortString());
            // The neighbors.
            // Each house its own: lap siding, face brick to the eaves, or stucco. This one is face brick, its door shut in the wall's outer face.
            var front=l.getBlockState(b.offset(-34,0,-37));
            h.assertTrue(mat(l,b,-42,1,-40,"storefront")&&mat(l,b,-42,1,-55,"siding")&&shut(l,b,new BlockPos(-34,0,-37))&&front.getValue(DoorBlock.FACING)==net.minecraft.core.Direction.NORTH
                &&fitting(l,b,new BlockPos(-31,0,TheListSite.NINTH_N-1),Kind.MAILBOX),"a neighbor's house in face brick, the next in siding, its door shut in the outer face, its mailbox at the curb");
            h.assertTrue(f.data().door(OnwardDoors.id(P))!=null||OnwardDoors.install(l,b,P),"the way on goes in where it was placed");
            h.assertTrue(l.getBlockState(b.offset(TheListSite.ONWARD)).getBlock() instanceof DoorBlock,"the empty house's front door is the way on");
            h.assertFalse(fitting(l,b,new BlockPos(1,1,TheListSite.ONWARD.getZ()+1),Kind.YARD),"no light on the empty house's porch");
            // Walking. Inside: the kitchen, the hall and the front room, but not the shut room or out of the shut front door.
            var inside=walked(l,b,new BlockPos(0,0,-3),TheListSite.HOUSE_W,TheListSite.HOUSE_N-3,TheListSite.HOUSE_E,0);
            h.assertTrue(inside.contains(new BlockPos(-5,0,-3))&&inside.contains(new BlockPos(0,0,-9))&&inside.contains(new BlockPos(8,0,-14)),"the kitchen, the hall and the front room: "+inside.size());
            h.assertFalse(inside.contains(new BlockPos(8,0,-4)),"not into the reader's room while its door is shut");
            h.assertFalse(inside.stream().anyMatch(c->c.getZ()<=TheListSite.HOUSE_N),"not out of the shut front door");
            open(l,b,TheListSite.ROOM_DOOR,true);
            h.assertTrue(walked(l,b,new BlockPos(0,0,-3),TheListSite.HOUSE_W,TheListSite.HOUSE_N-3,TheListSite.HOUSE_E,0).contains(new BlockPos(9,0,-3)),"into the reader's room by its door, to the desk");
            open(l,b,TheListSite.ROOM_DOOR,false);
            // Out: the town.
            open(l,b,TheListSite.FRONT_DOOR,true);
            var out=walked(l,b,new BlockPos(5,0,-15),TheListSite.WEST,TheListSite.NORTH,TheListSite.EAST,TheListSite.SOUTH);
            for(var at:List.of(new BlockPos(5,0,-28),new BlockPos(0,0,TheListSite.ONWARD.getZ()+2),new BlockPos(0,0,-117),new BlockPos(45,0,-135),new BlockPos(45,0,-28),
                    new BlockPos(TheListSite.TRACK_X+1,0,-28),new BlockPos(82,0,-33),new BlockPos(cx+4,0,cz),new BlockPos(80,0,-150)))
                h.assertTrue(out.contains(at),"out of the front door to "+at.toShortString()+": "+out.size()+" "+path(out,l,b));
            h.assertFalse(out.contains(new BlockPos(-34,0,-42)),"not into a neighbor's house by its shut door");
            h.assertFalse(out.contains(new BlockPos(-11,0,-104))||out.contains(new BlockPos(-8,0,-132)),"not into a store by its shut glass door");
            open(l,b,TheListSite.FRONT_DOOR,false);
            // The hour, the dealing and the title.
            h.assertTrue(SceneClock.at(f.origin,b.offset(0,0,-60),HouseDimensions.OUTSIDE)==9,"midnight in Lamesa");
            h.assertFalse(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P)||TheList.dealable(f.data(),java.util.UUID.randomUUID()),"the House does not deal it until night one");
            h.assertTrue(LiteraryTexts.accountTitle(P).length()<=32,"the account's title fits a book: "+LiteraryTexts.accountTitle(P));
            var paper=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);String text=String.join(" ",paper.pages().stream().map(pg->pg.raw().getString()).toList());
            h.assertTrue(text.contains("Plate in the oven")&&text.contains("Lights off")&&paper.pages().size()==4,"the pad by the phone, four notes: "+text);
            done();h.succeed();
        });
    }

    /**
     * A hand on anything in town finds a line of its own, for that reader alone: the refrigerator, a cotton plant, the letters on the tank
     * from the catwalk. A neighbor's door, either half, is locked, said so, and stays shut; the reader's own front door and the empty
     * house's door are not the town's to answer. A second reader standing by hears none of it.
     */
    @GameTest(template="empty",batch="the_list_hands",timeoutTicks=1200)
    public static void aHandOnAnythingAndTheNeighborsDoors(GameTestHelper h){
        var f=place(h);var p=f.player("list_hand",P);var peer=f.player("list_peer",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            cues(p);cues(peer);
            use(p,b,new BlockPos(-2,0,-2),new BlockPos(-1,0,-3));
            h.assertTrue(cues(p).contains(TheListTexts.thing(Kind.KITCHEN,3)),"the refrigerator");
            var cotton=new BlockPos(-76,0,-60);
            h.assertTrue(fitting(l,b,cotton,Kind.COTTON),"a cotton plant in its row at "+cotton.toShortString());
            use(p,b,cotton,cotton.west());
            h.assertTrue(cues(p).contains(TheListTexts.thing(Kind.COTTON,stage(l,b,cotton))),"the cotton plant, as it stands");
            int cx=TheListSite.TOWER_X,cz=TheListSite.TOWER_Z;
            use(p,b,new BlockPos(cx-2,26,cz),new BlockPos(cx-3,TheListSite.CATWALK_Y,cz));
            h.assertTrue(cues(p).contains("Lamesa, in letters taller than you."),"the letters on the tank, from the catwalk");
            var door=new BlockPos(-34,0,-37);
            use(p,b,door,door.south(2));
            h.assertTrue(cues(p).contains(TheListTexts.SHUT_DOOR)&&shut(l,b,door),"a neighbor's door is locked, and stays shut");
            use(p,b,door.above(),door.south(2));
            h.assertTrue(cues(p).contains(TheListTexts.SHUT_DOOR)&&shut(l,b,door),"its upper half too");
            use(p,b,TheListSite.FRONT_DOOR,TheListSite.FRONT_DOOR.south(3));
            h.assertFalse(cues(p).contains(TheListTexts.SHUT_DOOR),"the reader's own front door opens as a door does");
            h.assertTrue(f.data().door(OnwardDoors.id(P))!=null||OnwardDoors.install(l,b,P),"the way on goes in where it was placed");
            use(p,b,TheListSite.ONWARD,TheListSite.ONWARD.south(2));
            h.assertFalse(cues(p).contains(TheListTexts.SHUT_DOOR),"the empty house's door is the House's way on, not the town's");
            h.assertTrue(cues(peer).isEmpty(),"the other reader heard none of it");
            done();h.succeed();
        });
    }

    /**
     * The town's night, heard and forgotten. Two readers come in through the back door and are counted each once; the night is begun once.
     * A reader on Main Street by the drive-in hears its speaker. When nobody is left in town for a hundred ticks, the night is forgotten.
     */
    @GameTest(template="empty",batch="the_list_night",timeoutTicks=1200)
    public static void theTownsNightIsHeardAndForgotten(GameTestHelper h){
        var f=place(h);var p=f.player("list_night",P);var peer=f.player("list_night_peer",P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            h.assertTrue(TheList.active(b),"the night begins with the first reader in");
            h.assertTrue(f.own(p,P).getInt("Visits")==1&&f.own(peer,P).getInt("Visits")==1,"each reader counted once: "+f.own(p,P).getInt("Visits")+","+f.own(peer,P).getInt("Visits"));
            f.at(p,P,45.5,0,-124.5);f.at(peer,P,0.5,0,-3.5);
            NativeTestPlayers.flush(p);NativeTestPlayers.packets(p);
        }).thenWaitUntil(()->h.assertTrue(heard(p,TheListRegistry.SPEAKER.get()),"the drive-in's speaker, heard on Main Street (clock "+TheList.clock(b)+")"))
        .thenExecute(()->{p.setGameMode(GameType.SPECTATOR);peer.setGameMode(GameType.SPECTATOR);})
        .thenWaitUntil(()->h.assertFalse(TheList.active(b),"the night forgotten once nobody is in town (clock "+TheList.clock(b)+")"))
        .thenExecute(()->{done();h.succeed();});
    }
}
