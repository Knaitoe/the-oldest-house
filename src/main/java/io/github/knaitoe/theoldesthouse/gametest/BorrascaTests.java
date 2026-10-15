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
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/** Native interactions and physics. Staged entry/route positions are named; no test claims an ordinary playthrough. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
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
        /** Explicit authored-geometry fixture. Production queue/cursor is exercised separately below. */
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
        void leave(ServerPlayer p){var b=base(p);at(p,0,0,-3);LabyrinthDoors.tickPlayer(p,origin);at(p,0,0,2.2);LabyrinthDoors.tickPlayer(p,origin);}
        boolean home(ServerPlayer p){return p.level().dimension().equals(HouseDimensions.INTERIOR)&&data().returnDepth(p.getUUID())==0&&!LabyrinthDoors.isBusy(p);}
        void reload(){BorrascaSites.clear();var saved=data().save(new CompoundTag(),out.registryAccess());in.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.load(saved,out.registryAccess()));}
        @Override public void close(){BorrascaSites.clear();for(var p:players){for(var e:out.getEntitiesOfClass(Entity.class,BorrascaSites.bounds(base(p)),e->!(e instanceof ServerPlayer)))e.discard();NativeTestPlayers.remove(p);}chunks.close();LiteraryVignettes.clearAll();var store=in.getServer().overworld().getDataStorage();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}
    }
    private static void cleanup(){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="borrasca_route") public static void routeDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_four") public static void fourDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_private") public static void privateDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_build") public static void buildDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_interrupt") public static void interruptDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="borrasca_walk") public static void walkDone(ServerLevel l){cleanup();}

    @GameTest(template="empty",batch="borrasca_walk",timeoutTicks=1400)
    public static void nativeWalkingReachesTheBedroomAndTheRidgeWithAReachableMineSign(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("drisking_walker");final int[] phase={0},next={0},moves={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.at(p,16,0,-46);p.setNoGravity(false);phase[0]=1;}).thenWaitUntil(()->h.assertTrue(phase[0]==3,"ordinary walking finishes: phase="+phase[0]+", ridge point="+next[0]+", body="+p.position().subtract(Vec3.atLowerCornerOf(f.base(p))))).thenSucceed();
        h.onEachTick(()->{if(phase[0]==0||phase[0]==3)return;var b=f.base(p);if(phase[0]==1){p.setYRot(180);p.travel(new Vec3(0,0,1));if(p.getZ()<b.getZ()-54){h.assertTrue(p.getY()>=b.getY()+5.9,"the real stair reaches the upstairs floor without a scripted jump");f.at(p,0,0,-105);phase[0]=2;}return;}
            int n=next[0];if(n>64){h.assertTrue(moves[0]>200&&p.getY()>=b.getY()+11.8,"the same ordinary body walked the graded ridge and final native stair");h.succeed();phase[0]=3;return;}if(phase[0]!=2)return;
            double x=b.getX()+Math.round(8*Math.sin(n*.065))+.5,z=b.getZ()-105-n+.5,dx=x-p.getX(),dz=z-p.getZ();if(Math.hypot(dx,dz)<.35){if(n==43){var sign=b.offset(BorrascaRooms.SIGN).getCenter();h.assertTrue(p.getEyePosition().distanceTo(sign)<4.5,"the original mine notice is within native interaction reach from the actual trail");}next[0]++;return;}p.setYRot((float)Math.toDegrees(Math.atan2(-dx,dz)));p.travel(new Vec3(0,0,1));moves[0]++;
        });
    }

    @GameTest(template="empty",batch="borrasca_registration")
    public static void oneCompleteSourceAppendsWithoutRenumberingOrChangingTheThreeEndings(GameTestHelper h){
        h.assertTrue(P.slot()==98&&LabyrinthPlace.LAKE_HOUSE.slot()==96&&LabyrinthPlace.LAKE_HOUSE_EMPTY.slot()==97&&LabyrinthPlace.GHOSTS_SET.slot()==65,"all earlier source slots retain their addresses");
        h.assertTrue(P.kind()==LabyrinthPlace.Kind.MULTI_VISIT&&LiteraryRooms.isLiterary(P)&&NovelRooms.outside(P)&&!ScenePolish.applies(P)&&!SceneReview.applies(P)&&!SceneCraft.applies(P),"the new site owns its authored geometry");
        h.assertTrue(WitnessAccount.Story.of(P.id())==WitnessAccount.Story.BORRASCA&&WitnessAccount.Story.values().length==46&&WitnessAccount.REQUIRED==35,"exactly one eligible source: ceil(46*0.75)=35");
        for(String key:List.of("Bedroom","Register","Map","Service","Evidence","Testimony"))h.assertTrue(BorrascaTexts.document(key).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()>0,"every original document has native writing");h.succeed();
    }

    @GameTest(template="empty",batch="borrasca_route",timeoutTicks=800)
    public static void theActualLadderClimbsContinuouslyAndThePipeAcceptsOnlyACrouchedBody(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("drisking_climber");final int[] phase={0},climbs={0};final double[] previous={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);var b=f.base(p);h.assertTrue(f.out.getBlockEntity(b.offset(BorrascaRooms.SOURCE)) instanceof LecternBlockEntity,"content note is a real book");h.assertTrue(f.out.getBlockEntity(b.offset(BorrascaRooms.MACHINE)) instanceof BorrascaMachineBlock.Machine,"the sound's physical machine has its native renderer");f.at(p,3,0,-88);p.setNoGravity(false);previous[0]=p.getY();phase[0]=1;});
        h.onEachTick(()->{if(phase[0]!=1)return;p.setYRot(180);p.travel(new Vec3(0,0,1));double rise=p.getY()-previous[0];previous[0]=p.getY();h.assertTrue(Math.abs(rise)<.42,"ladder ascent uses native movement instead of script relocation: "+rise);if(rise>.05)climbs[0]++;if(p.getY()>=f.base(p).getY()+6){phase[0]=2;p.setNoGravity(true);h.assertTrue(climbs[0]>=25,"the native ladder raises the actual body over many server ticks");var b=f.base(p);var standing=new AABB(b.getX()-20.8,b.getY()+12,b.getZ()-208.8,b.getX()-20.2,b.getY()+13.8,b.getZ()-208.2);var ducked=new AABB(standing.minX,standing.minY,standing.minZ,standing.maxX,b.getY()+13.5,standing.maxZ);h.assertTrue(!f.out.noCollision(p,standing)&&f.out.noCollision(p,ducked),"the physical pipe blocks standing height and admits native crouching height");h.succeed();}});
    }

    @GameTest(template="empty",batch="borrasca_four",timeoutTicks=1600)
    public static void fourNativePaperAndReturnSequencesRequireTheOriginalAndTheServiceDescent(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("drisking_reader");var peer=f.player("drisking_peer");var sequence=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"both private native sites loaded")).thenExecute(()->{f.enter(p);f.enter(peer);}).thenIdle(10);
        sequence.thenExecute(()->{f.read(p,BorrascaRooms.SOURCE,false);f.talk(p,"CastKyle");f.talk(p,"CastKimber");f.at(p,0,6,-94);}).thenIdle(6).thenExecute(()->{f.use(p,BorrascaRooms.CARVING);h.assertTrue(f.own(p).getInt("Beat")==1,"climbing and meeting both friends makes one whole visit");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual first return consumes its saved destination"));
        sequence.thenExecute(()->{f.enter(p);h.assertTrue(f.own(p).getInt("Visit")==2,"only the completed return advances the visit");f.read(p,BorrascaRooms.BEDROOM.above(),false);f.read(p,BorrascaRooms.REGISTER,false);f.read(p,BorrascaRooms.MAP,false);}).thenIdle(6).thenExecute(()->{h.assertTrue(f.own(p).getInt("Beat")==2,"bedroom, public register and mineral survey are personally read");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual second return"));
        sequence.thenExecute(()->{f.enter(p);f.use(p,BorrascaRooms.SIGN);f.use(p,new BlockPos(12,13,-160));f.use(p,new BlockPos(-15,13,-168));f.read(p,BorrascaRooms.SERVICE,false);f.at(p,-6,12,-189);f.look(p,BorrascaRooms.MACHINE.above(2));}).thenIdle(6).thenExecute(()->{h.assertTrue(f.own(p).getInt("Beat")==3,"the actual sign, familiar vehicle, service record and visible industrial source answer the map");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual third return"));
        sequence.thenExecute(()->{f.enter(p);h.assertTrue(f.own(p).getInt("Visit")==4,"fourth visit retained");f.use(p,new BlockPos(-15,13,-168));h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu,"the non-graphic subject is confirmed before opening the final office");p.containerMenu.clicked(0,0,ClickType.PICKUP,p);h.assertTrue(f.own(p).getBoolean("Consent"),"native consent choice saved");f.read(p,BorrascaRooms.EVIDENCE,true);f.read(p,BorrascaRooms.TESTIMONY,false);h.assertTrue(BorrascaStory.carriesEvidence(p,f.own(p)),"the actual inventory contains this reader's unique original");f.use(p,BorrascaRooms.RELEASE);});
        for(int i=0;i<BorrascaRooms.RETURN_ROUTE.length;i++){var point=BorrascaRooms.RETURN_ROUTE[i];sequence.thenExecute(()->f.at(p,point.getX(),point.getY(),point.getZ())).thenIdle(6);if(i==0)sequence.thenExecute(()->{f.at(p,-20,12,-208.5);p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);}).thenIdle(6).thenExecute(()->{p.setShiftKeyDown(false);p.setPose(Pose.STANDING);});}
        sequence.thenExecute(()->f.at(p,0,0,-10)).thenIdle(6).thenExecute(()->{h.assertTrue(f.own(p).getBoolean("Ready"),"the service descent and low pipe lead home with the original");f.read(p,BorrascaRooms.ACCOUNT,false);h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA),"the account itself still cannot grant a return");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual final return"));
        sequence.thenExecute(()->{h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA)&&f.own(p).getBoolean("Completed"),"only successful native return resolves this personal source");h.assertTrue(f.own(peer).getInt("Visit")==1&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.BORRASCA),"a peer retains their earlier town and receives no borrowed source");f.reload();h.assertTrue(f.own(p).getBoolean("Completed")&&BorrascaStory.carriesEvidence(p,f.own(p)),"saved data keeps the completed source and the same carried original");}).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_private",timeoutTicks=400)
    public static void savedOriginalCastAndBorrowedRecordsStayPrivateAcrossReload(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("drisking_owner");var peer=f.player("drisking_other");h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.enter(peer);}).thenIdle(10).thenExecute(()->{
            var kyle=f.actor(p,"CastKyle");var theirs=f.actor(peer,"CastKyle");h.assertTrue(kyle!=null&&theirs!=null&&!kyle.getUUID().equals(theirs.getUUID())&&kyle.broadcastToPlayer(p)&&!kyle.broadcastToPlayer(peer),"each reader tracks only their native cast");var id=kyle.getUUID();var edited=f.base(p).offset(2,0,-12);f.out.setBlock(edited,Blocks.DIAMOND_BLOCK.defaultBlockState(),3);var original=LiteraryVignettes.mark(p,P,"Evidence",BorrascaTexts.document("Evidence"));peer.getInventory().add(original.copy());h.assertTrue(!BorrascaStory.carriesEvidence(peer,f.own(peer)),"another reader's physical ledger cannot replace your original");
            f.reload();LiteraryVignettes.onArrive(p,P);h.assertTrue(f.own(p).getInt("Visit")==1&&f.own(p).getUUID("CastKyle").equals(id)&&f.out.getEntity(id)==kyle,"reconnect-like arrival and native SavedData reload neither advance nor replace the original");h.assertTrue(f.out.getBlockState(edited).is(Blocks.DIAMOND_BLOCK)&&BorrascaSites.prepareEntry(p)!=null,"a ready saved site is never carved over a player edit");h.succeed();});
    }

    @GameTest(template="empty",batch="borrasca_build",timeoutTicks=1000)
    public static void firstConstructionResumesItsSavedBoundedCursorAndKeepsAppliedEdits(GameTestHelper h){
        var f=new Fixture(h);var p=NativeTestPlayers.survival(h,"drisking_builder");f.players.add(p);h.assertTrue(BorrascaSites.prepareEntry(p)==null,"first entry waits without starting a visit");var b=BorrascaSites.base(p.server,p.getUUID());f.chunks.hold(f.out,BorrascaSites.bounds(b).inflate(2));final boolean[] reloaded={false};final BlockPos[] kept={null};
        h.onEachTick(()->{var site=f.data().stateEntry("borrasca_sites_0497",p.getUUID().toString());var cursor=site.getCompound("BuildCursor");if(!reloaded[0]&&!cursor.isEmpty()&&cursor.getInt("Cursor")>=3){kept[0]=b.offset(0,-3,-80);f.out.setBlock(kept[0],Blocks.GOLD_BLOCK.defaultBlockState(),3);f.reload();h.assertTrue(BorrascaSites.prepareEntry(p)==null,"an unfinished build resumes at its saved command");reloaded[0]=true;}if(BorrascaSites.ready(p.server,p.getUUID())){h.assertTrue(reloaded[0]&&f.out.getBlockState(kept[0]).is(Blocks.GOLD_BLOCK),"already applied ground is not replayed after saved-data reload");h.assertTrue(BorrascaSites.heldChunks()==0&&BorrascaSites.entry(p)!=null&&f.own(p).getInt("Visit")==0,"completion releases construction tickets and grants no arrival, reading or credit");h.succeed();}});
    }

    @GameTest(template="empty",batch="borrasca_interrupt",timeoutTicks=350)
    public static void arrivalsAndUnconfirmedDeparturesDoNotBecomeVisitsOrWitnesses(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("drisking_interrupt");h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);var own=f.own(p);own.putInt("Beat",1);f.save(p,own);BorrascaStory.depart(p);LiteraryVignettes.onArrive(p,P);h.assertTrue(f.own(p).getInt("Visit")==1&&!f.own(p).getBoolean("BorrascaDeparted"),"a departure request without native confirmed travel cannot advance a visit");f.reload();LiteraryVignettes.onArrive(p,P);h.assertTrue(f.own(p).getInt("Visit")==1&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA),"saved re-entry and arrival provide no story completion");h.succeed();});
    }
}
