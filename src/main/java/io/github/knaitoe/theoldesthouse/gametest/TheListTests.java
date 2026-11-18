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
        h.startSequence().thenWaitUntil(()->{
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
        h.startSequence().thenExecute(()->{
            f.at(p,P,tank.getX()+2.5,0,tank.getZ()-14.5);f.at(peer,P,tank.getX()-1.5,0,tank.getZ()-9.5);
        }).thenWaitUntil(()->h.assertTrue(l.isLoaded(b.offset(tank))&&l.isLoaded(b.offset(TheListNight.AT_TANK[3])),"waiting for the tank"))
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
}
