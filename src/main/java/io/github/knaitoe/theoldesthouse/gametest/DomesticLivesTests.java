package io.github.knaitoe.theoldesthouse.gametest;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Actual old fragments, physical use and native movement; no staged story credit. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_interior_review") @PrefixGameTestTemplate(false)
public final class DomesticLivesTests {
    private static LiteraryTests.Fixture fixture;
    private static void close(){if(fixture!=null){fixture.close();fixture=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){var f=new LiteraryTests.Fixture(h,192);fixture=f;HouseSavedData.get(h.getLevel().getServer()).markInteriorInitialized();return f;}
    private static void build(LiteraryTests.Fixture f,LabyrinthPlace p){if(f.built.add(p)){
        // Review every authored variant; production selects only one, occasionally two.
        var chosen=f.data().state(DomesticLives.STATE);for(var home:DomesticLives.HOMES)if(home.hall()==p)chosen.putBoolean(DomesticLives.key(f.base(p),home),true);f.data().setState(DomesticLives.STATE,chosen);
        LabyrinthHalls.build(f.in,f.base(p),p);LabyrinthBuilder.registerDoors(f.data(),p,f.base(p));
    }}
    private static ServerPlayer player(LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(f.h,name);f.players.add(p);p.setNoGravity(true);return p;}
    private static boolean clear(ServerLevel l,BlockPos at){
        return !l.getBlockState(at.below()).getCollisionShape(l,at.below()).isEmpty()&&l.noCollision(null,new AABB(at.getX()+.2,at.getY()+.001,at.getZ()+.2,at.getX()+.8,at.getY()+1.8,at.getZ()+.8));
    }
    private static boolean crosses(ServerLevel l,BlockPos a,BlockPos b){
        for(int i=1;i<=5;i++){double t=i/5.0;var p=Vec3.atBottomCenterOf(a).lerp(Vec3.atBottomCenterOf(b),t);if(!l.noCollision(null,new AABB(p.x-.3,p.y+.001,p.z-.3,p.x+.3,p.y+1.8,p.z+.3)))return false;}return true;
    }
    private static List<BlockPos> route(ServerLevel l,BlockPos base,DomesticLives.Home h,BlockPos goal){
        var outside=DomesticLives.approach(h);var bounds=DomesticLives.workBounds(h);
        var start=base.offset(outside);goal=base.offset(goal);var queue=new ArrayDeque<BlockPos>();var previous=new HashMap<BlockPos,BlockPos>();queue.add(start);previous.put(start,start);
        while(!queue.isEmpty()&&previous.size()<10000){var at=queue.remove();if(at.equals(goal)){var path=new ArrayList<BlockPos>();while(!at.equals(start)){path.add(at);at=previous.get(at);}Collections.reverse(path);return path;}
            for(var d:Direction.Plane.HORIZONTAL){var n=at.relative(d);if(!bounds.isInside(n.subtract(base))||previous.containsKey(n)||!clear(l,n)||!crosses(l,at,n))continue;previous.put(n,at);queue.add(n);}
        }return List.of();
    }
    @GameTest(template="empty",batch="domestic_lives_rooms",timeoutTicks=700)
    public static void fourSelectedFragmentsHaveRealWalkableEmptyHomesAndNativeViews(GameTestHelper h){
        var f=fixture(h);for(var p:List.of(LabyrinthPlace.STRAIGHT_HALL,LabyrinthPlace.BENT_HALL,LabyrinthPlace.CROSS_HALL))f.chunks.hold(f.in,IndianLakeRooms.bounds(f.base(p),p));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"three actual halls and their selected extensions are resident")).thenExecute(()->{
            for(var p:List.of(LabyrinthPlace.STRAIGHT_HALL,LabyrinthPlace.BENT_HALL,LabyrinthPlace.CROSS_HALL))build(f,p);
            for(var home:DomesticLives.HOMES){var b=f.base(home.hall());h.assertTrue(f.in.getEntitiesOfClass(LivingEntity.class,home.area(b),e->!(e instanceof ServerPlayer)).isEmpty(),"the homes imply absent lives; no residents are spawned");
                h.assertTrue(LabyrinthPlaces.placeAt(f.origin,b.offset(home.x0(),0,home.z0()))==home.hall(),"extended home stays in its protected original slot: "+home.id());
                var targets=switch(home.id()){
                    case "family"->List.of(new BlockPos(-13,0,-24),new BlockPos(-17,0,-22),new BlockPos(-17,0,-17));
                    case "students"->List.of(new BlockPos(12,0,-16),new BlockPos(16,0,-14),new BlockPos(16,0,-10));
                    case "drinker"->List.of(new BlockPos(-1,0,-27),new BlockPos(-2,0,-33),new BlockPos(-7,0,-33));
                    default->List.of(new BlockPos(-10,0,-4),new BlockPos(-16,0,-4),new BlockPos(-17,0,1));
                };
                var p=player(f,"walk_"+home.id());
                for(var target:targets){var path=route(f.in,b,home,target);h.assertTrue(!path.isEmpty(),"body route through the real fragment to "+home.id()+" "+target);
                    var at=b.offset(DomesticLives.approach(home));
                    p.teleportTo(f.in,at.getX()+.5,at.getY(),at.getZ()+.5,0,0);
                    for(var n:path){p.move(MoverType.SELF,Vec3.atBottomCenterOf(n).subtract(p.position()));h.assertTrue(p.position().distanceToSqr(Vec3.atBottomCenterOf(n))<.001,"native collision permits each authored step in "+home.id()+": local="+n.subtract(b)+", actual="+p.position().subtract(Vec3.atLowerCornerOf(b))+", target="+target+", block="+f.in.getBlockState(n));}
                }
                for(var edit:DomesticLives.plan(home))if(edit.next().is(UncannyRegistry.FURNITURE.get())){
                    var kind=edit.next().getValue(UncannyFurnitureBlock.KIND);
                    if(kind==UncannyFurnitureBlock.Kind.WINDOW_FRAME)h.assertTrue(f.in.getBlockState(b.offset(edit.at()).relative(edit.next().getValue(UncannyFurnitureBlock.FACING).getOpposite())).is(Blocks.BLACK_WOOL),"every expanded-home window reveals a sealed black recess");
                    if(kind==UncannyFurnitureBlock.Kind.SOFA_LEFT||kind==UncannyFurnitureBlock.Kind.SOFA_RIGHT)h.assertTrue(edit.next().getValue(UncannyFurnitureBlock.FACING)==Direction.NORTH,"the sofa faces its coffee table rather than the wall");
                }
                try{export(f.in,b,home);}catch(Exception e){throw new RuntimeException(e);}
            }
            var kitchen=f.base(LabyrinthPlace.STRAIGHT_HALL);h.assertTrue(f.in.getBlockEntity(kitchen.offset(-6,0,-17)) instanceof net.minecraft.world.level.block.entity.BarrelBlockEntity,"the selected kitchen's original barrel is retained");
            h.assertTrue(f.in.getBlockState(kitchen.offset(7,1,-28)).is(Blocks.TINTED_GLASS)||!f.in.getBlockState(kitchen.offset(7,1,-28)).isAir(),"the unselected laundry still ends at its original wall/window");
            for(var p:f.players)h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"ordinary lives and handling add no Witness");h.succeed();
        });
    }
    @AfterBatch(batch="domestic_lives_rooms") public static void roomsDone(ServerLevel l){close();}
    private static Vec3 world(BlockPos at,Direction facing,Vec3 local){double x=local.x,z=local.z;for(int i=0;i<VignetteHandlingBlock.turns(facing);i++){double v=1-z;z=x;x=v;}return new Vec3(at.getX()+x,at.getY()+local.y,at.getZ()+z);}
    private static void use(ServerPlayer p,ServerLevel l,BlockPos at,Vec3 point){var s=l.getBlockState(at);var target=world(at,s.getValue(DomesticFittingBlock.FACING),point);p.teleportTo(l,at.getX()+2.5,at.getY()-1,at.getZ()+2.5,0,0);p.gameMode.useItemOn(p,l,p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(target,Direction.UP,at,false));}
    private static int stage(ServerLevel l,BlockPos at){return l.getBlockState(at).getValue(DomesticFittingBlock.STAGE);}
    @GameTest(template="empty",batch="domestic_lives_handling",timeoutTicks=300)
    public static void nativeUseSelectsIndividualCupsBottlesTonearmAndTunerAndInertSceneryStaysInert(GameTestHelper h){
        var f=fixture(h);var b=f.base(LabyrinthPlace.STRAIGHT_HALL);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,LabyrinthPlace.STRAIGHT_HALL));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native object-use area resident")).thenExecute(()->{
            build(f,LabyrinthPlace.STRAIGHT_HALL);var p=player(f,"domestic_hand");var peer=player(f,"domestic_peer");p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);var at=b.offset(-13,1,-25);
            for(var facing:Direction.Plane.HORIZONTAL){
                f.in.setBlock(at,DomesticFittingBlock.of(DomesticFittingBlock.Kind.BREAKFAST,facing),3);
                for(int i=0;i<3;i++){use(p,f.in,at,new Vec3((3.5+i*4)/16,.3,.45));h.assertTrue(stage(f.in,at)==(1<<(i+1))-1,"the actual cup touched turns independently in "+facing);}
                use(p,f.in,at,new Vec3(7.5/16,.3,.45));h.assertTrue(stage(f.in,at)==5,"only the middle cup turns");
                f.in.setBlock(at,DomesticFittingBlock.of(DomesticFittingBlock.Kind.BOTTLES,facing),3);
                for(int i=0;i<3;i++)use(p,f.in,at,new Vec3((2.5+i*5)/16,.3,.45));h.assertTrue(stage(f.in,at)==15,"each bottle can fall independently");
                for(int i=0;i<3;i++)use(p,f.in,at,new Vec3((2.5+i*5)/16,.3,.45));h.assertTrue(stage(f.in,at)==8,"righting the bottles leaves the actual spill");
                f.in.setBlock(at,DomesticFittingBlock.of(DomesticFittingBlock.Kind.RECORD_PLAYER,facing),3);
                use(p,f.in,at,new Vec3(.5,.85,.9));h.assertTrue(stage(f.in,at)==1,"native lid opens");use(p,f.in,at,new Vec3(.6,.85,.5));h.assertTrue(stage(f.in,at)==3,"lowering the real tonearm starts the record");
                use(p,f.in,at,new Vec3(.15,.85,.5));h.assertTrue(stage(f.in,at)==3,"the record cannot turn over under the needle");use(p,f.in,at,new Vec3(.6,.85,.5));use(p,f.in,at,new Vec3(.15,.85,.5));h.assertTrue(stage(f.in,at)==5,"the parked arm permits turning the record over");
                use(p,f.in,at,new Vec3(.6,.85,.5));use(p,f.in,at,new Vec3(.5,.85,.9));h.assertTrue(stage(f.in,at)==4,"closing the lid parks the arm and retains the selected side");
                f.in.setBlock(at,DomesticFittingBlock.of(DomesticFittingBlock.Kind.RADIO,facing),3);use(p,f.in,at,new Vec3(.25,.2,.85));use(p,f.in,at,new Vec3(.75,.2,.85));use(p,f.in,at,new Vec3(.75,.2,.85));h.assertTrue(stage(f.in,at)==6&&f.in.getBlockState(at).getLightEmission(f.in,at)==2,"independent power and tuning move the saved physical dial");
                for(var k:List.of(DomesticFittingBlock.Kind.PACKED_CLOTHES,DomesticFittingBlock.Kind.BOOK_BOX,DomesticFittingBlock.Kind.BOTTLE_RETURNS,DomesticFittingBlock.Kind.DESK_REMNANTS,DomesticFittingBlock.Kind.PHOTO_FAMILY,DomesticFittingBlock.Kind.PHOTO_COUPLE)){
                    f.in.setBlock(at,DomesticFittingBlock.of(k,facing),3);use(p,f.in,at,new Vec3(.5,.5,.5));h.assertTrue(stage(f.in,at)==1,"native handling exposes the real contents or face of "+k);use(p,f.in,at,new Vec3(.5,.5,.5));h.assertTrue(stage(f.in,at)==0,"the same object can be closed/turned back without creating an item: "+k);
                }
                f.in.setBlock(at,DomesticFittingBlock.of(DomesticFittingBlock.Kind.EMPTY_BOTTLES,facing),3);use(p,f.in,at,new Vec3(.5,.2,.5));h.assertTrue(stage(f.in,at)==1,"empty return bottles can be disturbed with a physical clink");
                f.in.setBlock(at,DomesticFittingBlock.of(DomesticFittingBlock.Kind.CLOSET_LOWER,facing),3);f.in.setBlock(at.above(),DomesticFittingBlock.of(DomesticFittingBlock.Kind.CLOSET_UPPER,facing),3);use(p,f.in,at.above(),new Vec3(.5,.5,.5));h.assertTrue(stage(f.in,at)==1&&stage(f.in,at.above())==1,"using either closet half opens the same full-height door");use(p,f.in,at,new Vec3(.5,.5,.5));h.assertTrue(stage(f.in,at)==0&&stage(f.in,at.above())==0,"closing keeps both actual halves in sync");f.in.setBlock(at.above(),Blocks.AIR.defaultBlockState(),3);

            }
            var before=f.in.getBlockState(at);f.reload();h.assertTrue(f.in.getBlockState(at).equals(before),"SavedData reload leaves the native tuned block state intact");
            for(var k:List.of(DomesticFittingBlock.Kind.TOPPLED_CHAIR,DomesticFittingBlock.Kind.TOPPLED_TABLE,DomesticFittingBlock.Kind.SCRAPES)){var s=DomesticFittingBlock.of(k,Direction.NORTH);f.in.setBlock(at,s,3);use(p,f.in,at,new Vec3(.5,.2,.5));h.assertTrue(f.in.getBlockState(at).equals(s)&&p.containerMenu==p.inventoryMenu,"disturbed furniture and scrapes have no use action");}
            h.assertTrue(p.getInventory().isEmpty()&&peer.getInventory().isEmpty()&&WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.count(f.data(),peer.getUUID())==0,"handling never creates supplies, pages or peer credit");h.succeed();
        });
    }
    @AfterBatch(batch="domestic_lives_handling") public static void handlingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="domestic_lives_old",timeoutTicks=800)
    public static void slicedOldHallAppendWaitsForBodiesAndCamerasAndPreservesFiniteOriginalsEditsAndRemoval(GameTestHelper h){
        var f=fixture(h);var home=DomesticLives.HOMES.getFirst();var b=f.base(home.hall());f.chunks.hold(f.in,IndianLakeRooms.bounds(b,home.hall()));ServerPlayer[] observer={null};Object[] original={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"old native hall resident")).thenExecute(()->{
            build(f,home.hall());for(int x=home.x0();x<=home.x1();x++)for(int z=home.z0();z<=home.z1();z++)for(int y=-1;y<=4;y++)f.in.setBlock(b.offset(x,y,z),Blocks.WHITE_TERRACOTTA.defaultBlockState(),2);
            var tag=f.data().state(DomesticLives.STATE);tag.remove(DomesticLives.key(b,home));tag.remove(DomesticLives.key(b,home)+":cursor");f.data().setState(DomesticLives.STATE,tag);
            f.in.setBlock(b.offset(-20,2,-17),Blocks.GOLD_BLOCK.defaultBlockState(),3);f.in.setBlock(b.offset(-12,0,-25),Blocks.CHEST.defaultBlockState(),3);((net.minecraft.world.Container)f.in.getBlockEntity(b.offset(-12,0,-25))).setItem(0,new ItemStack(Items.DIAMOND));original[0]=f.in.getBlockEntity(b.offset(-6,0,-17));((net.minecraft.world.Container)original[0]).setItem(0,new ItemStack(Items.EMERALD,3));
            observer[0]=player(f,"domestic_camera");observer[0].setGameMode(GameType.SPECTATOR);observer[0].teleportTo(f.in,b.getX()-12.5,b.getY()+2,b.getZ()-20.5,0,0);h.assertTrue(!DomesticLives.step(f.in,b,home),"a real spectator camera pauses the append");
            observer[0].teleportTo(f.in,b.getX()+60,b.getY()+2,b.getZ(),0,0);var stand=EntityType.ARMOR_STAND.create(f.in);stand.setNoGravity(true);stand.moveTo(b.getX()-20.5,b.getY(),b.getZ()-20.5);f.in.addFreshEntity(stand);f.extras.add(stand);h.assertTrue(!DomesticLives.step(f.in,b,home),"an unattended living body also pauses construction");stand.discard();
        }).thenWaitUntil(()->h.assertTrue(DomesticLives.step(f.in,b,home),"bounded append completes after actual vacancy")).thenExecute(()->{
            h.assertTrue(f.in.getBlockState(b.offset(-20,2,-17)).is(Blocks.GOLD_BLOCK),"an edited wall stays exact");h.assertTrue(f.in.getBlockEntity(b.offset(-6,0,-17))==original[0]&&((net.minecraft.world.Container)original[0]).getItem(0).getCount()==3,"the original fragment barrel and finite contents are never rebuilt");
            h.assertTrue(((net.minecraft.world.Container)f.in.getBlockEntity(b.offset(-12,0,-25))).getItem(0).is(Items.DIAMOND),"an existing chest in the extension is kept");
            var at=b.offset(-13,1,-25);h.assertTrue(f.in.getBlockState(at).is(DomesticRegistry.FITTING.get()),"the authored cup tray exists before its later removal");f.in.setBlock(at,Blocks.AIR.defaultBlockState(),3);f.reload();DomesticLives.step(f.in,b,home);h.assertTrue(f.in.getBlockState(at).isAir(),"the finished saved checkpoint never restores a later removal");
            h.assertTrue(f.in.getEntitiesOfClass(LivingEntity.class,home.area(b),e->!(e instanceof ServerPlayer)).isEmpty(),"the old-save append never spawns household NPCs");h.succeed();
        });
    }
    @AfterBatch(batch="domestic_lives_old") public static void oldDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="domestic_lives_geometry",timeoutTicks=50)
    public static void everyHouseholdStateKeepsCollisionInsideItsOwnNativeCell(GameTestHelper h){
        var l=h.getLevel();var at=h.absolutePos(BlockPos.ZERO);
        for(long seed=0;seed<1000;seed++){
            var chosen=DomesticLives.selection(seed);h.assertTrue(chosen.equals(DomesticLives.selection(seed))&&chosen.size()>=1&&chosen.size()<=2,"home rarity is stable per world and bounded");
            h.assertTrue(!(chosen.contains("students")&&chosen.contains("drinker")),"two full homes are never put in the same ordinary hall");
        }
        var shapes=new IdentityHashMap<BlockState,net.minecraft.world.phys.shapes.VoxelShape>();
        for(var k:DomesticFittingBlock.Kind.values())for(var f:Direction.Plane.HORIZONTAL)for(int n=0;n<16;n++){var s=DomesticFittingBlock.of(k,f).setValue(DomesticFittingBlock.STAGE,n);var shape=s.getCollisionShape(l,at);shapes.put(s,shape);
            h.assertTrue(s.getShape(l,at)==shape&&s.getShape(l,at.above())==shape,"repeated native support/picking queries reuse the same immutable state mesh");
            if(k==DomesticFittingBlock.Kind.SCRAPES){h.assertTrue(shape.isEmpty(),"floor scrapes never catch feet");continue;}
            h.assertTrue(!shape.isEmpty()&&shape.min(Direction.Axis.X)>=0&&shape.max(Direction.Axis.X)<=1&&shape.min(Direction.Axis.Y)>=0&&shape.max(Direction.Axis.Y)<=1&&shape.min(Direction.Axis.Z)>=0&&shape.max(Direction.Axis.Z)<=1,"rendered household cuboids have bounded matching native collision: "+k+" "+f+" "+n);
        }
        long refreshStarted=System.nanoTime();Blocks.rebuildCache();
        for(var entry:shapes.entrySet())h.assertTrue(entry.getKey().getShape(l,at)==entry.getValue()&&entry.getKey().getCollisionShape(l,at)==entry.getValue(),"the actual login registry refresh retains every original household collision mesh");
        TheOldestHouse.LOGGER.info("Native registry refresh retained all {} household state meshes in {} ms",shapes.size(),(System.nanoTime()-refreshStarted)/1_000_000);
        h.succeed();
    }
    @AfterBatch(batch="domestic_lives_geometry") public static void geometryDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="domestic_lives_repair",timeoutTicks=800)
    public static void olderHomeRepairClearsTheRealSnippetRouteWithoutReplacingOriginalsOrEdits(GameTestHelper h){
        var f=fixture(h);var home=DomesticLives.HOMES.getFirst();var b=f.base(home.hall());f.chunks.hold(f.in,IndianLakeRooms.bounds(b,home.hall()));Object[] original={null};ServerPlayer[] observer={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"legacy home and original approach are resident")).thenExecute(()->{
            build(f,home.hall());var old=new HashSet<BlockPos>();for(var e:DomesticHomeRepairs.legacyPlan(home)){old.add(e.at());f.in.setBlock(b.offset(e.at()),e.next(),2);}
            for(var e:DomesticLives.plan(home))if(!old.contains(e.at())&&f.in.getBlockEntity(b.offset(e.at()))==null)f.in.setBlock(b.offset(e.at()),Blocks.WHITE_TERRACOTTA.defaultBlockState(),2);
            var state=f.data().state(DomesticHomeRepairs.STATE);state.remove(DomesticLives.key(b,home));state.remove(DomesticLives.key(b,home)+":cursor");f.data().setState(DomesticHomeRepairs.STATE,state);
            original[0]=f.in.getBlockEntity(b.offset(-6,0,-17));((net.minecraft.world.Container)original[0]).setItem(0,new ItemStack(Items.EMERALD,3));
            f.in.setBlock(b.offset(-17,0,-15),Blocks.CHEST.defaultBlockState(),3);((net.minecraft.world.Container)f.in.getBlockEntity(b.offset(-17,0,-15))).setItem(0,new ItemStack(Items.DIAMOND));
            observer[0]=player(f,"home_repair_camera");observer[0].setGameMode(GameType.SPECTATOR);observer[0].teleportTo(f.in,b.getX()-3.5,b.getY()+1,b.getZ()-20.5,0,0);
            h.assertTrue(!DomesticHomeRepairs.step(f.in,b,home),"a camera in the original snippet pauses its adjoining work");observer[0].teleportTo(f.in,b.getX()+60,b.getY()+2,b.getZ(),0,0);
        }).thenWaitUntil(()->{DomesticHomeRepairs.step(f.in,b,home);h.assertTrue(f.data().state(DomesticHomeRepairs.STATE).getBoolean(DomesticLives.key(b,home)),"the bounded original-state repair finishes");}).thenExecute(()->{
            h.assertTrue(!route(f.in,b,home,new BlockPos(-13,0,-24)).isEmpty(),"there is a real route from the main corridor, through the original kitchen, into the home");
            h.assertTrue(f.in.getBlockEntity(b.offset(-6,0,-17))==original[0]&&((net.minecraft.world.Container)original[0]).getItem(0).getCount()==3,"the original snippet's finite barrel is never moved or restocked");
            h.assertTrue(((net.minecraft.world.Container)f.in.getBlockEntity(b.offset(-17,0,-15))).getItem(0).is(Items.DIAMOND),"a player's replacement chest is preserved where a wardrobe used to stand");
            var at=b.offset(-12,0,-15);f.in.setBlock(at,Blocks.GOLD_BLOCK.defaultBlockState(),3);f.reload();DomesticHomeRepairs.step(f.in,b,home);h.assertTrue(f.in.getBlockState(at).is(Blocks.GOLD_BLOCK),"the saved repair checkpoint never repeats");h.succeed();
        });
    }
    @AfterBatch(batch="domestic_lives_repair") public static void repairDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="domestic134_chance",timeoutTicks=40)
    public static void overturnedFurnitureIsRareInShallowRoutesAndRisesWithDepthWithoutRerolling(GameTestHelper h){
        int[] counts=new int[3];int[] depths={1,10,26};for(long route=0;route<40000;route++)for(int i=0;i<3;i++){boolean chosen=DomesticDisturbances.chosen(route,depths[i]);if(chosen)counts[i]++;h.assertTrue(chosen==DomesticDisturbances.chosen(route,depths[i]),"the same discovered route never rolls again");}
        h.assertTrue(counts[0]>650&&counts[0]<1300&&counts[1]>2100&&counts[1]<3100&&counts[2]>4800&&counts[2]<6200,"rare variations rise with actual depth over forty thousand independent routes: "+Arrays.toString(counts));h.succeed();
    }
    @GameTest(template="empty",batch="domestic134_variation",timeoutTicks=400)
    public static void anActualNormalChairCanRarelyFallAndKeepsItsMaterialsEditsAndSavedDiscovery(GameTestHelper h){
        var f=fixture(h);var hall=LabyrinthPlace.QUIET_ROOM;var b=f.base(hall);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,hall));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the native sitting room is resident")).thenExecute(()->{
            build(f,hall);HouseFurnishings.decorate(f.in,b,hall);long rare=0;while(!DomesticDisturbances.chosen(rare,1))rare++;
            var eye=player(f,"variation_camera");eye.setGameMode(GameType.SPECTATOR);eye.teleportTo(f.in,b.getX(),b.getY(),b.getZ()-8,0,0);h.assertTrue(!DomesticDisturbances.select(f.in,b,hall,rare,1),"a real camera prevents furniture movement");eye.teleportTo(f.in,b.getX()+100,b.getY(),b.getZ(),0,0);
            h.assertTrue(DomesticDisturbances.select(f.in,b,hall,rare,1),"the vacant physical room selects its rare variation");var choices=DomesticDisturbances.candidates(f.in,b,hall);var changed=choices.stream().filter(c->f.in.getBlockState(b.offset(c.at())).equals(c.tipped())).findFirst().orElseThrow();
            h.assertTrue(changed.tipped().getValue(DomesticFittingBlock.STAGE)>=2,"the tipped mesh is the original armchair/sofa/footstool family, not a different dining chair");f.reload();h.assertTrue(DomesticDisturbances.select(f.in,b,hall,rare,1)&&f.in.getBlockState(b.offset(changed.at())).equals(changed.tipped()),"the same route and actual fallen chair survive reload");
            long normal=rare+1;while(DomesticDisturbances.chosen(normal,1))normal++;DomesticDisturbances.select(f.in,b,hall,normal,1);h.assertTrue(f.in.getBlockState(b.offset(changed.at())).equals(changed.normal()),"a normal discovery has the original upright furniture, without changing floor or room type");
            DomesticDisturbances.select(f.in,b,hall,rare,1);var at=b.offset(changed.at());f.in.setBlock(at,Blocks.GOLD_BLOCK.defaultBlockState(),3);DomesticDisturbances.select(f.in,b,hall,normal,1);h.assertTrue(f.in.getBlockState(at).is(Blocks.GOLD_BLOCK),"player changes are not restored over");h.succeed();
        });
    }
    @AfterBatch(batch="domestic134_variation") public static void variation134Done(ServerLevel l){close();}
    @GameTest(template="empty",batch="domestic134_originals",timeoutTicks=400)
    public static void householdDrawingsAwardsAndLettersAreReadableFiniteSharedOriginals(GameTestHelper h){
        var f=fixture(h);var hall=LabyrinthPlace.BENT_HALL;var b=f.base(hall);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,hall));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the original bedroom fragment and expanded homes are resident")).thenExecute(()->{
            build(f,hall);var p=player(f,"family_paper_reader");var peer=player(f,"family_paper_peer");p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            for(var home:DomesticLives.HOMES)if(home.hall()==hall)for(var piece:DomesticStories.pieces(home.id())){
                var at=b.offset(piece.at());var be=(DomesticRecordBlockEntity)f.in.getBlockEntity(at);var original=be.book();h.assertTrue(!original.isEmpty(),"fresh household artifact contains its original: "+piece.id());
                var stored=be.saveWithFullMetadata(f.in.registryAccess());be.loadWithComponents(stored,f.in.registryAccess());
                h.assertTrue(ItemStack.matches(original,be.book())&&be.book().getHoverName().getString().equals(piece.title()),"every original, full heading and page survives the native item/chunk codec before collection: "+piece.id());
                p.teleportTo(f.in,at.getX()+.5,at.getY(),at.getZ()+2,0,0);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.gameMode.useItemOn(p,f.in,p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.UP,at,false));
                h.assertTrue(p.containerMenu instanceof DomesticRecordBlockEntity.PaperMenu,"ordinary native use opens the actual artifact: "+piece.id());var menu=p.containerMenu;menu.clicked(0,0,net.minecraft.world.inventory.ClickType.QUICK_MOVE,p);h.assertTrue(ItemStack.matches(original,be.book()),"an inventory packet cannot steal the display snapshot");
                h.assertTrue(menu.clickMenuButton(p,3)&&!menu.clickMenuButton(p,3)&&be.book().isEmpty(),"the same shared original transfers once: "+piece.id());
                h.assertTrue(!f.in.getBlockState(at).getValue(DomesticRecordBlock.PRESENT),"taking it leaves an empty paper mount or frame");var saved=be.saveWithFullMetadata(f.in.registryAccess());be.loadWithComponents(saved,f.in.registryAccess());be.seed(original);h.assertTrue(be.book().isEmpty(),"native BE reload and construction retries never replenish a collected original");
                peer.teleportTo(f.in,at.getX()+.5,at.getY(),at.getZ()+2,0,0);be.open(peer);h.assertTrue(peer.containerMenu==peer.inventoryMenu,"another reader finds the same empty frame");
            }
            h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.count(f.data(),peer.getUUID())==0,"optional household histories confer no story credit");h.succeed();
        });
    }
    @AfterBatch(batch="domestic134_originals") public static void originals134Done(ServerLevel l){close();}
    @GameTest(template="empty",batch="domestic134_continuity",timeoutTicks=700)
    public static void oldHomesGainTheirFragmentsPaletteAndStoriesWithoutRestockingOriginalContainers(GameTestHelper h){
        var f=fixture(h);var home=DomesticLives.HOMES.getFirst();var b=f.base(home.hall());f.chunks.hold(f.in,IndianLakeRooms.bounds(b,home.hall()));Object[] original={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the native old home is resident")).thenExecute(()->{
            build(f,home.hall());var old=DomesticLives0133.HOMES.getFirst();for(var e:DomesticLives0133.plan(old))f.in.setBlock(b.offset(e.at()),e.next(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS);
            var state=f.data().state(DomesticContinuity.STATE);state.remove(DomesticLives.key(b,home));f.data().setState(DomesticContinuity.STATE,state);original[0]=f.in.getBlockEntity(b.offset(-6,0,-17));((Container)original[0]).setItem(1,new ItemStack(Items.EMERALD,7));f.in.setBlock(b.offset(-12,-1,-23),Blocks.GOLD_BLOCK.defaultBlockState(),3);
            var camera=player(f,"palette_camera");camera.setGameMode(GameType.SPECTATOR);camera.teleportTo(f.in,b.getX()-12,b.getY(),b.getZ()-22,0,0);h.assertTrue(!DomesticContinuity.step(f.in,b,home),"a real camera pauses the palette/story repair");camera.teleportTo(f.in,b.getX()+100,b.getY(),b.getZ(),0,0);
        }).thenWaitUntil(()->{DomesticContinuity.step(f.in,b,home);h.assertTrue(f.data().state(DomesticContinuity.STATE).getBoolean(DomesticLives.key(b,home)),"the bounded old-state comparison finishes");}).thenExecute(()->{
            h.assertTrue(f.in.getBlockEntity(b.offset(-6,0,-17))==original[0]&&((Container)original[0]).getItem(1).getCount()==7,"the original small-kitchen barrel and exact finite contents survive");h.assertTrue(f.in.getBlockState(b.offset(-12,-1,-23)).is(Blocks.GOLD_BLOCK),"a player's floor edit is retained");
            h.assertTrue(f.in.getBlockState(b.offset(-11,-1,-24)).equals(LabyrinthDomestic.fragmentFloor(LabyrinthDomestic.Room.KITCHEN,-11,-24)),"the large kitchen continues the small kitchen's checker tiles");
            for(var piece:DomesticStories.pieces(home.id()))h.assertTrue(f.in.getBlockEntity(b.offset(piece.at())) instanceof DomesticRecordBlockEntity be&&!be.book().isEmpty(),"old homes receive each original once: "+piece.id());
            var at=b.offset(DomesticStories.pieces(home.id()).getFirst().at());f.in.setBlock(at,Blocks.AIR.defaultBlockState(),3);f.reload();DomesticContinuity.step(f.in,b,home);h.assertTrue(f.in.getBlockState(at).isAir(),"the saved checkpoint preserves later removal");h.succeed();
        });
    }
    @AfterBatch(batch="domestic134_continuity") public static void continuity134Done(ServerLevel l){close();}

    @GameTest(template="empty",batch="whale134_finish",timeoutTicks=700)
    public static void instituteShelvesBackOntoTheirWallAndNativeWindowsHaveActualPanels(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.WHALE;var b=f.base(place);f.chunks.hold(f.in,SceneReview.area(b,place));Object[] cabinet={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the native institute is resident")).thenExecute(()->{
            NovelRooms.build(f.in.getServer(),f.in,b,place);f.built.add(place);
            for(int y=0;y<3;y++){var at=b.offset(-12,y,-26);var shelf=f.in.getBlockState(at);h.assertTrue(shelf.is(NovelRegistry.WARD_FIXTURE.get())&&shelf.getValue(InstituteFixtureBlock.KIND)==InstituteFixtureBlock.Kind.SHELF&&shelf.getValue(InstituteFixtureBlock.FACING)==Direction.NORTH&&!f.in.getBlockState(at.south()).isAir(),"fresh shelves face the stair and sit against the partition");f.in.setBlock(at.north(),shelf,2|16);f.in.setBlock(at,Blocks.AIR.defaultBlockState(),2|16);}
            var pane=b.offset(-14,2,-11);var s=f.in.getBlockState(pane);h.assertTrue(s.getShape(f.in,pane).bounds().getZsize()>.99,"the fresh pane spans its supported native window");
            s=s.setValue(net.minecraft.world.level.block.IronBarsBlock.NORTH,false).setValue(net.minecraft.world.level.block.IronBarsBlock.SOUTH,false).setValue(net.minecraft.world.level.block.IronBarsBlock.EAST,false).setValue(net.minecraft.world.level.block.IronBarsBlock.WEST,false);f.in.setBlock(pane,s,2|16);
            var edited=b.offset(14,2,-11);var kept=f.in.getBlockState(edited).setValue(net.minecraft.world.level.block.IronBarsBlock.NORTH,false);f.in.setBlock(edited,kept,2|16);
            cabinet[0]=f.in.getBlockEntity(b.offset(12,0,-24));h.assertTrue(cabinet[0] instanceof Container,"the original native filing cabinet exists");((Container)cabinet[0]).setItem(3,new ItemStack(Items.EMERALD,5));
            var camera=player(f,"institute_camera");camera.setGameMode(GameType.SPECTATOR);camera.teleportTo(f.in,b.getX()-10,b.getY(),b.getZ()-29,0,0);h.assertTrue(!WhaleFinishing.repair(f.in,b),"a camera at the stairs prevents shelf movement");camera.teleportTo(f.in,b.getX()+100,b.getY(),b.getZ(),0,0);
        }).thenWaitUntil(()->h.assertTrue(WhaleFinishing.repair(f.in,b),"the small saved shelf/window units finish after vacancy")).thenExecute(()->{
            for(int y=0;y<3;y++)h.assertTrue(f.in.getBlockState(b.offset(-12,y,-27)).isAir()&&f.in.getBlockState(b.offset(-12,y,-26)).getValue(InstituteFixtureBlock.FACING)==Direction.NORTH,"the original shelf cells moved back one block without turning");
            var pane=b.offset(-14,2,-11);h.assertTrue(f.in.getBlockState(pane).getShape(f.in,pane).bounds().getZsize()>.99,"the old disconnected post becomes a real native window panel");h.assertTrue(!f.in.getBlockState(b.offset(14,2,-11)).getValue(net.minecraft.world.level.block.IronBarsBlock.NORTH),"an edited connected pane stays edited");
            h.assertTrue(f.in.getBlockEntity(b.offset(12,0,-24))==cabinet[0]&&((Container)cabinet[0]).getItem(3).getCount()==5,"the exact native cabinet and finite contents remain");
            try{region(f.in,b,"whale_stair_shelves",-13,-8,-31,-24,-1,3);region(f.in,b,"whale_window_panels",-15,-12,-12,-10,1,4);}catch(Exception e){throw new RuntimeException(e);}f.reload();f.in.setBlock(pane,Blocks.GOLD_BLOCK.defaultBlockState(),3);WhaleFinishing.repair(f.in,b);h.assertTrue(f.in.getBlockState(pane).is(Blocks.GOLD_BLOCK),"the saved finish does not run again");h.succeed();
        });
    }
    @AfterBatch(batch="whale134_finish") public static void whale134Done(ServerLevel l){close();}
    private static void region(ServerLevel l,BlockPos b,String name,int x0,int x1,int z0,int z1,int y0,int y1)throws Exception{
        var file=new JsonObject();file.addProperty("name",name);file.addProperty("viewYaw",name.equals("whale_window_panels")?-90:180);var palette=new JsonArray();var blocks=new JsonArray();var lookup=new LinkedHashMap<BlockState,Integer>();
        for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)for(int x=x0;x<=x1;x++){
            var state=l.getBlockState(b.offset(x,y,z));if(state.isAir())continue;int n=lookup.computeIfAbsent(state,key->{palette.add(BlockState.CODEC.encodeStart(JsonOps.INSTANCE,key).getOrThrow());return palette.size()-1;});var row=new JsonArray();row.add(x);row.add(y);row.add(z);row.add(n);blocks.add(row);
        }file.add("palette",palette);file.add("blocks",blocks);var folder=Path.of("../build/interior-environment-proof");Files.createDirectories(folder);Files.writeString(folder.resolve(name+".json"),new GsonBuilder().setPrettyPrinting().create().toJson(file));
    }
    private static void export(ServerLevel l,BlockPos b,DomesticLives.Home home)throws Exception{
        var file=new JsonObject();file.addProperty("name","domestic_"+home.id());var palette=new JsonArray();var blocks=new JsonArray();var lookup=new LinkedHashMap<BlockState,Integer>();
        for(int y=-1;y<=2;y++)for(int z=home.z0();z<home.z1();z++)for(int x=home.x0();x<=home.x1();x++){
            var s=l.getBlockState(b.offset(x,y,z));if(s.isAir())continue;int n=lookup.computeIfAbsent(s,key->{palette.add(BlockState.CODEC.encodeStart(JsonOps.INSTANCE,key).getOrThrow());return palette.size()-1;});
            var row=new JsonArray();row.add(x);row.add(y);row.add(z);row.add(n);blocks.add(row);
        }file.add("palette",palette);file.add("blocks",blocks);var folder=Path.of("../build/interior-environment-proof");Files.createDirectories(folder);Files.writeString(folder.resolve("domestic_"+home.id()+".json"),new GsonBuilder().setPrettyPrinting().create().toJson(file));
    }
}
