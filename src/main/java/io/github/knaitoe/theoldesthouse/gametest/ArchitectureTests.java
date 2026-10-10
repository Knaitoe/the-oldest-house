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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ArchitectureTests {
    /** A player pressed into the closed far door's cell is still in the hallway, never ejected through the wall. */
    @GameTest(template="empty") public static void pressingTheHallwayDoorStaysInsideTheHouse(GameTestHelper h){
        var origin=new BlockPos(0,80,0);double x=HouseLayout.AXIS_X+.5,y=origin.getY()+1,door=HouseImpossibleHallway.END_Z_OFFSET;
        h.assertTrue(HouseImpossibleHallway.isInsideWalkableVolume(origin,x,y,door-.3),"standing before the far door is inside");
        h.assertTrue(HouseImpossibleHallway.isInsideWalkableVolume(origin,x,y,door+.51),"pressed against the closed door's back panel is inside");
        h.assertTrue(!HouseImpossibleHallway.isInsideWalkableVolume(origin,x,y,door+1.2),"past the door's cell is outside");
        h.succeed();
    }

    private static AABB box(BlockPos a,BlockPos b){return new AABB(a.getX(),a.getY(),a.getZ(),b.getX(),b.getY(),b.getZ());}
    @GameTest(template="empty",batch="architecture_stair",timeoutTicks=200)
    public static void nativeGreatStaircaseUpgradeWaitsForResidentsAndPreservesEncounter(GameTestHelper h)throws Exception{
        var server=h.getLevel().getServer();var l=HouseTestLevel.get(server);var oldHouse=HouseSavedData.get(server);var oldData=LabyrinthData.get(server);
        BlockPos origin=new BlockPos(85500,80,85500);var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);
        var d=new LabyrinthData();d.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",d);
        var resident=h.makeMockServerPlayerInLevel();
        try{
            BlockPos base=FinaleArchitecture.base(origin),cache=base.offset(-10,FinaleArchitecture.ARENA,39);
            l.setBlock(cache,Blocks.BARREL.defaultBlockState(),3);var inventory=(BarrelBlockEntity)l.getBlockEntity(cache);inventory.setItem(4,new ItemStack(Items.EMERALD,7));
            l.setBlock(FinaleArchitecture.cell(origin),Blocks.AIR.defaultBlockState(),3);FinaleArchitecture.seal(l,origin,true);
            var state=new CompoundTag();state.putBoolean("Requested",true);state.putBoolean("Ready",true);state.putInt("CarveVersion",410);state.putInt("Cursor",11);state.putLong("Origin",origin.asLong());d.setState("finale_architecture_049",state);
            resident.teleportTo(l,base.getX()+.5,FinaleArchitecture.TOP,base.getZ()+12.5,0,0);
            FinaleArchitecture.tick(server);h.assertTrue(d.state("finale_architecture_049").getInt("Cursor")==11,"occupied old stairs are not altered under an explorer");
            resident.teleportTo(server.overworld(),0,90,0,0,0);
            int guard=0;while(!FinaleArchitecture.ready(server)&&guard++<2000)FinaleArchitecture.tick(server);
            h.assertTrue(FinaleArchitecture.ready(server),"the empty old shaft finishes its native structural upgrade");
            h.assertTrue(l.getBlockEntity(cache)==inventory&&inventory.getItem(0).isEmpty()&&inventory.getItem(4).getCount()==7,"the encounter cache remains the original, without replenishment");
            h.assertTrue(l.getBlockState(FinaleArchitecture.cell(origin)).isAir()&&!l.getBlockState(base.offset(0,FinaleArchitecture.ARENA,29)).isAir(),"the opened cell and closed encounter seal retain their saved physical state");
            h.assertTrue(d.door(FinaleArchitecture.ENTRY).lower.equals(FinaleArchitecture.entry(origin)),"the relocated entrance is registered at the new landing");
            for(var route:List.of(FinaleArchitecture.staircaseRoute(origin),FinaleArchitecture.continuationRoute(origin)))for(var at:route){
                var body=new AABB(at.getX()+.2,at.getY()+.01,at.getZ()+.2,at.getX()+.8,at.getY()+1.8,at.getZ()+.8);
                h.assertTrue(l.noCollision(null,body),"a native player body clears every actual upper and lower tread: "+at);
            }
            // Railings and depth variation must exist beyond the first visible stretch, on both descents.
            Set<Block> treadKinds=new HashSet<>();int checked=0;
            for(int top=FinaleArchitecture.TOP;top>FinaleArchitecture.LOOP_BOTTOM+128;top-=128){
                int rails=0;for(var at:BlockPos.betweenClosed(base.offset(-31,top-127,-31),base.offset(31,top,31)))if(l.getBlockState(at).is(Blocks.IRON_BARS))rails++;
                h.assertTrue(rails>80,"every full depth band has actual native guard rails: "+top+" / "+rails);checked++;
            }
            for(var at:FinaleArchitecture.continuationRoute(origin))treadKinds.add(l.getBlockState(at.below()).getBlock());
            h.assertTrue(checked>=19&&treadKinds.containsAll(List.of(Blocks.TUFF_BRICK_STAIRS,Blocks.DEEPSLATE_BRICK_STAIRS,Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)),"the lower continuation uses distinct authored native stair materials");
            exportStair(l,origin,"great_staircase",FinaleArchitecture.TOP-144,FinaleArchitecture.TOP+15);
            exportStair(l,origin,"great_staircase_middle",FinaleArchitecture.TOP-640,FinaleArchitecture.TOP-544);
            exportStair(l,origin,"great_staircase_lower",FinaleArchitecture.ARENA-768,FinaleArchitecture.ARENA-672);h.succeed();
        }finally{server.getPlayerList().remove(resident);server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);FinaleArchitecture.clearAll();}
    }
    private static void exportStair(net.minecraft.server.level.ServerLevel l,BlockPos origin,String name,int low,int high)throws Exception{
        BlockPos b=FinaleArchitecture.base(origin);JsonObject file=new JsonObject();file.addProperty("name",name);JsonArray palette=new JsonArray(),blocks=new JsonArray();Map<BlockState,Integer> lookup=new LinkedHashMap<>();
        for(int x=-34;x<34;x++)for(int z=-34;z<34;z++)for(int y=low;y<high;y++){
            var s=l.getBlockState(b.offset(x,y,z));if(s.isAir()||s.is(Blocks.LIGHT))continue;
            boolean visible=false;for(var side:Direction.values())if(l.getBlockState(b.offset(x,y,z).relative(side)).isAir()){visible=true;break;}if(!visible)continue;
            Integer i=lookup.get(s);if(i==null){i=lookup.size();lookup.put(s,i);palette.add(BlockState.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow());}
            JsonArray row=new JsonArray();row.add(x);row.add(y);row.add(z);row.add(i);blocks.add(row);
        }
        file.add("palette",palette);file.add("blocks",blocks);var folder=Path.of("../build/architecture-proof");Files.createDirectories(folder);Files.writeString(folder.resolve(name+".json"),new Gson().toJson(file));
    }
    @GameTest(template="empty",batch="architecture_pass",timeoutTicks=200)
    public static void nativeEverySceneHasSupportedDetailAndPreservesOriginalsOnUpgrade(GameTestHelper h)throws Exception{
        var server=h.getLevel().getServer();var interior=HouseTestLevel.get(server);var oldHouse=HouseSavedData.get(server);var oldData=LabyrinthData.get(server);var oldMother=MotherCollection.get(server);
        BlockPos origin=new BlockPos(84000,80,84000);var house=new HouseSavedData();house.markSpawned(origin);
        server.overworld().getDataStorage().set("the_oldest_house",house);
        var data=new LabyrinthData();server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);server.overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());
        try{
            LabyrinthBuilder.clearAll();LabyrinthBuilder.rebuild(server);LabyrinthBuilder.finishGameTest(server);
            for(var scene:LabyrinthPlace.values())if(SceneHuntReview.applies(scene)){var level=HouseTestLevel.get(server,NovelRooms.dimension(scene));var base=LabyrinthPlaces.base(origin,scene);var hide=scene==LabyrinthPlace.ELK_CARCASSES?level.getBlockEntity(base.offset(-2,0,-37)):null;h.assertTrue(SceneHuntReview.apply(level,origin,scene),scene.id()+" completes its actual playtest dressing");if(scene==LabyrinthPlace.ELK_CARCASSES)h.assertTrue(hide!=null&&level.getBlockEntity(base.offset(-2,0,-37))==hide,"the actual original carcass block entity survives its visual and forest upgrade");}
            VignetteAudit.write(server,origin);
            int dressed=0,props=0;
            for(var scene:LabyrinthPlace.values()){
                if(!VignetteArchitecture.applies(scene))continue;
                var level=HouseTestLevel.get(server,NovelRooms.dimension(scene));BlockPos base=LabyrinthPlaces.base(origin,scene);var bounds=scene.room();int count=0;
                for(var at:BlockPos.betweenClosed(base.offset(bounds.minX(),bounds.minY(),bounds.minZ()),base.offset(bounds.maxX(),bounds.maxY(),bounds.maxZ()))){
                    var s=level.getBlockState(at);if(s.is(HouseBlocks.SCENE_DETAIL.get())){
                        h.assertTrue(SceneDetailBlock.supported(level,at,s),scene.id()+" has no floating "+s.getValue(SceneDetailBlock.KIND));count++;
                    }
                }
                if(scene!=LabyrinthPlace.RED_ROOM){h.assertTrue(count>=2,scene.id()+" has intentional, grounded ambient detail");dressed++;props+=count;}
                for(var door:scene.doors()){
                    var at=base.offset(door.rel());h.assertTrue(level.getBlockState(at).getBlock() instanceof DoorBlock,scene.id()+" retains its actual door");
                    boolean accessible=false;
                    for(var side:List.of(door.facing(),door.facing().getOpposite())){
                        var approach=at.relative(side,2);if(!bounds.isInside(approach.subtract(base)))continue;
                        var shape=level.getBlockState(approach).getCollisionShape(level,approach);if(shape.isEmpty()||shape.max(Direction.Axis.Y)<=.125)accessible=true;
                    }
                    h.assertTrue(accessible,scene.id()+" has a usable interior door approach");
                }
                if(scene!=LabyrinthPlace.RED_ROOM)export(level,base,scene);
            }
            for(var scene:LabyrinthPlace.values())if(LiteraryRooms.isLiterary(scene)){var level=HouseTestLevel.get(server,NovelRooms.dimension(scene));var base=LabyrinthPlaces.base(origin,scene);h.assertTrue(scene==LabyrinthPlace.FAMILY_COPY||scene==LabyrinthPlace.OLD_CABIN||scene==LabyrinthPlace.ELK_CARCASSES?level.getBlockEntity(base.offset(LiteraryRooms.source(scene))) instanceof LecternBlockEntity:level.getBlockState(base.offset(LiteraryRooms.source(scene))).is(HouseBlocks.VIGNETTE_DETAIL.get())&&!SceneHuntReview.sourceBook(level,base,scene).isEmpty(),scene.id()+" has a native themed discovery surface and its preserved original");var entrance=base.offset(0,0,-3);h.assertTrue(level.noCollision(null,new AABB(entrance.getX()+.2,entrance.getY()+.01,entrance.getZ()+.2,entrance.getX()+.8,entrance.getY()+1.8,entrance.getZ()+.8)),scene.id()+" clears the actual entrance body");if(scene!=LabyrinthPlace.FAMILY_COPY&&scene!=LabyrinthPlace.OLD_CABIN)export(level,base,scene);}
            elkStages(h,HouseTestLevel.get(server,HouseDimensions.OUTSIDE),LabyrinthPlaces.base(origin,LabyrinthPlace.ELK_CARCASSES));
            export(interior,LabyrinthPlaces.base(origin,LabyrinthPlace.HOTEL),LabyrinthPlace.HOTEL,"hotel_upstairs");
            export(interior,LabyrinthPlaces.base(origin,LabyrinthPlace.HOTEL),LabyrinthPlace.HOTEL,"hotel_basement");
            export(interior,LabyrinthPlaces.base(origin,LabyrinthPlace.BLIND_STRETCH),LabyrinthPlace.BLIND_STRETCH,"blind_stretch");
            // The composed buildings from outside, roofs and all, without the woods' crowns in front of them.
            for(var scene:List.of(LabyrinthPlace.CAMP_BLOOD,LabyrinthPlace.MAPPING_INTERIOR,LabyrinthPlace.END_WORLD_CABIN,LabyrinthPlace.ELK_LOT,LabyrinthPlace.BARN_WELL))
                export(HouseTestLevel.get(server,NovelRooms.dimension(scene)),LabyrinthPlaces.base(origin,scene),scene,scene.id()+"_exterior");
            literaryRoutes(h,interior,LabyrinthPlaces.base(origin,LabyrinthPlace.WINCHESTER));
            var newCamp=LabyrinthPlaces.base(origin,LabyrinthPlace.CAMP_BLOOD);var woods=HouseTestLevel.get(server,HouseDimensions.OUTSIDE);
            for(int x:new int[]{-20,20})for(int z:new int[]{-26,-57,-83})for(var at:BlockPos.betweenClosed(newCamp.offset(x-7,0,z-6),newCamp.offset(x+7,10,z+6)))h.assertTrue(!woods.getBlockState(at).is(Blocks.SPRUCE_LEAVES)&&!woods.getBlockState(at).is(Blocks.SPRUCE_LOG),"the camp's forest cannot grow through a furnished cabin or roof");
            h.assertTrue(dressed==23&&props>=94,"all twenty-three authored scenes/camps receive supported detail; the copied Red Room stays personal");
            shellsAndEdges(h,server,origin,data);
            composition(h,server,origin,data);
            frontsAndInteriors(h,server,origin,data);
            var camp=LabyrinthPlaces.base(origin,LabyrinthPlace.EXPLORER_CAMP);var cache=(BarrelBlockEntity)interior.getBlockEntity(camp.offset(LabyrinthCampsite.CACHE));
            cache.clearContent();cache.setItem(7,new ItemStack(Items.DIAMOND,3));
            var mother=LabyrinthPlaces.base(origin,LabyrinthPlace.MOTHER_DEN);var beings=interior.getEntitiesOfClass(Entity.class,box(mother.offset(-11,-5,-26),mother.offset(11,15,1)));
            var ids=beings.stream().map(Entity::getUUID).collect(java.util.stream.Collectors.toSet());
            UUID observer=UUID.randomUUID();WitnessAccount.resolve(data,observer,WitnessAccount.Story.HARRIGAN,"remembered");
            var round=new CompoundTag();round.putInt("ArchitectureRound",137);data.setState(HideAndClap.ID,round);
            // A saved prior layout must go through the builder's real in-place upgrade, never its native builders.
            BlockPos oldDoor=LabyrinthPlaces.base(origin,LabyrinthPlace.HARRIGAN).offset(0,0,1);
            interior.setBlock(oldDoor,Blocks.DARK_OAK_PLANKS.defaultBlockState(),2);interior.setBlock(oldDoor.above(),Blocks.DARK_OAK_PLANKS.defaultBlockState(),2);
            data.setState(VignetteArchitecture.STATE,new CompoundTag());data.setBuilt(23,origin);
            h.assertTrue(!LabyrinthBuilder.ensureBuilt(server),"the old layout schedules an in-place architectural upgrade");LabyrinthBuilder.finishGameTest(server);
            h.assertTrue(cache==interior.getBlockEntity(camp.offset(LabyrinthCampsite.CACHE))&&cache.getItem(0).isEmpty()&&cache.getItem(7).getCount()==3,"a emptied cache stays the same original inventory");
            h.assertTrue(interior.getBlockState(oldDoor).getBlock() instanceof DoorBlock,"the authored wall in an older Harrigan return doorway is repaired in place");
            h.assertTrue(ids.equals(interior.getEntitiesOfClass(Entity.class,box(mother.offset(-11,-5,-26),mother.offset(11,15,1))).stream().map(Entity::getUUID).collect(java.util.stream.Collectors.toSet())),"native den residents retain their original UUIDs");
            h.assertTrue(WitnessAccount.has(data,observer,WitnessAccount.Story.HARRIGAN)&&data.state(HideAndClap.ID).getInt("ArchitectureRound")==137,"personal evidence and active story state survive the architectural upgrade");
            BlockPos removed=camp.offset(4,1,-9);h.assertTrue(interior.getBlockState(removed).is(HouseBlocks.SCENE_DETAIL.get()),"the camp satchel stands on the original cache");
            interior.setBlock(removed,Blocks.AIR.defaultBlockState(),3);VignetteArchitecture.decorateOnce(interior,origin,LabyrinthPlace.EXPLORER_CAMP);
            h.assertTrue(interior.getBlockState(removed).isAir(),"a completed decoration checkpoint does not respawn removed props");
            Files.writeString(Path.of("../build/architecture-proof/manifest.txt"),"Native generated scenes: "+dressed+"\nSupported ambient details: "+props+"\nOld inventories, actors, evidence and story state preserved.\n");
            h.succeed();
        }finally{
            for(var scene:LabyrinthPlace.values())if(VignetteArchitecture.applies(scene)||LiteraryRooms.isLiterary(scene)){
                var level=HouseTestLevel.get(server,NovelRooms.dimension(scene));var base=LabyrinthPlaces.base(origin,scene);var r=scene.room();
                for(var e:level.getEntitiesOfClass(Entity.class,box(base.offset(r.minX()-1,r.minY()-1,r.minZ()-1),base.offset(r.maxX()+1,r.maxY()+2,r.maxZ()+2))))e.discard();
            }
            server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);server.overworld().getDataStorage().set("the_oldest_house_mother",oldMother);LabyrinthBuilder.clearAll();LabyrinthDoors.clearAll();
        }
    }
    public static void literaryRoutes(GameTestHelper h,ServerLevel level,BlockPos b){
        // The slow stair rises eight blocks through sixteen long flights and fifteen connected turns.
        for(int flight=0;flight<16;flight++){int layer=flight/2,z=-8-flight*4;double feet=layer+(flight%2==0?.5:1);
            for(int x=-23;x<=23;x++)supportedBody(h,level,b,x,feet,z,"Winchester flight "+flight);
            if(flight<15){int turn=flight%2==0?23:-23;for(int dz=1;dz<4;dz++)supportedBody(h,level,b,turn,feet,z-dz,"Winchester turn "+flight);}
        }
        for(int z=-69;z>=-72;z--)supportedBody(h,level,b,-23,8,z,"Winchester upper landing");
        for(int x=-23;x<=25;x++)supportedBody(h,level,b,x,8,-72,"Winchester ledger approach");
        h.assertTrue(level.getBlockState(b.offset(26,9,-70)).is(LiteraryRegistry.PROP.get())&&level.getBlockState(b.offset(25,-1,-60)).is(Blocks.HAY_BLOCK),"the real upper ledger and survivable drop remain in the authored wing");
    }
    private static void supportedBody(GameTestHelper h,ServerLevel level,BlockPos b,int x,double feet,int z,String route){double xx=b.getX()+x,yy=b.getY()+feet,zz=b.getZ()+z;
        h.assertTrue(level.noCollision(null,new AABB(xx+.2,yy+.01,zz+.2,xx+.8,yy+1.8,zz+.8)),route+" clears a native player body at "+x+","+feet+","+z);
        h.assertTrue(!level.noCollision(null,new AABB(xx+.2,yy-.04,zz+.2,xx+.8,yy-.01,zz+.8)),route+" has an actual supporting tread at "+x+","+feet+","+z);
    }
    private static void frontsAndInteriors(GameTestHelper h,net.minecraft.server.MinecraftServer server,BlockPos origin,LabyrinthData data){
        for(var scene:List.of(LabyrinthPlace.GOATMAN,LabyrinthPlace.HOLLOWAY_CAMP,LabyrinthPlace.ZAMPANO_COURTYARD))h.assertTrue(data.state(SceneExteriors.STATE).getBoolean(origin.asLong()+":"+scene.id()),scene.id()+" has a finite exterior checkpoint");
        var town=HouseTestLevel.get(server,HouseDimensions.OUTSIDE);var b=LabyrinthPlaces.base(origin,LabyrinthPlace.DROWNED_TOWN);
        int lamps=0;
        for(var e:ProofrockTown.streetlights(b).entrySet()){
            var state=town.getBlockState(e.getKey());h.assertTrue(state.equals(e.getValue()),"a fresh native streetlight stands at its authored address: "+e.getKey().subtract(b)+" actual="+state+" expected="+e.getValue());
            if(state.getValue(TownStreetlightBlock.KIND)==TownStreetlightBlock.Kind.HEAD){lamps++;boolean wet=state.getValue(TownStreetlightBlock.WATERLOGGED);h.assertTrue(state.getLightEmission(town,e.getKey())==(wet?9:15)&&(!wet||state.getFluidState().isSource()),"each fitted native lamp retains its original light level and underwater source");}
        }
        h.assertTrue(lamps==25,"all twenty street lamps, the pier lamp and four submerged lamps have actual textured heads");
        // Proofrock (0.4.67): Main Street and the school's ring corridor are clear all the way round, the courtyard is living grass,
        // and the gym's folded bleachers leave a gap she fits under and a reader does not.
        for(var at:List.of(b.offset(0,0,-40),b.offset(-22,0,-65),b.offset(-39,0,-66),b.offset(-30,0,-56),b.offset(-30,0,-79)))h.assertTrue(town.noCollision(null,new AABB(at.getX()+.2,at.getY()+.01,at.getZ()+.2,at.getX()+.8,at.getY()+1.8,at.getZ()+.8)),"Proofrock retains a native player passage at "+at);
        h.assertTrue(town.getBlockState(b.offset(-30,-1,-63)).is(Blocks.GRASS_BLOCK)&&town.getBlockState(b.offset(-6,0,-61)).getBlock() instanceof DoorBlock,"the school courtyard is grass and the front doors are real doors");
        var gap=b.offset(-45,0,-46);
        h.assertTrue(town.getBlockState(gap.above()).is(DrownedTownRegistry.FIXTURE.get())&&!town.noCollision(null,new AABB(gap.getX()+.2,gap.getY()+.01,gap.getZ()+.2,gap.getX()+.8,gap.getY()+1.8,gap.getZ()+.8))
            &&town.noCollision(null,new AABB(gap.getX()+.11,gap.getY()+.01,gap.getZ()+.03,gap.getX()+.89,gap.getY()+.95,gap.getZ()+.97)),"the bleachers leave a gap only the witch fits under");
        var interior=HouseTestLevel.get(server);var trailer=LabyrinthPlaces.base(origin,LabyrinthPlace.GOATMAN);var hut=LabyrinthPlaces.base(origin,LabyrinthPlace.HOLLOWAY_CAMP);var archive=LabyrinthPlaces.base(origin,LabyrinthPlace.ZAMPANO_COURTYARD);
        h.assertTrue(interior.getBlockState(trailer.offset(0,7,-65)).is(Blocks.SMOOTH_STONE_SLAB)&&interior.getBlockState(trailer.offset(5,0,-51)).is(Blocks.IRON_BARS),"the trailer has a stepped metal roof and a physical tow frame");
        h.assertTrue(interior.getBlockState(hut.offset(3,3,0)).is(Blocks.STRIPPED_SPRUCE_LOG)&&!interior.getBlockState(hut.offset(0,8,-5)).isAir(),"the dugout has a braced entry and an earth-covered roof");
        h.assertTrue(town.getBlockState(archive.offset(3,3,-18)).is(Blocks.STONE_BRICKS)&&town.getBlockState(archive.offset(0,9,-25)).is(Blocks.LIGHT_GRAY_STAINED_GLASS),"the archive has projecting pilasters and glazed rooflights");
        var removed=archive.offset(0,9,-25);town.setBlock(removed,Blocks.AIR.defaultBlockState(),2);SceneExteriors.decorateOnce(town,origin,LabyrinthPlace.ZAMPANO_COURTYARD);h.assertTrue(town.getBlockState(removed).isAir(),"completed exterior upgrades do not replenish removed scenery");
    }
    /** 0.4.46: literary rooms and outdoor scenes are composed once, in place, and the generic pass's defects are gone. */
    private static void composition(GameTestHelper h,net.minecraft.server.MinecraftServer server,BlockPos origin,LabyrinthData data){
        var craft=data.state(SceneCraft.STATE);
        for(var scene:LabyrinthPlace.values())if(SceneCraft.applies(scene))h.assertTrue(craft.getBoolean(origin.asLong()+":"+scene.id()),scene.id()+" records its one-time composition");
        for(var scene:LabyrinthPlace.values()){
            if(!LiteraryRooms.isLiterary(scene)||NovelRooms.outside(scene))continue;
            var level=HouseTestLevel.get(server,NovelRooms.dimension(scene));var b=LabyrinthPlaces.base(origin,scene);var r=scene.room();int rugs=0,squares=0;
            for(var at:BlockPos.betweenClosed(b.offset(r.minX(),r.minY(),r.minZ()),b.offset(r.maxX(),r.maxY(),r.maxZ()))){
                var s=level.getBlockState(at);
                if(s.is(HouseBlocks.RUG_FLOOR.get()))rugs++;
                if(scene!=LabyrinthPlace.MASQUE&&s.is(net.minecraft.tags.BlockTags.WOOL_CARPETS)&&!level.getBlockState(at.below()).is(Blocks.SCULK_SENSOR)){
                    boolean alone=true;for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if((dx!=0||dz!=0)&&level.getBlockState(at.offset(dx,0,dz)).is(s.getBlock()))alone=false;
                    if(alone)squares++;
                }
            }
            h.assertTrue(squares==0,scene.id()+" keeps no lattice of lone carpet squares: "+squares);
            if(scene!=LabyrinthPlace.WINCHESTER&&scene!=LabyrinthPlace.MASQUE)h.assertTrue(rugs>=9,scene.id()+" has a laid rug: "+rugs);
            // The discovery paper and the ending can both be reached from the entrance.
            var reached=reachable(level,b,scene);
            for(var paper:List.of(LiteraryRooms.source(scene),LiteraryRooms.ending(scene))){
                boolean near=false;for(var d:Direction.values())near|=reached.contains(b.offset(paper).relative(d));
                h.assertTrue(near,scene.id()+" can be reached at "+paper);
            }
        }
        for(var scene:List.of(LabyrinthPlace.MINIATURES,LabyrinthPlace.USHER,LabyrinthPlace.CRIMSON_HALL,LabyrinthPlace.BLY_ROUTE,LabyrinthPlace.CONFESSION)){
            var level=HouseTestLevel.get(server,NovelRooms.dimension(scene));var b=LabyrinthPlaces.base(origin,scene);var r=scene.room();int hearths=0;
            for(var at:BlockPos.betweenClosed(b.offset(r.minX(),r.minY(),r.minZ()),b.offset(r.maxX(),r.maxY(),r.maxZ())))if(level.getBlockState(at).is(Blocks.CAMPFIRE))hearths++;
            h.assertTrue(hearths==1,scene.id()+" has one hearth: "+hearths);
        }
        var hill=LabyrinthPlaces.base(origin,LabyrinthPlace.HILL_NURSERY);var interior=HouseTestLevel.get(server);
        h.assertTrue(interior.getBlockState(hill.offset(LiteraryRooms.ending(LabyrinthPlace.HILL_NURSERY))).is(LiteraryRegistry.PROP.get())&&interior.getBlockState(hill.offset(8,1,-36)).isAir(),"the nursery's ending ledger stands in the cellar, not on its roof");
        // Outdoors: mixed woods instead of planted rows, nothing growing out of the lakes, banks without battlements.
        var outside=HouseTestLevel.get(server,HouseDimensions.OUTSIDE);
        var camp=LabyrinthPlaces.base(origin,LabyrinthPlace.CAMP_BLOOD);var cr=LabyrinthPlace.CAMP_BLOOD.room();var kinds=new HashMap<Block,Integer>();
        for(var at:BlockPos.betweenClosed(camp.offset(cr.minX(),0,cr.minZ()),camp.offset(cr.maxX(),0,cr.maxZ()))){var s=outside.getBlockState(at);if(s.is(net.minecraft.tags.BlockTags.LOGS)&&!outside.getBlockState(at.below()).is(net.minecraft.tags.BlockTags.LOGS))kinds.merge(s.getBlock(),1,Integer::sum);}
        h.assertTrue(kinds.getOrDefault(Blocks.OAK_LOG,0)>=3&&kinds.getOrDefault(Blocks.BIRCH_LOG,0)>=3&&kinds.getOrDefault(Blocks.SPRUCE_LOG,0)>=3,"the camp stands in a mixed wood: "+kinds);
        // The barn is open to a gambrel's rafters, with no flat birch lid left over it.
        var barnBase=LabyrinthPlaces.base(origin,LabyrinthPlace.BARN_WELL);int lid=0;
        for(int x=5;x<=15;x++)for(int z=-34;z<=-17;z++)if(outside.getBlockState(barnBase.offset(x,7,z)).is(Blocks.BIRCH_PLANKS))lid++;
        h.assertTrue(lid==0&&outside.getBlockState(barnBase.offset(10,8,-25)).is(Blocks.DARK_OAK_LOG),"the barn has a gambrel ridge and no flat lid: "+lid+" lid blocks, ridge "+outside.getBlockState(barnBase.offset(10,8,-25)));
        for(var scene:List.of(LabyrinthPlace.COSTUME_NIGHT,LabyrinthPlace.MOVIE_NIGHT,LabyrinthPlace.WINTER_LAKE,LabyrinthPlace.END_WORLD_CABIN)){
            var b=LabyrinthPlaces.base(origin,scene);var r=scene.room();int afloat=0;
            for(var at:BlockPos.betweenClosed(b.offset(r.minX(),0,r.minZ()),b.offset(r.maxX(),0,r.maxZ()))){var below=outside.getBlockState(at.below());if(outside.getBlockState(at).is(net.minecraft.tags.BlockTags.LOGS)&&(below.is(Blocks.WATER)||below.is(Blocks.ICE)))afloat++;}
            h.assertTrue(afloat==0,scene.id()+" grows no tree out of its lake: "+afloat);
        }
        var shallows=LabyrinthPlaces.base(origin,LabyrinthPlace.SHALLOWS);var sr=LabyrinthPlace.SHALLOWS.room();int steps=0,runs=0,previous=Integer.MIN_VALUE;
        for(int z=sr.minZ()+4;z<=-6;z++){
            int top=Integer.MIN_VALUE;for(int y=8;y>=-3;y--)if(outside.getBlockState(shallows.offset(sr.minX(),y,z)).is(Blocks.PODZOL)){top=y;break;}
            if(top==Integer.MIN_VALUE){previous=Integer.MIN_VALUE;continue;}
            if(previous!=Integer.MIN_VALUE){runs++;if(top!=previous)steps++;}
            previous=top;
        }
        h.assertTrue(runs>=8&&steps*2<runs,"the shallows bank rises smoothly rather than in a checkerboard: "+steps+"/"+runs);
        // A completed composition is never restaged.
        var miniatures=LabyrinthPlaces.base(origin,LabyrinthPlace.MINIATURES);var mr=LabyrinthPlace.MINIATURES.room();BlockPos grate=null;
        for(var at:BlockPos.betweenClosed(miniatures.offset(mr.minX(),mr.minY(),mr.minZ()),miniatures.offset(mr.maxX(),mr.maxY(),mr.maxZ())))if(interior.getBlockState(at).is(Blocks.CAMPFIRE))grate=at.immutable();
        h.assertTrue(grate!=null,"the workshop has its hearth");
        interior.setBlock(grate,Blocks.AIR.defaultBlockState(),2);SceneCraft.craftOnce(interior,origin,LabyrinthPlace.MINIATURES);
        h.assertTrue(interior.getBlockState(grate).isAir(),"a completed composition does not rebuild what was taken away");
    }

    /** Open space connected to a scene's doorways, through its doors. */
    private static Set<BlockPos> reachable(net.minecraft.server.level.ServerLevel level,BlockPos b,LabyrinthPlace scene){
        var r=scene.room();var seen=new HashSet<BlockPos>();var todo=new ArrayDeque<BlockPos>();
        for(var door:scene.doors())for(int up=0;up<=1;up++){var start=b.offset(door.rel()).relative(door.facing().getOpposite()).above(up);if(r.isInside(start.subtract(b))&&seen.add(start))todo.add(start);}
        while(!todo.isEmpty()){
            var at=todo.poll();
            for(var d:Direction.values()){
                var next=at.relative(d);if(!r.isInside(next.subtract(b))||seen.contains(next))continue;
                var s=level.getBlockState(next);
                if(!s.getCollisionShape(level,next).isEmpty()&&!(s.getBlock() instanceof DoorBlock)&&!(s.getBlock() instanceof TrapDoorBlock))continue;
                seen.add(next);todo.add(next);
            }
        }
        return seen;
    }

    /** 0.4.28: indoor shells read as built rooms, and the outdoor scenes end in land, not invisible walls. */
    private static void shellsAndEdges(GameTestHelper h,net.minecraft.server.MinecraftServer server,BlockPos origin,LabyrinthData data){
        var shells=data.state(SceneShells.STATE);
        for(var scene:LabyrinthPlace.values())if(SceneShells.shapes(scene))
            h.assertTrue(shells.getBoolean(origin.asLong()+":"+scene.id()),scene.id()+" records its one-time shell pass");
        // Fireplaces: a grate set back into the wall, inside a brick or stone surround.
        for(var scene:List.of(LabyrinthPlace.FLOORBOARDS,LabyrinthPlace.HARRIGAN)){
            var level=HouseTestLevel.get(server,NovelRooms.dimension(scene));var b=LabyrinthPlaces.base(origin,scene);var r=scene.room();int grates=0;
            for(var at:BlockPos.betweenClosed(b.offset(r.minX()-3,r.minY(),r.minZ()-3),b.offset(r.maxX()+3,r.maxY(),r.maxZ())))if(level.getBlockState(at).is(Blocks.CAMPFIRE))grates++;
            h.assertTrue(grates==1,scene.id()+" has exactly one fireplace: "+grates);
        }
        // Windows: glass set back behind the wall plane, so each opening has a real reveal.
        for(var scene:List.of(LabyrinthPlace.HIDE_AND_CLAP,LabyrinthPlace.KAREN_ROOM,LabyrinthPlace.HOSPITAL,LabyrinthPlace.MODEL_HOME,LabyrinthPlace.WHALE)){
            var level=HouseTestLevel.get(server,NovelRooms.dimension(scene));var b=LabyrinthPlaces.base(origin,scene);var r=scene==LabyrinthPlace.WHALE?WhaleInstitute.INTERIOR:SceneShells.interior(scene);int panes=0;
            for(var at:BlockPos.betweenClosed(b.offset(r.minX()-3,r.minY(),r.minZ()-3),b.offset(r.maxX()+3,r.maxY(),r.maxZ()))){
                var rel=at.subtract(b);boolean outside=rel.getX()<r.minX()-1||rel.getX()>r.maxX()+1||rel.getZ()<r.minZ()-1;
                var st=level.getBlockState(at);if(outside&&(st.is(Blocks.GLASS_PANE)||st.getBlock() instanceof StainedGlassPaneBlock||scene==LabyrinthPlace.WHALE&&st.is(NovelRegistry.WARD_GLASS.get())))panes++;
            }
            h.assertTrue(panes>=2,scene.id()+" has recessed windows: "+panes);
        }
        // Framing and beams in the study.
        var study=HouseTestLevel.get(server);var hb=LabyrinthPlaces.base(origin,LabyrinthPlace.HARRIGAN);int posts=0,beams=0,plaster=0;
        for(var at:BlockPos.betweenClosed(hb.offset(-8,0,-25),hb.offset(8,6,1))){
            var st=study.getBlockState(at);
            if(st.is(Blocks.STRIPPED_DARK_OAK_LOG))posts++;if(st.is(Blocks.DARK_OAK_LOG))beams++;if(st.is(Blocks.GREEN_TERRACOTTA))plaster++;
        }
        h.assertTrue(posts>=10&&beams>=10&&plaster>=20,"the study is framed, beamed and plastered: "+posts+"/"+beams+"/"+plaster);
        // Outdoor edges: no invisible walls remain around the plain or the courtyard.
        var outside=HouseTestLevel.get(server,HouseDimensions.OUTSIDE);
        for(var scene:List.of(LabyrinthPlace.PLAIN,LabyrinthPlace.ZAMPANO_COURTYARD)){
            var b=LabyrinthPlaces.base(origin,scene);int barriers=0;
            for(var at:BlockPos.betweenClosed(b.offset(-31,0,-66),b.offset(31,17,1)))if(outside.getBlockState(at).is(Blocks.BARRIER))barriers++;
            h.assertTrue(barriers==0,scene.id()+" keeps no invisible wall: "+barriers);
        }
        // The plain's dunes rise in steps of two or more from its walkable core: they can be seen, not climbed.
        // The core's scattered slabs are half steps over its own ground, not a rise.
        var plain=LabyrinthPlaces.base(origin,LabyrinthPlace.PLAIN);
        for(int z=-62;z<=-2;z+=4)for(int side:new int[]{-1,1}){
            int previous=-1;
            for(int x=Landscapes.PLAIN_HALF_WIDTH;x<=Landscapes.PLAIN_HALF_WIDTH+14;x++){
                int top=-9;for(int y=14;y>=-3;y--){var st=outside.getBlockState(plain.offset(side*x,y,z));if(!st.isAir()&&!st.is(Blocks.DEAD_BUSH)&&!(st.getBlock() instanceof SlabBlock)){top=y;break;}}
                h.assertTrue(top-previous!=1,"no one-block step climbs the dune at x="+side*x+" z="+z+" ("+previous+" -> "+top+")");
                previous=top;
            }
        }
        // The barn's bank carries a wood, not a flat curtain of leaves. A curtain is one flat top over every column; a wood has open sky between crowns of different heights.
        var barn=LabyrinthPlaces.base(origin,LabyrinthPlace.BARN_WELL);int cells=0,open=0;var tops=new java.util.HashMap<Integer,Integer>();
        for(int x=-23;x<=23;x++)for(int z=-45;z<=4;z++)if(Landscapes.barnEdge(x,z)>=4){
            cells++;int top=Integer.MIN_VALUE;
            for(int y=24;y>=-3;y--)if(outside.getBlockState(barn.offset(x,y,z)).getBlock() instanceof LeavesBlock){top=y;break;}
            if(top==Integer.MIN_VALUE)open++;else tops.merge(top,1,Integer::sum);
        }
        int crowned=cells-open,flattest=tops.values().stream().max(Integer::compare).orElse(0);
        h.assertTrue(open*7>=cells&&flattest*3<crowned&&tops.size()>=5,"the bank's skyline is broken by separate crowns: open "+open+"/"+cells+", commonest top "+flattest+"/"+crowned+", "+tops.size()+" heights");
        // Anyone the plain has seen inside it is returned from beyond its edge.
        var walker=h.makeMockServerPlayerInLevel();
        try{
            walker.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            // The guard reads coordinates only, so the walker stays in its own level and no arrival handler moves it.
            walker.teleportTo(plain.getX()+.5,plain.getY(),plain.getZ()-20.5);OutdoorBounds.remember(walker);
            var inside=walker.position();walker.teleportTo(plain.getX()+40.5,plain.getY()+12,plain.getZ()-20.5);
            boolean returned=OutdoorBounds.check(walker,origin);
            h.assertTrue(returned&&walker.position().distanceTo(inside)<1.0E-3,"crossing the dunes returns the explorer to where they stood: "+returned+" at "+walker.position().subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(plain))+", stood at "+inside.subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(plain)));
            h.assertTrue(!OutdoorBounds.check(walker,origin),"inside the plain nothing moves them");
        }finally{server.getPlayerList().remove(walker);}
    }

    /** 0.4.50: the elk scene is two stages: an enclosed yacht afloat, then woods, a stream and an actual cave under a knoll. */
    private static void elkStages(GameTestHelper h,net.minecraft.server.level.ServerLevel l,BlockPos b)throws Exception{
        h.assertTrue(l.getBlockState(b.offset(9,1,-10)).is(LiteraryRegistry.YACHT_PORTHOLE.get())&&l.getBlockState(b.offset(0,-1,-3)).is(LiteraryRegistry.YACHT_CARPET.get()),"the reader arrives in a yacht cabin with portholes");
        h.assertTrue(l.getBlockState(b.offset(0,-1,-50)).is(Blocks.WATER)&&l.getBlockState(b.offset(14,-1,-10)).is(Blocks.WATER),"the yacht stands in the lake, not on land");
        for(int x=-9;x<=9;x++)for(int y=-1;y<=3;y++)h.assertTrue(!l.getBlockState(b.offset(x,y,0)).is(Blocks.WATER),"no lake water inside the hull at "+x+","+y);
        var door=b.offset(ElkCarcassMap.SERVICE_DOOR);h.assertTrue(l.getBlockState(door).getBlock() instanceof DoorBlock,"the crew's gate is a real return door");
        h.assertTrue(l.getBlockState(b.offset(LiteraryRooms.ending(LabyrinthPlace.ELK_CARCASSES))).is(LiteraryRegistry.PROP.get()),"the ending ledger waits at the gate");
        for(int x=ElkCarcassMap.HOLLOW_X0;x<=ElkCarcassMap.HOLLOW_X1;x++)for(int z=ElkCarcassMap.HOLLOW_Z0;z<=ElkCarcassMap.HOLLOW_Z1;z++){
            var cell=b.offset(x,ElkCarcassMap.CAVE_Y,z);var roof=cell.above();
            h.assertTrue(l.getBlockState(cell).isAir()&&l.getBlockState(roof).is(LiteraryRegistry.PROP.get())&&l.getBlockState(roof).getValue(LiteraryPropBlock.KIND)==LiteraryPropBlock.Kind.CARCASS,"the hollow is one high under the carcasses at "+x+","+z);
        }
        int crew=0,carcasses=0;for(var at:BlockPos.betweenClosed(b.offset(-30,ElkCarcassMap.CAVE_Y,-196),b.offset(-12,ElkCarcassMap.CAVE_Y+4,-182))){var s=l.getBlockState(at);if(!s.is(LiteraryRegistry.PROP.get()))continue;if(s.getValue(LiteraryPropBlock.KIND)==LiteraryPropBlock.Kind.CREW_BODY)crew++;if(s.getValue(LiteraryPropBlock.KIND)==LiteraryPropBlock.Kind.CARCASS)carcasses++;}
        h.assertTrue(crew>=3&&carcasses>=30,"the cave holds the crew among the carcasses: "+crew+" / "+carcasses);
        var chamber=b.offset(ElkCarcassMap.CHAMBER);int roofed=0;for(int dx=-6;dx<=6;dx+=3)for(int dz=-3;dz<=3;dz+=3){var top=chamber.offset(dx,0,dz);while((l.getBlockState(top).isAir()||l.getBlockState(top).is(LiteraryRegistry.PROP.get())||l.getBlockState(top).is(Blocks.HANGING_ROOTS)||l.getBlockState(top).is(Blocks.POINTED_DRIPSTONE))&&top.getY()<b.getY()+20)top=top.above();if(l.getBlockState(top).isCollisionShapeFullBlock(l,top)&&top.getY()<b.getY()+18)roofed++;}
        h.assertTrue(roofed>=13,"the chamber is an actual cave under rock: "+roofed);
        h.assertTrue(Math.abs(ElkCarcassMap.MOUTH.getX())>=8&&ElkCarcassMap.MOUTH.getZ()<-150&&!ElkCarcassMap.wooded(ElkCarcassMap.MOUTH.getX()-8,ElkCarcassMap.MOUTH.getZ()-8),"the cave is off-centre on the outer edge of the western wood");
        int water=0;for(int z=-240;z<=-80;z+=8){double t=ElkCarcassMap.along(0,z);for(int x=-40;x<=60;x++)if(ElkCarcassMap.stream(x,z)&&l.getBlockState(b.offset(x,ElkCarcassMap.streamLevel(ElkCarcassMap.along(x,z)),z)).is(Blocks.WATER)){water++;break;}}
        h.assertTrue(water>=18,"a stream runs the length of the valley: "+water);
        export(l,b,LabyrinthPlace.ELK_CARCASSES,"elk_yacht");
        export(l,b,LabyrinthPlace.ELK_CARCASSES,"elk_cave");
    }
    public static void export(net.minecraft.server.level.ServerLevel l,BlockPos b,LabyrinthPlace scene)throws Exception{
        export(l,b,scene,scene.id());
    }
    private static void export(net.minecraft.server.level.ServerLevel l,BlockPos b,LabyrinthPlace scene,String name)throws Exception{
        var r=scene.room();JsonObject file=new JsonObject();file.addProperty("name",name);
        JsonArray palette=new JsonArray(),blocks=new JsonArray();Map<BlockState,Integer> lookup=new LinkedHashMap<>();
        int minX=r.minX(),maxX=r.maxX(),minZ=r.minZ(),maxZ=r.maxZ(),minY=Math.max(-1,r.minY()),maxY=Math.min(4,r.maxY());
        if(scene==LabyrinthPlace.GOATMAN){minZ=-79;maxZ=-45;}
        if(scene==LabyrinthPlace.HOLLOWAY_CAMP)minZ=-24;
        if(scene==LabyrinthPlace.TED_CAVER){minZ=-8;minY=-1;}
        if(scene==LabyrinthPlace.MOTHER_DEN)maxY=12;
        if(scene==LabyrinthPlace.PRESERVED_CAVE)maxY=4;
        if(scene==LabyrinthPlace.WHALE)maxY=9;
        if(scene==LabyrinthPlace.PLAIN)minZ=-105;
        if(LakeLandscape.isLake(scene))maxY=16;
        if(scene==LabyrinthPlace.HOTEL)maxY=3;
        if(name.equals("hotel_upstairs")){minY=4;maxY=8;}
        if(name.equals("hotel_basement")){minY=-5;maxY=-2;}
        if(HallVariations.added(scene))maxY=HallVariations.domestic(scene)?4:scene==LabyrinthPlace.STONE_BEND?5:6;
        if(scene==LabyrinthPlace.BLIND_STRETCH)maxY=2;if(LiteraryRooms.isLiterary(scene)){minY=Math.max(scene.room().minY(),-6);maxY=NovelRooms.outside(scene)?Math.min(7,scene.room().maxY()):Math.min(4,scene.room().maxY());if(scene==LabyrinthPlace.WINCHESTER)maxY=10;}
        boolean exterior=name.endsWith("_exterior");if(exterior){minY=-1;maxY=Math.min(14,r.maxY());}
        // The yacht in section along its keel, decks and hull; the cave sliced through its chamber.
        if(name.equals("elk_yacht")){minX=-12;maxX=0;minZ=-46;maxZ=19;minY=-9;maxY=13;}
        if(name.equals("elk_cave")){minX=-36;maxX=-2;minZ=-202;maxZ=-168;minY=2;maxY=8;}
        for(int x=minX;x<=maxX;x++)for(int y=minY;y<=maxY;y++)for(int z=minZ;z<=maxZ;z++){
            var at=b.offset(x,y,z);var s=l.getBlockState(at);if(s.is(LiteraryRegistry.FROZEN.get()))s=LiteraryFrozenBlock.original(l,at);
            if(s.isAir()||s.is(Blocks.BARRIER)||s.is(Blocks.LIGHT))continue;
            if(s.is(Blocks.WATER)&&(!LakeLandscape.isLake(scene)&&!LiteraryRooms.outside(scene)||!l.getBlockState(at.above()).isAir()))continue;
            if(exterior&&s.is(net.minecraft.tags.BlockTags.LEAVES))continue;
            // A documented cutaway removes roofs and the near walls, not interior contents.
            if(!exterior&&y>=0&&(x==maxX||z==maxZ||(scene==LabyrinthPlace.WALLPAPER_NURSERY&&x==9)||(scene==LabyrinthPlace.SEANCE&&z==-7&&Math.abs(x)>3)||(scene==LabyrinthPlace.WALLPAPER_NURSERY&&z==-6&&Math.abs(x)>3)))continue;
            if(y>=0&&((scene==LabyrinthPlace.WHALE&&x==13)||(scene==LabyrinthPlace.HOSPITAL&&x==9)
                    ||(scene==LabyrinthPlace.KAREN_ROOM&&x==9)||(scene==LabyrinthPlace.ZAMPANO_COURTYARD&&x==12&&z<=-19)))continue;
            if(name.equals("hotel_upstairs")&&y>=5&&(x==10||z==-6))continue;
            if(scene==LabyrinthPlace.HOTEL&&name.equals("hotel")&&y>=0&&(x==12||z==-4))continue;
            if(!exterior&&y>2&&s.is(Blocks.BIRCH_PLANKS))continue;
            // The institute's ward ceiling comes off; over the dayroom it is the attics' floor and stays.
            if(!exterior&&scene==LabyrinthPlace.WHALE&&y==5&&z>-25&&s.is(Blocks.SMOOTH_STONE))continue;
            boolean exposed=false;for(var side:Direction.values())if(l.getBlockState(at.relative(side)).isAir()||!l.getBlockState(at.relative(side)).isSolid()){exposed=true;break;}
            if(!exposed&&y!=minY)continue;
            Integer index=lookup.get(s);if(index==null){index=lookup.size();lookup.put(s,index);palette.add(BlockState.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow());}
            JsonArray row=new JsonArray();row.add(x);row.add(y);row.add(z);row.add(index);blocks.add(row);
        }
        file.add("palette",palette);file.add("blocks",blocks);Path folder=Path.of("../build/architecture-proof");Files.createDirectories(folder);
        Files.writeString(folder.resolve(name+".json"),new Gson().toJson(file));
    }
}
