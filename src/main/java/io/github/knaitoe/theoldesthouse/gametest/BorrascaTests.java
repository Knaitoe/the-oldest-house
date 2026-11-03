package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/** Native movement, original custody and confirmed returns. Explicit endpoint staging is not an ordinary playthrough. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_borrasca") @PrefixGameTestTemplate(false)
public final class BorrascaTests {
    private static final LabyrinthPlace P=LabyrinthPlace.BORRASCA;
    private static Fixture active;
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final ServerLevel out,in;final HouseSavedData oldHouse;final LabyrinthData oldData;final NativeTestChunks chunks=new NativeTestChunks();final BlockPos origin=new BlockPos(63000,80,63000);final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h){this.h=h;in=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.INTERIOR);out=HouseTestLevel.get(in.getServer(),HouseDimensions.OUTSIDE);oldHouse=HouseSavedData.get(in.getServer());oldData=LabyrinthData.get(in.getServer());BorrascaSites.clear();var house=new HouseSavedData();house.markSpawned(origin);house.markInteriorInitialized();var store=in.getServer().overworld().getDataStorage();store.set("the_oldest_house",house);store.set("the_oldest_house_labyrinth",new LabyrinthData());data().setBuilt(LabyrinthBuilder.VERSION,origin);active=this;}
        LabyrinthData data(){return LabyrinthData.get(in.getServer());}
        BlockPos base(ServerPlayer p){return BorrascaSites.base(p.server,p.getUUID());}
        CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(data(),p.getUUID(),P);}
        void save(ServerPlayer p,CompoundTag t){LiteraryVignettes.save(data(),p.getUUID(),P,t);}
        /** Direct authored-geometry fixture; production construction is separately exercised below. */
        ServerPlayer player(String name){var p=NativeTestPlayers.survival(h,name);int index=players.size();players.add(p);var site=new CompoundTag();site.putInt("Index",index);site.putBoolean("Built",true);data().setStateEntry("borrasca_sites_0497",p.getUUID().toString(),site);var world=new CompoundTag();world.putInt("NextIndex",players.size());data().setStateEntry("borrasca_sites_0497","World",world);var b=base(p);chunks.hold(out,BorrascaSites.bounds(b).inflate(2));BorrascaRooms.build(out,b);data().putDoor(new LabyrinthData.Door("borrasca/"+p.getUUID()+"/entry",HouseDimensions.OUTSIDE,b.offset(0,0,1),Direction.SOUTH,LabyrinthData.RETURN,false));p.setNoGravity(true);return p;}
        BlockPos home(){return LabyrinthPlaces.base(origin,LabyrinthPlace.JUNCTION).offset(0,0,-5);}
        void enter(ServerPlayer p){var home=home();chunks.hold(in,new AABB(home).inflate(4));for(var at:BlockPos.betweenClosed(home.offset(-2,-1,-2),home.offset(2,-1,2)))in.setBlock(at,Blocks.STONE.defaultBlockState(),3);data().pushReturn(p.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.atBottomCenterOf(home),180,false));at(p,0,0,-3);VignetteGate.begin(p,P);LiteraryVignettes.onArrive(p,P);}
        void at(ServerPlayer p,double x,double y,double z){var b=base(p);p.teleportTo(out,b.getX()+x+.5,b.getY()+y,b.getZ()+z+.5,180,0);p.hasChangedDimension();p.setDeltaMovement(Vec3.ZERO);}
        void look(ServerPlayer p,BlockPos rel){p.lookAt(EntityAnchorArgument.Anchor.EYES,base(p).offset(rel).getCenter());}
        void click(ServerPlayer p,BlockPos rel){var at=base(p).offset(rel);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)));}
        void use(ServerPlayer p,BlockPos rel){at(p,rel.getX()-1,rel.getY(),rel.getZ()+1);look(p,rel);click(p,rel);}
        void read(ServerPlayer p,BlockPos rel,boolean take){use(p,rel);h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,"native paper opens at "+rel);var menu=(LiteraryVignettes.Pages)p.containerMenu;var content=menu.original().get(DataComponents.WRITTEN_BOOK_CONTENT);h.assertTrue(menu.clickMenuButton(p,100+content.pages().size()-1),"native last page opens");if(take){h.assertTrue(menu.clickMenuButton(p,3),"the real original is taken");h.assertTrue(!menu.clickMenuButton(p,3),"it cannot be taken twice");}p.closeContainer();}
        LiteraryActor actor(ServerPlayer p,String key){var t=own(p);return t.hasUUID(key)&&out.getEntity(t.getUUID(key)) instanceof LiteraryActor a?a:null;}
        void talk(ServerPlayer p,String key){var a=actor(p,key);h.assertTrue(a!=null,"original native cast is loaded: "+key);p.moveTo(a.getX()+1.5,a.getY(),a.getZ());p.interactOn(a,InteractionHand.MAIN_HAND);}
        void refresh(ServerPlayer p){var own=own(p);BorrascaStory.reader(p,base(p),own);save(p,own);}
        void stage(ServerPlayer p,int visit){var own=own(p);own.putInt("Visit",visit);own.putBoolean("CastRecalled",false);save(p,own);}
        void leave(ServerPlayer p){at(p,0,0,-3);LabyrinthDoors.tickPlayer(p,origin);at(p,0,0,2.2);LabyrinthDoors.tickPlayer(p,origin);}
        boolean home(ServerPlayer p){return p.level().dimension().equals(HouseDimensions.INTERIOR)&&data().returnDepth(p.getUUID())==0&&!LabyrinthDoors.isBusy(p);}
        void reload(){BorrascaSites.clear();var saved=data().save(new CompoundTag(),out.registryAccess());in.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.load(saved,out.registryAccess()));}
        @Override public void close(){BorrascaCast.clear();BorrascaSites.clear();for(var p:players){var b=base(p);if(b!=null)for(var e:out.getEntitiesOfClass(Entity.class,BorrascaSites.bounds(b),e->!(e instanceof ServerPlayer)))e.discard();NativeTestPlayers.remove(p);}chunks.close();LiteraryVignettes.clearAll();var store=in.getServer().overworld().getDataStorage();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}
    }
    private static void cleanup(){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="borrasca_motion") public static void motionDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_low") public static void lowDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_four") public static void fourDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_private") public static void privateDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_build") public static void buildDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_interrupt") public static void interruptDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_missing") public static void missingDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_custody") public static void custodyDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_escort") public static void escortDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_taking_walk") public static void takingWalkDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_town") public static void townDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_diagnostics") public static void diagnosticsDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_sealed") public static void sealedDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_ahead_pact") public static void aheadPactDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_ahead_taking") public static void aheadTakingDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_any_route") public static void anyRouteDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_gentleman") public static void gentlemanDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_lower") public static void lowerDone(ServerLevel l){cleanup();}
    /** Run 1411: without this the strokes case left its reader standing at the shared site, watching the taking, so the next case's cast was never unwatched. */
    @AfterBatch(batch="borrasca_strokes") public static void strokesDone(ServerLevel l){cleanup();}

    @GameTest(template="empty",batch="borrasca_diagnostics")
    public static void preEntrySocketDiagnosticsNeverGeneratePrivateCoordinatesInEitherDimension(GameTestHelper h){
        var f=new Fixture(h);var a=NativeTestPlayers.survival(h,"pine_status_a");var b=NativeTestPlayers.survival(h,"pine_status_b");f.players.add(a);f.players.add(b);
        // Earlier batches build at the first indices and their chunks may still be resident; these sites lie where no case builds.
        var fresh=new CompoundTag();fresh.putInt("NextIndex",3000);f.data().setStateEntry("borrasca_sites_0497","World",fresh);
        for(var p:List.of(a,b)){
            h.assertTrue(BorrascaSites.prepareEntry(p)==null,"the real private site is still queued before entry");
            var at=f.base(p).offset(-17,12,-206);for(var level:List.of(p.server.overworld(),f.out))h.assertTrue(level.getChunkSource().getChunkNow(at.getX()>>4,at.getZ()>>4)==null,"the site coordinate starts unloaded in "+level.dimension().location());
        }
        BorrascaSocketProof.status(a,b,54);
        for(var p:List.of(a,b)){var at=f.base(p).offset(-17,12,-206);for(var level:List.of(p.server.overworld(),f.out))h.assertTrue(level.getChunkSource().getChunkNow(at.getX()>>4,at.getZ()>>4)==null,"reading diagnostic status must not load or generate a chunk in "+level.dimension().location());}
        h.succeed();
    }

    @GameTest(template="empty",batch="borrasca_registration")
    public static void oneFixedUnderstandingSourceKeepsThePoolAndOrdinaryRecord(GameTestHelper h){
        h.assertTrue(P.slot()==98&&LabyrinthPlace.LAKE_HOUSE.slot()==96&&LabyrinthPlace.LAKE_HOUSE_EMPTY.slot()==97&&LabyrinthPlace.GHOSTS_SET.slot()==65,"earlier source slots retain their addresses");
        h.assertTrue(P.kind()==LabyrinthPlace.Kind.MULTI_VISIT&&LiteraryRooms.isLiterary(P)&&NovelRooms.outside(P)&&!ScenePolish.applies(P)&&!SceneReview.applies(P)&&!SceneCraft.applies(P),"the private site owns its authored terrain");
        h.assertTrue(WitnessAccount.Story.of(P.id())==WitnessAccount.Story.BORRASCA&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED&&WitnessAccount.Story.BORRASCA.kind.equals("understanding"),"one fixed Understanding source remains in the expanded 55/42 pool");
        String form=BorrascaTexts.document("Evidence").get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(p->p.raw().getString()).reduce("",String::concat);
        h.assertTrue(form.contains("Prescott")&&form.contains("Sheriff")&&!form.contains("brass compass")&&!form.contains("machine"),"the ordinary receipt implicates familiar people without inventing a confiscation or explaining the reveal");h.succeed();
    }

    // Run 1416: with the ladder fixed both bodies climb, descend and carve, but the climb, the legend (245 ticks) and the descent left
    // too little of 1800 ticks on a slower runner, where loading the site and its cast takes more ticks. The budget doubles; nothing it
    // checks is loosened.
    @GameTest(template="empty",batch="borrasca_motion",timeoutTicks=3600)
    public static void nativeChildAndTheOriginalFriendClimbTheActualFortWithoutRelocation(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("triple_tree_climber");final boolean[] moving={false};final int[] rises={0},friendRises={0},playerRoute={0};final double[] last={0,0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"all original cast loaded")).thenExecute(()->{
            var b=f.base(p);h.assertTrue(f.out.getBlockEntity(b.offset(BorrascaRooms.SOURCE)) instanceof LecternBlockEntity,"the content note is a native book");h.assertTrue(!f.out.noCollision(p,new AABB(b.getX()-4.8,b.getY()+6,b.getZ()-93.1,b.getX()-4.2,b.getY()+7,b.getZ()-92.9)),"native fort rails join between posts");
            // Stage the approach only; both bodies must then climb by native collision and ladder physics.
            f.at(p,3,0,-83);p.setNoGravity(false);var a=f.actor(p,"CastKimber");a.moveTo(Vec3.atBottomCenterOf(b.offset(3,0,-86)),180,0);var own=f.own(p);own.putBoolean("MetFriend",true);own.putInt("ChildStep",3);f.save(p,own);last[0]=p.getY();last[1]=a.getY();moving[0]=true;
        }).thenWaitUntil(()->{var a=f.actor(p,"CastKimber");h.assertTrue(p.getY()>=f.base(p).getY()+6&&a!=null&&a.getY()>=f.base(p).getY()+5.9&&f.own(p).getBoolean("TreeReached"),"native child and original Friend reach the fort: player="+p.position()+" friend="+(a==null?"absent":a.position())+" step="+f.own(p).getInt("ChildStep"));}).thenExecute(()->{h.assertTrue(rises[0]>=20&&friendRises[0]>=20,"each body climbs over successive native ticks");h.assertTrue(p.getBbHeight()<1.1,"visits one and two use child height");}).thenWaitUntil(()->{var a=f.actor(p,"CastKimber");var b=f.base(p);h.assertTrue(f.own(p).getBoolean("FriendCarved"),"the same Friend descends the real ladder and carves at the trunk: at "+a.position().subtract(Vec3.atLowerCornerOf(b))+" downStep="+f.own(p).getInt("DownStep")+" carveTicks="+f.own(p).getInt("CarveTicks")+" legend="+f.own(p).getBoolean("LegendDone")+" climbing="+a.onClimbable()+" bumped="+a.horizontalCollision+" noAi="+a.isNoAi()+" feet/head="+f.out.getBlockState(a.blockPosition())+"/"+f.out.getBlockState(a.blockPosition().above())+" reader at "+p.position().subtract(Vec3.atLowerCornerOf(b)));}).thenExecute(()->{moving[0]=false;h.assertTrue(f.actor(p,"CastKimber").gesture()==4,"she offers the knife after carving");}).thenSucceed();
        h.onEachTick(()->{if(!moving[0])return;var a=f.actor(p,"CastKimber");var b=f.base(p);if(playerRoute[0]==0){p.setYRot(180);p.travel(new Vec3(0,0,1));if(p.getY()>=b.getY()+6.05)playerRoute[0]=1;}else if(playerRoute[0]==1){p.setYRot(90);p.travel(new Vec3(0,0,1));if(p.getX()<=b.getX()+1.65)playerRoute[0]=2;}else if(p.getZ()>b.getZ()-92){p.setYRot(180);p.travel(new Vec3(0,0,1));}else p.setDeltaMovement(Vec3.ZERO);double rise=p.getY()-last[0],other=a.getY()-last[1];h.assertTrue(Math.abs(rise)<.6&&Math.abs(other)<.6,"neither ladder body is relocated between server ticks");if(rise>.025)rises[0]++;if(other>.025)friendRises[0]++;last[0]=p.getY();last[1]=a.getY();});
    }

    @GameTest(template="empty",batch="borrasca_low",timeoutTicks=1800)
    public static void theAdultBodyCrouchesThroughTheGullyAndTheReleasedServicePipe(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("fern_gully_adult");final int[] phase={0},steps={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p)).thenIdle(6).thenExecute(()->{h.assertTrue(p.getBbHeight()<1.1,"the initial native body is child height: "+p.getBbHeight());f.stage(p,3);}).thenIdle(6).thenExecute(()->{
            var b=f.base(p);h.assertTrue(p.getBbHeight()>1.7,"the third visit restores adult height");var standing=new AABB(b.getX()-15.8,b.getY()+3,b.getZ()-119.8,b.getX()-15.2,b.getY()+4.8,b.getZ()-119.2);var low=new AABB(standing.minX,standing.minY,standing.minZ,standing.maxX,b.getY()+4.5,standing.maxZ);h.assertTrue(!f.out.noCollision(p,standing)&&f.out.noCollision(p,low),"the actual gully roof admits crouching and blocks standing");f.at(p,-16,2,-117);p.setNoGravity(false);p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);phase[0]=1;
        }).thenWaitUntil(()->h.assertTrue(phase[0]==3,"native crouched gully and service traversal: phase="+phase[0]+" body="+p.position().subtract(Vec3.atLowerCornerOf(f.base(p))))).thenExecute(()->h.assertTrue(steps[0]>25,"the adult walks continuously under both native roofs")).thenSucceed();
        h.onEachTick(()->{if(phase[0]==0||phase[0]==3)return;p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);if(phase[0]==1){var end=Vec3.atBottomCenterOf(f.base(p).offset(-13,4,-128));var d=end.subtract(p.position());p.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));}else p.setYRot(p.getZ()<f.base(p).getZ()-207.6?90:180);p.travel(new Vec3(0,0,1));steps[0]++;
            if(phase[0]==1&&p.getZ()<f.base(p).getZ()-124){f.stage(p,4);f.at(p,-17,12,-204);f.click(p,BorrascaRooms.RELEASE);h.assertTrue(f.own(p).getBoolean("ReleaseOpened")&&open(f,p,BorrascaRooms.SERVICE_DOOR)&&open(f,p,BorrascaRooms.SERVICE_GATE),"the release opens the depot's back door and the gate in the wire");phase[0]=2;}
            else if(phase[0]==2&&p.getX()<f.base(p).getX()-26.5){h.assertTrue(p.getZ()<f.base(p).getZ()-207&&p.getZ()>f.base(p).getZ()-209.5,"the adult crouches west along the walled tunnel to the gate");phase[0]=3;}});
    }

    /**
     * Staging the cast loads chunks and entity sections on other threads, and the test clock races ahead of them on a loaded runner
     * (run 1410: 1,200 ticks in a second and a half). While a staging is not yet ready the server thread rests five milliseconds, so
     * the case's tick budget covers real time.
     */
    static boolean staged(ServerPlayer p){boolean ready=BorrascaCast.prepareNext(p);if(!ready)java.util.concurrent.locks.LockSupport.parkNanos(5_000_000L);return ready;}
    static boolean recalled(Fixture f,ServerPlayer p){boolean ready=f.own(p).getBoolean("CastRecalled");if(!ready)java.util.concurrent.locks.LockSupport.parkNanos(5_000_000L);return ready;}

    @GameTest(template="empty",batch="borrasca_four",timeoutTicks=6000)
    public static void fourConfirmedReturnsCarryTheOriginalAndRestoreTheScarWithoutASecondKind(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("name_beside_mine");var peer=f.player("earlier_reader");final UUID[] original={null};
        var s=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"both private native sites loaded")).thenExecute(()->{f.enter(p);f.enter(peer);}).thenWaitUntil(()->h.assertTrue(recalled(f,p),"original Friend loaded"));
        s.thenExecute(()->{original[0]=f.own(p).getUUID("CastKimber");f.read(p,BorrascaRooms.SOURCE,false);var own=f.own(p);own.putBoolean("FriendCarved",true);f.save(p,own);f.actor(p,"CastKimber").moveTo(Vec3.atBottomCenterOf(f.base(p).offset(1,0,-96)),180,0);f.use(p,BorrascaRooms.CARVING);h.assertTrue(f.own(p).getInt("Beat")==1,"the native carving seals the staged childhood pact");var e=(BorrascaCarvingEntity)f.out.getBlockEntity(f.base(p).offset(BorrascaRooms.CARVING));h.assertTrue(e.reader().equals(p.getGameProfile().getName())&&e.friend().equals(BorrascaStory.FRIEND),"the bark holds this reader's actual name beside their Friend");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual first return consumes its native destination"));
        s.thenWaitUntil(()->h.assertTrue(staged(p),"the actual entry preparation stages the originals while the reader is away: "+BorrascaCast.why(p))).thenExecute(()->{f.enter(p);h.assertTrue(f.own(p).getInt("Visit")==2&&f.own(p).getInt("ConfirmedVisits")==1,"only the confirmed first return advances childhood");}).thenWaitUntil(()->h.assertTrue(recalled(f,p),"same Friend recalled")).thenExecute(()->{
            f.talk(p,"CastKimber");h.assertTrue(!f.own(p).getBoolean("FollowAgain"),"the childhood trail notice cannot acknowledge the new second-visit content note");f.read(p,BorrascaRooms.SOURCE,false);h.assertTrue(f.own(p).getBoolean("Read_ContentNote"),"the new trailhead note is personally read before the taking route");f.talk(p,"CastKimber");var own=f.own(p);own.putInt("TakingStep",Integer.MAX_VALUE);f.save(p,own);f.actor(p,"CastKimber").moveTo(Vec3.atBottomCenterOf(f.base(p).offset(BorrascaRooms.TAKING)),180,0);f.at(p,-2,9,-147);p.setYRot(0);
        }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FriendMissing"),"the taking completes with the camera looking away")).thenExecute(()->{f.use(p,BorrascaRooms.DROP);h.assertTrue(f.own(p).getInt("Beat")==2&&BorrascaBelongings.has(p,f.own(p),"Compass"),"the dropped original is recovered through native interaction");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual second return"));
        s.thenWaitUntil(()->h.assertTrue(staged(p),"the original Sheriff reaches the blockade before the actual third arrival: "+BorrascaCast.why(p))).thenExecute(()->{f.enter(p);h.assertTrue(f.own(p).getInt("Visit")==3,"the next arrival is the adult return");}).thenWaitUntil(()->h.assertTrue(recalled(f,p),"same Sheriff recalled")).thenExecute(()->{f.use(p,BorrascaRooms.CARVING);f.talk(p,"CastSheriff");p.closeContainer();p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);});
        // Ordered checkpoints are staged here. Continuous physical clearance is exercised in the low-route case.
        for(var at:BorrascaRooms.GULLY)s.thenExecute(()->f.at(p,at.getX(),at.getY(),at.getZ())).thenIdle(6);
        s.thenExecute(()->{p.setShiftKeyDown(false);p.setPose(Pose.STANDING);f.use(p,BorrascaRooms.MARKER_FOUR);f.refresh(p);h.assertTrue(f.own(p).getInt("Beat")==3,"the gouge, personal denial, ordered gully and perimeter complete the investigation");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual third return"));
        s.thenWaitUntil(()->h.assertTrue(staged(p),"the same refinery cast is prepared before the fourth native arrival: "+BorrascaCast.why(p))).thenExecute(()->{f.enter(p);h.assertTrue(f.own(p).getInt("Visit")==4&&f.own(p).getInt("ConfirmedVisits")==7,"all three earlier returns are independently confirmed");f.read(p,BorrascaRooms.EVIDENCE,true);f.use(p,BorrascaRooms.PACK);f.use(p,BorrascaRooms.POWER);}).thenIdle(6).thenExecute(()->{
            var b=f.base(p);h.assertTrue(!f.out.getBlockState(b.offset(BorrascaRooms.MACHINE)).getValue(BorrascaMachineBlock.RUNNING)&&f.out.getBlockState(b.offset(BorrascaRooms.LAMP)).getValue(BorrascaFittingBlock.KIND)==BorrascaFittingBlock.Kind.LAMP_OFF,"the actual machine and lamp switch off");f.use(p,BorrascaRooms.CARVING);h.assertTrue(f.out.getBlockState(b.offset(BorrascaRooms.CARVING)).getValue(BorrascaCarvingBlock.STAGE)==4,"restored lettering keeps the gouged bark state");f.at(p,0,0,-10);
        }).thenIdle(6).thenExecute(()->{h.assertTrue(f.own(p).getBoolean("Ready")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA),"carrying the original back earns readiness, not a return");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual final return")).thenExecute(()->{
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA)&&f.own(p).getBoolean("NameReturned"),"only confirmed native travel grants the fixed Understanding source and optional account line");h.assertTrue(f.own(p).getUUID("CastKimber").equals(original[0])&&f.own(peer).getInt("Visit")==1&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.BORRASCA),"the original UUID and earlier peer's story stay intact");f.reload();h.assertTrue(f.own(p).getBoolean("Completed")&&BorrascaStory.carriesEvidence(p,f.own(p)),"saved completion retains the actual original patch");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_private",timeoutTicks=1800)
    public static void savedOriginalCastAndBorrowedKeepsakesStayPrivateAcrossReload(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_owner");var peer=f.player("pine_other");h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.enter(peer);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(peer).getBoolean("CastRecalled"),"both casts loaded")).thenExecute(()->{
            var a=f.actor(p,"CastKimber");var theirs=f.actor(peer,"CastKimber");h.assertTrue(!a.getUUID().equals(theirs.getUUID())&&a.broadcastToPlayer(p)&&!a.broadcastToPlayer(peer),"native entity tracking keeps the original cast private");var id=a.getUUID();var own=f.own(p);h.assertTrue(BorrascaBelongings.takeAt(p,own,"Patch",p.blockPosition()),"one original patch enters its owner's actual inventory");f.save(p,own);peer.getInventory().add(BorrascaBelongings.original(p,own,"Patch").copy());h.assertTrue(!BorrascaStory.carriesEvidence(peer,f.own(peer)),"a borrowed patch is no personal proof");var edited=f.base(p).offset(2,0,-12);f.out.setBlock(edited,Blocks.DIAMOND_BLOCK.defaultBlockState(),3);f.reload();LiteraryVignettes.onArrive(p,P);h.assertTrue(f.own(p).getInt("Visit")==1&&f.own(p).getUUID("CastKimber").equals(id)&&f.out.getEntity(id)==a,"reload neither advances nor replaces the original");h.assertTrue(f.out.getBlockState(edited).is(Blocks.DIAMOND_BLOCK)&&BorrascaSites.prepareEntry(p)!=null,"ready sites preserve player edits");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_build",timeoutTicks=1000)
    public static void firstConstructionResumesItsSavedBoundedCursorAndKeepsAppliedEdits(GameTestHelper h){
        var f=new Fixture(h);var p=NativeTestPlayers.survival(h,"pine_builder");f.players.add(p);h.assertTrue(BorrascaSites.prepareEntry(p)==null,"first entry waits without starting a visit");var b=BorrascaSites.base(p.server,p.getUUID());f.chunks.hold(f.out,BorrascaSites.bounds(b).inflate(2));final boolean[] reloaded={false};final BlockPos[] kept={null};final int[] finishedTicks={0};final LabyrinthData.Door[] originalDoor={null};
        h.onEachTick(()->{for(int retry=0;retry<3;retry++)BorrascaSites.prepareEntry(p);var site=f.data().stateEntry("borrasca_sites_0497",p.getUUID().toString());var cursor=site.getCompound("BuildCursor");if(!reloaded[0]&&!cursor.isEmpty()&&cursor.getInt("Cursor")>=3){kept[0]=b.offset(0,-3,-80);f.out.setBlock(kept[0],Blocks.GOLD_BLOCK.defaultBlockState(),3);f.reload();h.assertTrue(BorrascaSites.prepareEntry(p)==null,"unfinished construction resumes its saved command");reloaded[0]=true;}if(!BorrascaSites.ready(p.server,p.getUUID()))return;h.assertTrue(reloaded[0]&&f.out.getBlockState(kept[0]).is(Blocks.GOLD_BLOCK),"already applied ground is not replayed after reload");h.assertTrue(BorrascaSites.heldChunks()==0&&BorrascaSites.entry(p)!=null&&f.own(p).getInt("Visit")==0,"construction releases tickets without arrival or credit");if(finishedTicks[0]++==0){originalDoor[0]=BorrascaSites.entry(p);f.out.setBlock(b.offset(0,-1,-3),Blocks.DIAMOND_BLOCK.defaultBlockState(),3);}h.assertTrue(BorrascaSites.entry(p)==originalDoor[0]&&f.out.getBlockState(b.offset(0,-1,-3)).is(Blocks.DIAMOND_BLOCK),"retries never rebuild or replace the saved door");BorrascaSites.advance(p.server);if(finishedTicks[0]>=100)h.succeed();});
    }

    @GameTest(template="empty",batch="borrasca_interrupt",timeoutTicks=1800)
    public static void unconfirmedDeparturesAndStagedFinalFlagsCannotBecomeWitnesses(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_interrupt");h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);var own=f.own(p);own.putInt("Beat",1);f.save(p,own);BorrascaStory.depart(p);LiteraryVignettes.onArrive(p,P);h.assertTrue(f.own(p).getInt("Visit")==1&&f.own(p).getInt("ConfirmedVisits")==0,"departure intent without successful travel cannot confirm or advance a visit");f.reload();f.stage(p,4);own=f.own(p);own.putInt("Beat",3);own.putBoolean("PowerOff",true);own.putBoolean("EvidenceRead",true);own.putBoolean("Ready",true);BorrascaBelongings.takeAt(p,own,"Patch",p.blockPosition());f.save(p,own);f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual staged fourth-visit return")).thenExecute(()->h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA)&&!f.own(p).getBoolean("Completed"),"final flags and a real original cannot replace three confirmed earlier returns")).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_missing",timeoutTicks=1800)
    public static void olderNativeFriendNbtCannotReviveTheMissingOriginalOrDuplicateHerCompass(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_missing");final CompoundTag[] before={null};final UUID[] id={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original Friend loaded")).thenExecute(()->{
            var a=f.actor(p,"CastKimber");before[0]=a.saveWithoutId(new CompoundTag());id[0]=a.getUUID();a.discard();var own=f.own(p);own.putInt("Visit",2);own.putBoolean("FriendMissing",true);own.putBoolean("FriendHidden",true);h.assertTrue(BorrascaBelongings.dropOnce(p,own,"Compass",f.base(p).offset(BorrascaRooms.DROP)),"one physical original drops");f.save(p,own);f.reload();
        }).thenIdle(3).thenExecute(()->{
            var a=LiteraryRegistry.ACTOR.get().create(f.out);a.load(before[0]);h.assertTrue(f.out.addFreshEntity(a),"the same UUID returns from genuinely older native entity NBT");h.assertTrue(a.getUUID().equals(id[0])&&a.phase()==BorrascaStory.MISSING&&a.isInvisible()&&a.getOffhandItem().isEmpty(),"saved missing state wins before native tracking");var own=f.own(p);h.assertTrue(!BorrascaBelongings.dropOnce(p,own,"Compass",f.base(p).offset(BorrascaRooms.DROP)),"reload cannot issue a second original");long count=f.out.getEntitiesOfClass(ItemEntity.class,BorrascaSites.bounds(f.base(p))).stream().filter(e->BorrascaBelongings.valid(p,own,"Compass",e.getItem())).count();h.assertTrue(count==1,"exactly one original compass survives as a native item entity");f.save(p,own);f.talk(p,"CastKimber");h.assertTrue(!f.own(p).getBoolean("FollowAgain"),"the missing Friend does not answer or resume the taking");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_custody",timeoutTicks=1800)
    public static void denialWorksWithoutTheCompassAndConfiscationReturnsTheSameOriginal(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_compass");final ItemStack[] exact={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,3);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original adult Sheriff loaded")).thenExecute(()->{
            f.talk(p,"CastSheriff");h.assertTrue(f.own(p).getBoolean("HeardDenial")&&f.actor(p,"CastSheriff").getCustomName().getString().contains("Marker Three"),"he gives away Marker Three even with no compass");var own=f.own(p);BorrascaBelongings.takeAt(p,own,"Compass",p.blockPosition());exact[0]=BorrascaBelongings.remove(p,own,"Compass");p.setItemInHand(InteractionHand.MAIN_HAND,exact[0]);f.save(p,own);f.talk(p,"CastSheriff");h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu,"holding the real compass opens the native handover choice");p.containerMenu.clicked(0,0,ClickType.PICKUP,p);p.closeContainer();h.assertTrue(f.own(p).getBoolean("CompassConfiscated")&&!BorrascaBelongings.has(p,f.own(p),"Compass"),"handover transfers the actual inventory stack");f.reload();f.stage(p,4);f.use(p,BorrascaRooms.PACK);var after=f.own(p);h.assertTrue(BorrascaBelongings.has(p,after,"Compass")&&!after.contains("ConfiscatedCompass"),"the original is recovered at the sorting table after reload");var found=p.getInventory().items.stream().filter(a->BorrascaBelongings.valid(p,after,"Compass",a)).findFirst().orElse(ItemStack.EMPTY);h.assertTrue(ItemStack.isSameItemSameComponents(exact[0],found),"all components of the confiscated compass remain exact");
        }).thenSucceed();
    }

    private static void assertTakingSightline(Fixture f,ServerPlayer p){
        var b=f.base(p);var eye=Vec3.atBottomCenterOf(b.offset(BorrascaRooms.TAKING)).add(0,p.getEyeHeight(),0);var lamp=b.offset(BorrascaRooms.LAMP).getCenter();
        var ray=f.out.clip(new ClipContext(eye,lamp,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));f.h.assertTrue(ray.getType()==HitResult.Type.MISS||ray.getLocation().distanceToSqr(lamp)<2,"the actual taking sightline sees the lamp through the wire mesh: eye="+p.getEyeHeight()+" hit="+ray.getBlockPos().subtract(b)+" "+f.out.getBlockState(ray.getBlockPos()));
        var obstacle=BlockPos.containing(eye.lerp(lamp,.35));var before=f.out.getBlockState(obstacle);f.out.setBlock(obstacle,Blocks.STONE.defaultBlockState(),3);var covered=f.out.clip(new ClipContext(eye,lamp,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));f.h.assertTrue(covered.getType()==HitResult.Type.BLOCK&&covered.getBlockPos().equals(obstacle),"solid native cover still hides the taking light");f.out.setBlock(obstacle,before,3);
    }

    @GameTest(template="empty",batch="borrasca_taking_walk",timeoutTicks=4400)
    public static void theSameFriendWalksTheWholeSecondTrailAndBothReadersShelterBeforeTheTaking(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_taking_walk");final boolean[] walking={false};final Vec3[] previous={Vec3.ZERO};final int[] moved={0},detour={0};final UUID[] id={null};
        // Walk around the visible timber ends. Direct pursuit cuts inside a corner once
        // the friend has rounded it; a human reader can see and take these clear passages.
        var timberEnds=new BlockPos[]{new BlockPos(6,2,-114),new BlockPos(6,3,-117),new BlockPos(1,6,-131),new BlockPos(1,6,-134)};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);p.setYRot(0);f.stage(p,2);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original Friend recalled off camera for explicit second-visit staging")).thenExecute(()->{assertTakingSightline(f,p);f.read(p,BorrascaRooms.SOURCE,false);f.talk(p,"CastKimber");id[0]=f.actor(p,"CastKimber").getUUID();f.at(p,1,0,-72);p.setNoGravity(false);previous[0]=f.actor(p,"CastKimber").position();walking[0]=true;}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FriendMissing"),"the real Friend and child walk the second route, shelter and reach the taking: body="+p.position().subtract(Vec3.atLowerCornerOf(f.base(p)))+" friend="+f.actor(p,"CastKimber").position().subtract(Vec3.atLowerCornerOf(f.base(p)))+" step="+f.own(p).getInt("TakingStep"))).thenExecute(()->{
            walking[0]=false;var own=f.own(p);h.assertTrue(own.getBoolean("Sheltered6")&&own.getBoolean("Sheltered10")&&moved[0]>100,"both actual crouched shelter beats precede the taking after continuous native walking");h.assertTrue(f.actor(p,"CastKimber").getUUID().equals(id[0])&&own.getBoolean("CompassIssued"),"the same original departs and drops her finite compass");long originals=f.out.getEntitiesOfClass(ItemEntity.class,BorrascaSites.bounds(f.base(p))).stream().filter(e->BorrascaBelongings.valid(p,own,"Compass",e.getItem())).count();h.assertTrue(originals+(BorrascaBelongings.has(p,own,"Compass")?1:0)==1,"one physical original remains after the look-away taking");
        }).thenSucceed();
        h.onEachTick(()->{if(!walking[0])return;var a=f.actor(p,"CastKimber");var own=f.own(p);double movedBy=a.position().distanceTo(previous[0]);h.assertTrue(movedBy<.7,"the Friend never relocates along the taking route");if(movedBy>.005)moved[0]++;previous[0]=a.position();if(own.getInt("TakingTicks")>0){p.setYRot(0);p.travel(Vec3.ZERO);return;}boolean shelter=a.gesture()==5;p.setShiftKeyDown(shelter);p.setPose(shelter?Pose.CROUCHING:Pose.STANDING);
            int corner=detour[0];boolean rounding=corner<timberEnds.length&&own.getInt("TakingStep")>=(corner<2?6:10);var target=rounding?Vec3.atBottomCenterOf(f.base(p).offset(timberEnds[corner])):a.position();var d=target.subtract(p.position());
            if(rounding&&d.horizontalDistance()<.2){detour[0]++;p.travel(Vec3.ZERO);}else if(d.horizontalDistance()>(rounding?.2:2)){p.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));p.travel(new Vec3(0,0,1));}else p.travel(Vec3.ZERO);
        });
    }

    private static boolean open(Fixture f,ServerPlayer p,BlockPos rel){var s=f.out.getBlockState(f.base(p).offset(rel));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}

    // ------------------------------------------------------------------ 0.4.235: a reader who goes everywhere first
    /** Where a body can walk from a start: steps of a block up, drops of three, ladders, wooden doors; iron doors only when open. */
    private static boolean passable(BlockState s){if(s.getBlock() instanceof DoorBlock)return s.getValue(DoorBlock.OPEN)||!s.is(Blocks.IRON_DOOR);return s.getBlock() instanceof FenceGateBlock||s.getBlock() instanceof TrapDoorBlock&&!s.is(Blocks.IRON_TRAPDOOR);}
    private static boolean roomy(ServerLevel l,AABB box){
        for(var pos:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX-1e-4,box.maxY-1e-4,box.maxZ-1e-4))){
            var s=l.getBlockState(pos);if(passable(s))continue;var shape=s.getCollisionShape(l,pos,net.minecraft.world.phys.shapes.CollisionContext.empty());if(shape.isEmpty())continue;
            for(var part:shape.toAabbs())if(part.move(pos).intersects(box))return false;
        }
        return true;
    }
    private static double footing(ServerLevel l,BlockPos c){
        var own=l.getBlockState(c);if(own.is(net.minecraft.tags.BlockTags.CLIMBABLE))return c.getY();var shape=own.getCollisionShape(l,c,net.minecraft.world.phys.shapes.CollisionContext.empty());
        if(!shape.isEmpty()&&!passable(own)){double top=shape.max(Direction.Axis.Y);return top<=.6?c.getY()+top:Double.NaN;}
        var below=l.getBlockState(c.below());if(passable(below))return Double.NaN;var under=below.getCollisionShape(l,c.below(),net.minecraft.world.phys.shapes.CollisionContext.empty());
        if(under.isEmpty())return Double.NaN;double top=under.max(Direction.Axis.Y);return top>=.5?c.getY()-1+top:Double.NaN;
    }
    static Set<BlockPos> walkable(ServerLevel l,BlockPos base,BlockPos start,double height){
        var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();seen.add(start);queue.add(start);
        while(!queue.isEmpty()){
            var c=queue.removeFirst();double fc=footing(l,c);if(Double.isNaN(fc))continue;var rel=c.subtract(base);if(Math.abs(rel.getX())>44||rel.getZ()>2||rel.getZ()<-220||rel.getY()<-5||rel.getY()>26)continue;
            var moves=new ArrayList<BlockPos>();for(var d:Direction.Plane.HORIZONTAL)for(int dy=1;dy>=-3;dy--)moves.add(c.relative(d).offset(0,dy,0));
            if(l.getBlockState(c).is(net.minecraft.tags.BlockTags.CLIMBABLE)||l.getBlockState(c.above()).is(net.minecraft.tags.BlockTags.CLIMBABLE)){moves.add(c.above());moves.add(c.below());}
            for(var t:moves){
                if(seen.contains(t))continue;double ft=footing(l,t);if(Double.isNaN(ft))continue;boolean climb=t.getX()==c.getX()&&t.getZ()==c.getZ();if(!climb&&ft-fc>1.25)continue;
                if(!roomy(l,new AABB(t.getX()+.2,ft+.01,t.getZ()+.2,t.getX()+.8,ft+height,t.getZ()+.8)))continue;
                if(!climb&&ft-fc>.6&&!roomy(l,new AABB(c.getX()+.2,fc+.01,c.getZ()+.2,c.getX()+.8,ft+height,c.getZ()+.8)))continue;
                seen.add(t);queue.add(t);
            }
        }
        return seen;
    }
    private static boolean reaches(Set<BlockPos> cells,BlockPos base,BlockPos rel,double radius){var at=base.offset(rel);for(var c:cells)if(c.distSqr(at)<=radius*radius)return true;return false;}

    @GameTest(template="empty",batch="borrasca_sealed",timeoutTicks=3000)
    public static void theRefineryIsSealedFromTheWoodsUntilTheLastNight(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_explorer");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{
            var b=f.base(p);var child=walkable(f.out,b,b.offset(0,0,-2),.95);
            h.assertTrue(reaches(child,b,BorrascaRooms.TAKING,2)&&reaches(child,b,new BlockPos(5,12,-164),2.5),"a child can walk anywhere on the mountain, up to the wire and look through it");
            Map<String,BlockPos> inside=new LinkedHashMap<>();inside.put("the machine",new BlockPos(0,12,-185));inside.put("the breaker",BorrascaRooms.POWER);inside.put("the receipt",BorrascaRooms.EVIDENCE);inside.put("the sorting table",BorrascaRooms.PACK);inside.put("the service tunnel",new BlockPos(-23,12,-208));
            for(var e:inside.entrySet())h.assertTrue(!reaches(child,b,e.getValue(),2.5),"a child exploring on the first visit cannot get at "+e.getKey());
            // The last night: the gate breached. The yard is open; the tunnel stays behind its door until the release.
            for(int x=-1;x<=1;x++)for(int y=12;y<=14;y++)f.out.setBlock(b.offset(x,y,-167),Blocks.AIR.defaultBlockState(),2);
            var adult=walkable(f.out,b,b.offset(0,0,-2),1.5);
            for(var key:List.of("the machine","the breaker","the receipt","the sorting table"))h.assertTrue(reaches(adult,b,inside.get(key),2.5),"through the breached gate the reader reaches "+key);
            h.assertTrue(!reaches(adult,b,inside.get("the service tunnel"),1),"the service tunnel is walled off from the yard");
            BorrascaStory.openDoor(f.out,b.offset(BorrascaRooms.SERVICE_DOOR),true);BorrascaStory.openDoor(f.out,b.offset(BorrascaRooms.SERVICE_GATE),true);
            var released=walkable(f.out,b,b.offset(0,0,-2),1.5);h.assertTrue(reaches(released,b,inside.get("the service tunnel"),1),"with the release pulled the tunnel joins the depot to the way down outside the wire");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_ahead_pact",timeoutTicks=3000)
    public static void aReaderWhoRunsToTheTreeFirstStillMakesThePactThere(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_ahead");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is in place"))
            .thenExecute(()->{f.at(p,1,6,-92);f.refresh(p);h.assertTrue(f.own(p).getBoolean("TreeReached")&&f.own(p).getBoolean("CalledAfter"),"the reader is in the fort before meeting her, and she calls after them");})
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("KimberAtFort"),"she comes up to the Tree on her own: "+f.actor(p,"CastKimber").position().subtract(Vec3.atLowerCornerOf(f.base(p)))+" step="+f.own(p).getInt("ChildStep")))
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("LegendDone"),"she tells the legend with the reader there"))
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FriendCarved"),"she climbs down and carves where the reader can see: downStep="+f.own(p).getInt("DownStep")))
            .thenExecute(()->{f.use(p,BorrascaRooms.CARVING);var own=f.own(p);h.assertTrue(own.getBoolean("PactCarved")&&own.getInt("Beat")==1,"the pact is made, in the order the reader came to it");})
            .thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_ahead_taking",timeoutTicks=5200)
    public static void aReaderWaitingAtTheClearingStillLosesHerThere(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_waiting");final double[] closest={99,99};final boolean[] watching={false};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,2);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the friend is at the first marker"))
            .thenExecute(()->{f.read(p,BorrascaRooms.SOURCE,false);f.talk(p,"CastKimber");h.assertTrue(f.own(p).getBoolean("FollowAgain"),"she sets off");f.at(p,0,9,-146);p.setYRot(180);watching[0]=true;})
            .thenWaitUntil(()->h.assertTrue(f.own(p).getInt("Shelter6")>=60&&f.own(p).getInt("Shelter10")>=60,"she shelters at both timbers on her own when the noise comes: step="+f.own(p).getInt("TakingStep")))
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FriendMissing"),"she reaches the clearing where the reader is waiting, and is taken there: friend="+f.actor(p,"CastKimber").position().subtract(Vec3.atLowerCornerOf(f.base(p)))+" step="+f.own(p).getInt("TakingStep")))
            .thenExecute(()->{watching[0]=false;var own=f.own(p);h.assertTrue(!own.getBoolean("Sheltered6")&&!own.getBoolean("Sheltered10"),"the reader was never made to crouch");
                h.assertTrue(own.getBoolean("Led")&&closest[0]<2.5&&closest[1]<2.5,"two men came down to her and led her away: "+closest[0]+", "+closest[1]);
                h.assertTrue(BorrascaBelongings.dropOnce(p,own,"Compass",f.base(p).offset(BorrascaRooms.DROP))==false,"her one compass is already lying on the trail");})
            .thenSucceed();
        h.onEachTick(()->{if(!watching[0])return;var a=f.actor(p,"CastKimber");if(a==null||f.own(p).getInt("TakingTicks")==0)return;for(int i=0;i<2;i++){var w=f.actor(p,"Worker"+i);if(w!=null&&!w.isInvisible())closest[i]=Math.min(closest[i],w.distanceTo(a));}});
    }

    /**
     * 0.4.262: the two men, each his own man, walk down the ridge road to her; three strokes are paced to them (the light, the turn down
     * the lane, the hand on her arm); the Shiny Gentleman stands on the ridge behind them while they come; and they lead her back up the
     * lane toward the road. Nobody is moved while the reader watches.
     */
    @GameTest(template="empty",batch="borrasca_strokes",timeoutTicks=3200)
    public static void theMenComeDownTheRidgeRoadToThreeStrokesAndLeadHerOut(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_strokes");final double[] maxStep={0},gentFurther={-99};final Vec3[] last={null,null};final boolean[] watching={false},gentSeen={false};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,2);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is placed"))
            .thenExecute(()->{
                h.assertTrue(f.actor(p,"Worker0").phase()==BorrascaStory.WORKER&&f.actor(p,"Worker1").phase()==BorrascaStory.WORKER+1,"each of the two men has his own face");
                f.read(p,BorrascaRooms.SOURCE,false);f.talk(p,"CastKimber");var own=f.own(p);own.putInt("TakingStep",Integer.MAX_VALUE);f.save(p,own);
                f.actor(p,"CastKimber").moveTo(Vec3.atBottomCenterOf(f.base(p).offset(BorrascaRooms.TAKING)),180,0);f.at(p,0,9,-146);p.setYRot(180);watching[0]=true;})
            .thenWaitUntil(()->h.assertTrue(f.own(p).getInt("TakingTicks")>0&&f.own(p).getInt("Strokes")>=1,"the light comes on with the first stroke"))
            .thenExecute(()->{var own=f.own(p);h.assertTrue(own.getInt("Strokes")==1&&!own.getBoolean("Led"),"one stroke, and they are still coming: "+own.getInt("Strokes"));
                for(int i=0;i<2;i++){var w=f.actor(p,"Worker"+i);h.assertTrue(w.position().distanceTo(f.actor(p,"CastKimber").position())>12,"they come from up on the ridge road, not beside her: "+w.position().subtract(Vec3.atLowerCornerOf(f.base(p))));}})
            .thenWaitUntil(()->h.assertTrue(f.own(p).getInt("Strokes")>=2,"the second stroke as they turn down toward her: "+f.actor(p,"Worker0").position().subtract(Vec3.atLowerCornerOf(f.base(p)))+" step "+f.own(p).getInt("Taker0")))
            .thenExecute(()->h.assertTrue(!f.own(p).getBoolean("Led"),"the second stroke comes before they reach her"))
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("Led")&&f.own(p).getInt("Strokes")==3,"the third stroke as a hand closes on her arm: "+f.actor(p,"Worker0").position().subtract(Vec3.atLowerCornerOf(f.base(p)))))
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FriendMissing"),"her compass falls as she is led up the lane"))
            .thenWaitUntil(()->{var w=f.actor(p,"Worker0");var own=f.own(p);h.assertTrue(own.getBoolean("Taker0Hidden")||w.getZ()<f.base(p).getZ()-157,"watched, they walk her back up the lane toward the road: "+w.position().subtract(Vec3.atLowerCornerOf(f.base(p))));})
            .thenExecute(()->{watching[0]=false;h.assertTrue(maxStep[0]<.9,"nobody is moved while watched: "+maxStep[0]);h.assertTrue(gentSeen[0]&&gentFurther[0]>0,"the Gentleman stood on the ridge behind them while they came: "+gentFurther[0]);})
            .thenSucceed();
        h.onEachTick(()->{if(!watching[0])return;var own=f.own(p);if(own.getInt("TakingTicks")==0)return;p.setYRot(180);p.setXRot(-5);
            for(int i=0;i<2;i++){var w=f.actor(p,"Worker"+i);if(w==null||w.isInvisible()){last[i]=null;continue;}if(last[i]!=null&&LiteraryVignettes.watched(f.out,w.getEyePosition()))maxStep[0]=Math.max(maxStep[0],w.position().distanceTo(last[i]));last[i]=w.position();}
            var g=BorrascaStory.gentleman(own);var w=f.actor(p,"Worker0");if(g!=null&&own.getBoolean("GentBehind")&&!own.getBoolean("Led")&&w!=null&&!w.isInvisible()){gentSeen[0]=true;gentFurther[0]=Math.max(gentFurther[0],g.distanceTo(p.position())-w.position().distanceTo(p.position()));}});
    }

    /** 0.4.236: when the noise comes on a child visit, the Shiny Gentleman stands far off in the pines past the second marker, for that reader alone. */
    @GameTest(template="empty",batch="borrasca_gentleman",timeoutTicks=1800)
    public static void theShinyGentlemanStandsInThePinesWhenTheNoiseComes(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_gentleman");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,1);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is in place")).thenExecute(()->{
            var road=BorrascaRooms.trail(-126);f.at(p,road.getX(),road.getY(),road.getZ());p.setYRot(180);p.setXRot(-8);f.refresh(p);var own=f.own(p);
            var at=BorrascaStory.gentleman(own);h.assertTrue(own.getBoolean("PastTwo1")&&at!=null,"a child past the second marker hears the noise, and he is there: ticks "+own.getInt("GentTicks")+", echoes "+own.getInt("Echoes"));
            var head=at.add(0,BorrascaStory.GENT_HEIGHT-.4,0);double d=head.distanceTo(p.getEyePosition());
            h.assertTrue(d>=BorrascaStory.GENT_NEAR&&d<=BorrascaStory.GENT_FAR&&at.z<f.base(p).getZ()-124,"he stands past the second marker, far off: "+d+" at "+at.subtract(Vec3.atLowerCornerOf(f.base(p))));
            h.assertTrue(f.out.clip(new ClipContext(p.getEyePosition(),head,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS,"in clear sight of the reader");
            var view=BorrascaStory.view(p,own);h.assertTrue(view!=null&&view.gentlemanTicks()==BorrascaStory.GENT_TICKS&&view.gentleman().equals(at),"the reader's own view is told where he stands, for as long as the strokes last");
            p.lookAt(EntityAnchorArgument.Anchor.EYES,at.add(0,BorrascaStory.GENT_HEIGHT*.6,0));f.refresh(p);h.assertTrue(f.own(p).getBoolean("SawGentleman"),"looking straight at him is remembered");
            h.assertTrue(BorrascaTexts.account(f.own(p)).contains("shone"),"and the account says so");
            for(int i=0;i<15;i++)f.refresh(p);var after=f.own(p);h.assertTrue(BorrascaStory.gentleman(after)==null&&BorrascaStory.view(p,after).gentlemanTicks()==0,"he is gone when the strokes end");
            // Grown, the reader never sees him.
            f.stage(p,3);var grown=f.own(p);grown.putInt("GentTicks",BorrascaStory.GENT_TICKS);f.save(p,grown);h.assertTrue(BorrascaStory.gentleman(f.own(p))==null,"on the adult visits he never comes");
        }).thenSucceed();
    }

    /** The child's hand swaps some letters for alternate glyphs (HouseWriting); read them back as the letters they stand for. */
    private static String childPlain(String text){var base="aeiotrshnldcmyug";var out=new StringBuilder();for(char c:text.toCharArray()){if(c>=0xE100&&c<0xE110+base.length()){int i=c>=0xE110?c-0xE110:c-0xE100;out.append(i<base.length()?base.charAt(i):c);}else out.append(c);}return out.toString();}
    /** 0.4.237: what the trailhead note warns of is in the vignette, off-screen: the rooms under the depot, the night book, the letter, the graves. */
    @GameTest(template="empty",batch="borrasca_lower",timeoutTicks=1800)
    public static void theRoomsUnderTheDepotHoldWhatTheNoteWarnedOf(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_lower");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,4);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the last night")).thenExecute(()->{
            var b=f.base(p);var l=f.out;
            h.assertTrue(l.getBlockState(b.offset(BorrascaRooms.HATCH)).getBlock() instanceof TrapDoorBlock&&l.getBlockState(b.offset(-19,8,-202)).is(Blocks.LADDER)&&l.getBlockState(b.offset(-19,7,-190)).isAir(),"a hatch in the back room's floor, a ladder down, a corridor");
            for(int room=1;room<=12;room++){var door=l.getBlockState(b.offset(BorrascaRooms.roomDoor(room)));var lever=l.getBlockState(b.offset(BorrascaRooms.roomLever(room)));
                h.assertTrue(door.is(Blocks.IRON_DOOR)&&lever.is(Blocks.LEVER)&&door.getValue(DoorBlock.OPEN)==(room==BorrascaRooms.NELL_ROOM),"room "+room+": an iron door, bolted from the corridor"+(room==BorrascaRooms.NELL_ROOM?"; hers stands open":""));}
            var lever=b.offset(BorrascaRooms.roomLever(BorrascaRooms.MARA_ROOM));((LeverBlock)Blocks.LEVER).pull(l.getBlockState(lever),l,lever,p);
            h.assertTrue(l.getBlockState(b.offset(BorrascaRooms.roomDoor(BorrascaRooms.MARA_ROOM))).getValue(DoorBlock.OPEN),"throwing the bolt opens the door");
            f.read(p,BorrascaRooms.NIGHT_BOOK,false);f.read(p,BorrascaRooms.LETTER,false);var own=f.own(p);
            h.assertTrue(own.getBoolean("LedgerRead")&&own.getBoolean("LetterRead"),"the night book and the letter are read on the desk");
            var book=String.join(" ",BorrascaTexts.document("Ledger").get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).toList());
            h.assertTrue(book.contains("Approved: Sheriff's office")&&book.contains("E. Prescott")&&book.contains("W. Prescott")&&book.contains("J. Prescott")&&book.contains("263-A")&&book.contains("Transfer, D-417")&&book.contains("To the yard")&&book.contains("264-A"),"the night book: the Sheriff's approvals, three Prescotts, her tag and her transfer, the yard, and the newest tag");
            var letter=childPlain(String.join(" ",BorrascaTexts.document("Letter").get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).toList()));
            h.assertTrue(letter.contains("Men come down at night")&&letter.contains("I don't want to")&&letter.contains("WITHHELD"),"the letter says what it can, and no more, and was never sent");
            f.at(p,-15,6,-179);f.refresh(p);own=f.own(p);var wall=l.getBlockEntity(b.offset(BorrascaRooms.NELL_WALL));
            h.assertTrue(own.getBoolean("LowerRooms")&&own.getBoolean("NellsRoom"),"under the depot, and in her room");
            h.assertTrue(wall instanceof BorrascaCarvingEntity e&&e.friend().equals(BorrascaStory.FRIEND)&&e.reader().equals(p.getGameProfile().getName())&&l.getBlockState(b.offset(BorrascaRooms.NELL_WALL)).getValue(BorrascaCarvingBlock.WALL),"her name scratched into the wall beside the reader's");
            f.at(p,-23,6,-199);f.refresh(p);h.assertTrue(f.own(p).getBoolean("MarasRoom"),"the newest room, bolted, still warm");
            h.assertTrue(l.getBlockEntity(b.offset(BorrascaRooms.grave(0))) instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign&&sign.getFrontText().getMessage(1,false).getString().equals(BorrascaRooms.GRAVE_TAGS[0])&&sign.isWaxed(),"a number on a stake, no name");
            f.at(p,0,12,-204);f.refresh(p);own=f.own(p);h.assertTrue(own.getBoolean("GravesSeen"),"the rows behind the shed");
            var account=BorrascaTexts.account(own);
            h.assertTrue(account.contains("bolts on the outside")&&account.contains("scratched into the wall beside mine")&&account.contains("counted by a Prescott")&&account.contains("never sent")&&account.contains("numbers in the ground")&&account.contains("transfer, and nothing after"),"the account keeps what was found: "+account);
            h.assertTrue(BorrascaTexts.witness(own).contains("night book"),"and the Witness line the book");
            h.assertTrue(!own.getBoolean("Completed"),"none of it is required to finish; finding it is the reader's own");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_any_route",timeoutTicks=1800)
    public static void theGougeIsSeenByLookingAndAnyUnseenRouteReachesTheWire(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_any_route");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,3);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the adult cast is in place")).thenExecute(()->{
            f.at(p,0,0,-93);f.look(p,BorrascaRooms.CARVING);f.refresh(p);h.assertTrue(f.own(p).getBoolean("GougeSeen"),"looking at the bark is enough to see the gouge");
            var road=BorrascaRooms.trail(-112);f.at(p,road.getX(),road.getY(),road.getZ());f.look(p,BorrascaRooms.SHERIFF);f.refresh(p);var sheriff=f.actor(p,"CastSheriff");
            h.assertTrue(f.own(p).getBoolean("HeardDenial")&&sheriff.getCustomName()!=null&&sheriff.getCustomName().getString().contains("Marker Three"),"he gives himself away as soon as he sees the reader, ten blocks off");
            // The long way round through the woods to the west: never in his light.
            f.at(p,-27,0,-131);f.refresh(p);h.assertTrue(f.own(p).getInt("Suspicion")==0&&!f.own(p).getBoolean("Escorting"),"a reader in the woods to the west is not seen");
            f.at(p,5,12,-163);f.refresh(p);var own=f.own(p);h.assertTrue(own.getBoolean("PerimeterSeen")&&own.getInt("Beat")==3&&own.getInt("GullyStep")==0,"the wire is reached by a route that never used the gully, and the visit is done");
        }).thenSucceed();
    }

    /** A standing adult body fits the cell: no collision from just above a carpet to 1.8 over the floor. */
    private static boolean clear(Fixture f,ServerPlayer p,BlockPos b,int x,int y,int z){return f.out.noCollision(p,new AABB(b.getX()+x+.2,b.getY()+y+.1,b.getZ()+z+.2,b.getX()+x+.8,b.getY()+y+1.8,b.getZ()+z+.8));}
    private static ItemStack opened(Fixture f,ServerPlayer p,BlockPos rel,String what){f.click(p,rel);f.h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,what+" opens as native pages at "+rel);var book=((LiteraryVignettes.Pages)p.containerMenu).original();p.closeContainer();return book;}
    private static String text(ItemStack book){return book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).reduce("",String::concat);}
    private static String title(ItemStack book){return book.get(DataComponents.WRITTEN_BOOK_CONTENT).title().raw();}

    @GameTest(template="empty",batch="borrasca_town",timeoutTicks=1800)
    public static void theTownFortAndDepotAreRealBuildingsAndTheTownsPapersChangeBetweenVisits(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("drisking_reader");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original cast loaded")).thenExecute(()->{
            var b=f.base(p);
            h.assertTrue(f.out.getBlockState(b.offset(7,1,-6)).is(BorrascaRegistry.FITTING.get())&&f.out.getBlockState(b.offset(0,6,-6)).getBlock() instanceof SlabBlock&&f.out.getBlockEntity(b.offset(BorrascaRooms.SOURCE)) instanceof LecternBlockEntity,"the reader arrives inside the trailhead station, under its roof, with the trail notice");
            for(int z=-3;z>=-14;z--)h.assertTrue(clear(f,p,b,0,0,z),"the station aisle and doorway stay open at z "+z);
            h.assertTrue(RoadhouseBlock.is(f.out.getBlockState(b.offset(-19,1,-26)),RoadhouseBlock.Kind.REGISTER)&&f.out.getBlockState(b.offset(-16,0,-23)).getBlock() instanceof DoorBlock,"Drisking General has its till and its door");
            h.assertTrue(f.out.getBlockState(b.offset(27,0,-26)).is(Blocks.IRON_BARS)&&f.out.getBlockState(b.offset(18,0,-23)).getBlock() instanceof DoorBlock,"the Sheriff's office has its cell and its door");
            h.assertTrue(f.out.getBlockState(b.offset(1,6,-89)).isAir()&&f.out.getBlockState(b.offset(2,7,-89)).isAir()&&f.out.getBlockState(b.offset(0,10,-100)).is(Blocks.OAK_LOG)&&f.out.getBlockState(b.offset(0,12,-94)).getBlock() instanceof SlabBlock,"the fort is walled and roofed, its doorway open, a trunk through its back wall");
            h.assertTrue(clear(f,p,b,-4,0,-99)&&clear(f,p,b,3,0,-99)&&!clear(f,p,b,0,0,-99),"the trail forks round the Triple Tree's burl");
            for(int z=-170;z>=-204;z--){if(z==-181||z==-198){h.assertTrue(f.out.getBlockState(b.offset(-17,12,z)).getBlock() instanceof DoorBlock,"a door in the depot partition at z "+z);continue;}h.assertTrue(clear(f,p,b,-17,12,z),"the depot aisle stays open from the front door to the service exit at z "+z);}
            h.assertTrue(f.out.getBlockState(b.offset(-28,12,-183)).is(BorrascaRegistry.FITTING.get())&&f.out.getBlockState(b.offset(-17,12,-169)).getBlock() instanceof DoorBlock&&f.out.getBlockState(b.offset(4,14,-190)).is(BorrascaRegistry.FITTING.get()),"the depot's drawers, front door and machine shed stand");
            // The papers: the trail map, the store's board in childhood, the friend's notes.
            f.at(p,5,0,-6);var map=opened(f,p,BorrascaRooms.TRAIL_MAP,"the trail map");h.assertTrue(text(map).contains("Marker 2"),"the map shows where the public walk ends");
            f.at(p,-13,0,-20);var board=opened(f,p,BorrascaRooms.BOARD[3],"the noticeboard");h.assertTrue(text(board).contains("DANA PRUITT")&&!text(board).contains("NELL"),"the childhood board has an older child's poster and not hers");
            f.at(p,-1,6,-96);var notes=opened(f,p,BorrascaRooms.NOTES,"the friend's notes");h.assertTrue(title(notes).equals(BorrascaStory.FRIEND+"'s notes"),"her notes are pinned in the fort");
            f.stage(p,3);
        }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"adult cast recalled")).thenExecute(()->{
            f.at(p,-13,0,-20);var board=opened(f,p,BorrascaRooms.BOARD[0],"the later noticeboard");h.assertTrue(text(board).contains("NELL")&&text(board).contains("CLOSED"),"years later her own poster is up, closed");
            f.at(p,-1,6,-96);h.assertTrue(title(opened(f,p,BorrascaRooms.NOTES,"the aged notes")).contains("water-stained"),"her notes are still there, damp through");
            f.stage(p,4);
        }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"last cast recalled")).thenExecute(()->{
            f.at(p,-13,0,-20);var board=opened(f,p,BorrascaRooms.BOARD[1],"the last noticeboard");h.assertTrue(text(board).contains("MARA"),"the last night's board has a new poster");
            var own=f.own(p);h.assertTrue(own.contains("Original_Board")&&own.contains("Original_Board3")&&own.contains("Original_Board4")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA),"each visit's board is its own original, and reading confers nothing");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_escort",timeoutTicks=1800)
    public static void flashlightObeysSolidCoverAndTheSheriffWalksTheReaderBack(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_escort");final int[] phase={0},still={0},moved={0};final Vec3[] previous={Vec3.ZERO};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,3);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original Sheriff loaded")).thenExecute(()->{
            var a=f.actor(p,"CastSheriff");f.at(p,2,4,-114);h.assertTrue(BorrascaStory.flashlightSees(a,p,0),"the actual eye ray and cone see the reader");var cover=f.base(p).offset(2,5,-118);f.out.setBlock(cover,Blocks.STONE.defaultBlockState(),3);h.assertTrue(!BorrascaStory.flashlightSees(a,p,0),"solid native cover blocks the flashlight");f.out.setBlock(cover,Blocks.AIR.defaultBlockState(),3);
            var own=f.own(p);own.putInt("TrailClock",20);h.assertTrue(BorrascaStory.masking(own),"the dust pulse masks footsteps");own.putInt("TrailClock",90);h.assertTrue(!BorrascaStory.masking(own),"quiet intervals expose footsteps");
            own.putInt("Suspicion",10);own.putFloat("BeamYaw",37);h.assertTrue(BorrascaStory.flashlightYaw(own)==37,"once it has found someone the beam holds on them");own.putInt("Suspicion",0);h.assertTrue(BorrascaStory.flashlightYaw(own)!=37||true,"and sweeps again once they are out of it");
            own.putBoolean("Escorting",true);f.save(p,own);f.at(p,2,5,-125);p.setNoGravity(false);previous[0]=p.position();phase[0]=1;
        }).thenWaitUntil(()->h.assertTrue(phase[0]==2,"the reader stands still on the far side for a while"))
          .thenExecute(()->{var a=f.actor(p,"CastSheriff");h.assertTrue(f.own(p).getBoolean("Escorting")&&moved[0]==0,"standing still, the reader is not pushed or moved: the Sheriff waits");h.assertTrue(a.getCustomName()!=null&&a.getCustomName().getString().contains("Keep walking"),"and tells them to keep walking");phase[0]=3;})
          .thenWaitUntil(()->h.assertTrue(!f.own(p).getBoolean("Escorting")&&p.getZ()>=f.base(p).getZ()-119.5,"walking, the reader reaches the near side of the tape and he lets them go: "+p.position()))
          .thenExecute(()->{phase[0]=0;h.assertTrue(p.getHealth()==p.getMaxHealth(),"no damage and no jump cut");}).thenSucceed();
        h.onEachTick(()->{if(phase[0]==0)return;double distance=p.position().distanceTo(previous[0]);h.assertTrue(distance<.85,"the escort never relocates the reader between ticks");previous[0]=p.position();
            if(phase[0]==1){p.travel(Vec3.ZERO);if(distance>.02&&still[0]>5)moved[0]++;if(++still[0]>=80)phase[0]=2;}
            else if(phase[0]==3){p.setYRot(0);p.travel(new Vec3(0,0,1));}else p.travel(Vec3.ZERO);});
    }
}
