package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_multiplayer")
@PrefixGameTestTemplate(false)
public final class StaircaseLeakTests {
    private static StaircaseAccessTests.Fixture current;
    private static BlockPos paper(StaircaseAccessTests.Fixture f){return f.base.offset(3,FinaleArchitecture.TOP,24);}
    private static void read(StaircaseAccessTests.Fixture f,ServerPlayer p,int index){
        var at=paper(f);
        for(int x=-3;x<=3;x++)for(int z=-2;z<=3;z++)for(int y=-1;y<=2;y++)f.put(f.level,at.offset(x,y,z),(y==-1?Blocks.STONE:Blocks.AIR).defaultBlockState());
        f.put(f.level,at,NoteSurfaceBlock.state(HouseMarginalia.Thread.HOUSEKEEPING,Direction.NORTH));
        p.teleportTo(f.level,at.getX()+.5+Math.max(0,f.players.indexOf(p))*1.2,at.getY(),at.getZ()+1.5,37,11);p.hasChangedDimension();p.connection.resetPosition();p.setDeltaMovement(Vec3.ZERO);
        FinaleProgress.phase(p.server,p.getUUID(),FinaleProgress.Phase.STAIRCASE);
        var own=f.data().stateEntry(StaircaseWriting.ID,p.getUUID().toString());var books=own.getCompound("Books");var indices=own.getCompound("Indices");var key=Long.toString(at.asLong());
        books.put(key,StaircaseNotes.specimen(index).save(p.registryAccess()));indices.putInt(key,index);own.put("Books",books);own.put("Indices",indices);f.data().setStateEntry(StaircaseWriting.ID,p.getUUID().toString(),own);
        StaircaseWriting.open(p,at);var book=p.containerMenu.getSlot(0).getItem();int last=book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()-1;
        if(last>0)p.containerMenu.clickMenuButton(p,100+last);
    }
    private static void run(GameTestHelper h,int coordinate,java.util.function.BiConsumer<StaircaseAccessTests.Fixture,long[]> step){
        var f=new StaircaseAccessTests.Fixture(h,coordinate);current=f;
        for(int slot=0;slot<8;slot++)f.chunks.hold(f.level,StaircaseLeakRooms.bounds(StaircaseLeakRooms.base(f.origin,slot)));
        f.chunks.hold(f.level,new AABB(paper(f)).inflate(35,6,35));long[] state={0,0};
        Runnable cleanup=()->{var ids=f.players.stream().map(ServerPlayer::getUUID).collect(java.util.stream.Collectors.toSet());var remove=new ArrayList<Entity>();for(var entity:f.level.getAllEntities())if(entity instanceof net.minecraft.world.entity.TamableAnimal pet&&ids.contains(pet.getOwnerUUID())||entity instanceof StaircaseReaderEcho echo&&echo.owner().filter(ids::contains).isPresent())remove.add(entity);remove.forEach(Entity::discard);StaircaseLeaks.clearForServer(f.level.getServer());for(int slot=0;slot<8;slot++){var base=StaircaseLeakRooms.base(f.origin,slot);for(int i=0;i<StaircaseLeakRooms.CELLS;i++)f.level.setBlock(StaircaseLeakRooms.position(base,i),Blocks.AIR.defaultBlockState(),18);}f.close();if(current==f)current=null;};
        h.runAfterDelay(1799,()->{if(!f.finished){String detail="Staircase scene stage "+state[0]+": "+f.players.stream().map(p->p.getGameProfile().getName()+" pos="+p.position()+" active="+StaircaseLeaks.active(p)+" offered="+StaircaseLeaks.offered(p)+" hurt="+p.hurtTime).toList();f.finished=true;cleanup.run();h.fail(detail);}});
        h.onEachTick(()->{if(f.finished||!f.chunks.ready())return;try{if(!f.started){f.start();StaircaseLeaks.clearForServer(f.level.getServer());}for(var player:new ArrayList<>(f.players))if(!player.isRemoved())player.doTick();step.accept(f,state);if(state[1]!=state[0]){TheOldestHouse.LOGGER.info("STAIRCASE LEAK native fixture {} stage {}",f.origin,state[0]);state[1]=state[0];}if(state[0]==99){f.finished=true;cleanup.run();h.succeed();}}catch(RuntimeException|Error ex){f.finished=true;cleanup.run();throw ex;}});
    }
    @AfterBatch(batch="leak_ownership") public static void cleanOwnership(ServerLevel l){cleanup();}
    @AfterBatch(batch="leak_chores") public static void cleanChores(ServerLevel l){cleanup();}
    @AfterBatch(batch="leak_recovery") public static void cleanRecovery(ServerLevel l){cleanup();}
    private static void cleanup(){if(current!=null){StaircaseLeaks.clearForServer(current.level.getServer());current.close();current=null;}}

