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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
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
        @Override public void close(){BorrascaCast.clear();BorrascaSites.clear();for(var p:players){for(var e:out.getEntitiesOfClass(Entity.class,BorrascaSites.bounds(base(p)),e->!(e instanceof ServerPlayer)))e.discard();NativeTestPlayers.remove(p);}chunks.close();LiteraryVignettes.clearAll();var store=in.getServer().overworld().getDataStorage();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}
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

    @GameTest(template="empty",batch="borrasca_registration")
    public static void oneFixedUnderstandingSourceKeepsThePoolAndOrdinaryRecord(GameTestHelper h){
        h.assertTrue(P.slot()==98&&LabyrinthPlace.LAKE_HOUSE.slot()==96&&LabyrinthPlace.LAKE_HOUSE_EMPTY.slot()==97&&LabyrinthPlace.GHOSTS_SET.slot()==65,"earlier source slots retain their addresses");
        h.assertTrue(P.kind()==LabyrinthPlace.Kind.MULTI_VISIT&&LiteraryRooms.isLiterary(P)&&NovelRooms.outside(P)&&!ScenePolish.applies(P)&&!SceneReview.applies(P)&&!SceneCraft.applies(P),"the private site owns its authored terrain");
        h.assertTrue(WitnessAccount.Story.of(P.id())==WitnessAccount.Story.BORRASCA&&WitnessAccount.Story.values().length==46&&WitnessAccount.REQUIRED==35&&WitnessAccount.Story.BORRASCA.kind.equals("understanding"),"one fixed Understanding source preserves the 46/35 rule");
        String form=BorrascaTexts.document("Evidence").get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(p->p.raw().getString()).reduce("",String::concat);
        h.assertTrue(form.contains("Prescott")&&form.contains("Sheriff")&&!form.contains("brass compass")&&!form.contains("machine"),"the ordinary receipt implicates familiar people without inventing a confiscation or explaining the reveal");h.succeed();
    }

    @GameTest(template="empty",batch="borrasca_motion",timeoutTicks=1800)
    public static void nativeChildAndTheOriginalFriendClimbTheActualFortWithoutRelocation(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("triple_tree_climber");final boolean[] moving={false};final int[] rises={0},friendRises={0};final double[] last={0,0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"all original cast loaded")).thenExecute(()->{
            var b=f.base(p);h.assertTrue(f.out.getBlockEntity(b.offset(BorrascaRooms.SOURCE)) instanceof LecternBlockEntity,"the content note is a native book");h.assertTrue(!f.out.noCollision(p,new AABB(b.getX()-4.8,b.getY()+6,b.getZ()-93.1,b.getX()-4.2,b.getY()+7,b.getZ()-92.9)),"native fort rails join between posts");
            // Stage the approach only; both bodies must then climb by native collision and ladder physics.
            f.at(p,3,0,-83);p.setNoGravity(false);var a=f.actor(p,"CastKimber");a.moveTo(Vec3.atBottomCenterOf(b.offset(3,0,-86)),180,0);var own=f.own(p);own.putBoolean("MetFriend",true);own.putInt("ChildStep",3);f.save(p,own);last[0]=p.getY();last[1]=a.getY();moving[0]=true;
        }).thenWaitUntil(()->{var a=f.actor(p,"CastKimber");h.assertTrue(p.getY()>=f.base(p).getY()+6&&a!=null&&a.getY()>=f.base(p).getY()+5.9&&f.own(p).getBoolean("TreeReached"),"native child and original Friend reach the fort: player="+p.position()+" friend="+(a==null?"absent":a.position())+" step="+f.own(p).getInt("ChildStep"));}).thenExecute(()->{h.assertTrue(rises[0]>=20&&friendRises[0]>=20,"each body climbs over successive native ticks");h.assertTrue(p.getBbHeight()<1.1,"visits one and two use child height");moving[0]=false;}).thenSucceed();
        h.onEachTick(()->{if(!moving[0])return;var a=f.actor(p,"CastKimber");p.setYRot(180);if(p.getZ()>f.base(p).getZ()-92)p.travel(new Vec3(0,0,1));else p.setDeltaMovement(Vec3.ZERO);double rise=p.getY()-last[0],other=a.getY()-last[1];h.assertTrue(Math.abs(rise)<.6&&Math.abs(other)<.6,"neither ladder body is relocated between server ticks");if(rise>.025)rises[0]++;if(other>.025)friendRises[0]++;last[0]=p.getY();last[1]=a.getY();});
    }

    @GameTest(template="empty",batch="borrasca_low",timeoutTicks=700)
    public static void theAdultBodyCrouchesThroughTheGullyAndTheReleasedServicePipe(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("fern_gully_adult");final int[] phase={0},steps={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,3);}).thenIdle(10).thenExecute(()->{
            var b=f.base(p);h.assertTrue(p.getBbHeight()>1.7,"the third visit restores adult height");var standing=new AABB(b.getX()-15.8,b.getY()+3,b.getZ()-119.8,b.getX()-15.2,b.getY()+4.8,b.getZ()-119.2);var low=new AABB(standing.minX,standing.minY,standing.minZ,standing.maxX,b.getY()+4.5,standing.maxZ);h.assertTrue(!f.out.noCollision(p,standing)&&f.out.noCollision(p,low),"the actual gully roof admits crouching and blocks standing");f.at(p,-16,2,-117);p.setNoGravity(false);p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);phase[0]=1;
        }).thenWaitUntil(()->h.assertTrue(phase[0]==3,"native crouched gully and service traversal: phase="+phase[0]+" body="+p.position().subtract(Vec3.atLowerCornerOf(f.base(p))))).thenExecute(()->h.assertTrue(steps[0]>25,"the adult walks continuously under both native roofs")).thenSucceed();
        h.onEachTick(()->{if(phase[0]==0||phase[0]==3)return;p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);p.setYRot(180);p.travel(new Vec3(0,0,1));steps[0]++;if(phase[0]==1&&p.getZ()<f.base(p).getZ()-122){f.stage(p,4);f.at(p,-17,12,-204);f.click(p,BorrascaRooms.RELEASE);h.assertTrue(f.own(p).getBoolean("ReleaseOpened"),"native lever opens both service-door halves");phase[0]=2;}else if(phase[0]==2&&p.getZ()<f.base(p).getZ()-209)phase[0]=3;});
    }

    @GameTest(template="empty",batch="borrasca_four",timeoutTicks=1500)
    public static void fourConfirmedReturnsCarryTheOriginalAndRestoreTheScarWithoutASecondKind(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("name_beside_mine");var peer=f.player("earlier_reader");final UUID[] original={null};
        var s=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"both private native sites loaded")).thenExecute(()->{f.enter(p);f.enter(peer);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original Friend loaded"));
        s.thenExecute(()->{original[0]=f.own(p).getUUID("CastKimber");f.read(p,BorrascaRooms.SOURCE,false);var own=f.own(p);own.putBoolean("FriendCarved",true);f.save(p,own);f.use(p,BorrascaRooms.CARVING);h.assertTrue(f.own(p).getInt("Beat")==1,"the native carving seals the staged childhood pact");var e=(BorrascaCarvingEntity)f.out.getBlockEntity(f.base(p).offset(BorrascaRooms.CARVING));h.assertTrue(e.reader().equals(p.getGameProfile().getName())&&e.friend().equals(BorrascaStory.FRIEND),"the bark holds this reader's actual name beside their Friend");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual first return consumes its native destination"));
        s.thenExecute(()->{f.enter(p);h.assertTrue(f.own(p).getInt("Visit")==2&&f.own(p).getInt("ConfirmedVisits")==1,"only the confirmed first return advances childhood");}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"same Friend recalled")).thenExecute(()->{
            f.read(p,BorrascaRooms.SOURCE,false);f.talk(p,"CastKimber");var own=f.own(p);own.putInt("TakingStep",7);f.save(p,own);f.actor(p,"CastKimber").moveTo(Vec3.atBottomCenterOf(f.base(p).offset(BorrascaRooms.TAKING)),180,0);f.at(p,-2,9,-147);p.setYRot(0);
        }).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("FriendMissing"),"the taking completes with the camera looking away")).thenExecute(()->{f.use(p,BorrascaRooms.DROP);h.assertTrue(f.own(p).getInt("Beat")==2&&BorrascaBelongings.has(p,f.own(p),"Compass"),"the dropped original is recovered through native interaction");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual second return"));
        s.thenExecute(()->{f.enter(p);h.assertTrue(f.own(p).getInt("Visit")==3,"the next arrival is the adult return");}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"same Sheriff recalled")).thenExecute(()->{f.use(p,BorrascaRooms.CARVING);f.talk(p,"CastSheriff");p.closeContainer();p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);});
        // Ordered checkpoints are staged here. Continuous physical clearance is exercised in the low-route case.
        for(var at:BorrascaRooms.GULLY)s.thenExecute(()->f.at(p,at.getX(),at.getY(),at.getZ())).thenIdle(6);
        s.thenExecute(()->{p.setShiftKeyDown(false);p.setPose(Pose.STANDING);f.use(p,BorrascaRooms.MARKER_FOUR);f.refresh(p);h.assertTrue(f.own(p).getInt("Beat")==3,"the gouge, personal denial, ordered gully and perimeter complete the investigation");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual third return"));
        s.thenExecute(()->{f.enter(p);h.assertTrue(f.own(p).getInt("Visit")==4&&f.own(p).getInt("ConfirmedVisits")==7,"all three earlier returns are independently confirmed");f.read(p,BorrascaRooms.EVIDENCE,true);f.use(p,BorrascaRooms.PACK);f.use(p,BorrascaRooms.POWER);}).thenIdle(6).thenExecute(()->{
            var b=f.base(p);h.assertTrue(!f.out.getBlockState(b.offset(BorrascaRooms.MACHINE)).getValue(BorrascaMachineBlock.RUNNING)&&f.out.getBlockState(b.offset(BorrascaRooms.LAMP)).getValue(BorrascaFittingBlock.KIND)==BorrascaFittingBlock.Kind.LAMP_OFF,"the actual machine and lamp switch off");f.use(p,BorrascaRooms.CARVING);h.assertTrue(f.out.getBlockState(b.offset(BorrascaRooms.CARVING)).getValue(BorrascaCarvingBlock.STAGE)==4,"restored lettering keeps the gouged bark state");f.at(p,0,0,-10);
        }).thenIdle(6).thenExecute(()->{h.assertTrue(f.own(p).getBoolean("Ready")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA),"carrying the original back earns readiness, not a return");f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual final return")).thenExecute(()->{
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA)&&f.own(p).getBoolean("NameReturned"),"only confirmed native travel grants the fixed Understanding source and optional account line");h.assertTrue(f.own(p).getUUID("CastKimber").equals(original[0])&&f.own(peer).getInt("Visit")==1&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.BORRASCA),"the original UUID and earlier peer's story stay intact");f.reload();h.assertTrue(f.own(p).getBoolean("Completed")&&BorrascaStory.carriesEvidence(p,f.own(p)),"saved completion retains the actual original patch");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_private",timeoutTicks=400)
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

    @GameTest(template="empty",batch="borrasca_interrupt",timeoutTicks=350)
    public static void unconfirmedDeparturesAndStagedFinalFlagsCannotBecomeWitnesses(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_interrupt");h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);var own=f.own(p);own.putInt("Beat",1);f.save(p,own);BorrascaStory.depart(p);LiteraryVignettes.onArrive(p,P);h.assertTrue(f.own(p).getInt("Visit")==1&&f.own(p).getInt("ConfirmedVisits")==0,"departure intent without successful travel cannot confirm or advance a visit");f.reload();f.stage(p,4);own=f.own(p);own.putInt("Beat",3);own.putBoolean("PowerOff",true);own.putBoolean("EvidenceRead",true);own.putBoolean("Ready",true);BorrascaBelongings.takeAt(p,own,"Patch",p.blockPosition());f.save(p,own);f.leave(p);}).thenWaitUntil(()->h.assertTrue(f.home(p),"actual staged fourth-visit return")).thenExecute(()->h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BORRASCA)&&!f.own(p).getBoolean("Completed"),"final flags and a real original cannot replace three confirmed earlier returns")).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_missing",timeoutTicks=500)
    public static void olderNativeFriendNbtCannotReviveTheMissingOriginalOrDuplicateHerCompass(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_missing");final CompoundTag[] before={null};final UUID[] id={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->f.enter(p)).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original Friend loaded")).thenExecute(()->{
            var a=f.actor(p,"CastKimber");before[0]=a.saveWithoutId(new CompoundTag());id[0]=a.getUUID();a.discard();var own=f.own(p);own.putInt("Visit",2);own.putBoolean("FriendMissing",true);own.putBoolean("FriendHidden",true);h.assertTrue(BorrascaBelongings.dropOnce(p,own,"Compass",f.base(p).offset(BorrascaRooms.DROP)),"one physical original drops");f.save(p,own);f.reload();
        }).thenIdle(3).thenExecute(()->{
            var a=LiteraryRegistry.ACTOR.get().create(f.out);a.load(before[0]);h.assertTrue(f.out.addFreshEntity(a),"the same UUID returns from genuinely older native entity NBT");h.assertTrue(a.getUUID().equals(id[0])&&a.phase()==BorrascaStory.MISSING&&a.isInvisible()&&a.getOffhandItem().isEmpty(),"saved missing state wins before native tracking");var own=f.own(p);h.assertTrue(!BorrascaBelongings.dropOnce(p,own,"Compass",f.base(p).offset(BorrascaRooms.DROP)),"reload cannot issue a second original");long count=f.out.getEntitiesOfClass(ItemEntity.class,BorrascaSites.bounds(f.base(p))).stream().filter(e->BorrascaBelongings.valid(p,own,"Compass",e.getItem())).count();h.assertTrue(count==1,"exactly one original compass survives as a native item entity");f.save(p,own);f.talk(p,"CastKimber");h.assertTrue(!f.own(p).getBoolean("FollowAgain"),"the missing Friend does not answer or resume the taking");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_custody",timeoutTicks=500)
    public static void denialWorksWithoutTheCompassAndConfiscationReturnsTheSameOriginal(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_compass");final ItemStack[] exact={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,3);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original adult Sheriff loaded")).thenExecute(()->{
            f.talk(p,"CastSheriff");h.assertTrue(f.own(p).getBoolean("HeardDenial")&&f.actor(p,"CastSheriff").getCustomName().getString().contains("Marker Three"),"he gives away Marker Three even with no compass");var own=f.own(p);BorrascaBelongings.takeAt(p,own,"Compass",p.blockPosition());exact[0]=BorrascaBelongings.remove(p,own,"Compass");p.setItemInHand(InteractionHand.MAIN_HAND,exact[0]);f.save(p,own);f.talk(p,"CastSheriff");h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu,"holding the real compass opens the native handover choice");p.containerMenu.clicked(0,0,ClickType.PICKUP,p);p.closeContainer();h.assertTrue(f.own(p).getBoolean("CompassConfiscated")&&!BorrascaBelongings.has(p,f.own(p),"Compass"),"handover transfers the actual inventory stack");f.reload();f.stage(p,4);f.use(p,BorrascaRooms.PACK);var after=f.own(p);h.assertTrue(BorrascaBelongings.has(p,after,"Compass")&&!after.contains("ConfiscatedCompass"),"the original is recovered at the sorting table after reload");var found=p.getInventory().items.stream().filter(a->BorrascaBelongings.valid(p,after,"Compass",a)).findFirst().orElse(ItemStack.EMPTY);h.assertTrue(ItemStack.isSameItemSameComponents(exact[0],found),"all components of the confiscated compass remain exact");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="borrasca_escort",timeoutTicks=750)
    public static void flashlightObeysSolidCoverAndTheSheriffWalksTheReaderBack(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("pine_escort");final boolean[] walking={false};final int[] moved={0};final Vec3[] previous={Vec3.ZERO};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native sections loaded")).thenExecute(()->{f.enter(p);f.stage(p,3);}).thenWaitUntil(()->h.assertTrue(f.own(p).getBoolean("CastRecalled"),"original Sheriff loaded")).thenExecute(()->{
            var a=f.actor(p,"CastSheriff");f.at(p,2,4,-114);h.assertTrue(BorrascaStory.flashlightSees(a,p,0),"the actual eye ray and cone see the reader");var cover=f.base(p).offset(2,5,-118);f.out.setBlock(cover,Blocks.STONE.defaultBlockState(),3);h.assertTrue(!BorrascaStory.flashlightSees(a,p,0),"solid native cover blocks the flashlight");f.out.setBlock(cover,Blocks.AIR.defaultBlockState(),3);var own=f.own(p);own.putInt("TrailClock",20);h.assertTrue(BorrascaStory.masking(own),"the dust pulse masks footsteps");own.putInt("TrailClock",90);h.assertTrue(!BorrascaStory.masking(own),"quiet intervals expose footsteps");own.putBoolean("Escorting",true);f.save(p,own);f.at(p,2,5,-125);p.setNoGravity(false);previous[0]=p.position();walking[0]=true;
        }).thenWaitUntil(()->h.assertTrue(walking[0]&&!f.own(p).getBoolean("Escorting")&&p.getZ()>=f.base(p).getZ()-119.5,"native walking escort reaches the near side of the tape: "+p.position())).thenExecute(()->{walking[0]=false;h.assertTrue(moved[0]>=10&&p.getHealth()==p.getMaxHealth(),"the reader is walked back continuously without damage or a jump cut");}).thenSucceed();
        h.onEachTick(()->{if(!walking[0])return;p.travel(Vec3.ZERO);double distance=p.position().distanceTo(previous[0]);h.assertTrue(distance<.85,"the escort never relocates the reader between ticks");if(distance>.005)moved[0]++;previous[0]=p.position();});
    }
}
