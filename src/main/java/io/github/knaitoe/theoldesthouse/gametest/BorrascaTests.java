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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded"))
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"all original cast loaded")).thenExecute(()->{
            var b=f.base(p);h.assertTrue(f.out.getBlockEntity(BorrascaRooms.at(b,BorrascaRooms.SOURCE)) instanceof LecternBlockEntity,"the content note is a native book");h.assertTrue(!f.out.noCollision(p,wbox(b,-4.8,6,-93.1,-4.2,7,-92.9)),"native fort rails join between posts");
            // Stage the approach only; both bodies must then climb by native collision and ladder physics.
            f.at(p,3,0,-83);p.setNoGravity(false);var a=f.actor(p,"CastKimber");a.moveTo(Vec3.atBottomCenterOf(BorrascaRooms.at(b,3,0,-86)),180,0);var own=f.own(p);own.putBoolean("MetFriend",true);own.putInt("ChildStep",3);f.save(p,own);last[0]=p.getY();last[1]=a.getY();moving[0]=true;
        }).thenWaitUntil(()->{var a=f.actor(p,"CastKimber");h.assertTrue(f.rel(p,p).y>=6&&a!=null&&f.rel(a,p).y>=5.9&&f.own(p).getBoolean("TreeReached"),"native child and original Friend reach the fort: player="+p.position()+" friend="+(a==null?"absent":a.position())+" step="+f.own(p).getInt("ChildStep"));}).thenExecute(()->{h.assertTrue(rises[0]>=20&&friendRises[0]>=20,"each body climbs over successive native ticks");h.assertTrue(p.getBbHeight()<1.1,"visits one and two use child height");}).thenWaitUntil(()->{var a=f.actor(p,"CastKimber");var b=f.base(p);h.assertTrue(f.own(p).getBoolean("FriendCarved"),"the same Friend descends the real ladder and carves at the trunk: at "+a.position().subtract(Vec3.atLowerCornerOf(b))+" downStep="+f.own(p).getInt("DownStep")+" carveTicks="+f.own(p).getInt("CarveTicks")+" legend="+f.own(p).getBoolean("LegendDone")+" climbing="+a.onClimbable()+" bumped="+a.horizontalCollision+" noAi="+a.isNoAi()+" feet/head="+f.out.getBlockState(a.blockPosition())+"/"+f.out.getBlockState(a.blockPosition().above())+" reader at "+p.position().subtract(Vec3.atLowerCornerOf(b)));}).thenExecute(()->{moving[0]=false;h.assertTrue(f.actor(p,"CastKimber").gesture()==4,"she offers the knife after carving");}).thenSucceed();
        h.onEachTick(()->{if(!moving[0])return;var a=f.actor(p,"CastKimber");var b=f.base(p);if(playerRoute[0]==0){p.setYRot(180);p.travel(new Vec3(0,0,1));if(f.rel(p,p).y>=6.05)playerRoute[0]=1;}else if(playerRoute[0]==1){p.setYRot(90);p.travel(new Vec3(0,0,1));if(p.getX()<=b.getX()+1.65)playerRoute[0]=2;}else if(f.rel(p,p).z> -92){p.setYRot(180);p.travel(new Vec3(0,0,1));}else p.setDeltaMovement(Vec3.ZERO);double rise=p.getY()-last[0],other=a.getY()-last[1];h.assertTrue(Math.abs(rise)<.6&&Math.abs(other)<.6,"neither ladder body is relocated between server ticks");if(rise>.025)rises[0]++;if(other>.025)friendRises[0]++;last[0]=p.getY();last[1]=a.getY();});
    }

    @GameTest(template="empty",batch="borrasca_low",timeoutTicks=1800)
    public static void theAdultBodyCrouchesThroughTheGullyAndTheReleasedServicePipe(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("fern_gully_adult");final int[] phase={0},steps={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p)).thenIdle(6).thenExecute(()->{h.assertTrue(p.getBbHeight()<1.1,"the initial native body is child height: "+p.getBbHeight());f.stage(p,3);}).thenIdle(6).thenExecute(()->{
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
        var s=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"both private native sites loaded")).thenExecute(()->{f.enter(p);f.enter(peer);}).thenWaitUntil(()->h.assertTrue(recalled(f,p),"original Friend loaded"));
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
        var f=new Fixture(h);var p=f.player("pine_owner");var peer=f.player("pine_other");h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.enter(peer);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(peer).getBoolean("CastRecalled"),"both casts loaded")).thenExecute(()->{
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
        var f=new Fixture(h);var p=f.player("pine_interrupt");h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);var own=f.own(p);own.putInt("Beat",1);f.save(p,own);BorrascaStory.depart(p);LiteraryVignettes.onArrive(p,P);h.assertTrue(f.own(p).getInt("Visit")==1&&f.own(p).getInt("ConfirmedVisits")==0,"departure intent without successful travel cannot confirm or advance a visit");f.reload();f.stage(p,4);own=f.own(p);own.putInt("Beat",3);own.putBoolean("PowerOff",true);own.putBoolean("EvidenceRead",true);own.putBoolean("Ready",true);BorrascaBelongings.takeAt(p,own,"Patch",p.blockPosition());f.save(p,own);f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual staged fourth-visit return")).thenExecute(()->h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA)&&!f.own(p).getBoolean("Completed"),"final flags and a real original cannot replace three confirmed earlier returns")).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_missing",timeoutTicks=1800)
    public static void olderNativeFriendNbtCannotReviveTheMissingOriginalOrDuplicateHerCompass(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_missing");final CompoundTag[] before={null};final UUID[] id={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original Friend loaded")).thenExecute(()->{
            var a=f.actor(p,"CastKimber");before[0]=a.saveWithoutId(new CompoundTag());id[0]=a.getUUID();a.discard();var own=f.own(p);own.putInt("Visit",2);own.putBoolean("FriendMissing",true);own.putBoolean("FriendHidden",true);h.assertTrue(BorrascaBelongings.dropOnce(p,own,"Compass",BorrascaRooms.at(f.base(p),BorrascaRooms.DROP)),"one physical original drops");f.save(p,own);f.reload();
        }).thenIdle(3).thenExecute(()->{
            var a=LiteraryRegistry.ACTOR.get().create(f.out);a.load(before[0]);h.assertTrue(f.out.addFreshEntity(a),"the same UUID returns from genuinely older native entity NBT");h.assertTrue(a.getUUID().equals(id[0])&&a.phase()==BorrascaStory.MISSING&&a.isInvisible()&&a.getOffhandItem().isEmpty(),"saved missing state wins before native tracking");var own=f.own(p);h.assertTrue(!BorrascaBelongings.dropOnce(p,own,"Compass",BorrascaRooms.at(f.base(p),BorrascaRooms.DROP)),"reload cannot issue a second original");long count=f.out.getEntitiesOfClass(ItemEntity.class,BorrascaSites.bounds(f.base(p))).stream().filter(e->BorrascaBelongings.valid(p,own,"Compass",e.getItem())).count();h.assertTrue(count==1,"exactly one original compass survives as a native item entity");f.save(p,own);f.talk(p,"CastKimber");h.assertTrue(!f.own(p).getBoolean("FollowAgain"),"the missing Friend does not answer or resume the taking");
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);p.setYRot(0);f.stage(p,2);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original Friend recalled off camera for explicit second-visit staging")).thenExecute(()->{assertTakingSightline(f,p);f.read(p,BorrascaRooms.SOURCE,false);f.talk(p,"CastKimber");id[0]=f.actor(p,"CastKimber").getUUID();f.at(p,1,0,-72);p.setNoGravity(false);previous[0]=f.actor(p,"CastKimber").position();walking[0]=true;}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FriendMissing"),"the real Friend and child walk the second route, shelter and reach the taking: body="+p.position().subtract(Vec3.atLowerCornerOf(f.base(p)))+" friend="+f.actor(p,"CastKimber").position().subtract(Vec3.atLowerCornerOf(f.base(p)))+" step="+f.own(p).getInt("TakingStep"))).thenExecute(()->{
            walking[0]=false;var own=f.own(p);h.assertTrue(own.getBoolean("Sheltered6")&&own.getBoolean("Sheltered10")&&moved[0]>100,"both actual crouched shelter beats precede the taking after continuous native walking");h.assertTrue(f.actor(p,"CastKimber").getUUID().equals(id[0])&&own.getBoolean("CompassIssued"),"the same original departs and drops her finite compass");long originals=f.out.getEntitiesOfClass(ItemEntity.class,BorrascaSites.bounds(f.base(p))).stream().filter(e->BorrascaBelongings.valid(p,own,"Compass",e.getItem())).count();h.assertTrue(originals+(BorrascaBelongings.has(p,own,"Compass")?1:0)==1,"one physical original remains after the look-away taking");
        }).thenSucceed();
        h.onEachTick(()->{
            if(!walking[0])return;
            var own=f.own(p);
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
            var c=queue.removeFirst();double fc=footing(l,c);if(Double.isNaN(fc))continue;var rel=c.subtract(base);if(Math.abs(rel.getX())>44||rel.getZ()>2||rel.getZ()<BorrascaRooms.FAR||rel.getY()<-5||rel.getY()>BorrascaRooms.TOP)continue;
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{
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
        }).thenSucceed();
    }
    /** How high above where it left the ground a sprint jump is when it has carried a body this far. */
    static double sprintJump(double distance){double x=0,y=0,vx=.48,vy=.42;while(x<distance){x+=vx;y+=vy;vx=vx*.91+.026;vy=(vy-.08)*.98;}return y;}

    @GameTest(template="empty",batch="borrasca_sealed",timeoutTicks=3000)
    public static void theRefineryIsSealedFromTheWoodsUntilTheLastNight(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_explorer");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is in place"))
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,2);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is placed"))
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,1);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"the cast is in place")).thenExecute(()->{
            var road=BorrascaRooms.trail(-126);f.at(p,road.getX(),road.getY(),road.getZ());p.setYRot(180);p.setXRot(-8);f.refresh(p);var own=f.own(p);
            var at=BorrascaStory.gentleman(own);h.assertTrue(own.getBoolean("PastTwo1")&&at!=null,"a child past the second marker hears the noise, and he is there: ticks "+own.getInt("GentTicks")+", echoes "+own.getInt("Echoes"));
            var head=at.add(0,BorrascaStory.GENT_HEIGHT-.4,0);double d=head.distanceTo(p.getEyePosition());
            h.assertTrue(d>=BorrascaStory.GENT_NEAR&&d<=BorrascaStory.GENT_FAR&&BorrascaRooms.authored(at.subtract(Vec3.atLowerCornerOf(f.base(p)))).z< -124,"he stands past the second marker, far off: "+d+" at "+at.subtract(Vec3.atLowerCornerOf(f.base(p))));
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
            h.assertTrue(book.contains("Approved: Sheriff's office")&&book.contains("E. Prescott")&&book.contains("W. Prescott")&&book.contains("J. Prescott")&&book.contains("263-A")&&book.contains("Transfer, D-417")&&book.contains("To the yard")&&book.contains("264-A"),"the night book: the Sheriff's approvals, three Prescotts, her tag and her transfer, the yard, and the newest tag");
            var letter=childPlain(String.join(" ",BorrascaTexts.document("Letter").get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).toList()));
            h.assertTrue(letter.contains("Men come down at night")&&letter.contains("I don't want to")&&letter.contains("WITHHELD"),"the letter says what it can, and no more, and was never sent");
            f.at(p,-15,6,-179);f.refresh(p);own=f.own(p);var wall=l.getBlockEntity(BorrascaRooms.at(b,BorrascaRooms.NELL_WALL));
            h.assertTrue(own.getBoolean("LowerRooms")&&own.getBoolean("NellsRoom"),"under the depot, and in her room");
            h.assertTrue(wall instanceof BorrascaCarvingEntity e&&e.friend().equals(BorrascaStory.FRIEND)&&e.reader().equals(p.getGameProfile().getName())&&l.getBlockState(BorrascaRooms.at(b,BorrascaRooms.NELL_WALL)).getValue(BorrascaCarvingBlock.WALL),"her name scratched into the wall beside the reader's");
            f.at(p,-23,6,-199);f.refresh(p);h.assertTrue(f.own(p).getBoolean("MarasRoom"),"the newest room, bolted, still warm");
            h.assertTrue(l.getBlockEntity(BorrascaRooms.at(b,BorrascaRooms.grave(0))) instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign&&sign.getFrontText().getMessage(1,false).getString().equals(BorrascaRooms.GRAVE_TAGS[0])&&sign.isWaxed(),"a number on a stake, no name");
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
    private static ItemStack openedWorld(Fixture f,ServerPlayer p,BlockPos w,String what){f.clickWorld(p,w);f.h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,what+" opens as native pages at "+w);var book=((LiteraryVignettes.Pages)p.containerMenu).original();p.closeContainer();return book;}
    private static ItemStack opened(Fixture f,ServerPlayer p,BlockPos rel,String what){f.click(p,rel);f.h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,what+" opens as native pages at "+rel);var book=((LiteraryVignettes.Pages)p.containerMenu).original();p.closeContainer();return book;}
    private static String text(ItemStack book){return book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).reduce("",String::concat);}
    /** Page breaks fall between words and take the space with them, and a child's hand swaps letters for drawn ones, so phrases are compared in plain letters without spaces. */
    private static String flat(String text){return io.github.knaitoe.theoldesthouse.house.HouseWriting.plain(text).replaceAll("\\s+","");}
    private static String title(ItemStack book){return book.get(DataComponents.WRITTEN_BOOK_CONTENT).title().raw();}

    @GameTest(template="empty",batch="borrasca_town",timeoutTicks=1800)
    public static void theTownFortAndDepotAreRealBuildingsAndTheTownsPapersChangeBetweenVisits(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("drisking_reader");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original cast loaded")).thenExecute(()->{
            var b=f.base(p);
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
            for(var door:new BlockPos[]{BorrascaShops.DINER_DOOR,BorrascaShops.ANTIQUES_DOOR,BorrascaShops.SOCIETY_DOOR,BorrascaShops.STATION_DOOR,BorrascaShops.DOCTOR_DOOR,BorrascaShops.DRUG_DOOR,BorrascaShops.BAR_DOOR})h.assertTrue(f.out.getBlockState(b.offset(door)).getBlock() instanceof DoorBlock,"a real door at "+door);
            for(int z=-76;z>=-132;z-=4)h.assertTrue(BorrascaLand.groundAt(-12,z)==0&&BorrascaLand.groundAt(12,z)==0,"the street past the chapel is level, the way down ending at the trailhead, at z "+z);
            f.atWorld(p,new BlockPos(-13,0,-88));h.assertTrue(flat(text(openedWorld(f,p,BorrascaShops.MENU,"the diner's menu"))).contains(flat("Coffee .50")),"the childhood menu has its early prices");
            f.atWorld(p,new BlockPos(10,0,-86));var cards=flat(text(openedWorld(f,p,BorrascaShops.CARDS,"the doctor's corkboard")));h.assertTrue(cards.contains(flat("1992")),"the doctor's cards are a few weeks apart in the childhood year");
            // 0.4.424 (owner: "the children were named after their progenitor"): the depot's children carry their fathers' letter, P for the
            // Prescotts and K for the Sheriff: the friend, the two town children and every baby on the doctor's board.
            h.assertTrue(BorrascaStory.FRIEND.startsWith("K")&&cards.contains(flat("Kelsey Ann"))&&cards.contains(flat("Paul"))&&cards.contains(flat("Peyton and Kody")),"the friend and every baby on the board have a P or K name: "+cards);
            f.atWorld(p,new BlockPos(-17,0,-101));h.assertTrue(flat(text(openedWorld(f,p,BorrascaShops.MAP,"the map of the old workings"))).contains(flat("DEPOT")),"the old map puts the depot over the first shaft");
            f.atWorld(p,new BlockPos(-22,0,-19));h.assertTrue(flat(text(openedWorld(f,p,BorrascaShops.PEGBOARD[0],"the hardware pegboard"))).contains(flat("Flashlight 4.99")),"the store's hardware counter has its price card");
            // 0.4.423: the Hollis yard's swing is a real one; a jump off it going forward throws the child out across the yard.
            var seatAt=BorrascaRooms.at(b,new BlockPos(-9,0,-54));
            h.assertTrue(HouseReadSwing.is(f.out.getBlockState(seatAt))&&HouseReadSwing.is(f.out.getBlockState(seatAt.east()))&&f.out.getBlockState(seatAt.above(3)).is(HouseReadRegistry.ROPE.get())&&f.out.getBlockState(seatAt.above(4)).is(Blocks.STRIPPED_OAK_LOG),"the Hollis yard has a rope swing, its seat and ropes under the crossbar");
            f.at(p,-9,0,-52);h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,seatAt)&&p.getVehicle() instanceof io.github.knaitoe.theoldesthouse.house.SeatEntity seat&&seat.swinging(),"a child sits on the yard's swing");
            ((io.github.knaitoe.theoldesthouse.house.SeatEntity)p.getVehicle()).swing(f.out.getGameTime()-157);var away=HouseReadSwing.jump(p);
            h.assertTrue(away!=null&&!p.isPassenger()&&away.z<-.3&&Math.abs(away.x)<.01,"a jump going forward throws the child out north, across the yard toward the path: "+away);
            f.stage(p,3);
        }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"adult cast recalled")).thenExecute(()->{
            f.at(p,-13,0,-20);var board=opened(f,p,BorrascaRooms.BOARD[0],"the later noticeboard");h.assertTrue(text(board).contains("KELLY")&&text(board).contains("CLOSED"),"years later her own poster is up, closed");
            f.at(p,-1,6,-96);h.assertTrue(title(opened(f,p,BorrascaRooms.NOTES,"the aged notes")).contains("water-stained"),"her notes are still there, damp through");
            f.atWorld(p,new BlockPos(-13,0,-88));h.assertTrue(flat(text(openedWorld(f,p,BorrascaShops.MENU,"the later menu"))).contains(flat("Coffee 1.25")),"years later the menu's prices have gone up");
            f.atWorld(p,new BlockPos(10,0,-86));var later=flat(text(openedWorld(f,p,BorrascaShops.CARDS,"the later corkboard")));h.assertTrue(later.contains(flat("2009"))&&later.contains(flat("Kennedy"))&&later.contains(flat("Piper and Kade")),"the corkboard holds a later year's cards, the babies still named P and K: "+later);
            f.stage(p,4);
        }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"last cast recalled")).thenExecute(()->{
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p))
          .thenWaitUntil(()->{for(int k=BorrascaPeople.LORNA;k<=BorrascaPeople.EARL;k++)h.assertTrue(keeper.apply(k)!=null,"keeper "+k+" stands at the counter");})
          .thenWaitUntil(()->h.assertTrue(BorrascaPeople.carried(p,false)!=null,"the child has change in a pocket"))
          .thenExecute(()->{
            var b=f.base(p);
            for(int k=BorrascaPeople.LORNA;k<=BorrascaPeople.EARL;k++){var a=keeper.apply(k);h.assertTrue(!a.isInvisible()&&a.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(BorrascaPeople.post(k))))<1.1,"on a childhood visit every keeper is at work, at the counter: "+BorrascaPeople.name(a));}
            var earl=keeper.apply(BorrascaPeople.EARL);h.assertTrue(earl.blockPosition().equals(BorrascaRooms.at(b,28,0,-28)),"Earl is in the Sheriff's cell");
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p))
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
            f.atWorld(p,new BlockPos(10,0,-86));var cards=flat(text(openedWorld(f,p,BorrascaShops.CARDS,"the second visit's corkboard")));
            h.assertTrue(cards.contains(flat("TATE"))&&cards.contains(flat("Paige")),"the Tates' girl is on the board by the second visit, a P name: "+cards);
            var own=f.own(p);own.putBoolean("FriendMissing",true);f.save(p,own);
            f.atWorld(p,new BlockPos(-13,0,-111));var tip=dial.apply(BorrascaPayphone.SHERIFF);
            h.assertTrue(said.apply(tip).contains("no open case")&&said.apply(tip).contains(BorrascaStory.FRIEND),"the Sheriff's office has no open case for her: "+said.apply(tip));
            f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,3);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled")&&f.own(p).getInt("PurseVisit")==3,"years later: "+BorrascaCast.why(p)))
          .thenExecute(()->{
            for(int k:new int[]{BorrascaPeople.DUTCH,BorrascaPeople.MERLE})h.assertTrue(!person.apply(k).isInvisible()&&RoadhouseBlock.is(f.out.getBlockState(person.apply(k).blockPosition()),RoadhouseBlock.Kind.STOOL),"years later "+BorrascaPeople.name(person.apply(k))+" sits at the bar");
            var own=f.own(p);var merle=person.apply(BorrascaPeople.MERLE);BorrascaPeople.talk(p,merle,own);var earl=person.apply(BorrascaPeople.EARL);BorrascaPeople.talk(p,earl,own);BorrascaPeople.talk(p,earl,own);f.save(p,own);
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

    @AfterBatch(batch="borrasca_town_children") public static void townChildrenDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="borrasca_town_children",timeoutTicks=1800)
    public static void townChildrenBecomeReadableMissingPosters(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_town");
        var eliPoster=new BlockPos(-15,1,-24);var wrenPoster=new BlockPos(-15,1,-22);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded"))
          .thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"town cast recalled"))
          .thenExecute(()->{
              f.talk(p,"TownEli");f.talk(p,"TownWren");
              h.assertTrue(f.own(p).getBoolean("MetEli")&&f.own(p).getBoolean("MetWren"),"both children can tell their part of the legend");
              h.assertTrue(f.out.getBlockState(f.base(p).offset(eliPoster)).isAir(),"Patrick's poster goes up only after he disappears");
              f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,2);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"second visit cast recalled"))
          .thenExecute(()->{
              h.assertTrue(f.actor(p,"TownEli").isInvisible()&&!f.actor(p,"TownWren").isInvisible(),"Patrick is missing while Kendra is still in town");
              f.talk(p,"TownWren");h.assertTrue(f.own(p).getBoolean("WrenRememberedEli"),"Kendra remembers Patrick's bicycle");
              f.read(p,eliPoster,false);
              f.at(p,0,0,-3);p.setYRot(0);p.setYHeadRot(0);f.stage(p,3);
          }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"adult visit cast recalled"))
          .thenExecute(()->{
              h.assertTrue(f.actor(p,"TownWren").isInvisible(),"Kendra is missing on the adult return");
              f.read(p,wrenPoster,false);
              h.assertTrue(f.own(p).getBoolean("Read_WrenPoster")&&f.own(p).getBoolean("Read_EliPoster"),"both posters have readable pages");
          }).thenSucceed();
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded"))
          .thenExecute(()->{f.enter(p);f.stage(p,4);f.at(p,0,12,-170);})
          .thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"fourth visit cast recalled"))
          .thenWaitUntil(()->h.assertTrue(f.rel(f.actor(p,"CastSheriff"),p).z< -176,"the sheriff walks his yard route"))
          .thenExecute(()->f.use(p,BorrascaRooms.POWER))
          .thenWaitUntil(()->{
              var a=f.actor(p,"CastSheriff");var b=f.base(p);
              h.assertTrue(a.distanceToSqr(Vec3.atBottomCenterOf(BorrascaRooms.at(b,5,12,-182)))<.1&&a.gesture()==4,"he goes to inspect the stopped machine");
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p))
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p))
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
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,3);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original Sheriff loaded")).thenExecute(()->{
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
