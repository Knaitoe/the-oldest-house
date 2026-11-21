package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.labyrinth.TheListBlock.Kind;
import io.github.knaitoe.theoldesthouse.network.TheListBodyPayload;
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
 * 0.4.699, The list (docs/THE_LIST_0_4_699.md), part 1a: the middle of Lamesa at night, the place alone; 0.4.700 (docs/THE_LIST_0_4_700.md),
 * part 1b: the school's field, Amber's house and the yards. Each case builds the place
 * itself, one case to a batch, as Fallow's cases do.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_the_list") @PrefixGameTestTemplate(false)
public final class TheListTests {
    private TheListTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.THE_LIST;
    private static LiteraryTests.Fixture active;
    /** Every case shares the one town: a night an earlier case left going (one that failed partway) is ended first. 0.4.707: and the
     * town is held quiet, so that no night begins round the case's readers coming in; a case that wants one lets it go or begins it. */
    private static LiteraryTests.Fixture place(GameTestHelper h){
        done();active=new LiteraryTests.Fixture(h);
        var l=active.level(P);var b=active.base(P);
        if(TheListNight.phase(l.getServer(),b)!=TheListNight.NONE)TheListNight.end(l,b);
        TheListNight.quiet(l.getServer(),b,true);
        return active;
    }
    private static void done(){if(active!=null){active.close();active=null;}}
    /** Run 1730: in the full suite the town's far places (the field, the pasture) were unloaded under a case that needed them. A case
     * holds the ground round each place it uses, and waits for it (f.chunks.ready()). */
    private static void hold(LiteraryTests.Fixture f,BlockPos b,BlockPos... at){var l=f.level(P);for(var a:at)f.chunks.hold(l,new AABB(b.offset(a)).inflate(18));}
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
     * the House deals it only from ten crossings down; the account's title fits.
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
            h.assertFalse(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P)||TheList.dealable(f.data(),java.util.UUID.randomUUID()),"the House deals it only from ten crossings down");
            h.assertTrue(TheList.dealable(TheList.DEPTH,0)&&!TheList.dealable(TheList.DEPTH-1,0)&&!TheList.dealable(TheList.DEPTH+5,1),"from ten down, to a reader who has not had night one");
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

    /**
     * 0.4.700: the school's field, Amber's house and the neighbors' yards. The field lined and mown inside its cinder track, its end zones
     * and goalposts with the crossbar on its gooseneck; the home stands rising in rows with the press box behind them, the visitors' stands
     * across; the lights dark on their towers; the scoreboard read from the field; the field house's steel doors shut. A body walks in at the
     * gate from South Seventh to the track, the field and the top of both stands and into the press box, but not into the field house until
     * a door is opened, and then through it to the coach's office and the showers. Amber's house: face brick, her television and her desk
     * and her window lit, the front door locked and said so for that reader, not walked into. Sprinklers on the lawns, heard by a reader
     * near one.
     */
    @GameTest(template="empty",batch="the_list_field",timeoutTicks=2400)
    public static void theFieldAmbersHouseAndTheYards(GameTestHelper h){
        var f=place(h);var p=f.player("list_field",P);var peer=f.player("list_field_peer",P);var l=f.level(P);var b=f.base(P);
        int fx=TheListSite.FIELD_X,fz=TheListSite.FIELD_Z;
        h.startSequence().thenIdle(5).thenExecute(()->{
            // The field.
            h.assertTrue(mat(l,b,fx,-1,TheListSite.FIELD_N+5,"turf_line")&&mat(l,b,fx,-1,TheListSite.FIELD_S-5,"turf_line")&&mat(l,b,fx,-1,TheListSite.FIELD_N+2,"endzone")
                &&mat(l,b,TheListSite.FIELD_W,-1,fz,"turf_side")&&(mat(l,b,fx,-1,fz+2,"turf")||mat(l,b,fx,-1,fz+2,"turf_dark")),"the field: its lines, an end zone, a sideline, mown turf");
            h.assertTrue(mat(l,b,fx-21,-1,fz,"track_cinder")&&mat(l,b,fx+20,-1,fz,"track_cinder")&&mat(l,b,fx,-1,TheListSite.FIELD_N-8,"track_cinder")&&!mat(l,b,fx-22,-1,fz,"track_cinder"),"the cinder track round it");
            for(int end:new int[]{TheListSite.FIELD_N,TheListSite.FIELD_S}){
                int base=end==TheListSite.FIELD_N?end-1:end+1;
                h.assertTrue(fitting(l,b,new BlockPos(fx,0,base),Kind.FIELD,0)&&fitting(l,b,new BlockPos(fx,3,base),Kind.FIELD,2)&&fitting(l,b,new BlockPos(fx,3,end),Kind.FIELD,3)
                    &&fitting(l,b,new BlockPos(fx-2,3,end),Kind.FIELD,4)&&fitting(l,b,new BlockPos(fx+2,8,end),Kind.FIELD,5),"the goalpost at "+end+": its foot, gooseneck, crossbar and uprights");
            }
            h.assertTrue(fitting(l,b,new BlockPos(fx-24,16,TheListSite.FIELD_N-2),Kind.FIELD,7)&&l.getBlockState(b.offset(fx-24,16,TheListSite.FIELD_N-2)).getLightEmission()==0,"a tower's lights, dark");
            h.assertTrue(fitting(l,b,new BlockPos(fx-23,0,fz),Kind.STANDS,0)&&fitting(l,b,new BlockPos(fx-30,3,fz),Kind.STANDS,1)&&fitting(l,b,new BlockPos(fx-30,4,fz-2),Kind.STANDS,3)
                &&fitting(l,b,new BlockPos(fx-31,5,fz),Kind.STANDS,5)&&fitting(l,b,new BlockPos(fx+26,1,fz),Kind.STANDS,1),"the home stands in rows, their rail, the press box's window; the visitors' stands");
            for(int i=0;i<3;i++)h.assertTrue(fitting(l,b,new BlockPos(fx+1-i,6,TheListSite.FENCE_S-2),Kind.SCORE,i)&&fitting(l,b,new BlockPos(fx+1-i,5,TheListSite.FENCE_S-2),Kind.SCORE,3+i),"the scoreboard read from the field, piece "+i);
            var fieldDoor=new BlockPos(fx-1,0,TheListSite.FIELD_HOUSE_S);
            h.assertTrue(is(l,b,fieldDoor,TheListRegistry.STEEL_DOOR.get())&&shut(l,b,fieldDoor)&&shut(l,b,fieldDoor.east())&&fitting(l,b,new BlockPos(fx-2,0,TheListSite.MAIN_FRONT_S+1),Kind.LOCKER,0),"the field house's doors shut, its lockers");
            // Walking in at the gate.
            var field=walked(l,b,new BlockPos(-80,0,-71),TheListSite.FENCE_W,TheListSite.MAIN_FRONT_S,-60,TheListSite.FENCE_S);
            for(var at:List.of(new BlockPos(fx-21,0,fz),new BlockPos(fx,0,fz),new BlockPos(fx,0,TheListSite.FIELD_N+2),new BlockPos(fx-30,3,fz-4),new BlockPos(fx+26,1,fz)))
                h.assertTrue(field.contains(at),"in at the gate to "+at.toShortString()+": "+field.size());
            h.assertFalse(field.stream().anyMatch(c->TheListSite.inFieldHouse(Vec3.atCenterOf(c))),"not into the field house by its shut doors");
            open(l,b,fieldDoor,true);
            var through=walked(l,b,new BlockPos(fx,0,fz),TheListSite.FENCE_W,TheListSite.MAIN_FRONT_S,-60,TheListSite.FENCE_S);
            h.assertTrue(through.contains(new BlockPos(fx+5,0,TheListSite.MAIN_FRONT_S+3))&&through.contains(new BlockPos(fx+6,0,TheListSite.FIELD_HOUSE_S-1)),"through its door to the coach's office and the showers");
            open(l,b,fieldDoor,false);
            // Amber's house.
            h.assertTrue(mat(l,b,TheListSite.AMBER_W,1,-84,"storefront")&&fitting(l,b,TheListSite.AMBER_TV,Kind.AMBER,0)&&fitting(l,b,TheListSite.AMBER_DESK,Kind.AMBER,3)
                &&fitting(l,b,new BlockPos(TheListSite.AMBER_DESK.getX(),1,TheListSite.AMBER_S),Kind.AMBER,6),"Amber's house in face brick: the television, her desk, her window");
            h.assertTrue(shut(l,b,TheListSite.AMBER_FRONT)&&shut(l,b,TheListSite.AMBER_BACK),"her front and back doors, shut");
            cues(p);cues(peer);
            use(p,b,TheListSite.AMBER_FRONT,TheListSite.AMBER_FRONT.south(2));
            h.assertTrue(cues(p).contains(TheListTexts.AMBER_DOOR)&&shut(l,b,TheListSite.AMBER_FRONT),"her front door is locked, and said so");
            h.assertTrue(cues(peer).isEmpty(),"to that reader alone");
            // 0.4.701 (run 1726): her back gate stands open onto the alley, so her back door opens onto somewhere a reader can stand; a hand
            // swings it to, and open again, said to that reader alone.
            var gate=new BlockPos(0,0,TheListSite.AMBER_N-1);
            h.assertTrue(fitting(l,b,gate,Kind.FENCE,3)&&l.getBlockState(b.offset(gate)).getCollisionShape(l,b.offset(gate)).isEmpty(),"her back gate stands open, nothing in its way");
            h.assertTrue(walked(l,b,gate.north(2),-6,-96,6,-88).contains(gate),"from the alley to her back step");
            use(p,b,gate,gate.north(2));
            h.assertTrue(cues(p).contains(TheListTexts.GATE_SHUT)&&fitting(l,b,gate,Kind.FENCE,2),"a hand swings the gate to");
            h.assertTrue(cues(peer).isEmpty(),"said to that reader alone");
            use(p,b,gate,gate.north(2));
            h.assertTrue(cues(p).contains(TheListTexts.GATE_OPEN)&&fitting(l,b,gate,Kind.FENCE,3),"and open again");
            var yard=walked(l,b,new BlockPos(0,0,-72),-20,-96,20,-70);
            h.assertFalse(yard.stream().anyMatch(c->TheListSite.inAmber(Vec3.atCenterOf(c))),"not into her house");
            // The yards.
            for(int[] at:TheListSite.SPRINKLERS)h.assertTrue(fitting(l,b,new BlockPos(at[0],0,at[1]),Kind.LOT,0),"a sprinkler on a lawn at "+at[0]+","+at[1]);
            f.at(p,P,TheListSite.SPRINKLERS[0][0]+3.5,0,TheListSite.SPRINKLERS[0][1]+.5);
            NativeTestPlayers.flush(p);NativeTestPlayers.packets(p);
        }).thenWaitUntil(()->h.assertTrue(heard(p,TheListRegistry.SPRINKLER.get()),"a sprinkler, heard on the lawn (clock "+TheList.clock(b)+")"))
        .thenExecute(()->{done();h.succeed();});
    }

    private static String said(LiteraryActor a){return a.getCustomName()==null?"":a.getCustomName().getString();}
    /**
     * 0.4.702: the town's people at their places by night, one of each, held where they are: Amber in her desk chair facing her window,
     * Jenna behind the order window, Janice at the return slot, Deek and Wes on the home bench, Mel and Lesley on the top row of the
     * stands, Shannon on the bus bench, Stace on the catwalk at the rail, the deputy outside the substation, and the reader's parents
     * asleep in their bed. Each one's words are each reader's own: a first line, then a second, then the second again; a second reader
     * hears the first line for themselves. A knuckle on Amber's window or the order window is answered through the glass. A hand on
     * the parents is a cue to that reader alone, as is a hand on their bed.
     */
    @GameTest(template="empty",batch="the_list_people",timeoutTicks=2400)
    public static void theTownsPeopleAtTheirPlaces(GameTestHelper h){
        var f=place(h);var p=f.player("list_people",P);var peer=f.player("list_people_peer",P);var l=f.level(P);var b=f.base(P);
        var town=new AABB(b.getX()+TheListSite.WEST,b.getY()-4,b.getZ()+TheListSite.NORTH,b.getX()+TheListSite.EAST+1,b.getY()+40,b.getZ()+TheListSite.SOUTH+1);
        hold(f,b,TheListPeople.AT);
        h.startSequence().thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"waiting for the ground round everyone's place");
            TheListPeople.ensure(l,b);
            for(int who=0;who<TheListPeople.CAST;who++)h.assertTrue(TheListPeople.find(l,b,who)!=null,"waiting for "+who+" at "+TheListPeople.AT[who].toShortString());
        }).thenIdle(20).thenExecute(()->{
            // The town's own pass (every five seconds while anyone is in town): it finds anyone an earlier case left elsewhere in the town.
            TheListPeople.ensure(l,b);
            for(int who=0;who<TheListPeople.CAST;who++){
                int phase=TheListPeople.FIRST+who;
                int n=l.getEntitiesOfClass(LiteraryActor.class,town,a->a.role()==LiteraryActor.STRANGER&&a.phase()==phase).size();
                h.assertTrue(n==1,"one of "+who+" in the town, none doubled: "+n);
                var a=TheListPeople.find(l,b,who);var at=b.offset(TheListPeople.AT[who]);
                double up=who==TheListPeople.STACE?2/16.0:who>=TheListPeople.MOTHER?TheListPeople.SLEEPER_Y:0;
                h.assertTrue(a.position().distanceTo(new Vec3(at.getX()+.5,at.getY()+up,at.getZ()+.5))<.05,who+" held at their place after a second: "+a.position()+" for "+at.toShortString());
            }
            int[][] seats={{TheListPeople.AMBER,10,Direction.SOUTH.get2DDataValue()},{TheListPeople.DEEK,8,Direction.EAST.get2DDataValue()},{TheListPeople.WES,8,Direction.EAST.get2DDataValue()},
                {TheListPeople.MEL,8,Direction.EAST.get2DDataValue()},{TheListPeople.LESLEY,8,Direction.EAST.get2DDataValue()},{TheListPeople.SHANNON,8,Direction.NORTH.get2DDataValue()}};
            for(var seat:seats){
                var s=ActorSeat.of(TheListPeople.find(l,b,seat[0]));
                h.assertTrue(s!=null&&s.seatHeight()==seat[1]&&s.facing()==Direction.from2DDataValue(seat[2]),seat[0]+" sits, facing the right way: "+s);
            }
            for(int who:new int[]{TheListPeople.JENNA,TheListPeople.JANICE,TheListPeople.STACE,TheListPeople.DEPUTY}){
                var a=TheListPeople.find(l,b,who);
                h.assertTrue(ActorSeat.of(a)==null&&a.getPose()!=net.minecraft.world.entity.Pose.SLEEPING,who+" stands");
            }
            h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(TheListPeople.find(l,b,TheListPeople.STACE).getYRot()-Direction.WEST.toYRot()))<1,"Stace looks out over the town");
            for(int who:new int[]{TheListPeople.MOTHER,TheListPeople.FATHER}){
                var a=TheListPeople.find(l,b,who);
                h.assertTrue(a.getPose()==net.minecraft.world.entity.Pose.SLEEPING&&a.getSleepingPos().orElse(BlockPos.ZERO).equals(b.offset(TheListPeople.AT[who]))
                    &&fitting(l,b,TheListPeople.AT[who],Kind.BED,0),who+" asleep, the head on a pillow of their bed");
            }
            // Out among them, each one's lines are each reader's own.
            for(int who:new int[]{TheListPeople.JANICE,TheListPeople.DEEK,TheListPeople.WES,TheListPeople.MEL,TheListPeople.LESLEY,TheListPeople.SHANNON,TheListPeople.STACE,TheListPeople.DEPUTY}){
                var a=TheListPeople.find(l,b,who);var rel=a.position().subtract(Vec3.atLowerCornerOf(b));
                var front=Vec3.directionFromRotation(0,a.getYRot()).scale(1.5);
                f.at(p,P,rel.x+front.x,rel.y,rel.z+front.z);LiteraryVignettes.talk(p,a);
                h.assertTrue(said(a).equals(TheListTexts.said(who,0)),"the first reader hears "+TheListTexts.NAMES[who]+"'s first line: "+said(a));
                f.at(peer,P,rel.x+front.x,rel.y,rel.z+front.z);LiteraryVignettes.talk(peer,a);
                h.assertTrue(said(a).equals(TheListTexts.said(who,0)),"and so does the second, for themselves: "+said(a));
                f.at(p,P,rel.x+front.x,rel.y,rel.z+front.z);LiteraryVignettes.talk(p,a);
                h.assertTrue(said(a).equals(TheListTexts.said(who,1))&&!TheListTexts.said(who,1).equals(TheListTexts.said(who,0)),"the first, asking again, hears the second: "+said(a));
                LiteraryVignettes.talk(p,a);h.assertTrue(said(a).equals(TheListTexts.said(who,1)),"and the second again after that");
            }
            // Through the glass: a knuckle on Amber's window, and on the order window.
            Object[][] windows={{TheListPeople.AMBER,new BlockPos(TheListSite.AMBER_DESK.getX(),1,TheListSite.AMBER_S),new BlockPos(TheListSite.AMBER_DESK.getX(),0,TheListSite.AMBER_S+2),TheListTexts.KNOCK_AMBER},
                {TheListPeople.JENNA,new BlockPos(35,1,-133),new BlockPos(35,0,-131),TheListTexts.KNOCK_ORDER}};
            for(var w:windows){
                int who=(int)w[0];var glass=(BlockPos)w[1];var stand=(BlockPos)w[2];var cue=(String)w[3];var a=TheListPeople.find(l,b,who);
                cues(p);cues(peer);
                use(p,b,glass,stand);
                h.assertTrue(cues(p).contains(cue)&&said(a).equals(TheListTexts.said(who,0)),TheListTexts.NAMES[who]+" answers a knock through the glass: "+said(a));
                h.assertTrue(cues(peer).isEmpty(),"the knock's cue is that reader's own");
                use(peer,b,glass,stand);h.assertTrue(said(a).equals(TheListTexts.said(who,0)),"a second reader's knock gets their own first line");
                use(p,b,glass,stand);h.assertTrue(said(a).equals(TheListTexts.said(who,1)),"the first reader's second knock, the second line");
            }
            // The reader's parents say nothing; a hand on them, or on their bed, is a cue to that reader alone.
            cues(p);cues(peer);
            var mother=TheListPeople.find(l,b,TheListPeople.MOTHER);var father=TheListPeople.find(l,b,TheListPeople.FATHER);
            f.at(p,P,-9.5,0,-15.5);LiteraryVignettes.talk(p,mother);LiteraryVignettes.talk(p,father);
            var c=cues(p);
            h.assertTrue(c.contains(TheListTexts.MOTHER)&&c.contains(TheListTexts.FATHER)&&said(mother).isEmpty()&&said(father).isEmpty(),"the parents asleep, a cue each: "+c);
            use(p,b,new BlockPos(-10,0,-15),new BlockPos(-8,0,-15));
            h.assertTrue(cues(p).contains(TheListTexts.PARENTS_BED),"their bed is theirs, not the reader's");
            h.assertTrue(cues(peer).isEmpty(),"nothing of it to the other reader");
        }).thenExecute(()->{done();h.succeed();});
    }

    /**
     * 0.4.701: the pasture south of town. Barbed wire on cedar posts all round, a braced post at each corner, the wire gap shut in the north
     * fence; the county road coming in over the cattle guard; the stock tank holding water inside its walls, the windmill's pipe over the
     * rim, the windmill's legs, footings, head and tail; the three pumpjacks, each with its wellhead, post, machine and gearbox; the tank
     * battery and the lease's sign. The machines' angles come round with the level's time, and the pitman arms keep their length.
     * A body walks in from the county road over the guard to the tank, the hay ring, a pumpjack's pad and the far corner; from inside it
     * finds no way out through the wire, until the gap is laid open by hand (said to that reader alone), and shut again. A reader crouching
     * ducks between the strands; standing, the wire holds them. The windmill creaks, a pumpjack knocks, and the guard rattles underfoot.
     */
    @GameTest(template="empty",batch="the_list_pasture",timeoutTicks=2400)
    public static void thePastureItsWindmillAndPumpjacks(GameTestHelper h){
        var f=place(h);var p=f.player("list_pasture",P);var peer=f.player("list_pasture_peer",P);var l=f.level(P);var b=f.base(P);
        int pw=TheListSite.PAST_W,pe=TheListSite.PAST_E,pn=TheListSite.PAST_N,ps=TheListSite.PAST_S,gap=TheListSite.GAP_X;
        var tank=TheListSite.TANK_AT;var mill=TheListSite.WINDMILL_AT;
        h.startSequence().thenIdle(5).thenExecute(()->{
            // The fence, its corners and the gap.
            h.assertTrue(fitting(l,b,new BlockPos(pw+5,0,pn),Kind.PASTURE,0)&&fitting(l,b,new BlockPos(pw+3,0,pn),Kind.PASTURE,1)&&fitting(l,b,new BlockPos(pe,0,pn+3),Kind.PASTURE,1)
                &&fitting(l,b,new BlockPos(pw,0,ps-3),Kind.PASTURE,1),"barbed wire on its posts along the north, east and west fences");
            for(var c:List.of(new BlockPos(pw,0,pn),new BlockPos(pe,0,pn),new BlockPos(pe,0,ps),new BlockPos(pw,0,ps)))h.assertTrue(fitting(l,b,c,Kind.PASTURE,5),"a braced post at the corner "+c.toShortString());
            for(int x=gap-1;x<=gap+1;x++)h.assertTrue(fitting(l,b,new BlockPos(x,0,pn),Kind.PASTURE,2),"the wire gap, shut, at "+x);
            h.assertTrue(fitting(l,b,new BlockPos(gap-2,0,pn),Kind.PASTURE,0)&&fitting(l,b,new BlockPos(gap+2,0,pn),Kind.PASTURE,0),"a post either side of the gap");
            // The county road and the cattle guard.
            h.assertTrue(mat(l,b,pe+5,-1,TheListSite.ROAD_N+2,"caliche")&&mat(l,b,pe-6,-1,TheListSite.LEASE_N+1,"caliche"),"the county road, and the lease road on inside");
            for(int x=pe-1;x<=pe+1;x++)for(int z=TheListSite.ROAD_N;z<=TheListSite.ROAD_S;z++)
                h.assertTrue(fitting(l,b,new BlockPos(x,-1,z),Kind.PASTURE,4)&&l.getBlockState(b.offset(x,0,z)).isAir(),"the cattle guard across the road at "+x+","+z);
            h.assertTrue(mat(l,b,tank.getX()+5,-1,tank.getZ(),"mud")||mat(l,b,tank.getX()+5,-1,tank.getZ(),"dirt")||mat(l,b,tank.getX()+5,-1,tank.getZ(),"pasture"),"the ground round the tank");
            h.assertTrue(mat(l,b,pw+20,-1,pn+20,"pasture")||mat(l,b,pw+20,-1,pn+20,"dirt"),"buffalo grass, grazed to dirt in places");
            // The stock tank: water inside its walls, the pipe over the rim.
            h.assertTrue(l.getFluidState(b.offset(tank)).isSource()&&l.getFluidState(b.offset(tank.offset(3,0,1))).isSource()&&mat(l,b,tank.getX(),-1,tank.getZ(),"galvanized"),"water in the tank on its floor");
            h.assertTrue(fitting(l,b,tank.offset(0,0,-4),Kind.STOCK,0)&&fitting(l,b,tank.offset(4,0,0),Kind.STOCK,0)&&fitting(l,b,tank.offset(2,0,3),Kind.STOCK,1),"its walls round the water");
            h.assertTrue(fitting(l,b,tank.offset(-4,0,0),Kind.STOCK,3)&&fitting(l,b,new BlockPos(mill.getX()+2,0,tank.getZ()),Kind.STOCK,4),"the windmill's pipe along the ground and over the rim");
            for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++){
                if(TheListSite.inTank(dx,dz)||(dx==-4&&dz==0))continue;
                boolean touches=TheListSite.inTank(dx+1,dz)||TheListSite.inTank(dx-1,dz)||TheListSite.inTank(dx,dz+1)||TheListSite.inTank(dx,dz-1);
                if(touches)h.assertTrue(fitting(l,b,tank.offset(dx,0,dz),Kind.STOCK),"the tank's wall where it meets the water at "+dx+","+dz);
            }
            // The windmill.
            for(int k=0;k<4;k++)h.assertTrue(fitting(l,b,mill.above(3*k),Kind.WINDMILL,k),"the windmill's tower, section "+k);
            h.assertTrue(fitting(l,b,mill.offset(-1,0,-1),Kind.WINDMILL,6)&&fitting(l,b,mill.offset(1,0,1),Kind.WINDMILL,6),"its footings");
            var head=b.offset(TheListSite.windmillHead());
            h.assertTrue(TheListMachineBlock.is(l.getBlockState(head),TheListMachineBlock.Machine.WINDMILL)&&l.getBlockEntity(head) instanceof TheListMachineBlock.Turning,"the windmill's head, its wheel turning");
            h.assertTrue(fitting(l,b,TheListSite.windmillHead().west(),Kind.WINDMILL,7),"the tail behind it");
            // The pumpjacks.
            for(int i=0;i<TheListSite.JACKS.length;i++){
                var a=TheListSite.JACKS[i];var fc=TheListSite.JACK_FACING[i];
                h.assertTrue(fitting(l,b,a,Kind.PUMPJACK,1)&&fitting(l,b,a.relative(fc,3),Kind.PUMPJACK,0)&&fitting(l,b,a.relative(fc.getOpposite(),2),Kind.PUMPJACK,7)
                    &&fitting(l,b,a.relative(fc,3).relative(fc.getClockWise(),2),Kind.BATTERY,5+i),"pumpjack "+i+": its post, wellhead, gearbox and the well's plate");
                var m=b.offset(TheListSite.jackMachine(i));
                h.assertTrue(TheListMachineBlock.is(l.getBlockState(m),TheListMachineBlock.Machine.PUMPJACK)&&l.getBlockState(m).getValue(TheListMachineBlock.FACING)==fc
                    &&l.getBlockEntity(m) instanceof TheListMachineBlock.Turning,"pumpjack "+i+"'s beam over its post, facing "+fc);
                h.assertTrue(mat(l,b,a.getX(),-1,a.getZ(),"caliche"),"pumpjack "+i+" on its caliche pad");
            }
            for(var t:TheListSite.BATTERY_TANKS)h.assertTrue(fitting(l,b,t,Kind.BATTERY,0)&&fitting(l,b,t.above(3),Kind.BATTERY,1),"a tank in the battery at "+t.toShortString());
            h.assertTrue(fitting(l,b,new BlockPos(pe+4,0,TheListSite.ROAD_S+2),Kind.BATTERY,4),"the lease's sign at the road");
            // The machines' angles come round with the time; the pitman arms keep their length.
            var at=b.offset(TheListSite.jackMachine(0));double lo=9,hi=-9,rlo=9,rhi=-9;
            for(int t=0;t<TheListMachines.STROKE;t+=5){
                double c=TheListMachines.crank(t,0,at),beam=TheListMachines.beam(c);var pin=TheListMachines.crankPin(c);var eq=TheListMachines.equalizer(beam);
                h.assertTrue(Math.abs(Math.hypot(eq[0]-pin[0],eq[1]-pin[1])-TheListMachines.PITMAN)<1e-3,"the pitman arm keeps its length at tick "+t+": beam "+beam);
                lo=Math.min(lo,beam);hi=Math.max(hi,beam);rlo=Math.min(rlo,TheListMachines.rod(beam));rhi=Math.max(rhi,TheListMachines.rod(beam));
            }
            h.assertTrue(hi-lo>.25&&hi<.5&&lo>-.5,"the beam rocks through its stroke: "+lo+" to "+hi);
            h.assertTrue(rhi-rlo>.8,"the rod rises and falls: "+rlo+" to "+rhi);
            h.assertTrue(TheListMachines.wheel(110,0,head)-TheListMachines.wheel(100,0,head)>.5,"the windmill's wheel turns");
            int knocks=0;for(long t=0;t<TheListMachines.STROKE*4;t++)if(TheListMachines.knocks(t,at))knocks++;
            h.assertTrue(knocks==4,"one knock a stroke: "+knocks);
            // In from the county road over the guard.
            var in=walked(l,b,new BlockPos(pe+5,0,TheListSite.ROAD_N+2),pw-3,pn-3,pe+8,ps+3);
            for(var c:List.of(new BlockPos(pe-4,0,TheListSite.LEASE_N+1),tank.offset(0,0,-6),new BlockPos(-20,0,64),TheListSite.JACKS[0].offset(3,0,0),new BlockPos(pw+6,0,ps-6)))
                h.assertTrue(in.contains(c),"in from the county road over the guard to "+c.toShortString()+": "+in.size());
            // From inside, no way out through the wire.
            var inside=walked(l,b,tank.offset(0,0,-6),pw-3,pn-3,pe-2,ps+3);
            h.assertTrue(inside.contains(new BlockPos(pw+6,0,ps-6))&&inside.contains(new BlockPos(pe-3,0,pn+3)),"the pasture, corner to corner: "+inside.size());
            h.assertFalse(inside.stream().anyMatch(c->c.getX()<pw||c.getZ()<pn||c.getZ()>ps),"no way out through the wire");
            cues(p);cues(peer);
            use(p,b,new BlockPos(gap,0,pn),new BlockPos(gap,0,pn+2));
            var said=cues(p);
            h.assertTrue(said.contains(TheListTexts.GAP_OPEN),"the gap laid open by hand: "+said+" in "+LiteraryVignettes.current(p)+" inside "+LiteraryVignettes.inside(p,P)
                +" at "+p.position().subtract(Vec3.atLowerCornerOf(b))+" "+l.getBlockState(b.offset(gap,0,pn)));
            for(int x=gap-1;x<=gap+1;x++)h.assertTrue(fitting(l,b,new BlockPos(x,0,pn),Kind.PASTURE,3),"the gap open at "+x);
            h.assertTrue(cues(peer).isEmpty(),"said to that reader alone");
            h.assertTrue(walked(l,b,tank.offset(0,0,-6),pw-3,pn-3,pe-2,ps+3).contains(new BlockPos(gap,0,pn-2)),"out through the open gap");
            use(p,b,new BlockPos(gap,0,pn),new BlockPos(gap,0,pn+2));
            h.assertTrue(cues(p).contains(TheListTexts.GAP_SHUT)&&fitting(l,b,new BlockPos(gap,0,pn),Kind.PASTURE,2),"and shut again");
            // Ducking between the strands.
            var span=b.offset(pw+3,0,pn);var body=new AABB(span.getX()+.2,span.getY()+.01,span.getZ()+.2,span.getX()+.8,span.getY()+1.45,span.getZ()+.8);
            h.assertFalse(l.noCollision(p,body),"the wire holds a reader standing");
            p.setPose(net.minecraft.world.entity.Pose.CROUCHING);
            h.assertTrue(l.noCollision(p,body),"a reader crouching ducks between the strands");
            p.setPose(net.minecraft.world.entity.Pose.STANDING);
            // The windmill heard from the tank.
            f.at(p,P,tank.getX()-3.5,0,tank.getZ()-4.5);f.at(peer,P,TheListSite.JACKS[0].getX()+3.5,0,TheListSite.JACKS[0].getZ()+.5);
            NativeTestPlayers.flush(p);NativeTestPlayers.packets(p);NativeTestPlayers.flush(peer);NativeTestPlayers.packets(peer);
        }).thenWaitUntil(()->h.assertTrue(heard(p,TheListRegistry.WINDMILL.get()),"the windmill, heard at the tank"))
        .thenWaitUntil(()->h.assertTrue(heard(peer,TheListRegistry.PUMPJACK.get()),"a pumpjack, heard by its pad"))
        .thenExecute(()->{f.at(p,P,pe+.3,0,TheListSite.ROAD_N+2.5);p.setOnGround(true);NativeTestPlayers.flush(p);NativeTestPlayers.packets(p);})
        .thenWaitUntil(()->{
            // Walking back and forth over the guard.
            double x=p.getX()+.15;if(x>b.getX()+pe+1.7)x=b.getX()+pe-.7;
            p.moveTo(x,p.getY(),p.getZ());p.setOnGround(true);
            h.assertTrue(heard(p,TheListRegistry.GUARD.get()),"the cattle guard rattles underfoot");
        })
        .thenExecute(()->{done();h.succeed();});
    }

    /** Every packet the reader has been sent since last asked. */
    private static List<String> cuesIn(List<net.minecraft.network.protocol.Packet<?>> packets){
        var out=new ArrayList<String>();
        for(var packet:packets){
            if(packet instanceof ClientboundSetActionBarTextPacket bar)out.add(bar.text().getString());
            if(packet instanceof ClientboundSystemChatPacket chat&&chat.overlay())out.add(chat.content().getString());
        }
        return out;
    }
    private static List<net.minecraft.network.protocol.Packet<?>> sent(ServerPlayer p){NativeTestPlayers.flush(p);return new ArrayList<>(NativeTestPlayers.packets(p));}
    private static boolean sound(List<net.minecraft.network.protocol.Packet<?>> packets,net.minecraft.sounds.SoundEvent sound){
        for(var packet:packets)if(packet instanceof ClientboundSoundPacket s&&s.getSound().value()==sound)return true;
        return false;
    }
    private static TheListBodyPayload body(List<net.minecraft.network.protocol.Packet<?>> packets){
        TheListBodyPayload last=null;
        for(var packet:packets)if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c&&c.payload() instanceof TheListBodyPayload b)last=b;
        return last;
    }
    private static boolean sack(ServerPlayer p){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(TheListSack.is(inv.getItem(i)))return true;return false;}

    /**
     * 0.4.705 (docs/THE_LIST_0_4_705.md), part 2: the slasher's body, tried on its own, with a second reader in town who never has it.
     * Given in the town, it walks at a run's pace and never sprints, and the sack is on its head. Every heart near enough is heard where
     * it beats, by the slasher alone, and the pull draws toward the nearest name; the name's heart races once she has the body in sight,
     * and with the sack off she has seen the face, said once. The second reader hears the slasher's tread, and the slasher does not. A
     * neighbor's locked door takes a blow (a second blow too soon after does nothing), splits at the second and gives at the third, open
     * and broken, both halves. A blow that is not a death leaves a heart; one that would kill puts the body down where it is, still, and
     * after five seconds it gets up there. Leaving town strips the body and leaves the sack on the back step; coming back puts it on again.
     * The body ended, nothing of it is left. A spectator is not given it.
     */
    @GameTest(template="empty",batch="the_list_body",timeoutTicks=2400)
    public static void theSlashersBody(GameTestHelper h){
        var f=place(h);var p=f.player("list_body",P);var peer=f.player("list_body_peer",P);var l=f.level(P);var b=f.base(P);
        var bench=TheListPeople.AT[TheListPeople.SHANNON];var door=new BlockPos(-34,0,-37);
        h.startSequence().thenWaitUntil(()->{
            TheListPeople.ensure(l,b);
            h.assertTrue(TheListPeople.find(l,b,TheListPeople.SHANNON)!=null,"waiting for Shannon on the bus bench");
        }).thenExecute(()->{
            h.assertTrue(TheListBody.give(p),"the body is given in the town");
            h.assertTrue(f.own(p,P).getBoolean("Body")&&!f.own(peer,P).getBoolean("Body"),"the body is the one reader's own");
        }).thenIdle(3).thenExecute(()->{
            h.assertTrue(TheListBody.walking(p)&&!TheListBody.walking(peer),"the slasher walks the body's walk; the other reader does not");
            var speed=p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            h.assertTrue(Math.abs(speed.getValue()-speed.getBaseValue()*(1+TheListBody.WALK))<1e-6,"a walk at a sprint's pace: "+speed.getValue());
            h.assertTrue(TheListSack.worn(p),"the sack is on the slasher's head");
            p.setSprinting(true);
            // Down Main Street from Shannon on the bench, the other reader beside the slasher.
            f.at(p,P,bench.getX()+.5,0,bench.getZ()-5.5);f.at(peer,P,bench.getX()+2.5,0,bench.getZ()-5.5);
            p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(b.offset(bench)));
            sent(p);sent(peer);
        }).thenIdle(40).thenExecute(()->{
            h.assertFalse(p.isSprinting(),"the body never sprints");
            var mine=sent(p);var theirs=sent(peer);
            h.assertTrue(sound(mine,TheListRegistry.HEART.get()),"the slasher hears the hearts round them");
            h.assertFalse(sound(theirs,TheListRegistry.HEART.get()),"the other reader hears none of them");
            var shannon=TheListPeople.find(l,b,TheListPeople.SHANNON);
            var shown=body(mine);
            h.assertTrue(shown!=null&&shown.body()&&shown.pull()>0,"the pull is sent to the slasher's own screen: "+shown);
            h.assertTrue(shannon.getUUID().equals(TheListBody.pulled(p)),"toward the nearest name, Shannon");
            h.assertTrue(body(theirs)==null,"nothing of the body is sent to the other reader");
            h.assertTrue(TheListBody.racing(p,shannon.getUUID()),"her heart races: she has the body in sight");
            h.assertTrue(f.own(p,P).getInt("Faced")==0,"with the sack on, nobody has seen the face");
            // The sack off: she has seen the face, once.
            var held=p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).copy();
            p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.item.ItemStack.EMPTY);p.getInventory().add(held);
            cues(p);
        }).thenIdle(25).thenExecute(()->{
            var c=cues(p);
            h.assertTrue((f.own(p,P).getInt("Faced")&1<<TheListPeople.SHANNON)!=0,"Shannon has seen the face, and the record keeps it");
            h.assertTrue(c.stream().filter(String.format(TheListTexts.FACE,"Shannon")::equals).count()==1,"said once: "+c);
            for(int i=0;i<p.getInventory().getContainerSize();i++)if(TheListSack.is(p.getInventory().getItem(i))){
                p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,p.getInventory().getItem(i).copy());p.getInventory().setItem(i,net.minecraft.world.item.ItemStack.EMPTY);break;}
            h.assertTrue(TheListSack.worn(p),"the sack back on");
            // The tread: a few steps on the ground, heard by the other reader and not the slasher.
            p.setOnGround(true);sent(p);sent(peer);
        }).thenExecute(()->f.at(p,P,bench.getX()+.5,0,bench.getZ()-6.3)).thenIdle(1)
        .thenExecute(()->f.at(p,P,bench.getX()+.5,0,bench.getZ()-7.1)).thenIdle(1)
        .thenExecute(()->f.at(p,P,bench.getX()+.5,0,bench.getZ()-7.9)).thenIdle(1)
        .thenExecute(()->f.at(p,P,bench.getX()+.5,0,bench.getZ()-8.7)).thenIdle(2).thenExecute(()->{
            h.assertTrue(sound(sent(peer),TheListRegistry.TREAD.get()),"the other reader hears the slasher's tread");
            h.assertFalse(sound(sent(p),TheListRegistry.TREAD.get()),"the slasher does not hear their own");
            // A neighbor's locked door: a blow, a second too soon (nothing), then two more a beat apart, and it gives.
            cues(p);
            use(p,b,door,door.south(2));
            h.assertTrue(TheListBody.blows(b.offset(door))==1&&shut(l,b,door)&&cues(p).contains(TheListTexts.STRIKES[0]),"the first blow");
            use(p,b,door.above(),door.south(2));
            h.assertTrue(TheListBody.blows(b.offset(door))==1,"a second blow too soon after does nothing");
        }).thenIdle(TheListBody.STRIKE_GAP).thenExecute(()->{
            use(p,b,door,door.south(2));
            h.assertTrue(TheListBody.blows(b.offset(door))==2&&shut(l,b,door)&&cues(p).contains(TheListTexts.STRIKES[1]),"the second blow splits the frame");
        }).thenIdle(TheListBody.STRIKE_GAP).thenExecute(()->{
            use(p,b,door,door.south(2));
            h.assertTrue(cues(p).contains(TheListTexts.STRIKES[2]),"the third blow");
            var lower=l.getBlockState(b.offset(door));var upper=l.getBlockState(b.offset(door).above());
            h.assertTrue(lower.is(TheListRegistry.BROKEN_DOOR.get())&&upper.is(TheListRegistry.BROKEN_DOOR.get()),"the door has given, both halves: "+lower+" / "+upper);
            h.assertTrue(lower.getValue(DoorBlock.OPEN)&&upper.getValue(DoorBlock.OPEN)&&lower.getValue(DoorBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER,"swung open");
            cues(peer);use(peer,b,door,door.south(2));
            h.assertTrue(cues(peer).isEmpty(),"a broken door is nobody's to lock: a hand on it is a door's, for anyone");
            // A blow that is not a death leaves a heart.
            p.setHealth(6);p.invulnerableTime=0;p.hurt(p.damageSources().generic(),5);
            h.assertTrue(p.isAlive()&&p.getHealth()==TheListBody.FLOOR&&TheListBody.down(p)==0,"a heart is kept, and the body stays up: "+p.getHealth());
        }).thenIdle(2).thenExecute(()->{
            // A blow that would kill puts the body down.
            p.invulnerableTime=0;var at=p.position();cues(p);
            p.hurt(p.damageSources().generic(),40);
            h.assertTrue(p.isAlive()&&p.getHealth()>=TheListBody.FLOOR,"not a death: "+p.getHealth());
            h.assertTrue(TheListBody.down(p)>0&&p.getForcedPose()==net.minecraft.world.entity.Pose.SWIMMING&&p.getPose()==net.minecraft.world.entity.Pose.SWIMMING,"down where it fell");
            h.assertTrue(cues(p).contains(TheListTexts.DOWN),"said");
            h.assertTrue(p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).getValue()==0,"lying still");
            p.invulnerableTime=0;float before=p.getHealth();p.hurt(p.damageSources().generic(),4);
            h.assertTrue(p.getHealth()==before,"nothing more is taken while down");
            h.assertTrue(p.position().distanceTo(at)<.01,"where it fell");
        }).thenIdle(TheListBody.DOWN+5).thenExecute(()->{
            h.assertTrue(TheListBody.down(p)==0&&p.getForcedPose()==null,"up again after five seconds");
            h.assertTrue(cues(p).contains(TheListTexts.UP),"said");
            var speed=p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            h.assertTrue(Math.abs(speed.getValue()-speed.getBaseValue()*(1+TheListBody.WALK))<1e-6,"the walk again");
            // Out of town by the House's way: nothing of the body is left on them, and the sack is left on the back step.
            f.leave(p);
        }).thenIdle(3).thenExecute(()->{
            h.assertFalse(TheListBody.walking(p)||sack(p)||TheListSack.worn(p),"nothing of the body outside the town");
            h.assertTrue(f.own(p,P).getBoolean("Body")&&f.own(p,P).getInt("Sack")==1,"the body is still theirs, the sack on the back step");
            h.assertTrue(cues(p).contains(TheListTexts.SACK_LEFT),"said");
            f.arrive(p,P);
        }).thenIdle(3).thenExecute(()->{
            h.assertTrue(TheListBody.walking(p)&&TheListSack.worn(p)&&f.own(p,P).getInt("Sack")==0,"back in town, the walk and the sack on again");
            h.assertTrue(cues(p).contains(TheListTexts.SACK_WORN),"said");
            TheListBody.end(p);
        }).thenIdle(2).thenExecute(()->{
            h.assertFalse(TheListBody.walking(p)||sack(p)||f.own(p,P).getBoolean("Body"),"the body ended, nothing of it left");
            peer.setGameMode(GameType.SPECTATOR);
            h.assertFalse(TheListBody.give(peer),"a spectator is not given the body");
            h.assertFalse(sound(sent(peer),TheListRegistry.HEART.get()),"and the other reader never heard a heart");
            done();h.succeed();
        });
    }

    /** The night's clock as last sent to a reader, or null. */
    private static io.github.knaitoe.theoldesthouse.network.TheListNightPayload night(List<net.minecraft.network.protocol.Packet<?>> packets){
        io.github.knaitoe.theoldesthouse.network.TheListNightPayload last=null;
        for(var packet:packets)if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c&&c.payload() instanceof io.github.knaitoe.theoldesthouse.network.TheListNightPayload n)last=n;
        return last;
    }
    private static boolean faded(List<net.minecraft.network.protocol.Packet<?>> packets){
        for(var packet:packets)if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c&&c.payload() instanceof io.github.knaitoe.theoldesthouse.network.HouseFadePayload)return true;
        return false;
    }
    private static @javax.annotation.Nullable TownCar car(ServerLevel l,BlockPos b,Vec3 rel,int kind){
        var at=Vec3.atLowerCornerOf(b).add(rel);
        for(var c:l.getEntitiesOfClass(TownCar.class,new net.minecraft.world.phys.AABB(at,at).inflate(3),c->c.kind()==kind&&c.isAlive()))return c;
        return null;
    }
    private static List<LiteraryActor> fiveInTown(ServerLevel l,BlockPos b){
        return l.getEntitiesOfClass(LiteraryActor.class,new net.minecraft.world.phys.AABB(b).inflate(200,40,200),a->TheListPeople.is(a)&&a.isAlive()&&TheListNight.five(TheListPeople.who(a)));
    }
    private static boolean doorShut(ServerLevel l,BlockPos b){
        for(int x=TheListNight.DOOR_X-1;x<=TheListNight.DOOR_X+1;x++)for(int y=0;y<=2;y++)if(!fitting(l,b,new BlockPos(x,y,TheListSite.SEED_S),Kind.SEED,y==2?5:4))return false;
        return true;
    }
    private static boolean doorOpen(ServerLevel l,BlockPos b){
        for(int x=TheListNight.DOOR_X-1;x<=TheListNight.DOOR_X+1;x++)for(int y=0;y<=2;y++)if(!l.getBlockState(b.offset(x,y,TheListSite.SEED_S)).isAir())return false;
        for(int x=TheListNight.DOOR_X+2;x<=TheListNight.DOOR_X+4;x++)for(int y=0;y<=2;y++)if(!fitting(l,b,new BlockPos(x,y,TheListSite.SEED_S+1),Kind.SEED,y==2?5:4))return false;
        return true;
    }

    /**
     * 0.4.706 (docs/THE_LIST_0_4_706.md), part 3a: night one as far as coming to, with a second reader at the party. The night begins
     * round the first reader, the one Deek picks, at ten; one night to a town. The party is heard across town; the five stand at the
     * stock tank, Mel with the camera, by a pickup with its lights on, a radio playing among their things. The second reader hears the
     * five's own party lines; Deek asks the one he picked when they come near, and talked to again takes everyone at the tank: the view
     * goes black, and they are set down in the gin's yard at two, the party taken up and the pickup with them. Deek rolls the seed house's
     * door open and sends them in. The House's door is still theirs. With the second reader inside too, nothing happens; alone, three
     * blocks in, the door rolls shut and is bolted, the House's door is shut to them (not to the other reader), the lamp and the bulb go
     * out and the auger is heard. Then black, and three in the morning: the one who went in comes to under the water tower, the body
     * theirs, lying face down; the five and the pickup are gone; the other reader is where they stood, alone, and is told so. They get up
     * with the hearts. Ended, the seed house is put back.
     */
    @GameTest(template="empty",batch="the_list_seed",timeoutTicks=4000)
    public static void nightOneTheSeedHouse(GameTestHelper h){
        var f=place(h);var p=f.player("list_night",P);var peer=f.player("list_night_peer",P);var l=f.level(P);var b=f.base(P);
        var tank=TheListSite.TANK_AT;var s=l.getServer();
        hold(f,b,tank,TheListNight.PROPS[4],TheListNight.AT_GIN[0],new BlockPos(TheListSite.TOWER_X,0,TheListSite.TOWER_Z),new BlockPos(TheListSite.SEED_W,0,TheListSite.SEED_N));
        h.startSequence().thenExecute(()->{
            f.at(p,P,tank.getX()+2.5,0,tank.getZ()-14.5);f.at(peer,P,tank.getX()-1.5,0,tank.getZ()-9.5);
        }).thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&l.isLoaded(b.offset(tank))&&l.isLoaded(b.offset(TheListNight.AT_TANK[3])),"waiting for the tank"))
        .thenExecute(()->{
            cues(p);cues(peer);sent(p);sent(peer);
            h.assertTrue(TheListNight.begin(p,b),"night one begins round the reader");
            h.assertFalse(TheListNight.begin(peer,b),"one night to a town");
            h.assertTrue(p.getUUID().equals(TheListNight.slasher(s,b))&&TheListNight.phase(s,b)==TheListNight.PARTY,"the one Deek picks is the first");
            h.assertTrue(TheListNight.time(s,b)==TheListNight.TEN,"ten at night");
            var got=sent(peer);
            h.assertTrue(cuesIn(got).contains(TheListTexts.PARTY_HEARD),"the party is heard by everyone in town");
            var clock=night(got);
            h.assertTrue(clock!=null&&clock.time()==TheListNight.TEN&&clock.rate()>0,"the night's hour is sent, running: "+clock);
            h.assertTrue(fitting(l,b,TheListNight.PROPS[0],Kind.PARTY,0)&&fitting(l,b,TheListNight.PROPS[4],Kind.PARTY,3),"the radio and the lantern are set out");
        }).thenWaitUntil(()->{
            h.assertTrue(l.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,b.offset(TheListNight.PROPS[4]))>=13,"waiting for the lantern's light");
            for(int i=0;i<TheListNight.FIVE.length;i++){var a=TheListPeople.find(l,b,TheListNight.FIVE[i]);
                h.assertTrue(a!=null&&a.blockPosition().equals(b.offset(TheListNight.AT_TANK[i])),"waiting for the five at the tank: "+TheListTexts.NAMES[TheListNight.FIVE[i]]);}
            var pickup=car(l,b,TheListNight.PICKUP,TownCar.WHITE);
            h.assertTrue(pickup!=null&&pickup.lit()&&car(l,b,TheListNight.SEDAN,TownCar.BLUE)!=null,"waiting for the pickup, its lights on, and the sedan");
            h.assertTrue(heard(p,TheListRegistry.RADIO.get()),"waiting for the radio");
        }).thenExecute(()->{
            h.assertTrue(TheListPeople.find(l,b,TheListPeople.MEL).getMainHandItem().is(LiteraryRegistry.CAMCORDER.get()),"Mel has the camera out");
            // The other reader's word with Wes and with Deek: the party's own lines, no ride.
            var wes=TheListPeople.find(l,b,TheListPeople.WES);var deek=TheListPeople.find(l,b,TheListPeople.DEEK);
            f.at(peer,P,TheListNight.AT_TANK[1].getX()+.5,0,TheListNight.AT_TANK[1].getZ()+1.5);LiteraryVignettes.talk(peer,wes);
            h.assertTrue(f.own(peer,P).getInt("Party"+TheListPeople.WES)==1&&TheListTexts.party(TheListPeople.WES,0).equals(wes.getCustomName().getString()),"Wes's party line");
            f.at(peer,P,TheListNight.AT_TANK[0].getX()-1.5,0,TheListNight.AT_TANK[0].getZ()+.5);LiteraryVignettes.talk(peer,deek);
            h.assertTrue(TheListTexts.party(TheListPeople.DEEK,0).equals(deek.getCustomName().getString())&&TheListNight.phase(s,b)==TheListNight.PARTY,"Deek's party line to the other reader, and no ride");
            // The one he picked comes near.
            f.at(p,P,TheListNight.AT_TANK[0].getX()+.5,0,TheListNight.AT_TANK[0].getZ()-1.5);
        }).thenIdle(3).thenExecute(()->{
            var deek=TheListPeople.find(l,b,TheListPeople.DEEK);
            h.assertTrue(TheListTexts.ASK.equals(deek.getCustomName().getString()),"Deek asks the one he picked: "+deek.getCustomName().getString());
            sent(p);sent(peer);
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(TheListTexts.RIDE.equals(deek.getCustomName().getString())&&TheListNight.phase(s,b)==TheListNight.RIDE,"talked to again, he takes them");
            h.assertTrue(faded(sent(p))&&faded(sent(peer)),"the view goes black for everyone at the tank");
            h.assertFalse(LiteraryVignettes.retreatLocked(p),"the House's door is still theirs");
        }).thenWaitUntil(()->h.assertTrue(TheListNight.phase(s,b)==TheListNight.SEED,"waiting for the ride"))
        .thenExecute(()->{
            var r=p.position().subtract(Vec3.atLowerCornerOf(b));
            h.assertTrue(r.distanceTo(new Vec3(79.5,0,-72.5))<.5,"set down before the seed house's door: "+r);
            h.assertTrue(peer.position().subtract(Vec3.atLowerCornerOf(b)).distanceTo(new Vec3(82.5,0,-72.5))<.5,"the other reader rode too");
            h.assertTrue(TheListNight.time(s,b)>=TheListNight.TWO,"two in the morning");
            h.assertFalse(fitting(l,b,TheListNight.PROPS[0],Kind.PARTY)||fitting(l,b,TheListNight.PROPS[4],Kind.PARTY),"the party is taken up");
        }).thenWaitUntil(()->{
            h.assertTrue(car(l,b,TheListNight.PICKUP_GIN,TownCar.WHITE)!=null&&car(l,b,TheListNight.SEDAN,TownCar.BLUE)==null,"waiting for the pickup at the gin");
            var deek=TheListPeople.find(l,b,TheListPeople.DEEK);
            h.assertTrue(deek!=null&&deek.blockPosition().equals(b.offset(TheListNight.AT_GIN[0])),"waiting for Deek at the door");
            h.assertTrue(doorOpen(l,b),"waiting for the door to roll open");
            h.assertTrue(TheListTexts.GO_IN.equals(deek.getCustomName().getString()),"waiting for Deek to send them in");
        }).thenExecute(()->{
            // Both inside: nothing.
            f.at(peer,P,TheListNight.DOOR_X+2.5,0,TheListSite.SEED_S-5.5);f.at(p,P,TheListNight.DOOR_X-.5,0,TheListSite.SEED_S-4.5);
        }).thenIdle(10).thenExecute(()->{
            h.assertTrue(TheListNight.phase(s,b)==TheListNight.SEED&&doorOpen(l,b),"with the other reader in there too, the door stays open");
            f.at(peer,P,82.5,0,-71.5);cues(p);sent(p);
        }).thenWaitUntil(()->h.assertTrue(TheListNight.phase(s,b)==TheListNight.SHUT,"waiting for the door"))
        .thenExecute(()->{
            h.assertTrue(TheListNight.locked(p)&&LiteraryVignettes.retreatLocked(p),"the House's door is shut to the one inside");
            h.assertFalse(LiteraryVignettes.retreatLocked(peer),"and not to the other reader");
        }).thenWaitUntil(()->h.assertTrue(doorShut(l,b),"waiting for the door to roll shut"))
        .thenWaitUntil(()->{
            h.assertTrue(fitting(l,b,TheListNight.LAMP,Kind.PARTY,6)&&fitting(l,b,TheListNight.BULB,Kind.PARTY,7),"waiting for the lights to go out");
            h.assertTrue(l.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,BlockPos.containing(p.getEyePosition()))<=3,"dark inside: "+l.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,BlockPos.containing(p.getEyePosition())));
        }).thenWaitUntil(()->h.assertTrue(heard(p,TheListRegistry.AUGER.get()),"waiting for the auger"))
        .thenExecute(()->{var shown=night(sent(p));h.assertTrue(shown==null||shown.shake()>=0,"the night still sent");})
        .thenWaitUntil(()->h.assertTrue(TheListNight.phase(s,b)==TheListNight.AFTER,"waiting for the cut"))
        .thenExecute(()->{
            var r=p.position().subtract(Vec3.atLowerCornerOf(b));
            h.assertTrue(r.distanceTo(TheListNight.UNDER)<.6,"under the water tower: "+r);
            h.assertTrue(TheListBody.has(p)&&TheListBody.down(p)>0&&p.getForcedPose()==net.minecraft.world.entity.Pose.SWIMMING,"the body theirs, face down");
            h.assertFalse(TheListBody.has(peer),"the other reader has no body");
            h.assertTrue(TheListNight.time(s,b)>=TheListNight.THREE,"three in the morning");
            h.assertTrue(f.own(p,P).getBoolean("CameTo1")&&f.own(peer,P).getBoolean("SawShut1"),"each reader's record keeps their night");
            h.assertFalse(LiteraryVignettes.retreatLocked(p),"the House's door is theirs again");
            h.assertTrue(peer.position().subtract(Vec3.atLowerCornerOf(b)).distanceTo(new Vec3(82.5,0,-71.5))<1,"the other reader is where they stood");
            h.assertTrue(doorShut(l,b)&&fitting(l,b,TheListNight.LAMP,Kind.PARTY,6),"the seed house stays shut and dark");
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(TheListSack.worn(p),"the sack is over their head");
            h.assertTrue(fiveInTown(l,b).isEmpty(),"the five are gone");
            h.assertTrue(car(l,b,TheListNight.PICKUP_GIN,TownCar.WHITE)==null,"the pickup is gone");
        }).thenWaitUntil(()->{
            h.assertTrue(cues(p).contains(TheListTexts.CAME_TO),"waiting to come to");
        }).thenExecute(()->h.assertTrue(cues(peer).contains(TheListTexts.ALONE),"the other reader is told they are alone"))
        .thenWaitUntil(()->h.assertTrue(TheListBody.down(p)==0,"waiting to get up"))
        .thenExecute(()->{
            h.assertTrue(p.getForcedPose()==null&&TheListBody.walking(p),"up, with the body's walk");
            TheListNight.end(l,b);TheListBody.end(p);
            h.assertTrue(TheListNight.phase(s,b)==TheListNight.NONE&&doorShut(l,b)&&fitting(l,b,TheListNight.LAMP,Kind.SEED,6)&&fitting(l,b,TheListNight.BULB,Kind.FIXTURE,3),"ended, the seed house put back");
            done();h.succeed();
        });
    }

    /**
     * 0.4.707 (docs/THE_LIST_0_4_707.md), part 3b: night one, home. With the town let go, night one begins round the first reader through
     * the door and a second reader coming in has the same night. After the cut the one who went in comes to under the tower, and Amber is
     * up at her window with a page out of a notebook: in the street in front of her house the one who went in, the sack on, is told about
     * the sack, the other reader is asked what happened, and both are given the second page; a knuckle on her glass gets her one word.
     * The one who went in lies down on their own bed: the black falls on both readers, it is first light, and each is told it in their own
     * words; each record keeps night one and where they were; neither is dealt the town again until night two; the House's door is theirs;
     * Amber is back at her desk with nothing in her hands.
     */
    @GameTest(template="empty",batch="the_list_home",timeoutTicks=3600)
    public static void nightOneHome(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        TheListNight.quiet(s,b,false);
        var p=f.player("list_home",P);var peer=f.player("list_home_peer",P);
        List<String> seen=new ArrayList<>(),peerSeen=new ArrayList<>();
        h.startSequence().thenExecute(()->{
            h.assertTrue(p.getUUID().equals(TheListNight.slasher(s,b))&&TheListNight.phase(s,b)==TheListNight.PARTY,"night one begins round the first reader through the door");
            h.assertTrue(SceneInstances.copyable(P)&&SceneInstances.party(P)==TheList.PARTY&&TheList.PARTY==5,"the town has copies, five readers to a town");
            TheListNight.jump(l,b,TheListNight.AFTER);
        }).thenWaitUntil(()->h.assertTrue(TheListBody.has(p)&&TheListBody.down(p)==0&&TheListNight.since(s,b)>TheListNight.WAKE,"waiting for the one who went in to come to and get up"))
        .thenExecute(()->{
            h.assertTrue(TheListSack.worn(p),"the sack is on");
            f.at(p,P,TheListNight.WINDOW.getX()+.5,0,TheListSite.AMBER_S+5.5);f.at(peer,P,TheListNight.WINDOW.getX()+3.5,0,TheListSite.AMBER_S+6.5);
            cues(p);cues(peer);
        }).thenWaitUntil(()->{
            var amber=TheListPeople.find(l,b,TheListPeople.AMBER);
            h.assertTrue(amber!=null&&amber.blockPosition().equals(b.offset(TheListNight.AT_WINDOW))&&amber.getMainHandItem().is(net.minecraft.world.item.Items.PAPER),"waiting for Amber at her window with a page");
            h.assertTrue(l.noCollision(amber,amber.getBoundingBox().deflate(.05)),"Amber stands clear of her desk and her wall");
        }).thenWaitUntil(()->{
            seen.addAll(cues(p));peerSeen.addAll(cues(peer));
            h.assertTrue(seen.contains(TheListTexts.PAGE_SACK)&&peerSeen.contains(TheListTexts.PAGE_WHAT),"waiting for the first pages: "+seen+" / "+peerSeen);
            h.assertTrue(seen.contains(TheListTexts.PAGE_TOMORROW)&&peerSeen.contains(TheListTexts.PAGE_TOMORROW),"waiting for the second pages");
        }).thenExecute(()->{
            h.assertFalse(seen.contains(TheListTexts.PAGE_WHAT)||peerSeen.contains(TheListTexts.PAGE_SACK),"each reader's pages are their own");
            h.assertTrue(f.own(p,P).getInt("Pages1")==2&&f.own(peer,P).getInt("Pages1")==2,"each record keeps the pages");
            f.click(peer,P,TheListNight.WINDOW);
            var amber=TheListPeople.find(l,b,TheListPeople.AMBER);
            h.assertTrue(TheListTexts.AMBER_AFTER.equals(amber.getCustomName().getString()),"a knuckle on her glass: "+amber.getCustomName().getString());
            h.assertFalse(TheListNight.phase(s,b)==TheListNight.DONE,"no first light yet");
            sent(p);sent(peer);
            f.click(p,P,TheListSite.BED_HEAD);
            h.assertTrue(TheListNight.phase(s,b)==TheListNight.DONE&&TheListNight.time(s,b)==TheListNight.LIGHT,"lying down on their own bed, it is first light");
            var mine=sent(p);var theirs=sent(peer);
            h.assertTrue(faded(mine)&&faded(theirs),"the black falls on both readers");
            var clock=night(theirs);h.assertTrue(clock!=null&&clock.time()==TheListNight.LIGHT&&clock.rate()==0,"first light's hour is sent, standing: "+clock);
            h.assertTrue(f.own(p,P).getInt("Night")==1&&"bed".equals(f.own(p,P).getString("Light1")),"the one who went in keeps night one, in bed: "+f.own(p,P));
            h.assertTrue(f.own(peer,P).getInt("Night")==1&&"town".equals(f.own(peer,P).getString("Light1")),"the other reader keeps night one, out in the town: "+f.own(peer,P));
            h.assertFalse(TheList.dealable(TheList.DEPTH+3,f.own(p,P).getInt("Night"))||TheList.dealable(TheList.DEPTH+3,f.own(peer,P).getInt("Night")),"neither is dealt the town again until night two");
            h.assertFalse(LiteraryVignettes.retreatLocked(p)||LiteraryVignettes.retreatLocked(peer),"the House's door is theirs");
            seen.clear();peerSeen.clear();
        }).thenWaitUntil(()->{
            seen.addAll(cues(p));peerSeen.addAll(cues(peer));
            h.assertTrue(seen.contains(TheListTexts.LIGHT_BED)&&peerSeen.contains(TheListTexts.LIGHT_OTHER),"waiting to be told it is light: "+seen+" / "+peerSeen);
        }).thenWaitUntil(()->{
            var amber=TheListPeople.find(l,b,TheListPeople.AMBER);
            h.assertTrue(amber!=null&&amber.blockPosition().equals(b.offset(TheListPeople.AT[TheListPeople.AMBER]))&&amber.getMainHandItem().isEmpty(),"waiting for Amber back at her desk, the page put down");
        }).thenExecute(()->{
            h.assertTrue(TheListNight.phase(s,b)==TheListNight.DONE,"first light holds");
            TheListNight.end(l,b);TheListBody.end(p);
            done();h.succeed();
        });
    }

    private static boolean near(ServerLevel l,BlockPos b,int who,Vec3 at){var a=TheListPeople.find(l,b,who);return a!=null&&a.position().subtract(Vec3.atLowerCornerOf(b)).subtract(at).horizontalDistance()<.6;}
    private static boolean hid(ServerLevel l,BlockPos b,int who,int way){var a=TheListPeople.find(l,b,who);
        return TheListHunt.state(b,who)==TheListHunt.State.HIDE&&TheListHunt.way(b,who)==way&&near(l,b,who,TheListHunt.hidingPlace(who,way))&&a.gesture()==TheListHunt.HIDING&&a.getPose()==net.minecraft.world.entity.Pose.CROUCHING;}
    private static String where(ServerLevel l,BlockPos b,int who){var a=TheListPeople.find(l,b,who);return TheListTexts.NAMES[who]+" "+TheListHunt.state(b,who)+" way "+TheListHunt.way(b,who)+" step "+TheListHunt.step(b,who)+" at "+(a==null?"nowhere":a.position().subtract(Vec3.atLowerCornerOf(b)));}
    /**
     * 0.4.708: the hunt. The slasher, with the body, comes near Deek and Wes on their bench: they get up screaming and run, and the
     * scream sends Mel and Lesley off the stands; Shannon, a block of Main Street away, hears none of it. Each says their own words. The
     * other reader hears the scream, and a few seconds later is told from which way each name is yelling for help; the slasher is not.
     * They run at more than a walk. Deek and Wes go through the field house's steel door and it is bolted behind them; Lesley bolts the
     * press box's (the slasher, near, is told). Mel goes under the stands. Each is crouched where their way ends, and a hidden name's
     * heart races in the slasher's ears. A bolted door is the other reader's hand only to be told so; the slasher's five blows spring the
     * steel. Found, Deek fights back once: the slasher goes down with what he did, and Deek runs for his second place; found there, he
     * cowers, and the slasher stays up. Shannon, come upon at the bus bench, runs to the drive-in's stand: Jenna lets her in and she is
     * bolted in. Ended, everyone is back at their place and every door is as it was built.
     */
    @GameTest(template="empty",batch="the_list_hunt",timeoutTicks=9000)
    public static void theHunt(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_hunt",P);var peer=f.player("list_hunt_peer",P);
        hold(f,b,TheListPeople.AT[TheListPeople.DEEK],TheListPeople.AT[TheListPeople.MEL],TheListPeople.AT[TheListPeople.LESLEY],TheListPeople.AT[TheListPeople.SHANNON],
            new BlockPos(-130,0,-80),new BlockPos(-124,0,-104),new BlockPos(-157,4,-58),new BlockPos(-5,0,-111),new BlockPos(20,0,-115),new BlockPos(38,0,-138));
        int[] fled={TheListPeople.DEEK,TheListPeople.WES,TheListPeople.MEL,TheListPeople.LESLEY};
        List<net.minecraft.network.protocol.Packet<?>> mine=new ArrayList<>(),theirs=new ArrayList<>();
        boolean[] jennaIn={false};
        var help=TheListTexts.HELP_HEARD.split("%s",-1);
        h.startSequence().thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"waiting for the ground along every way");
            TheListPeople.ensure(l,b);
            for(int who:TheListBody.LISTED)h.assertTrue(TheListPeople.find(l,b,who)!=null,"waiting for "+TheListTexts.NAMES[who]);
        }).thenExecute(()->{
            h.assertTrue("north".equals(TheListHunt.compass(Vec3.ZERO,new Vec3(0,0,-10)))&&"east".equals(TheListHunt.compass(Vec3.ZERO,new Vec3(10,0,0)))
                &&"southwest".equals(TheListHunt.compass(Vec3.ZERO,new Vec3(-10,0,10))),"the compass's words");
            h.assertTrue(TheListBody.give(p),"the body is given");
            TheListHunt.start(l,b);
            h.assertTrue(TheListHunt.on(b),"the hunt is on");
            for(int who:TheListBody.LISTED)h.assertTrue(TheListHunt.state(b,who)==TheListHunt.State.CALM&&ActorSeat.of(TheListPeople.find(l,b,who))!=null,TheListTexts.NAMES[who]+" sits at their place");
            f.at(peer,P,-120.5,0,-75.5);sent(peer);
            f.at(p,P,-133.5,0,-57.5);sent(p);
        }).thenWaitUntil(()->{
            mine.addAll(sent(p));theirs.addAll(sent(peer));
            for(int who:fled)h.assertTrue(TheListHunt.state(b,who)==TheListHunt.State.RUN,"waiting for them to run: "+where(l,b,who));
        }).thenExecute(()->{
            h.assertTrue(TheListHunt.state(b,TheListPeople.SHANNON)==TheListHunt.State.CALM,"Shannon, on Main Street, hears none of it");
            var deek=TheListPeople.find(l,b,TheListPeople.DEEK);
            h.assertTrue(deek.gesture()==TheListHunt.RUNNING&&TheListHunt.pace(deek)==TheListHunt.PACE&&deek.getPose()==net.minecraft.world.entity.Pose.STANDING&&!deek.isNoGravity()&&ActorSeat.of(deek)==null,"Deek up off the bench and running");
            for(int who:fled)h.assertTrue(TheListTexts.RUN[who].equals(said(TheListPeople.find(l,b,who))),TheListTexts.NAMES[who]+"'s own words: "+said(TheListPeople.find(l,b,who)));
            h.assertTrue(sound(theirs,TheListRegistry.SCREAM.get()),"the other reader hears the scream");
        }).thenWaitUntil(()->{
            mine.addAll(sent(p));theirs.addAll(sent(peer));
            var c=cuesIn(theirs);
            for(int who:fled)h.assertTrue(c.stream().anyMatch(s->s.startsWith(help[0])&&s.endsWith(help[1]+TheListTexts.NAMES[who]+help[2])),"waiting for the other reader to hear "+TheListTexts.NAMES[who]+" yell: "+c);
        }).thenExecute(()->{
            h.assertFalse(cuesIn(mine).stream().anyMatch(s->s.startsWith(help[0])),"the slasher is not told who is yelling");
            for(int who:fled)h.assertTrue(TheListTexts.HELP[who].equals(said(TheListPeople.find(l,b,who)))||TheListHunt.state(b,who)!=TheListHunt.State.RUN,"they yell: "+said(TheListPeople.find(l,b,who)));
        }).thenWaitUntil(()->{
            mine.addAll(sent(p));theirs.addAll(sent(peer));
            for(int who:fled)h.assertTrue(hid(l,b,who,0),"waiting for each to hide: "+where(l,b,TheListPeople.DEEK)+"; "+where(l,b,TheListPeople.WES)+"; "+where(l,b,TheListPeople.MEL)+"; "+where(l,b,TheListPeople.LESLEY));
        }).thenExecute(()->{
            h.assertTrue(TheListHunt.bolted(b,TheListHunt.FIELD_DOOR)&&shut(l,b,TheListHunt.FIELD_DOOR)&&shut(l,b,TheListHunt.FIELD_DOOR.east()),"the field house's steel door is shut and bolted behind Deek and Wes");
            h.assertTrue(TheListHunt.bolted(b,TheListHunt.PRESS_DOOR)&&shut(l,b,TheListHunt.PRESS_DOOR),"the press box's is bolted behind Lesley");
            h.assertTrue(cuesIn(mine).contains(TheListTexts.BOLTED_STEEL),"the slasher, near the press box, hears it: "+cuesIn(mine));
            h.assertTrue(sound(mine,TheListRegistry.BOLT.get()),"the bolt is heard");
            var mel=TheListPeople.find(l,b,TheListPeople.MEL);
            h.assertTrue(mel.getMainHandItem().is(LiteraryRegistry.CAMCORDER.get()),"Mel keeps the camcorder");
            mine.addAll(sent(p));mine.clear();
        }).thenIdle(40).thenExecute(()->{
            mine.addAll(sent(p));
            var mel=TheListPeople.find(l,b,TheListPeople.MEL);
            boolean racing=mine.stream().anyMatch(q->q instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket s&&s.getSound().value()==TheListRegistry.HEART.get()
                &&Math.abs(s.getX()-mel.getX())<1&&Math.abs(s.getZ()-mel.getZ())<1&&s.getPitch()>1.15F);
            h.assertTrue(racing,"Mel, hidden, has a racing heart in the slasher's ears");
            // The other reader's hand on the bolted steel.
            // 0.4.730: from out of reach, so as not to brace it (TheListAmber; a hand in reach braces it, the amberSide case).
            // 0.4.730: from Amber's side a hand on it braces it (TheListAmber), and it holds only while they stay by it.
            cues(peer);f.click(peer,P,TheListHunt.FIELD_DOOR);
            var pc=cues(peer);h.assertTrue(pc.contains(TheListTexts.BRACED)&&shut(l,b,TheListHunt.FIELD_DOOR),"braced, to the other reader's hand: "+pc);
            f.at(peer,P,TheListHunt.FIELD_DOOR.getX()+.5,0,TheListHunt.FIELD_DOOR.getZ()+12.5);
            h.assertTrue(TheListAmber.extra(l,b.offset(TheListHunt.FIELD_DOOR))==0,"stepped away, it is not held");
            cues(p);use(p,b,TheListHunt.FIELD_DOOR,new BlockPos(-125,0,-98));
        }).thenIdle(10).thenExecute(()->use(p,b,TheListHunt.FIELD_DOOR,new BlockPos(-125,0,-98)))
        .thenIdle(10).thenExecute(()->use(p,b,TheListHunt.FIELD_DOOR,new BlockPos(-125,0,-98)))
        .thenIdle(10).thenExecute(()->{
            h.assertTrue(shut(l,b,TheListHunt.FIELD_DOOR)&&TheListHunt.bolted(b,TheListHunt.FIELD_DOOR),"three blows, and it holds");
            use(p,b,TheListHunt.FIELD_DOOR,new BlockPos(-125,0,-98));
        }).thenIdle(10).thenExecute(()->{
            use(p,b,TheListHunt.FIELD_DOOR,new BlockPos(-125,0,-98));
            var c=cues(p);
            for(int i=0;i<TheListBody.STEEL_STRIKES;i++)h.assertTrue(c.contains(TheListTexts.STEEL_STRIKES[i]),"each blow its own cue: "+c);
            h.assertFalse(shut(l,b,TheListHunt.FIELD_DOOR)||TheListHunt.bolted(b,TheListHunt.FIELD_DOOR),"the fifth springs it");
            h.assertTrue(l.getBlockState(b.offset(TheListHunt.FIELD_DOOR)).is(TheListRegistry.STEEL_DOOR.get()),"the steel door is still there, swung in");
            // Found in the showers.
            f.at(p,P,-115.5,0,-100.5);p.setYRot(-90);
        }).thenWaitUntil(()->h.assertTrue(TheListHunt.fought(b,TheListPeople.DEEK),"waiting for Deek to be found: "+where(l,b,TheListPeople.DEEK)))
        .thenExecute(()->{
            h.assertTrue(TheListBody.down(p)>0,"the slasher is down");
            h.assertTrue(cues(p).contains(TheListTexts.FELLED[TheListPeople.DEEK]),"with what Deek did");
            h.assertTrue(TheListTexts.FIGHT[TheListPeople.DEEK].equals(said(TheListPeople.find(l,b,TheListPeople.DEEK))),"his words as he does it");
            h.assertTrue(TheListHunt.state(b,TheListPeople.DEEK)==TheListHunt.State.RUN&&TheListHunt.way(b,TheListPeople.DEEK)==1,"and he runs for his second place");
            h.assertTrue(TheListHunt.state(b,TheListPeople.WES)==TheListHunt.State.HIDE,"Wes stays where he is");
        }).thenWaitUntil(()->h.assertTrue(hid(l,b,TheListPeople.DEEK,1),"waiting for Deek to hide again: "+where(l,b,TheListPeople.DEEK)))
        .thenWaitUntil(()->h.assertTrue(TheListBody.down(p)==0,"waiting for the slasher to get up"))
        .thenExecute(()->{
            f.at(p,P,-131.5,0,-106.5);p.setYRot(90);
        }).thenWaitUntil(()->h.assertTrue(TheListHunt.state(b,TheListPeople.DEEK)==TheListHunt.State.COWER,"waiting for Deek to be found again: "+where(l,b,TheListPeople.DEEK)))
        .thenExecute(()->{
            var deek=TheListPeople.find(l,b,TheListPeople.DEEK);
            h.assertTrue(TheListTexts.COWER[TheListPeople.DEEK].equals(said(deek))&&deek.gesture()==TheListHunt.COWERING,"found twice, he cowers: "+said(deek));
            h.assertTrue(TheListBody.down(p)==0,"and does not fight again");
            // Shannon, come upon at the bus bench.
            f.at(p,P,-39.5,0,-110.5);
        }).thenWaitUntil(()->{
            var jenna=TheListPeople.find(l,b,TheListPeople.JENNA);
            if(jenna!=null&&TheListTexts.JENNA_IN.equals(said(jenna)))jennaIn[0]=true;
            h.assertTrue(hid(l,b,TheListPeople.SHANNON,0),"waiting for Shannon to hide in the stand: "+where(l,b,TheListPeople.SHANNON));
        }).thenExecute(()->{
            h.assertTrue(jennaIn[0],"Jenna lets her in");
            h.assertTrue(TheListHunt.bolted(b,TheListHunt.STAND_DOOR)&&shut(l,b,TheListHunt.STAND_DOOR),"the stand's door bolted behind her");
            h.assertTrue(TheListTexts.JENNA_DOWN.equals(said(TheListPeople.find(l,b,TheListPeople.JENNA))),"Jenna tells her to get down");
            TheListHunt.end(l,b);
            h.assertFalse(TheListHunt.on(b),"the hunt is over");
            for(int who:TheListBody.LISTED){
                var a=TheListPeople.find(l,b,who);var at=b.offset(TheListPeople.AT[who]);
                h.assertTrue(a!=null&&a.position().distanceTo(new Vec3(at.getX()+.5,at.getY(),at.getZ()+.5))<.05&&a.gesture()==TheListHunt.STILL&&ActorSeat.of(a)!=null,TheListTexts.NAMES[who]+" back at their place: "+where(l,b,who));
            }
            for(var door:new BlockPos[]{TheListHunt.FIELD_DOOR,TheListHunt.FIELD_DOOR.east(),TheListHunt.PRESS_DOOR,TheListHunt.STAND_DOOR})
                h.assertTrue(shut(l,b,door)&&!TheListHunt.bolted(b,door),"every door as it was built: "+door.toShortString());
            h.assertTrue(l.getBlockState(b.offset(TheListHunt.FIELD_DOOR)).is(TheListRegistry.STEEL_DOOR.get())&&l.getBlockState(b.offset(TheListHunt.STAND_DOOR)).is(TheListRegistry.HOUSE_DOOR.get()),"steel where steel was");
            TheListBody.end(p);
            done();h.succeed();
        });
    }

    /**
     * 0.4.709, the catch (TheListHunt.hand, take, held, loose, finish), with a second reader. Shannon on the bus bench, come up on from
     * behind, crouched, is taken hold of: held in front of the slasher, fighting the grip, her words and a scream the other reader hears,
     * the slasher stood still and unable to retreat. Looked at, she holds; looked away from, she tears loose, shoves the slasher, says so
     * and runs, and a hand on her running only gets "twists out of your reach"; she does not turn and fight straight after. Mel, found
     * twice under the stands, cowers; a hand takes hold of her, and a second with the hay hook in hand finishes her: she recoils, the hook
     * goes in (its cue, the sound, blood thrown at the slasher, and the other reader told where a scream stopped short), she goes to her
     * knees, it comes out bloodied and she folds, and lies face down. Dead, she has no heart in the slasher's ears, a hand on her gets that
     * she does not move, the slasher's record keeps her, and the hook stays bloodied. Ended, she is back at her place.
     */
    @GameTest(template="empty",batch="the_list_catch",timeoutTicks=9000)
    public static void theCatch(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_catch",P);var peer=f.player("list_catch_peer",P);
        hold(f,b,TheListPeople.AT[TheListPeople.DEEK],TheListPeople.AT[TheListPeople.MEL],TheListPeople.AT[TheListPeople.LESLEY],TheListPeople.AT[TheListPeople.SHANNON],
            new BlockPos(-130,0,-80),new BlockPos(-124,0,-104),new BlockPos(-157,4,-58),new BlockPos(-152,0,-46),new BlockPos(-5,0,-111),new BlockPos(20,0,-115),new BlockPos(38,0,-138));
        int S=TheListPeople.SHANNON,M=TheListPeople.MEL;
        var speed=net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED;
        List<net.minecraft.network.protocol.Packet<?>> mine=new ArrayList<>(),theirs=new ArrayList<>();
        h.startSequence().thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"waiting for the ground along every way");
            TheListPeople.ensure(l,b);
            for(int who:TheListBody.LISTED)h.assertTrue(TheListPeople.find(l,b,who)!=null,"waiting for "+TheListTexts.NAMES[who]);
        }).thenExecute(()->{
            h.assertTrue(TheListBody.give(p),"the body is given");
            TheListBody.giveHook(p);
            var inv=p.getInventory();for(int i=0;i<9;i++)if(inv.getItem(i).is(TheListRegistry.HAY_HOOK.get()))inv.selected=i;
            h.assertTrue(TheListBody.hookInHand(p),"the hay hook in hand");
            TheListHunt.start(l,b);
        }).thenWaitUntil(()->h.assertTrue(TheListBody.live(p),"waiting for the body to be the reader's"))
        .thenExecute(()->{
            // Shannon, from behind, crouched: she faces the street, the slasher comes up the sidewalk at her back.
            var shannon=TheListPeople.find(l,b,S);
            f.at(peer,P,-30.5,0,-111.5);sent(peer);
            f.at(p,P,-35.5,0,-109.9);p.setShiftKeyDown(true);p.setPose(net.minecraft.world.entity.Pose.CROUCHING);p.setYRot(180);p.setYHeadRot(180);sent(p);cues(p);
            h.assertFalse(TheListHunt.sees(shannon,p),"she does not see the slasher behind her");
            LiteraryVignettes.talk(p,shannon);
            h.assertTrue(TheListHunt.state(b,S)==TheListHunt.State.HELD&&TheListHunt.holding(p),"taken hold of: "+where(l,b,S));
            h.assertTrue(shannon.gesture()==TheListHunt.STRUGGLING&&shannon.isNoGravity()&&ActorSeat.of(shannon)==null,"up off the bench, fighting the grip");
            h.assertTrue(shannon.position().distanceTo(p.position())<1.0,"held in front of the slasher: "+shannon.position().distanceTo(p.position()));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.HOLD,"Shannon")),"the slasher is told");
            h.assertTrue(TheListTexts.CAUGHT[S].equals(said(shannon)),"her words: "+said(shannon));
            h.assertTrue(LiteraryVignettes.retreatLocked(p),"holding someone, there is no retreat");
            h.assertTrue(p.getAttribute(speed).hasModifier(TheListBody.HOLD_ID),"and the body stands where it is");
            theirs.addAll(sent(peer));
            h.assertTrue(sound(theirs,TheListRegistry.SCREAM.get())&&sound(theirs,TheListRegistry.STRUGGLE.get()),"the other reader hears her");
            h.assertFalse(LiteraryVignettes.retreatLocked(peer),"and is held to nothing");
        }).thenIdle(40).thenExecute(()->{
            h.assertTrue(TheListHunt.state(b,S)==TheListHunt.State.HELD,"looked at, she cannot get loose");
            var shannon=TheListPeople.find(l,b,S);
            h.assertTrue(shannon.position().distanceTo(p.position())<1.0,"still held in front of the slasher");
            p.setYRot(300);p.setYHeadRot(300);cues(p);
        }).thenWaitUntil(()->h.assertTrue(TheListHunt.state(b,S)==TheListHunt.State.RUN,"waiting for her to tear loose: "+where(l,b,S)))
        .thenExecute(()->{
            var shannon=TheListPeople.find(l,b,S);
            h.assertTrue(TheListTexts.LOOSE[S].equals(said(shannon)),"her words as she goes: "+said(shannon));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.TORE,"Shannon")),"the slasher is told");
            h.assertFalse(TheListHunt.holding(p)||p.getAttribute(speed).hasModifier(TheListBody.HOLD_ID)||LiteraryVignettes.retreatLocked(p),"let go: the body walks and may leave");
            h.assertTrue(TheListHunt.way(b,S)==0&&!shannon.isNoGravity(),"she runs her first way");
            LiteraryVignettes.talk(p,shannon);
            h.assertTrue(cues(p).contains(String.format(TheListTexts.TWIST,"Shannon"))&&TheListHunt.state(b,S)==TheListHunt.State.RUN,"running, she twists out of reach");
        }).thenIdle(10).thenExecute(()->{
            h.assertTrue(TheListBody.down(p)==0&&!TheListHunt.fought(b,S),"she does not turn and fight straight after");
            // Mel: the four at the field run, Mel hides under the north end of the stands.
            p.setShiftKeyDown(false);p.setPose(net.minecraft.world.entity.Pose.STANDING);
            f.at(p,P,-133.5,0,-57.5);
        }).thenWaitUntil(()->h.assertTrue(hid(l,b,M,0),"waiting for Mel to hide: "+where(l,b,M)))
        .thenExecute(()->{f.at(p,P,-152,0,-75.5);p.setYRot(0);p.setYHeadRot(0);})
        .thenWaitUntil(()->h.assertTrue(TheListHunt.fought(b,M),"waiting for Mel to be found: "+where(l,b,M)))
        // 0.4.710: her camcorder tears the sack on the slasher's head.
        .thenExecute(()->h.assertTrue((TheListSack.marks(p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD))&TheListSack.TORN)!=0,"the camcorder tears the sack"))
        .thenWaitUntil(()->h.assertTrue(hid(l,b,M,1)&&TheListBody.down(p)==0,"waiting for Mel at her second place and the slasher up: "+where(l,b,M)))
        .thenExecute(()->{f.at(peer,P,-145.5,0,-52.5);f.at(p,P,-152,0,-46.5);p.setYRot(0);p.setYHeadRot(0);})
        .thenWaitUntil(()->h.assertTrue(TheListHunt.state(b,M)==TheListHunt.State.COWER,"waiting for Mel to be found again: "+where(l,b,M)))
        .thenExecute(()->{
            var mel=TheListPeople.find(l,b,M);
            LiteraryVignettes.talk(p,mel);
            h.assertTrue(TheListHunt.state(b,M)==TheListHunt.State.HELD&&TheListHunt.holding(p),"cornered, she is taken hold of: "+where(l,b,M));
            h.assertTrue(TheListTexts.CAUGHT[M].equals(said(mel)),"her words: "+said(mel));
            sent(p);sent(peer);mine.clear();theirs.clear();
            LiteraryVignettes.talk(p,mel);
            h.assertTrue(TheListHunt.state(b,M)==TheListHunt.State.FINISH&&mel.gesture()==TheListHunt.RECOIL,"the hook comes up and she sees it");
            h.assertTrue(mel.getMainHandItem().isEmpty(),"she lets the camcorder fall");
        }).thenIdle(TheListHunt.RAISE+3).thenExecute(()->{
            mine.addAll(sent(p));theirs.addAll(sent(peer));
            var mel=TheListPeople.find(l,b,M);
            h.assertTrue(mel.gesture()==TheListHunt.KNEELING,"on her knees on the hook");
            var dropped=l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,mel.getBoundingBox().inflate(3),e->e.getTags().contains(TheListHunt.DROPPED));
            h.assertTrue(dropped.size()==1&&dropped.get(0).getItem().is(LiteraryRegistry.CAMCORDER.get())&&dropped.get(0).onGround()&&mel.getMainHandItem().isEmpty(),"the camcorder lies where it fell, and stays out of her hand: "+dropped);
            h.assertTrue(cuesIn(mine).contains(TheListTexts.HOOK_IN)&&sound(mine,TheListRegistry.HOOK_IN.get()),"it goes in: "+cuesIn(mine));
            h.assertTrue(mine.stream().anyMatch(q->q instanceof net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket pp&&pp.getParticle().getType()==TheListRegistry.BLOOD.get()),"blood is thrown");
            h.assertTrue(cuesIn(theirs).stream().anyMatch(c->c.startsWith("Somewhere to the ")&&c.endsWith(", a scream stops short.")),"the other reader hears a scream stop short: "+cuesIn(theirs));
            h.assertTrue(LiteraryVignettes.retreatLocked(p),"still no retreat while it is in her");
        }).thenWaitUntil(()->{
            mine.addAll(sent(p));
            h.assertTrue(TheListHunt.dead(b,M),"waiting for her to lie still: "+where(l,b,M));
        }).thenExecute(()->{
            var mel=TheListPeople.find(l,b,M);
            var c=cuesIn(mine);
            h.assertTrue(c.contains(String.format(TheListTexts.HOOK_KNEES,"Mel"))&&c.contains(String.format(TheListTexts.HOOK_OUT,"Mel")),"her knees give, and it comes out: "+c);
            h.assertTrue(sound(mine,TheListRegistry.HOOK_OUT.get()),"the hook tears free");
            h.assertTrue(mel.gesture()==TheListHunt.FALLEN&&!mel.isNoGravity()&&ActorSeat.of(mel)==null,"she lies face down where she fell");
            h.assertFalse(TheListHunt.holding(p)||p.getAttribute(speed).hasModifier(TheListBody.HOLD_ID)||LiteraryVignettes.retreatLocked(p),"let go: the body walks and may leave");
            var hook=p.getMainHandItem().get(DataComponents.CUSTOM_MODEL_DATA);
            h.assertTrue(TheListBody.hookInHand(p)&&hook!=null&&hook.value()==1&&TheListBody.bloodied(p),"the hook bloodied");
            h.assertTrue((TheListBody.kills(p)&1<<M)!=0,"the slasher's record keeps her");
            cues(p);LiteraryVignettes.talk(p,mel);
            h.assertTrue(cues(p).contains(String.format(TheListTexts.STILL_BODY,"Mel")),"a hand on her: she doesn't move");
            f.at(peer,P,mel.getX()-b.getX()+1.5,0,mel.getZ()-b.getZ());cues(peer);LiteraryVignettes.talk(peer,mel);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.STILL_BODY,"Mel")),"to the other reader's hand too");
            sent(p);
        }).thenIdle(40).thenExecute(()->{
            var mel=TheListPeople.find(l,b,M);
            boolean beat=sent(p).stream().anyMatch(q->q instanceof ClientboundSoundPacket s&&s.getSound().value()==TheListRegistry.HEART.get()&&Math.abs(s.getX()-mel.getX())<1.5&&Math.abs(s.getZ()-mel.getZ())<1.5);
            h.assertFalse(beat,"no heart where she lies");
            h.assertTrue(TheListHunt.dead(b,M)&&mel.gesture()==TheListHunt.FALLEN,"and she stays where she fell");
            TheListHunt.end(l,b);
            var at=b.offset(TheListPeople.AT[M]);
            h.assertTrue(mel.position().distanceTo(new Vec3(at.getX()+.5,at.getY(),at.getZ()+.5))<.05&&mel.gesture()==TheListHunt.STILL&&ActorSeat.of(mel)!=null,"ended, Mel is back at her place: "+where(l,b,M));
            h.assertTrue(l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,mel.getBoundingBox().inflate(80),e->e.getTags().contains(TheListHunt.DROPPED)).isEmpty(),"and the camcorder is taken up");
            TheListBody.end(p);
            var inv=p.getInventory();boolean left=false;for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(TheListRegistry.HAY_HOOK.get()))left=true;
            h.assertFalse(left,"the hook goes with the body");
            done();h.succeed();
        });
    }

    /**
     * 0.4.710, part 4a-3 (docs/THE_LIST_0_4_710.md): wounds, blood where it lands, and the dead found. Shannon, taken from behind at the
     * bus bench and given the hook, with a second reader near: the wound on her once it goes in; drops under her and a spatter where
     * the slasher stands; blood on the slasher and the sack, sent to the other reader; a smear and a pool by the time she lies still,
     * low and wide where she lies. The other reader, coming up and looking at her, is told who it is and keeps it; the slasher is not.
     * Deek, set down in sight of her, says so and runs. Ended, every stain is taken up and the wound is gone; the body taken back, the
     * blood with it.
     */
    @GameTest(template="empty",batch="the_list_found",timeoutTicks=6000)
    public static void theDeadFound(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_found",P);var peer=f.player("list_found_peer",P);
        hold(f,b,TheListPeople.AT[TheListPeople.DEEK],TheListPeople.AT[TheListPeople.SHANNON],new BlockPos(-30,0,-111));
        int S=TheListPeople.SHANNON,D=TheListPeople.DEEK;
        List<net.minecraft.network.protocol.Packet<?>> mine=new ArrayList<>(),theirs=new ArrayList<>();
        Vec3[] knelt=new Vec3[1];
        h.startSequence().thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"waiting for the ground");
            TheListPeople.ensure(l,b);
            for(int who:TheListBody.LISTED)h.assertTrue(TheListPeople.find(l,b,who)!=null,"waiting for "+TheListTexts.NAMES[who]);
        }).thenExecute(()->{
            h.assertTrue(TheListBody.give(p),"the body is given");
            TheListBody.giveHook(p);
            var inv=p.getInventory();for(int i=0;i<9;i++)if(inv.getItem(i).is(TheListRegistry.HAY_HOOK.get()))inv.selected=i;
            TheListHunt.start(l,b);
        }).thenWaitUntil(()->h.assertTrue(TheListBody.live(p)&&TheListSack.worn(p),"waiting for the body and the sack to be the reader's"))
        .thenExecute(()->{
            h.assertTrue(TheListBody.blood(p)==0&&TheListHunt.stains(l,b)==0,"nothing on the slasher, nothing on the ground");
            var shannon=TheListPeople.find(l,b,S);
            f.at(peer,P,-28.5,0,-116.5);sent(peer);
            f.at(p,P,-35.5,0,-109.9);p.setShiftKeyDown(true);p.setPose(net.minecraft.world.entity.Pose.CROUCHING);p.setYRot(180);p.setYHeadRot(180);sent(p);
            LiteraryVignettes.talk(p,shannon);
            h.assertTrue(TheListHunt.state(b,S)==TheListHunt.State.HELD,"taken hold of: "+where(l,b,S));
            LiteraryVignettes.talk(p,shannon);
            h.assertTrue(TheListHunt.state(b,S)==TheListHunt.State.FINISH,"the hook comes up");
            h.assertTrue((shannon.marks()&TheListHunt.WOUND)==0,"no wound before it goes in");
        }).thenIdle(TheListHunt.RAISE+2).thenExecute(()->{
            mine.addAll(sent(p));theirs.addAll(sent(peer));
            var shannon=TheListPeople.find(l,b,S);knelt[0]=shannon.position();
            h.assertTrue((shannon.marks()&TheListHunt.WOUND)!=0,"the wound on her once it is in");
            h.assertTrue(TheListBody.blood(p)==1,"blood on the slasher: "+TheListBody.blood(p));
            h.assertTrue((TheListSack.marks(p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD))&TheListSack.BLOOD)==1,"and on the sack");
            h.assertTrue(stain(l,shannon.position(),TheListHunt.DROPS),"drops under her");
            h.assertTrue(stain(l,p.position(),TheListHunt.SPATTER),"a spatter where the slasher stands");
            h.assertTrue(TheListHunt.stains(l,b)>=2,"both kept to be taken up: "+TheListHunt.stains(l,b));
            h.assertTrue(marks(theirs,p)>=1,"the other reader is sent the blood on the slasher: "+marks(theirs,p));
        }).thenWaitUntil(()->{
            mine.addAll(sent(p));theirs.addAll(sent(peer));
            h.assertTrue(TheListHunt.dead(b,S),"waiting for her to lie still: "+where(l,b,S));
        }).thenExecute(()->{
            var shannon=TheListPeople.find(l,b,S);
            h.assertTrue(TheListBody.blood(p)==2&&(TheListSack.marks(p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD))&TheListSack.BLOOD)==2,"more blood as it comes out: "+TheListBody.blood(p));
            h.assertTrue(marks(theirs,p)==2,"sent again: "+marks(theirs,p));
            h.assertTrue(stain(l,knelt[0],TheListHunt.SMEAR),"a smear where she knelt");
            h.assertTrue(stain(l,shannon.position(),TheListHunt.POOL),"a pool where she lies");
            h.assertTrue(shannon.getBbHeight()<.5&&shannon.getBbWidth()>1,"she lies low and long: "+shannon.getBbWidth()+" by "+shannon.getBbHeight());
            h.assertTrue(Math.abs(shannon.getY()-knelt[0].y)<.05,"she lies on the ground she knelt on: "+shannon.getY()+" against "+knelt[0].y);
            // The other reader comes up and looks at her; the slasher, standing over her, is told nothing of it.
            Vec3 spot=null;
            for(var o:new Vec3[]{new Vec3(2.5,0,0),new Vec3(-2.5,0,0),new Vec3(0,0,2.5),new Vec3(0,0,-2.5)}){
                var at=shannon.position().add(o);
                if(l.noCollision(peer,peer.getDimensions(peer.getPose()).makeBoundingBox(at))&&l.getBlockState(BlockPos.containing(at).below()).isFaceSturdy(l,BlockPos.containing(at).below(),Direction.UP)){spot=at;break;}
            }
            h.assertTrue(spot!=null,"somewhere to stand by her");
            f.at(peer,P,spot.x-b.getX(),shannon.getY()-b.getY(),spot.z-b.getZ());
            var to=shannon.position().subtract(peer.position());float yaw=(float)Math.toDegrees(Math.atan2(-to.x,to.z));
            peer.setYRot(yaw);peer.setYHeadRot(yaw);peer.setXRot(40);cues(peer);cues(p);
            TheListHunt.look(l,b);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.FOUND_DEAD,"Shannon")),"the other reader finds her");
            h.assertTrue((TheListBody.found(peer)&1<<S)!=0&&(TheListHunt.found(b,peer)&1<<S)!=0,"and keeps it");
            h.assertFalse(cues(p).contains(String.format(TheListTexts.FOUND_DEAD,"Shannon")),"the slasher is not told");
            TheListHunt.look(l,b);
            h.assertFalse(cues(peer).contains(String.format(TheListTexts.FOUND_DEAD,"Shannon")),"once");
            // Deek, set down where he can see her, at his place in the hunt: he says so, and runs.
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.CALM,"Deek has heard nothing at the field");
            deek.stopRiding();
            var by=spot.add(shannon.position()).scale(.5).add(spot.subtract(shannon.position()).normalize().scale(3));
            var look=shannon.position().subtract(by);float dy=(float)Math.toDegrees(Math.atan2(-look.x,look.z));
            deek.moveTo(by.x,shannon.getY(),by.z,dy,0);deek.setYHeadRot(dy);deek.setYBodyRot(dy);
            TheListHunt.look(l,b);
            h.assertTrue(TheListHunt.saw(b,D,S),"Deek sees her");
            h.assertTrue(String.format(TheListTexts.FOUND_BODY[D],"Shannon","Shannon").equals(said(deek)),"and says so: "+said(deek));
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.RUN,"and runs");
        }).thenExecute(()->{
            TheListHunt.end(l,b);
            h.assertTrue(TheListHunt.stains(l,b)==0,"ended, every stain is taken up");
            h.assertFalse(stain(l,knelt[0],TheListHunt.DROPS)||stain(l,p.position(),TheListHunt.DROPS),"the ground as it was");
            var shannon=TheListPeople.find(l,b,S);
            h.assertTrue(shannon.marks()==0&&shannon.gesture()==TheListHunt.STILL,"and the wound is gone");
            TheListBody.end(p);
            h.assertTrue(TheListBody.blood(p)==0&&marks(sent(peer),p)==0,"the body taken back, the blood with it");
            done();h.succeed();
        });
    }
    /**
     * 0.4.711, part 4b-1 (docs/THE_LIST_0_4_711.md): the boards. Lesley, taken from behind on the home stands by the slasher on the row
     * above her, with a second reader under the stands. A plank out of her reach, or a plank under anyone else's hand, does nothing. A hand
     * on the plank she sits on forces her over the row: her words, the slasher's cue, the scream heard, and the head under the next plank.
     * Three stamps, the first two on the plank over her neck by hand and by a blow, a second too soon counting for nothing: drops on the
     * dirt under the stands; then the crack, a spray, the neck's mark and blood on the slasher; then, by a hand on her, still, hung over
     * the row, a pool under the head, the other reader told a scream stopped, and the kill kept. The other reader, under the stands and
     * looking up at her head, is told she hangs there. Ended, the stains are taken up and she is back at her place, unmarked.
     */
    @GameTest(template="empty",batch="the_list_boards",timeoutTicks=6000)
    public static void theBoards(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_boards",P);var peer=f.player("list_boards_peer",P);
        hold(f,b,TheListPeople.AT[TheListPeople.LESLEY],TheListPeople.AT[TheListPeople.MEL],new BlockPos(-150,0,-54));
        int L=TheListPeople.LESLEY;
        var plank=new BlockPos(-153,3,-54);var above=new BlockPos(-154,3,-54);
        List<net.minecraft.network.protocol.Packet<?>> theirs=new ArrayList<>();
        Vec3[] head=new Vec3[1];
        h.startSequence().thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"waiting for the stands");
            TheListPeople.ensure(l,b);
            for(int who:TheListBody.LISTED)h.assertTrue(TheListPeople.find(l,b,who)!=null,"waiting for "+TheListTexts.NAMES[who]);
        }).thenExecute(()->{
            h.assertTrue(TheListBody.give(p),"the body is given");
            TheListHunt.start(l,b);
        }).thenWaitUntil(()->h.assertTrue(TheListBody.live(p),"waiting for the body to be the reader's"))
        .thenExecute(()->{
            var lesley=TheListPeople.find(l,b,L);
            h.assertTrue(TheListHunt.row(l,b,plank)==6&&TheListHunt.row(l,b,above)==-1,"her plank is the seventh row, the top row has none above it");
            f.at(peer,P,-150.5,0,-52.5);sent(peer);cues(peer);
            f.at(p,P,-153.5,4,-53.5);p.setShiftKeyDown(true);p.setPose(net.minecraft.world.entity.Pose.CROUCHING);p.setYRot(270);p.setYHeadRot(270);sent(p);cues(p);
            h.assertFalse(TheListHunt.sees(lesley,p),"she does not see the slasher on the row behind her");
            LiteraryVignettes.talk(p,lesley);
            h.assertTrue(TheListHunt.state(b,L)==TheListHunt.State.HELD,"taken hold of: "+where(l,b,L));
            hand(p,b,new BlockPos(-153,3,-62));
            h.assertTrue(TheListHunt.state(b,L)==TheListHunt.State.HELD,"a plank out of her reach does nothing");
            hand(peer,b,plank);
            h.assertTrue(TheListHunt.state(b,L)==TheListHunt.State.HELD,"nor anyone else's hand");
            cues(p);sent(peer);
            hand(p,b,plank);
            h.assertTrue(TheListHunt.state(b,L)==TheListHunt.State.BOARDS&&lesley.gesture()==TheListHunt.OVER,"forced over the row: "+where(l,b,L));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.FORCED_OVER,"Lesley")),"the slasher is told");
            h.assertTrue(TheListTexts.OVER[L].equals(said(lesley)),"her words: "+said(lesley));
            h.assertTrue(TheListHunt.holding(p)&&LiteraryVignettes.retreatLocked(p),"still held, and no retreat");
            head[0]=TheListHunt.head(b,L);var rel=head[0].subtract(Vec3.atLowerCornerOf(b));
            h.assertTrue(rel.x>-154&&rel.x<-153&&rel.y<3.875&&rel.y>3,"the head under the next plank: "+rel);
            h.assertTrue(lesley.getBbHeight()<1&&Math.abs(lesley.getY()-b.getY()-3.5)<.05,"down on her knees on the plank: "+lesley.getBbHeight()+" at "+(lesley.getY()-b.getY()));
            theirs.addAll(sent(peer));
            h.assertTrue(sound(theirs,TheListRegistry.SCREAM.get()),"the other reader hears her");
            hand(p,b,above);
            h.assertTrue(TheListHunt.stamps(b,L)==1&&cues(p).contains(TheListTexts.STAMPS[0]),"a stamp on the plank over her neck");
            h.assertTrue(stain(l,new Vec3(head[0].x,b.getY(),head[0].z),TheListHunt.DROPS),"blood drops onto the dirt under the stands");
            theirs.addAll(sent(peer));
            h.assertTrue(sound(theirs,TheListRegistry.STAMP.get()),"the row rings");
            hand(p,b,above);
            h.assertTrue(TheListHunt.stamps(b,L)==1,"a second straight after counts for nothing");
        }).thenIdle(TheListHunt.STAMP_GAP+1).thenExecute(()->{
            var lesley=TheListPeople.find(l,b,L);
            var at=b.offset(above);
            NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(p,at,Direction.UP,PlayerInteractEvent.LeftClickBlock.Action.START));
            h.assertTrue(TheListHunt.stamps(b,L)==2&&cues(p).contains(TheListTexts.STAMPS[1]),"a blow on it is a stamp too");
            h.assertTrue((lesley.marks()&TheListHunt.NECK)!=0,"the neck is marked");
            h.assertTrue(TheListBody.blood(p)==1,"blood on the slasher: "+TheListBody.blood(p));
            h.assertTrue(stain(l,new Vec3(head[0].x,b.getY(),head[0].z),TheListHunt.SPATTER),"blood sprayed onto the dirt");
            theirs.addAll(sent(peer));
            h.assertTrue(sound(theirs,TheListRegistry.CRACK.get()),"something gives, heard near");
            p.setYRot(90);p.setYHeadRot(90);
        }).thenIdle(TheListHunt.STAMP_GAP+60).thenExecute(()->{
            var lesley=TheListPeople.find(l,b,L);
            h.assertTrue(TheListHunt.state(b,L)==TheListHunt.State.BOARDS,"looked away from, she still cannot tear loose: "+where(l,b,L));
            p.setYRot(270);p.setYHeadRot(270);cues(peer);
            LiteraryVignettes.talk(p,lesley);
            h.assertTrue(TheListHunt.stamps(b,L)==3&&cues(p).contains(String.format(TheListTexts.STAMPS[2],"Lesley")),"the third, by a hand on her");
            h.assertTrue(TheListHunt.dead(b,L)&&lesley.gesture()==TheListHunt.HUNG,"she hangs over the row, dead");
            h.assertTrue(Math.abs(lesley.getY()-b.getY()-3.5)<.05,"where she knelt: "+(lesley.getY()-b.getY()));
            h.assertTrue(stain(l,new Vec3(head[0].x,b.getY(),head[0].z),TheListHunt.POOL),"a pool under the head");
            h.assertTrue(cues(peer).stream().anyMatch(c->c.startsWith("Somewhere to the")&&c.endsWith("a scream stops short.")),"the other reader is told a scream stopped");
            h.assertTrue((TheListBody.kills(p)&1<<L)!=0,"the slasher's record keeps her");
            h.assertFalse(TheListHunt.holding(p)||LiteraryVignettes.retreatLocked(p),"let go of: the body walks and may leave");
            LiteraryVignettes.talk(p,lesley);
            h.assertTrue(cues(p).contains(String.format(TheListTexts.STILL_BODY,"Lesley")),"a hand on her after: she does not move");
            // The other reader, under the stands, looking up at the head.
            var to=head[0].subtract(peer.getEyePosition());
            peer.setYRot((float)Math.toDegrees(Math.atan2(-to.x,to.z)));peer.setYHeadRot(peer.getYRot());
            peer.setXRot((float)-Math.toDegrees(Math.atan2(to.y,Math.sqrt(to.x*to.x+to.z*to.z))));cues(peer);
            TheListHunt.look(l,b);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.FOUND_HUNG,"Lesley")),"the other reader finds her from under the stands");
        }).thenExecute(()->{
            TheListHunt.end(l,b);
            h.assertTrue(TheListHunt.stains(l,b)==0,"ended, every stain is taken up");
            var lesley=TheListPeople.find(l,b,L);
            h.assertTrue(lesley.marks()==0&&lesley.gesture()==TheListHunt.STILL,"and she is back, unmarked: "+where(l,b,L));
            TheListBody.end(p);
            done();h.succeed();
        });
    }
    /**
     * 0.4.713, part 4b-2 (docs/THE_LIST_0_4_713.md): the line. Deek, taken from behind on the home bench by the slasher, with a second
     * reader at the valve. A riser out of his reach, or a riser under anyone else's hand, does nothing. A hand on the riser in front of
     * the bench forces him back onto it: his words, the slasher's cue, the sound and the scream heard, the head out through the front of
     * him, the riser bloodied and drops either side; the slasher walks again but still holds him and cannot leave. A hand on him: he will
     * not come out. Unwatered, he lives, and the slasher is told where the valve is. The other reader opens it: the water through him
     * told to both, the heads throwing water and his throwing blood, stains where it lands; shut, the count stops; open again, he goes
     * still on the riser, pools either side, the other reader told a scream stopped, the kill kept by the slasher and the hold let go.
     * The other reader, come near and looking, finds him across the line. Ended, the stains are taken up, the line laid again as it was
     * built and Deek back on his bench, unmarked.
     */
    @GameTest(template="empty",batch="the_list_line",timeoutTicks=6000)
    public static void theLine(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_line",P);var peer=f.player("list_line_peer",P);
        int D=TheListPeople.DEEK;
        var riser=TheListLine.riser(2);var far=TheListLine.riser(1);var valve=TheListLine.VALVE;
        hold(f,b,TheListPeople.AT[D],valve,far);
        List<net.minecraft.network.protocol.Packet<?>> theirs=new ArrayList<>();
        int[] water=new int[1];int[] stains=new int[1];
        h.startSequence().thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"waiting for the field");
            TheListPeople.ensure(l,b);
            for(int who:TheListBody.LISTED)h.assertTrue(TheListPeople.find(l,b,who)!=null,"waiting for "+TheListTexts.NAMES[who]);
        }).thenExecute(()->{
            h.assertTrue(TheListLine.riserAt(riser)==2&&riser.getZ()==TheListPeople.AT[D].getZ(),"the third riser is level with Deek on the bench");
            h.assertTrue(fitting(l,b,riser,Kind.LINE,TheListLine.RISER)&&fitting(l,b,valve,Kind.LINE,TheListLine.SHUT)
                &&fitting(l,b,new BlockPos(TheListLine.X,0,TheListLine.N),Kind.LINE,TheListLine.CAP)&&fitting(l,b,TheListLine.HOSE,Kind.LINE,TheListLine.HOSE_RUN),"the line is laid, the valve shut");
            h.assertTrue(TheListBody.give(p),"the body is given");
            TheListHunt.start(l,b);
        }).thenWaitUntil(()->h.assertTrue(TheListBody.live(p),"waiting for the body to be the reader's"))
        .thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            f.at(peer,P,valve.getX()-.5,0,valve.getZ()+.5);sent(peer);cues(peer);
            f.at(p,P,TheListPeople.AT[D].getX()-1.5,0,TheListPeople.AT[D].getZ()+.5);p.setShiftKeyDown(true);p.setPose(net.minecraft.world.entity.Pose.CROUCHING);
            p.setYRot(270);p.setYHeadRot(270);sent(p);cues(p);
            h.assertFalse(TheListHunt.sees(deek,p),"he does not see the slasher behind the bench");
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.HELD,"taken hold of: "+where(l,b,D));
            hand(p,b,far);
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.HELD,"a riser out of his reach does nothing");
            hand(peer,b,riser);
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.HELD,"nor anyone else's hand");
            cues(p);sent(peer);
            hand(p,b,riser);
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.LINE&&deek.gesture()==TheListHunt.IMPALED,"forced onto the riser: "+where(l,b,D));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.ONTO_RISER,"Deek")),"the slasher is told");
            h.assertTrue(TheListTexts.RISER[D].equals(said(deek)),"his words: "+said(deek));
            var top=Vec3.atBottomCenterOf(b.offset(riser));
            h.assertTrue(Math.abs(deek.getX()-top.x)<.05&&Math.abs(deek.getZ()-top.z)<.05&&Math.abs(deek.getY()-top.y)<.05,"across the riser: "+deek.position().subtract(top));
            h.assertTrue(Math.abs(Math.abs(deek.getYRot())-90)<1,"lying across the line: "+deek.getYRot());
            h.assertTrue(Math.abs(deek.getBbWidth()-1.2F)<.01&&Math.abs(deek.getBbHeight()-1F)<.01,"in a box the shape of him lying there: "+deek.getBbWidth()+" by "+deek.getBbHeight());
            h.assertTrue((deek.marks()&TheListHunt.GUT)!=0,"marked where it went through");
            h.assertTrue(fitting(l,b,riser,Kind.LINE,TheListLine.BLOODY),"the riser runs with blood");
            h.assertTrue(stain(l,top.add(-.9,0,0),TheListHunt.DROPS)&&stain(l,top.add(.9,0,0),TheListHunt.DROPS),"drops either side of the line");
            h.assertTrue(TheListHunt.holding(p)&&LiteraryVignettes.retreatLocked(p),"still his, and no retreat");
            h.assertTrue(p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)>0,"but the slasher walks again");
            theirs.addAll(sent(peer));
            h.assertTrue(sound(theirs,TheListRegistry.RISER.get())&&sound(theirs,TheListRegistry.SCREAM.get()),"the other reader hears it, and him");
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(cues(p).contains(String.format(TheListTexts.PINNED,"Deek"))&&TheListHunt.state(b,D)==TheListHunt.State.LINE,"a hand on him: he will not come out");
        }).thenIdle(42).thenExecute(()->{
            h.assertTrue(cues(p).contains(TheListTexts.VALVE_HINT),"the slasher is told where the valve is");
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.LINE&&TheListHunt.water(b,D)==0,"unwatered, he lives: "+where(l,b,D));
            stains[0]=TheListHunt.stains(l,b);
            hand(peer,b,valve);
            h.assertTrue(fitting(l,b,valve,Kind.LINE,TheListLine.OPEN)&&TheListLine.open(l,b),"the other reader opens the valve");
            h.assertTrue(cues(peer).contains(TheListTexts.VALVE_OPEN),"and is told so");
            cues(p);
        }).thenIdle(20).thenExecute(()->{
            var got=sent(peer);
            h.assertTrue(cuesIn(got).contains(String.format(TheListTexts.WATER_THROUGH,"Deek"))&&cues(p).contains(String.format(TheListTexts.WATER_THROUGH,"Deek")),"the water through him, told to both");
            h.assertTrue(particles(got,net.minecraft.core.particles.ParticleTypes.SPLASH)&&particles(got,TheListRegistry.BLOOD.get()),"the heads throw water, and his throws blood");
            water[0]=TheListHunt.water(b,D);
            h.assertTrue(water[0]>=15&&TheListHunt.state(b,D)==TheListHunt.State.LINE,"he lives a while under it: "+water[0]);
            h.assertTrue(TheListHunt.stains(l,b)>stains[0],"where it lands, blood: "+TheListHunt.stains(l,b));
            hand(peer,b,valve);
            h.assertTrue(fitting(l,b,valve,Kind.LINE,TheListLine.SHUT)&&cues(peer).contains(TheListTexts.VALVE_SHUT),"shut again");
            water[0]=TheListHunt.water(b,D);
        }).thenIdle(20).thenExecute(()->{
            h.assertTrue(TheListHunt.water(b,D)==water[0]&&TheListHunt.state(b,D)==TheListHunt.State.LINE,"shut, the count stops: "+TheListHunt.water(b,D));
            hand(peer,b,valve);cues(peer);cues(p);
        }).thenIdle(TheListLine.BLEEDS+5).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            var top=Vec3.atBottomCenterOf(b.offset(riser));
            h.assertTrue(TheListHunt.dead(b,D)&&deek.gesture()==TheListHunt.SPITTED,"he goes still on the riser: "+where(l,b,D));
            h.assertTrue(Math.abs(deek.getX()-top.x)<.05&&Math.abs(deek.getY()-top.y)<.05,"and stays across it");
            h.assertTrue(stain(l,top.add(-.9,0,0),TheListHunt.POOL)&&stain(l,top.add(.9,0,0),TheListHunt.POOL),"a pool either side");
            h.assertTrue(cues(p).contains(String.format(TheListTexts.LINE_STILL,"Deek")),"the slasher is told");
            h.assertTrue(cues(peer).stream().anyMatch(c->c.startsWith("Somewhere to the")&&c.endsWith("a scream stops short.")),"the other reader is told a scream stopped");
            h.assertTrue((TheListBody.kills(p)&1<<D)!=0,"the slasher's record keeps him, whoever opened the valve");
            h.assertFalse(TheListHunt.holding(p)||LiteraryVignettes.retreatLocked(p),"let go of: the body walks and may leave");
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(cues(p).contains(String.format(TheListTexts.STILL_BODY,"Deek")),"a hand on him after: he does not move");
            // The other reader, come near and looking at him.
            f.at(peer,P,top.x-b.getX()-3.5,0,top.z-b.getZ()+4);
            var to=top.add(0,.8,0).subtract(peer.getEyePosition());
            peer.setYRot((float)Math.toDegrees(Math.atan2(-to.x,to.z)));peer.setYHeadRot(peer.getYRot());
            peer.setXRot((float)-Math.toDegrees(Math.atan2(to.y,Math.sqrt(to.x*to.x+to.z*to.z))));cues(peer);
            TheListHunt.look(l,b);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.FOUND_LINE,"Deek")),"the other reader finds him across the line");
        }).thenExecute(()->{
            TheListHunt.end(l,b);
            h.assertTrue(TheListHunt.stains(l,b)==0,"ended, every stain is taken up");
            h.assertTrue(fitting(l,b,riser,Kind.LINE,TheListLine.RISER)&&fitting(l,b,valve,Kind.LINE,TheListLine.SHUT),"and the line laid again as it was built");
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.marks()==0&&deek.gesture()==TheListHunt.STILL,"and he is back, unmarked: "+where(l,b,D));
            TheListBody.end(p);
            done();h.succeed();
        });
    }
    /**
     * 0.4.714, the fryer (docs/THE_LIST_0_4_714.md). Laid off and cold in the drive-in's stand. Another reader lights it: told so, the
     * burners heard; it comes up hot on its own, and the reader by it is told and sees it bubble; put out again. Deek taken from behind on
     * the bench and the slasher brought into the stand: held near the cold fryer, a hand on it turns the knob; warming, a hand on it is
     * told the oil is not hot. Hot: a hand on it forces him in, bent over it with his face in the west vat, his words, the slasher's cue,
     * the burn, Jenna's shout, the hold kept with no retreat and no walk; the other reader hears the oil and the scream, and a hand on
     * him does not get him out. The oil has him; put out, the count stops; lit and hot again, he goes still, the oil gone dark, the
     * slasher told, the other reader told a scream stopped, the kill kept, the hold let go; Jenna has backed into the stand's corner and
     * got down. The other reader finds him over the fryer. Ended: the fryer laid off and clean, Deek back and unmarked, Jenna at her
     * window.
     */
    @GameTest(template="empty",batch="the_list_fryer",timeoutTicks=6000)
    public static void theFryer(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_fryer",P);var peer=f.player("list_fryer_peer",P);
        int D=TheListPeople.DEEK;var fryer=TheListFryer.AT;
        hold(f,b,TheListPeople.AT[D],fryer);
        List<net.minecraft.network.protocol.Packet<?>> theirs=new ArrayList<>();
        int[] fried=new int[1];
        h.startSequence().thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"waiting for the town");
            TheListPeople.ensure(l,b);
            for(int who:TheListBody.LISTED)h.assertTrue(TheListPeople.find(l,b,who)!=null,"waiting for "+TheListTexts.NAMES[who]);
            h.assertTrue(TheListPeople.find(l,b,TheListPeople.JENNA)!=null,"waiting for Jenna");
        }).thenExecute(()->{
            h.assertTrue(fitting(l,b,fryer,Kind.FRYER,TheListFryer.OFF),"the fryer stands in the stand, off");
            h.assertTrue(TheListBody.give(p),"the body is given");
            TheListHunt.start(l,b);
        }).thenWaitUntil(()->h.assertTrue(TheListBody.live(p),"waiting for the body to be the reader's"))
        .thenExecute(()->{
            f.at(peer,P,fryer.getX()+2.5,0,fryer.getZ()-2.5);sent(peer);cues(peer);
            hand(peer,b,fryer);
            var got=sent(peer);
            h.assertTrue(fitting(l,b,fryer,Kind.FRYER,TheListFryer.WARM)&&cuesIn(got).contains(TheListTexts.FRYER_ON),"the other reader lights it, and is told");
            h.assertTrue(sound(got,TheListRegistry.BURNER.get()),"the burners are heard");
        }).thenWaitUntil(()->h.assertTrue(TheListFryer.hot(l,b),"it comes up hot on its own"))
        .thenIdle(6).thenExecute(()->{
            var got=sent(peer);
            h.assertTrue(cuesIn(got).contains(TheListTexts.FRYER_HOT),"the reader by it is told the oil is smoking");
            h.assertTrue(particles(got,net.minecraft.core.particles.ParticleTypes.BUBBLE_POP),"and sees it bubble");
            hand(peer,b,fryer);
            h.assertTrue(fitting(l,b,fryer,Kind.FRYER,TheListFryer.OFF)&&cues(peer).contains(TheListTexts.FRYER_OFF),"put out again");
            // Deek taken from behind on the bench, and the slasher brought into the stand with him.
            var deek=TheListPeople.find(l,b,D);
            f.at(p,P,TheListPeople.AT[D].getX()-1.5,0,TheListPeople.AT[D].getZ()+.5);p.setShiftKeyDown(true);p.setPose(net.minecraft.world.entity.Pose.CROUCHING);
            p.setYRot(270);p.setYHeadRot(270);sent(p);cues(p);
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.HELD,"taken hold of: "+where(l,b,D));
            p.setShiftKeyDown(false);p.setPose(net.minecraft.world.entity.Pose.STANDING);
            f.at(p,P,fryer.getX()-.5,0,fryer.getZ()-1.5);
        }).thenIdle(2).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(Vec3.atCenterOf(b.offset(fryer)).distanceTo(deek.position().add(0,.6,0))<=TheListFryer.REACH,"held near the fryer: "+where(l,b,D));
            cues(p);
            hand(p,b,fryer);
            h.assertTrue(fitting(l,b,fryer,Kind.FRYER,TheListFryer.WARM)&&cues(p).contains(TheListTexts.FRYER_ON),"cold, a hand on it turns the knob");
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.HELD,"and he is still held");
            hand(p,b,fryer);
            h.assertTrue(cues(p).contains(TheListTexts.FRYER_NOT_HOT)&&TheListHunt.state(b,D)==TheListHunt.State.HELD,"warming, the oil isn't hot yet");
            var at=b.offset(fryer);TheListFryer.cameUp(l,at,l.getBlockState(at));
            h.assertTrue(TheListFryer.hot(l,b),"hot");
            sent(peer);cues(peer);
            hand(p,b,fryer);
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.FRYER&&deek.gesture()==TheListHunt.FRYING,"forced in: "+where(l,b,D));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.INTO_FRYER,"Deek")),"the slasher is told");
            h.assertTrue(TheListTexts.FRYER[D].equals(said(deek)),"his words: "+said(deek));
            var stand=Vec3.atLowerCornerOf(b).add(TheListFryer.STAND);
            h.assertTrue(deek.position().distanceTo(stand)<.05&&Math.abs(net.minecraft.util.Mth.wrapDegrees(deek.getYRot()))<1,"bent over it, facing into it: "+deek.position().subtract(stand)+" "+deek.getYRot());
            h.assertTrue((deek.marks()&TheListHunt.BURN)!=0,"the face scalded");
            var jenna=TheListPeople.find(l,b,TheListPeople.JENNA);
            h.assertTrue(String.format(TheListTexts.JENNA_FRYER,"Deek").equals(said(jenna)),"Jenna shouts at the slasher: "+said(jenna));
            h.assertTrue(TheListHunt.holding(p)&&LiteraryVignettes.retreatLocked(p),"still his, and no retreat");
            h.assertTrue(p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)==0,"and he holds him there");
            theirs.addAll(sent(peer));
            h.assertTrue(sound(theirs,TheListRegistry.FRYER.get())&&sound(theirs,TheListRegistry.SCREAM.get()),"the other reader hears the oil, and him");
            LiteraryVignettes.talk(peer,deek);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.FRYER_HELD,"Deek"))&&TheListHunt.state(b,D)==TheListHunt.State.FRYER,"a hand on him does not get him out");
        }).thenIdle(20).thenExecute(()->{
            fried[0]=TheListHunt.fried(b,D);
            h.assertTrue(fried[0]>=15&&TheListHunt.state(b,D)==TheListHunt.State.FRYER,"the oil has him a while: "+fried[0]);
            h.assertTrue(particles(sent(peer),net.minecraft.core.particles.ParticleTypes.SMOKE),"it smokes");
            hand(peer,b,fryer);
            h.assertTrue(fitting(l,b,fryer,Kind.FRYER,TheListFryer.OFF),"the other reader puts it out");
            fried[0]=TheListHunt.fried(b,D);
        }).thenIdle(20).thenExecute(()->{
            h.assertTrue(TheListHunt.fried(b,D)==fried[0]&&TheListHunt.state(b,D)==TheListHunt.State.FRYER,"put out, the count stops: "+TheListHunt.fried(b,D));
            hand(peer,b,fryer);
            var at=b.offset(fryer);TheListFryer.cameUp(l,at,l.getBlockState(at));
            cues(peer);cues(p);
        }).thenIdle(TheListFryer.UNDER+5).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(TheListHunt.dead(b,D)&&deek.gesture()==TheListHunt.FRIED,"he goes still over it: "+where(l,b,D));
            h.assertTrue(fitting(l,b,fryer,Kind.FRYER,TheListFryer.HOT+TheListFryer.DARK),"the oil gone dark");
            h.assertTrue(cues(p).contains(String.format(TheListTexts.FRYER_STILL,"Deek")),"the slasher is told");
            h.assertTrue(cues(peer).stream().anyMatch(c->c.startsWith("Somewhere to the")&&c.endsWith("a scream stops short.")),"the other reader is told a scream stopped");
            h.assertTrue((TheListBody.kills(p)&1<<D)!=0,"the slasher's record keeps him");
            h.assertFalse(TheListHunt.holding(p)||LiteraryVignettes.retreatLocked(p),"let go of: the body walks and may leave");
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(cues(p).contains(String.format(TheListTexts.STILL_BODY,"Deek")),"a hand on him after: he does not move");
            var jenna=TheListPeople.find(l,b,TheListPeople.JENNA);
            h.assertTrue(jenna.position().distanceTo(Vec3.atBottomCenterOf(b.offset(TheListPeople.CORNER)))<.5&&jenna.gesture()==TheListHunt.COWERING
                &&jenna.getPose()==net.minecraft.world.entity.Pose.CROUCHING,"Jenna backed into the stand's corner and got down: "+jenna.position().subtract(Vec3.atLowerCornerOf(b))+" "+jenna.gesture());
            // The other reader, looking at his back over the fryer.
            var back=deek.position().add(0,1,0).subtract(peer.getEyePosition());
            peer.setYRot((float)Math.toDegrees(Math.atan2(-back.x,back.z)));peer.setYHeadRot(peer.getYRot());
            peer.setXRot((float)-Math.toDegrees(Math.atan2(back.y,Math.sqrt(back.x*back.x+back.z*back.z))));cues(peer);
            TheListHunt.look(l,b);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.FOUND_FRYER,"Deek")),"the other reader finds him over the fryer");
        }).thenExecute(()->{
            TheListHunt.end(l,b);
            h.assertTrue(fitting(l,b,fryer,Kind.FRYER,TheListFryer.OFF),"ended, the fryer laid off and clean");
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.marks()==0&&deek.gesture()==TheListHunt.STILL,"and he is back, unmarked: "+where(l,b,D));
            var jenna=TheListPeople.find(l,b,TheListPeople.JENNA);
            h.assertTrue(jenna.blockPosition().equals(b.offset(TheListPeople.AT[TheListPeople.JENNA]))&&jenna.gesture()==0&&jenna.getPose()==net.minecraft.world.entity.Pose.STANDING,"Jenna back at her window, standing");
            TheListBody.end(p);
            done();h.succeed();
        });
    }
    /**
     * 0.4.715, the car door (docs/THE_LIST_0_4_715.md): the blue sedan parked in its stall at the drive-in, nobody in it, its door shut.
     * Another reader opens its door, is told so and hears the latch, and slams it shut again; a hand on the far side of the car is a
     * look at it. Deek taken from behind on the home bench and brought to the car, held by its door. Shut, a hand on the door opens
     * it and he is still held; open, a hand on it forces him down at it with the head in: the slasher told, his words, on the knees by
     * the door facing in, held with no retreat and no walk, the other reader hearing him. A hand from the other reader, on him or on
     * the door: the head is caught. The slasher's hand: the door is slammed and lands on him a moment after, stopping short of shut
     * and coming to rest against him; blood under it; a hand straight after does nothing. The second, after a wait: something gives,
     * the back of the head marked, blood on the slasher. The third: still, the slasher told, the other reader told a scream stopped,
     * the kill kept by the slasher, the hold let go, the door resting against him; a hand on the door will not shut it. The other
     * reader, looking, finds him at the car. Ended: the door shut and Deek back, unmarked.
     */
    @GameTest(template="empty",batch="the_list_car",timeoutTicks=6000)
    public static void theCarDoor(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_car",P);var peer=f.player("list_car_peer",P);
        int D=TheListPeople.DEEK;var car=BlockPos.containing(TheListCar.AT);
        hold(f,b,TheListPeople.AT[D],car);
        List<net.minecraft.network.protocol.Packet<?>> theirs=new ArrayList<>();
        h.startSequence().thenExecute(()->f.at(peer,P,TheListCar.AT.x+.3,0,TheListCar.AT.z-2.2))
        .thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"waiting for the town");
            TheListPeople.ensure(l,b);
            for(int who:TheListBody.LISTED)h.assertTrue(TheListPeople.find(l,b,who)!=null,"waiting for "+TheListTexts.NAMES[who]);
            h.assertTrue(TheListCar.car(l,b)!=null,"waiting for the car in its stall");
        }).thenExecute(()->{
            var c=TheListCar.car(l,b);
            h.assertTrue(c.kind()==TownCar.BLUE&&c.working()&&c.isPickable()&&!c.lit()&&c.position().distanceTo(Vec3.atLowerCornerOf(b).add(TheListCar.AT))<.01&&TheListCar.angle(b)==0,
                "the sedan stands in its stall, lights off, its door shut: "+c.position().subtract(Vec3.atLowerCornerOf(b)));
            h.assertTrue(TheListBody.give(p),"the body is given");
            TheListHunt.start(l,b);
            sent(peer);cues(peer);
            c.interact(peer,InteractionHand.MAIN_HAND);
            var got=sent(peer);
            h.assertTrue(cuesIn(got).contains(TheListTexts.CAR_OPENED)&&sound(got,TheListRegistry.CAR_OPEN.get()),"the other reader opens the door, told so and hearing the latch");
        }).thenIdle(10).thenExecute(()->{
            h.assertTrue(TheListCar.open(b)&&TheListCar.car(l,b).door()==TheListCar.OPEN,"the door stands open: "+TheListCar.angle(b));
            TheListCar.hand(peer,TheListCar.car(l,b));
            h.assertTrue(cues(peer).contains(TheListTexts.CAR_SHUT),"and shuts it");
        }).thenIdle(5).thenExecute(()->{
            h.assertTrue(TheListCar.angle(b)==0&&sound(sent(peer),TheListRegistry.CAR_DOOR.get()),"slammed shut, and heard: "+TheListCar.angle(b));
            f.at(peer,P,TheListCar.AT.x,0,TheListCar.AT.z+1.6);cues(peer);
            TheListCar.hand(peer,TheListCar.car(l,b));
            h.assertTrue(cues(peer).contains(TheListTexts.CAR_LOOK)&&TheListCar.angle(b)==0,"on the far side, a hand on the car is a look at it");
            f.at(peer,P,TheListCar.AT.x+1.4,0,TheListCar.AT.z-2.2);
            // Deek taken from behind on the bench, and the slasher brought to the car with him.
            var deek=TheListPeople.find(l,b,D);
            f.at(p,P,TheListPeople.AT[D].getX()-1.5,0,TheListPeople.AT[D].getZ()+.5);p.setShiftKeyDown(true);p.setPose(net.minecraft.world.entity.Pose.CROUCHING);
            p.setYRot(270);p.setYHeadRot(270);sent(p);cues(p);
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.HELD,"taken hold of: "+where(l,b,D));
            p.setShiftKeyDown(false);p.setPose(net.minecraft.world.entity.Pose.STANDING);
            f.at(p,P,TheListCar.AT.x-.65,0,TheListCar.AT.z-2.4);
        }).thenIdle(2).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.position().distanceTo(TheListCar.kneel(b))<=TheListCar.REACH,"held by the door: "+where(l,b,D));
            cues(p);
            TheListCar.hand(p,TheListCar.car(l,b));
            h.assertTrue(cues(p).contains(TheListTexts.CAR_OPENED)&&TheListHunt.state(b,D)==TheListHunt.State.HELD,"shut, a hand on it opens it, and he is still held");
        }).thenIdle(10).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(TheListCar.open(b),"open");
            sent(peer);cues(peer);
            TheListCar.hand(p,TheListCar.car(l,b));
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.CAR&&deek.gesture()==TheListHunt.DOORED,"forced down at the door: "+where(l,b,D));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.INTO_DOOR,"Deek")),"the slasher is told");
            h.assertTrue(TheListTexts.DOOR[D].equals(said(deek)),"his words: "+said(deek));
            h.assertTrue(deek.position().distanceTo(TheListCar.kneel(b))<.05&&Math.abs(net.minecraft.util.Mth.wrapDegrees(deek.getYRot()-TheListCar.KNEEL_YAW))<1,
                "on the knees by the door, facing in: "+deek.position().subtract(TheListCar.kneel(b))+" "+deek.getYRot());
            h.assertTrue(TheListHunt.holding(p)&&LiteraryVignettes.retreatLocked(p),"still his, and no retreat");
            h.assertTrue(p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)==0,"and he holds him there");
            theirs.addAll(sent(peer));
            h.assertTrue(sound(theirs,TheListRegistry.SCREAM.get()),"the other reader hears him");
            LiteraryVignettes.talk(peer,deek);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.DOOR_HELD,"Deek"))&&TheListHunt.state(b,D)==TheListHunt.State.CAR,"a hand on him: the head is caught");
            TheListCar.hand(peer,TheListCar.car(l,b));
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.DOOR_HELD,"Deek"))&&TheListCar.open(b),"and on the door: the same, the door left where it is");
            cues(p);
            TheListCar.hand(p,TheListCar.car(l,b));
            h.assertTrue(TheListHunt.slams(b,D)==0&&TheListCar.swinging(b),"slammed: on its way down");
        }).thenIdle(4).thenExecute(()->{
            h.assertTrue(TheListHunt.slams(b,D)==1&&cues(p).contains(TheListTexts.SLAMS[0]),"it lands on him, and the slasher is told: "+TheListHunt.slams(b,D));
            float at=TheListCar.angle(b);
            h.assertTrue(at>=TheListCar.HIT&&at<=TheListCar.HIT+11.01F,"stopped short of shut: "+at);
            h.assertTrue(sound(sent(peer),TheListRegistry.CAR_DOOR.get()),"the other reader hears it");
            h.assertTrue(stain(l,TheListCar.under(b),TheListHunt.DROPS),"blood under the door");
            TheListCar.hand(p,TheListCar.car(l,b));
        }).thenIdle(6).thenExecute(()->{
            h.assertTrue(TheListHunt.slams(b,D)==1&&TheListCar.angle(b)==TheListCar.HIT,"a hand straight after does nothing; the door rests against him: "+TheListHunt.slams(b,D)+" "+TheListCar.angle(b));
        }).thenIdle(TheListHunt.SLAM_GAP).thenExecute(()->{
            cues(p);TheListCar.hand(p,TheListCar.car(l,b));
        }).thenIdle(9).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(TheListHunt.slams(b,D)==2&&cues(p).contains(TheListTexts.SLAMS[1]),"again: something gives: "+TheListHunt.slams(b,D));
            h.assertTrue((deek.marks()&TheListHunt.CRUSH)!=0&&TheListBody.blood(p)>=1,"the back of the head marked, and blood on the slasher: "+TheListBody.blood(p));
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.CAR,"he is still there");
        }).thenIdle(TheListHunt.SLAM_GAP).thenExecute(()->{
            cues(p);cues(peer);TheListCar.hand(p,TheListCar.car(l,b));
        }).thenIdle(13).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(TheListHunt.dead(b,D)&&deek.gesture()==TheListHunt.SLAMMED,"he goes still: "+where(l,b,D));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.SLAMS[2],"Deek")),"the slasher is told");
            h.assertTrue(cues(peer).stream().anyMatch(c->c.startsWith("Somewhere to the")&&c.endsWith("a scream stops short.")),"the other reader is told a scream stopped");
            h.assertTrue((TheListBody.kills(p)&1<<D)!=0,"the slasher's record keeps him");
            h.assertFalse(TheListHunt.holding(p)||LiteraryVignettes.retreatLocked(p),"let go of: the body walks and may leave");
            h.assertTrue(TheListCar.angle(b)==TheListCar.HIT&&deek.position().distanceTo(TheListCar.kneel(b))<.05,"the door resting against him, where he knelt");
            TheListCar.hand(p,TheListCar.car(l,b));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.CAR_STOPPED,"Deek"))&&TheListCar.angle(b)==TheListCar.HIT,"a hand on the door will not shut it");
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(cues(p).contains(String.format(TheListTexts.STILL_BODY,"Deek")),"a hand on him after: he does not move");
            // The other reader, looking at his back at the car.
            var back=deek.position().add(0,.7,0).subtract(peer.getEyePosition());
            peer.setYRot((float)Math.toDegrees(Math.atan2(-back.x,back.z)));peer.setYHeadRot(peer.getYRot());
            peer.setXRot((float)-Math.toDegrees(Math.atan2(back.y,Math.sqrt(back.x*back.x+back.z*back.z))));cues(peer);
            TheListHunt.look(l,b);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.FOUND_CAR,"Deek")),"the other reader finds him at the car");
        }).thenExecute(()->{
            TheListHunt.end(l,b);
            h.assertTrue(TheListCar.angle(b)==0,"ended, the door shut");
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.marks()==0&&deek.gesture()==TheListHunt.STILL,"and he is back, unmarked: "+where(l,b,D));
            TheListBody.end(p);
            done();h.succeed();
        });
    }
    /**
     * 0.4.716, the pumpjack (docs/THE_LIST_0_4_716.md): the north well running. Another reader hauls its brake on, told so and hearing
     * it: the well stands where it is; knocked off, it runs on from there. Deek taken from behind on the home bench and brought to the
     * well, held by its gearbox. With the weight in its slot, a hand on the gearbox tells the slasher it is coming round and he is still
     * held; with it clear, a hand forces him down: the slasher told, his words, on the knees beside the gearbox bowed toward it, held
     * with no retreat and no walk, the other reader hearing him. A hand from the other reader on him: held down. The other reader hauls
     * the brake on: the weight does not come. The slasher's hand on him knocks it off; the weight comes round onto the head and the well
     * stops there: still, the side of the head marked, the slasher told, the other reader told a scream stopped and hearing the motor,
     * the kill kept by the slasher, the hold let go, blood under the head. The motor grinds the weight on a little and then only stands;
     * the brake makes no difference; a hand on him and he does not move. The other reader, looking, finds him at the pumpjack. Ended: the
     * well running, Deek back and unmarked.
     */
    @GameTest(template="empty",batch="the_list_jack",timeoutTicks=6000)
    public static void theJack(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_jack",P);var peer=f.player("list_jack_peer",P);
        int D=TheListPeople.DEEK,J=0;var box=TheListJack.gearbox(J);
        hold(f,b,TheListPeople.AT[D],TheListSite.JACKS[J]);
        var kneel=TheListJack.kneel(b,J).subtract(Vec3.atLowerCornerOf(b));
        double[] held=new double[1];
        List<net.minecraft.network.protocol.Packet<?>> theirs=new ArrayList<>();
        h.startSequence().thenExecute(()->f.at(peer,P,kneel.x-2.4,0,kneel.z+2.6))
        .thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"waiting for the town");
            TheListPeople.ensure(l,b);
            for(int who:TheListBody.LISTED)h.assertTrue(TheListPeople.find(l,b,who)!=null,"waiting for "+TheListTexts.NAMES[who]);
            h.assertTrue(l.getBlockEntity(TheListJack.machine(b,J)) instanceof TheListMachineBlock.Turning,"waiting for the well");
        }).thenExecute(()->{
            h.assertTrue(TheListJack.running(l,b,J)&&!TheListJack.braked(l,b,J)&&fitting(l,b,box,Kind.PUMPJACK,7),"the well runs, its brake off");
            h.assertTrue(TheListJack.JAM<TheListJack.CLEAR&&TheListJack.JAM>Math.toRadians(-90)&&TheListJack.JAM<0,"the weight meets the head coming down behind the shaft: "+Math.toDegrees(TheListJack.JAM)+" to "+Math.toDegrees(TheListJack.CLEAR));
            h.assertTrue(TheListBody.give(p),"the body is given");
            TheListHunt.start(l,b);
            sent(peer);cues(peer);
            TheListJack.hand(peer,b,box);
            var got=sent(peer);
            h.assertTrue(cuesIn(got).contains(TheListTexts.BRAKE_ON)&&sound(got,TheListRegistry.BRAKE.get()),"the other reader hauls the brake on, told so and hearing it");
            h.assertTrue(TheListJack.braked(l,b,J)&&!TheListJack.running(l,b,J),"braked, the well stands");
            held[0]=TheListJack.tick(l,b,J);
        }).thenIdle(5).thenExecute(()->{
            h.assertTrue(TheListJack.tick(l,b,J)==held[0],"it stands where it was: "+(TheListJack.tick(l,b,J)-held[0]));
            TheListJack.hand(peer,b,box);
            h.assertTrue(cues(peer).contains(TheListTexts.BRAKE_OFF)&&TheListJack.running(l,b,J)&&!TheListJack.braked(l,b,J),"knocked off, it runs");
        }).thenIdle(5).thenExecute(()->{
            double went=TheListJack.tick(l,b,J)-held[0];
            h.assertTrue(went>=4.5&&went<=5.5,"on from where it stood: "+went);
            // Deek taken from behind on the bench, and the slasher brought to the well with him.
            var deek=TheListPeople.find(l,b,D);
            f.at(p,P,TheListPeople.AT[D].getX()-1.5,0,TheListPeople.AT[D].getZ()+.5);p.setShiftKeyDown(true);p.setPose(net.minecraft.world.entity.Pose.CROUCHING);
            p.setYRot(270);p.setYHeadRot(270);sent(p);cues(p);
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.HELD,"taken hold of: "+where(l,b,D));
            p.setShiftKeyDown(false);p.setPose(net.minecraft.world.entity.Pose.STANDING);
            f.at(p,P,kneel.x-1.25,0,kneel.z);
        }).thenIdle(2).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.position().distanceTo(TheListJack.kneel(b,J))<=TheListJack.REACH,"held by the gearbox: "+where(l,b,D));
            // The weight down in its slot: not yet.
            TheListJack.setAngle(l,b,J,Math.toDegrees(TheListJack.JAM)+5);cues(p);
            TheListJack.hand(p,b,box);
            h.assertTrue(cues(p).contains(TheListTexts.JACK_WAIT)&&TheListHunt.state(b,D)==TheListHunt.State.HELD,"the weight is coming round, and he is still held");
            // Clear of it, a way still to come: down.
            TheListJack.setAngle(l,b,J,Math.toDegrees(TheListJack.CLEAR)+10);
            sent(peer);cues(peer);cues(p);
            TheListJack.hand(p,b,box);
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.JACK&&deek.gesture()==TheListHunt.PUMPED,"forced down by the gearbox: "+where(l,b,D));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.INTO_JACK,"Deek")),"the slasher is told");
            h.assertTrue(TheListTexts.JACK[D].equals(said(deek)),"his words: "+said(deek));
            h.assertTrue(deek.position().distanceTo(TheListJack.kneel(b,J))<.05&&Math.abs(net.minecraft.util.Mth.wrapDegrees(deek.getYRot()-TheListJack.kneelYaw(J)))<1,
                "on the knees by the gearbox, bowed toward it: "+deek.position().subtract(TheListJack.kneel(b,J))+" "+deek.getYRot());
            h.assertTrue(TheListHunt.holding(p)&&LiteraryVignettes.retreatLocked(p),"still his, and no retreat");
            h.assertTrue(p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)==0,"and he holds him there");
            theirs.addAll(sent(peer));
            h.assertTrue(sound(theirs,TheListRegistry.SCREAM.get()),"the other reader hears him");
            LiteraryVignettes.talk(peer,deek);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.JACK_HELD,"Deek"))&&TheListHunt.state(b,D)==TheListHunt.State.JACK,"a hand on him: held down");
            // The other reader hauls the brake on: the weight does not come.
            TheListJack.hand(peer,b,box);
            h.assertTrue(cues(peer).contains(TheListTexts.BRAKE_ON)&&TheListJack.braked(l,b,J),"the brake, on");
            // For the case: set on a moment from the head (which lets the brake go), and braked there.
            TheListJack.setAngle(l,b,J,Math.toDegrees(TheListJack.JAM)-6);
            h.assertTrue(TheListJack.running(l,b,J),"set running a moment from the head");
            TheListJack.hand(peer,b,box);
            h.assertTrue(TheListJack.braked(l,b,J),"and braked again");
            held[0]=TheListJack.tick(l,b,J);
        }).thenIdle(12).thenExecute(()->{
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.JACK&&TheListJack.tick(l,b,J)==held[0],"braked, the weight does not come: "+where(l,b,D));
            // The slasher's hand on him knocks it off.
            var deek=TheListPeople.find(l,b,D);
            cues(p);cues(peer);sent(peer);
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(cues(p).contains(TheListTexts.BRAKE_OFF)&&TheListJack.running(l,b,J),"the slasher knocks the brake off");
            TheListJack.hand(p,b,box);
            h.assertTrue(TheListJack.running(l,b,J)&&TheListHunt.state(b,D)==TheListHunt.State.JACK,"his hand on the gearbox after does nothing more");
        }).thenIdle(8).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(TheListHunt.dead(b,D)&&deek.gesture()==TheListHunt.PINNED,"the weight comes down on him: "+where(l,b,D));
            h.assertTrue((deek.marks()&TheListHunt.FLAT)!=0,"the side of the head marked");
            h.assertTrue(TheListJack.stalled(b,J)&&!TheListJack.running(l,b,J),"the well stopped on him");
            double at=TheListJack.ahead(TheListJack.JAM,TheListJack.angle(l,b,J));
            h.assertTrue(at<=TheListJack.GRIND+.01,"stopped where it met him: "+Math.toDegrees(at));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.JACK_STILL,"Deek")),"the slasher is told");
            var got=sent(peer);
            h.assertTrue(cuesIn(got).stream().anyMatch(c->c.startsWith("Somewhere to the")&&c.endsWith("a scream stops short.")),"the other reader is told a scream stopped");
            h.assertTrue(sound(got,TheListRegistry.STRAIN.get()),"and hears the motor");
            h.assertTrue((TheListBody.kills(p)&1<<D)!=0,"the slasher's record keeps him");
            h.assertFalse(TheListHunt.holding(p)||LiteraryVignettes.retreatLocked(p),"let go of: the body walks and may leave");
            h.assertTrue(stain(l,TheListJack.under(b,J),TheListHunt.POOL),"a pool under the head");
            h.assertTrue(deek.position().distanceTo(TheListJack.kneel(b,J))<.05,"where he knelt");
        }).thenIdle(TheListJack.GRIND_TICKS+10).thenExecute(()->{
            double at=TheListJack.ahead(TheListJack.JAM,TheListJack.angle(l,b,J));
            h.assertTrue(at>TheListJack.GRIND*.8&&at<=TheListJack.GRIND+.01,"ground on into him: "+Math.toDegrees(at));
            cues(p);cues(peer);
            TheListJack.hand(peer,b,box);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.JACK_STALLED,"Deek"))&&!TheListJack.braked(l,b,J)&&TheListJack.stalled(b,J),"the brake makes no difference");
            var deek=TheListPeople.find(l,b,D);
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(cues(p).contains(String.format(TheListTexts.STILL_BODY,"Deek")),"a hand on him after: he does not move");
            // The other reader, looking at his back by the gearbox.
            var back=deek.position().add(0,.45,0).subtract(peer.getEyePosition());
            peer.setYRot((float)Math.toDegrees(Math.atan2(-back.x,back.z)));peer.setYHeadRot(peer.getYRot());
            peer.setXRot((float)-Math.toDegrees(Math.atan2(back.y,Math.sqrt(back.x*back.x+back.z*back.z))));cues(peer);
            TheListHunt.look(l,b);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.FOUND_JACK,"Deek")),"the other reader finds him at the pumpjack");
        }).thenExecute(()->{
            TheListHunt.end(l,b);
            h.assertTrue(TheListJack.running(l,b,J)&&!TheListJack.braked(l,b,J)&&!TheListJack.stalled(b,J),"ended, the well running");
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.marks()==0&&deek.gesture()==TheListHunt.STILL,"and he is back, unmarked: "+where(l,b,D));
            TheListBody.end(p);
            done();h.succeed();
        });
    }
    /**
     * 0.4.717, the stock tank: a hand on its wall is a look for anyone without hold of one of the five. Deek taken and brought to it, the
     * slasher's hand on the wall forces him over the rim with the head under; another reader's hand on him finds him held under. He fights
     * the head up, gasping, and the slasher's hand puts it back, twice; the third time it stays under, and he drowns there.
     */
    @GameTest(template="empty",batch="the_list_tank",timeoutTicks=6000)
    public static void theTank(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_tank",P);var peer=f.player("list_tank_peer",P);
        int D=TheListPeople.DEEK;var wall=TheListSite.TANK_AT.offset(0,0,-4);var in=TheListTank.into(wall);
        hold(f,b,TheListPeople.AT[D],TheListSite.TANK_AT);
        var stand=TheListTank.stand(b,wall,in==null?net.minecraft.core.Direction.SOUTH:in).subtract(Vec3.atLowerCornerOf(b));
        h.startSequence().thenExecute(()->f.at(peer,P,stand.x+2.5,0,stand.z-2.5))
        .thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"waiting for the town");
            TheListPeople.ensure(l,b);
            for(int who:TheListBody.LISTED)h.assertTrue(TheListPeople.find(l,b,who)!=null,"waiting for "+TheListTexts.NAMES[who]);
        }).thenExecute(()->{
            h.assertTrue(in==net.minecraft.core.Direction.SOUTH,"the north wall leads south into the water: "+in);
            h.assertTrue(TheListTank.into(TheListSite.TANK_AT.offset(3,0,-3))==null&&TheListTank.into(TheListSite.TANK_AT)==null,"a corner, or the water, is no wall to use");
            h.assertTrue(TheListBody.give(p),"the body is given");
            TheListHunt.start(l,b);
            h.assertFalse(TheListTank.hand(peer,b,wall),"another reader's hand on the wall is a look");
        }).thenIdle(10).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            f.at(p,P,TheListPeople.AT[D].getX()-1.5,0,TheListPeople.AT[D].getZ()+.5);p.setShiftKeyDown(true);p.setPose(net.minecraft.world.entity.Pose.CROUCHING);
            p.setYRot(270);p.setYHeadRot(270);sent(p);cues(p);
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.HELD,"taken hold of: "+where(l,b,D));
            p.setShiftKeyDown(false);p.setPose(net.minecraft.world.entity.Pose.STANDING);
            f.at(p,P,stand.x,0,stand.z-1.3);
        }).thenIdle(2).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.position().distanceTo(TheListTank.stand(b,wall,in))<=TheListTank.REACH,"held by the tank: "+where(l,b,D));
            sent(peer);cues(peer);cues(p);
            h.assertTrue(TheListTank.hand(p,b,wall),"the slasher's hand on the wall");
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.TANK&&deek.gesture()==TheListHunt.DUNKED&&TheListHunt.under(b,D),"forced over the rim, the head under: "+where(l,b,D));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.INTO_TANK,"Deek")),"the slasher is told");
            h.assertTrue(TheListTexts.TANK[D].equals(said(deek)),"his words: "+said(deek));
            h.assertTrue(deek.position().distanceTo(TheListTank.stand(b,wall,in))<.05,"pressed to the wall: "+deek.position().subtract(TheListTank.stand(b,wall,in)));
            h.assertTrue(TheListHunt.holding(p)&&LiteraryVignettes.retreatLocked(p),"still his, and no retreat");
            var got=sent(peer);
            h.assertTrue(sound(got,TheListRegistry.SCREAM.get())&&sound(got,TheListRegistry.DUNK.get()),"the other reader hears him go under");
            LiteraryVignettes.talk(peer,deek);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.TANK_HELD,"Deek")),"a hand on him: held under");
            TheListTank.hand(peer,b,wall);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.TANK_HELD,"Deek"))&&TheListHunt.under(b,D),"and on the wall: held under still");
        }).thenWaitUntil(()->h.assertFalse(TheListHunt.under(b,D),"waiting for the head to come up: "+where(l,b,D)))
        .thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.gesture()==TheListHunt.SURFACED&&TheListHunt.state(b,D)==TheListHunt.State.TANK,"the head up, gasping: "+where(l,b,D));
            h.assertTrue(cues(p).contains(TheListTexts.TANK_UP),"the slasher is told");
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(TheListHunt.under(b,D)&&TheListHunt.dunks(b,D)==2&&cues(p).contains(TheListTexts.TANK_AGAIN),"the slasher's hand puts it back under");
        }).thenWaitUntil(()->h.assertFalse(TheListHunt.under(b,D),"waiting for it to come up again: "+where(l,b,D)))
        .thenExecute(()->{
            cues(p);
            h.assertTrue(TheListTank.hand(p,b,wall)&&TheListHunt.under(b,D)&&TheListHunt.dunks(b,D)==3,"a hand on the wall puts it back a third time");
            h.assertTrue(cues(p).contains(TheListTexts.TANK_LAST),"and the slasher holds it there");
            TheListHunt.breath(b,D,8);sent(peer);cues(peer);
        }).thenWaitUntil(()->h.assertTrue(TheListHunt.dead(b,D),"waiting for him to drown: "+where(l,b,D)))
        .thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.gesture()==TheListHunt.SUNK&&(deek.marks()&TheListHunt.DROWNED)!=0,"slumped over the rim, drowned: "+where(l,b,D));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.TANK_STILL,"Deek")),"the slasher is told");
            h.assertTrue(cuesIn(sent(peer)).stream().anyMatch(c->c.startsWith("Somewhere to the")&&c.endsWith("a scream stops short.")),"the other reader is told a scream stopped");
            h.assertTrue((TheListBody.kills(p)&1<<D)!=0,"the slasher's record keeps him");
            h.assertFalse(TheListHunt.holding(p)||LiteraryVignettes.retreatLocked(p),"let go of: the body walks and may leave");
            TheListTank.hand(peer,b,wall);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.TANK_STILLWATER,"Deek")),"a hand on the wall: he is still");
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(cues(p).contains(String.format(TheListTexts.STILL_BODY,"Deek")),"a hand on him after: he does not move");
            var back=deek.position().add(0,.9,0).subtract(peer.getEyePosition());
            peer.setYRot((float)Math.toDegrees(Math.atan2(-back.x,back.z)));peer.setYHeadRot(peer.getYRot());
            peer.setXRot((float)-Math.toDegrees(Math.atan2(back.y,Math.sqrt(back.x*back.x+back.z*back.z))));cues(peer);
            TheListHunt.look(l,b);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.FOUND_TANK,"Deek")),"the other reader finds him at the tank");
        }).thenExecute(()->{
            TheListHunt.end(l,b);
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.marks()==0&&deek.gesture()==TheListHunt.STILL,"ended, he is back, unmarked: "+where(l,b,D));
            TheListBody.end(p);
            done();h.succeed();
        });
    }
    /**
     * 0.4.718, the gin's auger. Before the fourth night the gin is still: the slasher's hand on an auger with hold of Deek is told the
     * screw takes nobody, and the switch only clicks. On the fourth, the slasher puts him in feet first and lets go; the other reader hears
     * him go in, a hand on him or the auger finds him held in the screw, and while the line runs it draws him down and he cannot get
     * out. The other reader throws the switch: the screw stops drawing and he fights his way loose at twice a grip's pace, unless it is
     * thrown on again first. Drawn all the way down he dies there, the screw jams and the switch does nothing; the other reader is told
     * the scream stopped and finds him in the trough. Ended, the gin is put back.
     */
    @GameTest(template="empty",batch="the_list_auger",timeoutTicks=6000)
    public static void theAuger(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_auger",P);var peer=f.player("list_auger_peer",P);
        int D=TheListPeople.DEEK;var cell=new BlockPos(TheListGin.MID,0,TheListGin.RUN_S-4);
        hold(f,b,TheListPeople.AT[D],cell);
        h.startSequence().thenExecute(()->f.at(peer,P,TheListGin.SWITCH.getX()+.5,0,TheListGin.SWITCH.getZ()-1.5))
        .thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"waiting for the town");
            TheListPeople.ensure(l,b);
            for(int who:TheListBody.LISTED)h.assertTrue(TheListPeople.find(l,b,who)!=null,"waiting for "+TheListTexts.NAMES[who]);
        }).thenExecute(()->{
            h.assertTrue(TheListGin.auger(cell)&&!TheListGin.auger(cell.east())&&TheListGin.feeder(TheListGin.SWITCH),"the run and the switch");
            h.assertTrue(fitting(l,b,cell,Kind.GIN,6)&&l.getBlockState(b.offset(cell)).getValue(TheListBlock.FACING).getAxis()==net.minecraft.core.Direction.Axis.X,"the trough lies along the run");
            h.assertTrue(TheListBody.give(p),"the body is given");
            TheListHunt.start(l,b);
            h.assertFalse(TheListGin.running(b),"the gin is still on the first night");
            cues(peer);
            h.assertTrue(TheListGin.hand(peer,b,TheListGin.SWITCH)&&cues(peer).contains(TheListTexts.GIN_DARK),"the switch only clicks");
        }).thenIdle(10).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            f.at(p,P,TheListPeople.AT[D].getX()-1.5,0,TheListPeople.AT[D].getZ()+.5);p.setShiftKeyDown(true);p.setPose(net.minecraft.world.entity.Pose.CROUCHING);
            p.setYRot(270);p.setYHeadRot(270);sent(p);cues(p);
            LiteraryVignettes.talk(p,deek);
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.HELD,"taken hold of: "+where(l,b,D));
            p.setShiftKeyDown(false);p.setPose(net.minecraft.world.entity.Pose.STANDING);
            f.at(p,P,cell.getX()-1.5,0,cell.getZ()+.5);p.setYRot(270);p.setYHeadRot(270);
        }).thenIdle(2).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.position().distanceTo(TheListGin.in(b,cell,0))<=TheListGin.REACH,"held by the trough: "+where(l,b,D));
            cues(p);
            h.assertTrue(TheListGin.hand(p,b,cell)&&cues(p).contains(TheListTexts.GIN_NOT_TONIGHT),"not tonight");
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.HELD,"still held");
            TheListHunt.night(b,TheListGin.NIGHT);
            h.assertTrue(TheListGin.running(b),"the fourth night: the gin runs");
            sent(peer);cues(peer);cues(p);
            h.assertTrue(TheListGin.hand(p,b,cell),"the slasher's hand on the auger");
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.AUGER&&deek.gesture()==TheListHunt.AUGERED,"in the auger: "+where(l,b,D));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.INTO_AUGER,"Deek")),"the slasher is told");
            h.assertTrue(TheListTexts.AUGER[D].equals(said(deek)),"his words: "+said(deek));
            h.assertFalse(TheListHunt.holding(p)||LiteraryVignettes.retreatLocked(p),"let go of: the screw has him");
            var got=sent(peer);
            h.assertTrue(sound(got,TheListRegistry.SCREAM.get())&&sound(got,TheListRegistry.AUGER.get()),"the other reader hears him go in");
            LiteraryVignettes.talk(peer,deek);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.AUGER_HELD,"Deek")),"a hand on him: held in the screw");
            TheListGin.hand(peer,b,cell);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.AUGER_HELD,"Deek")),"and on the auger");
        }).thenIdle(20).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            int drawn=TheListHunt.drawn(b,D);
            h.assertTrue(drawn>=15&&TheListHunt.strengthIn(b,D)==0,"drawn down while it runs, no fighting out of it: "+drawn);
            h.assertTrue(deek.getY()<b.getY()+.06-.1,"sunk into the trough: "+where(l,b,D));
            h.assertTrue(TheListGin.hand(peer,b,TheListGin.SWITCH)&&cues(peer).contains(TheListTexts.GIN_OFF)&&!TheListGin.running(b),"the other reader throws the switch");
        }).thenIdle(6).thenExecute(()->{
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.AUGER&&TheListHunt.strengthIn(b,D)>0,"stopped, he fights his way up");
            int drawn=TheListHunt.drawn(b,D);
            h.assertTrue(TheListGin.hand(p,b,TheListGin.SWITCH)&&cues(p).contains(TheListTexts.GIN_ON)&&TheListGin.running(b),"the slasher throws it back on");
            TheListHunt.drawn(b,D,TheListGin.DRAW-3);sent(peer);cues(peer);
        }).thenWaitUntil(()->h.assertTrue(TheListHunt.dead(b,D),"waiting for the screw: "+where(l,b,D)))
        .thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.gesture()==TheListHunt.MANGLED&&TheListGin.jammed(b)&&!TheListGin.running(b),"slumped in the trough, the screw jammed: "+where(l,b,D));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.AUGER_STILL,"Deek")),"the slasher is told");
            h.assertTrue((TheListBody.kills(p)&1<<D)!=0,"the slasher's record keeps him");
            h.assertTrue(cuesIn(sent(peer)).stream().anyMatch(c->c.startsWith("Somewhere to the")&&c.endsWith("a scream stops short.")||c.contains("scream stops short")),"the other reader is told a scream stopped");
            TheListGin.hand(peer,b,TheListGin.SWITCH);
            h.assertTrue(cues(peer).contains(TheListTexts.GIN_JAMMED)&&!TheListGin.running(b),"jammed: the switch turns nothing");
            TheListGin.hand(peer,b,cell);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.AUGER_STILL_BODY,"Deek")),"a hand on the auger: he is still");
            var back=deek.position().add(0,1.15,0).subtract(peer.getEyePosition());
            peer.setYRot((float)Math.toDegrees(Math.atan2(-back.x,back.z)));peer.setYHeadRot(peer.getYRot());
            peer.setXRot((float)-Math.toDegrees(Math.atan2(back.y,Math.sqrt(back.x*back.x+back.z*back.z))));cues(peer);
            TheListHunt.look(l,b);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.FOUND_AUGER,"Deek")),"the other reader finds him in the auger");
        }).thenExecute(()->{
            TheListHunt.end(l,b);
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.marks()==0&&deek.gesture()==TheListHunt.STILL&&!TheListGin.jammed(b)&&!TheListGin.off(b),"ended, he is back and the gin put back: "+where(l,b,D));
            TheListBody.end(p);
            done();h.succeed();
        });
    }
    /**
     * 0.4.719, the gin's press. Before the fourth night the slasher's hand on it with hold of Deek is told it is shut down. On the fourth,
     * the slasher shoves Deek into its chamber and lets go: the chamber opens, the ram starts down, the other reader hears it; the
     * slasher's hand on the press finds him held, and the other reader's hand hauls the release, the ram goes back up, he comes out and
     * runs, and the chamber is shut again. Then, the hunt begun again, Wes, shoved in and left: bent double under it halfway, crushed when it comes all the
     * way down, the slasher told, the kill kept, the other reader told the scream stopped and finding him folded in the press. A hand on
     * the press then: still. Ended, the press is shut and Wes is back.
     */
    @GameTest(template="empty",batch="the_list_press",timeoutTicks=6000)
    public static void thePress(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_press",P);var peer=f.player("list_press_peer",P);
        int D=TheListPeople.DEEK,W=TheListPeople.WES;var press=TheListGin.PRESS;var up=press.above();
        hold(f,b,TheListPeople.AT[D],TheListPeople.AT[W],press);
        java.util.function.IntConsumer take=who->{
            var a=TheListPeople.find(l,b,who);
            f.at(p,P,TheListPeople.AT[who].getX()-1.5,0,TheListPeople.AT[who].getZ()+.5);p.setShiftKeyDown(true);p.setPose(net.minecraft.world.entity.Pose.CROUCHING);
            p.setYRot(270);p.setYHeadRot(270);sent(p);cues(p);
            LiteraryVignettes.talk(p,a);
            h.assertTrue(TheListHunt.state(b,who)==TheListHunt.State.HELD,"taken hold of: "+where(l,b,who));
            p.setShiftKeyDown(false);p.setPose(net.minecraft.world.entity.Pose.STANDING);
            f.at(p,P,press.getX()-1.0,0,press.getZ()-.5);
        };
        h.startSequence().thenExecute(()->f.at(peer,P,press.getX()+2.5,0,press.getZ()-1.5))
        .thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"waiting for the town");
            TheListPeople.ensure(l,b);
            for(int who:TheListBody.LISTED)h.assertTrue(TheListPeople.find(l,b,who)!=null,"waiting for "+TheListTexts.NAMES[who]);
        }).thenExecute(()->{
            h.assertTrue(fitting(l,b,press,Kind.GIN,4)&&fitting(l,b,up,Kind.GIN,5)&&TheListGin.press(press)&&TheListGin.press(up),"the press");
            h.assertTrue(TheListBody.give(p),"the body is given");
            TheListHunt.start(l,b);
        }).thenIdle(10).thenExecute(()->take.accept(D))
        .thenIdle(2).thenExecute(()->{
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(deek.position().distanceTo(TheListGin.chamber(b))<=TheListGin.REACH,"held by the press: "+where(l,b,D));
            cues(p);
            h.assertTrue(TheListGin.hand(p,b,press)&&cues(p).contains(TheListTexts.PRESS_NOT_TONIGHT)&&TheListHunt.state(b,D)==TheListHunt.State.HELD,"not tonight");
            TheListHunt.night(b,TheListGin.NIGHT);sent(peer);cues(peer);
            h.assertTrue(TheListGin.hand(p,b,press),"the slasher's hand on the press");
            h.assertTrue(TheListHunt.state(b,D)==TheListHunt.State.PRESS&&deek.gesture()==TheListHunt.PRESSED,"in the press: "+where(l,b,D));
            h.assertTrue(l.getBlockState(b.offset(press)).isAir()&&deek.position().distanceTo(TheListGin.chamber(b))<.05,"in the opened chamber: "+where(l,b,D));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.INTO_PRESS,"Deek"))&&TheListTexts.PRESS[D].equals(said(deek)),"the slasher told, his words");
            h.assertFalse(TheListHunt.holding(p)||LiteraryVignettes.retreatLocked(p),"let go of: the chamber has him");
            h.assertTrue(sound(sent(peer),TheListRegistry.SCREAM.get()),"the other reader hears him");
        }).thenIdle(10).thenExecute(()->{
            h.assertTrue(TheListHunt.ram(b,D)>=8,"the ram coming: "+TheListHunt.ram(b,D));
            TheListGin.hand(p,b,up);
            h.assertTrue(cues(p).contains(String.format(TheListTexts.PRESS_HELD,"Deek"))&&TheListHunt.state(b,D)==TheListHunt.State.PRESS,"the slasher's hand: held");
            h.assertTrue(TheListGin.hand(peer,b,up),"the other reader's hand on the press");
            var deek=TheListPeople.find(l,b,D);
            h.assertTrue(cues(peer).contains(String.format(TheListTexts.PRESS_RELEASE,"Deek"))&&TheListHunt.state(b,D)==TheListHunt.State.RUN,"released, he runs: "+where(l,b,D));
            h.assertTrue(fitting(l,b,press,Kind.GIN,4)&&deek.position().distanceTo(TheListGin.chamber(b))>.8,"out, and the chamber shut: "+where(l,b,D));
            // Begun again for the next: the rest heard Deek and ran.
            TheListHunt.start(l,b);TheListHunt.night(b,TheListGin.NIGHT);
            h.assertTrue(fitting(l,b,press,Kind.GIN,4),"the press shut for the next hunt");
        }).thenIdle(10).thenExecute(()->take.accept(W))
        .thenIdle(2).thenExecute(()->{
            h.assertTrue(TheListGin.hand(p,b,press)&&TheListHunt.state(b,W)==TheListHunt.State.PRESS,"Wes in the press: "+where(l,b,W));
            TheListHunt.ram(b,W,TheListGin.RAM/2-2);
        }).thenWaitUntil(()->h.assertTrue(TheListPeople.find(l,b,W).gesture()==TheListHunt.BENT,"waiting for him to bend under it"))
        .thenExecute(()->{TheListHunt.ram(b,W,TheListGin.RAM-3);sent(peer);cues(peer);cues(p);})
        .thenWaitUntil(()->h.assertTrue(TheListHunt.dead(b,W),"waiting for the ram: "+where(l,b,W)))
        .thenExecute(()->{
            var wes=TheListPeople.find(l,b,W);
            h.assertTrue(wes.gesture()==TheListHunt.FOLDED&&(wes.marks()&TheListHunt.CRUSH)!=0,"folded under the ram, crushed: "+where(l,b,W));
            h.assertTrue(cues(p).contains(String.format(TheListTexts.PRESS_STILL,"Wes"))&&(TheListBody.kills(p)&1<<W)!=0,"the slasher told, the kill kept");
            h.assertTrue(cuesIn(sent(peer)).stream().anyMatch(c->c.contains("scream stops short")),"the other reader is told a scream stopped");
            TheListGin.hand(peer,b,up);
            // Standing by the press, the other reader may already have come across him: what was told so far counts.
            var told=cues(peer);
            h.assertTrue(told.contains(String.format(TheListTexts.PRESS_STILL_BODY,"Wes"))&&TheListHunt.dead(b,W),"a hand on the press: still");
            var back=wes.position().add(0,.5,0).subtract(peer.getEyePosition());
            peer.setYRot((float)Math.toDegrees(Math.atan2(-back.x,back.z)));peer.setYHeadRot(peer.getYRot());
            peer.setXRot((float)-Math.toDegrees(Math.atan2(back.y,Math.sqrt(back.x*back.x+back.z*back.z))));
            TheListHunt.look(l,b);
            told.addAll(cues(peer));
            h.assertTrue(told.contains(String.format(TheListTexts.FOUND_PRESS,"Wes")),"the other reader finds him in the press: "+told);
        }).thenExecute(()->{
            TheListHunt.end(l,b);
            var wes=TheListPeople.find(l,b,W);
            h.assertTrue(fitting(l,b,press,Kind.GIN,4)&&wes.marks()==0&&wes.gesture()==TheListHunt.STILL,"ended, the press shut and Wes back: "+where(l,b,W));
            TheListBody.end(p);
            done();h.succeed();
        });
    }
    /** Whether these packets drew a particle of this kind. */
    private static boolean particles(List<net.minecraft.network.protocol.Packet<?>> packets,net.minecraft.core.particles.ParticleOptions kind){
        for(var packet:packets)if(packet instanceof net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket q&&q.getParticle().getType()==kind.getType())return true;
        return false;
    }
    /** A hand on a block where the reader stands, without moving them. */
    private static void hand(ServerPlayer p,BlockPos b,BlockPos rel){
        var at=b.offset(rel);
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.UP,at,false)));
    }
    /** Whether there is blood at least this heavy within a block of a point, at its own height. */
    private static boolean stain(ServerLevel l,Vec3 at,int stage){
        var c=BlockPos.containing(at.x,at.y+.1,at.z);
        for(var q:BlockPos.betweenClosed(c.offset(-1,0,-1),c.offset(1,0,1))){var st=l.getBlockState(q);if(TheListBlock.is(st,Kind.BLOOD)&&st.getValue(TheListBlock.STAGE)>=stage)return true;}
        return false;
    }
    /** The last blood sent for a reader in these packets: -1 if none was. */
    private static int marks(List<net.minecraft.network.protocol.Packet<?>> packets,ServerPlayer who){
        int last=-1;
        for(var packet:packets)if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c&&c.payload() instanceof io.github.knaitoe.theoldesthouse.network.TheListMarksPayload m&&m.player().equals(who.getUUID()))last=m.blood();
        return last;
    }

    /** The pages of a written book, as plain text. */
    private static List<String> pages(net.minecraft.world.item.ItemStack s){
        var out=new ArrayList<String>();var c=s.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT);
        if(c!=null)for(var page:c.pages())out.add(page.raw().getString());
        return out;
    }
    private static net.minecraft.world.item.ItemStack notebook(ServerPlayer p){
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(TheListDen.isNotebook(inv.getItem(i)))return inv.getItem(i);
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
    /**
     * 0.4.720: night two, as far as Amber's first test. Begun round the one who went in: ten o'clock, Amber in her den. The other reader's
     * hand on her back door finds it locked; the one who went in's opens it, and the den begins. Talked to, she humors them: the rules, the
     * notebook (theirs alone, the tapes' rules in it), the first test. The other reader is only told to keep it down. She goes; set down at
     * the field's south end zone, she waits until the one who went in is beside her, counts three and runs the field's length. Kept up
     * with, she says it is not fair and writes the first rule down: it is in their record and in the notebook they carry, and holding the
     * notebook is holding it. Ended, Amber's doors are shut and the night is gone.
     */
    @GameTest(template="empty",batch="the_list_den",timeoutTicks=6000)
    public static void nightTwoDen(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        var p=f.player("list_den",P);var peer=f.player("list_den_peer",P);
        hold(f,b,TheListDen.DEN_AT,BlockPos.containing(TheListDen.END_S),BlockPos.containing(TheListDen.END_N),new BlockPos(TheListSite.FIELD_X,0,TheListSite.FIELD_Z));
        List<String> seen=new ArrayList<>();
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);f.at(peer,P,-2.5,0,-3.5);cues(p);
            h.assertTrue(TheListDen.begin(p,b),"night two begins round the one who went in");
            h.assertTrue(TheListNight.phase(s,b)==TheListNight.HOME2&&TheListNight.time(s,b)==TheListNight.TEN,"ten o'clock, at home");
            h.assertTrue(cues(p).contains(TheListTexts.HOME2),"told where Amber said");
            h.assertTrue(TheListBody.has(p),"the body is theirs");
        }).thenWaitUntil(()->{TheListPeople.ensure(l,b);h.assertTrue(near(l,b,TheListPeople.AMBER,Vec3.atBottomCenterOf(TheListDen.DEN_AT)),"waiting for Amber in her den");})
        .thenExecute(()->{
            cues(peer);f.click(peer,P,TheListSite.AMBER_BACK);
            h.assertTrue(cues(peer).contains(TheListTexts.AMBER_DOOR)&&TheListNight.phase(s,b)==TheListNight.HOME2&&shut(l,b,TheListSite.AMBER_BACK),"the other reader's hand finds her back door locked");
            cues(p);f.click(p,P,TheListSite.AMBER_BACK);
            h.assertTrue(TheListNight.phase(s,b)==TheListNight.DEN&&!shut(l,b,TheListSite.AMBER_BACK),"the one who went in has it open: "+TheListNight.NAMES[TheListNight.phase(s,b)]);
            h.assertTrue(cues(p).contains(TheListTexts.BACK_DOOR_OPENS),"told so");
            var amber=TheListPeople.find(l,b,TheListPeople.AMBER);
            f.at(p,P,DEN_STAND.x,0,DEN_STAND.z);f.at(peer,P,DEN_STAND.x+1,0,DEN_STAND.z);
            LiteraryVignettes.talk(peer,amber);
            h.assertTrue(TheListTexts.DEN_OTHER.equals(said(amber))&&notebook(peer).isEmpty(),"the other reader is told to keep it down, and given nothing");
            LiteraryVignettes.talk(p,amber);h.assertTrue(TheListTexts.DEN[1].equals(said(amber)),"the rules: "+said(amber));
            cues(p);LiteraryVignettes.talk(p,amber);
            h.assertTrue(TheListTexts.DEN[2].equals(said(amber))&&cues(p).contains(TheListTexts.NOTEBOOK_GIVEN),"the notebook");
            var book=notebook(p);
            h.assertTrue(!book.isEmpty()&&pages(book).size()==1&&pages(book).get(0).contains("from the tapes"),"the notebook is theirs, the tapes' rules in it: "+pages(book));
            LiteraryVignettes.talk(p,amber);h.assertTrue(TheListTexts.DEN[3].equals(said(amber))&&TheListNight.phase(s,b)==TheListNight.DEN,"the first test");
            LiteraryVignettes.talk(p,amber);
            h.assertTrue(TheListNight.phase(s,b)==TheListNight.TO_FIELD,"she sets off: "+TheListNight.NAMES[TheListNight.phase(s,b)]);
            LiteraryVignettes.talk(p,amber);h.assertTrue(TheListTexts.ON_THE_WAY.equals(said(amber)),"on the way she says where");
            h.assertTrue(f.own(p,P).getBoolean("Notebook"),"the record keeps the notebook given");
        }).thenWaitUntil(()->{var amber=TheListPeople.find(l,b,TheListPeople.AMBER);
            h.assertTrue(amber!=null&&amber.position().distanceTo(Vec3.atBottomCenterOf(b.offset(TheListDen.DEN_AT)))>2,"waiting for Amber to walk off");})
        .thenExecute(()->{
            TheListDen.atField(l,b);
            h.assertTrue(TheListNight.phase(s,b)==TheListNight.FIELD,"at the field");
            var end=TheListDen.END_S;f.at(p,P,end.x+2,0,end.z);
        }).thenWaitUntil(()->h.assertTrue(TheListDen.running(b),"waiting for her count and the run"))
        .thenWaitUntil(()->{
            var amber=TheListPeople.find(l,b,TheListPeople.AMBER);
            if(amber!=null&&TheListNight.phase(s,b)==TheListNight.FIELD){var at=amber.position().subtract(Vec3.atLowerCornerOf(b));f.at(p,P,at.x+1.5,0,at.z+2.5);}
            seen.addAll(cues(p));
            h.assertTrue(TheListNight.phase(s,b)==TheListNight.RAN,"waiting for the far end, kept up with: "+(amber==null?"nowhere":amber.position().subtract(Vec3.atLowerCornerOf(b))));
        }).thenExecute(()->{
            seen.addAll(cues(p));
            var amber=TheListPeople.find(l,b,TheListPeople.AMBER);
            h.assertTrue(amber!=null&&amber.position().subtract(Vec3.atLowerCornerOf(b)).distanceTo(TheListDen.END_N)<1,"she ran the field's length");
            h.assertTrue(TheListTexts.RAN_KEPT.equals(said(amber))&&seen.contains(TheListTexts.RULE_WRITTEN),"not fair, and the rule written: "+said(amber)+" / "+seen);
            h.assertTrue(TheListDen.rules(p)==1,"the record keeps the first rule");
            var book=notebook(p);
            h.assertTrue(pages(book).size()==2&&pages(book).get(1).contains(TheListTexts.RULES[0].substring(0,20)),"the notebook they carry has it: "+pages(book));
            var inv=p.getInventory();int slot=inv.findSlotMatchingItem(book);
            var kept=inv.getItem(20).copy();inv.setItem(20,book.copy());inv.setItem(slot,kept);
            h.assertFalse(TheListDen.holding(p),"packed away, it is not held");
            inv.selected=0;var first=inv.getItem(0).copy();inv.setItem(0,inv.getItem(20).copy());inv.setItem(20,first);
            h.assertTrue(TheListDen.holding(p),"in hand it is held");
            LiteraryVignettes.talk(p,amber);h.assertTrue(TheListTexts.NEXT_DOOR.equals(said(amber)),"the next test is the field house's door");
            TheListNight.end(l,b);
            h.assertTrue(TheListNight.phase(s,b)==TheListNight.NONE&&shut(l,b,TheListSite.AMBER_BACK)&&shut(l,b,TheListSite.AMBER_FRONT),"ended, her doors shut");
            TheListBody.end(p);
            done();h.succeed();
        });
    }
    /**
     * 0.4.722, the rest of night two's tests. The door: Amber goes in through the field house's back doors and bolts them once the one
     * who went in is outside; nobody's hand opens them, and the fifth blow springs one, and the second rule is written. The heart: sent
     * back out, she goes into one of the two rooms behind the block wall and bangs a locker; coming into the other room first writes the
     * wrong rule, that he cannot tell whose heart it is. The tower: up on the catwalk and off it, the body lands, goes down and gets up,
     * and the last rule is written, wrong too. Only then does the first name go out.
     */
    @GameTest(template="empty",batch="the_list_tests",timeoutTicks=8000)
    public static void nightTwoTests(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        var p=f.player("list_tests",P);
        hold(f,b,TheListDen.DEN_AT,BlockPos.containing(TheListDen.END_N),BlockPos.containing(TheListDen.BEHIND),BlockPos.containing(TheListDen.UNDER));hold(f,b,TheListPeople.AT);
        List<String> seen=new ArrayList<>();int[] room={-1};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);
            h.assertTrue(TheListDen.begin(p,b),"night two begins round the one who went in");
        }).thenWaitUntil(()->{TheListPeople.ensure(l,b);h.assertTrue(TheListPeople.find(l,b,TheListPeople.AMBER)!=null,"waiting for Amber");})
        .thenExecute(()->{
            TheListDen.toTests(p,b);
            h.assertTrue(TheListDen.rules(p)==1&&TheListDen.test(TheListDen.rules(p))==TheListDen.DOOR,"after the field, the door is next");
            TheListDen.atTest(l,b,TheListDen.DOOR);
            f.at(p,P,TheListDen.BEHIND.x,0,TheListDen.BEHIND.z+1);cues(p);
        }).thenWaitUntil(()->{seen.addAll(cues(p));h.assertTrue(TheListDen.bolted(b,TheListDen.BACK_L)==false&&seen.contains(TheListTexts.DOOR_BOLTED)==false,"not while they are inside");})
        .thenIdle(30)
        .thenExecute(()->{
            seen.addAll(cues(p));
            h.assertFalse(TheListDen.bolted(b,TheListDen.BACK_L),"she waits for them to be outside");
            f.at(p,P,TheListDen.BEHIND.x,0,TheListSite.FIELD_HOUSE_S+3.5);
        }).thenWaitUntil(()->{seen.addAll(cues(p));h.assertTrue(TheListDen.bolted(b,TheListDen.BACK_L)&&TheListDen.bolted(b,TheListDen.BACK_R),"waiting for the bolt");})
        .thenExecute(()->{
            seen.addAll(cues(p));
            h.assertTrue(seen.contains(TheListTexts.DOOR_BOLTED)&&shut(l,b,TheListDen.BACK_L)&&shut(l,b,TheListDen.BACK_R),"both doors shut and bolted, and they hear it");
            f.click(p,P,TheListDen.BACK_L);
            h.assertTrue(shut(l,b,TheListDen.BACK_L),"a hand does not open it");
        }).thenIdle(10).thenExecute(()->f.click(p,P,TheListDen.BACK_L))
        .thenIdle(10).thenExecute(()->f.click(p,P,TheListDen.BACK_L))
        .thenIdle(10).thenExecute(()->f.click(p,P,TheListDen.BACK_L))
        .thenIdle(10).thenExecute(()->{
            h.assertTrue(shut(l,b,TheListDen.BACK_L),"four blows do not do it");
            f.click(p,P,TheListDen.BACK_L);
        }).thenWaitUntil(()->h.assertTrue((TheListDen.rules(p)&1<<TheListDen.RULE_DOOR)!=0,"waiting for the second rule: "+TheListDen.rules(p)))
        .thenExecute(()->{
            var amber=TheListPeople.find(l,b,TheListPeople.AMBER);
            h.assertTrue(!shut(l,b,TheListDen.BACK_L)&&TheListTexts.DOOR_DONE.equals(said(amber)),"the fifth springs it, and she counted: "+said(amber));
            f.at(p,P,TheListDen.BEHIND.x,0,TheListSite.FIELD_HOUSE_S+3.5);cues(p);
        }).thenWaitUntil(()->{seen.addAll(cues(p));room[0]=TheListDen.heartRoom(b);h.assertTrue(TheListDen.knocked(b),"waiting for her to hide and bang a locker");})
        .thenExecute(()->{
            seen.addAll(cues(p));
            h.assertTrue(seen.contains(TheListTexts.HEART_KNOCK)&&room[0]>=0,"a locker bangs inside");
            var amber=TheListPeople.find(l,b,TheListPeople.AMBER);
            h.assertTrue(TheListDen.room(amber.position().subtract(Vec3.atLowerCornerOf(b)))==room[0],"she is in the room she chose");
            // The other room first: the wrong guess.
            if(room[0]==0)f.at(p,P,TheListSite.FIELD_X+6.5,0,TheListSite.FIELD_HOUSE_S-1.5);else f.at(p,P,TheListSite.FIELD_X+6.5,0,TheListSite.FIELD_HOUSE_S-5.5);
        }).thenWaitUntil(()->h.assertTrue((TheListDen.rules(p)&1<<TheListDen.RULE_NO_HEART)!=0,"waiting for the wrong rule: "+TheListDen.rules(p)))
        .thenExecute(()->{
            var amber=TheListPeople.find(l,b,TheListPeople.AMBER);
            h.assertTrue(TheListTexts.HEART_WRONG.equals(said(amber))&&(TheListDen.rules(p)&1<<TheListDen.RULE_HEART)==0,"wrong room, so he can't tell: "+said(amber));
            h.assertTrue(TheListNight.phase(s,b)==TheListNight.RAN,"no name out yet");
        }).thenIdle(45).thenExecute(()->{
            TheListDen.atTest(l,b,TheListDen.TOWER);
            f.at(p,P,TheListSite.TOWER_X+.5,TheListSite.CATWALK_Y+.2,TheListSite.TOWER_Z+3.5);
        }).thenWaitUntil(()->h.assertTrue(TheListDen.climbed(b),"waiting for her to see them up on the catwalk"))
        // Off the side: a test player is not moved by the game, so it is carried through the fall and set on the ground.
        .thenExecute(()->{f.at(p,P,TheListSite.TOWER_X+.5,12,TheListSite.TOWER_Z+5.5);p.setOnGround(false);})
        .thenIdle(3)
        .thenExecute(()->{f.at(p,P,TheListSite.TOWER_X+.5,0,TheListSite.TOWER_Z+5.5);p.setOnGround(true);})
        .thenWaitUntil(()->h.assertTrue(TheListBody.down(p)>0,"waiting for the body to go down on landing"))
        .thenWaitUntil(()->h.assertTrue((TheListDen.rules(p)&1<<TheListDen.RULE_TOWER)!=0,"waiting for them to get up and the last rule"))
        .thenExecute(()->{
            var amber=TheListPeople.find(l,b,TheListPeople.AMBER);
            h.assertTrue(TheListTexts.TOWER_DONE.equals(said(amber))&&TheListBody.has(p)&&p.isAlive(),"down and up, and higher would be longer: "+said(amber));
            var book=notebook(p);var tested=String.join("\n",pages(book).subList(1,pages(book).size()));
            h.assertTrue(pages(book).size()==3,"four rules on two pages: "+pages(book));
            h.assertTrue(tested.contains(TheListTexts.RULES[1].substring(0,20))&&tested.contains(TheListTexts.RULES[3].substring(0,20))&&tested.contains(TheListTexts.RULES[4].substring(0,20))
                &&!tested.contains(TheListTexts.RULES[2].substring(0,20)),"the notebook they carry has the four, the wrong ones too: "+tested);
            h.assertTrue(TheListDen.test(TheListDen.rules(p))==TheListDen.DONE,"every test had");
        }).thenWaitUntil(()->h.assertTrue(TheListNight.phase(s,b)==TheListNights.HUNT2,"waiting for the first name: "+TheListNight.NAMES[TheListNight.phase(s,b)]))
        .thenExecute(()->{
            TheListNight.end(l,b);TheListBody.end(p);
            done();h.succeed();
        });
    }
    /**
     * 0.4.723, part 6a: what comes back, and what the town knows. Night three begins: the flyers are up along Main Street and the reader
     * is told of the one on the kitchen window. The hay hook, which on night one could not be let go of, can be thrown away now: it is
     * not given back to the hand. Out in the street it stays away; in the house, not looking at the bed, the thrown one is gone and the
     * hook is on the bed, and looking at the bed they are told. Taken up again, it is theirs as before. Ended, the flyers come down.
     */
    @GameTest(template="empty",batch="the_list_back",timeoutTicks=4000)
    public static void whatComesBack(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        var p=f.player("list_back",P);
        hold(f,b,TheListSite.BED_HEAD,TheListDen.DEN_AT);
        for(int x=-48;x<=66;x+=24)hold(f,b,new BlockPos(x,0,TheListSite.MAIN_LINE));
        Vec3[] bed={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);cues(p);
            h.assertTrue(TheListNights.begin(p,b,3),"night three begins round the one who went in");
            h.assertTrue(TheListNight.phase(s,b)==TheListNights.HUNT3,"night three");
            var said=cues(p);
            h.assertTrue(said.contains(TheListTexts.NIGHT3)&&said.contains(TheListTexts.FLYERS),"the hook on the table, and the flyer on the window: "+said);
            h.assertTrue(TheListFlyers.up(l,b)>=4,"the flyers are up along Main Street: "+TheListFlyers.up(l,b));
            bed[0]=Vec3.atLowerCornerOf(b.offset(TheListSite.BED_HEAD)).add(.5,.7,.5);
        }).thenWaitUntil(()->h.assertTrue(TheListBody.hookInHand(p)||hook(p)>=0,"waiting for the hook in hand"))
        .thenExecute(()->{
            // Out in the street, thrown away.
            f.at(p,P,0.5,0,-28.5);
            var inv=p.getInventory();inv.selected=hook(p);
            p.drop(false);
            h.assertTrue(hook(p)<0&&TheListBody.hookAway(p),"thrown away, it is not in their hands: "+hook(p));
        }).thenIdle(45)
        .thenExecute(()->{
            h.assertTrue(hook(p)<0&&TheListBody.hookAway(p),"out of the house, it is not given back");
            // In the bedroom, looking the other way.
            f.at(p,P,7.5,0,-3.5);face(p,bed[0],true);
        }).thenWaitUntil(()->h.assertTrue(TheListBody.hookOnBed(p)==1,"waiting for it to be on the bed"))
        .thenExecute(()->{
            var thrown=l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(b).inflate(200),
                i->i.getItem().is(TheListRegistry.HAY_HOOK.get()));
            h.assertTrue(thrown.size()==1&&thrown.get(0).position().distanceTo(bed[0])<1,"the one in the street is gone; one lies on the bed: "+thrown.size());
            h.assertTrue(hook(p)<0,"and not in their hands");
            cues(p);face(p,bed[0],false);
        }).thenWaitUntil(()->h.assertTrue(TheListBody.hookOnBed(p)==2,"waiting for them to see it"))
        .thenExecute(()->{
            h.assertTrue(cues(p).contains(TheListTexts.HOOK_BACK),"told it is on the bed");
            for(var e:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(b).inflate(200),i->i.getItem().is(TheListRegistry.HAY_HOOK.get()))){
                p.getInventory().add(e.getItem().copy());e.discard();
            }
        }).thenWaitUntil(()->h.assertTrue(!TheListBody.hookAway(p)&&TheListBody.hookOnBed(p)==0,"waiting for it to be theirs again"))
        .thenIdle(30)
        .thenExecute(()->{
            int n=0;var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(TheListRegistry.HAY_HOOK.get()))n++;
            h.assertTrue(n==1,"one hook, not two: "+n);
            TheListNight.end(l,b);TheListBody.end(p);
            h.assertTrue(TheListFlyers.up(l,b)==0,"ended, the flyers are down");
            done();h.succeed();
        });
    }
    /**
     * 0.4.724, part 6b: the deputy out on his round. Night three begun, he leaves his place with a flashlight in his hand and walks; its
     * beam is a light ahead of him. Another reader near him in the street is told there is a curfew, once. The one who went in, put in
     * front of him, is seen: he calls out, keeps the light on them, and fires; the body goes down. Out of his sight, he walks on. At first
     * light he is back at his place and the beam is out.
     */
    @GameTest(template="empty",batch="the_list_deputy",timeoutTicks=4000)
    public static void theDeputy(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        var p=f.player("list_deputy",P);var peer=f.player("list_deputy_peer",P);
        hold(f,b,TheListDen.DEN_AT,TheListSite.BED_HEAD);hold(f,b,TheListPeople.AT);
        for(int x=-48;x<=48;x+=24)hold(f,b,new BlockPos(x,0,TheListSite.WALK_S_S));
        BlockPos[] start={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);f.at(peer,P,-2.5,0,-3.5);
            h.assertTrue(TheListNights.begin(p,b,3),"night three begins");
            h.assertTrue(TheListDeputy.on(s,b),"the deputy is out");
        }).thenWaitUntil(()->{TheListPeople.ensure(l,b);var d=TheListPeople.find(l,b,TheListPeople.DEPUTY);
            h.assertTrue(d!=null&&TheListDeputy.step(b)>=0,"waiting for the deputy on his round");
            start[0]=d.blockPosition();})
        .thenWaitUntil(()->{var d=TheListPeople.find(l,b,TheListPeople.DEPUTY);
            h.assertTrue(d!=null&&d.blockPosition().distManhattan(start[0])>=3,"waiting for him to walk off: "+(d==null?"nowhere":d.blockPosition().subtract(b)));
            h.assertTrue(d.getMainHandItem().is(io.github.knaitoe.theoldesthouse.labyrinth.BorrascaRegistry.FLASHLIGHT.get()),"a flashlight in his hand");})
        .thenExecute(()->{
            var beam=TheListDeputy.beamAt(b);
            h.assertTrue(beam!=null&&l.getBlockState(beam).is(net.minecraft.world.level.block.Blocks.LIGHT),"its beam is a light ahead of him");
            // Another reader just in front of him in the street.
            var d=TheListPeople.find(l,b,TheListPeople.DEPUTY);var look=Vec3.directionFromRotation(0,d.getYHeadRot());
            var at=d.position().add(look.scale(3)).subtract(Vec3.atLowerCornerOf(b));
            f.at(peer,P,at.x,0,at.z);
        }).thenWaitUntil(()->h.assertTrue(said(TheListPeople.find(l,b,TheListPeople.DEPUTY)).equals(TheListTexts.DEPUTY_CURFEW),"waiting for the curfew"))
        .thenExecute(()->{
            f.at(peer,P,-2.5,0,-3.5);
            var d=TheListPeople.find(l,b,TheListPeople.DEPUTY);var look=Vec3.directionFromRotation(0,d.getYHeadRot());
            var at=d.position().add(look.scale(8)).subtract(Vec3.atLowerCornerOf(b));
            f.at(p,P,at.x,0,at.z);cues(p);
        }).thenWaitUntil(()->h.assertTrue(TheListDeputy.aiming(b),"waiting for him to see them"))
        .thenExecute(()->h.assertTrue(cues(p).contains(TheListTexts.DEPUTY_SEEN),"the flashlight finds them"))
        .thenWaitUntil(()->h.assertTrue(TheListDeputy.fired(b)&&TheListBody.down(p)>0,"waiting for the shot"))
        .thenExecute(()->{
            h.assertTrue(p.isAlive()&&TheListBody.has(p),"down, not dead");
            f.at(p,P,-3.5,0,-3.5);
        }).thenWaitUntil(()->h.assertTrue(!TheListDeputy.aiming(b),"waiting for him to walk on"))
        .thenExecute(()->{
            TheListNights.dawn(l,b);TheListPeople.ensure(l,b);
            h.assertFalse(TheListDeputy.on(s,b),"first light: his round is over");
            var d=TheListPeople.find(l,b,TheListPeople.DEPUTY);
            h.assertTrue(d!=null&&d.blockPosition().subtract(b).equals(TheListPeople.AT[TheListPeople.DEPUTY]),"he is back at his place");
            h.assertTrue(TheListDeputy.beamAt(b)==null,"the beam is out");
            TheListNight.end(l,b);TheListBody.end(p);
            done();h.succeed();
        });
    }
    /**
     * 0.4.725, part 6c: night three, the five together in the field house. Begun, each of them stands in the locker room, both of its
     * doors are bolted and the field's lights are on. The back door gives at the fifth blow; the one who went in in the doorway is heard,
     * and they scream and run, each to their first hiding place inside. First light: the lights are out and they are back at their places.
     */
    @GameTest(template="empty",batch="the_list_field_house",timeoutTicks=4000)
    public static void theFieldHouse(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        var p=f.player("list_field_house",P);
        hold(f,b,TheListDen.DEN_AT,TheListSite.BED_HEAD);hold(f,b,TheListPeople.AT);hold(f,b,huddle());
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);
            h.assertTrue(TheListNights.begin(p,b,3),"night three begins");
            h.assertTrue(cues(p).contains(TheListTexts.FIELD_LIT),"told the field's lights are on");
        }).thenWaitUntil(()->{TheListPeople.ensure(l,b);
            for(int who:TheListBody.LISTED)h.assertTrue(TheListHunt.state(b,who)==TheListHunt.State.CALM&&near(l,b,who,Vec3.atBottomCenterOf(TheListHuddle.AT[who])),"waiting for "+TheListTexts.NAMES[who]+" in the field house");})
        .thenExecute(()->{
            for(var d:new BlockPos[]{TheListHunt.FIELD_DOOR,TheListHunt.FIELD_DOOR.east(),TheListHuddle.STREET_DOOR})h.assertTrue(TheListHunt.bolted(b,d)&&shut(l,b,d),"the field house's doors are bolted: "+d);
            h.assertTrue(TheListHuddle.lit(l,b),"the field's lights are on");
            f.at(p,P,TheListHunt.FIELD_DOOR.getX()+.5,0,TheListHunt.FIELD_DOOR.getZ()+2.5);
        }).thenWaitUntil(()->h.assertTrue(TheListBody.live(p),"waiting for the body to be the reader's in the street"))
        .thenExecute(()->{
            h.assertTrue(TheListBody.strike(p,b,TheListHunt.FIELD_DOOR),"the first blow: has "+TheListBody.has(p)+", live "+TheListBody.live(p)+", town "+TheListBody.town(p)+", at "+p.position().subtract(Vec3.atLowerCornerOf(b))+", down "+TheListBody.down(p)+", door "+l.getBlockState(b.offset(TheListHunt.FIELD_DOOR)));
        }).thenIdle(TheListBody.STRIKE_GAP).thenExecute(()->TheListBody.strike(p,b,TheListHunt.FIELD_DOOR))
        .thenIdle(TheListBody.STRIKE_GAP).thenExecute(()->TheListBody.strike(p,b,TheListHunt.FIELD_DOOR))
        .thenIdle(TheListBody.STRIKE_GAP).thenExecute(()->TheListBody.strike(p,b,TheListHunt.FIELD_DOOR))
        .thenIdle(TheListBody.STRIKE_GAP).thenExecute(()->{
            TheListBody.strike(p,b,TheListHunt.FIELD_DOOR);
            h.assertFalse(shut(l,b,TheListHunt.FIELD_DOOR),"the back door gives at the fifth blow");
            // In the doorway, close enough to be heard.
            f.at(p,P,TheListHunt.FIELD_DOOR.getX()+.5,0,TheListHunt.FIELD_DOOR.getZ()+.5);
        }).thenWaitUntil(()->h.assertTrue(TheListHunt.state(b,TheListPeople.SHANNON)!=TheListHunt.State.CALM,"waiting for them to hear"))
        .thenExecute(()->f.at(p,P,TheListHunt.FIELD_DOOR.getX()+.5,0,TheListHunt.FIELD_DOOR.getZ()+5.5))
        .thenWaitUntil(()->{for(int who:TheListBody.LISTED){var a=TheListPeople.find(l,b,who);
            h.assertTrue(TheListHunt.state(b,who)==TheListHunt.State.HIDE&&TheListHunt.way(b,who)==0&&near(l,b,who,TheListHuddle.hidingPlace(who,0)),"waiting for "+TheListTexts.NAMES[who]+" to hide: "+TheListHunt.state(b,who)+(a==null?"":" at "+a.position().subtract(Vec3.atLowerCornerOf(b))));}})
        .thenExecute(()->{
            TheListNights.dawn(l,b);TheListPeople.ensure(l,b);
            h.assertFalse(TheListHuddle.lit(l,b),"first light: the field's lights are out");
            var deek=TheListPeople.find(l,b,TheListPeople.DEEK);
            h.assertTrue(deek!=null&&deek.blockPosition().subtract(b).equals(TheListPeople.AT[TheListPeople.DEEK]),"they are back at their places");
            h.assertTrue(TheListNight.phase(s,b)==TheListNights.DONE3,"night three is over");
            TheListNight.end(l,b);TheListBody.end(p);
            done();h.succeed();
        });
    }
    /**
     * 0.4.726, part 7a: night four, Stace's plan. Begun with all five alive, it ends at the gin: she is there, the one who went in is
     * told and the House's door is shut to them. Brought to her, they are put down; up again, put down again; the third time she
     * finishes it, the night is over and the four nights end stopped.
     */
    @GameTest(template="empty",batch="the_list_stace",timeoutTicks=6000)
    public static void stacesPlan(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        var p=f.player("list_stace",P);
        hold(f,b,TheListDen.DEN_AT,TheListSite.BED_HEAD,TheListSite.ENDING,TheListSite.SOURCE);hold(f,b,TheListPeople.AT);
        hold(f,b,BlockPos.containing(TheListStace.at(b,TheListStace.GIN)),BlockPos.containing(TheListStace.at(b,TheListStace.TOWER)),BlockPos.containing(TheListStace.at(b,TheListStace.PLAYA)));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);
            h.assertTrue(TheListNights.begin(p,b,4),"night four begins");
            h.assertFalse(TheListNight.locked(p),"before her plan the House's door is open");
        }).thenWaitUntil(()->{TheListPeople.ensure(l,b);h.assertTrue(TheListPeople.find(l,b,TheListPeople.STACE)!=null,"waiting for Stace");})
        .thenExecute(()->{
            cues(p);TheListStace.planNow(l,b);
            h.assertTrue(TheListStace.where(b)==TheListStace.GIN,"with all five alive it ends at the gin");
            h.assertTrue(cues(p).contains(TheListTexts.STACE_PLAN[TheListStace.GIN]),"they are told where");
            h.assertTrue(TheListNight.locked(p),"from her plan, the House's door is shut to them");
            h.assertTrue(TheListStace.stage(b)==TheListStace.CATWALK,"she sets off from the catwalk");
        }).thenWaitUntil(()->{TheListPeople.ensure(l,b);h.assertTrue(TheListStace.stage(b)==TheListStace.LADDER,"waiting for her at the ladder: stage "+TheListStace.stage(b));})
        .thenWaitUntil(()->h.assertTrue(TheListStace.stage(b)==TheListStace.THERE,"waiting for her to come down and walk to the gin: stage "+TheListStace.stage(b)))
        .thenExecute(()->{
            var st=TheListPeople.find(l,b,TheListPeople.STACE);
            var spot=Vec3.atLowerCornerOf(b).add(TheListStace.at(b,TheListStace.GIN));
            h.assertTrue(st!=null&&st.position().subtract(spot).horizontalDistance()<1.5,"she is at the gin: "+(st==null?"nowhere":st.position().subtract(Vec3.atLowerCornerOf(b))));
            h.assertTrue(st.getMainHandItem().is(net.minecraft.world.item.Items.IRON_SHOVEL),"with the shovel");
            var at=st.position().subtract(Vec3.atLowerCornerOf(b));
            f.at(p,P,at.x+1.5,at.y,at.z);
        }).thenWaitUntil(()->h.assertTrue(TheListStace.blows(b)>=1&&TheListBody.down(p)>0,"waiting for her first blow"))
        .thenExecute(()->h.assertTrue(cues(p).contains(TheListTexts.STACE_HIT)&&p.isAlive(),"put down, not dead"))
        .thenWaitUntil(()->h.assertTrue(TheListStace.blows(b)>=2,"waiting for the second, once they are up: down "+TheListBody.down(p)))
        .thenWaitUntil(()->h.assertTrue(TheListNight.phase(s,b)==TheListNights.OVER,"waiting for the third: blows "+TheListStace.blows(b)+", down "+TheListBody.down(p)))
        .thenExecute(()->{
            h.assertTrue(cues(p).contains(TheListTexts.STACE_FINISHED),"she finishes it");
            var own=f.own(p,P);
            h.assertTrue(own.getBoolean("Ready")&&"was_stopped".equals(own.getString("Outcome")),"the four nights end stopped: "+own.getString("Outcome"));
            h.assertFalse(TheListStace.on(b),"her plan is put away");
            TheListNight.end(l,b);TheListBody.end(p);
            done();h.succeed();
        });
    }
    /**
     * 0.4.727, part 7b: Stace fought. Her plan begun, she comes down from the tower; struck by the one who went in she staggers twice
     * and goes down at the third blow; a blow with the hay hook in hand there finishes her, and their own record keeps it. At first
     * light she is stood up at her place again.
     */
    @GameTest(template="empty",batch="the_list_stace_fought",timeoutTicks=6000)
    public static void staceFought(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        var p=f.player("list_stace_fought",P);
        hold(f,b,TheListDen.DEN_AT,TheListSite.BED_HEAD,TheListSite.ENDING,TheListSite.SOURCE);hold(f,b,TheListPeople.AT);
        hold(f,b,BlockPos.containing(TheListStace.at(b,TheListStace.GIN)));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{f.at(p,P,-3.5,0,-3.5);h.assertTrue(TheListNights.begin(p,b,4),"night four begins");})
        .thenWaitUntil(()->{TheListPeople.ensure(l,b);h.assertTrue(TheListPeople.find(l,b,TheListPeople.STACE)!=null,"waiting for Stace");})
        .thenExecute(()->TheListStace.planNow(l,b))
        .thenWaitUntil(()->h.assertTrue(TheListStace.stage(b)>=TheListStace.WALKING,"waiting for her to come down the ladder: stage "+TheListStace.stage(b)))
        .thenExecute(()->{
            var st=TheListPeople.find(l,b,TheListPeople.STACE);var at=st.position().subtract(Vec3.atLowerCornerOf(b));
            // Behind her, at a distance she will not close on before the blows land.
            f.at(p,P,at.x-1.5,at.y,at.z);int slot=hook(p);if(slot>=0)p.getInventory().selected=slot;cues(p);
        }).thenWaitUntil(()->h.assertTrue(TheListBody.live(p),"waiting for the body to be the reader's"))
        .thenExecute(()->{TheListHunt.struck(p,TheListPeople.find(l,b,TheListPeople.STACE));h.assertTrue(TheListStace.taken(b)==1&&cues(p).contains(TheListTexts.STACE_STAGGERS),"the first blow staggers her");})
        .thenIdle(12).thenExecute(()->TheListHunt.struck(p,TheListPeople.find(l,b,TheListPeople.STACE)))
        .thenIdle(12).thenExecute(()->{
            TheListHunt.struck(p,TheListPeople.find(l,b,TheListPeople.STACE));
            h.assertTrue(TheListStace.stage(b)==TheListStace.DOWN&&cues(p).contains(TheListTexts.STACE_FALLS),"the third puts her down: stage "+TheListStace.stage(b));
            h.assertTrue(TheListBody.hookInHand(p),"the hook in hand");
            TheListHunt.struck(p,TheListPeople.find(l,b,TheListPeople.STACE));
            h.assertTrue(TheListStace.stage(b)==TheListStace.DEAD&&cues(p).contains(TheListTexts.STACE_KILLED)&&TheListStace.dead(p),"with the hook, finished; their record keeps it");
            h.assertTrue(TheListNight.phase(s,b)==TheListNights.HUNT4,"the night goes on");
            TheListNights.dawn(l,b);TheListPeople.ensure(l,b);
            var st=TheListPeople.find(l,b,TheListPeople.STACE);
            h.assertTrue(st!=null&&st.marks()==0&&st.blockPosition().subtract(b).equals(TheListPeople.AT[TheListPeople.STACE]),"at first light she is back at her place");
            h.assertTrue(TheListStace.dead(p),"and it is still theirs");
            TheListNight.end(l,b);TheListBody.end(p);
            done();h.succeed();
        });
    }
    /** Where the five stand in the field house on night three. */
    private static BlockPos[] huddle(){var out=new java.util.ArrayList<BlockPos>();for(var a:TheListHuddle.AT)if(a!=null)out.add(a);return out.toArray(BlockPos[]::new);}
    /** Which inventory slot holds the hay hook, or -1. */
    private static int hook(ServerPlayer p){var inv=p.getInventory();for(int i=0;i<9;i++)if(inv.getItem(i).is(TheListRegistry.HAY_HOOK.get()))return i;return -1;}
    /** The reader turned toward a point, or away from it. */
    private static void face(ServerPlayer p,Vec3 at,boolean away){
        var d=at.subtract(p.getEyePosition());float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z))+(away?180:0);
        p.setYRot(yaw);p.setYHeadRot(yaw);p.setXRot(away?0:(float)-Math.toDegrees(Math.atan2(d.y,Math.hypot(d.x,d.z))));
    }
    /**
     * 0.4.721, the skeleton to its end: the first name goes out after the field and night two's hunt begins; the one who went in lies down
     * on their own bed and night two is over. Back in town, night three begins with the hook and the hunt; one of the five dead on an
     * earlier night is not made again; first light ends it. Night four, the gin running; first light, and the end: the body is the
     * reader's own, both readers are ready, the account on the tape deck names whom the one who went in finished, and reading the pad
     * then the account gives each their own Witness story. A spectator earns nothing.
     */
    /**
     * 0.4.736, stand-ins 2: the deputy's patrol car. Night three: the car drives its loop with its headlights a light ahead of it. A reader
     * on Amber's side in front of it is not spotted; the one who went in is: the car stops, its spotlight is a light on them, they are told,
     * the reader near the car hears the radio and the deputy is called there. Gone from its sight, they are let go and it drives on; at
     * first light it is gone and its lights are out.
     */
    @GameTest(template="empty",batch="the_list_cruiser",timeoutTicks=3000)
    public static void theCruiser(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_cruiser",P);var peer=f.player("list_cruiser_peer",P);
        hold(f,b,TheListDen.DEN_AT,TheListSite.BED_HEAD);hold(f,b,TheListPeople.AT);
        for(int x=-88;x<=24;x+=16){hold(f,b,new BlockPos(x,0,-70));hold(f,b,new BlockPos(x,0,-28));}
        Vec3[] seen={null};BlockPos[] spot={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);f.at(peer,P,-2.5,0,-3.5);
            h.assertTrue(TheListNights.begin(p,b,3),"night three begins");
        }).thenWaitUntil(()->{var c=TheListCruiser.car(l,b);h.assertTrue(c!=null,"waiting for the patrol car");seen[0]=c.position();})
        .thenWaitUntil(()->{var c=TheListCruiser.car(l,b);h.assertTrue(c!=null&&c.position().distanceTo(seen[0])>=3,"waiting for it to drive");})
        .thenExecute(()->{
            var c=TheListCruiser.car(l,b);
            h.assertTrue(c.kind()==TownCar.CRUISER&&c.lit()&&!c.canBeCollidedWith(),"a patrol car, lamps lit, no wall");
            // Put it a way along Seventh, heading east, and a reader on Amber's side in front of it.
            TheListCruiser.moveTo(b,60);
            f.at(peer,P,-12.5,0,-69.5);cues(peer);cues(p);
        }).thenIdle(20)
        .thenExecute(()->{
            h.assertFalse(TheListCruiser.holding(b),"a reader who did not go in is not spotted");
            // The one who went in, eight blocks ahead of where the car is now; the other reader a little behind it.
            var c=TheListCruiser.car(l,b).position().subtract(Vec3.atLowerCornerOf(b));
            f.at(p,P,c.x+8,0,-69.5);f.at(peer,P,c.x-3,0,-70.5);
        }).thenWaitUntil(()->h.assertTrue(TheListCruiser.holding(b),"waiting for the spotlight"))
        .thenExecute(()->{
            h.assertTrue(cues(p).contains(TheListTexts.CRUISER_SPOT),"the one who went in is told");
            var heard=cues(peer);h.assertTrue(heard.contains(TheListTexts.CRUISER_RADIO),"the reader by the car hears the radio: "+heard);
            spot[0]=TheListCruiser.spotAt(b);
            h.assertTrue(spot[0]!=null&&l.getBlockState(spot[0]).is(net.minecraft.world.level.block.Blocks.LIGHT),"the spotlight is a light on them");
            h.assertTrue(TheListDeputy.called(b)!=null,"the deputy is called there");
            seen[0]=TheListCruiser.car(l,b).position();
            h.assertTrue(TheListBody.lit(p,l)&&!TheListBody.lit(peer,l),"the hunt reads the one in the spotlight as standing in light, and nobody else");
        }).thenIdle(10)
        .thenExecute(()->{
            h.assertTrue(TheListCruiser.car(l,b).position().distanceTo(seen[0])<.01,"the car stays stopped");
            f.at(p,P,-3.5,0,-3.5);f.at(peer,P,-2.5,0,-3.5);
        }).thenWaitUntil(()->h.assertTrue(!TheListCruiser.holding(b),"waiting for it to let them go"))
        .thenWaitUntil(()->{var c=TheListCruiser.car(l,b);h.assertTrue(c!=null&&c.position().distanceTo(seen[0])>=2,"waiting for it to drive on");})
        .thenExecute(()->{
            h.assertFalse(l.getBlockState(spot[0]).is(net.minecraft.world.level.block.Blocks.LIGHT),"the spotlight is out");
            TheListNights.dawn(l,b);
            h.assertTrue(TheListCruiser.car(l,b)==null,"first light: the car is gone");
            TheListNight.end(l,b);
            done();h.succeed();
        });
    }
    /**
     * 0.4.737, stand-ins 3: the town out with flashlights. The one who went in is read as DANGER at home, so on night three the three
     * searchers come out with their lights and go to the street in front of that reader's house first; only that reader is told. Let go
     * from there, one who has the one who went in in sight stops, shouts, calls the deputy over, and holds them in light; the other
     * reader is never stood in it, and the spectator hears nothing. First light takes them home.
     */
    @GameTest(template="empty",batch="the_list_search",timeoutTicks=4000)
    public static void theSearchers(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_search",P);var peer=f.player("list_search_peer",P);var ghost=f.player("list_search_ghost",P);
        hold(f,b,TheListDen.DEN_AT,TheListSite.BED_HEAD,TheListSite.FRONT_DOOR);hold(f,b,TheListPeople.AT);
        for(int z=-120;z<=-24;z+=16){hold(f,b,new BlockPos(21,0,z));hold(f,b,new BlockPos(5,0,z));}
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);f.at(peer,P,-2.5,0,-3.5);f.at(ghost,P,-4.5,0,-3.5);
            ghost.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
            HouseRead.signal(p,HouseRead.Trait.HOME,"DANGER",false);HouseRead.signal(p,HouseRead.Trait.HOME,"DANGER",true);
            h.assertTrue(TheListNights.begin(p,b,3),"night three begins");
            cues(p);cues(peer);cues(ghost);
        }).thenWaitUntil(()->{for(int i=0;i<TheListSearch.COUNT;i++)h.assertTrue(TheListSearch.searcher(l,b,i)!=null,"waiting for searcher "+i);})
        .thenExecute(()->{
            for(int i=0;i<TheListSearch.COUNT;i++){var a=TheListSearch.searcher(l,b,i);
                h.assertTrue(a.getMainHandItem().is(BorrascaRegistry.FLASHLIGHT.get()),"each carries a flashlight");}
            h.assertTrue(TheListSearch.goingToHouse(b),"read as DANGER: the town goes to their house first");
        }).thenWaitUntil(()->h.assertTrue(TheListSearch.atHouse(b),"waiting for them to stand in front of the house"))
        .thenExecute(()->{
            for(int i=0;i<TheListSearch.COUNT;i++){var a=TheListSearch.searcher(l,b,i);
                h.assertTrue(a.position().distanceTo(Vec3.atLowerCornerOf(b).add(TheListSearch.HOUSE[i]))<1,"each in the street in front of the house: "+i);}
            h.assertTrue(cues(p).contains(TheListTexts.SEARCH_HOUSE),"the one who went in is told");
            h.assertFalse(cues(peer).contains(TheListTexts.SEARCH_HOUSE),"nobody else is");
            h.assertTrue(TheListSearch.beamAt(b,0)!=null&&l.getBlockState(TheListSearch.beamAt(b,0)).is(net.minecraft.world.level.block.Blocks.LIGHT),"a flashlight's beam is a light");
            TheListSearch.leaveHouse(b);
        }).thenWaitUntil(()->h.assertTrue(TheListSearch.searcher(l,b,1).position().distanceTo(Vec3.atLowerCornerOf(b).add(TheListSearch.HOUSE[1]))>3,"waiting for them to go off along their streets"))
        .thenWaitUntil(()->{
            // Stood in front of the second searcher, the way they face, six blocks off; the other reader beside them.
            var a=TheListSearch.searcher(l,b,1);
            if(!TheListSearch.seeing(b,1)){var look=Vec3.directionFromRotation(0,a.getYHeadRot());var at=a.position().add(look.scale(6)).subtract(Vec3.atLowerCornerOf(b));
                f.at(p,P,at.x,0,at.z);f.at(peer,P,at.x+look.z*2,0,at.z-look.x*2);}
            h.assertTrue(TheListSearch.seeing(b,1),"waiting for a searcher to find them");
        }).thenExecute(()->{
            h.assertTrue(cues(p).contains(TheListTexts.SEARCH_SEEN),"the one who went in is told");
            h.assertTrue(TheListDeputy.called(b)!=null,"the deputy is called over");
            h.assertTrue(TheListBody.lit(p,l),"the hunt reads them as standing in light");
            h.assertFalse(TheListSearch.spotting(peer),"the other reader is not looked for");
            h.assertTrue(cues(ghost).isEmpty(),"the spectator hears nothing: "+cues(ghost));
            TheListNights.dawn(l,b);
            for(int i=0;i<TheListSearch.COUNT;i++)h.assertTrue(TheListSearch.searcher(l,b,i)==null,"first light: home");
            TheListNight.end(l,b);
            done();h.succeed();
        });
    }
    /**
     * 0.4.728, part 7c: the dawn bus. Three readers and a spectator: the one who went in and one on Amber's side are both read as
     * running, the third is not. Late in night four only the one who went in is told the bus is due. At first light, all at the bench,
     * only the one who went in gets on: took_the_bus, set down in their room; the others end as they would have, and the spectator
     * keeps nothing.
     */
    @GameTest(template="empty",batch="the_list_bus",timeoutTicks=4000)
    public static void theDawnBus(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        var p=f.player("list_bus",P);var peer=f.player("list_bus_peer",P);var third=f.player("list_bus_third",P);var ghost=f.player("list_bus_ghost",P);
        var bench=BlockPos.containing(TheListBus.BENCH);
        hold(f,b,TheListDen.DEN_AT,TheListSite.BED_HEAD,TheListSite.ENDING,TheListSite.SOURCE,bench);hold(f,b,TheListPeople.AT);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);f.at(peer,P,-2.5,0,-3.5);f.at(third,P,-1.5,0,-3.5);f.at(ghost,P,-4.5,0,-3.5);
            ghost.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
            for(var q:List.of(p,peer)){HouseRead.signal(q,HouseRead.Trait.HOME,"RUNAWAY",false);HouseRead.signal(q,HouseRead.Trait.HOME,"RUNAWAY",true);}
            h.assertTrue(TheListBus.runaway(p)&&TheListBus.runaway(peer)&&!TheListBus.runaway(third),"the read: two running, one not");
            h.assertTrue(TheListNights.begin(p,b,4),"night four begins");
            cues(p);cues(peer);cues(third);
            TheListNights.clockTo(l,b,TheListBus.DUE);
        }).thenWaitUntil(()->h.assertTrue(TheListBus.told(b,p.getUUID()),"waiting for the one who went in to be told the bus is due"))
        .thenExecute(()->{
            h.assertTrue(cues(p).contains(TheListTexts.BUS_DUE),"told, as a cue");
            h.assertFalse(TheListBus.told(b,peer.getUUID())||cues(peer).contains(TheListTexts.BUS_DUE),"the other running reader is not told: it is not their night");
            h.assertFalse(cues(third).contains(TheListTexts.BUS_DUE),"nor the third");
            for(var q:List.of(p,peer,third,ghost))f.at(q,P,TheListBus.BENCH.x+(q==p?0:q==peer?1:q==third?-1:.5),0,TheListBus.BENCH.z+.5);
        }).thenWaitUntil(()->h.assertTrue(TheListBus.atBench(p,b)&&TheListBus.atBench(peer,b),"waiting for them at the bench"))
        // 0.4.735: the bus itself, standing in the lane with its door at the bench before first light.
        .thenExecute(()->TheListNights.clockTo(l,b,TheListBus.STANDS))
        .thenWaitUntil(()->{
            var bus=TheListBus.bus(l,b);var at=bus==null?null:bus.position().subtract(Vec3.atLowerCornerOf(b));
            h.assertTrue(at!=null&&bus.kind()==TownCar.BUS&&Math.abs(at.x-TheListBus.STOP_X)<.3&&Math.abs(at.z-TheListBus.LANE_Z)<.3,"waiting for the bus at the bench: "+at);
        })
        .thenExecute(()->{
            var bus=TheListBus.bus(l,b);
            h.assertTrue(bus.lit()&&!bus.canBeCollidedWith(),"its lamps are on, and it is no wall");
            h.assertTrue(Math.abs(b.getX()+TheListBus.STOP_X+4.375-(b.getX()+TheListBus.BENCH.x))<.01,"its door is at the bench");
            TheListNights.dawn(l,b);
            h.assertTrue(TheListNight.phase(s,b)==TheListNights.OVER,"first light ends the four nights");
            var own=f.own(p,P);
            h.assertTrue(own.getBoolean("Ready")&&"took_the_bus".equals(own.getString("Outcome"))&&own.getBoolean("Bus"),"the one who went in is on the bus: "+own.getString("Outcome"));
            h.assertTrue(cues(p).contains(TheListTexts.BUS_TAKEN),"and told so");
            var room=Vec3.atLowerCornerOf(b).add(TheListBus.ROOM);
            h.assertTrue(p.position().distanceTo(room)<1,"set down in their room: "+p.position().subtract(Vec3.atLowerCornerOf(b)));
            h.assertFalse(TheListBody.has(p),"the body is their own again");
            var peers=f.own(peer,P);
            h.assertTrue(peers.getBoolean("Ready")&&"was_in_town_at_the_end".equals(peers.getString("Outcome"))&&!peers.getBoolean("Bus"),"the other running reader is not on it: "+peers.getString("Outcome"));
            h.assertTrue(TheListBus.atBench(peer,b),"and is still at the bench");
            h.assertTrue("was_in_town_at_the_end".equals(f.own(third,P).getString("Outcome")),"the third as before");
            h.assertFalse(f.own(ghost,P).getBoolean("Ready"),"the spectator keeps nothing");
            var account=String.join(" ",pages(TheListTexts.account("x",own)));
            h.assertTrue(account.contains("first bus out"),"the account says so");
            h.assertFalse(String.join(" ",pages(TheListTexts.account("x",peers))).contains("first bus out"),"theirs does not");
            h.assertFalse(TheListBus.told(b,p.getUUID()),"the night put away, who was told is forgotten");
            h.assertTrue(TheListBus.bus(l,b)!=null,"the bus is still there, pulling out");
        }).thenWaitUntil(()->h.assertTrue(TheListBus.bus(l,b)==null,"waiting for the bus to pull out and be gone"))
        .thenExecute(()->{
            TheListNight.end(l,b);
            done();h.succeed();
        });
    }
    /**
     * 0.4.729, part 8a: warned off. On night four, the one who went in, two readers on Amber's side and a spectator. The spectator's hand
     * does nothing. Warned by a reader on Amber's side, each of the five makes for the highway; caught up with by the one who went in,
     * Deek fights once and goes on, and caught again cowers, until warned again. Out of everyone's sight they are gone, the night ends
     * when the last is, and everyone gone is the ending: each reader keeps whom they got out, the spectator nothing.
     */
    @GameTest(template="empty",batch="the_list_warn",timeoutTicks=6000)
    public static void warnedOff(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        var p=f.player("list_warn",P);var q1=f.player("list_warn_one",P);var q2=f.player("list_warn_two",P);var ghost=f.player("list_warn_ghost",P);
        hold(f,b,TheListDen.DEN_AT,TheListSite.BED_HEAD,TheListSite.ENDING,TheListSite.SOURCE,BlockPos.containing(TheListHunt.OUT_OF_TOWN));hold(f,b,TheListPeople.AT);
        int[] firsts={TheListPeople.DEEK,TheListPeople.MEL},seconds={TheListPeople.LESLEY,TheListPeople.SHANNON,TheListPeople.WES};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);f.at(q1,P,-2.5,0,-3.5);f.at(q2,P,-1.5,0,-3.5);f.at(ghost,P,-4.5,0,-3.5);
            ghost.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
            h.assertTrue(TheListNights.begin(p,b,4),"night four begins");
        }).thenWaitUntil(()->{TheListPeople.ensure(l,b);for(int who:TheListBody.LISTED)h.assertTrue(TheListHunt.actor(l,b,who)!=null,"waiting for "+TheListTexts.NAMES[who]);})
        .thenExecute(()->{
            var deek=TheListHunt.actor(l,b,TheListPeople.DEEK);
            h.assertTrue(TheListHunt.hand(ghost,deek)&&TheListHunt.state(b,TheListPeople.DEEK)==TheListHunt.State.CALM,"a spectator's hand does nothing");
            cues(q1);cues(q2);
            for(int who:firsts)h.assertTrue(TheListHunt.hand(q1,TheListHunt.actor(l,b,who)),"warned by the first");
            for(int who:seconds)h.assertTrue(TheListHunt.hand(q2,TheListHunt.actor(l,b,who)),"warned by the second");
            for(int who:TheListBody.LISTED)h.assertTrue(TheListHunt.state(b,who)==TheListHunt.State.LEAVING,TheListTexts.NAMES[who]+" is on the way out");
            h.assertTrue(cues(q1).contains(String.format(TheListTexts.WARNED,TheListTexts.NAMES[TheListPeople.DEEK])),"the first is told");
            h.assertTrue(TheListHunt.warnedBy(b,TheListPeople.WES).equals(q2.getUUID()),"each keeps who warned them");
            var at=deek.position().subtract(Vec3.atLowerCornerOf(b));f.at(p,P,at.x+1,at.y,at.z);
        }).thenWaitUntil(()->h.assertTrue(TheListHunt.fought(b,TheListPeople.DEEK),"waiting for Deek to fight the one who went in"))
        .thenExecute(()->h.assertTrue(TheListHunt.state(b,TheListPeople.DEEK)==TheListHunt.State.LEAVING,"and go on his way"))
        .thenWaitUntil(()->h.assertTrue(TheListBody.down(p)==0,"waiting for the body to get up"))
        .thenExecute(()->{var deek=TheListHunt.actor(l,b,TheListPeople.DEEK);var at=deek.position().subtract(Vec3.atLowerCornerOf(b));f.at(p,P,at.x+1,at.y,at.z);})
        .thenWaitUntil(()->h.assertTrue(TheListHunt.state(b,TheListPeople.DEEK)==TheListHunt.State.COWER,"waiting for Deek, caught again, to cower: "+TheListHunt.state(b,TheListPeople.DEEK)))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);
            var deek=TheListHunt.actor(l,b,TheListPeople.DEEK);f.at(q1,P,deek.getX()-b.getX()+1,deek.getY()-b.getY(),deek.getZ()-b.getZ());
            h.assertTrue(TheListHunt.hand(q1,deek)&&TheListHunt.state(b,TheListPeople.DEEK)==TheListHunt.State.LEAVING,"warned again, he goes");
            // Anyone else caught up with beside him (Wes walks out with Deek) is warned again by the one who warned them.
            for(int who:TheListBody.LISTED)if(TheListHunt.state(b,who)==TheListHunt.State.COWER){var a=TheListHunt.actor(l,b,who);var by=TheListHunt.warnedBy(b,who).equals(q1.getUUID())?q1:q2;
                f.at(by,P,a.getX()-b.getX()+1,a.getY()-b.getY(),a.getZ()-b.getZ());h.assertTrue(TheListHunt.hand(by,a),"warned again");}
            f.at(q1,P,-2.5,0,-3.5);f.at(q2,P,-1.5,0,-3.5);
        }).thenWaitUntil(()->{for(int who:TheListBody.LISTED)h.assertTrue(TheListHunt.left(b,who)||TheListNight.phase(s,b)==TheListNights.OVER,"waiting for "+TheListTexts.NAMES[who]+" to be out of sight: "+TheListHunt.state(b,who));})
        .thenWaitUntil(()->h.assertTrue(TheListNight.phase(s,b)==TheListNights.OVER,"the last of them gone ends the night"))
        .thenExecute(()->{
            var own=f.own(p,P);
            h.assertTrue(own.getBoolean("Ready")&&"everyone_got_away".equals(own.getString("Outcome")),"everyone gone: "+own.getString("Outcome"));
            h.assertTrue(cues(p).contains(TheListTexts.END_ALL_GONE),"and told so");
            var one=f.own(q1,P);var two=f.own(q2,P);
            h.assertTrue(one.getInt("GotOut")==(1<<TheListPeople.DEEK|1<<TheListPeople.MEL)&&"got_them_out".equals(one.getString("Outcome")),"the first got Deek and Mel out: "+one.getInt("GotOut"));
            h.assertTrue(two.getInt("GotOut")==(1<<TheListPeople.LESLEY|1<<TheListPeople.SHANNON|1<<TheListPeople.WES),"the second the other three: "+two.getInt("GotOut"));
            h.assertFalse(f.own(ghost,P).getBoolean("Ready"),"the spectator keeps nothing");
            TheListPeople.ensure(l,b);
            for(int who:TheListBody.LISTED)h.assertTrue(TheListPeople.find(l,b,who)==null,TheListTexts.NAMES[who]+" is not made again");
            h.assertTrue(String.join(" ",pages(TheListTexts.account("x",own))).contains("every one of them had left town"),"the account says so");
            var theirs=String.join(" ",pages(TheListTexts.account("x",one)));
            h.assertTrue(theirs.contains("I told Deek and Mel to get out of town")&&theirs.contains("nobody on the list left"),"the first reader's says whom: "+theirs);
            TheListNight.end(l,b);
            done();h.succeed();
        });
    }
    /**
     * 0.4.730, part 8b: more of Amber's side, on night two's hunt, with the one who went in, two readers on Amber's side and a spectator.
     * Both brace a neighbor's door (a third brace from the same reader does nothing, the spectator's hand nothing): it takes nine blows,
     * the extra six held. One knocks the slasher down from behind, and cannot again at once; the spectator cannot. The other crouches by
     * Mel, hidden, and her heart is kept from the slasher's pull until they move off. Each record and account keeps its own.
     */
    @GameTest(template="empty",batch="the_list_amber",timeoutTicks=6000)
    public static void amberSide(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        var p=f.player("list_amber",P);var q1=f.player("list_amber_one",P);var q2=f.player("list_amber_two",P);var ghost=f.player("list_amber_ghost",P);
        var door=new BlockPos(-34,0,-37);var stand=door.south(2);
        hold(f,b,TheListDen.DEN_AT,TheListSite.BED_HEAD,TheListSite.ENDING,TheListSite.SOURCE,door);hold(f,b,TheListPeople.AT);
        var seq=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);f.at(q1,P,-2.5,0,-3.5);f.at(q2,P,-1.5,0,-3.5);f.at(ghost,P,-4.5,0,-3.5);
            ghost.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
            h.assertTrue(TheListDen.begin(p,b),"night two begins");TheListNights.toRan(l,b);
        }).thenWaitUntil(()->h.assertTrue(TheListNight.phase(s,b)==TheListNights.HUNT2,"waiting for the hunt"))
        .thenExecute(()->{
            cues(q1);cues(q2);
            use(q1,b,door,stand);h.assertTrue(cues(q1).contains(TheListTexts.BRACED),"the first braces the door");
            use(q1,b,door,stand);h.assertTrue(cues(q1).contains(TheListTexts.BRACED_ALREADY)&&TheListAmber.braces(b.offset(door))==1,"not twice");
            use(ghost,b,door,stand);h.assertTrue(TheListAmber.braces(b.offset(door))==1,"the spectator's hand does nothing");
            use(q2,b,door,stand);h.assertTrue(TheListAmber.braces(b.offset(door))==2,"the second braces it too");
            f.at(q1,P,stand.getX()+2.5,0,stand.getZ()+.5);f.at(q2,P,stand.getX()-1.5,0,stand.getZ()+.5);
            f.at(p,P,stand.getX()+.5,0,stand.getZ()+.5);cues(p);
        }).thenWaitUntil(()->h.assertTrue(TheListBody.live(p)&&TheListBody.down(p)==0,"waiting for the body"));
        int need=TheListBody.STRIKES+2*TheListAmber.BRACE;
        for(int i=1;i<=need;i++){final int n=i;
            seq=seq.thenIdle(TheListBody.STRIKE_GAP).thenExecute(()->{
                use(p,b,door,stand);
                if(n<need)h.assertTrue(shut(l,b,door)&&TheListBody.blows(b.offset(door))==n,"blow "+n+" of "+need+" and it holds: "+TheListBody.blows(b.offset(door)));
                if(n==TheListBody.STRIKES)h.assertTrue(cues(p).contains(TheListTexts.BRACE_HOLDS),"the third blow is held from the other side");
            });}
        seq.thenExecute(()->{
            h.assertTrue(l.getBlockState(b.offset(door)).is(TheListRegistry.BROKEN_DOOR.get()),"the ninth gives");
            h.assertTrue(TheListAmber.braces(b.offset(door))==0,"and the bracing goes with it");
            h.assertFalse(TheListAmber.knock(ghost,p),"the spectator cannot put the body down");
            h.assertTrue(TheListAmber.knock(q1,p)&&TheListBody.down(p)>0,"a blow from behind puts it down");
            h.assertTrue(cues(p).contains(TheListTexts.KNOCKED_DOWN)&&cues(q1).contains(TheListTexts.KNOCKED_IT),"each told");
        }).thenWaitUntil(()->h.assertTrue(TheListBody.down(p)==0,"waiting for the body to get up"))
        .thenExecute(()->{
            h.assertTrue(TheListAmber.knock(q1,p)&&TheListBody.down(p)==0&&cues(q1).contains(TheListTexts.KNOCK_SOON),"not again at once");
            TheListHunt.hideNow(l,b,TheListPeople.MEL);
            var mel=TheListHunt.actor(l,b,TheListPeople.MEL);
            h.assertTrue(mel!=null&&TheListHunt.state(b,TheListPeople.MEL)==TheListHunt.State.HIDE,"Mel is hidden");
            f.at(q2,P,mel.getX()-b.getX()+1.2,mel.getY()-b.getY(),mel.getZ()-b.getZ());q2.setPose(net.minecraft.world.entity.Pose.CROUCHING);
            f.at(p,P,mel.getX()-b.getX()+5,mel.getY()-b.getY(),mel.getZ()-b.getZ());
        }).thenWaitUntil(()->h.assertTrue(TheListHunt.hushed(TheListHunt.actor(l,b,TheListPeople.MEL)),"waiting for her heart to be kept quiet"))
        .thenIdle(3).thenExecute(()->{
            var mel=TheListHunt.actor(l,b,TheListPeople.MEL);
            h.assertFalse(mel.getUUID().equals(TheListBody.pulled(p)),"the slasher's pull is not to her");
            h.assertTrue(cues(q2).contains(String.format(TheListTexts.HUSHED,"Mel")),"the one beside her is told");
            q2.setPose(net.minecraft.world.entity.Pose.STANDING);f.at(q2,P,-1.5,0,-3.5);
        }).thenWaitUntil(()->h.assertFalse(TheListHunt.hushed(TheListHunt.actor(l,b,TheListPeople.MEL)),"waiting for her heart to be heard again"))
        .thenExecute(()->{
            var one=f.own(q1,P);var two=f.own(q2,P);
            h.assertTrue(one.getInt("Braced")==1&&one.getInt("Knocked")==1&&one.getInt("Hushed")==0,"the first's record: "+one);
            h.assertTrue(two.getInt("Braced")==1&&two.getInt("Knocked")==0&&two.getInt("Hushed")==1<<TheListPeople.MEL,"the second's: "+two);
            var ghostOwn=f.own(ghost,P);h.assertTrue(ghostOwn.getInt("Braced")==0&&ghostOwn.getInt("Knocked")==0,"the spectator's nothing");
            var a1=String.join(" ",pages(TheListTexts.account("x",one)));var a2=String.join(" ",pages(TheListTexts.account("x",two)));
            h.assertTrue(a1.contains("held a door shut once")&&a1.contains("hit it from behind"),"the first's account: "+a1);
            h.assertTrue(a2.contains("I lay next to Mel")&&!a2.contains("hit it from behind"),"the second's: "+a2);
            TheListNight.end(l,b);TheListBody.end(p);
            done();h.succeed();
        });
    }
    /** 0.4.732: the rules from the tapes tried for real by a reader on Amber's side; a spectator and the one who went in try none. */
    @GameTest(template="empty",batch="the_list_rules",timeoutTicks=6000)
    public static void theRules(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        var p=f.player("list_rules",P);var q=f.player("list_rules_one",P);var ghost=f.player("list_rules_ghost",P);
        var door=new BlockPos(-34,0,-37);var stand=door.south(2);
        hold(f,b,TheListDen.DEN_AT,TheListSite.BED_HEAD,TheListSite.ENDING,TheListSite.SOURCE,door);hold(f,b,TheListPeople.AT);
        var seq=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);f.at(q,P,-2.5,0,-3.5);f.at(ghost,P,-4.5,0,-3.5);
            ghost.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
            h.assertTrue(TheListDen.begin(p,b),"night two begins");TheListNights.toRan(l,b);
        }).thenWaitUntil(()->h.assertTrue(TheListNight.phase(s,b)==TheListNights.HUNT2,"waiting for the hunt"))
        .thenExecute(()->{
            cues(q);cues(ghost);
            f.at(p,P,stand.getX()+.5,0,stand.getZ()+.5);
            f.at(q,P,stand.getX()+4.5,0,stand.getZ()+.5);f.at(ghost,P,stand.getX()+3.5,0,stand.getZ()+1.5);
            for(var r:List.of(q,ghost,p)){r.setItemInHand(InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.TORCH));}
            for(var r:List.of(q,ghost))r.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,p.getEyePosition());
        }).thenWaitUntil(()->h.assertTrue(TheListBody.live(p)&&TheListBody.down(p)==0,"waiting for the body"))
        .thenWaitUntil(()->h.assertTrue(TheListRules.tried(f.own(q,P),TheListRules.LIGHT),"waiting for the light to be held on it: "+f.own(q,P)))
        .thenExecute(()->{
            h.assertTrue(cues(q).contains(TheListTexts.RULE_LIGHT),"told the light doesn't keep it back");
            h.assertFalse(TheListRules.tried(f.own(ghost,P),TheListRules.LIGHT)||TheListRules.tried(f.own(p,P),TheListRules.LIGHT),"neither the spectator nor the one who went in tries it");
            p.setItemInHand(InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);
        });
        for(int i=1;i<=TheListBody.STRIKES;i++){
            seq=seq.thenIdle(TheListBody.STRIKE_GAP).thenExecute(()->use(p,b,door,stand));}
        seq.thenExecute(()->{
            h.assertTrue(l.getBlockState(b.offset(door)).is(TheListRegistry.BROKEN_DOOR.get()),"the door gives");
            h.assertTrue(TheListRules.tried(f.own(q,P),TheListRules.ASKED)&&cues(q).contains(TheListTexts.RULE_ASKED),"the one by it saw it come in unasked");
            h.assertFalse(TheListRules.tried(f.own(ghost,P),TheListRules.ASKED)||TheListRules.tried(f.own(p,P),TheListRules.ASKED),"nobody else");
            TheListNights.dawn(l,b);
            h.assertTrue(TheListRules.tried(f.own(q,P),TheListRules.NIGHT)&&cues(q).contains(TheListTexts.RULE_NIGHT),"first light: the one rule that holds");
            h.assertTrue(f.own(ghost,P).getInt("Tried")==0&&f.own(p,P).getInt("Tried")==0,"the spectator and the one who went in tried nothing");
            var a=String.join(" ",pages(TheListTexts.account("x",f.own(q,P))));
            h.assertTrue(a.contains("came on anyway")&&a.contains("I watched it come in")&&a.contains("That one held"),"the account says what was tried: "+a);
            TheListNight.end(l,b);TheListBody.end(p);
            done();h.succeed();
        });
    }
    /** 0.4.733: the town afterwards, for a reader who has finished, alone with finished readers; not while an unfinished one is in it. */
    @GameTest(template="empty",batch="the_list_after",timeoutTicks=3000)
    public static void theTownAfter(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=f.player("list_after",P);var q=f.player("list_after_new",P);
        hold(f,b,TheListSite.BED_HEAD,TheListSite.ENDING,TheListSite.SOURCE);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);q.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);f.at(q,P,40.5,0,40.5);
            var own=f.own(p,P);own.putBoolean("Completed",true);own.putString("Outcome","took_the_bus");own.putInt("Night",4);own.putInt("Visit",5);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
            h.assertTrue(Aftermath.canDeal(f.data(),p.getUUID(),P),"a finished reader can be dealt the town again");
            cues(p);own=f.own(p,P);TheList.arrive(p,b,own);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
            h.assertTrue(cues(p).contains(TheListAfter.BUS)&&Aftermath.returns(f.data(),p.getUUID(),P)==1,"noon, and the bench empty: "+f.own(p,P));
            h.assertTrue(TheListNight.phase(l.getServer(),b)==TheListNight.NONE&&!TheListHunt.on(b),"no night begins");
            own=f.own(p,P);TheList.arrive(p,b,own);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
            h.assertTrue(Aftermath.returns(f.data(),p.getUUID(),P)==1,"the same visit counts once");
            // A reader who has not had a night, in the town with them: no aftermath, and night one begins round them.
            q.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);f.at(q,P,-2.5,0,-3.5);
            var mine=f.own(p,P);mine.putInt("Visit",6);LiteraryVignettes.save(f.data(),p.getUUID(),P,mine);
            h.assertFalse(Aftermath.alone(TheList.readers(l,b),P),"not alone with an unfinished reader");
            cues(p);mine=f.own(p,P);TheList.arrive(p,b,mine);
            h.assertTrue(Aftermath.returns(f.data(),p.getUUID(),P)==1,"and no return counted");
            h.assertTrue(TheListAfter.line(f.own(p,P)).equals(TheListAfter.BUS),"the line follows the record");
            // 0.4.734: and the reader's House of Leaves can tell it.
            var facts=StaircaseAccount.facts(p,new net.minecraft.nbt.CompoundTag());
            h.assertTrue(facts.stream().anyMatch(x->x.kind().id().equals("listed")&&x.value().equals("on the first bus out")),"the House of Leaves tells how they left the list: "+facts.stream().map(x->x.kind().id()+"="+x.value()).toList());
            h.assertTrue(StaircaseAccount.facts(q,new net.minecraft.nbt.CompoundTag()).stream().noneMatch(x->x.kind().id().equals("listed")),"not for a reader who has not finished it");
            TheListNight.end(l,b);
            done();h.succeed();
        });
    }
    @GameTest(template="empty",batch="the_list_end",timeoutTicks=6000)
    public static void toTheEnd(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        var p=f.player("list_end",P);var peer=f.player("list_end_peer",P);var ghost=f.player("list_end_ghost",P);
        hold(f,b,TheListDen.DEN_AT,TheListSite.BED_HEAD,TheListSite.ENDING,TheListSite.SOURCE);hold(f,b,TheListPeople.AT);hold(f,b,huddle());
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"waiting for the ground"))
        .thenExecute(()->{
            f.at(p,P,-3.5,0,-3.5);f.at(peer,P,-2.5,0,-3.5);f.at(ghost,P,-4.5,0,-3.5);ghost.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);cues(p);cues(peer);
            h.assertTrue(TheListDen.begin(p,b),"night two begins round the one who went in");
            TheListNights.toRan(l,b);
        }).thenWaitUntil(()->h.assertTrue(TheListNight.phase(s,b)==TheListNights.HUNT2,"waiting for the first name: "+TheListNight.NAMES[TheListNight.phase(s,b)]))
        .thenExecute(()->{
            h.assertTrue(TheListHunt.on(b),"night two's hunt is on");
            h.assertTrue(cues(p).contains(TheListTexts.FIRST_NAME_CUE)&&cues(peer).contains(TheListTexts.HUNT_OTHER),"each told");
            f.click(p,P,TheListSite.BED_HEAD);
            h.assertTrue(TheListNight.phase(s,b)==TheListNights.DONE2&&!TheListHunt.on(b),"lying down ends night two: "+TheListNight.NAMES[TheListNight.phase(s,b)]);
            h.assertTrue(f.own(p,P).getInt("Night")==2,"they have had two nights");
            TheListNights.slain(l,b,TheListPeople.WES,p.getUUID());
            var own=f.own(p,P);TheList.arrive(p,b,own);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
            h.assertTrue(TheListNight.phase(s,b)==TheListNights.HUNT3&&TheListHunt.on(b)&&TheListNight.time(s,b)==TheListNight.TEN,"back in town, night three: ten o'clock and the hunt on");
            h.assertTrue(cues(p).contains(TheListTexts.NIGHT3)&&TheListBody.has(p),"the hook back, the body theirs");
            h.assertTrue(TheListHunt.state(b,TheListPeople.WES)==null&&TheListPeople.find(l,b,TheListPeople.WES)==null,"Wes, dead the night before, is not made again");
        }).thenWaitUntil(()->h.assertTrue(TheListHunt.state(b,TheListPeople.DEEK)!=null,"the rest are hunted"))
        .thenExecute(()->{
            TheListNights.dawn(l,b);
            h.assertTrue(TheListNight.phase(s,b)==TheListNights.DONE3&&f.own(p,P).getInt("Night")==3,"first light ends night three");
            var own=f.own(p,P);TheList.arrive(p,b,own);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
            h.assertTrue(TheListNight.phase(s,b)==TheListNights.HUNT4&&TheListGin.running(b),"night four, the gin running");
            h.assertTrue(cues(p).contains(TheListTexts.NIGHT4),"told");
            cues(p);cues(peer);
            TheListNights.dawn(l,b);
            h.assertTrue(TheListNight.phase(s,b)==TheListNights.OVER&&!TheListHunt.on(b),"the end: "+TheListNight.NAMES[TheListNight.phase(s,b)]);
            h.assertFalse(TheListBody.has(p),"the body is their own again");
            h.assertTrue(cues(p).contains(TheListTexts.END_WENT_IN)&&cues(peer).contains(TheListTexts.END_OTHER),"each told");
            var mine=f.own(p,P);var theirs=f.own(peer,P);
            h.assertTrue(mine.getBoolean("Ready")&&mine.getString("Outcome").equals("crossed_names_off")&&mine.getInt("Night")==4,"the one who went in is ready: "+mine.getString("Outcome"));
            h.assertTrue(theirs.getBoolean("Ready")&&theirs.getString("Outcome").equals("was_in_town_at_the_end"),"so is the other reader");
            h.assertFalse(f.own(ghost,P).getBoolean("Ready"),"the spectator is not");
            var account=String.join(" ",pages(TheListTexts.account("x",mine)));
            h.assertTrue(account.contains("been to Wes.")&&account.contains("Four nights"),"the account says whom they finished: "+account);
            h.assertTrue(String.join(" ",pages(TheListTexts.account("x",theirs))).contains("Somebody else had the sack"),"the other reader's says somebody else");
            for(var q:List.of(p,peer)){f.read(q,P,TheListSite.SOURCE);f.read(q,P,TheListSite.ENDING);}
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.THE_LIST)&&WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.THE_LIST),"each reads their own account, and the story is theirs");
            h.assertTrue(WitnessAccount.Story.THE_LIST.kind.equals("memory")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES,"a memory story, in the pinned pool");
            // 0.4.731: what is kept. The one who went in takes a strip of the sack; the other reader a page of Amber's rules.
            h.assertTrue(Keepsakes.carries(p,P,"Strip")&&!Keepsakes.carries(p,P,"Page"),"the one who went in keeps a strip of the sack");
            h.assertTrue(Keepsakes.carries(peer,P,"Page")&&!Keepsakes.carries(peer,P,"Strip"),"the other reader keeps a page of the notebook");
            h.assertFalse(Keepsakes.carries(ghost,P,"Page")||Keepsakes.carries(ghost,P,"Strip"),"the spectator keeps nothing");
            TheListNight.end(l,b);
            // Out in their own world.
            var home=h.absoluteVec(new Vec3(2.5,1,2.5));
            for(var q:List.of(p,peer))q.teleportTo(h.getLevel(),home.x,home.y,home.z,0,0);
            h.assertFalse(Keepsakes.heard(p),"no page, no heart");
            h.assertTrue(Keepsakes.heard(peer)&&!Keepsakes.heard(peer),"something turning on them, they hear its heart, and not again at once");
            h.assertTrue(Keepsakes.gotUp(p)&&!Keepsakes.gotUp(p)&&!Keepsakes.gotUp(peer),"a killing blow puts the one with the strip down once a day; nobody else");
            h.assertTrue(AccountAfterwards.list(Keepsakes.record(p)).contains("I got up")&&AccountAfterwards.list(Keepsakes.record(peer)).contains("I heard a heart before I saw"),"the accounts say what each kept did");
            Keepsakes.rest(peer.getUUID());
            done();h.succeed();
        });
    }
    /** Where a reader stands in Amber's den to talk to her: between her and the couch. */
    private static final Vec3 DEN_STAND=new Vec3(-5.5,0,-80.5);
}
