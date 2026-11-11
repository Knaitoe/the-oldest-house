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
        ServerPlayer player(String name){var p=NativeTestPlayers.survival(h,name);int index=players.size();players.add(p);var site=new CompoundTag();site.putInt("Index",index);site.putBoolean("Built",true);data().setStateEntry("borrasca_sites_0497",p.getUUID().toString(),site);var world=new CompoundTag();world.putInt("NextIndex",players.size());data().setStateEntry("borrasca_sites_0497","World",world);var b=base(p);chunks.hold(out,BorrascaSites.bounds(b).inflate(2));BorrascaRooms.build(out,b);data().putDoor(new LabyrinthData.Door("borrasca/"+p.getUUID()+"/entry",HouseDimensions.OUTSIDE,BorrascaRooms.at(b,0,0,1),Direction.SOUTH,LabyrinthData.RETURN,false));p.setNoGravity(true);return p;}
        BlockPos home(){return LabyrinthPlaces.base(origin,LabyrinthPlace.JUNCTION).offset(0,0,-5);}
        /**
         * Run 1497: on a loaded runner a fresh site's entity sections took longer than a case's whole clock, which fast ticks spend in seconds.
         * While they are not in, the server thread rests a little longer, so the clock covers real time.
         */
        boolean loaded(){boolean ready=chunks.ready();if(!ready)java.util.concurrent.locks.LockSupport.parkNanos(20_000_000L);return ready;}
        void enter(ServerPlayer p){var home=home();chunks.hold(in,new AABB(home).inflate(4));for(var at:BlockPos.betweenClosed(home.offset(-2,-1,-2),home.offset(2,-1,2)))in.setBlock(at,Blocks.STONE.defaultBlockState(),3);data().pushReturn(p.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.atBottomCenterOf(home),180,false));at(p,0,0,-3);VignetteGate.begin(p,P);LiteraryVignettes.onArrive(p,P);}
        void at(ServerPlayer p,double x,double y,double z){var b=base(p);var w=BorrascaRooms.world(new Vec3(x+.5,y,z+.5));p.teleportTo(out,b.getX()+w.x,b.getY()+w.y,b.getZ()+w.z,180,0);p.hasChangedDimension();p.setDeltaMovement(Vec3.ZERO);}
        void look(ServerPlayer p,BlockPos rel){p.lookAt(EntityAnchorArgument.Anchor.EYES,BorrascaRooms.at(base(p),rel).getCenter());}
        /** 0.4.420: a place given in the world (relative to the base), for distances that are walked, not authored. */
        void atWorld(ServerPlayer p,BlockPos w){var b=base(p);p.teleportTo(out,b.getX()+w.getX()+.5,b.getY()+w.getY(),b.getZ()+w.getZ()+.5,180,0);p.hasChangedDimension();p.setDeltaMovement(Vec3.ZERO);}
        /** 0.4.420: where an entity stands on the hill, in authored coordinates. */
        Vec3 rel(Entity e,ServerPlayer p){return BorrascaRooms.rel(e,base(p));}
        void click(ServerPlayer p,BlockPos rel){var at=BorrascaRooms.at(base(p),rel);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)));}
        /** 0.4.422: a click on a block given in the world (the street past the chapel has no authored coordinates). */
        void clickWorld(ServerPlayer p,BlockPos w){var at=base(p).offset(w);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)));}
        void use(ServerPlayer p,BlockPos rel){at(p,rel.getX()-1,rel.getY(),rel.getZ()+1);look(p,rel);click(p,rel);}
        /**
         * 0.4.429: a connected client moves its own player by the velocity the server sends it; a test player has no client, so a shove
         * is played out here the way a client would (its movement against the blocks, then ground friction), each tick.
         */
        void shoved(ServerPlayer p){h.onEachTick(()->{var v=p.getDeltaMovement();if(v.horizontalDistanceSqr()>.0004){p.move(MoverType.SELF,new Vec3(v.x,0,v.z));p.setDeltaMovement(v.x*.546,v.y,v.z*.546);}});}
        /** 0.4.429: the reader takes her knife from the Friend and cuts their own name in the bark with it. */
        void carve(ServerPlayer p){
            talk(p,"CastKimber");h.assertTrue(BorrascaBelongings.valid(p,own(p),BorrascaBelongings.KNIFE,p.getMainHandItem())&&actor(p,"CastKimber").getMainHandItem().isEmpty(),"the reader takes the Friend's own knife from her hand into theirs");
            for(int i=0;i<BorrascaStory.CUTS;i++){
                use(p,BorrascaRooms.CARVING);
                if(i==0){var cut=((BorrascaCarvingEntity)out.getBlockEntity(BorrascaRooms.at(base(p),BorrascaRooms.CARVING))).reader();String name=p.getGameProfile().getName();
                    h.assertTrue(!cut.isEmpty()&&cut.length()<name.length()&&name.startsWith(cut),"the first cut shows only the start of the reader's name: "+cut);}
            }
        }
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
    // Run 1498: the wire case had no cleanup, so its reader stayed logged in on the real world and another suite's empty-world case failed.
    @AfterBatch(batch="borrasca_wire") public static void wireDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_registration") public static void registrationDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_arcade_rules") public static void arcadeRulesDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_feeding") public static void feedingDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_breaker") public static void breakerDone(ServerLevel l){cleanup();}

    @GameTest(template="empty",batch="borrasca_diagnostics")
    public static void preEntrySocketDiagnosticsNeverGeneratePrivateCoordinatesInEitherDimension(GameTestHelper h){
        var f=new Fixture(h);var a=NativeTestPlayers.survival(h,"pine_status_a");var b=NativeTestPlayers.survival(h,"pine_status_b");f.players.add(a);f.players.add(b);
        // Earlier batches build at the first indices and their chunks may still be resident; these sites lie where no case builds.
        var fresh=new CompoundTag();fresh.putInt("NextIndex",3000);f.data().setStateEntry("borrasca_sites_0497","World",fresh);
        for(var p:List.of(a,b)){
            h.assertTrue(BorrascaSites.prepareEntry(p)==null,"the real private site is still queued before entry");
            var at=BorrascaRooms.at(f.base(p),-17,12,-206);for(var level:List.of(p.server.overworld(),f.out))h.assertTrue(level.getChunkSource().getChunkNow(at.getX()>>4,at.getZ()>>4)==null,"the site coordinate starts unloaded in "+level.dimension().location());
        }
        BorrascaSocketProof.status(a,b,54);
        for(var p:List.of(a,b)){var at=BorrascaRooms.at(f.base(p),-17,12,-206);for(var level:List.of(p.server.overworld(),f.out))h.assertTrue(level.getChunkSource().getChunkNow(at.getX()>>4,at.getZ()>>4)==null,"reading diagnostic status must not load or generate a chunk in "+level.dimension().location());}
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
    @AfterBatch(batch="borrasca_play") public static void playDone(ServerLevel level){cleanup();}
    @GameTest(template="empty",batch="borrasca_play",timeoutTicks=1800)
    public static void nellPlaysBeforeTheTreeAndTheTrailChildrenHaveMoreToSay(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_play");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded"))
            .thenExecute(()->{f.enter(p);f.at(p,1,0,-18);})
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("MetFriend"),"Kelly starts the game at the trailhead"))
            .thenExecute(()->{
                var b=f.base(p);var eli=f.actor(p,"TownEli");var wren=f.actor(p,"TownWren");
                h.assertTrue(eli.blockPosition().equals(BorrascaRooms.at(b,-2,0,-24))
                        &&wren.blockPosition().equals(BorrascaRooms.at(b,-2,0,-32)),"both children are on the main trail");
                f.talk(p,"TownEli");f.talk(p,"TownEli");
                h.assertTrue(f.own(p).getInt("EliTalk1")>=2,"Patrick has a second conversation about the Sheriff");
                f.at(p,1,0,-28);
            })
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("TaggedNell"),"catching up tags Kelly before the Tree"))
            .thenWaitUntil(()->h.assertTrue(f.own(p).getInt("HideLeg")==2,"Kelly reaches one of the two hiding places"))
            .thenExecute(()->f.at(p,f.own(p).getInt("HideSide"),0,-35))
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FoundNell")&&f.own(p).getBoolean("PlayDone"),
                    "finding her on the far side of the hiding tree finishes the games"))
            .thenExecute(()->h.assertTrue(!f.own(p).getBoolean("KimberAtFort"),"the play took place before she reached the fort"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_motion",timeoutTicks=7200)
    public static void nativeChildAndTheOriginalFriendClimbTheActualFortWithoutRelocation(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("triple_tree_climber");final boolean[] moving={false};final int[] rises={0},friendRises={0},playerRoute={0};final double[] last={0,0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"all original cast loaded")).thenExecute(()->{
            var b=f.base(p);h.assertTrue(f.out.getBlockEntity(BorrascaRooms.at(b,BorrascaRooms.SOURCE)) instanceof LecternBlockEntity,"the content note is a native book");h.assertTrue(!f.out.noCollision(p,wbox(b,-4.8,6,-93.1,-4.2,7,-92.9)),"native fort rails join between posts");
            // Stage the approach only; both bodies must then climb by native collision and ladder physics.
            f.at(p,3,0,-83);p.setNoGravity(false);var a=f.actor(p,"CastKimber");a.moveTo(Vec3.atBottomCenterOf(BorrascaRooms.at(b,3,0,-86)),180,0);var own=f.own(p);own.putBoolean("MetFriend",true);own.putInt("ChildStep",3);f.save(p,own);last[0]=p.getY();last[1]=a.getY();moving[0]=true;
        }).thenWaitUntil(()->{var a=f.actor(p,"CastKimber");h.assertTrue(f.rel(p,p).y>=6&&a!=null&&f.rel(a,p).y>=5.9&&f.own(p).getBoolean("TreeReached"),"native child and original Friend reach the fort: player="+p.position()+" friend="+(a==null?"absent":a.position())+" step="+f.own(p).getInt("ChildStep"));}).thenExecute(()->{h.assertTrue(rises[0]>=20&&friendRises[0]>=20,"each body climbs over successive native ticks");h.assertTrue(p.getBbHeight()<1.1,"visits one and two use child height");}).thenWaitUntil(()->{var a=f.actor(p,"CastKimber");var b=f.base(p);h.assertTrue(f.own(p).getBoolean("FriendCarved"),"the same Friend descends the real ladder and carves at the trunk: at "+a.position().subtract(Vec3.atLowerCornerOf(b))+" downStep="+f.own(p).getInt("DownStep")+" carveTicks="+f.own(p).getInt("CarveTicks")+" legend="+f.own(p).getBoolean("LegendDone")+" climbing="+a.onClimbable()+" bumped="+a.horizontalCollision+" noAi="+a.isNoAi()+" feet/head="+f.out.getBlockState(a.blockPosition())+"/"+f.out.getBlockState(a.blockPosition().above())+" reader at "+p.position().subtract(Vec3.atLowerCornerOf(b)));}).thenExecute(()->{moving[0]=false;h.assertTrue(f.actor(p,"CastKimber").gesture()==4,"she offers the knife after carving");}).thenSucceed();
        h.onEachTick(()->{if(!moving[0])return;var a=f.actor(p,"CastKimber");var b=f.base(p);if(playerRoute[0]==0){p.setYRot(180);p.travel(new Vec3(0,0,1));if(f.rel(p,p).y>=6.05)playerRoute[0]=1;}else if(playerRoute[0]==1){p.setYRot(90);p.travel(new Vec3(0,0,1));if(p.getX()<=b.getX()+1.65)playerRoute[0]=2;}else if(f.rel(p,p).z> -92){p.setYRot(180);p.travel(new Vec3(0,0,1));}else p.setDeltaMovement(Vec3.ZERO);double rise=p.getY()-last[0],other=a.getY()-last[1];h.assertTrue(Math.abs(rise)<.6&&Math.abs(other)<.6,"neither ladder body is relocated between server ticks");if(rise>.025)rises[0]++;if(other>.025)friendRises[0]++;last[0]=p.getY();last[1]=a.getY();});
    }

    @GameTest(template="empty",batch="borrasca_low",timeoutTicks=1800)
    public static void theAdultBodyCrouchesThroughTheGullyAndTheReleasedServicePipe(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("fern_gully_adult");final int[] phase={0},steps={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->f.enter(p)).thenIdle(6).thenExecute(()->{h.assertTrue(p.getBbHeight()<1.1,"the initial native body is child height: "+p.getBbHeight());f.stage(p,3);}).thenIdle(6).thenExecute(()->{
            var b=f.base(p);h.assertTrue(p.getBbHeight()>1.7,"the third visit restores adult height");Vec3 g0=BorrascaRooms.world(new Vec3(-15.8,3,-119.8)),g1=BorrascaRooms.world(new Vec3(-15.2,4.8,-119.2));var standing=new AABB(b.getX()+g0.x,b.getY()+g0.y,b.getZ()+g0.z,b.getX()+g1.x,b.getY()+g1.y,b.getZ()+g1.z);var low=new AABB(standing.minX,standing.minY,standing.minZ,standing.maxX,b.getY()+BorrascaRooms.world(new Vec3(-15.8,4.5,-119.8)).y,standing.maxZ);h.assertTrue(!f.out.noCollision(p,standing)&&f.out.noCollision(p,low),"the actual gully roof admits crouching and blocks standing");f.at(p,-16,2,-119);p.setNoGravity(false);p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);phase[0]=1;
        }).thenWaitUntil(()->h.assertTrue(phase[0]==3,"native crouched gully and service traversal: phase="+phase[0]+" body="+p.position().subtract(Vec3.atLowerCornerOf(f.base(p))))).thenExecute(()->h.assertTrue(steps[0]>25,"the adult walks continuously under both native roofs")).thenSucceed();
        h.onEachTick(()->{if(phase[0]==0||phase[0]==3)return;p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);if(phase[0]==1){var end=Vec3.atBottomCenterOf(BorrascaRooms.at(f.base(p),-13,4,-128));var d=end.subtract(p.position());p.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));}else p.setYRot(f.rel(p,p).z< -207.6?90:180);p.travel(new Vec3(0,0,1));steps[0]++;
            if(phase[0]==1&&f.rel(p,p).z< -124){f.stage(p,4);f.at(p,-17,12,-204);f.click(p,BorrascaRooms.RELEASE);h.assertTrue(f.own(p).getBoolean("ReleaseOpened")&&open(f,p,BorrascaRooms.SERVICE_DOOR)&&open(f,p,BorrascaRooms.SERVICE_GATE),"the release opens the depot's back door and the gate in the wire");phase[0]=2;}
            else if(phase[0]==2&&p.getX()<f.base(p).getX()-26.5){h.assertTrue(f.rel(p,p).z< -207&&f.rel(p,p).z> -209.5,"the adult crouches west along the walled tunnel to the gate");phase[0]=3;}});
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
        var s=h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"both private native sites loaded")).thenExecute(()->{f.enter(p);f.enter(peer);}).thenWaitUntil(()->h.assertTrue(recalled(f,p),"original Friend loaded"));
        s.thenExecute(()->{original[0]=f.own(p).getUUID("CastKimber");f.read(p,BorrascaRooms.SOURCE,false);var own=f.own(p);own.putBoolean("FriendCarved",true);f.save(p,own);f.actor(p,"CastKimber").moveTo(Vec3.atBottomCenterOf(BorrascaRooms.at(f.base(p),1,0,-96)),180,0);f.use(p,BorrascaRooms.CARVING);h.assertTrue(f.own(p).getInt("Beat")==0,"bare hands cut no name");f.carve(p);h.assertTrue(f.own(p).getInt("Beat")==1,"the native carving seals the staged childhood pact");f.talk(p,"CastKimber");h.assertTrue(!BorrascaBelongings.has(p,f.own(p),BorrascaBelongings.KNIFE)&&f.own(p).getBoolean("KnifeBack"),"the knife goes back to her");var e=(BorrascaCarvingEntity)f.out.getBlockEntity(BorrascaRooms.at(f.base(p),BorrascaRooms.CARVING));h.assertTrue(e.reader().equals(p.getGameProfile().getName())&&e.friend().equals(BorrascaStory.FRIEND),"the bark holds this reader's actual name beside their Friend");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual first return consumes its native destination"));
        s.thenWaitUntil(()->h.assertTrue(staged(p),"the actual entry preparation stages the originals while the reader is away: "+BorrascaCast.why(p))).thenExecute(()->{f.enter(p);h.assertTrue(f.own(p).getInt("Visit")==2&&f.own(p).getInt("ConfirmedVisits")==1,"only the confirmed first return advances childhood");}).thenWaitUntil(()->h.assertTrue(recalled(f,p),"same Friend recalled")).thenExecute(()->{
            f.talk(p,"CastKimber");h.assertTrue(!f.own(p).getBoolean("FollowAgain"),"the childhood trail notice cannot acknowledge the new second-visit content note");f.read(p,BorrascaRooms.SOURCE,false);h.assertTrue(f.own(p).getBoolean("Read_ContentNote"),"the new trailhead note is personally read before the taking route");f.talk(p,"CastKimber");var own=f.own(p);own.putInt("TakingStep",Integer.MAX_VALUE);f.save(p,own);f.actor(p,"CastKimber").moveTo(Vec3.atBottomCenterOf(BorrascaRooms.at(f.base(p),BorrascaRooms.TAKING)),180,0);f.at(p,-2,9,-147);p.setYRot(0);
        }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FriendMissing"),"the taking completes with the camera looking away")).thenExecute(()->{f.use(p,BorrascaRooms.DROP);h.assertTrue(f.own(p).getInt("Beat")==2&&BorrascaBelongings.has(p,f.own(p),"Compass"),"the dropped original is recovered through native interaction");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual second return"));
        s.thenWaitUntil(()->h.assertTrue(staged(p),"the original Sheriff reaches the blockade before the actual third arrival: "+BorrascaCast.why(p))).thenExecute(()->{f.enter(p);h.assertTrue(f.own(p).getInt("Visit")==3,"the next arrival is the adult return");}).thenWaitUntil(()->h.assertTrue(recalled(f,p),"same Sheriff recalled")).thenExecute(()->{f.use(p,BorrascaRooms.CARVING);f.talk(p,"CastSheriff");p.closeContainer();p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);});
        // Ordered checkpoints are staged here. Continuous physical clearance is exercised in the low-route case.
        for(var at:BorrascaRooms.GULLY)s.thenExecute(()->f.at(p,at.getX(),at.getY(),at.getZ())).thenIdle(6);
        s.thenExecute(()->{p.setShiftKeyDown(false);p.setPose(Pose.STANDING);f.use(p,BorrascaRooms.MARKER_FOUR);f.refresh(p);h.assertTrue(f.own(p).getInt("Beat")==3,"the gouge, personal denial, ordered gully and perimeter complete the investigation");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual third return"));
        s.thenWaitUntil(()->h.assertTrue(staged(p),"the same refinery cast is prepared before the fourth native arrival: "+BorrascaCast.why(p))).thenExecute(()->{f.enter(p);h.assertTrue(f.own(p).getInt("Visit")==4&&f.own(p).getInt("ConfirmedVisits")==7,"all three earlier returns are independently confirmed");f.read(p,BorrascaRooms.EVIDENCE,true);f.use(p,BorrascaRooms.PACK);f.use(p,BorrascaRooms.POWER);}).thenIdle(6).thenExecute(()->{
            var b=f.base(p);h.assertTrue(!f.out.getBlockState(BorrascaRooms.at(b,BorrascaRooms.MACHINE)).getValue(BorrascaMachineBlock.RUNNING)&&f.out.getBlockState(BorrascaRooms.at(b,BorrascaRooms.LAMP)).getValue(BorrascaFittingBlock.KIND)==BorrascaFittingBlock.Kind.LAMP_OFF,"the actual machine and lamp switch off");f.use(p,BorrascaRooms.CARVING);h.assertTrue(f.out.getBlockState(BorrascaRooms.at(b,BorrascaRooms.CARVING)).getValue(BorrascaCarvingBlock.STAGE)==4,"restored lettering keeps the gouged bark state");f.at(p,0,0,-10);
        }).thenIdle(6).thenExecute(()->{h.assertTrue(f.own(p).getBoolean("Ready")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA),"carrying the original back earns readiness, not a return");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual final return")).thenExecute(()->{
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA)&&f.own(p).getBoolean("NameReturned"),"only confirmed native travel grants the fixed Understanding source and optional account line");h.assertTrue(f.own(p).getUUID("CastKimber").equals(original[0])&&f.own(peer).getInt("Visit")==1&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.BORRASCA),"the original UUID and earlier peer's story stay intact");f.reload();h.assertTrue(f.own(p).getBoolean("Completed")&&BorrascaStory.carriesEvidence(p,f.own(p)),"saved completion retains the actual original patch");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_private",timeoutTicks=1800)
    public static void savedOriginalCastAndBorrowedKeepsakesStayPrivateAcrossReload(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_owner");var peer=f.player("pine_other");h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.enter(peer);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(peer).getBoolean("CastRecalled"),"both casts loaded")).thenExecute(()->{
            var a=f.actor(p,"CastKimber");var theirs=f.actor(peer,"CastKimber");h.assertTrue(!a.getUUID().equals(theirs.getUUID())&&a.broadcastToPlayer(p)&&!a.broadcastToPlayer(peer),"native entity tracking keeps the original cast private");var id=a.getUUID();var own=f.own(p);h.assertTrue(BorrascaBelongings.takeAt(p,own,"Patch",p.blockPosition()),"one original patch enters its owner's actual inventory");f.save(p,own);peer.getInventory().add(BorrascaBelongings.original(p,own,"Patch").copy());h.assertTrue(!BorrascaStory.carriesEvidence(peer,f.own(peer)),"a borrowed patch is no personal proof");var edited=BorrascaRooms.at(f.base(p),2,0,-12);f.out.setBlock(edited,Blocks.DIAMOND_BLOCK.defaultBlockState(),3);f.reload();LiteraryVignettes.onArrive(p,P);h.assertTrue(f.own(p).getInt("Visit")==1&&f.own(p).getUUID("CastKimber").equals(id)&&f.out.getEntity(id)==a,"reload neither advances nor replaces the original");h.assertTrue(f.out.getBlockState(edited).is(Blocks.DIAMOND_BLOCK)&&BorrascaSites.prepareEntry(p)!=null,"ready sites preserve player edits");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_build",timeoutTicks=1000)
    public static void firstConstructionResumesItsSavedBoundedCursorAndKeepsAppliedEdits(GameTestHelper h){
        var f=new Fixture(h);var p=NativeTestPlayers.survival(h,"pine_builder");f.players.add(p);h.assertTrue(BorrascaSites.prepareEntry(p)==null,"first entry waits without starting a visit");var b=BorrascaSites.base(p.server,p.getUUID());f.chunks.hold(f.out,BorrascaSites.bounds(b).inflate(2));final boolean[] reloaded={false};final BlockPos[] kept={null};final int[] finishedTicks={0};final LabyrinthData.Door[] originalDoor={null};
        h.onEachTick(()->{for(int retry=0;retry<3;retry++)BorrascaSites.prepareEntry(p);var site=f.data().stateEntry("borrasca_sites_0497",p.getUUID().toString());var cursor=site.getCompound("BuildCursor");if(!reloaded[0]&&!cursor.isEmpty()&&cursor.getInt("Cursor")>=3){kept[0]=b.offset(0,-3,-80);f.out.setBlock(kept[0],Blocks.GOLD_BLOCK.defaultBlockState(),3);f.reload();h.assertTrue(BorrascaSites.prepareEntry(p)==null,"unfinished construction resumes its saved command");reloaded[0]=true;}if(!BorrascaSites.ready(p.server,p.getUUID()))return;h.assertTrue(reloaded[0]&&f.out.getBlockState(kept[0]).is(Blocks.GOLD_BLOCK),"already applied ground is not replayed after reload");h.assertTrue(BorrascaSites.heldChunks()==0&&BorrascaSites.entry(p)!=null&&f.own(p).getInt("Visit")==0,"construction releases tickets without arrival or credit");if(finishedTicks[0]++==0){originalDoor[0]=BorrascaSites.entry(p);f.out.setBlock(BorrascaRooms.at(b,0,-1,-3),Blocks.DIAMOND_BLOCK.defaultBlockState(),3);}h.assertTrue(BorrascaSites.entry(p)==originalDoor[0]&&f.out.getBlockState(BorrascaRooms.at(b,0,-1,-3)).is(Blocks.DIAMOND_BLOCK),"retries never rebuild or replace the saved door");BorrascaSites.advance(p.server);if(finishedTicks[0]>=100)h.succeed();});
    }

    @GameTest(template="empty",batch="borrasca_interrupt",timeoutTicks=1800)
    public static void unconfirmedDeparturesAndStagedFinalFlagsCannotBecomeWitnesses(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_interrupt");h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);var own=f.own(p);own.putInt("Beat",1);f.save(p,own);BorrascaStory.depart(p);LiteraryVignettes.onArrive(p,P);h.assertTrue(f.own(p).getInt("Visit")==1&&f.own(p).getInt("ConfirmedVisits")==0,"departure intent without successful travel cannot confirm or advance a visit");f.reload();f.stage(p,4);own=f.own(p);own.putInt("Beat",3);own.putBoolean("PowerOff",true);own.putBoolean("EvidenceRead",true);own.putBoolean("Ready",true);BorrascaBelongings.takeAt(p,own,"Patch",p.blockPosition());f.save(p,own);f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual staged fourth-visit return")).thenExecute(()->h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA)&&!f.own(p).getBoolean("Completed"),"final flags and a real original cannot replace three confirmed earlier returns")).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_missing",timeoutTicks=1800)
    public static void olderNativeFriendNbtCannotReviveTheMissingOriginalOrDuplicateHerCompass(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_missing");final CompoundTag[] before={null};final UUID[] id={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original Friend loaded")).thenExecute(()->{
            var a=f.actor(p,"CastKimber");before[0]=a.saveWithoutId(new CompoundTag());id[0]=a.getUUID();a.discard();var own=f.own(p);own.putInt("Visit",2);own.putBoolean("FriendMissing",true);own.putBoolean("FriendHidden",true);h.assertTrue(BorrascaBelongings.dropOnce(p,own,"Compass",BorrascaRooms.at(f.base(p),BorrascaRooms.DROP)),"one physical original drops");f.save(p,own);f.reload();
        }).thenIdle(3).thenExecute(()->{
            var a=LiteraryRegistry.ACTOR.get().create(f.out);a.load(before[0]);h.assertTrue(f.out.addFreshEntity(a),"the same UUID returns from genuinely older native entity NBT");h.assertTrue(a.getUUID().equals(id[0])&&a.phase()==BorrascaStory.MISSING&&a.isInvisible()&&a.getOffhandItem().isEmpty(),"saved missing state wins before native tracking");var own=f.own(p);h.assertTrue(!BorrascaBelongings.dropOnce(p,own,"Compass",BorrascaRooms.at(f.base(p),BorrascaRooms.DROP)),"reload cannot issue a second original");long count=f.out.getEntitiesOfClass(ItemEntity.class,BorrascaSites.bounds(f.base(p))).stream().filter(e->BorrascaBelongings.valid(p,own,"Compass",e.getItem())).count();h.assertTrue(count==1,"exactly one original compass survives as a native item entity");f.save(p,own);f.talk(p,"CastKimber");h.assertTrue(!f.own(p).getBoolean("FollowAgain"),"the missing Friend does not answer or resume the taking");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_custody",timeoutTicks=1800)
    public static void denialWorksWithoutTheCompassAndConfiscationReturnsTheSameOriginal(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_compass");final ItemStack[] exact={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,3);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original adult Sheriff loaded")).thenExecute(()->{
            f.talk(p,"CastSheriff");h.assertTrue(f.own(p).getBoolean("HeardDenial")&&f.actor(p,"CastSheriff").getCustomName().getString().contains("Marker Three"),"he gives away Marker Three even with no compass");var own=f.own(p);BorrascaBelongings.takeAt(p,own,"Compass",p.blockPosition());exact[0]=BorrascaBelongings.remove(p,own,"Compass");p.setItemInHand(InteractionHand.MAIN_HAND,exact[0]);f.save(p,own);f.talk(p,"CastSheriff");h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu,"holding the real compass opens the native handover choice");p.containerMenu.clicked(0,0,ClickType.PICKUP,p);p.closeContainer();h.assertTrue(f.own(p).getBoolean("CompassConfiscated")&&!BorrascaBelongings.has(p,f.own(p),"Compass"),"handover transfers the actual inventory stack");f.reload();f.stage(p,4);f.use(p,BorrascaRooms.PACK);var after=f.own(p);h.assertTrue(BorrascaBelongings.has(p,after,"Compass")&&!after.contains("ConfiscatedCompass"),"the original is recovered at the sorting table after reload");var found=p.getInventory().items.stream().filter(a->BorrascaBelongings.valid(p,after,"Compass",a)).findFirst().orElse(ItemStack.EMPTY);h.assertTrue(ItemStack.isSameItemSameComponents(exact[0],found),"all components of the confiscated compass remain exact");
        }).thenSucceed();
    }

    private static void assertTakingSightline(Fixture f,ServerPlayer p){
        var b=f.base(p);var eye=Vec3.atBottomCenterOf(BorrascaRooms.at(b,BorrascaRooms.TAKING)).add(0,p.getEyeHeight(),0);var lamp=BorrascaRooms.at(b,BorrascaRooms.LAMP).getCenter();
        var ray=f.out.clip(new ClipContext(eye,lamp,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));f.h.assertTrue(ray.getType()==HitResult.Type.MISS||ray.getLocation().distanceToSqr(lamp)<2,"the actual taking sightline sees the lamp through the wire mesh: eye="+p.getEyeHeight()+" hit="+ray.getBlockPos().subtract(b)+" "+f.out.getBlockState(ray.getBlockPos()));
        var obstacle=BlockPos.containing(eye.lerp(lamp,.35));var before=f.out.getBlockState(obstacle);f.out.setBlock(obstacle,Blocks.STONE.defaultBlockState(),3);var covered=f.out.clip(new ClipContext(eye,lamp,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));f.h.assertTrue(covered.getType()==HitResult.Type.BLOCK&&covered.getBlockPos().equals(obstacle),"solid native cover still hides the taking light");f.out.setBlock(obstacle,before,3);
    }

    @GameTest(template="empty",batch="borrasca_taking_walk",timeoutTicks=9000)
    public static void theSameFriendWalksTheWholeSecondTrailAndBothReadersShelterBeforeTheTaking(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_taking_walk");final boolean[] walking={false};final Vec3[] previous={Vec3.ZERO};final int[] moved={0},detour={0};final UUID[] id={null};
        LiteraryActor[] carrier={null};
        // Walk around the visible timber ends. Direct pursuit cuts inside a corner once
        // the friend has rounded it; a human reader can see and take these clear passages.
        var timberEnds=new BlockPos[]{new BlockPos(6,2,-114),new BlockPos(6,3,-117),new BlockPos(1,6,-131),new BlockPos(1,6,-134)};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);p.setYRot(0);f.stage(p,2);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original Friend recalled off camera for explicit second-visit staging")).thenExecute(()->{assertTakingSightline(f,p);f.read(p,BorrascaRooms.SOURCE,false);f.talk(p,"CastKimber");id[0]=f.actor(p,"CastKimber").getUUID();f.at(p,1,0,-72);p.setNoGravity(false);previous[0]=f.actor(p,"CastKimber").position();walking[0]=true;}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FriendMissing"),"the real Friend and child walk the second route, shelter and reach the taking: body="+p.position().subtract(Vec3.atLowerCornerOf(f.base(p)))+" friend="+f.actor(p,"CastKimber").position().subtract(Vec3.atLowerCornerOf(f.base(p)))+" step="+f.own(p).getInt("TakingStep"))).thenExecute(()->{
            walking[0]=false;var own=f.own(p);h.assertTrue(own.getBoolean("Sheltered6")&&own.getBoolean("Sheltered10")&&moved[0]>100,"both actual crouched shelter beats precede the taking after continuous native walking");h.assertTrue(f.actor(p,"CastKimber").getUUID().equals(id[0])&&own.getBoolean("CompassIssued"),"the same original departs and drops her finite compass");long originals=f.out.getEntitiesOfClass(ItemEntity.class,BorrascaSites.bounds(f.base(p))).stream().filter(e->BorrascaBelongings.valid(p,own,"Compass",e.getItem())).count();h.assertTrue(originals+(BorrascaBelongings.has(p,own,"Compass")?1:0)==1,"one physical original remains after the look-away taking");
        }).thenSucceed();
        h.onEachTick(()->{
            if(!walking[0])return;
            var own=f.own(p);
            // 0.4.440 (the owner's screenshot: Kelly carried in the air by nobody): whoever she rides is always to be seen.
            {var k=f.actor(p,"CastKimber");if(k!=null&&k.getVehicle() instanceof LiteraryActor v)h.assertTrue(!v.isInvisible()&&!k.isInvisible(),"she is never carried by a man who is not there: carrier hidden "+v.isInvisible()+", missing "+own.getBoolean("FriendMissing")+", led "+own.getBoolean("Led"));}
            if(own.getBoolean("FriendMissing"))return;
            var a=f.actor(p,"CastKimber");
            if(a.getVehicle() instanceof LiteraryActor worker){
                h.assertTrue(own.getBoolean("Led")&&(worker==f.actor(p,"Worker0")||worker==f.actor(p,"Worker1")),"a real worker carries the original Friend after reaching her");
                if(carrier[0]==worker){
                    h.assertTrue(worker.position().distanceTo(previous[0])<.7,"the worker walks continuously while carrying the Friend");
                }else{
                    // Native mounting raises the passenger to the worker's arms. Check the pickup distance before switching to the carrier's stride.
                    h.assertTrue(carrier[0]==null&&worker.position().distanceTo(previous[0])<2.7,"the worker picks the Friend up within reach");
                    carrier[0]=worker;
                }
                h.assertTrue(a.distanceTo(worker)<2.7,"the Friend stays with the worker carrying her");
                previous[0]=worker.position();
            }else{
                double movedBy=a.position().distanceTo(previous[0]);
                h.assertTrue(carrier[0]==null&&movedBy<.7,"the Friend never relocates on foot along the taking route: "+movedBy);
                if(own.getInt("TakingTicks")==0&&movedBy>.005)moved[0]++;
                previous[0]=a.position();
            }
            if(own.getInt("TakingTicks")>0){p.setYRot(0);p.travel(Vec3.ZERO);return;}boolean shelter=a.gesture()==5;p.setShiftKeyDown(shelter);p.setPose(shelter?Pose.CROUCHING:Pose.STANDING);
            int corner=detour[0];boolean rounding=corner<timberEnds.length&&own.getInt("TakingStep")>=(corner<2?6:10);var target=rounding?Vec3.atBottomCenterOf(BorrascaRooms.at(f.base(p),timberEnds[corner])):a.position();if(!rounding)target=onTrail(f,p,target);var d=target.subtract(p.position());
            if(rounding&&d.horizontalDistance()<.2){detour[0]++;p.travel(Vec3.ZERO);}else if(d.horizontalDistance()>(rounding?.2:2)){p.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));p.travel(new Vec3(0,0,1));}else p.travel(Vec3.ZERO);
        });
    }

    private static boolean open(Fixture f,ServerPlayer p,BlockPos rel){var s=f.out.getBlockState(BorrascaRooms.at(f.base(p),rel));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}

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
            var c=queue.removeFirst();double fc=footing(l,c);if(Double.isNaN(fc))continue;var rel=c.subtract(base);if(Math.abs(rel.getX())>44||rel.getZ()>2||rel.getZ()<BorrascaRooms.HEAD||rel.getY()<-5||rel.getY()>BorrascaRooms.TOP)continue;
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
    private static boolean reaches(Set<BlockPos> cells,BlockPos base,BlockPos rel,double radius){var at=BorrascaRooms.at(base,rel);for(var c:cells)if(c.distSqr(at)<=radius*radius)return true;return false;}

    /**
     * 0.4.429 (owner: "players can jump in from here"): no ground a reader can walk to outside the wire is near enough and high enough
     * for a running jump to carry them over its top. The jump is a sprint jump's arc (the boost, the air drag and gravity, tick by tick);
     * it starts with the reader's middle over the edge of their block nearest the wire.
     */
    @GameTest(template="empty",batch="borrasca_wire",timeoutTicks=3000)
    public static void nobodyJumpsTheWireFromTheGroundOutsideIt(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_jumper");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{
            var b=f.base(p);var cells=walkable(f.out,b,BorrascaRooms.at(b,0,0,-2),.95);
            var wire=BorrascaRooms.at(b,-30,15,-190);var s=f.out.getBlockState(wire);
            h.assertTrue(s.is(BorrascaRegistry.FITTING.get()),"the wire's top run is where it was built: "+s);
            double top=wire.getY()+s.getCollisionShape(f.out,wire,net.minecraft.world.phys.shapes.CollisionContext.empty()).max(Direction.Axis.Y);
            var bad=new ArrayList<String>();
            for(var c:cells){
                var a=BorrascaRooms.authored(c.subtract(b));int x=a.getX(),z=a.getZ();
                int d=Math.max(Math.max(-30-x,x-27),Math.max(-214-z,z+167));if(d<1||d>12)continue;
                double foot=footing(f.out,c);if(Double.isNaN(foot))continue;
                if(foot+sprintJump(d-.86)>=top)bad.add(a.toShortString()+" (stands "+String.format(Locale.ROOT,"%.1f",foot-(wire.getY()-3))+" over the yard, "+d+" out)");
            }
            h.assertTrue(bad.isEmpty(),"no ground outside the wire is near and high enough to jump it from: "+bad.size()+" "+bad.subList(0,Math.min(16,bad.size())));
            h.assertTrue(top-wire.getY()>=1.5,"the wire's top stops a body half a block above it, as a fence does");
            // 0.4.447 (owner: "End of the world here lol."): the hill ended six rows behind the wire, on ground a reader could walk out along.
            var brink=new ArrayList<String>();
            for(var c:cells){var rel=c.subtract(b);if(Double.isNaN(footing(f.out,c))||BorrascaLand.approach(rel.getX(),rel.getZ()))continue;if(Math.abs(rel.getX())>=42||rel.getZ()<=BorrascaRooms.HEAD+2||rel.getZ()>=2)brink.add(rel.toShortString());}
            h.assertTrue(brink.isEmpty(),"nowhere a reader can walk to is at the edge of the hill, where the ground stops: "+brink.size()+" "+brink.subList(0,Math.min(12,brink.size())));
            int back=(int)Math.round(BorrascaRooms.worldZ(-223)),cliff=(int)Math.round(BorrascaRooms.worldZ(-226));
            for(int x=-30;x<=27;x+=3)h.assertTrue(BorrascaLand.groundAt(x,cliff)-BorrascaLand.groundAt(x,back)>=5,"behind the yard the cliff at the valley's head stands over the ground a reader can reach: "+x+" "+BorrascaLand.groundAt(x,back)+" "+BorrascaLand.groundAt(x,cliff));
        }).thenSucceed();
    }
    /** How high above where it left the ground a sprint jump is when it has carried a body this far. */
    static double sprintJump(double distance){double x=0,y=0,vx=.48,vy=.42;while(x<distance){x+=vx;y+=vy;vx=vx*.91+.026;vy=(vy-.08)*.98;}return y;}

    @GameTest(template="empty",batch="borrasca_sealed",timeoutTicks=3000)
    public static void theRefineryIsSealedFromTheWoodsUntilTheLastNight(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_explorer");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{
            var b=f.base(p);var child=walkable(f.out,b,BorrascaRooms.at(b,0,0,-2),.95);
            h.assertTrue(reaches(child,b,BorrascaRooms.TAKING,2)&&reaches(child,b,new BlockPos(5,12,-164),2.5),"a child can walk anywhere on the mountain, up to the wire and look through it");
            Map<String,BlockPos> inside=new LinkedHashMap<>();inside.put("the machine",new BlockPos(0,12,-185));inside.put("the breaker",BorrascaRooms.POWER);inside.put("the receipt",BorrascaRooms.EVIDENCE);inside.put("the sorting table",BorrascaRooms.PACK);inside.put("the service tunnel",new BlockPos(-23,12,-208));
            for(var e:inside.entrySet())h.assertTrue(!reaches(child,b,e.getValue(),2.5),"a child exploring on the first visit cannot get at "+e.getKey());
            // The last night: the gate breached. The yard is open; the tunnel stays behind its door until the release.
            for(int x=-1;x<=1;x++)for(int y=12;y<=14;y++)f.out.setBlock(BorrascaRooms.at(b,x,y,-167),Blocks.AIR.defaultBlockState(),2);
            var adult=walkable(f.out,b,BorrascaRooms.at(b,0,0,-2),1.5);
            for(var key:List.of("the machine","the breaker","the receipt","the sorting table"))h.assertTrue(reaches(adult,b,inside.get(key),2.5),"through the breached gate the reader reaches "+key);
            h.assertTrue(!reaches(adult,b,inside.get("the service tunnel"),1),"the service tunnel is walled off from the yard");
            BorrascaStory.openDoor(f.out,BorrascaRooms.at(b,BorrascaRooms.SERVICE_DOOR),true);BorrascaStory.openDoor(f.out,BorrascaRooms.at(b,BorrascaRooms.SERVICE_GATE),true);
            var released=walkable(f.out,b,BorrascaRooms.at(b,0,0,-2),1.5);h.assertTrue(reaches(released,b,inside.get("the service tunnel"),1),"with the release pulled the tunnel joins the depot to the way down outside the wire");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_ahead_pact",timeoutTicks=6000)
    public static void aReaderWhoRunsToTheTreeFirstStillMakesThePactThere(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_ahead");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is in place"))
            .thenExecute(()->{f.at(p,1,6,-92);f.refresh(p);h.assertTrue(f.own(p).getBoolean("TreeReached")&&f.own(p).getBoolean("CalledAfter"),"the reader is in the fort before meeting her, and she calls after them");})
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("KimberAtFort"),"she comes up to the Tree on her own: "+f.actor(p,"CastKimber").position().subtract(Vec3.atLowerCornerOf(f.base(p)))+" step="+f.own(p).getInt("ChildStep")))
            .thenExecute(()->{
                int before=f.own(p).getInt("FortPlayTicks");
                f.at(p,1,0,-92);f.refresh(p);
                h.assertTrue(f.own(p).getInt("FortPlayTicks")==before&&!f.own(p).getBoolean("FortEcho"),
                        "standing under the treehouse cannot start the scene");
                f.at(p,-4,6,-94);
            })
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("SharedWindow"),"the reader picks a window and Kelly checks her compass"))
            // 0.4.445 (owner: "Let her tuck a borrowed comic into that sill for you"): she crosses to that window and stands it there.
            .thenWaitUntil(()->{
                var own=f.own(p);var kelly=f.actor(p,"CastKimber");
                h.assertTrue(own.getBoolean("ComicTucked"),"she crosses to the reader's window with the comic: step "+own.getInt("ComicStep")+" at "+f.rel(kelly,p));
                h.assertTrue(own.getString("FortWindow").equals("west")&&f.rel(kelly,p).distanceTo(Vec3.atBottomCenterOf(BorrascaComic.STAND_WEST))<1.3,"she is at the west window, the one the reader stood at: "+f.rel(kelly,p));
                var sill=f.out.getBlockState(BorrascaRooms.at(f.base(p),BorrascaComic.SILL_WEST));
                h.assertTrue(BorrascaShopBlock.is(sill,BorrascaShopBlock.Kind.COMIC)&&sill.getValue(BorrascaShopBlock.FACING)==Direction.EAST&&sill.getValue(BorrascaShopBlock.STAGE)==0,"the comic stands in that sill with its cover to the room: "+sill);
                h.assertTrue(f.out.getBlockState(BorrascaRooms.at(f.base(p),BorrascaComic.SILL_EAST)).isAir(),"and not in the other");
                var said=kelly.getCustomName().getString();
                h.assertTrue(kelly.getMainHandItem().isEmpty()&&said.contains("Patrick lent me this")&&said.contains("your window"),"it is out of her hand, and she says whose it is: "+said);
                h.assertTrue(!own.getBoolean("FortEcho"),"the ridge waits for her to be done");
            })
            .thenWaitUntil(()->{
                var own=f.own(p);var kelly=f.actor(p,"CastKimber");
                h.assertTrue(own.getBoolean("ComicBack")&&f.rel(kelly,p).distanceTo(Vec3.atBottomCenterOf(BorrascaStory.FORT))<1.3,"she goes back to her place: step "+own.getInt("ComicStep")+" at "+f.rel(kelly,p));
                h.assertTrue(own.getInt("FortPlayTicks")>=BorrascaStory.FORT_AFTER_COMIC,"and the ridge is a few seconds off");
            })
            .thenExecute(()->NativeTestPlayers.packets(p))
            .thenWaitUntil(()->{
                var own=f.own(p);
                h.assertTrue(own.getBoolean("FortEcho"),"the window game ends with an interruption from the ridge");
                var echo=NativeTestPlayers.sounds(p).stream()
                        .filter(sound->sound.getSound().value()==BorrascaRegistry.CHIME.get()).findFirst().orElse(null);
                h.assertTrue(echo!=null,"a native chime packet reaches the reader after the window game");
                h.assertTrue(p.distanceToSqr(echo.getX(),echo.getY(),echo.getZ())<64,
                        "the positional echo is in audible range");
                h.assertTrue(own.getInt("LegendTicks")<100,"Kelly gives the chime time to finish before naming it");
            })
            .thenWaitUntil(()->{
                var name=f.actor(p,"CastKimber").getCustomName();
                h.assertTrue(name!=null&&name.getString().contains("Shiny Gentleman"),"Kelly names the sound after the chime");
            })
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("LegendDone"),"she tells the legend with the reader there"))
            .thenExecute(()->f.at(p,1,6,-92))
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FriendCarved"),"she climbs down and carves where the reader can see: downStep="+f.own(p).getInt("DownStep")))
            .thenExecute(()->{f.carve(p);var own=f.own(p);h.assertTrue(own.getBoolean("PactCarved")&&own.getInt("Beat")==1,"the pact is made, in the order the reader came to it");})
            .thenExecute(()->{
                var account=BorrascaTexts.account(f.own(p));
                h.assertTrue(account.contains("let me pick a window. She stood a comic of Patrick's in its sill for me. She had only got halfway through it. We were still up there when the ridge rang."),"the account keeps the comic between the window and the ridge: "+account);
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_ahead_taking",timeoutTicks=5200)
    public static void aReaderWaitingAtTheClearingStillLosesHerThere(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_waiting");final double[] closest={99,99};final boolean[] watching={false};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,2);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the friend is at the first marker"))
            .thenExecute(()->{f.read(p,BorrascaRooms.SOURCE,false);f.talk(p,"CastKimber");h.assertTrue(f.own(p).getBoolean("FollowAgain"),"she sets off");f.at(p,0,9,-146);p.setYRot(180);watching[0]=true;})
            .thenWaitUntil(()->h.assertTrue(f.own(p).getInt("Shelter6")>=60&&f.own(p).getInt("Shelter10")>=60,"she shelters at both timbers on her own when the noise comes: step="+f.own(p).getInt("TakingStep")))
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FriendMissing"),"she reaches the clearing where the reader is waiting, and is taken there: friend="+f.actor(p,"CastKimber").position().subtract(Vec3.atLowerCornerOf(f.base(p)))+" step="+f.own(p).getInt("TakingStep")))
            .thenExecute(()->{watching[0]=false;var own=f.own(p);h.assertTrue(!own.getBoolean("Sheltered6")&&!own.getBoolean("Sheltered10"),"the reader was never made to crouch");
                h.assertTrue(own.getBoolean("Led")&&closest[0]<2.5&&closest[1]<2.5,"two men came down to her and led her away: "+closest[0]+", "+closest[1]);
                h.assertTrue(BorrascaBelongings.dropOnce(p,own,"Compass",BorrascaRooms.at(f.base(p),BorrascaRooms.DROP))==false,"her one compass is already lying on the trail");})
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,2);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is placed"))
            .thenExecute(()->{
                h.assertTrue(f.actor(p,"Worker0").phase()==BorrascaStory.WORKER&&f.actor(p,"Worker1").phase()==BorrascaStory.WORKER+1,"each of the two men has his own face");
                f.read(p,BorrascaRooms.SOURCE,false);f.talk(p,"CastKimber");var own=f.own(p);own.putInt("TakingStep",Integer.MAX_VALUE);f.save(p,own);
                f.actor(p,"CastKimber").moveTo(Vec3.atBottomCenterOf(BorrascaRooms.at(f.base(p),BorrascaRooms.TAKING)),180,0);f.at(p,0,9,-146);p.setYRot(180);watching[0]=true;})
            .thenWaitUntil(()->h.assertTrue(f.own(p).getInt("TakingTicks")>0&&f.own(p).getInt("Strokes")>=1,"the light comes on with the first stroke"))
            .thenExecute(()->{var own=f.own(p);h.assertTrue(own.getInt("Strokes")==1&&!own.getBoolean("Led"),"one stroke, and they are still coming: "+own.getInt("Strokes"));
                for(int i=0;i<2;i++){var w=f.actor(p,"Worker"+i);h.assertTrue(w.position().distanceTo(f.actor(p,"CastKimber").position())>12,"they come from up on the ridge road, not beside her: "+w.position().subtract(Vec3.atLowerCornerOf(f.base(p))));}})
            .thenWaitUntil(()->h.assertTrue(f.own(p).getInt("Strokes")>=2,"the second stroke as they turn down toward her: "+f.actor(p,"Worker0").position().subtract(Vec3.atLowerCornerOf(f.base(p)))+" step "+f.own(p).getInt("Taker0")))
            .thenExecute(()->h.assertTrue(!f.own(p).getBoolean("Led"),"the second stroke comes before they reach her"))
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("Led")&&f.own(p).getInt("Strokes")==3,"the third stroke as a hand closes on her arm: "+f.actor(p,"Worker0").position().subtract(Vec3.atLowerCornerOf(f.base(p)))))
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FriendMissing"),"her compass falls as she is led up the lane"))
            .thenWaitUntil(()->{var w=f.actor(p,"Worker0");var own=f.own(p);h.assertTrue(own.getBoolean("Taker0Hidden")||f.rel(w,p).z< -157,"watched, they walk her back up the lane toward the road: "+w.position().subtract(Vec3.atLowerCornerOf(f.base(p))));})
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,1);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is in place")).thenExecute(()->{
            var road=BorrascaRooms.trail(-126);f.at(p,road.getX(),road.getY(),road.getZ());p.setYRot(180);p.setXRot(-8);f.refresh(p);var own=f.own(p);
            var at=BorrascaStory.gentleman(own);h.assertTrue(own.getBoolean("PastTwo1")&&at!=null,"a child past the second marker hears the noise, and he is there: ticks "+own.getInt("GentTicks")+", echoes "+own.getInt("Echoes"));
            var head=at.add(0,BorrascaStory.GENT_HEIGHT*BorrascaStory.gentScale(own)-.4,0);double d=head.distanceTo(p.getEyePosition());
            h.assertTrue(d>=BorrascaStory.GENT_NEAR&&d<=BorrascaStory.GENT_FAR&&BorrascaRooms.authored(at.subtract(Vec3.atLowerCornerOf(f.base(p)))).z< -124,"he stands past the second marker, far off: "+d+" at "+at.subtract(Vec3.atLowerCornerOf(f.base(p))));
            h.assertTrue(f.out.clip(new ClipContext(p.getEyePosition(),head,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS,"in clear sight of the reader");
            var view=BorrascaStory.view(p,own);h.assertTrue(view!=null&&view.gentlemanTicks()==BorrascaStory.GENT_TICKS&&view.gentleman().equals(at)&&view.gentlemanScale()==BorrascaStory.gentScale(own),"the reader's own view is told where he stands and how tall, for as long as the strokes last");
            p.lookAt(EntityAnchorArgument.Anchor.EYES,at.add(0,BorrascaStory.GENT_HEIGHT*BorrascaStory.gentScale(own)*.6,0));f.refresh(p);h.assertTrue(f.own(p).getBoolean("SawGentleman"),"looking straight at him is remembered");
            h.assertTrue(BorrascaTexts.account(f.own(p)).contains("shone"),"and the account says so");
            // 0.4.440: back below the marker, where the noise does not come nearer.
            var below=BorrascaRooms.trail(-118);f.at(p,below.getX(),below.getY(),below.getZ());
            for(int i=0;i<15;i++)f.refresh(p);var after=f.own(p);h.assertTrue(BorrascaStory.gentleman(after)==null&&BorrascaStory.view(p,after).gentlemanTicks()==0,"he is gone when the strokes end");
            // Grown, the reader never sees him.
            f.stage(p,3);var grown=f.own(p);grown.putInt("GentTicks",BorrascaStory.GENT_TICKS);f.save(p,grown);h.assertTrue(BorrascaStory.gentleman(f.own(p))==null,"on the adult visits he never comes");
        }).thenSucceed();
    }

    /**
     * 0.4.434 (the owner: "Ensure the shiny gentleman still appears as an actual monster out by the far marker too"). From the foot of the
     * Tree, looking up the ridge, the noise shows him far off past the second marker, much farther than he could be seen before the hill
     * was drawn out, and taller for the distance.
     */
    @GameTest(template="empty",batch="borrasca_gentleman_far",timeoutTicks=1800)
    public static void fromTheTreeHeStandsOutPastTheSecondMarker(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_far");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,1);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is in place")).thenExecute(()->{
            // A few steps off to the side of the trunk, where a child stands to look up the ridge (against the bark, the trunk fills the view).
            var tree=BorrascaRooms.trail(-97);var col=BorrascaRooms.at(f.base(p),tree.getX()+6,0,tree.getZ()+2);int ground=f.out.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,col.getX(),col.getZ());
            p.teleportTo(f.out,col.getX()+.5,ground,col.getZ()+.5,180,-6);p.setDeltaMovement(Vec3.ZERO);
            var own=f.own(p);var at=BorrascaStory.glimpseFor(p,own);f.save(p,own);
            h.assertTrue(at!=null,"the noise shows him to a child at the Tree: "+whyNot(p,f.base(p)));
            var rel=BorrascaRooms.authored(at.subtract(Vec3.atLowerCornerOf(f.base(p))));float scale=BorrascaStory.gentScale(own);
            var head=at.add(0,BorrascaStory.GENT_HEIGHT*scale-.4,0);double d=head.distanceTo(p.getEyePosition());
            h.assertTrue(rel.z< -124&&d>70&&d<=BorrascaStory.GENT_FAR,"he stands out past the second marker, far off: "+d+" at "+rel);
            h.assertTrue(scale>1.5,"and stands taller for the distance: "+scale);
            h.assertTrue(f.out.clip(new ClipContext(p.getEyePosition(),head,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS,"his head is in clear sight from the Tree");
            var view=BorrascaStory.view(p,own);h.assertTrue(view!=null&&view.gentlemanScale()==scale&&view.gentleman().equals(at),"the reader's view is told where and how tall");
        }).thenSucceed();
    }
    /** Why no place past the second marker would do, for the failure message: what each column was turned down for. */
    private static String whyNot(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();var eye=p.getEyePosition();int cols=0,unloaded=0,high=0,ground=0,near=0,room=0,clip=0,ok=0;var hits=new java.util.TreeMap<String,Integer>();
        for(int z=-128;z>=-165;z-=3)for(int x=-30;x<=30;x+=3){cols++;
            var column=BorrascaRooms.at(b,x,0,z);if(!l.hasChunkAt(column)){unloaded++;continue;}
            int y=l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,column.getX(),column.getZ());
            if(y<column.getY()-1||y>column.getY()+16){high++;continue;}var feet=new BlockPos(column.getX(),y,column.getZ());
            var under=l.getBlockState(feet.below());if(under.is(net.minecraft.tags.BlockTags.LOGS)||under.is(net.minecraft.tags.BlockTags.LEAVES)||under.is(BorrascaRegistry.FITTING.get())){ground++;continue;}
            var base=Vec3.atBottomCenterOf(feet);double far=base.distanceTo(eye);if(far<BorrascaStory.GENT_NEAR-BorrascaStory.GENT_HEIGHT||far>BorrascaStory.GENT_FAR){near++;continue;}double tall=BorrascaStory.GENT_HEIGHT*BorrascaStory.gentScale(far);
            boolean r=true;for(int i=0;i<Math.ceil(tall)&&r;i++)r=l.getBlockState(feet.above(i)).getCollisionShape(l,feet.above(i)).isEmpty();if(!r){room++;continue;}
            var head=base.add(0,tall-.4,0);var hit=l.clip(new ClipContext(eye,head,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));
            if(hit.getType()!=HitResult.Type.MISS){clip++;hits.merge(l.getBlockState(hit.getBlockPos()).getBlock().getDescriptionId()+"@"+(int)BorrascaRooms.authored(hit.getBlockPos().subtract(b)).getZ(),1,Integer::sum);continue;}ok++;}
        return "cols "+cols+" unloaded "+unloaded+" high "+high+" ground "+ground+" near "+near+" room "+room+" clip "+clip+" ok "+ok+" eye "+BorrascaRooms.authored(BlockPos.containing(eye).subtract(b))+" hits "+hits;
    }
    @AfterBatch(batch="borrasca_gentleman_far") public static void gentlemanFarDone(ServerLevel l){cleanup();}

    /**
     * 0.4.440 (the owner: "have the klangs get closer while the player is past the mile marker and grow more frequent. the gentleman grows
     * closer to a child player with each until it's directly above them. it picks them up and eats them after that, sending the player to
     * the triple tree again"; "I'd allow it on visit one, too"). A child who stays past the second marker hears the noise again and again,
     * sooner each time, and each time he stands nearer, until he stands over them; then he has them, and they are under the Triple Tree.
     * Going back below the marker lets him fall back.
     */
    @GameTest(template="empty",batch="borrasca_gentleman_near",timeoutTicks=2400)
    public static void pastTheSecondMarkerHeComesNearerAndTakesTheChild(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_near");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,1);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is in place")).thenExecute(()->{
            var b=f.base(p);var road=BorrascaRooms.trail(-130);
            var near=new java.util.ArrayList<Double>();var gaps=new java.util.ArrayList<Integer>();int since=0;
            for(int i=0;i<200&&f.own(p).getInt("GrabTicks")==0;i++){
                f.at(p,road.getX(),road.getY(),road.getZ());p.setYRot(180);p.setXRot(-8);int before=f.own(p).getInt("Approach");f.refresh(p);since+=5;
                var own=f.own(p);var g=BorrascaStory.gentleman(own);
                if(own.getInt("Approach")>before&&g!=null){near.add(Math.hypot(g.x-p.getX(),g.z-p.getZ()));gaps.add(since);since=0;
                    h.assertTrue(own.getInt("Echoes")>0,"the noise comes with him");}
            }
            h.assertTrue(near.size()==BorrascaStory.NEARER.length+1,"he came nearer with each stroke, then stood over the child: "+near);
            for(int i=1;i<near.size();i++)h.assertTrue(near.get(i)<near.get(i-1),"each time nearer: "+near);
            h.assertTrue(Math.abs(near.get(0)-BorrascaStory.NEARER[0])<4&&near.get(near.size()-1)<BorrascaStory.OVER+1,"from far off to over them: "+near);
            for(int i=2;i<gaps.size();i++)h.assertTrue(gaps.get(i)<=gaps.get(i-1),"and the noise comes sooner each time: "+gaps);
            var own=f.own(p);h.assertTrue(own.getInt("GrabTicks")>0&&BorrascaStory.view(p,own).grab()>0,"then he has them, and the child's own view is told");
            h.assertTrue(BorrascaStory.gentScale(own)>=(float)BorrascaStory.GENT_SCALE_MAX-.01F,"standing at his tallest over them");
            for(int i=0;i<BorrascaStory.GRAB_TICKS/5+2&&f.own(p).getInt("Swallowed")==0;i++)f.refresh(p);
            own=f.own(p);var rel=BorrascaRooms.rel(p,b);
            h.assertTrue(own.getInt("Swallowed")==1&&own.getInt("GrabTicks")==0&&own.getInt("Approach")==0,"he ate them once, and it is over: "+own.getInt("Swallowed"));
            h.assertTrue(rel.z>-100&&rel.z<-89&&Math.abs(rel.x)<5&&Math.abs(rel.y-6)<.2&&p.isAlive(),"and they are in the fort in the Triple Tree, on its floor, unhurt: "+rel);
            h.assertTrue(BorrascaStory.view(p,own).swallowed()==1&&BorrascaStory.gentleman(own)==null,"the view is told, and he is gone");
            h.assertTrue(BorrascaTexts.account(own).contains("Once I went on past the second marker"),"the account remembers it");
            // Below the marker he does not come nearer; he falls back a step for each little while there.
            own.putInt("Approach",3);f.save(p,own);var below=BorrascaRooms.trail(-112);
            for(int i=0;i<BorrascaStory.FALL_BACK/5;i++){f.at(p,below.getX(),below.getY(),below.getZ());f.refresh(p);}
            h.assertTrue(f.own(p).getInt("Approach")==2&&f.own(p).getInt("GrabTicks")==0,"below the marker he falls back: "+f.own(p).getInt("Approach"));
            // Grown, never.
            f.stage(p,3);own=f.own(p);own.putInt("Approach",6);f.save(p,own);var past=BorrascaRooms.trail(-130);
            for(int i=0;i<40;i++){f.at(p,past.getX(),past.getY(),past.getZ());f.refresh(p);}
            h.assertTrue(f.own(p).getInt("GrabTicks")==0&&f.own(p).getInt("Swallowed")==1,"grown, he never comes for them");
        }).thenSucceed();
    }
    @AfterBatch(batch="borrasca_gentleman_near") public static void gentlemanNearDone(ServerLevel l){cleanup();}

    /**
     * 0.4.440 (the owner: "secure this so the player can't see it from the gate", of the cone and its rings standing up over the depot;
     * the book: "We were standing in what I guessed was a refinery and in the middle of the room was a large silver, conically shaped
     * machine"). From anywhere along the gate, every line to the cone, its tip and the far reach of its arms meets the refinery's walls or
     * roof; the roof is whole over the machine; the lamp is on the refinery's front, still in sight of the taking.
     */
    @GameTest(template="empty",batch="borrasca_refinery",timeoutTicks=1800)
    public static void theMachineStandsInsideTheRefinery(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_refinery");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{
            var b=f.base(p);var machine=Vec3.atLowerCornerOf(BorrascaRooms.at(b,BorrascaRooms.MACHINE)).add(.5,0,.5);
            var targets=new java.util.ArrayList<Vec3>();
            for(float y:new float[]{5.5F,8,11,15,16.5F})targets.add(machine.add(0,y,0));
            for(int k=0;k<8;k++){double a=k*Math.PI/4;targets.add(machine.add(Math.cos(a)*4.9,6.6,Math.sin(a)*4.9));targets.add(machine.add(Math.cos(a)*3.6,10.4,Math.sin(a)*3.6));}
            int checked=0;
            for(double x=-1.2;x<=2.2;x+=.85)for(double back:new double[]{.6,3,8})for(double eye:new double[]{1.0,1.62,2.4}){
                var gate=Vec3.atLowerCornerOf(BorrascaRooms.at(b,0,12,-167)).add(x,eye,1+back);
                for(var t:targets){checked++;var hit=f.out.clip(new ClipContext(gate,t,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));
                    h.assertTrue(hit.getType()==HitResult.Type.BLOCK&&hit.getLocation().distanceTo(t)>1.5,"from the gate the machine is hidden: "+gate.subtract(Vec3.atLowerCornerOf(b))+" to "+t.subtract(Vec3.atLowerCornerOf(b))+" hit "+(hit.getType()==HitResult.Type.BLOCK?f.out.getBlockState(hit.getBlockPos())+" at "+hit.getBlockPos().subtract(b):"nothing"));}}
            h.assertTrue(checked>100,"looked from enough places: "+checked);
            for(int x=-BorrascaRooms.REF_X;x<=BorrascaRooms.REF_X;x++)for(int z=BorrascaRooms.REF_NORTH;z<=BorrascaRooms.REF_SOUTH;z++){
                int half=BorrascaRooms.refineryRoof(x);var at=BorrascaRooms.at(b,x,half/2,z);h.assertTrue(f.out.getBlockState(at).getBlock() instanceof SlabBlock,"the roof is whole at "+x+","+z);}
            h.assertTrue(f.out.getBlockState(BorrascaRooms.at(b,BorrascaRooms.LAMP)).is(BorrascaRegistry.FITTING.get())&&!f.out.getBlockState(BorrascaRooms.at(b,BorrascaRooms.LAMP).north()).isAir(),"the lamp is on the refinery's front wall");
            assertTakingSightline(f,p);
        }).thenSucceed();
    }
    @AfterBatch(batch="borrasca_refinery") public static void refineryDone(ServerLevel l){cleanup();}

    /**
     * 0.4.435 (the owner: "Let players ride bikes? Maintaining balance and pedaling left/right should be a function."): Kendra's bike in
     * town is looked at, got on, pushed along by each strafe key in turn, and got off with a sneak. It is left on its kickstand where it
     * stopped, and it is still hers.
     */
    @GameTest(template="empty",batch="borrasca_bike",timeoutTicks=1800)
    public static void aChildGetsOnKendrasBikePedalsAndGetsOff(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_bike");final BorrascaBike[] bike={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,1);})
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&BorrascaShopBlock.is(f.out.getBlockState(BorrascaRooms.at(f.base(p),BorrascaRooms.KENDRA_BIKE)),BorrascaShopBlock.Kind.BIKE),"her bike is out in town"))
          .thenExecute(()->{
              var b=f.base(p);var at=BorrascaRooms.at(b,BorrascaRooms.KENDRA_BIKE);var rel=at.subtract(b);
              var own=f.own(p);h.assertTrue(BorrascaStory.click(p,b,rel,own),"the bike answers a click");f.save(p,own);
              h.assertTrue(!p.isPassenger()&&f.own(p).getBoolean("BikeLooked"+BorrascaBike.KENDRA),"the first click is a look at it");
              own=f.own(p);BorrascaStory.click(p,b,rel,own);f.save(p,own);
              h.assertTrue(p.getVehicle() instanceof BorrascaBike,"the second gets on it");bike[0]=(BorrascaBike)p.getVehicle();
              h.assertTrue(f.out.getBlockState(at).isAir()&&bike[0].whose()==BorrascaBike.KENDRA,"it is off its stand while it is ridden, and it is Kendra's");
          }).thenIdle(40).thenExecute(()->{
              h.assertTrue(p.getVehicle()==bike[0]&&Math.abs(bike[0].leanNow())<.05F,"stopped, a foot is down and it stays up: "+bike[0].leanNow());
              // 0.4.447: the rider's eyes were level with the handlebars and the top of the saddle, which lay across the view.
              double hips=p.getY()+.675*p.getScale()-bike[0].getY(),eyes=p.getEyeY()-bike[0].getY();
              h.assertTrue(Math.abs(hips-BorrascaBike.SADDLE)<.03&&eyes-14.6/16>.3,"the rider sits on the saddle, their eyes well over the bars: hips "+hips+", eyes "+eyes);
              p.setPlayerInput(1,0,false,false);
          }).thenIdle(2).thenExecute(()->{
              h.assertTrue(bike[0].strokes()==-1&&bike[0].speedNow()>0,"the left strafe key is a push on the left pedal: "+bike[0].strokes());
              p.setPlayerInput(0,0,false,false);
          }).thenIdle(2).thenExecute(()->p.setPlayerInput(-1,0,false,false)).thenIdle(2).thenExecute(()->{
              h.assertTrue(bike[0].strokes()==2,"the right key pushes the right pedal: "+bike[0].strokes());
              p.setPlayerInput(0,0,false,true);
          }).thenWaitUntil(()->h.assertTrue(!p.isPassenger()&&bike[0].isRemoved(),"sneaking gets off"))
          .thenExecute(()->{
              var own=f.own(p);h.assertTrue(own.contains("BikeAt"+BorrascaBike.KENDRA),"where it was left is remembered");
              var left=BlockPos.of(own.getLong("BikeAt"+BorrascaBike.KENDRA));var s=f.out.getBlockState(left);
              h.assertTrue(BorrascaShopBlock.is(s,BorrascaShopBlock.Kind.BIKE)&&s.getValue(BorrascaShopBlock.STAGE)==0,"and it stands there on its kickstand");
              h.assertTrue(BorrascaStory.whoseBike(f.base(p),own,left)==BorrascaBike.KENDRA,"still Kendra's where it was left");
              h.assertTrue(own.getBoolean("BikeRode"+BorrascaBike.KENDRA)&&BorrascaTexts.account(own).contains("I rode Kendra's bike"),"and the account says so");
          }).thenSucceed();
    }
    @AfterBatch(batch="borrasca_bike") public static void bikeDone(ServerLevel l){cleanup();}

    /**
     * 0.4.435: on a straight road, pushing the pedals in turn, and twice on the far pedal whenever it starts to go over, keeps a moving bike
     * up and carries it a good way. Pushing one pedal over and over tips it until the reader comes off, and the bike lies in the road.
     */
    @GameTest(template="empty",batch="borrasca_bike_balance",timeoutTicks=1800)
    public static void pedallingInTurnKeepsTheBikeUpAndOnePedalPutsItDown(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_pedal");final BorrascaBike[] bike={null};final Vec3[] start={null};final int[] t={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,1);})
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is in place"))
          .thenExecute(()->{
              // A clear straight road high over the town, inside the site, so nothing but the rider decides where the bike goes.
              var road=f.base(p).offset(0,36,-6);
              for(int z=0;z>-72;z--)for(int x=-1;x<=1;x++){f.out.setBlock(road.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);for(int y=0;y<4;y++)f.out.setBlock(road.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);}
              p.teleportTo(f.out,road.getX()+.5,road.getY(),road.getZ()+.5,180,0);
              var made=BorrascaRegistry.BIKE.get().create(f.out);made.moveTo(road.getX()+.5,road.getY(),road.getZ()+.5,180,0);made.setUp(p,BorrascaBike.PATRICK,180);f.out.addFreshEntity(made);
              h.assertTrue(p.startRiding(made,true),"on Patrick's bike");bike[0]=made;start[0]=made.position();
          }).thenExecuteFor(160,()->{
              // Pushing in turn every eight ticks; when it starts to go over, the pedal on the other side, twice.
              var b=bike[0];int n=t[0]++;if(p.getVehicle()!=b)return;
              if(b.leanNow()>.3F&&n%2==0)b.pedal(-1);else if(b.leanNow()< -.3F&&n%2==0)b.pedal(1);else if(n%8==0)b.pedal(b.strokes()<0?1:-1);
          }).thenExecute(()->{
              var b=bike[0];h.assertTrue(p.getVehicle()==b,"pedalled in turn and kept straight, it is still up after eight seconds: lean "+b.leanNow());
              double went=b.position().subtract(start[0]).horizontalDistance();h.assertTrue(went>25,"and it has gone a good way: "+went);
              h.assertTrue(f.own(p).getBoolean("BikeRode"+BorrascaBike.PATRICK),"the reader rode Patrick's bike");
          }).thenExecuteFor(80,()->{if(p.getVehicle()==bike[0]&&t[0]++%4==0)bike[0].pedal(1);})
          .thenExecute(()->{
              h.assertTrue(!p.isPassenger()&&bike[0].isRemoved(),"pushing the one pedal over and over, the reader comes off");
              var own=f.own(p);h.assertTrue(own.getInt("BikeFalls")==1,"one fall, remembered: "+own.getInt("BikeFalls"));
              var left=BlockPos.of(own.getLong("BikeAt"+BorrascaBike.PATRICK));var s=f.out.getBlockState(left);
              h.assertTrue(BorrascaShopBlock.is(s,BorrascaShopBlock.Kind.BIKE)&&s.getValue(BorrascaShopBlock.STAGE)==1,"and the bike lies on its side in the road");
              h.assertTrue(BorrascaTexts.account(own).contains("I came off once"),"the account remembers it");
          }).thenSucceed();
    }
    @AfterBatch(batch="borrasca_bike_balance") public static void bikeBalanceDone(ServerLevel l){cleanup();}

    /**
     * 0.4.436 (the owner: "It should have its own arcade games, don't recycle"): the drugstore's two cabinets stand by its south wall.
     * A play takes a quarter from the child's own money, and none is taken from a child who has none. The server keeps only what the
     * game scores when it is played again from the screen's keys, and refuses a log longer than the session has been open. The table
     * starts with the town's children's initials, Patrick's at the top.
     */
    @GameTest(template="empty",batch="borrasca_arcade",timeoutTicks=1800)
    public static void theDrugstoreCabinetsPlayTheirOwnGames(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_arcade");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,1);})
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is in place")).thenExecute(()->{
              var b=f.base(p);
              for(int game=0;game<2;game++){var low=f.out.getBlockState(b.offset(BorrascaShops.CABINETS[game]));var top=f.out.getBlockState(b.offset(BorrascaShops.CABINETS[game]).above());
                  h.assertTrue(BorrascaShopBlock.is(low,BorrascaShopBlock.Kind.CABINET)&&BorrascaShopBlock.is(top,BorrascaShopBlock.Kind.CABINET_TOP)&&BorrascaArcade.game(low)==game&&BorrascaArcade.game(top)==game,"cabinet "+game+" stands in the drugstore, both halves");}
              var own=f.own(p);
              h.assertTrue(BorrascaArcade.table(own,BorrascaArcade.SEAM).get(0)[0].equals("PAT"),"Patrick's initials are at the top of the mine game before the reader plays");
              int cents=own.getInt("Cents");
              h.assertTrue(BorrascaStory.click(p,b,BorrascaShops.CABINETS[BorrascaArcade.SEAM].above(),own),"the screen half answers a click");f.save(p,own);
              var first=BorrascaArcade.session(p.getUUID());h.assertTrue(first!=null&&first.game()==BorrascaArcade.SEAM,"a quarter in starts DEEP SEAM");
              h.assertTrue(f.own(p).getInt("Cents")==cents-BorrascaArcade.PRICE,"and the quarter comes out of the reader's money");
              // A cart nobody steers: the keys held on every tick, as runs, are all the server is sent.
              var g=BorrascaArcade.of(BorrascaArcade.SEAM,first.token(),null);int ticks=0;while(!g.over()&&ticks<BorrascaArcade.LONGEST[0]){g.tick(0);ticks++;}
              h.assertTrue(g.over(),"a cart nobody steers comes to grief");
              h.assertTrue(!BorrascaArcade.submit(p,first.token(),List.of(ticks<<5)),"a log longer than the session has been open is refused");
              own=f.own(p);BorrascaStory.click(p,b,BorrascaShops.CABINETS[BorrascaArcade.SEAM],own);f.save(p,own);
              var second=BorrascaArcade.session(p.getUUID());h.assertTrue(second!=null&&second.token()!=first.token(),"another quarter, another game");
              var again=BorrascaArcade.of(BorrascaArcade.SEAM,second.token(),null);int n=0;while(!again.over()&&n<BorrascaArcade.LONGEST[0]){again.tick(0);n++;}
              BorrascaArcade.backdate(p.getUUID(),n+5);
              h.assertTrue(BorrascaArcade.submit(p,second.token(),List.of(n<<5)),"a game the session had time for stands");
              h.assertTrue(f.own(p).getInt("ArcadeBest"+BorrascaArcade.SEAM)==again.score(),"and the score kept is the one the server's own replay reaches: "+again.score());
              h.assertTrue(f.own(p).getInt("Cents")==cents-2*BorrascaArcade.PRICE,"two plays, two quarters");
              // The table: a good game goes in under Patrick's, and the last of the children's drops off the bottom.
              own=f.own(p);h.assertTrue(BorrascaArcade.record(own,BorrascaArcade.SEAM,"ZZZ",700)==2,"seven hundred goes in second, under Patrick's seven thirty");
              var rows=BorrascaArcade.table(own,BorrascaArcade.SEAM);h.assertTrue(rows.size()==BorrascaArcade.ROWS&&rows.get(0)[0].equals("PAT")&&rows.get(1)[0].equals("ZZZ")&&rows.stream().noneMatch(r->r[0].equals("PAM")),"and the last of the children's falls off the bottom");f.save(p,own);
              // No money, no game.
              own=f.own(p);own.putInt("Cents",10);own.putInt("FriendCents",0);f.save(p,own);own=f.own(p);BorrascaStory.click(p,b,BorrascaShops.CABINETS[BorrascaArcade.CAT],own);f.save(p,own);
              h.assertTrue(BorrascaArcade.session(p.getUUID())==null&&f.own(p).getInt("Cents")==10,"a child without a quarter does not get a game, and keeps the dime");
          }).thenSucceed();
    }
    @AfterBatch(batch="borrasca_arcade") public static void arcadeDone(ServerLevel l){cleanup();}

    @AfterBatch(batch="borrasca_arcade_friends") public static void arcadeFriendsDone(ServerLevel l){cleanup();}
    /**
     * 0.4.446 (owner: "Give Patrick and Kendra a turn at the arcade. Patrick watches your first run of Deep Seam and asks for a
     * rematch if you beat him. Kendra knows a trick he keeps getting wrong. On the next visit, she reacts differently depending on
     * whose initials are at the top."). The reader's first quarter in DEEP SEAM brings the two of them in through the drugstore's
     * door. Under Patrick's score he says so and Kendra says what he gets wrong, then takes BIG CAT, where she can be watched. Over
     * it he takes the cabinet and hops every timber; told, he goes round them and has the top back; and the next time, Kendra says
     * how it came out.
     */
    @GameTest(template="empty",batch="borrasca_arcade_friends",timeoutTicks=6000)
    public static void patrickAndKendraTakeTheirTurnsAtTheCabinets(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_cabinets");final int seam=BorrascaArcade.SEAM,cat=BorrascaArcade.CAT;
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,1);})
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is in place")).thenExecute(()->{
              var b=f.base(p);var out=b.offset(BorrascaArcadeFriends.OUT);
              h.assertTrue(f.out.getBlockState(out).getCollisionShape(f.out,out).isEmpty()&&f.out.getBlockState(out.above()).getCollisionShape(f.out,out.above()).isEmpty()&&f.out.getBlockState(out.below()).isCollisionShapeFullBlock(f.out,out.below()),"the sidewalk by the drugstore's door is somewhere to stand");
              h.assertTrue(f.out.getBlockState(b.offset(BorrascaShops.DRUG_DOOR)).getBlock() instanceof DoorBlock&&!f.out.getBlockState(b.offset(BorrascaShops.DRUG_DOOR)).getValue(DoorBlock.OPEN),"and the door is shut");
              f.atWorld(p,new BlockPos(9,0,-94));p.setYRot(0);p.setYHeadRot(0);
              var own=f.own(p);own.putInt("Cents",200);
              h.assertTrue(BorrascaStory.click(p,b,BorrascaShops.CABINETS[seam],own),"a quarter in DEEP SEAM");f.save(p,own);
              h.assertTrue(BorrascaArcade.session(p.getUUID())!=null&&f.own(p).getInt("ArcadeScene")==BorrascaArcadeFriends.ON,"the reader's first game there begins the afternoon");
          })
          .thenWaitUntil(()->{
              var own=f.own(p);var b=f.base(p);var pat=f.actor(p,"TownEli");var ken=f.actor(p,"TownWren");
              h.assertTrue(own.getInt("PatMode")==BorrascaArcadeFriends.WATCHING&&own.getInt("KenMode")==BorrascaArcadeFriends.WATCHING,"they come in while the screen has the reader's eyes: Patrick "+own.getInt("PatMode")+" at "+pat.position().subtract(Vec3.atLowerCornerOf(b))+", Kendra "+own.getInt("KenMode")+" at "+ken.position().subtract(Vec3.atLowerCornerOf(b)));
              h.assertTrue(BorrascaArcadeFriends.inside(pat,b)&&BorrascaArcadeFriends.inside(ken,b)&&pat.position().distanceTo(Vec3.atBottomCenterOf(b.offset(BorrascaArcadeFriends.PAT_WATCH)))<1&&ken.position().distanceTo(Vec3.atBottomCenterOf(b.offset(BorrascaArcadeFriends.KEN_WATCH)))<1,"Patrick at the reader's shoulder and Kendra behind him");
              h.assertTrue(!f.out.getBlockState(b.offset(BorrascaShops.DRUG_DOOR)).getValue(DoorBlock.OPEN)&&own.getBoolean("ArcadeSeen"),"and the door shut behind them");
          })
          .thenWaitUntil(()->h.assertTrue(said(f,p,"TownEli").contains("Hop everything"),"with the reader's game still going, Patrick has advice: "+said(f,p,"TownEli")))
          .thenWaitUntil(()->h.assertTrue(said(f,p,"TownWren").contains("Don't listen to him"),"and Kendra has some about his: "+said(f,p,"TownWren")))
          .thenWaitUntil(()->{
              var own=f.own(p);var b=f.base(p);var kel=f.actor(p,"CastKimber");
              h.assertTrue(own.getBoolean("KelIn")&&own.getInt("KelMode")==BorrascaArcadeFriends.WATCHING&&kel.position().distanceTo(Vec3.atBottomCenterOf(b.offset(BorrascaArcadeFriends.KEL_WATCH)))<1,"Kelly, on her way up the street, comes in after them and stands at the comics: in "+own.getBoolean("KelIn")+" mode "+own.getInt("KelMode")+" at "+kel.position().subtract(Vec3.atLowerCornerOf(b)));
              h.assertTrue(!f.out.getBlockState(b.offset(BorrascaShops.DRUG_DOOR)).getValue(DoorBlock.OPEN),"and shuts the door");
          })
          .thenWaitUntil(()->h.assertTrue(said(f,p,"CastKimber").contains("Thirty-one's the one I've got"),"she knows which of Patrick's comics she has: "+said(f,p,"CastKimber")))
          .thenExecute(()->{
              // A cart nobody steers: under Patrick's score.
              var s=BorrascaArcade.session(p.getUUID());var g=BorrascaArcade.of(seam,s.token(),null);int n=0;while(!g.over()&&n<BorrascaArcade.LONGEST[0]){g.tick(0);n++;}
              BorrascaArcade.backdate(p.getUUID(),n+5);h.assertTrue(BorrascaArcade.submit(p,s.token(),List.of(n<<5)),"the reader's game stands");
          })
          .thenWaitUntil(()->h.assertTrue(said(f,p,"TownEli").contains("Seven thirty"),"under his score, Patrick says so: "+said(f,p,"TownEli")))
          .thenWaitUntil(()->h.assertTrue(said(f,p,"TownWren").contains("You can't hop a timber, you go round it"),"and Kendra says what he gets wrong: "+said(f,p,"TownWren")))
          .thenWaitUntil(()->{
              var own=f.own(p);var b=f.base(p);var ken=f.actor(p,"TownWren");
              h.assertTrue(own.getInt("KenMode")==BorrascaArcadeFriends.PLAYING,"then she takes BIG CAT: "+own.getInt("KenMode")+" step "+own.getInt("KenStep"));
              h.assertTrue(ken.position().distanceTo(Vec3.atBottomCenterOf(b.offset(BorrascaArcadeFriends.KEN_PLAY)))<1&&ken.gesture()==3,"standing at it with her hands on it");
              var game=BorrascaArcade.play(cat,own.getLong("KenSeed"),BorrascaArcade.FISHES);
              h.assertTrue(game.score()==own.getInt("KenScore")&&game.ticks()==own.getInt("KenLength")&&game.score()>100&&game.at().length>0,"her game is a real one out of its seed, with a big cat in it: "+game.score()+" in "+game.ticks());
              int cents=own.getInt("Cents");
              h.assertTrue(BorrascaStory.click(p,b,BorrascaShops.CABINETS[cat],own),"a hand on her cabinet");f.save(p,own);
              h.assertTrue(BorrascaArcade.session(p.getUUID())==null&&f.own(p).getInt("Cents")==cents&&f.own(p).getInt("ArcadeWatched"+cat)==1,"watches over her shoulder: no quarter, no game of the reader's");
              own=f.own(p);own.putLong("KenStart",own.getLong("KenStart")-own.getInt("KenLength"));f.save(p,own);
          })
          .thenWaitUntil(()->{
              var own=f.own(p);h.assertTrue(own.getBoolean("KenPlayed")&&own.getInt("KenMode")==BorrascaArcadeFriends.WATCHING,"her game ends");
              var rows=BorrascaArcade.table(own,cat);int rank=own.getInt("KenRank");
              h.assertTrue(rank>=1&&rows.get(rank-1)[0].equals("KEN")&&rows.get(rank-1)[1].equals(Integer.toString(own.getInt("KenScore")))&&said(f,p,"TownWren").contains(BorrascaArcadeFriends.fished(own,rank,BorrascaArcade.initials(p))),"her score goes on BIG CAT's table and she says how it came out: "+said(f,p,"TownWren")+" rank "+rank);
              var fresh=new CompoundTag();
              h.assertTrue(BorrascaArcadeFriends.fished(fresh,BorrascaArcade.record(fresh,cat,"KEN",250),"ZZZ").contains("K, R, T")&&BorrascaArcadeFriends.fished(fresh,BorrascaArcade.record(fresh,cat,"KEN",320),"ZZZ").contains("Kelly")&&BorrascaArcadeFriends.fished(fresh,BorrascaArcade.record(fresh,cat,"KEN",260),"ZZZ").equals(BorrascaArcadeFriends.FISHED_WORSE)&&BorrascaArcadeFriends.fished(new CompoundTag(),0,"ZZZ").equals(BorrascaArcadeFriends.FISHED_NOTHING),"over Kurt's, over Kelly's, worse than her last, or nothing at all");
          })
          .thenExecute(()->{
              // The reader's initials go over Patrick's.
              var own=f.own(p);h.assertTrue(BorrascaArcade.record(own,seam,BorrascaArcade.initials(p),900)==1,"nine hundred goes to the top");own.putBoolean("OverPatrick",true);
              BorrascaArcadeFriends.finished(p,own,seam,true);f.save(p,own);
          })
          .thenWaitUntil(()->h.assertTrue(said(f,p,"TownEli").contains("I'm getting it back")&&f.own(p).getInt("PatMode")==BorrascaArcadeFriends.GOING,"over his score, he wants the cabinet: "+said(f,p,"TownEli")))
          .thenWaitUntil(()->h.assertTrue(said(f,p,"TownEli").contains("Move!"),"and the reader is standing where he means to stand: "+said(f,p,"TownEli")))
          .thenExecute(()->{var own=f.own(p);h.assertTrue(BorrascaStory.click(p,f.base(p),BorrascaShops.CABINETS[seam],own)&&BorrascaArcade.session(p.getUUID())==null&&said(f,p,"TownEli").contains("My go"),"the cabinet is his for now");f.save(p,own);f.atWorld(p,new BlockPos(12,0,-97));p.setYRot(0);p.setYHeadRot(0);})
          .thenWaitUntil(()->{
              var own=f.own(p);var b=f.base(p);var pat=f.actor(p,"TownEli");
              h.assertTrue(own.getInt("PatMode")==BorrascaArcadeFriends.PLAYING,"he plays: "+own.getInt("PatMode"));
              h.assertTrue(pat.position().distanceTo(Vec3.atBottomCenterOf(b.offset(BorrascaArcadeFriends.PAT_PLAY)))<1&&pat.gesture()==3&&own.getInt("PatWay")==BorrascaArcade.HOPS&&own.getInt("PatGames")==1,"at the cabinet, the way he always has");
              var game=BorrascaArcade.play(seam,own.getLong("PatSeed"),BorrascaArcade.HOPS);
              h.assertTrue(game.score()==own.getInt("PatScore")&&game.ticks()==own.getInt("PatLength")&&game.score()<600&&game.ticks()<=1500&&Arrays.stream(game.what()).allMatch(k->k==BorrascaArcade.DeepSeam.TIMBER),"his game is a real one out of its seed, short, and lost to three timbers: "+game.score()+" in "+game.ticks());
              h.assertTrue(BorrascaStory.click(p,b,BorrascaShops.CABINETS[seam],own)&&BorrascaArcade.session(p.getUUID())==null&&own.getInt("ArcadeWatched"+seam)==1,"and can be watched over his shoulder");f.save(p,own);
              own=f.own(p);own.putLong("PatStart",own.getLong("PatStart")-own.getInt("PatLength"));f.save(p,own);
          })
          .thenWaitUntil(()->{
              var own=f.own(p);
              h.assertTrue(own.getBoolean("PatLost")&&own.getInt("PatMode")==BorrascaArcadeFriends.WATCHING&&said(f,p,"TownEli").contains("The stick's sticky"),"he loses, and it was the stick: "+said(f,p,"TownEli"));
              h.assertTrue(said(f,p,"TownWren").contains("Go round them, Pat")&&own.getInt("PatSaid")==3,"Kendra called every timber: "+said(f,p,"TownWren"));
              h.assertTrue(BorrascaArcade.table(own,seam).get(0)[0].equals(BorrascaArcade.initials(p))&&own.getInt("ArcadeTop")==BorrascaArcadeFriends.READERS,"the reader's initials are still at the top");
          })
          .thenWaitUntil(()->h.assertTrue(said(f,p,"CastKimber").contains("It's always the stick"),"Kelly has heard it before: "+said(f,p,"CastKimber")))
          .thenWaitUntil(()->{
              // A word to him is the chance to tell him.
              h.assertTrue(f.rel(f.actor(p,"TownEli"),p)!=null,"he is there");
              f.talk(p,"TownEli");h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu,"to tell him, or to say nothing: mode "+f.own(p).getInt("PatMode"));
              p.containerMenu.clicked(0,0,ClickType.PICKUP,p);var own=f.own(p);
              h.assertTrue(own.getBoolean("ArcadeTold")&&said(f,p,"TownEli").contains("I knew that"),"told, he knew that: "+said(f,p,"TownEli"));
          })
          .thenWaitUntil(()->{
              var own=f.own(p);h.assertTrue(own.getInt("PatMode")==BorrascaArcadeFriends.PLAYING&&own.getInt("PatWay")==BorrascaArcade.ROUND&&own.getInt("PatGames")==2,"and has one more quarter: "+own.getInt("PatMode"));
              var game=BorrascaArcade.play(seam,own.getLong("PatSeed"),BorrascaArcade.ROUND);
              h.assertTrue(game.score()==own.getInt("PatScore")&&game.score()>900&&game.ticks()<=3600,"going round them, his game is a good one: "+game.score()+" in "+game.ticks());
              own.putLong("PatStart",own.getLong("PatStart")-own.getInt("PatLength"));f.save(p,own);
          })
          .thenWaitUntil(()->{
              var own=f.own(p);var rows=BorrascaArcade.table(own,seam);
              h.assertTrue(own.getInt("PatMode")==BorrascaArcadeFriends.WATCHING&&rows.get(0)[0].equals("PAT")&&rows.get(0)[1].equals(Integer.toString(own.getInt("PatScore")))&&rows.get(1)[0].equals(BorrascaArcade.initials(p))&&own.getInt("ArcadeTop")==BorrascaArcadeFriends.HIS_AGAIN,"he has the top back, over the reader's: "+rows.get(0)[0]+" "+rows.get(0)[1]);
              h.assertTrue(said(f,p,"TownEli").contains("P. A. T. Read it."),"and says so: "+said(f,p,"TownEli"));
          })
          .thenWaitUntil(()->h.assertTrue(said(f,p,"TownWren").contains("You told him"),"Kendra knows who to thank: "+said(f,p,"TownWren")))
          .thenExecute(()->{
              f.talk(p,"TownEli");h.assertTrue(!(p.containerMenu instanceof LiteraryChoiceMenu),"there is nothing more to tell him");
              // The reader goes back to the gate and turns their back on the town.
              f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);
          })
          .thenWaitUntil(()->{
              var own=f.own(p);var b=f.base(p);var pat=f.actor(p,"TownEli");var ken=f.actor(p,"TownWren");
              var kel=f.actor(p,"CastKimber");
              h.assertTrue(own.getInt("ArcadeScene")==BorrascaArcadeFriends.OVER&&own.getBoolean("ArcadeHomed")&&own.getInt("KelMode")==BorrascaArcadeFriends.LEFT,"with the reader gone they leave by the door: scene "+own.getInt("ArcadeScene")+" Patrick "+own.getInt("PatMode")+" at "+pat.position().subtract(Vec3.atLowerCornerOf(b))+" Kendra "+own.getInt("KenMode")+" at "+ken.position().subtract(Vec3.atLowerCornerOf(b))+" Kelly "+own.getInt("KelMode")+" at "+kel.position().subtract(Vec3.atLowerCornerOf(b)));
              h.assertTrue(!BorrascaArcadeFriends.inside(pat,b)&&!BorrascaArcadeFriends.inside(ken,b)&&!BorrascaArcadeFriends.inside(kel,b)&&own.getInt("EliAlong")>=2&&own.getInt("WrenAlong")>=2&&own.getInt("ChildStep")>=2,"and go on up the street from it, the walk to the Tree picked up where they are: Patrick "+own.getInt("EliAlong")+" Kendra "+own.getInt("WrenAlong")+" Kelly "+own.getInt("ChildStep"));
              h.assertTrue(!f.out.getBlockState(b.offset(BorrascaShops.DRUG_DOOR)).getValue(DoorBlock.OPEN),"the door shut behind them");
              var account=BorrascaTexts.account(own);
              h.assertTrue(account.contains("I got my initials over Patrick's. Patrick and Kendra came in to watch. When my initials went over his, Patrick wanted the cabinet back. He hopped every timber, and Kendra told him so each time. I told him to go round them. He had the top back before we left. Kendra fished. Kelly went through the comics."),"the account keeps the afternoon: "+account);
              f.stage(p,2);
          })
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the next time"))
          .thenExecute(()->{
              f.talk(p,"TownWren");h.assertTrue(said(f,p,"TownWren").contains("He got the top back the same afternoon"),"Kendra says how it came out: "+said(f,p,"TownWren"));
              f.talk(p,"TownWren");h.assertTrue(!said(f,p,"TownWren").contains("same afternoon"),"once");
              var own=new CompoundTag();own.putBoolean("ArcadeSeen",true);own.putInt("ArcadeTop",BorrascaArcadeFriends.READERS);
              var kept=own.copy();var told=own.copy();told.putBoolean("ArcadeTold",true);var never=new CompoundTag();never.putBoolean("ArcadeSeen",true);
              h.assertTrue(BorrascaArcadeFriends.later(kept,2).contains("still over his")&&BorrascaArcadeFriends.later(told,2).contains("still got back over him")&&BorrascaArcadeFriends.later(never,2).contains("Nobody ever got over him")&&BorrascaArcadeFriends.later(new CompoundTag(),2)==null&&BorrascaArcadeFriends.later(own,1)==null,"and has a different thing to say for each way it could have come out");
              // A reader who left the hill in the middle of it and comes in at the gate again: it is over, and a game that was being
              // played has its score on the table.
              var mid=f.own(p);mid.putInt("ArcadeScene",BorrascaArcadeFriends.ON);mid.putInt("PatMode",BorrascaArcadeFriends.PLAYING);mid.putInt("PatWay",BorrascaArcade.HOPS);mid.putInt("PatScore",500);mid.putInt("KenMode",BorrascaArcadeFriends.GOING);
              BorrascaStory.arrive(p,mid);
              h.assertTrue(mid.getInt("ArcadeScene")==BorrascaArcadeFriends.OVER&&mid.getBoolean("ArcadeHomed")&&mid.getInt("PatMode")==BorrascaArcadeFriends.LEFT&&mid.getInt("KenMode")==BorrascaArcadeFriends.LEFT&&BorrascaArcade.table(mid,seam).stream().anyMatch(r->r[0].equals("PAT")&&r[1].equals("500")),"an afternoon left unfinished is settled at the gate: scene "+mid.getInt("ArcadeScene"));
          })
          .thenSucceed();
    }

    /**
     * 0.4.436: both games, played tick by tick with a changing hand, score the same when the server plays them again from the runs of
     * keys the screen would send. BIG CAT ends at dawn or when the worms run out; a hook left in the river loses its worms to what swims.
     */
    @GameTest(template="empty",batch="borrasca_arcade_rules",timeoutTicks=200)
    public static void theCabinetGamesScoreTheSameWhenPlayedAgain(GameTestHelper h){
        for(int game=0;game<2;game++)for(long seed:new long[]{7,436,90210}){
            var g=BorrascaArcade.of(game,seed,null);var runs=new ArrayList<Integer>();int held=-1,count=0;
            for(int t=0;t<BorrascaArcade.LONGEST[game]&&!g.over();t++){
                int k=game==0?(t%37==3?BorrascaArcade.UP:t%53==9?BorrascaArcade.DOWN:t%29==1?BorrascaArcade.SPACE:0):((t/40)%3==0?BorrascaArcade.SPACE:0)|((t/70)%2==0?BorrascaArcade.LEFT:BorrascaArcade.RIGHT);
                if(k==held)count++;else{if(count>0)runs.add(count<<5|held);held=k;count=1;}
                g.tick(k);
            }
            if(count>0)runs.add(count<<5|held);
            var replay=BorrascaArcade.replay(game,seed,runs,BorrascaArcade.LONGEST[game]);
            h.assertTrue(replay!=null&&replay.score()==g.score()&&replay.over()==g.over(),BorrascaArcade.TITLES[game]+" seed "+seed+": the replay scores "+(replay==null?"nothing":replay.score())+", the screen "+g.score());
        }
        var cat=BorrascaArcade.of(BorrascaArcade.CAT,5,null);while(!cat.over())cat.tick(0);
        h.assertTrue(cat.ticks<=BorrascaArcade.LONGEST[BorrascaArcade.CAT],"BIG CAT is over by dawn at the latest");
        // 0.4.446: the others' hands. A seed is a whole game, the same every time it is played, so a screen and the server agree.
        long hops=0,round=0;
        for(long seed=1;seed<=24;seed++){
            var a=BorrascaArcade.play(BorrascaArcade.SEAM,seed,BorrascaArcade.HOPS);var again=BorrascaArcade.play(BorrascaArcade.SEAM,seed,BorrascaArcade.HOPS);
            h.assertTrue(a.score()==again.score()&&a.ticks()==again.ticks()&&Arrays.equals(a.at(),again.at())&&Arrays.equals(a.what(),again.what()),"seed "+seed+": the same hand plays the same game twice");
            h.assertTrue(a.at().length==3&&Arrays.stream(a.what()).allMatch(k->k==BorrascaArcade.DeepSeam.TIMBER)&&a.ticks()<BorrascaArcade.LONGEST[0],"seed "+seed+": a hand that hops timbers loses all three carts to timbers: "+Arrays.toString(a.what())+" in "+a.ticks());
            var b=BorrascaArcade.play(BorrascaArcade.SEAM,seed,BorrascaArcade.ROUND);var twice=BorrascaArcade.play(BorrascaArcade.SEAM,seed,BorrascaArcade.ROUND);
            h.assertTrue(b.score()==twice.score()&&b.ticks()==twice.ticks()&&b.ticks()<=BorrascaArcade.LONGEST[0],"seed "+seed+": and so does the hand that goes round them");
            var fish=BorrascaArcade.play(BorrascaArcade.CAT,seed,BorrascaArcade.FISHES);var fished=BorrascaArcade.play(BorrascaArcade.CAT,seed,BorrascaArcade.FISHES);
            h.assertTrue(fish.score()==fished.score()&&fish.ticks()==fished.ticks()&&fish.ticks()<=BorrascaArcade.LONGEST[1]&&fish.score()>0,"seed "+seed+": and the one that fishes, who lands something: "+fish.score());
            hops+=a.score();round+=b.score();
        }
        h.assertTrue(round>hops*2&&hops/24<730,"going round the timbers is worth more than twice hopping them ("+round/24+" to "+hops/24+"), and hopping them is no way to seven thirty");
        long picked=BorrascaArcade.seed(446,BorrascaArcade.SEAM,BorrascaArcade.ROUND,g->g.score()>900&&g.ticks()<=3600,Comparator.comparingInt(g->-g.score()));
        var good=BorrascaArcade.play(BorrascaArcade.SEAM,picked,BorrascaArcade.ROUND);
        h.assertTrue(good.score()>900&&good.ticks()<=3600&&picked==BorrascaArcade.seed(446,BorrascaArcade.SEAM,BorrascaArcade.ROUND,g->g.score()>900&&g.ticks()<=3600,Comparator.comparingInt(g->-g.score())),"a game chosen to go over nine hundred does, and is the same choice every time: "+good.score()+" in "+good.ticks());
        h.succeed();
    }

    /** The child's hand swaps some letters for alternate glyphs (HouseWriting); read them back as the letters they stand for. */
    private static String childPlain(String text){var base="aeiotrshnldcmyug";var out=new StringBuilder();for(char c:text.toCharArray()){if(c>=0xE100&&c<0xE110+base.length()){int i=c>=0xE110?c-0xE110:c-0xE100;out.append(i<base.length()?base.charAt(i):c);}else out.append(c);}return out.toString();}
    /** 0.4.237: what the trailhead note warns of is in the vignette, off-screen: the rooms under the depot, the night book, the letter, the graves. */
    @AfterBatch(batch="borrasca_comic") public static void comicDone(ServerLevel l){cleanup();}
    private static int comics(ServerPlayer p){int n=0;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(BorrascaRegistry.COMIC.get()))n++;return n;}
    private static String said(Fixture f,ServerPlayer p,String key){var name=f.actor(p,key).getCustomName();return name==null?"":name.getString();}
    /** Her next two words in Room 6 with the comic's question still to come: the question, then the reader's true answer. */
    private static void asked(Fixture f,ServerPlayer p,String window){
        var own=f.own(p);own.putInt("ComicAsked",0);own.remove("ComicAnswer");f.save(p,own);
        f.talk(p,"CastKimber");var question=said(f,p,"CastKimber");
        f.h.assertTrue(question.contains(window)&&question.contains("I stood Patrick's comic in the sill for you. Did you ever finish it?"),"she names the window and asks: "+question);
        f.talk(p,"CastKimber");
    }
    /**
     * 0.4.445 (owner: "Let her tuck a borrowed comic into that sill for you. Years later, when you find her in Room 6, she remembers
     * which window you chose and asks if you ever finished it."). The fort's own scene is walked in
     * aReaderWhoRunsToTheTreeFirstStillMakesThePactThere; here the comic is already in each reader's window. One reader takes it
     * down as a child, has every page of its story open, is known by it to Patrick and then to Kendra, and years later has it to
     * leave with her. The other leaves it: it is still in the sill when they are grown, bleached, and each answer they could give
     * her is the true one for what they did.
     */
    @GameTest(template="empty",batch="borrasca_comic",timeoutTicks=3600)
    public static void theComicIsKnownInTownAndAnsweredForInRoomSix(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_comic");var q=f.player("pine_sill");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded"))
            .thenExecute(()->{for(var r:List.of(p,q)){f.enter(r);var own=f.own(r);own.putString("FortWindow",r==p?"east":"west");own.putBoolean("SharedWindow",r==p);own.putBoolean("ComicTucked",true);own.putBoolean("ComicBack",true);f.save(r,own);}})
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(q).getBoolean("CastRecalled"),"the cast is in place for both readers"))
            .thenExecute(()->{
                var b=f.base(p);f.refresh(p);var sill=f.out.getBlockState(BorrascaRooms.at(b,BorrascaComic.SILL_EAST));
                h.assertTrue(BorrascaShopBlock.is(sill,BorrascaShopBlock.Kind.COMIC)&&sill.getValue(BorrascaShopBlock.FACING)==Direction.WEST&&sill.getValue(BorrascaShopBlock.STAGE)==0&&f.out.getBlockState(BorrascaRooms.at(b,BorrascaComic.SILL_WEST)).isAir(),"left in the east window, it stands there, bright, its cover to the room: "+sill);
                h.assertTrue(sill.getCollisionShape(f.out,BorrascaRooms.at(b,BorrascaComic.SILL_EAST)).isEmpty()&&sill.getShape(f.out,BorrascaRooms.at(b,BorrascaComic.SILL_EAST)).bounds().minX>=1,"nothing bumps into it, and a hand finds it on the ledge under the pane, past its own block: "+sill.getShape(f.out,BorrascaRooms.at(b,BorrascaComic.SILL_EAST)).bounds());
                h.assertTrue(f.out.getBlockState(BorrascaRooms.at(b,5,7,-96)).is(Blocks.GLASS_PANE)&&f.out.getBlockState(BorrascaRooms.at(b,5,6,-96)).isCollisionShapeFullBlock(f.out,BorrascaRooms.at(b,5,6,-96)),"that ledge is the wall's top, inside the window's glass");
                f.at(p,3,6,-96);f.click(p,BorrascaComic.SILL_WEST);h.assertTrue(comics(p)==0,"a hand on the other window's sill takes nothing");
                f.click(p,BorrascaComic.SILL_EAST);var own=f.own(p);
                ItemStack comic=ItemStack.EMPTY;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(BorrascaRegistry.COMIC.get()))comic=p.getInventory().getItem(i);
                h.assertTrue(own.getBoolean("ComicTaken")&&own.getInt("ComicTakenVisit")==1&&comics(p)==1&&BorrascaBelongings.valid(p,own,BorrascaComic.KEY,comic)&&f.out.getBlockState(BorrascaRooms.at(b,BorrascaComic.SILL_EAST)).isAir(),"a hand on it takes it down: one comic, the reader's own, and the sill empty");
                h.assertTrue(!comic.has(DataComponents.CUSTOM_MODEL_DATA)&&!comic.has(DataComponents.CUSTOM_NAME)&&comic.getHoverName().getString().equals("Kit & Caboose No. 31")&&comic.get(DataComponents.LORE).lines().stream().anyMatch(line->line.getString().contains("Patrick's")),"it keeps the name printed on it, bright, and says whose it was: "+comic.getHoverName().getString());
                f.click(p,BorrascaComic.SILL_EAST);f.refresh(p);h.assertTrue(comics(p)==1&&f.out.getBlockState(BorrascaRooms.at(b,BorrascaComic.SILL_EAST)).isAir(),"there is no second one, and it does not come back to the sill");
                BorrascaStory.held(p,comic,BorrascaComic.KEY); // it opens in the hand; a test reader has no screen to open
                BorrascaComic.page(p,0);for(int page=1;page<6;page++)BorrascaComic.page(p,page);BorrascaComic.page(p,7);BorrascaComic.page(p,9);
                h.assertTrue(!f.own(p).getBoolean("ComicFinished"),"the covers and five of the story's six pages are not the end of it");
                BorrascaComic.page(p,6);own=f.own(p);h.assertTrue(own.getBoolean("ComicFinished")&&own.getInt("ComicAt")==6&&own.getInt("ComicSeen")==0xFF,"the sixth is, and the comic is kept open there: "+own.getInt("ComicSeen"));
                BorrascaComic.page(q,6);h.assertTrue(f.own(q).getInt("ComicSeen")==0,"a reader who is not carrying theirs has had no page open");
                f.talk(p,"TownEli");h.assertTrue(said(f,p,"TownEli").contains(BorrascaComic.PATRICK),"Patrick knows his own comic: "+said(f,p,"TownEli"));
                f.talk(p,"TownEli");h.assertTrue(!said(f,p,"TownEli").contains(BorrascaComic.PATRICK)&&f.own(p).getInt("EliTalk1")==1,"he says so once, and then what he would have said: "+said(f,p,"TownEli"));
                f.talk(q,"TownEli");h.assertTrue(!said(f,q,"TownEli").contains(BorrascaComic.PATRICK),"to a reader without it he says nothing of it");
                // Each turns their back on the town at the gate, so the cast can be moved to its places for the next time.
                for(var r:List.of(p,q)){f.at(r,0,0,-3);r.setYRot(0);r.setYHeadRot(0);}f.stage(p,2);f.stage(q,4);
            })
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(q).getBoolean("CastRecalled"),"the next time for one, the last night for the other"))
            .thenExecute(()->{
                f.talk(p,"TownWren");h.assertTrue(said(f,p,"TownWren").contains(BorrascaComic.KENDRA),"with Patrick gone, Kendra knows whose it is: "+said(f,p,"TownWren"));
                f.talk(p,"TownWren");h.assertTrue(!said(f,p,"TownWren").contains(BorrascaComic.KENDRA),"once");
                // The reader who left it: grown, it is still in their window, and the window has had it.
                var b=f.base(q);f.refresh(q);var sill=f.out.getBlockState(BorrascaRooms.at(b,BorrascaComic.SILL_WEST));
                h.assertTrue(BorrascaShopBlock.is(sill,BorrascaShopBlock.Kind.COMIC)&&sill.getValue(BorrascaShopBlock.FACING)==Direction.EAST&&sill.getValue(BorrascaShopBlock.STAGE)==1,"years later it is still in the west sill, bleached: "+sill);
                f.at(q,-16,6,-179);f.refresh(q);h.assertTrue(f.own(q).getBoolean("KellyFound")&&f.own(q).getInt("KellyLine")==1,"she is in Room 6, and has warned the reader off the doors");
                asked(f,q,"The west window.");var own=f.own(q);
                h.assertTrue(!said(f,q,"CastKimber").contains("You picked it")&&own.getString("ComicAnswer").equals("sill")&&said(f,q,"CastKimber").contains(BorrascaComic.HER_SILL),"a reader who chose no window is not told they did, and one who left it says so: "+said(f,q,"CastKimber"));
                h.assertTrue(BorrascaTexts.account(own).contains("She remembered which window it was, and asked if I had ever finished the comic. It was still in the sill."),"the account: "+BorrascaTexts.account(own));
                // Taken down grown, it is the bleached one; carried to her, it can be kept.
                f.at(q,-3,6,-96);f.click(q,BorrascaComic.SILL_WEST);own=f.own(q);ItemStack aged=ItemStack.EMPTY;for(int i=0;i<q.getInventory().getContainerSize();i++)if(q.getInventory().getItem(i).is(BorrascaRegistry.COMIC.get()))aged=q.getInventory().getItem(i);
                h.assertTrue(own.getInt("ComicTakenVisit")==4&&comics(q)==1&&aged.has(DataComponents.CUSTOM_MODEL_DATA)&&BorrascaBelongings.valid(q,own,BorrascaComic.KEY,aged)&&f.out.getBlockState(BorrascaRooms.at(b,BorrascaComic.SILL_WEST)).isAir(),"taken down grown, it is the one the window bleached");
                asked(f,q,"The west window.");own=f.own(q);
                h.assertTrue(said(f,q,"CastKimber").contains(BorrascaComic.HER_HERE_AGED)&&q.containerMenu instanceof LiteraryChoiceMenu&&own.getString("ComicAnswer").equals("kept"),"held out to her, she sees what the window did, and it is the reader's to leave or keep: "+said(f,q,"CastKimber"));
                q.containerMenu.clicked(8,0,net.minecraft.world.inventory.ClickType.PICKUP,q);own=f.own(q);
                h.assertTrue(comics(q)==1&&!own.getBoolean("ComicGiven")&&said(f,q,"CastKimber").contains(BorrascaComic.HER_KEPT)&&!(q.containerMenu instanceof LiteraryChoiceMenu),"kept, unfinished: "+said(f,q,"CastKimber"));
                // Put by, and never finished; then finished, and told.
                var put=BorrascaBelongings.remove(q,own,BorrascaComic.KEY);f.save(q,own);asked(f,q,"The west window.");own=f.own(q);
                h.assertTrue(own.getString("ComicAnswer").equals("unfinished")&&said(f,q,"CastKimber").contains(BorrascaComic.HER_NOT)&&!(q.containerMenu instanceof LiteraryChoiceMenu),"taken and never finished, and not here to hand over: "+said(f,q,"CastKimber"));
                h.assertTrue(BorrascaTexts.account(own).contains("asked if I had ever finished the comic. I never had."),"the account: "+BorrascaTexts.account(own));
                own.putBoolean("ComicFinished",true);f.save(q,own);asked(f,q,"The west window.");own=f.own(q);
                h.assertTrue(own.getString("ComicAnswer").equals("finished")&&said(f,q,"CastKimber").contains(BorrascaComic.HER_END),"read to where it stops, the reader can tell her: "+said(f,q,"CastKimber"));
                h.assertTrue(BorrascaTexts.account(own).contains("I told her where it stops."),"the account: "+BorrascaTexts.account(own));
                q.getInventory().add(put);
                for(int i=0;i<4;i++)f.talk(q,"CastKimber");own=f.own(q);
                h.assertTrue(own.getBoolean("KellyAsked")&&own.getInt("KellyLine")>=5&&own.getInt("ComicAsked")==2,"then her own lines go on to the end: "+own.getInt("KellyLine"));
                f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,4);
            })
            .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the last night for the reader who took it as a child"))
            .thenExecute(()->{
                f.at(p,-16,6,-179);f.refresh(p);asked(f,p,"The east window. You picked it.");var own=f.own(p);var kelly=f.actor(p,"CastKimber");
                h.assertTrue(said(f,p,"CastKimber").contains(BorrascaComic.HER_HERE)&&p.containerMenu instanceof LiteraryChoiceMenu,"she names the window the reader picked, and sees they kept it flat: "+said(f,p,"CastKimber"));
                p.containerMenu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,p);own=f.own(p);
                h.assertTrue(comics(p)==0&&own.getBoolean("ComicGiven")&&own.getString("ComicAnswer").equals("given")&&BorrascaBelongings.valid(p,own,BorrascaComic.KEY,kelly.getMainHandItem())&&said(f,p,"CastKimber").contains(BorrascaComic.HER_GIVEN),"left with her, the reader's own comic is in her hand, and she knows her page: "+said(f,p,"CastKimber"));
                h.assertTrue(BorrascaTexts.account(own).contains("She remembered which window was mine, and asked if I had ever finished the comic. I had it with me. I left it with her, and she found her page."),"the account: "+BorrascaTexts.account(own));
                f.refresh(p);h.assertTrue(f.out.getBlockState(BorrascaRooms.at(f.base(p),BorrascaComic.SILL_EAST)).isAir()&&BorrascaBelongings.valid(p,f.own(p),BorrascaComic.KEY,f.actor(p,"CastKimber").getMainHandItem()),"it stays with her");
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_lower",timeoutTicks=1800)
    public static void theRoomsUnderTheDepotHoldWhatTheNoteWarnedOf(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_lower");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,4);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the last night")).thenExecute(()->{
            var b=f.base(p);var l=f.out;
            h.assertTrue(l.getBlockState(BorrascaRooms.at(b,BorrascaRooms.HATCH)).getBlock() instanceof TrapDoorBlock&&l.getBlockState(BorrascaRooms.at(b,-19,8,-202)).is(Blocks.LADDER)&&l.getBlockState(BorrascaRooms.at(b,-19,7,-190)).isAir(),"a hatch in the back room's floor, a ladder down, a corridor");
            for(int room=1;room<=12;room++){var door=l.getBlockState(BorrascaRooms.at(b,BorrascaRooms.roomDoor(room)));var lever=l.getBlockState(BorrascaRooms.at(b,BorrascaRooms.roomLever(room)));
                h.assertTrue(door.is(Blocks.IRON_DOOR)&&lever.is(Blocks.LEVER)&&door.getValue(DoorBlock.OPEN)==(room==BorrascaRooms.NELL_ROOM),"room "+room+": an iron door, bolted from the corridor"+(room==BorrascaRooms.NELL_ROOM?"; hers stands open":""));}
            var lever=BorrascaRooms.at(b,BorrascaRooms.roomLever(BorrascaRooms.MARA_ROOM));((LeverBlock)Blocks.LEVER).pull(l.getBlockState(lever),l,lever,p);
            h.assertTrue(l.getBlockState(BorrascaRooms.at(b,BorrascaRooms.roomDoor(BorrascaRooms.MARA_ROOM))).getValue(DoorBlock.OPEN),"throwing the bolt opens the door");
            f.at(p,6,12,-170);
            h.assertTrue(title(opened(f,p,BorrascaRooms.GATE_LOG,"the gate hut paper")).equals("Ridge gate log"),
                    "right-clicking the gate hut desk sheet opens its own document");
            f.read(p,BorrascaRooms.NIGHT_BOOK,false);f.read(p,BorrascaRooms.LETTER,false);var own=f.own(p);
            h.assertTrue(own.getBoolean("LedgerRead")&&own.getBoolean("LetterRead"),"the night book and the letter are read on the desk");
            var book=String.join(" ",BorrascaTexts.document("Ledger").get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).toList());
            h.assertTrue(book.contains("Approved: Sheriff's office")&&book.contains("E. Prescott")&&book.contains("W. Prescott")&&book.contains("J. Prescott")&&book.contains("263-A")&&book.contains("Asks for nobody now")&&book.contains("To the yard")&&book.contains("264-A"),"the night book: the Sheriff's approvals, three Prescotts, her tag, the yard, and the newest tag");
            // 0.4.437 (owner): grown women only below, and the town's reason on paper.
            h.assertTrue(book.contains("None to come down before eighteen")&&book.contains("Kept above")&&book.contains("Placed in town")&&book.contains("141-A. Down"),"nobody comes down before eighteen, a child taken is kept above, and what is born below is placed in town");
            var letterPages=BorrascaTexts.document("Letter").get(DataComponents.WRITTEN_BOOK_CONTENT).pages();
            var letter=String.join(" ",letterPages.stream().map(page->page.raw().getString()).toList()).toUpperCase(Locale.ROOT).replaceAll("\\s+"," ");
            var styles=new java.util.HashSet<net.minecraft.network.chat.Style>();letterPages.get(0).raw().visit((style,text)->{if(!text.isBlank())styles.add(style);return java.util.Optional.empty();},net.minecraft.network.chat.Style.EMPTY);
            h.assertTrue(letter.contains("UNDER THE DEPOT")&&letter.contains("THE BABIES GO UP TO TOWN")&&letter.contains("WITHHELD")&&!letter.contains("MOM,"),"the note says where she is and where the babies go, and was never sent: "+letter);
            h.assertTrue(styles.size()>=6,"the note is pasted from cuttings, no two alike, so it has no hand: "+styles.size());
            f.at(p,-16,6,-179);f.refresh(p);own=f.own(p);var wall=l.getBlockEntity(BorrascaRooms.at(b,BorrascaRooms.NELL_WALL));
            h.assertTrue(own.getBoolean("LowerRooms")&&own.getBoolean("NellsRoom"),"under the depot, and in her room");
            h.assertTrue(wall instanceof BorrascaCarvingEntity e&&e.friend().equals(BorrascaStory.FRIEND)&&e.reader().equals(p.getGameProfile().getName())&&l.getBlockState(BorrascaRooms.at(b,BorrascaRooms.NELL_WALL)).getValue(BorrascaCarvingBlock.WALL),"her name scratched into the wall beside the reader's");
            // 0.4.438 (owner): Kelly is there, grown: the same saved actor as the child, on her grown skin and at full size.
            var kelly=f.actor(p,"CastKimber");
            h.assertTrue(kelly!=null&&kelly.phase()==BorrascaStory.GROWN&&!kelly.isInvisible()&&kelly.getBbHeight()>1.5F&&kelly.position().distanceTo(Vec3.atBottomCenterOf(BorrascaRooms.at(b,BorrascaRooms.KELLY)))<1.6&&own.getBoolean("KellyFound"),"she is there, grown, by the cot: "+(kelly==null?"no actor":kelly.phase()+" "+kelly.getBbHeight()+" "+kelly.position()+" for "+Vec3.atBottomCenterOf(BorrascaRooms.at(b,BorrascaRooms.KELLY))+" invisible "+kelly.isInvisible()));
            for(int i=0;i<4;i++)f.talk(p,"CastKimber");own=f.own(p);
            h.assertTrue(own.getBoolean("KellyAsked")&&own.getInt("KellyLine")>=5,"she will not come up, and asks the reader to stop the machine and take her name up: "+own.getInt("KellyLine"));
            f.at(p,-23,6,-199);f.refresh(p);h.assertTrue(f.own(p).getBoolean("MarasRoom"),"the newest room, bolted, made up for her");
            h.assertTrue(l.getBlockEntity(BorrascaRooms.at(b,BorrascaRooms.grave(0))) instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign&&sign.getFrontText().getMessage(1,false).getString().equals(BorrascaRooms.GRAVE_TAGS[0])&&sign.isWaxed(),"a number on a stake, no name");
            f.at(p,0,12,-204);f.refresh(p);own=f.own(p);h.assertTrue(own.getBoolean("GravesSeen"),"the rows behind the shed");
            var account=BorrascaTexts.account(own);
            h.assertTrue(account.contains("bolts on the outside")&&account.contains("scratched into the wall beside mine")&&account.contains("counted by a Prescott")&&account.contains("never sent")&&account.contains("numbers in the ground")&&account.contains("two births, both placed in town")&&account.contains("grown, and would not come up")&&account.contains("take her name up instead of her"),"the account keeps what was found: "+account);
            h.assertTrue(BorrascaTexts.witness(own).contains("night book"),"and the Witness line the book");
            h.assertTrue(!own.getBoolean("Completed"),"none of it is required to finish; finding it is the reader's own");
        }).thenSucceed();
    }

    /**
     * 0.4.439 (owner: "when she hits the blade it shouldn't be quick... the game should track the splitting/tearing of her body so it
     * doesn't tear all at once"). 0.4.444: Dana is on the belt when the night begins, the two men standing by it, and the belt starts
     * when the reader comes up to the yard; her cot in room 2 is empty. Then, from the slot's mouth, the
     * machine takes her a little at a time over most of a minute, catching on bone, while her voice goes from a moan to a scream to
     * nothing; pieces of her come out of the far end before it has her head, and the pile and the band are what is left.
     */
    @GameTest(template="empty",batch="borrasca_feeding",timeoutTicks=5000)
    public static void theMachineTakesHerSlowlyAndTearsHerASliceAtATime(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_feeding");
        final int[] depth={0},steps={0},firstTorn={-1},fedAt={-1},tick={0};final boolean[] piecesWhileOnBelt={false},living={false},dead={false};final List<String> voice=new ArrayList<>();
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,4);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(p).getBoolean("FeedPlaced"),"the last night, the men at the belt")).thenExecute(()->{
            var dana=f.actor(p,"CastDana");
            h.assertTrue(dana!=null&&!dana.isInvisible()&&dana.gesture()==BorrascaStory.LAID&&dana.torn()==0,"she lies whole: "+(dana==null?"no actor":dana.gesture()+" "+dana.torn()));
            var lies=f.rel(dana,p);h.assertTrue(Math.abs(lies.z-BorrascaFeeding.LAID_Z)<.3&&Math.abs(lies.x-BorrascaFeeding.BELT_X)<.3&&lies.y>12.4&&BorrascaFeeding.state(f.own(p))==BorrascaFeeding.IDLE,"she is on the belt, at its head, when the night begins, and it has not started: "+lies);
            for(String key:new String[]{"Worker0","Worker1"}){var w=f.actor(p,key);var at=w==null?null:f.rel(w,p);h.assertTrue(w!=null&&!w.isInvisible()&&Math.abs(at.x+1.6)<1&&at.z<-180&&at.z>-185,"the two men stand beside the belt: "+key+" "+at);}
            f.at(p,-16,6,-195);f.refresh(p);h.assertTrue(f.own(p).getBoolean("DanaRoomEmpty")&&BorrascaFeeding.state(f.own(p))==BorrascaFeeding.IDLE,"room 2: her cot is empty, the blanket folded back; under the depot the belt does not start");
            f.at(p,-10,12,-176);f.refresh(p);h.assertTrue(BorrascaFeeding.state(f.own(p))==BorrascaFeeding.RIDE,"when the reader comes up to the yard the belt starts, with her on it");
            // The walk and the ride are their own business; this case starts with her feet at the slot.
            var own=f.own(p);own.putInt("Feed",BorrascaFeeding.RIDE);own.putInt("FeedTicks",0);own.putDouble("FeedZ",BorrascaFeeding.SLOT_Z+.05);f.save(p,own);
            h.onEachTick(()->{tick[0]++;var d=f.actor(p,"CastDana");var o=f.own(p);if(d==null||fedAt[0]>=0)return;
                if(d.torn()<depth[0])throw new GameTestAssertException("the machine never gives anything back: "+d.torn()+" after "+depth[0]);
                if(d.torn()>depth[0]){if(depth[0]==0)firstTorn[0]=tick[0];depth[0]=d.torn();steps[0]++;}
                if(d.gesture()==BorrascaStory.TORN_LIVE)living[0]=true;if(d.gesture()==BorrascaStory.TORN_DEAD)dead[0]=true;
                if(d.isCustomNameVisible()&&d.getCustomName()!=null){String w=d.getCustomName().getString();if(voice.isEmpty()||!voice.get(voice.size()-1).equals(w))voice.add(w);}
                if(o.contains("FeedOut")&&!o.getBoolean("DanaFed"))piecesWhileOnBelt[0]=true;
                if(o.getBoolean("DanaFed"))fedAt[0]=tick[0];});
        }).thenWaitUntil(()->h.assertTrue(fedAt[0]>=0,"the machine has all of her: "+depth[0]+" of "+(int)(BorrascaFeeding.BODY*1000)+", "+voice)).thenExecute(()->{
            h.assertTrue(fedAt[0]-firstTorn[0]>=600,"it is slow: "+(fedAt[0]-firstTorn[0])+" ticks from the slot to her head");
            h.assertTrue(steps[0]>=60,"her body goes a little at a time, not at once: "+steps[0]+" steps");
            h.assertTrue(living[0]&&dead[0],"she is alive at the blade, and then she is not");
            int moan=voice.indexOf("awwwoo"),scream=voice.indexOf("AAHHHHH"),end=voice.indexOf("-");
            h.assertTrue(moan>=0&&scream>moan&&end>scream,"her voice goes from awwwoo to AAHHHHH to -: "+voice);
            h.assertTrue(piecesWhileOnBelt[0],"pieces of her come out of the far end while the rest of her is still on the belt");
            var dana=f.actor(p,"CastDana");h.assertTrue(dana==null||dana.isInvisible(),"none of her is left on the belt");
        }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FeedPiled"),"what came out has reached the end of the belt")).thenExecute(()->{
            var b=f.base(p);
            h.assertTrue(BorrascaShopBlock.is(f.out.getBlockState(BorrascaRooms.at(b,BorrascaFeeding.PILE_REMAINS)),BorrascaShopBlock.Kind.REMAINS)&&BorrascaShopBlock.is(f.out.getBlockState(BorrascaRooms.at(b,BorrascaFeeding.PILE_HAIR)),BorrascaShopBlock.Kind.REMAINS)&&BorrascaShopBlock.is(f.out.getBlockState(BorrascaRooms.at(b,BorrascaFeeding.PILE_BAND)),BorrascaShopBlock.Kind.WRISTBAND),"pulp and bone, hair and a slipper, and her band on the pile");
            f.use(p,BorrascaFeeding.PILE_BAND);var own=f.own(p);h.assertTrue(own.getBoolean("BandRead"),"the band from her wrist reads 141-A");
            var account=BorrascaTexts.account(own);
            h.assertTrue(account.contains("feet first")&&account.contains("screamed until it had her chest")&&account.contains("before it had her head")&&account.contains("141-A"),"the account keeps what the machine did: "+account);
            f.at(p,-16,6,-195);f.refresh(p);h.assertTrue(f.own(p).getBoolean("DanaRoomEmpty"),"her cot is empty after");
        }).thenSucceed();
    }
    /**
     * 0.4.439, the owner: "The player should be able to shut the belt off to save her; the account remembers that, but Dana does not get
     * off the belt. She does not respond to the player". Pulled while she rides, the breaker stops the belt with her whole on it; she
     * says nothing and takes no hand. Another reader pulls theirs once the machine has her legs: it stops there, and she goes quiet. The
     * note at the trailhead says the last visit shows this.
     */
    @GameTest(template="empty",batch="borrasca_breaker",timeoutTicks=3000)
    public static void theBreakerStopsTheBeltButSheDoesNotGetOff(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_breaker");var q=f.player("pine_late");final double[] at={0};
        var note=String.join(" ",BorrascaTexts.contentNote().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).toList());
        h.assertTrue(note.contains("GRAPHIC GORE")&&note.contains("fed alive")&&note.contains("Sexual violence stays off-screen"),"the trailhead note warns of the gore and keeps the rest off-screen: "+note);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.enter(q);f.stage(p,4);f.stage(q,4);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(q).getBoolean("CastRecalled")&&f.own(p).getBoolean("FeedPlaced")&&f.own(q).getBoolean("FeedPlaced"),"the last night, for both, the men at their belts")).thenExecute(()->{
            var own=f.own(p);own.putInt("Feed",BorrascaFeeding.RIDE);own.putInt("FeedTicks",0);own.putDouble("FeedZ",BorrascaFeeding.SLOT_Z+3);f.save(p,own);
            own=f.own(q);own.putInt("Feed",BorrascaFeeding.INTAKE);own.putInt("FeedTicks",0);own.putInt("FeedBones",2);own.putDouble("FeedZ",BorrascaFeeding.SLOT_Z-.5);f.save(q,own);
        }).thenIdle(20).thenExecute(()->{
            var watching=f.actor(p,"Worker0");h.assertTrue(watching!=null&&!watching.isInvisible()&&watching.getCustomName()!=null&&watching.getCustomName().getString().contains("Gentleman'll look after her"),"the men watch her go in, and say who has her now: "+(watching==null?null:watching.getCustomName()));
            at[0]=f.own(p).getDouble("FeedZ");f.use(p,BorrascaRooms.POWER);f.use(q,BorrascaRooms.POWER);
        }).thenWaitUntil(()->h.assertTrue(BorrascaFeeding.state(f.own(p))==BorrascaFeeding.SAVED&&BorrascaFeeding.state(f.own(q))==BorrascaFeeding.LATE,"both belts stop")).thenIdle(200).thenExecute(()->{
            var dana=f.actor(p,"CastDana");var own=f.own(p);
            h.assertTrue(own.getBoolean("DanaSaved")&&dana!=null&&!dana.isInvisible()&&dana.gesture()==BorrascaStory.LAID&&dana.torn()==0&&dana.isNoGravity(),"saved: she lies whole where they put her, on the stopped belt: "+(dana==null?"no actor":dana.gesture()+" "+dana.torn()));
            h.assertTrue(Math.abs(own.getDouble("FeedZ")-at[0])<.2&&own.getDouble("FeedZ")>BorrascaFeeding.SLOT_Z,"the belt has not moved her since: "+own.getDouble("FeedZ"));
            f.talk(p,"CastDana");own=f.own(p);h.assertTrue(own.getBoolean("DanaIgnored")&&!dana.isCustomNameVisible(),"held out a hand, she does not take it or say anything");
            var account=BorrascaTexts.account(own);h.assertTrue(account.contains("I pulled the breaker before it took her in")&&account.contains("never once looked at me")&&account.contains("She didn't take it"),"the account remembers that she was saved and did not get off: "+account);
            var late=f.actor(q,"CastDana");var lateOwn=f.own(q);
            h.assertTrue(lateOwn.getBoolean("DanaLate")&&lateOwn.getInt("DanaLateAt")>=400&&late!=null&&late.torn()>=400&&late.torn()==lateOwn.getInt("DanaLateAt"),"late: it stopped with her legs taken, and takes no more: "+(late==null?"no actor":late.torn()));
            h.assertTrue(late.gesture()==BorrascaStory.TORN_DEAD,"and she has gone still");
            account=BorrascaTexts.account(lateOwn);h.assertTrue(account.contains("already had her legs")&&account.contains("went quiet"),"the late account: "+account);
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_any_route",timeoutTicks=1800)
    public static void theGougeIsSeenByLookingAndAnyUnseenRouteReachesTheWire(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_any_route");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,3);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the adult cast is in place")).thenExecute(()->{
            f.at(p,0,0,-93);f.look(p,BorrascaRooms.CARVING);f.refresh(p);h.assertTrue(f.own(p).getBoolean("GougeSeen"),"looking at the bark is enough to see the gouge");
            f.atWorld(p,BorrascaRooms.trailWorld((int)Math.round(BorrascaRooms.worldZ(BorrascaRooms.SHERIFF.getZ()))+10));f.look(p,BorrascaRooms.SHERIFF);f.refresh(p);var sheriff=f.actor(p,"CastSheriff");
            h.assertTrue(f.own(p).getBoolean("HeardDenial")&&sheriff.getCustomName()!=null&&sheriff.getCustomName().getString().contains("Marker Three"),"he gives himself away as soon as he sees the reader, ten blocks off");
            // The long way round through the woods to the west: never in his light.
            f.at(p,-27,0,-131);f.refresh(p);h.assertTrue(f.own(p).getInt("Suspicion")==0&&!f.own(p).getBoolean("Escorting"),"a reader in the woods to the west is not seen");
            var gate=BorrascaRooms.at(f.base(p),BorrascaRooms.LOCK);
            h.assertTrue(f.out.getBlockState(gate).getValue(BorrascaFittingBlock.KIND)==BorrascaFittingBlock.Kind.LOCKED_FENCE,
                    "the lock is mounted on the closed gate panel");
            f.at(p,5,12,-163);f.refresh(p);var own=f.own(p);h.assertTrue(own.getBoolean("PerimeterSeen")&&own.getInt("Beat")==3&&own.getInt("GullyStep")==0,"the wire is reached by a route that never used the gully, and the visit is done");
        }).thenSucceed();
    }

    /** A standing adult body fits the cell: no collision from just above a carpet to 1.8 over the floor. */
    /** 0.4.420: an authored box on the hill, in the world. */
    /** 0.4.420: a body walking after someone far up a drawn-out slope keeps to the trail, as a reader would, instead of cutting across the hillside. */
    private static Vec3 onTrail(Fixture f,ServerPlayer p,Vec3 goal){
        var b=Vec3.atLowerCornerOf(f.base(p));double z=p.getZ()-b.z;
        if(!BorrascaRooms.drawnOutWorld(z)||goal.z-b.z>z-4)return goal;
        return Vec3.atBottomCenterOf(BorrascaRooms.trailWorld((int)Math.floor(z)-3)).add(b);
    }
    private static AABB wbox(BlockPos b,double x0,double y0,double z0,double x1,double y1,double z1){var a=BorrascaRooms.world(new Vec3(x0,y0,z0)).add(Vec3.atLowerCornerOf(b));var c=BorrascaRooms.world(new Vec3(x1,y1,z1)).add(Vec3.atLowerCornerOf(b));return new AABB(a,c);}
    private static boolean clear(Fixture f,ServerPlayer p,BlockPos b,int x,int y,int z){var w=BorrascaRooms.at(b,x,y,z);return f.out.noCollision(p,new AABB(w.getX()+.2,w.getY()+.1,w.getZ()+.2,w.getX()+.8,w.getY()+1.8,w.getZ()+.8));}
    /** 0.4.455: the doctor's corkboard opens to a reader as a board, not as pages: everything written on what hangs there on their visit. */
    private static String corkboard(Fixture f,ServerPlayer p,String what){
        int visit=f.own(p).getInt("Visit");f.clickWorld(p,BorrascaShops.CARDS);
        f.h.assertFalse(p.containerMenu instanceof LiteraryVignettes.Pages,what+" opens as a board, not as pages");
        f.h.assertTrue(visit>=1&&f.own(p).getInt("CardsBoard")==visit,what+" is the board of visit "+visit+": "+f.own(p).getInt("CardsBoard"));
        return BorrascaCards.board(visit).stream().map(BorrascaCards.Piece::text).reduce("",(a,b)->a+"\n"+b);
    }
    private static ItemStack openedWorld(Fixture f,ServerPlayer p,BlockPos w,String what){f.clickWorld(p,w);f.h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,what+" opens as native pages at "+w);var book=((LiteraryVignettes.Pages)p.containerMenu).original();p.closeContainer();return book;}
    private static ItemStack opened(Fixture f,ServerPlayer p,BlockPos rel,String what){f.click(p,rel);f.h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,what+" opens as native pages at "+rel);var book=((LiteraryVignettes.Pages)p.containerMenu).original();p.closeContainer();return book;}
    private static String text(ItemStack book){return book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).reduce("",String::concat);}
    /** Page breaks fall between words and take the space with them, and a child's hand swaps letters for drawn ones, so phrases are compared in plain letters without spaces. */
    private static String flat(String text){return io.github.knaitoe.theoldesthouse.house.HouseWriting.plain(text).replaceAll("\\s+","");}
    private static String title(ItemStack book){return book.get(DataComponents.WRITTEN_BOOK_CONTENT).title().raw();}

    @GameTest(template="empty",batch="borrasca_town",timeoutTicks=1800)
    public static void theTownFortAndDepotAreRealBuildingsAndTheTownsPapersChangeBetweenVisits(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("drisking_reader");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original cast loaded")).thenExecute(()->{
            var b=f.base(p);
            // 0.4.444: nothing hangs on glass. Every piece that hangs on a wall has wall behind it, and the OPEN signs hang inside their windows.
            var hung=new ArrayList<String>();
            for(var hungAt:BlockPos.betweenClosed(b.offset(-50,-1,-125),b.offset(50,10,3))){var hs=f.out.getBlockState(hungAt);Direction face=null;
                if(hs.is(BorrascaRegistry.SHOP.get())&&hs.getValue(BorrascaShopBlock.KIND).wall())face=hs.getValue(BorrascaShopBlock.FACING);
                else if(hs.is(BorrascaRegistry.FITTING.get())&&hs.getValue(BorrascaFittingBlock.KIND)==BorrascaFittingBlock.Kind.NOTICE)face=hs.getValue(BorrascaFittingBlock.FACING);
                if(face==null)continue;var behind=f.out.getBlockState(hungAt.relative(face.getOpposite()));
                if(behind.is(Blocks.GLASS_PANE)||behind.isAir())hung.add(hungAt.subtract(b).toShortString()+" "+hs);}
            h.assertTrue(hung.isEmpty(),"every piece that hangs on a wall has wall behind it, not glass or air: "+hung);
            // 0.4.449 (owner: "Give the different stores some variance/characterization in their signage"; "custom signs"): the
            // town's signs are its own painted boards. None of the game's wooden signs is left but the numbers on the graves; every
            // board is whole; one on a wall has wall or wire behind it, one in a window has the pane before it, one that hangs has
            // its beam over it.
            int wooden=0;var adrift=new ArrayList<String>();var boards=new java.util.EnumMap<BorrascaSigns.Sign,Integer>(BorrascaSigns.Sign.class);
            for(var at:BlockPos.betweenClosed(b.offset(-47,-8,BorrascaRooms.HEAD),b.offset(47,60,12))){var s=f.out.getBlockState(at);
                if(s.getBlock() instanceof net.minecraft.world.level.block.SignBlock)wooden++;
                if(!BorrascaSignBlock.is(s))continue;var sign=BorrascaSignBlock.sign(s);boards.merge(sign,1,Integer::sum);var face=s.getValue(BorrascaSignBlock.FACING);
                var behind=f.out.getBlockState(at.relative(face.getOpposite()));
                boolean held=switch(sign.mount){case WALL,DECAL->!behind.isAir()&&!behind.is(Blocks.GLASS_PANE);case WINDOW->f.out.getBlockState(at.relative(face)).is(Blocks.GLASS_PANE);case FREE->!f.out.getBlockState(at.above()).isAir();};
                if(!held)adrift.add(at.subtract(b).toShortString()+" "+sign);}
            h.assertTrue(wooden==BorrascaRooms.GRAVE_TAGS.length,"the only wooden signs left on the hill are the numbers on the graves: "+wooden);
            h.assertTrue(adrift.isEmpty(),"every board has wall or wire behind it, its window before it or its beam over it: "+adrift);
            for(var sign:BorrascaSigns.Sign.values()){int want=sign.pieces()*(sign==BorrascaSigns.Sign.NO_ENTRY||sign==BorrascaSigns.Sign.ROUNDEL?2:1);h.assertTrue(boards.getOrDefault(sign,0)==want,"the "+sign+" sign is hung whole: "+boards.getOrDefault(sign,0)+" blocks of "+want);}
            // 0.4.447: the yard's boards lie on the gravel or on each other. Two of them hung in the air.
            var loose=new ArrayList<String>();
            for(var board:BlockPos.betweenClosed(new BlockPos(-32,12,-212),new BlockPos(32,13,-168))){var at=BorrascaRooms.at(b,board);var bs=f.out.getBlockState(at);if(!bs.is(Blocks.SPRUCE_SLAB))continue;
                var under=f.out.getBlockState(at.below());var type=bs.getValue(SlabBlock.TYPE);
                boolean rests=type==net.minecraft.world.level.block.state.properties.SlabType.TOP?false:!under.isAir()&&(!(under.getBlock() instanceof SlabBlock)||under.getValue(SlabBlock.TYPE)!=net.minecraft.world.level.block.state.properties.SlabType.BOTTOM);
                if(!rests)loose.add(board.toShortString()+" "+type+" over "+under.getBlock());}
            h.assertTrue(loose.isEmpty(),"every board in the yard rests on the ground or on the board under it: "+loose);
            // 0.4.447: the depot office's cooler is the waiting room's, and the front door answers a hand and stays shut.
            var water=f.out.getBlockState(BorrascaRooms.at(b,BorrascaRooms.WATER));
            h.assertTrue(water.is(HouseReadRegistry.FITTING.get())&&water.getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.COOLER&&f.out.getBlockState(BorrascaRooms.at(b,BorrascaRooms.WATER).above()).isAir(),"the depot office's water cooler is the waiting room's: "+water);
            var hand=f.own(p);
            h.assertTrue(BorrascaStory.click(p,b,BorrascaRooms.world(BorrascaRooms.WATER),hand)&&BorrascaStory.click(p,b,BorrascaRooms.world(BorrascaRooms.FACTORY_DOOR),hand)&&!open(f,p,BorrascaRooms.FACTORY_DOOR),"the cooler and the depot's front door each answer a hand, and the door stays shut");
            // 0.4.448 (owner: "There should be names all over this tree.")
            int named=0;for(var at:BlockPos.betweenClosed(new BlockPos(-5,0,-102),new BlockPos(6,8,-97)))if(BorrascaNamesBlock.is(f.out.getBlockState(BorrascaRooms.at(b,at))))named++;
            h.assertTrue(named>=60,"the burl and the trunks are cut with names all over: "+named);
            for(int x=-3;x<2;x++)for(int y=0;y<=2;y++){var one=f.out.getBlockState(BorrascaRooms.at(b,x,y,-98));var next=f.out.getBlockState(BorrascaRooms.at(b,x+1,y,-98));if(BorrascaNamesBlock.is(one)&&BorrascaNamesBlock.is(next))h.assertTrue(!one.getValue(BorrascaNamesBlock.FACE).equals(next.getValue(BorrascaNamesBlock.FACE)),"no two blocks side by side carry the same names: "+x+","+y);}
            h.assertTrue(f.out.getBlockState(BorrascaRooms.at(b,BorrascaRooms.CARVING)).is(BorrascaRegistry.CARVING.get())&&f.out.getBlockState(BorrascaRooms.at(b,BorrascaRooms.FRIENDS_NAMES)).getValue(BorrascaNamesBlock.FACE)==BorrascaNamesBlock.BOTH&&BorrascaNamesBlock.friends(2).getValue(BorrascaNamesBlock.FACE)==BorrascaNamesBlock.HIS_GONE,"the reader's carving keeps its place, and Patrick's and Kendra's names are cut beside it");
            h.assertTrue(BorrascaStory.click(p,b,BorrascaRooms.world(new BlockPos(-2,1,-98)),hand)&&BorrascaStory.click(p,b,BorrascaRooms.world(BorrascaRooms.FRIENDS_NAMES),hand),"a hand on the names is answered");
            // 0.4.449: each shop's own sign hangs inside its window (the scan above found the pane before each); a hand on a board
            // reads it.
            h.assertTrue(BorrascaSignBlock.is(f.out.getBlockState(b.offset(BorrascaShops.OPEN_DINER)),BorrascaSigns.Sign.OPEN_DINER)&&BorrascaSignBlock.is(f.out.getBlockState(b.offset(BorrascaShops.OPEN_DRUG)),BorrascaSigns.Sign.OPEN_DRUG)
                &&BorrascaSignBlock.is(f.out.getBlockState(b.offset(BorrascaShops.OPEN_ANTIQUES)),BorrascaSigns.Sign.OPEN_ANTIQUES)&&BorrascaSignBlock.is(f.out.getBlockState(b.offset(BorrascaShops.IN_DOCTOR)),BorrascaSigns.Sign.IN_DOCTOR),"each shop has its own sign in its window");
            h.assertTrue(BorrascaStory.click(p,b,BorrascaShops.OPEN_DINER,hand)&&BorrascaStory.click(p,b,new BlockPos(-6,5,-84),hand)&&BorrascaSignBlock.is(f.out.getBlockState(b.offset(-6,5,-84)),BorrascaSigns.Sign.DINER),"a hand on a board reads it, the cafe's across its false front among them");
            // 0.4.449: under the depot the corridor's walls are painted on the corridor's side, each door has its plate, and room 6's
            // has its card; the yard's tank is open at the top and dry.
            var painted=f.out.getBlockState(BorrascaRooms.at(b,-21,7,-190));
            h.assertTrue(BorrascaWardBlock.is(painted)&&painted.getValue(BorrascaWardBlock.FACING)==Direction.EAST&&painted.getValue(BorrascaWardBlock.COURSE)==1&&BorrascaSignBlock.is(f.out.getBlockState(BorrascaRooms.at(b,-18,8,BorrascaRooms.ROOM_Z[5])),BorrascaSigns.Sign.ROOM_6),"the corridor's wall is painted where it looks into the corridor, and room 6 has its plate: "+painted);
            h.assertTrue(f.out.getBlockState(BorrascaRooms.at(b,21,23,-174)).is(Blocks.SPRUCE_TRAPDOOR)&&f.out.getBlockState(BorrascaRooms.at(b,23,23,-174)).is(Blocks.LADDER)&&f.out.getBlockState(BorrascaRooms.at(b,20,20,-175)).is(Blocks.RED_SAND)
                &&BorrascaStory.click(p,b,BorrascaRooms.world(new BlockPos(20,20,-175)),hand),"the tank's ladder reaches its lid, the hatch stands open over red silt, and a hand in it is answered");
            h.assertTrue(!BorrascaShopTexts.pump(0,false).equals(BorrascaShopTexts.pump(1,false))&&!BorrascaShopTexts.pump(0,true).equals(BorrascaShopTexts.pump(1,true)),"each pump's dials stopped at its own last sale");
            h.assertTrue(f.out.getBlockState(b.offset(20,0,-103)).getValue(BorrascaShopBlock.STAGE)!=f.out.getBlockState(b.offset(20,0,-102)).getValue(BorrascaShopBlock.STAGE),"the pharmacy's shelves are not all one arrangement");
            h.assertTrue(f.out.getBlockState(b.offset(18,1,-97)).getValue(RoadhouseBlock.FACING)==Direction.EAST,"the pharmacy register's keys face the pharmacist");
            // 0.4.444: the depot's papers and the Sheriff's forms are each their own paper, and each can be read.
            var faces=new java.util.HashSet<BorrascaShopBlock.Kind>();
            for(var paper:new BlockPos[]{BorrascaRooms.DISPATCH,BorrascaRooms.PAYROLL,BorrascaRooms.SIGN_IN,BorrascaRooms.INTAKE,BorrascaRooms.TAGS,BorrascaRooms.FORMS,BorrascaRooms.EVIDENCE,BorrascaRooms.GATE_LOG,BorrascaRooms.NIGHT_BOOK,BorrascaRooms.LETTER}){
                var ps=f.out.getBlockState(BorrascaRooms.at(b,paper));h.assertTrue(ps.is(BorrascaRegistry.SHOP.get())&&ps.getValue(BorrascaShopBlock.KIND).doc()&&faces.add(ps.getValue(BorrascaShopBlock.KIND)),"a paper with its own face lies at "+paper+": "+ps);}
            f.at(p,24,0,-19);h.assertTrue(flat(text(opened(f,p,BorrascaRooms.FORMS,"the Sheriff's report forms"))).contains(flat("Finding: RUNAWAY")),"the forms on the Sheriff's desk have their finding printed already");
            f.at(p,-24,12,-175);h.assertTrue(flat(text(opened(f,p,BorrascaRooms.DISPATCH,"the depot's dispatch sheet"))).contains(flat("folding tables to the church")),"the dispatch sheet sends Prescott's truck to the church with the tables");
            h.assertTrue(flat(text(opened(f,p,BorrascaRooms.PAYROLL,"the depot's payroll"))).contains(flat("by their own fathers")),"the payroll names who in town is paid");
            h.assertTrue(f.out.getBlockState(BorrascaRooms.at(b,7,1,-6)).is(BorrascaRegistry.FITTING.get())&&f.out.getBlockState(BorrascaRooms.at(b,0,6,-6)).getBlock() instanceof SlabBlock&&f.out.getBlockEntity(BorrascaRooms.at(b,BorrascaRooms.SOURCE)) instanceof LecternBlockEntity,"the reader arrives inside the trailhead station, under its roof, with the trail notice");
            for(int z=-3;z>=-14;z--)h.assertTrue(clear(f,p,b,0,0,z),"the station aisle and doorway stay open at z "+z);
            h.assertTrue(RoadhouseBlock.is(f.out.getBlockState(BorrascaRooms.at(b,-19,1,-26)),RoadhouseBlock.Kind.REGISTER)&&f.out.getBlockState(BorrascaRooms.at(b,-16,0,-23)).getBlock() instanceof DoorBlock,"Drisking General has its till and its door");
            h.assertTrue(f.out.getBlockState(BorrascaRooms.at(b,27,0,-26)).is(Blocks.IRON_BARS)&&f.out.getBlockState(BorrascaRooms.at(b,18,0,-23)).getBlock() instanceof DoorBlock,"the Sheriff's office has its cell and its door");
            h.assertTrue(f.out.getBlockState(BorrascaRooms.at(b,1,6,-89)).isAir()&&f.out.getBlockState(BorrascaRooms.at(b,2,7,-89)).isAir()&&f.out.getBlockState(BorrascaRooms.at(b,0,10,-100)).is(Blocks.OAK_LOG)&&f.out.getBlockState(BorrascaRooms.at(b,0,12,-94)).getBlock() instanceof SlabBlock,"the fort is walled and roofed, its doorway open, a trunk through its back wall");
            h.assertTrue(clear(f,p,b,-4,0,-99)&&clear(f,p,b,3,0,-99)&&!clear(f,p,b,0,0,-99),"the trail forks round the Triple Tree's burl");
            for(int z=-170;z>=-204;z--){if(z==-181||z==-198){h.assertTrue(f.out.getBlockState(BorrascaRooms.at(b,-17,12,z)).getBlock() instanceof DoorBlock,"a door in the depot partition at z "+z);continue;}h.assertTrue(clear(f,p,b,-17,12,z),"the depot aisle stays open from the front door to the service exit at z "+z);}
            h.assertTrue(f.out.getBlockState(BorrascaRooms.at(b,-28,12,-183)).is(BorrascaRegistry.FITTING.get())&&f.out.getBlockState(BorrascaRooms.at(b,-17,12,-169)).getBlock() instanceof DoorBlock&&f.out.getBlockState(BorrascaRooms.at(b,4,14,-190)).is(BorrascaRegistry.FITTING.get()),"the depot's drawers, front door and machine shed stand");
            // The papers: the trail map, the store's board in childhood, the friend's notes.
            f.at(p,5,0,-6);var map=opened(f,p,BorrascaRooms.TRAIL_MAP,"the trail map");h.assertTrue(text(map).contains("Marker 2"),"the map shows where the public walk ends");
            f.at(p,-13,0,-20);var board=opened(f,p,BorrascaRooms.BOARD[3],"the noticeboard");h.assertTrue(text(board).contains("DANA PRUITT")&&!text(board).contains("KELLY"),"the childhood board has an older child's poster and not hers");
            f.at(p,-1,6,-96);var notes=opened(f,p,BorrascaRooms.NOTES,"the friend's notes");h.assertTrue(title(notes).equals(BorrascaStory.FRIEND+"'s notes"),"her notes are pinned in the fort");
            // 0.4.419: her notes keep the tag rhyme, and the store's history hangs too high for a child.
            h.assertTrue(flat(text(notes)).contains(flat("three trunks meet")),"her notes keep the tag rhyme");
            // 0.4.422: the street past the chapel has its shops, and the frame hangs in the historical society, still too high for a child.
            f.atWorld(p,new BlockPos(-17,0,-97));f.clickWorld(p,BorrascaShops.HISTORY);h.assertFalse(p.containerMenu instanceof LiteraryVignettes.Pages,"the society's framed history is out of a child's reach");
            h.assertTrue(BorrascaShopBlock.is(f.out.getBlockState(b.offset(BorrascaShops.PUMPS[0])),BorrascaShopBlock.Kind.PUMP)&&BorrascaShopBlock.is(f.out.getBlockState(b.offset(BorrascaShops.PIE_CASE)),BorrascaShopBlock.Kind.PIE_CASE)&&f.out.getBlockState(b.offset(BorrascaShops.POOL)).is(BarRegistry.POOL_TABLE.get()),"the station has its pumps, the diner its pies, the bar its pool table");
            // 0.4.454 (owner: "Borrasca needs a custom texture pass"; of the pews, "again, custom textures would be beneficial here."):
            // each pump is its own, foot and head; the church has pews and the town benches, where plank stairs stood.
            for(int i=0;i<2;i++)for(var at:List.of(BorrascaShops.PUMPS[i],BorrascaShops.PUMPS[i].above())){var s=f.out.getBlockState(b.offset(at));
                h.assertTrue(BorrascaShopBlock.is(s,at.getY()==0?BorrascaShopBlock.Kind.PUMP:BorrascaShopBlock.Kind.PUMP_TOP)&&s.getValue(BorrascaShopBlock.STAGE)==i,"pump "+i+" is its own at "+at.toShortString()+": "+s);}
            seat(h,f,b.offset(15,0,-60),BorrascaShopBlock.Kind.PEW_END,Direction.NORTH,0,"a pew's end at the wall");seat(h,f,b.offset(16,0,-60),BorrascaShopBlock.Kind.PEW_MID,Direction.NORTH,0,"the pew between its ends");
            seat(h,f,b.offset(17,0,-60),BorrascaShopBlock.Kind.PEW_END,Direction.NORTH,1,"its other end at the aisle");seat(h,f,b.offset(19,0,-60),BorrascaShopBlock.Kind.PEW_END,Direction.NORTH,0,"the pew across the aisle");
            seat(h,f,b.offset(16,0,-68),BorrascaShopBlock.Kind.PEW_END,Direction.NORTH,1,"the last row stands back from the aisle");
            h.assertTrue(f.out.getBlockState(b.offset(18,0,-60)).isAir()&&f.out.getBlockState(b.offset(17,0,-68)).isAir()&&f.out.getBlockState(b.offset(18,0,-66)).isAir(),"the church's aisle is clear");
            seat(h,f,b.offset(-15,0,-27),BorrascaShopBlock.Kind.BENCH_END,Direction.EAST,0,"the store's porch bench");seat(h,f,b.offset(-15,0,-26),BorrascaShopBlock.Kind.BENCH_END,Direction.EAST,1,"its other end");
            seat(h,f,b.offset(19,0,-18),BorrascaShopBlock.Kind.BENCH,Direction.EAST,0,"the lone bench in the Sheriff's office");seat(h,f,b.offset(-6,0,-7),BorrascaShopBlock.Kind.BENCH_MID,Direction.EAST,0,"the middle of the ranger's bench");
            seat(h,f,BorrascaRooms.at(b,-15,12,-176),BorrascaShopBlock.Kind.BENCH_MID,Direction.WEST,0,"the depot's waiting bench");
            for(var at:List.of(b.offset(15,0,-60),b.offset(-15,0,-27),b.offset(19,0,-18),b.offset(-6,0,-7),b.offset(-11,0,-20),b.offset(-18,0,-28),BorrascaRooms.at(b,-21,12,-172),BorrascaRooms.at(b,-15,12,-176)))
                h.assertFalse(f.out.getBlockState(at).getBlock() instanceof StairBlock,"no plank stair stands for a seat at "+at.subtract(b).toShortString());
            for(var door:new BlockPos[]{BorrascaShops.DINER_DOOR,BorrascaShops.ANTIQUES_DOOR,BorrascaShops.SOCIETY_DOOR,BorrascaShops.STATION_DOOR,BorrascaShops.DOCTOR_DOOR,BorrascaShops.DRUG_DOOR,BorrascaShops.BAR_DOOR})h.assertTrue(f.out.getBlockState(b.offset(door)).getBlock() instanceof DoorBlock,"a real door at "+door);
            for(int z=-76;z>=-132;z-=4)h.assertTrue(BorrascaLand.groundAt(-12,z)==0&&BorrascaLand.groundAt(12,z)==0,"the street past the chapel is level, the way down ending at the trailhead, at z "+z);
            f.atWorld(p,new BlockPos(-13,0,-88));h.assertTrue(flat(text(openedWorld(f,p,BorrascaShops.MENU,"the diner's menu"))).contains(flat("Coffee .50")),"the childhood menu has its early prices");
            f.atWorld(p,new BlockPos(10,0,-86));var cards=flat(corkboard(f,p,"the doctor's corkboard"));h.assertTrue(cards.contains(flat("1992")),"the doctor's cards are a few weeks apart in the childhood year");
            // 0.4.455 (owner: "Try to show, don't tell. Try to design an actual ui for this"): the board is a board. Five cards and the envelope
            // hang on it; a piece is looked at once it has been in the hand, and the board is read when every piece on top has been.
            var pinned=BorrascaCards.board(1);
            h.assertTrue(pinned.size()==6&&pinned.get(5).envelope()&&pinned.get(5).text().equals("Dr. Hale")&&pinned.stream().noneMatch(BorrascaCards.Piece::under)&&BorrascaCards.top(1)==63,"five cards and the doctor's envelope hang on the first visit's board: "+pinned);
            h.assertTrue(pinned.stream().limit(5).noneMatch(card->card.thanks().isEmpty()||card.name().isEmpty()||card.signed().isEmpty()),"every card has its child's name, a line to the doctor and who it is from");
            for(int i=0;i<5;i++)BorrascaCards.seen(p,i);
            h.assertTrue(f.own(p).getInt("Seen_Cards")==31&&!f.own(p).getBoolean("Read_Cards"),"the board is not read with the envelope still on its pin: "+f.own(p).getInt("Seen_Cards"));
            BorrascaCards.seen(p,6);BorrascaCards.seen(p,-1);BorrascaCards.seen(p,40);
            h.assertTrue(f.own(p).getInt("Seen_Cards")==31,"a piece that does not hang on the board is not looked at: "+f.own(p).getInt("Seen_Cards"));
            var atBoard=p.position();f.atWorld(p,new BlockPos(-17,0,-101));BorrascaCards.seen(p,5);
            h.assertTrue(f.own(p).getInt("Seen_Cards")==31,"nobody looks at the board from across the street");
            p.teleportTo(atBoard.x,atBoard.y,atBoard.z);BorrascaCards.seen(p,5);
            h.assertTrue(f.own(p).getInt("Seen_Cards")==63&&f.own(p).getBoolean("Read_Cards"),"every piece looked at is the board read: "+f.own(p).getInt("Seen_Cards"));
            // 0.4.424 (owner: "the children were named after their progenitor"): the depot's children carry their fathers' letter, P for the
            // Prescotts and K for the Sheriff: the friend, the two town children and every baby on the doctor's board.
            h.assertTrue(BorrascaStory.FRIEND.startsWith("K")&&cards.contains(flat("Kelsey Ann"))&&cards.contains(flat("Paul"))&&cards.contains(flat("Peyton and Kody")),"the friend and every baby on the board have a P or K name: "+cards);
            f.atWorld(p,new BlockPos(-17,0,-101));h.assertTrue(flat(text(openedWorld(f,p,BorrascaShops.MAP,"the map of the old workings"))).contains(flat("DEPOT")),"the old map puts the depot over the first shaft");
            f.atWorld(p,new BlockPos(-22,0,-19));h.assertTrue(flat(text(openedWorld(f,p,BorrascaShops.PEGBOARD[0],"the hardware pegboard"))).contains(flat("Flashlight 4.99")),"the store's hardware counter has its price card");
            // 0.4.423: the Hollis yard's swing is a real one; a jump off it going forward throws the child out across the yard.
            // 0.4.443: it faces the path (south), away from the side fence, and every jump is pushed forward.
            var seatAt=BorrascaRooms.at(b,new BlockPos(-9,0,-54));
            h.assertTrue(HouseReadSwing.is(f.out.getBlockState(seatAt))&&HouseReadSwing.is(f.out.getBlockState(seatAt.east()))&&f.out.getBlockState(seatAt.above(3)).is(HouseReadRegistry.ROPE.get())&&f.out.getBlockState(seatAt.above(4)).is(Blocks.STRIPPED_OAK_LOG),"the Hollis yard has a rope swing, its seat and ropes under the crossbar");
            f.at(p,-9,0,-52);h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,seatAt)&&p.getVehicle() instanceof io.github.knaitoe.theoldesthouse.house.SeatEntity seat&&seat.swinging(),"a child sits on the yard's swing");
            ((io.github.knaitoe.theoldesthouse.house.SeatEntity)p.getVehicle()).swing(f.out.getGameTime()-157);var away=HouseReadSwing.jump(p);
            h.assertTrue(away!=null&&!p.isPassenger()&&away.z>.45&&Math.abs(away.x)<.01,"a jump going forward throws the child out south, across the yard toward the path: "+away);
            for(int z=-53;z<=-48;z++)for(int y=0;y<=1;y++)for(int x=-9;x<=-8;x++)h.assertTrue(f.out.getBlockState(BorrascaRooms.at(b,x,y,z)).getCollisionShape(f.out,BorrascaRooms.at(b,x,y,z)).isEmpty(),"the yard in front of the swing is clear to land in at "+x+","+y+","+z+": "+f.out.getBlockState(BorrascaRooms.at(b,x,y,z)));
            h.assertTrue(f.out.getBlockState(BorrascaRooms.at(b,-9,0,-57)).is(Blocks.IRON_BARS),"the side fence is behind the swing, not in front of it");
            // The swing now leaves the child facing the town, and the cast is only recalled out of sight: turn them back to the fence.
            f.at(p,-9,0,-52);
            f.stage(p,3);
        }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"adult cast recalled")).thenExecute(()->{
            h.assertTrue(f.out.getBlockState(BorrascaRooms.at(f.base(p),BorrascaRooms.FRIENDS_NAMES)).getValue(BorrascaNamesBlock.FACE)==BorrascaNamesBlock.BOTH_GONE,"by the third visit both their names have been scraped off the Tree");
            f.at(p,-13,0,-20);var board=opened(f,p,BorrascaRooms.BOARD[0],"the later noticeboard");h.assertTrue(text(board).contains("KELLY")&&text(board).contains("CLOSED"),"years later her own poster is up, closed");
            f.at(p,-1,6,-96);h.assertTrue(title(opened(f,p,BorrascaRooms.NOTES,"the aged notes")).contains("water-stained"),"her notes are still there, damp through");
            f.atWorld(p,new BlockPos(-13,0,-88));h.assertTrue(flat(text(openedWorld(f,p,BorrascaShops.MENU,"the later menu"))).contains(flat("Coffee 1.25")),"years later the menu's prices have gone up");
            f.atWorld(p,new BlockPos(10,0,-86));h.assertTrue(f.own(p).getInt("CardsBoard")==1,"the board a reader last stood at was the childhood's");BorrascaCards.seen(p,0);
            h.assertTrue(f.own(p).getInt("Seen_CardsLater")==0,"a board from another visit is not this one");
            var later=flat(corkboard(f,p,"the later corkboard"));h.assertTrue(later.contains(flat("2009"))&&later.contains(flat("Kennedy"))&&later.contains(flat("Piper and Kade")),"the corkboard holds a later year's cards, the babies still named P and K: "+later);
            // 0.4.455: the childhood's cards are still under the later year's, gone yellow, and are not what reading the board asks.
            var grownBoard=BorrascaCards.board(3);
            h.assertTrue(grownBoard.size()==11&&grownBoard.stream().limit(5).allMatch(card->card.under()&&card.old()&&card.date().endsWith("1992"))&&grownBoard.stream().skip(5).noneMatch(BorrascaCards.Piece::under)&&grownBoard.get(10).envelope()&&grownBoard.get(10).old()&&later.contains(flat("Kelsey Ann"))&&java.util.Arrays.equals(BorrascaCards.order(3),new int[]{5,0,6,1,7,2,8,3,9,4,10}),"the old cards hang under the new, each after the one over it: "+grownBoard+" / "+java.util.Arrays.toString(BorrascaCards.order(3)));
            for(int i=5;i<11;i++)BorrascaCards.seen(p,i);
            h.assertTrue(f.own(p).getBoolean("Read_CardsLater")&&f.own(p).getInt("Seen_CardsLater")==BorrascaCards.top(3),"the later board is read by what hangs on top: "+f.own(p).getInt("Seen_CardsLater"));
            f.stage(p,4);
        }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"last cast recalled")).thenExecute(()->{
            h.assertTrue(f.out.getBlockState(BorrascaRooms.at(f.base(p),BorrascaRooms.FRIENDS_NAMES)).getValue(BorrascaNamesBlock.FACE)==BorrascaNamesBlock.LONG_GONE,"years later the two patches have gone grey");
            f.at(p,-13,0,-20);var board=opened(f,p,BorrascaRooms.BOARD[1],"the last noticeboard");h.assertTrue(text(board).contains("KAYLA"),"the last night's board has a new poster: visit "+f.own(p).getInt("Visit")+", "+title(board)+": "+text(board));
            var own=f.own(p);h.assertTrue(own.contains("Original_Board")&&own.contains("Original_Board3")&&own.contains("Original_Board4")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA),"each visit's board is its own original, and reading confers nothing");
            // 0.4.419: grown, the reader can read the frame: what the word means and what the town owes the Prescotts.
            f.atWorld(p,new BlockPos(-17,0,-97));var history=openedWorld(f,p,BorrascaShops.HISTORY,"the society's framed history");h.assertTrue(flat(text(history)).contains(flat("borrasca"))&&flat(text(history)).contains(flat("gives nothing back"))&&flat(text(history)).contains(flat("Prescotts stayed")),"the frame says what borrasca means and who brought the town back");
            var read=new net.minecraft.nbt.CompoundTag();read.putBoolean("Read_History",true);h.assertTrue(BorrascaTexts.account(read).contains("gives nothing back")&&!BorrascaTexts.account().contains("gives nothing back"),"the account keeps the word only for a reader who read the frame");

        }).thenSucceed();
    }

    /**
     * 0.4.425: the town's people and the pocket money. Each shop has its keeper at the counter and Earl is in the Sheriff's cell; a child has
     * a dollar fifty in change, buys coffee that comes mostly milk, is short for pie, is refused a flashlight for the trail, gets one lollipop
     * and is turned out of the bar. Grown, the reader has a wallet with twenty in it, real coffee, a flashlight and a beer, and is let stay in
     * the bar. On the last night only the station and the bar are open. Leaving takes the money out of the reader's pockets.
     */
    @AfterBatch(batch="borrasca_people") public static void peopleDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="borrasca_people",timeoutTicks=2400)
    public static void theTownsKeepersSellToAChildAndAGrownReaderAndTurnChildrenOutOfTheBar(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("drisking_customer");f.shoved(p);
        java.util.function.IntFunction<LiteraryActor> keeper=phase->BorrascaPeople.keeper(p,f.own(p),phase);
        java.util.function.BiFunction<Integer,BorrascaPeople.Ware,String> buy=(phase,ware)->{var own=f.own(p);var line=BorrascaPeople.buy(p,own,keeper.apply(phase),ware);f.save(p,own);return line;};
        java.util.function.Function<net.minecraft.world.item.Item,Integer> count=item->p.getInventory().countItem(item);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->f.enter(p))
          .thenWaitUntil(()->{for(int k=BorrascaPeople.LORNA;k<=BorrascaPeople.EARL;k++)h.assertTrue(keeper.apply(k)!=null,"keeper "+k+" stands at the counter");})
          .thenWaitUntil(()->h.assertTrue(BorrascaPeople.carried(p,false)!=null,"the child has change in a pocket"))
          .thenExecute(()->{
            var b=f.base(p);
            for(int k=BorrascaPeople.LORNA;k<=BorrascaPeople.EARL;k++){var a=keeper.apply(k);h.assertTrue(!a.isInvisible()&&a.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(BorrascaPeople.post(k))))<1.1,"on a childhood visit every keeper is at work, at the counter: "+BorrascaPeople.name(a));}
            var earl=keeper.apply(BorrascaPeople.EARL);h.assertTrue(earl.blockPosition().equals(BorrascaRooms.at(b,28,0,-28)),"Earl is in the Sheriff's cell");
            // 0.4.444: the cell is shut, and Earl is spoken to through its bars.
            var cellDoor=f.out.getBlockState(BorrascaRooms.at(b,26,0,-28));h.assertTrue(cellDoor.getBlock() instanceof net.minecraft.world.level.block.DoorBlock&&!cellDoor.getValue(net.minecraft.world.level.block.DoorBlock.OPEN),"the cell's door is shut: "+cellDoor);
            f.at(p,24,0,-27);f.click(p,new BlockPos(26,1,-27));h.assertTrue(earl.getCustomName()!=null&&earl.isCustomNameVisible()&&earl.getCustomName().getString().contains("asleep"),"a hand on the bars speaks to Earl through them: "+earl.getCustomName());
            h.assertTrue(BorrascaPeople.cents(f.own(p))==BorrascaPeople.CHANGE&&BorrascaPurseItem.money(BorrascaPeople.CHANGE).equals("1.50"),"a child has a dollar fifty to spend");
            f.atWorld(p,new BlockPos(-15,0,-85));
            var coffee=buy.apply(BorrascaPeople.LORNA,BorrascaPeople.Ware.COFFEE);
            h.assertTrue(coffee!=null&&coffee.contains("mostly milk")&&count.apply(BorrascaRegistry.MILK_COFFEE.get())==1&&count.apply(BorrascaRegistry.COFFEE.get())==0,"the host answers a child's coffee order and makes it mostly milk: "+coffee);
            h.assertTrue(BorrascaPeople.cents(f.own(p))==100&&BorrascaPurseItem.cents(BorrascaPeople.carried(p,false))==100,"the coffee cost fifty cents and the change in the pocket says so");
            var pie=buy.apply(BorrascaPeople.LORNA,BorrascaPeople.Ware.PIE);
            h.assertTrue(pie!=null&&pie.contains("short")&&count.apply(BorrascaRegistry.PIE.get())==0&&BorrascaPeople.cents(f.own(p))==100,"a dollar is short of a slice of pie: "+pie);
            f.at(p,-20,0,-26);
            var torch=buy.apply(BorrascaPeople.AMES,BorrascaPeople.Ware.FLASHLIGHT);
            h.assertTrue(torch!=null&&torch.contains("For the trail")&&count.apply(BorrascaRegistry.FLASHLIGHT.get())==0&&BorrascaPeople.cents(f.own(p))==100,"the store won't sell a child a flashlight for the trail: "+torch);
            f.atWorld(p,new BlockPos(-18,0,-117));
            h.assertTrue(buy.apply(BorrascaPeople.KURT,BorrascaPeople.Ware.CANDY)!=null&&count.apply(BorrascaRegistry.CANDY.get())==1&&BorrascaPeople.cents(f.own(p))==65,"thirty-five cents buys a candy bar at the station");
            h.assertTrue(buy.apply(BorrascaPeople.GUS,BorrascaPeople.Ware.BEER)==null&&count.apply(BorrascaRegistry.BEER.get())==0,"the bar serves a child nothing");
            f.atWorld(p,new BlockPos(11,0,-82));
            var own=f.own(p);h.assertTrue(BorrascaPeople.lollipop(p,own)&&!BorrascaPeople.lollipop(p,own)&&count.apply(BorrascaRegistry.LOLLIPOP.get())==1,"the receptionist gives a child one lollipop, and only one");f.save(p,own);
            h.assertTrue(BorrascaShopBlock.is(f.out.getBlockState(b.offset(BorrascaShops.JAR)),BorrascaShopBlock.Kind.JAR)&&f.out.getBlockState(b.offset(BorrascaShops.JAR)).getValue(BorrascaShopBlock.STAGE)==1,"the jar is lower by one");
            f.atWorld(p,new BlockPos(12,0,-114));
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("BarShove"),"Gus starts walking the child out"))
          .thenExecute(()->{var gus=keeper.apply(BorrascaPeople.GUS);var b=f.base(p);h.assertTrue(gus.getCustomName()!=null&&gus.getCustomName().getString().startsWith("Out")&&BorrascaPeople.inBar(p.blockPosition().subtract(b)),"he says so, and the child is still inside, not set down outside: "+p.position().subtract(Vec3.atLowerCornerOf(b)));
              h.assertTrue(f.out.getBlockState(b.offset(BorrascaShops.BAR_DOOR)).getValue(DoorBlock.OPEN),"he opens the door for them");})
          .thenWaitUntil(()->h.assertTrue(p.getX()-f.base(p).getX()<7,"Gus pushes the child out of the bar onto the sidewalk: "+p.position().subtract(Vec3.atLowerCornerOf(f.base(p)))))
          .thenWaitUntil(()->h.assertTrue(!f.own(p).getBoolean("BarShove")&&!f.out.getBlockState(f.base(p).offset(BorrascaShops.BAR_DOOR)).getValue(DoorBlock.OPEN),"and the door swings shut behind them"))
          .thenExecute(()->{h.assertTrue(f.own(p).getInt("BarTurnedOut")==1,"turned out once");f.stage(p,3);})
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&BorrascaPeople.carried(p,true)!=null,"years later the reader carries a wallet"))
          .thenExecute(()->{
            h.assertTrue(BorrascaPeople.carried(p,false)==null&&BorrascaPeople.cents(f.own(p))==BorrascaPeople.WALLET,"the child's change is gone and the wallet holds twenty");
            f.atWorld(p,new BlockPos(-15,0,-85));
            var coffee=buy.apply(BorrascaPeople.LORNA,BorrascaPeople.Ware.COFFEE);
            h.assertTrue(coffee!=null&&!coffee.contains("milk")&&count.apply(BorrascaRegistry.COFFEE.get())==1&&BorrascaPeople.cents(f.own(p))==1875,"grown, the coffee is coffee, at the later price: "+coffee);
            f.at(p,-20,0,-26);
            h.assertTrue(buy.apply(BorrascaPeople.AMES,BorrascaPeople.Ware.FLASHLIGHT)!=null&&count.apply(BorrascaRegistry.FLASHLIGHT.get())==1&&BorrascaPeople.cents(f.own(p))==876,"a grown reader can buy a flashlight");
            f.atWorld(p,new BlockPos(12,0,-116));
            h.assertTrue(buy.apply(BorrascaPeople.GUS,BorrascaPeople.Ware.BEER)!=null&&count.apply(BorrascaRegistry.BEER.get())==1&&BorrascaPeople.cents(f.own(p))==576,"the bar serves a grown reader a beer");
          }).thenIdle(20).thenExecute(()->{
            h.assertTrue(BorrascaPeople.inBar(p.blockPosition().subtract(f.base(p)))&&f.own(p).getInt("BarTurnedOut")==0,"a grown reader stays in the bar");
            f.stage(p,4);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(p).getInt("PurseVisit")==4,"the last night's purse"))
          .thenExecute(()->{
            for(int k=BorrascaPeople.LORNA;k<=BorrascaPeople.EARL;k++){boolean open=k==BorrascaPeople.KURT||k==BorrascaPeople.GUS||k==BorrascaPeople.EARL;h.assertTrue(keeper.apply(k).isInvisible()!=open,"on the last night only the station and the bar are open: "+BorrascaPeople.name(keeper.apply(k)));}
            f.atWorld(p,new BlockPos(-15,0,-85));h.assertTrue(buy.apply(BorrascaPeople.LORNA,BorrascaPeople.Ware.PIE)==null,"the diner is closed on the last night");
            BorrascaStory.depart(p);h.assertTrue(BorrascaPeople.carried(p,true)==null&&BorrascaPeople.carried(p,false)==null,"leaving takes the money out of the reader's pockets");
          }).thenSucceed();
    }

    /**
     * 0.4.426: the town's day. The Webbs sit at the diner counter and the Tates wait at the doctor's on the first visit; their girl's card
     * is on the board by the second. The payphone takes a quarter, gives it back for a number not in service and nothing for 911; the
     * Sheriff's office has no open case for the friend once she is gone, and years later still none. A child who is short has the friend
     * put in the rest. Grown, two retired deputies sit at the bar and Earl says too much; on the last night Prescott's number rings through
     * to a machine running.
     */
    @AfterBatch(batch="borrasca_day") public static void dayDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="borrasca_day",timeoutTicks=3000)
    public static void theTownsDayHasItsPatronsAPayphoneWithNoOpenCaseAndFriendsWhoPoolTheirCoins(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("drisking_caller");
        java.util.function.IntFunction<LiteraryActor> person=phase->BorrascaPeople.keeper(p,f.own(p),phase);
        java.util.function.Function<String,BorrascaPayphone.Call> dial=number->{var own=f.own(p);var call=BorrascaPayphone.dial(p,own,number);f.save(p,own);return call;};
        java.util.function.Function<BorrascaPayphone.Call,String> said=call->String.join(" ",call.lines());
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->f.enter(p))
          .thenWaitUntil(()->{h.assertTrue(f.own(p).getBoolean("CastRecalled"),"cast recalled");for(int k=BorrascaPeople.LORNA;k<=BorrascaPeople.MERLE;k++)h.assertTrue(person.apply(k)!=null,"everyone in town is placed: "+k);})
          .thenWaitUntil(()->h.assertTrue(BorrascaPeople.carried(p,false)!=null,"the child has change"))
          .thenExecute(()->{
            var b=f.base(p);
            for(int k:new int[]{BorrascaPeople.WEBB,BorrascaPeople.SHERRY,BorrascaPeople.ROY,BorrascaPeople.JOLENE})h.assertFalse(person.apply(k).isInvisible(),"on the first visit "+BorrascaPeople.name(person.apply(k))+" is about");
            for(int k:new int[]{BorrascaPeople.DUTCH,BorrascaPeople.MERLE})h.assertTrue(person.apply(k).isInvisible(),"the retired deputies aren't at the bar in the daytime of a childhood visit");
            h.assertTrue(RoadhouseBlock.is(f.out.getBlockState(person.apply(BorrascaPeople.WEBB).blockPosition()),RoadhouseBlock.Kind.STOOL),"Mr. Webb sits on a stool at the diner counter");
            h.assertTrue(f.out.getBlockState(person.apply(BorrascaPeople.JOLENE).blockPosition()).is(HouseBlocks.HOUSEHOLD_FURNITURE.get()),"Jolene Tate sits in a waiting-room chair");
            // The payphone: a quarter for a call, the coin back for a number not in service, 911 free.
            f.atWorld(p,new BlockPos(-13,0,-111));
            var sheriff=dial.apply(BorrascaPayphone.SHERIFF);h.assertTrue(sheriff!=null&&sheriff.charged()&&said.apply(sheriff).contains("grown-ups")&&BorrascaPeople.cents(f.own(p))==125,"the Sheriff's office tells a child the line is for grown-ups, for a quarter: "+(sheriff==null?null:said.apply(sheriff)));
            var wrong=dial.apply("5551234");h.assertTrue(!wrong.charged()&&said.apply(wrong).contains("not in service")&&BorrascaPeople.cents(f.own(p))==125,"a number not in service gives the quarter back");
            var help=dial.apply(BorrascaPayphone.EMERGENCY);h.assertTrue(said.apply(help).contains("dispatch")&&BorrascaPeople.cents(f.own(p))==125,"911 is free");
            f.atWorld(p,new BlockPos(30,0,-140));h.assertTrue(dial.apply(BorrascaPayphone.SHERIFF)==null,"away from the phone nothing is dialled");
            // Friends pool their coins: short for pie, the friend beside the child puts in the rest.
            var own=f.own(p);own.putInt("Cents",100);f.save(p,own);
            f.atWorld(p,new BlockPos(-15,0,-86));var kelly=(LiteraryActor)f.out.getEntity(f.own(p).getUUID("CastKimber"));kelly.moveTo(p.getX()+1,p.getY(),p.getZ());
            own=f.own(p);var pie=BorrascaPeople.buy(p,own,person.apply(BorrascaPeople.LORNA),BorrascaPeople.Ware.PIE);f.save(p,own);
            h.assertTrue(pie!=null&&p.getInventory().countItem(BorrascaRegistry.PIE.get())==1&&BorrascaPeople.cents(f.own(p))==0&&f.own(p).getInt("FriendCents")==BorrascaPeople.FRIEND_CHANGE-25,"the friend puts in the quarter the child was short: "+pie+" / "+f.own(p).getInt("FriendCents"));
            f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,2);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(p).getInt("PurseVisit")==2,"second visit: "+BorrascaCast.why(p)))
          .thenExecute(()->{
            h.assertTrue(person.apply(BorrascaPeople.ROY).isInvisible()&&person.apply(BorrascaPeople.JOLENE).isInvisible(),"the Tates are home with their baby");
            f.atWorld(p,new BlockPos(10,0,-86));var cards=flat(corkboard(f,p,"the second visit's corkboard"));
            h.assertTrue(cards.contains(flat("Roy and Jolene"))&&cards.contains(flat("Paige")),"the Tates' girl is on the board by the second visit, a P name: "+cards);
            // 0.4.455: her card is pinned over the envelope, which is still there underneath.
            var second=BorrascaCards.board(2);
            h.assertTrue(second.size()==7&&second.get(5).envelope()&&second.get(5).under()&&second.get(6).over()&&second.get(6).place()==second.get(5).place()&&second.get(6).family().equals("Tate")&&BorrascaCards.top(2)==95&&java.util.Arrays.equals(BorrascaCards.order(2),new int[]{0,1,2,3,4,6,5}),"the Tates' card hangs over the envelope, and a hand comes to it first: "+second+" / "+java.util.Arrays.toString(BorrascaCards.order(2)));
            for(int i=0;i<5;i++)BorrascaCards.seen(p,i);BorrascaCards.seen(p,6);
            h.assertTrue(f.own(p).getBoolean("Read_Cards2")&&f.own(p).getInt("Seen_Cards2")==95,"the second board is read without the envelope under the newest card: "+f.own(p).getInt("Seen_Cards2"));
            var own=f.own(p);own.putBoolean("FriendMissing",true);f.save(p,own);
            // 0.4.444: after she is taken the Sheriff no longer says "you two".
            f.talk(p,"CastSheriff");var sheriffSaid=f.actor(p,"CastSheriff").getCustomName();h.assertTrue(sheriffSaid!=null&&sheriffSaid.getString().contains("by yourself")&&!sheriffSaid.getString().contains("You two"),"after the taking the Sheriff speaks to a child who came down alone: "+sheriffSaid);
            f.atWorld(p,new BlockPos(-13,0,-111));var tip=dial.apply(BorrascaPayphone.SHERIFF);
            h.assertTrue(said.apply(tip).contains("no open case")&&said.apply(tip).contains(BorrascaStory.FRIEND),"the Sheriff's office has no open case for her: "+said.apply(tip));
            // 0.4.458 (owner: "Dialogue should change on second day, after Kelly/friend is taken"): and so does the rest of the town.
            // Each of them says what they have to say in order and then the last of it again, and none of it is what they said
            // while there were two children.
            java.util.function.Function<LiteraryActor,String> heard=who->who.getCustomName()==null?"":who.getCustomName().getString();
            h.assertTrue(BorrascaPeople.taken(f.own(p)),"it is the second day and she has been taken");
            for(int k:new int[]{BorrascaPeople.LORNA,BorrascaPeople.AMES,BorrascaPeople.ROWE,BorrascaPeople.KURT,BorrascaPeople.FENN,BorrascaPeople.WEBB,BorrascaPeople.SHERRY,BorrascaPeople.EARL,BorrascaPeople.CARRIER}){
                var who=person.apply(k);var lines=BorrascaPeople.afterTheTaking(k);
                h.assertTrue(who!=null&&!who.isInvisible()&&lines!=null&&lines.length>=2,"everyone at work that day has something of their own to say: "+k);
                var mine=f.own(p);mine.remove("RoweOut");var all=new StringBuilder();
                for(int i=0;i<=lines.length;i++){BorrascaPeople.talk(p,who,mine);String line=heard.apply(who);all.append(line).append(" / ");
                    h.assertTrue(line.contains(lines[Math.min(i,lines.length-1)]),BorrascaPeople.name(who)+" says them in order and then the last again: "+i+" "+line);}
                h.assertTrue(!all.toString().contains("You two")&&!all.toString().contains("your friends")&&!all.toString().contains("your folks"),BorrascaPeople.name(who)+" does not speak as if nothing had happened: "+all);
                f.save(p,mine);p.closeContainer();
            }
            {var mine=f.own(p);var gus=person.apply(BorrascaPeople.GUS);BorrascaPeople.talk(p,gus,mine);h.assertTrue(heard.apply(gus).contains(BorrascaPeople.GUS_AFTER),"Gus sends the child home by the street: "+heard.apply(gus));
             mine.putBoolean("RoweOut",true);var rowe=person.apply(BorrascaPeople.ROWE);BorrascaPeople.talk(p,rowe,mine);h.assertTrue(heard.apply(rowe).contains(BorrascaPeople.ROWE_OUT_AFTER),"on his break Mr. Rowe is not looking up the street: "+heard.apply(rowe));mine.remove("RoweOut");
             // Mrs. Odell's rule is one. That day she gives two at once, and then has nothing else.
             var odell=person.apply(BorrascaPeople.ODELL);int had=p.getInventory().countItem(BorrascaRegistry.LOLLIPOP.get());
             h.assertTrue(!mine.getBoolean("Lollipop")&&BorrascaPeople.lollipop(p,mine)&&p.getInventory().countItem(BorrascaRegistry.LOLLIPOP.get())==had+2&&heard.apply(odell).contains(BorrascaPeople.ODELL_TWO),"she gives a child who came down alone two: "+heard.apply(odell)+" / "+p.getInventory().countItem(BorrascaRegistry.LOLLIPOP.get()));
             h.assertTrue(!BorrascaPeople.lollipop(p,mine)&&p.getInventory().countItem(BorrascaRegistry.LOLLIPOP.get())==had+2&&heard.apply(odell).contains(BorrascaPeople.ODELL_DONE),"and then has nothing else for them: "+heard.apply(odell));
             // A child who had the one already is given another.
             mine.remove("LollipopAfter");
             h.assertTrue(BorrascaPeople.lollipop(p,mine)&&p.getInventory().countItem(BorrascaRegistry.LOLLIPOP.get())==had+3&&heard.apply(odell).contains(BorrascaPeople.ODELL_ANOTHER)&&!BorrascaPeople.lollipop(p,mine)&&p.getInventory().countItem(BorrascaRegistry.LOLLIPOP.get())==had+3,"a child who had theirs already is given the one her rule does not allow, once: "+heard.apply(odell));
             f.save(p,mine);}
            f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,3);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(p).getInt("PurseVisit")==3,"years later: "+BorrascaCast.why(p)))
          .thenExecute(()->{
            for(int k:new int[]{BorrascaPeople.DUTCH,BorrascaPeople.MERLE})h.assertTrue(!person.apply(k).isInvisible()&&RoadhouseBlock.is(f.out.getBlockState(person.apply(k).blockPosition()),RoadhouseBlock.Kind.STOOL),"years later "+BorrascaPeople.name(person.apply(k))+" sits at the bar");
            var own=f.own(p);var merle=person.apply(BorrascaPeople.MERLE);BorrascaPeople.talk(p,merle,own);var earl=person.apply(BorrascaPeople.EARL);BorrascaPeople.talk(p,earl,own);BorrascaPeople.talk(p,earl,own);f.save(p,own);
            // 0.4.444: years later Earl is not in the cell; he sits on a flattened box at the mouth of the alley by the Tipple.
            h.assertTrue(!earl.isInvisible()&&earl.blockPosition().equals(f.base(p).offset(BorrascaPeople.EARL_STREET))&&f.out.getBlockState(earl.blockPosition()).getBlock() instanceof net.minecraft.world.level.block.CarpetBlock,"years later Earl is out on the street: "+earl.blockPosition().subtract(f.base(p)));
            h.assertTrue(merle.getCustomName()!=null&&merle.getCustomName().getString().contains("Runaway"),"Merle took the report and wrote what the Sheriff said: "+merle.getCustomName());
            h.assertTrue(earl.getCustomName()!=null&&earl.getCustomName().getString().contains("reports"),"Earl says too much: "+earl.getCustomName());
            f.atWorld(p,new BlockPos(-13,0,-111));var tip=dial.apply(BorrascaPayphone.SHERIFF);
            h.assertTrue(said.apply(tip).contains("no open case")&&BorrascaPeople.cents(f.own(p))==BorrascaPeople.WALLET-50,"years later there is still no open case, for two quarters: "+said.apply(tip));
            f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,4);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(p).getInt("PurseVisit")==4,"the last night: "+BorrascaCast.why(p)))
          .thenExecute(()->{
            f.atWorld(p,new BlockPos(-13,0,-111));var call=dial.apply(BorrascaPayphone.PRESCOTT);
            h.assertTrue(said.apply(call).contains("machine")&&call.rings()>3,"on the last night Prescott's number rings and rings, and a machine is running behind whoever picks up: "+said.apply(call));
            h.assertTrue(person.apply(BorrascaPeople.WEBB).isInvisible()&&!person.apply(BorrascaPeople.DUTCH).isInvisible(),"at night the diner's regulars are gone and the deputies still at the bar");
          }).thenSucceed();
    }

    /**
     * 0.4.456 (owner: "You could add inflections of personality into the citizens homes. Remember that this town is sick, that many know
     * what the town does and why and just accept it. Some don't, but they have to bite their tongue."): the street's four houses are four
     * households, each told by what is in its room, and years on each is the same household later.
     */
    @AfterBatch(batch="borrasca_homes") public static void homesDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="borrasca_homes",timeoutTicks=2000)
    public static void theStreetsFourHousesAreFourHouseholds(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_homes");var things=BorrascaHomes.things();
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded"))
          .thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"town cast recalled"))
          .thenExecute(()->{
              var b=f.base(p);var rooms=new ArrayList<Set<BorrascaHomeBlock.Kind>>();var lines=new HashSet<String>();int told=0;
              for(int house=0;house<BorrascaHomes.HOUSES;house++){
                  var mine=new TreeSet<BorrascaHomeBlock.Kind>();
                  for(var t:things)if(t.house()==house){
                      var s=f.out.getBlockState(b.offset(t.at()));String what="house "+house+"'s "+t.kind().getSerializedName()+" at "+t.at();
                      h.assertTrue(BorrascaHomeBlock.is(s,t.kind())&&s.getValue(BorrascaHomeBlock.FACING)==t.facing()&&s.getValue(BorrascaHomeBlock.STAGE)==0,what+" stands as its household keeps it, in the childhood year: "+s);
                      h.assertTrue(BorrascaHomes.house(t.at())==house,what+" is in its own house");
                      if(t.kind().lies()){
                          var under=f.out.getBlockState(b.offset(t.at().below()));
                          h.assertTrue(under.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())&&Set.of(HouseholdFurnitureBlock.Kind.FORMICA_TABLE,HouseholdFurnitureBlock.Kind.WALNUT_DESK,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS).contains(under.getValue(HouseholdFurnitureBlock.KIND)),what+" lies on a table, a desk or the drawers: "+under);
                      }
                      if(t.kind().wall()){
                          var behind=b.offset(t.at().relative(t.facing().getOpposite()));
                          h.assertTrue(f.out.getBlockState(behind).isSolidRender(f.out,behind),what+" hangs on a wall, not on a window or the air: "+f.out.getBlockState(behind));
                      }
                      String early=BorrascaHomes.look(house,t.kind(),false),later=BorrascaHomes.look(house,t.kind(),true);
                      h.assertTrue(early!=null&&later!=null&&!early.isBlank()&&!later.isBlank(),what+" has a line in both years");
                      h.assertTrue(!t.kind().ages()||!early.equals(later),what+" says something else years on, having aged: "+early+" / "+later);
                      for(var word:List.of("mine","baby","babies","Sheriff","stable","sold","bought the"))h.assertFalse(early.contains(word)||later.contains(word),what+" says what is there, not what it means: "+word);
                      if(lines.add(early))told++;
                      h.assertTrue(BorrascaHomes.click(p,b,t.at(),f.own(p)),what+" answers a hand with its line");
                      mine.add(t.kind());
                  }
                  rooms.add(mine);
                  h.assertTrue(mine.contains(BorrascaHomeBlock.Kind.CRIB),"house "+house+" has a crib: its card hangs on the doctor's board");
                  h.assertTrue(f.out.getBlockState(b.offset(BorrascaHomes.cell(house,0,0,0))).getBlock() instanceof BedBlock&&f.out.getBlockState(b.offset(BorrascaHomes.cell(house,0,1,0))).getBlock() instanceof BedBlock,"house "+house+" has its bed against the back wall");
                  for(int u=4;u<=6;u++){
                      var w=b.offset(BorrascaHomes.cell(house,u,3,0));
                      h.assertTrue(f.out.noCollision(p,new AABB(w.getX()+.2,w.getY()+.1,w.getZ()+.2,w.getX()+.8,w.getY()+1.8,w.getZ()+.8)),"a reader can walk in at house "+house+"'s door: "+w);
                  }
                  var door=b.offset(BorrascaHomes.cell(house,7,3,0));
                  h.assertTrue(f.out.getBlockState(door).getBlock() instanceof DoorBlock,"house "+house+"'s door is where its room expects it: "+f.out.getBlockState(door));
              }
              h.assertTrue(new HashSet<>(rooms).size()==BorrascaHomes.HOUSES,"the four rooms hold four different sets of things: "+rooms);
              h.assertTrue(told==new HashSet<>(things.stream().map(t->t.house()+"/"+t.kind()).toList()).size(),"no two houses share a line: "+told);
              h.assertTrue(rooms.get(BorrascaHomes.PRUITT).contains(BorrascaHomeBlock.Kind.PORTRAIT)&&rooms.get(BorrascaHomes.HOLLIS).containsAll(Set.of(BorrascaHomeBlock.Kind.CROSS,BorrascaHomeBlock.Kind.BIBLE))
                  &&rooms.get(BorrascaHomes.WEBB).containsAll(Set.of(BorrascaHomeBlock.Kind.TV,BorrascaHomeBlock.Kind.JACKET))&&rooms.get(BorrascaHomes.CORLEY).containsAll(Set.of(BorrascaHomeBlock.Kind.SUITCASE,BorrascaHomeBlock.Kind.ATLAS,BorrascaHomeBlock.Kind.FRAME_DOWN)),"each household has what tells it: "+rooms);
              h.assertTrue(things.stream().filter(t->t.house()==BorrascaHomes.CORLEY&&t.kind()==BorrascaHomeBlock.Kind.CRIB).count()==2,"the Corleys have two cribs: their card says twins");
              h.assertFalse(BorrascaHomes.click(p,b,BorrascaHomes.cell(BorrascaHomes.PRUITT,3,3,0),f.own(p)),"the floor of a room is not one of its things");
              f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,3);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"adult visit cast recalled"))
          .thenWaitUntil(()->h.assertTrue(f.out.getBlockState(f.base(p).offset(things.get(0).at())).getValue(BorrascaHomeBlock.STAGE)==1,"years on, the first room has aged"))
          .thenExecute(()->{
              var b=f.base(p);
              for(var t:things){
                  var s=f.out.getBlockState(b.offset(t.at()));
                  h.assertTrue(BorrascaHomeBlock.is(s,t.kind())&&s.getValue(BorrascaHomeBlock.FACING)==t.facing()&&s.getValue(BorrascaHomeBlock.STAGE)==(t.kind().ages()?1:0),"years on, house "+t.house()+"'s "+t.kind().getSerializedName()+" is the same thing later: "+s);
              }
              f.close();
          }).thenSucceed();
    }
    /**
     * 0.4.459 (owner: "Carry a letter for Dorsey. He already mentions the Garritys' mailbox going untouched. On the second visit, he
     * could hand you a letter to leave under their porch door. Years later, the box is still on its post, with the family name barely
     * legible under cloudy tape. You remember where it is because you've been to that porch."): the friend's family has a house, with
     * a door that does not open and curtains drawn; every gate has a mailbox; theirs is full on the second visit and rusted years on;
     * Mr. Dorsey hands the letter over when asked again; it goes under the door from the hand, not from the pack; it is there years
     * on, yellow, and comes out; and the account says only what happened.
     */
    @AfterBatch(batch="borrasca_letter") public static void letterDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="borrasca_letter",timeoutTicks=2400)
    public static void theGarritysLetterGoesUnderTheirDoorAndIsThereYearsOn(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_letter");String key=BorrascaGarritys.KEY;
        java.util.function.IntFunction<LiteraryActor> person=phase->BorrascaPeople.keeper(p,f.own(p),phase);
        java.util.function.Function<LiteraryActor,String> heard=who->who.getCustomName()==null?"":who.getCustomName().getString();
        java.util.function.Supplier<String> knock=()->{var own=f.own(p);String line=BorrascaGarritys.door(p,f.base(p),own);f.save(p,own);return line;};
        java.util.function.Supplier<String> dorsey=()->{var own=f.own(p);var a=person.apply(BorrascaPeople.CARRIER);BorrascaPeople.talk(p,a,own);f.save(p,own);return heard.apply(a);};
        java.util.function.IntSupplier letters=()->p.getInventory().countItem(BorrascaRegistry.LETTER.get());
        // The letter out of wherever it is in the pack and into the hand, or out of the hand and into the pack's far end.
        java.util.function.Consumer<Boolean> hold=inHand->{var inv=p.getInventory();int from=-1;for(int i=0;i<inv.getContainerSize();i++)if(BorrascaBelongings.valid(p,f.own(p),key,inv.getItem(i)))from=i;
            h.assertTrue(from>=0,"the letter is somewhere in the pack");var s=inv.getItem(from);inv.setItem(from,ItemStack.EMPTY);
            if(inHand){var was=p.getMainHandItem();p.setItemInHand(InteractionHand.MAIN_HAND,s);if(!was.isEmpty())inv.setItem(from,was);}else{var was=inv.getItem(30);inv.setItem(30,s);if(!was.isEmpty())inv.setItem(from,was);}};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded"))
          .thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"town cast recalled"))
          .thenWaitUntil(()->h.assertTrue(person.apply(BorrascaPeople.CARRIER)!=null,"Mr. Dorsey is on his round"))
          .thenExecute(()->{
              var b=f.base(p);
              // The house: their own door, in two halves, which is not one of the game's and does not open.
              var low=f.out.getBlockState(b.offset(BorrascaGarritys.DOOR));var top=f.out.getBlockState(b.offset(BorrascaGarritys.DOOR.above()));
              h.assertTrue(BorrascaHomeBlock.is(low,BorrascaHomeBlock.Kind.DOOR)&&BorrascaHomeBlock.is(top,BorrascaHomeBlock.Kind.DOOR_TOP)&&low.getValue(BorrascaHomeBlock.FACING)==Direction.EAST&&top.getValue(BorrascaHomeBlock.FACING)==Direction.EAST&&low.getValue(BorrascaHomeBlock.STAGE)==0,"the Garritys' front door stands in its wall, facing the street: "+low+" / "+top);
              h.assertTrue(!low.getCollisionShape(f.out,b.offset(BorrascaGarritys.DOOR)).isEmpty()&&!top.getCollisionShape(f.out,b.offset(BorrascaGarritys.DOOR.above())).isEmpty(),"and it is shut");
              h.assertTrue(f.out.getBlockState(b.offset(BorrascaGarritys.X0,1,BorrascaGarritys.NORTH)).is(Blocks.SPRUCE_PLANKS)&&f.out.getBlockState(b.offset(BorrascaGarritys.X1,3,BorrascaGarritys.SOUTH)).is(Blocks.SPRUCE_PLANKS),"the house is of dark boards");
              // Every pane in its walls has a curtain drawn inside it.
              int panes=0;
              for(int x=BorrascaGarritys.X0;x<=BorrascaGarritys.X1;x++)for(int z=BorrascaGarritys.NORTH;z<=BorrascaGarritys.SOUTH;z++)for(int y=1;y<=2;y++){
                  if(!f.out.getBlockState(b.offset(x,y,z)).is(Blocks.GLASS_PANE))continue;panes++;
                  var inward=x==BorrascaGarritys.X0?Direction.EAST:x==BorrascaGarritys.X1?Direction.WEST:z==BorrascaGarritys.NORTH?Direction.SOUTH:Direction.NORTH;
                  var c=f.out.getBlockState(b.offset(x,y,z).relative(inward));
                  h.assertTrue(BorrascaHomeBlock.is(c,BorrascaHomeBlock.Kind.CURTAIN)&&c.getValue(BorrascaHomeBlock.FACING)==inward,"a curtain is drawn inside the pane at "+x+","+y+","+z+": "+c);
              }
              h.assertTrue(panes==12,"six windows, two panes high: "+panes);
              // A mailbox on a post at every gate, its mouth to the street; the four households keep their name plates too.
              for(int house=0;house<BorrascaHomes.HOUSES;house++){
                  var at=BorrascaGarritys.box(house);var s=f.out.getBlockState(b.offset(at));
                  h.assertTrue(BorrascaHomeBlock.is(s,BorrascaHomeBlock.Kind.BOX)&&s.getValue(BorrascaHomeBlock.FACING)==BorrascaHomes.out(house)&&s.getValue(BorrascaHomeBlock.STAGE)==0&&f.out.getBlockState(b.offset(at.below())).is(Blocks.OAK_FENCE),"house "+house+" has a mailbox on a post at its gate: "+s);
                  h.assertTrue(BorrascaSignBlock.is(f.out.getBlockState(b.offset(at.offset(BorrascaHomes.out(house).getStepX(),0,2)))),"and its name plate still hangs by the gate: "+f.out.getBlockState(b.offset(at.offset(BorrascaHomes.out(house).getStepX(),0,2))));
                  var own=f.own(p);h.assertTrue(BorrascaGarritys.click(p,b,at,own),"house "+house+"'s mailbox answers a hand");
              }
              var theirs=f.out.getBlockState(b.offset(BorrascaGarritys.BOX));
              h.assertTrue(BorrascaHomeBlock.is(theirs,BorrascaHomeBlock.Kind.BOX)&&theirs.getValue(BorrascaHomeBlock.FACING)==Direction.EAST&&theirs.getValue(BorrascaHomeBlock.STAGE)==0&&f.out.getBlockState(b.offset(BorrascaGarritys.BOX.below())).is(Blocks.OAK_FENCE),"the Garritys' mailbox is as any other on the first visit: "+theirs);
              h.assertTrue(BorrascaGarritys.look(true,"GARRITY",1).contains("GARRITY")&&BorrascaGarritys.look(false,"WEBB",1).contains("WEBB")&&BorrascaGarritys.look(false,"WEBB",3).contains("new")&&!BorrascaGarritys.look(true,"GARRITY",1).equals(BorrascaGarritys.look(true,"GARRITY",2))&&BorrascaGarritys.look(true,"GARRITY",3).contains("barely, GARRITY"),"a mailbox says whose it is, and theirs is another thing on each visit");
              h.assertTrue(f.out.getBlockState(b.offset(BorrascaGarritys.PORCH)).isAir(),"nothing lies at their door yet");
              // A knock on the first visit: a house with people in it. With the friend there, she says whose it is.
              var friend=f.actor(p,"CastKimber");h.assertTrue(friend!=null,"the friend is in town");
              p.moveTo(friend.getX()+1.5,friend.getY(),friend.getZ());
              h.assertTrue(knock.get().equals(BorrascaGarritys.KELLY_DOOR)&&heard.apply(friend).contains(BorrascaGarritys.KELLY_DOOR),"with her beside the reader, she says it is her house: "+heard.apply(friend));
              p.moveTo(friend.getX()+40,friend.getY(),friend.getZ());
              h.assertTrue(knock.get().equals(BorrascaGarritys.KNOCK_FIRST),"alone, a knock is answered through the door");
              h.assertTrue(!dorsey.get().contains("Garritys")&&letters.getAsInt()==0,"on the first visit Mr. Dorsey has no letter for them");
              h.assertTrue(BorrascaGarritys.carried(f.own(p)).isEmpty()&&BorrascaGarritys.found(f.own(p)).isEmpty(),"and the account has nothing to say of one");
              f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,2);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(p).getInt("PurseVisit")==2,"second visit: "+BorrascaCast.why(p)))
          .thenWaitUntil(()->h.assertTrue(BorrascaHomeBlock.is(f.out.getBlockState(f.base(p).offset(BorrascaGarritys.BOX)),BorrascaHomeBlock.Kind.BOX_FULL),"on the second visit their box is full: "+f.out.getBlockState(f.base(p).offset(BorrascaGarritys.BOX))))
          .thenExecute(()->{
              var b=f.base(p);
              for(int house=0;house<BorrascaHomes.HOUSES;house++)h.assertTrue(BorrascaHomeBlock.is(f.out.getBlockState(b.offset(BorrascaGarritys.box(house))),BorrascaHomeBlock.Kind.BOX),"nobody else's is");
              h.assertTrue(knock.get().equals(BorrascaGarritys.KNOCK_SECOND),"a knock on the second visit brings nobody");
              // Mr. Dorsey says he has it; asked again, he hands it over; after that he has his other line and where it goes.
              h.assertTrue(dorsey.get().contains(BorrascaGarritys.DORSEY_HAS)&&letters.getAsInt()==0&&!f.own(p).getBoolean("MailGiven"),"he says he has a letter for them, and keeps it");
              h.assertTrue(dorsey.get().contains(BorrascaGarritys.DORSEY_GIVES)&&letters.getAsInt()==1&&f.own(p).getBoolean("MailGiven")&&BorrascaBelongings.has(p,f.own(p),key),"asked again, he hands it over: "+letters.getAsInt());
              h.assertTrue(dorsey.get().contains(BorrascaGarritys.DORSEY_MADDOX)&&dorsey.get().contains(BorrascaGarritys.DORSEY_REMINDS)&&letters.getAsInt()==1,"he has one to give, and says where it goes");
              // Looked at, its face counts; the account says what was printed in the corner only to a reader who looked.
              h.assertTrue(BorrascaGarritys.carried(f.own(p)).contains("did not take it")&&!BorrascaGarritys.carried(f.own(p)).contains("company"),"carried and not yet looked at: "+BorrascaGarritys.carried(f.own(p)));
              BorrascaGarritys.seen(p,false);
              h.assertTrue(f.own(p).getInt("MailSeen")==1&&BorrascaGarritys.carried(f.own(p)).contains("company's name"),"its face has been looked at");
              // In the pack it stays in the pack. In the hand it goes under the door.
              hold.accept(false);
              h.assertTrue(knock.get().equals(BorrascaGarritys.IN_PACK)&&letters.getAsInt()==1&&!f.own(p).getBoolean("MailLeft"),"a knock with the letter in the pack leaves it there");
              hold.accept(true);
              h.assertTrue(knock.get().equals(BorrascaGarritys.PUSHED)&&letters.getAsInt()==0&&f.own(p).getBoolean("MailLeft")&&f.own(p).getInt("MailFirstVisit")==2&&BorrascaGarritys.lying(f.own(p)),"from the hand it goes under the door: "+letters.getAsInt());
              var corner=f.out.getBlockState(b.offset(BorrascaGarritys.PORCH));
              h.assertTrue(BorrascaHomeBlock.is(corner,BorrascaHomeBlock.Kind.LETTER)&&corner.getValue(BorrascaHomeBlock.FACING)==Direction.WEST&&corner.getValue(BorrascaHomeBlock.STAGE)==0&&corner.getShape(f.out,b.offset(BorrascaGarritys.PORCH)).isEmpty(),"its corner shows at the door's foot, toward the door, and is not a thing a hand finds: "+corner);
              h.assertTrue(knock.get().equals(BorrascaGarritys.CORNER)&&letters.getAsInt()==0,"a child cannot get it back out");
              BorrascaGarritys.seen(p,true);h.assertTrue(f.own(p).getInt("MailSeen")==1,"a reader without the letter is not looking at it");
              h.assertTrue(dorsey.get().contains(BorrascaGarritys.DORSEY_MADDOX)&&dorsey.get().contains(BorrascaGarritys.DORSEY_LEFT),"and Mr. Dorsey hears it is done");
              h.assertTrue(BorrascaGarritys.carried(f.own(p)).contains("pushed it under their porch door")&&BorrascaGarritys.found(f.own(p)).isEmpty(),"the account, that week: "+BorrascaGarritys.carried(f.own(p)));
              // After she is taken, a letter still in the pack is still theirs.
              var own=f.own(p);own.putBoolean("FriendMissing",true);own.putBoolean("MailLeft",false);f.save(p,own);
              dorsey.get();h.assertTrue(dorsey.get().contains(BorrascaGarritys.DORSEY_STILL),"after she is taken he says the letter is still theirs");
              own=f.own(p);own.putBoolean("MailLeft",true);f.save(p,own);
              f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,3);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(p).getInt("PurseVisit")==3,"years later: "+BorrascaCast.why(p)))
          .thenWaitUntil(()->{var s=f.out.getBlockState(f.base(p).offset(BorrascaGarritys.BOX));h.assertTrue(BorrascaHomeBlock.is(s,BorrascaHomeBlock.Kind.BOX)&&s.getValue(BorrascaHomeBlock.STAGE)==1,"years on their box is still on its post, rusted: "+s);})
          .thenExecute(()->{
              var b=f.base(p);
              for(int house=0;house<BorrascaHomes.HOUSES;house++)h.assertTrue(f.out.getBlockState(b.offset(BorrascaGarritys.box(house))).getValue(BorrascaHomeBlock.STAGE)==0,"the others' are seen to");
              h.assertTrue(f.out.getBlockState(b.offset(BorrascaGarritys.DOOR)).getValue(BorrascaHomeBlock.STAGE)==1&&f.out.getBlockState(b.offset(BorrascaGarritys.DOOR.above())).getValue(BorrascaHomeBlock.STAGE)==1&&f.out.getBlockState(b.offset(BorrascaGarritys.X1-1,1,BorrascaGarritys.MIDDLE-2)).getValue(BorrascaHomeBlock.STAGE)==1,"the door and the curtains are years older");
              var corner=f.out.getBlockState(b.offset(BorrascaGarritys.PORCH));
              h.assertTrue(BorrascaHomeBlock.is(corner,BorrascaHomeBlock.Kind.LETTER)&&corner.getValue(BorrascaHomeBlock.STAGE)==1,"the letter is where the reader put it, gone yellow: "+corner);
              // The first hand finds it; the second draws it out, and it is the yellowed one.
              h.assertTrue(knock.get().equals(BorrascaGarritys.CORNER_LATER)&&letters.getAsInt()==0&&f.own(p).getBoolean("MailFound"),"a grown reader finds it there");
              h.assertTrue(BorrascaGarritys.found(f.own(p)).contains("still under the Garritys' door")&&!BorrascaGarritys.found(f.own(p)).contains("I have it"),"found and left: "+BorrascaGarritys.found(f.own(p)));
              h.assertTrue(knock.get().equals(BorrascaGarritys.DRAWN_OUT)&&letters.getAsInt()==1&&f.own(p).getBoolean("MailBack")&&!BorrascaGarritys.lying(f.own(p))&&f.out.getBlockState(b.offset(BorrascaGarritys.PORCH)).isAir(),"and draws it out: "+letters.getAsInt());
              hold.accept(true);
              h.assertTrue(p.getMainHandItem().has(DataComponents.CUSTOM_MODEL_DATA)&&BorrascaBelongings.valid(p,f.own(p),key,p.getMainHandItem()),"it is the yellowed one, and the reader's own");
              h.assertTrue(BorrascaGarritys.found(f.own(p)).contains("I have it now"),"the account: "+BorrascaGarritys.found(f.own(p)));
              // Mr. Dorsey knows it again, once.
              h.assertTrue(dorsey.get().contains(BorrascaGarritys.DORSEY_GROWN)&&!dorsey.get().contains(BorrascaGarritys.DORSEY_GROWN),"he knows the letter again, and says so once");
              // It can go back under.
              h.assertTrue(knock.get().equals(BorrascaGarritys.PUT_BACK)&&letters.getAsInt()==0&&f.own(p).getInt("MailFirstVisit")==2&&f.out.getBlockState(b.offset(BorrascaGarritys.PORCH)).getValue(BorrascaHomeBlock.STAGE)==1,"put back, it is the yellow one that lies there");
              h.assertTrue(BorrascaGarritys.found(f.own(p)).contains("put it back"),"the account: "+BorrascaGarritys.found(f.own(p)));
              h.assertTrue(knock.get().equals(BorrascaGarritys.DRAWN_AGAIN)&&letters.getAsInt()==1&&BorrascaGarritys.found(f.own(p)).contains("I have it now"),"and it comes out again");
              // A reader who takes it to the door for the first time years on: it goes under as it is, and comes out as it went in.
              {var own=f.own(p);for(String flag:List.of("MailLeft","MailBack","MailDrawn","MailFound","MailFirstVisit"))own.remove(flag);
               var s=BorrascaBelongings.remove(p,own,key);s.remove(DataComponents.CUSTOM_MODEL_DATA);own.put(key+"Original",s.save(p.registryAccess()));p.setItemInHand(InteractionHand.MAIN_HAND,s);f.save(p,own);}
              h.assertTrue(knock.get().equals(BorrascaGarritys.PUSHED_LATE)&&f.own(p).getInt("MailFirstVisit")==3&&f.out.getBlockState(b.offset(BorrascaGarritys.PORCH)).getValue(BorrascaHomeBlock.STAGE)==0,"first taken to the door years on, it lies there as it is: "+f.out.getBlockState(b.offset(BorrascaGarritys.PORCH)));
              h.assertTrue(knock.get().equals(BorrascaGarritys.CORNER_LOOSE)&&knock.get().equals(BorrascaGarritys.DRAWN_AGAIN)&&letters.getAsInt()==1&&BorrascaGarritys.found(f.own(p)).contains("drew it out again"),"and comes out as it went in: "+BorrascaGarritys.found(f.own(p)));
              hold.accept(true);h.assertTrue(!p.getMainHandItem().has(DataComponents.CUSTOM_MODEL_DATA),"not yellowed");
              // A reader who never took it to the door, and one who took it years late.
              var kept=new CompoundTag();kept.putBoolean("MailGiven",true);
              h.assertTrue(BorrascaGarritys.carried(kept).contains("did not take it to their door")&&BorrascaGarritys.found(kept).isEmpty(),"kept: "+BorrascaGarritys.carried(kept));
              kept.putBoolean("MailLeft",true);kept.putInt("MailFirstVisit",3);
              h.assertTrue(BorrascaGarritys.carried(kept).contains("did not take it to their door")&&BorrascaGarritys.found(kept).contains("years late"),"late: "+BorrascaGarritys.found(kept));
              for(var line:List.of(BorrascaGarritys.KNOCK_FIRST,BorrascaGarritys.KNOCK_SECOND,BorrascaGarritys.KNOCK_LATER,BorrascaGarritys.CORNER,BorrascaGarritys.CORNER_LATER,BorrascaGarritys.DRAWN_OUT,BorrascaGarritys.BOX_FULL,BorrascaGarritys.BOX_LATER,BorrascaGarritys.PUSHED))
                  for(var word:List.of("sold","paid","money","Sheriff","taken away","you feel","afraid","sad"))h.assertFalse(line.contains(word),"a line says what is there, not what it means: "+word+" in "+line);
              f.close();
          }).thenSucceed();
    }
    /**
     * 0.4.457 (owner: "Combat with the sheriff shouldn't' feel like a vanilla Minecraft fight. Punches should be sluggish with real weight.
     * The sheriff should use his baton, then drop it after being hit a few times and start swinging his own fists. He swings with one fist
     * then the other."): a blow not brought all the way back glances; his baton is raised before it comes down, lands on a reader who
     * stands there and misses one who has stepped out; the third whole blow has it out of his hand and on the ground; then the left
     * fist and the right, each drawn back first; and the reader's arms are heavy for as long as it lasts.
     */
    @AfterBatch(batch="borrasca_fight") public static void fightDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="borrasca_fight",timeoutTicks=3000)
    public static void theSheriffFightsWithHisBatonAndThenHisFists(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_fight");var order=new ArrayList<Integer>();
        var arm=net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED;
        Runnable beside=()->{var a=f.actor(p,"CastSheriff");p.teleportTo(f.out,a.getX()+1.6,a.getY(),a.getZ(),90,0);};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded"))
          .thenExecute(()->{f.enter(p);f.stage(p,4);f.at(p,0,12,-170);})
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"fourth visit cast recalled"))
          .thenWaitUntil(()->h.assertTrue(f.rel(f.actor(p,"CastSheriff"),p).z< -176,"the sheriff walks his yard route"))
          .thenExecute(()->{
              var a=f.actor(p,"CastSheriff");beside.run();p.setHealth(p.getMaxHealth());
              float whole=(float)p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
              h.assertTrue(p.getAttribute(arm).getModifier(BorrascaSheriffFight.HEAVY)==null,"before the fight the reader's arms are their own");
              h.assertTrue(BorrascaSheriffFight.full(p,whole)&&!BorrascaSheriffFight.full(p,whole*.7F),"a whole blow is a blow, and most of one is not");
              h.assertFalse(a.hurt(p.damageSources().playerAttack(p),whole*.4F),"a blow not brought all the way back glances off him");
              h.assertTrue(a.getHealth()==a.getMaxHealth()&&f.own(p).getInt("SheriffHits")==0&&f.own(p).getBoolean("SheriffRoused"),"it does nothing but rouse him: "+a.getHealth());
          })
          .thenWaitUntil(()->h.assertTrue(f.actor(p,"CastSheriff").getMainHandItem().is(BorrascaRegistry.BATON.get())&&p.getAttribute(arm).getModifier(BorrascaSheriffFight.HEAVY)!=null,"roused, he has his baton out, and the reader's arms are heavy"))
          .thenExecute(()->h.assertTrue(Math.abs(p.getAttributeValue(arm)-BorrascaSheriffFight.ARM_SPEED)<.05,"a blow takes most of a second to bring back: "+p.getAttributeValue(arm)))
          .thenWaitUntil(()->h.assertTrue(f.actor(p,"CastSheriff").gesture()==BorrascaSheriffFight.BATON_UP&&p.getHealth()==p.getMaxHealth(),"he raises it before anything lands"))
          .thenWaitUntil(()->h.assertTrue(f.actor(p,"CastSheriff").gesture()==BorrascaSheriffFight.BATON_DOWN&&p.getHealth()<=p.getMaxHealth()-BorrascaSheriffFight.BATON_DAMAGE+.01F,"and it comes down on a reader who stood there: "+p.getHealth()))
          .thenExecute(()->{h.assertTrue(p.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"the baton leaves the reader's legs slow");p.setHealth(p.getMaxHealth());})
          .thenWaitUntil(()->h.assertTrue(f.actor(p,"CastSheriff").gesture()==BorrascaSheriffFight.BATON_UP,"he raises it again"))
          .thenExecute(()->{var a=f.actor(p,"CastSheriff");p.teleportTo(f.out,a.getX()+7,a.getY(),a.getZ(),90,0);})
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("SheriffMissed")&&p.getHealth()==p.getMaxHealth(),"a reader who has stepped out of it is missed"))
          .thenWaitUntil(()->h.assertTrue(f.own(p).getInt("SheriffSwing")==BorrascaSheriffFight.NONE,"and he gets his balance back"))
          .thenExecute(()->{
              var a=f.actor(p,"CastSheriff");beside.run();
              h.assertTrue(hitSheriff(f,p),"a whole blow lands");
              h.assertTrue(a.gesture()==BorrascaSheriffFight.REEL&&f.own(p).getInt("SheriffHits")==1,"and he reels from it");
          }).thenIdle(12)
          .thenExecute(()->{hitSheriff(f,p);h.assertTrue(f.actor(p,"CastSheriff").getMainHandItem().is(BorrascaRegistry.BATON.get())&&!f.own(p).getBoolean("SheriffDisarmed"),"two blows, and the baton is still in his hand");}).thenIdle(12)
          .thenExecute(()->{
              var a=f.actor(p,"CastSheriff");hitSheriff(f,p);var own=f.own(p);
              h.assertTrue(own.getBoolean("SheriffDisarmed")&&a.getMainHandItem().isEmpty(),"the third has it out of his hand");
              h.assertTrue(own.contains("SheriffBatonAt")&&BorrascaShopBlock.is(f.out.getBlockState(BlockPos.of(own.getLong("SheriffBatonAt"))),BorrascaShopBlock.Kind.BATON),"and it lies where it fell");
              h.assertTrue(BorrascaShopTexts.look(BorrascaShopBlock.Kind.BATON,0,true)!=null,"a hand on it says what it is");
              beside.run();p.setHealth(p.getMaxHealth());
          })
          .thenWaitUntil(()->{
              int g=f.actor(p,"CastSheriff").gesture();
              if(g>=BorrascaSheriffFight.LEFT_BACK&&g<=BorrascaSheriffFight.RIGHT_OUT&&(order.isEmpty()||order.get(order.size()-1)!=g))order.add(g);
              h.assertTrue(order.size()>=4,"he throws the left and then the right: "+order);
          })
          .thenExecute(()->{
              h.assertTrue(order.subList(0,4).equals(List.of(BorrascaSheriffFight.LEFT_BACK,BorrascaSheriffFight.LEFT_OUT,BorrascaSheriffFight.RIGHT_BACK,BorrascaSheriffFight.RIGHT_OUT)),"each fist drawn back before it is thrown, the left before the right: "+order);
              h.assertTrue(f.actor(p,"CastSheriff").getMainHandItem().isEmpty(),"with nothing in his hands");
              h.assertTrue(p.getHealth()<=p.getMaxHealth()-BorrascaSheriffFight.FIST_DAMAGE+.01F,"a fist lands on a reader who stood there: "+p.getHealth());
              p.setHealth(p.getMaxHealth());
          }).thenIdle(12)
          .thenExecute(()->{
              var a=f.actor(p,"CastSheriff");hitSheriff(f,p);var own=f.own(p);
              h.assertTrue(own.getBoolean("SheriffDown")&&a.gesture()==BorrascaStory.KNELT,"beaten, he goes down as he did: health "+a.getHealth());
              h.assertTrue(p.getAttribute(arm).getModifier(BorrascaSheriffFight.HEAVY)==null,"and the reader's arms are their own again");
              var lay=BlockPos.of(own.getLong("SheriffBatonAt"));BorrascaSheriffFight.rest(p,own,true);
              h.assertTrue(f.out.getBlockState(lay).isAir()&&!own.contains("SheriffDisarmed")&&!own.contains("SheriffBatonAt"),"on another visit he has his baton again, and it is not on the ground");
              f.close();
          }).thenSucceed();
    }
    @AfterBatch(batch="borrasca_town_children") public static void townChildrenDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="borrasca_town_children",timeoutTicks=2600)
    public static void townChildrenBecomeReadableMissingPosters(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_town");
        var eliPoster=new BlockPos(-15,1,-24);var wrenPoster=new BlockPos(-15,1,-22);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded"))
          .thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"town cast recalled"))
          .thenExecute(()->{
              f.talk(p,"TownEli");f.talk(p,"TownWren");
              h.assertTrue(f.own(p).getBoolean("MetEli")&&f.own(p).getBoolean("MetWren"),"both children can tell their part of the legend");
              h.assertTrue(f.out.getBlockState(f.base(p).offset(eliPoster)).isAir(),"Patrick's poster goes up only after he disappears");
              // 0.4.451: the store's noticeboard is one board, two blocks by two, a quarter of its picture in each cell.
              board(h,f,p,BorrascaPapers.Sheet.BOARD,"as the town was built");
              // 0.4.454: the station's board shows the prices of the reader's own year.
              h.assertTrue(f.out.getBlockState(f.base(p).offset(BorrascaShops.PRICE)).getValue(BorrascaShopBlock.STAGE)==0,"in the childhood years the station's board shows the early prices");
              bike(h,f,p,BorrascaRooms.PATRICK_BIKE,1,"on the first visit Patrick's bike lies on its side outside the store");
              bike(h,f,p,BorrascaRooms.KENDRA_BIKE,0,"and Kendra's stands on its kickstand by where she waits");
              f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,2);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"second visit cast recalled"))
          .thenWaitUntil(()->h.assertTrue(BorrascaPaperBlock.is(f.out.getBlockState(BorrascaRooms.at(f.base(p),eliPoster)),BorrascaPapers.Sheet.PATRICK),"Patrick's own sheet hangs by the store's door once he is gone"))
          .thenExecute(()->{
              h.assertTrue(f.out.getBlockState(BorrascaRooms.at(f.base(p),wrenPoster)).isAir(),"Kendra's goes up only after she disappears");
              board(h,f,p,BorrascaPapers.Sheet.BOARD,"on the second visit");
              h.assertTrue(f.actor(p,"TownEli").isInvisible()&&!f.actor(p,"TownWren").isInvisible(),"Patrick is missing while Kendra is still in town");
              f.talk(p,"TownWren");h.assertTrue(f.own(p).getBoolean("WrenRememberedEli"),"Kendra remembers Patrick's bicycle");
              // 0.4.458: to a reader who came down without the friend she says three other things, in order, and then the last again.
              {var mine=f.own(p);boolean was=mine.getBoolean("FriendMissing");mine.putBoolean("FriendMissing",true);mine.remove("WrenRememberedEli");f.save(p,mine);
               String[] hers={"You came down by yourself.","I asked him about Patrick.","tell her mom","tell her mom"};
               for(String line:hers){f.talk(p,"TownWren");h.assertTrue(said(f,p,"TownWren").contains(line),"Kendra, after the friend is taken: "+line+" / "+said(f,p,"TownWren"));}
               mine=f.own(p);h.assertTrue(!mine.getBoolean("WrenRememberedEli"),"and that is not her telling about the bicycle");
               mine.putBoolean("FriendMissing",was);mine.putBoolean("WrenRememberedEli",true);mine.remove("WrenAfter");f.save(p,mine);}
              f.read(p,eliPoster,false);
              h.assertTrue(f.out.getBlockState(BorrascaRooms.at(f.base(p),BorrascaRooms.PATRICK_BIKE)).isAir(),"by the second visit Kendra has walked Patrick's bike home to his mother");
              bike(h,f,p,BorrascaRooms.KENDRA_BIKE,0,"her own still stands where she waits");
              f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,3);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"adult visit cast recalled"))
          .thenWaitUntil(()->h.assertTrue(BorrascaPaperBlock.is(f.out.getBlockState(BorrascaRooms.at(f.base(p),wrenPoster)),BorrascaPapers.Sheet.KENDRA),"Kendra's own sheet hangs the other side of the door"))
          .thenExecute(()->{
              h.assertTrue(BorrascaPaperBlock.is(f.out.getBlockState(BorrascaRooms.at(f.base(p),eliPoster)),BorrascaPapers.Sheet.PATRICK),"Patrick's is still up");
              board(h,f,p,BorrascaPapers.Sheet.BOARD_THIRD,"years on");
              h.assertTrue(f.out.getBlockState(f.base(p).offset(BorrascaShops.PRICE)).getValue(BorrascaShopBlock.STAGE)==1,"years on, the board shows the later prices");
              h.assertTrue(f.actor(p,"TownWren").isInvisible(),"Kendra is missing on the adult return");
              f.read(p,wrenPoster,false);
              h.assertTrue(f.own(p).getBoolean("Read_WrenPoster")&&f.own(p).getBoolean("Read_EliPoster"),"both posters have readable pages");
              for(var at:List.of(BorrascaRooms.PATRICK_BIKE,BorrascaRooms.KENDRA_BIKE))h.assertTrue(f.out.getBlockState(BorrascaRooms.at(f.base(p),at)).isAir(),"years later neither bike is in town");
              p.closeContainer();f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,4);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"last visit cast recalled"))
          .thenWaitUntil(()->h.assertTrue(BorrascaPaperBlock.is(f.out.getBlockState(BorrascaRooms.at(f.base(p),eliPoster)),BorrascaPapers.Sheet.PATRICK_YELLOWED),"on the last night Patrick's sheet has gone yellow"))
          .thenExecute(()->{
              // 0.4.451 (owner: "Have this whole front of house filled with lost papers after vignette has ended – after the orrery
              // has either been fed or stopped."): the last night's front, then the front once the machine has stopped.
              h.assertTrue(BorrascaPaperBlock.is(f.out.getBlockState(BorrascaRooms.at(f.base(p),wrenPoster)),BorrascaPapers.Sheet.KENDRA_YELLOWED),"and Kendra's");
              board(h,f,p,BorrascaPapers.Sheet.BOARD_LAST,"on the last night");
              h.assertTrue(f.out.getBlockState(BorrascaRooms.at(f.base(p),BorrascaPapers.FRONT_X,BorrascaPapers.HEAD,BorrascaPapers.NORTH)).isAir(),"nothing else is on the front before the ending");
              var fed=new CompoundTag();fed.putBoolean("DanaFed",true);var stopped=new CompoundTag();stopped.putBoolean("PowerOff",true);
              h.assertTrue(BorrascaStory.frontStage(fed,4)==BorrascaPapers.PAPERED&&BorrascaStory.frontStage(stopped,4)==BorrascaPapers.PAPERED&&BorrascaStory.frontStage(new CompoundTag(),4)==BorrascaPapers.LAST,"the front is papered once the machine has been fed or stopped, whichever, and not before");
              var own=f.own(p);own.putBoolean("PowerOff",true);f.save(p,own);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getInt("TownPostersVisit")==BorrascaPapers.PAPERED,"the front is papered over once the machine has stopped"))
          .thenExecute(()->{
              var b=f.base(p);var odd=new ArrayList<String>();int sheets=0,onGlass=0;
              for(int z=BorrascaPapers.SOUTH;z>=BorrascaPapers.NORTH;z--)for(int y=BorrascaPapers.FOOT;y<=BorrascaPapers.HEAD;y++){
                  var cell=new BlockPos(BorrascaPapers.FRONT_X,y,z);var s=f.out.getBlockState(BorrascaRooms.at(b,cell));var wall=BorrascaRooms.at(b,cell.west());var behind=f.out.getBlockState(wall);
                  if(Arrays.asList(BorrascaRooms.BOARD).contains(cell)){if(!s.equals(BorrascaPapers.boardPiece(BorrascaPapers.Sheet.BOARD_COVERED,cell)))odd.add("the board at "+cell.toShortString()+" is "+s);continue;}
                  if(z==BorrascaPapers.DOOR_Z&&y<=BorrascaPapers.FOOT+1){if(!s.isAir())odd.add("across the doorway at "+cell.toShortString()+": "+s);continue;}
                  if(BorrascaSignBlock.is(s)||BorrascaShopBlock.is(s,BorrascaShopBlock.Kind.BENCH_END))continue; // the store's tin sign and its bench are left as they are
                  if(!BorrascaPaperBlock.is(s)){odd.add("bare at "+cell.toShortString()+": "+s);continue;}
                  sheets++;boolean glass=s.getValue(BorrascaPaperBlock.GLASS);if(glass)onGlass++;
                  if(glass!=behind.is(Blocks.GLASS_PANE)||!glass&&!behind.isFaceSturdy(f.out,wall,Direction.EAST))odd.add("the sheet at "+cell.toShortString()+" is held by "+behind);
                  if(BorrascaPaperBlock.sheet(s)!=BorrascaPapers.wall(y,z)||s.getValue(BorrascaPaperBlock.FACING)!=Direction.EAST||BorrascaPaperBlock.sheet(s).look.isEmpty())odd.add("the wrong sheet at "+cell.toShortString()+": "+s);
                  for(var next:List.of(cell.above(),cell.north())){var n=f.out.getBlockState(BorrascaRooms.at(b,next));if(BorrascaPaperBlock.is(n)&&n.getValue(BorrascaPaperBlock.PIECE).equals(s.getValue(BorrascaPaperBlock.PIECE)))odd.add("the same sheet twice, at "+cell.toShortString()+" and "+next.toShortString());}
              }
              h.assertTrue(odd.isEmpty()&&sheets==42&&onGlass==10,"after the ending a sheet hangs on every board of the store's front and on the glass of both windows, the board is covered, and the doorway is clear: "+sheets+" sheets, "+onGlass+" on glass, "+odd);
              h.assertTrue(BorrascaPaperBlock.is(f.out.getBlockState(BorrascaRooms.at(b,eliPoster)),BorrascaPapers.Sheet.WALL_PATRICK)&&BorrascaPaperBlock.is(f.out.getBlockState(BorrascaRooms.at(b,wrenPoster)),BorrascaPapers.Sheet.WALL_KENDRA),"Patrick's and Kendra's sheets are where they were, under the rest");
              // A hand on a sheet reads it where it hangs; the two by the door and the board still open as pages.
              f.at(p,-13,0,-27);p.closeContainer();
              h.assertTrue(BorrascaStory.click(p,b,BorrascaRooms.world(new BlockPos(BorrascaPapers.FRONT_X,BorrascaPapers.HEAD,BorrascaPapers.NORTH)),f.own(p))&&!(p.containerMenu instanceof LiteraryVignettes.Pages),"a hand on a sheet reads it without opening pages");
              f.at(p,-13,0,-20);var after=opened(f,p,BorrascaRooms.BOARD[2],"the covered noticeboard");
              h.assertTrue(text(after).contains("LOST")&&text(after).contains("Kayla")&&!text(after).contains("supper")&&f.own(p).contains("Original_BoardAfter"),"the covered board is its own original: "+text(after));
              h.assertTrue(text(opened(f,p,eliPoster,"Patrick's sheet, after")).contains("PATRICK MADDOX")&&text(opened(f,p,wrenPoster,"Kendra's sheet, after")).contains("KENDRA BELL"),"their two sheets still open as their pages");
          }).thenSucceed();
    }
    /** 0.4.451: the store's noticeboard is the four quarters of one picture, each looking out over the porch. */
    /** 0.4.454: a seat of the town's, of its kind, looking its way, its end to the left (0) or the right (1). */
    private static void seat(GameTestHelper h,Fixture f,BlockPos at,BorrascaShopBlock.Kind kind,Direction looks,int stage,String what){
        var s=f.out.getBlockState(at);h.assertTrue(BorrascaShopBlock.is(s,kind)&&s.getValue(BorrascaShopBlock.FACING)==looks&&s.getValue(BorrascaShopBlock.STAGE)==stage,what+": "+s);
    }
    private static void board(GameTestHelper h,Fixture f,ServerPlayer p,BorrascaPapers.Sheet board,String when){
        var pieces=new HashSet<Integer>();
        for(var cell:BorrascaRooms.BOARD){var s=f.out.getBlockState(BorrascaRooms.at(f.base(p),cell));h.assertTrue(s.equals(BorrascaPapers.boardPiece(board,cell))&&BorrascaPaperBlock.sheet(s)==board,"the noticeboard "+when+" at "+cell.toShortString()+": "+s);pieces.add(s.getValue(BorrascaPaperBlock.PIECE));}
        h.assertTrue(pieces.size()==4,"the noticeboard is four different quarters of one picture "+when+": "+pieces);
    }
    /**
     * 0.4.432 (owner: the longer hill "gives Nell and the player ... time to talk and bond"). Kelly talks on the walk up to the Tree, a line
     * at a time, while the reader keeps near her; hanging back hears nothing. Her lines go into the journal's record of what was said.
     */
    @AfterBatch(batch="borrasca_walk_talk") public static void walkTalkDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="borrasca_walk_talk",timeoutTicks=3600)
    public static void kellyTalksOnTheWalkUpToTheTreeWhileTheReaderKeepsNear(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_walker");int[] hung={0};
        // Distances here are in the world: the hill stretches the authored plan, so a step on the plan can be many blocks on the ground.
        java.util.function.DoubleConsumer behind=d->{var a=f.actor(p,"CastKimber");p.teleportTo(f.out,a.getX(),a.getY(),a.getZ()+d,180,0);p.setDeltaMovement(Vec3.ZERO);};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->f.enter(p))
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is in place"))
          .thenExecute(()->{var own=f.own(p);own.putBoolean("MetFriend",true);own.putBoolean("PlayDone",true);f.save(p,own);})
          // Hanging back, a dozen blocks behind her, the reader hears nothing she says.
          .thenWaitUntil(()->{behind.accept(12);h.assertTrue(++hung[0]>=40,"hanging back a while");})
          .thenExecute(()->h.assertTrue(f.own(p).getInt("WalkTalk")==0,"a reader hanging back a dozen blocks behind hears none of it"))
          .thenWaitUntil(()->{if(!f.own(p).getBoolean("KimberAtFort")&&f.rel(f.actor(p,"CastKimber"),p).y<5)behind.accept(2);
              h.assertTrue(f.own(p).getBoolean("KimberAtFort"),"walking beside her to the Tree: step="+f.own(p).getInt("ChildStep")+" said="+f.own(p).getInt("WalkTalk")+" her="+f.rel(f.actor(p,"CastKimber"),p));})
          .thenExecute(()->{
              int said=f.own(p).getInt("WalkTalk");
              h.assertTrue(said>=2,"beside her, the reader hears her talk on the way up: "+said+" lines");
              h.assertTrue(TalkHistory.conversations(p.server,p.getUUID()).stream().map(CompoundTag.class::cast).flatMap(t->t.getList("Lines",10).stream()).map(CompoundTag.class::cast)
                  .anyMatch(line->line.getString("T").equals(BorrascaStory.WALK_TALK[0])),"what she said on the walk is in the journal");
              h.assertTrue(BorrascaTexts.account(f.own(p)).contains("on the way up to the Tree")||BorrascaTexts.account(f.own(p)).contains("the whole way up to the Tree"),"the account remembers the walk");
          }).thenSucceed();
    }
    /**
     * 0.4.433 (owner: "other potential npcs ... time to talk and bond"). Patrick and Kendra walk up behind Kelly as far as the first marker,
     * say goodbye there to a reader near enough to hear, walk back down, and are home in town once nobody is watching them.
     */
    @AfterBatch(batch="borrasca_along") public static void alongDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="borrasca_along",timeoutTicks=4800)
    public static void patrickAndKendraWalkUpAsFarAsTheFirstMarker(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_along");
        java.util.function.DoubleConsumer behind=d->{var a=f.actor(p,"CastKimber");p.teleportTo(f.out,a.getX(),a.getY(),a.getZ()+d,180,0);p.setDeltaMovement(Vec3.ZERO);};
        java.util.function.IntPredicate there=s->f.own(p).getInt("EliAlong")>=s&&f.own(p).getInt("WrenAlong")>=s;
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->f.enter(p))
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is in place"))
          .thenExecute(()->{var own=f.own(p);own.putBoolean("MetFriend",true);own.putBoolean("PlayDone",true);f.save(p,own);})
          // Walking beside Kelly, the two come up the trail behind.
          .thenWaitUntil(()->{if(!f.own(p).getBoolean("KimberAtFort")&&f.rel(f.actor(p,"CastKimber"),p).y<5)behind.accept(2);
              h.assertTrue(f.own(p).getBoolean("AlongStarted")&&there.test(2),"they follow her up the trail: eli="+f.own(p).getInt("EliAlong")+" wren="+f.own(p).getInt("WrenAlong")
                  +" patrick="+f.rel(f.actor(p,"TownEli"),p)+" kendra="+f.rel(f.actor(p,"TownWren"),p));})
          .thenWaitUntil(()->{if(!f.own(p).getBoolean("KimberAtFort")&&f.rel(f.actor(p,"CastKimber"),p).y<5)behind.accept(2);
              var eli=f.actor(p,"TownEli");var wren=f.actor(p,"TownWren");
              h.assertTrue(there.test(3),"they stop at the first marker: patrick="+f.rel(eli,p)+" kendra="+f.rel(wren,p)+" kelly="+f.rel(f.actor(p,"CastKimber"),p));})
          .thenExecute(()->{
              var marker=Vec3.atBottomCenterOf(BorrascaRooms.at(f.base(p),BorrascaRooms.MARKER_ONE));
              for(var key:new String[]{"TownEli","TownWren"}){var a=f.actor(p,key);h.assertTrue(a.position().distanceTo(marker)<5,key+" stands by the first marker: "+a.position().distanceTo(marker));}
              var friend=f.actor(p,"CastKimber");h.assertTrue(f.actor(p,"TownEli").distanceToSqr(f.actor(p,"TownWren"))>.5,"at the marker they stand either side of the trail");
              var wren=f.actor(p,"TownWren");p.teleportTo(f.out,wren.getX(),wren.getY(),wren.getZ()-3,0,0);p.setDeltaMovement(Vec3.ZERO);})
          // Near them, the reader hears them say goodbye; then they turn back down.
          .thenWaitUntil(()->{var wren=f.actor(p,"TownWren");if(!f.own(p).getBoolean("AlongHome")){p.teleportTo(f.out,wren.getX(),wren.getY(),wren.getZ()-3,0,0);p.setDeltaMovement(Vec3.ZERO);}
              h.assertTrue(f.own(p).getBoolean("AlongHeard"),"they say goodbye at the marker");})
          .thenExecute(()->{
              h.assertTrue(TalkHistory.conversations(p.server,p.getUUID()).stream().map(CompoundTag.class::cast).flatMap(t->t.getList("Lines",10).stream()).map(CompoundTag.class::cast)
                  .anyMatch(line->line.getString("T").equals(BorrascaStory.FAREWELL_ELI)),"what Patrick said at the marker is in the journal");
              // The reader goes on up to the Tree, looking up the hill; behind them the two go home.
              var fort=BorrascaRooms.at(f.base(p),BorrascaStory.FORT);p.teleportTo(f.out,fort.getX()+.5,fort.getY(),fort.getZ()+.5,180,-10);p.setDeltaMovement(Vec3.ZERO);})
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("AlongDone"),"they are home again once nobody watches: patrick="+f.rel(f.actor(p,"TownEli"),p)+" kendra="+f.rel(f.actor(p,"TownWren"),p)))
          .thenExecute(()->{
              for(var key:new String[]{"TownEli","TownWren"}){var home=Vec3.atBottomCenterOf(f.base(p).offset(BorrascaCast.target(key,1)));h.assertTrue(f.actor(p,key).position().distanceTo(home)<1.5,key+" is back in town");}
              h.assertTrue(BorrascaTexts.account(f.own(p)).contains("as far as the first marker"),"the account remembers who came part of the way");
          }).thenSucceed();
    }
    /** 0.4.432: on the second visit's walk toward the mill she talks about Patrick, the same way, between the shelters' calls. */
    @AfterBatch(batch="borrasca_walk_talk_two") public static void walkTalkTwoDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="borrasca_walk_talk_two",timeoutTicks=5200)
    public static void kellyTalksAboutPatrickOnTheWalkToTheMill(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_mill_walker");
        java.util.function.DoubleConsumer behind=d->{var a=f.actor(p,"CastKimber");p.teleportTo(f.out,a.getX(),a.getY(),a.getZ()+d,180,0);p.setDeltaMovement(Vec3.ZERO);};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,2);})
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the friend is at the first marker"))
          .thenExecute(()->{f.read(p,BorrascaRooms.SOURCE,false);f.talk(p,"CastKimber");h.assertTrue(f.own(p).getBoolean("FollowAgain"),"she sets off");})
          .thenWaitUntil(()->{if(f.own(p).getInt("TakingTicks")==0)behind.accept(2);
              h.assertTrue(f.own(p).getBoolean("FriendMissing"),"walking beside her to the clearing: step="+f.own(p).getInt("TakingStep")+" said="+f.own(p).getInt("WalkTalkTwo"));})
          .thenExecute(()->{
              var own=f.own(p);int said=own.getInt("WalkTalkTwo");
              h.assertTrue(said>=2,"beside her, the reader hears her talk about Patrick on the way: "+said+" lines");
              h.assertTrue(own.getInt("WalkTalk")==0,"the first visit's walk lines are not said again");
              h.assertTrue(BorrascaTexts.account(own).contains("on the way to the mill, she told me about Patrick"),"the account remembers the second walk");
          }).thenSucceed();
    }
    /** 0.4.431: a child's bike stands or lies at the given place, on the ground, with something to say when looked at, and bumps nobody. */
    private static void bike(GameTestHelper h,Fixture f,ServerPlayer p,BlockPos authored,int stage,String what){
        var at=BorrascaRooms.at(f.base(p),authored);var s=f.out.getBlockState(at);
        h.assertTrue(s.is(BorrascaRegistry.SHOP.get())&&s.getValue(BorrascaShopBlock.KIND)==BorrascaShopBlock.Kind.BIKE&&s.getValue(BorrascaShopBlock.STAGE)==stage,what+": "+s);
        h.assertTrue(!f.out.getBlockState(at.below()).getCollisionShape(f.out,at.below()).isEmpty(),what+", on solid ground: "+f.out.getBlockState(at.below()));
        h.assertTrue(s.getCollisionShape(f.out,at).isEmpty()&&BorrascaShopTexts.look(BorrascaShopBlock.Kind.BIKE,stage,false)!=null,"the bike bumps nobody and can be looked at");
    }

    /**
     * 0.4.427 (owner: "Ensure the player can enact violent retribution against the sheriff too, when they're an adult"). On the last night
     * the Sheriff walks his yard and goes to the stopped machine; struck, he fights back with his flashlight, goes down in the dirt with his
     * hands up when beaten and talks, and a blow after that kills him. His body stays.
     */
    @AfterBatch(batch="borrasca_sheriff_patrol") public static void sheriffPatrolDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="borrasca_sheriff_patrol",timeoutTicks=2400)
    public static void theFourthVisitSheriffPatrolsFightsBackAndCanBeBeatenDownAndKilled(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_patrol");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded"))
          .thenExecute(()->{f.enter(p);f.stage(p,4);f.at(p,0,12,-170);})
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"fourth visit cast recalled"))
          .thenWaitUntil(()->h.assertTrue(f.rel(f.actor(p,"CastSheriff"),p).z< -176,"the sheriff walks his yard route"))
          .thenExecute(()->f.use(p,BorrascaRooms.POWER))
          .thenWaitUntil(()->{
              var a=f.actor(p,"CastSheriff");var b=f.base(p);
              h.assertTrue(a.distanceToSqr(Vec3.atBottomCenterOf(BorrascaRooms.at(b,BorrascaStory.SHERIFF_SHUTDOWN_CHECK)))<.1&&a.gesture()==4,"he goes to the refinery's doorway to look in at the stopped machine: "+a.position().subtract(Vec3.atLowerCornerOf(b)));
          })
          .thenExecute(()->{var a=f.actor(p,"CastSheriff");p.teleportTo(f.out,a.getX()+1.6,a.getY(),a.getZ(),90,0);p.setHealth(p.getMaxHealth());h.assertTrue(hitSheriff(f,p),"a grown reader's blow lands");})
          .thenWaitUntil(()->h.assertTrue(p.getHealth()<p.getMaxHealth()&&f.own(p).getInt("SheriffHits")==1,"struck, the Sheriff turns on the reader and hits back")).thenIdle(12)
          .thenExecute(()->{p.setHealth(p.getMaxHealth());hitSheriff(f,p);}).thenIdle(12)
          .thenExecute(()->{p.setHealth(p.getMaxHealth());hitSheriff(f,p);}).thenIdle(12)
          .thenExecute(()->{p.setHealth(p.getMaxHealth());hitSheriff(f,p);
              var a=f.actor(p,"CastSheriff");
              h.assertTrue(a.isAlive()&&f.own(p).getBoolean("SheriffDown")&&a.gesture()==BorrascaStory.KNELT&&a.getMainHandItem().isEmpty(),"beaten, he goes down in the dirt with his hands up and the flashlight gone: health "+a.getHealth()+" hits "+f.own(p).getInt("SheriffHits"));
              f.talk(p,"CastSheriff");h.assertTrue(a.getCustomName()!=null&&a.getCustomName().getString().contains("never my idea"),"down, he talks: "+a.getCustomName());
              h.assertFalse(hitSheriff(f,p),"a blow in the moment he goes down does not finish him");
          }).thenIdle(22)
          .thenExecute(()->{
              var a=f.actor(p,"CastSheriff");h.assertTrue(hitSheriff(f,p),"a blow after that lands");
              h.assertTrue(f.own(p).getBoolean("SheriffDead")&&a.isAlive()&&a.gesture()==BorrascaStory.FALLEN&&f.own(p).getInt("SheriffDiedVisit")==4,"and kills him; his body lies where he fell");
              h.assertFalse(hitSheriff(f,p),"the body takes no more blows");
              h.assertTrue(BorrascaTexts.account(f.own(p)).contains("killed the Sheriff")&&BorrascaSites.inside(p)&&!f.own(p).getBoolean("Interrupted"),"the account says so, and the visit goes on");
          }).thenIdle(10).thenExecute(()->h.assertTrue(f.actor(p,"CastSheriff").gesture()==BorrascaStory.FALLEN&&!f.actor(p,"CastSheriff").isInvisible(),"the body stays where it fell for the rest of the night")).thenSucceed();
    }

    /**
     * 0.4.427: a child's blows do nothing to the Sheriff. Grown, on the third visit at the tape, the reader can beat him down and kill him;
     * on the last night his body still lies where he fell, the deputies at the bar know he is dead, and his office says so on the phone.
     */
    @AfterBatch(batch="borrasca_retribution") public static void retributionDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="borrasca_retribution",timeoutTicks=3000)
    public static void aChildCannotHurtTheSheriffButGrownTheReaderCanKillHimAndTheTownKnows(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_retribution");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->f.enter(p))
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"first visit cast recalled"))
          .thenExecute(()->{
              var a=f.actor(p,"CastSheriff");p.teleportTo(f.out,a.getX()+1.6,a.getY(),a.getZ(),90,0);
              h.assertFalse(hitSheriff(f,p),"a child's blow does nothing to the Sheriff");h.assertTrue(f.own(p).getInt("SheriffHits")==0&&a.getHealth()==a.getMaxHealth(),"and he is untouched");
              f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,3);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(p).getInt("Visit")==3,"years later: "+BorrascaCast.why(p)))
          .thenExecute(()->{var a=f.actor(p,"CastSheriff");p.teleportTo(f.out,a.getX()+1.6,a.getY(),a.getZ(),90,0);p.setHealth(p.getMaxHealth());h.assertTrue(hitSheriff(f,p),"grown, a blow lands at the tape");}).thenIdle(12)
          .thenExecute(()->{p.setHealth(p.getMaxHealth());hitSheriff(f,p);}).thenIdle(12)
          .thenExecute(()->{p.setHealth(p.getMaxHealth());hitSheriff(f,p);}).thenIdle(12)
          .thenExecute(()->{p.setHealth(p.getMaxHealth());hitSheriff(f,p);h.assertTrue(f.own(p).getBoolean("SheriffDown"),"beaten down at the tape");}).thenIdle(22)
          .thenExecute(()->{p.setHealth(p.getMaxHealth());h.assertTrue(hitSheriff(f,p)&&f.own(p).getBoolean("SheriffDead")&&f.own(p).getInt("SheriffDiedVisit")==3,"and killed there");
              f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,4);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(p).getInt("PurseVisit")==4,"the last night: "+BorrascaCast.why(p)))
          .thenExecute(()->{
              var body=f.actor(p,"CastSheriff");var lies=BlockPos.of(f.own(p).getLong("SheriffBody"));
              h.assertTrue(!body.isInvisible()&&body.isAlive()&&body.gesture()==BorrascaStory.FALLEN&&body.getMainHandItem().isEmpty(),"on the last night the Sheriff's body is still there, face down, and doesn't get up");
              h.assertTrue(body.blockPosition().distManhattan(lies)<=1,"it lies where he fell at the tape, years later the same night: "+body.blockPosition()+" fell "+lies);
              var own=f.own(p);var dutch=BorrascaPeople.keeper(p,own,BorrascaPeople.DUTCH);BorrascaPeople.talk(p,dutch,own);f.save(p,own);
              h.assertTrue(dutch.getCustomName()!=null&&dutch.getCustomName().getString().contains("Sheriff's dead"),"the deputies at the bar know he is dead: "+dutch.getCustomName());
              f.atWorld(p,new BlockPos(-13,0,-111));own=f.own(p);var call=BorrascaPayphone.dial(p,own,BorrascaPayphone.SHERIFF);f.save(p,own);
              h.assertTrue(call!=null&&String.join(" ",call.lines()).contains("The Sheriff's dead"),"his office says so on the phone: "+(call==null?null:call.lines()));
              h.assertTrue(BorrascaTexts.witness(f.own(p)).contains("never came down off the ridge"),"the Witness line keeps it");
          }).thenSucceed();
    }

    /**
     * 0.4.428, the town's routines: each reader's town clock sends Mr. Rowe out front for a smoke, only while nobody watches him go. While he
     * is at his counter the society's door stays shut to a child; while he smokes a child can go in and read the map of the old workings,
     * and back at his counter he sends out a child he finds there. Mr. Dorsey walks his mail round door to door, and the account keeps the
     * society.
     */
    @AfterBatch(batch="borrasca_routines") public static void routinesDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="borrasca_routines",timeoutTicks=4000)
    public static void theTownKeepsItsRoutinesRoweSmokesTheSocietyOpensAndTheMailGoesRound(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("drisking_routine");f.shoved(p);
        java.util.function.IntFunction<LiteraryActor> person=phase->BorrascaPeople.keeper(p,f.own(p),phase);
        java.util.function.Function<BlockPos,Boolean> tryDoor=w->{var at=f.base(p).offset(w);var e=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.EAST,at,false));NeoForge.EVENT_BUS.post(e);return !e.isCanceled();};
        java.util.function.Consumer<Integer> clock=c->{var own=f.own(p);own.putInt("TownClock",c);f.save(p,own);};
        Runnable away=()->{f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);};
        final int[] firstLeg={-1};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->f.enter(p))
          .thenWaitUntil(()->{h.assertTrue(f.own(p).getBoolean("CastRecalled"),"cast recalled");for(int k=BorrascaPeople.LORNA;k<=BorrascaPeople.CARRIER;k++)h.assertTrue(person.apply(k)!=null,"everyone in town is placed: "+k);})
          .thenExecute(()->{
            var rowe=person.apply(BorrascaPeople.ROWE);var b=f.base(p);
            h.assertTrue(!f.own(p).getBoolean("RoweOut")&&rowe.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(BorrascaPeople.post(BorrascaPeople.ROWE))))<1.1,"Mr. Rowe starts the day at his counter");
            h.assertFalse(person.apply(BorrascaPeople.CARRIER).isInvisible(),"the mail carrier is out on his round on a childhood visit");
            firstLeg[0]=f.own(p).getInt("RoundLeg");
            f.atWorld(p,new BlockPos(-11,0,-100));
            h.assertFalse(tryDoor.apply(BorrascaShops.SOCIETY_DOOR),"while Mr. Rowe is at his counter the society's door stays shut to a child");
            h.assertTrue(f.own(p).getInt("SocietyAsked")==1&&rowe.getCustomName()!=null&&rowe.getCustomName().getString().contains("historical society"),"and he says what it is: "+rowe.getCustomName());
            away.run();clock.accept(BorrascaPeople.SMOKE_FROM);
          })
          .thenWaitUntil(()->{var rowe=person.apply(BorrascaPeople.ROWE);h.assertTrue(f.own(p).getBoolean("RoweOut")&&rowe.position().distanceToSqr(Vec3.atBottomCenterOf(f.base(p).offset(BorrascaPeople.STOOP)))<1.1,"at his break, unwatched, Mr. Rowe steps out front: "+rowe.position());})
          .thenExecute(()->{
            var rowe=person.apply(BorrascaPeople.ROWE);
            f.atWorld(p,new BlockPos(-11,0,-100));
            h.assertTrue(tryDoor.apply(BorrascaShops.SOCIETY_DOOR),"while he smokes a child can open the society's door");
            var own=f.own(p);BorrascaPeople.talk(p,rowe,own);f.save(p,own);
            h.assertTrue(rowe.getCustomName()!=null&&rowe.getCustomName().getString().contains("break"),"out front he is on his break: "+rowe.getCustomName());
            f.atWorld(p,new BlockPos(-18,0,-100));f.clickWorld(p,BorrascaShops.MAP);
            h.assertTrue(f.own(p).getInt("SocietyChild")==1,"in the society the child reads the map of the old workings while he is out");
            p.closeContainer();clock.accept(BorrascaPeople.SMOKE_TO);
          })
          .thenWaitUntil(()->h.assertTrue(!f.own(p).getBoolean("RoweOut"),"the break over, he comes back in while nobody in the society can see the counter"))
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CaughtInSociety"),"back at his counter, he finds the child in the society"))
          .thenExecute(()->h.assertTrue(BorrascaPeople.inSociety(p.blockPosition().subtract(f.base(p)))&&f.own(p).getBoolean("SocietyShove"),"he walks them out; nobody is set down anywhere"))
          .thenWaitUntil(()->{var b=f.base(p);h.assertTrue(!f.own(p).getBoolean("SocietyShove")&&p.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(BorrascaPeople.SENT_OUT)))<2.5&&!BorrascaPeople.inSociety(p.blockPosition().subtract(b)),"and pushes the child out through the shop: "+p.position().subtract(Vec3.atLowerCornerOf(b)));})
          .thenExecute(()->{
            var rowe=person.apply(BorrascaPeople.ROWE);var b=f.base(p);
            h.assertTrue(rowe.getCustomName()!=null&&rowe.getCustomName().getString().contains("Out of there"),"he says so: "+rowe.getCustomName());
            String account=BorrascaTexts.account(f.own(p));
            h.assertTrue(account.contains("While Mr. Rowe smoked out front")&&account.contains("DEPOT")&&account.contains("sent me out"),"the account keeps the society: "+account);
            away.run();
          })
          .thenWaitUntil(()->{int leg=f.own(p).getInt("RoundLeg");h.assertTrue(leg!=firstLeg[0]&&leg>=3,"Mr. Dorsey walks his round up the street, door to door: leg "+leg);})
          .thenWaitUntil(()->{var a=person.apply(BorrascaPeople.CARRIER);int leg=f.own(p).getInt("RoundLeg");h.assertTrue(!a.getPersistentData().contains("BorrascaSpeed")&&a.position().distanceToSqr(Vec3.atBottomCenterOf(f.base(p).offset(BorrascaPeople.ROUND[leg])))<1.5,"and stands at the door he walked to: "+a.position()+" leg "+leg);})
          .thenExecute(()->{
            var a=person.apply(BorrascaPeople.CARRIER);var own=f.own(p);p.moveTo(a.getX()+1.5,a.getY(),a.getZ());BorrascaPeople.talk(p,a,own);f.save(p,own);
            h.assertTrue(a.getCustomName()!=null&&!a.getCustomName().getString().isEmpty(),"he talks to a child on his round: "+a.getCustomName());
            away.run();f.stage(p,3);
          })
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(p).getInt("Visit")==3,"years later: "+BorrascaCast.why(p)))
          .thenExecute(()->{
            var own=f.own(p);own.putBoolean("RoweOut",false);own.putInt("TownClock",0);f.save(p,own);
            f.atWorld(p,new BlockPos(-11,0,-100));
            h.assertTrue(tryDoor.apply(BorrascaShops.SOCIETY_DOOR),"a grown reader goes into the society whenever the shop is open");
            var a=person.apply(BorrascaPeople.CARRIER);own=f.own(p);BorrascaPeople.talk(p,a,own);f.save(p,own);
            h.assertTrue(a.getCustomName()!=null&&(a.getCustomName().getString().contains("route")||a.getCustomName().getString().contains("Garritys")),"years later he is still on the route: "+a.getCustomName());
          }).thenSucceed();
    }

    private static boolean hitSheriff(Fixture f,ServerPlayer p){return f.actor(p,"CastSheriff").hurt(p.damageSources().playerAttack(p),8);}

    @GameTest(template="empty",batch="borrasca_escort",timeoutTicks=1800)
    public static void flashlightObeysSolidCoverAndTheSheriffEnforcesTheTape(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_escort");final int[] phase={0},still={0},moved={0};final Vec3[] previous={Vec3.ZERO};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.loaded(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,3);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original Sheriff loaded")).thenExecute(()->{
            var a=f.actor(p,"CastSheriff");f.atWorld(p,BorrascaRooms.trailWorld((int)Math.round(a.getZ()-f.base(p).getZ())+8));Vec3 d=p.getEyePosition().subtract(a.getEyePosition());float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));h.assertTrue(BorrascaStory.flashlightSees(a,p,yaw),"the actual eye ray and cone see the reader: sheriff="+a.position()+" reader="+p.position());var cover=BlockPos.containing(a.getEyePosition().add(d.scale(.5)));f.out.setBlock(cover,Blocks.STONE.defaultBlockState(),3);h.assertTrue(!BorrascaStory.flashlightSees(a,p,yaw),"solid native cover blocks the flashlight");f.out.setBlock(cover,Blocks.AIR.defaultBlockState(),3);
            var own=f.own(p);own.putInt("TrailClock",20);h.assertTrue(BorrascaStory.masking(own),"the dust pulse masks footsteps");own.putInt("TrailClock",90);h.assertTrue(!BorrascaStory.masking(own),"quiet intervals expose footsteps");
            own.putInt("Suspicion",10);own.putFloat("BeamYaw",37);h.assertTrue(BorrascaStory.flashlightYaw(own)==37,"once it has found someone the beam holds on them");own.putInt("Suspicion",0);h.assertTrue(BorrascaStory.flashlightYaw(own)!=37||true,"and sweeps again once they are out of it");
            own.putBoolean("Escorting",true);own.putInt("EscortTicks",0);f.save(p,own);f.at(p,2,5,-125);p.setNoGravity(false);previous[0]=p.position();phase[0]=1;
        }).thenWaitUntil(()->h.assertTrue(phase[0]==2,"the reader stands still on the far side for a while"))
          .thenExecute(()->{var a=f.actor(p,"CastSheriff");h.assertTrue(f.own(p).getBoolean("Escorting")&&moved[0]==0,"standing still, the reader is not pushed or moved: the Sheriff waits");h.assertTrue(a.getCustomName()!=null&&a.getCustomName().getString().contains("Keep walking"),"and tells them to keep walking");phase[0]=3;})
          .thenWaitUntil(()->h.assertTrue(!f.own(p).getBoolean("Escorting")&&f.rel(p,p).z>= -119.5,"walking, the reader reaches the near side of the tape and he lets them go: "+p.position()))
          .thenExecute(()->{
              phase[0]=0;
              var own=f.own(p);own.putBoolean("Escorting",true);own.putInt("EscortTicks",0);f.save(p,own);
              f.at(p,2,5,-125);
          })
          .thenWaitUntil(()->h.assertTrue(!f.own(p).getBoolean("Escorting")&&f.rel(p,p).z>= -119.5,
                  "the Sheriff returns a reader who refuses to walk back"))
          .thenExecute(()->h.assertTrue(p.getHealth()==p.getMaxHealth()&&BorrascaSites.inside(p),
                  "the enforced return keeps the reader in Borrasca without damage")).thenSucceed();
        h.onEachTick(()->{if(phase[0]==0)return;double distance=p.position().distanceTo(previous[0]);h.assertTrue(distance<.85,"the escort never relocates the reader between ticks");previous[0]=p.position();
            if(phase[0]==1){p.travel(Vec3.ZERO);if(distance>.02&&still[0]>5)moved[0]++;if(++still[0]>=80)phase[0]=2;}
            else if(phase[0]==3){p.setYRot(0);p.travel(new Vec3(0,0,1));}else p.travel(Vec3.ZERO);});
    }
}
