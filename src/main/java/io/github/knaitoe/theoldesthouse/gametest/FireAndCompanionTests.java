package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FireAndCompanionTests {
    private static final List<Entity> ROUTE_ENTITIES=new ArrayList<>();
    private static ServerLevel routeLevel;private static BlockPos routeTicket;
    private static void remove(ServerPlayer p){NativeTestPlayers.remove(p);}
    @AfterBatch(batch="companion_route") public static void cleanRoute(ServerLevel level){for(var e:ROUTE_ENTITIES){if(e instanceof ServerPlayer p)remove(p);else e.discard();}ROUTE_ENTITIES.clear();if(routeLevel!=null){routeLevel.getChunkSource().removeRegionTicket(TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(routeTicket),3,routeTicket);routeLevel=null;}}
    @GameTest(template="empty",batch="companion_route",timeoutTicks=900)
    public static void nativeGuideActuallyWalksPastStoppingRadiusAndAroundBend(GameTestHelper h){
        var owner=NativeTestPlayers.survival(h,"walking_guide_owner");var level=HouseTestLevel.get(owner.server,HouseDimensions.INTERIOR);
        BlockPos base=new BlockPos(h.absolutePos(BlockPos.ZERO).getX()+240000,180,240000);var floor=LabyrinthHalls.floor(LabyrinthPlace.BENT_HALL);
        // getChunkAt registers geometry; a portal ticket makes this remote native entity area tick.
        routeLevel=level;routeTicket=base.offset(-8,0,-15);level.getChunkSource().addRegionTicket(TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(routeTicket),3,routeTicket);
        for(var local:floor){var at=base.offset(local);level.getChunkAt(at);level.setBlock(at.below(),Blocks.STONE.defaultBlockState(),3);for(int y=0;y<3;y++)level.setBlock(at.above(y),Blocks.AIR.defaultBlockState(),3);}
        // Real walls prevent native navigation from cutting across the empty corner.
        for(var local:floor)for(var d:Direction.Plane.HORIZONTAL)if(!floor.contains(local.relative(d)))for(int y=0;y<3;y++)level.setBlock(base.offset(local.relative(d)).above(y),Blocks.STONE.defaultBlockState(),3);
        owner.teleportTo(level,base.getX()+.5,base.getY(),base.getZ()+.5,0,0);
        var dog=EntityType.WOLF.create(level);dog.tame(owner);dog.moveTo(Vec3.atBottomCenterOf(base));level.addFreshEntity(dog);ROUTE_ENTITIES.add(owner);ROUTE_ENTITIES.add(dog);
        BlockPos goal=base.offset(-15,0,-30);
        // The remote area ticks entities only once its ticket has promoted it, which on a busy server (run 1387: hundreds of chunks
        // still generating) can take longer than the route itself; the route's 280 ticks start when the dog first ticks there.
        long[] start={-1};
        h.onEachTick(()->{
            if(level.getEntity(dog.getUUID())!=dog)return;
            if(start[0]<0){
                if(dog.tickCount>0&&level.isPositionEntityTicking(dog.blockPosition()))start[0]=h.getTick();
                else if(h.getTick()>600)h.fail("the route's area never ticked entities: ticks="+dog.tickCount+", entityTicking="+level.isPositionEntityTicking(dog.blockPosition()));
                return;
            }
            if(h.getTick()-start[0]>280)h.fail("native route did not advance: ticks="+dog.tickCount+", position="+dog.position()+", base="+base+", noAi="+dog.isNoAi()+", navigationDone="+dog.getNavigation().isDone());
            owner.moveTo(dog.position().add(0,0,2));HillaryPaths.lead(dog,owner,goal,LabyrinthPlace.BENT_HALL,base);
            if(dog.getX()<base.getX()-8&&dog.getZ()<base.getZ()-17){
                h.assertTrue(dog.getY()>base.getY()-1&&dog.getHealth()>0,"the native guide walked on the connected floor around the bend");
                dog.discard();remove(owner);h.succeed();
            }
        });
    }
    @AfterBatch(batch="stair_light") public static void cleanLights(ServerLevel level){StaircaseFallingLight.clear();}
    /** 0.4.594: a light let fall over a rail is the well's; one on a tread is a dropped thing; it lands where there is something to land on, and lies there. */
    @GameTest(template="empty",batch="stair_light",timeoutTicks=420)
    public static void aLightLetFallIsTakenByTheWell(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"stair_light_one");var level=HouseTestLevel.get(p.server,HouseDimensions.INTERIOR);
        BlockPos origin=new BlockPos(h.absolutePos(BlockPos.ZERO).getX()+230000,0,230000);var base=FinaleArchitecture.base(origin);
        // A place inside the well, clear of everything: nothing is built at this origin.
        var hand=new Vec3(base.getX()+10.5,FinaleArchitecture.TOP-40,base.getZ()+10.5);var below=new Vec3(hand.x,hand.y-7,hand.z);
        var floor=BlockPos.containing(hand.x,hand.y-30,hand.z);var wall=BlockPos.containing(hand.x+2,hand.y-7,hand.z);
        // A tread to one side, two blocks below the hand: what lands there is a dropped thing.
        var tread=BlockPos.containing(hand.x-4,hand.y-3,hand.z);
        var held=new NativeTestChunks();
        Runnable clean=()->{
            held.close();
            for(int y=-40;y<=0;y++)level.setBlock(wall.above(y),Blocks.AIR.defaultBlockState(),3);
            level.setBlock(floor,Blocks.AIR.defaultBlockState(),3);level.setBlock(tread,Blocks.AIR.defaultBlockState(),3);
            for(var e:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(floor).inflate(8,40,8)))e.discard();
            StaircaseFallingLight.clear();remove(p);};
        try{
            level.getChunkAt(floor);level.getChunkAt(wall);p.teleportTo(level,hand.x,hand.y,hand.z,0,0);
            h.assertTrue(StaircaseFallingLight.isLight(new ItemStack(Items.TORCH))&&StaircaseFallingLight.isLight(new ItemStack(Items.SOUL_LANTERN))
                    &&!StaircaseFallingLight.isLight(new ItemStack(Items.STICK))&&!StaircaseFallingLight.isLight(new ItemStack(Items.CANDLE))
                    &&!StaircaseFallingLight.isLight(new ItemStack(Items.FLINT_AND_STEEL)),"a light is a thing that burns: a torch or a lantern, not a stick, an unlit candle or a striker");
            h.assertTrue(StaircaseFallingLight.tint(new ItemStack(Items.SOUL_TORCH))==StaircaseFallingLight.SOUL
                    &&StaircaseFallingLight.tint(new ItemStack(Items.TORCH))==StaircaseFallingLight.WARM,"a soul fire keeps its own colour as it falls");
            var stick=new net.minecraft.world.entity.item.ItemEntity(level,hand.x,hand.y,hand.z,new ItemStack(Items.STICK));
            h.assertTrue(!StaircaseFallingLight.track(p,stick,origin)&&StaircaseFallingLight.onTheirWay()==0,"a stick let fall is only a stick");
            var torch=new net.minecraft.world.entity.item.ItemEntity(level,hand.x,hand.y,hand.z,new ItemStack(Items.TORCH));torch.setDeltaMovement(.02,-.2,0);
            h.assertTrue(StaircaseFallingLight.track(p,torch,origin)&&StaircaseFallingLight.onTheirWay()==1,"a torch let go in the well is watched");
            torch.setPos(hand.x,hand.y-3,hand.z);
            h.assertTrue(!StaircaseFallingLight.given(torch,hand.y,origin),"three blocks below the hand it is not yet the well's");
            torch.setPos(below.x,below.y,below.z);torch.setOnGround(true);
            h.assertTrue(!StaircaseFallingLight.given(torch,hand.y,origin),"a torch lying on a tread is not the well's");
            torch.setOnGround(false);
            h.assertTrue(StaircaseFallingLight.given(torch,hand.y,origin),"seven blocks below the hand and still falling, the well has it");
            torch.setPos(base.getX()+60.5,below.y,below.z);
            h.assertTrue(!StaircaseFallingLight.given(torch,hand.y,origin),"outside the well's walls nothing is taken");
            torch.setPos(below.x,below.y,below.z);
            // Something to land on, twenty-three blocks further down.
            level.setBlock(floor,Blocks.STONE.defaultBlockState(),3);
            var fall=StaircaseFallingLight.let(level,torch,origin);
            h.assertTrue(torch.isRemoved(),"the torch is out of the world while it falls");
            h.assertTrue(fall.hit()&&Math.abs(fall.end().y-(floor.getY()+1))<.1&&BlockPos.containing(fall.end()).below().equals(floor),"it comes down on the stone that is really there: "+fall.end());
            // A client stepping the same start and speed arrives where the server found the landing.
            var motion=new StaircaseFallingLight.Motion(fall.start(),fall.speed());for(int t=1;t<fall.ticks();t++)motion.step();
            h.assertTrue(motion.y>fall.end().y&&motion.y-fall.end().y<=StaircaseFallingLight.TERMINAL+.1&&Math.hypot(motion.x-fall.end().x,motion.z-fall.end().z)<.5,
                    "a client stepping the same fall is over the landing a tick before it: "+motion.y+" above "+fall.end().y);
            h.assertTrue(StaircaseFallingLight.audience(level,origin).contains(p),"the reader who let it fall is told of it");
            // With nothing below, it is followed a very long way down: here, to the floor of the world, far out of anyone's hearing.
            level.setBlock(floor,Blocks.AIR.defaultBlockState(),3);
            var lost=StaircaseFallingLight.fall(level,below,new Vec3(0,-.2,0),StaircaseFallingLight.WARM);
            h.assertTrue(lost.end().y<hand.y-1000&&lost.ticks()>500&&lost.ticks()<StaircaseFallingLight.LONGEST,"with nothing under it the light goes on down: "+lost.end()+" after "+lost.ticks()+" ticks");
            // A wall it would drift into does not stop it in mid-air: it is followed straight down, as a client will draw it.
            for(int y=-40;y<=0;y++)level.setBlock(wall.above(y),Blocks.STONE.defaultBlockState(),3);
            level.setBlock(floor,Blocks.STONE.defaultBlockState(),3);
            var walled=StaircaseFallingLight.fall(level,below,new Vec3(.5,-.2,0),StaircaseFallingLight.WARM);
            h.assertTrue(walled.hit()&&walled.speed().x==0&&BlockPos.containing(walled.end()).below().equals(floor),"a light that would strike a wall falls straight down beside it: "+walled.end()+" at "+walled.speed());
            for(int y=-40;y<=0;y++)level.setBlock(wall.above(y),Blocks.AIR.defaultBlockState(),3);
            // The watch itself, with real things falling: lanterns let go over the drop, and torches that come down on a tread.
            // Their chunks are held, and tick their entities, before anything is let go: an item in a chunk that is not ticking never falls.
            level.setBlock(tread,Blocks.STONE.defaultBlockState(),3);
            var box=new AABB(floor).inflate(8,40,8);held.hold(level,box);
            var lantern=new net.minecraft.world.entity.item.ItemEntity(level,hand.x,hand.y,hand.z,new ItemStack(Items.LANTERN,3));lantern.setDeltaMovement(0,0,0);lantern.setPickUpDelay(32767);
            var dropped=new net.minecraft.world.entity.item.ItemEntity(level,tread.getX()+.5,hand.y,tread.getZ()+.5,new ItemStack(Items.TORCH,2));dropped.setDeltaMovement(0,0,0);dropped.setPickUpDelay(32767);
            // Polled until every fall is over; on the wait's last look the fixture is cleared before it fails.
            int[] looks={0};boolean[] letGo={false};
            SceneReady.when(h,()->{
                String missing;
                try{
                    if(!letGo[0]){
                        missing=SceneReady.notTicking(level,box);
                        if(missing==null){
                            level.addFreshEntity(lantern);level.addFreshEntity(dropped);letGo[0]=true;
                            missing=StaircaseFallingLight.track(p,lantern,origin)&&StaircaseFallingLight.track(p,dropped,origin)?"the lights have only now been let go":"both should be watched as they are let go";
                        }
                    }else{
                        var lying=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(floor).inflate(3,2,3));
                        int torches=lying.stream().filter(e->e.getItem().is(Items.TORCH)).mapToInt(e->e.getItem().getCount()).sum();
                        int lanterns=lying.stream().filter(e->e!=lantern&&e.getItem().is(Items.LANTERN)).mapToInt(e->e.getItem().getCount()).sum();
                        missing=torches!=1?"the torch let fall should lie where it came down, not be lost: "+lying
                                :!lantern.isRemoved()?"the lanterns let go over the drop should be the well's; still a dropped thing at "+lantern.position()+", watched "+StaircaseFallingLight.onTheirWay()
                                :lanterns!=3?"all three lanterns should lie at the bottom of the drop once their fall is over: "+lying
                                :dropped.isRemoved()||!dropped.onGround()||dropped.getItem().getCount()!=2||!BlockPos.containing(dropped.position()).below().equals(tread)
                                        ?"what came down on a tread should still be the dropped thing it was, lying there: "+dropped+" removed "+dropped.isRemoved()+" on ground "+dropped.onGround():null;
                    }
                }catch(RuntimeException e){clean.run();throw new GameTestAssertException("the falling-light case broke: "+e);}
                if(missing!=null&&++looks[0]>150)clean.run();
                return missing;
            },2,300,()->{clean.run();h.succeed();});
        }catch(RuntimeException|Error e){clean.run();throw e;}
    }
    /** 0.4.594: a reader at a rail in the well who looks down leans out over it, and a light let go then falls from the hand held out there. */
    @GameTest(template="empty",batch="stair_light",timeoutTicks=100)
    public static void aReaderLeansOverARailToLetALightFall(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"stair_light_lean");var level=HouseTestLevel.get(p.server,HouseDimensions.INTERIOR);
        BlockPos origin=new BlockPos(h.absolutePos(BlockPos.ZERO).getX()+240000,0,240000);var base=FinaleArchitecture.base(origin);
        // A tread, with a rail on a stone string to its west and the drop beyond: a flight's inner edge, as it is built.
        var tread=new BlockPos(base.getX()+10,FinaleArchitecture.TOP-60,base.getZ()+10);BlockPos string=tread.west(),rail=string.above(),ledge=rail.west().below();
        var bars=io.github.knaitoe.theoldesthouse.labyrinth.StairWell.BALUSTRADE.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH,true).setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH,true);
        double y=tread.getY()+1,z=tread.getZ()+.5,atRail=tread.getX()+.1,back=tread.getX()+.5;float west=90,east=-90,eye=1.62F;
        try{
            level.getChunkAt(tread);level.setBlock(tread,Blocks.STONE.defaultBlockState(),3);level.setBlock(string,Blocks.STONE.defaultBlockState(),3);level.setBlock(rail,bars,3);
            var over=io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLean.over(level,origin,new Vec3(atRail,y,z),eye,west,50);
            h.assertTrue(over!=null&&Math.abs(over.x+1.3)<.001&&over.z==0&&Math.abs(over.y+.25)<.001,"at the rail, looking down, the eye goes out past the rail's far face and a little lower: "+over);
            h.assertTrue(atRail+over.x<rail.getX()&&y+eye+over.y>rail.getY()+1,"leaning, the eye is beyond the string's face and above the rail's top");
            h.assertTrue(io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLean.over(level,origin,new Vec3(atRail,y,z),eye,west,10)==null,"looking out level, a reader stands as they are");
            h.assertTrue(io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLean.over(level,origin,new Vec3(back,y,z),eye,west,50)==null,"a step back from the rail is not at it");
            h.assertTrue(io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLean.over(level,origin,new Vec3(atRail,y,z),eye,east,50)==null,"with no rail the way they face there is nothing to lean over");
            h.assertTrue(io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLean.over(level,origin.offset(4000,0,4000),new Vec3(atRail,y,z),eye,west,50)==null,"a rail that is not in the well is only a rail");
            h.assertTrue(io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLean.over(level,origin,new Vec3(atRail,y,z),1.0,west,50)==null,"an eye that would not clear the rail's top does not go through it");
            level.setBlock(ledge,Blocks.STONE.defaultBlockState(),3);
            h.assertTrue(io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLean.over(level,origin,new Vec3(atRail,y,z),eye,west,50)==null,"where there is floor beyond the rail there is no drop to lean out over");
            level.setBlock(ledge,Blocks.AIR.defaultBlockState(),3);
            // Let go while leaning, a light falls from the hand; a stick does not, and neither does a light let go looking out level.
            p.teleportTo(level,atRail,y,z,west,50);
            var torch=new net.minecraft.world.entity.item.ItemEntity(level,p.getX(),p.getEyeY()-.3,p.getZ(),new ItemStack(Items.TORCH));torch.setDeltaMovement(-.2,.1,0);
            h.assertTrue(StaircaseFallingLight.held(p,torch,origin),"a light let go while leaning leaves the hand held out over the rail");
            h.assertTrue(torch.getX()+.125<rail.getX()&&torch.getY()>rail.getY()+1&&Math.abs(torch.getDeltaMovement().x)<=.03&&torch.getDeltaMovement().y<0,
                    "it starts clear of the string's face, above the rail, and falls: "+torch.position()+" at "+torch.getDeltaMovement());
            var stick=new net.minecraft.world.entity.item.ItemEntity(level,p.getX(),p.getEyeY()-.3,p.getZ(),new ItemStack(Items.STICK));
            h.assertTrue(!StaircaseFallingLight.held(p,stick,origin)&&stick.getX()==p.getX(),"anything else let go at a rail stays in the reader's reach");
            p.teleportTo(level,atRail,y,z,west,0);
            var level2=new net.minecraft.world.entity.item.ItemEntity(level,p.getX(),p.getEyeY()-.3,p.getZ(),new ItemStack(Items.TORCH));
            h.assertTrue(!StaircaseFallingLight.held(p,level2,origin)&&level2.getX()==p.getX(),"looking out level, a light is thrown as anything is");
            h.succeed();
        }finally{
            // Nothing here is watched or on its way: the well's lists are left alone, for the case beside this one in the batch.
            for(var at:List.of(tread,string,rail,ledge))level.setBlock(at,Blocks.AIR.defaultBlockState(),3);
            remove(p);
        }
    }
    @GameTest(template="empty",batch="stair_fire",timeoutTicks=160)
    public static void fiveNativeFiresConsumeBookAndUnlockOnlyTheirReader(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"stair_fire_reader");var peer=h.makeMockServerPlayerInLevel();var level=HouseTestLevel.get(p.server,HouseDimensions.INTERIOR);
        BlockPos origin=new BlockPos(h.absolutePos(BlockPos.ZERO).getX()+220000,0,220000);
        var old=FinaleProgress.world(p.server);var fires=StaircaseFire.braziers(origin);
        try{
            p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.FLINT_AND_STEEL));
            p.setItemInHand(InteractionHand.OFF_HAND,StaircaseFire.book(p.getUUID(),5));
            CompoundTag record=new CompoundTag();record.putString("Phase",FinaleProgress.Phase.STAIRCASE.name());record.putBoolean("StairFireVersion",true);FinaleProgress.save(p.server,p.getUUID(),record);
            for(int i=0;i<5;i++){
                var at=fires.get(i);level.getChunkAt(at);level.setBlock(at.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(at,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false),3);
                p.teleportTo(level,at.getX()+1.5,at.getY(),at.getZ()+.5,0,0);
                h.assertTrue(StaircaseFire.ignite(p,origin,at),"the actual hearth accepts its ordered leaf "+i);
                h.assertTrue(level.getBlockState(at).getValue(CampfireBlock.LIT),"a real native fire lights");
                h.assertTrue(StaircaseFire.flames(FinaleProgress.player(p.server,p.getUUID()))==i+1,"the reader advances once");
                level.setBlock(at,level.getBlockState(at).setValue(CampfireBlock.LIT,false),3);
                h.assertTrue(!StaircaseFire.ignite(p,origin,at)
                        &&level.getBlockState(at).getValue(CampfireBlock.LIT)
                        &&StaircaseFire.flames(FinaleProgress.player(p.server,p.getUUID()))==i+1,
                        "an extinguished hearth restores its flame without spending another leaf");
            }
            h.assertTrue(p.getOffhandItem().isEmpty()&&p.getMainHandItem().getDamageValue()==5,"the fifth leaf consumes the book and five uses of the striker");
            var data=LabyrinthData.get(p.server);var loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),level.registryAccess()),level.registryAccess());
            h.assertTrue(StaircaseFire.open(loaded.state(FinaleProgress.STATE).getCompound(p.getUUID().toString()))
                    &&!StaircaseFire.open(loaded.state(FinaleProgress.STATE).getCompound(peer.getUUID().toString())),"reload preserves personal light; a peer cannot borrow it");
            h.assertTrue(!StaircaseFire.take(p)&&!StaircaseFire.take(p),"a story that has fully burned is not bound again");
        }finally{LabyrinthData.get(p.server).setState(FinaleProgress.STATE,old);for(var at:fires){level.setBlock(at,Blocks.AIR.defaultBlockState(),3);level.setBlock(at.below(),Blocks.AIR.defaultBlockState(),3);}remove(p);remove(peer);}
        h.succeed();
    }
    @GameTest(template="empty",batch="stair_fire",timeoutTicks=160)
    public static void unlitDepthStopsAdvanceAndOldDeepVisitsRemainOpen(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"stair_dark_walker");var level=HouseTestLevel.get(p.server,HouseDimensions.INTERIOR);
        BlockPos origin=new BlockPos(h.absolutePos(BlockPos.ZERO).getX()+230000,0,230000);
        var record=new CompoundTag();
        var safe=StaircaseFire.landings(origin).getFirst();var edge=StaircaseFire.edge(origin,record);
        level.getChunkAt(safe);level.setBlock(safe.below(),Blocks.STONE.defaultBlockState(),3);
        try{
            p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.teleportTo(level,edge.getX()+.5,edge.getY()-2,edge.getZ()+.5,0,0);
            boolean blocked=StaircaseFire.tick(p,origin,record);
            h.assertTrue(blocked,"the native survival boundary blocks: creative="+p.isCreative()+", fires="+StaircaseFire.flames(record)+", edge="+edge+", position="+p.position());
            h.assertTrue(p.position().distanceToSqr(Vec3.atBottomCenterOf(safe))<1,"native movement is returned safely: position="+p.position()+", safe="+safe);
            p.moveTo(safe.getX()+.5,FinaleArchitecture.TOP,safe.getZ()+.5);
            h.assertTrue(!StaircaseFire.tick(p,origin,record),"movement back toward the entry remains available");
            record.putInt("StairFires",5);p.moveTo(safe.getX()+.5,FinaleArchitecture.ARENA,safe.getZ()+.5);
            h.assertTrue(!StaircaseFire.tick(p,origin,record),"five fires allow the full physical descent");
            var fresh=new CompoundTag();
            p.moveTo(safe.getX()+.5,FinaleArchitecture.ARENA,safe.getZ()+.5);
            h.assertTrue(StaircaseFire.tick(p,origin,fresh)&&StaircaseFire.flames(fresh)==0,
                    "a fall to the arena does not award any personal fires");
            h.assertTrue(!WitnessAccount.ready(LabyrinthData.get(p.server),p.getUUID()),"lighting is no additional Witness source");
        }finally{level.setBlock(safe.below(),Blocks.AIR.defaultBlockState(),3);remove(p);}
        h.succeed();
    }
    @GameTest(template="empty",batch="companion_doors",timeoutTicks=160)
    public static void nativeThresholdCarriesAheadGuideButLeavesStayAndForeignPets(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();var stranger=h.makeMockServerPlayerInLevel();var level=h.getLevel();
        BlockPos at=h.absolutePos(new BlockPos(2,2,2));p.moveTo(Vec3.atBottomCenterOf(at));
        var guide=EntityType.WOLF.create(level);var stay=EntityType.CAT.create(level);var foreign=EntityType.WOLF.create(level);
        guide.tame(p);stay.tame(p);foreign.tame(stranger);guide.moveTo(p.position().add(20,0,0));stay.moveTo(p.position().add(1,0,0));foreign.moveTo(p.position().add(1,0,1));
        // Persisted tracking is allowed in a source world; issuing it still requires the interior.
        guide.getPersistentData().putInt("HouseCompanionOrder",CompanionOrders.Order.DEEPER.ordinal());guide.setHealth(4);
        CompanionOrders.issue(stay,p,CompanionOrders.Order.STAY);stay.setOrderedToSit(false);
        level.addFreshEntity(guide);level.addFreshEntity(stay);level.addFreshEntity(foreign);UUID id=guide.getUUID();
        h.runAfterDelay(2,()->{
            try{
                var followers=CompanionOrders.followingAll(p);
                h.assertTrue(followers.contains(guide)&&!followers.contains(stay)&&!followers.contains(foreign),"the guide ahead crosses; saved Stay and someone else's pet do not");
                Vec3 left=stay.position();HouseInternalTeleport.shift(p,p.position().add(60,0,0),90);
                h.assertTrue(guide.distanceToSqr(p)<4&&guide.getUUID().equals(id)&&guide.getHealth()==4
                        &&CompanionOrders.order(guide)==CompanionOrders.Order.DEEPER,"the actual seamless door move retains native identity, health and order");
                h.assertTrue(stay.position().distanceToSqr(left)<1,"Stay remains on the original side");
            }finally{guide.discard();stay.discard();foreign.discard();remove(p);remove(stranger);}
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="companion_doors")
    public static void nativePatKeepsDogAndCatOrdersAndThrottlesResponse(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();p.moveTo(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,2,2))));
        try{for(boolean feline:List.of(false,true)){
            TamableAnimal pet=feline?EntityType.CAT.create(h.getLevel()):EntityType.WOLF.create(h.getLevel());pet.tame(p);pet.moveTo(p.position().add(1,0,0));h.getLevel().addFreshEntity(pet);
            CompanionOrders.issue(pet,p,CompanionOrders.Order.STAY);
            h.assertTrue(CompanionOrders.pet(pet,p)&&!CompanionOrders.pet(pet,p),"a native pat starts exactly one bounded response");
            h.assertTrue(pet.getPersistentData().getLong("CompanionPatUntil")>h.getLevel().getGameTime()
                    &&CompanionOrders.order(pet)==CompanionOrders.Order.STAY&&pet.isOrderedToSit(),"dog and cat responses retain their movement command");pet.discard();
        }}finally{remove(p);}h.succeed();
    }
}
