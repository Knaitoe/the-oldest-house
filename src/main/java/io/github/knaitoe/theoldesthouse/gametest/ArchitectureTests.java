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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ArchitectureTests {
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
            exportStair(l,origin);h.succeed();
        }finally{server.getPlayerList().remove(resident);server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);FinaleArchitecture.clearAll();}
    }
    private static void exportStair(net.minecraft.server.level.ServerLevel l,BlockPos origin)throws Exception{
        BlockPos b=FinaleArchitecture.base(origin);JsonObject file=new JsonObject();file.addProperty("name","great_staircase");JsonArray palette=new JsonArray(),blocks=new JsonArray();Map<BlockState,Integer> lookup=new LinkedHashMap<>();
        for(int x=-34;x<34;x++)for(int z=-34;z<34;z++)for(int y=FinaleArchitecture.TOP-144;y<FinaleArchitecture.TOP+15;y++){
            var s=l.getBlockState(b.offset(x,y,z));if(s.isAir()||s.is(Blocks.LIGHT))continue;
            boolean visible=false;for(var side:Direction.values())if(l.getBlockState(b.offset(x,y,z).relative(side)).isAir()){visible=true;break;}if(!visible)continue;
            Integer i=lookup.get(s);if(i==null){i=lookup.size();lookup.put(s,i);palette.add(BlockState.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow());}
            JsonArray row=new JsonArray();row.add(x);row.add(y);row.add(z);row.add(i);blocks.add(row);
        }
        file.add("palette",palette);file.add("blocks",blocks);var folder=Path.of("../build/architecture-proof");Files.createDirectories(folder);Files.writeString(folder.resolve("great_staircase.json"),new Gson().toJson(file));
    }
    @GameTest(template="empty",batch="architecture_pass",timeoutTicks=200)
    public static void nativeEverySceneHasSupportedDetailAndPreservesOriginalsOnUpgrade(GameTestHelper h)throws Exception{
        var server=h.getLevel().getServer();var interior=HouseTestLevel.get(server);var oldHouse=HouseSavedData.get(server);var oldData=LabyrinthData.get(server);var oldMother=MotherCollection.get(server);
        BlockPos origin=new BlockPos(84000,80,84000);var house=new HouseSavedData();house.markSpawned(origin);
        server.overworld().getDataStorage().set("the_oldest_house",house);
        var data=new LabyrinthData();server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);server.overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());
        try{
            LabyrinthBuilder.clearAll();LabyrinthBuilder.rebuild(server);while(LabyrinthBuilder.isCarving())LabyrinthBuilder.tick(server);
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
            h.assertTrue(dressed==19&&props>=90,"all nineteen authored vignettes/camps receive supported detail; the copied Red Room stays personal");
            shellsAndEdges(h,server,origin,data);
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
            h.assertTrue(!LabyrinthBuilder.ensureBuilt(server),"the old layout schedules an in-place architectural upgrade");while(LabyrinthBuilder.isCarving())LabyrinthBuilder.tick(server);
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
            for(var scene:LabyrinthPlace.values())if(VignetteArchitecture.applies(scene)){
                var level=HouseTestLevel.get(server,NovelRooms.dimension(scene));var base=LabyrinthPlaces.base(origin,scene);var r=scene.room();
                for(var e:level.getEntitiesOfClass(Entity.class,box(base.offset(r.minX()-1,r.minY()-1,r.minZ()-1),base.offset(r.maxX()+1,r.maxY()+2,r.maxZ()+2))))e.discard();
            }
            server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);server.overworld().getDataStorage().set("the_oldest_house_mother",oldMother);LabyrinthBuilder.clearAll();LabyrinthDoors.clearAll();
        }
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
            var level=HouseTestLevel.get(server,NovelRooms.dimension(scene));var b=LabyrinthPlaces.base(origin,scene);var r=SceneShells.interior(scene);int panes=0;
            for(var at:BlockPos.betweenClosed(b.offset(r.minX()-3,r.minY(),r.minZ()-3),b.offset(r.maxX()+3,r.maxY(),r.maxZ()))){
                var rel=at.subtract(b);boolean outside=rel.getX()<r.minX()-1||rel.getX()>r.maxX()+1||rel.getZ()<r.minZ()-1;
                var st=level.getBlockState(at);if(outside&&(st.is(Blocks.GLASS_PANE)||st.getBlock() instanceof StainedGlassPaneBlock))panes++;
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
        // The barn's bank carries a wood, not a flat curtain of leaves.
        var barn=LabyrinthPlaces.base(origin,LabyrinthPlace.BARN_WELL);int curtain=0,cells=0;
        for(int x=-23;x<=23;x++)for(int z=-45;z<=4;z++)if(Landscapes.barnEdge(x,z)>=4){cells++;if(outside.getBlockState(barn.offset(x,8,z)).getBlock() instanceof LeavesBlock)curtain++;}
        h.assertTrue(curtain*4<cells,"the bank's skyline is broken by separate crowns: "+curtain+"/"+cells);
        // Anyone the plain has seen inside it is returned from beyond its edge.
        var walker=h.makeMockServerPlayerInLevel();
        try{
            walker.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            walker.teleportTo(outside,plain.getX()+.5,plain.getY(),plain.getZ()-20.5,180,0);OutdoorBounds.remember(walker);
            var inside=walker.position();walker.teleportTo(plain.getX()+40.5,plain.getY()+12,plain.getZ()-20.5);
            h.assertTrue(OutdoorBounds.check(walker,origin)&&walker.position().distanceTo(inside)<1.0E-3,"crossing the dunes returns the explorer to where they stood");
            h.assertTrue(!OutdoorBounds.check(walker,origin),"inside the plain nothing moves them");
        }finally{server.getPlayerList().remove(walker);}
    }

    private static void export(net.minecraft.server.level.ServerLevel l,BlockPos b,LabyrinthPlace scene)throws Exception{
        var r=scene.room();JsonObject file=new JsonObject();file.addProperty("name",scene.id());
        JsonArray palette=new JsonArray(),blocks=new JsonArray();Map<BlockState,Integer> lookup=new LinkedHashMap<>();
        int minZ=r.minZ(),maxZ=r.maxZ(),minY=Math.max(-1,r.minY()),maxY=Math.min(4,r.maxY());
        if(scene==LabyrinthPlace.GOATMAN){minZ=-79;maxZ=-45;}
        if(scene==LabyrinthPlace.HOLLOWAY_CAMP)minZ=-24;
        if(scene==LabyrinthPlace.TED_CAVER){minZ=-8;minY=-1;}
        if(scene==LabyrinthPlace.MOTHER_DEN)maxY=12;
        if(scene==LabyrinthPlace.PRESERVED_CAVE)maxY=4;
        if(scene==LabyrinthPlace.WHALE)maxY=9;
        if(scene==LabyrinthPlace.PLAIN)minZ=-105;
        if(LakeLandscape.isLake(scene))maxY=16;
        for(int x=r.minX();x<=r.maxX();x++)for(int y=minY;y<=maxY;y++)for(int z=minZ;z<=maxZ;z++){
            var at=b.offset(x,y,z);var s=l.getBlockState(at);
            if(s.isAir()||s.is(Blocks.BARRIER)||s.is(Blocks.LIGHT))continue;
            if(s.is(Blocks.WATER)&&(!LakeLandscape.isLake(scene)||!l.getBlockState(at.above()).isAir()))continue;
            // A documented cutaway removes roofs and the near walls, not interior contents.
            if(y>=0&&(x==r.maxX()||z==maxZ))continue;
            if(y>=0&&((scene==LabyrinthPlace.WHALE&&x==13)||(scene==LabyrinthPlace.HOSPITAL&&x==9)
                    ||(scene==LabyrinthPlace.KAREN_ROOM&&x==9)||(scene==LabyrinthPlace.ZAMPANO_COURTYARD&&x==12&&z<=-19)))continue;
            if(y>2&&s.is(Blocks.BIRCH_PLANKS))continue;
            boolean exposed=false;for(var side:Direction.values())if(l.getBlockState(at.relative(side)).isAir()||!l.getBlockState(at.relative(side)).isSolid()){exposed=true;break;}
            if(!exposed&&y!=minY)continue;
            Integer index=lookup.get(s);if(index==null){index=lookup.size();lookup.put(s,index);palette.add(BlockState.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow());}
            JsonArray row=new JsonArray();row.add(x);row.add(y);row.add(z);row.add(index);blocks.add(row);
        }
        file.add("palette",palette);file.add("blocks",blocks);Path folder=Path.of("../build/architecture-proof");Files.createDirectories(folder);
        Files.writeString(folder.resolve(scene.id()+".json"),new Gson().toJson(file));
    }
}