    @GameTest(template="empty",batch="leak_ownership",timeoutTicks=1800)
    public static void nativeOriginalReadingWaitsCancelsAndKeepsPeersPetsAndExactReturns(GameTestHelper h){
        ServerPlayer[] p=new ServerPlayer[3];Wolf[] pet={null};Vec3[] source=new Vec3[2];ItemStack[] kept={null};CompoundTag[] snapshot={null};long[] born={0};
        run(h,652000,(f,s)->{
            long now=f.level.getGameTime();
            if(s[0]==0){
                p[0]=f.player(h,"leak_owner");p[1]=f.player(h,"leak_peer");p[2]=f.player(h,"leak_observer");read(f,p[0],9);read(f,p[1],9);p[2].teleportTo(f.level,p[0].getX()+2,p[0].getY(),p[0].getZ(),0,0);p[2].setGameMode(GameType.SPECTATOR);
                pet[0]=EntityType.WOLF.create(f.level);pet[0].setTame(true,true);pet[0].setOwnerUUID(p[0].getUUID());pet[0].moveTo(p[0].position().add(-1,0,0));pet[0].setHealth(13);pet[0].setNoGravity(true);f.level.addFreshEntity(pet[0]);CompanionOrders.issue(pet[0],p[0],CompanionOrders.Order.FOLLOW);
                kept[0]=new ItemStack(Items.DIAMOND_AXE);kept[0].set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Kept axe"));kept[0].setDamageValue(17);
                // Native MotherOfStrays ticks remember unstackable carried originals before entry.
                // Give the expected original that same provenance rather than comparing to an unstamped factory item.
                MotherCollection.rememberCarried(kept[0],p[0].getUUID(),now);
                p[0].getInventory().setItem(2,kept[0].copy());p[0].getInventory().selected=2;p[0].setHealth(15);p[0].getFoodData().setFoodLevel(14);p[0].addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,900,1));
                born[0]=now;s[0]=1;return;
            }
            if(s[0]==1&&now-born[0]>=22){p[0].closeContainer();p[1].closeContainer();h.assertTrue(StaircaseLeaks.offered(p[0])&&StaircaseLeaks.offered(p[1]),"both own full original readings offer a personal scene");p[0].moveTo(p[0].position().add(.3,0,0));s[0]=2;born[0]=now;return;}
            if(s[0]==2&&now-born[0]>=3){h.assertTrue(!StaircaseLeaks.offered(p[0])&&!StaircaseLeaks.active(p[0]),"moving cancels silently before any scene is consumed");h.assertTrue(!StaircaseLeaks.active(p[2]),"spectators receive no offer or progress");read(f,p[0],9);born[0]=now;s[0]=3;return;}
            if(s[0]==3&&now-born[0]>=22){p[0].closeContainer();source[0]=p[0].position();source[1]=p[1].position();born[0]=now;s[0]=4;return;}
            if(s[0]==4){if(now-born[0]<60)h.assertTrue(!StaircaseLeaks.active(p[0]),"closing the native reader still requires three occupied seconds");if(!StaircaseLeaks.active(p[0])||!StaircaseLeaks.active(p[1]))return;
                snapshot[0]=f.data().stateEntry(StaircaseLeaks.STATE,p[0].getUUID().toString()).getCompound("Active").getCompound("Return").copy();var a=StaircaseLeaks.activeBase(p[0]);var b=StaircaseLeaks.activeBase(p[1]);h.assertTrue(!a.equals(b)&&p[0].position().distanceToSqr(p[1].position())>1000,"neighbors on one tread receive separate native rooms");h.assertTrue(pet[0].isNoAi(),"the waiting companion is paused: "+pet[0].position()+" health="+pet[0].getHealth());h.assertTrue(pet[0].position().distanceToSqr(source[0])<36,"the same companion stays on the source tread: "+pet[0].position()+" / "+source[0]);h.assertTrue(pet[0].getHealth()==13,"the waiting companion keeps its health: "+pet[0].getHealth());h.assertTrue(CompanionOrders.followAcross(pet[0],p[0])==pet[0]&&pet[0].position().distanceToSqr(source[0])<36,"normal companion migration cannot follow into a leak");
                p[2].setCamera(p[0]);born[0]=now;s[0]=5;return;
            }
            if(s[0]==5&&now-born[0]>=2){h.assertTrue(p[2].getCamera()==p[2]&&p[2].position().distanceToSqr(source[0])<4,"even a directly attached spectator returns its camera to the stairs");
                var base=StaircaseLeaks.activeBase(p[0]);p[0].moveTo(base.offset(-2,0,-4).getCenter());StaircaseLeakChores.interact(p[0],base.offset(-2,1,-5));StaircaseLeakChores.interact(p[0],base.offset(-2,0,-5));StaircaseLeakChores.interact(p[0],base.offset(-2,0,-5));StaircaseLeakChores.interact(p[0],base.offset(-2,0,-5));
                h.assertTrue(StaircaseLeaks.progress(p[0]).getBoolean("Waxed")&&!StaircaseLeaks.progress(p[1]).getBoolean("Waxed"),"repeated drawer interactions are personal and never leak to a peer");p[0].getInventory().clearContent();p[0].removeAllEffects();p[0].setHealth(3);p[0].getFoodData().setFoodLevel(1);p[0].setYRot(150);p[0].setXRot(-20);StaircaseLeaks.returnNow(p[0]);
                h.assertTrue(!StaircaseLeaks.active(p[0])&&p[0].position().equals(source[0])&&p[0].getYRot()==37&&p[0].getXRot()==11,"early return restores the exact original pose");h.assertTrue(ItemStack.isSameItemSameComponents(kept[0],p[0].getInventory().getItem(2)),"the actual named, damaged and remembered axe retains its components: "+p[0].getInventory().getItem(2).getComponents()+" / "+kept[0].getComponents());h.assertTrue(p[0].getInventory().save(new ListTag()).equals(snapshot[0].getList("Inventory",Tag.TAG_COMPOUND)),"every saved inventory slot and native component is restored");h.assertTrue(p[0].getInventory().selected==snapshot[0].getInt("Selected"),"the actual selected hotbar slot survives");h.assertTrue(p[0].getHealth()==snapshot[0].getFloat("Health"),"health equals the actual entry snapshot: "+p[0].getHealth()+" / "+snapshot[0].getFloat("Health"));h.assertTrue(p[0].getFoodData().getFoodLevel()==snapshot[0].getCompound("Food").getInt("foodLevel"),"food equals the actual entry snapshot");h.assertTrue(p[0].hasEffect(MobEffects.DAMAGE_BOOST),"the actual strength effect survives");
                h.assertTrue(!pet[0].isNoAi()&&CompanionOrders.order(pet[0])==CompanionOrders.Order.FOLLOW&&pet[0].getHealth()==13,"the same pet resumes its original order and health");
                StaircaseLeaks.logout(new PlayerEvent.PlayerLoggedOutEvent(p[1]));h.assertTrue(!StaircaseLeaks.active(p[1])&&p[1].position().equals(source[1]),"disconnect restores its own tread before native player saving");var id=p[1].getUUID();NativeTestPlayers.remove(p[1]);f.players.remove(p[1]);f.reload();StaircaseLeaks.clearForServer(p[0].server);p[1]=NativeTestPlayers.survival(h,"leak_peer",id);f.players.add(p[1]);p[1].setNoGravity(true);p[1].teleportTo(f.level,source[1].x,source[1].y,source[1].z,37,11);StaircaseLeaks.login(new PlayerEvent.PlayerLoggedInEvent(p[1]));read(f,p[1],9);born[0]=now;s[0]=6;return;
            }
            if(s[0]==6&&now-born[0]>=22){p[1].closeContainer();h.assertTrue(!StaircaseLeaks.offered(p[1])&&!StaircaseLeaks.active(p[1]),"reconnect and replay cannot repeat a consumed personal scene");h.assertTrue(FinaleProgress.phase(p[0].server,p[0].getUUID())==FinaleProgress.Phase.STAIRCASE&&StaircaseFire.flames(FinaleProgress.player(p[0].server,p[0].getUUID()))==0,"scenery never advances phase or burns a leaf");pet[0].discard();s[0]=99;}
        });
    }

    @GameTest(template="empty",batch="leak_chores",timeoutTicks=1800)
    public static void allFiveNativeChoresUseVirtualPropsAndRestoreTheirSavedTemplates(GameTestHelper h){
        int[] indices={0,12,6,9,32};ServerPlayer[] p={null};int[] which={0};long[] born={0};List<BlockPos> rooms=new ArrayList<>();
        run(h,654000,(f,s)->{
            long now=f.level.getGameTime();if(s[0]==0){p[0]=f.player(h,"leak_chores");read(f,p[0],indices[which[0]]);born[0]=now;s[0]=1;return;}
            if(s[0]==1&&now-born[0]>=22){p[0].closeContainer();s[0]=2;return;}
            if(s[0]==2&&StaircaseLeaks.active(p[0])){
                var b=StaircaseLeaks.activeBase(p[0]);rooms.add(b);try{export(f.level,b,StaircaseLeakRooms.kind(indices[which[0]]));}catch(Exception ex){throw new RuntimeException(ex);}var inv=p[0].getInventory().save(new ListTag());
                switch(indices[which[0]]){
                    case 0->{p[0].moveTo(b.offset(1,0,-3).getCenter());StaircaseLeakChores.interact(p[0],b.offset(3,1,-2));for(int i=0;i<3;i++){StaircaseLeakChores.interact(p[0],b.offset(i-2,1,-4));StaircaseLeakChores.interact(p[0],b.offset(i-2,2,-5));}StaircaseLeakChores.interact(p[0],b.offset(1,1,-4));StaircaseLeakChores.interact(p[0],b.offset(3,1,-4));h.assertTrue(StaircaseLeaks.progress(p[0]).getInt("Cups")==3,"three native cups dry and hang before the plate goes under the pot");}
                    case 12->{p[0].moveTo(b.offset(-1,0,-4).getCenter());StaircaseLeakChores.interact(p[0],b.offset(-2,1,-4));for(var pair:new int[][]{{0,5},{1,8},{2,7},{3,6},{4,9}})for(int slot:pair)p[0].containerMenu.clicked(slot,0,ClickType.PICKUP,p[0]);h.assertTrue(StaircaseLeaks.progress(p[0]).getInt("Pairs")==5&&p[0].containerMenu.getSlot(10).getItem().is(StaircaseLeakRegistry.SOCKS.get(5).get()),"the native sock menu pairs five colors and leaves one unmatched");p[0].containerMenu.clicked(10,0,ClickType.QUICK_MOVE,p[0]);p[0].closeContainer();StaircaseLeakChores.interact(p[0],b.offset(-1,0,-6));h.assertTrue(p[0].containerMenu instanceof LecternMenu&&!p[0].containerMenu.clickMenuButton(p[0],3),"the trouser list is a native read-only page");p[0].closeContainer();p[0].moveTo(b.offset(1,0,-5).getCenter());StaircaseLeakChores.interact(p[0],b.offset(2,1,-6));}
                    case 6->{h.assertTrue(p[0].isPassenger(),"the car has a real native passenger seat");StaircaseLeakChores.interact(p[0],b.offset(0,1,-1));h.assertTrue(StaircaseLeaks.progress(p[0]).getBoolean("RadioOff"),"the car radio switches off through the real prop");}
                    case 9->{p[0].moveTo(b.offset(-2,0,-4).getCenter());StaircaseLeakChores.interact(p[0],b.offset(-2,1,-5));for(int i=0;i<4;i++)StaircaseLeakChores.interact(p[0],b.offset(-2,0,-5));h.assertTrue(StaircaseLeaks.active(p[0])&&StaircaseLeaks.progress(p[0]).getBoolean("Waxed"),"the repair scene waits for the reader to choose the doorway");p[0].moveTo(b.offset(0,0,2).getCenter());StaircaseLeaks.guardTick(p[0]);}
                    case 32->{p[0].moveTo(b.offset(-1,0,-3).getCenter());for(int i=0;i<3;i++)h.assertTrue(f.level.getBlockState(b.offset(i-2,2,-5)).getValue(StaircaseLeakProps.KIND).name().startsWith("CUP_HUNG"),"the later kitchen retains the cups hung earlier");StaircaseLeakChores.interact(p[0],b.offset(-3,1,-4));StaircaseLeakChores.interact(p[0],b.offset(0,3,-3));h.assertTrue(f.level.getBlockState(b.offset(2,2,-4)).getValue(StaircaseLeakProps.KIND)==StaircaseLeakProps.Kind.SMALL_LIGHT,"the big light leaves the little light on");}
                }
                h.assertTrue(inv.equals(p[0].getInventory().save(new ListTag())),"native scene menus and virtual chores never transfer items");born[0]=now;s[0]=3;return;
            }
            if(s[0]==3&&!StaircaseLeaks.active(p[0])){which[0]++;if(which[0]<indices.length){read(f,p[0],indices[which[0]]);born[0]=now;s[0]=1;}else{s[0]=4;born[0]=now;}return;}
            if(s[0]==4&&now-born[0]>=100){for(int i=0;i<rooms.size();i++){var b=rooms.get(i);var saved=f.data().stateEntry(StaircaseLeaks.ROOMS,Integer.toString(i));h.assertTrue(!saved.getBoolean("Dirty")&&saved.getBoolean("Ready"),"the native template restoration finishes and releases idle rooms");var template=StaircaseLeakRooms.Template.load(f.level,saved.getCompound("Template"));for(int cell=0;cell<StaircaseLeakRooms.CELLS;cell++)h.assertTrue(f.level.getBlockState(StaircaseLeakRooms.position(b,cell)).equals(template.state(cell)),"each completed room returns to its saved original geometry");}s[0]=99;}
        });
    }
    @GameTest(template="empty",batch="leak_recovery",timeoutTicks=1800)
    public static void nativeCombatCopiesAndAnActiveSaveReloadCannotStrandTheReader(GameTestHelper h){
        ServerPlayer[] p={null};net.minecraft.world.entity.monster.Zombie[] hostile={null};Vec3[] source={null};long[] born={0};BlockPos[] room={null};
        run(h,656000,(f,s)->{
            long now=f.level.getGameTime();
            if(s[0]==0){p[0]=f.player(h,"leak_recovery");p[0].setItemInHand(InteractionHand.MAIN_HAND,StaircaseNotes.specimen(9));p[0].getMainHandItem().getItem().use(p[0].level(),p[0],InteractionHand.MAIN_HAND);h.assertTrue(!StaircaseLeaks.offered(p[0]),"ordinary carried or borrowed written-book use cannot manufacture an original surface offer");read(f,p[0],9);born[0]=now;s[0]=1;return;}
            if(s[0]==1&&now-born[0]>=22){p[0].closeContainer();hostile[0]=EntityType.ZOMBIE.create(f.level);LabyrinthSpawnRules.allowInLabyrinth(hostile[0]);hostile[0].setNoAi(true);hostile[0].moveTo(p[0].position().add(2,0,0));f.level.addFreshEntity(hostile[0]);born[0]=now;s[0]=2;return;}
            if(s[0]==2&&now-born[0]>=3){h.assertTrue(!StaircaseLeaks.offered(p[0])&&!StaircaseLeaks.active(p[0]),"a native hostile within sixteen blocks cancels the offer without spending it");hostile[0].discard();read(f,p[0],9);born[0]=now;s[0]=3;return;}
            if(s[0]==3&&now-born[0]>=62){p[0].closeContainer();p[0].hurt(p[0].damageSources().generic(),1);h.assertTrue(!StaircaseLeaks.offered(p[0]),"native combat cancels an outstanding offer");read(f,p[0],9);born[0]=now;s[0]=4;return;}
            if(s[0]==4&&now-born[0]>=22){p[0].closeContainer();source[0]=p[0].position();s[0]=5;return;}
            if(s[0]==5&&StaircaseLeaks.active(p[0])){room[0]=StaircaseLeaks.activeBase(p[0]);f.level.setBlock(room[0].offset(3,0,-2),Blocks.DIAMOND_BLOCK.defaultBlockState(),18);p[0].drop(new ItemStack(Items.DIAMOND),false);float health=p[0].getHealth();p[0].hurt(p[0].damageSources().genericKill(),Float.MAX_VALUE);h.assertTrue(p[0].isAlive()&&!StaircaseLeaks.active(p[0])&&p[0].position().equals(source[0])&&p[0].getHealth()==health,"native lethal damage safely returns the living reader before a pocket-room death");read(f,p[0],6);born[0]=now;s[0]=6;return;}
            if(s[0]==6&&now-born[0]>=22){p[0].closeContainer();source[0]=p[0].position();s[0]=7;return;}
            if(s[0]==7&&StaircaseLeaks.active(p[0])){h.assertTrue(p[0].isPassenger(),"the native active-save fixture is seated in the car");f.reload();StaircaseLeaks.clearForServer(p[0].server);StaircaseLeaks.login(new PlayerEvent.PlayerLoggedInEvent(p[0]));h.assertTrue(!StaircaseLeaks.active(p[0])&&!p[0].isPassenger()&&p[0].position().equals(source[0]),"deserialized active scenery and the player backup recover to the exact source tread");born[0]=now;s[0]=8;return;}
            if(s[0]==8&&now-born[0]>=80){h.assertTrue(!f.level.getBlockState(room[0].offset(3,0,-2)).is(Blocks.DIAMOND_BLOCK)&&f.level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,StaircaseLeakRooms.bounds(room[0])).isEmpty(),"private edits and dropped props are removed by the saved template restoration");s[0]=99;}
        });
    }
    private static void export(ServerLevel l,BlockPos b,StaircaseLeakRooms.Kind kind)throws Exception{
        var file=new JsonObject();file.addProperty("name","staircase_leak_"+kind.name().toLowerCase(Locale.ROOT));var palette=new JsonArray();var blocks=new JsonArray();Map<net.minecraft.world.level.block.state.BlockState,Integer> ids=new LinkedHashMap<>();
        int half=kind==StaircaseLeakRooms.Kind.REPAIR?4:3;
        for(int y=-1;y<=3;y++)for(int z=-7;z<=2;z++)for(int x=-half-1;x<=half+1;x++){
            var state=l.getBlockState(b.offset(x,y,z));if(state.isAir()||state.is(Blocks.LIGHT)||y>=0&&(x==half+1||z==0&&kind!=StaircaseLeakRooms.Kind.CAR))continue;
            if(kind==StaircaseLeakRooms.Kind.CAR&&(x==2||z==2)&&y>0)continue;
            var id=ids.get(state);if(id==null){id=ids.size();ids.put(state,id);palette.add(net.minecraft.world.level.block.state.BlockState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow());}var row=new JsonArray();row.add(x);row.add(y);row.add(z);row.add(id);blocks.add(row);
        }
        file.add("palette",palette);file.add("blocks",blocks);var folder=Path.of("../build/architecture-proof");Files.createDirectories(folder);Files.writeString(folder.resolve(file.get("name").getAsString()+".json"),new Gson().toJson(file));
    }
}
