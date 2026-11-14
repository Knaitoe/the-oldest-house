package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.409, the city on the ice, with real readers. The camp, the pass and the city built whole, the radio log in the hut. The wind pushes
 * back down the upper pass and the cold builds out in it but stops short of freezing, and a nook keeps it off. The carvings read in order,
 * each by standing and looking at it; a later one first says nothing yet. The tunnel: a reader who has not read them all finds nothing at
 * the shore; one who has brings the piping and what fills the tunnel; a reader who goes on gets out of the pit ahead of it, a reader who
 * stays is taken into the dark and comes to by the hut with every carving still read. The look back from the crest readies the account
 * for the reader who outran it, and not for the one who didn't.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class CityTests {
    private CityTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.BLACK_CITY;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);BlackCity.clearAll();return active;}
    /** Run 1493: the last case of each batch leaves no players behind for whatever the runner takes up next. */
    private static void close(){if(active!=null){active.close();active=null;}BlackCity.clearAll();}
    @net.minecraft.gametest.framework.AfterBatch(batch="city_camp") public static void cityCampDone(net.minecraft.server.level.ServerLevel l){close();}
    @net.minecraft.gametest.framework.AfterBatch(batch="city_look") public static void cityLookDone(net.minecraft.server.level.ServerLevel l){close();}
    @net.minecraft.gametest.framework.AfterBatch(batch="city_tunnel") public static void cityTunnelDone(net.minecraft.server.level.ServerLevel l){close();}
    private static AABB box(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()-65,b.getY()-47,b.getZ()-193,b.getX()+65,b.getY()+61,b.getZ()+2);}
    private static String account(LiteraryTests.Fixture f,ServerPlayer p){var book=CityTexts.account(p.getGameProfile().getName(),f.own(p,P));var c=book.get(DataComponents.WRITTEN_BOOK_CONTENT);return String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());}
    private static void murals(LiteraryTests.Fixture f,ServerPlayer p,int read){var own=f.own(p,P);own.putInt("Murals",read);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);}
    /** Stands in front of a carving at the height people stand there, looking at the middle of it. */
    private static void before(LiteraryTests.Fixture f,ServerPlayer p,int panel){
        var m=CitySite.MURALS.get(panel);var n=new Vec3(m.facing().getStepX(),0,m.facing().getStepZ());var feet=m.centre().add(n.scale(2.5));
        f.at(p,P,feet.x,m.centre().y-1.5,feet.z);var b=f.base(P);p.lookAt(EntityAnchorArgument.Anchor.EYES,m.centre().add(b.getX(),b.getY(),b.getZ()));
    }
    private static boolean mass(LiteraryTests.Fixture f,int slice){var l=f.level(P);var b=f.base(P);for(var at:CitySite.CHASE.get(slice))if(l.getBlockState(b.offset(at)).is(CityRegistry.MASS.get()))return true;return false;}

    @GameTest(template="empty",batch="city_camp",timeoutTicks=4000)
    public static void theCampThePassTheWindAndTheCarvingsReadInOrder(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] me=new ServerPlayer[1];double[] frost=new double[1];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the camp and the city are resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.kind()==LabyrinthPlace.Kind.RECURRING&&P.authored()&&b.getY()==CitySite.BASE_Y&&SceneClock.at(f.origin,b.offset(0,0,-20),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE)==20,"a literary place outside, built whole, high enough for its pit, under a low sun");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("understanding")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"an understanding story, the quota derived");
                h.assertTrue(CityBlock.is(l.getBlockState(b.offset(CitySite.LOGBOOK)),CityBlock.Kind.LOGBOOK)&&CityBlock.is(l.getBlockState(b.offset(CitySite.PAD)),CityBlock.Kind.PAD)&&CityBlock.is(l.getBlockState(b.offset(CitySite.RADIO)),CityBlock.Kind.RADIO),"the log, the pad and the radio in the hut");
                for(var m:CitySite.MURALS)for(int i=0;i<m.cells().size();i++){var s=l.getBlockState(b.offset(m.cells().get(i)));h.assertTrue(CityMural.is(s)&&s.getValue(CityMural.PANEL)==m.index()&&s.getValue(CityMural.PART)==m.parts().get(i)&&s.getValue(CityMural.FACING)==m.facing(),"panel "+m.index()+" carved whole into the wall");}
                h.assertTrue(CitySite.MURALS.size()==12,"twelve carvings");
                for(int i=1;i<CitySite.MURALS.size();i++)h.assertTrue(CitySite.MURALS.get(i).s()>CitySite.MURALS.get(i-1).s()&&CitySite.MURALS.get(i).centre().y<CitySite.MURALS.get(i-1).centre().y+.01,"the carvings go down the ramp in order: "+i);
                // The ramp: a floor all the way round, never a whole block's step, to the landing at the tunnel's mouth.
                for(int x=-2;x<=2;x++)for(int z=CitySite.TUNNEL_N;z<=CitySite.PIT_Z-CitySite.RIM;z++)h.assertTrue(l.getBlockState(b.offset(x,CitySite.FLOOR,z)).isAir()||CityBlock.is(l.getBlockState(b.offset(x,CitySite.FLOOR,z)),CityBlock.Kind.PRINTS),"the tunnel runs clear at "+x+","+z);
                h.assertTrue(!l.getBlockState(b.offset(0,CitySite.FLOOR-1,CitySite.TUNNEL_N)).isAir(),"the tunnel has a floor");
                for(var at:CitySite.LAMPS)h.assertTrue(CityBlock.is(l.getBlockState(b.offset(at)),CityBlock.Kind.LAMP)&&l.getBlockState(b.offset(at)).getValue(CityBlock.STAGE)==1,"the lamps in the tunnel are lit");
                h.assertTrue(l.getBlockState(b.offset(0,CitySite.stand(-74),-74)).isAir()&&!l.getBlockState(b.offset(0,CitySite.stand(-74)-1,-74)).isAir(),"the path over the crest");
                for(int z:CitySite.NOOKS)h.assertTrue(l.getBlockState(b.offset(CitySite.PATH+1,CitySite.stand(z),z)).isAir()&&!l.getBlockState(b.offset(CitySite.PATH+1,CitySite.stand(z)+3,z)).isAir(),"a nook in the rock at "+z);
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=String.join(" ",text.pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(all.contains("too regular to be peaks")&&all.contains("I am going after them"),"the radio log: "+all);
                me[0]=f.player("city_reader",P);me[0].setTicksFrozen(0);
                f.at(me[0],P,.5,CitySite.stand(-60),-59.5);me[0].setDeltaMovement(Vec3.ZERO);
            })
            .thenWaitUntil(()->h.assertTrue(me[0]!=null&&me[0].getTicksFrozen()>=60&&me[0].getDeltaMovement().z>0,"out in the wind on the pass: pushed back down it, and the cold building: "+me[0].getTicksFrozen()+" "+me[0].getDeltaMovement()))
            .thenIdle(160)
            .thenExecute(()->{
                h.assertTrue(me[0].getTicksFrozen()<me[0].getTicksRequiredToFreeze(),"the cold stops short of freezing: "+me[0].getTicksFrozen());
                frost[0]=me[0].getTicksFrozen();me[0].setTicksFrozen(20);frost[0]=20;
                f.at(me[0],P,CitySite.PATH+1.5,CitySite.stand(-54),-53.5);me[0].setDeltaMovement(Vec3.ZERO);
            })
            .thenIdle(120)
            .thenExecute(()->{
                h.assertTrue(me[0].getTicksFrozen()<=frost[0]&&me[0].getDeltaMovement().z==0,"in a nook, out of the wind: "+me[0].getTicksFrozen()+" "+me[0].getDeltaMovement());
                before(f,me[0],1);
            })
            .thenIdle(90)
            .thenExecute(()->{var o=f.own(me[0],P);h.assertTrue(o.getInt("Murals")==0&&(o.getInt("Skipped")&2)!=0,"a later carving first goes on from something further up: "+o);before(f,me[0],0);})
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getInt("Murals")==1,"the first carving read by standing and looking at it"))
            .thenExecute(()->{murals(f,me[0],11);before(f,me[0],11);})
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getInt("Murals")==12,"the twelfth, read in its turn"))
            .thenExecute(()->{
                f.click(me[0],P,CitySite.MURALS.get(4).cells().get(0));
                h.assertTrue(!f.own(me[0],P).getBoolean("Ready")&&LiteraryVignettes.canDeal(f.data(),me[0].getUUID(),P),"nothing is ready yet, and the city is there again on the next visit");
                BlackCity.resetNow(l,b);
            })
            .thenSucceed();
    }

    /**
     * 0.4.579. The aeroplane stands whole: every part of it in the cell the art tool's table gives it and no part twice, thirteen cells
     * from tail to nose and seventeen across its wing; the propeller on its nose; nothing left of the canvas box it was. A body walks
     * under the wing and is stopped by it from above. A hand on its fabric is told the fabric is cut, a hand on a ski is told nothing,
     * and the propeller says what it said. The fixed rope: a piece in every row of the pass at each side, the rock on its left, unbroken
     * from row to row and the same height over the lower ground at every edge; a stake at each end of a run and at least every fourth
     * row; none across a nook's mouth; it stops nobody; and no fence post is left on the steps.
     */
    @GameTest(template="empty",batch="city_camp",timeoutTicks=3000)
    public static void theAeroplaneStandsWholeAndAFixedRopeRunsUpThePass(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the camp and the pass are resident"))
            .thenExecute(()->{
                var at=b.offset(CitySite.PLANE);
                int fabric=0,stopping=0,x0=99,x1=-99,z0=99,z1=-99,y1=-99;var cells=new java.util.HashSet<BlockPos>();
                for(int i=0;i<CityPlaneBlock.PARTS;i++){
                    var cell=CityPlaneBlock.cell(i);var s=l.getBlockState(at.offset(cell));
                    h.assertTrue(CityPlaneBlock.is(s)&&s.getValue(CityPlaneBlock.PART)==i,"part "+i+" of the aeroplane stands in its cell "+cell+": "+s);
                    h.assertTrue(cells.add(cell),"no two parts in one cell: "+cell);
                    h.assertTrue(!CityPlaneBlock.found(i).isEmpty()&&s.getShape(l,at.offset(cell)).equals(CityPlaneBlock.found(i))&&s.getCollisionShape(l,at.offset(cell)).equals(CityPlaneBlock.stops(i)),"a hand finds part "+i+", and it has its own shape");
                    if(CityPlaneBlock.fabric(i))fabric++;if(!CityPlaneBlock.stops(i).isEmpty())stopping++;
                    x0=Math.min(x0,cell.getX());x1=Math.max(x1,cell.getX());z0=Math.min(z0,cell.getZ());z1=Math.max(z1,cell.getZ());y1=Math.max(y1,cell.getY());
                    h.assertTrue(cell.getY()>=0,"no part under the ice: "+cell);
                }
                h.assertTrue(x0==0&&x1==12&&z0==-8&&z1==8&&y1==2,"thirteen cells from tail to nose, seventeen across the wing, three high: "+x0+".."+x1+" "+z0+".."+z1+" "+y1);
                h.assertTrue(fabric>60&&fabric<CityPlaneBlock.PARTS&&stopping>80,"most of it is fabric and most of it stops a body: "+fabric+" "+stopping+" of "+CityPlaneBlock.PARTS);
                int parts=0,cloth=0;var canvas=CityRegistry.material("canvas");
                for(int x=-CitySite.CAMP_HALF;x<=CitySite.CAMP_HALF;x++)for(int y=0;y<=6;y++)for(int z=CitySite.CAMP_N;z<=0;z++){
                    var s=l.getBlockState(b.offset(x,y,z));if(CityPlaneBlock.is(s))parts++;
                    if(x>=18&&(s.is(canvas)||CityBlock.is(s,CityBlock.Kind.FABRIC)))cloth++;
                }
                h.assertTrue(parts==CityPlaneBlock.PARTS&&cloth==0,"the aeroplane's parts and nothing of the canvas box it was: "+parts+" parts, "+cloth+" of canvas");
                var nose=at.offset(CityPlaneBlock.propeller());var prop=l.getBlockState(nose);
                h.assertTrue(CityBlock.is(prop,CityBlock.Kind.PROP)&&prop.getValue(CityBlock.FACING)==Direction.EAST&&CityPlaneBlock.propeller().equals(new BlockPos(13,1,0))&&CityPlaneBlock.is(l.getBlockState(nose.west())),"the propeller on the nose, facing the way the aeroplane points: "+prop);
                // Under the wing, out past the struts: clear for a body from the ice up, and the wing over it stops one.
                var under=at.offset(8,0,5);var wing=l.getBlockState(under.above(2));
                h.assertTrue(l.noCollision(null,new AABB(under.getX()+.2,under.getY()+.01,under.getZ()+.2,under.getX()+.8,under.getY()+1.8,under.getZ()+.8))&&CityPlaneBlock.is(wing)&&!wing.getCollisionShape(l,under.above(2)).isEmpty()&&wing.getCollisionShape(l,under.above(2)).min(Direction.Axis.Y)>.25,"a body walks under the wing: "+wing);
                // What a hand is told.
                var me=f.player("city_flyer",P);int cut=-1,ski=-1;
                for(int i=0;i<CityPlaneBlock.PARTS;i++){if(cut<0&&CityPlaneBlock.fabric(i)&&CityPlaneBlock.cell(i).getY()==0)cut=i;if(ski<0&&!CityPlaneBlock.fabric(i)&&!CityPlaneBlock.stops(i).isEmpty())ski=i;}
                h.assertTrue(cut>=0&&ski>=0,"a fabric part by the ice and a part that is not fabric: "+cut+" "+ski);
                NativeTestPlayers.messages(me);f.click(me,P,CitySite.PLANE.offset(CityPlaneBlock.cell(cut)));var told=NativeTestPlayers.messages(me);
                h.assertTrue(told.stream().anyMatch(m->m.equals("The fabric has been opened in long straight cuts.")),"a hand on the aeroplane's fabric: "+told);
                f.click(me,P,CitySite.PLANE.offset(CityPlaneBlock.cell(ski)));told=NativeTestPlayers.messages(me);
                h.assertTrue(told.isEmpty(),"a hand on a ski is told nothing: "+told);
                f.click(me,P,CitySite.PLANE.offset(CityPlaneBlock.propeller()));told=NativeTestPlayers.messages(me);
                h.assertTrue(told.stream().anyMatch(m->m.equals("The propeller is whole. Nothing else is.")),"a hand on the propeller: "+told);
                // The fixed rope.
                for(int side=-1;side<=1;side+=2){
                    int last=Integer.MIN_VALUE,pieces=0;
                    for(int z=CitySite.CAMP_N;z>=CitySite.DESCENT_N;z--){
                        int x=side*CitySite.PATH,s=CitySite.stand(z);var pos=b.offset(x,s,z);var st=l.getBlockState(pos);
                        if(roped(z,side)){
                            h.assertTrue(CityRopeBlock.is(st)&&st.getValue(CityRopeBlock.FACING)==(side<0?Direction.NORTH:Direction.SOUTH),"the fixed rope at row "+z+" on side "+side+", the rock on its left: "+st);
                            h.assertTrue(st.getCollisionShape(l,pos).isEmpty()&&st.getShape(l,pos).isEmpty()==(st.getValue(CityRopeBlock.STAKE)==CityRopeBlock.Stake.NONE),"the rope stops nobody, and a hand finds only a stake: "+z);
                            for(var way:new Direction[]{Direction.NORTH,Direction.SOUTH}){
                                int nz=z+way.getStepZ(),here=CityRopeBlock.high(st,way);
                                if(!roped(nz,side)){h.assertTrue(here<0&&st.getValue(CityRopeBlock.STAKE)!=CityRopeBlock.Stake.NONE,"the rope ends at a stake at row "+z+" on side "+side+": "+st);continue;}
                                int ns=CitySite.stand(nz),there=CityRopeBlock.high(l.getBlockState(b.offset(x,ns,nz)),way.getOpposite());
                                h.assertTrue(here>=0&&16*s+here==16*ns+there&&16*s+here==16*Math.min(s,ns)+CityRopeBlock.HIGH,"the rope runs on unbroken from row "+z+" to "+nz+" on side "+side+", a hand's height over the lower ground: "+here+" "+there);
                            }
                            boolean staked=st.getValue(CityRopeBlock.STAKE)!=CityRopeBlock.Stake.NONE;
                            if(staked){h.assertTrue(last==Integer.MIN_VALUE||last-z<=4,"a stake at least every fourth row: "+last+" to "+z+" on side "+side);last=z;}
                            h.assertTrue(staked||Math.floorMod(z,4)!=3,"a stake on each landing: row "+z);
                            pieces++;
                        }else h.assertTrue(st.isAir()&&side>0&&l.getBlockState(b.offset(CitySite.PATH+1,s,z)).isAir(),"no rope across a nook's mouth at row "+z+": "+st);
                        for(int px=-CitySite.PATH;px<=CitySite.PATH;px++)for(int y=s;y<=s+2;y++)h.assertTrue(!(l.getBlockState(b.offset(px,y,z)).getBlock() instanceof FenceBlock),"no fence post left on the steps at "+px+","+y+","+z);
                    }
                    h.assertTrue(last==CitySite.DESCENT_N&&pieces==CitySite.CAMP_N-CitySite.DESCENT_N+1-(side>0?CitySite.NOOKS.length:0),"the rope from the camp's edge to the stair's foot on side "+side+": "+pieces+" pieces, the last stake at "+last);
                }
                BlackCity.resetNow(l,b);
            })
            .thenSucceed();
    }
    /** Whether a row of the pass has the fixed rope on one side: from the camp's edge to the stair's foot, but not across a nook's mouth. */
    private static boolean roped(int z,int side){return z<=CitySite.CAMP_N&&z>=CitySite.DESCENT_N&&!(side>0&&java.util.Arrays.stream(CitySite.NOOKS).anyMatch(n->n==z));}

    @GameTest(template="empty",batch="city_tunnel",timeoutTicks=5000)
    public static void theTunnelOneReaderOutrunsItAndOneStaysAndComesToByTheHut(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] r=new ServerPlayer[3];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the camp and the city are resident"))
            .thenExecute(()->{
                r[0]=f.player("city_runner",P);r[1]=f.player("city_stayer",P);r[2]=f.player("city_unread",P);
                murals(f,r[2],5);f.at(r[2],P,0.5,CitySite.FLOOR,-178.5);
            })
            .thenIdle(60)
            .thenExecute(()->{
                h.assertTrue(BlackCity.state(b)==BlackCity.IDLE&&f.own(r[2],P).getBoolean("ShoreCue"),"a reader who has not read the carvings finds only the piping, far off: nothing comes: state "+BlackCity.state(b)+" "+f.own(r[2],P)+" at "+r[2].position().subtract(b.getX(),b.getY(),b.getZ())+" current "+LiteraryVignettes.current(r[2]));
                murals(f,r[0],12);murals(f,r[1],12);f.at(r[0],P,-1.5,CitySite.FLOOR,-178.5);f.at(r[1],P,2.5,CitySite.FLOOR,-178.5);
            })
            .thenWaitUntil(()->h.assertTrue(BlackCity.state(b)==BlackCity.PIPING,"one who has read them all, on the shore: something pipes over the water"))
            .thenExecute(()->{
                for(var p:r)h.assertTrue(f.own(p,P).getBoolean("Chased"),"everyone in the cavern is in it");
                h.assertTrue(f.own(r[0],P).getBoolean("ChaseCredit")&&!f.own(r[2],P).getBoolean("ChaseCredit"),"it counts only for those who read every carving");
                f.at(r[2],P,.5,0,-110.5);BlackCity.skipPiping(b);
            })
            .thenWaitUntil(()->h.assertTrue(BlackCity.state(b)==BlackCity.CHASE&&mass(f,0),"what fills the tunnel comes up out of the water"))
            .thenExecute(()->{f.at(r[0],P,.5,CitySite.FLOOR,-160.5);h.assertTrue(f.own(r[2],P).getBoolean("Chased")==false,"the one who got out first is out of it");})
            .thenWaitUntil(()->h.assertTrue(BlackCity.dark(r[1].getUUID()),"the one who stayed on the shore is taken into the dark"))
            .thenExecute(()->{
                h.assertTrue(f.own(r[1],P).getInt("Caught")==1&&!f.own(r[1],P).getBoolean("Survived"),"taken: once");
                h.assertTrue(!BlackCity.dark(r[0].getUUID())&&BlackCity.front(b)<CitySite.chase(r[0].position().subtract(b.getX(),b.getY(),b.getZ())),"the one in the tunnel is ahead of it");
                h.assertTrue(CityBlock.is(l.getBlockState(b.offset(CitySite.LAMPS.get(3))),CityBlock.Kind.LAMP)&&l.getBlockState(b.offset(CitySite.LAMPS.get(3))).getValue(CityBlock.STAGE)==0,"the lamps on the shore go out as it passes");
            })
            .thenWaitUntil(()->h.assertTrue(!BlackCity.dark(r[1].getUUID())&&r[1].position().distanceTo(Vec3.atLowerCornerOf(b).add(CitySite.WAKE))<2,"the one it took comes to on the ice by the hut"))
            .thenExecute(()->{
                h.assertTrue(f.own(r[1],P).getInt("Murals")==12,"with every carving still read");
                f.at(r[0],P,.5,0,-112.5);
            })
            .thenWaitUntil(()->h.assertTrue(f.own(r[0],P).getBoolean("Survived"),"out of the pit ahead of it: survived"))
            .thenWaitUntil(()->h.assertTrue(BlackCity.state(b)==BlackCity.RECEDE&&!mass(f,1)&&!mass(f,10),"with nobody left in it, it goes back down and the tunnel is empty"))
            .thenExecute(()->BlackCity.resetNow(l,b))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="city_look",timeoutTicks=4000)
    public static void theLookBackFromThePassAndEachReadersOwnAccount(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] r=new ServerPlayer[2];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the camp and the city are resident"))
            .thenExecute(()->{
                r[0]=f.player("city_looker",P);r[1]=f.player("city_not_yet",P);
                for(var p:r)murals(f,p,12);
                var o=f.own(r[0],P);o.putBoolean("Survived",true);o.putInt("Caught",1);LiteraryVignettes.save(f.data(),r[0].getUUID(),P,o);
                for(int i=0;i<2;i++){f.at(r[i],P,.5+i,24,-74.5);r[i].setYRot(180);r[i].setXRot(-8);r[i].setYHeadRot(180);}
            })
            .thenWaitUntil(()->h.assertTrue(f.own(r[0],P).getBoolean("Looked")&&f.own(r[0],P).getBoolean("Ready"),"the reader who outran it looks back at the far peaks: the account is ready"))
            .thenExecute(()->{
                var o=f.own(r[1],P);h.assertTrue(!o.getBoolean("Looked")&&!o.getBoolean("Ready"),"the one who didn't outrun it sees nothing in the look back: "+o);
                f.source(r[0],P);f.read(r[0],P,CitySite.PAD);
                h.assertTrue(f.own(r[0],P).getBoolean("Completed")&&WitnessAccount.has(f.data(),r[0].getUUID(),WitnessAccount.Story.BLACK_CITY),"the account read: the story resolved");
                var text=account(f,r[0]);
                h.assertTrue(text.contains("too regular to be peaks")&&text.contains("They came down out of the stars")&&text.contains("cut out")&&text.contains("filling it from wall to wall")&&text.contains("Once I stopped")&&text.contains("against the wind"),"the account: "+text);
                h.assertTrue(!text.matches(".*\\b(afraid|terrified|happy|sad|scared|black|white)\\b.*"),"never told how they felt, nor a colour: "+text);
                f.source(r[1],P);f.click(r[1],P,CitySite.PAD);h.assertTrue(!f.own(r[1],P).getBoolean("Completed")&&!WitnessAccount.has(f.data(),r[1].getUUID(),WitnessAccount.Story.BLACK_CITY),"each account is its own reader's");
                BlackCity.resetNow(l,b);
            })
            .thenSucceed();
    }
}
